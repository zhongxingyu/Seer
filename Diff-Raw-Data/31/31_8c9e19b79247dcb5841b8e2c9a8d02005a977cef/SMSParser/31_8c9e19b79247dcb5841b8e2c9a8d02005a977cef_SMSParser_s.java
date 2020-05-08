 package final_project.control;
 
 import java.util.Scanner;
 import java.util.Calendar;
 import final_project.model.store.*;
 
 //import mocks.*;
 
 public class SMSParser {
 
 	private IDataStore _store;
 	private ISMSController _control;
 	private Calendar _cal;
 
 	public SMSParser(IDataStore s, ISMSController ctrl) {
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
 
 		//Message: "help string"
 		if (firstWord.equals("Help") || firstWord.equals("help")) {
 			_control.alertGUI("Help message received! Message: " + received, _cal.getTime());
 			/* Alerting the proper group for help (either technical or medical) */
 			if(s.hasNext()) {
 				String groupToAlert = s.next();
 				if(groupToAlert.equals("medical") || groupToAlert.equals("Medical"))
 					_control.sendGroupMessage("Medical", received);
 				else if(groupToAlert.equals("technical") || groupToAlert.equals("Technical"))
 					_control.sendGroupMessage("Technical", received);
 			}
 		}
 
 		// Message: "Follow first last" or "follow clubName"
 		else if(firstWord.equals("Follow") || firstWord.equals("follow")) {
 			System.out.println("Follow message received");
 			String firstName = "", lastName = "";
 			if(s.hasNext())
 				firstName = s.next();
 			if(s.hasNext())
 				lastName = s.next();
 
 			this.subscribeUserToObservable(firstName, lastName, number);
 		}
 
 		// Message: "unsubscribe first last" or "Unsubscribe clubName"
 		else if(firstWord.equals("Unsubscribe") || firstWord.equals("unsubscribe")) {
 			String firstName = "", lastName = "";
 			if(s.hasNext())
 				firstName = s.next();
 			if(s.hasNext())
 				lastName = s.next();
 
 			this.unsubscribeUserToObservable(firstName, lastName, number);
 		}
 
 		//Message: "refswap number"
 		else if(firstWord.equals("Refswap") || firstWord.equals("refswap")) {
 			String newRefNumber = "";
 			if(s.hasNext())
 				newRefNumber = s.next();
 
 			this.swapRefs(number, newRefNumber);
 		}
 
 		//Message "result id beat id this-that" or "id beat id this to that"
 		else if(firstWord.toLowerCase().equals("result")) {//TODO changed tolower case and then equals
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
 			if(s.hasNext() && !s.next().equals("beat")) { //Eating "beat" token
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
				if (!next.equals("to") || !next.equals("-")){ //Eating "to"/"-" token
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
 			_control.returnResults(refID, winID, winScore, loseID, loseScore);
 		}
 
 		else {
 			_control.sendMessage("We're sorry, this message could not be parsed.", number);
 		}
 	}
 
 	//Subscribes a user to an IObservable
 	public boolean subscribeUserToObservable(String firstNameToSubscribeTo, String lastNameToSubscribeTo, String number) {
 		//Checking to see that the person is registered in the database -- linear search through all people.
 		boolean found = false;
 		System.out.println("Gets into subscribeUser " + number);
 		if(_store == null)
 			return false;
 
 		for (IPerson i: _store.getPeople()) {
 			System.out.println(i.getPhoneNumber());
 			if(i.getPhoneNumber().equals(number)) {
 				found = true;
 				break;
 			}
 		}
 
 		System.out.println("person found? " + found);
 		if (!found) {
 			return false;
 		}
 		System.out.println("gets before checking person/club exists");
 
 		/* Checking to see that the person/club to be followed exists */
 		found = false;
 		int counter = 0; //To make sure that we don't get duplicates
 		int idToFollow = 0;
 		if(lastNameToSubscribeTo.equals("")) { // Last name empty, this is a club.
 			System.out.println("inside club");
 
 			for (IClub i: _store.getClubs()) {
 				if(i.getName().equals(firstNameToSubscribeTo)) {
 					idToFollow = i.getID();
 					found = true;
 					counter++;
 				}
 			}
 		}
 		else {
 			for (IPlayer i: _store.getPlayers()) {
 				System.out.println("inside player");
 
 				if(i.getFirstName().equals(firstNameToSubscribeTo)) {
 					if (i.getLastName().equals(lastNameToSubscribeTo)) {
 						idToFollow = i.getID();
 						found = true;
 						counter++;
 					}
 				}
 			}
 		}
 
 		/* Handling error messages */
 		if (!found) {
 			System.out.println("Not found");
 			_control.sendMessage("We're sorry, no fencer or group was found with this name.", number);
 			return false;
 		}
 		else if (counter > 1) {
 			_control.sendMessage("Multiple entries found for this name. Please respond with \"follow\" followed by an ID number.", number);
 			return false;
 		}
 		System.out.println("Subscribing");
 		/* ACTUALLY SUBSCRIBING USER */
 		found = false;
 		int idSpectator = 0;
 		for (IPerson i: _store.getPeopleForGroup("Spectator")) {
 			if (i.getPhoneNumber().equals(number)) {
 				found = true;
 				idSpectator = i.getID();
 				break;
 			}
 		}
 
 		final int finalID = idSpectator;
 		final int finalIDToFollow = idToFollow;
 		_store.runTransaction(new Runnable(){
 			public void run(){
 				IPerson spect = _store.getPerson(finalID).addWatched(finalIDToFollow);
 				_store.putData(spect);
 			}
 		});
 		System.out.println("Finishing subscribe should be texting user");
 		_control.sendMessage("You were successfully subscribed to " + firstNameToSubscribeTo + lastNameToSubscribeTo + " !", number);
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
 				if(i.getName().equals(firstToUnsubscribe)) {
 					idToUnfollow = i.getID();
 					found = true;
 					counter++;
 				}
 			}
 		}
 		else {
 			for (IPlayer i: _store.getPlayers()) {
 				if(i.getFirstName().equals(firstToUnsubscribe)) {
 					if (i.getLastName().equals(lastToUnsubscribe)) {
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
