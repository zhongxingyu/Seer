 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core.tests.contentassist;
 
import org.eclipse.dltk.compiler.env.IModuleSource;

 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 
 public abstract class AbstractContentAssistTest extends TestCase {
 
 	protected IModuleSource createModule(String moduleName) {
 		return new TestModule(getClass().getResource(moduleName));
 	}
 
 	public static int lastPositionInFile(String string, IModuleSource source) {
 		return lastPositionInFile(string, source, true);
 	}
 
 	public static int lastPositionInFile(String string, IModuleSource source,
 			boolean after) {
 		int position = source.getSourceContents().lastIndexOf(string);
 		if (position >= 0) {
 			if (after) {
 				position += string.length();
 			}
 			return position;
 		} else {
			throw new AssertionFailedError("Not found");
 		}
 	}
	
 	public static int firstPositionInFile(String string, IModuleSource source,
 			boolean after) {
 		int position = source.getSourceContents().indexOf(string);
 		if (position >= 0) {
 			if (after) {
 				position += string.length();
 			}
 			return position;
 		} else {
 			throw new AssertionFailedError("Not found");
 		}
 	}
 

 }
