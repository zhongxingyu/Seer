 /*
  * Copyright (C) 2011-2012  Christian Roesch
  * 
  * This file is part of micro-debug.
  * 
  * micro-debug is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * micro-debug is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with micro-debug.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.croesch.micro_debug.properties;
 
 import java.util.Locale;
 import java.util.Properties;
 
 import com.github.croesch.micro_debug.annotation.NotNull;
 
 /**
  * Provides properties read from xml files.
  * 
  * @author croesch
  * @since Date: Feb 22, 2012
  */
 public final class XMLPropertiesProvider extends APropertiesProvider {
 
   /**
    * Hidden constructor to be able to create a singleton.
    * 
    * @since Date: Feb 22, 2012
    */
   private XMLPropertiesProvider() {
    super('-');
   }
 
   @Override
   @NotNull
   protected Properties createNewProperties(final String file) {
     return new XMLI18nProperties(file, Locale.getDefault());
   }
 
   /**
    * Provides the instance of this {@link XMLPropertiesProvider}.
    * 
    * @since Date: Feb 22, 2012
    * @return the single instance of this class.
    */
   @NotNull
   public static XMLPropertiesProvider getInstance() {
     return LazyHolder.INSTANCE;
   }
 
   /**
    * Initialization on Demand Holder.
    * 
    * @author croesch
    * @since Date: Jan 25, 2012
    */
   private static final class LazyHolder {
     /** instance of {@link XMLPropertiesProvider} */
     public static final XMLPropertiesProvider INSTANCE = new XMLPropertiesProvider();
 
     /**
      * Hidden constructor..
      * 
      * @since Date: Jan 25, 2012
      */
     private LazyHolder() {
       throw new AssertionError();
     }
   }
 }
