 package fi.kl.shop.domain;
 
 import javax.validation.constraints.NotNull;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.layers.repository.mongo.RooMongoEntity;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooMongoEntity
 public class User {
 
     @NotNull
     private String username;
 
     @NotNull
     private String address;
 }
