 package com.marshmallowswisdom.liber.domain;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="article")
 public class Article {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private int id;
 	@Column
 	private String name;
 	@OneToMany(mappedBy="article", cascade=CascadeType.ALL)
 	private Set<ArticleVersion> versions;
 	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
 	@JoinColumn(name="latest_version")
 	private ArticleVersion latestVersion;
 	
 	@SuppressWarnings("unused")
 	private Article() { /* for JPA */ }
 	
 	public Article( final String name ) {
 		this.name = name;
 		versions = new TreeSet<ArticleVersion>();
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName( final String name ) {
 		this.name = name;
 	}
 	
 	public String getContent() {
 		return latestVersion.getContent();
 	}
 	
 	public void setLatestVersion( final ArticleVersion version ) {
 		latestVersion = version;
 	}
 	
 	public Set<ArticleVersion> getVersions() {
 		return versions;
 	}
 
	public Set<FieldValue> getFieldValues() {
		return latestVersion.getFieldValues();
	}

 }
