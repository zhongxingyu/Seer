 package ai.reaver;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: 12/18/10
  * Time: 9:16 PM
  *
  * @author Ravindranath Akila
  */
 public class LoggerClientImpl implements LoggerClient, Serializable {
 

    transient final private Logger logger;
 
     @Inject
     public LoggerClientImpl(
             @Assisted
             final String loggerName) {
         logger = Logger.getLogger(loggerName);
     }
 
 
     @Override
     public void log(final String message, final Object objectOfWhichToStringWillBeCalledButPermitsNull) {
         System.out.println("Logger Name:" + logger.getName());
 
         logger.log(Level.SEVERE, message, objectOfWhichToStringWillBeCalledButPermitsNull != null ?
                 objectOfWhichToStringWillBeCalledButPermitsNull.toString() :
                 "");
         System.out.println(message + objectOfWhichToStringWillBeCalledButPermitsNull);
     }
 }
