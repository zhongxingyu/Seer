 /**
  * Copyright (c) 2011 modelversioning.org
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  */
 package org.modelversioning.emfprofileapplication.validation;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.ECollections;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.modelversioning.emfprofile.Extension;
 import org.modelversioning.emfprofile.Stereotype;
 import org.modelversioning.emfprofileapplication.ProfileApplication;
 import org.modelversioning.emfprofileapplication.StereotypeApplication;
 
 /**
  * Checks for a violation of the upper bound constraint of a {@link Stereotype}.
  * 
  * @author <a href="mailto:langer@big.tuwien.ac.at">Philip Langer</a>
  * 
  */
 public class UpperBoundConstraintValidator {
 
 	private final ProfileApplication profileApplication;
 	private EList<UpperBoundConstraintViolation> violations = new BasicEList<UpperBoundConstraintViolation>();
 
 	public UpperBoundConstraintValidator(ProfileApplication profileApplication) {
 		this.profileApplication = profileApplication;
 	}
 
	public EList<UpperBoundConstraintViolation> getViolations() {
 		validateProfileApplication();
 		return ECollections.unmodifiableEList(violations);
 	}
 
 	private void validateProfileApplication() {
 		EList<EObject> annotatedObjects = profileApplication
 				.getAnnotatedObjects();
 		for (EObject annotatedObject : annotatedObjects) {
 			checkAnnotatedObject(annotatedObject);
 		}
 	}
 
 	private void checkAnnotatedObject(EObject annotatedObject) {
 		EList<StereotypeApplication> stereotypeApplications = profileApplication
 				.getStereotypeApplications(annotatedObject);
 		for (StereotypeApplication stereotypeApplication : stereotypeApplications) {
 			Extension extension = stereotypeApplication.getExtension();
 			if (extension.getUpperBound() != -1
 					&& !isUpperBoundOk(extension, stereotypeApplications)) {
 				addViolatingStereotypeApplication(stereotypeApplication);
 			}
 		}
 	}
 
 	private void addViolatingStereotypeApplication(
 			StereotypeApplication stereotypeApplication) {
 		if (!alreadyReported(stereotypeApplication)) {
 			violations.add(new UpperBoundConstraintViolation(
 					stereotypeApplication));
 		}
 	}
 
 	private boolean alreadyReported(StereotypeApplication stereotypeApplication) {
 		for (UpperBoundConstraintViolation violation : violations) {
			if (EcoreUtil.equals(violation.getExtension(),
					stereotypeApplication.getExtension())
					&& EcoreUtil.equals(violation.getModelObject(),
							stereotypeApplication.getAppliedTo())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isUpperBoundOk(Extension extension,
 			EList<StereotypeApplication> stereotypeApplications) {
 		EList<Extension> usedExtensions = extractUsedExtensions(
 				stereotypeApplications, extension);
 		if (usedExtensions.size() > extension.getUpperBound()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	private EList<Extension> extractUsedExtensions(
 			EList<StereotypeApplication> stereotypeApplications,
 			Extension extension) {
 		EList<Extension> extensions = new BasicEList<Extension>();
 		for (StereotypeApplication stereotypeApplication : stereotypeApplications) {
 			if (extension.equals(stereotypeApplication.getExtension())) {
 				extensions.add(stereotypeApplication.getExtension());
 			}
 		}
 		return extensions;
 	}
 
 }
