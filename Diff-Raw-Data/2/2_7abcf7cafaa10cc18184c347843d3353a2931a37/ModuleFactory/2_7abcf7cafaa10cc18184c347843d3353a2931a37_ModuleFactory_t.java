 package de.ravenfly.mle.gui.osgi;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.launch.Framework;
 import org.osgi.framework.launch.FrameworkFactory;
 
 import de.ravenfly.mle.gui.osgi.model.Module;
 import de.ravenfly.mle.gui.osgi.model.Modules;
 
 public class ModuleFactory {
 
 	private final static Logger log = Logger.getLogger(ModuleFactory.class.getName());
 
 	private static ModuleFactory instance = new ModuleFactory();
 
 	private BundleContext bundleContext;
 
 	public static ModuleFactory getInstance() {
 		return instance;
 	}
 
 	private ModuleFactory(){
 
 		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
 		Map<String, String> config = new HashMap<String, String>();
 		config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
 
 		List<String> packages = new ArrayList<String>();
 
 		packages.add("javax.xml.bind");
 		packages.add("org.jdesktop.beansbinding");
 		packages.add("org.osgi.framework");
 		packages.add("de.ravenfly.mle.modulebase");
 		packages.add("de.ravenfly.mle.modulebase.filemodel");
 		packages.add("de.ravenfly.mle.modulebase.gui");
 
 		StringBuilder buffer = new StringBuilder();
 		int i = 0;
 		for (String string : packages) {
 			buffer.append(i++ > 0?",":"");
 			buffer.append(string);
 		}
 
 		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, buffer.toString());
 		Framework framework = frameworkFactory.newFramework(config);
 		try {
 			framework.start();
 		} catch (BundleException e) {
 			log.log(Level.SEVERE, "Bundle Exception on Start Farmework", e);
 		}
 
 		bundleContext = framework.getBundleContext();
 		List<Bundle> installedBundles = new LinkedList<Bundle>();
 
 		try {
 			Modules modules = readXML();
 			for (Module module : modules.getModules()) {
 				log.info("Install Module: " + module);
 				installedBundles.add(bundleContext.installBundle(module.getFilename()));
 			}
 		} catch (BundleException e) {
 			log.log(Level.SEVERE, "Bundle Exception on install Bundles", e);
 		} catch (FileNotFoundException e) {
 			log.log(Level.SEVERE, "File Not Found Exception on install Bundles", e);
 		} catch (JAXBException e) {
 			log.log(Level.SEVERE, "JAXB Exception on install Bundles", e);
 		}
 
 		for (Bundle bundle : installedBundles) {
 			if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null){
 				try {
 					bundle.start();
 				} catch (BundleException e) {
 					log.log(Level.SEVERE, "Bundle Exception on start Bundle", e);
 				}
 			}
 		}
 	}
 
 	public BundleContext getBundleContext() {
 		return bundleContext;
 	}
 
	private Modules readXML() throws JAXBException, FileNotFoundException{
 		JAXBContext context = JAXBContext.newInstance(Modules.class);
 		Unmarshaller um = context.createUnmarshaller();
 		Modules modules = (Modules) um.unmarshal(new File("config/modules.xml"));
 		return modules;
 	}
 }
