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
 
 import com.nordpos.mobile.stripes.model.Role;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import net.sf.persist.Persist;
 
 /**
  *
  * @author Andrey Svininykh <svininykh@gmail.com>
  */
 public class RolePersist extends BaseJDBCPersist {
 
     public RolePersist(ServletContext servletContext) {
         try {
             persist = new Persist(getConnection(servletContext));
         } catch (Exception ex) {
            Logger.getLogger(ApplicationPersist.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public List<Role> findRoles() {
         return persist.readList(Role.class,
                 "SELECT ID, NAME FROM ROLES ORDER BY NAME");
     }
 
 }
