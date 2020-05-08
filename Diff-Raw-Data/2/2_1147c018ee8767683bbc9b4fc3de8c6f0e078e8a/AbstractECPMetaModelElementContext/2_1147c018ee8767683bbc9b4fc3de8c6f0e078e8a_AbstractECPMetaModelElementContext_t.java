 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.common.model;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EPackage.Registry;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcorePackage;
 
 /**
  * MetaModelContext used by the editor to determine which model elements belong to the model.
  * 
  * @author helming
  */
 public abstract class AbstractECPMetaModelElementContext implements ECPMetaModelElementContext {
 
 	private Set<EClass> modelElementEClasses;
 	private static Set<EClass> guessedPackages;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isNonDomainElement(EObject eObject) {
 		return isNonDomainElement(eObject.eClass());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecp.model.MetaModelElementContext#isNonDomainElement(org.eclipse.emf.ecore.EClass)
 	 */
 	public abstract boolean isNonDomainElement(EClass eClass);
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Set<EClass> getAllSubEClasses(EClass eClass, boolean association) {
 
 		Set<EClass> allEClasses = getAllModelElementEClasses(association);
 		Set<EClass> result = new HashSet<EClass>();
 
 		for (EClass subClass : allEClasses) {
 			if ((eClass.equals(EcorePackage.eINSTANCE.getEObject()) || eClass.isSuperTypeOf(subClass))
 				&& (!subClass.isAbstract()) && (!subClass.isInterface())) {
 				result.add(subClass);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Set<EClass> getAllModelElementEClasses(boolean association) {
 
 		Set<EClass> result = new HashSet<EClass>();
 
 		for (EClass subClass : getAllModelElementEClassesImpl()) {
 			if (association || !isAssociationClassElement(subClass)) {
 				result.add(subClass);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param newMEInstance {@link EObject} the new modelElement instance.
 	 * @return EReference the Container
 	 * @param parent The EObject to get containment references from
 	 */
 	public EReference getPossibleContainingReference(final EObject newMEInstance, EObject parent) {
 		// the value of the 'EAll Containments' reference list.
 		List<EReference> eallcontainments = parent.eClass().getEAllContainments();
 		EReference reference = null;
 		for (EReference containmentitem : eallcontainments) {
 
 			EClass eReferenceType = containmentitem.getEReferenceType();
 			if (eReferenceType.equals(newMEInstance)) {
 				reference = containmentitem;
 
 				break;
 			} else if (eReferenceType.equals(EcorePackage.eINSTANCE.getEObject())
 				|| eReferenceType.isSuperTypeOf(newMEInstance.eClass())) {
 				reference = containmentitem;
 				break;
 			}
 		}
 		return reference;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isGuessed() {
 		IConfigurationElement[] packages = Platform.getExtensionRegistry().getConfigurationElementsFor(
 			"org.eclipse.emf.ecp.model.ecppackage");
 		return (packages.length == 0);
 	}
 
 	/**
 	 * Returns all types of model elements in this context.
 	 * 
 	 * @return a set of {@link EClass}es
 	 */
 	protected Set<EClass> getAllModelElementEClassesImpl() {
 
 		if (modelElementEClasses != null) {
 			return new HashSet<EClass>(modelElementEClasses);
 		}
 
 		Set<EClass> result = new HashSet<EClass>();
 		Set<String> registeredPackages = new HashSet<String>();
 		Registry registry = EPackage.Registry.INSTANCE;
 		IConfigurationElement[] packages = Platform.getExtensionRegistry().getConfigurationElementsFor(
 			"org.eclipse.emf.ecp.model.ecppackage");
 
 		for (IConfigurationElement element : packages) {
 			String packageName = element.getAttribute("modelPackage");
 			registeredPackages.add(packageName);
 		}
 
 		if (registeredPackages.isEmpty()) {
 			return guessPackages(new HashSet<Entry<String, Object>>(registry.entrySet()));
 		}
 
 		for (Entry<String, Object> entry : new HashSet<Entry<String, Object>>(registry.entrySet())) {
 
 			if (!registeredPackages.contains(entry.getKey())) {
 				continue;
 			}
 
 			try {
 				EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(entry.getKey());
 				result.addAll(getAllModelElementEClasses(ePackage));
 			}
 			// BEGIN SUPRESS CATCH EXCEPTION
 			catch (RuntimeException exception) {
 				// END SUPRESS CATCH EXCEPTION
 				String message = "Failed to load model package " + entry.getKey();
 				Activator.getDefault().logException(message, exception);
 			}
 		}
 
 		modelElementEClasses = result;
 		return result;
 	}
 
 	private Set<EClass> guessPackages(HashSet<Entry<String, Object>> entries) {
 		if (guessedPackages == null) {
 			guessedPackages = new HashSet<EClass>();
 			for (Entry<String, Object> entry : entries) {
 
 				if (!isKnownPackage(entry.getKey())) {
 					// This is used to discover known packages which can be added to
 					// know packages then.
					// System.out.println("\"" + entry.getKey() + "\"" + ",");
 					try {
 						EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(entry.getKey());
 						guessedPackages.addAll(getAllModelElementEClasses(ePackage));
 						// BEGIN SUPRESS CATCH EXCEPTION
 					} catch (RuntimeException e) {
 						// END SUPRESS CATCH EXCEPTION
 						String message = "Failed to load model package: " + entry.getKey();
 						Activator.getDefault().logWarning(message, e);
 					}
 				}
 			}
 		}
 		return guessedPackages;
 	}
 
 	private boolean isKnownPackage(String key) {
 
 		String[] elements = { "http://www.eclipse.org/m2t/xpand/Trace",
 			"http://www.eclipse.org/emf/eef/mapping/filters/1.0.0", "http://www.cs.tum.edu/cope/history/0.1.42",
 			"http://www.eclipse.org/acceleo/profiler/3.0", "http://www.eclipse.org/emf/compare/epatch/0.1",
 			"http://www.eclipse.org/ocl/1.1.0/UML", "http://www.eclipse.org/emf/eef/components/1.0.0",
 			"http://www.eclipse.org/amalgamation/discovery/1.0", "http://www.eclipse.org/emf/compare/match/1.1",
 			"http://www.eclipse.org/emf/eef/views/1.0.0",
 			"http://www.eclipse.org/gmf/examples/runtime/1.0.0/logicsemantic",
 			"http://www.eclipse.org/acceleo/mtl/3.0", "http://www.eclipse.org/acceleo/mt/2.6.0/statements",
 			"http://www.eclipse.org/acceleo/traceability/1.0", "http://www.eclipse.org/acceleo/mt/2.6.0",
 			"http:///org/eclipse/emf/examples/library/extlibrary.ecore/1.0.0",
 			"http://www.cs.tum.edu/cope/migration/test", "http://www.eclipse.org/uml2/1.1.0/GenModel",
 			"http://www.eclipse.org/acceleo/mt/2.6.0/expressions", "http://www.eclipse.org/acceleo/mtl/3.0/",
 			"http://www.eclipse.org/emf/CDO/Eresource/2.0.0",
 			"http://www.eclipse.org/emf/eef/mapping/navigation/1.0.0",
 			"http://www.eclipse.org/emf/eef/views/toolkits/1.0.0", "http://www.eclipse.org/acceleo/mtl/cst/3.0",
 			"http://www.eclipse.org/emf/eef/generation/1.0.0", "http://www.eclipse.org/emf/2009/Validation",
 			"http://www.eclipse.org/emf/eef/mapping/1.0.0", "http://www.eclipse.org/emf/compare/diff/1.1",
 			"http://www.eclipse.org/acceleo/mt/2.6.0/core", "http://www.eclipse.org/gmf/2008/mappings",
 			"http://www.w3.org/XML/1998/namespace", "http://www.eclipse.org/ocl/1.1.0/OCL",
 			"http://www.eclipse.org/ocl/1.1.0/OCL/Expressions", "http://www.eclipse.org/emf/2002/GenModel",
 			"http://www.eclipse.org/qvt/1.0/ImperativeOCL", "http://unicase.org/emfstore/esmodel",
 			"http://www.cs.tum.edu/cope/migration", "http://unicase.org/esmodel/accesscontrol/roles",
 			"http:///com/ibm/etools/dtd.ecore", "http:///www.eclipse.org/m2m/qvt/operational/trace.ecore",
 			"http://www.eclipse.org/qvt/1.0.0/Operational/Expressions", "http://www.eclipse.org/gmf/2006/GenModel",
 			"http://www.eclipse.org/uml2/3.0.0/UML", "http://www.eclipse.org/uml2/2.0.0/UML",
 			"http://www.eclipse.org/gmf/runtime/1.0.0/notation", "http://www.eclipse.org/emf/2002/Tree",
 			"http://www.eclipse.org/OCL2/1.0.0/ocl/uml", "http://www.eclipse.org/OCL2/1.0.0/ocl/types",
 			"http://unicase.org/esmodel/versioning/events", "http://www.eclipse.org/emf/2003/Change",
 			"http://www.eclipse.org/OCL2/1.0.0/ocl/query", "http://www.eclipse.org/gmf/2006/Trace",
 			"http://www.eclipse.org/QVT2/1.0.0/Operational/cst", "http://unicase.org/esmodel/accesscontrol",
 			"http://www.eclipse.org/emf/2005/Ecore2XML", "http://unicase.org/esmodel/versioning/operations",
 			"http://www.eclipse.org/emf/2002/Ecore", "http://www.eclipse.org/QVT2/1.0.0/Operational/cst/temp",
 			"http://www.eclipse.org/gmf/2005/GenModel/2.0", "http://www.eclipse.org/gmf/2005/ToolDefinition",
 			"http://unicase.org/esmodel/versioning", "http://www.cs.tum.edu/cope/declaration",
 			"http://www.eclipse.org/gmf/2005/GenModel", "http://www.eclipse.org/gmf/2005/mappings",
 			"http://unicase.org/workspace", "http://www.eclipse.org/gmf/2006/GraphicalDefinition",
 			"http://www.eclipse.org/gmf/runtime/1.0.1/notation", "http://www.eclipse.org/emf/2003/XMLType",
 			"http://www.eclipse.org/uml2/2.1.0/UML", "http://www.eclipse.org/gmf/2005/mappings/2.0",
 			"http://www.eclipse.org/ocl/1.1.0/OCL/Types", "http://www.eclipse.org/QVT/1.0.0/Operational",
 			"http://www.eclipse.org/gmf/runtime/1.0.2/notation", "http://www.eclipse.org/ocl/1.1.0/OCL/Utilities",
 			"moduleCore.xmi", "http://www.eclipse.org/emf/2002/XSD2Ecore", "http://unicase.org/workspaceModel",
 			"http://www.eclipse.org/emf/2004/Ecore2Ecore", "http://www.eclipse.org/gmf/2005/GraphicalDefinition",
 			"http://www.eclipse.org/gmf/2009/GenModel", "http://www.eclipse.org/xsd/2002/XSD",
 			"http://www.eclipse.org/emf/2002/Mapping", "http://unicase.org/metamodel",
 			"http://www.eclipse.org/gmf/2008/GenModel", "DTD.xmi", "http://www.eclipse.org/ocl/1.1.0/OCL/CST",
 			"http://www.eclipse.org/OCL2/1.0.0/ocl/expressions", "componentcore.xmi",
 			"http://www.eclipse.org/ocl/1.1.0/Ecore", "http://unicase.org/esmodel/notification",
 			"http://www.eclipse.org/OCL2/1.0.0/oclstdlib", "http://unicase.org/esmodel/versioning/events/server/",
 			"http://unicase.org/emfstore/esmodel/url", "http://www.eclipse.org/gmf/2006/mappings",
 			"http://www.eclipse.org/OCL2/1.0.0/ocl", "http://unicase.org/esmodel/versioning/operations/semantic",
 			"http://www.eclipse.org/OCL2/1.0.0/ocl/utilities", "http://www.eclipse.org/uml2/2.2.0/GenModel",
 			"http://eclipse.org/emf/emfstore/server/model/versioning", "http://eclipse.org/emf/emfstore/server/model/url",
 			"http://eclipse.org/emf/emfstore/client/model", "urn:model.ecore", "http://eclipse.org/emf/emfstore/server/model/notification",
 			"http://eclipse.org/emf/ecp/common/model/workspaceModel", "http://eclipse.org/emf/emfstore/server/model/versioning/events",
 			"http://eclipse.org/emf/emfstore/server/model/roles", "http://eclipse.org/emf/emfstore/server/model/versioning/operations", 
 			"http://eclipse.org/emf/emfstore/server/model/versioning/events/server/", "http://eclipse.org/emf/emfstore/server/model",
 			"http://eclipse.org/emf/emfstore/server/model/accesscontrol", "http://eclipse.org/emf/emfstore/common/model",
 			"http://eclipse.org/emf/emfstore/server/model/versioning/operations/semantic"};
 		Set<String> knownPackages = new HashSet<String>(Arrays.asList(elements));
 		return knownPackages.contains(key);
 	}
 
 	/**
 	 * Retrieve all EClasses from the Ecore package that are model element subclasses.
 	 * 
 	 * @param ePackage the package to get the classes from
 	 * @return a set of EClasses
 	 */
 	private static Set<EClass> getAllModelElementEClasses(EPackage ePackage) {
 		Set<EClass> result = new HashSet<EClass>();
 		for (EPackage subPackage : ePackage.getESubpackages()) {
 			result.addAll(getAllModelElementEClasses(subPackage));
 		}
 		for (EClassifier classifier : ePackage.getEClassifiers()) {
 			if (classifier instanceof EClass) {
 				EClass subEClass = (EClass) classifier;
 				result.add(subEClass);
 			}
 		}
 		return result;
 	}
 }
