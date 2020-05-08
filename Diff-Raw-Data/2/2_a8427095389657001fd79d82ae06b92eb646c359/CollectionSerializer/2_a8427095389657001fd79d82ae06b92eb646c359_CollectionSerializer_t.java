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
 package com.cedarsoft.serialization.stax.mate;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.annotation.Nonnull;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.codehaus.staxmate.out.SMOutputElement;
 
 import com.cedarsoft.version.Version;
 import com.cedarsoft.version.VersionException;
 import com.cedarsoft.version.VersionRange;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  * @param T the type of the elements that are serialized
  */
 public class CollectionSerializer<T> extends AbstractStaxMateSerializer<List<? extends T>> {
   @Nonnull
   private final Class<T> type;
 
   protected CollectionSerializer(@Nonnull Class<T> type, @Nonnull AbstractStaxMateSerializer<T> serializer) {
     this(type, serializer, serializer.getDefaultElementName() + "s", serializer.getNameSpaceBase() + "s", serializer.getFormatVersionRange());
   }
 
   public CollectionSerializer(@Nonnull Class<T> type, @Nonnull AbstractStaxMateSerializer<T> serializer, @Nonnull String defaultElementName, @Nonnull String nameSpaceUriBase, @Nonnull VersionRange formatVersionRange) {
     super(defaultElementName, nameSpaceUriBase, formatVersionRange);
     this.type = type;
 
    add(serializer).responsibleFor(type).map(formatVersionRange).toDelegateVersion(serializer.getFormatVersion());
     assert getDelegatesMappings().verify();
   }
 
   @Override
   public void serialize(@Nonnull SMOutputElement serializeTo, @Nonnull List<? extends T> object, @Nonnull Version formatVersion) throws IOException, VersionException, XMLStreamException {
     verifyVersionWritable(formatVersion);
 
     serializeCollection(object, type, serializeTo, formatVersion);
   }
 
   @Nonnull
   @Override
   public List<? extends T> deserialize(@Nonnull XMLStreamReader deserializeFrom, @Nonnull Version formatVersion) throws IOException, VersionException, XMLStreamException {
     verifyVersionReadable(formatVersion);
 
     return deserializeCollection(deserializeFrom, type, formatVersion);
   }
 }
