 package org.yajug.users.api;
 
 import java.io.InputStream;
 import java.lang.reflect.Type;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang3.BooleanUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yajug.users.domain.Event;
 import org.yajug.users.domain.Flyer;
 import org.yajug.users.domain.Member;
 import org.yajug.users.service.DataException;
 import org.yajug.users.service.EventService;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gson.JsonObject;
 import com.google.gson.reflect.TypeToken;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataParam;
 
 /**
  * This controller expose the Event API over HTTP
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 @Path("event")
 public class EventController extends RestController {
 
 	private final static Logger logger = LoggerFactory.getLogger(EventController.class);
 	
 	/** The service instance that manages events */
 	@Inject private EventService eventService;
 	
 	/** Enables you to access the current servlet context within an action (it's thread safe) */
 	@Context private ServletContext servletContext;
 	
 	@Inject @Named("flyer.path") private String flyerPath;
 	
 	/**
 	 * Get the list of events either for the current or a particular year <br>
 	 * 
 	 * Example:<br>
 	 * {@code curl -i -H "Accept: application/json" http://localhost:8000/YajMember/api/event/list?current=true}
 	 * 
 	 * 
 	 * @param current if true we load the events of the current year
 	 * @param year if current is false, it defines which year we load the events for
 	 * @return a JSON representation of the events
 	 */
 	@GET
 	@Path("list")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String list(@QueryParam("current") Boolean current, @QueryParam("year") Integer year){
 		
 		String response = "";
 		Collection<Event> events = new ArrayList<>();
 		
 		try {
 			if(BooleanUtils.isTrue(current)){
 				year = Integer.valueOf(Calendar.getInstance().get(Calendar.YEAR));
 			}
 			if(year != null && year.intValue() > 1990 && year.intValue() < 2030) {	
 				//yes ! a strong validation with hard coded values, I assume that
 				
 				Collection<Event> allEvents = eventService.getAll();
 				
 				//we filter events based on the event year
 				final int yearFilter = year.intValue();
 				events = ImmutableList.copyOf(
 							Iterables.filter(
 									allEvents, 
 									new Predicate<Event> (){
 
 										private final SimpleDateFormat formater = new SimpleDateFormat("yyyy");
 										
 										@Override
 										public boolean apply(Event event) {
 											return (event != null 
 													&& event.getDate() != null 
 													&& Integer.valueOf(formater.format(event.getDate())) == yearFilter);
 										}
 									}
 								)
 							);
 			}
 			response = serializer.get().toJson(events);
 			
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		}
 		return response;
 	}
 	
 	/**
 	 * Get the list of years where there was events. The current year is always present.
 	 * @return a JSON representation of the list.
 	 */
 	@GET
 	@Path("getYears")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String getEventYears(){
 		String response = "";
 		
 		try {
 			//we use a TreeSet to avoid duplicate and get the years ordered
 			TreeSet<Integer> years = Sets.newTreeSet();
 			int currentYear = Integer.valueOf(Calendar.getInstance().get(Calendar.YEAR));
 			years.add(currentYear);
 			
 			Collection<Event> allEvents = eventService.getAll();
 			
 			final SimpleDateFormat formater = new SimpleDateFormat("yyyy");
 			for (Event event : allEvents) {
 				int year = Integer.valueOf(formater.format(event.getDate()));
 				if(!years.contains(year)){
 					years.add(year);
 				}
 			}
 			
 			response = serializer.get().toJson(years.descendingSet());
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	/**
 	 * Get an event from it's identifier<br>
 	 * 
 	 * Example:<br>
 	 * {@code curl -i -H "Accept: application/json"  http://localhost:8000/YajMember/api/event/getOne?id=1}
 	 * 
 	 * @param id
 	 * @return  a JSON representation of the event
 	 */
 	@GET
 	@Path("getOne")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String getOne(@QueryParam("id") Long id){
 		String response = "";
 		
 		try {
 			if (id == null || id.longValue() <= 0) {
 				throw new DataException("Unable to retrieve an event from a wrong id");
 			}
 			Event event = eventService.getOne(id);
 			response = serializer.get().toJson(event);
 			
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	/**
 	 * Add a new event<br>
 	 * 
 	 * Example:<br>
 	 * {@code curl -i -H "Accept: application/json" -X PUT -d "event={key:...}"  http://localhost:8000/YajMember/api/event/update}
 	 * 
 	 * @param event the event to add in JSON format (a parsing/mapping will be done)
 	 * @return a JSON object that contains the 'saved' property
 	 */
 	@PUT
 	@Path("add")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String add(@FormParam("event") String event){
 		return saveEvent(event);
 	}
 	
 	/**
 	 * Update an event<br>
 	 * 
 	 * Example:<br>
 	 * {@code curl -i -H "Accept: application/json" -X POST -d "event={key:...}"  http://localhost:8000/YajMember/api/event/update}
 	 * 
 	 * @param event the event to update in JSON format (a parsing/mapping will be done)
 	 * @return a JSON object that contains the 'saved' property
 	 */
 	@POST
 	@Path("update")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String update(@FormParam("event") String event){
 		return saveEvent(event);
 	}
 	
 	/**
 	 * Save an event
 	 * @param eventData
 	 * @return the json response 
 	 */
 	private String saveEvent(String eventData){
 		
 		JsonObject response = new JsonObject();
 		boolean saved = false;
 		
 		Event event = serializer.get().fromJson(eventData, Event.class);
 		try {
 			saved = this.eventService.save(event);
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response.addProperty("error", e.getLocalizedMessage());
 		} 
 		response.addProperty("saved", saved);
 		
 		return serializer.get().toJson(response);
 	}
 	
 	/**
 	 * Remove an event<br>
 	 * 
 	 * Example:<br>
 	 * {@code curl -i -H "Accept: application/json" -X DELETE  http://localhost:8000/YajMember/api/event/remove/1}
 	 * 
 	 * @param id the eventidentifier
 	 * @return a JSON object that contains the 'removed' property
 	 */
 	@DELETE
 	@Path("remove/{id}")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String remove(@PathParam("id") long id){
 		
 		JsonObject response = new JsonObject();
 		boolean removed = false;
 		
 		if (id > 0) {
 			try {
 				Event event = new Event();
 				event.setKey(id);
 				removed = eventService.remove(event);
 			} catch (DataException e) {
 				logger.error(e.getLocalizedMessage(), e);
 				response.addProperty("error", e.getLocalizedMessage());
 			} 
 		}
 		response.addProperty("removed", removed);
 		
 		return  serializer.get().toJson(response);
 	}
 	
 	/**
 	 * Update the event flyer's file. The method handles an HTTP file upload.
 	 * 
 	 * @param stream uploaded file data
 	 * @param contentDisposition file upload meta
 	 * @param bodyPart file upload meta
 	 * @param id the event identifier the flyer is linked to
 	 * @return  a JSON object that contains the 'saved' property
 	 */
 	@POST
 	@Path("/flyer/{id}")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	@Produces("text/plain; charset=UTF-8")
 	public String updateFlyer(
 			@FormDataParam("flyer") InputStream stream,
 			@FormDataParam("flyer") FormDataContentDisposition contentDisposition,
 			@FormDataParam("flyer") FormDataBodyPart bodyPart, 
 			@PathParam("id") long id) {
 		
 		JsonObject response = new JsonObject();
 		boolean saved = false;
 		
 		//check MIME TYPE
 		if(bodyPart == null || !bodyPart.getMediaType().isCompatible(new MediaType("image", "*"))){
 			response.addProperty("error", "Unknow or unsupported file type");
 		}
 		
 		try {
 			//get the flyer's event
 			Event event = eventService.getOne(id);
 			if(event != null){
 			
 				//create a Flyer instance based on context
 				final String flyerFullPath = servletContext.getRealPath(flyerPath);
 				Flyer flyer = new Flyer(flyerFullPath, event);
 				String format = bodyPart.getMediaType().getSubtype().toLowerCase();
 				
 				//and save the file
 				if(eventService.saveFlyer(stream, format, flyer)){
 					saved = true;
 					response.addProperty("flyer", flyerPath + flyer.getFile().getName());
 					response.addProperty("thumb", flyerPath + flyer.getThumbnail().getFile().getName());
 				}
 			}
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response.addProperty("error", serializeException(e));
 		} 
 		response.addProperty("saved", saved);
 		
 		return serializer.get().toJson(response);
 	}
 	
 	/**
 	 * Removes a flyer 
 	 * 
 	 * @param id the event identifier the flyer belongs to
 	 * @return  a JSON object that contains the 'removed' property
 	 */
 	@DELETE
 	@Path("/removeFlyer/{id}")
 	public String removeFlyer(@PathParam("id") long id){
 		JsonObject response = new JsonObject();
 		boolean removed = false;
 		
 		if(id > 0){
 			
 			try {
 				Event event = eventService.getOne(id);
 				if(event != null){
 					final String flyerFullPath = servletContext.getRealPath(flyerPath);
 					Flyer flyer = new Flyer(flyerFullPath, event);
 					
 					removed = eventService.removeFlyer(flyer);
 				}
 			} catch (DataException e) {
 				logger.error(e.getLocalizedMessage(), e);
 				response.addProperty("error", serializeException(e));
 			}
 		}
 		response.addProperty("removed", removed);
 		return serializer.get().toJson(response);
 	}
 	
 	/**
 	 * Update an the participants of an event<br>
 	 * 
 	 * Example:<br>
 	 * {@code curl -i -H "Accept: application/json" -X POST -d "registered=[1,...]&participant=[2,...]"  http://localhost:8000/YajMember/api/event/updateParticipant/12}
 	 * 
 	 * @param id the key of the event to update
 	 * @param registeredData JSON array of the registered member's ids
 	 * @param participantData JSON array of the participants member's ids
 	 * @return a JSON object that contains the 'saved' property
 	 */
 	@POST
 	@Path("updateParticipant/{id}")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String updateParticipant(
 			@PathParam("id") long id, 
			@FormParam("registrant") String registeredData, 
 			@FormParam("participant") String participantData){
 		
 		JsonObject response = new JsonObject();
 		boolean saved = false;
 		
 		if(id > 0){
 		
 			//unserialize the JSON ids to lists
 			Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
 			List<Long> registeredIds = serializer.get().fromJson(registeredData, listType);
 			List<Long> participantIds = serializer.get().fromJson(participantData, listType);
 		
 			//function used to map an id to a member
 			Function <Long, Member> idToMember = new Function<Long, Member>() {
 				@Override public Member apply(Long input) {
 					return new Member(input.longValue());
 				}
 			};
 			
 			//do the mapping
 			List<Member> registereds = Lists.transform(registeredIds, idToMember);
 			List<Member> participants = Lists.transform(participantIds, idToMember);
 			
 			try {
 				//create a bag instance
 				Event event = eventService.getOne(id);
 				if(event != null){
 					event.setParticipants(participants);
 					event.setRegistrants(registereds);
 					saved = eventService.save(event);
 				}
 			} catch (DataException e) {
 				logger.error(e.getLocalizedMessage(), e);
 				response.addProperty("error", serializeException(e));
 			} 
 		}
 		response.addProperty("saved", saved);
 		return serializer.get().toJson(response);
 	}
 }
