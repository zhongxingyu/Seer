 package org.fofo.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.fofo.entity.*;
 
 /**
  *
  * @author David Hernández
  * @author Anton Urrea
  */
 @RunWith(JMock.class)
 public class CalendarDAOImplTest {
 
     Mockery context = new JUnit4Mockery();
     private EntityManager em;
     private TeamDAO tdao;
     private CalendarDAOImpl calDAO;
     private EntityTransaction transaction;
     FCalendar cal;
     Competition comp, comp2;
     Club club;
     WeekMatch wm1, wm2, wm3, wm4;
     Match match1, match2, match3, match4;
     Team team1, team2, team3, team4;
     private String compf, compf2;
 
     @Before
     public void setUp() throws Exception {
 
         em = context.mock(EntityManager.class);
         transaction = context.mock(EntityTransaction.class);
         tdao = context.mock(TeamDAO.class);
 
 
         calDAO = new CalendarDAOImpl();
         calDAO.setEm(em);
         calDAO.setTd(tdao);
 
         club = new Club("ClubExemple");
         club.setEmail("exemple@hotmail.com");
 
 
         team1 = new Team("Team1", Category.FEMALE);
         team1.setClub(club);
         team1.setEmail("Team1@hotmail.com");
 
         team2 = new Team("Team2", Category.FEMALE);
         team2.setClub(club);
         team2.setEmail("Team2@hotmail.com");
 
         team3 = new Team("Team3", Category.FEMALE);
         team3.setClub(club);
         team3.setEmail("Team3@hotmail.com");
 
         team4 = new Team("Team4", Category.FEMALE);
         team4.setClub(club);
         team4.setEmail("Team4@hotmail.com");
 
 
         match1 = new Match();
         match1.setHome(team1);
         match1.setVisitor(team2);
 
         match2 = new Match();
         match2.setHome(team3);
         match2.setVisitor(team4);
 
         match3 = new Match();
         match3.setHome(team2);
         match3.setVisitor(team1);
 
         match4 = new Match();
         match4.setHome(team4);
         match4.setVisitor(team3);
 
 
         wm1 = new WeekMatch();
         wm1.addMatch(match1);
 
         wm2 = new WeekMatch();
         wm2.addMatch(match2);
         wm2.addMatch(match3);
 
         wm3 = new WeekMatch();
         wm3.addMatch(match3);
         wm3.addMatch(match4);
 
         wm4 = new WeekMatch();
 
 
 
         comp = Competition.create(CompetitionType.CUP);
         comp.setCategory(Category.FEMALE);
         comp.setInici(new Date());
         comp.setMaxTeams(4);
         comp.setMinTeams(2);
         comp.setName("Lleida");
 
         cal = new FCalendar();
         cal.setCompetition(comp);
 
         comp2 = Competition.create(CompetitionType.CUP);
 
     }
 
     /**
      * Add Calendar with no WM.
      *
      * @throws Exception
      */
     @Test
     public void CorrectAddCalenadrwithNotWM() throws Exception {
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
                 oneOf(em).persist(cal);
                 oneOf(em).getTransaction().commit();
                 oneOf(em).close();
             }
         });
 
         calDAO.addCalendar(cal);
     }
 
     /**
      * Add Calendar with wm, but without Matches.
      *
      * @throws Exception
      */
     @Test
     public void CorrectAddCalenadrwithNotMatch() throws Exception {
 
         cal.getAllWeekMatches().add(wm4);
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
                 oneOf(em).persist(cal.getWeekMatch(0));
                 oneOf(em).persist(cal);
                 oneOf(em).getTransaction().commit();
                 oneOf(em).close();
             }
         });
 
         calDAO.addCalendar(cal);
     }
 
     /**
      * Add Calendar with wm, but without Matches.
      *
      * @throws Exception
      */
     @Test(expected = IncorrectTeamException.class)
     public void NoTeamsInMatch() throws Exception {
 
         List<WeekMatch> lwm = new ArrayList<WeekMatch>();
 
         lwm.add(wm1);
 
         cal.setWeekMatches(lwm);
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
 
 
                 allowing(em).find(Team.class, team1.getName());
                 will(returnValue(null));
                 allowing(em).find(Team.class, team2.getName());
                 will(returnValue(null));
 
 
                 oneOf(em).close();
             }
         });
 
         calDAO.addCalendar(cal);
     }
 
     /**
      * The match don't have a local team.
      *
      * @throws Exception
      */
     @Test(expected = IncorrectTeamException.class)
     public void NotLocalTeamInMatch() throws Exception {
 
         List<WeekMatch> lwm = new ArrayList<WeekMatch>();
 
         lwm.add(wm1);
 
         cal.setWeekMatches(lwm);
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
 
                 oneOf(em).find(Team.class, cal.getWeekMatch(0).getListOfWeekMatches().get(0).getHome().getName());
                 will(returnValue(null));
 
                 oneOf(em).close();
             }
         });
 
         calDAO.addCalendar(cal);
     }
 
     /**
      * The match don't have a visitant team.
      *
      * @throws Exception
      */
     @Test(expected = IncorrectTeamException.class)
     public void NotVisitantTeamInMatch() throws Exception {
         List<WeekMatch> lwm = new ArrayList<WeekMatch>();
 
         lwm.add(wm1);
 
         cal.setWeekMatches(lwm);
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
 
                 oneOf(em).find(Team.class, cal.getWeekMatch(0).getListOfWeekMatches().get(0).getHome().getName());
                 oneOf(em).find(Team.class, cal.getWeekMatch(0).getListOfWeekMatches().get(0).getVisitor().getName());
                 will(returnValue(null));
 
                 oneOf(em).close();
             }
         });
 
         calDAO.addCalendar(cal);
 
     }
 
     /**
      * Add Calendar with only one WeekMatch with only one Match.
      *
      * @throws Exception
      */
     @Test
     public void testAdditionOfJustOneMatch() throws Exception {
 
         cal.getAllWeekMatches().add(wm1);
 
         context.checking(new Expectations() {
 
             {
 
                 oneOf(em).getTransaction().begin();
 
                 /*
                  * Match
                  */
 
                 oneOf(em).find(Team.class, cal.getWeekMatch(0).getListOfWeekMatches().get(0).getHome().getName());
                 oneOf(em).find(Team.class, cal.getWeekMatch(0).getListOfWeekMatches().get(0).getVisitor().getName());
 
                 oneOf(em).persist(cal.getWeekMatch(0).getListOfWeekMatches().get(0));
 
                 /*
                  * WeekMatch
                  */
                 oneOf(em).persist(cal.getWeekMatch(0));
 
                 /*
                  * Cal
                  */
                 oneOf(em).persist(cal);
                 oneOf(em).getTransaction().commit();
                 oneOf(em).close();
 
             }
         });
 
         calDAO.addCalendar(cal);
 
     }
 
     /**
      * Add Calendar with only one WeekMatch with various Match.
      *
      * @throws Exception
      */
     @Test
     public void testAdditionOfVariousMatchesOneWM() throws Exception {
 
         //REFACTOR..... SEE ISSUE #22 ****** REALIZED
 
 
         cal.getAllWeekMatches().add(wm2);
 
 
         context.checking(new Expectations() {
 
             {
 
                 oneOf(em).getTransaction().begin();
 
                 for (int i = 0; i < cal.getNumOfWeekMatches(); i++) {
 
                     testWeekMatches(i, cal);
                 }
 
                 /*
                  * Cal
                  */
                 oneOf(em).persist(cal);
                 oneOf(em).getTransaction().commit();
                 oneOf(em).close();
             }
         });
 
         calDAO.addCalendar(cal);
     }
 
     /**
      * Add Calendar with various WeekMatch with various Match.
      *
      * @throws Exception
      */
     @Test
     public void testAddVariousWeekMatches() throws Exception {
 
         //REFACTOR..... SEE ISSUE #22 ****** REALIZED
 
         cal.getAllWeekMatches().add(wm1);
         cal.getAllWeekMatches().add(wm2);
         cal.getAllWeekMatches().add(wm3);
 
         context.checking(new Expectations() {
 
             {
 
                 oneOf(em).getTransaction().begin();
 
                 for (int i = 0; i < cal.getNumOfWeekMatches(); i++) {
 
                     testWeekMatches(i, cal);
                 }
 
                 /*
                  * Cal
                  */
                 oneOf(em).persist(cal);
                 oneOf(em).getTransaction().commit();
                 oneOf(em).close();
 
             }
         });
 
         calDAO.addCalendar(cal);
 
     }
 
     private void testWeekMatches(final int i, final FCalendar cal) {
 
         context.checking(new Expectations() {
 
             {
                 for (int x = 0; x < cal.getWeekMatch(i).getNumberOfMatchs(); x++) {
 
                     testMatch(cal, i, x);
 
                 }
                 /*
                  * WeekMatch
                  */
                 allowing(em).persist(cal.getWeekMatch(i));
             }
         });
     }
 
     private void testMatch(final FCalendar cal, final int i, final int x) {
         /*
          * Match
          */
         context.checking(new Expectations() {
 
             {
                 allowing(em).find(Team.class, cal.getWeekMatch(i).getListOfWeekMatches().get(x).getHome().getName());
                 allowing(em).find(Team.class, cal.getWeekMatch(i).getListOfWeekMatches().get(x).getVisitor().getName());
 
                 allowing(em).persist(cal.getWeekMatch(i).getListOfWeekMatches().get(x));
             }
         });
     }
 
    /*
     * Search FCalendar by CompetitionName, but CompetitionName don't have any FCalendar.
     */
     //@Test(expected = PersistException.class)
     public void IncorrectfindFCalendarByCompetitionName() throws Exception {
 
         comp.setName("Lleida");
         cal.setCompetition(comp);
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
                oneOf(em).find(Competition.class, "Barcelona");
                 will(returnValue(null));
                 oneOf(em).find(FCalendar.class, comp.getFCalendar().getIdFCalendar());
                 will(returnValue(comp.getFCalendar().getIdFCalendar()));
                 oneOf(em).getTransaction().commit();
                 oneOf(em).isOpen();
                 will(returnValue(true));
                 oneOf(em).close();
             }
         });
 
         calDAO.findFCalendarByCompetitionName("Barcelona");
 
     }
 
    /*
     * Search FCalendar by CompetitionName.
     */
     //@Test
     public void CorrectfindFCalendarByCompetitionName() throws Exception {
 
         comp.setName("Lleida");
         cal.setCompetition(comp);
 
         context.checking(new Expectations() {
 
             {
                 oneOf(em).getTransaction().begin();
                 oneOf(em).find(Competition.class, "Lleida");
                 will(returnValue(comp));
                 oneOf(em).find(FCalendar.class, comp.getFCalendar().getIdFCalendar());
                 will(returnValue(comp.getFCalendar().getIdFCalendar()));
                 oneOf(em).getTransaction().commit();
                 oneOf(em).isOpen();
                 will(returnValue(true));
                 oneOf(em).close();
             }
         });
 
         calDAO.findFCalendarByCompetitionName("Lleida");
     }
 //
 //
 // @Test
 // public void CorrectAddMatch() throws Exception {
 // context.checking(new Expectations() {
 //
 // {
 // oneOf(tdao).getTeams();
 // will(returnValue(new ArrayList<Team>()));
 // oneOf(tdao).findTeam(match1.getLocal());
 // will(returnValue(true));
 // oneOf(tdao).findTeam(match1.getVisitant());
 // will(returnValue(true));
 // oneOf(em).persist(match1);
 // }
 // });
 //
 //
 // calDAO.addMatch(match1);
 // }
 //
 // @Test
 // public void CorrectAddWeekMatches() throws Exception {
 //
 // context.checking(new Expectations() {
 //
 // {
 // for (int i = 0; i < wm1.getNumberOfMatchs(); i++) {
 // oneOf(tdao).getTeams();
 // will(returnValue(new ArrayList<Team>()));
 // oneOf(tdao).findTeam(wm1.getListOfWeekMatches().get(i).getLocal());
 // will(returnValue(true));
 // oneOf(tdao).findTeam(wm1.getListOfWeekMatches().get(i).getVisitant());
 // will(returnValue(true));
 // oneOf(em).persist(wm1.getListOfWeekMatches().get(i));
 // }
 // oneOf(em).persist(wm1);
 // }
 // });
 //
 //
 // calDAO.addWeekMatches(wm1);
 // }
 }
