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
 import org.sukrupa.app.students.Talent;
 import org.sukrupa.platform.DoNotRemove;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 @Entity
 public class Student {
 
 	static final String DATE_OF_BIRTH_FORMAT = "dd-MM-YYYY";
 	@Id
     @GeneratedValue
     private long id;
 
     @Column(name = "STUDENT_ID")
     private String studentId;
 
     private String name;
 
     private String religion;
 
     private String caste;
 
     private String mother;
 
     private String father;
 
     @Column(name = "SUB_CASTE")
     private String subCaste;
 
     @Column(name = "COMMUNITY_LOCATION")
     private String communityLocation;
 
     private String gender;
 
     @Column(name = "STUDENT_CLASS")
     private String studentClass;
 
     @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
     @Column(name = "DATE_OF_BIRTH")
     private LocalDate dateOfBirth;
 
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
 
     @DoNotRemove
     public Student() {
     }
 
     public Student(String studentId, String name, String religion, String caste, String subCaste,
                    String communityLocation, String gender, String studentClass, Set<Talent> talents,
                    String father, String mother, LocalDate dateOfBirth, Set<Note> notes) {
         this.studentId = studentId;
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
 
     public String getStudentId() {
         return studentId;
     }
 
     public String getGender() {
         return gender;
     }
 
     public String getStudentClass() {
         return studentClass;
     }
 
     public String getMother() {
         return mother;
     }
 
     public String getFather() {
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
 
     protected LocalDate getCurrentDate() {
         return new LocalDate();
     }
 
     public void addNote(Note note) {
         notes.add(note);
     }
 
     public Set<Note> getNotes() {
         return notes;
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
 
 	public void updateFrom(StudentUpdateParameter studentUpdateParameter, Set<Talent> newTalents) {
 		this.studentClass = studentUpdateParameter.getStudentClass();
 		this.gender = studentUpdateParameter.getGender();
 		this.name = studentUpdateParameter.getName();
 		this.religion = studentUpdateParameter.getReligion();
 		this.caste = studentUpdateParameter.getCaste();
 		this.subCaste = studentUpdateParameter.getSubCaste();
 		this.communityLocation = studentUpdateParameter.getCommunityLocation();
 		this.father = studentUpdateParameter.getFather();
 		this.mother = studentUpdateParameter.getMother();
 		this.talents = Sets.newHashSet(newTalents);
 		this.dateOfBirth = new LocalDate(DateTimeFormat.forPattern(DATE_OF_BIRTH_FORMAT).parseDateTime(studentUpdateParameter.getDateOfBirth()));
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
 }
