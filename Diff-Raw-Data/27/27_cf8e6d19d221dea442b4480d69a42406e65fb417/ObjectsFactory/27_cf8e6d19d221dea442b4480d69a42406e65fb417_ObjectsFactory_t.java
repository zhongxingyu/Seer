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
 package org.jtalks.poulpe.model.dao.hibernate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import org.jtalks.common.model.entity.User;
 import org.jtalks.poulpe.model.entity.Branch;
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.model.entity.ComponentType;
 import org.jtalks.poulpe.model.entity.Group;
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.Rank;
 import org.jtalks.poulpe.model.entity.Section;
 import org.jtalks.poulpe.model.entity.TopicType;
 
 /**
  * @author Kirill Afonin
  * @author Alexey Grigorev
  */
 public final class ObjectsFactory {
 
     public static Branch createBranch() {
         Branch newBranch = new Branch(random(), "desc");
         newBranch.setSection(createSection());
         return newBranch;
     }
 
     public static TopicType createTopicTypeWithRandomTitle() {
         return new TopicType(random(), "desc");
     }
 
     public static Component createComponent(ComponentType type) {
         return new Component(random(), "desc", type);
     }
 
     public static Section createSectionWithBranches() {
         return createSectionWithBranches(randomInt());
     }
 
     public static Section createSectionWithBranches(int branchesAmount) {
         Section section = new Section(random());
 
         for (int i = 0; i < branchesAmount; i++) {
             Branch branch = createBranch();
             branch.setSection(section);
             section.addBranch(branch);
         }
 
         return section;
     }
 
     public static Section createSection() {
         return new Section(random());
     }
 
     public static User createUser() {
         return new User(random(), random(), random());
     }
 
     public static Group createGroup() {
         return new Group(random(), "desc");
     }
 
     private static String random() {
         return UUID.randomUUID().toString();
     }
 
     private static int randomInt() {
         return new Random().nextInt(10) + 1;
     }
 
     /**
      * Create rank with random name and postCount.
      * 
      * @return new rank
      */
     public static Rank createRank() {
         int randNum = new Random().nextInt();
         Rank rank = new Rank("Rank" + randNum, randNum);
         return rank;
     }
 
     public static Jcommune createJcommune() {
        Jcommune jcommune = new Jcommune(createComponent(ComponentType.FORUM));

         for (int i = 0; i < 5; i++) {
             jcommune.addSection(createSectionWithBranches());
         }
         return jcommune;
     }
 }
