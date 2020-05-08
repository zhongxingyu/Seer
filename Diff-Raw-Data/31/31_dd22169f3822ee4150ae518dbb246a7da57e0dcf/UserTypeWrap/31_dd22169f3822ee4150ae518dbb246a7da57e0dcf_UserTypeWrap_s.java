 package models.user;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import models.EMessages;
 import models.management.Listable;
 
 /**
  * This class represents a wrapper class for the class UserType.
  * This is used for the list view in the DMTV.
  * @author Thomas Mortier
  */
 public class UserTypeWrap implements Listable {
 
     /**
     * TODO explain why it's empty
      */
     public UserTypeWrap() {
 
     }
 
     /**
      * Returns a map for the translation of different usertypes.
      * This is used in the listview in the DMTV.
      * 
      * There is also a restriction when it comes to upgrading users
      * within the User Manager.
      * @return a map for the translation of different usertypes
      */
     @Override
     public Map<String, String> options() {
         LinkedHashMap<String, String> ret_opts = new LinkedHashMap<String, String>();
         /* check which type of user is currently logged in.
            ADMIN => EVERYONE\ANON
            ORGANIZER => STUDENT,TEACHER
            TEACHER => NULL
            (UPGRADE USER)
          */
         UserType t_curr_user = AuthenticationManager.getInstance().getUser().getType();
         for(UserType ut : UserType.values()) {
             if(t_curr_user.equals(UserType.ADMINISTRATOR)) {
                 if(!ut.equals(UserType.ANON)) {
                     ret_opts.put(ut.toString(), EMessages.get("user." + ut.toString()));
                 }
             } else if(t_curr_user.equals(UserType.ORGANIZER)) {
                 if(ut.equals(UserType.PUPIL_OR_INDEP) || ut.equals(UserType.TEACHER)) {
                     ret_opts.put(ut.toString(), EMessages.get("user." + ut.toString()));
                 }
             } else {
                 ret_opts.put(UserType.PUPIL_OR_INDEP.toString(),EMessages.get("user." + UserType.PUPIL_OR_INDEP.toString()));
             }
         }
         return ret_opts;
     }
 }
