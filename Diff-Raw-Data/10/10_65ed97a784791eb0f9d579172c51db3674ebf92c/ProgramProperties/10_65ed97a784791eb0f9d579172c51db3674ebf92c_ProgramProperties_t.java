 package program;
 
 import java.util.ArrayList;
 import utilities.pdf.FieldAndVal;
 
 //-----------------------------------------------------------------------------
 /**
  * The <code>ProgramProperties</code> class represents all the settings used 
  * within the program. 
  * There are two types of settings: those automatically set and updated by the
  * program in response to user events and new data, and those configured by the
  * user to adapt the program to the user's needs.
  * The <code>ProgramProperties</code> class includes all settings available to
  * the program. Not all of these settings are necessarily set at all times.
  * Not all users can set all settings. The permissions are as follows:
  * Officers - 
  * Shift Commanders - 
  * Supervisor - can change all user configurable settings
  */
 public class ProgramProperties {
 //-----------------------------------------------------------------------------
 	/**stores the absolute path name of the most recent Shift Cdr Summary Report*/
 	String latestReport;
 	
 	String lastestVideo;
 		
 	/**list of <code>FieldAndval</code> objects representing the set properties*/
 	//(order doesn't matter in this list)
 	 ArrayList<FieldAndVal> setPropsList; 
 //-----------------------------------------------------------------------------
 	public ProgramProperties(){
 		latestReport = "";
		setPropsList = new ArrayList<FieldAndVal>();
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return latestReport - the absolute path name of the most recently saved
 	 * Shift Cdr Summary Report
 	 */
 	public String getLatestReport() {
 		return latestReport;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param latestReport - the absolute path name of the most recently saved 
 	 * Shift Cdr Summary Report
 	 */
 	public void setLatestReport(String latestReport) {
 		this.latestReport = latestReport;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Set the specified property equal to the specified value.
 	 * @param key - the property to set
 	 * @param value - the value to set the property to
 	 */
 	public void setProp(String key, String value){		
 		//Check which of property to set & set it accordingly
 		if(key.equals("UMPD.latestReport")){
 			this.setLatestReport(value);
 			return;
 		}  
 		if (key.equals("UMPD.latestVideo")) {
 			this.setLastestVideo(value);
 			return;
 		}
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return - a list of <code>FieldAndval</code> objects representing the set 
 	 * properties
 	 */
 	public ArrayList<FieldAndVal> getSetPropsList(){
 		createSetPropsList();
 		return setPropsList;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	private void createSetPropsList(){
 		if((this.latestReport).length()>0){
 			setPropsList.add(new FieldAndVal("UMPD.latestReport", this.latestReport));
			setPropsList.add(new FieldAndVal("UMPD.latestVideo", this.lastestVideo));
 		}
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return lastestVideo - 
 	 */
 	public String getLastestVideo() {
 		return lastestVideo;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param lastestVideo - the lastestVideo value to set
 	 */
 	public void setLastestVideo(String lastestVideo) {
 		this.lastestVideo = lastestVideo;
 	}
 //-----------------------------------------------------------------------------
 }
