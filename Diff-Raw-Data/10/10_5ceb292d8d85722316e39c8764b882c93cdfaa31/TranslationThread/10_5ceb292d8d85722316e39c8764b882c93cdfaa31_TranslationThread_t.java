 /**
  * ********************************************************************************
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
 package eu.monnetproject.translation.controller.impl;
 
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.lang.LanguageCodeFormatException;
 import eu.monnetproject.lemon.LemonModels;
 import eu.monnetproject.lemon.model.LexicalEntry;
 import eu.monnetproject.lemon.model.LexicalForm;
 import eu.monnetproject.lemon.model.Lexicon;
 import eu.monnetproject.lemon.model.Text;
 import eu.monnetproject.ontology.Entity;
 import eu.monnetproject.translation.*;
 import eu.monnetproject.translation.monitor.TranslationMonitor;
 import eu.monnetproject.translation.monitor.Messages;
 import eu.monnetproject.translation.monitor.Job;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 /**
  *
  * @author John McCrae
  */
 public class TranslationThread implements Runnable {
 
     private final Entity entity;
     private final LexicalEntry entry;
     private final int nBest;
     private final Language sourceLanguage;
     private final Lexicon targetLexicon;
     private final String namePrefix;
     private final List<TranslationPhraseChunker> chunkers;
     private final Iterable<TranslationSource> sources;
     private final List<TranslationFeaturizer> featurizers;
     private final Decoder decoder;
     private final Tokenizer tokenizer;
     private final TranslationConfidence confidence;
     private final boolean verbose;
     private final boolean fast;
     private final Iterable<TranslationMonitor> monitors;
     private final TrueCaser trueCaser;
     private final List<String> features;
     private final Job job;
 
     public TranslationThread(Entity entity, LexicalEntry entry, int nBest, Language sourceLanguage, Lexicon targetLexicon, String namePrefix, List<TranslationPhraseChunker> chunkers, Iterable<TranslationSource> sources, List<TranslationFeaturizer> featurizers, Decoder decoder, Tokenizer tokenizer, boolean verbose, Iterable<TranslationMonitor> monitors, TranslationConfidence confidence, boolean fast, TrueCaser trueCaser, List<String> features, Job job) {
         this.entity = entity;
         this.entry = entry;
         this.nBest = nBest;
         this.sourceLanguage = sourceLanguage;
         this.targetLexicon = targetLexicon;
         this.namePrefix = namePrefix;
         this.chunkers = chunkers;
         this.sources = sources;
         this.featurizers = featurizers;
         this.decoder = decoder;
         this.tokenizer = tokenizer;
         this.verbose = verbose;
         this.monitors = monitors;
         this.confidence = confidence;
         this.fast = fast;
         this.trueCaser = trueCaser;
         this.features = features;
         this.job = job;
     }
 
     @Override
     public void run() {
     	    Messages.associateThread(job,Thread.currentThread());
         try {
             if (entity == null) {
                 Messages.severe("Translating null entity");
                 return;
             }
             final Label srcLabel = getLabel(entry);
             if (srcLabel == null) {
                 return;
             }
             final Language targetLang = Language.get(targetLexicon.getLanguage());
 
             // Step 1: Chunking
             final ChunkListImpl chunkList = new ChunkListImpl();
             for (TranslationPhraseChunker chunker : chunkers) {
                 chunkList.addAll(chunker.chunk(srcLabel));
             }
 
             // Step 2: Sourcing translations
             final PhraseTableImpl phraseTable = new PhraseTableImpl(sourceLanguage, targetLang, "Personal Table", 0, features);
             for (TranslationSource source : sources) {
                 for (Chunk chunk : chunkList) {
                     phraseTable.addAll(source.candidates(chunk));
                 }
             }
             
             // Step 3: Re-evaluating features semantically
             PhraseTable rerankedTable = phraseTable;
             for (TranslationFeaturizer featurizer : featurizers) {
                 rerankedTable = featurizer.featurize(rerankedTable, entity);
             }
 
             // Step 4: Decode
             final List<Translation> initialTranslations = fast
                     ? decoder.decodeFast(tokenize(srcLabel), rerankedTable, features, nBest)
                     : decoder.decode(tokenize(srcLabel), rerankedTable, features, nBest);
 
             // Step 4.1: Removal of trailing punctuation
 
             List<Translation> tempList = new ArrayList<Translation>();
             for (Translation translation : initialTranslations) {
                 String trgLabel = translation.getTargetLabel().asString().replaceFirst("^[\\p{P}&&[^\\p{Pi}\\p{Ps}]]+\\s", "").replaceAll("\\s[\\p{P}&&[^\\p{Pf}\\p{Pe}]]+$", "");
                 List<String> tokens = tokenizer.tokenize(trgLabel);
                 StringBuilder finalLabel = new StringBuilder();
                 final String[] trueTokens;
                 String[] arrayTokens = new String[tokens.size()];
                 for (int i = 0; i < tokens.size(); i++) {
                    arrayTokens[i] = tokens.get(i);
                 }
                 if (trueCaser != null) {
                     trueTokens = trueCaser.trueCase(arrayTokens, 1);
                 } else {
                     trueTokens = arrayTokens;
                 }
 
                 for (String token : trueTokens) {
                     if (finalLabel.length() == 0) {
                         finalLabel.append(token);
                     } else if (token.toString().matches("[\\p{P}&&[^\\p{Pi}\\p{Ps}]].*") 
                             || finalLabel.toString().matches(".*[\\p{Pi}\\p{Ps}]")) { 
                         finalLabel.append(token);
                     } else {
                         finalLabel.append(" ").append(token);
                     }
                 }
                final String finalLabelStr = finalLabel.toString().replaceAll("\\s?@?-@\\s?","-");
                Translation trans = new TranslationImpl(translation.getEntity() == null ? entity.getURI() : translation.getEntity(), translation.getSourceLabel().asString(), translation.getSourceLabel().getLanguage(), finalLabelStr, translation.getTargetLabel().getLanguage(), translation.getScore(), translation.getFeatures());
                 tempList.add(trans);
             }
 
             final List<Translation> translations = tempList;
 
             // Step 5: Insert into lexicon
             for (Translation translation : translations) {
                 insertIntoLexicon(translation, targetLexicon, namePrefix);
             }
 
             if (!translations.isEmpty()) {
             	    Messages.translationSuccess(entity.getURI(),translations.get(0).getTargetLabel());
                // if (verbose) {
                  //   for (Translation translation : translations) {
                    //     System.out.println(translation.getTargetLabel().asString());
                    //     System.err.println("Score: " + translation.getScore());
                     //    for (Feature feature : translation.getFeatures()) {
                      //       System.err.print(feature.name + " = " + feature.score + " ");
                      //   }
                      //   System.err.println();
                    // }
                 //}
                 for (Translation translation : translations) {
                     for (TranslationMonitor monitor : monitors) {
                         monitor.recordTranslation(translation);
                     }
                 }
             } else {
                 Messages.translationFail(entity.getURI(),"Found no translations for " + entity.getURI());
             }
         } catch (Throwable x) {
             Messages.translationFail(entity.getURI(),x);
         }
     }
 
     private List<String> tokenize(Label label) {
         if (label instanceof TokenizedLabel) {
             return ((TokenizedLabel) label).getTokens();
         } else {
             return Arrays.asList(label.asString().split("\\s+"));
         }
     }
 
     private Label getLabel(LexicalEntry entry) {
         final LexicalForm canonicalForm = entry.getCanonicalForm();
         if (canonicalForm != null) {
             final Text writtenRep = canonicalForm.getWrittenRep();
             if (writtenRep != null) {
                 try {
                     return new SimpleLabel(writtenRep.value, Language.get(writtenRep.language), tokenizer);
                 } catch (LanguageCodeFormatException x) {
                 }
             }
         }
         return null;
     }
     private static final Random r = new Random();
 
     private void insertIntoLexicon(Translation translation, Lexicon targetLexicon, String namePrefix) {
         StringBuilder entryURI = new StringBuilder();
         if (namePrefix != null) {
             entryURI.append(namePrefix);
         } else {
             entryURI.append("unknown:entry#");
         }
         try {
             entryURI.append(URLEncoder.encode(translation.getTargetLabel().asString(), "UTF-8")).append("_");
             entryURI.append(r.nextInt(99999));
         } catch (UnsupportedEncodingException x) {
             throw new RuntimeException(x);
         }
         final LexicalEntry entry = LemonModels.addEntryToLexicon(targetLexicon, URI.create(entryURI.toString()), translation.getTargetLabel().asString(), entity.getURI());
         if (confidence != null) {
             final double conf = confidence.confidence(translation);
             Messages.info("Confidence=" + conf);
             entry.addAnnotation(URI.create("http://monnet01.sindice.net/ontologies/translation.owl#confidence"), "" + conf);
         } else {
             Messages.info("No confidence estimator");
         }
     }
 }
 
 class TranslationImpl implements Translation {
 
     private URI entity;
     private String srcLabel;
     private String trgLabel;
     private Language srcLang;
     private Language trgLang;
     private double score;
     private Collection<Feature> features;
 
     public TranslationImpl(URI entity, String srcLabel, Language srcLang, String trgLabel, Language trgLang, double score, Collection<Feature> features) {
         this.entity = entity;
         this.srcLabel = srcLabel;
         this.srcLang = srcLang;
         this.trgLabel = trgLabel;
         this.trgLang = trgLang;
         this.score = score;
         this.features = features;
     }
 
     @Override
     public URI getEntity() {
         // TODO Auto-generated method stub
         return entity;
     }
 
     @Override
     public Collection<Feature> getFeatures() {
         // TODO Auto-generated method stub
         return Collections.EMPTY_LIST;
     }
 
     @Override
     public double getScore() {
         // TODO Auto-generated method stub
         return 1.0;
     }
 
     @Override
     public Label getSourceLabel() {
         // TODO Auto-generated method stub
         return new Label() {
             @Override
             public Language getLanguage() {
                 // TODO Auto-generated method stub
                 return srcLang;
             }
 
             @Override
             public String asString() {
                 // TODO Auto-generated method stub
                 return srcLabel;
             }
         };
     }
 
     @Override
     public Label getTargetLabel() {
         // TODO Auto-generated method stub
         return new Label() {
             @Override
             public Language getLanguage() {
                 // TODO Auto-generated method stub
                 return trgLang;
             }
 
             @Override
             public String asString() {
                 // TODO Auto-generated method stub
                 return trgLabel;
             }
         };
     }
 }
