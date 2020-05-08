 package org.netvogue.server.neo4japi.domain;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.neo4j.graphdb.Direction;
 import org.springframework.data.neo4j.annotation.Fetch;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 import org.springframework.data.neo4j.support.index.IndexType;
 
 @NodeEntity
 public class Linesheet {
 	@GraphId
 	Long nodeId;
 	
 	@Indexed(indexName="linesheetid", unique = true)
 	String linesheetid;
 	
 	@Indexed(indexName="linesheetname", indexType=IndexType.FULLTEXT)
 	String linesheetname;
 	
 	String profilePicLink;
 	
 	Date deliveryDate = new Date();
 	Date createdDate  = new Date();
 	
 	@RelatedTo(type="Linesheet_Category", direction=Direction.INCOMING)
 	@Fetch Category productcategory;
 	
 	@RelatedTo(type="LINESHEET", direction=Direction.INCOMING)
 	User	createdBy;
 	
 	@RelatedTo(type="LS_STYLE", direction=Direction.OUTGOING)
 	@Fetch Set<Style>	styles =  new HashSet<Style>();
 	
 	public Linesheet() {
 		
 	}
 	
 	public Linesheet(String name, User createdByTemp, Date deliverydate) {
 		linesheetname 	= name;
 		createdBy		= createdByTemp;
 		deliveryDate	= deliverydate;
 		linesheetid 	= UUID.randomUUID().toString();
 		profilePicLink 	= "http://placehold.it/220X320";
 	}
 
 	public Long getNodeId() {
 		return nodeId;
 	}
 
 	public void setNodeId(Long nodeId) {
 		this.nodeId = nodeId;
 	}
 	
 	public String getLinesheetid() {
 		return linesheetid;
 	}
 
 	public void setLinesheetid(String linesheetid) {
 		this.linesheetid = linesheetid;
 	}
 
 	public String getLinesheetname() {
 		return linesheetname;
 	}
 
 	public void setLinesheetname(String linesheetname) {
 		this.linesheetname = linesheetname;
 	}
 
 	public Date getDeliveryDate() {
 		return deliveryDate;
 	}
 
 	public void setDeliveryDate(Date deliveryDate) {
 		this.deliveryDate = deliveryDate;
 	}
 
 	public void setStyles(Set<Style> styles) {
 		this.styles = styles;
 	}
 
 	public String getProfilePicLink() {
 		return profilePicLink;
 	}
 
 	public void setProfilePicLink(String profilePicLink) {
 		this.profilePicLink = profilePicLink;
 	}
 
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 
 	public Category getProductcategory() {
 		return productcategory;
 	}
 
 	public void setProductcategory(Category productcategory) {
 		this.productcategory = productcategory;
 	}
 
 	public User getCreatedBy() {
 		return createdBy;
 	}
 
 	public void setCreatedBy(User createdBy) {
 		this.createdBy = createdBy;
 	}
 
 	public Set<Style> getStyles() {
 		return styles;
 	}
 	
 	public void addStyles(Style newStyle) {
 		if(0 == styles.size()) {
			setProfilePicLink(newStyle.getProfilePicLink());
 		}
 		styles.add(newStyle);
 	}
 	
 	@Override
     public boolean equals(Object other) {
 		if (this == other) 
 			return true;
 		if (nodeId == null) 
 			return false;
 		if (! (other instanceof User)) 
 			return false;
 		return nodeId.equals(((User) other).nodeId);    
 	}
 
     @Override
     public int hashCode() {
     	return nodeId == null ? System.identityHashCode(this) : nodeId.hashCode();
     }
 }
