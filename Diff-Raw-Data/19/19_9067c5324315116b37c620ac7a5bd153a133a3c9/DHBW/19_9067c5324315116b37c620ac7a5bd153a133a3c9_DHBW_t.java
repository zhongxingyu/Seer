 package de.dhbw.swe.notenverwaltung;
 
 import java.util.ArrayList;
 import java.util.List;
 /**
  * Die DHBW ist der Hierarchiekopf.
  * Sie besitzt nur 1 Objekt, welches mit der Funktion "getDHBW" aufgerufen werden kann und vereint alle Kurse unter sich. 
  * (Indirekt also auch alle Studenten.)
  * 
  * @author Hanne Nobis
  *
  */
 public class DHBW {
 	
 	private static DHBW dhbw;
 	private List<Kurs> kurse;
 
 	private DHBW(){
 		kurse = new ArrayList<Kurs>();
 	}
 	
 	
 	/**
 	 * Liefert das Singleton-Objekt der DHBW-Klasse zurck
 	 * @return
 	 */
 	public static DHBW getDHBW(){
 		
 		if(dhbw == null) {
 			dhbw = new DHBW();
 		}
 		
 		return dhbw;
 	}
 	
 	
 	/**
 	 * Findet einen Studenten anhand seiner Matrikelnummer
 	 * @param matrikelnummer
 	 * @return Student; null, wenn der Student nicht gefunden wird
 	 */
 	public Student findStudentByMatrikelnummer(int matrikelnummer){
 		
 		for(Kurs k : kurse) {
 			for(Student s : k.getStudenten()) {
 				if(s.getMatrikelnummer() == matrikelnummer) {
 					return s;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Findet einen Kurs anhand seines Namens
 	 * @param kursname
 	 * @return Kurs; null, wenn der Kurs nicht gefunden wird
 	 */
 	public Kurs findKursByName(String kursname){
 		
 		for(Kurs k : kurse) {
 			if(k.getKursname().equals(kursname)) {
 				return k;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	public void addKurs(Kurs kurs) {
 		kurse.add(kurs);
 		kurs.setDhbw(this);
 	}
 	
 	public void removeKurs(Kurs kurs) {
 		kurse.remove(kurs);
		kurs.setDhbw(null);
 	}
 	
 	
 	//Getter und Setter
 	public List<Kurs> getKurse() {
 		return kurse;
 	}
 
 	public void setKurse(List<Kurs> kurse) {
 		this.kurse = kurse;
 	}
 	
 }
