 package org.jtheque.features.impl;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.jtheque.features.CoreFeature;
 import org.jtheque.features.Feature;
 import org.jtheque.features.FeatureListener;
 import org.jtheque.features.FeatureService;
 import org.jtheque.features.Menu;
 import org.jtheque.features.Features;
 import org.jtheque.features.Feature.FeatureType;
 import org.jtheque.i18n.LanguageService;
 import org.jtheque.modules.ModuleResourceCache;
 import org.jtheque.modules.ModuleService;
 import org.jtheque.modules.Module;
 import org.jtheque.modules.ModuleListener;
 import org.jtheque.utils.StringUtils;
 import org.jtheque.utils.annotations.GuardedInternally;
 import org.jtheque.utils.annotations.ThreadSafe;
 import org.jtheque.utils.collections.CollectionUtils;
 import org.jtheque.utils.collections.WeakEventListenerList;
 
 import java.util.Collection;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * A Feature manager.
  *
  * @author Baptiste Wicht
  */
 @ThreadSafe
 public final class FeatureServiceImpl implements FeatureService, ModuleListener {
     @GuardedInternally
     private final WeakEventListenerList<FeatureListener> listeners = WeakEventListenerList.create();
 
     @GuardedInternally
     //Because it's never modified after creation
     private final Map<CoreFeature, Feature> coreFeatures;
 
     @GuardedInternally
     private final LanguageService languageService;
 
     @GuardedInternally
     private final Collection<Feature> features = CollectionUtils.newConcurrentList();
 
     /**
      * Construct a new FeatureServiceImpl.
      *
      * @param languageService The language service.
      * @param moduleService   The module service.
      */
     public FeatureServiceImpl(LanguageService languageService, ModuleService moduleService) {
         super();
 
         this.languageService = languageService;
 
         moduleService.addModuleListener("", this);
 
         coreFeatures = new EnumMap<CoreFeature, Feature>(CoreFeature.class);
 
         coreFeatures.put(CoreFeature.FILE, createAndAddFeature(0, "menu.file"));
         coreFeatures.put(CoreFeature.EDIT, createAndAddFeature(1, "menu.edit"));
         coreFeatures.put(CoreFeature.ADVANCED, createAndAddFeature(990, "menu.advanced"));
         coreFeatures.put(CoreFeature.HELP, createAndAddFeature(1000, "menu.help"));
     }
 
     /**
      * Create and add the feature.
      *
      * @param position The position of the feature.
      * @param key      The i18n key of the feature.
      *
      * @return The added feature.
      */
     private Feature createAndAddFeature(int position, String key) {
         Feature feature = Features.newFeature(FeatureType.PACK, key, position);
 
         features.add(feature);
 
         return feature;
     }
 
     @Override
     public void addMenu(String moduleId, Menu menu) {
         languageService.addInternationalizable(menu);
 
         if (StringUtils.isNotEmpty(moduleId)) {
             ModuleResourceCache.addResource(moduleId, Menu.class, menu);
         }
 
         for (CoreFeature feature : CoreFeature.values()) {
             Collection<Feature> subFeatures = menu.getSubFeatures(feature);
 
             if(!subFeatures.isEmpty()){
                 coreFeatures.get(feature).addSubFeatures(subFeatures);
 
                 fireFeatureModified(coreFeatures.get(feature));
             }
         }
 
         addFeatures(menu.getMainFeatures());
 
         menu.refreshText(languageService);
 
     }
 
     /**
      * Add the given features into the menu.
      *
      * @param newFeatures The features to add to the menu.
      */
     private void addFeatures(Iterable<Feature> newFeatures) {
         for (Feature feature : newFeatures) {
             if (feature.getType() != FeatureType.PACK) {
                 throw new IllegalArgumentException("Can only add feature of type pack directly. ");
             }
 
             features.add(feature);
 
             fireFeatureAdded(feature);
         }
     }
 
     @Override
     public Collection<Feature> getFeatures() {
         return CollectionUtils.protect(features);
     }
 
     @Override
     public void addFeatureListener(FeatureListener listener) {
         listeners.add(listener);
     }
 
     @Override
     public void removeFeatureListener(FeatureListener listener) {
        listeners.remove(listener);
     }
 
     /**
      * Avert the listeners thant a feature has been added.
      *
      * @param feature The feature who's been added.
      */
     private void fireFeatureAdded(Feature feature) {
         for (FeatureListener listener : listeners) {
             listener.featureAdded(feature);
         }
     }
 
     /**
      * Avert the listeners thant a feature has been removed.
      *
      * @param feature The feature who's been removed.
      */
     private void fireFeatureRemoved(Feature feature) {
         for (FeatureListener listener : listeners) {
             listener.featureRemoved(feature);
         }
     }
 
     /**
      * Avert the listeners thant a sub feature has been added in a specific feature.
      *
      * @param feature The feature in which the sub feature has been added.
      */
     private void fireFeatureModified(Feature feature) {
         for (FeatureListener listener : listeners) {
             listener.featureModified(feature);
         }
     }
 
     @Override
     public void moduleStopped(Module module) {
         Set<Menu> resources = ModuleResourceCache.getResources(module.getId(), Menu.class);
 
         for (Menu menu : resources) {
             removeMenu(menu);
         }
 
         ModuleResourceCache.removeResourceOfType(module.getId(), Menu.class);
     }
 
     /**
      * Remove the specified menu.
      *
      * @param menu The menu to remove.
      */
     private void removeMenu(Menu menu) {
         for (CoreFeature feature : CoreFeature.values()) {
             coreFeatures.get(feature).removeSubFeatures(menu.getSubFeatures(feature));
 
             fireFeatureModified(coreFeatures.get(feature));
         }
 
         for (Feature f : menu.getMainFeatures()) {
             features.remove(f);
 
             fireFeatureRemoved(f);
         }
     }
 
     @Override
     public void moduleStarted(Module module) {
         //Nothing to do here
     }
 
     @Override
     public void moduleInstalled(Module module) {
         //Nothing to do here
     }
 
     @Override
     public void moduleUninstalled(Module module) {
         //Nothing to do here
     }
 }
