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
 package com.github.fhirschmann.clozegen.lib.util;
 
 import com.github.fhirschmann.clozegen.lib.generator.Gap;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import java.lang.reflect.Constructor;
 import java.util.Collection;
 import java.util.List;
 import org.apache.uima.UIMAException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.junit.*;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.matchers.JUnitMatchers.*;
 import org.uimafit.factory.JCasFactory;
 import org.uimafit.util.FSCollectionFactory;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class UIMAUtilsTest {
     private JCas jcas;
     private GapAnnotation gap1;
     private GapAnnotation gap2;
 
     @Before
     public void setUp() throws UIMAException {
         jcas = JCasFactory.createJCas();
         jcas.setDocumentText("This is a sample text.");
 
         gap1 = UIMAUtils.createGapAnnotation(jcas,
                 Sets.newHashSet("true1"), Sets.newHashSet("false1", "false2"));
         gap1.setBegin(0);
         gap1.setEnd(4);
         gap1.addToIndexes();
 
         gap2 = UIMAUtils.createGapAnnotation(jcas,
                 Sets.newHashSet("true1", "true2"), Sets.newHashSet("false1", "false2"));
         gap2.setBegin(5);
         gap2.setEnd(7);
         gap2.addToIndexes();
     }
 
     @Test
     public void testCreateGapAnnotationAll() throws UIMAException {
         Collection<String> list = FSCollectionFactory.create(gap1.getAllAnswers());
         assertThat(list, hasItems("true1", "false1", "false2"));
     }
 
     @Test
     public void testCreateGapAnnotationValid() throws UIMAException {
         Collection<String> list = FSCollectionFactory.create(gap2.getValidAnswers());
         assertThat(list, hasItems("true1", "true2"));
         assertThat(list, not(hasItems("false2")));
     }
 
     /**
      * Hack to exclude the private constructor from code coverage metrics.
      */
     @Test
     public void testPrivateConstructor() throws Exception {
         Constructor<?>[] cons = UIMAUtils.class.getDeclaredConstructors();
         cons[0].setAccessible(true);
         cons[0].newInstance((Object[]) null);
     }
 
     @Test
     public void testCreateGapAnnotation_3args() {
         Gap gap = new Gap();
         gap.addInvalidAnswers("foo");
         gap.addValidAnswers("bar");
         GapAnnotation an = UIMAUtils.createGapAnnotation(jcas, gap);
         assertThat(an.getValidAnswers().getHead(), is("bar"));
     }
 
     @Test
     public void testCopyBounds() {
         UIMAUtils.copyBounds(gap1, gap2);
         assertThat(gap1.getBegin(), is(gap2.getBegin()));
         assertThat(gap1.getEnd(), is(gap2.getEnd()));
     }
 
     @Test
     public void testGetAdjacentAnnotations() {
         List<Annotation> list = Lists.newLinkedList();
         list.add(gap1);
         list.add(gap2);
         List<GapAnnotation> result = UIMAUtils.getAdjacentAnnotations(
                 GapAnnotation.class, list, 0, 1);
         List<GapAnnotation> expected = Lists.newArrayList(null, gap1, gap2);
         assertThat(result, is(expected));
     }
 
     @Test
     public void testGetAdjacentTokens() {
         List<Annotation> list = Lists.newLinkedList();
         list.add(gap1);
         list.add(gap2);
         List<String> result = UIMAUtils.getAdjacentTokens(
                 GapAnnotation.class, list, 0, 1);
        List<String> expected = Lists.newArrayList(null, "This", "is");
         assertThat(result, is(expected));
     }
 }
