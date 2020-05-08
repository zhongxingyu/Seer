 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
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
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
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
 import com.sun.tools.mjavac.code.Symbol.ClassSymbol;
 import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
 import com.sun.tools.mjavac.code.Type;
 import javax.lang.model.element.ExecutableElement;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.swing.text.*;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.openide.filesystems.FileObject;
 import org.openide.text.Annotation;
 import org.openide.text.NbDocument;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class MarkOverriddenTaskFactory extends JavaFXAbstractEditorHint {
 
     static final String ANNOTATION_TYPE = "org.netbeans.modules.javafx.editor.hints"; //NOI18N
     private Collection<OverriddenAnnotation> annotations = new HashSet<OverriddenAnnotation>();
     private final AtomicBoolean cancel = new AtomicBoolean();
 
     public MarkOverriddenTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         final Collection<OverriddenAnnotation> addedAnotations = new HashSet<OverriddenAnnotation>();
         final Map<MethodSymbol, Integer> positions = new LinkedHashMap<MethodSymbol, Integer>();
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             @SuppressWarnings("element-type-mismatch") //NOI18N
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final Map<Element, List<MethodSymbol>> overriddenMethods = new LinkedHashMap<Element, List<MethodSymbol>>();
                 final JavaFXTreePathScanner<Void, Void> visitor = new OverrideVisitor(compilationInfo, overriddenMethods, positions);
                 visitor.scan(compilationInfo.getCompilationUnit(), null);
                 final Map<Element, List<MethodSymbol>> om = new LinkedHashMap<Element, List<MethodSymbol>>(overriddenMethods);
                 for (Element currentClass : om.keySet()) {
                     if (om.get(currentClass) == null || om.get(currentClass).isEmpty()) {
                         continue;
                     }
                     ClassSymbol classSymbol = (ClassSymbol) currentClass;
                     HashMap<ClassSymbol, Collection<? extends Element>> allElements = new HashMap<ClassSymbol, Collection<? extends Element>>();
                     for (Type classType : classSymbol.getInterfaces()) {
                         if (classType.asElement() instanceof ClassSymbol) {
                             ClassSymbol cs = ((ClassSymbol) classType.asElement());
                             allElements.put(cs, FXSourceUtils.getAllMembers(compilationInfo.getElements(), (TypeElement) cs));
                         }
                     }
                     for (Element element : om.get(currentClass)) {
                         if (element.getModifiers().contains(Modifier.STATIC)) {
                             continue;
                         }
                         if (element instanceof MethodSymbol) {
                             String fqnString = null;
                             for (ClassSymbol cs : allElements.keySet()) {
                                 Collection<? extends Element> list = allElements.get(cs);
                                 for (Element e : list) {
                                    if (e instanceof MethodSymbol && compilationInfo.getElements().overrides((ExecutableElement) element, (ExecutableElement) e, classSymbol)) {
                                         fqnString = cs.getQualifiedName().toString();
                                         break;
                                     }
                                 }
                             }
                             if (fqnString != null) {
                                 int start = positions.get(element);
                                 boolean isInErrorZone = false;
                                 for (Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                                     if (diagnostic.getStartPosition() <= start && diagnostic.getEndPosition() >= start) {
                                         isInErrorZone = true;
                                     }
                                 }
                                 if (start > 0 && !isInErrorZone) {
                                     addedAnotations.add(new OverriddenAnnotation(start, fqnString));
                                 }
                             }
                             fqnString = null;
                         }
                     }
 
                 }
                 updateAnnotationsOverridden(compilationInfo, addedAnotations);
                 addedAnotations.clear();
                 positions.clear();
             }
 
             private void updateAnnotationsOverridden(CompilationInfo compilationInfo, Collection<OverriddenAnnotation> addedAnnotations) {
                 final StyledDocument document = (StyledDocument) compilationInfo.getDocument();
                 final Collection<OverriddenAnnotation> annotationsToRemoveCopy = new HashSet<OverriddenAnnotation>(annotations);
                 final Collection<OverriddenAnnotation> addedAnnotationsCopy = new HashSet<OverriddenAnnotation>(addedAnnotations);
 
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
                         for (OverriddenAnnotation annotation : addedAnnotationsCopy) {
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
                         annotations = new HashSet<OverriddenAnnotation>(addedAnnotationsCopy);
                     }
                 };
                 HintsUtils.runInAWT(update);
             }
         };
     }
 
     private static class OverriddenAnnotation extends Annotation {
 
         private int positon;
         private String superClassFQN;
 
         public OverriddenAnnotation(int position, String superClassFQN) {
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
 
         private int getPosition() {
             return positon;
         }
     }
 
     Collection<? extends Annotation> getAnnotations() {
         return Collections.unmodifiableCollection(annotations);
     }
 }
