 package org.sukrupa.student;
 
 import org.joda.time.format.DateTimeFormat;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 import org.sukrupa.app.services.StudentImageRepository;
 import org.sukrupa.platform.DoNotRemove;
 
 import java.util.Set;
 
 public class StudentForm {
     private String studentId;
     private String name;
     private String dateOfBirth;
     private String gender;
     private String studentClass;
     private String religion;
     private String caste;
     private String subCaste;
     private String communityLocation;
     private Caregiver father = new Caregiver();
     private Caregiver mother = new Caregiver();
     private Caregiver guardian = new Caregiver();
     private Set<String> talents;
     private String status;
     private boolean sponsored;
     private String disciplinary;
     private String performance;
     private String background;
     private CommonsMultipartFile imageToUpload;
     private String familyStatus;
 
 
     public StudentForm(String studentId, String name, String dateOfBirth, String gender, String studentClass, String religion, String caste, String subCaste, String communityLocation, Caregiver father, Caregiver mother, Caregiver guardian, Set<String> talents, String status, String disciplinary, String performance, String background, String familyStatus, CommonsMultipartFile imageToUpload) {
         this.studentId = studentId;
         this.name = name;
         this.dateOfBirth = dateOfBirth;
         this.gender = gender;
         this.studentClass = studentClass;
         this.religion = religion;
         this.caste = caste;
         this.subCaste = subCaste;
         this.communityLocation = communityLocation;
         this.father = father;
         this.mother = mother;
         this.guardian = guardian;
         this.imageToUpload = imageToUpload;
         this.talents = talents;
         this.status = status;
         this.disciplinary = disciplinary;
         this.performance = performance;
         this.background = background;
         this.familyStatus = familyStatus;
     }
 
     @DoNotRemove
     public StudentForm() {
     }
 
     public String getStudentId() {
         return studentId;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDateOfBirth() {
         return dateOfBirth;
     }
 
     public String getDateOfBirthForDisplay() {
         return getDateOfBirth();
     }
 
     public CommonsMultipartFile getImageToUpload() {
         return imageToUpload;
     }
 
     public void setImageToUpload(CommonsMultipartFile imageToUpload) {
         this.imageToUpload = imageToUpload;
     }
 
     public String getGender() {
         return gender;
     }
 
     public String getStudentClass() {
         return studentClass;
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
 
     public Caregiver getFather() {
         return father;
     }
 
     public Caregiver getMother() {
         return mother;
     }
 
     public Set<String> getTalentDescriptions() {
         return talents;
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     public void setStudentId(String studentId) {
         this.studentId = studentId;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setDateOfBirth(String dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public void setStudentClass(String studentClass) {
         this.studentClass = studentClass;
     }
 
     public void setReligion(String religion) {
         this.religion = religion;
     }
 
     public void setCaste(String caste) {
         this.caste = caste;
     }
 
     public void setSubCaste(String subCaste) {
         this.subCaste = subCaste;
     }
 
     public void setCommunityLocation(String communityLocation) {
         this.communityLocation = communityLocation;
     }
 
     public void setFather(Caregiver father) {
         this.father = father;
     }
 
     public void setMother(Caregiver mother) {
         this.mother = mother;
     }
 
     public void setTalents(Set<String> talents) {
         this.talents = talents;
     }
 
     public void setPerformance(String performance) {
         this.performance = performance;
     }
 
     public void setDisciplinary(String disciplinary) {
         this.disciplinary = disciplinary;
     }
 
     public void setBackground(String background) {
         this.background = background;
     }
 
     public void setFamilyStatus(String familyStatus) {
         this.familyStatus = familyStatus;
     }
 
     public void setSponsored(boolean sponsored) {
         this.sponsored = sponsored;
     }
 
     public String getPerformance() {
     return performance;
 }
 
     public String getDisciplinary() {
     return disciplinary;
 }
 
     public String getBackground() {
     return this.background;
 }
 
     public boolean getSponsored() {
     return sponsored;
 }
     public String getfamilyStatus() {
         return familyStatus;
     }
 
     public Caregiver getGuardian() {
         return guardian;
     }
 
     public boolean hasImage() {
        return null != imageToUpload && !imageToUpload.isEmpty();
     }
 
     public Image getImage() {
         return new Image(imageToUpload);
     }
 
     public void createImage(StudentImageRepository studentImageRepository) {
         studentImageRepository.save(getImage(), studentId);
     }
 }
