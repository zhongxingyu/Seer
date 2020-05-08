 package cs380C.compiler;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.*;
 
 public class compiler_ui {
 
 	public enum opt
 	{
 		DCE
 	}
 	
 	public enum backend
 	{
 		C,
 		CFG
 	}
 	
 	private static LinkedList<String> cmdlist = new LinkedList<String>();
 	private static LinkedList<opt> optimizations = new LinkedList<opt>();
 	private static backend outputType;
 	
 	/**
 	 * @param args
 	 */
	public static void main(String[] args) throws Exception {
		try {			
 			// Parse CommandLine Arguments
 			final Scanner r = new Scanner(new File(args[0]));
 			//final Scanner r = new Scanner(System.in);
 			final Writer w = new PrintWriter(System.out);
 			
 			cmdlist = read(r);
 			
 			for (String s: args) {
 	            if(s.startsWith("-backend="))
 	            {
 	            	outputType = backend.valueOf(s.split("=")[1].toUpperCase());
 	            }
 	            else if(s.startsWith("-opt="))
 	            {
 	            	String[] opts = s.split("=")[1].split(",");
 	            	for(String o : opts)
 	            	{
 	            		optimizations.add(opt.valueOf(o.toUpperCase()));
 	            	}
 	            }
 	        }
 			
 			for(opt o : optimizations)
 				performOptimization(o);
 			
 			generateOutput(w, outputType);
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
 	}
 
 	private static void generateOutput(Writer w, backend outputType) throws Exception {
 		switch(outputType)
 		{
 			case C:
 				w.write(cscTranslator.translator(toString(cmdlist)));
 			break;
 			case CFG:
 				w.write(new CFG(cmdlist).toString());
 			break;
 			default:
 				throw new Exception("Unrecognized Output Type");
 		}
 		w.flush();
 	}
 
 	private static Scanner toString(LinkedList<String> output) {
 		StringBuilder out = new StringBuilder();
 		for(String s : output)
 		{
 			out.append(s);
 			out.append("\n");
 		}
 		return new Scanner(out.toString());
 	}
 
 	private static void performOptimization(opt o) throws Exception {
 		switch(o)
 		{
 			case DCE:
 				cmdlist = DCE.instance().performOptimization(cmdlist);
 			break;
 			default:
 				throw new Exception("Unrecognized Optimization");
 		}
 	}
 
 	private static LinkedList<String> read(Scanner r) {
 		LinkedList<String> file = new LinkedList<String>();
 		while(r.hasNext())
 			file.add(r.nextLine());
 		return file;
 	}
 }
