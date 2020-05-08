 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.serialization.stax;
 
 import org.codehaus.staxmate.SMInputFactory;
 import org.codehaus.staxmate.SMOutputFactory;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * Support class for stax mate
  */
 public class StaxMateSupport {
   @NotNull
   static final ThreadLocal<SMInputFactory> SM_INPUT_FACTORY = new ThreadLocal<SMInputFactory>() {
     /** @noinspection RefusedBequest*/
     @Override
     protected SMInputFactory initialValue() {
       return new SMInputFactory( StaxSupport.XML_INPUT_FACTORY.get() );
     }
   };
   @NotNull
   static final ThreadLocal<SMOutputFactory> SM_OUTPUT_FACTORY = new ThreadLocal<SMOutputFactory>() {
     /** @noinspection RefusedBequest*/
     @Override
     protected SMOutputFactory initialValue() {
       return new SMOutputFactory( StaxSupport.XML_OUTPUT_FACTORY.get() );
     }
   };
 
   private StaxMateSupport() {
   }
 
   /**
    * Returns the cached sm output factory
    *
    * @return the cached sm output factory
    */
   @NotNull
   public static SMOutputFactory getSmOutputFactory() {
     return SM_OUTPUT_FACTORY.get();
   }
 
   /**
    * Returns the cached sm input factory
    *
    * @return the cached sm input factory
    */
   @NotNull
   public static SMInputFactory getSmInputFactory() {
     return SM_INPUT_FACTORY.get();
   }
 
   public static void clear() {
     StaxMateSupport.SM_INPUT_FACTORY.remove();
     StaxMateSupport.SM_OUTPUT_FACTORY.remove();
 
     StaxSupport.clear();
   }
 }
