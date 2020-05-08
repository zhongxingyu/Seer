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
//    private final String loggerName;
 
     @Inject
    public LoggerClientImpl(
            @Assisted
            final String loggerName) {
        // this.loggerName = loggerName;
         logger = Logger.getLogger(loggerName);
     }
 
//    public String getLoggerName() {
//        return loggerName;
//    }
 
     @Override
     public void log(final String message, final Object objectOfWhichToStringWillBeCalledButPermitsNull) {
        System.out.println("Logger Name:" + logger.getName());

        logger.log(Level.SEVERE, message, objectOfWhichToStringWillBeCalledButPermitsNull != null ?
                 objectOfWhichToStringWillBeCalledButPermitsNull.toString() :
                 "");
        System.out.println(message + objectOfWhichToStringWillBeCalledButPermitsNull);
     }
 }
