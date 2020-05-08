 /**
  * *******************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.translation.fidel;
 
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.translation.Decoder;
 import eu.monnetproject.translation.DecoderWeights;
 import eu.monnetproject.translation.Feature;
 import eu.monnetproject.translation.Label;
 import eu.monnetproject.translation.PhraseTable;
 import eu.monnetproject.translation.PhraseTableEntry;
 import eu.monnetproject.translation.Translation;
 import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
 import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
 import it.unimi.dsi.fastutil.objects.Object2IntMap;
 import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
 import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
 import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author John McCrae
  */
 public class FidelDecoderWrapper implements Decoder {
 
     private final IntegerLanguageModel languageModel;
     private final Object2IntMap<String> srcWordMap, trgWordMap;
     private final Int2ObjectMap<String> srcInvMap, invWordMap;
     private final int distortionLimit = Integer.parseInt(System.getProperty("distortionlimit", "5"));
     private final DecoderWeights weights;
 
     public FidelDecoderWrapper(IntegerLanguageModel languageModel, DecoderWeights weights) {
         this.languageModel = languageModel;
         this.srcWordMap = new Object2IntOpenHashMap<String>();
         this.srcInvMap = new Int2ObjectOpenHashMap<String>();
         this.trgWordMap = languageModel.wordMap();
         this.invWordMap = languageModel.invWordMap();
         this.weights = weights;
     }
 
     @Override
     public List<Translation> decode(List<String> phrase, PhraseTable phraseTable, List<String> featureNames, int nBest) {
         return decode(phrase, phraseTable, featureNames, nBest, 50,false);
     }
 
     @Override
     public List<Translation> decodeFast(List<String> phrase, PhraseTable phraseTable, List<String> featureNames, int nBest) {
         return decode(phrase, phraseTable, featureNames, nBest, 20,true);
     }
 
     private List<Translation> decode(List<String> phrase, PhraseTable phraseTable, List<String> featureNames, int nBest, int beamSize, boolean useLazy) {
         FidelDecoder.wordMap = invWordMap;
         FidelDecoder.srcWordMap = srcWordMap;
         int[] src = convertPhrase(phrase);
         Object2ObjectMap<Phrase, Collection<PhraseTranslation>> pt = convertPT(phraseTable, trgWordMap, featureNames);
         int lmN = languageModel.order();
         double[] wts = new double[featureNames.size() + FidelDecoder.PT];
         int i = FidelDecoder.PT;
         //wts[FidelDecoder.UNK] = -100 * (weights.containsKey("UnknownWord") ? weights.get("UnknownWord") : 1.0);
         wts[FidelDecoder.UNK] = -100;
         wts[FidelDecoder.DIST] = weights.containsKey("LinearDistortion") ? weights.get("LinearDistortion") : 0.0;
         wts[FidelDecoder.LM] = weights.containsKey("LM") ? weights.get("LM") : 0.0;
         for (String feat : featureNames) {
             wts[i++] = weights.containsKey(feat) ? weights.get(feat)
                     : (weights.containsKey("TM:" + feat) ? weights.get("TM:" + feat) : 0);
         }
         final Solution[] translations = FidelDecoder.decode(src, pt, languageModel, lmN, wts, distortionLimit, nBest, beamSize, useLazy);
         final StringBuilder sb = new StringBuilder();
         for (String w : phrase) {
             if (sb.length() != 0) {
                 sb.append(" ");
             }
             sb.append(w);
         }
         return convertTranslations(translations, new StringLabel(sb.toString(), phraseTable.getForeignLanguage()), phraseTable.getTranslationLanguage(), featureNames);
     }
 
     private int[] convertPhrase(List<String> phrase) {
         final int[] p = new int[phrase.size()];
         int i = 0;
         int W = srcWordMap.size();
         for (String s : phrase) {
             if (srcWordMap.containsKey(s)) {
                 p[i++] = srcWordMap.getInt(s);
             } else {
                 p[i++] = ++W;
                 srcWordMap.put(s, W);
                 srcInvMap.put(W, s);
             }
         }
         return p;
     }
 
     private Phrase convertPhrase(String[] phrase, Object2IntMap<String> dict) {
         final int[] p = new int[phrase.length];
         int i = 0;
         int W = dict.size();
         for (String s : phrase) {
             if (dict.containsKey(s)) {
                 p[i++] = dict.getInt(s);
             } else {
                 p[i++] = ++W;
                 if (srcWordMap == dict) {
                     dict.put(s, W);
                     srcInvMap.put(W,s);
                 }
             }
         }
         return new Phrase(p);
     }
 
     private Object2ObjectMap<Phrase, Collection<PhraseTranslation>> convertPT(PhraseTable phraseTable, Object2IntMap<String> trgDict, List<String> featureNames) {
         final Object2ObjectOpenHashMap<Phrase, Collection<PhraseTranslation>> pt = new Object2ObjectOpenHashMap<Phrase, Collection<PhraseTranslation>>();
         for (PhraseTableEntry pte : phraseTable) {
             final Phrase src;// = convertPhrase(FairlyGoodTokenizer.split(pte.getForeign().asString()), srcWordMap);
             final Phrase trg;// = convertPhrase(FairlyGoodTokenizer.split(pte.getTranslation().asString()), trgDict);
             synchronized(srcWordMap) {
                 src = convertPhrase(FairlyGoodTokenizer.split(pte.getForeign().asString()), srcWordMap);
             }
             synchronized(trgDict) {
                 trg = convertPhrase(FairlyGoodTokenizer.split(pte.getTranslation().asString()), trgDict);
             }
             final double[] wts = convertWeights(pte.getFeatures(), featureNames);
             final PhraseTranslation translation = new PhraseTranslation(trg.p, wts);
             if (!pt.containsKey(src)) {
                 pt.put(src, new LinkedList<PhraseTranslation>());
             }
             pt.get(src).add(translation);
         }
         return pt;
     }
 
     private List<Translation> convertTranslations(Solution[] translations, Label srcLabel, Language trgLang, List<String> featureNames) {
         final ArrayList<Translation> converted = new ArrayList<Translation>();
         for (Solution soln : translations) {
             Feature[] features = new Feature[FidelDecoder.PT + featureNames.size()];
             final double[] solnFeatures = soln.features();
             features[FidelDecoder.UNK] = new Feature("UnknownWord", solnFeatures[FidelDecoder.UNK]);
             features[FidelDecoder.DIST] = new Feature("LinearDistortion", solnFeatures[FidelDecoder.DIST]);
             features[FidelDecoder.LM] = new Feature("LM", solnFeatures[FidelDecoder.LM]);
             int i = FidelDecoder.PT;
             for(String featName : featureNames) {
                 features[i] = new Feature(featName.startsWith("TM:") ? featName : ("TM:" + featName), solnFeatures[i]);
                 i++;
             }
             if (soln != null) {
                 converted.add(new TranslationImpl(soln, srcLabel, trgLang, invWordMap, srcInvMap,features));
             }
         }
         return converted;
     }
 
     private double[] convertWeights(Feature[] features, List<String> featureNames) {
         double[] wts = new double[featureNames.size()];
         int i = 0;
         for (String featureName : featureNames) {
             for (Feature feat : features) {
                 if (feat.name.equals(featureName)) {
                     wts[i] += feat.score;
                 }
             }
             i++;
         }
         return wts;
     }
 
     private static class TranslationImpl implements Translation {
 
         final Solution solution;
         final Label srcLabel;
         final Label trgLabel;
         final Feature[] features;
 
         public TranslationImpl(Solution solution, Label srcLabel, Language trgLang, Int2ObjectMap<String> invMap, Int2ObjectMap<String> srcInvMap, Feature[] features) {
             this.solution = solution;
             this.srcLabel = srcLabel;
             this.features = features;
             StringBuilder sb = new StringBuilder();
             for (int w : solution.soln()) {
                 if (sb.length() != 0) {
                     sb.append(" ");
                 }
                 if (w >= 0) {
                    sb.append(invMap.get(w));
                 } else {
                     sb.append(srcInvMap.get(-w));
                 }
             }
             this.trgLabel = new StringLabel(sb.toString(), trgLang);
         }
 
         @Override
         public Label getSourceLabel() {
             return srcLabel;
         }
 
         @Override
         public Label getTargetLabel() {
             return trgLabel;
         }
 
         @Override
         public URI getEntity() {
             return null;
         }
 
         @Override
         public double getScore() {
             return solution.score();
         }
 
         @Override
         @SuppressWarnings("unchecked")
         public Collection<Feature> getFeatures() {
             return Arrays.asList(features);
         }
     }
 
     private static class StringLabel implements Label {
 
         private final String label;
         private final Language language;
 
         public StringLabel(String label, Language language) {
             this.label = label;
             this.language = language;
         }
 
         @Override
         public String asString() {
             return label;
         }
 
         @Override
         public Language getLanguage() {
             return language;
         }
     }
     
 }
