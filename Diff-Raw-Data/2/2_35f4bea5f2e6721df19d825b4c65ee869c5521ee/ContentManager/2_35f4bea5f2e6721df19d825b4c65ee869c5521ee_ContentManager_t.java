 package fr.imag.adele.dynamic.manager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Wire;
 import fr.imag.adele.apam.declarations.CompositeDeclaration;
 import fr.imag.adele.apam.declarations.ConstrainedReference;
 import fr.imag.adele.apam.declarations.DependencyDeclaration;
 import fr.imag.adele.apam.declarations.DependencyInjection;
 import fr.imag.adele.apam.declarations.GrantDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationReference;
 import fr.imag.adele.apam.declarations.InstanceDeclaration;
 import fr.imag.adele.apam.declarations.OwnedComponentDeclaration;
 import fr.imag.adele.apam.declarations.PropertyDefinition;
 import fr.imag.adele.apam.declarations.SpecificationReference;
 import fr.imag.adele.apam.impl.ComponentImpl.InvalidConfiguration;
 import fr.imag.adele.apam.impl.CompositeImpl;
 import fr.imag.adele.apam.impl.InstanceImpl;
 import fr.imag.adele.apam.util.Util;
 
 
 /**
  * This class is responsible for the content management of a composite.
  * 
  * @author vega
  *
  */
 public class ContentManager  {
 
 	@SuppressWarnings("unused")
 	private final static Logger	logger = LoggerFactory.getLogger(ContentManager.class);
 
 	/**
 	 * The declaration of the composite type
 	 */
 	private final CompositeDeclaration declaration;
 	
 	/**
 	 * The managed composite 
 	 */
 	private final Composite composite;
 	
 	/**
 	 * The instances that are owned by this content manager. This is a subset of the 
 	 * contained instances of the composite.
 	 */
 	private Map<OwnedComponentDeclaration, Set<Instance>> owned;
 
 	/**
 	 * The list of contained instances that must be dynamically created when the
 	 * specified triggering condition is satisfied
 	 */
 	private List<InstanceDeclaration> dynamicContains;
 
 	/**
 	 * The list of dynamic dependencies that must be updated without waiting for
 	 * lazy resolution
 	 */
 	private List<DynamicResolutionRequest> dynamicDependencies;
 	
 	/**
 	 * The list of waiting resolutions in this composite
 	 */
 	private List<PendingRequest> waitingResolutions;
 
 	
 	/**
 	 * The current state of the content manager
 	 */
 	private String state;
 
 	/**
 	 * The instance that holds the state of the content manager. This instance is
 	 * automatically created inside the composite at start of content manager.
 	 */
 	private Instance	stateHolder;
 
 	/**
 	 * The property used for holding the state
 	 */
 	private String 		stateProperty;
 	
 	/**
 	 * The active grant in the current state 
 	 */
 	private Map<OwnedComponentDeclaration, GrantDeclaration> granted;
 
 	/**
 	 * The list of pending resolutions waiting for a grant. This is a subset
 	 * of the waiting resolutions indexed by the associated grant.
 	 */
 	private Map<GrantDeclaration, List<PendingRequest>> pendingGrants;
 	
 	
 	
 	/**
 	 * Initializes the content manager
 	 */
 	public ContentManager(DynamicManagerImplementation manager, Composite composite) throws InvalidConfiguration {
 		
 		this.composite		= composite;
 		this.declaration	= composite.getCompType().getCompoDeclaration();
 		
 		/*
 		 * Initialize state information
 		 */
 		
 		this.stateHolder		= null;
 		this.stateProperty		= null;
 		this.state				= null;
 		
 		if (declaration.getStateProperty() != null) {
 			
 			PropertyDefinition.Reference propertyReference = declaration.getStateProperty();
 
 			/*
 			 * Get the component that defines the property, notice that this may deploy the implementation if not yet installed
 			 */
 			Component implementation = CST.apamResolver.findComponentByName(composite.getMainInst(),propertyReference.getDeclaringComponent().getName());
 			
 			/*
 			 * In case the implementation providing the state is not available signal an error. 
 			 * 
 			 */
 			if (implementation == null || ! (implementation instanceof Implementation)) {
 				throw new InvalidConfiguration("Invalid state declaration, implementation can not be found "+propertyReference.getDeclaringComponent().getName());
 			}
 
 			/*
 			 * Eagerly instantiate an instance to hold the state.
 			 *
 			 * In case the main instance can be used to hold state we avoid creating additional objects.
 			 */
 			if (composite.getMainInst().getImpl().equals(implementation)) {
 				this.stateHolder 	= composite.getMainInst();
 			}
 			else
 				this.stateHolder	= ((Implementation)implementation).createInstance(composite, null);
 			
 			/*
 			 * Get the property used to handle the state
 			 */
 			PropertyDefinition propertyDefinition = implementation.getDeclaration().getPropertyDefinition(propertyReference);
 			
 			/*
 			 * In case the property providing the state is not defined signal an error. 
 			 * 
 			 */
 			if (propertyDefinition == null ) {
 				throw new InvalidConfiguration("Invalid state declaration, property not defined "+propertyReference.getIdentifier());
 			}
 			
 			this.stateProperty	= propertyDefinition.getName();
 			
 			/*
 			 * compute the initial state of the composite
 			 */
 			this.state	= this.stateHolder.getProperty(this.stateProperty);
 
 		}
 
 
 		/*
 		 * Initialize the list of dynamic dependencies
 		 */
 		dynamicDependencies	= new ArrayList<DynamicResolutionRequest>();
 
 		/*
 		 * Initialize the list of dynamic contains
 		 */
 		dynamicContains	= new ArrayList<InstanceDeclaration>();
 		
 		for (InstanceDeclaration instanceDeclaration : declaration.getInstanceDeclarations()) {
 			
 			Implementation implementation = CST.apamResolver.findImplByName(composite.getMainInst(),instanceDeclaration.getImplementation().getName());			
 			
 			if (implementation == null || ! (implementation instanceof Implementation)) {
 				throw new InvalidConfiguration("Invalid instance declaration, implementation can not be found "+instanceDeclaration.getImplementation().getName());
 			}
 
 			dynamicContains.add(instanceDeclaration);
 		}
 		
 		/*
 		 * Initialize ownership information
 		 */
 		owned 			= new HashMap<OwnedComponentDeclaration, Set<Instance>>();
 		granted			= new HashMap<OwnedComponentDeclaration, GrantDeclaration>();
 		
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			
 			/*
 			 * No instances are initially owned
 			 */
 			owned.put(ownedDeclaration, new HashSet<Instance>());
 			
 			/*
 			 * Compute the current grant based on the initial state
 			 */
 			for (GrantDeclaration grant : ownedDeclaration.getGrants()) {
 				if (state != null && grant.getStates().contains(state))
 					granted.put(ownedDeclaration, grant);
 			}
 		}
 		
 		/*
 		 * Initialize the list of waiting resolutions
 		 */
 		waitingResolutions	= new ArrayList<PendingRequest>();
 		pendingGrants		= new HashMap<GrantDeclaration, List<PendingRequest>>();
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			for (GrantDeclaration grant : ownedDeclaration.getGrants()) {
 				pendingGrants.put(grant, new ArrayList<PendingRequest>());
 			}
 		}
 		
 		/*
 		 * Trigger an initial update of dynamic containment and instances
 		 */
 		for (Instance contained : composite.getContainInsts()) {
 			updateDynamicDependencies(contained);
 		}
 		
 		updateContainementTriggers();
 	}
 	
 	/**
 	 * The composite managed by this content manager
 	 */
 	public Composite getComposite() {
 		return composite;
 	}
 
 	/**
 	 * Updates the list of dynamic dependencies when a new instance is added to the composite
 	 */
 	private void updateDynamicDependencies(Instance instance) {
 		
 		assert instance.getComposite().equals(getComposite());
 		
 		for (DependencyDeclaration dependency : Util.computeAllEffectiveDependency(instance)) {
 
 			boolean hasField =  false;
 			for (DependencyInjection injection : dependency.getInjections()) {
 				if (injection instanceof DependencyInjection.Field) {
 					hasField = true;
 					break;
 				}
 			}
 			
 //			if (! hasField || dependency.isMultiple() || dependency.isEager())
 //				dynamicDependencies.add(new DynamicResolutionRequest(CST.apamResolver,instance,dependency));
 			if (! hasField || dependency.isMultiple() || dependency.isEffectiveEager()) {
 				DynamicResolutionRequest dynamicRequest = new DynamicResolutionRequest(CST.apamResolver,instance,dependency);
 				dynamicDependencies.add(dynamicRequest);
 
 				/*
 				 * Force initial resolution of eager dependency
 				 */
 				if (dependency.isEffectiveEager())
 					dynamicRequest.resolve();
 			}			
 			
 			
 		}
 	}
 
 	/**
 	 * Verifies if the triggering conditions of pending dynamic contained instances are satisfied
 	 */
 	private synchronized void updateContainementTriggers() {
 		
 		/*
 		 * Iterate over all pending dynamic instances 
 		 */
 		
 		List<InstanceDeclaration> pendingInstances = new ArrayList<InstanceDeclaration>(this.dynamicContains);
 		
 		for (InstanceDeclaration pendingInstance : pendingInstances) {
 			
 			/*
 			 * verify if all triggering conditions are satisfied
 			 */
 			boolean satisfied = true;
 			for (ConstrainedReference trigger : pendingInstance.getTriggers()) {
 				
 				/*
 				 * evaluate the specified trigger
 				 */
 				boolean satisfiedTrigger = false;
 				for (Instance candidate : getComposite().getContainInsts()) {
 
 					/*
 					 * ignore non matching candidates
 					 */
 					
 					String target = trigger.getTarget().getName();
 					
 					if (trigger.getTarget() instanceof SpecificationReference && !candidate.getSpec().getName().equals(target))
 						continue;
 
 					if (trigger.getTarget() instanceof ImplementationReference<?> && !candidate.getImpl().getName().equals(target))
 						continue;
 
 					if (!candidate.match(Util.toFilter(trigger.getInstanceConstraints())))
 						continue;
 
 					if (!candidate.getImpl().match(	Util.toFilter(trigger.getImplementationConstraints())))
 						continue;
 
 					/*
 					 * Stop evaluation at first match 
 					 */
 					satisfiedTrigger = true;
 					break;
 				}
 				
 				/*
 				 * stop at the first unsatisfied trigger
 				 */
 				if (! satisfiedTrigger) {
 					satisfied = false;
 					break;
 					
 				}
 			}
 			
 			/*
 			 * If triggering conditions are not satisfied, just keep it in the list of pending instances  
 			 */
 			if (! satisfied)
 				continue;
 			
 			/*
 			 * Remove from the list of pending dynamic containment
 			 */
 			dynamicContains.remove(pendingInstance);
 
 			/*
 			 * Otherwise try to instantiate the specified implementation.
 			 * 
 			 * TODO BUG We are initializing the properties of the instance, but we lost the dependency overrides. We need to
 			 * modify the API to allow specifying explicitly an instance declaration for Implementation.craeteInstance.
 			 */
 			Implementation implementation = CST.apamResolver.findImplByName(composite.getMainInst(),pendingInstance.getImplementation().getName());			
 			implementation.createInstance(getComposite(), pendingInstance.getProperties());
 
 		}
 	}
 	
 	/**
 	 * Handle state changes in the composite
 	 */
 	private  void stateChanged(String newState) {
 		
 		/*
 		 * Change state
 		 */
 		this.state	= newState;
 		
 		/*
 		 * Reevaluate grants
 		 */
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			
 			/*
 			 * If the current grant is still valid there is nothing to do
 			 */
 			GrantDeclaration previuos = granted.get(ownedDeclaration);
 			if (previuos != null && previuos.getStates().contains(this.state))
 				continue;
 			
 			/*
 			 * Check if another grant is activated
 			 */
 			for (GrantDeclaration grant : ownedDeclaration.getGrants()) {
 				if (grant.getStates().contains(this.state))
 					preempt(ownedDeclaration,grant);
 			}
 			
 		}
 	}
 
 	
 	/**
 	 * Preempt access to the owned instances from their current clients, and give it to the
 	 * specified grant
 	 */
 	private void preempt(OwnedComponentDeclaration ownedDeclaration, GrantDeclaration newGrant) {
 		
 		/*
 		 * change current grant
 		 */
 		granted.put(ownedDeclaration, newGrant);
 
 		/*
 		 * preempt all owned instances
 		 */
 		for (Instance ownedInstance : owned.get(ownedDeclaration)) {
 			preempt(ownedInstance, newGrant);
 		}
 		
 		
 	}
 	
 	/**
 	 * Preempt access to the specified instance from their current clients and give it
 	 * to the specified grant
 	 * 
 	 * TODO IMPORTANT BUG If the old clients are concurrently resolved again (after the
 	 * revoke but before the new resolve) they can get access to the owned instance. 
 	 * 
 	 * We should find a way to make this method atomic, this requires synchronizing this
 	 * content manager and the Apam resolver.
 	 */
 	private void preempt(Instance ownedInstance,  GrantDeclaration grant) {
 
 		/*
 		 * If there is no active grant in the current state, nothing to do
 		 */
 		if (grant == null)
 			return;
 		
 		/*
 		 * If there is no pending request waiting for the grant, nothing to do
 		 */
 		if (pendingGrants.get(grant).isEmpty())
 			return;
 		
 		/*
 		 * revoke all non granted wires
 		 */
 		for (Wire incoming : ownedInstance.getInvWires()) {
 			boolean granted	= 	incoming.getSource().getImpl().getDeclaration().getDependency(grant.getDependency()) != null ||
 								incoming.getSource().getSpec().getDeclaration().getDependency(grant.getDependency()) != null;
 			if (! granted)
 				incoming.remove();
 		}
 
 		/*
 		 * try to resolve pending requests of this grant
 		 */
 		for (PendingRequest request : pendingGrants.get(grant)) {
 			if (request.isSatisfiedBy(ownedInstance))
 				request.resolve();
 		}
 
 	}
 	
 	/**
 	 * Whether the specified instance is owned by this composite
 	 */
 	public boolean owns(Instance instance) {
 	
 		if (!instance.getComposite().equals(getComposite()))
 			return false;
 		
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			if (owned.get(ownedDeclaration).contains(instance))
 				return true;
 		}
 		
 		return false;
 		
 	}
 	
 	/**
 	 * Whether this composite requests ownership of the specified instance
 	 */
 	public boolean requestOwnership(Instance instance) {
 		
 		/*
 		 * Iterate over all ownership declarations and verify if the instance matches the specified
 		 * condition
 		 */
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 
 
 			boolean matchType = false;	
 			
 			if (ownedDeclaration.getComponent() instanceof SpecificationReference)
 				matchType = instance.getSpec().getDeclaration().getReference().equals(ownedDeclaration.getComponent());
 			
 			if (ownedDeclaration.getComponent() instanceof ImplementationReference<?>)
 				matchType = instance.getImpl().getDeclaration().getReference().equals(ownedDeclaration.getComponent());
 			
 			String propertyValue 	= instance.getProperty(ownedDeclaration.getProperty().getIdentifier());
 			boolean matchProperty	= propertyValue != null && ownedDeclaration.getValues().contains(propertyValue);
 			
 			if (matchType && matchProperty)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Accord ownership of the specified instance to this composite 
 	 */
 	public void grantOwnership(Instance instance) {
 		
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 
 			/*
 			 * find matching declaration
 			 */
 			boolean matchType = false;	
 			
 			if (ownedDeclaration.getComponent() instanceof SpecificationReference)
 				matchType = instance.getSpec().getDeclaration().getReference().equals(ownedDeclaration.getComponent());
 			
 			if (ownedDeclaration.getComponent() instanceof ImplementationReference<?>)
 				matchType = instance.getImpl().getDeclaration().getReference().equals(ownedDeclaration.getComponent());
 			
 			if (!matchType)
 				continue;
 			
 			String propertyValue 	= instance.getProperty(ownedDeclaration.getProperty().getIdentifier());
 			boolean matchProperty	= propertyValue != null && ownedDeclaration.getValues().contains(propertyValue);
 			
 			if (!matchProperty)
 				continue;
 
 			/*
 			 * get ownership
 			 */
 			((InstanceImpl)instance).setOwner(getComposite());
 			owned.get(ownedDeclaration).add(instance);
 			
 			/*
 			 * Force recalculation of dependencies that may have been invalidated by the ownership change
 			 * 
 			 */
 			for (Wire incoming : instance.getInvWires()) {
 				if (! Util.checkInstVisible(incoming.getSource().getComposite(),instance))
 					incoming.remove();
 			}
 			
 			/*
 			 * preempt previous users of the instance and give access to granted waiting requests
 			 */
 			preempt(instance, granted.get(ownedDeclaration));
 			
 		}			
 	}
 
 	/**
 	 * Revokes ownership of the specified instance
 	 * 
 	 */
 	public void revokeOwnership(Instance instance) {
 		
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			owned.get(ownedDeclaration).remove(instance);
 		}
 
 		((InstanceImpl) instance).setOwner(CompositeImpl.getRootAllComposites());
 	}
 
 
 	/**
 	 * Try to resolve all the pending requests that are potentially satisfied by a given component
 	 */
 	private void resolveRequestsWaitingFor(Component candidate) {
 		for (PendingRequest request : waitingResolutions) {
 			if (request.isSatisfiedBy(candidate))
 				request.resolve();
 		}
 	}
 	
 	/**
 	 * Try to resolve all the dynamic requests that are potentially satisfied by a given instance
 	 */
 	private void resolveDynamicRequests(Instance candidate) {
 		for (DynamicResolutionRequest request : dynamicDependencies) {
 			if (request.isSatisfiedBy(candidate))
 				request.resolve();
 		}
 	}
 
 	
 	/**
 	 * Updates the contents of this composite when a new implementation is added in APAM
 	 */
 	public synchronized void implementationAdded(Implementation implementation) {
 		
 		/*
 		 * verify if the new implementation satisfies any pending resolutions in
 		 * this composite
 		 */
 		if (Util.checkImplVisible(getComposite().getCompType(),implementation))
 			resolveRequestsWaitingFor(implementation);
 	}
 	
 	
 	/**
 	 * Updates the contents of this composite when a new instance is added in APAM
 	 */
 	public synchronized void instanceAdded(Instance instance) {
 		
 		/*
 		 * verify if the new instance satisfies any pending resolutions in this composite
 		 */
 		if (instance.isSharable() && Util.checkInstVisible(getComposite(),instance)) {
 			resolveRequestsWaitingFor(instance);
 			resolveDynamicRequests(instance);
 		}
 
 		/*
 		 * verify if a newly contained instance has dynamic dependencies or satisfies a trigger
 		 */
 		if ( instance.getComposite().equals(getComposite())) {
 			updateDynamicDependencies(instance);
 			updateContainementTriggers();
 		}
 	}
 
 	/**
 	 * Verifies all the effects of a property change in a contained instance
 	 * 
 	 */
 	public synchronized void propertyChanged(Instance instance, String property) {
 
 		/*
 		 * For modified contained instances
 		 */
 		if ( instance.getComposite().equals(getComposite())) {
 			
 			/*
 			 * update triggers
 			 */
 			updateContainementTriggers();
 			
 			/*
 			 * Force recalculation of dependencies that may have been invalidated by
 			 * the property change
 			 * 
 			 */
 			for (Wire incoming : instance.getInvWires()) {
 				if (incoming.hasConstraints())
 					incoming.remove();
 			}
 
 			/*
 			 * Verify if property change triggers a state change
 			 */
 			if (stateHolder != null && stateHolder.equals(instance) && stateProperty.equals(property))
 				stateChanged(stateHolder.getProperty(stateProperty));
 
 		}
 
 		/*
 		 * verify if the modified instance satisfies any pending resolutions and dynamic
 		 * dependencies in this composite
 		 */
 		if (instance.isSharable() && Util.checkInstVisible(getComposite(),instance)) {
 			resolveRequestsWaitingFor(instance);
 	        resolveDynamicRequests(instance);
 		}
 
 	}
 
 
 	/**
 	 * Updates the contents of this composite when a contained instance is removed from APAM
 	 * 
 	 */
 	public synchronized void instanceRemoved(Instance instance) {
 		
 		assert instance.getComposite().equals(getComposite());
 
 		/*
 		 * update state
 		 */
 		if (instance == stateHolder)
 			stateHolder = null;
 		
 		/*
 		 * update list of owned instances
 		 */
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			if (owned.get(ownedDeclaration).contains(instance))
 				owned.get(ownedDeclaration).remove(instance);
 		}
 		
 		/*
 		 * update list of dynamic dependencies
 		 */
 		List<DynamicResolutionRequest> removedDynamicRequests = new ArrayList<DynamicResolutionRequest>();
 		for (DynamicResolutionRequest dynamicDependency : dynamicDependencies) {
 			if (dynamicDependency.getSource().equals(instance))
 				removedDynamicRequests.add(dynamicDependency);
 		}
 		
 		dynamicDependencies.removeAll(removedDynamicRequests);
 		
 		/*
 		 * update list of waiting requests
 		 */
 		List<PendingRequest> removedWaitingResolutions = new ArrayList<PendingRequest>();
 		for (PendingRequest pendingRequest : waitingResolutions) {
 			if (pendingRequest.getSource().equals(instance))
 				removedWaitingResolutions.add(pendingRequest);
 		}
 		
 		waitingResolutions.removeAll(removedWaitingResolutions);
 		
 	}
 	
 	/**
 	 * Updates the contents of this composite when a wire is removed from APAM.
 	 * 
 	 * If the target of the wire is a non sharable instance, the released instance can
 	 * potentially be used by a pending requests.
 	 * 
 	 */
 	public synchronized void wireRemoved(Wire wire) {
 		Instance instance = wire.getDestination();
 		if (instance.isSharable() && Util.checkInstVisible(getComposite(),instance))
 			resolveRequestsWaitingFor(instance);
 	}
 	
 	/**
 	 * Add a new pending request in the content of the composite
 	 */
 	public synchronized void addPendingRequest(PendingRequest request) {
 		
 		/*
 		 * add to the list of pending requests
 		 */
 		waitingResolutions.add(request);
 		
 		/*
 		 * Verify if the request corresponds to a grant for an owned instance, and
 		 * index it to accelerate scheduling of grants
 		 */
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			for (GrantDeclaration grant :ownedDeclaration.getGrants()) {
 				
 				/*
 				 * TODO BUG This test may fail if the resolution dependency is defined in the
 				 * specification or refined in the instance. Dependency declaration equality
 				 * is defined based on the name of the dependency PLUS the defining component.
 				 * 
 				 * Comparing only the name of the dependency is not correct. Because it is 
 				 * possible, and frequent, to have dependencies with the same name in different
 				 * implementations or specifications.
 				 * 
 				 * The right test will be to get the actual implementation of the  source instance
 				 * of the resolution and compare it to grant.getDependency().getDeclaringComponent(),
 				 * but this information currently is not available in the dependency manager API 
				 * 
				 * TODO BUG if the grant is currently active preempt any non granted wires
 				 */
 				if (request.getDependency().equals(grant.getDependency()))
 					pendingGrants.get(grant).add(request);
 			}
 		}
 	}
 	
 	/**
 	 * Remove a pending request from the content of the composite
 	 */
 	public synchronized void removePendingRequest(PendingRequest request) {
 
 		waitingResolutions.remove(request);
 
 		for (OwnedComponentDeclaration ownedDeclaration : declaration.getOwnedComponents()) {
 			for (GrantDeclaration grant :ownedDeclaration.getGrants()) {
 				pendingGrants.get(grant).remove(request);
 			}
 		}
 		
 	}
 
 	
 	/**
 	 * The composite is removed
 	 */
 	public void dispose() {
 	}
 
 
 }
