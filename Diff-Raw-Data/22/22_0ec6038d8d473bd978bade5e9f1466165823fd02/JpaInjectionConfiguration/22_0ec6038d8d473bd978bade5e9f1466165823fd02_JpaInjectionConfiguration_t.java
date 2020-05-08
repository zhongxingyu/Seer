 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.jpa;
 
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

 import com.threewks.thundr.action.method.ActionInterceptorRegistry;
 import com.threewks.thundr.configuration.Environment;
 import com.threewks.thundr.injection.BaseInjectionConfiguration;
 import com.threewks.thundr.injection.UpdatableInjectionContext;
 
 public class JpaInjectionConfiguration extends BaseInjectionConfiguration {
 
 	public static final String PersistenceManagerRegistry = String.format("thundr-jpa-%s", PersistenceManagerRegistryImpl.class);
 
 	@Override
 	public void configure(UpdatableInjectionContext injectionContext) {
 		PersistenceManagerRegistry registry = initializePersistenceManagerRegistry(injectionContext);
 		registerTransactionalAnnotation(injectionContext, registry);
 	}
 
 	protected final void registerTransactionalAnnotation(UpdatableInjectionContext injectionContext, PersistenceManagerRegistry persistenceManagerRegistry) {
 		ActionInterceptorRegistry actionInterceptorRegistry = injectionContext.get(ActionInterceptorRegistry.class);
 		actionInterceptorRegistry.registerInterceptor(Transactional.class, new TransactionalActionInterceptor(persistenceManagerRegistry));
 	}
 
 	protected final PersistenceManagerRegistry initializePersistenceManagerRegistry(UpdatableInjectionContext injectionContext) {
 		PersistenceManagerRegistry registry = new PersistenceManagerRegistryImpl();
 		injectionContext.inject(registry).as(PersistenceManagerRegistry.class);
 
 		List<String> persistenceUnits = getPersistenceUnitNames();
		for (String persistenceUnit : persistenceUnits) {
 			PersistenceManager persistenceManager = new PersistenceManagerImpl(persistenceUnit);
 			registry.register(persistenceUnit, persistenceManager);
			injectionContext.inject(persistenceManager).as(PersistenceManager.class);
 		}
 
 		// Put reference into servlet context to provide context listener with access to call
 		// PersistenceManagerRegister#clear() on shutdown.
 		ServletContext context = injectionContext.get(ServletContext.class);
 		if (context.getAttribute(PersistenceManagerRegistry) == null) {
 			context.setAttribute(PersistenceManagerRegistry, registry);
 		}
 
 		return registry;
 	}
 
 	/**
 	 * Gets a list of persistence unit names to initialize. Defaults to a single persistence unit named after the
	 * current environment as provided by {@link Environment#get()}. Override this if you wish to initialize a
 	 * different or multiple persistence managers.
	 * 
 	 * @return a list of persistence unit names
 	 */
 	protected List<String> getPersistenceUnitNames() {
 		return Collections.singletonList(Environment.get());
 	}
 }
