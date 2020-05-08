 package uk.ac.ic.doc.gander.analysis.ssa;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.python.pydev.parser.jython.ast.Name;
 
 import uk.ac.ic.doc.gander.analysis.dominance.DomFront.DomInfo;
 import uk.ac.ic.doc.gander.cfg.BasicBlock;
 import uk.ac.ic.doc.gander.cfg.Cfg;
 
 /**
 * Based on Figure 9.13, p169 Engineering a Compiler.
  */
 public class SSAVariableSubscripts {
 
 	/**
 	 * Perform most of Rename algorithm on p469 of Engineering A Compiler.
 	 */
 	private class Renamer extends NameInspector {
 
 		public Renamer(BasicBlock block) {
 			inspect(block);
 		}
 
 		@Override
 		public void seenLoad(Name name) {
 			rewrite(name, top(name.id));
 		}
 
 		@Override
 		public void seenStore(Name name) {
 			rewrite(name, newName(name.id));
 		}
 
 		@Override
 		public void seenAugStore(Name name) {
 			// for the purposes of variable renaming, we can ignore that
 			// the variable is used and rename because it is also redefined
 			// XXX: Is this correct by SSA?
 			rewrite(name, newName(name.id));
 		}
 	}
 
 	/**
 	 * Perform last part of Rename algorithm on p469 of Engineering A Compiler
 	 * (popping new names off the stack).
 	 */
 	private class Popper extends NameInspector {
 
 		public Popper(BasicBlock block) {
 			inspect(block);
 		}
 
 		@Override
 		protected void unhandledName(Name name) {
 		}
 
 		@Override
 		protected void seenStore(Name name) {
 			stacks.get(name.id).pop();
 		}
 
 		@Override
 		protected void seenAugStore(Name name) {
 			stacks.get(name.id).pop();
 		}
 	}
 
	private Map<Name, Integer> subscripts = new HashMap<Name, Integer>();
	private Map<String, Integer> counters = new HashMap<String, Integer>();
	private Map<String, Stack<Integer>> stacks = new HashMap<String, Stack<Integer>>();
	private Map<BasicBlock, Set<BasicBlock>> domSuccessors = new HashMap<BasicBlock, Set<BasicBlock>>();
	private PhiPlacement phis;
 
 	public SSAVariableSubscripts(Cfg graph) {
 		phis = new PhiPlacement(graph);
 		buildDomTree(graph);
 
 		rename(graph.getStart());
 	}
 
 	private void push(String variable, int subscript) {
 		Stack<Integer> stack = stacks.get(variable);
 		if (stack == null) {
 			stack = new Stack<Integer>();
 			stacks.put(variable, stack);
 		}
 		stack.push(subscript);
 	}
 
 	private int top(String variable) {
 		Stack<Integer> stack = stacks.get(variable);
 		if (stack == null || stack.isEmpty())
 			return -1;
 
 		return stack.peek();
 	}
 
 	private void rewrite(Name name, Integer subscript) {
 		Integer prev = subscripts.put(name, subscript);
 		assert prev == null; // This exact name instance has already been
 		// renamed.
 	}
 
 	private void rename(BasicBlock node) {
 
 		// for each phi-function in b, "x <- phi(...)", rename x as NewName(x)
 		Iterable<String> targets = phis.phiTargets(node);
 		if (targets != null) {
 			for (String target : targets) {
 				newName(target);
 			}
 		}
 
 		// for each operation "x <- y op z" in b, rewrite y as as top(stack[y]),
 		// rewrite z as top(stack[z]), rewrite x as NewName(x)
 		new Renamer(node);
 
 		// for each sucessor in the CFG, fill in phi-function paramters
 		// NOT NEEDED
 
 		// for each successor s in the dominator tree, Rename(s)
 		Set<BasicBlock> successors = domSuccessors.get(node);
 		if (successors != null) {
 			for (BasicBlock successor : successors) {
 				rename(successor);
 			}
 		}
 
 		// for each operation "x <- y op z" in b and each phi-function
 		// "x <- phi(...)", pop(stack[x])
 		new Popper(node);
 		if (targets != null) {
 			for (String target : targets) {
 				stacks.get(target).pop();
 			}
 		}
 	}
 
 	private int newName(String variable) {
 		Integer i = counters.get(variable);
 		if (i == null)
 			i = new Integer(0);
 
 		counters.put(variable, i + 1);
 
 		push(variable, i);
 		return i;
 	}
 
 	public int subscript(Name variable) {
 		Integer i = subscripts.get(variable);
 		if (i == null) // non-global
 			return -1;
 		else
 			return i;
 	}
 
 	private void buildDomTree(Cfg graph) {
 
 		Map<BasicBlock, DomInfo> domInfo = phis.getDomInfo();
 		for (BasicBlock block : graph.getBlocks()) {
 			DomInfo info = domInfo.get(block);
 
 			if (info.idom == null || info.idom == block)
 				continue;
 
 			Set<BasicBlock> dominatedChildren = domSuccessors.get(info.idom);
 			if (dominatedChildren == null) {
 				dominatedChildren = new HashSet<BasicBlock>();
 				domSuccessors.put(info.idom, dominatedChildren);
 			}
 			dominatedChildren.add(block);
 		}
 	}
 
 	public Map<BasicBlock, DomInfo> getDomInfo() {
 		return phis.getDomInfo();
 	}
 }
