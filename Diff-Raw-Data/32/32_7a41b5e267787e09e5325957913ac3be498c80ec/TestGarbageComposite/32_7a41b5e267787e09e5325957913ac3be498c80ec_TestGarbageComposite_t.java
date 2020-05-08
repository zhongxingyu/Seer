 /**
  * Copyright (C) 2012 Schneider Electric
  *
  * This file is part of "Mind Compiler" is free software: you can redistribute 
  * it and/or modify it under the terms of the GNU Lesser General Public License 
  * as published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact: mind@ow2.org
  *
  * Authors: Julien TOUS
  * Contributors: 
  */
 
 package org.ow2.mind;
 
 import static org.testng.Assert.assertTrue;
 
 import java.io.File;
 
 import org.testng.annotations.Test;
 
 public class TestGarbageComposite extends AbstractFunctionalTest {
 
 	protected static File         buildDir = new File("target/build/");
 	
 	protected void cleanBuildDir() {
 		if (buildDir.exists()) deleteDir(buildDir);
 	}
 
 	protected void deleteDir(final File f) {
 		if (f.isDirectory()) {
 			for (final File subFile : f.listFiles())
 				deleteDir(subFile);
 		}
 		assertTrue(f.delete(), "Can't delete \"" + f + "\".");
 	}
 	
 	@Test(groups = {"checkin"})
 	public void testNoBinding1() throws Exception {
 		cleanBuildDir();  
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"NoBinding");
 		runner.compileRunAndCheck("NoBinding1", null);
 	}
 
 	@Test(groups = {"checkin"})
 	public void testNoBinding2() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"NoBinding");
 		runner.compileRunAndCheck("NoBinding2", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void testTemplateUser() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"NoBinding");
 		runner.compileRunAndCheck("TemplateUser", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void testTemplateContainer() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"NoBinding");
 		runner.compileRunAndCheck("TemplateContainer", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void testParameters() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"Parameters");
 		runner.compileRunAndCheck("NoBinding1", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void testNestedParameters() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"Parameters");
 		runner.compileRunAndCheck("NoBinding2", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void testParameterTemplateContainer() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"Parameters");
 		runner.compileRunAndCheck("TemplateContainer", null);
 	}
 	@Test(groups = {"checkin"})
 	public void internalOnlyBinding() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalOnlyBinding");
 		runner.compileRunAndCheck("InternalOnlyBinding", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void internalClientCollectionBinding1() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientCollectionBinding");
 		runner.compileRunAndCheck("InternalClientBinding1", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void internalClientCollectionBinding2() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientCollectionBinding");
 		runner.compileRunAndCheck("InternalClientBinding2", null);
 	}
 
 	@Test(groups = {"checkin"})
 	public void internalClientCollectionBinding3() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientCollectionBinding");
 		runner.compileRunAndCheck("InternalClientBinding3", null);
 	}
 	
 	@Test(groups = {"checkin"})
	public void internalClientCollectionToSimpleBinding1() throws Exception {
		cleanBuildDir();
		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientCollectionToSimpleBinding");
		runner.compileRunAndCheck("InternalClientBinding1", null);
	}
	
	@Test(groups = {"checkin"})
	public void internalClientCollectionToSimpleBinding2() throws Exception {
		cleanBuildDir();
		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientCollectionToSimpleBinding");
		runner.compileRunAndCheck("InternalClientBinding2", null);
	}

	@Test(groups = {"checkin"})
	public void internalClientCollectionToSimpleBinding3() throws Exception {
		cleanBuildDir();
		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientCollectionToSimpleBinding");
		runner.compileRunAndCheck("InternalClientBinding3", null);
	}
	
	@Test(groups = {"checkin"})
 	public void internalClientBinding1() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientBinding");
 		runner.compileRunAndCheck("InternalClientBinding1", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void internalClientBinding2() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalClientBinding");
 		runner.compileRunAndCheck("InternalClientBinding2", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void internalServerBinding1() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalServerBinding");
 		runner.compileRunAndCheck("InternalServerBinding1", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void internalServerBinding2() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"InternalServerBinding");
 		runner.compileRunAndCheck("InternalServerBinding2", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void multiInternalClientBinding1() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"MultiInternalClientBinding");
 		runner.compileRunAndCheck("MultiInternalClientBinding1", null);
 	}
 
 	@Test(groups = {"checkin"})
 	public void multiInternalClientBinding2() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"MultiInternalClientBinding");
 		runner.compileRunAndCheck("MultiInternalClientBinding2", null);
 	}
 	
 	@Test(groups = {"checkin"})
 	public void multiInternalClientBinding3() throws Exception {
 		cleanBuildDir();
 		initSourcePath(getDepsDir("memory/api/Allocator.itf").getAbsolutePath(),"MultiInternalClientBinding");
 		runner.compileRunAndCheck("MultiInternalClientBinding3", null);
 	}
 
 }
