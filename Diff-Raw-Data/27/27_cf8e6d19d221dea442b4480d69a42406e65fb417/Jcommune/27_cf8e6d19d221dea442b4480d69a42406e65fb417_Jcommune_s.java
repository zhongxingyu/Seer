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
 import java.util.List;
 
 /**
  * The central JCommune entity that contains all the configuration for
  * respective component.
  * 
  * @author Guram Savinov
  * 
  */
 public class Jcommune extends Component {
 
     private List<Section> sections = new ArrayList<Section>();
 
     /**
      * Gets the sections.
      * 
      * @return the sections
      */
     public List<Section> getSections() {
         return sections;
     }
 
     /**
      * Sets the sections.
      * 
      * @param sections
      *            the sections to set
      */
     public void setSections(List<Section> sections) {
         this.sections = sections;
     }
 
     /**
      * Adds a section to the list.
      * 
      * @param section
      *            the section to add
      */
     public void addSection(Section section) {
         sections.add(section);
     }
 
 }
