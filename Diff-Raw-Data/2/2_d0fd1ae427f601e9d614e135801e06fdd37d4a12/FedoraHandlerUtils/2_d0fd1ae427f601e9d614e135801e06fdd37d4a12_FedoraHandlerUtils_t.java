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
 package org.fedoraproject.eclipse.packager.handlers;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.ssl.Certificates;
 import org.apache.commons.ssl.KeyMaterial;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.linuxtools.rpm.core.utils.Utils;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchSite;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.part.EditorPart;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.Messages;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 
 /**
  * Utility class used to determine fedora specific properties from the
  * information in the ExecutionEvent.
  */
 public class FedoraHandlerUtils {
 
 	private static final String GIT_REPOSITORY = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$
 	private static final String CVS_REPOSITORY = "org.eclipse.team.cvs.core.cvsnature"; //$NON-NLS-1$
 
 	/**
 	 * Represents the Git, Cvs or unknown project type.
 	 * 
 	 */
 	public static enum ProjectType {
 		/** Git project */
 		GIT,
 		/** Cvs project */
 		CVS,
 		/** Unknown */
 		UNKNOWN
 	}
 
 	/**
 	 * Returns a FedoraProjectRoot from the given resource. It finds
 	 * the underlying resource from the given ExecutionEvent and then
 	 * uses the closes container containing the sources file
 	 * 
 	 * @param event
 	 *            The execution event.
 	 * @return The retrieved FedoraProjectRoot.
 	 */
 	public static FedoraProjectRoot getValidRoot(ExecutionEvent event) {
 		IResource resource = getResource(event);
 		if (resource instanceof IFolder || resource instanceof IProject) {
 			// TODO check that spec file and sources file are present
 			if (validateFedorapackageRoot((IContainer) resource)) {
 				return new FedoraProjectRoot((IContainer) resource, event.getCommand().getId());
 			}
 		} else if (resource instanceof IFile) {
 			if (validateFedorapackageRoot(resource.getParent())) {
 				return new FedoraProjectRoot(resource.getParent(), event.getCommand().getId());
 			}
 		}
 		return null;
 	}
 
 	private static boolean validateFedorapackageRoot(IContainer resource) {
 		IFile file = resource.getFile(new Path("sources")); //$NON-NLS-1$
 		if (file.exists()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the IResource that was selected when the event was fired.
 	 * @param event The fired execution event. 
 	 * @return The resource that was selected.
 	 */
 	public static IResource getResource(ExecutionEvent event) {
 		IWorkbenchPart part = HandlerUtil.getActivePart(event);
 		if (part == null) {
 			return null;
 		}
 		if (part instanceof EditorPart) {
 			IEditorInput input = ((EditorPart) part).getEditorInput();
 			if (input instanceof IFileEditorInput) {
 				return ((IFileEditorInput) input).getFile();
 			} else {
 				return null;
 			}
 		}
 		IWorkbenchSite site = part.getSite();
 		if (site == null) {
 			return null;
 		}
 		ISelectionProvider provider = site.getSelectionProvider();
 		if (provider == null) {
 			return null;
 		}
 		ISelection selection = provider.getSelection();
 		if (selection instanceof IStructuredSelection) {
 			Object element = ((IStructuredSelection) selection)
 					.getFirstElement();
 			if (element instanceof IResource) {
 				return (IResource) element;
 			} else if (element instanceof IAdaptable) {
 				IAdaptable adaptable = (IAdaptable) element;
 				Object adapted = adaptable.getAdapter(IResource.class);
 				return (IResource) adapted;
 			} else {
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Creates a list of rpm defines to use the given directory as a base directory.
 	 * @param dir The base directory.
 	 * @return Defines to instruct rpmbuild to use given directory.
 	 */
 	public static List<String> getRPMDefines(String dir) {
 		ArrayList<String> rpmDefines = new ArrayList<String>();
 		rpmDefines.add("--define"); //$NON-NLS-1$
 		rpmDefines.add("_sourcedir " + dir); //$NON-NLS-1$
 		rpmDefines.add("--define"); //$NON-NLS-1$
 		rpmDefines.add("_builddir " + dir); //$NON-NLS-1$
 		rpmDefines.add("--define"); //$NON-NLS-1$
 		rpmDefines.add("_srcrpmdir " + dir); //$NON-NLS-1$
 		rpmDefines.add("--define"); //$NON-NLS-1$
 		rpmDefines.add("_rpmdir " + dir); //$NON-NLS-1$
 
 		return rpmDefines;
 	}
 
 	/**
 	 * Returns the project type determined from the given IResource.
 	 * @param resource The base for determining the project type.
 	 * @return The project type.
 	 */
 	public static ProjectType getProjectType(IResource resource) {
 
 		Map<?,?> persistentProperties = null;
 		try {
 			persistentProperties = resource.getProject()
 					.getPersistentProperties();
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		QualifiedName name = new QualifiedName("org.eclipse.team.core", //$NON-NLS-1$
 				"repository"); //$NON-NLS-1$
 		String repository = (String) persistentProperties.get(name);
 		if (GIT_REPOSITORY.equals(repository)) {
 			return ProjectType.GIT;
 		} else if (CVS_REPOSITORY.equals(repository)) {
 			return ProjectType.CVS;
 		}
 		return ProjectType.UNKNOWN;
 	}
 
 	/**
 	 * Returns the IFpProjectBits used to abstract vcs specific things.
 	 * 
 	 * @param fedoraprojectRoot The project for which to get the VCS specific parts.
 	 * @return The needed IFpProjectBits.
 	 */
 	public static IFpProjectBits getVcsHandler(FedoraProjectRoot fedoraprojectRoot) {
 		IResource project = fedoraprojectRoot.getProject();
 		ProjectType type = FedoraHandlerUtils.getProjectType(project);
 		IExtensionPoint vcsExtensions = Platform.getExtensionRegistry()
 				.getExtensionPoint(PackagerPlugin.PLUGIN_ID, "vcsContribution"); //$NON-NLS-1$
 		if (vcsExtensions != null) {
 			IConfigurationElement[] elements = vcsExtensions
 					.getConfigurationElements();
 			for (int i = 0; i < elements.length; i++) {
 				if (elements[i].getName().equals("vcs") //$NON-NLS-1$
 						&& (elements[i].getAttribute("type") //$NON-NLS-1$
 								.equals(type.name()))) {
 					try {
 						IConfigurationElement bob = elements[i];
 						IFpProjectBits vcsContributor = (IFpProjectBits) bob
 								.createExecutableExtension("class");  //$NON-NLS-1$
 						// Do initialization
 						if (vcsContributor != null) {
 							vcsContributor.initialize(fedoraprojectRoot);
 						}
 						return vcsContributor;
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if <code>candidate</code> is a valid file for uploading.
 	 * I.e. is non-empty and has a valid file extension. Valid file extensions
 	 * are: <code>'tar', 'gz', 'bz2', 'lzma', 'xz', 'Z', 'zip', 'tff', 'bin',
      *            'tbz', 'tbz2', 'tlz', 'txz', 'pdf', 'rpm', 'jar', 'war', 'db',
      *            'cpio', 'jisp', 'egg', 'gem'</code>
 	 * 
 	 * @param candidate
 	 * @return <code>true</code> if <code>candidate</code> is a valid file for uploading
 	 * 		   <code>false</code> otherwise.
 	 */
 	public static boolean isValidUploadFile(File candidate) {
 		if (candidate.length() != 0) {
 			Pattern extensionPattern = Pattern.compile("^.*\\.(?:tar|gz|bz2|lzma|xz|Z|zip|tff|bin|tbz|tbz2|tlz|txz|pdf|rpm|jar|war|db|cpio|jisp|egg|gem)$"); //$NON-NLS-1$
 			Matcher extMatcher = extensionPattern.matcher(candidate.getName());
 			if (extMatcher.matches()) {
 				// file extension seems to be good
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Determine FAS username from <code>.fedora.cert</code>.
 	 * 
 	 * @return Username if retrieval is successful. <code>"anonymous"</code> otherwise.
 	 */
 	//TODO: Move this into Handler independent utility class?
 	public static String getUsernameFromCert() {
 		String file = System.getProperty("user.home") + IPath.SEPARATOR //$NON-NLS-1$
 				+ ".fedora.cert"; //$NON-NLS-1$
 		File cert = new File(file);
 		if (cert.exists()) {
 			KeyMaterial kmat;
 			try {
 				kmat = new KeyMaterial(cert, cert, new char[0]);
 				List<?> chains = kmat.getAssociatedCertificateChains();
 				Iterator<?> it = chains.iterator();
 				ArrayList<String> cns = new ArrayList<String>();
 				while (it.hasNext()) {
 					X509Certificate[] certs = (X509Certificate[]) it.next();
 					if (certs != null) {
 						for (int i = 0; i < certs.length; i++) {
 							cns.add(Certificates.getCN(certs[i]));
 						}
 					}
 				}
 				return cns.get(0);
 			} catch (GeneralSecurityException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return "anonymous"; //$NON-NLS-1$
 	}
 	
 	public static String makeTagName(FedoraProjectRoot projectRoot) throws CoreException {
 		String name = rpmQuery(projectRoot, "NAME").replaceAll("^[0-9]+", "");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		String version = rpmQuery(projectRoot, "VERSION");  //$NON-NLS-1$
 		String release = rpmQuery(projectRoot, "RELEASE");  //$NON-NLS-1$
 		return (name + "-" + version + "-" + release).replaceAll("\\.", "_");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 	}
 
 	public static String rpmQuery(FedoraProjectRoot projectRoot, String format)
 			throws CoreException {
 		IResource parent = projectRoot.getSpecFile().getParent();
 		String dir = parent.getLocation().toString();
 		List<String> defines = FedoraHandlerUtils.getRPMDefines(dir);
 		IFpProjectBits projectBits = FedoraHandlerUtils.getVcsHandler(projectRoot);
 		List<String> distDefines = getDistDefines(projectBits, parent.getName());
 
 		String result = null;
 		defines.add(0, "rpm"); //$NON-NLS-1$
 		defines.addAll(distDefines);
 		defines.add("-q"); //$NON-NLS-1$
 		defines.add("--qf"); //$NON-NLS-1$
 		defines.add("%{" + format + "}\\n");  //$NON-NLS-1$//$NON-NLS-2$
 		defines.add("--specfile"); //$NON-NLS-1$
 		defines.add(projectRoot.getSpecFile().getLocation().toString());
 
 		try {
 			result = Utils.runCommandToString(defines.toArray(new String[0]));
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					PackagerPlugin.PLUGIN_ID, e.getMessage(), e));
 		}
 
 		return result.substring(0, result.indexOf('\n'));
 	}
 	
 	/**
 	 * Get distribution definitions required for RPM build.
 	 * 
 	 * @param projectBits
 	 * @param parentName
 	 * @return A list of required dist-defines.
 	 */
 	public static List<String> getDistDefines(IFpProjectBits projectBits, String parentName) {
 		// substitution for rhel
 		ArrayList<String> distDefines = new ArrayList<String>();
 		String distvar = projectBits.getDistVariable().equals("epel") ? "rhel" //$NON-NLS-1$//$NON-NLS-2$ 
 				: projectBits.getDistVariable(); 
 		distDefines.add("--define"); //$NON-NLS-1$
 		distDefines.add("dist " + projectBits.getDist()); //$NON-NLS-1$
 		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add(distvar +' ' + projectBits.getDistVal()); 
 		return distDefines;
 	}
 	
 	/**
 	 * Create an IStatus error
 	 * 
 	 * @param message
 	 * @return A newly created Status instance.
 	 */
 	public static IStatus error(String message) {
 		return new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, message);
 	}
 
 	/**
 	 * Create a MessageDialog 
 	 * @param message
 	 * @param exception
 	 * @param isError
 	 * @param showInDialog
 	 * @return
 	 */
 	private static IStatus handleError(final String message, Throwable exception,
 			final boolean isError, boolean showInDialog) {
 		// do not ask for user interaction while in debug mode
 		if (showInDialog) {
 			if (Display.getCurrent() == null) {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (isError) {
 							MessageDialog.openError(null, Messages.commonHandler_fedoraPackagerName,
 									message);
 						} else {
 							MessageDialog.openInformation(null,
 									Messages.commonHandler_fedoraPackagerName, message);
 						}
 					}
 				});
 			} else {
 				if (isError) {
 					MessageDialog.openError(null, Messages.commonHandler_fedoraPackagerName, message);
 				} else {
 					MessageDialog.openInformation(null, Messages.commonHandler_fedoraPackagerName,
 							message);
 				}
 			}
 		}
 		return new Status(isError ? IStatus.ERROR : IStatus.OK,
 				PackagerPlugin.PLUGIN_ID, message, exception);
 	}
 
 	/**
 	 * Create a user-friendly IStatus error message.
 	 * 
 	 * @param message
 	 * 		The error which occurred.
 	 * @return The IStatus object.
 	 */
 	public static IStatus handleError(String message) {
 		return handleError(message, null, true, false);
 	}
 
 	/**
 	 * Create a user-friendly IStatus error message.
 	 * @param message
 	 * 		The error which occurred.
 	 * @param showInDialog
 	 * 		Show error inline?
 	 * @return The IStatus object.
 	 */
 	public static IStatus handleError(String message, boolean showInDialog) {
 		return handleError(message, null, true, showInDialog);
 	}
 
 	/**
 	 * Create a user-friendly IStatus message.
 	 * @param message
 	 * 		The message for this status.
 	 * @param showInDialog
 	 * 		Show dialog inline?
 	 * @return The IStatus object.
 	 */
 	public static IStatus handleOK(String message, boolean showInDialog) {
 		return handleError(message, null, false, showInDialog);
 	}
 
 	/**
 	 * Create a user-friendly IStatus error message.
 	 * @param e
 	 * 		The Exception which occurred.
 	 * @return The IStatus object.
 	 */
 	public static IStatus handleError(Exception e) {
 		return handleError(e.getMessage(), e, true, false);
 	}
 
 	/**
 	 * Create a user-friendly IStatus error message.
 	 * @param e
 	 * 		The Exception which occurred.
 	 * @param showInDialog
 	 * 		Show error inline?
 	 * @return The IStatus object.
 	 */
 	public static IStatus handleError(Exception e, boolean showInDialog) {
 		return handleError(e.getMessage(), e, true, showInDialog);
 	}
 
 }
