 package org.fofo.services.management;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.PersistenceException;
 import org.fofo.dao.ClubDAO;
 import org.fofo.dao.CompetitionDAO;
 import org.fofo.dao.TeamDAO;
 import org.fofo.entity.*;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.AfterClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 /**
  *
  * @author jnp2
  */
 public class ClubManagementServiceTest {
     
     ManagementService service;
     Mockery context = new JUnit4Mockery();
     TeamDAO teamDao;
     ClubDAO clubDao;
     Team team1, team2;
     Club club;
     
     public ClubManagementServiceTest() {
     }
 
     @Before
     public void setUp() throws Exception {
         service = new ManagementService();  
         teamDao  = context.mock(TeamDAO.class);
         clubDao  = context.mock(ClubDAO.class);
         service.setTeamDao(teamDao);
         service.setClubDao(clubDao);
         team1 = new Team("Team 1");
        team2 = new Team("Team 2");  
         team1.setCategory(Category.MALE);  
        team1.setEmail("email@email.com");     
         team2.setCategory(Category.MALE);  
         team2.setEmail("email@email.com");         
         club = new Club("Club 1");
     }
 
 
     @Test(expected=InscriptionClubException.class)
     public void testInscriptionClubWithNoemail() throws Exception{
         service.addClub(club);
     }
 
     @Test
     public void testCorrectInscriptionClubWithNoTeams() throws Exception{
         context.checking(new Expectations() {{
             oneOf (clubDao).findClubByName(club.getName());will(returnValue(null));
             oneOf (clubDao).addClub(club);
         }});  
         
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }  
     
     @Test(expected=InscriptionClubException.class)
     public void testInscriptionClubAlreadyInDB() throws Exception{
         context.checking(new Expectations() {{
             oneOf (clubDao).findClubByName(club.getName());will(returnValue(club));
         }});  
         
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }  
     
     @Test(expected=InscriptionClubException.class)
     public void testInscriptionClubWithTeamAlreadyInDB() throws Exception{
         final List<Team> list = new ArrayList<Team>();
         list.add(team1);
         club.getTeams().add(team1);
         context.checking(new Expectations() {{
             oneOf (clubDao).findClubByName(club.getName());will(returnValue(null));
             oneOf (teamDao).getTeams();will(returnValue(list));
         }});  
         
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }     
     
     @Test(expected=InscriptionClubException.class)
     public void testInscriptionClubWithTeamsAlreadyInDB() throws Exception{
         final List<Team> list = new ArrayList<Team>();
         list.add(team1);
         list.add(team2);
         club.getTeams().add(team1);    
         club.getTeams().add(team2);
         context.checking(new Expectations() {{
             oneOf (clubDao).findClubByName(club.getName());will(returnValue(null));
             oneOf (teamDao).getTeams();will(returnValue(list));
         }});  
         
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }     
     
    //@Test
     public void testCorrectInscriptionClubWithTeam() throws Exception{
        final List<Team> list = new ArrayList<Team>();
         
         club.getTeams().add(team1);   
 
         context.checking(new Expectations() {{
             oneOf (clubDao).findClubByName(club.getName());will(returnValue(null));
            oneOf (teamDao).getTeams();will(returnValue(list));
             oneOf (clubDao).addClub(club);
             oneOf (teamDao).addTeam(team1);
         }});  
         
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }     
     
    //@Test
     public void testCorrectInscriptionClubWithTeams() throws Exception{
         final List<Team> list = new ArrayList<Team>();
         final List<Club> listClubs = new ArrayList<Club>();
         listClubs.add(club);
         
         club.getTeams().add(team1);    
         club.getTeams().add(team2);
         
         context.checking(new Expectations() {{
             oneOf (clubDao).findClubByName(club.getName());will(returnValue(null));
             oneOf (teamDao).getTeams();will(returnValue(list));
             oneOf (clubDao).addClub(club);
             oneOf (clubDao).getClubs();will(returnValue(listClubs));
            oneOf (teamDao).addTeam(team1);         
             oneOf (teamDao).addTeam(team2);
         }});  
         
         club.setEmail("email@email.com");                    
         service.addClub(club);
     }     
 }
