 package final_project.control;
 
 import java.util.Iterator;
 import final_project.model.*;
 import final_project.model.store.*;
 
 /**
  * Class to help the TournamentController format the
  * data from the data store in a specific way, so that
  * the GUI can display it nicely.
  *
  * @author mksteele
  */
 public class DataFormattingHelper implements Constants {
 
 	private IDataStore _dataStore;
 	private TournamentController _tournament;
 
 	public DataFormattingHelper(IDataStore s, TournamentController t) {
 		_dataStore = s;
 		_tournament = t;
 	}
 
 	/**
 	 * Formats data for the SignInPanel. Has the form
 	 * {"First Last", "Club", "Group", signed in y/n, "ID" }
 	 * @return
 	 */
 	public Object[][] giveSignInPanelInfo() {
 		int numPeople = _dataStore.getPeople().size() - _dataStore.getPeopleForGroup("Spectator").size();
 		Object[][] toReturn = new Object[numPeople][NUM_COLS_SIGN_IN];
 
 		//Making one blank row so that the GUI does not break on empty input
 		for(int i=0; i < NUM_COLS_SIGN_IN; i++)
 			toReturn[0][i] = "";
 
 		int index = 0;
 		for (IPerson i: _dataStore.getPeople()) {
 			if(!i.getGroup().equals("Spectator")){
 				/* NAME */
 				toReturn[index][0] = i.getFirstName() + " " + i.getLastName();
 
 				/* CLUB */
 				if(i instanceof IPlayer) {
 					Iterator<Integer> iter = ((IPlayer) i).getClubs().iterator();
 					if(iter.hasNext()) //Such a mess, just to get out the club name...
 						toReturn[index][1] =  _dataStore.getClub(iter.next()).getName();
 					else
 						toReturn[index][1] = "";
 				}
 				else if(i instanceof IReferee) {
 					Iterator<Integer> iter = ((IReferee) i).getClubs().iterator();
 					if(iter.hasNext()) //Such a mess, just to get out the club name...
 						toReturn[index][1] =  _dataStore.getClub(iter.next()).getName();
 					else
 						toReturn[index][1] = "";
 				}
 				else
 					toReturn[index][1] = "";
 
 				/* GROUP */
 				toReturn[index][2] = i.getGroup();
 
 				/* SIGNED IN (if player) */
 				if(i instanceof IPlayer) {
 					toReturn[index][3] = ((IPlayer)i).getCheckedIn();
 				}
 				else
 					toReturn[index][3] = true;
 
 				/* ID */
 				toReturn[index][4] = i.getID();
 
 				index++;
 			}
 		}
 		return toReturn;
 	}
 
 	/**
 	 *
 	 *
 	 * {"First Last", "Number", "Following"},
 	 */
 	public Object[][] giveSubscriberTableInfo() {
 		//Making the object array with as many rows as spectators in the data store
 		Object[][] toReturn = null;
		if(_dataStore.getPeopleForGroup("Spectator").size()!=0)
			toReturn = new Object[_dataStore.getPeopleForGroup("Spectator").size()][NUM_COLS_SUBSCRIBER_PANEL];
		else {
			toReturn = new Object[1][NUM_COLS_SUBSCRIBER_PANEL];
			return toReturn;
		}

		//TODO WHY DOES THE GUI BREAK ON EMPTY INPUT????
		//Making one blank row so that the GUI does not break on empty input
		for(int i=0; i < NUM_COLS_SUBSCRIBER_PANEL; i++)
			toReturn[0][i] = "";
 
 		int index = 0;
 		for(IPerson i: _dataStore.getPeopleForGroup("Spectator")) {
 			toReturn[index][0] = i.getFirstName() + " " + i.getLastName();
 			toReturn[index][1] = i.getPhoneNumber();
 
 			toReturn[index][2] = "";
 			while(i.getWatched().iterator().hasNext()) {
 				int id = ((Integer)(i.getWatched().iterator().next())).intValue();
 				System.out.println("ID in data formatting helper subscribe panel" + id); //TODO println
 				IObservable followed = _dataStore.getObservable(id);
 				if(followed instanceof IClub) 
 					toReturn[index][2] =  ((IClub) followed).getName() + "";
 			}
 
 			index++;
 		}
 
 		return toReturn;
 	}
 
 	public Object[][] getPoolSizeInfoTable(int stripRows, int stripCols) {
 		Object[][] toReturn = new Object[NUM_POOL_SIZES_POSSIBLE][NUM_COLS_POOL_SETUP];
 
 		Iterator<PoolSizeInfo> iter = _tournament.getValidPoolSizes().iterator();
 		int i = 0;
 		while(iter.hasNext()){
 			toReturn[i][0] = i+4; //Pool sizes go from 4-8
 			PoolSizeInfo info = iter.next();
 			toReturn[i][1] = info._numBigPools;
 			toReturn[i][2] = info._numSmallPools;
 			toReturn[i][3] = "Select";
 			i++;
 		}
 
 		return toReturn;
 	}
 
 	public Object[][] getPoolRefListTable(Pool pool) {
 		Object[][] toReturn = new Object[pool.getPlayers().size()][NUM_COLS_POOL_REF_LIST];
 		
 		int i = 0;
 		for(int p: pool.getPlayers()) {
 			//Not checking for data store null ptrs but whatever.
 			toReturn[i][0] = _dataStore.getPerson(p).getFirstName() + " " + _dataStore.getPerson(p).getLastName();
 			
 			toReturn[i][1] = "";
 			Iterator<Integer> clubs = _dataStore.getPlayer(p).getClubs().iterator();
 			if(clubs.hasNext()) {
 				toReturn[i][1] = _dataStore.getClub(clubs.next()).getName();
 			}
 			
 			toReturn[i][2] = i+1;
 			
 			i++;
 		}
 		
 		return toReturn;
 	}
 
 }
 
