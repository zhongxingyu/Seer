 package final_project.control;
 
 import java.util.*;
 
 import final_project.model.*;
 
 public class PoolRoundController {
 	private PoolRound _poolRound;
 	private int _numPools, _numBigPools, _numSmallPools;
 	private IDataStore _dataStore;
 
 	public PoolRoundController(IDataStore ds) {
 		//_poolRound = new FencerPoolRound();
 		_dataStore = ds;
 	}
 
 	public boolean addCompleteResult(CompleteResult result) throws IllegalArgumentException{
         return _poolRound.addCompleteResult(result);
 	}
 
 	public boolean createPools(int poolSize) {
 		//Try to calculate the pool size. If unable to do so, return false
 		try {
 			calcPoolSize(poolSize);
 		} catch (IllegalArgumentException e) {
 			return false;
 		}
 
		_poolRound = new FencerPoolRound(_numPools, poolSize);
         _poolRound.populatePools();
         //_poolRound.assignReferees(refs);
         Collection<IReferee> allRefs = _dataStore.getReferees();
         List<Integer> availableRefs = new  LinkedList<Integer>();
 
         for(IReferee ref: allRefs){
         	if(!ref.getReffing())
         		availableRefs.add(ref.getID());
         }
 
         _poolRound.assignStrips();
         _poolRound.assignReferees(availableRefs);
 
         return true;
 	}
 
 	private void calcPoolSize(int poolSize) throws IllegalArgumentException{
 		PoolSizeCalculator poolSizeCalc = new PoolSizeCalculator(_poolRound.getNumPlayers(), poolSize);
 		_numBigPools = poolSizeCalc.getNumBigPools();
 		_numSmallPools = poolSizeCalc.getNumSmallPools();
 		_numPools = _numBigPools + _numSmallPools;
 	}
 }
