 package de.dhbw.swe.notenverwaltung;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Ein Studienplan ist einem Studenten zugeordnet und besitzt eine Liste von Modulen
  * 
  * @author Michael Strobel
  *
  */
 public class Studienplan {
 	
 	private String name;
 	private String abschluss;
 	
 	/**
 	 * Student, zu welchem der Studienplan gehrt
 	 * Wird beim Hinzufgen des Studienplans zum Studenten automatisch gesetzt
 	 */
 	private Student student;
 	
 	private List<Modul> module;
 	
 	
 	public Studienplan(String name, String abschluss) {
 		this.name = name;
 		this.abschluss = abschluss;
 		this.module = new ArrayList<Modul>();
 	}
 	
 	
 	/**
 	 * Fgt ein Modul hinzu und setzt dessen Verbindung zum Studienplan
 	 * @param modul
 	 */
 	public void addModul(Modul modul) {
 		module.add(modul);
 		modul.setStudienplan(this);
 	}
 	
 	/**
 	 * Entfernt ein Modul aus der Liste
 	 * @param modul
 	 */
 	public void removeModul(Modul modul) {
 		module.remove(modul);
		modul.setStudienplan(null);
 	}
 	
 	/**
 	 * Sucht eine Unit anhand ihres Namens
 	 * @param unitname
 	 * @return gesuchte Unit; null, wenn die Unit nicht gefunden wurde
 	 */
 	public Unit findUnitByName(String unitname){
 		
 		for(Modul m : module) {
 			for(Unit u : m.getUnits()) {
 				if(u.getName().equals(unitname)) {
 					return u;
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	//Getter und Setter
 	
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	public String getAbschluss() {
 		return abschluss;
 	}
 
 
 	public void setAbschluss(String abschluss) {
 		this.abschluss = abschluss;
 	}
 
 
 	public Student getStudent() {
 		return student;
 	}
 
 	
 	public void setStudent(Student student) {
 		this.student = student;
 	}
 	
 
 	public List<Modul> getModule() {
 		return module;
 	}
 	
 	
 	public void setModule(List<Modul> module) {
 		this.module = module;
 	}
 
 	
 }
