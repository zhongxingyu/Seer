 package com.ngdb.entities.reference;
 
 import static javax.xml.bind.annotation.XmlAccessType.FIELD;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.ngdb.entities.AbstractEntity;
 
 @Entity
 @XmlRootElement(name = "origin")
 @XmlAccessorType(FIELD)
 public class Origin extends AbstractEntity implements Comparable<Origin> {
 
 	@Column(unique = true, nullable = false)
 	private String title;
 
 	public Origin() {
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	@Override
 	public int compareTo(Origin origin) {
		if (title == null || origin == null || origin.title == null) {
			return 0;
		}
 		return title.compareToIgnoreCase(origin.title);
 	}
 
 }
