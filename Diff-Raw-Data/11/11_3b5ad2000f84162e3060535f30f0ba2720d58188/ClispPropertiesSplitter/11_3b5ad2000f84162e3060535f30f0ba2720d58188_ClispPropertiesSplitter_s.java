 package org.kornicameister.sise.lake.clisp;
 
 import org.apache.log4j.Logger;
 import org.kornicameister.sise.exception.BadActorDescriptor;
 
 import java.util.*;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 
 public class ClispPropertiesSplitter {
     private static final Logger LOGGER           = Logger.getLogger(ClispPropertiesSplitter.class);
     private static final String ENTRY_REGEX      = ",";
     private static final String PACKAGE_REGEX    = "=";
     private static final String CLAZZ_SUFFIX     = "clazz";
     private static final String CLISP_SUFFIX     = "clisp";
     private static final String INIT_DATA_SUFFIX = "init";
 
    public static Set<ClispBootstrapTypeDescriptor> load(final String key, final Properties properties) {
         final String[] actors = ClispPropertiesSplitter.getSplit(key, properties);
        final Set<ClispBootstrapTypeDescriptor> descriptors = new HashSet<>(actors.length);
 
         for (String actor : actors) {
             final String accessKey = String.format("%s.%s", key, actor);
             final String clazz = properties.getProperty(String.format("%s.%s", accessKey, CLAZZ_SUFFIX)),
                     clisp = properties.getProperty(String.format("%s.%s", accessKey, CLISP_SUFFIX)),
                     init = properties.getProperty(String.format("%s.%s", accessKey, INIT_DATA_SUFFIX));
 
             if (clazz != null || clisp != null || init != null) {
                 assert clisp != null;
                 final List<String> clisps = new ArrayList<>(Arrays.asList(clisp.split(",")));
                 descriptors.add(new ClispBootstrapTypeDescriptor(clazz, clisps, init));
             } else {
                 throw new BadActorDescriptor("Either clazz,clisp,init is missing, check format against lake.actors.%s.actor.%s.[clisp,clazz,init]");
             }
         }
 
         return descriptors;
     }
 
     private static String[] getSplit(final String key, final Properties properties) {
         final String property = properties.getProperty(key);
         if (property.contains(ENTRY_REGEX)) {
             return property.split(ENTRY_REGEX);
         } else {
             return new String[]{property};
         }
     }
 
     private static class Loader {
         protected Map<String, String> packageToClisp = new HashMap<>();
 
         private Map<String, String> load(final String property) {
             this.extractData(property);
             return this.packageToClisp;
         }
 
         private void extractData(String property) {
             final String[] splinted = property.split(ENTRY_REGEX);
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug(String
                         .format("%s \t->\t %d types will be loaded", this.getClass().getName(), splinted.length));
             }
             for (String entry : splinted) {
                 final String[] entryArray = entry.split(PACKAGE_REGEX);
                 this.packageToClisp.put(entryArray[0], entryArray[1]);
             }
         }
     }
 }
