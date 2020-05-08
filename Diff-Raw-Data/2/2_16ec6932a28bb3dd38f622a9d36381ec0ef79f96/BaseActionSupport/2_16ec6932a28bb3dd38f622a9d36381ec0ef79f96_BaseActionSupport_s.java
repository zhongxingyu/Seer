 package com.github.mytask.web.controller;
 
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.struts2.interceptor.ServletRequestAware;
 import org.apache.struts2.interceptor.ServletResponseAware;
 import org.apache.struts2.interceptor.SessionAware;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 
 /**
  * base action support
  */
 public class BaseActionSupport extends ActionSupport implements ServletRequestAware, ServletResponseAware, SessionAware {
     /**
      * http servlet request
      */
     protected HttpServletRequest request;
     /**
      * http servlet response
      */
     protected HttpServletResponse response;
     /**
      * http session
      */
     protected Map<String, Object> session;
 
     public void setServletRequest(HttpServletRequest request) {
         this.request = request;
     }
 
     public void setServletResponse(HttpServletResponse response) {
         this.response = response;
     }
 
    public void setSession(Map<String, Object> stringObjectMap) {
         this.session = session;
     }
 }
