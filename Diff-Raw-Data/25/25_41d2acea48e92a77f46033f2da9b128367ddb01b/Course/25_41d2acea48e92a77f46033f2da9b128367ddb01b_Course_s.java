 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.models;
 
 import adg.red.utils.RedEntityManager;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author harsimran.maan
  */
 @Entity
 @Table(name = "Course")
 @XmlRootElement
 @NamedQueries(
         {
     @NamedQuery(name = "Course.findAll", query = "SELECT c FROM Course c"),
     @NamedQuery(name = "Course.findByCourseNumber", query = "SELECT c FROM Course c WHERE c.courseNumber = :courseNumber"),
     @NamedQuery(name = "Course.findByName", query = "SELECT c FROM Course c WHERE c.name = :name"),
     @NamedQuery(name = "Course.findByDescription", query = "SELECT c FROM Course c WHERE c.description = :description"),
     @NamedQuery(name = "Course.findByCredits", query = "SELECT c FROM Course c WHERE c.credits = :credits"),
     @NamedQuery(name = "Course.findByPassingRequirement", query = "SELECT c FROM Course c WHERE c.passingRequirement = :passingRequirement"),
     @NamedQuery(name = "Course.findByIsActive", query = "SELECT c FROM Course c WHERE c.isActive = :isActive"),
     @NamedQuery(name = "Course.findByDepartment", query = "SELECT c FROM Course c WHERE c.departmentId = :departmentId")
 })
 public class Course implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     @Id
     @Basic(optional = false)
     @Column(name = "courseNumber")
     private Integer courseNumber;
     @Basic(optional = false)
     @Column(name = "name")
     private String name;
     @Basic(optional = false)
     @Column(name = "description")
     private String description;
     @Basic(optional = false)
     @Column(name = "credits")
     private int credits;
     @Basic(optional = false)
     @Column(name = "passingRequirement")
     private String passingRequirement;
     @Basic(optional = false)
     @Column(name = "isActive")
     private boolean isActive;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
     private Collection<CoRequisite> coRequisiteCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course1")
     private Collection<CoRequisite> coRequisiteCollection1;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course2")
     private Collection<CoRequisite> coRequisiteCollection2;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course3")
     private Collection<CoRequisite> coRequisiteCollection3;
     @JoinColumn(name = "gradingSchemeId", referencedColumnName = "gradingSchemeId")
     @ManyToOne(optional = false)
     private GradingScheme gradingSchemeId;
     @JoinColumn(name = "departmentId", referencedColumnName = "departmentId")
     @ManyToOne(optional = false)
     private Department departmentId;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
     private Collection<Prerequisite> prerequisiteCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course1")
     private Collection<Prerequisite> prerequisiteCollection1;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course2")
     private Collection<Prerequisite> prerequisiteCollection2;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course3")
     private Collection<Prerequisite> prerequisiteCollection3;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
     private Collection<Section> sectionCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "course1")
     private Collection<Section> sectionCollection1;
 
     public Course()
     {
     }
 
     public Course(Integer courseNumber)
     {
         this.courseNumber = courseNumber;
     }
 
     public Course(Integer courseNumber, String name, String description, int credits, String passingRequirement, boolean isActive)
     {
         this.courseNumber = courseNumber;
         this.name = name;
         this.description = description;
         this.credits = credits;
         this.passingRequirement = passingRequirement;
         this.isActive = isActive;
     }
 
     public Integer getCourseNumber()
     {
         return courseNumber;
     }
 
     public void setCourseNumber(Integer courseNumber)
     {
         this.courseNumber = courseNumber;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     public int getCredits()
     {
         return credits;
     }
 
     public void setCredits(int credits)
     {
         this.credits = credits;
     }
 
     public String getPassingRequirement()
     {
         return passingRequirement;
     }
 
     public void setPassingRequirement(String passingRequirement)
     {
         this.passingRequirement = passingRequirement;
     }
 
     public boolean getIsActive()
     {
         return isActive;
     }
 
     public void setIsActive(boolean isActive)
     {
         this.isActive = isActive;
     }
 
     @XmlTransient
     public Collection<CoRequisite> getCoRequisiteCollection()
     {
         return coRequisiteCollection;
     }
 
     public void setCoRequisiteCollection(Collection<CoRequisite> coRequisiteCollection)
     {
         this.coRequisiteCollection = coRequisiteCollection;
     }
 
     @XmlTransient
     public Collection<CoRequisite> getCoRequisiteCollection1()
     {
         return coRequisiteCollection1;
     }
 
     public void setCoRequisiteCollection1(Collection<CoRequisite> coRequisiteCollection1)
     {
         this.coRequisiteCollection1 = coRequisiteCollection1;
     }
 
     @XmlTransient
     public Collection<CoRequisite> getCoRequisiteCollection2()
     {
         return coRequisiteCollection2;
     }
 
     public void setCoRequisiteCollection2(Collection<CoRequisite> coRequisiteCollection2)
     {
         this.coRequisiteCollection2 = coRequisiteCollection2;
     }
 
     @XmlTransient
     public Collection<CoRequisite> getCoRequisiteCollection3()
     {
         return coRequisiteCollection3;
     }
 
     public void setCoRequisiteCollection3(Collection<CoRequisite> coRequisiteCollection3)
     {
         this.coRequisiteCollection3 = coRequisiteCollection3;
     }
 
     public GradingScheme getGradingSchemeId()
     {
         return gradingSchemeId;
     }
 
     public void setGradingSchemeId(GradingScheme gradingSchemeId)
     {
         this.gradingSchemeId = gradingSchemeId;
     }
 
     public Department getDepartmentId()
     {
         return departmentId;
     }
 
     public void setDepartmentId(Department departmentId)
     {
         this.departmentId = departmentId;
     }
 
     @XmlTransient
     public Collection<Prerequisite> getPrerequisiteCollection()
     {
         return prerequisiteCollection;
     }
 
     public void setPrerequisiteCollection(Collection<Prerequisite> prerequisiteCollection)
     {
         this.prerequisiteCollection = prerequisiteCollection;
     }
 
     @XmlTransient
     public Collection<Prerequisite> getPrerequisiteCollection1()
     {
         return prerequisiteCollection1;
     }
 
     public void setPrerequisiteCollection1(Collection<Prerequisite> prerequisiteCollection1)
     {
         this.prerequisiteCollection1 = prerequisiteCollection1;
     }
 
     @XmlTransient
     public Collection<Prerequisite> getPrerequisiteCollection2()
     {
         return prerequisiteCollection2;
     }
 
     public void setPrerequisiteCollection2(Collection<Prerequisite> prerequisiteCollection2)
     {
         this.prerequisiteCollection2 = prerequisiteCollection2;
     }
 
     @XmlTransient
     public Collection<Prerequisite> getPrerequisiteCollection3()
     {
         return prerequisiteCollection3;
     }
 
     public void setPrerequisiteCollection3(Collection<Prerequisite> prerequisiteCollection3)
     {
         this.prerequisiteCollection3 = prerequisiteCollection3;
     }
 
     @XmlTransient
     public Collection<Section> getSectionCollection()
     {
         return sectionCollection;
     }
 
     public void setSectionCollection(Collection<Section> sectionCollection)
     {
         this.sectionCollection = sectionCollection;
     }
 
     @XmlTransient
     public Collection<Section> getSectionCollection1()
     {
         return sectionCollection1;
     }
 
     public void setSectionCollection1(Collection<Section> sectionCollection1)
     {
         this.sectionCollection1 = sectionCollection1;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 0;
         hash += (courseNumber != null ? courseNumber.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object)
     {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Course))
         {
             return false;
         }
         Course other = (Course) object;
         if ((this.courseNumber == null && other.courseNumber != null) || (this.courseNumber != null && !this.courseNumber.equals(other.courseNumber)))
         {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString()
     {
         return "adg.red.models.Course[ courseNumber=" + courseNumber + " ]";
     }
 
     public static List<Course> getByDepartment(Department department)
     {
         return RedEntityManager.getEntityManager().createNamedQuery("Course.findByDepartment").setParameter("departmentId", department).getResultList();
     }
 }
