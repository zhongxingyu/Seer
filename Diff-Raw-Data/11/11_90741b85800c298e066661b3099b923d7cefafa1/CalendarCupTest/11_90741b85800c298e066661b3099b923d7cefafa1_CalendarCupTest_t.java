 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fofo.entity;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author jnp2
  */
 public class CalendarCupTest {
     CalendarGen generator;
     Competition comp;
     
     public CalendarCupTest() throws InvalidRequisitsException {
         comp = Competition.create(Type.CUP);
         comp.setCategory(Category.MALE);
         comp.setInici(null);
         comp.setMaxTeams(16);
         comp.setMinTeams(4);
        Club club = new Club();
        club.setName("AA");
         List<Team> list = new ArrayList<Team>();
         for(int i=0; i<16;i++){
            list.add(new Team("Team number "+i,club, Category.MALE));
         }
         comp.setTeams(list);
         
         int año = 2013; int mes = 4; int dia = 22; //Fecha anterior 
         GregorianCalendar cal = new GregorianCalendar(año, mes-1, dia);
         Date fecha = new java.sql.Date(cal.getTimeInMillis());
         comp.setInici(fecha);         
     }
 
 
 
     @Test(expected=MinimumDaysException.class)
     public void testDateException() throws Exception {
         comp.setInici(new Date());
         generator = new CalendarGen(comp);  
         generator.CalculateCalendar();
     }
     
     //@Test(expected=NumberOfTeamsException.class)    
     public void testMinTeamsException() throws Exception {
         comp.setMinTeams(20);
         generator = new CalendarGen(comp);  
         generator.CalculateCalendar();
     }   
     
     //@Test(expected=NumberOfTeamsException.class)    
     public void testMaxTeamsException()  throws Exception {
         comp.setMaxTeams(10);
         generator = new CalendarGen(comp); 
         generator.CalculateCalendar();
     }      
     
     @Test   
     public void testNumWeekMatchesException()  throws Exception {
         generator = new CalendarGen(comp);        
         FCalendar calendar = generator.CalculateCalendar();
         int nj = calendar.getNumOfWeekMatches();
         assertEquals(4,nj);  
     }
     
     
     @Test   
     public void testIfAllTeamsParticipateInFirstWeekMatches()  throws Exception {
         generator = new CalendarGen(comp);        
         FCalendar calendar = generator.CalculateCalendar();
         
         WeekMatches first = calendar.getWeekMatch(0);
         List<Match> listMatch = first.getListOfWeekMatches();
         List<Team> expected = comp.getTeams();
         
         if(listMatch.size()*2 != 16) fail();
          
         for(Match match : listMatch){
             Team local = match.getLocal();
             Team visitant = match.getVisitant();
            if(!expected.contains(local) || !expected.contains(visitant)){
                 fail();
             }
         }  
     }
       
    //@Test   
     public void testNumMatchesInEachWeekMatches()  throws Exception {
         generator = new CalendarGen(comp); 
         FCalendar calendar = generator.CalculateCalendar();
   
         int nmatches = 8;
         for(int i=0; i<4; i++){
             assertTrue(AssertEquals(calendar.getWeekMatch(i),nmatches));
             nmatches = nmatches/2;
         }        
     }    
     
     private boolean AssertEquals(WeekMatches match, int nmatches){
         if(match.getNumberOfMatchs() == nmatches) return true;
         else return false;
     }
     
     
 }    
