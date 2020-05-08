 package controllers;
 
 import models.ChatRoom;
 import models.User;
 import play.cache.Cache;
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.data.validation.URL;
 import play.mvc.*;
 import play.libs.Codec;
 
 public class GroupController extends Controller {
 
     public static final String USER_KEY = "auth-user";
     
     public static final String FROM_KEY = "from-time";
 
     public static void index(@Required String groupId) {
         boolean logged = false;
         User user = null;
         if (session.contains(USER_KEY)) {
             logged = true;
             user = User.findByGroupAndEmail(groupId, session.get(USER_KEY));
             if (user == null) {
                 logged = false;
                 session.clear();
             } else {
                 if (!user.group.groupId.equals(groupId)) {
                     index(user.group.groupId);
                 }
             }
         }
         render(user, logged, groupId);
     }
 
     public static void signin(@Required String groupId) {
         session.put(FROM_KEY, System.currentTimeMillis());
         boolean logged = false;
         if (session.contains(USER_KEY)) {
             logged = true;
         }
         if (logged) {
             User user = User.findByGroupAndEmail(groupId, session.get(USER_KEY));
             Rooms.room(groupId, Rooms.WELCOME_ROOM);
         }
         render(logged, groupId);
     }
     
     public static void register(@Required String groupId) {
         String randomID = Codec.UUID();
         String username = "";
         String password = "";
         String name = "";
         String surname = "";
         String mail = "";
         String avatar  ="";
         render(randomID, username, password, name, surname, mail, avatar, groupId);
     }
     
     public static void registration(
             @Required String groupId,
             @Required String username, 
             @Required String password, 
             @Required String repassword, 
             @Required String name, 
             @Required String surname, 
             String phone, 
             @Required @Email String mail, 
             String address, 
             @URL String avatar, 
             @URL String gravatar, 
             @Required String code, 
             @Required String randomID) {
         if (!password.equals(repassword)) {
             flash.error("Passwords don't match !");
             randomID = Codec.UUID();
             render("GroupController/register.html", randomID, username, 
                     password, name, surname, mail, avatar, groupId);
         }
         if (!validation.equals(code, Cache.get(randomID)).ok) {
             flash.error("Wrong CAPTCHA !!!");
             randomID = Codec.UUID();
             render("GroupController/register.html", randomID, username, 
                     password, name, surname, mail, avatar, groupId);
         }
         if(validation.hasErrors()) {
             flash.error(validation.errorsMap().toString());
             signin(groupId);
         }
         User existing = User.findByGroupAndEmail(groupId, mail);
         if (existing != null) {
             flash.error("Email address already exists");
             randomID = Codec.UUID();
             render("GroupController/register.html", randomID, username, 
                     password, name, surname, mail, avatar, groupId);
         }
         User.createUser(username, name, surname, 
                 phone, mail, address, avatar, gravatar, password, groupId);
         signin(groupId);
     }
     
     public static void enter(@Required String groupId, 
             @Required @Email String user, @Required String password) {        
         if(validation.hasErrors()) {
             flash.error("Bad email address");
             index(groupId);
         }
         User u = User.findByGroupAndMailAndPassword(groupId, user, password);
         if (u == null) {
             flash.error("Please choose a valid user.");
             index(groupId);
         }
         session.put(USER_KEY, u.mail);
         // TODO : not great, need to handle join and leave
 //        for (Object obj : ChatRoom.findPublicRoomsByGroup(groupId)) {
 //            ChatRoom chat = ((ChatRoom) obj);
 //            if (!chat.closed) {
 //                chat.join(u);
 //            }
 //        }
         //ChatRoom room = ChatRoom.findByGroupAndName(groupId, Rooms.WELCOME_ROOM);
         //room.leave(u);
        u = User.connect(u);
         Rooms.room(groupId, Rooms.WELCOME_ROOM);
     }
 }
