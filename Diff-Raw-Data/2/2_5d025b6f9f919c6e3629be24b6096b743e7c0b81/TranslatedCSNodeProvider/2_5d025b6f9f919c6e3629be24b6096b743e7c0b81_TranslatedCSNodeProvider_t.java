 package org.jboss.pressgang.ccms.provider;
 
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeStringWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public interface TranslatedCSNodeProvider {
     TranslatedCSNodeWrapper getCSTranslatedNode(int id);
 
     TranslatedCSNodeWrapper getCSTranslatedNode(int id, Integer revision);
 
     UpdateableCollectionWrapper<TranslatedCSNodeStringWrapper> getCSTranslatedNodeStrings(int id, Integer revision);
 
     CollectionWrapper<TranslatedCSNodeWrapper> getCSTranslatedNodeRevisions(int id, Integer revision);
 
     CollectionWrapper<TranslatedCSNodeWrapper> createCSTranslatedNodes(
             CollectionWrapper<TranslatedCSNodeWrapper> translatedNodes) throws Exception;
 
     TranslatedCSNodeWrapper newCSTranslatedNode();
 
    UpdateableCollectionWrapper<TranslatedCSNodeWrapper> newCSTranslatedNodeCollection();
 }
