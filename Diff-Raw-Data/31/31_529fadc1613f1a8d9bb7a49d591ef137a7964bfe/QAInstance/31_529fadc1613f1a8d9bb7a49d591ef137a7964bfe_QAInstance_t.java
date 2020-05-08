 package models.queryanalyzer.ds;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 
 public class QAInstance {
 	private int _slots;
 	private int _advetisers;
 	private double[] _avgPos;
 	private int[] _agentIds;
 	private int _agentIndex;
 	private int _impressions;
 	private int _impressionsUB;
 	
 	public QAInstance(int slots, int advetisers, double[] avgPos, int[] agentIds, int agentIndex, int impressions, int impressionsUB){
 		assert(avgPos.length == advetisers);
 		assert(agentIds.length == advetisers);
 		assert(advetisers == 0 || (advetisers > agentIndex && agentIndex >= 0));
 		_slots = slots;
 		_advetisers = advetisers;
 		_avgPos = avgPos;
 		_agentIds = agentIds;
 		_agentIndex = agentIndex;
 		_impressions = impressions;
 		_impressionsUB = impressionsUB;
 		
 		
		//
 		
 		
		while(!feasibleOrder(getAvgPosOrder())){
			int[] apOrder = getAvgPosOrder();
			
			int foundTooBigStart = 0;
			int foundTooBigStop = 0;
			for(int i=0; i<_slots && i<_advetisers; i++){
				if(_avgPos[apOrder[i]] > i+1){
					foundTooBigStart = i+1;
					foundTooBigStop = (int)Math.ceil(_avgPos[apOrder[i]]);
					break;
				}
			}
			
			if(foundTooBigStart > 0){
				addPaddingAgents(foundTooBigStart, foundTooBigStop);
			}
 		}
 		
 		assert(feasibleOrder(getAvgPosOrder())) : "addPaddingAgents broke did not work...";
 	}
 	
 	//pads the auction with "fake" advertisers so that the instance is feasible
 	//Feasible means every agent starts in a position greater or equal to their avg pos
 	//assumes the highest agent ID is 99
 	private void addPaddingAgents(int startSlot, int stopSlot){
 		int oldAdvertisers = _advetisers;
 		double[] oldAvgPos = _avgPos;
 		int[] oldAgentIds = _agentIds;
 		
 		int newAdvertisers = oldAdvertisers+stopSlot-startSlot;
 		double[] newAvgPos = new double[newAdvertisers];
 		int[] newAgentIds = new int[newAdvertisers];
 		
 		for(int i=0; i < oldAdvertisers; i++){
 			newAvgPos[i] = oldAvgPos[i];
 			newAgentIds[i] = oldAgentIds[i];
 		}
 		
 		for(int i=0; i < stopSlot-startSlot; i++){
 			newAvgPos[oldAdvertisers+i] = startSlot+i;
 			newAgentIds[oldAdvertisers+i] = 100+i;
 		}
 		
 		_advetisers = newAdvertisers;
 		_avgPos = newAvgPos;
 		_agentIds = newAgentIds;
 	}
 	
 	public int getNumSlots() {return _slots;}
 	public int getNumAdvetisers() {return _advetisers;}
 	public double[] getAvgPos() {return _avgPos;}
 	public int[] getAgentIds() {return _agentIds;}
 	public int getAgentIndex() {return _agentIndex;}
 	public int getImpressions() {return _impressions;}
 	public int getImpressionsUB() {return _impressionsUB;}
 	
 	public int[] getBidOrder(QAData data){
 		double[] bids = new double[_advetisers];
 		int[] bidOrder = new int[_advetisers];
 		for(int i=0; i < _advetisers; i++){
 			bids[i] = data._agentInfo[_agentIds[i]-1].bid;
 			bidOrder[i] = i;
 		}
 		
 		sortListsDecending(bidOrder, bids);
 		
 		//System.out.println("Bid order "+Arrays.toString(bidOrder));
 		//System.out.println("Bid value "+Arrays.toString(bids));
 		
 		return bidOrder;
 	}
 	
 	private boolean feasibleOrder(int[] order) {
 		for(int i=0; i < order.length; i++){
 			int startPos = Math.min(i+1,_slots);
 			if(startPos < _avgPos[order[i]]){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public int[] getAvgPosOrder() {
 		double[] pos = new double[_advetisers];
 		int[] posOrder = new int[_advetisers];
 		for(int i=0; i < _advetisers; i++){
 			pos[i] = -_avgPos[i];
 			posOrder[i] = i;
 		}
 		
 		sortListsDecending(posOrder, pos);
 		
 		//System.out.println("Pos order "+Arrays.toString(posOrder));
 		//System.out.println("Pos value "+Arrays.toString(pos));
 		
 		//not nessissary in general, but makes results consistent with the comet model
 		sortTiesAccending(posOrder, pos);
 		
 		//System.out.println("Pos order (break ties) "+Arrays.toString(posOrder));
 		//System.out.println("Pos value (break ties) "+Arrays.toString(pos));
 		
 		
 		return posOrder;
 	}
 	
 	public int[] getCarletonOrder() {
 		int[] avgPosOrder = getAvgPosOrder();
 		boolean[] avdAssigned = new boolean[_advetisers];
 		//boolean[] posAssigned = new boolean[_advetisers];
 		int[] carletonOrder = new int[_advetisers];
 		for(int i=0; i < _advetisers; i++){
 			avdAssigned[i] = false;
 			carletonOrder[i] = -1;
 		}
 		
 		ArrayList<HashSet<Integer>> advWholePos = new ArrayList<HashSet<Integer>>();
 		for(int i=0; i < _slots; i++){
 			advWholePos.add(i, new HashSet<Integer>());
 		}
 		
 		for(int i=0; i < _advetisers; i++){
 			if((((int)(_avgPos[i] * 100000) % 100000)) == 0){
 				int slot = (int)_avgPos[i];
 				if(slot < _slots){ //very important not to keep the last slot
 					 HashSet<Integer> advs = advWholePos.get(slot-1);
 					 advs.add(i);
 				}
 			}
 		}
 		
 		for(int i=0; i < _slots-1; i++){ //this should hold as long as we don't consider the last slot
 			HashSet<Integer> advs = advWholePos.get(i);
 			//assert(advs.size() <= 1) : "this may need to go away in new game data";
 			if(advs.size() > 0){
 				int adv = advs.iterator().next(); //no good idea on how to pick, just do random.
 				
 				assert(carletonOrder[i] < 0);
 				carletonOrder[i] = adv;
 				assert(!avdAssigned[adv]);
 				avdAssigned[adv] = true;
 			}
 		}
 		
 		for(int i=0; i < _advetisers; i++){ 
 			int a = avgPosOrder[i];
 			if(!avdAssigned[a]){
 				for(int j=0; j < _advetisers; j++){ 
 					if(carletonOrder[j] < 0){
 						carletonOrder[j] = a;
 						avdAssigned[a] = true;
 						break;
 					}
 				}
 			}
 		}
 		
 		
 		
 		
 		return carletonOrder;
 	}
 	
 	public int[] getTrueImpressions(QAData data) {
 		int[] impressions = new int[_advetisers];
 		for(int i=0; i < _advetisers; i++){
 			impressions[i] = data._agentInfo[_agentIds[i]-1].impressions;
 		}
 		
 		return impressions;
 	}
 	
 	private void sortListsDecending(int[] ids, double[] vals){
 		assert(ids.length == vals.length);
 		int length = ids.length;
 		
 		for(int i=0; i < length; i++){
 			for(int j=i+1; j < length; j++){
 				if(vals[i] < vals[j]){
 					double tempVal = vals[i];
 					int tempId = ids[i];
 					
 					vals[i] = vals[j];
 					ids[i] = ids[j];
 					
 					vals[j] = tempVal;
 					ids[j] = tempId;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * sorts ties by accending agent id
 	 * This has 0 impact on the algorithm, it was added just to have identical results 
 	 * with the comet model
 	 * @param ids
 	 * @param vals
 	 */
 	private void sortTiesAccending(int[] ids, double[] vals){
 		assert(ids.length == vals.length);
 		int length = ids.length;
 		
 		for(int i=0; i < length; i++){
 			for(int j=i+1; j < length; j++){
 				if(vals[i] == vals[j] && ids[i] > ids[j]){
 					double tempVal = vals[i];
 					int tempId = ids[i];
 					
 					vals[i] = vals[j];
 					ids[i] = ids[j];
 					
 					vals[j] = tempVal;
 					ids[j] = tempId;
 				}
 			}
 		}
 	}
 	
 	
 	public String toString() {
 		String temp = "";
 		temp += "Slots: "+_slots+"\n";
 		temp += "Advertisers: "+_advetisers+"\n";
 		temp += Arrays.toString(_agentIds)+"\n";
 		temp += Arrays.toString(_avgPos)+"\n";
 		temp += "index: "+_agentIndex+"\n";
 		temp += "impressions: "+_impressions+"\n";
 		return temp;
 	}
 
 
 	
 }
