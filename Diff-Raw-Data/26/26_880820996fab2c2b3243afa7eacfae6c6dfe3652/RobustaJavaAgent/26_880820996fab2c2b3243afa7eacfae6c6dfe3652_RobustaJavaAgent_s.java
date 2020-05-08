 package fr.adele.robusta;
 
 import java.lang.instrument.*;
 import java.util.UUID;
 
 public class RobustaJavaAgent {
 
 	// private static Instrumentation inst;
 
 	// public static Instrumentation getInstrumentation() {
 	// return inst;
 	// }
 
 	private static final Object INSTRUMENTATION_UUID = UUID.fromString("021df202-6bb0-11e2-8f99-5c260a385954");
 
	public Instrumentation getInstrumentation() {
		return (Instrumentation) System.getProperties().get(
				INSTRUMENTATION_UUID);
 	}
 
 	public static void premain(String agentArgs, Instrumentation inst) {
 		System.out.println("ROBUSTA: RobustaJavaAgent" + inst.getClass() + ": " + inst);
 		// RobustaJavaAgent.inst = inst;
 
		//Make insttrumentation reachable from Robusta Shelbie Command
 		System.getProperties().put(INSTRUMENTATION_UUID, inst);
 
 	}
 }
