 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import ilog.concert.IloException;
 import ilog.concert.IloLinearNumExpr;
 import ilog.concert.IloNumVar;
 import ilog.concert.IloObjectiveSense;
 import ilog.cplex.IloCplex;
 
 /**
  * The LP model of an instance
  */
 
 /**
  * @author lrem
  *
  */
 public class Model {
 
 	int routerCount;
 	int cdnCount;
 	double [][] r2r;
 	double [][] r2cdn;
 	double [][] topo;
 	IloNumVar [][] x;
 	IloNumVar [] y;
 	IloNumVar [] z;
 	IloNumVar [][][] f;
 	IloNumVar [][][] sv;
 	IloNumVar [][] cr;
 	IloNumVar [][] cc;
 	double alpha, beta, gamma, cbw;
 	ArrayList<ArrayList<Integer>> locations;
 	ArrayList<ArrayList<Double>> capacities;
 
 	IloCplex model;
 
 	public Model (String ipath, double alpha, double beta, double gamma, double cbw, double limit) 
 			throws IloException, IOException
 			{
 		this.alpha = alpha;
 		this.beta = beta;
 		this.gamma = gamma;
 		this.cbw = cbw;
 		routerCount = parseInt(ipath + "rcount");
 		cdnCount = parseInt(ipath + "ccount");
 		r2r = parseMatrix(ipath + "r2r.int", routerCount, routerCount);
 		r2cdn = parseMatrix(ipath + "r2cdn.int", routerCount, cdnCount);
 		topo = parseMatrix(ipath + "topo.txt", routerCount, routerCount);
 		locations = parseAL(ipath + "cdnl.int");

 		model = new IloCplex();
 		model.setParam(IloCplex.DoubleParam.TiLim, limit);
 		
 		IloLinearNumExpr obj = model.linearNumExpr();
 		x = new IloNumVar[routerCount][routerCount];
 		y = new IloNumVar[routerCount];
 		z = new IloNumVar[routerCount];
 		for(int i = 0; i < routerCount; i++)
 		{
 			for(int j = 0; j < routerCount; j++)
 			{
 				x[i][j] = model.numVar(0, 1);
 				obj.addTerm(1, x[i][j]);
 			}
 			y[i] = model.numVar(0, 1);
 			obj.addTerm(beta, y[i]);
 			z[i] = model.numVar(0, 1);
 			obj.addTerm(gamma, z[i]);
 		}
 		model.addObjective(IloObjectiveSense.Minimize, obj);
 
 		f = new IloNumVar[routerCount][routerCount][routerCount];
 		for(int i = 0; i < routerCount; i++)
 			for(int j = 0; j < routerCount; j++)
 				for(int k = 0; k < routerCount; k++)
 					f[i][j][k] = model.numVar(0, Double.MAX_VALUE);
 		cr = new IloNumVar[routerCount][routerCount];
 		for(int i = 0; i < routerCount; i++)
 			for(int j = 0; j < routerCount; j++)
 				cr[i][j] = model.numVar(0, Double.MAX_VALUE);
 		cc = new IloNumVar[routerCount][cdnCount];
 		for(int i = 0; i < routerCount; i++)
 			for(int j = 0; j < cdnCount; j++)
				cr[i][j] = model.numVar(0, Double.MAX_VALUE);
 		sv = new IloNumVar[cdnCount][routerCount][routerCount];
 		for(int i = 0; i < cdnCount; i++)
 			for(int j = 0; j < routerCount; j++)
 				for(int k = 0; k < routerCount; k++)
 					sv[i][j][k] = model.numVar(0, Double.MAX_VALUE);
 		
 		// Flow constraints
 		for(int src = 0; src < routerCount; src++)
 			for(int middle = 0; middle < routerCount; middle++)
 			{
 				IloLinearNumExpr lhs = model.linearNumExpr();
 				for(int left = 0; left < routerCount; left++)
 					lhs.addTerm(1, f[src][left][middle]);
 				for(int right = 0; right < routerCount; right++)
 					lhs.addTerm(-1, f[src][middle][right]);
 				double total = 0;
 				if(middle == src)
 				{
 					/*total = -sum(strict_demand(src, dst) 
                             for dst in xrange(router_count)) \
                         -sum(flexible_demand(src, cdn)
                             for cdn in xrange(cdn_count))*/
 					for(int dst = 0; dst < routerCount; dst++)
 					{
 						total -= r2r[src][dst];
 						lhs.addTerm(-1, cr[src][dst]);
 					}
 					for(int cdn = 0; cdn < cdnCount; cdn++)
 					{
 						total -= r2cdn[src][cdn];
 						lhs.addTerm(-1, cc[src][cdn]);
 					}
 				}
 				else
 				{
 					/*total = strict_demand(src, middle) + \
 	                        sum(serv_vars[cdn, src, middle]
 	                                for cdn in xrange(cdn_count) 
 	                                if  middle in locations[cdn])*/
 					total += r2r[src][middle];
 					lhs.addTerm(1, cr[src][middle]);
 					for(int cdn = 0; cdn < cdnCount; cdn++)
 						if(locations.get(cdn).contains(middle))
 							lhs.addTerm(-1, sv[cdn][src][middle]);
 				}
 				// lpSum(incoming) - lpSum(outgoing) == total
 				model.addEq(lhs, total);
 			}
 
 		// Link capacities
 		for(int i = 0; i < routerCount; i++)
 			for(int j = 0; j < i; j++)
 			{
 				IloLinearNumExpr total = model.linearNumExpr();
 				for(int src = 0; src < routerCount; src++)
 				{
 					total.addTerm(1, f[src][i][j]);
 					total.addTerm(1, f[src][j][i]);
 				}
 				IloLinearNumExpr cap = model.linearNumExpr();
				assert(capacities.get(i).get(j) == capacities.get(j).get(i));
				cap.addTerm(x[i][j], capacities.get(i).get(j));
 				model.addLe(total, cap);
 			}
 
 		// Server capacities
 		for(int cdn = 0; cdn < cdnCount; cdn++)
 			for(int i = 0; i < locations.get(cdn).size(); i++)
 			{
				int loc = locations.get(cdn).get(i);
 				IloLinearNumExpr total = model.linearNumExpr();
 				for(int src = 0; src < routerCount; src++)
 					total.addTerm(1, sv[cdn][src][loc]);
 				model.addLe(total, capacities.get(cdn).get(i));
 			}
 
 		// Cache alpha-limits
 		for(int src = 0; src < routerCount; src++)
 		{
 			for(int dst = 0; dst < routerCount; dst++)
 			{
 				IloLinearNumExpr lhs = model.linearNumExpr();
 				lhs.addTerm(1, cr[src][dst]);
 				model.addLe(lhs, alpha * r2r[src][dst]);
 			}
 			for(int cdn = 0; cdn < cdnCount; cdn++)
 			{
 				IloLinearNumExpr lhs = model.linearNumExpr();
 				lhs.addTerm(1, cc[src][cdn]);
 				model.addLe(lhs, alpha * r2cdn[src][cdn]);
 			}
 		}
 
 		// Cache capacities
 		for(int src = 0; src < routerCount; src++)
 		{
 			IloLinearNumExpr total = model.linearNumExpr();
 			for(int dst = 0; dst < routerCount; dst++)
 				total.addTerm(1, cr[src][dst]);
 			for(int cdn = 0; cdn < cdnCount; cdn++)
 				total.addTerm(1, cc[src][cdn]);
 			IloLinearNumExpr cap = model.linearNumExpr();
 			cap.addTerm(z[src], cbw);
 			model.addLe(total, cap);
 			IloLinearNumExpr load = model.linearNumExpr();
 			load.addTerm(1, z[src]);
 			IloLinearNumExpr up = model.linearNumExpr();
 			up.addTerm(1, y[src]);
 			model.addLe(load, up);
 		}
 			}
 
 	private double [][] parseMatrix(String path, int rows, int columns) throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(path));
 		String line;
 		double [][] matrix = new double[rows][columns];
 		for(int i = 0; i < rows; i++){
 			line = reader.readLine();
 			assert(line != null);
 			StringTokenizer tok = new StringTokenizer(line);
 			assert(tok.countTokens() == columns);
 			for(int j = 0; j < columns; j++)
 				matrix[i][j] = Double.valueOf(tok.nextToken());
 		}
 		return matrix;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends Number> ArrayList<ArrayList<T>> parseAL(String path) throws NumberFormatException, IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(path));
 		String line;
 		ArrayList<ArrayList<T>> ret = new ArrayList<ArrayList<T>> ();
 		while((line = reader.readLine()) != null) {
 			StringTokenizer tok = new StringTokenizer(line);
 			if(tok.countTokens() == 0) // empty lines can happen only at the end of file, can't they?
 				break;
 			ArrayList<T> row = new ArrayList<T>();
 			while(tok.hasMoreTokens())
 				row.add((T) Double.valueOf(tok.nextToken()));
 		}
 		return ret;
 	}
 
 	private int parseInt(String path) throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(path));
 		String line = reader.readLine();
 		return Integer.valueOf(line);
 	}
 }
