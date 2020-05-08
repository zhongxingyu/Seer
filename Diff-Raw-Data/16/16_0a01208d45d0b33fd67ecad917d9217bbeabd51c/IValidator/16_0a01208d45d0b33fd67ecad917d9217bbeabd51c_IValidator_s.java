  /*******************************************************************************
   * Copyright (c) 2007 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.kb.validation;
 
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper;
 import org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager;
 
 /**
  * Represents a validator that is managed by ValidatorManager.
  * @author Alexey Kazakov
  */
 public interface IValidator {
 
 	public static final String KB_PROBLEM_MARKER_TYPE = "org.jboss.tools.jst.web.kb.kbproblemmarker"; //$NON-NLS-1$
 	public static final String MARKED_RESOURCE_MESSAGE_GROUP = "markedKbResource"; //$NON-NLS-1$
 	public static final String RESOURCE_MESSAGE_ID = "org.jboss.tools.kb.problem"; //$NON-NLS-1$
 	public static final String EXTENSION_POINT_ID = "org.jboss.tools.jst.web.kb.validator"; //$NON-NLS-1$
 
 	/**
 	 * Incremental Validation
 	 * @return
 	 * @throws ValidationException
 	 */
 	IStatus validate(Set<IFile> changedFiles, IProject project, ContextValidationHelper validationHelper, IProjectValidationContext validationContext, ValidatorManager manager, IReporter reporter) throws ValidationException;
 
 	/**
 	 * Full Validation
 	 * @return
 	 * @throws ValidationException
 	 */
 	IStatus validateAll(IProject project, ContextValidationHelper validationHelper, IProjectValidationContext validationContext, ValidatorManager manager, IReporter reporter) throws ValidationException;
 
 	/**
 	 * @return unique ID of the validator
 	 */
 	String getId();
 
 	/**
 	 * @param project
 	 * @return @return a set of projects which should be validated together with the given project.
 	 */
 	IValidatingProjectTree getValidatingProjects(IProject project);
 
 	/**
 	 * @param project
 	 * @return true if this validator should validate the given project.
 	 */
 	boolean shouldValidate(IProject project);
 
 	/**
 	 * Returns "true" if this validator is enabled in the preferences store. 
 	 * @param project
 	 * @return
 	 */
 	boolean isEnabled(IProject project);
 }
