 package mobserv.smsgaming;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * Provides methods to see if a given challenge has been completed
  * @author lolo
  *
  */
 public class SMSParser {
 
 	ArrayList<Group> groups = new ArrayList<Group>();
 	Player user;
 	
 	SMSParser() {
 		groups = null;
 	}
 	
 	SMSParser(Player user, ArrayList<Group> groups) {
 		this.groups = groups;
 		this.user = user;
 		if (null == user){
 		    Log.e("SMSParser", "No user provided !");
 		}
 	}
 	
 	/**
 	 * Looks through the SMS inbox for messages containing
 	 *  <code>chall.objective</code>.
 	 * 
 	 * @param chall(Challenge)	challenge we're looking for
 	 * @param act(Activity)	activity the method was called from
 	 * @return		first matching SMS, null if nothing found
 	 */
 	public String searchSMS(Challenge chall, Activity act){
 		if (chall.isCompleted()){
 			return "challenge already completed";
 		}
 		String[] projection = {"address","date","body", "_id"};//The informations we are interested in the SMSs
 		Group group = null;
 		ArrayList<String> playerNumbers;
 		Cursor cursor = act.getContentResolver().query(Uri.parse("content://sms/inbox"), projection, null, null, null);
 		//int lastSearch = chall.lastSearch;
 		
 		Log.d("SMSParser", "Searching for challenge "+chall.toString());
 
 		//Try to find the group...
 		for (Group g : groups){
 		    if (g.getName() == chall.getGroupname()){
 			group = g;
 		    }
 		}
 		if (null == group){
 		    Log.e("SMSParser", "Group "+chall.getGroupname()+" was not found.");
 //		    return "No group !";
 		}
 		playerNumbers = group.getPlayerNumbers();
 		
 		if (cursor.getCount() == 0){
 		    Log.e("SMSParser", "You don't have any SMS, loser !");
 		    return "No SMS !";
 		}
 		
 		
 		//cursor.moveToPosition(cursor.getCount() - lastSearch - 1);//Go to the last message checked, according to the Challenge object
 		cursor.moveToLast();
 		do{
 		   String msgData = cursor.getString(2);
 		   String sender = cursor.getString(0);
 		   //Log.d("SMSParser", "Message "+cursor.getString(3)+" from : "+cursor.getString(0)+" : "+msgData);
		   if (msgData.contains(chall.objective) ){
		   //if (msgData.contains(chall.objective) && playerNumbers.contains(sender) ){
 		       Log.d("SMSParser", "    Challenge completed : "+msgData);
 		       user.challengeCompleted(group,chall);
 		       return "Success";
 		   }
 		}while(cursor.moveToPrevious());
 		chall.lastSearch = cursor.getCount();
 		return "No matching sms !!";
 	}
 	/**
 	 * Looks through the SMS inbox for messages containing <code>obj</code>.
 	 * 
 	 * @param obj	string we're looking for
 	 * @param act	activity the method was called for
 	 * @return		first matching SMS
 	 */
 	public String searchSMS(String obj, Activity act){
 		return searchSMS(
 				new Challenge(act, obj, 0, null, false),
 				act);
 	}
 	
 	/**
 	 * This method is called by SMSReceiver when messages are incoming
 	 * Currently only printing the SMS
 	 * 
 	 * @param msg : String containing one or more SMS
 	 */
 	@Deprecated
 	public void parse(String msg){
 		Log.i("SMSParser", msg);
 		try{
 			//groups.get(0).getUser().challengeCompleted(groups.get(0), groups.get(0).getChallenges().get(0));
 			//groups.get(0).getChallenges().get(0).setCompleted();
 		}
 		catch (java.lang.NullPointerException e){
 			Log.e("SMSParser", "I don't know where the 'groups' is !!!");
 		}
 	}
 }
