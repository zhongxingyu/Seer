 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package devoxx.core;
 
 import devoxx.core.db.NotesModel;
 import devoxx.core.fwk.HttpServiceTracker;
 import devoxx.core.fwk.SimpleLogger;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.Any;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
 import org.jboss.weld.environment.osgi.api.events.Invalid;
 import org.jboss.weld.environment.osgi.api.events.Valid;
 import org.osgi.util.tracker.ServiceTracker;
 
 @ApplicationScoped
 public class Server {
 
     private static final String CONTEXT_ROOT = "/";
     @Inject @Any Instance<Object> instances;
     private ServiceTracker tracker;
     private AtomicBoolean valid = new AtomicBoolean(false);
 
     public void validate(@Observes Valid event) {
         valid.getAndSet(true);
     }
 
     public void invalidate(@Observes Invalid event) {
         valid.getAndSet(false);
     }
 
     public boolean isValid() {
         return valid.get();
     }
 
     public void start(@Observes BundleContainerEvents.BundleContainerInitialized init) throws Exception {
        SimpleLogger.enableTrace(true);
         SimpleLogger.enableColors(true);
         this.tracker = new HttpServiceTracker(
                 init.getBundleContext(),
                 getClass().getClassLoader(),
                 instances, CONTEXT_ROOT);
         this.tracker.open();
        NotesModel.initDB();
     }
 }
