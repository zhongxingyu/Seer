 package edu.uw.zookeeper.safari.peer;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.concurrent.ExecutionException;
 
import org.apache.logging.log4j.LogManager;
 import org.apache.zookeeper.KeeperException;
 
import com.google.common.base.Objects;
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 
 import edu.uw.zookeeper.EnsembleView;
 import edu.uw.zookeeper.ServerInetAddressView;
 import edu.uw.zookeeper.safari.Identifier;
 import edu.uw.zookeeper.safari.backend.BackendConfiguration;
 import edu.uw.zookeeper.safari.control.ControlMaterializerService;
 import edu.uw.zookeeper.safari.control.ControlSchema;
 
 public class EnsembleConfiguration {
 
     public static Module module() {
         return new Module();
     }
     
     public static class Module extends AbstractModule {
 
         public Module() {}
         
         @Override
         protected void configure() {
         }
 
         @Provides @Singleton
         public EnsembleConfiguration getEnsembleConfiguration(
                 BackendConfiguration backendConfiguration,
                 ControlMaterializerService control) throws InterruptedException, ExecutionException, KeeperException {
             checkState(control.isRunning());
             // Find my ensemble
             EnsembleView<ServerInetAddressView> myView = backendConfiguration.getView().getEnsemble();
             ControlSchema.Regions.Entity ensembleNode = ControlSchema.Regions.Entity.create(
                     myView, 
                     control.materializer()).get();
            EnsembleConfiguration instance = new EnsembleConfiguration(ensembleNode.get());
            LogManager.getLogger(getClass()).info("{}", instance);
            return instance;
         }
     }
     
     private final Identifier ensemble;
 
     public EnsembleConfiguration(Identifier ensemble) {
         this.ensemble = ensemble;
     }
     
     public Identifier getEnsemble() {
         return ensemble;
     }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("ensemble", ensemble).toString();
    }
 }
