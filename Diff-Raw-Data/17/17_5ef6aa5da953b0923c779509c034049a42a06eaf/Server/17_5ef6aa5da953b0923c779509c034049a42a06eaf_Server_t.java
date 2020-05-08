 package cryptocast.server.programs;
 
 import cryptocast.server.*;
 import cryptocast.util.FileUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 
 import ch.qos.logback.classic.Level;
 
 import com.beust.jcommander.Parameter;
 
 /**
  * The main method to start the server.
  */
 public final class Server {    
     static class Options extends OptParse.WithHelp {
         @Parameter(names = { "-l", "--logfile" }, description = "Log output file")
         private File logFile = FileUtils.expandPath("~/.cryptocast/cryptocast.log");
 
         @Parameter(names = { "-f", "--database" }, description = "User database file")
         private File databaseFile = FileUtils.expandPath("~/.cryptocast/users.db");
 
         @Parameter(names = { "-p", "--port" }, description = "Listen port")
         private int listenPort = 21337;
 
         @Parameter(names = { "-b", "--listen-address"}, description = "Listen address")
         private String listenAddr = "0.0.0.0";
         
         @Parameter(names = { "-i", "--key-broadcast-interval" }, description = "Key broadcast interval in seconds")
         private int keyBroadcastIntervalSecs = 15;
         
         @Parameter(names = { "-v", "--verbosity" }, 
                    description = "Control the level of debug output on STDERR (trace, debug, info, warn, error)")
         private String verbosity = "info";
     }
     
     /**
      * @param args command line arguments
      */
     public static void main(String[] argv) throws Exception {
         Options opts = OptParse.parseArgs(new Options(), "server", argv);
         Level logLevel = Level.toLevel(opts.verbosity);
         
         System.out.printf("Logging to STDERR (level %s) and %s\n", logLevel, opts.logFile);
         LogbackUtils.removeAllAppenders();
         LogbackUtils.addFileAppender(opts.logFile, Level.ALL);
         LogbackUtils.addStderrLogger(logLevel);
         
         Controller control = Controller.start(
                 opts.databaseFile,
                 new InetSocketAddress(opts.listenAddr, opts.listenPort),
                 opts.keyBroadcastIntervalSecs);
         new Shell(new BufferedReader(new InputStreamReader(System.in)), 
                   System.out, System.err, control).run();
     }
 }
