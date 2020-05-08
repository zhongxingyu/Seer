 package sw3.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The server class is a complete server solution,
  * that loads up according to a configuration file and
  * runs indefinitely until closed. The server can run
  * in daemon (unattended) or active (console-based) form.
  * In active form a console is made available through which
  * to pass commands to the server itself.
  * In daemon form this single functionality is disabled,
  * although the logging output is still available.
  * @see
  * @author Johannes Lindhart Borresen
  */
 public class ServerThread extends Thread
 {
     public Properties config = new Properties();
     
     public String configPath = "./config.ini";
     
     public Logger log;
 
     public ServerSocket serverSocket;
     
     public Scanner input;
     
     private ServerModeEnum _mode;
     
     protected CommandOpEnum loopSignal;
     
     /**
      * Get the current running mode of the server.
      * Can be "daemon" for non-interactive mode or
      * "active" for an active console-based mode.
      * @return The current running mode.
      */
     public ServerModeEnum getMode()
     {
         return _mode;
     }
     
     public ServerThread()
     {
         // Important. Start by creating a logger we can see.
         _createLogger();
         // THEN get any pre-execution information from the on-file config.
         loadConfig();
         
         // And theeeen, do everything else.
         
         // Create the server socket.
         _createServerSocket();
         // Activate the input scanner if 
         if (config.getProperty("mode").compareToIgnoreCase("active") == 0)_createScanner();
         
         log.info("LOG: Server has started up. Be happy!");
         
         // At this point we await the start command from our calling environment,
         // at which point run is called and we start out server loop.
     }
     
     @Override
     public void run()
     {
         // We loop indefinitely until the loopSignal field has been set
         // to TERM. For now, only NOOP and TERM are supported.
         while (loopSignal != CommandOpEnum.TERM)
         {
             // We only take user input if we're running active mode.
             if (getMode() == ServerModeEnum.ACTIVE) runScanner();
         }
         // If we break loop, we do a clean shutdown.
         shutdown();
     }
     
     /**
     * Runs the scanne section of a loop. The scanner reads
      * console input and reacts to it, if applicable.
      * May change loopSignal if applicable.
      */
     protected void runScanner()
     {
        
         // If the scanner has registered some user input, we start reacting to it.
         if (input.hasNext())
         {
             String cmd;
             // Currently, that entails printing it like an idiot.
             cmd = input.next();
             log.info("Command: " + cmd);
             
             // If the command is quit, however, we return the term signal.
             if (cmd.compareToIgnoreCase("quit") == 0) loopSignal = CommandOpEnum.TERM;
         }
     }
     
     public void shutdown()
     {
         try
         {
             serverSocket.close();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             log.warning("The socket could not be closed:\n" + e.getMessage());
         }
     }
     
     private void _createScanner()
     {
         input = new Scanner(System.in);
     }
 
     private void _createServerSocket()
     {
         try
         {
             serverSocket = new ServerSocket(Integer.parseInt(config.getProperty("port")));
         }
         catch (NumberFormatException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     public void loadConfig()
     {
         if (!configExists())
         {
             log.info("Creating new config file.");
             createConfig();
         }
         
         try
         {
             config.load(new FileInputStream(getConfigFile()));
             log.info("Configuration file loaded successfully from path:\n" + getConfigFile().getAbsolutePath());
         }
         catch (FileNotFoundException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     /**
      * Retrieves a File object with a path leading to the
      * config file proper - calling this method ensures
      * existance of the file.
      * @return Handler to the config file.
      */
     public File getConfigFile()
     {
         if(!configExists()) createConfig();
         
         return new File(configPath);
     }
     
     /**
      * Creates a new configuration file with default values.
      * @return File handler to the new config file.
      */
     protected void createConfig()
     {
         File f = new File(configPath);
         
         if (f.exists())
         {
             f.delete();
         }
         
         try // We should be so solid that trying is simply a formality...
         {
             f.createNewFile();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         Properties p = new Properties();
         
         p.setProperty("port", "16489");
         p.setProperty("mode", "active");
         p.setProperty("dbhost", "localhost");
         p.setProperty("dbuser", "giraf");
         p.setProperty("dbpass", "secret");
         
         try
         {
             p.store(new FileOutputStream(f), "Default values ensue!");
             log.info("A new config file was written to the path '" + f.getAbsolutePath() + "'.");
         }
         catch (FileNotFoundException e)
         {
             log.severe("The config file could not be found for writing. This should NOT happen!!!");
             e.printStackTrace();
         }
         catch (IOException e)
         {
             log.severe("An error occurred while saving the config file");
             log.severe(e.getMessage());
         }
     }
     
     /**
      * Checks to see if a config file exists, and responds.
      * @return True if it exists, false otherwise.
      */
     protected boolean configExists()
     {
         File f = new File(configPath);
         
         if (f.exists())
         {
             log.finest("Config file already exists.");
             return true;
         }
         else
         {
             log.finest("Config file does not exist.");
             return false;
         }
     }
     
     protected void _createLogger()
     {
         log = Logger.getLogger("sw3server");
         log.setLevel(Level.ALL);
         log.setUseParentHandlers(false);
         
         Handler[] handlers = log.getHandlers();
         if (handlers.length > 2)
         {
             log.warning("The number of active handlers exceeds 1.");
         }
         for (Handler handler : handlers)
         {
             log.removeHandler(handler);
         }
         
         ConsoleHandler myHandler = new ConsoleHandler();
         myHandler.setFormatter(new LoggingFormatter());
         log.addHandler(myHandler);
     }
 
     public void logFinest(String msg)
     {
         log.finest(msg);
     }
 }
