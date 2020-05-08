 package ptm.client.datamodel;
 
 import java.io.Serializable;
 import java.util.Date;
 
 @SuppressWarnings("serial")
 public class Task implements Serializable,Comparable<Task> {
 
 	//this id is used only in client side
 	private long id;
 	
 	private Date creationDate;
 	private Date checkedDate;
 	private Date dueDate;
 	private String content;
 	private Boolean isDone;
     private Integer alertBefore;
     private Boolean isAlerOn;
 
 	
 	//Constructors
 	public Task(long id, String content) {
 		this.id = id;
 		this.content = content;
 		this.creationDate = new Date();
 		this.isDone = false;
		this.isAlerOn = false;
		this.alertBefore = 2;
 	}
 	
 	//DoNothing constructor (required by serializable)
 	public Task() {
 		
 	}
 	
 
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (id ^ (id >>> 32));
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Task other = (Task) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 	@Override
 	public int compareTo(Task arg0) {
 		if(arg0.isDone != isDone)
 			return isDone.compareTo(arg0.isDone);
 		else
 			if (isDone)
 				return arg0.getCheckedDate().compareTo(getCheckedDate());
 			else
 				//return getCreationDate().compareTo(arg0.getCreationDate());
 				return arg0.getCreationDate().compareTo(getCreationDate());
 	}
 
 	//Getters and Setters
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public Boolean getIsDone() {
 		return isDone;
 	}
 	public void setIsDone(Boolean isDone) {
 		this.isDone = isDone;
 		if(isDone)
 			checkedDate = new Date();
 		else
 			checkedDate = null;
 	}
 	public long getId() {
 		return id;
 	}
 	public Date getCreationDate() {
 		return creationDate;
 	}
 	public Date getCheckedDate() {
 		return checkedDate;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 	public void setCheckedDate(Date checkedDate) {
 		this.checkedDate = checkedDate;
 	}
 	public Date getDueDate() {
 		return dueDate;
 	}
 	public void setDueDate(Date dueDate) {
 		this.dueDate = dueDate;
 	}
 	public int getAlertBefore() {
 		return alertBefore;
 	}
 	public void setAlertBefore(int alertBefore) {
 		this.alertBefore = alertBefore;
 	}
 	public boolean getIsAlerOn() {
 		return isAlerOn;
 	}
 	public void setIsAlerOn(boolean isAlerOn) {
 		this.isAlerOn = isAlerOn;
 	}
 
 	
 }
