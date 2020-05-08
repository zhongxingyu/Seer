 package info.mikaelsvensson.ftpbackup.report;
 
@SuppressWarnings("UnusedDeclaration")
 public class ReportException extends Exception {
     public ReportException() {
     }
 
     public ReportException(Throwable cause) {
         super(cause);
     }
 
     public ReportException(String message) {
         super(message);
     }
 
     public ReportException(String message, Throwable cause) {
         super(message, cause);
     }
 }
