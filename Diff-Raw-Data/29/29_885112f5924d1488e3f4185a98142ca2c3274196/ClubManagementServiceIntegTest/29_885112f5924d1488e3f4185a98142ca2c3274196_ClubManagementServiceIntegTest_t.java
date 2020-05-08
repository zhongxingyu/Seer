 package org.fofo.services.management;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import org.fofo.dao.ClubDAO;
 import org.fofo.dao.CompetitionDAO;
 import org.fofo.dao.TeamDAO;
 import org.fofo.entity.*;
 import org.fofo.dao.*;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 /**
  *
  * @author josepma, Jordi Niubo i Oriol Capell
  */
 public class ClubManagementServiceIntegTest {
     
     ManagementService service;
     TeamDAOImpl teamDao;
     ClubDAOImpl clubDao;
     Team team1, team2;
     Club club;
     EntityManager em;
     
     public ClubManagementServiceIntegTest() {
     }
 
     @Before
     public void setUp() throws Exception {
         service = new ManagementService();  
         teamDao  = new TeamDAOImpl();
         clubDao  = new ClubDAOImpl();
 
         em = getEntityManager();
         clubDao.setEM(em);
         teamDao.setEM(em);
         
         service.setTeamDao(teamDao);
         service.setClubDao(clubDao);
         team1 = new Team("Team 1");
         team1.setCategory(Category.MALE);  
         team1.setEmail("email@email.com"); 
         
         team2 = new Team("Team 2");      
         team2.setCategory(Category.MALE);  
         team2.setEmail("email@email.com");         
         club = new Club("Club 1");
     }
 
 
      @After
     public void tearDown() throws Exception{
         
         em = clubDao.getEM();
         if (em.isOpen()) em.close();
         
         em = getEntityManager();
         em.getTransaction().begin();
         
         Query query=em.createQuery("DELETE FROM Team");
         Query query2=em.createQuery("DELETE FROM Club");
         int deleteRecords=query.executeUpdate();
         deleteRecords=query2.executeUpdate();
         
         em.getTransaction().commit();
         em.close();
         System.out.println("All records have been deleted.");
          
     }
    
     
     @Test(expected=InscriptionClubException.class)
     public void testInscriptionClubWithNoemail() throws Exception{
         service.addClub(club);        
     }
 
     
     @Test
     public void testCorrectInscriptionClubWithNoTeams() throws Exception{        
         club.setEmail("email@email.com");                    
         service.addClub(club);
         
          Club club1 = clubDao.findClubByName("Club 1");
         
          System.out.println("CLUB DB="+club1);
          System.out.println("CLUB   ="+club);
          
          assertEquals("Clubs should be equals",club,club1);
          
     }  
     
    @Test(expected=InscriptionClubException.class)
     public void testInscriptionClubAlreadyInDB() throws Exception{
         club.setEmail("email@email.com");                    
         service.addClub(club);
         service.addClub(club);
     }  
     
    /*
     * 
     * THE FOLLOWING TESTS DO NOT WORK SINCE THE OP. teamDAO.getTeams() has not 
     * been implemented yet.
     * 
     */
    
     @Test(expected=AlreadyExistingClubOrTeamsException.class)
     public void testInscriptionClubWithTeamAlreadyInDB() throws Exception{
         Club club2 = new Club("Club 2");    
         club2.setEmail("email@email.com");                    
         service.addClub(club2);
         
         Team team3 = new Team(team1.getName(), club2, Category.MALE);
         team3.setEmail("email@email.com");
         teamDao.addTeam(team3);
         
         club.getTeams().add(team1);    
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }     
     
     @Test(expected=AlreadyExistingClubOrTeamsException.class)
     public void testInscriptionClubWithTeamsAlreadyInDB() throws Exception{
         Club club2 = new Club("Club 2");    
         club2.setEmail("email@email.com");                    
         service.addClub(club2);
         
         Team team3 = new Team(team1.getName(), club2, Category.MALE);
         team3.setEmail("email@email.com");
         teamDao.addTeam(team3);
         Team team4 = new Team(team2.getName(), club2, Category.MALE);
         team4.setEmail("email@email.com");
         teamDao.addTeam(team4);
         
         club.getTeams().add(team1);    
         club.getTeams().add(team2);        
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }     
     
    @Test
     public void testCorrectInscriptionClubWithTeam() throws Exception{
         club.getTeams().add(team1);         
         club.setEmail("email@email.com");                    
         service.addClub(club);
         
         Club cluDB = clubDao.findClubByName(club.getName());
         Team teamDB = teamDao.findTeamByName(team1.getName());
         
         assertEquals("Clubs should be equals", club, cluDB);       
         assertEquals("Teams should be equals", team1, teamDB);
     }     
     
    @Test
     public void testCorrectInscriptionClubWithTeams() throws Exception{
         club.getTeams().add(team1);    
         club.getTeams().add(team2);        
         club.setEmail("email@email.com");                    
         service.addClub(club);
         
         Club cluDB = clubDao.findClubByName(club.getName());
         Team team1DB = teamDao.findTeamByName(team1.getName());  
         Team team2DB = teamDao.findTeamByName(team2.getName());
         
         assertEquals("Clubs should be equals", club, cluDB);       
         assertEquals("Teams 1 should be equals", team1, team1DB);
        assertEquals("Teams 2 should be equals", team2, team2DB);
     }     
 
   /*
    * PRIVATE OPS
    * 
    * 
    */
     
      private EntityManager getEntityManager() throws Exception{
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("fofo");
         return emf.createEntityManager();  
     }
 
 
 }
