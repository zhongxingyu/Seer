 package org.gesis.rdf;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
 
 import org.gesis.rdfs.Resource;
 
 @Entity
 @Inheritance( strategy = InheritanceType.JOINED )
 public class List extends Resource
 {
 
 	// properties
 
 	// relations
 
	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private Resource first;
 
	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
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
