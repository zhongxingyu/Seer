 package org.netvogue.ecommerce.persistence.mongo.converters;
 
 import org.netvogue.ecommerce.domain.model.User;
 import org.netvogue.ecommerce.persistence.util.Util;
 import org.springframework.core.convert.converter.Converter;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 public class UserWriteConverter implements Converter<User, DBObject> {
 
   public DBObject convert(final User user) {
 
     BasicDBObject dbOject = new BasicDBObject();
 
     if (user.getId() != null) {
       dbOject.put("_id", Util.massageAsObjectId(user.getId()));
     }
 
     dbOject.put("firstName", user.getFirstName());
     dbOject.put("lastName", user.getLastName());
     dbOject.put("userName", user.getUsername());
    dbOject.put("email", user.getEmail());
     dbOject.put("password", user.getEncodedPassword());
     dbOject.put("salt", user.getSalt());
     dbOject.put("profilePicLink", user.getProfilePicLink());
     dbOject.put("primarycontact", user.getPrimarycontact());
     dbOject.put("aboutUs", user.getAboutUs());
     dbOject.put("address", user.getAddress());
     dbOject.put("city", user.getCity());
     dbOject.put("state", user.getState());
     dbOject.put("zipCode", user.getZipCode());
     dbOject.put("country", user.getCountry());
     dbOject.put("mobileNo", user.getMobileNo());
     dbOject.put("telephoneNo1", user.getTelephoneNo1());
     dbOject.put("telephoneNo2", user.getTelephoneNo2());
     dbOject.put("active", user.isActive());
     dbOject.put("subscriptionType", user.getSubscirptionType().toString());
     dbOject.put("role", user.getRole().toString());
     dbOject.put("_class", User.class.getName());
 
     return dbOject;
   }
 
 }
