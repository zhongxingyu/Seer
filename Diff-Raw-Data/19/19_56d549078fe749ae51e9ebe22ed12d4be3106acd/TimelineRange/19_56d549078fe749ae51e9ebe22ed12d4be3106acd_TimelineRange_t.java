 package com.rcs.liferaysense.entities.enums;
 
 import java.util.*;
 
 /**
  * @author Prj.M@x <pablo.rendon@rotterdam-cs.com>
  */
 public enum TimelineRange {
     REAL_TIME(5) { 
         @Override
         public Date getDate() {            
             Calendar gc;
             gc = GregorianCalendar.getInstance();
             gc.setTime(new Date());
            gc.add(Calendar.MINUTE, -30);
             return gc.getTime();
         }
     },
     LAST_HOUR(4) { 
         @Override
         public Date getDate() {            
             Calendar gc;
             gc = GregorianCalendar.getInstance();
             gc.setTime(new Date());         
             gc.add(Calendar.HOUR, -1);
             return gc.getTime();
         }
     },
     LAST_DAY(3) {  
         @Override
         public Date getDate() {           
             Calendar gc;
             gc = GregorianCalendar.getInstance();
             gc.setTime(new Date());          
             gc.add(Calendar.HOUR, -24);
             return gc.getTime();
         }
     },
     LAST_WEEK(2) {  
         @Override
         public Date getDate() {           
             Calendar gc;
             gc = GregorianCalendar.getInstance();
             gc.setTime(new Date());          
             gc.add(Calendar.DAY_OF_YEAR, -7);
             return gc.getTime();
         }
     },
     LAST_MONTH(1) {  
         @Override
         public Date getDate() {            
             Calendar gc;
             gc = GregorianCalendar.getInstance();
             gc.setTime(new Date());         
             gc.add(Calendar.MONTH, -1);
             return gc.getTime();
         }
     };
     
     private int id;
     private static Map<Integer, Date> lookup = new HashMap<Integer, Date>();
     
     abstract Date getDate();
     
 //    static {
 //        for (TimelineRange s : TimelineRange.values()) {
 //            lookup.put(s.getId(), s.getDate());
 //        }
 //    }
     
     private TimelineRange (int r) {        
         id = r;
     }
     
     public int getId() {
         return id;
     }
     
     public static Date get(int id) {
         lookup = new HashMap<Integer, Date>();
         for (TimelineRange s : TimelineRange.values()) {
             lookup.put(s.getId(), s.getDate());
         }
         return lookup.get(id);
     }
     
 }
