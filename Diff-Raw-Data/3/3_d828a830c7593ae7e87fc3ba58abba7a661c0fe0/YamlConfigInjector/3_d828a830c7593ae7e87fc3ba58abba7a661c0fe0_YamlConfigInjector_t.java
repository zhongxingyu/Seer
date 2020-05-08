 package me.tehbeard.utils.syringe.configInjector;
 
 import java.lang.reflect.Field;
 
 import me.tehbeard.utils.syringe.Injector;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 /**
  * Injects data into fields of an object from a ConfigurationSection, using @InjectConfig
  * annotations
  * 
  * @author James
  * 
  */
 public class YamlConfigInjector extends Injector<Object, InjectConfig> {
 
     private ConfigurationSection section;
 
     public YamlConfigInjector(ConfigurationSection section) {
 
         super(InjectConfig.class);
         this.section = section;
     }
 
     @Override
     protected void doInject(InjectConfig annotation, Object object, Field field) throws IllegalArgumentException,
             IllegalAccessException {
         Object value = this.section.get(annotation.value());
        if(value != null){
         field.set(object, value);
        }
 
     }
 
 }
