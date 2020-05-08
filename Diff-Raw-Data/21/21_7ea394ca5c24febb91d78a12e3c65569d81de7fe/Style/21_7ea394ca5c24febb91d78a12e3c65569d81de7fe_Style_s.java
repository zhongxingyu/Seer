 package org.netvogue.server.neo4japi.domain;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.netvogue.server.neo4japi.common.Constants;
 import org.netvogue.server.neo4japi.common.ProductLineSizes;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.support.index.IndexType;
 
 @NodeEntity
 public class Style {
 
 	@GraphId
 	Long nodeId;
 	
 	@Indexed(indexName="styleid", unique = true)
 	String styleid;
 	
 	@Indexed(indexName="style", indexType=IndexType.FULLTEXT)
 	String stylename;
 	
 	@Indexed(indexName="style", indexType=IndexType.FULLTEXT)
 	String styleno;
 	
 	String profilePicLink;
 	
 	String description;
 	
 	@Indexed(indexName="style", indexType=IndexType.FULLTEXT)
 	String fabrication;
 	
 	@Indexed(indexName="style", indexType=IndexType.FULLTEXT)
 	long price;
 	
 	@Indexed(indexName="style", indexType=IndexType.FULLTEXT)
 	Set<ProductLineSizes> availableSizes = new HashSet<ProductLineSizes>(); //Check, if this can be replaced with StyleSize //Azeez
 	
 	Set<String> availableColors = new HashSet<String>();
 	
 	Set<String> availableImages = new HashSet<String>();
 	
 	Date createdDate = new Date();
 	
 	public Style() {
 		
 	}
 	
 	public Style(String name, String no) {
 		this.stylename = name;
 		this.styleno	= no;
 		this.styleid = UUID.randomUUID().toString();
 	}
 	
 	public String getStyleid() {
 		return styleid;
 	}
 
 	public void setStyleid(String styleid) {
 		this.styleid = styleid;
 	}
 
 	public String getStylename() {
 		return stylename;
 	}
 
 	public void setStylename(String stylename) {
 		this.stylename = stylename;
 	}
 
 	public String getStyleno() {
 		return styleno;
 	}
 
 	public void setStyleno(String styleno) {
 		this.styleno = styleno;
 	}
 
 	public String getProfilePicLink() {
 		return profilePicLink;
 	}
 
 	public void setProfilePicLink(String profilePicLink) {
 		this.profilePicLink = profilePicLink;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getFabrication() {
 		return fabrication;
 	}
 
 	public void setFabrication(String fabrication) {
 		this.fabrication = fabrication;
 	}
 
 	public long getPrice() {
 		return price;
 	}
 
 	public void setPrice(long price) {
 		this.price = price;
 	}
 
 	public Set<ProductLineSizes> getAvailableSizes() {
 		return availableSizes;
 	}
 
 	public void setAvailableSizes(Set<ProductLineSizes> availableSizes) {
 		this.availableSizes = availableSizes;
 	}
 
 	public Set<String> getAvailableColors() {
 		return availableColors;
 	}
 
 	public void setAvailableColors(Set<String> availableColors) {
 		this.availableColors = availableColors;
 	}
 
 	public Set<String> getAvailableImages() {
 		return availableImages;
 	}
 
 	public void setAvailableImages(Set<String> availableImages) {
		this.availableImages = availableImages;
 	}
 
 	public void addImages(String imageId) {
 		int size = availableImages.size();
 		if(0 == size) {
 			this.profilePicLink = imageId;
 		}
 		if(Constants.MAX_IMAGES_IN_STYLE > size) {
 			availableImages.add(imageId);
 		}
 	}
 	
 	public void emptyImages() {
 		availableImages.clear();
 	}
 	
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 
 	public void Copy(Style source) {
 		this.stylename 		= source.getStylename();
 		this.styleno		= source.getStyleno();
 		this.description	= source.getDescription();
 		this.fabrication	= source.getFabrication();
 		this.price			= source.getPrice();
 		this.profilePicLink = source.getProfilePicLink();
 		setAvailableColors(source.getAvailableColors());
 		setAvailableImages(source.getAvailableImages());
 		setAvailableSizes(source.getAvailableSizes());
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
