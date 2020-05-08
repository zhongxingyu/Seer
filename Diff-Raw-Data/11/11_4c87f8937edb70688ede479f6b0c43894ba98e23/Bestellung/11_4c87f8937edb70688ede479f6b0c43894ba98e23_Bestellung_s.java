 package de.webshop.bestellverwaltung.domain;
 
 import static javax.persistence.CascadeType.PERSIST;
 import static javax.persistence.CascadeType.REMOVE;
 import static javax.persistence.FetchType.EAGER;
 import static javax.persistence.TemporalType.DATE;
 import java.lang.invoke.MethodHandles;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Index;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.persistence.PostPersist;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.jboss.logging.Logger;
 import de.webshop.kundenverwaltung.domain.Kunde;
 import de.webshop.util.persistence.AbstractAuditable;
 
 @XmlRootElement
 @Entity
 @Table(indexes = {
 		@Index(columnList = "kunde_fk"),
 		@Index(columnList = "erzeugt")
 	})
 @NamedQueries({
 	@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_KUNDE,
 			query = "SELECT b"
					+ "FROM Bestellung b "
					+ "WHERE b.kunde = :" + Bestellung.PARAM_KUNDE),
 	@NamedQuery(name  = Bestellung.FIND_KUNDE_BY_ID,
 	    	query = "SELECT b.kunde"
 	    			+ " FROM   Bestellung b"
 		            + " WHERE  b.id = :" + Bestellung.PARAM_ID)
 })
 public class Bestellung extends AbstractAuditable {
 	
 	private static final long serialVersionUID = 8645031681820165535L;
 	
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	private static final String PREFIX = "Bestellung.";
 	public static final String FIND_BESTELLUNGEN_BY_KUNDE = PREFIX + "findBestellungenByKunde";
 	public static final String FIND_KUNDE_BY_ID = PREFIX + "findBestellungKundeById";
 	
 	public static final String PARAM_KUNDE = "kunde";
 	public static final String PARAM_ID = "id";
 	
 	@Id
 	@GeneratedValue
 	@Basic(optional = false)
 	@Min(value = 1, message = "{bestellverwaltung.bestellung.id.min}")
 	private Long id = null;
 	
 	@ManyToOne
 	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
 	@OrderColumn(name = "idx")
 	@XmlTransient
 	private Kunde kunde;
 	
 	@Transient
 	private URI	kundeUri;
 	
 	@Temporal(DATE)
 	@Column(updatable = false)
 	@NotNull(message = "{bestellverwaltung.bestellung.date.notNull}")
 	private Date bestelldatum;
 	
 	@OneToMany(fetch = EAGER, cascade = { PERSIST, REMOVE })
 	@JoinColumn(name = "bestellung_fk", nullable = false)
 	@NotEmpty(message = "{bestellverwaltung.bestellung.positionen.notEmpty}")
 	@Valid
 	private List<Position> positionen;
 	
 	
 	public Bestellung() {
 		super();
 	}
 	
 	public Bestellung(List<Position> positionen) {
 		super();
 		this.positionen = positionen;
 	}
 	
 	@PostPersist
 	private void postPersist() {
 		LOGGER.debugf("Neue Bestellung mit ID = %d", id);
 	}
 	
 	@XmlElement
 	public Date getDatum() {
 		return getErzeugt();
 	}
 	
 	public void setDatum(Date datum) {
 		setErzeugt(datum);
 	}
 	
 	public Long getID() {
 		return id;
 	}
 	
 	public void setID(Long id) {
 		this.id = id;
 	}
 	
 	public Kunde getKunde() {
 		return kunde;
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
 	
 	public Date getBestelldatum() {
 		return bestelldatum;
 	}
 	
 	public void setBestelldatum(Date bestelldatum) {
 		this.bestelldatum = bestelldatum;
 	}
 	
 	public List<Position> getPositionen() {
 		return positionen;
 	}
 	
 	public void setPositionen(List<Position> positionen) {
 		if(this.positionen == null) {
 			this.positionen = positionen;
 			return;
 		}
 		
 		this.positionen.clear();
 		if(positionen != null) {
 			this.positionen.addAll(positionen);
 		}
 	}
 	
 	public Bestellung addPosition(Position position) {
 		if (positionen == null) {
 			positionen = new ArrayList<Position>();
 		}
 		positionen.add(position);
 		return this;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((bestelldatum == null) ? 0 : bestelldatum.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((kunde == null) ? 0 : kunde.hashCode());
 		result = prime * result + ((kundeUri == null) ? 0 : kundeUri.hashCode());
 		result = prime * result + ((positionen == null) ? 0 : positionen.hashCode());
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
 		if (bestelldatum == null) {
 			if (other.bestelldatum != null)
 				return false;
 		}
 		else if (!bestelldatum.equals(other.bestelldatum))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		}
 		else if (!id.equals(other.id))
 			return false;
 		if (kunde == null) {
 			if (other.kunde != null)
 				return false;
 		}
 		else if (!kunde.equals(other.kunde))
 			return false;
 		if (kundeUri == null) {
 			if (other.kundeUri != null)
 				return false;
 		}
 		else if (!kundeUri.equals(other.kundeUri))
 			return false;
 		if (positionen == null) {
 			if (other.positionen != null)
 				return false;
 		}
 		else if (!positionen.equals(other.positionen))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Bestellung [id=" + id + ", kunde=" + kunde + ", kundeUri=" + kundeUri
 				+ ", bestelldatum=" + bestelldatum + ", positionen=" + positionen + "]";
 	}
 	
 }
