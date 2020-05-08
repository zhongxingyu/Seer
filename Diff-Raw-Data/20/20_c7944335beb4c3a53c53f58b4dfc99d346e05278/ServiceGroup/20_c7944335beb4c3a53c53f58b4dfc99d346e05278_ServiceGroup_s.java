 package edu.wustl.cab2b.common.queryengine;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 public class ServiceGroup implements Serializable {
 
 	private static final long serialVersionUID = -897722669252090017L;
 	private Long id;
 	private String name;
 	private Cab2bQuery query;
 	private Collection<ServiceGroupItem> items;
 	
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
 	public Collection<ServiceGroupItem> getItems() {
		return items;
 	}
 	public void setItems(Collection<ServiceGroupItem> items) {
 		this.items = items;
 	}
 	public Cab2bQuery getQuery() {
 		return query;
 	}
 	public void setQuery(Cab2bQuery query) {
 		this.query = query;
 	}
 
 }
