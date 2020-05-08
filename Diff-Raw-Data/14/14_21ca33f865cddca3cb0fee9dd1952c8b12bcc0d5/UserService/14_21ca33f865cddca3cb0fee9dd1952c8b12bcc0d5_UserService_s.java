 package com.clip.web.service;
 
 import com.clip.core.bean.ReturnBean;
 import com.clip.web.dao.UserDao;
 import com.clip.web.model.User;
 import net.sf.json.JSONObject;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.Resource;
 
 @Service
 public class UserService {
     @Resource
     private UserDao userDao;
 
     public User getUserByUid(String uid) {
         return userDao.getUserByUid(uid);
     }
 
     public User addUser(User user) {
        userDao.addUser(user);
        return user;
     }
 
     public Boolean updateToken(String token) {
         return false;
     }
 
     public User getUser(String type, String token) {
         return userDao.getUser(type, token);
     }
 
     public User getUserByUidAndType(String uid, String type) {
         return userDao.getUserByUidAndType(uid, type);
     }
 
     public User getUserById(Integer id) {
         return userDao.getUserById(id);
     }
 
     public User getUserByName(String name) {
         return userDao.getUserByName(name);
     }
 
 //    public User addUser(String uid, String type, String token) {
 //        return userDao.addUser(uid, type, token);
 //    }
 
     public JSONObject updateName(Integer id, String username) {
         Boolean success = userDao.updateName(id, username);
         JSONObject resultJson = JSONObject.fromObject(success);
         JSONObject dataJson = resultJson.optJSONObject("data");
         JSONObject result = new ReturnBean(true, null, dataJson).toJSONObject();
         return result;
     }
//
     public JSONObject updateEmail(Integer id, String email) {
         Boolean success = userDao.updateEmail(id, email);
         JSONObject resultJson = JSONObject.fromObject(success);
         JSONObject dataJson = resultJson.optJSONObject("data");
         JSONObject result = new ReturnBean(true, null, dataJson).toJSONObject();
         return result;
     }
 //
 //    public JSONObject updateRemind(Integer id, Integer remind) {
 //        Boolean success = userDao.updateRemind(id, remind);
 //        JSONObject resultJson = JSONObject.fromObject(success);
 //        JSONObject dataJson = resultJson.optJSONObject("data");
 //        JSONObject result = new ReturnBean(true, null, dataJson).toJSONObject();
 //        return result;
 //    }
 //
 //    public JSONObject updateToken(Integer id, String type, String token) {
 //        Boolean success = userDao.updateToken(id, type, token);
 //        JSONObject resultJson = JSONObject.fromObject(success);
 //        JSONObject dataJson = resultJson.optJSONObject("data");
 //        JSONObject result = new ReturnBean(true, null, dataJson).toJSONObject();
 //        return result;
 //    }
 
 }
