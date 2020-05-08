 package org.gesis.rdf;
 
 import javax.persistence.CascadeType;
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
 
 import org.gesis.rdfs.Resource;
 
 @Entity
 @Inheritance( strategy = InheritanceType.JOINED )
 public class List extends Resource
 {
 
 	// properties
 
 	// relations
 
	@Column
 	private Resource first;
 
	@OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
	@JoinTable(
			name = "List_List",
			joinColumns = @JoinColumn( name = "parent_id" ),
			inverseJoinColumns = @JoinColumn( name = "child_id" ) )
 	private List rest;
 
 	// getter / setter
 
 	public Resource getFirst()
 	{
 		return first;
 	}
 
 	public void setFirst( final Resource first )
 	{
 		this.first = first;
 	}
 
 	public List getRest()
 	{
 		return rest;
 	}
 
 	public void setRest( final List rest )
 	{
 		this.rest = rest;
 	}
 
 }
