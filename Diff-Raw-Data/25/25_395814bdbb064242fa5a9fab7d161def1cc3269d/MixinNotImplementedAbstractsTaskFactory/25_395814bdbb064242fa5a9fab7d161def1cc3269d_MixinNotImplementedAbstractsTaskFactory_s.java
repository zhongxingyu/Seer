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
 import com.sun.tools.javafx.code.JavafxClassSymbol;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.ElementUtilities;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.swing.text.Document;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.spi.editor.hints.ErrorDescription;
 import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
 import org.netbeans.spi.editor.hints.HintsController;
 import org.netbeans.spi.editor.hints.Severity;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class MixinNotImplementedAbstractsTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private static final String HINTS_IDENT = "abstractmixinjavafx"; //NOI18N
     private final AtomicBoolean cancel = new AtomicBoolean();
 
     public MixinNotImplementedAbstractsTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         final Collection<Tree> mixins = new HashSet<Tree>();
         final Collection<ExecutableElement> existingMethods = new HashSet<ExecutableElement>();
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final Element[] mainClassElement = new Element[1];
                 final Document document = compilationInfo.getDocument();
 
                 JavaFXTreePathScanner<Void, Void> visitor = new JavaFXTreePathScanner<Void, Void>() {
 
                     @Override
                     public Void visitClassDeclaration(ClassDeclarationTree node, Void v) {
                         try {
                             if (node.getMixins() != null) {
                                 mixins.addAll(node.getMixins());
                             }
                         } catch (NullPointerException npe) {
                             npe.printStackTrace();
                         }
                         mainClassElement[0] = compilationInfo.getTrees().getElement(getCurrentPath());
                        
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
                 if (mixins.isEmpty() || HintsUtils.checkString(mainClassElement[0].getSimpleName().toString())) {
                     if (document != null) {
                         HintsController.setErrors(document, HINTS_IDENT, Collections.EMPTY_LIST);
                     }
                     clear();
                     return;
                 }
                 Collection<ErrorDescription> errorDescriptions = new HashSet<ErrorDescription>();
                 for (Tree mixin : mixins) {
                     if (cancel.get()) {
                         clear();
                         return;
                     }
                     JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), mixin);
                     Element mixinElement = null;
                     try {
                         mixinElement = compilationInfo.getTrees().getElement(path);
                     } catch (NullPointerException npe) {
                         npe.printStackTrace();
                         continue;
                     }
                     if (mixinElement == null || !(mixinElement instanceof JavafxClassSymbol)) {
                         continue;
                     }
                     JavafxClassSymbol classSymbol = (JavafxClassSymbol) mixinElement;
                     Map<Element, Boolean> toOverrides = new HashMap<Element, Boolean>();
                     for (Element element : classSymbol.getEnclosedElements()) {
                         if (element instanceof ExecutableElement && element.getModifiers().contains(Modifier.ABSTRACT)) {
                             boolean isOverride = checkIfOveridden(compilationInfo, existingMethods, (ExecutableElement) element);
                             toOverrides.put(element, isOverride);
                         }
                         if (toOverrides.values().contains(Boolean.FALSE)) {
                             SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                             final int start = (int) sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), mixin);
                             String hintText = NbBundle.getMessage(MixinNotImplementedAbstractsTaskFactory.class, "TITLE_MIXIN_ABSTRACT"); //NOI18N
                             ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, hintText, compilationInfo.getFileObject(), start, start);
                             errorDescriptions.add(errorDescription);
                             break;
                         }
                     }
                 }
                 if (document != null) {
                     HintsController.setErrors(document, HINTS_IDENT, errorDescriptions);
                 }
                 clear();
             }
 
             private void clear() {
                 mixins.clear();
                 existingMethods.clear();
             }
         };
     }
 
     private boolean checkIfOveridden(CompilationInfo compilationInfo, Collection<ExecutableElement> elementsToCheck, ExecutableElement overridden) {
         for (ExecutableElement override : elementsToCheck) {
            TypeElement type = ElementUtilities.enclosingTypeElement(overridden);
            if (compilationInfo.getElements().overrides(override, overridden, type)) {
                return true;
             }
         }
         return false;
     }

 }
