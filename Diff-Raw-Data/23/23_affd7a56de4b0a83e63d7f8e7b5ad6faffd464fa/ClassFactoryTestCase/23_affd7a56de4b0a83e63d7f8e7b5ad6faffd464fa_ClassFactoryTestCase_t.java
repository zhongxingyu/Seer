 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.asm;
 
 import java.io.Serializable;
import java.util.Date;
 
 import junit.framework.TestCase;
 
 public class ClassFactoryTestCase extends TestCase {
 	
 	public void testArray() {
 		System.out.println(int[].class.getComponentType());
 	}
 	
 	private static class TestClassLoader extends ClassLoader {
 		protected Class<?> defineClass(String name, byte[] bytes) {
 			return defineClass(bytes, 0, bytes.length);
 		}
 	}
 	
 	public void test() throws Exception {
 		ClassFactory factory = new ClassFactory();
 		String canonicalClassName = "org.dada.test.ImmutableDatum";
 		
 		String[][] fields = new String[][] {
 				{byte.class.getCanonicalName(),      "field00"},
 				{short.class.getCanonicalName(),     "field01"},
 				{int.class.getCanonicalName(),       "field02"},
 				{long.class.getCanonicalName(),      "field03"},
 				{float.class.getCanonicalName(),     "field04"},
 				{double.class.getCanonicalName(),    "field05"},
 				{boolean.class.getCanonicalName(),   "field06"},
 				{char.class.getCanonicalName(),      "field07"},
 				{Object.class.getCanonicalName(),    "field08"},
				{Date.class.getCanonicalName(),      "field09"},
 				{byte[].class.getCanonicalName(),    "field10"},
 				{short[].class.getCanonicalName(),   "field11"},
 				{int[].class.getCanonicalName(),     "field12"},
 				{long[].class.getCanonicalName(),    "field13"},
 				{float[].class.getCanonicalName(),   "field14"},
 				{double[].class.getCanonicalName(),  "field15"},
 				{boolean[].class.getCanonicalName(), "field16"},
 				{char[].class.getCanonicalName(),    "field17"},
 				{Object[].class.getCanonicalName(),  "field18"},
				{Date[].class.getCanonicalName(),    "field19"},
 			};
 		
 		byte[] bytecode = factory.create(canonicalClassName, Object.class, fields);
 		TestClassLoader loader = new TestClassLoader();
 		Class<?> type = loader.defineClass(canonicalClassName, bytecode);
 		assertTrue(type.getCanonicalName().equals(canonicalClassName));
 		Class<?>[] parameterTypes = {
 				byte.class,
 				short.class,
 				int.class,
 				long.class,
 				float.class,
 				double.class,
 				boolean.class,
 				char.class,
 				Object.class,
				Date.class,
 				byte[].class,
 				short[].class,
 				int[].class,
 				long[].class,
 				float[].class,
 				double[].class,
 				boolean[].class,
 				char[].class,
 				Object[].class,
				Date[].class,
 				};
 		
 		Object    object   = new Object();
		Date      date     = new Date();
 		byte[]    bytes    = new byte[0];
 		short[]   shorts   = new short[0];
 		int[]     ints     = new int[0];
 		long[]    longs    = new long[0];
 		float[]   floats   = new float[0];
 		double[]  doubles  = new double[0];
 		boolean[] booleans = new boolean[0];
 		char[]    chars    = new char[0];
		Object[]  objects  = new Object[0];
		Date[]    dates    = new Date[0];
 		
 		Object[] initArgs = {
 				Byte.MAX_VALUE,
 				Short.MAX_VALUE,
 				Integer.MAX_VALUE,
 				Long.MAX_VALUE,
 				Float.MAX_VALUE,
 				Double.MAX_VALUE,
 				Boolean.TRUE,
 				Character.MAX_VALUE,
 				object,
				date,
 				bytes,
 				shorts,
 				ints,
 				longs,
 				floats,
 				doubles,
 				booleans,
 				chars,
 				objects,
				dates,
 		};
 		Object instance = type.getConstructor(parameterTypes).newInstance(initArgs);
 		
 		assertTrue(instance instanceof Serializable);
 		assertTrue(type.getMethod("getField00", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Byte.MAX_VALUE));
 		assertTrue(type.getMethod("getField01", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Short.MAX_VALUE));
 		assertTrue(type.getMethod("getField02", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Integer.MAX_VALUE));
 		assertTrue(type.getMethod("getField03", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Long.MAX_VALUE));
 		assertTrue(type.getMethod("getField04", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Float.MAX_VALUE));
 		assertTrue(type.getMethod("getField05", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Double.MAX_VALUE));
 		assertTrue(type.getMethod("getField06", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Boolean.TRUE));
 		assertTrue(type.getMethod("getField07", (Class<?>[]) null).invoke(instance, (Object[]) null).equals(Character.MAX_VALUE));
 		assertTrue(type.getMethod("getField08", (Class<?>[]) null).invoke(instance, (Object[]) null) == object);
		assertTrue(type.getMethod("getField09", (Class<?>[]) null).invoke(instance, (Object[]) null) == date);
 		assertTrue(type.getMethod("getField10", (Class<?>[]) null).invoke(instance, (Object[]) null) == bytes);
 		assertTrue(type.getMethod("getField11", (Class<?>[]) null).invoke(instance, (Object[]) null) == shorts);
 		assertTrue(type.getMethod("getField12", (Class<?>[]) null).invoke(instance, (Object[]) null) == ints);
 		assertTrue(type.getMethod("getField13", (Class<?>[]) null).invoke(instance, (Object[]) null) == longs);
 		assertTrue(type.getMethod("getField14", (Class<?>[]) null).invoke(instance, (Object[]) null) == floats);
 		assertTrue(type.getMethod("getField15", (Class<?>[]) null).invoke(instance, (Object[]) null) == doubles);
 		assertTrue(type.getMethod("getField16", (Class<?>[]) null).invoke(instance, (Object[]) null) == booleans);
 		assertTrue(type.getMethod("getField17", (Class<?>[]) null).invoke(instance, (Object[]) null) == chars);
 		assertTrue(type.getMethod("getField18", (Class<?>[]) null).invoke(instance, (Object[]) null) == objects);
		assertTrue(type.getMethod("getField19", (Class<?>[]) null).invoke(instance, (Object[]) null) == dates);
 	}
 
 }
