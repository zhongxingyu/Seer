 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.navigation.ui.swt.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 
 import org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade;
 
 /**
  * Switch focus to the 'work area'.
  */
 public class SwitchToWorkarea extends AbstractHandler {
 
 	public Object execute(final ExecutionEvent event) throws ExecutionException {
		WorkbenchFacade.getInstance().switchToWorkarea(event);
 		return null;
 	}
 }
