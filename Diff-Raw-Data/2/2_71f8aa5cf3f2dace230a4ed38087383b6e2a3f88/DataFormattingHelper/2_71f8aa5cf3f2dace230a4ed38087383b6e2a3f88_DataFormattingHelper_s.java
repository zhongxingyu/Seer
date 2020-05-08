 package final_project.control;
 
 import java.util.Iterator;
 import final_project.model.*;
 
 /**
  * Class to help the TournamentController format the
  * data from the data store in a specific way, so that
  * the GUI can display it nicely.
  *
  * @author mksteele
  */
 public class DataFormattingHelper implements Constants {
 
 	private IDataStore _dataStore;
 
 	public DataFormattingHelper(IDataStore s) {
 		_dataStore = s;
 	}
 
 	/**
 	 * Formats data for the SignInPanel. Has the form
 	 * {"First Last", "Club", "Group", signed in y/n, "ID" }
 	 * @return
 	 */
 	public Object[][] giveSignInPanelInfo() {
 		int numPeople = _dataStore.getPeople().size() - _dataStore.getPeopleForGroup("Spectator").size();
 		Object[][] toReturn = new Object[numPeople][NUM_COLS_SIGN_IN];
 		System.out.println("Sign in table size: " + numPeople);
 		System.out.println("People size: " + _dataStore.getPeople().size());
 		
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
 					//TODO test fix code
 					else{
 						toReturn[index][1] = "";
 					}
 				}
 				else if(i instanceof IReferee) {
 					Iterator<Integer> iter = ((IReferee) i).getClubs().iterator();
 					if(iter.hasNext()) //Such a mess, just to get out the club name...
 						toReturn[index][1] =  _dataStore.getClub(iter.next()).getName();
 					//TODO test fix code
 					else{
 						toReturn[index][1] = "";
 					}
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
 					toReturn[index][3] = null;
 
 				/* ID */
 				toReturn[index][4] = i.getID();
 				
 				index++;
 			}
 		}
 		
 		System.out.println(toReturn); //TODO println
 		return toReturn;
 	}
 
 	/**
 	 *
 	 *
 	 * {"First Last", "Number", "Following"},
 	 */
 	public Object[][] giveSubscriberTableInfo() {
 		//Making the object array with as many rows as spectators in the data store
 		Object[][] toReturn = new Object[_dataStore.getPeopleForGroup("Spectator").size()][NUM_COLS_SUBSCRIBER_PANEL];
 		System.out.println("Subscriber size: " + _dataStore.getPeopleForGroup("Spectator").size());
 		
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
 				if(followed instanceof IClub) {
 					toReturn[index][2] =  ((IClub) followed).getName() + "";
 				}
 			}
 			index++;
 		}
 
 		return toReturn;
 	}
 
 	public Object[][] getPoolSizeInfoTable(int stripRows, int stripCols) {
 		
 		Object[][] toReturn = new Object[NUM_POOL_SIZES_POSSIBLE][NUM_COLS_POOL_SETUP];
 		
 		// TODO Auto-generated method stub
 		/* 			new Object[][] {
 				{"4", null, null, "Select"},
 				{"5", null, null, "Select"},
 				{"6", null, null, "Select"},
 				{"7", null, null, "Select"},
 				{"8", null, null, "Select"},
 			} */
 		                           
 		for(int i=0; i<NUM_POOL_SIZES_POSSIBLE; i++) {
 			toReturn[i][0] = i+4; //Pool sizes go from 4-8
 			//toReturn[i][1] = tournament.getBigPools();
 		}
 		                                 
		return null;
 	}
 
 }
 
