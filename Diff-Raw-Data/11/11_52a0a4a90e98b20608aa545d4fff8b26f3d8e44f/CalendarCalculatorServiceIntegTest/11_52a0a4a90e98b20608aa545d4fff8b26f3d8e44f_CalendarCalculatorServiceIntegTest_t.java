 /*
  * EXAMPLE OF INTEGRATION TEST FOR TeamDAOImpl
  */
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
  * @author josepma
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
         
         addAtDBImaginaryTeamsForCup(club);
     }
     
     @After
     public void tearDown() throws Exception{
         EntityManager em = getEntityManagerFact();
           em.getTransaction().begin();
         
           Query query=em.createQuery("DELETE FROM Team st");
           int deleteRecords=query.executeUpdate();
          em.getTransaction().commit();
          em.close();
          System.out.println("All records have been deleted.");
          
 
     }
 
     
     //@Test 
     public void testCalculateAndStoreLeagueCalendar() throws Exception{
                   
         service.setCalendarDao(caldao);        
         service.setCalendarCupGen(calLeagueGen);
         service.calculateAndStoreCupCalendar(compCup);
 
     
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
 
 
 //   private Team getTeamFromDB(String name) throws Exception{
 //
 //         em = getEntityManagerFact();
 //         em.getTransaction().begin();
 //         Team teamDB = em.find(Team.class, name);
 //         em.getTransaction().commit();
 //         em.close();
 // 
 //         return teamDB; 
 //         
 //   } 
     
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
 
 
 }
