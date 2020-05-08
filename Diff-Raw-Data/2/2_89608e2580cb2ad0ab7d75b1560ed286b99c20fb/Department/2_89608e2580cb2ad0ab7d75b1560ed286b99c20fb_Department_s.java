 package com.scheduler.models;
 import java.sql.Date;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.List;
 
 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;
 
 @Data
 @NoArgsConstructor
 @AllArgsConstructor
 public class Department {
 	
 	private int departmentId;
 	private int campusId;
 	private String departmentName;
 	private String departmentHod;
	private int contactInfo;
 	private String departmentDescription;
 	private Date dateCreated;
 	private List<OfficialUser> officialUsers = new ArrayList<OfficialUser>();
 
 	private List<Departmenttimeslot> slots;
 	private int timeslotId;
 	private int departmentTimeId;
 	private int capacity;
 	private List<String> days;
 }
