 package ch.heigvd.gamification.rest;
 
 import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
 import java.util.HashSet;
 import java.util.Set;
 import javax.ws.rs.ApplicationPath;
 //import javax.ws.rs.core.Application;
 
 /**
  *
  * @author Khaled Basbous
  */
 @ApplicationPath("/api")
 public class RESTAPI extends javax.ws.rs.core.Application {
 
     @Override
     public Set<Class<?>> getClasses() {
         final Set<Class<?>> classes = new HashSet<Class<?>>();
         // register root resources/providers
         classes.add(GamificationExceptionMapper.class);
         classes.add(ApplicationResource.class);
         classes.add(PlayerResource.class);
        classes.add(EventResource.class);
        classes.add(RuleResource.class);
         return classes;
     }
 }
