 package cz.cvut.fit.mi_mpr_dip.admission.domain;
 
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Version;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.roo.addon.equals.RooEquals;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import cz.cvut.fit.mi_mpr_dip.admission.domain.study.Programme;
 
 @RooJavaBean
 @RooToString
 @RooEquals(excludeFields = { "termId", "programs", "registrations" })
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement
 @RooJpaActiveRecord(finders = { "findTermsByDateOfTermEqualsAndRoomEquals" })
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "dateOfTerm", "room" }))
 public class Term {
 
 	@Version
 	@Transient
 	@XmlTransient
 	private int version;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@XmlTransient
 	private Long termId;
 
 	@NotNull
 	private Date dateOfTerm;
 
 	@NotEmpty
 	@NotNull
 	private String room;
 
 	@NotNull
 	private Integer capacity;
 
 	@NotNull
 	private Date registerFrom;
 
 	@NotNull
 	private Date registerTo;
 
 	@NotNull
 	private Date apologyTo;
 
 	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, fetch = FetchType.EAGER)
 	@JoinTable(name = "term_programme", joinColumns = { @JoinColumn(name = "term_id", referencedColumnName = "termId") }, inverseJoinColumns = { @JoinColumn(name = "programme_id", referencedColumnName = "programmeId") })
 	@Valid
 	@XmlElementWrapper(name = "programs")
 	@XmlElement(name = "programme")
 	private Set<Programme> programs;
 
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, fetch = FetchType.EAGER, mappedBy = "term")
 	@XmlElementWrapper(name = "registrations")
 	@XmlElement(name = "registration")
 	private Set<TermRegistration> registrations;
 
 	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
 	@Valid
 	private TermType termType;
 }
