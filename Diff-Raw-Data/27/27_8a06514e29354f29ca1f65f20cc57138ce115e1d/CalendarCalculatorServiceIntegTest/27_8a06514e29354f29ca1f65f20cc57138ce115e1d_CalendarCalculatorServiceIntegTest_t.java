 package org.fofo.services.management;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.fofo.dao.*;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import org.fofo.entity.*;
 import org.jmock.Expectations;
 import org.joda.time.DateTime;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.After;
 
 /**
  *
  * @author jnp2
  */
 public class CalendarCalculatorServiceIntegTest {
     CalendarCalculatorService  service;
     
     EntityManager em = null;
     
     TeamDAOImpl tdao = null;
     
     CalendarDAOImpl caldao = null;
     
     CompetitionDAOImpl compdao = null;
 
     Competition compCup;    
     Competition compLeague;   
     CalendarGen calCupGen;    
     CalendarGen calLeagueGen;
     
     public CalendarCalculatorServiceIntegTest() {
         
         
     }
 
  
     @Before
     public void setup() throws Exception{
         service = new CalendarCalculatorService();  
         em = getEntityManagerFact(); 
         
         tdao = new TeamDAOImpl();   
         tdao.setEM(em);
         
         caldao = new CalendarDAOImpl();    
         caldao.setTd(tdao);
         caldao.setEm(em);
         
         compdao = new CompetitionDAOImpl(); 
         compdao.setEM(em);
         
         Club club = new Club();
         club.setName("Imaginary club");
         club.setEmail("email@email.com");
         ClubDAOImpl clubdao = new ClubDAOImpl(); 
         clubdao.setEM(em);
         clubdao.addClub(club);
         
         
         compCup = Competition.create(CompetitionType.CUP);
         compCup.setName("Competition Cup");
         createCompetition(compCup, club);  
         
         compLeague = Competition.create(CompetitionType.LEAGUE);
         compLeague.setName("Competition League"); 
         createCompetition(compLeague,club);
         
         addAtDBImaginaryTeamsForCup(club);
         
         calCupGen = new CalendarCupGen();   
         calLeagueGen = new CalendarLeagueGen();
    }    
     
     @After
     public void tearDown() throws Exception{     
         EntityManager em = caldao.getEm();
 
         if (em.isOpen()) em.close();
         
         em = getEntityManagerFact();
         em.getTransaction().begin();
         
         Query query=em.createQuery("DELETE FROM Team st");       
         Query query2=em.createQuery("DELETE FROM Competition comp");     
         Query query3=em.createQuery("DELETE FROM FCalendar cal");     
         Query query4=em.createQuery("DELETE FROM WeekMatch wm");     
        Query query5=em.createQuery("DELETE FROM Match m");        
        Query query6=em.createQuery("DELETE FROM Club");
        
        int deleteRecords=query.executeUpdate();      //Delete Team
        deleteRecords=query2.executeUpdate();          //Delete Competition
        deleteRecords=query3.executeUpdate();          //Delete FCalendar
        deleteRecords=query4.executeUpdate();          //Delete WeekMatch
        deleteRecords=query5.executeUpdate();           //Delete Match       
        deleteRecords=query6.executeUpdate();           //Delete Club          
                   
         em.getTransaction().commit();
         em.close();
         System.out.println("All records have been deleted.");       
      
     }
     
     //@Test 
     public void testCalculateAndStoreLeagueCalendar() throws Exception{                  
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calLeagueGen);
         service.calculateAndStoreCupCalendar(compLeague);
 
         Competition competitionDB = getCompetitionFromDB("Competition League");
         assertEquals("Should have the same competition",compLeague,competitionDB);         
     }
         
     //@Test 
     public void testCalculateAndStoreCupCalendar() throws Exception{                  
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calCupGen);
         service.calculateAndStoreCupCalendar(compCup);
 
         Competition competitionDB = getCompetitionFromDB("Competition Cup");
         assertEquals("Should have the same competition",compCup,competitionDB);          
     }    
     
     //@Test 
     public void testCalculateStoreAndGetLeagueCalendar() throws Exception{                  
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calLeagueGen);
         service.calculateAndStoreCupCalendar(compLeague);
 
         FCalendar calendarDB = null;
         calendarDB = caldao.findFCalendarByCompetitionName("Competition League");
         assertNotNull(calendarDB);              
     }    
     //@Test 
     public void testCalculateStoreAndGetCupCalendar() throws Exception{                  
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calCupGen);
         service.calculateAndStoreCupCalendar(compCup);
 
         FCalendar calendarDB = null;
         calendarDB = caldao.findFCalendarByCompetitionName("Competition Cup");
         assertNotNull(calendarDB);       
     }     
     
     //@Test 
     public void testGetCompetitionOfLeagueCalendar() throws Exception{                  
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calLeagueGen);
         service.calculateAndStoreCupCalendar(compLeague);
 
         FCalendar calendarDB = caldao.findFCalendarByCompetitionName("Competition League");
         assertEquals("Should have the same competition",compLeague,calendarDB.getCompetition());             
     }     
     
     //@Test 
     public void testGetCompetitionOfCupCalendar() throws Exception{                  
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calLeagueGen);
         service.calculateAndStoreCupCalendar(compCup);
 
         FCalendar calendarDB = caldao.findFCalendarByCompetitionName("Competition Cup");
         assertEquals("Should have the same competition",compCup,calendarDB.getCompetition());             
     }     
     
     
     /*
      * 
      * PRIVATE OPERATIONS
      * 
      */
     
     private EntityManager getEntityManagerFact() throws Exception{
 
      try{
          EntityManagerFactory emf = 
                  Persistence.createEntityManagerFactory("fofo");
          return emf.createEntityManager();  
      }
      catch(Exception e){
          System.out.println("ERROR CREATING ENTITY MANAGER FACTORY");
 	 throw e;
      }
 
     }
     
     private void createCompetition(Competition comp, Club club) throws Exception{   
         comp.setCategory(Category.MALE);
         comp.setInici(null);
         comp.setMaxTeams(16);
         comp.setMinTeams(4);
         comp.setInici(new DateTime().minusDays(8).toDate()); 
                
         compdao.addCompetition(comp);
         
         for(int i=0; i<16;i++){
             Team team = new Team("Team number "+i,club, Category.MALE);
             tdao.addTeam(team);
             compdao.addTeam(comp,team);
         }
     }
 
         
     private void addAtDBImaginaryTeamsForCup(Club club) throws Exception {        
         for(int round=1; round<=5;round++){
             int numMatches = 16;
             for(int i=1; i<=numMatches;i++){
                 Team team = new Team("Winer match "+i+" of round "+round,club, Category.MALE);
                 tdao.addTeam(team); 
             }
             numMatches/=2;
         }   
     }
     
     private Competition getCompetitionFromDB(String idcomp) throws Exception{
         em.getTransaction().begin();
         Competition comp = em.find(Competition.class, idcomp);
         em.getTransaction().commit();
      
         return comp;          
    } 
 
 
 }
