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
 
 import com.github.fhirschmann.clozegen.lib.components.api.GapProcessor;
 import com.github.fhirschmann.clozegen.lib.functions.CoveredTextFunction;
 import com.github.fhirschmann.clozegen.lib.functions.EscapeNullFunction;
 import com.github.fhirschmann.clozegen.lib.generators.api.Gap;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.google.common.base.Functions;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
 import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import org.apache.uima.UIMAException;
 import org.apache.uima.cas.FSMatchConstraint;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.NonEmptyStringList;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.uimafit.factory.JCasFactory;
 import org.uimafit.util.FSCollectionFactory;
 import org.uimafit.util.JCasUtil;
import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Utility functions for UIMA related stuff.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class UIMAUtils {
     /** Utility class cannot be called. */
     private UIMAUtils() {
     }
 
     /**
      * Creates a {@link GapAnnotation} from <code>validAnswers</code> and
      * <code>invalidAnswers</code>. The resulting list of all answer options
      * of a gap will be composed of union of <code>validAnswers</code> and
      * <code>invalidAnswers</code> in random order.
      *
      * @param aJCas The {@link JCas} this Annotation belongs to
      * @param validAnswers the valid answers for this gap
      * @param invalidAnswers the invalid answers for this gap
      * @return a new {@link GapAnnotation}
      */
     public static GapAnnotation createGapAnnotation(final JCas aJCas,
             final Set<String> validAnswers, final Set<String> invalidAnswers) {
         final GapAnnotation annotation = new GapAnnotation(aJCas);
 
         final List<String> allAnswers = Lists.newArrayList(
                 Sets.union(invalidAnswers, validAnswers));
         Collections.shuffle(allAnswers);
 
         final NonEmptyStringList all = (NonEmptyStringList)
                 FSCollectionFactory.createStringList(
                 aJCas, allAnswers);
         annotation.setAllAnswers(all);
 
         final NonEmptyStringList valid = (NonEmptyStringList)
                 FSCollectionFactory.createStringList(
                 aJCas, validAnswers);
         annotation.setValidAnswers(valid);
 
         return annotation;
     }
 
     /**
      * Creates a {@link GapAnnotation} from a {@link Gap}.
      *
      * @param aJCas The {@link JCas} this Annotation belongs to
      * @param gap the gap to create the annotation for
      * @see UIMAUtils#createGapAnnotation(JCas, Set)}
      * @return a new {@link GapAnnotation}
      */
     public static GapAnnotation createGapAnnotation(final JCas aJCas, final Gap gap) {
         return createGapAnnotation(aJCas, gap.getValidAnswers(), gap.getInvalidAnswers());
     }
 
     /**
      * Copy the bounds ({@link Annotation#getBegin()} and {@link Annotation#getEnd()})
      * from the <code>source</code> annotation to the <code>destination</code>
      * annotation.
      *
      * @param source the source annotation
      * @param destination the destination annotation
      */
     public static void copyBounds(final Annotation source, final Annotation destination) {
         destination.setBegin(source.getBegin());
         destination.setEnd(source.getEnd());
     }
 
     /**
      * Returns a List of <code>T</code> of the <code>num</code> neighbors.
      *
      * @param <T> the annotation type
      * @param clazz the class of the annotation type
      * @param annotationList the list of annotation
      * @param offset the offset of the element in <code>annotationList</code>
      * @param num the number of neighbors to receive
      * @return a list of neighbors
      */
     public static <T extends Annotation> List<T> getAdjacentAnnotations(
             final Class<T> clazz, final List<Annotation> annotationList,
             final int offset, final int num) {
         // Get a list of all T tags
         final List<T> tList = Lists.newArrayList(
                 Iterables.filter(annotationList, clazz));
 
         // Get the neighbors of our T tag
         final List<T> adjacent = CollectionUtils.getNullPaddedAdjacentTo(
                 tList, tList.indexOf(annotationList.get(offset)), 1);
 
         return adjacent;
     }
 
     /**
      * Returns the covered text of the list of {@link Annotation}s produced
      * by {@link UIMAUtils#getAdjacentAnnotations(Class, List, int, int)}.
      * {@code null} will be replaced with "NULL" by {@link EscapeNullFunction}.
      *
      * @param <T> the annotation type
      * @param clazz the class of the annotation type
      * @param annotationList the list of annotation
      * @param offset the offset of the element in <code>annotationList</code>
      * @param num the number of neighbors to receive
      * @return a list of neighbors (covered text)
      */
     public static <T extends Annotation> List<String> getAdjacentTokens(
             final Class<T> clazz, final List<Annotation> annotationList,
             final int offset, final int num) {
 
         final List<T> adjacent = getAdjacentAnnotations(
                 clazz, annotationList, offset, num);
 
         // The tokens of the trigram (A, p, B)
         final List<String> tokens = Lists.newArrayList(Collections2.transform(
                 adjacent, Functions.compose(
                 new EscapeNullFunction(), new CoveredTextFunction())));
 
         return tokens;
     }
 
     /**
      * Calls <code>processor</code> for each {@link Annotation} matched by
      * <code>constraint</code> in a sentence.
      *
      * @param jcas the JCas to work on
      * @param constraint the constraint to match
      * @param processor the callback
      */
     public static void annotationCaller(final JCas jcas,
             final FSMatchConstraint constraint, final GapProcessor processor) {
         for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
             int i = 0;
             List<Annotation> alist = JCasUtil.selectCovered(Annotation.class, sentence);
             for (Annotation annotation : alist) {
                if ((checkNotNull(constraint).match(annotation))) {
                     processor.process(jcas, alist, i);
                 }
                 i++;
             }
         }
     }
 
     /**
      * Creates a JCas solely meant for testing purposes.
      *
      * @return a sample jcas
      * @throws UIMAException on errors creating the JCas
      */
     public static JCas createTestJCas() throws UIMAException {
         JCas jcas = JCasFactory.createJCas();
         jcas.setDocumentLanguage("en");
         jcas.setDocumentText("He studies at the university. He can't think of anything.");
 
         DocumentMetaData metadata = DocumentMetaData.create(jcas);
         metadata.setDocumentId("test");
         return jcas;
     }
 }
