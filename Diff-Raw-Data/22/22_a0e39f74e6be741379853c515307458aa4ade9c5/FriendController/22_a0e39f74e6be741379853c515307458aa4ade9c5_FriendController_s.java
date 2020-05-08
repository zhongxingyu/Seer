 /*
  * Copyright (c) 2013 Lucas Holt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 
 package com.justjournal.ctl.api;
 
 import com.justjournal.WebLogin;
 import com.justjournal.db.*;
 import org.apache.log4j.Logger;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Manage Friends
  *
  * @author Lucas Holt
  */
 @Controller
 @RequestMapping("/api/friend")
 public class FriendController {
     private static final Logger log = Logger.getLogger(FriendController.class);
 
     // TODO: refactor to return user objects?
 
     /**
      * @param id       username
      * @param response http response
      * @return List of usernames as strings
      */
     @Cacheable(value = "friends", key = "id")
    @RequestMapping("/api/friend/{id}")
     @ResponseBody
    public Collection<String> getById(@PathVariable String id, HttpServletResponse response) {
         try {
             return UserDao.friends(id);
         } catch (Exception e) {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             return null;
         }
     }
 
     @CacheEvict(value = "friends", key = "friend")
     @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
     @ResponseBody
     public Map<String, String> put(@RequestParam String friend, HttpSession session, HttpServletResponse response) {
         if (!WebLogin.isAuthenticated(session)) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return java.util.Collections.singletonMap("error", "The login timed out or is invalid.");
         }
 
         try {
             UserTo friendUser = UserDao.get(friend);
 
             if (friendUser == null)
                 return java.util.Collections.singletonMap("error", "Could not find friend's username");
 
             String sqlStatement = "Insert INTO friends (id, friendid) values('" + WebLogin.currentLoginId(session) + "','" + friendUser.getId() + "');";
             int rowsAffected = SQLHelper.executeNonQuery(sqlStatement);
             if (rowsAffected == 1)
                 return java.util.Collections.singletonMap("status", "success");
 
             return java.util.Collections.singletonMap("error", "Unable to add friend");
         } catch (Exception e) {
             log.error(e.getMessage());
             return java.util.Collections.singletonMap("error", "Could not find friend's username");
         }
 
     }
 
     // TODO: api makes it hard to selectively delete cache entries
     @CacheEvict(value = "friends", allEntries = true)
     @RequestMapping(method = RequestMethod.DELETE)
     public
     @ResponseBody
     Map<String, String> delete(@RequestBody FriendTo friend, HttpSession session, HttpServletResponse response) throws Exception {
 
 
         if (!WebLogin.isAuthenticated(session)) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return java.util.Collections.singletonMap("error", "The login timed out or is invalid.");
         }
 
         friend.setOwnerId(WebLogin.currentLoginId(session));
 
         if (FriendsDao.delete(friend)) {
             return java.util.Collections.singletonMap("status", "success");
         }
 
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return java.util.Collections.singletonMap("error", "Error deleting friend");
     }
 }
