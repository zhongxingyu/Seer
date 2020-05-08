 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.validators.core;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.validators.internal.core.ValidatorsCore;
 
 public abstract class AbstractBuildParticipantType extends
 		AbstractValidatorType {
 
 	private final String id;
 	private final String name;
 
 	protected AbstractBuildParticipantType(String id, String name) {
 		this.id = id;
 		this.name = name;
 		final ValidatorStub validator = new ValidatorStub(id, name, this);
 		validators.put(validator.getID(), validator);
 	}
 
 	public IValidator createValidator(String id) {
 		throw new UnsupportedOperationException();
 	}
 
 	public String getID() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean isBuiltin() {
 		return true;
 	}
 
 	public boolean isConfigurable() {
		return true;
 	}
 
 	public boolean supports(Class validatorType) {
 		return IBuildParticipant.class.equals(validatorType);
 	}
 
 	private static class ValidatorStub extends AbstractValidator {
 
 		/**
 		 * @param id
 		 * @param name
 		 */
 		public ValidatorStub(String id, String name, IValidatorType type) {
 			super(id, name, type);
 		}
 
 		public Object getValidator(IScriptProject project, Class validatorType) {
 			if (IBuildParticipant.class.equals(validatorType)) {
 				try {
 					final AbstractBuildParticipantType type = getOwner();
 					return type.createBuildParticipant(project);
 				} catch (CoreException e) {
 					ValidatorsCore.error("createBuildParticipant", e); //$NON-NLS-1$
 				}
 			}
 			return null;
 		}
 
 		private AbstractBuildParticipantType getOwner() {
 			return (AbstractBuildParticipantType) getValidatorType();
 		}
 
 		public boolean isValidatorValid(IScriptProject project) {
 			return true;
 		}
 
 	}
 
 	protected abstract IBuildParticipant createBuildParticipant(
 			IScriptProject project) throws CoreException;
 
 }
