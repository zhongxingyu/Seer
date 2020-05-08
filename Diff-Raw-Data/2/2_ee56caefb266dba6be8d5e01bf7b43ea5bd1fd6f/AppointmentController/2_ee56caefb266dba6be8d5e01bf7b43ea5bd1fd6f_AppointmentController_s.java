 package com.teamsierra.csc191.api.controller;
 
 import com.teamsierra.csc191.api.exception.GenericException;
 import com.teamsierra.csc191.api.model.*;
 import com.teamsierra.csc191.api.repository.AppointmentRepository;
 import com.teamsierra.csc191.api.repository.AppointmentTypeRepository;
 import com.teamsierra.csc191.api.repository.StylistAvailabilityRepository;
 import com.teamsierra.csc191.api.resources.ResourceHandler;
 import com.teamsierra.csc191.api.util.Availability;
 import com.teamsierra.csc191.api.util.DateRange;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.hateoas.Resource;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
 
 /**
  * @author Alex Chernyak
  *
  * Controller managing appointment business logic
  *
  * Public methods:
  * getAppointments(HttpServletRequest)
  * gets all appointments relevant to the user type of a caller
  *
  * getAppointment(String, HttpServletRequest)
  * get a specific requestData specificed a appointmentID
  *
  * searchAppointments(Appointment, HttpServletRequest)
  * search appointments that match search fields
  *
  * Sample returned object:
  * {
  *   "links": [
  *     {
  *       "rel": "self",
  *       "href": "http://10.100.1.6:8080/salon/appointments/527ae382e4b00d9311cf63dd"
  *     },
  *     {
  *       "rel": "stylist",
  *       "href": "http://10.100.1.6:8080/salon/users/5273092cae29d92436b7f6f1"
  *     },
  *     {
  *       "rel": "client",
  *       "href": "http://10.100.1.6:8080/salon/users/52730965ae29d92436b7f6f2"
  *     },
  *     {
  *       "rel": "appointmentType",
  *       "href": "http://10.100.1.6:8080/salon/527add30e4b0afd24604fe2d"
  *     }
  *   ],
  *   "id": "527ae382e4b00d9311cf63dd",
  *   "clientID": "52730965ae29d92436b7f6f2",
  *   "stylistID": "5273092cae29d92436b7f6f1",
  *   "startTime": 1386460800000,
  *   "endTime": 1386374400000,
  *   "appointmentStatus": "APPROVED",
  *   "appointmentType": "haircut"
  * }
  *
  *
  * addAppointment(Appointment, HttpServletRequest)
  * add requestData to database
  *
  * editAppointment(Appointment, String, HttpServletRequest)
  * edit an exisit requestData
  * Sample object:
  * {
  *   "appointmentTypeID":"527b55f0e4b01219435d812c",
  *   "stylistID": "5273092cae29d92436b7f6f1",
  *   "startTime": "2013-11-20T15:00:00.000-08:00",
  *   "endTime": "2013-11-20T16:00:00.000-08:00",
  *   “appointmentStatus”: ”CANCELED”
  * }
  *
  */
 @Controller
 @RequestMapping(value = "/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
 public class AppointmentController extends GenericController
 {
     @Autowired
     private AppointmentRepository appRepository;
     @Autowired
     private AppointmentTypeRepository appTypeRepository;
     @Autowired
     private StylistAvailabilityRepository availRepository;
 
 
     @Autowired
     public AppointmentController(AppointmentRepository appRepository,
                                  AppointmentTypeRepository appTypeRepository,
                                  StylistAvailabilityRepository availRepository)
     {
         this.appRepository = appRepository;
         this.appTypeRepository = appTypeRepository;
         this.availRepository = availRepository;
     }
 
     /**
      * Get list of all appointments relevant to a caller
      * @return list of appointments
      * @throws Exception
      */
     @RequestMapping(method = RequestMethod.GET)
     @ResponseBody
     public ResponseEntity<List<Resource<Appointment>>> getAppointments(HttpServletRequest request) throws Exception
     {
         this.setRequestControllerState(request);
 
         Appointment findAppointment = new Appointment();
         List<Resource<Appointment>> appointmentResources = new ArrayList<>();
 
         // Set search constraints based on authType
         switch (this.authType)
         {
             case CLIENT:
                 findAppointment.setClientID(this.id);
             break;
 
             case STYLIST:
                 findAppointment.setStylistID(this.id);
             break;
 
             case ADMIN:
             break;
 
             default:
             break;
         }
 
         for(Appointment appointment: appRepository.findByCriteria(findAppointment))
             appointmentResources.add(ResourceHandler.createResource(appointment));
 
         if (appointmentResources.isEmpty())
             throw new GenericException("Search returned empty set", HttpStatus.NOT_FOUND, L);
 
         return new ResponseEntity<>(appointmentResources, HttpStatus.OK);
     }
 
 
     /**
      * Get a specific appointment
      * @param appointmentID
      * @return
      * @throws Exception
      */
     @RequestMapping(value = "/{appointmentID}",  method = RequestMethod.GET)
     @ResponseBody
     public ResponseEntity<Resource<Appointment>> getAppointment(@PathVariable String appointmentID,
                                                                 HttpServletRequest request) throws Exception
     {
         this.setRequestControllerState(request);
 
         Appointment targetAppointment = new Appointment();
         Resource<Appointment> appointmentResource;
         List<Appointment> appointments;
 
         // Set search constraints based on authType
         targetAppointment.setId(appointmentID);
         switch (this.authType)
         {
             case CLIENT:
                 targetAppointment.setClientID(id);
             break;
 
             case STYLIST:
                 targetAppointment.setStylistID(id);
             break;
 
             case ADMIN:
             break;
 
             default:
             break;
         }
 
         appointments = appRepository.findByCriteria(targetAppointment);
 
         if (appointments == null || appointments.isEmpty() || appointments.size() == 0)
             throw new GenericException("Appointment with supplied id cannot be found", HttpStatus.NOT_FOUND, L);
 
         appointmentResource = ResourceHandler.createResource(appointments.get(0));
 
         return new ResponseEntity<>(appointmentResource, HttpStatus.OK);
     }
 
     /**
      * Search for appointments by supplying a search criteria
      * @param criteria
      * @param request
      * @return
      * @throws Exception
      */
    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public ResponseEntity<List<Resource<Appointment>>> searchAppointments(@RequestParam String criteria,
                                                                           HttpServletRequest request) throws Exception
     {
         this.setRequestControllerState(request);
 
         ObjectMapper mapper = new ObjectMapper();
         Appointment searchFilters = mapper.readValue(criteria, Appointment.class);
 
         List<Resource<Appointment>> appointmentResources = new ArrayList<>();
         List<Appointment> appointments;
 
         // Set search constraints based on authType
         switch (this.authType)
         {
             case CLIENT:
                 searchFilters.setClientID(id);
             break;
 
             case STYLIST:
                 searchFilters.setStylistID(id);
             break;
 
             case ADMIN:
             break;
 
             default:
             break;
         }
 
         appointments = appRepository.findByCriteria(searchFilters);
         if (appointments.isEmpty())
             throw new GenericException("Search returned empty set", HttpStatus.NOT_FOUND, L);
 
         for(Appointment appointment: appointments)
             appointmentResources.add(ResourceHandler.createResource(appointment));
 
         return new ResponseEntity<>(appointmentResources, HttpStatus.OK);
     }
 
 
     /**
      * Add a single appointment
      * @param requestData
      * @return
      * @throws Exception
      */
     @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public ResponseEntity<Resource<GenericModel>> addAppointment(@RequestBody Appointment requestData,
                                                                  HttpServletRequest request) throws Exception
     {
         this.setRequestControllerState(request);
         User client, stylist;
         Appointment targetAppointment = new Appointment();
         List<Appointment> appointments;
         AppointmentType apType = new AppointmentType();
 
         StringBuilder errors = new StringBuilder("");
         switch (this.authType)
         {
             case CLIENT:
                 requestData.setClientID(id);
                 requestData.setAppointmentStatus(GenericModel.AppointmentStatus.NEW);
                 break;
 
             case STYLIST:
                 requestData.setStylistID(id);
                 break;
 
             case ADMIN:
             break;
 
             default:
             break;
         }
 
         // Check for missing request parameters
         if (requestData.getClientID() == null || requestData.getClientID().isEmpty())
             errors.append("clientID ");
         if (requestData.getStylistID() == null || requestData.getStylistID().isEmpty())
             errors.append("stylistID ");
         if (requestData.getStartTime() == null)
             errors.append("startTime ");
         if (requestData.getEndTime() == null)
             errors.append("endTime ");
         if (errors.length() > 0)
             throw new GenericException("Request is missing field(s): " + errors.toString(), HttpStatus.BAD_REQUEST, L);
 
         // Validate supplied appointment date/time parameters
         Date currentTimestamp = Calendar.getInstance().getTime();
         if (requestData.getStartTime().after(requestData.getEndTime()))
         {
             Date temp = requestData.getStartTime();
             requestData.setStartTime(requestData.getEndTime());
             requestData.setEndTime(temp);
         }
 
         // Check if dates are valid
         if (requestData.getStartTime().before(currentTimestamp) ||
             requestData.getEndTime().before(currentTimestamp))
             throw new GenericException("Appointments cannot be created in the past", HttpStatus.CONFLICT, L);
 
         // Validate stylist
         stylist = userRepository.findById(requestData.getStylistID());
         if (stylist == null || stylist.getType().compareTo(GenericModel.UserType.STYLIST) != 0 || !stylist.isActive())
             throw new GenericException("Invalid stylistID supplied", HttpStatus.NOT_FOUND, L);
         else
         {
             DateRange requestRange = new DateRange(requestData.getStartTime(), requestData.getEndTime());
             StylistAvailability stylistAvail = availRepository.findByDateRange(requestRange, requestData.getStylistID());
             if (stylistAvail == null)
                 throw new GenericException("Stylist is not available during the requested time",
                                            HttpStatus.CONFLICT, L);
             Availability avail = stylistAvail.getAvailability();
             if (avail == null || avail.isEmpty())
                 throw new GenericException("Stylist is not available during the requested time",
                                            HttpStatus.CONFLICT, L);
         }
 
         // Validate client
         client = userRepository.findById(requestData.getClientID());
         if (client == null || !client.isActive() || client.getType().compareTo(GenericModel.UserType.CLIENT) != 0)
             throw new GenericException("Invalid clientID supplied", HttpStatus.NOT_FOUND, L);
 
         // Validate requested appointment dates
         // These 4 properties define an appointment, other properties are for description and shouldn't be used
         targetAppointment.setStartTime(requestData.getStartTime());
         targetAppointment.setEndTime(requestData.getEndTime());
         targetAppointment.setStylistID(requestData.getStylistID());
         targetAppointment.setClientID(requestData.getClientID());
 
         appointments = appRepository.findByCriteria(targetAppointment);
         for (Appointment a: appointments)
         {
             switch (a.getAppointmentStatus())
             {
                 case APPROVED:
                 case NEW:
                     throw new GenericException("Given date/time range conflicts with existing appointments",
                                                HttpStatus.CONFLICT, L);
                 default:
             }
         }
 
         // Validate appointment type
         if (requestData.getAppointmentTypeID() == null || requestData.getAppointmentTypeID().isEmpty())
             throw new GenericException("A valid AppointmentTypeID is required",
                                        HttpStatus.BAD_REQUEST, L);
         else
         {
             String[] stylists = { requestData.getStylistID() };
 
             apType.setStylists(stylists);
             apType.setId(requestData.getAppointmentTypeID());
             List<AppointmentType> foundTypes = appTypeRepository.findByCriteria(apType);
             if (foundTypes == null || foundTypes.isEmpty())
                 throw new GenericException("Supplied stylist does not offer specified service. Invalid AppointmentTypeID",
                                            HttpStatus.NOT_FOUND, L);
 
             requestData.setAppointmentType(foundTypes.get(0).getAppointmentType());
         }
 
         // Force appointment status to NEW if it wasn't supplied
         if (requestData.getAppointmentStatus() == null)
             requestData.setAppointmentStatus(GenericModel.AppointmentStatus.NEW);
 
         // Appointment data is valid, insert into database
         GenericModel response = appRepository.insert(requestData);
         Resource<GenericModel> resource = new Resource<>(response);
         resource.add(linkTo(AppointmentController.class).slash(response).withSelfRel());
 
         return new ResponseEntity<>(resource, HttpStatus.CREATED);
     }
 
 
     /**
      * Update an existing requestData
      * @param requestData requestData related information
      * @param appointmentID
      * @return
      * @throws Exception
      */
     @RequestMapping(value = "/{appointmentID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<Resource<Appointment>> editAppointment(@RequestBody Appointment requestData,
                                                                   @PathVariable String appointmentID,
                                                                   HttpServletRequest request) throws Exception
     {
         this.setRequestControllerState(request);
 
         // Validate request data and set update constraints
         Appointment targetAppointment = new Appointment();
         switch (this.authType)
         {
             case CLIENT:
                 if (requestData.getAppointmentStatus() != null &&
                     requestData.getAppointmentStatus() != GenericModel.AppointmentStatus.CANCELED)
                 {
                     throw new GenericException("Clients can only change the appointment status to canceled",
                                                HttpStatus.BAD_REQUEST, L);
                 }
                 targetAppointment.setClientID(id);
                 break;
 
             case STYLIST:
                 targetAppointment.setStylistID(id);
                 break;
 
             case ADMIN:
                 break;
 
             default:
                 break;
         }
 
         // Notify caller of update constraints
         if (requestData.getStartTime() != null || requestData.getEndTime() != null)
             throw new GenericException("Start and end times cannot be changed. Cancel current appointment and make a new one",
                                        HttpStatus.BAD_REQUEST, L);
 
         if (requestData.getStylistID() != null || requestData.getClientID() != null)
             throw new GenericException("Assigned client or stylist cannot be changed",
                                        HttpStatus.BAD_REQUEST, L);
 
         // Attempt to find a single appointment with supplied id, constrained by caller authType
         targetAppointment.setId(appointmentID);
         List<Appointment> appointments = appRepository.findByCriteria(targetAppointment);
         if (appointments == null || appointments.isEmpty())
             throw new GenericException("Appointment with specified id was not found",
                                        HttpStatus.NOT_FOUND, L);
 
         targetAppointment = appointments.get(0);
 
         // Appointment is found, check if caller is authorized to modify it
         switch (targetAppointment.getAppointmentStatus())
         {
             case NEW:
                 break;
 
             case APPROVED:
                 break;
 
             case COMPLETED:
                 if (this.authType == GenericModel.UserType.STYLIST)
                     throw new GenericException("Stylist cannot update COMPLETED appointments",
                                                HttpStatus.CONFLICT, L);
                 break;
 
             default:
                 // Restrict client initiated updates for other appointment statuses
                 if (this.authType == GenericModel.UserType.CLIENT)
                     throw new GenericException("Client cannot update an appointment if it's not NEW or APPROVED",
                                                HttpStatus.CONFLICT, L);
         }
 
         // Update target appointment with parameters supplied by requestData
         if (requestData.getAppointmentStatus() != null &&
             requestData.getAppointmentStatus().compareTo(targetAppointment.getAppointmentStatus()) != 0)
         {
             // Prevent stylist from double-booking APPROVED appointments
             if (requestData.getAppointmentStatus().compareTo(GenericModel.AppointmentStatus.APPROVED) == 0)
             {
                 Appointment validateApprovedStatus = new Appointment();
                 validateApprovedStatus.setStylistID(targetAppointment.getStylistID());
                 validateApprovedStatus.setStartTime(targetAppointment.getStartTime());
                 validateApprovedStatus.setEndTime(targetAppointment.getEndTime());
                 validateApprovedStatus.setAppointmentStatus(GenericModel.AppointmentStatus.APPROVED);
 
                 List<Appointment> matches = appRepository.findByCriteria(validateApprovedStatus);
 
                 if (matches.size() > 0)
                 {
                     throw new GenericException("There's already an APPROVED appointment in the same time range. ",
                                                HttpStatus.CONFLICT, L);
                 }
             }
 
             targetAppointment.setAppointmentStatus(requestData.getAppointmentStatus());
         }
 
 
         appRepository.save(targetAppointment);
 
         Resource<Appointment> resource = new Resource<>(targetAppointment);
         resource.add(linkTo(AppointmentController.class).slash(targetAppointment).withSelfRel());
 
         return new ResponseEntity<>(resource, HttpStatus.ACCEPTED);
     }
 }
