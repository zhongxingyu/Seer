 /*
                         Java Commons
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
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
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: EnumUtils.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Helper methods when working with enums.
  *
  * Date: 2013/08/14
  * Time: 09:55
  *
  */
package org.acmsl.queryj.commons.utils;
 
 /*
  * Importing JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /*
  * Importing JDK classes.
  */
 import java.util.Locale;
 
 /**
  * Helper methods when working with enums.
  * @author <a href="mailto:java-commons@acm-sl.org">Jose San Leandro</a>
  * @since 0.8
  * Created: 2013/08/14 09:55
  */
 @ThreadSafe
 public class EnumUtils
 {
     /**
      * Singleton implementation to avoid double-locking check.
      */
     protected static final class EnumUtilsSingletonContainer
     {
         /**
          * The actual singleton.
          */
         public static final EnumUtils SINGLETON = new EnumUtils();
     }
 
     /**
      * Retrieves the singleton instance.
      *
      * @return such instance.
      */
     @NotNull
     public static EnumUtils getInstance()
     {
         return EnumUtilsSingletonContainer.SINGLETON;
     }
 
     /**
      * Retrieves the enum associated to given string.
      * @param c the enum class.
      * @param string the enum value.
      * @param <T> the enum class.
      * @return the enum instance.
      */
     @NotNull
     public <T extends Enum<T>> T getEnumFromString(@NotNull final Class<T> c, @NotNull final String string)
     {
         @NotNull final T result;
 
         try
         {
             result = Enum.valueOf(c, string.trim().toUpperCase(Locale.US).replaceAll("-", "_"));
         }
         catch (@NotNull final IllegalArgumentException invalidEnumName)
         {
             // TODO
             throw new RuntimeException("Invalid enum: " + string);
         }
 
         return result;
     }
 }
