 package cz.cvut.fit.mi_mpr_dip.admission.domain;
 
 import javax.persistence.CascadeType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
import javax.persistence.Table;
 import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
 import javax.persistence.Version;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.springframework.roo.addon.equals.RooEquals;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooEquals(excludeFields = { "termRegistrationId", "admission" })
 @XmlAccessorType(XmlAccessType.FIELD)
 @RooJpaActiveRecord
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "admission", "term" }))
 public class TermRegistration {
 
 	@Version
 	@Transient
 	@XmlTransient
 	private int version;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@XmlTransient
 	private Long termRegistrationId;
 
 	@NotNull
 	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
 	private Admission admission;
 
 	@NotNull
 	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
 	@Valid
 	private Term term;
 
 	@Transient
 	@OneToOne(mappedBy = "termRegistration", cascade = CascadeType.ALL)
 	@Valid
 	private Apology apology;
 
	private Boolean attended;
 }
