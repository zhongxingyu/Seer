 package ai.reaver;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
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
 public class LoggerClientImpl implements LoggerClient {
 
     final private Logger logger;

 
     @Inject
    public LoggerClientImpl(@Assisted(value = "loggerName")
                            final String loggerName) {
         logger = Logger.getLogger(loggerName);
     }
 
 
     @Override
     public void log(final String message, final Object objectOfWhichToStringWillBeCalledButPermitsNull) {
        logger.log(Level.ALL, message, objectOfWhichToStringWillBeCalledButPermitsNull != null ?
                 objectOfWhichToStringWillBeCalledButPermitsNull.toString() :
                 "");
     }
 }
