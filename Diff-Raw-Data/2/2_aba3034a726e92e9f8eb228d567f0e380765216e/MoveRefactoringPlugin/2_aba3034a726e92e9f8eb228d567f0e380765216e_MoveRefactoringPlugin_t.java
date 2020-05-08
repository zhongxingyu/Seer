 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl.plugins;
 
 import org.netbeans.modules.javafx.refactoring.impl.plugins.elements.BaseRefactoringElementImplementation;
 import java.io.IOException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.lang.model.element.ElementKind;
 import org.netbeans.api.java.source.JavaSource;
 import org.netbeans.api.java.source.TreePathHandle;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.modules.javafx.refactoring.RefactoringSupport;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.SourceUtils;
 import org.netbeans.modules.javafx.refactoring.impl.plugins.elements.FixImportsElement;
 import org.netbeans.modules.javafx.refactoring.impl.plugins.elements.ReindexFileElement;
 import org.netbeans.modules.javafx.refactoring.impl.plugins.elements.RenameOccurencesElement;
 import org.netbeans.modules.javafx.refactoring.impl.plugins.elements.UpdatePackageDeclarationElement;
 import org.netbeans.modules.javafx.refactoring.impl.scanners.MoveProblemCollector;
 import org.netbeans.modules.javafx.refactoring.repository.ClassModel;
 import org.netbeans.modules.javafx.refactoring.repository.ElementDef;
 import org.netbeans.modules.javafx.refactoring.repository.ImportEntry;
 import org.netbeans.modules.javafx.refactoring.repository.ImportSet;
 import org.netbeans.modules.javafx.refactoring.repository.PackageDef;
 import org.netbeans.modules.javafx.refactoring.repository.Usage;
 import org.netbeans.modules.javafx.refactoring.transformations.InsertTextTransformation;
 import org.netbeans.modules.javafx.refactoring.transformations.RemoveTextTransformation;
 import org.netbeans.modules.javafx.refactoring.transformations.ReplaceTextTransformation;
 import org.netbeans.modules.javafx.refactoring.transformations.Transformation;
 import org.netbeans.modules.refactoring.api.MoveRefactoring;
 import org.netbeans.modules.refactoring.api.Problem;
 import org.netbeans.modules.refactoring.api.RefactoringSession;
 import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author Jaroslav Bachorik <yardus@netbeans.org>
  */
 public class MoveRefactoringPlugin extends JavaFXRefactoringPlugin {
     private MoveRefactoring refactoring;
 
     private Map<FileObject, Set<ElementDef>> movingDefs = null;
     private Map<String, Set<FileObject>> relatedPerPkg = null;
     private Set<FileObject> related = null;
     private Map<String, String> renames = null;
     private Map<String, String> pkgRenames = null;
 
     public MoveRefactoringPlugin(MoveRefactoring refactoring) {
         this.refactoring = refactoring;
     }
 
     public Problem checkParameters() {
         // for now use MoveProblemCollector; might rewrite it to use ClassModel later on
         final Set<String> movingClasses = new HashSet<String>();
 
         Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
 
         collectMovingData(RefactoringSupport.classIndex(refactoring));
 
         final Problem p[] = new Problem[1];
 
         for(Map.Entry<FileObject, Set<ElementDef>> entry : movingDefs.entrySet()) {
             for(ElementDef edef : entry.getValue()) {
                 movingClasses.add(edef.createHandle().getQualifiedName());
             }
         }
 
         Collection<FileObject> allFiles = new ArrayList<FileObject>();
         allFiles.addAll(files);
         allFiles.addAll(related);
         
         fireProgressListenerStart(MoveRefactoring.PARAMETERS_CHECK, allFiles.size());
         for(FileObject f : allFiles) {
             if (!SourceUtils.isJavaFXFile(f)) continue;
             JavaFXSource jfxs = JavaFXSource.forFileObject(f);
             try {
                 jfxs.runUserActionTask(new Task<CompilationController>() {
 
                     public void run(CompilationController cc) throws Exception {
                         MoveProblemCollector mpc = new MoveProblemCollector<Void, Void>(cc, movingClasses, pkgRenames);
                         mpc.scan(cc.getCompilationUnit(), null);
                         if (mpc.getProblem() != null) {
                             p[0] = chainProblems(p[0], mpc.getProblem());
                         }
                     }
                 }, true);
             } catch (IOException e) {
             }
             fireProgressListenerStep();
         }
         fireProgressListenerStop();
 
         return p[0];
     }
 
     public Problem fastCheckParameters() {
         try {
             for (FileObject f: refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
                 if (!SourceUtils.isJavaFXFile(f))
                     continue;
                 String targetPackageName = getNewPackageName();
                 if (!SourceUtils.isValidPackageName(targetPackageName)) {
                     String s = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage"); //NOI18N
                     String msg = new MessageFormat(s).format(
                             new Object[] {targetPackageName}
                     );
                     return new Problem(true, msg);
                 }
                 FileObject targetRoot = SourceUtils.getClassPathRoot(((MoveRefactoring)refactoring).getTarget().lookup(URL.class));
                 FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));
 
                 String pkgName = null;
                 if ((targetF!=null && !targetF.canWrite())) {
                     return new Problem(true, new MessageFormat(NbBundle.getMessage(MoveRefactoringPlugin.class,"ERR_PackageIsReadOnly")).format( // NOI18N
                             new Object[] {targetPackageName}
                     ));
                 }
 
                 pkgName = targetPackageName;
 
                 if (pkgName == null) {
                     pkgName = ""; // NOI18N
                 } else if (pkgName.length() > 0) {
                     pkgName = pkgName + '.'; // NOI18N
                 }
                 String fileName = f.getName();
                 if (targetF!=null) {
                     FileObject[] children = targetF.getChildren();
                     for (int x = 0; x < children.length; x++) {
                         if (children[x].getName().equals(fileName) && "java".equals(children[x].getExt()) && !children[x].equals(f) && !children[x].isVirtual()) { //NOI18N
                             return new Problem(true, new MessageFormat(
                                     NbBundle.getMessage(MoveRefactoringPlugin.class,"ERR_ClassToMoveClashes")).format(new Object[] {fileName} // NOI18N
                             ));
                         }
                     } // for
                 }
             }
         } catch (IOException ioe) {
             //do nothing
         }
         return null;
     }
 
     public Problem preCheck() {
         Problem preCheckProblem = null;
         for (FileObject file:refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
             if (!SourceUtils.isElementInOpenProject(file)) {
                 preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                         MoveRefactoringPlugin.class,
                         "ERR_ProjectNotOpened", // NOI18N
                         FileUtil.getFileDisplayName(file)));
             }
         }
         return preCheckProblem;
     }
 
     public Problem prepare(RefactoringElementsBag reb) {
         collectMovingData(RefactoringSupport.classIndex(refactoring));
 
         if (isCancelled()) return null;
         fireProgressListenerStart(MoveRefactoring.PREPARE, 2 + movingDefs.size() + relatedPerPkg.size() + related.size());
         
         fireProgressListenerStep();
 
         final Set<ElementDef> movingElDefs = new HashSet<ElementDef>();
         for(Set<ElementDef> set : movingDefs.values()) {
             movingElDefs.addAll(set);
         }
         fireProgressListenerStep();
 
         Set<BaseRefactoringElementImplementation> refelems = new HashSet<BaseRefactoringElementImplementation>();
 
         final String newPkgName = getNewPackageName();
         for(final Map.Entry<FileObject, Set<ElementDef>> entry : movingDefs.entrySet()) {
             if (isCancelled()) return null;
 
             final FileObject file = entry.getKey();
             final String oldPkgName = entry.getValue().iterator().next().getPackageName();
 
             fireProgressListenerStep();
 
             if (SourceUtils.isJavaFXFile(file)) {
                 final ClassModel cm =RefactoringSupport.classModelFactory(refactoring).classModelFor(file);
 
                 BaseRefactoringElementImplementation ref = null;
                 if (!(newPkgName == null && oldPkgName == null) && !oldPkgName.equals(newPkgName)) {
                     ref = new UpdatePackageDeclarationElement(cm.getPackageDef().getName(), newPkgName, file, reb.getSession()) {
                         @Override
                         protected Set<Transformation> prepareTransformations(FileObject fo) {
                             Transformation t;
                             if (isOldDefault()) {
                                 t = new RemoveTextTransformation(cm.getPackageDef().getStartPos(), cm.getPackageDef().getEndPos() - cm.getPackageDef().getStartPos());
                             } else if (isNewDefault()) {
                                 t = new InsertTextTransformation(cm.getPackagePos(), "package " + newPkgName + ";\n"); // NOI18N
                             } else {
                                 t = new ReplaceTextTransformation(cm.getPackageDef().getStartFQN(), getOldPkgName(), getNewPkgName());
                             }
                             return Collections.singleton(t);
                         }
                     };
                 }
 
                 if (ref != null) {
                     refelems.add(ref);
                 }
                 
                 FixImportsElement fixImports = new FixImportsElement(file, reb.getSession()) {
 
                     @Override
                     protected Set<Transformation> prepareTransformations(FileObject fo) {
                         Set<Transformation> transformations = new HashSet<Transformation>();
                         ImportSet is = cm.getImportSet();
 
                         is.setPkgName(newPkgName);
                         fixImports(movingElDefs, is, cm, true, transformations);
                         return transformations;
                     }
                 };
                 refelems.add(fixImports);
             }
         }
 
         refelems.addAll(getRenameOccurencesInRelated(newPkgName, movingElDefs, reb.getSession()));
         refelems.addAll(getFixImportsInRelated(movingElDefs, reb.getSession()));
 
         if (isCancelled()) return null;
         for(BaseRefactoringElementImplementation brei : refelems) {
             if (isCancelled()) return null;
             if (brei.hasChanges()) {
                 reb.add(refactoring, brei);
             } else {
                 reb.addFileChange(refactoring, new ReindexFileElement(brei.getParentFile()));
             }
         }
         fireProgressListenerStop();
 
         return null;
     }
 
     private String getNewPackageName() {
         // XXX cache it !!!
         return SourceUtils.getPackageName(((MoveRefactoring) refactoring).getTarget().lookup(URL.class));
     }
 
     private void fixImports(Set<ElementDef> movingDefs, ImportSet is, ClassModel cm, boolean isMoving, Set<Transformation> transformations) {
         int lastRemovePos = -1;
         for(ImportEntry ie : is.getUnused()) {
             transformations.add(new RemoveTextTransformation(ie.getStartPos(), ie.getEndPos() - ie.getStartPos()));
             if (ie.getStartPos() > lastRemovePos) {
                 lastRemovePos = ie.getStartPos();
             }
         }
         
         int insertionPos = lastRemovePos > cm.getImportPos() ? lastRemovePos : cm.getImportPos();
         
         for(ImportSet.Touple<ElementDef, ImportEntry> missing : is.getMissing()) {
             if (isMoving ^ movingDefs.contains(missing.getT1())) {
                 transformations.add(new InsertTextTransformation(insertionPos, missing.getT2().toString() + ";\n")); // NOI18N
             }
         }
     }
 
     synchronized private void collectMovingData(ClassIndex ci) {
         if (movingDefs != null) return; // already initialized
         
         Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
         fireProgressListenerStart(MoveRefactoring.INIT, files.size());
 
         movingDefs = new HashMap<FileObject, Set<ElementDef>>();
         related = new HashSet<FileObject>();
         relatedPerPkg = new HashMap<String, Set<FileObject>>();
         renames = new HashMap<String, String>();
         pkgRenames = new HashMap<String, String>();
 
         for(FileObject file : files) {
             if (isCancelled()) return;
 
             final Set<ElementDef> edefs = new HashSet<ElementDef>();
             if (SourceUtils.isJavaFXFile(file)) {
                 ClassModel cm = RefactoringSupport.classModelFactory(refactoring).classModelFor(file);
                 edefs.addAll(cm.getElementDefs(EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM)));
             } else {
                 Object[] hdls = refactoring.getRefactoringSource().lookup(new Object[0].getClass());
                 if (hdls != null) {
                     for(final Object hdl : hdls) {
                         if (hdl instanceof TreePathHandle) {
                             edefs.add(RefactoringSupport.fromJava((TreePathHandle)hdl));
                         }
                     }
                 } else {
                     JavaSource js = JavaSource.forFileObject(file);
                     try {
                         js.runUserActionTask(new org.netbeans.api.java.source.Task<org.netbeans.api.java.source.CompilationController>() {
 
                             public void run(final org.netbeans.api.java.source.CompilationController cc) throws Exception {
                                 if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED) == JavaSource.Phase.ELEMENTS_RESOLVED) {
                                     Object elements = cc.getClass().getMethod("getTopLevelElements").invoke(cc); // NOI18N
                                     Iterator iter = (Iterator)List.class.getMethod("iterator").invoke(elements); // NOI18N
 
                                     while (iter.hasNext()) {
                                         Object te = iter.next();
                                         ElementDef edef = RefactoringSupport.fromJava(te, cc);
                                         edefs.add(edef);
                                     }
                                 }
                             }
                         }, true);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
             String newPkgName = getNewPackageName();
             for(ElementDef edef : edefs) {
                 if (isCancelled()) return;
                 
                 org.netbeans.api.javafx.source.ElementHandle eh = edef.createHandle();
                 Set<FileObject> fileRelated = ci.getResources(eh, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class));
                 String fqn = eh.getQualifiedName();
                 renames.put(fqn, fqn.replace(edef.getPackageName(), newPkgName));
                 pkgRenames.put(edef.getPackageName(), newPkgName);
                 Set<FileObject> rppkg = relatedPerPkg.get(edef.getPackageName());
                 if (rppkg == null) {
                     rppkg = new HashSet<FileObject>();
                     relatedPerPkg.put(edef.getPackageName(), rppkg);
                 }
                 rppkg.addAll(fileRelated);
                 related.addAll(fileRelated);
             }
             movingDefs.put(file, edefs);
 
             fireProgressListenerStep();
         }
         related.removeAll(files); // don't process imports in dependencies which are also moved
         fireProgressListenerStop();
     }
 
     private Collection<BaseRefactoringElementImplementation> getRenameOccurencesInRelated(final String newName, final Set<ElementDef> movingElDefs, RefactoringSession session) {
         Collection<BaseRefactoringElementImplementation> refelems = new HashSet<BaseRefactoringElementImplementation>();
         for(final Map.Entry<String, Set<FileObject>> entry : relatedPerPkg.entrySet()) {
             fireProgressListenerStep();
             for(FileObject refFo : entry.getValue()) {
                 if (isCancelled()) return null;
                 if (!SourceUtils.isJavaFXFile(refFo)) continue;
                 RenameOccurencesElement updateRefs = new RenameOccurencesElement(entry.getKey(), newName, refFo, session) {
 
                     @Override
                     protected Set<Transformation> prepareTransformations(FileObject fo) {
                         Set<Transformation> transformations = new HashSet<Transformation>();
                         ClassModel refCm = RefactoringSupport.classModelFactory(refactoring).classModelFor(fo);
                         for(Usage usg : refCm.getUsages(new PackageDef(entry.getKey()))) {
                             if (usg.getStartPos() == refCm.getPackageDef().getStartFQN()) continue; // don't process the package name
                             // a small hack
                             ElementDef typeDef = refCm.getDefForPos(usg.getEndPos() + 2); // move 1 character past the "." delimiter
                             if (typeDef != null && movingElDefs.contains(typeDef)) {
                                 transformations.add(new ReplaceTextTransformation(usg.getStartPos(), getOldName(), getNewName()));
                             }
                         }
                         return transformations;
                     }
                 };
                 refelems.add(updateRefs);
             }
         }
         return refelems;
     }
 
     private Collection<BaseRefactoringElementImplementation> getFixImportsInRelated(final Set<ElementDef> movingElDefs, RefactoringSession session) {
         Collection<BaseRefactoringElementImplementation> refelems = new HashSet<BaseRefactoringElementImplementation>();
         for(FileObject refFo : related) {
             if (isCancelled()) return null;
            if (!SourceUtils.isJavaFXFile(refFo)) continue;
            
             fireProgressListenerStep();
             FixImportsElement fixImports = new FixImportsElement(refFo, session) {
 
                 @Override
                 protected Set<Transformation> prepareTransformations(FileObject fo) {
                     Set<Transformation> transformations = new HashSet<Transformation>();
                     ClassModel refCm = RefactoringSupport.classModelFactory(refactoring).classModelFor(fo);
                     ImportSet is = refCm.getImportSet();
                     for(Map.Entry<String, String> entry : renames.entrySet()) {
                         is.addRename(entry.getKey(), entry.getValue());
                     }
                     fixImports(movingElDefs, is, refCm, false, transformations);
                     return transformations;
                 }
             };
             refelems.add(fixImports);
         }
         return refelems;
     }
 }
