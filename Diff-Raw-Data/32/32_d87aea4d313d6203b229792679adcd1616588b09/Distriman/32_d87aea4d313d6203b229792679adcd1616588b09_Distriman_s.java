 package fr.imag.adele.apam.distriman;
 
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.http.HttpService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import fr.imag.adele.apam.ApamManagers;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.CompositeType;
import fr.imag.adele.apam.DependencyManager;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.ManagerModel;
import fr.imag.adele.apam.Resolved;
import fr.imag.adele.apam.Specification;
import fr.imag.adele.apam.declarations.DependencyDeclaration;
import fr.imag.adele.apam.declarations.ResolvableReference;
import fr.imag.adele.apam.distriman.disco.MachineDiscovery;
 
 @org.apache.felix.ipojo.annotations.Component(name = "Apam::Distriman")
 @Instantiate
 public class Distriman implements DependencyManager{
 
     //TODO resolved via system/framework/httpservice property
     private static final int HTTP_PORT = 8080;
 
     /**
      * Hostname of the InetAdress to be used for the MachineDiscovery.
      */
     @Property(name = "inet.host",value = "localhost",mandatory = true)
     private String HOST;
 
     @Requires(optional = false)
     private HttpService http;
 
     private final DependencyManager apamMan;
 
 
     //Default logger
     private static Logger logger = LoggerFactory.getLogger(Distriman.class);
 
     private final RemoteMachineFactory remotes;
 
     private final LocalMachine my_local = LocalMachine.INSTANCE;
 
     private final MachineDiscovery discovery;
 
 
     private final BundleContext context;
 
     public Distriman(BundleContext context) {
         this.context = context;
 
         remotes = new RemoteMachineFactory(context);
         discovery = new MachineDiscovery(remotes);
         apamMan= ApamManagers.getManager(CST.APAMMAN);
     }
 
     public String getName() {
         return CST.DISTRIMAN;
     }
 
     //
     // DependencyManager methods
     //
 
     @Override
     public void getSelectionPath(Instance client, DependencyDeclaration dependency, List<DependencyManager> selPath) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public int getPriority() {
         return 4;  //TODO is it alright
     }
 
     @Override
     public void newComposite(ManagerModel model, CompositeType composite) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
    
 	@Override
 	public Resolved resolveDependency(Instance client, DependencyDeclaration dependency, boolean needsInstances) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
     @Override
     public Instance findInstByName(Instance client, String instName) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Implementation findImplByName(Instance client, String implName) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Specification findSpecByName(Instance client, String specName) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Component findComponentByName(Instance client, String compName) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Instance resolveImpl(Instance client, Implementation impl, Set<String> constraints, List<String> preferences) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Set<Instance> resolveImpls(Instance client, Implementation impl, Set<String> constraints) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void notifySelection(Instance client, ResolvableReference resName, String depName, Implementation impl, Instance inst, Set<Instance> insts) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ComponentBundle findBundle(CompositeType context, String bundleSymbolicName, String componentName) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     //
     // Lifecycle call back
     //
 
     @Validate
     private void init(){
         logInfo("Starting...");
 
         //init the local machine
         my_local.init("localhost",HTTP_PORT);
 
         //start the discovery
         discovery.start(HOST);
 
         //Register this local machine servlet
         try {
             http.registerServlet(LocalMachine.INSTANCE.getPath(),my_local.getServlet(),null,null);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
 
         //publish this local machine over the network!
         try {
             discovery.registerLocal(my_local);
         } catch (IOException e) {
             discovery.stop();
             http.unregister(my_local.getPath());
             throw new RuntimeException(e);
         }
 
         //Add Distriman to Apam
         logInfo("Successfully initialized");
     }
 
     @Invalidate
     private void stop(){
         logInfo("Stopping...");
 
         //stop the discovery
         discovery.stop();
 
         http.unregister(my_local.getPath());
 
         logInfo("Successfully stopped");
     }
 
     //
     // Convenient static log method
     //
 
     protected static void logInfo(String message,Throwable t){
         logger.info("["+CST.DISTRIMAN+"]"+message,t);
     }
 
     protected static void logInfo(String message){
         logger.info("["+CST.DISTRIMAN+"]"+message);
     }
 
 }
