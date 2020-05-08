 /* XmlObjectFactoryTest.java - created on Jan 25, 2012, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.repox.rest.utils;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.theeuropeanlibrary.model.common.qualifier.Country;
 
 import eu.europeana.uim.api.StorageEngineException;
 import eu.europeana.uim.repox.rest.client.xml.Aggregator;
 import eu.europeana.uim.repox.rest.client.xml.Source;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.StandardControlledVocabulary;
 import eu.europeana.uim.store.memory.MemoryStorageEngine;
 
 /**
  * Tests xml object factory functionality.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Jan 25, 2012
  */
 public class XmlObjectFactoryTest {
     private XmlObjectFactory    factory;
     private MemoryStorageEngine engine;
 
     /**
      * Setup of factory.
      */
     @Before
     public void setupFactory() {
         factory = new BasicXmlObjectFactory();
         engine = new MemoryStorageEngine();
     }
 
     /**
      * Tests creation of aggregator from provider
      * 
      * @throws StorageEngineException
      */
     @Test
     public void testAggregator() throws StorageEngineException {
         Provider<Long> provider = engine.createProvider();
         provider.setName("test-provider");
         provider.setMnemonic("test-prov");
         provider.putValue(StandardControlledVocabulary.COUNTRY, "GBR");
         engine.updateProvider(provider);
 
         Aggregator aggr = factory.createAggregator(provider);
         String iso3 = Country.lookupCountry(aggr.getNameCode(), false).getIso3();
         String country = provider.getValue(StandardControlledVocabulary.COUNTRY);
         Assert.assertTrue(iso3.equals(country));
     }
 
     /**
      * Tests creation of aggregator from provider
      * 
      * @throws StorageEngineException
      */
     @Test
     public void testProvider() throws StorageEngineException {
         Provider<Long> provider = engine.createProvider();
         provider.setName("test-provider");
         provider.setMnemonic("test-prov");
         provider.putValue(StandardControlledVocabulary.COUNTRY, "GBR");
         engine.updateProvider(provider);
 
         eu.europeana.uim.repox.rest.client.xml.Provider prov = factory.createProvider(provider);
         Assert.assertEquals(provider.getName(), prov.getName());
         Assert.assertEquals(provider.getMnemonic(), prov.getNameCode());
        String iso3 = Country.lookupCountry(prov.getCountry(), false).getIso3();
        String country = provider.getValue(StandardControlledVocabulary.COUNTRY);
        Assert.assertTrue(iso3.equals(country));
     }
 
     /**
      * Tests creation of aggregator from provider
      * 
      * @throws StorageEngineException
      */
     @Test
     public void testBasicDataSource() throws StorageEngineException {
         Provider<Long> provider = engine.createProvider();
         provider.setName("test-provider");
         provider.setMnemonic("test-prov");
         provider.putValue(StandardControlledVocabulary.COUNTRY, "GBR");
         engine.updateProvider(provider);
 
         Collection<Long> collection = engine.createCollection(provider);
         collection.setName("test-collection");
         collection.setMnemonic("test-coll");
         engine.updateCollection(collection);
 
         Source source = factory.createDataSource(collection);
         Assert.assertEquals(collection.getName(), source.getName());
         Assert.assertEquals(collection.getMnemonic(), source.getNameCode());
     }
 }
