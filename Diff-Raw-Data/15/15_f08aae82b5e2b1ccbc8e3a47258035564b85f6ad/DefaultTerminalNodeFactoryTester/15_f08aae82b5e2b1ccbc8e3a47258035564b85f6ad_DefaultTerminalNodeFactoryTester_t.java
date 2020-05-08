 /**
  * DefaultTerminalNodeFactoryTester.java
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
 import jmona.gp.TerminalNode;
 import jmona.gp.impl.example.ExampleTerminalNode;
 import jmona.test.Util;
 
 import org.junit.Test;
 
 /**
  * Test class for the DefaultTerminalNodeFactory class.
  * 
  * @author jfinkels
  */
 public class DefaultTerminalNodeFactoryTester {
   /** The number of times to create a Node with the factory under test. */
   public static final int NUM_TESTS = 100;
 
   /**
    * Test method for
    * {@link jmona.gp.impl.DefaultTerminalNodeFactory#createNode()}.
    */
   @Test
   public void testCreateNode() {
     final Set<Class<TerminalNode<Integer>>> classesSet = new HashSet<Class<TerminalNode<Integer>>>();
 
     try {
       classesSet.add((Class<TerminalNode<Integer>>) Class
          .forName("jmona.gp.impl.example.ExampleTerminalNode"));
     } catch (final ClassNotFoundException exception) {
       Util.fail(exception);
     }
 
     final DefaultTerminalNodeFactory<Integer> factory = new DefaultTerminalNodeFactory<Integer>(
         classesSet);
 
     try {
       for (int i = 0; i < NUM_TESTS; ++i) {
         final TerminalNode<Integer> node = factory.createNode();
         assertTrue(node instanceof ExampleTerminalNode);
       }
     } catch (final InitializationException exception) {
       Util.fail(exception);
     }
 
   }
 
   /**
    * Test method for
    * {@link jmona.gp.impl.DefaultTerminalNodeFactory#nodeClasses()}.
    */
   @Test
   public void testNodeClasses() {
     final Set<Class<TerminalNode<Integer>>> classesSet = new HashSet<Class<TerminalNode<Integer>>>();
 
     try {
       classesSet.add((Class<TerminalNode<Integer>>) Class
          .forName("jmona.gp.impl.example.ExampleTerminalNode"));
     } catch (final ClassNotFoundException exception) {
       Util.fail(exception);
     }
 
     final DefaultTerminalNodeFactory<Integer> factory = new DefaultTerminalNodeFactory<Integer>(
         classesSet);
 
     assertTrue(factory.nodeClasses().contains(ExampleTerminalNode.class));
     assertEquals(1, factory.nodeClasses().size());
   }
 
 }
