 package com.isu;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.isu.internals.service.Service;
 import com.isu.internals.service.ServiceManager;
 import com.isu.module.directoryspider.DirectorySpiderModule;
 import com.isu.module.luceneindexer.IndexerModule;
 import com.isu.module.luceneindexer.IndexerServiceAnnotation;
 import com.isu.module.luceneindexer.IndexingComponent;
 
 public class IndexerModuleTest {
 
 	@Test
 	public void test() 
 	{
/**
 		Injector injector = Guice.createInjector(
 				new DirectorySpiderModule(),
 				new IndexerModule("D:\\temp\\search"));
 		
 		Service indexerService = injector.getInstance(Key.get(Service.class, IndexerServiceAnnotation.class));
 		ServiceManager.addShutdownHook(indexerService);
 		indexerService.start();
 		
 		IndexingComponent indexingComponent = injector.getInstance(IndexingComponent.class);
 		
 		indexingComponent.indexAllFiles("D:\\basil\\source_code\\personal");
 		
 		ServiceManager.shutdownAllServices();
*/
 	}
 }
