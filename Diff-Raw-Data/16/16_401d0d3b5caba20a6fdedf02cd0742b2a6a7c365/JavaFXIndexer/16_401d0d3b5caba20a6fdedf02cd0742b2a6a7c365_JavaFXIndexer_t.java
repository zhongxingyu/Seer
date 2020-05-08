 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.source.indexing;
 
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.ImportTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.MemberSelectTree;
 import com.sun.javafx.api.tree.ObjectLiteralPartTree;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.TypeClassTree;
 import com.sun.javafx.api.tree.VariableTree;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symbol.TypeSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.tree.JFXIdent;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.Name;
 import javax.lang.model.element.NestingKind;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.TypeKind;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXParserResult;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.modules.javafx.source.tasklist.FXErrorAnnotator;
 import org.netbeans.modules.parsing.api.Snapshot;
 import org.netbeans.modules.parsing.spi.Parser.Result;
 import org.netbeans.modules.parsing.spi.indexing.Context;
 import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
 import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
 import org.netbeans.modules.parsing.spi.indexing.Indexable;
 import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
 import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
 import org.openide.filesystems.FileUtil;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 public class JavaFXIndexer extends EmbeddingIndexer {
     final private static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(JavaFXIndexer.class.getName());
     final private static boolean DEBUG = LOG.isLoggable(Level.FINEST);
 
     final public static String NAME = "fx";
     final public static int VERSION = 1;
 
     public enum IndexKey {
         PACKAGE_NAME,
         CLASS_FQN, CLASS_NAME_SIMPLE, CLASS_NAME_INSENSITIVE,
         FUNCTION_DEF, FUNCTION_INV, FIELD_DEF, FIELD_REF,
         TYPE_REF, TYPE_IMPL,
         NOT_INDEXED
     }
 
 
     // <editor-fold defaultstate="collapsed" desc="Indexer Factory">
     public static class Factory extends EmbeddingIndexerFactory {
 
         @Override
         public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
             return new JavaFXIndexer();
         }
 
         @Override
         public void filesDeleted(Iterable<? extends Indexable> itrbl, Context cntxt) {
             for(Indexable ixbl : itrbl) {
                 try {
                     IndexingSupport.getInstance(cntxt).removeDocuments(ixbl);
                 } catch (IOException e) {
                     LOG.log(Level.WARNING, null, e);
                 }
             }
         }
 
         @Override
         public void filesDirty(Iterable<? extends Indexable> itrbl, Context cntxt) {
            for(Indexable ixbl : itrbl) {
                try {
                    IndexingSupport.getInstance(cntxt).markDirtyDocuments(ixbl);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
         }
 
         @Override
         public int getIndexVersion() {
             return VERSION;
         }
 
         @Override
         public String getIndexerName() {
             return NAME;
         }
     }// </editor-fold>
     @Override
     protected void index(final Indexable indexable, Result result, Context context) {
         final JavaFXParserResult fxresult = (JavaFXParserResult)result;
         if (DEBUG) {
             LOG.log(Level.FINEST,"Indexing {0}", indexable.toString());
             LOG.log(Level.FINEST, "Tree: {0}", fxresult.getCompilationUnit());
         }
 
         IndexingSupport support;
         try {
             support = IndexingSupport.getInstance(context);
             IndexDocument document = support.createDocument(indexable);
 
             JavaFXTreePathScanner<Void, IndexDocument> visitor = new JavaFXTreePathScanner<Void, IndexDocument>() {
 
                 @Override
                 public Void visitImport(ImportTree node, IndexDocument document) {
                     Tree jfxIdent = node.getQualifiedIdentifier();
                     if (jfxIdent == null) {
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Error resolving import statment: {0}", node);
                         }
                         return super.visitImport(node, document);
                     }
                     String indexVal = jfxIdent.toString();
                     if (indexVal != null) {
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Indexing import type reference {0}", indexVal);
                         }
                         index(document, IndexKey.TYPE_REF, indexVal);
                     } else {
                         LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node);
                     }
 
                     return super.visitImport(node, document);
                 }
 
                 @Override
                 public Void visitClassDeclaration(ClassDeclarationTree node, IndexDocument document) {
                     if (!node.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                         Element e = fxresult.getTrees().getElement(getCurrentPath());
                         if (e == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                             }
                             return super.visitClassDeclaration(node, document);
                         }
 
                         TypeElement type = (TypeElement)e;
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Indexing {0}:", type.getQualifiedName());
                             LOG.log(Level.FINEST, "  Simple: {0}", node.getSimpleName());
                             LOG.log(Level.FINEST, "  Case insensitive: {0}", node.getSimpleName().toString().toLowerCase());
                         }
 
                         List<ExpressionTree> superTypes = node.getSupertypeList();
                         if (superTypes != null) {
                             for(ExpressionTree et : superTypes) {
                                 JavaFXTreePath tp = JavafxcTrees.getPath(getCurrentPath(), et);
                                 if (tp != null) {
                                     TypeElement supr = (TypeElement)fxresult.getTrees().getElement(tp);
                                     if (supr != null) {
                                         if (DEBUG) {
                                             LOG.log(Level.FINEST, "Indexing {0} as a supertype of {1}:", new Object[]{supr.getQualifiedName(), type.getQualifiedName()});
                                         }
                                         index(document, IndexKey.TYPE_IMPL, supr.getQualifiedName().toString());
                                         index(document, IndexKey.TYPE_REF, supr.getQualifiedName().toString());
                                     }
                                 }
                             }
                         }
                         index(document, IndexKey.CLASS_NAME_SIMPLE, node.getSimpleName().toString());
                         index(document, IndexKey.CLASS_NAME_INSENSITIVE, node.getSimpleName().toString().toLowerCase());
                         String fqn = type.getQualifiedName().toString();
                         index(document, IndexKey.CLASS_FQN, fqn);
                         if (type.getNestingKind() == NestingKind.TOP_LEVEL) {
                             int pkgLen = fqn.lastIndexOf(".");
                             if (pkgLen > -1) {
                                 index(document, IndexKey.PACKAGE_NAME, fqn.substring(0, pkgLen));
                             } else {
                                 index(document, IndexKey.PACKAGE_NAME, IndexingUtilities.DEFAULT_PACKAGE);
                             }
                         }
                     }
                     return super.visitClassDeclaration(node, document);
                 }
 
                 @Override
                 public Void visitVariable(VariableTree node, IndexDocument document) {
                     if (!node.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                         VariableElement e = (VariableElement)fxresult.getTrees().getElement(getCurrentPath());
                         if (e == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                             }
                             return super.visitVariable(node, document);
                         }
                         if (e.getKind() == ElementKind.FIELD) { // can handle only fields for now
                             ElementHandle eh = ElementHandle.create(e);
                             if (eh == null) {
                                 if (DEBUG) {
                                     LOG.log(Level.FINEST, "Error while processing variable: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                                 }
                                 return super.visitVariable(node, document);
                             }
 
                             String indexVal = IndexingUtilities.getIndexValue(eh);
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Indexing variable {0} as {1}\n", new String[]{node.toString(), indexVal}); // NOI18N
                             }
                             index(document, IndexKey.FIELD_DEF, indexVal);
                             indexVal = e.asType() != null ? e.asType().toString() : null;
                             if (indexVal != null) {
                                 if (DEBUG) {
                                     LOG.log(Level.FINEST, "Indexing variable type reference {0}\n", new String[]{indexVal}); // NOI18N
                                 }
                                 index(document, IndexKey.TYPE_REF, indexVal);
                             } else {
                                 LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node.getInitializer());
                             }
 
                         }
                     }
                     return super.visitVariable(node, document);
                 }
 
                 @Override
                 public Void visitFunctionDefinition(FunctionDefinitionTree node, IndexDocument document) {
                     if (!node.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                         Element el = fxresult.getTrees().getElement(getCurrentPath());
                         if (el == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                             }
                             return super.visitFunctionDefinition(node, document);
                         }
                         if (el.getKind() == ElementKind.METHOD) {
                             ExecutableElement e = (ExecutableElement)el;
                             if (e.asType() == null) return super.visitFunctionDefinition(node, document); // workaround for NPE in Symbol$MethodSymbol
                             if (e.getReturnType() != null && e.getReturnType().getKind() != TypeKind.OTHER && !e.getSimpleName().contentEquals("javafx$run$")) { // skip the synthetic "$javafx$run$" method generated for javafx scripts
                                 ElementHandle eh = ElementHandle.create(e);
                                 if (eh == null) {
                                     if (DEBUG) {
                                         LOG.log(Level.FINEST, "Error while processing function definition: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                                     }
                                     return super.visitFunctionDefinition(node, document);
                                 }
                                 String indexVal = IndexingUtilities.getIndexValue(eh);
                                 if (DEBUG) {
                                     LOG.log(Level.FINEST, "Indexing function definition {0} as {1}\n", new String[]{node.toString(), indexVal});
                                 }
                                 index(document, IndexKey.FUNCTION_DEF, indexVal);
                                 indexVal = e.asType() != null ? e.asType().toString() : null;
                                 if (indexVal != null) {
                                     if (DEBUG) {
                                         LOG.log(Level.FINEST, "Indexing function def type reference {0}\n", new String[]{indexVal});
                                     }
                                     index(document, IndexKey.TYPE_REF, indexVal);
                                 } else {
                                     LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node);
                                 }
                             }
                         }
                     }
                     return super.visitFunctionDefinition(node, document);
                 }
 
                 @Override
                 public Void visitMethodInvocation(FunctionInvocationTree node, IndexDocument document) {
                     Element el = fxresult.getTrees().getElement(getCurrentPath());
                     if (el == null) {
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                         }
                         return super.visitMethodInvocation(node, document);
                     }
                     if (el.getKind() == ElementKind.METHOD) {
                         ExecutableElement e = (ExecutableElement)el;
                         ElementHandle eh = ElementHandle.create(e);
                         if (eh == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error while processing method invocation: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                             }
                             return super.visitMethodInvocation(node, document);
                         }
                         String indexVal = IndexingUtilities.getIndexValue(eh);
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Indexing method invocation {0} as {1}\n", new String[]{node.toString(), indexVal});
                         }
                         index(document, IndexKey.FUNCTION_INV, indexVal);
                         indexVal = e.asType() != null ? e.asType().toString() : null;
                         if (indexVal != null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Indexing function inv type reference {0}\n", new String[]{indexVal});
                             }
                             index(document, IndexKey.TYPE_REF, indexVal);
                         } else {
                             LOG.log(Level.FINE, "Can not determine function inv type for: {0}", node != null ? node.getJavaFXKind() : "null");
                         }
                     }
                     return super.visitMethodInvocation(node, document);
                 }
 
                 @Override
                 public Void visitTypeClass(TypeClassTree node, IndexDocument document) {
                     Element el = fxresult.getTrees().getElement(getCurrentPath());
                     if (el == null) {
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                         }
                         return super.visitTypeClass(node, document);
                     }
                     if (el.getKind() == ElementKind.CLASS || el.getKind() == ElementKind.INTERFACE) {
                         ElementHandle eh = ElementHandle.create(el);
                         if (eh == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error while processing type class: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                             }
                             return super.visitTypeClass(node, document);
                         }
                         String indexVal = IndexingUtilities.getIndexValue(eh);
                         if (indexVal != null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Indexing type reference {0} as {1}\n", new String[]{node.toString(), indexVal});
                             }
                             index(document, IndexKey.TYPE_REF, indexVal);
                         } else {
                             LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node);
                         }
                     }
                     return super.visitTypeClass(node, document);
                 }
 
                 @Override
                 public Void visitMemberSelect(MemberSelectTree node, IndexDocument document) {
                     ExpressionTree expression = node.getExpression();
                     if (expression instanceof JFXIdent) {
                         Name memberName = node.getIdentifier();
                         Type type = ((JFXIdent)expression).type;
                         if (type == null) return super.visitMemberSelect(node, document);
                         TypeSymbol ts = type.asElement();
                         if (ts == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                             }
                             return super.visitMemberSelect(node, document);
                         }
                         if (ts.getKind() != ElementKind.CLASS) return super.visitMemberSelect(node, document);
                         /**
                          * Workaround for NPEs thrown from the javac when calling ts.getEnclosedElements()
                          * The NPE is the result of the source not being compilable
                          *
                          * Unfortunately, we can not check for compilation errors before indexing because due to http://javafx-jira.kenai.com/browse/JFXC-3468
                          * many compilable sources are falsly marked as non-compilable
                          *
                          * Just ignore the exception - nothing else to do than may be log it
                          */
                         try {
                             for(Symbol sy : ts.getEnclosedElements()) {
                                 if (sy.getKind() == ElementKind.FIELD) {
                                     if (sy.getSimpleName().equals(memberName)) {
                                         ElementHandle eh = ElementHandle.create(sy);
                                         if (eh == null) {
                                             if (DEBUG) {
                                                 LOG.log(Level.FINEST, "Error while processing member select: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                                             }
                                             return super.visitMemberSelect(node, document);
                                         }
                                         String indexVal = IndexingUtilities.getIndexValue(ElementHandle.create(sy));
                                         if (indexVal != null) {
                                             if (DEBUG) {
                                                 LOG.log(Level.FINEST, "Indexing field reference {0} as {1}\n", new String[]{node.toString(), indexVal});
                                             }
                                             index(document, IndexKey.FIELD_REF, indexVal);
                                         } else {
                                             LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node);
                                         }
                                     }
                                 }
                             }
                         } catch (NullPointerException e) {
                             LOG.log(Level.INFO, "Trying to index non-compilable file {0}. Giving up.", indexable.getRelativePath());
                         }
                     }
 
                     return super.visitMemberSelect(node, document);
                 }
 
                 @Override
                 public Void visitObjectLiteralPart(ObjectLiteralPartTree node, IndexDocument document) {
                     Element el = fxresult.getTrees().getElement(getCurrentPath());
                     if (el == null) {
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                         }
                         return super.visitObjectLiteralPart(node, document);
                     }
                     switch (el.getKind()) {
                         case FIELD: {
                             ElementHandle eh = ElementHandle.create(el);
                             if (eh == null) {
                                 if (DEBUG) {
                                     LOG.log(Level.FINEST, "Error while processing object literal part: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                                 }
                                 return super.visitObjectLiteralPart(node, document);
                             }
                             String indexVal = IndexingUtilities.getIndexValue(eh);
                             if (indexVal != null) {
                                 if (DEBUG) {
                                     LOG.log(Level.FINEST, "Indexing field reference {0} as {1}\n", new String[]{node.toString(), indexVal});
                                 }
                                 index(document, IndexKey.FIELD_REF, indexVal);
                             } else {
                                 LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node);
                             }
                         }
                     }
                     return super.visitObjectLiteralPart(node, document);
                 }
 
                 @Override
                 public Void visitInstantiate(InstantiateTree node, IndexDocument document) {
                     Element el = fxresult.getTrees().getElement(JavafxcTrees.getPath(getCurrentPath(), node.getIdentifier()));
                     if (el == null) {
                         if (DEBUG) {
                             LOG.log(Level.FINEST, "Error resolving element of {0}", node);
                         }
                         return super.visitInstantiate(node, document);
                     }
                     if (el.getKind() == ElementKind.CLASS) {
                         ElementHandle eh = ElementHandle.create(el);
                         if (eh == null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Error while processing instantiation: {0}\n({1})", new Object[]{node.toString(), indexable.toString()}); // NOI18N
                             }
                             return super.visitInstantiate(node, document);
                         }
                         String indexVal = IndexingUtilities.getIndexValue(ElementHandle.create(el));
                         if (indexVal != null) {
                             if (DEBUG) {
                                 LOG.log(Level.FINEST, "Indexing type reference {0} as {1}\n", new String[]{node.toString(), indexVal});
                             }
                             index(document, IndexKey.TYPE_REF, indexVal);
                         } else {
                             LOG.log(Level.FINE, "Can not determine indexing value for: {0}", node);
                         }
                     }
                     return super.visitInstantiate(node, document);
                 }
             };
 
             // if (fxresult.isErrors()) return;
             visitor.scan(fxresult.getCompilationUnit(), document);
             support.addDocument(document);
             JavaFXSource.forFileObject(FileUtil.toFileObject(new File(indexable.getURL().toURI()))).runUserActionTask(new Task<CompilationController>() {
 
                 public void run(CompilationController cc) throws Exception {
                     FXErrorAnnotator.getInstance().process(cc);
                 }
             }, true);
         } catch (Exception e) {
             LOG.log(Level.WARNING, "Error indexing " + indexable.toString(), e);
             return;
         }
     }
 
     private void index(IndexDocument document, IndexKey key, String value) {
         document.addPair(key.toString(), value, true, true);
     }
 }
