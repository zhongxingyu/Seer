 package com.apidump.models;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.eclipse.egit.github.core.Milestone;
 import org.eclipse.egit.github.core.client.RequestException;
 
 import com.apidump.generator.UsersGenerator;
 
 @Entity
 public class Milestones {
 
 	@Id
 	private String url;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date createdAt;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date dueOn;
 
 	private int closedIssues;
 
 	private int number;
 
 	private int openIssues;
 
 	@Lob
 	private String description;
 
 	private String state;
 
 	private String title;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "CREATOR_ID")
 	private Users creator;
 	
 	public Milestones(Milestone m) throws RequestException, IOException {
 		url = m.getUrl();
 		
 		createdAt = m.getCreatedAt();
 		dueOn = m.getDueOn();
 		
 		closedIssues = m.getClosedIssues();
 		number = m.getNumber();
 		openIssues = m.getOpenIssues();
 		
 		description = m.getDescription();
 		state = m.getState();
 		title = m.getTitle();
 		
		if (m.getCreator() != null)
 			creator = UsersGenerator.getInstance().getUsers(m.getCreator().getLogin());
 	}
 	
 	public Milestones() {
 		
 	}
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * @param url the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * @return the createdAt
 	 */
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	/**
 	 * @param createdAt the createdAt to set
 	 */
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	/**
 	 * @return the dueOn
 	 */
 	public Date getDueOn() {
 		return dueOn;
 	}
 
 	/**
 	 * @param dueOn the dueOn to set
 	 */
 	public void setDueOn(Date dueOn) {
 		this.dueOn = dueOn;
 	}
 
 	/**
 	 * @return the closedIssues
 	 */
 	public int getClosedIssues() {
 		return closedIssues;
 	}
 
 	/**
 	 * @param closedIssues the closedIssues to set
 	 */
 	public void setClosedIssues(int closedIssues) {
 		this.closedIssues = closedIssues;
 	}
 
 	/**
 	 * @return the number
 	 */
 	public int getNumber() {
 		return number;
 	}
 
 	/**
 	 * @param number the number to set
 	 */
 	public void setNumber(int number) {
 		this.number = number;
 	}
 
 	/**
 	 * @return the openIssues
 	 */
 	public int getOpenIssues() {
 		return openIssues;
 	}
 
 	/**
 	 * @param openIssues the openIssues to set
 	 */
 	public void setOpenIssues(int openIssues) {
 		this.openIssues = openIssues;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the state
 	 */
 	public String getState() {
 		return state;
 	}
 
 	/**
 	 * @param state the state to set
 	 */
 	public void setState(String state) {
 		this.state = state;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the creator
 	 */
 	public Users getCreator() {
 		return creator;
 	}
 
 	/**
 	 * @param creator the creator to set
 	 */
 	public void setCreator(Users creator) {
 		this.creator = creator;
 	}
 }
