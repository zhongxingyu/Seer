 package net.sourceforge.eclipseccase.ui.actions;
 
 import net.sourceforge.eclipseccase.ClearCasePreferences;
 
 import net.sourceforge.eclipseccase.ui.compare.VersionCompareInput;
 
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.core.resources.IFile;
 
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.ui.IActionDelegate;
 
 /**
  * Pulls up the compare with predecessor dialog.
  */
 public class CompareWithVersionAction extends ClearCaseWorkspaceAction {
 
 	private IResource element = null;
 
 	private String versionA = null;
 
 	private String versionB = null;
 
 	public void setElement(IResource element) {
 		this.element = element;
 	}
 
 	public void setVersionA(String versionA) {
 		this.versionA = versionA;
 	}
 
 	public void setVersionB(String versionB) {
 		this.versionB = versionB;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isEnabled() {
 		if (element != null) {
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(element);
 			if (provider != null && !provider.isUnknownState(element) && !provider.isIgnored(element) && provider.isClearCaseElement(element))
 				return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	@Override
 	public void execute(IAction action) {
 		if (ClearCasePreferences.isCompareExternal()) {
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor) throws CoreException {
 					try {
 						String path = element.getLocation().toOSString();
 
 						String v1 = path;
 						if (versionA != null) {
 							v1 = v1 + "@@" + versionA;
 						}
 
 						String v2 = path;
 						if (versionB != null) {
 							v2 = v2 + "@@" + versionB;
 						}
 						ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(element);
 						if (p != null)
 							p.compareWithVersion(v1, v2);
 
 					} catch (Exception ex) {
 
 					} finally {
 						monitor.done();
 					}
 				}
 			};
 			executeInBackground(runnable, "Compare With Another version");
 
 		} else {
 			// 20101124 mike use new internal compare.
 			CompareConfiguration config = new CompareConfiguration();
 			config.setLeftEditable(false);
 			config.setRightEditable(false); // Could be made editable if version
 			// is null in the future.
			ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(element);
 
 			if (element instanceof IFile) {
				VersionCompareInput input = new VersionCompareInput(config, (IFile) element, versionA, versionB,p);
 				CompareUI.openCompareEditor(input);
 			}
 		}
 	}
 }
