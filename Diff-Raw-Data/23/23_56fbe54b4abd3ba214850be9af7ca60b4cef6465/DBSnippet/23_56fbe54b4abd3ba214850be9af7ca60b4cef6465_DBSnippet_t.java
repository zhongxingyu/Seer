 /**
  * File: DBSnippet.java
  * Date: 03.05.2012
  */
 package org.smartsnip.persistence.hibernate;
 
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.DynamicInsert;
 import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
 import org.hibernate.search.annotations.DocumentId;
 import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
 import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Resolution;
 import org.hibernate.search.annotations.Store;
 /**
  * Database OR mapping class for table Snippet
  * 
  * @author littlelion
  * 
  */
 @Entity
 @Indexed
 @DynamicInsert
 @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="EntityCache")
 @Table(name = "Snippet")
 public class DBSnippet {
 
 	@Id
 	@DocumentId
 	@GeneratedValue
 	@Column(name = "snippet_id", insertable = false, updatable = false)
 	private Long snippetId;
 
 	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
 	@Column(name = "headline", length = 255)
 	private String headline;
 
 	@Lob
 	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
 	@Column(name = "description", columnDefinition="TEXT")
 	private String description;
 
 	@Column(name = "viewcount")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
 	private Integer viewcount;
 
 	@GeneratedValue
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
 	@Column(name = "rating_average", insertable = false, updatable = false)
 	private Float ratingAverage;
 
 	@GeneratedValue
 	@Temporal(TemporalType.TIMESTAMP)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	@DateBridge(resolution=Resolution.MINUTE)
 	@Column(name = "last_edited", insertable = false, updatable = false)
 	private Date lastEdited;
 
 	@Column(name = "user_name", length = 20)
 	private String owner;
 
 	@Column(name = "category_id")
 	private Long categoryId;
 
 	@Column(name = "license_id")
 	private Long licenseId;
 
 	/**
 	 * Entity, POJO class
 	 */
 	DBSnippet() {
 		super();
 	}
 
 	/**
 	 * @return the snippetId
 	 */
 	public Long getSnippetId() {
 		return this.snippetId;
 	}
 
 	/**
 	 * @param snippetId
 	 *            the snippetId to set
 	 */
 	public void setSnippetId(Long snippetId) {
 		this.snippetId = snippetId;
 	}
 
 	/**
 	 * @return the headline
 	 */
 	public String getHeadline() {
 		return this.headline;
 	}
 
 	/**
 	 * @param headline
 	 *            the headline to set
 	 */
 	public void setHeadline(String headline) {
 		this.headline = headline;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return this.description;
 	}
 
 	/**
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the viewcount
 	 */
 	public Integer getViewcount() {
 		return this.viewcount;
 	}
 
 	/**
 	 * @param viewcount
 	 *            the viewcount to set
 	 */
 	public void setViewcount(Integer viewcount) {
 		this.viewcount = viewcount;
 	}
 
 	/**
 	 * @return the ratingAverage
 	 */
 	public Float getRatingAverage() {
 		return this.ratingAverage;
 	}
 
 	/**
 	 * @param ratingAverage
 	 *            the ratingAverage to set
 	 */
 	public void setRatingAverage(Float ratingAverage) {
 		this.ratingAverage = ratingAverage;
 	}
 
 	/**
 	 * @return the lastEdited
 	 */
 	public Date getLastEdited() {
 		return this.lastEdited;
 	}
 
 	/**
 	 * @param lastEdited
 	 *            the lastEdited to set
 	 */
 	public void setLastEdited(Date lastEdited) {
 		this.lastEdited = lastEdited;
 	}
 
 	/**
 	 * @return the owner
 	 */
 	public String getOwner() {
 		return this.owner;
 	}
 
 	/**
 	 * @param owner
 	 *            the owner to set
 	 */
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	/**
 	 * @return the categoryId
 	 */
 	public Long getCategoryId() {
 		return this.categoryId;
 	}
 
 	/**
 	 * @param categoryId
 	 *            the categoryId to set
 	 */
 	public void setCategoryId(Long categoryId) {
 		this.categoryId = categoryId;
 	}
 
 	/**
 	 * @return the licenseId
 	 */
 	public Long getLicenseId() {
 		return this.licenseId;
 	}
 
 	/**
 	 * @param licenseId
 	 *            the licenseId to set
 	 */
 	public void setLicenseId(Long licenseId) {
 		this.licenseId = licenseId;
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((this.snippetId == null) ? 0 : this.snippetId.hashCode());
 		return result;
 	}
 
 	/**
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
 		DBSnippet other = (DBSnippet) obj;
 		if (this.snippetId == null) {
 			if (other.snippetId != null)
 				return false;
 		} else if (!this.snippetId.equals(other.snippetId))
 			return false;
 		return true;
 	}
 }
