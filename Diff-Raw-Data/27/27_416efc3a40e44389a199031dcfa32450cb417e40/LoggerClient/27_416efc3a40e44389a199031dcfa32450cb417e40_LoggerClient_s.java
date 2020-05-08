 package ai.reaver;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: 12/18/10
  * Time: 9:16 PM
  *
  * @author Ravindranath Akila
  */
 public interface LoggerClient {
     public void log(final String message, final Object objectOfWhichToStringWillBeCalledButPermitsNull);
 }
