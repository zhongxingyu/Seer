 package edu.tum.lua;
 
 import static edu.tum.lua.ast.LegacyAdapter.convert;
 
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.tum.lua.ast.Asm;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.DoExp;
 import edu.tum.lua.ast.Exp;
 import edu.tum.lua.ast.ForExp;
 import edu.tum.lua.ast.ForIn;
 import edu.tum.lua.ast.FuncCallStmt;
 import edu.tum.lua.ast.IfThenElse;
 import edu.tum.lua.ast.LastBreak;
 import edu.tum.lua.ast.LastReturn;
 import edu.tum.lua.ast.LegacyAdapter;
 import edu.tum.lua.ast.LocalDecl;
 import edu.tum.lua.ast.LocalFuncDef;
 import edu.tum.lua.ast.RepeatUntil;
 import edu.tum.lua.ast.Stat;
 import edu.tum.lua.ast.Var;
 import edu.tum.lua.ast.VarTabIndex;
 import edu.tum.lua.ast.Variable;
 import edu.tum.lua.ast.VisitorAdaptor;
 import edu.tum.lua.ast.WhileExp;
 import edu.tum.lua.exceptions.LuaRuntimeException;
 import edu.tum.lua.operator.logical.LogicalOperatorSupport;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaFunctionInterpreted;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class BlockVisitor extends VisitorAdaptor {
 
 	private LocalEnvironment environment;
 	private final List<Object> vararg;
 	private boolean Break;
 	private List<Object> Return;
 
 	public BlockVisitor(LocalEnvironment e) {
 		this(e, null);
 	}
 
 	public BlockVisitor(LocalEnvironment e, List<Object> v) {
 		this.environment = e;
 		vararg = v;
 		Break = false;
 		Return = null;
 	}
 
 	public LocalEnvironment getEnvironment() {
 		return environment;
 	}
 
 	@Override
 	public void visit() {
 		throw new RuntimeException("Unsupported Statement");
 	}
 
 	private interface StoreLocation {
 		void store(Object object);
 	}
 
 	private static class EnvironmentLocation implements StoreLocation {
 
 		private final LocalEnvironment environment;
 		private final String ident;
 
 		protected EnvironmentLocation(LocalEnvironment e, String i) {
 			environment = e;
 			ident = i;
 		}
 
 		@Override
 		public void store(Object object) {
 			environment.set(ident, object);
 		}
 	}
 
 	private static class TableLocation implements StoreLocation {
 		private final LuaTable environment;
 		private final Object key;
 
 		protected TableLocation(LuaTable e, Object k) {
 			if (k == null) {
 				throw new IllegalArgumentException();
 			}
 
 			environment = e;
 
 			key = k;
 		}
 
 		@Override
 		public void store(Object object) {
 			environment.set(key, object);
 		}
 	}
 
 	@Override
 	public void visit(Asm stmt) {
 		ExpVisitor visitor = new ExpVisitor(environment, vararg);
 		stmt.explist.accept(visitor);
 
 		Enumeration<Var> identifierIterator = stmt.varlist.elements();
 		Iterator<Object> valuesIterator = visitor.popAll().iterator();
 
 		Deque<StoreLocation> locations = new LinkedList<>();
 
 		while (identifierIterator.hasMoreElements()) {
 			Var identifier = identifierIterator.nextElement();
 
 			if (identifier instanceof Variable) {
 				locations.addLast(new EnvironmentLocation(environment, ((Variable) identifier).var));
 			} else /* VarTabIndex */{
 				VarTabIndex varTabIndex = (VarTabIndex) identifier;
 				varTabIndex.preexp.accept(visitor);
 
 				Object object = visitor.popLast();
 
 				if (LuaType.getTypeOf(object) != LuaType.TABLE) {
 					throw new LuaRuntimeException(varTabIndex, "attempt to index a "
 							+ LuaType.getTypeOf(LuaType.getTypeOf(object)));
 				}
 
 				LuaTable table = (LuaTable) object;
 				varTabIndex.indexexp.accept(visitor);
 				Object key = visitor.popLast();
 				locations.addLast(new TableLocation(table, key));
 			}
 		}
 
 		for (StoreLocation location : locations) {
 			Object value = valuesIterator.hasNext() ? valuesIterator.next() : null;
 			location.store(value);
 		}
 	}
 
 	@Override
 	public void visit(Block block) {
 		BlockVisitor visitor = new BlockVisitor(environment, vararg);
 
 		for (Stat statement : convert(block.stats)) {
 			statement.accept(visitor);
 
 			if (visitor.Break) {
 				Break = true;
 				return;
 			}
 
 			if (visitor.Return != null) {
 				Return = visitor.Return;
 				return;
 			}
 		}
 
 		if (block.last == null) {
 			return;
 		}
 
 		if (block.last instanceof LastBreak) {
 			Break = true;
 			return;
 		}
 
 		LastReturn last = (LastReturn) block.last;
 		ExpVisitor visit = new ExpVisitor(environment, vararg);
 		last.explist.accept(visit);
 		Return = visit.popAll();
 	}
 
 	@Override
 	public void visit(DoExp stmt) {
 		stmt.block.accept(this);
 	}
 
 	/**
 	 * - Lua Pseudocode:
 	 * 
 	 * for v = e1, e2, e3 do block end
 	 * 
 	 * is equivalent to the code:
 	 * 
 	 * do local var, limit, step = tonumber(e1), tonumber(e2), tonumber(e3) if
 	 * not (var and limit and step) then error() end while (step > 0 and var <=
 	 * limit) or (step <= 0 and var >= limit) do local v = var block var = var +
 	 * step end end // @formatter:on
 	 */
 	@Override
 	public void visit(ForExp stmt) {
 		ExpVisitor visitor = new ExpVisitor(environment, vararg);
 
 		stmt.start.accept(visitor);
 		double var = (double) visitor.popLast();
 
 		stmt.end.accept(visitor);
 		double limit = (double) visitor.popLast();
 
 		stmt.step.accept(visitor);
 		double step = (double) visitor.popLast();
 
 		while ((step > 0.0 && var <= limit) || step <= 0 && var >= limit) {
 			LocalEnvironment localEnvironment = new LocalEnvironment(environment);
 			localEnvironment.setLocal(stmt.ident, var);
 
 			BlockVisitor blockVisitor = new BlockVisitor(localEnvironment, vararg);
 			stmt.block.accept(blockVisitor);
 
 			if (blockVisitor.Break) {
 				break;
 			}
 
 			if (blockVisitor.Return != null) {
 				Return = blockVisitor.Return;
 				return;
 			}
 
 			var += step;
 		}
 	}
 
 	@Override
 	public void visit(ForIn stmt) {
 		ExpVisitor visitor = new ExpVisitor(environment, vararg);
 
 		List<String> nl = LegacyAdapter.convert(stmt.namelist);
 
 		stmt.explist.accept(visitor);
 		List<Object> expl = visitor.popAll();
 
 		LuaFunction func = (LuaFunction) expl.get(0);
 		Object state = expl.get(1);
 		Object var = expl.get(2);
 
 		while (true) {
 			LocalEnvironment localEnvironment = new LocalEnvironment(environment);
 
 			List<Object> results = ExpVisitor.call(func, Arrays.asList(state, var));
 
 			final int lastExpIndex = results.size() - 1;
 
 			for (int i = 0; i < nl.size(); i++) {
 				Object resultValue = (i <= lastExpIndex) ? results.get(i) : null;
 				localEnvironment.setLocal(nl.get(i), resultValue);
 			}
 
 			var = localEnvironment.get(nl.get(0));
 
 			if (LuaType.getTypeOf(var) == LuaType.NIL) {
 				break;
 			}
 
 			BlockVisitor blockVisitor = new BlockVisitor(localEnvironment, vararg);
 			stmt.block.accept(blockVisitor);
 
 			if (blockVisitor.Break) {
 				break;
 			}
 
 			if (blockVisitor.Return != null) {
 				Return = blockVisitor.Return;
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void visit(FuncCallStmt stmt) {
 		ExpVisitor visitor = new ExpVisitor(environment, vararg);
 		stmt.call.accept(visitor);
 	}
 
 	@Override
 	public void visit(IfThenElse stmt) {
 		if (isTrue(stmt.ifexp)) {
 			stmt.thenblock.accept(this);
 		} else {
 			stmt.elseblock.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(LocalDecl stmt) {
 		environment = new LocalEnvironment(environment);
 
 		ExpVisitor visitor = new ExpVisitor(environment, vararg);
 		stmt.explist.accept(visitor);
 
 		Iterator<String> identifierIterator = LegacyAdapter.convert(stmt.namelist).iterator();
 		Iterator<Object> valuesIterator = visitor.popAll().iterator();
 
 		while (identifierIterator.hasNext()) {
 			String identifier = identifierIterator.next();
 			Object value = valuesIterator.hasNext() ? valuesIterator.next() : null;
 			environment.setLocal(identifier, value);
 		}
 	}
 
 	@Override
 	public void visit(LocalFuncDef stmt) {
 		LocalEnvironment oldLocalEnvironment = environment;
 
 		/* Enable recursive calls */
 		oldLocalEnvironment.setLocal(stmt.name, null);
 		LuaFunction function = new LuaFunctionInterpreted(stmt, oldLocalEnvironment);
 		oldLocalEnvironment.setLocal(stmt.name, function);
 
 		environment = new LocalEnvironment(oldLocalEnvironment);
 	}
 
 	@Override
 	public void visit(RepeatUntil stmt) {
 		do {
 			stmt.block.accept(this);
 
 			if (Break) {
				Break = false;
 				break;
 			}
 
 			if (Return != null) {
 				return;
 			}
 		} while (!isTrue(stmt.exp));
 	}
 
 	@Override
 	public void visit(WhileExp stmt) {
 		while (isTrue(stmt.exp)) {
 			stmt.block.accept(this);
 
 			if (Break) {
				Break = false;
 				break;
 			}
 
 			if (Return != null) {
 				return;
 			}
 		}
 	}
 
 	private boolean isTrue(Exp exp) {
 		ExpVisitor visitor = new ExpVisitor(environment, vararg);
 		exp.accept(visitor);
 		return LogicalOperatorSupport.isTrue(visitor.popLast());
 	}
 
 	public List<Object> getReturn() {
 		return Return;
 	}
 
 	public boolean getBreak() {
 		return Break;
 	}
 }
