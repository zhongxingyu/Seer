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
 package org.jtalks.poulpe.web.controller.users;
 
 import org.jtalks.common.model.entity.Group;
 
 import java.text.Collator;
 import java.util.Locale;
 
 /**
  * Wires {@link Group} and {@link Boolean} by enable selected status for group, allows to make interface with
  * list of groups wired with checkboxes.
  * @author Leonid Kazancev
  */
 public class GroupBooleanPair implements Comparable<GroupBooleanPair> {
     private boolean enable;
     private boolean changed;
     private Group group;
 
     /**
      * Construction method.
      * @param group to hold
      * @param enable status of group
      */
     public GroupBooleanPair(Group group, boolean enable) {
         this.enable = enable;
         this.group = group;
     }
 
     /**
      * @return enable status
      */
     public boolean isEnable() {
         return enable;
     }
 
     /**
      * Sets group enable status, also keep actual state of changed status by changing it on enable change.
      * @param enable status to set
      */
     public void setEnable(boolean enable) {
         if (this.enable != enable) {
             changed = !changed;
         }
         this.enable = enable;
     }
 
     /**
      * @return changed status(holds group color)
      */
     public boolean isChanged() {
         return changed;
     }
 
     /**
      * @return {@link Group}
      */
     public Group getGroup() {
         return group;
     }
 
     @Override
     public int compareTo(GroupBooleanPair pair) {
        String name1 = group.getName();
        String name2 = pair.getGroup().getName();

        Boolean isEnable1 = enable;
        Boolean isEnable2 = pair.isEnable();

        if (!isEnable1.equals(isEnable2)) {
            return isEnable2.compareTo(isEnable1);
         } else {
             Collator russianCollator = Collator.getInstance(new Locale("ru", "RU"));
            return russianCollator.compare(name1, name2);
         }
     }
 }
