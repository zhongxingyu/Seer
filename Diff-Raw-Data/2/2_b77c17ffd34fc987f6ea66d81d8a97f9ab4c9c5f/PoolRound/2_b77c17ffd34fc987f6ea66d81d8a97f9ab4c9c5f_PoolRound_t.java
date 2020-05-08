 package final_project.model;
 
 import java.util.*;
 
 import final_project.control.StripController;
 
 //also serves as pool controller
 public abstract class PoolRound implements IRound{
 	private IDataStore _dataStore;
 	protected List<Pool> _pools;
 	protected List<Integer> _resultSeedList;
 	protected List<Integer> _initialSeeding;
 	protected int _poolSize;
 	protected int _numPlayers;
 	private StripController _stripControl;
 
 	/*
 	 * Adds the given completeResult to the appropriate pool.
 	 * @param result A CompleteResult to be added to this PoolRound
 	 * @return a boolean, true if all matches in the PoolRound have been completed, false otherwise
 	 */
 	public boolean addCompleteResult(CompleteResult result) throws IllegalArgumentException{
 		for(Pool p : _pools){
 			if(poolHasResult(p, result)){
 				if(p.addCompletedResult(result)){
 					//code to reassign newly free ref(s) and strip(s) to pools that don't have them.
 					Iterator<Integer> refIter = p.getRefs().iterator();
 					Iterator<Integer> stripIter = p.getStrips().iterator();
 					for(Pool toCheck : _pools){
 						if(!refIter.hasNext() && !stripIter.hasNext())
 							break;
 						if(!toCheck.isDone()) {
 							boolean hasNoRef, hasNoStrip, extraRef, extraStrip, newPoolReady;
 							newPoolReady = false;
 							hasNoRef = toCheck.getRefs().isEmpty();
 							hasNoStrip = toCheck.getStrips().isEmpty();
 							extraRef = refIter.hasNext();
 							extraStrip = stripIter.hasNext();
 							if(hasNoRef && hasNoStrip &&
 							   extraRef && extraStrip){
 								toCheck.addRef(refIter.next());
 								refIter.remove();
 								toCheck.addStrip(stripIter.next());
 								stripIter.remove();
 								newPoolReady = true;
 							}
 							if(hasNoRef && !hasNoStrip &&
 							   extraRef) {
 								toCheck.addRef(refIter.next());
 								refIter.remove();
 								newPoolReady = true;
 							}
 							if(!hasNoRef && hasNoStrip &&
 									        extraStrip) {
 								toCheck.addStrip(stripIter.next());
 								stripIter.remove();
 								newPoolReady = true;
 							}
 							if(newPoolReady) {
 								//TODO: Notify newly ready pool(ref and fencers) that there pool has now begun
 							}
 						}
 
 					}
 					while(refIter.hasNext()){
 						final Integer temp = refIter.next();
 						_dataStore.runTransaction(new Runnable(){
 							public void run(){
 								_dataStore.putData(_dataStore.getReferee(temp).setReffing(false));
 							}
 						});
 					}
 					p.clearRefs();
 				}
 				//returns true if all pools have completed, false otherwise
 				for(Pool tempPool : _pools){
 					if(!tempPool.isDone())
 						return false;
 				}
 				return true;
 			}
 		}
 		throw new IllegalArgumentException("No pools have given result.");
 	}
 
 	//return true if the given pool p has the given CompleteResult as one of its matches
 	private boolean poolHasResult(Pool p, CompleteResult result){
 		return  p.getPlayers().contains(result.getLoser()) && p.getPlayers().contains(result.getWinner());
 	}
 
 	/**
 	 * Assigns referees from the list of referee ID argument to the rounds pools; if there
 	 * are not enough referees, the remaining pools will have empty referees lists and will
 	 * be notified that they have been flighted.
 	 * @param refs A list of Integer representing a list of referees available for the PoolRound's pools.
 	 * @return true if there are no potential conflicts of interest, false if there are.
 	 */
 	public boolean assignReferees(List<Integer> refs){
 		boolean toReturn = true;
 		Map<Pool, Integer> poolToNumConflicts = new HashMap<Pool,Integer>();
 		for(Pool p : _pools){
 			poolToNumConflicts.put(p, 0);
 		}
 		for(Pool pool : _pools){
 			for(Integer ref : refs){
 				if(haveConflict(pool, ref))
 					poolToNumConflicts.put(pool, poolToNumConflicts.get(pool) + 1);
 			}
 		}
 
 		//Standard Insertion Sort
         for(int i = 1; i < _pools.size(); i++){
             Pool tmp = _pools.get(i);
             int k = i;
             //TODO: check if this comparison is correct (might be > instead??)
             while((k>0) && poolToNumConflicts.get(_pools.get(k - 1)) <  poolToNumConflicts.get(tmp)){
                 _pools.set(k, _pools.get(k-1));
                 k--;
             }
             _pools.set(k, tmp);
         }
 
         Iterator<Integer> iter;
         for(Pool p : _pools){
         	//TODO: notify gui and fencers that pool has been flighted
         	if(refs.isEmpty()){
         		p.clearRefs();
         	}else{
 
         	int temp = -1;
         	iter = refs.iterator();
         	while(iter.hasNext()){
         		temp = iter.next();
         		if(!haveConflict(p, temp)){
         			p.addRef(temp);
         			iter.remove();
         			break;
         		}
         	}
         	if(temp == -1){
         		//this should never be -1, (notice that if refs is empty we break, so temp must be initialized to something other than -1)
         		throw new IllegalStateException("Structural error.  Must terminate.");//TODO: make this error message better
         	}
         	p.addRef(temp);
         	iter.remove();
         	toReturn = false;
         	}
         }
         return toReturn;
 	}
 
 	/**
 	 * Assigns a strip(if available) to each pool that has a referee.  If there are no more available strips,
 	 * the remaining pools that have not been assigned strips are notified that they have been flighted.
 	 */
 	public void assignStrips() {
 		for(Pool p : _pools) {
 			if(!p.getRefs().isEmpty()){
 				if(_stripControl.availableStrip())
 					p.addStrip(_stripControl.checkOutStrip());
 				else {
 					//TODO: Send message to all the rest of the pools that they have been flighted
 					return;
 				}
 			}
 		}
 	}
 
 	private boolean haveConflict(Pool p, Integer ref){
 		Collection<Integer> col1 = new HashSet<Integer>();
 		Collection<Integer> col2 = _dataStore.getReferee(ref).getClubs();
 
 		for(Integer player : p.getPlayers()){
 			col1.addAll(_dataStore.getPlayer(player).getClubs());
 		}
 
 		Set<Integer> intersection = new HashSet<Integer>();
 		for(Integer i : col1){
 			if(col2.contains(i))
 				intersection.add(i);
 		}
 		return (intersection.size() != 0);
 	}
 
 	public int getNumPlayers(){
 		return _numPlayers;
 	}
 
 	public void setNumPlayers(int numPlayers){
 		_numPlayers = numPlayers;
 	}
 
 	public int getPoolSize(){
 		return _poolSize;
 	}
 
 	public void setPoolSize(int newSize){
 		_poolSize = newSize;
 	}
 
 	public List<Integer> getResults(){
 		if (_resultSeedList == null)
 			seedFromResults();
 		return _resultSeedList;
 	}
 
 	public void seedFromResults() {
 		_resultSeedList = new LinkedList<Integer>();
 		List<PlayerSeed> playerSeeds = new LinkedList<PlayerSeed>();
 		for (Pool pool : _pools)
 			playerSeeds.addAll(pool.getSeeds());
 		Collections.sort(playerSeeds);
 		for (PlayerSeed playerSeed : playerSeeds)
 			_resultSeedList.add(playerSeed.getPlayer());
 	}
 
 	public void populatePools() {
 		for (int i = 0; i < _numPlayers; ++i)
			_pools.get(i % _pools.size()).addPlayer(_initialSeeding.get(i));
 	}
 }
