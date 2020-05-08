 package pls.reduce;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.PriorityQueue;
 
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.log4j.Logger;
 
 import pls.PlsUtil;
 import pls.SolutionData;
 import pls.vrp.VrpExtraDataHandler;
 import pls.vrp.VrpSolvingExtraData;
 
 public abstract class ChooserReducer extends MapReduceBase implements Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable> {
 	
 	private static final Logger LOG = Logger.getLogger(ChooserReducer.class);
 	
 	private int k = -1;
 	private double bestCostAlways = -1;
 	private int roundTime = -1;
 	private boolean useBestForAll;
 	
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
 		
 		if (key.equals(PlsUtil.METADATA_KEY)) {
 			//first value is info regarding the problem and best solution
 			BytesWritable auxInfo = values.next();
 			ByteArrayInputStream bais = new ByteArrayInputStream(auxInfo.getBytes(), 0, auxInfo.getLength());
 			DataInputStream dis = new DataInputStream(bais);
 			k = dis.readInt();
 			bestCostAlways = dis.readDouble();
 			roundTime = dis.readInt();
 			useBestForAll = dis.readBoolean();
 			LOG.info("Received metadata: k=" + k + ", bestCostAlways=" + bestCostAlways + ", roundTime=" + roundTime + 
 					", useBestForAll=" + useBestForAll);
 			return;
 		} else if (k == -1) {
 			LOG.info("Received solutions before metadata, aborting");
 			return;
 			//log something here
 			//output some sort of error code
 		}
 		
 		Class<SolutionData> solDataClass = getSolutionDataClass();
 		
 		SolutionData bestSolThisRound = null;
 		ArrayList<SolutionData> solsThisRound = new ArrayList<SolutionData>();
 		//find the best solution
 		try {
 			while (values.hasNext()) {
 				BytesWritable bytes = values.next();
 				SolutionData solData = solDataClass.newInstance();
 				solData.init(bytes);
				if (solData.getBestCost() < bestSolThisRound.getBestCost()) {
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
 		
 		//handle extra datas
 		ArrayList<Writable> extraDatas = new ArrayList<Writable>();
 		for (SolutionData solData : solsThisRound) {
 			if (solData != bestSolThisRound && solData.getExtraData() != null) {
 				extraDatas.add(solData.getExtraData());
 			}
 		}
 		VrpExtraDataHandler handler = new VrpExtraDataHandler();
 		List<Writable> helperDatas = handler.makeNextRoundHelperDataFromExtraData(extraDatas, solsThisRound.size());
 		
 		//prepare inputs to next round
 		LOG.info("Best cost this round: " + bestSolThisRound.getBestCost());
 		List<BytesWritable> outSols = chooseNextRoundSols(solsThisRound, bestSolThisRound, false);
 		long nextRoundFinishTime = System.currentTimeMillis() + roundTime;
 		
 		//write out sols
 		for (BytesWritable outSol : outSols) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(baos);
 			dos.write(outSol.getBytes(), 0, outSol.getLength());
 			LOG.info("outSol.getLength(): " + outSol.getLength());
 			Writable helperData = helperDatas.remove(helperDatas.size()-1);
 			LOG.info("About to write helper data with " + ((VrpSolvingExtraData)helperData).getNeighborhoods());
 			helperData.write(dos);
 			BytesWritable outData = new BytesWritable(baos.toByteArray());
 			LOG.info("output bytes hashCode: " + outData.hashCode());
 			LOG.info("outData.getBytes().length: " + outData.getBytes().length);
 			output.collect(PlsUtil.getMapSolKey(nextRoundFinishTime), outData);
 		}
 
 		//write out the metadata key
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 		dos.writeInt(k);
 		dos.writeDouble(bestCostAlways);
 		dos.writeInt(roundTime);
 		dos.writeBoolean(useBestForAll);
 		BytesWritable metadata = new BytesWritable(baos.toByteArray());
 		output.collect(PlsUtil.METADATA_KEY, metadata);
 	}
 	
 	private List<BytesWritable> chooseNextRoundSols(List<SolutionData> solsThisRound, SolutionData bestSolThisRound,
 			boolean useBestForAll) {
 		List<BytesWritable> outSols = new ArrayList<BytesWritable>(solsThisRound.size());
 		if (bestSolThisRound.getBestCost() < bestCostAlways) {
 			bestCostAlways = bestSolThisRound.getBestCost(); //for passing on
 			int nMappers = solsThisRound.size();
 			//choose best k solutions
 			List<SolutionData> bestSols = chooseKBest(solsThisRound, k);
 			BytesWritable val;
 			//first write out the k best
 			for (SolutionData solData : bestSols) {
 				val = solData.getEndSolutionBytes();
 				LOG.info("Writing out sol with cost " + solData.getBestCost());
 				outSols.add(val);
 			}
 			
 			//then write out the best solution(s)
 			int nBest = nMappers - k;
 			for (int i = 0; i < nBest; i++) {
 				LOG.info("Writing out best sol with cost " + bestSolThisRound.getBestCost());
 				if (useBestForAll) {
 					val = bestSolThisRound.getBestSolutionBytes();
 				} else {
 					//last is the best, so if not visible, we want to include it the most times
 					int index = bestSols.size() - 1 - i % bestSols.size();
 					val = bestSols.get(index).getBestSolutionBytes();
 				}
 
 				outSols.add(val);
 			}
 		} else { //just continue with what we've got
 			LOG.info("No best cost improvement, still " + bestCostAlways);
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
 	
 	public abstract Class<SolutionData> getSolutionDataClass();
 }
