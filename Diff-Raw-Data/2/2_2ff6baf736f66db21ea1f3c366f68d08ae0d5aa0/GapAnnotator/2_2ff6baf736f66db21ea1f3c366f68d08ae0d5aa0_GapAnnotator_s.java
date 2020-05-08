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
 package com.github.fhirschmann.clozegen.lib.component;
 
 import com.github.fhirschmann.clozegen.lib.component.api.AbstractAnnotator;
 import com.github.fhirschmann.clozegen.lib.adapter.api.GeneratorAdapter;
 import com.github.fhirschmann.clozegen.lib.generator.Gap;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import java.util.List;
 import org.apache.uima.cas.FSMatchConstraint;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.descriptor.ExternalResource;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class GapAnnotator extends AbstractAnnotator {
     /**
      * The wrapper which implements {@link GeneratorAdapter}.
      */
    public static final String ADAPTER_KEY = "GapAnnotatorInterface";
     @ExternalResource(key = ADAPTER_KEY)
     private GeneratorAdapter adapter;
 
     /**
      * The number of invalid answers to generate.
      */
     public static final String PARAM_ANSWER_COUNT = "AnswerCount";
     @ConfigurationParameter(name = PARAM_ANSWER_COUNT,
             mandatory = false, defaultValue = "4")
     private int answerCount;
 
     @Override
     public FSMatchConstraint getConstraint() {
         return adapter.getConstraint();
     }
 
     @Override
     public void process(final JCas jcas, final List<Annotation> annotationList,
             final int index) {
         Gap gap = adapter.generator(annotationList, index).generate(answerCount);
         GapAnnotation gapAnnotation = UIMAUtils.createGapAnnotation(jcas, gap);
         UIMAUtils.copyBounds(annotationList.get(index), gapAnnotation);
         gapAnnotation.addToIndexes();
     }
 }
