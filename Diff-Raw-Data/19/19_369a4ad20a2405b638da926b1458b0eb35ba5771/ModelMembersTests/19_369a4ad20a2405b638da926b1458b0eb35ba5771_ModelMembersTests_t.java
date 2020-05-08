 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.model;
 
 import junit.framework.Test;
 
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 
 
 public class ModelMembersTests extends AbstractModelTests {
 	public ModelMembersTests(String name) {
 		super(ModelTestsPlugin.PLUGIN_NAME, name);
 	}
 
 	public static Test suite() {
 		return new Suite(ModelMembersTests.class);
 	}
 
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
		setUpScriptProjectTo("ModelMembersq", "ModelMembers");
 	}
 
 	public void tearDownSuite() throws Exception {
		deleteProject("ModelMembersq");
 		super.tearDownSuite();
 	}
 
 	public void test001() throws ModelException {
		ISourceModule module = getSourceModule("ModelMembersq", "src1", new Path("X.txt"));
 		assertNotNull("No source module", module);
 		IModelElement[] children = module.getChildren();
 		assertNotNull("No children", children);
 		assertEquals("Wrong size", 2, children.length);
 		IType type = (IType) children[0];
 		assertEquals("Class1", type.getElementName());
 		assertEquals("Wrong size", 1, type.getChildren().length);
 		IMember proc = (IMember) children[1];
 		assertEquals("Procedure1", proc.getElementName());
 	}
 }
