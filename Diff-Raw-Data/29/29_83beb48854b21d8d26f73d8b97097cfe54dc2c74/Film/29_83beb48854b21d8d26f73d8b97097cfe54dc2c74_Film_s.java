 package com.alextim.diskarchive.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
import javax.persistence.Transient;
 
 @Entity
 @Table(name="films")
 public class Film {
 	@Id @GeneratedValue(strategy=GenerationType.AUTO)
 	private Long id;
 
 	private String name;
 	private String description;
 	
 	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="group_id")
 	private FilmGroup filmGroup;
 	
 	@OneToOne(mappedBy="film")
 	private Author author;
 	
 	public FilmGroup getFilmGroup() {
 		return filmGroup;
 	}
 	public void setFilmGroup(FilmGroup filmGroup) {
 		this.filmGroup = filmGroup;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getDescription() {
 		return description == null ? "" : description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Author getAuthor() {
 		return author;
 	}
 	public void setAuthor(Author author) {
 		this.author = author;
 	}
 }
