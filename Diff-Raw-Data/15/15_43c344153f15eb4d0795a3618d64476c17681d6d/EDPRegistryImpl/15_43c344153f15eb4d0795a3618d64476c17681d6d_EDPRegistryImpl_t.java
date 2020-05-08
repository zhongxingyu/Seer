 /*******************************************************************************
  * Copyright (c) 2013 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.edp.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.wazaabi.engine.edp.AdapterFactory;
 import org.eclipse.wazaabi.engine.edp.ComponentFactory;
 import org.eclipse.wazaabi.engine.edp.IdentifiableFactory;
 import org.eclipse.wazaabi.engine.edp.Registry;
 import org.eclipse.wazaabi.engine.edp.adapters.EventAdapter;
 import org.eclipse.wazaabi.engine.edp.adapters.EventHandlerAdapter;
 import org.eclipse.wazaabi.engine.edp.coderesolution.AbstractCodeDescriptor;
 import org.eclipse.wazaabi.engine.edp.coderesolution.ExecutableAdapter;
 import org.eclipse.wazaabi.engine.edp.coderesolution.ICodeLocator;
 import org.eclipse.wazaabi.engine.edp.converters.BundledConverter;
 import org.eclipse.wazaabi.engine.edp.converters.BundledConverterFactory;
 import org.eclipse.wazaabi.engine.edp.events.EventAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.events.EventHandlerAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.executables.ExecutableAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.internal.osgi.Activator;
 import org.eclipse.wazaabi.engine.edp.validators.BundledValidator;
 import org.eclipse.wazaabi.engine.edp.validators.BundledValidatorFactory;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EDPRegistryImpl implements Registry, ServiceListener {
 
 	private final Logger logger = LoggerFactory
 			.getLogger(EDPRegistryImpl.class);
 
 	private HashMap<Class<?>, List<Object>> activatedServices = new HashMap<Class<?>, List<Object>>();
 	private HashMap<Object, ServiceReference<?>> serviceToServiceReference = new HashMap<Object, ServiceReference<?>>();
 	private HashMap<ServiceReference<?>, Object> serviceReferenceToService = new HashMap<ServiceReference<?>, Object>();
 	private HashMap<Class<?>, Boolean> blockedServices = new HashMap<Class<?>, Boolean>();
 
 	private boolean isDisposed = false;
 
 	public EDPRegistryImpl() {
 		logger.debug("Registry created");
 		if (Activator.getDefault() != null
 				&& Activator.getDefault().getContext() != null) {
 			Activator.getDefault().getContext().addServiceListener(this);
 		}
 	}
 
 	public Adapter createAdapter(Object callingContext, EObject model,
 			Object creationHint, Class<?> returnedType) {
 		if (isDisposed || returnedType == null || model == null)
 			return null;
 
 		Class<?> interfaze = getServiceInterfacerFor(returnedType);
 		if (interfaze == null) {
 			logger.error("No factory interface found for {}", returnedType); //$NON-NLS-1$
 			return null;
 		}
 		IdentifiableFactory f = getFactoryFor(callingContext, model,
 				creationHint, interfaze);
 		if (f instanceof AdapterFactory) {
 			return ((AdapterFactory) f).createAdapter(callingContext, model,
 					creationHint);
 		}
 		return null;
 	}
 
 	/**
	 * Given an interface, returns a copy of services implementing this
	 * interface by looking up into OSGI declarative services if running. Each
	 * time service is found, it is stored in a cache.
 	 * 
 	 * @param interfaze
	 *            The interface the returned services implement
	 * @return a list of services A unmodifiable, not null, copy of the services
	 *         implementing the interface
 	 */
 	public List<Object> getServices(Class<?> interfaze) {
 
 		//		logger.debug("seeking service for {}", interfaze); //$NON-NLS-1$
 		if (isDisposed || interfaze == null)
 			return null;
 
 		// first we lookup into activatedServices
 		List<Object> services = activatedServices.get(interfaze);
 		if (services == null) {
 			services = new ArrayList<Object>();
 			activatedServices.put(interfaze, services);
 		}
 
 		// if running from within a OSGI container
 		if (Activator.getDefault() != null
 				&& Activator.getDefault().getContext() != null
 				&& !blockedServices.containsKey(interfaze)) {
 			List<Object> declaratedServices = new ArrayList<Object>();
 			try {
 				for (ServiceReference<?> sr : Activator.getDefault()
 						.getContext().getServiceReferences(interfaze, null)) {
 
 					// did we already meet this service reference ?
 					if (serviceReferenceToService.containsKey(sr))
 						continue;
 
 					Object service = Activator.getDefault().getContext()
 							.getService(sr);
 					if (service == null)
 						continue;
 
 					serviceToServiceReference.put(service, sr);
 					serviceReferenceToService.put(sr, service);
 					declaratedServices.add(service);
 
 					logger.debug("Discovered : {} from OSGI DS", //$NON-NLS-1$
 							service);
 
 				}
 			} catch (InvalidSyntaxException e) {
 				logger.error("{}", e);
 			}
			if (!declaratedServices.isEmpty())
				services.addAll(declaratedServices);
 		}
 		return Collections.unmodifiableList(services);
 	}
 
 	/**
 	 * Returns the service class name of the factory returning the given
 	 * returned type. Factories are registered by osgi framework using the
 	 * service class name.
 	 * 
 	 * @param returnedType
 	 * @return
 	 */
 	protected Class<?> getServiceInterfacerFor(Class<?> returnedType) {
 		if (EventAdapter.class.equals(returnedType))
 			return EventAdapterFactory.class;
 		if (EventHandlerAdapter.class.equals(returnedType))
 			return EventHandlerAdapterFactory.class;
 		if (ExecutableAdapter.class.equals(returnedType))
 			return ExecutableAdapterFactory.class;
 		if (AbstractCodeDescriptor.class.equals(returnedType))
 			return ICodeLocator.class;
 		if (BundledConverter.class.equals(returnedType))
 			return BundledConverterFactory.class;
 		if (BundledValidator.class.equals(returnedType))
 			return BundledValidatorFactory.class;
 		return null;
 	}
 
 	public IdentifiableFactory getFactoryFor(Object callingContext,
 			Object model, Object creationHint, Class<?> interfaze) {
 		if (isDisposed || model == null || interfaze == null)
 			return null;
 
 		List<Object> services = getServices(interfaze);
 
 		for (Object service : services)
 			if (service instanceof IdentifiableFactory
 					&& ((IdentifiableFactory) service).isFactoryFor(
 							callingContext, model, creationHint)) {
 				//				logger.debug("found DeclaratedFactory : {}", service); //$NON-NLS-1$
 				return (IdentifiableFactory) service;
 			}
 
 		return null;
 	}
 
 	public Object createComponent(Object callingContext, Object model,
 			Object creationHint, Class<?> returnedType) {
 		if (isDisposed || returnedType == null || model == null)
 			return null;
 
 		Class<?> interfaze = getServiceInterfacerFor(returnedType);
 		if (interfaze == null) {
 			logger.error("No factory interface found for {}", returnedType); //$NON-NLS-1$
 			return null;
 		}
 		IdentifiableFactory f = getFactoryFor(callingContext, model,
 				creationHint, interfaze);
 		if (f instanceof ComponentFactory) {
 			return ((ComponentFactory) f).createComponent(callingContext,
 					model, creationHint);
 		}
 		return null;
 	}
 
 	@Override
 	public void startBatchOptimization() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void endBatchOptimization() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setServices(Class<?> interfaze, List<Object> services,
 			boolean blockOSGI) {
 		List<Object> servicesToRemove = new ArrayList<Object>();
 		if (services != null && Activator.getDefault() != null
 				&& Activator.getDefault().getContext() != null) {
 			List<Object> existingServices = activatedServices.get(interfaze);
 			if (existingServices != null)
 				for (Object existingService : existingServices)
 					if (!services.contains(existingService))
 						servicesToRemove.add(existingService);
 		}
 		activatedServices.put(interfaze,
 				services != null ? new ArrayList<Object>(services)
 						: new ArrayList<Object>());
 		blockedServices.put(interfaze, blockOSGI);
 		if (Activator.getDefault() != null
 				&& Activator.getDefault().getContext() != null)
 			for (Object service : servicesToRemove) {
 				ServiceReference<?> sr = serviceToServiceReference.get(service);
 				if (sr != null) {
 					Activator.getDefault().getContext().ungetService(sr);
 					serviceToServiceReference.remove(service);
 					serviceReferenceToService.remove(sr);
 				}
 			}
 
 	}
 
 	@Override
 	public void dispose() {
 		logger.debug("Registry disposed");
 		if (Activator.getDefault() != null
 				&& Activator.getDefault().getContext() != null) {
 			Activator.getDefault().getContext().removeServiceListener(this);
 		}
 		isDisposed = true;
 	}
 
 	@Override
 	public void initialize(Registry otherRegistry) {
 		if (otherRegistry == null)
 			return;
 		activatedServices
 				.putAll(((EDPRegistryImpl) otherRegistry).activatedServices);
 		serviceReferenceToService
 				.putAll(((EDPRegistryImpl) otherRegistry).serviceReferenceToService);
 		serviceToServiceReference
 				.putAll(((EDPRegistryImpl) otherRegistry).serviceToServiceReference);
 	}
 
 	@Override
 	public boolean isDisposed() {
 		return isDisposed;
 	}
 
 	@Override
 	public void serviceChanged(ServiceEvent event) {
 		if (event.getType() == ServiceEvent.UNREGISTERING) {
 			ServiceReference<?> sr = event.getServiceReference();
 			Object service = serviceReferenceToService.get(sr);
 			if (service != null) {
 				serviceReferenceToService.remove(sr);
 				serviceToServiceReference.remove(service);
 
 				List<Class<?>> foundInterfaces = new ArrayList<Class<?>>();
 				for (Class<?> i : activatedServices.keySet())
 					if (i.isAssignableFrom(service.getClass()))
 						foundInterfaces.add(i);
 				for (Class<?> i : foundInterfaces) {
 					List<Object> services = activatedServices.get(i);
 					services.remove(service);
 					blockedServices.remove(i);
 				}
 			}
 		}
 	}
 }
