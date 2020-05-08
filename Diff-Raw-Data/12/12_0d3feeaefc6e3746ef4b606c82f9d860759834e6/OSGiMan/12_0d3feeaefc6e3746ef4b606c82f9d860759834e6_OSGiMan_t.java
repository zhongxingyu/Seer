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
 package fr.imag.adele.apam.apform.legacy.osgi;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.felix.ipojo.Factory;
 import org.apache.felix.ipojo.Pojo;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.Apam;
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Dependency;
 import fr.imag.adele.apam.DependencyManager;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.ManagerModel;
 import fr.imag.adele.apam.Resolved;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.apform.Apform2Apam;
 import fr.imag.adele.apam.apform.ApformImplementation;
 import fr.imag.adele.apam.declarations.ComponentKind;
 import fr.imag.adele.apam.declarations.DependencyDeclaration;
 import fr.imag.adele.apam.declarations.InterfaceReference;
 import fr.imag.adele.apam.declarations.ResolvableReference;
 
 @Instantiate(name = "OSGiMan-Instance")
 @org.apache.felix.ipojo.annotations.Component(name = "OSGiMan" , immediate=true)
 @Provides
 public class OSGiMan implements DependencyManager {
 
 	private final static Logger	logger = LoggerFactory.getLogger(OSGiMan.class);
 
 	/**
 	 * A reference to the APAM machine
 	 */
     @SuppressWarnings("unused")
 	@Requires(proxy = false)
 	private Apam apam;
 
     /**
      * The associated OSGi context
      */
     private final BundleContext context;
     
     /**
      * The associated model
      */
     @SuppressWarnings("unused")
 	private ManagerModel model;
     
     public OSGiMan(BundleContext context) {
         this.context = context;
     }
 
 	@Override
 	public String getName() {
 		return "OSGiMan";
 	}
 
 	@Override
 	public int getPriority() {
 		return 2;
 	}
 
 	@Validate
 	private @SuppressWarnings("unused") synchronized void start()  {
 		ApamManagers.addDependencyManager(this,getPriority());
 	}
 	
 	@Invalidate
 	private  @SuppressWarnings("unused") synchronized void stop() {
 		ApamManagers.removeDependencyManager(this);
 	}
 	
     
 	@Override
 	public void newComposite(ManagerModel model, CompositeType composite) {
 		this.model = model;
 	}
 
 	@Override
 	public void getSelectionPath(Component client, Dependency dependency, List<DependencyManager> selPath) {
         selPath.add(selPath.size(), this);
 	}
 
 	@Override
 	public Resolved resolveDependency(Component client, Dependency dependency) {
 		
 		InterfaceReference target = dependency.getTarget().as(InterfaceReference.class);
 		if (target == null)
 			return null;
 		
 		Resolved resolution = null;
 		
 		/*
 		 * Get all matching OSGi services and reify them in APAM, along with their implementation
 		 */
 		try {
 			Set<Implementation> implementations = new HashSet<Implementation>();
 			Set<Instance> instances 			= new HashSet<Instance>();
 		
 			ServiceReference matchingServices[] = context.getAllServiceReferences(target.getJavaType(), null);
 			for (ServiceReference matchingService : matchingServices != null ? matchingServices : new ServiceReference[0]) {
 				
 		        /*
 		         * ignore services that are iPojo, these are treated separately
 		         */
 		        Object service = context.getService(matchingService);
 		        if (service instanceof Pojo || service instanceof Factory)
 		        	continue;
 
 				/*
 				 * Register instance in APAM
 				 */
 				ApformOSGiInstance osgiInstance = new ApformOSGiInstance(matchingService);
 				Apform2Apam.newInstance(osgiInstance);
 
 				/*
 				 * Find or create the associated implementation
 				 */
 				ApformImplementation osgiImplementation = new ApformOSGiImplementation(osgiInstance);
 				if (CST.componentBroker.getImpl(osgiImplementation.getDeclaration().getName()) == null) {
 					Apform2Apam.newImplementation(osgiImplementation);
 				}
 				
 				Component implementation	= CST.componentBroker.getWaitComponent(osgiImplementation.getDeclaration().getName());
 				Component instance 			= CST.componentBroker.getWaitComponent(osgiInstance.getDeclaration().getName());
 				
 				instances.add((Instance)instance);
 				implementations.add((Implementation)implementation);
 				
 			}
 			if (dependency.getTargetType() == ComponentKind.IMPLEMENTATION) {
 				if (dependency.isMultiple())
 					return new Resolved<Implementation> (implementations) ;
				return new Resolved<Implementation> (implementations.iterator().next()) ;
 			}
 			if (dependency.getTargetType() == ComponentKind.INSTANCE) {
 				if (dependency.isMultiple())
 					return new Resolved<Instance> (instances) ;
				return new Resolved<Instance> (instances.iterator().next()) ;
 			}
 
 		} catch (InvalidSyntaxException ignored) { }
 
 		return resolution;
 	}
 
 	@Override
 	public Instance resolveImpl(Component client, Implementation impl, Dependency dep) {
 		return null;
 	}
 
 	@Override
 	public Set<Instance> resolveImpls(Component client, Implementation impl,	Dependency dep) {
 		return null;
 	}
 
 	@Override
 	public Implementation findImplByName(Component client, String implName) {
 		return null;
 	}
 
 	@Override
 	public Instance findInstByName(Component client, String instName) {
 		return null;
 	}
 
 	@Override
 	public Specification findSpecByName(Component client, String specName) {
 		return null;
 	}
 
 //	@Override
 //	public Component findComponentByName(Component client, String compName) {
 //		return null;
 //	}
 
 	@Override
 	public void notifySelection(Component client, ResolvableReference resName, String depName, Implementation impl, Instance inst, Set<Instance> insts) {
 	}
 
 	@Override
 	public ComponentBundle findBundle(CompositeType context, String bundleSymbolicName, String componentName) {
 		return null;
 	}
 
 }
