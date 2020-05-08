 package friendzone.elec3609.model;
 
 
 import friendzone.elec3609.service.DatabaseHandler;
 
 
 public class Invitation {
 	 
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	int id;
 	private String senderID;
 	private String recipientID;
 	private String message;
 	private int projectID;
 	
 	 
 	public Invitation(String senderSID, String recipientSID, int projectID, String message) {
 		this.senderID = senderSID;
 		this.recipientID = recipientSID;
 		this.projectID = projectID;
 		id = dbHandler.addInvitation(projectID, senderID, recipientID);
 		setMessage(message);
 	 }
 	
 	public Invitation(int id, String senderSID, String recipientSID, int projectID, String message) {
 		this.senderID = senderSID;
 		this.recipientID = recipientSID;
 		this.projectID = projectID;
 		this.id = id;
 		setMessage(message);
 	 }
 	
 	 public int getID(){
 		 return id;
 	 }
 	 
 	 public String getMessage(){
 		return message;
 	 }
 	 
 	 public void accept(){
		 dbHandler.update("Invitation", recipientID, "ACCEPTED", true);
 	 }
 	 
 	 public void decline(){
		 
 	 }
 	 
 	 public void setMessage(String message){
 		 this.message = message;
 	 }
 	 
 	 public Student getSender(){
 		 return dbHandler.getStudent(senderID);
 	 }
 	 
 	 public Student getRecipient(){
 		 return dbHandler.getStudent(recipientID);
 	 }
 	 
 	 public Project getProject(){
 		 return dbHandler.getProject(projectID);
 	 }
 	 
 	 public void copyValues(Invitation invite) {
 		 this.senderID = invite.senderID;
 		 this.recipientID = invite.recipientID;
 		 this.message = invite.message;
 		 this.projectID = invite.projectID;
 	 }
 }
