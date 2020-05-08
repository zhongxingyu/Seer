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
 @Table(name = "Department")
 @XmlRootElement
 @NamedQueries(
         {
     @NamedQuery(name = "Department.findAll", query = "SELECT d FROM Department d"),
     @NamedQuery(name = "Department.findByDepartmentId", query = "SELECT d FROM Department d WHERE d.departmentId = :departmentId"),
     @NamedQuery(name = "Department.findByName", query = "SELECT d FROM Department d WHERE d.name = :name"),
     @NamedQuery(name = "Department.findByDirectorId", query = "SELECT d FROM Department d WHERE d.directorId = :directorId"),
     @NamedQuery(name = "Department.findByPhone", query = "SELECT d FROM Department d WHERE d.phone = :phone"),
     @NamedQuery(name = "Department.findByEmail", query = "SELECT d FROM Department d WHERE d.email = :email"),
     @NamedQuery(name = "Department.findByWebsite", query = "SELECT d FROM Department d WHERE d.website = :website"),
     @NamedQuery(name = "Department.findByIsActive", query = "SELECT d FROM Department d WHERE d.isActive = :isActive"),
    @NamedQuery(name = "Department.findByNameBeginsWith", query = "SELECT d FROM Department d WHERE d.name LIKE  :keyWord")
 })
 public class Department implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     @Id
     @Basic(optional = false)
     @Column(name = "departmentId")
     private String departmentId;
     @Basic(optional = false)
     @Column(name = "name")
     private String name;
     @Column(name = "directorId")
     private Integer directorId;
     @Basic(optional = false)
     @Column(name = "phone")
     private String phone;
     @Basic(optional = false)
     @Column(name = "email")
     private String email;
     @Basic(optional = false)
     @Column(name = "website")
     private String website;
     @Basic(optional = false)
     @Column(name = "isActive")
     private boolean isActive;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "departmentId")
     private Collection<Course> courseCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "departmentId")
     private Collection<FacultyMember> facultyMemberCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "department")
     private Collection<Program> programCollection;
     @JoinColumn(name = "facultyId", referencedColumnName = "facultyId")
     @ManyToOne(optional = false)
     private Faculty facultyId;
     @JoinColumn(name = "addressId", referencedColumnName = "addressId")
     @ManyToOne
     private Address addressId;
 
     public Department()
     {
     }
 
     public Department(String departmentId)
     {
         this.departmentId = departmentId;
     }
 
     public Department(String departmentId, String name, String phone, String email, String website, boolean isActive)
     {
         this.departmentId = departmentId;
         this.name = name;
         this.phone = phone;
         this.email = email;
         this.website = website;
         this.isActive = isActive;
     }
 
     public String getDepartmentId()
     {
         return departmentId;
     }
 
     public void setDepartmentId(String departmentId)
     {
         this.departmentId = departmentId;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public Integer getDirectorId()
     {
         return directorId;
     }
 
     public void setDirectorId(Integer directorId)
     {
         this.directorId = directorId;
     }
 
     public String getPhone()
     {
         return phone;
     }
 
     public void setPhone(String phone)
     {
         this.phone = phone;
     }
 
     public String getEmail()
     {
         return email;
     }
 
     public void setEmail(String email)
     {
         this.email = email;
     }
 
     public String getWebsite()
     {
         return website;
     }
 
     public void setWebsite(String website)
     {
         this.website = website;
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
     public Collection<Course> getCourseCollection()
     {
         return courseCollection;
     }
 
     public void setCourseCollection(Collection<Course> courseCollection)
     {
         this.courseCollection = courseCollection;
     }
 
     @XmlTransient
     public Collection<FacultyMember> getFacultyMemberCollection()
     {
         return facultyMemberCollection;
     }
 
     public void setFacultyMemberCollection(Collection<FacultyMember> facultyMemberCollection)
     {
         this.facultyMemberCollection = facultyMemberCollection;
     }
 
     @XmlTransient
     public Collection<Program> getProgramCollection()
     {
         return programCollection;
     }
 
     public void setProgramCollection(Collection<Program> programCollection)
     {
         this.programCollection = programCollection;
     }
 
     public Faculty getFacultyId()
     {
         return facultyId;
     }
 
     public void setFacultyId(Faculty facultyId)
     {
         this.facultyId = facultyId;
     }
 
     public Address getAddressId()
     {
         return addressId;
     }
 
     public void setAddressId(Address addressId)
     {
         this.addressId = addressId;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 0;
         hash += (departmentId != null ? departmentId.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object)
     {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Department))
         {
             return false;
         }
         Department other = (Department) object;
         if ((this.departmentId == null && other.departmentId != null) || (this.departmentId != null && !this.departmentId.equals(other.departmentId)))
         {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString()
     {
         return "adg.red.models.Department[ departmentId=" + departmentId + " ]";
     }
 
    public static List<Department> getByNameBeginsWith(String beginsWith)
     {
        return RedEntityManager.getEntityManager().createNamedQuery("Department.findByNameBeginsWith").setParameter("keyWord", beginsWith + "%").getResultList();
     }
 
     public static List<Department> getAll()
     {
         return RedEntityManager.getEntityManager().createNamedQuery("Department.findAll").getResultList();
     }
 
     public static Department getDepartmentById(String departmentId)
     {
         return ((List<Department>) RedEntityManager.getEntityManager().createNamedQuery("Department.findByDepartmentId").setParameter("departmentId", departmentId).getResultList()).get(0);
     }
 }
