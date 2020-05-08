 package org.slf4j.impl;
 
 import org.slf4j.helpers.MarkerIgnoringBase;
 import org.slf4j.helpers.MessageFormatter;
 
 import android.util.Log;
 
 public class AndroidLogger extends MarkerIgnoringBase {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Log tag for SLF4J itself
      **/
     private static final String SLF4J_TAG = "slf4j";
 
     /**
      * The tag this logger will log with
      */
     private final String tag;
 
     /**
      * Package access allows only {@link AndroidLoggerFactory} to instantiate
      * AndroidLogger instances.
 	 *
      * @return
      */
     AndroidLogger(String tag) {
 		// Android only supports tags of length <= 23
 		if (tag.length() > 23) {
 			// We try to do something smart here to shorten
 			StringBuffer shortTag = new StringBuffer();
 			String[] parts = tag.split("\\.");
 			String lastPart = parts[parts.length - 1];
 			if (lastPart.length() < 23) {
				if ( ( (parts.length-2) * 2 ) + lastPart.length() < 23 ) {
 					for (int x=0; x<parts.length - 1; x++) {
 						shortTag.append(parts[x].charAt(0));
 						shortTag.append('.');
 					}
 				}
 				shortTag.append(lastPart);
 			} else {
 				shortTag.append(lastPart.substring(0,10));
 				shortTag.append("...");
 				shortTag.append(lastPart.substring(lastPart.length() - 10));
 			}
 			this.tag = shortTag.toString();
 			if( Log.isLoggable(SLF4J_TAG, Log.DEBUG) ) {
 				Log.d(SLF4J_TAG, "Tag: " + tag + " shortened to: " + this.tag);
 			}
 		} else {
 			this.tag = tag;
 		}
     }
 
     public void debug(String arg0) {
         Log.d(tag, arg0);
     }
 
     public void debug(String arg0, Object arg1) {
         Log.d(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void debug(String arg0, Object[] arg1) {
         Log.d(tag, MessageFormatter.arrayFormat(arg0, arg1));
     }
 
     public void debug(String arg0, Throwable arg1) {
         Log.d(tag, arg0, arg1);
     }
 
     public void debug(String arg0, Object arg1, Object arg2) {
         Log.d(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
     public void error(String arg0) {
         Log.e(tag, arg0);
     }
 
     public void error(String arg0, Object arg1) {
         Log.e(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void error(String arg0, Object[] arg1) {
         Log.e(tag, MessageFormatter.arrayFormat(arg0, arg1));
     }
 
     public void error(String arg0, Throwable arg1) {
         Log.e(tag, arg0, arg1);
     }
 
     public void error(String arg0, Object arg1, Object arg2) {
         Log.e(tag, MessageFormatter.format(arg0, arg1, arg1));
     }
 
     public void info(String arg0) {
         Log.i(tag, arg0);
     }
 
     public void info(String arg0, Object arg1) {
         Log.i(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void info(String arg0, Object[] arg1) {
         Log.i(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void info(String arg0, Throwable arg1) {
         Log.i(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void info(String arg0, Object arg1, Object arg2) {
         Log.i(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
     public boolean isDebugEnabled() {
         return Log.isLoggable(tag, Log.DEBUG);
     }
 
     public boolean isErrorEnabled() {
         return Log.isLoggable(tag, Log.ERROR);
     }
 
     public boolean isInfoEnabled() {
         return Log.isLoggable(tag, Log.INFO);
     }
 
     public boolean isTraceEnabled() {
         return Log.isLoggable(tag, Log.VERBOSE);
     }
 
     public boolean isWarnEnabled() {
         return Log.isLoggable(tag, Log.WARN);
     }
 
     public void trace(String arg0) {
         Log.v(tag, arg0);
     }
 
     public void trace(String arg0, Object arg1) {
         Log.v(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void trace(String arg0, Object[] arg1) {
         Log.v(tag, MessageFormatter.arrayFormat(arg0, arg1));
     }
 
     public void trace(String arg0, Throwable arg1) {
         Log.v(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void trace(String arg0, Object arg1, Object arg2) {
         Log.v(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
     public void warn(String arg0) {
         Log.w(tag, arg0);
     }
 
     public void warn(String arg0, Object arg1) {
         Log.w(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void warn(String arg0, Object[] arg1) {
         Log.w(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void warn(String arg0, Throwable arg1) {
         Log.w(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void warn(String arg0, Object arg1, Object arg2) {
         Log.w(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
 }
