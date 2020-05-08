 package com.britesnow.samplesocial.web;
 
 import java.util.List;
 import java.util.Map;
 
 import com.britesnow.samplesocial.entity.Contact;
 import com.britesnow.samplesocial.entity.SocialIdEntity;
 import com.britesnow.samplesocial.entity.User;
 import com.britesnow.samplesocial.service.FContactService;
 import com.britesnow.samplesocial.service.FacebookAuthService;
 import com.britesnow.samplesocial.service.FacebookService;
 import com.britesnow.snow.web.RequestContext;
 import com.britesnow.snow.web.handler.annotation.WebActionHandler;
 import com.britesnow.snow.web.handler.annotation.WebModelHandler;
 import com.britesnow.snow.web.param.annotation.WebModel;
 import com.britesnow.snow.web.param.annotation.WebParam;
 import com.britesnow.snow.web.param.annotation.WebUser;
 import com.google.inject.Inject;
 
 public class FacebookContactHandlers {
     @Inject
     private FacebookService     facebookService;
     @Inject
     private FContactService     fContactService;
 
     @Inject
     private FacebookAuthService facebookAuthService;
 
     @WebModelHandler(startsWith = "/fbFriendsList")
     public void getFacebookFriends(@WebUser User user, @WebParam("limit") Integer limit,
                             @WebParam("offset") Integer offset, RequestContext rc) {
         SocialIdEntity e = facebookAuthService.getSocialIdEntity(user.getId());
         String token = e.getToken();
         List ls = facebookService.getFriendsByPage(token, limit, offset);
         rc.getWebModel().put("_jsonData", ls);
     }
 
     @WebModelHandler(startsWith = "/fbContactsList")
     public void getFacebookContacts(@WebModel Map m, @WebUser User user, @WebParam("pageSize") Integer pageSize,
                             @WebParam("pageIndex") Integer pageIndex, RequestContext rc) {
         List ls = fContactService.getContactsByPage(user);
         m.put("result", ls);
        if (ls.size() == pageSize) {
             m.put("hasNext", true);
         }
     }
 
     @WebActionHandler
     public Object addFacebookContact(@WebParam("token") String token, @WebParam("groupId") Long groupId,
                             @WebParam("fbid") String fbid) {
         try {
             Contact c = fContactService.addContact(token, groupId, fbid);
             return c;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @WebActionHandler
     public void deleteFacebookContact(@WebParam("id") String id) {
         try {
             fContactService.deleteContact(id);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
