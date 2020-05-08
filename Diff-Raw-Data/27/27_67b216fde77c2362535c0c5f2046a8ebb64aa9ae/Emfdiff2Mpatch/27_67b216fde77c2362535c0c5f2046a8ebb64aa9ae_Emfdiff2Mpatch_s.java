 /*******************************************************************************
  * Copyright (c) 2010, 2011 Technical University of Denmark.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors:
  *    Patrick Koenemann, DTU Informatics - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.mpatch.emfdiff2mpatch.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.compare.diff.metamodel.AttributeChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.MoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
 import org.eclipse.emf.compare.diff.metamodel.UpdateReference;
 import org.eclipse.emf.compare.mpatch.ChangeGroup;
 import org.eclipse.emf.compare.mpatch.IndepAddAttributeChange;
 import org.eclipse.emf.compare.mpatch.IndepAddElementChange;
 import org.eclipse.emf.compare.mpatch.IndepAddReferenceChange;
 import org.eclipse.emf.compare.mpatch.IndepChange;
 import org.eclipse.emf.compare.mpatch.IndepMoveElementChange;
 import org.eclipse.emf.compare.mpatch.IndepRemoveAttributeChange;
 import org.eclipse.emf.compare.mpatch.IndepRemoveElementChange;
 import org.eclipse.emf.compare.mpatch.IndepRemoveReferenceChange;
 import org.eclipse.emf.compare.mpatch.IndepUpdateAttributeChange;
 import org.eclipse.emf.compare.mpatch.IndepUpdateReferenceChange;
 import org.eclipse.emf.compare.mpatch.MPatchFactory;
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.UnknownChange;
 import org.eclipse.emf.compare.mpatch.common.util.CommonUtils;
 import org.eclipse.emf.compare.mpatch.emfdiff2mpatch.lib.MPatchLibrary;
 import org.eclipse.emf.compare.mpatch.emfdiff2mpatch.lib.MPatchLibraryComponents;
 import org.eclipse.emf.compare.mpatch.extension.IModelDescriptorCreator;
 import org.eclipse.emf.compare.mpatch.extension.ISymbolicReferenceCreator;
 import org.eclipse.emf.compare.mpatch.util.ExtEcoreUtils;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * The transformation from the emfdiff format (EMF Compare) to MPatch.
  * 
  * Originally, this transformation was specified with QVT Operational Mappings (see /transforms/emfdiff2mpatch.qvto),
  * but due to critical dependencies, the entire transformation has been re-written in pure Java.
  * 
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  * 
  */
 public class Emfdiff2Mpatch {
 
 	/** The blackbox library that is used to create symbolic references and model descriptors. */
 	protected final MPatchLibrary lib = new MPatchLibrary();
 
 	/** Logging. */
 	private StringBuffer log;
 
 	/**
 	 * Create an instance of the transformation. Make sure that the {@link MPatchLibrary} is configured correctly by
 	 * setting a {@link ISymbolicReferenceCreator} and {@link IModelDescriptorCreator} via the interface
 	 * {@link MPatchLibraryComponents}!
 	 */
 	public Emfdiff2Mpatch() {
 		if (MPatchLibraryComponents.getModelDescriptorCreator() == null)
 			throw new IllegalStateException("There is no model descriptor creator configured for the MPatchLibrary! "
 					+ "Please specify one before initializing this transformation!");
 		if (MPatchLibraryComponents.getSymbolicReferenceCreator() == null)
 			throw new IllegalStateException("There is no symbolic reference creator configured for the MPatchLibrary! "
 					+ "Please specify one before initializing this transformation!");
 	}
 
 	/** Append msg to log. Optional obj will be appended. */
 	protected void log(String msg, Object obj) {
 		if (log != null) {
 			final String time = String.format("[%1$tF %1$tT]: ", Calendar.getInstance());
 			if (obj != null)
 				log.append(time + msg + "  - " + obj);
 			else
 				log.append(time + msg);
 		}
 	}
 
 	/**
 	 * Execute the actual transformation from emfdiff to mpatch. If output is set, some logging is stored in the string
 	 * buffer.
 	 * 
 	 * @param emfdiff
 	 *            The input emfdiff.
 	 * @param output
 	 *            A string buffer for debugging output; may be <code>null</code>.
 	 * @return The resulting MPatch.
 	 */
 	public MPatchModel transform(ComparisonSnapshot emfdiff, StringBuffer output) {
 		this.log = output;
 		log("starting transformation...", emfdiff);
 
 		// get the DiffModel
 		final List<EObject> diffModels = ExtEcoreUtils.collectTypedElements(Collections.singletonList(emfdiff),
 				Collections.singleton(DiffPackage.Literals.DIFF_MODEL), true);
 		if (diffModels == null || diffModels.size() != 1) {
 			log("Only one DiffModel is supported at the moment! Aborting transformation...", diffModels);
 			throw new IllegalArgumentException(
 					"At the moment, only emfdiffs with exactly one DiffModel contained are supported! Found: "
 							+ diffModels);
 		}
 		final DiffModel diffModel = (DiffModel) diffModels.get(0);
 
 		// call main mapping
 		final MPatchModel mpatch = toMPatchModel(diffModel);
 
 		log("transformation finished!", mpatch);
 		return mpatch;
 	}
 
 	/**
 	 * main mapping: an emfdiff diffmodel is mapped to an mpatch
 	 */
 	protected MPatchModel toMPatchModel(DiffModel diffModel) {
 		// lets check whether we got a diff from two models
 		if (diffModel.getLeftRoots().isEmpty() || diffModel.getRightRoots().isEmpty()) {
 			log("leftRoots and rightRoots must not be empty in DiffModel! Aborting transformation...", diffModel);
 			throw new IllegalArgumentException("Expecting leftRoots and rightRoots non-empty in the DiffModel!");
 		}
 		if (!diffModel.getAncestorRoots().isEmpty()) {
 			log("ancestorRoots must be empty in DiffModel! Aborting transformation...", diffModel);
 			throw new IllegalArgumentException("Expecting ancestorRoots to be empty in the DiffModel!");
 		}
 
 		// create the mpatch and set properties
 		final MPatchModel mpatch = MPatchFactory.eINSTANCE.createMPatchModel();
 		final List<String> leftUris = new ArrayList<String>();
 		final List<String> rightUris = new ArrayList<String>();
 		for (EObject left : diffModel.getLeftRoots())
 			leftUris.add(lib.toUriString(left.eResource()));
 		for (EObject right : diffModel.getRightRoots())
 			rightUris.add(lib.toUriString(right.eResource()));
 		mpatch.setNewModel(CommonUtils.join(leftUris, " "));
 		mpatch.setOldModel(CommonUtils.join(rightUris, " "));
 		mpatch.setEmfdiff(lib.toUriString(diffModel.eResource()));
 
 		// get all relevant diff elements and transform them
 		final List<EObject> diffElements = ExtEcoreUtils.collectTypedElements(diffModel.getOwnedElements(),
 				Collections.singleton(DiffPackage.Literals.DIFF_ELEMENT), true);
 		for (EObject diffElement : diffElements) {
			if (!(diffElement instanceof DiffGroup)) {
 				mpatch.getChanges().add(toIndepChange((DiffElement) diffElement));
 			}
 		}
 		log(mpatch.getChanges().size() + " DiffElements transformed.", null);
 		return mpatch;
 	}
 
 	/**
 	 * mapping for abstract DiffElement:
 	 * <ul>
 	 * <li>if the DiffElement is of a concrete sub type for which another mapping exists (e.g.
 	 * DiffGroup::toIndepChange), then the other mapping is called; however, after the init-section of the other
 	 * mapping, this mapping is called! the reason is that here we can add additional logic for all transformations,
 	 * e.g. logging.
 	 * <li>if it is called for an unknown type, the entire transformation is aborted with an appropriate log message.
 	 * </ul>
 	 */
 	protected IndepChange toIndepChange(DiffElement diffElement) {
 		log("  transforming DiffElement", diffElement);
 		
 		// in contrast to qvto, we have to check the type explicitly :-(
 		if (diffElement instanceof DiffGroup)
 			return toIndepChange((DiffGroup) diffElement);
 		if (diffElement instanceof ConflictingDiffElement)
 			return toIndepChange((ConflictingDiffElement) diffElement);
 		if (diffElement instanceof MoveModelElement)
 			return toIndepChange((MoveModelElement) diffElement);
 		if (diffElement instanceof ModelElementChangeRightTarget)
 			return toIndepChange((ModelElementChangeRightTarget) diffElement);
 		if (diffElement instanceof ModelElementChangeLeftTarget)
 			return toIndepChange((ModelElementChangeLeftTarget) diffElement);
 		if (diffElement instanceof UpdateAttribute)
 			return toIndepChange((UpdateAttribute) diffElement);
 		if (diffElement instanceof AttributeChangeRightTarget)
 			return toIndepChange((AttributeChangeRightTarget) diffElement);
 		if (diffElement instanceof AttributeChangeLeftTarget)
 			return toIndepChange((AttributeChangeLeftTarget) diffElement);
 		if (diffElement instanceof UpdateReference)
 			return toIndepChange((UpdateReference) diffElement);
 		if (diffElement instanceof ReferenceChangeRightTarget)
 			return toIndepChange((ReferenceChangeRightTarget) diffElement);
 		if (diffElement instanceof ReferenceChangeLeftTarget)
 			return toIndepChange((ReferenceChangeLeftTarget) diffElement);
 		return toUnknownChange(diffElement);
 	}
 
 	/**
 	 * mapping for all unknown change types.
 	 */
 	protected IndepChange toUnknownChange(DiffElement diffElement) {
 		final UnknownChange change = MPatchFactory.eINSTANCE.createUnknownChange();
 		change.setInfo("[" + diffElement.eClass().getName() + "]: \"" + diffElement + "\"");
 		return change;
 	}
 
 	/**
 	 * mapping for groups: currently not used because we create a different structure in the mpatch later.
 	 */
 	protected IndepChange toIndepChange(DiffGroup diffElement) {
 		final ChangeGroup group = MPatchFactory.eINSTANCE.createChangeGroup();
 		if (diffElement.getRightParent() != null) // in some diff groups, the parent is not set.
 			group.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightParent()));
 		return group;
 	}
 
 	/**
 	 * mapping for conflicting elements: abort transformation with an appropriate log message.
 	 */
 	protected IndepChange toIndepChange(ConflictingDiffElement diffElement) {
 		log("Conflicting Diff Element found! Aborting transformation...", diffElement);
 		throw new IllegalArgumentException(
 				"Conflicting elements are not supported! Please use this transformation on non-conflicting diffs only! "
 						+ diffElement);
 	}
 
 	/**
 	 * mapping for moved elements
 	 */
 	protected IndepChange toIndepChange(MoveModelElement diffElement) {
 		final IndepMoveElementChange indepChange = MPatchFactory.eINSTANCE.createIndepMoveElementChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setOldContainment(diffElement.getRightElement().eContainmentFeature());
 		indepChange.setNewContainment(diffElement.getLeftElement().eContainmentFeature());
 
 		/*
 		 * for whatever reason, EMF Compare refers to the left target as the old parent in the new model, and the right
 		 * target points to the new parent in the old model!
 		 */
 		// oldParent := self.leftTarget.toSymbolicReference();
 		// newParent := self.rightTarget.toSymbolicReference();
 
 		// fix: directly get the container instead of relying on the targets of EMF Compare
 		indepChange.setOldParent(lib.toSymbolicReference(diffElement.getRightElement().eContainer()));
 		indepChange.setNewParent(lib.toSymbolicReference(diffElement.getLeftElement().eContainer()));
 		return indepChange;
 	}
 
 	/**
 	 * mapping for removed elements
 	 */
 	protected IndepChange toIndepChange(ModelElementChangeRightTarget diffElement) {
 		final IndepRemoveElementChange indepChange = MPatchFactory.eINSTANCE.createIndepRemoveElementChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement().eContainer()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftParent()));
 		indepChange.setContainment(diffElement.getRightElement().eContainmentFeature());
 		indepChange.setSubModel(lib.toModelDescriptor(diffElement.getRightElement(), true));
 		return indepChange;
 	}
 
 	/**
 	 * mapping for added elements
 	 */
 	protected IndepChange toIndepChange(ModelElementChangeLeftTarget diffElement) {
 		final IndepAddElementChange indepChange = MPatchFactory.eINSTANCE.createIndepAddElementChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightParent()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement().eContainer()));
 		indepChange.setContainment(diffElement.getLeftElement().eContainmentFeature());
 		indepChange.setSubModel(lib.toModelDescriptor(diffElement.getLeftElement(), true));
 		return indepChange;
 	}
 
 	/**
 	 * mapping for changed attributes
 	 */
 	protected IndepChange toIndepChange(UpdateAttribute diffElement) {
 		final IndepUpdateAttributeChange indepChange = MPatchFactory.eINSTANCE.createIndepUpdateAttributeChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setChangedAttribute(diffElement.getAttribute());
 		indepChange.setOldValue(diffElement.getRightElement().eGet(diffElement.getAttribute()));
 		indepChange.setNewValue(diffElement.getLeftElement().eGet(diffElement.getAttribute()));
 		return indepChange;
 	}
 
 	/**
 	 * mapping for removed attributes (i.e. the cardinality of this attribute is >1)
 	 */
 	protected IndepChange toIndepChange(AttributeChangeRightTarget diffElement) {
 		final IndepRemoveAttributeChange indepChange = MPatchFactory.eINSTANCE.createIndepRemoveAttributeChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setChangedAttribute(diffElement.getAttribute());
 		indepChange.setValue(diffElement.getRightTarget());
 		return indepChange;
 	}
 
 	/**
 	 * mapping for added attributes (i.e. the cardinality of this attribute is >1)
 	 */
 	protected IndepChange toIndepChange(AttributeChangeLeftTarget diffElement) {
 		final IndepAddAttributeChange indepChange = MPatchFactory.eINSTANCE.createIndepAddAttributeChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setChangedAttribute(diffElement.getAttribute());
 		indepChange.setValue(diffElement.getLeftTarget());
 		return indepChange;
 	}
 
 	/**
 	 * mapping for a changed reference
 	 */
 	protected IndepChange toIndepChange(UpdateReference diffElement) {
 		final IndepUpdateReferenceChange indepChange = MPatchFactory.eINSTANCE.createIndepUpdateReferenceChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setReference(diffElement.getReference());
 
 		// unfortunately we need to figure out from the _model_, whether a reference is null
 		final Object rightTarget = diffElement.getRightElement().eGet(diffElement.getReference());
 		final Object leftTarget = diffElement.getLeftElement().eGet(diffElement.getReference());
 		if (rightTarget != null)
 			indepChange.setOldReference(lib.toSymbolicReference((EObject) rightTarget));
 		if (leftTarget != null)
 			indepChange.setNewReference(lib.toSymbolicReference((EObject) leftTarget));
 
 		return indepChange;
 	}
 
 	/**
 	 * mapping for removed references (i.e. the cardinality of this reference is >1)
 	 */
 	protected IndepChange toIndepChange(ReferenceChangeRightTarget diffElement) {
 		final IndepRemoveReferenceChange indepChange = MPatchFactory.eINSTANCE.createIndepRemoveReferenceChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setReference(diffElement.getReference());
 		indepChange.setChangedReference(lib.toSymbolicReference(diffElement.getRightTarget()));
 		return indepChange;
 	}
 
 	/**
 	 * mapping for added references (i.e. the cardinality of this reference is >1)
 	 */
 	protected IndepChange toIndepChange(ReferenceChangeLeftTarget diffElement) {
 		final IndepAddReferenceChange indepChange = MPatchFactory.eINSTANCE.createIndepAddReferenceChange();
 		indepChange.setCorrespondingElement(lib.toSymbolicReference(diffElement.getRightElement()));
 		indepChange.setResultingElement(lib.toSymbolicReference(diffElement.getLeftElement()));
 		indepChange.setReference(diffElement.getReference());
 		indepChange.setChangedReference(lib.toSymbolicReference(diffElement.getLeftTarget()));
 		return indepChange;
 	}
 }
