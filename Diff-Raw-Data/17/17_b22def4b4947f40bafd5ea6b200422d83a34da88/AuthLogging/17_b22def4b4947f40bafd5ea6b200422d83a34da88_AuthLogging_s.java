 package ecologylab.oodss.logging;
 
import ecologylab.oodss.logging.Logging;
 
 /**
  * Interface for classes that will fire logging events based on authentication events.
  * 
  * @author Zachary O. Toups (zach@ecologylab.net)
  * 
  */
 public interface AuthLogging
 {
 	public void addLoggingListener(Logging log);
 
 	public void fireLoggingEvent(AuthenticationOp op);
 }
