 package org.suite.doer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import org.suite.Binder;
 import org.suite.Journal;
 import org.suite.SuiteUtil;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.kb.Prototype;
 import org.suite.kb.Rule;
 import org.suite.kb.RuleSet;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.suite.predicates.SystemPredicates;
 import org.util.FormatUtil;
 import org.util.LogUtil;
 
 public class Prover {
 
 	private RuleSet ruleSet;
 	private SystemPredicates systemPredicates = new SystemPredicates(this);
 
 	private boolean isEnableTrace = SuiteUtil.isTrace;
 	private Tracer tracer;
 	private static final Set<String> noTracePredicates = new HashSet<>(
 			Arrays.asList("member", "replace"));
 
 	private static final Node OK = Atom.NIL;
 	private static final Node FAIL = Atom.create("fail");
 
 	private Node rem, alt; // remaining, alternative
 
 	private Journal journal = new Journal();
 
 	public Prover(Prover prover) {
 		this(prover.ruleSet);
 	}
 
 	public Prover(RuleSet ruleSet) {
 		this.ruleSet = ruleSet;
 	}
 
 	/**
 	 * Try to prove a query clause. Perform bindings on the way.
 	 * 
 	 * @param query
 	 *            Clause to be proved.
 	 * @return true if success.
 	 */
 	public boolean prove(Node query) {
 		if (isEnableTrace)
 			tracer = new Tracer();
 
 		boolean result = prove0(query);
 
 		if (isEnableTrace) {
 			String date = FormatUtil.dtFmt.format(new Date());
 			String dump = tracer.getDump();
 			LogUtil.info("DUMP", "-- Prover dump at " + date + " --\n" + dump);
 		}
 
 		return result;
 	}
 
 	public boolean prove0(Node query) {
 		rem = OK;
 		alt = FAIL;
 
 		while (true) {
 			// LogUtil.info("PROVE", Formatter.dump(query));
 
 			Tree tree = Tree.decompose(query);
 			if (tree != null) {
 				final Node left = tree.getLeft(), right = tree.getRight();
 
 				switch ((TermOp) tree.getOperator()) {
 				case OR____:
 					final int pit = journal.getPointInTime();
 					Node bt = new Station() {
 						public boolean run() {
 							journal.undoBinds(pit);
 							return true;
 						}
 					};
 
 					Tree alt0 = Tree.create(TermOp.AND___, right, rem);
 					alt = alt != FAIL ? Tree.create(TermOp.OR____, alt0, alt)
 							: alt0;
 					alt = Tree.create(TermOp.AND___, bt, alt);
 					query = left;
 					continue;
 				case AND___:
 					if (right != OK)
 						rem = Tree.create(TermOp.AND___, right, rem);
 					query = left;
 					continue;
 				case EQUAL_:
 					query = isSuccess(bind(left, right));
 					break;
 				default:
 				}
 			} else if (query instanceof Station) {
 				query = isSuccess(((Station) query).run());
 				continue;
 			}
 
 			Boolean result = systemPredicates.call(query);
 			if (result != null)
 				query = isSuccess(result);
 
 			// Not handled above
 			if (query == OK)
 				if (rem != OK) {
 					query = rem;
 					rem = OK;
 				} else
 					return true;
 			else if (query == FAIL)
 				if (alt != FAIL) {
 					query = alt;
 					alt = FAIL;
 					rem = OK;
 				} else
 					return false;
 			else {
 				boolean isTrace = isEnableTrace;
				Prototype prototype = isTrace ? Prototype.get(query) : null;
 				Node head = prototype != null ? prototype.getHead() : null;
 				Atom atom = head instanceof Atom ? (Atom) head : null;
 				String name = atom != null ? atom.getName() : null;
 				isTrace &= !noTracePredicates.contains(name);
 
 				if (!isTrace)
 					query = expand(query);
 				else
 					query = tracer.expandWithTrace(query);
 			}
 		}
 	}
 
 	/**
 	 * Performs binding of two items.
 	 * 
 	 * @Return true if success.
 	 */
 	public boolean bind(Node left, Node right) {
 		return Binder.bind(left, right, journal);
 	}
 
 	/**
 	 * Resets all bind done by this prover.
 	 */
 	public void undoAllBinds() {
 		journal.undoBinds(0);
 	}
 
 	private Node isSuccess(boolean b) {
 		return b ? OK : FAIL;
 	}
 
 	/**
 	 * Expands an user predicate (with many clauses) to a chain of logic.
 	 * 
 	 * @param query
 	 *            The invocation pattern.
 	 * @param remaining
 	 *            The final goal to be appended.
 	 * @return The chained node.
 	 */
 	private Node expand(Node query) {
 		final Node alt0 = alt;
 		Node ret = FAIL;
 
 		List<Rule> rules = ruleSet.searchRule(query);
 		ListIterator<Rule> iter = rules.listIterator(rules.size());
 
 		while (iter.hasPrevious()) {
 			Rule rule = iter.previous();
 
 			Generalizer generalizer = new Generalizer();
 			generalizer.setCut(new Station() {
 				public boolean run() {
 					alt = alt0;
 					return true;
 				}
 			});
 
 			Node head = generalizer.generalize(rule.getHead());
 			Node tail = generalizer.generalize(rule.getTail());
 
 			ret = Tree.create(TermOp.OR____ //
 					, Tree.create(TermOp.AND___ //
 							, Tree.create(TermOp.EQUAL_ //
 									, query //
 									, head //
 							) //
 							, tail //
 					) //
 					, ret //
 					);
 		}
 
 		return ret;
 	}
 
 	public class Tracer {
 		private List<Record> records = new ArrayList<>();
 		private Record currentRecord = null;
 		private int currentDepth;
 
 		private class Record {
 			private Record parent;
 			private Node query;
 			private int depth;
 			private boolean result;
 
 			private Record(Record parent, Node query, int depth) {
 				this.parent = parent;
 				this.query = query;
 				this.depth = depth;
 			}
 
 			private void appendTo(StringBuilder sb) {
 				String header = "" //
 						+ "[" + (result ? "OK__" : "FAIL") //
 						+ ":" + depth //
 						+ "]";
 				sb.append(String.format("%-10s ", header));
 				for (int i = 1; i < depth; i++)
 					sb.append("| ");
 				sb.append(Formatter.dump(query));
 				sb.append("\n");
 			}
 		}
 
 		private Node expandWithTrace(Node query) {
 			Node query1 = new Cloner().clone(query);
 
 			final Record record0 = currentRecord;
 			final int depth0 = currentDepth;
 			final Record record = new Record(record0, query1, currentDepth + 1);
 
 			final Station enter = new Station() {
 				public boolean run() {
 					currentRecord = record;
 					currentDepth = record.depth;
 					records.add(record);
 					return true;
 				}
 			};
 
 			final Station leaveOk = new Station() {
 				public boolean run() {
 					currentRecord = record0;
 					currentDepth = depth0;
 					return record.result = true;
 				}
 			};
 
 			final Station leaveFail = new Station() {
 				public boolean run() {
 					currentRecord = record0;
 					currentDepth = depth0;
 					return false;
 				}
 			};
 
 			alt = Tree.create(TermOp.OR____, leaveFail, alt);
 			rem = Tree.create(TermOp.AND___, leaveOk, rem);
 			query = expand(query);
 			query = Tree.create(TermOp.AND___, enter, query);
 			return query;
 		}
 
 		public String getDump() {
 			StringBuilder sb = new StringBuilder();
 			for (Record record : records)
 				record.appendTo(sb);
 			return sb.toString();
 		}
 
 		public String getStackTrace() {
 			List<Node> traces = new ArrayList<>();
 			Record record = currentRecord;
 
 			while (record != null) {
 				traces.add(record.query);
 				record = record.parent;
 			}
 
 			StringBuilder sb = new StringBuilder();
 			for (int i = traces.size(); i > 0; i--)
 				sb.append(traces.get(i - 1) + "\n");
 
 			return sb.toString();
 		}
 
 		public Record getCurrentRecord() {
 			return currentRecord;
 		}
 
 		public int getCurrentDepth() {
 			return tracer.currentDepth;
 		}
 	}
 
 	/**
 	 * The set of rules which is mutable (may assert/retract).
 	 * 
 	 * Allows access from predicates.
 	 */
 	public RuleSet getRuleSet() {
 		return ruleSet;
 	}
 
 	/**
 	 * Allows taking stack dump, with performance hit.
 	 */
 	public void setEnableTrace(boolean isEnableTrace) {
 		this.isEnableTrace = isEnableTrace;
 	}
 
 	/**
 	 * Goals ahead.
 	 */
 	public Node getRemaining() {
 		return rem;
 	}
 
 	public void setRemaining(Node rem) {
 		this.rem = rem;
 	}
 
 	/**
 	 * Alternative path to succeed.
 	 */
 	public Node getAlternative() {
 		return alt;
 	}
 
 	public void setAlternative(Node alt) {
 		this.alt = alt;
 	}
 
 	/**
 	 * The roll-back log of variable binds.
 	 */
 	public Journal getJournal() {
 		return journal;
 	}
 
 	/**
 	 * Traces program flow.
 	 */
 	public Tracer getTracer() {
 		return tracer;
 	}
 
 }
