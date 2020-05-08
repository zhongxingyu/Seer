 package org.nuxeo.ide.files;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 
 public class DotResourcesSyncher {
 
     protected final IPath[] dotsPath;
 
     protected final IPath resourcePath;
 
     private static final int COPY_FLAGS = IResource.DERIVED | IResource.REPLACE | IResource.FORCE;
 
     public DotResourcesSyncher(IPath root, IPath... dots) {
         this.resourcePath = root;
         this.dotsPath = dots;
     }
 
     public static DotResourcesSyncher mainResourcesSyncher() {
         return new DotResourcesSyncher(new Path("src/main/resources"), new Path("META-INF"), new Path("OSGI-INF"));
     }
 
     public boolean needsSynchronization(IProject project, IResourceDelta delta) {
         IResource resource = delta.getResource();
         IPath path = resource.getProjectRelativePath();
        for (IPath dotPath : dotsPath) {
            if (!dotPath.isPrefixOf(path)) {
                 continue;
             }
             if (isAffected(project, delta)) {
                 return true;
             }
         }
         for (IResourceDelta affected : delta.getAffectedChildren()) {
             if (needsSynchronization(project, affected)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isAffected(IProject project, IResourceDelta delta) {
         switch (delta.getKind()) {
         case IResourceDelta.NO_CHANGE:
         case IResourceDelta.ADDED_PHANTOM:
         case IResourceDelta.DESCRIPTION:
         case IResourceDelta.MARKERS:
         case IResourceDelta.OPEN:
             return false;
         }
         return true;
     }
 
     protected void createDerivedHierarchy(IFolder folder, IPath path, IProgressMonitor monitor) throws CoreException {
         if (!folder.exists()) {
             folder.create(IResource.DERIVED, true, monitor);
         }
         if (path.isEmpty()) {
             return;
         }
         folder = folder.getFolder(path.segment(0));
         path = path.removeFirstSegments(1);
         createDerivedHierarchy(folder, path, monitor);
     }
 
     protected void synchronizeDotFile(IProject project, IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
         IFolder resourceFolder = project.getFolder(resourcePath);
         IResource resource = delta.getResource();
         IPath path = resource.getProjectRelativePath();
 
         switch (delta.getKind()) {
         case IResourceDelta.REMOVED:
             IResource derived = resourceFolder.findMember(path);
             if (derived == null) {
                 return;
             }
             derived.delete(true, monitor);
             break;
         case IResourceDelta.ADDED:
         case IResourceDelta.CHANGED:
             createDerivedHierarchy(resourceFolder, path.removeLastSegments(1), monitor);
             IFile source = (IFile) resource;
             IFile target = resourceFolder.getFile(path);
             if (!target.exists()) {
                 resource.copy(resourceFolder.getFullPath().append(path), COPY_FLAGS, monitor);
             } else {
                 target.setContents(source.getContents(), 0, monitor);
             }
             break;
         }
     }
 
     public void copyToResources(IProject project, IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        if (!needsSynchronization(project, delta)) {
            return;
        }
         if (delta.getResource().getType() == IResource.FILE) {
             synchronizeDotFile(project, delta, monitor);
            return;
         }
         for (IResourceDelta affectedDelta : delta.getAffectedChildren()) {
             copyToResources(project, affectedDelta, monitor);
         }
     }
 
     public void copyToResources(final IProject project, final IProgressMonitor monitor) throws CoreException {
         final IFolder resourceFolder = project.getFolder(resourcePath);
         final IPath projectLocation = project.getLocation();
         for (IPath dotPath : dotsPath) {
             final IFolder dotFolder = project.getFolder(dotPath);
             if (!dotFolder.exists()) {
                 continue;
             }
             dotFolder.accept(new IResourceVisitor() {
                 @Override
                 public boolean visit(IResource dotResource) throws CoreException {
                     IPath path = relocatePath(projectLocation, dotResource, resourceFolder);
                     if (dotResource.getType() == IResource.FOLDER) {
                         IFolder dotFolder = (IFolder)dotResource;
                         IFolder targetFolder = resourceFolder.getFolder(path);
                         if (targetFolder.exists()) {
                             return true;
                         }
                         dotFolder.copy(targetFolder.getFullPath(), COPY_FLAGS, monitor);
                        return false;
                     } else if (dotResource.getType() == IResource.FILE) {
                         IFile dotFile = (IFile)dotResource;
                         IFile targetFile = resourceFolder.getFile(path);
                         if (targetFile.exists()) {
                             targetFile.setContents(dotFile.getContents(), 0, monitor);
                             return false;
                         }
                         dotFile.copy(targetFile.getFullPath(), COPY_FLAGS, monitor);
                         return false;
                     }
                     return false;
                 }
             });
         }
     }
 
 
     public void eraseAndCopyFromResources(IProject project, IProgressMonitor monitor) throws CoreException {
         IFolder resourcesFolder = project.getFolder(resourcePath);
         for (IPath path : dotsPath) {
             IResource source = resourcesFolder.findMember(path);
             if (source == null) {
                 return;
             }
             IResource target = project.getFolder(path);
             if (target.exists()) {
                 target.delete(false, monitor);
             }
             if (path.segmentCount() > 1) {
                 createDerivedHierarchy(project.getFolder(path.segment(0)), path.removeLastSegments(1), monitor);
             }
             source.copy(target.getFullPath(), COPY_FLAGS, monitor);
         }
     }
 
     public static final IPath DOT_PATH = new Path(".");
 
     public IPath relocatePath(IPath baseLocation, IResource resource, IFolder target) {
         IPath resourceLocation = resource.getLocation();
         IPath relativeLocation = resourceLocation.removeFirstSegments(baseLocation.segmentCount());
         return relativeLocation;
     }
 
 }
