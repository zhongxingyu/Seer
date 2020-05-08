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
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.javac.code.Symbol.ClassSymbol;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Symbol.VarSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javafx.tree.JFXImport;
 import com.sun.tools.javafx.tree.JFXInstanciate;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.lang.model.element.*;
 import javax.lang.model.element.TypeElement;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.StyledDocument;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.Imports;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.javafx.editor.hints.HintsModel.Hint;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class OverrideAllTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private static final String EXCEPTION = "java.lang.UnsupportedOperationException"; //NOI18N
     private static final EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES);
     private static final String TAB = "    "; //NOI18N
     private static final String ERROR_CODE1 = "compiler.err.does.not.override.abstract"; //NOI18N
     private static final String ERROR_CODE2 = "compiler.err.abstract.cant.be.instantiated"; //NOI18N
     private static final String HINT_IDENT = "overridejavafx"; //NOI18N
     private final AtomicBoolean cancel = new AtomicBoolean();
     
 
     public OverrideAllTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.NORMAL);
     }
 
     @Override
     public CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         final Map<String, Collection<ElementHandle<TypeElement>>> optionsCache = new HashMap<String, Collection<ElementHandle<TypeElement>>>();
         final Map<ElementHandle<TypeElement>, TypeElement> typeElementCash = new HashMap<ElementHandle<TypeElement>, TypeElement>();
         final Map<TypeElement, Collection<? extends Element>> elementsCash = new HashMap<TypeElement, Collection<? extends Element>>();
         final Collection<Boolean> mixinMain = new HashSet<Boolean>();
         final Collection<Boolean> mixinExtends = new HashSet<Boolean>();
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final StyledDocument document = (StyledDocument) compilationInfo.getDocument();
                 new JavaFXTreePathScanner<Void, Void>() {
 
                     @Override
                     public Void visitClassDeclaration(ClassDeclarationTree node, Void v) {
                         if (node.getModifiers().toString().contains("mixin")) { //NOI18N
                             mixinMain.add(Boolean.TRUE);
                         }
                         if (node.getMixins() != null && !node.getMixins().isEmpty()) {
                             mixinExtends.add(Boolean.TRUE);
                         }
                         return super.visitClassDeclaration(node, v);
                     }
                 }.scan(compilationInfo.getCompilationUnit(), null);
                 ClassIndex classIndex = ClasspathInfo.create(file).getClassIndex();
                 final HintsModel modelFix = new HintsModel(compilationInfo);
                 for (final Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                     if (!isValidError(diagnostic.getCode()) || cancel.get()) {
                         continue;
                     }
                     JavaFXTreePath path = compilationInfo.getTreeUtilities().pathFor(diagnostic.getPosition());
                     Element element = compilationInfo.getTrees().getElement(path);
                     Tree superTree = compilationInfo.getTreeUtilities().parseExpression("", (int) diagnostic.getStartPosition());
 
                     String className = null;
                     if (element != null && element instanceof ClassSymbol) {
                         ClassSymbol classSymbol = (ClassSymbol) element;
                         className = classSymbol.getSimpleName().toString();
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
                             className = HintsUtils.getClassSimpleName(superTree.toString());
                         }
                     }
                     if (findPosition(compilationInfo, superTree) < 0) {
                         continue;
                     }
                     if (className != null) {
                         final Collection<MethodSymbol> overriddenMethods = new HashSet<MethodSymbol>();
                         final Collection<JFXImport> imports = new HashSet<JFXImport>();
                         final Collection<MethodSymbol> abstractMethods = new HashSet<MethodSymbol>();
 
                         JavaFXTreePathScanner<Void, Void> visitor = new OverrideAllVisitor(compilationInfo, overriddenMethods, imports);
                         visitor.scan(compilationInfo.getCompilationUnit(), null);
                         Collection<ElementHandle<TypeElement>> options = optionsCache.get(className);
                         if (options == null) {
                             options = classIndex.getDeclaredTypes(className, ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
                             optionsCache.put(className, options);
                         }
                         for (ElementHandle<TypeElement> elementHandle : options) {
                             TypeElement typeElement = typeElementCash.get(elementHandle);
                             if (typeElement == null) {
                                 typeElement = elementHandle.resolve(compilationInfo);
                                 typeElementCash.put(elementHandle, typeElement);
                             }
                             if (typeElement == null) {
                                 continue;
                             }
                             Collection<? extends Element> elements = elementsCash.get(typeElement);
                             if (elements == null) {
                                 elements = elements = getAllMembers(typeElement, compilationInfo);
                                 elementsCash.put(typeElement, elements);
                             }
                             for (Element e : elements) {
                                 if (e instanceof MethodSymbol) {
                                     MethodSymbol method = (MethodSymbol) e;
                                     for (Modifier modifier : method.getModifiers()) {
                                         if (modifier != Modifier.ABSTRACT) {
                                             continue;
                                         }
 
                                         MethodSymbol overriddenMethod = HintsUtils.isOverridden(overriddenMethods, method, compilationInfo);
                                         if (overriddenMethod == null) {
                                             abstractMethods.add(method);
                                             break;
                                         }
 
                                     }
                                 }
                             }
                         }
                         if (!abstractMethods.isEmpty()) {
                             modelFix.addHint(superTree, abstractMethods);
                         }
                     }
                 }
                 addHintsToController(document, modelFix, compilationInfo, file);
                 clear();
             }
 
             private void clear() {
                 optionsCache.clear();
                 typeElementCash.clear();
                 elementsCash.clear();
                 mixinExtends.clear();
                 mixinMain.clear();
             }
         };
     }
 
     private boolean isValidError(String errorCode) {
         if (errorCode.equals(ERROR_CODE1) || errorCode.equals(ERROR_CODE2)) {
             return true;
         }
         return false;
     }
 
     private int findPosition(CompilationInfo compilationInfo, Tree tree) {
         SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
         int start = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), tree);
         Document document = compilationInfo.getDocument();
         try {
             String text = document.getText(0, document.getLength()).substring(start, document.getLength());
             int index = text.indexOf("{"); //NOI18N
             if (index > 0) {
                 return start + index + 1;
             } else {
                 return -1;
             }
         } catch (BadLocationException ex) {
             ex.printStackTrace();
         }
 
         return -1;
     }
 
     private void addHintsToController(Document document, HintsModel model, CompilationInfo compilationInfo, FileObject file) {
         if (model.getHints() != null) {
             Collection<ErrorDescription> errors = new HashSet<ErrorDescription>();
             for (Hint hint : model.getHints()) {
                 errors.add(getErrorDescription(document, file, hint, compilationInfo));
             }
             if (document != null || !errors.isEmpty()) {
                 HintsController.setErrors(document, HINT_IDENT, errors); //NOI18N
             }
         } else {
             if (document != null) {
                 HintsController.setErrors(document, HINT_IDENT, Collections.EMPTY_LIST); //NOI18N
             }
 
         }
     }
 
     private ErrorDescription getErrorDescription(final Document document, FileObject file, final Hint hint, final CompilationInfo compilationInfo) {
         Fix fix = new Fix() {
 
             public String getText() {
                 return NbBundle.getMessage(OverrideAllTaskFactory.class, "TITLE_IMPLEMENT_ABSTRACT"); //NOI18N
             }
 
             public ChangeInfo implement() throws Exception {
                 final StringBuilder methods = new StringBuilder();
                 final String space = HintsUtils.calculateSpace(hint.getStartPosition(), document);
                 for (MethodSymbol methodSymbol : hint.getMethods()) {
                     methods.append(createMethod(methodSymbol, space));
                 }
                 if (methods.toString().length() > 0) {
                     methods.append(space);
                 }
                 final int positon = findPosition(compilationInfo, hint.getTree());
                 if (positon < 0) {
                     return null;
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                     public void run() {
                         try {
                             document.insertString(positon, methods.toString(), null);
                             JTextComponent target = Utilities.getFocusedComponent();
                             if (target == null) {
                                 return;
                             }
                             Imports.addImport(target, EXCEPTION);
                             for (MethodSymbol method : hint.getMethods()) {
                                 addImport(target, method.asType());
                                 for (VarSymbol var : method.getParameters()) {
                                     scanImport(target, var.asType());
                                 }
                             }
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                     }
                 });
 
                 return null;
             }
 
             private void scanImport(JTextComponent target, Type type) {
                 if (type.getParameterTypes() != null && !type.getParameterTypes().isEmpty()) {
                     for (Type t : type.getParameterTypes()) {
                         scanImport(target, t);
                     }
                 }
                 addImport(target, type);
             }
 
             private void addImport(JTextComponent target, Type type) {
                 if (type == null) {
                     return;
                 }
                 String importName = type.toString();
                 if (!type.isPrimitive() && !importName.equals("void") && !importName.equals("Void") && importName.contains(".")) { //NOI18N
                     importName = removeBetween("()", importName); //NOI18N
                     importName = removeBetween("<>", importName); //NOI18N
                     Matcher symbolMatcher = Pattern.compile("[\\[\\]!@#$%^&*(){}|:'?/<>~`,]").matcher(importName); //NOI18N
                     if (symbolMatcher.find()) {
                         importName = symbolMatcher.replaceAll("").trim(); //NOI18N
                     }
                     if (importName.contains(".") && !importName.equals("java.lang.Object")) { //NOI18N
                         Imports.addImport(target, importName);
                     }
                 }
             }
 
             private String createMethod(MethodSymbol methodSymbol, String space) {
                 StringBuilder method = new StringBuilder();
                 method.append("\n").append(space).append(TAB).append("override "); //NOI18N
                 for (Modifier modifier : methodSymbol.getModifiers()) {
                     switch (modifier) {
                         case PUBLIC:
                             method.append("public "); //NOI18N
                             break;
                         case PROTECTED:
                             method.append("protected "); //NOI18N
                             break;
                     }
                 }
                 method.append("function ").append(methodSymbol.getQualifiedName() + " ("); //NOI18N
                 //TODO Work around for NPE which is thrown by methodSymbol.getParameters();
                 try {
                     if (methodSymbol.getParameters() != null) {
                         Iterator<VarSymbol> iterator = methodSymbol.getParameters().iterator();
                         while (iterator.hasNext()) {
                             VarSymbol var = iterator.next();
                             String varType = getTypeString(var.asType());
                             method.append(var.getSimpleName()).append(" : ").append(HintsUtils.getClassSimpleName(varType)); //NOI18N
                             if (iterator.hasNext()) {
                                 method.append(", "); //NOI18N
                             }
                         }
                     }
                 } catch (NullPointerException npe) {
                     npe.printStackTrace();
                 }
                 //TODO Work around for methodSymbol.getReturnType() which throws NPE!
                 String returnType = null;
                 try {
                     returnType = getTypeString(methodSymbol.getReturnType());
                 } catch (NullPointerException npe) {
                     npe.printStackTrace();
                 }
                 if (returnType == null || returnType.equals("void")) { //NOI18N
                     returnType = "Void"; //NOI18N
                 } else {
                     returnType = HintsUtils.getClassSimpleName(returnType);
                 }
                 method.append(")").append(" : ").append(returnType).append(" { \n"); //NOI18N
                 method.append(space).append(TAB).append(TAB).append("throw new UnsupportedOperationException('Not implemented yet');\n"); //NOI18N
                 method.append(space).append(TAB).append("}\n"); //NOI18N
 
                 return method.toString();
             }
         };
         if (hint.getStartPosition() < 0) {
             return null;
         }
         ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, NbBundle.getMessage(OverrideAllTaskFactory.class, "TITLE_IMPLEMENT_ABSTRACT"), Collections.singletonList(fix), file, hint.getStartPosition(), hint.getStartPosition());
         return ed;
     }
 
     private String removeBetween(String symbols, String name) {
         int firstIndex = name.indexOf(symbols.substring(0, 1));
         int lastIndex = name.indexOf(symbols.substring(1, 2));
         if (firstIndex < 0 || lastIndex < 0) {
             return name;
         }
         name = name.replace(name.substring(firstIndex, lastIndex + 1), "");
         return name;
     }
 
     private String getTypeString(Type type) {
        String varType = type.toString();
         if (type.isPrimitive()) {
             if (varType.equals("int")) { //NOI18N
                 varType = Integer.class.getSimpleName();
             } else if (varType.equals("long") //NOI18N
                     || varType.equals("byte") //NOI18N
                     || varType.equals("short") //NOI18N
                     || varType.equals("float") //NOI18N
                     || varType.equals("double")) { //NOI18N
 
                 varType = "Number"; //NOI18N
             } else if (varType.equals("char")) { //NOI18N
                 varType = Character.class.getSimpleName();
             } else if (varType.equals("boolean")) { //NOI18N
                 return Boolean.class.getSimpleName();
             }
         }
         if (varType.equals("E") || varType.equals("T")) { //NOI18N
             varType = "Object"; //NOI18N
         }
         if (varType.equals("E[]") || varType.equals("T[]")) { //NOI18N
             varType = "Object[]"; //NOI18N
         }
         varType = removeBetween("<>", varType); //NOI18N
         return varType;
     }
 
     //TODO Temporary log for issue 148890
     Collection<? extends Element> getAllMembers(TypeElement typeElement, CompilationInfo compilationInfo) {
         Collection<? extends Element> elements = null;
         try {
             elements = compilationInfo.getElements().getAllMembers(typeElement);
         } catch (NullPointerException npe) {
             npe.printStackTrace();
             System.err.println("* e = " + typeElement); //NOI18N
             System.err.println("* e.getKind() = " + typeElement.getKind()); //NOI18N
             System.err.println("* e.asType() = " + typeElement.asType()); //NOI18N
         }
         return elements;
     }
 }
 
 
 
 
