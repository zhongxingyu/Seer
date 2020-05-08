 package com.rytong.ui.internal.wizard;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
 import org.erlide.backend.BackendCore;
 import org.erlide.core.ErlangCore;
 import org.erlide.core.ErlangPlugin;
 import org.erlide.core.internal.model.root.OldErlangProjectProperties;
 import org.erlide.core.internal.model.root.PathSerializer;
 import org.erlide.core.model.root.ErlModelManager;
 import org.erlide.core.model.root.IErlProject;
 import org.erlide.core.model.util.PluginUtils;
 import org.erlide.jinterface.ErlLogger;
 import org.osgi.service.prefs.BackingStoreException;
 
 import com.rytong.ui.internal.RytongUIMessages;
 import com.rytong.ui.internal.RytongUIPlugin;
 import com.rytong.ui.RytongUIConstants;
 
 @SuppressWarnings("restriction")
 public class NewAppProjectWizard extends Wizard implements INewWizard {
 
 	private WizardNewProjectCreationPage namePage;
 	private VersionSelectionPage buildPage;
 	private TemplateListSelectionPage tempPage;
 	private AppFieldData fData;
 	private IProject project;
 
 	public NewAppProjectWizard() {
 		super();
 		// setNeedsProgressMonitor(true);
 		setWindowTitle(RytongUIMessages.NewAppProjectWizard_title);
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		setNeedsProgressMonitor(true);
 	}
 
 	@Override
 	public boolean performFinish() {
 		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		project = root.getProject(namePage.getProjectName());
 		IProjectDescription description = ResourcesPlugin.getWorkspace()
 				.newProjectDescription(project.getName());
 		if (!Platform.getLocation().equals(namePage.getLocationPath())) {
 			description.setLocation(namePage.getLocationPath());
 		}
 		try {
 			project.create(description, null);
 			project.open(null);
 			description = project.getDescription();
 
 			description.setNatureIds(new String[] { ErlangCore.NATURE_ID });
 			project.setDescription(description, null);
 			set_ewp_path();
 
 			final IErlProject erlProject = ErlModelManager.getErlangModel()
 					.getErlangProject(project);
 			OldErlangProjectProperties prefs;
 			prefs = new OldErlangProjectProperties();
 			prefs.setRuntimeVersion(BackendCore.getRuntimeInfoManager()
 					.getDefaultRuntime().getVersion());
 			String includeDir = "include";
 			if (ErlangPlugin.yawsPath != null)
 				includeDir += ";" + ErlangPlugin.yawsPath + "/include";
 			if (ErlangPlugin.ewpPath != null) {
 				includeDir += ";" + ErlangPlugin.ewpPath + "/include";
 				includeDir += ";" + ErlangPlugin.ewpPath + "/include/models";
 				includeDir += ";" + ErlangPlugin.ewpPath + "/include/internal";
 				includeDir += ";" + ErlangPlugin.ewpPath
 						+ "/drivers/db/include";
 			}
 			prefs.setIncludeDirs(PathSerializer.unpackList(includeDir));
 			erlProject.setAllProperties(prefs);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 		}
 		File templateDirectory = new File(fData.template.getLocation());
 		generateFiles(templateDirectory, project);
 		// String[] appdirs = RytongUIPlugin.getAppDirs();
 		// List<String> sresult = new ArrayList<String>(Arrays.asList(appdirs));
 		// List<IPath> result = new ArrayList<IPath>();
 		// for (final String s : sresult) {
 		// result.add(new Path(s));
 		// }
 		// buildPaths(root, project, result);
 		return true;
 	}
 
 	private void generateFiles(File src, IContainer dst) {
 		File[] members = src.listFiles();
 
 		for (int i = 0; i < members.length; i++) {
 			File member = members[i];
 			if (member.isDirectory()) {
 				// IPath path = new Path(member.getName());
 				IContainer dstContainer = dst.getFolder(new Path(member
 						.getName()));
 
 				if (dstContainer instanceof IFolder && !dstContainer.exists())
 					try {
 						((IFolder) dstContainer).create(true, true, null);
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 				generateFiles(member, dstContainer);
 			} else {
 				InputStream in = null;
 				try {
 					in = new FileInputStream(member);
 
 					String fileName = member.getName();
 					boolean binary = false;
 					if (fileName.endsWith(".png") || fileName.endsWith(".jpg")
 							|| fileName.endsWith(".gif"))
 						binary = true;
 					if (!fileName
 							.equalsIgnoreCase(RytongUIConstants.TEMPLATE_PROPFILE))
 						copyFile(fileName, in, dst, binary, null);
 				} catch (IOException ioe) {
 				} finally {
 					if (in != null)
 						try {
 							in.close();
 						} catch (IOException ioe2) {
 						}
 				}
 			}
 		}
 
 	}
 
 	// private IFolder generateSubFolder(IFolder curFolder, IPath path) {
 	// //IPath path = new Path(packageName.replace('.', File.separatorChar));
 	// if (curFolder != null)
 	// path = curFolder.getProjectRelativePath().append(path);
 	//
 	// for (int i = 1; i <= path.segmentCount(); i++) {
 	// IPath subpath = path.uptoSegment(i);
 	// IFolder subfolder = project.getFolder(subpath);
 	// if (subfolder.exists() == false)
 	// try {
 	// subfolder.create(true, true, null);
 	// } catch (CoreException e) {
 	// e.printStackTrace();
 	// }
 	// }
 	// return project.getFolder(path);
 	// }
 
 	private void copyFile(String fileName, InputStream input, IContainer dst,
 			boolean binary, IProgressMonitor monitor) {
 		String targetFileName = getProcessedString(fileName, fileName);
 
 		IFile dstFile = dst.getFile(new Path(targetFileName));
 
 		try {
 			InputStream stream = getProcessedStream(fileName, input, binary);
 			if (dstFile.exists()) {
 				dstFile.setContents(stream, true, true, monitor);
 			} else {
 				dstFile.create(stream, true, monitor);
 			}
 			stream.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getProcessedString(String fileName, String source) {
 		if (source.indexOf("${") == -1)
 			return source;
 		int loc = -1;
 		StringBuffer buffer = new StringBuffer();
 		boolean replacementMode = false;
 		int len = source.length();
 		for (int i = 0; i < len; i++) {
 			char c = source.charAt(i);
 			if (c == '$' && i<len-1 && source.charAt(i+1)=='{' && !replacementMode) {
 				replacementMode = true;
 				loc = i + 2;
 				continue;
 			} else if (c == '}' && replacementMode) {
 				String key = source.substring(loc, i);
				String value = key.length() == 0 ? "$" //$NON-NLS-1$
						: getReplacementString(fileName, key);
 				buffer.append(value);
 				replacementMode = false;
 			} else if (!replacementMode)
 				buffer.append(c);
 		}
 		return buffer.toString();
 	}
 
 	public String getReplacementString(String fileName, String key) {
		if ("app".endsWith("app")) {
 			return namePage.getProjectName();
		} else
			return key;
 	}
 
 	// TODO We should do some replace here.
 	private InputStream getProcessedStream(String fileName, InputStream stream,
 			boolean binary) throws IOException, CoreException {
 		 if (binary)
 			 return stream;
 
 		 InputStreamReader reader = new InputStreamReader(stream);
 		 int bufsize = 1024;
 		 char[] cbuffer = new char[bufsize];
 		 int read = 0;
 		 StringBuffer keyBuffer = new StringBuffer();
 		 StringBuffer outBuffer = new StringBuffer();
 		// StringBuffer preBuffer = new StringBuffer();
 		// boolean newLine = true;
 		// ControlStack preStack = new ControlStack();
 		// preStack.setValueProvider(this);
 		//
 		 boolean replacementMode = false;
 		// boolean preprocessorMode = false;
 		// boolean escape = false;
 		 while (read != -1) {
 		 read = reader.read(cbuffer);
 		 for (int i = 0; i < read; i++) {
 		 char c = cbuffer[i];
 		//
 		// if (escape) {
 		// StringBuffer buf = preprocessorMode ? preBuffer : outBuffer;
 		// buf.append(c);
 		// escape = false;
 		// continue;
 		// }
 		//
 		// if (newLine && c == '%') {
 		// // preprocessor line
 		// preprocessorMode = true;
 		// preBuffer.delete(0, preBuffer.length());
 		// continue;
 		// }
 		// if (preprocessorMode) {
 		// if (c == '\\') {
 		// escape = true;
 		// continue;
 		// }
 		// if (c == '\n') {
 		// // handle line
 		// preprocessorMode = false;
 		// newLine = true;
 		// String line = preBuffer.toString().trim();
 		// preStack.processLine(line);
 		// continue;
 		// }
 		// preBuffer.append(c);
 		//
 		// continue;
 		// }
 		//
 		// if (preStack.getCurrentState() == false) {
 		// continue;
 		// }
 		//
 			if (c == '$' && i<read-1 && cbuffer[i+1]=='{' && !replacementMode) {
 				replacementMode = true;
 				continue;
 			} else if (c == '}' && replacementMode) {
 				String key = keyBuffer.toString();
				String value = key.length() == 0 ? "$" //$NON-NLS-1$
						: getReplacementString(fileName, key);
 				outBuffer.append(value);
 				replacementMode = false;
 			} else if (!replacementMode) {
 				outBuffer.append(c);
 			} else
 				keyBuffer.append(c);
 		 }
 		 }
 		return new ByteArrayInputStream(outBuffer.toString().getBytes(project.getDefaultCharset()));
 	}
 
 	// private void buildPaths(IWorkspaceRoot root, IProject project,
 	// List<IPath> result) {
 	// if (result != null) {
 	// final IPath projectPath = project.getFullPath();
 	// for (final IPath pp : result) {
 	// if (!pp.isAbsolute() && !pp.toString().equals(".")
 	// && !pp.isEmpty()) {
 	// final IPath path = projectPath.append(pp);
 	// final IFolder folder = root.getFolder(path);
 	// createFolderHelper(folder);
 	// }
 	// }
 	// }
 	//
 	// }
 
 	// private void createFolderHelper(IFolder folder) {
 	// if (!folder.exists()) {
 	// final IContainer parent = folder.getParent();
 	// if (parent instanceof IFolder && !((IFolder) parent).exists()) {
 	// createFolderHelper((IFolder) parent);
 	// }
 	// try {
 	// folder.create(false, true, null);
 	// } catch (CoreException e) {
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// }
 
 	@Override
 	public void addPages() {
 		try {
 			fData = new AppFieldData();
 
 			super.addPages();
 			namePage = new WizardNewProjectCreationPage("main");
 			namePage.setTitle(RytongUIMessages.NewAppProjectWizard_title);
 			namePage.setDescription(RytongUIMessages.NewAppProjectWizard_desc);
 			namePage.setImageDescriptor(RytongUIPlugin.getDefault()
 					.getImageDescriptor(
 							RytongUIConstants.IMG_NEW_PROJECT_WIZARD));
 			addPage(namePage);
 			namePage.getProjectName();
 
 			buildPage = new VersionSelectionPage("select_version", fData);
 			buildPage.setTitle(RytongUIMessages.VersionSelectionPage_title);
 			buildPage
 					.setDescription(RytongUIMessages.VersionSelectionPage_desc);
 			buildPage.setImageDescriptor(RytongUIPlugin.getDefault()
 					.getImageDescriptor(
 							RytongUIConstants.IMG_NEW_PROJECT_WIZARD));
 			addPage(buildPage);
 
 			tempPage = new TemplateListSelectionPage("select_template", fData);
 			tempPage.setTitle(RytongUIMessages.TemplateListSelectionPage_title);
 			tempPage.setDescription(RytongUIMessages.TemplateListSelectionPage_desc);
 			tempPage.setImageDescriptor(RytongUIPlugin.getDefault()
 					.getImageDescriptor(
 							RytongUIConstants.IMG_NEW_PROJECT_WIZARD));
 			addPage(tempPage);
 		} catch (final Exception x) {
 			reportError(x);
 		}
 	}
 
 	@Override
 	public boolean canFinish() {
 		IWizardPage page = getContainer().getCurrentPage();
 		return super.canFinish() && (page == tempPage);
 	}
 
 	@Override
 	public void createPageControls(Composite pageContainer) {
 	}
 
 	private void reportError(final Exception x) {
 		ErlLogger.error(x);
 		ErrorDialog.openError(getShell(), RytongUIPlugin
 				.getResourceString("wizards.errors.projecterrordesc"),
 				RytongUIPlugin
 						.getResourceString("wizards.errors.projecterrortitle"),
 				PluginUtils.makeStatus(x));
 	}
 
 	protected void createProject(final IProgressMonitor monitor) {
 	}
 
 	private void set_ewp_path() {
 		if (fData.ewpPath != null) {
 			File ewp = new File(fData.ewpPath);
 			if (ewp.isDirectory())
 				ErlangPlugin.ewpPath = fData.ewpPath;
 				ErlangPlugin.ewpVer = ErlangPlugin.get_ewp_version();
 		}
 	}
 }
