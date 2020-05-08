 package de.dhbw.swe.notenverwaltung;
 
 import java.util.ArrayList;
 import java.util.List;
 /**
  * Beschreibt das Objekt "Kurs".
  * Der Kurs liegt direkt unter der DHBW. 
  * Er vereint mehrere Studenten.
  * 
  * @author Hanne Nobis
  *
  */
 public class Kurs {
 
 	private String kursname;
 	private int jahrgang;
 	private String raum;
 	private String studiengang;
 	private String studiengangsleiter;
 	
 	private DHBW dhbw;
 	private List<Student> studenten;
 
 	
 	public Kurs(String kursname, int jahrgang, String studiengang) {
 		this.kursname = kursname;
 		this.jahrgang = jahrgang;
 		this.studiengang = studiengang;
 		
 		studenten = new ArrayList<Student>();
 	}
 	
 	public void addStudent(Student student) {
 		studenten.add(student);
 		student.setKurs(this);
 	}
 	
 	public void removeStudent(Student student) {
 		studenten.remove(student);
 	}
 	
 	
 	
 	
 	//Getter und Setter
 	public String getKursname() {
 		return kursname;
 	}
 	public void setKursname(String kursname) {
 		this.kursname = kursname;
 	}
 	
 	public int getJahrgang() {
 		return jahrgang;
 	}
 	public void setJahrgang(int jahrgang) {
 		this.jahrgang = jahrgang;
 	}
 	
 	public String getRaum() {
 		return raum;
 	}
 	public void setRaum(String raum) {
 		this.raum = raum;
 	}
 	
 	public String getStudiengang() {
 		return studiengang;
 	}
 	public void setStudiengang(String studiengang) {
 		this.studiengang = studiengang;
 	}
 	
 	public String getStudiengangsleiter() {
 		return studiengangsleiter;
 	}
 	public void setStudiengangsleiter(String studiengangsleiter) {
 		this.studiengangsleiter = studiengangsleiter;
 	}
 	
 	public List<Student> getStudenten() {
 		return studenten;
 	}
 	public void setStudenten(List<Student> studenten) {
 		this.studenten = studenten;
 	}
 
 	public DHBW getDhbw() {
 		return dhbw;
 	}
 
 	public void setDhbw(DHBW dhbw) {
 		this.dhbw = dhbw;
 	}
 }
