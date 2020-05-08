 package com.page5of4.codon.examples.webapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value = "/")
 public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 
    @Autowired
    BundleContext bundleContext;
    @Autowired
    ConfigurationAdmin configurationAdmin;
 
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView home() throws Exception {
       logger.info("ConfigurationAdmin: {}", configurationAdmin);
       logger.info("BundleContext: {}", bundleContext);
 
       Configuration[] configurations = configurationAdmin.listConfigurations(null);
       Bundle[] bundles = bundleContext.getBundles();
 
       ViewModel vm = new ViewModel();
       for(Bundle bundle : bundles) {
          vm.getBundles().add(new BundleViewModel(bundle.getBundleId(), bundle.toString()));
       }
       for(Configuration configuration : configurations) {
          vm.getConfiguration().add(new ConfigurationViewModel(configuration.getPid()));
       }
       return new ModelAndView("home", "model", vm);
    }
 
    public static class ViewModel {
       private final List<BundleViewModel> bundles = new ArrayList<BundleViewModel>();
       private final List<ConfigurationViewModel> configuration = new ArrayList<ConfigurationViewModel>();
 
       public List<BundleViewModel> getBundles() {
          return bundles;
       }
 
       public List<ConfigurationViewModel> getConfiguration() {
          return configuration;
       }
    }
 
    public static class BundleViewModel {
       private final long id;
       private final String name;
 
       public long getId() {
          return id;
       }
 
       public String getName() {
          return name;
       }
 
       public BundleViewModel(long id, String name) {
          super();
          this.id = id;
          this.name = name;
       }
    }
 
    public static class ConfigurationViewModel {
       private final String pid;
 
       public String getPid() {
          return pid;
       }
 
       public ConfigurationViewModel(String pid) {
          super();
          this.pid = pid;
       }
    }
 }
