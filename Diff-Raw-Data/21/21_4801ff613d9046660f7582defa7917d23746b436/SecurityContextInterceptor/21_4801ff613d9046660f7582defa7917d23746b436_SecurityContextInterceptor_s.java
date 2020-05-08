 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nurelm.securitycontext;
 
 import com.nurelm.interceptordeps.*;
 import com.nurelm.securitycontext.util.SecurityContextUtilImpl;
 import java.util.Collection;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 /**
  *
  * @author Paul Scarrone NuRelm
  */
 public class SecurityContextInterceptor extends HandlerInterceptorAdapter {
 
   @Autowired SecurityContextUtilImpl securityContextUtil;
 
   private enum UUIDStatus {
 
     VALID,
     INVALID,
     NOCONTEXT
   }
 
   /**
    * 
    * @param request
    * @param response
    * @param handler
    * @return
    * @throws Exception
    */
   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
     if (request.getMethod().toLowerCase().equals("get")) {
       if (request.getSession().getAttribute("AjaxRequestSigning") != null) {
         RequestSigning rs = (RequestSigning) request.getSession().getAttribute("AjaxRequestSigning");
         rs.getNextCurrentSigning();
         request.getSession().setAttribute("AjaxRequestSigning", rs);
       } else {
         RequestSigning rs = new RequestSigning();
         rs.getNextCurrentSigning();
         request.getSession().setAttribute("AjaxRequestSigning", rs);
       }
       //UUID current = UUID.randomUUID();
       RequestSigning active = (RequestSigning) request.getSession().getAttribute("RequestAuth");
       if (active != null) {
         RequestSigning RequestAuth = (RequestSigning) request.getSession().getAttribute("RequestAuth");
         RequestAuth.getNextCurrentSigning();
         request.getSession().setAttribute("RequestAuth", RequestAuth);
       } else {
         RequestSigning RequestAuth = new RequestSigning();
         RequestAuth.getNextCurrentSigning();
         request.getSession().setAttribute("RequestAuth", RequestAuth);
       }
       //request.getSession().setAttribute("RequestAuth", current);
       if (((AuthData) request.getSession().getAttribute("AuthData")) != null) {
       }
     }
     UserMonitorImpl userMon = (UserMonitorImpl) request.getSession().getAttribute("UserMonitor");
     if (userMon != null) {
       userMon.setRa((RequestSigning) request.getSession().getAttribute("RequestAuth"));
       userMon.setAjaxra((RequestSigning) request.getSession().getAttribute("AjaxRequestSigning"));
       request.getSession().setAttribute("UserMonitor", userMon);
     }
     return super.preHandle(request, response, handler);
   }
 
   private UUIDStatus requestSigning(ModelAndView modelAndView, HttpServletRequest request) {
     if (modelAndView.getModel().containsKey("RequestSigning")) {
       if (modelAndView.getModel().get("RequestSigning").equals(((RequestSigning) request.getSession().getAttribute("RequestAuth")).getCurrentSigning())
               || modelAndView.getModel().get("RequestSigning").equals(((RequestSigning) request.getSession().getAttribute("RequestAuth")).getLastSigning())
               || request.getRequestURL().equals(((NavigationStruct) request.getSession().getAttribute("NavigationData")).getLastPage().replace("\\?", ""))) {
         return UUIDStatus.VALID;
       } else {
         return UUIDStatus.INVALID;
       }
     }
     return UUIDStatus.NOCONTEXT;
   }
 
   /**
    * 
    * @param request
    * @param response
    * @param handler
    * @param modelAndView
    * @throws Exception
    */
   @Override
   public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
     AuthData Auth = ((AuthData) request.getSession().getAttribute("AuthData"));
     Boolean isAuth = false;
     if (Auth != null) {
       AuthStruct AuthRole = Auth.getAuth();
       AccessLevel matchAuth = null;
       if (modelAndView.getModelMap().containsKey("AccessLevels")) {
         Map<AccessLevel, Object> roles = (Map<AccessLevel, Object>) modelAndView.getModelMap().get("AccessLevels");
         for (AccessLevel item : (Collection<AccessLevel>) AuthRole.getAuthRole()) {
           if (roles.containsKey(item)) {
             matchAuth = (AccessLevel) item;
             isAuth = true;
           }
         }
       }
 
       if (!isAuth) {
         String message = "";
         if (modelAndView.getModelMap().containsKey("AccessLevels")) {
           message = request.getRequestURI() + ": User must have Auth Level " + this.securityContextUtil.getAccessLevelString((Map) modelAndView.getModelMap().get("AccessLevels"));
           //Logger.getLogger(SecurityContextInterceptor.class.getName()).log(Level.INFO, );
         } else {
           message = "No Security Context Configured for Page " + request.getRequestURI();
           //Logger.getLogger(SecurityContextInterceptor.class.getName()).log(Level.INFO, "No Security Context Configured for Page " + request.getRequestURI());
         }
         //UserMessage.setMessage(request, message, "Routing Error");
         Logger.getLogger(SecurityContextInterceptor.class.getName()).log(Level.INFO, message);
         modelAndView.setViewName("redirect:/" + ((NavigationStruct) request.getSession().getAttribute("NavigationData")).getRedirectURL(((NavigationStruct) request.getSession().getAttribute("NavigationData")).getLastPage()));
         //modelAndView.setViewName("redirect:/maindash");
         request.getSession().setAttribute("SecurityMessage", message);
       }else{
         Logger.getLogger(SecurityContextInterceptor.class.getName()).log(Level.INFO, request.getRequestURI() + ": User matched access level " +matchAuth );
       }
     }
     UUIDStatus uuidStatus = requestSigning(modelAndView, request);
     if (!uuidStatus.equals(UUIDStatus.INVALID)) {
     } else if (uuidStatus.equals(UUIDStatus.INVALID)) {
       modelAndView.setViewName("redirect:/" + ((NavigationStruct) request.getSession().getAttribute("NavigationData")).getRedirectURL(((NavigationStruct) request.getSession().getAttribute("NavigationData")).getLastPage()));
       request.getSession().setAttribute("SecurityMessage", "Counterfit Post Request Detected " + request.getRequestURI());
     }
 
     super.postHandle(request, response, handler, modelAndView);
   }
 
   /**
    * 
    * @param request
    * @param response
    * @param handler
    * @param ex
    * @throws Exception
    */
   @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
     super.afterCompletion(request, response, handler, ex);
   }
 }
