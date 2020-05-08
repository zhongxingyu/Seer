 package com.sm.mnc.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.Length;
 
 /**
  * Entity implementation class for Entity: Region
  *
  */
 @Entity
@Table(name="REGION", uniqueConstraints = @UniqueConstraint(columnNames = "REGION_NAME"))
 public class Region implements Serializable {
 
 	
 	private Long regionId;
 	private String regionName;
 	private static final long serialVersionUID = 1L;
 
 	public Region() {
 		super();
 	}
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	@Column(name="REGION_ID")
 	public Long getRegionId() {
 		return this.regionId;
 	}
 
 	public void setRegionId(Long regionId) {
 		this.regionId = regionId;
 	}   
 	
 	@NotNull(message = "Region name can't be blank.")
 	@Length(min=3, max=25, message = "Region name should have at least 2 or maximum 25 characters.")
 	@Column(name="REGION_NAME")	
 	public String getRegionName() {
 		return this.regionName;
 	}
 
 	public void setRegionName(String regionName) {
 		this.regionName = regionName;
 	}
    
 }
