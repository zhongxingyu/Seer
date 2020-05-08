 package org.karmaexchange.resources;
 
 import static com.google.common.base.Preconditions.checkState;
 import static org.karmaexchange.util.OfyService.ofy;
 import static org.karmaexchange.util.UserService.getCurrentUserKey;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.karmaexchange.dao.Event;
 import org.karmaexchange.dao.BaseDao;
import org.karmaexchange.dao.Event.ParticipantType;
 import org.karmaexchange.dao.Event.UpsertParticipantTxn;
 import org.karmaexchange.dao.Event.DeleteParticipantTxn;
 import org.karmaexchange.dao.KeyWrapper;
 import org.karmaexchange.dao.User;
 import org.karmaexchange.resources.msg.EventParticipantView;
 import org.karmaexchange.resources.msg.EventSearchView;
 import org.karmaexchange.resources.msg.ListResponseMsg;
 import org.karmaexchange.resources.msg.ListResponseMsg.PagingInfo;
 
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.QueryResultIterator;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.cmd.Query;
 
 @Path("/event")
 public class EventResource extends BaseDaoResource<Event> {
 
   public static final String START_TIME_PARAM = "start_time";
   public static final String SEARCH_TYPE_PARAM = "type";
 
   public static final String DEFAULT_NUM_PARTICIPANT_VIEW_RESULTS = 10 + "";
 
   public enum EventSearchType {
     UPCOMING,
     PAST
   }
 
   @Override
   protected Class<Event> getResourceClass() {
     return Event.class;
   }
 
   @GET
   @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public ListResponseMsg<EventSearchView> getResources(
       @QueryParam(PagingInfo.AFTER_CURSOR_PARAM) String afterCursorStr,
       @QueryParam(PagingInfo.LIMIT_PARAM) @DefaultValue(DEFAULT_NUM_SEARCH_RESULTS) int limit,
       @QueryParam(START_TIME_PARAM) Long startTimeValue) {
     return eventSearch(afterCursorStr, limit, startTimeValue, uriInfo.getAbsolutePath(),
       EventSearchType.UPCOMING, Maps.<String, Object>newHashMap());
   }
 
   // TODO(avaliani): take one. Need to re-factor this a bunch!!!
   public static ListResponseMsg<EventSearchView> eventSearch(String afterCursorStr, int limit,
       Long startTimeValue, URI baseUri, EventSearchType searchType, Map<String, Object> filters) {
     filters = Maps.newHashMap(filters);
     Date startTime = (startTimeValue == null) ? new Date() : new Date(startTimeValue);
     String resultOrder;
     if (searchType == null) {
       searchType = EventSearchType.UPCOMING;
     }
     if (searchType == EventSearchType.UPCOMING) {
       resultOrder = "startTime";
       filters.put("startTime >=", startTime);
     } else {
       resultOrder = "-startTime";
       filters.put("startTime <", startTime);
     }
 
     // Query one more than the limit to see if we need to provide a link to additional results.
     Query<Event> query = ofy().load().type(Event.class)
         .order(resultOrder)
         .limit(limit + 1);
     for (Map.Entry<String, Object> entry : filters.entrySet()) {
       query = query.filter(entry.getKey(), entry.getValue());
     }
     if (afterCursorStr != null) {
       query = query.startAt(Cursor.fromWebSafeString(afterCursorStr));
     }
 
     QueryResultIterator<Event> queryIter = query.iterator();
     List<Event> searchResults = Lists.newArrayList(Iterators.limit(queryIter, limit));
     Cursor afterCursor = queryIter.getCursor();
     BaseDao.processLoadResults(searchResults);
 
     Map<String, Object> paginationParams = Maps.newHashMap();
     paginationParams.put(START_TIME_PARAM, startTime.getTime());
     paginationParams.put(SEARCH_TYPE_PARAM, searchType);
 
     return ListResponseMsg.create(
       EventSearchView.create(searchResults),
       PagingInfo.create(afterCursor, limit, queryIter.hasNext(), baseUri, paginationParams));
   }
 
   @Path("{resource}/participants/{participant_type}")
   @GET
   @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public ListResponseMsg<EventParticipantView> getParticipants(
       @PathParam("resource") String eventKeyStr,
       @PathParam("participant_type") ParticipantType participantType,
       @QueryParam(PagingInfo.OFFSET_PARAM) @DefaultValue("0") int offset,
       @QueryParam(PagingInfo.LIMIT_PARAM)
       @DefaultValue(DEFAULT_NUM_PARTICIPANT_VIEW_RESULTS) int limit) {
     Event event = getResourceObj(eventKeyStr);
     checkState((limit >= 0) && (offset >= 0), "limit and offset must be non-negative");
     List<KeyWrapper<User>> participants = event.getParticipants(participantType);
     List<KeyWrapper<User>> offsettedResult =
         PagingInfo.createOffsettedResult(participants, offset, limit);
     return ListResponseMsg.create(
       EventParticipantView.get(offsettedResult),
       PagingInfo.create(offset, limit, participants.size(), uriInfo.getAbsolutePath(), null));
   }
 
   @Path("{resource}/participants/{participant_type}")
   @POST
   @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public Response upsertParticipants(
       @PathParam("resource") String eventKeyStr,
       @PathParam("participant_type") ParticipantType participantType,
       @QueryParam("user") String userKeyStr) {
     Key<User> userKey = (userKeyStr == null) ? getCurrentUserKey() : Key.<User>create(userKeyStr);
     ofy().transact(new UpsertParticipantTxn(
       Key.<Event>create(eventKeyStr), userKey, participantType));
     return Response.ok().build();
   }
 
   @Path("{resource}/participants")
   @DELETE
   public void deleteParticipants(
       @PathParam("resource") String eventKeyStr,
       @QueryParam("user") String userKeyStr) {
     Key<User> userKey = (userKeyStr == null) ? getCurrentUserKey() : Key.<User>create(userKeyStr);
     ofy().transact(new DeleteParticipantTxn(Key.<Event>create(eventKeyStr), userKey));
   }
 }
