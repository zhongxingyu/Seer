 package edu.tum.lua;
 
 import static edu.tum.lua.LuaInterpreter.eval;
 
 import java.util.List;
 
 import edu.tum.lua.ast.Asm;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.DoExp;
 import edu.tum.lua.ast.Exp;
 import edu.tum.lua.ast.ForExp;
 import edu.tum.lua.ast.ForIn;
 import edu.tum.lua.ast.FuncCallStmt;
 import edu.tum.lua.ast.FunctionDef;
 import edu.tum.lua.ast.IfThenElse;
 import edu.tum.lua.ast.LegacyAdapter;
 import edu.tum.lua.ast.LocalDecl;
 import edu.tum.lua.ast.LocalFuncDef;
 import edu.tum.lua.ast.RepeatUntil;
 import edu.tum.lua.ast.VisitorAdaptor;
 import edu.tum.lua.ast.WhileExp;
 import edu.tum.lua.operator.logical.LogicalOperatorSupport;
 
 public class StatementVisitor extends VisitorAdaptor {
 
 	private final Environment environment;
 
 	public StatementVisitor(Environment e) {
 		this.environment = e;
 	}
 
 	public Environment getEnvironment() {
 		return environment;
 	}
 
 	@Override
 	public void visit() {
 		throw new RuntimeException("Unsupported Statement");
 	}
 
 	@Override
 	public void visit(Asm stmt) {
 		List<String> varlist = LegacyAdapter.convert(stmt.varlist);
 		List<Exp> explist = LegacyAdapter.convert(stmt.explist);
 		environment.assign(varlist, explist);
 	}
 
 	@Override
 	public void visit(DoExp stmt) {
 		executeBlock(stmt.block);
 	}
 
 	@Override
 	public void visit(ForExp stmt) {
 
 	}
 
 	@Override
 	public void visit(ForIn stmt) {
 
 	}
 
 	@Override
 	public void visit(FuncCallStmt stmt) {
 		ExpVisitor visitor = new ExpVisitor(environment);
		stmt.call.accept(visitor);
 	}
 
 	@Override
 	public void visit(FunctionDef stmt) {
 
 	}
 
 	@Override
 	public void visit(IfThenElse stmt) {
 		if (isTrue(stmt.ifexp)) {
 			executeBlock(stmt.thenblock);
 		} else {
 			executeBlock(stmt.elseblock);
 		}
 	}
 
 	@Override
 	public void visit(LocalDecl stmt) {
 
 	}
 
 	@Override
 	public void visit(LocalFuncDef stmt) {
 
 	}
 
 	@Override
 	public void visit(RepeatUntil stmt) {
 		do {
 			executeBlock(stmt.block);
 		} while (!isTrue(stmt.exp));
 	}
 
 	@Override
 	public void visit(WhileExp stmt) {
 		while (isTrue(stmt.exp)) {
 			executeBlock(stmt.block);
 		}
 	}
 
 	private void executeBlock(Block block) {
 		Environment blockEnvironment = new Environment(environment);
 		LuaInterpreter.eval(block, blockEnvironment);
 
 		// TODO: handle return / break;
 	}
 
 	private boolean isTrue(Exp exp) {
 		return LogicalOperatorSupport.isTrue(eval(exp, environment));
 	}
 }
