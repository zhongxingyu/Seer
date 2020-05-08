 package uk.ac.cam.dashboard.forms;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.FormParam;
 
 import org.hibernate.Session;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPGroup;
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
 import uk.ac.cam.cl.dtg.ldap.LDAPUser;
 import uk.ac.cam.dashboard.models.Group;
 import uk.ac.cam.dashboard.models.Notification;
 import uk.ac.cam.dashboard.models.NotificationUser;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.GroupQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 import uk.ac.cam.dashboard.util.Mail;
 import uk.ac.cam.dashboard.util.Strings;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 
 public class GroupForm {
 	@FormParam("title") String title;
 	@FormParam("users") String users;
 	@FormParam("import_id") String importID;
 	LDAPGroup importedGroup;
 	
 	public int handle(User currentUser) {		
 		
 		Session session = HibernateUtil.getTransactionSession();
 		
 		Group group = new Group(currentUser, title);
 
 		Set<User> groupMembers = createUserSet();
 		
 		group.setUsers(groupMembers);
 		
 		session.save(group);
 		
 		for(User u : groupMembers){
 			Set<Group> subscriptions = u.getSubscriptions();
 			subscriptions.add(group);
 			session.update(u);
 		}
 		
 		Notification notification = new Notification(); 
 		notification.setMessage(currentUser.getName() + " ("+currentUser.getCrsid()+")" +Strings.NOTIFICATION_SETGROUP + group.getTitle());
 		notification.setSection("dashboard");
 		notification.setLink("groups/");
 		session.save(notification);
 		for(User u : groupMembers){
 			NotificationUser nu = new NotificationUser();
 			nu.setUser(u);
 			nu.setNotification(notification);
 			session.save(nu);
 		}
 		
 		Mail.sendNotificationEmail(notification.getMessage(), groupMembers, "dashboard");
 		
 		return group.getId();
 	}
 
 	public Group handleUpdate(User currentUser, int id) {		
 		
 		Session session = HibernateUtil.getTransactionSession();
 
 		Group group = GroupQuery.get(id);
 		group.setTitle(title);
 		
 		Set<User> groupMembers = createUserSet();
 		
 		group.setUsers(groupMembers);
 		
 		session.update(group);
 		
 		Notification notification = new Notification(); 
 		notification.setMessage(currentUser.getName() + " ("+currentUser.getCrsid()+")" +Strings.NOTIFICATION_UPDATEGROUP + group.getTitle());
 		notification.setSection("dashboard");
 		notification.setLink("groups/");
 		session.save(notification);
 		for(User u : groupMembers){
 			NotificationUser nu = new NotificationUser();
 			nu.setUser(u);
 			nu.setNotification(notification);
 			session.save(nu);
 		}
 		
 		Mail.sendNotificationEmail(notification.getMessage(), groupMembers, "dashboard");
 		
 		return group;			
 	}
 	
 	public int handleImport(User currentUser) {	
 
 		Session session = HibernateUtil.getTransactionSession();
 		
 		String title = importedGroup.getName();
 		List<String> members = importedGroup.getUsers();
 
 		Group group = new Group(currentUser, title);
 
 		Set<User> groupMembers = new HashSet<User>();
 		for(String c : members){
 			User user = User.registerUser(c);
 			if(user!=null) { groupMembers.add(user); }
 		}	
 		
 		group.setUsers(groupMembers);
 		session.save(group);
 		
 		for(User u : groupMembers){
 			Set<Group> subscriptions = u.getSubscriptions();
 			subscriptions.add(group);
 			session.update(u);
 		}
 		
 		Notification notification = new Notification(); 
 		notification.setMessage(currentUser.getName() + " ("+currentUser.getCrsid()+")" +Strings.NOTIFICATION_SETGROUP + group.getTitle());
 		notification.setSection("dashboard");
 		notification.setLink("groups/");
 		session.save(notification);
 		for(User u : groupMembers){
 			NotificationUser nu = new NotificationUser();
 			nu.setUser(u);
 			nu.setNotification(notification);
 			session.save(nu);
 		}
 		
 		return group.getId();		
 	}
 
 	public ArrayListMultimap<String, String> validate() {
 		ArrayListMultimap<String, String> errors = ArrayListMultimap.create();
 
 		// title
 		if (title.equals("") || title == null){
 			errors.put("title", Strings.GROUP_NO_TITLE);
 		} else if(title.length()>255){
 			errors.put("title", Strings.GROUP_TITLE_LENGTH);
 		}
 		
 		// users
 		if((users==null||users.equals(""))){ 
 			errors.put("users", Strings.GROUP_NO_USERS); 
 		} 
 		
 		return errors;	
 	}
 	
 	public ArrayListMultimap<String, String> validateUpdate(int id, User currentUser) {
 		ArrayListMultimap<String, String> errors = validate();
 		
 		Group group = GroupQuery.get(id);
 		
 		if(!group.getOwner().equals(currentUser)){
 			errors.put("auth", Strings.GROUP_AUTHEDIT);
 		}
 		
 		return errors;
 	}
 	
 	public ArrayListMultimap<String, String> validateImport() {
 		ArrayListMultimap<String, String> errors = ArrayListMultimap.create();
 		
 		if((importID==null||importID.equals(""))){ 
 			errors.put("import_id", Strings.GROUP_IMPORT_NONE); 
 		}	
 		
 		try { 
 			importedGroup = LDAPQueryManager.getGroup(importID); 
 			if(importedGroup.getUsers().size()>100){
 				errors.put("import_id", Strings.GROUP_TOO_LARGE);
 			}
 			if(importedGroup.getUsers().size()==0){
 				errors.put("import_id", Strings.GROUP_NO_MEMBERS);
 			}
 		} 
 		catch (LDAPObjectNotFoundException e) {
 			errors.put("import_id", Strings.GROUP_CANNOT_RETRIEVE);
 		}
 		
 		return errors;	
 	}
 	
 	public ImmutableMap<String, ?> toMap(int id) {
 		ImmutableMap.Builder<String, Object> map = new ImmutableMap.Builder<String, Object>();
 		
 		map.put("id", id);
 		map.put("name", title);
 		
 		return map.build();
 	}
 	
 	public List<ImmutableMap<String, String>> usersToMap() {
 		List<ImmutableMap<String, String>> users = new ArrayList<ImmutableMap<String, String>>();
 
 		for(String c : parseUsers()){
 			try {
 				LDAPUser u = LDAPQueryManager.getUser(c);
				users.add(ImmutableMap.of("crsid", c, "name", u.getcName()));
 			} catch (LDAPObjectNotFoundException e) {
 				users.add(ImmutableMap.of("crsid", c, "name", Strings.USER_NOUSERNAME));				
 			}
 		}
 		
 		return users;
 	}
 
 	public String[] parseUsers(){
 		if(!(users==null||users.equals(""))){ return users.split(","); }
 		else { return new String[0]; }
 	}
 	
 	public Set<User> createUserSet(){
 		Set<User> groupMembers = new HashSet<User>();
 		for(String c : parseUsers()){
 			User user = User.registerUser(c);
 			if(user!=null) { groupMembers.add(user); };
 		}
 		return groupMembers;
 	}
 }
