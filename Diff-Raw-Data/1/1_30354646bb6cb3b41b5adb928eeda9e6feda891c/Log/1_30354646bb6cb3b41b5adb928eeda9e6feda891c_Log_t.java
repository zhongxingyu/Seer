 package org.jyald.debuglog;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.jyald.util.StringHelper;
 
 public class Log {
 	
 	private static StringBuffer _logBuffer = new StringBuffer(6 * 1024);
 	private static PrintStream _stream;
 	private static DebugLogLevel _logLevel = new DebugLogLevel();
 	
 	public static boolean printOnDefaultSysStreamLogReplica;
 	
 	private static String initLogBuffer(String format, Object...objects) {
 		String finalLog;
 		String currTime;
 		
 		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 		Date date = new Date();
 		currTime = dateFormat.format(date);
         
 		
 		if (objects != null) 
 			finalLog = String.format("%s : %s%s", currTime, String.format(format, objects), StringHelper.NEW_LINE);
 		else
 			finalLog = String.format("%s : %s%s", currTime, format, StringHelper.NEW_LINE);
 		
 		return finalLog;
 	}
 	
 	private static void internalWrite(String format, boolean forceWriteToDisk, Object...objects) {
 		String logLine;
 		
 		if (forceWriteToDisk || (_logBuffer.length() >= 5 * 1024)) {
 			
 			try {
 				FileWriter fw = new FileWriter("jyald-deblog.txt",true);
 				fw.write(_logBuffer.toString());
 				fw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			_logBuffer.delete(0, _logBuffer.length());
 			
 		}
 		
 		if (StringHelper.isNullOrEmpty(format))
 			return;
 		
 		if (format.equals(StringHelper.NEW_LINE))
 			return;
 		
 		logLine = initLogBuffer(format,objects);
 		
 		_logBuffer.append(logLine);
 		
 		if (Log.printOnDefaultSysStreamLogReplica)
 			System.out.println(logLine);
 	}
 	
 	public synchronized static void write(String format, Object...objects) {
 		Log.internalWrite(format, false, objects);
 	}
 	
 	public synchronized static void writeByLevel(int level, String format, Object...objects) {
 		if (_logLevel.isLoggableLevel(level)) {
 			Log.internalWrite(format, false, objects);
 		}
 	}
 	
 	public static void setLogLevel(int level) {
 		_logLevel.setLevel(level);
		
 	}
 	
 	public static PrintStream getPrintStreamInstance() {
 		if (_stream == null) {
 			_stream = new PrintStream(new DebugPrintStream());
 		}
 		
 		return _stream;
 	}
 	
 	
 	
 	public static void finish() {
 		Log.write("Logging system finishing");
 		Log.internalWrite(null, true);
 		if (_stream != null)
 			_stream.close();
 	}
 }
