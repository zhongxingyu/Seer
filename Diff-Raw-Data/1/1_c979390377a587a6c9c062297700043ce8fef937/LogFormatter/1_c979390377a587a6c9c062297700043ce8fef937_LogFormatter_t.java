 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 28th December 2009
  */
 package au.edu.uts.eng.remotelabs.schedserver.logger.impl;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Formats the message to their specifying format. The following is a list of 
  * macros that can be substituted into a log format string. 
  * <ul>
  *  <li><code>__MESSAGE__</code> - Log message.</li>
  *  <li><code>__LEVEL__</code> - Log level.</li>
  *  <li><code>__TIME__</code> - Time with hour of day, minute and second.</li>
  *  <li><code>__DATE__</code> - Date with day of month, month and year.</li>
  *  <li><code>__ISO8601__</code> - ISO8601 formatted date / time.</li>
  *  <li><code>__CLASS__</code> - Class name whence message originated.</li>
  *  <li><code>__METHOD__</code> - Method whence message originated.</li>
  *  <li><code>__SOURCE__</code> - Source file whence message originated.</li>
  *  <li><code>__LINE_NUM__</code> - Line number of source file whence message 
  *  originated.</li>
  *  <li><code>__TID__</code> - Thread id of whence message originated.</li>
  * </ul>
  * NOTE: All macros are prefixed and suffixed with two under-scores (i.e.
  * <tt>__</tt>).
  */
 public class LogFormatter
 {
     /**
      * Substitution macros.
      */
     enum Macro 
     {
         /** Log message. */
         MESSAGE, /** Log level. */
         LEVEL, /** Time. */
         TIME, /** Date. */
         DATE, /** ISO8601 date time. */
         ISO8601, /** Class name. */
         CLASS, /** Method name. */
         METHOD, /** Source file. */
         SOURCE, /** Source line number. */
         LINE_NUM, /** Thread id. */
         TID
     }
 
     /** List of macros. */
     private final Map<String, Macro> macros;
     
     /** The stack position . */
     private static int stackPos = -1;
     
     /**
      * Constructor.
      */
     public LogFormatter()
     {
         this.macros = new HashMap<String, Macro>();
         this.macros.put("MESSAGE", Macro.MESSAGE);
         this.macros.put("LEVEL", Macro.LEVEL);
         this.macros.put("TIME", Macro.TIME);
         this.macros.put("DATE", Macro.DATE);
         this.macros.put("ISO8601", Macro.ISO8601);
         this.macros.put("CLASS", Macro.CLASS);
         this.macros.put("METHOD", Macro.METHOD);
         this.macros.put("SOURCE", Macro.SOURCE);
         this.macros.put("LINE_NUM", Macro.LINE_NUM);
         this.macros.put("TID", Macro.TID);
     }
     
     /**
      * Format the log message using the provided format string.
      * 
      * @param fmt format string
      * @param msg log message
      * @param lvl log level
      * @return formatted log message
      */
     public String formatLog(final String fmt, final String msg, final String lvl)
     {
         final Calendar cal = Calendar.getInstance();
         StackTraceElement frame = null;
         
         if (fmt == null) return null;
         
         final String elements[] = fmt.split("__");
         final StringBuffer buf = new StringBuffer(fmt.length());
 
         for (String e : elements)
         {
             if (this.macros.containsKey(e))
             {
                 switch (this.macros.get(e))
                 {
                     case MESSAGE:
                         buf.append(msg);
                         break;
                     case LEVEL:
                         buf.append(lvl);
                         break;
                         
                     case DATE:
                         buf.append(this.addLeadingZeros(cal.get(Calendar.DAY_OF_MONTH)));
                         buf.append('-');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.MONTH) + 1));
                         buf.append('-');
                         buf.append(cal.get(Calendar.YEAR));
                         break;
                     case TIME:
                         buf.append(this.addLeadingZeros(cal.get(Calendar.HOUR_OF_DAY)));
                         buf.append(':');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.MINUTE)));
                         buf.append(':');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.SECOND)));
                         break;
                     case ISO8601:
                         /* Format of ISO 8061 date and time can be found at 
                          * http://www.iso.org/iso/date_and_time_format#what-iso-8601-covers. */
                         buf.append(cal.get(Calendar.YEAR));
                         buf.append('-');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.MONTH) + 1));
                         buf.append('-');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.DAY_OF_MONTH)));
                         buf.append('T');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.HOUR_OF_DAY)));
                         buf.append(':');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.MINUTE)));
                         buf.append(':');
                         buf.append(this.addLeadingZeros(cal.get(Calendar.SECOND)));
                         break;
                         
                     case CLASS:
                         if ((frame = LogFormatter.getStackTrace(frame)) != null)
                         {
                             buf.append(frame.getClassName());
                         }
                         break;
                     case METHOD:
                         if ((frame = LogFormatter.getStackTrace(frame)) != null)
                         {
                             buf.append(frame.getMethodName());
                         }
                         break;
                     case SOURCE:
                         if ((frame = LogFormatter.getStackTrace(frame)) != null)
                         {
                             buf.append(frame.getFileName());
                         }
                         break;
                     case LINE_NUM:
                         if ((frame = LogFormatter.getStackTrace(frame)) != null)
                         {
                             buf.append(frame.getLineNumber());
                         }
                         break;
                     case TID:
                         buf.append(Thread.currentThread().getId());
                         break;
                 }
             }
             else
             {
                 buf.append(e);
             }
         }
         
         return buf.toString();
     }
 
     /**
      * Returns a string containing a two digit representation of the provided
      * string. If the <code>val</code> contains a single digit, a '0' 
      * character is prepended to the string.
      * 
      * @param val value to add leading zero if less then two digits long
      * @return two digit representation of the provided value 
      */
     protected String addLeadingZeros(final int val)
     {
         String str = String.valueOf(val);
         if (str.length() == 1)
         {
             str = "0" + str;
         }
             
         return str;
     }
     
     private static StackTraceElement getStackTrace(StackTraceElement frame)
     {
         if (frame != null) return frame;
         
         StackTraceElement[] thrStack = Thread.currentThread().getStackTrace();
         if (LogFormatter.stackPos < 0)
         {
             synchronized (LogFormatter.class)
             {
                 /* As the stack level is inconsistent accross platforms and deployment mechanisms,
                  * we parse the StackTrace to set the pointer to the appropriate stack frame in order  
                  * to get the details of the caller method (filename, line number etc.)
                  * The stack is parsed from position 0 (i.e. here) till the first stack frame that is 
                  * after a logger. */
                 
                 String fileName;
                 int newStackPos = 0;
                 boolean foundLogger = false;
                 
                 while (thrStack.length > newStackPos)
                 {
                     fileName = thrStack[newStackPos].getFileName();
                     if (fileName == null || fileName.endsWith("Logger.java") )
                     {
                         foundLogger = true;
                         newStackPos++;
                     }
                     else if (!foundLogger)
                     {
                         newStackPos++;
                     }
                     else
                     {
                         LogFormatter.stackPos = newStackPos;
                         break;
                     }
                 }
             }
         }
         
         return LogFormatter.stackPos < thrStack.length && LogFormatter.stackPos >= 0 ?
                 thrStack[LogFormatter.stackPos] : null;
     }
 }
