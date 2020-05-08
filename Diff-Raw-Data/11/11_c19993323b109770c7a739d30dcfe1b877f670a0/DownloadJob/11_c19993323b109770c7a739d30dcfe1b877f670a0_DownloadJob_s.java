 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *      Alexander Kurtakov (Red Hat) - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
 
 /**
  * Job class for downloading files from lookaside cache.
  */
 // TODO Remove and use this class from Rpm specfile editor once it is in released version.
 public class DownloadJob extends Job {
 	private IFile file;
 	private URLConnection content;
 	private boolean fileOverride;
 
 	public DownloadJob(IFile file, URLConnection content, boolean override) {
 		super(NLS.bind(Messages.downloadJob_name, file.getName()));
 		this.file = file;
 		this.content = content;
 		this.fileOverride = override;
 	}
 
 	public DownloadJob(IFile file, URLConnection content) {
 		this(file, content, false);
 	}
 
 	@Override
 	public IStatus run(IProgressMonitor monitor) {
 		try {
 			monitor.beginTask(
 					NLS.bind(Messages.downloadJob_name,
 							file.getName()), content.getContentLength());
 			File tempFile = File.createTempFile(file.getName(), ""); //$NON-NLS-1$
 			FileOutputStream fos = new FileOutputStream(tempFile);
 			InputStream is = new BufferedInputStream(content.getInputStream());
			int b;
 			boolean canceled = false;
			while ((b = is.read()) != -1) {
 				if (monitor.isCanceled()) {
 					canceled = true;
 					break;
 				}
				fos.write(b);
				monitor.worked(1);
 			}
 			is.close();
 			fos.close();
 			if (!canceled) {
 				if (fileOverride) {
 					file.setContents(new FileInputStream(tempFile), true,
 							false, monitor);
 				} else {
 					file.create(new FileInputStream(tempFile), true, monitor);
 
 				}
 			}
 			tempFile.delete();
 		} catch (FileNotFoundException e) {
 			return FedoraHandlerUtils.handleError(NLS.bind(Messages.downloadJob_fileDoesNotExist,
 					file.getName(), e.getMessage()), false);
 		} catch (UnknownHostException e) {
 			return FedoraHandlerUtils.handleError(NLS.bind(Messages.downloadJob_badHostname,
 					e.getMessage()), false);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return FedoraHandlerUtils.handleError(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return FedoraHandlerUtils.handleError(e);
 		}
 		monitor.done();
 		return Status.OK_STATUS;
 	}
 }
