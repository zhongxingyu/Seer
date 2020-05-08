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
 
 import com.google.common.collect.Range;
 import com.google.common.collect.Ranges;
 import com.google.common.collect.Sets;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
 import static org.uimafit.util.JCasUtil.selectCovered;
 import static org.uimafit.util.JCasUtil.select;
 import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;
 import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
 import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
 import de.tudarmstadt.ukp.dkpro.core.ngrams.NGramIterable;
 import java.util.HashSet;
 import java.util.Iterator;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class FilteredNGramAnnotator extends JCasAnnotator_ImplBase {
     public static final String PARAM_N = "N";
     @ConfigurationParameter(name = PARAM_N, mandatory = true, defaultValue = "3")
     private int n;

     private HashSet<Range> prepositions;
 
     @Override
     public void initialize(UimaContext context) throws ResourceInitializationException {
         super.initialize(context);
         prepositions = Sets.newHashSet();
     }
 
     @Override
     public void process(JCas aJCas) throws AnalysisEngineProcessException {
         for (final Iterator<Annotation> i = aJCas.getAnnotationIndex(
                 PP.type).iterator(); i.hasNext();) {
             final Annotation subject = i.next();
             prepositions.add(Ranges.closed(subject.getBegin(), subject.getEnd()));
         }
 
         Range range;
 
         for (Sentence s : select(aJCas, Sentence.class)) {
             for (NGram ngram : NGramIterable.create(selectCovered(Token.class, s), n)) {
                 range = Ranges.closed(ngram.getBegin(), ngram.getEnd());
                 for (Range preposition : prepositions) {
                     if (range.encloses(preposition)) {
                         ngram.addToIndexes();
                     }
                 }
             }
         }
     }
 }
