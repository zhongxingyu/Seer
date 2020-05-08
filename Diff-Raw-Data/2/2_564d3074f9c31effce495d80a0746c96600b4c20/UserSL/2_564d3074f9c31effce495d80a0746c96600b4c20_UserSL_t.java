 package uk.frequency.glance.server.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import uk.frequency.glance.server.business.UserBL;
 import uk.frequency.glance.server.business.exception.MissingFieldException;
 import uk.frequency.glance.server.model.event.Event;
 import uk.frequency.glance.server.model.user.Friendship;
 import uk.frequency.glance.server.model.user.User;
 import uk.frequency.glance.server.model.user.UserProfile;
 import uk.frequency.glance.server.transfer.user.FriendshipDTO;
 import uk.frequency.glance.server.transfer.user.UserDTO;
 
 
 @Path("/user")
 public class UserSL extends GenericSL<User, UserDTO>{
 
 	UserBL userBl;
 
 	public UserSL() {
 		super(new UserBL());
 		userBl = (UserBL)business;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/friendship/full")
 	public List<FriendshipDTO> findFriendships(
 			@PathParam("id") long userId){
 		List<Friendship> entities = userBl.findFriendships(userId);
 		List<FriendshipDTO> dto = listToDTO(entities);
 		business.flush();
 		return dto;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{id}/friendship")
 	public List<Long> findFriends(
 			@PathParam("id") long userId){
 		List<Long> list = userBl.findFriends(userId);
 		business.flush();
 		return list;
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{id}/friendship/received")
 	public List<Long> findFriendshipRequests(
 			@PathParam("id") long userId){
 		List<Long> list = userBl.findFriendshipRequests(userId);
 		business.flush();
 		return list;
 	}
 	
 	@PUT
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{id}/friendship/request-{friend}")
 	public FriendshipDTO requestFriendship(
 			@PathParam("id") long userId,
 			@PathParam("friend") long friendId){
 		Friendship entity = userBl.createFriendshipRequest(userId, friendId);
 		FriendshipDTO dto = toDTO(entity);
 		business.flush();
 		return dto;
 	}
 	
 	@PUT
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{id}/friendship/accept-{friend}")
 	public FriendshipDTO acceptFriendship(
 			@PathParam("id") long userId,
 			@PathParam("friend") long friendId){
 		Friendship entity = userBl.acceptFriendshipRequest(userId, friendId);
 		FriendshipDTO dto = toDTO(entity);
 		business.flush();
 		return dto;
 	}
 	
 	@PUT
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/{id}/friendship/deny-{friend}")
 	public FriendshipDTO denyFriendship(
 			@PathParam("id") long userId,
 			@PathParam("friend") long friendId){
 		Friendship entity = userBl.denyFriendshipRequest(userId, friendId);
 		FriendshipDTO dto = toDTO(entity);
 		business.flush();
 		return dto;
 	}
 	
 	@Override
 	protected UserDTO toDTO(User user){
 		UserDTO dto = new UserDTO();
 		initToDTO(user, dto);
 		
 		if(user.getProfileHistory() != null && !user.getProfileHistory().isEmpty()){
 			dto.setProfile(user.getProfileHistory().get(0)); //TODO get most recent profile
 		}
 		
 		if(user.getEvents() != null){
 			List<Long> eventIds = new ArrayList<Long>();
 			for(Event event : user.getEvents()){
 				eventIds.add(event.getId());
 			}
 			dto.setEventsIds(eventIds);
 		}
 		
 		return dto;
 	}
 
 	@Override
 	protected User fromDTO(UserDTO dto) {
 		User user = new User();
 		initFromDTO(dto, user);
 
 		if (dto.getProfile() != null) {
 			List<UserProfile> profiles = new ArrayList<UserProfile>();
 			profiles.add(dto.getProfile());
 			user.setProfileHistory(profiles);
 		}
 		
 		user.setUsername(dto.getUsername());
 		if(dto.getUsername() == null){
 			throw new MissingFieldException("username");
 		}
 
 		return user;
 	}
 	
 	protected FriendshipDTO toDTO(Friendship friendship){
 		FriendshipDTO dto = new FriendshipDTO();
 		initToDTO(friendship, dto);
 		
 		dto.setUserId(friendship.getUser().getId());
 		dto.setFriendId(friendship.getFriend().getId());
 		dto.setStatus(friendship.getStatus());
 		
 		return dto;
 	}
 	
 	protected List<FriendshipDTO> listToDTO(List<Friendship> list){
 		List<FriendshipDTO> dto = new ArrayList<FriendshipDTO>();
 		for(Friendship entity : list){
 			dto.add(toDTO(entity));
 		}
 		return dto;
 	}
 	
 }
