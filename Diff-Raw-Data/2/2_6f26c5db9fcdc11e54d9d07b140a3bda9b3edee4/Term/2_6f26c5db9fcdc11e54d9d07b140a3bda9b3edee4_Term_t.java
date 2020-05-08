 package cz.cvut.fit.mi_mpr_dip.admission.domain;
 
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.springframework.roo.addon.equals.RooEquals;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import cz.cvut.fit.mi_mpr_dip.admission.domain.study.Programme;
 
 @RooJavaBean
 @RooToString
 @RooEquals(excludeFields = { "termId" })
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement
 @RooJpaActiveRecord
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
 	private String room;
 	
 	@NotNull
	private Date dateAndTime;
 
 	@NotNull
 	private Integer capacity;
 
 	@NotNull
 	private Date registerFrom;
 
 	@NotNull
 	private Date registerTo;
 
 	@NotNull
 	private Date appologyTo;
 
 	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	private Set<Programme> programs;
 
 	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	private Set<Admission> admissions;
 
 	@ManyToOne(cascade = CascadeType.ALL)
 	private TermType termType;
 }
