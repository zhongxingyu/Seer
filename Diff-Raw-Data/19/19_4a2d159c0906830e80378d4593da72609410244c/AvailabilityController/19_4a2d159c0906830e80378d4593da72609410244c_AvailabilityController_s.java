 package com.teamsierra.csc191.api.controller;
 
 import com.teamsierra.csc191.api.exception.GenericAvailabilityException;
 import com.teamsierra.csc191.api.model.Appointment;
 import com.teamsierra.csc191.api.model.GenericModel.AppointmentStatus;
 import com.teamsierra.csc191.api.model.StylistAvailability;
 import com.teamsierra.csc191.api.model.User;
 import com.teamsierra.csc191.api.repository.AppointmentRepository;
 import com.teamsierra.csc191.api.repository.StylistAvailabilityRepository;
 import com.teamsierra.csc191.api.repository.UserRepository;
 import com.teamsierra.csc191.api.resources.ResourceHandler;
 import com.teamsierra.csc191.api.util.Availability;
 import com.teamsierra.csc191.api.util.DateRange;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.hateoas.Resource;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.*;
 
 @Controller
 @RequestMapping("/availability")
 public class AvailabilityController extends GenericController
 {
 	private static final Log L = LogFactory.getLog(AvailabilityController.class);
 	
 	private UserRepository userRepo;
 	private StylistAvailabilityRepository sar;
 	private AppointmentRepository apptRepo;
 	
 	@Autowired
 	public AvailabilityController(UserRepository userRepo,
 								  StylistAvailabilityRepository stylistAvailRepo,
 								  AppointmentRepository apptRepo)
 	{
 		this.userRepo = userRepo;
 		this.sar = stylistAvailRepo;
 		this.apptRepo = apptRepo;
 	}
 	
 	/**
 	 * A method to get the current availability for all stylists within the given month
 	 * and year. The availability returned has the time of all approved appointments
 	 * removed. This is a subset of the stylist's full availability.
 	 * 
 	 * Usage: GET call to "/availability?year=Y&month=M".
 	 *  where M is the month param and Y is the year param as integer values.
 	 *  These values wrap around if they exceed their specific ranges. 
 	 * 
 	 * 	i.e. month = 12 would be equivalent to month = 0 and year++
 	 * 
 	 * Input:
      * 	-RequestParameters: month, year
      * 
      * Return: List<Resource<StylistAvailability>>. List may be empty, but
      * 	should always return a list.
      * 	
      * 	format - Json format of the resource with a link to each individual user and
      * 		the user's full set of availability.
      * 
      * 	the following is an example of what would be returned if there were two
      * 	stylists in the system, and both with availability in the range. Some
      * 	notes: 0L is a long value. there is only one stylistAvailabiltiy per
      * 	stylist or admin (denoted by stylistID and stylistID1).
      * { 
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "stylist",
      * 				"href": ".../users/{stylistID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{stylistID}"
      * 			}
      * 		]
      * 		"id": "{stylistAvailabilityID}",
      * 		"stylistID": "{stylistID}",
      * 		"availability":	
      * 		[
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			},
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			}
      * 		]
      * 	},
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "stylist",
      * 				"href": ".../users/{stylistID1}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{stylistID1}"
      * 			}
      * 		]
      * 		"id": "{stylistAvailabilityID1}",
      * 		"stylistID": "{stylistID1}",
      * 		"availability":	
      * 		[
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			},
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			}
      * 		]
      *	}
      * }
 	 * 
 	 * @param month integer in the range 0-11, values outside of this range wrap around.
 	 * @param year integer value for the year
 	 * @return a list of the current available times for all of the stylists along with
 	 * their corresponding userID for the entire month.
 	 */
 	@RequestMapping(method = RequestMethod.GET, params = {"month", "year"}, produces = MediaType.APPLICATION_JSON_VALUE)
 	public ResponseEntity<List<Resource<StylistAvailability>>> getMonthAvailability(@RequestParam int month,
 																	 		   		@RequestParam int year)
 	{
 		Calendar cal = new GregorianCalendar();
 		cal.set(Calendar.YEAR, year);
 		cal.set(Calendar.MONTH, month);
 		
 		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));		
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		Date sDate = cal.getTime();
 		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
 		cal.set(Calendar.HOUR_OF_DAY, 23);
 		cal.set(Calendar.MINUTE, 59);
 		cal.set(Calendar.SECOND, 59);
 		cal.set(Calendar.MILLISECOND, 999);
 		Date eDate = cal.getTime();
 		DateRange dr = new DateRange(sDate, eDate);
 		
 		List<Resource<StylistAvailability>> returnList = getAvailForCalendar(dr);
 		
 		return new ResponseEntity<List<Resource<StylistAvailability>>>(returnList, HttpStatus.OK);
 	}
 	
 	/**
 	 * A method to get the current availability for all stylists for a specific day.
 	 * The availability returned has the time of all approved appointments
 	 * removed.
 	 * 
 	 * Usage: GET call to "/availability?year=Y&month=M&day=D".
 	 * 	where D is the day param, M is the month param, and Y is the year param as
 	 *  integer values. These values wrap around if they exceed their specific
 	 *  ranges.
 	 * 
 	 * i.e. month = 12 would be equivalent to month = 0 and year++
 	 * 
 	 * Input:
      * 	-RequestParameters: day, month, year
      * 
      * Return: List<Resource<StylistAvailability>>. List may be empty, but 
      * 	should always return a List.
      * 	
      * 	format - Json format of the resource with a link to each individual user and
      * 		the user's full set of availability.
      * 
      * 	the following is an example of what would be returned if there were two
      * 	stylists in the system, and both with availability in the range. Some
      * 	notes: 0L is a long value. there is only one stylistAvailabiltiy per
      * 	stylist or admin (denoted by stylistID and stylistID1).
      * { 
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "stylist",
      * 				"href": ".../users/{stylistID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{stylistID}"
      * 			}
      * 		]
      * 		"id": "{stylistAvailabilityID}",
      * 		"stylistID": "{stylistID}",
      * 		"availability":	
      * 		[
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			},
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			}
      * 		]
      * 	},
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "stylist",
      * 				"href": ".../users/{stylistID1}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{stylistID1}"
      * 			}
      * 		]
      * 		"id": "{stylistAvailabilityID1}",
      * 		"stylistID": "{stylistID1}",
      * 		"availability":	
      * 		[
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			},
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			}
      * 		]
      *	}
      * }
 	 * 
 	 * @param day integer value for the day of the month
 	 * @param month integer in the range 0-11, values outside of this range wrap around.
 	 * @param year integer value for the year
 	 * @return a list of the current available times for all of the stylists along with
 	 * their corresponding userID for a specific day.
 	 */
 	@RequestMapping(method = RequestMethod.GET, params = {"day", "month", "year"}, produces = MediaType.APPLICATION_JSON_VALUE)
 	public ResponseEntity<List<Resource<StylistAvailability>>> getDayAvailability(@RequestParam int day,
 																				  @RequestParam int month,
 																				  @RequestParam int year)
 	{
 		Calendar cal = new GregorianCalendar();
 		cal.set(Calendar.YEAR, year);
 		cal.set(Calendar.MONTH, month);
 		cal.set(Calendar.DAY_OF_MONTH, day);
 		
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		Date sDate = cal.getTime();
 		cal.set(Calendar.HOUR_OF_DAY, 23);
 		cal.set(Calendar.MINUTE, 59);
 		cal.set(Calendar.SECOND, 59);
 		cal.set(Calendar.MILLISECOND, 999);
 		Date eDate = cal.getTime();
 		DateRange dr = new DateRange(sDate, eDate);
 		
 		List<Resource<StylistAvailability>> returnList = getAvailForCalendar(dr);
 		
 		return new ResponseEntity<List<Resource<StylistAvailability>>>(returnList, HttpStatus.OK);
 	}
 	
 	/**
 	 * Retrieves a specific stylist's full set of availability without any time
 	 * removed.
 	 * 
 	 * Usage: GET call to /availability/{userID}.
      * 	Client - will throw an exception
      * 	Stylist - will throw an exception if trying to access someone else's
      * 		availability.
      * 	Admin - no other case where an exception will be thrown.
      * 
      * Input:
      * 	-PathVariable: userID
      * 	-Headers: authType, authToken
      * 
      * Return: Resource<StylistAvailability>. will throw an exception if the
      * 	specified availability cannot be found.
      * 	
      * 	format - Json format of the resource with a link to the user and
      * 		the user's full set of availability.
      * 
      * 	the following is an example of what would be returned. Some
      * 	notes: 0L is a long value. there is only one stylistAvailabiltiy per
      * 	stylist or admin (denoted by stylistID and stylistID1).
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "stylist",
      * 				"href": ".../users/{stylistID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{stylistID}"
      * 			}
      * 		]
      * 		"id": "{stylistAvailabilityID}",
      * 		"stylistID": "{stylistID}",
      * 		"availability":	
      * 		[
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			},
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			}
      * 		]
      * 	}
 	 * 
 	 * @param stylistID
 	 * @param request
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/{stylistID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public ResponseEntity<Resource<StylistAvailability>> getUserAvailability(@PathVariable String stylistID,
 															 				 HttpServletRequest request) throws GenericAvailabilityException
 	{
 		try
     	{
     		this.setRequestControllerState(request);
     	}
     	catch(Exception e)
     	{
     		throw new GenericAvailabilityException(e.getMessage(),
     				HttpStatus.BAD_REQUEST);
     	}
 		
 		StylistAvailability sa = sar.findByStylistID(stylistID);
 		if(sa == null)
 		{
 			throw new GenericAvailabilityException("Unable to find stylist availability in the repository.",
 					HttpStatus.NOT_FOUND);
 		}
 		
 		return new ResponseEntity<Resource<StylistAvailability>>(ResourceHandler.createResource(sa), HttpStatus.OK);
 	}
 	
 	/**
 	 * Updates a stylist's availability. Completely overwrites the existing
 	 * availability for the specified stylist.
 	 * 
 	 * Usage: PUT call to /availability/{stylistID}.
      * 	Client - will throw an exception
      * 	Stylist - will throw an exception if trying to access someone else's
      * 		availability.
      * 	Admin - no other case where an exception will be thrown.
      * 
      * Input:
      * 	-PathVariable: stylistID
      * 	-Headers: authType, authToken
      * 	-RequestBody: a Json formatted StylistAvailability model which requires
      * 	 	the availability field to not be null. 0L represents a long value.
      * 
      * 		example:
      * 		{
      * 			"availability":	
      * 			[
      * 				{
      * 					"startDate": 0L,
      * 					"endDate": 0L
      * 				},
      * 				{
      * 					"startDate": 0L,
      * 					"endDate": 0L
      * 				}
      * 			]
      * 		}
      * 
      * Return: Resource<StylistAvailability>. will throw an exception if the
      * 	specified availability cannot be found.
      * 	
      * 	format - Json format of the resource with a link to the user and
      * 		the user's full set of availability.
      * 
      * 	the following is an example of what would be returned. Some
      * 	notes: 0L is a long value. there is only one stylistAvailabiltiy per
      * 	stylist or admin (denoted by stylistID and stylistID1).
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "stylist",
      * 				"href": ".../users/{stylistID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{stylistID}"
      * 			}
      * 		]
      * 		"id": "{stylistAvailabilityID}",
      * 		"stylistID": "{stylistID}",
      * 		"availability":	
      * 		[
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			},
      * 			{
      * 				"startDate": 0L,
      * 				"endDate": 0L
      * 			}
      * 		]
      * 	}
 	 * 
 	 * @param stylistID
 	 * @param stylistAvailability
 	 * @param request
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/{stylistID}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
 	public ResponseEntity<Resource<StylistAvailability>> updateAvailability(@PathVariable String stylistID,
 								   							@RequestBody StylistAvailability stylistAvailability,
 								   							HttpServletRequest request) throws Exception
 	{
 		try
     	{
     		this.setRequestControllerState(request);
     	}
     	catch(Exception e)
     	{
     		throw new GenericAvailabilityException(e.getMessage(),
     				HttpStatus.BAD_REQUEST);
     	}
 		
 		if(stylistAvailability.getAvailability() == null)
 		{
 			throw new GenericAvailabilityException("Invalid request. The availability cannot be null.",
 					HttpStatus.BAD_REQUEST);
 		}
 		
 		switch(authType)
 		{
 			case CLIENT: throw new GenericAvailabilityException("Clients cannot modify stylist availability.",
 					HttpStatus.BAD_REQUEST);
 			case STYLIST: User user = userRepo.findByToken(authToken);
 						  if(user == null)
 						  {
 							  throw new GenericAvailabilityException("Unable to find your user credentials in the database.",
 									 HttpStatus.FORBIDDEN);
 						  }
 						  
 						  if(!stylistID.equals(user.getId()))
 						  {
 							  throw new GenericAvailabilityException("Stylists can only edit their own availability.",
 									  HttpStatus.FORBIDDEN);
 						  }
 						  break;
 			case ADMIN: 
 						break;
 			default:
 		}
 		
 		StylistAvailability sa = sar.findByStylistID(stylistID);
 		if(sa == null)
 		{
 			throw new GenericAvailabilityException("Unable to find stylist availability in the repository.",
 					HttpStatus.NOT_FOUND);
 		}

		/* the following is done to ensure a consistent state of
		 * no two overlapping DateRanges in the availability.
		 */
		Availability avail = new Availability();
		avail.addAll(stylistAvailability.getAvailability());
		
		sa.setAvailability(avail);
 		
 		sar.save(sa);
 		
 		return new ResponseEntity<Resource<StylistAvailability>>(ResourceHandler.createResource(sa), HttpStatus.ACCEPTED);
 	}
 	
 	private List<Resource<StylistAvailability>> getAvailForCalendar(DateRange dr)
 	{
 		List<StylistAvailability> list = sar.findByDateRange(dr);
 		List<Appointment> apptList;
 		Appointment appt;
 		DateRange dateRange;
 		for(StylistAvailability sa : list)
 		{
 			appt = new Appointment();
 			appt.setStylistID(sa.getStylistID());
 			appt.setStartTime(dr.getStartDate());
 			appt.setEndTime(dr.getEndDate());
 			appt.setAppointmentStatus(AppointmentStatus.APPROVED);
 			
 			apptList = apptRepo.findByCriteria(appt);
 			
 			for(Appointment appointment : apptList)
 			{
 				dateRange = new DateRange(appointment.getStartTime(), appointment.getEndTime());
 				sa.getAvailability().remove(dateRange);
 			}
 		}
 		
 		List<Resource<StylistAvailability>> returnList = new ArrayList<Resource<StylistAvailability>>();
 		for(StylistAvailability sa : list)
 		{
 			returnList.add(ResourceHandler.createResource(sa));
 		}
 		
 		return returnList;
 	}
 	
 	//@ExceptionHandler(Exception.class)
     public ResponseEntity<String> handleAllExceptions(Exception e) throws GenericAvailabilityException
     {
 		if(!(e instanceof GenericAvailabilityException))
 		{
 			throw new GenericAvailabilityException(e.getMessage(),
 					HttpStatus.BAD_REQUEST);
 		}
 		else
 		{
 			throw (GenericAvailabilityException) e;
 		}
     }
 }
