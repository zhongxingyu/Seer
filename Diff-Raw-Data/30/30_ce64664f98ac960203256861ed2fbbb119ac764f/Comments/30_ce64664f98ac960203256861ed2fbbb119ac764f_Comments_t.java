 package org.amm.ams.domain;
 
 import java.io.Serializable;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 @Entity
 public class Comments implements Serializable, Identifiable {
 
 	private static final long serialVersionUID = -5913343948444253930L;
 
 	@Id
 	@GeneratedValue
	@Column(name = "id")
 	private Long id;
 
	@Column(name = "parent")
	private Long parent;
	
	@Column(name = "text")
 	private String text;
 
 	@ManyToOne
 	private Articles article;
 
 	protected Comments() {
 	}
 
	public Comments(Long id, String text, Articles article, Long parent) {
 		this.id = id;
 		this.text = text;
 		this.article = article;
		this.parent = parent;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public Articles getArticle() {
 		return article;
 	}
 
 	public void setArticle(Articles article) {
 		this.article = article;
 	}
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 37).append(id).append(text)
				.append(article).append(parent).toHashCode();
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
 
 		Comments rhs = (Comments) obj;
 		return new EqualsBuilder().append(id, rhs.id).append(text, rhs.text)
				.append(article, rhs.article).append(parent, rhs.parent).isEquals();
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("id", id).append("text", text)
				.append("article", article).append("parent", parent).toString();
 	}
 
 }
