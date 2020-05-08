 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.diigu.eclipse.builder;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.Signature;
 import org.seasar.diigu.ParameterNameEnhancer;
 import org.seasar.diigu.eclipse.DiiguPlugin;
 import org.seasar.diigu.eclipse.nls.Messages;
 
 public class DiiguBuilder extends IncrementalProjectBuilder {
 
     public static final String BUILDER_ID = "org.seasar.diigu.eclipse.diiguBuilder";
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
      *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
      */
     protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
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
 
     protected void fullBuild(IProgressMonitor monitor) throws CoreException {
         Job job = new WorkspaceJob(Messages.ENHANCE_FULLBUILD) {
             public IStatus runInWorkspace(final IProgressMonitor monitor)
                     throws CoreException {
                 getProject().accept(new IResourceVisitor() {
                     public boolean visit(IResource resource)
                             throws CoreException {
                         enhance(resource, monitor);
                         return true;
                     }
                 });
                 return Status.OK_STATUS;
             }
 
         };
         job.setPriority(Job.SHORT);
         job.schedule();
     }
 
     protected void incrementalBuild(IResourceDelta delta,
             final IProgressMonitor monitor) throws CoreException {
         delta.accept(new IResourceDeltaVisitor() {
             public boolean visit(IResourceDelta delta) throws CoreException {
                 final int MASK = IResourceDelta.ADDED | IResourceDelta.CHANGED;
                 if ((MASK & delta.getKind()) != 0) {
                     enhance(delta.getResource(), monitor);
                 }
                 return true;
             }
         });
     }
 
     void enhance(IResource resource, IProgressMonitor monitor)
             throws CoreException {
         if (resource instanceof IFile && resource.getName().endsWith(".java")) {
             try {
                 if (monitor == null) {
                     monitor = new NullProgressMonitor();
                 }
 
                 ICompilationUnit unit = JavaCore
                         .createCompilationUnitFrom((IFile) resource);
                 enhance(unit, monitor);
             } catch (CoreException e) {
                 throw e;
             } catch (Exception e) {
                 e.printStackTrace();
                 IStatus status = new Status(IStatus.ERROR,
                         DiiguPlugin.PLUGIN_ID, 0, e.getMessage(), e);
                 throw new CoreException(status);
             }
         }
     }
 
     protected void enhance(ICompilationUnit unit, IProgressMonitor monitor)
             throws CoreException, Exception {
         monitor.beginTask(Messages.ENHANCE_BEGIN, 3);
         try {
             IPath outpath = unit.getJavaProject().getOutputLocation();
             // FIXME : クラスローダは毎回作らなければならないが、URL[]を毎回作るのは、どうなのかな…。
             // そもそも、変更の入ったクラスのローダは作り直さないといけないけど、
             // 参照ライブラリの類をロードする為のローダは、毎回作り直す必然性が無いんじゃね？
             // どっかでイベント拾って作る方が、体感速度があがると思われ。
             URLClassLoader loader = new URLClassLoader(getClassPathEntries(unit
                     .getJavaProject()));
             monitor.worked(1);
             IProgressMonitor submonitor = new SubProgressMonitor(monitor, 1);
             IType[] types = unit.getAllTypes();
             submonitor.beginTask(Messages.ENHANCE_PROCEED, types.length);
             DiiguNature nature = DiiguNature.getInstance(unit.getJavaProject()
                     .getProject());
             Pattern ptn = null;
             if (nature != null) {
                 ptn = nature.getSelectExpression();
             }
             for (int i = 0; i < types.length; i++) {
                 IType type = types[i];
                 String typename = type.getFullyQualifiedName();
 
                 if (ptn != null && ptn.matcher(typename).matches()) {
                     submonitor.subTask(typename);
                     IPath path = outpath.append(type.getFullyQualifiedName()
                             .replace('.', '/')
                             + ".class");
                     IResource resource = getProject().getParent().getFile(path);
                     ParameterNameEnhancer enhancer = new ParameterNameEnhancer(
                             type.getFullyQualifiedName(), loader);
 
                     if (enhanceClassFile(type, enhancer)) {
                         enhancer.save();
                         resource.refreshLocal(IResource.DEPTH_ONE, monitor);
                     }
                     submonitor.worked(1);
                     if (submonitor.isCanceled()) {
                         break;
                     }
                 }
             }
             submonitor.done();
             monitor.setTaskName(Messages.ENHANCE_END);
             monitor.worked(1);
         } finally {
             monitor.done();
         }
     }
 
     protected URL[] getClassPathEntries(IJavaProject project)
             throws CoreException, Exception {
         Set urls = new HashSet();
         Set already = new HashSet();
         addClasspathEntries(project, urls, already, true);
         return (URL[]) urls.toArray(new URL[urls.size()]);
     }
 
     protected void addClasspathEntries(IJavaProject project, Set urls,
             Set already, boolean atFirst) throws CoreException, Exception {
         already.add(project);
 
         IPath path = project.getOutputLocation();
         urls.add(toURL(project.getProject().getParent().getFolder(path)
                 .getLocation()));
 
         IClasspathEntry[] entries = project.getResolvedClasspath(true);
         for (int i = 0; i < entries.length; i++) {
             IClasspathEntry entry = entries[i];
             switch (entry.getEntryKind()) {
             case IClasspathEntry.CPE_SOURCE:
                 IPath dist = entry.getOutputLocation();
                 if (dist != null) {
                     urls.add(toURL(project.getProject().getParent().getFolder(
                             dist).getLocation()));
                 }
                 break;
             case IClasspathEntry.CPE_LIBRARY:
             case IClasspathEntry.CPE_CONTAINER:
             case IClasspathEntry.CPE_VARIABLE:
                 urls.add(toURL(entry.getPath()));
                 break;
             case IClasspathEntry.CPE_PROJECT:
                 IJavaProject proj = getJavaProject(entry.getPath());
                 if (proj != null && already.contains(proj) == false
                         && (atFirst || entry.isExported())) {
                     addClasspathEntries(proj, urls, already, false);
                 }
                 break;
             default:
                 break;
             }
         }
     }
 
     protected URL toURL(IPath path) throws Exception {
        return path.toFile().toURI().toURL();
     }
 
     protected IJavaProject getJavaProject(IPath path) throws CoreException {
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                 path.segment(0));
         return JavaCore.create(project);
     }
 
     protected boolean enhanceClassFile(IType type,
             ParameterNameEnhancer enhancer) throws CoreException {
         boolean dirty = false;
         IMethod[] methods = type.getMethods();
         for (int i = 0; i < methods.length; i++) {
             dirty |= enhanceMethod(methods[i], enhancer);
         }
         return dirty;
     }
 
     protected boolean enhanceMethod(IMethod method,
             ParameterNameEnhancer enhancer) throws CoreException {
         if (method.getNumberOfParameters() < 1) {
             return false;
         }
         String[] typeSignatures = method.getParameterTypes();
         String[] parameterTypes = new String[typeSignatures.length];
         for (int i = 0; i < typeSignatures.length; i++) {
             parameterTypes[i] = getResolvedTypeName(typeSignatures[i], method
                     .getDeclaringType());
         }
         String[] parameterNames = method.getParameterNames();
         if (method.isConstructor()) {
             enhancer.setConstructorParameterNames(parameterTypes,
                     parameterNames);
         } else {
             String methodName = method.getElementName();
             enhancer.setMethodParameterNames(methodName, parameterTypes,
                     parameterNames);
         }
         return true;
     }
 
     public static String getResolvedTypeName(String typeSignature, IType type)
             throws JavaModelException {
         int count = Signature.getArrayCount(typeSignature);
         if (Signature.C_UNRESOLVED == typeSignature.charAt(count)) {
             String name = null;
             if (0 < count) {
                 name = typeSignature.substring(count + 1, typeSignature
                         .indexOf(';'));
             } else {
                 name = Signature.toString(typeSignature);
             }
 
             String[][] resolvedNames = type.resolveType(name);
             if (resolvedNames != null && resolvedNames.length > 0) {
                 StringBuffer stb = new StringBuffer();
                 stb.append(resolvedNames[0][0]);
                 stb.append('.');
                 stb.append(resolvedNames[0][1].replace('.', '$'));
                 for (int i = 0; i < count; i++) {
                     stb.append("[]");
                 }
                 return stb.toString();
             }
             return "";
         } else {
             return Signature.toString(typeSignature);
         }
     }
 
 }
