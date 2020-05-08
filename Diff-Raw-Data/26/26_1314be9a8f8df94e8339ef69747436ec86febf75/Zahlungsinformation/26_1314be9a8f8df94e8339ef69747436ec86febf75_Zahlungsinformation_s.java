 package de.shop.kundenverwaltung.domain;
 
 import static de.shop.util.Constants.ERSTE_VERSION;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Version;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import javax.validation.constraints.Digits;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * The persistent class for the zahlungsinformation database table.
  * 
  */
 @Entity
 @Table(name = "zahlungsinformation")
 
 @NamedQueries({
 	@NamedQuery(name  = Zahlungsinformation.FIND_ZAHLUNGSINFORMATION_BY_KONTONR,
             	query = "SELECT z"
 			        + " FROM   Zahlungsinformation z"
             		+ " WHERE  z.kontonummer = :" + Zahlungsinformation.PARAM_ZAHLUNGSINFORMATION_KONTONR),
             		
     @NamedQuery(name  = Zahlungsinformation.FIND_ZAHLUNGSINFORMATION_BY_BLZ,
                 query = "SELECT z"
                 	+ " FROM   Zahlungsinformation z"
                     + " WHERE  z.blz = :" + Zahlungsinformation.PARAM_ZAHLUNGSINFORMATION_BLZ),
                     
     @NamedQuery(name  = Zahlungsinformation.FIND_ALL_ZAHLUNGSINFORMATION,
                 query = "SELECT z"
                     + " FROM   Zahlungsinformation z"),
    // Deaktiviert, ermittlung mittels EM  
    /* @NamedQuery(name  = Zahlungsinformation.FIND_ZAHLUNGSINFORMATION_BY_ID,
                 query = "SELECT z"
                     + " FROM Zahlungsinformation z"
                 	+ " WHERE z.zahl_id = :" + Zahlungsinformation.PARAM_ZAHLUNGSINFORMATION_ZAHL_ID)      */           
                     
 	})
 
 public class Zahlungsinformation implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	public static final String PARAM_ZAHLUNGSINFORMATION_KONTONR = "kontonr";
 	public static final String PARAM_ZAHLUNGSINFORMATION_BLZ = "blz";
 	public static final String PARAM_ZAHLUNGSINFORMATION_ZAHL_ID = "zahlID";
 	private static final String PREFIX = "Zahlungsinformation.";
 	public static final String FIND_ZAHLUNGSINFORMATION_BY_KONTONR = PREFIX + "findZahlungsinformation";
 	public static final String FIND_ZAHLUNGSINFORMATION_BY_BLZ = PREFIX + "findZahlungsinformationByBlz";
 	public static final String FIND_ZAHLUNGSINFORMATION_BY_ID = PREFIX + "findZahlungsinformationById";
 	public static final String FIND_ALL_ZAHLUNGSINFORMATION = PREFIX + "findAllZahlungsinformation";
 	
 
 	@Id
 	@GeneratedValue
 	@Column(name = "zahl_id")
 	private Long zahlId = null;
 	
 	@Version
 	@Basic(optional = false)
 	private int version = ERSTE_VERSION;
 	
 	@NotNull(message = "{kundenverwaltung.zahlungsinformation.kontoinhaber.notNull}")
 	@Column(length = 45, nullable = false)
 	@Size(min = 2, max = 45, message = "{kundenverwaltung.zahlungsinformation.kontoinhaber.length}")
 	@Pattern(regexp = "[A-Z][a-z]+ [A-Z][a-z]+"
 			 , message = "{kundenverwaltung.zahlungsinformation.kontoinhaber.pattern}")
 	private String kontoinhaber;
 	
 	@NotNull(message = "{kundenverwaltung.zahlungsinformation.kontonummer.notNull}")
 	@Column(nullable = false)
 	@Min(value = 2 , message = "{kundenverwaltung.zahlungsinformation.kontonummer.min}")
 	// TODO Test hat nicht funktioniert bei Angabe mit @ Max obwohl KtNr <30
 	//@Max(value = 30, message = "{kundenverwaltung.zahlungsinformation.kontonummer.max}")
 	@Digits(integer = 24, fraction = 0)
 	private Long kontonummer;
 	
 	@NotNull(message = "{kundenverwaltung.zahlungsinformation.blz.notNull}")
 	@Column(nullable = false)
 	private Long blz;
 	
 	@NotNull(message = "{kundenverwaltung.zahlungsinformation.kreditinstitut.notNull}")
 	@Size(min = 2, max = 60, message = "{kundenverwaltung.zahlungsinformation.kreditinstitut.length}")
 	@Column(length = 60, nullable = false)
 	private String kreditinstitut;
 	
 	@NotNull(message = "{kundenverwaltung.zahlungsinformation.iban.notNull}")
 	@Column(length = 34, nullable = false)
 	@Size(min = 22, max = 34, message = "{kundenverwaltung.zahlungsinformation.iban.length}")
 	private String iban;
 	
 	@NotNull(message = "{kundenverwaltung.zahlungsinformation.swift.notNull}")
 	@Column(length = 45, nullable = false)
 	@Size(min = 2, max = 45, message = "{kundenverwaltung.zahlungsinformation.swift.length}")
 	private String swift;
 	
 	@Column(nullable = false)
 	@JsonIgnore
 	private Timestamp aktualisiert;
 	
 	@Column(nullable = false)
 	@JsonIgnore
 	private Timestamp erzeugt;
 
 	public Long getZahlId() {
 		return this.zahlId;
 	}
 
 	public void setZahlId(Long zahlId) {
 		this.zahlId = zahlId;
 	}
 	
 	
 	public Timestamp getAktualisiert() {
 		return aktualisiert == null ? null : (Timestamp) aktualisiert.clone();
 	}
 	
 	
 	public void setAktualisiert(Timestamp aktualisiert) {
 		this.aktualisiert = aktualisiert == null ? null : (Timestamp) aktualisiert.clone();
 	}
 
 	public Long getBlz() {
 		return this.blz;
 	}
 
 	public void setBlz(Long blz) {
 		this.blz = blz;
 	}
 	
 	
 	public Timestamp getErzeugt() {
 		return erzeugt == null ? null : (Timestamp) erzeugt.clone();
 	}
 	
 	
 	public void setErzeugt(Timestamp erzeugt) {
 		this.erzeugt = erzeugt == null ? null : (Timestamp) erzeugt.clone();
 	}
 
 	public String getIban() {
 		return this.iban;
 	}
 
 	public void setIban(String iban) {
 		this.iban = iban;
 	}
 
 	public String getKontoinhaber() {
 		return this.kontoinhaber;
 	}
 
 	public void setKontoinhaber(String kontoinhaber) {
 		this.kontoinhaber = kontoinhaber;
 	}
 
 	public Long getKontonummer() {
 		return this.kontonummer;
 	}
 
 	public void setKontonummer(Long kontonummer) {
 		this.kontonummer = kontonummer;
 	}
 
 	public String getKreditinstitut() {
 		return this.kreditinstitut;
 	}
 
 	public void setKreditinstitut(String kreditinstitut) {
 		this.kreditinstitut = kreditinstitut;
 	}
 
 	public String getSwift() {
 		return this.swift;
 	}
 
 	public void setSwift(String swift) {
 		this.swift = swift;
 	}
 	
 	public int getVersion() {
 		return this.version;
 	}
 	
 	public void setVersion(int version) {
 		this.version = version;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((kontoinhaber == null) ? 0 : kontoinhaber.hashCode());
 		result = prime * result
 				+ ((kontonummer == null) ? 0 : kontonummer.hashCode());
 		result = prime * result + ((blz == null) ? 0 : blz.hashCode());
 		result = prime * result + ((kreditinstitut == null) ? 0 : kreditinstitut.hashCode());
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
 		Zahlungsinformation other = (Zahlungsinformation) obj;
 		
 		if (kontoinhaber == null) {
 		if (other.kontoinhaber != null)
 			return false;
 		}
 		else if (!kontoinhaber.equals(other.kontoinhaber))
 			return false;
 		
 		if (kontonummer == null) {
 		if (other.kontonummer != null)
 			return false;
 		}
 		else if (!kontonummer.equals(other.kontonummer))
 			return false;
 		
 		if (blz == null) {
 		if (other.blz != null)
 			return false;
 		}
 		else if (!blz.equals(other.blz))
 			return false;
 		
 		if (kreditinstitut == null) {
 		if (other.kreditinstitut != null)
 			return false;
 		}
 		else if (!kreditinstitut.equals(other.kreditinstitut))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Zahlungsinformation [zahlId=" + zahlId + ", aktualisiert="
 				+ aktualisiert + ", blz=" + blz + ", erzeugt=" + erzeugt
 				+ ", iban=" + iban + ", kontoinhaber=" + kontoinhaber
 				+ ", kontonummer=" + kontonummer + ", kreditinstitut="
 				+ kreditinstitut + ", swift=" + swift + "]";
 	}
 	
 	@PrePersist
 	private void prePersist() {
 		erzeugt = new Timestamp(new Date().getTime());
 		aktualisiert = new Timestamp(new Date().getTime());
 	}
 	
 	@PreUpdate
 	protected void preUpdate() {
 		aktualisiert = new Timestamp(new Date().getTime());
 	} 
 	
 	public void setValues(Zahlungsinformation z) {
 		this.kontoinhaber = z.kontoinhaber;
 		this.kontonummer = z.kontonummer;
 		this.blz = z.blz;
 		this.kreditinstitut = z.kreditinstitut;
 		this.iban = z.iban;
 		this.swift = z.swift;
 		this.version = z.getVersion();
 	}
 
}
