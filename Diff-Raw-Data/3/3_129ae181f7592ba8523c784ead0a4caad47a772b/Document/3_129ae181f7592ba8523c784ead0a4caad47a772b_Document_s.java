 package model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Representation of the document.
  * Contains important meta data regarding the document.
  * Also contains data of the document
  * 
  * Thread safety argument:
  * 
  * This document is thread safe because the only public methods are 
  * either getters that we do not need to worry about or mutators
  * that are synchronized around the object the mutate. Our insert and 
  * delete content methods are synchronized so that only one of these can be 
  * editing the content of the document at once. Also the objects that we do return
  * are not the actual objects if they are mutable, we return clones or immutable objects. 
  */
 public class Document {
 	
 	private final String documentName;
 	private String content;
 	private Calendar lastEditDateTime;
 	private List<String> collaborators;
 	private int versionNumber;
 	private List<Change> changeList;	
 	
 	/**
 	 * Constructor of the class Document. Creates a new document with the given document ID, document name and collaborator
 	 * @param documentName String representing the name of the document
 	 * @param collaborator String representing the name of the user that is creating the document
 	 */
 	public Document(String documentName, String collaborator) {
 		this.documentName = documentName;
 		this.content = "";
 		this.lastEditDateTime = Calendar.getInstance();
 		this.collaborators = new ArrayList<String> ();
 		this.collaborators.add(collaborator);
 		this.versionNumber = 0;
 		this.changeList = new ArrayList<Change>();
 	}
 	
 	/**
 	 * Returns the name of the document
 	 * @return String representing the name of the document
 	 */
 	public String getName() {
 		return documentName;
 	}
 		
 	/**
 	 * Inserts new content into the given position in the document
 	 * The content should be a letter that results from the user typing. 
 	 * The position should be less than the lenth of the text in the document
 	 * For example, insertion into the word "abcd" at position 1 with newLetter "e" will
 	 * result in the new string "aebcd". Insertion into the word "acd" at position 3 with
 	 * newLetter "f" results in the new string "acdf"
 	 * @param newLetter New letter to be inserted into the document
 	 * @param position Position in the document at which new content should be inserted
 	 * @return New content of the document
 	 */
 	public synchronized void insertContent(String newLetter, int position, int version) {
 		synchronized(content) {
 			position = transformPosition(position, version);
 			position = Math.min(position, content.length());
 			content = content.substring(0, position) + newLetter + content.substring(position);
 			updateVersion();
 			changeList.add(new Change(position, newLetter.length(), version));
 		}
 	}
 	
 
 	/**
 	 * Deletes old content from the document at the given position. 
 	 * The position should be less than the length of the text in the document
 	 * For example, deletion from the word "abcd" at position 1  will
 	 * result in the new string "acd". Deletion from the word "acd" at
 	 * position 2 results in the new string "ac"
 	 * @param position Old content that is removed from the document
 	 * @param length Length of text that is being deleted from the document
 	 * @return New content of the document
 	 */
 	public void deleteContent(int position, int length, int version) {
 		synchronized(content) {
 			position = transformPosition(position, version);
 			position = Math.min(position, content.length());
			content = content.substring(0, position) + content.substring(position + length);
 			updateVersion();
 			changeList.add(new Change(position, -length, version));
 		}
 	}
 	
 	/**
 	 * Finds the position at which edit should be made given version history
 	 * @param position Position of edit at the initial version number
 	 * @param version New version number
 	 * @return Position of final edit
 	 */
 	private int transformPosition(int position, int version) {
 		for (Change ch : changeList){
 			if (ch.getVersion() > version){
 				if (ch.getPosition() < position){
 					position += ch.getCharInserted();
 				}
 			}
 		}
 		return position;
 	}
 	
 	/**
 	 * Method that represents the content of the document in the form of a string. Does not contain meta data
 	 * about the document
 	 * @return a String that represents the content of the document
 	 */
 	@Override
 	public synchronized String toString() {
 		synchronized(content){
 			return content;
 		}
 	}
 
 	/**
 	 * Sets the lastEditDateTime state of the class to a date object that represents the current date and time
 	 */
 	public void setLastEditDateTime() {
 		synchronized(content){
 			lastEditDateTime = Calendar.getInstance();
 		}
 	}
 	
 	/**
 	 * @return String representation of the time of the last edit of the document
 	 */
 	public String getDate() {
 		synchronized(content){
 			String AM_PM = lastEditDateTime.get(Calendar.AM_PM) == 0 ? "AM" : "PM";
 			int integerHour = lastEditDateTime.get(Calendar.HOUR);
 			String currentHour = integerHour == 0 ? "12" : String.valueOf(integerHour);
 			int integerMinute = lastEditDateTime.get(Calendar.MINUTE);
 			String currentMinute = integerMinute < 10 ? "0" + String.valueOf(integerMinute) : String.valueOf(integerMinute);
 			String currentMonth = String.valueOf(lastEditDateTime.get(Calendar.MONTH) + 1);
 			String currentDay = String.valueOf(lastEditDateTime.get(Calendar.DAY_OF_MONTH));
 			
 			String date = currentHour + ":" + currentMinute + " " + AM_PM + " , " + currentMonth + "/" + currentDay;
 			return date;
 		}
 	}
 	
 	/**
 	 * Gets the version number of the document saved on the server
 	 * @return Version number of the document saved on the server
 	 */
 	public int getVersion() {
 		synchronized(content){
 			return versionNumber;
 		}
 	}
 	
 	/**
 	 * Increments the version number of the document saved on the server
 	 */
 	public void updateVersion() {
 		synchronized(content){
 			versionNumber++;
 		}
 	}
 	
 	/**
 	 * Method that adds the name of a new collaborator to the list of currently online
 	 * collaborators
 	 * @param newCollaborator String representing the name of new collaborator
 	 */
 	public void addCollaborator(String newCollaborator) {
 		synchronized(collaborators){			
 			if (! collaborators.contains(newCollaborator))
 				collaborators.add(newCollaborator);
 		}
 	}
 	
 	/**
 	 * Method that removes the name of a collaborator if the collaborator exits the document
 	 * @param collaborator String representing name of the collaborator who just exited the
 	 * document
 	 */
 	public void removeCollaborator(String collaborator) {
 		synchronized(collaborators){
 			collaborators.remove(collaborator);
 		}
 	}
 	
 	/**
 	 * Returns a list of strings that represent the users that have edited this document
 	 * @return List of names of the different collaborators of the document
 	 */
 	public String getCollab(){
 		synchronized(collaborators){
 			String collab = collaborators.toString();
 			int collaboratorLength = collab.length();
 			collab = collab.substring(1, collaboratorLength - 1);
 			return collab;
 		}
 	}
 	
 	/**
 	 * Returns collaborators in a document in the form of a list of strings
 	 * @return A list of strings representing the collaborators of the document. These collaborators
 	 * do not necessarily have to have the document open at the current time instance
 	 */
 	public List<String> getCollabList(){
 		synchronized(collaborators){
 			return new ArrayList<String>(this.collaborators);
 		}
 	}
 		
 }
