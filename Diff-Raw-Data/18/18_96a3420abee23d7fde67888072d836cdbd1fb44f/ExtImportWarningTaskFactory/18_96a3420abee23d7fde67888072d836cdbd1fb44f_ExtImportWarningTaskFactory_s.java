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
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
 package org.netbeans.modules.javafx.editor.hints;
 
 import com.sun.javafx.api.tree.ImportTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import java.util.*;
 import javax.swing.text.Position;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class ExtImportWarningTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private static final String HINTS_IDENT = "extimportjavafx"; //NOI18N
     private static final String IMPORT_NAME_EXT = "javafx.ext."; //NOI18N
 
     public ExtImportWarningTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         return new CancellableTask<CompilationInfo>() {
 
             @Override
             public void cancel() {
             }
 
             @Override
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 if (compilationInfo.getDocument() == null) {
                     return;
                 }
 
                 final SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                 final List<ErrorDescription> extWarnings = new ArrayList<ErrorDescription>();
                 new JavaFXTreePathScanner<Void, Void>() {
 
                     @Override
                     public Void visitImport(final ImportTree node, Void p) {
                        String fqn = node.getQualifiedIdentifier().toString();
                        if (fqn.contains(IMPORT_NAME_EXT)) {
                            final ImportPosition start = new ImportPosition((int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node));
                            final ImportPosition end = new ImportPosition((int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), node));
                            String message = NbBundle.getMessage(ExtImportWarningTaskFactory.class, "TIP_IMPORT_WARNING", fqn); //NOI18N
                            ErrorDescription er = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, message, compilationInfo.getDocument(), start, end);//NOI18N
                            extWarnings.add(er);
                         }

                         return super.visitImport(node, p);
                     }
 
                     class ImportPosition implements Position {
 
                         int position;
 
                         public ImportPosition(int position) {
                             this.position = position;
                         }
 
                         public int getOffset() {
                             return this.position;
                         }
                     }
                 }.scan(compilationInfo.getCompilationUnit(), null);
 
                 HintsController.setErrors(compilationInfo.getDocument(), HINTS_IDENT, extWarnings);
             }
         };
     }
 }
