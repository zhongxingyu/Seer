 /**
  * $Revision: $
  * $Date: $
  *
  * Copyright (C) 2006 Jive Software. All rights reserved.
  *
  * This software is published under the terms of the GNU Lesser Public License (LGPL),
  * a copy of which is included in this distribution.
  */
 
 package org.jivesoftware.sparkimpl.settings;
 
 public class JiveInfo {
 
     private JiveInfo() {
 
     }
 
     public static String getVersion() {
        return "2.5.0 Beta 3";
     }
 
     public static String getBuildNumber(){
         return "2.5.0";
     }
 
     public static String getOS() {
         return System.getProperty("os.name");
     }
 }
