 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import play.db.ebean.Model;
 
 @Entity
 public class Arbeid extends Model {
 	private static final long serialVersionUID = -7058143797771675826L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public long ansattNr;
	public Date dato;
 	public String arbeid;
 	public double timer;
 	public boolean overtid;
 	public String kommentar;
 	
 	public void oppdater(Arbeid arbeid) {
 		this.dato = arbeid.getDato();
 		this.arbeid = arbeid.getArbeid();
 		this.timer = arbeid.getTimer();
 		this.overtid = arbeid.getOvertid();
 		this.kommentar = arbeid.getKommentar();
 	}
 
	public Arbeid(long ansattNr, Date dato, String arbeid, double timer, boolean overtid, String kommentar) {
 		this.ansattNr = ansattNr;
		
		if (dato == null) {
			this.dato = new Date();	
		} else {
			this.dato = dato;
		}
		
 		this.arbeid = arbeid;
 		this.timer = timer;
 		this.overtid = overtid;
 		this.kommentar = kommentar;
 	}
 
 	public long getAnsattNr() {
 		return ansattNr;
 	}
 
 	public void setAnsattNr(long ansattNr) {
 		this.ansattNr = ansattNr;
 	}
 
	public Date getDato() {
 		return dato;
 	}
 
	public void setDato(Date dato) {
 		this.dato = dato;
 	}
 
 	public String getArbeid() {
 		return arbeid;
 	}
 
 	public void setArbeid(String arbeid) {
 		this.arbeid = arbeid;
 	}
 
 	public double getTimer() {
 		return timer;
 	}
 
 	public void setTimer(double timer) {
 		this.timer = timer;
 	}
 
 	public boolean getOvertid() {
 		return overtid;
 	}
 
 	public void setOvertid(boolean overtid) {
 		this.overtid = overtid;
 	}
 	
 	public String getKommentar() {
 		return kommentar;
 	}
 	
 	public void setKommentar(String kommentar) {
 		this.kommentar = kommentar;
 	}
 
 	public static Finder<Long, Arbeid> find = new Finder<Long, Arbeid>(Long.class, Arbeid.class);
 }
