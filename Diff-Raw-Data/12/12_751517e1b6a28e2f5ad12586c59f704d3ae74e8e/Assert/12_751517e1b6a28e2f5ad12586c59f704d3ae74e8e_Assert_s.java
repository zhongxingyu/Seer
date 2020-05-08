 /*
  *  Copyright 2011 paul.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package org.codeartisans.junit;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 public final class Assert
 {
 
     public static interface PostSerializationAssertions<T extends Serializable>
     {
 
         void postSerializationAssertions( T copy );
 
     }
 
     public static <T extends Serializable> void assertSerializable( T tested, PostSerializationAssertions<T> assertions )
             throws IOException, ClassNotFoundException
     {
         // Serialize
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream( out );
         oos.writeObject( tested );
         oos.close();
 
         // Deserialize
         byte[] pickled = out.toByteArray();
         InputStream in = new ByteArrayInputStream( pickled );
         ObjectInputStream ois = new ObjectInputStream( in );
         Object o = ois.readObject();
         // Cast
         @SuppressWarnings( "unchecked" )
         T copy = ( T ) o;
 
         assertions.postSerializationAssertions( copy );
     }
 
     private Assert()
     {
     }
 
 }
