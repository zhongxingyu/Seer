 /*
  * This class translates dumps an AST from the javaparser library in
  * the "deriving Read" forma for the Java AST used by the language-java package
  * for Haskell.
  *
  * A small number of constructs, custom annotations and empty declarations,
  * are unsupported. Comments and annotations are dropped.
  *
  * NodeField and its descendants are used to create a language specifying how
  * to print nodes; these are used to handle most sorts. See the definition of "metaInf"
  * below. When a sort in the javaparser AST corresponds to multiple sorts in the
  * language-java AST, generally the "smallest" one is chosen, and special functions
  * are provided to output to a larger one. For example, ClassOrInterfaceType
  * variously corresponds to "ClassType" or "Type". Thus, the visit
  * method outputs it as a "ClassType", but the "printType" function
  * outputs it as a "Type".
  *
  * This file makes heavy use of dynamic typing.
  *
  * @author James Koppel
  */
 
 /*
  *
  * Notes on what Java types are translated into which Haskell types
  *
  * Visits
  *   CompilationUnit -> CompilationUnit
  *   PackageDeclaration -> PackageDeclaration
  *   ImportDeclaration -> ImportDeclaration
  *   TypeParameter -> TypeParam
  *   ClassOrInterfaceDeclaration -> TypeDecl 
  *   TypeDeclaration -> TypeDecl
  *   ClassOrInterfaceType -> ClassType
  *   Expression -> Exp
  *   EnumConstantDeclaration -> EnumConstant
  *   FieldDeclaration -> MemberDecl
  *   VariableDeclarator -> VarDecl
  *   VariableDeclaratorId -> VarDeclId
  *   ConstructorDeclaration -> MemberDecl
  *   MethodDeclaration -> MemberDecl
  *   Parameter -> FormalParam
  *   BlockStmt -> Block
  *   Type -> Type
  *   InitializerDeclaration -> Decl
  *   PrimitiveType -> Type
  *   ReferenceType -> RefType
  *   WildcardType -> TypeArgument
  *   ArrayAccessExpr -> Exp
  *   Expression -> Exp
  *   ArrayCreationExpr -> Exp
  *   ArrayInitializerExpr -> [VarInit]
  *   AssignExpr -> Exp
  *   BinaryExpr -> Exp
  *   CastExpr -> Exp
  *   ClassExpr -> Exp
  *   ConditionalExpr -> Exp
  *   EnclosedExpr -> Exp
  *   FieldAccess -> Exp
  *   InstanceOfExpr -> Exp
  *   NameExpr -> Exp
  *   other exprs -> Exp
  *   ExplicitConstructorInvocationStmt -> ExplConstrInv
  *   VariableDeclarationExpr -> BlockStmt
  *   TypeDeclarationStmt -> BlockStmt
  *   Assert -> Stmt
  *   SwitchEntryStmt -> SwitchBlock
  *   other statements -> Stmt
  *   CatchClause -> Catch
  * Others
  *   printModifiers :: int -> [Modifer]
  *   printIdent :: String -> Ident
  *   printName :: NameExpr -> Name
  *   printNameIdent :: NameExpr -> Ident
  *   printMemberDecl :: BodyDeclaration -> MemberDecl
  *   printDecl :: BodyDeclaration -> Decl
  *   printType :: Type -> Type
  *   printVarInit :: Expression -> VarInit
  *   printExceptionType :: NameExpr -> ExceptionType
  *   printConstructorBody :: BlockStmt -> ConstructorBody
  *   printVoidableType :: Type -> Maybe Type
  *   printPrimitive :: PrimitiveType.Primitive -> Type
  *   printTypeArg :: Type -> TypeArgument
  *   printVarInit :: Expression -> VarInit
  *   printLHS :: Expression -> LHS
  *   printAssignOp :: AssignExpr.Operator -> AssignOp
  *   printBinOp :: BinExpr.Operator -> Op
  *   printClassTypeID :: ClassOrInterfaceType -> Ident
  *   printStmt :: Statement -> Stmt
  *   printBlockStmt :: Statement -> BlockStmt
  *   printSwitchLabel :: Expression -> SwitchLabel
  *   printForInit :: List<Expression> -> ForInit
  */
 
 import java.lang.reflect.Method;
 import java.io.*;
 import java.util.*;
 
 import org.apache.commons.lang3.math.NumberUtils;
 
 import japa.parser.*;
 import japa.parser.ast.*;
 import japa.parser.ast.expr.*;
 import japa.parser.ast.body.*;
 import japa.parser.ast.stmt.*;
 import japa.parser.ast.type.*;
 import japa.parser.ast.visitor.*;
 
 public class ToDeriveReadVisitor implements VoidVisitor {
 
     private static ToDeriveReadVisitor instance;
     private PrintWriter out;
     
     public ToDeriveReadVisitor(PrintWriter out) {
         this.out = out;
         instance = this;
     }
 
     private static ToDeriveReadVisitor getInstance() {
         return instance;
     }
 
     private static void output(String s) {
         getInstance().out.print(s);
     }
     
     private static abstract class NodeField {
         protected abstract Object getValueFrom(Object n); //optional
         protected abstract void printValue(Object x); //less optional
         
         protected void printValueFor(Object n) {
             printValue(getValueFrom(n));
         }
     }
 
     private static class RawField extends NodeField {
         private String fieldName;
         
         protected Object getValueFrom(Object n) {
             Method getter;
             try {
                 getter = n.getClass().getMethod("get"+fieldName);
             } catch(NoSuchMethodException _) {
                 try {
                   getter = n.getClass().getMethod("is"+fieldName);
                 } catch(NoSuchMethodException e) {
                     throw new ExceptionConverter(e);
                 }
             }
 
             try {
               return getter.invoke(n);
             } catch(Exception e) {
                 throw new ExceptionConverter(e);
             }
         }
 
         protected void printValue(Object val) {
             if(val instanceof Boolean) {
                 if(((Boolean)val).booleanValue()) {
                     output("True");
                 } else {
                     output("False");
                 }
             } else if(val instanceof Integer) {
                 output(Integer.toString(((Integer)val).intValue()));
             } else {
                 ((Node)val).accept(getInstance(), null);
              }
         }
     }
         
     protected static RawField f(String name) {
         RawField f = new RawField();
         f.fieldName = name;
         return f;
     }
     
     private static class MaybeField extends NodeField {
         private NodeField field;
 
         protected Object getValueFrom(Object n) {
             return field.getValueFrom(n);
         }
 
         protected void printValue(Object x) {
             if(x == null) {
                 output("Nothing");
             } else {
                 output("(Just ");
                 field.printValue(x);
                 output(")");
             }
         }
     }
         
     protected static MaybeField maybe(NodeField f) {
         MaybeField m = new MaybeField();
         m.field = f;
         return m;
     }
 
     private static class MaybeListField extends NodeField {
         private NodeField field;
 
         protected Object getValueFrom(Object n) {
             return field.getValueFrom(n);
         }
 
         protected void printValue(Object x) {
             List l = (List)x;
             if(l == null || l.isEmpty()) {
                 output("Nothing");
             } else {
                 output("(Just ");
                 field.printValue(l.get(0));
                 output(")");
             }
         }
     }
     
     protected static MaybeListField maybelist(NodeField f) {
         MaybeListField m = new MaybeListField();
         m.field = f;
         return m;
     }
 
     private static class ListField extends RawField {
         private NodeField field;
 
         protected Object getValueFrom(Object n) {
             return field.getValueFrom(n);
         }
 
         protected void printValueFor(Object n) {
             List l = (List)field.getValueFrom(n);
             printValue(l);
         }
 
         protected void printValue(Object x) {
             if(x == null) {
                 output("[]");
                 return;
             }
             
             List l = (List)x;
             output("[");
             String sep = "";
             for(Object o : l) {
                 output(sep);
                 sep = ", ";
                 field.printValue(o);
             }
             output("]");
         }
     }
         
     protected static ListField list(NodeField f) {
         ListField l = new ListField();
         l.field = f;
         return l;
     }
 
     private static class SpecialField extends RawField {
         private NodeField field;
         private Method printer;
 
         protected Object getValueFrom(Object n) {
             return field.getValueFrom(n);
         }
 
         protected void printValue(Object x) {
             try {
                 printer.invoke(getInstance(), x);
             } catch(Exception e) {
                 throw new ExceptionConverter(e);
             }
         }
     }
                 
     protected static SpecialField id(NodeField f) {
         return special(f, "printIdent");
     }
     
     protected static SpecialField special(NodeField f, String methName) {
         SpecialField s = new SpecialField();
         s.field = f;
         try {
             s.printer = ToDeriveReadVisitor.class.getMethod(methName, Object.class);
         } catch(NoSuchMethodException e) {
             throw new ExceptionConverter(e);
         }
         return s;
     }
     
     private static class FieldWrap extends NodeField {
         private String name;
         private List<NodeField> contents;
 
         protected Object getValueFrom(Object n) {
             if(contents.size() > 1)
                 throw new UnsupportedOperationException();
 
             return contents.get(0).getValueFrom(n);
         }
 
         protected void printValueFor(Object n) {
             output("(");
             output(name);
 
             for(NodeField f : contents) {
                 output(" ");
                 f.printValueFor(n);
             }
             
             output(")");
         }
 
         protected void printValue(Object x) {
             if(contents.size() > 1)
                 throw new UnsupportedOperationException();
 
             output("(");
             output(name);
             output(" ");
             contents.get(0).printValue(x);
             output(")");
         }
     }
     
     protected static FieldWrap wrap(String name, NodeField... contents) {
         FieldWrap f = new FieldWrap();
         f.name = name;
         f.contents = Arrays.asList(contents);
         return f;
     }
     
     private static HashMap<String, NodeField> metaInf;
 
     static {
         metaInf = new HashMap<String, NodeField>() {{
                 put("CompilationUnit", wrap("CompilationUnit", maybe(f("Package")), list(f("Imports")), list(f("Types"))));
                 put("ImportDeclaration", wrap("ImportDecl", f("Static"), special(f("Name"), "printName"), f("Asterisk")));
                 put("PackageDeclaration", wrap("PackageDecl", special(f("Name"), "printName")));
                 put("TypeParameter", wrap("TypeParam", id(f("Name")), list(wrap("ClassRefType", f("TypeBound")))));
                 put("_InterfaceDeclaration", wrap("InterfaceTypeDecl", wrap("InterfaceDecl",
                                                   special(f("Modifiers"), "printModifiers"), id(f("Name")), list(f("TypeParameters")),
                                                   list(wrap("ClassRefType", f("Implements"))),
                                                   wrap("InterfaceBody", list(special(f("Members"), "printMemberDecl"))))));
                 put("_ClassDeclaration", wrap("ClassDecl",
                                               special(f("Modifiers"), "printModifiers"), id(f("Name")), list(f("TypeParameters")),
                                               maybelist(wrap("ClassRefType", f("Extends"))), list(wrap("ClassRefType", f("Implements"))),
                                               wrap("ClassBody", list(special(f("Members"), "printDecl")))));
                 put("EnumDeclaration", wrap("ClassTypeDecl", wrap("EnumDecl",
                                                                   special(f("Modifiers"), "printModifiers"), id(f("Name")),
                                                                   list(wrap("ClassRefType", f("Implements"))),
                                                                   wrap("EnumBody", list(f("Entries")),
                                                                        maybe(wrap("ClassBody", list(special(f("Members"), "printDecl"))))))));
                 put("EnumConstantDeclaration", wrap("EnumConstant", id(f("Name")), list(f("Args")), maybe(wrap("ClassBody", list(special(f("ClassBody"), "printDecl"))))));
                 put("FieldDeclaration", wrap("FieldDecl", special(f("Modifiers"), "printModifiers"), special(f("Type"), "printType"), list(f("Variables"))));
                 put("VariableDeclarator", wrap("VarDecl", f("Id"), maybe(special(f("Init"), "printVarInit"))));
                 put("ConstructorDeclaration", wrap("ConstructorDecl", special(f("Modifiers"), "printModifiers"),
                                                    list(f("TypeParameters")), id(f("Name")), list(f("Parameters")),
                                                    list(special(f("Throws"), "printExceptionType")), special(f("Block"), "printConstructorBody")));
                 put("MethodDeclaration", wrap("MethodDecl", special(f("Modifiers"), "printModifiers"), list(f("TypeParameters")), special(f("Type"), "printVoidableType"),
                                               id(f("Name")), list(f("Parameters")), list(special(f("Throws"), "printExceptionType")),
                                               wrap("MethodBody", maybe(f("Body")))));
                 put("Parameter", wrap("FormalParam", special(f("Modifiers"), "printModifiers"), special(f("Type"), "printType"), f("VarArgs"), f("Id")));
                 put("InitializerDeclaration", wrap("InitDecl", f("Static"), f("Block")));
                 put("PrimitiveType", wrap("PrimType", special(f("Type"), "printPrimitive")));
                 put("ArrayAccessExpr", wrap("ArrayAccess", wrap("ArrayIndex", f("Name"), f("Index"))));
                 put("_ArrayAccessLhs", wrap("ArrayLhs", wrap("ArrayIndex", f("Name"), f("Index"))));
                 put("_ArrayCreate", wrap("ArrayCreate", special(f("Type"), "printType"), list(f("Dimensions")), f("ArrayCount")));
                 put("_ArrayCreateInit", wrap("ArrayCreateInit", special(f("Type"), "printType"), f("ArrayCount"), wrap("ArrayInit", f("Initializer"))));
                 put("ArrayInitializerExpr", list(special(f("Values"), "printVarInit")));
                 put("AssignExpr", wrap("Assign", special(f("Target"), "printLHS"), special(f("Operator"), "printAssignOp"), f("Value")));
                 put("BinaryExpr", wrap("BinOp", f("Left"), special(f("Operator"), "printBinOp"), f("Right")));
                 put("CastExpr", wrap("Cast", special(f("Type"), "printType"), f("Expr")));
                 put("ClassExpr", wrap("ClassLit", special(f("Type"), "printVoidableType")));
                 put("ConditionalExpr", wrap("Cond", f("Condition"), f("ThenExpr"), f("ElseExpr")));
                 put("EnclosedExpr", f("Inner"));
                 put("_PrimaryFieldAccess", wrap("PrimaryFieldAccess", f("Scope"), id(f("Field"))));
                 put("_SuperFieldAccess", wrap("SuperFieldAccess", id(f("Field"))));
                 put("InstanceOfExpr", wrap("InstanceOf", f("Expr"), f("Type")));
                 put("BooleanLiteralExpr", wrap("Lit", wrap("Boolean", f("Value"))));
                 put("_InstanceCreation", wrap("InstanceCreation", list(special(f("TypeArgs"), "printTypeArg")), f("Type"), list(f("Args")), maybe(wrap("ClassBody", list(special(f("AnonymousClassBody"), "printDecl"))))));
                 put("_QualInstanceCreation", wrap("QualInstanceCreation", f("Scope"), list(special(f("TypeArgs"), "printTypeArg")), special(f("Type"), "printClassTypeID"), list(f("Args")), maybelist(f("AnonymousClassBody"))));
                 put("VariableDeclarationExpr", wrap("LocalVars", special(f("Modifiers"), "printModifiers"), special(f("Type"), "printType"), list(f("Vars"))));
                 put("_ForLocalVars", wrap("ForLocalVars", special(f("Modifiers"), "printModifiers"), special(f("Type"), "printType"), list(f("Vars"))));
                 put("_ThisInvoke", wrap("ThisInvoke", list(f("TypeArgs")), list(f("Args"))));
                 put("_SuperInvoke", wrap("SuperInvoke", list(f("TypeArgs")), list(f("Args"))));
                 put("_PrimarySuperInvoke", wrap("PrimarySuperInvoke", f("Expr"), list(f("TypeArgs")), list(f("Args"))));
                 put("AssertStmt", wrap("Assert", f("Check"), maybe(f("Message"))));
                 put("BlockStmt", wrap("Block", list(special(f("Stmts"), "printBlockStmt"))));
                 put("LabeledStmt", wrap("Labeled", id(f("Label")), special(f("Stmt"), "printStmt")));
                 put("ExpressionStmt", wrap("ExpStmt", f("Expression")));
                 put("SwitchStmt", wrap("Switch", f("Selector"), list(f("Entries"))));
                 put("SwitchEntryStmt", wrap("SwitchBlock", special(f("Label"), "printSwitchLabel"), list(special(f("Stmts"), "printBlockStmt"))));
                 put("BreakStmt", wrap("Break", maybe(id(f("Id")))));
                 put("ContinueStmt", wrap("Continue", maybe(id(f("Id")))));
                 put("ReturnStmt", wrap("Return", maybe(f("Expr"))));
                 put("_IfThen", wrap("IfThen", f("Condition"), special(f("ThenStmt"), "printStmt")));
                 put("_IfThenElse", wrap("IfThenElse", f("Condition"), special(f("ThenStmt"), "printStmt"), special(f("ElseStmt"), "printStmt")));
                 put("WhileStmt", wrap("While", f("Condition"), special(f("Body"), "printStmt")));
                 put("DoStmt", wrap("While", special(f("Body"), "printStmt"), f("Condition")));
                 put("ThrowStmt", wrap("Throw", f("Expr")));
                 put("SynchronizedStmt", wrap("Synchronized", f("Expr"), f("Block")));
                 put("ForStmt", wrap("BasicFor", maybe(special(f("Init"), "printForInit")), maybe(f("Compare")), maybe(list(f("Update"))), special(f("Body"), "printStmt")));
                 put("TryStmt", wrap("Try", f("TryBlock"), list(f("Catchs")), maybe(f("FinallyBlock"))));
                 put("CatchClause", wrap("Catch", f("Except"), f("CatchBlock")));
             }};
     }
     
 
     private void dispatchVisit(Object n, String key) {
         NodeField f = metaInf.get(key);
         f.printValueFor(n);
     }
     
     private void genericVisit(Object n) {
         String name = n.getClass().getSimpleName();
         dispatchVisit(n, name);
     }
 
     public void printIdent(Object o) { printIdent((String)o); }
     
     public void printIdent(String id) {
         output("(Ident \"");
         output(id);
         output("\")");
     }
 
     public void printMemberDecl(Object o) { printMemberDecl((BodyDeclaration)o); }
 
     public void printMemberDecl(BodyDeclaration n) {
         if(n instanceof FieldDeclaration ||
            n instanceof MethodDeclaration ||
            n instanceof ConstructorDeclaration) {
 
             n.accept(this, null);
         } else if(n instanceof ClassOrInterfaceDeclaration) {
             if(((ClassOrInterfaceDeclaration)n).isInterface()) {
                 output("(MemberInterfaceDecl ");
                 n.accept(this, null);
                 output(")");
             } else {
                 output("(MemberClassDecl ");
                 dispatchVisit(n, "_ClassDeclaration");
                 output(")");
             }
         } else if(n instanceof EnumDeclaration) {
             output("(MemberClassDecl ");
             n.accept(this, null);
             output(")");
         } else {
             throw new IllegalArgumentException("Unsupported node passed to printMemberDecl");
         }
             
     }
 
     public void printDecl(Object o) { printDecl((BodyDeclaration)o); }
 
     public void printDecl(BodyDeclaration n) {
         if(n instanceof InitializerDeclaration) {
             n.accept(this, null);
         } else {
             output("(MemberDecl ");
             printMemberDecl(n);
             output(")");
         }
     }
 
     public void printName(Object o) { printName((NameExpr)o); }
 
     public void printName(NameExpr n) {
         List<NameExpr> l = new LinkedList<NameExpr>();
         for(NameExpr e = n; e != null; e = e instanceof QualifiedNameExpr ? ((QualifiedNameExpr)e).getQualifier() : null) {
             l.add(0, e);
         }
 
         wrap("Name", list(special(null, "printNameIdent"))).printValue(l);
     }
 
     public void printNameIdent(Object o) { printNameIdent((NameExpr)o); }
 
     public void printNameIdent(NameExpr n) {
         printIdent(n.getName());
     }
 
     public void printModifiers(Object o) { printModifiers(((Integer)o).intValue()); }
 
     public void printModifiers(int m) {
         output("[");
         String sep = "";
 
         if(ModifierSet.isAbstract(m)) {
             output(sep); sep = ", ";
             output("Abstract");
         }
         if(ModifierSet.isFinal(m)) {
             output(sep); sep = ", ";
             output("Final");
         }
         if(ModifierSet.isNative(m)) {
             output(sep); sep = ", ";
             output("Native");
         }
         if(ModifierSet.isPrivate(m)) {
             output(sep); sep = ", ";
             output("Private");
         }
         if(ModifierSet.isProtected(m)) {
             output(sep); sep = ", ";
             output("Protected");
         }
         if(ModifierSet.isPublic(m)) {
             output(sep); sep = ", ";
             output("Public");
         }
         if(ModifierSet.isStatic(m)) {
             output(sep); sep = ", ";
             output("Static");
         }
         if(ModifierSet.isStrictfp(m)) {
             output(sep); sep = ", ";
             output("StrictFP");
         }
         if(ModifierSet.isSynchronized(m)) {
             output(sep); sep = ", ";
            output("Synchronised"); // "Synchronized" refers to the type of statement
         }
         if(ModifierSet.isTransient(m)) {
             output(sep); sep = ", ";
             output("Transient");
         }
         if(ModifierSet.isVolatile(m)) {
             output(sep); sep = ", ";
             output("Volatile");
         }
 
         output("]");
     }
     
     public void printType(Object o) { printType((Type)o); }
 
     public void printType(Type n) {
         if(n instanceof PrimitiveType) {
             n.accept(this, null);
         } else if(n instanceof ClassOrInterfaceType) {
             output("(RefType (ClassRefType ");
             n.accept(this, null);
             output("))");
         } else if(n instanceof ReferenceType) {
             output("(RefType ");
             n.accept(this, null);
             output(")");
         } else {
             throw new IllegalArgumentException("Illegal node passed to printType");
         }
     }
 
     public void printExceptionType(Object o) { printExceptionType((NameExpr)o); }
 
     public void printExceptionType(NameExpr n) {
         List<NameExpr> l = new LinkedList<NameExpr>();
         for(NameExpr e = n; e != null; e = e instanceof QualifiedNameExpr ? ((QualifiedNameExpr)e).getQualifier() : null) {
             l.add(0, e);
         }
 
         output("(ClassRefType (ClassType [");
 
         String sep = "";
         for(NameExpr e : l) {
             output(sep);
             sep = ", ";
             output("(");
             printIdent(e.getName());
             output(", [])");
         }
         
         output("]))");
         
     }
     
 
     public void printVoidableType(Object o) { printVoidableType((Type)o); }
 
     public void printVoidableType(Type n) {
         if(n instanceof VoidType) {
             output("Nothing");
         } else {
             output("(Just ");
             printType(n);
             output(")");
         }
     }
 
     public void printPrimitive(Object o) { printPrimitive((PrimitiveType.Primitive)o); }
 
     public void printPrimitive(PrimitiveType.Primitive p) {
         switch(p) {
         case Boolean:
             output("BooleanT");
             break;
         case Byte:
             output("ByteT");
             break;
         case Char:
             output("CharT");
             break;
         case Double:
             output("DoubleT");
             break;
         case Float:
             output("FloatT");
             break;
         case Int:
             output("IntT");
             break;
         case Long:
             output("LongT");
             break;
         case Short:
             output("ShortT");
             break;
         }
     }
 
     public void printConstructorBody(Object o) { printConstructorBody((BlockStmt)o); }
 
     public void printConstructorBody(BlockStmt n) {
         List<Statement> l = n.getStmts();
         if(l == null)
             l = new ArrayList<Statement>();
         List<Statement> lNormalized;
 
         output("(ConstructorBody ");
         
         if(l.size() > 0 && l.get(0) instanceof ExplicitConstructorInvocationStmt) {
             output("(Just ");
             l.get(0).accept(this, null);
             output(") ");
             
             lNormalized = new ArrayList(l);
             lNormalized.remove(0);
         } else {
             output("Nothing ");
             lNormalized = l;
         }
 
         list(special(null, "printBlockStmt")).printValue(lNormalized);
 
         output(")");
     }
 
     public void printStmt(Object o) { printStmt((Statement)o); }
 
     public void printStmt(Statement n) {
         if(n instanceof BlockStmt) {
             output("(StmtBlock ");
             n.accept(this, null);
             output(")");
         } else {
             n.accept(this, null);
         }
     }
 
     public void printBlockStmt(Object o) { printBlockStmt((Statement)o); }
 
     public void printBlockStmt(Statement n) {
         if(n instanceof ExpressionStmt &&
            ((ExpressionStmt)n).getExpression() instanceof VariableDeclarationExpr) {
             ((ExpressionStmt)n).getExpression().accept(this, null);
         } else if(n instanceof TypeDeclarationStmt) {
             n.accept(this, null);
         } else {
             output("(BlockStmt ");
             printStmt(n);
             output(")");
         }
     }
 
     public void printSwitchLabel(Object o) { printSwitchLabel((Expression)o); }
 
     public void printSwitchLabel(Expression n) {
         if(n == null) {
             output("Default");
         } else {
             output("(SwitchCase ");
             n.accept(this, null);
             output(")");
         }
     }
 
     public void printForInit(Object o) { printForInit((List<Expression>)o); }
 
     public void printForInit(List<Expression> l) {
         if(l.size() == 1 && l.get(0) instanceof VariableDeclarationExpr) {
             dispatchVisit(l.get(0), "_ForLocalVars");
         } else {
             wrap("ForInitExps", list(f("Init"))).printValueFor(new ForStmt(l, null, null, null));
         }
     }
 
     public void printVarInit(Object o) { printVarInit((Expression)o); }
     
     public void printVarInit(Expression n) {
         if(n instanceof ArrayInitializerExpr) {
             output("(InitArray (ArrayInit ");
             n.accept(this, null);
             output("))");
         } else {
             output("(InitExp ");
             n.accept(this, null);
             output(")");
         }
     }
 
     public void printTypeArg(Object o) { printTypeArg((Type)o); }
     
     public void printTypeArg(Type n) {
         if(n instanceof WildcardType) {
             n.accept(this, null);
         } else if(n instanceof ReferenceType) {
             output("(ActualType ");
             n.accept(this, null);
             output(")");
         } else {
             throw new IllegalArgumentException("Illegal node passed to printTypeArg");
         }
     }
     
     public void printFieldAccess(Object o) { printFieldAccess((FieldAccessExpr)o); }
     
     public void printFieldAccess(FieldAccessExpr n) {
         if(n.getScope() instanceof SuperExpr) {
             if(((SuperExpr)n.getScope()).getClassExpr() == null) {
                 dispatchVisit(n, "_SuperFieldAccess");
             } else {
                 output("(ClassFieldAccess ");
                 printName(((SuperExpr)n.getScope()).getClassExpr());
                 output(" ");
                 printIdent(n.getField());
                 output(")");
             }
         } else {
             dispatchVisit(n, "_PrimaryFieldAccess");
         }
     }
     
     public void printLHS(Object o) { printLHS((Expression)o); }
 
     public void printLHS(Expression n) {
         if(n instanceof NameExpr) {
             output("(NameLhs ");
             printName(n);
             output(")");
         } else if(n instanceof FieldAccessExpr) {
             output("(FieldLhs ");
             printFieldAccess(n);
             output(")");
         } else if(n instanceof ArrayAccessExpr) {
             dispatchVisit(n, "_ArrayAccessLhs");
         } else {
             throw new IllegalArgumentException("Illegal node passed to printLHS");
         }
     }
 
     public void printClassTypeID(Object o) { printClassTypeID((ClassOrInterfaceType)o); }
 
     public void printClassTypeID(ClassOrInterfaceType n) {
         printIdent(n.getName());
     }
 
     public void printAssignOp(Object o) { printAssignOp((AssignExpr.Operator)o); }
 
     public void printAssignOp(AssignExpr.Operator o) {
         switch(o) {
         case and:
             output("AndA");
             break;
         case assign:
             output("EqualA");
             break;
         case lShift:
             output("LShiftA");
             break;
         case minus:
             output("SubA");
             break;
         case or:
             output("OrA");
             break;
         case plus:
             output("AddA");
             break;
         case rem:
             output("RemA");
             break;
         case rSignedShift:
             output("RShiftA");
             break;
         case rUnsignedShift:
             output("RRShiftA");
             break;
         case slash:
             output("DivA");
             break;
         case star:
             output("MultA");
             break;
         case xor:
             output("XorA");
             break;
         }
     }
 
     public void printBinOp(Object o) { printBinOp((BinaryExpr.Operator)o); }
 
     public void printBinOp(BinaryExpr.Operator o) {
         switch(o) {
         case and:
             output("CAnd");
             break;
         case binAnd:
             output("And");
             break;
         case binOr:
             output("Or");
             break;
         case divide:
             output("Div");
             break;
         case equals:
             output("Equal");
             break;
         case greater:
             output("GThan");
             break;
         case greaterEquals:
             output("GThanE");
             break;
         case less:
             output("LThan");
             break;
         case lessEquals:
             output("LThanE");
             break;
         case lShift:
             output("LShift");
             break;
         case minus:
             output("Sub");
             break;
         case notEquals:
             output("NotEq");
             break;
         case or:
             output("COr");
             break;
         case plus:
             output("Add");
             break;
         case remainder:
             output("Rem");
             break;
         case rSignedShift:
             output("RShift");
             break;
         case rUnsignedShift:
             output("RRShift");
             break;
         case times:
             output("Mult");
             break;
         case xor:
             output("Xor");
             break;
         }
     }
 
     private void fail(String name) {
         System.err.println("javaparser-to-hs failed because unsupported node " + name + " encountered.");
         System.exit(1);
     }
 
     
     //- Compilation Unit ----------------------------------
 
     public void visit(CompilationUnit n, Object _) { genericVisit(n); }
 
     public void visit(PackageDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(ImportDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(TypeParameter n, Object _) { genericVisit(n); }
 
     public void visit(LineComment n, Object _) { fail("LineComment"); }
 
     public void visit(BlockComment n, Object _) { fail("BlockComment"); }
 
     //- Body ----------------------------------------------
 
     public void visit(ClassOrInterfaceDeclaration n, Object _) {
         if(n.isInterface()) {
             dispatchVisit(n, "_InterfaceDeclaration");
         } else {
             output("(ClassTypeDecl ");
             dispatchVisit(n, "_ClassDeclaration");
             output(")");
         }
     }
 
     public void visit(EnumDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(EmptyTypeDeclaration n, Object _) { fail("EmptyTypeDeclaration"); }
 
     public void visit(EnumConstantDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(AnnotationDeclaration n, Object _) { fail("AnnotationDeclaration"); }
 
     public void visit(AnnotationMemberDeclaration n, Object _) { fail("AnnotationDeclaration"); }
 
     public void visit(FieldDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(VariableDeclarator n, Object _) { genericVisit(n); }
 
     
     public void visit(VariableDeclaratorId n, Object _) {
         for(int i = 0; i < n.getArrayCount(); i++) {
             output("(VarDeclArray ");
         }
 
         output("(VarId ");
         printIdent(n.getName());
         output(")");
         
         for(int i = 0; i < n.getArrayCount(); i++) {
             output(")");
         }
     }
 
     public void visit(ConstructorDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(MethodDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(Parameter n, Object _) { genericVisit(n); }
 
     public void visit(EmptyMemberDeclaration n, Object _) { fail("EmptyMemberDeclaration"); }
 
     public void visit(InitializerDeclaration n, Object _) { genericVisit(n); }
 
     public void visit(JavadocComment n, Object _) { fail("JavadocComment"); }
 
     //- Type ----------------------------------------------
 
     public void visit(ClassOrInterfaceType n, Object _) {
         List<ClassOrInterfaceType> qualifiers = new LinkedList<ClassOrInterfaceType>();
         for(ClassOrInterfaceType c = n; c != null; c = c.getScope()) {
             qualifiers.add(0, c);
         }
 
         output("(ClassType [");
 
         String sep = "";
         for(ClassOrInterfaceType c : qualifiers) {
             output(sep);
             sep = ", ";
             output("(");
             printIdent(c.getName());
             output(", ");
             list(special(f("TypeArgs"), "printTypeArg")).printValueFor(c);
             output(")");
         }
 
         output("])");
     }
 
     public void visit(PrimitiveType n, Object _) { genericVisit(n); }
 
     public void visit(ReferenceType n, Object _) {
         if(n.getArrayCount() == 0) {
             output("(ClassRefType ");
             n.getType().accept(this, null);
             output(")");
         } else {
             String sep = "";
             for(int i = 0; i < n.getArrayCount(); i++) {
                 output(sep);
                 sep = "(RefType ";
                 output("(ArrayType ");
             }
 
             printType(n.getType());
 
             sep = "";
             for(int i = 0; i < n.getArrayCount(); i++) {
                 output(sep);
                 sep = ")";
                 output(")");
             }
         }
     }
 
     public void visit(VoidType n, Object _) { fail("VoidType"); } // Should always be handled by printVoidableType
 
     public void visit(WildcardType n, Object _) {
         output("(Wildcard ");
 
         if(n.getExtends() != null) {
             output("(Just (ExtendsBound ");
             n.getExtends().accept(this, null);
             output("))");
         } else if(n.getSuper() != null) {
             output("(Just (SuperBound ");
             n.getExtends().accept(this, null);
             output("))");
         } else {
             output("Nothing");
         }
         
         output(")");
     }
 
     //- Expression ----------------------------------------
 
     public void visit(ArrayAccessExpr n, Object _) { genericVisit(n); }
 
     public void visit(ArrayCreationExpr n, Object _) {
         if(n.getInitializer() == null) {
             dispatchVisit(n, "_ArrayCreate");
         } else {
             dispatchVisit(n, "_ArrayCreateInit");
         }
     }
 
     public void visit(ArrayInitializerExpr n, Object _) { genericVisit(n); }
 
     public void visit(AssignExpr n, Object _) { genericVisit(n); }
 
     public void visit(BinaryExpr n, Object _) { genericVisit(n); }
 
     public void visit(CastExpr n, Object _) { genericVisit(n); }
 
     public void visit(ClassExpr n, Object _) { genericVisit(n); }
 
     public void visit(ConditionalExpr n, Object _) { genericVisit(n); }
 
     public void visit(EnclosedExpr n, Object _) { genericVisit(n); }
 
     public void visit(FieldAccessExpr n, Object _) {
         output("(FieldAccess ");
         printFieldAccess(n);
         output(")");
     }
 
     public void visit(InstanceOfExpr n, Object _) { genericVisit(n); }
 
     public void visit(StringLiteralExpr n, Object _) {
         output("(Lit (String \"");
         output(n.getValue());
         output("\"))");
     }
 
     public void visit(IntegerLiteralExpr n, Object _) {
         output("(Lit (Int ");
         output(NumberUtils.createNumber(n.getValue()).toString());
         output("))");
     }
 
     public void visit(LongLiteralExpr n, Object _) {
         output("(Lit (Int ");
         output(NumberUtils.createNumber(n.getValue()).toString());
         output("))");
     }
 
     public void visit(IntegerLiteralMinValueExpr n, Object _) {
         output("(Lit (Int ");
         output(NumberUtils.createNumber(n.getValue()).toString());
         output("))");
     }
 
     public void visit(LongLiteralMinValueExpr n, Object _) {
         output("(Lit (Int ");
         output(NumberUtils.createNumber(n.getValue()).toString());
         output("))");
     }
 
     public void visit(CharLiteralExpr n, Object _) {
         output("(Lit (Char '");
         output(n.getValue());
         output("'))");
     }
 
     public void visit(DoubleLiteralExpr n, Object _) {
         output("(Lit (Double ");
         output(NumberUtils.createNumber(n.getValue()).toString());
         output("))");
     }
 
     public void visit(BooleanLiteralExpr n, Object _) { genericVisit(n); }
 
     public void visit(NullLiteralExpr n, Object _) {
         output("(Lit Null)");
     }
 
     public void visit(MethodCallExpr n, Object _) {
         output("(MethodInv ");
         /* Some confusion over the meaning of the different kinds
            of method calls in language-java */
         if(n.getScope() == null) {
             output("(MethodCall (Name [");
             printIdent(n.getName());
             output("]) ");
             list(f("Args")).printValueFor(n);
             output(")");
         } else if(n.getScope() instanceof SuperExpr) {
             SuperExpr s = (SuperExpr)n.getScope();
             if(s.getClassExpr() == null) {
                 output("(SuperMethodCall ");
                 list(f("TypeArgs")).printValueFor(n);
                 output(" ");
                 printIdent(n.getName());
                 output(" ");
                 list(f("Args")).printValueFor(n);
                 output(")");
             } else {
                 output("(ClassMethodCall ");
                 printName(s.getClassExpr());
                 output(" ");
                 list(f("TypeArgs")).printValueFor(n);
                 output(" ");
                 printIdent(n.getName());
                 output(" ");
                 list(f("Args")).printValueFor(n);
                 output(")");
             }
         } else {
             output("(PrimaryMethodCall ");
             n.getScope().accept(this, null);
             output(" ");
             list(f("TypeArgs")).printValueFor(n);
             output(" ");
             printIdent(n.getName());
             output(" ");
             list(f("Args")).printValueFor(n);
             output(")");
         }
 
         output(")");
     }
 
     public void visit(NameExpr n, Object _) {
         output("(ExpName ");
         printName(n);
         output(")");
     }
 
     public void visit(ObjectCreationExpr n, Object _) {
         if(n.getScope() == null) {
             dispatchVisit(n, "_InstanceCreation");
         } else {
             dispatchVisit(n, "_QualInstanceCreation");
         }
     }
 
     public void visit(QualifiedNameExpr n, Object _) {
         output("(ExpName ");
         printName(n);
         output(")");
     }
 
     public void visit(ThisExpr n, Object _) {
         if(n.getClassExpr() == null) {
             output("This");
         } else {
             output("(ThisClass ");
             printName(n.getClassExpr());
             output(")");
         }
     }
 
     public void visit(SuperExpr n, Object _) { fail("SuperExpr"); }
 
     public void visit(UnaryExpr n, Object _) {
         switch(n.getOperator()) {
         case inverse:
             wrap("PreBitCompl", f("Expr")).printValueFor(n);
             break;
         case negative:
             wrap("PreMinus", f("Expr")).printValueFor(n);
             break;
         case not:
             wrap("PreNot", f("Expr")).printValueFor(n);
             break;
         case posDecrement:
             wrap("PostDecrement", f("Expr")).printValueFor(n);
             break;
         case posIncrement:
             wrap("PostIncrement", f("Expr")).printValueFor(n);
             break;
         case positive:
             wrap("PrePlus", f("Expr")).printValueFor(n);
             break;
         case preDecrement:
             wrap("PreDecrement", f("Expr")).printValueFor(n);
             break;
         case preIncrement:
             wrap("PreIncrement", f("Expr")).printValueFor(n);
             break;
         }
     }
 
     public void visit(VariableDeclarationExpr n, Object _) { genericVisit(n); }
 
 
     public void visit(MarkerAnnotationExpr n, Object _) { fail("MarkerAnnotationExpr"); }
 
     public void visit(SingleMemberAnnotationExpr n, Object _) { fail("SingleMemberAnnotationExpr"); }
 
     public void visit(NormalAnnotationExpr n, Object _) { fail("NormalAnnotationExpr"); }
 
     public void visit(MemberValuePair n, Object _) { fail("MemberValuePair"); }
 
     //- Statements ----------------------------------------
 
     public void visit(ExplicitConstructorInvocationStmt n, Object _) {
         if(n.isThis()) {
             dispatchVisit(n, "_ThisInvoke");
         } else {
             if(n.getExpr() == null) {
                 dispatchVisit(n, "_SuperInvoke");
             } else {
                 dispatchVisit(n, "_PrimarySuperInvoke");
             }
         }
     }
 
     public void visit(TypeDeclarationStmt n, Object _) {
         output("(LocalClass ");
         dispatchVisit(n.getTypeDeclaration(), "_ClassDeclaration");
         output(")");
     }
 
     public void visit(AssertStmt n, Object _) { genericVisit(n); }
 
     public void visit(BlockStmt n, Object _) { genericVisit(n); }
 
     public void visit(LabeledStmt n, Object _) { genericVisit(n); }
 
     public void visit(EmptyStmt n, Object _) {
         output("Empty");
     }
 
     public void visit(ExpressionStmt n, Object _) { genericVisit(n); }
 
     public void visit(SwitchStmt n, Object _) { genericVisit(n); }
 
     public void visit(SwitchEntryStmt n, Object _) { genericVisit(n); }
 
     public void visit(BreakStmt n, Object _) { genericVisit(n); }
 
     public void visit(ReturnStmt n, Object _) { genericVisit(n); }
 
     public void visit(IfStmt n, Object _) {
         if(n.getElseStmt() == null) {
             dispatchVisit(n, "_IfThen");
         } else {
             dispatchVisit(n, "_IfThenElse");
         }
     }
 
     public void visit(WhileStmt n, Object _) { genericVisit(n); }
 
     public void visit(ContinueStmt n, Object _) { genericVisit(n); }
 
     public void visit(DoStmt n, Object _) { genericVisit(n); }
 
     public void visit(ForeachStmt n, Object _) {
         output("(EnhancedFor ");
         printModifiers(n.getVariable().getModifiers());
         output(" ");
         printType(n.getVariable().getType());
         output(" ");
         printIdent(n.getVariable().getVars().get(0).getId().getName());
         output(" ");
         n.getIterable().accept(this, null);
         output(" ");
         printStmt(n.getBody());
         output(")");
     }
 
     public void visit(ForStmt n, Object _) { genericVisit(n); }
 
     public void visit(ThrowStmt n, Object _) { genericVisit(n); }
 
     public void visit(SynchronizedStmt n, Object _) { genericVisit(n); }
 
     public void visit(TryStmt n, Object _) { genericVisit(n); }
 
     public void visit(CatchClause n, Object _) { genericVisit(n); }
     
 }
