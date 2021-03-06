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
 import com.sun.tools.javac.tree.TreeMaker;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.List;
 import com.sun.tools.javac.util.ListBuffer;
 import com.sun.tools.javac.util.Log;
 import com.sun.tools.javac.util.Name;
 import com.sun.tools.javac.code.Flags;
 import com.sun.tools.javac.code.Scope;
 import com.sun.tools.javac.code.Kinds;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symbol.TypeSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.code.Type.*;
 import com.sun.tools.javac.code.Symtab;
 import com.sun.tools.javac.code.TypeTags;
 import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
 import com.sun.tools.javac.code.Symbol.VarSymbol;
 
 import com.sun.tools.javafx.code.JavafxBindStatus;
 import static com.sun.tools.javafx.code.JavafxVarSymbol.*;
 import com.sun.tools.javafx.comp.JavafxInitializationBuilder.TranslatedAttributeInfo;
 import com.sun.tools.javafx.tree.*;
 import com.sun.tools.javafx.comp.JavafxTypeMorpher.VarMorphInfo;
 import com.sun.tools.javafx.tree.JavafxTreeMaker; // only for BlockExpression
 
 import java.util.HashSet;
 import java.util.Set;
 import java.io.OutputStreamWriter;
 
 
 public class JavafxToJava extends JCTree.Visitor implements JavafxVisitor {
     protected static final Context.Key<JavafxToJava> jfxToJavaKey =
         new Context.Key<JavafxToJava>();
 
     JCTree result;
 
     private TreeMaker make;  // should be generating Java AST, explicitly cast when not
     private Log log;
     private Name.Table names;
     private final Symtab syms;
     private JavafxInitializationBuilder initBuilder;
     private Set<JCNewClass> visitedNewClasses;
 
     private final String syntheticNamePrefix = "jfx$$";
     private int syntheticNameCounter = 0;
     private ListBuffer<JCStatement> prependInFrontOfStatement = null;
     
     private static final String sequencesMakeString = "com.sun.javafx.runtime.sequence.Sequences.make";
     private static final String sequencesRangeString = "com.sun.javafx.runtime.sequence.Sequences.range";
     private static final String sequenceBuilderString = "com.sun.javafx.runtime.sequence.SequenceBuilder";
     private static final String toSequenceString = "toSequence";
     
     // for type morphing
     private final JavafxTypeMorpher typeMorpher;
     private JavafxBindStatus bindContext = JavafxBindStatus.UNBOUND;
     private boolean inLHS = false;
     private JavafxEnv<JavafxAttrContext> attrEnv;
     
     public static JavafxToJava instance(Context context) {
         JavafxToJava instance = context.get(jfxToJavaKey);
         if (instance == null)
             instance = new JavafxToJava(context);
         return instance;
     }
 
     protected JavafxToJava(Context context) {
         make = JavafxTreeMaker.instance(context);
         log = Log.instance(context);
         names = Name.Table.instance(context);
         syms = Symtab.instance(context);
         typeMorpher = JavafxTypeMorpher.instance(context);
         initBuilder = JavafxInitializationBuilder.instance(context);
         names = Name.Table.instance(context);
     }
     
     /** Visitor method: Translate a single node.
      */
     @SuppressWarnings("unchecked")
     public <T extends JCTree> T translate(T tree) {
 	if (tree == null) {
 	    return null;
 	} else {
 	    tree.accept(this);
 	    JCTree result = this.result;
 	    this.result = null;
 	    return (T)result; // XXX cast
 	}
     }
     
     /**  Visitor method: translate a list of variable definitions.
      */
     public List<JCVariableDecl> translateVarDefs(List<JCVariableDecl> trees) {
 	for (List<JCVariableDecl> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
     }
 
     /**  Visitor method: translate a list of type parameters.
      */
     public List<JCTypeParameter> translateTypeParams(List<JCTypeParameter> trees) {
 	for (List<JCTypeParameter> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
     }
 
     /**  Visitor method: translate a list of case parts of switch statements.
      */
     public List<JCCase> translateCases(List<JCCase> trees) {
 	for (List<JCCase> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
     }
 
     /**  Visitor method: translate a list of catch clauses in try statements.
      */
     public List<JCCatch> translateCatchers(List<JCCatch> trees) {
 	for (List<JCCatch> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
     }
 
     /**  Visitor method: translate a list of catch clauses in try statements.
      */
     public List<JCAnnotation> translateAnnotations(List<JCAnnotation> trees) {
 	for (List<JCAnnotation> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
     }
 
     /** Visitor method: translate a list of nodes.
      */
     /***
     public <T extends JCTree> List<T> translate(List<T> trees) {
 	if (trees == null) return null;
 	for (List<T> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
     }
      * ***/
 
     /** Visitor method: translate a list of nodes.
      */
     public <T extends JCTree> List<T> translate(List<T> trees) {
 	if (trees == null) return null;
         List<T> prev = null;
 	for (List<T> l = trees; l.nonEmpty();) {
             T tree = translate(l.head);
             if (tree != null) {
                 l.head = tree;
                 prev = l;
                 l = l.tail;
             } else {
                 T nonNullTree = null;
                 List<T> ls = null;
                 for (ls = l.tail; ls.nonEmpty(); ls = ls.tail) {
                     nonNullTree = translate(ls.head);
                     if (nonNullTree != null) {
                         break;
                     }
                 }
                 if (nonNullTree != null) {
                     prev = l;
                     l.head = nonNullTree;
                     l.tail = ls.tail;
                     l = l.tail;
                 }
                 else {
                     prev.tail = ls;
                     l = ls;
                 }
             }
         }
 	return trees;
     }
 
     /**  Visitor method: translate a list of variable definitions.
      */
     public List<JFXVar> translateJFXVarDefs(List<JFXVar> trees) {
 	for (List<JFXVar> l = trees; l.nonEmpty(); l = l.tail)
 	    l.head = translate(l.head);
 	return trees;
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
     
     private  <T extends JCTree> T boundTranslate(T tree, JavafxBindStatus bind) {
         JavafxBindStatus prevBindContext = bindContext;
         if (bindContext == JavafxBindStatus.UNBOUND) {
             bindContext = bind;
         } else { // TODO
             assert bind == JavafxBindStatus.UNBOUND || bind == bindContext : "how do we handle this?";
         }
         T result = translate(tree);
         bindContext = prevBindContext;
         return result;
     }
     
     private  <T extends JCTree> T translateLHS(T tree) {
         return translateLHS(tree, true);
     }
     
     private  <T extends JCTree> T translateLHS(T tree, boolean isImmediateLHS) {
         boolean wasInLHS = inLHS;
         inLHS = isImmediateLHS;
         T result = translate(tree);
         inLHS = wasInLHS;
         return result;
     }
     
     public void toJava(JavafxEnv<JavafxAttrContext> attrEnv) {
         this.attrEnv = attrEnv;
         syntheticNameCounter = 0;
         
         translate(attrEnv.toplevel);
     }
     
     @Override
     public void visitTopLevel(JCCompilationUnit tree) {
 	List<JCTree> tdefs = translate(tree.defs);
         
         for (JCTree def : tree.defs) {
             if (def.getTag() == JavafxTag.CLASS_DEF) {
                 List<JCStatement> ret = initBuilder.createJFXClassModel((JFXClassDeclaration)def);
                 for (JCStatement retDef : ret) {
                     tdefs = tdefs.append(retDef);
                 }
             }
         }
         
 	JCExpression pid = translate(tree.pid);
         result = make.at(tree.pos).TopLevel(List.<JCAnnotation>nil(), pid, tdefs);
     }
     
     @Override
     public void visitClassDeclaration(JFXClassDeclaration tree) {
         JFXClassDeclaration prevEnclClass = attrEnv.enclClass;
         JavafxBindStatus prevBindContext = bindContext;
         boolean prevInLHS = inLHS;
         
         attrEnv.enclClass = tree;
         bindContext = JavafxBindStatus.UNBOUND;
         inLHS = false;
         
         ListBuffer<JCBlock> translatedInitBlocks = ListBuffer.<JCBlock>lb();
         ListBuffer<JCTree> translatedDefs = ListBuffer.<JCTree>lb();
         ListBuffer<TranslatedAttributeInfo> attrInfo = ListBuffer.<TranslatedAttributeInfo>lb();
         JCMethodDecl initMethod = null;
         Set<JCNewClass> prevVisitedNews = visitedNewClasses;
 
         List<JCStatement> defs = List.<JCStatement>nil();
         for (JCTree def : tree.defs) {
             if (def.getTag() == JavafxTag.CLASS_DEF) {
                 List<JCStatement> ret = initBuilder.createJFXClassModel((JFXClassDeclaration)def);
                 for (JCStatement retDef : ret) {
 //rgf                    defs = defs.append(retDef);
                     translatedDefs.append(retDef);
                 }
             }
         }
         
 //rgf        for (JCStatement stat : defs) {
 //rgf            tree.defs = tree.defs.append(stat);
 //rgf        }
 
         try {
             visitedNewClasses = new HashSet<JCNewClass>();
             for (JCTree def : tree.defs) {
                 switch(def.getTag()) {
                     case JavafxTag.INIT_DEF: {
                         JFXInitDefinition initDef = (JFXInitDefinition) def;
                         translatedInitBlocks.append(translate(initDef.getBody()));
                         break;
                     }
                     case JavafxTag.VAR_DEF: {
                         JFXVar attrDef = (JFXVar) def;
                         JCTree trans = translate(attrDef);
                         attrInfo.append(new TranslatedAttributeInfo(
                                 attrDef, 
                                 translateVarInit(attrDef), 
                                 translate(attrDef.getOnChanges())));
                         translatedDefs.append(trans);
                         break;
                     }
                     case JavafxTag.FUNCTION_DEF: {
                         JCMethodDecl method = (JCMethodDecl)translate(def);
                         translatedDefs.append(method);
                         if (method.getName() == initBuilder.initializerName) {
                             initMethod = method;
                         }
                         break;
                     }
                     default: {
                         translatedDefs.append(translate(def));
                         break;
                     }
                 }
             }
         } finally {
             visitedNewClasses = prevVisitedNews;
         }
         
         // add the class instance initializer method
         if (initMethod != null) {
             initMethod.body.stats = initBuilder.initializerMethodBody(tree, attrInfo.toList(), translatedInitBlocks.toList());
         } else {
             assert false : "should always be an init method";
         }
 
         if (tree.isModuleClass) {
             // Add main method...
             translatedDefs.append(makeMainMethod());
         }
 
         result = make.ClassDef(tree.mods, tree.name, tree.typarams, tree.extending, tree.implementing, translatedDefs.toList());
 
         attrEnv.enclClass = prevEnclClass;
         bindContext = prevBindContext;
         inLHS = prevInLHS;
     }
     
     @Override
     public void visitInitDefinition(JFXInitDefinition tree) {
         result = null; // Just remove this tree...
     }
 
     @Override
     public void visitPureObjectLiteral(JFXPureObjectLiteral tree) {
         Name tmpName = getSyntheticName("objlit");
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
             if (part == null) {
                 continue;
             }
             
             if (part instanceof JFXObjectLiteralPart) {
                 JFXObjectLiteralPart olpart = (JFXObjectLiteralPart)part;
                 DiagnosticPosition diagPos = olpart.pos();
                 JavafxBindStatus bindStatus = olpart.getBindStatus();
                 JCExpression init = boundTranslate(olpart.getExpression(), bindStatus);
                 VarSymbol vsym = (VarSymbol)olpart.sym;
                 VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
                 if (vmi.shouldMorph()) {
                     init = typeMorpher.translateDefinitionalAssignment(init, vmi, diagPos, bindStatus);
                 }
                 JCIdent ident1 = make.at(diagPos).Ident(tmpName);
                 JCFieldAccess attr = make.at(diagPos).Select(
                         ident1,
                         olpart.getName());
                 JCAssign assign1 = make.at(diagPos).Assign(attr,init);
                 lastStatement = make.at(diagPos).Exec(assign1);
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
 
     public JCExpression translateVarInit(JFXVar tree) {
         DiagnosticPosition diagPos = tree.pos();
         JavafxBindStatus bindStatus = tree.getBindStatus();
         JCExpression init = boundTranslate(tree.init, bindStatus);
         VarSymbol vsym = tree.sym;
         VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
         if (vmi.shouldMorph()) {
             init = typeMorpher.translateDefinitionalAssignment(init, vmi, diagPos, bindStatus);

         }
         return init;
     }
 
     @Override
     public void visitVar(JFXVar tree) {
         DiagnosticPosition diagPos = tree.pos();
         Type type = tree.type;
         JCModifiers mods = tree.getModifiers();
         long modFlags = mods==null? 0L : mods.flags;
         VarSymbol vsym = tree.sym;
         VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
         if (vmi.shouldMorph()) {
             type = vmi.getUsedType();
 
             // local variables need to be final so they can be referenced
             // attributes cannot be final since they are initialized outside of the constructor
            if (vsym.owner.kind == Kinds.TYP) {
                 modFlags &= ~Flags.FINAL;
             } else {
                 modFlags |= Flags.FINAL;  
             }
         }
         mods = make.at(diagPos).Modifiers(modFlags);
         JCExpression typeExpresion = makeTypeTree(type, diagPos);
        result = make.at(diagPos).VarDef(mods, tree.name, typeExpresion, translateVarInit(tree));
     }
 
         // TOD: temp hack
     public void visitAbstractOnChange(JFXAbstractOnChange tree) {
 	translate(tree.getIndex());
 	translate(tree.getOldValue());  
         translate(tree.getBody());
     }
     
     @Override
     public void visitOnReplace(JFXOnReplace tree) {
         result = ((JavafxTreeMaker)make).OnReplace(
                 translate(tree.getOldValue()),
                 translate(tree.getBody()));
     }
     
     @Override
     public void visitOnReplaceElement(JFXOnReplaceElement tree) {
         result = ((JavafxTreeMaker)make).OnReplaceElement(
                 translate(tree.getIndex()),
                 translate(tree.getOldValue()),
                 translate(tree.getBody()));
     }
     
     @Override
     public void visitOnInsertElement(JFXOnInsertElement tree) {
         visitAbstractOnChange(tree);
     }
     
     @Override
     public void visitOnDeleteElement(JFXOnDeleteElement tree) {
         visitAbstractOnChange(tree);
     }
     
     @Override
     public void visitOnDeleteAll(JFXOnDeleteAll tree) {
         visitAbstractOnChange(tree);
     }
     
     /**
      * assume seq is a sequence of element type U
      * convert   for (x in seq where cond) { body }
      * into the following block expression
      * 
      *   {
      *     SequenceBuilder<T> sb = new SequenceBuilder<T>(clazz);
      *     for (U x : seq) {
      *       if (!cond)
      *         continue;
      *       sb.add( { body } );
      *     }
      *     sb.toSequence()
      *   }
      *
      * **/
     @Override
     public void visitForExpression(JFXForExpression tree) {
         // sub-translation in done inline -- no super.visitForExpression(tree);
         DiagnosticPosition diagPos = tree.pos();
         ListBuffer<JCStatement> stmts = ListBuffer.<JCStatement>lb();
         JCStatement stmt;
         JCExpression value;
         
         if (tree.type != syms.voidType) {  // body has value (non-void)
             // Compute the element type from the sequence type
             assert tree.type.getTypeArguments().size() == 1;
             Type elemType = tree.type.getTypeArguments().get(0);
 
             // Build the type declaration expression for the sequence builder
             JCExpression builderTypeExpr = ((JavafxTreeMaker) make).at(diagPos).Identifier(sequenceBuilderString);
             List<JCExpression> btargs = List.<JCExpression>of(makeTypeTree(elemType, diagPos));
             builderTypeExpr = make.at(diagPos).TypeApply(builderTypeExpr, btargs);
 
             // Sequence builder temp var name "sb"
             Name sbName = getSyntheticName("sb");
 
             // Build "sb" initializing expression -- new SequenceBuilder<T>(clazz)
             List<JCExpression> args = List.<JCExpression>of( make.at(diagPos).Select(
                 makeTypeTree(elemType, diagPos), 
                 names._class));               
             JCExpression newExpr = make.at(diagPos).NewClass(
                 null,                               // enclosing
                 List.<JCExpression>nil(),           // type args
                 make.at(diagPos).TypeApply(         // class name -- SequenceBuilder<elemType>
                     ((JavafxTreeMaker)make).at(diagPos).Identifier(sequenceBuilderString), 
                      List.<JCExpression>of(makeTypeTree(elemType, diagPos))),
                 args,                               // args
                 null                                // empty body
                 );
         
             // Build the sequence builder variable
             JCStatement varDef = make.at(diagPos).VarDef( 
                 make.at(diagPos).Modifiers(0L), 
                 sbName, builderTypeExpr, newExpr);
             stmts.append(varDef);
         
             // Build innermost loop body
             JCIdent varIdent = make.Ident(sbName);  
             JCMethodInvocation addCall = make.Apply(
                 List.<JCExpression>nil(), // type arguments
                 make.at(diagPos).Select(varIdent, Name.fromString(names, "add")), 
                 List.<JCExpression>of(translate(tree.getBodyExpression())));
             stmt = make.at(diagPos).Exec(addCall);
         
             // Build the result value
             JCIdent varIdent2 = make.Ident(sbName);  
             value = make.Apply(
                 List.<JCExpression>nil(), // type arguments
                 make.at(diagPos).Select(varIdent2, Name.fromString(names, toSequenceString)), 
                 List.<JCExpression>nil() // arguments
                 );
         } else {
             // void body
             stmt = make.at(diagPos).Exec(translate(tree.getBodyExpression()));
             value = null;  // resulting block expression has no value
         }
         
         for (int inx = tree.getInClauses().size() - 1; inx >= 0; --inx) {
             JFXForExpressionInClause clause = tree.getInClauses().get(inx);
             if (clause.getWhereExpression() != null) {
                 stmt = make.at(diagPos).If(clause.getWhereExpression(), stmt, null);
             }
 
             // Build the loop
             stmt = make.at(diagPos).ForeachLoop(
                     translate(clause.getVar()), 
                     translate(clause.getSequenceExpression()),
                     stmt);
         }
         stmts.append(stmt);
         
         // Build the block expression -- which is what we translate to
         result = ((JavafxTreeMaker)make).at(diagPos).BlockExpression(0L, stmts.toList(), value);
     }
     
     @Override
     public void visitOperationDefinition(JFXOperationDefinition tree) {
         DiagnosticPosition diagPos = tree.pos();
         MethodType mtype = (MethodType)tree.type;
         JFXBlockExpression bexpr = tree.getBodyExpression();
         JCBlock body = null; // stays null if no block expression
         if (bexpr != null) {
             List<JCStatement> stats = translate(bexpr.stats);
             if (bexpr.value != null) {
                 JCStatement valueStatement;
                 JCExpression value = translate(bexpr.value);
                 if (mtype.getReturnType() == syms.voidType) {
                     valueStatement = make.at(diagPos).Exec(value);
                 } else {
                     valueStatement = make.at(diagPos).Return(value);
                 }
                 stats = stats.append(valueStatement);
             }
             body = make.at(diagPos).Block(0, stats);
         }
         ListBuffer<JCVariableDecl> params = ListBuffer.<JCVariableDecl>lb();
         for (JFXVar fxVar : tree.getParameters()) {
             params.append(translate(fxVar));
         }
         result = make.at(diagPos).MethodDef(
                 translate(tree.mods),
                 tree.name, 
                 makeTypeTree(mtype.getReturnType(), diagPos), 
                 make.at(diagPos).TypeParams(mtype.getTypeArguments()), 
                 params.toList(),
                 make.at(diagPos).Types(mtype.getThrownTypes()),
                 body, 
                 null);
     }
 
     public void visitBlockExpression(JFXBlockExpression tree) {
         List<JCStatement> defs = translateStatements(tree.stats);
         for (JCTree def : tree.stats) {
             if (def.getTag() == JavafxTag.CLASS_DEF) {
                 List<JCStatement> ret = initBuilder.createJFXClassModel((JFXClassDeclaration)def);
                 for (JCStatement retDef : ret) {
                     defs = defs.append(retDef);
                 }
             }
         }
 	result = ((JavafxTreeMaker)make).at(tree.pos).BlockExpression(
                 tree.flags, 
                 defs, 
                 translate(tree.value));
     }
  
     @Override
     public void visitBlock(JCBlock tree) {
         List<JCStatement> defs = translateStatements(tree.stats);
         for (JCTree def : tree.stats) {
             if (def.getTag() == JavafxTag.CLASS_DEF) {
                 List<JCStatement> ret = initBuilder.createJFXClassModel((JFXClassDeclaration)def);
                 for (JCStatement retDef : ret) {
                     defs = defs.append(retDef);
                 }
             }
         }       
 	result = make.at(tree.pos).Block(tree.flags, defs);
     }
 
     @Override
     public void visitInstanciate(JFXInstanciate tree) {
         List<JCExpression> emptyTypeArgs = List.nil();
 
         JCExpression encl = translate(tree.encl);
 	JCExpression clazz = translate(tree.clazz);
 	List<JCExpression> args = translate(tree.args);
 	JCClassDecl def = null; assert tree.def == null : "until we clone Instanciate, must be null";
 	result = make.NewClass(encl, emptyTypeArgs, clazz, args, def);
 
         Symbol initSym = getJavafxInitializerSymbol(tree);
         if (initSym != null) {
             Name tmpName = getSyntheticName("init");
             ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
 
             JCVariableDecl tmpVar = make.at(tree.pos).VarDef(make.Modifiers(0), tmpName, tree.clazz, tree);
             stats.append(tmpVar);
 
             List<JCExpression> emptyArgs = List.nil();
 
             JCIdent ident3 = make.Ident(tmpName);
             JCFieldAccess select1 = make.at(tree.pos).Select(ident3, initBuilder.initializerName);
             JCMethodInvocation apply1 = make.at(tree.pos).Apply(emptyTypeArgs, select1, emptyArgs);
             JCExpressionStatement exec1 = make.at(tree.pos).Exec(apply1);
             stats.append(exec1);
 
             JCIdent ident2 = make.Ident(tmpName);
             JFXBlockExpression blockExpr1 = ((JavafxTreeMaker) make).at(tree.pos).BlockExpression(0, stats.toList(), ident2);
             result = blockExpr1;
         }
     }
     
     @Override
     public void visitAssign(JCAssign tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression rhs = translate(tree.rhs);
         if (tree.lhs.getTag() == JavafxTag.SEQUENCE_INDEXED) {
             // assignment of a sequence element --  s[i]=8
             JFXSequenceIndexed si = (JFXSequenceIndexed)tree.lhs;
             JCExpression seq = translateLHS(si.getSequence(), true);  // LHS?
             JCExpression index = translate(si.getIndex());
             result = typeMorpher.morphSequenceIndexedAssign(diagPos, seq, index, rhs);
         } else {
             JCExpression lhs = translateLHS(tree.lhs, true);
             result = typeMorpher.morphAssign(diagPos, lhs, rhs);
         }
     }
 
     @Override
     public void visitSelect(JCFieldAccess tree) {
         // this may or may not be in a LHS but in either
         // event the selector is a value expression
         tree.selected = translateLHS(tree.selected, false);
         
         result = typeMorpher.convertVariableReference(tree, tree.sym, bindContext, inLHS);
     }
     
     @Override
     public void visitIdent(JCIdent tree)   {
         result = typeMorpher.convertVariableReference(tree, tree.sym, bindContext, inLHS);
     }
 
     @Override
     public void visitSequenceExplicit(JFXSequenceExplicit tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression meth = ((JavafxTreeMaker)make).at(diagPos).Identifier(sequencesMakeString);
         Type elemType = tree.type.getTypeArguments().get(0);
         ListBuffer<JCExpression> args = ListBuffer.<JCExpression>lb();
         List<JCExpression> typeArgs = List.<JCExpression>of(makeTypeTree(elemType, diagPos));
         // type name .class
         args.append(make.at(diagPos).Select(makeTypeTree(elemType, diagPos), names._class));
         args.appendList( translate( tree.getItems() ) );
         result = make.at(diagPos).Apply(typeArgs, meth, args.toList());
     }
     
     @Override
     public void visitSequenceRange(JFXSequenceRange tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression meth = ((JavafxTreeMaker)make).at(diagPos).Identifier(sequencesRangeString);
         ListBuffer<JCExpression> args = ListBuffer.<JCExpression>lb();
         List<JCExpression> typeArgs = List.<JCExpression>nil();
         args.append( translate( tree.getLower() ));
         args.append( translate( tree.getUpper() ));
         result = make.at(diagPos).Apply(typeArgs, meth, args.toList());
     }
     
     @Override
     public void visitSequenceIndexed(JFXSequenceIndexed tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression seq = translateLHS(tree.getSequence(), true);  // LHS?
         JCExpression index = translate(tree.getIndex());
         result = typeMorpher.morphSequenceIndexedAccess(diagPos, seq, index);
     }
 
     @Override
     public void visitSequenceInsert(JFXSequenceInsert tree) {
         result = callStatement(tree, 
                 translateLHS( tree.getSequence() ), 
                 "insert", 
                 translate( tree.getElement() ));
     }
     
     @Override
     public void visitSequenceDelete(JFXSequenceDelete tree) {
         if (tree.getIndex() != null) { 
             result = callStatement(tree, 
                 translateLHS( tree.getSequence() ), 
                 "delete", 
                 translate( tree.getIndex() ));
         } else if (tree.getElement() != null) { 
             result = callStatement(tree, 
                 translateLHS( tree.getSequence() ), 
                 "deleteValue", 
                 translate( tree.getElement() ));
         } else { 
             result = callStatement(tree, 
                 translateLHS( tree.getSequence() ), 
                 "deleteAll");
         }
     }
 
     /**** utility methods ******/
     
     JCStatement callStatement(
             JCTree treeForPos,
             JCExpression receiver, 
             String method) {
         return callStatement(treeForPos, receiver, method, List.<JCExpression>nil());
     }
     
     JCStatement callStatement(
             JCTree treeForPos,
             JCExpression receiver, 
             String method, 
             JCExpression arg) {
         return callStatement(treeForPos, receiver, method, List.<JCExpression>of(arg));
     }
     
     JCStatement callStatement(
             JCTree treeForPos,
             JCExpression receiver, 
             String method, 
             List<JCExpression> args) {
         DiagnosticPosition diagPos = treeForPos.pos();
         Name methodName = names.fromString(method);
         JCFieldAccess select = make.at(diagPos).Select(receiver, methodName);
         JCExpression apply = make.at(diagPos).Apply(null, select, args);
         return make.at(diagPos).Exec(apply);
     }
     
     private Name getSyntheticName(String kind) {
         return Name.fromString(names, syntheticNamePrefix + syntheticNameCounter++ + kind);
     }
 
     JCMethodDecl makeMainMethod() {
             List<JCExpression> emptyExpressionList = List.nil();
             JCIdent runIdent = make.Ident(Name.fromString(names, JavafxModuleBuilder.runMethodString));
             JCMethodInvocation runCall = make.Apply(emptyExpressionList, runIdent, emptyExpressionList);
             List<JCStatement> mainStats = List.<JCStatement>of(make.Exec(runCall));
             List<JCVariableDecl> paramList = List.nil();
             paramList = paramList.append(make.VarDef(make.Modifiers(0), 
                     Name.fromString(names, "args"), 
                     make.TypeArray(make.Ident(Name.fromString(names, "String"))), 
                     null));
             JCBlock body = make.Block(0, mainStats);
             return make.MethodDef(make.Modifiers(Flags.PUBLIC | Flags.STATIC),
                     Name.fromString(names, "main"), 
                     make.TypeIdent(TypeTags.VOID), 
                     List.<JCTypeParameter>nil(), 
                     paramList, 
                     emptyExpressionList, 
                     body, 
                     null);
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
 
     /**
      * JCTrees which can just be copied and trees which sjould not occur 
      * */
     
     public void visitAnnotation(JCAnnotation tree) {
         JCTree annotationType = translate(tree.annotationType);
         List<JCExpression> args = translate(tree.args);
         result = make.at(tree.pos).Annotation(annotationType, args);
     }
 
     public void visitAssert(JCAssert tree) {
         JCExpression cond = translate(tree.cond);
         JCExpression detail = translate(tree.detail);
         result = make.at(tree.pos).Assert(cond, detail);
     }
 
     public void visitAssignop(JCAssignOp tree) {
         JCTree lhs = translate(tree.lhs);
         JCTree rhs = translate(tree.rhs);
         result = make.at(tree.pos).Assignop(tree.getTag(), lhs, rhs);
     }
 
     public void visitBinary(JCBinary tree) {
         JCExpression lhs = translate(tree.lhs);
         JCExpression rhs = translate(tree.rhs);
         result = make.at(tree.pos).Binary(tree.getTag(), lhs, rhs);
     }
 
     public void visitBreak(JCBreak tree) {
         result = make.at(tree.pos).Break(tree.label);
     }
 
    public void visitCase(JCCase tree) {
         JCExpression pat = translate(tree.pat);
         List<JCStatement> stats = translate(tree.stats);
         result = make.at(tree.pos).Case(pat, stats);
     }
 
     public void visitCatch(JCCatch tree) {
         JCVariableDecl param = translate(tree.param);
         JCBlock body = translate(tree.body);
         result = make.at(tree.pos).Catch(param, body);
     }
 
     public void visitClassDef(JCClassDecl tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitConditional(JCConditional tree) {
         JCExpression cond = translate( tree.getCondition() );
         JCExpression trueSide = translate( tree.getTrueExpression() );
         JCExpression falseSide = translate( tree.getFalseExpression() );
         result = make.at(tree.pos).Conditional(cond, trueSide, falseSide);
     }
 
     public void visitContinue(JCContinue tree) {
         result = make.at(tree.pos).Continue(tree.label);
     }
 
     public void visitDoLoop(JCDoWhileLoop tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitErroneous(JCErroneous tree) {
         List<? extends JCTree> errs = translate(tree.errs);
         result = make.at(tree.pos).Erroneous(errs);
     }
 
     public void visitExec(JCExpressionStatement tree) {
         JCExpression expr = translate(tree.expr);
         result = make.at(tree.pos).Exec(expr);
     }
 
     public void visitForeachLoop(JCEnhancedForLoop tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitForLoop(JCForLoop tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitIf(JCIf tree) {
         assert false : "should not be in JavaFX AST";
 /***
         JCExpression cond = translate(tree.cond);
         JCStatement thenpart = translate(tree.thenpart);
         JCStatement elsepart = translate(tree.elsepart);
         result = make.at(tree.pos).If(cond, thenpart, elsepart);
 ****/
     }
 
     public void visitImport(JCImport tree) {
         JCTree qualid = translate(tree.qualid);
         result = make.at(tree.pos).Import(qualid, tree.staticImport);
     }
 
     public void visitIndexed(JCArrayAccess tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitTypeTest(JCInstanceOf tree) {
         JCExpression expr = translate(tree.expr);
         JCTree clazz = translate(tree.clazz);
         result = make.at(tree.pos).TypeTest(expr, clazz);
     }
 
     public void visitLabelled(JCLabeledStatement tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitLiteral(JCLiteral tree) {
         result = make.at(tree.pos).Literal(tree.typetag, tree.value);
     }
 
     public void visitMethodDef(JCMethodDecl tree) {
          assert false : "should not be in JavaFX AST";
    }
 
     public void visitApply(JCMethodInvocation tree) {
         List<JCExpression> typeargs = translate(tree.typeargs);
         JCExpression meth = translate(tree.meth);
         List<JCExpression> args = translate(tree.args);
         result = make.at(tree.pos).Apply(typeargs, meth, args);
     }
 
     public void visitModifiers(JCModifiers tree) {
         List<JCAnnotation> annotations = translate(tree.annotations);
         result = make.at(tree.pos).Modifiers(tree.flags, annotations);
     }
 
     public void visitNewArray(JCNewArray tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitNewClass(JCNewClass tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitParens(JCParens tree) {
         JCExpression expr = translate(tree.expr);
         result = make.at(tree.pos).Parens(expr);
     }
 
     public void visitReturn(JCReturn tree) {
         JCExpression expr = translate(tree.expr);
         result = make.at(tree.pos).Return(expr);
     }
 
     public void visitSkip(JCSkip tree) {
         result = make.at(tree.pos).Skip();
     }
 
     public void visitSwitch(JCSwitch tree) {
         JCExpression selector = translate(tree.selector);
         List<JCCase> cases = translate(tree.cases);
         result = make.at(tree.pos).Switch(selector, cases);
     }
 
     public void visitSynchronized(JCSynchronized tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitThrow(JCThrow tree) {
         JCTree expr = translate(tree.expr);
         result = make.at(tree.pos).Throw(expr);
     }
 
     public void visitTry(JCTry tree) {
         JCBlock body = translate(tree.body);
         List<JCCatch> catchers = translate(tree.catchers);
         JCBlock finalizer = translate(tree.finalizer);
         result = make.at(tree.pos).Try(body, catchers, finalizer);
     }
 
     public void visitTypeApply(JCTypeApply tree) {
         JCExpression clazz = translate(tree.clazz);
         List<JCExpression> arguments = translate(tree.arguments);
         result = make.at(tree.pos).TypeApply(clazz, arguments);
     }
 
     public void visitTypeArray(JCArrayTypeTree tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitTypeBoundKind(TypeBoundKind tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitTypeCast(JCTypeCast tree) {
         JCTree clazz = translate(tree.clazz);
         JCExpression expr = translate(tree.expr);
         result = make.at(tree.pos).TypeCast(clazz, expr);
     }
 
     public void visitTypeIdent(JCPrimitiveTypeTree tree) {
         result = make.at(tree.pos).TypeIdent(tree.typetag);
     }
 
     public void visitTypeParameter(JCTypeParameter tree) {
         List<JCExpression> bounds = translate(tree.bounds);
         result = make.at(tree.pos).TypeParameter(tree.name, tree.bounds);
     }
 
     public void visitUnary(JCUnary tree) {
         JCExpression arg = translate(tree.arg);
         result = make.at(tree.pos).Unary(tree.getTag(), arg);
     }
 
     public void visitVarDef(JCVariableDecl tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitWhileLoop(JCWhileLoop tree) {
         JCStatement body = translate(tree.body);
         JCExpression cond = translate(tree.cond);
         result = make.at(tree.pos).WhileLoop(cond, body);
     }
 
     public void visitWildcard(JCWildcard tree) {
         TypeBoundKind kind = make.at(tree.kind.pos).TypeBoundKind(tree.kind.kind);
         JCTree inner = translate(tree.inner);
         result = make.at(tree.pos).Wildcard(kind, inner);
     }
 
     public void visitLetExpr(LetExpr tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     public void visitTree(JCTree tree) {
         assert false : "should not be in JavaFX AST";
     }
     
     /******** goofy visitors, most of which should go away ******/
 
     public void visitDoLater(JFXDoLater that) {
         that.body = translate(that.body);
         result = that;
     }
 
     public void visitMemberSelector(JFXMemberSelector that) {
         result = that;
     }
     
     public void visitSequenceEmpty(JFXSequenceEmpty that) {
         result = that;
     }
         
     public void visitSetAttributeToObjectBeingInitialized(JFXSetAttributeToObjectBeingInitialized that) {
         result = that;
     }
     
     public void visitObjectLiteralPart(JFXObjectLiteralPart that) {
         that.expr = translate(that.expr);
         result = that;
     }  
     
     public void visitTypeAny(JFXTypeAny that) {
         result = that;
     }
     
     public void visitTypeClass(JFXTypeClass that) {
         result = that;
     }
     
     public void visitTypeFunctional(JFXTypeFunctional that) {
         that.params = translate(that.params);
         that.restype = translate(that.restype);
         result = that;
     }
     
     public void visitTypeUnknown(JFXTypeUnknown that) {
         result = that;
     }
 
     public void visitForExpressionInClause(JFXForExpressionInClause that) {
         that.var = translate(that.var);
         that.seqExpr = translate(that.seqExpr);
         that.whereExpr = translate(that.whereExpr);
         result = that;
     }
     
     protected void prettyPrint(JCTree node) {
         OutputStreamWriter osw = new OutputStreamWriter(System.out);
         JavafxPretty pretty = new JavafxPretty(osw, false);
         try {
             pretty.printExpr(node);
             osw.flush();
         } catch (Exception ex) {
             System.err.println("Error in pretty-printing: " + ex);
         }
     }
 
 }
 
 
