 package org.xtest.ui.decorators;
 
 import org.eclipse.core.internal.runtime.AdapterManager;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
 import org.xtest.ui.internal.XtestPluginImages;
 
 /**
  * Custom icon decorator for Xtest files and projects/packages containing xtest files
  * 
  * @author Michael Barry
  */
public class XtestNavigatorTreeDecorator extends LabelProvider implements
        ILightweightLabelDecorator {
 
     @Override
     public void decorate(Object input, IDecoration decorator) {
         Object adapter = AdapterManager.getDefault().getAdapter(input, IResource.class);
         if (adapter instanceof IResource) {
             IResource resource = (IResource) adapter;
             try {
                 // Don't show errors from
                 int depth = input instanceof IPackageFragment ? IResource.DEPTH_ONE
                         : IResource.DEPTH_INFINITE;
                int severity = resource.findMaxProblemSeverity(null, true, depth);
                if (severity != IMarker.SEVERITY_WARNING && severity != IMarker.SEVERITY_ERROR
                        && containsXtestFile(resource, depth)) {
                     decorator.addOverlay(XtestPluginImages.OK_OVERLAY, IDecoration.BOTTOM_LEFT);
                 }
             } catch (CoreException e) {
                e.printStackTrace();
             }
         }
     }
 
     private boolean containsXtestFile(IResource input, int depth) throws CoreException {
         int nextDepth = depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE
                 : IResource.DEPTH_ZERO;
        if (input instanceof IFile && ((IFile) input).getFileExtension().equals("xtest")) {
            return true;
         } else if (input instanceof IContainer && depth != IResource.DEPTH_ZERO) {
             for (IResource resource : ((IContainer) input).members()) {
                 if (containsXtestFile(resource, nextDepth)) {
                    return true;
                 }
             }
         }
        return false;
     }
 }
