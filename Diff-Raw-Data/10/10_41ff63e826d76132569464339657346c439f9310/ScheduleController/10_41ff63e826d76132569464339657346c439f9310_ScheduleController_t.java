 /**
  * 
  */
 package se.mrpeachum.scheduler.controllers;
 
 import java.text.DateFormat;
 import java.text.DateFormatSymbols;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import se.mrpeachum.scheduler.entities.Employee;
 import se.mrpeachum.scheduler.entities.Position;
 import se.mrpeachum.scheduler.entities.ShiftDto;
 import se.mrpeachum.scheduler.entities.User;
 import se.mrpeachum.scheduler.exception.RedirectException;
 import se.mrpeachum.scheduler.service.SchedulerService;
 
 /**
  * @author eolsson
  * 
  */
 @Controller
 public class ScheduleController {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);
 
     private static final DateFormat YEAR_WEEK_FORMAT = new SimpleDateFormat("YYYYww");
 
     private static final DateFormat PRETTY_WEEK_FORMAT = new SimpleDateFormat("MMM d, yyyy (w)", DateFormatSymbols.getInstance(Locale.US));
 
     private SchedulerService schedulerService;
     
     @Autowired
     public ScheduleController(SchedulerService schedulerService) {
     	this.schedulerService = schedulerService;
     }
 
     @RequestMapping("/")
     public String getMain(final ModelMap model, final HttpSession session, @RequestParam(value = "w", required = false) final String yearAndWeek) {
     	User user;
     	try {
     		user = schedulerService.fetchOrSaveUser(session);
     	} catch (RedirectException r) {
     		return r.getRedirectUrl();
     	}
         
         model.addAttribute("user", user);
         model.addAttribute("positions", schedulerService.getPositions(user));
         model.addAttribute("employees", schedulerService.getEmployees(user));
         model.addAttribute("firstDayOfWeek", getFirstDayOfWeek(yearAndWeek));
        model.addAttribute("nextWeek", makeWeekLink(yearAndWeek, 1));
        model.addAttribute("previousWeek", makeWeekLink(yearAndWeek, -1));
         return "main";
     }
 
     @RequestMapping(value ="/positions", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
     @ResponseStatus(HttpStatus.NO_CONTENT)
     public void putPositions(@RequestBody final Position[] positions, final HttpSession session) {
     	User user = getUser(session);
     	LOGGER.debug("Received {}", Arrays.asList(positions));
     	schedulerService.mergePositions(user, Arrays.asList(positions));
     }
     
     @RequestMapping(value = "/employees", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
     @ResponseStatus(HttpStatus.NO_CONTENT)
     public void putEmployees(@RequestBody final Employee[] employees, final HttpSession session) {
     	User user = getUser(session);
     	LOGGER.debug("Received {}", Arrays.asList(employees));
     	schedulerService.mergeEmployees(user, Arrays.asList(employees));
     }
 
     @RequestMapping(value = "/shifts", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
     @ResponseStatus(HttpStatus.CREATED)
     public void postShift(@RequestBody @Valid final ShiftDto shift, final HttpSession session) {
     	User user = getUser(session);
     	LOGGER.debug("Received {}", shift);
     	schedulerService.saveShift(shift, user);
     }
     
     @RequestMapping(value = "/shifts", method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.NO_CONTENT)
     public void postShift(@RequestParam final Long id, final HttpSession session) {
     	User user = getUser(session);
     	LOGGER.debug("Received delete request {}", id);
     	schedulerService.deleteShift(id, user);
     }
     
     @RequestMapping(value = "/copy", method = RequestMethod.POST)
     @ResponseStatus(HttpStatus.NO_CONTENT)
     public void copyShifts(@RequestParam final Long employeeId, @RequestParam final String fromWeek, @RequestParam final String toWeek,
     		final HttpSession session) throws Exception {
     	User user = getUser(session);
     	LOGGER.debug("Got request to copy shifts for emp id {} from week {} to week {} for user {}", 
     			new Object[]{employeeId, fromWeek, toWeek, user});
     	Date fromParsed = PRETTY_WEEK_FORMAT.parse(fromWeek);
     	Date toParsed = YEAR_WEEK_FORMAT.parse(toWeek);
     	schedulerService.copyShifts(employeeId, fromParsed, toParsed, user);
     }
     
     protected final String makeWeekLink(String yearAndWeek, int increment) {
         final Date firstDay = getFirstDayOfWeek(yearAndWeek);
         Calendar cal = Calendar.getInstance(Locale.US);
         cal.setTime(firstDay);
         cal.add(Calendar.WEEK_OF_YEAR, increment);
        String week = Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
        cal.add(Calendar.DATE, 7);
        String year = Integer.toString(cal.get(Calendar.YEAR));
        return year + week;
     }
 
 	private User getUser(final HttpSession session) {
 		User user;
     	try {
     		user = schedulerService.fetchOrSaveUser(session);
     	} catch (RedirectException r) {
     		throw new IllegalStateException("Must be logged in");
     	}
 		return user;
 	}
     
     protected final Date getFirstDayOfWeek(String yearAndWeek) {
         final Calendar cal = Calendar.getInstance(Locale.US);
         if (yearAndWeek != null) {
             try {
                 cal.setTime(YEAR_WEEK_FORMAT.parse(yearAndWeek));
             } catch (ParseException e) {
                 LOGGER.error("Failed to parse the year and week. Using now. {}", e);
                 cal.setTime(new Date());
             }
         } else {
             cal.setTime(new Date());
         }
         
         cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         return cal.getTime();
     }
 
 
 }
