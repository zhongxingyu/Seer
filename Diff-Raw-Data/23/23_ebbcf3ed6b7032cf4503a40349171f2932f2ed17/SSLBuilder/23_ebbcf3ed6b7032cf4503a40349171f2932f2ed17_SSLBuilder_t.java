 package fdt.builder;
 
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 public class SSLBuilder extends IncrementalProjectBuilder {
 
    public static final String BUILDER_ID = "FDT.SSLBuilder";
 
     public static boolean checkSSLName(String name) {
         return name.matches(".*\\.[Ss][Ss][Ll]$");
     }
 
     protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor)
             throws CoreException {
         if (kind == FULL_BUILD) {
             fullBuild(monitor);
         } else {
             IResourceDelta delta = getDelta(getProject());
             if (delta == null) {
                 fullBuild(monitor);
             } else {
                 incrementalBuild(delta, monitor);
             }
         }
         return null;
     }
 
     private int getAll(IResource resource) throws CoreException {
 
         if (resource.getType() == IResource.FILE && checkSSLName(resource.getName())) {
             return 1;
         } else if (resource instanceof IContainer) {
             IContainer folder = (IContainer) resource;
             int res = 0;
             for (IResource d : folder.members()) {
                 res += getAll(d);
             }
             return res;
         }
 
         return 0;
     }
 
     protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
         try {
             getProject().accept(new SSLCResourceVisitor(getProject(), monitor, getAll(getProject())));
         } catch (CoreException e) {}
     }
 
     private int getChanged(IResourceDelta delta) throws CoreException {
         IResource resource = delta.getResource();
         if (resource.getType() == IResource.FILE && checkSSLName(resource.getName())) {
             return 1;
         } else if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
             int res = 0;
             for (IResourceDelta d : delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.CHANGED)) {
                 res += getChanged(d);
             }
 
             return res;
         }
 
         return 0;
     }
 
     protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
         delta.accept(new SSLCResourceVisitor(getProject(), monitor, getChanged(delta)));
     }
 }
