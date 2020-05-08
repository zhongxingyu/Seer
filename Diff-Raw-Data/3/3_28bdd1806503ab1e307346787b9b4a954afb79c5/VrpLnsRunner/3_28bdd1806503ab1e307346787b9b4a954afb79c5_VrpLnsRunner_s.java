 package pls.vrp;
 
 import java.util.List;
 import java.util.Random;
 
 import org.apache.hadoop.io.Writable;
 import org.apache.log4j.Logger;
 
 import pls.PlsSolution;
 import pls.map.PlsRunner;
 import pls.vrp.hm.VrpCpStats;
 import pls.vrp.hm.VrpSearcher;
 
 public class VrpLnsRunner implements PlsRunner {
 	
 	private static final Logger LOG = Logger.getLogger(VrpLnsRunner.class);
 	
 	private LnsExtraData extraData = new LnsExtraData();
 	private LnsExtraData helperData;
 	
 	@Override
 	public PlsSolution[] run(PlsSolution plsSol, long timeToFinish, Random rand) {
 		VrpPlsSolution solAndStuff = (VrpPlsSolution)plsSol;
 		
 		long startTime = System.currentTimeMillis();
 		
 		VrpSolution sol = solAndStuff.getSolution();
 		VrpProblem problem = sol.getProblem();
 		LnsRelaxer relaxer = new LnsRelaxer(solAndStuff.getRelaxationRandomness(), problem.getMaxDistance(), rand);
 		VrpSearcher solver = new VrpSearcher(problem);
 
 		//if we've been sent neighborhoods to check, check them first
 		if (helperData != null) {
 			LOG.info("Has helper neighborhoods!");
 			List<List<Integer>> neighborhoods = helperData.getNeighborhoods();
 			int numSuccessful = 0;
 			long helperStartTime = System.currentTimeMillis();
 			double beforeSolCost = sol.getToursCost();
 			for (List<Integer> neighborhood : neighborhoods) {
 				VrpCpStats stats = new VrpCpStats();
 				VrpSolution partialSol = new VrpSolution(relaxer.buildRoutesWithoutCusts(sol.getRoutes(), neighborhood), neighborhood, problem);
 				VrpSolution newSol = solver.solve(partialSol, sol.getToursCost(), solAndStuff.getMaxDiscrepancies(), stats, true);
 				if (newSol != null && Math.abs(newSol.getToursCost() - sol.getToursCost()) > .001) {
 					extraData.addNeighborhood(partialSol);
 					sol = newSol;
 					solAndStuff.setSolution(sol);
 					numSuccessful++;
 				}
 			}
 			long helperFinishTime = System.currentTimeMillis();
 			int helperTime = (int)(helperFinishTime - helperStartTime);
 			extraData.setHelperStats(numSuccessful, neighborhoods.size(), beforeSolCost - sol.getToursCost(), helperTime);
 			LOG.info("Helper neighborhoods: " + numSuccessful + " successful / " + neighborhoods.size() + 
 					". Took " + helperTime + " ms");
 		}
 		
 		int numTries = 0;
 		int numSuccesses = 0;
 		double beforeBestCost = sol.getToursCost();
 		long regStartTime = System.currentTimeMillis();
 		outer:
 		while (true) {
 			for (int n = solAndStuff.getCurEscalation(); n <= solAndStuff.getMaxEscalation(); n++) { 
 				for (int i = solAndStuff.getCurIteration(); i < solAndStuff.getMaxIterations(); i++) {
 					if (System.currentTimeMillis() >= timeToFinish) {
 						break outer;
 					}
 					
 					VrpCpStats stats = new VrpCpStats();
 					VrpSolution partialSol = relaxer.relaxShaw(sol, n, -1);
 					
 					VrpSolution newSol = solver.solve(partialSol, sol.getToursCost(), solAndStuff.getMaxDiscrepancies(), stats, true);
 					if (newSol != null && Math.abs(newSol.getToursCost() - sol.getToursCost()) > .001) {
 						extraData.addNeighborhood(partialSol);
 						sol = newSol;
 						solAndStuff.setSolution(sol);
 						i = 0;
 						numSuccesses++;
 					}
 					solAndStuff.setCurEscalation(n);
 					solAndStuff.setCurIteration(i);
 				}
 			}
 			//LOG.info("Starting new search");
 			solAndStuff.setCurEscalation(1);
 			solAndStuff.setCurIteration(0);
			numTries++;
 		}
 		long regEndTime = System.currentTimeMillis();
 		int regTime = (int)(regEndTime - regStartTime);
 		extraData.setRegularStats(numSuccesses, numTries, beforeBestCost - sol.getToursCost(), regTime);
 		
 		long endTime = System.currentTimeMillis();
 		LOG.info("VrpLnsRunner took " + (endTime - startTime) + " ms");
 		
 		return new PlsSolution[] {solAndStuff};
 	}
 
 	@Override
 	public Writable getExtraData() {
 		return extraData;
 	}
 
 	@Override
 	public void setHelperData(Writable helperData) {
 		this.helperData = (LnsExtraData)helperData;
 	}
 }
