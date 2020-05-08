 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.collada.model;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 
 /**
  * Tests the BoolArray class.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public class BoolArrayTest
 {
     /**
      * Tests the constructor.
      */
 
     @Test
     public void testConstructor()
     {
         final BoolArray array = new BoolArray(16);
        assertEquals(16, ((Array) array).getCount());
        assertNull(((Array) array).getId());
        assertNull(((Array) array).getName());
     }
 
 
     /**
      * Tests the constructor with a negative count.
      */
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNegativeCount()
     {
         new BoolArray(-1).toString();
     }
 
 
     /**
      * Tests the setCount() method.
      */
 
     @Test
     public void testSetCount()
     {
         final BoolArray array = new BoolArray(4);
         array.getData()[0] = true;
         array.getData()[1] = true;
         array.getData()[2] = true;
         array.getData()[3] = true;
        assertSame(array, ((Array) array).setCount(6));
         assertEquals(6, array.getCount());
        assertNotNull(((Array) array).getData());
         assertTrue(array.getData()[0]);
         assertTrue(array.getData()[1]);
         assertTrue(array.getData()[2]);
         assertTrue(array.getData()[3]);
         array.setCount(2);
         assertEquals(2, array.getCount());
         assertTrue(array.getData()[0]);
         assertTrue(array.getData()[1]);
     }
 
 
     /**
      * Tests the name.
      */
 
     @Test
     public void testName()
     {
         final BoolArray array = new BoolArray(4);
         assertNull(array.getName());
        assertSame(array, ((Array) array).setName("foo"));
         assertEquals("foo", array.getName());
         array.setName(null);
         assertNull(array.getName());
     }
 
 
     /**
      * Tests the ID.
      */
 
     @Test
     public void testId()
     {
         final BoolArray array = new BoolArray(4);
         assertNull(array.getId());
        assertSame(array, ((Element) array).setId("foo"));
         assertEquals("foo", array.getId());
         array.setId(null);
         assertNull(array.getId());
     }
 }
