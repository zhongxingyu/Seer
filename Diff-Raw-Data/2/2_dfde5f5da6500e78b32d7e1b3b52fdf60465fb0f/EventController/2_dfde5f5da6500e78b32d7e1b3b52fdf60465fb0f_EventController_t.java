 package edu.rit.taskers.controller;
 
 import java.security.Principal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.rit.taskers.command.UpdateEventCommand;
 import edu.rit.taskers.model.Actionable;
 import edu.rit.taskers.model.Event;
 import edu.rit.taskers.persistence.ContactDao;
 import edu.rit.taskers.persistence.EventDao;
 import edu.rit.taskers.persistence.UserDao;
 
 /**
  * Handles requests with a space related to events.
  */
 @Controller
 @RequestMapping("/events")
 public class EventController {
 
 	private static final Logger logger = LoggerFactory.getLogger(EventController.class);
 
 	@Autowired
 	private EventDao eventDao;
 
     @Autowired
     private UserDao userDao;
 
     @Autowired
     private ContactDao contactDao;
 
 	/**
 	 * Fetch all events that belong to session space id
 	 * @return Collection of events
 	 */
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView getAllEvents(@CookieValue("SPACE") int id, Principal principal) {
 
         //Sort events so that events you're invited to appear above
         int uid = userDao.findByUsername(principal.getName()).getPrimaryContact().getId();
         List<Integer> invitedTo = eventDao.findEvents(uid);
         List<Event> events = eventDao.findBySpace(id);
         List<Event> eventsSorted = new ArrayList<Event>();
         for(Event e : events) {
             if(invitedTo.contains(e.getId())) {
                 eventsSorted.add(e);
             }
         }
        events.removeAll(eventsSorted);
         eventsSorted.addAll(events);
 
 		ModelAndView eventsMainPage = new ModelAndView("events", "allevents", eventsSorted);
 		return eventsMainPage;
 	}
 
 	/**
 	 * Retrieve the event details from a selected event in the list
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.GET)
 	public ModelAndView getEventDetails(@PathVariable int id, @CookieValue("SPACE") int spaceId) {
 		ModelAndView eventDetailsEditPage = new ModelAndView("editevent", "event", eventDao.findById(id));
 		eventDetailsEditPage.addObject("allcontacts", contactDao.findBySpace(spaceId));
         eventDetailsEditPage.addObject("invitedcontacts", eventDao.findInvited(id));
 		return eventDetailsEditPage;
 	}
 
 	/**
 	 * Delete a event by id
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
 	public String delete(@PathVariable int id, Principal principal) {
 		Event retrievedEvent = eventDao.findById(id);
         if(userDao.findByUsername(principal.getName()).getPrimaryContact().getId() !=
                 retrievedEvent.getCreator().getId()) {
             return "Only Creator Can Delete.";
         }
 
 		eventDao.delete(retrievedEvent);
 		return "redirect:/events";
 	}
 
 	/**
 	 * Update event details from a selected event in the list
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.POST)
 	public @ResponseBody String updateEvent(@PathVariable int id, @RequestParam(value="title") String eventName,
 			@RequestParam(value="targetdate") String targetDate,
 			@RequestParam(value="targettime") String targetTime,
 			@RequestParam(value="description") String desc,
 			@RequestParam(value="allcontacts") String invitedcontacts) {
 
 		if ( isEmpty( eventName ) ) {
 			return "Please specify a title.";
 		} else if ( isEmpty( targetDate ) ) {
 			return "Please specify a date.";
 		} else if ( isEmpty( targetTime ) ) {
 			return "Please specify a time.";
 		}
 
 		Date dateTimeResult = combineDateAndTime(targetDate, targetTime);
 		if ( dateTimeResult == null ) {
 			return "Invalid date/time specified.";
 		}
 		
 		
 		// Create the new event object and persist it
 		Event fetchedEvent = eventDao.findById(id);
 		fetchedEvent.setName( eventName );
 		fetchedEvent.setTargetDate( dateTimeResult );
 		fetchedEvent.setDescription( desc );
 
         //parse list of contact ids
         List<Integer> contactIds = new ArrayList<Integer>();
         if (!invitedcontacts.equals("null")) {
 	        String[] ids = invitedcontacts.split(",");
 	        for(String contactId : ids) {
 	            contactIds.add(Integer.parseInt(contactId));
 	        }
         }
 
         UpdateEventCommand command = new UpdateEventCommand(fetchedEvent, eventDao, contactIds);
 
         command.execute();
 
 		return "success"; // This is coupled to javascript, must be "success" (re-design later)
 	}
 
 	/**
 	 * Create New Event page requested
 	 */
 	@RequestMapping(value="/new", method=RequestMethod.GET)
 	public ModelAndView getCreateEventPage(@CookieValue("SPACE") int spaceId) {
 		ModelAndView newEventPage = new ModelAndView("newevent");
 		newEventPage.addObject("allcontacts", contactDao.findBySpace(spaceId));
 		return newEventPage;
 	}
 
 	/**
 	 * Create New Event from js function with form data parameters
 	 */
 	@RequestMapping(value="/new", method=RequestMethod.POST)
 	public @ResponseBody String createNewEvent(Principal principal,
             @RequestParam(value="title") String eventName,
 			@RequestParam(value="targetdate") String targetDate,
 			@RequestParam(value="targettime") String targetTime,
 			@RequestParam(value="description") String desc,
 			@RequestParam(value="allcontacts") String invitedcontacts, /* CSV OF CONTACT IDS */
             @CookieValue("SPACE") int spaceId) {
 		
 		if ( isEmpty( eventName ) ) {
 			return "Please specify a title.";
 		} else if ( isEmpty( targetDate ) ) {
 			return "Please specify a date.";
 		} else if ( isEmpty( targetTime ) ) {
 			return "Please specify a time.";
 		}
 
 		Date dateTimeResult = combineDateAndTime( targetDate, targetTime );
 		if ( dateTimeResult == null ) {
 			return "Invalid date/time specified.";
 		}
 
 		// Create the new event object and persist it
 		Event newEvent = new Event();
 		newEvent.setName( eventName ) ;
 		newEvent.setTargetDate( dateTimeResult );
 		newEvent.setDescription( desc );
         newEvent.setSpaceId( spaceId );
         newEvent.setCreator( userDao.findByUsername(principal.getName()).getPrimaryContact() );
 
         //parse list of contact ids
         List<Integer> contactIds = new ArrayList<Integer>();
         if (!invitedcontacts.equals("null")) {
 	        String[] ids = invitedcontacts.split(",");
 	        for(String contactId : ids) {
 	            contactIds.add(Integer.parseInt(contactId));
 	        }
         }
 
         UpdateEventCommand command = new UpdateEventCommand(newEvent, eventDao, contactIds);
 
         command.execute();
 
 		return "success"; // This is coupled to javascript, must be "success" (re-design later)
 	}
 	
 	/**
 	 * Retrieve the event details from a selected event in the list
 	 */
 	@RequestMapping(value="/{id}/comments", method=RequestMethod.GET)
 	public String getEventComments(@PathVariable int id) {
 		eventDao.findById(id).getComments(); //Use this for whatever we're returning.
 		
 		//TODO - Hook into JSP?
 		//ModelAndView eventDetailsEditPage = new ModelAndView("editevent", "event", eventDao.findById(id));		
 		//return eventDetailsEditPage;
 		return "TBD";
 	}
 
     /**
 	 * Combine the date and time fields to create one Datetime
 	 */
 	private Date combineDateAndTime( String targetDate, String targetTime ) {
 		try {
 			// Heavyweight, ugly way to generate one datetime stamp from our separated date and time
 			// without the help of a third-party library (such as Joda time)
 			DateFormat df = new SimpleDateFormat( Actionable.ACTION_UI_DATEFORMAT );
 			DateFormat dft = new SimpleDateFormat( Actionable.ACTION_UI_TIMEFORMAT );
 
 			Date tempDate = df.parse( targetDate );
 			Date tempTime = dft.parse( targetTime );
 
 			Calendar calendarA = Calendar.getInstance();
 			calendarA.setTime( tempDate );
 
 			Calendar calendarB = Calendar.getInstance();
 			calendarB.setTime( tempTime );
 
 			calendarA.set( Calendar.HOUR_OF_DAY, calendarB.get( Calendar.HOUR_OF_DAY) );
 			calendarA.set( Calendar.MINUTE, calendarB.get( Calendar.MINUTE) );
 			calendarA.set( Calendar.SECOND, calendarB.get( Calendar.SECOND) );
 			calendarA.set( Calendar.MILLISECOND, calendarB.get(Calendar.MILLISECOND) );
 
 			return calendarA.getTime();
 
 		} catch ( ParseException e ) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 
 
 	/**
 	 * String isEmpty helper method
 	 */
 	private boolean isEmpty(String s) {
 		return ( s == null ) || ( "".equals(s) ) || ( s.length() == 0 );
 	}
 }
