 package fi.vincit.jmobster.util;
 
 /*
  * Copyright 2012-2013 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import fi.vincit.jmobster.JMobsterFactory;
 import fi.vincit.jmobster.ModelGenerator;
 import fi.vincit.jmobster.processor.ModelFactory;
 import fi.vincit.jmobster.processor.ModelProcessor;
 import fi.vincit.jmobster.processor.languages.LanguageContext;
 import fi.vincit.jmobster.processor.model.Model;
 import fi.vincit.jmobster.util.writer.DataWriter;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * The purpose of model cache is to provide an easy way to generate and store generated
  * models dynamically by model name. One usage example could be a REST interface from which
  * validation rules could be queried. The interface could take the model name and give it
  * to a model cache. The cache then generates the model if needed or fetches it from the cache.
  * @param <C> {@link LanguageContext} to use
  * @param <W> {@link DataWriter} the LanguageContext uses
  */
 public abstract class BaseModelCache<C extends LanguageContext<W>, W extends DataWriter> {
    private Map<String, String> generatedModelsByName = new ConcurrentHashMap<String, String>();
 
    private Map<String, Model> modelsByName = new ConcurrentHashMap<String, Model>();
 
     private ModelGenerator<W> modelGenerator;
     private ModelFactory modelFactory;
     private C languageContext;
 
     public BaseModelCache(ModelProcessor<C, W> modelProcessor, ModelFactory modelFactory) {
         this.modelGenerator = JMobsterFactory.getModelGenerator(modelProcessor);
         this.modelFactory = modelFactory;
         this.languageContext = modelProcessor.getLanguageContext();
     }
 
     protected C getLanguageContext() {
         return languageContext;
     }
 
     public String getModel(String name) {
         String internalModelName = nameToInternalName(name);
         if( !generatedModelsByName.containsKey(internalModelName) ) {
             if( !modelsByName.containsKey(internalModelName) ) {
                 throw new IllegalArgumentException("No such model: " + name);
             }
 
             generateModelAndAddToCache(internalModelName);
         }
         return generatedModelsByName.get(internalModelName);
     }
 
    private synchronized void generateModelAndAddToCache(String internalModelName) {
         Model model = modelsByName.get(internalModelName);
         modelGenerator.process(model);
         String modelData = getLanguageContext().getWriter().toString();
         generatedModelsByName.put(internalModelName, modelData);
         getLanguageContext().getWriter().clear();
     }
 
     public void addModels(Collection<Class> classes) {
         Collection<Model> models = modelFactory.createAll(classes);
         for( Model model : models ) {
             String modelName = nameToInternalName(model.getName());
             modelsByName.put(modelName, model);
         }
     }
 
     private String nameToInternalName(String name) {
         return name;
     }
 
     public void clearModelCache() {
         generatedModelsByName.clear();
     }
 
     public void clearModelCacheAndModels() {
         clearModelCache();
         modelsByName.clear();
     }
 
     public Set<String> getModelNames() {
         return modelsByName.keySet();
     }
 }
