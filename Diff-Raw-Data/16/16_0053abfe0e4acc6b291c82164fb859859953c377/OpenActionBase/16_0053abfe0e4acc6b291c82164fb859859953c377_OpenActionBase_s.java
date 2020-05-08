 package org.seasar.mayaa.matatabi.action;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.seasar.mayaa.matatabi.MatatabiPlugin;
 import org.seasar.mayaa.matatabi.util.EditorUtil;
 import org.seasar.mayaa.matatabi.util.PreferencesUtil;
 
 public abstract class OpenActionBase extends ActionBase {
 	protected IPath path;
 
 	protected IProject project;
 
 	protected IEditorPart openEditorPart;
 	protected ScopedPreferenceStore store = null;
 
 	protected String baseDir;
 	protected String targetBaseDir;
 	private String targetExtension;
 
 	/**
 	 * Constructor for Action1.
 	 */
 	public OpenActionBase(String targetExtension) {
 		super();
 		this.targetExtension = targetExtension;
 	}
 
 	protected abstract void init();
 
 	protected String getResourceName(String baseName) {
 		return baseName;
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		if (path == null && targetPart instanceof IEditorPart) {
 			IFile file = ((IFileEditorInput) ((IEditorPart) targetPart)
 					.getEditorInput()).getFile();
 			path = file.getProjectRelativePath();
 			project = file.getProject();
 			store = PreferencesUtil.getPreference(file);
 		} else {
 			store = PreferencesUtil.getPreference(project);
 		}
 
 		init();
 		if (path.toString().startsWith(baseDir)) {
 			IFile openFile = getTargetFile();
 			if (openFile == null || !openFile.exists()) {
 				return;
 			}
 			try {
 				openEditorPart = EditorUtil.openFile(openFile);
 			} catch (PartInitException e) {
 				MatatabiPlugin.errorLog(e);
 			}
 		}
 	}
 
 	protected IFile getTargetFile() {
		String[] filaPath = path.toString().substring(baseDir.length()).split(
 				"/");
 		StringBuilder className = new StringBuilder("");
		for (int i = 0; i < filaPath.length - 1; i++) {
			if (!filaPath[i].equals("")) {
				className.append("." + filaPath[i]);
 			}
 		}
		String baseName = filaPath[filaPath.length - 1].substring(0,
				filaPath[filaPath.length - 1].indexOf("."));
		className.append(getResourceName(baseName));
 
 		return project.getFile(targetBaseDir + "/"
 				+ className.toString().replace('.', '/') + "."
 				+ targetExtension);
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		super.selectionChanged(action, selection);
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection ss = (StructuredSelection) selection;
 			Object obj = ss.getFirstElement();
 			if (obj instanceof IFile) {
 				path = ((IFile) obj).getProjectRelativePath();
 				project = ((IFile) obj).getProject();
 			}
 		} else {
 			path = null;
 			project = null;
 		}
 	}
 }
