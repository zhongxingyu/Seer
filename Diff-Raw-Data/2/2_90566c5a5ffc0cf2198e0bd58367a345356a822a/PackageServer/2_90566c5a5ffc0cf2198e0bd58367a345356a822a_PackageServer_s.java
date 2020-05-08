 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.pivot.manager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.ocl.examples.pivot.LambdaType;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.executor.PivotReflectivePackage;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 
 /**
  * A PackageServer adapts the primary Package to coordinate the coherent behaviour of a primary and one or more
  * secondary Packages as required for Complete OCL package extension.
  */
 public class PackageServer extends PackageTracker
 {
 	public static Function<PackageTracker, org.eclipse.ocl.examples.pivot.Package> tracker2package = new Function<PackageTracker, org.eclipse.ocl.examples.pivot.Package>()
 	{
 		public org.eclipse.ocl.examples.pivot.Package apply(PackageTracker packageTracker) {
 			return packageTracker.getTarget();
 		}
 	};
 	
 	/**
 	 * List of all package extensions including this.
 	 */
 	private final List<PackageTracker> trackers = new ArrayList<PackageTracker>();
 	
 	/**
 	 * Map of nested class-name to multi-class server.
 	 */
 	private Map<String, TypeServer> typeServers = null;
 	
 	/**
 	 * Map of nested package-name to multi-package server.
 	 */
 	private Map<String, PackageServer> nestedPackageServers = null;
 
 	/**
 	 * The Executor package containing the dispatch table representation.
 	 */
 	private PivotReflectivePackage executorPackage = null;
 	
 	protected PackageServer(PackageManager packageManager, org.eclipse.ocl.examples.pivot.Package primaryPackage) {
 		super(packageManager, primaryPackage);
 		trackers.add(this);
 		initContents(this);
 	}
 
 	public void addNestedPackage(org.eclipse.ocl.examples.pivot.Package pivotPackage) {
 		if (nestedPackageServers == null) {
 			nestedPackageServers = new HashMap<String, PackageServer>();
 		}
 		String packageName = pivotPackage.getName();
 		String nsURI = pivotPackage.getNsURI();
 		org.eclipse.ocl.examples.pivot.Package primaryPackage = null;
 		if (nsURI != null) {										// Explicit nsURI for explicit package (merge)
 			primaryPackage = packageManager.getPackageByURI(nsURI);
 			if (primaryPackage != null) {
 				PackageServer packageServer = packageManager.getPackageTracker(primaryPackage).getPackageServer();
 				if (primaryPackage != pivotPackage) {
 					packageServer.addSecondaryPackage(pivotPackage);
 				}
 				nestedPackageServers.put(packageName, packageServer);
 				return;
 			}
 		}
 		PackageServer nestedPackageServer = nestedPackageServers.get(packageName);
 		if (nestedPackageServer == null) {
 			PackageTracker nestedPackageTracker = (PackageTracker) EcoreUtil.getAdapter(pivotPackage.eAdapters(), packageManager);
 			if (nestedPackageTracker instanceof PackageClient) {
 				nestedPackageServer = nestedPackageTracker.getPackageServer();
 				nestedPackageServers.put(packageName, nestedPackageServer);
 			}
 			else if (nestedPackageTracker instanceof PackageServer) {
				nestedPackageServers.put(packageName, nestedPackageServer);
 				packageManager.addedNestedPrimaryPackage(pivotPackage);
 			}
 			else {
 				nestedPackageServer = new PackageServer(packageManager, pivotPackage);
 				nestedPackageServers.put(packageName, nestedPackageServer);
 				packageManager.addedNestedPrimaryPackage(pivotPackage);
 			}
 		}
 		else {
 			nestedPackageServer.addSecondaryPackage(pivotPackage);
 		}
 	}
 	
 	public void addSecondaryPackage(org.eclipse.ocl.examples.pivot.Package secondaryPackage) {
 		PackageClient packageClient = (PackageClient)EcoreUtil.getAdapter(secondaryPackage.eAdapters(), packageManager);
 		if (packageClient == null) {
 			packageClient = new PackageClient(this, secondaryPackage);
 		}
 		if (!trackers.contains(packageClient)) {
 			trackers.add(packageClient);
 		}
 	}	
 	
 	void addType(Type pivotType) {
 		if ((pivotType instanceof LambdaType) || (pivotType instanceof TupleType)) {	// FIXME parent not necessarily in place
 			return;
 		}
 		if (typeServers == null) {
 			typeServers = new HashMap<String, TypeServer>();
 		}
 		String className = pivotType.getName();
 		TypeServer typeServer = typeServers.get(className);
 		if (typeServer == null) {
 			TypeTracker typeTracker = (TypeTracker) EcoreUtil.getAdapter(pivotType.eAdapters(), packageManager);
 			typeServer = typeTracker != null ? typeTracker.getTypeServer() : null;
 			if (typeServer == null) {
 				typeServer = new TypeServer(packageManager, pivotType);
 			}
 			if (pivotType.getUnspecializedElement() == null) {
 				typeServers.put(className, typeServer);
 			}
 		}
 		else {
 			typeServer.addSecondaryType(pivotType);
 		}
 	}
 
 	void addedNestedPackage(Object nestedObject) {
 		if (nestedObject instanceof org.eclipse.ocl.examples.pivot.Package) {
 			org.eclipse.ocl.examples.pivot.Package nestedPackage = (org.eclipse.ocl.examples.pivot.Package)nestedObject;
 			addNestedPackage(nestedPackage);
 		}
 	}
 
 	@Override
 	public void dispose() {
 		if (!trackers.isEmpty()) {
 			Collection<PackageTracker> savedPackageTrackers = new ArrayList<PackageTracker>(trackers);
 			trackers.clear();
 			for (PackageTracker tracker : savedPackageTrackers) {
 				if (tracker instanceof PackageClient) {
 					tracker.dispose();
 				}
 			}
 		}
 		if (typeServers != null) {
 			Collection<TypeServer> savedTypeServers = new ArrayList<TypeServer>(typeServers.values());
 			typeServers.clear();
 			for (TypeServer typeServer : savedTypeServers) {
 				typeServer.dispose();
 			}
 			typeServers = null;
 		}
 		if (nestedPackageServers != null) {
 			Collection<PackageServer> savedPackageServers = new ArrayList<PackageServer>(nestedPackageServers.values());
 			nestedPackageServers.clear();
 			for (PackageServer packageServer : savedPackageServers) {
 				packageServer.dispose();
 			}
 			nestedPackageServers = null;
 		}
 		super.dispose();
 	}
 
 	public PivotReflectivePackage getExecutorPackage() {
 		if (executorPackage == null) {
 			executorPackage = new PivotReflectivePackage(getMetaModelManager(), target);
 		}
 		return executorPackage ;
 	}
 
 	public org.eclipse.ocl.examples.pivot.Package getNestedPackage(String nestedPackageName) {
 		PackageServer nestedPackageServer = nestedPackageServers.get(nestedPackageName);
 		return nestedPackageServer != null ? nestedPackageServer.getTarget() : null;
 	}
 	
 	@Override
 	public PackageServer getPackageServer() {
 		return this;
 	}
 
 	public Iterable<org.eclipse.ocl.examples.pivot.Package> getPackages() {
 		return Iterables.transform(trackers, tracker2package);
 	}
 
 	public Type getType(String typeName) {
 		TypeServer typeServer = typeServers.get(typeName);
 		return typeServer != null ? typeServer.getTarget() : null;
 	}
 
 	@Override
 	TypeTracker getTypeTracker(Type pivotType) {
 		if (typeServers == null) {
 			typeServers = new HashMap<String, TypeServer>();
 		}
 		String className = pivotType.getName();
 		TypeServer typeServer = typeServers.get(className);
 		if (typeServer == null) {
 			typeServer = (TypeServer) EcoreUtil.getAdapter(pivotType.eAdapters(), packageManager);
 			if (typeServer == null) {
 				typeServer = new TypeServer(packageManager, pivotType);
 			}
 			typeServers.put(className, typeServer);
 		}
 		return typeServer.getTypeTracker(pivotType);
 	}
 	
 	void removedClient(PackageClient packageClient) {
 		trackers.remove(packageClient);
 	}
 
 	void removedNestedPackage(Object nestedObject) {
 		if (nestedObject instanceof org.eclipse.ocl.examples.pivot.Package) {
 			org.eclipse.ocl.examples.pivot.Package nestedPackage = (org.eclipse.ocl.examples.pivot.Package)nestedObject;
 			PackageServer packageServer = nestedPackageServers.get(nestedPackage.getName());
 			packageServer.removedPackage(nestedPackage);
 		}
 	}
 
 //	public void removePackage(Package pivotPackage) {
 //		removedPackage(pivotPackage);
 //	}
 
 	void removedPackage(org.eclipse.ocl.examples.pivot.Package pivotPackage) {
 		PackageTracker packageTracker = packageManager.findPackageTracker(pivotPackage);
 		if (packageTracker == this) {
 			dispose();
 		}
 		else {
 			trackers.remove(packageTracker);
 		}
 	}
 }
