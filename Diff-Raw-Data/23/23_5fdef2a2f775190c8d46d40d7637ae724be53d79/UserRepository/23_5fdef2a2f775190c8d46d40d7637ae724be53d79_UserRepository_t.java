package org.triple_brain.module.repository.user;
 import org.codehaus.jettison.json.JSONException;
 import org.triple_brain.module.model.User;
 import org.codehaus.jettison.json.JSONObject;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public interface UserRepository {
     void save(User user);
     User findById(String id) throws NonExistingUserException;
     User findByUsername(String username) throws NonExistingUserException;
     User findByEmail(String email) throws NonExistingUserException;
     boolean usernameExists(String username);
     boolean emailExists(String email);
     JSONObject findByIdAsJson(String id) throws NonExistingUserException, JSONException;
 }
