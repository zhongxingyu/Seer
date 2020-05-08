 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.apform.impl;
 
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.felix.ipojo.ComponentInstance;
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.HandlerManager;
 import org.apache.felix.ipojo.InstanceManager;
 import org.apache.felix.ipojo.architecture.PropertyDescription;
 import org.apache.felix.ipojo.handlers.configuration.ConfigurationHandlerDescription;
 import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceDescription;
 import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandlerDescription;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.util.Logger;
 import org.osgi.framework.BundleContext;
 
 import fr.imag.adele.apam.Apam;
 import fr.imag.adele.apam.ApamComponent;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Link;
 import fr.imag.adele.apam.RelationDefinition;
 import fr.imag.adele.apam.apform.Apform2Apam;
 import fr.imag.adele.apam.apform.ApformInstance;
 import fr.imag.adele.apam.apform.impl.handlers.PropertyInjectionHandler;
 import fr.imag.adele.apam.apform.impl.handlers.RelationInjectionManager;
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.declarations.CallbackDeclaration;
 import fr.imag.adele.apam.declarations.InstanceDeclaration;
 import fr.imag.adele.apam.declarations.RelationDeclaration;
 import fr.imag.adele.apam.impl.BaseApformComponent;
 import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import fr.imag.adele.apam.impl.InstanceImpl;
 
 public class ApamInstanceManager extends InstanceManager implements
 		RelationInjectionManager.Resolver {
 
 	/**
 	 * The property used to configure this instance with its declaration
 	 */
 	public final static String ATT_DECLARATION = "declaration";
 
 	/**
 	 * Whether this instance was created directly using the APAM API
 	 */
 	private final boolean isApamCreated;
 
 	/**
 	 * The corresponding APAM declaration
 	 */
 	private InstanceDeclaration declaration;
 
 	/**
 	 * The associated Apform component
 	 */
 	protected Apform apform;
 
 	public ApamInstanceManager(ApamImplementationFactory implementation,
 			boolean isApamCreated, BundleContext context,
 			HandlerManager[] handlers) {
 
 		super(implementation, context, handlers);
 
 		this.isApamCreated = isApamCreated;
 	}
 
 	@Override
 	public ApamImplementationFactory getFactory() {
 		return (ApamImplementationFactory) super.getFactory();
 	}
 
 	@Override
 	public Object getPojoObject() {
 		if (getFactory().hasInstrumentedCode())
 			return super.getPojoObject();
 
 		return null;
 	}
 
 	public ApformInstance getApform() {
 		return apform;
 	}
 
 	public Instance getApamComponent() {
 		return getApform().getApamComponent();
 	}
 
 	@Override
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void configure(Element metadata, Dictionary configuration)
 			throws ConfigurationException {
 
 		String instanceName = (String) configuration.get("instance.name");
 
 		declaration = (InstanceDeclaration) configuration
 				.get(ApamInstanceManager.ATT_DECLARATION);
 		if (isApamCreated || (declaration == null)) {
 			declaration = new InstanceDeclaration(getFactory().getApform().getDeclaration().getReference(), instanceName, null);
 			for (Enumeration<String> properties = configuration.keys(); properties.hasMoreElements();) {
 				String property = properties.nextElement();
 				if (!Apform2Apam.isPlatformPrivateProperty(property))
 					declaration.getProperties().put(property,
 							configuration.get(property).toString());
 			}
 		}
 
 		/*
 		 * Create the associated Apform component
 		 * 
 		 * IMPORTANT This must be done before invoking super.configure() because
 		 * APAM handler configuration requires that the Apform be initialized.
 		 * 
 		 * TODO refactor responsibilities between the Apform and the instance
 		 * manager to avoid such fragile ordering problems
 		 */
 		apform = this.new Apform();
 
 		/*
 		 * InstanceManager.configure assumes that there is instrumented code
 		 * manipulation data, so we cannot reuse this method for APAM abstract
 		 * components. This is one of the drawbacks of trying to reuse
 		 * InstanceManager for all kind of APAM components.
 		 */
 
 		if (getFactory().hasInstrumentedCode()) {
 			configuration.put("instance.name", declaration.getName());
 			super.configure(metadata, configuration);
 		} else {
 			// Add the name
 			m_name = (String) configuration.get("instance.name");
 
 			// Create the standard handlers and add these handlers to the list
 			for (int i = 0; i < m_handlers.length; i++) {
 				m_handlers[i].init(this, metadata, configuration);
 			}
 		}
 
 		/*
 		 * TODO Currently there is no life-cycle handler, so we perform
 		 * configuration directly in the instance manager. Consider implementing
 		 * life-cycle management as a handler..
 		 */
 		loadLifeCycleCallbacks();
 	}
 
 	/**
 	 * Notify instance activation/deactivation
 	 */
 	@Override
 	public void setState(int state) {
 		super.setState(state);
 
 		/*
 		 * Copy ipojo properties to declaration on validation
 		 */
 		if (state == ComponentInstance.VALID) {
 			ConfigurationHandlerDescription configuration = (ConfigurationHandlerDescription) getInstanceDescription()
 					.getHandlerDescription("org.apache.felix.ipojo:properties");
 			ProvidedServiceHandlerDescription provides = (ProvidedServiceHandlerDescription) getInstanceDescription()
 					.getHandlerDescription("org.apache.felix.ipojo:provides");
 
 			if (configuration != null) {
 				for (PropertyDescription configurationProperty : configuration
 						.getProperties()) {
 					declaration.getProperties().put(
 							configurationProperty.getName(),
 							configurationProperty.getValue());
 				}
 			}
 
 			if (provides != null) {
 				for (ProvidedServiceDescription providedServiceDescription : provides
 						.getProvidedServices()) {
 					for (Object key : providedServiceDescription
 							.getProperties().keySet()) {
 						declaration.getProperties().put(
 								(String) key,
 								providedServiceDescription.getProperties()
 										.get(key).toString());
 					}
 				}
 			}
 		}
 
 		/*
 		 * For instances that are not created using the Apam API, register
 		 * instance with APAM on validation
 		 */
 		if (state == ComponentInstance.VALID && !isApamCreated)
 			Apform2Apam.newInstance(getApform());
 
 		if (state == ComponentInstance.INVALID)
 			// Apform2Apam.vanishInstance(getInstanceName());
 			((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(getInstanceName());
 	}
 
 	/**
 	 * Delegate APAM to resolve a given injection.
 	 * 
 	 * NOTE nothing is returned from this method, the call to APAM has as
 	 * side-effect the update of the relation.
 	 * 
 	 * @param injection
 	 */
 	@Override
 	public boolean resolve(RelationInjectionManager injection) {
 
 		/*
 		 * This instance is not actually yet managed by APAM
 		 */
 		if (getApform().getApamComponent() == null) {
 			System.err.println("resolve failure for client "
 					+ getInstanceName() + " : APAM instance unkown");
 			return false;
 		}
 
 		Apam apam = getFactory().getApam();
 		if (apam == null) {
 			System.err.println("resolve failure for client "
 					+ getInstanceName() + " : APAM not found");
 			return false;
 		}
 
 		/*
 		 * Find the relation to resolve and trigger resolution at the level
 		 * specified in the source kind
 		 */
 
 		RelationDefinition relDef = getApform().getApamComponent().getRelation(injection.getRelationInjection().getRelation().getIdentifier());
 
 		Component source = null;
 		switch (relDef.getSourceKind()) {
 		case INSTANCE:
 		case COMPONENT:
 			source = getApform().getApamComponent();
 			break;
 		case IMPLEMENTATION:
 			source = getApform().getApamComponent().getImpl();
 			break;
 		case SPECIFICATION:
 			source = getApform().getApamComponent().getSpec();
 			break;
 		}
 
 		return CST.apamResolver.resolveLink(source, relDef) != null;
 
 	}
 
 	/**
 	 * Delegate APAM to remove the currently resolved relation and force a new
 	 * resolution the next time the injected relation is accessed
 	 * 
 	 */
 	@Override
 	public boolean unresolve(RelationInjectionManager injection) {
 
 		/*
 		 * This instance is not actually yet managed by APAM
 		 */
 		if (getApform().getApamComponent() == null) {
 			// System.err.println("unresolve failure for client " +
 			// getInstanceName() + " : ASM instance unkown");
 			return false;
 		}
 
 		Apam apam = getFactory().getApam();
 		if (apam == null) {
 			System.err.println("unresolve failure for client "
 					+ getInstanceName() + " : APAM not found");
 			return false;
 		}
 
		RelationDeclaration relation = injection.getRelationInjection().getRelation();
		for (Link outgoing : ((InstanceImpl)getApform().getApamComponent()).getExistingLinks(relation.getIdentifier())) {
 			outgoing.remove();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Adds a new injected field to this instance
 	 */
 	@Override
 	public void addInjection(RelationInjectionManager relation) {
 		apform.injectedFields.add(relation);
 	}
 
 	/**
 	 * Get the list of injected fields
 	 */
 	public Set<RelationInjectionManager> getInjections() {
 		return apform.injectedFields;
 	}
 
 	/**
 	 * Loads the definition of the life-cycle callbacks associated with this
 	 * instance
 	 */
 	private void loadLifeCycleCallbacks() throws ConfigurationException {
 
 		if (!(getFactory().getDeclaration() instanceof AtomicImplementationDeclaration))
 			return;
 
 		AtomicImplementationDeclaration implementation = (AtomicImplementationDeclaration) getFactory()
 				.getDeclaration();
 		for (AtomicImplementationDeclaration.Event trigger : AtomicImplementationDeclaration.Event
 				.values()) {
 
 			Set<CallbackDeclaration> callbacks = implementation
 					.getCallback(trigger);
 
 			if (callbacks == null)
 				continue;
 
 			for (CallbackDeclaration callback : callbacks) {
 				addCallback(new LifecycleCallback(this, trigger, callback));
 			}
 
 		}
 	}
 
 	/**
 	 * Adds a new life-cycle change callback
 	 */
 	public void addCallback(LifecycleCallback callback)
 			throws ConfigurationException {
 		apform.lifeCycleCallbacks.add(callback);
 	}
 
 	/**
 	 * Adds a new property change callback
 	 */
 	public void addCallback(PropertyCallback callback)
 			throws ConfigurationException {
 		apform.propertyCallbacks.add(callback);
 	}
 
 	/**
 	 * Adds a new relation life-cycle callback
 	 */
 	public void addCallback(RelationCallback callback)
 			throws ConfigurationException {
 		apform.relationCallbacks.add(callback);
 	}
 
 	/**
 	 * This class represents the mediator between APAM and this instance manager
 	 */
 	public class Apform extends	BaseApformComponent<Instance, InstanceDeclaration> implements ApformInstance {
 
 		/**
 		 * The list of injected fields handled by this instance
 		 */
 		private final Set<RelationInjectionManager> injectedFields;
 
 		/**
 		 * The list of callbacks to notify when onInit, onRemove
 		 */
 		private final Set<LifecycleCallback> lifeCycleCallbacks;
 
 		/**
 		 * The list of callbacks to notify when a property is set
 		 */
 		private final Set<PropertyCallback> propertyCallbacks;
 
 		/**
 		 * The list of callbacks to notify when bind, Unbind
 		 */
 		private final Set<RelationCallback> relationCallbacks;
 
 		private Apform() throws ConfigurationException {
 
 			super(ApamInstanceManager.this.declaration);
 
 			injectedFields = new HashSet<RelationInjectionManager>();
 			lifeCycleCallbacks = new HashSet<LifecycleCallback>();
 			propertyCallbacks = new HashSet<PropertyCallback>();
 			relationCallbacks = new HashSet<RelationCallback>();
 
 		}
 
 		public InstanceManager getManager() {
 			return ApamInstanceManager.this;
 		}
 
 		@Override
 		public Object getServiceObject() {
 			return ApamInstanceManager.this.getPojoObject();
 		}
 
 		@Override
 		public void setApamComponent(Component apamComponent) {
 
 			Instance previousComponent = this.apamComponent;
 			super.setApamComponent(apamComponent);
 
 			/*
 			 * Invoke the execution platform instance callback on the pojo
 			 * object
 			 */
 
 			Object pojo = getServiceObject();
 			if (pojo == null)
 				return;
 
 			PropertyInjectionHandler handler = (PropertyInjectionHandler) ApamInstanceManager.this
 					.getHandler(ApamComponentFactory.APAM_NAMESPACE + ":"
 							+ PropertyInjectionHandler.NAME);
 
 			if (apamComponent != null) { // starting the instance
 
 				if (handler != null)
 					handler.setApamComponent(apamComponent);
 
 				if (pojo instanceof ApamComponent) {
 					ApamComponent serviceComponent = (ApamComponent) pojo;
 					serviceComponent.apamInit(this.apamComponent);
 				}
 
 				fireCallbacks(AtomicImplementationDeclaration.Event.INIT,this.apamComponent);
 				return;
 			}
 
 			if (apamComponent == null) { // stopping the instance
 
 				fireCallbacks(AtomicImplementationDeclaration.Event.REMOVE,previousComponent);
 
 				if (pojo instanceof ApamComponent) {
 					ApamComponent serviceComponent = (ApamComponent) pojo;
 					serviceComponent.apamRemove();
 				}
 
 				/*
 				 * dispose this instance
 				 */
 				if (handler != null)
 					handler.setApamComponent(null);
 
 				ApamInstanceManager.this.dispose();
 
 				return;
 			}
 
 		}
 
 		@Override
 		public boolean checkLink(Component destination, String depName) {
 
 			/*
 			 * Validate all the instrumentations can be performed
 			 */
 
 			for (RelationInjectionManager injectedField : injectedFields) {
 				if (!injectedField.isValid())
 					return false;
 			}
 
 			return true;
 		}
 
 		@Override
 		public boolean setLink(Component destination, String depName) {
 
 			/*
 			 * perform injection update
 			 */
 			for (RelationInjectionManager injectedField : injectedFields) {
 				if (injectedField.getRelationInjection().getRelation().getIdentifier().equals(depName)) {
 					injectedField.addTarget(destination);
 				}
 			}
 
 			/*
 			 * perform callback bind
 			 */
 
 			Object pojo = getServiceObject();
 			if (pojo == null)
 				return true;
 
 			fireCallbacks(RelationDeclaration.Event.BIND, depName, destination);
 
 			return true;
 		}
 
 		@Override
 		public boolean remLink(Component destination, String depName) {
 
 
 			/*
 			 * perform injection update
 			 */
 			for (RelationInjectionManager injectedField : injectedFields) {
 				if (injectedField.getRelationInjection().getRelation().getIdentifier().equals(depName)) {
 					injectedField.removeTarget(destination);
 				}
 			}
 
 			/*
 			 * perform callback unbind
 			 */
 
 			Object pojo = getServiceObject();
 			if (pojo == null)
 				return true;
 
 			fireCallbacks(RelationDeclaration.Event.UNBIND, depName,
 					destination);
 
 			return true;
 		}
 
 		@Override
 		public void setProperty(String attr, String value) {
 
 			Object pojo = getPojoObject();
 			if (pojo == null)
 				return;
 
 			fireCallbacks(attr, super.getApamComponent()
 					.getPropertyObject(attr));
 		}
 
 		private void fireCallbacks(AtomicImplementationDeclaration.Event event,
 				Instance component) {
 			for (LifecycleCallback callback : lifeCycleCallbacks) {
 				try {
 					if (callback.isTriggeredBy(event))
 						callback.invoke(component);
 				} catch (Exception ignored) {
 					getLogger().log(
 							Logger.ERROR,
 							"error invoking lifecycle callback "
 									+ callback.getMethod(), ignored);
 				}
 			}
 		}
 
 		private void fireCallbacks(String attr, Object value) {
 			for (PropertyCallback callback : propertyCallbacks) {
 				try {
 					if (callback.isTriggeredBy(attr))
 						callback.invoke(value);
 				} catch (Exception ignored) {
 					getLogger().log(
 							Logger.ERROR,
 							"error invoking callback " + callback.getMethod()
 									+ " for property " + attr, ignored);
 				}
 			}
 		}
 
 		private void fireCallbacks(RelationDeclaration.Event event,
 				String relationName, Component target) {
 			for (RelationCallback callback : relationCallbacks) {
 				try {
 					if (callback.isTriggeredBy(relationName, event))
 						callback.invoke(target);
 				} catch (Exception ignored) {
 					getLogger().log(
 							Logger.ERROR,
 							"error invoking lifecycle callback "
 									+ callback.getMethod(), ignored);
 				}
 			}
 		}
 
 	}
 }
