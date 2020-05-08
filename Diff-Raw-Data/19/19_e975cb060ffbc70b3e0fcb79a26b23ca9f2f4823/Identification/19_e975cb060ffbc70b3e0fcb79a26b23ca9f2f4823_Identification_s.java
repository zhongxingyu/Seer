 /*
  * File:    Identification.java
  * Author:  Rajesh Gopidi
  * PID:     720367703
  * Course : COMP520
  */
 
 package miniJava.ContextualAnalyzer;
 
 import miniJava.AbstractSyntaxTrees.*;
 import miniJava.SyntacticAnalyzer.SourcePosition;
 import miniJava.AbstractSyntaxTrees.Package;
 
 public class Identification implements Visitor<String,Object> {
 	
    private boolean secondWalk = false, debug = false;
     private SymbolTable table;
     private boolean isStaticMethod = false, mainMethodFound = false;
     private boolean userDefinedStringClass = false;
     private boolean userDefinedSystemClass = false;
     private String currentClass = null;
     private ErrorReporter reporter;
     private VarDecl lVariable = null;
     boolean mainClassfound = false;
     private MemberDecl out = null;
     StringBuilder str;
        
     public Identification() 
     {
         // we are going to use the same table for both walks as 
         // everything is added in the first walk will be removed
         str = new StringBuilder();
         table = new SymbolTable();
         reporter = new ErrorReporter();
         // adding the standard environment in the zero level
         table.newScope();
         addStandardImports();
 	table.newScope();
     }
 
     public void Identify (AST ast)
     {
         // need to add the standard environment code here
         ast.visit(this, null);
         if (table.retrieveLevel("String") != SymbolTable.PREDEFINEDLEVEL) {
             userDefinedStringClass = true;
         }
         if (table.retrieveLevel("System") != SymbolTable.PREDEFINEDLEVEL) {
             userDefinedSystemClass = true;
             System.out.println("user defined system class");
         }
         secondWalk = true;
         ast.visit(this,null);
         // done with this check
         if (!mainMethodFound) {
             if (userDefinedStringClass)
                 reporter.reportError("public static void main (String[] args) method found, but" + 
                                      "parameter is not of default String type", "", ast.posn);
             else 
                 reporter.reportError("No valid definition of public static void main (String[] args) found ",
                                      "", ast.posn);
         }
         if (reporter.errorCount > 0)
             System.exit(4);
         
         
     }   
     
     // Package
     public Object visitPackage(Package prog, String arg) {
     
         ClassDeclList cl = prog.classDeclList;
         for (ClassDecl c: prog.classDeclList){
             c.visit(this, null);
         }
         
         if (secondWalk)
         {
             prog.classDeclList.add((ClassDecl)table.retrievePDefClassDecl("String"));
             prog.classDeclList.add((ClassDecl)table.retrievePDefClassDecl("System"));
             prog.classDeclList.add((ClassDecl)table.retrievePDefClassDecl("_PrintStream"));
         }
         return null;
     }
     
   // Declarations
     public Object visitClassDecl(ClassDecl clas, String arg)
     {
         int fieldOffset = 0;
         int staticFieldOffset = 2;
         int methodOffset = 1;
 
         if (debug)
             System.out.println(" In class decl " + clas.name);
         if (!secondWalk) {
             if (table.add(clas.name, clas)) {
                 reporter.reportError("class " + clas.name+" is already defined",
                                      "", clas.posn);        
             }
         }
 
         if (secondWalk) {  
             // parent scope is being set for the case where we have a block inside in the class
             table.newScope(true); 
             
             for (FieldDecl f: clas.fieldDeclList) 
             {
                 if (f.isStatic) {
                     clas.noOfStaticFields++;
                 } else { 
                     clas.noOfFields++;
                     f.storage.offset = fieldOffset;
                     fieldOffset++;
                 }
                 f.visit(this, null);
             }
 
             for (MethodDecl m: clas.methodDeclList) {
                 if ((table.retrieveMemberDecl(m.name) != null) ||
                     (table.retrieveMemberDecl(m.name+"@function") != null)) {
                     reporter.reportError(m.name+" is already defined in the class", "", m.posn);
                 }  else if (table.add(m.name+"@function", m)) { 
                         reporter.reportError("Method" +m.name+" wth parameters: " +
                                              m.parameterDeclList + " is already defined in the class", 
                                              "", m.posn);
                 }
                 m.storage.offset = methodOffset++; 
             }
 
             currentClass = clas.name;
             for (MethodDecl m: clas.methodDeclList) {
                 isStaticMethod = false;
         	m.visit(this, null);
             }
 
             if (mainMethodFound && !mainClassfound) {
                 clas.containsMain = true;
                 mainClassfound =  true;
             }
 
             table.closeScope(true);
         }
         return null;
     }
 
     /*
     public String pdListToString (ParameterDeclList pdl)
     {
         StringBuilder str = new StringBuilder();
         for (ParameterDecl pd: pdl) {
             str.append(pd.type.toString());
             str.append(",");
         }
         str.delete(str.length()-1, str.length());
         return (str.toString());
     }
     
 
     public String mangleRoutineName (MethodDecl m) 
     {
         String name = null;
         str.append(m.name);
         str.append("function");
         ParameterDeclList pdl = m.parameterDeclList;
         for (ParameterDecl pd: pdl) {
             str.append('@');
             str.append(pd.type.toString());
         }
         name = str.toString();
         if (debug)
             System.out.println("mangled name: " + name);
         str.delete(0, str.length());
 
         return(name);
     }*/
     
     public Object visitFieldDecl(FieldDecl f, String arg)
     {
        
         f.type.visit(this, null); 
         if (table.add(f.name, f)) {
             reporter.reportError(f.name + " is already defined in class ", "", f.posn);
         }
         return null;
     }
 
     public void visitMainMethodDecl(MethodDecl m)
     {
         ParameterDeclList pdl = m.parameterDeclList;
         ParameterDecl pd = null;
 
         if (!m.isPublic) {
             return;
         }
 
         if (!m.isStatic) {
             return;
         }
 
         if (m.type.typeKind != TypeKind.VOID) {
 	    return;
         }
         
         if (pdl.size() > 0)
             pd = pdl.get(0);
 
         if ((pdl.size() > 1) || (pdl.size() < 1)) {
             return;
         } else if (pd.type.typeKind != TypeKind.ARRAY) {
             return;
         } else if (((ArrayType) pd.type).eltType.typeKind != TypeKind.CLASS) {
             return;
         } else if (!(((ClassType)((ArrayType) pd.type).eltType).className.equals("String"))) {
             return;
         } else if (userDefinedStringClass && !mainMethodFound) {
                 reporter.reportError("public static void main (String[] args) method found, but" +
                                      "parameter is not of default String type", "", m.posn);
         }
 	
         if (mainMethodFound) {
             reporter.reportError("More than one public static void main (String[] args) method found in the program",
 				 "", m.posn);
         }
 
         // Initializing the System.out variable
         if (!userDefinedSystemClass) {
             SourcePosition pos = new SourcePosition();
             StaticRef ref = new StaticRef((ClassDecl) table.retrieveClassDecl("System"),
                                         out, pos); 
             ClassType type = new ClassType("_PrintStream", pos);
             type.classDecl = (ClassDecl) table.retrieveClassDecl("_PrintStream");
             Expression expr = new NewObjectExpr(type, pos);           
             Statement stmt =  new AssignStmt(ref, expr, pos);
             m.statementList.add(0, stmt);
         } 
 	mainMethodFound = true;
         
     }
     
     public Object visitMethodDecl(MethodDecl m, String arg)
     {
         table.newScope(true); 
         isStaticMethod = m.isStatic;
 
         /*
         if (m.name.equals("main")) {
            visitMainMethodDecl(m);
         } 
         */
         // checking for return type;       
         m.type.visit(this,null); 
         
         ParameterDeclList pdl = m.parameterDeclList;
 	//int length = pdl.size();
         for (ParameterDecl pd: pdl) {
             pd.visit(this, null);
 	    //pd.storage.offset = -length;
 	    //length--;
         }   
 
         StatementList sl = m.statementList;
 
         for (Statement s: sl) {
             s.visit(this, null);
         }
         if (m.returnExp != null) {
             m.returnExp.visit(this, null);
         }
         table.closeScope(true);
     
         if (m.name.equals("main")) {
            visitMainMethodDecl(m);
         }
         return null;
     }
     
     public Object visitParameterDecl(ParameterDecl pd, String arg) 
     {
         pd.type.visit(this, null); 
         if (table.add(pd.name, pd))  {
             reporter.reportError("Variable " +pd.name+" is already defined","", pd.posn);
         }
         return null;
     } 
     
     public Object visitVarDecl(VarDecl vd, String arg)
     {
         vd.type.visit(this, null);
         if (table.add(vd.name, vd)) {
             reporter.reportError(vd.name+" is already defined in the method ", 
                                  "", vd.posn);
         }
         lVariable = vd;
         return null;
     }
  
     // Statements
     public Object visitBlockStmt(BlockStmt stmt, String arg)
     {
         StatementList sl = stmt.sl;
         table.newScope();
         for (Statement s: sl) {
         	s.visit(this, null);
         }
         table.closeScope();
         return null;
     }
     
     public Object visitVardeclStmt(VarDeclStmt stmt, String arg)
     {
         stmt.varDecl.visit(this, null);    
         if (stmt.initExp != null)
             stmt.initExp.visit(this, null);
         lVariable = null;
         return null;
     }
     
     public Object visitAssignStmt(AssignStmt stmt, String arg){
         stmt.ref = (Reference) stmt.ref.visit(this, "false");
         stmt.val.visit(this, null);
         return null;
     }
     
     public Object visitCallStmt(CallStmt stmt, String arg){
         stmt.methodRef = (Reference) stmt.methodRef.visit(this, "true");
         ExprList al = stmt.argList;
         for (Expression e: al) {
             e.visit(this, null);
         }
         return null;
     }
     
     public Object visitIfStmt(IfStmt stmt, String arg){
         stmt.cond.visit(this, null);
         stmt.thenStmt.visit(this, null);
         if (stmt.elseStmt != null)
             stmt.elseStmt.visit(this, null);
         return null;
     }
     
     public Object visitWhileStmt(WhileStmt stmt, String arg){
         stmt.cond.visit(this, null);
         stmt.body.visit(this, null);
         return null;
     }
     
     
   // Expressions
     public Object visitUnaryExpr(UnaryExpr expr, String arg){
         expr.operator.visit(this, null);
         expr.expr.visit(this, null);
         return null;
     }
     
     public Object visitBinaryExpr(BinaryExpr expr, String arg){
         expr.operator.visit(this, null);
         expr.left.visit(this, null);
         expr.right.visit(this, null);
         return null;
     }
     
     public Object visitRefExpr(RefExpr expr, String arg){
         expr.ref = (Reference) expr.ref.visit(this, "false");
         return null;
     }
     
     public Object visitCallExpr(CallExpr expr, String arg){
         expr.functionRef = (Reference) expr.functionRef.visit(this, "true");
         ExprList al = expr.argList;
         for (Expression e: al) {
             e.visit(this, null);
         }
         return null;
     }
     
     public Object visitLiteralExpr(LiteralExpr expr, String arg){
         expr.literal.visit(this, null);
         return null;
     }
  
     public Object visitNewArrayExpr(NewArrayExpr expr, String arg){
         expr.eltType.visit(this, null);
         expr.sizeExpr.visit(this, null);
         return null;
     }
     
     public Object visitNewObjectExpr(NewObjectExpr expr, String arg){
         expr.classtype.visit(this, null);
         return null;
     }
 
     //Types
 
     public Object visitBaseType(BaseType type, String arg) {
         //show(arg, type.typeKind + " " + type.toString());
         return null;
     }
 
     public Object visitClassType(ClassType type, String arg)
     {
         Declaration decl = null;
         if ((decl = table.retrieveClassDecl(type.className)) == null) {
             reporter.reportError("Cannot find symbol", type.className, type.posn);
         }
         type.classDecl = (ClassDecl) decl;
         return decl;
     }
 
     public Object visitArrayType(ArrayType type, String arg){
         return (type.eltType.visit(this, null));
     }    
    
   // References
   // store the type in reference class
     
     public Object visitQualifiedRef(QualifiedRef qr, String isMCall)
     {
         Reference ref = null;
         Declaration decl = null, classDecl = null;        
         int level = -1;
         Identifier id = null;
         String name = null, className = null;
         boolean isMethodCall = false, isStatic = false, isPrivate = false;
         boolean isLocalVar = false, shouldBStatic = false, skipLookup = false;
         boolean isLengthRef = false, isOutVar = false;
         boolean accessThStatic = false, isSystemClass = false;
         IdentifierList ql = qr.qualifierList;
     
         if ((isMCall != null) && isMCall.equals("true"))
             isMethodCall = true;
 
         if (qr.thisRelative && isStaticMethod) {
             reporter.reportError("this cannot be referenced " + 
                                  "from a static method", " ", qr.posn);
             return (qr);
         } else if (qr.thisRelative) {
             if (isMethodCall && ql.size() < 1) {
                 reporter.reportError("cannot find symbol - method-", "this", qr.posn);
                 return (qr);
             }
             decl = table.retrieve(currentClass);
             ref = new ThisRef((ClassDecl) decl, qr.posn);
         }
 
         if (ql.size() > 0) {
             id = ql.get(0);
             if ((ql.size() == 1) && isMethodCall) {
                 if (debug)
                     System.out.println("doing method search");
                 name = id.spelling + "@function";
             } else {
                 name = id.spelling;
             }
 
             if ((decl = table.retrieve(name)) == null) {
                 reporter.reportError("cannot find symbol -", id.spelling, id.posn);
                 return (qr);
             }
             level = table.retrieveLevel(name);
             if  (debug) {
                 System.out.println("level = " + level + " of id = "+ id.spelling );
             }
             if (ref == null) {   
                 if (level > SymbolTable.MEMBERLEVEL) {
                     // int x = x + y --> blocks these kind of stmts
                     if (decl == lVariable) {
                         reporter.reportError("usage of uninitialized variable-'" + id.spelling +
                                 "'", " ", id.posn);
                         return (qr);
                     }
                     ref = new LocalRef((LocalDecl) decl, qr.posn);
                     classDecl = retrieveClassDecl(decl);
                 } else if (level == SymbolTable.MEMBERLEVEL) {
                     if (isStaticMethod && !(((MemberDecl)decl).isStatic)) {
                         reporter.reportError("non static variable ' " + id.spelling + 
                                 " ' cannot be referenced from a static context ", 
                                 " ", id.posn);
                         return (qr);
                     } 
                     if ((((MemberDecl)decl).isStatic)) {
                         ref = new StaticRef((ClassDecl) table.retrieve(currentClass),
                                (MemberDecl) decl, qr.posn);
                     } else {
                         ref = new MemberRef((MemberDecl) decl, qr.posn);
                     }
                     classDecl = retrieveClassDecl(decl);
                 } else if (((level == SymbolTable.CLASSLEVEL) || 
                             (level == SymbolTable.PREDEFINEDLEVEL)) && 
                             (ql.size() != 1)) {
                     ref = new StaticRef((ClassDecl) decl, qr.posn);
                     classDecl = decl;
                     shouldBStatic = true;
                 } else {
                     reporter.reportError("cannot find symbol - variable ", id.spelling, id.posn);
                     return (qr);
                 }
             } else {
                 if ((decl = table.retrieveMemberDecl(name)) == null) {
                     reporter.reportError("cannot find symbol -", id.spelling, id.posn);
                     return (qr);
                 }
                 if (((MemberDecl)decl).isStatic) {
                     ref = new StaticRef((ClassDecl) table.retrieve(currentClass),
                                         (MemberDecl)decl, qr.posn);
                 } else {
                     ((ThisRef)ref).setMemberDecl((MemberDecl)decl);
                 }
                 classDecl = retrieveClassDecl(decl);
             }      
 
             for (int i = 1; i < ql.size(); i++) {
                 id = ql.get(i);
                 name = id.spelling;
 
                 // check for the length field of arrays
                 if ((i  == (ql.size() - 1)) && 
                       !shouldBStatic        &&  // to block isArrayType() call on class decls 
                       name.equals("length") &&
                       isArrayType(decl)) {
                     ref = new ArrayLengthRef(ref, id.posn);
                 } else {
                     if ((classDecl == null)) { 
                         reporter.reportError("unable to derefence variable-", id.spelling, id.posn);
                         return (qr);
                     }
                     className = classDecl.name;
                     // Method name is passed as an argument
 
                     if ((i  == (ql.size() - 1)) && isMethodCall) {
                         decl = retrieveMethodDeclaration(classDecl, name);
                         if (debug)
                             System.out.println("Found method declaration for "+name);
                     } else { 
                         decl = retrieveFieldDeclaration(classDecl, name);
                         if (debug)
                             System.out.println("Found field declaration for "+name);
                     } 
                     if (decl != null) {
                         isStatic = ((MemberDecl) decl).isStatic;
                         isPrivate = ((MemberDecl) decl).isPrivate;
 
                         if (isPrivate && !className.equals(currentClass)) {
                             reporter.reportError(id.spelling + " has private access in ",
                                     className, id.posn);
                             return (qr);
                         }
                         if ((shouldBStatic) && (i == 1) && !isStatic) {
                             reporter.reportError("non static variable ' " + id.spelling +
                                     "' :cannot be referenced from a static context ",
                                     " ", id.posn);
                             return (qr);
                         } else if ((shouldBStatic) && (i == 1)) {
                             ((StaticRef)ref).decl = (MemberDecl)decl;
                             shouldBStatic = false;
                         } else { 
                             if (isStatic) {
                                 ref = new StaticRef((ClassDecl) classDecl ,
                                         (MemberDecl) decl, id.posn);
                             } else {
                                 ref = new DeRef(ref, (MemberDecl)decl, id.posn);
                             }
                         }
                         classDecl = retrieveClassDecl(decl);
                     } else {
                         reporter.reportError("cannot find symbol - variable- " +
                                 id.spelling +" in class: " + className,
                                 "", id.posn);
                         return (qr);
                     }    
                 }
             }
         }
         return ref;
     }
 
     public boolean isArrayType(Declaration decl) 
     {
         if (decl.type.typeKind == TypeKind.ARRAY) 
             return (true);       
 
         return (false);
     }
 
     public ClassDecl retrieveClassDecl (Declaration decl)
     {
         return ((ClassDecl)decl.type.visit(this, null));
     }
 
 
     // Retrieving the declaration
 
     public Declaration retrieveFieldDeclaration (Declaration decl, String key)
     {
         ClassDecl clas = (ClassDecl) decl;
 
         for (FieldDecl f: clas.fieldDeclList) {
             if (key.equals(f.name)) {
                 return (f);
             }
         }
         return null;    
     }
 
     public Declaration retrieveMethodDeclaration (Declaration decl, String key)
     {
         ClassDecl clas = (ClassDecl) decl;
 
         for (MethodDecl m: clas.methodDeclList) {
             if (key.equals(m.name)) {
                 return (m);
             }
         }
         return null;
     }
 
     public Object  visitArrayLengthRef(ArrayLengthRef ref, String arg)
     {
         return null;
     }    
 
     public Object visitIndexedRef(IndexedRef ir, String arg) {
         //show(arg, ir);
         ir.ref = (Reference) ir.ref.visit(this, null);
         ir.indexExpr.visit(this, null);
         return ir;
     }
 
     // Terminals
     public Object visitIdentifier(Identifier id, String arg){
         //show(arg, "\"" + id.spelling + "\" " + id.toString());
         return null;
     }
 
     public Object visitOperator(Operator op, String arg){
         //show(arg, "\"" + op.spelling + "\" " + op.toString());
         return null;
     }
 
     public Object visitIntLiteral(IntLiteral num, String arg){
         //show(arg, "\"" + num.spelling + "\" " + num.toString());
         return null;
     }
 
     public Object visitBooleanLiteral(BooleanLiteral bool, String arg){
         //show(arg, "\"" + bool.spelling + "\" " + bool.toString());
         return null;
     }
 
     public Object visitNullLiteral(NullLiteral Null, String arg){
         //show(arg, "\"" + bool.spelling + "\" " + bool.toString());
         return null;
     }
 
 
     public Object visitLocalRef(LocalRef ref, String arg) 
     {
         return null;
     }
 
     public Object visitMemberRef(MemberRef ref, String arg)
     {
         return null;
     }
 
     public Object visitStaticRef(StaticRef ref, String arg)
     {
         return null;
     }
 
     public Object visitThisRef(ThisRef ref, String arg)
     {
         return null;
     }
 
     public Object visitDeRef(DeRef ref, String arg)
     {
         return null;
     }
 
     public ClassDecl addStringClassDef()
     {
         ClassDecl classDecl = null;
         FieldDeclList fieldDeclList = new FieldDeclList();
         MethodDeclList methodDeclList = new MethodDeclList();
         SourcePosition pos = new SourcePosition();
         classDecl = new ClassDecl("String", fieldDeclList, methodDeclList, pos);
         return classDecl;
     }
 
     public ClassDecl addPrintStreamClassDef()
     {
         ParameterDecl paraDecl = null;
         ClassDecl classDecl = null;
         FieldDeclList fieldDeclList = new FieldDeclList();
         MethodDeclList methodDeclList = new MethodDeclList();
         SourcePosition pos = new SourcePosition();
 
         Type typ = new BaseType(TypeKind.VOID, pos);
         Type typarg = new BaseType(TypeKind.INT, pos);
         FieldDecl fieldDecl = new FieldDecl(false, false, typ, "println", pos);
         fieldDecl.isPublic = true;
         //fieldDecl.storage.size = 0;
         paraDecl = new ParameterDecl(typarg, "n", pos);
         ParameterDeclList paraList = new ParameterDeclList();
         paraList.add(paraDecl);
         MethodDecl decl = new MethodDecl(fieldDecl, paraList, new StatementList(), null, pos);
         decl.storage.size = 0;
         methodDeclList.add(decl);
         classDecl = new ClassDecl("_PrintStream", fieldDeclList, methodDeclList, pos);
         return classDecl;
     }
 
     public ClassDecl addSystemClassDef(ClassDecl printStreamClass)
     {
         ClassDecl classDecl = null;
         FieldDeclList fieldDeclList = new FieldDeclList();
         MethodDeclList methodDeclList = new MethodDeclList();
         SourcePosition pos = new SourcePosition();
         ClassType typ = new ClassType("_PrintStream", pos);
         typ.classDecl = printStreamClass;
         FieldDecl fieldDecl = new FieldDecl(false, true, typ, "out", pos);
         fieldDecl.isPublic = true;
         out = fieldDecl;
         fieldDeclList.add(fieldDecl);
         classDecl = new ClassDecl("System", fieldDeclList, methodDeclList, pos);
         return classDecl;
     }
 
     public void addStandardImports()
     {
         ClassDecl printStreamClass = addPrintStreamClassDef();
         table.add("_PrintStream", printStreamClass);
 
         ClassDecl systemClass = addSystemClassDef(printStreamClass);
         table.add("System", systemClass);
         ClassDecl stringClass = addStringClassDef();
         table.add("String", stringClass);
 
     }
 
 }
