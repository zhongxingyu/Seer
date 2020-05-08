 package org.northstar.bricks.services;
 
 import com.google.inject.Inject;
 import com.google.sitebricks.client.transport.Json;
 import com.google.sitebricks.headless.Reply;
 import com.google.sitebricks.headless.Request;
 import com.google.sitebricks.http.Post;
 import org.northstar.bricks.auth.CurrentUser;
 import org.northstar.bricks.domain.User;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: northstar
  * Date: 12-8-16
  * Time: 下午5:51
  * To change this template use File | Settings | File Templates.
  */
 public class LoginAction {
 
     private String name;
     private String password;
    private String lastUrl;
 
     private CurrentUser login;
     private Authentication auth;
    @Inject
    private Request request;
 
     @Inject
     public LoginAction(CurrentUser login, Authentication auth) {
         this.login = login;
         this.auth = auth;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
    public String getLastUrl() {
        return lastUrl;
    }

    public void setLastUrl(String lastUrl) {
        this.lastUrl = lastUrl;
    }

     @Post
     public Reply login() {
         System.out.println("loginAction excuted ...");
         login = auth.authenticate(name, password);
         Map<String, String> result = new HashMap<String, String>();
         if (login.isAuthenticated()) {
            //return Reply.saying().redirect(lastUrl);
             result.put("info", "success");
         } else {
             result.put("info", "您输入的用户名或密码不正确！");
         }
         return Reply.with(result).as(Json.class);
     }
 }
