 package dk.aersoe.jobfinder.model;
 
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Class containing a single Job Entry
  * @author Morten morten@aersoe.dk
  *
  */
 public class JobEntry {
 
 	private int id = 0;
 	private Date creationDate = null;
 	private Date modifyDate = null;
 	private Date deletedDate = null;
 	private Date foreignDate = null;
 	private int status = 0;
 	private Date deadline = null;
 	private String category = "";
 	private String title = "";
 	private String company = "";
 	private String description = "";
 	private String url = "";
 	private String source = "";
 
 	/**
 	 * Constructor for the JobEntry Class 
 	 * Init of variables
 	 * status is set to 0
 	 * creationDate is set to now
 	 * modifyDate is set to now
 	 */
 	public JobEntry(){
 		Calendar cal = Calendar.getInstance();
 		creationDate = cal.getTime();
 		modifyDate = cal.getTime();
 		deletedDate = cal.getTime();
 		foreignDate = cal.getTime();
		deadline = cal.getTime();
 	}
 	
 	/**
 	 * getter for the id of the entry
 	 * @return the id of the entry
 	 */
 	public int getId(){
 		return id;
 	}
 	
 	/**
 	 * Sets the identifier of the JobEntry object
 	 * @param identifier the id of the JobEntry
 	 */
 	public void setId(int identifier){
 		id = identifier;
 	}
 	
 	/**
 	 * getter for the creation date of the entry
 	 * @return Date containing the creation date of the entry 
 	 */
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	/**
 	 * Set the creation date of the entry
 	 * @param creationDate Date containing the creation date
 	 */
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	/**
 	 * getter for the modification date of the entry
 	 * @return Date containing the modification date of the entry 
 	 */
 	public Date getModifyDate() {
 		return modifyDate;
 	}
 
 	/**
 	 * Set the modification date of the entry
 	 * @param modifyDate Date containing the creation date
 	 */
 	public void setModifyDate(Date modifyDate) {
 		this.modifyDate = modifyDate;
 	}
 
 	/**
 	 * getter for the deletion date of the entry
 	 * @return Date containing the deletion date of the entry 
 	 */
 	public Date getDeletedDate() {
 		return deletedDate;
 	}
 
 	/**
 	 * Set the deletion date of the entry
 	 * @param deletedDate Date containing the creation date
 	 */
 	public void setDeletedDate(Date deletedDate) {
 		this.deletedDate = deletedDate;
 	}
 
 	/**
 	 *  getter for the status of the entry
 	 * @return int containing the status of the entry
 	 */
 	public int getStatus() {
 		return status;
 	}
 
 	/**
 	 * Set the status of the entry
 	 * valid values:
 	 * 0: unknown
 	 * 1: active
 	 * 2: overdue
 	 * 999: deleted
 	 * @param status the status of the entry
 	 */
 	public void setStatus(int status) {
 		this.status = status;
 	}
 	
 	// TODO: Add the doc for the following methods
 	public Date getForeignDate() {
 		return foreignDate;
 	}
 
 	public void setForeignDate(Date foreignDate) {
 		this.foreignDate = foreignDate;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	// --- Untill here ---
 	
 	/**
 	 * getter for the company name of the entry
 	 * @return String containing the company name
 	 */
 	public String getCompany() {
 		return company;
 	}
 
 	/**
 	 * Set the compaany name of the entry
 	 * @param company String containing the company name
 	 */
 	public void setCompany(String company) {
 		this.company = company;
 	}
 
 	/**
 	 * getter for the description of the entry
 	 * @return String containing the entry description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Set the description of the entry
 	 * @param description String containing the description
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * getter for the url that points to the original jobentry
 	 * This may be empty if the url can notbe determined
 	 * @return String containing the url of the original entry 
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * getter for the url that points to the original jobentry
 	 * this may be null if the url cxan not be determined
 	 * @return URL that points to the original entry
 	 */
 	public URL getUrlAsUrl() {
 		URL result = null;
 		try{
 			result = new URL(getUrl());
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/**
 	 * Set the url of the original jobentry
 	 * @param url String that contains the url of the original jobentry
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * getter for the source from which the JobEntry is generated
 	 * This will typically be based on a RSS feed url
 	 * @return String containing the source of the job query
 	 */
 	public String getSource() {
 		return source;
 	}
 
 	/**
 	 * Set the source from which the JobEntry is generated
 	 * @param source String containing the source of the job query
 	 */
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	/**
 	 * getter for the date on which the entry expires
 	 * This may be null if the expire date is unknown
 	 * @return Date containing the expire date of the entry 
 	 */
 	public Date getDeadline() {
 		return deadline;
 	}
 	
 	/**
 	 * Set the deadline on which the JobEntry expires
 	 * @param deadline Date containing the expire date of the original job entry
 	 */
 	public void setDeadline(Date deadline) {
 		this.deadline = deadline;
 	}
 	
 	/**
 	 * 
 	 */
 	public String toString(){
 		String result = getCategory() + ", " + getCompany() + ", "+ getTitle();
 		return result;
 	}
 }
