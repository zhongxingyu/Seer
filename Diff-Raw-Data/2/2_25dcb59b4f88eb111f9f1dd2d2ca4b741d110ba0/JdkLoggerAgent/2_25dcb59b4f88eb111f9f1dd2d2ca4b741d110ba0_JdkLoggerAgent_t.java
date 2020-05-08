 package in.v8delta.template.myWebAppTmpl.core.log;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Agent for JDK logger Implementation
  * 
 * Level is varying between application logger type and JDK Logger implementations
  * Below is the matching betweek them.
  * 
  * <pre>
  * ---------------------------
  * |   APP     |    JDK      |
  * ---------------------------
  * | ALL       | ALL         |
  * | TRACE     | FINER,FINEST|
  * | DEBUG     | FINE        |
  * | INFO      | INFO,CONFIG |
  * | WARN      | WARNING     |
  * | ERROR     | SEVERE      |
  * | FATAL     | SEVERE      |
  * ---------------------------
  * <pre>
  * 
  * @author v8-suresh
  *
  */
 public class JdkLoggerAgent implements LoggerAgent{
 
 	private final Logger logger;
 	
 	public JdkLoggerAgent(final Logger logger) {
 		this.logger = logger;
 	}
 	
 	public boolean isTraceEnabled() {
 		return this.logger.isLoggable(Level.FINER) ||
 				this.logger.isLoggable(Level.FINEST);
 	}
 
 	public boolean isDebugEnabled() {
 		return this.logger.isLoggable(Level.FINE); 
 	}
 
 	public boolean isInfoEnabled() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isWarnEnabled() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isErrorEnabled() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isFatalEnabled() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public String getLevel() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setLevel(String level) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void trace(Object message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void trace(Object message, Throwable t) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void debug(Object message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void debug(Object message, Throwable t) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void info(Object message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void info(Object message, Throwable t) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void warn(Object message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void warn(Object message, Throwable t) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void error(Object message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void error(Object message, Throwable t) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void fatal(Object message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void fatal(Object message, Throwable t) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
