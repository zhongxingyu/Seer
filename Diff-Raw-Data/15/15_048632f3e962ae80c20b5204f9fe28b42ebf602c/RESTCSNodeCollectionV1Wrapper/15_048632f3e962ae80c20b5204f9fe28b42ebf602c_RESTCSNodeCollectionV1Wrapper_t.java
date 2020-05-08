 package org.jboss.pressgang.ccms.contentspec.wrapper.collection;
 
 import org.jboss.pressgang.ccms.contentspec.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 
public class RESTCSNodeCollectionV1Wrapper extends RESTUpdateableCollectionWrapper<CSNodeWrapper, RESTCSNodeV1,
        RESTCSNodeCollectionV1> implements UpdateableCollectionWrapper<CSNodeWrapper> {
 
     public RESTCSNodeCollectionV1Wrapper(RESTProviderFactory providerFactory, final RESTCSNodeCollectionV1 collection,
             boolean isRevisionCollection) {
         super(providerFactory, collection, isRevisionCollection);
     }
 }
