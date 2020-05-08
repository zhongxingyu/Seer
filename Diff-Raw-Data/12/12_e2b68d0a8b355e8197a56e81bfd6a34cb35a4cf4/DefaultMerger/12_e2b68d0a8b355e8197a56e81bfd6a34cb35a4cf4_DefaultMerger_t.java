 /*******************************************************************************
  * Copyright (c) 2006, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.merge;
 
 import org.eclipse.emf.compare.diff.merge.service.MergeService;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChange;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeRightTarget;
import org.eclipse.emf.compare.diff.metamodel.UpdateReference;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EGenericType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 
 /**
  * Basic implementation of an {@link IMerger}. Clients can extend this class instead of implementing IMerger
  * to avoid reimplementing all methods.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class DefaultMerger implements IMerger {
 	/** {@link DiffElement} to be merged by this merger. */
 	protected DiffElement diff;
 
 	/** Keeps a reference on the left resource for this merger. */
 	@Deprecated
 	protected Resource leftResource;
 
 	/** Keeps a reference on the right resource for this merger. */
 	@Deprecated
 	protected Resource rightResource;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.merge.IMerger#applyInOrigin()
 	 */
 	public void applyInOrigin() {
 		handleMutuallyDerivedReferences();
 		ensureXMIIDCopied();
 		removeFromContainer(diff);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.merge.IMerger#canApplyInOrigin()
 	 */
 	public boolean canApplyInOrigin() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.merge.IMerger#canUndoInTarget()
 	 */
 	public boolean canUndoInTarget() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.merge.IMerger#setDiffElement(org.eclipse.emf.compare.diff.metamodel.DiffElement)
 	 */
 	public void setDiffElement(DiffElement element) {
 		diff = element;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.merge.IMerger#undoInTarget()
 	 */
 	public void undoInTarget() {
 		handleMutuallyDerivedReferences();
 		ensureXMIIDCopied();
 		removeFromContainer(diff);
 	}
 
 	/**
 	 * Removes the given {@link DiffGroup} from its container if it was its last child, also calls for the
 	 * same cleanup operation on its hierarchy.
 	 * 
 	 * @param diffGroup
 	 *            {@link DiffGroup} we want to cleanup.
 	 */
 	protected void cleanDiffGroup(DiffGroup diffGroup) {
 		if (diffGroup != null && diffGroup.getSubDiffElements().size() == 0) {
 			final EObject parent = diffGroup.eContainer();
 			if (parent instanceof DiffGroup) {
 				EcoreUtil.remove(diffGroup);
 				cleanDiffGroup((DiffGroup)parent);
 			}
 		}
 	}
 
 	/**
 	 * Creates a copy of the given EObject as would {@link EcoreUtil#copy(EObject)} would, except we use
 	 * specific handling for unmatched references.
 	 * 
 	 * @param eObject
 	 *            The object to copy.
 	 * @return the copied object.
 	 */
 	protected EObject copy(EObject eObject) {
 		final EMFCompareEObjectCopier copier = MergeService.getCopier(diff);
 		final EObject result = copier.copy(eObject);
 		copier.copyReferences();
 		copier.copyXMIIDs();
 		return result;
 	}
 
 	/**
 	 * This can be called after a merge operation to ensure that all objects created by the operation share
 	 * the same XMI ID as their original.
 	 * <p>
 	 * Implemented because of bug 351591 : some of the objects we copy mays not have been added to a resource
 	 * when we check their IDs. We thus need to wait till the merge operation has completed.
 	 * </p>
 	 * 
 	 * @since 1.3
 	 */
 	protected void ensureXMIIDCopied() {
 		final EMFCompareEObjectCopier copier = MergeService.getCopier(diff);
 		copier.copyXMIIDs();
 	}
 
 	/**
 	 * Returns the {@link DiffModel} containing the {@link DiffElement} this merger is intended to merge.
 	 * 
 	 * @return The {@link DiffModel} containing the {@link DiffElement} this merger is intended to merge.
 	 */
 	protected DiffModel getDiffModel() {
 		EObject container = diff.eContainer();
 		while (container != null) {
 			if (container instanceof DiffModel)
 				return (DiffModel)container;
 			container = container.eContainer();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the XMI ID of the given {@link EObject} or <code>null</code> if it cannot be resolved.
 	 * 
 	 * @param object
 	 *            Object which we seek the XMI ID of.
 	 * @return <code>object</code>'s XMI ID, <code>null</code> if not applicable.
 	 */
 	protected String getXMIID(EObject object) {
 		String objectID = null;
 		if (object != null && object.eResource() instanceof XMIResource) {
 			objectID = ((XMIResource)object.eResource()).getID(object);
 		}
 		return objectID;
 	}
 
 	/**
 	 * Removes all references to the given {@link EObject} from the {@link DiffModel}.
 	 * 
 	 * @param deletedObject
 	 *            Object to remove all references to.
 	 */
 	protected void removeDanglingReferences(EObject deletedObject) {
 		// EObject root = EcoreUtil.getRootContainer(deletedObject);
 		// if (root instanceof ComparisonResourceSnapshot) {
 		// root = ((ComparisonResourceSnapshot)root).getDiff();
 		// }
 		// if (root != null) {
 		// // FIXME performance, find a way to cache this referencer
 		// final Resource res = root.eResource();
 		// final EcoreUtil.CrossReferencer referencer;
 		// if (res != null && res.getResourceSet() != null) {
 		// referencer = new EcoreUtil.CrossReferencer(res.getResourceSet()) {
 		// private static final long serialVersionUID = 616050158241084372L;
 		//
 		// // initializer for this anonymous class
 		// {
 		// crossReference();
 		// }
 		//
 		// @Override
 		// protected boolean crossReference(EObject eObject, EReference eReference,
 		// EObject crossReferencedEObject) {
 		// if (eReference.isChangeable() && !eReference.isDerived())
 		// return crossReferencedEObject.eResource() == null;
 		// return false;
 		// }
 		// };
 		// } else if (res != null) {
 		// referencer = new EcoreUtil.CrossReferencer(res) {
 		// private static final long serialVersionUID = 616050158241084372L;
 		//
 		// // initializer for this anonymous class
 		// {
 		// crossReference();
 		// }
 		//
 		// @Override
 		// protected boolean crossReference(EObject eObject, EReference eReference,
 		// EObject crossReferencedEObject) {
 		// if (eReference.isChangeable() && !eReference.isDerived())
 		// return crossReferencedEObject.eResource() == null;
 		// return false;
 		// }
 		// };
 		// } else {
 		// referencer = new EcoreUtil.CrossReferencer(root) {
 		// private static final long serialVersionUID = 616050158241084372L;
 		//
 		// // initializer for this anonymous class
 		// {
 		// crossReference();
 		// }
 		//
 		// @Override
 		// protected boolean crossReference(EObject eObject, EReference eReference,
 		// EObject crossReferencedEObject) {
 		// if (eReference.isChangeable() && !eReference.isDerived())
 		// return crossReferencedEObject.eResource() == null;
 		// return false;
 		// }
 		// };
 		// }
 		// final Iterator<Map.Entry<EObject, Collection<EStructuralFeature.Setting>>> i = referencer
 		// .entrySet().iterator();
 		// while (i.hasNext()) {
 		// final Map.Entry<EObject, Collection<EStructuralFeature.Setting>> entry = i.next();
 		// final Iterator<EStructuralFeature.Setting> j = entry.getValue().iterator();
 		// while (j.hasNext()) {
 		// EcoreUtil.remove(j.next(), entry.getKey());
 		// }
 		// }
 		// }
 	}
 
 	/**
 	 * Removes a {@link DiffElement} from its {@link DiffGroup}.
 	 * 
 	 * @param diffElement
 	 *            {@link DiffElement} to remove from its container.
 	 */
 	protected void removeFromContainer(DiffElement diffElement) {
 		final EObject parent = diffElement.eContainer();
 		EcoreUtil.remove(diffElement);
 		removeDanglingReferences(parent);
 
 		// If diff was contained by a ConflictingDiffElement, we call back this on it
 		if (parent instanceof ConflictingDiffElement) {
 			removeFromContainer((DiffElement)parent);
 		}
 
 		// if diff was in a diffGroup and it was the last one, we also remove the diffgroup
 		if (parent instanceof DiffGroup) {
 			cleanDiffGroup((DiffGroup)parent);
 		}
 	}
 
 	/**
 	 * Sets the XMI ID of the given {@link EObject} if it belongs in an {@link XMIResource}.
 	 * 
 	 * @param object
 	 *            Object we want to set the XMI ID of.
 	 * @param id
 	 *            XMI ID to give to <code>object</code>.
 	 */
 	protected void setXMIID(EObject object, String id) {
 		if (object != null && object.eResource() instanceof XMIResource) {
 			((XMIResource)object.eResource()).setID(object, id);
 		}
 	}
 
 	/**
 	 * Mutually derived references need specific handling : merging one will implicitely merge the other and
 	 * there are no way to tell such references apart.
 	 * <p>
 	 * Currently known references raising such issues :
 	 * <table>
 	 * <tr>
 	 * <td>{@link EcorePackage#ECLASS__ESUPER_TYPES}</td>
 	 * <td>{@link EcorePackage#ECLASS__EGENERIC_SUPER_TYPES}</td>
 	 * </tr>
 	 * </table>
 	 * </p>
 	 */
 	private void handleMutuallyDerivedReferences() {
 		DiffElement toRemove = null;
 		if (diff instanceof ReferenceChange) {
 			final EReference reference = ((ReferenceChange)diff).getReference();
 			if (reference == EcorePackage.eINSTANCE.getEClass_ESuperTypes()) {
				EObject referenceType = null;
 				if (diff instanceof ReferenceChangeLeftTarget) {
 					referenceType = ((ReferenceChangeLeftTarget)diff).getRightTarget();
				} else if (diff instanceof ReferenceChangeRightTarget) {
 					referenceType = ((ReferenceChangeRightTarget)diff).getLeftTarget();
				} else if (diff instanceof UpdateReference) {
					referenceType = ((UpdateReference)diff).getLeftTarget();
				} else {
					// we did cover all the subclasses, we should have a RferenceOrderChange
 				}
 				for (final DiffElement siblingDiff : ((DiffGroup)diff.eContainer()).getSubDiffElements()) {
 					if (siblingDiff instanceof ModelElementChangeLeftTarget) {
 						if (((ModelElementChangeLeftTarget)siblingDiff).getLeftElement() instanceof EGenericType
 								&& ((EGenericType)((ModelElementChangeLeftTarget)siblingDiff)
 										.getLeftElement()).getEClassifier() == referenceType) {
 							toRemove = siblingDiff;
 							break;
 						}
 					} else if (siblingDiff instanceof ModelElementChangeRightTarget) {
 						if (((ModelElementChangeRightTarget)siblingDiff).getRightElement() instanceof EGenericType
 								&& ((EGenericType)((ModelElementChangeRightTarget)siblingDiff)
 										.getRightElement()).getEClassifier() == referenceType) {
 							toRemove = siblingDiff;
 							break;
 						}
 					}
 				}
 			}
 		} else if (diff instanceof ModelElementChangeLeftTarget
 				&& ((ModelElementChangeLeftTarget)diff).getLeftElement() instanceof EGenericType) {
 			final ModelElementChangeLeftTarget theDiff = (ModelElementChangeLeftTarget)diff;
 			final EClassifier referenceType = ((EGenericType)theDiff.getLeftElement()).getEClassifier();
 			for (final DiffElement siblingDiff : ((DiffGroup)diff.eContainer()).getSubDiffElements()) {
 				if (siblingDiff instanceof ReferenceChangeLeftTarget
 						&& ((ReferenceChangeLeftTarget)siblingDiff).getReference().getFeatureID() == EcorePackage.ECLASS__ESUPER_TYPES) {
 					if (((ReferenceChangeLeftTarget)siblingDiff).getRightTarget() == referenceType) {
 						toRemove = siblingDiff;
 						break;
 					}
 				}
 			}
 		} else if (diff instanceof ModelElementChangeRightTarget
 				&& ((ModelElementChangeRightTarget)diff).getRightElement() instanceof EGenericType) {
 			final ModelElementChangeRightTarget theDiff = (ModelElementChangeRightTarget)diff;
 			final EClassifier referenceType = ((EGenericType)theDiff.getRightElement()).getEClassifier();
 			for (final DiffElement siblingDiff : ((DiffGroup)diff.eContainer()).getSubDiffElements()) {
 				if (siblingDiff instanceof ReferenceChangeRightTarget
 						&& ((ReferenceChangeRightTarget)siblingDiff).getReference().getFeatureID() == EcorePackage.ECLASS__ESUPER_TYPES) {
 					if (((ReferenceChangeRightTarget)siblingDiff).getLeftTarget() == referenceType) {
 						toRemove = siblingDiff;
 						break;
 					}
 				}
 			}
 		}
 		if (toRemove != null) {
 			removeFromContainer(toRemove);
 		}
 	}
 
 }
