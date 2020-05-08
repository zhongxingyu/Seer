 package com.operativus.senacrs.audit.model;
 
 import java.util.Date;
 
 public class Identification {
 
 	private String course = null;
 	private String unit = null;
 	private String academic = null;
 	private String classDesc = null;
 	private String semester = null;
 	private Date lastDay = null;
 
 	public Identification(final String course, final String unit, final String academic,
 			final String classDesc, final String semester) {
 
 		super();
 
 		this.course = course;
 		this.unit = unit;
 		this.academic = academic;
 		this.classDesc = classDesc;
 		this.semester = semester;
 	}
 
 	public String getCourse() {
 
 		return this.course;
 	}
 
 	public void setCourse(final String course) {
 
 		this.course = course;
 	}
 
 	public String getUnit() {
 
 		return this.unit;
 	}
 
 	public void setUnit(final String unit) {
 
 		this.unit = unit;
 	}
 
 	public String getAcademic() {
 
 		return this.academic;
 	}
 
 	public void setAcademic(final String academic) {
 
 		this.academic = academic;
 	}
 
 	public String getClassDesc() {
 
 		return this.classDesc;
 	}
 
 	public void setClassDesc(final String classDesc) {
 
 		this.classDesc = classDesc;
 	}
 
 	public String getSemester() {
 
 		return this.semester;
 	}
 
 	public void setSemester(final String semester) {
 
 		this.semester = semester;
 	}
 
 	public Date getLastDay() {
 
 		return this.lastDay;
 	}
 
 	public void setLastDay(final Date lastDay) {
 
 		this.lastDay = lastDay;
 	}
 
 	@Override
 	public String toString() {
 		
		return this.academic + ":" + this.unit + ":" + this.course + ":" + this.classDesc + ":" + this.semester + ":" + this.lastDay;
 	}
 }
