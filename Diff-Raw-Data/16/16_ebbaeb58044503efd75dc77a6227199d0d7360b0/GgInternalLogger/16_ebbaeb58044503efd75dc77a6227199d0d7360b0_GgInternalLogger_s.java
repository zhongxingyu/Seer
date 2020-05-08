 /**
    This file is part of GoldenGate Project (named also GoldenGate or GG).
 
    Copyright 2009, Frederic Bregier, and individual contributors by the @author
    tags. See the COPYRIGHT.txt in the distribution for a full listing of
    individual contributors.
 
    All GoldenGate Project is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    GoldenGate is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
  */
 package goldengate.common.logging;
 
 import org.jboss.netty.logging.InternalLogLevel;
 import org.jboss.netty.logging.InternalLogger;
 
 /**
  * Logger inspired from Netty implementation, adding some extra commands that
  * allow to limit the overhead of some ignored logger calls (toString or string
  * construction is called only if necessary).
  *
  * Based on The Netty Project (netty-dev@lists.jboss.org)
  *
  * @author Trustin Lee (tlee@redhat.com)
  *
  * @author Frederic Bregier
  *
  */
 public abstract class GgInternalLogger implements InternalLogger {
    private static int BASELEVEL = 2;
     /**
      * Determine the good level
      * @return the default base level
      */
     private static int detectLoggingBaseLevel() {
         StackTraceElement []elt = Thread.currentThread().getStackTrace();
         int i = 0;
         for (i = 0; i < elt.length ; i++) {
             if (elt[i].getMethodName().equalsIgnoreCase("detectLoggingBaseLevel")) {
                 break;
             }
         }
         return i;
     }
     {
         BASELEVEL = detectLoggingBaseLevel();
     }
     /**
      * To be used in message for logger (rank 2) like
      * logger.warn(code,"message:"+getImmediateMethodAndLine(),null);
      *
      * @return "ClassAndMethodName(FileName:LineNumber)"
      */
     public static String getImmediateMethodAndLine() {
         StackTraceElement elt = Thread.currentThread().getStackTrace()[BASELEVEL+1];
         return getMethodAndLine(elt);
     }
 //FIXME TODO for JDK6 IBM add 1 (2->3 and 3->4)
     /**
      * To be used only by Logger (rank 5)
      *
      * @return "MethodName(FileName:LineNumber)"
      */
     protected static String getLoggerMethodAndLine() {
         StackTraceElement elt = Thread.currentThread().getStackTrace()[BASELEVEL+2];
         return getMethodAndLine(elt);
     }
 
     /**
      * @param rank
      *            is the current depth of call+1 (immediate = 1+1=2)
      * @return "ClassAndMethodName(FileName:LineNumber)"
      */
     public static String getRankMethodAndLine(int rank) {
         StackTraceElement elt = Thread.currentThread().getStackTrace()[rank];
         return getMethodAndLine(elt);
     }
     /**
      *
      * @param elt
      * @return "MethodName(FileName:LineNumber) " from elt
      */
     private static String getMethodAndLine(StackTraceElement elt) {
         StringBuilder builder = new StringBuilder(elt.getClassName());
         builder.append('.');
         builder.append(elt.getMethodName());
         builder.append('(');
         builder.append(elt.getFileName());
         builder.append(':');
         builder.append(elt.getLineNumber());
         builder.append(") : ");
         return builder.toString();
     }
     /**
      * @param level
      * @return True if the level is enabled
      */
     public boolean isEnabled(InternalLogLevel level) {
         switch (level) {
             case DEBUG:
                 return isDebugEnabled();
             case INFO:
                 return isInfoEnabled();
             case WARN:
                 return isWarnEnabled();
             case ERROR:
                 return isErrorEnabled();
             default:
                 throw new Error();
         }
     }
 
     public void log(InternalLogLevel level, String msg, Throwable cause) {
         switch (level) {
             case DEBUG:
                 debug(msg, cause);
                 break;
             case INFO:
                 info(msg, cause);
                 break;
             case WARN:
                 warn(msg, cause);
                 break;
             case ERROR:
                 error(msg, cause);
                 break;
             default:
                 throw new Error();
         }
     }
 
     public void log(InternalLogLevel level, String msg) {
         switch (level) {
             case DEBUG:
                 debug(msg);
                 break;
             case INFO:
                 info(msg);
                 break;
             case WARN:
                 warn(msg);
                 break;
             case ERROR:
                 error(msg);
                 break;
             default:
                 throw new Error();
         }
     }
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void debug(String format, String arg1);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void info(String format, String arg1);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void warn(String format, String arg1);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void error(String format, String arg1);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void debug(String format, String arg1, String arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void info(String format, String arg1, String arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void warn(String format, String arg1, String arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void error(String format, String arg1, String arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void debug(String format, Object arg1, Object arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void info(String format, Object arg1, Object arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void warn(String format, Object arg1, Object arg2);
 
     /**
      *
      * @param format
      * @param arg1
      * @param arg2
      */
     public abstract void error(String format, Object arg1, Object arg2);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void debug(String format, Object arg1);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void info(String format, Object arg1);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void warn(String format, Object arg1);
 
     /**
      *
      * @param format
      * @param arg1
      */
     public abstract void error(String format, Object arg1);
 }
