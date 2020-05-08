 package cl.own.usi.gateway.client.impl.thrift;
 
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cl.own.usi.gateway.client.impl.thrift.WorkerClientThriftImpl.WorkerHost;
 import cl.own.usi.gateway.client.pool.MultiPool;
 import cl.own.usi.gateway.client.pool.exception.PoolException;
 import cl.own.usi.thrift.WorkerRPC.Client;
 
 /**
  * Wrapper for executing a thrift action.
 *
  * @author bperroud
  * @author nire
 *
  */
 abstract class ThriftAction<T> {
 
 	private static final int THRIFT_RETRY = 3;
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(ThriftAction.class);
 
 	private final MultiPool<WorkerHost, Client> pools;
 
 	public ThriftAction(final MultiPool<WorkerHost, Client> pools) {
 		this.pools = pools;
 	}
 
 	protected abstract String getActionDescription();
 
 	protected abstract T action(final Client client) throws TException;
 
 	public final T doAction() {
 
		final String description = getActionDescription();

 		for (int i = 0; i < THRIFT_RETRY; i++) {
 			final Client client = getClient();
 			try {
 				return action(client);
 			} catch (TException e) {
 				LOGGER.warn(
 						String.format(
 								"Exception caught while executing action %s through thrift (try=%d)",
								description, i), e);
 				pools.invalidate(client);
 			} finally {
 				if (client != null) {
 					release(client);
 				}
 			}
 		}
 
 		LOGGER.error(
 				"Failure to execute action {} after {} try, returning null instead",
				description, THRIFT_RETRY);
 		return null;
 	}
 
 	private final void release(final Client client) {
 		try {
 			if (client != null) {
 				pools.release(client);
 			}
 		} catch (PoolException e) {
 
 		}
 	}
 
 	private final Client getClient() {
 		try {
 			return pools.borrow();
 		} catch (PoolException e) {
 			throw new IllegalStateException("No pool borrowed...", e);
 		}
 	}
 
 }
