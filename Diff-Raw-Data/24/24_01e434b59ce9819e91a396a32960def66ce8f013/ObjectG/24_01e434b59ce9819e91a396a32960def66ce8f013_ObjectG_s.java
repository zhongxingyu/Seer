 package org.objectg;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.objectg.conf.ConfigurationBuilder;
 import org.objectg.conf.ConfigurationManager;
 import org.objectg.conf.GenerationConfiguration;
 import org.objectg.conf.OngoingConfiguration;
 import org.objectg.conf.exception.ConfigurationException;
 import org.objectg.conf.prototype.InterceptedByPrototypeCreator;
 import org.objectg.conf.prototype.PrototypeCreator;
 import org.objectg.gen.GenerationContext;
 import org.objectg.gen.GenerationRule;
 import org.objectg.gen.GeneratorRegistry;
 import org.objectg.gen.rule.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 
 /**
  * <p>
  *     User-facing api of the framework.
  * </p>
  * <p>
  * User: __nocach
  * Date: 26.8.12
  * </p>
  */
 public class ObjectG {
    private static final Logger logger = LoggerFactory.getLogger(ObjectG.class);
     private static final PrototypeCreator PROTOTYPE_CREATOR = new PrototypeCreator();
 	private static final ConfigurationManager CONFIGURATION_MANAGER = new ConfigurationManager(1, ObjectG.class);
 
     public static <T> T unique(T prototype){
         return unique(PROTOTYPE_CREATOR.getRealObjectClass(prototype), new GenerationConfiguration(), prototype);
     }
 
     public static <T> T unique(Class<T> clazz){
         return unique(clazz, new GenerationConfiguration());
     }
 
     public static <T> T unique(Class<T> clazz, GenerationConfiguration configuration){
         configuration.setUnique(true);
         return generate(clazz, configuration);
     }
     public static <T> T unique(T prototype, GenerationConfiguration configuration){
         configuration.setUnique(true);
         return generate(PROTOTYPE_CREATOR.getRealObjectClass(prototype), configuration);
     }
 
     public static <T> T unique(Class<T> clazz, ConfigurationBuilder configurationBuilder){
         return unique(clazz, configurationBuilder.done());
     }
     public static <T> T unique(T prototype, ConfigurationBuilder configurationBuilder){
         return unique(PROTOTYPE_CREATOR.getRealObjectClass(prototype), configurationBuilder.done());
     }
 
     public static <T> T unique(Class<T> clazz, ConfigurationBuilder configurationBuilder, Object... prototypes){
         return unique(clazz, configurationBuilder.done(), prototypes);
     }
     public static <T> T unique(T prototype, ConfigurationBuilder configurationBuilder, Object... prototypes){
         return unique(PROTOTYPE_CREATOR.getRealObjectClass(prototype), configurationBuilder.done(), prototypes);
     }
 
     public static <T> T unique(Class<T> clazz, Object... prototypes){
         assertPrototypesAreValid(prototypes);
         return unique(clazz, new GenerationConfiguration(), prototypes);
     }
 
     private static void assertPrototypesAreValid(Object[] prototypes) {
         for (Object each : prototypes){
             if (each instanceof ConfigurationBuilder){
                 throw new ConfigurationException("tried to use ConfigurationBuilder as prototype object. " +
                         "Did you forget to call .done()? Check ObjectG.config() for more info");
             }
             if ( !(each instanceof InterceptedByPrototypeCreator) ){
                 throw new ConfigurationException("tried to pass not prototype instance. " +
                         "Make sure you passed instances return by ObjectG.prototype()");
             }
         }
     }
 
     /**
      * will create GenerationConfiguration from {@code prototypes} for this generation
      */
     public static <T> T unique(Class<T> clazz, GenerationConfiguration configuration, Object... prototypes){
         configuration.setUnique(true);
         configuration.addAllRules(rulesFromPrototypes(prototypes));
         return generate(clazz, configuration);
     }
     /**
      * will create GenerationConfiguration from {@code prototypes} for this generation
      */
     public static <T> T unique(T prototype, GenerationConfiguration configuration, Object... prototypes){
         configuration.setUnique(true);
         configuration.addAllRules(rulesFromPrototypes(prototypes));
         return generate(PROTOTYPE_CREATOR.getRealObjectClass(prototype), configuration);
     }
 
     public static <T> T generate(Class<T> clazz, GenerationConfiguration configuration) {
         addDefaultRules(configuration);
 		GenerationConfiguration finalConfiguration = CONFIGURATION_MANAGER
 				.getFinalConfiguration(PROTOTYPE_CREATOR, configuration);
         T result = (T) GeneratorRegistry.getInstance().generate(finalConfiguration, GenerationContext.createRoot(clazz));
         configuration.postProcess(result);
         return result;
     }
 
 	private static void addDefaultRules(GenerationConfiguration configuration) {
         configuration.addAllRules(Rules.defaultRules());
     }
 
     public static <T> T prototype(Class<T> clazz){
         T result = PROTOTYPE_CREATOR.newPrototype(clazz);
         OngoingConfiguration.plannedPrototype = result;
         return result;
     }
 
     private static List<GenerationRule> rulesFromPrototypes(Object... prototypes){
         return PROTOTYPE_CREATOR.getRulesFromPrototypes(prototypes);
     }
 
     /**
      *
      * @param clazz type of the objects for list
      * @param <T>
      * @return
      */
     public static <T> List<T> uniqueList(Class<T> clazz) {
         return uniqueList(clazz, 1);
     }
     /**
      *
      * @param prototype not null
      * @param <T>
      * @return
      */
     public static <T> List<T> uniqueList(T prototype) {
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), 1);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, int size, Object... prototypes){
         return uniqueList(clazz, new GenerationConfiguration(), size, prototypes);
     }
     public static <T> List<T> uniqueList(T prototype, int size, Object... prototypes){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), new GenerationConfiguration(), size, prototypes);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, Object... prototypes){
         GenerationConfiguration generationConfiguration = new GenerationConfiguration();
         return uniqueList(clazz, generationConfiguration, 1, prototypes);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, GenerationConfiguration generationConfiguration){
         return uniqueList(clazz, generationConfiguration, 1);
     }
     public static <T> List<T> uniqueList(T prototype, GenerationConfiguration generationConfiguration){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, 1);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, GenerationConfiguration generationConfiguration, int size){
         return uniqueList(clazz, generationConfiguration, size, new Object[]{});
     }
     public static <T> List<T> uniqueList(T prototype, GenerationConfiguration generationConfiguration, int size){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, size, new Object[]{});
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, ConfigurationBuilder generationBuilder){
         return uniqueList(clazz, generationBuilder.done(), 1);
     }
     public static <T> List<T> uniqueList(T prototype, ConfigurationBuilder generationBuilder){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), 1);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, ConfigurationBuilder generationBuilder, int size){
         return uniqueList(clazz, generationBuilder.done(), size);
     }
     public static <T> List<T> uniqueList(T prototype, ConfigurationBuilder generationBuilder, int size){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), size);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, GenerationConfiguration generationConfiguration, Object... prototypes){
         return uniqueList(clazz, generationConfiguration, 1, prototypes);
     }
     public static <T> List<T> uniqueList(T prototype, GenerationConfiguration generationConfiguration, Object... prototypes){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, 1, prototypes);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, ConfigurationBuilder generationBuilder, Object... prototypes){
         return uniqueList(clazz, generationBuilder.done(), 1, prototypes);
     }
     public static <T> List<T> uniqueList(T prototype, ConfigurationBuilder generationBuilder, Object... prototypes){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), 1, prototypes);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, ConfigurationBuilder generationBuilder, int size, Object... prototypes){
         return uniqueList(clazz, generationBuilder.done(), size, prototypes);
     }
 
     public static <T> List<T> uniqueList(T prototype, ConfigurationBuilder generationBuilder, int size, Object... prototypes){
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), size, prototypes);
     }
 
     /**
      *
      * @param clazz type of the objects for list
      * @param size how many objects to generate
      * @param <T>
      * @return
      */
     public static <T> List<T> uniqueList(Class<T> clazz, int size) {
         GenerationConfiguration generationConfiguration = new GenerationConfiguration();
         return uniqueList(clazz, generationConfiguration, size);
     }
     /**
      *
      * @param prototype not null
      * @param size how many objects to generate
      * @param <T>
      * @return
      */
     public static <T> List<T> uniqueList(T prototype, int size) {
         GenerationConfiguration generationConfiguration = new GenerationConfiguration();
         return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, size);
     }
 
     public static <T> List<T> uniqueList(Class<T> clazz, GenerationConfiguration configuration, int size, Object... prototypes){
         Assert.isTrue(size >= 0, "size must be >= 0");
         List<T> result = new ArrayList<T>(size);
         configuration.setUnique(true);
         configuration.addAllRules(rulesFromPrototypes(prototypes));
         for (int i = 0; i < size; i++){
             result.add(i, generate(clazz, configuration));
         }
         return result;
     }
 
     public static <T> List<T> uniqueList(T prototype, GenerationConfiguration configuration, int size, Object... prototypes){
 		return uniqueList(PROTOTYPE_CREATOR.getRealObjectClass(prototype), configuration, size, prototypes);
     }
 
     /**
      *
      * @param clazz type of the objects for set
      * @param <T>
      * @return
      */
     public static <T> Set<T> uniqueSet(Class<T> clazz) {
         return uniqueSet(clazz, 1);
     }
     /**
      *
      * @param prototype not null
      * @param <T>
      * @return
      */
     public static <T> Set<T> uniqueSet(T prototype) {
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), 1);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, Object... prototypes){
         GenerationConfiguration generationConfiguration = new GenerationConfiguration();
         return uniqueSet(clazz, generationConfiguration, 1, prototypes);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, GenerationConfiguration generationConfiguration){
         return uniqueSet(clazz, generationConfiguration, 1);
     }
     public static <T> Set<T> uniqueSet(T prototype, GenerationConfiguration generationConfiguration){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, 1);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, GenerationConfiguration generationConfiguration, int size){
         return uniqueSet(clazz, generationConfiguration, size, new Object[]{});
     }
     public static <T> Set<T> uniqueSet(T prototype, GenerationConfiguration generationConfiguration, int size){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, size, new Object[]{});
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, ConfigurationBuilder generationBuilder){
         return uniqueSet(clazz, generationBuilder.done(), 1);
     }
     public static <T> Set<T> uniqueSet(T prototype, ConfigurationBuilder generationBuilder){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), 1);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, ConfigurationBuilder generationBuilder, int size){
         return uniqueSet(clazz, generationBuilder.done(), size);
     }
     public static <T> Set<T> uniqueSet(T prototype, ConfigurationBuilder generationBuilder, int size){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), size);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, GenerationConfiguration generationConfiguration, Object... prototypes){
         return uniqueSet(clazz, generationConfiguration, 1, prototypes);
     }
     public static <T> Set<T> uniqueSet(T prototype, GenerationConfiguration generationConfiguration, Object... prototypes){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, 1, prototypes);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, ConfigurationBuilder generationBuilder, Object... prototypes){
         return uniqueSet(clazz, generationBuilder.done(), 1, prototypes);
     }
     public static <T> Set<T> uniqueSet(T prototype, ConfigurationBuilder generationBuilder, Object... prototypes){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), 1, prototypes);
     }
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, ConfigurationBuilder generationBuilder, int size, Object... prototypes){
         return uniqueSet(clazz, generationBuilder.done(), size, prototypes);
     }
     public static <T> Set<T> uniqueSet(T prototype, ConfigurationBuilder generationBuilder, int size, Object... prototypes){
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationBuilder.done(), size, prototypes);
     }
 
     /**
      *
      * @param clazz type of the objects for list
      * @param size how many objects to generate
      * @param <T>
      * @return
      */
     public static <T> Set<T> uniqueSet(Class<T> clazz, int size) {
         GenerationConfiguration generationConfiguration = new GenerationConfiguration();
         return uniqueSet(clazz, generationConfiguration, size);
     }
 
     /**
      *
      * @param prototype not null
      * @param size how many objects to generate
      * @param <T>
      * @return
      */
     public static <T> Set<T> uniqueSet(T prototype, int size) {
         GenerationConfiguration generationConfiguration = new GenerationConfiguration();
         return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), generationConfiguration, size);
     }
 
 
     public static <T> Set<T> uniqueSet(Class<T> clazz, GenerationConfiguration configuration, int size, Object... prototypes){
         Assert.isTrue(size >= 0, "size must be >= 0");
         Set<T> result = new HashSet<T>();
         configuration.setUnique(true);
         configuration.addAllRules(rulesFromPrototypes(prototypes));
         for (int i = 0; i < size; i++){
             result.add(generate(clazz, configuration));
         }
         return result;
     }
 
 	public static <T> Set<T> uniqueSet(T prototype, GenerationConfiguration configuration, int size, Object... prototypes){
 		return uniqueSet(PROTOTYPE_CREATOR.getRealObjectClass(prototype), configuration, size, prototypes);
 	}
 
     public static ConfigurationBuilder config() {
         return new ConfigurationBuilder(PROTOTYPE_CREATOR);
     }
 
 	/**
 	 *
 	 * @param objectWithConfiguration e.g. TestCase having method with
 	 *                                   annotation {@link org.objectg.conf.local.ConfigurationProvider}
 	 */
 	public static void setupConfig(final Object objectWithConfiguration) {
 		CONFIGURATION_MANAGER.register(objectWithConfiguration);
 	}
 
 	/**
 	 * @param configurationBuilder not null configuration to use for all subsequent generations
 	 *                             in class that called this method
 	 */
 	public static void setupConfig(final ConfigurationBuilder configurationBuilder) {
 		setupConfig(configurationBuilder.done());
 	}
 
 	/**
 	 *
 	 * @param configuration not null configuration to use for all subsequent generations
 	 *                             in class that called this method
 	 */
 	public static void setupConfig(GenerationConfiguration configuration){
 		CONFIGURATION_MANAGER.setLocalConfiguration(configuration);
 	}
 }
