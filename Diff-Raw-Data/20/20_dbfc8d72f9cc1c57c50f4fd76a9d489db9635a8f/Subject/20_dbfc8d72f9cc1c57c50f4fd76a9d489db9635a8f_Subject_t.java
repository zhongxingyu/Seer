 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ejb;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author Rowan
  */
 @Entity
 @Table(name = "subject")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Subject.findAll", query = "SELECT s FROM Subject s"),
     @NamedQuery(name = "Subject.findByClassId", query = "SELECT s FROM Subject s WHERE s.classId = :classId"),
     @NamedQuery(name = "Subject.findByStudentId", query = "SELECT s FROM Subject s, IN (s.studentCollection) t WHERE t.studentId = :studentId"),
     @NamedQuery(name = "Subject.findByTeacherId", query = "SELECT s FROM Subject s WHERE s.teacherId.teacherId = :teacherId"),
     @NamedQuery(name = "Subject.findByName", query = "SELECT s FROM Subject s WHERE s.name = :name"),
     @NamedQuery(name = "Subject.findByMaxstudents", query = "SELECT s FROM Subject s WHERE s.maxstudents = :maxstudents"),
     @NamedQuery(name = "Subject.findBySchedule", query = "SELECT s FROM Subject s WHERE s.schedule = :schedule")})
 public class Subject implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "class_id")
     private Integer classId;
     @Size(max = 50)
     @Column(name = "name")
     private String name;
     @Column(name = "maxstudents")
     private Short maxstudents;
     @Column(name = "schedule")
     @Temporal(TemporalType.TIME)
     private Date schedule;
     @JoinTable(name = "student_subject", joinColumns = {
         @JoinColumn(name = "class_id", referencedColumnName = "class_id")}, inverseJoinColumns = {
         @JoinColumn(name = "student_id", referencedColumnName = "student_id")})
     @ManyToMany
     private Collection<Student> studentCollection;
     @JoinColumn(name = "teacher_id", referencedColumnName = "teacher_id")
     @ManyToOne
     private Teacher teacherId;
 
     public Subject() {
     }
 
     public Subject(Integer classId) {
         this.classId = classId;
     }
 
     public Integer getClassId() {
         return classId;
     }
 
     public void setClassId(Integer classId) {
         this.classId = classId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Short getMaxstudents() {
         return maxstudents;
     }
 
     public void setMaxstudents(Short maxstudents) {
         this.maxstudents = maxstudents;
     }
 
     public Date getSchedule() {
         return schedule;
     }
 
     public void setSchedule(Date schedule) {
         this.schedule = schedule;
     }
 
     @XmlTransient
     public Collection<Student> getStudentCollection() {
         return studentCollection;
     }
 
     public void setStudentCollection(Collection<Student> studentCollection) {
         this.studentCollection = studentCollection;
     }
 
     public Teacher getTeacherId() {
         return teacherId;
     }
 
     public void setTeacherId(Teacher teacherId) {
         this.teacherId = teacherId;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (classId != null ? classId.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Subject)) {
             return false;
         }
         Subject other = (Subject) object;
         if ((this.classId == null && other.classId != null) || (this.classId != null && !this.classId.equals(other.classId))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return this.getName();
     }
     
    public String getCapacity() {
        return String.format("%d / %d", this.studentCollection.size(), this.maxstudents);
    }
    
 }
