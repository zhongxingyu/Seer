 package com.reversemind.glia.other.spring;
 
 import com.reversemind.glia.GliaPayload;
 import com.reversemind.glia.server.GliaServerFactory;
 import com.reversemind.glia.server.IGliaPayloadProcessor;
 import com.reversemind.glia.server.IGliaServer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.io.Serializable;
 import java.util.Map;
 
 /**
  *
  */
 public class GliaServerSpringContextLoader implements Serializable {
 
     private static final Logger LOG = LoggerFactory.getLogger(GliaServerSpringContextLoader.class);
 
     public static void main(String... args) throws InterruptedException {
 
         ApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/glia-server-context.xml");
 
         GliaServerFactory.Builder builderAdvertiser = applicationContext.getBean("serverBuilderAdvertiser", GliaServerFactory.Builder.class);
 
         LOG.debug("--------------------------------------------------------");
         LOG.debug("Builder properties:");
         LOG.debug("Name:" + builderAdvertiser.getName());
         LOG.debug("Instance Name:" + builderAdvertiser.getInstanceName());
         LOG.debug("port:" + builderAdvertiser.getPort());
         LOG.debug("isAutoSelectPort:" + builderAdvertiser.isAutoSelectPort());
 
         LOG.debug("Type:" + builderAdvertiser.getType());
 
         LOG.debug("Zookeeper connection string:" + builderAdvertiser.getZookeeperHosts());
         LOG.debug("Zookeeper base path:" + builderAdvertiser.getServiceBasePath());
 
 
         IGliaServer server = builderAdvertiser.build();
 
         LOG.debug("\n\n");
         LOG.debug("--------------------------------------------------------");
         LOG.debug("After server initialization - properties");
         LOG.debug("\n");
         LOG.debug("Server properties:");
         LOG.debug("......");
         LOG.debug("Name:" + server.getName());
         LOG.debug("Instance Name:" + server.getInstanceName());
         LOG.debug("port:" + server.getPort());
 
         server.start();
 
         Thread.sleep(60000);
 
         server.shutdown();
 
 
         GliaServerFactory.Builder builderSimple = (GliaServerFactory.Builder) applicationContext.getBean("serverBuilderSimple");
        LOG.debug(builderSimple.port());
 
         IGliaServer serverSimple = builderSimple
                 .setAutoSelectPort(true)
                 .setName("N A M E")
                 .setPort(8000)
                 .setPayloadWorker(new IGliaPayloadProcessor() {
                     @Override
                     public Map<Class, Class> getPojoMap() {
                         return null;
                     }
 
                     @Override
                     public void setPojoMap(Map<Class, Class> map) {
                     }
 
                     @Override
                     public void setEjbMap(Map<Class, String> map) {
                     }
 
                     @Override
                     public void registerPOJO(Class interfaceClass, Class pojoClass) {
                     }
 
                     @Override
                     public GliaPayload process(Object gliaPayloadObject) {
                         return null;
                     }
                 }).build();
 
 
         LOG.debug("\n\n");
         LOG.debug("--------------------------------------------------------");
         LOG.debug("Simple Glia server");
         LOG.debug("\n");
         LOG.debug("Server properties:");
         LOG.debug("......");
         LOG.debug("Name:" + serverSimple.getName());
         LOG.debug("Instance Name:" + serverSimple.getInstanceName());
         LOG.debug("port:" + serverSimple.getPort());
 
     }
 
 }
