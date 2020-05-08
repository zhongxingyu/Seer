 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.Date;
 import java.util.List;
 
 interface GoteFarmRPCAsync {
     public void newUser(String username, String email, String password, AsyncCallback<String> cb);
     public void validateSID(String sid, AsyncCallback<String> cb);
     public void newCharacter(String sid, String realm, String character, AsyncCallback<Long> cb);
 
     public void getCharacters(String sid, AsyncCallback<List<JSCharacter>> cb);
     public void getCharacter(String sid, long cid, AsyncCallback<JSCharacter> cb);
 
     public void getRoles(AsyncCallback<List<JSRole>> cb);
     public void addRole(String sid, String name, boolean restricted, AsyncCallback<Boolean> cb);
     // public void getCharacterRoles(long cid, AsyncCallback<List<JSCharRole>> cb);
     public void updateCharacterRole(String sid, long cid, long roleid,
                                     boolean adding,
                                     AsyncCallback<JSCharacter> cb);
 
     public void getBadges(AsyncCallback<List<JSBadge>> cb);
     public void addBadge(String sid, String name, int score, AsyncCallback<Boolean> cb);
     public void updateCharacterBadge(String sid, long cid, long badgeid,
                                      boolean adding,
                                      AsyncCallback<JSCharacter> cb);
 
     public void addInstance(String sid, String name, AsyncCallback<Boolean> cb);
     public void addBoss(String sid, String instance, String boss, AsyncCallback<Boolean> cb);
 
     public void getInstances(AsyncCallback<List<String>> cb);
     public void getInstanceBosses(String instance, AsyncCallback<List<String>> cb);
 
     public void getEventTemplate(String sid, String name, AsyncCallback<JSEventTemplate> cb);
     public void getEventTemplates(String sid, AsyncCallback<List<JSEventTemplate>> cb);
     public void saveEventTemplate(String sid, JSEventTemplate et, AsyncCallback<Boolean> cb);
 
     public void getEventSchedules(String sid, String name, AsyncCallback<List<JSEventSchedule>> cb);
     public void saveEventSchedule(String sid, JSEventSchedule es, AsyncCallback<Boolean> cb);
 
     public void getEvents(String sid, AsyncCallback<List<JSEvent>> cb);
     public void getEventSignups(String sid, long eventid,
                                 Date if_changed_since,
                                 AsyncCallback<JSEventSignups> cb);
 }
