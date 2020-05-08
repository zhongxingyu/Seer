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
 package com.github.fhirschmann.clozegen.lib.generators;
 
 import com.github.fhirschmann.clozegen.lib.generators.CollocationGapGenerator;
 import com.github.fhirschmann.clozegen.lib.generators.api.Gap;
 import com.github.fhirschmann.clozegen.lib.generators.model.CollocationModel;
 import com.google.common.base.Optional;
 import com.google.common.collect.LinkedHashMultiset;
 import com.google.common.collect.Multiset;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class CollocationGapGeneratorTest {
     private CollocationModel model;
     private CollocationGapGenerator generator;
 
     @Before
     public void setUp() {
         Multiset<String> trigrams = LinkedHashMultiset.create();
         trigrams.add("as in the", 20);
         trigrams.add("as of the", 11);
         trigrams.add("as by a", 55);
 
         model = new CollocationModel();
         model.setMultiset(trigrams);
     }
 
     @Test
     public void testGenerate() {
         generator = new CollocationGapGenerator("as", "in", "the", model);
         Gap gap = new Gap();
         gap.addValidAnswers("in");
         gap.addInvalidAnswers("by");
         assertEquals(Optional.of(gap), generator.generate(2));
     }
 
     @Test
     public void testTrigramConstraint() {
         model.getMultiset().add("as of the");
         generator = new CollocationGapGenerator("as", "in", "the", model);
         Gap gap = new Gap();
         gap.addValidAnswers("in");
         gap.addInvalidAnswers("by");
         assertEquals(Optional.of(gap), generator.generate(2));
     }
 
     @Test
     public void testNull() {
         generator = new CollocationGapGenerator("xx", "yy", "zz", model);
        assertEquals(Optional.absent(), generator.generate(2));
     }
 }
