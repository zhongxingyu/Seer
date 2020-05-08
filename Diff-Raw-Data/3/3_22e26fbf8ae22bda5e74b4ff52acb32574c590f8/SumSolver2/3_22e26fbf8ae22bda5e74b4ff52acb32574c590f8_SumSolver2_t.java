 
 
 import ilog.concert.*;
 import ilog.cplex.*;
 
 public class SumSolver 
 {
 	/**
 	 * XXX The returned solution should be multiplied by the size of the
 	 * original (large) database. Or we could pass it as a parameter here
 	 * and do the multiplication in this method.
 	 */
 	public static double[] sumSolver(int min, int max, double
 			querySelectivity, double[] selectivities, double eta,
 			double epsilon, boolean solveMax)
 	{
 		try 
 		{
 			IloCplex cplex = new IloCplex();
 			
 			double[] lowerBound = new double[max - min + 1];
 			double[] upperBound = new double[max - min + 1];
 			
 			for(int i=0; i<max-min+1; i++)
 			{
 				lowerBound[i] = Math.max(selectivities[i] * (1 - eta) / eta, -epsilon);
				upperBound[i] = Math.min((1 - ((querySelectivity - selectivities[i]) / eta) -
							selectivities[i], epsilon);
 			}
 			
 			IloNumVar[] epsilonV  = cplex.numVarArray(max-min+1, lowerBound, upperBound);
 			double[] coeffs = new double[max - min + 1];
 			for(int i=0; i<max-min+1; i++)
 			{
 				coeffs[i] = (double)(min+i);
 			}
 			
 			if(solveMax)
 			{
 				cplex.addMaximize(cplex.scalProd(epsilonV, coeffs));
 			} 
 			else 
 			{
 				cplex.addMinimize(cplex.scalProd(epsilonV, coeffs));
 			}
 			
 			IloNumExpr[] ieExpr = new IloNumExpr[max - min + 1];
 			for(int i=0; i<max-min+1; i++)
 			{
 				ieExpr[i] = cplex.prod(1.0, epsilonV[i]);
 			}
 			
 			IloNumExpr sumExpr = cplex.sum(ieExpr);
 			cplex.addLe(sumExpr, Math.min(1-querySelectivity, epsilon));
 			cplex.addGe(sumExpr, Math.max(querySelectivity * (1 - eta) / eta , -epsilon));
 			
 			if(cplex.solve()) 
 			{
 				double[] val = cplex.getValues(epsilonV);
 				cplex.end();
 				return val;
 			} 
 			else 
 			{
 				cplex.end();
 				return null;
 			}
 		} 
 		catch (IloException e) 
 		{
 			System.err.println("Concert exception '" + e + "' caught");
 			return null;
 		}
 	}
 	
 	// XXX Fix for the new optimization problem
 	public static void main(String[] args) 
 	{
 		// Max
 		double[] val1 = sumSolver(1, 100, 0.02, true);
 		for (int j = 0; j < val1.length; j++)
 		{
 			System.out.println("EpsilonV" + (j + 1) + " = " + val1[j]);
 		}
 		
 		// Min
 		double[] val2 = sumSolver(1, 100, 0.02, false);
 		for (int j = 0; j < val2.length; j++)
 		{
 			System.out.println("EpsilonV" + (j + 1) + " = " + val2[j]);
 		}
 	}
 } 
 
