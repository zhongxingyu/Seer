 package uk.frequency.glance.server.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.hibernate.ObjectNotFoundException;
 
 import uk.frequency.glance.server.business.EventBL;
 import uk.frequency.glance.server.business.TraceBL;
 import uk.frequency.glance.server.business.UserBL;
import uk.frequency.glance.server.business.logic.PresentationUtil;
 import uk.frequency.glance.server.business.logic.event.EventScoreLogic;
 import uk.frequency.glance.server.business.logic.waveline.WavelineDataAdapter;
 import uk.frequency.glance.server.business.remote.EventDataFinder;
 import uk.frequency.glance.server.data_access.util.HibernateConfig;
 import uk.frequency.glance.server.model.Comment;
 import uk.frequency.glance.server.model.Location;
 import uk.frequency.glance.server.model.event.Event;
 import uk.frequency.glance.server.model.event.ListenEvent;
 import uk.frequency.glance.server.model.event.MoveEvent;
 import uk.frequency.glance.server.model.event.StayEvent;
 import uk.frequency.glance.server.model.event.TellEvent;
 import uk.frequency.glance.server.model.trace.PositionTrace;
 import uk.frequency.glance.server.model.user.User;
 import uk.frequency.glance.server.model.user.UserProfile;
 import uk.frequency.glance.server.transfer.event.EventDTO;
 import uk.frequency.glance.server.transfer.event.EventViewDTO;
 import uk.frequency.glance.server.transfer.event.ListenEventDTO;
 import uk.frequency.glance.server.transfer.event.MoveEventDTO;
 import uk.frequency.glance.server.transfer.event.StayEventDTO;
 import uk.frequency.glance.server.transfer.event.TellEventDTO;
 import uk.frequency.glance.server.transfer.user.UserDTO;
 

 @Path("/event")
 public class EventSL extends GenericSL<Event, EventDTO>{
 
 	EventBL eventBl;
 	UserBL userBl;
 	TraceBL traceBl;
 
 	public EventSL() {
 		super(new EventBL());
 		eventBl = (EventBL)business;
 		userBl = new UserBL();
 		traceBl = new TraceBL();
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/user-{id}")
 	public Response findByAuthor(@PathParam("id") long userId) {
 		try {
 			List<Event> list = eventBl.findByUser(userId);
 			List<EventDTO> dto = toDTO(list);
 			eventBl.flush();
 			return Response.ok(dto).build();
 		} catch (ObjectNotFoundException e) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/user-{id}/{start}to{end}")
 	public Response findByUserAndTimeRange(
 			@PathParam("id") long userId,
 			@PathParam("start") long start,
 			@PathParam("end") long end) {
 		try {
 		
 			List<Event> list = eventBl.findByTimeRange(userId, new Date(start), new Date(end)); 
 			List<EventDTO> dto = toDTO(list);
 			eventBl.flush();
 			return Response.ok(dto).build();
 		
 		} catch (ObjectNotFoundException e) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 	}
 	
 	@GET
 	@Path("/user-{id}/created_after-{time}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<EventDTO> findCreatedAfter(
 			@PathParam("id") long userId,
 			@PathParam("time") long time) {
 		try{
 			List<Event> entity = eventBl.findCreatedAfter(userId, new Date(time));
 			List<EventDTO> dto = toDTO(entity);
 			eventBl.flush();
 			return dto;
 		}catch(ObjectNotFoundException e){
 			throw new WebApplicationException(Status.NOT_FOUND);
 		}
 	}
 	
 	@GET
 	@Path("/user-{id}/waveline")
 	@Produces("image/png")
 	public Response generateWaveline(
 			@PathParam("id") long userId,
 			@QueryParam("width") int width,
 			@QueryParam("height") int height){
 		try {
 			byte[] out = eventBl.generateWaveline(userId, width, height);
 			return Response.ok(out).build();
 		} catch (IOException e) {
 			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 	
 	@GET
 	@Path("/user-{id}/waveline-{begin}to{end}")
 	@Produces("image/png")
 	public Response generateWaveline(
 			@PathParam("id") long userId,
 			@PathParam("begin") long begin,
 			@PathParam("end") long end,
 			@QueryParam("width") int width,
 			@QueryParam("height") int height){
 		try {
 			byte[] out = eventBl.generateWaveline(userId, begin, end, width, height);
 			return Response.ok(out).build();
 		} catch (IOException e) {
 			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 	
 	@GET
 	@Path("/user-{id}/eventFeedPage")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response eventFeedPage(
 		@PathParam("id") long userId,
 		@QueryParam("wl_width") long width,
 		@QueryParam("wl_height") long height){
 		
 		try{
 			
 			List<Event> events = eventBl.findRecent(userId, 50);
 			Collections.reverse(events); //TODO use them in desc order so we can use limit in SQL
 			
 			String waveUrl = uriInfo.getBaseUriBuilder()
 					.path("event/user-{id}/waveline")
 					.queryParam("width", width)
 					.queryParam("height", height)
 					.build(userId).toString();
 			
 			return eventFeedPage(userId, events, waveUrl);
 			
 		}catch (ObjectNotFoundException e){
 			return Response.status(Status.NOT_FOUND).build();
 		}
 	}
 	
 	@GET
 	@Path("/user-{id}/eventFeedPage-{start}to{end}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response eventFeedPage(
 		@PathParam("id") long userId,
 		@PathParam("start") long start,
 		@PathParam("end") long end,
 		@QueryParam("wl_width") long width,
 		@QueryParam("wl_height") long height){
 		
 		try{
 			
 			List<Event> events = eventBl.findByTimeRange(userId, new Date(start), new Date(end));
 			
 			String waveUrl = uriInfo.getBaseUriBuilder()
 					.path("event/user-{id}/waveline-{begin}to{end}")
 					.queryParam("width", width)
 					.queryParam("height", height)
 					.build(userId, start, end).toString();
 			
 			return eventFeedPage(userId, events, waveUrl);
 		
 		}catch (ObjectNotFoundException e){
 			return Response.status(Status.NOT_FOUND).build();
 		}
 	}
 	
 	private Response eventFeedPage(long userId, List<Event> events, String waveUrl){
 		User user = userBl.findById(userId);
 	
 		WavelineDataAdapter adapter = new WavelineDataAdapter();
 		adapter.buildLayers(events);
 		int[] index = adapter.getIndex();
 		
 		List<EventViewDTO> eventDtos = new ArrayList<EventViewDTO>();
 		for(Event event : events){
 			eventDtos.add(EventViewDTO.from(event));
 		}
 		
 		UserDTO dto = new UserDTO();
 		dto.id = userId;
 		if(user.getProfileHistory() != null && !user.getProfileHistory().isEmpty()){
 			UserProfile recentProfile = user.getProfileHistory().get(0);
 			dto.profile = new UserProfile();
 			dto.profile.setFirstName(recentProfile.getFirstName());
 			dto.profile.setMiddleName(recentProfile.getMiddleName());
 			dto.profile.setFullName(recentProfile.getFullName());
 			dto.profile.setImageUrl(recentProfile.getImageUrl());
 		}
 		
 		
 		PositionTrace trace = traceBl.findMostRecentPositionTrace(userId);
 		if(trace != null){
 			Location location = new EventDataFinder(trace.getPosition()).getLocation();
			dto.recentLocationName = location.getName();
			dto.recentLocationTime = PresentationUtil.timeText(trace.getTime());
 		}
 		
 		dto.wavelineIndex = index;
 		dto.wavelineImageUrl = waveUrl;
 		dto.eventViews = eventDtos;
 		
 		return Response.ok(dto).build();
 	}
 	
 	@Override
 	protected EventDTO toDTO(Event event){
 		return staticToDTO(event);
 	}
 	
 	@Override
 	protected Event fromDTO(EventDTO dto) {
 		return staticFromDTO(dto);
 	}
 	
 	public static EventDTO staticToDTO(Event event){
 		
 		EventDTO dto;
 		if(event instanceof StayEvent){
 			StayEvent stay = (StayEvent)event;
 			StayEventDTO stayDto = new StayEventDTO();
 			stayDto.location = stay.getLocation();
 			dto = stayDto;
 		}else if(event instanceof MoveEvent){
 			MoveEvent move = (MoveEvent)event;
 			MoveEventDTO moveDto = new MoveEventDTO();
 			moveDto.startLocation = move.getStartLocation();
 			moveDto.endLocation = move.getEndLocation();
 			moveDto.trail = HibernateConfig.initializeAndUnproxy(move.getTrail());
 			dto = moveDto;
 		}else if(event instanceof TellEvent){
 			TellEvent tell = (TellEvent)event;
 			TellEventDTO tellDto = new TellEventDTO();
 			tellDto.location = tell.getLocation();
 			tellDto.text = tell.getText();
 			dto = tellDto;
 		}else if(event instanceof ListenEvent){
 			ListenEvent listen = (ListenEvent)event;
 			ListenEventDTO listenDto = new ListenEventDTO();
 			listenDto.songMetadata = listen.getSongMetadata();
 			dto = listenDto;
 		}else{
 			throw new AssertionError();
 		}
 		
 		dto.initFromEntity(event);
 		dto.userId = event.getUser().getId();
 		dto.type = event.getType();
 		dto.startTime = event.getStartTime().getTime();
 		if(event.getEndTime() != null){
 			dto.endTime = event.getEndTime().getTime();
 		}
 		
 		if(event.getScore() == null){
 			event.setScore(EventScoreLogic.assignScore(event));
 		}else{
 			dto.score = event.getScore();
 		}
 		
 		dto.media = event.getMedia();
 		
 		if(event.getParticipants() != null){
 			List<Long> participantIds = new ArrayList<Long>();
 			for(User participant : event.getParticipants()){
 				participantIds.add(participant.getId());
 			}
 			dto.participantIds = participantIds;
 		}
 		
 		if(event.getComments() != null){
 			List<Long> commentIds = new ArrayList<Long>();
 			for(Comment comment : event.getComments()){
 				commentIds.add(comment.getId());
 			}
 			dto.commentIds = commentIds;
 		}
 		
 		return dto;
 	}
 	
 	public static Event staticFromDTO(EventDTO dto) {
 
 		User user = new User();
 		user.setId(dto.userId);
 		
 		Event event;
 		if(dto instanceof StayEventDTO){
 			StayEventDTO stayDto = (StayEventDTO)dto;
 			StayEvent stay = new StayEvent();
 			stay.setLocation(stayDto.location);
 			event = stay;
 		}else if(dto instanceof MoveEventDTO){
 			MoveEventDTO moveDto = (MoveEventDTO)dto;
 			MoveEvent move = new MoveEvent();
 			move.setStartLocation(moveDto.startLocation);
 			move.setEndLocation(moveDto.endLocation);
 			move.setTrail(moveDto.trail);
 			event = move;
 		}else if(dto instanceof TellEventDTO){
 			TellEventDTO tellDto = (TellEventDTO)dto;
 			TellEvent tell = new TellEvent();
 			tell.setLocation(tellDto.location);
 			tell.setText(tellDto.text);
 			event = tell;
 		}else if(dto instanceof ListenEventDTO){
 			ListenEventDTO listenDto = (ListenEventDTO)dto;
 			ListenEvent listen = new ListenEvent();
 			listen.setSongMetadata(listenDto.songMetadata);
 			event = listen;
 		}else{
 			throw new AssertionError();
 		}
 		
 		dto.initEntity(event);
 		event.setUser(user);
 		event.setType(dto.type);
 		event.setStartTime(new Date(dto.startTime));
 		if(dto.endTime != null){
 			event.setEndTime(new Date(dto.endTime));
 		}
 		event.setMedia(dto.media);
 		event.setScore(dto.score);
 		
 		if(dto.participantIds != null){
 			List<User> participants = new ArrayList<User>();
 			for(Long id : dto.participantIds){
 				User participant = new User();
 				participant.setId(id);
 				participants.add(participant);
 			}
 			event.setParticipants(participants);
 		}
 		
 		if(dto.commentIds != null){
 			List<Comment> comments = new ArrayList<Comment>();
 			for(Long id : dto.commentIds){
 				Comment comment = new Comment();
 				comment.setId(id);
 				comments.add(comment);
 			}
 			event.setComments(comments);
 		}
 		
 		return event;
 	}
 	
 }
