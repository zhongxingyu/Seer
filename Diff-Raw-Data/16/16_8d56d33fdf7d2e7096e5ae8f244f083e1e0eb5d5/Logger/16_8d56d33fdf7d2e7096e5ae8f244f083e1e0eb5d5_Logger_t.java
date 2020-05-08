 package edu.usu.cosl.util;
 
 import java.util.Date;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import java.text.SimpleDateFormat;
 
 import java.io.FileWriter;
 import java.io.PrintStream;
 import java.io.OutputStream;
 import java.sql.SQLException;
 
 public class Logger extends OutputStream
 {
 	public static final int NEVER		= 0;
 	public static final int CRITICAL	= 1;
 	public static final int EXCEPTION	= 2;
 	public static final int STATUS		= 3;
 	public static final int WARNING		= 4;
 	public static final int INFO		= 5;
 	public static final int ALL			= 10;
 
 	private static boolean bLogToConsole = true;
 	private static int nConsoleLogLevel = EXCEPTION;
 
 	private static boolean bLogToString = false;
 	private static StringBuffer sMsgs = new StringBuffer();
 	private static int nStringLogLevel = STATUS;
 
 	private static boolean bLogToFile = false;
 	private static int nFileLogLevel = INFO;
 	private static String sLogFilePrefix = "log/";
 	private static FileLogger fileLogger;
 	private static ConcurrentLinkedQueue<String> messageQueue;
 
 	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a");
 	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
 	
 	private static Logger logger;
 	private static PrintStream out;
 	private static PrintStream outConsole = System.out;
 	private static PrintStream errConsole = System.err;
 	
 
 	public void write(int c)
 	{
 		outConsole.write(c);
 	}
 	public static void log(int nLevel, String sMessage)
 	{
 		sMessage = timeFormatter.format(new Date()) + " " + sMessage;
 		if (bLogToConsole && nLevel <= nConsoleLogLevel) System.out.println(sMessage);
 		if (bLogToFile && nLevel <= nFileLogLevel && messageQueue != null) { 
 			messageQueue.add(sMessage);
 		}
 		if (bLogToString && nLevel <= nStringLogLevel) sMsgs.append(sMessage + "\n");
 	}
 	
 	public static void fatal(String sMessage){log(CRITICAL,sMessage);}
 	public static void error(String sMessage){log(EXCEPTION,sMessage);}
 	public static void error(Exception e){log(e);}
 	public static void error(Throwable t){log(t);}
 	public static void warn(String sMessage){log(WARNING,sMessage);}
 	public static void status(String sMessage){log(STATUS,sMessage);}
 	public static void info(String sMessage){log(INFO,sMessage);}
 
 	public static String getMessages()
 	{
 		return sMsgs.toString();
 	}
 	
 	public static void setConsoleLogLevel(int nLevel)
 	{
 		nConsoleLogLevel = nLevel;
 		bLogToConsole = true;
 	}
 	
 	public static void setFileLogLevel(int nLevel)
 	{
 		nFileLogLevel = nLevel;
 	}
 	
 	public static void setStringLogLevel(int nLevel)
 	{
 		nStringLogLevel = nLevel;
 		bLogToString = true;
 	}
 	
	public static void stopLogging()
	{
		setLogFilePrefix(null);
	}
	
 	public static void setLogFilePrefix(String sPrefix)
 	{
 		if (sPrefix != null)
 		{
 			bLogToFile = true;
 			sLogFilePrefix = sPrefix;
 			if (messageQueue == null)
 				messageQueue = new ConcurrentLinkedQueue<String>();
 			if (fileLogger == null)
 			{
 				getLogger();
 				fileLogger = logger.new FileLogger();
 				fileLogger.start();
 			}
 		}
 		else if (fileLogger != null)
 		{
 			bLogToFile = false;
 			fileLogger.stopRunning();
 			fileLogger = null;
 		}
 	}
 
 	public static void setLogToConsole(boolean bLog)
 	{
 		bLogToConsole = bLog;
 		System.setOut(bLog ? outConsole : out);
 		System.setErr(bLog ? errConsole : out);
 	}
 
 	public static void setLogToString(boolean bLog)
 	{
 		bLogToString = bLog;
 	}
 
 	public static void log(Exception e)
 	{
 		if (e != null) log(EXCEPTION,e.toString());
 		if (e instanceof SQLException) log(EXCEPTION, ((SQLException)e).getNextException().toString());
 	}
 
 	public static void error(String sMsg, Exception e)
 	{
 		if (e != null) log(EXCEPTION,sMsg + "\n" + e.toString());
 		if (e instanceof SQLException) log(EXCEPTION, ((SQLException)e).getNextException().toString());
 	}
 	
 	public static void log(Throwable t)
 	{
 		if (t != null) log(EXCEPTION,t.toString());
 	}
 	
 	public static Logger getLogger()
 	{
 		if (logger == null) logger = new Logger();
 		return logger;
 	}
 	
 	public class FileLogger extends Thread
 	{
 		private boolean bRun = true;
 
 		private void flushMessages() {
 			StringBuffer sbMessage = new StringBuffer();
 			while(!messageQueue.isEmpty()) {
 				sbMessage.append(messageQueue.poll() + "\r\n");
 			}
 			if (sbMessage.length() > 0)
 				logMessageToFile(sbMessage.toString());
 		}
		
 		public void run()
 		{
 			try 
 			{
 				while(bRun)
 				{
					if (!messageQueue.isEmpty()) {
 						flushMessages();
 					}
 					else {
 						Thread.sleep((long)(1000));
 					}
 				}
 			}
 			catch (InterruptedException e)
 			{
 				System.out.println(e);
 			}
 		}
		
		
 		public void logMessageToFile(String sMessage)
 		{
 			try
 			{
 				FileWriter writer = new FileWriter(sLogFilePrefix + "_" + dateFormatter.format(new Date()) + ".log", true);
				writer.write(sMessage);
 				writer.close();
 			}
 			catch (Exception e)
 			{
 				System.out.println(e);
 			}
 		}
 		public void stopRunning()
 		{
 			bRun = false;
 			flushMessages();
 		}
 	}
 	
 	public static void getOptions(Properties properties)
 	{
 		getLogger();
 		
         // log level
         String sValue = properties.getProperty("debug_level");
         int nDebugLevel = EXCEPTION;
         if (sValue != null) nDebugLevel = Integer.parseInt(sValue);
         if (System.getProperty("DEBUG") != null) nDebugLevel = ALL; 
         if (System.getProperty("WARN") != null) nDebugLevel = WARNING; 
         if (System.getProperty("INFO") != null) nDebugLevel = INFO; 
         Logger.setConsoleLogLevel(nDebugLevel);
 
         // log to console
         sValue = properties.getProperty("log_to_console");
         if ("true".equals(sValue) || System.getProperty("LOG_TO_CONSOLE") != null) Logger.setLogToConsole(true);
 
         // log to file
         sValue = properties.getProperty("log_file_prefix");
         sValue = System.getProperty("LOG_FILE_PREFIX", sValue);
         if (sValue != null) Logger.setLogFilePrefix(sValue);
 	}
 }
