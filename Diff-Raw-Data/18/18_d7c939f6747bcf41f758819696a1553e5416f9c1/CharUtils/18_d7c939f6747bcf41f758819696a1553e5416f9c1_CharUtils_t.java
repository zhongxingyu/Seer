 /*
                         ACM-SL Commons
 
     Copyright (C) 2002-2003  Jose San Leandro Armendriz
                              jsanleandro@yahoo.es
                              chousz@yahoo.com
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
    version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-307  USA
 
 
     Thanks to ACM S.L. for distributing this library under the LGPL license.
     Contact info: jsr000@terra.es
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabaas
                     Boadilla del monte
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendriz
  *
 * Description: Provides some useful methods when working with characters and
  *              buffers.
  *
  * Last modified by: $Author$ at $Date$
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *                  ("Name" means no concrete version has been checked out)
  *
  * $Id$
  *
  */
 package org.acmsl.commons.utils;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.patterns.Utils;
 import org.acmsl.commons.version.Version;
 import org.acmsl.commons.version.VersionFactory;
 
 /*
  * Importing some JDK classes.
  */
 import java.lang.ref.WeakReference;
 
 /**
  * Provides some useful methods when working with characters and buffers.
  * @author <a href="mailto:jsanleandro@yahoo.es"
            >Jose San Leandro Armendriz</a>
  * @version $Revision$
  */
 public abstract class CharUtils
     implements  Utils
 {
     /**
      * An empty char array for building invalid method invocations.
      */
     protected static final char[] EMPTY_CHAR_ARRAY = new char[0];
 
     /**
      * Singleton implemented as a weak reference.
      */
     private static WeakReference singleton;
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected CharUtils()  {};
 
     /**
      * Specifies a new weak reference.
      * @param utils the utils instance to use.
      */
     protected static void setReference(CharUtils utils)
     {
         singleton = new WeakReference(utils);
     }
 
     /**
      * Retrieves the weak reference.
      * @return such reference.
      */
     protected static WeakReference getReference()
     {
         return singleton;
     }
 
     /**
      * Retrieves a CharUtils instance.
      * @return such instance.
      */
     public static CharUtils getInstance()
     {
         CharUtils result = null;
 
         WeakReference reference = getReference();
 
         if  (reference != null) 
         {
             result = (CharUtils) reference.get();
         }
 
         if  (result == null) 
         {
             result = new CharUtils() {};
         }
         
         return result;
     }
 
     /**
      * Creates a char array with the specified subcontents of given array.
      * @param buffer the buffer where to extract the information from.
      * @param start the starting position of the desired subcontents.
      * @param offset the subcontents length.
      * @return the sub-buffer, or null if any parameter is invalid.
      */
    public char[] subbuffer(
        final char[] buffer, final int start, final int offset)
     {
         char[] result = EMPTY_CHAR_ARRAY;
 
         if  (   (buffer != null)
              && (start  > -1)
              && (offset >  0))
         {
             int t_iEnd =
                 (start + offset >= buffer.length)
                 ?   buffer.length
                 :   start + offset;
 
             result = new char[t_iEnd - start];
 
             for (
                 int t_iCharIndex = start;
                     t_iCharIndex < t_iEnd;
                     t_iCharIndex ++)
             {
                 result[t_iCharIndex - start] = buffer[t_iCharIndex];
             }
         }
 
         return result;
     }
 
     /**
      * Creates a char array with the specified subcontents of given array.
      * @param buffer the buffer where to extract the information from.
      * @param start the starting position of the desired subcontents.
      * @return the sub-buffer.
      */
    public char[] subbuffer(final char[] buffer, final int start)
     {
         char[] result = EMPTY_CHAR_ARRAY;
 
         if  (buffer != null)
         {
             result = subbuffer(buffer, start, buffer.length);
         }
 
         return result;
     }
 
     /**
      * Concrete version object updated everytime it's checked-in in a CVS
      * repository.
      */
     public static final Version VERSION =
         VersionFactory.createVersion("$Revision$");
 
     /**
      * Retrieves the current version of this object.
      * @return the version object with such information.
      */
     public Version getVersion()
     {
         return VERSION;
     }
 
     /**
      * Retrieves the current version of this class. It's defined because
      * this is a utility class that cannot be instantiated.
      * @return the object with class version information.
      */
     public static Version getClassVersion()
     {
         return VERSION;
     }
 }
