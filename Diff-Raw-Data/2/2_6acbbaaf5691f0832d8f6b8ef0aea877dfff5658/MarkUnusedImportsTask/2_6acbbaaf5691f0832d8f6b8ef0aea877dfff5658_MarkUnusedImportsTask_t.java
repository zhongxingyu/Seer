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
 package org.netbeans.modules.javafx.editor.imports;
 
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.modules.javafx.editor.JavaFXDocument;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 import org.openide.util.Exceptions;
 
 import javax.swing.text.Document;
 import javax.swing.text.Position;
 import java.lang.ref.WeakReference;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 
 /**
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
  * @author Jaroslav Bachorik
  * @todo documentation
  */
 class MarkUnusedImportsTask implements CancellableTask<CompilationInfo> {
     private final WeakReference<FileObject> file;
     private final AtomicBoolean canceled = new AtomicBoolean(false);
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/netbeans/modules/javafx/editor/imports/Bundle"); // NOI18N
 
     MarkUnusedImportsTask(FileObject file) {
         assert file != null : "File cannot be null!"; // NOI18N
         this.file = new WeakReference<FileObject>(file);
     }
 
     public void cancel() {
         canceled.set(true);
     }
 
     public void run(CompilationInfo cp) throws Exception {
         try {
             if (canceled.get() || file.get() == null || !file.get().isValid()) return;
             final JavaFXDocument document = getDoc(file.get());
             if (document == null || canceled.get()) return;
 
             ArrayList<ErrorDescription> warnings = new ArrayList<ErrorDescription>();
             ImportsWalker iw = new ImportsWalker(cp);
             ImportsModel imn = new ImportsModel();
             iw.scan(cp.getCompilationUnit(), imn);
             for(ImportsModel.Declared unused : imn.getUnusedImports()) {
                 int start = (int) unused.getStart();
                int end = Math.min((int) unused.getEnd() + 2  /*SEMI + WS[NL]*/, (int) document.getEndPosition().getOffset());
                 Position sp = document.createPosition(start);
                 Position ep = document.createPosition(end);
                 warnings.add(ErrorDescriptionFactory.createErrorDescription(
                         Severity.WARNING,
                         MessageFormat.format(BUNDLE.getString("Editor.unusedImports.message2user"), unused.getImportName()),   // NOI18N
                         Collections.<Fix>singletonList(new RemoveImportFix(document, sp, ep)),
                         document, sp, ep));
             }
 
             if (canceled.get()) return;
             HintsController.setErrors(document, "unused-imports", warnings); // NOI18N
         } finally {
             canceled.set(false);
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
 
     private static class RemoveImportFix implements Fix {
         public static final String TEXT = BUNDLE.getString("Remove_unused_import"); // NOI18N
         private final Document doc;
         private final Position start;
         private final Position end;
 
         private RemoveImportFix(Document doc, Position start, Position end) {
             this.doc = doc;
             this.start = start;
             this.end = end;            
         }
 
         public String getText() {
             return TEXT;
         }
 
         public ChangeInfo implement() throws Exception {
             doc.remove(start.getOffset(), end.getOffset() - start.getOffset());
             return new ChangeInfo(start, end);
         }
     }
 }
