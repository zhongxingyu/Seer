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
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.javac.code.Symbol.ClassSymbol;
 import com.sun.tools.javafx.tree.JFXInstanciate;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.swing.text.*;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.Imports;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.editor.Utilities;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class AddImportTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private final static EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES);
     private final static String HINTS_IDENT = "addimportjavafx"; //NOI18N
     private final static String ERROR_CODE1 = "compiler.err.cant.resolve.location";//NOI18N
     private final static String ERROR_CODE2 = "compiler.err.cant.resolve";//NOI18N
     private final AtomicBoolean cancel = new AtomicBoolean();
 
     public AddImportTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
         final Map<String, Collection<ElementHandle<TypeElement>>> optionsCache = new HashMap<String, Collection<ElementHandle<TypeElement>>>();
         final List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
 
         return new CancellableTask<CompilationInfo>() {
 
             @Override
             public void cancel() {
                 cancel.set(true);
             }
 
             @Override
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final ClassIndex classIndex = ClasspathInfo.create(file).getClassIndex();
                 if (file == null) {
                     throw new IllegalArgumentException();
                 }
                 if (!compilationInfo.isErrors()) {
                     if (compilationInfo.getDocument() != null) {
                         HintsController.setErrors(compilationInfo.getDocument(), HINTS_IDENT, Collections.EMPTY_LIST);
                     }
                     clear();
                     return;
                 }
                 for (final Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                    if (cancel.get()) {
                        break;
                    }

                    boolean onlyAbstractError = false;
                    if (diagnostic.getCode().equals(ERROR_CODE1) || diagnostic.getCode().equals(ERROR_CODE2)) {
                        onlyAbstractError = true;
                    }
                    if (!onlyAbstractError) {
                         continue;
                     }
                     final Collection<String> imports = new HashSet<String>();
                     new JavaFXTreePathScanner<Void, Void>() {
 
                         @Override
                         public Void visitImport(ImportTree node, Void p) {
                             node.getQualifiedIdentifier();
                             JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), node.getQualifiedIdentifier());
                             Element element = compilationInfo.getTrees().getElement(path);
                             if (element instanceof ClassSymbol) {
                                 ClassSymbol classSymbol = (ClassSymbol) element;
                                 imports.add(classSymbol.getQualifiedName().toString());
                             }
 
                             return super.visitImport(node, p);
                         }
                     }.scan(compilationInfo.getCompilationUnit(), null);
                     JavaFXTreePath path = compilationInfo.getTreeUtilities().pathFor(diagnostic.getPosition());
                     Element element = compilationInfo.getTrees().getElement(path);
                     Tree superTree = compilationInfo.getTreeUtilities().pathFor(diagnostic.getStartPosition()).getLeaf();
                     String potentialFqn = null;
                     if (element != null && element instanceof ClassSymbol) {
                         ClassSymbol classSymbol = (ClassSymbol) element;
                         potentialFqn = classSymbol.getSimpleName().toString();
                     } else if (superTree instanceof JFXInstanciate) {
                         final SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                         final Tree[] tree = new Tree[1];
                         JavaFXTreePathScanner<Void, Void> scaner = new JavaFXTreePathScanner<Void, Void>() {
 
                             @Override
                             public Void visitInstantiate(InstantiateTree node, Void p) {
                                 int position = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node);
                                 if (diagnostic.getStartPosition() == position) {
                                     tree[0] = node;
                                     return null;
                                 }
                                 return super.visitInstantiate(node, p);
                             }
                         };
                         scaner.scan(compilationInfo.getCompilationUnit(), null);
                         if (tree[0] != null) {
                             superTree = tree[0];
                             potentialFqn = HintsUtils.getClassSimpleName(superTree.toString());
                         }
                     }
 //                    if (findPosition(compilationInfo, superTree) < 0) {
 //                        continue;
 //                    }
                     if (potentialFqn == null || potentialFqn.length() == 0) {
                         return;
                     }
                     potentialFqn = HintsUtils.getClassSimpleName(potentialFqn);
                     Collection<ElementHandle<TypeElement>> options = optionsCache.get(potentialFqn);
                     if (options == null) {
                         options = classIndex.getDeclaredTypes(potentialFqn, ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
                         optionsCache.put(potentialFqn, options);
                     }
                     List<Fix> listFQN = new ArrayList<Fix>();
                     boolean exists = false;
                     for (ElementHandle<TypeElement> elementHandle : options) {
                         potentialFqn = elementHandle.getQualifiedName();
                         for (String importFQN : imports) {
                             if (potentialFqn.equals(importFQN)) {
                                 exists = true;
                                 break;
                             }
                         }
                         if (!exists) {
                             listFQN.add(new FixImport(potentialFqn));
                         }
                     }
                     if (listFQN.isEmpty()) {
                         continue;
                     }
                     ErrorDescription er = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, "", listFQN, compilationInfo.getFileObject(), (int) diagnostic.getStartPosition(), (int) diagnostic.getEndPosition());//NOI18N
                     errors.add(er);
                 }
                 HintsController.setErrors(compilationInfo.getDocument(), HINTS_IDENT, errors);
                 clear();
             }
 
             private void clear() {
                 optionsCache.clear();
                 errors.clear();
             }
         };
     }
 
     private class FixImport implements Fix {
 
         private String fqn;
 
         public FixImport(String fqn) {
             this.fqn = fqn;
         }
 
         public String getText() {
             return NbBundle.getMessage(AddImportTaskFactory.class, "TITLE_ADD_IMPORT") + fqn; //NOI18N
         }
 
         public ChangeInfo implement() throws Exception {
             JTextComponent target = Utilities.getFocusedComponent();
             Imports.addImport(target, fqn);
             return null;
         }
     }
 }
