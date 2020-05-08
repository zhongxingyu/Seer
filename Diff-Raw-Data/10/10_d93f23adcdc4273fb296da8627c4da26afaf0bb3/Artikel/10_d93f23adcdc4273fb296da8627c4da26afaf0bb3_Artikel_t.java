 package de.shop.artikelverwaltung.domain;
 
 import java.io.Serializable;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import static de.shop.util.Constants.ERSTE_VERSION;
 import static de.shop.util.Constants.MIN_ID;
 import static de.shop.util.Constants.BEZEICHNUNG_LENGTH_MAX;
 import static de.shop.util.Constants.NAME_LENGTH_MAX;
 import static javax.persistence.TemporalType.TIMESTAMP;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.Table;
 import javax.persistence.Column;
 import javax.persistence.ManyToOne;
 import javax.persistence.JoinColumn;
 import javax.persistence.Version;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import de.shop.util.IdGroup;
 
 /**
  * The persistent class for the artikel database table.
  * 
  */
 @Entity
 @Table(name = "artikel")
 @NamedQueries({
 	@NamedQuery(name = Artikel.FIND_ARTIKEL,
 			query = "SELECT a FROM Artikel a"),
 			
 	@NamedQuery(name = Artikel.ARTIKEL_BY_BESCHREIBUNG,
 			query = "SELECT      a"
 					+ " FROM Artikel a"
 					+ " WHERE a.beschreibung LIKE :"	+ Artikel.PARAMETER_BESCHREIBUNG
 					+ " ORDER BY a.id ASC"),
 					
 	@NamedQuery(name = Artikel.ARTIKEL_BY_NAME,
 			query = "SELECT      a"
 					+ " FROM Artikel a"
 					+ " WHERE a.name LIKE :"	+ Artikel.PARAMETER_NAME
 					+ " ORDER BY a.id ASC"),	
 					
 	@NamedQuery(name = Artikel.ARTIKEL_BY_PREIS,
 			query = "SELECT      a"
 					+ " FROM Artikel a"
 					+ " WHERE a.preis = :"	+ Artikel.PARAMETER_PREIS	
 					+ " ORDER BY a.preis ASC"),
 					
 	@NamedQuery (name = Artikel.ARTIKEL_BY_AKTUALISIERT,
 			query = "SELECT     a"
 					+ " FROM Artikel a"
 					+ " WHERE a.aktualisiert = :"	+ Artikel.PARAMETER_AKTUALISIERT),
 					
 	@NamedQuery (name = Artikel.ARTIKEL_BY_ERZEUGT,
 			query = "SELECT      a"
 					+ " FROM Artikel a"
 					+ " WHERE a.erzeugt = :" + Artikel.PARAMETER_ERZEUGT),
 
 	@NamedQuery (name = Artikel.ARTIKEL_BY_KATEGORIE,
 				query = "SELECT      a"
 					+ " FROM Artikel a"
 				    + " WHERE a.kategorie.kategorieId = :" + Artikel.PARAMETER_KATEGORIE_ID)			
 
 								
 									
 })
 public class Artikel implements Serializable {
 	
 	private static final String PREFIX = "Artikel.";
 	
 	public static final String FIND_ARTIKEL = PREFIX + "findAllArtikel";
 	public static final String ARTIKEL_BY_BESCHREIBUNG = PREFIX + "findArtikelByBeschreibung";
 	public static final String ARTIKEL_BY_NAME = PREFIX + "findArtikelByName";
 	public static final String ARTIKEL_BY_PREIS = PREFIX + "findArtikelByPreis";
 	public static final String ARTIKEL_BY_ID 		 = PREFIX + "findArtikelById";		
 	public static final String ARTIKEL_BY_AKTUALISIERT = PREFIX + "findArtikelByAktualisiert";
 	public static final String ARTIKEL_BY_ERZEUGT = PREFIX + "findArtikelByErzeugt";
 	public static final String ARTIKEL_BY_KATEGORIE = PREFIX + "findArtikelByKateogrie";
 	public static final String ARTIKEL_BY_MAX_PREIS = PREFIX + "findArtikelByMaxPreis";
 	
 	public static final String PARAMETER_BESCHREIBUNG = "beschreibung";
 	public static final String PARAMETER_NAME = "name";
 	public static final String PARAMETER_PREIS = "preis";
 	public static final String PARAMETER_AKTUALISIERT = "aktualisiert";
 	public static final String PARAMETER_ERZEUGT = "erzeugt";
 	public static final String PARAMETER_KATEGORIE_ID = "kategorie_id";
 	
 	public Artikel(Long artikel_id, Kategorie kategorie, String beschreibung,
 			String name, float preis, Date erzeugt, Date aktualisiert) {
 		this.artikel_id = artikel_id;
 		this.kategorie = kategorie;
 		this.beschreibung = beschreibung;
 		this.name = name;
 		this.preis = preis;
 		this.erzeugt = erzeugt == null ? null : (Timestamp) erzeugt.clone();
 		this.aktualisiert = aktualisiert == null ? null : (Timestamp) aktualisiert.clone();
	
 	}
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue
 	@Min(value = MIN_ID, message = "{artikelverwaltung.artikel.id.min}", groups = IdGroup.class)
 	@Column(name = "artikel_id")
 	private Long artikel_id;
 	
 	@Version
 	@Basic(optional = false)
 	private int version = ERSTE_VERSION;
 
 	@ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.REMOVE })
 	@JoinColumn(name = "kategorie_id", updatable = false)
 	@NotNull(message = "{artikelverwaltung.artikel.kategorie.notNull}")
 	private Kategorie kategorie;
 	
 	@Column(nullable = false, length = 45)
 	@NotNull(message = "{artikelverwaltung.artikel.beschreibung.notNull}")
 	@Size(max = BEZEICHNUNG_LENGTH_MAX, message = "{artikelverwaltung.artikel.beschreibung.length}")
 	private String beschreibung;
 	
 	@Column(nullable = false, length = 45)
 	@NotNull(message = "{artikelverwaltung.artikel.name.notNull}")
 	@Size(max = NAME_LENGTH_MAX, message = "{artikelverwaltung.artikel.name.length}")
 	private String name;
 
 	@Column(nullable = false)
 	@NotNull
 	private float preis;
 	
 	@Column(nullable = false)
 	@Temporal(TIMESTAMP)
 	@JsonIgnore
 	private Date erzeugt;
 	
 	@Column(nullable = false)
 	@Temporal(TIMESTAMP)
 	@JsonIgnore
 	private Date aktualisiert;
 
 	//@SuppressWarnings("unused")
 	@PrePersist
 	private void prePersist() {
 		erzeugt = new Date();
 		aktualisiert = new Date();
 	}
 
 	//@SuppressWarnings("unused")
 	@PreUpdate
 	private void preUpdate() {
 		aktualisiert = new Date();
 	}
 	
 	
 	public Artikel() {
 	}
 
 	public Long getArtikelId() {
 		return this.artikel_id;
 	}
 
 	public void setArtikelId(Long artikelId) {
 		this.artikel_id = artikelId;
 	}
 
 
 	public String getBeschreibung() {
 		return this.beschreibung;
 	}
 
 	public void setBeschreibung(String beschreibung) {
 		this.beschreibung = beschreibung;
 	}
 
 
 	public Kategorie getKategorie() {
 		return this.kategorie;
 	}
 
 	public void setKategorie(Kategorie kategorie) {
 		this.kategorie = kategorie;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public float getPreis() {
 		return this.preis;
 	}
 
 	public void setPreis(float preis) {
 		this.preis = preis;
 	}
 	public void setValues(Artikel a) {
 		beschreibung = a.beschreibung;
 		name = a.name;
 		preis = a.preis;
 		kategorie.setValues(a.kategorie);
 		version = a.version;
 		
 	}	
 
 	public Date getErzeugt() {
 		return erzeugt == null ? null : (Date) erzeugt.clone();
 	}
 
 	public void setErzeugt(Date erzeugt) {
 		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
 	}
 
 	public Date getAktualisiert() {
 		return aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 
 	public void setAktualisiert(Date aktualisiert) {
 		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 
 	@Override
 	public String toString() {
 		return "Artikel [artikelId=" + artikel_id + ", aktualisiert="
 				+ aktualisiert + ", beschreibung=" + beschreibung
 				+ ", erzeugt=" + erzeugt
 				+ ", name=" + name + ", preis=" + preis + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((artikel_id == null) ? 0 : artikel_id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Artikel other = (Artikel) obj;
 //		if (artikel_id == null) {
 //			if (other.artikel_id != null)
 //				return false;
 //		} 
 		if (!name.equals(other.name)||!beschreibung.equals(other.beschreibung)||!kategorie.equals(other.kategorie)){
 			return false;
 			}	
 		return true;
 	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
 }
 
