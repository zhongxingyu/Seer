 /**
  * A general test for the database system.
  */
 
 package tests;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.google.gson.Gson;
 
 import db.*;
 import db.rules.*;
 import util.*;
 
 public class DBTest {
 
     public static void status(String str) {
         System.out.println(str);
     }
 
     public static void main(String args[]) {
         FopsDB db = new FopsDB();
         Gson gson = new Gson();
         List<Rule> rules = new ArrayList<Rule>();
         List<Staff> faculty = new ArrayList<Staff>();
 
         /* Construct Rules */
         status("Constructing rules");
         rules.add(new MinimumYearsServiceRule(3));
         rules.add(new NumberRepresentativesRule(1));
         rules.add(new NumberAtLargeMembersRule(2));
         rules.add(new MinimumRankRule(DataTypes.Rank.ASSOCIATE));
 
         /* Add Committees */
         status("Adding committees");
         String com1 = "Test Committee One";
         String com2 = "Test Committee Two";
         db.addCommittee(new Committee(com1,
                 DataTypes.CommitteeType.UNIVERSITY, rules));
         db.addCommittee(new Committee(com2,
                 DataTypes.CommitteeType.AD_HOC, null));
 
         /* Construct Staff */
         status("Constructing staff");
         faculty.add(new Staff("John Doe", DataTypes.Gender.MALE, 
                 DataTypes.College.BUSINESS, DataTypes.Discipline.LATIN, 
                 DataTypes.Rank.PROFESSOR, new MonthYear(9, 1985), true));
         faculty.add(new Staff("Jane Doe", DataTypes.Gender.FEMALE,
                 DataTypes.College.ARTS_AND_SCIENCES,
                 DataTypes.Discipline.BIOLOGY, DataTypes.Rank.INSTRUCTOR,
                new MonthYear(1, 2011), false));
 
         /* Add Staff */
         status("Adding staff");
         for (Staff staff : faculty) {
             db.addFaculty(staff);
         }
 
         /* Check Staff Against Rules*/
         status("Checking against rules");
         String validity;
         String format;
         for (Staff staff : faculty) {
             boolean canJoin = true; 
             for (Rule rule : rules) {
                 boolean isValid = 
                         rule.isValidMember(db.getCommittee(com1), staff);
                 System.out.printf("%s: %s %s join.\n",
                         rule.getDescription(), staff.getName(),
                         isValid ? "can" : "cannot");
             }
         }
 
         gson.toJson(db, System.out); 
         System.out.println();
     }
 }
