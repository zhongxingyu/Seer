 package com.kdcloud.server.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.jdo.annotations.Extension;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.kdcloud.weka.core.Attribute;
 
 @PersistenceCapable
 public class Workflow implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
 	private String encodedKey;
 
 	@Persistent
     @Extension(vendorName="datanucleus", key="gae.pk-id", value="true")
 	Long id;
 	
 	String name;
 	
 	String description;
 	
 	@Persistent(serialized="true")
 	ArrayList<Attribute> inputSpec = new ArrayList<Attribute>();
 	
	@Persistent(serialized="true")
 	Serializable executionData;
 
 	public String getEncodedKey() {
 		return encodedKey;
 	}
 
 	public void setEncodedKey(String encodedKey) {
 		this.encodedKey = encodedKey;
 	}
 
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
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public ArrayList<Attribute> getInputSpec() {
 		return inputSpec;
 	}
 
 	public void setInputSpec(ArrayList<Attribute> inputSpec) {
 		this.inputSpec = inputSpec;
 	}
 
 	public Serializable getExecutionData() {
 		return executionData;
 	}
 
 	public void setExecutionData(Serializable executionData) {
 		this.executionData = executionData;
 	}
 	
 	
 
 }
