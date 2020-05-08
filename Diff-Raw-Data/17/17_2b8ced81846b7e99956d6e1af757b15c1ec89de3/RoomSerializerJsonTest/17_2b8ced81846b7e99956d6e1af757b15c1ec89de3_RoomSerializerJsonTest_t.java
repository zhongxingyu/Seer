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
 
 package com.cedarsoft.serialization.stax.test;
 
import com.cedarsoft.JsonUtils;
 import com.cedarsoft.serialization.AbstractSerializerTest2;
 import com.cedarsoft.serialization.Entry;
 import com.cedarsoft.serialization.Serializer;
 import com.cedarsoft.serialization.stax.StaxMateSupport;
 import com.cedarsoft.serialization.stax.StaxSupport;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 import org.junit.experimental.theories.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  */
 public class RoomSerializerJsonTest extends AbstractSerializerTest2<Room> {
   @NotNull
   @Override
   protected Serializer<Room> getSerializer() throws Exception {
     return new Room.Serializer( new Window.Serializer(), new Door.Serializer() );
   }
 
   @After
   public void clear() {
     StaxMateSupport.clear();
   }
 
   @Override
   protected void verifySerialized( @NotNull Entry<Room> entry, @NotNull byte[] serialized ) throws Exception {
    JsonUtils.assertJsonEquals( new String( entry.getExpected() ), new String( serialized ) );
   }
 
   @DataPoint
   public static Entry<?> json() {
     StaxSupport.enableJson();
 
     List<Window> windows = Arrays.asList( new Window( "asdf", 20, 30 ), new Window( "asdf2", 50, 60.7 ) );
     List<Door> doors = Arrays.asList( new Door( "asdf" ), new Door( "asdf2" ), new Door( "asdf3" ) );
     Room room = new Room( "descr", windows, doors );
 
     return create( room, "{\"room\":{\"@xmlns\":{\"$\":\"room\\/1.0.0\"},\"description\":{\"$\":\"descr\"},\"window\":[{\"@width\":\"20.0\",\"@height\":\"30.0\",\"description\":{\"$\":\"asdf\"}},{\"@width\":\"50.0\",\"@height\":\"60.7\",\"description\":{\"$\":\"asdf2\"}}],\"door\":[{\"description\":{\"$\":\"asdf\"}},{\"description\":{\"$\":\"asdf2\"}},{\"description\":{\"$\":\"asdf3\"}}]}}".getBytes() );
   }
 }
