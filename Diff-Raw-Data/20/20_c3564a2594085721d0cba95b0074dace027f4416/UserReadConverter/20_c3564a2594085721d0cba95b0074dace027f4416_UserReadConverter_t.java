 package org.netvogue.ecommerce.persistence.mongo.converters;
 
 import org.netvogue.ecommerce.domain.model.Role;
 import org.netvogue.ecommerce.domain.model.SubscriptionType;
 import org.netvogue.ecommerce.domain.model.User;
 import org.springframework.core.convert.converter.Converter;
 
 import com.mongodb.DBObject;
 
 public class UserReadConverter implements Converter<DBObject, User> {
 
   public User convert(final DBObject dbObj) {
     User user = new User();
     user.setId(dbObj.get("_id").toString());
     user.setFirstName((String) dbObj.get("firstName"));
     user.setLastName((String) dbObj.get("lastName"));
     user.setUsername((String) dbObj.get("userName"));
     user.setPrimarycontact((String) dbObj.get("primarycontact"));
     user.setAboutUs((String) dbObj.get("aboutUs"));
     user.setAddress((String) dbObj.get("address"));
     user.setCity((String) dbObj.get("city"));
     user.setState((String) dbObj.get("state"));
     user.setZipCode((Integer) dbObj.get("zipCode"));
     user.setCountry((String) dbObj.get("country"));
     user.setMobileNo((Long) dbObj.get("mobileNo"));
     user.setTelephoneNo1((Long) dbObj.get("telephoneNo1"));
     user.setTelephoneNo2((Long) dbObj.get("telephoneNo2"));
     user.setActive((Boolean) dbObj.get("active"));
     user.setSubscirptionType(SubscriptionType.valueOf((String)dbObj.get("subscriptionType")));
     user.setEncodedPassword((String) dbObj.get("password"));
     user.setSalt((Long) dbObj.get("salt"));
     user.setRole(Role.valueOf( (String) dbObj.get("role")));
    user.setEmail((String) dbObj.get("email"));
     return user;
   }
 
 }
