 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 package org.netbeans.api.javafx.source;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.javafx.api.tree.SyntheticTree.SynthType;
 import com.sun.source.tree.MethodTree;
 import com.sun.tools.mjavac.code.Flags;
 import com.sun.tools.mjavac.code.Symbol;
 import com.sun.tools.mjavac.code.Type;
 import com.sun.tools.mjavac.tree.JCTree;
 import com.sun.tools.javafx.api.JavafxcScope;
 import com.sun.tools.javafx.comp.JavafxAttrContext;
 import com.sun.tools.javafx.comp.JavafxEnv;
 import com.sun.tools.javafx.comp.JavafxResolve;
 import com.sun.tools.javafx.tree.JFXBreak;
 import com.sun.tools.javafx.tree.JFXContinue;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import com.sun.tools.javafx.tree.JFXLiteral;
 import com.sun.tools.javafx.tree.JFXTree;
 import com.sun.tools.javafx.tree.JavafxPretty;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileSystem;
 import org.openide.filesystems.FileUtil;
 
 import javax.lang.model.element.Element;
 import javax.lang.model.type.TypeMirror;
 import javax.swing.text.Document;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.ref.SoftReference;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.SourceVersion;
 import org.netbeans.modules.javafx.source.parsing.JavaFXParserResultImpl;
 import org.netbeans.modules.parsing.api.Source;
 
 /**
  *
  * @author Jan Lahoda, Dusan Balek, Tomas Zezula
  */
 public final class TreeUtilities {
     private static class PositionCache<T> {
         final private static ReadWriteLock cacheLock = new ReentrantReadWriteLock();
         private static class Entry<T> {
             private SoftReference<T> value;
             long start, end;
 
             public Entry(T value, long start, long end) {
                 this.value = new SoftReference(value);
                 this.start = start;
                 this.end = end;
             }
 
             public T getValue() {
                 return value.get();
             }
 
             @Override
             public boolean equals(Object obj) {
                 if (obj == null) {
                     return false;
                 }
                 if (getClass() != obj.getClass()) {
                     return false;
                 }
                 final Entry other = (Entry) obj;
                 if (this.value.get() != other.value.get() && (this.value.get() == null || !this.value.get().equals(other.value.get()))) {
                     return false;
                 }
                 if (this.start != other.start) {
                     return false;
                 }
                 if (this.end != other.end) {
                     return false;
                 }
                 return true;
             }
 
             @Override
             public int hashCode() {
                 int hash = 3;
                 hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
                 hash = 89 * hash + (int) (this.start ^ (this.start >>> 32));
                 hash = 89 * hash + (int) (this.end ^ (this.end >>> 32));
                 return hash;
             }
         }
         final private static Comparator<Entry> comparator = new Comparator<Entry>() {
 
             public int compare(Entry o1, Entry o2) {
                 if (o1 == null && o2 != null) return -1;
                 if (o1 != null && o2 == null) return 1;
                 if (o1.start == o2.start) return 0;
                 return o1.start < o2.start ? -1 : 1;
             }
         };
 
         private List<Entry> entries = new ArrayList<PositionCache.Entry>();
 
         public T getValue(long pos) {
             try {
                 cacheLock.readLock().lock();
                 if (entries.size() == 0) return null;
                 return findValue(pos, 0, entries.size() - 1);
             } finally {
                 cacheLock.readLock().unlock();
             }
         }
 
         public void addValue(T path, long start, long end) {
             try {
                 cacheLock.writeLock().lock();
                 entries.add(new Entry(path, start, end));
                 Collections.sort(entries, comparator);
             } finally {
                 cacheLock.writeLock().unlock();
             }
         }
 
         private T findValue(long pos, int intA, int intB) {
             if (Math.abs(intB - intA + 1) <= 2) {
                 for(int i=intA;i<=intB;i++) {
                     Entry<T> e = entries.get(i);
                     if (e.start == pos) {
                         return e.getValue();
                     }
                 }
                 return null;
             }
             int mid = (intA + intB) / 2;
             Entry<T> e = entries.get(mid);
             if (e.start < pos) {
                 return findValue(pos, mid, intB);
             } else if (e.start > pos) {
                 return findValue(pos, intA, mid - 1);
             } else {
                 return e.getValue();
             }
         }
     }
 
     public static TreeUtilities create(CompilationInfo info) {
         return new TreeUtilities(info);
     }
 
     private static final Logger logger = Logger.getLogger(TreeUtilities.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
     
     private JavaFXParserResultImpl parserResultImpl;
     
     /** Creates a new instance of CommentUtilities */
     TreeUtilities(final CompilationInfo info) {
         this(info.impl().parserResultImpl());
     }
 
     TreeUtilities(JavaFXParserResultImpl parserResultImpl) {
         assert parserResultImpl != null;
         this.parserResultImpl = parserResultImpl;
 //        this.handler = CommentHandlerService.instance(info.impl.getJavacTask().getContext());
     }
     
     /**
      * Returns whether or not the given tree is synthetic - generated by the parser.
      * Please note that this method does not check trees transitively - a child of a syntetic tree
      * may be considered non-syntetic.
      * @return true if the given tree is synthetic, false otherwise
      */
     public boolean isSynthetic(JavaFXTreePath path) {
         if (path == null) {
             if (LOGGABLE) log("isSynthetic invoked with null argument"); // NOI18N
             return false;
         }
         final Tree leaf = path.getLeaf();
         if (leaf instanceof JFXTree) {
             JFXTree fxLeaf = (JFXTree)leaf;
             SynthType type = fxLeaf.getGenType();
             return SynthType.SYNTHETIC.equals(type);
         }
         if (LOGGABLE) log("isSynthetic returning false because the leaf is not JFXTree."); // NOI18N
         return false;
     }
 
     public boolean isSynthetic(UnitTree cut, Tree leaf) throws NullPointerException {
         JCTree tree = (JCTree) leaf;
 
         if (tree.pos == (-1))
             return true;
 
         if (leaf.getJavaFXKind() == Tree.JavaFXKind.FUNCTION_DEFINITION) {
             //check for synthetic constructor:
             return (((JFXFunctionDefinition)leaf).mods.flags & (Flags.GENERATEDCONSTR | Flags.SYNTHETIC)) != 0L;
         }
 
         SourcePositions sp = parserResultImpl.getTrees().getSourcePositions();
         return sp.getStartPosition(cut, leaf) == sp.getEndPosition(cut, leaf);
 
 //        //check for synthetic superconstructor call:
 //        if (leaf.getJavaFXKind() == Tree.JavaFXKind.BLOCK_EXPRESSION) {
 //            ExpressionStatementTree est = (ExpressionStatementTree) leaf;
 //
 //            if (est.getExpression().getKind() == Kind.METHOD_INVOCATION) {
 //                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
 //
 //                if (mit.getMethodSelect().getKind() == Kind.IDENTIFIER) {
 //                    IdentifierTree it = (IdentifierTree) mit.getMethodSelect();
 //
 //                    if ("super".equals(it.getName().toString())) {
 //                        SourcePositions sp = info.getTrees().getSourcePositions();
 //
 //                        return sp.getEndPosition(cut, leaf) == (-1);
 //                    }
 //                }
 //            }
 //        }
 
 //        return false;
     }
     
     /**Returns list of comments attached to a given tree. Can return either
      * preceding or trailing comments.
      *
      * @param tree for which comments should be returned
      * @param preceding true if preceding comments should be returned, false if trailing comments should be returned.
      * @return list of preceding/trailing comments attached to the given tree
      */
 //    public List<Comment> getComments(Tree tree, boolean preceding) {
 //        CommentSetImpl set = handler.getComments(tree);
 //        
 //        if (!set.areCommentsMapped()) {
 //            boolean assertsEnabled = false;
 //            boolean automap = true;
 //            
 //            assert assertsEnabled = true;
 //            
 //            if (assertsEnabled) {
 //                TreePath tp = TreePath.getValue(info.getCompilationUnit(), tree);
 //                
 //                if (tp == null) {
 //                    Logger.getLogger(TreeUtilities.class.getName()).log(Level.WARNING, "Comment automap requested for Tree not from the root compilation info. Please, make sure to call GeneratorUtilities.importComments before Treeutilities.getComments. Tree: {0}", tree);
 //                    Logger.getLogger(TreeUtilities.class.getName()).log(Level.WARNING, "Caller", new Exception());
 //                    automap = false;
 //                }
 //            }
 //            
 //            if (automap) {
 //                try {
 //                    TokenSequence<JFXTokenId> seq = ((SourceFileObject) info.getCompilationUnit().getSourceFile()).getTokenHierarchy().tokenSequence(JFXTokenId.language());
 //                    new TranslateIdentifier(info, true, false, seq).translate(tree);
 //                } catch (IOException ex) {
 //                    Exceptions.printStackTrace(ex);
 //                }
 //            }
 //        }
 //        
 //        List<Comment> comments = preceding ? set.getPrecedingComments() : set.getTrailingComments();
 //        
 //        return Collections.unmodifiableList(comments);
 //    }
 
     final private PositionCache<JavaFXTreePath> pathCache = new PositionCache();
     final private PositionCache<TokenSequence<JFXTokenId>> tokenCache = new PositionCache();
 
     public JavaFXTreePath pathFor(long pos) {
         return pathFor(new JavaFXTreePath(parserResultImpl.getCompilationUnit()), pos);
     }
 
     /*XXX: dbalek
      */
     public JavaFXTreePath pathFor(JavaFXTreePath path, long pos) {
         return pathFor(path, pos, parserResultImpl.getTrees().getSourcePositions());
     }
 
     /*XXX: dbalek
      */
     public JavaFXTreePath pathFor(JavaFXTreePath path, long pos, SourcePositions sourcePositions) {
         if (parserResultImpl == null || path == null || sourcePositions == null)
             throw new IllegalArgumentException();
 
         JavaFXTreePath foundPath = pathCache.getValue(pos);
         if (foundPath != null) return foundPath;
 
         class Result extends Error {
             JavaFXTreePath path;
             Result(JavaFXTreePath path) {
                 this.path = path;
             }
         }
         
         class PathFinder extends JavaFXTreePathScanner<Void,Void> {
             private long pos;
             private SourcePositions sourcePositions;
             
             private PathFinder(long pos, SourcePositions sourcePositions) {
                 this.pos = pos;
                 this.sourcePositions = sourcePositions;
             }
 
             @Override
             public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                 long[] span = findNameSpan(node);
 
                 if (span != null && span[0] <= pos && pos <= span[1]) {
                     throw new Result(getCurrentPath());
                 }
                 
                 return super.visitClassDeclaration(node, p);
             }
 
             @Override
             public Void visitVariable(VariableTree node, Void p) {
                 long[] span = findNameSpan(node);
 
                 if (span != null && span[0] <= pos && pos <= span[1]) {
                     throw new Result(getCurrentPath());
                 }
 
                 return super.visitVariable(node, p);
             }
 
             @Override
             public Void visitFunctionDefinition(FunctionDefinitionTree node, Void p) {
                 long[] span = findNameSpan(node);
 
                 if (span != null && span[0] <= pos && pos <= span[1]) {
                     throw new Result(getCurrentPath());
                 }
 
                 return super.visitFunctionDefinition(node, p);
             }
 
             @Override
             public Void scan(Tree tree, Void p) {
                 if (tree != null && 
                     !isEmptyStringLiteral(tree)) {  // workaround for http://javafx-jira.kenai.com/browse/JFXC-3494
                     long start = sourcePositions.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                     long end = sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), tree);
                     
                    if (start == end) return null; // don't go this way; all subtrees are synthetic although they might not be flagged so
 
                     super.scan(tree, p);
                     if (start != -1 && start <= pos && end >= pos && tree.getJavaFXKind() != Tree.JavaFXKind.MODIFIERS) {
                         JavaFXTreePath tp = new JavaFXTreePath(getCurrentPath(), tree);
                         boolean isSynteticMainBlock = isSynthetic(tp.getCompilationUnit(), tp.getLeaf());
                         // we don't want to return the syntetic main block as the result
                         if (tree.getJavaFXKind() == Tree.JavaFXKind.BLOCK_EXPRESSION) {
                             JavaFXTreePath parentPath = tp.getParentPath();
                             if (parentPath != null) {
                                 JavaFXTreePath grandParentPath = parentPath.getParentPath();
                                 if (grandParentPath != null) {
                                     Tree grandParent = grandParentPath.getLeaf();
                                     if (grandParent.getJavaFXKind() == Tree.JavaFXKind.FUNCTION_DEFINITION && isSynthetic(grandParentPath)) {
                                         isSynteticMainBlock = true;
                                     }
                                 }
                             }
                         }
                         if (tree.getJavaFXKind() == Tree.JavaFXKind.FUNCTION_VALUE) {
                             JavaFXTreePath parentPath = tp.getParentPath();
                             if (parentPath != null) {
                                 Tree parent = parentPath.getLeaf();
                                 if (parent.getJavaFXKind() == Tree.JavaFXKind.FUNCTION_DEFINITION && isSynthetic(parentPath)) {
                                     isSynteticMainBlock = true;
                                 }
                             }
                         }
                         if (!isSynteticMainBlock) {
                             throw new Result(new JavaFXTreePath(getCurrentPath(), tree));
                         }
                     } else {
                         if ((start == -1) || (end == -1)) {
                             if (!isSynthetic(getCurrentPath())) {
                                 // here we might have a problem
                                 if (LOGGABLE) {
                                     logger.finest("SCAN: Cannot determine start and end for: " + treeToString(parserResultImpl, tree)); // NOI18N
                                 }
                             }
                         }
                     }
                 }
                 return null;
             }
 
             // workaround for http://javafx-jira.kenai.com/browse/JFXC-3494
             private boolean isEmptyStringLiteral(Tree tree) {
                 if (tree != null) {
 //                    return (tree instanceof JFXLiteral && ((JFXLiteral)tree).value != null && ((JFXLiteral)tree).value.equals("\"\""));
                     // see #177301 - it seems that soma compiler now uses an empty string as an empty string literal value rather than ""
                     return (tree instanceof JFXLiteral && ((JFXLiteral)tree).value != null && ((JFXLiteral)tree).value.equals(""));
                 }
                 return false;
             }
         }
         
         try {
             new PathFinder(pos, sourcePositions).scan(path, null);
         } catch (Result result) {
             path = result.path;
 
             pathCache.addValue(path,
                               sourcePositions.getStartPosition(parserResultImpl.getCompilationUnit(), path.getLeaf()),
                               sourcePositions.getEndPosition(parserResultImpl.getCompilationUnit(), path.getLeaf()));
         }
         
         if (path.getLeaf() == path.getCompilationUnit()) {
             log("pathFor returning compilation unit for position: " + pos); // NOI18N
             return path;
         }
         int start = (int)sourcePositions.getStartPosition(parserResultImpl.getCompilationUnit(), path.getLeaf());
         int end   = (int)sourcePositions.getEndPosition(parserResultImpl.getCompilationUnit(), path.getLeaf());
         while (start == -1 || pos < start || pos > end) {
             if (LOGGABLE) {
                 logger.finer("pathFor moving to parent: " + treeToString(parserResultImpl, path.getLeaf())); // NOI18N
             }
             path = path.getParentPath();
             if (LOGGABLE) {
                 logger.finer("pathFor moved to parent: " + treeToString(parserResultImpl, path.getLeaf())); // NOI18N
             }
             if (path.getLeaf() == path.getCompilationUnit()) {
                 break;
             }
             start = (int)sourcePositions.getStartPosition(parserResultImpl.getCompilationUnit(), path.getLeaf());
             end   = (int)sourcePositions.getEndPosition(parserResultImpl.getCompilationUnit(), path.getLeaf());
         }
         if (LOGGABLE) {
             log("pathFor(pos: " + pos + ") returning: " + treeToString(parserResultImpl, path.getLeaf())); // NOI18N
         }
         return path;
     }
     
     /**Computes {@link Scope} for the given position.
      */
     public JavafxcScope scopeFor(int pos) {
         JavaFXTreePath path = pathFor(pos);
         JavafxcScope scope = getScope(path);
         return scope;
     }
 
     public JavafxcScope getScope(JavaFXTreePath p) {
         JavafxcScope scope = null;
         while ((p != null) && (scope == null)) {
             try {
                 scope = parserResultImpl.getTrees().getScope(p);
             } catch (ThreadDeath td) {
                 throw td;
             } catch (Throwable ex) {
                 if (logger.isLoggable(Level.FINEST)) {
                     logger.log(Level.FINEST, "  getScope failed on " + p, ex); // NOI18N
                 }
                 p = p.getParentPath();
             }
         }
         return scope;
     }
 
     /**Find span of the {@link VariableTree#getName()} identifier in the source.
      * Returns starting and ending offset of the name in the source code that was parsed
      * (ie. {@link CompilationInfo.getText()}, which may differ from the positions in the source
      * document if it has been already altered.
      *
      * @param var variable which name should be searched for
      * @return the span of the name, or null if cannot be found
      * @since 0.25
      */
     public long[] findNameSpan(VariableTree var) {
         if (var == null || var.getName() == null) return null;
         return findNameSpan(var.getName().toString(), var, JFXTokenId.VAR, JFXTokenId.DEF, JFXTokenId.PUBLIC_INIT, JFXTokenId.PUBLIC_READ);
     }
 
     /**Find span of the {@link MethodTree#getName()} identifier in the source.
      * Returns starting and ending offset of the name in the source code that was parsed
      * (ie. {@link CompilationInfo.getText()}, which may differ from the positions in the source
      * document if it has been already altered.
      *
      * @param method method which name should be searched for
      * @return the span of the name, or null if cannot be found
      * @since 0.25
      */
     public long[] findNameSpan(FunctionDefinitionTree method) {
         if (method == null || isSynthetic(parserResultImpl.getCompilationUnit(), method)) {
             return null;
         }
         JFXFunctionDefinition jcm = (JFXFunctionDefinition) method;
         String name = jcm.name.toString();
         return findNameSpan(name, method, JFXTokenId.ABSTRACT, JFXTokenId.BOUND, JFXTokenId.OVERRIDE, JFXTokenId.FUNCTION);
     }
 
     public long[] findNameSpan(ClassDeclarationTree clazz) {
         if (clazz == null || clazz.getSimpleName() == null) return null;
 
         String name = clazz.getSimpleName().toString();
 
         return findNameSpan(name, clazz, JFXTokenId.ABSTRACT, JFXTokenId.CLASS, JFXTokenId.MIXIN);
     }
 
     public long[] findNameSpan(TypeClassTree type) {
         if (type == null || type.getClassName() == null) return null;
 
         String name = type.toString();
 
         return findNameSpan(name, type, JFXTokenId.ABSTRACT, JFXTokenId.CLASS, JFXTokenId.MIXIN);
     }
 
     /**Returns tokens for a given tree.
      */
     public TokenSequence<JFXTokenId> tokensFor(Tree tree) {
         return tokensFor(tree, parserResultImpl.getTrees().getSourcePositions());
     }
     
     /**Returns tokens for a given tree. Uses specified {@link SourcePositions}.
      */
     public TokenSequence<JFXTokenId> tokensFor(Tree tree, SourcePositions sourcePositions) {
         int start = (int)sourcePositions.getStartPosition(parserResultImpl.getCompilationUnit(), tree);
         int end   = (int)sourcePositions.getEndPosition(parserResultImpl.getCompilationUnit(), tree);
         if ((start == -1) || (end == -1)) {
             throw new RuntimeException("RE Cannot determine start and end for: " + treeToString(parserResultImpl, tree)); // NOI18N
         }
 
         TokenSequence<JFXTokenId> t = tokenCache.getValue(start);
 
         if (t == null) {
             t = ((TokenHierarchy<?>)parserResultImpl.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
             tokenCache.addValue(t, start, end);
         }
         if (t == null) {
             throw new RuntimeException("RE SDid not get a token sequence."); // NOI18N
         }
         return t.subSequence(start, end);
     }
     
     private static String treeToString(JavaFXParserResultImpl parserResultImpl, Tree t) {
         Tree.JavaFXKind k = null;
         StringWriter s = new StringWriter();
         try {
             new JavafxPretty(s, false).printExpr((JFXTree)t);
         } catch (Exception e) {
             if (LOGGABLE) logger.log(Level.FINE, "Unable to pretty print " + t.getJavaFXKind(), e); // NOI18N
         }
         k = t.getJavaFXKind();
         String res = k.toString();
         SourcePositions pos = parserResultImpl.getTrees().getSourcePositions();
         res = res + '[' + pos.getStartPosition(parserResultImpl.getCompilationUnit(), t) + ',' +  // NOI18N
                 pos.getEndPosition(parserResultImpl.getCompilationUnit(), t) + "]:" + s.toString(); // NOI18N
         return res;
     }
 
     public ExpressionTree getBreakContinueTarget(JavaFXTreePath breakOrContinue) throws IllegalArgumentException {
         if (parserResultImpl.getPhase().lessThan(CompilationPhase.ANALYZED))
             throw new IllegalArgumentException("Not in correct Phase. Required: Phase.RESOLVED, got: Phase." + parserResultImpl.getPhase().toString()); // NOI18N
         
         Tree leaf = breakOrContinue.getLeaf();
         
         switch (leaf.getJavaFXKind()) {
             case BREAK:
                 return (ExpressionTree) ((JFXBreak) leaf).target;
             case CONTINUE:
                 ExpressionTree target = (ExpressionTree) ((JFXContinue) leaf).target;
                 
                 if (target == null)
                     return null;
                 
                 // always true with current grammar
                 //if (((JFXContinue) leaf).label == null)
                     return target;
                 
             default:
                 throw new IllegalArgumentException("Unsupported kind: " + leaf.getJavaFXKind()); // NOI18N
         }
     }
 
     /**
      * Parses and analyzes given expression.
      * @param expr String expression to be parsed and analyzed
      * @param pos position in the source where the expression would occur
      * @return parsed expression tree or <code>null</code> if it was not
      *         successfull
      */
     public ExpressionTree parseExpression(String expr, int pos) {
         if (LOGGABLE) log("parseExpression pos= " + pos + " : " + expr); // NOI18N
         try {
             Source source = parserResultImpl.getSnapshot().getSource();
             Document d = source.getDocument(true);
             String start = d.getText(0, pos);
             if (LOGGABLE) log("  start = " + start); // NOI18N
             String end = d.getText(pos, d.getLength()-pos);
             if (LOGGABLE) log("  end = " + end); // NOI18N
             FileSystem fs = FileUtil.createMemoryFileSystem();
             final FileObject fo = fs.getRoot().createData("tmp" + (new Random().nextLong()) + ".fx"); // NOI18N
             Writer w = new OutputStreamWriter(fo.getOutputStream());
             w.write(start);
             w.write("\n" + expr+"\n"); // NOI18N
             w.write(end);
             w.close();
             if (LOGGABLE) log("  source written to " + fo); // NOI18N
             ClasspathInfo cp = ClasspathInfo.create(source.getFileObject());
             Source exprSource = Source.create(fo);
             JavaFXParserResult result = JavaFXParserResult.create(exprSource, cp);
             if (LOGGABLE) log("  JavaFXParserResult obtained " + result); // NOI18N
             result.toPhase(CompilationPhase.ANALYZED);
             JavaFXTreePath p = result.getTreeUtilities().pathFor(pos+2);
             if (p == null) {
                 if (LOGGABLE) log("  path for returned null"); // NOI18N
                 return null;
             }
             SourcePositions sp = result.getTrees().getSourcePositions();
             if (LOGGABLE) log(p.getLeaf().getClass().getName() + "   p = " + p.getLeaf()); // NOI18N
             // first loop will try to find our expression
             while ((p != null) && (! (p.getLeaf() instanceof ExpressionTree))) {
                 if (LOGGABLE) log(p.getLeaf().getClass().getName() + "   p (2) = " + p.getLeaf()); // NOI18N
                 p = p.getParentPath();
             }
             if (p == null) {
                 if (LOGGABLE) log("  ExpressionTree not found! Returning null"); // NOI18N
                 return null;
             }
             // the second while loop will try to find as big expression as possible
             JavaFXTreePath pp = p.getParentPath();
             if (LOGGABLE && pp != null) {
                 log(pp.getLeaf().getClass().getName() + "   pp = " + pp.getLeaf()); // NOI18N
                 log("   start == " + sp.getStartPosition(result.getCompilationUnit(),pp.getLeaf())); // NOI18N
                 log("   end == " + sp.getEndPosition(result.getCompilationUnit(),pp.getLeaf())); // NOI18N
                 log("   pos == " + pos); // NOI18N
                 log("   pos+length == " + (pos+expr.length())); // NOI18N
                 log("   (pp.getLeaf() instanceof ExpressionTree)" + (pp.getLeaf() instanceof ExpressionTree)); // NOI18N
             }
             while ((pp != null) && ((pp.getLeaf() instanceof ExpressionTree)) &&
                     (sp.getStartPosition(result.getCompilationUnit(),pp.getLeaf())>=pos) &&
                     (sp.getEndPosition(result.getCompilationUnit(),pp.getLeaf())<=(pos+expr.length()))) {
                 if (LOGGABLE) log(pp.getLeaf().getClass().getName() + "   p (3) = " + pp.getLeaf()); // NOI18N
                 p = pp;
                 pp = pp.getParentPath();
                 if (LOGGABLE) {
                     log(pp.getLeaf().getClass().getName() + "   pp = " + pp.getLeaf()); // NOI18N
                     log("   start == " + sp.getStartPosition(result.getCompilationUnit(),pp.getLeaf())); // NOI18N
                     log("   end == " + sp.getEndPosition(result.getCompilationUnit(),pp.getLeaf())); // NOI18N
                     log("   (pp.getLeaf() instanceof ExpressionTree)" + (pp.getLeaf() instanceof ExpressionTree)); // NOI18N
                 }
             }
             if (LOGGABLE) log(p.getLeaf().getClass().getName() + "   p (4) = " + p.getLeaf()); // NOI18N
             return (ExpressionTree)p.getLeaf();
         } catch (Exception x) {
             logger.log(Level.FINE, "Exception during parseExpression", x); // NOI18N
         }
         return null;
     }
 
     /**
      * @param scope
      * @param member
      * @param type
      * @return true if the given member of the given type is accessible in the
      *   given scope
      */
     public boolean isAccessible(Scope 
             scope, Element member, TypeMirror type) {
         if (LOGGABLE) {
             log("isAccessible scope == " + scope); // NOI18N
             log("   member == " + member); // NOI18N
             log("   type == " + type); // NOI18N
         }
         if (scope instanceof JavafxcScope && member instanceof Symbol && type instanceof Type) {
             JavafxResolve resolve = JavafxResolve.instance(parserResultImpl.getContext());
             if (LOGGABLE) log("     resolve == " + resolve); // NOI18N
             Object env = ((JavafxcScope) scope).getEnv();
             @SuppressWarnings("unchecked")
             JavafxEnv<JavafxAttrContext> fxEnv = (JavafxEnv<JavafxAttrContext>) env;
             if (LOGGABLE) log("     fxEnv == " + fxEnv); // NOI18N
             boolean res = resolve.isAccessible(fxEnv, (Type) type, (Symbol) member);
             if (LOGGABLE) log("     returning " + res); // NOI18N
             return res;
         } else {
             if (LOGGABLE) log("     returning FALSE from the else branch"); // NOI18N
             return false;
         }
     }
 
     /**
      *
      * @param scope
      * @param type
      * @return true if the class denoted by the type element is accessible
      *   in the given scope
      */
     public boolean isAccessible(Scope
             scope, Element type) {
         if (LOGGABLE) {
             log("isAccessible scope == " + scope); // NOI18N
             log("   type == " + type); // NOI18N
         }
         if (scope instanceof JavafxcScope &&  type instanceof Symbol.TypeSymbol) {
             JavafxResolve resolve = JavafxResolve.instance(parserResultImpl.getContext());
             if (LOGGABLE) log("     resolve == " + resolve); // NOI18N
             Object env = ((JavafxcScope) scope).getEnv();
             @SuppressWarnings("unchecked")
             JavafxEnv<JavafxAttrContext> fxEnv = (JavafxEnv<JavafxAttrContext>) env;
             if (LOGGABLE) log("     fxEnv == " + fxEnv); // NOI18N
             boolean res = resolve.isAccessible(fxEnv, (Symbol.TypeSymbol) type);
             if (LOGGABLE) log("     returning " + res); // NOI18N
             return res;
         } else {
             if (LOGGABLE) log("     returning FALSE from the else branch"); // NOI18N
             return false;
         }
     }
 
     /**
      *
      * @param scope
      * @return
      */
     public boolean isStaticContext(Scope scope) {
         Object env = ((JavafxcScope) scope).getEnv();
         @SuppressWarnings("unchecked")
         JavafxEnv<JavafxAttrContext> fxEnv = (JavafxEnv<JavafxAttrContext>) env;
         return JavafxResolve.isStatic(fxEnv);
 //        return Resolve.isStatic(((JavafxcScope) scope).getEnv());
     }
 
     private long[] findNameSpan(String name, Tree t, JFXTokenId... allowedTokens) {
         if (!SourceVersion.isIdentifier(name)) {
             //names like "<error>", etc.
             return null;
         }
 
         JCTree jcTree = (JCTree) t;
         int pos = jcTree.pos;
 
         if (pos < 0)
             return null;
 
         Set<JFXTokenId> allowedTokensSet = EnumSet.of(
                 JFXTokenId.WS,
                 JFXTokenId.PRIVATE,
                 JFXTokenId.PROTECTED,
                 JFXTokenId.PACKAGE,
                 JFXTokenId.PUBLIC,
                 JFXTokenId.STATIC);
 
         allowedTokensSet.addAll(Arrays.asList(allowedTokens));
 
         TokenSequence<JFXTokenId> tokenSequence = parserResultImpl.getTokenHierarchy().tokenSequence(JFXTokenId.language());
 
         tokenSequence.move(pos);
 
         boolean wasNext;
 
         while ((wasNext = tokenSequence.moveNext()) && allowedTokensSet.contains(tokenSequence.token().id()));
 
         if (wasNext) {
             if (tokenSequence.token().id() == JFXTokenId.IDENTIFIER &&
                 name.contentEquals(tokenSequence.token().text())) {
                 return new long[] {
                     tokenSequence.offset(),
                     tokenSequence.offset() + tokenSequence.token().length()
                 };
             }
         }
 
         return null;
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
