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
 package org.fedoraproject.eclipse.packager.cvs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.security.GeneralSecurityException;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.internal.ccvs.core.CVSException;
 import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
 import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
 import org.eclipse.team.internal.ccvs.core.ICVSFile;
 import org.eclipse.team.internal.ccvs.core.ICVSFolder;
 import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.client.Command;
 import org.eclipse.team.internal.ccvs.core.client.Session;
 import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.SSLUtils;
 import org.fedoraproject.eclipse.packager.SourcesFile;
 import org.fedoraproject.eclipse.packager.rpm.RPMHandler;
 
 @SuppressWarnings("restriction")
 public class UploadHandler extends RPMHandler {
 	public static String uploadURL = "https://cvs.fedoraproject.org/repo/pkgs/upload.cgi"; //$NON-NLS-1$
 
 	@Override
 	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
 			throws ExecutionException {
 		return Status.OK_STATUS;
 	}
 
 	@Override
 	public Object execute(final ExecutionEvent e) throws ExecutionException {
 		final IResource resource = getResource(e);
 		final FedoraProjectRoot fedoraProjectRoot = getValidRoot(resource);
 		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
 		specfile = fedoraProjectRoot.getSpecFile();
 		job = new Job("Fedora Packager") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				monitor.beginTask(
 						Messages.getString("UploadHandler.1"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
 				sources = getSourcesFile().getSources();
 
 				final File cvsignore = new File(fedoraProjectRoot
 						.getContainer().getLocation().toString()
 						+ Path.SEPARATOR + ".cvsignore");
 
 				// don't add empty files
 				final File toAdd = resource.getLocation().toFile();
 				if (toAdd.length() == 0) {
 					return handleOK(
 							NLS.bind(
 									Messages.getString("UploadHandler.0"), resource.getName()), true); //$NON-NLS-1$
 				}
 
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				monitor.subTask(Messages.getString("UploadHandler.5")); //$NON-NLS-1$
 				// ensure file has changed if already listed in sources
 				final String filename = resource.getName();
 				if (sources.containsKey(filename)
 						&& SourcesFile
 								.checkMD5(sources.get(filename), resource)) {
 					// file already in sources
 					return handleOK(NLS.bind(
 							Messages.getString("UploadHandler.2"), filename) //$NON-NLS-1$
 							, true);
 				}
 
 				// use our Fedora client certificate to start SSL connection
 				IStatus result = performUpload(toAdd, filename, monitor, fedoraProjectRoot);
 
 				if (result.isOK()) {
 					if (monitor.isCanceled()) {
 						throw new OperationCanceledException();
 					}
 					monitor.subTask(Messages.getString("UploadHandler.8")); //$NON-NLS-1$
 					// TODO fix
 					result = updateFiles(sourceFile, cvsignore, toAdd,
 							filename, monitor);
 
 				}
 
 				return result;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 
 	protected IStatus performUpload(final File toAdd, final String filename,
 			IProgressMonitor monitor, FedoraProjectRoot fedoraProjectRoot) {
 		IStatus status;
 		try {
 			registerProtocol();
 
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			// first check remote status to see if file is already uploaded
 			monitor.subTask(NLS.bind(
 					Messages.getString("UploadHandler.3"), filename)); //$NON-NLS-1$
 			HttpClient client = new HttpClient();
 			client.getHttpConnectionManager().getParams()
 					.setConnectionTimeout(30000);
 			PostMethod postMethod = new PostMethod(uploadURL);
 			NameValuePair[] data = {
 					new NameValuePair(
							"name", getValidRoot(resource).getSpecfileModel().getName()), //$NON-NLS-1$ //$NON-NLS-2$
 					new NameValuePair("md5sum", SourcesFile.getMD5(toAdd)), //$NON-NLS-1$
 					new NameValuePair("filename", filename) }; //$NON-NLS-1$
 			postMethod.setRequestBody(data);
 			if (client.executeMethod(postMethod) != HttpURLConnection.HTTP_OK) {
 				status = handleError(NLS.bind(
 						Messages.getString("UploadHandler.4"), filename)); //$NON-NLS-1$
 			} else {
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				InputStream responseStream = postMethod
 						.getResponseBodyAsStream();
 				String response = parseResponse(responseStream);
 
 				// if we're in debug mode, forget this check
 				if (response.toLowerCase().equals("available") && !debug) { //$NON-NLS-1$
 					status = handleOK(
 							NLS.bind(
 									Messages.getString("UploadHandler.6"), filename), true); //$NON-NLS-1$ //$NON-NLS-2$
 				} else if (response.toLowerCase().equals("missing") || debug) { //$NON-NLS-1$
 					if (monitor.isCanceled()) {
 						throw new OperationCanceledException();
 					}
 					monitor.subTask(NLS.bind(
 							Messages.getString("UploadHandler.9"), filename)); //$NON-NLS-1$
 					status = upload(toAdd, fedoraProjectRoot);
 				} else {
 					status = handleError(response);
 				}
 			}
 		} catch (HttpException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} catch (GeneralSecurityException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		}
 
 		return status;
 	}
 
 	protected IStatus updateFiles(final SourcesFile sources,
 			final File cvsignore, final File toAdd, final String filename,
 			IProgressMonitor monitor) {
 		IStatus status;
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		if ((status = updateSources(sources, toAdd)).isOK()) {
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			if ((status = updateCVSIgnore(cvsignore, toAdd)).isOK()) {
 				try {
 					handleOK(
 							NLS.bind(
 									Messages.getString("UploadHandler.10"), filename), true); //$NON-NLS-1$
 					specfile.getParent().refreshLocal(IResource.DEPTH_INFINITE,
 							new NullProgressMonitor());
 
 					// add sources and .cvsignore to CVS if not already there
 					status = updateCVS(sources, cvsignore, monitor);
 
 				} catch (CoreException e) {
 					e.printStackTrace();
 					status = handleError(e);
 				}
 			}
 		}
 		monitor.done();
 		return status;
 	}
 
 	protected IStatus updateCVSIgnore(File cvsignore, File toAdd) {
 		return updateCVSIgnore(cvsignore, toAdd, false);
 	}
 
 	protected IStatus updateSources(SourcesFile sources, File toAdd) {
 		return updateSources(sources, toAdd, false);
 	}
 
 	protected IStatus updateCVS(SourcesFile sources, File cvsignore,
 			IProgressMonitor monitor) {
 		IStatus status = Status.OK_STATUS;
 		// get CVSProvider
 		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
 				.getProvider(specfile.getProject(),
 						CVSProviderPlugin.getTypeId());
 
 		try {
 			ICVSRepositoryLocation location = provider.getRemoteLocation();
 
 			// get CVSROOT
 			CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
 			ICVSFolder rootFolder = cvsRoot.getLocalRoot();
 
 			// get Branch
 			ICVSFolder branchFolder = rootFolder.getFolder(specfile.getParent()
 					.getName());
 			if (branchFolder != null) {
 				ICVSFile cvsSources = branchFolder.getFile(sources.getName());
 				if (cvsSources != null) {
 					// if 'sources' is not shared with CVS, add it
 					Session session = new Session(location, branchFolder, true);
 					session.open(monitor, true);
 					if (!cvsSources.isManaged()) {
 						if (monitor.isCanceled()) {
 							throw new OperationCanceledException();
 						}
 						String[] arguments = new String[] { sources.getName() };
 						status = Command.ADD.execute(session,
 								Command.NO_GLOBAL_OPTIONS,
 								Command.NO_LOCAL_OPTIONS, arguments, null,
 								monitor);
 					}
 					if (status.isOK()) {
 						// everything has passed so far
 						if (monitor.isCanceled()) {
 							throw new OperationCanceledException();
 						}
 						// perform update on sources and .cvsignore
 						String[] arguments = new String[] { sources.getName(),
 								cvsignore.getName() };
 						status = Command.UPDATE.execute(session,
 								Command.NO_GLOBAL_OPTIONS,
 								Command.NO_LOCAL_OPTIONS, arguments, null,
 								monitor);
 					}
 				} else {
 					status = handleError(Messages.getString("UploadHandler.22")); //$NON-NLS-1$
 				}
 			} else {
 				status = handleError(Messages.getString("UploadHandler.23")); //$NON-NLS-1$
 			}
 
 		} catch (CVSException e) {
 			e.printStackTrace();
 			status = handleError(e.getMessage());
 		}
 		return status;
 	}
 
 	protected IStatus updateCVSIgnore(File cvsignore, File toAdd,
 			boolean forceOverwrite) {
 		IStatus status;
 		String filename = toAdd.getName();
 		ArrayList<String> ignoreFiles = new ArrayList<String>();
 		BufferedReader br = null;
 		PrintWriter pw = null;
 		try {
 			if (forceOverwrite) {
 				pw = new PrintWriter(new FileWriter(cvsignore, false));
 				pw.println(filename);
 				status = Status.OK_STATUS;
 			} else {
 				// only append to file if not already present
 				br = new BufferedReader(new FileReader(cvsignore));
 
 				String line = br.readLine();
 				while (line != null) {
 					ignoreFiles.add(line);
 					line = br.readLine();
 				}
 
 				if (!ignoreFiles.contains(filename)) {
 					pw = new PrintWriter(new FileWriter(cvsignore, true));
 					pw.println(filename);
 				}
 				status = Status.OK_STATUS;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} finally {
 			if (pw != null) {
 				pw.close();
 			}
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					status = handleError(e);
 				}
 			}
 		}
 
 		return status;
 	}
 
 	protected IStatus updateSources(SourcesFile sourceFile, File toAdd,
 			boolean forceOverwrite) {
 		String filename = toAdd.getName();
 		if (forceOverwrite) {
 			sourceFile.getSources().clear();
 		}
 		sourceFile.getSources().put(filename, SourcesFile.getMD5(toAdd));
 
 		try {
 			sourceFile.save();
 		} catch (CoreException e) {
 			return e.getStatus();
 		}
 		return Status.OK_STATUS;
 	}
 
 	protected void registerProtocol() throws GeneralSecurityException,
 			IOException, NoSuchAlgorithmException, KeyStoreException,
 			KeyManagementException, CertificateException {
 		HttpSecureProtocol protocol = new HttpSecureProtocol();
 		protocol.setKeyMaterial(SSLUtils.getKeyMaterial());
 		protocol.setTrustMaterial(TrustMaterial.TRUST_ALL);
 		Protocol.registerProtocol("https", new Protocol("https", //$NON-NLS-1$ //$NON-NLS-2$
 				(ProtocolSocketFactory) protocol, 443));
 	}
 
 	protected IStatus upload(File file, FedoraProjectRoot fedoraProjectRoot) {
 		IStatus status;
 		byte[] bytes = new byte[(int) file.length()];
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(file);
 
 			String filename = file.getName();
 
 			fis.read(bytes);
 
 			HttpClient client = new HttpClient();
 			client.getHttpConnectionManager().getParams()
 					.setConnectionTimeout(30000);
 			PostMethod postMethod = new PostMethod(uploadURL);
 
 			Part[] data = { new StringPart("name", fedoraProjectRoot.getSpecfileModel().getName()), //$NON-NLS-1$ //$NON-NLS-2$
 					new StringPart("md5sum", SourcesFile.getMD5(file)), //$NON-NLS-1$
 					new FilePart("file", file) }; //$NON-NLS-1$
 
 			postMethod.setRequestEntity(new MultipartRequestEntity(data,
 					postMethod.getParams()));
 
 			int code = client.executeMethod(postMethod);
 			if (code != HttpURLConnection.HTTP_OK) {
 				status = handleError(NLS
 						.bind(Messages.getString("UploadHandler.33"), filename, postMethod.getStatusLine())); //$NON-NLS-1$
 			} else {
 				status = Status.OK_STATUS;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} catch (HttpException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					status = handleError(e);
 				}
 			}
 		}
 		return status;
 	}
 
 	protected String parseResponse(InputStream responseStream)
 			throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(
 				responseStream));
 
 		String responseText = ""; //$NON-NLS-1$
 		String line;
 		try {
 			line = br.readLine();
 			while (line != null) {
 				responseText += line + "\n"; //$NON-NLS-1$
 				line = br.readLine();
 			}
 		} finally {
 			br.close();
 		}
 
 		return responseText.trim();
 	}
 
 	protected File getFileFor(String name) throws IOException {
 		File ret = null;
 		IResource res = specfile.getParent().findMember(name, true);
 		if (res == null) {
 			ret = new File(specfile.getParent().getLocation().toString()
 					+ Path.SEPARATOR + name);
 			if (!ret.createNewFile()) {
 				throw new IOException(NLS.bind(
 						Messages.getString("UploadHandler.12"), name)); //$NON-NLS-1$
 			}
 		} else {
 			ret = res.getLocation().toFile();
 		}
 		return ret;
 	}
 
 	@Override
 	protected String getTaskName() {
 		return Messages.getString("UploadHandler.38"); //$NON-NLS-1$
 	}
 
 }
