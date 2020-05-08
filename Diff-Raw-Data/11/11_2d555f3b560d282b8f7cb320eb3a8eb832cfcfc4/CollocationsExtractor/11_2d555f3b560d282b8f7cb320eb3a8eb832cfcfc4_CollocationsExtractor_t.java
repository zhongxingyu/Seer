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
 package com.github.fhirschmann.clozegen.lib.io;
 
 import com.github.fhirschmann.clozegen.lib.util.MultisetUtils;
 import com.github.fhirschmann.clozegen.lib.util.StringUtils;
 import com.google.common.base.Joiner;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Multiset;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
 import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.uimafit.component.JCasConsumer_ImplBase;
 import static org.uimafit.util.JCasUtil.select;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
public class CollocationsExtractor extends JCasConsumer_ImplBase {
 
     private Multiset<String> before;
     private Multiset<String> after;
     private Multiset<String> trigrams;
     private Multiset<String> unigrams;
     private final static Joiner joiner = Joiner.on(" ");
     private String output = "src/main/resources/frequency/prepositions";
 
     @Override
     public void initialize(final UimaContext context)
             throws ResourceInitializationException {
         super.initialize(context);
         before = HashMultiset.create();
         after = HashMultiset.create();
         trigrams = HashMultiset.create();
         unigrams = HashMultiset.create();
     }
 
     public static void addToMultiset(final Multiset<String> multiset,
             final String... tokens) {
         if (!StringUtils.containsPunctuationMark(tokens)) {
             multiset.add(joiner.join(tokens).toLowerCase());
         }
     }
 
     @Override
     public void process(JCas aJCas) throws AnalysisEngineProcessException {
         Annotation previous = null;
         Annotation next;
         Annotation current;
 
         for (Sentence sentence : select(aJCas, Sentence.class)) {
             for (final FSIterator<Annotation> i = aJCas.getAnnotationIndex(
                     POS.type).subiterator(sentence); i.hasNext();) {
                 current = i.next();
 
                 // TODO: Check for null
 
                 if ((current instanceof PP) && (previous != null)) {
                     next = i.next();
 
                     String lowered_previous = previous.getCoveredText();
                     String lowered_next = next.getCoveredText();
                     String lowered_current = current.getCoveredText();
 
                     unigrams.add(lowered_current);
                     addToMultiset(before, lowered_previous, lowered_current);
                     addToMultiset(after, lowered_current, lowered_next);
                     addToMultiset(trigrams, lowered_previous, lowered_current, lowered_next);
                 }
 
                 previous = current;
             }
         }
     }
 
     @Override
     public void collectionProcessComplete() {
         try {
             MultisetUtils.writeSortedMultiSet(trigrams, new File(output, "trigrams.txt"));
             MultisetUtils.writeSortedMultiSet(after, new File(output, "after.txt"));
             MultisetUtils.writeSortedMultiSet(before, new File(output, "before.txt"));
             MultisetUtils.writeSortedMultiSet(unigrams, new File(output, "unigrams.txt"));
         } catch (IOException ex) {
             Logger.getLogger(CollocationsExtractor.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
