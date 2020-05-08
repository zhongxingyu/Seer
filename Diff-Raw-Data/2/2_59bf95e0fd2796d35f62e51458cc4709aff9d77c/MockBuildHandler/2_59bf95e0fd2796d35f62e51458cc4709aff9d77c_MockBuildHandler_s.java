 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.rpm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.linuxtools.rpm.core.utils.Utils;
 import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
 import org.eclipse.osgi.util.NLS;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
 
 /**
  * Handler for building locally using mock.
  *
  */
 public class MockBuildHandler extends RPMHandler {
 	
 	@Override
 	public Object execute(final ExecutionEvent e) throws ExecutionException {
 		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils.getValidRoot(e);
 		specfile = fedoraProjectRoot.getSpecFile();
 		Job job = new Job(Messages.mockBuildHandler_jobName) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				monitor.beginTask(Messages.mockBuildHandler_testLocalBuildWithMock, IProgressMonitor.UNKNOWN);
 				// build fresh SRPM
 				IStatus result = makeSRPM(fedoraProjectRoot, monitor);
 				if (result.isOK()) {
 					if (monitor.isCanceled()) {
 						throw new OperationCanceledException();
 					}
 					result = createMockJob(fedoraProjectRoot, monitor);
 				}
 				monitor.done();
 				return result;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 
 	protected IStatus createMockJob(FedoraProjectRoot projectRoot, IProgressMonitor monitor) {
 		// get buildarch
 		try {
 			String buildarch = rpmEval("_arch"); //$NON-NLS-1$
 			final String mockcfg = getMockcfg(projectRoot, buildarch);
 
 			monitor.subTask(NLS.bind(Messages.mockBuildHandler_callMockMsg, projectRoot.getSpecFile().getName()));
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			return mockBuild(mockcfg, projectRoot, monitor);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return handleError(e);
 		}
 	}
 
 	protected IStatus mockBuild(String mockcfg, FedoraProjectRoot projectRoot, IProgressMonitor monitor) {
 		IStatus status;
 		IResource parent = specfile.getParent();
 		String dir = parent.getLocation().toString();
 		
 		// make sure mock is installed, bail out otherwise
 		if (!isMockInstalled()) {
 			return handleError(Messages.mockBuildHandler_mockNotInstalled);
 		}
 		try {
 			Specfile specfile = projectRoot.getSpecfileModel();
 			String[] cmd = { "mock", "-r", mockcfg, "--resultdir=" + dir //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					+ IPath.SEPARATOR + FedoraHandlerUtils.makeTagName(projectRoot), "rebuild", dir //$NON-NLS-1$
 					+ IPath.SEPARATOR + specfile.getName() + "-" //$NON-NLS-1$
 					+ specfile.getVersion() + "-" //$NON-NLS-1$
 					+ FedoraHandlerUtils.rpmQuery(projectRoot, "RELEASE") + ".src.rpm" }; //$NON-NLS-1$ //$NON-NLS-2$
 			InputStream is = Utils.runCommandToInputStream(cmd);
 			status = runShellCommand(is, monitor);
 			
 			// refresh containing folder
 			parent.refreshLocal(IResource.DEPTH_INFINITE,
 					new NullProgressMonitor());
 		} catch (CoreException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		}
 		return status;
 	}
 
 	private String getMockcfg(FedoraProjectRoot projectRoot, String buildarch) {
 		IFpProjectBits projectBits =  FedoraHandlerUtils.getVcsHandler(projectRoot);
 		String distvar = projectBits.getDistVariable(); 
 		String distval = projectBits.getDistVal(); 
 		String mockcfg = null;
 		if (distvar.equals("rhel")) { //$NON-NLS-1$
			mockcfg = "fedora-" + distval + "-" + buildarch + "-epel"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		} else {
 			mockcfg = "fedora-" + distval + "-" + buildarch; //$NON-NLS-1$ //$NON-NLS-2$
 			if (distval.equals("4") || distval.equals("5") //$NON-NLS-1$ //$NON-NLS-2$
 					|| distval.equals("6")) { //$NON-NLS-1$
 				mockcfg += "-core"; //$NON-NLS-1$
 			}
 			
 			if (projectBits.getCurrentBranchName().equals("devel")) { //$NON-NLS-1$
 				mockcfg = "fedora-devel-" + buildarch; //$NON-NLS-1$
 			}
 			
 			if (projectBits.getCurrentBranchName().equals("devel")) { //$NON-NLS-1$
 				//If the specified mockcfg does not exist...
 				File file = new File("/etc/mock/" + mockcfg); //$NON-NLS-1$
 				if (!file.exists()){ 
 					mockcfg = "fedora-devel-" + buildarch;  //$NON-NLS-1$
 				}
 			}
 		}
 		return mockcfg;
 	}
 	
 	/**
 	 * Determine if mock program is available
 	 * 
 	 * @return
 	 */
 	private boolean isMockInstalled() {
 		if (Utils.fileExist("/usr/bin/mock")) { //$NON-NLS-1$
 			return true;
 		}
 		return false;
 	}
 
 }
