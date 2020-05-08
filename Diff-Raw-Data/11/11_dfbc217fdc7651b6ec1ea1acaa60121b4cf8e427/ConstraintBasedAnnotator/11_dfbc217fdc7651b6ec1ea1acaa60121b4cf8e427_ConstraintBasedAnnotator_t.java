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
 package com.github.fhirschmann.clozegen.lib.component.api;
 
 import com.github.fhirschmann.clozegen.lib.constraint.api.ConstraintProvider;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import java.util.List;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSMatchConstraint;
 import org.apache.uima.jcas.JCas;
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ExternalResource;
 
 /**
  * Implementing classes have to provide a constraint which, when matched,
  * will trigger a call to {@link GapProcessor#process(JCas, List, int)}.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public abstract class ConstraintBasedAnnotator extends
         JCasAnnotator_ImplBase implements ConstraintProvider, GapProcessor {
     /**
      * <em>[mandatory]</em>
      *
     * A constraint which limits the words {@link GapProcessor#process(JCas, List, int)}
     * is called for.
      *
      * @see com.github.fhirschmann.clozegen.lib.constraint
      */
     public static final String CONSTRAINT_KEY = "Constraint";
     @ExternalResource(key = CONSTRAINT_KEY, mandatory = true)
     private ConstraintProvider constraint;
 
     @Override
     public void process(final JCas aJCas) throws AnalysisEngineProcessException {
         UIMAUtils.annotationCaller(aJCas, getConstraint(), this);
     }
 
     @Override
     public FSMatchConstraint getConstraint() {
         return constraint.getConstraint();
     }
 }
