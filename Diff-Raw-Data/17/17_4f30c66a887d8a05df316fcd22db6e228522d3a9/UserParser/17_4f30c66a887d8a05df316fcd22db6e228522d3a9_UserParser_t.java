 package org.carlspring.strongbox.xml.parsers;
 
 import org.carlspring.strongbox.security.jaas.User;
 import org.carlspring.strongbox.security.jaas.Credentials;
 
 import java.util.List;
 
 import com.thoughtworks.xstream.XStream;
 
 /**
  * @author mtodorov
  */
 public class UserParser
         extends GenericParser<User>
 {
 
     public XStream getXStreamInstance()
     {
         XStream xstream = new XStream();
         xstream.autodetectAnnotations(true);
         xstream.alias("user", User.class);
         xstream.alias("users", List.class);
        /*xstream.alias("roles", List.class);*/
         xstream.alias("role", String.class);
         xstream.alias("credentials", Credentials.class);
 
         return xstream;
     }
 
 }
