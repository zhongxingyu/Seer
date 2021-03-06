 /* dCache - http://www.dcache.org/
  *
  * Copyright (C) 2014 Deutsches Elektronen-Synchrotron
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.dcache.chimera.cli;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.CharStreams;
 import com.google.common.io.LineProcessor;
 import jline.ANSIBuffer;
 import jline.ConsoleReader;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import java.io.BufferedInputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Objects;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import dmg.util.Args;
 import dmg.util.CommandException;
 import dmg.util.CommandExitException;
 import dmg.util.CommandInterpreter;
 import dmg.util.CommandSyntaxException;
 import dmg.util.CommandThrowableException;
 import dmg.util.command.Argument;
 import dmg.util.command.Command;
 import dmg.util.command.HelpFormat;
 
 import org.dcache.acl.ACE;
 import org.dcache.acl.ACLException;
 import org.dcache.acl.enums.RsType;
 import org.dcache.chimera.ChimeraFsException;
 import org.dcache.chimera.DirectoryStreamB;
 import org.dcache.chimera.FileNotFoundHimeraFsException;
 import org.dcache.chimera.FileSystemProvider;
 import org.dcache.chimera.FsInode;
 import org.dcache.chimera.HimeraDirectoryEntry;
 import org.dcache.chimera.NotDirChimeraException;
 import org.dcache.chimera.UnixPermission;
 import org.dcache.chimera.posix.Stat;
 import org.dcache.util.Checksum;
 import org.dcache.util.ChecksumType;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static com.google.common.base.Strings.padStart;
 import static com.google.common.io.ByteStreams.toByteArray;
 import static java.util.Arrays.asList;
 
 public class Shell implements Closeable
 {
     private final FileSystemProvider fs;
     private final CommandInterpreter commandInterpreter;
     private final ConsoleReader console = new ConsoleReader();
     private final boolean isAnsiSupported;
     private final boolean hasConsole;
     private String path = "/";
     private FsInode pwd;
 
     public static void main(String[] arguments) throws Throwable
     {
         if (arguments.length < FsFactory.ARGC) {
             System.err.println(
                     "Usage: chimera " + FsFactory.USAGE + " [-e] [-f=<file>]|[-]|[COMMAND]");
             System.exit(4);
         }
 
         try (Shell shell = new Shell(arguments)) {
             Args args = new Args(arguments);
             if (args.hasOption("f")) {
                 try (InputStream in = new FileInputStream(args.getOption("f"))) {
                     shell.execute(new BufferedInputStream(in), System.out, args.hasOption("e"));
                 }
             } else if (args.argc() > FsFactory.ARGC) {
                 args.shift(FsFactory.ARGC);
                 if (args.argc() == 1 && args.argv(0).equals("-")) {
                     shell.execute(System.in, System.out, args.hasOption("e"));
                 } else {
                     shell.execute(args);
                 }
             } else if (!shell.hasConsole) {
                 shell.execute(System.in, System.out, args.hasOption("e"));
             } else {
                 shell.console();
             }
         }
     }
 
     public Shell(String[] args) throws Exception
     {
         fs = FsFactory.createFileSystem(args);
         pwd = fs.path2inode(path);
         commandInterpreter = new CommandInterpreter(this);
         hasConsole = System.console() != null;
         isAnsiSupported = console.getTerminal().isANSISupported() && hasConsole;
     }
 
     @Override
     public void close() throws IOException
     {
         fs.close();
     }
 
     public void execute(InputStream in, final PrintStream out, final boolean echo) throws IOException
     {
         CharStreams.readLines(
                 new InputStreamReader(in, Charsets.US_ASCII),
                 new LineProcessor<Object>()
                 {
                     @Override
                     public boolean processLine(String line) throws IOException
                     {
                         try {
                             if (echo) {
                                 out.println(line);
                             }
                             String s = Objects.toString(commandInterpreter.command(new Args(line)), null);
                             if (!isNullOrEmpty(s)) {
                                 out.println(s);
                             }
                             return true;
                         } catch (CommandException e) {
                             throw new IOException(e);
                         }
                     }
 
                     @Override
                     public Object getResult()
                     {
                         return null;
                     }
                 });
     }
 
     public void execute(Args args) throws Throwable
     {
         String out;
         try {
             if (isAnsiSupported && args.argc() > 0) {
                 if (args.argv(0).equals("help")) {
                     args.shift();
                     args = new Args("help -format=" + HelpFormat.ANSI + args.toString());
                 }
             }
             try {
                 out = Objects.toString(commandInterpreter.command(args), null);
             } catch (CommandThrowableException e) {
                 throw e.getCause();
             }
         } catch (ChimeraFsException e) {
             out = new ANSIBuffer().red(e.getMessage()).toString(isAnsiSupported);
         } catch (CommandSyntaxException e) {
             ANSIBuffer sb = new ANSIBuffer();
             sb.red("Syntax error: " + e.getMessage() + "\n");
             String help  = e.getHelpText();
             if (help != null) {
                 sb.append(help);
             }
             out = sb.toString(isAnsiSupported);
         } catch (CommandExitException e) {
             throw e;
         } catch (Exception e) {
             out = new ANSIBuffer().red(e.toString()).toString(isAnsiSupported);
         }
         if (!isNullOrEmpty(out)) {
             console.printString(out);
             if (out.charAt(out.length() - 1) != '\n') {
                 console.printNewline();
             }
             console.flushConsole();
         }
     }
 
     public void console() throws Throwable
     {
         try {
             while (true) {
                 String prompt = new ANSIBuffer().bold("chimera:" + path + "# ").toString(isAnsiSupported);
                 String str = console.readLine(prompt);
                 if (str == null) {
                     break;
                 }
                 execute(new Args(str));
             }
         } catch (CommandExitException ignored) {
         }
     }
 
     @Nonnull
     private FsInode lookup(@Nullable File path) throws ChimeraFsException
     {
         if (path == null) {
             return pwd;
         } else if (path.isAbsolute()) {
             return fs.path2inode(path.toString());
         } else {
             return fs.path2inode(path.toString(), pwd);
         }
     }
 
     @Command(name = "exit", hint = "exit chimera shell")
     public class ExitComamnd implements Callable<Serializable>
     {
         @Override
         public Serializable call() throws CommandExitException
         {
             throw new CommandExitException();
         }
     }
 
     @Command(name = "cd", hint = "change current directory")
     public class CdCommand implements Callable<Serializable>
     {
         @Argument
         File path;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             pwd = lookup(path);
             Shell.this.path = (pwd.getParent() != null) ? fs.inode2path(pwd) : "/";
             return null;
         }
     }
 
    @Command(name = "chgrp", hint = "change file group")
     public class ChgrpCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         int gid;
 
         @Argument(index = 1)
         File path;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             lookup(path).setGID(gid);
             return null;
         }
     }
 
    @Command(name = "chmod", hint = "change file mode")
     public class ChmodCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         String mode; // octal
 
         @Argument(index = 1)
         File path;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             lookup(path).setMode(Integer.parseInt(mode, 8));
             return null;
         }
     }
 
    @Command(name = "chown", hint = "change file owner and group")
     public class ChownCommand implements Callable<Serializable>
     {
         @Argument(index = 0, valueSpec = "UID[:GID]")
         String owner;
 
         @Argument(index = 1)
         File path;
 
         private int _uid;
         private int _gid;
 
         private void parseOwner(String ownership)
         {
             int colon = ownership.indexOf(':');
 
             if (colon == -1) {
                 _uid = parseInteger(ownership);
                 _gid = -1;
             } else {
                 checkArgument(colon > 0 && colon < ownership.length() - 1,
                               "Colon must separate two integers.");
                 _uid = parseInteger(ownership.substring(0, colon));
                 _gid = parseInteger(ownership.substring(colon + 1));
                 checkArgument(_gid >= 0, "GID must be 0 or greater.");
             }
 
             checkArgument(_uid >= 0, "UID must be 0 or greater.");
         }
 
         private int parseInteger(String value)
         {
             try {
                 return Integer.parseInt(value);
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Only integer values are allowed and \"" + value +"\" is not an integer.");
             }
         }
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             parseOwner(owner);
 
             FsInode inode = lookup(path);
             inode.setUID(_uid);
             if (_gid != -1) {
                 pwd.setGID(_gid);
             }
             return null;
         }
     }
 
     @Command(name = "ls", hint = "list directory contents")
     public class LsCommand implements Callable<Serializable>
     {
         /* The block size is purely nominal; we use 1k here as historically
          * filesystems have used a 1k block size. */
         private final int BLOCK_SIZE = 1024;
 
         private final  int[] INT_SIZE_TABLE = {9, 99, 999, 9999, 99999,
                 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
 
         private final DateFormat WITH_YEAR =
                 new SimpleDateFormat("MMM dd  yyyy");
 
         private final DateFormat WITHOUT_YEAR =
                 new SimpleDateFormat("MMM dd HH:mm");
 
         private int nlinkWidth = 0;
         private int uidWidth = 0;
         private int gidWidth = 0;
         private int sizeWidth = 0;
 
         private final long sixMonthsInPast = sixMonthsInPast();
         private final long oneHourInFuture = oneHourInFuture();
 
         @Argument(required = false)
         File path;
 
         public Serializable call() throws IOException
         {
             List<HimeraDirectoryEntry> entries = new LinkedList<>();
 
             long totalBlocks = 0;
 
             HimeraDirectoryEntry dot = null;
             HimeraDirectoryEntry dotdot = null;
 
             FsInode inode = lookup(path);
 
             try (DirectoryStreamB<HimeraDirectoryEntry> dirStream = inode.newDirectoryStream()) {
                 for (HimeraDirectoryEntry entry : dirStream) {
                     String name = entry.getName();
                     Stat stat = entry.getStat();
 
                     if (name.equals(".")) {
                         dot = entry;
                     } else if (name.equals("..")) {
                         dotdot = entry;
                     } else {
                         entries.add(entry);
                     }
 
                     totalBlocks = updateTotalBlocks(totalBlocks, stat);
                     nlinkWidth = updateMaxWidth(nlinkWidth, stat.getNlink());
                     uidWidth = updateMaxWidth(uidWidth, stat.getUid());
                     gidWidth = updateMaxWidth(gidWidth, stat.getGid());
                     sizeWidth = updateMaxWidth(sizeWidth, stat.getSize());
                 }
             }
 
             console.printString("total " + totalBlocks);
             console.printNewline();
             printEntry(dot);
             printEntry(dotdot);
             for (HimeraDirectoryEntry entry : entries) {
                 printEntry(entry);
             }
             return null;
         }
 
         private void printEntry(HimeraDirectoryEntry entry) throws IOException
         {
             if (entry != null) {
                 Stat stat = entry.getStat();
                 String s = String.format("%s %s %s %s %s %s %s",
                                          permissionsFor(stat),
                                          pad(stat.getNlink(), nlinkWidth),
                                          pad(stat.getUid(), uidWidth),
                                          pad(stat.getGid(), gidWidth),
                                          pad(stat.getSize(), sizeWidth),
                                          dateOf(stat.getMTime()),
                                          entry.getName());
 
                 console.printString(s);
                 console.printNewline();
             }
         }
 
         // For files with a time that is more than 6 months old or more than 1
         // hour into the future, the timestamp contains the year instead of the
         // time of day.
         private String dateOf(long time)
         {
             Date d = new Date(time);
 
             if(time < sixMonthsInPast || time > oneHourInFuture) {
                 return WITH_YEAR.format(d);
             } else {
                 return WITHOUT_YEAR.format(d);
             }
         }
 
         private long sixMonthsInPast()
         {
             Calendar calendar = Calendar.getInstance();
             calendar.add(Calendar.MONTH, -6);
             return calendar.getTimeInMillis();
         }
 
         private long oneHourInFuture()
         {
             return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
         }
 
         private String pad(long value, int width)
         {
             String str = String.valueOf(value);
             return padStart(str, width, ' ');
         }
 
         private long updateTotalBlocks(long total, Stat stat)
         {
             // calculate number of blocks, but rounding up
             long nBlocks = 1 + (stat.getSize() -1)/ BLOCK_SIZE;
             return total + nBlocks;
         }
 
         private int updateMaxWidth(int max, int value)
         {
             int width = widthOf(value);
             return width > max ? width : max;
         }
 
         private int updateMaxWidth(int max, long value)
         {
             int width = widthOf(value);
             return width > max ? width : max;
         }
 
         private String permissionsFor(Stat stat)
         {
             return new UnixPermission(stat.getMode()).toString();
         }
 
         // Requires positive x
         private int widthOf(int x)
         {
             for (int i=0; ; i++) {
                 if (x <= INT_SIZE_TABLE[i]) {
                     return i+1;
                 }
             }
         }
 
         // Requires positive x
         private int widthOf(long x)
         {
             if(x <= Integer.MAX_VALUE) {
                 return widthOf((int) x);
             }
 
             // x is more than 0x7fffffff or 2147483647
 
             if (x < 1000000000000L) { // from 10 to 12 digits
                 if (x < 10000000000L) {
                     return 10;
                 } else {
                     return x < 100000000000L ? 11 : 12;
                 }
             } else { // 13 or more digits
                 if (x < 10000000000000000L) {
                     if (x < 100000000000000L) {
                         return x < 10000000000000L ? 13 : 14;
                     } else {
                         return x < 1000000000000000L ? 15 : 16;
                     }
                 } else {
                     if (x < 1000000000000000000L) {
                         return x < 100000000000000000L ? 17 : 18;
                     } else {
                         return 19;
                     }
                 }
             }
         }
     }
 
     @Command(name = "lstag", hint = "list directory tags")
     public class LstagCommand implements Callable<Serializable>
     {
         @Argument
         File path;
 
         @Override
         public Serializable call() throws IOException
         {
             String[] tags = fs.tags(lookup(path));
             console.printString("Total: " + tags.length);
             console.printNewline();
             for (String tag : tags) {
                 console.printString(tag);
                 console.printNewline();
             }
             return null;
         }
     }
 
     @Command(name = "mkdir", hint = "make directory")
     public class MkdirCommand implements Callable<Serializable>
     {
         @Argument
         File path;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             if (path.isAbsolute()) {
                 fs.mkdir(path.toString());
             } else {
                 fs.mkdir(Shell.this.path + "/" + path);
             }
             return null;
         }
     }
 
     @Command(name = "rmdir", hint = "remove directory")
     public class RmdirCommand implements Callable<Serializable>
     {
         @Argument
         File path;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             FsInode inode = lookup(path);
             if (!inode.isDirectory()) {
                 throw new NotDirChimeraException(inode);
             }
             fs.remove(inode);
             return null;
         }
     }
 
     @Command(name = "readtag", hint = "display tag data")
     public class ReadTagCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
         @Argument(index = 1)
         String tag;
 
         @Override
         public Serializable call() throws IOException
         {
             FsInode inode = lookup(path);
             Stat stat = fs.statTag(inode, tag);
             byte[] data = new byte[(int) stat.getSize()];
             fs.getTag(inode, tag, data, 0, data.length);
             console.printString(new String(data));
             console.printNewline();
             return null;
         }
     }
 
     @Command(name = "writetag", hint = "write tag data")
     public class WriteTagCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
         @Argument(index = 1)
         String tag;
 
         @Argument(index = 2, required = false)
         String data;
 
         @Override
         public Serializable call() throws IOException
         {
             FsInode inode = lookup(path);
             try {
                 fs.statTag(inode, tag);
             } catch (FileNotFoundHimeraFsException fnf) {
                 fs.createTag(inode, tag);
             }
 
             byte[] bytes = (data == null)
                     ? toByteArray(console.getInput())
                     : newLineTerminated(data).getBytes();
             if (bytes.length > 0) {
                 fs.setTag(inode, tag, bytes, 0, bytes.length);
             }
 
             return null;
         }
 
         private String newLineTerminated(String s)
         {
             return s.endsWith("\n") ? s : s + "\n";
         }
     }
 
     @Command(name = "rmtag", hint = "remove tag from directory")
     public class RmTagCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
         @Argument(index = 1)
         String tag;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             FsInode inode = lookup(path);
             fs.removeTag(inode, tag);
             return null;
         }
     }
 
     @Command(name = "setfacl", hint = "change access control lists",
              usage = "Sets a new ACL consisting of one or more ACEs to a resource (a file or directory), " +
                      "which is defined by its pnfsId or globalPath.\n\n" +
 
                      "Each ACE defines permissions to access this resource " +
                      "for a subject (a user or group of users). " +
                      "ACEs are ordered by significance, i.e., first match wins.\n\n" +
 
                      "Description of the ACE structure. \n\n" +
 
                      "The element <subject> defines the subject of the ACE and " +
                      "must be one of the following values: \n" +
                      "   USER:<who_id> : user identified by the virtual user ID <who_id> \n" +
                      "  GROUP:<who_id> : group identified by the virtual group ID <who_id> \n" +
                      "          OWNER@ : user who owns the resource\n" +
                      "          GROUP@ : group that owns the resource \n" +
                      "       EVERYONE@ : world, including the owner and owning group\n" +
                      "      ANONYMOUS@ : accessed without any authentication \n" +
                      "  AUTHENTICATED@ : any authenticated user (opposite of ANONYMOUS) \n\n" +
 
                      "The MASK is a set of bits describing how correspondent permissions will " +
                      "be modified for users matching the SUBJECT. If MASK is preceded by a '+' then " +
                      "corresponding operations are allowed. If it is preceded by a '-' then " +
                      "corresponding operations are disallowed. Some bits apply only to regular " +
                      "files, others apply only to directories, and some to both. A bit is converted " +
                      "to the appropriate one, as indicated in parentheses.\n\n" +
 
                      "The following access permissions may be used: \n" +
                      "   r : Permission to read the data of a file (converted to 'l' if directory). \n" +
                      "   l : Permission to list the contents of a directory (converted to 'r' if file). \n" +
                      "   w : Permission to modify a file's data anywhere in the file's offset range.\n" +
                      "       This includes the ability to write to any arbitrary offset and \n" +
                      "       as a result to grow the file. (Converted to 'f' if directory).\n" +
                      "   f : Permission to add a new file in a directory (converted to 'w' if file).\n" +
                      "   a : The ability to modify a file's data, but only starting at EOF \n"+
                      "       (converted to 's' if directory).\n" +
                      "   s : Permission to create a subdirectory in a directory (converted to 'a' if file).\n" +
                      "   x : Permission to execute a file or traverse a directory.\n" +
                      "   d : Permission to delete the file or directory.\n" +
                      "   D : Permission to delete a file or directory within a directory.\n" +
                      "   n : Permission to read the named attributes of a file or to lookup \n" +
                      "       the named attributes directory.\n" +
                      "   N : Permission to write the named attributes of a file or \n" +
                      "       to create a named attribute directory.\n" +
                      "   t : The ability to read basic attributes (non-ACLs) of a file or directory.\n" +
                      "   T : Permission to change the times associated with a file \n" +
                      "       or directory to an arbitrary value.\n" +
                      "   c : Permission to read the ACL.\n" +
                      "   C : Permission to write the acl and mode attributes.\n" +
                      "   o : Permission to write the owner and owner group attributes.\n\n" +
 
                      "To enable ACL inheritance, the optional element <flags> must be defined. " +
                      "Multiple bits may be specified as a simple concatenated list of letters. " +
                      "Order doesn't matter.\n" +
                      "   f : Can be placed on a directory and indicates that this ACE \n" +
                      "       should be added to each new file created.\n" +
                      "   d : Can be placed on a directory and indicates that this ACE \n" +
                      "       should be added to each new directory created.\n" +
                      "   o : Can be placed on a directory and indicates that this ACE \n" +
                      "       should be ignored for this directory.\n" +
                      "       Any ACE that inherit from an ACE with 'o' flag set will not have the 'o' flag set.\n" +
                      "       Therefore, ACEs with this bit set take effect only if they are inherited \n" +
                      "       by newly created files or directories as specified by the above two flags.\n" +
                      "       REMARK: If 'o' flag is present on an ACE, then \n" +
                      "       either 'd' or 'f' (or both) must be present as well.\n\n" +
 
                      "Examples: \n" +
                      "setfacl /pnfs/example.org/data/TestDir USER:3750:+lfs:d EVERYONE@:+l GROUP:8745:-s USER:3452:+D\n" +
                      "       Permissions for TestDir are altered so: \n" +
                      "       First ACE: User with id 3750 (USER:3750) is allowed to \n"+
                      "          list directory contents (l), \n" +
                      "          create files (f), \n"+
                      "          create subdirectories (s), \n" +
                      "          and these permissions will be inherited by all newly created \n" +
                      "          subdirectories as well (d). \n" +
                      "       Second ACE: Everyone (EVERYONE@) is allowed to \n"+
                      "          list directory contents. \n" +
                      "       Third ACE: Group with id 8745 (GROUP:8745) is not allowed to \n" +
                      "          create subdirectories.\n" +
                      "       Fourth ACE: User with id 3452 (USER:3452) is allowed to \n" +
                      "          delete objects within this directory (D). The user must also have \n" +
                      "          the delete permission (d) for the object to be deleted. See next example.\n\n " +
                      "       \n" +
                      "setfacl /pnfs/example.org/data/TestDir/TestFile USER:3452:+d\n" +
                      "       Permissions for TestFile are altered so: \n" +
                      "          User with id 3452 (USER:3452) is allowed to \n" +
                      "          delete this resource (d). To delete TestFile, the user must also \n"+
                      "          have permission to delete directory contents (D). See previous example.\n\n" +
 
                      "For further information on ACLs in dCache please refer to: " +
                      "http://trac.dcache.org/trac.cgi/wiki/Integrate")
     public class SetFaclCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
         @Argument(index = 1, valueSpec = "SUBJECT:+|-MASK[:FLAGS]")
         ACE[] acl;
 
 
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             fs.setACL(lookup(path), asList(acl));
             return null;
         }
     }
 
     @Command(name = "getfacl", hint = "display access control lists")
     public class GetFaclComamnd implements Callable<Serializable>
     {
         @Argument
         File path;
 
         @Override
         public Serializable call() throws IOException, ACLException
         {
             FsInode inode = lookup(path);
             List<ACE> acl = fs.getACL(inode);
             for (ACE ace : acl) {
                 console.printString(ace.toExtraFormat(inode.isDirectory() ? RsType.DIR : RsType.FILE));
                 console.printNewline();
             }
             return null;
         }
     }
 
     @Command(name = "writedata", hint = "write file content",
              usage = "Be aware that such data is stored in the Chimera database and not in dCache. " +
                      "The data will not be accessible through dCache.")
     public class WriteDataCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
         @Argument(index = 1, required = false)
         String data;
 
         @Override
         public Serializable call() throws IOException
         {
             byte[] bytes = data != null
                     ? toByteArray(System.in)
                     : newLineTerminated(data).getBytes();
             writeDataIntoFile(bytes);
             return null;
         }
 
         private void writeDataIntoFile(byte[] data)
                 throws ChimeraFsException
         {
             FsInode inode;
             try {
                 inode = lookup(path);
             } catch (FileNotFoundHimeraFsException fnf) {
                 if (path.isAbsolute()) {
                     inode = fs.createFile(path.toString());
                 } else {
                     inode = fs.createFile(lookup(path.getParentFile()), path.getName());
                 }
             }
             fs.setInodeIo(inode, true);
             inode.write(0, data, 0, data.length);
         }
 
         private String newLineTerminated(String s)
         {
             return s.endsWith("\n") ? s : s + "\n";
         }
     }
 
     @Command(name = "checksum list", hint  = "list checksums of file")
     public class ChecksumListCommand implements Callable<Serializable>
     {
         @Argument
         File path;
 
         @Override
         public Serializable call() throws IOException
         {
             FsInode inode = lookup(path);
             for (ChecksumType type : ChecksumType.values()) {
                 String checksum = fs.getInodeChecksum(inode, type.getType());
                 if (checksum != null) {
                     console.printString(type.getName() + ":" + checksum);
                     console.printNewline();
                 }
             }
             return null;
         }
     }
 
     @Command(name = "checksum get", hint = "display checksum of file")
     public class ChecksumGetComamnd implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
        @Argument(index = 1)
         ChecksumType type;
 
         @Override
         public Serializable call() throws IOException
         {
             FsInode inode = lookup(path);
             String checksum = fs.getInodeChecksum(inode, type.getType());
             if (checksum == null) {
                 console.printString("No checksum of type " + type.getName());
             } else {
                 console.printString(checksum);
             }
             console.printNewline();
             return null;
         }
     }
 
     @Command(name = "checksum add", hint = "add checksum to file")
     public class ChecksumAddCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
        @Argument(index = 1)
         ChecksumType type;
 
         @Argument(index = 2)
         String checksum;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             Checksum c = new Checksum(type, checksum);
             FsInode inode = lookup(path);
             fs.setInodeChecksum(inode, type.getType(), c.getValue());
             return null;
         }
     }
 
     @Command(name = "checksum delete", hint = "remove checkusm from file")
     public class ChecksumDeleteCommand implements Callable<Serializable>
     {
         @Argument(index = 0)
         File path;
 
        @Argument(index = 1)
         ChecksumType type;
 
         @Override
         public Serializable call() throws ChimeraFsException
         {
             FsInode inode = lookup(path);
             fs.removeInodeChecksum(inode, type.getType());
             return null;
         }
     }
 }
