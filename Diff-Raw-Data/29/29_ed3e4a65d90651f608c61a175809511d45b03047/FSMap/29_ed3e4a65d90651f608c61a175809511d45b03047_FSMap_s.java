 package org.deepfs;
 
 import static org.deepfs.util.Conf.DBHOST;
 import static org.deepfs.util.Conf.DBNAME;
 import static org.deepfs.util.Conf.DBPASS;
 import static org.deepfs.util.Conf.DBPORT;
 import static org.deepfs.util.Conf.DBUSER;
 import static org.deepfs.util.Conf.VERSION;
 import jargs.gnu.CmdLineParser;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.deepfs.db.BaseXPipe;
 import org.deepfs.fsml.FSML;
 import org.deepfs.fsml.FSMLVisitor;
 
 /**
  * Display a file hierarchy in filesystem markup language (FSML).
  * 
  * @author Copyright (c) 2011, Alexander Holupirek, ISC license
  */
 public final class FSMap {
     
     private  FSMap() { }
     
     /**
      * Starts file hierarchy traversal and prints FSML to standard out.
      * 
      * @param root of file hierarchy traversal
      * @param o output stream mapping is printed to
      */
     public static void start(final Path root, final OutputStream o) {
         FSML.out = new PrintWriter(o, true);
         start(root);
     }
     
     /**
      * Starts file hierarchy traversal and prints FSML to standard out.
      *
      * @param root of file hierarchy traversal
      */
     public static void start(final Path root) {
         try {
             FSMLVisitor v = new FSMLVisitor();
            final String src = root.toAbsolutePath().toString();
             FSML.out.println("<fsml root=\"" + src + "\">");
             Files.walkFileTree(root, v);
             FSML.out.println("</fsml>");
         } catch (IOException e) {
            e.printStackTrace();
         }
     }
     
     private static void printUsage() {
         final String msg = String.format(
                 "%s\n" +
                 "\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n" +
                 "\t%s\t%s\n\n" +
                 "%s\n" +
                 "%s\n" +
                 "%s",
                 "Usage: java org.deepfs.FSMap [OPTIONS] [path]",
                 "Display a file hierarchy in Filesystem Markup Language (FSML)",
                 "-b | --basex",
                 "Send FSML output to BaseX DB Server.",
                 "--dbhost host",
                 "Connect to BaseX running on host. Default: " + DBHOST,
                 "--dbport port",
                 "Connect to BaseX running on port. Default: " + DBPORT,
                 "--dbuser user",
                 "Connect as database user. Default: " + DBUSER,
                 "--dbpass pwd",
                 "Password to authenticate database user. Default: " + DBPASS,
                 "--dbname name",
                 "Create database name from FSML output. Default: " + DBNAME,
                 "-h | --help",
                 "Print this message and exit",
                 "[path]  ",
                 "Recursively descend from path. Default: .",
                 "DeepFS project homepage: http://deepfs.org/",
                 "Report bugs: http://deepfs.org/issues",
                 "Version: " + VERSION
                 );
         System.err.println(msg);
     }
     
     /**
      * Application's main entry point.
      * 
      * If given the first command line argument is interpreted as starting point
      * of the traversal. If command line arguments are omitted the current
      * working directory is used as starting point.
      * 
      * @param args command line arguments
      */
     public static void main(final String[] args) {
         CmdLineParser p = new CmdLineParser();
         CmdLineParser.Option basexOpt = p.addBooleanOption('b', "basex");
         CmdLineParser.Option dbhostOpt = p.addStringOption("dbhost");
         CmdLineParser.Option dbportOpt = p.addIntegerOption("dbport");
         CmdLineParser.Option dbuserOpt = p.addStringOption("dbuser");
         CmdLineParser.Option dbpassOpt = p.addStringOption("dbpass");
         CmdLineParser.Option dbnameOpt = p.addStringOption("dbname");
         CmdLineParser.Option helpOpt = p.addBooleanOption('h', "help");
         
         try {
             p.parse(args);
         } catch (CmdLineParser.OptionException e) {
             System.err.println(e.getMessage());
             printUsage();
             System.exit(2);
         }
         
         boolean bpipe = (Boolean) p.getOptionValue(basexOpt, Boolean.FALSE);
         String dbhost = (String) p.getOptionValue(dbhostOpt, DBHOST);
         Integer dbport = (Integer) p.getOptionValue(dbportOpt, DBPORT);
         String dbuser = (String) p.getOptionValue(dbuserOpt, DBUSER);
         String dbpass = (String) p.getOptionValue(dbpassOpt, DBPASS);
         String dbname = (String) p.getOptionValue(dbnameOpt, DBNAME);
         boolean help = (Boolean) p.getOptionValue(helpOpt, Boolean.FALSE);
 
         if (help) {
             printUsage();
             System.exit(0);
         }
             
         String[] otherArgs = p.getRemainingArgs();        
         final String d = (otherArgs.length > 0) ? otherArgs[0] : ".";
         Path root = Paths.get(d);
         if (bpipe)
             BaseXPipe.start(root, dbhost, dbport, dbuser, dbpass, dbname);
         else
             FSMap.start(root);
     }
 }
