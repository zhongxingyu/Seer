 /**
  * BinaryStringIteratorTester.java
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
 package jmona.ga.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.NoSuchElementException;
 
 import jmona.ga.BinaryString;
 import jmona.test.Util;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test class for the BinaryStringIterator class.
  * 
  * @author jfinkels
  */
 public class BinaryStringIteratorTester {
 
   /** The iterator under test. */
   private BinaryStringIterator iterator = null;
   /** The binary string over which to iterate. */
   private BinaryString bitstring = null;
   /** The length of the binary string in these tests. */
   public static final int LENGTH = 10;
 
   /** Establish a fixture for tests in this class. */
   @Before
   public final void setUp() {
     this.bitstring = new CharArrayBinaryString(LENGTH);
     this.iterator = new BinaryStringIterator(this.bitstring);
   }
 
   /**
    * Test method for
   * {@link jmona.ga.impl.BinaryStringIterator#BitStringIterator(jmona.ga.BinaryString)}
    * .
    */
   @Test
   public void testBitStringIterator() {
     assertTrue(this.iterator instanceof BinaryStringIterator);
   }
 
   /**
    * Test method for {@link jmona.ga.impl.BinaryStringIterator#hasNext()}.
    */
   @Test
   public void testHasNext() {
     for (int i = 0; i < LENGTH; ++i) {
       assertTrue(this.iterator.hasNext());
       this.iterator.next();
     }
 
     assertFalse(this.iterator.hasNext());
 
   }
 
   /**
    * Test method for {@link jmona.ga.impl.BinaryStringIterator#next()}.
    */
   @Test
   public void testNext() {
    
     while (this.iterator.hasNext()) {
       assertEquals(0, this.iterator.next().intValue());
     }
 
     try {
       this.iterator.next();
       Util.shouldHaveThrownException();
     } catch (final NoSuchElementException exception) {
       assertTrue(exception instanceof NoSuchElementException);
     }
 
     final int index = 5;
 
     this.bitstring.flipBit(index);
 
     this.iterator = new BinaryStringIterator(this.bitstring);
 
     int i = 0;
     while (this.iterator.hasNext()) {
       if (i == index) {
         assertEquals(1, this.iterator.next().intValue());
       } else {
         assertEquals(0, this.iterator.next().intValue());
       }
       i += 1;
     }
   }
 
   /**
    * Test method for {@link jmona.ga.impl.BinaryStringIterator#remove()}.
    */
   @Test
   public void testRemove() {
     try {
       this.iterator.remove();
       Util.shouldHaveThrownException();
     } catch (final UnsupportedOperationException exception) {
       assertTrue(exception instanceof UnsupportedOperationException);
     }
   }
 
 }
