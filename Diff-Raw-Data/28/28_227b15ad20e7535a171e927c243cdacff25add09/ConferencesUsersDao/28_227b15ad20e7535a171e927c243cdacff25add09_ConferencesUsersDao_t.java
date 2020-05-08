 package daos;
 
 import helpers.EmailContent;
 import helpers.EmailTemplate;
 import helpers.EmailUtils;
 import helpers.UniqueUuid;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import model.Conference;
 import model.ConferenceFilters;
 import model.ConferencesUsers;
 import model.User;
 import model.UserAttendanceStatus;
 import model.UserRole;
 import org.hibernate.Session;
 import org.apache.log4j.Logger;
 import db.HibernateUtil;
 import org.apache.commons.lang3.time.DateUtils;
 
 public class ConferencesUsersDao {
 
 	private static ConferencesUsersDao instance = null;
 	static Logger logger = Logger.getLogger(ConferencesUsersDao.class);
 
 
 	private ConferencesUsersDao() {
 	}
 
 	public static ConferencesUsersDao getInstance() {
 		if (instance == null) {
 			instance = new ConferencesUsersDao();
 		}
 		return instance;
 	}
 
 	/**
 	 * Assign users to a conference
 	 * @param conference
 	 * @return
 	 * @throws Exception 
 	 */
 	
 	public void assignUsersToConference(Conference conference, List<User> users, int role) throws Exception{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		for (User user : users)
 		{
 			try {
 				ConferencesUsers cu = new ConferencesUsers(conference, user, role);
 				session.save(cu);
 			}
 			catch (RuntimeException e) {
 				logger.error(e.getMessage(), e);
 				session.getTransaction().rollback();
 				throw new Exception("User " + user.getUserName() + " failed to assign");
 			}	
 		}
 		
 		session.getTransaction().commit();
 	}
 	
 	public void assignUsersToConference(Conference conference, User user, int role) throws Exception{
 		List<User> convertedToList = new LinkedList<User>();
 		convertedToList.add(user);
 		
 		assignUsersToConference(conference, convertedToList, role);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getAllConferenceUsersByConference(Conference conference){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<ConferencesUsers> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.conference = :conf")
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
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getAllConferenceUsersByConferenceThatLoggedInLastHoursCount(Conference conference, int lastHours){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		Date date = new Date();
 		date = DateUtils.addHours(date, -lastHours);
 		
 		List<ConferencesUsers> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu join fetch cu.user user where cu.conference = :conf and user.lastLogin >= :loginDate ")
 	                .setEntity("conf", conference)
 	                .setDate("loginDate", date)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}		
 	
 		return result;
 	}
 	
 	public boolean isUserLinkedToConfrences(User user){
 		return !getAllConferenceUsersByUser(user).isEmpty();
 	}
 	
 	public List<ConferencesUsers> getAllConferenceUsersByUser(User user){
 		return getAllConferenceUsersByUser(user, false);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getAllConferenceUsersByUser(User user, Boolean onlyActiveConferences){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<ConferencesUsers> result = null;
 		
 		Date date = new Date();
 		
 		String query;
 		if (onlyActiveConferences)
 			query = "select cu from ConferencesUsers cu left join fetch cu.conference conf where cu.user = :user and conf.endDate >= :now";
 		else
 			query = "select cu from  ConferencesUsers cu where cu.user = :user";
 		
 		try {
 			session.beginTransaction();
 			
 			if (onlyActiveConferences)
 			{
 				result = (List<ConferencesUsers>)session.createQuery(
 						query)
 		                .setEntity("user", user)
 		                .setDate("now", date)
 		                .list();
 			}
 			else
 			{
 				result = (List<ConferencesUsers>)session.createQuery(
 						query)
 		                .setEntity("user", user)
 		                .list();
 			}
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}		
 	
 		return result;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public List<Conference> getAllActiveConferencesOfUserByUserType(User user, UserRole userRole){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<Conference> result = null;
 		
 		Date date = new Date();
 		
 		String query = "select conf from ConferencesUsers cu left join cu.conference conf where cu.user = :user and cu.userRole = :userRole and conf.endDate >= :now";
 		
 		try {
 			session.beginTransaction();
 				result = (List<Conference>)session.createQuery(
 						query)
 		                .setEntity("user", user)
 		                .setInteger("userRole",userRole.getValue())
 		                .setDate("now", date)
 		                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}		
 	
 		return result;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getConferenceUsersByType(Conference conference, UserRole ur ){	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<ConferencesUsers> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.conference = :conf and cu.userRole = :userRole")
 	                .setEntity("conf", conference)
 	                .setInteger("userRole", ur.getValue())
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		
 		return result;
 	}
 	
 	public ConferencesUsers getConferenceUser(Conference conference, User user ){	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		ConferencesUsers result = null;
 		try {
 			session.beginTransaction();
 			result = (ConferencesUsers)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.conference = :conf and cu.user = :user")
 	                .setEntity("conf", conference)
 	                .setEntity("user", user)
 	                .uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 	
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<User> getUsersThatNotBelongsToConference(Conference conference){	
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<User> result = null;
 		
 		try {
 			session.beginTransaction();
 			result = (List<User>)session.createQuery(
 					"select u from User u where u.admin != 1 and not exists (select 1 from ConferencesUsers cu where cu.user = u and cu.conference = :conf)")
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
 	
 	
 	public void removeUserFromConference(Conference conference, User user) throws Exception{
 		ConferencesUsers conferenceUser = getConferenceUser(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		try {
 			session.beginTransaction();
 			session.delete(conferenceUser);
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 	}
 	
 	/**
 	 * update users attendance approval to the conference
 	 * @param conference
 	 * @param user
 	 * @return
 	 */
 	public void updateUserAttendanceApproval(Conference conference, User user, UserAttendanceStatus status){
 		ConferencesUsers confPart = getConferenceUser(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		try {
 			session.beginTransaction();
 			session.update(confPart.setAttendanceStatus(status));
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}	
 	}
 	/**
 	 * This function sends a predefined invitation to a conference email to users, 
 	 * as long as they have never gotten such mail before about this conference
 	 * @param user
 	 * @throws Exception 
 	 */
 	public void sendConferenceAssignmentNotificationEmailToUsers(Conference conference, User user) throws Exception{
 		ConferencesUsers cu = getConferenceUser(conference, user);
 		cu.setUniqueIdForEmailNotification(UniqueUuid.GenarateUniqueId());
 		
 		EmailTemplate email = new EmailTemplate(EmailContent.AssignToConferenceEmail.from, EmailContent.AssignToConferenceEmail.subject, EmailContent.AssignToConferenceEmail.body);
 		email.setSubject(String.format(email.getSubject(), cu.getUser().getName()));
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
 		String startDateFormatted = sdf.format(conference.getStartDate());
 		String endDateFormatted = sdf.format(conference.getEndDate());
 		email.setBody(String.format(email.getBody(), 
 				cu.getUser().getName(), 
 				cu.getConference().getName(), 
 				startDateFormatted, 
 				endDateFormatted, 
 				"http://localhost:8080/conf4u/confirmConference.jsp?id=" + cu.getUniqueIdForEmailNotification()));
 		
 		try
 		{
 			EmailUtils.sendEmail(email, user.getEmail(), true);
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			return;
 		}
 		cu.setNotifiedByMail(true);
 		updateConferenceUser(cu);
 	}
 	
 	private void updateConferenceUser(ConferencesUsers cu) throws Exception {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		try {
 			session.beginTransaction();
 			session.update(cu);
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	public UserRole getUserHighestRole(User user){
 		
 		List<ConferencesUsers> results = null;
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			results = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.user = :user")
 	                .setEntity("user", user)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		
 		UserRole ur = UserRole.NONE;
 		
 		if (results.size() == 0)
 			return ur;
 		
 		for (ConferencesUsers cu : results)
 		{
 			if (cu.getUserRole() >= ur.getValue())
 			{
 				ur =  UserRole.resolveUserRole(cu.getUserRole());
 			}
 		}
 		
 		return ur;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Conference> getScopedConferenceByDate(User user, String filter)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<Conference> confList = null;
 		
 		try {
 			session.beginTransaction();
 			
 			StringBuilder query = new StringBuilder();
 			String filterQuery = "";
 			
 			Date current = new Date();
 			
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			String startDateFormatted = sdf.format(current);
 	
 			ConferenceFilters.ConferenceTimeFilter enumFilter = ConferenceFilters.ConferenceTimeFilter.valueOf(filter); 
 			
 			if (enumFilter == ConferenceFilters.ConferenceTimeFilter.ALL)
 			{
 				filterQuery = "";
 			}
 			else if (enumFilter == ConferenceFilters.ConferenceTimeFilter.CURRENT)
 			{
 				filterQuery = "where c.active = 1  and '"+ startDateFormatted + "' between c.startDate and c.endDate";
 			}
 			else if (enumFilter == ConferenceFilters.ConferenceTimeFilter.FUTURE)
 			{
 				filterQuery = "where c.active = 1  and '" + startDateFormatted + "' < c.startDate";
 			}
 			else if (enumFilter == ConferenceFilters.ConferenceTimeFilter.PAST)
 			{
 				filterQuery = "where c.active = 1  and '"+ startDateFormatted + "' > c.endDate";
 			}
 			else
 			{
 				//TODO: EXCEPTION
 			}
 	
 			if (user.isAdmin())
 			{
 				query.append("select c from Conference c ");
 				query.append(filterQuery);
 				confList = (List<Conference>)session.createQuery(
 						query.toString())
 		                .list();
 			}
 			else
 			{
 				query.append("select c from Conference c ");
 				query.append(filterQuery);
 				if (enumFilter == ConferenceFilters.ConferenceTimeFilter.ALL)
 					query.append(" where c.active = 1 and exists (select 1 from ConferencesUsers cu where cu.user = :user and cu.conference = c and (cu.userRole = 3 or cu.userRole = 4)) order by c.startDate");
 				else
 					query.append(" and exists (select 1 from ConferencesUsers cu where cu.user = :user and cu.conference = c and (cu.userRole = 3 or cu.userRole = 4)) order by c.startDate");
 				
 				
 				confList = (List<Conference>)session.createQuery(
 						query.toString())
 		                .setEntity("user", user)
 		                .list();
 			}
 					
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		return confList;	
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<User> getUsersForRoleInConference(Conference conference, UserRole ur ){	
 		
 		List<User> result = null;
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			result = (List<User>)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 					"select cu.user from  ConferencesUsers cu where cu.conference = :conf and cu.userRole = :userRole")
 	                .setEntity("conf", conference)
 	                .setInteger("userRole", ur.getValue())
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}
 		
 		return result;
 	}
 	
	@SuppressWarnings("unchecked")
	public UserRole getUsersRoleInConference(Conference conference, User user){	
		
		Integer result = null;
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			result = (Integer)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
					"select cu.userRole from  ConferencesUsers cu where cu.conference = :conf and cu.user = :user")
	                .setEntity("conf", conference)
	                .setEntity("user", user)
	                .uniqueResult();
			session.getTransaction().commit();
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			session.getTransaction().rollback();
		}
		
		if (result == null)
			return null;
		
		UserRole ur = UserRole.resolveUserRole(result);
		
		return ur;
	}
	
 	public ConferencesUsers getConferenceUsersByUUID(String uuid){
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		ConferencesUsers result = null;
 		
 		try {
 			session.beginTransaction();
 			result = (ConferencesUsers)session.createQuery(
 					"from ConferencesUsers where uniqueIdForEmailNotification = :uuid")
 	                .setString("uuid", uuid)
 	                .uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			session.getTransaction().rollback();
 		}		
 	
 		return result;
 	}
 	
 	/**
 	 * get the number of users the were assigned to a given conference with a given role 
 	 */
 	@SuppressWarnings("unchecked")
 	public int getCountOfUsersInConferenceWithRole(Conference conf, UserRole ur ){	
 
 		List<User> users = getUsersForRoleInConference(conf, ur);
 		return users.size();
 		
 	}
 	
 	/**
 	 * get the users that in the given attendance status in the given conference
 	 */
 	@SuppressWarnings("unchecked")
 	public List<User> getUsersInAttendanceStatusInConference(Conference conference, UserAttendanceStatus status){
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<User> users = null;
 		try
 		{
 			session.beginTransaction();
 			users = session.createQuery(
 					"select cu.user from  ConferencesUsers cu where cu.conference = :conf and cu.attendanceStatus = :attendanceStatus")
 	                .setEntity("conf", conference)
 	                .setInteger("attendanceStatus", status.ordinal())
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
 		
 	public int getNumOfUsersInAttendanceStatusInConference(Conference conference, UserAttendanceStatus status){
 	
 		List<User> users = getUsersInAttendanceStatusInConference(conference, status);
 		return users.size();
 	}
 	
 	public void removeAllUsersThatAssignToConference(Conference conference) throws Exception{
 		
 		List<ConferencesUsers> conferenceUsers = getAllConferenceUsersByConference(conference);
 		
 		for (ConferencesUsers uc : conferenceUsers)
 		{
 			removeUserFromConference(uc.getConference(), uc.getUser());
 		}
 	}
 	
 }
