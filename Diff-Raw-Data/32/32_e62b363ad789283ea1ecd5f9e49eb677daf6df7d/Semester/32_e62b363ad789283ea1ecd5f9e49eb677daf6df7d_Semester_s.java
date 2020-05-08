 package com.pedEdt.frontEnd.client.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

 import name.pehl.piriti.xml.client.XmlReader;
 import name.pehl.piriti.xml.client.XmlWriter;
 
 import com.google.gwt.core.client.GWT;
 
 public class Semester implements java.io.Serializable {
 	
 	public interface SemesterReader extends XmlReader<Semester> {}
 	public static final SemesterReader fromXML = GWT.create(SemesterReader.class);
 	
 	public interface SemesterWriter extends XmlWriter<Semester> {}
 	public static final SemesterWriter toXML = GWT.create(SemesterWriter.class);
 
 	private int id;
 	
 	private int year;
 	
 	private int number;
 	
	private Date startDate;
 	
	private Date endDate;
 
 	private List<TeachingUnit> teachingUnits;
 
 
 
 	public Semester() {
 
 	}
 
 	public Semester(int year, int number, Date startD, Date endD, List<TeachingUnit> teachingUnits) {
 		this.year = year;
 		this.number = number;
 		this.startDate = startD;
 		this.endDate = endD;
 		this.teachingUnits = teachingUnits;
 	}
 	
 	
 
	@XmlElement
 	public int getId() {
 		return this.id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 
	@XmlElement
 	public int getYear() {
 		return this.year;
 	}
 
 	public void setYear(int year) {
 		this.year = year;
 	}
 
	@XmlElement
 	public int getNumber() {
 		return this.number;
 	}
 
 	public void setNumber(int number) {
 		this.number = number;
 	}
	
	@XmlElement
 	public Date getStartDate() {
 		return this.startDate;
 	}
 
 	public void setStartDate(Date date) {
 		this.startDate = date;
 	}
	
	@XmlElement
 	public Date getEndDate() {
 		return this.endDate;
 	}
 
 	public void setEndDate(Date date) {
 		this.endDate = date;
 	}
 
	@XmlTransient
 	public List<TeachingUnit> getTeachingUnits() {
 		return this.teachingUnits;
 	}
 
 	public void setTeachingUnits(List<TeachingUnit> teachingUnits) {
 		this.teachingUnits = teachingUnits;
 	}
 	
 	public void addTeachingUnit(TeachingUnit teachingUnit) {
 		this.teachingUnits.add(teachingUnit);
 	}
 	
 	public void removeTeachingUnit(TeachingUnit teachingUnit) {
 		this.teachingUnits.remove(teachingUnit);
 	}
 	
 	public List<Teaching> isSessionInPeriod(Date start) {
 		
 		List<Teaching> myList = new ArrayList<Teaching>();
 		
 		for (int i = 0; i < this.teachingUnits.size(); i++) {
 			myList.addAll(this.teachingUnits.get(i).isSessionInPeriod(start));
 		}
 		
 		return myList;
 	}
 }
