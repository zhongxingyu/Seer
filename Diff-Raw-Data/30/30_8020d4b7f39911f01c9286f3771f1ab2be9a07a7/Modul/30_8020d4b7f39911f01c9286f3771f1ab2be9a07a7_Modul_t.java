 package de.team55.mms.data;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 @XmlRootElement(name = "Modul")
@XmlType(propOrder = { "name", "version", "user","inbearbeitung", "datum", "status", "kommentar", "felder" })
 public class Modul {
 
 	private String name;
 	private int version;
 	private Date datum;
 	private int status;
 	private boolean inbearbeitung = false;
 	private ArrayList<String> user = new ArrayList<String>();
 	private ArrayList<Feld> felder = new ArrayList<Feld>();
 	private String kommentar;
 
 	
 	public Modul(String name, int version){
 		this.name = name;
 		this.version = version;
 	}
 	
 	
 	public Modul(String name2,
 			int version2, Date datum2,
 			int status, boolean inbearbeitung2, ArrayList<String> user2, String kommentar) {
 		this.name = name2;
 		this.version = version2;
 		this.datum = datum2;
 		this.setStatus(status);
 		this.inbearbeitung = inbearbeitung2;
 		this.user = user2;
 		this.kommentar = kommentar;
 	}
 	
 	public Modul(String name2,
 			int version2, Date datum2,
 			int status, boolean inbearbeitung2, String kommentar) {
 		this.name = name2;
 		this.version = version2;
 		this.datum = datum2;
 		this.setStatus(status);
 		this.inbearbeitung = inbearbeitung2;
 		this.kommentar = kommentar;
 	}
 	
 	public Modul(String name2,
 			ArrayList<Feld> felder, int version2, Date datum2,
 			int status, boolean inbearbeitung2, String kommentar) {
 		this.name = name2;
 		this.felder = felder;
 		this.version = version2;
 		this.datum = datum2;
 		this.setStatus(status);
 		this.inbearbeitung = inbearbeitung2;
 		this.kommentar = kommentar;
 	}
 	
 	
 	public Modul(String name2,
 			ArrayList<Feld> felder, int version2, Date datum2,
 			int status, boolean inbearbeitung2, ArrayList<String> user2, String kommentar) {
 		this.name = name2;
 		this.felder = felder;
 		this.version = version2;
 		this.datum = datum2;
 		this.setStatus(status);
 		this.inbearbeitung = inbearbeitung2;
 		this.user = user2;
 		this.kommentar = kommentar;
 	}
 	
 	public Modul() {
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Modul other = (Modul) obj;
 		if (felder == null) {
 			if (other.felder != null)
 				return false;
 		} else if (!felder.equals(other.felder))
 			return false;
 		if (inbearbeitung != other.inbearbeitung)
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (user == null) {
 			if (other.user != null)
 				return false;
 		} else if (!user.equals(other.user))
 			return false;
 		return true;
 	} 
 
 	public Date getDatum() {
 		return datum;
 	}
 
 	@XmlElementWrapper(name = "felder")
 	@XmlElement(name = "feld")
 	public ArrayList<Feld> getFelder() {
 		return felder;
 	}
 
 
 	@XmlElement(name = "modulname")
 	public String getName() {
 		return name;
 	}
 
 	public int getVersion() {
 		return version;
 	}
 	
 //	@XmlElementWrapper(name = "zuordnungen")
 //	@XmlElement(name = "zuordnung")
 //	public ArrayList<Zuordnung> getZuordnungen() {
 //		return zuordnungen;
 //	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((felder == null) ? 0 : felder.hashCode());
 		result = prime * result + (inbearbeitung ? 1231 : 1237);
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((user == null) ? 0 : user.hashCode());
 		return result;
 	}
 
 
 
 	public boolean isInbearbeitung() {
 		return inbearbeitung;
 	}
 
 
 
 	public void setDatum(Date datum) {
 		this.datum = datum;
 	}
 
 	public void setFelder(ArrayList<Feld> felder) {
 		this.felder = felder;
 	}
 
 	public void setInbearbeitung(boolean inbearbeitung) {
 		this.inbearbeitung = inbearbeitung;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setVersion(int version) {
 		this.version = version;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
  
 	public ArrayList<String> getUser(){
 		return user;
 	}
 	
 	public void setUser(ArrayList<String> user){
 		this.user = user;
 	}
 	
 
 	@Override
 	public String toString() {
 		return name+", erstellt von: "+user;
 	}
 
 	public String getKommentar() {
 		return kommentar;
 	}
 
 	public void setKommentar(String kommentar) {
 		this.kommentar = kommentar;
 	}
 
 	
 }
