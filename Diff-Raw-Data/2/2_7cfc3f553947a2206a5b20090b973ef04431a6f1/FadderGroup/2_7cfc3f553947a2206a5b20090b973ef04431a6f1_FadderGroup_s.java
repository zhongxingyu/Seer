 package no.niths.domain.school;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.constants.MiscConstants;
 import no.niths.domain.Domain;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 /**
  * Class that holds information about a student orientation group 
  *
  *
  */
@XmlRootElement
 @Entity
 @Table(name = MiscConstants.FADDER_GROUPS)
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 @XmlAccessorType(XmlAccessType.FIELD)  
 public class FadderGroup implements Domain {
 
 	@Transient
 	private static final long serialVersionUID = -3434555328809873472L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 		
 	@Column(name="group_number", unique=true)
 	@XmlElement(name="groupnumber")
 	private Integer groupNumber;
 	
 	@ManyToMany(fetch = FetchType.LAZY)
 	@Cascade(CascadeType.ALL)
 	@JoinTable(	name="fadder_leaders_students", 
 	uniqueConstraints={@UniqueConstraint(columnNames ={"fadder_groups_id", "leaders_id"})} )
 	private List<Student> leaders = new ArrayList<Student>();
 	
 	@OneToMany(fetch = FetchType.LAZY)
 	@Cascade(CascadeType.ALL)
 	@JoinTable(	name="fadder_children_students", 
 	uniqueConstraints={@UniqueConstraint(columnNames ={"fadder_groups_id", "fadderChildren_id"})} )
 	@XmlElement(name="fadderchildren")
 	private List<Student> fadderChildren = new ArrayList<Student>();
 	
 	
 	public FadderGroup() {
 		this(null);
 		setFadderChildren(null);
 		setLeaders(null);
 	}
 	
 	public FadderGroup(Integer groupNumber) {
 		this.groupNumber = groupNumber;
 	}
 	
 	public List<Student> getLeaders() {
 		return leaders;
 	}
 
 	public void setLeaders(List<Student> leaders) {
 		this.leaders = leaders;
 	}
 
 	public List<Student> getFadderChildren() {
 		return fadderChildren;
 	}
 
 	public void setFadderChildren(List<Student> fadderChildren) {
 		this.fadderChildren = fadderChildren;
 	}
 
 
 	public Integer getGroupNumber() {
 		return groupNumber;
 	}
 
 	public void setGroupNumber(Integer groupId) {
 		this.groupNumber = groupId;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof FadderGroup)) {
 			return false;
 		}
 		FadderGroup s = (FadderGroup) obj;
 		return s == this ? true : s.getId() == id ? true : false;
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("[%s][%d]", id, groupNumber);
 	}
 
 }
