 package final_project.control;
 
 import java.util.*;
 import final_project.model.*;
 import final_project.model.DERound.NoSuchMatchException;
 
 //Created in the main method.
 public class TournamentController implements Constants{
 
 	private Collection<EventController> _events;
 	private IDataStore _dataStore;
 	private int _currentEventID;
 	private StripController _stripController;
 	private SMSController _smsController;
 	private DataFormattingHelper _dataHelper;
 
 	public TournamentController(String username, String password) {
 		_currentEventID = 0;
 		_events = new LinkedList<EventController>();
 		//_dataStore = new DataStore();
 		_dataStore = null; //Setting this temporarily to null because I need it for SMSController/DataFormattingHelper
 		_stripController = new StripController();
 		
 		_dataHelper = new DataFormattingHelper(_dataStore);
 		_smsController = new SMSController(_dataStore, this, username, password); 
 	}
 
 	public void addEvent(String weapon){
 		_events.add(new EventController(++_currentEventID, _dataStore, weapon));
 	}
 
 	public void addEvent(String weapon, Collection<Integer> preregs){
 		_events.add(new EventController(++_currentEventID, _dataStore, weapon, preregs));
 	}
 
 	public void startPoolRound(int eventID, int poolSize) throws IllegalStateException{
 		Iterator<EventController> iter = _events.iterator();
 		if(iter.hasNext()){
 			if(!iter.next().startPoolRound(poolSize)){
 				throw new IllegalStateException("Not correct time to create pool round.");
 			}
 		}
 		throw new IllegalStateException("No event created.");
 	}
 
 	public Collection<PoolSizeInfo> getValidPoolSizes(int eventID){
 		Iterator<EventController> iter = _events.iterator();
 		if(iter.hasNext()){
 			return iter.next().getValidPoolSizes();
 		}
 		throw new IllegalStateException("No event created.");
 	}
 
 	/**
 	 *Adds the given player to the given event.
 	 * @param eventID
 	 * @param playerID
 	 * @return a boolean, true if player was added, false otherwise.
 	 */
 	public boolean addPlayer(int eventID, int playerID){
 		Iterator<EventController> iter = _events.iterator();
 		if(iter.hasNext()){
 			iter.next().addPlayer(playerID);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Takes a completed bout and adds it to appropriate event.
 	 * @param bout A completed bout to be added to results
 	 * @param stripID The number of the strip the bout was fenced on
 	 */
 	public void addCompletedResult(CompleteResult result, int ref) {
 		for(EventController event : _events){
 			if(event.hasRef(ref)){
 				try {
 					event.addCompletedResult(result);
 					_smsController.sendMessage("Result successfully submitted!", _dataStore.getReferee(ref).getPhoneNumber());
 					return;
 				} catch (NoSuchMatchException e) {
 					_smsController.sendMessage("Error: result could not be processed", _dataStore.getReferee(ref).getPhoneNumber());
 					e.printStackTrace();
 				}
 			}
 		}
 		//TODO text ref again --> why? -mk
 	}
 
 	/**
 	 * Method called by the SMSController when two refs need to be
 	 * swapped.
 	 * @param oldRefID
 	 * @param newRefID
 	 */
 	public void swapRef(int eventID, int oldRefID, int newRefID) {
 		//TODO: empty stub
 	}
 	
 	/* METHODS TO MAKE IT POSSIBLE FOR THE GUI TO GET INFORMATION FROM THE DATA STORE */
 	
 	//Method to check in a fencer. Gives
 	public Object[][] checkInFencer(int playerID) {
 		_dataStore.getPlayer(playerID).setCheckedIn(true);
 		return _dataHelper.giveSignInPanelInfo();
 	}
 	
 	/** 
 	 * Maybe confusing, but this method checks in all fencers as either all true, or
 	 * all false, depending on param checkAs
 	 * @param checkAs
 	 * @return
 	 */
 	public Object[][] checkInAll(boolean checkAs) {
 		for (IPlayer i: _dataStore.getPlayers())
 			i.setCheckedIn(checkAs);
 		return _dataHelper.giveSignInPanelInfo();
 	}
 	
 	public Object[][] giveSubscriberTableInfo() {
 		return _dataHelper.giveSubscriberTableInfo();
 	}
 	
 	public Object[][] giveSignInPanelInfo() {
 		return _dataHelper.giveSignInPanelInfo();		
 	}
 	
 	public Object[][] registerSpectator(String number, String firstName, String lastName) {
 		_dataStore.createSpectator(number, firstName, lastName, "", "Spectator");
 		return _dataHelper.giveSubscriberTableInfo();
 	}
 	
 	public Object[][] registerFencer(String number, String firstName, String lastName, int rank, int seed) {
		_dataStore.createPlayer(number, firstName, lastName, "", "Fencer", rank, seed);
 		return _dataHelper.giveSignInPanelInfo();
 	}
 	
 }
