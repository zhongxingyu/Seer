 package model.content;
 
 import model.DbControllable;
 
 public class Event implements DbControllable {
 
 	
 	/* DATENBANK
 	 * INT eventID
 	 * INT modules_moduleID
 	 * VAR name
 	 * VAR sws
 	 * VAR lecturer_email
 	 */
 	
 	
 	private String name, sws, lecturer_email;
 	private int eventID, modules_moduleID;
 	
 	// Konstruktor
 	public Event(int eventID, int modules_moduleID, String name, String sws, String lecturer_email) {
 		this.eventID = eventID;
 		this.modules_moduleID = modules_moduleID;
 		this.name = name;
 		this.sws = sws;
 		this.lecturer_email = lecturer_email;
 	}
 	
 	// Getter & Setter
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getSws() {
 		return sws;
 	}
 	public void setSws(String sws) {
 		this.sws = sws;
 	}
 	public String getLecturer_email() {
 		return lecturer_email;
 	}
 	public void setLecturer_email(String lecturer_email) {
 		this.lecturer_email = lecturer_email;
 	}
 	public int getEventID() {
 		return eventID;
 	}
 	public void setEventID(int eventID) {
 		this.eventID = eventID;
 	}
 	public int getModules_moduleID() {
 		return modules_moduleID;
 	}
 	public void setModules_moduleID(int modules_moduleID) {
 		this.modules_moduleID = modules_moduleID;
 	}
 
 	@Override
	public String[] toValueNames() {
		String values[] = {"eventID,", "modules_moduleID,", "name,", "sws,", "lecturer_email"};
 		return values;
 	}
 
 	@Override
	public String[] toValues() {
		String values[] = {eventID+"", modules_moduleID+"", name+"",sws+"",lecturer_email+""};
		return values;
 	}
 	
 	
 }
