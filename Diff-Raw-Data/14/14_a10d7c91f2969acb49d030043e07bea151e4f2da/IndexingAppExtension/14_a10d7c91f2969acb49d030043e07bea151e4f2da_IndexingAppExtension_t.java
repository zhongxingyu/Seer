 package rcpmail.server;
 
 import org.eclipse.emf.cdo.spi.server.IAppExtension;
 
 import org.eclipse.net4j.util.container.IPluginContainer;
 
 import org.apache.lucene.index.IndexReader;
 
 import proj.zoie.api.DataConsumer.DataEvent;
 import proj.zoie.api.ZoieException;
 import proj.zoie.api.indexing.ZoieIndexableInterpreter;
 import proj.zoie.impl.indexing.ZoieConfig;
 import proj.zoie.impl.indexing.ZoieSystem;
 import rcpmail.model.Message;
 
 import java.io.File;
 import java.util.List;
 
 public class IndexingAppExtension implements IAppExtension
 {
   static IndexingAppExtension instance;
 
   static IndexingAppExtension getInstance()
   {
     return instance;
   }
 
   private ZoieSystem<IndexReader, Message> zoieSystem;
 
   @Override
   public void start(File configFile) throws Exception
   {
     // Add post-processor, run it on existing repository instance(s) as well
     IPluginContainer.INSTANCE.addPostProcessor(new IndexingPostProcessor(), true);
 
     // Initialize indexing subsystem
     File indexDirectory = new File("indexes");
     ZoieIndexableInterpreter<Message> interpreter = new MessageIndexableInterpreter();
     zoieSystem = ZoieSystem.buildDefaultInstance(indexDirectory, interpreter, new ZoieConfig());
     zoieSystem.start();
 
     OM.LOG.info("IndexingAppExtension started");
     instance = this;
   }
 
   @Override
   public void stop() throws Exception
   {
     if (null != zoieSystem)
     {
      zoieSystem.shutdown();
       zoieSystem = null;
     }
 
     instance = null;
     OM.LOG.info("IndexingAppExtension stopped");
   }
 
   public void consume(List<DataEvent<Message>> dataEvents) throws ZoieException
   {
     zoieSystem.consume(dataEvents);
   }
 }
