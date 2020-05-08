 /*
    Copyright 2011 Frode Carlsen
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package archie.builder;
 
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.ui.WorkbenchException;
 
 import archie.rule.ArchieRuleModel;
 
 public class ArchieBuilder extends IncrementalProjectBuilder {
 
     class ArchieDeltaVisitor implements IResourceDeltaVisitor {
         private final ArchieRuleModel ruleModel;
 
         public ArchieDeltaVisitor(IProject project) throws WorkbenchException {
             ruleModel = new ArchieRuleModel();
             ruleModel.readProperties(project);
         }
 
         @Override
         public boolean visit(IResourceDelta delta) throws CoreException {
             IResource resource = delta.getResource();
 
             switch (delta.getKind()) {
                 case IResourceDelta.ADDED:
                     // handle added resource
                     checkResource(ruleModel, resource);
                     break;
                 case IResourceDelta.REMOVED:
                     // handle removed resource
                     break;
                 case IResourceDelta.CHANGED:
                     // handle changed resource
                     checkResource(ruleModel, resource);
                     break;
             }
             // return true to continue visiting children.
             return true;
         }
     }
 
     class ArchieResourceVisitor implements IResourceVisitor {
         private final ArchieRuleModel ruleModel;
 
         public ArchieResourceVisitor(IProject project) throws WorkbenchException {
             ruleModel = new ArchieRuleModel();
             ruleModel.readProperties(project);
         }
 
         @Override
         public boolean visit(IResource resource) {
             // return true to continue visiting children.
             checkResource(ruleModel, resource);
             return true;
         }
     }
 
     public static final String BUILDER_ID = "archie.archieBuilder";
 
     private static final String MARKER_TYPE = "archie.archieProblem";
 
     public ArchieBuilder() {
     }
 
     public void checkResource(ArchieRuleModel ruleModel, IResource resource) {
        if (!(resource instanceof IFile) || !resource.getFileExtension().equals("java")) {
             return;
         }
         deleteMarkers((IFile) resource);
         ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom((IFile) resource);
 
         ASTParser parser = ASTParser.newParser(AST.JLS3);
         parser.setStatementsRecovery(true);
         parser.setBindingsRecovery(true);
         parser.setResolveBindings(true);
         parser.setSource(compilationUnit);
         CompilationUnit cu = (CompilationUnit) parser.createAST(null);
         ArchieCompilationUnit marker = new ArchieCompilationUnit((IFile) resource, cu);
 
         ruleModel.checkRules(cu, marker);
 
     }
 
     @SuppressWarnings("rawtypes")
     @Override
     protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
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
 
     private void deleteMarkers(IFile file) {
         try {
             file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
         } catch (CoreException ce) {
             ce.printStackTrace();
         }
     }
 
     @SuppressWarnings("unused")
     protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
         try {
             getProject().accept(new ArchieResourceVisitor(getProject()));
         } catch (CoreException e) {
             e.printStackTrace();
         }
     }
 
     @SuppressWarnings("unused")
     protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
         delta.accept(new ArchieDeltaVisitor(getProject()));
     }
 
     protected void checkCancel(IProgressMonitor monitor) {
         if (monitor.isCanceled()) {
             forgetLastBuiltState();// not always necessary
             throw new OperationCanceledException();
         }
     }
 }
