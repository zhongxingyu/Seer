 package com.ngdb.entities.shop;
 
import static javax.persistence.FetchType.LAZY;

 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.user.User;
 
 @Entity
 public class Wish {
 
 	@EmbeddedId
 	private WishId id;
 
	@ManyToOne(fetch = LAZY)
 	@JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
 	private User wisher;
 
	@ManyToOne(fetch = LAZY)
 	@JoinColumn(name = "article_id", insertable = false, updatable = false, nullable = false)
 	private Article article;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date creationDate;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date modificationDate;
 
 	public Wish() {
 		creationDate = modificationDate = new Date();
 	}
 
 	public Wish(User wisher, Article article) {
 		this();
 		this.wisher = wisher;
 		this.article = article;
 		this.id = new WishId(wisher.getId(), article.getId());
 	}
 
 	@PreUpdate
 	public void preUpdate() {
 		this.modificationDate = new Date();
 	}
 
 	public Article getArticle() {
 		return article;
 	}
 
 	public User getWisher() {
 		return wisher;
 	}
 
 	public String getTitle() {
 		return article.getTitle();
 	}
 
 	public String getUser() {
 		return wisher.getLogin();
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public Date getModificationDate() {
 		return modificationDate;
 	}
 
 	@Embeddable
 	public static class WishId implements Serializable {
 
 		@Column(name = "user_id", nullable = false, updatable = false)
 		private Long userId;
 
 		@Column(name = "article_id", nullable = false, updatable = false)
 		private Long articleId;
 
 		public WishId() {
 		}
 
 		public WishId(Long userId, Long articleId) {
 			this.userId = userId;
 			this.articleId = articleId;
 		}
 
 		public boolean equals(Object o) {
 			if (o == null)
 				return false;
 
 			if (!(o instanceof WishId))
 				return false;
 
 			WishId other = (WishId) o;
 			if (!(other.articleId.equals(articleId)))
 				return false;
 
 			if (!(other.userId.equals(userId)))
 				return false;
 
 			return true;
 		}
 	}
 
 }
