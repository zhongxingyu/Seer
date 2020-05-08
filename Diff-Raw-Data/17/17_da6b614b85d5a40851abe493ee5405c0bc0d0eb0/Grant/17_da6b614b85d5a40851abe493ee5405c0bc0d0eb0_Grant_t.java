 package edu.lmu.cs.headmaster.ws.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 
 import edu.lmu.cs.headmaster.ws.util.DateTimeXmlAdapter;
 
 /**
  * A Grant is a project that is proposed by a student, which may be funded by an
  * institution.
  */
 @Entity
 // "grant" is a reserved word.
 @Table(name="researchgrant")
 @XmlRootElement
 public class Grant {
 
     private Long id;
     private String title;
     private String description;
     private DateTime submissionDate;
     private String type;
     private Integer amount;
     private Boolean awarded;
     private String facultyMentor;
     private List<Student> students = new ArrayList<Student>();
     private Boolean presented;
     private String notes;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Lob
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     @Lob
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
     @XmlJavaTypeAdapter(value=DateTimeXmlAdapter.class)
     public DateTime getSubmissionDate() {
         return submissionDate;
     }
 
     public void setSubmissionDate(DateTime submissionDate) {
         this.submissionDate = submissionDate;
     }
 
     @Lob
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public Integer getAmount() {
         return amount;
     }
 
     public void setAmount(Integer amount) {
         this.amount = amount;
     }
 
     public Boolean getAwarded() {
         return awarded;
     }
 
     public void setAwarded(Boolean awarded) {
         this.awarded = awarded;
     }
 
     @Lob
     public String getFacultyMentor() {
         return facultyMentor;
     }
 
     public void setFacultyMentor(String facultyMentor) {
         this.facultyMentor = facultyMentor;
     }
 
     @ManyToMany(cascade = CascadeType.ALL)
     @OrderColumn
     @JoinTable(
         joinColumns = @JoinColumn(name = "grant_id"),
         inverseJoinColumns = @JoinColumn(name = "student_id")
     )
     @LazyCollection(LazyCollectionOption.FALSE)
     public List<Student> getStudents() {
         return students;
     }
 
     public void setStudents(List<Student> students) {
         this.students = students;
     }
 
     public Boolean getPresented() {
         return presented;
     }
 
     public void setPresented(Boolean presented) {
         this.presented = presented;
     }
 
     @Lob
     public String getNotes() {
         return notes;
     }
 
     public void setNotes(String notes) {
         this.notes = notes;
     }
 
 }
