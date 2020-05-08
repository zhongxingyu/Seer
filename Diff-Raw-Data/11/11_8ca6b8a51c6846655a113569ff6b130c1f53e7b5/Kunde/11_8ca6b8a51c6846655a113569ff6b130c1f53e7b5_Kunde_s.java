 package de.shop.kundenverwaltung.domain;
 
 import static de.shop.util.Constants.MIN_ID;
 import static de.shop.util.Constants.ERSTE_VERSION;
 import static javax.persistence.CascadeType.PERSIST;
 import static javax.persistence.CascadeType.REMOVE;
 import static javax.persistence.FetchType.EAGER;
 import static javax.persistence.FetchType.LAZY;
 import static javax.persistence.TemporalType.TIMESTAMP;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Basic;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.OrderColumn;
 import javax.persistence.PostPersist;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 import javax.persistence.UniqueConstraint;
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.hibernate.validator.constraints.Email;
 
 import de.shop.util.persistence.File;
 
 import org.hibernate.validator.constraints.ScriptAssert;
 import org.jboss.logging.Logger;
 import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
 
 import de.shop.auth.domain.RolleType;
 import de.shop.bestellverwaltung.domain.Bestellung;
 import de.shop.util.IdGroup;
 
 @Entity
 @Table(name = "kunde")
 @NamedQueries({
 	@NamedQuery(name  = Kunde.FIND_KUNDEN,
                 query = "SELECT k"
 				        + " FROM   Kunde k"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_FETCH_BESTELLUNGEN,
 				query = "SELECT  DISTINCT k"
 						+ " FROM Kunde k LEFT JOIN FETCH k.bestellungen"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_OHNE_BESTELLUNGEN,
                 query = "SELECT k"
 				        + " FROM   Kunde k"
 				        + " WHERE  k.bestellungen IS EMPTY"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_ORDER_BY_ID,
 		        query = "SELECT   k"
 				        + " FROM  Kunde k"
 		                + " ORDER BY k.id"),
 	@NamedQuery(name  = Kunde.FIND_IDS_BY_PREFIX,
 		        query = "SELECT   k.id"
 		                + " FROM  Kunde k"
 		                + " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_KUNDE_ID_PREFIX
 		                + " ORDER BY k.id"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME,
 	            query = "SELECT k"
 				        + " FROM   Kunde k"
 	            		+ " WHERE  UPPER(k.nachname) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"),
 	@NamedQuery(name  = Kunde.FIND_NACHNAMEN_BY_PREFIX,
    	            query = "SELECT   DISTINCT k.nachname"
 				        + " FROM  Kunde k "
 	            		+ " WHERE UPPER(k.nachname) LIKE UPPER(:"
 	            		+ Kunde.PARAM_KUNDE_NACHNAME_PREFIX + ")"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
 	            query = "SELECT DISTINCT k"
 			            + " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
 			            + " WHERE  UPPER(k.nachname) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
 	            query = "SELECT DISTINCT k"
 			            + " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
 			            + " WHERE  k.id = :" + Kunde.PARAM_KUNDE_ID),
    	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL,
    	            query = "SELECT DISTINCT k"
    			            + " FROM   Kunde k"
    			            + " WHERE  k.email = :" + Kunde.PARAM_KUNDE_EMAIL),
     @NamedQuery(name  = Kunde.FIND_KUNDEN_BY_PLZ,
 	            query = "SELECT k"
 				        + " FROM  Kunde k"
 			            + " WHERE k.adresse.plz = :" + Kunde.PARAM_KUNDE_ADRESSE_PLZ),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_USERNAME,
 			            query = "SELECT   k"
 						+ " FROM  Kunde k"
 			            + " WHERE CONCAT('', k.id) = :" + Kunde.PARAM_KUNDE_USERNAME),
 	@NamedQuery(name  = Kunde.FIND_USERNAME_BY_USERNAME_PREFIX,
 		  	            query = "SELECT   CONCAT('', k.id)"
 		  				+ " FROM  Kunde k"
 		   	            + " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_USERNAME_PREFIX),		      
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_ID_PREFIX,
 				query = "SELECT   k"
 				        + " FROM  Kunde k"
 				        + " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_KUNDE_ID_PREFIX
 				        + " ORDER BY k.id")
 })
 @ScriptAssert(lang = "javascript",
 	          script = "(_this.password == null && _this.passwordWdh == null)"
 	                   + "|| (_this.password != null && _this.password.equals(_this.passwordWdh))",
 	          message = "{kundenverwaltung.kunde.password.notEqual}")
 @XmlRootElement
 @Formatted
 public class Kunde implements Serializable, Cloneable {
 	private static final long serialVersionUID = 7401524595142572933L;
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 	
 	//Pattern mit UTF-8 (statt Latin-1 bzw. ISO-8859-1) Schreibweise fuer Umlaute:
 	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
 	private static final String PREFIX_ADEL = "(o'|von|von der|von und zu|van)?";
 	
 	public static final String NACHNAME_PATTERN = PREFIX_ADEL + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
 	public static final int NACHNAME_LENGTH_MIN = 2;
 	public static final int NACHNAME_LENGTH_MAX = 32;
 	public static final int VORNAME_LENGTH_MAX = 32;
 	public static final int EMAIL_LENGTH_MAX = 128;
 	public static final int DETAILS_LENGTH_MAX = 128 * 1024;
 	public static final int PASSWORD_LENGTH_MAX = 256;
 	public static final int GESCHLECHT_LENGTH = 1;
 
 	private static final String PREFIX = "Kunde.";
 	public static final String FIND_KUNDEN = PREFIX + "findKunden";
 	public static final String FIND_KUNDEN_BY_ID_PREFIX = PREFIX + "findKundenByIdPrefix";
 	public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX + "findKundenFetchBestellungen";
 	public static final String FIND_KUNDEN_ORDER_BY_ID = PREFIX + "findKundenOrderById";
 	public static final String FIND_KUNDE_BY_USERNAME = PREFIX + "findKundeByUsername";
 	public static final String PARAM_KUNDE_USERNAME = "username";
 	public static final String PARAM_USERNAME_PREFIX = "usernamePrefix";
 	public static final String FIND_USERNAME_BY_USERNAME_PREFIX = PREFIX + "findKundeByUsernamePrefix";
 	public static final String FIND_IDS_BY_PREFIX = PREFIX + "findIdsByPrefix";
 	public static final String FIND_KUNDEN_OHNE_BESTELLUNGEN = PREFIX + "findKundenOhneBestellungen";
 	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
 	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN =
 		                       PREFIX + "findKundenByNachnameFetchBestellungen";
 	public static final String FIND_NACHNAMEN_BY_PREFIX = PREFIX + "findNachnamenByPrefix";
 	public static final String FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN =
 		                       PREFIX + "findKundeByIdFetchBestellungen";
 	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
 	public static final String FIND_KUNDEN_BY_PLZ = PREFIX + "findKundenByPlz";
 	public static final String FIND_KUNDEN_BY_DATE = PREFIX + "findKundenByDate";
 	
 	public static final String PARAM_KUNDE_ID = "kundeId";
 	public static final String PARAM_KUNDE_ID_PREFIX = "idPrefix";
 	public static final String PARAM_KUNDE_NACHNAME = "nachname";
 	public static final String PARAM_KUNDE_NACHNAME_PREFIX = "nachnamePrefix";
 	public static final String PARAM_KUNDE_ADRESSE_PLZ = "plz";
 	public static final String PARAM_KUNDE_SEIT = "seit";
 	public static final String PARAM_KUNDE_EMAIL = "email";
 	
 	public static final String GRAPH_BESTELLUNGEN = "bestellungen";
 	
 	@Id
 	@GeneratedValue
 	@Min(value = MIN_ID, message = "{kundenverwaltung.kunde.id.min}", groups = IdGroup.class)
 	@Column(nullable = false, updatable = false)
 	private Long id;
 	
 	@Version
 	@Basic(optional = false)
 	private int version = ERSTE_VERSION;
 	
 	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX,
 	      message = "{kundenverwaltung.kunde.vorname.length}")
 	private String vorname;
 	
 	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
 	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX,
 	      message = "{kundenverwaltung.kunde.nachname.length}")
 	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
 	private String nachname;
 	
 	@NotNull(message = "{kundenverwaltung.kunde.geschlecht.notNull}")
 	@Size(min = GESCHLECHT_LENGTH, max = GESCHLECHT_LENGTH,
     message = "{kundenverwaltung.kunde.geschlecht.length}")
 	private String geschlecht;
 	
 	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
 	@NotNull(message = "{kundenverwaltung.kunde.email.notNull}")
 	@Size(max = EMAIL_LENGTH_MAX, message = "{kundenverwaltung.kunde.email.length}")
 	@Column(length = EMAIL_LENGTH_MAX, nullable = false, unique = true)
 	private String email;
 	
 	@Column(length = PASSWORD_LENGTH_MAX)
 	private String passwort;
 	
 	@OneToOne(cascade = { PERSIST, REMOVE }, mappedBy = "kunde")
 	@Valid
 	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
 	private Adresse adresse;
 
 	@OneToMany
 	@JoinColumn(name = "kunde_fk", nullable = false)
 	@OrderColumn(name = "idx", nullable = false)
 	@XmlTransient
 	private List<Bestellung> bestellungen;
 	
 	@Transient
 	private URI bestellungenUri;
 	
 	@ElementCollection(fetch = EAGER)
 	@CollectionTable(name = "kunde_rolle",
 	                 joinColumns = @JoinColumn(name = "kunde_fk", nullable = false),
    	                 uniqueConstraints =  @UniqueConstraint(columnNames = { "kunde_fk", "rolle" }))
 	@Column(table = "kunde_rolle", name = "rolle", length = 32, nullable = false)
 	private Set<RolleType> rollen;
 	
 	@OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
 	@JoinColumn(name = "file_fk")
 	@XmlTransient
 	private File file;
 	
 	@Basic(optional = false)
 	@Temporal (TIMESTAMP)
 	@XmlElement(name = "datum")
 	private Date erzeugt;	
 	
 	@Basic(optional = false)
 	@Temporal (TIMESTAMP)
 	@XmlTransient
 	private Date aktualisiert;
 	
 	@PrePersist
 	protected void prePersist() {
 		erzeugt = new Date();
 		aktualisiert = new Date();
 	}
 	
 	@PostPersist
 	protected void postPersist() {
 		LOGGER.debugf("Neuer Kunde mit ID=%d", id);
 	}
 	
 	@PreUpdate
 	protected void preUpdate() {
 		aktualisiert = new Date();
 	}
 	
 	
 	public void setValues(Kunde k) {
 		nachname = k.nachname;
 		vorname = k.vorname;
 		email = k.email;
 		passwort = k.passwort;
 	}
 	
 	public File getFile() {
 		return file;
 	}
 	
 	public void setFile(File file) {
 		this.file = file;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 
 	public String getVorname() {
 		return vorname;
 	}
 
 	public void setVorname(String vorname) {
 		this.vorname = vorname;
 	}
 
 	public String getGeschlecht() {
 		return geschlecht;
 	}
 
 	public void setGeschlecht(String geschlecht) {
 		this.geschlecht = geschlecht;
 	}
 
 	public String getPasswort() {
 		return passwort;
 	}
 	public void setPasswort(String passwort) {
 		this.passwort = passwort;
 	}
 	public String getNachname() {
 		return nachname;
 	}
 	public void setNachname(String nachname) {
 		this.nachname = nachname;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public Adresse getAdresse() {
 		return adresse;
 	}
 	public void setAdresse(Adresse adresse) {
 		this.adresse = adresse;
 	}
 	public List<Bestellung> getBestellungen() {
 		if (bestellungen == null) {
 			return null;
 		}		
 		return Collections.unmodifiableList(bestellungen);
 	}
 	public void setBestellungen(List<Bestellung> bestellungen) {
 		if (this.bestellungen == null) {
 			this.bestellungen = bestellungen;
 			return;
 		}
 		
 		// Wiederverwendung der vorhandenen Collection
 		this.bestellungen.clear();
 		if (bestellungen != null) {
 			this.bestellungen.addAll(bestellungen);
 		}
 	}
 	public Kunde addBestellung(Bestellung bestellung) {
 		if (bestellungen == null) {
 			bestellungen = new ArrayList<>();
 		}
 		bestellungen.add(bestellung);
 		return this;
 	}
 	public URI getBestellungenUri() {
 		return bestellungenUri;
 	}
 	public void setBestellungenUri(URI bestellungenUri) {
 		this.bestellungenUri = bestellungenUri;
 	}
 	public Set<RolleType> getRollen() {
 		if (rollen == null) {
 			return null;
 		}
 		
 		return Collections.unmodifiableSet(rollen);
 	}
 	public void setRollen(Set<RolleType> rollen) {
 		if (this.rollen == null) {
 			this.rollen = rollen;
 			return;
 		}
 		
 		// Wiederverwendung der vorhandenen Collection
 		this.rollen.clear();
 		if (rollen != null) {
 			this.rollen.addAll(rollen);
 		}
 	}
 	
 	public Kunde addRollen(Collection<RolleType> rollen) {
 		LOGGER.tracef("neue Rollen: %s", rollen);
 		if (this.rollen == null) {
 			this.rollen = new HashSet<>();
 		}
 		this.rollen.addAll(rollen);
 		LOGGER.tracef("Rollen nachher: %s", this.rollen);
 		return this;
 	}
 	
 	public Kunde removeRollen(Collection<RolleType> rollen) {
 		LOGGER.tracef("zu entfernende Rollen: %s", rollen);
 		if (this.rollen == null) {
 			return this;
 		}
 		this.rollen.removeAll(rollen);
 		LOGGER.tracef("Rollen nachher: %s", this.rollen);
 		return this;
 	}
 	public Date getErzeugt() {
 		return erzeugt == null ? null : (Date) erzeugt.clone();
 	}
 	public void setErzeugt(Date erzeugt) {
 		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
 	}
 	public Date getAktualisiert() {
 		return aktualisiert == null ? null : (Date) erzeugt.clone();
 	}
 	public void setAktualisiert(Date aktualisiert) {
 		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
 		final Kunde other = (Kunde) obj;
 		if (email == null) {
 			if (other.email != null)
 				return false;
 		}
 		else if (!email.equals(other.email))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Kunde [id=" + id + ", vorname=" + vorname + ", nachname=" + nachname
 				+ ", geschlecht=" + geschlecht + ", email=" + email + ", bestellungenUri=" + bestellungenUri + "]";
 	}
 }
