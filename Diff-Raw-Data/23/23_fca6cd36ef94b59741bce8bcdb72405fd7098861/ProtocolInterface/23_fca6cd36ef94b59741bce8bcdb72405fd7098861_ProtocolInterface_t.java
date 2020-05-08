 package ch.bfh.evoting.voterapp.protocol;
 
 import java.io.File;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.support.v4.content.LocalBroadcastManager;
 import ch.bfh.evoting.voterapp.AndroidApplication;
 import ch.bfh.evoting.voterapp.entities.Option;
 import ch.bfh.evoting.voterapp.entities.Poll;
 import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
 
 public abstract class ProtocolInterface {
 
 	private Context context;
 	
 	/**
 	 * Interface object for protocol implementation
 	 * @param context android context
 	 */
 	public ProtocolInterface(Context context){
 		this.context = context;
 
 		// Register a broadcast receiver listening for the poll to review
 		LocalBroadcastManager.getInstance(this.context).registerReceiver(
 				new BroadcastReceiver() {
 					@Override
 					public void onReceive(Context context, Intent intent) {
 						if(!AndroidApplication.getInstance().isAdmin()){
 							Poll poll = (Poll) intent.getSerializableExtra("poll");
 							// Poll is not in the DB, so reset the id
 							poll.setId(-1);
 							handleReceivedPoll(poll, intent.getStringExtra("sender"));
 						}
 					}
 				}, new IntentFilter(BroadcastIntentTypes.pollToReview));
 	}
 
 	/**
 	 * Method called by the administrator when he want to start the review process
 	 * @param poll poll to review
 	 */
 	public abstract void showReview(Poll poll);
 	
 	/**
 	 * Method called when a message containing the poll to review is received
 	 * @param poll poll to review
 	 * @param sender sender of the poll
 	 */
 	protected abstract void handleReceivedPoll(Poll poll, String sender);
 
 	/**
 	 * Method called by the administrator when he want to start the voting period
 	 * and by the voter when he receives the start message from the administrator
 	 * @param poll poll object already received for the review
 	 */
 	public abstract void beginVotingPeriod(Poll poll);
 
 	/**
 	 * Method called by the administrator when he want to end the voting period
 	 */
 	public abstract void endVotingPeriod();
 
 	/**
 	 * Method called when a participant want to want
 	 * @param selectedOption the option the participant choose to vote
 	 * @param poll the poll object
 	 */
 	public abstract void vote(Option selectedOption, Poll poll);
 
 	/**
 	 * Method called at the end of the vote, when displaying results. This methods
 	 * create a file in the app data containing the protocol values. This file can then
 	 * be used to verify the vote outside of this app. 
 	 * @param file filename
 	 */
	public abstract void exportToXML(File file, Poll poll);
 
 	/**
 	 * Method called when the administrator wants to cancel the voting period
 	 */
 	public abstract void cancelVotingPeriod();
 
 }
