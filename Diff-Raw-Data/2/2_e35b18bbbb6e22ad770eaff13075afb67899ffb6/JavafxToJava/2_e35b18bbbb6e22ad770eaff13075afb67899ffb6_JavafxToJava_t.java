 /*
  * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  
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
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 import javax.lang.model.type.TypeKind;
 
 import com.sun.javafx.api.JavafxBindStatus;
 import com.sun.javafx.api.tree.SequenceSliceTree;
 import com.sun.tools.javac.code.*;
 import static com.sun.tools.javac.code.Flags.*;
 import com.sun.tools.javac.code.Type.*;
 import com.sun.tools.javac.jvm.Target;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.*;
 import com.sun.tools.javac.tree.TreeInfo;
 import com.sun.tools.javac.tree.TreeMaker;
 import com.sun.tools.javac.util.*;
 import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
 import com.sun.tools.javafx.code.FunctionType;
 import com.sun.tools.javafx.code.JavafxFlags;
 import com.sun.tools.javafx.code.JavafxSymtab;
 import com.sun.tools.javafx.code.JavafxTypes;
 import static com.sun.tools.javafx.code.JavafxVarSymbol.*;
 import static com.sun.tools.javafx.comp.JavafxDefs.*;
 import com.sun.tools.javafx.comp.JavafxInitializationBuilder.JavafxClassModel;
 import com.sun.tools.javafx.comp.JavafxAnalyzeClass.TranslatedAttributeInfo;
 import com.sun.tools.javafx.comp.JavafxAnalyzeClass.TranslatedOverrideAttributeInfo;
 import com.sun.tools.javafx.comp.JavafxTypeMorpher.TypeMorphInfo;
 import com.sun.tools.javafx.comp.JavafxTypeMorpher.VarMorphInfo;
 import com.sun.tools.javafx.tree.*;
 
 /**
  * Translate JavaFX ASTs into Java ASTs
  * 
  * @author Robert Field
  * @author Per Bothner
  * @author Lubo Litchev
  */
 public class JavafxToJava extends JavafxTranslationSupport implements JavafxVisitor {
     protected static final Context.Key<JavafxToJava> jfxToJavaKey =
         new Context.Key<JavafxToJava>();
 
     /*
      * modules imported by context
      */
     private final JavafxToBound toBound;  
     private final JavafxInitializationBuilder initBuilder;
     
     private final JCExpression doNotInitializeMarker;
     
     /*
      * Buffers holding definitions waiting to be prepended to the current list of definitions.
      * At class or top-level these are the same.  
      * Within a method (or block) prependToStatements is split off.
      * They need to be different because anonymous classes need to be declared in the scope of the method,
      * but interfaces can't be declared here.
      */
     private ListBuffer<JCStatement> prependToDefinitions = null;
     private ListBuffer<JCStatement> prependToStatements = null;
     
     private ListBuffer<JCExpression> additionalImports = null;
 
     public enum Wrapped {
         InLocation,
         InNothing
     }
 
     Wrapped wrap = Wrapped.InNothing;
 
     
     // for type morphing
     enum Yield {
         ToExpression,
         ToReturnStatement,
         ToExecStatement
     }
 
     //TODO: all these should, probably, go into a translation wrap class
     Yield yield = Yield.ToExpression;
     
     private JavafxEnv<JavafxAttrContext> attrEnv;
     private Target target;
 
     abstract static class Translator {
 
         protected DiagnosticPosition diagPos;
         protected final JavafxToJava toJava;
         protected final JavafxDefs defs;
         protected final JavafxTypes types;
         protected final JavafxSymtab syms;
 
         Translator(DiagnosticPosition diagPos, JavafxToJava toJava) {
             this.diagPos = diagPos;
             this.toJava = toJava;
             this.defs = toJava.defs;
             this.types = toJava.types;
             this.syms = toJava.syms;
         }
 
         protected abstract JCTree doit();
 
         protected TreeMaker m() {
             return toJava.make.at(diagPos);
         }
 
         /**
          * Convert type to JCExpression
          */
         protected JCExpression makeExpression(Type type) {
             return toJava.makeTypeTree(diagPos, type, true);
         }
     }
     
     /*
      * static information
      */
     static final boolean generateNullChecks = true;
     
     private static final String sequencesRangeString = "com.sun.javafx.runtime.sequence.Sequences.range";
     private static final String sequencesRangeExclusiveString = "com.sun.javafx.runtime.sequence.Sequences.rangeExclusive";
     private static final String sequenceBuilderString = "com.sun.javafx.runtime.sequence.SequenceBuilder";
     private static final String boundSequenceBuilderString = "com.sun.javafx.runtime.sequence.BoundSequenceBuilder";
     private static final String toSequenceString = "toSequence";
     private static final String methodThrowsString = "java.lang.Throwable";
     private JFXClassDeclaration currentClass;  //TODO: this is redundant with attrEnv.enclClass
 
     /** Class symbols for classes that need a reference to the outer class. */
     Set<ClassSymbol> hasOuters = new HashSet<ClassSymbol>();
     
     private Set<VarSymbol> locallyBound = new HashSet<VarSymbol>();
     
     private static final Pattern EXTENDED_FORMAT_PATTERN = Pattern.compile("%[<$0-9]*[tT][guwxGUVWXE]");
     
     public static JavafxToJava instance(Context context) {
         JavafxToJava instance = context.get(jfxToJavaKey);
         if (instance == null)
             instance = new JavafxToJava(context);
         return instance;
     }
 
     protected JavafxToJava(Context context) {
         super(context);
         
         context.put(jfxToJavaKey, this);
 
         toBound = JavafxToBound.instance(context);
         initBuilder = JavafxInitializationBuilder.instance(context);
         target = Target.instance(context);
         
         doNotInitializeMarker = make.Literal(TypeTags.INT, 666);
     }
     
     /** Visitor method: Translate a single node.
      */
     @SuppressWarnings("unchecked")
     public <T extends JCTree> T translate(T tree) {
         T ret;
         Yield prevYield = yield;
         yield = Yield.ToExpression; // reset to default
 	if (tree == null) {
 	    ret = null;
 	} else {
 	    tree.accept(this);
 	    ret = (T)this.result;
 	    this.result = null;
 	}
         yield = prevYield;
         return ret;
     }
     
     public JCExpression translate(JCExpression tree, Type type) {
         if (tree == null)
             return null;
         return convertTranslated(translate(tree), tree.pos(), tree.type, type);
     }
     
     public JCExpression convertTranslated(JCExpression translated, DiagnosticPosition diagPos,
             Type sourceType, Type type) {
         if (types.isSequence(sourceType) && types.isArray(type)) {
             ListBuffer<JCStatement> stats = ListBuffer.lb();
             Type elemType = types.elemtype(type);
             if (elemType.isPrimitive()) {
                 String mname = "toArray";
                 if (elemType == syms.floatType)
                     mname = "toFloatArray";
                 return callExpression(diagPos, makeTypeTree( diagPos,syms.javafx_SequencesType, false),
                        mname, translated);
             }
             JCVariableDecl tmpVar = makeTmpVar(diagPos, sourceType, translated);
             stats.append(tmpVar);
             JCVariableDecl arrVar = makeTmpVar(diagPos, "arr", type, 
                     make.NewArray(makeTypeTree(diagPos, elemType, true),
                         List.<JCExpression>of(callExpression(diagPos, make.Ident(tmpVar.name), "size")), null));
             stats.append(arrVar);
             stats.append(callStatement(diagPos, make.Ident(tmpVar.name), "toArray",
                             List.of(make.Ident(arrVar.name), make.Literal(TypeTags.INT, 0))));
             JCIdent ident2 = make.Ident(arrVar.name);
             return makeBlockExpression(diagPos, stats, ident2);
             
         }
         if (types.isArray(sourceType) && types.isSequence(type)) {
             Type elemType = types.elemtype(sourceType);
             String mname = "fromArray";
             if (elemType.isPrimitive())
                 return callExpression(diagPos, makeTypeTree( diagPos,syms.javafx_SequencesType, false),
                        mname, translated);
             else {
                 List<JCExpression> args = 
                         List.of(make.at(diagPos).Select(makeTypeTree(diagPos, elemType, true), names._class),
                         translated);
                 return callExpression(diagPos, makeTypeTree( diagPos,syms.javafx_SequencesType, false),
                        mname, args);
             }
         }
         if (types.isSequence(type) && ! types.isSequence(sourceType)) {
             return callExpression(diagPos,
                     makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"),
                     "singleton",
                     List.of(makeElementClassObject(diagPos, types.elementType(type)),
                             translated));
         }        
 
         if (sourceType == syms.javafx_IntegerType ||
                 type == syms.javafx_IntegerType) {
             if (sourceType != type && type.isPrimitive()) {
                 translated = make.at(diagPos).TypeCast(type, translated);
             }
         }
 
         return translated;
     }
 
     /** Visitor method: translate a list of nodes.
      */
     public <T extends JCTree> List<T> translate(List<T> trees) {
         ListBuffer<T> translated = ListBuffer.lb();
 	if (trees == null) return null;
 	for (List<T> l = trees; l.nonEmpty();  l = l.tail) {
             T tree = translate(l.head);
             if (tree != null) {
                 translated.append( tree );
             }
         }
 	return translated.toList();
     }
     
      public List<JCExpression> translate(List<JCExpression> trees, List<Type> types) {
         ListBuffer<JCExpression> translated = ListBuffer.lb();
 	if (trees == null) return null;
         List<Type> t = types;
 	for (List<JCExpression> l = trees; l.nonEmpty();  l = l.tail, t = t.tail) {
             JCExpression tree = translate(l.head, t.head);
             if (tree != null) {
                 translated.append( tree );
             }
         }
 	return translated.toList();
     }
      
      /**
      * Translate a list of statements, 
      * The purpose of this method is to prepend an anonymous class definition in the correct
      * scope.  However, that is currently disabled since generated classes define static members
      * and that isn't allowed by an (unmodified) javac back-end.
      * */
     private List<JCStatement> translateStatements(List<JCStatement> stmts) {
         ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
         prependToStatements = ListBuffer.lb();
         List<JCStatement> translatedStats = translate(stmts);
         translatedStats = translatedStats.prependList(prependToStatements.toList());
         prependToStatements = prevPrependToStatements;
         return translatedStats;
     }
     
     //// Translation with wrap transition
     
     public <T extends JCTree> T translate(T tree, Wrapped newState) {
         Wrapped prevWrap = wrap;
         wrap = newState;
         T ret = translate(tree);
         wrap = prevWrap;
         return ret;
     }
 
     JCStatement translateExpressionToStatement(JCExpression expr) {
         assert yield != Yield.ToExpression;
 	if (expr == null) {
 	    return null;
 	} else {
 	    expr.accept(this);
 	    JCTree ret = this.result;
 	    this.result = null;
 	    return ret instanceof JCStatement? 
                 (JCStatement)ret  // already converted
                 : toCorrectReturn(expr, (JCExpression)ret );
 	}
     }
 
     JCStatement translateExpressionToStatement(JCExpression expr, Wrapped newState, Type targetType) {
         Wrapped prevWrap = wrap;
         Yield prevYield = yield;
         wrap = newState;
         JCStatement ret;
         if (targetType == expr.type || targetType==syms.voidType
                 || targetType == null || expr.type == syms.unreachableType) {
             yield = targetType==syms.voidType? Yield.ToExecStatement : Yield.ToReturnStatement;
             ret = translateExpressionToStatement(expr);
         } else {
             yield = Yield.ToExpression;
             ret = make.at(expr).Return(translate(expr, targetType));
         }
         yield = prevYield;
         wrap = prevWrap;
         return ret;
     }
 
     JCStatement translateExpressionToStatement(JCExpression expr, Type targetType) {
         return translateExpressionToStatement(expr, wrap,  targetType);
     }
 
     void setLocallyBound(VarSymbol vsym) {
         locallyBound.add(vsym);
     }
 
     JCBlock asBlock(JCStatement stmt) {
         if (stmt.getTag() == JavafxTag.BLOCK) {
             return (JCBlock)stmt;
         } else {
             return make.at(stmt).Block(0L, List.of(stmt));
         }
     }
     
     JCStatement toCorrectReturn(JCExpression expr, JCExpression translated) {
         switch (yield) {
             case ToExecStatement:
                 return make.at(expr).Exec( translated );
             case ToReturnStatement:
                 return make.at(expr).Return( translated );
             case ToExpression:
             default:
                 assert false : "this method should not be called";
                 return null;
         }
     }
     
     public void toJava(JavafxEnv<JavafxAttrContext> attrEnv) {
         this.attrEnv = attrEnv;
         
         attrEnv.toplevel = translate(attrEnv.toplevel);
     }
 
     /**
      * Fixup up block before translation.
      * For now this is only done for the run methods's block, and all we do is
      * convert to static variables that are accessed by other methods or classes.
      * In the future, I also want to:
      * (a) pre-allocate variables that need morphing ta the top of the block,
      * so we can do forward references.
      * (b) If a variable is final and initialized by an object literal,
      * invoke the Java constructor first, so we can make cyclic data structures. 
      * 
      * @param body The block to mogrify.
      * @param module If non-null, the module class whose body this is.
      */
     public void fixupBlockExpression (JFXBlockExpression body, JFXClassDeclaration module) {
         boolean changed = false;
         ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
 
         for (JCStatement stat : body.stats) {
             if (stat instanceof JFXVar) {
                 JFXVar decl = (JFXVar) stat;
                 if ((decl.sym.flags_field & JavafxFlags.INNER_ACCESS) != 0) {
                    decl.sym.flags_field |= STATIC;
                    changed = true;
                    JCExpression init = decl.init;
                    decl.sym.owner = module.type.tsym;
                    if (init != null) {
                         Name name = decl.name;
                         make.at(decl);
                         JavafxBindStatus bindStatus = decl.getBindStatus();
                         if (bindStatus != JavafxBindStatus.UNBOUND)
                             init = fxmake.BindExpression(init, bindStatus);
                         JCIdent lhs = make.Ident(name);
                         lhs.sym = decl.sym;
                         stats.append(make.Exec(make.Assign(lhs, init)));
                         decl.init = doNotInitializeMarker;
                     }
                    module.setMembers(module.getMembers().prepend(decl));
                    continue;
                 }
             }
             stats.append(stat);
         }
         if (changed) {
             body.stats = stats.toList();
         }
     }
 
     /**
      * Fixup up block before translation.
      * For now this is only done for the run methods's block, and all we do is
      * convert to static variables that are accessed by other methods, classes,
      * or attribute initializers (including those being moved out)
      * 
      * @param body The block to mogrify.
      * @param module If non-null, the module class whose body this is.
      */
     /**********************
     public void fixupBlockExpression (JFXBlockExpression body, JFXClassDeclaration module) {
         final Map<VarSymbol, JFXVar> toModule = new HashMap<VarSymbol, JFXVar>(); //vars to move to module level
         final Map<VarSymbol, JFXVar> candidate = new HashMap<VarSymbol, JFXVar>(); //other top-level vars
         
         // scanner that moves any top-level var to the module if it is
         // referenced in the initializer of a var moved to the module
         final JavafxTreeScanner scanner = new JavafxTreeScanner() {
 
             @Override
             public void visitIdent(JCIdent tree) {
                 if (tree.sym instanceof VarSymbol) {
                     VarSymbol vsym = (VarSymbol) tree.sym;
                     JFXVar usedVar = candidate.get(vsym);
                     if (usedVar != null) {
                         candidate.remove(vsym);
                         toModule.put(vsym, usedVar);
                         scan(usedVar.getInitializer());
                     }
                 }
             }
         };
 
         // top-level variables that have inner references are marked for moving to the module
         // all others are candidates for movement
         for (JCStatement stat : body.stats) {
             if (stat instanceof JFXVar) {
                 JFXVar decl = (JFXVar) stat;
                 if ((decl.sym.flags_field & JavafxFlags.INNER_ACCESS) != 0) {
                     toModule.put(decl.sym, decl);  // if they have
                 } else {
                     candidate.put(decl.sym, decl);
                 }
             }
         }
         
         // look for top-level vars referenced in the initializer of those to be moved
         // clone to avoid concurrent modification
         for (JFXVar var : toModule.values().toArray(new JFXVar[toModule.values().size()])) {
             scanner.scan(var);
         }
         
         if (toModule.size() > 0) {
             // there are variables to move to the module -- move them
             ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
             for (JCStatement stat : body.stats) {
                 JFXVar var;
                 VarSymbol vsym;
                 if (stat instanceof JFXVar && toModule.containsKey(vsym = (var = (JFXVar) stat).sym)) {
                     // this is a variable marked to be moved to the module -- do so
                     vsym.flags_field |= STATIC;
                     vsym.owner = module.type.tsym;
                     module.setMembers(module.getMembers().prepend(var));
                 } else {
                     // all other statements remain
                     stats.append(stat);
                 }
             }
             body.stats = stats.toList();
         }
     }
      * ****/
 
     @Override
     public void visitTopLevel(JCCompilationUnit tree) {
         for (JCTree def : tree.defs) {
             if (def instanceof JFXClassDeclaration) {
                 JFXClassDeclaration cdecl = (JFXClassDeclaration) def;
                 JFXFunctionDefinition runMethod = cdecl.runMethod;
                 if (runMethod != null) {
                     fixupBlockExpression(runMethod.getBodyExpression(), cdecl);
                 }
             }
         }
         // add to the hasOuters set the clas symbols for classes that need a reference to the outer class
         fillClassesWithOuters(tree);
         
        ListBuffer<JCTree> translatedDefinitions= ListBuffer.lb();
        ListBuffer<JCTree> imports= ListBuffer.lb();
        additionalImports = ListBuffer.lb();
        prependToStatements = prependToDefinitions = ListBuffer.lb();
        for (JCTree def : tree.defs) {
             if (def.getTag() == JCTree.IMPORT) {
                 imports.append(def);
                  if (!((JCImport)def).isStatic()) {
                     if (((JCImport)def).getQualifiedIdentifier().getTag() == JCTree.SELECT) {
                         JCFieldAccess select = (JCFieldAccess)((JCImport)def).getQualifiedIdentifier();
                         if (select.name != names.asterisk &&
                                 types.isCompoundClass(select.sym)) {
                            imports.append(make.Import(make.Select(
                                         translate( select.selected ), 
                                         names.fromString(select.name.toString() + interfaceSuffix)), 
                                    false));
                         }
                     }  else if (((JCImport)def).getQualifiedIdentifier().getTag() == JCTree.IDENT) {
                         JCIdent ident = (JCIdent)((JCImport)def).getQualifiedIdentifier();
                         if (ident.name != names.asterisk &&
                                 types.isCompoundClass(ident.sym)) {
                             imports.append(make.Import(make.Ident(names.fromString(ident.name.toString() + interfaceSuffix)), false));
                         }
                     }
                 }
             } else {
                 // anything but an import
                 translatedDefinitions.append( translate(def) );
             }
         }
 
        // order is imports, any prepends, then the translated non-imports
         for (JCTree prepend : prependToDefinitions) {
             translatedDefinitions.prepend(prepend);
         }
        
         for (JCTree prepend : imports) {
             translatedDefinitions.prepend(prepend);
         }
         
         for (JCExpression prepend : additionalImports) {
             translatedDefinitions.append(make.Import(prepend, false));
         }
 
         prependToStatements = prependToDefinitions = null; // shouldn't be used again until the next top level
 
  	JCExpression pid = tree.pid;  //translate(tree.pid);
         JCCompilationUnit translated = make.at(tree.pos).TopLevel(List.<JCAnnotation>nil(), pid, translatedDefinitions.toList());
         translated.sourcefile = tree.sourcefile;
         translated.docComments = tree.docComments;
         translated.lineMap = tree.lineMap;
         translated.flags = tree.flags;
         result = translated;
    }
     
     @Override
     public void visitClassDeclaration(JFXClassDeclaration tree) {
         JFXClassDeclaration prevClass = currentClass;
         JFXClassDeclaration prevEnclClass = attrEnv.enclClass;
         Wrapped prevWrap = wrap;
         wrap = Wrapped.InNothing;
         currentClass = tree;
 
         try {
             DiagnosticPosition diagPos = tree.pos();
 
             attrEnv.enclClass = tree;
 
             ListBuffer<JCStatement> translatedInitBlocks = ListBuffer.lb();
             ListBuffer<JCStatement> translatedPostInitBlocks = ListBuffer.lb();
             ListBuffer<JCTree> translatedDefs = ListBuffer.lb();
             ListBuffer<TranslatedAttributeInfo> attrInfo = ListBuffer.lb();
             ListBuffer<TranslatedOverrideAttributeInfo> overrideInfo = ListBuffer.lb();
 
            // translate all the definitions that make up the class.
            // collect any prepended definitions, and prepend then to the tranlations
             ListBuffer<JCStatement> prevPrependToDefinitions = prependToDefinitions;
             ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
             prependToStatements = prependToDefinitions = ListBuffer.lb();
             {
                 for (JCTree def : tree.getMembers()) {
                     switch(def.getTag()) {
                         case JavafxTag.INIT_DEF: {
                             translatedInitBlocks.append(translate((JCBlock) ((JFXInitDefinition) def).getBody()));
                             break;
                         }
                         case JavafxTag.POSTINIT_DEF: {
                             translatedPostInitBlocks.append(translate((JCBlock) ((JFXPostInitDefinition) def).getBody()));
                             break;
                         }
                         case JavafxTag.VAR_DEF: {
                             JFXVar attrDef = (JFXVar) def;
                             boolean isStatic = (attrDef.getModifiers().flags & STATIC) != 0;
                             JCStatement init = attrDef.getInitializer()==doNotInitializeMarker? null :
                                 translateDefinitionalAssignmentToSet(attrDef.pos(), 
                                 attrDef.getInitializer(), attrDef.getBindStatus(), attrDef.sym,
                                 isStatic? null : make.Ident(defs.receiverName), FROM_DEFAULT_MILIEU);
                             attrInfo.append(new TranslatedAttributeInfo(
                                     attrDef,
                                     typeMorpher.varMorphInfo(attrDef.sym),
                                     init,
                                     translate(attrDef.getOnReplace())));
                             //translatedDefs.append(trans);
                             break;
                         }
                         case JavafxTag.OVERRIDE_ATTRIBUTE_DEF: {
                             JFXOverrideAttribute override = (JFXOverrideAttribute) def;
                             boolean isStatic = (override.sym.flags() & STATIC) != 0;
                             JCStatement init;
                             if (override.getInitializer() == null) {
                                 init = null;
                             } else {
                                 init = translateDefinitionalAssignmentToSet(override.pos(),
                                     override.getInitializer(), override.getBindStatus(), override.sym,
                                     isStatic? null : make.Ident(defs.receiverName), 
                                     FROM_DEFAULT_MILIEU);
                             }
                             overrideInfo.append(new TranslatedOverrideAttributeInfo(
                                     override,
                                     typeMorpher.varMorphInfo(override.sym),
                                     init,
                                     translate(override.getOnReplace())));
                             break;
                         }
                        case JavafxTag.FUNCTION_DEF : {
                            JFXFunctionDefinition funcDef = (JFXFunctionDefinition) def;
                            translatedDefs.append(translate(funcDef));
                            break;
                         }
                         case JCTree.METHODDEF : {
                             assert false : "translated method should never appear in an FX class";
                             break;
                         }
                         default: {
                             JCTree tdef =  translate(def);
                             if ( tdef != null ) {
                                 translatedDefs.append( tdef );
                             }
                             break;
                         }
                     }
                 }
             }
             // the translated defs have prepends in front
             for (JCTree prepend : prependToDefinitions) {
                 translatedDefs.prepend(prepend);
             }
             prependToDefinitions = prevPrependToDefinitions ;
             prependToStatements = prevPrependToStatements;
             // WARNING: translate can't be called directly or indirectly after this point in the method, or the prepends won't be included
 
             boolean classOnly = tree.generateClassOnly();
             JavafxClassModel model = initBuilder.createJFXClassModel(tree, attrInfo.toList(), overrideInfo.toList());
             additionalImports.appendList(model.additionalImports);
        
             boolean classIsFinal = (tree.getModifiers().flags & Flags.FINAL) != 0;
             
             // include the interface only once
             if (!tree.hasBeenTranslated) {
                 if (! classOnly) {
                     JCModifiers mods = make.Modifiers(Flags.PUBLIC | Flags.INTERFACE);
                     mods = addAccessAnnotationModifiers(diagPos, tree.mods.flags, mods);
 
                     JCClassDecl cInterface = make.ClassDef(mods,
                             model.interfaceName, List.<JCTypeParameter>nil(), null,
                             model.interfaces, model.iDefinitions);
 
                     prependToDefinitions.append(cInterface); // prepend to the enclosing class or top-level
                 }
                 tree.hasBeenTranslated = true;
             }
             
             translatedDefs.appendList(model.additionalClassMembers);
 
             {
                 // Add the userInit$ method
                 List<JCVariableDecl> receiverVarDeclList = List.of(makeReceiverParam(tree));
                 ListBuffer<JCStatement> initStats = ListBuffer.lb();
                 // call the superclasses userInit$
                 Set<String> dupSet = new HashSet<String>();
                 for (JCExpression parent : tree.getExtending()) {
                     if (! (parent instanceof JCIdent))
                         continue;
                     Symbol symbol = expressionSymbol(parent);
                     if (types.isJFXClass(symbol)) {
                         ClassSymbol cSym = (ClassSymbol) symbol;
                         String className = cSym.fullname.toString();
                         if (className.endsWith(interfaceSuffix)) {
                             className = className.substring(0, className.length() - interfaceSuffix.length());
                         }
 
                         if (!dupSet.contains(className)) {
                             dupSet.add(className);
                             List<JCExpression> args1 = List.nil();
                             args1 = args1.append(make.TypeCast(makeTypeTree( diagPos,cSym.type, true), make.Ident(defs.receiverName)));
                             initStats = initStats.append(callStatement(tree.pos(), makeIdentifier(diagPos, className), initBuilder.userInitName, args1));
                         }
                     }
                 }
                 initStats.appendList(translatedInitBlocks);
 
                 JCBlock userInitBlock = make.Block(0L, initStats.toList());
                 translatedDefs.append(make.MethodDef(
                         make.Modifiers(classIsFinal? Flags.PUBLIC  : Flags.PUBLIC | Flags.STATIC), 
                         initBuilder.userInitName, 
                         makeTypeTree( null,syms.voidType), 
                         List.<JCTypeParameter>nil(), 
                         receiverVarDeclList, 
                         List.<JCExpression>nil(), 
                         userInitBlock, 
                         null));
             }
             {
                 // Add the userPostInit$ method
                 List<JCVariableDecl> receiverVarDeclList = List.of(makeReceiverParam(tree));
                 ListBuffer<JCStatement> initStats = ListBuffer.lb();
                 // call the superclasses postInit$
                 Set<String> dupSet = new HashSet<String>();
                 for (JCExpression parent : tree.getExtending()) {
                     if (! (parent instanceof JCIdent))
                         continue;
                     Symbol symbol = expressionSymbol(parent);
                     if (types.isJFXClass(symbol)) {
                         ClassSymbol cSym = (ClassSymbol) symbol;
                         String className = cSym.fullname.toString();
                         if (className.endsWith(interfaceSuffix)) {
                             className = className.substring(0, className.length() - interfaceSuffix.length());
                         }
 
                         if (!dupSet.contains(className)) {
                             dupSet.add(className);
                             List<JCExpression> args1 = List.nil();
                             args1 = args1.append(make.TypeCast(makeTypeTree( diagPos,cSym.type, true), make.Ident(defs.receiverName)));
                             initStats = initStats.append(callStatement(tree.pos(), makeIdentifier(diagPos, className), initBuilder.postInitName, args1));
                         }
                     }
                 }
                 initStats.appendList(translatedPostInitBlocks);
 
                 JCBlock postInitBlock = make.Block(0L, initStats.toList());
                 translatedDefs.append(make.MethodDef(
                         make.Modifiers(classIsFinal? Flags.PUBLIC  : Flags.PUBLIC | Flags.STATIC),
                         initBuilder.postInitName,
                         makeTypeTree( null,syms.voidType),
                         List.<JCTypeParameter>nil(),
                         receiverVarDeclList,
                         List.<JCExpression>nil(),
                         postInitBlock,
                         null));
             }
 
             if (tree.isModuleClass) {
                 // Add main method...
                 translatedDefs.append(makeMainMethod(diagPos, tree.getName()));
             }
 
             // build the list of implemented interfaces
             List<JCExpression> implementing;
             if (classOnly) {
                 implementing = model.interfaces;
             }
             else {
                 implementing = List.of(make.Ident(model.interfaceName), makeIdentifier(diagPos, fxObjectString));
             }
             
             long flags = tree.mods.flags & (Flags.PUBLIC | Flags.PRIVATE | Flags.PROTECTED | Flags.FINAL | Flags.ABSTRACT);
             if (tree.sym.owner.kind == Kinds.TYP) {
                 flags |= Flags.STATIC;
             }
             
             // make the Java class corresponding to this FX class, and return it
             JCClassDecl res = make.at(diagPos).ClassDef(
                     make.at(diagPos).Modifiers(flags),
                     tree.getName(),
                     tree.getEmptyTypeParameters(), 
                     model.superType==null? null : makeTypeTree( null,model.superType, false),
                     implementing, 
                     translatedDefs.toList());
             res.sym = tree.sym;
             res.type = tree.type;
             result = res;
         }
         finally {
             attrEnv.enclClass = prevEnclClass;
             wrap = prevWrap;
 
             currentClass = prevClass;
         }
     }
     
     @Override
     public void visitInitDefinition(JFXInitDefinition tree) {
         assert false : "should be processed by class tree";
     }
 
     public void visitPostInitDefinition(JFXPostInitDefinition tree) {
         assert false : "should be processed by class tree";
     }
 
     abstract static class InstanciateTranslator extends Translator {
 
         protected final JFXInstanciate tree;
         protected ListBuffer<JCStatement> stats = ListBuffer.lb();
 
         InstanciateTranslator(final JFXInstanciate tree, JavafxToJava toJava) {
             super(tree.pos(), toJava);
             this.tree = tree;
         }
         
         abstract protected void processLocalVar(JFXVar var);
         
         protected List<JCExpression> translatedConstructorArgs() {
             if (tree.constructor != null && tree.constructor.type != null) {
                 List<Type> ptypes = tree.constructor.type.asMethodType().getParameterTypes();
                 return toJava.translate(tree.getArgs(), ptypes);
             } else {
                 return toJava.translate(tree.getArgs());
             }
         }
 
         abstract protected JCStatement translateAttributeSet(JCExpression init, JavafxBindStatus bindStatus, VarSymbol vsym,
             JCExpression instance);
         
         protected JCExpression doit() {
             Type type;
             
             for (JFXVar var : tree.getLocalvars()) {
                 // force var to be a Location (so class members can see it)
                 toJava.typeMorpher.varMorphInfo(var.sym).markBoundTo();
                 // add the variable before the class definition or object litersl assignment
                 processLocalVar(var);
             }
             if (tree.getClassBody() == null) {
                 type = tree.type;
             } else {
                 JFXClassDeclaration cdef = tree.getClassBody();
                 stats.append(toJava.translate(cdef));
                 type = cdef.type;
             }
             JCExpression classTypeExpr = toJava.makeTypeTree(tree, type, false);
             Symbol sym = TreeInfo.symbol(tree.getIdentifier());
 
             List<JCExpression> newClassArgs = translatedConstructorArgs();
             if (tree.getClassBody() != null || types.isJFXClass(sym)) {
                 assert newClassArgs.size() == 0 : "should not be args for JavaFX class constructors";
                 newClassArgs = newClassArgs.append(m().Literal(TypeTags.BOOLEAN, 1));
             }
             if (tree.getClassBody() != null &&
                     tree.getClassBody().sym != null && toJava.hasOuters.contains(tree.getClassBody().sym)) {
                 JCIdent thisIdent = m().Ident(defs.receiverName);
                 thisIdent.sym = tree.getClassBody().sym.owner; //TODO: these are assumed cruft - remove
                 thisIdent.type = tree.getClassBody().sym.owner.type;
 
                 newClassArgs = newClassArgs.prepend(thisIdent);
             }
 
             JCNewClass newClass =
                     m().NewClass(null, null, classTypeExpr,
                     newClassArgs,
                     null);
 
             JCExpression instExpression;
             {
                 if (sym != null &&
                         sym.kind == Kinds.TYP && (sym instanceof ClassSymbol) &&
                         (types.isJFXClass((ClassSymbol) sym) ||
                         tree.getClassBody() != null)) {
                     // it is a JavaFX class, initializa it properly
                     JCVariableDecl tmpVar = toJava.makeTmpVar(diagPos, "objlit", type, newClass);
                     stats.append(tmpVar);
                     for (JFXObjectLiteralPart olpart : tree.getParts()) {
                         diagPos = olpart.pos(); // overwrite diagPos (must restore)
                         JavafxBindStatus bindStatus = olpart.getBindStatus();
                         JCExpression init = olpart.getExpression();
                         VarSymbol vsym = (VarSymbol) olpart.sym;
                         VarMorphInfo vmi = toJava.typeMorpher.varMorphInfo(vsym);
                         assert toJava.shouldMorph(vmi);
 
                         // Lift JFXObjectLiteralPart if needed
                         if (types.isSequence(olpart.type)) {
                             JCExpression olexpr = olpart.getExpression();
                             if (!types.isSequence(olexpr.type)) {
                                 init = ((JavafxTreeMaker) m()).ExplicitSequence(List.<JCExpression>of(olexpr));
                                 WildcardType tpType = new WildcardType(olexpr.type, BoundKind.EXTENDS, olexpr.type.tsym);
                                 init.type = new ClassType(((JavafxSymtab) syms).javafx_SequenceType, List.<Type>of(tpType), ((JavafxSymtab) syms).javafx_SequenceType.tsym);
                             }
                         }
 
                         JCIdent ident1 = m().Ident(tmpVar.name);
                         stats.append( translateAttributeSet(init, bindStatus, vsym, ident1) );
                     }
                     diagPos = tree.pos();
                     JCIdent ident3 = m().Ident(tmpVar.name);
                     JCStatement applyExec = toJava.callStatement(diagPos, ident3, defs.initializeName);
                     stats.append(applyExec);
 
                     JCIdent ident2 = m().Ident(tmpVar.name);
                     instExpression = toJava.makeBlockExpression(diagPos, stats, ident2);
                 } else {
                     // this is a Java class, just instanciate it
                     instExpression = newClass;
                 }
             }
             if (toJava.wrap == Wrapped.InLocation) {
                  instExpression = toJava.makeConstantLocation(diagPos, tree.type, instExpression);               
             }
             return instExpression;
         }
     }
 
     @Override
     public void visitInstanciate(JFXInstanciate tree) {   
         
         ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
         prependToStatements = ListBuffer.lb();
         
         result = new InstanciateTranslator(tree, this) {
 
             protected void processLocalVar(JFXVar var) {
                 stats.append(translate(var));
             }
 
             protected JCStatement translateAttributeSet(JCExpression init, JavafxBindStatus bindStatus, VarSymbol vsym, JCExpression instance) {
                 return toJava.translateDefinitionalAssignmentToSet(diagPos, init, bindStatus,
                         vsym, instance, FROM_LITERAL_MILIEU);
             }
             }.doit();
                   
             if (result instanceof JFXBlockExpression) {
                 JFXBlockExpression blockExpr = (JFXBlockExpression)result;               
                 blockExpr.stats = blockExpr.getStatements().prependList(prependToStatements.toList());   
             }
             prependToStatements = prevPrependToStatements;
     }
 
     abstract static class StringExpressionTranslator extends Translator {
 
         private final JFXStringExpression tree;
         StringExpressionTranslator(JFXStringExpression tree, JavafxToJava toJava) {
             super(tree.pos(), toJava);
             this.tree = tree;
         }
 
         protected JCExpression doit() {
             StringBuffer sb = new StringBuffer();
             List<JCExpression> parts = tree.getParts();
             ListBuffer<JCExpression> values = new ListBuffer<JCExpression>();
 
             JCLiteral lit = (JCLiteral) (parts.head);            // "...{
             sb.append((String) lit.value);
             parts = parts.tail;
             boolean containsExtendedFormat = false;
 
             while (parts.nonEmpty()) {
                 lit = (JCLiteral) (parts.head);                  // optional format (or null)
                 String format = (String) lit.value;
                 if ((!containsExtendedFormat) && format.length() > 0
                     && EXTENDED_FORMAT_PATTERN.matcher(format).find()) {
                     containsExtendedFormat = true;
                 }
                 parts = parts.tail;
                 JCExpression exp = parts.head;
                 if (exp != null &&
                         types.isSameType(exp.type, syms.javafx_DurationType)) {
                     exp = m().Apply(null,
                             m().Select(translateArg(exp),
                             Name.fromString(toJava.names, "toDate")),
                             List.<JCExpression>nil());
                     sb.append(format.length() == 0 ? "%tQms" : format);
                 } else {
                     exp = translateArg(exp);
                     sb.append(format.length() == 0 ? "%s" : format);
                 }
                 values.append(exp);
                 parts = parts.tail;
 
                 lit = (JCLiteral) (parts.head);                  // }...{  or  }..."
                 String part = (String)lit.value;
                 sb.append(part.replace("%", "%%"));              // escape percent signs
                 parts = parts.tail;
             }
             JCLiteral formatLiteral = m().Literal(TypeTags.CLASS, sb.toString());
             values.prepend(formatLiteral);
             String formatMethod;
             if (tree.translationKey != null) {
                 formatMethod = "com.sun.javafx.runtime.util.StringLocalization.getLocalizedString";
                 if (tree.translationKey.length() == 0) {
                     values.prepend(m().Literal(TypeTags.BOT, null));
                 } else {
                     values.prepend(m().Literal(TypeTags.CLASS, tree.translationKey));
                 }
                 String resourceName =
                         toJava.attrEnv.enclClass.sym.flatname.toString().replace('.', '/').replaceAll("\\$.*", "");
                 values.prepend(m().Literal(TypeTags.CLASS, resourceName));
             } else if (containsExtendedFormat) {
                 formatMethod = "com.sun.javafx.runtime.util.FXFormatter.sprintf";
             } else {
                 formatMethod = "java.lang.String.format";
             }
             JCExpression formatter = toJava.makeQualifiedTree(diagPos, formatMethod);
             return m().Apply(null, formatter, values.toList());
         }
 
         abstract protected JCExpression translateArg(JCExpression arg);
         
     }
 
     @Override
     public void visitStringExpression(JFXStringExpression tree) {
         result = new StringExpressionTranslator(tree, this) {
             protected JCExpression translateArg(JCExpression arg) {
                 return translate(arg);
             }
         }.doit();
     }
 
     private List<JCExpression> translateDefinitionalAssignmentToArgs(DiagnosticPosition diagPos,
             JCExpression init, JavafxBindStatus bindStatus, VarMorphInfo vmi) {
         if (bindStatus.isUnidiBind()) {
             return List.of(toBound.translate(init, vmi.getRealType()));
         } else if (bindStatus.isBidiBind()) {
             assert (shouldMorph(vmi));
             // Bi-directional bind translate so it stays in a Location
             return List.<JCExpression>of( translate(init, Wrapped.InLocation) );
         } else {
             JCExpression initExpr;
             // normal init -- unbound -- but it is setting a Location
             if (init == null) {
                 // no initializing expression determine a default value from the type
                 initExpr = makeDefaultValue(diagPos, vmi);
             } else {
                 // do a vanilla translation of the expression
                 initExpr = translate(init, vmi.getSymbol().type);
             }
             return List.<JCExpression>of( initExpr );
         }
     }
 
     private JCExpression translateDefinitionalAssignmentToValue(DiagnosticPosition diagPos,
             JCExpression init, JavafxBindStatus bindStatus, VarSymbol vsym) {
         VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
         List<JCExpression> args = translateDefinitionalAssignmentToArgs(diagPos, init, bindStatus, vmi);
         if (bindStatus.isUnidiBind()) {
             return args.head;
         } else if (shouldMorph(vmi)) {
             Name makeName = defs.makeMethodName;
             if (bindStatus.isBidiBind()) {
                 makeName = defs.makeBijectiveMethodName;
             }
             return makeLocationVariable(vmi, diagPos, args, makeName);
         } else {
             return args.head;
         }
     }
 
     private JCStatement translateDefinitionalAssignmentToSet(DiagnosticPosition diagPos,
             JCExpression init, JavafxBindStatus bindStatus, VarSymbol vsym,
             JCExpression instance, int milieu) {
         return make.at(diagPos).Exec( translateDefinitionalAssignmentToSetExpression(diagPos,
             init, bindStatus, vsym,
              instance, milieu) );
     }
    
     private JCExpression translateDefinitionalAssignmentToSetExpression(DiagnosticPosition diagPos,
             JCExpression init, JavafxBindStatus bindStatus, VarSymbol vsym,
             JCExpression instance, int milieu) {
         VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
         List<JCExpression> args = translateDefinitionalAssignmentToArgs(diagPos, init, bindStatus, vmi);
         JCExpression localAttr;
         
         // if it is an attribute
         if (vsym.owner.kind == Kinds.TYP) {
             if ((vsym.flags() & STATIC) != 0) {
                 // statics are accessed directly
                 localAttr = make.Ident(vsym);
             } else {
                 String attrAccess = attributeGetMethodNamePrefix + vsym;
                 localAttr = callExpression(diagPos, instance, attrAccess);
             }
         } else {
             // if it is a local variable
             assert( (vsym.flags() & Flags.PARAMETER) == 0): "Parameters are not initialized";
             localAttr = make.at(diagPos).Ident(vsym);   
             
             if (!shouldMorph(vmi)) {
                 return make.at(diagPos).Assign(localAttr, args.head);
             }
         }
         
         Name methName;
         if (bindStatus.isUnidiBind()) {
             methName = defs.locationBindMilieuMethodName[milieu];
         } else if (bindStatus.isBidiBind()) {
             methName = defs.locationBijectiveBindMilieuMethodName[milieu];
         } else {
             methName = defs.locationSetMilieuMethodName[vmi.getTypeKind()][milieu];
         }   
         return callExpression(diagPos, localAttr, methName, args);
     }
 
     @Override
     public void visitVar(JFXVar tree) {
         DiagnosticPosition diagPos = tree.pos();
         Type type = tree.type;
         JCModifiers mods = tree.getModifiers();
         long modFlags = mods.flags & ~Flags.FINAL;
         VarSymbol vsym = tree.sym;
         VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
         assert vsym.owner.kind != Kinds.TYP : "attributes are processed in the class and should never come here";
         
         // Variables accessed from an inner class must be FINAL
         if ((vsym.flags_field & JavafxFlags.INNER_ACCESS) != 0) {   
             modFlags |= Flags.FINAL;
         }
         
         // force morphing on variables with triggers
         if (tree.getOnReplace() != null)
             vmi.markBoundTo();
         
         if (shouldMorph(vmi)) {
             // convert the type to the Location type
             if ((vsym.flags() & Flags.PARAMETER) != 0) {
                 type = vmi.getLocationType();
             } else {
                 type = vmi.getVariableType();
             }
 
             // Locations are never overwritten
             modFlags |= Flags.FINAL;
         } 
 
         mods = make.at(diagPos).Modifiers(modFlags);
         JCExpression typeExpression = makeTypeTree(diagPos, type, true);
 
         // for class vars, initialization happens during class init, so set to
         // default Location.  For local vars translate as definitional
         JCExpression init;
         if ((vsym.flags() & Flags.PARAMETER) != 0) {
             // parameters aren't initialized
             init = null; 
             result = make.at(diagPos).VarDef(mods, tree.name, typeExpression, init);           
         } else {
            // create a blank variable declaration and move the declaration to the beginning of the block
            if ( !shouldMorph(vmi)) {
                 if ( (modFlags & Flags.FINAL) != 0 ) {
                     init = translateDefinitionalAssignmentToValue(tree.pos(), tree.init,
                     tree.getBindStatus(), tree.sym);
                     result = make.at(diagPos).VarDef(mods, tree.name, typeExpression, init);
                     return;
                 }
                 // non location types:
                 init = makeDefaultValue(diagPos, vmi);
             } else { 
                 // location types: XXXVariable.make() 
                 init = makeLocationAttributeVariable(vmi, diagPos);
             }
             prependToStatements.append(make.at(diagPos).VarDef(mods, tree.name, typeExpression, init));
             
             // create change Listener and append it to the beginning of the block after the blank variable declaration
             JFXOnReplace onReplace = tree.getOnReplace();
             if ( onReplace != null ) {
                    
                 TranslatedAttributeInfo attrInfo = new TranslatedAttributeInfo(
                                                             tree,
                                                             typeMorpher.varMorphInfo(vsym),
                                                             null,
                                                             translate(tree.getOnReplace()));
                 JCStatement changeListener = initBuilder.makeChangeListenerCall(attrInfo); 
                 prependToStatements.append(changeListener);
             }
             
             result = translateDefinitionalAssignmentToSet(diagPos, tree.init, tree.getBindStatus(), tree.sym, null, 0);
         }   
     }
 
     @Override
     public void visitOverrideAttribute(JFXOverrideAttribute tree) {
         assert false : "should be processed by parent tree";
     }
 
     @Override
     public void visitOnReplace(JFXOnReplace tree) {
         result = ((JavafxTreeMaker) make).OnReplace(
                 tree.getOldValue(), tree.getFirstIndex(), tree.getLastIndex(),
                 tree.getEndKind(), tree.getNewElements(),
                 translate(tree.getBody()));
     }
     
     
     @Override
     public void visitFunctionValue(JFXFunctionValue tree) {
         JFXFunctionDefinition def = tree.definition;
         result = makeFunctionValue(make.Ident(defs.lambdaName), def, tree.pos(), (MethodType) def.type);
     }
     
    JCExpression makeFunctionValue (JCExpression meth, JFXFunctionDefinition def, DiagnosticPosition diagPos, MethodType mtype) {
         ListBuffer<JCTree> members = new ListBuffer<JCTree>();
         if (def != null)
             members.append(translate(def));
         JCExpression encl = null;
         List<JCExpression> args = List.nil();
         int nargs = mtype.argtypes.size();
         Type ftype = syms.javafx_FunctionTypes[nargs];
         JCExpression t = makeQualifiedTree(null, ftype.tsym.getQualifiedName().toString());
         ListBuffer<JCExpression> typeargs = new ListBuffer<JCExpression>();
         Type rtype = syms.boxIfNeeded(mtype.restype);
         typeargs.append(makeTypeTree(diagPos, rtype));
         ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
         ListBuffer<JCExpression> margs = new ListBuffer<JCExpression>();
         int i = 0;
         for (List<Type> l = mtype.argtypes;  l.nonEmpty();  l = l.tail) {
             Name pname = make.paramName(i++);
             Type ptype = syms.boxIfNeeded(l.head);
             JCVariableDecl param = make.VarDef(make.Modifiers(0), pname,
                     makeTypeTree(diagPos, ptype), null);
             params.append(param);
             JCExpression marg = make.Ident(pname);
             margs.append(marg);
             typeargs.append(makeTypeTree(diagPos, ptype));
         }
 
         // The backend's Attr skips SYNTHETIC methods when looking for a matching method.
         long flags = PUBLIC | BRIDGE; // | SYNTHETIC;
 
         JCExpression call = make.Apply(null, meth, margs.toList());
 
         List<JCStatement> stats;
         if (mtype.restype == syms.voidType)
             stats = List.of(make.Exec(call), make.Return(make.Literal(TypeTags.BOT, null)));
         else {
             if (mtype.restype.isPrimitive())
                 call = makeBox(diagPos, call, mtype.restype);
             stats = List.<JCStatement>of(make.Return(call));
         }
        JCMethodDecl bridgeDef = make.at(diagPos).MethodDef(
                 make.Modifiers(flags),
                 defs.invokeName, 
                 makeTypeTree(diagPos, rtype),
                 List.<JCTypeParameter>nil(),
                 params.toList(),
                 make.at(diagPos).Types(mtype.getThrownTypes()),
                 make.Block(0, stats), 
                 null);
 
         members.append(bridgeDef);
         JCClassDecl cl = make.AnonymousClassDef(make.Modifiers(0), members.toList());
         return make.NewClass(encl, args, make.TypeApply(t, typeargs.toList()), args, cl);
     }
     
    boolean isInnerFunction (MethodSymbol sym) {
        return sym.owner != null && sym.owner.kind != Kinds.TYP
                 && (sym.flags() & Flags.SYNTHETIC) == 0;
    }
            
    /**
     * Translate JavaFX a class definition into a Java static implementation method.
     */
    @Override
     public void visitFunctionDefinition(JFXFunctionDefinition tree) {
         if (isInnerFunction(tree.sym)) {
             // If tree's context is not a class, then translate:
             //   function foo(args) { body }
             // to: final var foo = function(args) { body };
             // A probably cleaner and possibly more efficient solution would
             // be to create an anonymous class containing the function as
             // a method; closed-over local variables become fields.
             // That would have the advantage that calling the function directly
             // translates into a direct method call, rather than going via
             // Function's invoke method, which implies extra casts, possibly
             // boxing, etc.  FIXME
             DiagnosticPosition diagPos = tree.pos();
             MethodType mtype = (MethodType) tree.type;
             JCExpression typeExpression = makeTypeTree( diagPos,syms.makeFunctionType(mtype), true);
             JFXFunctionDefinition def = new JFXFunctionDefinition(make.Modifiers(Flags.SYNTHETIC), tree.name, tree.operation);
             def.type = mtype;
             def.sym = new MethodSymbol(0, def.name, mtype, tree.sym.owner.owner);
             JCExpression init =
                makeFunctionValue(make.Ident(tree.name), def, tree.pos(), mtype);
             JCModifiers mods = make.at(diagPos).Modifiers(Flags.FINAL);
             result = make.at(diagPos).VarDef(mods, tree.name, typeExpression, init);
             return;
         }
 
         boolean isBound = (tree.sym.flags() & JavafxFlags.BOUND) != 0;
         Wrapped prevWrap = wrap;
         wrap = Wrapped.InNothing;
 
         try {
             boolean classOnly = currentClass.generateClassOnly();
             // Made all the operations public. Per Brian's spec.
             // If they are left package level it interfere with Multiple Inheritance
             // The interface methods cannot be package level and an error is reported.
             long flags = tree.mods.flags;
             long originalFlags = flags;
             flags &= ~(Flags.PROTECTED | Flags.PRIVATE);
             flags |=  Flags.PUBLIC;
             if (((flags & (Flags.ABSTRACT | Flags.SYNTHETIC)) == 0) && !classOnly) {
                 flags |= Flags.STATIC;
             }
             flags &= ~Flags.SYNTHETIC;
             JCModifiers mods = make.Modifiers(flags);     
             boolean isImplMethod = (originalFlags & (Flags.STATIC | Flags.ABSTRACT | Flags.SYNTHETIC)) == 0 && !classOnly;
 
             DiagnosticPosition diagPos = tree.pos();
             MethodType mtype = (MethodType)tree.type;
             
             if (isBound) {
                 locallyBound = new HashSet<VarSymbol>();
                 for (JFXVar fxVar : tree.getParameters()) {
                     setLocallyBound(fxVar.sym);
                 }
             }
 
             // construct the body of the translated function
             JFXBlockExpression bexpr = tree.getBodyExpression();
             JCBlock body;
             if (bexpr == null) {
                 body = null; // null if no block expression
             } else if (isBound) {
                 // Build the body of a bound function by treating it as a bound expression
                 // TODO: Remove entry in findbugs-exclude.xml if permeateBind is implemented -- it is, so it should be
                 JCExpression expr = toBound.translate(bexpr, typeMorpher.varMorphInfo(tree.sym));
                 body = asBlock(make.at(diagPos).Return(expr));
             } else if (syms.isRunMethod(tree.sym)) {
                 // it is a module level run method, do special translation
                 body = runMethodBody(bexpr);
                 isImplMethod = false;
             } else {
                 // the "normal" case
                 body = asBlock(translateExpressionToStatement(bexpr, mtype.getReturnType()));
             }
 
             ListBuffer<JCVariableDecl> params = ListBuffer.lb();
             if ((originalFlags & (Flags.STATIC | Flags.ABSTRACT | Flags.SYNTHETIC)) == 0) {
                 if (classOnly) {
                     // all implementation code is generated assuming a receiver parameter.
                     // in the final class case, there is no receiver param, so allow generated code
                     // to function by adding:   var receiver = this;
                     body.stats = body.stats.prepend(
                             make.at(diagPos).VarDef(
                                 make.at(diagPos).Modifiers(Flags.FINAL), 
                                 defs.receiverName, 
                                 make.Ident(initBuilder.interfaceName(currentClass)), 
                                 make.at(diagPos).Ident(names._this))
                             );
                 } else {
                     // if we are converting a standard instance function (to a static method), the first parameter becomes a reference to the receiver
                     params.prepend(makeReceiverParam(currentClass));
                 }
             }
             for (JFXVar fxVar : tree.getParameters()) {
                 JCVariableDecl var = (JCVariableDecl)translate(fxVar);
                 params.append(var);
             }
             
             mods = addAccessAnnotationModifiers(diagPos, tree.mods.flags, mods);
 
             result = make.at(diagPos).MethodDef(
                     mods,
                     functionName(tree.sym, isImplMethod,  isBound), 
                     makeReturnTypeTree(diagPos, tree.sym, isBound), 
                     make.at(diagPos).TypeParams(mtype.getTypeArguments()), 
                     params.toList(),
                     make.at(diagPos).Types(mtype.getThrownTypes()),  // makeThrows(diagPos), //
                     body, 
                     null);
             ((JCMethodDecl)result).sym = tree.sym;
             result.type = tree.type;
         }
         finally {
             wrap = prevWrap;
         }
     }
    
     public void visitBindExpression(JFXBindExpression tree) {
          throw new AssertionError(tree);
     }
 
     public void visitBlockExpression(JFXBlockExpression tree) {
         DiagnosticPosition diagPos = tree.pos();
         ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
         prependToStatements = ListBuffer.lb();
         
         JCExpression value = tree.value;
         boolean valueFromReturn = (value == null) && (yield == Yield.ToReturnStatement);
         ListBuffer<JCStatement> translated = ListBuffer.lb();
         for(JCStatement stmt : tree.getStatements()) {
             if (valueFromReturn && stmt.getTag() == JavafxTag.RETURN) {
                 value = ((JCReturn)stmt).getExpression();
             } else {
                 translated.append( translate(stmt) );
             }
         }       
         List<JCStatement> localDefs = translated.toList();
         
         if (yield == Yield.ToExpression) {
             assert (tree.type != syms.voidType) : "void block expressions should be handled below";
             JCExpression tvalue = translate(value); // must be before prepend
             localDefs = prependToStatements.appendList(localDefs).toList();
             result = ((JavafxTreeMaker) make).at(tree.pos).BlockExpression(tree.flags, localDefs, tvalue);
         } else {
             prependToStatements.appendList(localDefs);
             if (value != null) {
                 prependToStatements.append( translateExpressionToStatement(value) );
             }
             localDefs = prependToStatements.toList();
             result = localDefs.size() == 1? localDefs.head : make.at(diagPos).Block(0L, localDefs);
         }
         prependToStatements = prevPrependToStatements;
     }
  
     @Override
     public void visitBlock(JCBlock tree) {
         List<JCStatement> localDefs = translateStatements(tree.stats);
 	result = make.at(tree.pos).Block(tree.flags, localDefs);
     }
 
     @Override
     public void visitAssign(JCAssign tree) {
         DiagnosticPosition diagPos = tree.pos();
         
         Symbol sym = expressionSymbol(tree.lhs);
         VarSymbol vsym = (sym != null && (sym instanceof VarSymbol))? (VarSymbol)sym : null;
 
         if (tree.rhs instanceof JFXBindExpression) {
             JFXBindExpression bind = (JFXBindExpression) tree.rhs;
             JCExpression lhs = translate(tree.lhs, Wrapped.InLocation);
             result = translateDefinitionalAssignmentToSetExpression(bind.pos(), 
                                     bind.getExpression(), bind.getBindStatus(), vsym,
                                     null /*for now*/, FROM_DEFAULT_MILIEU);
             return;
         }
         
         if (tree.lhs.getTag() == JavafxTag.SEQUENCE_INDEXED) {
             // assignment of a sequence element --  s[i]=8, call the sequence set method
            JCExpression rhs = translate(tree.rhs, tree.type); 
             JFXSequenceIndexed si = (JFXSequenceIndexed)tree.lhs;
             JCExpression seq = translate(si.getSequence(), Wrapped.InLocation); 
             JCExpression index = translate(si.getIndex());
             JCFieldAccess select = make.Select(seq, defs.setMethodName);
             List<JCExpression> args = List.of(index, rhs);
             result = make.at(diagPos).Apply(null, select, args);
         } else if (tree.lhs.getTag() == JavafxTag.SEQUENCE_SLICE) {
             // assignment of a sequence slice --  s[i..j]=8, call the sequence set method
             JFXSequenceSlice si = (JFXSequenceSlice)tree.lhs;
             JCExpression rhs = translate(tree.rhs, si.getSequence().type);
             JCExpression seq = translate(si.getSequence(), Wrapped.InLocation); 
             JCExpression firstIndex = translate(si.getFirstIndex());
             JCExpression lastIndex = makeSliceLastIndex(si);
             JCFieldAccess select = make.Select(seq, defs.replaceSliceMethodName);
             List<JCExpression> args = List.of(firstIndex, lastIndex, rhs);
             result = make.at(diagPos).Apply(null, select, args);
         } else if (shouldMorph(vsym)) {
             // we are setting a var Location, call the set method
             JCExpression rhs = translate(tree.rhs);  //TODO: use  type converted translate?
             JCExpression lhs = translate(tree.lhs, Wrapped.InLocation);
             JCFieldAccess setSelect = make.Select(lhs, defs.locationSetMethodName[typeMorpher.typeMorphInfo(vsym.type).getTypeKind()]);
             List<JCExpression> setArgs = List.of(rhs);
             result = make.at(diagPos).Apply(null, setSelect, setArgs);
         } else {
             // We are setting a "normal" non-Location, use normal assign
             JCExpression rhs = translate(tree.rhs);  //TODO: use  type converted translate?
             JCExpression lhs = translate(tree.lhs);
             result = make.at(diagPos).Assign(lhs, rhs); // make a new one so we are non-destructive
         }
     }
 
     @Override
     public void visitAssignop(JCAssignOp tree) {
         DiagnosticPosition diagPos = tree.pos();
         
         Symbol sym = expressionSymbol(tree.lhs);
         VarSymbol vsym = (sym != null && (sym instanceof VarSymbol))? (VarSymbol)sym : null;
         
         JCExpression rhs = translate(tree.rhs);
         JCExpression lhs = translate(tree.lhs);
         int binaryOp;
         switch (tree.getTag()) {
             case JCTree.PLUS_ASG:
                 binaryOp = JCTree.PLUS;
                 break;
             case JCTree.MINUS_ASG:
                 binaryOp = JCTree.MINUS;
                 break;
             case JCTree.MUL_ASG:
                 binaryOp = JCTree.MUL;
                 break;
             case JCTree.DIV_ASG:
                 binaryOp = JCTree.DIV;
                 break;
             case JCTree.MOD_ASG:
                 binaryOp = JCTree.MOD;
                 break;
             default:
                 assert false : "unexpected assign op kind";
                 binaryOp = JCTree.PLUS;
                 break;
         }
         JCExpression combined = make.at(diagPos).Binary(binaryOp, lhs, rhs);
 
         if (tree.lhs.getTag() == JavafxTag.SEQUENCE_INDEXED) {
             // assignment of a sequence element --  s[i]+=8, call the sequence set method
             JFXSequenceIndexed si = (JFXSequenceIndexed)tree.lhs;
             JCExpression seq = translate(si.getSequence(), Wrapped.InLocation); 
             JCExpression index = translate(si.getIndex());
             JCFieldAccess select = make.Select(seq, defs.setMethodName);
             List<JCExpression> args = List.of(index, combined);
             result = make.at(diagPos).Apply(null, select, args);
         } else if (shouldMorph(vsym)) {
             // we are setting a var Location, call the set method
             JCExpression targetLHS = translate(tree.lhs, Wrapped.InLocation);
             JCFieldAccess setSelect = make.Select(targetLHS, defs.locationSetMethodName[typeMorpher.typeMorphInfo(vsym.type).getTypeKind()]);
             List<JCExpression> setArgs = List.of(combined);
             result = make.at(diagPos).Apply(null, setSelect, setArgs);
         } else {
             // We are setting a "normal" non-Location non-sequence-access, use normal assignop
             result = make.at(diagPos).Assignop(tree.getTag(), lhs, rhs);
         }
     }
 
     @Override
     public void visitSelect(JCFieldAccess tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression expr = tree.getExpression();
         Type exprType = expr.type;
 
         // this may or may not be in a LHS but in either
         // event the selector is a value expression
         JCExpression translatedSelected = translate(expr, Wrapped.InNothing);
         
         if (tree.type instanceof FunctionType && tree.sym.type instanceof MethodType) {
             //TODO: this branch is never actually taken (in tests suite)
             MethodType mtype = (MethodType) tree.sym.type;            
             JCVariableDecl selectedTmpDecl = makeTmpVar(diagPos, "tg", exprType, translatedSelected);
             JCExpression translated = make.at(diagPos).Select(make.Ident(selectedTmpDecl.name), tree.getIdentifier());
             translated = makeFunctionValue(translated, null, diagPos, mtype);
             //result = make.LetExpr(selectedTmp, translated);
             result = ((JavafxTreeMaker)make).BlockExpression(
                 0L,
                 List.<JCStatement>of(selectedTmpDecl), 
                 translated);
            return;
         }
 
         if (exprType != null && exprType.isPrimitive()) { // expr.type is null for package symbols.
             translatedSelected = makeBox(diagPos, translatedSelected, exprType);
         }
         // determine if this is a static reference, eg.   MyClass.myAttribute
         boolean staticReference = tree.sym.isStatic();
         if (staticReference) {
             translatedSelected = makeTypeTree( diagPos,types.erasure(tree.sym.owner.type), false);
         }
 
         boolean testForNull = generateNullChecks && !staticReference
                                            && (tree.sym instanceof VarSymbol) 
                                            && types.isJFXClass(exprType.tsym);
         boolean hasSideEffects = testForNull && hasSideEffects(expr);
         JCVariableDecl tmpVar = null;
         if (hasSideEffects) {
             tmpVar = makeTmpVar(diagPos, exprType, translatedSelected);
             translatedSelected = make.at(diagPos).Ident(tmpVar.name);
         }
         
         JCFieldAccess translated = make.at(diagPos).Select(translatedSelected, tree.getIdentifier());
 
         JCExpression ref = convertVariableReference(
                 diagPos,
                 translated,
                 tree.sym, 
                 (wrap == Wrapped.InLocation));
         if (testForNull) {
             // we have a testable guard for null, wrap the attribute access  in it, return default value if null
             TypeMorphInfo tmi = typeMorpher.typeMorphInfo(tree.type);
             JCExpression defaultExpr = makeDefaultValue(diagPos, tmi);
             if (wrap == Wrapped.InLocation) {
                 defaultExpr = makeUnboundLocation(diagPos, tmi, defaultExpr);
             }
 
             JCExpression checkedExpr;
             if (hasSideEffects) {
                 // we don't recreate (we've stuck it in a var)
                 checkedExpr = make.at(diagPos).Ident(tmpVar.name);
             } else {
                 // re-translate, we need two of them
                 checkedExpr = translate(expr, Wrapped.InNothing);
             }
             JCExpression cond = make.at(diagPos).Binary(
                     JCTree.EQ,
                     checkedExpr,
                     make.Literal(TypeTags.BOT, null));
             ref = make.at(diagPos).Conditional(cond, defaultExpr, ref);
             if (hasSideEffects) {
                 // put the tmp var and the conditional in a block expression
                 List<JCStatement> stmts = List.<JCStatement>of(tmpVar);
                 ref = fxmake.at(diagPos).BlockExpression(0L, stmts, ref);
             }
         }
         result = ref;
     }
     //where
     private boolean hasSideEffects(JCExpression expr) {
         final boolean[] hasSideEffectHolder = {false};
         new JavafxTreeScanner() {
 
             private void markSideEffects() {
                 hasSideEffectHolder[0] = true;
             }
 
             @Override
             public void visitBlockExpression(JFXBlockExpression tree) {
                 markSideEffects(); // maybe doesn't but covers all statements
             }
 
             @Override
             public void visitUnary(JCUnary tree) {
                 markSideEffects();
             }
 
             @Override
             public void visitAssignop(JCAssignOp tree) {
                 markSideEffects();
             }
 
             @Override
             public void visitInstanciate(JFXInstanciate tree) {
                 markSideEffects();
             }
 
             @Override
             public void visitAssign(JCAssign tree) {
                 markSideEffects();
             }
 
             @Override
             public void visitApply(JCMethodInvocation tree) {
                 markSideEffects();
             }
         }.scan(expr);
         return hasSideEffectHolder[0];
     }
     
     @Override
     public void visitIdent(JCIdent tree)   {
        DiagnosticPosition diagPos = tree.pos();
         if (tree.name == names._this) {
             // in the static implementation method, "this" becomes "receiver$"
             JCExpression rcvr = make.at(diagPos).Ident(defs.receiverName);
             if (wrap == Wrapped.InLocation) {
                 rcvr = makeConstantLocation(diagPos, tree.type, rcvr);
             }
             result = rcvr;
             return;
         } else if (tree.name == names._super) {
             if (types.isCompoundClass(tree.type.tsym)) {
                 // "super" become just the class where the static implementation method is defined
                 //  the rest of the implementation is in visitApply
                 result = make.at(diagPos).Ident(tree.type.tsym.name);
             }
             else {
                JCFieldAccess superSelect = make.at(diagPos).Select(make.at(diagPos).Ident(defs.receiverName), tree.name);
                 result = superSelect;
             }
             return;
         }
 
        // if this is an instance reference to an attribute or function, it needs to go the the "receiver$" arg,
        // and possible outer access methods
         JCExpression convert;
         boolean isStatic = tree.sym.isStatic();
         int kind = tree.sym.kind;
         if (isStatic) {
             // make class-based direct static reference:   Foo.x
             convert = make.at(diagPos).Select(makeTypeTree( diagPos,tree.sym.owner.type, false), tree.name);
         } else {
             if ((kind == Kinds.VAR || kind == Kinds.MTH) && tree.sym.owner.kind == Kinds.TYP) {
                 // it is a non-static attribute or function class member
                 // reference it through the receiver
                 JCExpression mRec = makeReceiver(diagPos, tree.sym, attrEnv.enclClass.sym);
                 convert = make.at(diagPos).Select(mRec, tree.name);
             } else {
                 convert = make.at(diagPos).Ident(tree.name);
             }
         }
         
         if (tree.type instanceof FunctionType && tree.sym.type instanceof MethodType) {
             MethodType mtype = (MethodType) tree.sym.type;
             JFXFunctionDefinition def = null; // FIXME
             result = makeFunctionValue(convert, def, tree.pos(), mtype);
             return;
         }
 
         result = convertVariableReference(diagPos, 
                 convert, 
                 tree.sym, 
                 (wrap == Wrapped.InLocation));
     }
     
     @Override
     public void visitSequenceExplicit(JFXSequenceExplicit tree) {
         /*** 
          * In cases where the components of an explicitly constructed 
          * sequence are all singletons, we can revert to this (more 
          * optimal) implementation.
  
         DiagnosticPosition diagPos = tree.pos();
         JCExpression meth = ((JavafxTreeMaker)make).at(diagPos).Identifier(sequencesMakeString);
         Type elemType = tree.type.getTypeArguments().get(0);
         ListBuffer<JCExpression> args = ListBuffer.<JCExpression>lb();
         List<JCExpression> typeArgs = List.<JCExpression>of(makeTypeTree(elemType, diagPos));
         // type name .class
         args.append(makeElementClassObject(diagPos, elemType));
         args.appendList( translate( tree.getItems() ) );
         result = make.at(diagPos).Apply(typeArgs, meth, args.toList());
         */
         ListBuffer<JCStatement> stmts = ListBuffer.lb();
         Type elemType = elementType(tree.type);
         UseSequenceBuilder builder = useSequenceBuilder(tree.pos(), elemType);
         stmts.append(builder.makeBuilderVar());
         for (JCExpression item : tree.getItems()) {
             stmts.append(builder.makeAdd( item ) );
         }
         result = makeBlockExpression(tree.pos(), stmts, builder.makeToSequence());
     }
     
     @Override
     public void visitSequenceRange(JFXSequenceRange tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression meth = makeQualifiedTree(
                 diagPos, tree.isExclusive()? 
                     sequencesRangeExclusiveString : 
                     sequencesRangeString);
         ListBuffer<JCExpression> args = ListBuffer.lb();
         List<JCExpression> typeArgs = List.nil();
         args.append( translate( tree.getLower() ));
         args.append( translate( tree.getUpper() ));
         if (tree.getStepOrNull() != null) {
             args.append( translate( tree.getStepOrNull() ));
         }
         result = make.at(diagPos).Apply(typeArgs, meth, args.toList());
     }
     
     @Override
     public void visitSequenceEmpty(JFXSequenceEmpty tree) {
         if (types.isSequence(tree.type)) {
             Type elemType = elementType(tree.type);
             result = makeEmptySequenceCreator(tree.pos(), elemType);
         }
         else
             result = make.at(tree.pos).Literal(TypeTags.BOT, null);
     }
         
     @Override
     public void visitSequenceIndexed(JFXSequenceIndexed tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression seq = translate(tree.getSequence(), Wrapped.InLocation);  
         JCExpression index = translate(tree.getIndex());
         JCFieldAccess select = make.at(diagPos).Select(seq, defs.getMethodName);
         List<JCExpression> args = List.of(index);
         result = make.at(diagPos).Apply(null, select, args);
     }
 
     @Override
     public void visitSequenceSlice(JFXSequenceSlice tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression seq = translate(tree.getSequence(), Wrapped.InLocation);  
         JCExpression firstIndex = translate(tree.getFirstIndex());
         JCExpression lastIndex = makeSliceLastIndex(tree);
         JCFieldAccess select = make.at(diagPos).Select(seq, defs.getSliceMethodName);
         List<JCExpression> args = List.of(firstIndex, lastIndex);
         result = make.at(diagPos).Apply(null, select, args);
     }
 
     @Override
     public void visitSequenceInsert(JFXSequenceInsert tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression seqLoc = translate(tree.getSequence(), Wrapped.InLocation);
         JCExpression elem = translate( tree.getElement() );
         if (tree.getPosition() == null) {
             result = callStatement(diagPos, seqLoc, "insert", elem);
         } else {
             String meth = tree.shouldInsertAfter()? "insertAfter" : "insertBefore";
             result = callStatement(diagPos, seqLoc, meth, 
                     List.of(elem, translate(tree.getPosition())));
         }
     }
     
     @Override
     public void visitSequenceDelete(JFXSequenceDelete tree) {
         JCExpression seq = tree.getSequence();
 
         if (tree.getElement() != null) {
             JCExpression seqLoc = translate(seq, Wrapped.InLocation);
             result = callStatement(tree.pos(), seqLoc, "deleteValue", translate(tree.getElement()));
         } else {
             if (seq.getTag() == JavafxTag.SEQUENCE_INDEXED) {
                 // deletion of a sequence element -- delete s[i]
                 JFXSequenceIndexed si = (JFXSequenceIndexed) seq;
                 JCExpression seqseq = si.getSequence();
                 JCExpression seqLoc = translate(seqseq, Wrapped.InLocation);
                 JCExpression index = translate(si.getIndex());
                 result = callStatement(tree.pos(), seqLoc, "delete", index);
             } else if (seq.getTag() == JavafxTag.SEQUENCE_SLICE) {
                 // deletion of a sequence slice --  delete s[i..j]=8
                 JFXSequenceSlice slice = (JFXSequenceSlice) seq;
                 JCExpression seqseq = slice.getSequence();
                 JCExpression seqLoc = translate(seqseq, Wrapped.InLocation);
                 JCExpression first = translate(slice.getFirstIndex());
                 JCExpression last = makeSliceLastIndex(slice);
                 result = callStatement(tree.pos(), seqLoc, "deleteSlice", List.of(first, last));
             } else if (types.isSequence(seq.type)) {
                 JCExpression seqLoc = translate(seq, Wrapped.InLocation);
                 result = callStatement(tree.pos(), seqLoc, "deleteAll");
             } else {
                 result = make.at(tree.pos()).Exec(
                             make.Assign(
                                 translate(seq), 
                                 make.Literal(TypeTags.BOT, null)));
             }
         }
     }
 
     /**** utility methods ******/
     
     JCExpression makeSliceLastIndex(JFXSequenceSlice tree) {
         JCExpression lastIndex = tree.getLastIndex() == null ? 
                 callExpression(tree, 
                     translate(tree.getSequence()),
                     defs.sizeMethodName) : 
                 translate(tree.getLastIndex());
         int decr = 
                 (tree.getEndKind() == SequenceSliceTree.END_EXCLUSIVE ? 1 : 0) +
                 (tree.getLastIndex() == null ? 1 : 0);
         if (decr > 0) {
             lastIndex = make.at(tree).Binary(JavafxTag.MINUS,
                     lastIndex, make.Literal(TypeTags.INT, decr));
         }
         return lastIndex;
     }
    
     JCMethodDecl makeMainMethod(DiagnosticPosition diagPos, Name className) {
             List<JCExpression> emptyExpressionList = List.nil();
             JCExpression classIdent = make.at(diagPos).Ident(className);
             JCExpression classConstant = make.at(diagPos).Select(classIdent, names._class);
             JCExpression commandLineArgs = makeIdentifier(diagPos, "args");
             JCExpression startIdent = makeQualifiedTree(diagPos, JavafxDefs.startMethodString);
             ListBuffer<JCExpression>args = new ListBuffer<JCExpression>();
             args.append(classConstant);
             args.append(commandLineArgs);
             JCMethodInvocation runCall = make.at(diagPos).Apply(emptyExpressionList, startIdent, args.toList());
             List<JCStatement> mainStats = List.<JCStatement>of(make.at(diagPos).Exec(runCall));
             List<JCVariableDecl> paramList = List.nil();
             paramList = paramList.append(make.at(diagPos).VarDef(make.Modifiers(0), 
                     Name.fromString(names, "args"), 
                     make.at(diagPos).TypeArray(make.Ident(Name.fromString(names, "String"))), 
                     null));
             JCBlock body = make.Block(0, mainStats);
             return make.at(diagPos).MethodDef(make.Modifiers(Flags.PUBLIC | Flags.STATIC),
                     Name.fromString(names, "main"), 
                     make.at(diagPos).TypeIdent(TypeTags.VOID), 
                     List.<JCTypeParameter>nil(), 
                     paramList, 
                     makeThrows(diagPos), 
                     body, 
                     null);
     }
 
     /** Equivalent to make.at(pos.getStartPosition()) with side effect of caching
      *  pos as make_pos, for use in diagnostics.
      **/
     TreeMaker make_at(DiagnosticPosition pos) {
         return make.at(pos);
     }
 
     /** Look up a method in a given scope.
      */
     private MethodSymbol lookupMethod(DiagnosticPosition pos, Name name, Type qual, List<Type> args) {
 	return rs.resolveInternalMethod(pos, attrEnv, qual, name, args, null);
     }
 
     /** Look up a constructor.
      */
     private MethodSymbol lookupConstructor(DiagnosticPosition pos, Type qual, List<Type> args) {
 	return rs.resolveInternalConstructor(pos, attrEnv, qual, args, null);
     }
 
     /** Box up a single primitive expression. */
     JCExpression makeBox(DiagnosticPosition diagPos, JCExpression translatedExpr, Type primitiveType) {
 	make_at(translatedExpr.pos());
         if (target.boxWithConstructors()) {
             Symbol ctor = lookupConstructor(translatedExpr.pos(),
                                             types.boxedClass(primitiveType).type,
                                             List.<Type>nil()
                                             .prepend(primitiveType));
             return make.Create(ctor, List.of(translatedExpr));
         } else {
             Symbol valueOfSym = lookupMethod(translatedExpr.pos(),
                                              names.valueOf,
                                              types.boxedClass(primitiveType).type,
                                              List.<Type>nil()
                                              .prepend(primitiveType));
 //            JCExpression meth =makeIdentifier(valueOfSym.owner.type.toString() + "." + valueOfSym.name.toString());
             JCExpression meth = make.Select(makeTypeTree( diagPos,valueOfSym.owner.type), valueOfSym.name);
             TreeInfo.setSymbol(meth, valueOfSym);
             meth.type = valueOfSym.type;
             return make.App(meth, List.of(translatedExpr));
         }
     }
     
     public List<JCExpression> makeThrows(DiagnosticPosition diagPos) {
         return List.of(makeQualifiedTree(diagPos, methodThrowsString));
     }
     
     UseSequenceBuilder useSequenceBuilder(DiagnosticPosition diagPos, Type elemType) {
 
         return new UseSequenceBuilder(diagPos, elemType, sequenceBuilderString) {
 
             JCStatement makeAdd(JCExpression exprToAdd) {
                 JCExpression expr = translate(exprToAdd);
                 Type exprType = exprToAdd.type;
                 return makeAdd(expr, exprType);
             }
         };
     }
     
     UseSequenceBuilder useBoundSequenceBuilder(DiagnosticPosition diagPos, Type elemType) {
 
         return new UseSequenceBuilder(diagPos, elemType, boundSequenceBuilderString) {
 
             JCStatement makeAdd(JCExpression exprToAdd) {
                 JCExpression expr = toBound.translate(exprToAdd);
                 Type exprType = exprToAdd.type;
                 return makeAdd(expr, exprType);
             }
         };
     }
     
     abstract class UseSequenceBuilder {
         final DiagnosticPosition diagPos;
         final Type elemType;
         final String seqBuilder;
         
         Name sbName;
         
         UseSequenceBuilder(DiagnosticPosition diagPos, Type elemType, String seqBuilder) {
             this.diagPos = diagPos;
             this.elemType = elemType;
             this.seqBuilder = seqBuilder;
         }
         
         JCStatement makeBuilderVar() {
             JCExpression builderTypeExpr = makeQualifiedTree(diagPos, seqBuilder);
             List<JCExpression> btargs = List.of(makeTypeTree(diagPos, elemType));
             builderTypeExpr = make.at(diagPos).TypeApply(builderTypeExpr, btargs);
 
             // Sequence builder temp var name "sb"
             sbName = getSyntheticName("sb");
 
             // Build "sb" initializing expression -- new SequenceBuilder<T>(clazz)
             List<JCExpression> args = List.<JCExpression>of(
                     makeElementClassObject(diagPos, elemType));               
 
             JCExpression newExpr = make.at(diagPos).NewClass(
                 null,                               // enclosing
                 List.<JCExpression>nil(),           // type args
                 make.at(diagPos).TypeApply(         // class name -- SequenceBuilder<elemType>
                      makeQualifiedTree(diagPos, seqBuilder), 
                      List.<JCExpression>of(makeTypeTree(diagPos, elemType))),
                 args,                               // args
                 null                                // empty body
                 );
         
             // Build the sequence builder variable
             return make.at(diagPos).VarDef( 
                 make.at(diagPos).Modifiers(0L), 
                 sbName, builderTypeExpr, newExpr);
         }
         
         JCIdent makeBuilderVarAccess() {
             return make.Ident(sbName);  
         }
          
         abstract JCStatement makeAdd(JCExpression expr);
          
         JCStatement makeAdd(JCExpression expr, Type exprType) {
             if (exprType != elemType) {
                 Type unboxedElemType = types.unboxedType(elemType);
                 if (unboxedElemType != Type.noType) {
                     Type unboxedExprType = types.unboxedType(exprType);
                     if (unboxedExprType != Type.noType) {
                             expr = make.at(diagPos).TypeCast(unboxedExprType, expr);
                             exprType = unboxedExprType;
                     }
                     if (exprType.tag == TypeTags.INT && unboxedElemType.tag == TypeTags.DOUBLE) {
                         expr = make.at(diagPos).TypeCast(unboxedElemType, expr);
                     }
                 } 
              }
             JCMethodInvocation addCall = make.Apply(
                     List.<JCExpression>nil(), 
                     make.at(diagPos).Select(
                         makeBuilderVarAccess(), 
                         Name.fromString(names, "add")), 
                     List.<JCExpression>of(expr));
             return make.at(diagPos).Exec(addCall);
         }
 
         JCExpression makeToSequence() {
             return make.Apply(
                 List.<JCExpression>nil(), // type arguments
                 make.at(diagPos).Select(
                     makeBuilderVarAccess(), 
                     Name.fromString(names, toSequenceString)),
                 List.<JCExpression>nil() // arguments
                 );
         }
     }
   
    JCExpression castFromObject (JCExpression arg, Type castType) {
         if (castType.isPrimitive())
             castType = types.boxedClass(castType).type;
          return make.TypeCast(makeTypeTree( arg.pos(),castType), arg);
     }
 
     /**
      * JCTrees which can just be copied and trees which sjould not occur 
      * */
     
     @Override
     public void visitAnnotation(JCAnnotation tree) {
         JCTree annotationType = translate(tree.annotationType);
         List<JCExpression> args = translate(tree.args);
         result = make.at(tree.pos).Annotation(annotationType, args);
     }
 
     @Override
     public void visitAssert(JCAssert tree) {
         JCExpression cond = translate(tree.cond);
         JCExpression detail = translate(tree.detail);
         result = make.at(tree.pos).Assert(cond, detail);
     }
 
     @Override
     public void visitBinary(final JCBinary tree) {
         result = (new Translator( tree.pos(), this ) {
 
             /**
              * Compare against null
              */
             private JCExpression makeNullCheck(JCExpression targ) {
                 return makeEqEq(targ, makeNull());
             }
             
             //TODO: after type system is figured out, this needs to be revisited
             /**
              * Check if a primitive has the default value for its type.
              */
             private JCExpression makePrimitiveNullCheck(Type argType, JCExpression arg) {
                 TypeMorphInfo tmi = typeMorpher.typeMorphInfo(argType);
                 JCExpression defaultValue = makeLit(diagPos, tmi.getRealType(), tmi.getDefaultValue());
                 return makeEqEq( arg, defaultValue);
             }
             
             /**
              * Check if a non-primitive has the default value for its type.
              */
             private JCExpression makeObjectNullCheck(Type argType, JCExpression arg) {
                 TypeMorphInfo tmi = typeMorpher.typeMorphInfo(argType);
                 if (tmi.getTypeKind() == TYPE_KIND_SEQUENCE || tmi.getRealType() == syms.javafx_StringType) {
                     return callRuntime(JavafxDefs.isNullMethodString, List.of(arg));
                 } else {
                     return makeNullCheck(arg);
                 }
             }
            
             /*
              * Do a == compare
              */
             private JCExpression makeEqEq(JCExpression arg1, JCExpression arg2) {
                 return makeBinary(JCTree.EQ, arg1, arg2);
             }
             
             private JCExpression makeBinary(int tag, JCExpression arg1, JCExpression arg2) {
                 return make.at(diagPos).Binary(tag, arg1, arg2);
             }
             
             private JCExpression makeNull() {
                 return make.at(diagPos).Literal(TypeTags.BOT, null);
             }
             
             private JCExpression callRuntime(String methNameString, List<JCExpression> args) {
                 JCExpression meth = makeQualifiedTree(diagPos, methNameString);
                 List<JCExpression> typeArgs = List.nil();
                 return m().Apply(typeArgs, meth, args);
             }
             
             /**
              * Make a .equals() comparison with a null check on the receiver
              */
             private JCExpression makeFullCheck(JCExpression lhs, JCExpression rhs) {
                 return callRuntime(JavafxDefs.equalsMethodString, List.of(lhs, rhs));
             }
             
             /**
              * Return the translation for a == comparision
              */
             private JCExpression translateEqualsEquals() {
                 final JCExpression lhs = translate( tree.lhs );
                 final JCExpression rhs = translate( tree.rhs );
                 final Type lhsType = tree.lhs.type;
                 final Type rhsType = tree.rhs.type;
                 
                     // this is an x == y
                     if (lhsType.getKind() == TypeKind.NULL) {
                         if (rhsType.getKind() == TypeKind.NULL) {
                             // both are known to be null
                             return make.at(diagPos).Literal(TypeTags.BOOLEAN, 1);
                         } else if (rhsType.isPrimitive()) {
                             // lhs is null, rhs is primitive, do default check
                             return makePrimitiveNullCheck(rhsType, rhs);
                         } else {
                             // lhs is null, rhs is non-primitive, figure out what check to do
                             return makeObjectNullCheck(rhsType, rhs);
                         }
                     } else if (lhsType.isPrimitive()) {
                         if (rhsType.getKind() == TypeKind.NULL) {
                             // lhs is primitive, rhs is null, do default check on lhs
                             return makePrimitiveNullCheck(lhsType, lhs);
                         } else if (rhsType.isPrimitive()) {
                             // both are primitive, use ==
                             return makeEqEq(lhs, rhs);
                         } else {
                             // lhs is primitive, rhs is non-primitive, use equals(), but switch them
                             return makeFullCheck(rhs, lhs);
                         }
                     } else {
                         if (rhsType.getKind() == TypeKind.NULL) {
                             // lhs is non-primitive, rhs is null, figure out what check to do
                             return makeObjectNullCheck(lhsType, lhs);
                         } else {
                             //  lhs is non-primitive, use equals()
                             return makeFullCheck(lhs, rhs);
                         }
                     }
             }
            
             /**
              * Translate a binary expressions
              */
             public JCTree doit() {
                 //TODO: handle <>
                 if (tree.getTag() == JavafxTag.EQ) {
                     return translateEqualsEquals();
                 } else if (tree.getTag() == JavafxTag.NE) {
                     return make.at(diagPos).Unary(JCTree.NOT, translateEqualsEquals());
                 }  else {
                     // anything other than == or <>
 
                     // Duration type operator overloading
                     if ((types.isSameType(tree.lhs.type, syms.javafx_DurationType) ||
                          types.isSameType(tree.rhs.type, syms.javafx_DurationType)) &&
                         tree.operator == null) { // operator check is to try to get a decent error message by falling through if the Duration method isn't matched
                         JCExpression l = tree.lhs;
                         JCExpression r = tree.rhs;
                         switch (tree.getTag()) {
                         case JavafxTag.PLUS:
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "add")), List.<JCExpression>of(translate(r)));
                             // lhs.add(rhs);
                         case JavafxTag.MINUS:
                             // lhs.sub(rhs);
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "sub")), List.<JCExpression>of(translate(r)));
                         case JavafxTag.DIV:
                             // lhs.div(rhs);
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "div")), List.<JCExpression>of(translate(r)));
                         case JavafxTag.MUL:
                             // lhs.mul(rhs);
                             if (!types.isSameType(l.type, syms.javafx_DurationType)) {
                                 // FIXME This may get side-effects out-of-order.
                                 // A simple fix is to use a static Duration.mul(double,Duration).
                                 // Another is to use a BlockExpression and a temporary.
                                 r = l;
                                 l = tree.rhs;
                             }
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "mul")), List.<JCExpression>of(translate(r)));
                         case JavafxTag.LT:
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "lt")), List.<JCExpression>of(translate(r)));
                         case JavafxTag.LE:
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "le")), List.<JCExpression>of(translate(r)));
                         case JavafxTag.GT:
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "gt")), List.<JCExpression>of(translate(r)));
                         case JavafxTag.GE:
                             return make.at(diagPos).Apply(null,
                                                           make.at(diagPos).Select(translate(l), Name.fromString(names, "ge")), List.<JCExpression>of(translate(r)));
                         }
                     }
                     final JCExpression lhs = translate(tree.lhs);
                     final JCExpression rhs = translate(tree.rhs);
                     return makeBinary(tree.getTag(), lhs, rhs);
                 }
             }
 
         }).doit();
     }
 
     @Override
     public void visitBreak(JCBreak tree) {
         result = make.at(tree.pos).Break(tree.label);
     }
 
     @Override
    public void visitCase(JCCase tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitCatch(JCCatch tree) {
         JCVariableDecl param = translate(tree.param);
         JCBlock body = translate(tree.body);
         result = make.at(tree.pos).Catch(param, body);
     }
 
     @Override
     public void visitClassDef(JCClassDecl tree) {
         assert false : "should not be in JavaFX AST";
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
         if (yield == Yield.ToExecStatement) {
              result = wrapWithInClause(tree, translateExpressionToStatement(tree.getBodyExpression()));
         } else {
             // body has value (non-void)
             assert tree.type != syms.voidType : "should be handled above";
             DiagnosticPosition diagPos = tree.pos();
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             JCStatement stmt;
             JCExpression value;
 
             // Compute the element type from the sequence type
             assert tree.type.getTypeArguments().size() == 1;
             Type elemType = elementType(tree.type);
 
             UseSequenceBuilder builder = useSequenceBuilder(diagPos, elemType);
             stmts.append(builder.makeBuilderVar());
 
             // Build innermost loop body
             stmt = builder.makeAdd( tree.getBodyExpression() );
 
             // Build the result value
             value = builder.makeToSequence();
             stmt = wrapWithInClause(tree, stmt);
             stmts.append(stmt);
 
             if (yield == Yield.ToExpression) {
                     // Build the block expression -- which is what we translate to
                     result = makeBlockExpression(diagPos, stmts, value);
             } else {
                     stmts.append( make.at(tree).Return( value ) );
                     result = make.at(diagPos).Block(0L, stmts.toList());
             }
         }
     }
 
     //where
     private JCStatement wrapWithInClause(JFXForExpression tree, JCStatement coreStmt) {
         JCStatement stmt = coreStmt;
         for (int inx = tree.getInClauses().size() - 1; inx >= 0; --inx) {
             JFXForExpressionInClause clause = (JFXForExpressionInClause)tree.getInClauses().get(inx);
             if (clause.getWhereExpression() != null) {
                 stmt = make.at(clause).If( translate( clause.getWhereExpression() ), stmt, null);
             }
 
             // Build the loop
             //TODO:  below is the simpler version of the loop. Ideally, this should be used in
             // cases where the loop variable does not need to be final.
             /*
             stmt = make.at(clause).ForeachLoop(
                     // loop variable is synthetic should not be bound
                     // even if we are in a bind context
                     boundTranslate(clause.getVar(), JavafxBindStatus.UNBOUND), 
                     translate(clause.getSequenceExpression()),
                     stmt);
              */
             JFXVar var = clause.getVar();
             Name tmpVarName = getSyntheticName(var.getName().toString());
             JCVariableDecl finalVar = make.VarDef(
                     make.Modifiers(Flags.FINAL), 
                     var.getName(), 
                     makeTypeTree( var,var.type), 
                     make.Ident(tmpVarName));
             Name tmpIndexVarName;
             if (clause.getIndexUsed()) {
                 Name indexVarName = indexVarName(clause);
                 tmpIndexVarName = getSyntheticName(indexVarName.toString());
                 JCVariableDecl finalIndexVar = make.VarDef(
                     make.Modifiers(Flags.FINAL),
                     indexVarName, 
                     makeTypeTree( var,syms.javafx_IntegerType),
                     make.Unary(JCTree.POSTINC, make.Ident(tmpIndexVarName)));
                 stmt = make.Block(0L, List.of(finalIndexVar, finalVar, stmt));
             }                
             else {
                 tmpIndexVarName = null;
                 stmt = make.Block(0L, List.of(finalVar, stmt));
             }
     
             DiagnosticPosition diagPos = clause.seqExpr;
             if (types.isSequence(clause.seqExpr.type)) {
                 // It would be more efficient to move the Iterable.iterator call
                 // to a static method, which can also check for null.
                 // But that requires expanding the ForeachLoop by hand.  Later.
                 JCExpression seq = callExpression(diagPos,
                     makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"),
                     "forceNonNull",
                     List.of(makeElementClassObject(diagPos, var.type),
                         translate(clause.seqExpr)));
                 stmt = make.at(clause).ForeachLoop(
                     // loop variable is synthetic should not be bound
                     // even if we are in a bind context
                     make.VarDef(
                         make.Modifiers(0L), 
                         tmpVarName, 
                         makeTypeTree( var,var.type, true), 
                         null),
                     seq,
                     stmt);
             } else if (types.asSuper(clause.seqExpr.type, syms.iterableType.tsym) != null) {
                 stmt = make.at(clause).ForeachLoop(
                     // loop variable is synthetic should not be bound
                     // even if we are in a bind context
                     make.VarDef(
                         make.Modifiers(0L), 
                         tmpVarName, 
                         makeTypeTree(var,var.type, true), 
                         null),
                     translate(clause.seqExpr),
                     stmt);
             } else {
                 // The "sequence" isn't a Sequence.
                 // Compile: { var tmp = seq; if (tmp!=null) stmt; }
                 if (! var.type.isPrimitive())
                     stmt = make.If(make.Binary(JCTree.NE, make.Ident(tmpVarName),
                                 make.Literal(TypeTags.BOT, null)),
                             stmt, null);
                 stmt = make.at(diagPos).Block(0,
                     List.of(make.at(diagPos).VarDef(
                         make.Modifiers(0L), 
                         tmpVarName, 
                         makeTypeTree( var,var.type, true), 
                         translate(clause.seqExpr)),
                         stmt));
             }
             if (clause.getIndexUsed()) {
                 JCVariableDecl tmpIndexVar =
                         make.VarDef(
                         make.Modifiers(0L), 
                         tmpIndexVarName, 
                         makeTypeTree( var,syms.javafx_IntegerType), 
                         make.Literal(Integer.valueOf(0)));
                 stmt = make.Block(0L, List.of(tmpIndexVar, stmt));
             }
         }
         return stmt;
     }
     
     public void visitIndexof(JFXIndexof that) {
         result = make.at(that.pos()).Ident(indexVarName(that.clause));
     }
 
     @Override
     public void visitConditional(JCConditional tree) {
         final DiagnosticPosition diagPos = tree.pos();
         JCExpression cond = translate(tree.getCondition());
         JCExpression trueSide = tree.getTrueExpression();
         JCExpression falseSide = tree.getFalseExpression();
         if (yield == Yield.ToExpression) {
             JCExpression translatedFalseSide;
             if (falseSide == null) {
                 Type trueSideType = tree.getTrueExpression().type;
                 switch (trueSideType.tag) {
                     case TypeTags.BOOLEAN:
                         translatedFalseSide = make.at(diagPos).Literal(TypeTags.BOOLEAN, 0);
                         break;
                     case TypeTags.INT:
                         translatedFalseSide = make.at(diagPos).Literal(TypeTags.INT, 0);
                         break;
                     case TypeTags.DOUBLE:
                         translatedFalseSide = make.at(diagPos).Literal(TypeTags.DOUBLE, 0.0);
                         break;
                     case TypeTags.BOT:
                         translatedFalseSide = make.at(diagPos).Literal(TypeTags.BOT, null);
                         break;
                     case TypeTags.VOID:
                         assert false : "should have been translated";
                         translatedFalseSide = make.at(diagPos).Literal(TypeTags.BOT, null);
                         break;
                     case TypeTags.CLASS:
                         if (trueSideType == syms.stringType) {
                             translatedFalseSide = make.at(diagPos).Literal(TypeTags.CLASS, "");
                         } else {
                             translatedFalseSide = make.at(diagPos).Literal(TypeTags.BOT, null);
                         }
                         break;
                     default:
                         assert false : "what is this type doing here? " + trueSideType;
                         translatedFalseSide = make.at(diagPos).Literal(TypeTags.BOT, null);
                         break;
                 }
             } else {
                 translatedFalseSide = translate(falseSide);
             }
             JCExpression translatedTrueSide = convertTranslated(translate(trueSide), trueSide, trueSide.type, tree.type);
             if (falseSide != null) {
                 translatedFalseSide = convertTranslated(translatedFalseSide, falseSide, falseSide.type, tree.type);
             } else {
                 translatedFalseSide = convertTranslated(translatedFalseSide, null, trueSide.type, tree.type);
             }
             result = make.at(diagPos).Conditional(cond, translatedTrueSide, translatedFalseSide);
         } else {
             result = make.at(diagPos).If(cond, 
                     translateExpressionToStatement(trueSide), 
                     falseSide == null ? null : translateExpressionToStatement(falseSide));
         }
     }
 
     @Override
     public void visitContinue(JCContinue tree) {
         result = make.at(tree.pos).Continue(tree.label);
     }
 
     @Override
     public void visitDoLoop(JCDoWhileLoop tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitErroneous(JCErroneous tree) {
         List<? extends JCTree> errs = translate(tree.errs);
         result = make.at(tree.pos).Erroneous(errs);
     }
    
     @Override
     public void visitReturn(JCReturn tree) {
         JCExpression exp = tree.getExpression();
         if (exp == null) {
             result = make.at(tree).Return(null);
         } else {
             result = translateExpressionToStatement(exp, tree.type);
         }
     }
 
     @Override
     public void visitExec(JCExpressionStatement tree) {
         result = translateExpressionToStatement(tree.getExpression(), syms.voidType);
     }
 
     @Override
     public void visitParens(JCParens tree) {
         if (yield == Yield.ToExpression) {
             JCExpression expr = translate(tree.expr);
             result = make.at(tree.pos).Parens(expr);
         } else {
             result = translateExpressionToStatement(tree.expr);
         }
     }
 
     @Override
     public void visitForeachLoop(JCEnhancedForLoop tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitForLoop(JCForLoop tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitIf(JCIf tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitImport(JCImport tree) {
         JCTree qualid = translate(tree.qualid);
         result = make.at(tree.pos).Import(qualid, tree.staticImport);
     }
 
     @Override
     public void visitIndexed(JCArrayAccess tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitTypeTest(JCInstanceOf tree) {
         JCTree clazz = this.makeTypeTree( tree,tree.clazz.type);
         JCExpression expr = translate(tree.expr);
         if (types.isSequence(tree.expr.type) && ! types.isSequence(tree.clazz.type))
             expr = callExpression(tree.expr,
                     makeQualifiedTree(tree.expr, "com.sun.javafx.runtime.sequence.Sequences"),
                     "getSingleValue", expr);
         result = make.at(tree.pos).TypeTest(expr, clazz);
     }
 
     abstract static class TypeCastTranslator extends Translator {
 
         protected final JCTypeCast tree;
 
         TypeCastTranslator(final JCTypeCast tree, JavafxToJava toJava) {
             super(tree.pos(), toJava);
             this.tree = tree;
         }
         
         abstract protected JCExpression translatedExpr();
 
         protected JCExpression doit() {
             Type clazztype = tree.clazz.type;
             if (clazztype.isPrimitive() && !tree.expr.type.isPrimitive()) {
                 clazztype = types.boxedClass(clazztype).type;
             }
             JCTree clazz = makeExpression(clazztype);
             return m().TypeCast(clazz, translatedExpr());
         }
     }
     
     @Override
     public void visitTypeCast(final JCTypeCast tree) {
         result = new TypeCastTranslator(tree, this) {
             protected JCExpression translatedExpr() {
                 return translate(tree.expr);
             }
         }.doit();
     }
     
     @Override
     public void visitLabelled(JCLabeledStatement tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitLiteral(JCLiteral tree) {
         if (tree.typetag == TypeTags.BOT && types.isSequence(tree.type)) {
             Type elemType = elementType(tree.type);
             result = makeEmptySequenceCreator(tree.pos(), elemType);
         }
         else
             result = make.at(tree.pos).Literal(tree.typetag, tree.value);
     }
 
     @Override
     public void visitMethodDef(JCMethodDecl tree) {
          assert false : "should not be in JavaFX AST";
     }
 
     abstract static class FunctionCallTranslator extends Translator {
 
         protected final JCExpression meth;
         protected final JCExpression selector;
         protected final MethodSymbol msym;
         protected final boolean renameToSuper;
         protected final boolean superToStatic;
         protected final List<Type> formals;
         protected final boolean usesVarArgs;
         protected final boolean useInvoke;
         protected final boolean selectorMutable;
         protected final boolean callBound;
 
         FunctionCallTranslator(final JCMethodInvocation tree, JavafxToJava toJava) {
             super(tree.pos(), toJava);
             meth = tree.meth;
             JCFieldAccess fieldAccess = meth.getTag() == JavafxTag.SELECT ? (JCFieldAccess) meth : null;
             selector = fieldAccess != null ? fieldAccess.getExpression() : null;
             Symbol sym = toJava.expressionSymbol(meth);
             msym = (sym instanceof MethodSymbol) ? (MethodSymbol) sym : null;
             Name selectorIdName = (selector != null && selector.getTag() == JavafxTag.IDENT) ? ((JCIdent) selector).getName() : null;
             boolean thisCall = selectorIdName == toJava.names._this;
             boolean superCall = selectorIdName == toJava.names._super;
             ClassSymbol csym = toJava.attrEnv.enclClass.sym;
             
             boolean namedSuperCall =
                     selector != null && msym != null && !msym.isStatic() &&
                     toJava.expressionSymbol(selector) instanceof ClassSymbol &&
                     // FIXME should also allow other enclosing classes:
                     types.isSuperType(toJava.expressionSymbol(selector).type, csym);
             renameToSuper = namedSuperCall && !types.isCompoundClass(csym);
             superToStatic = (superCall || namedSuperCall) && !renameToSuper;
             formals = meth.type.getParameterTypes();
             //TODO: probably move this local to the arg processing
             usesVarArgs = tree.args != null && msym != null &&
                             (msym.flags() & VARARGS) != 0 &&
                             (formals.size() != tree.args.size() ||
                              types.isConvertible(tree.args.last().type,
                                  types.elemtype(formals.last())));
 
             useInvoke = meth.type instanceof FunctionType;
             selectorMutable = msym != null &&
                     !sym.isStatic() && selector != null && !superCall && !namedSuperCall &&
                     !thisCall && !renameToSuper;
             callBound = msym != null && !useInvoke && 
                   ((msym.flags() & JavafxFlags.BOUND) != 0);
         }
     }
 
     // fix me: there must be a better way...
     private boolean isJavaLangObjectType(Type type) {
         return type.toString().equals("java.lang.Object");
     }
 
 
     @Override
     public void visitApply(final JCMethodInvocation tree) {
         result = (new FunctionCallTranslator( tree, this ) {
 
             private final boolean hasSideEffects = selectorMutable && hasSideEffects(selector);
 
             public JCTree doit() {
                 JCVariableDecl selectorVar = null;
                 JCExpression transMeth;
                 if (renameToSuper) {
                     transMeth = make.at(selector).Select(make.Select(makeTypeTree( selector,currentClass.sym.type, false), names._super), msym);
                 } else {
                     transMeth = translate(meth);
                     if (hasSideEffects && !selector.type.isPrimitive() && transMeth.getTag() == JavafxTag.SELECT) {
                         // still a select and presumed to still have side effects -- hold the selector in a temp var
                         JCFieldAccess transMethFA = (JCFieldAccess) transMeth;
                         selectorVar = makeTmpVar(diagPos, "select", selector.type, transMethFA.getExpression());
                         transMeth = m().Select(m().Ident(selectorVar.name), transMethFA.name);
                     }  
                 }
 
                 // translate the method name -- e.g., foo  to foo$bound or foo$impl
                 //TODO: this is a paranoid cloning of the below -- integrate this
                 if (superToStatic) {
                     Name name = functionName(msym, superToStatic, callBound);
                     if (transMeth.getTag() == JavafxTag.IDENT) {
                         transMeth = m().Ident(name);
                     } else if (transMeth.getTag() == JavafxTag.SELECT) {
                         transMeth = m().Select(makeTypeTree(diagPos, msym.owner.type, false), name);
                     }
                 } else 
                 if (callBound && ! renameToSuper) {
                     Name name = functionName(msym, superToStatic, callBound);
                     if (transMeth.getTag() == JavafxTag.IDENT) {
                         transMeth = m().Ident(name);
                     } else if (transMeth.getTag() == JavafxTag.SELECT) {
                         JCFieldAccess faccess = (JCFieldAccess) transMeth;
                         transMeth = m().Select(faccess.getExpression(), name);
                     }
                 }
                 if (useInvoke) {
                     transMeth = make.Select(transMeth, defs.invokeName);
                 }
 
                 JCMethodInvocation app = m().Apply( translate(tree.typeargs), transMeth, determineArgs());
                 JCExpression fresult = app;
                 if (callBound) {
                     fresult = makeBoundCall(app);
                 }
                 if (useInvoke) {
                     if (tree.type.tag != TypeTags.VOID) {
                         fresult = castFromObject(fresult, tree.type);
                     }
                 }
                 // If we are to yield a Location, and this isn't going to happen as
                 // a return of using a bound call (for example, if this is a Java call)
                 // then convert into a Location
                 if (wrap == Wrapped.InLocation && !callBound && msym != null) {
                     TypeMorphInfo returnTypeInfo = typeMorpher.typeMorphInfo(msym.getReturnType());
                     fresult = makeUnboundLocation(diagPos, returnTypeInfo, fresult);
                 }
                 if (selectorMutable && !selector.type.isPrimitive()) {  // if mutable (and not primitive), we need to test for null
                     JCExpression toTest = hasSideEffects? 
                                              m().Ident(selectorVar.name) :
                                              translate(selector);
                     // we have a testable guard for null, test before the invoke (boxed conversions don't need a test)
                     JCExpression cond = m().Binary(JCTree.NE, toTest, make.Literal(TypeTags.BOT, null));
                     if (msym.getReturnType() == syms.voidType) {
                         // if this is a void expression, check it with an If-statement
                         JCStatement stmt = make.If(cond, make.Exec(fresult), null);
                         assert yield == Yield.ToExecStatement : "Yield from a void call should always be a statement";
                         // a statement is the desired result of the translation, return the If-statement
                         if (hasSideEffects) {
                             // if the selector has side-effects, we created a temp var to hold it
                             // so we need to make a block to include the temp var
                             return m().Block(0L, List.<JCStatement>of(selectorVar, stmt));
                         } else {
                             return stmt;
                         }
                     } else {
                         // it has a non-void return type, convert it to a conditional expression
                         // if it would dereference null, then instead give the default value
                         TypeMorphInfo returnTypeInfo = typeMorpher.typeMorphInfo(msym.getReturnType());
                         JCExpression defaultExpr = makeDefaultValue(diagPos, returnTypeInfo);
                         if (wrap == Wrapped.InLocation) {
                             defaultExpr = makeUnboundLocation(diagPos, returnTypeInfo, defaultExpr);
                         }
                         fresult =  m().Conditional(cond, fresult, defaultExpr);
                         if (hasSideEffects) {
                             // if the selector has side-effects, we created a temp var to hold it
                             // so we need to make a block-expression to include the temp var
                             fresult = fxmake.at(diagPos).BlockExpression(0L, List.<JCStatement>of(selectorVar), fresult);
                         }
                     }
                 }
                 return fresult;
             }
 
             // compute the translated arguments.
             // if this is a bound call, use left-hand side references for arguments consisting
             // solely of a  var or attribute reference, or function call, otherwise, wrap it
             // in an expression location
             List<JCExpression> determineArgs() {
                 List<JCExpression> args;
                 if (callBound) {
                     ListBuffer<JCExpression> targs = ListBuffer.lb();
                     List<Type> formal = formals;
                     for (JCExpression arg : tree.args) {
                         switch (arg.getTag()) {
                             case JavafxTag.IDENT:
                             case JavafxTag.SELECT:
                             case JavafxTag.APPLY:
                                 // This arg expression is one that will translate into a Location;
                                 // since that is needed for a this into Location, do so.
                                 // However, if the types need to by changed (subclass), this won't
                                 // directly work.
                                 // Also, if this is a mismatched sequence type, we will need
                                 // to do some different
                                 //TODO: never actually gets here
                                 if (arg.type.equals(formal.head) || 
                                     types.isSequence(formal.head) ||
                                     isJavaLangObjectType(formal.head) // don't add conversion for parameter type of java.lang.Object: doing so breaks the Pointer trick to obtain the original location (JFC-826)
                                     ) {
                                     targs.append(translate(arg, Wrapped.InLocation));
                                     break;
                                 }
                                 //TODO: handle sequence subclasses
                                 //TODO: use more efficient mechanism (use currently apears rare)
                                 //System.err.println("Not match: " + arg.type + " vs " + formal.head);
                                 // Otherwise, fall-through, presumably a conversion will work.
                             default:
                             {
                                 targs.append(makeUnboundLocation(
                                         arg.pos(),
                                         typeMorpher.typeMorphInfo(formal.head),
                                         translate(arg, arg.type)));
                             }
                         }
                         formal = formal.tail;
                     }
                     args = targs.toList();
                 } else {
                     ListBuffer<JCExpression> translated = ListBuffer.lb();
                     boolean handlingVarargs = false;
                     Type formal = null;
                     List<Type> t = formals;
 	            for (List<JCExpression> l = tree.args; l.nonEmpty();  l = l.tail) {
                         if (! handlingVarargs) {
                             formal = t.head;
                             t = t.tail;
                             if (usesVarArgs && t.isEmpty()) {
                                 formal = types.elemtype(formal);
                                 handlingVarargs = true;
                             }
                         }
                         translated.append( translate(l.head, formal) );
                     }
 	            args = translated.toList();
                 }
 
                // if this is a super.foo(x) call, "super" will be translated to referenced class,
                 // so we add a receiver arg to make a direct call to the implementing method  MyClass.foo(receiver$, x)
                 if (superToStatic) {
                     args = args.prepend(make.Ident(defs.receiverName));
                 }
 
                 return args;
             }
 
             // This is for calls from non-bound contexts (code for true bound calls is in JavafxToBound)
             JCExpression makeBoundCall(JCExpression applyExpression) {
                 JavafxTypeMorpher.TypeMorphInfo tmi = typeMorpher.typeMorphInfo(msym.getReturnType());
                 return wrap == Wrapped.InLocation ? applyExpression : callExpression(diagPos,
                         m().Parens(applyExpression),
                         defs.locationGetMethodName[tmi.getTypeKind()]);
             }
 
         }).doit();
     }
 
     @Override
     public void visitModifiers(JCModifiers tree) {
         List<JCAnnotation> annotations = translate(tree.annotations);
         result = make.at(tree.pos).Modifiers(tree.flags, annotations);
     }
 
     @Override
     public void visitNewArray(JCNewArray tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitNewClass(JCNewClass tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitSkip(JCSkip tree) {
         result = make.at(tree.pos).Skip();
     }
 
     @Override
     public void visitSwitch(JCSwitch tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitSynchronized(JCSynchronized tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitThrow(JCThrow tree) {
         JCTree expr = translate(tree.expr);
         result = make.at(tree.pos).Throw(expr);
     }
 
     @Override
     public void visitTry(JCTry tree) {
         JCBlock body = translate(tree.body);
         List<JCCatch> catchers = translate(tree.catchers);
         JCBlock finalizer = translate(tree.finalizer);
         result = make.at(tree.pos).Try(body, catchers, finalizer);
     }
 
     @Override
     public void visitTypeApply(JCTypeApply tree) {
         JCExpression clazz = translate(tree.clazz);
         List<JCExpression> arguments = translate(tree.arguments);
         result = make.at(tree.pos).TypeApply(clazz, arguments);
     }
 
     @Override
     public void visitTypeArray(JCArrayTypeTree tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitTypeBoundKind(TypeBoundKind tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitTypeIdent(JCPrimitiveTypeTree tree) {
         result = make.at(tree.pos).TypeIdent(tree.typetag);
     }
 
     @Override
     public void visitTypeParameter(JCTypeParameter tree) {
         List<JCExpression> bounds = translate(tree.bounds);
         result = make.at(tree.pos).TypeParameter(tree.name, bounds);
     }
 
     @Override
     public void visitUnary(final JCUnary tree) {
         result = (new Translator( tree.pos(), this ) {
 
             private final JCExpression expr = tree.getExpression();
             private final JCExpression transExpr = translate(expr);
 
             private JCExpression doVanilla() {
                 return m().Unary(tree.getTag(), transExpr);
             }
             
             private JCExpression callSetMethod(JCExpression target, List<JCExpression> args) {
                 JCExpression transTarget = translate(target, Wrapped.InLocation); // must be Location
                 JCFieldAccess setSelect = m().Select(transTarget,
                                                       args.size() == 1
                                                               ? defs.locationSetMethodName[typeMorpher.typeMorphInfo(target.type).getTypeKind()]
                                                               : defs.setMethodName);
                 return m().Apply(null, setSelect, args);
             }
 
             private JCExpression doIncDec(int binaryOp, boolean postfix) {
                 final Symbol sym = expressionSymbol(expr);
                 final VarSymbol vsym = (sym != null && (sym instanceof VarSymbol)) ? (VarSymbol) sym : null;
                 final JCExpression one = m().Literal(1);
                 final JCExpression combined = m().Binary(binaryOp, transExpr, one);
                 JCExpression ret;
 
                 if (expr.getTag() == JavafxTag.SEQUENCE_INDEXED) {
                     // assignment of a sequence element --  s[i]+=8, call the sequence set method
                     JFXSequenceIndexed si = (JFXSequenceIndexed) expr;
                     JCExpression index = translate(si.getIndex());
                     ret = callSetMethod( si.getSequence(), List.of(index, combined) );
                 } else if (shouldMorph(vsym)) {
                     // we are setting a var Location, call the set method
                     ret = callSetMethod( expr, List.of(combined) );
                 } else {
                     // We are setting a "normal" non-Location non-sequence-access, use normal operator
                     return doVanilla();
                 }
                 if (postfix) {
                     // this is a postfix operation, undo the value (not the variable) change
                     return m().Binary(binaryOp, ret, m().Literal(-1));
                 } else {
                     // prefix operation
                     return ret;
                 }
             }
 
             public JCTree doit() {
                 switch (tree.getTag()) {
                     case JavafxTag.SIZEOF:
                         return callExpression(diagPos, 
                                 makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"), 
                                 defs.sizeMethodName, transExpr);
                     case JavafxTag.REVERSE:
                         return callExpression(diagPos, 
                                 makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"), 
                                 "reverse", transExpr);
                     case JCTree.PREINC:
                         return doIncDec(JCTree.PLUS, false);
                     case JCTree.PREDEC:
                         return doIncDec(JCTree.MINUS, false);
                     case JCTree.POSTINC:
                         return doIncDec(JCTree.PLUS, true);
                     case JCTree.POSTDEC:
                         return doIncDec(JCTree.MINUS, true);
                     case JCTree.NEG:
                         if (types.isSameType(tree.type, syms.javafx_DurationType)) {
                             return m().Apply(null,
                                                           m().Select(translate(tree.arg), Name.fromString(names, "negate")), List.<JCExpression>nil());
                         }
                     default:
                         return doVanilla();
                 }
             }
         }).doit();
     }
   
 
     @Override
     public void visitVarDef(JCVariableDecl tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitWhileLoop(JCWhileLoop tree) {
         JCStatement body = translate(tree.body);
         JCExpression cond = translate(tree.cond);
         result = make.at(tree.pos).WhileLoop(cond, body);
     }
 
     @Override
     public void visitWildcard(JCWildcard tree) {
         TypeBoundKind kind = make.at(tree.kind.pos).TypeBoundKind(tree.kind.kind);
         JCTree inner = translate(tree.inner);
         result = make.at(tree.pos).Wildcard(kind, inner);
     }
 
     @Override
     public void visitLetExpr(LetExpr tree) {
         assert false : "should not be in JavaFX AST";
     }
 
     @Override
     public void visitTree(JCTree tree) {
         assert false : "should not be in JavaFX AST";
     }
     
     /******** goofy visitors, most of which should go away ******/
 
     public void visitSetAttributeToObjectBeingInitialized(JFXSetAttributeToObjectBeingInitialized that) {
         assert false : "yeah, right";
     }
 
     public void visitObjectLiteralPart(JFXObjectLiteralPart that) {
         assert false : "should be processed by parent tree";
     }  
     
     public void visitTypeAny(JFXTypeAny that) {
         assert false : "should be processed by parent tree";
     }
     
     public void visitTypeClass(JFXTypeClass that) {
         assert false : "should be processed by parent tree";
     }
     
     public void visitTypeFunctional(JFXTypeFunctional that) {
         assert false : "should be processed by parent tree";
     }
     
     public void visitTypeUnknown(JFXTypeUnknown that) {
         assert false : "should be processed by parent tree";
     }
 
     public void visitForExpressionInClause(JFXForExpressionInClause that) {
         assert false : "should be processed by parent tree";
     }
     
     /***********************************************************************
      *
      * Utilities 
      *
      */
     
     private JCBlock runMethodBody(JFXBlockExpression bexpr) {
         DiagnosticPosition diagPos = bexpr.pos();
         List<JCStatement> stats = translateStatements(bexpr.stats);
         boolean nullReturnNeeded = true;
         if (bexpr.value != null) {
             Type valueType = bexpr.value.type;
             if (valueType == syms.voidType) {
                 // convert the void typed expression to a statement, still return null
                 stats = stats.append( translateExpressionToStatement(bexpr.value, valueType) );
             } else {
                 // the returned value will be the trailing expression, box if needed
                 //TODO: handle cases of single legged if, etc
                 if (valueType.isPrimitive()) {
                     JCExpression boxedExpr = makeBox(diagPos, translate(bexpr.value), valueType);
                     stats = stats.append( make.Return( boxedExpr ) );
                 } else {
                     stats = stats.append( translateExpressionToStatement(bexpr.value, valueType) );
                 }
                 nullReturnNeeded = false;
             }
         }
         if (nullReturnNeeded) {
             stats = stats.append( make.Return(make.Literal(TypeTags.BOT, null)) );
         }
         return make.at(diagPos).Block(0L, stats);
     }
     
     protected String getSyntheticPrefix() {
         return "jfx$";
     }
     
     boolean shouldMorph(VarSymbol vsym) {
         if (vsym == null) {
             return false;
         }
         VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
         return shouldMorph(vmi);
     }
     
     boolean shouldMorph(VarMorphInfo vmi) {
         if ( vmi.mustMorph() )
             return true;
         else
             return locallyBound != null ? locallyBound.contains(vmi.getSymbol()) : false;
     }   
 
     JCExpression convertVariableReference(DiagnosticPosition diagPos,
                                                  JCExpression varRef, Symbol sym, 
                                                  boolean wantLocation) {
 
         JCExpression expr = varRef;
 
         boolean staticReference = sym.isStatic();
         if (sym instanceof VarSymbol) {
              VarSymbol vsym = (VarSymbol) sym;
             VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
             if (shouldMorph(vmi)) {
                 if (sym.owner.kind == Kinds.TYP && !staticReference) {
                     // this is a non-static reference to an attribute, use the get$ form
                     assert varRef.getTag() == JCTree.SELECT : "attribute must be accessed through receiver";
                     JCFieldAccess select = (JCFieldAccess) varRef;
                     Name attrAccessName = names.fromString(attributeGetMethodNamePrefix + select.name.toString());
                     select = make.at(diagPos).Select(select.getExpression(), attrAccessName);
                     List<JCExpression> emptyArgs = List.nil();
                     expr = make.at(diagPos).Apply(null, select, emptyArgs);
                 }
 
                 if (wantLocation) {
                     // already in correct form-- leave it
                 } else {
                     // non-bind context -- want v1.get()
                     JCFieldAccess getSelect = make.Select(expr, defs.locationGetMethodName[vmi.getTypeKind()]);
                     List<JCExpression> getArgs = List.nil();
                     expr = make.at(diagPos).Apply(null, getSelect, getArgs);
                 }
             } else {
                 // not morphed
                 if (wantLocation) {
                     expr = makeUnboundLocation(diagPos, vmi, expr);
                 }                
             }
         } else if (sym instanceof MethodSymbol) {
             if (staticReference) {
                 Name name = functionName((MethodSymbol)sym);
                 if (expr.getTag() == JCTree.SELECT) {
                     JCFieldAccess select = (JCFieldAccess) expr;
                     expr = make.at(diagPos).Select(select.getExpression(), name);
                 } else if (expr.getTag() == JCTree.IDENT) {
                     expr = make.at(diagPos).Ident(name);
                 }
             }
         }
          return expr;
     }
 
     /**
      * Build the AST for accessing the outer member. 
      * The accessors might be chained if the member accessed is more than one level up in the outer chain.
      * */
     private JCExpression makeReceiver(DiagnosticPosition pos, Symbol treeSym, Symbol siteOwner) {
         JCExpression ret = null;
         if (treeSym != null && siteOwner != null) {
             
             ret = make.Ident(defs.receiverName);
             ret.type = siteOwner.type;
             
             // check if it is in the chain
             if (siteOwner != treeSym.owner) {
                 Symbol siteCursor = siteOwner;
                 boolean foundOwner = false;
                 int numOfOuters = 0;
                 ownerSearch:
                 while (siteCursor.kind != Kinds.PCK) {            
                     ListBuffer<Type> supertypes = ListBuffer.lb();
                     Set<Type> superSet = new HashSet<Type>();
                     if (siteCursor.type != null) {
                         supertypes.append(siteCursor.type);
                         superSet.add(siteCursor.type);
                     }
 
                     if (siteCursor.kind == Kinds.TYP) {
                         types.getSupertypes(siteCursor, supertypes, superSet);
                     }
 
                     for (Type supType : supertypes) {
                         if (types.isSameType(supType, treeSym.owner.type)) {
                             foundOwner = true;
                             break ownerSearch;
                         }
                     }
 
                     if (siteCursor.kind == Kinds.TYP) {
                         numOfOuters++;
                     }
                     
                     siteCursor = siteCursor.owner;
                 }
 
                 if (foundOwner) {
                     // site was found up the outer class chain, add the chaining accessors
                     siteCursor = siteOwner;
                     while (numOfOuters > 0) {
                         if (siteCursor.kind == Kinds.TYP) {
                             ret = callExpression(pos, ret, initBuilder.outerAccessorName);
                             ret.type = siteCursor.type;
                         }
                         
                         if (siteCursor.kind == Kinds.TYP) {
                             numOfOuters--;
                         }
                         siteCursor = siteCursor.owner;
                     }
                 }
             }
         }
         return ret;
     }
 
     private void fillClassesWithOuters(JCCompilationUnit tree) {
         class FillClassesWithOuters extends JavafxTreeScanner {
             JFXClassDeclaration currentClass;
             
             @Override
             public void visitClassDeclaration(JFXClassDeclaration tree) {
                 JFXClassDeclaration prevClass = currentClass;
                 try {
                     currentClass = tree;
                     super.visitClassDeclaration(tree);
                 }
                 finally {
                     currentClass = prevClass;
                 }
             }
             
             @Override
             public void visitIdent(JCIdent tree) {
                 super.visitIdent(tree);
                 if (currentClass != null && tree.sym.kind != Kinds.TYP) {
                     addOutersForOuterAccess(tree.sym, currentClass.sym);
                 }
             }
             
             @Override // Need this because JavafxTreeScanner is not visiting the args of the JFXInstanciate tree. Starting to visit them generate tons of errors.
             public void visitInstanciate(JFXInstanciate tree) {
                 super.visitInstanciate(tree);
                 super.scan(tree.getArgs());
             }
 
             private void addOutersForOuterAccess(Symbol sym, Symbol currentClass) {
                 if (sym != null && sym.owner != null && sym.owner.type != null
                         && !sym.isStatic() && currentClass != null) {
                     Symbol outerSym = currentClass;
                     ListBuffer<ClassSymbol> potentialOuters = new ListBuffer<ClassSymbol>();
                     boolean foundOuterOwner = false;
                     while (outerSym != null) {
                         if (outerSym.kind == Kinds.TYP) {
                             ClassSymbol outerCSym = (ClassSymbol) outerSym;
                             if (types.isSuperType(sym.owner.type, outerCSym)) {
                                 foundOuterOwner = true;
                                 break;
                              }
                             potentialOuters.append(outerCSym);
                         }
                         else if (sym.owner == outerSym)
                             break;
                         outerSym = outerSym.owner;
                     }
                     
                     if (foundOuterOwner) {
                         for (ClassSymbol cs : potentialOuters) {
                             hasOuters.add(cs);
                         }
                     }
                 }
             }
         }
         
         new FillClassesWithOuters().scan(tree);
     }
 
     @Override
     public void visitTimeLiteral(JFXTimeLiteral tree) {        
         // convert this time literal to a javafx.lang.Duration object literal
         JFXInstanciate duration = timeLiteralToDuration(tree); // sets result
  
         // now convert that object literal to Java
         visitInstanciate(duration); // sets result
    }
 
     public void visitInterpolate(JFXInterpolate tree) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void visitInterpolateValue(final JFXInterpolateValue tree) {
         DiagnosticPosition diagPos = tree.pos();
         JCExpression clsname = fxmake.at(diagPos).Type(syms.javafx_KeyValueType);
         ((JCFieldAccess) clsname).sym = syms.javafx_KeyValueType.tsym;
         final Symbol targetSymbol = syms.javafx_KeyValueType.tsym.members().lookup(defs.targetName).sym;
         JFXObjectLiteralPart targetLiteral = fxmake.at(tree.pos()).ObjectLiteralPart(defs.targetName, tree.attribute);
         targetLiteral.sym = targetSymbol;
         JFXObjectLiteralPart valueLiteral =
                 fxmake.at(tree.pos()).ObjectLiteralPart(defs.valueName, tree.value, JavafxBindStatus.UNIDIBIND);
         valueLiteral.sym = syms.javafx_KeyValueType.tsym.members().lookup(defs.valueName).sym;
         List<JCTree> parts;
         if (tree.interpolation == null)
             parts = List.<JCTree>of(targetLiteral, valueLiteral);
         else {
             JFXObjectLiteralPart interpolateLiteral =
                     fxmake.at(tree.pos()).ObjectLiteralPart(defs.interpolateName, tree.interpolation, JavafxBindStatus.UNIDIBIND);
             interpolateLiteral.sym = syms.javafx_KeyValueType.tsym.members().lookup(defs.interpolateName).sym;
             parts = List.<JCTree>of(targetLiteral, valueLiteral, interpolateLiteral);
         }
 
         JFXInstanciate inst = fxmake.at(diagPos).Instanciate(clsname, null, parts);
         inst.type = clsname.type;
         result = new InstanciateTranslator(inst, this) {
             protected void processLocalVar(JFXVar var) {
             }
 
             protected JCStatement translateAttributeSet(JCExpression init, JavafxBindStatus bindStatus, VarSymbol vsym, JCExpression instance) {
                 if (targetSymbol==vsym) {
                     JCExpression target = translate(init, Wrapped.InLocation);
                     target = callExpression(diagPos, make.Type(syms.javafx_PointerType), "make", target);
                     VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
                     JCExpression localAttr;
 
                     // if it is an attribute
                     if (vsym.owner.kind == Kinds.TYP) {
                         if ((vsym.flags() & STATIC) != 0) {
                             // statics are accessed directly
                             localAttr = make.Ident(vsym);
                         } else {
                             String attrAccess = attributeGetMethodNamePrefix + vsym;
                             localAttr = callExpression(diagPos, instance, attrAccess);
                         }
                     } else {
                         // if it is a local variable
                         assert( (vsym.flags() & Flags.PARAMETER) == 0): "Parameters are not initialized";
                         localAttr = make.at(diagPos).Ident(vsym);   
                    }
                    return make.at(diagPos).Exec(callExpression(diagPos, localAttr, defs.locationSetMilieuMethodName[vmi.getTypeKind()][FROM_LITERAL_MILIEU], target));
                } else {
                     return toJava.translateDefinitionalAssignmentToSet(diagPos, init, bindStatus,
                         vsym, instance, FROM_LITERAL_MILIEU);
                 }
             }
         }.doit();
     }
 
     public void visitKeyFrameLiteral(JFXKeyFrameLiteral tree) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
