 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.utils;
 
 import java.io.IOException;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.cdt.utils.elf.Elf;
 import org.eclipse.cdt.utils.elf.Elf.ELFhdr;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.te.core.cdt.elf.ElfUtils;
 import org.eclipse.tcf.te.runtime.utils.Host;
 import org.eclipse.tcf.te.tests.CoreTestCase;
 
 /**
  * Utility test cases
  */
 @SuppressWarnings("restriction")
 public class UtilityTestCase extends CoreTestCase {
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("Test utility classes"); //$NON-NLS-1$
 
 			// add ourself to the test suite
 			testSuite.addTestSuite(UtilityTestCase.class);
 
 		return testSuite;
 	}
 
 	//***** BEGIN SECTION: Single test methods *****
 	//NOTE: All method which represents a single test case must
 	//      start with 'test'!
 
     public void testElfUtils() {
 		// Test case works on Linux only
 		if (!Host.isLinuxHost()) return;
 
 		// Use the Linux agent to test the ELF utilities
 		IPath path =  getDataLocation("agent", true, true); //$NON-NLS-1$
 		assertNotNull("Unexpected null value from getDataLocation()", path); //$NON-NLS-1$
 		assertTrue("Test ELF file does not exist or is not readable", path.toFile().canRead()); //$NON-NLS-1$
 
 		Exception error = null;
 		String message = null;
 
 		// Get the ELF type and class;
 		int type = -1;
 		try {
 			type = ElfUtils.getELFType(path.toFile());
 		} catch (IOException e) {
 			error = e;
 			message = e.getLocalizedMessage();
 		}
 		assertNull("Filed to get ELF type. Possible cause: " + message, error); //$NON-NLS-1$
 		assertEquals("Unexpected ELF type", Elf.Attribute.ELF_TYPE_EXE, type); //$NON-NLS-1$
 
 		int elfClass = ELFhdr.ELFCLASSNONE;
 		try {
 			elfClass = ElfUtils.getELFClass(path.toFile());
 		} catch (IOException e) {
 			error = e;
 			message = e.getLocalizedMessage();
 		}
 		assertNull("Filed to get ELF class. Possible cause: " + message, error); //$NON-NLS-1$
 		assertEquals("Unexpected ELF class", "x86_64".equals(Platform.getOSArch()) ? ELFhdr.ELFCLASS64 : ELFhdr.ELFCLASS32, elfClass); //$NON-NLS-1$ //$NON-NLS-2$
 
 		org.eclipse.tcf.te.core.cdt.activator.CoreBundleActivator.getContext();
 		org.eclipse.tcf.te.core.cdt.activator.CoreBundleActivator.getUniqueIdentifier();
 		org.eclipse.tcf.te.core.cdt.activator.CoreBundleActivator.getTraceHandler();
 	}
 
 	//***** END SECTION: Single test methods *****
 
 }
