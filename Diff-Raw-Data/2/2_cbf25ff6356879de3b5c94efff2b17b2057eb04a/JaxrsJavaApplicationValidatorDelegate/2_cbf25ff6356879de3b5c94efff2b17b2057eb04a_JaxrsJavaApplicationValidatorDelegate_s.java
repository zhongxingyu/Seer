 /******************************************************************************* 
  * Copyright (c) 2012 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.validation;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.ISourceRange;
 import org.eclipse.jdt.core.IType;
 import org.jboss.tools.common.validation.TempMarkerManager;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsJavaApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
 import org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname;
 import org.jboss.tools.ws.jaxrs.core.metamodel.quickfix.JaxrsValidationQuickFixes;
 import org.jboss.tools.ws.jaxrs.core.preferences.JaxrsPreferences;
 
 /**
  * Java-based JAX-RS Application validator
  * 
  * @author Xavier Coulon
  * 
  */
 public class JaxrsJavaApplicationValidatorDelegate extends AbstractJaxrsElementValidatorDelegate<JaxrsJavaApplication> {
 
 	public JaxrsJavaApplicationValidatorDelegate(TempMarkerManager markerManager, JaxrsJavaApplication application) {
 		super(markerManager, application);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.ws.jaxrs.core.internal.metamodel.validation.AbstractJaxrsElementValidatorDelegate#validate()
 	 */
 	@Override
 	public void validate() throws CoreException {
 		final JaxrsJavaApplication application = getElement();
 		deleteJaxrsMarkers(application.getResource());
 		
 		final Annotation applicationPathAnnotation = application
 				.getAnnotation(EnumJaxrsClassname.APPLICATION_PATH.qualifiedName);
 		final IType appJavaElement = application.getJavaElement();
 		if (!application.isOverriden() && applicationPathAnnotation == null) {
 			addProblem(JaxrsValidationMessages.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION,
 					JaxrsPreferences.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION, new String[0],
					appJavaElement.getSourceRange().getLength(), appJavaElement.getSourceRange().getOffset(),
 					application.getResource(),
 					JaxrsValidationQuickFixes.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION_ID);
 		}
 		if (!application.isJaxrsCoreApplicationSubclass()) {
 			addProblem(JaxrsValidationMessages.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY,
 					JaxrsPreferences.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY,
 					new String[] { appJavaElement.getFullyQualifiedName() }, application.getJavaElement()
 							.getSourceRange().getLength(), appJavaElement.getSourceRange().getOffset(),
 					application.getResource(), JaxrsValidationQuickFixes.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY_ID);
 		}
 
 		if (application.getMetamodel().hasMultipleApplications()) {
 			final IResource javaResource = application.getResource();
 			ISourceRange javaNameRange = application.getJavaElement().getNameRange();
 			if (javaNameRange == null) {
 				Logger.warn("Cannot add a problem marker: unable to locate '"
 						+ application.getJavaElement().getElementName() + "' in resource '"
 						+ application.getJavaElement().getResource().getFullPath().toString() + "'. ");
 			} else {
 				addProblem(JaxrsValidationMessages.APPLICATION_TOO_MANY_OCCURRENCES,
 						JaxrsPreferences.APPLICATION_TOO_MANY_OCCURRENCES, new String[0], 
 						javaNameRange.getLength(), javaNameRange.getOffset(), javaResource);
 			}
 		}
 
 	}
 
 }
