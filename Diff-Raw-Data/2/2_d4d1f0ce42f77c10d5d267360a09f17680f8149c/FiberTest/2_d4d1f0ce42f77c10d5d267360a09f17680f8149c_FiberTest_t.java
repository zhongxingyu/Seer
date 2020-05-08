 /*
  * The MIT License (MIT)
  *
  * Copyright (c) 2013 Steven Willems
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package be.idevelop.fiber;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class FiberTest {
 
     private Fiber fiber;
 
     @Before
     public void setUp() {
         fiber = new Fiber();
     }
 
     @Test
     public void testSerializePrimitives() {
         int intValue = 10;
         testSerialization(intValue);
 
         boolean boolValue = false;
         testSerialization(boolValue);
 
         byte byteValue = 91;
         testSerialization(byteValue);
 
         char charValue = '@';
         testSerialization(charValue);
 
         float floatValue = 0.2f;
         testSerialization(floatValue);
 
         double doubleValue = 0.3d;
         testSerialization(doubleValue);
 
         short shortValue = 1;
         testSerialization(shortValue);
 
         long longValue = 24124L;
         testSerialization(longValue);
     }
 
     @Test
     public void testSerializeNullValuesForPrimitiveWrappers() {
         Integer intValue = null;
         testSerialization(intValue);
 
         Boolean boolValue = null;
         testSerialization(boolValue);
 
         Byte byteValue = null;
         testSerialization(byteValue);
 
         Character charValue = null;
         testSerialization(charValue);
 
         Float floatValue = null;
         testSerialization(floatValue);
 
         Double doubleValue = null;
         testSerialization(doubleValue);
 
         Short shortValue = null;
         testSerialization(shortValue);
 
         Long longValue = null;
         testSerialization(longValue);
     }
 
     @Test
     public void testSerializeOfString() {
        String s = "some text!@#\u00b4\u00fe\u00af-\u2026\u02da\u02dd\u02d8\u02dc\u02db\u02c7.\u00b4\u00e9\u01a1\u00e7";
         testSerialization(s);
     }
 
     @Test
     public void testSerializerPrimitiveWrapperClasses() {
         Object[] objects = new Object[]{10, false, (byte) 91, '@', 0.2f, 0.3d, (short) 1, 12L};
 
         for (Object object : objects) {
             testSerialization(object);
         }
     }
 
     @Test
     public void testSerializeArrayOfIntegers() {
         int[] integers = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
         ByteBuffer serialized = fiber.serialize(integers);
         int[] result = fiber.deserialize(serialized);
         assertArrayEquals(integers, result);
     }
 
     @Test
     public void testSerializeArrayOfDifferentObjects() {
         Object[] objects = new Object[]{0, "bla", '1'};
         ByteBuffer serialized = fiber.serialize(objects);
         Object[] result = fiber.deserialize(serialized);
         assertArrayEquals(objects, result);
     }
 
     @Test
     public void testSerializeArrayOfObjects() {
         SimpleObject[] objects = new SimpleObject[]{new SimpleObject(1), new SimpleObject(2)};
         fiber.register(SimpleObject.class);
         ByteBuffer serialized = fiber.serialize(objects);
         Object[] result = fiber.deserialize(serialized);
         assertArrayEquals(objects, result);
     }
 
     @Test
     public void testSerializeComplexClass() {
         ComplexClassWithDefaultConstructorAndReferences complexClass = ComplexClassWithDefaultConstructorAndReferences.createNewInstance();
         fiber.register(ComplexClassWithDefaultConstructorAndReferences.class);
 
         testSerialization(complexClass);
     }
 
     @Test
     public void testSerializeToInputStreamAndDeserializeFromStream() {
         ComplexClassWithDefaultConstructorAndReferences complexClass = ComplexClassWithDefaultConstructorAndReferences.createNewInstance();
         fiber.register(ComplexClassWithDefaultConstructorAndReferences.class);
 
         InputStream stream = fiber.serializeToStream(complexClass);
         Object result = fiber.deserializeFromStream(stream);
         assertEquals(complexClass, result);
     }
 
     @Test
     public void testSerializeToBytesAndDeserializeFromBytes() {
         ComplexClassWithDefaultConstructorAndReferences complexClass = ComplexClassWithDefaultConstructorAndReferences.createNewInstance();
         fiber.register(ComplexClassWithDefaultConstructorAndReferences.class);
 
         byte[] bytes = fiber.serializeToBytes(complexClass);
         assertTrue(bytes.length != 8192);
         Object result = fiber.deserializeFromBytes(bytes);
         assertEquals(complexClass, result);
     }
 
     private void testSerialization(Object value) {
         ByteBuffer serialized = fiber.serialize(value);
         Object result = fiber.deserialize(serialized);
         assertEquals(value, result);
     }
 }
