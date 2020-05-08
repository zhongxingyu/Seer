 package models;
 
import com.mongodb.BasicDBObject;
 import enums.PassportTypeEnum;
 
 /**
  * User: Charles
  * Date: 5/4/13
  */
 public class Passport {
 
 
     public String passportName;
 
     public String userID;
 
     public PassportTypeEnum type;
 
    public BasicDBObject cardsComplete;
 
     public Passport() {   }
 
     public Passport(String passportName, String userID, PassportTypeEnum type) {
         this.passportName = passportName;
         this.userID = userID;
         this.type = type;
     }
 
 }
