 package org.net9.simplex.ppmc.checker;
 
 import java.util.BitSet;
 
 import org.net9.simplex.ppmc.core.DTMC;
 import org.net9.simplex.ppmc.prop.*;
 import org.net9.simplex.ppmc.solver.*;
 import org.net9.simplex.ppmc.solver.logic.*;
 import org.nfunk.jep.Node;
 
 public abstract class BasePCTLChecker implements ModelChecker {
 
 	DTMC model;
 	
 	//checker internal state
 	int initState;
 	boolean isNested;
 	public final Solver sTrue, sFalse;
 	NextChecker nextChecker;
 	
 	public BasePCTLChecker (DTMC model) {
 		this.model = model;
 		this.sTrue = new TrueSolver(model.size());
 		this.sFalse = new FalseSolver(model.size());
 	}
 	
 	NextChecker getNextChecker() {
 		if (this.nextChecker==null)
 			this.nextChecker = new NextChecker(model.getTrans());
 		return this.nextChecker;
 		
 	}
 	
 	Solver s(Object o){
 		return (Solver) o;
 	}
 	
 	@Override
 	public Solver check(StateProperty p) 
 			throws IllegalArgumentException, 
 				UnsupportedOperationException {
 		initState = model.getInitState();
 		isNested = false;
 		p.accept(this);
 		return s(p.accept(this));
 	}
 	
 	Solver getConstSolver(boolean b) {
 		return b? sTrue: sFalse;
 	}
 	
 	Solver getSetSolver(Solver s) {
 		if (s==sTrue || s==sFalse) return s;
 		BitSet bs = s.solveSet(null);
 		if (bs.cardinality()== model.size()) return sTrue;
 		else if (bs.cardinality()== 0) return sFalse;
 		return new SetSolver(bs);
 	}
 
 	@Override
 	public Object visit(PropTrue p) {
 		return sTrue;
 	}
 
 	@Override
 	public Object visit(PropNot p) {
 		
 		Solver s = s(p.p1.accept(this));
 		if (s==sTrue) return sFalse;
 		else if (s==sFalse) return sTrue;
 		else {
 			Solver n = new NotSolver(s, model.size());
 			if (s.isConstant()) 
 				return this.getSetSolver(n);
 			else
 				return n;
 		}
 	}
 
 	@Override
 	public Object visit(PropAnd p) {
 		AndSolver sc = new AndSolver();
 		AndSolver constSolver = new AndSolver();
 		for (StateProperty sp: p.item){
 			Solver s = s(sp.accept(this));
 			if (s==sFalse) {
 				return sFalse;
 			}
 			if (s==sTrue) continue;
 			if (s.isConstant()) 
 				constSolver.item.add(s);
 			else
 				sc.item.add(s);
 		}
 		switch(constSolver.item.size()){
 		case 0: break;
 		case 1: sc.item.add(constSolver.item.getFirst()); break;
 		default:
 			Solver s = this.getSetSolver(constSolver);
 			if (s==sFalse) {
 				return  sFalse;
 			}
 			if (s!=sTrue) sc.item.add(s);
 		}
 		switch(sc.item.size()){
 		case 0: return sTrue; 
 		case 1: return sc.item.getFirst(); 
 		default: return sc;
 		}
 	}
 
 	@Override
 	public Object visit(PropOr p) {
 		OrSolver sc = new OrSolver();
 		OrSolver constSolver = new OrSolver();
 		for (StateProperty sp: p.item){
 			
 			Solver s = s(sp.accept(this));
 			if (s==sTrue) {
 				return sTrue;
 			}
 			if (s==sFalse) continue;
 			if (s.isConstant()) 
 				constSolver.item.add(s);
 			else
 				sc.item.add(s);
 		}
 		switch(constSolver.item.size()){
 		case 0: break;
 		case 1: sc.item.add(constSolver.item.getFirst()); break;
 		default: 
 			Solver s = this.getSetSolver(constSolver);
 			if (s==sTrue) {
 				return sTrue;
 			}
 			if (s!=sFalse) sc.item.add(s);
 		}
 		switch(sc.item.size()){
 		case 0: return sFalse; 
 		case 1: return sc.item.getFirst(); 
 		default: return sc;
 		}		
 	}
 	
 	@Override
 	public Object visit(PropAtom p) {
 		BitSet ap = model.getAP().get(p.atom); 
 		if (ap == null) throw new IllegalArgumentException("No Atom Proposition: "+p.atom);
 		if(!isNested){
 			return this.getConstSolver(ap.get(initState));
 		} else {
 			return new SetSolver(ap);
 		}
 	}
 	
 	@Override
 	public Object visit(PropSet p) {
 		if (isNested){
 			return new SetSolver(p.item);
 		} else {
 			return this.getConstSolver(p.item.get(initState));
 		}
 	}
 	
 	@Override
 	public Object visit(PropProb p) {
 		p.isNested = this.isNested;
 		this.isNested = true;
 		
		ExpressionSolver s = (ExpressionSolver) p.p1.accept(this);;
		s.setConstraints(p.strComparator, p.comparator, p.prob);
		Solver r = s;
 		if (s.isConstant()){
			if (s.isSingle()) {
				r = this.getConstSolver(s.solve(null));
 			} else {
				r = this.getSetSolver(s);
 			}
 		}
 		this.isNested = p.isNested;
		return r;
 	}
 
 	@Override
 	public abstract Object visit(PropEventually p);
 
 	@Override
 	public Object visit(PropAlways p) {
 		PropEventually eventually = 
 				new PropEventually(new PropNot(p.p1));
 		eventually.parent = p.parent;
 		NumericSolver s = (NumericSolver) eventually.accept(this);
 		s.complement();
 		return s;
 	}
 
 	@Override
 	public Object visit(PropNext p) {
 		
 		Solver s = s(p.p1.accept(this));
 		if (!s.isConstant()){
 			//TODO : run time solver
 			throw new UnsupportedOperationException();
 		}
 		
 		if (p.parent.isNested) {
 			// TODO : return set solver
 			throw new UnsupportedOperationException();
 		} else {
 			NextChecker nextChecker = this.getNextChecker();
 			Node exp = nextChecker.check(this.initState, s.solveSet(null));
 			return new ExpressionSolver(exp);
 		}
 	}
 
 	@Override
 	public abstract Object visit(PropUntil p);
 
 	@Override
 	public Object visit(PropBoundedUntil p) {
 		
 		Solver s1 = s(p.p1.accept(this));
 		Solver s2 = s(p.p2.accept(this));
 		if (!s1.isConstant() || !s2.isConstant()){
 			//TODO : run time solver
 			throw new UnsupportedOperationException();
 		}
 		
 		BitSet bs1 = s1.solveSet(null);
 		BitSet bs2 = s2.solveSet(null);
 		if (p.parent.isNested) {
 			// TODO : return set solver
 			throw new UnsupportedOperationException();
 		} else {
 			Node r = BoundedUntilChecker.check(model,p.bound, bs1, bs2, this.initState);
 			return new ExpressionSolver(r);
 		}
 	}
 	
 
 	@Override
 	public Object visit(PropLTLPathWrapper p) {
 		// As PropLTLPathWrapp will be embedded in the PropLTL when parsing Prob operator, 
 		// it should not be handled by model checkers.
 		throw new IllegalArgumentException();
 	}	
 
 	@Override
 	public Object visit(PropLTL p) {
 		throw new UnsupportedOperationException();
 	}
 }
