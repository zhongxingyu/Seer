 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 
 import java.util.Date;
 import java.util.List;
 
 public interface GoteFarmRPC extends RemoteService {
     public String newUser(String username, String email, String password) throws AlreadyExistsError;
     public String validateSID(String sid);
     public Long newCharacter(String sid, String realm, String character) throws UserNotLoggedInError, AlreadyExistsError, NotFoundError;
 
     public List<JSCharacter> getCharacters(String sid) throws UserNotLoggedInError;
     public JSCharacter getCharacter(String sid, long cid) throws UserNotLoggedInError, NotFoundError;
 
     public List<JSRole> getRoles();
     public Boolean addRole(String sid, String name, boolean restricted) throws UserNotLoggedInError, AlreadyExistsError;
     // public List<JSCharRole> getCharacterRoles(long cid);
     public JSCharacter updateCharacterRole(String sid, long cid, long roleid,
                                            boolean adding)
         throws UserNotLoggedInError, NotFoundError, IllegalArgumentException;
 
     public List<JSBadge> getBadges();
     public Boolean addBadge(String sid, String name, int score) throws UserNotLoggedInError, AlreadyExistsError;
     public JSCharacter updateCharacterBadge(String sid, long cid, long badgeid,
                                             boolean adding)
         throws UserNotLoggedInError, NotFoundError, IllegalArgumentException;
 
     public Boolean addInstance(String sid, String name) throws UserNotLoggedInError, AlreadyExistsError;
     public Boolean addBoss(String sid, String instance, String boss) throws UserNotLoggedInError, NotFoundError, AlreadyExistsError;
 
     public List<String> getInstances();
     public List<String> getInstanceBosses(String instance);
 
     public JSEventTemplate getEventTemplate(String sid, String name) throws UserNotLoggedInError, NotFoundError;
     public List<JSEventTemplate> getEventTemplates(String sid) throws UserNotLoggedInError;
     public Boolean saveEventTemplate(String sid, JSEventTemplate et)
         throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
             IllegalArgumentException;
 
     public List<JSEventSchedule> getEventSchedules(String sid, String name) throws UserNotLoggedInError;
     public Boolean saveEventSchedule(String sid, JSEventSchedule es)
         throws UserNotLoggedInError, NotFoundError, IllegalArgumentException;
 
     public List<JSEvent> getEvents(String sid) throws UserNotLoggedInError;
     /* Return null if signups are unchanged */
     public JSEventSignups getEventSignups(String sid, long eventid,
                                           Date if_changed_since)
         throws UserNotLoggedInError, NotFoundError;
    public JSEventSignups signupForEvent(String sid, long eventid,
                                         long cid, long roleid,
                                         int signup_type)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
               IllegalArgumentException;
    public JSEventSignups changeEventSignup(String sid, long eventid,
                                            long eventsignupid,
                                            long new_roleid,
                                            int new_signup_type)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
               IllegalArgumentException;
    public JSEventSignups removeEventSignup(String sid, long eventid,
                                            long eventsignupid)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
               IllegalArgumentException;
 }
