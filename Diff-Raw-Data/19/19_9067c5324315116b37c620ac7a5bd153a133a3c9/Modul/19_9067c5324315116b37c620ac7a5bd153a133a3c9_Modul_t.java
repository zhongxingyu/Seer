 package de.dhbw.swe.notenverwaltung;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Module sind einem Studienplan zugeordnet und besitzen eine Liste mit Units
  * 
  * @author Michael Strobel
  *
  */
 public class Modul {
 	
 	private String name;
 	private int credits;
 	private int semester;
 	
 	private int praesenz;
 	private int eigenstudium;
 	
 	/**
 	 * Ist ein Modul nicht benotet, werden Note und Credits nicht in die Berechnung
 	 * der Bachelornote mit einbezogen
 	 */
 	private boolean benotet = true;
 	
 	/**
 	 * Gesamtnote des Moduls
 	 * Wird automatisch berechnet, sobald alle Units als bestanden markiert wurden
 	 */
 	private double modulnote;
 	
 	/**
 	 * Modul wurde bestanden
 	 * Wird automatisch gesetzt, wenn die Gesamtmodulnote als 4 oder besser berechnet wurde
 	 */
 	private boolean bestanden;
 	
 	/**
 	 * Der Studienplan, zu welchem das Modul gehrt
 	 * Wird automatisch gesetzt, wenn das Modul zum Studienplan hinzugefgt wird
 	 */
 	private Studienplan studienplan;
 	
 	private List<Unit> units;
 	
 	
 	public Modul(String name) {
 		this.name = name;
 		this.units = new ArrayList<Unit>();
 	}
 	
 	/**
 	 * Fgt eine Unit zum Modul hinzu und setzt dessen Verbindung zum Modul
 	 * @param unit
 	 */
 	public void addUnit(Unit unit) {
 		units.add(unit);
 		unit.setModul(this);
 	}
 	
 	/**
 	 * Entfernt eine Unit aus der Liste
 	 * @param unit
 	 */
 	public void removeUnit(Unit unit) {
 		units.remove(unit);
		unit.setModul(null);
 	}
 	
 	/**
 	 * Berechnet die Modulnote
 	 * Wird automatisch beim Bestehen einer Unit oder beim ndern ihrer Note angestoen
 	 * Markiert das Modul bei einer Note von 4 oder besser als bestanden, wenn alle Units bestanden werden
 	 * Bei unbenoteten Modulen wird nur berprft, ob alle Units bestanden wurden und dann das Modul
 	 * ebenfalls als bestanden markiert
 	 */
 	public void modulnoteBerechnen() {
 		
 		// benotetes Modul: Gewichteten Schnitt aus den Unitnoten bilden
 		if(isBenotet()) {
 		
 			double note = 0;
 			
 			for(Unit u : units) {
 				
 				// Note nicht berechnen, wenn eine Unit nicht bestanden wurde
 				if(!u.isBestanden()) {
 					return;
 				}
 				
 				if(u.getGewichtung() > 0) {
 					note += u.getNote() * u.getGewichtung() / 100;
 				}
 				
 			}
 			
 			modulnote = note;
 			
 			if(modulnote <= 4) {
 				bestanden = true;
 			}
 		}
 		
 		// unbenotetes Modul: Units mssen nur bestanden sein
 		else {
 			for(Unit u : units) {
 				if(!u.isBestanden()) {
 					return;
 				}
 			}
 			
 			bestanden = true;
 		}
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	public int getCredits() {
 		return credits;
 	}
 
 
 	public void setCredits(int credits) {
 		this.credits = credits;
 	}
 
 
 	public int getSemester() {
 		return semester;
 	}
 
 
 	public void setSemester(int semester) {
 		this.semester = semester;
 	}
 
 	/**
 	 * der Workload eines Moduls setzt sich zusammen
 	 * aus Prsenz und Eigenstudium
 	 * @return Prsenz + Eigenstudium
 	 */
 	public int getWorkload() {
 		return praesenz+eigenstudium;
 	}
 
 
 	public int getPraesenz() {
 		return praesenz;
 	}
 
 
 	public void setPraesenz(int praesenz) {
 		this.praesenz = praesenz;
 	}
 
 
 	public int getEigenstudium() {
 		return eigenstudium;
 	}
 
 
 	public void setEigenstudium(int eigenstudium) {
 		this.eigenstudium = eigenstudium;
 	}
 
 
 	public Studienplan getStudienplan() {
 		return studienplan;
 	}
 
 
 	public void setStudienplan(Studienplan studienplan) {
 		this.studienplan = studienplan;
 	}
 
 
 	public List<Unit> getUnits() {
 		return units;
 	}
 
 
 	public void setUnits(List<Unit> units) {
 		this.units = units;
 	}
 	
 	public boolean isBestanden() {
 		return bestanden;
 	}
 
 
 	public boolean isBenotet() {
 		return benotet;
 	}
 
 
 	public void setBenotet(boolean benotet) {
 		this.benotet = benotet;
 	}
 	
 	
 	public double getModulnote() {
 		return modulnote;
 	}
 	
 }
