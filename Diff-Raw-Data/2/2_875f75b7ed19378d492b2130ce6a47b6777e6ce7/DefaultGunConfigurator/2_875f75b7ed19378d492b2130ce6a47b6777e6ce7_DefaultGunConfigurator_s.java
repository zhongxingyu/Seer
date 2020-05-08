 package com.illmeyer.polygraph.core.init;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.reflections.Reflections;
 import org.reflections.scanners.SubTypesScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 
 import com.illmeyer.polygraph.core.Gun;
 import com.illmeyer.polygraph.core.MessageGunTemplateLoader;
 import com.illmeyer.polygraph.core.interfaces.Module;
 import com.illmeyer.polygraph.core.spi.Extension;
 import com.illmeyer.polygraph.core.spi.GunConfigurator;
 import com.illmeyer.polygraph.core.spi.MessageType;
 import com.illmeyer.polygraph.core.spi.Template;
 
 import freemarker.cache.TemplateLoader;
 
 //@CommonsLog // ?!?
 public class DefaultGunConfigurator implements GunConfigurator {
 	private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(DefaultGunConfigurator.class);
 
 	private Map<String,Extension> extensions = new HashMap<String, Extension>();
 	private Map<String,Template> templates = new HashMap<String,Template>();
 	private Map<String,MessageType> messageTypes = new HashMap<String,MessageType>();
 	private Module syslib;
 	private TemplateLoader templateLoader;
 	
 	@Getter @Setter private String activeTemplate;
 	
 	@Override
 	public void initialize() {
 		Map<String,Module> modules = new HashMap<String,Module>();
 
 		Reflections ref = new Reflections(
 				new ConfigurationBuilder()
 				.setUrls(ClasspathHelper.forJavaClassPath())
 				.setScanners(new SubTypesScanner())
 		);
 		Set<Class<? extends Module>> moduleClasses = ref.getSubTypesOf(Module.class);
 		Map<URL,Set<Class<? extends Module>>> sourceToModule = new HashMap<URL,Set<Class<? extends Module>>>();
 
 		// Obtain Code source for all module classes 
 		for(Class<?extends Module> m : moduleClasses) {
 			URL source = m.getProtectionDomain().getCodeSource().getLocation();
 			if(!sourceToModule.containsKey(source)) sourceToModule.put(source, new HashSet<Class<? extends Module>>());
 			sourceToModule.get(source).add(m);
 		}
 		
 		// initialize all module classes packaged in separate locations
 		for (Entry<URL, Set<Class<? extends Module>>> e : sourceToModule.entrySet()) {
 			if (e.getValue().size()==1) {
 				Module m;
 				try {
 					m = e.getValue().iterator().next().newInstance();
 					modules.put(m.getClass().getName(), m);
 				} catch (InstantiationException e1) {
 					log.error(e1);
 				} catch (IllegalAccessException e1) {
 					log.error(e1);
 				}
 			}
 		}
 		modules=checkDependencies(modules);
 		templateLoader = new MessageGunTemplateLoader(modules);
 	}
 
 	/**
 	 * check if all module dependencies are met and everything is there for a successful operation of the gun, initializes the modules
 	 * @param modules list of valid modules
 	 */
 	private Map<String,Module> checkDependencies(Map<String,Module> modules) {
 		Map<String,Module> work = new HashMap<String, Module>(modules);
 		DependencyResolver r = new DependencyResolver(new ArrayList<Module>(work.values()));
 		r.checkDependencies();
 		for (String s : r.getUnsatisfiedModules()) work.remove(s);
 		for(Entry<String, Module> e : work.entrySet()) {
			if(r.getUnsatisfiedModules().contains(r)) continue;
 			if (e.getValue().getClass().getName().equals("com.illmeyer.polygraph.syslib.Syslib")) {
 				syslib=e.getValue();
 			} else {
 				if (e.getValue() instanceof MessageType) messageTypes.put(e.getKey(),(MessageType)e.getValue());				
 				if (e.getValue() instanceof Extension) extensions.put(e.getKey(),(Extension)e.getValue());
 				if (e.getValue() instanceof Template) templates.put(e.getKey(),(Template)e.getValue());
 			}
 			e.getValue().initialize();
 		}
 		if (templates.isEmpty()) log.error("No usable templates found");
 		if (messageTypes.isEmpty()) log.error("No usable message types found");
 		return work;
 	}
 
 
 	@Override
 	public TemplateLoader getTemplateLoader() {
 		return templateLoader;
 	}
 
 	@Override
 	public void registerModules(Gun g) {
 		Map<String,Object> mtm = new HashMap<String, Object>();
 		g.getContext().put("mt", mtm);
 		Map<String,Object> extm = new HashMap<String, Object>();
 		g.getContext().put("ext", extm);
 		Map<String,Object> tplm = new HashMap<String, Object>();
 		g.getContext().put("tpl", tplm);
 		for(Entry<String, MessageType> e: messageTypes.entrySet()) {
 			mtm.put(e.getKey().replace('.', '_'), e.getValue().createContext());
 		}
 		for(Entry<String, Extension> e: extensions.entrySet()) {
 			extm.put(e.getKey().replace('.', '_'), e.getValue().createContext());
 		}
 		for(Entry<String, Template> e: templates.entrySet()) {
 			tplm.put(e.getKey().replace('.', '_'), e.getValue().createContext());
 		}
 		if (syslib!=null) {
 			g.getContext().put("sys", syslib.createContext());
 		}
 		if (activeTemplate!=null && templates.containsKey(activeTemplate)) {
 			Template templateObject = templates.get(activeTemplate);
 			MessageType mt = messageTypes.get(templateObject.getMessageType());
 			g.setInitialTemplate(templateObject.getMainTemplatePath());
 			g.setMt(mt);
 		}
 	}
 
 	@Override
 	public void destroy() {
 		@SuppressWarnings("unchecked")
 		Map<String,Module>[] allModules = new Map[]{messageTypes,extensions,templates};
 		for (Map<String,Module> modules : allModules)
 		for (Module m:modules.values()) {
 			m.destroy();
 		}
 	}
 
 }
