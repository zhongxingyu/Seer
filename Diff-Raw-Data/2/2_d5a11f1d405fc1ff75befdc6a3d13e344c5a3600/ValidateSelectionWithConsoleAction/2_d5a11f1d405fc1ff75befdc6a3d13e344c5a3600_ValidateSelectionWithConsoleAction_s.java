 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.internal.ui.popup.actions;
 
 import java.io.OutputStream;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.validators.core.ValidatorRuntime;
 import org.eclipse.dltk.validators.ui.AbstractValidateSelectionWithConsole;
 
 public class ValidateSelectionWithConsoleAction extends AbstractValidateSelectionWithConsole {
 	protected void invoceValidationFor(final OutputStream out,
 			final List elements, final List resources,
 			IProgressMonitor monitor) {
		ValidatorRuntime.executeAllValidatorsWithConsole(out,
 				elements, resources, monitor);
 	}
 }
