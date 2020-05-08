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
 package org.eclipse.riena.internal.ui.ridgets.swt.uiprocess;
 
 import java.util.Comparator;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 
 /**
  * Extension interface for {@link ProcessDetail} comparators.
 * <p>
 * <b>Note:</b> The "org.eclipse.riena.ui.ridgets.swt.processdetail" is
 * deprecated.
  */
@ExtensionInterface(id = "processDetailComparator,org.eclipse.riena.ui.ridgets.swt.processdetail")
 public interface IProcessDetailComparatorExtension {
 
 	/**
 	 * Create an {@code Comparator} instance for {@code ProcessDetail}s.
 	 * 
 	 * @return the comparator
 	 */
 	@MapName("instance")
 	Comparator<ProcessDetail> createComparator();
 
 }
