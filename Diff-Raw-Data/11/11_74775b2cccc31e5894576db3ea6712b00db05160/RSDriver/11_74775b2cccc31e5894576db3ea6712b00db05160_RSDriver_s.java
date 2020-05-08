 package rs;
 
 import ilog.concert.IloException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class RSDriver {
 
 	public static void main(String[] args) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException{
 		try{
 			if(args.length<2){
 				System.out.println("Error, usage: exec solverType format pathname timeout [formulations extension specifications]");
 				System.out.println("Available solverType: Group, Node");
 				System.out.println("Available format: OLD NEW");
 				System.out.println("Available formulations extensions:");
 				for(ILPSolver.Extension e:ILPSolver.Extension.values()){
 					System.out.println(e.name());
 				}
 				return;
 			}
 			int timeout=Integer.parseInt(args[3]);
 			for(int i=4;i<args.length;++i){
 				try{
 					ILPSolver.Extension ext=ILPSolver.Extension.valueOf(args[i]);
 					ILPSolver.insertExtension(ext);
 				}catch(IllegalArgumentException e){
 					System.out.println("Ignoring wrong extension:"+args[i]);
 				}			
 			}
 			
 			File file=new File(args[2]);
 			if(!file.exists()){
 				System.out.println("Error, file "+file.getAbsolutePath()+" doesn't exists");
 				return;
 			}
 			Instance ins=null;
 			if(args[1].equals("OLD"))
 				ins=Instance.parseInstanceOld(file);
 			else if(args[1].equals("NEW"))
 				ins=Instance.parseInstanceNew(file);
 			else{
 				System.err.println("Invalid format: "+args[1]);
 				return;			
 			}
 			String className="rs."+args[0]+"ILPSolver";
 			ILPSolver solver=null;
 			try{
 				solver=(ILPSolver) Class.forName(className).newInstance();
 			}catch(ClassNotFoundException e){
 				System.err.println("Unable to find the solver:"+className);
 				return;
 			}
 			System.out.println("Finished Instance Parsing.");
			System.out.println("Number of nodes:"+ins.getNumNodes());
 			Solution sol=new Solution(ins);
 			long beg=System.currentTimeMillis();			
 			double obj=solver.solve(ins,sol,timeout);
 			if(!sol.isFeasible(ins, obj)){
 				System.err.println("Solution not feasible");
 				return;
 			}
 			System.out.println("ETIME="+(System.currentTimeMillis() - beg)/1000.0);
 			sol.isFeasible(ins, obj);		
 			System.out.println("Optimal Solution:");
 			System.out.println(sol);
 			System.out.println("Optimal Objective Value:");
 			System.out.println(obj);
 		}
 		catch(IloException e){
 			System.err.println("Raised exception:"+e.getMessage());			
 		}
 	}
 
 }
