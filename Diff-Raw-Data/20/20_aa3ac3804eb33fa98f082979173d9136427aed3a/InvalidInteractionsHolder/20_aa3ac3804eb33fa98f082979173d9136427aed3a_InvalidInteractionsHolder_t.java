 package mx.itesm.mexadl.metrics.util;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.aspectj.bridge.AbortException;
 import org.aspectj.bridge.IMessage;
 import org.aspectj.bridge.IMessage.Kind;
 import org.aspectj.bridge.IMessageHolder;
 import org.aspectj.bridge.ISourceLocation;
 
 /**
  * The InvalidInteractionsHolder class is responsible for the verification of
  * valid interactions between java classes.
  * 
  * @author jccastrejon
  * 
  */
 public class InvalidInteractionsHolder implements IMessageHolder {
 
     static {
         // Logging configuration
         try {
             LogManager.getLogManager().readConfiguration(
                     MexAdlTask.class.getClassLoader().getResourceAsStream(
                             "mx/itesm/mexadl/metrics/logging-interactions.properties"));
         } catch (Exception e) {
             System.out.println("Unable to register logging configuration: " + e);
         }
     }
 
     /**
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(InvalidInteractionsHolder.class.getName());
 
     @Override
     @SuppressWarnings("unchecked")
     public boolean handleMessage(final IMessage message) throws AbortException {
        InvalidInteractionsHolder.logger.log(Level.INFO, "Starting interactions log...");
         // Log only warnings of invalid interactions
         if ((message.isWarning()) && (message.getExtraSourceLocations() != null)) {
             for (ISourceLocation location : (List<ISourceLocation>) message.getExtraSourceLocations()) {
                 if (location.getSourceFile().getAbsolutePath().contains("mx/itesm/mexadl")) {
                     InvalidInteractionsHolder.logger.log(Level.INFO,
                             "[message]" + message.getMessage() + "[/message][details]" + message.getDetails()
                                     + "[/details][location]" + message.getSourceLocation() + "[/location]");
                 }
             }
         }
 
         return true;
     }
 
     @Override
     public void dontIgnore(final Kind kind) {
         // NO-OP
     }
 
     @Override
     public void ignore(final Kind kind) {
         // NO-OP
     }
 
     @Override
     public boolean isIgnoring(final Kind kind) {
         return false;
     }
 
     @Override
     public void clearMessages() throws UnsupportedOperationException {
         // NO-OP
     }
 
     @Override
     public IMessage[] getMessages(Kind kind, boolean orGreater) {
         return null;
     }
 
     @Override
     @SuppressWarnings("rawtypes")
     public List getUnmodifiableListView() {
         return null;
     }
 
     @Override
     public boolean hasAnyMessage(Kind kind, boolean orGreater) {
         return false;
     }
 
     @Override
     public int numMessages(Kind kind, boolean orGreater) {
         return 0;
     }
 }
