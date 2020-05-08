 package com.fem.google.cloud.endpoints;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import javax.annotation.Nullable;
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.persistence.EntityNotFoundException;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.api.server.spi.response.CollectionResponse;
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.datanucleus.query.JDOCursorHelper;
 
 @Api(name = "userendpoint")
 public class UserEndpoint {
 
 	/**
 	 * This method lists all the entities inserted in datastore.
 	 * It uses HTTP GET method and paging support.
 	 *
 	 * @return A CollectionResponse class containing the list of all entities
 	 * persisted and a cursor to the next page.
 	 */
 	@SuppressWarnings({ "unchecked", "unused" })
 	public CollectionResponse<User> listUser(
 			@Nullable @Named("cursor") String cursorString,
 			@Nullable @Named("limit") Integer limit) {
 
 		PersistenceManager mgr = null;
 		Cursor cursor = null;
 		List<User> execute = null;
 
 		try {
 			mgr = getPersistenceManager();
 			Query query = mgr.newQuery(User.class);
 			if (cursorString != null && cursorString != "") {
 				cursor = Cursor.fromWebSafeString(cursorString);
 				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
 				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
 				query.setExtensions(extensionMap);
 			}
 
 			if (limit != null) {
 				query.setRange(0, limit);
 			}
 
 			execute = (List<User>) query.execute();
 			cursor = JDOCursorHelper.getCursor(execute);
 			if (cursor != null)
 				cursorString = cursor.toWebSafeString();
 
 			// Tight loop for fetching all entities from datastore and accomodate
 			// for lazy fetch.
 			for (User obj : execute)
 				;
 		} finally {
 			mgr.close();
 		}
 
 		return CollectionResponse.<User> builder().setItems(execute)
 				.setNextPageToken(cursorString).build();
 	}
 
 	/**
 	 * This method gets the entity having primary key id. It uses HTTP GET method.
 	 *
 	 * @param id the primary key of the java bean.
 	 * @return The entity with primary key id.
 	 */
 	public User getUser(@Named("id") String id) {
 		
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 		try {
 			user = mgr.getObjectById(User.class, id);
 		} finally {
 			mgr.close();
 		}
 		return user;
 		
 	}
 
 	/**
 	 * This inserts a new entity into App Engine datastore. If the entity already
 	 * exists in the datastore, an exception is thrown.
 	 * It uses HTTP POST method.
 	 *
 	 * @param user the entity to be inserted.
 	 * @return The inserted entity.
 	 */
 	public User insertUser(User user) {
 		PersistenceManager mgr = getPersistenceManager();
 		try {
 			/*if (containsUser(user)) {
 				throw new EntityExistsException("Object already exists");
 			}*/
 			mgr.makePersistent(user);
 		} finally {
 			mgr.close();
 		}
 		return user;
 	}
 
 	/**
 	 * This method is used for updating an existing entity. If the entity does not
 	 * exist in the datastore, an exception is thrown.
 	 * It uses HTTP PUT method.
 	 *
 	 * @param user the entity to be updated.
 	 * @return The updated entity.
 	 */
 	public User updateUser(User user) {
 		PersistenceManager mgr = getPersistenceManager();
 		try {
 			if (!containsUser(user)) {
 				throw new EntityNotFoundException("Object does not exist");
 			}
 			mgr.makePersistent(user);
 		} finally {
 			mgr.close();
 		}
 		return user;
 	}
 
 	/**
 	 * This method removes the entity with primary key id.
 	 * It uses HTTP DELETE method.
 	 *
 	 * @param id the primary key of the entity to be deleted.
 	 * @return The deleted entity.
 	 */
 	public User removeUser(@Named("id") String id) {
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 		try {
 			user = mgr.getObjectById(User.class, id);
 			mgr.deletePersistent(user);
 		} finally {
 			mgr.close();
 		}
 		return user;
 	}
 
 	private boolean containsUser(User user) {
 		PersistenceManager mgr = getPersistenceManager();
 		boolean contains = true;
 		try {
 			mgr.getObjectById(User.class, user.getUserId());
 		} catch (javax.jdo.JDOObjectNotFoundException ex) {
 			contains = false;
 		} finally {
 			mgr.close();
 		}
 		return contains;
 	}
 
 	private static PersistenceManager getPersistenceManager() {
 		return PMF.get().getPersistenceManager();
 	}
 	
 	
 	/***************** Automatic code generation end  ***************/
 	
 	
 	/***************** Custom endpoint methods start ****************/
 	
 	
 	/**
 	 * This method is supposed to retrieve all the groups of a user with id.
 	 * @param id UserId of the user whose groups to be fetched
 	 * */
 	@SuppressWarnings("unchecked")
 	@ApiMethod(
  			httpMethod = "GET", 
  			name = "user.groups",
 			path="user/{id}/group"
 			)
 	public List<Group> getGroups(@Named("id") String id) {
 		
 		List<Group> alGroups = new ArrayList<Group>();
 		GroupMemberMapping groupMemberMapping = null;
 		int iCounter = 0;
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		
 		Query q = pm.newQuery(GroupMemberMapping.class);
 
 		q.setFilter("userId == userIdParam");
 		q.declareParameters("String userIdParam");
 		
 		List<GroupMemberMapping> execute = null;
 		
 		execute = (List<GroupMemberMapping>)q.execute(id);
 		
 		while(iCounter < execute.size()){
 			groupMemberMapping = execute.get(iCounter++);
 			
 			q = pm.newQuery(Group.class);
 			
 			q.setFilter("groupId == groupIdParam");
 			q.declareParameters("String groupIdParam");
 			//alGroups.addAll((List<Group>) q.execute(groupMemberMapping.getGroupId()));
 			//TODO : Doing too many queries here, Need to put in transaction and optimize using single query
 			alGroups.add(new GroupEndpoint().getGroup(groupMemberMapping.getGroupId()));
 			
 		}
 		
 		
 		
 		
 		
 		
 		return alGroups;
 	}
 	
 	/**
 	 * This method is supposed to retrieve group information of specific group.
 	 * @param userId UserId of the user whose groups to be fetched
 	 * @param groupId Group id of which information is to be fetched.
 	 * */
 	@SuppressWarnings("unchecked")
 	@ApiMethod(
  			httpMethod = "GET", 
  			name = "user.groups.group",
 			path="user/{userId}/group/{groupId}"
 			)
 	public Group getGroup(@Named("userId") String userId, @Named("groupId") String groupId) {
 		Group objGroup = new Group();
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		
 		Query q = pm.newQuery(Group.class);
 		
 		q.setFilter("groupId == groupIdParam");
 		q.declareParameters("String groupIdParam");
 		
 		List<Group> groups = (List<Group>) q.execute(groupId);
 		
 		objGroup = groups.get(0);
 		
 		return objGroup;
 	}
 	
 	
 	
 	
 	/**
 	 * Test method which shows what will be protocol for implementing search method
 	 * */
 	@ApiMethod(
 		      httpMethod = "GET", 
 		      name = "user.search",
 		      path = "user/search/{queryString}")
 		  public List<User> search(@Named("queryString") String queryString) 
 		      throws Exception {
 		   /* if (guser == null) {
 		      throw new UnauthorizedException(CustomErrors.MUST_LOG_IN.toString());
 		    }
 		    */
 		    List<User> returnList = new ArrayList<User>();
 		    /*Results<ScoredDocument> searchResults = INDEX.search(queryString);
 
 		    for (ScoredDocument scoredDoc : searchResults) {
 		      Field fieldId = scoredDoc.getOnlyField("id");
 		      if (fieldId == null || fieldId.getText() == null)
 		        continue;
 
 		      long userId = Long.parseLong(fieldId.getText());
 		      if (userId != -1) {
 		        User p = getUser(userId, guser);
 		        returnList.add(p);
 		      }
 		    }*/
 		    return returnList;
 		  }
 	
 	@ApiMethod(
  			httpMethod = "POST", 
  			name = "user.login",
 			path="user/doLogin"
 	)
 	public User doLogin(User user) {
 		user = getOrInsertUser(user);
 		user.setLastLoggedInAt(new Date());
 		user.setAccessToken(UUID.randomUUID().toString());
 		return user;
 		
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	@ApiMethod(path="userendpoint/user/getorinsertuser")
 	public User getOrInsertUser(User user){
 		//TODO : Add support for app register and login
 		String apiId = null;
 		
 		long start = new Date().getTime();
 		
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		
 		Query q = pm.newQuery(User.class);
 
 		if("google".equalsIgnoreCase(user.getLoginType())) {
 			apiId = user.getGoogleId();
 			q.setFilter("googleId == googleIdParam");
 			q.declareParameters("String googleIdParam");
 		} else if("facebook".equalsIgnoreCase(user.getLoginType())) {
 			apiId = user.getFacebookId();
 			q.setFilter("facebookId == facebookIdParam");
 			q.declareParameters("String facebookIdParam");
 		}
 		
 		List<User> execute = null;
 		
		execute = (List<User>)q.execute(apiId);
		if(execute.size()>0){
			user = execute.get(0);
		} else {
 			user = this.insertUser(user);
 		}
 		
 		long end = new Date().getTime();
 		
 		System.out.println("Time taken to login: ");
		System.out.println(end - start/1000*1000 + " Seconds");
 		
 		return user;
 	}
 
 	
 	/**
 	 * This method is supposed to retrieve all the expenses of a user with id.
 	 * @param id UserId of the user whose expenses to be fetched
 	 * */
 	@SuppressWarnings("unchecked")
 	@ApiMethod(
  			httpMethod = "GET", 
  			name = "user.expenses",
 			path="user/{id}/expenses"
 			)
 	public List<ExpenseEntity> getExpenses(@Named("id") String id) {
 		
 		List<ExpenseEntity> alExpenses = null;
 		
 		HashMap<String, ExpenseEntity> hmExpenses = new HashMap<String, ExpenseEntity>();
 		
 		ExpenseInfo objExpenseInfo = null;
 		int iCounter = 0;
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		
 		Query q = pm.newQuery(ExpenseInfo.class);
 
 		q.setFilter("userId == userIdParam");
 		q.declareParameters("String userIdParam");
 		
 		List<ExpenseInfo> execute = null;
 		
 		execute = (List<ExpenseInfo>)q.execute(id);
 		
 		while(iCounter < execute.size()){
 			objExpenseInfo = execute.get(iCounter++);
 			
 			q = pm.newQuery(ExpenseEntity.class);
 			
 			q.setFilter("expenseEntityId == expenseIdParam");
 			q.declareParameters("String expenseIdParam");
 			
 			List<ExpenseEntity> listExpenseResult = (List<ExpenseEntity>)q.execute(objExpenseInfo.getExpenseId());
 			hmExpenses.put(listExpenseResult.get(0).getExpenseEntityId(), listExpenseResult.get(0));
 		}
 		
 		alExpenses = new ArrayList<ExpenseEntity>(hmExpenses.values());
 		
 		return alExpenses;
 	}
 	
 	
 	/**
 	 * This method is supposed to retrieve all the expenses of a user with id.
 	 * @param id UserId of the user whose expenses to be fetched
 	 * */
 	@SuppressWarnings("unchecked")
 	@ApiMethod(
  			httpMethod = "GET", 
  			name = "user.iou",
 			path="user/{id}/iou"
 			)
 	public List<IOU> getIOU(@Named("id") String id) {
 		
 /*		List<IOU> alIOU = null;
 		
 		HashMap<String, ExpenseEntity> hmExpenses = new HashMap<String, ExpenseEntity>();*/
 		
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		
 		Query q = pm.newQuery(IOU.class);
 
 		q.setFilter("fromUserId == userIdParam");
 		q.setFilter("toUserId == userIdParam");
 		q.declareParameters("String userIdParam");
 		q.declareParameters("String userIdParam");
 		
 		List<IOU> execute = null;
 		execute = (List<IOU>)q.execute(id);
 		
 		/*while(iCounter < execute.size()){
 			objIOU = execute.get(iCounter++);
 			
 			q = pm.newQuery(ExpenseEntity.class);
 			
 			q.setFilter("expenseEntityId == expenseIdParam");
 			q.declareParameters("String expenseIdParam");
 			
 			List<ExpenseEntity> listExpenseResult = (List<ExpenseEntity>)q.execute(objExpenseInfo.getExpenseId());
 			hmExpenses.put(listExpenseResult.get(0).getExpenseEntityId(), listExpenseResult.get(0));
 		}
 		
 		alExpenses = new ArrayList<ExpenseEntity>(hmExpenses.values());
 		*/
 		return execute;
 	}
 	
 	
 	
 }
 
 
 
