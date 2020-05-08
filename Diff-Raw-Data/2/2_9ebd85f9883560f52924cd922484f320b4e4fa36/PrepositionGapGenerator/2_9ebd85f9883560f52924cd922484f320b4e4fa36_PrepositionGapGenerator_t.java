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
 package com.github.fhirschmann.clozegen.lib.annotators.en;
 
 import com.github.fhirschmann.clozegen.lib.annotators.AbstractPosTrigramAnnotator;
 import com.github.fhirschmann.clozegen.lib.multiset.MapMultiset;
 import com.github.fhirschmann.clozegen.lib.parser.FrequencyParser;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.github.fhirschmann.clozegen.lib.util.MultisetUtils;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import com.github.fhirschmann.clozegen.lib.util.WordFilterFunction;
 import com.google.common.base.Joiner;
 import com.google.common.collect.*;
 import com.google.common.collect.Multiset.Entry;
 import com.google.common.io.Resources;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.uima.UimaContext;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.NonEmptyStringList;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.util.FSCollectionFactory;
 
 /**
  * This annotator creates annotations for prepositions.
  *
  * <p>It implements the concepts as proposed in the paper
  * <i>Automatic Generation of Cloze Items for Prepositions</i> [1]
 * by Lee et all.
  *
  * <p>Three collocation data files which describe the frequency of preposition
  * collocations need to be present in the model directory:
  * <ul>
  * <li>trigrams.txt: prepositions located in the middle
  * <li>before.txt: preposition located on the left-hand side
  * <li>after.txt: preposition located on the right-hand side
  * </ul>
  * The space separated word sequence and the corresponding counts need
  * to be separated by the tab-character. For more detail on the format,
  * please consult the documentation on {@link FrequencyParser#parseMultiset}
  * and {@link FrequencyParser#parseMapMultiset}, which describe the format
  * for {before|after}.txt and trigrams.txt, respectively.
  *
  * <p>[1] <b>J. Lee and S. Seneff</b>.<br/>
  * Automatic generation of cloze items for prepositions.<br/>
  * <i>In Eight Annual Conference of the International Speech Communication
  * Association, 2007</i>.
  *
  * @param PARAM_MODEL_PATH the directory in which the models can be found
  * @param CHOICES_COUNT the number of answers to generate
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class PrepositionGapGenerator extends AbstractPosTrigramAnnotator {
 
     public static final String PARAM_MODEL_PATH = "ModelPath";
     @ConfigurationParameter(name = PARAM_MODEL_PATH, mandatory = true)
     private String modelPath;
 
     private Multiset<String> trigrams;
     private MapMultiset<String, String> after;
     private MapMultiset<String, String> before;
 
     private final static Joiner joiner = Joiner.on(" ");
 
     public final static int CHOICES_COUNT = 4;
 
     @Override
     public void initialize(UimaContext context) throws ResourceInitializationException {
         super.initialize(context);
         try {
             trigrams = FrequencyParser.parseMultiset(
                     Resources.getResource(modelPath + "/trigrams.txt"));
             after = FrequencyParser.parseMapMultiset(
                     Resources.getResource(modelPath + "/after.txt"), 0);
             before = FrequencyParser.parseMapMultiset(
                     Resources.getResource(modelPath + "/before.txt"), 1);
         } catch (IOException ex) {
             Logger.getLogger(PrepositionGapGenerator.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     @Override
     public void processTrigram(JCas aJCas, List<POS> pos) {
             List<String> strings = Lists.newArrayList(Collections2.
                     transform(pos, new WordFilterFunction()));
 
             final Multiset<String> candidates = ConcurrentHashMultiset.create(
                     MultisetUtils.mergeMultiSets(
                     before.get(strings.get(0)), after.get(strings.get(0))));
 
             for (Entry<String> entry : candidates.entrySet()) {
                 if (trigrams.contains(
                         joiner.join(strings.get(0), entry.getElement(), strings.get(1)))) {
                     candidates.remove(entry.getElement(), entry.getCount());
                 }
             }
             candidates.remove(strings.get(1), candidates.count(strings.get(1)));
 
             if (candidates.elementSet().size() > CHOICES_COUNT - 2) {
                 final Set<String> invalidAnswers = Sets.newHashSet(
                         MultisetUtils.sortedElementList(candidates, CHOICES_COUNT - 1));
 
                 GapAnnotation gap = UIMAUtils.createGapAnnotation(aJCas,
                         ImmutableSet.of(strings.get(1)), invalidAnswers);
                 gap.setBegin(pos.get(1).getBegin());
                 gap.setEnd(pos.get(1).getEnd());
                 gap.addToIndexes();
 
             }
     }
 }
