 package com.pocketcookies.pepco.model;
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 
 @Entity
 @Table(name="OUTAGEAREA")
 public class OutageArea {
 	// The zip code(s) that represent this area. There may be more than one zip
 	// code because Pepco sometimes combines them.
 	private String id;
 
 	private SortedSet<OutageAreaRevision> revisions;
 
 	public OutageArea(final String id) {
 		setId(id);
 		setRevisions(new TreeSet<OutageAreaRevision>());
 	}
 
 	public OutageArea() {
 	}
 
         @Id
        @GeneratedValue
 	public String getId() {
 		return id;
 	}
 
 	private void setId(String id) {
 		this.id = id;
 	}
 
         @OneToMany(targetEntity=OutageAreaRevision.class,mappedBy="area")
         @Sort(type=SortType.NATURAL)
 	public SortedSet<OutageAreaRevision> getRevisions() {
 		return revisions;
 	}
 
 	private void setRevisions(SortedSet<OutageAreaRevision> set) {
 		this.revisions = set;
 	}
 }
