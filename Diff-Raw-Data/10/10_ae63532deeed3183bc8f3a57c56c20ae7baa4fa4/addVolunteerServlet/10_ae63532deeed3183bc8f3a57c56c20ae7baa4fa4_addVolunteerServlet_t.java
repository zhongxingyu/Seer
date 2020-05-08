 package com.VolunteerCoordinatorApp;
 
 import java.io.*;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.servlet.http.*;
 import com.google.gdata.client.calendar.CalendarQuery;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.data.extensions.*;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 @SuppressWarnings("serial")
 public class addVolunteerServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws IOException {
 		doGet(req, resp); 
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String date = req.getParameter("date");
 		String title = req.getParameter("title");
 		String name = req.getParameter("name"); 
 		
 		URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/" +
 		        "private/full");
 		  
 		CalendarQuery myQuery = new CalendarQuery(feedUrl);
 		   
 		CalendarService myService = new CalendarService("Volunteer-Coordinator-Calendar"); 
 		try {
 			myService.setUserCredentials("rockcreekvolunteercoordinator@gmail.com", "G0covenant");
 		} catch (AuthenticationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Send the request and receive the response:
 		CalendarEventFeed resultFeed = null;
 		try {
 			resultFeed = myService.query(myQuery, CalendarEventFeed.class);
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		  
 		List<CalendarEventEntry> results = (List<CalendarEventEntry>)resultFeed.getEntries(); 
 		
 		for (CalendarEventEntry entry : results) {
 	        // Get the start time for the event 
 	        When time = entry.getTimes().get(0); 
 	        DateTime start = time.getStartTime(); 
	        
	        // TODO Find a way to automate this switching.
	        //(Offset is done in minutes)
	        //start.setTzShift(-240);
	        
	        //Use an offset of -300 for non-Daylight Savings time.
	        start.setTzShift(-300);
 	        
 	        // Concert to milliseconds to get a date object, which can be formatted easier. 
 	        Date entryDate = new Date(start.getValue() + 1000 * (start.getTzShift() * 60)); 
 	        
 			String datePattern = "MM-dd-yyyy"; 
 			SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
 	        String startDay = dateFormat.format(entryDate);
 	        
 	        String eventTitle = new String(entry.getTitle().getPlainText());
 	        
 	        if (startDay.equals(date) && eventTitle.equals(title)) {
 	            String content = entry.getPlainTextContent(); 
 	            
 	            if (content.contains("<volunteers>")) {
 	            	String contentArray[] = content.split("<volunteers>");
 	            	StringBuffer volList = new StringBuffer(contentArray[1]);
 	            	//make sure the user isn't already in the list
 	            	if (!contentArray[1].contains(name.trim())) {
 		            	int end = volList.indexOf("</volunteers>");
 		            	volList.insert(end, name.trim() + " ; ");
 		            	content = contentArray[0] + volList;
 	            	}
 	            }
 	            else {
 	            	content += " <volunteers> " + name + " ; </volunteers>";
 	            }
 	            entry.setContent(new PlainTextConstruct(content));
 	            URL editUrl = new URL(entry.getEditLink().getHref());
 	            try {
	                System.out.println("Just making sure.");
 					myService.update(editUrl, entry);
 				} catch (ServiceException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        }
 		}
         
         resp.sendRedirect("/calendar.jsp?name=" + name);
 	}
 }
 	
