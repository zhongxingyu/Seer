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
 
 package com.quartercode.disconnected.graphics.component;
 
 import de.matthiasmann.twl.Widget;
 
 /**
  * The current state of a root widget state defines what should be drawn.
  * It is used as the top-level child widget by the root widget.
  * 
  * @see RootWidget
  */
 public class GraphicsState extends Widget {
 
     private final String themeResource;
 
     /**
      * Creates a new empty state which defines what should be drawn.
      * 
      * @param themeResource The theme resource (file) as a resource url.
      */
     public GraphicsState(String themeResource) {
 
         this.themeResource = themeResource;
     }
 
     /**
      * Returns the theme resource (file) as a resource url.
      * 
      * @return The theme resource (file) as a resource url.
      */
     public String getThemeResource() {
 
         return themeResource;
     }
 
 }
