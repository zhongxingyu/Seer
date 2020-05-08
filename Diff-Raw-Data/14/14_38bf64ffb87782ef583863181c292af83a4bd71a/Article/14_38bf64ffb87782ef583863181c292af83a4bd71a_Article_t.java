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
 import javax.persistence.NamedNativeQueries;
 import javax.persistence.NamedNativeQuery;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 
 import org.amm.ams.dto.ArticleDto;
 import org.amm.ams.utils.json.JsonDateSerializer;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 //@NamedQueries(
 //		{ @NamedQuery(
 //			name = "findAllArticlesForLang",
 //			query = "SELECT article.article_id as id, article.creationDate as creationDate, article.publishDate as publishDate,article.is_tmp as is_tmp,articledef.title as title,articledef.body as body,articledef.articledefId as articledefId,language.langPrefix as langPrefix FROM article LEFT JOIN articledef ON article.article_id = articledef.article_id LEFT JOIN language ON articledef.language_id = language.language_id WHERE language.prefix =  :prefix"
 //		)
 //})
//@NamedNativeQueries({ 
//	@NamedNativeQuery(
//			name = "findAllArticlesByLang", 
//			query = "SELECT article.article_id as id, article.creationDate as creationDate, article.publishDate as publishDate, article.is_tmp as is_tmp, articledef.title as title, articledef.body as body, articledef.articledef_id as articledefId, language.prefix as langPrefix FROM article as article LEFT JOIN articledef as articledef ON article.article_id = articledef.article_id LEFT JOIN language as language ON articledef.language_id = language.language_id WHERE language.prefix = :prefix",
//			resultClass = ArticleDto.class
//	) 
//})
 @Entity
 public class Article implements Serializable, Identifiable {
 
 	private static final long serialVersionUID = -816296721109914665L;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "article_id")
 	private Long id;
 
 	private Boolean is_tmp;
 
 	@JsonSerialize(using = JsonDateSerializer.class)
 	@Column(nullable = false)
 	private Date creationDate;
 
 	@JsonSerialize(using = JsonDateSerializer.class)
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
 
 	public Article(Long id, Boolean is_tmp, Date creationDate, Date publishDate) {
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
 				.append(creationDate).append(publishDate).toHashCode();
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
 				.append(publishDate, rhs.publishDate).isEquals();
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("id", id)
 				.append("is_tmp", is_tmp).append("creationDate", creationDate)
 				.append("publishDate", publishDate).toString();
 	}
 }
