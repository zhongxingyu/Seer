 package final_project.control;
 
 import java.util.Scanner;
 import java.util.Calendar;
 import final_project.model.store.*;
 import final_project.view.PoolObserverPanel;
 
 //import mocks.*;
 
 public class SMSParser {
 
 	private IDataStore _store;
 	private SMSController _control;
 	private Calendar _cal;
 
 	public SMSParser(IDataStore s, SMSController ctrl) {
 		_store = s;
 		_control = ctrl;
 		_cal = Calendar.getInstance();
 	}
 
 	/**
 	 * This method handles parsing the message from the API &
 	 * then calling the appropriate other methods on the data that
 	 * it gets out of the line.
 	 */
 	public void parseOutput(String received, String number) {
 		Scanner s = new Scanner(received);
 
 		//First, getting the first word
 		String firstWord = s.next();
 		System.out.println("First word: " + firstWord);
 		
 		//Find sender id
 		int id = -1;
 		for(IPerson p: _store.getPeople()) {
 			if(p.getPhoneNumber().equals(number)) {
 				System.out.println("Number match found!");
 				id = p.getID();
 			}
 		}
 		
 		//Message: "help string"
 		if (firstWord.equalsIgnoreCase("help")) {
 			if(id == -1)
 				return;
 			/* Alerting the proper group for help (either technical or medical) */
 			if(s.hasNext()) {
 				String groupToAlert = s.next();
 				if(groupToAlert.equalsIgnoreCase("medical")) {
 					_control.alertGUI(PoolObserverPanel.Status.MEDICAL, received, _cal.getTime());
 					_control.sendGroupMessage("Medical", received);
 					_control.setGUIStatusLabel(PoolObserverPanel.Status.MEDICAL, id);
 				}
 				else if(groupToAlert.equalsIgnoreCase("technical")) {
 					_control.alertGUI(PoolObserverPanel.Status.TECHNICAL, received, _cal.getTime());
 					_control.sendGroupMessage("Technical", received);
 					_control.setGUIStatusLabel(PoolObserverPanel.Status.TECHNICAL, id);
 				}
 				else if (groupToAlert.equalsIgnoreCase("fixed")) {
 					_control.alertGUI(PoolObserverPanel.Status.TECHNICAL, received, _cal.getTime());
 					_control.setGUIStatusLabel(PoolObserverPanel.Status.FENCING, id);
 				}
 				else {
 					_control.alertGUI(null, received, _cal.getTime());
 				}
 			}
 		}
 		
 		// Message: "Fixed strip 5"
 		else if(firstWord.equalsIgnoreCase("fixed")) {
 			_control.alertGUI(PoolObserverPanel.Status.TECHNICAL, received, _cal.getTime());
 			_control.setGUIStatusLabel(PoolObserverPanel.Status.FENCING, id);
 		}
 		
 		// Message: "Ready on strip 3"
 		else if(firstWord.equalsIgnoreCase("ready")) {
 			_control.alertGUI(PoolObserverPanel.Status.MEDICAL, received, _cal.getTime());
 			_control.setGUIStatusLabel(PoolObserverPanel.Status.FENCING, id);
 		}
 
 		// Message: "Follow first last" or "follow clubName"
 		else if(firstWord.equalsIgnoreCase("follow")) {
 			String firstName = "", lastName = "";
 			if(s.hasNext())
 				firstName = s.next();
 			if(s.hasNext())
 				lastName = s.next();
 
 			this.subscribeUserToObservable(firstName, lastName, number);
 		}
 
 		// Message: "unsubscribe first last" or "Unsubscribe clubName"
 		else if(firstWord.equalsIgnoreCase("unsubscribe")) {
 			String firstName = "", lastName = "";
 			if(s.hasNext())
 				firstName = s.next();
 			if(s.hasNext())
 				lastName = s.next();
 
 			this.unsubscribeUserToObservable(firstName, lastName, number);
 		}
 
 		//Message: "refswap number"
 		else if(firstWord.equalsIgnoreCase("Refswap")) {
 			String newRefNumber = "";
 			if(s.hasNext())
 				newRefNumber = s.next();
 
 			this.swapRefs(number, newRefNumber);
 		}
 
 		//Message "result id beat id this-that" or "id beat id this to that"
 		else if(firstWord.equalsIgnoreCase("result") || firstWord.equalsIgnoreCase("rescore")) {
 			int refID =0, winID = 0, loseID = 0, winScore = 0, loseScore = 0;
 
 			/* Looping through to find the ref ID of this number */
 			boolean found = false;
 			for (IReferee i: _store.getReferees()) {
 				if(i.getPhoneNumber().equals(number)) {
 					refID = i.getID();
 					found = true;
 					break;
 				}
 			}
 			if(!found) {
 				_control.sendMessage("We're sorry, this number is not registered as a referee.", number);
 				return;
 			}
 
 			/* Now, parsing out fencer IDs and score */
 			if(s.hasNextInt())
 				winID = s.nextInt();
 
 			if(s.hasNext() && !s.next().equalsIgnoreCase("beat")) { //Eating "beat" token
 				_control.sendMessage("We're sorry, this message could not be parsed.", number);
 				return;
 			}
 
 			if(s.hasNextInt())
 				loseID = s.nextInt();
 			else {
 				_control.sendMessage("We're sorry, this message could not be parsed.", number);
 				return;
 			}
 
 			if(s.hasNextInt())
 				winScore = s.nextInt();
 			else {
 				_control.sendMessage("We're sorry, this message could not be parsed.", number);
 				return;
 			}
 
 			if(s.hasNext()) {
 				String next = s.next();
 				if (!next.equalsIgnoreCase("to") && !next.equalsIgnoreCase("-")){ //Eating "to"/"-" token
 					_control.sendMessage("We're sorry, this message could not be parsed.", number);
 					return;
 				}
 			}
 
 			if(s.hasNextInt())
 				loseScore = s.nextInt();
 			else {
 				_control.sendMessage("We're sorry, this message could not be parsed.", number);
 				return;
 			}
 
 			/* NOW THAT WE'VE PARSED OUT ALL OF THE DATA */
 			if(firstWord.toLowerCase().equalsIgnoreCase("rescore")) { //changing the mistaken results
 				_control.rescoreLastMatch(refID, winID, winScore, loseID, loseScore);
 			}
 			else {
 				try{
 					_control.returnResults(refID, winID, winScore, loseID, loseScore);
 				}catch (IllegalArgumentException e){
 					_control.sendMessage("Result not for current bout. Check fencer id's?", number);
 				}
 			}
 		}
 
 		else {
 			_control.sendMessage("We're sorry, this message could not be parsed.", number);
 		}
 	}
 
 	//Subscribes a user to an IObservable
 	public boolean subscribeUserToObservable(String firstNameToSubscribeTo, String lastNameToSubscribeTo, String number) {
 		//Checking to see that the person is registered in the database -- linear search through all people.
 		boolean found = false;
 		if(_store == null)
 			return false;
 
 		int idSpectator = -1;
 		for (IPerson i: _store.getPeople()) {
 			if(i.getPhoneNumber().equals(number)) {
 				found = true;
 				idSpectator = i.getID();
 				break;
 			}
 		}
 
 		if (!found) {
 			return false;
 		}
 
 		/* Checking to see that the person/club to be followed exists */
 		found = false;
 		int counter = 0; //To make sure that we don't get duplicates
 		int idToFollow = 0;
 		if(lastNameToSubscribeTo.equals("")) { // Last name empty, this is a club.
 			for (IClub i: _store.getClubs()) {
 				if(i.getName().equalsIgnoreCase(firstNameToSubscribeTo.toLowerCase())) {
 					idToFollow = i.getID();
 					found = true;
 					counter++;
 				}
 			}
 		}
 		else {
 			for (IPlayer i: _store.getPlayers()) {
 				if(i.getFirstName().equalsIgnoreCase(firstNameToSubscribeTo.toLowerCase())) {
 					if (i.getLastName().equalsIgnoreCase(lastNameToSubscribeTo)) {
 						idToFollow = i.getID();
 						found = true;
 						counter++;
 					}
 				}
 			}
 		}
 
 		/* Handling error messages */
 		if (!found) {
 			_control.sendMessage("We're sorry, no fencer or group was found with this name.", number);
 			return false;
 		}
 		else if (counter > 1) {
 			_control.sendMessage("Multiple entries found for this name. Please respond with \"follow\" followed by an ID number.", number);
 			return false;
 		}
 
 		/* ACTUALLY SUBSCRIBING USER */
 		final int finalID = idSpectator;
 		final int finalIDToFollow = idToFollow;
 		_store.runTransaction(new Runnable(){
 			public void run(){
 				IPerson spect = _store.getPerson(finalID).addWatched(finalIDToFollow);
 				_store.putData(spect);
 			}
 		});
 		_control.updateSubscriberGUI();
 		_control.sendMessage("You were successfully subscribed to " + firstNameToSubscribeTo + " " + lastNameToSubscribeTo + "!", number);
 		return true;
 	} 
 
 	public boolean unsubscribeUserToObservable(String firstToUnsubscribe, String lastToUnsubscribe, String number) {
 		//Finding the spectator at this phone number
 		int idToFind = 0;
 		boolean found = false;
 		for (IPerson i: _store.getPeople()) {
 			if (i.getPhoneNumber().equals(number)) {
 				idToFind = i.getID();
 				found = true;
 				break;
 			}
 		}
 		if(!found) {
 			_control.sendMessage("Error: this phone number is not registered.", number);
 			return false;
 		}
 
 		/* Finding the person/club to be unsubscribed from*/
 		found = false;
 		int counter = 0; //To make sure that we don't get duplicates
 		int idToUnfollow = 0;
 		if(lastToUnsubscribe.equals("")) { // Last name empty, this is a club.
 			for (IClub i: _store.getClubs()) {
 				if(i.getName().equalsIgnoreCase(firstToUnsubscribe)) {
 					idToUnfollow = i.getID();
 					found = true;
 					counter++;
 				}
 			}
 		}
 		else {
 			for (IPlayer i: _store.getPlayers()) {
 				if(i.getFirstName().equalsIgnoreCase(firstToUnsubscribe)) {
 					if (i.getLastName().equalsIgnoreCase(lastToUnsubscribe)) {
 						idToUnfollow = i.getID();
 						found = true;
 						counter++;
 					}
 				}
 			}
 		}
 
 		/* Handling errors */
 		if(!found) {
 			_control.sendMessage("We're sorry, no fencer or group was found with this name.", number);
 			return false;
 		}
 		if(counter > 1) {
 			_control.sendMessage("Multiple entries found for this name. Please respond with \"unsubscribe\"" +
 					" followed by an ID number.", number);
 			return false;
 		}
 
 		/* UNSUBSCRIBING */
 		final IPerson spect = _store.getPerson(idToFind);
		found = false;
		for(Object o: spect.getWatched()) {
			Integer i = (Integer) o;
			if(i == idToFind) {
				found = true;
				break;
			}
		}
		
		if(!found) {
			_control.sendMessage("We're sorry, it does not appear that you are subscribed to this fencer", number);
			return false;
		}
		
 		if (spect == null || spect.removeWatched(idToUnfollow) == null) {
 			_control.sendMessage("Error: unsubscription not successful", number);
 			return false;
 		}
 		else {
 			_control.sendMessage("You have been successfully unsubscribed from " + firstToUnsubscribe
 					+ " " + lastToUnsubscribe + " .", number);
 			//Cleaning up Spectator
 			if (spect.getWatched().isEmpty()) {
 				_store.runTransaction(new Runnable() {
 					public void run() {
 						_store.removeData(spect);
 					}
 				});
 			}
 			return false;
 		}
 	}
 
 	public boolean swapRefs(String oldRefNumber, String newRefNumber){
 		/**
 		 * Looking up ref ids by searching through list of referees. If either
 		 * phone number does not belong to a registered ref, the swap will not
 		 * go through.
 		 */
 		int oldRefID = 0, newRefID = 0;
 		boolean found = false;
 		for(IReferee i : _store.getReferees()){
 			if (i.getPhoneNumber().equals(oldRefNumber)) {
 				oldRefID = i.getID();
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
 			_control.sendMessage("You (message sender) are not registered as a referee.", oldRefNumber);
 			return false;
 		}
 		/* NEW REFEREE */
 		found = false;
 		for(IReferee i : _store.getReferees()){
 			if (i.getPhoneNumber().equals(newRefNumber)) {
 				newRefID = i.getID();
 				found = true;
 				break;
 			}
 		}
 
 		if (!found) {
 			_control.sendMessage("You (message sender) are not registered as a referee.", oldRefNumber);
 			return false;
 		}
 
 		/* Now we know both numbers are valid referees */
 		_control.swapRefs(oldRefID, newRefID);
 		_control.sendMessage("Swap successful", oldRefNumber);
 		return true;
 
 	}
 
 }
