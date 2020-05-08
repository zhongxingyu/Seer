 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fofo.dao;
 
 import java.util.Arrays;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import org.fofo.entity.Club;
 import org.fofo.entity.Competition;
 import org.fofo.entity.CompetitionLeague;
 import org.fofo.entity.Team;
 import org.jmock.Expectations;
 import static org.jmock.Expectations.returnValue;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 
 /**
  *
  * @author Alejandro
  */
 @RunWith(JMock.class)
 public class CompetitionDAOImplTest {
 
     Mockery context = new JUnit4Mockery();
     EntityManager em;
     EntityTransaction transaction;
     CompetitionDAOImpl competitionDAO;
     Team team;
     Competition competition;
     Club club;
 
     @Before
     public void setUp() {
 
         em = context.mock(EntityManager.class);
         transaction = context.mock(EntityTransaction.class);
         competitionDAO = new CompetitionDAOImpl();
         team = new Team("name");
         competition = new CompetitionLeague();
        competition.setName("LeagueOne");
         club = new Club("name");
        club.setEmail("email");
         competitionDAO.setEM(em);
         team.setClub(club);
 
     }
 
     @Test
     public void addCompetition() throws PersistException {
 
         context.checking(new Expectations() {
             {
                 atLeast(1).of(em).getTransaction();
                 will(returnValue(transaction));
                 oneOf(transaction).begin();
                 oneOf(transaction).commit();
                 oneOf(em).persist(competition);
                 oneOf(em).isOpen();
                 will(returnValue(true));
                 oneOf(em).close();
 
 
             }
         });
 
         competitionDAO.addCompetition(competition);
     }
 
     @Test
     public void addTeam_To_Competition() throws Exception {
 
         // 1º Llamar a find competetition y team
         //2º Y se llama a compAux.getTeam.add
         List<Team> list = Arrays.asList(team);
 
         context.checking(new Expectations() {
             {
                 atLeast(1).of(em).getTransaction();
                 will(returnValue(transaction));
                 oneOf(em).find(Team.class, team.getName());
                 will(returnValue(team));
                 oneOf(em).find(Competition.class, competition.getName());
                 will(returnValue(competition));
                 oneOf(transaction).begin();
                 oneOf(transaction).commit();
 
             }
         });
 
         competitionDAO.addTeam(competition, team);
         assertEquals(list, competition.getTeams());
 
 
     }
 
     @Test(expected = InvalidTeamException.class)
     public void add_incorrect_team_to_competition() throws Exception {
         
           context.checking(new Expectations() {
             {
                 atLeast(1).of (em).getTransaction();will(returnValue(transaction));
                 oneOf(transaction).begin();
 
             }
         });
 
         competitionDAO.addTeam(competition, null);
         
     }
     
     @Test(expected=InvalidCompetitionException.class)
     public void addTeamtoIncorrectCompetition() throws Exception{
         
           context.checking(new Expectations() {
             {
                 atLeast(1).of (em).getTransaction();will(returnValue(transaction));
                 oneOf(transaction).begin();
 
             }
         });
         competitionDAO.addTeam(null, team);
     }
 }
