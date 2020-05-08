 package com.zenika.dorm.servlet;
 
 import com.sun.jersey.guice.JerseyServletModule;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 import com.zenika.dorm.core.dao.DormDao;
 import com.zenika.dorm.core.dao.neo4j.DormDaoNeo4j;
 import com.zenika.dorm.core.model.impl.DefaultDormMetadataExtension;
 import com.zenika.dorm.core.processor.Processor;
 import com.zenika.dorm.core.processor.impl.DefaultProcessor;
 import com.zenika.dorm.core.processor.impl.DormProcessor;
 import com.zenika.dorm.core.repository.DormRepositoryManager;
 import com.zenika.dorm.core.repository.impl.IvyRepositoryManager;
 import com.zenika.dorm.core.service.DormService;
 import com.zenika.dorm.core.service.impl.DefaultDormService;
 import com.zenika.dorm.core.ws.resource.DormResource;
 import com.zenika.dorm.maven.model.impl.MavenMetadataExtension;
 import com.zenika.dorm.maven.processor.extension.MavenProcessor;
 import com.zenika.dorm.maven.ws.resource.MavenResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class GuiceModule extends JerseyServletModule {
 
     private static final Logger LOG = LoggerFactory.getLogger(GuiceModule.class);
 
     @Override
     protected void configureServlets() {
 
         LOG.info("Zenika DORM POC");
         LOG.info("Init guice module");
 
         bindResources();
         bindProcessor();
         bindService();
         bindDao();
         bindRepository();
 
         serve("/*").with(GuiceContainer.class);
     }
 
     private void bindResources() {
         bind(DormResource.class);
         bind(MavenResource.class);
     }
 
     private void bindProcessor() {
         DefaultProcessor processor = new DefaultProcessor();
         processor.getExtensions().put(DefaultDormMetadataExtension.NAME, new DormProcessor());
         processor.getExtensions().put(MavenMetadataExtension.NAME, new MavenProcessor());
         bind(Processor.class).toInstance(processor);
     }
 
     private void bindService() {
         bind(DormService.class).to(DefaultDormService.class);
     }
 
     private void bindDao() {
         bind(DormDao.class).to(DormDaoNeo4j.class);
     }
 
     private void bindRepository() {
        bind(DormRepositoryManager.class).to(IvyRepositoryManager.class);
     }
 }
