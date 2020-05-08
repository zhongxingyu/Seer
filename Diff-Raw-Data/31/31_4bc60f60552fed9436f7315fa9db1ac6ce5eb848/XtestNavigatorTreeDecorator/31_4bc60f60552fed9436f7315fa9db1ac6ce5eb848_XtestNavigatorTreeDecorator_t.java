 package org.xtest.ui.decorators;
 
 import org.eclipse.core.internal.runtime.AdapterManager;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
 import org.eclipse.jface.viewers.IDecoration;
 import org.xtest.ui.internal.XtestPluginImages;
 
 /**
  * Custom icon decorator for Xtest files and projects/packages containing xtest files
  * 
  * @author Michael Barry
  */
public class XtestNavigatorTreeDecorator extends ProblemsLabelDecorator {
 
     @Override
     public void decorate(Object input, IDecoration decorator) {
         Object adapter = AdapterManager.getDefault().getAdapter(input, IResource.class);
         if (adapter instanceof IResource) {
             IResource resource = (IResource) adapter;
             try {
                 // Don't show errors from
                 int depth = input instanceof IPackageFragment ? IResource.DEPTH_ONE
                         : IResource.DEPTH_INFINITE;
                if (containsXtestFile(resource, depth) && 0 == computeAdornmentFlags(input)) {
                     decorator.addOverlay(XtestPluginImages.OK_OVERLAY, IDecoration.BOTTOM_LEFT);
                 }
             } catch (CoreException e) {
             }
         }
     }
 
     private boolean containsXtestFile(IResource input, int depth) throws CoreException {
        boolean result = false;
         int nextDepth = depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE
                 : IResource.DEPTH_ZERO;
        String fileExtension = input instanceof IFile ? ((IFile) input).getFileExtension() : null;
        if (fileExtension != null && fileExtension.equals("xtest")) {
            result = true;
         } else if (input instanceof IContainer && depth != IResource.DEPTH_ZERO) {
             for (IResource resource : ((IContainer) input).members()) {
                 if (containsXtestFile(resource, nextDepth)) {
                    result = true;
                 }
             }
         }
        return result;
     }
 }
