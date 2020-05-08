 /*
  * Copyright (C) 2010 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xproc;
 
 /**
  * @author Herve Quiroz
  */
 public final class Tubular
 {
     private static final int VERSION_MAJOR = 0;
    private static final int VERSION_MINOR = 0;
     private static final boolean SNAPSHOT = true;
     private static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + (SNAPSHOT ? "-SNAPSHOT" : "");
 
     private Tubular()
     {
         // No instantiation
     }
 
     public static String version()
     {
         return VERSION;
     }
 
     public static String xprocVersion()
     {
         return "1.0";
     }
 
     public static String xpathVersion()
     {
         return "2.0";
     }
 }
