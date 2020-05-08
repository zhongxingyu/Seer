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
 
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.javafx.code.JavafxClassSymbol;
 import com.sun.tools.javafx.tree.JFXBlock;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import com.sun.tools.javafx.tree.JFXVar;
 import com.sun.tools.javafx.tree.JFXVarInit;
 import com.sun.tools.mjavac.code.Type;
 import com.sun.tools.mjavac.util.JCDiagnostic;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.spi.editor.hints.ChangeInfo;
 import org.netbeans.spi.editor.hints.ErrorDescription;
 import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
 import org.netbeans.spi.editor.hints.Fix;
 import org.netbeans.spi.editor.hints.HintsController;
 import org.netbeans.spi.editor.hints.Severity;
 import org.openide.cookies.OpenCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataFolder;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class CreateElementTaskFactory extends JavaFXAbstractEditorHint {
 
     private final AtomicBoolean cancel = new AtomicBoolean();
     private static final String ERROR_CODE = "compiler.err.cant.resolve.location"; //NOI18N
     private static final Logger LOGGER = Logger.getLogger(CreateElementTaskFactory.class.getName());
     private static final String TEMPLATE_JAVAFX = "Templates/JavaFX/JavaFXClass.fx"; //NOI18N
     private static final String JAVAFX_RUN = "public static synthetic function javafx$run$"; //NOI18N
     private final Collection<ErrorDescription> errorDescriptions = new HashSet<ErrorDescription>();
 
     public CreateElementTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     enum Kind {
 
         LOCAL_CLASS,
         CLASS,
         FUNCTION,
         LOCAL_VARIABLE,
         VARIABLE
     };
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 errorDescriptions.clear();
                 cancel.set(false);
                 Document document = compilationInfo.getDocument();
                 for (final Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                     if (cancel.get()) {
                         break;
                     }
                     if (!(diagnostic instanceof JCDiagnostic)) {
                         continue;
                     }
                     if (isValidError(diagnostic, document) && (diagnostic instanceof JCDiagnostic)) {
                         Kind kinds[] = getKinds((JCDiagnostic) diagnostic, compilationInfo);
                         for (Kind kind : kinds) {
                             errorDescriptions.add(getErrorDescription(document, compilationInfo, (JCDiagnostic) diagnostic, kind));
                         }
                     }
                 }
                 HintsController.setErrors(compilationInfo.getDocument(), "", errorDescriptions); //NOI18N
             }
         };
     }
 
     @Override
     Collection<ErrorDescription> getErrorDescriptions() {
         return Collections.unmodifiableCollection(errorDescriptions);
     }
 
     private boolean isValidError(Diagnostic diagnostic, Document document) {
         if (diagnostic.getCode().equals(ERROR_CODE) && !HintsUtils.isInGuardedBlock(document, (int) diagnostic.getPosition())) {
             return true;
         }
 
         return false;
     }
 
     private ErrorDescription getErrorDescription(Document document,
             final CompilationInfo compilationInfo,
             final JCDiagnostic diagnostic,
             final Kind kind) {
 
         if (kind == null || diagnostic.getArgs()[1] == null || diagnostic.getArgs()[5] == null) {
             LOGGER.severe("Kind is null "); //NOI18N
             return null;
         }
         String message = getMessage(kind, diagnostic.getArgs()[1].toString(), diagnostic.getArgs()[5].toString(), compilationInfo.getCompilationUnit().getPackageName());
         Fix fix = new ElementFix(kind, document, diagnostic, compilationInfo, message);
         ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, message, Collections.singletonList(fix), compilationInfo.getFileObject(), (int) diagnostic.getStartPosition(), (int) diagnostic.getStartPosition());
 
         return errorDescription;
     }
 
     private Kind[] getKinds(final JCDiagnostic diagnostic, final CompilationInfo compilationInfo) {
         final SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
         final ArrayList<Kind> array = new ArrayList<Kind>();
         final String[] classFQN = new String[1];
         if (diagnostic.getArgs().length >= 6 && diagnostic.getArgs()[5] instanceof String) {
             classFQN[0] = (String) diagnostic.getArgs()[5];
         }
         new JavaFXTreePathScanner<Void, Void>() {
 
             private String currentClassFQN;
 
             @Override
             public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                 JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), node);
                 Element element = compilationInfo.getTrees().getElement(path);
                 if (element instanceof JavafxClassSymbol) {
                     JavafxClassSymbol classSymbol = (JavafxClassSymbol) element;
                    classSymbol.getInterfaces();
                     currentClassFQN = classSymbol.className();
                    for (Tree tree : node.getExtends()) {
                        if (currentClassFQN != null && checkPosition(tree, diagnostic) && currentClassFQN.equals(classFQN[0])) {
                            array.add(Kind.CLASS);
                            array.add(Kind.LOCAL_CLASS);
                            return null;
                         }
                     }
                 }
                 return super.visitClassDeclaration(node, p);
             }
 
             @Override
             public Void visitInstantiate(InstantiateTree node, Void p) {
                 if (currentClassFQN != null && checkPosition(node, diagnostic) && currentClassFQN.equals(classFQN[0])) {
                     array.add(Kind.CLASS);
                     array.add(Kind.LOCAL_CLASS);
                     return null;
                 }
                 return super.visitInstantiate(node, p);
             }
 
             @Override
             public Void visitIdentifier(IdentifierTree node, Void p) {
                 if (currentClassFQN != null && checkPosition(node, diagnostic) && currentClassFQN.equals(classFQN[0])) {
                     if (getCurrentPath().getParentPath().getLeaf() instanceof JFXBlock) {
                         array.add(Kind.LOCAL_VARIABLE);
                     }
                     array.add(Kind.VARIABLE);
                     return null;
                 }
                 return super.visitIdentifier(node, p);
             }
 
             @Override
             public Void visitMethodInvocation(FunctionInvocationTree node, Void p) {
                 if (currentClassFQN != null && checkPosition(node, diagnostic) && currentClassFQN.equals(classFQN[0])) {
                     array.add(Kind.FUNCTION);
                     return null;
                 }
                 return super.visitMethodInvocation(node, p);
             }
 
             private boolean checkPosition(Tree node, Diagnostic diagnostic) {
                 long start = sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node);
                 //long end = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), node);
 
                 if (start == diagnostic.getStartPosition()) {
                     return true;
                 }
 
                 return false;
             }
         }.scan(compilationInfo.getCompilationUnit(), null);
 
         return array.toArray(new Kind[array.size()]);
     }
 
     private static String getMessage(Kind kind, String elementName, String classFullName, ExpressionTree packageName) {
         String message = null;
         if (kind == Kind.FUNCTION) {
             message = "LABEL_FUNCTION"; //NOI18N
         } else if (kind == Kind.VARIABLE) {
             message = "LABEL_VARIABLE"; //NOI18N
         } else if (kind == Kind.LOCAL_VARIABLE) {
             message = "LABEL_LOCAL_VARIABLE"; //NOI18N
         } else if (kind == Kind.LOCAL_CLASS) {
             message = "LABEL_LOCAL_CLASS"; //NOI18N
         } else if (kind == Kind.CLASS) {
             message = "LABEL_CLASS"; //NOI18N
             return NbBundle.getMessage(CreateElementTaskFactory.class, message, elementName, packageName != null ? packageName.toString() : ""); //NOI18N
         }
 
         return NbBundle.getMessage(CreateElementTaskFactory.class, message, elementName, classFullName);
         //return NbBundle.getMessage(CreateNewElementTaskFactory.class, message, diagnostic.getArgs()[1].toString(), diagnostic.getArgs()[5].toString());
     }
 
     private static class ElementFix implements Fix {
 
         private final Kind kind;
         private final Document document;
         private final JCDiagnostic diagnostic;
         private final CompilationInfo compilationInfo;
         private final String message;
 
         public ElementFix(Kind kind,
                 Document document,
                 JCDiagnostic diagnostic,
                 CompilationInfo compilationInfo,
                 String message) {
 
             this.kind = kind;
             this.document = document;
             this.diagnostic = diagnostic;
             this.compilationInfo = compilationInfo;
             this.message = message;
         }
 
         public String getText() {
             return message;
         }
 
         public ChangeInfo implement() throws Exception {
             final GeneratedCode[] generatedCode = new GeneratedCode[1];
             if (kind == Kind.FUNCTION) {
                 generatedCode[0] = createFunction(compilationInfo, diagnostic);
             } else if (kind == Kind.LOCAL_VARIABLE || kind == Kind.VARIABLE) {
                 generatedCode[0] = createVariable(compilationInfo, diagnostic, kind);
             } else if (kind == Kind.LOCAL_CLASS) {
                 generatedCode[0] = createLocalClass(compilationInfo, diagnostic);
             } else if (kind == Kind.CLASS) {
                 //Does not insert any code in current class, creates new class and open it in editor
                 createClass(compilationInfo, diagnostic);
             }
             if (generatedCode[0] == null) {
                 return null;
             }
             Runnable runnable = new Runnable() {
 
                 public void run() {
                     try {
                         document.insertString(generatedCode[0].getPositon(), generatedCode[0].getCode(), null);
                         if (kind != Kind.FUNCTION) {
                             return;
                         }
                         HintsUtils.addImport(document, HintsUtils.EXCEPTION_UOE);
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             };
             HintsUtils.runInAWT(runnable);
 
             return null;
         }
 
         @Override
         public String toString() {
             return super.toString() + " " + kind + " ";
         }
 
         private static GeneratedCode createFunction(final CompilationInfo compilationInfo, final JCDiagnostic diagnostic) {
             StringBuffer code = new StringBuffer();
             Object name = diagnostic.getArgs()[1];
 
             final int position[] = new int[1];
             new JavaFXTreePathScanner<Void, Void>() {
 
                 private ClassDeclarationTree currentClass;
 
                 @Override
                 public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                     this.currentClass = node;
 
                     return super.visitClassDeclaration(node, p);
                 }
 
                 @Override
                 public Void visitMethodInvocation(FunctionInvocationTree node, Void p) {
                     SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                     int startPosition = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node);
                     if (startPosition == diagnostic.getStartPosition()) {
                         position[0] = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), currentClass.getClassMembers().get(currentClass.getClassMembers().size() - 1));
 
                         return null;
                     }
 
                     return super.visitMethodInvocation(node, p);
                 }
             }.scan(compilationInfo.getCompilationUnit(), null);
 
             String space = HintsUtils.calculateSpace(position[0], compilationInfo.getDocument());
             if (space.length() > 0) {
                 space = space.substring(0, space.length() - 1);
             }
             code.append("\n\n"); //NOI18N
             code.append(space).append("function ").append(name).append("() {\n"); //NOI18N
             code.append(space).append(HintsUtils.TAB).append("throw new UnsupportedOperationException('Not implemented yet');\n"); //NOI18N
             code.append(space).append("}\n"); //NOI18N
 
             return new GeneratedCode(position[0], code.toString());
         }
 
         private static GeneratedCode createVariable(CompilationInfo compilationInfo, JCDiagnostic diagnostic, Kind kind) {
             Object varName = diagnostic.getArgs()[1];
             GeneratedCode generatedCode = null;
             if (kind == Kind.LOCAL_VARIABLE) {
                 generatedCode = generateLocalVar(compilationInfo, diagnostic, varName.toString());
             } else if (kind == Kind.VARIABLE) {
                 generatedCode = generateVar(compilationInfo, diagnostic, varName.toString());
             }
 
             return generatedCode;
         }
 
         private static GeneratedCode generateLocalVar(final CompilationInfo compilationInfo, final JCDiagnostic diagnostic, String varName) {
             final int position[] = new int[1];
             final SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
             position[0] = -1;
             new JavaFXTreePathScanner<Void, Void>() {
 
                 @Override
                 public Void visitIdentifier(IdentifierTree node, Void p) {
                     int startPosition = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node);
                     if (startPosition == diagnostic.getStartPosition()) {
                         position[0] = startPosition;
 
                         return null;
                     }
 
                     return super.visitIdentifier(node, p);
                 }
             }.scan(compilationInfo.getCompilationUnit(), null);
             String space = HintsUtils.calculateSpace(position[0], compilationInfo.getDocument());
             StringBuffer code = new StringBuffer().append("\n").append(space).append("var ").append(varName).append(";\n").append(space); //NOI18N
             if (position[0] < 0) {
                 position[0] = (int) diagnostic.getStartPosition();
             }
 
             return new GeneratedCode(position[0], code.toString());
         }
 
         private static GeneratedCode generateVar(final CompilationInfo compilationInfo, final JCDiagnostic diagnostic, final String varName) {
             final int position[] = new int[1];
             final SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
             final StringBuffer code = new StringBuffer();
             position[0] = -1;
             new JavaFXTreePathScanner<Void, Void>() {
 
                 private ClassDeclarationTree currentClass;
 
                 @Override
                 public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                     this.currentClass = node;
 
                     return super.visitClassDeclaration(node, p);
                 }
 
                 @Override
                 public Void visitIdentifier(IdentifierTree node, Void p) {
                     int startPosition = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node);
                     if (startPosition == diagnostic.getStartPosition()) {
                         int start = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), currentClass);
                         Tree firstVar = null;
                         Iterator<Tree> iterator = currentClass.getClassMembers().iterator();
                         if (iterator.hasNext()) {
                             Tree tree = iterator.next();
                             if (tree instanceof JFXVar) {
                                 firstVar = tree;
                             }
                         }
                         final Document document = compilationInfo.getDocument();
                         if (firstVar != null) {
                             position[0] = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), firstVar);
                             String space = HintsUtils.calculateSpace(position[0], document);
                             code.append("\n" + space).append("var ").append(varName).append(";\n").append(space); //NOI18N
 
                             return null;
                         }
 
                         try {
                             //TODO Line below returns 0 in some cases. It means start of the node and end of the node equals which is not true.
                             //int length = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), currentClass) - start;
                             //TODO Workaround for this problem
 
                             for (Tree tree : currentClass.getClassMembers()) {
                                 if (tree instanceof JFXFunctionDefinition && tree.toString().contains(JAVAFX_RUN)) {
                                     //position[0] = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(),((JFXFunctionDefinition) tree).getBodyExpression());
                                     List<ExpressionTree> statements = ((JFXFunctionDefinition) tree).getBodyExpression().getStatements();
                                     if (!statements.isEmpty()) {
                                         ExpressionTree treeForPosition = statements.iterator().next();
                                         //FIXME Bug in compiler - getStartPostion and getEndPosition return same value!
                                         code.append("\n").append("var ").append(varName).append(";\n"); //NOI18N
                                         if (treeForPosition instanceof JFXVarInit) {
                                             position[0] = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), treeForPosition);
                                         } else {
                                             code.append("\n"); //NOI18N
                                             position[0] = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), treeForPosition);
                                         }
                                     }
 
                                     return null;
                                 }
                             }
 
                             String sourceCode = document.getText(start, document.getLength() - start);
                             int index = sourceCode.indexOf("{"); //NOI18N
                             position[0] = start + index + 1;
                             String space = HintsUtils.calculateSpace(position[0], document);
                             code.append("\n").append(space).append(HintsUtils.TAB).append("var ").append(varName).append(";\n"); //NOI18N
                         } catch (BadLocationException ex) {
                             LOGGER.severe(ex.getMessage());
                         }
 
                         return null;
                     }
 
                     return super.visitIdentifier(node, p);
                 }
             }.scan(compilationInfo.getCompilationUnit(), null);
             if (position[0] < 0) {
                 return generateLocalVar(compilationInfo, diagnostic, varName);
             }
 
             return diagnostic.getStartPosition() < position[0] ? generateLocalVar(compilationInfo, diagnostic, varName) : new GeneratedCode(position[0], code.toString());
         }
 
         private static GeneratedCode createLocalClass(final CompilationInfo compilationInfo, JCDiagnostic diagnostic) {
             StringBuffer code = new StringBuffer();
             Object name = diagnostic.getArgs()[1];
             final int position[] = new int[1];
             final SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
             new JavaFXTreePathScanner<Void, Void>() {
 
                 @Override
                 public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                     position[0] = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), node);
                     return null;
                 }
             }.scan(compilationInfo.getCompilationUnit(), null);
             code.append("\n"); //NOI18N
             code.append("\nclass ").append(name).append(" {\n"); //NOI18N
             code.append(HintsUtils.TAB).append("//TODO Not implemented yet.\n"); //NOI18N
             code.append("}\n"); //NOI18N
 
             return new GeneratedCode(position[0], code.toString());
         }
 
         private static void createClass(CompilationInfo compilationInfo, JCDiagnostic diagnostic) throws DataObjectNotFoundException, IOException {
             FileObject classTemplate = FileUtil.getConfigFile(TEMPLATE_JAVAFX); //NOI18N
             DataObject classTemplateDO = DataObject.find(classTemplate);
             DataObject od = classTemplateDO.createFromTemplate(DataFolder.findFolder(compilationInfo.getFileObject().getParent()), diagnostic.getArgs()[1].toString());
             OpenCookie openCookie = od.getCookie(OpenCookie.class);
             openCookie.open();
         }
     }
 
     private static class GeneratedCode {
 
         private int positon;
         private String code;
 
         GeneratedCode(int position, String code) {
             this.code = code;
             this.positon = position;
         }
 
         public String getCode() {
             return code;
         }
 
         public int getPositon() {
             return positon;
         }
 
         @Override
         public String toString() {
             return code;
         }
     }
 }
 
