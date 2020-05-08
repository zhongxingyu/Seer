 /*
  * Copyright 2011 @ashigeru.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package com.ashigeru.util.serialization;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Date;
 
 import org.junit.Test;
 
 /**
 * TODO
  * @author ashigeru
 *
  */
 public class ContextualObjectInputStreamTest {
 
     /**
      * 通常のクラス。
      * @throws Exception テスト失敗
      */
     @Test
     public void simpleClass() throws Exception {
         RedefiningClassLoader cl = new RedefiningClassLoader(getClass().getClassLoader());
         byte[] contents = serialize(new EmptyClass());
 
         Class<?> redefined = cl.redefine(EmptyClass.class);
         assertThat(redefined, not(sameInstance((Object) EmptyClass.class)));
         Object restored = deserialize(cl, contents);
 
         assertThat(restored, is(redefined));
     }
 
     /**
      * 通常のクラスの配列。
      * @throws Exception テスト失敗
      */
     @Test
     public void classArray() throws Exception {
         RedefiningClassLoader cl = new RedefiningClassLoader(getClass().getClassLoader());
         byte[] contents = serialize(new EmptyClass[0]);
 
         Class<?> redefined = cl.redefine(EmptyClass.class);
         assertThat(redefined, not(sameInstance((Object) EmptyClass.class)));
         Object[] restored = deserialize(cl, contents);
 
         assertThat(restored.getClass().getComponentType(), sameInstance((Object) redefined));
     }
 
     /**
      * 通常のクラスの深い配列。
      * @throws Exception テスト失敗
      */
     @Test
     public void classDeepArray() throws Exception {
         RedefiningClassLoader cl = new RedefiningClassLoader(getClass().getClassLoader());
         byte[] contents = serialize(new EmptyClass[0][][]);
 
         Class<?> redefined = cl.redefine(EmptyClass.class);
         assertThat(redefined, not(sameInstance((Object) EmptyClass.class)));
         Object[][][] restored = deserialize(cl, contents);
 
         assertThat(
                 restored.getClass().getComponentType().getComponentType().getComponentType(),
                 sameInstance((Object) redefined));
     }
 
     /**
      * システムクラス。
      * @throws Exception テスト失敗
      */
     @Test
     public void systemClass() throws Exception {
         RedefiningClassLoader cl = new RedefiningClassLoader(getClass().getClassLoader());
         Date date = new Date();
         byte[] contents = serialize(date);
 
         Class<?> redefined = cl.redefine(EmptyClass.class);
         assertThat(redefined, not(sameInstance((Object) EmptyClass.class)));
         Date restored = deserialize(cl, contents);
 
         assertThat(restored, is(date));
     }
 
     /**
     * システムクラス。
      * @throws Exception テスト失敗
      */
     @Test
     public void arraySystemClass() throws Exception {
         RedefiningClassLoader cl = new RedefiningClassLoader(getClass().getClassLoader());
         byte[] contents = serialize(new Date[0]);
 
         Class<?> redefined = cl.redefine(EmptyClass.class);
         assertThat(redefined, not(sameInstance((Object) EmptyClass.class)));
         Object restored = deserialize(cl, contents);
 
         assertThat(restored, is(Date[].class));
     }
 
     /**
     * システムクラス。
      * @throws Exception テスト失敗
      */
     @Test
     public void primitiveArray() throws Exception {
         RedefiningClassLoader cl = new RedefiningClassLoader(getClass().getClassLoader());
         byte[] contents = serialize(new int[0]);
 
         Class<?> redefined = cl.redefine(EmptyClass.class);
         assertThat(redefined, not(sameInstance((Object) EmptyClass.class)));
         Object restored = deserialize(cl, contents);
 
         assertThat(restored, is(int[].class));
     }
 
     private byte[] serialize(Object object) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ObjectOutputStream os = new ObjectOutputStream(out);
         os.writeObject(object);
         os.close();
         return out.toByteArray();
     }
 
     @SuppressWarnings("unchecked")
     private <T> T deserialize(ClassLoader cl, byte[] contents) throws IOException, ClassNotFoundException {
         ByteArrayInputStream in = new ByteArrayInputStream(contents);
         ObjectInputStream is = new ContextualObjectInputStream(in, cl);
         return (T) is.readObject();
     }
 }
