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
 package com.github.fhirschmann.clozegen.lib.annotators;
 
 import com.github.fhirschmann.clozegen.lib.generator.Gap;
 import com.github.fhirschmann.clozegen.lib.generator.GapGeneratorInterface;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
 import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
 import java.util.List;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSTypeConstraint;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.util.JCasUtil;
 
 /**
  * Abstract Gap annotator class.
  *
  * <p>All annotators should inherit from this class and call their
  * gap generation algorithms in {@link AbstractGapAnnotator#generate(Annotation)}.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public abstract class AbstractGapAnnotator extends
         JCasAnnotator_ImplBase implements GapAnnotatorInterface {
 
     /** The number of answer option to produce. */
     public static final String PARAM_ANSWER_OPTIONS_COUNT = "AnswerOptionsCount";
     @ConfigurationParameter(name = PARAM_ANSWER_OPTIONS_COUNT,
             mandatory = false, defaultValue = "4")
     private int answerOptionsCount;
 
     /**
      * A constraint, possibly <code>null</code>, which limits the elements of the
      * iterator passed to
      * {@link AbstractGapAnnotator#generate(Annotation)}.
      *
      * <p>For example, if you want to only work on annotations of the type
      * {@link ART}, then this method should return <code>cons</code> like so:
      * <p><blockquote><pre>
      * {@code
      * FSTypeConstraint cons = ConstraintFactory.instance().createTypeConstraint();
      * cons.add(ART.class.getName());
      * }
      * </pre></blockquote>
      *
      * @return a new constraint
      */
     public abstract FSTypeConstraint getConstraint();
 
     /**
      * This method gets called for each word in a sentence which matches
      * {@link AbstractGapAnnotator#getConstraint()}.
      *
      * @param annotationList the list of annotations in the current sentence
      * @param offset the offset (index) of the word to generate a gap for
     * @return a set of gaps generated for the sentence
      */
     public abstract GapGeneratorInterface generator(
             List<Annotation> annotationList, int offset);
 
     @Override
     public void process(final JCas aJCas) throws AnalysisEngineProcessException {
         // Throw an exception if the language is not supported.
         if (!getSupportedLanguages().contains(aJCas.getDocumentLanguage())) {
             throw new UnsupportedOperationException(
                     "The annotator you tried to use does not support your language!");
         }
         Gap gap;
         GapAnnotation gapAnnotation;
         FSTypeConstraint constraint = getConstraint();
 
         for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
             int i = 0;
             List<Annotation> alist = JCasUtil.selectCovered(Annotation.class, sentence);
             for (Annotation annotation : alist) {
                 if ((constraint == null) || (constraint.match(annotation))) {
                     gap = generator(alist, i).generate();
                     gapAnnotation = UIMAUtils.createGapAnnotation(aJCas, gap);
                     UIMAUtils.copyBounds(annotation, gapAnnotation);
                     gapAnnotation.addToIndexes();
                 }
                 i++;
             }
         }
     }
 
     /**
      * Returns the number of answer options to generate.
      *
      * @return the number of answer options to generate
      */
     public int getAnswerOptionsCount() {
         return answerOptionsCount;
     }
 }
