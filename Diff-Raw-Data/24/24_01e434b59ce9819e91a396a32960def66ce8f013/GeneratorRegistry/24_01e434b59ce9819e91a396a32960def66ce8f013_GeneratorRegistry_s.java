 package org.objectg.gen;
 
 import org.objectg.conf.GenerationConfiguration;
 import org.objectg.gen.impl.Generators;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 
 /**
  * <p>
  *      Knows about all generators in the framework. Allows to generate value for any type by using
  *      {@link #generate(org.objectg.conf.GenerationConfiguration, GenerationContext)}
  * </p>
  * <p>
  * User: __nocach
  * Date: 28.8.12
  * </p>
  */
 public class GeneratorRegistry {
     private static final Logger logger = LoggerFactory.getLogger(GeneratorRegistry.class);
 
     private final CompositeGenerator generatorChain = new CompositeGenerator(
             Generators.primitive(),
             Generators.list(),
             Generators.map(),
             Generators.set(),
             Generators.enumeration(),
             Generators.array(),
             Generators.notNativeClass());
     private static final GeneratorRegistry instance = new GeneratorRegistry();
 
     /**
      *
      *
      * @param context not null context for which generator must be found
      * @throws IllegalArgumentException if generator for the passed context and configuration was not found
      */
     public <T> T generate(GenerationConfiguration configuration,  GenerationContext context) {
         Assert.notNull(context, "context");
 
         T result = (T) generatorChain.generate(configuration, context);
        logger.info("generated value=" + result);
 		logger.debug("for context="+context);
         return result;
     }
 
    public static final GeneratorRegistry getInstance(){
         return instance;
     }
 }
