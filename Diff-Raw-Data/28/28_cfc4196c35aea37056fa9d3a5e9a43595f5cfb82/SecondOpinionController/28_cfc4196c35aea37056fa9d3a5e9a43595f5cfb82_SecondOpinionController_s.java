 package com.amazon.aws.samplecode.travellog.web;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
 import java.util.Date;
import java.util.LinkedHashMap;
 import java.util.List;
import java.util.Map;
import java.util.logging.Level;
 import java.util.logging.Logger;
 
import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
import com.amazon.aws.samplecode.travellog.aws.S3PhotoUtil;
import com.amazon.aws.samplecode.travellog.aws.TravelLogSNSManager;
 import com.amazon.aws.samplecode.travellog.dao.TravelLogDAO;
import com.amazon.aws.samplecode.travellog.entity.Comment;
import com.amazon.aws.samplecode.travellog.entity.Entry;
import com.amazon.aws.samplecode.travellog.entity.Journal;
import com.amazon.aws.samplecode.travellog.entity.Photo;
 import com.amazon.aws.samplecode.travellog.entity.User;
import com.amazon.aws.samplecode.travellog.util.Configuration;
import com.amazon.aws.samplecode.travellog.util.DataExtractor;
import com.amazon.aws.samplecode.travellog.util.DataLoader;
 
 /**
  * This is the core of the TravelLog functionality.  It's a Spring controller implemented
  * using annotations.  Most methods for loading and storing journals, entries, comments and photos
  * are initiated in this class.
  */
 @Controller
 public class SecondOpinionController {
 
     private TravelLogDAO dao;
     private static final Logger logger=Logger.getLogger(SecondOpinionController.class.getName());
 
 
     /**
      * AWS Elastic Beanstalk checks your application's health by periodically
      * sending an HTTP HEAD request to a resource in your application. By
      * default, this is the root or default resource in your application,
      * but can be configured for each environment.
      *
      * Here, we report success as long as the app server is up, but skip
      * generating the whole page since this is a HEAD request only. You
      * can employ more sophisticated health checks in your application.
      *
      * @param model the spring model for the request
      */
     @RequestMapping(value="/home.do", method=RequestMethod.HEAD)
     public void doHealthCheck(HttpServletResponse response) {
         response.setContentLength(0);
         response.setStatus(HttpServletResponse.SC_OK);
     }
 
     /**
      * The main request handler that builds out the home page for the journal
      * @param model the spring model for the request
      */
     @RequestMapping(value="/home.do", method={RequestMethod.GET, RequestMethod.POST})
     public void doHome (ModelMap model) {
         List<User> users = dao.getUsers();
 
     }
     
     @RequestMapping(value="/signin.do", method={RequestMethod.GET})
     public void doSignIn (ModelMap model) {
    
     }    
 
     @RequestMapping(value="/forgotpassword.do", method={RequestMethod.GET})
     public void doForgotPassword (ModelMap model) {
         
     }   
 
     @RequestMapping(value="/registration.do", method={RequestMethod.GET})
     public void doRegistration (ModelMap model) {
         
     } 
     
     /**
      * If we have a login failure this request mapping flags the error to be shown
      * in the UI.
      * @param model the spring model for the request
      */
     @RequestMapping ("/loginFailure.do")
     public ModelAndView doLoginFailure (ModelMap map) {
     	map.addAttribute("popupScreen","login_div");
         doSignIn(map);
         return new ModelAndView("signin", map);
     }
 
     
     @RequestMapping("/createAccount.do")
     public ModelAndView doCreateAccount (User user,  BindingResult result, ModelMap map,
       @RequestParam("password2") String password2) {
 
         //Verify user info submission
         if (user.getUsername().equals("")) {
             result.reject("username", "Username cannot be blank");
         }
         if (user.getPassword().equals("")) {
             result.reject("password", "Password cannot be blank");
         }
         if (!user.getPassword().equals(password2)) {
             result.reject("password", "Passwords do not match");
         }
 
         if (result.hasErrors()) {
             doHome(map);
             return new ModelAndView("home", map);
         }
 
         // check to make sure we don't have a user account already
         List<User> users = dao.getUsers();
         if (users.size() > 0) {
             result.reject("username", "The admin user already exists");
             return new ModelAndView("home", map);
         } else {
             dao.saveUser(user);
             map.addAttribute("usercreated", true);
         }
 
         doHome(map);
         return new ModelAndView("redirect:home.do");
 
     }
     
     @RequestMapping(value="/doctorprofile.do", method={RequestMethod.GET})
     public void doDoctorProfile (ModelMap model) {
    
     }
     
     @RequestMapping(value="/patientprofile.do", method={RequestMethod.GET})
     public void doPatientProfile (ModelMap model) {
    
     }
     
     @RequestMapping(value="/patientbasicinfo.do", method={RequestMethod.GET})
     public void doPatientBasicInfo (ModelMap model) {
    
     }
     
     @RequestMapping(value="/patientmedication.do", method={RequestMethod.GET})
     public void doPatientMedication (ModelMap model) {
    
     }
     
     @RequestMapping(value="/patientsymptoms.do", method={RequestMethod.GET})
     public void doPatientSymptoms (ModelMap model) {
    
     }
     
     @RequestMapping(value="/patientallergies.do", method={RequestMethod.GET})
     public void doPatientAllergies (ModelMap model) {
    
     }
     
     @RequestMapping(value="/patientprocedures.do", method={RequestMethod.GET})
     public void doPatientProcedures (ModelMap model) {
    
     }
     
     @RequestMapping(value="/searchdoc.do", method={RequestMethod.GET})
     public void doSearch (ModelMap model) {
    
     }  
     
     @RequestMapping(value="/askquestion.do", method={RequestMethod.GET})
     public void doAskQuestion (ModelMap model) {
    
     }
     
     @RequestMapping("/logout.do")
     public void doLogout (HttpServletResponse response) throws IOException {
         response.sendRedirect("signin.do");
     }
 
     @Autowired
     public void setTravelLogDAO (TravelLogDAO dao) {
         this.dao = dao;
     }
 
     /**
      * Method establishes the transformation of incoming date strings into Date objects
      * @param binder the spring databinder object that we connect to the date editor
      */
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         dateFormat.setLenient(true);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
     }
 
 
 }
