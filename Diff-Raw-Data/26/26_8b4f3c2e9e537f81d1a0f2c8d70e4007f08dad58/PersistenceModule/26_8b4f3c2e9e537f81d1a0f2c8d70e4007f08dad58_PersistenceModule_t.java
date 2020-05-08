 package polly.core.persistence;
 
 
import polly.Polly;
 import polly.configuration.PollyConfiguration;
 import polly.core.ShutdownManagerImpl;
 import polly.core.plugins.PluginManagerImpl;
 import polly.data.Attribute;
 import polly.data.User;
 import polly.moduleloader.AbstractModule;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.moduleloader.annotations.Require;
 import polly.moduleloader.annotations.Provide;
 import polly.core.ModuleStates;
 
 @Module(
     requires = {
         @Require(component = PluginManagerImpl.class),
         @Require(component = PollyConfiguration.class),
         @Require(component = ShutdownManagerImpl.class),
         @Require(state = ModuleStates.PLUGINS_READY)
     },
     provides = {
         @Provide(component = PersistenceManagerImpl.class),
         @Provide(state = ModuleStates.PERSISTENCE_READY),
     })
 public class PersistenceModule extends AbstractModule {
 
     private PollyConfiguration config;
     private PluginManagerImpl pluginManager;
     private PersistenceManagerImpl persistenceManager;
     private ShutdownManagerImpl shutdownManager;
     private XmlCreator xmlCreator;
     
     
     
     public PersistenceModule(ModuleLoader loader) {
         super("MODULE_PERSISTENCE", loader, true);
     }
     
     
     
     @Override
     public void beforeSetup() {
         this.config = this.requireNow(PollyConfiguration.class);
         this.pluginManager = this.requireNow(PluginManagerImpl.class);
         this.shutdownManager = this.requireNow(ShutdownManagerImpl.class);
     }
     
 
     
     @Override
     public void setup() throws SetupException {
         this.persistenceManager = new PersistenceManagerImpl();
         this.provideComponent(this.persistenceManager);
         
         DatabaseProperties dp = new DatabaseProperties(
             this.config.getDbPassword(), 
             this.config.getDbUser(), 
             this.config.getDbDriver(), 
             this.config.getDbUrl());
     
         this.xmlCreator = new XmlCreator(
                 this.persistenceManager.getEntities(), 
                 dp, 
                 this.config.getPersistenceUnit(), 
                this.pluginManager,
                Polly.PLUGIN_FOLDER);
         
         this.persistenceManager.registerEntity(User.class);
         this.persistenceManager.registerEntity(Attribute.class);
                
         this.shutdownManager.addDisposable(this.persistenceManager);
     }
 
     
     
 
     public void run() throws Exception {
         logger.debug("Writing persistence.xml to " + this.config.getPersistenceXML());
         this.xmlCreator.writePersistenceXml(this.config.getPersistenceXML());
         
         this.persistenceManager.connect(this.config.getPersistenceUnit());
         this.addState(ModuleStates.PERSISTENCE_READY);
     }
 
 
 
     @Override
     public void dispose() {
         this.config = null;
         this.pluginManager = null;
         this.shutdownManager = null;
         this.persistenceManager = null;
         this.xmlCreator = null;
         super.dispose();
     }
 }
