 package org.fofo.services.management;
 
 import org.fofo.dao.*;
 import org.fofo.entity.*;
 
 /**
  *
  * @author jnp2
  */
 public class CalendarCalculatorService {
     
     private CalendarDAO calDao;
     private CalendarGen calLeagueGen;
     private CalendarGen calCupGen;
     
     public CalendarCalculatorService(){
         this.calDao =null;     
         this.calLeagueGen =null;        
         this.calCupGen =null;
     }
     
     public void setCalendarDao(CalendarDAO calDao){
         this.calDao = calDao;
     }
     
     public void setCalendarLeagueGen(CalendarGen cal){
         this.calLeagueGen = cal;
     }
     
     public void setCalendarCupGen(CalendarGen cal){
         this.calCupGen = cal;
     }   
     
     public CalendarDAO getCalendarDao(){
         return calDao;
     }
     
     public void calculateAndStoreLeagueCalendar(Competition comp) throws Exception{
         if(calDao==null) throw new InvalidRequisitsException();
         if(calLeagueGen==null) throw new InvalidRequisitsException();           
         FCalendar cal = calLeagueGen.calculateCalendar(comp);
 
         calDao.addCalendar(cal);
     }
     
     public void calculateAndStoreCupCalendar(Competition comp) throws Exception{
         if(calDao==null) throw new InvalidRequisitsException();
        CalendarCupGen calCupGen = new CalendarCupGen();
         
        //if(calCupGen==null) throw new InvalidRequisitsException();        
         FCalendar cal = calCupGen.calculateCalendar(comp);
 
         calDao.addCalendar(cal);
     }
     
 }
