 package de.shop.kundenverwaltung.domain;
 
 import static de.shop.util.Constants.MIN_ID;
 import static javax.persistence.CascadeType.PERSIST;
 import static javax.persistence.CascadeType.REMOVE;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 
//TODO methode testen
 import javax.persistence.Column;
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
 import javax.persistence.PostUpdate;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.validator.constraints.Email;
 import org.jboss.logging.Logger;
 
 import de.shop.bestellverwaltung.domain.Bestellung;
 import de.shop.util.IdGroup;
 
 
 @Entity
 @Table(name = "kunde")
 @NamedQueries({
 	@NamedQuery(name = Kunde.FIND_KUNDEN,
 				query = "SELECT k"
 						+ " FROM Kunde k"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME,
             query = "SELECT k"
 			        + " FROM   Kunde k"
             		+ " WHERE  UPPER(k.nachname) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"),
     @NamedQuery(name  = Kunde.FIND_KUNDEN_BY_ID,
                     query = "SELECT k"
         			        + " FROM   Kunde k"
                     		+ " WHERE k.id = :" + Kunde.PARAM_KUNDE_ID),
    	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL,
        query = "SELECT DISTINCT k"
 	            + " FROM   Kunde k"
 	            + " WHERE  k.email = :" + Kunde.PARAM_KUNDE_EMAIL)
 })
 
 public class Kunde implements Serializable {
 	private static final long serialVersionUID = 7401524595142572933L;
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	public static final String PRIVATKUNDE = "P";
 	public static final String FIRMENKUNDE = "F";
 	
 	private static final String PREFIX = "Kunde.";
 	public static final String FIND_KUNDEN = PREFIX + "findKunden";
 	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
 	public static final String FIND_KUNDEN_BY_ID = PREFIX + "findKundenById";
 	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
 	
 	public static final String PARAM_KUNDE_NACHNAME = "nachname";
 	public static final String PARAM_KUNDE_ID = "id";
 	public static final String PARAM_KUNDE_EMAIL = "email";
 	
 	//Pattern mit UTF-8 (statt Latin-1 bzw. ISO-8859-1) Schreibweise fuer Umlaute:
 	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
 	private static final String NACHNAME_PREFIX = "(o'|von|von der|von und zu|van)?";
 	
 	public static final String NACHNAME_PATTERN = NACHNAME_PREFIX + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
 	public static final int NACHNAME_LENGTH_MIN = 2;
 	public static final int NACHNAME_LENGTH_MAX = 32;
 	public static final int EMAIL_LENGTH_MAX = 128;
 	
 	@Id
 	@GeneratedValue
 	@Column(nullable = false, updatable = false)
 	@Min(value = MIN_ID, message = "{kundenverwaltung.kunde.id.min}", groups = IdGroup.class)
 	private Long id;
 	
 	@Column(name = "name", length = NACHNAME_LENGTH_MAX, nullable = false)
 	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
 	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.nachname.length}")
 	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
 	private String nachname;
 	
 	@Column(length = EMAIL_LENGTH_MAX, nullable = false, unique = true)
 	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
 	@NotNull(message = "{kundenverwaltung.kunde.email.notNull}")
 	@Size(max = EMAIL_LENGTH_MAX, message = "{kundenverwaltung.kunde.email.length}")
 	private String email;
 	
 	@Past(message = "{kundenverwaltung.kunde.erstellt.past}")
 	private Date erstellt;
 	
 	@Past(message = "{kundenverwaltung.kunde.aktualisiert.past}")
 	private Date aktualisiert;
 	
 	@OneToOne(cascade = { PERSIST, REMOVE }, mappedBy = "kunde")
 	@Valid
 	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
 	private Adresse adresse;
 	
 	@OneToMany
 	@JoinColumn(name = "kunde_fk", nullable = false)
 	@OrderColumn(name = "idx", nullable = false)
 	@JsonIgnore
 	private List<Bestellung> bestellungen;
 	
 	@Transient
 	private URI bestellungenUri;
 	
 	@PrePersist
 	protected void prePersist() {
 		erstellt = new Date();
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
 	
 	@PostUpdate
 	protected void postUpdate() {	
 		LOGGER.debugf("Kunde mit ID=%d aktualisiert.", id);
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
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
 	public Date getErstellt() {
 		return erstellt == null ? null : (Date) erstellt.clone();
 	}
 	public void setSeit(Date seit) {
 		this.erstellt = seit == null ? null : (Date) erstellt.clone();
 	}
 	public Adresse getAdresse() {
 		return adresse;
 	}
 	public void setAdresse(Adresse adresse) {
 		this.adresse = adresse;
 	}
 	public List<Bestellung> getBestellungen() {
 		return bestellungen;
 	}
 	public void setBestellungen(List<Bestellung> bestellungen) {
 		this.bestellungen = bestellungen;
 	}
 	public URI getBestellungenUri() {
 		return bestellungenUri;
 	}
 	public void setBestellungenUri(URI bestellungenUri) {
 		this.bestellungenUri = bestellungenUri;
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
 		return "Kunde [id=" + id + ", nachname=" + nachname + ", email="
 				+ email + ", erstellt=" + erstellt + ", aktualisiert="
 				+ aktualisiert + ", adresse=" + adresse + ", bestellungen="
 				+ bestellungen + ", bestellungenUri=" + bestellungenUri + "]";
 	}
 }
