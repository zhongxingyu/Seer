 package net.skup.swifty.model;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import net.skup.swifty.R;
 import net.skup.swifty.DownloadFilesTask;
 import net.skup.swifty.DownloadFilesTask.Downloader;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 
public class ChallengesProvider extends Activity implements Downloader {
 
 	private List<Pun> challenges = new ArrayList<Pun>();
     private Set<Long> blacklist = new HashSet<Long>();//used up
 	private int limit = 100;
 	public static final String challengesURL = "http://tom-swifty.appspot.com/challenges.json";
     private static Context applicationContext;
     
 	private static ChallengesProvider instance = null;
 	private ChallengesProvider() {
 	}
 	
 	public static ChallengesProvider getInstance(Context a) {
 		if (instance == null) {
 			applicationContext = a;
 			instance = new ChallengesProvider();
 		}
 		return instance;
 	}
 	
 	public int available() {
 		return challenges.size();
 	}
 	
 	int blacklistSize() {
 		return blacklist.size();
 	}
 	
 	/** Disqualify or consume a pun. */
     public void disqualify(long id) {
     	Iterator<Pun> it = challenges.iterator();
     	boolean found = false;
     	Pun p = null;
         while (it.hasNext()) { 
         	p = (Pun) it.next();
         	if (p.getCreatedTimeSeconds() == id) {
         		found = true;
         		break;
         	}
         }
         if (found) challenges.remove(p);
     	blacklist.add(id);
     }
     
     public ChallengeBlock getChallenge(int max) {
     	return getChallenge(max, null);
     }
     
     /**
      * Prepares a set of challenges containing one match of a Pun statement to its adverb and (max - 1) mismatches,
      * If data is not available from the fetch falls back to sample data.  
      * @param sentinal - if null this is added as the first position in the list 
      * @param max - the maximum size of the challenge list
      * @return a list of challenges or null upon error
      */
 	public ChallengeBlock getChallenge(int max, String sentinal) {
 		
 		Log.i(getClass().getSimpleName()+" getChallenge ", "challenges.size:"+challenges.size() +" blacklist-size:"+blacklistSize());
 		if (challenges.size() <= 0) {
 			challenges = fetchSynchronously(max);
 			if (challenges.size() <= 0) {
 				Log.e(getClass().getName(),"getChallenge: could not get challenges, even fallback data.");
                 return null;
 			}
 		}
         
 		List<String> adverbs = new ArrayList<String>(max);
 		Pun challengePun = null;
 		
 		// find a qualified challenge and its correct answer
 		Iterator<Pun> it = challenges.iterator();
 		if (it.hasNext()) {
 			do {
 				challengePun = it.next();
 			} while (!blacklist.isEmpty() &&  blacklist.contains(challengePun.getCreatedTimeSeconds()));
 		}
 		adverbs.add(challengePun.getAdverb());
 
 
 		// add N unique candidate adverbs
 		it = challenges.iterator();
 		while (it.hasNext() && (adverbs.size() < max)) {
 			challengePun = it.next();
 			if (adverbs.contains(challengePun.getAdverb())) continue;
 			if (blacklist.contains(challengePun)) continue;
 			adverbs.add(challengePun.getAdverb());
 		}
 		
 		Collections.shuffle(adverbs);
 		if (sentinal != null && !sentinal.isEmpty()) adverbs.add(0, sentinal);
 		return new ChallengeBlock(challengePun, adverbs);
 	}
 	
 	public static class ChallengeBlock {
 		public final List<String> candidates; 
 		public final Pun pun;
 		public ChallengeBlock(Pun pun, List<String> candidates) {
 			super();
 			this.pun = pun;
 			this.candidates = candidates;
 		}
 	}
 
 	/** Fallback data.*/
 	// ApplicationContext vs ActivityContext
 	//http://stackoverflow.com/questions/4391720/how-can-i-get-a-resource-content-from-a-static-context
 	//http://stackoverflow.com/questions/3572463/what-is-context-in-android
 	//http://stackoverflow.com/questions/5498669/android-needing-context-in-non-activity-classes
 	//http://stackoverflow.com/questions/9239462/difference-applicationcontext-vs-activity-context-in-android
 	//http://https417.blogspot.com/2013/04/activity-independent-asynctasks.html
 	// There are two easy ways to avoid context-related memory leaks. The most obvious one is to avoid escaping the context outside of its own scope. ...  The second solution is to use the Application context. This context will live as long as your application is alive and does not depend on the activities life cycle. If you plan on keeping long-lived objects that need a context, remember the application object. You can obtain it easily by callingContext.getApplicationContext() or Activity.getApplication().
 	//http://android-developers.blogspot.com/2009/01/avoiding-memory-leaks.html
 	List<Pun> fetchSynchronously(int max) {
     	List<Pun> challengesSych = new ArrayList<Pun>();
     	if (applicationContext == null) throw new RuntimeException("no context");
 		InputStream fis = applicationContext.getResources().openRawResource(R.raw.challenges);
 		String stringified = Pun.convertToString(fis);
 		challengesSych = Pun.deserializeJson(stringified);
 		Log.i(getClass().getSimpleName()+" fetchSynchronously","got fallbk challenges.size"+ challengesSych.size());
 		return challengesSych;
 	}
 		
 	public void fetch(int max) {
 		limit = max;
 		new DownloadFilesTask(this).execute(new String[] {challengesURL});
 	}
 
 	@Override
 	public void setData(String data) {
 		List<Pun> newPuns = Pun.deserializeJson(data);
 		Log.i(getClass().getSimpleName()+" setData","Puns.size/blacklist size"+ newPuns.size() +"/"+blacklist.size());
 
 		for (Pun p : newPuns) {
 			//TODO do not add redundant
 			if ( ! blacklist.contains(p.getCreatedTimeSeconds() /*&& ! (challenges).find(p.getCreatedTimeSeconds())*/)) {
 				challenges.add(p);
 			}
 			if (challenges.size() >= limit) break;
 		}
 		Log.i(getClass().getSimpleName()+" setData","challenges.size"+ challenges.size());
 	}
 }
