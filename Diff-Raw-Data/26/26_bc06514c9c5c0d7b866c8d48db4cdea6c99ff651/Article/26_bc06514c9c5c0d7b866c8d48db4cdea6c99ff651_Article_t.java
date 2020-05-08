 package org.amm.ams.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 @Entity
 public class Article implements Serializable, Identifiable {
 
 	private static final long serialVersionUID = -816296721109914665L;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "article_id")
 	private Long id;
 
 	private Boolean is_tmp;
 	private Date creationDate;
 	private Date publishDate;
 
 	@OneToMany(mappedBy = "article")
 	private Set<ArticleDef> articlesDef = new HashSet<ArticleDef>();
 
 	@ManyToMany(cascade = { CascadeType.ALL })
 	@JoinTable(name = "Articles_Users", joinColumns = { @JoinColumn(name = "article_id") }, inverseJoinColumns = { @JoinColumn(name = "user_id") })
 	private Set<User> users = new HashSet<User>();
 
 	@ManyToMany(cascade = { CascadeType.ALL })
 	@JoinTable(name = "Articles_Tags", joinColumns = { @JoinColumn(name = "article_id") }, inverseJoinColumns = { @JoinColumn(name = "tag_id") })
 	private Set<Tag> tags = new HashSet<Tag>();
 
 	@ManyToMany(cascade = { CascadeType.ALL })
 	@JoinTable(name = "Articles_Categories", joinColumns = { @JoinColumn(name = "article_id") }, inverseJoinColumns = { @JoinColumn(name = "category_id") })
 	private Set<Category> categories = new HashSet<Category>();
 
 	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "articles")
 	private Set<Bookmark> bookmarks = new HashSet<Bookmark>();
 
 	protected Article() {
 	}
 
 	public Article(Long id, Boolean is_tmp, Date creationDate,
			Date publishDate) {
 		super();
 		this.id = id;
 		this.is_tmp = is_tmp;
 		this.creationDate = creationDate;
 		this.publishDate = publishDate;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Boolean getIs_tmp() {
 		return is_tmp;
 	}
 
 	public void setIs_tmp(Boolean is_tmp) {
 		this.is_tmp = is_tmp;
 	}
 
 	public Set<User> getUsers() {
 		return users;
 	}
 
 	public void setUsers(Set<User> users) {
 		this.users = users;
 	}
 
 	public Set<Tag> getTags() {
 		return tags;
 	}
 
 	public void setTags(Set<Tag> tags) {
 		this.tags = tags;
 	}
 
 	public Set<Category> getCategories() {
 		return categories;
 	}
 
 	public void setCategories(Set<Category> categories) {
 		this.categories = categories;
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public Date getPublishDate() {
 		return publishDate;
 	}
 
 	public void setPublishDate(Date publishDate) {
 		this.publishDate = publishDate;
 	}
 
 	public Set<Bookmark> getBookmarks() {
 		return bookmarks;
 	}
 
 	public void setBookmarks(Set<Bookmark> bookmarks) {
 		this.bookmarks = bookmarks;
 	}
 
 	public Set<ArticleDef> getArticlesDef() {
 		return articlesDef;
 	}
 
 	public void setArticlesDef(Set<ArticleDef> articlesDef) {
 		this.articlesDef = articlesDef;
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 37).append(id).append(is_tmp)
				.append(creationDate).append(publishDate)
 				.toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (obj == this) {
 			return true;
 		}
 		if (obj.getClass() != getClass()) {
 			return false;
 		}
 
 		Article rhs = (Article) obj;
 		return new EqualsBuilder().append(id, rhs.id)
				.append(is_tmp, rhs.is_tmp)
 				.append(creationDate, rhs.creationDate)
 				.append(publishDate, rhs.publishDate)
				.isEquals();
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("id", id)
				.append("is_tmp", is_tmp)
 				.append("creationDate", creationDate)
 				.append("publishDate", publishDate)
				.toString();
 	}
 }
