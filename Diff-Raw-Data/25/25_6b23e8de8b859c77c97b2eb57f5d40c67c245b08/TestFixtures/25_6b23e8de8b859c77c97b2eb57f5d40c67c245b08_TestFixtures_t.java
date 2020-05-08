 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.model.fixtures;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.lang3.RandomStringUtils;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.model.entity.Property;
 import org.jtalks.common.model.entity.User;
 import org.jtalks.poulpe.model.entity.*;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Provides unified way for creating text fixtures.<br> <br> This class is not under test source folder because it
  * should be accessible for all components.
  *
  * @author Kirill Afonin
  * @author Alexey Grigorev
  */
 public final class TestFixtures {
 
     private static final Random RANDOM = new Random();
     private static final int SECTIONS_COUNT = 10;
     private static final int BRANCH_COUNT_MIN = 5;
     private static final int BRANCH_COUNT_MAX = 15;
     private static final int USERS_COUNT = 5;
     //private static final int POST_LIMIT_COUNT = 1000;
     private static final int LENGTH = 10;
 
     /** No need to create instance, only static methods used */
     private TestFixtures() {
     }
     
     /** @return branch with random name and description beloning to random section with moderators group */
     public static PoulpeBranch branch() {
         PoulpeBranch newBranch = new PoulpeBranch(random(), random());
         newBranch.setSection(section());
         newBranch.setModeratorsGroup(group());
         return newBranch;
     }
 
     /** @return branch with id = 1L, random name and description belong to random section and moderators group */
     public static PoulpeBranch branchWithId() {
         PoulpeBranch newBranch = new PoulpeBranch(random(), random());
         newBranch.setSection(section());
         newBranch.setModeratorsGroup(group());
         newBranch.setId(1L);
         return newBranch;
     }
 
     /** @return topic type with random name and description */
     public static TopicType topicType() {
         return new TopicType(random(), random());
     }
 
     /**
      * @param type of the component
      * @return component with random name and description and with given type
      */
     public static Component component(ComponentType type) {
         ComponentBase base = new ComponentBase(type);
         return base.newComponent(random(), random());
     }
 
     /** @return list of components of all {@link ComponentType} values */
     public static List<Component> allComponents() {
         List<Component> result = Lists.newArrayList();
 
         for (ComponentType componentType : ComponentType.values()) {
             result.add(component(componentType));
         }
 
         return result;
     }
 
     /** @return component with {@link ComponentType} = {@link ComponentType#FORUM} */
     public static Jcommune jcommune() {
         return (Jcommune) component(ComponentType.FORUM);
     }
 
     /**
      * @param sectionsAmount amount of sections to be add to result
      * @return component with {@link ComponentType} = {@link ComponentType#FORUM} and with sections and branches
      */
     public static Jcommune jcommuneWithSections(int sectionsAmount) {
         Jcommune jcommune = jcommune();
 
         for (int i = 0; i < sectionsAmount; i++) {
             PoulpeSection section = sectionWithBranches();
             jcommune.addOrUpdateSection(section);
         }
 
         return jcommune;
     }
 
     /** @return jcommune component with SECTIONS_COUNT sections */
     public static Jcommune jcommuneWithSections() {
         return jcommuneWithSections(SECTIONS_COUNT);
     }
 
     /** @return section with random amount of branches (from BRANCH_COUNT_MIN to BRANCH_COUNT_MAX) */
     public static PoulpeSection sectionWithBranches() {
         return sectionWithBranches(BRANCH_COUNT_MIN + randomInt(BRANCH_COUNT_MAX - BRANCH_COUNT_MIN));
     }
 
     /**
      * @param branchesAmount amount of branches in section
      * @return section with random name and given amount of branches
      */
     public static PoulpeSection sectionWithBranches(int branchesAmount) {
         PoulpeSection section = new PoulpeSection(random());
 
         for (int i = 0; i < branchesAmount; i++) {
             PoulpeBranch branch = branch();
             branch.setSection(section);
             section.addOrUpdateBranch(branch);
         }
 
         return section;
     }
 
     /** @return component of random {@link ComponentType} */
     public static Component randomComponent() {
         return component(randomComponentType());
     }
 
     /** @return component of random {@link ComponentType} with id = 1L */
     public static Component randomComponentWithId() {
         Component component = component(randomComponentType());
         component.setId(1L);
         return component;
     }
 
     /** @return random {@link ComponentType} value */
     public static ComponentType randomComponentType() {
         ComponentType[] types = ComponentType.values();
         return types[randomInt(types.length)];
     }
 
     /** @return {@link ComponentBase} with random {@link ComponentType} */
     public static ComponentBase baseComponent() {
         return new ComponentBase(randomComponentType());
     }
 
     /** @return property with random name, description and validation rule */
     public static Property property() {
         Property property = new Property(random(), random());
         property.setValidationRule(random());
         return property;
     }
 
     /** @return section with random name */
     public static PoulpeSection section() {
         return new PoulpeSection(random());
     }
 
     /**
      * @param username of the user
      * @return user with the given username, generated email and empty salt
      */
     public static PoulpeUser user(String username) {
         String email = username + "@" + random() + ".com";
         return new PoulpeUser(username, email, random(), "");
     }
 
     /**
      * @param username user's name
      * @param email    user's email
      * @return user with the given username, email and generated empty salt
      */
     public static PoulpeUser user(String username, String email) {
         return new PoulpeUser(username, email, random(), "");
     }
 
     /**
      * @param username user's name
      * @param password user's password
      * @param email    user's email
      * @return user with the given username, email and generated empty salt
      */
     public static PoulpeUser user(String username, String password, String email) {
         return new PoulpeUser(username, email, password, "");
     }
 
     /** @return user with random name, email, password and empty salt */
     public static PoulpeUser user() {
        PoulpeUser user = user(random());
        user.setEnabled(true);
        return user;
     }
 
     /** @return group with randoms users */
     public static Group groupWithUsers() {
         List<PoulpeUser> users = usersListOf(USERS_COUNT);
         Group group = group();
         group.setUsers((List<User>) (Object) users);
         return group;
     }
 
     /**
      * @param n amount of users
      * @return list of users
      */
     public static List<PoulpeUser> usersListOf(int n) {
         int count = n;
         List<PoulpeUser> result = Lists.newArrayListWithCapacity(count);
 
         while (count > 0) {
             result.add(user());
             count--;
         }
 
         return result;
     }
 
     /** @return group with random name and description */
     public static Group group() {
         return new Group(random(), random());
     }
 
     /** @return list with one group of banned users */
     public static List<Group> bannedGroups() {
         return Collections.singletonList(group());
     }
 
     /** @return random string of LENGTH symbols */
     private static String random() {
         return RandomStringUtils.randomAlphanumeric(LENGTH);
     }
 
     /**
      * @param max upper bound
      * @return random int with upper bound
      */
     private static int randomInt(int max) {
         return RANDOM.nextInt(max);
     }
 
 }
