 /*
  * Copyright 2011 Robert W. Vawter III <bob@vawter.org>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.jsonddl.impl;
 
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
import org.jsonddl.JsonDdlObject;
import org.jsonddl.JsonDdlVisitor.PropertyVisitor;
import org.jsonddl.model.Kind;

 /**
  * Computes a MessageDigest.
  */
 public class DigestVisitor implements PropertyVisitor {
   private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
      'b', 'c', 'd', 'e', 'f' };
   private static final Charset UTF8 = Charset.forName("UTF8");
   private final MessageDigest sha;
   private final ByteBuffer temp = ByteBuffer.allocate(8);
 
   public DigestVisitor() {
     try {
       sha = MessageDigest.getInstance("SHA1");
     } catch (NoSuchAlgorithmException e) {
       throw new RuntimeException("SHA1 unsupported", e);
     }
   }
 
   public DigestVisitor(String digestName) throws NoSuchAlgorithmException {
     sha = MessageDigest.getInstance(digestName);
   }
 
   public void endVisit(JsonDdlObject<?> x) {
     sha.update(x.getDdlObjectType().getName().getBytes(UTF8));
   }
 
   @Override
   public <T> void endVisitProperty(T value, Context<T> ctx) {
     update(ctx.getProperty());
     update(value, ctx.getKind(), ctx.getNestedKinds());
   }
 
   /**
    * Returns the computed digest and resets the state of the visitor.
    */
   public byte[] getDigest() {
     return sha.digest();
   }
 
   /**
    * Returns the computed digest as a hexadecimal-encoded string and resets the state of the
    * visitor.
    */
   public String getDigestHex() {
     StringBuilder sb = new StringBuilder();
     for (byte b : getDigest()) {
       char lsc = HEX_DIGITS[b & 0xF];
       char msc = HEX_DIGITS[b >>> 4];
       sb.append(msc).append(lsc);
     }
     return sb.toString();
   }
 
   @Override
   public <T> boolean visitProperty(T value, Context<T> ctx) {
     return false;
   }
 
   private void update(double d) {
     temp.rewind();
     temp.putDouble(d);
     temp.rewind();
     temp.limit(8);
     sha.update(temp);
   }
 
   private void update(int i) {
     temp.rewind();
     temp.putInt(i);
     temp.rewind();
     temp.limit(4);
     sha.update(temp);
   }
 
   private void update(Object value, Kind kind, List<Kind> nestedKinds) {
     if (value == null) {
       return;
     }
 
     switch (kind) {
       case BOOLEAN:
         sha.update(((Boolean) value).booleanValue() ? (byte) 1 : 0);
         break;
       case DDL:
         sha.update(((Digested) value).computeDigest());
         break;
       case DOUBLE:
         update((Double) value);
         break;
       case ENUM:
         update(((Enum<?>) value).name());
         break;
       case INTEGER:
         update((Integer) value);
         break;
       case STRING:
         update(value.toString());
         break;
       case LIST: {
         List<?> list = (List<?>) value;
         update(list.size());
         List<Kind> newKinds = new ArrayList<Kind>(nestedKinds);
         Kind newKind = newKinds.remove(0);
         for (Object o : list) {
           update(o, newKind, newKinds);
         }
         break;
       }
       case MAP: {
         Map<?, ?> map = (Map<?, ?>) value;
         update(map.size());
         List<Kind> newKinds = new ArrayList<Kind>(nestedKinds);
         if (!Kind.STRING.equals(newKinds.remove(0))) {
           throw new RuntimeException("Did not find expected String kind");
         }
         Kind newKind = newKinds.remove(0);
         for (Map.Entry<?, ?> entry : map.entrySet()) {
           update(entry.getKey().toString());
           update(entry.getValue(), newKind, newKinds);
         }
         break;
       }
       case EXTERNAL:
         if (value instanceof Digested) {
           sha.update(((Digested) value).computeDigest());
         } else {
           // Fake it.
           update(value.getClass().getName());
           update(value.hashCode());
           update(value.toString());
         }
         break;
       default:
         throw new RuntimeException("Unhandled kind " + kind);
     }
   }
 
   private void update(String s) {
     sha.update(s.getBytes(UTF8));
   }
 }
