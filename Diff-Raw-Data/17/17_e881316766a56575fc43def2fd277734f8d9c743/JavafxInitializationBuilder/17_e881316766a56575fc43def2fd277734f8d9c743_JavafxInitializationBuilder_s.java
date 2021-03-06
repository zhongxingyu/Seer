 /*
  * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
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
 
 import com.sun.tools.javac.code.*;
 import com.sun.tools.javac.code.Symbol.ClassSymbol;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Symbol.VarSymbol;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.*;
 import com.sun.tools.javac.tree.TreeMaker;
 import com.sun.tools.javac.util.*;
 import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
 import com.sun.tools.javafx.code.JavafxFlags;
 import com.sun.tools.javafx.code.JavafxSymtab;
 import com.sun.tools.javafx.comp.JavafxAnalyzeClass.*;
 import static com.sun.tools.javafx.comp.JavafxDefs.*;
 import com.sun.tools.javafx.comp.JavafxTypeMorpher.VarMorphInfo;
 import com.sun.tools.javafx.tree.*;
 import static com.sun.tools.javafx.comp.JavafxTypeMorpher.VarRepresentation.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Build the representation(s) of a JavaFX class.  Includes class initialization, attribute and function proxies.
  * With support for mixins.
  * 
  * @author Robert Field
  * @author Lubo Litchev
  * @author Per Bothner
  * @author Zhiqun Chen
  * @author Jim Laskey
  */
 public class JavafxInitializationBuilder extends JavafxTranslationSupport {
     protected static final Context.Key<JavafxInitializationBuilder> javafxInitializationBuilderKey =
         new Context.Key<JavafxInitializationBuilder>();
 
     private final JavafxToJava toJava;
     private final JavafxClassReader reader;
     private final JavafxOptimizationStatistics optStat;
     
     private static final String initHelperClassName = "com.sun.javafx.runtime.InitHelper";
     Name outerAccessorName;
     Name outerAccessorFieldName;
     Name makeInitMap;
     
     Name varNumName;
     Name varLocalNumName;
     Name varWordName;
     Name varBitName;
 
     final Type initHelperType;
     final Type abstractVariableType;
     final Type locationDependencyType;
     final Type locationType;
     
     public static class LiteralInitVarMap {
         private int count = 1;
         public Map<VarSymbol, Integer> varMap = new HashMap<VarSymbol, Integer>();
         public ListBuffer<VarSymbol> varList = ListBuffer.lb();
         
         public int addVar(VarSymbol sym) {
             Integer value = varMap.get(sym);
             
             if (value == null) {
                 value = new Integer(count++);
                 varMap.put(sym, value);
                 varList.append(sym);
             }
             
             return value.intValue();
         }
 
         public int size() {
             return varMap.size();
         }
     }
     
     public static class LiteralInitClassMap {
         public Map<ClassSymbol, LiteralInitVarMap> classMap = new HashMap<ClassSymbol, LiteralInitVarMap>();
         
         public LiteralInitVarMap getVarMap(ClassSymbol sym) {
             LiteralInitVarMap map = classMap.get(sym);
             
             if (map == null) {
                 map = new LiteralInitVarMap();
                 classMap.put(sym, map);
             }
             
             return map;
         }
         
         public int size() {
             return classMap.size();
         }
     }
     
     public static JavafxInitializationBuilder instance(Context context) {
         JavafxInitializationBuilder instance = context.get(javafxInitializationBuilderKey);
         if (instance == null)
             instance = new JavafxInitializationBuilder(context);
         return instance;
     }
 
     protected JavafxInitializationBuilder(Context context) {
         super(context);
         
         context.put(javafxInitializationBuilderKey, this);
 
         toJava = JavafxToJava.instance(context);
         reader = (JavafxClassReader) JavafxClassReader.instance(context);
         optStat = JavafxOptimizationStatistics.instance(context);
         
         outerAccessorName = names.fromString("accessOuter$");
         outerAccessorFieldName = names.fromString("accessOuterField$");
         makeInitMap = names.fromString("makeInitMap$");
         
         varNumName = names.fromString("varNum$");
         varLocalNumName = names.fromString("varLocalNum$");
         varWordName = names.fromString("varWord$");
         varBitName = names.fromString("varBit$");
         
         {
             Name name = names.fromString(initHelperClassName);
             ClassSymbol sym = reader.enterClass(name);
             initHelperType = sym.type;
         }
         {
             Name name = names.fromString(locationPackageNameString + ".AbstractVariable");
             ClassSymbol sym = reader.enterClass(name);
             abstractVariableType = types.erasure( sym.type );
         }
         {
             Name name = names.fromString(locationPackageNameString + ".LocationDependency");
             ClassSymbol sym = reader.enterClass(name);
             locationDependencyType = types.erasure( sym.type );
         }
         {
             Name name = names.fromString(locationPackageNameString + ".Location");
             ClassSymbol sym = reader.enterClass(name);
             locationType = types.erasure( sym.type );
         }
     }
 
     /**
      * Hold the result of analyzing the class.
      * */
     static class JavafxClassModel {
         final Name interfaceName;
         final List<JCExpression> interfaces;
         final List<JCTree> iDefinitions;
         final List<JCTree> additionalClassMembers;
         final List<JCExpression> additionalImports;
         final Type superType;
         final ClassSymbol superClassSym;
         final List<ClassSymbol> superClasses;
         final List<ClassSymbol> immediateMixins;
         final List<ClassSymbol> allMixins;
 
         JavafxClassModel(
                 Name interfaceName,
                 List<JCExpression> interfaces,
                 List<JCTree> iDefinitions,
                 List<JCTree> addedClassMembers,
                 List<JCExpression> additionalImports,
                 Type superType,
                 ClassSymbol superClassSym,
                 List<ClassSymbol> superClasses,
                 List<ClassSymbol> immediateMixins,
                 List<ClassSymbol> allMixins) {
             this.interfaceName = interfaceName;
             this.interfaces = interfaces;
             this.iDefinitions = iDefinitions;
             this.additionalClassMembers = addedClassMembers;
             this.additionalImports = additionalImports;
             this.superType = superType;
             this.superClassSym = superClassSym;
             this.superClasses = superClasses;
             this.immediateMixins = immediateMixins;
             this.allMixins = allMixins;
         }
     }
 
     /**
      * Analyze the class.
      * 
      * Determine what methods will be needed to access attributes.
      * Determine what methods will be needed to proxy to the static implementations of functions.
      * Determine what other misc fields and methods will be needed.
      * Create the corresponding interface.
      * 
      * Return all this as a JavafxClassModel for use in translation.
      * */
    JavafxClassModel createJFXClassModel(JFXClassDeclaration cDecl, 
            List<TranslatedVarInfo> translatedAttrInfo,
            List<TranslatedOverrideClassVarInfo> translatedOverrideAttrInfo,
            LiteralInitClassMap initClassMap) {
            
         DiagnosticPosition diagPos = cDecl.pos();
         Type superType = types.superType(cDecl);
         ClassSymbol outerTypeSym = outerTypeSymbol(cDecl); // null unless inner class with outer reference
 
         JavafxAnalyzeClass analysis = new JavafxAnalyzeClass(diagPos,
                 cDecl.sym, translatedAttrInfo, translatedOverrideAttrInfo,
                 names, types, reader, typeMorpher);
         JavaCodeMaker javaCodeMaker = new JavaCodeMaker(analysis);
         List<VarInfo> instanceAttributeInfos = analysis.instanceAttributeInfos();
         List<VarInfo> staticAttributeInfos = analysis.staticAttributeInfos();
         List<MethodSymbol> needDispatch = analysis.needDispatch();
         ClassSymbol fxSuperClassSym = analysis.getFXSuperClassSym();
         List<ClassSymbol> superClasses = analysis.getSuperClasses();
         List<ClassSymbol> immediateMixinClasses = analysis.getImmediateMixins();
         List<ClassSymbol> allMixinClasses = analysis.getAllMixins();
 
         boolean isMixinClass = cDecl.isMixinClass();
         boolean isScriptClass = cDecl.isScriptClass;
         boolean isAnonClass = analysis.isAnonClass();
         boolean hasFxSuper = fxSuperClassSym != null;
         
         // Have to populate the var map for anon classes.
         // TODO: figure away to avoid this if not used (needs global knowledge.)
         LiteralInitVarMap varMap = isAnonClass ? initClassMap.getVarMap(analysis.getCurrentClassSymbol()) : null;
         
         ListBuffer<JCTree> cDefinitions = ListBuffer.lb();  // additional class members needed
         ListBuffer<JCTree> iDefinitions = ListBuffer.lb();
          
         if (!isMixinClass) {
             cDefinitions.appendList(javaCodeMaker.makeAttributeNumbers(varMap));
             cDefinitions.appendList(javaCodeMaker.makeAttributeFields(instanceAttributeInfos));
             cDefinitions.appendList(javaCodeMaker.makeAttributeFields(staticAttributeInfos));
             cDefinitions.appendList(javaCodeMaker.makeAttributeAccessorMethods(instanceAttributeInfos));
             cDefinitions.appendList(javaCodeMaker.makeAttributeAccessorMethods(staticAttributeInfos));
             cDefinitions.appendList(javaCodeMaker.makeIsInitialized());
             cDefinitions.appendList(javaCodeMaker.makeBlanketApplyDefaults());
             cDefinitions.appendList(javaCodeMaker.makeSpecificApplyDefaults());
             cDefinitions.appendList(javaCodeMaker.makeGetDependency());
             
             if (isScriptClass) {
                 cDefinitions.appendList(javaCodeMaker.makeInitClassMaps(initClassMap));
             }
             
             JCStatement initMap = isAnonClass ? javaCodeMaker.makeInitVarMapInit(analysis.getCurrentClassSymbol(), varMap) : null;
             
             cDefinitions.appendList(javaCodeMaker.makeApplyDefaultsMethods(instanceAttributeInfos));
             cDefinitions.append    (makeInitStaticAttributesBlock(cDecl, translatedAttrInfo, initMap));
             cDefinitions.append    (makeInitializeMethod(diagPos));
 
             if (!hasFxSuper) {
                 // Has a non-JavaFX super, so we can't use FXBase, add the complete$ and initialize$ methods
  //               cDefinitions.append(makeInitializeMethod(diagPos));
                 cDefinitions.append(makeCompleteMethod(diagPos));
 //                           cDefinitions.appendList(javaCodeMaker.makeGeneralApplyDefaults());
             }
 
             if (outerTypeSym == null) {
                 cDefinitions.append(makeJavaEntryConstructor(diagPos));
             } else {
                 cDefinitions.append(makeOuterAccessorField(diagPos, cDecl, outerTypeSym));
                 cDefinitions.append(makeOuterAccessorMethod(diagPos, cDecl, outerTypeSym));
             }
 
             cDefinitions.appendList(makeAddTriggersMethod(diagPos, cDecl, fxSuperClassSym, immediateMixinClasses, translatedAttrInfo, translatedOverrideAttrInfo));
             cDefinitions.appendList(makeFunctionProxyMethods(cDecl, needDispatch));
             cDefinitions.append(makeFXEntryConstructor(diagPos, outerTypeSym, hasFxSuper));
         } else {
             cDefinitions.appendList(javaCodeMaker.makeAttributeFields(instanceAttributeInfos));
             iDefinitions.appendList(javaCodeMaker.makeMemberVariableAccessorInterfaceMethods());
             
             if (isScriptClass) {
                 cDefinitions.appendList(javaCodeMaker.makeInitClassMaps(initClassMap));
            }    
 
             cDefinitions.appendList(javaCodeMaker.makeApplyDefaultsMethods(instanceAttributeInfos));
             iDefinitions.appendList(makeFunctionInterfaceMethods(cDecl));
             iDefinitions.appendList(makeOuterAccessorInterfaceMembers(cDecl));
             cDefinitions.appendList(makeAddTriggersMethod(diagPos, cDecl, fxSuperClassSym, immediateMixinClasses, translatedAttrInfo, translatedOverrideAttrInfo));
         }
         Name interfaceName = isMixinClass ? interfaceName(cDecl) : null;
 
         return new JavafxClassModel(
                 interfaceName,
                 makeImplementingInterfaces(diagPos, cDecl, immediateMixinClasses),
                 iDefinitions.toList(),
                 cDefinitions.toList(),
                 makeAdditionalImports(diagPos, cDecl, immediateMixinClasses),
                 superType,
                 fxSuperClassSym,
                 superClasses,
                 immediateMixinClasses,
                 allMixinClasses);
     }
 
     
     private List<JCTree> makeFunctionInterfaceMethods(JFXClassDeclaration cDecl) {
         ListBuffer<JCTree> methods = ListBuffer.lb();
         for (JFXTree def : cDecl.getMembers()) {
             if (def.getFXTag() == JavafxTag.FUNCTION_DEF) {
                 JFXFunctionDefinition func = (JFXFunctionDefinition) def;
                 MethodSymbol sym = func.sym;
                 if ((sym.flags() & (Flags.SYNTHETIC | Flags.STATIC | Flags.PRIVATE)) == 0) {
                     appendMethodClones(methods, cDecl, sym, false);
                 }
             }
         }
         return methods.toList();
     }
     
     /** add proxies which redirect to the static implementation for every concrete method
      * 
      * @param cDecl
      * @param needDispatch
      * @return
      */
     private List<JCTree> makeFunctionProxyMethods(JFXClassDeclaration cDecl, List<MethodSymbol> needDispatch) {
         ListBuffer<JCTree> methods = ListBuffer.lb();
         for (MethodSymbol sym : needDispatch) {
             if ((sym.flags() & Flags.PRIVATE) == 0) {
                 appendMethodClones(methods, cDecl, sym, true);
             }
         }
         return methods.toList();
     }
     
    /**
      * Make a method from a MethodSymbol and an optional method body.
      * Make a bound version if "isBound" is set.
      */
     private void appendMethodClones(ListBuffer<JCTree> methods, JFXClassDeclaration cDecl, MethodSymbol sym, boolean withDispatch) {
         //TODO: static test is broken
         boolean isBound = (sym.flags() & JavafxFlags.BOUND) != 0;
         JCBlock mthBody = withDispatch ? makeDispatchBody(cDecl, sym, isBound, (sym.flags() & Flags.STATIC) != 0) : null;
         DiagnosticPosition diagPos = cDecl;
         // build the parameter list
         ListBuffer<JCVariableDecl> params = ListBuffer.lb();
         for (VarSymbol vsym : sym.getParameters()) {
             Type vtype = vsym.asType();
             if (isBound) {
                 VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
                 vtype = vmi.getLocationType();
             }
             params.append(make.VarDef(
                     make.Modifiers(0L), 
                     vsym.name, 
                     makeTypeTree(diagPos, vtype), 
                     null // no initial value
                      // no initial value
                     ));
         }
         
         // make the method
         JCModifiers mods = make.Modifiers(Flags.PUBLIC | (mthBody==null? Flags.ABSTRACT : 0L));
         if (sym.owner == cDecl.sym)
             mods = addAccessAnnotationModifiers(diagPos, sym.flags(), mods);
         else
             mods = addInheritedAnnotationModifiers(diagPos, sym.flags(), mods);
         methods.append(make.at(diagPos).MethodDef(
                         mods, 
                         functionName(sym, false, isBound), 
                         makeReturnTypeTree(diagPos, sym, isBound), 
                         List.<JCTypeParameter>nil(), 
                         params.toList(), 
                         List.<JCExpression>nil(), 
                         mthBody, 
                         null));
         if (withDispatch) {
             optStat.recordProxyMethod();
         }
     }
 
     
     private List<JCExpression> makeImplementingInterfaces(DiagnosticPosition diagPos,
             JFXClassDeclaration cDecl, 
             List<ClassSymbol> baseInterfaces) {
         ListBuffer<JCExpression> implementing = ListBuffer.lb();
         
         if (cDecl.isMixinClass()) {
             implementing.append(makeIdentifier(diagPos, fxMixinString));
         } else {
             implementing.append(makeIdentifier(diagPos, fxObjectString));
         }
         
         for (JFXExpression intf : cDecl.getImplementing()) {
             implementing.append(makeTypeTree(diagPos, intf.type, false));
         }
 
         for (ClassSymbol baseClass : baseInterfaces) {
             implementing.append(makeTypeTree(diagPos, baseClass.type, true));
         }
         return implementing.toList();
     }
 
     private List<JCExpression> makeAdditionalImports(DiagnosticPosition diagPos, JFXClassDeclaration cDecl, List<ClassSymbol> baseInterfaces) {
         // Add import statements for all the base classes and basClass $Mixin(s).
         // There might be references to them when the methods/attributes are rolled up.
         ListBuffer<JCExpression> additionalImports = new ListBuffer<JCExpression>();
         for (ClassSymbol baseClass : baseInterfaces) {
             if (baseClass.type != null && baseClass.type.tsym != null &&
                     baseClass.type.tsym.packge() != cDecl.sym.packge() &&     // Work around javac bug (CR 6695838)
                     baseClass.type.tsym.packge() != syms.unnamedPackage) {    // Work around javac bug. the visitImport of Attr 
                 // is casting to JCFieldAcces, but if you have imported an
                 // JCIdent only a ClassCastException is thrown.
                 additionalImports.append(makeTypeTree( diagPos,baseClass.type, false));
                 additionalImports.append(makeTypeTree( diagPos,baseClass.type, true));
             }
         }
         return additionalImports.toList();
     }
    
     /**
      * Make a constructor to be called by Java code.
      * Simply pass up to super, unless this is the last JavaFX class, in which case add object initialization
      * @param diagPos
      * @param superIsFX true if there is a super class (in the generated code) and it is a JavaFX class
      * @return the constructor
      */
     private JCMethodDecl makeJavaEntryConstructor(DiagnosticPosition diagPos) {
         make.at(diagPos);
 
         //    public Foo() {
         //        this(false);
         //        initialize$();
         //    }
         return makeConstructor(diagPos, List.<JCVariableDecl>nil(), List.of(
                 callStatement(diagPos, names._this, make.Literal(TypeTags.BOOLEAN, 0)),
                 callStatement(diagPos, defs.initializeName)));
     }
 
     /**
      * Make a constructor to be called by JavaFX code.
      * @param diagPos
      * @param superIsFX true if there is a super class (in the generated code) and it is a JavaFX class
      * @return the constructor
      */
     private JCMethodDecl makeFXEntryConstructor(DiagnosticPosition diagPos, ClassSymbol outerTypeSym, boolean superIsFX) {    
         make.at(diagPos);
         ListBuffer<JCStatement> stmts = ListBuffer.lb();
         Name dummyParamName = names.fromString("dummy");
 
         // call the FX version of the constructor in the superclass
         //    public Foo(boolean dummy) {
         //        super(dummy);
         //    }
         if (superIsFX) {
             stmts.append(callStatement(diagPos,
                 names._super,
                 List.<JCExpression>of(make.Ident(dummyParamName))));
         }
 
         // Construct the parameters
         ListBuffer<JCVariableDecl> params = ListBuffer.lb();
         if (outerTypeSym != null) {
                // add a parameter and a statement to constructor for the outer instance reference
                 params.append( makeParam(diagPos, outerAccessorFieldName, make.Ident(outerTypeSym)) );
                 JCFieldAccess cSelect = make.Select(make.Ident(names._this), outerAccessorFieldName);
                 JCAssign assignStat = make.Assign(cSelect, make.Ident(outerAccessorFieldName));
                 stmts.append(make.Exec(assignStat));            
         }
         params.append( makeParam(diagPos, dummyParamName, syms.booleanType) );
 
         return makeConstructor(diagPos, params.toList(), stmts.toList());
     }
     
    private JCMethodDecl makeConstructor(DiagnosticPosition diagPos, List<JCVariableDecl> params, List<JCStatement> cStats) {
        return make.MethodDef(make.Modifiers(Flags.PUBLIC), 
                names.init, 
                make.TypeIdent(TypeTags.VOID), 
                List.<JCTypeParameter>nil(), 
                params,
                List.<JCExpression>nil(), 
                make.Block(0L, cStats), 
                null);
 
    }
     
     // Add the methods and field for accessing the outer members. Also add a constructor with an extra parameter to handle the instantiation of the classes that access outer members
     private ClassSymbol outerTypeSymbol(JFXClassDeclaration cdecl) {
         if (cdecl.sym != null && toJava.hasOuters.contains(cdecl.sym)) {
             Symbol typeOwner = cdecl.sym.owner;
             while (typeOwner != null && typeOwner.kind != Kinds.TYP) {
                 typeOwner = typeOwner.owner;
             }
             
             if (typeOwner != null) {
                 // Only return an interface class if it's a mixin.
                 return !isMixinClass((ClassSymbol)typeOwner) ? (ClassSymbol)typeOwner.type.tsym :
                         reader.jreader.enterClass(names.fromString(typeOwner.type.toString() + mixinSuffix));
             }
         }
         return null;
    }
     
     // Make the field for accessing the outer members
     private JCTree makeOuterAccessorField(DiagnosticPosition diagPos, JFXClassDeclaration cdecl, ClassSymbol outerTypeSym) {
         // Create the field to store the outer instance reference
         return make.at(diagPos).VarDef(make.at(diagPos).Modifiers(Flags.PUBLIC), outerAccessorFieldName, make.Ident(outerTypeSym), null);
     }
 
     // Make the method for accessing the outer members
     private JCTree makeOuterAccessorMethod(DiagnosticPosition diagPos, JFXClassDeclaration cdecl, ClassSymbol outerTypeSym) {
         make.at(diagPos);
         VarSymbol vs = new VarSymbol(Flags.PUBLIC, outerAccessorFieldName, outerTypeSym.type, cdecl.sym);
         JCIdent retIdent = make.Ident(vs);
         JCStatement retRet = make.Return(retIdent);
         List<JCStatement> mStats = List.of(retRet);
         return make.MethodDef(make.Modifiers(Flags.PUBLIC), outerAccessorName, make.Ident(outerTypeSym), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(),
                 List.<JCExpression>nil(), make.Block(0L, mStats), null);
     }
 
     // methods for accessing the outer members.
     private List<JCTree> makeOuterAccessorInterfaceMembers(JFXClassDeclaration cdecl) {
         ListBuffer<JCTree> members = ListBuffer.lb();
         if (cdecl.sym != null && toJava.hasOuters.contains(cdecl.sym)) {
             Symbol typeOwner = cdecl.sym.owner;
             while (typeOwner != null && typeOwner.kind != Kinds.TYP) {
                 typeOwner = typeOwner.owner;
             }
 
             if (typeOwner != null) {
                 ClassSymbol returnSym = typeMorpher.reader.enterClass(names.fromString(typeOwner.type.toString() + mixinSuffix));
                 JCMethodDecl accessorMethod = make.MethodDef(
                         make.Modifiers(Flags.PUBLIC), 
                         outerAccessorName, 
                         make.Ident(returnSym), 
                         List.<JCTypeParameter>nil(), 
                         List.<JCVariableDecl>nil(),
                         List.<JCExpression>nil(), null, null);
                 members.append(accessorMethod);
                 optStat.recordProxyMethod();
             }
         }
         return members.toList();
     }
 
     private JCStatement makeSuperCall(DiagnosticPosition diagPos, ClassSymbol cSym, Name methodName, boolean fromMixin) {
         if ((cSym.flags() & JavafxFlags.MIXIN) != 0) {
             // call to a mixin super, use local static reference
             Name rcvr = fromMixin? defs.receiverName : names._this;
             return callStatement(diagPos,
                     makeTypeTree(diagPos, cSym.type, false),
                     methodName, make.at(diagPos).Ident(rcvr));
         } else {
             // call to a non-mixin super, use "super"
             return callStatement(diagPos, make.at(diagPos).Ident(names._super), methodName);
         }
     }
 
     /**
      * Generated only when there is a Java super class
      * @param diagPos
      * @return
      */
     private JCMethodDecl makeInitializeMethod(DiagnosticPosition diagPos) {
         make.at(diagPos);
 
         // Add calls to do the Java-style initialization
         //     public void initialize$() {
         //         addTriggers$();
         //         applyDefaults$();
         //         complete$();
         //     }
         return makeInitMethod(diagPos, defs.initializeName, syms.voidType, List.of(
                 callStatement(diagPos, defs.addTriggersName),
                 callStatement(diagPos, defs.applyDefaultsPrefixName),
                 callStatement(diagPos, defs.completeName)));
     }
 
     /**
      * Generated only when there is a Java super class
      * @param diagPos
      * @return
      */
     private JCMethodDecl makeCompleteMethod(DiagnosticPosition diagPos) {
         // Add calls to do the JavaFX-style initialization completeion
         //     public void complete$() {
         //         postInit$();
         //         postInit$();
         //     }
         return makeInitMethod(diagPos, defs.completeName, syms.voidType, List.of(
                 callStatement(diagPos, defs.userInitName),
                 callStatement(diagPos, defs.postInitName)));
     }
 
     private JCMethodDecl makeInitMethod(DiagnosticPosition diagPos, Name name, Type retType, List<JCStatement> stmts) {
         make.at(diagPos);
         JCBlock initializeBlock = make.Block(0L, stmts);
         return make.MethodDef(
                 make.Modifiers(Flags.PUBLIC),
                 name,
                 makeTypeTree(diagPos, retType),
                 List.<JCTypeParameter>nil(),
                 List.<JCVariableDecl>nil(),
                 List.<JCExpression>nil(),
                 initializeBlock,
                 null);
     }
     
     /**
      * Construct the static block for setting defaults
      * */
     private JCBlock makeInitStaticAttributesBlock(JFXClassDeclaration cDecl,
             List<TranslatedVarInfo> translatedAttrInfo,
             JCStatement initMap) {
         // Add the initialization of this class' attributesa
         ListBuffer<JCStatement> stmts = ListBuffer.lb();
         
         // Initialize the var map for anon class.
         if (initMap != null) {
             stmts.append(initMap);
         }
         
         boolean isLibrary = toJava.getAttrEnv().toplevel.isLibrary;
         for (TranslatedVarInfo tai : translatedAttrInfo) {
             assert tai.jfxVar() != null;
             assert tai.jfxVar().getFXTag() == JavafxTag.VAR_DEF;
             assert tai.jfxVar().pos != Position.NOPOS;
             if (tai.isStatic()) {
                 DiagnosticPosition diagPos = tai.pos();
                 // don't put variable initialization in the static initializer if this is a simple-form
                 // script (where variable initialization is done in the run method).
                 if (tai.isDirectOwner() && isLibrary) {
                     if (tai.getDefaultInitStatement() != null) {
                         stmts.append(tai.getDefaultInitStatement());
                     }
                     if (tai.representation().possiblyLocation()) {  //TODO: this goes away
                         // If the static variable is represented with a Location, initialize it
                         Name locName = attributeLocationName(tai.getSymbol());
                         JCStatement initvar = callStatement(diagPos, make.at(diagPos).Ident(locName), defs.locationInitializeName);
                         JCExpression nullCheck = make.at(diagPos).Binary(JCTree.NE, make.at(diagPos).Ident(locName), make.at(diagPos).Literal(TypeTags.BOT, null));
                         stmts.append(make.at(diagPos).If(nullCheck, initvar, null));
                     }
                 }
                 JCStatement stat = tai.onReplaceAsListenerInstanciation();
                 if (stat != null) {
                     stmts.append(stat);
                 }
             }
         }
         return make.at(cDecl.pos()).Block(Flags.STATIC, stmts.toList());
     }
      
     /**
      * Construct the addTriggers method
      * */
     private List<JCTree> makeAddTriggersMethod(DiagnosticPosition diagPos, 
                                                JFXClassDeclaration cDecl,
                                                ClassSymbol superClassSym,
                                                List<ClassSymbol> immediateMixinClasses,
                                                List<TranslatedVarInfo> translatedAttrInfo,
                                                List<TranslatedOverrideClassVarInfo> translatedTriggerInfo) {
         ListBuffer<JCTree> methods = ListBuffer.lb();
         ListBuffer<JCStatement> stmts = ListBuffer.lb();
         boolean isMixinClass = cDecl.isMixinClass();
 
         // Capture the number of statements prior to adding relevant triggers.
         int emptySize = stmts.size();
 
         // Supers will be called when inserted into real classes.
         if (!isMixinClass) {
             // call the super addTriggers
             if (superClassSym != null) {
                 stmts.append(makeSuperCall(diagPos, superClassSym, defs.addTriggersName, isMixinClass));
             }
             
             // Super still classified as empty.
             emptySize = stmts.size();
 
             // JFXC-2822 - Triggers need to work from mixins.
             for (ClassSymbol cSym : immediateMixinClasses) {
                 stmts.append(makeSuperCall(diagPos, cSym, defs.addTriggersName, isMixinClass));
             }
         }
         
         // add change listeners for triggers on instance var definitions
         for (TranslatedVarInfo info : translatedAttrInfo) {
             if (!info.isStatic()) {
                 JCStatement stat = info.onReplaceAsListenerInstanciation();
                 if (stat != null) {
                     stmts.append(stat);
                 }
             }
         }
 
         // add change listeners for on replace on overridden vars
         for (TranslatedOverrideClassVarInfo info : translatedTriggerInfo) {
             if (!info.isStatic()) {
                 JCStatement stat = info.onReplaceAsListenerInstanciation();
                 if (stat != null) {
                     stmts.append(stat);
                 }
             }
         }
         
         // Only generate method if necessary.
         if (stmts.size() != emptySize || isMixinClass || superClassSym == null) {
             methods.append(make.at(diagPos).MethodDef(
                     make.Modifiers(isMixinClass? Flags.PUBLIC | Flags.STATIC : Flags.PUBLIC),
                     defs.addTriggersName,
                     makeTypeTree( null,syms.voidType),
                     List.<JCTypeParameter>nil(),
                     isMixinClass? List.<JCVariableDecl>of( makeReceiverParam(cDecl) ) : List.<JCVariableDecl>nil(),
                     List.<JCExpression>nil(),
                     make.Block(0L, stmts.toList()),
                     null));
         }
         
         return methods.toList();
     }
 
     /**
      * Make a method body which redirects to the actual implementation in a static method of the defining class.
      */
     private JCBlock makeDispatchBody(JFXClassDeclaration cDecl, MethodSymbol mth, boolean isBound, boolean isStatic) {
         ListBuffer<JCExpression> args = ListBuffer.lb();
         if (!isStatic) {
             // Add the this argument, so the static implementation method is invoked
             args.append(make.TypeCast(make.Ident(interfaceName(cDecl)), make.Ident(names._this)));
         }
         for (VarSymbol var : mth.params) {
             args.append(make.Ident(var.name));
         }
         JCExpression receiver = mth.owner == cDecl.sym ? null : makeTypeTree(cDecl.pos(), mth.owner.type, false);
         JCExpression expr = callExpression(cDecl.pos(), receiver, functionName(mth, !isStatic, isBound), args);
         JCStatement statement = (mth.getReturnType() == syms.voidType) ? make.Exec(expr) : make.Return(expr);
         return make.at(cDecl.pos()).Block(0L, List.<JCStatement>of(statement));
     }
 
     protected String getSyntheticPrefix() {
         return "ifx$";
     }
     
     //-----------------------------------------------------------------------------------------------------------------------------
     //
     // This class is used to simplify the construction of java code in the
     // initialization builder.
     //
     class JavaCodeMaker {
         // The current class analysis/
         private final JavafxAnalyzeClass analysis;
         // The current position used to construct the JCTree.
         private DiagnosticPosition currentPos;
         
         JavaCodeMaker(JavafxAnalyzeClass analysis) {
             this.analysis = analysis;
             currentPos = analysis.getCurrentClassPos();
         }
         
         // 
         // Methods for managing the current diagnostic position.
         //
         private void setCurrentPos(DiagnosticPosition diagPos) { currentPos = diagPos; }
         private void setCurrentPos(VarInfo ai) { setCurrentPos(ai.pos()); }
         private void resetCurrentPos() { currentPos = analysis.getCurrentClassPos(); }
         
         //
         // This method simplifies the declaration of new java code nodes.
         //
         private TreeMaker m() { return make.at(currentPos); }
         
         //
         // Methods to generate simple constants.
         //
         private JCExpression makeInt(int value)         { return m().Literal(TypeTags.INT, value); }
         private JCExpression makeBoolean(boolean value) { return m().Literal(TypeTags.BOOLEAN, value ? 1 : 0); }
         private JCExpression makeNull()                 { return m().Literal(TypeTags.BOT, null); }
         
         //
         // This method simplifies Ident declaration.
         //
         private JCExpression Id(Name name) { return m().Ident(name); }
 
         //
         // This method makes a type tree using the current diagnosic position.
         //
         private JCExpression makeType(Type t)                   { return makeTypeTree(currentPos, t); }
         private JCExpression makeType(Type t, boolean makeIntf) { return makeTypeTree(currentPos, t, makeIntf); }
 
         //
         // This method generates a simple java integer field then adds to the buffer.
         //
         private JCVariableDecl addSimpleIntVariable(long modifiers, Name name, int value) {
             // Construct the variable itself.
             return makeVariable(modifiers, syms.intType, name, makeInt(value));
         }
         
         //
         // This method generates a java field for a varInfo.
         //
         private JCVariableDecl makeVariableField(VarInfo varInfo, JCModifiers mods, Type varType, Name name, JCExpression varInit) {
             setCurrentPos(varInfo);
             // Define the type.
             JCExpression type = makeType(varType);
             // Construct the variable itself.
             JCVariableDecl var = m().VarDef(mods, name, type, varInit);
              // Update the statistics.
             optStat.recordClassVar(varInfo.getSymbol(), varInfo.representation());
             optStat.recordConcreteField();
 
             return var;
         }
         
         //
         // This method generates a simple variable.
         //
         private JCVariableDecl makeVariable(long modifiers, Type varType, String name, JCExpression varInit) {
             return makeVariable(modifiers, varType, names.fromString(name), varInit);
         }
         private JCVariableDecl makeVariable(long modifiers, Type varType, Name name, JCExpression varInit) {
             // JCVariableDecl the modifiers.
             JCModifiers mods = m().Modifiers(modifiers);
             // Define the type.
             JCExpression type = makeType(varType);
             // Construct the variable itself.
             JCVariableDecl var = m().VarDef(mods, name, type, varInit);
             // Update the statistics.
             optStat.recordConcreteField();
 
             return var;
         }
         
         //
         // Build the location and value field for each attribute.
         //
         public List<JCTree> makeAttributeFields(List<? extends VarInfo> attrInfos) {
             // Buffer for new vars.
             ListBuffer<JCTree> vars = ListBuffer.lb();
  
             for (VarInfo ai : attrInfos) {
                 // Only process attributes declared in this class (includes mixins.)
                 if (ai.needsDeclaration()) {
                     // Set the current diagnostic position.
                     setCurrentPos(ai);
                     // Grab the variable symbol.
                     VarSymbol varSym = ai.getSymbol();
                     // The fields need to be available to reflection.
                     // TODO deal with defs.
                     JCModifiers mods = m().Modifiers(Flags.PUBLIC | (ai.getFlags() & Flags.STATIC));
                     
                     // Apply annotations, if current class then add source annotations.
                     if (varSym.owner == analysis.getCurrentClassSymbol()) {
                         List<JCAnnotation> annotations = List.<JCAnnotation>of(make.Annotation(
                                 makeIdentifier(currentPos, JavafxSymtab.sourceNameAnnotationClassNameString),
                                 List.<JCExpression>of(m().Literal(varSym.name.toString()))));
                         mods = addAccessAnnotationModifiers(currentPos, varSym.flags(), mods, annotations);
                     } else {
                         mods = addInheritedAnnotationModifiers(currentPos, varSym.flags(), mods);
                     }
 
                     // Construct the value field unless it will always be a Location
                     if (ai.representation() != AlwaysLocation) {
                         vars.append(makeVariableField(ai, mods, ai.getRealType(), attributeValueName(varSym),
                                 makeDefaultValue(currentPos, ai.getVMI())));
                     }
 
                     // If a Location might be needed, build the field
                     if (ai.representation() != NeverLocation) {
                         // TODO - switch over to using NULL.
                         JCExpression initialValue = ai.representation()==AlwaysLocation ? makeLocationAttributeVariable(ai.getVMI(), currentPos) : null;
                         // Construct the location field.
                         vars.append(makeVariableField(ai, mods, ai.getVariableType(), attributeLocationName(varSym), initialValue));
                     }
                 }
             }
             
             return vars.toList();
         }
         
         //
         // This method constructs modifiers for getters/setters and proxies.
         //
         private JCModifiers proxyModifiers(VarInfo ai, boolean isAbstract) {
             // Copy flags from VarInfo.
             long flags = ai.getFlags();
 
             // Set up basic flags.
             JCModifiers mods = make.Modifiers((flags & Flags.STATIC) | (isAbstract ? (Flags.PUBLIC | Flags.ABSTRACT) : Flags.PUBLIC));
            
             // If var is in current class.
             if (ai.getSymbol().owner == analysis.getCurrentClassSymbol()) {
                 // Use local access modifiers.
                 mods = addAccessAnnotationModifiers(ai.pos(), flags, mods);
             } else {
                 // Use inherited modifiers.
                 mods = addInheritedAnnotationModifiers(ai.pos(), flags, mods);
             }
             
             return mods;
         }
 
         // This method constructs the getter method for the specified attribute.
         //
         //     type get$var() {
         //         return loc$var != null ? loc$var.getAsType() : $var;
         //     }
         //     
         private JCTree makeGetterAccessorMethod(VarInfo varInfo, boolean needsBody) {
             setCurrentPos(varInfo);
             // Symbol used on the method.
             VarSymbol varSym = varInfo.getSymbol();
             // Real type for var.
             Type type = varInfo.getRealType();
             // Assume no body.
             ListBuffer<JCStatement> stmts = null;
             
             if (needsBody) {
                 // Prepare to accumulate statements.
                 stmts = ListBuffer.lb();
                 
                 // Symbol used when accessing the variable.
                 VarSymbol proxyVarSym = varInfo.proxyVarSym();
                 int typeKind = varInfo.getVMI().getTypeKind();
 
                 switch (varInfo.representation()) {
                     case SlackerLocation: {
                         // Construct and add: return loc$var != null ? loc$var.getAsType() : $var;
 
                         // Get the location accessor method name.
                         Name getMethodName = defs.locationGetMethodName[typeKind];
 
                         // loc$var
                         JCExpression locationExp = Id(attributeLocationName(proxyVarSym));
                         // loc$var.getAsType
                         JCFieldAccess getSelect = m().Select(locationExp, getMethodName);
                         // loc$var.getAsType()
                         JCExpression getCall = m().Apply(null, getSelect, List.<JCExpression>nil());
                         // $var
                         JCExpression valueExp = Id(attributeValueName(proxyVarSym));
                         // loc$var != null
                         JCExpression condition = m().Binary(JCTree.NE, locationExp, makeNull());
                         // loc$var != null ? loc$var.getAsType() : $var
                         stmts.append(m().If(condition, m().Return(getCall), m().Return(valueExp)));
                         break;
                     }
                     case AlwaysLocation: {
                         // Get the location accessor method name.
                         Name getMethodName = defs.locationGetMethodName[typeKind];
 
                         // loc$var
                         JCExpression locationExp = Id(attributeLocationName(proxyVarSym));
                         // loc$var.getAsType
                         JCFieldAccess getSelect = m().Select(locationExp, getMethodName);
                         // loc$var.getAsType()
                         JCExpression getCall = m().Apply(null, getSelect, List.<JCExpression>nil());
 
                         stmts.append(m().Return(getCall));
                         break;
                     }
                     case NeverLocation: {
                         // $var
                         JCExpression valueExp = Id(attributeValueName(proxyVarSym));
                         // Construct and add: return $var;
                         stmts.append(m().Return(valueExp));
                         break;
                     }
                 }
             }
             
             // Construct method.
             JCMethodDecl method = makeMethod(proxyModifiers(varInfo, !needsBody), 
                                              type,
                                              attributeGetterName(varSym),
                                              List.<JCVariableDecl>nil(),
                                              stmts);
             optStat.recordProxyMethod();
             
             return method;
         }
         
         //
         // This method returns the actual set statement used in the setter method.
         //
         private JCStatement makeSetterStatement(VarInfo varInfo) {
              // Symbol used when accessing the variable.
             VarSymbol proxyVarSym = varInfo.proxyVarSym();
 
             // $var
             JCExpression valueExp = Id(attributeValueName(proxyVarSym));
             // Arg value, if not from setter use the default value for the type.
             JCExpression argExp = Id(defs.attributeSetMethodParamName);
             // $var = value
             JCExpression assignExp = m().Assign(valueExp, argExp);
 
             int typeKind = varInfo.getVMI().getTypeKind();
 
             switch (varInfo.representation()) {
                 case SlackerLocation: {
                     // Construct and add: if (loc$var != null) return loc$var.setAsType(value) else return $var = value
 
                     // Get the location accessor method name.
                     Name setMethodName = defs.locationSetMethodName[typeKind];
 
                     // loc$var.setAsType
                     JCFieldAccess setSelect = m().Select(Id(attributeLocationName(proxyVarSym)), setMethodName);
                     // loc$var.setAsType(value)
                     JCExpression setCall = m().Apply(null, setSelect, List.<JCExpression>of(argExp));
                     // loc$var != null
                     JCExpression condition = m().Binary(JCTree.NE, Id(attributeLocationName(proxyVarSym)), makeNull());
 
                     // if (loc$var != null) return loc$var.setAsType(value) else return $var = value
                     return m().If(condition, m().Return(setCall), m().Return(assignExp));
                 }
                 case AlwaysLocation: {
                     // Construct and add: loc$var.setAsType(value)
 
                     // Get the location accessor method name.
                     Name setMethodName = defs.locationSetMethodName[typeKind];
 
                     // loc$var
                     JCExpression locationExp = Id(attributeLocationName(proxyVarSym));
                     // loc$var.setAsType
                     JCFieldAccess setSelect = m().Select(locationExp, setMethodName);
                     // loc$var.setAsType(value)
                     JCExpression setCall = m().Apply(null, setSelect, List.<JCExpression>of(argExp));
 
                     // return loc$var.setAsType(value);
                     return m().Return(setCall);
                 }
                 case NeverLocation: {
                     // return $var = value;
                     return m().Return(assignExp);
                 }
             }
 
             return null;
         }
 
         //
         // This method constructs the setter method for the specified attribute.
         //
         //     type set$var(type value) {
         //         return loc$var != null ? loc$var.setAsType(value) : $var = value;
         //     }
         //     
         private JCTree makeSetterAccessorMethod(VarInfo varInfo, boolean needsBody) {
             setCurrentPos(varInfo);
             // Symbol used on the method.
             VarSymbol varSym = varInfo.getSymbol();
             // Real type for var.
             Type type = varInfo.getRealType();
             // Assume no body.
             ListBuffer<JCStatement> stmts = null;
  
             if (needsBody) {
                 // Prepare to accumulate statements.
                 stmts = ListBuffer.lb();
                 
                 // Script vars don't need flags.
                 if (!varInfo.isStatic()) {
                     // Get the var enumeration.
                     int enumeration = varInfo.getEnumeration();
                     // Which VFLGS$ word.
                     int word = enumeration >> 5;
                     // Which VFLGS$ bit.
                     int bit = 1 << (enumeration & 31);
             
                     // VFLGS$word
                     JCExpression bitsIdent = Id(attributeBitsName(word));
                     // VFLGS$word |= bit;
                     JCStatement bitsStmt = m().Exec(m().Assignop(JCTree.BITOR_ASG, bitsIdent, makeInt(bit)));
                     stmts.append(bitsStmt);
                 }
                 
                 // Add set statement.
                 stmts.append(makeSetterStatement(varInfo));
             }
 
             // Set up value arg.
             JCVariableDecl arg = m().VarDef(m().Modifiers(Flags.FINAL | Flags.PARAMETER),
                                                           defs.attributeSetMethodParamName,
                                                           makeType(type),
                                                           null);
             // Construct method.
             JCMethodDecl method = makeMethod(proxyModifiers(varInfo, !needsBody),
                                              type,
                                              attributeSetterName(varSym),
                                              List.<JCVariableDecl>of(arg),
                                              stmts);
             optStat.recordProxyMethod();
             
             return method;
         }
         
         //
         // This method returns a .setDefault() call (if appropriate)
         //
         private JCStatement makeSetDefaultStatement(VarInfo varInfo) {
             // Symbol used when accessing the variable.
             VarSymbol proxyVarSym = varInfo.proxyVarSym();
 
             // loc$var.setDefault()
             JCStatement setDefaultCall = callStatement(currentPos, Id(attributeLocationName(proxyVarSym)), defs.setDefaultMethodName);
 
             switch (varInfo.representation()) {
                 case SlackerLocation: {
                     // loc$var != null
                     JCExpression condition = m().Binary(JCTree.NE, Id(attributeLocationName(proxyVarSym)), makeNull());
 
                     // if (loc$var != null) loc$var.setDefault()
                     return m().If(condition, setDefaultCall, null);
                 }
                 case AlwaysLocation: {
                     // loc$var.setDefault()
                     return setDefaultCall;
                 }
                 case NeverLocation: {
                     // Not a location
                     return null;
                 }
             }
             return null;
         }
 
         //
         // This method constructs the get location method for the specified attribute.
         //
         //     Location loc$var() {
         //         return loc$var;
         //     }
         //
         // Or:
         //     Location loc$var() {
         //         if (loc$var == null) {
         //             loc$var = XXXVariable.makeWithDefault($var));
         //         }
         //         return loc$var;
         //     }
         //     
         private JCTree makeGetLocationAccessorMethod(VarInfo varInfo, boolean needsBody) {
             setCurrentPos(varInfo);
             // Symbol used on the method.
             VarSymbol varSym = varInfo.getSymbol();
             // Assume no body.
             ListBuffer<JCStatement> stmts = null;
             
             if (needsBody) {
                 // Prepare to accumulate statements.
                 stmts = ListBuffer.lb();
                 // $var
                 Name valueName = attributeValueName(varSym);
                 // loc$var
                 Name locationName = attributeLocationName(varSym);
 
                 switch (varInfo.representation()) {
                     case SlackerLocation: {
                         // XXXVariable.makeWithDefault($var)
                         JCExpression initExpr = makeLocationWithDefault(varInfo.getVMI(), varInfo.pos(), Id(valueName));
                         // loc$var = XXXVariable.makeWithDefault($var)
                         JCStatement assignExpr = m().Exec(m().Assign(Id(locationName), initExpr));
                         // loc$var == null
                         JCExpression nullCheck = m().Binary(JCTree.EQ, Id(locationName), makeNull());
                         //
                         stmts.append(m().If(nullCheck, assignExpr, null));
                         // Construct and add: return loc$var)
                         stmts.append(m().Return(Id(locationName)));
                         break;
                     }
                     case AlwaysLocation: {
                         // Construct and add: return loc$var)
                         stmts.append(m().Return(Id(locationName)));
                         break;
                     }
                     case NeverLocation: {
                         // new ConstantLocation<T>($var)
                         JCExpression locationExpr = makeUnboundLocation(currentPos, varInfo.getVMI(), Id(valueName));
                         // Construct and add: return new ConstantLocation<T>($var);
                         stmts.append(m().Return(locationExpr));
                         break;
                     }
                 }
             }
 
             // Construct method.
             JCMethodDecl method = makeMethod(proxyModifiers(varInfo, !needsBody), 
                                              varInfo.getVariableType(),
                                              attributeGetLocationName(varSym),
                                              List.<JCVariableDecl>nil(),
                                              stmts);
             optStat.recordProxyMethod();
             
             return method;
         }
         
         //
         // This method constructs the getter/setter/location accessor methods for each attribute.
         //     
         public List<JCTree> makeAttributeAccessorMethods(List<VarInfo> attrInfos) {
             ListBuffer<JCTree> accessors = ListBuffer.lb();
             
             for (VarInfo ai : attrInfos) {
                 // Only create accessors for declared and proxied vars.
                 if (ai.needsDeclaration()) {
                     setCurrentPos(ai.pos());
                     
                     if (ai.useAccessors()) {
                         accessors.append(makeGetterAccessorMethod(ai, true));
                         accessors.append(makeSetterAccessorMethod(ai, true));
                     }                
                     accessors.append(makeGetLocationAccessorMethod(ai, true));
                 }
             }
             
             return accessors.toList();
         }
           
         //
         // This method constructs the abstract interfaces for the getters and setters in
         // a mixin class.
         //
         public List<JCTree> makeMemberVariableAccessorInterfaceMethods() {
             // Buffer for new decls.
             ListBuffer<JCTree> accessors = ListBuffer.lb();
             // TranslatedVarInfo for the current class.
             List<TranslatedVarInfo> translatedAttrInfo = analysis.getTranslatedAttrInfo();
             
             // Only for vars within the class.
             for (VarInfo ai : translatedAttrInfo) {
                 if (!ai.isStatic()) {
                     setCurrentPos(ai.pos());
                     
                     if (ai.useAccessors()) {
                         accessors.append(makeGetterAccessorMethod(ai, false));
                         accessors.append(makeSetterAccessorMethod(ai, false));
                     }                    
                     accessors.append(makeGetLocationAccessorMethod(ai, false));
                 }
             }
             return accessors.toList();
         }
 
         //
         // This method generates an enumeration for each of the instance attributes
         // of the class.
         //
         public List<JCTree> makeAttributeNumbers(LiteralInitVarMap varMap) {
             // Buffer for new members.
             ListBuffer<JCTree> members = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             // Get the list of instance attributes.
             List<VarInfo> attrInfos = analysis.instanceAttributeInfos();
             
             // See if there is a javafx super class.
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
             
             // Construct a base offset cache (VBASE$)
             members.append(addSimpleIntVariable(Flags.STATIC | Flags.PUBLIC, defs.varBaseName, -1));
             
             // Construct a base offset accessor method (VBASE$)
             members.append(makeVBASE$());
             
             // Construct a static count accessor method (VCNT$)
             members.append(makeVCNT$());
             
             // Construct a virtual count accessor method (count$)
             members.append(makecount$());
             
             // Accumulate variable numbering.
             for (VarInfo ai : attrInfos) {
                 // Only variables actually declared.
                 if (ai.needsDeclaration()) {
                     // Set diagnostic position for attribute.
                     setCurrentPos(ai.pos());
                     
                     // Construct enumeration var.
                     Name name = attributeOffsetName(ai.getSymbol());
                     // Construct and add: public static int VOFF$name() {}
                     members.append(makeVOFF$(name, ai.getEnumeration()));
                 }
                 
                 // Add to var map if an anon class.
                 if (varMap != null) varMap.addVar(ai.getSymbol());
             }
     
             // private int VFLGS$0 = 0; private int VFLGS$1 = 0; ...
             {
                 // Number of variables in current class.
                 int count = analysis.getVarCount();
             
                 // Number of words needed to manage initialization bitmaps.
                 int words = (count + 31) >> 5;
                 
                 // Allocate bit map words.
                 for (int word = 0; word < words; word++) {
                     // Construct and add: private int VFLGS$0 = 0;
                     members.append(addSimpleIntVariable(0, attributeBitsName(word), 0));
                 }
             }
              
             return members.toList();
         }
     
         //
         // The method constructs the VBASE$ method for the current class.
         //
         public JCTree makeVBASE$() {
             // Prepare to accumulate statements.
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
             // See if there is a javafx super class.
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
             
             // Get the count from the super class.
             JCExpression superBaseExpr;
             if (superClassSym != null) {
                 // super.VCNT$
                 JCExpression selectExpr = m().Select(makeType(superClassSym.type, false), defs.varCountName);
                 // super.VCNT$()
                 superBaseExpr = m().Apply(null, selectExpr, List.<JCExpression>nil());
             } else {
                 // 0
                 superBaseExpr = makeInt(0);
             }
             
             // VBASE$ = super.VCNT$()
             JCExpression assignExpr = m().Assign(Id(defs.varBaseName), superBaseExpr);
             // (VBASE$ == -1)
             JCExpression condition = m().Binary(JCTree.EQ, Id(defs.varBaseName), makeInt(-1));
             // (VBASE$ == -1) ? (VBASE$ = SUPER.VCNT$()) : VBASE$
             JCExpression resultExpr = m().Conditional(condition, assignExpr, Id(defs.varBaseName));
             // Construct and add: return (VBASE$ == -1) ? (VBASE$ = SUPER.VCNT$()) : VBASE$;
             stmts.append(m().Return(resultExpr));
             
             // Construct method.
             JCMethodDecl method = makeMethod(Flags.PUBLIC | Flags.STATIC,
                                              syms.intType,
                                              defs.varBaseName,
                                              List.<JCVariableDecl>nil(),
                                              stmts);
             return method;
         }
 
         //
         // The method constructs the VCNT$ method for the current class.
         //
         public JCTree makeVCNT$() {
             // Prepare to accumulate statements.
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
             // VBASE$()
             JCExpression baseExpr = m().Apply(null, Id(defs.varBaseName), List.<JCExpression>nil());
             // VBASE$() + n
             JCExpression countExpr = m().Binary(JCTree.PLUS, baseExpr, makeInt(analysis.getVarCount()));
             // Construct and add: return VBASE$() + n;
             stmts.append(m().Return(countExpr));
             
             // Construct method.
             JCMethodDecl method = makeMethod(Flags.PUBLIC | Flags.STATIC,
                                              syms.intType,
                                              defs.varCountName,
                                              List.<JCVariableDecl>nil(),
                                              stmts);
             return method;
         }
 
         //
         // The method constructs the count$ method for the current class.
         //
         public JCTree makecount$() {
             // Prepare to accumulate statements.
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
             // VCNT$()
             JCExpression countExpr = m().Apply(null, Id(defs.varCountName), List.<JCExpression>nil());
             // Construct and add: return VCNT$();
             stmts.append(m().Return(countExpr));
             
             // Construct method.
             JCMethodDecl method = makeMethod(Flags.PUBLIC,
                                              syms.intType,
                                              defs.attributeCountMethodName,
                                              List.<JCVariableDecl>nil(),
                                              stmts);
             return method;
         }
 
         //
         // The method constructs a VOFF$ method for the specified var.
         //
         public JCTree makeVOFF$(Name name, int offset) {
             // Prepare to accumulate statements.
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
             // VBASE$()
             JCExpression baseExpr = m().Apply(null, Id(defs.varBaseName), List.<JCExpression>nil());
             // VBASE$() + n
             JCExpression offsetExpr = m().Binary(JCTree.PLUS, baseExpr, makeInt(offset));
             // Construct and add: return VBASE$() + n;
             stmts.append(m().Return(offsetExpr));
             
             // Construct method.
             JCMethodDecl method = makeMethod(Flags.PUBLIC | Flags.STATIC,
                                              syms.intType,
                                              name,
                                              List.<JCVariableDecl>nil(),
                                              stmts);
             return method;
         }
 
         //
         // This methods generates the isInitialized$ method for this class.
         //
         public List<JCTree> makeIsInitialized() {
             // Buffer for new methods.
             ListBuffer<JCTree> methods = ListBuffer.lb();
             
             // Number of variables in current class.
             int count = analysis.getVarCount();
             
             // Prepare to accumulate statements.
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             // Grab the super class.
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
             
             // Only bother if there are vars.
             if (0 < count) {
                 // varNum - VBASE$;
                 JCExpression localVarNumExp = m().Binary(JCTree.MINUS, Id(varNumName), Id(defs.varBaseName));
                 // Construct and add: final int varlocalNum = varNum - VBASE$;
                 stmts.append(makeVariable(Flags.FINAL, syms.intType, varLocalNumName, localVarNumExp));
  
                 // Check to see if we need to pass to the super class.
                 if (superClassSym != null) {
                     // super
                     JCExpression selector = Id(names._super);
                     // (varNum)
                     List<JCExpression> args = List.<JCExpression>of(Id(varNumName));
                     // super.isInitialized$(varNum);
                     JCExpression callExp = callExpression(currentPos, selector, defs.isInitializedPrefixName, args);
                     // return super.isInitialized$(varNum)
                     JCStatement returnStmt = m().Return(callExp);
                     // varlocalNum < 0
                     JCExpression condition = m().Binary(JCTree.LT, Id(varLocalNumName), makeInt(0));
                     // Construct and add: if (varlocalNum < 0) return super.isInitialized$(varNum);
                     stmts.append(m().If(condition, returnStmt, null));
                 }
             
                 // varLocalNum & 31
                 JCExpression varBitExp = m().Binary(JCTree.BITAND, Id(varLocalNumName), makeInt(31));
                 // Construct and add: int varBit = varLocalNum & 31;
                 stmts.append(makeVariable(Flags.FINAL, syms.intType, varBitName, varBitExp));
                 
                 // Number of words needed to manage initialization bitmaps.
                 int words = (count + 31) >> 5;
                 
                 // Get the correct initialize bits word.
                 JCExpression varWordExp = Id(attributeBitsName(words - 1));
     
                 for (int i = words - 1; 0 < i; i--) {
                     // varlocalNum < (i*32)
                     JCExpression condition = m().Binary(JCTree.LT, Id(varLocalNumName), makeInt(i * 32));
                     // varlocalNum < (i*32) ? VFLGS$(i-1) : VFLGS$(i)
                     varWordExp = m().Conditional(condition, Id(attributeBitsName(i-1)), varWordExp);
                 }
                 
                 // Construct and add: int varWord = ...varlocalNum < (i*32) ? VFLGS$(i) : VFLGS$(i+1)...
                 stmts.append(makeVariable(Flags.FINAL, syms.intType, varWordName, varWordExp));
                 
                 // 1 << varBit
                 JCExpression bitShiftExpr = m().Binary(JCTree.SL, makeInt(1), Id(varBitName));
                 // (varWord & (1 << varBit))
                 JCExpression maskExpr = m().Binary(JCTree.BITAND, Id(varWordName), bitShiftExpr);
                  // (varWord & (1 << varBit)) != 0
                 JCExpression resultExpr = m().Binary(JCTree.NE, maskExpr, makeInt(0));
                 // Construct and add: return (varWord & (1 << varBit)) != 0;
                 stmts.append(m().Return(resultExpr));
             } else if (superClassSym == null) {
                 // Construct and add: return true;
                 stmts.append(m().Return(makeBoolean(true)));
             }
             
             if (stmts.nonEmpty()) {
                 // varNum ARG
                 JCVariableDecl arg = m().VarDef(m().Modifiers(Flags.FINAL | Flags.PARAMETER),
                                                               varNumName,
                                                               makeType(syms.intType),
                                                               null);
                 // Construct method.
                 JCMethodDecl method = makeMethod(Flags.PUBLIC,
                                                  syms.booleanType,
                                                  defs.isInitializedPrefixName,
                                                  List.<JCVariableDecl>of(arg),
                                                  stmts);
                 // Add to the methods list.
                 methods.append(method);
             }
             
             return methods.toList();
         }
         
         //
         // This method constructs an applyDefaults method for each attribute.
         //
         private List<JCTree> makeApplyDefaultsMethods(List<? extends VarInfo> attrInfos) {
             // Mixin vars always have applyDefaults.
             boolean isMixinClass = analysis.isMixinClass();
             // Prepare to accumulate methods.
             ListBuffer<JCTree> methods = ListBuffer.lb();
 
             for (VarInfo ai : attrInfos) {
                 // True if the the user specified a default.
                 boolean hasDefault = ai.getDefaultInitStatement() != null;
 
                 // If the var is defined in the current class or it has a (override) default.
                 if (ai.needsCloning() || hasDefault) {
                     // Set diagnostic position for attribute.
                     setCurrentPos(ai.pos());
                     // Fetch the attribute symbol.
                     VarSymbol varSym = ai.getSymbol();
                     // Construct the name of the method.
                     Name methodName = attributeApplyDefaultsName(varSym);
                     // Prepare to accumulate statements.
                     ListBuffer<JCStatement> stmts = ListBuffer.lb();
 
                     // Only need receiver arg if a mixin.
                     List<JCVariableDecl> args;
                     if (isMixinClass) {
                         // Don't override someone elses default.
                         if (analysis.getCurrentClassSymbol() != varSym.owner && !hasDefault) continue;
                         // Use a receiver arg.
                         args = List.<JCVariableDecl>of(makeReceiverParam(analysis.getCurrentClassDecl()));
                     } else {
                         // No arguments.
                         args = List.<JCVariableDecl>nil();
                         // Compensate with a local receiver.
                         stmts.append(makeReceiverLocal(analysis.getCurrentClassDecl()));
                     }
 
                     if (hasDefault) {
                          // a default exists, either on the direct attribute or on an override
                         stmts.append(ai.getDefaultInitStatement());
                     } else if (!isMixinClass) {
                         if (ai.isMixinVar()) {
                             // Include defaults for mixins into real classes.
                             stmts.append(makeSuperCall((ClassSymbol)varSym.owner, methodName, List.<JCExpression>of(Id(names._this))));
                        } else if (ai instanceof TranslatedVarInfo) {
                             //TODO: see SequenceVariable.setDefault() and JFXC-885
                             // setDefault() isn't really done for sequences
                             if (!ai.isSequence()) {
                                 // Make .setDefault() if Location (without clearing initialize bit) to fire trigger.
                                 JCStatement setter = makeSetDefaultStatement(ai);
                                 if (setter != null) {
                                     stmts.append(setter);
                                 }
                             }
                         }
                     }
 
                     // Construct method.
                     JCMethodDecl method = makeMethod(Flags.PUBLIC | (isMixinClass ? Flags.STATIC : 0L),
                                                      syms.voidType,
                                                      methodName,
                                                      args,
                                                      stmts);
                     methods.append(method);
                 }
             }
             return methods.toList();
         }
 
         public List<JCTree> makeGeneralApplyDefaults() {
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             stmts.append(callStatement(currentPos, makeType(syms.javafx_FXBaseType), names.fromString(defs.applyDefaultsPrefixName + "base"), m().Ident(names._this)));
             JCMethodDecl method = makeMethod(Flags.PUBLIC,
                     syms.voidType,
                     defs.applyDefaultsPrefixName,
                     List.<JCVariableDecl>nil(),
                     stmts);
             return List.<JCTree>of(method);
         }
 
         //
         // This methods generates the applDefaults$ methods for this class.  The first method
         // Is a blanket apply all defaults.  The second methods is the default apply default
         // of a specific (numbered) var.
         //
         public List<JCTree> makeBlanketApplyDefaults() {
             // Buffer for new methods.
             ListBuffer<JCTree> methods = ListBuffer.lb();
             
             // Number of variables in current class.
             int count = analysis.getVarCount();
 
             // Grab the super class.                                         
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
 
             // Only bother if there are some vars or no super class.
             if (0 < count || superClassSym == null) {
                 // Prepare to accumulate statements.
                 ListBuffer<JCStatement> stmts = ListBuffer.lb();
                 // Reset diagnostic position to current class.
                 resetCurrentPos();
             
                 // If present we need to call super.applDefaults$
                 if (superClassSym != null) {
                     stmts.append(makeSuperCall(superClassSym, defs.applyDefaultsPrefixName));
                 }
                 
                 // Gather the instance attributes.
                 List<VarInfo> attrInfos = analysis.instanceAttributeInfos();
                 for (VarInfo ai : attrInfos) {
                     // Only attributes with default expressions.
                     if (ai.needsDeclaration()) {
                         // Name of applDefaults$ methods.
                         Name methodName = attributeApplyDefaultsName(ai.getSymbol());
                         // applyDefaults$var()
                         JCStatement applyDefaultsCall = callStatement(currentPos, null, methodName, List.<JCExpression>nil());
                     
                         if (!ai.isDef()) {
                             // Find the vars enumeration.
                             int enumeration = ai.getEnumeration();
 
                             // Don't generate for overrides
                             if (enumeration >= 0) {
                                 // Which VFLGS$(word) to use.
                                 int word = enumeration >> 5;
                                 // Which bit to use.
                                 int bit = enumeration & 31;
 
                                 // (varWord & (1 << varBit))
                                 JCExpression maskExpr = m().Binary(JCTree.BITAND, Id(attributeBitsName(word)), makeInt(1 << bit));
                                 // (varWord & (1 << varBit)) == 0
                                 JCExpression condition = m().Binary(JCTree.EQ, maskExpr, makeInt(0));
                                 // Construct and add: if ((VFLGS$(word) & (1 << bit)) == 0) { applyDefaults$var(); }
                                 stmts.append(m().If(condition, applyDefaultsCall, null));
                             }
                         } else {
                             /// Construct and add: applyDefaults$var(this);
                             stmts.append(applyDefaultsCall);
                         }
                     }
                 }
     
                 // Reset diagnostic position to current class.
                 resetCurrentPos();
                 
                 // Construct method.
                 JCMethodDecl method = makeMethod(Flags.PUBLIC,
                                                  syms.voidType,
                                                  defs.applyDefaultsPrefixName,
                                                  List.<JCVariableDecl>nil(),
                                                  stmts);
                 // Add to the methods list.
                 methods.append(method);
             }
             
             return methods.toList();
         }
         public List<JCTree> makeSpecificApplyDefaults() {
             // Buffer for new methods.
             ListBuffer<JCTree> methods = ListBuffer.lb();
             
             // Number of variables in current class.
             int count = analysis.getVarCount();
 
             // Grab the super class.                                         
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
 
             // Only bother if there are some vars or no super class.
             if (0 < count || superClassSym == null) {
                 // Prepare to accumulate statements.
                 ListBuffer<JCStatement> stmts = ListBuffer.lb();
                 // Reset diagnostic position to current class.
                 resetCurrentPos();
 
                 // Prepare to accumulate cases.
                 ListBuffer<JCCase> cases = ListBuffer.lb();
                 
                 // Gather the instance attributes.
                 List<VarInfo> attrInfos = analysis.instanceAttributeInfos();
                 for (VarInfo ai : attrInfos) {
                     // Only attributes with default expressions.
                     if (ai.needsDeclaration()) {
                         // Name of applDefaults$ methods.
                         Name methodName = attributeApplyDefaultsName(ai.getSymbol());
                         // applyDefaults$var(this)
                         JCStatement applyDefaultsCall = callStatement(currentPos, null, methodName, List.<JCExpression>nil());
                         // return true;
                         JCStatement returnExpr = m().Return(makeBoolean(true));
                         // i: applyDefaults$var(); return true;
                         cases.append(m().Case(makeInt(ai.getEnumeration()), List.<JCStatement>of(applyDefaultsCall, returnExpr)));
                     }
                 }
     
                 // Reset diagnostic position to current class.
                 resetCurrentPos();
                 
                 // If there were some location vars.
                 if (cases.nonEmpty()) {
                     // varNum - VBASE$
                     JCExpression tagExpr = m().Binary(JCTree.MINUS, Id(varNumName), Id(defs.varBaseName));
                     // Construct and add: switch(varNum - VBASE$) { ... } 
                     stmts.append(m().Switch(tagExpr, cases.toList()));
                 }
 
                 // If there is a super class.
                 if (superClassSym != null) {
                     // super
                     JCExpression selector = Id(names._super);
                     // (varNum)
                     List<JCExpression> args = List.<JCExpression>of(Id(varNumName));
                     // super.applyDefaults$(varNum);
                     JCExpression callExp = callExpression(currentPos, selector, defs.applyDefaultsPrefixName, args);
                     // Construct and add: return super.applyDefaults$(varNum);
                     stmts.append(m().Return(callExp));
                 } else {
                     // Construct and add: return false;
                     stmts.append(m().Return(makeBoolean(false)));
                 }
 
                 // varNum ARG
                 JCVariableDecl arg = m().VarDef(m().Modifiers(Flags.FINAL | Flags.PARAMETER),
                                                               varNumName,
                                                               makeType(syms.intType),
                                                               null);
                 // Construct method.
                 JCMethodDecl method = makeMethod(Flags.PUBLIC,
                                                  syms.booleanType,
                                                  defs.applyDefaultsPrefixName,
                                                  List.<JCVariableDecl>of(arg),
                                                  stmts);
                 // Add to the methods list.
                 methods.append(method);
             }
             
             return methods.toList();
         }
 
         //
         // This methods generates the getDependency$ method for this class.
         //
         public List<JCTree> makeGetDependency() {
             // Buffer for new methods.
             ListBuffer<JCTree> methods = ListBuffer.lb();
             
             // Prepare to accumulate statements.
             ListBuffer<JCStatement> stmts = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
             // Prepare to accumulate cases.
             ListBuffer<JCCase> cases = ListBuffer.lb();
             
             // Gather this class' instance attributes.
             List<VarInfo> attrInfos = analysis.instanceAttributeInfos();
             for (VarInfo ai : attrInfos) {
                 // Only process attributes declared in this class (includes mixins.)
                 if (ai.needsDeclaration()) {
                     // Set the current diagnostic position.
                     setCurrentPos(ai);
                     // Grab the variable symbol.
                     VarSymbol varSym = ai.getSymbol();
                     
                     // getDependency$var()
                     JCExpression callExp = callExpression(currentPos, null, attributeGetLocationName(varSym), List.<JCExpression>nil());
                     // (Location)getDependency$var()
                     JCExpression castExpr = m().TypeCast(makeType(locationType), callExp);
                     // return (Location)getDependency$var()
                     JCStatement returnStmt = m().Return(castExpr);
                     // i: return (Location)getDependency$var();
                     cases.append(m().Case(makeInt(ai.getEnumeration()), List.<JCStatement>of(returnStmt)));
                 }
             }
             
             // Reset diagnostic position to current class.
             resetCurrentPos();           
             // Grab the super class.
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
             
             // Only bother if there are location vars or no super class.
             if (cases.nonEmpty() || superClassSym == null) {
                 // If there were some location vars.
                 if (cases.nonEmpty()) {
                     // varNum - VBASE$
                     JCExpression tagExpr = m().Binary(JCTree.MINUS, Id(varNumName), Id(defs.varBaseName));
                     // Construct and add: switch(varNum - VBASE$) { ... } 
                     stmts.append(m().Switch(tagExpr, cases.toList()));
                 }
                 
                 // If there is a super class.
                 if (superClassSym != null) {
                     // super
                     JCExpression selector = Id(names._super);
                     // (varNum)
                     List<JCExpression> args = List.<JCExpression>of(Id(varNumName));
                     // super.getDependency$(varNum);
                     JCExpression callExp = callExpression(currentPos, selector, defs.getLocationPrefixName, args);
                     // Construct and add: return super.getDependency$(varNum);
                     stmts.append(m().Return(callExp));
                 } else {
                     // Construct and add: return null;
                     stmts.append(m().Return(makeNull()));
                 }
     
                 // varNum ARG
                 JCVariableDecl arg = m().VarDef(m().Modifiers(Flags.FINAL | Flags.PARAMETER),
                                                               varNumName,
                                                               makeType(syms.intType),
                                                               null);
                 // Construct method.
                 JCMethodDecl method = makeMethod(Flags.PUBLIC,
                                                  locationType,
                                                  defs.getLocationPrefixName,
                                                  List.<JCVariableDecl>of(arg),
                                                  stmts);
                 // Add to the methods list.
                 methods.append(method);
             }
             
             return methods.toList();
         }
         
         //
         // This method constructs the initializer for a var map.
         //
         public JCExpression makeInitVarMapExpression(ClassSymbol cSym, LiteralInitVarMap varMap) {
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
              // Build up the argument list for the call.
             ListBuffer<JCExpression> args = ListBuffer.lb();
             // X.VCNT$()
             args.append(m().Apply(null, m().Select(makeType(cSym.type), defs.varCountName), List.<JCExpression>nil()));
             
             // For each var declared in order (to make the switch tags align to the vars.)
             for (VarSymbol vSym : varMap.varList.toList()) {
                 // ..., X.VOFF$x(), ...
                 
                 args.append(m().Apply(null, m().Select(makeType(cSym.type), attributeOffsetName(vSym)), List.<JCExpression>nil()));
             }
             
             // FXBase.makeInitMap$
             JCExpression methExpr = m().Select(makeType(syms.javafx_FXBaseType), makeInitMap);
             // FXBase.makeInitMap$(X.VCNT$(), X.VOFF$a(), ...)
             return m().Apply(null, methExpr, args.toList());
         }
         
         //
         // This method constructs a single var map declaration.
         //
         public JCVariableDecl makeInitVarMapDecl(ClassSymbol cSym, LiteralInitVarMap varMap) {
             // Reset diagnostic position to current class.
             resetCurrentPos();
             // Fetch name of map.
             Name mapName = varMapName(cSym);
             // static short[] Map$X;
             return makeVariable(Flags.STATIC, syms.javafx_ShortArray, mapName, null);
         }
          
         //
         // This method constructs a single var map initial value.
         //
         public JCStatement makeInitVarMapInit(ClassSymbol cSym, LiteralInitVarMap varMap) {
             // Reset diagnostic position to current class.
             resetCurrentPos();
             // Fetch name of map.
             Name mapName = varMapName(cSym);
             // Map$X = FXBase.makeInitMap$(X.VCNT$, X.VOFF$a, ...);
             return m().Exec(m().Assign(Id(mapName), makeInitVarMapExpression(cSym, varMap)));
         }
          
         //
         // This method constructs declarations for var maps used by literal initializers.
         //
         public List<JCTree> makeInitClassMaps(LiteralInitClassMap initClassMap) {
             // Buffer for new vars and methods.
             ListBuffer<JCTree> members = ListBuffer.lb();
             // Reset diagnostic position to current class.
             resetCurrentPos();
             
             // For each class initialized in the current class.
             for (ClassSymbol cSym : initClassMap.classMap.keySet()) {
                 // Get the var map for the referencing class.
                 LiteralInitVarMap varMap = initClassMap.classMap.get(cSym);
                 // Add to var to list.
                 members.append(makeInitVarMapDecl(cSym, varMap));
                 
                 // Fetch name of map.
                 Name mapName = varMapName(cSym);
                 // Prepare to accumulate statements.
                 ListBuffer<JCStatement> stmts = ListBuffer.lb();
                 
                 if (analysis.isAnonClass(cSym)) {
                     // Construct and add: return MAP$X;
                     stmts.append(m().Return(Id(mapName)));
                 } else {
                     // MAP$X == null
                     JCExpression condition = m().Binary(JCTree.EQ, Id(mapName), makeNull());
                     // MAP$X = FXBase.makeInitMap$(X.VCNT$, X.VOFF$a, ...)
                     JCExpression assignExpr = m().Assign(Id(mapName), makeInitVarMapExpression(cSym, varMap));
                     // Construct and add: return MAP$X == null ? (MAP$X = FXBase.makeInitMap$(X.VCNT$, X.VOFF$a, ...)) : MAP$X;
                     stmts.append(m().Return(m().Conditional(condition, assignExpr, Id(mapName))));
                 }
                 
                 // Construct lazy accessor method.
                 JCMethodDecl method = makeMethod(Flags.PUBLIC | Flags.STATIC,
                                                  syms.javafx_ShortArray,
                                                  varGetMapName(cSym),
                                                  List.<JCVariableDecl>nil(),
                                                  stmts);
                 // Add method to list.
                 members.append(method);
             }
             
             return members.toList();
         }
         
 
         //
         // This method constructs a super call with appropriate arguments.
         //
         private JCStatement makeSuperCall(ClassSymbol cSym, Name name) {
             return makeSuperCall(cSym, name, List.<JCExpression>nil());
         }
         private JCStatement makeSuperCall(ClassSymbol cSym, Name name, List<JCExpression> args) {
             // If this is from a mixin class then we need to use receiver$ otherwise this.
             boolean fromMixinClass = analysis.isMixinClass();
             // If this is to a mixin class then we need to use receiver$ otherwise this.
             boolean toMixinClass = JavafxAnalyzeClass.isMixinClass(cSym);
             // If this class doesn't have a javafx super then punt to FXBase.
             boolean toFXBase = cSym == null;
             
             // Add in the receiver if necessary.
             if (toMixinClass || toFXBase) {
                 // Determine the receiver name.
                 Name receiver = fromMixinClass ? defs.receiverName : names._this;
                 args.prepend(Id(receiver));
             }
             
             // Determine the selector.
             JCExpression selector;
             if (toMixinClass) {
                 selector = makeType(cSym.type, false);
             } else if (toFXBase) {
                 selector = makeType(syms.javafx_FXBaseType, false);
             } else {
                 selector = Id(names._super);
             }
             
             // Construct the call.
             
             JCStatement call = callStatement(currentPos, selector, name, args);
             
             return call;
         }
     
         //
         // This method adds the cascading calls to the super classes and mixins.  The topdown flag indicates
         // whether the calls should be made in top down order or bottom up order.  The analysis is used to 
         // determine whether the method is static (mixin) or an instance (normal.)  The analysis also
         // indicates whether the inheritance goes back to the FXBase class or whether it inherits from a
         // java class.
         //
         private ListBuffer<JCStatement> addSuperCalls(Name name, ListBuffer<JCStatement> stmts, boolean topdown) {
             // Get the current class's super class.                                         
             ClassSymbol superClassSym = analysis.getFXSuperClassSym();
             // Get the immediate mixin classes.
             List<ClassSymbol> immediateMixinClasses = analysis.getImmediateMixins();
             // Construct a list to hold the super calls in the correct order.
             ListBuffer<JCStatement> superCalls = ListBuffer.lb();
             
             // Order calls appropriately.
             if (topdown) {
                 // Call the super.
                 if (superClassSym != null) {
                     superCalls.append(makeSuperCall(superClassSym, name));
                 } else {
                     // TODO - call FXBase.name();
                 }
                 
                 // Call the immediate mixins.
                 for (ClassSymbol cSym : immediateMixinClasses) {
                     superCalls.append(makeSuperCall(cSym, name));
                 }
                 
                 stmts = superCalls.appendList(stmts);
             } else {
                 // Call the super.
                 if (superClassSym != null) {
                     superCalls.prepend(makeSuperCall(superClassSym, name));
                 } else {
                     // TODO - call FXBase.name();
                 }
                 
                 // Call the immediate mixins.
                 for (ClassSymbol cSym : immediateMixinClasses) {
                     superCalls.prepend(makeSuperCall(cSym, name));
                 }
                 
                 stmts = stmts.appendList(superCalls);
             }
     
             
             return stmts;
         }
         
         //
         // This method is a convenience routine to simplify making runtime methods.
         //
         private JCMethodDecl makeMethod(JCModifiers modifiers, Type type, Name name,
                                         List<JCVariableDecl> args, ListBuffer<JCStatement> stmts) {
                                         
             JCBlock body = stmts != null ? m().Block(0L, stmts.toList()) : null;
                                         
             // Construct the method.
             JCMethodDecl method = m().MethodDef(
                 modifiers,                                     // Modifiers
                 name,                                          // Name
                 makeType(type),                                // Return type
                 List.<JCTypeParameter>nil(),                   // Argument types
                 args,                                          // Argument variables
                 List.<JCExpression>nil(),                      // Throws
                 body,                                          // Body
                 null);                                         // Default
                 
             return method;
         }
         private JCMethodDecl makeMethod(long modifiers, Type type, Name name,
                                         List<JCVariableDecl> args, ListBuffer<JCStatement> stmts) {
                                         
             JCBlock body = stmts != null ? m().Block(0L, stmts.toList()) : null;
                                         
             // Construct the method.
             JCMethodDecl method = m().MethodDef(
                 make.Modifiers(modifiers),                     // Modifiers
                 name,                                          // Name
                 makeType(type),                                // Return type
                 List.<JCTypeParameter>nil(),                   // Argument types
                 args,                                          // Argument variables
                 List.<JCExpression>nil(),                      // Throws
                 body,                                          // Body
                 null);                                         // Default
                 
             return method;
         }
     }
 }
