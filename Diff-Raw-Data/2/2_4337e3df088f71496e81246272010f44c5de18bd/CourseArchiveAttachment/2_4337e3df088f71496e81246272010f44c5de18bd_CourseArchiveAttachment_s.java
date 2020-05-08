 package org.sakaiproject.coursearchive.model;
 
 import java.util.Date;
 
 public class CourseArchiveAttachment {
 	private Long id;
 	private String name;
 	private String type;
 	private String resourceId;
 	private String resourceURL;
 
 	private CourseArchiveSyllabus syllabus;
 
 	/**
 	 * Default constructor
 	 */
 	public CourseArchiveAttachment() {
 		this(null);
 	}
 
 	public CourseArchiveAttachment(CourseArchiveSyllabus syllabus) {
 		this(syllabus, null, null, null, null);
 	}
 
 	/**
 	 * Full constructor
 	 */
	public CourseArchiveAttachment(CourseArchiveSyllabus item, String name, String type, String resourceId, String resourceURL) {
 		this.syllabus    = syllabus;
 		this.name        = name;
 		this.type        = type;
 		this.resourceId  = resourceId;
 		this.resourceURL = resourceURL;
 	}
 
 	/**
 	 * Getters and Setters
 	 */
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
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public String getResourceId() {
 		return resourceId;
 	}
 	public void setResourceId(String resourceId) {
 		this.resourceId = resourceId;
 	}
 	public String getResourceURL() {
 		return resourceURL;
 	}
 	public void setResourceURL(String resourceURL) {
 		this.resourceURL = resourceURL;
 	}
 	public CourseArchiveSyllabus getSyllabus() {
 		return syllabus;
 	}
 	public void setSyllabus(CourseArchiveSyllabus syllabus) {
 		this.syllabus = syllabus;
 	}
 }
