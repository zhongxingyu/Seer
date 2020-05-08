 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.misc.Exceptions;
 import util.misc.IO;
 import util.xml.XML;
 
 /**
  * Facility for creating log files. Log files (.log) are valid XML if a
  * /log tag is appended to the end of the file on reading.
  * <p>
  * When logging data, there is an optional category parameter. I recommend
  * that you either use this parameter for everything you log, or don't use
  * it at all.
  */
 public class Log
 {
 	/** Folder where log files are stored */
 	private File fFolder;
 
 	/** Component name for logs */
 	private String sComponent;
 
 	/** Whether to include stuff logged as debug */
 	private boolean bShowDebug;
 
 	/** Currently-open log file */
 	private PrintWriter pwCurrentLog=null;
 
 	/** True if data has been written to stream since it was last flushed */
 	private boolean bData;
 
 	/** Current date */
 	private String sCurrentDate=null;
 
 	/** True if log has been closed and can no longer be used */
 	private boolean bClosed=false;
 
 	/** List of recent errors/warnings */
 	private LinkedList<String> llRecentProblems=new LinkedList<String>();
 
 	/** Message for debugging purposes */
 	private final static String SEVERITY_DEBUG = "debug";
 	/** Message describing expected event */
 	private final static String SEVERITY_NORMAL = "normal";
 	/** Message describing a minor problem which will not prevent system operation */
 	private final static String SEVERITY_WARNING = "warning";
 	/** Message describing an unexpected or serious problem */
 	private final static String SEVERITY_ERROR = "error";
 
 	/** Date formatter used for current date in log filename */
 	public final static SimpleDateFormat DATEFORMAT=new SimpleDateFormat("yyyy-MM-dd");
 
 	/** Date formatter used for time in log */
 	public final static SimpleDateFormat TIMEFORMAT=new SimpleDateFormat("HH:mm:ss");
 
 	/** Both at once, not actually used in log but hey */
 	public final static SimpleDateFormat DATETIMEFORMAT=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 	/* For some reason the default time zone is GMT, probably best to use local */
 	static
 	{
 		DATETIMEFORMAT.setTimeZone(TimeZone.getTimeZone("Europe/London"));
 		DATEFORMAT.setTimeZone(TimeZone.getTimeZone("Europe/London"));
 		TIMEFORMAT.setTimeZone(TimeZone.getTimeZone("Europe/London"));
 	}
 
 	/** Regular expression that matches log file names */
 	private static final Pattern LOGFILEMATCHER=Pattern.compile(
 		"(.*?)\\.([0-9]{4}-[0-9]{2}-[0-9]{2})\\.log");
 
 	/** Keep 5 most recent errors */
 	private static final int RECENTPROBLEMCOUNT=5;
 
 	/** Number of recent log entries to keep track at (for display as status etc) */
 	private final static int RECENTENTRYCOUNT=10;
 
 	/** List of recent log entries */
 	private LinkedList<String> llRecentEntries=new LinkedList<String>();
 
 
 	/**
 	 * Creates a log.
 	 * @param fFolder Folder where log files are stored
 	 * @param sComponent Component name (used as first part of filename)
 	 * @param bShowDebug If false, ignores messages logged as debug
 	 * @throws IOException If it needs to create the folder and there's a problem
 	 */
 	public Log(File fFolder,String sComponent,boolean bShowDebug) throws IOException
 	{
 		this.fFolder=fFolder;
 		this.sComponent=sComponent;
 		this.bShowDebug=bShowDebug;
 
 		if(!fFolder.exists())
 		{
 			if(!fFolder.mkdirs())
 				throw new IOException("Failed to create log folder. Check that web server has permission to create folder "+fFolder);
 		}
 	}
 
 	/** Log thread */
 	private LogThread lt=null;
 
 	/**
 	 * Thread flushes log every 3 seconds (this avoids performance problems
 	 * caused if the log is flushed after every write, and we write a humungous
 	 * amount of stuff very quickly in some situation)
 	 */
 	class LogThread extends Thread
 	{
 		LogThread()
 		{
 			super("Log thread for: "+sComponent);
 			start();
 		}
 
 		@Override
 		public void run()
 		{
 			// Keep thread running until we've been idle 60 seconds
 			try
 			{
 				int iIdle=0;
 				while(iIdle < 60000)
 				{
 					try
 					{
 						Thread.sleep(3000);
 						iIdle+=3000;
 
 						synchronized(Log.this)
 						{
 							if(bClosed) return;
 
 							if(pwCurrentLog!=null && bData)
 							{
 								pwCurrentLog.flush();
 								bData=false;
 							}
 						}
 					}
 					catch (InterruptedException ie)
 					{
 					}
 				}
 			}
 			finally
 			{
 				// When thread closes, inform owner so that we know to create it again
 				synchronized(Log.this)
 				{
 					lt=null;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Closes the log. You should call this on exit if possible, but it isn't
 	 * entirely necessary if you don't mind the file not being closed for a bit.
 	 */
 	public synchronized void close()
 	{
 		if(bClosed) return;
 
 		if(pwCurrentLog!=null) pwCurrentLog.close();
 		bClosed=true;
 	}
 
 	/**
 	 * Logs an item.
 	 * @param sSeverity Severity code (use a SEVERITY_xxx constant)
 	 * @param sCategory Category (program area in which error occurred; may be null)
 	 * @param sMessage Message text (may be null)
 	 * @param tException Exception to be logged (null if none)
 	 */
 	private synchronized void log(String sSeverity,
 		String sCategory,String sMessage,Throwable tException)
 	{
 		if(bClosed) return;
 
 		if(!bShowDebug && sSeverity.equals(SEVERITY_DEBUG))
 		  return;
 
 		// Obtain current date
 		Date dNow=new Date();
 		String sDate=DATEFORMAT.format(dNow);
 
 		// If date has changed, close old file
 		if(sCurrentDate != null && !sDate.equals(sCurrentDate))
 		{
 			pwCurrentLog.close();
 			pwCurrentLog=null;
 		}
 
 		// Open new file if needed
 		if(pwCurrentLog==null)
 		{
 			try
 			{
 				// Check whether log file exists
 				File fLog=getLogFile(fFolder,sComponent,sDate);
 
 				if(fLog.exists())
 				{
 					// Append to log file
 					pwCurrentLog=new PrintWriter(new OutputStreamWriter(
 						new FileOutputStream(fLog,true),"UTF-8"));
 				}
 				else
 				{
 					// Create and start off log file
 					pwCurrentLog=new PrintWriter(new OutputStreamWriter(
 						new FileOutputStream(fLog),"UTF-8"));
 					pwCurrentLog.println(
 						"<log component='"+XML.escape(sComponent)+
 						"' date='"+sDate+"'>");
 				}
 			}
 			catch(IOException ioe)
 			{
 				throw new Error(
 					"Error creating log file. Admin staff: ensure that server has disk " +
 					"space and can write to "+	fFolder.getAbsolutePath());
 			}
 
 			sCurrentDate=sDate;
 		}
 
 		// Make up log entry text
 		StringBuffer sbEntry=new StringBuffer();
 
 		sbEntry.append(
 			"<entry time='"+TIMEFORMAT.format(dNow)+"' " +
 			"severity='"+XML.escape(sSeverity)+"'");
 		if(sCategory!=null)
 			sbEntry.append(" category='"+XML.escape(sCategory)+"'");
 		sbEntry.append(">");
 
 		if(sMessage!=null) sbEntry.append(XML.escape(sMessage));
 
 		if(tException!=null)
 		{
 			sbEntry.append("<exception>");
 
 			sbEntry.append(XML.escape(getOmExceptionString(tException))+"\n");
 
 			sbEntry.append("</exception>");
 		}
 
 		sbEntry.append("</entry>\n");
 
 		String sEntry=sbEntry.toString();
 
 		// Store in list of recent errors/warnings if appropriate
 		if(sSeverity.equals(SEVERITY_ERROR) || sSeverity.equals(SEVERITY_WARNING))
 		{
 			llRecentProblems.addFirst(sEntry);
 			if(llRecentProblems.size() > RECENTPROBLEMCOUNT)
 				llRecentProblems.removeLast();
 		}
 		// Always store in list of recent entries
 		llRecentEntries.addFirst(sEntry);
 		if(llRecentEntries.size() > RECENTENTRYCOUNT)
 			llRecentEntries.removeLast();
 
 		// Print entry to file
 		pwCurrentLog.print(sEntry);
 
 		// Mark that there's some data so we know to flush it later
 		bData=true;
 
 		// Make sure log thread is running
 		if(lt==null) lt=new LogThread();
 	}
 
 	/**
 	 * Wrapper wround Exceptions.getString().
 	 * @param tException
 	 * @return The reformatted exception trace.
 	 */
 	public static String getOmExceptionString(Throwable tException)
 	{
 		return Exceptions.getString(tException,new String[] {"om"});
 	}
 
 	/**
 	 * Logs a normal, expected event.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logNormal(String sCategory,String sMessage,Throwable tException)
 	{
 		log(SEVERITY_NORMAL,sCategory,sMessage,tException);
 	}
 	/**
 	 * Logs a normal, expected event.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 */
 	public void logNormal(String sCategory,String sMessage)
 	{
 		log(SEVERITY_NORMAL,sCategory,sMessage,null);
 	}
 	/**
 	 * Logs a normal, expected event.
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logNormal(String sMessage,Throwable tException)
 	{
 		log(SEVERITY_NORMAL,null,sMessage,tException);
 	}
 	/**
 	 * Logs a normal, expected event.
 	 * @param sMessage Message text
 	 */
 	public void logNormal(String sMessage)
 	{
 		log(SEVERITY_NORMAL,null,sMessage,null);
 	}
 
 
 	/**
 	 * Logs a minor problem which should not affect system operation.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logWarning(String sCategory,String sMessage,Throwable tException)
 	{
 		log(SEVERITY_WARNING,sCategory,sMessage,tException);
 	}
 	/**
 	 * Logs a minor problem which should not affect system operation.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 */
 	public void logWarning(String sCategory,String sMessage)
 	{
 		log(SEVERITY_WARNING,sCategory,sMessage,null);
 	}
 	/**
 	 * Logs a minor problem which should not affect system operation.
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logWarning(String sMessage,Throwable tException)
 	{
 		log(SEVERITY_WARNING,null,sMessage,tException);
 	}
 	/**
 	 * Logs a minor problem which should not affect system operation.
 	 * @param sMessage Message text
 	 */
 	public void logWarning(String sMessage)
 	{
 		log(SEVERITY_WARNING,null,sMessage,null);
 	}
 
 
 	/**
 	 * Logs an unexpected or serious problem.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logError(String sCategory,String sMessage,Throwable tException)
 	{
 		log(SEVERITY_ERROR,sCategory,sMessage,tException);
 	}
 	/**
 	 * Logs an unexpected or serious problem.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 */
 	public void logError(String sCategory,String sMessage)
 	{
 		log(SEVERITY_ERROR,sCategory,sMessage,null);
 	}
 	/**
 	 * Logs an unexpected or serious problem.
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logError(String sMessage,Throwable tException)
 	{
 		log(SEVERITY_ERROR,null,sMessage,tException);
 	}
 	/**
 	 * Logs an unexpected or serious problem.
 	 * @param sMessage Message text
 	 */
 	public void logError(String sMessage)
 	{
 		log(SEVERITY_ERROR,null,sMessage,null);
 	}
 
 
 
 	/**
 	 * Logs an event of interest for debugging only.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logDebug(String sCategory,String sMessage,Throwable tException)
 	{
 		log(SEVERITY_DEBUG,sCategory,sMessage,tException);
 	}
 	/**
 	 * Logs an event of interest for debugging only.
 	 * @param sCategory Category (program area in which event occurred)
 	 * @param sMessage Message text
 	 */
 	public void logDebug(String sCategory,String sMessage)
 	{
 		log(SEVERITY_DEBUG,sCategory,sMessage,null);
 	}
 	/**
 	 * Logs an event of interest for debugging only.
 	 * @param sMessage Message text
 	 * @param tException Related exception for stack trace
 	 */
 	public void logDebug(String sMessage,Throwable tException)
 	{
 		log(SEVERITY_DEBUG,null,sMessage,tException);
 	}
 	/**
 	 * Logs an event of interest for debugging only.
 	 * @param sMessage Message text
 	 */
 	public void logDebug(String sMessage)
 	{
 		log(SEVERITY_DEBUG,null,sMessage,null);
 	}
 
 	/**
 	 * @return XML document listing recent problems that were logged (errors/warnings)
 	 * @throws IOException
 	 */
 	public synchronized Document getRecentProblems() throws IOException
 	{
 		Document d=XML.createDocument();
 		Element e=XML.createChild(d,"recentproblems");
 
		for(Iterator i=llRecentProblems.iterator();i.hasNext();)
 		{
			String s=(String)i.next();
 			e.appendChild(d.importNode(XML.parse(s).getDocumentElement(),true));
 		}
 
 		return d;
 	}
 
 	/**
 	 * @return XML document listing recent entries that were logged (anything logged)
 	 * @throws IOException
 	 */
 	public synchronized Document getRecentEntries() throws IOException
 	{
 		Document d=XML.createDocument();
 		Element e=XML.createChild(d,"recententries");
 
		for(Iterator i=llRecentEntries.iterator();i.hasNext();)
 		{
			String s=(String)i.next();
 			e.appendChild(d.importNode(XML.parse(s).getDocumentElement(),true));
 		}
 
 		return d;
 	}
 
 	/** Logs shared between things, referenced by key */
 	private static Map<String,Log> mSharedLogs=new HashMap<String,Log>();
 
 	// Request shutdown notification
 	static
 	{
 		ShutdownManager.requestShutdownNotification(Log.class);
 	}
 
 	/** On shutdown, close all shared logs and remove references to them */
 	public static void shutdown()
 	{
 		Log[] al=mSharedLogs.values().toArray(new Log[0]);
 		for(int i=0;i<al.length;i++)
 		{
 			al[i].close();
 		}
 		mSharedLogs=null;
 	}
 
 	/**
 	 * Obtains a shared (static) log
 	 * @param sKey Key for log
 	 * @param fFolder Log folder
 	 * @param sComponent Component name
 	 * @param bShowDebug True to include logDebug stuff
 	 * @return Log file, newly-created if necessary
 	 * @throws IOException Any error creating log
 	 */
 	public static Log getShared(String sKey,File fFolder,String sComponent,boolean bShowDebug)
 	  throws IOException
 	{
 		Log l=mSharedLogs.get(sKey);
 		if(l==null)
 		{
 			l=new Log(fFolder,sComponent,bShowDebug);
 			mSharedLogs.put(sKey,l);
 		}
 		return l;
 	}
 
 	/** @return The log folder */
 	public File getLogFolder()
 	{
 		return fFolder;
 	}
 
 	/**
 	 * Lists log files.
 	 * @param fLogFolder Log folder
 	 * @param sComponent Desired component (or null for any)
 	 * @param sDate Desired date (or null for any)
 	 * @return List of available log files
 	 */
 	public static LogFile[] listLogs(File fLogFolder,String sComponent,String sDate)
 	{
 		List<LogFile> l=new LinkedList<LogFile>();
 		File[] afAllFiles=IO.listFiles(fLogFolder);
 	  for(int iFile=0;iFile<afAllFiles.length;iFile++)
 		{
 	  	Matcher m=LOGFILEMATCHER.matcher(afAllFiles[iFile].getName());
 			if(m.matches())
 			{
 				String sLogComponent=m.group(1);
 				String sLogDate=m.group(2);
 				if((sComponent==null || sLogComponent.equals(sComponent)) &&
 					(sDate==null || sLogDate.equals(sDate)))
 					l.add(new LogFile(sLogComponent,sLogDate,afAllFiles[iFile]));
 			}
 		}
 	  return l.toArray(new LogFile[0]);
 	}
 
 	/**
 	 * Obtains the File object referring to a particular log (which may or may
 	 * not exist).
 	 * @param fLogFolder Log folder
 	 * @param sComponent Component
 	 * @param sDate Date (in 2004-04-16 format)
 	 * @return Java file object
 	 */
 	public static File getLogFile(File fLogFolder,String sComponent,String sDate)
 	{
 		return new File(fLogFolder,sComponent+"."+sDate+".log");
 	}
 
 	/** Data about a single log file. Comparable in date order. */
	public static class LogFile implements Comparable
 	{
 		String sComponent;
 		String sDate;
 		File f;
 
 		private LogFile(String sComponent,String sDate,File f)
 		{
 			this.sComponent=sComponent;
 			this.sDate=sDate;
 			this.f=f;
 		}
 
 		/** @return File object for this log */
 		public File getFile()
 		{
 			return f;
 		}
 		/** @return Component name for this log */
 		public String getComponent()
 		{
 			return sComponent;
 		}
 		/** @return Date for this log in 2004-04-11 format */
 		public String getDate()
 		{
 			return sDate;
 		}
 
		public int compareTo(Object o)
 		{
			LogFile lfOther=(LogFile)o;
 			return getDate().compareTo(lfOther.getDate());
 		}
 	}
 
 	// Useful utility methods for formatting output
 	/**
 	 * Convert a string array to a string
 	 * @param ao an array of objects.
 	 * @return a string of the form [object 1][object 2].
 	 */
 	public static String formatArray(Object[] ao) {
 	    StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < ao.length; ++i) {
 		    sb.append("[" + ao[i].toString() + "]");
 		}
 		return sb.toString();
 	}
 	/**
 	 * Convert an integer array to a string
 	 * @param ai an array of integers.
 	 * @return a string of the form [int 1][int 2].
 	 */
 	public static String formatArray(int[] ai) {
 	    StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < ai.length; ++i) {
 		    sb.append("[" + ai[i] + "]");
 		}
 		return sb.toString();
 	}
 	/**
 	 * Convert a two-dimensional integer array to a string
 	 * @param aai an array of array of integers.
 	 * @return A string of the form {[1][2]}{[3][4]}.
 	 */
 	public static String formatArray(int[][] aai) {
 	    StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < aai.length; ++i) {
 		    sb.append("{" + formatArray(aai[i]) + "}");
 		}
 		return sb.toString();
 	}
 }
