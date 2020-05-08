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
 import org.eclipse.riena.ui.ridgets.IRidget;
 
 /**
  * Extension interface to set a custom label-prefix for the
  * {@link CorrespondingLabelMapper}.
 * <p>
 * <b>Note:</b> The "org.eclipse.riena.ui.ridgets.correspondinglabel.config" is
 * deprecated.
  * 
  * @since 1.2
  * 
  */
@ExtensionInterface(id = "org.eclipse.riena.ui.ridgets.correspondinglabel.config,correspondingLabel")
 public interface ICorrespondingLabelExtension {
 
 	/**
 	 * The label-prefix that will be used by the
 	 * {@link CorrespondingLabelMapper}
 	 * 
 	 * @return the label-prefix for finding corresponding labels of a
 	 *         {@link IRidget}
 	 */
 	String getLabelPrefix();
 }
