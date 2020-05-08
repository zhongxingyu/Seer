 package rs;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import ilog.concert.IloException;
 import ilog.concert.IloNumExpr;
 import ilog.concert.IloNumVar;
 import ilog.concert.IloObjectiveSense;
 import ilog.cplex.IloCplex;
 import or.util.BiIndexedIntVarMap;
 import or.util.BiIndexedNumVarMap;
 import or.util.IndexedNumVarMap;
 import or.util.TriIndexedNumVarMap;
 import rs.ILPSolver.Extension;
 import util.FloatUtils;
 
 public class NodeILPSolver extends ILPSolver {
 	public double solve(Instance ins,Solution sol, int timeout) throws IloException{
 		IloCplex solver=new IloCplex();
 		BiIndexedIntVarMap x=new BiIndexedIntVarMap(solver, 0, 1, "x"); 				//(l,v)   (cluster,node)
 		IndexedNumVarMap dp=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dp");	//(l)     (cluster)
 		IndexedNumVarMap dm=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dm");	//(l)     (cluster)
 		TriIndexedNumVarMap f=new TriIndexedNumVarMap(solver, 0, Double.MAX_VALUE, "f");//(l,i,j) (cluster,tail,head)		
 		BiIndexedNumVarMap y=new BiIndexedNumVarMap(solver, 0, 1, "y");			//(i,j)   (node1<node2)
 		
 		int superRoot=ins.getNumNodes();
 		//compute the weights of the objective function
 		double w1=ins.getW1() / ins.getNumNodes();
 		double w2=ins.getW2() / (ins.getLambda() * ins.getMaxClusterNumber());
 		double w3=2*ins.getW3() / (ins.getNumNodes() * (ins.getNumNodes() -1)*(ins.getDMax() - ins.getDMin()));
 		
 		Set<OrderedPair> fp=new TreeSet<OrderedPair>(ins.getP());		
 		if(isActiveExtension(Extension.PFILTER)){
 			fp=ins.filterP();
 			System.out.println("PFILTER: "+(ins.getP().size() - fp.size())+" pairs filtered");
 		}
 		//build P_l structure
 		ArrayList< List<Integer> > pl=new ArrayList< List<Integer> >();
 		for(int l=0;l<ins.getNumNodes();++l){
 			pl.add(new LinkedList<Integer>());
 			pl.get(l).add(l);
 			for(int i=l+1;i<ins.getNumNodes();++i){
 				if(fp.contains(new OrderedPair(l,i)))
 					pl.get(l).add(i);
 			}
 		}
 		
 		//compute the objective function expression
 		IloNumExpr expr=solver.numExpr();
 		
 		//O1
 		IloNumExpr exprO1=solver.numExpr();
 		for(int i=0;i<ins.getNumNodes();++i){
 			expr=solver.numExpr();
 			expr=solver.sum(expr,1);
 			for(int l=0;l<=i;++l){	
 				if(pl.get(l).contains(i))
 					expr=solver.diff(expr, x.get(l, i));
 			}
 			exprO1=solver.sum(exprO1,solver.prod(w1,expr));
 		}
 		//O2
 		IloNumExpr exprO2=solver.numExpr();
 		for(int l=0;l<ins.getNumNodes();++l){
 			exprO2=solver.sum(exprO2,solver.prod(w2,dm.get(l)));
 			exprO2=solver.sum(exprO2,solver.prod(w2,dp.get(l)));
 		}
 		//O3
 		IloNumExpr exprO3=solver.numExpr();
 		for(OrderedPair p:fp){
 			double dmax=ins.getDMax();
 			double d=ins.getD(p.i,p.j);
 			IloNumVar yvar=y.get(p.i, p.j);
 			exprO3=solver.sum(exprO3,solver.prod(w3*(dmax-d),yvar));
 		}
 		expr=solver.sum(exprO1,solver.sum(exprO2,exprO3));
 		solver.add(solver.objective(IloObjectiveSense.Minimize,expr));
 		
 		//packing constraints
 		for(int i=0;i<ins.getNumNodes();++i){
 			expr=solver.numExpr();
 			for(int l=0;l<=i;++l){				
 				if(pl.get(l).contains(i))
 					expr=solver.sum(expr,x.get(l, i));
 			}
			solver.addLe(expr, 1);
 		}
 		
 		//nodes -> root linking constraints
 		for(int l=0;l<ins.getNumNodes();++l){
 			expr=solver.numExpr();
 			for(int i:pl.get(l)){
 				expr=solver.sum(expr,x.get(l,i));								
 			}
 			expr=solver.diff(expr,solver.prod(ins.getMaxClusterSize(), x.get(l, l)));
 			solver.addLe(expr, 0);
 		}
 		
 		//number of cluster constraint
 		expr=solver.numExpr();
 		for(int l=0;l<ins.getNumNodes();++l){
 			expr=solver.sum(expr,x.get(l,l));
 		}
 		solver.addLe(expr,ins.getMaxClusterNumber());
 		
 		//f-x constraints
 		for(int l=0;l<ins.getNumNodes();++l){
 			for(int i:pl.get(l)){
 				expr=solver.numExpr();			
 				for(int j:ins.getOutcut(i)){
 					if(pl.get(l).contains(j)) 
 						expr=solver.sum(expr,f.get(l, i, j));
 				}
 				expr=solver.diff(expr, solver.prod(ins.getMaxClusterSize(),x.get(l, i)));
 				solver.addLe(expr,0);
 			}
 		}
 		
 		//flow generation constraints
 		for(int l=0;l<ins.getNumNodes();++l){
 			expr=solver.numExpr();		
 			for(int i:ins.getOutcut(l)){
 				if(!fp.contains(new OrderedPair(l,i))) continue;
 				expr=solver.sum(expr,f.get(l,l,i));								
 			}
 			for(int i:pl.get(l)){
 				if(i==l) continue;
 				expr=solver.diff(expr,x.get(l, i));
 			}
 			solver.addEq(expr,0.0);
 		}
 		
 		//flow conservation constraints
 		for(int l=0;l<ins.getNumNodes();++l){		
 			for(int i:pl.get(l)){
 				if(i==l) continue;
 				expr=solver.numExpr();			
 				for(int j:ins.getOutcut(i)){
 					if( pl.get(l).contains(j)){
 						expr=solver.diff(expr,f.get(l, i, j));
 						expr=solver.sum(expr, f.get(l,j,i));
 					}
 				}
 				solver.addEq(expr,x.get(l,i));
 			}
 		}
 		
 		//dp - dm definition constraints
 		for(int l=0;l<ins.getNumNodes();++l){
 			IloNumExpr e1=solver.numExpr();
 			e1=solver.sum(e1,x.get(l, l));
 			e1=solver.diff(1, e1);
 			e1=solver.prod(ins.getMaxClusterSize(), e1);
 			
 			IloNumExpr e2=solver.numExpr();
 			for(int i:pl.get(l))
 				e2=solver.sum(e2,x.get(l,i));
 			//dp
 			expr=solver.numExpr();
 			expr=solver.sum(expr,dp.get(l));
 			expr=solver.sum(expr, ins.getLambda());
 			expr=solver.diff(expr, e2);
 			expr=solver.sum(expr,e1);
 			solver.addGe(expr, 0);
 			//dm
 			expr=solver.numExpr();
 			expr=solver.sum(expr,dm.get(l));
 			expr=solver.diff(expr,ins.getLambda());
 			expr=solver.sum(expr,e2);
 			expr=solver.sum(expr,e1);
 			solver.addGe(expr,0);			
 		}
 //		
 //		//y var definition constraints
 		for(int l=0;l<ins.getNumNodes();++l){
 			for(OrderedPair p:fp){
 				expr=solver.numExpr();
 				expr=solver.sum(expr,y.get(p.i,p.j));
 				expr=solver.diff(expr, x.get(l,p.i));
 				expr=solver.diff(expr, x.get(l,p.j));
 				expr=solver.sum(expr,1);
 				solver.addGe(expr, 0);				
 			}
 		}
 		
 		solver.setParam(IloCplex.DoubleParam.TiLim, timeout);
 		solver.solve();
 		System.out.println("STATE="+solver.getStatus());
 		System.out.println("NN="+solver.getNnodes());
 		System.out.println("LB="+solver.getBestObjValue());
 		
 		//If we have a feasible solution we save it
 		if(solver.getStatus() == IloCplex.Status.Feasible || solver.getStatus() == IloCplex.Status.Optimal){		
 			int k=0;
 			for(int l=0;l<ins.getNumNodes();++l){
 				if(FloatUtils.eq(solver.getValue(x.get(l, l)), 0.0)) continue;			
 				for(int v=l;v<ins.getNumNodes();++v){
 					if(FloatUtils.eq(solver.getValue(x.get(l,v)),1.0)){
 						sol.insert(k, v);
 					}
 				}
 				++k;
 			}
 		}
 		
 		return solver.getObjValue();
 	}
 
 }
