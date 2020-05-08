 package de.geotweeter;
 
 import android.os.AsyncTask;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.apiconn.TwitterApiAccess;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 
 public class Conversation {
 
 	private TimelineElementAdapter tea;
 	private TwitterApiAccess api;
 	private boolean backwards;
 	
	public Conversation(TimelineElementAdapter tea, Account current_account, boolean backwards) {
 		this.tea = tea;
 		this.backwards = backwards;
 		api = current_account.getApi();
		current_account.pushTimeline(tea);
 		new LoadConversationTask().execute(tea.getItem(0));
 	}
 	
 	private class LoadConversationTask extends AsyncTask<TimelineElement, TimelineElement, Void> {
 
 		@Override
 		protected Void doInBackground(TimelineElement... params) {
 			TimelineElement current_element = params[0];
 			if (current_element.getClass() != Tweet.class) {
 				throw new ClassCastException("Conversation should be based on a tweet");
 			}
 			Tweet current = (Tweet) current_element;
 			while (current.in_reply_to_status_id != 0) {
 				long predecessor_id = current.in_reply_to_status_id;
 				current = (Tweet) TimelineActivity.availableTweets.get(predecessor_id);
 				if (current == null) {
 					current = api.getTweet(predecessor_id);
 				}				
 				publishProgress(current);
 			}
 			return null;
 		}
 		
 		protected void onProgressUpdate(TimelineElement... params) {
 			if (backwards) {
 				tea.add(params[0]);
 			} else {
 				tea.addAsFirst(params[0]);
 			}
 		}
 		
 	}
 	
 }
