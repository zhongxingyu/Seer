 package com.pocketcookies.zipcodeconverter;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.vividsolutions.jts.geom.Coordinate;
 import junit.framework.TestCase;
 
 /**
  * Tests for {@link CoordinateToZipCode}.
  * 
  * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
  */
 public class CoordinateToZipCodeTest extends TestCase {
 
     public void testZipCode() throws Exception {
         assertEquals("20815", Iterables.getOnlyElement(
                 CoordinateToZipCode.getZipCodes(
                ImmutableSet.of(new Coordinate(-77.06720, 39.00404)))
                 .entrySet()).getValue());
     }
     
     public void testMissingZipCode() throws Exception {
        assertEquals(ImmutableMap.of(), CoordinateToZipCode.getZipCodes(ImmutableSet.of(new Coordinate(37.71859, -39.023437))));
     }
 }
