 package final_project.control;
 
 import java.util.*;
 
 import final_project.model.*;
 import final_project.model.store.*;
 
 public class EventController {
 
 	private Collection<Integer> _refs;
 	private State _state;
 	private int[] _stripArrangement;
 	private DERoundController _deController;
 	private PoolRoundController _poolController;
 	private IDataStore _dataStore;
 	private String _weapon;
 	private List<Integer> _players;
 	private int _eventID;
 	private StripController _stripController;
 	private SMSController _smsController;
 	private TournamentController _tournamentController;
 
 	public EventController(int id, IDataStore dataStore, String weapon, StripController stripController, SMSController smsController, TournamentController tc){
         this(id,dataStore,weapon,new LinkedList<Integer>(), stripController, smsController, tc);
 	}
 
 	public void clearPlayers() {
 		_players.clear();
 	}
 
 	public Result[] getDEMatches(){
 		return _deController.getMatches();
 	}
 
 	public EventController(int id, IDataStore dataStore, String weapon, Collection<Integer> preregs, StripController stripController, SMSController smsController, TournamentController tc){
 		_tournamentController = tc;
 		_state = State.REGISTRATION;
 		_refs = new HashSet<Integer>();
 		_eventID = id;
 		_weapon = weapon;
 		_players = new LinkedList<Integer>(preregs);
 		_stripArrangement = new int[2];
 		_stripController = stripController;
         _dataStore = dataStore;
         _smsController = smsController;
 	}
 
 	public void addPlayer(int id){
 		_players.add(id);
 	}
 
 	public void addRef(Integer refID){
 		_refs.add(refID);
 	}
 
 	// Represents the phase of the tournament that the TournamentControl is ready to carry out
 	public enum State {
 		REGISTRATION, POOLS, DE, FINAL
 	}
 
 	public boolean hasRef(Integer ref){
 		return _refs.contains(ref);
 	}
 
 	public boolean addCompletedResult(CompleteResult result) throws DERound.NoSuchMatchException{
 		if(_state.equals(State.POOLS)){
 			System.out.println("calling add completed result on pool controller");
 			return _poolController.addCompleteResult(result);
 		} else if(_state.equals(State.DE)){
 			 _deController.addCompleteResult(result);
 		}
         return false;
 	}
 
 	public boolean rescoreLastMatch(int ref, CompleteResult result) {
 		if(_state.equals(State.POOLS))
 			return _poolController.rescoreLastMatch(ref, result);
 		return false;
 	}
 	
 	public void setStripArrangement(int rows, int cols) {
 		_stripArrangement[0] = rows;
 		_stripArrangement[1] = cols;
 	}
 
 	public int[] getStripArrangement() {
 		return _stripArrangement;
 	}
 
 	/**
 	 * Called in TournamentController;
 	 * returns true if the pool round was started, false otherwise.
 	 * @return
 	 */
 	public boolean startPoolRound(int poolSize) {
 		if(_state != State.REGISTRATION)
 			return false;
 		_poolController = new PoolRoundController(_dataStore, new LinkedList<Integer>(_players), _stripController, _smsController, poolSize);
 		boolean createPoolSuccess = _poolController.createPools(poolSize);
 		if(!createPoolSuccess){
 			_poolController = null;
 		}else{
 			_state = State.POOLS;
 			//_poolController.notifyNewPools();
 		}
 		return createPoolSuccess;
 	}
 
 	public void notifyPools(){
 		_poolController.notifyNewPools();
 	}
 
 	public boolean startDERound(double cut){
 		if(_state != State.POOLS)
 			return false;
 		List<Integer> seeds = _poolController.getResults();
 		_deController = new DERoundController(_dataStore, _stripController, seeds, cut, _tournamentController);
 		_state = State.DE;
 		return true;
 	}
 
 	public Collection<PoolSizeInfo> getValidPoolSizes() {
 		Collection<PoolSizeInfo> toReturn = new LinkedList<PoolSizeInfo>();
         boolean refFound = false;
         for (IReferee ref : _dataStore.getReferees())
             if (!ref.getReffing())
                 refFound = true;
 		if(_players.size()==0 || !refFound)
 			return toReturn;
 
 		PoolSizeCalculator poolSizeCalc;
 		for(int i = 4; i < 9; i++){
 			try{
 				poolSizeCalc = new PoolSizeCalculator(_players.size(), i);
 				toReturn.add(new PoolSizeInfo(i, poolSizeCalc.getNumBigPools(), poolSizeCalc.getNumSmallPools()));
 
 			}catch(Exception e){
 			}
 		}
 		return toReturn;
 	}
 
 	public void convertPlayersListToSortedSeeding(){
 		Collections.sort(new LinkedList<Integer>(_players),
                          new Comparator<Integer>(){
                              @Override public int compare(Integer arg0, Integer arg1) {
                                  _dataStore.getData(3);
                                  return 0;
                              }
                          });
 	}
 
 	public List<Pool> getPools() {
 		if(_poolController == null)
 			return new LinkedList<Pool>(); //TODO tossing empty list
 		return _poolController.getPools();
 	}
 
     public State getState() {
         return _state;
     }
 
     public void addDEObserver(DERoundObserver obs) {
         _deController.addDEObserver(obs);
     }
 
 	public void changeRefs(Collection<Integer> refs) {
 		if(_state.equals(State.POOLS)){
 			_poolController.changeRefs(refs);
 		}
 	}
 }
 
