 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.dispatch;
 
import sorcer.core.Cataloger;
 import sorcer.core.Dispatcher;
 import sorcer.core.Provider;
 import sorcer.core.exertion.Jobs;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.loki.member.LokiMemberUtil;
 import sorcer.service.Context;
 import sorcer.service.Exertion;
 import sorcer.service.Job;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * This class creates instances of appropriate subclasses of Dispatcher. The
  * appropriate subclass is determined by calling the ServiceJob object's
  */
 public class ExertionDispatcherFactory implements DispatcherFactory {
 
 	private static ExertionDispatcherFactory factory;
	public static Cataloger catalog; // The service catalog object
 	private final static Logger logger = Logger
 			.getLogger(ExertionDispatcherFactory.class.getName());
 
 	private ExertionDispatcherFactory() {
 		// do nothing
 	}
 
 	public static ExertionDispatcherFactory getFactory() {
 		if (factory == null)
 			factory = new ExertionDispatcherFactory();
 
 		return factory;
 	}
 
 	/**
 	 * Returns an instance of the appropriate subclass of Dispatcher as
 	 * determined from information provided by the given Job instance.
 	 * 
 	 * @param job
 	 *            The SORCER job that will be used to perform a collection of
 	 *            components exertions
 	 */
 	public Dispatcher createDispatcher(Job job, Provider provider)
 			throws DispatcherException {
 		return createDispatcher(job, new HashSet<Context>(), false, null, provider);
 	}
 		
 	public Dispatcher createDispatcher(Job job, Set<Context> sharedContexts,
 			boolean isSpawned, Provider provider) throws DispatcherException {
 		return createDispatcher(job, sharedContexts, isSpawned, null, provider);
 	}
 
 	public Dispatcher createDispatcher(Exertion exertion, Set<Context> sharedContexts,
 			boolean isSpawned, LokiMemberUtil myMemberUtil, Provider provider)
 			throws DispatcherException {
 		Dispatcher dispatcher = null;
 		try {
 			if (!exertion.isJob()) {
 				logger.info("Running Space Task Dispatcher...");
 				return new SpaceTaskDispatcher((NetTask)exertion,
 						sharedContexts, isSpawned, myMemberUtil);
 			}
 				
 			Job job = (Job)exertion;
 			if (Jobs.isSpaceSingleton(job)) {
 				logger.info("Running Space Sequential Dispatcher...");
 				dispatcher = new SpaceSequentialDispatcher(job,
 						sharedContexts, isSpawned, myMemberUtil, provider);
 			} else if (Jobs.isSpaceParallel(job)) {
 				logger.info("Running Space Parallel Dispatcher...");
 				dispatcher = new SpaceParallelDispatcher(job,
 						sharedContexts, isSpawned, myMemberUtil, provider);
 			} else if (Jobs.isSpaceSequential(job)) {
 				logger.info("Running Space Sequential Dispatcher ...");
 				dispatcher = new SpaceSequentialDispatcher(job,
 						sharedContexts, isSpawned, myMemberUtil, provider);
 			} else if (Jobs.isCatalogSingleton(job)) {
 				logger.info("Running Catalog Singleton Dispatcher...");
 				dispatcher = new CatalogSingletonDispatcher(job,
 						sharedContexts, isSpawned, provider);
 			} else if (Jobs.isCatalogParallel(job)) {
 				logger.info("Running Catalog Parallel Dispatcher...");
 				dispatcher = new CatalogParallelDispatcher(job,
 						sharedContexts, isSpawned, provider);
 			} else if (Jobs.isCatalogSequential(job)) {
 				logger.info("Running Catalog Sequential Dispatcher...");
 				dispatcher = new CatalogSequentialDispatcher(job,
 						sharedContexts, isSpawned, provider);
 			} else if (Jobs.isSWIFSequential(job)) {
 				logger.info("Running SWIF Sequential Dispatcher...");
 				dispatcher = new SWIFSequentialDispatcher(job,
 						sharedContexts, isSpawned, provider);
 			}
 			//logger.info("*** tally of used dispatchers: " + ExertDispatcher.getDispatchers().size());
 		} catch (Throwable e) {
 			e.printStackTrace();
 			throw new DispatcherException(
 					"Failed to create the exertion dispatcher for job: "
 							+ exertion.getName(), e);
 		}
 		return dispatcher;
 	}
 }
