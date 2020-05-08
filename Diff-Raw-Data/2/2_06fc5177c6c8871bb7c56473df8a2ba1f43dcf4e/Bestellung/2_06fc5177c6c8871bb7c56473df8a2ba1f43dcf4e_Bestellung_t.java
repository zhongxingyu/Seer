 package de.shop.bestellverwaltung.domain;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.PostPersist;
 import javax.persistence.PostUpdate;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import static de.shop.util.Constants.ERSTE_VERSION;
 import static de.shop.util.Constants.MIN_ID;
 import static javax.persistence.TemporalType.TIMESTAMP;
 import static javax.persistence.CascadeType.PERSIST;
 import static javax.persistence.CascadeType.REMOVE;
 import static javax.persistence.EnumType.STRING;
 
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.jboss.logging.Logger;
 
 import de.shop.kundenverwaltung.domain.Kunde;
 import de.shop.util.IdGroup;
 import de.shop.util.PreExistingGroup;
 import de.shop.util.TechnicalDate;
 
 import java.util.List;
 
 
 
 /**
  * The persistent class for the bestellung database table.
  * 
  */
 
 @Entity
 @Table (name = "bestellung")
 @NamedQueries({
 	@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_STATUS,
     query = "SELECT b"
 			+ " FROM   Bestellung b"
             + " WHERE  b.status = :" + Bestellung.PARAM_BESTELLUNG_STATUS),
             
     @NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_BESTELLPOSITIONEN,
     query = "SELECT DISTINCT b"
             + " FROM   Bestellung b"
 		    + " JOIN FETCH b.bestellpositionen"
 		    + " WHERE  b.id = :" + Bestellung.PARAM_ID),  
            
     @NamedQuery(name = Bestellung.FIND_ALL_BESTELLUNGEN,
     query = "SELECT b"
             + " FROM   Bestellung b"),   
             
     @NamedQuery(name = Bestellung.FIND_ALL_BESTELLUNGEN_FETCH_BESTELLPOSITIONEN,
     query = "SELECT b"
             + " FROM   Bestellung b"
             + " JOIN FETCH b.bestellpositionen"),         
             		
 	@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_LIEFERVERFOLGUNGSNUMMER,
     query = "SELECT b"
 	        + " FROM   Bestellung b"
     		+ " WHERE  b.lieferverfolgungsnummer = :" + Bestellung.PARAM_BESTELLUNG_LIEFERVERFOLGUNGSNUMMER),
     		
     @NamedQuery(name = Bestellung.FIND_KUNDE_BY_ID,
 	query = "SELECT b.kunde"
             + " FROM   Bestellung b"
 		    + " WHERE  b.id = :" + Bestellung.PARAM_ID),
 		    
 	@NamedQuery(name  = Bestellung.FIND_BESTELLUNGEN_BY_KUNDE,
 	query = "SELECT b"
 		    + " FROM   Bestellung b"
			+ " WHERE  b.kunde.kundeid = :" + Bestellung.PARAM_KUNDEID)	    
 })
 
 @Cacheable
 public class Bestellung implements Serializable {
 	
 	private static final String PREFIX = "Bestellung.";
 	public static final String FIND_ALL_BESTELLUNGEN = PREFIX + "findAllBestellungen";
 	public static final String FIND_ALL_BESTELLUNGEN_FETCH_BESTELLPOSITIONEN = 
 							   		PREFIX 
 							   		+ "findAllBestellungenFetchBestellpositionen";
 	public static final String FIND_BESTELLUNG_BY_ID_FETCH_BESTELLPOSITIONEN = 
 									PREFIX 
 									+ "findBestellungByIdFetchBestellpositionen";
 	public static final String FIND_BESTELLUNGEN_BY_STATUS = PREFIX 
 														   + "findBestellungenByStatus";
 	public static final String FIND_BESTELLUNGEN_BY_LIEFERVERFOLGUNGSNUMMER = 
 									PREFIX 
 									+ "findBestellungenByLieferverfolgungsnummer";
 	public static final String FIND_KUNDE_BY_ID = PREFIX + "findBestellungKundeById";
 	public static final String FIND_BESTELLUNGEN_BY_KUNDE = PREFIX + "findBestellungenByKunde";
 	
 	public static final String PARAM_BESTELLUNG_STATUS = "status";
 	public static final String PARAM_BESTELLUNG_LIEFERVERFOLGUNGSNUMMER = "lieferverfolgungsnummer";
 	public static final String PARAM_ID = "id";
 	public static final String PARAM_KUNDEID = "KundeId";
 	
 	private static final long serialVersionUID = 6704238277609138074L;
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	@Id
 	@GeneratedValue
 	@Column(name = "bestell_id", unique = true, nullable = false, updatable = false)
 	@Min(value = MIN_ID, message = "{bestellverwaltung.bestellung.id.min}", groups = IdGroup.class)
 	private Long id;
 	
 	@Version
 	@Basic(optional = false)
 	private int version = ERSTE_VERSION;
 	
 	@Column(name = "status")
 	@Enumerated(STRING)
 	private BestellstatusType status;
 	
 	@Column(length = 45)
 	private String lieferverfolgungsnummer;
 	
 	@Column(nullable = false)
 	@NotNull(message = "{bestellverwaltung.bestellung.erzeugt.notNull}", groups = TechnicalDate.class)
 	@Temporal(TIMESTAMP)
 	@JsonIgnore
 	private Date erzeugt;
 	
 	@Column(nullable = false)
 	@NotNull(message = "{bestellverwaltung.bestellung.aktualisiert.notNull}", groups = TechnicalDate.class)
 	@Temporal(TIMESTAMP)
 	@JsonIgnore
 	private Date aktualisiert;
 
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "kunde_id")//, nullable = false, insertable = false, updatable = false)
 	@NotNull(message = "{bestellverwaltung.bestellung.kunde.notNull}", groups = PreExistingGroup.class)
 	@JsonIgnore
 	private Kunde kunde;
 	
 	@Transient
 	private URI kundeUri;
 
 	@OneToMany(cascade = { PERSIST, REMOVE })
 	@JoinColumn(name = "bestell_id", nullable = false, updatable = true)
 	//@OrderColumn(name = "idx", nullable = false)
 	//ToDo @NotEmpty lst Fehler aus in BestellungTest.java, da leere Bestellung angelegt wird
 	//@NotEmpty(message = "{bestellverwaltung.bestellung.bestellpositionen.notEmpty}")
 	@Valid
 	private List<Bestellposition> bestellpositionen;
 	
 	public Bestellung() {
 		super();
 	}
 	
 	public Bestellung(List<Bestellposition> bestellpositionen) {
 		super();
 		this.bestellpositionen = bestellpositionen;
 	}
 	
 	//@SuppressWarnings("unused")
 	@PrePersist
 	private void prePersist() {
 		erzeugt = new Date();
 		aktualisiert = new Date();
 	}
 	
 	@PostPersist
 	private void postPersist() {
 		LOGGER.debugf("Neue Bestellund mit ID=%d", id);
 	}
 	
 	//@SuppressWarnings("unused")
 	@PreUpdate
 	private void preUpdate() {
 		aktualisiert = new Date();
 	}
 	
 	@PostUpdate
 	private void postUpdate() {
 		LOGGER.debugf("Bestellung mit ID=%d aktualisiert: version=%d", id, version);
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long bestellId) {
 		this.id = bestellId;
 	}
 
 
 	public Date getAktualisiert() {
 		return this.aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 
 	public void setAktualisiert(Date aktualisiert) {
 		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 	
 	public Date getErzeugt() {
 		return aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 	
 	public void setErzeugt(Date erzeugt) {
 		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
 	}
 
 
 	public Kunde getKunde() {
 		return this.kunde;
 	}
 
 	public void setKunde(Kunde kunde) {
 		this.kunde = kunde;
 	}
 	
 	public URI getKundeUri() {
 		return kundeUri;
 	}
 	
 	public void setKundeUri(URI kundeUri) {
 		this.kundeUri = kundeUri;
 	}
 
 	public String getLieferverfolgungsnummer() {
 		return this.lieferverfolgungsnummer;
 	}
 
 	public void setLieferverfolgungsnummer(String lieferverfolgungsnummer) {
 		this.lieferverfolgungsnummer = lieferverfolgungsnummer;
 	}
 
 	public BestellstatusType getStatus() {
 		return this.status;
 	}
 
 	public void setStatus(BestellstatusType status) {
 		this.status = status;
 	}
 	
 	public List<Bestellposition> getBestellpositionen() {
 		return bestellpositionen == null ? null : Collections.unmodifiableList(bestellpositionen);
 	}
 	
 	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
 		if (this.bestellpositionen == null) {
 			this.bestellpositionen = bestellpositionen;
 			return;
 		}
 
 		this.bestellpositionen.clear();
 		if (bestellpositionen != null) {
 			this.bestellpositionen.addAll(bestellpositionen);
 		}
 	}
 	
 	public Bestellung addBestellposition(Bestellposition bestellposition) {
 		if (bestellpositionen == null) {
 			bestellpositionen = new ArrayList<>();
 		}
 		bestellpositionen.add(bestellposition);
 		return this;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
 		result = prime * result + ((kunde == null) ? 0 : kunde.hashCode());	
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
 		
 		final Bestellung other = (Bestellung) obj;
 
 		if (erzeugt == null) {
 			if (other.erzeugt != null)
 				return false;
 		} 
 		else if (!erzeugt.equals(other.erzeugt))
 			return false;
 		if (kunde == null) {
 			if (other.kunde != null)
 				return false;
 		} 
 		else if (!kunde.equals(other.kunde))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Bestellung [id=" + id + ", aktualisiert="
 				+ aktualisiert + ", erzeugt=" + erzeugt
 				+ ", lieferverfolgungsnummer="
 				+ lieferverfolgungsnummer + ", status=" + status + "]";
 	}
 	
 }
