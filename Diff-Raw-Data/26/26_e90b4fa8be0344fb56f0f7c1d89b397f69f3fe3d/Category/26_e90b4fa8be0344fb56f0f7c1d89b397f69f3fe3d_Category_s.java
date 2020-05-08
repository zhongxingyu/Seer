 package com.wesandrachel.model.domain;
 
 import java.io.Serializable;
import java.util.List;
 
 import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity @Table(name="FOOSBALL_CATEGORIES")
 @Cacheable @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class Category implements Serializable {
 
 	private static final long serialVersionUID = -9040449188113301739L;
 	
 	private int id;
 	private String name;
 	
 	@Id @GeneratedValue
 	@Column(name="CATEGORY_ID", nullable=false)
 	public int getId() {
 		return id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	@Column(name="CATEGORY_NAME", nullable=false)
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 }
