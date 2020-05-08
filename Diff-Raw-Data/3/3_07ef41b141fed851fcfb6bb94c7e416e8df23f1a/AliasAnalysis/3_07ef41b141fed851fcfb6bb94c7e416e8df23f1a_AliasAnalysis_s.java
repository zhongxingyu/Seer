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
 package org.eclipse.ocl.examples.xtext.base.pivot2cs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.util.Pivotable;
 import org.eclipse.ocl.examples.pivot.utilities.PathElement;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ImportCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.NamedElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.RootPackageCS;
 import org.eclipse.ocl.examples.xtext.base.utilities.ElementUtil;
 
 /**
  * An AliasAnalysis is dynamically created to support the serialization
  * of cross-references following a Pivot to CS conversion. It ensures the
  * resource-wide uniqueness of aliases for package names.
  * 
  * Uniqueness is achieved with respect to all names to avoid the complexity
  * of considering which name usages are not actually conflicting.
  */
 public class AliasAnalysis extends AdapterImpl
 {
 	public static void dispose(Resource resource) {
 		if (resource != null) {
 			List<Adapter> eAdapters = resource.eAdapters();
 			AliasAnalysis adapter = PivotUtil.getAdapter(AliasAnalysis.class, eAdapters);
 			if (adapter != null) {
 				adapter.dispose();
 			}
 		}
 	}
 
 	public static AliasAnalysis getAdapter(Resource resource) {
 		if (resource == null) {
 			return null;
 		}
 		List<Adapter> eAdapters = resource.eAdapters();
 		AliasAnalysis adapter = PivotUtil.getAdapter(AliasAnalysis.class, eAdapters);
 		if (adapter == null) {
 			adapter = new AliasAnalysis(resource);
 			Set<org.eclipse.ocl.examples.pivot.Package> localPackages = new HashSet<org.eclipse.ocl.examples.pivot.Package>();
 			Set<org.eclipse.ocl.examples.pivot.Package> otherPackages = new HashSet<org.eclipse.ocl.examples.pivot.Package>();
 			MetaModelManager metaModelManager = ElementUtil.findMetaModelManager(resource);
 			adapter.computePackages(metaModelManager, localPackages, otherPackages);
 			adapter.computeAliases(metaModelManager, localPackages, otherPackages);
 		}
 		return adapter;
 	}
 
 	/**
 	 * Mapping of all named elements from the name to the name usage,
 	 * which is non-null for a uniquely named element, or
 	 * null for a shared name.
 	 */
 	private Map<String, EObject> allNames = new HashMap<String, EObject>();
 	
 	/**
 	 * The known or assigned package aliases/
 	 */
 	private Map<org.eclipse.ocl.examples.pivot.Package, String> allAliases = new HashMap<org.eclipse.ocl.examples.pivot.Package, String>();
 
 	public AliasAnalysis(Resource resource) {
 		resource.eAdapters().add(this);
 	}
 
 	/**
 	 * Assign a unique alias to each localPackage then to each otherPackage.
 	 */
 	private void computeAliases(MetaModelManager metaModelManager,
 			Set<org.eclipse.ocl.examples.pivot.Package> localPackages,
 			Set<org.eclipse.ocl.examples.pivot.Package> otherPackages) {		
 		for (org.eclipse.ocl.examples.pivot.Package localPackage : localPackages) {
 			if (metaModelManager != null) {
 				localPackage = metaModelManager.getPrimaryPackage(localPackage);
 			}
 			if ((localPackage.getNsPrefix() != null) || (localPackage.getNestingPackage() == null)) {
 				if (!allAliases.containsKey(localPackage)) {
 					String alias = computeAlias(localPackage);
 					allAliases.put(localPackage, alias);
 				}
 			}
 		}
 		for (org.eclipse.ocl.examples.pivot.Package otherPackage : otherPackages) {
 			if (metaModelManager != null) {
 				otherPackage = metaModelManager.getPrimaryPackage(otherPackage);
 			}
 			if (!allAliases.containsKey(otherPackage)) {
 				String alias = computeAlias(otherPackage);
 				allAliases.put(otherPackage, alias);
 			}
 		}
 	}
 
 	/**
 	 * Register the usage of name by primaryElement, and if name is already in use
 	 * register the ambiguity as a usage by null.
 	 */
 	private void addName(String name, EObject primaryElement) {
 		if (name != null) {
 			if (!allNames.containsKey(name)) {
 				allNames.put(name, primaryElement);
 			}
 			else if (allNames.get(name) != primaryElement) {
 				allNames.put(name, null);
 			}
 		}
 	}
 
 	/**
 	 * Determine a unique alias for primaryPackage/
 	 */
 	private String computeAlias(org.eclipse.ocl.examples.pivot.Package primaryPackage) {
 		String nsPrefix = primaryPackage.getNsPrefix();
 		String aliasBase = nsPrefix != null ? nsPrefix : getDefaultAlias(primaryPackage.getName());
 		int index = 0;
 		String alias = aliasBase;
 		while (allNames.containsKey(alias) && (allNames.get(alias) != primaryPackage)) {
 			@SuppressWarnings("unused")
 			EObject debugObject = allNames.get(alias);
 			alias = aliasBase + "_" + index++;
 		}
 		addName(alias, primaryPackage);
 		return alias;
 	}
 
 	/**
 	 * Scan the target resource to identify allNames of any form that appear,
 	 * allAliases assigned by explicit imports, all localPackages whose name is
 	 * defined within the target resource all all otherPackages. Nested packages
 	 * of localPackages are excluded from localPackages.
 	 */
 	private void computePackages(MetaModelManager metaModelManager,
 			Set<org.eclipse.ocl.examples.pivot.Package> localPackages,
 			Set<org.eclipse.ocl.examples.pivot.Package> otherPackages) {
 		for (TreeIterator<EObject> tit = ((Resource)target).getAllContents(); tit.hasNext(); ) {
 			EObject eObject = tit.next();
 			if (eObject instanceof ImportCS) {
 				String name = ((ImportCS)eObject).getName();
 				Namespace namespace = ((ImportCS)eObject).getNamespace();
 				if (namespace instanceof org.eclipse.ocl.examples.pivot.Package) {
 					allAliases.put((org.eclipse.ocl.examples.pivot.Package) namespace, name);
 				}
 			}
 			EObject csObject = eObject;
 			if (eObject instanceof Pivotable) {
 				eObject = ((Pivotable)eObject).getPivot();
 			}
 			if (eObject instanceof NamedElement) {
 				if (metaModelManager != null) {
 					eObject = metaModelManager.getPrimaryElement(eObject);
 				}
 				addName(((NamedElement)eObject).getName(), eObject);
 				if ((eObject instanceof org.eclipse.ocl.examples.pivot.Package) && (csObject instanceof RootPackageCS)) {
 					org.eclipse.ocl.examples.pivot.Package pivotPackage = (org.eclipse.ocl.examples.pivot.Package)eObject;
 					addName(pivotPackage.getNsPrefix(), eObject);
 					localPackages.add(pivotPackage);
 				}
 				else {
 					for (EObject eContainer = eObject; eContainer != null; eContainer = eContainer.eContainer()) {
 						if (eContainer instanceof org.eclipse.ocl.examples.pivot.Package) {
 							otherPackages.add((org.eclipse.ocl.examples.pivot.Package)eContainer);
 							break;
 						}
 						if (eContainer instanceof Type) {
 							eContainer = PivotUtil.getUnspecializedTemplateableElement((Type)eContainer);
 						}
 					}
 				}
 			}
 		}
 		otherPackages.removeAll(localPackages);
 		Set<org.eclipse.ocl.examples.pivot.Package> nestedPackages = new HashSet<org.eclipse.ocl.examples.pivot.Package>();
 		for (org.eclipse.ocl.examples.pivot.Package localPackage : localPackages) {
 			EObject eContainer = localPackage.eContainer();
 			if (eContainer instanceof org.eclipse.ocl.examples.pivot.Package) {
 				EObject eContainerContainer = eContainer.eContainer();
 				if (eContainerContainer instanceof org.eclipse.ocl.examples.pivot.Package) {
 					nestedPackages.add(localPackage);
 				}
 			}
 		}
 		localPackages.removeAll(nestedPackages);
 	}
 	
 	public void dispose() {
 		target.eAdapters().remove(this);
 	}
 
 	/**
 	 * Return the alias for eObject.
 	 */
 	public String getAlias(EObject eObject) {
 		if (eObject instanceof Pivotable) {
 			eObject = ((Pivotable)eObject).getPivot();
 		}
 		if (eObject instanceof org.eclipse.ocl.examples.pivot.Package) {
 			String alias = allAliases.get(eObject);
 			if (alias != null) {
 				return alias;
 			}
 			MetaModelManager metaModelManager = ElementUtil.findMetaModelManager((Resource)getTarget());
 			if (metaModelManager != null) {
 				eObject = metaModelManager.getPrimaryElement(eObject);
 				return allAliases.get(eObject);
 			}
 		}
 		return null;
 	}
 	
 	protected String getDefaultAlias(String name) {
 		if (name == null) {
 			return "anon";			// Never happens
 		}
 		int iMax = name.length();
 		if (iMax <= 0) {
 			return "anon";			// Never happens
 		}
 		if (Character.isLowerCase(name.charAt(0))) {
 			return name;
 		}
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < iMax; i++) {
 			char c = name.charAt(i);
 			if (Character.isUpperCase(c)) {
 				s.append(Character.toLowerCase(c));
 			}
 			else {
 				s.append(name.substring(i));
 				break;
 			}
 		}
 		return s.toString();
 	}
 
 	public List<PathElement> getPath(Element eObject) {
 		EObject eContainer = eObject.eContainer();
 		if (eContainer == null) {
 			return new ArrayList<PathElement>();
 		}
 		List<PathElement> result = getPath((Element) eContainer);
 		if (eObject instanceof NamedElement) {
 			result.add(new PathElement(((NamedElement)eObject).getName(), eObject));
 		}
 		else if (eObject instanceof ENamedElement) {
 			result.add(new PathElement(((ENamedElement)eObject).getName(), eObject));
 		}
 		else if (eObject instanceof NamedElementCS) {
 			result.add(new PathElement(((NamedElementCS)eObject).getName(), eObject));
 		}
 		return result;
 	}
 	
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return type == AliasAnalysis.class;
 	}
 }
