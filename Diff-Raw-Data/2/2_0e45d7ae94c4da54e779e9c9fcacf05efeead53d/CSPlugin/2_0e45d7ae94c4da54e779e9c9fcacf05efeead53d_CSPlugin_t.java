 package com.censoredsoftware.demigods.helper;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 
 public abstract class CSPlugin extends JavaPlugin
 {
 	public QuitReason getLatestQuitReason()
 	{
 		if(QuitReasonHandler.latestQuit == null) getServer().getLogger().addHandler(new QuitReasonHandler());
 		return QuitReasonHandler.latestQuit;
 	}
 
 	public static class QuitReasonHandler extends Handler
 	{
		protected static QuitReason latestQuit;
 
 		@Override
 		public void publish(LogRecord record)
 		{
 			if(!record.getMessage().toLowerCase().contains("disconnect")) return;
 			latestQuit = QuitReason.QUITTING;
 			if(record.getMessage().toLowerCase().contains("genericreason")) latestQuit = QuitReason.GENERIC_REASON;
 			else if(record.getMessage().toLowerCase().contains("spam")) latestQuit = QuitReason.SPAM;
 			else if(record.getMessage().toLowerCase().contains("endofstream")) latestQuit = QuitReason.END_OF_STREAM;
 			else if(record.getMessage().toLowerCase().contains("overflow")) latestQuit = QuitReason.OVERFLOW;
 			else if(record.getMessage().toLowerCase().contains("timeout")) latestQuit = QuitReason.TIMEOUT;
 		}
 
 		@Override
 		public void flush()
 		{}
 
 		@Override
 		public void close() throws SecurityException
 		{}
 	}
 }
