 package info.mikaelsvensson.ftpbackup.log.report;
 
 import info.mikaelsvensson.ftpbackup.util.UnknownNamedArgumentException;
 
 import java.io.PrintStream;
 import java.util.Date;
 
 public class ConsoleReport extends AbstractSummaryReport {
 // ------------------------------ FIELDS ------------------------------
 
     private final PrintStream stream;
     private final String template;
     private final String destinationTemplate;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public ConsoleReport(boolean isStderr, String template, String destinationTemplate, Date backupStartDate) {
         super(backupStartDate);
         stream = isStderr ? System.err : System.out;
         this.template = template;
         this.destinationTemplate = destinationTemplate;
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Report ---------------------
 
     @Override
     public void generate() {
         try {
             stream.print(getFormattedText(template, destinationTemplate));
         } catch (UnknownNamedArgumentException e) {
            e.printStackTrace(stream);
         }
     }
 }
