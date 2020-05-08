 /**
  *  Copyright (c) 2012 Nord Trading Ltd.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.nordpos.mobile.stripes.dao;
 
 import com.nordpos.mobile.stripes.model.People;
 import com.nordpos.mobile.stripes.util.PasswordUtils;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import net.sf.persist.Persist;
 
 /**
  *
  * @author Andrey Svininykh <svininykh@gmail.com>
  */
 public class PeoplePersist extends BaseJDBCPersist {
 
     public PeoplePersist(ServletContext servletContext) {
         try {
             persist = new Persist(getConnection(servletContext));
         } catch (Exception ex) {
            Logger.getLogger(ApplicationPersist.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public People findUser(String login, String password) {
         People validUser;
         if (password.isEmpty()) {
             validUser = persist.read(People.class,
                     "SELECT * FROM PEOPLE WHERE NAME = ? AND APPPASSWORD IS NULL",
                     login);
         } else {
             validUser = persist.read(People.class,
                     "SELECT * FROM PEOPLE WHERE NAME = ? AND APPPASSWORD = ?",
                     login,
                     PasswordUtils.hashString(password));
         }
 
         return validUser;
     }
 
     public People findUser(String id) {
         return persist.read(People.class,
                 "SELECT * FROM PEOPLE WHERE ID = ?",
                 id);
     }
 
     public List<People> findUsers() {
         return persist.readList(People.class,
                 "SELECT * FROM PEOPLE ORDER BY NAME");
     }
 
     public Integer countUsers() {
         return persist.read(Integer.class,
                 "SELECT COUNT(ID) FROM PEOPLE");
     }
 }
