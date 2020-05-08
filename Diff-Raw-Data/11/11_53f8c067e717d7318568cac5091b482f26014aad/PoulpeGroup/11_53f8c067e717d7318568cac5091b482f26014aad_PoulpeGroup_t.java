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
 package org.jtalks.poulpe.model.entity;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.annotation.Nonnull;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.validation.annotations.UniqueConstraint;
 
 /**
  * User Groups is the class that can join users into groups. After that
  * permissions can be assigned to the groups and all users in this group will
  * have that permission while browsing components.
  * 
  * @author Akimov Konstantin
  * @author Vyacheslav Zhivaev
  */
 @UniqueConstraint
 public class PoulpeGroup extends Group {
 
     private PoulpeBranch branch;
 
     /**
      * Creates a group with all fields set to null
      */
     public PoulpeGroup() {
         super();
     }
 
     /**
      * Creates a group with the given name
      * 
      * @param name of the group
      */
     public PoulpeGroup(String name) {
         super(name);
     }
 
     /**
      * @param name the title of the groups, when saving to DB, can't be empty or
      * {@code null}, it also should be unique
      * @param description an optional description of the group
      */
     public PoulpeGroup(String name, String description) {
         super(name, description);
     }
 
     /**
      * @return branch of this group
      */
     public PoulpeBranch getBranch() {
         return branch;
     }
 
     /**
      * @param branch to set for the group
      */
     public void setBranch(PoulpeBranch branch) {
         this.branch = branch;
     }
 
     /**
      * Should be used in preference of {@link #getUsers()}
      * @return the list of {@link User} object
      */
     @SuppressWarnings("unchecked")
     public List<User> getPoulpeUsers() {
        List<?> users = getUsers();
        return (List<User>) users;
     }
 
     /**
      * @param users to be added to the group
      */
     public void setPoulpeUsers(Collection<? extends org.jtalks.common.model.entity.User> users) {
         setUsers(new ArrayList<org.jtalks.common.model.entity.User>(users));
     }
 
     /**
      * Lets the Group classes be comparable by their names. Throws NPE if
      * anything is {@code null} whether it's a group itself or its name.
      * 
      * @author stanislav bashkirtsev
      */
     static public class ByNameComparator<T extends Group> implements Comparator<T> {
         /**
          * {@inheritDoc}
          */
         @Override
         public int compare(@Nonnull Group group, @Nonnull Group group1) {
             return group.getName().compareTo(group1.getName());
         }
     }
 }
