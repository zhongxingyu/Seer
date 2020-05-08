 package final_project.control;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.Timer;
 import java.io.Serializable;
 import final_project.model.store.*;
 import final_project.model.*;
 
 public class SMSController implements Constants, ISMSController {
 
 	private SMSSender _sender;
 	private SMSParser _parser;
 	private SMSReceiver _receiver;
 	private TournamentController _tournament;
 	private Timer _timer;
 	private Calendar _cal;
 	private boolean _sendingMessages;
 
 	/**
 	 *  Constructor needs to take the username & password for the BulkSMS API
 	 * @param s
 	 * @param t
 	 * @param username
 	 * @param password
 	 */
 	public SMSController(IDataStore s, TournamentController t, String username, String password) {
		_sendingMessages = false;
 		_tournament = t;
 		_cal = Calendar.getInstance();
 
 		/* Making sender and parser */
 		_sender = new SMSSender(s, this, username, password);
 		_parser = new SMSParser(s, this);
 		_receiver = new SMSReceiver(this, username, password);
 
 		/* Starting the timer to continuously check the inbox */
 		_timer = new Timer(true);
 		_timer.scheduleAtFixedRate(_receiver, RECIEVE_START_DELAY, RECEIVE_TIMER_STEP);
 
 		_receiver.flushInbox(); //Flushing the inbox to make sure that it's empty
 	}
 
 	/* TODO: How to handle the booleans that the sender methods return? */
 	public void sendMessage(String message, String number) {
 		System.out.println("Send message called: " + message + " to " + number);
 		System.out.println("Sending actual text messages: " + _sendingMessages);
 		if(_sendingMessages)
 			_sender.sendMessage(message, number);
 	}
 
 	public void sendCollectionMessage(String message, Collection<Integer> people) {
         System.out.println("sendCollectionMessage "+message+" to "+people);
 		_sender.sendCollectionMessage(message,people);
 	}
 
 	public void sendAllMessage(String message) {
 		_sender.sendAllMessage(message);
 	}
 
 	public void sendGroupMessage(String group, String message) {
 		_sender.sendGroupMessage(group, message);
 	}
 
 	public void sendFencerStripMessage(int id,  int strip) {
 		_sender.sendFencerStripMessage(id, strip);
 	}
 
 	public void sendMatchNotifications(IncompleteResult result, int refID, int stripID) {
 		System.out.println("Send match notifications called: ");
 		System.out.println("Sending actual text messages: " + _sendingMessages);
 		if(_sendingMessages)
 			_sender.sendMatchNotifications(result, refID, stripID);
 	}
 
 	public void parseOutput(String received, String number) {
 		_parser.parseOutput(received, number);
 	}
 
 	public void returnResults(int refID, int winnerID, int winnerScore, int loserID, int loserScore) {
 		/* Making the CompleteResult */
 		CompleteResult cr = new CompleteResult(new PlayerResult(winnerID, winnerScore), new PlayerResult(loserID, loserScore));
 		_tournament.addCompletedResult(cr, refID);
 	}
 
 	public void swapRefs(int oldRefID, int newRefID) {
 		_tournament.swapRef(0, oldRefID, newRefID);
 	}
 
 	public void alertGUI(String message, Date time) {
 		//should call some sort of alert method
 	}
 
 	public Date getTime() {
 		return _cal.getTime();
 	}
 }
