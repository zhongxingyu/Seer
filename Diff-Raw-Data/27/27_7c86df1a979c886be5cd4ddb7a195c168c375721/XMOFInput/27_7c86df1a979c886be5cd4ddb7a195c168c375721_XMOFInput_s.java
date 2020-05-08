 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fuml.convert.xmof.internal;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 
 /**
  * Represents the input of a xMOF to fUML conversion and obtains its elements to
  * be converted.
  * 
  * @author Philip Langer (langer@big.tuwien.ac.at)
  * 
  */
 public class XMOFInput {
 
 	private Object originalInput;
 	private Collection<EObject> eObjectsToConvert;
 	private Collection<Activity> allActivities;
 
 	public XMOFInput(Object input) {
 		super();
 		this.originalInput = input;
 		eObjectsToConvert = deriveEObjectsToConvertFromInput();
 	}
 
 	public Object getOriginalInput() {
 		return originalInput;
 	}
 
 	private Collection<EObject> deriveEObjectsToConvertFromInput() {
 		if (originalInput instanceof EObject) {
 			return getEObjectsToConvertFromEObject((EObject) originalInput);
 		} else if (originalInput instanceof Resource) {
 			return getEObjectsToConvertFromResource((Resource) originalInput);
 		} else {
 			return Collections.emptyList();
 		}
 	}
 
 	private Collection<EObject> getEObjectsToConvertFromEObject(EObject eObject) {
 		if (eObject.eResource() != null) {
 			return getEObjectsToConvertFromResource(eObject.eResource());
 		} else {
 			return Arrays.asList((new EObject[] { EcoreUtil.getRootContainer(
 					eObject, true) }));
 		}
 	}
 
 	private Collection<EObject> getEObjectsToConvertFromResource(
 			Resource eResource) {
 		if (eResource.getResourceSet() != null) {
 			return getEObjectsToConvertFromResourceSet(eResource
 					.getResourceSet());
 		} else {
 			return eResource.getContents();
 		}
 	}
 
 	private Collection<EObject> getEObjectsToConvertFromResourceSet(
 			ResourceSet resourceSet) {
 		Collection<EObject> eObjectsToConvert = new HashSet<EObject>();
 		Collection<Resource> resources = getAllResources(resourceSet);
 		for (Resource resource : resources) {
 			eObjectsToConvert.addAll(resource.getContents());
 		}
 		return eObjectsToConvert;
 	}
 
 	private Collection<Resource> getAllResources(ResourceSet resourceSet) {
 		Collection<Resource> resources = new HashSet<Resource>();
 		resources.addAll(resourceSet.getResources());
        for(Resource r : resourceSet.getResources()){
        	for(Iterator<EObject> j = r.getAllContents(); j.hasNext(); ){
        		for(Object object : j.next().eCrossReferences()){
        			EObject eObject = (EObject)object;
        			Resource otherResource = eObject.eResource();
        			if(otherResource != null && !resources.contains(otherResource)){
        				resources.add(otherResource);
        			}
        		}
        	}
        }
        return resources;
 	}
 
 	public boolean containsActivities() {
 		return !getAllActivites().isEmpty();
 	}
 
 	public Collection<EModelElement> getElementsToConvert() {
 		Collection<EModelElement> elementsToConvert = new HashSet<EModelElement>();
 		for (EObject eObjectToConvert : eObjectsToConvert) {
 			if (eObjectToConvert instanceof EModelElement) {
 				elementsToConvert.add((EModelElement) eObjectToConvert);
 			}
 		}
 		return elementsToConvert;
 	}
 
 	public Collection<Activity> getMainActivities() {
 		// TODO only take activity that is in MainEClass.classifierBehavior
 		Collection<Activity> mainActivities = new HashSet<Activity>();
 		if (originalInput instanceof Activity) {
 			mainActivities.add((Activity) originalInput);
 		} else {
 			mainActivities.addAll(getAllActivites());
 		}
 		return mainActivities;
 	}
 
 	private Collection<Activity> getAllActivites() {
 		if (allActivities == null) {
 			allActivities = new HashSet<Activity>();
 			for (EObject eObject : eObjectsToConvert) {
 				if (isActivity(eObject)) {
 					allActivities.add((Activity) eObject);
 				}
 				allActivities.addAll(getContainedActivities(eObject));
 			}
 		}
 		return allActivities;
 	}
 
 	private boolean isActivity(EObject eObject) {
 		return eObject instanceof Activity;
 	}
 
 	private Collection<Activity> getContainedActivities(EObject eObject) {
 		Collection<Activity> containedActivities = new HashSet<Activity>();
 		for (TreeIterator<EObject> contents = eObject.eAllContents(); contents
 				.hasNext();) {
 			EObject child = contents.next();
 			if (isActivity(child)) {
 				containedActivities.add((Activity) child);
 			}
 		}
 		return containedActivities;
 	}
 
 }
