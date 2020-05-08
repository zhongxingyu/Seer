 package org.eclipse.dltk.ruby.internal.ui;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jface.viewers.ILightweightLabelDecorator;
 import org.eclipse.jface.viewers.LabelProvider;
 
 public class RubyProjectDecorator extends LabelProvider implements
 		ILightweightLabelDecorator {
 
 	public void decorate(Object element, IDecoration decoration) {
 		IProject project = null;
 		if (element instanceof IScriptProject) {
 			project = ((IScriptProject) element).getProject();
 		} else if (element instanceof IProject) {
 			project = (IProject) element;
 		}
 
 		if (project != null) {
 			try {
				if (project.hasNature(RubyNature.NATURE_ID)) {
 					decoration.addOverlay(RubyImages.PROJECT_DECARATOR);
 				}
 			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
 		}
 	}
 }
