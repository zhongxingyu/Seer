 package pls.reduce;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Random;
 
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.log4j.Logger;
 
 import pls.PlsMetadata;
 import pls.PlsUtil;
 import pls.SolutionData;
 import pls.vrp.VrpExtraDataHandler;
 import pls.vrp.LnsExtraData;
 
 public abstract class ChooserReducer extends MapReduceBase implements Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable> {
 	
 	private static final Logger LOG = Logger.getLogger(ChooserReducer.class);
 	
 	/**
 	 * The reduce inputs can take two forms:
 	 * "best"->two BytesWritables
 	 * 		this is the global best solution being passed through
 	 * 		the first BytesWritable contains metadata like k and the global best cost
 	 * 		the second BytesWritable is the serialized global best solution
 	 * "rest"->many BytesWritables
 	 * 		these are the solutions found in by each of the jobs
 	 * 		each BytesWritable contains
 	 * 			any metadata (cost of best solution of run, cost of ending solution of run)
 	 * 			the ending solution of the run
 	 * 				containing T
 	 * 			the best solution found in the run
 	 * 				containing T
 	 */
 	@Override
 	public void reduce(BytesWritable key, Iterator<BytesWritable> values,
 			OutputCollector<BytesWritable, BytesWritable> output, Reporter reporter)
 			throws IOException {
 		
 		Class<SolutionData> solDataClass = getSolutionDataClass();
 		
 		SolutionData bestSolThisRound = null;
 		ArrayList<SolutionData> solsThisRound = new ArrayList<SolutionData>();
 		//find the best solution
 		try {
 			while (values.hasNext()) {
 				BytesWritable bytes = values.next();
 				SolutionData solData = solDataClass.newInstance();
 				solData.init(bytes);
 				if (bestSolThisRound == null || solData.getBestCost() < bestSolThisRound.getBestCost()) {
 					bestSolThisRound = solData;
 				}
 				solsThisRound.add(solData);
 			}
 		} catch (IOException ex) {
 			LOG.error("Error reading data, this shouldn't happen, aborting...", ex);
 			return;
 		} catch (Exception ex) {
 			LOG.error("Error interpreting SolutionData class, aborting...", ex);
 			return;
 		}
 		
 		LOG.info("Received " + solsThisRound.size() + " solution(s)");
 		
 		PlsMetadata metadata = solsThisRound.get(0).getMetadata();
 		
 		//handle extra datas
 		ArrayList<LnsExtraData> extraDatas = new ArrayList<LnsExtraData>();
 		for (SolutionData solData : solsThisRound) {
 			if (solData != bestSolThisRound && solData.getExtraData() != null) {
 				extraDatas.add((LnsExtraData)solData.getExtraData());
 			}
 		}
		VrpExtraDataHandler handler = new VrpExtraDataHandler(new Random(), metadata.getHelperDataNumNeighbors());
 		List<LnsExtraData> helperDatas = handler.makeNextRoundHelperDataFromExtraData2(extraDatas, solsThisRound.size());
 		
 		//prepare inputs to next round
 		LOG.info("Best cost this round: " + bestSolThisRound.getBestCost());
 		List<BytesWritable> outSols = chooseNextRoundSols(solsThisRound, bestSolThisRound, metadata);
 		long nextRoundFinishTime = System.currentTimeMillis() + metadata.getRoundTime();
 		
		updateMetadata(metadata, (LnsExtraData)bestSolThisRound.getExtraData(), extraDatas);
 		//write out sols
 		for (BytesWritable outSol : outSols) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(baos);
 			dos.write(outSol.getBytes(), 0, outSol.getLength());
 			metadata.write(dos);
 			Writable helperData = helperDatas.remove(helperDatas.size()-1);
 			LOG.info("About to write helper data with " + ((LnsExtraData)helperData).getNeighborhoods());
 			helperData.write(dos);
 			BytesWritable outData = new BytesWritable(baos.toByteArray());
 			output.collect(PlsUtil.getMapSolKey(nextRoundFinishTime), outData);
 		}
 	}
 	
 	private List<BytesWritable> chooseNextRoundSols(List<SolutionData> solsThisRound, SolutionData bestSolThisRound, 
 			PlsMetadata metadata) {
 		List<BytesWritable> outSols = new ArrayList<BytesWritable>(solsThisRound.size());
 		if (bestSolThisRound.getBestCost() < metadata.getBestCostAlways()) {
 			metadata.setBestCostAlways(bestSolThisRound.getBestCost()); //for passing on
 			int nMappers = solsThisRound.size();
 			//choose best k solutions
 			List<SolutionData> bestSols = chooseKBest(solsThisRound, metadata.getK());
 			BytesWritable val;
 			//first write out the k best
 			for (SolutionData solData : bestSols) {
 				val = solData.getEndSolutionBytes();
 				LOG.info("Writing out sol with cost " + solData.getBestCost());
 				outSols.add(val);
 			}
 			
 			//then write out the best solution(s)
 			int nBest = nMappers - metadata.getK();
 			for (int i = 0; i < nBest; i++) {
 				LOG.info("Writing out best sol with cost " + bestSolThisRound.getBestCost());
 				if (metadata.getUseBestForAll()) {
 					val = bestSolThisRound.getBestSolutionBytes();
 				} else {
 					//last is the best, so if not visible, we want to include it the most times
 					int index = bestSols.size() - 1 - i % bestSols.size();
 					val = bestSols.get(index).getBestSolutionBytes();
 				}
 
 				outSols.add(val);
 			}
 		} else { //just continue with what we've got
 			LOG.info("No best cost improvement, still " + metadata.getBestCostAlways());
 			for (SolutionData solution : solsThisRound) {
 				//TODO: should we need to copy here?
 				BytesWritable val = solution.getEndSolutionBytes();
 				outSols.add(val);
 			}
 		}
 		
 		return outSols;
 	}
 	
 	/**
 	 * Returns in order of highest cost first.
 	 */
 	protected ArrayList<SolutionData> chooseKBest(List<SolutionData> solDatas, int k) {
 		if (k == 0) {
 			return new ArrayList<SolutionData>();
 		}
 		
 		PriorityQueue<SolutionData> maxHeap = new PriorityQueue<SolutionData>();
 		for (SolutionData solData : solDatas) {
 			if (maxHeap.size() < k || solData.getBestCost() < maxHeap.peek().getBestCost()) {
 				if (maxHeap.size() >= k) {
 					maxHeap.remove();
 				}
 				maxHeap.add(solData);
 			}
 		}
 		
 		ArrayList<SolutionData> kbest = new ArrayList<SolutionData>();
 		while (!maxHeap.isEmpty()) {
 			kbest.add(maxHeap.remove());
 		}
 		
 		return kbest;
 	}
 	
 	private void updateMetadata(PlsMetadata metadata, LnsExtraData bestExtraData, List<LnsExtraData> otherExtraDatas) {
 		//tally num working
 		int numWorking = 0;
 		for (LnsExtraData extraData : otherExtraDatas) {
 			if (extraData.getRegularTime() > metadata.getRoundTime() / 2) {
 				numWorking++;
 			}
 		}
 		if (bestExtraData.getRegularTime() > metadata.getRoundTime() / 2) {
 			numWorking++;
 		}
 		
 		metadata.setRoundStats(/*metadata.getRegularNumSuccessful() + */bestExtraData.getNumRegularSuccessful(),
 				/*metadata.getRegularNumTries() + */bestExtraData.getNumRegularTried(),
 				/*metadata.getHelperNumSuccessful() + */bestExtraData.getNumHelperSuccessful(),
 				/*metadata.getHelperNumTries() + */bestExtraData.getNumHelperTried(),
 				/*metadata.getImprovementRegular() + */bestExtraData.getRegularImprovement(),
 				/*metadata.getImprovementHelper() + */bestExtraData.getHelperImprovement(),
 				bestExtraData.getRegularTime(), bestExtraData.getHelperTime(), numWorking);
 	}
 	
 	public abstract Class<SolutionData> getSolutionDataClass();
 }
