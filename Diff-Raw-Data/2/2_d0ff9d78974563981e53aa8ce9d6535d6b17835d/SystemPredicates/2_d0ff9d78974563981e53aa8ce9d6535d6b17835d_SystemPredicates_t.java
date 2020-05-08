 package suite.lp.predicate;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import suite.lp.doer.Cloner;
 import suite.lp.doer.Prover;
 import suite.node.Atom;
 import suite.node.Data;
 import suite.node.Node;
 import suite.node.Reference;
 import suite.node.Tree;
 import suite.node.io.Operator;
 import suite.node.io.TermParser.TermOp;
 import suite.util.CacheUtil;
 import suite.util.FunUtil.Fun;
 import suite.util.FunUtil.Source;
 import suite.util.Pair;
 
 public class SystemPredicates {
 
 	public interface SystemPredicate {
 		public boolean prove(Prover prover, Node parameter);
 	}
 
 	private Map<String, SystemPredicate> predicates = new HashMap<>();
 
 	private Prover prover;
 
 	public SystemPredicates(Prover prover) {
 		this.prover = prover;
 
 		addPredicate("cut.begin", new CutBegin());
 		addPredicate("cut.end", new CutEnd());
 		addPredicate("find.all", new FindAll());
 		addPredicate("memoize", new Memoize());
 		addPredicate("not", new Not());
 		addPredicate("once", new Once());
 		addPredicate("system.predicate", new SystemPredicate_());
 		addPredicate("temporary", new Temporary());
 
 		addPredicate("bound", new EvalPredicates.Bound());
 		addPredicate("clone", new EvalPredicates.Clone());
 		addPredicate("complexity", new EvalPredicates.ComplexityPredicate());
 		addPredicate("contains", new EvalPredicates.Contains());
 		addPredicate("eval.fun", new EvalPredicates.EvalFun());
 		addPredicate("eval.js", new EvalPredicates.EvalJs());
 		addPredicate(TermOp.LE____, new EvalPredicates.Compare());
 		addPredicate(TermOp.LT____, new EvalPredicates.Compare());
 		addPredicate(TermOp.NOTEQ_, new EvalPredicates.NotEquals());
 		addPredicate(TermOp.GE____, new EvalPredicates.Compare());
 		addPredicate(TermOp.GT____, new EvalPredicates.Compare());
 		addPredicate("generalize", new EvalPredicates.Generalize());
 		addPredicate("generalize.prefix", new EvalPredicates.GeneralizeWithPrefix());
 		addPredicate("hash", new EvalPredicates.Hash());
 		addPredicate("is.cyclic", new EvalPredicates.IsCyclic());
 		addPredicate("let", new EvalPredicates.Let());
 		addPredicate("random", new EvalPredicates.RandomPredicate());
 		addPredicate("replace", new EvalPredicates.ReplacePredicate());
 		addPredicate("same", new EvalPredicates.Same());
 		addPredicate("specialize", new EvalPredicates.Specialize());
 		addPredicate("temp", new EvalPredicates.Temp());
 		addPredicate("tree", new EvalPredicates.TreePredicate());
 
 		addPredicate("concat", new FormatPredicates.Concat());
 		addPredicate("is.atom", new FormatPredicates.IsAtom());
 		addPredicate("is.int", new FormatPredicates.IsInt());
 		addPredicate("is.string", new FormatPredicates.IsString());
 		addPredicate("is.tree", new FormatPredicates.IsTree());
 		addPredicate("parse", new FormatPredicates.Parse());
 		addPredicate("pretty.print", new FormatPredicates.PrettyPrint());
 		addPredicate("rpn", new FormatPredicates.Rpn());
 		addPredicate("starts.with", new FormatPredicates.StartsWith());
 		addPredicate("string.length", new FormatPredicates.StringLength());
 		addPredicate("substring", new FormatPredicates.Substring());
 		addPredicate("to.atom", new FormatPredicates.ToAtom());
 		addPredicate("to.dump.string", new FormatPredicates.ToDumpString());
 		addPredicate("to.int", new FormatPredicates.ToInt());
 		addPredicate("to.string", new FormatPredicates.ToString());
 		addPredicate("treeize", new FormatPredicates.Treeize());
 		addPredicate("trim", new FormatPredicates.Trim());
 
 		addPredicate("dump", new IoPredicates.Dump());
 		addPredicate("dump.stack", new IoPredicates.DumpStack());
 		addPredicate("exec", new IoPredicates.Exec());
 		addPredicate("exit", new IoPredicates.Exit());
 		addPredicate("file.exists", new IoPredicates.FileExists());
 		addPredicate("file.read", new IoPredicates.FileRead());
 		addPredicate("file.write", new IoPredicates.FileWrite());
 		addPredicate("home.dir", new IoPredicates.HomeDir());
 		addPredicate("log", new IoPredicates.Log());
 		addPredicate("nl", new IoPredicates.Nl());
 		addPredicate("sink", new IoPredicates.Sink());
 		addPredicate("source", new IoPredicates.Source());
 		addPredicate("write", new IoPredicates.Write());
 
 		addPredicate("assert", new RuleSetPredicates.Assertz());
 		addPredicate("asserta", new RuleSetPredicates.Asserta());
 		addPredicate("assertz", new RuleSetPredicates.Assertz());
 		addPredicate("import", new RuleSetPredicates.Import());
 		addPredicate("import.file", new RuleSetPredicates.ImportFile());
 		addPredicate("list", new RuleSetPredicates.ListPredicates());
 		addPredicate("retract", new RuleSetPredicates.Retract());
 		addPredicate("retract.all", new RuleSetPredicates.RetractAll());
 		addPredicate("rules", new RuleSetPredicates.GetAllRules());
 		addPredicate("with", new RuleSetPredicates.With());
 	}
 
 	public Boolean call(Node query) {
 		SystemPredicate predicate;
 		Tree tree;
 		String name = null;
 		Node pass = query;
 
 		if (query instanceof Atom) {
 			name = ((Atom) query).getName();
 			pass = Atom.NIL;
 		} else if ((tree = Tree.decompose(query)) != null)
 			if (tree.getOperator() != TermOp.TUPLE_)
 				name = tree.getOperator().getName();
 			else {
 				Node left = tree.getLeft();
 
 				if (left instanceof Atom) {
 					name = ((Atom) left).getName();
 					pass = tree.getRight();
 				}
 			}
 
 		predicate = name != null ? predicates.get(name) : null;
 		return predicate != null ? predicate.prove(prover, pass) : null;
 	}
 
 	private void addPredicate(Operator operator, SystemPredicate pred) {
 		predicates.put(operator.getName(), pred);
 	}
 
 	private void addPredicate(String name, SystemPredicate pred) {
 		predicates.put(name, pred);
 	}
 
 	private class CutBegin implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return prover.bind(ps, prover.getAlternative());
 		}
 	}
 
 	private class CutEnd implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			prover.setAlternative(ps.finalNode());
 			return true;
 		}
 	}
 
 	private class FindAll implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			Node params[] = Node.tupleToArray(ps, 3);
 			return prover.bind(params[2], findAll(prover, params[0], params[1]));
 		}
 	}
 
 	private class Memoize implements SystemPredicate {
 		private Reference uniqueReference = new Reference();
 
 		private Fun<Pair<Node, Node>, Node> findAll = new CacheUtil().proxy(new Fun<Pair<Node, Node>, Node>() {
 			public Node apply(Pair<Node, Node> pair) {
 				return findAll(prover, pair.t0, pair.t1);
 			}
 		});
 
 		public boolean prove(Prover prover, Node ps) {
 			Node params[] = Node.tupleToArray(ps, 3);
 			Node var = params[0];
 
 			// Avoids changing hash-code - but making memoize not re-entrant
 			((Reference) var).bound(uniqueReference);
 
			Node result = findAll.apply(Pair.<Node, Node> create(var, params[1]));
 			return prover.bind(params[2], result);
 		}
 	}
 
 	private class Not implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			Prover prover1 = new Prover(prover);
 			boolean result = !prover1.prove(ps);
 			if (!result) // Roll back bindings if overall goal is failed
 				prover1.undoAllBinds();
 			return result;
 		}
 	}
 
 	private class Once implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return new Prover(prover).prove(ps);
 		}
 	}
 
 	private class SystemPredicate_ implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			ps = ps.finalNode();
 			Atom atom = ps instanceof Atom ? (Atom) ps : null;
 			String name = atom != null ? atom.getName() : null;
 			return name != null ? predicates.containsKey(name) : false;
 		}
 	}
 
 	private class Temporary implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return prover.bind(ps, Atom.unique());
 		}
 	}
 
 	private Node findAll(Prover prover, final Node var, Node goal) {
 		final Stack<Node> stack = new Stack<>();
 
 		Tree subGoal = Tree.create(TermOp.AND___, goal, new Data<Source<Boolean>>(new Source<Boolean>() {
 			public Boolean source() {
 				stack.push(new Cloner().clone(var));
 				return Boolean.FALSE;
 			}
 		}));
 
 		new Prover(prover).elaborate(subGoal);
 
 		Node result = Atom.NIL;
 		while (!stack.isEmpty())
 			result = Tree.create(TermOp.AND___, stack.pop(), result);
 		return result;
 	}
 
 }
