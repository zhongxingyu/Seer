 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.coverage;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Provides;
 import com.google.inject.multibindings.Multibinder;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.ActionListProvider;
 import com.google.jstestdriver.ResponseStreamFactory;
 import com.google.jstestdriver.hooks.FileLoadPostProcessor;
 import com.google.jstestdriver.hooks.FileLoadPreProcessor;
 
 /**
  * Configure the code coverage plugin.
  * @author corysmith@google.com (Cory Smith)
  *
  */
 public class CoverageModule extends AbstractModule {
 
   @Override
   protected void configure() {
     bind(ResponseStreamFactory.class).to(CoverageResponseStreamFactory.class);
     bind(ActionListProvider.class).to(CoverageActionDecorator.class);
     Multibinder.newSetBinder(binder(), FileLoadPostProcessor.class)
         .addBinding().to(CoverageInstrumentingProcessor.class);
     Multibinder.newSetBinder(binder(), FileLoadPreProcessor.class)
         .addBinding().to(CoverageJsAdder.class);
   }
   
   // TODO(corysmith): figure out if there is a better way for plugins to configure themselves.
   // no point in requiring bad practice to integrate. (unlike some frameworks...)
   @Provides @Inject
   public CoverageWriter createCoverageWriter(@Named("testOutput") String testOut,
                                             @Named("config") String configFileName,
                                              @Named("outputStream") PrintStream out) {
     if (testOut.length() > 0) {
       try {
         File testOutDir = new File(testOut);
         if (!testOutDir.exists()) {
           testOutDir.mkdirs();
         }
         // this should probably be configurable
        File coverageFile = new File(testOutDir, String.format("%s-coverage.dat", configFileName));
         if (coverageFile.exists()) {
           coverageFile.delete();
         }
         coverageFile.createNewFile();
         return new LcovWriter(new FileWriter(coverageFile));
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
     }
     return new SummaryCoverageWriter(out);
   }
 }
