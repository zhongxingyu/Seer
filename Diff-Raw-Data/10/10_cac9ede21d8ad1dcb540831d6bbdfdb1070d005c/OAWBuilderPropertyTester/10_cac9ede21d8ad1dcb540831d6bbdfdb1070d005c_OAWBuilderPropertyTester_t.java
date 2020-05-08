 /**
  * Copyright (c) 2009 Borland Software Corp.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Alexander Shatalin (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.migration.ui;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.gmf.internal.xpand.build.OawBuilder;
 import org.eclipse.gmf.internal.xpand.migration.Activator;
 import org.eclipse.jdt.core.IJavaProject;
 
 public class OAWBuilderPropertyTester extends PropertyTester {
 
 	private static final String HAS_OAW_BUILDER_PROPERTY = "hasOAWBuilder";
 
 	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 		if (HAS_OAW_BUILDER_PROPERTY.equals(property)) {
 			if (receiver instanceof IJavaProject) {
 				return hasOAWBuilder(((IJavaProject) receiver).getProject());
 			} else if (receiver instanceof IProject) {
 				return hasOAWBuilder((IProject) receiver);
 			}
 		}
 		return false;
 	}
 
 	private boolean hasOAWBuilder(IProject project) {
 		try {
			if (project.isOpen()) {
				ICommand[] buildSpecification = project.getDescription().getBuildSpec();
				for (ICommand buildCommand : buildSpecification) {
					if (OawBuilder.getBUILDER_ID().equals(buildCommand.getBuilderName())) {
						return true;
					}
 				}
 			}
 		} catch (CoreException e) {
 			Activator.logError(e);
 		}
 		return false;
 	}
 
 }
