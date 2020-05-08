 /*
  * Copyright (C) 2013 UniCoPA
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package unicopa.copa.base.event;
 
 import java.util.List;
 
 /**
  * A group of Events. Similiar events can be grouped, e.g. to be listed together
  * in a query.
  * 
  * @author Felix Wiemuth
  */
 public class EventGroup {
 
     private int eventGroupID;
     private String eventGroupName;
     private String eventGroupInfo;
     private List<Integer> categories;
 
     /**
      * Create a new EventGroup object.
      * 
      * @param eventGroupID
      * @param eventGroupName
      * @param eventGroupInfo
      * @param categories
      */
     public EventGroup(int eventGroupID, String eventGroupName,
 	    String eventGroupInfo, List<Integer> categories) {
 	this.eventGroupID = eventGroupID;
 	this.eventGroupName = eventGroupName;
 	this.eventGroupInfo = eventGroupInfo;
 	this.categories = categories;
     }
     
    public EventGroup(){}
 
     /**
      * Get the ID of this group. It is guaranteed to be unique under all
      * available EventGroup objects in use of the system.
      * 
      * @return
      */
     public int getEventGroupID() {
 	return eventGroupID;
     }
 
     /**
      * Get the name of this group.
      * 
      * @return
      */
     public String getEventGroupName() {
 	return eventGroupName;
     }
 
     /**
      * Get a description of this group.
      * 
      * @return
      */
     public String getEventGroupInfo() {
 	return eventGroupInfo;
     }
 
     /**
      * Get all categories associated with this group. The categories are leaves
      * of the category tree.
      * 
      * @return the IDs of all leaves of the category tree this group is
      *         associated with
      */
     public List<Integer> getCategories() {
 	return categories;
     }
 }
