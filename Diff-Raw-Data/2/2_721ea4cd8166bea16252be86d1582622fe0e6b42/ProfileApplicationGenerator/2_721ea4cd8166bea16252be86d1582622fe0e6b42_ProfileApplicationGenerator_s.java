 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.configuration.profile;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
 import org.modelexecution.xmof.configuration.ConfigurationObjectMap;
 import org.modelexecution.xmof.vm.IXMOFVirtualMachineListener;
 import org.modelexecution.xmof.vm.XMOFBasedModel;
 import org.modelexecution.xmof.vm.XMOFVirtualMachine;
 import org.modelexecution.xmof.vm.XMOFVirtualMachineEvent;
 import org.modelexecution.xmof.vm.XMOFVirtualMachineEvent.Type;
 import org.modelexecution.xmof.vm.internal.XMOFInstanceMap;
 import org.modelversioning.emfprofile.IProfileFacade;
 import org.modelversioning.emfprofile.Profile;
 import org.modelversioning.emfprofile.Stereotype;
 import org.modelversioning.emfprofile.impl.ProfileFacadeImpl;
 import org.modelversioning.emfprofileapplication.StereotypeApplication;
 
 import fUML.Semantics.Classes.Kernel.BooleanValue;
 import fUML.Semantics.Classes.Kernel.EnumerationValue;
 import fUML.Semantics.Classes.Kernel.ExtensionalValue;
 import fUML.Semantics.Classes.Kernel.ExtensionalValueList;
 import fUML.Semantics.Classes.Kernel.FeatureValue;
 import fUML.Semantics.Classes.Kernel.IntegerValue;
 import fUML.Semantics.Classes.Kernel.Link;
 import fUML.Semantics.Classes.Kernel.LinkList;
 import fUML.Semantics.Classes.Kernel.Object_;
 import fUML.Semantics.Classes.Kernel.Reference;
 import fUML.Semantics.Classes.Kernel.StringValue;
 import fUML.Semantics.Classes.Kernel.Value;
 import fUML.Syntax.Classes.Kernel.Association;
 import fUML.Syntax.Classes.Kernel.Class_;
 import fUML.Syntax.Classes.Kernel.EnumerationLiteral;
 import fUML.Syntax.Classes.Kernel.Property;
 import fUML.Syntax.Classes.Kernel.StructuralFeature;
 
 public class ProfileApplicationGenerator implements IXMOFVirtualMachineListener {
 
 	private XMOFBasedModel model;
 	private Collection<Profile> configurationProfiles;
 	private ConfigurationObjectMap configurationMap;
 	private ResourceSet resourceSet;
 	private URI profileApplicationURI;
 	private Resource profileApplicationResource;
 	private IProfileFacade facade;
 	private XMOFInstanceMap instanceMap;
 	private Copier copier = new Copier();
 
 	public ProfileApplicationGenerator(XMOFBasedModel model,
 			Collection<Profile> configurationProfiles,
 			ConfigurationObjectMap configurationMap, XMOFInstanceMap instanceMap) {
 		this.model = model;
 		this.configurationProfiles = configurationProfiles;
 		this.configurationMap = configurationMap;
 		this.instanceMap = instanceMap;
 	}
 
 	@Override
 	public void notify(XMOFVirtualMachineEvent event) {
 		if (Type.STOP.equals(event.getType())) {
 			try {
 				generateProfileApplication(event.getVirtualMachine());
 			} catch (IOException e) {
 				XMOFConfigurationProfilePlugin.log(e);
 			}
 		}
 	}
 
 	private void generateProfileApplication(XMOFVirtualMachine virtualMachine)
 			throws IOException {
		if (profileApplicationResource == null) {
 			return;
 		}
 		prepareProfileFacade();
 		createStereotypeApplications(virtualMachine);
 		saveProfileApplication();
 	}
 
 	private void prepareProfileFacade() throws IOException {
 		facade = new ProfileFacadeImpl();
 		profileApplicationResource = facade.loadProfileApplication(
 				profileApplicationURI, resourceSet);
 		for (Profile profile : configurationProfiles) {
 			facade.makeApplicable(profile);
 			facade.loadProfile(profile);
 		}
 	}
 
 	private void createStereotypeApplications(XMOFVirtualMachine virtualMachine) {
 		XMOFInstanceMap instanceMap = virtualMachine.getInstanceMap();
 		for (ExtensionalValue value : instanceMap.getExtensionalValues()) {
 			if (value instanceof Object_) {
 				createStereotypeApplication((Object_) value, instanceMap);
 			}
 		}
 	}
 
 	private void createStereotypeApplication(Object_ object,
 			XMOFInstanceMap instanceMap) {
 		EObject confObject = instanceMap.getEObject(object);
 		EObject eObject = configurationMap.getOriginalObject(confObject);
 		Stereotype confStereotype = getConfigurationStereotype(confObject);
 		if (confObject == null || confStereotype == null) {
 			return;
 		}
 		if (shouldApply(confStereotype)
 				&& facade.isApplicable(confStereotype, eObject)) {
 			StereotypeApplication application = facade.apply(confStereotype,
 					eObject);
 			for (EStructuralFeature feature : confStereotype
 					.getEStructuralFeatures()) {
 				Object value = getValue(object, feature);
 				if (value != null) {
 					facade.setTaggedValue(application, feature, value);
 				}
 			}
 		}
 	}
 
 	private boolean shouldApply(Stereotype confStereotype) {
 		return confStereotype.getTaggedValues().size() > 0;
 	}
 
 	private Stereotype getConfigurationStereotype(EObject eObject) {
 		EClass confClass = eObject.eClass();
 		for (Profile profile : configurationProfiles) {
 			// TODO use extension base class to decide and not the name
 			Stereotype stereotype = profile.getStereotype(confClass.getName()
 					+ "Stereotype");
 			if (stereotype != null)
 				return stereotype;
 		}
 		return null;
 	}
 
 	private Object getValue(Object_ object, EStructuralFeature feature) {
 		for (Iterator<FeatureValue> iterator = object.featureValues.iterator(); iterator
 				.hasNext();) {
 			FeatureValue featureValue = iterator.next();
 			if (featureValue.feature.name.equals(feature.getName())) {
 				if (feature instanceof EAttribute) {
 					return getAttributeValue(featureValue, (EAttribute) feature);
 				} else if (feature instanceof EReference) {
 					return getReferenceValue(object, (EReference) feature,
 							featureValue);
 				}
 			}
 		}
 		return null;
 	}
 
 	private Object getReferenceValue(Object_ object, EReference reference,
 			FeatureValue featureValue) {
 		Association association = getAssociation(featureValue);
 		Collection<Object_> linkedObjects = getLinkedObjects(association,
 				featureValue.feature, object);
 		EList<Object> linkedObjectsOriginal = new BasicEList<Object>();
 		for (Object_ o : linkedObjects) {
 			EObject confobject = instanceMap.getEObject(o);
 			if (confobject != null) {
 				EObject originalobject = configurationMap
 						.getOriginalObject(confobject);
 				if (originalobject != null) {
 					linkedObjectsOriginal.add(originalobject);
 				} else if (reference.isContainment()) {
 					EObject confobjectcopy = copier.copy(confobject);
 					linkedObjectsOriginal.add(confobjectcopy);
 					createReferencesForCopiedEObject(confobject, confobjectcopy);
 				}
 			} else { // new object was created
 				EObject newEObject = createEObject(o);
 				linkedObjectsOriginal.add(newEObject);
 			}
 		}
 		if (linkedObjectsOriginal.size() > 0) {
 			if (reference.isMany()) {
 				return linkedObjectsOriginal;
 			} else {
 				return linkedObjectsOriginal.get(0);
 			}
 		}
 		return null;
 	}
 
 	private void createReferencesForCopiedEObject(EObject confobject,
 			EObject confobjectcopy) {
 		for (EReference eReference : confobject.eClass().getEAllReferences()) {
 			if (!eReference.isContainment()) {
 				Object referencedEObjects = getReferencedObjects(confobject,
 						eReference);
 				if (referencedEObjects != null) {
 					confobjectcopy.eSet(eReference, referencedEObjects);
 				}
 			} else {
 				Object referenced = confobject.eGet(eReference);
 				if (referenced instanceof EList<?>) {
 					EList<?> referencedEObjects = (EList<?>) referenced;
 					for (Object o : referencedEObjects) {
 						if (o instanceof EObject) {
 							createReferencesForCopiedEObject((EObject) o,
 									copier.get(o));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private Object getReferencedObjects(EObject eObject, EReference eReference) {
 		EList<EObject> referencedObjects = new BasicEList<EObject>();
 
 		Object referencedObjectsInRuntime = eObject.eGet(eReference);
 		if (referencedObjectsInRuntime instanceof EList<?>) {
 			referencedObjects.addAll(getReferencedObjectsOfRequiredType(
 					(EList<?>) referencedObjectsInRuntime, eReference));
 		} else if (referencedObjectsInRuntime instanceof EObject) {
 			EObject referencedObjectOfRequiredType = getObjectOfRequiredType(
 					(EObject) referencedObjectsInRuntime, eReference.getEType());
 			if (referencedObjectOfRequiredType != null) {
 				referencedObjects.add(referencedObjectOfRequiredType);
 			}
 		}
 		if (referencedObjects.size() > 0) {
 			if (eReference.isMany()) {
 				return referencedObjects;
 			} else {
 				return referencedObjects.get(0);
 			}
 		}
 		return null;
 	}
 
 	private EList<EObject> getReferencedObjectsOfRequiredType(
 			EList<?> referencedEObjects, EReference eReference) {
 		EList<EObject> referencedObjectsOfRequiredType = new BasicEList<EObject>();
 		for (Object o : referencedEObjects) {
 			if (o instanceof EObject) {
 				EObject referencedObjectOfRequiredType = getObjectOfRequiredType(
 						(EObject) o, eReference.getEType());
 				if (referencedObjectOfRequiredType != null) {
 					referencedObjectsOfRequiredType
 							.add(referencedObjectOfRequiredType);
 				}
 			}
 		}
 		return referencedObjectsOfRequiredType;
 	}
 
 	private EObject getObjectOfRequiredType(EObject eObject, EClassifier type) {
 		if (eObject.eClass().equals(type)) {
 			return eObject;
 		} else {
 			EObject originalObject = configurationMap
 					.getOriginalObject(eObject);
 			if (originalObject.eClass().equals(type)) {
 				return originalObject;
 			}
 		}
 		return null;
 	}
 
 	private EObject createEObject(Object_ object) {
 		Class_ class_ = object.types.get(0);
 		EClass eClass = instanceMap.getEClass(class_);
 
 		EObject eObject = EcoreUtil.create(eClass);
 		for (EStructuralFeature feature : eClass.getEStructuralFeatures()) {
 			Object value = getValue(object, feature);
 			if (value != null) {
 				eObject.eSet(feature, value);
 			}
 		}
 		return eObject;
 	}
 
 	private Association getAssociation(FeatureValue featureValue) {
 		Association association = null;
 		StructuralFeature structuralFeature = featureValue.feature;
 		if (structuralFeature instanceof Property) {
 			association = ((Property) structuralFeature).association;
 		}
 		return association;
 	}
 
 	private Collection<Object_> getLinkedObjects(Association association,
 			StructuralFeature end, Object_ referent) {
 
 		Property oppositeEnd = association.memberEnd.getValue(0);
 		if (oppositeEnd == end) {
 			oppositeEnd = association.memberEnd.getValue(1);
 		}
 
 		ExtensionalValueList extent = referent.locus.getExtent(association);
 
 		LinkList links = new LinkList();
 		for (int i = 0; i < extent.size(); i++) {
 			ExtensionalValue link = extent.getValue(i);
 			Value linkValue = link.getFeatureValue(oppositeEnd).values
 					.getValue(0);
 			if (linkValue instanceof Reference) {
 				linkValue = ((Reference) linkValue).referent;
 			}
 			// if (linkValue.equals(referent)) {
 			if (linkValue == referent) {
 				if (!end.multiplicityElement.isOrdered | links.size() == 0) {
 					links.addValue((Link) link);
 				} else {
 					int n = link.getFeatureValue(end).position;
 					boolean continueSearching = true;
 					int j = 0;
 					while (continueSearching & j < links.size()) {
 						j = j + 1;
 						continueSearching = links.getValue(j - 1)
 								.getFeatureValue(end).position < n;
 					}
 					if (!continueSearching) {
 						links.addValue(j - 1, (Link) link);
 					} else {
 						links.add((Link) link);
 					}
 				}
 			}
 		}
 
 		Collection<Object_> linkedObjects = new HashSet<Object_>();
 		for (Link link : links) {
 			FeatureValue fv = link.getFeatureValue(end);
 			Value v = fv.values.get(0);
 			if (v instanceof Object_) {
 				linkedObjects.add((Object_) v);
 			} else if (v instanceof Reference) {
 				linkedObjects.add(((Reference) v).referent);
 			}
 		}
 
 		return linkedObjects;
 	} // getMatchingLinks
 
 	private Object getAttributeValue(FeatureValue featureValue,
 			EAttribute eAttribute) {
 		if (!eAttribute.isMany()) {
 			EDataType attType = eAttribute.getEAttributeType();
 			if (featureValue.values.isEmpty()) {
 				return null;
 			}
 			Value value = featureValue.values.get(0);
 			if (isEBooleanType(attType)) {
 				return ((BooleanValue) value).value;
 			} else if (isEIntType(attType)) {
 				return (int) ((IntegerValue) value).value;
 			} else if (isEStringType(attType)) {
 				return ((StringValue) value).value;
 			} else if (isCustomEEnumType(attType)) {
 				EnumerationValue enumerationValue = (EnumerationValue) value;
 				EnumerationLiteral literal = enumerationValue.literal;
 				EEnum eEnum = (EEnum) attType;
 				return eEnum.getEEnumLiteral(literal.name);
 			}
 		} else {
 			// TODO handle multivalued attribute values
 		}
 		return null;
 	}
 
 	private boolean isEBooleanType(EDataType valueType) {
 		return EcorePackage.eINSTANCE.getEBoolean().equals(valueType);
 	}
 
 	private boolean isEIntType(EDataType valueType) {
 		return EcorePackage.eINSTANCE.getEInt().equals(valueType);
 	}
 
 	private boolean isEStringType(EDataType valueType) {
 		return EcorePackage.eINSTANCE.getEString().equals(valueType);
 	}
 
 	private boolean isCustomEEnumType(EDataType valueType) {
 		return valueType instanceof EEnum;
 	}
 
 	private void saveProfileApplication() throws IOException {
 		facade.save();
 	}
 
 	public Resource getProfileApplicationResource() {
 		return profileApplicationResource;
 	}
 
 	public void setResourceSet(ResourceSet resourceSet) {
 		this.resourceSet = resourceSet;
 	}
 
 	public void setProfileApplicationURI(URI profileApplicationURI) {
 		this.profileApplicationURI = profileApplicationURI;
 	}
 
 	public XMOFBasedModel getModel() {
 		return model;
 	}
 
 	public Collection<Profile> getConfigurationProfiles() {
 		return configurationProfiles;
 	}
 
 }
