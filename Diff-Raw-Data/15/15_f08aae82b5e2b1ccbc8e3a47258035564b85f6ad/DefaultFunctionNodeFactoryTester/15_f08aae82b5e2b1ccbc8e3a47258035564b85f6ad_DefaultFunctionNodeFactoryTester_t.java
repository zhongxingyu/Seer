 /**
  * DefaultFunctionNodeFactoryTester.java
  * 
  * Copyright 2009 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.gp.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import jmona.InitializationException;
 import jmona.gp.FunctionNode;
 import jmona.gp.impl.example.ExampleBinaryNode;
 import jmona.test.Util;
 
 import org.junit.Test;
 
 /**
  * Test class for the DefaultFunctionNodeFactory class.
  * 
  * @author jfinkels
  */
 public class DefaultFunctionNodeFactoryTester {
   /** The number of times to create a Node with the factory under test. */
   public static final int NUM_TESTS = 100;
 
   /**
    * Test method for
    * {@link jmona.gp.impl.DefaultFunctionNodeFactory#createNode()}.
    */
   @Test
   public void testCreateNode() {
     final Set<Class<FunctionNode<Integer>>> classesSet = new HashSet<Class<FunctionNode<Integer>>>();
 
     try {
       classesSet.add((Class<FunctionNode<Integer>>) Class
          .forName("jmona.gp.impl.example.ExampleBinaryNode"));
     } catch (final ClassNotFoundException exception) {
       Util.fail(exception);
     }
 
     final DefaultFunctionNodeFactory<Integer> factory = new DefaultFunctionNodeFactory<Integer>(
         classesSet);
 
     try {
       for (int i = 0; i < NUM_TESTS; ++i) {
         final FunctionNode<Integer> node = factory.createNode();
         assertTrue(node instanceof ExampleBinaryNode);
       }
     } catch (final InitializationException exception) {
       Util.fail(exception);
     }
 
   }
 
   /**
    * Test method for
    * {@link jmona.gp.impl.DefaultFunctionNodeFactory#nodeClasses()}.
    */
   @Test
   public void testNodeClasses() {
     final Set<Class<FunctionNode<Integer>>> classesSet = new HashSet<Class<FunctionNode<Integer>>>();
 
     try {
       classesSet.add((Class<FunctionNode<Integer>>) Class
          .forName("jmona.gp.impl.example.ExampleBinaryNode"));
     } catch (final ClassNotFoundException exception) {
       Util.fail(exception);
     }
 
     final DefaultFunctionNodeFactory<Integer> factory = new DefaultFunctionNodeFactory<Integer>(
         classesSet);
 
     assertTrue(factory.nodeClasses().contains(ExampleBinaryNode.class));
     assertEquals(1, factory.nodeClasses().size());
   }
 
 }
