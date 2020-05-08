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
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.Tree;
 import java.util.Collection;
 import java.util.HashSet;
 import javax.swing.text.BadLocationException;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Symbol.VarSymbol;
 import com.sun.tools.javac.code.Type;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.swing.SwingUtilities;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.Position;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.Imports;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.javafx.editor.hints.HintsModel.Hint;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.filesystems.FileObject;
 import org.openide.text.Annotation;
 import org.openide.text.NbDocument;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author karol harezlak
  */
 public class ImplementAbstractTaskFactory extends EditorAwareJavaSourceTaskFactory {
 
     private static final String EXCEPTION = "java.lang.UnsupportedOperationException"; //NOI18N
     private static final String ANNOTATION_TYPE = "org.netbeans.modules.javafx.editor.hints"; //NOI18N
     private static final Comparator<List<VarSymbol>> COMPARATOR = new ParamsComparator();
     private EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES);
 
     public ImplementAbstractTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
         return new CancellableTask<CompilationInfo>() {
 
             private final Collection<Annotation> annotationsToRemove = new HashSet<Annotation>();
 
             public void cancel() {
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 final Map<Element, Collection<Tree>> classTrees = new HashMap<Element, Collection<Tree>>();
                 final Map<Element, List<MethodSymbol>> abstractMethods = new HashMap<Element, List<MethodSymbol>>();
                 final Map<Element, List<MethodSymbol>> overridenMethods = new HashMap<Element, List<MethodSymbol>>();
                 final Map<MethodSymbol, MethodSymbol> overridenToAbstract = new HashMap<MethodSymbol, MethodSymbol>();
                 StyledDocument document = (StyledDocument) FXSourceUtils.getDocument(file);
                 removeAnnotations(document, annotationsToRemove);
                 annotationsToRemove.clear();
                 JavaFXTreePathScanner<Void, Void> visitor = new JavaFXTreePathScanner<Void, Void>() {
 
                     @Override
                     public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                         List<Tree> extendsList = new ArrayList<Tree>();
                         if (node.getMixins() != null) {
                             extendsList.addAll(node.getMixins());
                         }
                         if (node.getExtends() != null) {
                             extendsList.addAll(node.getExtends());
                         }
                         if (extendsList != null && extendsList.size() != 0) {
                             Element currentClass = compilationInfo.getTrees().getElement(getCurrentPath());
                             if (classTrees.get(currentClass) == null) {
                                 classTrees.put(currentClass, new HashSet<Tree>());
                             }
                             Collection<Tree> extendsSet = classTrees.get(currentClass);
 
                             for (Tree extend : extendsList) {
                                 extendsSet.add(extend);
                             }
                             classTrees.put(currentClass, extendsSet);
                         }
                         return super.visitClassDeclaration(node, p);
                     }
 
                     @Override
                     public Void visitInstantiate(InstantiateTree node, Void p) {
 //                        Element element = compilationInfo.getTrees().getElement(getCurrentPath());
 //                        if (element != null && element.getKind() == ElementKind.CLASS) {
 //                            classTrees.put(element, Collections.<Tree>singletonList(node));
 //                        }
                         return super.visitInstantiate(node, p);
                     }
 
                     @Override
                     public Void visitFunctionDefinition(FunctionDefinitionTree node, Void p) {
                         if (node.toString().contains(" overridefunction ") || node.toString().contains(" override ")) {
                             Element element = compilationInfo.getTrees().getElement(getCurrentPath());
                             if (element != null) {
                                 Element currentClass = element.getEnclosingElement();
                                 if (element instanceof MethodSymbol) {
                                     if (overridenMethods.get(currentClass) == null) {
                                         overridenMethods.put(currentClass, new ArrayList<MethodSymbol>());
                                     }
                                     List<MethodSymbol> methods = overridenMethods.get(currentClass);
                                     methods.add((MethodSymbol) element);
                                     overridenMethods.put(currentClass, methods);
                                 }
                             }
                         }
                         return super.visitFunctionDefinition(node, p);
                     }
                 };
 
                 visitor.scan(compilationInfo.getCompilationUnit(), null);
                 ClassIndex classIndex = ClasspathInfo.create(file).getClassIndex();
                 for (Element currentClass : classTrees.keySet()) {
                     for (Tree tree : classTrees.get(currentClass)) {
                         if (HintsUtils.checkString(tree.toString())) {
                             continue;
                         }
                         Set<ElementHandle<TypeElement>> options = classIndex.getDeclaredTypes(tree.toString(), ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
                         for (ElementHandle<TypeElement> elementHandle : options) {
                             TypeElement typeElement = elementHandle.resolve(compilationInfo);
                             if (typeElement == null) {
                                 continue;
                             }
                             Collection<? extends Element> elements = getAllMembers(typeElement, compilationInfo);
                             for (Element element : elements) {
                                 if (element instanceof MethodSymbol) {
                                     MethodSymbol method = (MethodSymbol) element;
                                     for (Modifier modifier : method.getModifiers()) {
                                         if (modifier == Modifier.ABSTRACT) {
                                             if (abstractMethods.get(currentClass) == null) {
                                                 abstractMethods.put(currentClass, new ArrayList<MethodSymbol>());
                                             }
                                             List<MethodSymbol> methods = abstractMethods.get(currentClass);
                                             Collection<MethodSymbol> overridenMethodList = overridenMethods.get(currentClass);
                                             boolean exists = false;
                                             if (overridenMethodList != null && overridenMethodList.size() != 0) {
                                                 for (MethodSymbol overridenMethod : overridenMethodList) {
                                                     if (method.getQualifiedName().equals(overridenMethod.getQualifiedName()) &&
                                                             method.getParameters().size() == overridenMethod.getParameters().size() &&
                                                             COMPARATOR.compare(method.getParameters(), overridenMethod.getParameters()) == 0) {
 
                                                         overridenToAbstract.put(overridenMethod, method);
                                                         exists = true;
                                                         break;
                                                     }
                                                 }
                                             }
                                             if (exists) {
                                                 break;
                                             }
                                             methods.add(method);
                                             abstractMethods.put(currentClass, methods);
                                             break;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 HintsModel modelFix = new HintsModel(compilationInfo);
                 HintsModel modelOverriden = new HintsModel(compilationInfo);
                 for (Element currentClass : abstractMethods.keySet()) {
                     Tree currentTree = compilationInfo.getTrees().getTree(currentClass);
                     if (abstractMethods.get(currentClass) != null && abstractMethods.get(currentClass).size() != 0) {
                         modelFix.addHint(currentTree, abstractMethods.get(currentClass), currentClass);
                     }
                     if (overridenMethods.get(currentClass) != null && overridenMethods.get(currentClass).size() != 0) {
                         modelOverriden.addHint(currentTree, overridenMethods.get(currentClass), currentClass);
                     }
                 }
                 addHintsToController(modelFix, compilationInfo, file);
                 addOverriddenAnnotations(modelOverriden, file, compilationInfo, overridenToAbstract, annotationsToRemove);
             }
         };
     }
 
     private void addHintsToController(HintsModel model, CompilationInfo compilationInfo, FileObject file) {
         if (model.getHints() != null) {
             Collection<ErrorDescription> errors = new HashSet<ErrorDescription>();
             for (Hint hint : model.getHints()) {
                 errors.add(getErrorDescription(file, hint, compilationInfo)); //NOI18N
             }
             HintsController.setErrors(FXSourceUtils.getDocument(file), "Override", errors); //NOI18N
         }
     }
 
     private void addOverriddenAnnotations(final HintsModel model,
             final FileObject file,
             CompilationInfo compilationInfo,
             Map<MethodSymbol, MethodSymbol> overridenToAbstract,
             final Collection<Annotation> annotationsToRemove) {
 
         if (model.getHints() != null) {
             for (Hint hint : model.getHints()) {
                 resolveOverridenDescription(file, hint, compilationInfo, overridenToAbstract, annotationsToRemove); //NOI18N
             }
         }
     }
 
     private synchronized void resolveOverridenDescription(FileObject file,
             Hint hint,
             CompilationInfo compilationInfo,
             Map<MethodSymbol, MethodSymbol> overridenToAbstract,
             Collection<Annotation> annotationsToRemove) {
 
         Map<Annotation, Integer> annotations = new HashMap<Annotation, Integer>();
 
         for (MethodSymbol overridenMethod : hint.getMethods()) {
             Tree tree = compilationInfo.getTrees().getTree(overridenMethod);
             SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
             final int start = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), tree);
             MethodSymbol abstractMethod = overridenToAbstract.get(overridenMethod);
             String type = null;
             if (abstractMethod == null) {
                 type = overridenMethod.getEnclosingElement().toString();
             } else {
                 type = abstractMethod.getEnclosingElement().toString();
             }
             final String finalType = type;
             Annotation annotation = new Annotation() {
 
                 @Override
                 public String getAnnotationType() {
                     return ANNOTATION_TYPE;
                 }
 
                 @Override
                 public String getShortDescription() {
                     return finalType;
                 }
             };
             annotations.put(annotation, start);
         }
         final StyledDocument document = (StyledDocument) FXSourceUtils.getDocument(file);
         final Map<Annotation, Integer> annotationsCopy = new HashMap<Annotation, Integer>(annotations);
         annotationsToRemove.addAll(annotations.keySet());
         Runnable runnable = new Runnable() {
 
             public void run() {
                 for (Annotation annotation : annotationsCopy.keySet()) {
                     final int start = annotationsCopy.get(annotation);
                     Position position = new Position() {
 
                         public int getOffset() {
                             return start;
                         }
                     };
                     NbDocument.addAnnotation(document, position, start, annotation);
                 }
             }
         };
         if (SwingUtilities.isEventDispatchThread()) {
             runnable.run();
         } else {
             SwingUtilities.invokeLater(runnable);
         }
     }
 
     private void removeAnnotations(final StyledDocument document, Collection<Annotation> annotationsToRemove) {
         final Collection<Annotation> annotationsToRemoveCopy = new HashSet<Annotation>(annotationsToRemove);
         Runnable runnable = new Runnable() {
 
             public void run() {
                 for (Annotation annotation : annotationsToRemoveCopy) {
                     NbDocument.removeAnnotation(document, annotation);
                 }
             }
         };
         if (SwingUtilities.isEventDispatchThread()) {
             runnable.run();
         } else {
             SwingUtilities.invokeLater(runnable);
         }
         for (Annotation annotation : annotationsToRemove) {
             NbDocument.removeAnnotation(document, annotation);
         }
     }
 
     private ErrorDescription getErrorDescription(final FileObject file, final Hint hint, final CompilationInfo compilationInfo) {
         Fix fix = new Fix() {
 
             public String getText() {
                 return "Implement all abstract methods"; //NOI18N
             }
 
             public ChangeInfo implement() throws Exception {
                 final StringBuilder methods = new StringBuilder();
 
                 for (MethodSymbol methodSymbol : hint.getMethods()) {
                     methods.append(createMethod(methodSymbol));
                 }
                 final Document document = FXSourceUtils.getDocument(file);
                 final int positon = findPositionAtTheEnd(compilationInfo, hint.getTree());
                 SwingUtilities.invokeLater(new Runnable() {
 
                     public void run() {
                         try {
                             document.insertString(positon, methods.toString(), null);
                             JTextComponent target = Utilities.getFocusedComponent();
                             Imports.addImport(target, EXCEPTION);
                             for (MethodSymbol method : hint.getMethods()) {
                                 addImport(target, method.asType());
                                 for (VarSymbol var : method.getParameters()) {
                                     scanImport(target, var.asType());
                                 }
                             }
                         } catch (BadLocationException ex) {
                             Exceptions.printStackTrace(ex);
                         }
                     }
                 });
 
                 return null;
             }
 
             private void scanImport(JTextComponent target, Type type) {
                 if (type.getParameterTypes() != null && type.getParameterTypes().size() != 0) {
                     for (Type t : type.getParameterTypes()) {
                         scanImport(target, t);
                     }
                 }
                 addImport(target, type);
             }
 
             private void addImport(JTextComponent target, Type type) {
                 String importName = type.toString();
                 if (!type.isPrimitive() && !importName.equals("void") && !importName.equals("Void") && importName.contains(".")) {
                     importName = removeBetween("()", importName);
                     importName = removeBetween("<>", importName);
                     Matcher symbolMatcher = Pattern.compile("[\\[\\]!@#$%^&*(){}|:'?/<>~`,]").matcher(importName);
                     if (symbolMatcher.find()) {
                         importName = symbolMatcher.replaceAll("").trim();
                     }
                     if (importName.contains(".") && !importName.equals("java.lang.Object")) {
                         Imports.addImport(target, importName);
                     }
                 }
             }
 
             private String createMethod(MethodSymbol methodSymbol) {
                 //StringBuilder method = new StringBuilder("\n");
                 StringBuilder method = new StringBuilder();
                 method.append("\n\toverride ");
                 for (Modifier modifier : methodSymbol.getModifiers()) {
                     switch (modifier) {
                         case PUBLIC:
                             method.append("public ");
                             break;
                         case PROTECTED:
                             method.append("protected ");
                             break;
                     }
                 }
                 method.append("function ").append(methodSymbol.getQualifiedName() + " (");
                 if (methodSymbol.getParameters() != null) {
                     Iterator<VarSymbol> iterator = methodSymbol.getParameters().iterator();
                     while (iterator.hasNext()) {
                         VarSymbol var = iterator.next();
                         String varType = getTypeString(var.asType());
                         method.append(var.getSimpleName()).append(" : ").append(HintsUtils.getClassSimpleName(varType));
                         if (iterator.hasNext()) {
                             method.append(", ");
                         }
                     }
                 }
                 String returnType = getTypeString(methodSymbol.getReturnType());
                 if (returnType.equals("void")) {
                     returnType = "Void";
                 } else {
                     returnType = HintsUtils.getClassSimpleName(returnType);
                 }
                 method.append(")").append(" : ").append(returnType).append(" { \n");
                 method.append("\t\tthrow new UnsupportedOperationException('Not implemented yet');\n");
                 method.append("\t}\n");
 
                 return method.toString();
             }
         };
         ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, "Implement all abstract methods", Collections.singletonList(fix), file, hint.getStartPosition(), hint.getStartPosition());
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
             if (varType.equals("int")) {
                 varType = Integer.class.getSimpleName();
             } else if (varType.equals("long")) {
                 varType = Long.class.getSimpleName();
             } else if (varType.equals("byte")) {
                 varType = Byte.class.getSimpleName();
             } else if (varType.equals("short")) {
                 varType = Short.class.getSimpleName();
             } else if (varType.equals("float")) {
                 varType = Float.class.getSimpleName();
             } else if (varType.equals("double")) {
                 varType = Double.class.getSimpleName();
             } else if (varType.equals("boolean")) {
                 varType = Boolean.class.getSimpleName();
             } else if (varType.equals("char")) {
                 varType = Character.class.getSimpleName();
             }
         }
         if (varType.equals("E") || varType.equals("T")) {
             varType = "Object";
         }
         if (varType.equals("E[]") || varType.equals("T[]")) {
             varType = "Object[]";
         }
         varType = removeBetween("<>", varType);
         return varType;
     }
 
     private int findPositionAtTheEnd(CompilationInfo compilationInfo, Tree tree) {
         SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
         int end = (int) sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), tree);
         return end;
     }
 
     private static class ParamsComparator implements Comparator<List<VarSymbol>> {
 
         public int compare(List<VarSymbol> methodList, List<VarSymbol> overridenMethod) {
             for (VarSymbol var : methodList) {
                 VarSymbol overridenVar = overridenMethod.get(methodList.indexOf(var));
                 if (!var.asType().toString().equals(overridenVar.asType().toString())) {
                     return -1;
                 }
             }
             return 0;
         }
     }
 
     //TODO Temporary log for issue 148890
     private Collection<? extends Element> getAllMembers(TypeElement typeElement, CompilationInfo compilationInfo) {
         Collection<? extends Element> elements = null;
         try {
             elements = compilationInfo.getElements().getAllMembers(typeElement);
         } catch (NullPointerException npe) {
             npe.printStackTrace();
             System.err.println("* e = " + typeElement);
             System.err.println("* e.getKind() = " + typeElement.getKind());
             System.err.println("* e.asType() = " + typeElement.asType());
         }
         return elements;
     }
 }
