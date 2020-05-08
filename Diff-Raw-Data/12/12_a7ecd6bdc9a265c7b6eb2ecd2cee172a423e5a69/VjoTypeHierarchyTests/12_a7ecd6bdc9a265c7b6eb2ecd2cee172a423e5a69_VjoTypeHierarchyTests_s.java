 /*******************************************************************************
  * Copyright (c) 2005-2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.core.test.parser;
 
 import java.io.ByteArrayOutputStream;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.mod.ast.Modifiers;
 import org.eclipse.dltk.mod.core.IType;
 import org.eclipse.dltk.mod.core.ITypeHierarchy;
 import org.eclipse.dltk.mod.core.ModelException;
 
 import org.eclipse.vjet.eclipse.core.IJSSourceModule;
 
 public class VjoTypeHierarchyTests extends AbstractVjoModelTests {
 
 //	@Before
 	public void setUp(){
 		IProject project = getWorkspaceRoot().getProject(getTestProjectName());
 		if (!project.exists())
 			super.setUpSuite();
 	}
 
 //	@Test
 	public void testHierarchy() throws ModelException {
 		IJSSourceModule module1 = (IJSSourceModule) getSourceModule(
 				getTestProjectName(), "src", new Path("test/testTypeA.js"));
 
 		IJSSourceModule module2 = (IJSSourceModule) getSourceModule(
 				getTestProjectName(), "src", new Path("test/testTypeB.js"));
 
 		IType typeA = module1.getTypes()[0];
 		assertEquals("Wrong type name", "testTypeA", typeA.getElementName());
 		IType typeB = module2.getTypes()[0];
 		assertEquals("Wrong type name", "testTypeB", typeB.getElementName());
 
 		ITypeHierarchy typeHierarchy = typeA.newTypeHierarchy(null);
 		IType[] subtypesA = typeHierarchy.getAllSubtypes(typeA);
 		IType[] superTypesB = typeHierarchy.getSuperclass(typeB);
 
 		assertEquals("Wrong number of subtypes", 1, subtypesA.length);
 		assertEquals("Wrong subtype", typeB, subtypesA[0]);
 
 		
 		typeHierarchy = typeB.newTypeHierarchy(null);
 		superTypesB = typeHierarchy.getSuperclass(typeB);
 		assertEquals("Wrong number of supertypes", 1, superTypesB.length);
 		assertEquals("Wrong supertype", typeA, superTypesB[0]);
 //		System.out.println("");
 	}
 
 //	@Test
 
 	public void testHierarchyTree() throws ModelException {
 		IJSSourceModule moduleB1 = (IJSSourceModule) getSourceModule(
 				getTestProjectName(), "src", new Path("test1/B.js"));
 
 		IType typeB1 = moduleB1.getTypes()[0];
 
 		ITypeHierarchy typeHierarchy = typeB1.newTypeHierarchy(null);
 
 		IType[] superTypesB1 = typeHierarchy.getAllSuperclasses(typeB1);
 		assertEquals("Wrong supertypes number", 2, superTypesB1.length);
 		assertEquals("Wrong supertype", "test1.A", superTypesB1[0]
 				.getFullyQualifiedName());
 		assertEquals("Wrong supertype", "test1.Root", superTypesB1[1]
 				.getFullyQualifiedName());
 
 		IType[] subtypesB1 = typeHierarchy.getSubtypes(typeB1);
 		assertEquals("Wrong subtypes number", 2, subtypesB1.length);
		assertEquals("Wrong supertype", "test1.C", subtypesB1[0]
 				.getFullyQualifiedName());		
		assertEquals("Wrong supertype", "test2.C", subtypesB1[1]
 				.getFullyQualifiedName());
 	}
 	
 	/**
 	 * Test all the method in ITypeHierarchy
 	 * @Test
 	 * @throws ModelException
 	 */
 	public void testHierarchyApi() throws ModelException {
 
 		IJSSourceModule moduleB1 = (IJSSourceModule) getSourceModule(
 				getTestProjectName(), "src", new Path("test1/B.js"));
 		IJSSourceModule moduleC1 = (IJSSourceModule) getSourceModule(
 				getTestProjectName(), "src", new Path("test1/C.js"));
 
 		IType typeB1 = moduleB1.getTypes()[0];
 		
 		IType typeC1 = moduleC1.getTypes()[0];
 
 		ITypeHierarchy typeHierarchy = typeB1.newTypeHierarchy(null);
 		//test getAllSuperClasses()
 		IType[] superTypesB1 = typeHierarchy.getAllSuperclasses(typeB1);
 		assertEquals("Wrong all superclasses size", 2, superTypesB1.length);
 		
 		//test contains()
 		assertTrue("Should  contain the calling type", typeHierarchy.contains(typeB1));
 		assertFalse("Should not contain its sub type",typeHierarchy.contains(typeC1));
 		//test getType()
 		assertEquals("Wrong result for getType", typeHierarchy.getType(), typeB1);
 		//test getAllClasses
 		IType[] types = typeHierarchy.getAllClasses();
 		assertEquals("Wrong all classes size", 3, types.length);
 		//test getAllSupertypes()
 		types = typeHierarchy.getAllSupertypes(typeB1);
 		assertEquals("Wrong all supertypes size", 2, types.length);
 		//test getSupertypes()
 		types = typeHierarchy.getSupertypes(typeB1);
 		assertEquals("Wrong supertypes size", 1, types.length);
 		//test getAllTypes()
 		types = typeHierarchy.getAllTypes();
 		assertEquals("Wrong all types size", 3, types.length);
 		//test getCacheFlags()
 		int flag1 = typeHierarchy.getCachedFlags(typeB1);
 		assertEquals("Wrong cache flag", Modifiers.AccPublic, flag1);
 		flag1 = typeHierarchy.getCachedFlags(typeC1);
 		assertEquals("Wrong cache flag", Modifiers.AccPublic, flag1);
 		//test getRootClasses()
 		types = typeHierarchy.getRootClasses();
 		assertEquals("Wrong root classes size", 1, types.length);
 		assertEquals("Wrong root classes", "Root", types[0].getElementName());
 		//test getSubclasses()
 		types = typeHierarchy.getSubclasses(typeB1);
 		assertEquals("Wrong subclasses size", 2, types.length);
 		//test refresh()
 		moduleC1.rename("MC.js", true, new NullProgressMonitor());
 		typeHierarchy.refresh(new NullProgressMonitor());
 		types = typeHierarchy.getSubclasses(typeB1);
 //		assertEquals("Can not resolve the module after renaming", 2, types.length);
 				
 		//test store
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		typeHierarchy.store(bos, new NullProgressMonitor());
 		System.out.println("%" + new String(bos.toByteArray()) + "%");
 	}
 }
