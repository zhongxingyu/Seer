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
 
 import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.jtalks.common.model.entity.Entity;
 import org.jtalks.poulpe.validation.annotations.UniqueConstraint;
 import org.jtalks.poulpe.validation.annotations.UniqueField;
 
 import javax.annotation.Nonnull;
 import javax.validation.constraints.NotNull;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * User Groups is the class that can join users into groups. After that permissions can be assigned to the groups and
  * all users in this group will have that permission while browsing components.
  *
  * @author Akimov Knostantin
  */
 @UniqueConstraint
 public class Group extends Entity {
 	/**
      * Error message if group already exists
      */
     public static final String GROUP_ALREADY_EXISTS = "groups.validation.not_unique_group_name";
     
     /**
      * Error message if group name is void
      */
    public static final String GROUP_CANT_BE_VOID = "groups.error.group_name_cant_be_void";
     
     /**
      * Error message if section name is wrong
      */
     public static final String ERROR_LABEL_SECTION_NAME_WRONG = "sections.editsection.name.err";
     
 	@UniqueField(message = GROUP_ALREADY_EXISTS)
     @NotNull(message = GROUP_CANT_BE_VOID)
     @NotEmpty(message = GROUP_CANT_BE_VOID)
     @Length(min = 1, max = 254, message = ERROR_LABEL_SECTION_NAME_WRONG)
 	private String name;
     private String description;
 
     public Group() {
     }
 
     public Group(String name) {
         this.name = name;
     }
 
     /**
      * @param name        the title of the groups, when saving to DB, can't be empty or {@code null}, it also should be
      *                    unique
      * @param description an optional description of the group
      */
     public Group(String name, String description) {
         this.name = name;
         this.description = description;
     }
 
     /**
      * Gets the title of the group, if it's already in DB, it's unique and not empty or {@code null}.
      *
      * @return the title of the group
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the title of the group, when saving to DB, can't be empty or {@code null}, it also should be unique.
      *
      * @param name the title of the group
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Gets the textual description of the group.
      *
      * @return the optional description of the group
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Sets the optional textual description of the group.
      *
      * @param description the description of the group; optional
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * A handy method to create a number of groups with specified names.
      *
      * @param names the names you want resulting groups to be with
      * @return a list of groups with the specified name in the same order
      */
     public static List<Group> createGroupsWithNames(String... names) {
         List<Group> groups = new ArrayList<Group>(names.length);
         for (String nextName : names) {
             groups.add(new Group(nextName, ""));
         }
         return groups;
     }
 
     /**
      * Lets the Group classes be comparable by their names. Throws NPE if anything is {@code null} whether it's a group
      * itself or its name.
      *
      * @author stanislav bashkirtsev
      */
     public static class ByNameComparator implements Comparator<Group> {
         /**
          * {@inheritDoc}
          */
         @Override
         public int compare(@Nonnull Group group, @Nonnull Group group1) {
             return group.getName().compareTo(group1.getName());
         }
     }
 
 }
