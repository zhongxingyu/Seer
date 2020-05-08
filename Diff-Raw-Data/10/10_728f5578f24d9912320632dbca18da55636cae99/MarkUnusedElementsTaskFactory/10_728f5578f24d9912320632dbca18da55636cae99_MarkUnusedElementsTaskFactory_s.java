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
 package org.netbeans.modules.javafx.editor.semantic;
 
 import com.sun.javafx.api.tree.CatchTree;
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.ForExpressionTree;
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.VariableTree;
 import com.sun.tools.javafx.tree.JFXForExpressionInClause;
 import com.sun.tools.javafx.tree.JFXVar;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.Modifier;
 import javax.swing.SwingUtilities;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.Document;
 import org.netbeans.api.editor.settings.EditorStyleConstants;
 import org.netbeans.api.editor.settings.AttributesUtilities;
 import org.netbeans.api.javafx.editor.Cancellable;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.editor.SafeTokenSequence;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.spi.editor.highlighting.HighlightsSequence;
 import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
 import org.openide.filesystems.FileObject;
 
 /**
  *
  * @author karol harezlak
  */
 public class MarkUnusedElementsTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     //private final static Logger LOG = Logger.getAnonymousLogger();
     private final AtomicBoolean cancel = new AtomicBoolean();
 
     public MarkUnusedElementsTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.LOW);
     }
 
     @Override
     protected CancellableTask<CompilationInfo> createTask(final FileObject file) {
        final Document document = FXSourceUtils.getDocument(file);
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final Map<Element, Tree> elementsToAdd = new HashMap<Element, Tree>();
                 final Map<Tree, String> elementsNames = new HashMap<Tree, String>();
                 final Map<Element, Tree> elementsToRemove = new HashMap<Element, Tree>();
                 //final Map<Tree, Integer> cachedPositions = new HashMap<Tree, Integer>();
 
                 JavaFXTreePathScanner<Void, Void> scanner = new JavaFXTreePathScanner<Void, Void>() {
 
                     boolean parentClass = true;
 
                     @Override
                     public Void visitClassDeclaration(ClassDeclarationTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         if (!parentClass && !isPublic(node.getModifiers().toString())) {
                             addElementToAdd(node);
                         } else {
                             parentClass = false;
                         }
 
                         return super.visitClassDeclaration(node, v);
                     }
 
                     @Override
                     public Void visitInstantiate(InstantiateTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         addElementToRemove(node);
 
                         return super.visitInstantiate(node, v);
                     }
 
                     @Override
                     public Void visitVariable(VariableTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         if (!isPublic(node.getModifiers().toString())) {
                             addElementToAdd(node);
                         }
 
                         return super.visitVariable(node, v);
                     }
 
                     @Override
                     public Void visitIdentifier(IdentifierTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         addElementToRemove(node);
 
                         return super.visitIdentifier(node, v);
                     }
 
                     @Override
                     public Void visitFunctionDefinition(FunctionDefinitionTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         for (Tree tree : node.getFunctionValue().getParameters()) {
                             addElementToAdd(tree);
                         }
                         Element element = compilationInfo.getTrees().getElement(getCurrentPath());
                         Collection<Modifier> modifiers = node.getModifiers().getFlags();
                         if (element != null && element.getSimpleName() != null) {
                             //TODO Hack for package modifiers which does not provide info about package
                             if (!isPackage(node.getModifiers().toString())) {
                                 if (modifiers.isEmpty() || modifiers.size() == 1 && modifiers.iterator().next() == Modifier.STATIC) {
                                     addElementToAdd(node);
                                 }
                             }
                         }
 
                         return super.visitFunctionDefinition(node, v);
                     }
 
                     @Override
                     public Void visitForExpression(ForExpressionTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         for (Tree tree : node.getInClauses()) {
                             if (tree instanceof JFXForExpressionInClause) {
                                 Tree variable = ((JFXForExpressionInClause) tree).getVariable();
                                 addElementToAdd(variable);
                             }
                         }
 
                         return super.visitForExpression(node, v);
                     }
 
                     @Override
                     public Void visitCatch(CatchTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         addElementToAdd(node.getParameter());
 
                         return super.visitCatch(node, v);
                     }
 
                     @Override
                     public Void visitMethodInvocation(FunctionInvocationTree node, Void v) {
                         if (cancel.get()) {
                             return null;
                         }
                         addElementToRemove(node);
 
                         return super.visitMethodInvocation(node, v);
                     }
 
                     private boolean isPublic(String modifiersString) {
                         StringTokenizer tokenizer = new StringTokenizer(modifiersString);
                         while (tokenizer.hasMoreTokens()) {
                             String token = tokenizer.nextToken();
                             if (token.contains("public") || token.contains("protected")) { //NOI18N
                                 return true;
                             }
                         }
 
                         return false;
                     }
 
                     private boolean isPackage(String modifiers) {
                         Pattern pattern = Pattern.compile("package"); //NOI18N
                         Matcher matcher = pattern.matcher(modifiers);
                         if (matcher.find()) {
                             return true;
                         }
 
                         return false;
                     }
 
                     private void addElementToAdd(Tree node) {
                         if (node == null) {
                             return;
                         }
                         JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), node);
                         Element element = compilationInfo.getTrees().getElement(path);
                         if (element == null || element.getSimpleName() == null) {
                             return;
                         }
                         elementsToAdd.put(element, node);
                         elementsNames.put(node, element.getSimpleName().toString());
                     }
 
                     private void addElementToRemove(Tree node) {
                         if (node == null) {
                             return;
                         }
                         JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), node);
                         Element element = compilationInfo.getTrees().getElement(path);
                         elementsToRemove.put(element, node);
                     }
 
                 };
                 scanner.scan(compilationInfo.getCompilationUnit(), null);
                 if (cancel.get()) {
                     return;
                 }
                 for (Element element : elementsToAdd.keySet()) {
                     Tree tree = elementsToAdd.get(element);
                     if (tree instanceof JFXVar && element.getSimpleName() != null) {
                         if (((JFXVar) elementsToAdd.get(element)).isBound()) {
                             elementsToRemove.put(element, tree);
                             elementsNames.put(tree, element.getSimpleName().toString());
                         }
                     }
                 }
                 for (Element element : elementsToRemove.keySet()) {
                     if (element == null) {
                         continue;
                     }
                     if (elementsToAdd.containsKey(element)) {
                         elementsToAdd.remove(element);
                     } else if (element.getSimpleName() != null && elementsNames.values().contains(element.getSimpleName().toString())) {
                         Element toRemvEncElement = element.getEnclosingElement();
                         if (toRemvEncElement == null) {
                             continue;
                         }
                         String toRemvEncName = toRemvEncElement.getSimpleName().toString();
                         String simpleName = element.getSimpleName().toString();
                         Set<Element> toAddCheck = getElementsForSimpleName(simpleName, elementsToAdd.keySet());
 
                         for (Element e : toAddCheck) {
                             Element toAddEncElement = e.getEnclosingElement();
                             if (toAddEncElement == null) {
                                 continue;
                             }
                             String toAddEncName = toAddEncElement.getSimpleName().toString();
                             //FIXME This part of code compare elements based on simple names. Something more reliable then string simple name is need it.
                             if (toAddEncName.contains(toRemvEncName) || toRemvEncName.contains(toAddEncName)) {
                                 elementsToAdd.remove(e);
                             }
                         }
 
                     }
                 }
                 if (elementsToAdd.isEmpty()) {
                     return;
                 }
                 SourcePositions sourcePositions = compilationInfo.getTrees().getSourcePositions();
                 Collection<Position> positions = new HashSet<Position>(elementsToAdd.size());
                 for (Tree tree : elementsToAdd.values()) {
                     long start = sourcePositions.getStartPosition(compilationInfo.getCompilationUnit(), tree);
                     long end = sourcePositions.getEndPosition(compilationInfo.getCompilationUnit(), tree);
                     if (start < 0 || end < 0) {
                         continue;
                     }
                     TreeUtilities treeUtilities = compilationInfo.getTreeUtilities();
                     //TODO this is dumb cancelable, need to be replaced with real one
                     Cancellable cancellable = new Cancellable() {
 
                         public boolean isCancelled() {
                             return false;
                         }
 
                         public void cancell() {
                         }
                     };
                     SafeTokenSequence<JFXTokenId> tokenSequence = new SafeTokenSequence<JFXTokenId>(treeUtilities.tokensFor(tree), document, cancellable);
                     if (tokenSequence == null) {
                         return;
                     }
                     while (tokenSequence.moveNext()) {
                         Token token = tokenSequence.token();
                         if (token.toString().equals(elementsNames.get(tree))) {
                             start = tokenSequence.offset();
                             end = start + token.length();
                         }
                     }
                     positions.add(new Position((int) start, (int) end));
                 }
                 if (SwingUtilities.isEventDispatchThread()) {
                     updateEditor(positions, document).run();
                 } else {
                     SwingUtilities.invokeLater(updateEditor(positions, document));
                 }
             }
 
             Set<Element> getElementsForSimpleName(String simpleName, Set<Element> elements) {
                 Set<Element> results = new HashSet<Element>();
                 for (Element e : elements) {
                     if (e != null && e.getSimpleName() != null && e.getSimpleName().toString().equals(simpleName)) {
                         results.add(e);
                     }
                 }
                 
                 return results;
             }
         };
     }
 
     private Runnable updateEditor(final Collection<Position> positions, final Document document) {
         return new Runnable() {
 
             public void run() {
                 for (Position position : positions) {
                     OffsetsBag bag = (OffsetsBag) document.getProperty(SemanticHighlighter.class);
                     if (bag == null) {
                         bag = new OffsetsBag(document);
                         document.putProperty(SemanticHighlighter.class, bag);
                     }
                     HighlightsSequence highlightsSequence = bag.getHighlights(position.getStart(), position.getEnd());
                     List<AttributeSet> attributeSet = new ArrayList<AttributeSet>();
                     while (highlightsSequence.moveNext()) {
                         attributeSet.add(highlightsSequence.getAttributes());
                     }
                     attributeSet.add(AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, Color.LIGHT_GRAY));
                     AttributeSet[] array = attributeSet.toArray(new AttributeSet[attributeSet.size()]);
                     AttributeSet asfinal = AttributesUtilities.createImmutable(array);
                     bag.addHighlight(position.start, position.getEnd(), asfinal);
                 }
             }
         };
     }
 
     private class Position {
 
         private int start;
         private int end;
 
         Position(int start, int end) {
             this.start = start;
             this.end = end;
         }
 
         int getStart() {
             return start;
         }
 
         int getEnd() {
             return end;
         }
     }
 }
 
 
