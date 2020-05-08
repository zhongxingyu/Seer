 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.launch;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
 import org.eclipse.tcf.te.launch.core.lm.LaunchSpecification;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
 import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.services.filetransfer.FileTransferItem;
 import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
 import org.eclipse.tcf.te.runtime.utils.Host;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;
 import org.eclipse.tcf.te.tcf.launch.core.interfaces.IRemoteAppLaunchAttributes;
 import org.eclipse.tcf.te.tests.tcf.TcfTestCase;
 
 /**
  * TCF Launch tests.
  */
 public class TcfLaunchTests extends TcfTestCase {
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("TCF Launch tests"); //$NON-NLS-1$
 
 		// add ourself to the test suite
 		testSuite.addTestSuite(TcfLaunchTests.class);
 
 		return testSuite;
 	}
 
 
 	public void testRemoteAppLaunch() {
 		final ILaunchSpecification spec = new LaunchSpecification(ILaunchTypes.REMOTE_APPLICATION, ILaunchManager.RUN_MODE);
 		LaunchContextsPersistenceDelegate.setLaunchContexts(spec, new IModelNode[]{peerModel});
 
 		IPath helloWorldLocation = getHelloWorldLocation();
 		assertTrue("Missing hello world example for current OS and Arch:" + Platform.getOS() + "/" + Platform.getOSArch(), //$NON-NLS-1$ //$NON-NLS-2$
 						helloWorldLocation != null &&
 						helloWorldLocation.toFile().exists() &&
						helloWorldLocation.toFile().canRead());
 
 		String temp = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
 		IPath tempDir = temp != null ? new Path(temp) : null;
 		assertNotNull("Missing java temp directory", tempDir); //$NON-NLS-1$
 
 		IPath tempHelloWorld = tempDir.append(helloWorldLocation.lastSegment());
 		if (tempHelloWorld.toFile().exists()) {
 			tempHelloWorld.toFile().delete();
 		}
 		assertFalse("Cannot delete process image " + tempHelloWorld.toOSString(), tempHelloWorld.toFile().exists()); //$NON-NLS-1$
 
 		IPath outFile = tempDir.append("/HelloWorld.out"); //$NON-NLS-1$
 		if (outFile.toFile().exists()) {
 			outFile.toFile().delete();
 		}
 		assertFalse("Cannot delete console output file " + outFile.toOSString(), outFile.toFile().exists()); //$NON-NLS-1$
 
 		FileTransfersPersistenceDelegate.setFileTransfers(spec, new IFileTransferItem[]{new FileTransferItem(helloWorldLocation, tempDir)});
 		spec.addAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, tempHelloWorld.toOSString());
 
 		ILaunchConfiguration config = null;
 		try {
 			config = LaunchManager.getInstance().getLaunchConfiguration(spec, true);
 			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
 			wc.setAttribute("org.eclipse.debug.ui.ATTR_CONSOLE_OUTPUT_ON", false); //$NON-NLS-1$
 			wc.setAttribute("org.eclipse.debug.ui.ATTR_CAPTURE_IN_FILE", outFile.toOSString()); //$NON-NLS-1$
 			config = wc.doSave();
 		}
 		catch (Exception e) {
 			assertNull("Unexpected exception when creating launch: " + e, e); //$NON-NLS-1$
 		}
 
 		try {
 			LaunchManager.getInstance().launch(config, ILaunchManager.RUN_MODE, false, new NullProgressMonitor());
 		}
 		catch (Exception e) {
 			assertNull("Unexpected exception when launching hello world: " + e, e); //$NON-NLS-1$
 		}
 
 		assertTrue("Missing console output file", outFile.toFile().exists() && outFile.toFile().length() > 0); //$NON-NLS-1$
 	}
 
 	private IPath getHelloWorldLocation() {
 		IPath path = getDataLocation("helloWorld", true, true); //$NON-NLS-1$
 		if (path != null) {
 			path = path.append("HelloWorld"); //$NON-NLS-1$
 			if (Host.isWindowsHost()) {
 				path = path.addFileExtension("exe"); //$NON-NLS-1$
 			}
 		}
 
 		return path;
 	}
 }
