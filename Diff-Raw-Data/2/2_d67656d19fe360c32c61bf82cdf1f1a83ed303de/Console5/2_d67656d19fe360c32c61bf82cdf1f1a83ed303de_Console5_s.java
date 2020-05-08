 package edu.sjtu.simpl.ui;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 import edu.sjtu.simpl.exception.SimPLNotDefinedException;
 import edu.sjtu.simpl.exception.SimPLRuntimeException;
 import edu.sjtu.simpl.exception.SimPLTypeException;
 import edu.sjtu.simpl.grammar.SimPL;
 import edu.sjtu.simpl.grammar.SimpleNode;
 import edu.sjtu.simpl.runtime.Executor;
 import edu.sjtu.simpl.runtime.IExecutor;
 import edu.sjtu.simpl.runtime.Memory;
 import edu.sjtu.simpl.runtime.RunTimeState;
 import edu.sjtu.simpl.syntax.Expression;
 import edu.sjtu.simpl.syntax.Value;
 import edu.sjtu.simpl.type.Type;
 import edu.sjtu.simpl.util.Log;
 import edu.sjtu.simpl.validate.ComplilerValidator;
 import edu.sjtu.simpl.validate.TypeMap;
 import edu.sjtu.simpl.visitor.SyntaxVisitor;
 
 public class Console5 {
 	private static InputStream is = System.in;
 	private static PrintStream os = System.out;
 	private static boolean isShell = true;
 	private static String cmdPrefix = "";
 
 	private static String getRstFileName(String fileName) {
 		int idx = fileName.lastIndexOf(".");
 		String name = fileName.substring(0, idx);
 		return name + ".rst";
 	}
 
 	private static void parseArgs(String args[]) {
 		if (args.length == 1) {
 			isShell = true;
 			cmdPrefix = "SimPL> ";
 		} else if (args.length == 2) {
 			isShell = false;
 
 			try {
 				is = new FileInputStream(args[1]);
 			} catch (FileNotFoundException e) {
 				System.err.println("Args Error: " + args[1] + " NOT found!");
 				System.exit(-1);
 			}
 
 			String rstName = getRstFileName(args[1]);
 			File rst = new File(rstName);
 			try {
 				os = new PrintStream(rst);
 			} catch (FileNotFoundException e) {
 				System.err.println("Error: Cannot create " + rstName + " !");
 				System.exit(-1);
 			}
 		} else {
 			System.out.println("Usage1: java -jar SimPL.jar -s");
 			System.out.println("Usage2: java -jar SimPL.jar -f sample.spl");
 			System.exit(-2);
 		}
 	}
 
 	public static void main(String args[]) {
 		parseArgs(args);
 
 		SimPL parser = new SimPL(is);
 
 		do {
 			SimpleNode n = null;
 			parser.ReInit(is);
 
 			try {
 				System.out.print(cmdPrefix);
 				n = parser.Program();
 			} catch (Throwable e) {
 				// System.out.println("Syntax Error!");
 				// e.printStackTrace();
 				// break;
 			}
 
 			if (n == null) {
 				os.println(cmdPrefix + "Syntax Error!");
 				continue;
 			}
 
 			Expression root = null;
 			try {
 				SyntaxVisitor visitor = new SyntaxVisitor();
 				root = (Expression) n.jjtAccept(visitor, null);
 				// System.out.println(root.toString());
 			} catch (Exception e) {
 				continue;
 			}
 			
			Log.debug("..................complier time........................");
 
 			//Log.debug("..................complier time........................");
 			Type t = null;
 			try {
 				ComplilerValidator validator = new ComplilerValidator();
 
 				t = validator.V(root, new TypeMap());
 			} catch(SimPLTypeException e)
 			{
 				os.println(cmdPrefix + "Type Error:"+e.getMessage());
 			}catch(SimPLNotDefinedException e)
 			{
 				os.println(cmdPrefix + "Runtime Error:"+e.getMessage());
 			}
 			
 			if (t == null) {
 				continue;
 			}
 
 			Value v = null;
 			//Log.debug(".................run time.........................");
 
 			try {
 				IExecutor exe = new Executor();
 				RunTimeState state = new RunTimeState();
 				v = exe.M(root, state);
 				Memory.getInstance().clean();
 			} catch (SimPLTypeException e) {
 				os.println(cmdPrefix+"Type Error:" + e.getMessage());
 			} catch (SimPLRuntimeException e) {
 				os.println(cmdPrefix+"runtime error:" + e.getMessage());
 			}
 
 			if (v != null) {
 				os.println(cmdPrefix + v.toString());
 			}
 			
 		} while (isShell);
 
 	}
 }
