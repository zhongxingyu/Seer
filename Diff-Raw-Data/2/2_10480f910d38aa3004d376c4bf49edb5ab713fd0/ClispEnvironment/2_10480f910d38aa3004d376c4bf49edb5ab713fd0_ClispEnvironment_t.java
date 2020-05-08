 package org.kornicameister.sise.lake.clisp;
 
 import CLIPSJNI.Environment;
 import org.apache.log4j.Logger;
 import org.kornicameister.sise.exception.LakeInitializationException;
 import org.kornicameister.sise.lake.types.ClispType;
 import org.kornicameister.sise.lake.types.WorldHelper;
 import org.kornicameister.sise.lake.types.world.DefaultWorld;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 
 public class ClispEnvironment {
     private static final Logger LOGGER = Logger.getLogger(ClispEnvironment.class);
     private static final String LAKE_TYPES = "lake.types";
     private static ClispEnvironment ourInstance;
     private final Environment environment = new Environment();
     private final String propertiesPath;
     private List<ClispType> clispTypes;
     private boolean bootstrapped;
 
     private ClispEnvironment(final String propertiesPath) {
         this.propertiesPath = propertiesPath;
         this.clispTypes = new ArrayList<>();
         this.bootstrapped = this.bootstrap();
     }
 
     private boolean bootstrap() {
         final Properties properties = new Properties();
         try {
             properties.load(new BufferedReader(new FileReader(new File(this.propertiesPath))));
 
             for (ClispBootstrapTypeDescriptor entry : ClispPropertiesSplitter.load(LAKE_TYPES, properties)) {
                 final Integer count = entry.getNumber();
                for (int i = 0; i < count; i++) {
                     LOGGER.info(String.format("[%d]>>>Bootstrapping-> %s", i, entry.getClazz()));
                     final ClispType value = this.bootstrapInternal(entry);
                     LOGGER.info(String.format("[%d]>>>Bootstrapped -> %s to %s", i, entry.getClazz(), value.getFactName()));
                     this.clispTypes.add(value);
                 }
             }
 
             return this.clispTypes.size() != 0;
 
         } catch (IOException ioe) {
             LOGGER.error("Failed to load app", ioe);
         }
         return false;
     }
 
     private ClispType bootstrapInternal(final ClispBootstrapTypeDescriptor entry) {
         try {
 
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug(String.format("Loading type -> %s", entry.getClazz()));
             }
 
             final Properties loadData = new Properties();
             loadData.load(new BufferedReader(new FileReader(new File(entry.getInitDataProperties()))));
 
 
             Class<?> clazz = Class.forName(entry.getClazz());
             ClispType clispType = (ClispType) clazz.newInstance();
             clispType.initType(loadData, this.environment, entry.getClisp());
 
             return clispType;
 
         } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException multipleE) {
             LOGGER.error(String.format("Failure in creating actor for entry -> %s", entry.getClazz()), multipleE);
             throw new LakeInitializationException(multipleE);
         }
     }
 
     public static ClispEnvironment getInstance(final String propertiesPath) {
         if (ClispEnvironment.ourInstance == null) {
             ClispEnvironment.ourInstance = new ClispEnvironment(propertiesPath);
             return ClispEnvironment.ourInstance;
         }
         return ClispEnvironment.ourInstance;
     }
 
     public void mainLoop() {
         // find world
         final DefaultWorld defaultWorld = WorldHelper.getWorld();
         if (defaultWorld == null || !bootstrapped) {
             LOGGER.error("No world found...will exit");
             throw new LakeInitializationException("There is no world defined...");
         }
         // find world
         if (defaultWorld.initializeWorld()) {
             defaultWorld.run();
         } else {
             throw new LakeInitializationException("World failed to be initialized...");
         }
     }
 
     public boolean isBootstrapped() {
         return bootstrapped;
     }
 
     public void destroy() {
         LOGGER.info("Closing up, destroying environment");
         this.environment.destroy();
     }
 }
