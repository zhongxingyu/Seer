 package edu.rit.taskers.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.rit.taskers.model.Actionable;
 import edu.rit.taskers.model.Event;
 import edu.rit.taskers.persistence.EventDao;
 
 /**
  * Handles requests with a space related to events.
  */
 @Controller
 @RequestMapping("/events")
 public class EventController {
 
 	private static final Logger logger = LoggerFactory.getLogger(EventController.class);
 
 	@Autowired
 	private EventDao eventDao;
 
 	/**
 	 * Fetch all events that belong to session space id
 	 * @return Collection of events
 	 */
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView getAllEvents() {
 		//TODO - Session space ID
 		ModelAndView eventsMainPage = new ModelAndView("events", "allevents", eventDao.findAll());
 		return eventsMainPage;
 	}
 
 	/**
 	 * Retrieve the event details from a selected event in the list
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.GET)
 	public ModelAndView getEventDetails(@PathVariable int id) {
 		ModelAndView eventDetailsEditPage = new ModelAndView("editevent", "event", eventDao.findById(id));
 		return eventDetailsEditPage;
 	}
 
 	/**
 	 * Delete a event by id
 	 */
 	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
 	public String delete(@PathVariable int id) {
 		Event retrievedEvent = eventDao.findById(id);
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
 			@RequestParam(value="description") String desc) {
 
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
		System.out.println(fetchedEvent.getId() + " " + eventName + " " + dateTimeResult.toString() + " " + desc );
 
		eventDao.update( fetchedEvent );
 		return "Event successfully updated!";
 	}
 
 	/**
 	 * Create New Event page requested
 	 */
 	@RequestMapping(value="/new", method=RequestMethod.GET)
 	public ModelAndView getCreateEventPage() {
 		ModelAndView newEventPage = new ModelAndView("newevent");
 		return newEventPage;
 	}
 
 	/**
 	 * Create New Event from js function with form data parameters
 	 */
 	@RequestMapping(value="/new", method=RequestMethod.POST)
 	public @ResponseBody String createNewEvent(@RequestParam(value="title") String eventName,
 			@RequestParam(value="targetdate") String targetDate,
 			@RequestParam(value="targettime") String targetTime,
 			@RequestParam(value="description") String desc) {
 
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
 
 		eventDao.save( newEvent );
 		return "Event successfully created!";
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
