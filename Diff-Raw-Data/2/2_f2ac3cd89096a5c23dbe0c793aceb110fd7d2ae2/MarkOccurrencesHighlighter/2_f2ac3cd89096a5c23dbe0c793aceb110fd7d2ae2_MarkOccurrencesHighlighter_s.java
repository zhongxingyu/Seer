 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 package org.netbeans.modules.javafx.editor.semantic;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import org.netbeans.api.editor.settings.AttributesUtilities;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
 import org.netbeans.modules.javafx.editor.options.MarkOccurencesSettings;
 import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 import javax.lang.model.element.Element;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.Document;
 import javax.swing.text.StyleConstants;
 import java.awt.*;
 import java.io.IOException;
 import java.util.*;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.Preferences;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.ElementFilter;
 
 /**
  *
  * @author Jan Lahoda, Anton Chechel
  */
 public class MarkOccurrencesHighlighter implements CancellableTask<CompilationInfo> {
 
     public static final Color ES_COLOR = new Color(175, 172, 102); // new Color(244, 164, 113);
 
     private FileObject file;
 
     MarkOccurrencesHighlighter(FileObject file) {
         this.file = file;
     }
 
     public void run(CompilationInfo info) throws IOException {
         resume();
 
         Document doc = info.getDocument();
         if (doc == null) {
             Logger.getLogger(MarkOccurrencesHighlighter.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!"); // NOI18N
             return;
         }
 
         Preferences pref = MarkOccurencesSettings.getCurrentNode();
         if (!pref.getBoolean(MarkOccurencesSettings.ON_OFF, true)) {
             getHighlightsBag(doc).clear();
             OccurrencesMarkProvider.get(doc).setOccurrences(Collections.<Mark>emptySet());
             return;
         }
 
         int caretPosition = MarkOccurrencesHighlighterFactory.getLastPosition(file);
         if (isCancelled()) {
             return;
         }
 
         List<int[]> bag = processImpl(info, pref, doc, caretPosition);
         if (isCancelled()) {
             return;
         }
 
         if (bag == null) {
             if (pref.getBoolean(MarkOccurencesSettings.KEEP_MARKS, true)) {
                 return;
             }
             bag = new ArrayList<int[]>();
         }
 
         Collections.sort(bag, new Comparator<int[]>() {
             public int compare(int[] o1, int[] o2) {
                 return o1[0] - o2[0];
             }
         });
 
         Iterator<int[]> it = bag.iterator();
         int[] last = it.hasNext() ? it.next() : null;
         List<int[]> result = new ArrayList<int[]>(bag.size());
 
         while (it.hasNext()) {
             int[] current = it.next();
 
             if (current[0] < last[1]) {
                 //merge the highlights:
                 last[1] = Math.max(current[1], last[1]);
             } else {
                 result.add(last);
                 last = current;
             }
         }
 
         if (last != null) {
             result.add(last);
         }
 
         OffsetsBag obag = new OffsetsBag(doc);
         obag.clear();
 
         AttributeSet attributes = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(236, 235, 163));
 
         for (int[] span : result) {
             int convertedStart = span[0];
             int convertedEnd = span[1];
 
             if (convertedStart != (-1) && convertedEnd != (-1)) {
                 obag.addHighlight(convertedStart, convertedEnd, attributes);
             }
         }
 
         getHighlightsBag(doc).setHighlights(obag);
         OccurrencesMarkProvider.get(doc).setOccurrences(OccurrencesMarkProvider.createMarks(doc, bag, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP"))); // NOI18N
     }
 
     private boolean isIn(UnitTree cu, SourcePositions sp, Tree tree, int position) {
         return sp.getStartPosition(cu, tree) <= position && position <= sp.getEndPosition(cu, tree);
     }
 
     private boolean isIn(int caretPosition, Token<?> span) {
         if (span == null) {
             return false;
         }
 
         return span.offset(null) <= caretPosition && caretPosition <= span.offset(null) + span.length();
     }
 
     List<int[]> processImpl(CompilationInfo info, Preferences pref, Document doc, int caretPosition) {
         UnitTree cu = info.getCompilationUnit();
 //        TreePath tp = info.getTreeUtilities().pathFor(caretPosition);
         TreeUtilities tu = TreeUtilities.create(info);
         JavaFXTreePath tp = tu.pathFor(caretPosition);
         JavaFXTreePath typePath = findTypePath(tp);
 
         if (isCancelled()) {
             return null;
         }
 
         //detect caret inside the return type or throws clause:
         // manowar: a bit of FX magic
         if (typePath != null) {
             JavaFXTreePath pTypePath = typePath.getParentPath();
             if (pTypePath != null) {
                 JavaFXTreePath gpTypePath = pTypePath.getParentPath();
                 if (gpTypePath != null) {
                     JavaFXTreePath ggpTypePath = gpTypePath.getParentPath();
                     if (getJFXKind(ggpTypePath) == JavaFXKind.FUNCTION_DEFINITION &&
                             getJFXKind(gpTypePath) == JavaFXKind.FUNCTION_VALUE &&
                             getJFXKind(pTypePath) == JavaFXKind.TYPE_CLASS &&
                             getJFXKind(typePath) == JavaFXKind.IDENTIFIER) {
 
                         JFXFunctionDefinition decl = (JFXFunctionDefinition) ggpTypePath.getLeaf();
                         Tree type = decl.getJFXReturnType();
 
                         if (pref.getBoolean(MarkOccurencesSettings.EXIT, true) && isIn(cu, info.getTrees().getSourcePositions(), type, caretPosition)) {
                             MethodExitDetector med = new MethodExitDetector();
                             setExitDetector(med);
                             try {
                                 return med.process(info, doc, decl, null);
                             } finally {
                                 setExitDetector(null);
                             }
                         }
 
                         if (pref.getBoolean(MarkOccurencesSettings.EXCEPTIONS, true)) {
                             for (Tree exc : decl.getErrorTrees()) {
                                 if (isIn(cu, info.getTrees().getSourcePositions(), exc, caretPosition)) {
                                     MethodExitDetector med = new MethodExitDetector();
                                     setExitDetector(med);
                                     try {
                                         return med.process(info, doc, decl, Collections.singletonList(exc));
                                     } finally {
                                         setExitDetector(null);
                                     }
                                 }
                             }
                         }
 
                     }
                 }
             }
         }
 
         if (isCancelled()) {
             return null;
         }
 
         // extends/implements clause
         if (pref.getBoolean(MarkOccurencesSettings.IMPLEMENTS, true)) {
             if (typePath != null && getJFXKind(typePath) == JavaFXKind.TYPE_CLASS) {
                 boolean isExtends = true;
                 boolean isImplements = false;
 //                boolean isExtends = ctree.getExtendsClause() == typePath.getLeaf();
 //                boolean isImplements = false;
 //
 //                for (Tree t : ctree.getImplementsClause()) {
 //                    if (t == typePath.getLeaf()) {
 //                        isImplements = true;
 //                        break;
 //                    }
 //                }
 
                 if ((isExtends && pref.getBoolean(MarkOccurencesSettings.OVERRIDES, true)) ||
                         (isImplements && pref.getBoolean(MarkOccurencesSettings.IMPLEMENTS, true))) {
                     Element superType = info.getTrees().getElement(typePath);
                     Element thisType  = info.getTrees().getElement(typePath.getParentPath());
 
                     if (isClass(superType) && isClass(thisType))
                         return detectMethodsForClass(info, doc, typePath.getParentPath(), (TypeElement) superType, (TypeElement) thisType);
                 }
             }
 
             if (isCancelled())
                 return null;
 
             TokenSequence<JFXTokenId> ts = info.getTokenHierarchy().tokenSequence(JFXTokenId.language());
 
            if (ts != null && tp.getLeaf().getJavaFXKind() == JavaFXKind.CLASS_DECLARATION) {
                 int bodyStart = Utilities.findBodyStart(tp.getLeaf(), cu, info.getTrees().getSourcePositions(), doc);
 
                 if (caretPosition < bodyStart) {
                     ts.move(caretPosition);
 
                     if (ts.moveNext()) {
                         if (pref.getBoolean(MarkOccurencesSettings.OVERRIDES, true) && ts.token().id() == JFXTokenId.EXTENDS) {
 //                            Tree superClass = ((ClassTree) tp.getLeaf()).getExtendsClause();
                             Tree superClass = typePath.getParentPath() != null ? (JFXClassDeclaration) typePath.getParentPath().getLeaf() : null;
 
                             if (superClass != null) {
                                 Element superType = info.getTrees().getElement(new JavaFXTreePath(tp, superClass));
                                 Element thisType  = info.getTrees().getElement(tp);
 
                                 if (isClass(superType) && isClass(thisType))
                                     return detectMethodsForClass(info, doc, tp, (TypeElement) superType, (TypeElement) thisType);
                             }
                         }
 
 //                        if (pref.getBoolean(MarkOccurencesSettings.IMPLEMENTS, true) && ts.token().id() == JFXTokenId.IMPLEMENTS) {
 //                            List<? extends Tree> superClasses = ((ClassTree) tp.getLeaf()).getImplementsClause();
 //
 //                            if (superClasses != null) {
 //                                List<TypeElement> superTypes = new ArrayList<TypeElement>();
 //
 //                                for (Tree superTypeTree : superClasses) {
 //                                    if (superTypeTree != null) {
 //                                        Element superType = info.getTrees().getElement(new JavaFXTreePath(tp, superTypeTree));
 //
 //                                        if (isClass(superType))
 //                                            superTypes.add((TypeElement) superType);
 //                                    }
 //                                }
 //
 //                                Element thisType  = info.getTrees().getElement(tp);
 //
 //                                if (!superTypes.isEmpty() && isClass(thisType))
 //                                    return detectMethodsForClass(info, doc, tp, superTypes, (TypeElement) thisType);
 //                            }
 //
 //                        }
                     }
                 }
             }
         }
 
         if (isCancelled()) {
             return null;
         }
 
         Tree tree = tp.getLeaf();
         if (pref.getBoolean(MarkOccurencesSettings.BREAK_CONTINUE, true) &&
                 (tree.getJavaFXKind() == JavaFXKind.BREAK || tree.getJavaFXKind() == JavaFXKind.CONTINUE)) {
             return detectBreakOrContinueTarget(info, doc, tp);
         }
 
         if (isCancelled()) {
             return null;
         }
 
         // TODO inside javadoc
         // variable declaration
         Element el = info.getTrees().getElement(tp);
 
         if (el != null && !Utilities.isKeyword(tree) && isEnabled(pref, el) &&
                 (tree.getJavaFXKind() != JavaFXKind.CLASS_DECLARATION || isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, tp))) &&
                 (tree.getJavaFXKind() != JavaFXKind.FUNCTION_DEFINITION || isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, tp)))) {
 
             FindLocalUsagesQuery fluq = new FindLocalUsagesQuery();
             setLocalUsages(fluq);
             try {
                 List<int[]> bag = new ArrayList<int[]>();
                 for (Token<?> t : fluq.findUsages(el, info, doc)) {
                     bag.add(new int[]{t.offset(null), t.offset(null) + t.length()});
                 }
 
                 return bag;
             } finally {
                 setLocalUsages(null);
             }
         }
 
         return null;
     }
     
     private List<int[]> detectMethodsForClass(CompilationInfo info, Document document, JavaFXTreePath clazz, TypeElement superType, TypeElement thisType) {
         return detectMethodsForClass(info, document, clazz, Collections.singletonList(superType), thisType);
     }
 
     private List<int[]> detectMethodsForClass(CompilationInfo info, Document document, JavaFXTreePath clazz, List<TypeElement> superTypes, TypeElement thisType) {
         List<int[]> highlights = new ArrayList<int[]>();
         JFXClassDeclaration clazzTree = (JFXClassDeclaration) clazz.getLeaf();
         TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
 
         OUTER: for (Tree member: clazzTree.getMembers()) {
             if (isCancelled()) {
                 return null;
             }
 
             if (member.getJavaFXKind() == JavaFXKind.FUNCTION_DEFINITION) {
                 JavaFXTreePath path = new JavaFXTreePath(clazz, member);
                 Element el = info.getTrees().getElement(path);
 
                 if (el.getKind() == ElementKind.METHOD) {
                     for (TypeElement superType : superTypes) {
                         for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(superType))) {
                             if (info.getElements().overrides((ExecutableElement) el, ee, thisType) && (superType.getKind().isClass() || !ee.getEnclosingElement().equals(jlObject))) {
                                 Token t = Utilities.getToken(info, document, path);
                                 if (t != null) {
                                     highlights.add(new int[] {t.offset(null), t.offset(null) + t.length()});
                                 }
                                 continue OUTER;
                             }
                         }
                     }
                 }
             }
         }
 
         return highlights;
     }
 
     private static final Set<JavaFXKind> TYPE_PATH_ELEMENT = EnumSet.of(JavaFXKind.IDENTIFIER, JavaFXKind.MEMBER_SELECT);
 
     private static JavaFXTreePath findTypePath(JavaFXTreePath tp) {
         if (!TYPE_PATH_ELEMENT.contains(tp.getLeaf().getJavaFXKind())) {
             return null;
         }
 
         while (TYPE_PATH_ELEMENT.contains(tp.getParentPath().getLeaf().getJavaFXKind())) {
             tp = tp.getParentPath();
         }
 
         return tp;
     }
 
     private static boolean isClass(Element el) {
         return el != null && (el.getKind().isClass() || el.getKind().isInterface());
     }
     
     private static boolean isEnabled(Preferences pref, Element el) {
         switch (el.getKind()) {
             case ANNOTATION_TYPE:
             case CLASS:
             case ENUM:
             case INTERFACE:
             case TYPE_PARAMETER:
                 return pref.getBoolean(MarkOccurencesSettings.TYPES, true);
             case CONSTRUCTOR:
             case METHOD:
                 return pref.getBoolean(MarkOccurencesSettings.METHODS, true);
             case ENUM_CONSTANT:
                 return pref.getBoolean(MarkOccurencesSettings.CONSTANTS, true);
             case FIELD:
                 if (el.getModifiers().containsAll(EnumSet.of(Modifier.STATIC, Modifier.FINAL))) {
                     return pref.getBoolean(MarkOccurencesSettings.CONSTANTS, true);
                 } else {
                     return pref.getBoolean(MarkOccurencesSettings.FIELDS, true);
                 }
             case LOCAL_VARIABLE:
             case PARAMETER:
             case EXCEPTION_PARAMETER:
                 return pref.getBoolean(MarkOccurencesSettings.LOCAL_VARIABLES, true);
             case PACKAGE:
                 return false; // never mark occurrence packages
             default:
                 Logger.getLogger(MarkOccurrencesHighlighter.class.getName()).log(Level.INFO, "Unknow element type: {0}.", el.getKind());
                 return true;
         }
     }
 
     private boolean canceled;
     private MethodExitDetector exitDetector;
     private FindLocalUsagesQuery localUsages;
 
     private final synchronized void setExitDetector(MethodExitDetector detector) {
         this.exitDetector = detector;
     }
 
     private final synchronized void setLocalUsages(FindLocalUsagesQuery localUsages) {
         this.localUsages = localUsages;
     }
 
     public final synchronized void cancel() {
         canceled = true;
 
         if (exitDetector != null) {
             exitDetector.cancel();
         }
         if (localUsages != null) {
             localUsages.cancel();
         }
     }
 
     protected final synchronized boolean isCancelled() {
         return canceled;
     }
 
     protected final synchronized void resume() {
         canceled = false;
     }
 
     private List<int[]> detectBreakOrContinueTarget(CompilationInfo info, Document document, JavaFXTreePath breakOrContinue) {
         List<int[]> result = new ArrayList<int[]>();
         ExpressionTree target = TreeUtilities.create(info).getBreakContinueTarget(breakOrContinue);
 
         if (target == null) {
             return null;
         }
 
         TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>) info.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         ts.move((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), target));
         if (ts.moveNext()) {
             result.add(new int[]{ts.offset(), ts.offset() + ts.token().length()});
         }
 
         ExpressionTree statement = target;
         Tree block = null;
 
         switch (statement.getJavaFXKind()) {
             case WHILE_LOOP:
                 if (((WhileLoopTree) statement).getStatement().getJavaFXKind() == JavaFXKind.BLOCK_EXPRESSION) {
                     block = ((WhileLoopTree) statement).getStatement();
                 }
                 break;
             case FOR_EXPRESSION_FOR:
                 if (((ForExpressionTree) statement).getBodyExpression().getJavaFXKind() == JavaFXKind.BLOCK_EXPRESSION) {
                     block = ((ForExpressionTree) statement).getBodyExpression();
                 }
                 break;
         }
 
         if (block != null) {
             ts.move((int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), block));
 
             if (ts.movePrevious() && ts.token().id() == JFXTokenId.RBRACE) {
                 result.add(new int[]{ts.offset(), ts.offset() + ts.token().length()});
             }
         }
 
         return result;
     }
 
     static OffsetsBag getHighlightsBag(Document doc) {
         OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrencesHighlighter.class);
 
         if (bag == null) {
             doc.putProperty(MarkOccurrencesHighlighter.class, bag = new OffsetsBag(doc, false));
 
             final OffsetsBag bagFin = bag;
             DocumentListener l = new DocumentListener() {
                 public void insertUpdate(DocumentEvent e) {
                     bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                 }
                 public void removeUpdate(DocumentEvent e) {
                     bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                 }
                 public void changedUpdate(DocumentEvent e) {
                 }
             };
 
             doc.addDocumentListener(l);
         }
         return bag;
     }
     
     private static JavaFXKind getJFXKind(JavaFXTreePath tp) {
         return (tp == null || tp.getLeaf() == null) ? null : tp.getLeaf().getJavaFXKind();
     }
 
 }
 
