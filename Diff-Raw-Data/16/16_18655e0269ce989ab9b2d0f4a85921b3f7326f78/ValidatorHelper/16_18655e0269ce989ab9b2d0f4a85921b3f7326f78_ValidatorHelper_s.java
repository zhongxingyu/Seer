 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.validator;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.wst.validation.internal.operations.WorkbenchContext;
 import org.eclipse.wst.validation.internal.operations.WorkbenchReporter;
 
 /**
  * @author Michal Chmielewski (michal.chmielewski@oracle.com)
  * @date Mar 13, 2007
  *
  */
 public class ValidatorHelper extends WorkbenchContext {
 
 	@Override
 	public void registerResource(IResource resource) {
		getValidationFileURIs().add(resource.getFullPath().toOSString());		
 	}
 
 }
