 package fr.imag.adele.obrMan.internal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 import org.apache.felix.bundlerepository.Repository;
 import org.apache.felix.bundlerepository.RepositoryAdmin;
 import org.apache.felix.bundlerepository.Resource;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.Apam;
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.DependencyManager;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.ManagerModel;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.core.DependencyDeclaration;
 import fr.imag.adele.apam.core.InterfaceReference;
 import fr.imag.adele.apam.core.MessageReference;
 import fr.imag.adele.apam.core.ResolvableReference;
 import fr.imag.adele.apam.core.SpecificationReference;
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.util.Util;
 import fr.imag.adele.obrMan.OBRManCommand;
 import fr.imag.adele.obrMan.internal.OBRManager.Selected;
 
 public class OBRMan implements DependencyManager, OBRManCommand {
 
     // Link compositeType with it instance of obrManager
     private final Map<String, OBRManager> obrManagers;
 
     // iPOJO injected
     private RepositoryAdmin               repoAdmin;
 
     private Apam                          apam;
 
     private final Logger                  logger = LoggerFactory.getLogger(OBRMan.class);
 
     private final BundleContext           m_context;
 
     /**
      * OBRMAN activated, register with APAM
      */
 
     public OBRMan(BundleContext context) {
         m_context = context;
         obrManagers = new HashMap<String, OBRManager>();
     }
 
     public void start() {
         ApamManagers.addDependencyManager(this, 3);
         System.out.println("[OBRMAN] started");
     }
 
     public void stop() {
         ApamManagers.removeDependencyManager(this);
         obrManagers.clear();
         System.out.println("[OBRMAN] stopped");
     }
 
 
     /**
      * Instal ans instantiate the selected bundle, and return the component.
      * If forced = false (default) does not try to install if the component is allready existing.
      * 
      * @param selected
      * @param forced
      * @return
      */
     private Component installInstantiate(Selected selected) {
         if (selected == null)
             return null;
 
         String name = selected.getComponentName();
         fr.imag.adele.apam.Component c = CST.componentBroker.getComponent(name);
         // Check if already deployed
         if (c == null) {
            // check if the resource is already deployed
             if (alreadyDeployed(selected)){
                System.err.print("Already installed resource : " + selected.getResource().getSymbolicName());
                 return null;
             }
             // deploy selected resource
             boolean deployed = selected.obrManager.deployInstall(selected);
             if (!deployed) {
                 System.err.print("could not install resource ");
                 ObrUtil.printRes(selected.resource);
                 return null;
             }
             // waiting for the component to be ready in Apam.
             c = CST.componentBroker.getWaitComponent(name);
         } else { // do not install twice.
             // It is a logical deployment. The already existing component is not visible !
             // System.err.println("Logical deployment of : " + name + " found by OBRMAN but allready deployed.");
         }
 
         return c;
     }
 
     private boolean alreadyDeployed(Selected selected) {
         for (Resource resource : selected.obrManager.getRunningResources().getResources()) {
             if (resource.equals(selected.resource)){
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public String getName() {
         return CST.OBRMAN;
     }
 
     // at the end
     @Override
     public void
             getSelectionPath(CompositeType compTypeFrom, DependencyDeclaration dep, List<DependencyManager> involved) {
         involved.add(involved.size(), this);
     }
 
     @Override
     public Instance resolveImpl(Composite composite, Implementation impl, DependencyDeclaration dep) {
         return null;
     }
 
     @Override
     public Set<Instance> resolveImpls(Composite composite, Implementation impl, DependencyDeclaration dep) {
         return null;
     }
 
     @Override
     public int getPriority() {
         return 3;
     }
 
     @Override
     public void newComposite(ManagerModel model, CompositeType compositeType) {
         OBRManager obrManager;
         if (model == null) { // if no model for the compositeType, set the root composite model 
             obrManager = searchOBRManager(compositeType);
         } else {
             try {// try to load the compositeType model
                 LinkedProperties obrModel = new LinkedProperties();
                 obrModel.load(model.getURL().openStream());
                 obrManager = new OBRManager(this, compositeType.getName(), repoAdmin, obrModel);
             } catch (IOException e) {// if impossible to load the model for the compositeType, set the root composite model 
                 logger.error("Invalid OBRMAN Model. Cannot be read stream " + model.getURL(), e.getCause());
                 obrManager = searchOBRManager(compositeType);
             }
         }
         obrManagers.put(compositeType.getName(), obrManager);
     }
 
     // interface manager
     private Implementation resolveSpec(CompositeType compoType, ResolvableReference resource,
             Set<Filter> constraints, List<Filter> preferences) {
 
         // Find the composite OBRManager
         OBRManager obrManager = searchOBRManager(compoType);
         if (obrManager == null)
             return null;
 
         // temporary ??
         if (preferences == null)
             preferences = new ArrayList<Filter>();
         Filter f = ApamFilter.newInstance("(apam-composite=true)");
         preferences.add(f);
         // end
 
         f = ApamFilter.newInstance("(" + CST.COMPONENT_TYPE + "=" + CST.IMPLEMENTATION + ")");
         constraints.add(f);
 
         fr.imag.adele.obrMan.internal.OBRManager.Selected selected = null;
         Implementation impl = null;
         if (resource instanceof SpecificationReference) {
             selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(provide-specification*>"
                     + resource.as(SpecificationReference.class).getName() + ")",
                     constraints, preferences);
         }
         if (resource instanceof InterfaceReference) {
             selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(provide-interfaces*>" // =*;"
                     + resource.as(InterfaceReference.class).getJavaType() + ")", // ";*)"
                     constraints, preferences);
         }
         if (resource instanceof MessageReference) {
             selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(provide-messages*>"
                     + resource.as(MessageReference.class).getJavaType() + ")",
                     constraints, preferences);
         }
         if (selected != null) {
             // String implName = obrManager.getAttributeInCapability(selected.capability, "impl-name");
             impl = (Implementation) installInstantiate(selected);
             // System.out.println("deployed :" + impl);
             // printRes(selected);
             return impl;
         }
         return null;
     }
 
     private OBRManager searchOBRManager(CompositeType compoType) {
         OBRManager obrManager = null;
 
         // in the case of root composite, compoType = null
         if (compoType != null) {
             obrManager = obrManagers.get(compoType.getName());
         }
 
        // Use the root composite if the model is not specified
         if (obrManager == null) {
             obrManager = obrManagers.get(CST.ROOT_COMPOSITE_TYPE);
             if (obrManager == null){ // If the root manager was never been initialized
                 // lookFor root.OBRMAN.cfg and create obrmanager for the root composite in a customized location
                 String rootModelurl = m_context.getProperty(ObrUtil.ROOT_MODEL_URL);
                 try {// try to load root obr model from the customized location
                     if (rootModelurl != null) {
                         URL urlModel = (new File(rootModelurl)).toURI().toURL();
                         setInitialConfig(urlModel);
                     }else {
                         LinkedProperties obrModel = new LinkedProperties();
                         customizedRootModelLocation();
                         obrModel.put(ObrUtil.LOCAL_MAVEN_REPOSITORY, "true");
                         obrModel.put(ObrUtil.DEFAULT_OSGI_REPOSITORIES, "true");
                         obrManager = new OBRManager(this, CST.ROOT_COMPOSITE_TYPE, repoAdmin, obrModel);
                         obrManagers.put(CST.ROOT_COMPOSITE_TYPE, obrManager);
                     }
                 } catch (Exception e) {// if failed to load customized location, set default properties for the root model
                     logger.error("Invalid Root URL Model. Cannot be read stream " + rootModelurl, e.getCause());
                     LinkedProperties obrModel = new LinkedProperties();
                     customizedRootModelLocation();
                     obrModel.put(ObrUtil.LOCAL_MAVEN_REPOSITORY, "true");
                     obrModel.put(ObrUtil.DEFAULT_OSGI_REPOSITORIES, "true");
                     obrManager = new OBRManager(this, CST.ROOT_COMPOSITE_TYPE, repoAdmin, obrModel);
                     obrManagers.put(CST.ROOT_COMPOSITE_TYPE, obrManager);
                 } 
             }
         }
        return obrManager;
     }
 
     private void customizedRootModelLocation() {
     
         
     }
 
     @Override
     public Set<Implementation> resolveSpecs(CompositeType compoType, DependencyDeclaration dep) {
         Set<Implementation> ret = new HashSet<Implementation>();
         ret.add(resolveSpec(compoType, dep));
         return ret;
     }
 
     @Override
     public Implementation resolveSpec(CompositeType compoType, DependencyDeclaration dep) {
         Set<Filter> constraints = Util.toFilter(dep.getImplementationConstraints());
         List<Filter> preferences = Util.toFilterList(dep.getImplementationPreferences());
 
         return resolveSpec(compoType, dep.getTarget(), constraints, preferences);
     }
 
     private <C extends Component> C findByName(CompositeType compoType, String componentName,
             Class<C> kind) {
         if (componentName == null)
             return null;
 
         // Find the composite OBRManager
         OBRManager obrManager = searchOBRManager(compoType);
         if (obrManager == null)
             return null;
 
         Selected selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(name=" + componentName + ")", null, null);
         fr.imag.adele.apam.Component c = installInstantiate(selected);
         if (c == null)
             return null;
         if (!kind.isAssignableFrom(c.getClass())) {
             System.err.println("ERROR : " + componentName + " is found but is not a " + kind.getCanonicalName());
             return null;
         }
 
         // @SuppressWarnings("")
         return kind.cast(c);
     }
 
     @Override
     public Component findComponentByName(CompositeType compoType, String componentName) {
         return findByName(compoType, componentName, fr.imag.adele.apam.Component.class);
     }
 
     @Override
     public Specification findSpecByName(CompositeType compoType, String specName) {
         return findByName(compoType, specName, fr.imag.adele.apam.Specification.class);
     }
 
     @Override
     public Implementation findImplByName(CompositeType compoType, String implName) {
         return findByName(compoType, implName, fr.imag.adele.apam.Implementation.class);
     }
 
     @Override
     public Instance findInstByName(Composite compo, String instName) {
         CompositeType compoType = compo.getCompType();
         return findByName(compoType, instName, fr.imag.adele.apam.Instance.class);
     }
 
     public OBRManager getOBRManager(String compositeTypeName) {
         return obrManagers.get(compositeTypeName);
     }
 
     @Override
     public void notifySelection(Instance client, ResolvableReference resName, String depName, Implementation impl,
             Instance inst, Set<Instance> insts) {
         // Do not care
     }
 
     public String getDeclaredOSGiOBR() {
         return m_context.getProperty(ObrUtil.OSGI_OBR_REPOSITORY_URL);
     }
 
     @Override
     public Set<String> getCompositeRepositories(String compositeTypeName) {
         Set<String> result = new HashSet<String>();
         OBRManager obrmanager = getOBRManager(compositeTypeName);
         if (obrmanager == null)
             return result;
      
         for (Repository repository : obrmanager.getRepositories()) {
           result.add(repository.getURI());
       }
         return result;
     }
 
     @Override
     public void setInitialConfig(URL modellocation) throws IOException {
         LinkedProperties obrModel = new LinkedProperties();
         if (modellocation != null){
                obrModel.load(modellocation.openStream());
                OBRManager obrManager = new OBRManager(this, CST.ROOT_COMPOSITE_TYPE, repoAdmin, obrModel);
                obrManagers.put(CST.ROOT_COMPOSITE_TYPE, obrManager);
         }else{
             throw new IOException("URL is null");
         }
     }
 
     @Override
     public ComponentBundle findBundle(CompositeType compoType, String bundleSymbolicName, String componentName) {
         if (bundleSymbolicName == null || componentName == null)
             return null;
 
         // Find the composite OBRManager
         OBRManager obrManager = searchOBRManager(compoType);
         if (obrManager == null)
             return null;
 
         return obrManager.lookForBundle(bundleSymbolicName, componentName);
     }
 
     @Override
     public Implementation findImplByDependency(CompositeType compoType,
             DependencyDeclaration dependency) {
         return findImplByName(compoType, dependency.getTarget().getName());
     }
 
 }
