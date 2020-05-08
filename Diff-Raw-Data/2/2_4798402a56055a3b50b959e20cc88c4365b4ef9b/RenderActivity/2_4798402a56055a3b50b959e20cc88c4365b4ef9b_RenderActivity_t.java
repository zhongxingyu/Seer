 package com.greylock;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.fima.cardsui.views.CardUI;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 //public class RenderActivity extends BroadcastReceiver{
 //	private static final String TAG = "RenderActivity";
 //	
 //	  @Override
 //	  public void onReceive(Context context, Intent intent) {
 //	    try {
 //	      String action = intent.getAction();
 //	      String channel = intent.getExtras().getString("com.parse.Channel");
 //	      JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
 //	 
 //	      Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
 //	      Iterator itr = json.keys();
 //	      while (itr.hasNext()) {
 //	        String key = (String) itr.next();
 //	        Log.d(TAG, "..." + key + " => " + json.getString(key));
 //	      }
 //	    } catch (JSONException e) {
 //	      Log.d(TAG, "JSONException: " + e.getMessage());
 //	    }
 //	  }
 //
 //}
 
 public class RenderActivity extends Activity {
 	private CardUI mCardView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_cards);
 		final String deviceId = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
 		
 		System.out.println("Oncreate activity!");
 		
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("cardInfo");
 		query.whereEqualTo("device_id", deviceId);
 		Date now = new Date();
		Date before = new Date(now.getTime()-(1000*60*10));
 		query.whereGreaterThan("createdAt", before);
 		
 		mCardView = (CardUI) findViewById(R.id.cardsview2);
 		mCardView.setSwipeable(true);
 		
 		query.findInBackground(new FindCallback<ParseObject>() {
 		    public void done(List<ParseObject> results, ParseException e) {
 		        if (e == null && results.size()>0) {
 		        	for (int i=0; i<results.size(); i++){
 		        		ParseObject res = results.get(i);
 		        		String cardTitle = res.getString("english");
 		        		String cardContent = res.getString("translation");
 		        		String cardPlace = res.getString("place");
 		        		String cardService = res.getString("service");
 		        		System.out.println("CARD INFO:" + cardTitle + " + " + cardContent);
 		        		
 		        		if (i==0){
 		        			mCardView.addCard(new MyCard(cardTitle, cardContent + " \n Sent because you checked in at " + cardPlace + " with " + cardService));
 		        		}
 		        		else{
 		        			mCardView.addCardToLastStack(new MyCard(cardTitle, cardContent));
 		        		}
 		        		
 		        	}
 		        	mCardView.refresh();
 		        } else {
 		        	System.out.println("no data");
 		        }
 		        mCardView.refresh();
 		    }
 		});
 	}
 	
 	public void renderNewCard (String frenchWord, String englishWord) {
 		mCardView.addCard(new MyCard(frenchWord, englishWord));
 		mCardView.refresh();
 	}
 }
