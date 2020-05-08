 package ru.forxy.service.impl;
 
 import ru.forxy.service.pojo.User;
 import ru.forxy.service.IUserService;
 
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 import javax.xml.ws.WebServiceException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class UserServiceImpl implements IUserService {
 
     private static Map<Integer, User> users = new HashMap<Integer, User>(3);
 
     static {
         users.put(1, new User(1, "Alfred"));
         users.put(2, new User(2, "Bob"));
         users.put(3, new User(3, "Cliff"));
         users.put(4, new User(4, "Daniel"));
         users.put(5, new User(5, "Eleanor"));
     }
 
     @Override
     public List<User> getUsers() {
         return new ArrayList<User>(users.values());
     }
 
     @Override
     public User getUser(Integer id) {
         return users.get(id);
     }
 
     @Override
     public Response getBadResponse() {
         return Response.status(Response.Status.BAD_REQUEST).build();
     }
 
     @Override
     public void updateUser(User user) {
         if (user.getId() != null) {
             users.put(user.getId(), user);
         } else {
             throw new WebServiceException("User's id is null");
         }
     }
 
     @Override
     public void addUser(User user) {
         if (user.getId() != null) {
             if (!users.containsKey(user.getId())) {
                 users.put(user.getId(), user);
             } else {
                 throw new WebServiceException("User with id " + user.getId() + " already exist");
             }
         } else {
             throw new WebServiceException("User's id is null");
         }
     }
 
     @Override
    public Response deleteUser(@PathParam("id") Integer id) {
         users.remove(id);
         return Response.ok().build();
     }
 }
