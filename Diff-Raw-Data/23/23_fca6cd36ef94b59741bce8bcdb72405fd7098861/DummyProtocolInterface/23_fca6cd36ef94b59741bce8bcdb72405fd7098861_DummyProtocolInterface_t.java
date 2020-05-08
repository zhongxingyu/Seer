 package ch.bfh.evoting.voterapp.protocol;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.content.LocalBroadcastManager;
 import ch.bfh.evoting.voterapp.AndroidApplication;
 import ch.bfh.evoting.voterapp.entities.Option;
 import ch.bfh.evoting.voterapp.entities.Poll;
 import ch.bfh.evoting.voterapp.entities.VoteMessage;
 import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
 
 public class DummyProtocolInterface extends ProtocolInterface {
 
 	private Context context;
 
 	public DummyProtocolInterface(Context context) {
 		super(context);
 		this.context = context;
 	}
 
 	@Override
 	public void showReview(Poll poll) {
 		//Add protocol specific stuff to the poll
 
 		//Send poll to other participants
 		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW, (Serializable)poll);
 		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
 
 		//Send a broadcast to start the next activity
 		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
 		intent.putExtra("poll", (Serializable)poll);
 		intent.putExtra("sender", AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId());
 		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);	
 	}
 
 	@Override
 	public void beginVotingPeriod(Poll poll) {
 
 		if(AndroidApplication.getInstance().isAdmin()){
 			//called when admin want to begin voting period
 			
 			// Send start poll signal over the network
 			VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
 			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
 
 			//Do some protocol specific stuff
 
 			//start service listening to incoming votes and stop voting period events
 			context.startService(new Intent(context, VoteService.class).putExtra("poll", poll));
 
 			//Send a broadcast to start the review activity
 			Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
 			intent.putExtra("poll", (Serializable)poll);
 			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 		} else {
 			//called when start message received from admin
 			
 			//Do some protocol specific stuff
 			
 			//start service listening to incoming votes and stop voting period events
 			context.startService(new Intent(context, VoteService.class).putExtra("poll", poll));
 		}
 	}
 
 	@Override
 	public void endVotingPeriod() {
 		//Send stop signal over the network
 		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_STOP_POLL, null);
 		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
 
 		//send broadcast containing the stop voting period event
 		Intent i = new Intent(BroadcastIntentTypes.stopVote);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
 		//The VoteService listens to this broadcast and a calls the computeResult method
 	}
 
 	@Override
 	public void vote(Option selectedOption, Poll poll) {
 		//do some protocol specific stuff
 
 		//send the vote over the network
 		AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, selectedOption));
 
 		//Send a broadcast to start the review activity
 		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
 		intent.putExtra("poll", (Serializable)poll);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 	}
 
 	/**
 	 * Method called when the result must be computed (all votes received or stop asked by the admin)
 	 * @param poll poll object
 	 */
 	public void computeResult(Poll poll){
 
 		context.stopService(new Intent(context, VoteService.class));
 
 		//do some protocol specific stuff
 		//go through compute result and set percentage result
 		List<Option> options = poll.getOptions();
 		int votesReceived = 0;
 		for(Option option : options){
 			votesReceived += option.getVotes();
 		}
 		for(Option option : options){
 			if(votesReceived!=0){
 				option.setPercentage(option.getVotes()*100/votesReceived);
 			} else {
 				option.setPercentage(0);
 			}
 		}
 
 		poll.setTerminated(true);
 
 		//Send a broadcast to start the review activity
 		Intent intent = new Intent(BroadcastIntentTypes.showResultActivity);
 		intent.putExtra("poll", (Serializable)poll);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 	}
 
 	@Override
 	protected void handleReceivedPoll(Poll poll, String sender) {
 		//do some protocol specific stuff
 
 		//Send a broadcast to start the review activity
 		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
 		intent.putExtra("poll", (Serializable) poll);
 		intent.putExtra("sender", sender);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 	}
 
 	@Override
	public void exportToXML(File file, Poll poll){
 		FileOutputStream outputStream;
 		try {
 			outputStream = new FileOutputStream(file);
 			outputStream.write("toto\ntoto2".getBytes());
 			outputStream.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void cancelVotingPeriod() {
 		Intent i = new Intent(context, VoteService.class);
 		context.stopService(i);
 	}
 
 
 
 
 
 }
