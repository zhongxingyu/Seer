 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.Gson;
 import edu.wpi.cs.wpisuitetng.modules.AbstractModel;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.AcceptanceTest;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.Attachment;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.DevelopmentTask;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.Note;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.TransactionHistory;
 
 /**
  * Basic Requirement class
  * 
  * @author david
  *
  */
 public class Requirement extends AbstractModel {
 	/**  the ID of the requirement */
 	private int id; //TODO: move ID stuff to server side?
 	
 	/**  the name of the requirement */
 	private String name;
 	
 	/**  the release number of the requirement */
 	private String release;
 	
 	/**  the project status of the requirement */
 	private RequirementStatus status;
 
 	/**  the priority of the requirement */
 	private RequirementPriority priority;
 	
 	/**  a short description of the requirement */
 	private String description;
 	
 	/**  the estimated amount of time to complete the requirement */
 	private int estimate;
 	
 	/**  the actual effort of completing the requirement */
 	private int actualEffort;
 	
 	/** a flag indicating if the requirement is active or deleted */
 	private boolean activeStatus;
 	
 	/** history of transactions of the requirement */
 	private TransactionHistory history;
 	
 	/** the type of the requirement */
 	private RequirementType type;
 	
 	/** subrequirements that must be completed before the current requirement is considered complete */
 	private List<Requirement> subRequirements;
 	
 	/** notes associated with the requirement */
 	private List<Note> notes;
 	
 	/** iteration the requirement is assigned to */
 	private Iteration iteration;
 	
 	/** team members the requirement is assigned to 
 	 *  need to figure out the class of a user name, then use that instead of TeamMember 
 	 */
 	private List<String> assignedTo; 
 	
 	/** development tasks associated with the requirement */
 	private List<DevelopmentTask> tasks;
 	
 	/** acceptance tests associated with the requirement */
 	private List<AcceptanceTest> tests;
 	
 	/** attachments associated with the requirement */
 	private List<Attachment> attachments;
 	
 	/**
 	 * Constructs a Requirement with default characteristics
 	 */
 	public Requirement() {
 		super();
 		name = description = "";
 		release = "";
 		status = RequirementStatus.NEW;
 		priority = RequirementPriority.BLANK;
 		estimate = actualEffort = 0;
 		activeStatus = true;
 		history = new TransactionHistory();
		iteration = (new Iteration("Backlog"));
 		type = RequirementType.BLANK;
 		notes = new ArrayList<Note>();
 		tasks = new ArrayList<DevelopmentTask>();
 		tests = new ArrayList<AcceptanceTest>();
 		attachments = new ArrayList<Attachment>(); 
 	}
 
 	/**
 	 * Construct a Requirement with required properties provided and others set to default
 	 * 
 	 * @param id The ID number of the requirement
 	 * @param name The name of the requirement
 	 * @param description A short description of the requirement
 	 */
 	// need to phase out supplying the ID
 	public Requirement(int id, String name, String description) {
 		this();
 		this.id = id;
 		this.name = name;
 		this.description = description;
 	}
 
 	/**
 	 * Constructs a requirement from the provided characteristics
 	 * 
 	 * @param id The ID number of the requirement
 	 * @param name The name of the requirement
 	 * @param release The release number of the requirement
 	 * @param status The status of the requirement
 	 * @param description A short description of the requirement
 	 * @param estimate The estimated time required by the task. This is in a point system decided by the user
 	 * @param effort The estimated amount of work required by the requirement.
 	 * @deprecated
 	 */
 	@Deprecated
 	public Requirement(int id, String name, String release, RequirementStatus status, RequirementPriority priority, String description, int estimate, int effort){
 		this.id = id;
 		this.name = name;
 		this.release = release;
 		this.status = status;
 		this.priority = priority;
 		this.description = description;
 		this.estimate = estimate;
 		this.actualEffort = effort;
 	}
 
 	/**
 	 * Returns an instance of Requirement constructed using the given
 	 * Requirement encoded as a JSON string.
 	 * 
 	 * @param the JSON-encoded Requirement to deserialize
 	 * @return the Requirement contained in the given JSON
 	 */
 	public static Requirement fromJson(String json) {
 		final Gson parser = new Gson();
 		return parser.fromJson(json, Requirement.class);
 	}
 
 	/**
 	/**Getter for id
 	 * 
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**Setter for the id
 	 * 
 	 * @param id the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	/**getter for the id
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**setter for the name
 	 * 
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 		if(name.length() > 100) this.name = name.substring(0, 100);
 	}
 
 	/**getter for the name
 	 * 
 	 * @return the release
 	 */
 	public String getRelease() {
 		return release;
 	}
 
 	/**Setter for the release number
 	 * 
 	 * @param release the release to set
 	 */
 	public void setRelease(String release) {
 		this.release = release;
 	}
 
 	/**Getter for the release number
 	 * 
 	 * @return the status
 	 */
 	public RequirementStatus getStatus() {
 		return status;
 	}
 
 	/**Setter for the status
 	 * 
 	 * @param status the status to set
 	 */
 	public void setStatus(RequirementStatus status) {
 		if (status != this.status){
 			String originalStatus = this.status.name();
 			String newStatus = status.name();
 			String message = ("Changed status from " + originalStatus + " to " + newStatus);
 			this.history.add(message);
 		}
 
 		this.status = status;
 		
 	}
 
 	/**Getter for the description
 	 * 
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**Setter for the description
 	 * 
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**Getter for the estimate
 	 * 
 	 * @return the estimate
 	 */
 	public int getEstimate() {
 		return estimate;
 	}
 
 	/**Setter for the estimate
 	 * 
 	 * @param estimate the estimate to set
 	 */
 	public void setEstimate(int estimate) {
 		this.estimate = estimate;
 	}
 
 	/**Getter for the estimate
 	 * 
 	 * @return the effort
 	 */
 	public int getEffort() {
 		return actualEffort;
 	}
 
 	/**Setter for the effort
 	 * 
 	 * @param effort the effort to set
 	 */
 	public void setEffort(int effort) {
 		this.actualEffort = effort;
 	}
 
 	/**Getter for the priority
 	 * 
 	 * @return the priority
 	 */
 	public RequirementPriority getPriority() {
 		return priority;
 	}
 
 	/**Setter for the priority
 	 * 
 	 * @param priority the priority to set
 	 */
 	public void setPriority(RequirementPriority priority) {
 		if (priority != this.priority){
 			String originalPriority = this.priority.name();
 			String newPriority = priority.name();
 			String message = ("Changed priority from " + originalPriority + " to " + newPriority);
 			this.history.add(message);
 		}
 		
 		this.priority = priority;
 	}
 	
 	/**Getter for the type
 	 * 
 	 * @return the type
 	 */
 	public RequirementType getType() {
 		return type;
 	}
 
 	/**Setter for the type
 	 * 
 	 * @param type the type to set the requirement to
 	 */
 	public void setType(RequirementType type) {
 		this.type = type;
 	}
 	
 	/**Getter for the sub-requirements
 	 * 
 	 * @return a list of the sub-requirements
 	 */
 	public List<Requirement> getSubRequirements(){
 		return subRequirements;
 	}
 	
 	/**Method to add a requirement to the list of sub-requirements
 	 * 
 	 * @param requirement Requirement to add
 	 */
 	public void addSubRequirement(Requirement subRequirement){
 		this.subRequirements.add(subRequirement);
 	}
 	
 	/** Method to remove a requirement to the list of sub-requirements
 	 * 
 	 * @param id The id of the requirement to be remove from the list of sub-requirements
 	 */
 	public void removeSubRequirement (int id){
 		// iterate through the list looking for the requirement to remove
 		for (int i=0; i < this.subRequirements.size(); i++){
 			if (subRequirements.get(i).getId() == id){
 				// remove the id
 				subRequirements.remove(i);
 				break;
 			}
 		}
 	}
 	
 	/** Getter for the notes
 	 * 
 	 * @return the list of notes associated with the requirement
 	 */
 	public List<Note> getNotes(){
 		return notes;
 	}
 	
 	/** Method to add a note to the list of notes
 	 * 
 	 * @param note The note to add to the list
 	 */
 	public void addNote(Note note){
 		notes.add(note);
 	}
 	
 	/** Method to remove a note from a list of notes
 	 * 
 	 * @param id The id of the note to be deleted
 	 */
 	public void removeNote(int id){
 		// iterate through the list looking for the note to remove
 		for (int i=0; i < this.notes.size(); i++){
 			if (notes.get(i).getId() == id){
 				// remove the id
 				notes.remove(i);
 				break;
 			}
 		}
 	}
 	
 	/** Getter for the list of development tasks
 	 * 
 	 * @return the list of development tasks
 	 */
 	public List<DevelopmentTask> getTasks(){
 		return tasks;
 	}
 	
 	/** Method to add a development task
 	 * 
 	 * @param task the task to be added to the list of development tasks
 	 */
 	public void addTask(DevelopmentTask task){
 		tasks.add(task);
 	}
 	
 	/** Method to remove a development task
 	 * 
 	 * @param 
 	 */
 	public void removeTask(int id){
 		// iterate through the list looking for the note to remove
 		for (int i=0; i < this.tasks.size(); i++){
 			if (tasks.get(i).getId() == id){
 				// remove the id
 				tasks.remove(i);
 				break;
 			}
 		}
 	}
 	
 	/** Getter for AcceptanceTests
 	 * 
 	 * @return the list of acceptance tests for the requirement
 	 */
 	public List<AcceptanceTest> getTests(){
 		return tests;
 	}
 	
 	/** Method for adding an Acceptance Test
 	 * 
 	 * @param test the acceptance test to implement
 	 */
 	public void addTest(AcceptanceTest test){
 		tests.add(test);
 	}
 	
 	/** Method for removing an Acceptance Test
 	 * 
 	 * @param id the id of the test to remove
 	 */
 	public void removeTest(int id){
 		// iterate through the list looking for the note to remove
 		for (int i=0; i < this.tests.size(); i++){
 			if (tests.get(i).getId() == id){
 				// remove the id
 				tests.remove(i);
 				break;
 			}
 		}
 	}
 	
 	/** Getter for attachments
 	 * 
 	 * @return the list of attachments
 	 */
 	public List<Attachment> getAttachments(){
 		return attachments;
 	}
 	
 	/** Method to add an attachment
 	 * 
 	 * @param attachment Attachment to add
 	 */
 	public void addAttachment(Attachment attachment){
 		attachments.add(attachment);
 	}
 	
 	/** Method to remove an attachment
 	 * 
 	 * @param id ID of the attachment to be removed
 	 */
 	public void removeAttachment(int id){
 		// iterate through the list looking for the note to remove
 		for (int i=0; i < this.attachments.size(); i++){
 			if (attachments.get(i).getId() == id){
 				// remove the id
 				attachments.remove(i);
 				break;
 			}
 		}
 	}
 	/** Getter for Iteration. Currently deals in Strings, but will deal with Iterations in the future
 	 * 
 	 * @return a string representing the iteration it has been assigned to
 	 */
 	public Iteration getIteration() {
 		return iteration;
 	}
 
 	/** Setter for iteration. Currently deals with strings, but will deal with Iterations in the future.
 	 * 
 	 * @param iteration the iteration to assign the requirement to
 	 */
 	public void setIteration(Iteration newIteration) {
 		if(this.iteration == null) this.iteration = newIteration;
 		if (!this.iteration.equals(newIteration)){
 			String originalIteration = this.iteration.toString();
 			String newIterationString = newIteration.toString();
 			String message = ("Moved from " + originalIteration + " to " + newIterationString);
 			this.history.add(message);
 		}
 		
 		this.iteration = newIteration;
 	}
 	
 	/** Getter for AssignedTo
 	 * 
 	 * @return the list of strings representing the users for whom the requirement has been assigned to.
 	 */ 
 	public List<String> getAssignedTo() {
 		return assignedTo;
 	}
 
 	/**Setter for assignedTo
 	 * 
 	 * @param assignedTo the list of strings representing the people who the requirement is assigned to.
 	 */
 	public void setAssignedTo(List<String> assignedTo) {
 		this.assignedTo = assignedTo;
 	}
 
 	/**Sets a flag in the requirement to indicate it's deleted */
 	public void remove() {
 		this.activeStatus = false;
 	}
 	
 	@Override
 	public void save() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void delete() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	/**This returns a Json encoded String representation of this requirement object.
 	 * 
 	 * @return a Json encoded String representation of this requirement
 	 * 
 	 */
 	public String toJSON() {
 		return new Gson().toJson(this, Requirement.class);
 	}
 	
 	/**
 	 * Returns an array of Requirements parsed from the given JSON-encoded
 	 * string.
 	 * 
 	 * @param a string containing a JSON-encoded array of Requirement
 	 * @return an array of Requirement deserialzied from the given JSON string
 	 */
 	public static Requirement[] fromJsonArray(String json) {
 		final Gson parser = new Gson();
 		return parser.fromJson(json, Requirement[].class);
 	}
 
 	@Override
 	public Boolean identify(Object o) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public String toString() {
 		return this.getName();
 	}
 
 	/**
 	 * Returns whether the requirement has been deleted.
 	 * @return delete status of the requirement.
 	 */
 	public boolean isDeleted() {
 		return !activeStatus;
 	}
 	
 	/** The getter for Transaction History
 	 * 
 	 * @return a TransdactionHistory for this requirement
 	 */
 	public TransactionHistory getHistory() {
 		return history;
 	}
 	
 	/** The Setter for TransactionHistory
 	 * 
 	 * @param history The history to assign to the requirement
 	 */
 	public void setHistory(TransactionHistory history) {
 		this.history = history;
 	}
 
 }
