 /* ResourceEngineAdapter.java - created on May 3, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.api;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 
 import eu.europeana.uim.api.StorageEngine.EngineStatus;
 import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.Provider;
 
 /**
  * Dummy implementation of the ResourceEngine
  * 
  * @author Rene Wiermer (rene.wiermer@kb.nl)
  * @param <I>
  * @date May 3, 2011
  */
 public class ResourceEngineAdapter<I> implements ResourceEngine<I> {
 
     @Override
     public String getIdentifier() {
        return ResourceEngineAdapter.class.getSimpleName();
     }
 
     @Override
     public void setGlobalResources(LinkedHashMap<String, List<String>> resources) {
    
     }
 
 
 
     @Override
     public void setProviderResources(Provider<I> id, LinkedHashMap<String, List<String>> resources) {
         
     }
 
 
     @Override
     public void setCollectionResources(Collection<I> id,
             LinkedHashMap<String, List<String>> resources) {
 
     }
 
 
 
 
     @Override
     public LinkedHashMap<String, List<String>> getGlobalResources(List<String> keys) {
         return new LinkedHashMap<String, List<String>>();
     }
 
     @Override
     public LinkedHashMap<String, List<String>> getProviderResources(Provider<I> id,
             List<String> keys) {
         return new LinkedHashMap<String, List<String>>();
     }
 
     @Override
     public LinkedHashMap<String, List<String>> getCollectionResources(Collection<I> id,
             List<String> keys) {
         return new LinkedHashMap<String, List<String>>();
     }
 
  
 
     @Override
     public void setConfiguration(Map<String, String> config) {
         
     }
 
     @Override
     public Map<String, String> getConfiguration() {
         return new HashMap<String, String>();
     }
 
     @Override
     public void initialize() {
       
     }
 
     @Override
     public void shutdown() {
         
     }
 
     @Override
     public void checkpoint() {
 
     }
 
     @Override
     public EngineStatus getStatus() {
         return EngineStatus.RUNNING;
     }
 
     @Override
     public File getRootDirectory() {
         return null;
     }
 
     @Override
     public File getWorkingRootDirectory() {
         return null;
     }
 
     @Override
     public File getTmpRootDirectory() {
         return null;
     }
 
     
 }
