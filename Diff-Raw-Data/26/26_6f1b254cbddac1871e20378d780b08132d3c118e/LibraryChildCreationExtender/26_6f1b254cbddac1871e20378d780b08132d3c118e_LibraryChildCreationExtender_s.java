 /*******************************************************************************
  * Copyright (c) Aug 3, 2011 NetXForge.
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
 package com.netxforge.netxstudio.screens.editing.actions;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.edit.domain.EditingDomain;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.edit.EditUtils;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.Lifecycle;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.Function;
 import com.netxforge.netxstudio.library.LibraryFactory;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.operators.Node;
 
 /**
  * Custom child extender, capable to add conditional ( Type etc...) child
  * descriptors It works best, by disabling the generated EMF descriptors.
  * 
  * Note: This is called for all EObject of the specified extender package, so
  * first check the target eclass.
  * 
  * @author Christophe Bouhier
  */
 public class LibraryChildCreationExtender extends
 		AbstractConditionalChildCreationExtender {
 
 	protected ModelUtils modelUtils;
 
 	@Inject
 	public LibraryChildCreationExtender(ModelUtils modelUtils) {
 		this.modelUtils = modelUtils;
 	}
 
 	public Collection<?> getNewChildDescriptors(Object object,
 			EditingDomain editingDomain) {
 		Collection<Object> newChildDescriptors = Lists.newArrayList();
 
 		if (object instanceof EObject) {
 			EObject target = (EObject) object;
 
 			if (target instanceof Component || target instanceof NodeType) {
 
 				// Are we in the design view? (The target must have a parent
 				// Node).
 				Node node = modelUtils.nodeFor(target);
				if (node != null && node.getOriginalNodeTypeRef() != null) {
					NodeType ntRef = node.getOriginalNodeTypeRef();
 					if (target.eClass() == LibraryPackage.Literals.NODE_TYPE) {
 						// Handled by the Node child creation has NodeType is
 						// not visible in the Design view.
 					} else if (target.eClass() == LibraryPackage.Literals.EQUIPMENT) {
						Equipment targetEq = (Equipment) target;
						newChildDescriptors = newEquimentDescriptorsForTargetNodeType(
								editingDomain, ntRef, targetEq);
 
 					} else if (target.eClass() == LibraryPackage.Literals.FUNCTION) {
 
 						// Allow creation of Function object, not from the
 						// original type.
 						Function function = LibraryFactory.eINSTANCE
 								.createFunction();
 
 						Lifecycle newLC = GenericsFactory.eINSTANCE
 								.createLifecycle();
 						newLC.setProposed(modelUtils.toXMLDate(modelUtils
 								.todayAndNow()));
 						function.setLifecycle(newLC);
 
 						newChildDescriptors.add(createChildParameter(
 								LibraryPackage.Literals.FUNCTION__FUNCTIONS,
 								function));
 					}
 				} else {
 
 					if (target instanceof NodeType) {
 						Function createFunction = newLibraryFunction(
 								editingDomain, target,
 								LibraryPackage.Literals.NODE_TYPE__FUNCTIONS);
 
 						Equipment createEquipment = newLibraryEquipment(
 								editingDomain, target,
 								LibraryPackage.Literals.NODE_TYPE__EQUIPMENTS);
 
 						newChildDescriptors.add(createChildParameter(
 								LibraryPackage.Literals.NODE_TYPE__FUNCTIONS,
 								createFunction));
 						newChildDescriptors.add(createChildParameter(
 								LibraryPackage.Literals.NODE_TYPE__EQUIPMENTS,
 								createEquipment));
 					} else if (target instanceof Equipment) {
 
 						Equipment createEquipment = newLibraryEquipment(
 								editingDomain, target,
 								LibraryPackage.Literals.EQUIPMENT__EQUIPMENTS);
 
 						newChildDescriptors.add(createChildParameter(
 								LibraryPackage.Literals.EQUIPMENT__EQUIPMENTS,
 								createEquipment));
 
 					} else if (target instanceof Function) {
 						Function createFunction = newLibraryFunction(
 								editingDomain, target,
 								LibraryPackage.Literals.FUNCTION__FUNCTIONS);
 
 						newChildDescriptors.add(createChildParameter(
 								LibraryPackage.Literals.FUNCTION__FUNCTIONS,
 								createFunction));
 					}
 				}
 			}
 		}
 
 		return newChildDescriptors;
 	}
 
 	/**
 	 * A new function with a calculated name.
 	 * 
 	 * @param target
 	 * @param collectionFeature
 	 * @return
 	 */
 	private Function newLibraryFunction(EditingDomain domain, EObject target,
 			EReference collectionFeature) {
 		Function createFunction = LibraryFactory.eINSTANCE.createFunction();
 		String newSequenceNumber = EditUtils.INSTANCE.nextSequenceNumber(
 				domain, target, collectionFeature,
 				LibraryPackage.Literals.COMPONENT__NAME);
 
 		createFunction.setName(newSequenceNumber);
 		return createFunction;
 	}
 
 	/**
 	 * A new Equipment with a calculated code.
 	 * 
 	 * @param target
 	 * @param collectionFeature
 	 * @return
 	 */
 	private Equipment newLibraryEquipment(EditingDomain domain, EObject target,
 			EReference collectionFeature) {
 		Equipment createEquipment = LibraryFactory.eINSTANCE.createEquipment();
 		String newSequenceNumber = EditUtils.INSTANCE.nextSequenceNumber(
 				domain, target, collectionFeature,
 				LibraryPackage.Literals.EQUIPMENT__EQUIPMENT_CODE);
 		createEquipment.setEquipmentCode(newSequenceNumber);
 		return createEquipment;
 	}
 
 	private Collection<Object> newEquimentDescriptorsForTargetNodeType(
 			EditingDomain domain, NodeType nodeType, Equipment targetEq) {
 		// find the object from the ref and return all possible
 		// creation references.
 		// TODO, possibly optimize, by filtering non Equipments.
 
 		Collection<Object> newChildDescriptors = Lists.newArrayList();
 
 		TreeIterator<EObject> it = nodeType.eAllContents();
 		while (it.hasNext()) {
 			EObject next = it.next();
 			if (next.eClass() == LibraryPackage.Literals.EQUIPMENT) {
 
 				// Match on equipmentcode.
 				Equipment eq = (Equipment) next;
 				if (eq.getEquipmentCode().equals(targetEq.getEquipmentCode())) {
 					// get children and build descriptors.
 					EList<EObject> directContent = next.eContents();
 					for (EObject targetChild : directContent) {
 						if (targetChild instanceof Equipment) {
 
 							Equipment eqCopy = (Equipment) EcoreUtil
 									.copy(targetChild);
 
 							// Set the name as a sequence.
 							String newSequenceNumber = EditUtils.INSTANCE
 									.nextSequenceNumber(
 											domain,
 											targetEq,
 											LibraryPackage.Literals.EQUIPMENT__EQUIPMENTS,
 											LibraryPackage.Literals.COMPONENT__NAME);
 							eqCopy.setName(newSequenceNumber);
 
 							Lifecycle newLC = GenericsFactory.eINSTANCE
 									.createLifecycle();
 							newLC.setProposed(modelUtils.toXMLDate(modelUtils
 									.todayAndNow()));
 							eqCopy.setLifecycle(newLC);
 
 							newChildDescriptors
 									.add(createChildParameter(
 											LibraryPackage.Literals.EQUIPMENT__EQUIPMENTS,
 											eqCopy));
 						}
 					}
 				}
 			}
 		}
 
 		return newChildDescriptors;
 
 	}
 
 	/**
 	 * It copies the component, for the referenced resources we create a copy of
 	 * the resource and store it in it's own CDOResource. For this we open a
 	 * transaction.
 	 * 
 	 * @param resourceSet
 	 * @param component
 	 * @return
 	 */
 	@SuppressWarnings("unused")
 	private Component copyComponentWithResources(Component component) {
 
 		@SuppressWarnings("serial")
 		EcoreUtil.Copier nodeTypeCopier = new EcoreUtil.Copier() {
 
 			/**
 			 * Our version of copy reference has a special treatment for
 			 * NetXResource object.
 			 */
 			@Override
 			protected void copyReference(EReference eReference,
 					EObject eObject, EObject copyEObject) {
 
 				if (eReference == LibraryPackage.Literals.COMPONENT__RESOURCE_REFS) {
 					copyResourceReference(eReference, eObject, copyEObject);
 				} else {
 					super.copyReference(eReference, eObject, copyEObject);
 				}
 			}
 
 			protected void copyResourceReference(EReference eReference,
 					EObject eObject, EObject copyEObject) {
 				if (eObject.eIsSet(eReference) && eReference.isMany()) {
 					@SuppressWarnings("unchecked")
 					InternalEList<EObject> source = (InternalEList<EObject>) eObject
 							.eGet(eReference);
 					@SuppressWarnings("unchecked")
 					InternalEList<EObject> target = (InternalEList<EObject>) copyEObject
 							.eGet(getTarget(eReference));
 					if (source.isEmpty()) {
 						target.clear();
 					} else {
 						boolean isBidirectional = eReference.getEOpposite() != null;
 						int index = 0;
 						for (Iterator<EObject> k = resolveProxies ? source
 								.iterator() : source.basicIterator(); k
 								.hasNext();) {
 							EObject referencedEObject = k.next();
 							EObject copyReferencedEObject = get(referencedEObject);
 							if (copyReferencedEObject == null) {
 								if (useOriginalReferences) {
 									// NetXResource is a bidi link, so
 									// make an actual copy (A copier
 									// within a copier... auch).
 									if (isBidirectional) {
 										EcoreUtil.Copier defaultCopier = new EcoreUtil.Copier();
 										EObject newEObject = defaultCopier
 												.copy(referencedEObject);
 
 										if (copyEObject instanceof Component) {
 											// bug 235, component name must
 											// exist.
 											if (copyEObject instanceof Equipment) {
 												((Component) copyEObject)
 														.setName("<name>");
 											}
 
 											// FIXME.
 											// we don't know where to store this
 											// non-contained reference????
 
 										}
 
 										target.addUnique(index, newEObject);
 										index++;
 									} else {
 										target.addUnique(index,
 												referencedEObject);
 										++index;
 									}
 								}
 							} else {
 
 								// This would actually do what?
 								if (isBidirectional) {
 									int position = target
 											.indexOf(copyReferencedEObject);
 									if (position == -1) {
 										target.addUnique(index,
 												copyReferencedEObject);
 									} else if (index != position) {
 										target.move(index,
 												copyReferencedEObject);
 									}
 								} else {
 									target.addUnique(index,
 											copyReferencedEObject);
 								}
 								++index;
 							}
 						}
 					}
 				}
 			}
 		};
 
 		Component copyOfComponent = (Component) nodeTypeCopier.copy(component);
 		nodeTypeCopier.copyReferences();
 		return copyOfComponent;
 	}
 
 }
