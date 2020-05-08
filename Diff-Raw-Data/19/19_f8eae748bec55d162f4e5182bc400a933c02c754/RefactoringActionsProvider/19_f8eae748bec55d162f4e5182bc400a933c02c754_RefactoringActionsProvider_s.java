 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
  * Portions Copyrighted 1997-2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl;
 
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.UnitTree;
 import com.sun.tools.javafx.tree.JFXTree;
 import com.sun.tools.javafx.tree.JavafxTreeInfo;
 import java.awt.datatransfer.Transferable;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.swing.Action;
 import javax.swing.JOptionPane;
 import javax.swing.text.JTextComponent;
 import org.netbeans.api.fileinfo.NonRecursiveFolder;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSourceUtils;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.SourceUtils;
 import org.netbeans.modules.javafx.refactoring.impl.ui.CopyClassUI;
 import org.netbeans.modules.javafx.refactoring.impl.ui.CopyClassesUI;
 import org.netbeans.modules.javafx.refactoring.impl.ui.MoveClassUI;
 import org.netbeans.modules.javafx.refactoring.impl.ui.MoveClassesUI;
 import org.netbeans.modules.javafx.refactoring.impl.ui.RenameRefactoringUI;
 import org.netbeans.modules.javafx.refactoring.impl.ui.SafeDeleteUI;
 import org.netbeans.modules.javafx.refactoring.impl.ui.WhereUsedQueryUI;
 import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
 import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
 import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
 import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
 import org.netbeans.modules.refactoring.spi.ui.UI;
 import org.openide.ErrorManager;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataFolder;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 import org.openide.nodes.Node;
 import org.openide.text.CloneableEditorSupport;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.datatransfer.PasteType;
 import org.openide.util.lookup.ServiceProvider;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 @ServiceProvider(service=ActionsImplementationProvider.class)
 public class RefactoringActionsProvider extends ActionsImplementationProvider {
     final private static Logger LOGGER = Logger.getLogger(RefactoringActionsProvider.class.getName());
 
     @Override
     public boolean canFindUsages(Lookup lkp) {
         FileObject file = lkp.lookup(FileObject.class);
         return file != null && file.getMIMEType().equals("text/x-fx");
     }
 
     volatile private boolean isFindUsages;
 
     @Override
     public void doFindUsages(Lookup lkp) {
         if (isFindUsages) return;
         
         Runnable task;
         EditorCookie ec = lkp.lookup(EditorCookie.class);
         if (isFromEditor(ec)) {
             task = new TextComponentTask(ec) {
                 @Override
                 protected RefactoringUI createRefactoringUI(ElementLocation location,int startOffset,int endOffset, CompilationInfo info) {
                     if (location == null) return null;
                     return new WhereUsedQueryUI(location, info);
                 }
             };
         } else {
             task = new NodeToElementTask(lkp.lookupAll(Node.class)) {
                 protected RefactoringUI createRefactoringUI(ElementLocation location, CompilationInfo info) {
                     if (location == null) return null;
                     return new WhereUsedQueryUI(location, info);
                 }
             };
         }
         try {
             isFindUsages = true;
             task.run();
         } finally {
             isFindUsages = false;
         }
     }
 
     @Override
     public boolean canRename(Lookup lkp) {
 //        if (!isRefactoringEnabled()) return false;
         Node target = lkp.lookup(Node.class);
 
         DataObject dobj = (target != null ? target.getCookie(DataObject.class) : null);
         return dobj != null && SourceUtils.isJavaFXFile(dobj.getPrimaryFile());
     }
 
     @Override
     public void doRename(final Lookup lookup) {
         Runnable task;
         EditorCookie ec = lookup.lookup(EditorCookie.class);
         if (isFromEditor(ec)) {
             task = new TextComponentTask(ec) {
                 @Override
                 protected RefactoringUI createRefactoringUI(ElementLocation location,int startOffset,int endOffset, final CompilationInfo info) {
                     if (location==null) {
                         LOGGER.log(Level.INFO, "doRename: " + location, new NullPointerException("selected")); // NOI18N
                         return null;
                     }
                     if (location.getElement(info).getKind() == ElementKind.PACKAGE) {
                         NonRecursiveFolder folder = new NonRecursiveFolder() {
                             public FileObject getFolder() {
                                 return info.getFileObject().getParent();
                             }
                         };
                         return new RenameRefactoringUI(folder);
                     } else {
                         return new RenameRefactoringUI(location, info);
                     }
                 }
             };
         } else if (nodeHandle(lookup)) {
             task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {
 
                 RenameRefactoringUI ui;
 
                 @Override
                 protected void locationResolved(ElementLocation location, CompilationInfo javac) {
                     if (renameFile) {
                         ui = new RenameRefactoringUI(location.getSourceFile(), location, javac);
                     } else {
                         ui = new RenameRefactoringUI(location, javac);
                     }
                 }
 
                 @Override
                 protected RefactoringUI createRefactoringUI(Collection<ElementLocation> locations) {
                     return ui;
                 }
 
             };
 
         } else {
 //            task = new NodeToFileObjectTask(new HashSet(lookup.lookupAll(Node.class))) {
             // canRename is valid only for single node
             task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {
 
                 RenameRefactoringUI ui;
 
                 @Override
                 protected void nodeTranslated(Node node, Collection<ElementLocation> locations, CompilationInfo javac) {
                     String newName = getName(lookup);
                     ui = newName != null
                             ? new RenameRefactoringUI(javac.getFileObject(), newName, locations==null||locations.isEmpty()?null:locations.iterator().next(), javac)
                             : new RenameRefactoringUI(locations==null||locations.isEmpty()?null:locations.iterator().next(), javac);
                 }
 
                 @Override
                 protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<ElementLocation> locations) {
                     if (ui == null) {
                         String newName = getName(lookup);
                         if (newName != null) {
                             ui = pkg[0] != null
                                     ? new RenameRefactoringUI(pkg[0], newName)
                                     : new RenameRefactoringUI(selectedElements[0], newName, null, null);
                         } else {
                             ui = pkg[0]!= null
                                     ? new RenameRefactoringUI(pkg[0])
                                     : null; //new RenameRefactoringUI(selectedElements[0], null, null);
                         }
                     }
                     return ui;
                 }
             };
         }
         SourceUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
     }
 
     @Override
     public boolean canMove(Lookup lkp) {
 //        if (!isRefactoringEnabled()) return false;
         Collection<? extends Node> nodes = new HashSet<Node>(lkp.lookupAll(Node.class));
         ExplorerContext drop = lkp.lookup(ExplorerContext.class);
         FileObject fo = getTarget(lkp);
         if (fo != null) {
             if (!fo.isFolder())
                 return false;
             if (!SourceUtils.isOnSourceClasspath(fo))
                 return false;
 
             //it is drag and drop
             Set<DataFolder> folders = new HashSet<DataFolder>();
             boolean jdoFound = false;
             for (Node n:nodes) {
                 DataObject dob = n.getCookie(DataObject.class);
                 if (dob==null) {
                     return false;
                 }
                 if (!SourceUtils.isOnSourceClasspath(dob.getPrimaryFile())) {
                     return false;
                 }
                 if (dob instanceof DataFolder) {
                     if (FileUtil.getRelativePath(dob.getPrimaryFile(), fo)!=null)
                         return false;
                     folders.add((DataFolder)dob);
                 } else if (SourceUtils.isJavaFXFile(dob.getPrimaryFile())) {
                     jdoFound = true;
                 }
             }
             if (jdoFound)
                 return true;
             for (DataFolder fold:folders) {
                 for (Enumeration<DataObject> e = (fold).children(true); e.hasMoreElements();) {
                     if (SourceUtils.isJavaFXFile(e.nextElement().getPrimaryFile())) {
                         return true;
                     }
                 }
             }
             return false;
         } else {
             //regular invocation
             boolean result = false;
             for (Node n:nodes) {
                 final DataObject dob = n.getCookie(DataObject.class);
                 if (dob==null) {
                     return false;
                 }
                 if (dob instanceof DataFolder) {
                     return drop!=null;
                 }
                 if (!SourceUtils.isOnSourceClasspath(dob.getPrimaryFile())) {
                     return false;
                 }
                 if (SourceUtils.isJavaFXFile(dob.getPrimaryFile())) {
                     result = true;
                 }
             }
             return result;
         }
     }
 
     @Override
     public void doMove(final Lookup lkp) {
         Runnable task;
         EditorCookie ec = lkp.lookup(EditorCookie.class);
         if (isFromEditor(ec)) {
             task = new TextComponentTask(ec) {
                 @Override
                 protected RefactoringUI createRefactoringUI(ElementLocation location,int startOffset,int endOffset, CompilationInfo info) {
                     Element e = location.getElement(info);
                     if (e == null) {
                         LOGGER.log(Level.INFO, "doMove: " + location, new NullPointerException("e")); // NOI18N
                         return null;
                     }
                     if ((e.getKind().isClass() || e.getKind().isInterface()) &&
                             JavaFXSourceUtils.getOutermostEnclosingTypeElement(e)==e) {
                         try {
                             FileObject fo = SourceUtils.getFile(e, info.getClasspathInfo());
                             if (fo!=null) {
                                 DataObject d = DataObject.find(SourceUtils.getFile(e, info.getClasspathInfo()));
                                 if (d.getName().equals(e.getSimpleName().toString())) {
                                     return null ; //new MoveClassUI(d);
                                 }
                             }
                         } catch (DataObjectNotFoundException ex) {
                             throw (RuntimeException) new RuntimeException().initCause(ex);
                         }
                     }
                    if (info.getTrees().getPath(e).getLeaf().getJavaFXKind() == Tree.JavaFXKind.COMPILATION_UNIT) {
                         try {
                             return new MoveClassUI(DataObject.find(info.getFileObject()));
                         } catch (DataObjectNotFoundException ex) {
                             throw (RuntimeException) new RuntimeException().initCause(ex);
                         }
                     } else {
                         try {
                             return new MoveClassUI(DataObject.find(info.getFileObject()));
                         } catch (DataObjectNotFoundException ex) {
                             throw (RuntimeException) new RuntimeException().initCause(ex);
                         }
                     }
                 }
             };
         } else {
             task = new NodeToFileObjectTask(new HashSet<Node>(lkp.lookupAll(Node.class))) {
                 @Override
                 protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<ElementLocation> location) {
                     PasteType paste = getPaste(lkp);
                     FileObject tar=getTarget(lkp);
                     if (selectedElements.length == 1) {
                         if (!selectedElements[0].isFolder()) {
                             try {
                                 return new MoveClassUI(DataObject.find(selectedElements[0]), tar, paste, location);
                             } catch (DataObjectNotFoundException ex) {
                                 throw (RuntimeException) new RuntimeException().initCause(ex);
                             }
                         } else {
                             Set<FileObject> s = new HashSet<FileObject>();
                             s.addAll(Arrays.asList(selectedElements));
                             return new MoveClassesUI(s, tar, paste);
                         }
                     } else {
                         Set<FileObject> s = new HashSet<FileObject>();
                         s.addAll(Arrays.asList(selectedElements));
                         return new MoveClassesUI(s, tar, paste);
                     }
                 }
 
             };
         }
         SourceUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
     }
 
     @Override
     public boolean canCopy(Lookup lkp) {
         Collection<? extends Node> nodes = new HashSet<Node>(lkp.lookupAll(Node.class));
         if (nodes.size() < 1) {
             return false;
         }
         for(Iterator<? extends Node> iter = nodes.iterator();iter.hasNext();) {
             Node n = iter.next();
             DataObject dob = n.getCookie(DataObject.class);
             if (dob==null) {
                 return false;
             }
 
             ExplorerContext dict = lkp.lookup(ExplorerContext.class);
             FileObject fob = getTarget(lkp);
             if (dict!=null && dict.getTargetNode() != null && fob==null) { //NOI18N
                 //unknown target
                 return false;
             }
             if (fob != null) {
                 if (!fob.isFolder() || !SourceUtils.isOnSourceClasspath(fob))
                     return false;
                 FileObject fo = dob.getPrimaryFile();
                 if (!SourceUtils.isRefactorable(fo)) { //NOI18N
                     return false;
                 }
 
             } else {
                 FileObject fo = dob.getPrimaryFile();
                 if (!SourceUtils.isRefactorable(fo)) { //NOI18N
                     return false;
                 }
             }
         }
         return true;
     }
 
     @Override
     public void doCopy(final Lookup lkp) {
        Runnable task = new NodeToFileObjectTask(new HashSet<Node>(lkp.lookupAll(Node.class))) {
                 @Override
                 protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<ElementLocation> handle) {
                     if (selectedElements.length == 1) {
                         return new CopyClassUI(selectedElements[0], getTarget(lkp), getPaste(lkp));
                     } else {
                         return new CopyClassesUI(new HashSet<FileObject>(Arrays.asList(selectedElements)), getTarget(lkp), getPaste(lkp));
                     }
                 }
             };
 //        }
         SourceUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.copyAction()));
     }
 
     @Override
     public boolean canDelete(Lookup lookup) {
         if (SourceUtils.isScanInProgress()) {
             return false;
         }
         Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
         //We live with a 2 pass validation of the selected nodes for now since
         //the code will become unreadable if we attempt to implement all checks
         //in one pass.
         if (isSelectionHeterogeneous(nodes)) {
             return false;
         }
         for (Node n:nodes) {
             ElementLocation tph = n.getLookup().lookup(ElementLocation.class);
             if (tph != null) {
                 return SourceUtils.isRefactorable(tph.getSourceFile());
             }
             DataObject dataObject = n.getCookie(DataObject.class);
             if (dataObject == null){
                 return false;
             }
             FileObject fileObject = dataObject.getPrimaryFile();
             if (isRefactorableFolder(dataObject)){
                 return true;
             }
             if (!SourceUtils.isRefactorable(fileObject)) {
                 return false;
             }
         }
         return !nodes.isEmpty();
     }
 
     @Override
     public void doDelete(final Lookup lookup) {
         Runnable task;
         EditorCookie ec = lookup.lookup(EditorCookie.class);
         final boolean b = lookup.lookup(ExplorerContext.class)!=null;
         if (isFromEditor(ec)) {
             task = new TextComponentTask(ec) {
                 @Override
                 protected RefactoringUI createRefactoringUI(ElementLocation selectedLocation,int startOffset,int endOffset, CompilationInfo info) {
                     Element selected = selectedLocation.getElement(info);
                     if (selected == null) {
                         LOGGER.log(Level.INFO, "doDelete: " + selectedLocation, new NullPointerException("selected")); // NOI18N
                         return null;
                     }
                     if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
                         FileObject file = SourceUtils.getFile(selected, info.getClasspathInfo());
                         if (file==null) {
                             return null;
                         }
                         if (file.getName().equals(selected.getSimpleName().toString())) {
                             return new SafeDeleteUI(new FileObject[]{file}, Collections.singleton(selectedLocation), b);
                         }
                     }
                         return new SafeDeleteUI(new ElementLocation[]{selectedLocation});
                     }
             };
         } else if (nodeHandle(lookup)) {
             task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {
 
                 @Override
                 protected RefactoringUI createRefactoringUI(Collection<ElementLocation> handles) {
                     if (renameFile) {
                         FileObject[] files = new FileObject[handles.size()];
                         int i=0;
                         for (ElementLocation handle:handles) {
                             files[i++] = handle.getSourceFile();
                         }
                         return new SafeDeleteUI(files, handles, b);
                     } else {
                         return new SafeDeleteUI(handles.toArray(new ElementLocation[handles.size()]));
                     }
                 }
 
             };
         } else {
             task = new NodeToFileObjectTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {
                 @Override
                 protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<ElementLocation> handles) {
                     if (pkg[0]!= null) {
                         return new SafeDeleteUI(pkg[0],b);
                     } else{
                         return new SafeDeleteUI(selectedElements, handles, b);
                     }
                 }
 
             };
         }
         SourceUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.safeDeleteAction()));
     }
 
     private static boolean isSelectionHeterogeneous(Collection<? extends Node> nodes){
         boolean folderSelected = false;
         boolean nonFolderNodeSelected = false;
         for (Node node : nodes) {
             DataObject dataObject = node.getCookie(DataObject.class);
             if (dataObject == null){
                 continue;
             }
             if (isRefactorableFolder(dataObject)){
                 if (folderSelected || nonFolderNodeSelected) {
                     return true;
                 }else{
                     folderSelected = true;
                 }
             }else{
                 nonFolderNodeSelected = true;
             }
         }
 
         return false;
     }
 
     static boolean isFromEditor(EditorCookie ec) {
         if (ec != null && ec.getOpenedPanes() != null) {
             TopComponent activetc = TopComponent.getRegistry().getActivated();
             if (activetc instanceof CloneableEditorSupport.Pane) {
                 return true;
             }
         }
         return false;
     }
 
     static boolean nodeHandle(Lookup lookup) {
         Node n = lookup.lookup(Node.class);
         if (n!=null) {
             if (n.getLookup().lookup(ElementLocation.class)!=null)
                 return true;
         }
         return false;
     }
 
     private static boolean isRefactorableFolder(DataObject dataObject) {
         FileObject fileObject = dataObject.getPrimaryFile();
         if (/* #159628 */!Boolean.TRUE.equals(fileObject.getAttribute("isRemoteAndSlow"))) { // NOI18N
             FileObject[] children = fileObject.getChildren();
             if (children == null || children.length <= 0) {
                 return false;
             }
         }
 
         return (dataObject instanceof DataFolder) &&
                 SourceUtils.isFileInOpenProject(fileObject) &&
                 SourceUtils.isOnSourceClasspath(fileObject) &&
                 !SourceUtils.isClasspathRoot(fileObject);
     }
 
     public static abstract class TreePathHandleTask implements Runnable, Task<CompilationController> {
         private Collection<ElementLocation> locations = new ArrayList<ElementLocation>();
         private ElementLocation current;
         boolean renameFile;
 
         public TreePathHandleTask(Collection<? extends Node> nodes) {
             this(nodes, false);
         }
 
         public TreePathHandleTask(Collection<? extends Node> nodes, boolean useFirstHandle) {
             for (Node n:nodes) {
                 ElementLocation temp = n.getLookup().lookup(ElementLocation.class);
                 if (temp!=null) {
                     locations.add(temp);
                     if (useFirstHandle) {
                         break;
                     }
                 }
             }
         }
 
         public void cancel() {
         }
 
         public void run(CompilationController info) throws Exception {
             Element el = info.getElementUtilities().elementFor(current.getStartPosition());
             if (el!=null && el instanceof TypeElement && !((TypeElement)el).getNestingKind().isNested()) {
                 if (info.getFileObject().getName().equals(el.getSimpleName().toString())) {
                     renameFile = true;
                 }
             }
             locationResolved(current, info);
         }
 
         public void run() {
             for (ElementLocation location:locations) {
                 FileObject f = location.getSourceFile();
                 current = location;
                 JavaFXSource source = JavaFXSource.forFileObject(f);
                 assert source != null;
                 try {
                     source.runUserActionTask(this, true);
                 } catch (IllegalArgumentException ex) {
                     ex.printStackTrace();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
 
             TopComponent activetc = TopComponent.getRegistry().getActivated();
 
             RefactoringUI ui = createRefactoringUI(locations);
             if (ui!=null) {
                 UI.openRefactoringUI(ui, activetc);
             } else {
                 JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
             }
         }
 
         /**
          * This is the place where subclasses may collect info about handles.
          * @param handle handle
          * @param javac context of running transaction
          */
         protected void locationResolved(ElementLocation location, CompilationInfo javac) {
         }
 
         protected abstract RefactoringUI createRefactoringUI(Collection<ElementLocation> locations);
     }
 
     public static abstract class TextComponentTask implements Runnable, Task<CompilationController> {
         private JTextComponent textC;
         private int caret;
         private int start;
         private int end;
         private RefactoringUI ui;
 
         public TextComponentTask(EditorCookie ec) {
             this.textC = ec.getOpenedPanes()[0];
             this.caret = textC.getCaretPosition();
             this.start = textC.getSelectionStart();
             this.end = textC.getSelectionEnd();
             assert caret != -1;
             assert start != -1;
             assert end != -1;
         }
 
         public void cancel() {
         }
 
         public void run(CompilationController cc) throws Exception {
             Element selectedElement = cc.getElementUtilities().elementFor(caret);
            ui = createRefactoringUI(new ElementLocation(selectedElement, caret, cc), start, end, cc);
//            JavaFXTreePath selectedElement = null;
//            selectedElement = cc.getTreeUtilities().pathFor(caret);
//            TreePathHandle searchHandle = null;
//            //workaround for issue 89064
//            if (selectedElement.getLeaf().getJavaFXKind() == Tree.JavaFXKind.COMPILATION_UNIT) {
//                List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
//                if (!decls.isEmpty()) {
//                    searchHandle = TreePathHandle.create(JavaFXTreePath.getPath(cc.getCompilationUnit(), decls.get(0)), cc);
//                }
//            }
//            if (searchHandle == null) {
//                searchHandle = TreePathHandle.create(caret, selectedElement, cc);
//            }
//            ui = createRefactoringUI(searchHandle, start, end, cc);
         }
 
         public final void run() {
             try {
                 JavaFXSource source = JavaFXSource.forDocument(textC.getDocument());
                 source.runUserActionTask(this, true);
             } catch (IOException ioe) {
                 ErrorManager.getDefault().notify(ioe);
                 return ;
             }
             TopComponent activetc = TopComponent.getRegistry().getActivated();
 
             if (ui!=null) {
                 UI.openRefactoringUI(ui, activetc);
             } else {
                 JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
             }
         }
 
         protected abstract RefactoringUI createRefactoringUI(ElementLocation selectedElement,int startOffset,int endOffset, CompilationInfo info);
     }
 
     public static abstract class NodeToElementTask implements Runnable, Task<CompilationController>  {
         private Node node;
         private RefactoringUI ui;
 
         public NodeToElementTask(Collection<? extends Node> nodes) {
             assert nodes.size() == 1;
             this.node = nodes.iterator().next();
         }
 
         public void cancel() {
         }
 
         public void run(CompilationController info) throws Exception {
             UnitTree unit = info.getCompilationUnit();
             if (unit.getTypeDecls().isEmpty()) {
                 ui = createRefactoringUI(null, info);
             } else {
                 Element e = JavafxTreeInfo.symbolFor((JFXTree)unit.getTypeDecls().get(0));
                 ui = createRefactoringUI(new ElementLocation(e, 0, info), info);
             }
         }
 
         public final void run() {
             DataObject o = node.getCookie(DataObject.class);
             JavaFXSource source = JavaFXSource.forFileObject(o.getPrimaryFile());
             assert source != null;
             try {
                 source.runUserActionTask(this, true);
             } catch (IllegalArgumentException ex) {
                 ex.printStackTrace();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
             if (ui!=null) {
                 UI.openRefactoringUI(ui);
             } else {
                 JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
             }
         }
         protected abstract RefactoringUI createRefactoringUI(ElementLocation location, CompilationInfo info);
     }
 
     public static abstract class NodeToFileObjectTask implements Runnable, Task<CompilationController> {
         private Collection<? extends Node> nodes;
         public NonRecursiveFolder pkg[];
         Collection<ElementLocation> locations = new ArrayList<ElementLocation>();
         private Node currentNode;
 
         public NodeToFileObjectTask(Collection<? extends Node> nodes) {
             this.nodes = nodes;
         }
 
         public void cancel() {
         }
 
         public void run(CompilationController info) throws Exception {
             Collection<ElementLocation> locationsPerNode = new ArrayList<ElementLocation>();
             UnitTree unit = info.getCompilationUnit();
             Collection<ElementLocation> publicLocations = new ArrayList<ElementLocation>();
             Collection<ElementLocation> sameNameLocations = new ArrayList<ElementLocation>();
             for (Tree t: unit.getTypeDecls()) {
                 Element e = info.getTrees().getElement(JavaFXTreePath.getPath(unit, t));
                 if (e == null || !(e.getKind().isClass() || e.getKind().isInterface())) {
                     // syntax errors #111195
                     continue;
                 }
                 ElementLocation l = new ElementLocation(e, (int)info.getTrees().getSourcePositions().getStartPosition(unit, t), info);
                 if (e.getSimpleName().toString().equals(info.getFileObject().getName())) {
                     sameNameLocations.add(l);
                 }
                 if (e.getModifiers().contains(Modifier.PUBLIC)) {
                     publicLocations.add(l);
                 }
             }
             if (!publicLocations.isEmpty()) {
                 locationsPerNode.addAll(publicLocations);
             } else {
                 locationsPerNode.addAll(sameNameLocations);
             }
 
             if (!locationsPerNode.isEmpty()) {
                 locations.addAll(locationsPerNode);
                 nodeTranslated(currentNode, locationsPerNode, info);
             }
         }
 
         public void run() {
             FileObject[] fobs = new FileObject[nodes.size()];
             pkg = new NonRecursiveFolder[fobs.length];
             int i = 0;
             for (Node node:nodes) {
                 DataObject dob = node.getCookie(DataObject.class);
                 if (dob!=null) {
                     fobs[i] = dob.getPrimaryFile();
                     if (SourceUtils.isJavaFXFile(fobs[i])) {
                         JavaFXSource source = JavaFXSource.forFileObject(fobs[i]);
                         assert source != null;
                         try {
                             currentNode = node;
                             // XXX this could be optimize by ClasspasthInfo in case of more than one file
                             source.runUserActionTask(this, true);
                         } catch (IllegalArgumentException ex) {
                             ex.printStackTrace();
                         } catch (IOException ex) {
                             ex.printStackTrace();
                         } finally {
                             currentNode = null;
                         }
                     }
 
                     pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                 }
             }
             RefactoringUI ui = createRefactoringUI(fobs, locations);
             if (ui!=null) {
                 UI.openRefactoringUI(ui);
             } else {
                 JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
             }
         }
 
         /**
          * Notifies subclasses about the translation.
          * This is the place where subclasses may collect info about handles.
          * @param node node that is translated
          * @param locations handles translated from the node
          * @param javac context of running translation
          */
         protected void nodeTranslated(Node node, Collection<ElementLocation> locations, CompilationInfo javac) {
         }
 
         protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<ElementLocation> locations);
     }
 
     static String getName(Lookup look) {
         ExplorerContext ren = look.lookup(ExplorerContext.class);
         if (ren==null)
             return null;
         return ren.getNewName(); //NOI18N
     }
 
     static String getActionName(Action action) {
         String arg = (String) action.getValue(Action.NAME);
         arg = arg.replace("&", ""); // NOI18N
         return arg.replace("...", ""); // NOI18N
     }
 
     private FileObject getTarget(Lookup look) {
         ExplorerContext drop = look.lookup(ExplorerContext.class);
         if (drop==null)
             return null;
         Node n = drop.getTargetNode();
         if (n==null)
             return null;
         DataObject dob = n.getCookie(DataObject.class);
         if (dob!=null)
             return dob.getPrimaryFile();
         return null;
     }
 
     private PasteType getPaste(Lookup look) {
         ExplorerContext drop = look.lookup(ExplorerContext.class);
         if (drop==null)
             return null;
         Transferable orig = drop.getTransferable();
         if (orig==null)
             return null;
         Node n = drop.getTargetNode();
         if (n==null)
             return null;
         PasteType[] pt = n.getPasteTypes(orig);
         if (pt.length==1) {
             return null;
         }
         return pt[1];
     }
 
     private static boolean isRefactoringEnabled() {
         return Boolean.getBoolean("javafx.refactoring");
     }
 }
