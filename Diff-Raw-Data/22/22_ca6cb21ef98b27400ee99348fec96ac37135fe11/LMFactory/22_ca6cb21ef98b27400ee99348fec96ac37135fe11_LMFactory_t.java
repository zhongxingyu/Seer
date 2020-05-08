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
 package eu.monnetproject.translation.langmodel;
 
 import eu.monnetproject.config.Configurator;
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.translation.LanguageModel;
 import eu.monnetproject.translation.LanguageModelFactory;
 import eu.monnetproject.translation.TrueCaser;
 import eu.monnetproject.translation.monitor.Messages;
 import java.io.File;
 import java.util.Properties;
 import java.util.WeakHashMap;
 
 /**
  *
  * @author John McCrae
  */
 public class LMFactory implements LanguageModelFactory {
 
     private final WeakHashMap<Language, LanguageModelAndTrueCaser> pagedLMs = new WeakHashMap<Language, LanguageModelAndTrueCaser>();
 
     @Override
     public LanguageModel getModel(Language language) {
         return get(language);
     }
 
     private LanguageModelAndTrueCaser get(Language language) {
        final String pid = System.getProperty("langmodel.config", "eu.monnetproject.translation.langmodel");
         final Properties config = Configurator.getConfig(pid);
         if (config.containsKey(language.toString())) {
             LanguageModelAndTrueCaser lm = pagedLMs.get(language);
             if (lm != null) {
                 return lm;
             } else {
                 final File modelFile = new File(config.getProperty(language.toString()));
                 if (!modelFile.exists()) {
                     Messages.componentLoadFail(PagedLM.class, modelFile.getPath() + " not found");
                     return null;
                 }
                 try {
                    final String method = System.getProperty("eu.monnetproject.translation.langmodel.method") != null
                            ? System.getProperty("eu.monnetproject.translation.langmodel.method")
                            : (config.containsKey("method")
                             ? config.getProperty("method")
                            : "");
                     if (method.equals("mem")) {
                         lm = new MemoryLM(language, modelFile);
                     } else if (method.equals("mix") || method.equals("mixp")) {
                         if (!config.containsKey("lambda")) {
                             Messages.componentLoadFail(MixtureLM.class, "Mixture model must specify lambda parameter");
                             return null;
                         }
                         final double lambda;
                         try {
                             lambda = Double.parseDouble(config.getProperty("lambda"));
                         } catch (NumberFormatException x) {
                             Messages.componentLoadFail(MixtureLM.class, x);
                             return null;
                         }

                         if (!config.containsKey(language.toString() + "2")) {
                            if (method.equals("mixp")) {
                                 return new PagedLM(language, modelFile);
                             } else {
                                 return new MemoryLM(language, modelFile);
                             }
                        }
                         final File modelFile2 = new File(config.getProperty(language + "2"));
                         if (!modelFile2.exists()) {
                             Messages.componentLoadFail(PagedLM.class, modelFile2.getPath() + " not found");
                             return null;
                         }

                         final AbstractLM lm1, lm2;
                         if (method.equals("mixp")) {
                             lm1 = new PagedLM(language, modelFile);
                             lm2 = new PagedLM(language, modelFile2);
                         } else {
                             lm1 = new MemoryLM(language, modelFile);
                             lm2 = new MemoryLM(language, modelFile2);
                         }
                         return new MixtureLM(lm1, lm2, lambda);
                     } else {
                         lm = new PagedLM(language, modelFile);
                     }
                 } catch (Exception x) {
                     Messages.componentLoadFail(AbstractLM.class, x);
                     return null;
                 }
                 pagedLMs.put(language, lm);
                 return lm;
             }
         } else {
             return null;
         }
     }
 
     @Override
     public TrueCaser getTrueCaser(Language language) {
         return get(language);
     }
 }
