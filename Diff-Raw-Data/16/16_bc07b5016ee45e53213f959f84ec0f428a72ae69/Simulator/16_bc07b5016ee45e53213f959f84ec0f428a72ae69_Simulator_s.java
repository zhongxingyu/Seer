 import Jama.*;
 
 public class Simulator {
   private Renderer rend;
 	private World world;
 	private double timeStep;
 
 	public void read(String filename) {
     In specFile = new In(filename);
 		timeStep = specFile.readDouble();
 		StdOut.println("Time step: " + timeStep);
 		int stateDim = specFile.readInt();
 		StdOut.println("State dimension: " + stateDim);
 		int controlDim = specFile.readInt();
 		StdOut.println("Control dimension: " + controlDim);
 		Matrix initCond = new Matrix(stateDim, 1);
 		for (int i = 0; i < stateDim; i++) {
 			initCond.set(i, 0, specFile.readDouble());
 		} 
 		StdOut.println("Initial conditions: ");
 		initCond.print(2, 1);
 		Matrix initControl = new Matrix(controlDim, 1);
 		for (int i = 0; i < controlDim; i++) {
 			initControl.set(i, 0, specFile.readDouble());
 		} 
 		StdOut.println("Initial controls: ");
 		initControl.print(2, 1);
 		Matrix sys = new Matrix(stateDim, stateDim);
 		for (int i = 0; i < stateDim; i++) {
 			for (int j = 0; j < stateDim; j++) {
 				sys.set(i, j, specFile.readDouble());
 			} 
 		} 
 		StdOut.println("Linear system: ");
 		sys.print(2, 1);
 		Matrix controlSys = new Matrix(stateDim, controlDim);
 		for (int i = 0; i < stateDim; i++) {
 			for (int j = 0; j < controlDim; j++) {
 				controlSys.set(i, j, specFile.readDouble());
 			} 
 		} 
 		StdOut.println("Control model: ");
 		sys.print(2, 1);
 
 		// Initialize instance variables
 	}
 
 	public void advance() {
 		Matrix curState = world.step();
 		rend.update(curState);
 	}
 
   public static void main (String[] argv) {
 		Simulator sim = new Simulator();
		sim.read("CarWorld.wld");
 		while(true) {
 			sim.advance();
 			try { Thread.sleep(100); } catch(Exception e) {}
 		}
 	}
 }
