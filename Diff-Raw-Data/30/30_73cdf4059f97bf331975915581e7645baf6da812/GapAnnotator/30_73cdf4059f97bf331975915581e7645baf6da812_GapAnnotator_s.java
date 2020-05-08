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
 package com.github.fhirschmann.clozegen.lib.components;
 
 import com.github.fhirschmann.clozegen.lib.components.api.ConstraintBasedAnnotator;
 import com.github.fhirschmann.clozegen.lib.adapters.api.GeneratorAdapter;
 import com.github.fhirschmann.clozegen.lib.generators.api.Gap;
 import com.github.fhirschmann.clozegen.lib.generators.api.GapGenerator;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import com.google.common.base.Optional;
 import java.util.List;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.descriptor.ExternalResource;
 
 /**
  * This annotator annotates words with a {@link GapAnnotation}.
  *
  * @see com.github.fhirschmann.clozegen.lib.examples
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class GapAnnotator extends ConstraintBasedAnnotator {
     /**
      * <em>[mandatory]</em>
      *
      * The argument to this keyword should be a {@link GeneratorAdapter} which
      * knows how to create a {@link GapGenerator}.
      */
     public static final String ADAPTER_KEY = "Adapter";
     @ExternalResource(key = ADAPTER_KEY)
     private GeneratorAdapter adapter;
 
     /**
      * <em>[optional]</em>
      *
      * The number of invalid answers to generate. This argument is optional
      * and defaults to 4.
      *
      * <p>
      * Please note that it is up to the underlying generator to respect this
      * parameter.
      * </p>
      */
     public static final String PARAM_ANSWER_COUNT = "AnswerCount";
     @ConfigurationParameter(name = PARAM_ANSWER_COUNT,
            mandatory = false, defaultValue = "4")
     private int answerCount;
 
     @Override
     public void process(final JCas jcas, final List<Annotation> annotationList,
             final int index) {
         Optional<Gap> gap = adapter.generator(annotationList, index).
                 generate(answerCount);
 
         if (gap.isPresent()) {
             GapAnnotation gapAnnotation = UIMAUtils.createGapAnnotation(jcas, gap.get());
             UIMAUtils.copyBounds(annotationList.get(index), gapAnnotation);
             gapAnnotation.addToIndexes();
         }
     }
 }
