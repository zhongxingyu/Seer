 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.tomcat.tests.performance.tomcat50;
 
 import java.io.File;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.tests.performance.common.AbstractGetDelegateTestCase;
 public class GetDelegateTestCase extends AbstractGetDelegateTestCase {
 	public static Test suite() {
 		return new TestSuite(GetDelegateTestCase.class, "GetDelegateTestCase");
 	}
 
 	protected IRuntimeWorkingCopy createRuntime(String runtimeTypeId, String runtimeTypeLocation) throws CoreException {
 		if (runtimeTypeId == null)
 			throw new IllegalArgumentException();
 		IRuntimeWorkingCopy runtimeCopy = ServerCore.findRuntimeType(runtimeTypeId).createRuntime(runtimeTypeId, null);
 		runtimeCopy.setLocation(new Path(runtimeTypeLocation));
 		runtimeCopy.setReadOnly(false);
 		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
 		ITomcatRuntimeWorkingCopy rwc = (ITomcatRuntimeWorkingCopy) runtimeCopy
 				.getAdapter(ITomcatRuntimeWorkingCopy.class);
 		rwc.setVMInstall(vmInstall);
 		runtimeCopy.save(false, null);
 		return runtimeCopy;
 	}
 
 	protected String getRuntimeTypeId() {
 		return "org.eclipse.jst.server.tomcat.50.runtime";
 	}
 
 	protected String getRuntimeTypeLocation() {
 		String location = System.getProperty("org.eclipse.jst.server.tomcat.50");
 		assertNotNull(location);
 		assertTrue((new File(location)).exists());
 		return location;
 	}
 
 	protected String getServerTypeId() {
 		return "org.eclipse.jst.server.tomcat.50";
 	}
 }
