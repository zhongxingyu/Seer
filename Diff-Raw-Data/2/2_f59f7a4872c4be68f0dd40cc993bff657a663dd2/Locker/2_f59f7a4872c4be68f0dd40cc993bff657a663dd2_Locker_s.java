 package no.niths.domain.school;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.Pattern;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity
 @XmlRootElement(name = AppConstants.LOCKERS)
 @Table(name = AppConstants.LOCKERS)
 @XmlAccessorType(XmlAccessType.FIELD)
 @JsonSerialize(include = Inclusion.NON_NULL)
 public class Locker {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     @Column(name = "locker_number")
     @Pattern(regexp = "[1-9]([0-9]{1,2})?")
    @XmlElement(name = "locker_name")
     private Long lockerNumber;
 
     @ManyToOne(fetch = FetchType.LAZY, targetEntity = Student.class)
     @JoinTable(
             name               = "students_lockers",
             joinColumns        = @JoinColumn(name = "student_id"),
             inverseJoinColumns = @JoinColumn(name = "locker_id"))
     @Cascade(CascadeType.ALL)
     private Student owner;
 
     public Locker(){
     	this(null);
     	setOwner(null);
     }
     
     public Locker(Long lockerNumber){
     	setLockerNumber(lockerNumber);
     }
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Long getLockerNumber() {
         return lockerNumber;
     }
 
     public void setLockerNumber(Long lockerNumber) {
         this.lockerNumber = lockerNumber;
     }
 
     public Student getOwner() {
         return owner;
     }
 
     public void setOwner(Student owner) {
         this.owner = owner;
     }
 }
