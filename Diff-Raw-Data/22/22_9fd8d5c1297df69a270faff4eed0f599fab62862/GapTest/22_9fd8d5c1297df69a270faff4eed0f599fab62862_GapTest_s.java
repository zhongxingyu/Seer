 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.generator;
 
 import com.google.common.collect.Sets;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class GapTest {
     private Gap gap;
     private Gap gap2;
 
     @Before
     public void setUp() {
         gap = new Gap();
         gap.setValidAnswers("val1", "val2");
         gap.setInvalidAnswers("inval1", "inval2", "inval3");
         gap2 = new Gap();
         gap2.setValidAnswers("val1", "val2");
        gap2.setInvalidAnswers("inval1", "inval2", "inval3");
     }
 
     @Test
     public void testGetInvalidAnswers() {
         assertEquals(Sets.newHashSet("inval1", "inval2", "inval3"),
                 gap.getInvalidAnswers());
     }
 
     @Test
     public void testGetValidAnswers() {
         assertEquals(Sets.newHashSet("val1", "val2"), gap.getValidAnswers());
     }
 
     @Test
     public void testGetAllAnswers() {
         assertEquals(Sets.newHashSet("inval1", "inval2", "inval3", "val1", "val2"),
                 gap.getAllAnswers());
     }
 
     @Test
     public void testHashCode() {
         assertEquals(gap2.hashCode(), gap.hashCode());
     }
 
     @Test
     public void testEquals() {
         assertEquals(gap2, gap);
     }
 }
