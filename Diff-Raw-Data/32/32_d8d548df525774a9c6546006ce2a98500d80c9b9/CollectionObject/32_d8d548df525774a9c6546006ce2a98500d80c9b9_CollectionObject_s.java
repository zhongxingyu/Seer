 package com.ngdb.entities.user;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import com.ngdb.entities.article.Article;
 
 @Entity
 public class CollectionObject implements Comparable<CollectionObject> {
 
 	@EmbeddedId
 	private CollectionId id;
 
	@ManyToOne
 	@JoinColumn(name = "user_id", insertable = false, updatable = false)
 	private User owner;
 
	@ManyToOne
 	@JoinColumn(name = "article_id", insertable = false, updatable = false)
 	private Article article;
 
 	public CollectionObject() {
 	}
 
 	public CollectionObject(User owner, Article article) {
 		this.owner = owner;
 		this.article = article;
 		this.id = new CollectionId(owner, article);
 	}
 
 	public User getOwner() {
 		return owner;
 	}
 
 	public Article getArticle() {
 		return article;
 	}
 
 	@Embeddable
 	public static class CollectionId implements Serializable {
 
 		@Column(name = "user_id", nullable = false, updatable = false)
 		private Long userId;
 
 		@Column(name = "article_id", nullable = false, updatable = false)
 		private Long articleId;
 
 		public CollectionId() {
 
 		}
 
 		public CollectionId(User user, Article article) {
 			this.userId = user.getId();
 			this.articleId = article.getId();
 		}
 
 		public boolean equals(Object o) {
 			if (o == null)
 				return false;
 
 			if (!(o instanceof CollectionId))
 				return false;
 
 			CollectionId other = (CollectionId) o;
 			if (!(other.articleId.equals(articleId)))
 				return false;
 
 			if (!(other.userId.equals(userId)))
 				return false;
 
 			return true;
 		}
 	}
 
 	@Override
 	public int compareTo(CollectionObject collectionObject) {
 		return this.getArticle().getTitle().compareTo(collectionObject.getArticle().getTitle());
 	}
 
 }
