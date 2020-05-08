package com.sm.mnc.model;
 
 import java.io.Serializable;
 import java.lang.Long;
 import java.lang.String;
 import javax.persistence.*;
 
 /**
  * Entity implementation class for Entity: Region
  *
  */
 @Entity
 @Table(name="REGION")
 
 public class Region implements Serializable {
 
 	
 	private Long regionId;
 	private String regionName;
 	private static final long serialVersionUID = 1L;
 
 	public Region() {
 		super();
 	}
 	
 	@Id
 	@Column(name="REGION_ID")
 	public Long getRegionId() {
 		return this.regionId;
 	}
 
 	public void setRegionId(Long regionId) {
 		this.regionId = regionId;
 	}   
 	
 	@Column(name="REGION_NAME")
 	public String getRegionName() {
 		return this.regionName;
 	}
 
 	public void setRegionName(String regionName) {
 		this.regionName = regionName;
 	}
    
 }
