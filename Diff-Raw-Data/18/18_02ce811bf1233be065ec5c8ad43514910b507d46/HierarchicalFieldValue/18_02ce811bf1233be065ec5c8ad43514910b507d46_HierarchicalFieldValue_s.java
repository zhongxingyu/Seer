 package com.marshmallowswisdom.liber.domain;
 
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 @Entity
 @DiscriminatorValue( "hierarchical" )
 public class HierarchicalFieldValue extends FieldValue {
 	
 	private static final String PATH_DIVIDER = "/";
 	
 	@ManyToOne
 	@JoinColumn(name="parent_field_value_id", referencedColumnName="id")
 	private HierarchicalFieldValue parent;
 	@Column
 	private String path;
 	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
 	private Set<HierarchicalFieldValue> childValues;
 	
	/* for JPA */
	private HierarchicalFieldValue() {
		super( null );
 	}
 	
 	public HierarchicalFieldValue( final String value, final HierarchicalFieldValue parent ) {
 		super( value );
 		this.parent = parent;
 		updatePath();
 	}
 	
 	public HierarchicalFieldValue getParent() {
 		return parent;
 	}
 	
 	public String getPath() {
 		return path;
 	}
 	
 	public Set<HierarchicalFieldValue> getChildren() {
 		return childValues;
 	}
 	
 	private void updatePath() {
 		path = "";
 		if( parent != null ) {
 			path += parent.getPath();
 		}
 		path += PATH_DIVIDER + getValue();
 	}
 	
 }
