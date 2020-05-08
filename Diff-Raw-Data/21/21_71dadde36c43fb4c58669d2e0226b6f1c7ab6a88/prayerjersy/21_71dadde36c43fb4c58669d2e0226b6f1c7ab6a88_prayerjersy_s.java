 package il.ac.tau.team3.prayerjersy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.SPGeoPoint;
 import il.ac.tau.team3.common.User;
 import il.ac.tau.team3.datastore.EMF;
 import il.ac.tau.team3.datastore.PlaceLocation;
 import il.ac.tau.team3.datastore.UserLocation;
 
 import javax.persistence.EntityManager;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 
 import javax.ws.rs.Path;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.model.GeocellQuery;
 import com.beoui.geocell.model.Point;
 import com.google.appengine.api.users.UserService;
 
 //The Java class will be hosted at the URI path "/helloworld"
 
 @Path("/prayerjersy")
 public class prayerjersy {
 
 	private volatile static List<GeneralUser> users;
 	private volatile static List<GeneralPlace> places;
 	private static final Logger log = Logger.getLogger(ServerCls.class
 			.getName());
 	private EntityManager entity;
 
 	public prayerjersy() {
 		entity = EMF.get().createEntityManager();
 	}
 
 	@GET
 	@Path("/user/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public GeneralUser retrieveUser(@PathParam("id") int id) {
 		if(id < users.size()){
 			return users.get(id);
 		}
 		else return null;
 	}
 	
 	@GET
 	@Path("/updateuserbyid")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean UpdateUserLocationById(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("id") long id){
 		entity.getTransaction().begin();
 		UserLocation userx = entity.find(UserLocation.class, id);
 		if(userx!=null){
 			userx.setLatitude(latitude);
 			userx.setLongitude(longitude);
 			userx.setGeoCellsData(latitude, longitude);
 			entity.getTransaction().commit();
 			return true;
 		}
 		return false;
 	}
 	
 	@PUT
 	@Path("/updateuserbyname")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public void UpdateUserLocationByName(GeneralUser user){
		entity.getTransaction().begin();
		UserLocation userx = entity.find(UserLocation.class, user.getName());
		if(userx==null){
 			this.createUserInDS(user.getSpGeoPoint().getLongitudeInDegrees(), user.getSpGeoPoint().getLatitudeInDegrees(), user.getName(), user.getStatus());
 			return;
 		}
 		
 		userx.setLatitude(user.getSpGeoPoint().getLatitudeInDegrees());
 		userx.setLongitude(user.getSpGeoPoint().getLongitudeInDegrees());
 		userx.setGeoCellsData(user.getSpGeoPoint().getLatitudeInDegrees(), user.getSpGeoPoint().getLongitudeInDegrees());
 		userx.setStatus(user.getStatus());
 		entity.getTransaction().commit();
 		return;
 	}
 	
 	
 	@GET
 	@Path("/users")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<GeneralUser> retrieveAllUsers(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("radius")long radius) {
 		return convertServerUserObjToClientUserObj(requestDatastoreForUsers(longitude, latitude, radius));
 	}
 	
 	@GET
 	@Path("/places")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<GeneralPlace> retrieveAllPlaces(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("radius")long radius) {
 		return convertServerPlaceObjToClientPlaceObj(requestDatastoreForPlaces(longitude, latitude, radius));
 	}
 	
 	@GET
 	@Path("/newuser")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Long CreateNewUser(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("name")String name,@QueryParam("status")String status) {
 		return this.createUserInDS(longitude, latitude, name, status);
 	}
 	
 	@GET
 	@Path("/newplace")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Long CreateNewPlace(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("name")String name,@QueryParam("status")String address) {
 		GeneralPlace u = new GeneralPlace(name,address,new SPGeoPoint((int)latitude*1000000, (int)longitude*1000000));
 		
 		PlaceLocation uloc = new PlaceLocation(u);
 		entity.getTransaction().begin();
 		entity.persist(uloc);
 		entity.getTransaction().commit();
 		
 		return uloc.getKey();
 	}
 	
 	@GET
 	@Path("/signtoplace")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean SignToPlace(@QueryParam("id") long id,  @QueryParam("name")String name) {
 		entity.getTransaction().begin();
 		PlaceLocation placex = entity.find(PlaceLocation.class, id);
 		if(placex!=null){
 			placex.addJoiner(name);
 			entity.getTransaction().commit();
 			return true;
 		}
 		return false;
 	}
 	
 	@GET
 	@Path("/place/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public GeneralPlace retrievePlace(@PathParam("id") int id) {
 		if (id < places.size()) return places.get(id);
 		else return null;
 	}
 
 	@GET
 	@Path("/user")
 	@Produces("application/json")
 	public Integer getNumUsers() {
 		return users.size();
 	}
 	
 	@GET
 	@Path("/place")
 	@Produces("application/json")
 	public Integer getNumPlaces() {
 		return places.size();
 	}
 
 	@PUT
 	@Consumes("application/json")
 	public void store(GeneralUser user) {
 
 	}
 
 	@GET
 	@Path("/deleteuser")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean removeUser(@QueryParam("id") long id) {
 		entity.getTransaction().begin();
 		UserLocation userx = entity.find(UserLocation.class, id);
 		if(userx!=null){
 		entity.remove(userx); 
 		entity.getTransaction().commit();
 		return true;
 		}
 		return false;
 	}
 	
 	@GET
 	@Path("/deleteplace")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean removePlace(@QueryParam("id") long id) {
 		entity.getTransaction().begin();
 		PlaceLocation placex = entity.find(PlaceLocation.class, id);
 		if(placex!=null){
 		entity.remove(placex); 
 		entity.getTransaction().commit();
 		return true;
 		}
 		return false;
 	}
 	
 	private List<UserLocation> requestDatastoreForUsers(double longitude,double latitude,long radius){
 		GeocellQuery baseQuery = new GeocellQuery("SELECT x FROM UserLocation x");
 		Point center = new Point (latitude, longitude);
 		List<UserLocation> results = GeocellManager.proximitySearch(center, 10, radius, UserLocation.class, baseQuery, entity, 13);
 		return results;
 	}
 	private List<PlaceLocation> requestDatastoreForPlaces(double longitude,double latitude,long radius){
 		GeocellQuery baseQuery = new GeocellQuery("SELECT x FROM PlaceLocation x");
 		Point center = new Point (latitude, longitude);
 		List<PlaceLocation> results = GeocellManager.proximitySearch(center, 10, radius, PlaceLocation.class, baseQuery, entity, 13);
 		return results;
 	}
 	
 	
 	private List<GeneralUser> convertServerUserObjToClientUserObj(List<UserLocation> serverUsers){
 		List<GeneralUser> returnList = new ArrayList<GeneralUser>();
 		for(UserLocation serverUser : serverUsers){
 			GeneralUser tmp = new GeneralUser(serverUser.getName(),new SPGeoPoint((int)serverUser.getLatitude()*1000000, (int)serverUser.getLongitude()*1000000),serverUser.getStatus());
 			returnList.add(tmp);
 			
 		}
 		return returnList;
 	}
 	
 	private List<GeneralPlace> convertServerPlaceObjToClientPlaceObj(List<PlaceLocation> serverPlaces){
 		List<GeneralPlace> returnList = new ArrayList<GeneralPlace>();
 		for(PlaceLocation serverPlace : serverPlaces){
 			GeneralPlace tmp = new GeneralPlace(serverPlace.getName(),serverPlace.getAddress(),new SPGeoPoint((int)serverPlace.getLatitude()*1000000, (int)serverPlace.getLongitude()*1000000));
 			returnList.add(tmp);
 			
 		}
 		return returnList;
 	}
 	
 	private Long createUserInDS(double longitude, double latitude, String name,String status){
 		GeneralUser u = new GeneralUser(name,new SPGeoPoint((int)latitude*1000000, (int)longitude*1000000),status);
 		UserLocation uloc = new UserLocation(u);
 		entity.getTransaction().begin();
 		entity.persist(uloc);
 		entity.getTransaction().commit();
 		
 		return uloc.getKey();
 	}
 }
