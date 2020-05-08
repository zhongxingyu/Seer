 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 /*
 SNARL/Compiler Compiles Snarl code into MIPS assembly.
 
 James Current
 1/30/12
 */
 public class Compiler extends Common{
     protected Scanner scanner;                      //Compiler pulls tokens from Scanner
     protected Source source;                        //Used to make 2nd pass of input stream
     protected long comparisonOperators = makeSet(   //Set of comparison operators
             equalToken,
             greaterToken,
             lessToken,
             lessEqualToken,
             greaterEqualToken,
             lessGreaterToken);
     protected long sumOperators = makeSet(          //Set of sum operators
             plusToken,
             dashToken);
     protected long productOperators = makeSet(      //Set of product operators
             starToken,
             slashToken);
     protected long declarationTokens = makeSet(     //Set of tokens that can start a declaration
             boldIntToken,
             boldStringToken,
             openBracketToken);
     protected SymbolTable table;                    //Used to store names with descriptors
     protected BasicType intType;                    //Basic Int Type
     protected BasicType stringType;                 //Basic String Type
     protected Type procValType;                     //Type of the current procedure's return value
     protected Allocator allocator;                  //Used to allocate registers
     protected Assembler assembler;                  //Used to write assembly code to output file
     protected Global global;                        //Used to handle global variables
     protected String opToString[];                  //Used to turn a token into a mips operation
 
     //  GLOBAL ARRAY DESCRIPTOR. Describe a global array.
 
     private class GlobalArrayDescriptor extends GlobalDescriptor
     {
 
 //  Constructor.
 
         private GlobalArrayDescriptor(Type type, Label label)
         {
             this.type = type;
             this.label = label;
         }
 
 //  LVALUE. An array can't be alone on the left side of an assignment.
 
         protected Allocator.Register lvalue()
         {
             throw new SnarlCompilerException("Cannot assign an array.");
         }
 
 //  RVALUE. Return a register that holds the address of the global array.
 
         protected Allocator.Register rvalue()
         {
             Allocator.Register register = allocator.request();
             assembler.emit("la", register, label);
             return register;
         }
 
 //  TO STRING. For debugging.
 
         public String toString()
         {
             return "[GlobalArrayDescriptor " + type + " " + label + "]";
         }
     }
 
 //  GLOBAL PROCEDURE DESCRIPTOR. Describe a procedure.
 
     private class GlobalProcedureDescriptor extends GlobalDescriptor
     {
 
 //  Constructor.
 
         private GlobalProcedureDescriptor(Type type, Label label)
         {
             this.type = type;
             this.label = label;
         }
 
 //  LVALUE. A procedure can't be alone on the left side of an assignment.
 
         protected Allocator.Register lvalue()
         {
             throw new SnarlCompilerException("Illegal Procedure Call.");
         }
 
 //  RVALUE. A procedure can't be used as a variable name either.
 
         protected Allocator.Register rvalue()
         {
             throw new SnarlCompilerException("Illegal Procedure Call.");
         }
 
 //  TO STRING. For debugging.
 
         public String toString()
         {
             return "[GlobalProcedureDescriptor " + type + " " + label + "]";
         }
     }
 
 //  GLOBAL VARIABLE DESCRIPTOR. Describe a global variable that's not an array.
 
     private class GlobalVariableDescriptor extends GlobalDescriptor
     {
 
 //  Constructor.
 
         private GlobalVariableDescriptor(Type type, Label label)
         {
             this.type = type;
             this.label = label;
         }
 
 //  LVALUE. Return a register that holds the global variable's address.
 
         protected Allocator.Register lvalue()
         {
             Allocator.Register register = allocator.request();
             assembler.emit("la", register, label);
             return register;
         }
 
 //  RVALUE. Return a register that holds the global variable's value.
 
         protected Allocator.Register rvalue()
         {
             Allocator.Register register = allocator.request();
             assembler.emit("la", register, label);
             assembler.emit("lw", register, 0, register);
             return register;
         }
 
 //  TO STRING. For debugging.
 
         public String toString()
         {
             return "[GlobalVariableDescriptor " + type + " " + label + "]";
         }
     }
 
 //  LOCAL ARRAY DESCRIPTOR. Describe a local array variable.
 
     private class LocalArrayDescriptor extends LocalDescriptor
     {
 
 //  Constructor.
 
         private LocalArrayDescriptor(Type type, int offset)
         {
             this.type = type;
             this.offset = offset;
         }
 
 //  LVALUE. An array can't be alone on the left side of an assignment.
 
         protected Allocator.Register lvalue()
         {
             throw new SnarlCompilerException("Cannot assign an array.");
         }
 
 //  RVALUE. Return a register that holds the address of a local array variable.
 
         protected Allocator.Register rvalue()
         {
             Allocator.Register register = allocator.request();
             assembler.emit("addi", register, allocator.fp, offset);
             return register;
         }
 
 //  TO STRING. For debugging.
 
         public String toString()
         {
             return "[LocalArrayDescriptor " + type + " " + offset + "]";
         }
     }
 
 //  LOCAL VARIABLE DESCRIPTOR. Describe a local variable that's not an array.
 
     private class LocalVariableDescriptor extends LocalDescriptor
     {
 
 //  Constructor.
 
         private LocalVariableDescriptor(Type type, int offset)
         {
             this.type = type;
             this.offset = offset;
         }
 
 //  LVALUE. Return a register that holds the address of the local variable.
 
         protected Allocator.Register lvalue()
         {
             Allocator.Register register = allocator.request();
             assembler.emit("addi", register, allocator.fp, offset);
             return register;
         }
 
 //  RVALUE. Return a register that holds the value of the local variable.
 
         protected Allocator.Register rvalue()
         {
             Allocator.Register register = allocator.request();
             assembler.emit("lw", register, offset, allocator.fp);
             return register;
         }
 
 //  TO STRING. For debugging.
 
         public String toString()
         {
             return "[LocalVariableDescriptor " + type + " " + offset + "]";
         }
     }
 
     //Constructor. Returns a new Compiler positioned at the first unignored token, uses a default output filename.
     
     public Compiler(Source source){
         scanner = new Scanner(source);
         this.source = source;
         table = new SymbolTable();
         table.push();
         intType = new BasicType("int",Type.wordSize,null);
         stringType = new BasicType("string",Type.addressSize,null);
         allocator = new Allocator();
         assembler = new Assembler("out.asm");
         global = new Global(assembler);
         opToString = new String[maxToken];
         opToString[lessToken] = "slt";
         opToString[lessEqualToken] = "sle";
         opToString[lessGreaterToken] = "sne";
         opToString[equalToken] = "seq";
         opToString[greaterToken] = "sgt";
         opToString[greaterEqualToken] = "sge";
         opToString[plusToken] = "add";
         opToString[dashToken] = "sub";
         opToString[starToken] = "mul";
         opToString[slashToken] = "div";
     }
     
     //Constructor. Returns a new Compiler positioned at the first unignored token using the specified output filename.
 
     public Compiler(Source source, String out){
         scanner = new Scanner(source);
         this.source = source;
         table = new SymbolTable();
         table.push();
         intType = new BasicType("int",Type.wordSize,null);
         stringType = new BasicType("string",Type.addressSize,null);
         allocator = new Allocator();
         assembler = new Assembler(out);
         global = new Global(assembler);
         opToString = new String[maxToken];
         opToString[plusToken] = "add";
         opToString[dashToken] = "sub";
     }
     
    /* protected void errorRecoveryExample(){
         if(!isInSet(scanner.getToken(), declarationTokens)){
             source.recoverableError("message");
             while (! isInSet(scanner.getToken(), declarationFollowSet)){
                 source.nextChar();
             }
         }
     }
      */
 
     //PassOne. Runs pass one of the parser, parsing procedure names and signatures only.
 
     public void passOne(){
         enter("pass one");
         while(scanner.getToken() != endFileToken){
             if(scanner.getToken() == boldProcToken){
                 scanner.nextToken();
                 ProcedureType procedure = new ProcedureType();
                 Label label = new Label("proc");
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 nextExpected(openParenToken);
                 while(scanner.getToken() != closeParenToken){
                     switch (scanner.getToken()){
                         case openBracketToken: {
                             scanner.nextToken();
                             ArrayType array = new ArrayType(scanner.getInt(), intType);
                             nextExpected(intConstantToken);
                             nextExpected(closeBracketToken);
                             nextExpected(boldIntToken);
                             procedure.addParameter(array);
                             break;
                         }
                         case boldIntToken: {
                             scanner.nextToken();
                             procedure.addParameter(intType);
                             break;
                         }
                         case boldStringToken: {
                             scanner.nextToken();
                             procedure.addParameter(stringType);
                             break;
                         }
                         default: {
                             nextExpected(
                                     openBracketToken,
                                     boldIntToken,
                                     boldStringToken
                             );
                         }
                     }
                     nextExpected(nameToken);
                 }
                 nextExpected(closeParenToken);
                 switch (scanner.getToken()){
                     case boldIntToken: {
                         procedure.addValue(intType);
                         break;
                     }
                     case boldStringToken: {
                         procedure.addValue(stringType);
                         break;
                     }
                     default: {
                         nextExpected(
                                 boldIntToken,
                                 boldStringToken
                         );
                     }
                 }
                 table.setDescriptor(name,new GlobalProcedureDescriptor(procedure, label));
             }
             scanner.nextToken();
         }
         source.reset();
         scanner.nextToken();
         exit("pass one");
         passTwo();
     }
     
     //PassTwo. Runs pass two of the parser, parsing the bulk of the input.
     
     protected void passTwo(){
         enter("pass two");
         assembler.emit(".text");
         assembler.emit(".globl main");
         assembler.emit("jal main");
         assembler.emit("li", allocator.v0, 10);
         assembler.emit("syscall");
         nextProgram();
         global.emit();
         assembler.close();
         exit("pass two");
     }
 
     //TypeCheck. Tests if descriptor is of type type.
 
     protected void typeCheck(Descriptor descriptor, Type type){
         if(!descriptor.getType().isSubtype(type)){
             throw new SnarlCompilerException("Expression of type " + type + " expected.");
         }
     }
 
     //MakeSet. Returns a set of the elements.
 
     protected long makeSet(int... elements){
         long set = 0L;
         for(int element : elements){           
             set |= (1L << element);
         }
         return set;
     }
     
     //IsInSet. Tests if element is in set.
 
     protected boolean isInSet(int element, long set){
         return (set & (1L << element)) != 0L;
     }
 
     //NextProgram. Parses the next program.
 
     protected void nextProgram(){
         enter("program");
         nextProgramPart();
         while (scanner.getToken() == semicolonToken){
             scanner.nextToken();
             nextProgramPart();
         }
         nextExpected(endFileToken);
         exit("program");
     }
 
     //NextProgramPart. Parses the next program part.
 
     protected void nextProgramPart(){
         enter("program part");
         if(isInSet(scanner.getToken(), declarationTokens)){
             nextGlobalDeclaration();
         } else if(scanner.getToken() == boldProcToken){
             nextProcedure();
         } else {
             nextExpected(boldProcToken,boldIntToken,boldStringToken,openBracketToken);
         }
         exit("program part");
     }
     
     //NextGlobalDeclaration. Parses the next global declaration.
     
     protected void nextGlobalDeclaration(){
         enter("global declaration");
         switch (scanner.getToken()){
             case boldIntToken: {
                 scanner.nextToken();
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 Label label = global.enterVariable(intType);
                 table.setDescriptor(name, new GlobalVariableDescriptor(intType, label));
                 break;
             }
             case boldStringToken: {
                 scanner.nextToken();
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 Label label = global.enterVariable(stringType);
                 table.setDescriptor(name, new GlobalVariableDescriptor(stringType, label));
                 break;
             }
             case openBracketToken: {
                 scanner.nextToken();
                 ArrayType type = new ArrayType(scanner.getInt(), intType);
                 nextExpected(intConstantToken);
                 nextExpected(closeBracketToken);
                 nextExpected(boldIntToken);
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 Label label = global.enterVariable(type);
                 table.setDescriptor(name, new GlobalArrayDescriptor(type, label));
                 break;
             }
             default: {
                 nextExpected(
                         boldIntToken,
                         boldStringToken,
                         openBracketToken
                 );
             }
         }
         exit("global declaration");
     }
 
     //NextParameterDeclaration. Parses the next parameter declaration.
 
     protected void nextParameterDeclaration(int index){
         enter("parameter declaration");
         switch (scanner.getToken()){
             case boldIntToken: {
                 scanner.nextToken();
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 table.setDescriptor(name, new LocalVariableDescriptor(intType, -index * Type.wordSize));
                 break;
             }
             case boldStringToken: {
                 scanner.nextToken();
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 table.setDescriptor(name, new LocalVariableDescriptor(stringType, -index * Type.wordSize));
                 break;
             }
             case openBracketToken: {
                 scanner.nextToken();
                 ArrayType type = new ArrayType(scanner.getInt(), intType);
                 nextExpected(intConstantToken);
                 nextExpected(closeBracketToken);
                 nextExpected(boldIntToken);
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 table.setDescriptor(name, new LocalVariableDescriptor(type, -index * Type.wordSize));
                 break;
             }
             default: {
                 nextExpected(
                         boldIntToken,
                         boldStringToken,
                         openBracketToken
                 );
             }
         }
         exit("parameter declaration");
     }
 
     //NextLocalDeclaration. Parses the next local declaration.
     
     protected int nextLocalDeclaration(int offset){          
         enter("declaration");
         
         int size;
         
         switch (scanner.getToken()){
             case openBracketToken: {
                 ArrayType type;
                 scanner.nextToken();
                 type = new ArrayType(scanner.getInt(), intType);
                 nextExpected(intConstantToken);
                 nextExpected(closeBracketToken);
                 nextExpected(boldIntToken);
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 table.setDescriptor(name, new LocalArrayDescriptor(type, offset));
                 size = type.getSize();
                 break;
             }
             case boldIntToken: {
                 scanner.nextToken();
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 table.setDescriptor(name,new LocalVariableDescriptor(intType, offset));
                 size = Type.wordSize;
                 break;
             }
             case boldStringToken: {
                 scanner.nextToken();
                 String name = scanner.getString();
                 nextExpected(nameToken);
                 table.setDescriptor(name, new LocalVariableDescriptor(stringType, offset));
                 size = Type.addressSize;
                 break;
             }
             default: {
                 nextExpected(boldIntToken, boldStringToken, openBracketToken);
                 size = 0;
             }
         }
 
         exit("declaration");
         
         return size;
     }
 
     //NextProcedure. Parses the next procedure.
 
     protected void nextProcedure(){
         enter("procedure");
         nextExpected(boldProcToken);
         
         String name = scanner.getString();
         if(name.equals("main")){
             assembler.emit("main:");
         }
         
         nextExpected(nameToken);
 
         table.push();
         nextParameters();
 
         switch (scanner.getToken()){
             case boldIntToken: {
                 scanner.nextToken();
                 procValType = intType;
                 break;
             }
             case boldStringToken: {
                 scanner.nextToken();
                 procValType = stringType;
                 break;
             }
             default: {
                 nextExpected(boldIntToken, boldStringToken);
             }
         }
         nextExpected(colonToken);
         
         GlobalProcedureDescriptor descriptor = (GlobalProcedureDescriptor) table.getDescriptor(name);
 
         nextBody(descriptor);
 
         table.pop();
 
         exit("procedure");
     }
 
     //NextParameters. Parses the next parameters.
 
     protected void nextParameters(){
         enter("parameters");
 
         nextExpected(openParenToken);
         
         int index = 0;
 
         if (scanner.getToken() != closeParenToken) {
             nextParameterDeclaration(index);
             ++index;
 
             while(scanner.getToken() == commaToken){
                 scanner.nextToken();
                 nextParameterDeclaration(index);
                 ++index;
             }
         }
 
         nextExpected(closeParenToken);
 
         exit("parameters");
     }
 
     //NextBody. Parses the next body.
 
     protected void nextBody(GlobalProcedureDescriptor descriptor){
         enter("body");
         
         int local = 0;
         Label label = descriptor.getLabel();
         int arity = ((ProcedureType) descriptor.getType()).getArity();
         
         if(scanner.getToken() != boldBeginToken){
             
             local += nextLocalDeclaration(-(local + arity * Type.addressSize));
 
             while(scanner.getToken() == semicolonToken){
                 scanner.nextToken();
                 local += nextLocalDeclaration(-(local + arity * Type.addressSize));
             }
         }
 
         assembler.emit(label, "addi", allocator.sp, allocator.sp, -(40 + local));
         assembler.emit("sw", allocator.ra, 40, allocator.sp);
         assembler.emit("sw", allocator.fp, 36, allocator.sp);
         for(int i = 0; i < 8; ++i){
             int offset = 32 - i * 4;
             assembler.emit("sw $s" + i + ", " + offset + "($sp)");
         }
         assembler.emit("addi", allocator.fp, allocator.sp, (40 + local + 4 * arity));
         
         nextBeginStatement();
 
         assembler.emit("lw", allocator.ra, 40, allocator.sp);
         assembler.emit("lw", allocator.fp, 36, allocator.sp);
         for(int i = 0; i < 8; ++i){
             int offset = 32 - i * 4;
             assembler.emit("lw $s" + i + ", " + offset + "($sp)");
         }
         assembler.emit("addi", allocator.sp, allocator.sp, (40 + local + 4 * arity));
         
         assembler.emit("jr", allocator.ra);
 
         exit("body");
     }
 
     //NextStatement. Parses the next statement.
 
     protected void nextStatement(){
         enter("statement");
 
         switch(scanner.getToken()){
             case nameToken: {nextAssignmentOrCallStatement();break;}
             case boldBeginToken: {nextBeginStatement();break;}
             case boldCodeToken: {nextCodeStatement();break;}
             case boldIfToken: {nextIfStatement();break;}
             case boldValueToken : {nextValueStatement();break;}
             case boldWhileToken: {nextWhileStatement();break;}
             default: nextExpected(
                             nameToken,
                             boldBeginToken,
                             boldCodeToken,
                             boldIfToken,
                             boldValueToken,
                             boldWhileToken);
         }
 
         exit("statement");
     }
 
     //NextAssignmentOrCallStatement. Parses the next assignment or call statement.
     
     protected void nextAssignmentOrCallStatement(){
         enter("assignment or call statement");
         
         String name = scanner.getString();
         NameDescriptor descriptor = table.getDescriptor(name);
         Type type = descriptor.getType();
         nextExpected(nameToken);
         
         switch(scanner.getToken()){
             case openParenToken: {
                 nextCall(descriptor, name);
                 break;
             }
             case openBracketToken: {
                 if(!(type instanceof ArrayType)){
                     throw new SnarlCompilerException(name + " is not an array.");
                 }
                 scanner.nextToken();
                 Allocator.Register register1 = descriptor.rvalue();
                 RegisterDescriptor registerDescriptor1 = nextExpression();
                 typeCheck(registerDescriptor1, intType);
                 Allocator.Register register2 = registerDescriptor1.getRegister();
                 nextExpected(closeBracketToken);
                 nextExpected(colonEqualToken);
                 RegisterDescriptor registerDescriptor2 = nextExpression();
                 typeCheck(registerDescriptor2, intType);
                 Allocator.Register register3 = registerDescriptor2.getRegister();
                 assembler.emit("sll", register2, register2, 2);
                 assembler.emit("add", register1, register1, register2);
                 allocator.release(register2);
                 assembler.emit("sw", register3, 0, register1);
                 allocator.release(register1);
                 allocator.release(register3);
                 break;
             }
             case colonEqualToken: {                                                
                 if(!(type.isSubtype(intType) || type.isSubtype(stringType))){
                     throw new SnarlCompilerException(name + " must of type int or type string.");
                 }
                 scanner.nextToken();
                 Allocator.Register register = descriptor.lvalue();
                 RegisterDescriptor registerDescriptor = nextExpression();
                 typeCheck(registerDescriptor, type);
                 assembler.emit("sw", registerDescriptor.getRegister(), 0, register);
                 allocator.release(register);
                 allocator.release(registerDescriptor.getRegister());
                 break;
             }
             default: {
                 nextExpected(
                         openParenToken,
                         openBracketToken,
                         colonEqualToken);
             }
         }
         
         exit("assignment or call statement");
     }
 
     //NextBeginStatement. Parses the next begin statement.
     
     protected void nextBeginStatement(){
         enter("begin statement");
         
         nextExpected(boldBeginToken);
         
         if(scanner.getToken() != boldEndToken){
             
             nextStatement();
             
             while(scanner.getToken() == semicolonToken){
                 scanner.nextToken();
                 nextStatement();
             }
         }
         
         nextExpected(boldEndToken);
         
         exit("begin statement");
     }
 
     //NextCodeStatement. Parses the next code statement.
     
     protected void nextCodeStatement(){
         enter("code statement");
         
         nextExpected(boldCodeToken);
         assembler.emit(scanner.getString());
         nextExpected(stringConstantToken);
         
         exit("code statement");
     }
 
     //NextIfStatement. Parses the next if statement.
     
     protected void nextIfStatement(){
         enter("if statement");
 
         Label exitLabel = new Label("endif");
 
         while(scanner.getToken() == boldIfToken){
             scanner.nextToken();
             RegisterDescriptor descriptor = nextExpression();
             typeCheck(descriptor, intType);
             Label elseLabel = new Label("else");
             assembler.emit("beq", descriptor.getRegister(), allocator.zero, elseLabel);
             allocator.release(descriptor.getRegister());
 
             nextExpected(boldThenToken);
             nextStatement();
             
             assembler.emit("j", exitLabel);
             assembler.emit(elseLabel);
             
             if(scanner.getToken() == boldElseToken){
                 scanner.nextToken();
                 if(scanner.getToken() != boldIfToken){
                     nextStatement();
                 }
             }
         }
         
         assembler.emit(exitLabel);
 
         exit("if statement");
         
         
     }
 
     //NextValueStatement. Parses the next value statement.
     
     protected void nextValueStatement(){
         enter("value statement");
         
         nextExpected(boldValueToken);
         RegisterDescriptor descriptor = nextExpression();
         typeCheck(descriptor, procValType);
         assembler.emit("move", allocator.v0, descriptor.getRegister());
         allocator.release(descriptor.getRegister());
         
         exit("value statement");
     }
 
     //NextWhileStatement. Parses the next while statement.
     
     protected void nextWhileStatement(){
         enter("while statement");
 
         nextExpected(boldWhileToken);
         
         Label start = new Label("start");
         Label end = new Label("end");
         assembler.emit(start);
         
         RegisterDescriptor descriptor = nextExpression();
         
         typeCheck(descriptor, intType);
         
         assembler.emit("beq", descriptor.getRegister(), allocator.zero, end);
         allocator.release(descriptor.getRegister());
         
         nextExpected(boldDoToken);
         nextStatement();
         
         assembler.emit("j", start);
         assembler.emit(end);
         
         exit("while statement");
     }
 
     //NextExpression. Parses the next expression.
 
     protected RegisterDescriptor nextExpression(){
         enter("expression");
         
         RegisterDescriptor first = nextConjunction();
 
         if (scanner.getToken() == boldOrToken) {
             Label label = new Label("or");
 
             typeCheck(first, intType);
             Allocator.Register register = first.getRegister();
 
             assembler.emit("sne", register, register, allocator.zero);
 
             while (scanner.getToken() == boldOrToken){
                 scanner.nextToken();

                assembler.emit("bne", register, allocator.zero, label);

                 RegisterDescriptor rest = nextConjunction();
                 typeCheck(rest, intType);
 
                 assembler.emit("sne", register, rest.getRegister(), allocator.zero);
                 allocator.release(rest.getRegister());
             }
             assembler.emit(label);
         }
 
         exit("expression");
         
         return first;
     }
 
     //NextConjunction. Parses the next conjunction.
     
     protected RegisterDescriptor nextConjunction(){
         enter("conjunction");
 
         RegisterDescriptor first = nextComparison();
 
         if (scanner.getToken() == boldAndToken) {
             Label label = new Label("and");
             
             typeCheck(first, intType);
             Allocator.Register register = first.getRegister();
             
             assembler.emit("sne", register, register, allocator.zero);
             while (scanner.getToken() == boldAndToken){
                 scanner.nextToken();

                assembler.emit("beq", register, allocator.zero, label);

                 RegisterDescriptor rest = nextComparison();
                 typeCheck(rest, intType);
 
                 assembler.emit("sne", register, rest.getRegister(), allocator.zero);
                 allocator.release(rest.getRegister());
             }
            assembler.emit(label);
         }
 
         exit("conjunction");
         
         return first;
     }
 
     //NextComparison. Parses the next comparison.
     
     protected RegisterDescriptor nextComparison(){
         enter("comparison");
 
         RegisterDescriptor first = nextSum();
 
         if(isInSet(scanner.getToken(), comparisonOperators)){
             typeCheck(first, intType);
             
             int operator = scanner.getToken();
             Allocator.Register register = first.getRegister();
             
             scanner.nextToken();
             RegisterDescriptor rest = nextSum();
             typeCheck(rest, intType);
             
             assembler.emit(opToString[operator], register, register, rest.getRegister());
             
             allocator.release(rest.getRegister());
         }
 
         if(isInSet(scanner.getToken(), comparisonOperators)){
             throw new SnarlCompilerException("Chaining comparison operators is not allowed");
         }
         
         exit("comparison");
         
         return first;
     }
 
     //NextSum. Parses the next sum.
 
     protected RegisterDescriptor nextSum(){           
         enter("sum");
 
         RegisterDescriptor first = nextProduct();
 
         if (isInSet(scanner.getToken(),sumOperators)) {
             typeCheck(first, intType);
             Allocator.Register register = first.getRegister();
             while(isInSet(scanner.getToken(),sumOperators)){
                 int operator = scanner.getToken();
                 scanner.nextToken();
                 RegisterDescriptor rest = nextProduct();
                 typeCheck(rest, intType);
                 
                 assembler.emit(opToString[operator], register, register, rest.getRegister());
                 
                 allocator.release(rest.getRegister());
             }
         }
 
         exit("sum");
 
         return first;
     }
 
     //NextProduct. Parses the next product.
 
     protected RegisterDescriptor nextProduct(){
         enter("product");
 
         RegisterDescriptor first = nextTerm();
 
         if (isInSet(scanner.getToken(), productOperators)) {
             typeCheck(first, intType);
             Allocator.Register register = first.getRegister();
             while(isInSet(scanner.getToken(),productOperators)){
                 int operator = scanner.getToken();
                 scanner.nextToken();
                 RegisterDescriptor rest = nextTerm();
                 typeCheck(rest, intType);
                 
                 assembler.emit(opToString[operator], register, register, rest.getRegister());
                 
                 allocator.release(rest.getRegister());
             }
         }
 
         exit("product");
         
         return first;
     }
 
     //NextTerm. Parses the next term.
 
     protected RegisterDescriptor nextTerm(){
         enter("term");
 
         RegisterDescriptor descriptor;
         
         switch (scanner.getToken()){
             case dashToken: {
                 scanner.nextToken();
                 if(scanner.getToken() == intConstantToken){
                     Allocator.Register register = allocator.request();
                     assembler.emit("li", register, -(scanner.getInt()));
                     descriptor = new RegisterDescriptor(intType, register);
                     scanner.nextToken();
                 } else {
                     descriptor = nextTerm();
                     typeCheck(descriptor, intType);
                     
                     assembler.emit("sub", descriptor.getRegister(), allocator.zero, descriptor.getRegister());
                     
                     scanner.nextToken();
                 }
                 break;
             }
             case boldNotToken: {
                 descriptor = nextTerm();
                 typeCheck(descriptor, intType);
                 
                 assembler.emit("seq", descriptor.getRegister(), allocator.zero, descriptor.getRegister());
                 
                 scanner.nextToken();
                 break;
             }
             default: {
                 descriptor = nextUnit();
                 break;
             }
         }
         
         exit("term");
         
         return descriptor;
     }
 
     //NextUnit. Parses the next unit.
     
     protected RegisterDescriptor nextUnit(){       
         enter("unit");
         RegisterDescriptor registerDescriptor;
         switch (scanner.getToken()){
             case nameToken: {
                 String name = scanner.getString();
                 NameDescriptor descriptor = table.getDescriptor(name);
                 Type type = descriptor.getType();
                 scanner.nextToken();
                 switch(scanner.getToken()){
                     case openParenToken: {
                         nextCall(descriptor, name);
                         Allocator.Register register = allocator.request();
                         assembler.emit("move", register, allocator.v0);
                         
                         registerDescriptor = new RegisterDescriptor(((ProcedureType) type).getValue(), register);
                         break;
                     }
                     case openBracketToken: {
                         if(!(type instanceof ArrayType)){
                             throw new SnarlCompilerException(name + " is not an array.");
                         }
                         scanner.nextToken();
                         RegisterDescriptor index = nextExpression();
                         typeCheck(index, intType);
                         nextExpected(closeBracketToken);
                         
                         Allocator.Register register = descriptor.rvalue();
                         assembler.emit("sll", index.getRegister(), index.getRegister(), 2);
                         assembler.emit("add", register, register, index.getRegister());
                         allocator.release(index.getRegister());
                         assembler.emit("lw", register, 0, register);
                         
                         registerDescriptor = new RegisterDescriptor(((ArrayType) type).getBase(), register);
                         break;
                     }
                     default: {
                         Allocator.Register register = descriptor.rvalue();
                         registerDescriptor = new RegisterDescriptor(table.getDescriptor(name).getType(), register);
                         break;
                     }
                 }
                 break;
             }
             case openParenToken: {
                 scanner.nextToken();
                 registerDescriptor = nextExpression();
                 nextExpected(closeParenToken);
                 break;
             }
             case stringConstantToken: {
                 Allocator.Register register = allocator.request();
                 Label label = global.enterString(scanner.getString());
                 assembler.emit("la", register, label);
                 registerDescriptor = new RegisterDescriptor(stringType, register);
                 scanner.nextToken();
                 break;
             }
             case intConstantToken: {
                 Allocator.Register register = allocator.request();
                 assembler.emit("li", register, scanner.getInt());
                 registerDescriptor = new RegisterDescriptor(intType, register);
                 scanner.nextToken();
                 break;
             }
             default: {
                 nextExpected(
                     nameToken,
                     openParenToken,
                     stringConstantToken,
                     intConstantToken
                 );
                 registerDescriptor = null;
             }
         }
 
         exit("unit");
         
         return registerDescriptor;
     }
     
     //nextCall. Parses a procedure call, but does not move $v0 into a different register.
     
     protected void nextCall(NameDescriptor descriptor, String name){
         if(!(descriptor.getType() instanceof ProcedureType)){
             throw new SnarlCompilerException(name + " is not a procedure.");
         }
         nextArguments((ProcedureType) descriptor.getType());
         assembler.emit("jal", ((GlobalProcedureDescriptor) descriptor).getLabel());
     }
 
     //NextArguments. Parses the next arguments.
 
     protected void nextArguments(ProcedureType proc){
         enter("arguments");
         
         nextExpected(openParenToken);
         ProcedureType.Parameter params = proc.getParameters();
 
         if(scanner.getToken() != closeParenToken){
             if(params == null){
                 throw new SnarlCompilerException("Procedure has too many arguments");
             }
             RegisterDescriptor descriptor = nextExpression();
             typeCheck(descriptor, params.getType());
             assembler.emit("sw", descriptor.getRegister(), 0, allocator.sp);
             allocator.release(descriptor.getRegister());
             assembler.emit("addi", allocator.sp, allocator.sp, -4);
             
             params = params.getNext();
             while(scanner.getToken() == commaToken){
                 scanner.nextToken();
                 if(params == null){
                     throw new SnarlCompilerException("Procedure has too many arguments");
                 }
                 descriptor = nextExpression();
                 typeCheck(descriptor, params.getType());
                 assembler.emit("sw", descriptor.getRegister(), 0, allocator.sp);
                 allocator.release(descriptor.getRegister());
                 assembler.emit("addi", allocator.sp, allocator.sp, -4);
                 
                 params = params.getNext();
             }
         }
         
         if(params != null){
             throw new SnarlCompilerException("Expected " + proc.getArity() + " arguments.");
         }
 
         nextExpected(closeParenToken);
 
         exit("arguments");
     }
 
     //NextExpected(int token). Checks to see if the current token is the token passed, if not throws an exception.
 
     protected void nextExpected(int token) throws SnarlCompilerException{
         if(scanner.getToken() == token){
             scanner.nextToken();
         } else {
             throw new SnarlCompilerException("Expected " + tokenToString(token) + ".");
         }
     }
 
     //NextExpected(int... tokens). Checks to see if the current token is in tokens, if not throws an exception.
 
     protected void nextExpected(int... tokens) throws SnarlCompilerException{
         boolean expected = false;
         for (int token : tokens) {
             if (scanner.getToken() == token) {
                 expected = true;
             }
         }
         if(expected){
             scanner.nextToken();
         } else {
             StringBuilder error = new StringBuilder();
             for(int i = 0; i < tokens.length - 1; ++i){
                 error.append(tokenToString(tokens[i]));
                 error.append(" or ");
             }
             error.append(tokenToString(tokens[tokens.length - 1]));
             throw new SnarlCompilerException("Expected " + error.toString() + ".");
         }
     }
 
     public static void main(String[] args){
         String file = args[0];
         Compiler compiler;
         Source source = null;
         try {
             source = new Source(new FileReader(file));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         compiler = new Compiler(source);
 
         try {
             compiler.passOne();
         } catch (SnarlCompilerException e) {
             e.printStackTrace();
             source.error(e.getMessage());
         }
     }
 }
