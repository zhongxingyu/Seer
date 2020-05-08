 package ai.ilikeplaces.util;
 
 import ai.ilikeplaces.doc.*;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import static ai.ilikeplaces.util.Loggers.LEVEL;
 
 /**
  * Lets try make a smart logger.
  * <p/>
  * Intention:
  * Get a start message such as "Shutting down system"
  * Wait for a confirmation such as "Done"
  * If no confirmation within a specific time(sent with the message) print "Possible failure, See logs".
  * <p/>
  * Requirements:
  * Accept a logger
  * Accept a logger severity
  * <p/>
  * if you find any bugs, please notify somebody at ilikeplaces.com
  * <p/>
  * I did this for fun when noticing how Mandriva Linux(then again all linux) was shutting down.
  * The status messages goes as "Stopping SASL.... [FAILED]"... "Stopping MySQL... [done]".
  * This got me thinking, logging is not about just notifying. It is about reporting a process.
  * Hence, this smart logger is to facilitate a start and time out, completion or delayed completion
  * of a process *
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: May 29, 2010
  * Time: 8:28:17 PM
  */
 
 
 @DOCUMENTATION(
         LOGIC = @LOGIC(
                 @NOTE(
                         {
                                 "scenario 1: no timeout. normal complete> sleep 0 , status false",
                                 "scenario 2: no timeout, 2 normal completes> sleep 0, status true",
 
                                 "scenario 3: with timeout, normal complete> sleep +ve, status false",
                                 "scenario 4: with timeout, 2 normal completes> sleep +ve, status true",
                                 "scenario 4: with timeout, recovered complete> sleep +ve, status true",
                                 "scenario 4: with timeout, 2 recovered completes> sleep +ve, status true",
 
                                 "summary 1: scenario 1 and 2 can be tracked by, sleep always 0, and if staus is true, throw an exception.",
                                 "summary 2:"
                         }
                 )
         ),
         TODO = @TODO(task = "Well there is something I noticed recently. Unless something goes wrong, logs are useful only for analytics. " +
                 "Hence, if we could implement a logger that logs an entire sequence of monitored events upon an exception, that'd be really cool! " +
                 "FOr example, a user logs in and does A, and B, and while doing C, he gets a server error/or not. " +
                 "If C threw an exception, both A and B get logged. If C didn't, nothing gets logged. " +
                 "This approach will dramatically reduce log entries. It has a memory penalty. " +
                 "But then again, this already can be done by a new complete method. Lets see. " +
                 "Hmmm.... Logging levels can be tuned to gain this feature so maybe this TODO is absurd! :D " +
                 "Yes, it is!"),
         WARNING = @WARNING("Do not user any java.lang.Objects classes. The SmartLogger should be extremely memory efficient.")
 )
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 final public class SmartLogger extends Thread {
 
     //Let's experiment with ThreadLocal :-P
 
     final private static ThreadLocal<SmartLogger> smartLoggerThreadLocal = new ThreadLocal<SmartLogger>() {
         /**
          * Returns the current thread's "initial value" for this
          * thread-local variable.  This method will be invoked the first
          * time a thread accesses the variable with the {@link #get}
          * method, unless the thread previously invoked the {@link #set}
          * method, in which case the <tt>initialValue</tt> method will not
          * be invoked for the thread.  Normally, this method is invoked at
          * most once per thread, but it may be invoked again in case of
          * subsequent invocations of {@link #remove} followed by {@link #get}.
          * <p/>
          * <p>This implementation simply returns <tt>null</tt>; if the
          * programmer desires thread-local variables to have an initial
          * value other than <tt>null</tt>, <tt>ThreadLocal</tt> must be
          * subclassed, and this method overridden.  Typically, an
          * anonymous inner class will be used.
          *
          * @return the initial value for this thread-local
          */
         @Override
         protected synchronized SmartLogger initialValue() {
            return SmartLogger.start(LEVEL.INFO, "SL STARTING FOR THREAD ID:" + Thread.currentThread().getId(), 0, "SL:" + Thread.currentThread().getId(), null);
         }
 
         /**
          * Returns the value in the current thread's copy of this
          * thread-local variable.  If the variable has no value for the
          * current thread, it is first initialized to the value returned
          * by an invocation of the {@link #initialValue} method.
          *
          * @return the current thread's value of this thread-local
          */
         @Override
         public SmartLogger get() {
             final SmartLogger smartLogger = super.get();
             if (smartLogger.hasCompleted()) {
                 remove();
             }
             return super.get();
         }
     };
 
     public static SmartLogger g() {
         return smartLoggerThreadLocal.get();
     }
 
     private static final String LOGGER_HAS_ALREADY_BEEN_COMPLETED = "Logger has already been completed!";
     private static final String LOGGER_HAS_ALREADY_BEEN_COMPLETED_UNDER_TIMEOUT_LOGGING = "Logger has already been completed under timeout logging.";
     private static final String SORRY_I_POSSIBLY_FAILED_TO_LOG = "SORRY! I POSSIBLY FAILED TO LOG:";
     final Loggers.LEVEL level;
     boolean recordedAtLeastOneError = false;
     String logmsg;
     long sleep;
     long starTime = -1;//WARNING: DO NOT change this default value as it is used in IF/TERNARY conditions.
 
     boolean logged = false;
 
     /*The following string constants might look odd but we do not want any delays in a logger. Macro optimization!!*/
     private static final String CAUSE_UNRESPONSIVE_IN_MILLIS = "{} <= cause UNRESPONSIVE in millis:";
     private static final String SORRY_I_FAILED_TO_LOG_THIS_MESSAGE = "SORRY! I FAILED TO LOG THIS MESSAGE:";
     private static final String CAUSE_RECOVERED_WITH_STATUS = " <= cause RECOVERED with status:";
     private static final String TIME_TAKEN = "[Time Taken:";
     private static final String CLOSE_SQUARE_BRACKET = "]";
     private static final String COLON = ":";
     private static final String EMPTY = "";
     private static final String PIPE = "|";
 
     /**
      * Leaving optional parameters as null will simply ignore them.
      * You can specify any number of optinal parameters as indicated by the underscore.
      * At the time of commenting, the optional value startMsg_calcExecTime means,
      * you can give a startMsg(you guessed right, a string) and a calcExecTime(boolean).
      * Leaving any null will make the program simply ignore the value.
      *
      * @param logLevel
      * @param logMessage
      * @param timeout
      * @param startMsg_calcExecTime
      */
     private SmartLogger(final LEVEL logLevel, final String logMessage, final long timeout, final Object... startMsg_calcExecTime) {
         if (startMsg_calcExecTime.length == 2) {  //Placing this IF above as we need to log time asap.
             if (startMsg_calcExecTime[1] != null) {
                 if ((Boolean) startMsg_calcExecTime[1]) {
                     starTime = System.currentTimeMillis();
                 }
             }
             if (startMsg_calcExecTime[0] != null) {
                 Loggers.log(logLevel, Loggers.EMBED, startMsg_calcExecTime[0]);
             }
         } else if (startMsg_calcExecTime.length == 1) {
             if (startMsg_calcExecTime[0] != null) {
                 Loggers.log(logLevel, Loggers.EMBED, startMsg_calcExecTime[0]);
             }
         }
 
         this.logmsg = logMessage != null ? logMessage : EMPTY;//I think this is cool, especially since null caused a bug which delayed a release by one day and...
         this.sleep = timeout;
         this.level = logLevel;
 
         this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 Loggers.log(LEVEL.ERROR, SORRY_I_POSSIBLY_FAILED_TO_LOG + logMessage, e);
             }
         });
 
         start();
         setPriority(Thread.NORM_PRIORITY);//Should we set this to Min? Hmmm.. then all method calls should be checked for asynchronosity
     }
 
     @Override
     public void run() {
         try {
             if (sleep != 0) {//Sleep 0 avoids timeout. This is when this object will wait till task "complete"
                 Thread.sleep(sleep);
                 if (!status()) {
                     Loggers.log(level, CAUSE_UNRESPONSIVE_IN_MILLIS + sleep, logmsg);
                     sleep = -1;//To track that sleep value was consumed once
                 }
             }
         } catch (final InterruptedException e) {
             Loggers.log(LEVEL.ERROR, SORRY_I_FAILED_TO_LOG_THIS_MESSAGE + logmsg, e);
         }
     }
 
     /**
      *
      * @return Updates and returns log status to be false if no logging was done before. Subsequent calls will return true;
      */
     private synchronized boolean status() {
         return !logged ? !(logged = true) : !logged;//hehe, tricky. :D
     }
 
     /**
      * Returns the time taken to execute task, or empty string, if calculations disabled
      *
      * @return
      */
     private String timeTaken() {
         return (starTime == -1) ? EMPTY : TIME_TAKEN + (System.currentTimeMillis() - starTime + CLOSE_SQUARE_BRACKET);
     }
 
     /**
      * Leaving optional parameters as null will simply ignore them.
      * You can specify any number of optinal parameters as indicated by the underscore.
      * At the time of commenting, the optional value startMsg_calcExecTime means,
      * you can give a startMsg(you guessed right, a string) and a calcExecTime(boolean).
      * Leaving any null will make the program simply ignore the value.
      *
      * @param logLevel
      * @param logMessage
      * @param timeout
      * @param startMsg_calcExecTime calcExecTime is of unit milliseconds
      * @return
      */
     static public SmartLogger start(final LEVEL logLevel, final String logMessage, final long timeout, final Object... startMsg_calcExecTime) {
         return new SmartLogger(logLevel, logMessage, timeout, startMsg_calcExecTime);
     }
 
     public void appendToLogMSG(final String stringToBeAppended) {
         logmsg += PIPE + stringToBeAppended;
     }
 
     private void appendToLogMSG(final String errorDescription, final Throwable t) {
         logmsg += PIPE + errorDescription;
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         t.printStackTrace(pw);
         pw.flush();
         sw.flush();
         sw.toString();
         logmsg += PIPE + sw;
         recordedAtLeastOneError = true;
     }
 
     /**
      * @param stringToBeAppended
      */
     public void l(final String stringToBeAppended) {
         appendToLogMSG(stringToBeAppended);
     }
 
     /**
      * @param objectWithToStringOverridden
      */
     public void l(final Object objectWithToStringOverridden) {
         appendToLogMSG(objectWithToStringOverridden.toString());
     }
 
     /**
      * @param errorDescription
      * @param throwableErrorToBeLogged
      */
     public void l(final String errorDescription, final Throwable throwableErrorToBeLogged) {
         appendToLogMSG(errorDescription, throwableErrorToBeLogged);
     }
 
     static public void complete(final SmartLogger smartLogger, final LEVEL completeLevel, final String completeStatus) {
         smartLogger.complete(completeLevel, completeStatus);
     }
 
     static public void complete(final SmartLogger smartLogger, final String completeStatus) {
         smartLogger.complete(completeStatus);
     }
 
     /**
      * @param completeLevel  Log Level
      * @param completeStatus Throwable type needed in case of ERROR
      */
     public void complete(final LEVEL completeLevel, final Object completeStatus) {
         if (sleep == 0) {
             if (!status()) {
                 Loggers.log(completeLevel, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                 if (recordedAtLeastOneError) {
                     Loggers.log(LEVEL.ERROR, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                 }
             } else { //this means with no timeout, the logger also was completed once. i.e. status=true. Hence error.
                 throw new RuntimeException(LOGGER_HAS_ALREADY_BEEN_COMPLETED);
             }
         } else if (sleep == -1) {//timeout happened //need to track if logging happens twice... or not... since this condition is triggered by us inside the new thread
             Loggers.log(completeLevel, Loggers.EMBED + CAUSE_RECOVERED_WITH_STATUS + COLON + completeStatus + timeTaken(), logmsg);
         } else {
             if (!status()) {
                 Loggers.log(completeLevel, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                 if (recordedAtLeastOneError) {
                     Loggers.log(LEVEL.ERROR, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                 }
             } else { //this means with no timeout, the logger also was completed once. i.e. status=true. Hence error.
                 throw new RuntimeException(LOGGER_HAS_ALREADY_BEEN_COMPLETED);
             }
         }
     }
 
     /**
      * @param completeLevels
      * @param completeStatus
      */
     public void multiComplete(final LEVEL[] completeLevels, Object completeStatus) {
         completeStatus = completeStatus != null ? completeStatus : "";
         if (sleep == 0) {
             if (!status()) {
                 for (final LEVEL completeLevel : completeLevels) {
                     Loggers.log(completeLevel, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                     if (recordedAtLeastOneError) {
                         Loggers.log(LEVEL.ERROR, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                     }
                 }
             } else { //this means with no timeout, the logger also was completed once. i.e. status=true. Hence error.
                 throw new RuntimeException(LOGGER_HAS_ALREADY_BEEN_COMPLETED);
             }
         } else if (sleep == -1) {//timeout happened //need to track if logging happens twice... or not... since this condition is triggered by us inside the new thread
             for (final LEVEL completeLevel : completeLevels) {
                 Loggers.log(completeLevel, Loggers.EMBED + CAUSE_RECOVERED_WITH_STATUS + COLON + completeStatus + timeTaken(), logmsg);
                 if (recordedAtLeastOneError) {
                     Loggers.log(LEVEL.ERROR, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                 }
             }
         } else {
             if (!status()) {
                 for (final LEVEL completeLevel : completeLevels) {
                     Loggers.log(completeLevel, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                     if (recordedAtLeastOneError) {
                         Loggers.log(LEVEL.ERROR, Loggers.EMBED + COLON + completeStatus + timeTaken(), logmsg);
                     }
                 }
             } else { //this means with no timeout, the logger also was completed once. i.e. status=true. Hence error.
                 throw new RuntimeException(LOGGER_HAS_ALREADY_BEEN_COMPLETED);
             }
         }
     }
 
     /**
      * @param completeStatus Complete status or null if you want to drop all further logging
      */
     public void complete(final String completeStatus) {
         complete(level, completeStatus);
     }
 
     public boolean isTimed() {
         return sleep > 0;
     }
 
     public boolean hasTimedOut() {
         return sleep == -1;
     }
 
     public boolean hasCompleted() {
         return logged;
     }
 }
