 /**
  * UtilTester.java
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
 package jmona.impl;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
 /**
  * Test class for the jmona.impl.Util class.
  * 
  * @author jfinkels
  */
 public class UtilTester {
 
   /**
    * Test method for {@link jmona.impl.Util#randomFromSet(java.util.Set)}.
    */
   @Test
   public void testRandomFromSet() {
     final Set<Object> set = new HashSet<Object>();
     final Object object1 = new Object();
     final Object object2 = new Object();
     final Object object3 = new Object();
 
     set.add(object1);
     set.add(object2);
     set.add(object3);
 
     Map<Object, Integer> selectionsMap = new HashMap<Object, Integer>();
 
     Object choice = null;
     for (int i = 0; i < NUM_TESTS; ++i) {
       choice = Util.randomFromSet(set);
 
       if (selectionsMap.containsKey(choice)) {
         selectionsMap.put(choice, selectionsMap.get(choice) + 1);
       } else {
         selectionsMap.put(choice, 1);
       }
     }
 
     int sum = 0;
     for (final Integer selections : selectionsMap.values()) {
       sum += selections;
     }
 
     final double meanSelections = (double) sum / selectionsMap.size();
    
     final double delta = meanSelections * 0.1;
    
     for (final Integer selection : selectionsMap.values()) {
       assertEquals(meanSelections, selection, delta);
     }
   }
 
   /** The number of times to repeat the test. */
   public static final int NUM_TESTS = 10000;
 
 }
