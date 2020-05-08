 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.sim.comp.os;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlIDREF;
 import com.quartercode.disconnected.util.InfoString;
 
 /**
  * A user represents someone who uses a system.
  * The user object takes care of the right system and other things related to users.
  * 
  * @see Group
  */
 @XmlAccessorType (XmlAccessType.FIELD)
 public class User implements Comparable<User>, InfoString {
 
     @XmlIDREF
     @XmlAttribute
     private OperatingSystem host;
     private String          name;
     @XmlIDREF
     @XmlElement (name = "group")
     private List<Group>     groups;
 
     /**
      * Creates a new empty user object.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     protected User() {
 
     }
 
     /**
      * Creates a new user and sets the host system the object is used for and his name.
      * 
      * @param host The host operating system the user is used for.
      * @param name The name the user has.
      */
     public User(OperatingSystem host, String name) {
 
         this.host = host;
         this.name = name;
 
         groups = new ArrayList<Group>();
     }
 
     /**
      * Returns the host operating system the user is used for.
      * 
      * @return The host operating system the user is used for.
      */
     public OperatingSystem getHost() {
 
         return host;
     }
 
     /**
      * Returns the name of the user.
      * The name is used for recognizing a user on the os-level.
      * 
      * @return The name the user has.
      */
     public String getName() {
 
         return name;
     }
 
     /**
      * Returns a list of all groups the user is in.
      * Groups are used to set rights for multiple users.
      * 
      * @return A list of all groups the user is in.
      */
     public List<Group> getGroups() {
 
         return Collections.unmodifiableList(groups);
     }
 
     /**
      * Adds the user as a member to the given group.
      * If primary is set, the new group automatically becomes the primary one.
      * 
      * @param group The group to add the user to.
      * @param primary True if the new group should become the primary one.
      */
    public void addGroup(Group group, boolean primary) {
 
         if (!groups.contains(group)) {
             groups.add(group);
 
             if (primary) {
                 setPrimaryGroup(group);
             }
         }
     }
 
     /**
      * Removes the membership of this user from the given group.
      * 
      * @param group The group to remove the user from.
      * @throws IllegalStateException The group is the primary one (you have to set another group as primary first).
      */
    public void removeGroup(Group group) throws IllegalStateException {
 
         if (!getPrimaryGroup().equals(group)) {
            removeGroup(group);
         } else {
             throw new IllegalStateException("Can't remove group " + group.getName() + ": group is primary");
         }
     }
 
     /**
      * Returns the primary group of the user.
      * The primary group is the first group in the groups list and is used when rights are applied.
      * 
      * @return The primary group of the user.
      */
     public Group getPrimaryGroup() {
 
         if (groups.size() > 0) {
             return groups.get(0);
         } else {
             return null;
         }
     }
 
     /**
      * Changes the primary group of the user to the given one.
      * The user must already be a member of the group.
      * The primary group is the first group in the groups list and is used when rights are applied.
      * 
      * @param group The new primary group of the user.
      */
     public void setPrimaryGroup(Group group) {
 
         if (groups.contains(group)) {
             groups.remove(group);
             groups.add(0, group);
         }
     }
 
     /**
      * Returns the unique serialization id for the user.
      * The id is a combination of the host computer's id and the user's name.
      * It should only be used by a serialization algorithm.
      * 
      * @return The unique serialization id for the user.
      */
     @XmlAttribute
     @XmlID
     protected String getId() {
 
         return host.getHost().getId() + "-" + name;
     }
 
     @Override
     public int compareTo(User o) {
 
         return name.compareTo(o.getName());
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (groups == null ? 0 : groups.hashCode());
         result = prime * result + (name == null ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         User other = (User) obj;
         if (groups == null) {
             if (other.groups != null) {
                 return false;
             }
         } else if (!groups.equals(other.groups)) {
             return false;
         }
         if (name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toInfoString() {
 
         return name + ", " + groups.size() + " memberships";
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [" + toInfoString() + "]";
     }
 
 }
