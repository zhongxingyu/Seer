 package br.ufal.cideei.soot.analyses.wholeline;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import profiling.ProfilingTag;
 import soot.Body;
 import soot.BodyTransformer;
 import soot.PatchingChain;
 import soot.Unit;
 import soot.jimple.Jimple;
 import soot.jimple.JimpleBody;
 import soot.toolkits.graph.BriefUnitGraph;
 import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;
 import br.ufal.cideei.soot.instrument.FeatureTag;
 
 public class WholeLineObliviousUninitializedVariablesAnalysis extends BodyTransformer {
 
 	private static WholeLineObliviousUninitializedVariablesAnalysis instance = new WholeLineObliviousUninitializedVariablesAnalysis();
 
 	private WholeLineObliviousUninitializedVariablesAnalysis() {
 	}
 
 	public static WholeLineObliviousUninitializedVariablesAnalysis v() {
 		return instance;
 	}
 
 	@Override
 	protected void internalTransform(Body body, String phase, Map options) {
 		long totalAnalysis = 0;
 
 		long startAnalysis = 0;
 		long endAnalysis = 0;
 
 		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
 
 		int maximumBodySize = 0;
 		int minimalBodySize = 0;
 
 		if (featureTag.size() > 1) {
 			Collection configs = featureTag.getFeatures();
 			for (Object object : configs) {
 
 				Set<String> config = (Set<String>) object;
 				JimpleBody newBody = Jimple.v().newBody(body.getMethod());
 				newBody.importBodyContentsFrom(body);
 
 				PatchingChain<Unit> newBodyUnits = newBody.getUnits();
 				Iterator<Unit> snapshotIterator = newBodyUnits.snapshotIterator();
 
 				while (snapshotIterator.hasNext()) {
 					Unit unit = (Unit) snapshotIterator.next();
 					FeatureTag unitFeatureTag = (FeatureTag) unit.getTag("FeatureTag");
 					if (!unitFeatureTag.belongsToConfiguration(config)) {
 						newBodyUnits.remove(unit);
 					}
 				}
 
 				/*
 				 * If the body size is 0, then cannot continue. Store maximum
 				 * and minimal size.
 				 */
 				int newBodySize = newBodyUnits.size();
 				if (newBodySize == 0) {
 					continue;
 				} else {
 					if (newBodySize > maximumBodySize) {
 						maximumBodySize = newBodySize;
 					}
 					if (newBodySize < minimalBodySize) {
 						minimalBodySize = newBodySize;
 					}
 				}
 
 				BriefUnitGraph newBodyGraph = new BriefUnitGraph(newBody);
 
 				// #ifdef METRICS
 				startAnalysis = System.nanoTime();
 				// #endif
 				new SimpleUninitializedVariableAnalysis(newBodyGraph);
 				// #ifdef METRICS
 				endAnalysis = System.nanoTime();
 				totalAnalysis += (endAnalysis - startAnalysis);
 				// #endif
 			}
 		} else {
 			BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
 			// #ifdef METRICS
 			startAnalysis = System.nanoTime();
 			// #endif
 			new SimpleUninitializedVariableAnalysis(bodyGraph);
 			// #ifdef METRICS
 			endAnalysis = System.nanoTime();
 			totalAnalysis = endAnalysis - startAnalysis;
 			// #endif
 		}
 
 		// #ifdef METRICS
 		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
 		profilingTag.setUvAnalysisTime(totalAnalysis);
 		profilingTag.setPreprocessingTime(0);
 		
		// minimal = (minSize* maxTime)/maxSize 
		double minimalProportionalJimplificationTime = (minimalBodySize * profilingTag.getJimplificationTime())/maximumBodySize;
		profilingTag.setJimplificationTime((profilingTag.getJimplificationTime() + Math.round(minimalProportionalJimplificationTime))/2);
 		// #endif
 	}
 }
