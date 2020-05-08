 /**
  *        Licensed to the Apache Software Foundation (ASF) under one
  *        or more contributor license agreements.  See the NOTICE file
  *        distributed with this work for additional information
  *        regarding copyright ownership.  The ASF licenses this file
  *        to you under the Apache License, Version 2.0 (the
  *        "License"); you may not use this file except in compliance
  *        with the License.  You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *        Unless required by applicable law or agreed to in writing,
  *        software distributed under the License is distributed on an
  *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *        KIND, either express or implied.  See the License for the
  *        specific language governing permissions and limitations
  *        under the License.
  *
  */
 package org.intelligentsia.dowsers.core.reflexion;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import org.intelligentsia.dowsers.core.reflection.Reflection;
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 
 /**
  * ReflectionTest.
  * 
  * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
  */
 public class ReflectionTest {
 
 	@Test
 	public void checkCapitalize() {
 		try {
 			Reflection.capitalize(null, "");
 			fail();
 		} catch (final NullPointerException exception) {
 		}
 		try {
 			Reflection.capitalize(null, null);
 			fail();
 		} catch (final NullPointerException exception) {
 		}
 		assertEquals("A", Reflection.capitalize("", "a"));
 		assertEquals("aA", Reflection.capitalize("a", "a"));
 		assertEquals("-Abc", Reflection.capitalize("-", "abc"));
 	}
 
 	@Test
 	public void checkToFieldName() {
 		try {
 			Reflection.toFieldName(null);
 			fail();
 		} catch (final NullPointerException exception) {
 		}
 		try {
 			Reflection.toFieldName("");
 			fail();
 		} catch (final StringIndexOutOfBoundsException exception) {
 		}
 		try {
 			Reflection.toFieldName("a");
 			fail();
 		} catch (final StringIndexOutOfBoundsException exception) {
 		}
 		try {
 			Reflection.toFieldName("ab");
 			fail();
 		} catch (final StringIndexOutOfBoundsException exception) {
 		}
 		try {
 			Reflection.toFieldName("abc");
 			fail();
 		} catch (final StringIndexOutOfBoundsException exception) {
 		}
 		assertEquals("a", Reflection.toFieldName("getA"));
 		assertEquals("abcdE", Reflection.toFieldName("getAbcdE"));
 	}
 
 	@Test
 	public void testfindGenericClassInstance() {
 		// dummy
 		assertEquals("java.lang.String", String.class.getName());
 		final List<String> dummy = Lists.newArrayList();
 		// we love runtime type
 		assertEquals("java.util.ArrayList", dummy.getClass().getName());
 
 		final StringList stringList = new StringList();
 		List<Class<?>> list = Reflection.findGenericClass(stringList);
 		assertNotNull(list);
 		assertTrue(!list.isEmpty());
 		assertTrue(list.size() == 1);
 		assertEquals(String.class, list.get(0));
 
 		final StringListFinal stringListFinal = new StringListFinal();
 		list = Reflection.findGenericClass(stringListFinal);
 		assertNotNull(list);
 		assertTrue(!list.isEmpty());
 		assertTrue(list.size() == 2);
 		assertEquals(Integer.class, list.get(0));
 		assertEquals(Long.class, list.get(1));
 
 		list = Reflection.findGenericClass(new String());
 		assertNotNull(list);
 		assertTrue(list.isEmpty());
 
 	}
 
 	@Test
 	public void testfindGenericClass() {
 		// dummy
 		assertEquals("java.lang.String", String.class.getName());
 		final List<String> dummy = Lists.newArrayList();
 		// we love runtime type
 		assertEquals("java.util.ArrayList", dummy.getClass().getName());
 
 		List<Class<?>> list = Reflection.findGenericClass(StringList.class);
 		assertNotNull(list);
 		assertTrue(!list.isEmpty());
 		assertTrue(list.size() == 1);
 		assertEquals(String.class, list.get(0));
 
 		list = Reflection.findGenericClass(StringListFinal.class);
 		assertNotNull(list);
 		assertTrue(!list.isEmpty());
 		assertTrue(list.size() == 2);
 		assertEquals(Integer.class, list.get(0));
 		assertEquals(Long.class, list.get(1));
 
 		list = Reflection.findGenericClass(String.class);
 		assertNotNull(list);
 		assertTrue(list.isEmpty());
 
 	}
 }
