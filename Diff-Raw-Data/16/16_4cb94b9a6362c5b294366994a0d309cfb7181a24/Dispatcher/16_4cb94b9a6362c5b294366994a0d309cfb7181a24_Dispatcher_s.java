 package de.chrfritz.jolokiamunin.controller;
 
 import de.chrfritz.jolokiamunin.config.Configuration;
 import de.chrfritz.jolokiamunin.munin.MuninProvider;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class Dispatcher {
 
     public static final String DEFAULT_SEARCH_PACKAGE = "de.chrfritz.jolokiamunin.controller";
     private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);
     private List<String> searchPackages;
     private Configuration configuration;
     private MuninProvider proivder;
 
     public Dispatcher(Configuration configuration, MuninProvider provider) {
         this(Arrays.asList(DEFAULT_SEARCH_PACKAGE), configuration, provider);
     }
 
     public Dispatcher(List<String> searchPackages, Configuration configuration, MuninProvider provider) {
         if (!searchPackages.contains(DEFAULT_SEARCH_PACKAGE)) {
             searchPackages.add(0, DEFAULT_SEARCH_PACKAGE);
         }
         this.searchPackages = searchPackages;
         this.configuration = configuration;
         this.proivder = provider;
     }
 
 
     public String handleRequest(String request) {
         String[] requestArray = request.split("\\s+", 2);
         Controller controller = resolveController(requestArray[0]);
 
        controller.setMuninProvider(proivder);
        controller.setConfiguration(configuration);

         String arguments = requestArray.length > 1 ? requestArray[1] : "";
         if (controller != null) {
             return controller.execute(arguments);
         }
         else {
             return "ERROR: Can not handle request. Invalid command";
         }
     }
 
     public Controller resolveController(String command) {
         String className = StringUtils.capitalize(command) + "Controller";
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
 
         for (String pkg : searchPackages) {
             try {
                 Class controllerClass = loader.loadClass(pkg + "." + className);
 
                 Object controller = controllerClass.newInstance();
                 if (controller instanceof Controller) {
                     return (Controller) controller;
                 }
             }
             catch (ClassNotFoundException e) {
                 LOGGER.debug("No Class {} in package {} found", className, pkg);
             }
             catch (InstantiationException | IllegalAccessException e) {
                 LOGGER.info("Can not init controller class", e);
             }
         }
         LOGGER.error("Can not get controller class for command {}", command);
         return null;
     }
 }
