 package zinger.secsan.server;
 
 //import com.google.gwt.rpc.server.*;
 import com.google.gwt.user.server.rpc.*;
 
 import com.google.appengine.api.taskqueue.*;
 import com.google.appengine.api.users.*;
 
 import java.util.*;
 import java.util.logging.*;
 
 import zinger.secsan.client.*;
 import zinger.secsan.db.*;
 
 import static zinger.secsan.client.Util.*;
 
 public class BackEndServiceImpl extends RemoteServiceServlet implements BackEndService
 {
 	protected final Logger log = Logger.getLogger(this.getClass().getName());
 	protected final UserService userService = UserServiceFactory.getUserService();
 	
 	protected String getLoggedInUser() throws InsufficientPrivilegesException
 	{
 		final User user = userService.getCurrentUser();
 		log.info(String.format("User: %s", user));
 		if(user == null)
 			throw new InsufficientPrivilegesException("Not logged in", userService.createLoginURL("/"));
 		return user.getEmail();
 	}
 	
 	public Set<String> getPools() throws InsufficientPrivilegesException, NotFoundException
 	{
 		return StateManagerFactory.INSTANCE.getStateManager().getUserPools(this.getLoggedInUser());
 	}
 	
	protected void assertMyPool(final String pool) throws InsufficientPrivilegesException, NotFoundException
	{
		final String user = getLoggedInUser();
		final Set<String> usersInPool = getUsersInPool(pool);
		if(!usersInPool.isEmpty() && !usersInPool.contains(user))
			throw new InsufficientPrivilegesException("This pool exists and does not belong to you.");
	}
	
 	public void joinPool(final String pool) throws InsufficientPrivilegesException, NotFoundException
 	{
 		validStrings(pool);
 		final String user = getLoggedInUser();
 		try
 		{
			assertMyPool(pool);
 		}
 		catch(final NotFoundException ex)
 		{
 		}
 		StateManagerFactory.INSTANCE.getStateManager().addPool(this.getLoggedInUser(), pool);
 	}
 	
 	public void leavePool(final String pool) throws InsufficientPrivilegesException
 	{
 		StateManagerFactory.INSTANCE.getStateManager().removePool(this.getLoggedInUser(), pool);
 	}
 	
 	public void remindPoolAssignment(final String pool) throws InsufficientPrivilegesException
 	{
 		final String user = getLoggedInUser();
 		final com.google.appengine.api.taskqueue.Queue taskQueue = QueueFactory.getDefaultQueue();
 		taskQueue.add(TaskOptions.Builder
 			.withUrl("/job/notify_assignment")
 			.method(TaskOptions.Method.GET)
 			.param(NotifyAssignmentServlet.USER_PARAM, user)
 			.param(NotifyAssignmentServlet.POOL_PARAM, pool)
 		);
 	}
 	
 	public Set<String> getUsersInPool(final String pool) throws InsufficientPrivilegesException, NotFoundException
 	{
 		validStrings(pool);
 		return StateManagerFactory.INSTANCE.getStateManager().getUsersInPool(pool);
 	}
 	
 	public void addToPool(final String user, final String pool) throws InsufficientPrivilegesException, NotFoundException, IllegalArgumentException
 	{
 		validStrings(user, pool);
 		validEmail(user);
 		final String me = getLoggedInUser();
 		if(me.equals(user))
 			throw new InsufficientPrivilegesException("You cannot add yourself");
 		StateManagerFactory.INSTANCE.getStateManager().addPool(user, pool);
 	}
 	
 	public void removeFromPool(final String user, final String pool) throws InsufficientPrivilegesException, NotFoundException
 	{
 		removeUsersFromPool(pool, Arrays.asList(user));
 	}
 	
 	public void removeUsersFromPool(final String pool, final Iterable<String> users) throws InsufficientPrivilegesException, NotFoundException
 	{
 		validStrings(pool);
 		validStrings(users);
		assertMyPool(pool);
 		StateManagerFactory.INSTANCE.getStateManager().removeUsersFromPool(pool, users);
 	}
 	
 	public void shufflePool(final String pool) throws InsufficientPrivilegesException, NotFoundException
 	{
 		validStrings(pool);
 		final String me = getLoggedInUser();
 		final StateManager stateManager = StateManagerFactory.INSTANCE.getStateManager();
 		final List<String> users = new ArrayList<String>(stateManager.getUsersInPool(pool));
 		if(!users.contains(me))
 			throw new InsufficientPrivilegesException("You are not authorized to shuffle pool " + pool);
 		final List<Integer> assignedIndexes = Main.randomizeIndexes(users.size());
 		
 		final com.google.appengine.api.taskqueue.Queue taskQueue = QueueFactory.getDefaultQueue();
 		
 		for(int i = assignedIndexes.size() - 1; i >= 0; --i)
 		{
 			final String user = users.get(i);
 			stateManager.setAssignedToUser(user, users.get(assignedIndexes.get(i)), pool);
 			taskQueue.add(TaskOptions.Builder
 				.withUrl("/job/notify_assignment")
 				.method(TaskOptions.Method.GET)
 				.param(NotifyAssignmentServlet.USER_PARAM, user)
 				.param(NotifyAssignmentServlet.POOL_PARAM, pool)
 			);
 		}
 	}
 	
 	public boolean isPoolShuffled(final String pool) throws InsufficientPrivilegesException, NotFoundException
 	{
 		validStrings(pool);
 		return StateManagerFactory.INSTANCE.getStateManager().isPoolShuffled(pool);
 	}
 	
 	@Override
 	public String toString() { return "back-end"; }
 }
