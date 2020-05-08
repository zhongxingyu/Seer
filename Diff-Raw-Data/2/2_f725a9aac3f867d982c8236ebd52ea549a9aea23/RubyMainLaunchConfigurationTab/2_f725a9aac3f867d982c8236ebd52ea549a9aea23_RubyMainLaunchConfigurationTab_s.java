 package org.eclipse.dltk.ruby.internal.debug.ui.launchConfigurations;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.debug.ui.launchConfigurations.MainLaunchConfigurationTab;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.swt.graphics.Image;
 
 public class RubyMainLaunchConfigurationTab extends MainLaunchConfigurationTab {
 
 
 	protected boolean validateProject(IDLTKProject project) {
 		if (project == null)
 			return false;
 		// check project nature		
 		try {
			IDLTKLanguageToolkit ltk = DLTKLanguageManager.getLangaugeToolkit(project);
 			if (ltk instanceof RubyLanguageToolkit)
 				return true;
 		} catch (CoreException e) {
 		}
 		return false;
 	}
 	
 	protected String getLanguageName () {
 		return "RUBY";
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
 	 */
 	public Image getImage() {
 		return DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_CLASS);
 	}
 
 }
