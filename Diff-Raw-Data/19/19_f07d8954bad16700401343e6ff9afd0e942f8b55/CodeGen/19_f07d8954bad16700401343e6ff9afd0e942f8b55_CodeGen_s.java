 package mips;
 
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 import ast.*;
 import types.*;
 
 /**
  * A visitor that generated MIPS assembly code.
  */
 public class CodeGen implements ast.CommandVisitor {
     /* Collected error messages. */
     private StringBuilder errorBuffer = new StringBuilder();
 
     /* The type checker. */
     private TypeChecker typeChecker;
 
     /* The program we're building. */
     private Program program;
 
     /* The end point of a function, jump here when all is done. */
     private String funcRetLabel;
 
     /* The current activation record. */
     private ActivationRecord currentFunction;
 
     /**
      * Construct a new code generator using the given type checker.
      * @param TypeChecker The type checker to use.
      */
     public CodeGen(TypeChecker typeChecker) {
         this.typeChecker = typeChecker;
         this.program = new Program();
     }
 
     /**
      * Query for errors.
      * @return Indication of error precense.
      */
     public boolean hasError() {
         return errorBuffer.length() != 0;
     }
 
     /**
      * Get an error report.
      * @return An error report.
      */
     public String errorReport() {
         return errorBuffer.toString();
     }
 
 	/**
 	 * Exception representing an error in the code generation.
 	 */
     private class CodeGenException extends RuntimeException {
         private static final long serialVersionUID = 1L;
 
         /**
          * Construct an error with an message.
          * @param errorMessage The error message.
          */
         public CodeGenException(String errorMessage) {
             super(errorMessage);
         }
     }
 
     /**
      * Generate assemly program on an AST.
      * @param ast The AST to generate code from.
      * @return Success indication.
      */
     public boolean generate(Command ast) {
         boolean error = false;
         try {
             currentFunction = ActivationRecord.newGlobalFrame();
             ast.accept(this);
             error = !hasError();
         } catch (CodeGenException cge) {
             error = true;
         }
         return !error;
     }
 
     /**
      * Get the generated program representation.
      * @return The program.
      */
     public Program getProgram() {
         return program;
     }
 
 	/**
 	 * Generate a unique name in the crux namespace for functions to not conflict with MIPS built ins.
 	 * @param name The input name.
 	 * @return A mangled version of input.
 	 */
     private String namespaceFunc(String name) {
         return "cruxfunc." + name;
     }
 
 	/**
 	 * Short hand for testing if the given type is of MIPS int compatible type.
 	 * @param type The type to test.
 	 * @return true if int compatible.
 	 */
     private boolean isIntCompatType(Type type) {
     	return type.equivalent(new IntType()) || type.equivalent(new BoolType());
     }
 
 	// Visitor methods ===================================
 
     @Override
     public void visit(ExpressionList node) {
         for (Expression expr : node) {
         	expr.accept(this);
         }
     }
 
     @Override
     public void visit(DeclarationList node) {
         for (Declaration decl : node) {
         	decl.accept(this);
         }
     }
 
     @Override
     public void visit(StatementList node) {
         for (Statement stmt : node) {
         	stmt.accept(this);
         	if (stmt instanceof Call) {
         		Call callNode = (Call) stmt;
 				Type retType = typeChecker.getType(callNode);
 				if (!retType.equivalent(new VoidType())) {
 					program.debugComment("Cleaning up unused function return value on stack.");
 					if (retType.equivalent(new FloatType())) {
 						program.popFloat("$t0");
 					} else if (isIntCompatType(retType)) {
 						program.popInt("$t0");
 					}
 				}
         	}
         }
     }
 
     @Override
     public void visit(AddressOf node) {
         program.debugComment("Taking address of variable " + node.symbol().name() + ".");
 		currentFunction.getAddress(program, "$t0" , node.symbol());
 		program.pushInt("$t0");
     }
 
     @Override
     public void visit(LiteralBool node) {
         int intVal = -1;
         switch (node.value()) {
 			case TRUE:
 			  	intVal = 1;
 			  	break;
 			case FALSE:
 			  	intVal = 0;
         }
         program.debugComment("Literalbool == " + intVal);
 		program.appendInstruction("li $t0, " + intVal);
 		program.pushInt("$t0");
     }
 
     @Override
     public void visit(LiteralFloat node) {
     	Float value = node.value();
         program.debugComment("LiteralFloat == " + value);
 		program.appendInstruction("li.s $f0, " + value);
 		program.pushFloat("$f0");
     }
 
     @Override
     public void visit(LiteralInt node) {
     	int value = node.value();
         program.debugComment("LiteralInt == " + value);
 		program.appendInstruction("li $t0, " + value);
 		program.pushInt("$t0");
     }
 
     @Override
     public void visit(VariableDeclaration node) {
 		currentFunction.add(program, node);
     }
 
     @Override
     public void visit(ArrayDeclaration node) {
 		currentFunction.add(program, node);
     }
 
     @Override
     public void visit(FunctionDefinition node) {
 		String funcName = node.symbol().name();
 		boolean isMain = funcName.equals("main");
 		program.debugComment("Function definition  starts here.");
 		funcRetLabel = program.newLabel();
 		program.debugComment("Join label for function is " + funcRetLabel);
 		if (!isMain) {
 			funcName = namespaceFunc(node.function().name());
 		}
 		program.debugComment("Register argument symbols.");
         currentFunction = new ActivationRecord(node, currentFunction);
 		program.debugComment("done -> Register argument symbols.");
 
 		int startPos = program.appendInstruction(funcName + ":");
 
 		program.debugComment("Function body begins here.");
 		node.body().accept(this);
 		program.debugComment("done -> Function body begins here.");
 		program.insertPrologue((startPos + 1), currentFunction.stackSize());
 
 		program.appendInstruction(funcRetLabel + ":");
 		Type retType = typeChecker.getType(node);
 		if (!retType.equivalent(new VoidType())) {
 			program.debugComment("Storing return value.");
 			if (retType.equivalent(new FloatType())) {
 				program.popFloat("$v0");
 			} else if (isIntCompatType(retType)) {
 				program.popInt("$v0");
 			}
 		}
 
 		program.appendEpilogue(currentFunction.stackSize(), isMain);
     	currentFunction = currentFunction.parent();
     }
 
     @Override
     public void visit(Addition node) {
         program.debugComment("Addition beginns.");
         program.debugComment("Visit left hand side.");
         node.leftSide().accept(this);
         program.debugComment("Visit right hand side.");
         node.rightSide().accept(this);
         Type type = typeChecker.getType(node);
         if (type.equivalent(new FloatType())) {
         	program.popFloat("$f2");
         	program.popFloat("$f0");
         	program.appendInstruction("add.s $f4, $f0, $f2");
         	program.debugComment("Store addition result.");
         	program.pushFloat("$f4");
         } else if (isIntCompatType(type)) {
         	program.popInt("$t1");
         	program.popInt("$t0");
         	program.appendInstruction("add $t2, $t0, $t1");
         	program.debugComment("Store addition result.");
         	program.pushInt("$t2");
         }
         program.debugComment("done -> Addition beginns.");
     }
 
     @Override
     public void visit(Subtraction node) {
         program.debugComment("Substract beginns.");
         program.debugComment("Visit left hand side.");
         node.leftSide().accept(this);
         program.debugComment("Visit right hand side.");
         node.rightSide().accept(this);
 
         Type type = typeChecker.getType(node);
         if (type.equivalent(new FloatType())) {
         	program.popFloat("$f2");
         	program.popFloat("$f0");
         	program.appendInstruction("sub.s $f4, $f0, $f2");
         	program.debugComment("Store substraction result.");
         	program.pushFloat("$f4");
         } else if (isIntCompatType(type)) {
         	program.popInt("$t1");
         	program.popInt("$t0");
         	program.appendInstruction("sub $t3, $t0, $t1");
         	program.debugComment("Store substraction result.");
         	program.pushInt("$t3");
         }
         program.debugComment("done -> Substract beginns.");
     }
 
     @Override
     public void visit(Multiplication node) {
         program.debugComment("Multiplication beginns.");
         program.debugComment("Visit left hand side.");
         node.leftSide().accept(this);
         program.debugComment("Visit right hand side.");
         node.rightSide().accept(this);
         Type type = typeChecker.getType(node);
         if (type.equivalent(new FloatType())) {
         	program.popFloat("$f2");
         	program.popFloat("$f0");
         	program.appendInstruction("mul.s $f4, $f0, $f2");
         	program.debugComment("Store mul result.");
         	program.pushFloat("$f4");
         } else if (isIntCompatType(type)) {
         	program.popInt("$t1");
         	program.popInt("$t0");
         	program.appendInstruction("mul $t3, $t0, $t1");
         	program.debugComment("Store mul result.");
         	program.pushInt("$t3");
         }
         program.debugComment("done -> Multiplication beginns.");
     }
 
     @Override
     public void visit(Division node) {
         program.debugComment("Divison beginns.");
         program.debugComment("Visit left hand side.");
         node.leftSide().accept(this);
         program.debugComment("Visit right hand side.");
         node.rightSide().accept(this);
         Type type = typeChecker.getType(node);
         if (type.equivalent(new FloatType())) {
         	program.popFloat("$f2");
         	program.popFloat("$f0");
         	program.appendInstruction("div.s $f4, $f0, $f2");
         	program.debugComment("Store div result.");
         	program.pushFloat("$f4");
         } else if (isIntCompatType(type)) {
         	program.popInt("$t1");
         	program.popInt("$t0");
         	program.appendInstruction("div $t3, $t0, $t1");
         	program.debugComment("Store div result.");
         	program.pushInt("$t3");
         }
         program.debugComment("done -> Divison beginns.");
     }
 
     @Override
     public void visit(LogicalAnd node) {
         program.debugComment("LogicalAnd beginns here.");
         program.debugComment("Visiting LHS");
 		node.leftSide().accept(this);
         program.debugComment("Visiting RHS");
 		node.rightSide().accept(this);
         program.debugComment("Popping off RHS value.");
         program.popInt("$t1");
         program.debugComment("Popping off LHS value.");
         program.popInt("$t0");
         program.appendInstruction("and $t2, $t0, $t1");
         program.debugComment("Pushing and result.");
        program.popInt("$t0");
         program.pushInt("$t2");

         program.debugComment("done -> LogicalAnd beginns here.");
     }
 
     @Override
     public void visit(LogicalOr node) {
    	// TODO 
        throw new RuntimeException("Implement this");
     }
 
     @Override
     public void visit(LogicalNot node) {
     	// TODO 
         throw new RuntimeException("Implement this");
     }
 
     @Override
     public void visit(Comparison node) {
         program.debugComment("Comparsion beginns here.");
         program.debugComment("Evaluate LHS.");
 		node.leftSide().accept(this);
         program.debugComment("Evaluate RHS.");
 		node.rightSide().accept(this);
         Type type = typeChecker.getType((Command) node.leftSide());
         if (type.equivalent(new FloatType())) {
         	program.debugComment("Popping off RHS value");
 			program.popFloat("$f2");
         	program.debugComment("Popping off LHS value");
 			program.popFloat("$f0");
 			switch (node.operation()) {
 				case LT:
         			program.debugComment("$f0 < $f2");
 					program.appendInstruction("c.lt.s $f0, $f2");
 					pushFloatCond();
 					break;
 				case GT:
         			program.debugComment("$f0 > $f2");
 					program.appendInstruction("c.gt.s $f0, $f2");
 					pushFloatCond();
 					break;
 				case LE: 
         			program.debugComment("$f0 <= $f2");
 					program.appendInstruction("c.le.s $f0, $f2");
 					pushFloatCond();
 					break;
 				case GE:
         			program.debugComment("$f0 >= $f2");
 					program.appendInstruction("c.ge.s $f0, $f2");
 					pushFloatCond();
 					break;
 				case EQ:
         			program.debugComment("$f0 == $f2");
 					program.appendInstruction("c.eq.s $f0, $f2");
 					pushFloatCond();
 					break;
 				case NE:
         			program.debugComment("$f0 != $f2");
 					program.appendInstruction("c.ne.s $f0, $f2");
 					pushFloatCond();
 					break;
 			}
         } else if (isIntCompatType(type)) {
         	program.debugComment("Popping off RHS value");
 			program.popInt("$t1");
         	program.debugComment("Popping off LHS value");
 			program.popInt("$t0");
 			switch (node.operation()) {
 				case LT:
         			program.debugComment("$t0 < $t1");
 					program.appendInstruction("slt $t2, $t0, $t1");
 					break;
 				case GT:
         			program.debugComment("$t0 > $t1");
 					//program.appendInstruction("slt $t2, $t1, $t0");
 					// Why not use synthetic instruction if we can? Clearer to read.
 					program.appendInstruction("sgt $t2, $t0, $t1");
 					break;
 				case LE: 
         			program.debugComment("$t0 <= $t1");
 					program.appendInstruction("sle $t2, $t0, $t1");
 					break;
 				case GE:
         			program.debugComment("$t0 >= $t1");
 					program.appendInstruction("sge $t2, $t0, $t1");
 					break;
 				case EQ:
         			program.debugComment("$t0 == $t1");
 					program.appendInstruction("seq $t2, $t0, $t1");
 					break;
 				case NE:
         			program.debugComment("$t0 != $t1");
 					program.appendInstruction("sne $t2, $t0, $t1");
 					break;
 			}
         	program.debugComment("Pushing comparsion outcome.");
 			program.pushInt("$t2");
         }
         program.debugComment("done -> Comparsion beginns here.");
     }
 
     /**
      * Push the value of the floating condition register to the stack.
      */
 	private void pushFloatCond() {
 		program.debugComment("Determin float cond value.");
 		String elseLabel = program.newLabel();
     	program.debugComment("elseLabel = " + elseLabel);
 		String joinLabel = program.newLabel();
     	program.debugComment("joinLabel = " + joinLabel);
 
 		program.appendInstruction("bc1f " + elseLabel);
 		program.debugComment("Float cond was true.");
 		program.appendInstruction("li $t0, 1");
 		program.appendInstruction("b " + joinLabel);
 		program.appendInstruction(elseLabel + ":");
 		program.debugComment("Float cond was false.");
 		program.appendInstruction("li $t0, 0");
 		program.appendInstruction(joinLabel + ":");
 
 		program.pushInt("$t0");
 	}
 
     @Override
     public void visit(Dereference node) {
         program.debugComment("Dereferencing address. Now getting address.");
         node.expression().accept(this);
         program.popInt("$t0"); // Contains address to type ,/
         Type type = typeChecker.getType(node);
         program.debugComment("Load value at the found address.");
         if (type.equivalent(new FloatType())) {
 			program.appendInstruction("lwc1 $f0, 0($t0)");
 			program.pushFloat("$f0");
         } else if (isIntCompatType(type)) {
 			program.appendInstruction("lw $t1, 0($t0)");
 			program.pushInt("$t1");
         } else {
         	throw new RuntimeException("Bad type in deref?.");
         }
     }
 
     @Override
     public void visit(Index node) {
         program.debugComment("Taking index of expression.");
         node.base().accept(this);
         node.amount().accept(this);
         program.debugComment("Popping amount. ");
         program.popInt("$t0");
         program.debugComment("Popping base address.");
         program.popInt("$t1"); 
 
         Type type = typeChecker.getType(node);
         program.debugComment("Calculate base + offset");
 		program.appendInstruction("li $t2, " + type.numBytes());
 		program.appendInstruction("mul $t3, $t0, $t2");
 		program.appendInstruction("add $t4, $t1, $t3");
 		program.pushInt("$t4");
     }
 
     @Override
     public void visit(Assignment node) {
     	program.debugComment("Assignment beginns.");
     	program.debugComment("Handle destination.");
     	node.destination().accept(this);
     	program.debugComment("Handle source.");
     	node.source().accept(this);
     	Type type = typeChecker.getType(node);
         if (type instanceof ArrayType) {
         	// TODO 
         	throw new RuntimeException("Implement this?????????????/");
         } else if (type.equivalent(new FloatType())) { 
         	program.popFloat("$f0");
         	program.appendInstruction("swc1 $f0, 0($t0)");
         } else { // Int
     		program.debugComment("Popping off value in asignmnet.");
     		program.popInt("$t0"); 
     		program.debugComment("Popping off destination address in assigmnet.");
         	program.popInt("$t1"); // dest addr
     		program.debugComment("Final assignment.");
         	program.appendInstruction("sw $t0, 0($t1)");
         }
     }
 
     @Override
     public void visit(Call node) {
         program.debugComment("Caller Setup");
         program.debugComment("Begin evaluate function arguments.");
 		node.arguments().accept(this);
         program.debugComment("done -> Begin evaluate function arguments.");
 
         String funcName =  node.function().name();
         if (!funcName.matches("print(Bool|Float|Int|ln)|read(Float|Int)")) {
         	funcName = namespaceFunc(funcName);
         } else {
         	funcName = "func." + funcName;
         }
         program.appendInstruction("jal " + funcName);
 
         program.debugComment("Caller Teardown.");
         if (node.arguments().size() > 0) {
         	program.debugComment("Cleaning up used func args.");
         	int argSize = 0;
         	for (Expression expr : node.arguments()) {
 				Type type = typeChecker.getType((Command) expr);
         		argSize += type.numBytes();
         	}
 			program.appendInstruction("addi $sp, $sp, " + argSize);
         }
 
 		FuncType func = (FuncType) node.function().type();
  		if (!func.returnType().equivalent(new VoidType())) {
         	program.debugComment("Saving function return value at $v0 on the stack.");
 			program.appendInstruction("subu $sp, $sp, 4");
 			program.appendInstruction("sw $v0, 0($sp)");
  		}
 
     }
 
     @Override
     public void visit(IfElseBranch node) {
     	program.debugComment("IfElseBranch beginns here");
 		String elseLabel = program.newLabel();
     	program.debugComment("elseLabel = " + elseLabel);
 		String joinLabel = program.newLabel();
     	program.debugComment("joinLabel = " + joinLabel);
 
     	program.debugComment("Evaluate if-condition.");
         node.condition().accept(this);
     	program.debugComment("done -> Evaluate if expression.");
     	program.debugComment("Pop off condition.");
     	program.popInt("$t7");
     	program.appendInstruction("beqz $t7, " + elseLabel);
 
     	program.debugComment("Then branch beginns.");
 		node.thenBlock().accept(this);
 		program.appendInstruction("b " + joinLabel);
     	program.debugComment("done -> Then branch beginns.");
     	program.debugComment("Else branch beginns.");
     	program.appendInstruction(elseLabel + ":");
 		node.elseBlock().accept(this);
     	program.debugComment("done -> Else branch beginns.");
 
     	program.appendInstruction(joinLabel + ":");
     	program.debugComment("done -> IfElseBranch beginns here");
     }
 
     @Override
     public void visit(WhileLoop node) {
         program.debugComment("While loop beginns here.");
 		String condLabel = program.newLabel();
     	program.debugComment("condLabel = " + condLabel);
 		String joinLabel = program.newLabel();
     	program.debugComment("joinLabel = " + joinLabel);
 
     	program.debugComment("Evaluate while-condition.");
     	program.appendInstruction(condLabel + ":");
         node.condition().accept(this);
     	program.debugComment("done -> Evaluate while-condition.");
     	program.debugComment("Pop off condition.");
     	program.popInt("$t7");
     	program.appendInstruction("beqz $t7, " + joinLabel);
 
     	program.debugComment("While body  beginns.");
     	node.body().accept(this);
     	program.appendInstruction("b " + condLabel);
     	program.debugComment("done -> While body  beginns.");
     	program.appendInstruction(joinLabel + ":");
         program.debugComment("done -> While loop beginns here.");
     }
 
     @Override
     public void visit(Return node) {
     	program.debugComment("Begin return func value.");
     	node.argument().accept(this);
     	program.debugComment("done -> Begin return func value.");
     	program.debugComment("Jumping to end of function.");
     	program.appendInstruction("b " + funcRetLabel);
     }
 
     @Override
     public void visit(ast.Error node) {
         String message = "CodeGen cannot compile a " + node;
         errorBuffer.append(message);
         throw new CodeGenException(message);
     }
 }
