 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.lmb97.web.action;
 
 import java.io.StringReader;
 import java.lang.Integer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import net.sourceforge.stripes.action.DontValidate;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.StreamingResolution;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationError;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import org.lmb97.data.Assistances;
 import org.lmb97.data.AssistancesExample;
 import org.lmb97.data.AssistancesMapper;
 import org.lmb97.data.EventTypes;
 import org.lmb97.data.EventTypesExample;
 import org.lmb97.data.EventTypesMapper;
 import org.lmb97.data.Events;
 import org.lmb97.data.EventsExample;
 import org.lmb97.data.EventsMapper;
 import org.lmb97.data.People;
 import org.lmb97.data.PeopleExample;
 import org.lmb97.data.PeopleMapper;
 import org.lmb97.data.Posts;
 import org.lmb97.data.PostsMapper;
 import org.lmb97.data.RelPostsPeople;
 import org.lmb97.data.RelPostsPeopleExample;
 import org.lmb97.data.RelPostsPeopleMapper;
 import org.lmb97.data.Seasons;
 import org.lmb97.data.SeasonsExample;
 import org.lmb97.data.SeasonsMapper;
 import org.lmb97.util.NormalDateTimeTypeConverter;
 
 /**
  *
  * @author javier
  */
 public class EventsActionBean extends AbstractActionBean implements ValidationErrorHandler {
     //First, the two pages this Action bean is going to use
 
     private static final String GRID = "/WEB-INF/jsp/events/GridEvents.jsp";
     private static final String FORM = "/WEB-INF/jsp/events/FormEvents.jsp";
     //Then the data the JSP are going to use, correctly adecuated in each case
     
     @ValidateNestedProperties({
         @Validate(field="eventType", required=true),
         @Validate(field="date", converter=NormalDateTimeTypeConverter.class, required=true),
         @Validate(field="season", required=true)
     })
     private Events event;
     private Posts post;
     
     @ValidateNestedProperties({
         @Validate(field="person", required=true),
         @Validate(field="arrival",converter=NormalDateTimeTypeConverter.class , required=true)
     })
     private List<Assistances> assistances;
     
     private List<Events> events;
     private List<EventTypes> eventTypes;
     private List<Seasons> seasons;
     private List<People> people;
     private List<RelPostsPeople> relPostsPeople;
     private Map<Integer, Integer> totalassistants;
     private Map<Integer, Integer> ontimeassistants;
     private boolean readonly;
     private String view;
     private int following;
     private int previous;
     //At last, all the entity mappers
     @SpringBean
     private AssistancesMapper assistancesMapper;
     @SpringBean
     private EventsMapper eventsMapper;
     @SpringBean
     private EventTypesMapper eventTypesMapper;
     @SpringBean
     private PeopleMapper peopleMapper;
     @SpringBean
     private SeasonsMapper seasonsMapper;
     @SpringBean
     private PostsMapper postsMapper;
     @SpringBean
     private RelPostsPeopleMapper relPostsPeopleMapper;
 
     @DefaultHandler
     @DontValidate
     public Resolution viewGrid() {
         EventsExample eventsExample = new EventsExample();
         EventTypesExample eventTypesExample = new EventTypesExample();
         SeasonsExample seasonsExample = new SeasonsExample();
 
         this.view = "Grid";
         this.readonly = true;
         eventsExample.createCriteria();
         this.events = eventsMapper.selectByExample(eventsExample);
 
         seasonsExample.createCriteria();
         this.seasons = seasonsMapper.selectByExample(seasonsExample);
 
         eventTypesExample.createCriteria();
         this.eventTypes = eventTypesMapper.selectByExample(eventTypesExample);
 
         createAssistancesStatistics();
 
         return new ForwardResolution(GRID);
     }
 
     @DontValidate
     public Resolution viewForm() {
         AssistancesExample assistancesExample = new AssistancesExample();
         EventTypesExample eventTypesExample = new EventTypesExample();
         PeopleExample peopleExample = new PeopleExample();
         SeasonsExample seasonsExample = new SeasonsExample();
 
         this.readonly = true;
         this.view = "Form";
         int id;
         if (null != context.getRequest().getParameter("id")) {
             id = new Integer(context.getRequest().getParameter("id")).intValue();
         } else {
             id = 1;
         }
 
         this.event = eventsMapper.selectByPrimaryKey(id);
         initFollowingAndPrevious(id);
 
         assistancesExample.createCriteria().andEventEqualTo(id);
         this.assistances = assistancesMapper.selectByExample(assistancesExample);
 
         peopleExample.createCriteria().andIdIn(getAssistantsIds());
         this.people = peopleMapper.selectByExample(peopleExample);
 
         seasonsExample.createCriteria().andIdEqualTo(this.event.getSeason());
         this.seasons = seasonsMapper.selectByExample(seasonsExample);
 
         eventTypesExample.createCriteria().andIdEqualTo(this.event.getEventType());
         this.eventTypes = eventTypesMapper.selectByExample(eventTypesExample);
 
         return new ForwardResolution(FORM);
     }
 
     @DontValidate
     public Resolution modifyForm() {
         AssistancesExample assistancesExample = new AssistancesExample();
         EventTypesExample eventTypesExample = new EventTypesExample();
         PeopleExample peopleExample = new PeopleExample();
         SeasonsExample seasonsExample = new SeasonsExample();
         RelPostsPeopleExample relPostsPeopleExample = new RelPostsPeopleExample();
 
 
         this.readonly = false;
         this.view = "Form";
         int id;
         if (null != context.getRequest().getParameter("id")) {
             id = new Integer(context.getRequest().getParameter("id")).intValue();
         } else {
             id = 1;
         }
         
         if(this.event==null)
             this.event = eventsMapper.selectByPrimaryKey(id);
         initFollowingAndPrevious(id);
 
         eventTypesExample.createCriteria();
         this.eventTypes = eventTypesMapper.selectByExample(eventTypesExample);
 
         this.post = postsMapper.selectByPrimaryKey(eventTypesMapper.selectByPrimaryKey(event.getEventType()).getPost());
 
         relPostsPeopleExample.or().
                 andPostEqualTo(this.post.getId()).
                 andJoinDateLessThanOrEqualTo(this.event.getDate()).
                 andOutDateGreaterThan(this.event.getDate());
         relPostsPeopleExample.or().
                 andPostEqualTo(this.post.getId()).
                 andJoinDateLessThanOrEqualTo(this.event.getDate()).
                 andOutDateIsNull();
 
         this.relPostsPeople = relPostsPeopleMapper.selectByExample(relPostsPeopleExample);
 
         seasonsExample.createCriteria();
         this.seasons = seasonsMapper.selectByExample(seasonsExample);
 
         List<Integer> posiblePeopleIds = getPeopleIds();
 
         if (!posiblePeopleIds.isEmpty()) {
             peopleExample.createCriteria().andIdIn(posiblePeopleIds);
             this.people = peopleMapper.selectByExample(peopleExample);
 
             assistancesExample.createCriteria().andEventEqualTo(id).andPersonIn(posiblePeopleIds);
         } else {
             this.people = null;
             assistancesExample.createCriteria().andEventIsNull().andEventIsNotNull();
         }
 
         this.assistances = assistancesMapper.selectByExample(assistancesExample);
 
         return new ForwardResolution(FORM);
     }
     
     public Resolution modifyingForm(){
        
         return modifyForm();
     }
     private void createAssistancesStatistics() {
         AssistancesExample assistancesExample = new AssistancesExample();
         this.ontimeassistants = new TreeMap<Integer, Integer>();
         this.totalassistants = new TreeMap<Integer, Integer>();
 
         for (Events item : this.events) {
             assistancesExample.createCriteria().
                     andEventEqualTo(item.getId()).
                     andArrivalLessThanOrEqualTo(item.getDate());
 
             this.ontimeassistants.put(
                     item.getId(),
                     assistancesMapper.countByExample(assistancesExample));
 
             assistancesExample.clear();
 
             assistancesExample.createCriteria().
                     andEventEqualTo(item.getId());
 
             this.totalassistants.put(
                     item.getId(),
                     assistancesMapper.countByExample(assistancesExample));
 
             assistancesExample.clear();
         }
     }
     
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
         StringBuilder message = new StringBuilder();
 
         for (List<ValidationError> fieldErrors : errors.values()) {
             for (ValidationError error : fieldErrors) {
                 message.append("<div class=\"error\">");
                 message.append(error.getMessage(getContext().getLocale()));
                 message.append("</div>");
             }
         }
 
         return new StreamingResolution("text/html", new StringReader(message.toString()));
     }
 
     private List<Integer> getPeopleIds() {
         List<Integer> peopleIds = new ArrayList<Integer>();
         for (RelPostsPeople item : this.relPostsPeople) {
             peopleIds.add(item.getPerson());
         }
         return peopleIds;
     }
 
     private List<Integer> getAssistantsIds() {
         List<Integer> assistantsIds = new ArrayList<Integer>();
         for (Assistances item : this.assistances) {
             assistantsIds.add(item.getPerson());
         }
         return assistantsIds;
     }
 
     private void initFollowingAndPrevious(int id) {
         EventsExample eventsExample = new EventsExample();
         boolean found = false;
 
         eventsExample.createCriteria();
         this.previous = 0;
         this.following = 0;
         for (Events item : eventsMapper.selectByExample(eventsExample)) {
             if (found) {
                 this.following = item.getId();
                 return;
             }
             if (item.getId() == id) {
                 found = true;
             }
             if (!found) {
                 this.previous = item.getId();
             }
         }
     }
 
     public int getFollowing() {
         return following;
     }
 
     public void setFollowing(int following) {
         this.following = following;
     }
 
     public int getPrevious() {
         return previous;
     }
 
     public void setPrevious(int previous) {
         this.previous = previous;
     }
 
     public String getView() {
         return view;
     }
 
     public boolean isReadonly() {
         return readonly;
     }
 
     public Map<Integer, Integer> getOntimeassistants() {
         return ontimeassistants;
     }
 
     public Map<Integer, Integer> getTotalassistants() {
         return totalassistants;
     }
 
     public List<EventTypes> getEventTypes() {
         return eventTypes;
     }
 
     public void setEventTypes(List<EventTypes> eventTypes) {
         this.eventTypes = eventTypes;
     }
 
     public List<Assistances> getAssistances() {
         return assistances;
     }
 
     public void setAssistances(List<Assistances> assistances) {
         this.assistances = assistances;
     }
 
     public Events getEvent() {
         return event;
     }
 
     public void setEvent(Events event) {
         this.event = event;
     }
 
     public List<Events> getEvents() {
         return events;
     }
 
     public void setEvents(List<Events> events) {
         this.events = events;
     }
 
     public List<People> getPeople() {
         return people;
     }
 
     public void setPeople(List<People> people) {
         this.people = people;
     }
 
     public List<Seasons> getSeasons() {
         return seasons;
     }
 
     public void setSeasons(List<Seasons> seasons) {
         this.seasons = seasons;
     }
 }
