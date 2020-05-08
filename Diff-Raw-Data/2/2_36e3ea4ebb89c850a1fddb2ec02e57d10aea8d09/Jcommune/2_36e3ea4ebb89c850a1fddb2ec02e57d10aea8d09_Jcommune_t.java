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
 
 import org.apache.commons.lang3.StringUtils;
 import org.jtalks.common.model.entity.Property;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The central JCommune entity that contains all the configuration for respective component.
  *
  * @author Guram Savinov
  * @author Vahluev Vyacheslav
  * @author Leonid Kazantcev
  */
 public class Jcommune extends Component {
     static final String URL_PROPERTY = "jcommune.url_address";
     static final String URL_SUFFIX = "/";
     static final String URL_PROTOCOL = "http://";
     private List<PoulpeSection> sections = new ArrayList<PoulpeSection>();
 
     /**
      * Creates Component with {@link ComponentType#FORUM} and empty section list. Visible for hibernate.
      */
     protected Jcommune() {
         super(ComponentType.FORUM);
     }
 
     /**
      * Creates JCommune component with ComponentType.FORUM type, given name, description and the list of properties.
      * Instances should be created using {@link ComponentBase#newComponent(String, String)} with FORUM ComponentBase
      *
      * @param name              of the component
      * @param description       its descriptions
      * @param defaultProperties of the component
      */
     Jcommune(String name, String description, List<Property> defaultProperties) {
         super(name, description, ComponentType.FORUM, defaultProperties);
     }
 
     /**
      * @return component's sections
      */
     public List<PoulpeSection> getSections() {
         return sections;
     }
 
     /**
      * @param sections new list of sections
      */
     public void setSections(List<PoulpeSection> sections) {
         this.sections = sections;
     }
 
     /**
      * Removes the specified section from jcommune instance if it's there, does nothing if it's not there.
      *
      * @param section the section to remove it from the list
      * @return {@code true} if the specified section wasn't in the list
      */
     public boolean removeSection(PoulpeSection section) {
         return getSections().remove(section);
     }
 
     /**
      * Adds a section to the list if it doesn't exist or update it.
      *
      * @param section the section to add or update
      */
     public void addOrUpdateSection(PoulpeSection section) {
         int position = sections.indexOf(section);
         if (position >= 0) {
             sections.set(position, section);
         } else {
             sections.add(section);
         }
     }
 
     /**
      * Returns the URL of JCommune, cuts the last symbol if it is '/', to provide link in correct format.
      *
      * @return URL of the component
      */
     public String getUrl() {
         String url = getProperty(URL_PROPERTY);
        if(!StringUtils.isBlank(url) && !url.startsWith(URL_PROTOCOL)){
             url = URL_PROTOCOL + url;
         }
         return StringUtils.removeEnd(url, URL_SUFFIX);
     }
 
     /**
      * Moves the section to the target section place. Shifts the target section and any subsequent sections to the right.
      *
      * @param section a section to move
      * @param target  a target section that will be shifted
      */
     public void moveSection(PoulpeSection section, PoulpeSection target) {
         sections.remove(section);
         int position = sections.indexOf(target);
         sections.set(position, section);
         sections.add(position + 1, target);
     }
 }
