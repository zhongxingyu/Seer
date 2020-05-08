 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.webengine;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class AnnotationBuilder extends IncrementalProjectBuilder {
 
     protected long modcount;
     protected ConcurrentMap<String, AnnotatedResource> resources;
     
 
     public AnnotationBuilder() {
         resources = new ConcurrentHashMap<String, AnnotatedResource>();
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.core.resources.IncrementalProjectBuilder#startupOnInitialize()
      */
     @Override
     protected void startupOnInitialize() {
         super.startupOnInitialize();
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
             throws CoreException {
        //double s = System.currentTimeMillis();
         long mod = modcount;
         if (kind == FULL_BUILD) {
             processProject(getProject());
         } else {
             IResourceDelta delta = getDelta(getProject());
             if (delta != null) {
                 delta.accept(new DeltaVisitor(monitor));
             }
         }
         if (mod != modcount) {
             try {
                 //System.out.println("################ Writing annotations");
                 writeState(monitor);
             } catch (Exception e) {
                 e.printStackTrace(); //TODO
             }
         }
        //System.out.println(">>>>>>>>>"+((System.currentTimeMillis()-s)/1000));
         return null;
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     protected void clean(IProgressMonitor monitor) throws CoreException {
         super.clean(monitor);
         clearState();
     }
 
     
     public void clearState() {
         resources = new ConcurrentHashMap<String, AnnotatedResource>();
         modcount = 0;
     }
     
     protected void updateState(String key, AnnotatedResource res) {
         resources.put(key, res);
         modcount++;
     }
     
     protected boolean isJavaFile(IResource file) {
         return "java".equals(file.getFileExtension());
     }
     
     class DeltaVisitor implements IResourceDeltaVisitor {
         private IProgressMonitor monitor;
 
         public DeltaVisitor(IProgressMonitor monitor) {
             this.monitor = monitor;
         }
 
         public boolean visit(IResourceDelta delta) {
             IResource resource = delta.getResource();
 
             try {
                 switch(resource.getType()) {
                 case IResource.PROJECT :
                     return Nuxeo.isWebEngineProject((IProject)resource);
                 case IResource.FOLDER:
                     return true;    
                 case IResource.FILE:
                     if (isJavaFile(resource)) {
                         if (delta.getKind() == IResourceDelta.REMOVED) {
                             removeResource((IFile)resource);
                         } else {
                             processJavaFile((IFile)resource);
                         }    
                     }
                     break;
                 }
             } catch (CoreException e) {
                 e.printStackTrace(); //TODO
             }
                     
             return false;
         }
     }
     
 
     protected void removeResource(IFile file) throws CoreException {
         resources.remove(file.getFullPath().toString());
         modcount++;
     }
     
     protected void processProject(IProject project) throws CoreException {        
         IJavaProject prj =  JavaCore.create(project);
         IPackageFragmentRoot[] roots = prj.getPackageFragmentRoots();
         for (IPackageFragmentRoot root : roots) {            
             if (root.getKind() != IPackageFragmentRoot.K_SOURCE) continue;
             IResource res = root.getResource();
             res.accept(new IResourceVisitor() {
                 public boolean visit(IResource resource) throws CoreException {
                     switch(resource.getType()) {                    
                     case IResource.FOLDER:
                         return true;
                     case IResource.FILE:
                         if (isJavaFile(resource)) {
                             processJavaFile((IFile)resource);
                         }
                         return false;
                     }
                     return false;
                 }
             });
         }
     }
     
     public void processJavaFile(IFile file) throws CoreException {
         IType type = getType(file);
         // TODO: better to check annotation fqn
         AnnotatedResource res = getAnnotatedResource(type);
         if (res != null) {
             updateState(file.getFullPath().toString(), res);
         } else {
             removeResource(file);
         }
     }
     
     public IPackageFragmentRoot getSourceFolder(IJavaElement file) {
         if (file == null) {
             return null;
         }
         if (file.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
             return (IPackageFragmentRoot)file;
         }
         return getSourceFolder(file.getParent());
     }
 
     public IJavaProject getJavaProject() {
         return JavaCore.create(getProject());
     }
 
     public synchronized void writeState(IProgressMonitor monitor) throws CoreException {
         StringBuilder buf = new StringBuilder();
         for (AnnotatedResource res : resources.values().toArray(new AnnotatedResource[resources.size()])) {
             buf.append(res.toString()).append("\r\n");
         }
         writeOutputResource(getJavaProject(), "META-INF/web-types", buf.toString().getBytes(), monitor); 
     }
     
     public void writeOutputResource(IJavaProject prj, String path, byte[] bytes, IProgressMonitor monitor) throws CoreException {
         writeOutputResource(prj, path, new ByteArrayInputStream(bytes), monitor);
     }
     
     public void mkdirs(IContainer container, IProgressMonitor monitor) throws CoreException {
         if (container == null || container.getType() != IResource.FOLDER) {
             return;
         } 
         IFolder folder = (IFolder)container;
         if (!folder.exists()) {
             mkdirs(folder.getParent(), monitor);
             folder.create(true, true, monitor);
         }
     }
     
     public void writeOutputResource(IJavaProject prj, String path, InputStream in, IProgressMonitor monitor) throws CoreException {
         IPath p = prj.getOutputLocation();
         p = p.append(path);
         IFile file = prj.getResource().getWorkspace().getRoot().getFile(p);
         if (!file.exists()) {
             mkdirs(file.getParent(), monitor);
             file.create(in, IFile.DERIVED | IFile.FORCE, monitor);
         } else {
             file.setContents(in, true, false, monitor);
         }
     }
 
     public ICompilationUnit getCompilationUnit(IResource resource) {
         if (resource.getType() == IResource.FILE) {
             return getCompilationUnit((IFile)resource); 
         }
         return null;
     }
     
     public ICompilationUnit getCompilationUnit(IFile resource) {
         IJavaElement element = JavaCore.create(resource);
         if (element != null) {
             if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
                 return (ICompilationUnit)element;
             }
         }
         return null;
     }
     
     public IType getFirstType(ICompilationUnit unit) throws CoreException {
         IType[] types = unit.getTypes();
         return types.length > 0 ? types[0] : null;
     }
 
     public IType getType(IResource resource) throws CoreException {
         ICompilationUnit unit = getCompilationUnit(resource);
         return unit != null ? getFirstType(unit) : null;
     }
 
     //TODO it doesn't work as expected
     public boolean hasAnnotation(IType type, String name) {
         return type.getAnnotation(name).exists();
     }
 
     public static IJavaProject getavaProject(IResource resource) {
         return JavaCore.create(resource.getProject());
     }
 
     public AnnotatedResource getAnnotatedResource(IType type) throws CoreException {
         IAnnotation[] annos = type.getAnnotations();        
         for (IAnnotation anno : annos) {
             String name = anno.getElementName();
             if ("WebObject".equals(name)) {
                 return new WebObjectResource(type, anno);
             } else if ("WebAdapter".equals(name)) {
                 return new WebAdapterResource(type, anno);
             }
         }
         return null;
     }
     
 }
