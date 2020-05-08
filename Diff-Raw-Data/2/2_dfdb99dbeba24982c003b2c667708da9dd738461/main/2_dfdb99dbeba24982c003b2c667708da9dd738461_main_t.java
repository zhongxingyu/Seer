 import java.io.*;
 import org.tsho.jidmclib.*;
 
 public class main {
 	static {
 		System.loadLibrary("jidmclib");
 	}
 	
 	public static void main(String argv[]) {
 		model();
 		try{
 			modelInvalidError();
 		} catch(RuntimeException re) {
 			System.out.println(re);
 		}
 		try{
 			modelSyntaxError();
 		} catch(RuntimeException re) {
 			System.out.println(re);
 		}
 		try{
 			modelRuntimeError();
 		} catch(RuntimeException re) {
 			System.out.println(re);
 		}
 		trajectory();
 		ctrajectory();
 		cycles();
 	}
 
 	static void model() {
 		String modelBuffer = readFile(new File("logistic.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 	}
 
 	static void modelInvalidError() {
 		String modelBuffer = readFile(new File("invalid.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 		m.getType();
 	}
 
 	static void modelSyntaxError() {
 		String modelBuffer = readFile(new File("syntaxError.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 		m.getType();
 	}
 
 	static void modelRuntimeError() {
 		String modelBuffer = readFile(new File("runtimeError.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 		m.getType();
 		double ans[] = new double[1];
 		m.f(new double[] {0.3}, new double[] {0.7}, ans);
 	}
 	
 	static void trajectory() {
 		String modelBuffer = readFile(new File("logistic.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 		Trajectory tr = new Trajectory(m, new double[] {0.3}, new double[] {0.7});
 		for(int i=0; i<10; i++)
 			tr.dostep();
 	}
 	
 	static void ctrajectory() {
 		String modelBuffer = readFile(new File("lorenz.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 		CTrajectory tr = new CTrajectory(m, new double[] {0.3, 1.0, 2.0}, new double[] {1.0,2.0,3.0},
 			0.005, idmc.getGsl_odeiv_step_rk4());
 		for(int i=0; i<10; i++)
 			tr.dostep();
 	}
 	
 	static void cycles() {
 		String modelBuffer = readFile(new File("logistic.lua"));
 		Model m = new Model(modelBuffer, modelBuffer.length());
 		double ans[] = new double[]{23.0};
 		double eigmods[] = new double[]{22.1};
 		idmc.idmc_cycles_find(m, new double[] {0.3}, new double[] {0.6}, 2, 1e-5, 1000, ans, eigmods);
 		System.out.println(ans[0]);
 		System.out.println(eigmods[0]);
 	}
 	
 	static String readFile(File f) {
 		StringBuffer contents = new StringBuffer();
 		BufferedReader input = null;
 		try {
 			input = new BufferedReader( new FileReader(f) );
 			String line = null; //not declared within while loop
 			while (( line = input.readLine()) != null){
 				contents.append(line);
 				contents.append(System.getProperty("line.separator"));
 			}
 		}
 		catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		}
 		catch (IOException ex){
 			ex.printStackTrace();
 		}
 		finally {
 			try {
 				if (input!= null) {
 					input.close();
 				}
 			}
 			catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 		return contents.toString();
 	}
 }
