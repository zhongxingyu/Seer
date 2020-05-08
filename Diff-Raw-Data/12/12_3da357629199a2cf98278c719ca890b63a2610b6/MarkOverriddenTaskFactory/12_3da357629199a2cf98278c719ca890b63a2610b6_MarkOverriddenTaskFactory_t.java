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
 
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.mjavac.code.Symbol;
 import com.sun.tools.mjavac.code.Symbol.ClassSymbol;
 import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
 import com.sun.tools.mjavac.code.Type;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.lang.model.element.Element;
 import javax.swing.SwingUtilities;
 import javax.swing.text.*;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.openide.filesystems.FileObject;
 import org.openide.text.Annotation;
 import org.openide.text.NbDocument;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class MarkOverriddenTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private static final String ANNOTATION_TYPE = "org.netbeans.modules.javafx.editor.hints"; //NOI18N
     private final Map<Document, Collection<OverriddeAnnotation>> annotations = new WeakHashMap<Document, Collection<OverriddeAnnotation>>();
     private final AtomicBoolean cancel = new AtomicBoolean();
 
     public MarkOverriddenTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
         final Collection<Element> classes = new HashSet<Element>();
         final Map<Element, List<MethodSymbol>> overriddenMethods = new HashMap<Element, List<MethodSymbol>>();
         final Collection<OverriddeAnnotation> addedAnotations = new HashSet<OverriddeAnnotation>();
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             @SuppressWarnings("element-type-mismatch") //NOI18N
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final JavaFXTreePathScanner<Void, Void> visitor = new OverrideVisitor(compilationInfo, classes, overriddenMethods);
                 visitor.scan(compilationInfo.getCompilationUnit(), null);
                 SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                 for (Element currentClass : classes) {
                     if (overriddenMethods.get(currentClass) == null || overriddenMethods.get(currentClass).isEmpty()) {
                         continue;
                     }
                     ClassSymbol classSymbol = (ClassSymbol) currentClass;
                     for (Element element : overriddenMethods.get(currentClass)) {
                         if (element instanceof MethodSymbol) {
                             String fqnString = null;
                             if (classSymbol.getInterfaces().size() == 1) {
                                 fqnString = classSymbol.getInterfaces().iterator().next().toString();
                             } else {
                                 for (Type classType : classSymbol.getInterfaces()) {
                                    fqnString = getOwnerClassName(classType.asElement().enclClass(), (MethodSymbol) element);
                                     if (fqnString != null) {
                                         break;
                                     }
                                 }
                             }
                             if (fqnString != null) {
                                 Tree tree = compilationInfo.getTrees().getTree(element);
                                 int start = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), tree);
                                 boolean isInErrorZone = false;
                                 for (Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                                     if (diagnostic.getStartPosition() <= start && diagnostic.getEndPosition() >= start) {
                                         isInErrorZone = true;
                                     }
                                 }
                                 if (start > 0 && !isInErrorZone) {
                                     addedAnotations.add(new OverriddeAnnotation(start, fqnString));
                                 }
                             }
                         }
                     }
                 }
                 updateAnnotationsOverridden(compilationInfo, addedAnotations);
                 clear();
             }
 
            private String getOwnerClassName(ClassSymbol classSymbol, MethodSymbol methodSymbol) {
                 String fqnType = null;
                if (classSymbol == null || classSymbol.getEnclosedElements() == null) {
                    return null;
                }
                 for (Symbol symbol : classSymbol.getEnclosedElements()) {
                     if (symbol instanceof MethodSymbol) {
                         MethodSymbol method = (MethodSymbol) symbol;
                         if (!method.name.toString().equals(methodSymbol.name.toString())) {
                             continue;
                         }
                         if (method.getReturnType() != methodSymbol.getReturnType()) {
                             continue;
                         }
                         if (method.getParameters().size() != methodSymbol.getParameters().size()) {
                             continue;
                         }
 
                         return classSymbol.className();
                     }
                 }
 
                 if (fqnType != null) {
                     return fqnType;
                 }
 
                 for (Type classType : classSymbol.getInterfaces()) {
                    fqnType = getOwnerClassName(classType.asElement().enclClass(), methodSymbol);
                 }
 
                 return fqnType;
             }
 
             private void updateAnnotationsOverridden(CompilationInfo compilationInfo, Collection<OverriddeAnnotation> addedAnnotations) {
                 final StyledDocument document = (StyledDocument) compilationInfo.getDocument();
                 final Collection<OverriddeAnnotation> annotationsToRemoveCopy = annotations.get(document) == null ? null : new HashSet<OverriddeAnnotation>(annotations.get(document));
                 final Collection<OverriddeAnnotation> addedAnnotationsCopy = new HashSet<OverriddeAnnotation>(addedAnnotations);
 
                 Runnable update = new Runnable() {
 
                     public void run() {
                         if (document == null) {
                             return;
                         }
                         if (annotationsToRemoveCopy != null) {
                             for (Annotation annotation : annotationsToRemoveCopy) {
                                 NbDocument.removeAnnotation(document, annotation);
                             }
                         }
                         for (OverriddeAnnotation annotation : addedAnnotationsCopy) {
                             Position position = null;
                             try {
                                 position = document.createPosition(annotation.getPosition());
                             } catch (BadLocationException ex) {
                                 ex.printStackTrace();
                             }
                             if (document != null && position != null) {
                                 NbDocument.addAnnotation(document, position, annotation.getPosition(), annotation);
                             }
                         }
                         annotations.put(document, addedAnnotationsCopy);
                     }
                 };
                 runRunnable(update);
             }
 
             private void runRunnable(Runnable task) {
                 if (SwingUtilities.isEventDispatchThread()) {
                     task.run();
                 } else {
                     SwingUtilities.invokeLater(task);
                 }
             }
 
             private void clear() {
                 addedAnotations.clear();
                 classes.clear();
                 overriddenMethods.clear();
             }
         };
     }
 
     private static class OverriddeAnnotation extends Annotation {
 
         private int positon;
         private String superClassFQN;
 
         public OverriddeAnnotation(int position, String superClassFQN) {
             this.positon = position;
             this.superClassFQN = superClassFQN;
         }
 
         @Override
         public String getAnnotationType() {
             return ANNOTATION_TYPE;
         }
 
         @Override
         public String getShortDescription() {
             return NbBundle.getMessage(MarkOverriddenTaskFactory.class, "LABEL_OVERRIDES") + superClassFQN; //NOI18N
         }
 
         int getPosition() {
             return positon;
         }
     }
 }
