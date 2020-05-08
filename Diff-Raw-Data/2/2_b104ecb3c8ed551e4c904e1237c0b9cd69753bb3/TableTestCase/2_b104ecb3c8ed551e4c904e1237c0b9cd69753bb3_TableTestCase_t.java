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
 package org.dada.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CountDownLatch;
 
 import junit.framework.TestCase;
 
 public class TableTestCase extends TestCase {
 
 	public void doNottestCompactOpenTable() {
 
 		CompactOpenTable.Factory<String> factory = new CompactOpenTable.Factory<String>() {
 
 			@Override
 			public String create(Integer key, Collection<String> views) {
 				return "" + key;
 			}
 		};
 
 		Table<Integer, String> table = new CompactOpenTable<String>(new ArrayList<String>(), factory);
 
 		testTable(table);
 	}
 
 	public void testSparseOpenTable() {
 
 		SparseOpenTable.Factory<Integer, String> factory = new SparseOpenTable.Factory<Integer, String>() {
 
 			// TODO: map should not be passed as a param here - but rather to ctor, if needed...
 			@Override
 			public String create(Integer key) throws Exception {
 				if (key < 0)
 					throw new UnsupportedOperationException("NYI");
 				else
 					return "" + key;
 			}
 		};
 
 		Table<Integer, String> table = new SparseOpenTable<Integer, String>(new ConcurrentHashMap<Integer, String>(), factory);
 
 		testTable(table);
 	}
 
 	public static class Getter implements Runnable {
 
 		private final Integer key;
 		private final Table<Integer, String> table;
 		
 		private String value;
 		
 		public Getter(Integer key, Table<Integer, String> table) {
 			this.key = key;
 			this.table = table;
 		}
 		
 		@Override
 		public void run() {
 			value = table.get(key);
 		}
 
 		public String getValue() {
 			return value;
 		}
 	}
 
 	public void testSparseOpenTableRaceCondition() throws Exception {
 
 		final CountDownLatch latch = new CountDownLatch(1);
 
 		SparseOpenTable.Factory<Integer, String> factory = new SparseOpenTable.Factory<Integer, String>() {
 			@Override
 			public String create(Integer key) throws Exception {
 				latch.await();
 				return "" + key;
 			}
 		};
 
 		final Table<Integer, String> table = new SparseOpenTable<Integer, String>(new ConcurrentHashMap<Integer, String>(), factory);
 
 		Getter getter1 = new Getter(0, table);
 		Getter getter2 = new Getter(0, table);
 
 		Thread thread1 = new Thread(getter1);
 		Thread thread2 = new Thread(getter2);
 
 		// line up two threads on latch in factory.create - one will lose the race
		// exercising that code path...
 		thread1.start();
 		thread2.start();
 		// let them run...
 		latch.countDown();
 		
 		thread1.join();
 		thread2.join();
 		
 		String value1 = getter1.getValue();
 		String value2 = getter2.getValue();
 		assertTrue(value1.equals("0") && value2.equals("0"));
 	}
 
 	public void testTable(Table<Integer, String> table) {
 
 		assertTrue(table.get(0).equals("0"));
 
 		String one = "1";
 		table.put(1, one);
 		assertTrue(table.get(1) == one);
 
 		assertTrue(table.get(-1) == null);
 
 		String minusOne = "-1";
 		table.put(-1, minusOne);
 		assertTrue(table.get(-1) == minusOne);
 	}
 }
