 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 
 import org.junit.Test;
 
 public class ClassUtilTest {
 
 	@Test
 	public void testGetPackageName() {
 		
 		assertEquals("", ClassUtil.getPackageName(null));
 		assertEquals("net.sf.okapi.common", ClassUtil.getPackageName(this.getClass()));
 		assertEquals("java.lang", ClassUtil.getPackageName((new String()).getClass()));
 		// TODO test on a class w/o package info
 	}
 	
 	@Test
 	public void testExtractPackageName() {
 		
 		assertEquals("", ClassUtil.extractPackageName(null));
 		assertEquals("", ClassUtil.extractPackageName(""));
 		assertEquals("", ClassUtil.extractPackageName("aaa/bbb/ccc"));
 		assertEquals("aaa.bbb", ClassUtil.extractPackageName("aaa.bbb.ccc"));
 		assertEquals("net.sf.okapi.common", ClassUtil.extractPackageName("net.sf.okapi.common.ClassUtil"));
 	}
 	
 	@Test
 	public void testQualifyName() {
 		
 		assertEquals("", ClassUtil.qualifyName("", null));
 		assertEquals("", ClassUtil.qualifyName("", ""));
 		assertEquals("", ClassUtil.qualifyName("package", ""));
 		assertEquals("", ClassUtil.qualifyName("", "class"));
 		assertEquals("package.class", ClassUtil.qualifyName("package", "class"));
 		assertEquals("package.class", ClassUtil.qualifyName("package.", "class"));
 		assertEquals("package.class", ClassUtil.qualifyName("package_class", "package.class"));
 		assertEquals(".class", ClassUtil.qualifyName("package.", ".class"));		
 		assertEquals("java.lang.Integer", ClassUtil.qualifyName((new String()).getClass(), "Integer"));
 		
 		assertEquals("java.lang.Integer", ClassUtil.qualifyName(ClassUtil.extractPackageName(
 				(new String()).getClass().getName()), "Integer"));
 		
 		assertEquals("net.sf.okapi.common.UtilTest", ClassUtil.qualifyName(ClassUtil.extractPackageName(
 				this.getClass().getName()), "UtilTest"));
 		
 		assertEquals("net.sf.okapi.common.UtilTest", ClassUtil.qualifyName(this, "UtilTest"));
 	}
 	
 	@Test
 	public void testInstantiateClass() {
 		
 		// 1 Class reference
 		try {
 			ClassUtil.instantiateClass(BOMAwareInputStream.class);
 			fail("InstantiationException should've been trown");
 			
 		} catch (InstantiationException e) {
 			// OK, expected
 		} catch (IllegalAccessException e) {
 			fail("IllegalAccessException shouldn't have been trown");
 		}
 		
 		// 2 Class reference, empty constructor parameters
 			try {
 				assertNull(ClassUtil.instantiateClass(BOMAwareInputStream.class, (Object[]) null));
 				fail("InstantiationException should've been trown");			
 			} catch (InstantiationException e) {
 				// OK, expected
 			} catch (IllegalAccessException e) {
 				fail("IllegalAccessException shouldn't have been trown");
 			} catch (Exception e) {
 				fail(e.getMessage());
 			}
 		
 		// 3 Class reference, correct constructor parameters
 		InputStream input = null;
 		try {
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 				
 			String defaultEncoding = "UTF-8";
 			assertNotNull(ClassUtil.instantiateClass(BOMAwareInputStream.class, input, defaultEncoding));
 			
 		} catch (Exception e) {				
 			fail(e.getMessage());
 		}
 						
 		// 4 Class reference, wrong parameter types given, no matching constructor
 		try {
 			input = null;
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 					
 			assertNull(ClassUtil.instantiateClass(BOMAwareInputStream.class, input, 3));
 			fail("RuntimeException should've been trown");
 		} catch (IllegalAccessException e) {
 			fail("IllegalAccessException shouldn't have been trown");
 		} catch (RuntimeException e) {
 			// OK, expected
 		} catch (Exception e) {
 			// OK, expected
 		}
 		
 		// 5 Class name, null
 		try {
 			assertNull(ClassUtil.instantiateClass((String) null));
 		} catch (Exception e) {
 		}
 		
 		// 6 Class name, empty
 		try {
 			assertNull(ClassUtil.instantiateClass(""));
 		} catch (Exception e) {
 		}
 		
 		// 7 Class name, wrong name
 		try {
 			assertNull(ClassUtil.instantiateClass("foo.bar"));
 		} catch (Exception e) {
 		}
 		
 		// 8 Class name, correct name, but no empty constructor
 		try {
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream"));
 			fail("InstantiationException should've been trown");
 			
 		} catch (Exception e) {
 			// OK, expected
 		}
 		
 		// 9 Class name, correct name
 		try {
 			assertNotNull(ClassUtil.instantiateClass("java.lang.String"));			
 			
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 		
 		// 10 Class name, wrong loader
 		try {
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.ClassUtilTest2", String.class.getClassLoader()));			
 			fail("IllegalArgumentException should've been trown");
 		} catch (IllegalArgumentException e) {
 			// OK, expected
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 		
 		// 11 Class name, wrong name
 		try {
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.ClassUtilTest2", ClassUtil.class.getClassLoader()));			
 			fail("InstantiationException should've been trown");
 			
 		} catch (Exception e) {
 		}
 		
 		// 12 Class name, correct loader
 		try {
 			assertNotNull(ClassUtil.instantiateClass("net.sf.okapi.common.ClassUtilTest", this.getClass().getClassLoader()));						
 			
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 
 		// 13 Class name, correct constructor parameters
 		input = null;
 		try {
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 				
 			String defaultEncoding = "UTF-8";
 			assertNotNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream", input, defaultEncoding));
 			
 		} catch (Exception e) {				
 			fail(e.getMessage());
 		}
 						
 		// 14 Class name, wrong parameter types given, no matching constructor
 		try {
 			input = null;
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 					
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream", input, 3));
 			fail("RuntimeException should've been trown");
 		} catch (RuntimeException e) {
 			// OK, expected
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 		
 		// 15 Class name, correct name, correct loader, correct constructor parameters
 		input = null;
 		try {
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 				
 			String defaultEncoding = "UTF-8";
 			assertNotNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream", 
 					this.getClass().getClassLoader(), input, defaultEncoding));
 			
 		} catch (Exception e) {				
 			fail(e.getMessage());
 		}
 		
 		// 16 Class name, correct name, correct loader, incorrect constructor parameters
 		input = null;
 		try {
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 				
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream", 
 					this.getClass().getClassLoader(), input, 10));
 			
 		} catch (RuntimeException e) {	
 			// OK, expected
 		} catch (Exception e) {	
 			fail(e.getMessage());
 		}
 		
 		// 17 Class name, correct name, incorrect loader, correct constructor parameters
 		input = null;
 		try {
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 				
 			String defaultEncoding = "UTF-8";
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream", 
 					String.class.getClassLoader(), input, defaultEncoding));
 			
 		} catch (RuntimeException e) {	
 			// OK, expected
 		} catch (Exception e) {	
 			fail(e.getMessage());
 		}
 		
 		// 18 Class name, incorrect name, correct loader, correct constructor parameters
 		input = null;
 		try {
 			URL url = this.getClass().getResource("/test.txt");
 			input = new FileInputStream(new File(url.toURI()));
 				
 			String defaultEncoding = "UTF-8";
 			assertNull(ClassUtil.instantiateClass("net.sf.okapi.common.BOMAwareInputStream2", 
 					this.getClass().getClassLoader(), input, defaultEncoding));
 			
 		} catch (Exception e) {				
 		}
 	}
 }
