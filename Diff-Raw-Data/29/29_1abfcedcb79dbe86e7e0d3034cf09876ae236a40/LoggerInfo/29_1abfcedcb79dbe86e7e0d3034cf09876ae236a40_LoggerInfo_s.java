 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.web.actions.admin;
 
 import java.io.Serializable;
 
 import org.jrecruiter.web.actions.util.LoggingUtil.LogLevels;
 
 /**
  * Logger information.
  *
  * @author Gunnar Hillert
 * @version $Id: LogFileInfo.java 291 2008-11-13 02:20:08Z ghillert $
  *
  */
 public class LoggerInfo implements Serializable {
 
     /** serialVersionUID. */
 	private static final long serialVersionUID = 5542917607467653570L;
 	
 	private String    loggerName;
     private LogLevels logLevel;
     private LogLevels newLogLevel;
     
     //~~~~~Constructors~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	
     /**
 	 * 
 	 */
 	public LoggerInfo() {
 		super();
 	}
 	/**
 	 * @param loggerName
 	 * @param logLevel
 	 */
 	public LoggerInfo(String loggerName, int logLevelAsInt) {
 		super();
 		this.loggerName = loggerName;
 		this.logLevel = LogLevels.getLogLevelFromId(logLevelAsInt);
 	}
     
 	//~~~~~Getters and Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	
 	public String getLoggerName() {
 		return loggerName;
 	}
 	public void setLoggerName(String loggerName) {
 		this.loggerName = loggerName;
 	}
 	public LogLevels getLogLevel() {
 		return logLevel;
 	}
 	public void setLogLevel(LogLevels logLevel) {
 		this.logLevel = logLevel;
 	}
 	public LogLevels getNewLogLevel() {
 		return newLogLevel;
 	}
 	public void setNewLogLevel(LogLevels newLogLevel) {
 		this.newLogLevel = newLogLevel;
 	}
 	
 }
