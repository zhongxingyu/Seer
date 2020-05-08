 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.uibinding;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 
 /**
  * Extension interface for the {@link ILabelFinderStrategy}
 * <p>
 * <b>Note:</b> The "org.eclipse.riena.ui.ridgets.labelfinderstrategy" is
 * deprecated.
  * 
  * @since 1.2
  */
@ExtensionInterface(id = "org.eclipse.riena.ui.ridgets.labelfinderstrategy,labelFinderStrategy")
 public interface ILabelFinderStrategyExtension {
 
 	/**
 	 * Create a {@code ILabelFinderStrategy}
 	 * 
 	 * @return a {@code ILabelFinderStrategy}
 	 */
 	@MapName("className")
 	ILabelFinderStrategy createFinderStrategy();
 }
