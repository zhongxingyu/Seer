 package net.qldarch.service.rdf;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.Pattern;
 
 public class Validators {
     public static Logger logger = LoggerFactory.getLogger(Validators.class);
 
    public static final Pattern USERNAME_REGEX = Pattern.compile("[a-zA-Z0-9_.-]{3,}");
 
     public static String username(String username) {
         if (username == null) {
             return "";
         } else if (USERNAME_REGEX.matcher(username).matches()) {
             return username;
         } else {
             logger.warn("Invalid username provided: {}", username);
             throw new IllegalArgumentException("Invalid username");
         }
     }
 }
