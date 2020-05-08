 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package netcracker.dao;
 
 /**
  * Contains constants for DB and table names
  *
  * @author lasha.k;
  */
 final public class DAOConstants {
 
     /**
      * temlate for url jdbc:mysql://[host][,failoverhost...][:port]/[database]
      */
     static public final String url = "jdbc:mysql://localhost/waytonetcracker?useUnicode=true&characterEncoding=Cp1251";
     /**
      * class name of mysql driver
      */
     static public final String driver = "com.mysql.jdbc.Driver";
     /**
      * user name for mysql db connection
      */
     static public final String user = "root";
     /**
      * password for mysql db user
      */
     static public final String password = "13adsf";
     /**
      * table name "Employees"
      */
     static public final String EmpTableName = "employees";
     /**
      * table name "Students"
      */
     static public final String StudentsTableName = "students";
     /**
      * table name "Students"
      */
     static public final String UniversitiesTableName = "universities";
     /**
      * table name "Faculties"
      */
     static public final String FacultiesTableName = "faculties";
     /**
      * table name "results"
      */
     static public final String ResultsTableName = "results";
     /**
      * table name "adverts"
      */
     static public final String AdvertsTableName = "adverts";
     /**
      * table name "advertsForStudents"
      */
     static public final String AdvertsForStudentsTableName = "advertsForStudents";
     /**
      * table name "interests"
      */
     static public final String InterestsTableName = "interests";
     /**
      * table name "interestsForStudents"
      */
     static public final String InterestsForStudentsTableName = "interestsForStudents";
     /**
      * table name "intervals"
      */
     static public final String IntervalsTableName = "intervals";
     /**
      * table name "intervalsForStudents"
      */
     static public final String IntervalsForStudentsTableName = "intervalsForStudents";
     /**
      * table name "intervalStatuses"
      */
     static public final String IntervalStatusesTableName = "intervalStatuses";
     /**
      * table name "messages"
      */
     static public final String MessagesTableName = "messages";
     /**
      * table name "roles"
      */
     static public final String RolesTableName = "roles";
     /**
      * table name "skills"
      */
     static public final String SkillsTableName = "skills";
     /**
      * table name "skillsForStudents"
      */
     static public final String SkillsForStudentsTableName = "skillsForStudents";
     /**
      * table name "skillsTypes"
      */
     static public final String SkillsTypesForStudentsTableName = "skillsTypes";
     static public final String InterestStudying = "studying";
     static public final String InterestWorking = "working";
     static public final String InterestSoftwareDeveloping = "software developing";
     static public final String InterestOther = "other";
     static public final String InterestDeepSpecialization = "deep specialization";
     static public final String InterestDifferentJob = "different job";
     static public final String InterestExpertsManagement = "experts menegement";
     static public final String InterestSale = "sale";
     static public final String InterestOtherJob = "other job";
     static public final String SkillTypeLanguage = "language";
     static public final String SkillTypeKnowledge = "knowledge";
     static public final String SkillTypeEnglish = "english";
     static public final String SkillCPP = "C++";
     static public final String SkillJava = "Java";
     static public final String SkillNetworkTechnologies = "network technologies";
     static public final String SkillAlgorithms = "algorithms";
     static public final String SkillOOP = "OOP";
     static public final String SkillDB = "DB";
     static public final String SkillWeb = "Web";
     static public final String SkillGUI = "GUI";
     static public final String SkillNetworkProgramming = "network programming";
     static public final String SkillProgramDesign = "program design";
     static public final String SkillReading = "reading";
     static public final String SkillWriting = "writing";
     static public final String SkillSpeaking = "speaking";
 
     private DAOConstants() {
         // this constructor is intentionally private 
     }
 }
