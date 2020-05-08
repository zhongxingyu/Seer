 package org.jboss.pressgang.ccms.wrapper.collection;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 
public class RESTTranslatedCSNodeCollectionV1Wrapper extends RESTCollectionWrapper<TranslatedCSNodeWrapper, RESTTranslatedCSNodeV1, RESTTranslatedCSNodeCollectionV1> implements CollectionWrapper<TranslatedCSNodeWrapper> {
 
     public RESTTranslatedCSNodeCollectionV1Wrapper(final RESTProviderFactory providerFactory,
             final RESTTranslatedCSNodeCollectionV1 collection, boolean isRevisionCollection) {
         super(providerFactory, collection, isRevisionCollection);
     }
 }
