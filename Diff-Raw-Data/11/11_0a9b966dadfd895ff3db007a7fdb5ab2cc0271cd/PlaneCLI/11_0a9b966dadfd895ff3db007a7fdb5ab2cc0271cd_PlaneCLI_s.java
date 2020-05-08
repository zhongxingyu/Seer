 package sat.plane;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 
 import sat.cli.CLI;
 
public class PlaneCLI extends CLI {
 	private Plane plane;
 	
 	public PlaneCLI(Plane plane, InputStream in, PrintStream out) {
 		super(in, out, "Plane> ");
 		this.plane = plane;
 	}
 }
