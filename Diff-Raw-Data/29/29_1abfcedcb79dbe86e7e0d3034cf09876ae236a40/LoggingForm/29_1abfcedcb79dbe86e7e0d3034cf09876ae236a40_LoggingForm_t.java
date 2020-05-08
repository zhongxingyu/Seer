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
 import java.util.List;
 
 import org.jrecruiter.common.CollectionUtils;
 
 /**
  * Form for the Logging Action.
  *
  * @author Gunnar Hillert
 * @version $Id$
  *
  */
 public class LoggingForm implements Serializable {
 
     /** serialVersionUID. */
 	private static final long serialVersionUID = 5970927715241338665L;
 
 	/** List of Loggers, simplified for display purposes */
     private List<LoggerInfo> updatedLoggers = CollectionUtils.getArrayList();
 
     /** The user can enter a new logger to be configured */
     private LoggerInfo newLogger;
     
     /** Used for requesting a logfile download */
     private String fileName;
     
     //~~~~~Constructors~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	
     /**
 	 * 
 	 */
 	public LoggingForm() {
 		super();
 	}
 
     
 	//~~~~~Getters and Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	
 	
 	public List<LoggerInfo> getUpdatedLoggers() {
 		return updatedLoggers;
 	}
 
 	public void setUpdatedLoggers(List<LoggerInfo> updatedLoggers) {
 		this.updatedLoggers = updatedLoggers;
 	}
 
 	public LoggerInfo getNewLogger() {
 		return newLogger;
 	}
 
 	public void setNewLogger(LoggerInfo newLogger) {
 		this.newLogger = newLogger;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 	
 }
