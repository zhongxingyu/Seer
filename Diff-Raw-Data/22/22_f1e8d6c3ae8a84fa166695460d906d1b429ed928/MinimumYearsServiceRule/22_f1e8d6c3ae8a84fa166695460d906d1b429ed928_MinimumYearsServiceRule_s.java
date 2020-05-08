 /**
  * Rule specifying how long a committee member must have served at UMW.
  */
 
 package db.rules;
 
 import util.MonthYear;
 import db.*;
 
 public class MinimumYearsServiceRule implements Rule {
 
     private String description;
     private int minYearsService;
 
     public MinimumYearsServiceRule(int minYearsService) {
         this.minYearsService = minYearsService;
         description = String.format(
                 "This committee requires that members have served for %d "
                 + "years at UMW.", minYearsService);
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public boolean isValidMember(Committee committee, Staff faculty) {
         return (faculty.getStartDate().timeSince().getYear()
                < minYearsService);
     }
 }
