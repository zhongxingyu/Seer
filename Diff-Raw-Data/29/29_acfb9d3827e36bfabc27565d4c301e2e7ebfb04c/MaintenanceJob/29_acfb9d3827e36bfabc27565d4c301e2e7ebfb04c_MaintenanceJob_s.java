 package jobs;
 
 import ext.Ext;
 import ext.ExtAttendance;
import java.io.*;
import java.util.*;
 import models.*;
 import play.Play;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 
//@OnApplicationStart
 public class MaintenanceJob extends Job {
 
     @Override
     public void doJob() throws Exception {
         if (JobFailBack.stopJob()) {
             return;
         }
 
        
     }
 
     private void addHolidayTypeRH() {
         List<String> status = Arrays.asList(PersonStatus.employe.value, PersonStatus.worker.value);
         List<Contract> contracts = Contract.find("from Contract c,in(c.person.personStatus) ps "
                 + "where ps in (:status) ").bind("status", status).fetch();
 
         int i = 1;
         for (Contract contract : contracts) {
             contract = contract.merge();
             Holiday holiday = Holiday.getCurrent(contract.person);
             if (holiday != null && holiday.holidayTypes.size() == 22) {
                 HolidayType ht = new HolidayType();
                 ht.type = 22;
                 holiday.holidayTypes.add(ht);
                 holiday.save();
             }
 
             if (i % 20 == 0) {
                 System.out.println(i);
                 Holiday.em().flush();
                 Holiday.em().getTransaction().commit();
                 Holiday.em().getTransaction().begin();
                 Holiday.em().clear();
             }
 
             i++;
         }
 
     }
 
     private void computeHolidayRH() {
         List<String> status = Arrays.asList(PersonStatus.employe.value, PersonStatus.worker.value);
         List<Contract> contracts = Contract.find("from Contract c,in(c.person.personStatus) ps "
                 + "where ps in (:status) ").bind("status", status).fetch();
 
         int i = 1;
         for (Contract contract : contracts) {
             contract = contract.merge();
             Holiday holiday = Holiday.getCurrent(contract.person);
             if (holiday == null || holiday.holidayTypes.size() < 22) {
                 continue;
             }
 
             holiday.totalDone = 0;
 
             for (int j = 0; j < 22; j++) {
                 holiday.holidayTypes.get(j).done = 0;
             }
 
             List<HolidayBook> holidayBooks = HolidayBook.byPerson(contract.person);
 
             for (HolidayBook holidayBook : holidayBooks) {
                 if (holidayBook.isAM || holidayBook.isPM) {
                     holiday.incrementDone(holidayBook.holidayType, 0.5);
                 } else {
                     holiday.incrementDone(holidayBook.holidayType, 1);
                 }
             }
 
             holiday.save();
 
             if (i % 20 == 0) {
                 System.out.println(i);
                 Holiday.em().flush();
                 Holiday.em().getTransaction().commit();
                 Holiday.em().getTransaction().begin();
                 Holiday.em().clear();
             }
 
             i++;
         }
     }
 
     private void computeHolidayArt60() {
         List<Contract> contracts = Contract.find("from Contract c,in(c.person.personStatus) ps "
                 + "where ps = ? ", PersonStatus.art60.value).fetch();
 
         int i = 1;
         for (Contract contract : contracts) {
             contract = contract.merge();
             Holiday holiday = Holiday.getCurrent(contract.person);
             if (holiday == null) {
                 holiday = Holiday.newInstanceArt60(contract.person);
             }
 
             holiday.holidayTypes.get(0).done = 0;
             holiday.holidayTypes.get(1).done = 0;
             holiday.holidayTypes.get(2).done = 0;
             holiday.holidayTypes.get(3).done = 0;
 
             List<Attendance> attendances = Attendance.holidaysArt60(contract.person);
 
             for (Attendance attendance : attendances) {
                 if (attendance.halfDay) {
                     switch (attendance.statusAM) {
                         case 6:
                             holiday.incrementDone(0, 0.5);
                             break;
                         case 7:
                             holiday.incrementDone(1, 0.5);
                             break;
                         case 8:
                             holiday.incrementDone(2, 0.5);
                             break;
                         case 23:
                             holiday.incrementDone(3, 0.5);
                             break;
                     }
                     switch (attendance.statusPM) {
                         case 6:
                             holiday.incrementDone(0, 0.5);
                             break;
                         case 7:
                             holiday.incrementDone(1, 0.5);
                             break;
                         case 8:
                             holiday.incrementDone(2, 0.5);
                             break;
                         case 23:
                             holiday.incrementDone(3, 0.5);
                             break;
                     }
                 } else {
                     switch (attendance.status) {
                         case 6:
                             holiday.incrementDone(0, 1);
                             break;
                         case 7:
                             holiday.incrementDone(1, 1);
                             break;
                         case 8:
                             holiday.incrementDone(2, 1);
                             break;
                         case 23:
                             holiday.incrementDone(3, 1);
                             break;
                     }
                 }
             }
 
             holiday.save();
 
             if (i % 20 == 0) {
                 System.out.println(i);
                 Holiday.em().flush();
                 Holiday.em().getTransaction().commit();
                 Holiday.em().getTransaction().begin();
                 Holiday.em().clear();
             }
 
             i++;
         }
     }
 
     private void createHolidayBookArt60() {
         List<Contract> contracts = Contract.find("from Contract c,in(c.person.personStatus) ps "
                 + "where ps = ? ", PersonStatus.art60.value).fetch();
 
         int i = 1;
         for (Contract c : contracts) {
             c = c.merge();
             Person p = c.person;
 
             Holiday h = Holiday.getCurrent(p);
 
             if (h == null) {
                 h = new Holiday();
             } else {
                 if (h.holidayTypes.size() != 4) {
                     System.out.println("new");
                     HolidayType ht = new HolidayType();
                     ht.type = 3;
                     ht.save();
                     h.holidayTypes.add(ht);
                     h.save();
                 }
                 continue;
             }
 
             h.person = p;
             h.year = 2012;
             h.holidayTypes = HolidayType.createForArt60();
             h.holidayTypes.get(0).total = c.nbHolydays;
             h.holidayTypes.get(0).done = c.nbHolydays;
             h.total = 0;
             h.save();
 
             if (i % 20 == 0) {
                 System.out.println(i);
                 Holiday.em().flush();
                 Holiday.em().getTransaction().commit();
                 Holiday.em().getTransaction().begin();
                 Holiday.em().clear();
             }
 
             i++;
         }
     }
 
     private void createHolidayBook() {
         List<Person> persons = Person.findRHPersons();
 
         int i = 1;
         for (Person p : persons) {
             p = p.merge();
             Holiday h = Holiday.getCurrent(p);
 
             if (h == null) {
                 h = new Holiday();
             } else {
                 //continue;
             }
 
             h.person = p;
             h.year = 2012;
             if (h.holidayTypes == null) {
                 h.holidayTypes = new ArrayList<HolidayType>();
             } else {
                 h.holidayTypes.clear();
             }
 
             List<HolidayType> holidaysType = HolidayType.createForRH();
             for (HolidayType holidayType : holidaysType) {
                 h.holidayTypes.add(holidayType);
             }
 
             h.total = 0;
             h.save();
 
             if (i % 20 == 0) {
                 System.out.println(i);
                 Holiday.em().flush();
                 Holiday.em().getTransaction().commit();
                 Holiday.em().getTransaction().begin();
                 Holiday.em().clear();
             }
 
             i++;
         }
     }
 
     private void personList() throws Exception {
         BufferedWriter out = new BufferedWriter(
                 new OutputStreamWriter(
                 new FileOutputStream(Play.applicationPath.getAbsolutePath() + File.separator
                 + "personnel.csv"), "ISO-8859-1"));
 
         List<String> status = PersonStatus.getRHStatus();
 
         List<Person> persons = Person.find("from Person p,in(p.personStatus) ps "
                 + "where p.department is not null "
                 + "and ps in (:status) "
                 + "and p.enabled = true "
                 + "order by p.name ").bind("status", status).fetch();
 
         out.write("NOM;PRENOM;SERVICE\n");
         for (Person p : persons) {
             out.write(Ext.capFirst(p.name) + ";" + Ext.capFirst(p.firstname) + ";" + p.department.name + "\n");
         }
 
         out.flush();
         out.close();
     }
 
     private void updateEmployerStatus() {
         List<Employer> employers = Employer.getCPAS();
 
         int i = 1;
         for (Employer e : employers) {
             e = e.merge();
 
             for (int j = 1; j < 39; j++) {
                 if(j == 4){
                     e.attendanceStatus.set(3, "BS");
                 }
                 if (j > e.attendanceStatus.size()) {
                     e.attendanceStatus.add(ExtAttendance.statusToAbrev(j));
                 }
             }
             //e.attendanceStatus = null;
             //e.initAttendanceStatusIfEmpty();
             e.save();
 
             if (i % 20 == 0) {
                 Employer.em().flush();
                 Employer.em().getTransaction().commit();
                 Employer.em().clear();
                 Employer.em().getTransaction().begin();
             }
 
             i++;
         }
     }
 
     private void initEmployerStatus() {
         List<Employer> employers = Employer.getCPAS();
 
         int i = 1;
         for (Employer e : employers) {
             e = e.merge();
             e.attendanceStatus = null;
             e.initAttendanceStatusIfEmpty();
             e.save();
 
             if (i % 20 == 0) {
                 Employer.em().flush();
                 Employer.em().getTransaction().commit();
                 Employer.em().clear();
                 Employer.em().getTransaction().begin();
             }
 
             i++;
         }
     }
 
     private void correctInfoSource() {
         InfoSource source = InfoSource.find("name = ?", "Particulier").first();
         if (source == null) {
             source = new InfoSource();
             source.name = "Particulier";
             source.save();
         }
 
         List<Info> infos = Info.all().fetch();
         for (Info info : infos) {
             info.source = source;
             info.save();
         }
     }
 }
