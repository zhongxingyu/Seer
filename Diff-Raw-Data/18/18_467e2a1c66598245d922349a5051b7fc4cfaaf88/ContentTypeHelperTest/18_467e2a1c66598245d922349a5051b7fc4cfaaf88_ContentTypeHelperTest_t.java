 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.filesystem.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpUpload;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.ContentTypeHelper;
 import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
 import org.eclipse.tcf.te.tests.activator.UIPlugin;
 import org.osgi.framework.Bundle;
 
 public class ContentTypeHelperTest extends UtilsTestBase {
 	private FSTreeNode agentNode;
 	@Override
     protected void setUp() throws Exception {
 	    super.setUp();
 		uploadAgent();
 		prepareXmlFile();
     }
 
 	private void prepareXmlFile() throws IOException {
 	    StringBuilder content = new StringBuilder();
 		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
 		content.append("<root></root>\n"); //$NON-NLS-1$
 		writeFileContent(content.toString());
     }
 
     private void uploadAgent() throws Exception {
	    IPath path = getWindowsAgent();
	    assertNotNull("Cannot find (Windows) agent location!", path); //$NON-NLS-1$
 		assertTrue("Invalid agent location: " + path.toString(), path.toFile().isFile()); //$NON-NLS-1$
 	    File agentFile = path.toFile();
 	    String agentPath = getTestRoot() + getPathSep() + agentFile.getName();
 	    agentNode = getFSNode(agentPath);
 		if (agentNode == null) {
 			URL rootURL = testRoot.getLocationURL();
 			URL agentURL = new URL(rootURL, agentFile.getName());
 			OpUpload upload = new OpUpload(agentFile, agentURL);
 			upload.run(new NullProgressMonitor());
 			agentNode = getFSNode(agentPath);
 			assertNotNull(agentNode);
 		}
     }
 
    private IPath getWindowsAgent() {
 		Bundle bundle = UIPlugin.getDefault().getBundle();
 		if (bundle != null) {
 			IPath relative = new Path ("data").append("agent"); //$NON-NLS-1$ //$NON-NLS-2$
			relative = relative.append("win32"); //$NON-NLS-1$
 			relative = relative.append("x86_64"); //$NON-NLS-1$
 			relative = relative.append("agent"); //$NON-NLS-1$
			relative.addFileExtension("exe"); //$NON-NLS-1$
 
 			URL url = FileLocator.find(bundle, relative, null);
 			if (url != null) {
 				try {
 					return new Path(FileLocator.resolve(url).getFile());
 				} catch (IOException e) { /* ignored on purpose */ }
 			}
 		}
 		return null;
     }
 
 	public void testBinaryFile() {
 		printMessage("The agent's location is: "+agentNode.getLocation()); //$NON-NLS-1$
 		IContentType contentType = ContentTypeHelper.getContentType(agentNode);
 		if (contentType != null) {
 			IContentType binaryFile = Platform.getContentTypeManager().getContentType("org.eclipse.cdt.core.binaryFile"); //$NON-NLS-1$
 			if (binaryFile != null) {
 				 if(contentType.isKindOf(binaryFile)){
 					 printMessage("Successful of testing binary file"); //$NON-NLS-1$
 				 }
 				 else{
 					 printMessage("Failure of testing binary file"); //$NON-NLS-1$
 				 }
 			}
 			else {
 				printMessage("The content type for binary file cannot be found!"); //$NON-NLS-1$
 			}
 		}
 		else {
 			printMessage("The content type of the agent node is null!"); //$NON-NLS-1$
 		}
 	}
 
 	public void testContentType() {
 		IContentType contentType = ContentTypeHelper.getContentType(testFile);
 		assertEquals("Text", contentType.getName()); //$NON-NLS-1$
 	}
 }
