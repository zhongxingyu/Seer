 package lightLogger;
 
 import java.util.Set;
 
 /**
 * Basic implementation of ILogger. Output on stdo. Format: [Level.getLabel()] class: message
  * 
  * @author executor
  * 
  */
 public class LoggerBase implements ILogger {
 
 	private int logMask = Level.DEFAULT.ALL.getLevel().getValue();
 
 	@Override
 	public int getLogMask() {
 		return this.logMask;
 	}
 
 	@Override
 	public void setLogMask(Set<Level> level) {
 		this.logMask = 0;
 		int tmp;
 		for (Level lvl : level) {
 			tmp = this.logMask;
 			this.logMask += lvl.getValue();
 			if (this.logMask < tmp) {
 				this.logMask = tmp;
 				this.logWarn(this.getClass(),
 						"setLogMask(Set<Level>) overflow!");
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void setLogMask(int value) {
 		this.logMask = value;
 	}
 
 	@Override
 	public void log(Level level, Class<?> clazz, String message) {
		if ((this.logMask & level.getValue()) == level.getValue())
			System.out.println("[" + level.getLabel() + "] " + clazz.getName()
					+ ": " + message);
 	}
 
 	@Override
 	public void logError(Class<?> clazz, String message) {
		this.log(Level.DEFAULT.ERROR.getLevel(), clazz, message);
 	}
 
 	@Override
 	public void logWarn(Class<?> clazz, String message) {
 		this.log(Level.DEFAULT.WARN.getLevel(), clazz, message);
 
 	}
 
 	@Override
 	public void logInfo(Class<?> clazz, String message) {
 		this.log(Level.DEFAULT.INFO.getLevel(), clazz, message);
 	}
 
 	@Override
 	public void logTrace(Class<?> clazz, String message) {
 		this.log(Level.DEFAULT.TRACE.getLevel(), clazz, message);
 	}
 
 	@Override
 	public void logDebug(Class<?> clazz, String message) {
 		this.log(Level.DEFAULT.DEBUG.getLevel(), clazz, message);
 	}
 }
