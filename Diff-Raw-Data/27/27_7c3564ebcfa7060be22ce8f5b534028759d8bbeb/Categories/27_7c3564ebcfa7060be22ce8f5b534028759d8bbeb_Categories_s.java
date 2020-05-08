 package org.amm.ams.domain;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 @Entity
 public class Categories implements Serializable, Identifiable {
 
 	private static final long serialVersionUID = -3582413995673329122L;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "tags_id")
 	private Long id;
 
 	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
 	private Set<Articles> articles = new HashSet<Articles>();
 
 	protected Categories() {
 	}
 
	public Categories(Long id, Set<Articles> articles) {
 		this.id = id;
 		this.articles = articles;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Set<Articles> getArticles() {
 		return articles;
 	}
 
 	public void setArticles(Set<Articles> articles) {
 		this.articles = articles;
 	}
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 37).append(id).append(articles)
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
 
 		Categories rhs = (Categories) obj;
 		return new EqualsBuilder().append(id, rhs.id)
				.append(articles, rhs.articles).isEquals();
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("id", id)
				.append("articles", articles).toString();
 	}
 
 }
