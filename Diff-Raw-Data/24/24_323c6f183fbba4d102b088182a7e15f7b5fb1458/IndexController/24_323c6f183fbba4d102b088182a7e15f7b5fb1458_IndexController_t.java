 package com.xhills.golf_party.controller.me;
 
 import java.io.Writer;
 import java.util.List;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
import com.google.appengine.repackaged.org.json.JSONObject;
 import com.restfb.types.User;
 import com.xhills.golf_party.common.facebook.Me;
 
 /**
    /me/
 
    http://locahost:8888/me/?service=xxxx&access_token=175391655867010|a53f95a16a8627ce29b0687a.1-100002000444155|SDBPT15R06N2yRWvVSz9WPzgFgo
  */
 public class IndexController extends Controller {
 
     @Override
     public Navigation run() throws Exception {
         
         String service = asString("service"); //asInt
 
         Writer writer = this.response.getWriter();
         Me me = (Me) sessionScope("me");
         if (me != null) {
 
             if (service.equals("get_me")) {
 
                 // service: get_me
                 User user = me.user;
                //writer.write("{name:" + user.getName() + ",id:" + user.getId() + "}");
 
                JSONObject obj = new JSONObject();
                obj.put("name", user.getName());
                obj.put("id", user.getId());
                obj.write(writer);
                
             } else if (service.equals("get_friends")) {
             
                 // service: get_friends
                 List<User> friends = me.friends;
                 writer.write("{friends:[");
                 for (int i = 0; i < friends.size(); i++) {
                     if (i > 0)
                         writer.write(",");
                     User friend = friends.get(i);
                     writer.write("{name:" + friend.getName() + ",id:" + friend.getId() + "}");
                 }
                 writer.write("]}");
             }
         } else {
             
             if (service.equals("login")) {
 
                 // service: login
                 String accessToken = asString("access_token");
                 sessionScope("me", new Me(accessToken));
                 
                 writer.write("{success:true}");
             }
             
         }
         return null;
     }
 }
