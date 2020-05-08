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
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import javax.swing.text.BadLocationException;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.ElementUtilities;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import java.util.*;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 import javax.swing.SwingUtilities;
 import javax.swing.text.Document;
 import javax.swing.text.Position;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.openide.filesystems.FileObject;
 import org.openide.text.Annotation;
 import org.openide.text.NbDocument;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author karol harezlak
  */
 public class OverridenTaskFactory extends EditorAwareJavaSourceTaskFactory {
 
     private static final EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES);
     private static final String ANNOTATION_TYPE = "org.netbeans.modules.javafx.editor.hints"; //NOI18N
     private final Map<Document, Collection<OverriddeAnnotation>> annotations = new WeakHashMap<Document, Collection<OverriddeAnnotation>>();
 
     public OverridenTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     private void updateAnnotationsOverriden(CompilationInfo compilationInfo, Collection<OverriddeAnnotation> addedAnnotations) {
         final StyledDocument document = (StyledDocument) compilationInfo.getDocument();
         final Collection<OverriddeAnnotation> annotationsToRemoveCopy = annotations.get(document) ==  null ? null : new HashSet<OverriddeAnnotation>(annotations.get(document));
         final Collection<OverriddeAnnotation> addedAnnotationsCopy = new HashSet<OverriddeAnnotation>(addedAnnotations);
         
         Runnable update = new Runnable() {
 
             public void run() {
                 System.out.println("START");
                 if (annotationsToRemoveCopy != null) {
                     for (Annotation annotation : annotationsToRemoveCopy) {
                         System.out.println("Annotation removed " + annotation);
                         NbDocument.removeAnnotation(document, annotation);
                     }
                 }
                 for (OverriddeAnnotation annotation : addedAnnotationsCopy) {
                     Position position = null;
                     try {
                         position = document.createPosition(annotation.getPosition());
                     } catch (BadLocationException ex) {
                         Exceptions.printStackTrace(ex);
                     }
                     System.out.println("Annotation added " + annotation);
                     NbDocument.addAnnotation(document, position, annotation.getPosition(), annotation);
                 }
                 System.out.println("END");
             }
         };
         runRunnable(update);
         annotations.put(document, addedAnnotations);
     }
 
     private void runRunnable(Runnable task) {
         if (SwingUtilities.isEventDispatchThread()) {
             task.run();
         } else {
             SwingUtilities.invokeLater(task);
         }
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
 
                 final Element[] mainClassElement = new Element[1];
                 final Collection<Tree> supertypes = new HashSet<Tree>();
                 final Collection<ExecutableElement> existingMethods = new HashSet<ExecutableElement>();
                 final Collection<OverriddeAnnotation> addedAnotations = new HashSet<OverriddeAnnotation>();
 
                 JavaFXTreePathScanner<Void, Void> visitor = new JavaFXTreePathScanner<Void, Void>() {
 
                     @Override
                     public Void visitClassDeclaration(ClassDeclarationTree node, Void v) {
                         mainClassElement[0] = compilationInfo.getTrees().getElement(getCurrentPath());
                         supertypes.addAll(node.getSupertypeList());
                         supertypes.add(node);
                         return super.visitClassDeclaration(node, v);
                     }
 
                     @Override
                     public Void visitFunctionDefinition(FunctionDefinitionTree node, Void v) {
                         Element element = compilationInfo.getTrees().getElement(getCurrentPath());
                         if (element != null && element instanceof ExecutableElement) {
                             if (node.getModifiers().toString().contains(" override")) { //NOI18N
                                 existingMethods.add((ExecutableElement) element);
                             }
                         }
 
                         return super.visitFunctionDefinition(node, v);
                     }
                 };
                 visitor.scan(compilationInfo.getCompilationUnit(), null);
 
                 if (HintsUtils.checkString(mainClassElement[0].getSimpleName().toString())) {
                     return;
                 }
                 ClassIndex classIndex = ClasspathInfo.create(file).getClassIndex();
                 boolean breakIt = false;
                 for (Tree mixin : supertypes) {
                     JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), mixin);
                     Element mixinElement = compilationInfo.getTrees().getElement(path);
                     if (mixinElement == null) {
                         continue;
                     }
                     String mixinName = mixinElement.getSimpleName().toString();
                     if (HintsUtils.checkString(mixinName)) {
                         continue;
                     }
                     Set<ElementHandle<TypeElement>> options = classIndex.getDeclaredTypes(mixinName, ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
                     for (ElementHandle<TypeElement> elementHandle : options) {
                         TypeElement typeElement = elementHandle.resolve(compilationInfo);
                         if (typeElement == null) {
                             continue;
                         }
                         Collection<? extends Element> elements = getAllMembers(typeElement, compilationInfo);
                         if (elements == null) {
                             continue;
                         }
                         Collection<Element> overriddens = new HashSet<Element>();
                         for (Element element : elements) {
                             if (element instanceof ExecutableElement) {
                                 Element overriden = checkIfOveridden(compilationInfo, existingMethods, (ExecutableElement) element);
                                 overriddens.add(overriden);
                             }
                         }
                         for (Element overriden : overriddens) {
                             if (overriden == null) {
                                 continue;
                             }
                             Tree tree = compilationInfo.getTrees().getTree(overriden);
                             SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                             int start = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), tree);
                             addedAnotations.add(new OverriddeAnnotation(start));
                         }
                     }
                     if (breakIt) {
                         break;
                     }
                 }
                 updateAnnotationsOverriden(compilationInfo, addedAnotations);
             }
         };
     }
 
     private Element checkIfOveridden(CompilationInfo compilationInfo, Collection<ExecutableElement> elementsToCheck, ExecutableElement overridden) {
         for (ExecutableElement override : elementsToCheck) {
             TypeElement type = ElementUtilities.enclosingTypeElement(overridden);
             if (compilationInfo.getElements().overrides(override, overridden, type)) {
                 return override;
             }
         }
         return null;
     }
 
     private Collection<? extends Element> getAllMembers(TypeElement typeElement, CompilationInfo compilationInfo) {
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
 
     private static class OverriddeAnnotation extends Annotation {
 
         private int positon;
 
         public OverriddeAnnotation(int position) {
             this.positon = position;
         }
 
         @Override
         public String getAnnotationType() {
             return ANNOTATION_TYPE;
         }
 
         @Override
         public String getShortDescription() {
             return "Overriden";
         }
 
         int getPosition() {
             return positon;
         }
     }
 }
