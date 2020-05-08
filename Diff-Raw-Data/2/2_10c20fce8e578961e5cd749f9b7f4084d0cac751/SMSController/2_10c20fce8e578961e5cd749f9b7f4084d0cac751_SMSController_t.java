 package final_project.control;
 
 import final_project.model.*;
 
 public class SMSController implements Constants, ISMSController {
 
 	// Also need a reference to the GUI
 	private SMSSender _sender;
 	private SMSParser _parser;
 	private TournamentController _tournament;
 	private Thread _sendThread;
 
 	public SMSController(IDataStore s, TournamentController t) {
 		_tournament = t;
 
 		/* Making sender and parser */
 		_sender = new SMSSender(s, this);
 		_parser = new SMSParser(s, this);
 
 		/* Starting the "receiver" thread to continuously check the inbox */
 		SMSReceiver receiver = new SMSReceiver(this);
 		_sendThread = new Thread(receiver);
		_sendThread.start();
 	}
 
 	/* TODO: How to handle the booleans that the sender methods return? */
 	public void sendMessage(String message, String number) {
 		_sender.sendMessage(message, number);
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
 
 	public void parseOutput(String received, String number) {
 		_parser.parseOutput(received, number);
 	}
 
 	public void returnResults(int refID, int winnerID, int winnerScore, int loserID, int loserScore) {
 		/* Making the CompleteResult */
 		CompleteResult cr = new CompleteResult(new PlayerResult(winnerID, winnerScore), new PlayerResult(loserID, loserScore));
 		_tournament.addCompletedResult(cr, refID);
 	}
 
 	public void swapRefs(int oldRefID, int newRefID) {
 		//should call swapRef method in tournament
 	}
 
 	public void alertGUI(String message) {
 		//should call some sort of alert method
 	}
 }
