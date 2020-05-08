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
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.javafx.tree.JFXBlock;
 import com.sun.tools.javafx.tree.JFXVar;
 import com.sun.tools.mjavac.util.JCDiagnostic;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.Imports;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.modules.javafx.editor.JavaFXDocument;
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
  * @author karol
  */
 public final class CreateElementTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private final AtomicBoolean cancel = new AtomicBoolean();
     private static final String ERROR_CODE = "compiler.err.cant.resolve.location"; //NOI18N
     private static final Logger log = Logger.getLogger(CreateElementTaskFactory.class.getName());
     private static final String TEMPLATE_JAVAFX = "Templates/JavaFX/JavaFXClass.fx"; //NOI18N
 
     public CreateElementTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     private enum Kind {
 
         LOCAL_CLASS,
         CLASS,
         FUNCTION,
         LOCAL_VARIABLE,
         VARIABLE
     };
 
     @Override
     public CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
 
                 cancel.set(false);
                 if (!(compilationInfo.getDocument() instanceof JavaFXDocument)) {
                     return;
                 }
                 JavaFXDocument document = (JavaFXDocument) compilationInfo.getDocument();
                 final Collection<ErrorDescription> errorDescriptions = new HashSet<ErrorDescription>();
 
                 for (final Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                     if (isValidError(diagnostic, compilationInfo)
                             && !cancel.get()
                             && (diagnostic instanceof JCDiagnostic)) {
 
                         String message = diagnostic.getMessage(Locale.ENGLISH);
                         Kind kinds[] = getKinds(message, diagnostic, compilationInfo);
                         for (Kind kind : kinds) {
                             errorDescriptions.add(getErrorDescription(document, compilationInfo, (JCDiagnostic) diagnostic, kind));
                         }
                     }
                 }
                 HintsController.setErrors(compilationInfo.getDocument(), "", errorDescriptions);
             }
         };
     }
 
     private boolean isValidError(Diagnostic diagnostic, CompilationInfo compilationInfo) {
         if (diagnostic.getCode().equals(ERROR_CODE)) {
 //            for (Diagnostic d : compilationInfo.getDiagnostics()) {
 //                if (d.getLineNumber() == diagnostic.getLineNumber()) {
 //                    return false;
 //                }
 //            }
             return true;
         }
 
         return false;
     }
 
     private ErrorDescription getErrorDescription(JavaFXDocument document,
             final CompilationInfo compilationInfo,
             final JCDiagnostic diagnostic,
             final Kind kind) {
 
        if (kind == null) {
             return null;
         }
         String message = getMessage(kind, diagnostic.getArgs()[1].toString(), diagnostic.getArgs()[5].toString(), compilationInfo.getCompilationUnit().getPackageName().toString());
         Fix fix = new ElementFix(kind, document, diagnostic, compilationInfo, message);
         ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, message, Collections.singletonList(fix), compilationInfo.getFileObject(), (int) diagnostic.getStartPosition(), (int) diagnostic.getStartPosition());
 
         return errorDescription;
     }
 
     private Kind[] getKinds(String message, Diagnostic diagnostic, CompilationInfo compilationInfo) {
         if (message.contains("symbol  : class")) { //NOI18N
             return new Kind[]{Kind.LOCAL_CLASS, Kind.CLASS};
         } else if (message.contains("symbol  : variable")) { //NOI18N
 
             if (!isValidLocalVariable(diagnostic, compilationInfo)) {
                 return new Kind[]{Kind.VARIABLE};
             }
             return new Kind[]{Kind.VARIABLE, Kind.LOCAL_VARIABLE};
         } else if (message.contains("symbol  : function")) { //NOI18N
             return new Kind[]{Kind.FUNCTION}; //NOI18N
         }
 
         throw new IllegalStateException();
     }
 
     private boolean isValidLocalVariable(final Diagnostic diagnostic, final CompilationInfo compilationInfo) {
         final boolean[] validVar = new boolean[1];
         new JavaFXTreePathScanner<Void, Void>() {
 
             @Override
             public Void visitIdentifier(IdentifierTree node, Void p) {
                 SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                 int startPosition = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), node);
                 if (startPosition == diagnostic.getStartPosition()) {
                     validVar[0] = (getCurrentPath().getParentPath().getLeaf() instanceof JFXBlock);
                     return null;
                 }
 
                 return super.visitIdentifier(node, p);
             }
         }.scan(compilationInfo.getCompilationUnit(), null);
 
         return validVar[0];
     }
 
     private static String getMessage(Kind kind, String elementName, String classFullName, String packageName) {
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
             return NbBundle.getMessage(CreateElementTaskFactory.class, message, elementName, packageName);
         }
 
         return NbBundle.getMessage(CreateElementTaskFactory.class, message, elementName, classFullName);
         //return NbBundle.getMessage(CreateNewElementTaskFactory.class, message, diagnostic.getArgs()[1].toString(), diagnostic.getArgs()[5].toString());
     }
 
     private class ElementFix implements Fix {
 
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
                 generatedCode[0] = createFunction();
             } else if (kind == Kind.LOCAL_VARIABLE || kind == Kind.VARIABLE) {
                 generatedCode[0] = createVariable(diagnostic, kind);
             } else if (kind == Kind.LOCAL_CLASS) {
                 generatedCode[0] = createLocalClass();
             } else if (kind == Kind.CLASS) {
                 //Does not insert any code in current class, creates new class and open it in editor
                 createClass();
             }
             if (generatedCode[0] == null) {
                 return null;
             }
             SwingUtilities.invokeLater(new Runnable() {
 
                 public void run() {
                     try {
                         document.insertString(generatedCode[0].getPositon(), generatedCode[0].getCode(), null);
                         if (kind != Kind.FUNCTION) {
                             return;
                         }
                         JTextComponent target = HintsUtils.getEditorComponent(document);
                         if (target == null) {
                             log.severe("No GUI component for editor document " + document); //NOI18N
                             return;
                         }
                         Imports.addImport(target, HintsUtils.EXCEPTION_UOE);
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             });
 
             return null;
         }
 
         private GeneratedCode createFunction() {
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
 
             String space = HintsUtils.calculateSpace(position[0], document);
             if (space.length() > 0) {
                 space = space.substring(0, space.length() - 1);
             }
             code.append("\n\n"); //NOI18N
             code.append(space).append("function ").append(name).append("() {\n"); //NOI18N
             code.append(space).append(HintsUtils.TAB).append("throw new UnsupportedOperationException('Not implemented yet');\n"); //NOI18N
             code.append(space).append("}\n"); //NOI18N
 
             return new GeneratedCode(position[0], code.toString());
         }
 
         private GeneratedCode createVariable(final JCDiagnostic diagnostic, Kind kind) {
             Object varName = diagnostic.getArgs()[1];
             GeneratedCode generatedCode = null;
             if (kind == Kind.LOCAL_VARIABLE) {
                 generatedCode = generateLocalVar(varName.toString());
             } else if (kind == Kind.VARIABLE) {
                 generatedCode = generatelVar(varName.toString());
             }
 
             return generatedCode;
         }
 
         private GeneratedCode generateLocalVar(String varName) {
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
             String space = HintsUtils.calculateSpace(position[0], document);
             StringBuffer code = new StringBuffer().append("var ").append(varName).append(";\n").append(space); //NOI18N
             if (position[0] < 0) {
                 position[0] = (int) diagnostic.getStartPosition();
             }
 
             return new GeneratedCode(position[0], code.toString());
         }
 
         private GeneratedCode generatelVar(final String varName) {
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
                         if (firstVar != null) {
                             position[0] = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), firstVar);
                             String space = HintsUtils.calculateSpace(position[0], document);
                             code.append("var ").append(varName).append(";\n").append(space); //NOI18N
 
                             return null;
                         }
                         try {
                             //TODO Line below returns 0 in same cases which means start of the node and end of the node is the same which is not true.
                             //int length = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), currentClass) - start;
                             //TODO Workaround for this problem
                             String sourceCode = document.getText(start, document.getLength() - start);
                             int index = sourceCode.indexOf("{"); //NOI18N
                             position[0] = start + index + 1;
                             String space = HintsUtils.calculateSpace(position[0], document);
                             code.append("\n").append(space).append(HintsUtils.TAB).append("var ").append(varName).append(";"); //NOI18N
                         } catch (BadLocationException ex) {
                             log.severe(ex.getMessage());
                         }
 
                         return null;
                     }
 
                     return super.visitIdentifier(node, p);
                 }
             }.scan(compilationInfo.getCompilationUnit(), null);
             if (position[0] < 0) {
                 return generateLocalVar(varName);
             }
 
             return diagnostic.getStartPosition() < position[0] ? generateLocalVar(varName) : new GeneratedCode(position[0], code.toString());
         }
 
         private GeneratedCode createLocalClass() {
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
 
         private void createClass() throws DataObjectNotFoundException, IOException {
             FileObject classTemplate = FileUtil.getConfigFile(TEMPLATE_JAVAFX); //NOI18N
             DataObject classTemplateDO = DataObject.find(classTemplate);
             DataObject od = classTemplateDO.createFromTemplate(DataFolder.findFolder(compilationInfo.getFileObject().getParent()), diagnostic.getArgs()[1].toString());
             OpenCookie openCookie = od.getCookie(OpenCookie.class);
             openCookie.open();
         }
     }
 
     private class GeneratedCode {
 
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
     }
 }
 
