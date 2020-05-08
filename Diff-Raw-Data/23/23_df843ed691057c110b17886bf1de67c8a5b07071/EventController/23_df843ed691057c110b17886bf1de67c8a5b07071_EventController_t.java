 package se.ryttargardskyrkan.rosette.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Order;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import se.ryttargardskyrkan.rosette.exception.NotFoundException;
 import se.ryttargardskyrkan.rosette.model.Event;
 
 @Controller
 public class EventController extends AbstractController {
 	@Autowired
 	private MongoTemplate mongoTemplate;
 
 	@RequestMapping(value = "events/{id}", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public Event getEvent(@PathVariable String id) {
 		checkPermission("events:read:" + id);
 		
 		Event event = mongoTemplate.findById(id, Event.class);
 		if (event == null) {
 			throw new NotFoundException();
 		}
 		return event;
 	}
 
 	@RequestMapping(value = "events", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public List<Event> getEvents(@RequestParam(required = false) String themeId, HttpServletResponse response) {
 		Query query = new Query();
 		query.sort().on("startTime", Order.ASCENDING);
 
 		if (themeId != null) {
 			query.addCriteria(Criteria.where("themeId").is(themeId));
 		}
 		
 		List<Event> eventsInDatabase = mongoTemplate.find(query, Event.class);
 		List<Event> events = new ArrayList<Event>();
 		if (eventsInDatabase != null) {
 			for (Event eventInDatabase : eventsInDatabase) {
 				if (isPermitted("events:read:" + eventInDatabase.getId())) {
 					events.add(eventInDatabase);
 				}
 			}
 		}
 
 		return events;
 	}
 
 	@RequestMapping(value = "events", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
 	@ResponseBody
 	public Event postEvent(@RequestBody Event event, HttpServletResponse response) {
 		checkPermission("events:create");
 		validate(event);
 
 		mongoTemplate.insert(event);
 
 		response.setStatus(HttpStatus.CREATED.value());
 		return event;
 	}
 
 	@RequestMapping(value = "events/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
 	public void putEvent(@PathVariable String id, @RequestBody Event event, HttpServletResponse response) {
 		checkPermission("events:update");
 		validate(event);
 
 		Update update = new Update();
        update.set("title", event.getTitle());
        update.set("startTime", event.getStartTime());
        update.set("endTime", event.getEndTime());
        update.set("description", event.getDescription());

        if (mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(id)), update, Event.class).getN() == 0) {
 			throw new NotFoundException();
 		}
 
 		response.setStatus(HttpStatus.OK.value());
 	}
 
 	@RequestMapping(value = "events/{id}", method = RequestMethod.DELETE, produces = "application/json")
 	public void deleteEvent(@PathVariable String id, HttpServletResponse response) {
 		checkPermission("events:delete:" + id);
 
 		Event deletedEvent = mongoTemplate.findAndRemove(Query.query(Criteria.where("id").is(id)), Event.class);
 		if (deletedEvent == null) {
 			throw new NotFoundException();
 		} else {
 			response.setStatus(HttpStatus.OK.value());
 		}
 	}
 }
