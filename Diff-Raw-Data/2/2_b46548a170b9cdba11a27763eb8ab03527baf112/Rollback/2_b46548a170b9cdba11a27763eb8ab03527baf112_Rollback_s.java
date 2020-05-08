 package de.cosmocode.palava.jpa.tx;
 
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import de.cosmocode.palava.ipc.IpcCall;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import de.cosmocode.palava.ipc.IpcCommand.Description;
 import de.cosmocode.palava.ipc.IpcCommand.Throw;
 import de.cosmocode.palava.ipc.IpcCommand.Throws;
 
 /**
  * See below.
  *
  * @author Willi Schoenborn
  */
@Description("Rollbacks the current transaction")
 @Throws({
     @Throw(name = IllegalStateException.class, description = "If the current transaction is not active"),
     @Throw(name = PersistenceException.class, description = "If rollback failed")
 })
 public final class Rollback implements IpcCommand {
 
     private static final Logger LOG = LoggerFactory.getLogger(Rollback.class);
 
     private final Provider<EntityManager> provider;
     
     @Inject
     public Rollback(Provider<EntityManager> provider) {
         this.provider = Preconditions.checkNotNull(provider, "Provider");
     }
 
     @Override
     public void execute(IpcCall call, Map<String, Object> result) throws IpcCommandExecutionException {
         LOG.trace("Rolling back transaction");
         provider.get().getTransaction().rollback();
     }
 
 }
