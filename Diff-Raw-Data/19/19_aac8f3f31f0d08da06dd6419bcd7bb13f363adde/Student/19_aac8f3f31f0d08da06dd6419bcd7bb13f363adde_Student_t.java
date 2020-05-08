 package org.sukrupa.student;
 
 import com.google.common.collect.Sets;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.hibernate.annotations.Type;
 import org.joda.time.LocalDate;
 import org.joda.time.Years;
 import org.joda.time.format.DateTimeFormat;
 import org.sukrupa.platform.DoNotRemove;
 
 import javax.persistence.*;
 import java.util.*;
 
 @Entity
 public class Student {
 
 	public static final String DATE_OF_BIRTH_FORMAT = "dd-MM-YYYY";
     private static final String PLACEHOLDER_IMAGE = "placeholderImage";
 
     @Id
     @GeneratedValue
     private long id;
 
     @Column(name = "STUDENT_ID")
     private String studentId;
 
     private String name;
 
     private String religion;
 
     private String caste;
 
     @OneToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "mother_id", referencedColumnName = "id")
     private Caregiver mother;
 
     @OneToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "father_id", referencedColumnName = "id")
     private Caregiver father;
 
     @Column(name = "SUB_CASTE")
     private String subCaste;
 
     @Column(name = "COMMUNITY_LOCATION")
     private String communityLocation;
 
     private String gender;
 
     @Column(name = "STUDENT_CLASS")
     private String studentClass;
 
     @Column(name = "IMAGE_LINK")
     private String imageLink;
 
     @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
     @Column(name = "DATE_OF_BIRTH")
     private LocalDate dateOfBirth;
 
     @Column(name = "STUDENT_DISCIPLINARY")
     private String disciplinary;
 
     @Column(name = "STUDENT_PERFORMANCE")
     private String performance;
 
     @OneToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "student_profile", referencedColumnName = "id")
     private Profile profile;
 
 
     @ManyToMany
     @JoinTable(name = "STUDENT_TALENT",
             joinColumns = @JoinColumn(name = "student_id"),
             inverseJoinColumns = @JoinColumn(name = "talent_id"))
     private Set<Talent> talents;
     @OrderBy("date desc")
     @OneToMany(cascade = CascadeType.ALL)
     @JoinTable(name = "STUDENT_NOTE",
             joinColumns = @JoinColumn(name = "student_id"),
             inverseJoinColumns = @JoinColumn(name = "note_id"))
     private Set<Note> notes;
     public static final Student EMPTY_STUDENT = new EmptyStudent();
 
     @Enumerated(EnumType.ORDINAL)
     private StudentStatus status = StudentStatus.EXISTING_STUDENT;
 
     @DoNotRemove
     public Student() {
     }
 
     public Student(String studentId, String name, String religion, String caste, String subCaste,
                    String communityLocation, String gender, String studentClass, Set<Talent> talents,
                    Caregiver father, Caregiver mother, LocalDate dateOfBirth, Set<Note> notes, String imageLink,
                    StudentStatus status, String disciplinary, String performance, Profile profile) {
         this.studentId = setStudentId(studentId);
         this.name = name;
         this.religion = religion;
         this.caste = caste;
         this.subCaste = subCaste;
         this.communityLocation = communityLocation;
         this.gender = gender;
         this.studentClass = studentClass;
         this.father = father;
         this.mother = mother;
         this.dateOfBirth = dateOfBirth;
         this.talents = talents;
         this.notes = notes;
         this.imageLink = imageLink;
 
         if(status == null) {
             status = StudentStatus.EXISTING_STUDENT;
         }
 
         this.status = status;
         this.disciplinary = disciplinary;
         this.performance = performance;
         this.profile = profile;
     }
 
     public Student(String studentId, String name, String dateOfBirth, String gender) {
         this.studentId = setStudentId(studentId);
         this.name = name;
         this.dateOfBirth = convertDate(dateOfBirth);
         this.gender = gender;
         this.talents = new HashSet<Talent>();
     }
 
     private String setStudentId(String studentId) {
         return StringUtils.upperCase(studentId);
     }
 
     private LocalDate convertDate(String dateOfBirth) {
         return new LocalDate(DateTimeFormat.forPattern(DATE_OF_BIRTH_FORMAT).parseDateTime(dateOfBirth));
     }
 
     public String getName() {
         return name;
     }
 
     public String getReligion() {
         return religion;
     }
 
     public String getCaste() {
         return caste;
     }
 
     public String getSubCaste() {
         return subCaste;
     }
 
     public String getCommunityLocation() {
         return communityLocation;
     }
 
     public String getDisciplinary(){
         return disciplinary;
     }
 
     public String getPerformance(){
         return performance;
     }
 
     public Profile getProfile(){
         if(profile == null){
             this.profile = new Profile();
         }
         return profile;
     }
 
     public String getStudentId() {
         return studentId;
     }
 
     public String getGender() {
         return gender;
     }
 
     public String getStudentClass() {
         return studentClass;
     }
 
     public Caregiver getMother() {
         return mother;
     }
 
     public Caregiver getFather() {
         return father;
     }
 
     public LocalDate getDateOfBirth() {
         return dateOfBirth;
     }
 
     public Set<Talent> getTalents() {
         return talents;
     }
 
     public String getTalentsForDisplay() {
         return StringUtils.join(talentDescriptions(), ", ");
     }
 
     public List<String> talentDescriptions() {
         List<String> talentDescriptions = new ArrayList<String>();
 
         for (Talent talent : talents) {
             talentDescriptions.add(talent.getDescription());
         }
         return talentDescriptions;
     }
 
     public int getAge() {
         return Years.yearsBetween(dateOfBirth, getCurrentDate()).getYears();
     }
 
     public String getImageLink() {
         if (imageLink==null){
             return PLACEHOLDER_IMAGE;
         } else {
         return imageLink;
         }
     }
 
     protected LocalDate getCurrentDate() {
         return new LocalDate();
     }
 
     public void addNote(Note note) {
         notes.add(note);
     }
 
     public Set<Note> getNotes() {
         return notes;
     }
 
     public void setBackground(String background){
         this.getProfile().background(background);
     }
 
     public StudentStatus getStatus() {
         return status;
     }
 
     private static String[] excludedFields = new String[]{"id"};
 
     public boolean equals(Object other) {
         return EqualsBuilder.reflectionEquals(this, other, excludedFields);
     }
 
     public int hashCode() {
         return HashCodeBuilder.reflectionHashCode(this, excludedFields);
     }
 
     public String toString() {
         return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
     }
 
 	public String getDateOfBirthForDisplay() {
 		return DateTimeFormat.forPattern(DATE_OF_BIRTH_FORMAT).print(dateOfBirth);
 	}
 
 	public void updateFrom(StudentProfileForm studentUpdateParameters, Set<Talent> newTalents) {
 		this.studentClass = studentUpdateParameters.getStudentClass();
 		this.gender = studentUpdateParameters.getGender();
 		this.name = studentUpdateParameters.getName();
 		this.religion = studentUpdateParameters.getReligion();
 		this.caste = studentUpdateParameters.getCaste();
 		this.subCaste = studentUpdateParameters.getSubCaste();
 		this.communityLocation = studentUpdateParameters.getCommunityLocation();
         this.performance = studentUpdateParameters.getPerformance();
         this.disciplinary = studentUpdateParameters.getDisciplinary();
 		this.talents = Sets.newHashSet(newTalents);
 		this.dateOfBirth = convertDate(studentUpdateParameters.getDateOfBirth());
         this.status = StudentStatus.fromString(studentUpdateParameters.getStatus());
 
        if (studentUpdateParameters.getFather() != null) {
            this.father = new Caregiver();
            this.father.setName(studentUpdateParameters.getFather());
        }
 
        if (studentUpdateParameters.getFather() != null) {
            this.mother = new Caregiver();
            this.mother.setName(studentUpdateParameters.getMother());
        }
 
         setBackground(studentUpdateParameters.getBackground());
 	}
 
 
 
     public void promote() {
 
         if(this.studentClass.equals("Graduated")){
            this.studentClass = this.studentClass;
         }else if(this.studentClass.equals("UKG")){
           this.studentClass = "1 Std";
        }else if(this.studentClass.equals("LKG")) {
                   this.studentClass = "UKG";
        }else if(this.studentClass.equals("Preschool")){
            this.studentClass = "LKG";
        }else if(this.studentClass.equals("10 Std")){
            this.studentClass = "Graduated";
        }
        else{
             int studentClassInt = Integer.parseInt(this.studentClass.substring(0,1));
             studentClassInt++;
             this.studentClass = this.studentClass.replace(this.studentClass.substring(0,1), Integer.toString(studentClassInt));
         }
 
     }
 
 
     private static class EmptyStudent extends Student {
         @Override
         public String getName() {
             return "";
         }
 
         @Override
         public String getReligion() {
             return "";
         }
 
         @Override
         public String getCaste() {
             return "";
         }
 
         @Override
         public String getSubCaste() {
             return "";
         }
 
         @Override
         public String getCommunityLocation() {
             return "";
         }
 
         @Override
         public String getStudentId() {
             return "";
         }
 
         @Override
         public String getGender() {
             return "";
         }
 
         @Override
         public String getStudentClass() {
             return "";
         }
 
         @Override
         public Caregiver getMother() {
             return null;
         }
 
         @Override
         public Caregiver getFather() {
             return null;
         }
 
         @Override
         public LocalDate getDateOfBirth() {
             return new LocalDate();
         }
 
         @Override
         public Set<Talent> getTalents() {
             return Collections.emptySet();
         }
 
         @Override
         public String getTalentsForDisplay() {
             return "";
         }
 
         @Override
         public List<String> talentDescriptions() {
             return Collections.emptyList();
         }
 
         @Override
         public String getDisciplinary(){
             return "";
         }
 
         @Override
         public String getPerformance(){
             return "";
         }
 
 
         @Override
         public int getAge() {
             return 0;
         }
 
 
     }
 }
