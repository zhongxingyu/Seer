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
 
 	public EventController(int id, IDataStore dataStore, String weapon, StripController stripController){
         this(id,dataStore,weapon,new LinkedList<Integer>(), stripController);
 	}
 
 	public Result[] getDEMatches(){
 		return _deController.getMatches();
 	}
 
 	public EventController(int id, IDataStore dataStore, String weapon, Collection<Integer> preregs, StripController stripController){
 		_state = State.REGISTRATION;
 		_refs = new HashSet<Integer>();
 		_eventID = id;
 		_weapon = weapon;
 		_players = new LinkedList<Integer>(preregs);
 		_stripArrangement = new int[2];
 		_stripController = stripController;
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
 
 	public void addCompletedResult(CompleteResult result) throws DERound.NoSuchMatchException{
 		if(_state.equals(State.POOLS)){
 			_poolController.addCompleteResult(result);
 		}else if(_state.equals(State.DE)){
 			_deController.addCompleteResult(result);
 		}
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
 		_poolController = new PoolRoundController(_dataStore, new LinkedList<Integer>(_players), _stripController);
 		boolean createPoolSuccess = _poolController.createPools(poolSize);
 		if(!createPoolSuccess){
 			_poolController = null;
 		}
 		return createPoolSuccess;
 	}
 
 	public boolean startDERound(double cut){
 		if(_state != State.POOLS)
 			return false;
		//_deController = new DERoundController(_dataStore, )
 		return true;
 	}
 
 	public Collection<PoolSizeInfo> getValidPoolSizes() {
 		Collection<PoolSizeInfo> toReturn = new LinkedList<PoolSizeInfo>();
 		if(_players.size()==0)
 			return toReturn;
 		
 		PoolSizeCalculator poolSizeCalc;
 		for(int i = 4; i < 9; i++){
 			try{
 				poolSizeCalc = new PoolSizeCalculator(_players.size(), i);
 				System.out.println("big pools: " + poolSizeCalc.getNumBigPools() + " small: " + poolSizeCalc.getNumSmallPools());
 				toReturn.add(new PoolSizeInfo(i, poolSizeCalc.getNumBigPools(), poolSizeCalc.getNumSmallPools()));
 
 			}catch(IllegalArgumentException e){
 				System.out.println("big pools: " + 0 + " small: " + 0);
 				toReturn.add(new PoolSizeInfo(i, 0, 0));
 			}
 		}
 		return toReturn;
 	}
 
 	public void convertPlayersListToSortedSeeding(){
 		Collections.sort(new LinkedList<Integer>(_players),
 				new Comparator<Integer>(){
 			@Override
 			public int compare(Integer arg0, Integer arg1) {
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
 }
 
