 package org.mdissjava.mdisscore.view.user;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 
 import org.bson.types.ObjectId;
 import org.mdissjava.commonutils.properties.PropertiesFacade;
 import org.mdissjava.mdisscore.controller.bll.AlbumManager;
 import org.mdissjava.mdisscore.controller.bll.UserManager;
 import org.mdissjava.mdisscore.controller.bll.impl.AlbumManagerImpl;
 import org.mdissjava.mdisscore.controller.bll.impl.UserManagerImpl;
 import org.mdissjava.mdisscore.model.dao.MdissNotificationDao;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.dao.impl.MdissNotificationDaoImpl;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.User;
 import org.mdissjava.mdisscore.model.pojo.notifications.FollowingNotification;
 import org.mdissjava.mdisscore.model.pojo.notifications.MdissNotification;
 import org.mdissjava.mdisscore.model.pojo.notifications.PhotoUploadedNotification;
 import org.mdissjava.mdisscore.view.params.ParamsBean;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.google.code.morphia.Datastore;
 
 @RequestScoped
 @ManagedBean
 public class DashboardBean {
 	private String user;
 	private User userObject;
 	//private String user2;
 	
 	private List<MdissNotification> notifications; 
 	private MdissNotificationDao mdissNotificationsDao;
 	private UserManager userManager;
 	private AlbumManager albumManager;
 	private Datastore datastore;
 	private String thumbnailBucket = "square.75";
 	private String thumbnailDatabase = "images";
 	private List<Album> albums;
 	private int knowMoreTimes = 1; 
 	
 	private Map<String, User> users;
 	
 	private final String GLOBAL_PROPS_KEY = "globals";
 	private final String MORPHIA_DATABASE_KEY = "morphia.db";
 	
 	private int NOTIFICATION_BATCH_NUMBER = 10;
 	private int totalNotifications;
 	private int page;
 	
 	private boolean more; 
 	
 	public DashboardBean() throws IOException {
 		this.user = getUser();
 		ParamsBean pb = getPrettyfacesParams();
 		this.page = pb.getPage();
 		if (this.page == 0){
 			this.page = 1;
 		}
 		
 		//get properties
 		PropertiesFacade propertiesFacade = new PropertiesFacade();
 		String database = propertiesFacade.getProperties(GLOBAL_PROPS_KEY).getProperty(MORPHIA_DATABASE_KEY);
 	
 		//get datastores
 		datastore = MorphiaDatastoreFactory.getDatastore(database);
 		mdissNotificationsDao = new MdissNotificationDaoImpl(datastore);
 		userManager = new UserManagerImpl();
 		albumManager = new AlbumManagerImpl(datastore);
 		
 		//get current user
 		this.userObject = userManager.getUserByNick(this.user);
 		
 		//get all the notifications
 		totalNotifications = mdissNotificationsDao.findTotalNotificationsUser(this.user);
 		notifications = mdissNotificationsDao.findUsersMdissNotifications(this.user, NOTIFICATION_BATCH_NUMBER, (page-1) * NOTIFICATION_BATCH_NUMBER);
 		this.users = new HashMap<String, User>();
 		
 		for (MdissNotification n: notifications){
 			if (n instanceof PhotoUploadedNotification){
				//do something...
 			}else if (n instanceof FollowingNotification){
 				FollowingNotification fn = (FollowingNotification)n;
				User follower = userManager.getUserByNick(fn.getFollowerUserName());
 				users.put(follower.getNick(), follower);
 			}
 		} 
 		
 		//get all the user albums
 		
 		this.albums = this.albumManager.findUserAlbums(user);
 	}
 
 	
 	public boolean followsUser(User follow) {
 		return userManager.followsUser(this.user, follow);
 	}
 	
 	public void addFollow(User follow) {		
 		userManager.addFollow(this.user, follow);
 	}
 	
 	public void addPrivateFollow(User follow, ObjectId objectId) {
 		
 		//updateState
 		FollowingNotification notification = new FollowingNotification();
 		notification.setId(objectId);
 		notification =  (FollowingNotification)mdissNotificationsDao.findMdissNotification(notification).get(0);
 		notification.setRead(true);
 		mdissNotificationsDao.updateMdissNotification((MdissNotification)notification);
 		//we are accepting is inverse from the original :D
 		userManager.addPrivateFollow(follow.getNick(), this.userObject);
 	}
 	
 	public void deleteFollow(User follow) {		
 		userManager.deleteFollow(this.user, follow);
 	}
 	
 //	public void infiniteScroll(AjaxBehaviorEvent event){
 //		this.knowMoreTimes++;
 //		this.notifications = mdissNotificationsDao.findUsersMdissNotifications(this.user, NOTIFICATION_BATCH_NUMBER * knowMoreTimes);
 //		if (this.notifications.size() != (NOTIFICATION_BATCH_NUMBER * knowMoreTimes)){
 //			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"No notifications remaining", "No more Notifications"));
 //			this.more = false;
 //		}else
 //			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Loaded " + NOTIFICATION_BATCH_NUMBER*knowMoreTimes + " more", null));
 //	}
 	
 	public List<MdissNotification> getNotifications() {
 		return notifications;
 	}
 
 	public void setNotifications(List<MdissNotification> notifications) {
 		this.notifications = notifications;
 	}
 		
 	public int getTotalNotifications() {
 		return totalNotifications;
 	}
 
 
 	public void setTotalNotifications(int totalNotifications) {
 		this.totalNotifications = totalNotifications;
 	}
 
 
 	public String getThumbnailBucket() {
 		return thumbnailBucket;
 	}
 
 	public void setThumbnailBucket(String thumbnailBucket) {
 		this.thumbnailBucket = thumbnailBucket;
 	}
 
 	public String getThumbnailDatabase() {
 		return thumbnailDatabase;
 	}
 
 	public void setThumbnailDatabase(String thumbnailDatabase) {
 		this.thumbnailDatabase = thumbnailDatabase;
 	}
 	
 	public int getNotificationBatchNumber() {
 		return NOTIFICATION_BATCH_NUMBER;
 	}
 
 
 	public void setNotificationBatchNumber(int notificationBatchNumber) {
 		this.NOTIFICATION_BATCH_NUMBER = notificationBatchNumber;
 	}
 
 
 	public int getPage() {
 		return page;
 	}
 
 
 	public void setPage(int page) {
 		this.page = page;
 	}
 
 
 	public boolean isMore() {
 		return more;
 	}
 
 
 	public void setMore(boolean more) {
 		this.more = more;
 	}
 
 	public Map<String, User> getUsers() {
 		return users;
 	}
 
 
 	public void setUsers(Map<String, User> users) {
 		this.users = users;
 	}
 
 	public List<Album> getAlbums() {
 		return albums;
 	}
 
 	public void setAlbums(List<Album> albums) {
 		this.albums = albums;
 	}
 
 	public User getUserObject() {
 		return userObject;
 	}
 
 
 	public void setUserObject(User userObject) {
 		this.userObject = userObject;
 	}
 
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 
 	//TODO: Clean up this class
 	//method 1 for getting the user from the session
 	public String getUser() {
 		return SecurityContextHolder.getContext().getAuthentication().getName();
 	}
 	
 	private ParamsBean getPrettyfacesParams() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		ParamsBean pb = (ParamsBean) context.getApplication().evaluateExpressionGet(context, "#{paramsBean}", ParamsBean.class);
 		return pb;
 	}	
 	
 	//method 2 for getting the user grom the session
 	/*public String getUser2() {
 		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		return user.getUsername(); //get logged in username
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 	public void setUser2(String user2) {
 		this.user2 = user2;
 	}*/
 	
 	
 
 }
