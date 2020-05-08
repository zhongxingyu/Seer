 package model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 /**
  * Klass Album: Ein Album besteht aus einem Namen, einen optionalen Interpreten
  * und einem Coverbild. Ausserdem setzt sich ein Album aus mehreren Medien
  * zusammen. (Beziehungen noch nicht beachtet)
  * @author Hans-Helge Buerger
  *
  */
 @Entity
 @Table (name="MEDIUM")
 public class Medium {
 	@Id @GeneratedValue(strategy=GenerationType.AUTO)
 	private int id;
	@ManyToOne @Column(nullable=false)
 	private Type type;
 	@ManyToOne
 	private Album album;
 	@Column(nullable=false)
 	private String titel;
 	private String interpret;
 	@Column(nullable=false)
 	private float laenge;
 	@Column(nullable=false)
 	private float dateigroesse;
 	@Column(nullable=false)
 	private String pfad;
 	@Column(nullable=false)
 	private int angehoert;
 	@Column(nullable=false)
 	private int gekauft;
 	
 	public Medium(){
 		this.angehoert = 0;
 		this.gekauft = 0;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public Type getType() {
 		return type;
 	}
 	public void setType(Type type) {
 		this.type = type;
 	}
 	public Album getAlbum() {
 		return album;
 	}
 	public void setAlbum(Album album) {
 		this.album = album;
 	}
 	public String getTitel() {
 		return titel;
 	}
 	public void setTitel(String titel) {
 		this.titel = titel;
 	}
 	public String getInterpret() {
 		return interpret;
 	}
 	public void setInterpret(String interpret) {
 		this.interpret = interpret;
 	}
 	public float getLaenge() {
 		return laenge;
 	}
 	public void setLaenge(float laenge) {
 		this.laenge = laenge;
 	}
 	public float getDateigroesse() {
 		return dateigroesse;
 	}
 	public String getDateigroesseMB() {
 		return dateigroesse + " MB";
 	}
 	public void setDateigroesse(float dateigroesse) {
 		this.dateigroesse = dateigroesse;
 	}
 	public String getPfad() {
 		return pfad;
 	}
 	public void setPfad(String pfad) {
 		this.pfad = pfad;
 	}
 	public int getAngehoert() {
 		return angehoert;
 	}
 	public void setAngehoert(int angehoert) {
 		this.angehoert = angehoert;
 	}
 	public int getGekauft() {
 		return gekauft;
 	}
 	public void setGekauft(int gekauft) {
 		this.gekauft = gekauft;
 	}
 	
 }
