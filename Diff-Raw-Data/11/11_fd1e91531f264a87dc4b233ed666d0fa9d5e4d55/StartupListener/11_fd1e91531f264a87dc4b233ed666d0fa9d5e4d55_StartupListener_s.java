 package com.nkhoang.gae.listener;
 
 import com.nkhoang.gae.manager.ItemManager;
 import com.nkhoang.gae.manager.UserManager;
 import com.nkhoang.gae.model.Item;
 import com.nkhoang.gae.model.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * <p/>
  * StartupListener class used to initialize and database settings and populate
  * any application-wide drop-downs.
  * <p/>
  * <p/>
  * Keep in mind that this listener is executed outside of
  * OpenSessionInViewFilter, so if you're using Hibernate you'll have to
  * explicitly initialize all loaded data at the GenericDao or service level to
  * avoid LazyInitializationException. Hibernate.initialize() works well for
  * doing this.
  *
  * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
  */
 public class StartupListener implements ServletContextListener {
     private static final Logger LOGGER = LoggerFactory.getLogger(StartupListener.class);
 
     @SuppressWarnings({"unchecked"})
     public void contextInitialized(ServletContextEvent event) {
         LOGGER.debug("Initializing context...");
 
         ServletContext context = event.getServletContext();
 
         setupContext(context);
     }
 
     public void contextDestroyed(ServletContextEvent servletContextEvent) {
 
     }
 
     /**
      * This method uses the LookupManager to lookup available roles from the
      * data layer.
      *
      * @param context The servlet context
      */
     public static void setupContext(ServletContext context) {
        LOGGER.debug("Check default user ...");
        ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
         ItemManager itemService = (ItemManager) ctx.getBean("itemService");
 
         UserManager userService = (UserManager) ctx.getBean("userManager");
         PasswordEncoder passwordEncoder = (PasswordEncoder) ctx.getBean("passwordEncoder");
 
         userService.clearAll();
 
         List<User> users = userService.listAll();
         if (users != null && users.size() > 0) {
             LOGGER.debug("Default users existed");
         } else {
             LOGGER.debug("Creating default users...");
             User admin = new User();
             admin.setEnabled(true);
             admin.setUsername("admin");
             admin.setFirstName("Hoang");
             admin.setLastName("Nguyen");
             List<String> roles = new ArrayList<String>(0);
             roles.add("ROLE_ADMIN");
             roles.add("ROLE_USER");
             // set roles
             admin.setRoleNames(roles);
             admin.setPassword(passwordEncoder.encodePassword("admin", null));
 
             userService.save(admin);
 
             if (admin.getId() != null) {
                 LOGGER.debug("Saving default users [ok]");
             }
         }
     }
 
     public static Item createItem(Long ipId) {
         Item item = new Item();
         item.setCode("s001");
         item.setDescription("A Description");
         item.setPrice(123L);
         Calendar calendar = Calendar.getInstance();
         item.setDateAdded(calendar.getTime());
         item.setThumbnail("https://docs.google.com/File?id=d5brrvd_932dsqqhrc8_b");
         item.setThumbnailBig("http://docs.google.com/File?id=d5brrvd_1064htxmhbdd_b");
 
         item.getPictureIds().add(ipId);
 
         return item;
     }
 }
