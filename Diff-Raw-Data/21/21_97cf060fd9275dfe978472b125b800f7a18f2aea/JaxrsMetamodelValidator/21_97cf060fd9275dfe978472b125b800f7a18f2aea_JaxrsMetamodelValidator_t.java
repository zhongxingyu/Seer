 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.validation;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.wst.validation.AbstractValidator;
 import org.eclipse.wst.validation.ValidationResult;
 import org.eclipse.wst.validation.ValidationState;
 import org.eclipse.wst.validation.ValidatorMessage;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsMetamodelBuilder;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsBaseElement;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsMetamodel;
import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResource;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.ConstantUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
import org.jboss.tools.ws.jaxrs.core.metamodel.EnumElementKind;
import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsResource;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelLocator;
 
 public class JaxrsMetamodelValidator extends AbstractValidator {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.wst.validation.AbstractValidator#validate(org.eclipse.core
 	 * .resources.IResource, int, org.eclipse.wst.validation.ValidationState,
 	 * org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public ValidationResult validate(IResource resource, int kind, ValidationState state, IProgressMonitor monitor) {
 		final ValidationResult validationResult = new ValidationResult();
 		final IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
 		try {
 			subMonitor.beginTask("Validating the JAX-RS Metamodel", 1);
 			if (resource.getType() == IResource.FILE && "java".equals(resource.getFileExtension())) {
 				clearMarkers((IFile) resource);
 				final JaxrsMetamodel jaxrsMetamodel = JaxrsMetamodelLocator.get(resource.getProject());
 				if (jaxrsMetamodel == null) {
 					return validationResult;
 				}
				List<JaxrsBaseElement> elements = jaxrsMetamodel.getElements(JdtUtils.getCompilationUnit(resource));
				for(JaxrsBaseElement element : elements) {
					if (element.getElementKind() == EnumElementKind.RESOURCE) {
						Logger.debug("Validating the JAX-RS Metamodel after {} was {}", resource.getName(),
								ConstantUtils.getStaticFieldName(IResourceDelta.class, kind));
						List<ValidatorMessage> validationMessages = element.validate();
						for (ValidatorMessage validationMessage : validationMessages) {
							validationResult.add(validationMessage);
						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 			Logger.error("Failed to validate the resource change", e);
 		} finally {
 			subMonitor.done();
 		}
 		return validationResult;
 	}
 
 	private void clearMarkers(IFile file) {
 		try {
 			file.deleteMarkers(JaxrsMetamodelBuilder.JAXRS_PROBLEM, true,
 					org.eclipse.core.resources.IResource.DEPTH_INFINITE);
 		} catch (CoreException e) {
 		}
 	}
 
 }
