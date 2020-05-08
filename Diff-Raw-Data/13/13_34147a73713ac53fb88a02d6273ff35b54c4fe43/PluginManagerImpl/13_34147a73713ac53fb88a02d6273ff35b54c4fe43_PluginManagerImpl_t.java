 package org.mule.galaxy.impl.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.artifact.ArtifactType;
 import org.mule.galaxy.impl.jcr.JcrUtil;
 import org.mule.galaxy.index.IndexManager;
 import org.mule.galaxy.plugin.Plugin;
 import org.mule.galaxy.plugin.PluginInfo;
 import org.mule.galaxy.plugin.PluginManager;
 import org.mule.galaxy.plugins.config.jaxb.ArtifactPolicyType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyArtifactType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyPoliciesType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyType;
 import org.mule.galaxy.policy.Policy;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.render.RendererManager;
 import org.mule.galaxy.type.TypeManager;
 import org.mule.galaxy.util.GalaxyUtils;
 import org.mule.galaxy.util.SecurityUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.event.ContextRefreshedEvent;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.util.ClassUtils;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 public class PluginManagerImpl
     implements ApplicationContextAware, PluginManager, ApplicationListener {
     public static final String PLUGIN_SERVICE_PATH = "META-INF/";
 
     public static final String GALAXY_PLUGIN_DESCRIPTOR = "galaxy-plugins.xml";
 
     private final Log log = LogFactory.getLog(getClass());
     
     protected Registry registry;
     protected Dao<ArtifactType> artifactTypeDao;
     protected RendererManager rendererManager;
     protected IndexManager indexManager;
     protected PolicyManager policyManager;
     private ApplicationContext context;
     private List<Plugin> plugins = new ArrayList<Plugin>();
     private TypeManager typeManager;
     private Dao<PluginInfo> pluginDao;
     private JcrTemplate jcrTemplate;
     
     private String pluginDirectory;
     
     public void setApplicationContext(ApplicationContext context) throws BeansException {
         this.context = context;
     }
 
     public void initialize() throws IOException {
         Runnable runnable = new Runnable() {
             public void run() {
                 try {
                     initializePlugins();
                 } catch (Exception e) {
                     if (e instanceof RuntimeException) {
                         throw (RuntimeException) e;
                     }
                     throw new RuntimeException(e);
                 }
             }
         };
         
         SecurityUtils.doPriveleged(runnable);
     }
     
     public boolean isRunning() {
         return true;
     }
 
     public void onApplicationEvent(ApplicationEvent event) {
         if (event instanceof ContextRefreshedEvent) {
             if (((ContextRefreshedEvent)event).getApplicationContext() == context) {
                 File pluginDirFile = new File(pluginDirectory);
                 
                 if (pluginDirFile.exists()) {
                     for (File p : pluginDirFile.listFiles()) {
                         if (p.getName().endsWith(".zip")) {
                             try {
                                 loadPluginArchive(p);
                             } catch (IOException e) {
                                 log.error("Could not load plugin " + p.getAbsolutePath(), e);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public void stop() {
     }
 
     public List<PluginInfo> getInstalledPlugins() {
         return pluginDao.listAll();
     }
     
     public PluginInfo getPluginInfo(String pluginName) {
         List<PluginInfo> plugins = pluginDao.find("plugin", pluginName);
 
         return plugins.isEmpty() ? null : plugins.get(0);
     }
     
     public void initializePlugins() throws Exception {
         JcrUtil.doInTransaction(jcrTemplate.getSessionFactory(), new JcrCallback()
         {
             public Object doInJcr(Session session) throws IOException, RepositoryException
             {
                 try {
                     loadXmlPlugins();
                     loadSpringPlugins();
                     
                     for (Plugin p : plugins) {
                         PluginInfo pluginInfo = getPluginInfo(p.getName());
         
                         Integer previousVersion = null;
         
                         if (pluginInfo == null) {
                             pluginInfo = new PluginInfo();
                             pluginInfo.setPlugin(p.getName());
                             pluginInfo.setVersion(p.getVersion());
                             
                             pluginDao.save(pluginInfo);
                         }
                         else
                         {
                            // Stupid JCR ORM doesn't have int support
                            previousVersion = new Long(pluginInfo.getVersion()).intValue();
                         }
         
                         p.update(previousVersion);
                         p.initialize();
                     }
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                 return null;
             }
         });
     }
     
     @SuppressWarnings("unchecked")
     private void loadSpringPlugins() {
         plugins.addAll(context.getBeansOfType(Plugin.class).values());
     }
 
     protected void loadXmlPlugins() throws Exception
     {
         JAXBContext jc = JAXBContext.newInstance("org.mule.galaxy.plugins.config.jaxb");
 
         Enumeration e = getClass().getClassLoader().getResources(PLUGIN_SERVICE_PATH + GALAXY_PLUGIN_DESCRIPTOR);
         while (e.hasMoreElements())
         {
             URL url = (URL) e.nextElement();
             log.info("Loading plugins from: " + url.toString());
             Unmarshaller u = jc.createUnmarshaller();
             JAXBElement ele = (JAXBElement) u.unmarshal(url.openStream());
 
             GalaxyType pluginsType = (GalaxyType) ele.getValue();
             List<GalaxyArtifactType> pluginsList = pluginsType.getArtifactType();
 
             for (GalaxyArtifactType pluginType : pluginsList)
             {
                 XmlArtifactTypePlugin plugin = new XmlArtifactTypePlugin(pluginType);
                 plugin.setArtifactTypeDao(artifactTypeDao);
                 plugin.setIndexManager(indexManager);
                 plugin.setRegistry(registry);
                 plugin.setRendererManager(rendererManager);
                 plugin.setPolicyManager(policyManager);
                 plugin.setTypeManager(typeManager);
                 
                 plugins.add(plugin);
             }
             
             GalaxyPoliciesType policies = pluginsType.getPolicies();
             if (policies != null) {
                 for (ArtifactPolicyType p : policies.getArtifactPolicy()) {
                     Class clazz = ClassUtils.forName(p.getClazz());
                     Policy policy = (Policy)clazz.newInstance();
                     policy.setRegistry(registry);
                     policyManager.addPolicy(policy);
                 }
             }
         }
 
     }
 
     public void loadPluginArchive(File plugin) throws IOException {
         log.info("Loading plugin " + plugin.getAbsolutePath());
         String name = plugin.getName();
 
         File expand;
         if (plugin.isDirectory()) {
             expand = plugin;
         } else {        
             // trim off ".zip" and expand
             name = name.substring(0, name.length() - 4);
             expand = new File(pluginDirectory, name);
             expand.delete();
             expand.mkdirs();
             
             GalaxyUtils.expand(plugin.getAbsolutePath(), expand.getAbsolutePath());
         }
         
         loadPluginDirectory(expand);
     }
 
     protected void loadPluginDirectory(File expand) throws MalformedURLException, IOException {
         File libs = new File(expand, "lib");
         File[] libraries = libs.listFiles();
         URL[] urls = new URL[libraries.length];
         for (int i = 0; i < libraries.length; i++) {
             urls[i] = libraries[i].toURI().toURL();
         }
 
         ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
         // All the libraries in this expanded archive
         URLClassLoader libraryClassLoader = new URLClassLoader(urls, oldCL);
         // Don't include a parent class loader so we only pick up configuration files from the new jars
         URLClassLoader resourcesClassLoader = new URLClassLoader(urls, null);
         
         try {
             Thread.currentThread().setContextClassLoader(libraryClassLoader);
     
             PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourcesClassLoader);
             ConfigurableApplicationContext childCtx = createPluginApplicationContext(context);
             XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)childCtx);
             
             // Find spring config files
             Resource[] resources = resolver.getResources("classpath*:/META-INF/galaxy-applicationContext.xml");
 
             // load them!
             reader.loadBeanDefinitions(resources);
             childCtx.refresh();
         } finally {
             Thread.currentThread().setContextClassLoader(oldCL);
         }
     }
 
     protected ConfigurableApplicationContext createPluginApplicationContext(ApplicationContext context) {
         return new GenericApplicationContext(context);
     }
 
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     public void setArtifactTypeDao(Dao<ArtifactType> artifactTypeDao) {
         this.artifactTypeDao = artifactTypeDao;
     }
 
     public void setRendererManager(RendererManager viewManager) {
         this.rendererManager = viewManager;
     }
 
     public void setIndexManager(IndexManager indexManager) {
         this.indexManager = indexManager;
     }
 
     public void setJcrTemplate(JcrTemplate jcrTemplate) {
         this.jcrTemplate = jcrTemplate;
     }
 
     public void setPluginDao(Dao<PluginInfo> pluginDao) {
         this.pluginDao = pluginDao;
     }
 
     public void setPolicyManager(PolicyManager policyManager) {
         this.policyManager = policyManager;
     }
     
     public void setTypeManager(TypeManager typeManager) {
         this.typeManager = typeManager;
     }
 
     public void setPluginDirectory(String pluginDirectory) {
         this.pluginDirectory = pluginDirectory;
     }
     
 }
