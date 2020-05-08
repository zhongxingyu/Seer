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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 package org.netbeans.modules.javafx.editor.completion;
 
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.MemberSelectTree;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.javafx.api.tree.UnitTree;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import javax.lang.model.element.Element;
 import javax.swing.JToolTip;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import org.netbeans.api.editor.completion.Completion;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.javafx.source.JavaFXSourceUtils;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.javafx.editor.JavaCompletionDoc;
 import org.netbeans.modules.javafx.editor.completion.environment.*;
 import org.netbeans.spi.editor.completion.CompletionDocumentation;
 import org.netbeans.spi.editor.completion.CompletionResultSet;
 import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
 import org.openide.filesystems.FileObject;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 public final class JavaFXCompletionQuery extends AsyncCompletionQuery implements Task<CompilationController> {
     
     private static final Logger logger = Logger.getLogger(JavaFXCompletionProvider.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     public static final String ERROR = "<error>"; // NOI18N
     public static final String INIT = "<init>"; // NOI18N
     public static final String SPACE = " "; // NOI18N
     public static final String COLON = ":"; // NOI18N
     public static final String SEMI = ";"; // NOI18N
     public static final String EMPTY = ""; // NOI18N
     public static final String ABSTRACT_KEYWORD = "abstract"; // NOI18N
     public static final String AFTER_KEYWORD = "after"; // NOI18N
     public static final String AND_KEYWORD = "and"; // NOI18N
     public static final String AS_KEYWORD = "as"; // NOI18N
     public static final String ASSERT_KEYWORD = "assert"; // NOI18N
     public static final String BEFORE_KEYWORD = "before"; // NOI18N
     public static final String BIND_KEYWORD = "bind"; // NOI18N
     public static final String BOUND_KEYWORD = "bound"; // NOI18N
     public static final String BREAK_KEYWORD = "break"; // NOI18N
     public static final String CATCH_KEYWORD = "catch"; // NOI18N
     public static final String CLASS_KEYWORD = "class"; // NOI18N
     public static final String CONTINUE_KEYWORD = "continue"; // NOI18N
     public static final String DELETE_KEYWORD = "delete"; // NOI18N
     public static final String DEF_KEYWORD = "def"; // NOI18N
     public static final String ELSE_KEYWORD = "else"; // NOI18N
     public static final String EXCLUSIVE_KEYWORD = "exclusive"; // NOI18N
     public static final String EXTENDS_KEYWORD = "extends"; // NOI18N
     public static final String FALSE_KEYWORD = "false"; // NOI18N
     public static final String FINALLY_KEYWORD = "finally"; // NOI18N
     public static final String FIRST_KEYWORD = "first"; // NOI18N
     public static final String FOR_KEYWORD = "for"; // NOI18N
     public static final String FROM_KEYWORD = "from"; // NOI18N
     public static final String FUNCTION_KEYWORD = "function"; // NOI18N
     public static final String IF_KEYWORD = "if"; // NOI18N
     public static final String IMPORT_KEYWORD = "import"; // NOI18N
     public static final String INDEXOF_KEYWORD = "indexof"; // NOI18N
     public static final String INIT_KEYWORD = "init"; // NOI18N
     public static final String IN_KEYWORD = "in"; // NOI18N
     public static final String INSERT_KEYWORD = "insert"; // NOI18N
     public static final String INSTANCEOF_KEYWORD = "instanceof"; // NOI18N
     public static final String INTO_KEYWORD = "into"; // NOI18N
     public static final String INVERSE_KEYWORD = "inverse"; // NOI18N
     public static final String LAST_KEYWORD = "last"; // NOI18N
     public static final String LAZY_KEYWORD = "lazy"; // NOI18N
     public static final String LET_KEYWORD = "let"; // NOI18N
     public static final String NEW_KEYWORD = "new"; // NOI18N
     public static final String NOT_KEYWORD = "not"; // NOI18N
     public static final String NULL_KEYWORD = "null"; // NOI18N
     public static final String ON_KEYWORD = "on"; // NOI18N
     public static final String OR_KEYWORD = "or"; // NOI18N
     public static final String OVERRIDE_KEYWORD = "override"; // NOI18N
     public static final String PACKAGE_KEYWORD = "package"; // NOI18N
     public static final String POSTINIT_KEYWORD = "postinit"; // NOI18N
     public static final String PROTECTED_KEYWORD = "protected"; // NOI18N
     public static final String PUBLIC_KEYWORD = "public"; // NOI18N
     public static final String PUBLIC_INIT_KEYWORD = "public-init"; // NOI18N
     public static final String PUBLIC_READ_KEYWORD = "public-read"; // NOI18N
     public static final String REPLACE_KEYWORD = "replace"; // NOI18N
     public static final String RETURN_KEYWORD = "return"; // NOI18N
     public static final String REVERSE_KEYWORD = "reverse"; // NOI18N
     public static final String SIZEOF_KEYWORD = "sizeof"; // NOI18N
     public static final String STEP_KEYWORD = "step"; // NOI18N
     public static final String SUPER_KEYWORD = "super"; // NOI18N
     public static final String THEN_KEYWORD = "then"; // NOI18N
     public static final String THIS_KEYWORD = "this"; // NOI18N
     public static final String THROW_KEYWORD = "throw"; // NOI18N
     public static final String TRANSIENT_KEYWORD = "transient"; // NOI18N
     public static final String TRUE_KEYWORD = "true"; // NOI18N
     public static final String TRY_KEYWORD = "try"; // NOI18N
     public static final String TWEEN_KEYWORD = "tween"; // NOI18N
     public static final String TYPEOF_KEYWORD = "typeof"; // NOI18N
     public static final String VAR_KEYWORD = "var"; // NOI18N
     public static final String WHERE_KEYWORD = "where"; // NOI18N
     public static final String WHILE_KEYWORD = "while"; // NOI18N
     public static final String WITH_KEYWORD = "with"; // NOI18N
     
     public static final String[] STATEMENT_KEYWORDS = new String[]{
         FOR_KEYWORD,
         IF_KEYWORD,
         TRY_KEYWORD, 
         WHILE_KEYWORD
     };
     public static final String[] STATEMENT_SPACE_KEYWORDS = new String[]{
         INSERT_KEYWORD,
         NEW_KEYWORD,
         REVERSE_KEYWORD,
         THROW_KEYWORD,
         VAR_KEYWORD
     };
     public static final String[] CLASS_BODY_KEYWORDS = new String[]{
         ABSTRACT_KEYWORD,
         DEF_KEYWORD,
         FUNCTION_KEYWORD,
         INIT_KEYWORD,
         POSTINIT_KEYWORD,
         PUBLIC_KEYWORD, PROTECTED_KEYWORD, PACKAGE_KEYWORD, PUBLIC_INIT_KEYWORD, PUBLIC_READ_KEYWORD,
         VAR_KEYWORD
     };
 
     static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\.|\\$)*){2,}"); // NOI18N
     
     public Set<JavaFXCompletionItem> results;
     boolean hasAdditionalItems;
     JToolTip toolTip;
     private CompletionDocumentation documentation;
     int anchorOffset;
     int toolTipOffset;
     JTextComponent component;
     public int queryType;
     private int caretOffset;
     private String filterPrefix;
     private ElementHandle element;
     private boolean hasTask;
 
     public JavaFXCompletionQuery(int queryType, int caretOffset, boolean hasTask) {
         super();
         this.queryType = queryType;
         this.caretOffset = caretOffset;
         this.hasTask = hasTask;
     }
     
     void setElement(ElementHandle element) {
         this.element = element;
     }
 
     @Override
     protected void preQueryUpdate(JTextComponent component) {
         int newCaretOffset = component.getSelectionStart();
         if (newCaretOffset >= caretOffset) {
             try {
                 if (isJavaIdentifierPart(component.getDocument().getText(caretOffset, newCaretOffset - caretOffset))) {
                     return;
                 }
             } catch (BadLocationException e) {
             }
         }
         Completion.get().hideCompletion();
     }
 
     @Override
     protected void prepareQuery(JTextComponent component) {
         this.component = component;
     }
 
     @Override
     protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
         try {
             this.caretOffset = caretOffset;
             if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE || JavaFXCompletionProvider.isJavaFXContext(component, caretOffset)) {
                 results = null;
                 documentation = null;
                 toolTip = null;
                 anchorOffset = -1;
                 JavaFXSource js = JavaFXSource.forDocument(doc);
                 if (js != null) {
                     if (queryType == JavaFXCompletionProvider.DOCUMENTATION_QUERY_TYPE && element != null) {
                         FileObject fo = JavaFXSourceUtils.getFile(element, js.getCpInfo());
                         if (fo != null) {
                             js = JavaFXSource.forFileObject(fo);
                         }
                     }
                     Future<Void> f = js.runWhenScanFinished(this, true);
                     if (!f.isDone()) {
                         component.putClientProperty("completion-active", Boolean.FALSE); // NOI18N
                         resultSet.setWaitText(NbBundle.getMessage(JavaFXCompletionProvider.class, "scanning-in-progress")); // NOI18N
                         f.get();
                     }
                     if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                         if (results != null) {
                             resultSet.addAllItems(results);
                         }
                         resultSet.setHasAdditionalItems(hasAdditionalItems);
                         if (hasAdditionalItems) {
                             resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JavaFXCompletionProvider.class, "JCP-imported-items")); // NOI18N
                         }
                     } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
                         if (toolTip != null) {
                             resultSet.setToolTip(toolTip);
                         }
                     } else if (queryType == JavaFXCompletionProvider.DOCUMENTATION_QUERY_TYPE) {
                         if (documentation != null) {
                             resultSet.setDocumentation(documentation);
                         }
                     }
                     if (anchorOffset > -1) {
                         resultSet.setAnchorOffset(anchorOffset);
                     }
                 }
             }
         } catch (Exception e) {
             Exceptions.printStackTrace(e);
         } finally {
             resultSet.finish();
         }
     }
 
     @Override
     protected boolean canFilter(JTextComponent component) {
         filterPrefix = null;
         int newOffset = component.getSelectionStart();
         if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
             int offset = Math.min(anchorOffset, caretOffset);
             if (offset > -1) {
                 if (newOffset < offset) {
                     return true;
                 }
                 if (newOffset >= caretOffset) {
                     try {
                         String prefix = component.getDocument().getText(offset, newOffset - offset);
                         filterPrefix = isJavaIdentifierPart(prefix) ? prefix : null;
                         if (filterPrefix != null && filterPrefix.length() == 0) {
                             anchorOffset = newOffset;
                         }
                     } catch (BadLocationException e) {
                     }
                     return true;
                 }
             }
             return false;
         } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
             try {
                 if (newOffset == caretOffset) {
                     filterPrefix = EMPTY;
                 } else if (newOffset - caretOffset > 0) {
                     filterPrefix = component.getDocument().getText(caretOffset, newOffset - caretOffset);
                 } else if (newOffset - caretOffset < 0) {
                     filterPrefix = newOffset > toolTipOffset ? component.getDocument().getText(newOffset, caretOffset - newOffset) : null;
                 }
             } catch (BadLocationException ex) {
             }
             return filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1; // NOI18N
         }
         return false;
     }
 
     @Override
     protected void filter(CompletionResultSet resultSet) {
         try {
             if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                 if (results != null) {
                     if (filterPrefix != null) {
                         resultSet.addAllItems(getFilteredData(results, filterPrefix));
                         resultSet.setHasAdditionalItems(hasAdditionalItems);
                     } else {
                         Completion.get().hideDocumentation();
                         Completion.get().hideCompletion();
                     }
                 }
             } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
                 resultSet.setToolTip(toolTip);
             }
             resultSet.setAnchorOffset(anchorOffset);
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
         resultSet.finish();
     }
 
     public void run(CompilationController controller) throws Exception {
         if (!hasTask || !isTaskCancelled()) {
             if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                 if (component != null) {
                     component.putClientProperty("completion-active", Boolean.TRUE); // NOI18N
                 }
                 resolveCompletion(controller);
                 if (component != null && isTaskCancelled()) {
                     component.putClientProperty("completion-active", Boolean.FALSE); // NOI18N
                 }
             } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
                 JavaFXCompletionEnvironment env = getCompletionEnvironment(controller, caretOffset,true);
                 env.resolveToolTip(controller);
             } else if (queryType == JavaFXCompletionProvider.DOCUMENTATION_QUERY_TYPE) {
                 resolveDocumentation(controller);
             }
         }
     }
 
     private void resolveDocumentation(CompilationController controller) throws IOException {
         controller.toPhase(Phase.ANALYZED);
         Element el = null;
         if (element != null) {
             el = element.resolve(controller);
         } else {
             JavaFXCompletionEnvironment env = getCompletionEnvironment(controller, caretOffset, true);
             if (env != null) {
                 el = controller.getTrees().getElement(env.getPath());
             }
         }
         if (el != null) {
             documentation = JavaCompletionDoc.create(controller, el);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void resolveCompletion(CompilationController controller) throws IOException {
         JavaFXCompletionEnvironment env = getCompletionEnvironment(controller, caretOffset,true);
         results = new HashSet<JavaFXCompletionItem>();
         if (anchorOffset == -1) {
             anchorOffset = env.getOffset();
         }
         
         // make sure the init method was called
         if (env.query != this) {
             throw new IllegalStateException("init method not called before resolveCompletion"); // NOI18N
         }
         
         Phase resPhase = controller.toPhase(Phase.ANALYZED);
         
         if  ((!resPhase.lessThan(Phase.ANALYZED)) && (! env.isTreeBroken())) {
             Tree leaf = env.getPath().getLeaf();
             env.inside(leaf);
             if (results.isEmpty()) {
                 if (anchorOffset != env.getOffset()) {
                     if (LOGGABLE) log("  let's try without moving back"); // NOI18N
                     env = getCompletionEnvironment(controller, caretOffset,false);
                     if (anchorOffset == -1) {
                         anchorOffset = env.getOffset();
                     }
                     leaf = env.getPath().getLeaf();
                     env.inside(leaf);
                 }
             }
         } else {
             if (LOGGABLE) log("Completion not resolved: phase: " + resPhase); // NOI18N
         }
         
         if (LOGGABLE) log("Results: " + results); // NOI18N
     }
 
     static boolean isJavaIdentifierPart(String text) {
         for (int i = 0; i < text.length(); i++) {
             if (!(Character.isJavaIdentifierPart(text.charAt(i)))) {
                 return false;
             }
         }
         return true;
     }
 
     private Collection<JavaFXCompletionItem> getFilteredData(Collection<JavaFXCompletionItem> data, String prefix) {
         if (prefix.length() == 0) {
             return data;
         }
         List<JavaFXCompletionItem> ret = new ArrayList<JavaFXCompletionItem>();
         boolean camelCase = prefix.length() > 1 && camelCasePattern.matcher(prefix).matches();
         for (Iterator<JavaFXCompletionItem> it = data.iterator(); it.hasNext();) {
             JavaFXCompletionItem itm = it.next();
             if (JavaFXCompletionProvider.startsWith(itm.getInsertPrefix().toString(), prefix)) {
                 ret.add(itm);
             }
         }
         return ret;
     }
 
     private String fullName(Tree tree) {
         switch (tree.getJavaFXKind()) {
             case IDENTIFIER:
                 return ((IdentifierTree) tree).getName().toString();
             case MEMBER_SELECT:
                 String sname = fullName(((MemberSelectTree) tree).getExpression());
                 return sname == null ? null : sname + '.' + ((MemberSelectTree) tree).getIdentifier();
             default:
                 return null;
         }
     }
 
     JavaFXCompletionEnvironment getCompletionEnvironment(CompilationController controller, int offset,boolean allowMovingBack) throws IOException {
        controller.toPhase(Phase.PARSED);
         String prefix = null;
         if (offset > 0) {
             TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>)controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
             // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
             if (ts.move(offset) == 0 || !ts.moveNext()) {
                 ts.movePrevious();
             }
             int len = offset - ts.offset();
             if (LOGGABLE) log("getCompletionEnvironment len = " + len); // NOI18N
             if (len > 0 &&
                         (ts.token().id() == JFXTokenId.IDENTIFIER ||
                         (ts.token().id().primaryCategory().startsWith("keyword")) || // NOI18N
                         ts.token().id().primaryCategory().equals("literal")) && // NOI18N
                     ts.token().length() >= len) {
                 //TODO: Use isKeyword(...) when available
                 prefix = ts.token().toString().substring(0, len);
                 offset = ts.offset();
             } else if (allowMovingBack) {
                 boolean moved = false;
                 while (ts.token().id() == JFXTokenId.WS) {
                     if (LOGGABLE) log("     moving back " + ts.token().id()); // NOI18N
                     if (ts.movePrevious()) {
                         moved = true;
                     } else {
                         break;
                     }
                 }
                 if (moved) {
                     // the last one was not WS, lets move past it.
                     ts.moveNext();
                     // leave the anchor at the caret position
                     anchorOffset = offset;
                     // but search at the end of possibly broken tree
                     offset = ts.offset();
                 }
             }
         }
         if (LOGGABLE) log("getCompletionEnvironment caretOffset: " + caretOffset + " offset: " + offset); // NOI18N
         JavaFXTreePath path = controller.getTreeUtilities().pathFor(offset);
         Tree t = path.getLeaf();
         SourcePositions pos = controller.getTrees().getSourcePositions();
         UnitTree unit = controller.getCompilationUnit();
         long s = pos.getStartPosition(unit, t);
         long e = pos.getEndPosition(unit, t);
         while (t != null &&
                 (t.getJavaFXKind() == JavaFXKind.ERRONEOUS ||
                     (offset <= s) ||
                     (offset > e)
                 )
         ) {
             path = path.getParentPath();
             if (path != null) {
                 t = path.getLeaf();
             } else {
                 t = null;
             }
             s = pos.getStartPosition(unit, t);
             e = pos.getEndPosition(unit, t);
         }
         if (t == null) {
             t = unit;
             path = new JavaFXTreePath(unit);
         }
         JavaFXCompletionEnvironment result = null;
         JavaFXKind k = t.getJavaFXKind();
         result = createEnvironment(k);
         result.init(offset, prefix, controller, path, controller.getTrees().getSourcePositions(), this);
         return result;
     }
     
     static JavaFXCompletionEnvironment createEnvironment(JavaFXKind k) {
         JavaFXCompletionEnvironment result = null;
         if (LOGGABLE) log("JavaFXKind: " + k); // NOI18N
         switch (k) {
             case COMPILATION_UNIT:
                 result = new CompilationUnitEnvironment();
                 break;
             case IMPORT:
                 result = new ImportTreeEnvironment();
                 break;
             case VARIABLE:
                 result = new VariableTreeEnvironment();
                 break;
             case MODIFIERS:
                 result = new ModifiersTreeEnvironment();
                 break;
             case MEMBER_SELECT:
                 result = new MemberSelectTreeEnvironment();
                 break;
             case METHOD_INVOCATION:
                 result = new MethodInvocationTreeEnvironment();
                 break;
             case INSTANTIATE_NEW:
                 result = new InstantiateNewEnvironment();
                 break;
             case RETURN:
                 result = new ReturnEnvironment();
                 break;
             case THROW:
                 result = new ThrowEnvironment();
                 break;
             case CATCH:
                 result = new CatchEnvironment();
                 break;
             case WHILE_LOOP:
                 result = new WhileLoopTreeEnvironment();
                 break;
             case PARENTHESIZED:
                 result = new ParenthesizedTreeEnvironment();
                 break;
             case TYPE_CAST:
                 result = new TypeCastEnvironment();
                 break;
             case INSTANCE_OF:
                 result = new InstanceOfTreeEnvironment();
                 break;
             case ASSIGNMENT:
                 result = new AssignmentTreeEnvironment();
                 break;
             case MULTIPLY_ASSIGNMENT:
             case DIVIDE_ASSIGNMENT:
 //            case REMAINDER_ASSIGNMENT:
             case PLUS_ASSIGNMENT:
             case MINUS_ASSIGNMENT:
 //            case AND_ASSIGNMENT:
 //            case XOR_ASSIGNMENT:
 //            case OR_ASSIGNMENT:
                 result = new CompoundAssignmentTreeEnvironment();
                 break;
             case PREFIX_INCREMENT:
             case POSTFIX_INCREMENT:
             case PREFIX_DECREMENT:
             case POSTFIX_DECREMENT:
 //            case UNARY_PLUS:
             case UNARY_MINUS:
 //            case BITWISE_COMPLEMENT:
             case LOGICAL_COMPLEMENT:
                 result = new UnaryTreeEnvironment();
                 break;
 //            case AND:
             case CONDITIONAL_AND:
             case CONDITIONAL_OR:
             case DIVIDE:
             case EQUAL_TO:
             case GREATER_THAN:
             case GREATER_THAN_EQUAL:
             case LESS_THAN:
             case LESS_THAN_EQUAL:
             case MINUS:
             case MULTIPLY:
             case NOT_EQUAL_TO:
 //            case OR:
             case PLUS:
             case REMAINDER:
 //            case XOR:
                 result = new BinaryTreeEnvironment();
                 break;
             case CONDITIONAL_EXPRESSION:
                 break;
             case BLOCK_EXPRESSION:
                 result = new BlockExpressionEnvironment();
                 break;
             case CLASS_DECLARATION:
                 result = new ClassDeclarationEnvironment();
                 break;
             case FOR_EXPRESSION_FOR:
                 result = new ForExpressionEnvironment();
                 break;
             case FOR_EXPRESSION_PREDICATE:
                 result = new ForExpressionPredicateEnvironment();
                 break;
             case FOR_EXPRESSION_IN_CLAUSE:
                 result = new ForExpressionInClauseEnvironment();
                 break;
             case FUNCTION_DEFINITION:
                 result = new FunctionDefinitionEnvironment();
                 break;
             case FUNCTION_VALUE:
                 break;
             case INIT_DEFINITION:
                 break;
             case INSTANTIATE_OBJECT_LITERAL:
                 result = new InstantiateEnvironment();
                 break;
             case INTERPOLATE_VALUE:
                 break;
             case KEYFRAME_LITERAL:
                 break;
             case OBJECT_LITERAL_PART:
                 result = new ObjectLiteralPartEnvironment();
                 break;
             case ON_REPLACE:
                 break;
             case POSTINIT_DEFINITION:
                 break;
             case SEQUENCE_DELETE:
                 break;
             case SEQUENCE_EMPTY:
                 result = new SequenceEmptyEnvironment();
                 break;
             case SEQUENCE_EXPLICIT:
                 result = new SequenceExplicitEnvironment();
                 break;
             case SEQUENCE_INDEXED:
                 break;
             case SEQUENCE_INSERT:
                 break;
             case SEQUENCE_RANGE:
                 break;
             case SEQUENCE_SLICE:
                 break;
             case STRING_EXPRESSION:
                 result = new StringExpressionEnvironment();
                 break;
             case TIME_LITERAL:
                 break;
             case TRIGGER_WRAPPER:
                 break;
             case TYPE_ANY:
                 break;
             case TYPE_CLASS:
                 break;
             case TYPE_FUNCTIONAL:
                 break;
             case TYPE_UNKNOWN:
                 break;
             case BREAK:
                 break;
             case CONTINUE:
                 break;
             case IDENTIFIER:
                 break;
             case EMPTY_STATEMENT:
                 break;
             case TRY:
                 break;
             case INT_LITERAL:
 //            case LONG_LITERAL:
             case FLOAT_LITERAL:
             case DOUBLE_LITERAL:
             case BOOLEAN_LITERAL:
 //            case CHAR_LITERAL:
             case STRING_LITERAL:
             case NULL_LITERAL:
                 break;
             case ERRONEOUS:
                 break;
             case SIZEOF:
                 break;
             case REVERSE:
                 break;
             case INDEXOF:
                 break;
             case OTHER:
                 break;
         }
 
         if (result == null) {
             result = new JavaFXCompletionEnvironment();
         }
         
         return result;
     }
             
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
