 package com.ngdb.entities.user;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.ngdb.entities.article.Article;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 import static javax.persistence.FetchType.LAZY;
 
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class CollectionObject implements Comparable<CollectionObject> {
 
 	@EmbeddedId
 	private CollectionId id;
 
 	@ManyToOne(fetch = LAZY)
 	@JoinColumn(name = "user_id", insertable = false, updatable = false)
 	private User owner;
 
 	@ManyToOne(fetch = LAZY)
 	@JoinColumn(name = "article_id", insertable = false, updatable = false)
 	private Article article;
 
 	public CollectionObject() {
 	}
 
 	public CollectionObject(User owner, Article article) {
 		Preconditions.checkNotNull(owner, "owner is mandatory");
 		Preconditions.checkNotNull(article, "article is mandatory");
 		this.owner = owner;
 		this.article = article;
 		this.id = new CollectionId(owner.getId(), article.getId());
 	}
 
 	public User getOwner() {
 		return owner;
 	}
 
 	public Article getArticle() {
 		return article;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof CollectionObject) {
 			CollectionObject collectionObject = (CollectionObject) o;
 			return owner.equals(collectionObject.owner) && article.equals(collectionObject.article);
 		}
 		return false;
 	}
 
	@Override
	public int hashCode() {
		return Objects.hashCode(owner, article);
	}

 	@Embeddable
 	private static class CollectionId implements Serializable {
 
 		@Column(name = "user_id", nullable = false, updatable = false)
 		private Long userId;
 
 		@Column(name = "article_id", nullable = false, updatable = false)
 		private Long articleId;
 
 		public CollectionId() {
 
 		}
 
 		CollectionId(Long userId, Long articleId) {
 			Preconditions.checkNotNull(userId, "userId is mandatory");
 			Preconditions.checkNotNull(articleId, "articleId is mandatory");
 			this.userId = userId;
 			this.articleId = articleId;
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
 
 		@Override
 		public int hashCode() {
 			return Objects.hashCode(articleId, userId);
 		}
 	}
 
 	@Override
 	public int compareTo(CollectionObject collectionObject) {
 		return this.getArticle().getTitle().compareTo(collectionObject.getArticle().getTitle());
 	}
 
 	@Override
 	public String toString() {
 		return owner.toString() + " " + article.toString();
 	}
 
 }
