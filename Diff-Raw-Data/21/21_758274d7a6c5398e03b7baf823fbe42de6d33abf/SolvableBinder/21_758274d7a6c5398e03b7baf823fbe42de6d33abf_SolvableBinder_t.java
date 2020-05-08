 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: SolvableBinder.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: SolvableBinder
  */
 package edu.columbia.mipl.runtime.execute;
 
 import java.util.*;
 
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.traverse.*;
 
 class VariableGrouper implements Traverser {
 	VariableStack vs;
 	Map<String, Term> map;
 
 	public VariableGrouper(VariableStack vs) {
 		this.vs = vs;
 		map = new HashMap<String, Term>();
 	}
 
 	public Method getMethod() {
 		return Method.POST;
 	}
 
 	public boolean reach(Traversable target) {
 		if (target instanceof Term) {
 			Term t = (Term) target;
 			if (t.getType() == Term.Type.VARIABLE) {
 				Term exist = map.get(t.getName());
 				if (exist == null)
 					map.put(t.getName(), t);
 				else
 					vs.group(exist, t);
 			}
 		}
 		return true;
 	}
 
 	public Map<String, Term> getVariableMap() {
 		return map;
 	}
 
 	public void finish() {
 	}
 }
 
 public class SolvableBinder extends Binder implements Solvable {
 	Goal goal;
 	VariableStack vs;
 
 	SolvableBinder(Term term) {
 		goal = new Goal(term);
 		vs = new VariableStack();
 		VariableGrouper grouper = new VariableGrouper(vs);
 		term.traverse(grouper);
 		goal.setInitialVariableMap(grouper.getVariableMap());
 	}
 
 	boolean bind() {
 		return bind(goal, vs, this);
 	}
 
 	boolean match(Term target, Term source, VariableStack vs) {
 		if (target.getType() != Term.Type.VARIABLE &&
 				source.getType() != Term.Type.VARIABLE &&
 				source.getType() != Term.Type.MATRIX &&
 				target.getType() != source.getType())
 			return false;
 
 		if (target.getType() == Term.Type.VARIABLE &&
 				source.getType() == Term.Type.VARIABLE) {
 
 			if (vs.group(target, source))
 				return true;
 
 			return match(vs.get(target), vs.get(source), vs);
 		}
 		else if (target.getType() == Term.Type.VARIABLE &&
 				source.getType() != Term.Type.VARIABLE) {
 
 			if (vs.get(target) == null) {
 				vs.put(target, source); //TODO clone and put
 				return true;
 			}
 
 			return match(vs.get(target), source, vs);
 		}
 		else if (target.getType() != Term.Type.VARIABLE &&
 				source.getType() == Term.Type.VARIABLE) {
 
 			if (vs.get(source) == null) {
 				vs.put(source, target); //TODO clone and put
 				return true;
 			}
 
 			return match(vs.get(source), target, vs);
 		}
 
 		int size = target.getArguments().size();
 		int i;
 
 		if (!target.getName().equals(source.getName()))
 			return false;
 
 		if (source.getType() == Term.Type.TERM) {
 			if (size != source.getArguments().size())
 				return false;
 
 			List<Term> tgtArgs = target.getArguments();
 			List<Term> srcArgs = source.getArguments();
 			for (i = 0; i < size; i++) {
 				if (!match(tgtArgs.get(i), srcArgs.get(i), vs))
 					return false;
 			}
 		}
 		/* NUMBER, MATRIX, STRING, REGEX */
 		else { /* source.getType == Term.Type.MATRIX */
 			//TODO
 		}
 
 		return true;
 	}
 
 	boolean bind(Goal goal, VariableStack vs, Solvable solver) {
 		boolean result = false;
 		Term currentGoal;
 
 		if (goal.empty()) {
 			solver.solve(goal, vs);
 			return true;
 		}
 
 		currentGoal = goal.pop();
 
 		if (true) {
 			List<Knowledge> knowledges;
 			Term newTerm;
 			Rule rule;
 
 			knowledges = KnowledgeTableFactory.getKnowledgeTable().get(currentGoal.getName());
 			if (knowledges == null)
 				return false;
 
 
 			for (Knowledge knowledge : knowledges) {
 				VariableStack newVs = new VariableStack(vs);
 				knowledge.traverse(new VariableGrouper(newVs));
 
 
 				if (knowledge instanceof Job)
 					continue;
 
 
 				if (!match(currentGoal, knowledge.getTerm(), newVs))
 					continue;
 
 
 				if (knowledge instanceof Fact) {
 					result = result || bind(new Goal(goal), newVs, solver);
 					continue;
 				}
 
 
 				rule = (Rule) knowledge;
 				Term source = rule.getSource();
 				Term orTermRhs = null;
 
 				if (source.getType() == Term.Type.ORTERMS)
 					orTermRhs = source;
 				
 				do {
 					if (orTermRhs != null) {
 						source = source.getTerm1();
 						orTermRhs = source.getTerm2();
 					}
 
 					while (source.getType() == Term.Type.ANDTERMS) {
 						goal.push(source.getTerm1());
 						source = source.getTerm2();
 					}
 					assert (source.getType() == Term.Type.TERM);
 					goal.push(source);
 
 					result = result || bind(new Goal(goal), newVs, solver);
 				} while (orTermRhs != null);
 			}
 		}
 //		solver.solve(goal, vs);
 
 		return result;
 	}
 
 	public boolean solve(Goal goal, VariableStack vs) {
 		System.out.println("Solved Goal!");
		new Exception().printStackTrace();
		Map<String, Term> variables = goal.getInitialVariableMap();

		for (String variable : variables.keySet()) {
			System.out.println(variable + " = ");
		}
 
 		return true;
 	}
 }
