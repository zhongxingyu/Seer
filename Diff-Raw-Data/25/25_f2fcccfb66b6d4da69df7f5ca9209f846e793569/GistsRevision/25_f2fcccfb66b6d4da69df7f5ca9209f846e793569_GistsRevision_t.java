 package com.apidump.models;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.eclipse.egit.github.core.GistChangeStatus;
 import org.eclipse.egit.github.core.GistRevision;
 import org.eclipse.egit.github.core.client.RequestException;

import com.apidump.generator.UsersGenerator;
 
 @Entity
 public class GistsRevision {
 
 	@Id
 	private String version;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date committedAt;
 
 	private String url;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "USER_ID")
 	private Users user;
 	
 	// Collapsed GistChangeStatus into GistsRevision
 	private int additions;
 
 	private int deletions;
 
 	private int total;
 	
 	public GistsRevision(GistRevision g) throws IOException, RequestException {
 		version = g.getVersion();
 		committedAt = g.getCommittedAt();
 		url = g.getUrl();
 		
 		if (g.getUser() != null)
			user = UsersGenerator.getInstance().getUsers(g.getUser().getLogin());
 		
 		GistChangeStatus status = g.getChangeStatus();
 		if (status != null) {
 			additions = status.getAdditions();
 			deletions = status.getDeletions();
 			total = status.getTotal();
 		}
 	}
 	
 	public GistsRevision() {
 		
 	}
 
 	/**
 	 * @return the version
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * @param version the version to set
 	 */
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	/**
 	 * @return the committedAt
 	 */
 	public Date getCommittedAt() {
 		return committedAt;
 	}
 
 	/**
 	 * @param committedAt the committedAt to set
 	 */
 	public void setCommittedAt(Date committedAt) {
 		this.committedAt = committedAt;
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
 	 * @return the user
 	 */
 	public Users getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user the user to set
 	 */
 	public void setUser(Users user) {
 		this.user = user;
 	}
 
 	/**
 	 * @return the additions
 	 */
 	public int getAdditions() {
 		return additions;
 	}
 
 	/**
 	 * @param additions the additions to set
 	 */
 	public void setAdditions(int additions) {
 		this.additions = additions;
 	}
 
 	/**
 	 * @return the deletions
 	 */
 	public int getDeletions() {
 		return deletions;
 	}
 
 	/**
 	 * @param deletions the deletions to set
 	 */
 	public void setDeletions(int deletions) {
 		this.deletions = deletions;
 	}
 
 	/**
 	 * @return the total
 	 */
 	public int getTotal() {
 		return total;
 	}
 
 	/**
 	 * @param total the total to set
 	 */
 	public void setTotal(int total) {
 		this.total = total;
 	}
 }
