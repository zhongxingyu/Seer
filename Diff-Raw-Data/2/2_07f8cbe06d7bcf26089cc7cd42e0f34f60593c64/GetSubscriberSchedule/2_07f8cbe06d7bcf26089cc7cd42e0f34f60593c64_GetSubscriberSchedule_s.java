 package api;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 
 import deadlineserver.SaveDeadlineServlet;
 import deadlineserver.Utils;
 import deadlineserver.models.DUser;
 import deadlineserver.models.Deadline;
 import deadlineserver.models.Subscription;
 
 @SuppressWarnings("serial")
 public class GetSubscriberSchedule extends HttpServlet
 {
 	private static final Logger log = Logger.getLogger(GetSubscriberSchedule.class.getName());
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException
 	{
 		Objectify ofy = ObjectifyService.begin();
 		Utils.registerObjectifyClasses();
 		log.setLevel(Level.ALL);
 		
 		UserService userService = UserServiceFactory.getUserService();
 	    User user = userService.getCurrentUser();
 	    
 	    if(user==null) {
 	    	resp.getWriter().println("[]");
 	    	log.info("User not logged in");
 	    	return;
 	    }
 	    
 	    DUser oldUser = ofy.query(DUser.class).filter("userId",user.getEmail()).get();
 	    
 	    resp.setContentType("application/json");
 	        
 	    if(oldUser==null) {
 	    	resp.getWriter().println("[]");
 	    	log.info("User not found in database");
 	    	return;
 	    }
 	    
 	    if(oldUser.user==null){
 	    	oldUser.user=user;
 	    	ofy.put(oldUser);
 	    }
 	    
 	    String subscriptionId = (String)req.getParameter("id");
 	    
 	    if(subscriptionId==null)
 	    {
 	    	resp.getWriter().println("[]");
 	    	log.info("Course ID not supplied");
 	    	return;
 	    }
 
 	    // Check if subscription exists
 	    
 	    Subscription s = null;
 	    Key<Subscription> sKey = new Key<Subscription>(Subscription.class, subscriptionId);
 	    
 	    try {
 	    	s = ofy.get(sKey);
 	    }
 	    catch(Exception e)
 	    {
 	    	resp.getWriter().println("[]");
 	    	log.info("Course ID does not exist");
 	    	return;
 	    }
 	    
 	    // Check if user is owner of subscription
 	    
 	    if(s.owner.getId()!=oldUser.id)
 	    {
 	    	resp.getWriter().println("[]");
 	    	log.info("User is not course owner");
 	    	return;
 	    }
 	    
 	    // If everything looks good so far, we will start creating the proper JSON object
 	    String jsonResult = "[";
 	    
 	    // Go through all users looking for a subscription to this course
 	    HashMap<String,Integer> courseToSubscribers = new HashMap<String,Integer>(); // Used for multiplication
 	    ArrayList<Key<Subscription>> courses = new ArrayList<Key<Subscription>>(); // Used to query Objectify
 	    
 	    Query<DUser> allUsers = ofy.query(DUser.class);
 	    for(DUser du : allUsers)
 	    {
 	    	for(Key<Subscription> ks:du.subscriptions)
 	    	{
 	    		System.out.println(ks.getName() + " " + ks.equals(sKey));
 	    	}
 	    	// Check to see if the user is a subscriber to this course
 	    	if(du.subscriptions.contains(sKey))
 	    	{
 	    		// Will consider the given course as part of the workload as well
 	    		for(Key<Subscription> userSubKey : du.subscriptions)
 	    		{
 	    			Integer numSubscribersToCourse = courseToSubscribers.get(userSubKey.getName());
 	    			if(numSubscribersToCourse==null)
 	    			{
 	    				courseToSubscribers.put(userSubKey.getName(), 1);
 	    				courses.add(userSubKey);
 	    			}
 	    			else
 	    			{
 	    				courseToSubscribers.put(userSubKey.getName(), numSubscribersToCourse+1);
 	    			}
 	    		}
 	    		
 	    	}
 	    }
 	    
 	    // Get timezone of requesting user in required format
 	    
 	    Double timeZoneOffset = Double.parseDouble((String)req.getParameter("offset"));
 	    timeZoneOffset = timeZoneOffset/60;
 	    String timeZoneStr = "GMT";
 	    if(timeZoneOffset<0)
 	    {
 	    	timeZoneStr = timeZoneStr + "+";
 	    	timeZoneOffset = timeZoneOffset *-1;
 	    }
 	    else
 	    {
 	    	timeZoneStr = timeZoneStr + "-";
 	    }
 	    
 	    if(timeZoneOffset.toString().contains(".5"))
 	    {
 	    	timeZoneOffset = timeZoneOffset-0.5;
 	    	timeZoneStr += timeZoneOffset.intValue() + ":30";
 	    }
 	    else
 	    {
 	    	timeZoneStr += timeZoneOffset.intValue() + ":00";
 	    }
 	    //System.out.println("Timezone offset: " + timeZoneStr);
 	    TimeZone localTimeZone = TimeZone.getTimeZone(timeZoneStr);
 	    
 	    // Fixing the date range for deadline search
 	    int dateRange = 20; // TODO: Figure out how many days in the future this should be
 	    Date now = new Date();
 	    //System.out.println("Current date: " + now);
 		Calendar localCalendar = Calendar.getInstance(localTimeZone);
 		localCalendar.setTime(now);
 		localCalendar.add(Calendar.DATE, 1);
 		localCalendar.set(Calendar.HOUR_OF_DAY, 0);
 		localCalendar.set(Calendar.MINUTE, 0);
 		localCalendar.set(Calendar.SECOND, 0);
 		localCalendar.set(Calendar.MILLISECOND, 0);
 		now = localCalendar.getTime();
 		//System.out.println("Begin tomorrow morning in calendar: " + now);
 		
 		localCalendar.add(Calendar.DATE, dateRange); 
 		Date nowPlusRange = localCalendar.getTime();
 		//System.out.println("End date morning in calendar: " + nowPlusRange);
 		
 		// Getting deadlines matching the subscribed courses and date ranges
 	    List<Deadline> allDeadlines = new ArrayList<Deadline>();
 	    
 	    try
 	    {
 	    	allDeadlines = ofy.query(Deadline.class).filter("dueDate >", now).filter("dueDate <", nowPlusRange).filter("subscription in", courses).list();
 	    }
 	    catch(Exception e)
 	    {
 	    	// Generally means no deadlines matched the filter criteria
 	    	log.warning("Query failed : " + e.getMessage());
 	    }
 		
 	    // Create buckets for each day
 	    localCalendar.add(Calendar.DATE, -dateRange);
 	    Date beginCurrentBucket = localCalendar.getTime();
 	    Date endCurrentBucket = null;
 	    
 	    // Hold the dates and the affected subscribers on each date
 	    ArrayList<String> dates = new ArrayList<String>();
 	    ArrayList<Integer> numSubscribersAffected = new ArrayList<Integer>();
 	    
 	    SimpleDateFormat localDateFormat = new SimpleDateFormat("MMM");
 	    localDateFormat.setTimeZone(localTimeZone);
 	    
 	    for(int i=0; i<dateRange; i++)
 	    {
 	    	String dateString = localCalendar.get(Calendar.DAY_OF_MONTH) + "-";
 	    	dateString = dateString + localDateFormat.format(localCalendar.getTime());
 	    	dates.add(dateString);
 	    	
 	    	localCalendar.add(Calendar.DATE, 1);
 	    	endCurrentBucket = localCalendar.getTime();
 	    	int subscribersWithDeadlineOnDay = 0;
 	    	
 	    	String coursesWithDeadlines = "";
 	    	
 	    	for(Deadline d:allDeadlines)
 	    	{
 	    		// Checking if deadline falls during that day
 	    		if((d.dueDate.before(endCurrentBucket) || d.dueDate.equals(endCurrentBucket)) && d.dueDate.after(beginCurrentBucket))
 	    		{
 	    			Integer subscribers = courseToSubscribers.get(d.subscription.getName());
 	    			subscribersWithDeadlineOnDay += subscribers;
 	    			coursesWithDeadlines += d.subscription.getName() + ", ";
 	    		}
 	    	}
 	    	
 	    	// Removing the last comma space
 	    	if(coursesWithDeadlines.length()>2)
 	    		coursesWithDeadlines = coursesWithDeadlines.substring(0, coursesWithDeadlines.length()-2);
 	    	
 	    	beginCurrentBucket = endCurrentBucket;
 	    	
 	    	numSubscribersAffected.add(subscribersWithDeadlineOnDay);
 	    	
 	    	String htmlTooltipString = "<div class=\\\"chart-tooltip\\\"><b>" + subscribersWithDeadlineOnDay + 
 	    			"</b> students already have deadlines on <b>" + dateString +"</b> in these courses: <b>" 
 	    			+ coursesWithDeadlines + "</b></div>";
 	    	
	    	System.out.println("Day: " + dateString + " Subscribers affected: " + subscribersWithDeadlineOnDay);
 	    	jsonResult += "[\"" + dateString + "\", " + subscribersWithDeadlineOnDay  + ", \"" + htmlTooltipString
 	    			+ "\"],\n";
 	    }
 	    
 	    jsonResult = jsonResult.substring(0, jsonResult.length()-2);
 	    jsonResult += "\n]";
 	    
 	    resp.getWriter().println(jsonResult);
 	}
 }
