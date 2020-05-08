 package org.mule.galaxy.impl.jcr;
 
 import org.mule.galaxy.ArtifactType;
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.Plugin;
 import org.mule.galaxy.PluginInfo;
 import org.mule.galaxy.PluginManager;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.impl.artifact.XmlArtifactTypePlugin;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 import org.mule.galaxy.index.IndexManager;
 import org.mule.galaxy.plugins.config.jaxb.ArtifactPolicyType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyArtifactType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyPoliciesType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyType;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.util.SecurityUtils;
 import org.mule.galaxy.view.ViewManager;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.util.ClassUtils;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 public class PluginManagerImpl extends AbstractReflectionDao<PluginInfo> 
     implements ApplicationContextAware, PluginManager {
     public static final String PLUGIN_SERVICE_PATH = "META-INF/";
 
     public static final String GALAXY_PLUGIN_DESCRIPTOR = "galaxy-plugins.xml";
 
     private final Log log = LogFactory.getLog(getClass());
     
     protected Registry registry;
     protected Dao<ArtifactType> artifactTypeDao;
     protected ViewManager viewManager;
     protected IndexManager indexManager;
     protected PolicyManager policyManager;
     private ApplicationContext context;
     private JcrTemplate jcrTemplate;
     private List<Plugin> plugins = new ArrayList<Plugin>();
     
     public PluginManagerImpl() throws Exception {
         super(PluginInfo.class, "plugins", true);
     }
     public void setJcrTemplate(JcrTemplate jcrTemplate) {
         this.jcrTemplate = jcrTemplate;
     }
 
     public void setApplicationContext(ApplicationContext context) throws BeansException {
         this.context = context;
     }
 
     @Override
     protected void doCreateInitialNodes(Session session, Node objects) throws RepositoryException {
 
         SecurityUtils.doPriveleged(new Runnable() {
             public void run() {
                 initializePlugins();
             }
         });
     }
     
     public List<PluginInfo> getInstalledPlugins() {
         return listAll();
     }
     
     public PluginInfo getPluginInfo(String pluginName) {
         List<PluginInfo> plugins = find("plugin", pluginName);
         
         for (PluginInfo p : plugins) {
             return p;
         }
         
         return null;
     }
     
     public void initializePlugins() {
         try {
             loadXmlPlugins();
             loadSpringPlugins();
             
             for (Plugin p : plugins) {
                 PluginInfo pluginInfo = getPluginInfo(p.getName());
                 
                 if (pluginInfo == null) {
                     pluginInfo = new PluginInfo();
                    pluginInfo.setPlugin(p.getName());
                     pluginInfo.setVersion(p.getVersion());
                     
                     save(pluginInfo);
                     
                     p.initializeOnce();
                 } else {
                     int newVersion = p.getVersion();
                     
                     if (newVersion > pluginInfo.getVersion()) {
                         // TODO upgrade
                     }
                 }
                 
                 p.initializeEverytime();
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     @SuppressWarnings("unchecked")
     private void loadSpringPlugins() {
         plugins.addAll(context.getBeansOfType(Plugin.class).values());
     }
 
     protected void loadXmlPlugins() throws Exception
     {
         JcrUtil.doInTransaction(jcrTemplate.getSessionFactory(), new JcrCallback()
         {
             public Object doInJcr(Session session) throws IOException, RepositoryException
             {
                 try
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
                             plugin.setViewManager(viewManager);
                             plugin.setPolicyManager(policyManager);
                             
                             plugins.add(plugin);
                         }
                         
                         GalaxyPoliciesType policies = pluginsType.getPolicies();
                         if (policies != null) {
                             for (ArtifactPolicyType p : policies.getArtifactPolicy()) {
                                 Class clazz = ClassUtils.forName(p.getClazz());
                                 ArtifactPolicy policy = (ArtifactPolicy)clazz.newInstance();
                                 policy.setRegistry(registry);
                                 policyManager.addPolicy(policy);
                             }
                         }
                     }
                 }
                 catch (Exception e1)
                 {
                     throw new RuntimeException(e1);
                 }
                 return null;
             }
         });
 
     }
 
     public void setRegistry(Registry registry)
     {
         this.registry = registry;
     }
 
     public void setArtifactTypeDao(Dao<ArtifactType> artifactTypeDao)
     {
         this.artifactTypeDao = artifactTypeDao;
     }
 
     public void setViewManager(ViewManager viewManager)
     {
         this.viewManager = viewManager;
     }
 
     public void setIndexManager(IndexManager indexManager)
     {
         this.indexManager = indexManager;
     }
 
     public void setPolicyManager(PolicyManager policyManager)
     {
         this.policyManager = policyManager;
     }
 }
