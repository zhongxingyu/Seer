 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
  * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
  */
 package org.netbeans.modules.javafx.editor.task;
 
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.UnitTree;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.netbeans.modules.javafx.editor.JavaFXDocument;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.DialogDisplayer;
 import org.openide.ErrorManager;
 import org.openide.NotifyDescriptor;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataFolder;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 import org.openide.util.Exceptions;
 
 import javax.swing.text.Document;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
  * @todo documentation
  */
 class WrongPackageStatmentTask implements CancellableTask<CompilationInfo> {
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/netbeans/modules/javafx/editor/task/Bundle"); // NOI18N
     private static final String WRONG_PROJECT = "wrong-project"; // NOI18N
     private static final String WRONG_PACKAGE = "wrong-package"; // NOI18N
     private static final String EDITOR_WRONG_PROJECT = "Editor.wrongProject"; // NOI18N
     private static final String EDITOR_WRONG_PACKAGE_PACKAGE = "Editor.wrongPackage.package"; // NOI18N
 
     private final FileObject file;
     private final AtomicBoolean canceled = new AtomicBoolean(false);
 
 
     WrongPackageStatmentTask(FileObject file) {
         this.file = file;
     }
 
     public void cancel() {
         canceled.set(true);
     }
 
     public void run(CompilationInfo ci) throws Exception {
         if (!file.isValid()) return;
         ExpressionTree packageTree = ci.getCompilationUnit().getPackageName();
         String packageName = packageTree == null ? null : packageTree.toString();
         Project project = FileOwnerQuery.getOwner(file);
         if (project == null) return;
         int sp = packageTree == null ? 0 : (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), packageTree);
         int ep = packageTree == null ? 0 : (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), packageTree);
         if (project instanceof JavaFXProject) {
             JavaFXProject fxp = (JavaFXProject) project;
             FileObject[] sourceRoots = fxp.getFOSourceRoots();
             List<Fix> fixes = new ArrayList<Fix>(2);
             for (FileObject sourceRoot : sourceRoots) {
                 if (FileUtil.isParentOf(sourceRoot, file)) {
                     String path = sourceRoot.getPath();
                     String me = file.getParent().getPath();
                     if (me.startsWith(path)) {
                         boolean inDefaultPackage = me.equals(path); // the paths are same. The file is in <default package>
                         if (!inDefaultPackage) {
                             me = me.substring(path.length() + 1); //stripping first slash
                             me = me.replace("/", ".");        // NOI18N
                         }
                         if (me.equals(packageName) || (inDefaultPackage && packageName == null)) {
                             return;
                         } else {
                             if (!inDefaultPackage) {
                                 fixes.add(new ReplacePackageNameFix(ci, me, getDoc(file)));
                             } else {
                                 fixes.add(new SetPackageToDefaultFix(ci, getDoc(file)));
                             }
                             fixes.add(new MoveToFolderFix(file, packageName, sourceRoot));
                         }
                     }
                 }
             }
             HintsController.setErrors(file, WRONG_PACKAGE, Collections.<ErrorDescription>singletonList(
                     ErrorDescriptionFactory.createErrorDescription(
                             Severity.ERROR,
                             MessageFormat.format(BUNDLE.getString(EDITOR_WRONG_PACKAGE_PACKAGE), packageName),
                             fixes, file, sp, ep)
             ));
         } else {
             // solving compilation error under J5
             //noinspection RedundantArrayCreation
             HintsController.setErrors(file, WRONG_PROJECT, Collections.<ErrorDescription>singletonList(
                     ErrorDescriptionFactory.createErrorDescription(
                             Severity.ERROR,
                             MessageFormat.format(BUNDLE.getString(EDITOR_WRONG_PROJECT), new Object[0]),
                             file, sp, ep)
             ));
         }
     }
 
     private JavaFXDocument getDoc(FileObject file) {
         if (file == null || !file.isValid()) return null;
         DataObject od = null;
         try {
             od = DataObject.find(file);
         } catch (DataObjectNotFoundException ex) {
             Exceptions.printStackTrace(ex);
         }
         EditorCookie ec = od != null ? od.getLookup().lookup(EditorCookie.class) : null;
         if (ec == null) {
             return null;
         }
         Document doc = ec.getDocument();
         if (doc instanceof JavaFXDocument) {
             return (JavaFXDocument) doc;
         }
         return null;
     }
 
 
     private static class ReplacePackageNameFix implements Fix {
         private CompilationInfo ci;
         private String newName;
         private JavaFXDocument doc;
 
         public ReplacePackageNameFix(CompilationInfo ci, String newName, JavaFXDocument doc) {
             this.ci = ci;
             this.newName = newName;
             this.doc = doc;
         }
 
         public String getText() {
             return MessageFormat.format(BUNDLE.getString("Editor.FixPackage"), newName); // NOI18N
         }
 
         public ChangeInfo implement() throws Exception {
             if (doc == null) return new ChangeInfo();
             UnitTree cu = ci.getCompilationUnit();
             ExpressionTree pn = cu.getPackageName();
             SourcePositions sp = ci.getTrees().getSourcePositions();
             int start = (int) sp.getStartPosition(cu, pn);
             int end = (int) sp.getEndPosition(cu, pn);
             if (start < end) {
                 doc.replace(start, end - start, newName, null);
                 return new ChangeInfo(doc.createPosition(start), doc.createPosition(start + newName.length()));
             } else {
                 doc.insertString(0, "package " + newName + ";\n", null);
                 return new ChangeInfo(doc.createPosition(0), doc.createPosition(start + newName.length() + 10));
             }
 
         }
     }
 
     private static class SetPackageToDefaultFix implements Fix {
         private final CompilationInfo ci;
         private final JavaFXDocument doc;
 
         private SetPackageToDefaultFix(CompilationInfo ci, JavaFXDocument doc) {
             this.ci = ci;
             this.doc = doc;
         }
 
         public String getText() {
             return MessageFormat.format(BUNDLE.getString("Editor.FixPackage"), "<default package>"); // NOI18N
         }
 
         public ChangeInfo implement() throws Exception {
             if (doc == null) return new ChangeInfo();
             UnitTree cu = ci.getCompilationUnit();
             ExpressionTree pn = cu.getPackageName();
             SourcePositions sp = ci.getTrees().getSourcePositions();
             int start = (int) sp.getStartPosition(cu, pn);
             int end = (int) sp.getEndPosition(cu, pn) + 1;
             //noinspection unchecked
             TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) TokenHierarchy.get(doc).tokenSequence();
             ts.move(start);
             boolean finish = false;
             while (ts.movePrevious() || !finish) {
                 JFXTokenId id = ts.token().id();
                 switch (id) {
                     case PACKAGE: {
                         start = ts.offset();
                         finish = true;
                         break;
                     }                    
                 }
             }
 
             doc.remove(start, end - start);
             return new ChangeInfo();
         }
     }
 
     private static class MoveToFolderFix implements Fix {
         private final FileObject file;
         private final String packageName;
         private final FileObject root;
 
         private MoveToFolderFix(FileObject file, String packageName, FileObject root) {
             this.file = file;
             this.packageName = packageName;
             this.root = root;
         }
 
         public String getText() {
             return BUNDLE.getString("Editor.moveToFolder"); // NOI18N
         }
 
         public ChangeInfo implement() throws Exception {
             try {
                 String path = packageName == null? "" : packageName.replace('.', '/');
 
                 FileObject packFile = root.getFileObject(path);
 
                 if (packFile != null && !packFile.isFolder()) {
                     NotifyDescriptor nd = new NotifyDescriptor.Message(BUNDLE.getString("ERR_CannotMoveAlreadyExists"), NotifyDescriptor.Message.ERROR_MESSAGE); // NOI18N
                     DialogDisplayer.getDefault().notifyLater(nd);
                     return null;
                 }
 
                 if (packageName != null ) {
                     packFile = FileUtil.createFolder(root, packageName.replace('.', '/'));   // NOI18N
                 } else {
                     packFile = root;
                 }
 
                 DataObject fileDO = DataObject.find(file);
                 DataFolder folder = DataFolder.findFolder(packFile);
 
                 fileDO.move(folder);
             } catch (IllegalArgumentException e) {
                 Exceptions.attachLocalizedMessage(e, BUNDLE.getString("ERR_CannotMove")); // NOI18N
                 ErrorManager.getDefault().notify(ErrorManager.USER, e);
             } catch (IOException e) {
                 Exceptions.attachLocalizedMessage(e, BUNDLE.getString("ERR_CannotMove")); // NOI18N
                 ErrorManager.getDefault().notify(ErrorManager.USER, e);
             }
 
             return null;
         }
     }
 }
 
