 package final_project.model;
 
 import java.util.*;
 
 import final_project.model.store.*;
 import final_project.control.SMSController;
 import final_project.control.StripController;
 
 //also serves as pool controller
 public abstract class PoolRound implements IRound{
 	protected IDataStore _dataStore;
 	protected List<Pool> _pools;
 	protected List<Integer> _resultSeedList;
 	protected List<Integer> _initialSeeding;
 	protected int _poolSize;
 	protected int _numPlayers;
 	protected StripController _stripControl;
 	protected SMSController _smsController;
 
 
 	/*
 	 * Adds the given completeResult to the appropriate pool.
 	 * @param result A CompleteResult to be added to this PoolRound
 	 * @return a boolean, true if all matches in the PoolRound have been completed, false otherwise
 	 */
 	public boolean addCompleteResult(CompleteResult result) throws IllegalArgumentException{
 		for(Pool p : _pools){
 			if(poolHasResult(p, result)){
 				// Check to ensure that the scores in the result are within the valid range.
 				if(result.getWinnerScore() > 5 ||  result.getWinnerScore() < 0  ||
 	               result.getLoserScore() > 5  ||  result.getLoserScore() < 0) {
 	            	   _smsController.sendMessage("The last result you entered did not have valid point values. " +
    							   "Please retry.",
    							   _dataStore.getReferee(p.getRefs().iterator().next()).getPhoneNumber());
 	            	   return false;
 	               }
 				if(p.addCompletedResult(result)){  // If pool is now over
 					// Notify the referee(s) that their pool is now completed
 					String refPhone;
 					for(Integer ref : p.getRefs()) {
 						refPhone = _dataStore.getPerson(ref).getPhoneNumber();
 						_smsController.sendMessage("Your pool is now over.", refPhone);
 					}
 					for(Integer athlete : p.getPlayers()) {
 						_smsController.sendSubscriberMessage(_dataStore.getPlayer(athlete).getFirstName() + 
 								_dataStore.getPlayer(athlete).getLastName() +
 								" has finished his/her pool", athlete);
 					}
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
 								Iterator<Integer> s = toCheck.getStrips().iterator();
 								Iterator<Integer> r = toCheck.getRefs().iterator();
 								String stripNum = s.next().toString();
 								_smsController.sendCollectionMessage("Your pool is now ready to start on strip: " + stripNum, toCheck.getPlayers());
 								refPhone = _dataStore.getPerson(r.next()).getPhoneNumber();
 								_smsController.sendMessage("Your pool is ready to start on strip: " + stripNum, refPhone);
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
 					//returns true if all pools have completed, false otherwise
 					for(Pool tempPool : _pools){
 						if(!tempPool.isDone())
 							return false;
 					}
 					return true;
 				}
 				else { // If pool is not done
 					// Notify the referee(s) about their next match
 					IncompleteResult nextMatch;
 					String refPhone, name1, name2;
 					for(Integer ref : p.getRefs()) {
 						nextMatch = p.getNextResult();
 						refPhone = _dataStore.getPerson(ref).getPhoneNumber();
 						if(nextMatch != null) {
 							name1 = _dataStore.getPlayer(nextMatch.getPlayer1()).getFirstName() + " " +
                                 _dataStore.getPlayer(nextMatch.getPlayer1()).getLastName() + " (" +
                                 nextMatch.getPlayer1() + ")";
 							name2 = _dataStore.getPlayer(nextMatch.getPlayer2()).getFirstName() + " " +
                                 _dataStore.getPlayer(nextMatch.getPlayer2()).getLastName() + " (" +
                                 nextMatch.getPlayer2() + ")";
                             System.out.println("----------Next match: "+name1+", "+name2+", by ref "+ref);
 							_smsController.sendMessage("Your next match is between: " + name1 + " and " + name2,
                                                        refPhone);
 						}							
 					}
 					// Notify subscribers of the competitors that are on deck of that fact
 					IncompleteResult onDeck = p.getOnDeckResult();
 					if (onDeck!=null) {
 						name1 = _dataStore.getPlayer(onDeck.getPlayer1()).getFirstName() + " " +
 						_dataStore.getPlayer(onDeck.getPlayer1()).getLastName();
 						_smsController.sendSubscriberMessage(name1 + " is now on deck on strip: " + p.getStrips().iterator().next(),
 								onDeck.getPlayer1());
 						name2 = _dataStore.getPlayer(onDeck.getPlayer2()).getFirstName() + " " +
 						_dataStore.getPlayer(onDeck.getPlayer2()).getLastName();
 						_smsController.sendSubscriberMessage(name2 + " is now on deck on strip: " + p.getStrips().iterator().next(),
 								onDeck.getPlayer2());
 					}
 					// If the pool is being double stripped, two bouts will be "on deck," so more subscribers must be notified
 					if(p.getRefs().size() > 1){
 						onDeck = p.getInHoleBout();
 						name1 = _dataStore.getPlayer(onDeck.getPlayer1()).getFirstName() + " " +
 						_dataStore.getPlayer(onDeck.getPlayer1()).getLastName();
 						_smsController.sendSubscriberMessage(name1 + " is now on deck on strip: " + p.getStrips().iterator().next(),
 								onDeck.getPlayer1());
 						name2 = _dataStore.getPlayer(onDeck.getPlayer2()).getFirstName() + " " +
 						_dataStore.getPlayer(onDeck.getPlayer2()).getLastName();
 						_smsController.sendSubscriberMessage(name2 + " is now on deck on strip: " + p.getStrips().iterator().next(),
 								onDeck.getPlayer2());
 					}
 					return false;
 				}
 			}
 		}
 		throw new IllegalArgumentException("No pools have given result.");
 	}
 
 	//return true if the given pool p has the given CompleteResult as one of its matches
 	private boolean poolHasResult(Pool p, CompleteResult result){
 		return  p.getPlayers().contains(result.getLoser()) && p.getPlayers().contains(result.getWinner());
 	}
 
 	public void createAllIncompleteResult(){
 		for(Pool p : _pools){
 			((FencerPool) p).createIncompleteResults();
 		}
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
 		for(Pool pool : _pools) {
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
 			if(refs.isEmpty()){
 				p.clearRefs();
 			}else{
 
 				int temp = -1;
 				iter = refs.iterator();
 				while(iter.hasNext()){
 					temp = iter.next();
 					if(!haveConflict(p, temp)){
 						break;
 					}
 				}
 				if(temp == -1){
 					//this should never be -1, (notice that if refs is empty we break, so temp must be initialized to something other than -1)
 					throw new IllegalStateException("Structural error.  Must terminate.");//TODO: make this error message better
 				}
 				p.addRef(temp);
 				iter.remove();
 				toReturn = haveConflict(p, temp);
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
 				else
 					return;
 			}
 		}
 	}
 
 	/**
 	 * Iterates through all pools in round and all that either do not have a referee or strip are notified
 	 * that they have been flighted and must wait to compete.
 	 */
 	public void notifyPools() {
 		for(Pool p : _pools) {
 			if(p.getRefs() == null  ||  p.getStrips()  == null ||
 					p.getRefs().isEmpty()  ||   p.getStrips().isEmpty()){
 				_smsController.sendCollectionMessage("Your pool has been flighted.", p.getPlayers());
 				//p.clearRefs(); //EDIT: taking this out
 				//p.clearStrips();
 			}
 			else {
 				Iterator<Integer> s = p.getStrips().iterator();
 				String stripNum = s.next().toString();
 				_smsController.sendCollectionMessage("Your pool will start momentarily on strip: " + stripNum, p.getPlayers());
 				String name;
 				// Notify followers of the competitor that they are about to start their pool.
 				for(Integer f : p.getPlayers()) {
 					name = _dataStore.getPlayer(f).getFirstName() + " " + _dataStore.getPlayer(f).getLastName();
 					_smsController.sendSubscriberMessage(name + " is about to start his/her pool on strip: " + p.getStrips().iterator().next(), f);
 				}
 				String refPhone, name1, name2;
 				IncompleteResult firstMatch;
 				for(Integer ref : p.getRefs()) {
 					refPhone = _dataStore.getPerson(ref).getPhoneNumber();
 					_smsController.sendMessage("Your pool is ready to start on strip: " + stripNum, refPhone);
 					firstMatch = p.getNextResult();
 					name1 = _dataStore.getPlayer(firstMatch.getPlayer1()).getFirstName() + " " +
 					_dataStore.getPlayer(firstMatch.getPlayer1()).getLastName() + " (" +
 					firstMatch.getPlayer1() + ")";
 					name2 = _dataStore.getPlayer(firstMatch.getPlayer2()).getFirstName() + " " +
 					_dataStore.getPlayer(firstMatch.getPlayer2()).getLastName() + " (" +
 					firstMatch.getPlayer2() + ")";
                    System.out.println("----------Next match: "+name1+", "+name2+", by ref "+ref);
 					_smsController.sendMessage("Your first match is between: " + name1 + " and " + name2,
 							refPhone);
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
 
 	public boolean rescoreLastMatch(int referee, CompleteResult newScore) {
 		for(Pool p : _pools) {
 			if(p.getRefs().contains(referee)) {
 				if (p.rescoreLastMatch(newScore)) {
 					_smsController.sendMessage("The score of your last match was successfully changed." +
 							"  The most recent next bout message you recieved s still accurate.",
 							_dataStore.getReferee(referee).getPhoneNumber());
 					return true;
 				}
 			}
 		}
 
 		_smsController.sendMessage("The players of the new score you sent in do not match those of your " +
 				"most recent score submission. Please retry.",
 				_dataStore.getReferee(referee).getPhoneNumber());
 		return false; 		
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
 		for (int i = 0; i < _initialSeeding.size(); ++i){
 			_pools.get(i % _pools.size()).addPlayer(_initialSeeding.get(i));
 		}
 
 		for (Pool p: _pools) {
 			p.shufflePlayers();
 		}
 	}
 
 	public List<Pool> getPools() {
 		return _pools;
 	}
 }
