 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.gtri.util.iteratee.api;
 
 import java.util.Date;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import org.gtri.util.iteratee.api.Issue.ImpactCode;
 
 /**
  *
  * @author lance.gatlin@gmail.com
  */
 public class Issues {
 
  public static ConfigIssue configFatalError(String message) {
     return new ConfigIssue(message, ImpactCode.FATAL);
   }
 
  public static ConfigIssue configRecoverableError(String message) {
     return new ConfigIssue(message, ImpactCode.RECOVERABLE);
   }
 
  public static ConfigIssue configWarning(String message) {
     return new ConfigIssue(message, ImpactCode.WARNING);
   }
 
   public static class ConfigIssue implements Issue {
 
     private final String message;
     private final Issue.ImpactCode impactCode;
 
     public ConfigIssue(String message, Issue.ImpactCode impactCode) {
       this.message = message;
       this.impactCode = impactCode;
     }
 
     @Override
     public String message() {
       return message;
     }
 
     @Override
     public Issue.ImpactCode impactCode() {
       return impactCode;
     }
 
     @Override
     public String toString() {
       return message();
     }
   }
 
  public static InternalIssue internalFatalError(Throwable cause) {
     return new InternalIssue(cause);
   }
 
  public static InternalIssue internalRecoverableError(Throwable cause) {
     return new InternalIssue(cause, ImpactCode.RECOVERABLE);
   }
 
  public static InternalIssue internalWarning(Throwable cause) {
     return new InternalIssue(cause, ImpactCode.WARNING);
   }
 
   public static class InternalIssue implements Issue {
 
     private final Throwable cause;
     private final Issue.ImpactCode impactCode;
 
     public InternalIssue(Throwable cause, Issue.ImpactCode impactCode) {
       this.cause = cause;
       this.impactCode = impactCode;
     }
 
     public InternalIssue(Throwable cause) {
       this.cause = cause;
       this.impactCode = Issue.ImpactCode.FATAL;
     }
 
     @Override
     public String message() {
       return cause.getMessage();
     }
 
     @Override
     public Issue.ImpactCode impactCode() {
       return impactCode;
     }
 
     @Override
     public String toString() {
       return message();
     }
   }
 
  public static InputIssue inputFatalError(String message, ImmutableDiagnosticLocator locator) {
     return new InputIssue(message, locator, ImpactCode.FATAL);
   }
 
  public static InputIssue inputRecoverableError(String message, ImmutableDiagnosticLocator locator) {
     return new InputIssue(message, locator, ImpactCode.RECOVERABLE);
   }
 
   public static InputIssue inputWarning(String message, ImmutableDiagnosticLocator locator) {
     return new InputIssue(message, locator, ImpactCode.WARNING);
   }
 
   public static class InputIssue implements Issue {
 
     private final String message;
     private final ImmutableDiagnosticLocator locator;
     private final Issue.ImpactCode impactCode;
 
     public InputIssue(String message, ImmutableDiagnosticLocator locator, Issue.ImpactCode impactCode) {
       this.message = message;
       this.locator = locator;
       this.impactCode = impactCode;
     }
 
     public ImmutableDiagnosticLocator locator() {
       return locator;
     }
 
     @Override
     public String message() {
       return new StringBuilder(256).append(locator).append(message).toString();
     }
 
     @Override
     public Issue.ImpactCode impactCode() {
       return impactCode;
     }
 
     @Override
     public String toString() {
       return message();
     }
   }
 
   public static LogIssue log(String message, Level logLevel) {
     return new LogIssue(message, logLevel);
   }
 
   public static class LogIssue implements Issue {
 //    private final String message;
 //    private final Level logLevel;
 
     private final LogRecord logRecord;
 
     public LogIssue(String message, Level logLevel) {
       this.logRecord = new LogRecord(logLevel, message);
 //      this.message = message;
 //      this.logLevel = logLevel;
     }
 
     public Level logLevel() {
       return logRecord.getLevel();
 //      return logLevel;
     }
 
     @Override
     public String message() {
       return logRecord.getMessage();
 //      return message;
     }
 
     @Override
     public Issue.ImpactCode impactCode() {
       return Issue.ImpactCode.NONE;
     }
 
     // From http://stackoverflow.com/questions/194765/how-do-i-get-java-logging-output-to-appear-on-a-single-line
     private static final class LogFormatter extends java.util.logging.Formatter {
 
 //      private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
       @Override
       public String format(LogRecord record) {
         StringBuilder sb = new StringBuilder();
 
         sb.append(new Date(record.getMillis()))
           .append(" ")
           .append(record.getLevel().getLocalizedName())
           .append(": ")
           .append(formatMessage(record));
 
 //              .append(LINE_SEPARATOR);
 
         if (record.getThrown() != null) {
           sb.append(" ")
             .append(record.getThrown().getClass())
             .append(" ")
             .append(record.getThrown().getMessage());
 //              try {
 //                  StringWriter sw = new StringWriter();
 //                  PrintWriter pw = new PrintWriter(sw);
 //                  record.getThrown().printStackTrace(pw);
 //                  pw.close();
 //                  sb.append(sw.toString());
 //              } catch (Exception ex) {
 //                  // ignore
 //              }
         }
 
         return sb.toString();
       }
     }
 
     @Override
     public String toString() {
       Formatter formatter = new LogFormatter();
       return formatter.format(logRecord);
     }
   }
 }
