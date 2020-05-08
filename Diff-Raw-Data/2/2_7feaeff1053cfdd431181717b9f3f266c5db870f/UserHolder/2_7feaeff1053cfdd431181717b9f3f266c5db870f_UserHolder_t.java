 package fr.fcamblor.demos.sbjd.web.holders;
 
 import fr.fcamblor.demos.sbjd.models.Credentials;
 import fr.fcamblor.demos.sbjd.models.User;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.RequestContextHolder;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * @author fcamblor
  */
 public class UserHolder {
 
     private static final AtomicLong userIncrement = new AtomicLong(0);
     private static final String LOGGED_USER_KEY = "loggedUser";
     private static final String USERS_KEY = "users";
 
     protected static <T> T getInSession(String key){
         return (T) RequestContextHolder.currentRequestAttributes().getAttribute(key, RequestAttributes.SCOPE_SESSION);
     }
 
     protected static <T> void storeInSession(String key, T value){
         RequestContextHolder.currentRequestAttributes().setAttribute(key, value, RequestAttributes.SCOPE_SESSION);
     }
 
     public static User loggedUser(){
         return getInSession(LOGGED_USER_KEY);
     }
 
     public static void login(Credentials credentials){
         User existingUser = null;
         for(User user : users()){
             if(user.getCredentials().equals(credentials)){
                 existingUser = user;
                 break;
             }
         }
 
         if(existingUser != null){
             storeInSession(LOGGED_USER_KEY, existingUser);
         } else {
             throw new IllegalStateException(String.format("Bad credentials for login : %s !", credentials));
         }
     }
 
     public static void update(User userToUpdate) {
         List<User> users = users();
         int i = 0;
         for(i=0; i<users.size(); i++){
             if(users.get(i).getId().equals(userToUpdate.getId())){
                 break;
             }
         }
 
         if(i != users.size()){
             users.set(i, userToUpdate);
         } else {
             throw new IllegalStateException(String.format("Unknown user id : %s !", userToUpdate.getId()));
         }
     }
 
     public static List<User> users(){
         List<User> users = getInSession(USERS_KEY);
         if(users == null){
             users = new ArrayList<User>();
         }
         return users;
     }
 
     public static void store(User user){
         long userId = userIncrement.getAndIncrement();
         user.setId(userId);
 
         List<User> users = users();
         if(users == null){
             users = new ArrayList<User>();
         }
         users.add(user);
        storeInSession(USERS_KEY, users);
     }
 }
