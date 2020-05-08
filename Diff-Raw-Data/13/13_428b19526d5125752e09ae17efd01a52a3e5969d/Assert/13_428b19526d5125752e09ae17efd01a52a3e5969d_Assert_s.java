 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.gramath.support;
 
 import static org.junit.Assert.assertNotNull;
 
 
 /**
  * Additional asserts.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public final class Assert
 {
     /**
      * Checks if the two arrays have the same values.
      *
      * @param a
      *            The first array.
      * @param b
      *            The second array.
      * @param precision
      *            The number precision.
      */
 
     public static void assertArrayEquals(final float[] a, final double[] b,
         final double precision)
     {
         assertNotNull(a);
         assertNotNull(b);
        org.junit.Assert.assertEquals(a.length, b.length);
         for (int i = 0; i < a.length; i++)
            org.junit.Assert.assertEquals(a[i], b[i], precision);
     }
 
 
     /**
      * Checks if the two arrays have the same values.
      *
      * @param a
      *            The first array.
      * @param b
      *            The second array.
      * @param precision
      *            The number precision.
      */
 
     public static void assertArrayEquals(final double[] a, final float[] b,
         final double precision)
     {
         assertNotNull(a);
         assertNotNull(b);
        org.junit.Assert.assertEquals(a.length, b.length);
         for (int i = 0; i < a.length; i++)
            org.junit.Assert.assertEquals(a[i], b[i], precision);
     }
 }
