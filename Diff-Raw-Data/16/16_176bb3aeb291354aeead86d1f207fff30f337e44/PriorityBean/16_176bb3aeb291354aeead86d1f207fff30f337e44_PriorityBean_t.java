 package com.ocmc.ws.beans;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlType;
 
 @XmlType(name="PriorityType", namespace="com.ocmc.ws.beans",
 		propOrder={"priorityID", "priorityName", "priorityDesc"})
 public class PriorityBean implements Serializable{
 
 	private static final long serialVersionUID = 1L;
 
 	private Long priorityID;
 	private String priorityName;
 	private String priorityDesc;
 	
 	
 	public PriorityBean(){}
 	
 	public PriorityBean(Long priorityID, String priorityName, 
 			String priorityDesc){
 		this.priorityID = priorityID;
 		this.priorityName = priorityName;
 		this.priorityDesc = priorityDesc;
 	}
 				
 	
 	public void setPriorityID(Long priorityID){
 		this.priorityID = priorityID;
 	}
 	
 	public Long getPriorityID(){
 		return this.priorityID;
 	}
 	
 	public void setPriorityName(String priorityName){
 		this.priorityName = priorityName;
 	}
 	
 	public String getPriorityName(){
 		return this.priorityName;
 	}
 	
 	public void setPriorityDesc(String priorityDesc){
 		this.priorityDesc = priorityDesc;
 	}
 	
 	public String getPriorityDesc(){
 		return this.priorityDesc;
 	}
 	
 	
 
 }
