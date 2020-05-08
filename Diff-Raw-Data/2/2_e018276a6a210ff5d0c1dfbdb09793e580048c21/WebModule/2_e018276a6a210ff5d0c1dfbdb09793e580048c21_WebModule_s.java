 /*
 * Copyright (C) 2006 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
  
 package org.opensubsystems.core.www;
 
 import org.opensubsystems.core.application.Module;
 
 /**
  * Interface representing module of a web application, which usually represents
  * a distinct section in a web user interface and can be switched to and from. 
  * 
  * @author OpenSubsystems
  */
 public interface WebModule extends Module
 {
    /**
     * Get unique module identifier that contains no spaces and is all in lower 
     * case so that it can be used in various internal web interface constructs.
     * 
     * @return String
     */
    String getIdentifier(
    );
    
    /**
     * Get tooltip that will be displayed for particular module on the web gui.
     * 
     * @return String
     */
    String getTooltip(
    );
 
    /**
     * Get URL of the module.
     * 
     * @return String
     */
    String getURL(
    );
 
    /**
     * Set the URL of the module.
     * 
     * @param strURL - URL of the module 
     */
    void setURL(
       String strURL
    );
 }
