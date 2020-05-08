 package de.psi.alloy4smt.ast;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import kodkod.instance.TupleFactory;
 import kodkod.instance.TupleSet;
 import edu.mit.csail.sdg.alloy4.ConstList;
 import edu.mit.csail.sdg.alloy4.ConstList.TempList;
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4.ErrorFatal;
 import edu.mit.csail.sdg.alloy4.ErrorSyntax;
 import edu.mit.csail.sdg.alloy4.Pair;
 import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
 import edu.mit.csail.sdg.alloy4compiler.ast.Command;
 import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
 import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
 import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprITE;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprLet;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
 import edu.mit.csail.sdg.alloy4compiler.ast.Type;
 import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
 import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
 
 
 public class IntRefPreprocessor {
     public final Sig.PrimSig intref;
     public final ConstList<CmdBundle> commands;
     public final ConstList<Sig> sigs;
     
     private static int getScope(Command command, Sig sig) {
 		CommandScope scope = command.getScope(sig);
 		int result;
 		if (scope != null) {
 			result = scope.endingScope;
 		} else if (sig.isOne != null || sig.isLone != null) {
 			result = 1;
 		} else {
 			result = command.overall < 0 ? 1 : command.overall;
 		}
 		return result;
     }
     
     public static String atomize(Sig sig, int id) {
     	String label = sig.label;
     	if (label.startsWith("this/")) {
     		label = label.substring(5);
     	}
     	return label + "$" + id;
     }
     	
     public static class IntrefSigRecord {
     	public final PrimSig sig;
     	public final Sig.Field mapfield;
     	public final ConstList<Sig> dependencies;
     	public final int instances;
     	
     	public IntrefSigRecord(PrimSig intexpr, Sig.Field mapfield, ConstList<Sig> dependencies, int instances) {
     		this.sig = intexpr;
     		this.mapfield = mapfield;
     		this.dependencies = dependencies;
     		this.instances = instances;
     	}
     	
     	public List<String> getAtoms() {
     		List<String> result = new Vector<String>();
     		for (int i = 0; i < instances; ++i) {
     			result.add(atomize(sig, i));
     		}
     		return result;
     	}
     	
     	private ConstList<Pair<Sig, Integer>> getDependencyScopes(Command command) {
     		TempList<Pair<Sig, Integer>> result = new TempList<Pair<Sig, Integer>>();
     		for (Sig sig : dependencies) {
     			result.add(new Pair<Sig, Integer>(sig, getScope(command, sig)));
     		}
     		return result.makeConst();
     	}
     	
     	public TupleSet getMapBounds(Command command, TupleFactory factory) {
     		TupleSet result = null;
     		
     		if (mapfield != null) {
     			final ConstList<Pair<Sig, Integer>> depscopes = getDependencyScopes(command);
     			final int depsize = depscopes.size();
     			int[] sigids = new int[depsize + 1];
     			result = factory.noneOf(depsize + 1);
     			addMapBoundsTuples(factory, result, depscopes, 0, sigids);
     		}
     		
     		return result;
     	}
     	
     	private void addMapBoundsTuples(TupleFactory factory, TupleSet result,
     			                        ConstList<Pair<Sig, Integer>> depscopes, int depthlvl,
     			                        int[] sigids) {
     		if (depthlvl < depscopes.size()) {
     			final int scope = depscopes.get(depthlvl).b;
     			for (int i = 0; i < scope; ++i) {
     				sigids[depthlvl + 1] = i;
     				addMapBoundsTuples(factory, result, depscopes, depthlvl + 1, sigids);
     			}
     		} else {
     			List<String> tuple = new Vector<String>();
     			tuple.add(atomize(sig, sigids[0]++));
     			for (int d = depscopes.size() - 1; d >= 0; --d) {
     				tuple.add(atomize(depscopes.get(d).a, sigids[d+1]));
     			}
     			result.add(factory.tuple(tuple));
     		}
     	}
     }
     
 	public static class CmdBundle {
 		public final Command command;
 		public final ConstList<String> hysatExprs;
 		public final ConstList<IntrefSigRecord> intrefRecords;
 		public final ConstList<Sig> sigs;
 		public final Sig.PrimSig intref;
 		
 		public CmdBundle(Command command, ConstList<String> hysatExprs, ConstList<IntrefSigRecord> intrefRecords,
 				ConstList<Sig> sigs, Sig.PrimSig intref) {
 			this.command = command;
 			this.hysatExprs = hysatExprs;
 			this.intrefRecords = intrefRecords;
 			this.sigs = sigs;
 			this.intref = intref;
 		}
 		
 		public CmdBundle(Command command, ConstList<Sig> sigs) {
 			this.command = command;
 			this.hysatExprs = null;
 			this.intrefRecords = null;
 			this.sigs = sigs;
 			this.intref = null;
 		}
 		
 		public ConstList<String> getIntrefAtoms() {
 			TempList<String> result = new TempList<String>();
 			for (IntrefSigRecord record : intrefRecords) {
 				result.addAll(record.getAtoms());
 			}
 			return result.makeConst();
 		}
 
 		public TupleSet getIntRefEqualsTupleSet(TupleFactory factory) {
 			final List<String> atoms = getIntrefAtoms();
 			final int numatoms = atoms.size();
 			TupleSet result = factory.noneOf(2);
 			
 			for (int i = 0; i < numatoms; ++i) {
 				for (int j = i + 1; j < numatoms; ++j) {
 					result.add(factory.tuple(atoms.get(i), atoms.get(j)));
 				}
 			}
 			
 			return result;
 		}
 	}
 
         
     private IntRefPreprocessor(Computer computer) throws Err {
     	intref = computer.intref;
     	sigs = computer.sigs.makeConst();
     	
     	TempList<CmdBundle> tmpCommands = new TempList<CmdBundle>();
     	for (int i = 0; i < computer.commands.size(); ++i) {
     		final IntexprSigBuilder isbuilder = new IntexprSigBuilder(computer.commands.get(i), intref);
     		final FactRewriter rewriter = FactRewriter.rewrite(computer.commands.get(i).formula, isbuilder);
         	final Command command = isbuilder.getModifiedCommand().change(rewriter.getFacts());
     		final TempList<IntrefSigRecord> l = new TempList<IntrefSigRecord>();
     		l.addAll(computer.intrefRecords.get(i));
     		l.addAll(isbuilder.getIntexprRecords());
     		final TempList<Sig> esigs = new TempList<Sig>();
     		esigs.addAll(sigs);
     		esigs.addAll(isbuilder.getIntExprSigs());
     		
     		tmpCommands.add(new CmdBundle(command, rewriter.getHysatExprs(), l.makeConst(), esigs.makeConst(), intref));
     	}
     	
     	commands = tmpCommands.makeConst();
     }
     
     private IntRefPreprocessor(CompModule module) {
     	intref = null;
     	sigs = module.getAllReachableSigs();
     	
     	TempList<CmdBundle> tmpCommands = new TempList<CmdBundle>();
     	for (Command c : module.getAllCommands()) {
     		tmpCommands.add(new CmdBundle(c, sigs));
     	}
     	
     	commands = tmpCommands.makeConst();
     }
     
 
     private static class Computer {
     	private ConstList<Command> oldcommands;
     	private Sig currentSig;
     	private Sig.Field currentField;
     	private Sig.Field lastfield = null;
     	private int fieldcnt = 0;
     	private Map<Command, Integer> factors;
     	private Map<Command, List<CommandScope>> newscopes;
     	private Map<Command, TempList<IntrefSigRecord>> tmpIntrefRecords;
     	private Map<PrimSig, PrimSig> old2newsigs;
     	private Map<Field, Field> old2newfields;
     	private List<PrimSig> newintrefs;
     	
     	public TempList<Sig> sigs;
     	public Sig.PrimSig intref;
     	public TempList<Command> commands;
     	public TempList<ConstList<IntrefSigRecord>> intrefRecords;
     	
     	public Computer(CompModule module, Sig.PrimSig intref) throws Err {
     		this.intref = intref;
     		sigs = new TempList<Sig>();
     		old2newsigs = new HashMap<PrimSig, PrimSig>();
     		old2newfields = new HashMap<Field, Field>();
     		oldcommands = module.getAllCommands();
     		commands = new TempList<Command>();
     		factors = new HashMap<Command, Integer>();
     		newscopes = new HashMap<Command, List<CommandScope>>();
     		newintrefs = new Vector<PrimSig>();
     		tmpIntrefRecords = new HashMap<Command, TempList<IntrefSigRecord>>();
     		intrefRecords = new TempList<ConstList<IntrefSigRecord>>();
     		
     		for (Command c: oldcommands) {
     			newscopes.put(c, new Vector<CommandScope>());
     			tmpIntrefRecords.put(c, new TempList<IntrefSigRecord>());
     		}
     		
     		for (Sig s: module.getAllReachableSigs()) {
     			if (s == intref) {
     				old2newsigs.put(intref, intref);
     				for (Field f : s.getFields())
     					old2newfields.put(f, f);
     			} else if (!s.builtin && s instanceof PrimSig) {
     				Attr[] attrs = new Attr[1];
     				PrimSig newsig = new PrimSig(s.label, s.attributes.toArray(attrs));
     				old2newsigs.put((PrimSig) s, newsig);
     			}
     		}
     		    		
     		for (Sig s: module.getAllReachableSigs()) {
    			if (s.builtin || s == intref) {
     				sigs.add(s);
     			} else {
     				sigs.add(convertSig(s));
     			}
     		}
     		
     		for (Sig s: module.getAllReachableSigs()) {
     			if (!s.builtin)
     				convertSigFacts(s);
     		}
     		
     		for (Command c: oldcommands) {
     			TempList<CommandScope> scopes = new TempList<CommandScope>();
     			for (CommandScope scope : c.scope) {
     				scopes.add(new CommandScope(old2newsigs.get(scope.sig), scope.isExact, scope.endingScope));
     			}
     			scopes.addAll(newscopes.get(c));
     			commands.add(c.change(scopes.makeConst()).change(EXPR_REWRITER.visitThis(c.formula)));
     			intrefRecords.add(tmpIntrefRecords.get(c).makeConst());
     		}
     	}
     	
     	public void addFactor(Sig factor) {
     		for (Command c: oldcommands) {
     			factors.put(c, factors.get(c) * getScope(c, factor));
     		}
     	}
     	
     	private void resetFactors() {
     		for (Command c: oldcommands) {
     			factors.put(c, 1);
     		}
     	}
 
     	public Sig makeSig() throws Err {
     		if (lastfield != currentField) {
     			fieldcnt = 0;
     			lastfield = currentField;
     		} else {
     			fieldcnt++;
     			throw new ErrorFatal(currentField.pos, "unsupported decl");
     		}
     		String label = currentSig.label + "_" + currentField.label + "_IntRef";
     		PrimSig sig = new Sig.PrimSig(label, intref);
 			newintrefs.add(sig);
 			
 			return sig;
     	}
     	
     	private void integrateNewIntRefSigs() throws ErrorSyntax {
     		for (PrimSig sig: newintrefs) {
 	    		for (Command c: oldcommands) {
 	    			final int scope = factors.get(c);
 	    			newscopes.get(c).add(new CommandScope(sig, true, scope));
 	    			tmpIntrefRecords.get(c).add(new IntrefSigRecord(sig, null, null, scope));
 	    		}
 	    		sigs.add(sig);
     		}
     		newintrefs.clear();
     	}
     	
     	private final ExprRewriter EXPR_REWRITER = new ExprRewriter();
     	
         private Sig convertSig(Sig sig) throws Err {
         	final Sig newSig = old2newsigs.get(sig);        	
         	
         	currentSig = sig;
         	
         	for (Sig.Field field: sig.getFields()) {
             	resetFactors();
             	addFactor(sig);
             	
         		currentField = field;
         		final Expr newExpr = EXPR_REWRITER.visitThis(field.decl().expr);
         		final Field[] newField = newSig.addTrickyField(
         				field.pos, field.isPrivate, null, null, field.isMeta, 
         				new String[] { field.label }, newExpr);
         		old2newfields.put(field, newField[0]);
         		
         		integrateNewIntRefSigs();
         	}
         	
         	return newSig;
         }
         
         private void convertSigFacts(Sig sig) throws Err {
         	final Sig newSig = old2newsigs.get(sig);
         	
         	for (Expr fact : sig.getFacts()) {
         		newSig.addFact(EXPR_REWRITER.visitThis(fact));
         	}
         }
 
         private class ExprRewriter extends VisitReturn<Expr> {
 
 			@Override
 			public Expr visit(ExprBinary x) throws Err {
 				return x.op.make(x.pos, x.closingBracket, visitThis(x.left), visitThis(x.right));
 			}
 
 			@Override
 			public Expr visit(ExprList x) throws Err {
 				List<Expr> args = new Vector<Expr>();
 				for (Expr a : x.args) args.add(visitThis(a));
 				return ExprList.make(x.pos, x.closingBracket, x.op, args);
 			}
 
 			@Override
 			public Expr visit(ExprCall x) throws Err {
 				return x;
 			}
 
 			@Override
 			public Expr visit(ExprConstant x) throws Err {
 				return x;
 			}
 
 			@Override
 			public Expr visit(ExprITE x) throws Err {
 				return ExprITE.make(x.pos, visitThis(x.cond), visitThis(x.left), visitThis(x.right));
 			}
 
 			@Override
 			public Expr visit(ExprLet x) throws Err {
 				return ExprLet.make(x.pos, x.var, visitThis(x.expr), visitThis(x.sub));
 			}
 
 			@Override
 			public Expr visit(ExprQt x) throws Err {
 				List<Decl> decls = new Vector<Decl>();
 				for (Decl d : x.decls) {
 					decls.add(new Decl(d.isPrivate, d.disjoint, d.disjoint2, d.names, visitThis(d.expr)));
 				}
 				return x.op.make(x.pos, x.closingBracket, decls, visitThis(x.sub));
 			}
 
 			@Override
 			public Expr visit(ExprUnary x) throws Err {
 				return x.op.make(x.pos, visitThis(x.sub));
 			}
 
 			@Override
 			public Expr visit(ExprVar x) throws Err {
 				return x;
 			}
 
 			@Override
 			public Expr visit(Sig x) throws Err {
 				Sig s;
 				if (x == Sig.SIGINT) {
 					s = makeSig();
 				} else {
 					s = old2newsigs.get(x);
 					if (s == null) s = x;
 					addFactor(x);
 				}
 				return s;
 			}
 
 			@Override
 			public Expr visit(Field x) throws Err {
 				Field f = old2newfields.get(x);
 				if (f == null) throw new AssertionError();
 				return f;
 			}
         	
         }
 
     }
     
     private static class IntexprSigBuilder {
     	
     	private LinkedHashMap<ExprVar, Expr> freeVars;
     	private Context ctx;
     	
     	private static class Context {
     		public int id = 0;
     		public PrimSig intref;
     		public Sig.Field aqclass;
     		public List<IntrefSigRecord> records = new Vector<IntrefSigRecord>();
     		public Command command;
     	}
     	
     	public IntexprSigBuilder(Command command, PrimSig intref) {
     		ctx = new Context();
     		ctx.command = command;
     		ctx.intref = intref;
     		ctx.aqclass = Helpers.getFieldByName(intref.getFields(), "aqclass");
     		freeVars = new LinkedHashMap<ExprVar, Expr>();
 		}
     	
     	private IntexprSigBuilder(IntexprSigBuilder other) {
     		ctx = other.ctx;
     		freeVars = new LinkedHashMap<ExprVar, Expr>(other.freeVars);
     	}
     	
     	public Pair<IntrefSigRecord, Expr> make(Expr intrefExpr) throws Err {
     		final PrimSig intexprsig = new PrimSig("IntExpr" + ctx.id++, ctx.intref);
     		final Set<ExprVar> usedFreeVars = FreeVarFinder.find(intrefExpr);
     		final Expr right = ExprBinary.Op.JOIN.make(null, null, intrefExpr, ctx.aqclass);
     		Expr left;
     		Sig.Field mapfield = null;
     		List<Sig> dependencies = new Vector<Sig>();
     		int instances = 1;
     		
     		if (!usedFreeVars.isEmpty()) {
     			Type type = null;
     			for (ExprVar var : usedFreeVars) {
     				final Expr e = freeVars.get(var);
     				if (type == null) {
     					type = e.type();
     				} else {
     					type = e.type().product(type);
     				}
     			}
                mapfield = intexprsig.addField("map", type.toExpr());
     			
     			for (List<PrimSig> ss : type.fold()) {
     				int ssinst = 1;
     				for (PrimSig sig : ss) {
     					ssinst *= getScope(ctx.command, sig);
     					dependencies.add(0, sig);
     				}
     				instances *= ssinst;
     			}
     			
     			Expr mapjoin = mapfield;
     			for (ExprVar var : usedFreeVars) {
     				mapjoin = ExprBinary.Op.JOIN.make(null, null, mapjoin, var);
     			}
     			
     			left = ExprBinary.Op.JOIN.make(null, null, mapjoin, ctx.aqclass);
     		} else {
     			left = ExprBinary.Op.JOIN.make(null, null, intexprsig, ctx.aqclass);
     		}
     		
     		IntrefSigRecord result = new IntrefSigRecord(intexprsig, mapfield, ConstList.make(dependencies), instances);
     		
     		ctx.command = ctx.command.change(intexprsig, true, instances);
     		ctx.records.add(result);
     		
     		return new Pair<IntrefSigRecord, Expr>(result, ExprBinary.Op.EQUALS.make(null, null, left, right));
     	}
     	
     	public IntexprSigBuilder addFreeVariables(ConstList<Decl> decls) {
     		IntexprSigBuilder result = new IntexprSigBuilder(this);
     		
     		for (Decl d : decls) {
     			for (ExprHasName ehn : d.names) {
     				result.freeVars.put((ExprVar) ehn, d.expr);
     			}
     		}
     		
     		return result;
     	}
     	
     	public Command getModifiedCommand() {
     		return ctx.command;
     	}
     	
     	public List<IntrefSigRecord> getIntexprRecords() {
     		return ctx.records;
     	}
     	
     	public List<PrimSig> getIntExprSigs() {
     		List<PrimSig> result = new Vector<PrimSig>();
     		for (IntrefSigRecord record : ctx.records) {
     			result.add(record.sig);
     		}
     		return result;
     	}    	
     }
     
     private static class FreeVarFinder extends VisitQuery<Object> {
     	private Set<ExprVar> freeVars = new LinkedHashSet<ExprVar>();
 
 		@Override
 		public Object visit(ExprVar x) throws Err {
 			freeVars.add(x);
 			return super.visit(x);
 		}
 		
 		private FreeVarFinder() {
 		}
 		
 		public static Set<ExprVar> find(Expr x) throws Err {
 			FreeVarFinder finder = new FreeVarFinder();
 			finder.visitThis(x);
 			return finder.freeVars;
 		}
     }
     
     private static class IntExprHandler extends VisitReturn<TempList<String>> {
     	
     	private List<Expr> facts;
     	private IntexprSigBuilder builder;
     	private boolean cast2intSeen;
     	
     	public IntExprHandler(IntexprSigBuilder isb) {
 			this.facts = new Vector<Expr>();
 			this.builder = isb;
 			this.cast2intSeen = false;
 		}
     	
     	public Expr getFacts() {
     		return ExprList.make(null, null, ExprList.Op.AND, facts);
     	}
     	
     	private void throwUnsupportedOperator(Expr x) throws Err {
     		throw new ErrorSyntax(x.pos, "HySAT does not support this operator.");
     	}
 
 		@Override
 		public TempList<String> visit(ExprUnary x) throws Err {
 			TempList<String> result;
 			
 			if (x.op == ExprUnary.Op.CAST2INT) {
 				cast2intSeen = true;
 				result = visitThis(x.sub);
 				cast2intSeen = false;
 			} else {	
 				final TempList<String> sub = visitThis(x.sub);
 				switch (x.op) {
 				case NOOP: result = sub; break;
 				default:
 					throw new AssertionError();
 				}
 			}
 
 			return result;
 		}
 
 		@Override
 		public TempList<String> visit(ExprBinary x) throws Err {
 			if (cast2intSeen && x.op == ExprBinary.Op.JOIN) {
 				Pair<IntrefSigRecord, Expr> result = builder.make(x);
 				facts.add(result.b);
 				return new TempList<String>(result.a.getAtoms());
 			} else {
 				final TempList<String> left = visitThis(x.left);
 				final TempList<String> right = visitThis(x.right);
 				String op = null;
 				
 				switch (x.op) {
 				case GT:         op = ">"; break;
 				case LT:         op = "<"; break;
 				case GTE:        op = ">="; break;
 				case LTE:        op = "<="; break;
 				case EQUALS:     op = "="; break;
 				case MINUS:      op = "-"; break;
 				case MUL:        op = "*"; break;
 				case NOT_EQUALS: op = "!="; break;
 				case NOT_GT:     op = "<="; break;
 				case NOT_LT:     op = ">="; break;
 				case NOT_GTE:    op = "<"; break;
 				case NOT_LTE:    op = ">"; break;
 				case PLUS:       op = "+"; break;
 				
 				default:
 					throwUnsupportedOperator(x);
 				}
 				
 				TempList<String> result = new TempList<String>();
 				for (String l : left.makeConst()) {
 					for (String r : right.makeConst()) {
 						result.add("(" + l + " " + op + " " + r + ")");
 					}
 				}
 				return result;
 			}
 		}
 
 		@Override
 		public TempList<String> visit(ExprConstant x) throws Err {
 			if (x.op == ExprConstant.Op.NUMBER) {
 				return new TempList<String>(String.valueOf(x.num));
 			} else {
 				throw new ErrorSyntax(x.pos, "Constant not convertible to HySAT");
 			}
 		}
 
 		@Override
 		public TempList<String> visit(ExprList x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(ExprCall x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(ExprITE x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(ExprLet x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(ExprQt x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(ExprVar x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(Sig x) throws Err {
 			throw new AssertionError();
 		}
 
 		@Override
 		public TempList<String> visit(Field x) throws Err {
 			throw new AssertionError();
 		}
 
     }
     
     private static class FactRewriter extends VisitReturn<Expr> {
     	
     	private IntexprSigBuilder intexprBuilder;
 
     	private Expr rewritten;
     	private TempList<String> hysatexprs;
     	
     	private FactRewriter(IntexprSigBuilder builder) {
     		hysatexprs = new TempList<String>();
     		intexprBuilder = builder;
 		}
     	
     	public static FactRewriter rewrite(Expr expr, IntexprSigBuilder builder) throws Err {
     		FactRewriter rewriter = new FactRewriter(builder);
     		rewriter.rewritten = rewriter.visitThis(expr);
     		return rewriter;
     	}
     	
     	public Expr getFacts() {
     		return rewritten;
     	}
     	
     	public ConstList<String> getHysatExprs() {
     		return hysatexprs.makeConst();
     	}
     	
 		@Override
 		public Expr visit(ExprBinary x) throws Err {
 			Expr result = null;
 			
 			if (x.left.type().is_int && x.right.type().is_int) {
 				final IntExprHandler ieh = new IntExprHandler(intexprBuilder);
 				final TempList<String> hexpr = ieh.visitThis(x);
 				hysatexprs.addAll(hexpr.makeConst());
 				result = ieh.getFacts();
 			} else {
 				final Expr left = visitThis(x.left);
 				final Expr right = visitThis(x.right);
 				result = x.op.make(x.pos, x.closingBracket, left, right);					
 			}
 			
 			return result;
 		}
 
 		@Override
 		public Expr visit(ExprList x) throws Err {
 			TempList<Expr> args = new TempList<Expr>();
 			for (Expr e: x.args) {
 				args.add(visitThis(e));
 			}
 			return ExprList.make(x.pos, x.closingBracket, x.op, args.makeConst());
 		}
 
 		@Override
 		public Expr visit(ExprCall x) throws Err {
 			TempList<Expr> args = new TempList<Expr>();
 			for (Expr e: x.args) {
 				args.add(visitThis(e));
 			}
 			return ExprCall.make(x.pos, x.closingBracket, x.fun, args.makeConst(), x.extraWeight);
 		}
 
 		@Override
 		public Expr visit(ExprConstant x) throws Err {
 			return x;
 		}
 
 		@Override
 		public Expr visit(ExprITE x) throws Err {
 			Expr cond = visitThis(x.cond);
 			Expr left = visitThis(x.left);
 			Expr right = visitThis(x.right);
 			return ExprITE.make(x.pos, cond, left, right);
 		}
 
 		@Override
 		public Expr visit(ExprLet x) throws Err {
 			Expr sub = visitThis(x.sub);
 			return ExprLet.make(x.pos, x.var, x.expr, sub);
 		}
 
 		@Override
 		public Expr visit(ExprQt x) throws Err {
 			final IntexprSigBuilder tmpold = intexprBuilder;
 			intexprBuilder = tmpold.addFreeVariables(x.decls);
 			Expr sub = visitThis(x.sub);
 			intexprBuilder = tmpold;
 			return x.op.make(x.pos, x.closingBracket, x.decls, sub);
 		}
 
 		@Override
 		public Expr visit(ExprUnary x) throws Err {
 			final Expr sub = visitThis(x.sub);
 			if (x.op == ExprUnary.Op.CAST2INT) {
 				throw new AssertionError();
 			} else {
 				return x.op.make(x.pos, sub);
 			}
 		}
 
 		@Override
 		public Expr visit(ExprVar x) throws Err {
 			return x;
 		}
 
 		@Override
 		public Expr visit(Sig x) throws Err {
 			return x;
 		}
 
 		@Override
 		public Expr visit(Field x) throws Err {
 			return x;
 		}
     	
     }
     
 
     public static IntRefPreprocessor processModule(CompModule module) throws Err {
     	final Sig.PrimSig intref = (Sig.PrimSig) Helpers.getSigByName(module.getAllReachableSigs(), "intref/IntRef");
     	if (intref != null) {
     		final Computer computer = new Computer(module, intref);
     		return new IntRefPreprocessor(computer);
     	} else {
     		return new IntRefPreprocessor(module);
     	}
     }
 
 }
