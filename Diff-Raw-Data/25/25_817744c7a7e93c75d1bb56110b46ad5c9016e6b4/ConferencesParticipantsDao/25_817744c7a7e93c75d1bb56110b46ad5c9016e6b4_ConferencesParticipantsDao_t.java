 package daos;
 
 import helpers.ConferenceUsersArivalHelper;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 
 import db.HibernateUtil;
 import model.Conference;
 import model.ConferencesParticipants;
 import model.ConferencesUsers;
 import model.User;
 import model.UserRole;
 
 public class ConferencesParticipantsDao {
 
 	private static ConferencesParticipantsDao instance = null;
 	static Logger logger = Logger.getLogger(ConferencesParticipantsDao.class);
 	private ConferencesParticipantsDao() {}
 
 	public static ConferencesParticipantsDao getInstance() {
 		if (instance == null) {
 			instance = new ConferencesParticipantsDao();
 		}
 		return instance;
 	}
 	
	public enum IsArrivedEnum
	{
		Arrived, NotArrived, All
	}
	
 	@SuppressWarnings("unchecked")
	public List<ConferenceUsersArivalHelper> getAllParticipantsByConferenceAndIfArrivedToDay(Conference conference, String filterArrived){
 		
		IsArrivedEnum isArrivedEnum = IsArrivedEnum.All;
		if (filterArrived != null)
		{
			if (filterArrived.equals("Arrived"))
				isArrivedEnum = IsArrivedEnum.Arrived;
			if (filterArrived.equals("NotArrived"))
				isArrivedEnum = IsArrivedEnum.NotArrived;
		}
				
 		List<ConferencesUsers> conferenceUsers = ConferencesUsersDao.getInstance().getConferenceUsersByType(conference, UserRole.PARTICIPANT);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		List<ConferencesParticipants> participants = null;
 		
 		try {
 			Date now = new Date( );
 			
 			now = DateUtils.setHours(now, 0);
 			now = DateUtils.setMinutes(now, 0);
 			now = DateUtils.setSeconds(now, 0);
 			now = DateUtils.setMilliseconds(now, 0);
 			
 			session.beginTransaction();
 			participants = (List<ConferencesParticipants>)session.createQuery(
 					"select cp from  ConferencesParticipants cp where cp.conference = :conf and cp.date = :date")
 	                .setEntity("conf", conference)
 	                .setDate("date", now)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}		
 	
 		List<ConferenceUsersArivalHelper> cua = new LinkedList<ConferenceUsersArivalHelper>();
 		
 		if (conferenceUsers == null)
 			return null;
 		
 		for (ConferencesUsers cu : conferenceUsers)
 		{
 			boolean contains = false;
 			for (ConferencesParticipants participant : participants)
 			{
 				if (cu.getUser().getUserId() == participant.getUser().getUserId())
 				{
 					contains = true;
 					continue;
 				}
 			}
 			
			if (contains && (isArrivedEnum == IsArrivedEnum.Arrived || isArrivedEnum == IsArrivedEnum.All))
				cua.add(new ConferenceUsersArivalHelper(cu, contains));
			if (!contains && (isArrivedEnum == IsArrivedEnum.NotArrived || isArrivedEnum == IsArrivedEnum.All))
				cua.add(new ConferenceUsersArivalHelper(cu, contains));
 		}
 		
 		return cua;
 	}
 	
 	
 	/**
 	 * update users arrival to the conference
 	 * @param conference
 	 * @param user
 	 * @return
 	 */
 	public void updateUserArrival(Conference conference, User user)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try
 		{		
 			Date now = new Date( );
 			
 			now = DateUtils.setHours(now, 0);
 			now = DateUtils.setMinutes(now, 0);
 			now = DateUtils.setSeconds(now, 0);
 			now = DateUtils.setMilliseconds(now, 0);
 			
 			
 			session.beginTransaction();
 			ConferencesParticipants confUsr = new ConferencesParticipants(conference, user,  now);
 						
 			session.save(confUsr);
 			session.getTransaction().commit();
 		}
 		catch (RuntimeException e)
 		{
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 	}
 	
 	public Boolean isUserArrivedToday(Conference conference, User user){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		Boolean exists = false;
 		try
 		{
 			Date now = new Date( );
 			
 			now = DateUtils.setHours(now, 0);
 			now = DateUtils.setMinutes(now, 0);
 			now = DateUtils.setSeconds(now, 0);
 			now = DateUtils.setMilliseconds(now, 0);
 			
 			session.beginTransaction();
 			exists = session.createQuery(
 					"select cp from  ConferencesParticipants cp where cp.conferenceId=:conferenceId and cp.userId=:userId and cp.date=:date")
 	                .setLong("conferenceId", conference.getConferenceId())
 	                .setLong("userId",user.getUserId())
 	                .setDate("date", now)
 	                .uniqueResult() != null;
 			session.getTransaction().commit();
 		}
 		catch (RuntimeException e)
 		{
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		return exists;
 		
 	}
 	
 	/**
 	 * get the users the arrived to the given conference in the given date
 	 */
 	@SuppressWarnings("unchecked")
 	public List<User> getUsersThatArrivedToConferenceInDate(Conference conference, Date date){
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<User> users = null;
 		try
 		{
 			date = DateUtils.setHours(date, 0);
 			date = DateUtils.setMinutes(date, 0);
 			date = DateUtils.setSeconds(date, 0);
 			date = DateUtils.setMilliseconds(date, 0);
 			
 			session.beginTransaction();
 			users = session.createQuery(
 					"select cp.user from ConferencesParticipants cp where cp.conference = :conf and cp.date=:date")
 	                .setEntity("conf", conference)
 	                .setDate("date", date)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (RuntimeException e)
 		{
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		return users;
 	}
 	
 	/**
 	 * get the users the arrived to the given conference in the given date
 	 */
 	@SuppressWarnings("unchecked")
 	public int getNumberOfUsersThatArrivedToConferenceInDate(Conference conference, Date date){
 		
 		List<User> users =  getUsersThatArrivedToConferenceInDate(conference, date);
 		return users.size();
 	}
 	
 	public void removeUserArrivalFromConference(Conference conference, User user) throws Exception
 	{
 		//ConferencesUsers conferenceUser = getConferenceUser(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		try {
 			Date now = new Date( );
 			now = DateUtils.setHours(now, 0);
 			now = DateUtils.setMinutes(now, 0);
 			now = DateUtils.setSeconds(now, 0);
 			now = DateUtils.setMilliseconds(now, 0);
 			
 			session.beginTransaction();
 			ConferencesParticipants confUsr = new ConferencesParticipants(conference, user,  now);
 			session.delete(confUsr);
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 	}
 	
 	
 	public List<ConferencesParticipants> getConferencesParticipantsByConference(Conference conference){	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		List<ConferencesParticipants> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesParticipants>)session.createQuery(
 					"select cp from  ConferencesParticipants cp where cp.conference = :conf")
 	                .setEntity("conf", conference)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 	
 		return result;
 	}
 	
 	
 
 
 }
 
