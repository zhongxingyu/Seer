 /*******************************************************************************
  * Copyright (c) 28 dec. 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.scoping;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.cdo.eresource.CDOResourceFolder;
 import org.eclipse.emf.cdo.eresource.CDOResourceNode;
 import org.eclipse.emf.cdo.view.CDOView;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.resource.IResourceDescription;
 import org.eclipse.xtext.resource.IResourceDescriptions;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.scoping.IScopeProvider;
 import org.eclipse.xtext.scoping.impl.AbstractGlobalScopeProvider;
 import org.eclipse.xtext.scoping.impl.SelectableBasedScope;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.netxforge.internal.RuntimeActivator;
 import com.netxforge.interpreter.IInterpreterContext;
 import com.netxforge.netxscript.NetxscriptPackage;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 /**
  * A Scope provider based on a {@link Map} of external model {@link EClass
  * NetXStudio EMF Classes} and the corresponding {@link CDOResource#getURI()}
  * .</p> The {@link IScope} is produced from an implementation of
  * {@link IResourceDescriptions} which is
  * {@link AbstractDynamixCDOResourceDescriptions}. </p>It can be (optionally)
  * populated for the given URI map by calling the method
  * <code>{@link #initDescriptions()}</code>. When not populated the cache will
  * do so, whenever an {@link IScope} is requested.</p>
  * 
  * Updates to the URI map are performed whenever the CDO targets invalidations
  * are received. The <code>Map</code> is initialized at instantiation with a
  * predefined set of {@link CDOResource} URI's which contain the referenced.
  * (Scoped) objects. </p> This {@link IScopeProvider} implements
  * {@link IExternalContextAware}. When applied can reduce the set of
  * {@link IEObjectDescription descriptions} by setting a {@link Predicate
  * <IEObjectDescription> }
  * 
  * 
  * @author Christophe Bouhier
  */
 @Singleton
 public class DynamixCDOScopeProvider extends AbstractGlobalScopeProvider
 		implements IExternalContextAware {
 
 	private Provider<IResourceDescriptions> loadOnDemandDescriptions;
 
 	private ModelUtils modelUtils;
 
 	private CDOView view;
 
 	/**
 	 * Our dynamic map of EClass and URIs for NetXResource object, we expect the
 	 * list of URI's to grow.
 	 */
 	private Map<EClass, List<URI>> eClassToURIMap;
 
 	/** The Resource Descriptions */
 	private IResourceDescriptions descriptions;
 
 	/** Our external model (CDO) listener **/
 	private DynamixCDOScopeListener cdoScopeListener;
 
 	/**
 	 * maintain the initializing state of the scope providers. when
 	 * initializing, no scope can be queried
 	 */
 	private boolean initializing = false;
 
 	@Inject
 	public DynamixCDOScopeProvider(
 			Provider<IResourceDescriptions> descriptionsProvider,
 			ModelUtils modelUtils) {
 		super();
 
 		this.modelUtils = modelUtils;
 		this.loadOnDemandDescriptions = descriptionsProvider;
 
 		initialize();
 	}
 
 	/**
 	 * Will not initialize if the view is already set.
 	 */
 	private void initialize() {
 		// Use a singleton transaction.
 		if (view == null) {
 			descriptions = loadOnDemandDescriptions.get();
 			if (descriptions instanceof AbstractDynamixCDOResourceDescriptions) {
 				view = ((AbstractDynamixCDOResourceDescriptions) descriptions)
 						.getDataProvider().getView();
 				eClassToURIMap = this.getClassToURIMap();
 				cdoScopeListener = new DynamixCDOScopeListener(this);
 				view.addListener(cdoScopeListener);
 				((AbstractDynamixCDOResourceDescriptions) descriptions)
 						.initialize(flattenMap(), view);
 			}
 		} else {
 			throw new IllegalStateException(
 					"Can't initialize the Global Scope Provider, as the CDO View is already set, this is an illegal state of the application. ");
 		}
 	}
 
 	/**
 	 * @return the initializing
 	 */
 	public synchronized boolean isInitializing() {
 		return initializing;
 	}
 
 	/**
 	 * @param initializing
 	 *            the initializing to set
 	 */
 	public synchronized void setInitializing(boolean initializing) {
 		this.initializing = initializing;
 	}
 
 	/**
 	 * Tell us we are running on the Server.
 	 * 
 	 * @return
 	 */
 	public boolean isServer() {
 
 		// We can tell if we are on the server, bind the instantiated
 		// xxxDescriptions, which varies depending
 		// Server or Client.
 		if (descriptions instanceof DynamixCDOResourceDescriptions) {
 			return false;
 		}
 		return true;
 	}
 	
 	
	/**
	 * A filter which interprets {@link IExternalContextAware} 
	 * 
	 * @author Christophe
	 */
 	class ExternalReferencePredicate implements Predicate<IEObjectDescription> {
 
 		// Keeps count of filtered items.
 		int countFilteredDescriptions = 0;
 
 		public int getCountFilteredDescriptions() {
 			return countFilteredDescriptions;
 		}
 
 		public int getTotalDescriptions() {
 			return totalDescriptions;
 		}
 
 		int totalDescriptions = 0;
 
 		private EReference reference;
 
 		private IInterpreterContext[] externalContexts;
 
 		private Component contextComponent = null;
 
 		@SuppressWarnings("unused")
 		private Node contextNode = null;
 
 		public ExternalReferencePredicate(EReference reference,
 				IInterpreterContext[] externalContexts) {
 			this.reference = reference;
 			this.externalContexts = externalContexts;
 			disectContext();
 		}
 
 		private void disectContext() {
 			for (int i = 0; i < externalContexts.length; i++) {
 				IInterpreterContext context = externalContexts[i];
 				if (context.getKind() == IInterpreterContext.MODEL_OBJECT) {
 					Object modelObject = context.getContext();
 					if (modelObject instanceof Component) {
 						contextComponent = (Component) modelObject;
 					} else if (modelObject instanceof Node) {
 						contextNode = (Node) modelObject;
 					}
 				}
 			}
 		}
 
 		/**
 		 * Various {@link NetxscriptPackage} external references are global,
 		 * meaning non-context sensitive and should therefor not be filtered.
 		 */
 		public boolean apply(IEObjectDescription desc) {
 			
 			totalDescriptions++;
 			
 			boolean result = true;
 			// use the context to create a new predicate for the
 			// descriptions.
 			// For the following external references.
 			if (reference == NetxscriptPackage.Literals.RESOURCE_REF__RESOURCE) {
 				// context will have a component, if the component doesn't have
 				// a resource with a matching
 				// expression name, filter the description.
 				if (contextComponent != null) {
 					// Find resources in the component with this name, if any
 					// return true.
 				}
 			}
 
 			if (result) {
 				countFilteredDescriptions++;
 			}
 			return result;
 		}
 
 		@Override
 		public String toString() {
 			return "Filtered " + countFilteredDescriptions + " out of "
 					+ totalDescriptions;
 
 		}
 	};
 	
 	@Override
 	public IScope getScope(Resource resource, final EReference reference, Predicate<IEObjectDescription> filter) {
 		if (externalContexts != null) {
 			// override the filter.
 			filter = new ExternalReferencePredicate(
 					reference, externalContexts);
 		}
 		return getScope(resource, isIgnoreCase(reference), reference.getEReferenceType(), filter);
 	}
 	
 	/**
 	 * This implementation resolves one or more {@link URI URI's} for the given
 	 * {@link EClass} with help from the URI Map which is then delegated to
 	 * {@link AbstractDynamixCDOResourceDescriptions} to populate and return the
 	 * {@link IScope}.
 	 * 
 	 */
 	@Override
 	protected IScope getScope(Resource resource, boolean ignoreCase,
 			EClass type, Predicate<IEObjectDescription> filter) {
 
 		IScope scope = IScope.NULLSCOPE;
 
 		if (RuntimeActivator.DEBUG) {
 			RuntimeActivator.TRACE.trace(
 					RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 					" scope provider invoked for Resource: "
 							+ resource.getURI().toFileString()
 							+ " , EClass: "
 							+ type.getName()
 							+ (filter != null ? " , filter:"
 									+ filter.toString() : ", no filter"));
 		}
 
 		// prevent from accessing the global scope, if we
 		if (!isInitializing()) {
 
 			List<URI> urisForClass = urisForClass(type);
 			if (urisForClass != null && urisForClass.size() > 0) {
 				for (URI uri : urisForClass) {
 
 					// the parent scope is self, so all URI's are caught.
 					scope = createLazyResourceScope(scope, uri, descriptions,
 							type, filter, ignoreCase);
 				}
 			} else {
 				if (RuntimeActivator.DEBUG) {
 					RuntimeActivator.TRACE.trace(
 							RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 							"no URI's for target EClass:" + type.getName());
 				}
 			}
 		} else {
 			if (RuntimeActivator.DEBUG) {
 				System.err
 						.println("NETXSCRIPT: Global scope not available, currently Initializing...please try again later");
 			}
 		}
 		return scope;
 	}
 
 	/**
 	 * Also for the supers of this class.
 	 * 
 	 * @return
 	 */
 	private List<URI> urisForClass(EClass eClass) {
 
 		List<EClass> classesToCheck = Lists.newArrayList();
 
 		if (eClass == LibraryPackage.Literals.BASE_RESOURCE) {
 			// Add for both NetXResource and Derived Resource.
 			// CB http://work.netxforge.com/issues/297
 			classesToCheck.add(LibraryPackage.Literals.NET_XRESOURCE);
 			classesToCheck.add(ServicesPackage.Literals.SERVICE_USER);
 		} else {
 			classesToCheck.add(eClass);
 		}
 
 		classesToCheck.addAll(eClass.getEAllSuperTypes());
 
 		// CB http://work.netxforge.com/issues/297
 		List<URI> urisForClass = Lists.newArrayList();
 		for (EClass eC : classesToCheck) {
 			List<URI> list = this.eClassToURIMap.get(eC);
 			if (list != null) {
 				urisForClass.addAll(list);
 			}
 		}
 
 		if (urisForClass.size() > 0) {
 			return urisForClass;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Requires, a IResourceServiceProvider which can deal with the CDO URI.
 	 * 
 	 * @param parent
 	 * @param uri
 	 * @param descriptions
 	 * @param type
 	 * @param filter
 	 * @param ignoreCase
 	 * @return
 	 */
 	protected IScope createLazyResourceScope(IScope parent, final URI uri,
 			final IResourceDescriptions descriptions, EClass type,
 			final Predicate<IEObjectDescription> filter, boolean ignoreCase) {
 
 		IResourceDescription description = descriptions
 				.getResourceDescription(uri);
 
 		IScope createScope = SelectableBasedScope.createScope(parent,
 				description, filter, type, ignoreCase);
 
 //		if (RuntimeActivator.DEBUG && filter != null) {
 //			RuntimeActivator.TRACE.trace(
 //					RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 //					filter.toString());
 //		}
 
 		return createScope;
 
 	}
 
 	/**
 	 * Initialize for all URI's representing CDO Resources. this will populate
 	 * the cache and speed up usage of an Expression editor. The method is not
 	 * reentrant. the initializing state is kept, and can be queried.
 	 */
 	public void initDescriptions() {
 
 		if (!isInitializing()) {
 			setInitializing(true);
 			for (EClass eClass : eClassToURIMap.keySet()) {
 				List<URI> urisForClass = eClassToURIMap.get(eClass);
 				for (URI uri : urisForClass) {
 					// Call to populate the cache.
 					descriptions.getResourceDescription(uri);
 				}
 			}
 			setInitializing(false);
 		}
 	}
 
 	/**
 	 * Populates our URI Map with a pre-defined set of {@link EClass EMF
 	 * Classes} and the associated {@link CDOResource#getURI()}.
 	 * 
 	 * @see {@link NetxscriptPackage NetXScript} for possible referenced
 	 *      external objects.
 	 */
 	public Map<EClass, List<URI>> getClassToURIMap() {
 
 		// Resolve
 		String repo_name = this.view.getSession().getRepositoryInfo().getName();
 
 		final LinkedHashMap<EClass, List<URI>> classURIMap = Maps
 				.newLinkedHashMap();
 
 		URI nodeTypeURI = uri(repo_name, LibraryPackage.Literals.NODE_TYPE);
 		classURIMap.put(LibraryPackage.Literals.NODE_TYPE,
 				ImmutableList.of(nodeTypeURI));
 
 		classURIMap.put(LibraryPackage.Literals.PARAMETER, ImmutableList
 				.of(uri(repo_name, LibraryPackage.Literals.PARAMETER)));
 
 		classURIMap.put(ServicesPackage.Literals.SERVICE_USER, ImmutableList
 				.of(uri(repo_name, ServicesPackage.Literals.SERVICE_USER)));
 
 		URI operatorURI = uri(repo_name, OperatorsPackage.Literals.OPERATOR);
 
 		classURIMap.put(OperatorsPackage.Literals.OPERATOR,
 				ImmutableList.of(operatorURI));
 
 		// CB don't need network (yet??)
 		// classURIMap.put(OperatorsPackage.Literals.NETWORK,
 		// ImmutableList.of(operatorURI));
 		classURIMap.put(OperatorsPackage.Literals.NODE,
 				ImmutableList.of(operatorURI));
 
 		// CB Equipment and Function do not have their own resource, but could
 		// exist both in a type URI as in
 		// an Operator URI
 		classURIMap.put(LibraryPackage.Literals.COMPONENT,
 				ImmutableList.of(operatorURI, nodeTypeURI));
 
 		// CB NetXResource are held in a CDO Folder container, scan the
 		// container
 
 		URI nodeResourceURI = URI.createURI("cdo://" + repo_name + "/Node_");
 
 		// CB, do not allow NetXResource objects from the NodeTypes in an
 		// expression.
 		//
 		// URI nodeTypeResoureURI = URI.createURI("cdo://" + repo_name
 		// + "/NodeType_");
 
 		// this list is mutable by intend.
 		ArrayList<URI> urisAsList = Lists.newArrayList();
 
 		{
 			List<URI> allNodeURIs = resolveAllURIs(nodeResourceURI);
 			urisAsList.addAll(allNodeURIs);
 		}
 
 		// first type init.
 		classURIMap.put(LibraryPackage.Literals.NET_XRESOURCE, urisAsList);
 		return classURIMap;
 		// return ImmutableMap.copyOf(classURIMap);
 
 	}
 
 	public URI uri(String repo_name, EClass eClass) {
 		return URI.createURI("cdo://" + repo_name + "/" + eClass.getName());
 	}
 
 	/*
 	 * Resolves all child URI's from the provider URI path.
 	 * 
 	 * @param nodeURI
 	 * 
 	 * @return
 	 */
 	private List<URI> resolveAllURIs(URI uri) {
 		String resourcePath = "/" + uri.lastSegment();
 		// return uriForPath(transaction.getRootResource().getFolder(),
 		// resourcePath);
 		return uriForPath(resourcePath);
 	}
 
 	/*
 	 * Returns all URI as a child of the provided path. Consider moving this as
 	 * a DataProvider Interface.
 	 */
 	private List<URI> uriForPath(String name) {
 		List<URI> childURIs = Lists.newArrayList();
 		if (view != null && view.hasResource(name)) {
 			CDOResourceNode resourceNode = view.getResourceNode(name);
 			if (resourceNode instanceof CDOResourceFolder) {
 				List<Resource> resources = this
 						.getResourcesFromNode((CDOResourceFolder) resourceNode);
 				childURIs.addAll(modelUtils.transformResourceToURI(resources));
 			}
 		}
 		return childURIs;
 	}
 
 	private List<Resource> getResourcesFromNode(CDOResourceFolder cdoFolder) {
 		List<Resource> resources = Lists.newArrayList();
 		EList<CDOResourceNode> nodes = cdoFolder.getNodes();
 		for (CDOResourceNode n : nodes) {
 			if (n instanceof CDOResourceFolder) {
 				resources.addAll(getResourcesFromNode((CDOResourceFolder) n));
 			} else {
 				resources.add((Resource) n);
 			}
 		}
 		return resources;
 	}
 
 	private ImmutableList<URI> flattenMap() {
 
 		ArrayList<URI> flattenURIList = Lists.newArrayList();
 		for (Iterator<EClass> it = eClassToURIMap.keySet().iterator(); it
 				.hasNext();) {
 			flattenURIList.addAll(eClassToURIMap.get(it.next()));
 		}
 
 		return ImmutableList.copyOf(flattenURIList);
 	}
 
 	/**
 	 * An updated URI could be of a CDO Folder, a CDO Resource or an object. For
 	 * any the URI would either exist in the existing map or might be new.
 	 * <ul>
 	 * <li>CDO Folder updates occur for /Node_ and /NodeType_ CDO folders
 	 * containing NetXResource objects.</li>
 	 * <li>CDO Resource updates occur for any of the CDO Resources adding or
 	 * deleting objects to their contents</li>
 	 * <li>Individual object updates occur for single object mutations</li>
 	 * </ul>
 	 * We first check if the update is a CDOResource Folder update, we look for
 	 * new URI's which are not in our map, add them and also add them to the
 	 * cached so a ResourceDecription is produced in the cache.
 	 * 
 	 * For regular object we update the object URI .
 	 * 
 	 * @param dirtyDozen
 	 */
 	public void updateURIMap(Set<CDOObject> dirtyDozen) {
 
 		List<URI> urisToUpdate = Lists.newArrayList();
 		List<URI> urisToAdd = Lists.newArrayList();
 
 		for (CDOObject cdoO : dirtyDozen) {
 			if (cdoO instanceof CDOResourceFolder) {
 				// Get the uri from children.
 				CDOResourceFolder cdoFolder = (CDOResourceFolder) cdoO;
 				List<Resource> resourcesFromNode = this
 						.getResourcesFromNode(cdoFolder);
 
 				List<URI> dirtyURIS = Lists.newArrayList();
 				dirtyURIS.addAll(modelUtils
 						.transformResourceToURI(resourcesFromNode));
 
 				if (cdoFolder.getName().equals("Node_")
 						|| cdoFolder.getName().equals("NodeType_")) {
 					List<URI> existingNetXResourceURIS = this.eClassToURIMap
 							.get(LibraryPackage.Literals.NET_XRESOURCE);
 					for (URI dirtyURI : dirtyURIS) {
 						if (existingNetXResourceURIS.contains(dirtyURI)) {
 							// update it.
 							urisToUpdate.add(dirtyURI);
 						} else {
 							urisToAdd.add(dirtyURI);
 						}
 					}
 
 					// Process additions.
 					if (!urisToAdd.isEmpty()) {
 						existingNetXResourceURIS.addAll(urisToAdd);
 						this.eClassToURIMap.put(
 								LibraryPackage.Literals.NET_XRESOURCE,
 								existingNetXResourceURIS);
 
 						if (RuntimeActivator.DEBUG) {
 							for (URI uri : urisToAdd) {
 								RuntimeActivator.TRACE
 										.trace(RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 												"adding descriptions for URI "
 														+ uri.toString());
 							}
 						}
 
 						((AbstractDynamixCDOResourceDescriptions) descriptions)
 								.add(urisToAdd);
 
 					}
 					// Process updates.
 					if (!urisToUpdate.isEmpty()) {
 
 						if (RuntimeActivator.DEBUG) {
 
 							for (URI uri : urisToUpdate) {
 								RuntimeActivator.TRACE
 										.trace(RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 												"updating descriptions for URI "
 														+ uri.toString());
 							}
 						}
 						((AbstractDynamixCDOResourceDescriptions) descriptions)
 								.update(urisToUpdate);
 					}
 				}
 
 			} else if (cdoO instanceof CDOResource) {
 				CDOResource cdoRes = (CDOResource) cdoO;
 
 				if (flattenMap().contains(cdoRes.getURI())) {
 					urisToUpdate.add(cdoRes.getURI());
 					if (RuntimeActivator.DEBUG) {
 
 						for (URI uri : urisToUpdate) {
 							RuntimeActivator.TRACE
 									.trace(RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 											" updating descriptions for URI "
 													+ uri.toString());
 						}
 					}
 
 					((AbstractDynamixCDOResourceDescriptions) descriptions)
 							.update(urisToUpdate);
 				} else {
 					// We are not interrested in this resource.
 				}
 
 			} else if (cdoO instanceof CDOObject) {
 				EClass eClass = cdoO.eClass();
 
 				// Force a rebuild of the uri list.
 				List<URI> existingURIs = this.eClassToURIMap.get(eClass);
 				if (existingURIs != null) {
 					if (RuntimeActivator.DEBUG) {
 						for (URI uri : existingURIs) {
 							RuntimeActivator.TRACE
 									.trace(RuntimeActivator.TRACE_NETXSCRIPT_SCOPING_OPTION,
 											" updating descriptions for URI "
 													+ uri.toString());
 						}
 					}
 
 					((AbstractDynamixCDOResourceDescriptions) descriptions)
 							.update(existingURIs);
 				}
 			}
 		}
 	}
 
 	/**
 	 * An external context which is used to reduce the scope for the given
 	 * context
 	 */
 	private IInterpreterContext[] externalContexts = null;
 
 	public void setExternalContext(IInterpreterContext... context) {
 		externalContexts = context;
 
 	}
 
 	public void clearExternalContext() {
 		externalContexts = null;
 	}
 }
