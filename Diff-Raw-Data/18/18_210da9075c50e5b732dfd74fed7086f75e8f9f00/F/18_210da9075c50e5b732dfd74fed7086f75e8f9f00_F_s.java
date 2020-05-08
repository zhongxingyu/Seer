 /*
 	F.java
 
 	Author: David Fogel
 
 	Copyright 2005-2009
 
 	All rights reserved. 
  */
 
 package net.logismo.text;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 
 import net.logismo.LogEvent;
 
 /**
  * F
  *
  * Comment here.  Author: David Fogel
  */
 public class F {
 	// *** Class Members ***
 	
 	public static Formatter layout(Object... items) {
 		return new LayoutF(items);
 	}
 	
 	public static Formatter level() {
 		return new LevelF();
 	}
 	
 	public static Formatter name() {
 		return new NameF();
 	}
 	
 	public static Formatter message() {
 		return new MessageF();
 	}
 	
 	public static Formatter time() {
 		return new TimeF();
 	}
 	
 	public static Formatter time(DateFormat df) {
 		return new TimeF(df);
 	}
 	
 	public static Formatter time(String pattern) {
 		return new TimeF(pattern);
 	}
 	
 	public static Formatter throwable() {
 		return new ThrowableF();
 	}
 	
 	public static Formatter threadName() {
 		return new ThreadNameF();
 	}
 
 	// *** Instance Members ***
 
 	// *** Constructors ***
 
 	// *** Interface Methods ***
 
 	// *** Public Methods ***
 
 	// *** Protected Methods ***
 
 	// *** Package Methods ***
 
 	// *** Private Methods ***
 
 	// *** Private Classes ***
 	
 	private static class LayoutF implements Formatter {
 		private Object[] theItems;
 		LayoutF(Object... items) {
 			theItems = items;
 		}
 		public void format(LogEvent e, Writer out) throws IOException {
 			for(int i = 0 ; i < theItems.length ; i++) {
 				if (theItems[i] instanceof Formatter)
 					((Formatter) theItems[i]).format(e, out);
 				else
 					out.write(theItems[i].toString());
 			}
 		}
 	}
 	
 	private static class LevelF implements Formatter {
 		public void format(LogEvent e, Writer out) throws IOException {
 			out.write(e.getLevel().getName());
 		}
 	}
 	
 	private static class NameF implements Formatter {
 		public void format(LogEvent e, Writer out) throws IOException {
 			out.write(e.getLogName());
 		}
 	}
 	
 	private static class MessageF implements Formatter {
 		public void format(LogEvent e, Writer out) throws IOException {
 			out.write(e.getMessage());
 		}
 	}
 	
 	private static class TimeF implements Formatter {
 		DateFormat theDateFormat;
 		Date theDate;
 		TimeF() {
 			theDateFormat = DateFormat.getDateTimeInstance();
 			theDate = new Date();
 		}
 		TimeF(DateFormat dateFormat) {
 			theDateFormat = dateFormat;
 			theDate = new Date();
 		}
 		TimeF(String pattern) {
 			theDateFormat = new SimpleDateFormat(pattern);
 			theDate = new Date();
 		}
 		public void format(LogEvent e, Writer out) throws IOException {
 			theDate.setTime(e.getTime());
 			out.write(theDateFormat.format(theDate));
 		}
 	}
 	
 	private static class ThrowableF implements Formatter {
 		public void format(LogEvent e, Writer out) throws IOException {
 			Throwable t = e.getThrowable();
			if (t != null) {
 				out.write('\n');
 				String m = t.getMessage();
 				if (m != null)
					out.write(t.getMessage());
 				StackTraceElement[] stl = t.getStackTrace();
 				for (int i = 0 ; i < stl.length ; i++) {
 					out.write("\n\t");
 					out.write(stl[i].toString());
 				}
 			}
 		}
 	}
 	
 	private static class ThreadNameF implements Formatter {
 		public void format(LogEvent e, Writer out) throws IOException {
 			out.write(e.getThreadName());
 		}
 	}
 }
 
 
 
 
 
 
 
 
 
 
 /* end */
