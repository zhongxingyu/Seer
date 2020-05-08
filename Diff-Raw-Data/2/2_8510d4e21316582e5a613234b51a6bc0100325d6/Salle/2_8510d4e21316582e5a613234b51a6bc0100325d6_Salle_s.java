 package com.jcertif.bo.salle;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 import com.jcertif.bo.AbstractBO;
 import com.jcertif.bo.conference.CentreConference;
 
 /**
  * BO Salle.
  * 
  * @author rossi.oddet
  * 
  */
 @Entity
 @XmlRootElement
 public class Salle extends AbstractBO {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Identifiant d'une salle.
 	 */
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	/**
 	 * Libelle d'une salle.
 	 */
 	@Column
 	private String libelle;
 
 	/**
 	 * Description d'une salle.
 	 */
 	@Column
 	private String description;
 
 	/**
 	 * Nombre de place d'une salle.
 	 */
 	@Column
 	private Integer nombrePlace;
 
 	/**
 	 * Dtails d'une salle.
 	 */
 	@Column
 	private String details;
 
 	/**
 	 * Centre de confrence auquel appartient la salle.
 	 */
 	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 	@JoinColumn(name = "centre_conference_id")
 	private CentreConference centreConference;
 	
 	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name ="salle_paritularite_salle", joinColumns = @JoinColumn(name = "salle_id"), inverseJoinColumns = @JoinColumn(name = "particularite_salle_id"))
 	private Set<ParticulariteSalle> particularitesalles=new HashSet<ParticulariteSalle>();
 	
 	/**
 	 * Contructeur par dfaut.
 	 */
 	public Salle() {
 		super();
 	}
 
 	/**
 	 * Un constructeur.
 	 * 
 	 * @param libelle
 	 *            un libell
 	 * @param description
 	 *            une description
 	 */
 	public Salle(String libelle, String description) {
 		super();
 		this.libelle = libelle;
 		this.description = description;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the libelle
 	 */
 	public String getLibelle() {
 		return libelle;
 	}
 
 	/**
 	 * @param libelle
 	 *            the libelle to set
 	 */
 	public void setLibelle(String libelle) {
 		this.libelle = libelle;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the nombrePlace
 	 */
 	public Integer getNombrePlace() {
 		return nombrePlace;
 	}
 
 	/**
 	 * @param nombrePlace
 	 *            the nombrePlace to set
 	 */
 	public void setNombrePlace(Integer nombrePlace) {
 		this.nombrePlace = nombrePlace;
 	}
 
 	/**
 	 * @return the details
 	 */
 	public String getDetails() {
 		return details;
 	}
 
 	/**
 	 * @param details
 	 *            the details to set
 	 */
 	public void setDetails(String details) {
 		this.details = details;
 	}
 
 	/**
 	 * @return the centreConference
 	 */
 	public CentreConference getCentreConference() {
 		return centreConference;
 	}
 
 	/**
 	 * @param centreConference
 	 *            the centreConference to set
 	 */
 	public void setCentreConference(CentreConference centreConference) {
 		this.centreConference = centreConference;
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder().append(libelle).append(description)
 				.toHashCode();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj) {
 			return true;
 		}
 
 		if (!(obj instanceof Salle)) {
 			return false;
 		}
 
 		final Salle other = (Salle) obj;
 
 		return new EqualsBuilder().append(libelle, other.getLibelle())
 				.append(description, other.getDescription()).isEquals();
 	}
 
 	/**
 	 * @return the particularitesallesTODO
 	 */
 	public Set<ParticulariteSalle> getParticularitesalles() {
 		return particularitesalles;
 	}
 
 	/**
 	 * @param particularitesalles the particularitesalles to setTODOparticularitesalles
 	 */
 	public void setParticularitesalles(Set<ParticulariteSalle> particularitesalles) {
 		this.particularitesalles = particularitesalles;
 	}
 
 }
