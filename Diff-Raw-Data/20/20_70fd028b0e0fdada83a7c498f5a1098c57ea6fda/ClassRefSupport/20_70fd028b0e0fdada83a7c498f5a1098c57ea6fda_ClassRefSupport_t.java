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
 
 package com.cedarsoft.serialization.generator.output;
 
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JCodeModel;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  */
 public class ClassRefSupport {
   @NotNull
   protected final JCodeModel model;
   @NotNull
   private final Map<String, JClass> refs = new HashMap<String, JClass>();
 
   public ClassRefSupport( @NotNull JCodeModel model ) {
     this.model = model;
   }
 
   @NotNull
   public JClass ref( @NotNull @NonNls String qualifiedName ) {
    if ( qualifiedName.contains( "?" ) || qualifiedName.contains( "<" ) || qualifiedName.contains( ">" ) ) {
      throw new IllegalArgumentException( "Cannot create ref for <" + qualifiedName + ">" );
    }

     JClass resolved = refs.get( qualifiedName );
     if ( resolved != null ) {
       return resolved;
     }
 
     JClass newRef = model.ref( qualifiedName );
     refs.put( qualifiedName, newRef );
     return newRef;
   }
 
   @NotNull
   public JClass ref( @NotNull Class<?> type ) {
     return model.ref( type );
   }
 }
