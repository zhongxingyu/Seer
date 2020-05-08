 package dev;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.inbox.Mail;
 import models.person.Department;
 import models.person.Gender;
 import models.person.Person;
 import models.residence.Residence;
 import models.residence.ResidenceType;
 
 import org.joda.time.LocalDate;
 
 import com.google.common.collect.Lists;
 
 /**
  * @author adericbourg
  */
 public class DevDataFactory {
 
     public static void createData() {
         if (!Person.findAll().isEmpty()) {
             return;
         }
 
         // Create persons for prostitution department.
         createPerson1();
         createPerson2();
         createPerson3();
         createPerson4();
 
         // Create persons for precarity department.
         createPerson5();
         createPerson6();
         createPerson7();
         createPerson8();
 
         List<Person> persons = Person.findAll();
 
         // Register mail.
         Mail mail;
         for (int i = 0; i < persons.size() * 5; i++) {
             mail = new Mail();
             mail.recipient = randomPerson(persons);
             mail.sender = "Expéditeur " + i;
             mail.arrivalDate = randomDay();
             if (i % 10 == 0) {
                 mail.withdrawalDate = mail.arrivalDate.plusDays((int) (Math.random() * 60));
             }
             mail.save();
         }
     }
 
     private static LocalDate randomDay() {
         return LocalDate.now().minusDays((int) (Math.random() * 300));
     }
 
     private static Person randomPerson(List<Person> persons) {
         return persons.get((int) (Math.random() * (persons.size() - 1)));
     }
 
     private static void createPerson1() {
         Person person = new Person();
         person.firstName = "Jeanine";
         person.lastName = "Durand";
         person.gender = Gender.FEMALE;
         person.birthDate = LocalDate.parse("1980-05-12");
         person.followedBy = "VLM";
         person.followingDepartment = Department.PROSTITUTION;
         person.isFollowed = true;
         person.orientation = "Bus des femmes";
 
         Residence pastResidence = createResidence(LocalDate.now().minusYears(2), ResidenceType.STATE_MEDICAL_SUPPORT);
         Residence currentResidence = createResidence(LocalDate.now().minusMonths(1), ResidenceType.STATE_MEDICAL_SUPPORT);
         person.residences = Lists.newArrayList(pastResidence, currentResidence);
 
         person.save();
     }
 
     private static void createPerson2() {
         Person person = new Person();
         person.firstName = "Pi Wai";
         person.lastName = "Wang";
         person.gender = Gender.FEMALE;
         person.birthDate = LocalDate.parse("1987-04-14");
         person.followedBy = "VLM";
         person.followingDepartment = Department.PROSTITUTION;
         person.isFollowed = true;
 
         person.residences = Lists.newArrayList(createResidence(LocalDate.now().minusWeeks(7), ResidenceType.STATE_MEDICAL_SUPPORT));
 
         person.save();
     }
 
     private static void createPerson3() {
         Person person = new Person();
         person.firstName = "Patience";
         person.lastName = "Nsota";
         person.gender = Gender.FEMALE;
         person.birthDate = LocalDate.parse("1992-11-24");
         person.followedBy = "VLM";
         person.followingDepartment = Department.PROSTITUTION;
         person.isFollowed = true;
 
         person.residences = Lists.newArrayList(createResidence(LocalDate.now().minusMonths(7), ResidenceType.STATE_MEDICAL_SUPPORT));
 
         person.save();
     }
 
     private static void createPerson4() {
         Person person = new Person();
         person.firstName = "Germaine";
         person.lastName = "Moulin";
         person.gender = Gender.FEMALE;
         person.birthDate = LocalDate.parse("1954-09-13");
         person.followedBy = "VLM";
         person.followingDepartment = Department.PROSTITUTION;
         person.isFollowed = true;
         person.residences = new ArrayList<Residence>();
 
        LocalDate residenceStartDate = LocalDate.now().minusYears(25).minusWeeks(4);
         while (LocalDate.now().isAfter(residenceStartDate)) {
             person.residences.add(createResidence(residenceStartDate, ResidenceType.STATE_MEDICAL_SUPPORT));
             residenceStartDate = residenceStartDate.plusYears(1);
         }
         person.save();
     }
 
     private static void createPerson5() {
         Person person = new Person();
         person.firstName = "Jean";
         person.lastName = "Pierre";
         person.gender = Gender.MALE;
         person.birthDate = LocalDate.parse("1992-11-24");
         person.followedBy = "MD";
         person.followingDepartment = Department.PRECARITY;
         person.isFollowed = true;
 
         person.residences = Lists.newArrayList(createResidence(LocalDate.now().minusMonths(9), ResidenceType.ADMINISTRATIVE));
 
         person.save();
     }
 
     private static void createPerson6() {
         Person person = new Person();
         person.firstName = "Fabrice";
         person.lastName = "Gentil";
         person.gender = Gender.MALE;
         person.birthDate = LocalDate.parse("1972-11-24");
         person.followedBy = "MD";
         person.followingDepartment = Department.PRECARITY;
         person.isFollowed = true;
         person.orientation = "AIDES";
         person.residences = new ArrayList<Residence>();
 
         LocalDate residenceStartDate = LocalDate.now().minusYears(4);
         while (LocalDate.now().isAfter(residenceStartDate)) {
             person.residences.add(createResidence(residenceStartDate, ResidenceType.ADMINISTRATIVE));
             residenceStartDate = residenceStartDate.plusYears(1);
         }
         person.save();
     }
 
     private static void createPerson7() {
         Person person = new Person();
         person.firstName = "Christophe";
         person.lastName = "Allegre";
         person.gender = Gender.MALE;
         person.birthDate = LocalDate.parse("1942-01-24");
         person.followedBy = "MD";
         person.followingDepartment = Department.PRECARITY;
         person.isFollowed = false;
         person.orientation = "Emmaüs";
 
         person.save();
     }
 
     private static void createPerson8() {
         Person person = new Person();
         person.firstName = "Steven";
         person.lastName = "Peter";
         person.gender = Gender.MALE;
         person.birthDate = LocalDate.parse("1952-01-08");
         person.followedBy = "MD";
         person.followingDepartment = Department.PRECARITY;
         person.isFollowed = true;
         person.orientation = "Abitbol";
         person.residences = new ArrayList<Residence>();
 
         LocalDate residenceStartDate = LocalDate.now().minusYears(2);
         while (LocalDate.now().isAfter(residenceStartDate)) {
             person.residences.add(createResidence(residenceStartDate, ResidenceType.STATE_MEDICAL_SUPPORT));
             residenceStartDate = residenceStartDate.plusYears(1);
         }
 
         person.save();
     }
 
     private static Residence createResidence(LocalDate startDate, ResidenceType type) {
         Residence residence = new Residence();
         residence.startDate = startDate;
         residence.endDate = residence.startDate.plusYears(1).minusDays(1);
         residence.residenceType = type;
         return residence;
     }
 }
