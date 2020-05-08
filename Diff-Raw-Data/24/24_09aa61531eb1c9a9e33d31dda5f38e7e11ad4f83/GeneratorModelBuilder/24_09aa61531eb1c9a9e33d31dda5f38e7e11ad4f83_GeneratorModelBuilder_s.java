 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.builder;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.BindingEndPoint;
 import org.eclipse.etrice.core.room.ExternalPort;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.ProtocolClass;
 import org.eclipse.etrice.core.room.RefSAPoint;
 import org.eclipse.etrice.core.room.RelaySAPoint;
 import org.eclipse.etrice.core.room.RoomModel;
 import org.eclipse.etrice.core.room.RoomPackage;
 import org.eclipse.etrice.core.room.SAPRef;
 import org.eclipse.etrice.core.room.SAPoint;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.SPPoint;
 import org.eclipse.etrice.core.room.ServiceImplementation;
 import org.eclipse.etrice.core.room.SubSystemClass;
 import org.eclipse.etrice.core.room.SubSystemRef;
 import org.eclipse.etrice.generator.base.ILogger;
 import org.eclipse.etrice.generator.etricegen.ActorInstance;
 import org.eclipse.etrice.generator.etricegen.BindingInstance;
 import org.eclipse.etrice.generator.etricegen.ConnectionInstance;
 import org.eclipse.etrice.generator.etricegen.Counter;
 import org.eclipse.etrice.generator.etricegen.ETriceGenFactory;
 import org.eclipse.etrice.generator.etricegen.ExpandedActorClass;
 import org.eclipse.etrice.generator.etricegen.IDiagnostician;
 import org.eclipse.etrice.generator.etricegen.PortInstance;
 import org.eclipse.etrice.generator.etricegen.PortKind;
 import org.eclipse.etrice.generator.etricegen.Root;
 import org.eclipse.etrice.generator.etricegen.SAPInstance;
 import org.eclipse.etrice.generator.etricegen.SPPInstance;
 import org.eclipse.etrice.generator.etricegen.ServiceImplInstance;
 import org.eclipse.etrice.generator.etricegen.StructureInstance;
 import org.eclipse.etrice.generator.etricegen.SubSystemInstance;
 import org.eclipse.etrice.generator.etricegen.impl.StructureInstanceImpl;
 
 /**
  * A class for the creation of an intermediate model combining all information needed by
  * the code generator.
  * 
  * @author Henrik Rentz-Reichert
  *
  */
 public class GeneratorModelBuilder {
 
 	/**
 	 * the first object id used for the {@link org.eclipse.etrice.core.etrice.runtime.messaging.Address Address}es s of runtime instances
 	 */
 	private static final int OBJ_ID_OFFSET = 100;
 	
 	/**
 	 * a set containing all relay ports for fast frequent access to this information
 	 */
 	private HashSet<Port> relayPorts = new HashSet<Port>();
 	
 	/**
 	 * an instance of a logger
 	 */
 	private ILogger logger;
 	
 	/**
 	 * an isntance of a diagnostician
 	 */
 	private IDiagnostician diagnostician;
 
 	/**
 	 * the only constructor takes a logger and a diagnostician as arguments
 	 * @param logger
 	 * @param diagnostician
 	 */
 	public GeneratorModelBuilder(ILogger logger, IDiagnostician diagnostician) {
 		this.logger = logger;
 		this.diagnostician = diagnostician;
 	}
 	
 	// TODOHRR: combine a RoomProject (.room_proj) with RoomModels (.room)
 	
 	/**
 	 * Creates a model of all instances for all sub systems.
 	 * Actor instances are created in a hierarchical structure.
 	 * There is only one list of port per actor instances.
 	 * Ports have a type attribute (relay, intern, external).
 	 * Bindings connect port instances. And since ports are
 	 * instances it is possible to have pointers back to the
 	 * bindings.
 	 * After creating the instance tree ports are connected to
 	 * their peers.
 	 * Similar SAPs, Services and layer connections are treated.
 	 * Finally expanded (xp) actor classes are created which
 	 * contain also inherited state graph items and where RefinedStates
 	 * are removed and their contents is relocated.
 	 * 
 	 * @param models
 	 * @return the root of the newly created instance model
 	 */
 	public Root createGeneratorModel(List<RoomModel> models, boolean asLibrary) {
 		Root root = ETriceGenFactory.eINSTANCE.createRoot();
 		root.getModels().addAll(models);
 		root.setLibrary(asLibrary);
 
 		if (!root.isLibrary()) {
 			// create instance model
 			
 			determineRelayPorts(root);
 			
 			for (SubSystemClass comp : root.getMainPathSubSystemClasses()) {
 				root.getSubSystemInstances().add(createSubSystemInstance(comp));
 			}
 			
 			connectPorts(root);
 			checkPortMultiplicity(root);
 			
 			connectServices(root);
 		}
 		
 		// transform actor classes
 		createExpandedActorClasses(root);
 		
 		return root;
 	}
 
 	/**
 	 * Connect all services hierarchically. This finally connects SAPs with corresponding services
 	 * according to layer connections.
 	 * 
 	 * @param root
 	 */
 	private void connectServices(Root root) {
 		createServiceMappings(root);
 		bindSAPs(root);
 	}
 
 	/**
 	 * Hierarchically create service mappings at each starting point of layer connections (root level).
 	 * 
 	 * @param root
 	 */
 	private void createServiceMappings(Root root) {
 		for (SubSystemInstance comp : root.getSubSystemInstances()) {
 			createServiceMappings(comp);
 		}
 	}
 	
 	/**
 	 * Hierarchically create service mappings at each starting point of layer connections (recursively for all structure instances).
 	 * 
 	 * @see createServiceMappings
 	 * @param si
 	 */
 	private void createServiceMappings(StructureInstance si) {
 		for (ConnectionInstance ci : si.getConnections()) {
 			if (ci.getFromSPP()==null) {
 				// this connection originates at an actor instance
 				addService(ci.getFromAI(), ci);
 			}
 			else if (ci.getFromSPP().getIncoming().isEmpty()) {
 				// this connection originates at an SPP instance which is not connected outside
 				addService(si, ci);
 			}
 		}
 		
 		// recursive call for all children
 		for (ActorInstance child : si.getInstances()) {
 			createServiceMappings(child);
 		}
 	}
 
 	/**
 	 * Determines a connected services and attaches it to the protocol2service mapping
 	 * of a structure instance
 	 * 
 	 * @param si
 	 * @param ci
 	 */
 	private void addService(StructureInstance si, ConnectionInstance ci) {
 		assert(si instanceof StructureInstanceImpl): "unknown implementation "+si.eClass().getName();
 		StructureInstanceImpl sii = (StructureInstanceImpl) si;
 		
 		ProtocolClass pc = ci.getToSPP().getSpp().getProtocol();
 		if (sii.protocol2service.get(pc)!=null) {
 			
 			// this protocol is already handled on this level
 			
 			EObject obj = null;
 			if (si instanceof ActorInstance)
 				obj = ((ActorInstance)si).getActorClass();
 			else if (si instanceof SubSystemInstance)
 				obj = ((SubSystemInstance)si).getSubSystemClass();
 			else
 				obj = si;
 			diagnostician.error("A service can only be offered once per actor instance, consider pushing one down to a contained actor!", obj, RoomPackage.eINSTANCE.getActorContainerClass_IfSPPs());
 		}
 		else {
 			if (ci.getFromSPP()!=null && ci.getFromSPP().getSpp().getProtocol()!=pc) {
 				diagnostician.error("Layer connection must connect same protocols!", ci.getConnection(), RoomPackage.eINSTANCE.getLayerConnection_From());
 			}
 			else {
 				// now we follow the layer connections
 				while(true) {
 					SPPInstance sppi = ci.getToSPP();
 					if (sppi.getOutgoing()==null) {
 						// we reached the end, find the ServiceImplementation
 						boolean found = false;
 						if (sppi.eContainer() instanceof ActorInstance) {
 							ActorInstance implementor = (ActorInstance) sppi.eContainer();
 							for (ServiceImplInstance svc : implementor.getServices()) {
 								if (svc.getSvcImpl().getSpp()==sppi.getSpp()) {
 									found = true;
 									sii.protocol2service.put(pc, svc);
 								}
 							}
 						}
 						else {
 							assert(false);
 						}
 						if (!found) {
 							ActorContainerClass acr = (ActorContainerClass) sppi.getSpp().eContainer();
 							int idx = acr.getIfSPPs().indexOf(sppi.getSpp());
 							diagnostician.error("An SPP mus be connected by a layer connection or implemented by a ServiceImplementation!", sppi.getSpp(), RoomPackage.eINSTANCE.getActorContainerClass_IfSPPs(), idx);
 						}
 						return;
 					}
 					else {
 						ci = sppi.getOutgoing();
 						if (ci.getToSPP().getSpp().getProtocol()!=pc) {
 							diagnostician.error("Layer connection must connect same protocols!", ci.getConnection(), RoomPackage.eINSTANCE.getLayerConnection_From());
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Connect a SAP to its service (root level)
 	 * 
 	 * @param root
 	 */
 	private void bindSAPs(Root root) {
 		for (SubSystemInstance comp : root.getSubSystemInstances()) {
 			bindSAPs(comp);
 			setServiceObjectIDs(comp, comp.getObjCounter());
 		}
 	}
 
 	/**
 	 * Connect a SAP to its service (recursively for all structure instances).
 	 * 
 	 * @param si
 	 */
 	private void bindSAPs(StructureInstance si) {
 		for (SAPInstance sap : si.getSaps()) {
 			bindSAP(si, sap);
 		}
 
 		// recursive call for all children
 		for (ActorInstance child : si.getInstances()) {
 			bindSAPs(child);
 		}
 	}
 
 	/**
 	 * Do the actual binding of a SAP.
 	 * 
 	 * @param si
 	 * @param sap
 	 */
 	private void bindSAP(StructureInstance si, SAPInstance sap) {
 		assert(si instanceof StructureInstanceImpl);
 		StructureInstanceImpl sii = (StructureInstanceImpl) si;
 		
 		// walk up the container hierarchy until the sap is satisfied
 		do {
 			ServiceImplInstance svc = sii.protocol2service.get(sap.getSap().getProtocol());
 			if (svc!=null) {
 				sap.getPeers().add(svc);
 				svc.getPeers().add(sap);
 				return;
 			}
 			if (sii.eContainer() instanceof StructureInstanceImpl)
 				sii = (StructureInstanceImpl) sii.eContainer();
 			else
 				sii = null;
 		}
 		while (sii!=null);
 		
 		ActorClass ac = (ActorClass) sap.getSap().eContainer();
 		int idx = ac.getStrSAPs().indexOf(sap.getSap());
		diagnostician.error("SAP not satisfied!", sap.getSap(), RoomPackage.eINSTANCE.getActorClass_StrSAPs(), idx);
 	}
 
 	/**
 	 * Finally recursively the Address object IDs are calculated and assigned
 	 * @param si
 	 * @param counter
 	 */
 	private void setServiceObjectIDs(StructureInstance si, Counter counter) {
 		for (ServiceImplInstance svc : si.getServices()) {
 			svc.setObjId(counter.getAndIncrementCount(svc.getPeers().size()));
 		}
 		
 		// recursive call for all children
 		for (ActorInstance child : si.getInstances()) {
 			setServiceObjectIDs(child, counter);
 		}
 	}
 
 	/**
 	 * for efficiency reasons we create a set holding all relay ports
 	 * @param root - the root object
 	 */
 	private void determineRelayPorts(Root root) {
 		for (RoomModel model : root.getModels()) {
 			for (ActorClass ac : model.getActorClasses()) {
 				for (Port port : ac.getIfPorts()) {
 					boolean external = false;
 					for (ExternalPort ep : ac.getExtPorts()) {
 						if (ep.getIfport()==port) {
 							external = true;
 							break;
 						}
 					}
 					if (!external) {
 						relayPorts.add(port);
 						
 						// check whether relay port is multiply connected
 						int count = 0;
 						for (Binding b : ac.getBindings()) {
 							if (b.getEndpoint1().getPort()==port)
 								++count;
 							if (b.getEndpoint2().getPort()==port)
 								++count;
 						}
 						if (count>1) {
 							int idx = ac.getIfPorts().indexOf(port);
 							diagnostician.error("relay port is multiply connected inside its actor class", port, RoomPackage.eINSTANCE.getActorClass_IfPorts(), idx);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * hierarchically (i.e. recursively) creates all instances implied by this component
 	 * @param comp - the component class
 	 * @return the newly created hierarchy of instances
 	 */
 	private SubSystemInstance createSubSystemInstance(SubSystemClass comp) {
 		logger.logInfo("GeneratorModelBuilder: creating component instance from "+comp.getName());
 
 		SubSystemInstance instance = ETriceGenFactory.eINSTANCE.createSubSystemInstance();
 		
 		instance.setName(comp.getName());
 		Counter objCounter = ETriceGenFactory.eINSTANCE.createCounter();
 		objCounter.setCounter(OBJ_ID_OFFSET);
 		instance.setObjCounter(objCounter);
 		instance.setObjId(objCounter.getAndIncrementCount());
 		instance.setSubSystemClass(comp);
 		
 		// TODOHRR: enumerate object ids per thread
 		
 		for (ActorRef ar : comp.getActorRefs()) {
 			instance.getInstances().add(recursivelyCreateActorInstances(objCounter, ar));
 		}
 		
 		// bindings are handled now since port instances of sub-actor instances are available
 		createBindingInstances(instance, comp.getBindings());
 		createConnectionInstances(instance, comp.getConnections());
 		
 		return instance;
 	}
 	
 	/**
 	 * hierarchically (i.e. recursively) creates all instances implied by this actor
 	 * @param instance - the root component instance
 	 * @param aref - create the instance sub-tree of this actor reference
 	 * @return the newly created actor instance
 	 */
 	private ActorInstance recursivelyCreateActorInstances(Counter objCounter, ActorRef aref) {
 		logger.logInfo("GeneratorModelBuilder: creating actor instance "+aref.getName()+" from "+aref.getType().getName());
 
 		ActorInstance ai = ETriceGenFactory.eINSTANCE.createActorInstance();
 		
 		ai.setName(aref.getName());
 		ai.setObjId(objCounter.getAndIncrementCount());
 		ActorClass ac = aref.getType();
 		ai.setActorClass(ac);
 
 		// create a list of super classes, super first, sub-classes last
 		LinkedList<ActorClass> classes = new LinkedList<ActorClass>();
 		classes.addFirst(ac);
 		while (ac.getBase()!=null) {
 			ac = ac.getBase();
 			classes.addFirst(ac);
 		}
 		
 		// create instances for super classes recursively (ports, actor refs and bindings)
 		// super classes first ensures that actor refs are present when bindings are created
 		for (ActorClass acl : classes) {
 			// first we add our port instances to have them numbered subsequently
 			createPortInstances(objCounter, ai, acl);
 			createServiceRelatedInstances(objCounter, ai, acl);
 			
 			// recurse down into sub-actors
 			for (ActorRef ar : acl.getActorRefs()) {
 				ai.getInstances().add(recursivelyCreateActorInstances(objCounter, ar));
 			}
 			
 		}
 		for (ActorClass acl : classes) {
 			// bindings are handled now since port instances of sub-actor instances are available
 			createBindingInstances(ai, acl.getBindings());
 			createConnectionInstances(ai, acl.getConnections());
 		}
 		
 		return ai;
 	}
 
 	/**
 	 * create port instances for every kind of port
 	 * @param objCounter - for unique object id used for address creation
 	 * @param ai - the currently considered actor instance
 	 * @param ac - the actor class (might be a base class)
 	 */
 	private void createPortInstances(Counter objCounter, ActorInstance ai,
 			ActorClass ac) {
 		for (ExternalPort port : ac.getExtPorts()) {
 			PortInstance pi = ETriceGenFactory.eINSTANCE.createPortInstance();
 			
 			pi.setName(port.getIfport().getName());
 			// replicated ports have subsequent object IDs
 			pi.setObjId(objCounter.getAndIncrementCount(port.getIfport().getMultiplicity()));
 			pi.setPort(port.getIfport());
 			pi.setKind(PortKind.EXTERNAL);
 			
 			ai.getPorts().add(pi);
 		}
 		for (Port port : ac.getIntPorts()) {
 			PortInstance pi = ETriceGenFactory.eINSTANCE.createPortInstance();
 			
 			pi.setName(port.getName());
 			// replicated ports have subsequent object IDs
 			pi.setObjId(objCounter.getAndIncrementCount(port.getMultiplicity()));
 			pi.setPort(port);
 			pi.setKind(PortKind.INTERNAL);
 			
 			ai.getPorts().add(pi);
 		}
 		for (Port port : ac.getIfPorts()) {
 			if (relayPorts.contains(port)) {
 				PortInstance pi = ETriceGenFactory.eINSTANCE.createPortInstance();
 				
 				pi.setName(port.getName());
 				// relay ports are not instantiated and thus have no object ID
 				//pi.setObjId(instance.getAndIncrementObjCount());
 				pi.setPort(port);
 				pi.setKind(PortKind.RELAY);
 				
 				ai.getPorts().add(pi);
 			}
 		}
 	}
 	
 	/**
 	 * create sap, spp and service instances
 	 * @param objCounter - for unique object id used for address creation
 	 * @param ai - the currently considered actor instance
 	 * @param ac - the actor class (might be a base class)
 	 */
 	private void createServiceRelatedInstances(Counter objCounter, ActorInstance ai,
 			ActorClass ac) {
 		for (SAPRef sap : ac.getStrSAPs()) {
 			SAPInstance si = ETriceGenFactory.eINSTANCE.createSAPInstance();
 			si.setName(sap.getName());
 			si.setObjId(objCounter.getAndIncrementCount());
 			si.setSap(sap);
 			
 			ai.getSaps().add(si);
 		}
 		for (SPPRef sap : ac.getIfSPPs()) {
 			SPPInstance si = ETriceGenFactory.eINSTANCE.createSPPInstance();
 			si.setName(sap.getName());
 			// SPPs are not instantiated and thus need no object ID
 			//si.setObjId(objCounter.getAndIncrementCount());
 			si.setSpp(sap);
 			
 			ai.getSpps().add(si);
 		}
 		for (ServiceImplementation svcimpl : ac.getServiceImplementations()) {
 			ServiceImplInstance sii = ETriceGenFactory.eINSTANCE.createServiceImplInstance();
 			sii.setName(svcimpl.getSpp().getName());
 			//will set the object ID later when we know all connected saps
 			//sii.setObjId(objCounter.getAndIncrementCount());
 			sii.setSvcImpl(svcimpl);
 			
 			ai.getServices().add(sii);
 		}
 	}
 	
 	/**
 	 * create binding instances. Since bindings connect port instances the ports can point back to their bindings
 	 * using EOpposite
 	 * @param ai - create bindings for this actor instance
 	 * @param bindings - a list of bindings
 	 */
 	private void createBindingInstances(StructureInstance ai, EList<Binding> bindings) {
 		for (Binding bind : bindings) {
 			BindingInstance bi = ETriceGenFactory.eINSTANCE.createBindingInstance();
 			
 			if (bind.getEndpoint1().getActorRef()==null && bind.getEndpoint2().getActorRef()!=null) {
 				bi.getPorts().add(getPortInstance(ai, bind.getEndpoint1()));
 				bi.getPorts().add(getPortInstance(ai, bind.getEndpoint2()));
 			}
 			else if (bind.getEndpoint1().getActorRef()!=null && bind.getEndpoint2().getActorRef()==null) {
 				bi.getPorts().add(getPortInstance(ai, bind.getEndpoint2()));
 				bi.getPorts().add(getPortInstance(ai, bind.getEndpoint1()));
 			}
 			else if (bind.getEndpoint1().getActorRef()!=null && bind.getEndpoint2().getActorRef()!=null) {
 				bi.getPorts().add(getPortInstance(ai, bind.getEndpoint1()));
 				bi.getPorts().add(getPortInstance(ai, bind.getEndpoint2()));
 			}
 			else {
 				int idx = bindings.indexOf(bind);
 				diagnostician.error("binding connects two ports of the same actor", bind, RoomPackage.eINSTANCE.getStructureClass_Bindings(), idx);
 			}
 			
 			ai.getBindings().add(bi);
 		}
 	}
 	
 	/**
 	 * Create layer connection instances.
 	 * @param si - create layer connections for this actor instance
 	 * @param connections - a list of layer connections
 	 */
 	private void createConnectionInstances(StructureInstance si, EList<LayerConnection> connections) {
 		for (LayerConnection lc : connections) {
 			ConnectionInstance ci = ETriceGenFactory.eINSTANCE.createConnectionInstance();
 			
 			ci.setConnection(lc);
 			
 			SAPoint from = lc.getFrom();
 			if (from instanceof RefSAPoint) {
 				if (((RefSAPoint)from).getRef() instanceof ActorRef) {
 					ActorInstance fromAI = getActorInstance(si, ((ActorRef)((RefSAPoint)from).getRef()));
 					ci.setFromAI(fromAI);
 				}
 				else {
 					//TODOHRR: handle SubSystemRef
 					System.err.println("error");
 				}
 			}
 			else if (from instanceof RelaySAPoint) {
 				SPPInstance sppi = getSPPInstance(si, null, ((RelaySAPoint)from).getRelay());
 				if (sppi.getOutgoing()!=null) {
 					ActorContainerClass acr = (ActorContainerClass) sppi.getSpp().eContainer();
 					int idx = acr.getIfSPPs().indexOf(sppi.getSpp());
 					diagnostician.error("SPPRef has several outgoing layer connections!", sppi.getSpp(), RoomPackage.eINSTANCE.getActorContainerClass_IfSPPs(), idx);
 				}
 				ci.setFromSPP(sppi);
 			}
 			else {
 				assert(false): "unknown type of "+from.eClass().getName();
 			}
 			
 			SPPoint to = lc.getTo();
 			SPPInstance sppi = getSPPInstance(si, to.getRef(), to.getService());
 			ci.setToSPP(sppi);
 			
 			si.getConnections().add(ci);
 		}
 	}
 	
 	/**
 	 * Returns the endpoint of a layer connection.
 	 * 
 	 * @param si
 	 * @param ar
 	 * @param spp
 	 * @return
 	 */
 	private SPPInstance getSPPInstance(StructureInstance si, ActorContainerRef ar, SPPRef spp) {
 		if (ar==null) {
 			for (SPPInstance sppi : si.getSpps()) {
 				if (sppi.getSpp()==spp)
 					return sppi;
 			}
 		}
 		else {
 			if (ar instanceof ActorRef) {
 				ActorInstance subai = getActorInstance(si, (ActorRef)ar);
 				if (subai!=null)
 					return getSPPInstance(subai, null, spp);
 			}
 			else if (ar instanceof SubSystemRef) {
 				// TODOHRR: handle SubSystemRef
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns an actor instances corresponding to an ActorRef.
 	 * 
 	 * @param si
 	 * @param ar
 	 * @return
 	 */
 	private ActorInstance getActorInstance(StructureInstance si, ActorRef ar) {
 		for (ActorInstance subai : si.getInstances()) {
 			if (subai.getName().equals(ar.getName())) {
 				return subai;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * get a port instance for a binding endpoint
 	 * @param si - consider this actor instance
 	 * @param be - the binding endpoint to match
 	 * @return the port instance found
 	 */
 	private PortInstance getPortInstance(StructureInstance si, BindingEndPoint be) {
 		if (be.getActorRef()==null) {
 			for (PortInstance pi : si.getPorts()) {
 				if (pi.getPort()==be.getPort()) {
 					if (pi.getKind()==PortKind.EXTERNAL) {
 						EStructuralFeature feature = (((Binding)be.eContainer()).getEndpoint1()==be)? RoomPackage.eINSTANCE.getBinding_Endpoint1()
 								: RoomPackage.eINSTANCE.getBinding_Endpoint2();
 						diagnostician.error("binding connects external end port to sub-actor interface", be.eContainer(), feature);
 					}
 					return pi;
 				}
 			}
 		}
 		else {
 			for (ActorInstance subai : si.getInstances()) {
 				if (subai.getName().equals(be.getActorRef().getName())) {
 					for (PortInstance pi : subai.getPorts()) {
 						if (pi.getPort()==be.getPort()) {
 							if (pi.getKind()==PortKind.INTERNAL) {
 								EStructuralFeature feature = (((Binding)be.eContainer()).getEndpoint1()==be)? RoomPackage.eINSTANCE.getBinding_Endpoint1()
 										: RoomPackage.eINSTANCE.getBinding_Endpoint2();
 								diagnostician.error("binding connects to sub-actor internal end port", be.eContainer(), feature);
 							}
 							return pi;
 						}
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * set peer ports in the whole instance model
 	 * @param root
 	 */
 	private void connectPorts(Root root) {
 		TreeIterator<EObject> it = root.eAllContents();
 		while (it.hasNext()) {
 			EObject obj = it.next();
 			if (obj instanceof ActorInstance) {
 				for (PortInstance pi : ((ActorInstance) obj).getPorts()) {
 					if (pi.getKind()!=PortKind.RELAY) {
 						List<PortInstance> peers = getFinalPeers(pi, null);
 						pi.getPeers().addAll(peers);
 						// we don't have to add pi to its peer.peers since we do that once we reach there
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * determine final peers of an end port
 	 * @param pi - a end port
 	 * @param from - the binding from which we reached pi or null if start
 	 * @return a list of final peer port instances (end ports themselves)
 	 */
 	private List<PortInstance> getFinalPeers(PortInstance pi, BindingInstance from) {
 		List<PortInstance> peers = new LinkedList<PortInstance>();
 		
 		for (BindingInstance bi : pi.getBindings()) {
 			if (bi==from)
 				// skip the binding where we came from
 				continue;
 			
 			if (from!=null && from.eContainer()==bi.eContainer())
 				// the container of a binding instance is a StructureInstance
 				// by this we make sure that we go from inside to outside or vice versa
 				continue;
 			
 			PortInstance end = (bi.getPorts().get(0)!=pi)? bi.getPorts().get(0) : bi.getPorts().get(1);
 			if (end.getKind()==PortKind.RELAY)
 				// continue recursion
 				peers.addAll(getFinalPeers(end, bi));
 			else
 				// this is a final peer
 				peers.add(end);
 		}
 		return peers;
 	}
 
 	/**
 	 * check that the number of peer ports does not exceed the multiplicity of a port
 	 * @param root
 	 */
 	private void checkPortMultiplicity(Root root) {
 		TreeIterator<EObject> it = root.eAllContents();
 		while (it.hasNext()) {
 			EObject obj = it.next();
 			if (obj instanceof ActorInstance) {
 				ActorInstance ai = (ActorInstance) obj;
 				for (PortInstance pi : ai.getPorts()) {
 					if (pi.getKind()!=PortKind.RELAY) {
 						if (pi.getBindings().size()>pi.getPort().getMultiplicity()) {
 							EStructuralFeature feature = null;
 							int idx = IDiagnostician.INSIGNIFICANT_INDEX;
 							if (pi.getPort().eContainer() instanceof ActorClass) {
 								ActorClass ac = (ActorClass) pi.getPort().eContainer();
 								idx = ac.getIfPorts().indexOf(pi.getPort());
 								if (idx>=0) {
 									feature = RoomPackage.eINSTANCE.getActorClass_IfPorts();
 								}
 								else {
 									feature = RoomPackage.eINSTANCE.getActorClass_IntPorts();
 									idx = ac.getIntPorts().indexOf(pi.getPort());
 								}
 							}
 							else if (pi.getPort().eContainer() instanceof SubSystemClass) {
 								SubSystemClass ssc = (SubSystemClass) pi.getPort().eContainer();
 								feature = RoomPackage.eINSTANCE.getSubSystemClass_RelayPorts();
 								idx = ssc.getRelayPorts().indexOf(pi.getPort());
 							}
 							diagnostician.error("number of peers "+pi.getBindings().size()
 									+ " of port "+pi.getName()
 									+" exceeds multiplicity "+pi.getPort().getMultiplicity()
 									+" in instance "+ai.getPath(), pi, feature, idx);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * expanded (xp) actor classes are created which
 	 * contain also inherited features and where RefinedStates
 	 * are removed and their contents is relocated.
 	 * @param root - the model root
 	 */
 	private void createExpandedActorClasses(Root root) {
 		for (ActorClass ac : root.getUsedActorClasses()) {
 			root.getXpActorClasses().add(createExpandedActorClass(ac));
 		}
 	}
 
 	/**
 	 * create an expanded actor class from the original model class
 	 * @param ac - the original model class
 	 * @return - the newly created expanded actor class
 	 */
 	private ExpandedActorClass createExpandedActorClass(ActorClass ac) {
 		logger.logInfo("GeneratorModelBuilder: creating expanded actor class from "+ac.getName()
 			+" of "+((RoomModel)ac.eContainer()).getName());
 
 		ExpandedActorClass xpac = ETriceGenFactory.eINSTANCE.createExpandedActorClass();
 		xpac.setName(ac.getName());
 		xpac.setActorClass(ac);
 		xpac.setAbstract(ac.isAbstract());
 		
 		xpac.prepare(diagnostician);
 		
 		return xpac;
 	}
 }
