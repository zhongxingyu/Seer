 package eu.alertproject.iccs.mlsensor.subscribers.api;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * User: fotis
  * Date: 02/11/11
  * Time: 22:33
  */
 public class SimpleLoggerVisitor implements MailActionVisitor{
 
     private Logger logger = LoggerFactory.getLogger(SimpleLoggerVisitor.class);
 
    private int counter = 0;
 
     @Override
     public void visit(String message) {
 
        logger.info("void visit() {} = {}",counter++,message);
 
     }
 }
