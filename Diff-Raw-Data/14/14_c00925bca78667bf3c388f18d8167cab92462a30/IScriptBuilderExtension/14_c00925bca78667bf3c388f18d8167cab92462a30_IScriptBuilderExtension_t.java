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
 package org.eclipse.dltk.core.builder;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IScriptProject;
 
 public interface IScriptBuilderExtension {
 
 	/**
 	 * @param project
 	 * @param externalElements
 	 * @param monitor
 	 * @param buildType
 	 */
	void buildExternalElements(IScriptProject project, List externalElements,
 			IProgressMonitor monitor, int buildType);
 
 }
