 package mips;
 
 import types.TypeChecker;
 import ast.Addition;
 import ast.AddressOf;
 import ast.ArrayDeclaration;
 import ast.Assignment;
 import ast.Call;
 import ast.Command;
 import ast.Comparison;
 import ast.Declaration;
 import ast.DeclarationList;
 import ast.Dereference;
 import ast.Division;
 import ast.Expression;
 import ast.ExpressionList;
 import ast.FunctionDefinition;
 import ast.IfElseBranch;
 import ast.Index;
 import ast.LiteralBool;
 import ast.LiteralBool.Value;
 import ast.LiteralFloat;
 import ast.LiteralInt;
 import ast.LogicalAnd;
 import ast.LogicalNot;
 import ast.LogicalOr;
 import ast.Multiplication;
 import ast.Return;
 import ast.Statement;
 import ast.StatementList;
 import ast.Subtraction;
 import ast.VariableDeclaration;
 import ast.WhileLoop;
 import types .*;
 
 public class CodeGen implements ast.CommandVisitor {
 
 
 	private StringBuffer errorBuffer = new StringBuffer();
 	private TypeChecker tc;
 	private Program program;
 	private ActivationRecord currentFunction;
 
 	private int regCounter = 0;
 
 	private String makeTempRegister(int regNum)
 	{
 		return "$t" + regNum;
 	}
 
 	private String makeSavedRegister(int regNum)
 	{
 		return "$s" + regNum;
 	}
 
 	private String makeArgumentResiterCounter(int regNum)
 	{
 		return "$a" + regNum;
 	}
 	
 	private String makeFloatRegister(int regNum)
 	{
 		return "$f" + regNum;
 	}
 
 
 	public CodeGen(TypeChecker tc)
 	{
 		this.tc = tc;
 		this.program = new Program();
 	}
 
 	public boolean hasError()
 	{
 		return errorBuffer.length() != 0;
 	}
 
 	public String errorReport()
 	{
 		return errorBuffer.toString();
 	}
 
 	private class CodeGenException extends RuntimeException
 	{
 		private static final long serialVersionUID = 1L;
 		public CodeGenException(String errorMessage) {
 			super(errorMessage);
 		}
 	}
 
 	public boolean generate(Command ast)
 	{
 		try 
 		{
 			currentFunction = ActivationRecord.newGlobalFrame();
 			ast.accept(this);
 			
 			return !hasError();
 		} 
 		catch (CodeGenException e) 
 		{
 			return false;
 		}
 	}
 
 	public Program getProgram()
 	{
 		return program;
 	}
 
 	@Override
 	public void visit(ExpressionList node) 
 	{
 		for (Expression e : node)
 			e.accept(this);
 	}
 
 	@Override
 	public void visit(DeclarationList node) 
 	{
 		for (Declaration d : node)
 			d.accept(this);
 		// throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(StatementList node) 
 	{
 		for (Statement statement : node)
 			statement.accept(this);   	
 	}
 
 	@Override
 	public void visit(AddressOf node) 
 	{	
 		// AddressOf either denotes the address of the variable or in the case 
 		// of array, it denotes the value for indexing into the array
 		// In either case, we treat AddressOf as an int or float and push
 		// onto the stack
 		String rd = makeTempRegister(regCounter);
 		// push the address onto the stack
 		program.pushInt(rd);
 	}
 
 	@Override
 	public void visit(LiteralBool node) 
 	{
 		// need to determine the boolean value to push onto the stack
 		Value boolVal = node.value();
 		String rd = makeTempRegister(regCounter);
 		if (boolVal == Value.FALSE)		// put 0 to represent false
 			program.appendInstruction("li " + rd + ", " + 0  
 					+ " # " + rd + " = 0");
 		else
 			program.appendInstruction("li " + rd + ", " + 1  
 					+ " # " + rd + " = 1");
 
 		// push the result back onto the stack
 		program.pushInt(rd);
 	}
 
 	@Override
 	public void visit(LiteralFloat node) 
 	{
 		String rd = makeFloatRegister(regCounter);
 		
 		// add the float value into register
 		// not sure if this works
 		program.appendInstruction("li.s " + rd + ", " + node.value()
 				+ " # " + rd + " = " + node.value());
 
 		// push the result back onto the stack
 		program.pushFloat(rd);
 	}
 
 	@Override
 	public void visit(LiteralInt node) 
 	{
 		String rd = makeTempRegister(regCounter);
 		program.appendInstruction("add " + rd + ", " + "$0, " + node.value()
 					+ " # " + rd + " = " + node.value());
 		program.pushInt(rd);
 	}
 
 	@Override
 	public void visit(VariableDeclaration node) 
 	{
 		// Each time the visitor encounters an array or variable declaration it
 		// notifies the current ActivationRecord object, which records an 
 		// offset (from the frame pointer) where the symbol will be stored at 
 		// runtime.
 		currentFunction.add(program, node);
 	}
 
 	@Override
 	public void visit(ArrayDeclaration node) 
 	{
 		currentFunction.add(program, node);	
 	}
 
 	@Override
 	public void visit(FunctionDefinition node) 
 	{
 		// Each time the CodeGen visitor encounters a FunctionDefinition, it
 		// can create a new ActivationRecord object to model that function's
 		// scope. ActivationRecord's are linked via a parent field, that allows
 		// symbol lookup to chain upwards. If a symbol isn't found in the 
 		// inner-most scope, a parent ActivationRecord supplies the address.
 		currentFunction = new ActivationRecord(node, currentFunction);
 		
 		int pos = program.appendInstruction(program.funcLabel(node.function().name()) + ":") + 1;
 		// since we do not have information about the variables or array 
 		// declarations local to this function until parsing the function's 
 		// body, we need to parse the body first and squeeze in the prologue
 		// after the label but before the function's body
 		node.body().accept(this);
 	
 		// squeeze in the prologue
 		program.insertPrologue(pos, currentFunction.stackSize());
 		program.appendEpilogue(currentFunction.stackSize());
 		
 		// if function is main, exit the program when done
 		if (node.function().name().equals("main"))
 		{
 			// insert mips instructions to exit the program
 			// li $v0, 10 # 10 is the exit syscall.
 			// syscall # do the syscall.
 			program.appendInstruction("li $v0, 10 ");
 			program.appendInstruction("syscall");
 		}
 	}
 
 	@Override
 	public void visit(Addition node) 
 	{
 		// push left and right sides onto stack
 		int regLeft = regCounter;
 		node.leftSide().accept(this);
 		int regRight = ++regCounter;
 		node.rightSide().accept(this);
 
 		// retrieve left and right sides from stack
 		Type leftType = tc.getType((Command) node.leftSide());
 		Type rightType = tc.getType((Command) node.rightSide());
 
 		String rd;
 		String rs;
 	
 		if (leftType instanceof IntType
 				&& rightType instanceof IntType)
 		{
 			rd = makeTempRegister(regLeft);
 			rs = makeTempRegister(regRight);
 			
 			// pop right side
 			program.popInt(rs);
 			// pop left side
 			program.popInt(rd);
 			// do the addition
 			program.appendInstruction("add " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			// push the result back onto stack
 			program.pushInt(rd);
 		}
 		else if (leftType instanceof FloatType
 					&& rightType instanceof FloatType)
 		{
 			rd = makeFloatRegister(regLeft);
 			rs = makeFloatRegister(regRight);
 			program.popFloat(rs);
 			program.popFloat(rd);
 			program.appendInstruction("add.s " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			program.pushFloat(rd);
 		}
 		else
 			throw new RuntimeException("cannot compute " + leftType + " + " + rightType); 
 		
 		regCounter = 0;
 	}
 
 	@Override
 	public void visit(Subtraction node) 
 	{
 		// push left and right sides onto stack
 		int regLeft = regCounter;
 		node.leftSide().accept(this);
 		int regRight = ++regCounter;
 		node.rightSide().accept(this);
 
 		// retrieve left and right sides from stack
 		Type leftType = tc.getType((Command) node.leftSide());
 		Type rightType = tc.getType((Command) node.rightSide());
 
 		String rd;
 		String rs;
 	
 		if (leftType instanceof IntType
 				&& rightType instanceof IntType)
 		{
 			rd = makeTempRegister(regLeft);
 			rs = makeTempRegister(regRight);
 			
 			// pop right side
 			program.popInt(rs);
 			// pop left side
 			program.popInt(rd);
 			// do the addition
 			program.appendInstruction("sub " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			// push the result back onto stack
 			program.pushInt(rd);
 		}
 		else if (leftType instanceof FloatType
 					&& rightType instanceof FloatType)
 		{
 			rd = makeFloatRegister(regLeft);
 			rs = makeFloatRegister(regRight);
 			program.popFloat(rs);
 			program.popFloat(rd);
 			program.appendInstruction("sub.s " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			program.pushFloat(rd);
 		}
 		else
 			throw new RuntimeException("cannot compute " + leftType + " - " + rightType); 
 		
 		regCounter = 0;
 	}
 
 	@Override
 	public void visit(Multiplication node) 
 	{
 		// push left and right sides onto stack
 		int regLeft = regCounter;
 		node.leftSide().accept(this);
 		int regRight = ++regCounter;
 		node.rightSide().accept(this);
 
 		// retrieve left and right sides from stack
 		Type leftType = tc.getType((Command) node.leftSide());
 		Type rightType = tc.getType((Command) node.rightSide());
 
 		String rd;
 		String rs;
 	
 		if (leftType instanceof IntType
 				&& rightType instanceof IntType)
 		{
 			rd = makeTempRegister(regLeft);
 			rs = makeTempRegister(regRight);
 			
 			// pop right side
 			program.popInt(rs);
 			// pop left side
 			program.popInt(rd);
 			// do the addition
 			program.appendInstruction("mul " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			// push the result back onto stack
 			program.pushInt(rd);
 		}
 		else if (leftType instanceof FloatType
 					&& rightType instanceof FloatType)
 		{
 			rd = makeFloatRegister(regLeft);
 			rs = makeFloatRegister(regRight);
 			program.popFloat(rs);
 			program.popFloat(rd);
 			program.appendInstruction("mul.s " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			program.pushFloat(rd);
 		}
 		else
 			throw new RuntimeException("cannot compute " + leftType + " + " + rightType); 
 		
 		regCounter = 0;
 	}
 
 
 	@Override
 	public void visit(Division node) 
 	{
 		// push left and right sides onto stack
 		int regLeft = regCounter;
 		node.leftSide().accept(this);
 		int regRight = ++regCounter;
 		node.rightSide().accept(this);
 
 		// retrieve left and right sides from stack
 		Type leftType = tc.getType((Command) node.leftSide());
 		Type rightType = tc.getType((Command) node.rightSide());
 
 		String rd;
 		String rs;
 	
 		if (leftType instanceof IntType
 				&& rightType instanceof IntType)
 		{
 			rd = makeTempRegister(regLeft);
 			rs = makeTempRegister(regRight);
 			
 			// pop right side
 			program.popInt(rs);
 			// pop left side
 			program.popInt(rd);
 			// do the addition
 			program.appendInstruction("div " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			// push the result back onto stack
 			program.pushInt(rd);
 		}
 		else if (leftType instanceof FloatType
 					&& rightType instanceof FloatType)
 		{
 			rd = makeFloatRegister(regLeft);
 			rs = makeFloatRegister(regRight);
 			program.popFloat(rs);
 			program.popFloat(rd);
 			program.appendInstruction("div.s " + rd + ", " 
 					+ rd + ", " + rs
 					+ " # " + rd + " = " 
 					+  rd + " + " +  rs);
 			program.pushFloat(rd);
 		}
 		else
 			throw new RuntimeException("cannot compute " + leftType + " / " + rightType); 
 		
 		regCounter = 0;
 	}
 
 	// Bitwise and syntax: and $d, $s, $t
 	// $d = $s and $t
 	@Override
 	public void visit(LogicalAnd node) 
 	{
 		// push left and right sides onto stack
 		String rd = makeTempRegister(regCounter++);
 		node.leftSide().accept(this);
 		String rs = makeTempRegister(regCounter);
 		node.rightSide().accept(this);
 
 		Type leftType = tc.getType((Command) node.leftSide());
 		Type rightType = tc.getType((Command) node.rightSide());
 
 		// pop left and right sides from stack
 		if (rightType instanceof BoolType
 				&& leftType instanceof BoolType)
 		{
 			// pop the right side
 			program.popInt(rs);
 			// pop the left side
 			program.popInt(rd);
 			// compute logical and 
 			program.appendData("and " + "rd, " + "rd, " + "rs "
 			 + "# " + rd + " = " + rd + " and " + rs);
 			//push the result back onto stack
 			program.pushInt(rd);
 		}
 		else
 			throw new RuntimeException("cannot compute " + leftType + " and " 
 										+ rightType);
 
 		regCounter = 0;
 
 	}
 
 	@Override
 	public void visit(LogicalOr node) 
 	{
 		// push left and right sides onto stack
 		String rd = makeTempRegister(regCounter++);
 		node.leftSide().accept(this);
 		String rs = makeTempRegister(regCounter);
 		node.rightSide().accept(this);
 		
 		Type leftType = tc.getType((Command) node.leftSide());
 		Type rightType = tc.getType((Command) node.rightSide());
 
 		// pop left and right sides from stack
 		if (rightType instanceof BoolType
 				&& leftType instanceof BoolType)
 		{
 			// pop the right side
 			program.popInt(rs);
 			// pop the left side
 			program.popInt(rd);
 			// compute logical or
 			program.appendData("or " + "rd, " + "rd, " + "rs "
 			 + "# " + rd + " = " + rd + " or " + rs);
 			//push the result back onto stack
 			program.pushInt(rd);
 		}
 		else
 			throw new RuntimeException("cannot compute " + leftType + " or " 
 										+ rightType);
 
 		regCounter = 0;
 	}
 
 	@Override
 	public void visit(LogicalNot node) 
 	{
 		String rd = makeTempRegister(regCounter++);
 		// push the boolean expression onto the stack
 		node.expression().accept(this);
 
 		// MIPS does not support bitwise negation (this can be performed with 
 		// two instructions: setting a register's value to -1 with addi and
 		// using xor on the register to be negated and the register with -1 in
 		// it, which is storing all 1's).
 		String rs = makeTempRegister(regCounter);
 		program.appendInstruction("addi " + rs 
 				+ ", $0, " + -1 
 				+ " # " + rs + " = "
 				+ "-1");
 
 		program.pushInt(rs);
 
 		// pop the -1 from stack 
 		program.popInt(rs);
 		// pop the value of the register to be negated from stack 
 		program.popInt(rd);
 
 		// do the xor
 		program.appendInstruction("xor " + rd + ", " + rd + ", "
 				+ rs + " # " + rd + " = " 
 				+ rd + "xor " + rs);
 
 		// push the result back onto the stack
 		program.pushInt(rd);
 		
 		regCounter = 0;
 	}
 
 	@Override
 	public void visit(Comparison node) 
 	{
 		String rd = makeTempRegister(regCounter++);
 		// push left
 		node.leftSide().accept(this);
 		String rs = makeTempRegister(regCounter);
 		// push right
 		node.rightSide().accept(this);
 		// pop right
 		program.popInt(rs);
 		// pop left
 		program.popInt(rd);
 		
 		// do comparison using pseudo-instructions
 		// first compute 
 		program.appendInstruction("sub " + rd + ", " + rd + ", " + rs);
 		
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(Dereference node) 
 	{
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(Index node) {
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
	public void visit(Assignment node) {
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(Call node) 
 	{
 		// caller setup:
 		// caller evaluate the arguments and push them onto stack for callee
 		node.arguments().accept(this);
 		
 		// after evaluating the arguments, and placing them on the stack, the
 		// caller makes a call to the function "func". The jal opcode 
 		// automatically changes the return address register, $ra to hold the 
 		// instruction immediately following itself. When "func" has finished,
 		// that's exactly where the current function will pick up again.
 		
 		// jump to callee
 		program.appendInstruction("jal " + program.funcLabel(node.function().name()));	
 		// The caller now picks up execution where it left off. The arguments
 		// provided to the caller are no longer needed, and can be popped off
 		// the stack
 		program.appendInstruction("addi $sp, $sp, "
 					+ ActivationRecord.numBytes(tc.getType(node.arguments())));
 	}
 	
 	
 
 	@Override
 	public void visit(IfElseBranch node) {
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(WhileLoop node) {
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(Return node) {
 		throw new RuntimeException("Implement this");
 	}
 
 	@Override
 	public void visit(ast.Error node) 
 	{
 		String message = "CodeGen cannot compile a " + node;
 		errorBuffer.append(message);
 		throw new CodeGenException(message);
 	}
 }
