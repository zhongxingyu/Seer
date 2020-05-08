 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import ext.Ext;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.persistence.ElementCollection;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import play.Logger;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import utils.Base64;
 import utils.Dates;
 import utils.Validate;
 
 /**
  *
  * @author inf04
  */
 @Entity
 public class Person extends Model implements Comparable<Person> {
 
     @Required
     public String name;
     @Required
     public String firstname;
     public int birthDay;
     public int birthMonth;
     public int birthYear;
     public String gsm;
     public String phone;
     public String email;
     public String nationality;
     public String nationalNumber;
     public String securityNumber;
     public String birthPlace;
     public int civilStatus;
     public int driverLicence;
     public String talkLanguage;
     public String writeLanguage;
     @Lob
     public String otherTalkLanguage;
     @Lob
     public String otherWriteLanguage;
     @Embedded
     public Address address;
     @ManyToOne
     public Department department;
     public Blob picture;
     // travailleur maison accueil
     public int nbFolders;
     @ManyToOne
     public Floor floor;
     @OneToOne(mappedBy = "person")
     public User user;
     @OneToOne
     public Division division;
     public boolean enabled;
     // Données résidents
     public int nbStays;
     public long folderNumber;
     public String contactPerson;
     public boolean withPig;
     public int docIDType;
     @Temporal(TemporalType.DATE)
     public Date readmissionDate;
     @ManyToOne
     public Person referent1;
     @ManyToOne
     public Person referent2;
     @ManyToOne
     public Person referent3;
     @OneToOne
     public Room room;
     public boolean redList;
     public boolean noReadmission;
     public boolean noForMA;
     // Données magasins
     @ManyToMany
     public List<Shop> shops;
     @ElementCollection(fetch = FetchType.EAGER)
     public Set<String> personStatus;
     // Insertion : volontaire, art 60, tig
     /*
      * 1 = Occupation
      * 2 = Aide
      * 3 = Contacts
      * 4 = Avantages
      * 5 = Conditions
      * 6 = Autres
      */
     public int motivation;
     @Lob
     public String motivationDetails;
     public String comment;
     @ManyToOne
     public Locality locality;
     // Employé
     public boolean chiefDepartment;
     public boolean chiefDivision;
 
 
     public Person() {
         this.personStatus = new TreeSet<String>();
     }
 
     public static List<Person> byReferent1And2And3(Person ref1,Person ref2,Person ref3) {
         List<Person> residents = Person.find("from Person p,in(p.personStatus) ps "
                 + "where (p.referent1 = ? "
                 + "or p.referent2 = ? "
                 + "or p.referent3 = ?) "
                 + "and p.enabled = true "
                 + "and  ps = ? "
                 + "order by p.name,p.firstname",
                 ref1, ref2, ref3,PersonStatus.resident.value).fetch();
         return residents;
     }
     
     public void moveToServiceX() {
         Department dep = Department.getX();
         this.department = dep;
         this.save();
     }
     
     public void setToOldResident() {
         this.personStatus.remove("000");
         this.personStatus.remove("002");
         this.personStatus.remove("005");
         this.personStatus.remove("006");
         this.personStatus.remove("007");
         this.personStatus.remove("008");
         this.personStatus.remove("009");
 
         this.personStatus.add("003");
     }
 
     public static List<Person> birthDaysINS(List<String> status) {
         Calendar today = new GregorianCalendar();
         Calendar yesterday = Dates.getYesterday();
         Calendar tomorrow = Dates.getTomorrow();
 
         return Person.find("select distinct(p) from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and p.enabled = true "
                 + "and (p.birthDay = :dayBefore "
                 + "or p.birthDay = :today "
                 + "or p.birthDay = :dayAfter) "
                 + "and (p.birthMonth = :monthBefore "
                 + "or p.birthMonth = :month "
                 + "or p.birthMonth = :monthAfter) "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "order by p.birthDay ").
                 bind("status", status).query.setParameter("dayBefore", yesterday.get(Calendar.DAY_OF_MONTH)).
                 setParameter("today", today.get(Calendar.DAY_OF_MONTH)).
                 setParameter("dayAfter", tomorrow.get(Calendar.DAY_OF_MONTH)).
                 setParameter("monthBefore", yesterday.get(Calendar.MONTH)).
                 setParameter("month", today.get(Calendar.MONTH)).
                 setParameter("monthAfter", tomorrow.get(Calendar.MONTH)).getResultList();
     }
 
     public static List<Person> birthDaysResidentByFloor(Floor floor) {
         Calendar today = new GregorianCalendar();
         Calendar yesterday = Dates.getYesterday();
         Calendar tomorrow = Dates.getTomorrow();
 
         List<String> status = Arrays.asList(PersonStatus.resident.value, PersonStatus.beforeOutResident.value);
         return Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and p.enabled = true "
                 + "and (p.birthDay = :dayBefore "
                 + "or p.birthDay = :today "
                 + "or p.birthDay = :dayAfter) "
                 + "and (p.birthMonth = :monthBefore "
                 + "or p.birthMonth = :month "
                 + "or p.birthMonth = :monthAfter) "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "and (p.floor is null "
                 + "or p.floor = :floor) "
                 + "order by p.birthDay ").
                 bind("status", status).query.setParameter("dayBefore", yesterday.get(Calendar.DAY_OF_MONTH)).
                 setParameter("today", today.get(Calendar.DAY_OF_MONTH)).
                 setParameter("dayAfter", tomorrow.get(Calendar.DAY_OF_MONTH)).
                 setParameter("monthBefore", yesterday.get(Calendar.MONTH)).
                 setParameter("month", today.get(Calendar.MONTH)).
                 setParameter("monthAfter", tomorrow.get(Calendar.MONTH)).
                 setParameter("floor", floor).getResultList();
     }
 
     public static List<Person> birthDaysResident() {
         Calendar today = new GregorianCalendar();
         Calendar yesterday = Dates.getYesterday();
         Calendar tomorrow = Dates.getTomorrow();
 
         List<String> status = Arrays.asList(PersonStatus.resident.value, PersonStatus.beforeOutResident.value);
         return Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and p.enabled = true "
                 + "and (p.birthDay = :dayBefore "
                 + "or p.birthDay = :today "
                 + "or p.birthDay = :dayAfter) "
                 + "and (p.birthMonth = :monthBefore "
                 + "or p.birthMonth = :month "
                 + "or p.birthMonth = :monthAfter) "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "order by p.birthDay ").
                 bind("status", status).query.setParameter("dayBefore", yesterday.get(Calendar.DAY_OF_MONTH)).
                 setParameter("today", today.get(Calendar.DAY_OF_MONTH)).
                 setParameter("dayAfter", tomorrow.get(Calendar.DAY_OF_MONTH)).
                 setParameter("monthBefore", yesterday.get(Calendar.MONTH)).
                 setParameter("month", today.get(Calendar.MONTH)).
                 setParameter("monthAfter", tomorrow.get(Calendar.MONTH)).getResultList();
     }
 
     public static List<Person> personsActivated(String status) {
         return Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "order by p.name", status).fetch();
     }
 
     public static Long nbResidents() {
         return Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true ", PersonStatus.resident.value);
     }
 
     public static long nbPersons(String status) {
         return Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true ", status);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Person other = (Person) obj;
         if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
         return hash;
     }
 
     public int getPersonType() {
         if (this.personStatus.contains("000")) {
             return 0;
         }
         if (this.personStatus.contains("100")) {
             return 1;
         }
         if (this.personStatus.contains("200")) {
             return 2;
         }
         if (this.personStatus.contains("300")) {
             return 3;
         }
         if (this.personStatus.contains("400")) {
             return 4;
         }
         return -1;
     }
 
     public static List<Person> getSalers() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r where r.name = ?", "userSA").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getAllSalers() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where r.name = ? or r.name = ?", "userSA", "adminSA").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
 
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static long getNewFolderNumber() {
         Long folderNumber = (Long) Person.em().createQuery("select max(r.folderNumber) "
                 + "from Person r").getSingleResult();
         folderNumber++;
         return folderNumber;
     }
 
     public static void updateNBFolder(Person p) {
 
         if (p == null) {
             return;
         }
 
         long nbf = Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true "
                 + "and p.referent1 = ? ", "000", p);
 
         if (nbf == 0) {
             return;
         }
 
         p.nbFolders = (int) nbf;
         p.save();
 
     }
 
     public void checkStatus() {
         Date today = new Date();
         List<Activity> activities = Activity.find("actif = true and "
                 + "? >= startDate and ? <= endDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Activity a : activities) {
             a.resident.personStatus.add("005");
             a.resident.save();
             return;
         }
 
         List<Training> trainings = Training.find("actif = true and "
                 + "? >= startDate and ? <= endDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Training t : trainings) {
             t.resident.personStatus.add("006");
             t.resident.save();
             return;
         }
 
         List<Work> works = Work.find("actif = true and "
                 + "? >= startDate and ? <= endDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Work w : works) {
             w.resident.personStatus.add("007");
             w.resident.save();
             return;
         }
 
         List<Itt> itts = Itt.find("actif = true and "
                 + "? >= fromDate and ? <= toDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Itt i : itts) {
             i.resident.personStatus.add("008");
             i.resident.save();
             return;
         }
 
         this.personStatus.remove("005");
         this.personStatus.remove("006");
         this.personStatus.remove("007");
         this.personStatus.remove("008");
 
         this.save();
     }
 
     public void endContract() {
         this.department = null;
         this.division = null;
         this.chiefDepartment = false;
         this.chiefDivision = false;
         this.personStatus.add(PersonStatus.oldPerson.value);
         this.save();
     }
 
     public void setBirthDate(String birthDate) {
         this.resetBirthDate();
         if (birthDate != null && !birthDate.isEmpty()) {
             try {
                 if (birthDate.length() == 10) {
                     Date d = new SimpleDateFormat("dd-MM-yyyy").parse(birthDate);
                     this.birthDay = d.getDate();
                     this.birthMonth = d.getMonth();
                     this.birthYear = d.getYear() + 1900;
                 }
             } catch (ParseException ex) {
             }
         }
     }
 
     private void resetBirthDate() {
         this.birthDay = 0;
         this.birthMonth = 0;
         this.birthYear = 0;
     }
 
     public static File decodePictureToFile(String encodedPicture) {
         File picture = null;
         try {
             picture = new File("picture");
             Base64.decodeToFile(encodedPicture, "picture");
         } catch (IOException ex) {
             Logger.info(ex.toString());
         } finally {
             return picture;
         }
     }
 
     public void changePicture(File picture) {
         try {
             if (this.picture != null && this.picture.exists()) {
                 this.picture.getFile().delete();
             }
             this.picture = new Blob();
             this.picture.set(new FileInputStream(picture), "File");
         } catch (IOException ex) {
         }
     }
 
     public void setMeatType(String meat) {
         if (meat == null || meat.equals("pig")) {
             this.withPig = true;
         } else {
             this.withPig = false;
         }
     }
 
     public void setTalkLanguage(Integer[] talkLanguage) {
         this.talkLanguage = "";
 
         if (talkLanguage != null && talkLanguage.length > 0) {
             for (int i = 0; i < talkLanguage.length; i++) {
                 if ((i + 1) == talkLanguage.length) {
                     this.talkLanguage += talkLanguage[i];
                 } else {
                     this.talkLanguage += talkLanguage[i] + ":";
                 }
 
             }
         }
     }
 
     public void setWriteLanguage(Integer[] writeLanguage) {
         this.writeLanguage = "";
 
         if (writeLanguage != null && writeLanguage.length > 0) {
             for (int i = 0; i < writeLanguage.length; i++) {
                 if ((i + 1) == writeLanguage.length) {
                     this.writeLanguage += writeLanguage[i];
                 } else {
                     this.writeLanguage += writeLanguage[i] + ":";
                 }
 
             }
         }
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname.toLowerCase();
     }
 
     public void setName(String name) {
         this.name = name.toLowerCase();
     }
 
     public void changeStatus(String status) {
         if (this.personStatus.isEmpty()) {
             this.personStatus.add(status);
             return;
         }
 
         if (this.isINSPerson() && PersonStatus.isINSStatus(status)) {
             this.removeINSStatus();
             this.personStatus.add(status);
         }
 
         if (this.isRHPerson() && PersonStatus.isRHStatus(status)) {
             this.removeRHStatus();
             this.personStatus.add(status);
         }
     }
 
     private void removeINSStatus() {
         this.personStatus.remove("200");
         this.personStatus.remove("300");
         this.personStatus.remove("400");
     }
 
     private void removeRHStatus() {
         this.personStatus.remove("100");
         this.personStatus.remove("500");
     }
 
     public void updateAffectation(Professional professional) {
         this.chiefDepartment = professional.chiefDepartment;
         this.chiefDivision = professional.chiefDivision;
 
         this.department = Department.findById(professional.getFirstDepartment().id);
         if (this.chiefDepartment && Validate.isNotNull(this.department)) {
 
             this.department.person = this;
             this.department.save();
         }
 
         if (professional.division != null) {
             this.division = professional.division;
             this.division.person = this;
             this.division.save();
         }
 
 
         if (this.chiefDivision) {
             this.setChiefDivisionRole();
         } else {
             this.removeChiefDivisionRole();
         }
 
         if (this.chiefDepartment) {
             this.setChiefDepartmentRole();
         } else {
             this.removeChiefDepartmentRole();
         }
 
         this.save();
     }
 
     public void removeAffectation() {
         this.chiefDepartment = false;
         this.chiefDivision = false;
 
         if (Validate.isNotNull(this.department)) {
             this.department.person = null;
             this.department.save();
         }
 
         this.department = null;
         this.division = null;
 
         this.save();
     }
 
     public int compareTo(Person o) {
         if (this.name.compareTo(o.name) == 0) {
             return this.firstname.compareTo(o.firstname);
         }
 
         return this.name.compareTo(o.name);
     }
 
     public List<Department> getHisDepartments() {
         if (this.chiefDivision) {
             return new ArrayList<Department>(this.division.departments);
         }
         if (this.chiefDepartment) {
             Division div = this.department.getDivision();
             if (div == null) {
                 return Arrays.asList(this.department);
             }
             return new ArrayList<Department>(div.departments);
         }
 
         return null;
     }
 
     public static HashMap<Department, List<Person>> byDepartments(List<Department> departments) {
         HashMap<Department, List<Person>> personByDepartments = new HashMap<Department, List<Person>>();
 
         for (Department department : departments) {
             List<Person> persons = Person.byDepartment(department);
 
             if (!persons.isEmpty()) {
                 personByDepartments.put(department, persons);
             }
         }
 
         return personByDepartments;
     }
 
     public static List<Person> byDepartment(Department department) {
         return Person.find("department = ? "
                 + "and enabled = true "
                 + "order by name,firstname", department).fetch();
     }
 
     public static List<Person> byXAndReferent(Person person) {
         Department depX = Department.getX();
         List<Person> residents = Person.find("from Person p "
                 + "where (p.referent1 = ? "
                 + "or p.referent2 = ? "
                 + "or p.referent3 = ?) "
                 + "and department = ? "
                 + "and p.enabled = true "
                 + "order by p.name,p.firstname",
                 person, person, person,depX).fetch();
         
         return residents;
     }
 
     public void setChiefDivisionRole() {
         if (Validate.isNotNull(this.user)) {
             this.user.roles.add(Role.getDivisionChief());
             this.user.save();
         }
     }
 
     public void removeChiefDivisionRole() {
         if (Validate.isNotNull(this.user)) {
             this.user.roles.remove(new Role("divisionChief"));
             this.user.save();
         }
     }
 
     public void setChiefDepartmentRole() {
         if (Validate.isNotNull(this.user)) {
             this.user.roles.add(Role.getDepartmentChief());
             this.user.save();
         }
     }
 
     public void removeChiefDepartmentRole() {
         if (Validate.isNotNull(this.user)) {
             this.user.roles.remove(new Role("departmentChief"));
             this.user.save();
         }
     }
 
     public static List<Person> findInsPersons() {
         List<Person> employes = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where r.name in (:status) "
                 + "and u.enabled = true ").
                 bind("status", Arrays.asList("adminINS","userINS")).fetch();
         for (User user : users) {
             if (!employes.contains(user.person)) {
                 employes.add(user.person);
             }
         }
         
         return employes;
     }
 
     public static List<Person> findRHPersons() {
         List<String> status = PersonStatus.getRHStatus();
         List<Person> employes = Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and p.enabled = true ").bind("status", status).fetch();
         return employes;
     }
 
     public static List<Person> getDivisionChiefs() {
         return Person.find("enabled = true "
                 + "and chiefDivision = true ").fetch();
     }
 
     public Blob getPicture() {
         return picture;
     }
 
     public void setPicture(Blob picture) {
         this.picture = picture;
     }
 
     @Override
     public String toString() {
         return this.name + " " + this.firstname;
     }
 
     public boolean isINSPerson() {
         return this.isVolunteer() || this.isArt60() || this.isTIG();
     }
 
     public boolean isRHPerson() {
         return this.isEmployee() || this.isWorker();
     }
 
     public boolean isVolunteer() {
         return this.personStatus.contains("200");
     }
 
     public boolean isTIG() {
         return this.personStatus.contains("400");
     }
 
     public boolean isArt60() {
         return this.personStatus.contains("300");
     }
 
     public boolean isEmployee() {
         return this.personStatus.contains("100");
     }
 
     public boolean isWorker() {
         return this.personStatus.contains("500");
     }
 
     public boolean isBeforeOutResident() {
         return this.personStatus.contains("002");
     }
 
     public boolean isResident() {
         return this.personStatus.contains("000");
     }
 
     public boolean isOldPerson() {
         return this.personStatus.contains("011");
     }
 
     public void cancelOutStay() {
         this.personStatus.remove("002");
         this.personStatus.add("000");
         this.save();
     }
 
     public void setToResident() {
         this.personStatus.add(PersonStatus.resident.value);
         this.personStatus.remove(PersonStatus.beforeResident.value);
         this.personStatus.remove(PersonStatus.oldResident.value);
         this.personStatus.remove(PersonStatus.postResident.value);
     }
 
     public static List<Person> suggestForINS(String query) {
         List<String> s = PersonStatus.getINSStatus();
         List<Person> persons = suggestByStatus(s, query);
 
         return persons;
     }
 
     public static List<Person> suggestForRH(String query) {
         List<String> s = PersonStatus.getRHStatus();
         List<Person> persons = suggestByStatus(s, query);
 
         return persons;
     }
 
     public static List<Person> suggestForMA(String query) {
         List<String> s = PersonStatus.getResidentStatus();
         List<Person> list = suggestByStatus(s, query);
 
         List<Person> persons = new ArrayList<Person>();
         for (Person person : list) {
             if (person.folderNumber > 0) {
                 persons.add(person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> suggestForMAResidentOnly(String query) {
         List<String> s = new ArrayList<String>();
         s.add(PersonStatus.resident.value);
         List<Person> list = suggestByStatus(s, query);
 
         return list;
     }
 
     private static List<Person> suggestByStatus(List<String> s, String query) {
         List<Person> persons = Person.find("select distinct p "
                 + "from Person p,in(p.personStatus) ps "
                 + "where p.name like :name "
                 + "and p.enabled = true "
                 + "and ps in (:status) "
                 + "order by p.name, p.firstname").
                 bind("status", s).query.setParameter("name", query + "%").getResultList();
         return persons;
     }
 
     public static String createAutoCompleteJson(String query, List<Person> persons) {
         String json = "{ "
                + "query:'" + query + "', ";
         String suggestion = "suggestions:[";
         for (int i = 0; i < persons.size(); i++) {
             suggestion += "'" + persons.get(i).name.replace("'", "\\'") 
                     + " " + persons.get(i).firstname.replace("'", "\\'");
             suggestion += " (" + Ext.showStatus(persons.get(i)) + ")'";
             if ((i + 1) != persons.size()) {
                 suggestion += ",";
             }
         }
         suggestion += "],";
         String data = "data:[";
         for (int i = 0; i < persons.size(); i++) {
             data += "'" + persons.get(i).id + "'";
             if ((i + 1) != persons.size()) {
                 data += ",";
             }
         }
         data += "]}";
         json += suggestion;
         json += data;
         return json;
     }
 
     public void changeToOldPerson() {
         this.personStatus.add(PersonStatus.oldPerson.value);
     }
 
     public int getStatusOrder() {
         if(this.isEmployee()){
             return 1;
         }
         if(this.isWorker()){
             return 2;
         }
         
         if(this.isVolunteer()){
             return 3;
         }
         if(this.isArt60()){
             return 4;
         }
         if(this.isTIG()){
             return 5;
         }
         
         return 6;
     }
     
     public String getCurrentStatus() {
         if(this.isEmployee()){
             return PersonStatus.employe.value;
         }
         if(this.isWorker()){
             return PersonStatus.worker.value;
         }
         
         if(this.isVolunteer()){
             return PersonStatus.volunteer.value;
         }
         if(this.isArt60()){
             return PersonStatus.art60.value;
         }
         if(this.isTIG()){
             return PersonStatus.tig.value;
         }
         
         return PersonStatus.resident.value;
     }
 }
