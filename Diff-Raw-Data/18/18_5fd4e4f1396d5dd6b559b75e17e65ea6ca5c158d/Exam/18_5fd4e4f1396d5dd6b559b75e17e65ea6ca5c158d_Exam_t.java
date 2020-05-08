 package no.niths.domain.school;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.validation.constraints.Pattern;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import no.niths.common.constants.DomainConstantNames;
 import no.niths.common.constants.ValidationConstants;
 import no.niths.domain.Domain;
 import no.niths.domain.adapter.JsonCalendarSerializerAdapter;
 import no.niths.domain.adapter.XmlCalendarAdapter;
 import no.niths.domain.location.Room;
 import no.niths.domain.school.constants.ExamType;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 /**
  * Domain class for Exam
  *
  * <p>
  * Exam has these variables:
  * name = example Eksamen i PG2100,
  * examType = example Skriftlig,
  * startTime = example 9:00,
  * endTime = example 12:00,
  * allowedAid = example none
  * </p>
  * <p>
  * And relations too:
  * Subject,
  * Room
  * </p>
  */
 @XmlRootElement
 @Entity
 @Table(name = DomainConstantNames.EXAMS)
 @XmlAccessorType(XmlAccessType.FIELD)
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 public class Exam implements Domain {
 
     @Transient
     private static final long serialVersionUID = 3498003779752196516L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     @Column
     @Pattern(
             regexp  = ValidationConstants.REGULAR,
             message = "Invalid name (should be 2 -80 alphanumeric letters")
     private String name;
 
    @Column(name = "exam_type")
     @Enumerated(EnumType.STRING)
 	@XmlElement(name="examtype")
     private ExamType examType;
 
     @Column(name="start_time")
     @Temporal(TemporalType.TIMESTAMP)
     @XmlSchemaType(name = "date")
     @XmlJavaTypeAdapter(XmlCalendarAdapter.class)
 	@XmlElement(name="starttime")
     private Calendar startTime;
 
     @Column(name="end_time")
     @Temporal(TemporalType.TIMESTAMP)
     @XmlSchemaType(name = "date")
     @XmlJavaTypeAdapter(XmlCalendarAdapter.class)
 	@XmlElement(name="endtime")
     private Calendar endTime;
 
     @Column(name="allowed_aid")
 	@XmlElement(name="allowaid")
     private String allowedAid;
 
     @ManyToOne(fetch = FetchType.LAZY, targetEntity= Subject.class)
     @JoinTable(name = "exam_subjects",
             joinColumns = @JoinColumn(name = "exams_id"),
             inverseJoinColumns = @JoinColumn(name = "subjects_id"))
     @Cascade(org.hibernate.annotations.CascadeType.ALL)
     private Subject subject;
 
     @ManyToMany(fetch = FetchType.LAZY)
     @JoinTable(name = "rooms_exams",
             joinColumns = @JoinColumn(name = "exams_id"),
             inverseJoinColumns = @JoinColumn(name = "room_id"))
     @Cascade(org.hibernate.annotations.CascadeType.ALL)
     private List<Room> rooms = new ArrayList<Room>();
 
     public Exam(){
     	this(null,null,null,null,null);
     	this.rooms   = null;
     	this.subject = null;
     }
 
     public Exam(String name) {
         this.name = name;
     }
 
     public Exam(String name, ExamType examType) {
         this.name     = name;
         this.examType = examType;
     }
 
     public Exam(
             String name,
             ExamType examType,
             String allowedAid,
             GregorianCalendar startTime,
             GregorianCalendar endTime) {
         this.name       = name;
         this.examType   = examType;
         this.allowedAid = allowedAid;
         this.startTime  = startTime;
         this.endTime    = endTime;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public ExamType getExamType() {
         return examType;
     }
 
     public void setExamType(ExamType examType) {
         this.examType = examType;
     }
 
     public String getAllowedAid() {
         return allowedAid;
     }
 
     public void setAllowedAid(String allowedAid) {
         this.allowedAid = allowedAid;
     }
 
     @JsonSerialize(using = JsonCalendarSerializerAdapter.class)
     public Calendar getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Calendar startTime) {
         this.startTime = startTime;
     }
 
     @JsonSerialize(using = JsonCalendarSerializerAdapter.class)
     public Calendar getEndTime() {
         return endTime;
     }
 
     public void setEndTime(Calendar endTime) {
         this.endTime = endTime;
     }
 
     public void setSubject(Subject subject) {
         this.subject = subject;
     }
 
     @JsonSerialize(as=Subject.class)
     public Subject getSubject() {
         return subject;
     }
 
     public void setRooms(List<Room> rooms) {
         this.rooms = rooms;
     }
 
     public List<Room> getRooms() {
         return rooms;
     }
 
     @Override
     public boolean equals(Object that) {
         if(!(that instanceof Exam)) return false;
         Exam exam = (Exam) that;
 
         return exam == this || exam.getId().equals(id);
     }
 
     @JsonIgnore
     public boolean isEmpty(){
         return (id == null && name == null && examType == null && allowedAid ==  null
                 && startTime == null && endTime == null);
     }
 
     @Override
     public String toString() {
         return String.format("[%s][%s][%s]", id, name, examType);
     }
 }
