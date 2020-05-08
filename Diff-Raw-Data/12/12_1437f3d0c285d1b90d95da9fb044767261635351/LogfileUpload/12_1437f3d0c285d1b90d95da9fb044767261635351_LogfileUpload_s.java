 package what.web;
 
 import controllers.Localize;
 import play.data.validation.Constraints.Required;
 import what.Facade;
 
 import java.io.File;
 
 /**
  * class to support form validation in logfilePath uploads
  * @author Lukas Ehnle
  *
  */
 public class LogfileUpload {
 	@Required
 	public String pathToLogfile;
 	
 	/**
 	 * method to check if the uploaded path exists on the serverside
 	 * @return returns null or an error message
 	 */
 	public String validate() {
		if (new File(pathToLogfile).exists()) {
 			//start parser
			Facade f = Facade.getFacadeInstance();
			if(f == null) {
				return Localize.get("Internal error!");
			}
			f.parseLogFile(pathToLogfile);
 			return null;
 		}
		return Localize.get("admin.wrongPath");
 	}
 }
