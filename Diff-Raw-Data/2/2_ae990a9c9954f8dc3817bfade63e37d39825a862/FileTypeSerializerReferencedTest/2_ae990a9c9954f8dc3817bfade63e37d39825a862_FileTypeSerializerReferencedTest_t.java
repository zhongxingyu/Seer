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
 
 package com.cedarsoft.serialization.serializers.json;
 
 import com.cedarsoft.file.FileType;
 import com.cedarsoft.file.FileTypeRegistry;
 import com.cedarsoft.serialization.test.utils.AbstractJsonSerializerTest2;
 import com.cedarsoft.serialization.test.utils.Entry;
 import com.cedarsoft.serialization.Serializer;
 import org.junit.experimental.theories.*;
 
 import javax.annotation.Nonnull;
 
 public class FileTypeSerializerReferencedTest extends AbstractJsonSerializerTest2<FileType> {
 
   @DataPoint
   public static final Entry<? extends FileType> ENTRY1 = create(
     FileTypeRegistry.RAW_CANON,
    "\"CanonRaw\""
   );
 
   @Nonnull
   @Override
   protected Serializer<FileType> getSerializer() throws Exception {
     return new FileTypeSerializer.Referenced( new FileTypeRegistry() );
   }
 
   @Override
   protected boolean addTypeInformation() {
     return false;
   }
 }
