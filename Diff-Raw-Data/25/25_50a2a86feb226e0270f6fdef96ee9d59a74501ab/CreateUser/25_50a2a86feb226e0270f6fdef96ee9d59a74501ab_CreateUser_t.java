 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.pages.user;
 
 import com.preppa.web.data.Gender;
 import com.preppa.web.data.UserObDAO;
 import com.preppa.web.entities.Role;
 import com.preppa.web.entities.User;
 import com.preppa.web.pages.Index;
 import com.preppa.web.services.EmailService;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import org.apache.commons.mail.EmailException;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Log;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.PasswordField;
 import org.apache.tapestry5.hibernate.HibernateSessionManager;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
import org.chenillekit.tapestry.core.components.AjaxCheckbox;
 import org.chenillekit.tapestry.core.components.DateSelector;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.security.providers.dao.SaltSource;
 import org.springframework.security.providers.encoding.ShaPasswordEncoder;
 
 /**
  *
  * @author newtonik
  */
 public class CreateUser {
 
     
     private User user;
     @Property
     private User auser;
     @InjectPage
     private Index index;
     @Inject
     private UserObDAO userDAO;
     @Inject
     private Messages messages;
     @Property
     private String fLogin;
     @Property
     private String ffName;
     @Property
     private String flName;
     @Property
     private Gender fgender;
     @Property
     private Date fdob;
     @Property
     private String femail;
     @Persist
     @Property
     private String fpass1;
     @Persist
     @Property
     private String fpass2;
     @Inject
     private SaltSource salt;
     @Inject
     private HibernateSessionManager sessionManager;
     @Inject
     private EmailService emailer;
     @Property
     private int currYear;
     @Component(parameters = {"value=fdob", "firstYear=1930", "lastYear=prop:currYear"})
     private DateSelector datefield;
     @Component
     private PasswordField passwordField;
     @Component
     private Form userform;
     @Property
     private Boolean passKap;
     @Property
     private Boolean userAgreement;
    @Property
    private Boolean isThirteen;

 
     void CreateUser() {
         Calendar cal = Calendar.getInstance();
         currYear = cal.get(Calendar.YEAR);
     }
 
     //private Timestamp currentTime;
     void onActivate() {
         this.auser = new User();
         //Setting the current Year to use in Date Selector field for dob
         Calendar cal = Calendar.getInstance();
         currYear = cal.get(Calendar.YEAR);
     }
 
     Object onPassivate() {
         return auser;
     }
     
     void onValidateForm() {
      long birth = fdob.getTime();
      Date tester = new Date();
      long current = tester.getTime();

      long age = (current-birth);
      // Get difference in days
      long diffDays = age/(24*60*60*1000);  // 7

      /*System.out.println("Day Age is " + diffDays);
      System.out.println("Age is " + diffDays/365);
      System.out.println("isThirteen is " + isThirteen);*/

      age = diffDays/365;
      
      if(age < 13 || !isThirteen) {
          userform.recordError(passwordField, messages.get("age-min-message"));
      }

       if(!fpass1.equals(fpass2)) {
             fpass1 = null;
             fpass2 = null;
 
             userform.recordError(passwordField, messages.get("passwords-dont-match"));
 
         }
       if(!passKap) {
           userform.recordError("failed kaptcha, try again");
       }
       if(!userAgreement) {
           userform.recordError("You cannot create an Account without agreeing to the site policies!");
       }
     }
    
     @CommitAfter
     @Log
     Object onSuccess() throws EmailException {
         //user = new User();
 
         auser.setPassword(fpass1);
         auser.setLoginId(fLogin);
         auser.setUsername(fLogin);
         auser.setEmail(femail);
         auser.setDob(fdob);
         auser.setLastName(flName);
         auser.setFirstName(ffName);
         Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
         auser.setCreatedAt(now);
         auser.setUpdatedAt(now);
         auser.setEnabled(false);
         userDAO.doSave(auser);
       
         RegisterUser(auser);
         //session.persist(user);
        emailer.sendSendRegistrationEmail(auser);
 
         this.user = auser;
         return "loginpage";
     }
 
    public void RegisterUser(User user) {
         if( user != null)
         {
            // userDAO.doSave(user);
             Object salter = salt.getSalt(auser);
             //String passwordToencode = user.getPassword() + user.getUsername();
              ShaPasswordEncoder enc = new ShaPasswordEncoder();
              System.out.println(salter);
             String encodpassword = enc.encodePassword(user.getPassword(), salter);
             System.out.println(encodpassword);
             user.setPassword(encodpassword);
 
             user.setEnabled(false);
             user.setAccountNonExpired(true);
             user.setAccountNonLocked(true);
             user.setCredentialsNonExpired(true);
 
             Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
             String basiccode = now.toString() + user.getUsername() + user.getFirstName();
             String activationcode = enc.encodePassword(basiccode, salter);
             user.setActivationcode(activationcode);
             //find a the default role, create it if it doesn't exist
             Session session = sessionManager.getSession();
             Role r = (Role) session.createCriteria(Role.class).add(
                     Restrictions.eq("authority", "ROLE_USER")).uniqueResult();
 
             if (r == null)
             {
 //                logger.debug("role not found, creating");
 
                 r = new Role();
 
                 r.setAuthority("ROLE_USER");
 
                 session.saveOrUpdate(r);
             }
 
             auser.setRoles(new HashSet<Role>());
 
             auser.getRoles().add(r);
 
             //session.saveOrUpdate(u);
             userDAO.doSave(auser);
         }
 
         //logger.debug("returning user " + user.getUsername());
 
         //sessionManager.commit();
 
 
 
 
     }
 
 }
