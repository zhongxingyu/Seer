/**
 * The Subversion Authentication Parse Module (SAPM for short).
 *
 * Copyright (c) 2010, 2011 by SoftwareEntwicklung Beratung Schulung (SoEBeS)
 * Copyright (c) 2010, 2011 by Karl Heinz Marbaise
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.soebes.subversion.sapm;
 
 import java.util.ArrayList;
 
 /**
  * Convenience class for the grammar to handle a list of {@link User}s.
  *
  * @author Karl Heinz Marbaise
  *
  */
 public class Users {
 
     private ArrayList<User> usersList;
 
     public Users() {
         setUsersList(new ArrayList<User>());
     }
 
     public void setUsersList(ArrayList<User> usersList) {
         this.usersList = usersList;
     }
 
     public ArrayList<User> getUsersList() {
         return usersList;
     }
 
     /**
      * Add a single user to the list of users.
      * @param user The user which will be added.
      */
     public void add(User user) {
         getUsersList().add(user);
     }
 
     /**
      * Check to see if a given username is within
      * the list of users.
      * Checking will be done case sensitive.
      * @param userName The name of the user.
      * @return true if the user has been found false otherwise.
      */
     public boolean hasUser(String userName) {
         boolean result = false;
         for (User item : getUsersList()) {
             if (item.getName().equals(userName)) {
                 result = true;
             }
         }
         return result;
     }
 
     public User getUser(String userName) {
         User result = null;
         for (User item : getUsersList()) {
             if (item.getName().equals(userName)) {
                 result = item;
             }
         }
         return result;
     }
 }
