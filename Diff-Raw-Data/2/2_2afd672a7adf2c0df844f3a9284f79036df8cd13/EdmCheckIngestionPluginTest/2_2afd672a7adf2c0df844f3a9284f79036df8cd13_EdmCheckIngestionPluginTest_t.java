 /* LinkcheckIngestionPluginTest.java - created on Mar 20, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.uim.check.edm;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.theeuropeanlibrary.commons.export.iom.IomXmlReader;
 import org.theeuropeanlibrary.uim.check.edm.AbstractEdmIngestionPlugin.ContextRunningData;
 
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.logging.LoggingEngine;
 import eu.europeana.uim.logging.LoggingEngineAdapter;
 import eu.europeana.uim.orchestration.ActiveExecution;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.bean.CollectionBean;
 import eu.europeana.uim.store.bean.ExecutionBean;
 import eu.europeana.uim.store.bean.MetaDataRecordBean;
 
 /**
  * Tests the link checking functionality.
  * 
  * @author Andreas Juffinger (andreas.juffinger@kb.nl)
  * @since Mar 20, 2011
  */
 public class EdmCheckIngestionPluginTest {
     /**
      * Tests a simple runthrough of the link checking plugin against live (and not-supposed to be)
      * live data.
      * @throws Exception 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Test
     public void testSimpleCheck() throws Exception {
         EdmCheckIngestionPlugin plugin = new EdmCheckIngestionPlugin();
         plugin.initialize();
 
         CollectionBean collection = new CollectionBean();
         collection.setName("test");
         ExecutionBean execution = new ExecutionBean(1L);
         execution.setDataSet(collection);
 
         Properties properties = new Properties();
 
         LoggingEngine logging = LoggingEngineAdapter.LONG;
 
         ActiveExecution<MetaDataRecord<Long>, Long> context = mock(ActiveExecution.class);
         when(context.getProperties()).thenReturn(properties);
         when(context.getExecution()).thenReturn(execution);
         when(context.getLoggingEngine()).thenReturn(logging);
         ContextRunningData data = new AbstractEdmIngestionPlugin.ContextRunningData(new File(
                 "src/test/resources/xsd/EDM_tel.xsd"));
         data.maxErrors = 10;
 
         when(context.getValue((TKey<?, ContextRunningData>)any())).thenReturn(data);
         when(context.getFileResource((String)any())).thenReturn(
                 new File("src/test/resources/xsd/EDM_tel.xsd"));
 
         plugin.initialize(context);
         IomXmlReader reader = new IomXmlReader(new File(
                 "src/test/resources/sample_a0232.teliom.xml"));
         for (MetaDataRecordBean<Long> mdr : reader) {
             plugin.process(mdr, context);
         }
         plugin.completed(context);
 
         System.out.println(data.submitted);
         System.out.println(data.ignored);
         System.out.println(data.report.getInvalidRecords());
         System.out.println(data.report.getValidRecords());
 
         // TODO: assert validation errors
         Assert.assertTrue(data.submitted > 0);
        Assert.assertTrue(data.report.getInvalidRecords() > 0);
 
         plugin.shutdown();
     }
 }
