 /*
  * Copyright (c) 2011, Ken Gilmer
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
  * in the documentation and/or other materials provided with the distribution. Neither the name of the Ken Gilmer nor the names 
  * of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sprinkles.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.sprinkles.Fn;
 import org.sprinkles.functions.ReturnFilesFunction;
 import org.sprinkles.functions.StringFunctions;
 
 
 
 /**
  * Some informal tests for Sprinkles.
  * 
  * @author kgilmer
  * 
  */
 public class BaseTests extends TestCase {
 
 	/**
 	 * Test getting files usiing the ReturnFilesFunction.
 	 */
 	public void testFiles() {
 		//Collection fd = Mapper.fileToCollection(new File("/tmp"), false);
 		
 		Collection<File> result = Fn.map(new Fn.Function<File, File>() {
 			
 			@Override
 			public File apply(File f) {
 				System.out.println(f.toString());
 				
 				return f;
 			}
 		}, Fn.map(ReturnFilesFunction.GET_FILES_FN, new File(".")));
 		
 		assertNotNull(result);		
 	}
 
 	public void testDepthFirst() {
 		Map m = new LinkedHashMap();
 		
 		for (int i = 0; i < 10; ++i) {
 			Map mi = new LinkedHashMap();
 			m.put("mi" + i, mi);
 			for (int j = 0; j < 10; ++j) {
 				Map mj = new LinkedHashMap();
 				mi.put("mj" + j, mj);
 				for (int k = 0; k < 10; ++k) {
 					mj.put("k" + k, "i" + i + "j" + j + "k" + k);
 				}
 			}
 		}
 		
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				System.out.println(element.toString());
 				
 				return element;
 			}
 			
 		}, m.values());
 		
 		assertTrue(output.size() == 10 * 10 * 10);
 	}
 
 	public void testMap() {
 		final Map m = new HashMap();
 
 		m.put("ka", "va");
 		m.put("kb", "vb");
 		m.put("kc", "vc");
 
 		//Object input = Mapper.adaptToIterable(m);
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element.equals(m)) {
 					return null;
 				}
 
 				if (element instanceof String) {
 					return ((String) element) + "4";
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, m.values());
 
 		System.out.println(output);
 	}
 
 	public void testMapValues() {
 		Map m = new HashMap();
 
 		m.put("ka", "va");
 		m.put("kb", "vb");
 		m.put("kc", "vc");
 
 		
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element instanceof String) {
 					return ((String) element) + "4";
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, m);
 
 		System.out.println(output);
 	}
 
 	public void testList() {
 		List l = new ArrayList();
 
 		l.add("a");
 		l.add("b");
 		l.add("c");
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element instanceof String) {
 					return ((String) element) + "4";
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, l);
 
 		System.out.println(output);
 	}
 
 	public void testRawList() {
 		final List l = new ArrayList();
 
 		l.add("a");
 		l.add("b");
 		l.add("c");
 
 		Iterable input = l;
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element.equals(l)) {
 					return null;
 				}
 
 				if (element instanceof String) {
 					return ((String) element) + "4";
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, input);
 
 		System.out.println(output);
 	}
 
 	public void testString() {
 		String[] s = { "a", "b", "c", "d", "e" };
 
 		Object input = Arrays.asList(s);
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element instanceof String) {
 					return ((String) element) + "2";
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, input);
 
 		System.out.println(output);
 	}
 
 	public void testAutobox() {
 		int[] s = { 1, 2, 3, 4, 5 };
 
 		System.out.println(Arrays.asList(s).size());
 		
 		Iterable input = Arrays.asList(s);
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element instanceof Integer) {
 					return ((Integer) element).intValue() + 1;
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, input);
 
 		System.out.println(output);
 		assertTrue(output.size() == 5);
 	}
 
 	public void testIntegers() {
 		Integer[] s = { 1, 2, 3, 4, 5 };
 
 		Iterable input = Arrays.asList(s);
 
 		Collection output = Fn.map(new Fn.Function() {
 
 			@Override
 			public Object apply(Object element) {
 				if (element instanceof Integer) {
 					return ((Integer) element).intValue() + 1;
 				}
 
 				throw new RuntimeException("passed something funny!");
 			}
 
 		}, input);
 
 		System.out.println(output);
 		assertTrue(output.size() == 5);
 	}
 	
 	public void testStringFunctions() {
 		List<String> l = new ArrayList<String>();
 		
 		for (int i = 0 ; i < 100; ++i)
 			l.add("i" + i);
 		
 		System.out.println(
				Fn.fold(
					new StringFunctions.JoinFn(","), l));
 	}
 }
