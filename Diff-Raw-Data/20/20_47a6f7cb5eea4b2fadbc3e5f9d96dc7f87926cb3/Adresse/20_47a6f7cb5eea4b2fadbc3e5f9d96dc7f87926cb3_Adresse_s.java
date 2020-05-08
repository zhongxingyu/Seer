 package de.shop.Kundenverwaltung.domain;
 
 import static de.shop.Util.Constants.KEINE_ID;
 import static de.shop.Util.Constants.LONG_ANZ_ZIFFERN;
 import static java.util.logging.Level.FINER;
 import static javax.persistence.TemporalType.TIMESTAMP;
 import static javax.xml.bind.annotation.XmlAccessType.FIELD;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.PostPersist;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.validation.constraints.Digits;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  * The persistent class for the adresse database table.
  * 
  */
 @Entity
 @Table(name = "adresse")
 @XmlAccessorType(FIELD)
 
 @NamedQueries({
 @NamedQuery(name = Adresse.ADRESSE_BY_ID,
 			query = "FROM Adresse a WHERE a.id = :" + Adresse.PARAM_ID)
 })
 public class Adresse implements Serializable {
 	
 	private static final long serialVersionUID = -6118084007129611335L;
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 	
 	private static final String PREFIX = "Adresse.";
 	public static final String ADRESSE_BY_ID = PREFIX + "findAdresseByID";	
 	public static final String PARAM_ID = "id";
 		
 	@Id
 	@GeneratedValue
 	@Column(name = "adresse_ID", nullable = false, updatable = false, precision = LONG_ANZ_ZIFFERN)
 	@XmlAttribute
 	private Long id = KEINE_ID;
 	
 	@NotNull(message = "{kundenverwaltung.adresse.hausNr.notNull}")
 	private String hausNr;
 	
 	//EAGER-Fetching
 	@OneToOne
 	@JoinColumn(name = "kunde_FID", nullable = false, updatable = false)
 	@XmlTransient
 	private Kunde kunde;
 	
 	@NotNull(message = "{kundenverwaltung.adresse.land.notNull}")
 	@XmlElement(required = true)
 	private String land;
 	
 	@NotNull(message = "{kundenverwaltung.adresse.ort.notNull}")
 	@XmlElement(required = true)
 	private String ort;
 	
 	@NotNull(message = "{kundenverwaltung.adresse.plz.notNull}")
 	@Digits(integer = 5, fraction = 0, message = "{kundenverwaltung.adresse.plz.digits}")
 	@XmlElement(required = true)
 	private String plz;
 	
 	@NotNull(message = "{kundenverwaltung.adresse.strasse.notNull}")
 	@XmlElement(required = true)
 	private String strasse;
 	
 	@Column(name = "erstellt_am")
 	@Temporal(TIMESTAMP)
 	@XmlTransient
 	private Date erstelltAm;
 
 	@Column(name = "geaendert_am")
 	@Temporal(TIMESTAMP)
 	@XmlTransient
 	private Date geaendertAm;
 	
 	@PrePersist
 	protected void prePersist() {
 		erstelltAm = new Date();
 		geaendertAm = new Date();
 	}
 	
 	@PostPersist
 	private void postPersist() {
 		LOGGER.log(FINER, "Neue Adresse mit ID={0}", id);
 	}
 	
 	@PreUpdate
 	protected void preUpdate() {
 		geaendertAm = new Date();
 	}
 
 	public Adresse() {
 	}
 
 	public Kunde getKunde() {
 		return kunde;
 	}
 
 	public void setKunde(Kunde kunde) {
 		this.kunde = kunde;
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Date getErstelltAm() {
 		return this.erstelltAm == null ? null : (Date) this.erstelltAm.clone();
 	}
 
 	public void setErstelltAm(Date erstelltAm) {
 		this.erstelltAm = erstelltAm == null ? null : (Date) erstelltAm.clone();
 	}
 
 	public Date getGeaendertAm() {
 		return this.geaendertAm == null ? null : (Date) this.geaendertAm.clone();
 	}
 
 	public void setGeaendertAm(Date geaendertAm) {
 		this.geaendertAm = geaendertAm == null ? null : (Date) geaendertAm.clone();
 	}
 
 	public String getHausNr() {
 		return this.hausNr;
 	}
 
 	public void setHausNr(String hausNr) {
 		this.hausNr = hausNr;
 	}
 
 	public String getLand() {
 		return this.land;
 	}
 
 	public void setLand(String land) {
 		this.land = land;
 	}
 
 	public String getOrt() {
 		return this.ort;
 	}
 
 	public void setOrt(String ort) {
 		this.ort = ort;
 	}
 
 	public String getPlz() {
 		return this.plz;
 	}
 
 	public void setPlz(String plz) {
 		this.plz = plz;
 	}
 
 	public String getStrasse() {
 		return this.strasse;
 	}
 
 	public void setStrasse(String strasse) {
 		this.strasse = strasse;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result
 				+ ((erstelltAm == null) ? 0 : erstelltAm.hashCode());
 		result = prime * result
 				+ ((geaendertAm == null) ? 0 : geaendertAm.hashCode());
 		result = prime * result + ((hausNr == null) ? 0 : hausNr.hashCode());
 		result = prime * result + ((land == null) ? 0 : land.hashCode());
 		result = prime * result + ((ort == null) ? 0 : ort.hashCode());
 		result = prime * result + ((plz == null) ? 0 : plz.hashCode());
 		result = prime * result + ((strasse == null) ? 0 : strasse.hashCode());
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
 		Adresse other = (Adresse) obj;
 		if (id != other.id)
 			return false;
 		if (erstelltAm == null) {
 			if (other.erstelltAm != null)
 				return false;
 		}
 		else if (!erstelltAm.equals(other.erstelltAm))
 			return false;
 		if (geaendertAm == null) {
 			if (other.geaendertAm != null)
 				return false;
 		}
 		else if (!geaendertAm.equals(other.geaendertAm))
 			return false;
 		if (hausNr == null) {
 			if (other.hausNr != null)
 				return false;
 		}
 		else if (!hausNr.equals(other.hausNr))
 			return false;
 		if (land == null) {
 			if (other.land != null)
 				return false;
 		}
 		else if (!land.equals(other.land))
 			return false;
 		if (ort == null) {
 			if (other.ort != null)
 				return false;
 		}
 		else if (!ort.equals(other.ort))
 			return false;
 		if (plz == null) {
 			if (other.plz != null)
 				return false;
 		}
 		else if (!plz.equals(other.plz))
 			return false;
 		if (strasse == null) {
 			if (other.strasse != null)
 				return false;
 		}
 		else if (!strasse.equals(other.strasse))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Adresse [adresse_ID=" + id + ", erstelltAm="
 				+ erstelltAm + ", geaendertAm=" + geaendertAm + ", hausNr="
 				+ hausNr + ", land=" + land + ", ort=" + ort + ", plz=" + plz
 				+ ", strasse=" + strasse + "]";
 	}
 	
 	
 
 }
