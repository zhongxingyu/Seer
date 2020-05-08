 package org.bndtools.rt.executor;
 
 import static java.lang.Math.*;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 import org.bndtools.rt.executor.ExecutorImpl.Config;
 import org.bndtools.rt.executor.ExecutorImpl.Config.Type;
 
 import aQute.bnd.annotation.component.*;
 import aQute.bnd.annotation.metatype.*;
 
 /**
  * This bundle provides a java.util.concurrent.Executor service that can
  * be configured (for multiple instances) and is shared between all bundles.
  * 
  * @see <a
  *      href="http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/Executor.html">java.util.concurrent.Executor</a>
  */
 @Component(designateFactory = Config.class, configurationPolicy = ConfigurationPolicy.require, servicefactory = true)
 public class ExecutorImpl implements Executor {
 
 	/**
 	 * Configuration parameters expected from the Config Admin
 	 */
 	interface Config {
 		enum Type {
 			FIXED, CACHED, SINGLE
 		}
 
 		int service_ranking();
 		String service_pid();
 		String service_factoryPid();
 
 		Type type();
 
 		String id();
 
 		int size();
 	}
 
 	/**
 	 * Internal representation of the underlying (shared) instances of the
 	 * thread pool, associated with the number of bundles using them
 	 */
 	static class EsHolder {
 		ExecutorService	staticEs;
 		int				counter;
  		
 		EsHolder(Config config) {
 			counter = 0;
 
 			Type t = config.type();
 			if (t == null)
 				t = Config.Type.FIXED;
 			switch (t) {
 				case FIXED :
 					staticEs = Executors.newFixedThreadPool(max(config.size(), 2));
 					break;
 				case CACHED :
 					staticEs = Executors.newCachedThreadPool();
 					break;
 				case SINGLE :
 					staticEs = Executors.newSingleThreadExecutor();
 					break;
 			}
 		}
 
 		ExecutorService getEs() {
 			counter++;
 			return staticEs;
 		}
 
 		void unget() {
 			counter--;
 			if (counter == 0) {
 				staticEs.shutdownNow();
 			}
 		}
 	}
 
 	/**
 	 *  Wrapper around the runnable passed by the using bundle
 	 *  This wrapper gets a future object corresponding to its task
 	 *  once it is created, which allows it:
 	 *    - to add it to a per instance list of futures (in order
 	 *    	to be able to cancel tasks pertaining to an exiting bundle
 	 *    - to remove this future from the list of futures once the task
 	 *    	is finished (and does not have to be cancelled anymore)
 	 *
 	 */
 	class Wrapper implements Runnable {
 
 		Runnable	runnable;
 		Future< ? >	future	= null;
 
 		boolean		done	= false;
 
 		Wrapper( Runnable command) {
 			runnable = command;
 		}
 
 		void setFuture(Future< ? > f) {
 			this.future = f;
 			synchronized (futures) {
 				if (!done) {
 					futures.add(f);
 				}
 			}
 		}
 
 		@Override
 		public void run() {
 			try {
 				runnable.run();
 			}
 			finally {
 				synchronized (futures) {
 					if (future != null) {
 						futures.remove(future);
 					}
 					done = true;
 				}
 
 			}
 		}
 	}
 
 
 	static ConcurrentHashMap<String,EsHolder> holders = new ConcurrentHashMap<String,ExecutorImpl.EsHolder>();
 	// List of tasks submitted by one bundle
 	List<Future< ? >> futures = new ArrayList<Future< ? >>();
 	String servicePid;
 	// Executor implementation used by the bundle
 	ExecutorService	es;
 
 	/**
 	 * Creates a new instance of the underlying implementation of the executor
 	 * service (depending on the configuration parameters) if needed, or returns
 	 * a pre-existing instance of this service, shared by all bundles.
 	 * 
 	 * @param properties
 	 *            Configuration parameters, passed by the framework
 	 */
 	@Activate
 	void activate(Map<String,Object> properties) {
 
 		Config config = Configurable.createConfigurable(Config.class, properties);
 		servicePid = config.service_pid();
 		
 		synchronized(holders) {
			holders.putIfAbsent(servicePid, new EsHolder(config));
			es = holders.get(servicePid).getEs();
 		}
 
 	}
 
 	/**
 	 * Cancels the tasks submitted by the exiting bundle, shutting down the
 	 * executor service if no more bundle is using it
 	 * 
 	 * @param properties
 	 *            Configuration parameters, passed by the framework
 	 */
 	@Deactivate
 	void deactivate() {
 
 		
 		synchronized (futures) {
 			for (Future< ? > f : futures) {
 				f.cancel(true);
 			}
 		}
 
 		synchronized (holders) {
 			EsHolder holder = holders.get(servicePid);
 			holder.unget();
 			if (holder.counter == 0) {
 				holders.remove(servicePid);
 			}
 		}
 
 	}
 
 	@Override
 	public void execute(Runnable command) {
 		Wrapper w = new Wrapper(command);
 		Future< ? > f = es.submit(w);
 		w.setFuture(f);
 	}
 
 }
