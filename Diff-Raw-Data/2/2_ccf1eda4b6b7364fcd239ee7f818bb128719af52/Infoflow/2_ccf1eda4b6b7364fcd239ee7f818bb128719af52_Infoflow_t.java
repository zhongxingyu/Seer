 package soot.jimple.infoflow;
 
 import heros.InterproceduralCFG;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import soot.PackManager;
 import soot.Scene;
 import soot.SceneTransformer;
 import soot.SootClass;
 import soot.SootMethod;
 import soot.Transform;
 import soot.Unit;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.infoflow.util.ArgBuilder;
 import soot.jimple.infoflow.util.EntryPointCreator;
 import soot.jimple.infoflow.util.SootMethodRepresentationParser;
 import soot.jimple.toolkits.ide.JimpleIFDSSolver;
 import soot.options.Options;
 
 public class Infoflow implements IInfoflow {
 
 	@Override
 	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources, List<String> sinks) {
 		if(sources == null || sources.isEmpty()){
 			System.out.println("Error: sources are empty!");
 			return;
 		}
 		if(sinks == null || sinks.isEmpty()){
 			if(sinks == null){
 				sinks = new ArrayList<String>();
 			}
 			System.out.println("Warning: sinks are empty!");
 		}
 		
 		// convert to internal format:
 		SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
 		// className as String and methodNames as string in soot representation
 		HashMap<String, List<String>> classes = parser.parseClassNames(entryPoints);
 
 		// add SceneTransformer:
 		addSceneTransformer(sources, sinks);
 
 		for (Entry<String, List<String>> classEntry : classes.entrySet()) {
 			// prepare soot arguments:
 			ArgBuilder builder = new ArgBuilder();
 			String[] args = builder.buildArgs(path, classEntry.getKey());
 
 			// Anpassungen fuer kuerzere Laufzeit:
 			List<String> includeList = new LinkedList<String>();
 			includeList.add("java.lang.");
 			includeList.add("java.util.");
 			includeList.add("sun.misc.");
 			includeList.add("android.");
 			includeList.add("org.apache.http.");
 			includeList.add("de.test.");
 			includeList.add("soot.");
 			includeList.add("com.example.");
 			includeList.add("com.jakobkontor.");
 //			includeList.add("java.net.");
 			Options.v().set_include(includeList);
 			Options.v().set_allow_phantom_refs(true);
 			Options.v().set_no_bodies_for_excluded(true);
 
 			Options.v().parse(args);
 
 			// entryPoints are the entryPoints required by Soot to calculate Graph - if there is no main method, we have to create a new main method and use it as entryPoint, but store our real entryPoints
 			List<SootMethod> sootEntryPoints;
 			Scene.v().loadNecessaryClasses();
 			SootClass c = Scene.v().forceResolve(classEntry.getKey(), SootClass.BODIES);
 
 			c.setApplicationClass();
 
 			EntryPointCreator epCreator = new EntryPointCreator();
 
 			sootEntryPoints = epCreator.createDummyMain(classEntry, c);
 			
 			Scene.v().setEntryPoints(sootEntryPoints);
 			
 			PackManager.v().runPacks();
 			
 		}
 
 	}
 
 	public void addSceneTransformer(final List<String> sources, final List<String> sinks) {
 		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
 			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
 
 				InfoflowProblem problem = new InfoflowProblem(sources, sinks);
 				for (SootMethod ep : Scene.v().getEntryPoints()) {
 					problem.initialSeeds.add(ep.getActiveBody().getUnits().getFirst());
 				}
 
				JimpleIFDSSolver<Abstraction> solver = new JimpleIFDSSolver<Abstraction>(problem);
 
 				solver.solve(0);
 				solver.dumpResults(); // only for debugging
 
 				for (SootMethod ep : Scene.v().getEntryPoints()) {
 
 					Unit ret = ep.getActiveBody().getUnits().getLast();
 
 					System.err.println(ep.getActiveBody());
 
 					System.err.println("----------------------------------------------");
 					System.err.println("At end of: " + ep.getSignature());
 					System.err.println(solver.ifdsResultsAt(ret).size() + " Variables:");
 					System.err.println("----------------------------------------------");
 
 					for (Abstraction l : solver.ifdsResultsAt(ret)) {
 						System.err.println(l.getCorrespondingMethod() +": "+ l.getTaintedObject() + " contains value from " + l.getSource());
 					}
 					System.err.println("---");
 				}
 				
 				HashMap<String, List<String>> results = problem.results;
 				for(Entry<String, List<String>> entry : results.entrySet()){
 					System.out.println("The sink " + entry.getKey() + " was called with values from the following sources:");
 					for(String str : entry.getValue()){
 						System.out.println("- " + str);
 					}	
 				}
 			}
 		});
 
 		PackManager.v().getPack("wjtp").add(transform);
 	}
 
 }
