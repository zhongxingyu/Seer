 package daos;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.hibernate.Session;
 import org.hibernate.Query;
 
 import sun.util.calendar.CalendarDate;
 
 import db.HibernateUtil;
 
 import model.Conference;
 import model.ConferenceFilters;
 import model.ConferenceParticipantStatus;
 import model.ConferencesParticipants;
 import model.ConferencesUsers;
 import model.Location;
 import model.User;
 import model.UserAttendanceStatus;
 import model.UserRole;
 
 /**
  * This class is responsible of supplying services related to the Conference entity which require database access.
  * Singleton class.
  */
 public class ConferenceDao {
 
 	private static ConferenceDao instance = null;
 
 	private ConferenceDao() {}
 
 	public static ConferenceDao getInstance() {
 		if (instance == null) {
 			instance = new ConferenceDao();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Get a list of all the Conferences that are stored in the database
 	 */
 	public List<Conference> getConferences(ConferenceFilters.ConferencePreDefinedFilter filter){
 		
 		Calendar filterDate = Calendar.getInstance();
 		Date dateToFilter = new Date();		
 		filterDate.setTime(dateToFilter);
 		
 		switch(filter)
 		{
 			case ALL:
 				filterDate.add(Calendar.YEAR, -40);
 				break;
 			case LAST7DAYS:
 				filterDate.add(Calendar.DATE, -7);
 				break;
 			case LAST30DAYS:
 				filterDate.add(Calendar.DATE, -30);
 				break;
 			case LAST90DAYS:
 				filterDate.add(Calendar.DATE, -90);
 				break;
 				
 		}
 		
 		dateToFilter.setTime(filterDate.getTimeInMillis());
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Conference> result = (List<Conference>) session.createQuery("select conf from Conference conf where conf.startDate >= :filterDate")
 				.setDate("filterDate", dateToFilter)
 				.list();
 		session.getTransaction().commit();
 		return result;
 	}
 	
 
 	/**
 	 * Get a list of all the Companies that match the given filter.
 	 * @param filter
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Conference> getConferences(ConferenceFilters.ConferenceDatesFilter filter){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<Conference> result = (List<Conference>)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 				"select conf from Conference conf where conf.startDate >= :startDate and conf.endtDate <= :endDate")
                 .setDate("startDate", filter.getFromDate())
                 .setDate("endDate", filter.getToDate())
                 .list();
		session.getTransaction().commit();
		return result;
 	}
 	
 	
 	/**
 	 * Get a conference by its database key ID
 	 * @param id
 	 * @return
 	 */
 	public Conference getConferenceById(long id){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Conference conf = (Conference)session.createQuery(
 				"select conf from  Conference conf where conf.conferenceID = :confId")
                 .setLong("confId", id)
                 .uniqueResult();
 		session.getTransaction().commit();
 		return conf;
 	}
 	
 	
 	public void addNewConference(List<Conference> conferences){
 	 for (Conference conf:conferences)
 	 {
 		 addNewConference(conf);
 	 }
 	}
 	
 	
 	/**
 	 * Add a new Conference to the database
 	 */
 	public Conference addNewConference(Conference conference){
 		Conference conf = new Conference(conference.getLocation(), conference.getName(), conference.getDescription(), conference.getStartDate(), conference.getEndDate());
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.save(conf);
 		session.getTransaction().commit();
 		
 		return conf;
 	}
 	
 
 
 	/**
 	 * Update an existing conference in the database
 	 */
 	public Conference updateConference(Conference conference){
 		Conference conf = getConferenceById(conference.getConferenceID());
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.update(conf.setName(conference.getName())
 				.setDescription(conference.getDescription())
 				.setLocation(conference.getLocation())
 				.setStartDate(conference.getStartDate())
 				.setEndDate(conference.getEndDate()));
 		session.getTransaction().commit();
 		
 		return conf;
 
 		
 	}
 	
 	/**
 	 * Assign users to a conference
 	 * @param conference
 	 * @return
 	 */
 	public void assignUserToConference(Conference conference, User user, UserRole userRole){
 		ConferencesUsers confUsr = new ConferencesUsers(conference, user, userRole);
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.save(confUsr);
 		session.getTransaction().commit();
 	}
 	
 	/**
 	 * Assign users to a conference
 	 * @param conference
 	 * @return
 	 */
 	public void assignParticipantsToConference(Conference conference, List<User> users){
 				Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		for (User user:users)
 		{
 			session.save(new ConferenceParticipantStatus(conference, user, null, false));
 		}
 		
 		session.getTransaction().commit();
 	}
 	
 	/**
 	 * assign location to a conference
 	 * @param conference
 	 * @return
 	 */
 	public Conference assignLocationToConference(Conference conference, Location location){
 		Conference conf = getConferenceById(conference.getConferenceID());
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.update(conf.setLocation(location));
 		session.getTransaction().commit();
 		
 		return conf;
 	}
 	
 	/**
 	 * Remove users from a conference
 	 * @param conference
 	 * @return
 	 */
 	public void removeUserFromConference(Conference conference, User user){
 		
 		ConferencesUsers cu = getConferernceUser(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.delete(cu);
 		session.getTransaction().commit();
 	}
 	
 	private  ConferencesUsers getConferernceUser(Conference conference, User user)
 	{
 		return (ConferencesUsers)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 				"select confUsr from  ConferencesUsers confUsr where confUsr.conference = :conf and confUsr.user = :user")
                 .setEntity("conf", conference)
                 .setEntity("user", user)
                 .uniqueResult();
 	}
 	
 	public void removeParticipantFromConference(Conference conference, User user){
 		
 		ConferenceParticipantStatus conferenceParticipantStatus = getConferenceParticipant(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.delete(conferenceParticipantStatus);
 		session.getTransaction().commit();
 	}
 	
 	private  ConferenceParticipantStatus getConferenceParticipant(Conference conference, User user)
 	{
 		return (ConferenceParticipantStatus)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 				"select confPart from  ConferenceParticipantStatus confPart where confPart.conference = :conf and confPart.user = :participant")
                 .setEntity("conf", conference)
                 .setEntity("user", user)
                 .uniqueResult();
 	}
 	
 	/**
 	 * This function sends a predefined invitation to a conference email to users, 
 	 * as long as they have never gotten such mail before about this conference
 	 * @param user
 	 */
 	public void sendConferenceAssignmentNotificationEmailToUsers(List<User> user){
 		
 	}
 
 	/**
 	 * update users arrival to the conference
 	 * @param conference
 	 * @param user
 	 * @return
 	 */
 	public void updateUserArrival(Conference conference, User user){
 		ConferencesParticipants confUsr = new ConferencesParticipants(conference, user,  new Date());
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.update(confUsr);
 		session.getTransaction().commit();
 	}	
 	
 	/**
 	 * update users attendance approval to the conference
 	 * @param conference
 	 * @param user
 	 * @return
 	 */
 	public void updateUserAttendanceApproval(Conference conference, User user, UserAttendanceStatus status){
 		ConferenceParticipantStatus confPart = getConferenceParticipant(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.update(confPart.setAttendanceStatus(status));
 		session.getTransaction().commit();
 		
 	}
 	
 	/**
 	 * Get all conferences associated with a given user
 	 * @param user
 	 * @return
 	 */
 	public List<Conference> getConferencesForUser(User user){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
 		List<Conference> conferernces = null;
 		
 		conferernces.addAll(
 				(List<Conference>)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 				"select confUser.conference from  ConferencesUsers confUser where confUser.user = :user")
                 .setEntity("user", user));
 		
 		
 		conferernces.addAll(
 				(List<Conference>)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 				"select confPart.conference from  ConferenceParticipantStatus confPart where confPart.user = :user")
                 .setEntity("user", user));
		session.getTransaction().commit();
 		return conferernces;
 	}
 }
