 package com.vdweem.jplanningpoker.actions;
 
 import javax.servlet.http.Cookie;
 
 import org.apache.struts2.ServletActionContext;
 
 import com.opensymphony.xwork2.Result;
 import com.vdweem.jplanningpoker.Util;
 import com.vdweem.jplanningpoker.session.SessionManager;
 
 /**
  * com.vdweem.jplanningpoker.actions.ChangenameAction
  *
  * Changes the name of a user on a POST request, or returns the name on a GET request.
  * @author       Niels
  */
 public class ChangenameAction {
     private String name;
 
     public Result execute() {
         if ("POST".equals(ServletActionContext.getRequest().getMethod())) {
             Cookie cookie = new Cookie("name", this.name);
             cookie.setMaxAge(60 * 60 * 24 * 31); // ~1 month
             ServletActionContext.getResponse().addCookie(cookie);
             SessionManager.getSession().setName(this.name);
             return new JSONResult("success");
         }
         if (Util.isEmpty(SessionManager.getSession().getName())) {
             changeNameFromCookie();
         }
         return new JSONResult("name", SessionManager.getSession().getName());
     }
 
     public static void changeNameFromCookie() {
         for (Cookie cookie : ServletActionContext.getRequest().getCookies()) {
             if ("name".equals(cookie.getName())) {
                 SessionManager.getSession().setName(cookie.getValue());
             }
         }
     }
 
     /**
      * @param name The name to set.
      */
     public void setName(String name) {
         this.name = name;
     }
 
 }
