 /* ResourceEngineAdapter.java - created on May 3, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.api;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 
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
     public void setExecutionResources(Execution<I> id, LinkedHashMap<String, List<String>> resources) {
   
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
     public LinkedHashMap<String, List<String>> getExecutionResources(Execution<I> id,
             List<String> keys) {
         return new LinkedHashMap<String, List<String>>();
     }
 
     
 }
