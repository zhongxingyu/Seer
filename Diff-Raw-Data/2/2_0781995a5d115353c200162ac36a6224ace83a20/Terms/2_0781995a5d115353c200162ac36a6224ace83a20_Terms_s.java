 package cz.cvut.fit.mi_mpr_dip.admission.domain.collection;
 
 import java.util.Set;
 
 import javax.validation.Valid;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.springframework.roo.addon.equals.RooEquals;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 
import com.sun.xml.xsom.impl.Ref.Term;
 
 @RooJavaBean
 @RooToString
 @RooEquals
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class Terms extends DomainCollection {
 
 	@Valid
 	@XmlElement(name = "term")
 	private Set<Term> terms;
 }
