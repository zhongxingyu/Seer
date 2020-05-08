 package uk.co.mindbadger.footballresultsanalyser.dao;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 import uk.co.mindbadger.footballresultsanalyser.domain.Division;
 import uk.co.mindbadger.footballresultsanalyser.domain.DivisionImpl;
 import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
 import uk.co.mindbadger.footballresultsanalyser.domain.Fixture;
 import uk.co.mindbadger.footballresultsanalyser.domain.Season;
 import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;
 import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionId;
 import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionImpl;
 import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
 import uk.co.mindbadger.footballresultsanalyser.domain.SeasonImpl;
 import uk.co.mindbadger.footballresultsanalyser.domain.Team;
 import uk.co.mindbadger.footballresultsanalyser.hibernate.HibernateUtil;
 
 public class FootballResultsAnalyserHibernateDAO implements FootballResultsAnalyserDAO {
 
 	Logger logger = Logger.getLogger(FootballResultsAnalyserHibernateDAO.class);
 	
 	private Map<Thread, Session> sessions = new HashMap<Thread, Session>();
 	private DomainObjectFactory domainObjectFactory;
 
 	@Override
 	public void startSession() {
 		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
 		Session session = sessionFactory.openSession();
 
 		sessions.put(Thread.currentThread(), session);
 	}
 
 	@Override
 	public void closeSession() {
 		Session session = sessions.get(Thread.currentThread());
 		session.close();
 
 		sessions.remove(session);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Season> getSeasons() {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Season> seasons = null;
 		tx = session.beginTransaction();
 
 		// Get the Seasons
 		seasons = session.createQuery("from SeasonImpl").list();
 
 		tx.commit();
 
 		return seasons;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Season getSeason(Integer seasonNumber) {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Season> seasons = null;
 		tx = session.beginTransaction();
 
 		seasons = session.createQuery("select S from SeasonImpl S where S.seasonNumber = " + seasonNumber).list();
 
 		tx.commit();
 
 		if (seasons.size() == 1) {
 			return seasons.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map<Integer, Division> getAllDivisions() {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Division> divisions = null;
 		tx = session.beginTransaction();
 
 		// Get the Divisions
 		divisions = session.createQuery("from DivisionImpl").list();
 
 		tx.commit();
 
 		Map<Integer, Division> divisionsMap = new HashMap<Integer, Division>();
 		for (Division division : divisions) {
 			divisionsMap.put(division.getDivisionId(), division);
 		}
 
 		return divisionsMap;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map<Integer, Team> getAllTeams() {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Team> teams = null;
 		tx = session.beginTransaction();
 
 		// Get the Divisions
 		teams = session.createQuery("from TeamImpl").list();
 
 		tx.commit();
 
 		Map<Integer, Team> teamsMap = new HashMap<Integer, Team>();
 		for (Team team : teams) {
 			teamsMap.put(team.getTeamId(), team);
 		}
 
 		return teamsMap;
 	}
 
 	@Override
 	public Set<SeasonDivision> getDivisionsForSeason(int seasonNumber) {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		Season season = null;
 		tx = session.beginTransaction();
 
 		season = (Season) session.get(SeasonImpl.class, seasonNumber);
 
 		tx.commit();
 
 		return season.getDivisionsInSeason();
 	}
 
 	@Override
 	public Set<SeasonDivisionTeam> getTeamsForDivisionInSeason(int seasonNumber, int divisionId) {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		SeasonDivision seasonDivision = null;
 		tx = session.beginTransaction();
 
 		Season season = (Season) session.get(SeasonImpl.class, seasonNumber);
 		Division division = (Division) session.get(DivisionImpl.class, divisionId);
 
 		SeasonDivisionId seasonDivisionId = domainObjectFactory.createSeasonDivisionId(season, division);
 
 		seasonDivision = (SeasonDivision) session.get(SeasonDivisionImpl.class, seasonDivisionId);
 
 		tx.commit();
 
 		return seasonDivision.getTeamsInSeasonDivision();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Fixture> getFixturesForTeamInDivisionInSeason(int seasonNumber, int divisionId, int teamId) {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Fixture> fixtures = null;
 		tx = session.beginTransaction();
 
 		fixtures = session.createQuery("select F from FixtureImpl F join F.division D join F.season S" + " where S.seasonNumber = " + seasonNumber + " and D.divisionId = " + divisionId + " order by F.fixtureDate").list();
 
 		tx.commit();
 
 		return fixtures;
 	}
 
 	@Override
 	public Season addSeason(Integer seasonNum) {
 		Session session = sessions.get(Thread.currentThread());
 		Transaction tx = session.beginTransaction();
 
 		Season season = domainObjectFactory.createSeason(seasonNum);
 
 		session.save(season);
 		tx.commit();
 
 		return season;
 	}
 
 	@Override
 	public Division addDivision(String divisionName) {
 		Session session = sessions.get(Thread.currentThread());
 		Transaction tx = session.beginTransaction();
 
 		Division division = domainObjectFactory.createDivision(divisionName);
 
 		session.save(division);
 		tx.commit();
 
 		return division;
 	}
 
 	@Override
 	public Team addTeam(String teamName) {
 		Session session = sessions.get(Thread.currentThread());
 		Transaction tx = session.beginTransaction();
 
 		Team team = domainObjectFactory.createTeam(teamName);
 
 		session.save(team);
 		tx.commit();
 
 		return team;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Fixture addFixture(Season season, Calendar fixtureDate, Division division, Team homeTeam, Team awayTeam, Integer homeGoals, Integer awayGoals) {
 		Session session = sessions.get(Thread.currentThread());
 		Transaction tx = session.beginTransaction();
 		Fixture fixture = null;
 
 		// Have we got a fixture with a date?
 
 		List<Fixture> fixtures = null;
 		StringBuffer sb = new StringBuffer("select F from FixtureImpl F join F.homeTeam T1 join F.awayTeam T2 join F.season S ");
 		sb.append(" where S.seasonNumber = :seasonNumber ");
 		sb.append(" and T1.teamId = :homeTeamId ");
 		sb.append(" and T2.teamId = :awayTeamId ");
 		sb.append(" and F.fixtureDate = :fixtureDate ");
 
 		Query query = session.createQuery(sb.toString());
 		query.setInteger("seasonNumber", season.getSeasonNumber());
 		query.setInteger("homeTeamId", homeTeam.getTeamId());
 		query.setInteger("awayTeamId", awayTeam.getTeamId());
 		query.setCalendarDate("fixtureDate", fixtureDate);
 		
 		fixtures = query.list();
 		
 		if (fixtures.size() > 0) {
 			logger.debug("Got an existing fixture on the date specified (count: " + fixtures.size() + ")");
 			fixture = fixtures.get(0);
			Calendar existingFixtureDate = fixture.getFixtureDate();
			logger.debug("Fixture date: " + new SimpleDateFormat("yyyy-MM-dd").format(existingFixtureDate.getTime()));
 		} else {
 			sb = new StringBuffer("select F from FixtureImpl F join F.homeTeam T1 join F.awayTeam T2 join F.season S join F.division D ");
 			sb.append(" where S.seasonNumber = :seasonNumber ");
 			sb.append(" and T1.teamId = :homeTeamId ");
 			sb.append(" and T2.teamId = :awayTeamId ");
 			sb.append(" and D.divisionId = :divisionId ");
 	
 			query = session.createQuery(sb.toString());
 			query.setInteger("seasonNumber", season.getSeasonNumber());
 			query.setInteger("homeTeamId", homeTeam.getTeamId());
 			query.setInteger("awayTeamId", awayTeam.getTeamId());
 			query.setInteger("divisionId", division.getDivisionId());
 			
 			fixtures = query.list();
 			
 			if (fixtures.size() > 0) {
 				logger.debug("Got an existing fixture withoug a date");
 				fixture = fixtures.get(0);
				Calendar existingFixtureDate = fixture.getFixtureDate();
				logger.debug("fixtureDate = " + existingFixtureDate);
				if (existingFixtureDate != null) {
					logger.debug("Shouldn't have one, but fixture date: " + new SimpleDateFormat("yyyy-MM-dd").format(existingFixtureDate.getTime()));
				}
 			} else {
 				logger.debug("No fixture found for these teams in this division for the season, so adding one...");
 				fixture = domainObjectFactory.createFixture(season, homeTeam, awayTeam);
 			}
 
 			fixture.setFixtureDate(fixtureDate);
 		}
 		
 		fixture.setDivision(division);
 		fixture.setHomeGoals(homeGoals);
 		fixture.setAwayGoals(awayGoals);
 
 		session.save(fixture);
 		tx.commit();
 
 		return fixture;
 	}
 
 	public DomainObjectFactory getDomainObjectFactory() {
 		return domainObjectFactory;
 	}
 
 	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
 		this.domainObjectFactory = domainObjectFactory;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Fixture> getUnplayedFixturesBeforeToday() {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Fixture> fixtures = null;
 		tx = session.beginTransaction();
 
 		fixtures = session.createQuery("select F from FixtureImpl F where F.fixtureDate <= current_date and F.homeGoals is null").list();
 
 		tx.commit();
 
 		return fixtures;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Fixture> getFixturesWithNoFixtureDate() {
 		Transaction tx = null;
 
 		Session session = sessions.get(Thread.currentThread());
 
 		List<Fixture> fixtures = null;
 		tx = session.beginTransaction();
 
 		fixtures = session.createQuery("select F from FixtureImpl F where F.fixtureDate is null").list();
 
 		tx.commit();
 
 		return fixtures;
 	}
 }
