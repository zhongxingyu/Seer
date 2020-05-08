 /*
     Copyright 2012 Georgia Tech Research Institute
 
     Author: lance.gatlin@gtri.gatech.edu
 
     This file is part of org.gtri.util.iteratee library.
 
     org.gtri.util.iteratee library is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     org.gtri.util.iteratee library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with org.gtri.util.iteratee library. If not, see <http://www.gnu.org/licenses/>.
 
 */
 
package org.gtri.iteratee.api;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.gtri.util.iteratee.api.*;
 import org.gtri.util.iteratee.impl.test.*;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 /**
  *
  * @author Lance
  */
 public class IterateeApiTests {
   List<Integer> integers = new ArrayList<Integer>();
   List<String> strings = new ArrayList<String>();
   List<String> strings2 = new ArrayList<String>();
   Planner planner = new org.gtri.util.iteratee.impl.Planner();
   
   public IterateeApiTests() {
     integers.add(1);
     integers.add(2);
     integers.add(3);
     strings.add("a");
     strings.add("b");
     strings.add("c");
     strings2.add("d");
     strings2.add("e");
     strings2.add("f");
   }
   
   @BeforeClass
   public static void setUpClass() throws Exception {
   }
 
   @AfterClass
   public static void tearDownClass() throws Exception {
   }
   
   @Before
   public void setUp() {
   }
   
   @After
   public void tearDown() {
   }
 
   @Test
   public void testIntToString() {
     Producer<Integer> integerProducer = new TestIntProducer(integers);
     
     Translator<Integer,String> intToString = new TestIntToStringTranslator();
     
     List<String> output = new ArrayList<String>();
     Consumer<String> stringConsumer = new TestStringConsumer(output);
     
     Consumer<Integer> integerConsumer = planner.translate(intToString, stringConsumer);
     Consumer.Plan<Integer> plan = planner.connect(integerProducer, integerConsumer);
     plan.run();
     
     StringBuilder outBuilder = new StringBuilder();
     for(String s : output) {
       outBuilder.append(s);
     }
     String actual = outBuilder.toString();
     assertEquals("123", actual);
   }
   
   public void testStringToStringBuilder() {
     Producer<String> stringProducer = new TestStringProducer(strings);
     
     List<String> output = new ArrayList<String>();
     Consumer<String> stringConsumer = new TestStringConsumer(output);
     
     Consumer.Plan<String> plan = planner.connect(stringProducer, stringConsumer);
     plan.run();
     
     StringBuilder outBuilder = new StringBuilder();
     for(String s : output) {
       outBuilder.append(s);
     }
     String actual = outBuilder.toString();
     assertEquals("abc", actual);
   }
   
   public void testStringConcat() {
     Producer<String> stringProducer1 = new TestStringProducer(strings);
     Producer<String> stringProducer2 = new TestStringProducer(strings2);
     Producer<String> stringProducer3 = planner.concat(stringProducer1, stringProducer2);
     Builder<String, String> stringBuilder = new TestStringBuilder();
 
     Builder.Plan<String,String> plan = planner.connect(stringProducer3, stringBuilder);
     Builder.Result<String,String> result = plan.run();
     
     assertEquals(true, result.isSuccess());
     
     String actual = result.get();
     assertEquals("abcdef", actual);
   }
 }
