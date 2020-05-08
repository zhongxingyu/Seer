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
  * This class describes an AccessRule which comprises of an repository name and
  * a repository path which is usually defined like the following in the
  * Subversion Access file.
  *
  * <pre>
  * [repository:/trunk]
  * &#64;group = rw
  * harry = r
  * </pre>
  *
  * Example:
  * <pre class="javacode">
  * User userBrian = UserFactory.createInstance("brian");
  * Group group = new Group("group");
  * group.add(userBrian)
  *
  * User userHarry = UserFactory.createInstance("harry");
  *
  * AccessRule accessRule = new AccessRule("repository", "/test/trunk");
  * accessRule.add(userHarry, AccessLevel.READ);
  * accessRule.add(group, AccessLevel.READ_WRITE);
  *
  * AccessLevel level = accessRule.getAccess("hugo", "repository", "/trunk/test/Test.java");
  * ....
  * </pre>
  *
  * @author Karl Heinz Marbaise
  *
  */
 public class AccessRule {
 
     private ArrayList<Access> accessList;
 
     /**
      * The name of the repository which this AccessRule represents.
      */
     private String repositoryName;
     /**
      * The path inside the repository which is represented by this AccessRule.
      */
     private String repositoryPath;
 
     /**
      * This is used if you have no particular repository defined for example
      * like the following.
      *
      * <pre>
      * [/test/trunk]
      * ...
      * </pre>
      *
      * @param path
      */
     public AccessRule(String path) {
         super();
         init(null, path);
     }
 
     /**
      * This is used if you have given both the repository name
      * and the path within the repository.
      * @param repositoryName The name of the repository.
      * @param path The path inside the repository.
      */
     public AccessRule(String repositoryName, String path) {
         super();
         init(repositoryName, path);
     }
 
     private void init(String repositoryName, String path) {
         if (!path.endsWith("/")) {
             path += "/";
         }
         setRepositoryName(repositoryName);
         setRepositoryPath(path);
         setAccessList(new ArrayList<Access>());
     }
 
     public String getRepositoryName() {
         return repositoryName;
     }
 
     public void setRepositoryName(String repositoryName) {
         this.repositoryName = repositoryName;
     }
 
     public String getRepositoryPath() {
         return repositoryPath;
     }
 
     public void setRepositoryPath(String path) {
         this.repositoryPath = path;
     }
 
     public void setAccessList(ArrayList<Access> accessList) {
         this.accessList = accessList;
     }
 
     public ArrayList<Access> getAccessList() {
         return accessList;
     }
 
     /**
      * Add the user to the access list with it's appropriate {@link AccessLevel}.
      *
      * @param user The user which will be used.
      * @param accessLevel The accessLevel which will be given to the user.
      */
     public void add(User user, AccessLevel accessLevel) {
         getAccessList().add(new Access(user, accessLevel));
     }
 
     /**
      * Add the user to the access list with it's appropriate {@link AccessLevel}
      * but this rule means not this user (~user)
      * @param user The user which it shouldn't be.
      * @param accessLevel The level of access for the non users.
      */
     public void addNegative(User user, AccessLevel accessLevel) {
         getAccessList().add(new Access(user, accessLevel, true));
     }
 
     /**
      * Add the given group to the access list with it's appropriate {@link AccessLevel}.
      * @param group The group which will be added to the access list.
      * @param accessLevel The accessLevel which will be given to the group.
      */
     public void add(Group group, AccessLevel accessLevel) {
         getAccessList().add(new Access(group, accessLevel));
     }
 
     /**
      * Add the given group to the access list with it's appropriate {@link AccessLevel}.
      * Negate the rule.
      * <pre>
      * ~group = rw
      * </pre>
      * This means that everybody who is NOT member of the group will
      * get the permission (rw).
      * @param group The group which will be added to the access list.
      * @param accessLevel The accessLevel which will be given to the group.
      */
     public void addNegative(Group group, AccessLevel accessLevel) {
         getAccessList().add(new Access(group, accessLevel, true));
     }
 
     /**
      * Convenience method for usage in grammar.
      * @param access The {@link Access}
      */
     public void add(Access access) {
         getAccessList().add(access);
     }
 
     /**
      * Convenience method if you have a {@link User} object instead of user name as a string.
      * @param user The user for which you like to know the access level.
      * @param repository The repository which will be checked for.
      * @param path The path within the repository.
      * @return The AccessLevel which represents the permission for the given user in
      *  the repository and the given path.
      */
     public AccessLevel getAccess(User user, String repository, String path) {
         return getAccess(user.getName(), repository, path);
     }
 
     /**
      * Get the {@link AccessLevel} for the user who want's to get access to the path
      * inside the given repository.
      *
      * @param user The user who will be checked against the permission rules.
      * @param repository The repository to which the user tries to get access.
      * @param path The path within the repository.
      * @return AccessLevel of the user within the given path and repository.
      */
     public AccessLevel getAccess(String user, String repository, String path) {
         AccessLevel result = AccessLevel.NOTHING;
         if (getRepositoryName() == null) {
            Path repositoryPath = new Path(getRepositoryPath());
            if (repositoryPath.contains(path)) {
                 result = getAccessForPrincipal(user);
             }
         } else {
             if (getRepositoryName().equals(repository)) {
                Path repositoryPath = new Path(getRepositoryPath());
                if (repositoryPath.contains(path)) {
                     result = getAccessForPrincipal(user);
                 }
             }
         }
 
         return result;
     }
 
     public AccessLevel getAccessForPrincipal(User user) {
         return getAccessForPrincipal(user.getName());
     }
 
     /**
      * Will get the {@link AccessLevel} for the given user.
      *
      * @param user The user which will be searched for.
      * @return The {@link AccessLevel} of the given user @{link AccessLevel.NOTHING} if
      *         the user will not be found.
      */
     public AccessLevel getAccessForPrincipal(String user) {
         AccessLevel result = AccessLevel.NOTHING;
         for (Access item : getAccessList()) {
             if (item.isNegativeRule()) {
                 if (!item.getPrincipal().contains(user)) {
                     result = item.getLevel();
                 }
             } else {
                 if (item.getPrincipal().contains(user)) {
                     result = item.getLevel();
                 }
             }
         }
         return result;
     }
 
 
     public String toString() {
         StringBuilder result = new StringBuilder("[");
         if (getRepositoryName() != null) {
             result.append(getRepositoryName() + ":");
         }
         result.append(getRepositoryPath() + "]" + "\n");
         for (Access item : getAccessList()) {
             result.append(item.getPrincipal() + " = " + item.getLevel() + "\n");
         }
         return result.toString();
     }
 }
