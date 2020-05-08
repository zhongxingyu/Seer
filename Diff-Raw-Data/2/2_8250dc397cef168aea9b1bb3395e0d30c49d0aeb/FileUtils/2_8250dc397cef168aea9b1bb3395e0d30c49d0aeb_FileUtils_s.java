 package nl.nikhef.jgridstart.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /** Some file-related utilities */
 public class FileUtils {
     static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
     
     /** Copy a file from one place to another. This calls an external copy
      * program on the operating system to make sure it is properly copied and
      * permissions are retained.
      * @throws IOException */
     public static boolean CopyFile(File in, File out) throws IOException {
 	String[] cmd;
 	if (System.getProperty("os.name").startsWith("Windows")) {
 	    boolean hasRobocopy = false;
 	    
 	    // windows: use special copy program to retain permissions.
 	    //   on Vista, "xcopy /O" requires administrative rights, so we
 	    //   have to resort to using robocopy there.
 	    try {
 		int ret = Exec(new String[]{"robocopy.exe"});
		if (ret==0 && ret==16) hasRobocopy = true;
 	    } catch (Exception e) { }
 	    
 	    if (hasRobocopy) {
 		// we have robocopy. But ... its destination filename
 		//   needs to be equal to the source filename :(
 		//   So we rename any existing file out of the way, copy
 		//   the new file, rename it to the new name, and restore
 		//   the optional original file. All this is required to
 		//   copy a file retaining its permissions.
 		// TODO proper return value handling (!)
 		
 		// move old file out of the way
 		File origFile = new File(out.getParentFile(), in.getName());
 		File origFileRenamed = null;
 		if (origFile.exists()) {
 		    origFileRenamed = new File(origFile.getParentFile(), origFile.getName()+".xxx_tmp");
 		    origFile.renameTo(origFileRenamed);
 		} else {
 		    origFile = null;
 		}
 		// copy file to new place
 		cmd = new String[]{"robocopy.exe",
 			    in.getParent(), out.getParent(),
 			    in.getName(),
 			    "/SEC", "/NP", "/NS", "/NC", "/NFL", "/NDL"};
 		int ret = Exec(cmd);
 		boolean success = ret < 4 && ret >= 0;
 		// rename new file
 		if (success) {
 		    new File(out.getParentFile(), in.getName()).renameTo(out);
 		}
 		
 		// move old file to original place again
 		if (origFile!=null)
 		    origFileRenamed.renameTo(origFile);
 		
 		return success;
 	    } else {
 		// use xcopy instead
 		cmd = new String[]{"xcopy.exe",
 		    in.getAbsolutePath(),
 		    out.getAbsolutePath(),
 		    "/O", "/Q", "/Y"};
 		// If the file/ doesn't exist on copying, xcopy will ask whether you want
 		// to create it as a directory or just copy a file, so we always
 		// just put "F" in xcopy's stdin.
 		return Exec(cmd, "F", null) == 1;
 	    }
 	    
 	} else {
 	    // other, assume unix-like
 	    cmd = new String[]{"cp",
 		    "-f", "-p",
 		    in.getAbsolutePath(), out.getAbsolutePath()};
 	    return Exec(cmd) == 0;
 	}
     }
     
     /** List ordinary files in a directory.
      * <p>
      * 
      * @see File#listFiles
      * @param path Directory to list files in
      */
     public static File[] listFilesOnly(File path) {
 	return path.listFiles(new FileFilter() {
 	    public boolean accept(File pathname) {
 		return pathname.isFile();
 	    }
 	});
     }
     
     /** Copies a list of files to a directory.
      * <p>
      * When an error occurs, files already copied are removed again and
      * an {@linkplain IOException} is thrown.
      * <p>
      * It is discouraged to have files with the same name existing
      * already in the destination path.
      */
     public static void CopyFiles(File[] fromFiles, final File toPath) throws IOException {
 	EachFile(fromFiles, new FileCallback() {
 	    public void action(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!CopyFile(f, toFile))
 		    throw new IOException("Copy failed: "+f+" -> "+toFile);
 	    }
 	    public void reverseAction(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!toFile.delete())
 		    throw new IOException("Delete failed: "+toFile);
 	    }
 	});
     }
     
     /** Moves a list of files to a directory.
      * <p>
      * When an error occurs, files already moved are put back and an
      * {@linkplain IOException} is thrown.
      * <p>
      * It is discouraged to have files with the same name
      * existing already in the destination path.
      */
     public static void MoveFiles(File[] fromFiles, final File toPath) throws IOException {
 	EachFile(fromFiles, new FileCallback() {
 	    public void action(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!f.renameTo(toFile))
 		    throw new IOException("Move failed: "+f+" -> "+toFile);
 	    }
 	    public void reverseAction(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!toFile.renameTo(f))
 		    throw new IOException("Move back failed: "+toFile+" -> "+f);
 	    }
 	});
     }
     
     /** Runs a callback on each file specified.
      * <p>
      * Implements a rollback mechanism: when the callback throws an {@linkplain IOException},
      * the previous operations are undone by means of the {@link FileCallback#reverseAction}
      * method.
      */
     protected static void EachFile(File[] fromFiles, FileCallback callback) throws IOException {
 	// then copy or move
 	int i=0;
 	try {
 	    for (i=0; i<fromFiles.length; i++) {
 		callback.action(fromFiles[i]);
 	    }
 	} catch (IOException e1) {
 	    String extra = "";
 	    // move already moved files back
 	    for (int j=0; j<i; j++) {
 		try {
 		    callback.reverseAction(fromFiles[j]);
 		} catch (IOException e2) {
 		    extra += "\n" + e2.getLocalizedMessage();
 		}
 	    }
 	    // and propagate exception
 	    if (extra=="")
 		throw e1;
 	    else
 		throw new IOException(e1.getLocalizedMessage() +
 			"\n\nNote that the following errors occured on rollback(!):\n" +
 			extra);
 	}
     }
     /** Callback handler for {@link #EachFile} */
     protected interface FileCallback {
 	/** Does an action on a File */
 	public void action(File f) throws IOException;
 	/** Reverses the action of {@linkplain #action} on a File */
 	public void reverseAction(File f) throws IOException;
     }
     
     /**
      * Return the contents of a text file as String
      */
     public static String readFile(File file) throws IOException {
 	String s = System.getProperty("line.separator");
 	BufferedReader r = new BufferedReader(new FileReader(file));
 	StringBuffer buf = new StringBuffer();
 	String line;
 	while ( (line = r.readLine() ) != null) {
 	    buf.append(line);
 	    buf.append(s);
 	}
 	r.close();
 	return buf.toString();
     }
 
     /**
      * Change file permissions
      * <p>
      * Not supported natively until java 1.6. Bad Java.
      * Note that the ownerOnly argument differs from Java. When {@code ownerOnly} is
      * true for Java's {@link File#setReadable}, {@link File#setWritable} or
      * File.setExecutable(), the other/group permissions are left as they are.
      * This method resets them instead. When ownerOnly is false, behaviour is
      * as Java's.
      * 
      * @param file File to set permissions on
      * @param read  if reading should be allowed
      * @param write if writing should be allowed
      * @param exec if executing should be allowed
      * @param ownerOnly true to set group&world permissions to none,
      *                  false to set them all alike
      */
     static public boolean chmod(File file, boolean read, boolean write,
 	    boolean exec, boolean ownerOnly) {
 	try {
 	    // Try Java 1.6 method first.
 	    // This is also compilable on lower java versions
 	    boolean ret = true;
 	    Method setReadable = File.class.getDeclaredMethod("setReadable",
 		    new Class[] { boolean.class, boolean.class });
 	    Method setWritable = File.class.getDeclaredMethod("setWritable",
 		    new Class[] { boolean.class, boolean.class });
 	    Method setExecutable = File.class.getDeclaredMethod("setExecutable",
 		    new Class[] { boolean.class, boolean.class });
 
 	    // first remove all permissions if ownerOnly is wanted, because File.set*
 	    // doesn't touch other/group permissions when ownerOnly is true.
 	    if (ownerOnly) {
 		ret &= (Boolean)setReadable.invoke(file, new Object[]{ false, false });
 		ret &= (Boolean)setWritable.invoke(file, new Object[]{ false, false });
 		ret &= (Boolean)setExecutable.invoke(file, new Object[]{ false, false });
 	    }
 	    // then set owner/all permissions
 	    ret &= (Boolean)setReadable.invoke(file, new Object[] { read, ownerOnly });
 	    ret &= (Boolean)setWritable.invoke(file, new Object[] { write, ownerOnly });
 	    ret &= (Boolean)setExecutable.invoke(file, new Object[] { exec, ownerOnly });
 	    
 	    if (logger.isLoggable(Level.FINEST)) {
 		String perms = new String(new char[] {
 			read?  'r' : '-',
 			write? 'w' : '-',
 			exec?  'x' : '-'
 		}) + (ownerOnly ? "user" : "all");
 		logger.finest("Java 1.6 chmod "+perms+" of "+file+" returns "+ret);
 	    }
 
 	    return ret;
 	} catch (InvocationTargetException e) {
 	    // throw exceptions caused by set* methods
 	    throw (SecurityException)e.getTargetException();
 	    // return false; // (would be unreachable code)
 
 	} catch (NoSuchMethodException e) {
 	} catch (IllegalAccessException e) {
 	} catch (IllegalArgumentException e) {
 	}
 	
 	try {
 	    // fallback to unix command
 	    int perm = 0;
 	    if (read)  perm |= 4;
 	    if (write) perm |= 2;
 	    if (exec)  perm |= 1;
 	    String[] chmodcmd = { "chmod", null, file.getPath() };
 	    if (ownerOnly) {
 		Object args[] = { new Integer(perm) };
 		chmodcmd[1] = String.format("0%1d00", args);
 	    } else {
 		Object args[] = {new Integer(perm),new Integer(perm),new Integer(perm)};
 		chmodcmd[1] = String.format("0%1d%1d%1d", args);
 	    }
 	    return Exec(chmodcmd) == 0;
 	} catch (Exception e2) {
 	    return false;
 	}
     }
     
     /** Create a temporary directory with read-only permissions.
      * <p>
      * TODO Current code contains a race-condition. Please see Sun
      * bug <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4735419">4735419</a>
      * for a better solution.
      */
     public static File createTempDir(String prefix, File directory) throws IOException {
 	File d = File.createTempFile(prefix, null, directory);
 	d.delete();
 	d.mkdirs();
 	chmod(d, true, true, true, true);
 	return d;
     }
     public static File createTempDir(String prefix) throws IOException {
 	return createTempDir(prefix, new File(System.getProperty("java.io.tmpdir")));
     }
     
     /** Exec invocation index, to keep track of logging lines */
     private static int globalExecIndex = 0;
     
     /** Execute a command, enter input on stdin, and return the exit code while storing stdout and stderr.
      * <p>
      * The process must stop waiting for input by itself, and not rely on its stdin
      * being closed. This doesn't work on Windows, so the process will never terminate.
      * 
      * @param cmd command to run
      * @param input String to feed to process's stdin
      * @param output String to which stdout and stderr is appended, or null
      * @return process exit code
      * 
      * @throws IOException */
     public static int Exec(String[] cmd, String input, StringBuffer output) throws IOException {
 	// get current exec invocation number for stdout/stderr tracking
 	int index = globalExecIndex++;	
 	// log
 	String scmd = "";
 	for (int i=0; i<cmd.length; i++) scmd += " "+cmd[i]; 
 	logger.finer("exec #"+index+":"+scmd);
 	// run
 	Process p = Runtime.getRuntime().exec(cmd);
 	if (input!=null) {
 	    p.getOutputStream().write(input.getBytes());
 	    p.getOutputStream().close();
 	}
 	// retrieve output
 	String s = System.getProperty("line.separator");
 	String lineout = null, lineerr = null;
 	BufferedReader stdout = new BufferedReader(
 		new InputStreamReader(p.getInputStream()));
 	BufferedReader stderr = new BufferedReader(
 		new InputStreamReader(p.getErrorStream()));
 	while ( (lineout=stdout.readLine()) != null || (lineerr=stderr.readLine()) != null) {
 	    if (lineout!=null) logger.finest("[stdout #"+index+"] "+lineout);
 	    if (lineerr!=null) logger.finest("[stderr #"+index+"] "+lineerr);
 	    if (lineout!=null && output!=null) output.append(lineout + s);
 	    if (lineerr!=null && output!=null) output.append(lineerr + s);
 	}
 	stdout.close();	
 	// log and return
 	int ret = -1;
 	try {
 	    ret = p.waitFor();
 	} catch (InterruptedException e) {
 	    // TODO verify this is the right thing to do
 	    throw new IOException(e.getMessage());
 	}
 	logger.finest("exec"+scmd+" returns "+ret);
 	return ret;
     }
     
     /** Execute a command and return the exit code while storing stdout and stderr.
      * 
      * @param cmd command to run
      * @param output String to which stdin and stdout is appended.
      * @return process exit code
      * 
      * @throws IOException
      */
     public static int Exec(String[] cmd, String output) throws IOException {
 	return Exec(cmd, null, null);
     }
     
     /** Execute a command and return the exit code 
      * @throws IOException */
     public static int Exec(String[] cmd) throws IOException {
 	return Exec(cmd, null);
     }
 }
