 package ca.ubc.ctlt.blackboardb2util;
 
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import blackboard.base.InitializationException;
 import blackboard.data.ValidationException;
 import blackboard.data.course.Course;
 import blackboard.data.course.CourseMembership;
 import blackboard.data.course.Group;
 import blackboard.data.course.GroupMembership;
 import blackboard.data.gradebook.Lineitem;
 import blackboard.data.gradebook.Score;
 import blackboard.data.gradebook.impl.OutcomeDefinitionCategory;
 import blackboard.data.user.User;
 import blackboard.db.ConnectionNotAvailableException;
 import blackboard.persist.BbPersistenceManager;
 import blackboard.persist.Id;
 import blackboard.persist.KeyNotFoundException;
 import blackboard.persist.PersistenceException;
 import blackboard.persist.course.CourseMembershipDbLoader;
 import blackboard.persist.course.GroupDbLoader;
 import blackboard.persist.course.GroupDbPersister;
 import blackboard.persist.course.impl.GroupMembershipDAO;
 import blackboard.persist.gradebook.LineitemDbLoader;
 import blackboard.persist.gradebook.LineitemDbPersister;
 import blackboard.persist.gradebook.ScoreDbPersister;
 import blackboard.persist.gradebook.ext.OutcomeDefinitionCategoryDbLoader;
 import blackboard.persist.user.UserDbLoader;
 import blackboard.platform.BbServiceException;
 import blackboard.platform.BbServiceManager;
 import blackboard.platform.context.Context;
 import blackboard.platform.context.ContextManager;
 import blackboard.platform.context.ContextManagerFactory;
 import blackboard.platform.log.LogService;
 import blackboard.platform.log.LogServiceFactory;
 import blackboard.platform.persistence.PersistenceServiceFactory;
 import blackboard.platform.security.CourseRole;
 import blackboard.platform.security.persist.CourseRoleDbLoader;
 
 public class B2Util 
 {
 	private Context ctx;
 	private static final LogService LOG = LogServiceFactory.getInstance();
 	
 	private HashMap<String, User> usersByUsername = new HashMap<String, User>();
 	private HashMap<String, User> usersByStudentId = new HashMap<String, User>();
 
 	public static Context extractContext(HttpServletRequest request) {
 		ContextManager ctxMgr = null;
 		Context context = null;
 		try {
 			// get services
 			LOG.logDebug("Initializing context manager...");
 			ctxMgr = (ContextManager) BbServiceManager
 					.lookupService(ContextManager.class);
 			context = ctxMgr.setContext(request);
 			LOG.logDebug("Current context: " + context);
 		} catch (BbServiceException e) {
 			LOG.logFatal("Lookup service failed! " + e.getMessage(), e);
 		} catch (InitializationException e) {
 			LOG.logFatal(
 					"Failed to initialize the context manager! "
 							+ e.getFullMessageTrace(), e);
 		} finally {
 			if (ctxMgr != null) {
 				ctxMgr.releaseContext();
 			}
 		}
 
 		return context;
 	}
 
 	public B2Util(Context ctx) throws PersistenceException {
 		this(ctx, null);
 	}
 
 	public B2Util(HttpServletRequest request) throws PersistenceException {
 		this(B2Util.extractContext(request));
 	}
 
 	public B2Util(HttpServletRequest request, Connection db) throws PersistenceException {
 		this(B2Util.extractContext(request), db);
 	}
 
 	public B2Util(Context ctx, Connection db) throws PersistenceException {
 		this.ctx = ctx;
 		
 		// prefetch user data for quick lookup later
 		UserDbLoader userLoader = UserDbLoader.Default.getInstance();
 		List<User> tmpUsers = userLoader.loadByCourseId(ctx.getCourseId());
 		for (User user : tmpUsers) 
 		{
 			// Map username to User object
 			usersByUsername.put(user.getUserName(), user);
 			usersByStudentId.put(user.getStudentId(), user);
 		}
 	}
 
 	public Context getContext() {
 		return ctx;
 	}
 	
 //	public Map<String, GroupSet> getGroupSets() throws PersistenceException
 //	{
 //		HashMap<String, GroupSet> sets;
 //		
 //		sets = getSets(ctx.getCourseId());
 //		List<Group> bbGroups = getAllBbGroups(ctx.getCourseId());
 //		GroupSet defaultSet = new GroupSet(GroupSet.EMPTY_NAME);
 //		
 //		for(Group s: bbGroups) {
 //			LOG.logDebug("Processing bbGroup: " + s);
 //			ca.ubc.ctlt.group.GroGroup g = new ca.ubc.ctlt.group.GroGroup(s);
 //			if (!s.isInGroupSet()) {
 //				defaultSet.addGroup(g);
 //				LOG.logDebug("Added to default set");
 //			} else {
 //				boolean added = false;
 //				for (Entry<String, GroupSet> entry : sets.entrySet()) {
 //					GroupSet set = entry.getValue();
 //					if (set.getId().equals(s.getSetId().toExternalString())) {
 //						set.addGroup(g);
 //						added = true;
 //						LOG.logDebug("Added to "+ set.getName() +" set");
 //					}
 //				}
 //				
 //				if (!added) {
 //					throw new MissingResourceException("Group " + g.getName() + " could not find group set!", "GroupSet", g.getName());
 //				}
 //			}
 //		}
 //		
 //		if (!defaultSet.getGroups().isEmpty()) {
 //			sets.put(GroupSet.EMPTY_NAME, defaultSet);
 //		}
 //		LOG.logDebug(sets.toString());
 //		return sets;
 //	}
 	
 	/**
 	 * Get the class size for the course, only count active students
 	 * @return
 	 * @throws PersistenceException
 	 */
 	public static int getClassSize(String courseIdStr, List<CourseMembership.Role> roles) throws PersistenceException {
 		List<CourseMembership> list = new ArrayList<CourseMembership>();
 		for(CourseMembership.Role role : roles) {
 			list.addAll(getBbUsersInCourse(courseIdStr, role, false));
 		}
 		int size = 0;
 		for (CourseMembership membership : list) {
 			if(membership.getIsAvailable()) {
 				size++;
 			}
 		}
 		
 		return size;
 	}
 	
 	/**
 	 * Filter out Group objects that actually represent groupsets and return the list of groups
 	 * that are actually groups.
 	 * @return
 	 * @throws PersistenceException
 	 */
 	public List<Group> getGroups() throws PersistenceException {
 		List<Group> ret = new ArrayList<Group>();
 		
 		List<Group> bbGroups = getAllBbGroups(ctx.getCourseId());
 		
 		for(Group group : bbGroups) {
 			if (!group.isGroupSet()) {
 				ret.add(group);
 			}
 		}
 		
 		return ret;
 	}
 	
 //	protected HashMap<String, GroupSet> getSets (Id courseId) {
 //		HashMap<String, GroupSet> sets = new HashMap<String, GroupSet>();
 //		
 //		//log("Loading group sets from course " + courseId);
 //		List<Group> bbGroupSets = blackboard.persist.course.impl.GroupDAO.get()
 //				.loadGroupSetsOnly(courseId);
 //		
 //		if (bbGroupSets.isEmpty()) {
 //			return sets;
 //		}
 //		
 //		for (Group s : bbGroupSets) {
 //			GroupSet set = new GroupSet(s.getTitle());
 //			set.setId(s.getId().toExternalString());
 //			sets.put(s.getTitle(), set);
 //		}
 //	
 //		return sets;
 //	}
 	
 	public static List<Group> getAllBbGroups(Id courseId) throws PersistenceException {
 		List<Group> courseGroups = null;
 		GroupDbLoader groupLoader =  GroupDbLoader.Default.getInstance();
 		courseGroups = groupLoader.loadByCourseId(courseId);
 		return courseGroups;
 	}
 	
 	public static <T> List<T> getGroupsInCourse(String courseIdStr, GroupAdapter<T> adapter) throws PersistenceException {
 		List<T> ret = new ArrayList<T>(); 
 		Id courseId = Id.generateId(Course.DATA_TYPE, courseIdStr);
 		List<Group> groups = getAllBbGroups(courseId);
 		for(Group group : groups) {
 			ret.add(adapter.bbGroupToGroup(group));
 		}
 		
 		return ret;
 	}
 	
 	public static List<User> getUsersInGroup(String groupId) throws PersistenceException, ConnectionNotAvailableException {
 		if (groupId == null || groupId.isEmpty()) {
 			return new ArrayList<User>();
 		}
 		
 		// get the Group from ID
 		GroupDbLoader gLoader = GroupDbLoader.Default.getInstance();
 		Group group = gLoader.loadById(Id.generateId(Group.DATA_TYPE, groupId));
 		
 		return getUsersInGroup(group);
 	}
 	
 	public static List<CourseMembership> getCourseMembershipsInGroup(Group group) throws PersistenceException, ConnectionNotAvailableException {
 		// To get a list of users in a group, we have to traverse a hierarchy of data relations.
 		// The hierarchy goes Group -> GroupMembership -> CourseMembership -> User
 		
 		// First we get the list of GroupMemberships in the Group
 		List<GroupMembership> groupMembers = group.getGroupMemberships();
 		
 		// From each GroupMembership, we can get the CourseMembership
 		CourseMembershipDbLoader cmLoader = CourseMembershipDbLoader.Default.getInstance();
 		List<CourseMembership> courseMembers = new ArrayList<CourseMembership>();
 		for (GroupMembership gmember : groupMembers) {
 			CourseMembership cmember = cmLoader.loadById(gmember.getCourseMembershipId(), null, true);
 			courseMembers.add(cmember);
 		}
 
 		return courseMembers;
 	}
 	
 	public static List<User> getUsersInGroup(Group group) throws PersistenceException, ConnectionNotAvailableException {
 		List<User> ret = new ArrayList<User>();
 		
 		List<CourseMembership> memberships = getCourseMembershipsInGroup(group);
 		// From the CourseMembership, we can get the Users
 		for (CourseMembership member : memberships) {
 			ret.add(member.getUser());
 		}
 		
 		return ret;
 	}
 	
 	public static <T> List<T> getUsersInGroup(Group group, UserAdapter<T> adapter) throws PersistenceException, ConnectionNotAvailableException {
 		List<CourseMembership> memberships = getCourseMembershipsInGroup(group);
 		List<T> ret = new ArrayList<T>();
 		for(CourseMembership membership : memberships) {
 			ret.add(adapter.bbUserToUser(membership));
 		}
 		
 		return ret;
 	}
 	
 	public User findUserByUsername(String username) 
 	{
 		User user = usersByUsername.get(username);
 		if (user == null)
 		{
 			LOG.logError("User with username "+username+" cannot be found!");
 		}
 		return user;
 	}
 	
 	public <T> T findUserByUsername(String username, UserAdapter<T> adapter){
 		User user = findUserByUsername(username);
 		return adapter.bbUserToUser(user);
 	}
 	
 	/**
 	 * Given a student id, load the user object by searching the class list for
 	 * a user with the matching student id.
 	 * 
 	 * @param studentId
 	 */
 	public User findUserByStudentId(String studentId) 
 	{
 		User user = usersByStudentId.get(studentId);
 		if (user == null)
 		{
 			LOG.logError("User with student ID "+ studentId +" cannot be found!");
 		}
 		return user;
 	}
 	
 	public <T> T findUserByStudentId(String studentId, UserAdapter<T> adapter){
 		User user = findUserByStudentId(studentId);
 		return adapter.bbUserToUser(user);
 	}
 	
 	public static List<CourseMembership> getBbUsersInCourse(String courseIdStr) throws PersistenceException {
 		return getBbUsersInCourse(courseIdStr, null);
 	}
 	
 	public static List<CourseMembership> getBbUsersInCourse(String courseIdStr, CourseMembership.Role role) throws PersistenceException {
 		return getBbUsersInCourse(courseIdStr, role, true);
 	}
 	
 	public static List<CourseMembership> getBbUsersInCourse(String courseIdStr, CourseMembership.Role role, boolean isHeavy) throws PersistenceException {
 		CourseMembershipDbLoader courseMembershipLoader = CourseMembershipDbLoader.Default.getInstance();
 		Id courseId = Id.generateId(Course.DATA_TYPE, courseIdStr);
 		// Load course membership with user
 		if (null != role) {
 			return courseMembershipLoader.loadByCourseIdAndRole(courseId, role, null, isHeavy);
 		} else {
 			return courseMembershipLoader.loadByCourseId(courseId, null, isHeavy);
 		}
 	}
 	
 	public static <T> List<T> getActiveUsersInCourse(String courseIdStr, UserAdapter<T> adapter) throws PersistenceException {
 		return getUsersInCourse(courseIdStr, adapter, true);
 	}
 	
 	public static <T> List<T> getAllUsersInCourse(String courseIdStr, UserAdapter<T> adapter) throws PersistenceException {
 		return getUsersInCourse(courseIdStr, adapter, false);
 	}
 	
 	public static <T> List<T> getUsersInCourse(String courseIdStr, UserAdapter<T> adapter, boolean onlyActive) throws PersistenceException {
 		List<CourseMembership> memberships = getBbUsersInCourse(courseIdStr);
 		List<T> ret = new ArrayList<T>();
 		for(CourseMembership membership : memberships) {
 			if(!onlyActive || membership.getIsAvailable()) {
 				ret.add(adapter.bbUserToUser(membership));
 			}
 		}
 		
 		return ret;
 	}
 	
 	public static <T> List<T> getActiveStudentsInCourse(String courseIdStr, UserAdapter<T> adapter) throws PersistenceException {
 		List<CourseMembership> memberships = getBbUsersInCourse(courseIdStr, CourseMembership.Role.STUDENT);
 		List<T> ret = new ArrayList<T>();
 		for(CourseMembership membership : memberships) {
 			if(membership.getIsAvailable()) {
 				ret.add(adapter.bbUserToUser(membership));
 			}
 		}
 		
 		return ret;
 	}
 	
 	public static Group createGroup(Id courseId, String groupTitle) throws PersistenceException, ValidationException {
 		return createGroup(courseId, groupTitle, null);
 	}
 	
 	public static Group createGroup(Id courseId, String groupTitle, Id setId) throws PersistenceException, ValidationException {
 		// create the group
 		Group bbGroup = new Group();
 		bbGroup.setTitle(groupTitle);
 		bbGroup.setCourseId(courseId);
 		bbGroup.setSetId(setId);
 		bbGroup.setIsAvailable(true);
 		
 		return createGroup(bbGroup);
 	}
 	
 	public static <T> Group createGroup(String courseIdStr, T group, GroupAdapter<T> adapter) throws PersistenceException, ValidationException {
 		Id courseId = Id.generateId(Course.DATA_TYPE, courseIdStr);
 		Group bbGroup = adapter.groupToBbGroup(group);
 		bbGroup.setCourseId(courseId);
 		bbGroup.setIsAvailable(true);
 		
 		return createGroup(bbGroup);
 	}
 	
 	/**
 	 * Create a group in BB with BB group. Assuming the group already have course ID
 	 * 
 	 * @param group
 	 * @return the group created
 	 * @throws PersistenceException
 	 * @throws ValidationException
 	 */
 	public static Group createGroup(Group group) throws PersistenceException, ValidationException {
 		LOG.logDebug("Creating group " + group.getTitle());
 		BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
 		GroupDbPersister groupDbPersister = (GroupDbPersister) bbPm.getPersister(GroupDbPersister.TYPE);
 		
 		groupDbPersister.persist(group);
 		
 		return group;
 	}
 	
 	
 	public static boolean enrolUsersInGroup(Group group, Set<Id> courseMembershipIds) {
 		GroupMembershipDAO.get().setGroupMembers(group.getId(), courseMembershipIds);
 
 		return true;
 	}
 	
 	public static <T> boolean setUsersInGroup(Group group, List<T> users, UserAdapter<T> adapter) throws PersistenceException {
 		Set<Id> idsToBeAdded = new HashSet<Id>();
 		List<CourseMembership> memberships = CourseMembershipDbLoader.Default.getInstance().loadByCourseId(group.getCourseId(), null, true);
 		
 		for (T user : users) {
 			User bbUser = adapter.userToBbUser(user);
 			for (CourseMembership membership : memberships) {
 				if(membership.getUser().getBatchUid().equals(bbUser.getBatchUid())) {
 					idsToBeAdded.add(membership.getId());
 					System.out.println("bb BUID to be add: "+membership.getUser().getBatchUid());
 				}
 			}
 		}
 		
 		return enrolUsersInGroup(group, idsToBeAdded);
 	}
 	
 	/************* Grades ***************/
 	
 	public static boolean setGradebook(Id courseId, String name, List<Score> scores) {
 		List<Lineitem> lineitems = null;
 		try {
 			lineitems = LineitemDbLoader.Default.getInstance().loadByCourseIdAndLineitemName(courseId, name);
 		} catch (KeyNotFoundException e) {
 			throw new RuntimeException("Could not find couse with Id " + courseId.toExternalString() +"!", e);
 		} catch (PersistenceException e) {
 			throw new RuntimeException("Load course with ID " + courseId.toExternalString() +" failed!", e);
 		}
 		
 		Lineitem item = null;
 		// check if the column exists
 		if (lineitems.size() == 1) {
 			// column exists
 			item = lineitems.get(0);
 		} else if (lineitems.size() == 0){
 			// create a new one
 			OutcomeDefinitionCategory category = getOutcomeDefinitionCategory(courseId, "SelfAndPeer.name");
 			item = new Lineitem();
 			item.setCourseId(courseId);
 			item.setPointsPossible(100.0f);
 			item.setName(name);
 			item.getOutcomeDefinition().setCategory(category);
 			try {
 				LineitemDbPersister.Default.getInstance().persist(item);
 			} catch (PersistenceException e) {
 				throw new RuntimeException("Failed to save gradebook column " + name +"!", e);
 			} catch (ValidationException e) {
 				throw new RuntimeException("Invalid gradebook column data to save!", e);
 			}
 		} else {
 			throw new RuntimeException("Found multiple gradebook columns matching name " + name + "!");
 		}
 		
 		// associate all scores to the lineitem
 		for (Score score : scores) {
 			score.setLineitemId(item.getId());
 		}
 		
 		// save the scores
 		try {
 			ScoreDbPersister.Default.getInstance().persist(scores);
 		} catch (PersistenceException e) {
 			throw new RuntimeException("Failed to save score!", e);
 		} catch (ValidationException e) {
 			throw new RuntimeException("Invalid score to save!", e);
 		}
 		
 		return true;
 	}
 
     /**
      * Set the gradebook using an GradeAdapter
      * @param courseIdStr BB course ID string
      * @param name gradebook column name
      * @param grades list of grades
      * @param adapter grade adapter
      * @param <T>
      * @return the grades that are set unsuccessful
      */
 	public static <T> List<T> setGradebook(String courseIdStr, String name, List<T> grades, GradeAdapter<T> adapter) {
 		List<Score> scores = new ArrayList<Score>();
 		Id courseId = null;
         List<T> failedGrades = new ArrayList<T>();
 		List<CourseMembership> memberships = null;
 		try {
 			courseId = Id.generateId(Course.DATA_TYPE, courseIdStr);
 			memberships = CourseMembershipDbLoader.Default.getInstance().loadByCourseId(courseId, null, true);
 		} catch (PersistenceException e) {
 			throw new RuntimeException("Failed to load course membership!", e);
 		}
 		
 		for (T grade : grades) {
 			Score score = adapter.gradeToBbScore(grade, memberships);
            if (score == null) {
                 failedGrades.add(grade);
             } else {
                 scores.add(score);
             }
 		}
 
         setGradebook(courseId, name, scores);
 
 		return failedGrades;
 	}
 	
 	public static OutcomeDefinitionCategory getOutcomeDefinitionCategory(Id courseId, String title) {
 		OutcomeDefinitionCategory category = null;
 		try {	
 			category = OutcomeDefinitionCategoryDbLoader.Default.getInstance().loadByCourseIdAndTitle(courseId, title);
 		} catch (KeyNotFoundException e) {
 			category = null;
 		} catch (PersistenceException e) {
 			throw new RuntimeException("Failed to load outcome category " + title + "!", e);
 		}
 		// if there is no such category, create a new one
 		if (category == null) {
 			category = new OutcomeDefinitionCategory(title);
 			category.setDescription("");
 			category.setCourseId(courseId);
 			category.setUserDefined(true);
 			category.setCalculated(false);
 			category.setDateCreated(Calendar.getInstance());
 			category.setScorable(false);
 			category.setWeight(0.0f);
 			try {
 				category.persist();
 			} catch (PersistenceException e) {
 				throw new RuntimeException("Failed to save outcome category " + title + "!", e);
 			} catch (ValidationException e) {
 				throw new RuntimeException("Invalid outcome category " + title + " to save!", e);
 			}
 		}
 		
 		return category;
 	}
 	
 	public static User getCurrentUser(HttpServletRequest request) {
 		Context context = ContextManagerFactory.getInstance().setContext(request);
 		return context.getUser();
 	}
 	
 	public static String getCurrentUsername(HttpServletRequest request) {
 		return getCurrentUser(request).getBatchUid();
 	}
 	
 	/**
 	 * Return all roles in the system
 	 * 
 	 * @return List of roles
 	 */
 	public static List<CourseRole> getCourseRoles() {  
 		List<CourseRole> roles = null;
 		try {
 			BbPersistenceManager pm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
 			CourseRoleDbLoader crLoader = (CourseRoleDbLoader) pm.getLoader("CourseRoleDbLoader");
 			roles = crLoader.loadAll();
 		} catch (PersistenceException e) {
 			throw new RuntimeException("Failed to load roles!", e);
 		}
 
 		return roles;
 	}
 	
 }
