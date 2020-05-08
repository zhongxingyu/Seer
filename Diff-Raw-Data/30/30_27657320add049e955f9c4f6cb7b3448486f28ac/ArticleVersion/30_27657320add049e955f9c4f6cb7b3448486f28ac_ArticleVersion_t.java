 package com.marshmallowswisdom.liber.domain;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="article_version")
 public class ArticleVersion {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private int id;
 	@Column
 	private String content;
 	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
 	@JoinColumn(name="article_id", referencedColumnName="id")
 	private Article article;
 	@OneToMany( mappedBy = "articleVersion", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
 	private Set<FieldValue> fieldValues;
 	@ManyToMany(mappedBy="articles", cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
 	private Set<Tag> tags;
 	
 	@SuppressWarnings("unused")
 	private ArticleVersion() { /* for JPA */ }
 	
 	public ArticleVersion( final Article article, final String content ) {
 		this.article = article;
 		this.content = content;
 		this.tags = new HashSet<Tag>();
 	}
 	
 	public ArticleVersion( final Article article, final String content, final Set<Tag> tags ) {
 		this.article = article;
 		this.content = content;
 		this.tags = tags;
 		for( Tag tag : tags ) {
 			tag.addArticle( this );
 		}
 	}
 	
 	public ArticleVersion( final Article article, 
 							final String content, 
 							final Set<FieldValue> fieldValues, 
 							final Set<Tag> tags ) {
 		this.article = article;
 		this.content = content;
 		this.fieldValues = fieldValues;
 		for( FieldValue fieldValue : fieldValues ) {
 			fieldValue.setArticleVersion( this );
 		}
 		this.tags = tags;
		for( Tag tag : tags ) {
			tag.addArticle( this );
		}
 	}
 	
 	public Article getArticle() {
 		return article;
 	}
 	
 	public String getArticleName() {
 		return article.getName();
 	}
 	
 	public void setArticleName( final String name ) {
 		article.setName( name );
 	}
 	
 	public String getContent() {
 		return content;
 	}
 	
 	public void setContent( final String content ) {
 		this.content = content;
 	}
 	
 	public Set<Tag> getTags() {
 		return tags;
 	}
 	
 	public void removeTags() {
 		tags = new HashSet<Tag>();
 	}
 
	public Set<FieldValue> getFieldValues() {
		return fieldValues;
	}

 }
