 /*
  * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 2 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 2 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
  * CA 95054 USA or visit www.sun.com if you need additional information or
  * have any questions.
  */
 package com.sun.tools.javafx.comp;
 
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.*;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.List;
 import com.sun.tools.javac.util.ListBuffer;
 import com.sun.tools.javac.util.Log;
 import com.sun.tools.javac.util.Name;
 import com.sun.tools.javac.code.Flags;
 import com.sun.tools.javac.code.Scope;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symbol.TypeSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.code.Type.*;
 import com.sun.tools.javac.code.Symtab;
 import com.sun.tools.javac.code.TypeTags;
 import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
 
 import com.sun.tools.javafx.tree.JFXAttributeDefinition;
 import com.sun.tools.javafx.tree.JFXBlockExpression;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXOperationDefinition;
 import com.sun.tools.javafx.tree.JFXInitDefinition;
 import com.sun.tools.javafx.tree.JFXObjectLiteralPart;
 import com.sun.tools.javafx.tree.JFXPureObjectLiteral;
 import com.sun.tools.javafx.tree.JFXStringExpression;
 import com.sun.tools.javafx.tree.JFXVar;
 import com.sun.tools.javafx.tree.JavafxTreeTranslator;
 import com.sun.tools.javafx.tree.JavafxTreeMaker; // only for BlockExpression
 import java.util.HashSet;
 import java.util.Set;
 
 public class JavafxToJava extends JavafxTreeTranslator {
     protected static final Context.Key<JavafxToJava> jfxToJavaKey =
         new Context.Key<JavafxToJava>();
 
     private JavafxTreeMaker make;
     private Log log;
     private Name.Table names;
     private final Symtab syms;
     private JavafxInitializationBuilder initBuilder;
    private Set visitedNewClasses;
 
     private final String objLitSyntheticName = "$objlit$synth$";
     private int currentObjLitCounter = 0;
     private ListBuffer<JCStatement> prependInFrontOfStatement = null;
     
     public static JavafxToJava instance(Context context) {
         JavafxToJava instance = context.get(jfxToJavaKey);
         if (instance == null)
             instance = new JavafxToJava(context);
         return instance;
     }
 
     protected JavafxToJava(Context context) {
         make = (JavafxTreeMaker)JavafxTreeMaker.instance(context);
         log = Log.instance(context);
         names = Name.Table.instance(context);
         syms = Symtab.instance(context);
         initBuilder = JavafxInitializationBuilder.instance(context);
         names = Name.Table.instance(context);
     }
     
     private List<JCStatement> translateStatements(List<JCStatement> stats) {
         if (stats != null)  {
             List<JCStatement> prev = null;
             for (List<JCStatement> l = stats; l.nonEmpty(); l = l.tail) {
                 // translate must occur immediately before prependInFrontOfStatement check
                 JCStatement trans = translate(l.head);
                 if (trans == null) {
                     // This statement has translated to nothing, remove it from the list
                     prev.tail = l.tail;
                     l = prev;
                     continue;
                 }
                 if (prependInFrontOfStatement != null) {
                     List<JCStatement> pl = prependInFrontOfStatement.toList();
                     List<JCStatement> last = prependInFrontOfStatement.last;
                     // attach remainder of list to the prepended statements
                     for (List<JCStatement> al = pl; ; al = al.tail) {
                         if (al.tail == last) {
                             al.tail = l;
                             break;
                         }
                     }
                     // attach prepended statement to previous part of list
                     if (prev == null) {
                         stats = pl;
                     } else {
                         prev.tail = pl;
                     }
                     prependInFrontOfStatement = null;
                 }
                 l.head = trans;
                 prev = l;
             }
         }
         return stats;
     }
     
     @Override
     public void visitTopLevel(JCCompilationUnit tree) {
         currentObjLitCounter = 0;
         super.visitTopLevel(tree);
 //        tree.defs = tree.defs.prepend(makeImport("com.sun.javafx.runtime.location"));
         result = tree;
     }
     
     @Override
     public void visitClassDeclaration(JFXClassDeclaration tree) {
         int prevObjLitCounter = currentObjLitCounter;
         currentObjLitCounter = 0;
        Set prevVisitedNews = visitedNewClasses;
         try {
             visitedNewClasses = new HashSet<JCNewClass>();
             super.visitClassDeclaration(tree);
         } finally {
             currentObjLitCounter = prevObjLitCounter;
             visitedNewClasses = prevVisitedNews;
         }
         
         List<JCTree> defs = tree.defs;
         if (tree.isModuleClass) {
             // Add main method...
             List<JCExpression> emptyExpressionList = List.nil();
             JCNewClass newClass = make.NewClass(null, emptyExpressionList, make.Ident(tree.getSimpleName()), emptyExpressionList, null);
             JCFieldAccess select = make.Select(newClass, Name.fromString(names, JavafxModuleBuilder.runMethodName));
             JCMethodInvocation runCall = make.Apply(emptyExpressionList, select, emptyExpressionList);
             List<JCStatement> mainStats = List.<JCStatement>of(make.Exec(runCall));
             List<JCVariableDecl> paramList = List.nil();
             paramList = paramList.append(make.VarDef(make.Modifiers(0), 
                     Name.fromString(names, "args"), 
                     make.TypeArray(make.Ident(Name.fromString(names, "String"))), 
                     null));
             JCBlock body = make.Block(0, mainStats);
             JCMethodDecl meth = make.MethodDef(make.Modifiers(Flags.PUBLIC | Flags.STATIC),
                     Name.fromString(names, "main"), 
                     make.TypeIdent(TypeTags.VOID), 
                     List.<JCTypeParameter>nil(), 
                     paramList, 
                     emptyExpressionList, 
                     body, 
                     null);
             defs = defs.append(meth);
         }
 
         result = make.ClassDef(tree.mods, tree.name, tree.typarams, tree.extending, tree.implementing, defs);
     }
     
     @Override
     public void visitInitDefinition(JFXInitDefinition tree) {
         result = null; // Just remove this tree...
     }
 
     @Override
     public void visitPureObjectLiteral(JFXPureObjectLiteral tree) {
         super.visitPureObjectLiteral(tree);
         Name tmpName = getSyntheticName();
         JCIdent clazz = tree.getIdentifier();
         ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
         JCNewClass newClass = 
                 make.NewClass(null, null, clazz, List.<JCExpression>nil(), null);
         
         // We added this. The call to javafx$init$ is done below. We should not convert this to a BlockExpression.
         visitedNewClasses.add(newClass);
         
         JCVariableDecl tmpVar = make. VarDef(make.Modifiers(0), tmpName, clazz, newClass);
         stats.append(tmpVar);
         JCStatement lastStatement = null;
         for (JCStatement part : tree.getParts()) {
             if (part instanceof JFXObjectLiteralPart) {
                 JFXObjectLiteralPart olpart = (JFXObjectLiteralPart)part;
                 DiagnosticPosition diagPos = olpart.pos();
                 JCIdent ident1 = make.Ident(tmpName);
                 JCFieldAccess attr = make.at(diagPos).Select(
                         ident1,
                         olpart.getName());
                 JCAssign assign1 = make.Assign(
                         attr,
                         translate(olpart.getTranslationInit()));
                 lastStatement = make.Exec(assign1);
                 stats.append(lastStatement); 
             } else {
                 log.error(tree.pos, "compiler.err.javafx.not.yet.implemented",
                         part.getClass().getName() + " in object literal");
             }
         }
         
         List<JCExpression> typeargs = List.nil();
         List<JCExpression> args = List.nil();
         
         JCIdent ident3 = make.Ident(tmpName);   
         JCFieldAccess select1 = make.Select(ident3, initBuilder.initializerName);
         JCMethodInvocation apply1 = make.Apply(typeargs, select1, args);
         JCExpressionStatement exec1 = make.Exec(apply1);
         stats.append(exec1);
          
         JCIdent ident2 = make.Ident(tmpName);
         JFXBlockExpression blockExpr1 = ((JavafxTreeMaker)make).BlockExpression(0, stats.toList(), ident2);
         result = blockExpr1; 
     }
 
     
     @Override
     public void visitStringExpression(JFXStringExpression tree) {
        StringBuffer sb = new StringBuffer();
         List<JCExpression> parts = tree.getParts();
         ListBuffer<JCExpression> values = new ListBuffer<JCExpression>();
         
         JCLiteral lit = (JCLiteral)(parts.head);            // "...{
         sb.append((String)lit.value);            
         parts = parts.tail;
         
         while (parts.nonEmpty()) {
 
             lit = (JCLiteral)(parts.head);                  // optional format (or null)
             String format = (String)lit.value;
             sb.append(format.length() == 0? "%s" : format);
             parts = parts.tail;
             
             values.append(translate(parts.head));           // expression
             parts = parts.tail;
             
             lit = (JCLiteral)(parts.head);                  // }...{  or  }..."
             sb.append((String)lit.value);
             parts = parts.tail;
         }
         JCLiteral formatLiteral = make.at(tree.pos).Literal(TypeTags.CLASS, sb.toString());
         JCExpression formatter = make.Ident(Name.fromString(names, "java"));
         for (String s : new String[] {"lang", "String", "format"}) {
             formatter = make.Select(formatter, Name.fromString(names, s));
         }
         values.prepend(formatLiteral);
         result = make.Apply(null, formatter, values.toList());
     }
 
     @Override
     public void visitVar(JFXVar tree) {
         super.visitVar(tree);
         DiagnosticPosition diagPos = tree.pos();
         JCModifiers mods = tree.getModifiers();
         long modFlags = mods==null? 0L : mods.flags;
         if ((modFlags & Flags.FINAL) != 0 && tree instanceof JFXAttributeDefinition) {
             modFlags &= ~Flags.FINAL;  // because of init fields can't be final
         }
         mods = make.Modifiers(modFlags);
         JCExpression typeExpresion = makeTypeTree(tree.type, diagPos);
         result = make.at(diagPos).VarDef(mods, tree.name, typeExpresion, tree.init);
     }
       
     @Override
     public void visitAttributeDefinition(JFXAttributeDefinition tree) {
         visitVar(tree);
     }
     
     @Override
     public void visitOperationDefinition(JFXOperationDefinition tree) {
         super.visitOperationDefinition(tree);
         DiagnosticPosition diagPos = tree.pos();
         MethodType mtype = (MethodType)tree.type;
         JFXBlockExpression bexpr = tree.getBodyExpression();
         JCBlock body;
         List<JCStatement> stats = bexpr.stats;
         if (bexpr.value != null) {
             JCStatement valueStatement;
             if (mtype.getReturnType() == syms.voidType) {
                 valueStatement = make.at(diagPos).Exec(bexpr.value);
             } else {
                 valueStatement = make.at(diagPos).Return(bexpr.value);
             }
             stats = stats.append(valueStatement);
         }
         body = make.at(diagPos).Block(0, stats);
         result = make.at(diagPos).MethodDef(tree.mods, 
                 tree.name, 
                 makeTypeTree(mtype.getReturnType(), diagPos), 
                 make.at(diagPos).TypeParams(mtype.getTypeArguments()), 
                 tree.getParameters(),
                 make.at(diagPos).Types(mtype.getThrownTypes()),
                 body, 
                 null);
     }
 
     public void visitBlockExpression(JFXBlockExpression tree) {
         tree.stats = translateStatements(tree.stats);
         tree.value = translate(tree.value);
 	result = tree;
     }
  
     /**
      * Allow prepending of statements and/or deletin by translation to null
      */
     @Override
     public void visitBlock(JCBlock tree) {
         tree.stats = translateStatements(tree.stats);
 	result = tree;
     }
 
     @Override
     public void visitNewClass(JCNewClass tree) {
         if (visitedNewClasses.contains(tree)) {
             result = tree;
             return;
         }
         
         visitedNewClasses.add(tree);
         
         Symbol initSym = getJavafxInitializerSymbol(tree);
         if (initSym == null) {
             super.visitNewClass(tree);
         }
         else {
         Name tmpName = getSyntheticName();
         ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
         
         JCVariableDecl tmpVar = make.at(tree.pos).VarDef(make.Modifiers(0), tmpName, tree.clazz, tree);
         stats.append(tmpVar);
         
         List<JCExpression> typeargs = List.nil();
         List<JCExpression> args = List.nil();
         
         JCIdent ident3 = make.Ident(tmpName);   
         JCFieldAccess select1 = make.at(tree.pos).Select(ident3, initBuilder.initializerName);
         JCMethodInvocation apply1 = make.at(tree.pos).Apply(typeargs, select1, args);
         JCExpressionStatement exec1 = make.at(tree.pos).Exec(apply1);
         stats.append(exec1);
          
         JCIdent ident2 = make.Ident(tmpName);
         JFXBlockExpression blockExpr1 = ((JavafxTreeMaker)make).at(tree.pos).BlockExpression(0, stats.toList(), ident2);
         result = blockExpr1; 
         }
     }
     
     private Name getSyntheticName() {
         return Name.fromString(names, objLitSyntheticName + currentObjLitCounter++);
     }
 
     public JCImport makeImport(String str, DiagnosticPosition diagPos) {
         JCExpression tree = makeQualifiedTree(str, diagPos);
         tree = make.at(diagPos).Select(tree, names.asterisk);
         return make.at(diagPos).Import(tree, false);
     }
 
     public JCExpression makeQualifiedTree(String str, DiagnosticPosition diagPos) {
         JCExpression tree = null;
         int inx;
         int lastInx = 0;
         do {
             inx = str.indexOf('.', lastInx);
             int endInx;
             if (inx < 0) {
                 endInx = str.length();
             } else {
                 endInx = inx;
             }
             String part = str.substring(lastInx, endInx);
             Name partName = Name.fromString(names, part);
             tree = tree == null? 
                 make.at(diagPos).Ident(partName) : 
                 make.at(diagPos).Select(tree, partName);
             lastInx = endInx + 1;
         } while (inx >= 0);
         return tree;
     }
     
     public JCExpression makeTypeTree(Type t, DiagnosticPosition diagPos) {
         if (t.tag == TypeTags.CLASS) {
             JCExpression texp = makeQualifiedTree(t.tsym.getQualifiedName().toString(), diagPos);
             // Type outer = t.getEnclosingType();
             if (!t.getTypeArguments().isEmpty()) {
                 List<JCExpression> targs = List.<JCExpression>nil();
                 for (Type ta : t.getTypeArguments()) {
                     targs = targs.append(makeTypeTree(ta, diagPos));
                 }
                 texp = make.at(diagPos).TypeApply(texp, targs);
             }
             return texp;
         } else {
             return make.at(diagPos).Type(t);
         }
     }
 
     private Symbol getJavafxInitializerSymbol(JCNewClass newClass) {
         // newClass.type is null for the changeListener (on change trigger) anonymous class.
         // See JavaffxInitializationBuilder.visitAttributeDefinition
         if (newClass.type == null) {
             return null;
         }
         
         Symbol sym = newClass.type.tsym;
         if (sym == null ||
                 sym.members() == null || 
                 !(sym instanceof TypeSymbol)) {
             return null;
         }
 
         Scope.Entry entry = ((TypeSymbol)sym).members().lookup(initBuilder.initializerName);
         if (entry.sym == null) {
             return null;
         }
 
         return entry.sym;
     }
 
 }
 
