 package com.pedEdt.frontEnd.client.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import name.pehl.piriti.xml.client.XmlReader;
 import name.pehl.piriti.xml.client.XmlWriter;
 
 import com.google.gwt.core.client.GWT;
 
 public class Teaching implements java.io.Serializable {
 	
 	interface TeachingReader extends XmlReader<Teaching> {}
 	public static final TeachingReader fromXML = GWT.create(TeachingReader.class);
 	
 	public interface TeachingWriter extends XmlWriter<Teaching> {}
 	public static final TeachingWriter toXML = GWT.create(TeachingWriter.class);
 
 	private int id;
 	
 	private TeachingType type;
 	
 	private String teacher;
 	
 	private int nbHour;
 	
 	private int nbSeance;
 	
 	private int numGroup;
 	
	private List<Date> seances;
 	
 	private Module module;
 	
 	
 	
 	public Teaching() {
 		seances = new ArrayList<Date>();
 	}
 	
 	public Teaching(TeachingType type, String teacher, int nbHour, int nbSeance, int numGroup, List<Date> seances, Module module) {
 		this.type = type;
 		this.teacher = teacher;
 		this.nbHour = nbHour;
 		this.nbSeance = nbSeance;
 		this.numGroup = numGroup;
 		this.seances = seances;
 		this.module = module;
 	}
 	
 	
 
 	public int getId() {
 		return id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public TeachingType getType() {
 		return type;
 	}
 
 	public void setType(TeachingType type) {
 		this.type = type;
 	}
 
 	public String getTeacher() {
 		return teacher;
 	}
 
 	public void setTeacher(String teacher) {
 		this.teacher = teacher;
 	}
 
 	public int getNbHour() {
 		return nbHour;
 	}
 
 	public void setNbHour(int nbHour) {
 		this.nbHour = nbHour;
 	}
 
 	public int getNbSeance() {
 		return nbSeance;
 	}
 
 	public void setNbSeance(int nbSeance) {
 		this.nbSeance = nbSeance;
 	}
 
 	public int getNumGroup() {
 		return numGroup;
 	}
 
 	public void setNumGroup(int numGroup) {
 		this.numGroup = numGroup;
 	}
 
 	public List<Date> getSeances() {
 		return seances;
 	}
 
 	public void setSeances(List<Date> sceances) {
 		this.seances = sceances;
 	}
 
 	public Module getModule() {
 		return module;
 	}
 
 	public void setModule(Module module) {
 		this.module = module;
 	}
 	
 	public void addSeance(Date date) {
 		this.seances.add(date);
 	}
 	
 	public void removeSeance(Date date) {
 		for (int i = 0; i < this.seances.size(); i++)
 			if (this.seances.get(i).equals(date))
 				this.seances.remove(i);
 	}
 	
 	public boolean isSessionInPeriod(Date start) {
 		//TODO : timestamp to Date
 //		int WEEK = 604800;
 //		for (int i = 0; i < this.seances.size(); i++)
 //			if (this.seances.get(i).after(start) && this.seances.get(i).before(start + WEEK))
 //				return true;
 		return false;
 	}
 }
