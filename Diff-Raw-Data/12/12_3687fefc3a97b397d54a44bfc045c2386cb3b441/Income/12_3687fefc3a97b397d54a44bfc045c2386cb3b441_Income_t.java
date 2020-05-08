 
 package budgetforce.control;
 
 import budgetforce.model.DatabaseManager;
 import budgetforce.model.EPeriod;
 import static budgetforce.model.EPeriod.DAY;
 import static budgetforce.model.EPeriod.MONTH;
 import static budgetforce.model.EPeriod.ONCE;
 import static budgetforce.model.EPeriod.QUARTER;
 import static budgetforce.model.EPeriod.WEEK;
 import static budgetforce.model.EPeriod.YEAR;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 
 /**
  *
  * @author Soi Fon
  */
 public class Income 
 {
     Calendar c;
       
     public Income(int personID)
     {
         c = Calendar.getInstance();
         modelIncome = DatabaseManager.getDatabaseManager().getIncomeByPersonID(personID);      
     }
     
     public float MonthIncome(int year, int month)
     { 
         float completteIncome = 0.0f;
 
         year -= 1900;
         
         for (budgetforce.model.Income income : modelIncome) 
         {
             Timestamp start = income.getStart();
             Timestamp end   = income.getEnd();
             
             if(start.getYear() == year)
             {
                 //period starts before and ends after this month
                 if(start.getMonth() < month && end.getMonth() > month)
                 {            
                     switch(income.getPeriod())
                     {
                         case DAY    : completteIncome += income.getAmount() *  DateHandle.GetDaysOfMonth(year, month);     break;
                         case WEEK   : completteIncome += income.getAmount() * (DateHandle.GetDaysOfMonth(year, month) / 7);break;
                         case MONTH  : completteIncome += income.getAmount();     break;
                         case QUARTER: completteIncome += income.getAmount() / 3; break;
                         case YEAR   : completteIncome += income.getAmount() / 12;break;
                     }     
                 }
                 //Period ends this month
                 if(start.getMonth() < month && end.getMonth() == month)
                 {
                     float days = end.getDate() + 1;
                     switch(income.getPeriod())
                     {
                         case DAY    : completteIncome +=  income.getAmount() *        days;     break;
                         case WEEK   : completteIncome +=  income.getAmount() *       (days / 7);break;
                         case MONTH  : completteIncome +=  income.getAmount() *       (days / DateHandle.GetDaysOfMonth(year, month));break;
                         case QUARTER: completteIncome += (income.getAmount() /  3) * (days / DateHandle.GetDaysOfMonth(year, month));break;
                         case YEAR   : completteIncome += (income.getAmount() / 12) * (days / DateHandle.GetDaysOfMonth(year, month));break;
                     }                   
                 }
                 //Period starts this month
                 if(start.getMonth() == month && end.getMonth() > month)
                 {
                     float daysLeft = (DateHandle.GetDaysOfMonth(year, month) - start.getDate()) + 1;   
                     switch(income.getPeriod())
                     {
                         case DAY    : completteIncome +=  income.getAmount() *        daysLeft;     break;
                         case WEEK   : completteIncome +=  income.getAmount() *       (daysLeft / 7);break;
                         case MONTH  : completteIncome +=  income.getAmount() *       (daysLeft / DateHandle.GetDaysOfMonth(year, month));break;
                         case QUARTER: completteIncome += (income.getAmount() /  3) * (daysLeft / DateHandle.GetDaysOfMonth(year, month));break;
                         case YEAR   : completteIncome += (income.getAmount() / 12) * (daysLeft / DateHandle.GetDaysOfMonth(year, month));break;
                     }   
                 }
                 //Period starts and ends this month
                 if(start.getMonth() == month && end.getMonth() == month)
                 {
                     float days = end.getDate() - start.getDate() + 1;                    
                     switch(income.getPeriod())
                     {
                        case ONCE   : completteIncome += income.getAmount();             break;
                        case DAY    : completteIncome += income.getAmount() *  days;     break;
                        case WEEK   : completteIncome += income.getAmount() * (days / 7);break;
                        case MONTH  : completteIncome += income.getAmount();break; //there should not be a month , quarter or year period with 
                        case QUARTER: completteIncome += income.getAmount();break; //a start and end date within a month anyway, so just the full amount will be
                        case YEAR   : completteIncome += income.getAmount();break; //added in this case
                     }
                 }            
             }
         }
         return completteIncome;
     }
     
     private ArrayList<budgetforce.model.Income> modelIncome;
 }
