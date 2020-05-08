 package dk.tb.servlets;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import javax.inject.Qualifier;
 
 @Qualifier
 @Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
 public @interface WebSocket {}
