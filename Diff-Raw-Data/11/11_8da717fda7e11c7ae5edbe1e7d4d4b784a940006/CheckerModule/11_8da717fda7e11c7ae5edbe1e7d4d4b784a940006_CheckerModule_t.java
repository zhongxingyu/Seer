 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetab;
 
import com.google.common.io.Files;
 import com.google.inject.AbstractModule;
 import com.google.inject.Scopes;
 import com.google.inject.name.Names;
import com.google.inject.util.Providers;
 import uk.ac.ebi.fg.annotare2.services.efo.EfoService;
 
 /**
  * @author Olga Melnichuk
  */
public class CheckerModule extends AbstractModule {
 
     //TODO move to config files
     private static final String EFO_URL = "http://www.ebi.ac.uk/efo/efo.owl";
 
     @Override
     protected void configure() {
         bindConstant().annotatedWith(Names.named("efoCacheDir")).to(getEfoCachePath());
         bindConstant().annotatedWith(Names.named("efoUrl")).to(EFO_URL);
         bind(EfoService.class).in(Scopes.SINGLETON);
     }
 
     private static String getEfoCachePath() {
        String dir = System.getProperty("efo.cachedir");
        return dir == null ?
                Files.createTempDir().getPath() : dir;
     }
 }
