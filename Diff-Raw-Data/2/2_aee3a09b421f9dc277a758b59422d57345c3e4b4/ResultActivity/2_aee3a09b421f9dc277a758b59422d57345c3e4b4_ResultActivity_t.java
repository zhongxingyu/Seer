 package coinFlipV1.gitmad.app;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseIntArray;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.ScaleAnimation;
 import android.widget.ImageView;
 import android.widget.TextView;
 import coinFlipV1.gitmad.app.db.CoinFlipDbOpenHelper;
 
 public class ResultActivity extends Activity implements OnClickListener {
     
     public static final String PARAM_RESULT = "result";
 
     private String result = "not set", name = "david";
 	private int resultImage = 0, retries = 0, currTotal = 0;
 	private HttpURLConnection connection;
     private URL url;
     
     public void connectionSetup() {
     	try {
     		url = new URL("http://gitmadleaderboard.herokuapp.com/scores");
     	} catch (MalformedURLException e) {
     		throw new RuntimeException(e);
     	}
     }
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.result);
 		
 		String result = getIntent().getExtras().getString(ResultActivity.PARAM_RESULT);
 		setResult(result);
 		
 		connectionSetup();
 		
 		//open a writable database connection
 		CoinFlipDbOpenHelper dbHelper = new CoinFlipDbOpenHelper(this);
 	    SQLiteDatabase database = dbHelper.getWritableDatabase();
         	    
 	    try {
 	        //record the flip and update the distribution
     	    recordCoinFlip(database);
     	    updateFlipDistribution(database);
     	    
     	    // Spin off Async task
     		new postResultsTask().execute();
 	    } finally {
 	        database.close();
 	    }
 
 	    Log.d("Demo", getResult());
 		
 		int images[] = {R.drawable.heads, R.drawable.tails};
 		ImageView resultImageView = (ImageView) this.findViewById(R.id.result_value_image);
 		resultImageView.setBackgroundResource(images[0]);
 		
 		//TextView text = (TextView) this.findViewById(R.id.result_value_label);
 		//text.setText(getResult());
 				
		if (getResult().equals("heads"))
 		{
 			resultImage = R.drawable.heads;
 			flipAnimate(resultImageView, images, 0, 9, true);
 		}
 		else
 		{
 			resultImage = R.drawable.tails;
 			flipAnimate(resultImageView, images, 0, 7, true);
 		}
 
 		View flipCoinButton = findViewById(R.id.back_to_menu_button);
 		flipCoinButton.setOnClickListener(this);
 	}
 	
 	private class postResultsTask extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... params) {
 			Log.d("currTotal", String.valueOf(currTotal));
 			postScore(name, String.valueOf(currTotal));
 			return null;
 		}
 		
 		protected void onPostExecute(Void result) {}
 	}
 	
 	private void postScore(String name, String score) {
 		HttpURLConnection connection = null;
 		
 		try {
 			String leaderboardEntry = new String("{\"score\": { \"name\": \"" + name + "\", \"score\": " + score + "}}");
 			connection = (HttpURLConnection)url.openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type", "application/json");
 			connection.getOutputStream().write(leaderboardEntry.getBytes());
 			connection.connect();
 			connection.getResponseCode();
 			Log.d("resp code", String.valueOf(connection.getResponseCode()));
 			this.retries = 0;
 		} catch (IOException e) {
 			this.retries++;
 			if(this.retries >= 5) 
 				throw new RuntimeException(e);
 		} finally {
 			if(connection != null)
 				connection.disconnect();
 		}
 	}
 
     private void updateFlipDistribution(SQLiteDatabase database) {
 	    //process the counts
 	    SparseIntArray spArray = new SparseIntArray();
         spArray.put(0, 0);
         spArray.put(1, 0);
         int total = 0;
         Cursor c = database.query(CoinFlipDbOpenHelper.FLIP_TABLE,
                 new String[] {CoinFlipDbOpenHelper.FLIP_TYPE, "count(*)"},
                 null, null, CoinFlipDbOpenHelper.FLIP_TYPE, null, null);
 
         try {
             //iterate thru the cursor...there should be 1 or 2 entries
             //NOTE: first col of 0 indicates heads, 1 indicates tails
             c.moveToFirst();
             while(!c.isAfterLast()) {
                 int count = c.getInt(1);
                 spArray.put(c.getInt(0), count);
                 
                 total += count;
                 c.moveToNext();
             }
         } finally {
             c.close();
         }
         currTotal = total;
         //format the text for the flip distribution
         String text;
         if (total > 0) {
             text = String.format("%d flip%s, %2.0f%% heads, %2.0f%% tails", total, total != 1 ? "s" : "", spArray.get(0) * 100.0 / total, spArray.get(1) * 100.0 / total);
         } else {
             text = "0 flips.";
         }
         TextView textView = (TextView) this.findViewById(R.id.flip_distribution);
         textView.setText(text);
     }
 
     private void recordCoinFlip(SQLiteDatabase database) {
         ContentValues values = new ContentValues();
 	    values.put(CoinFlipDbOpenHelper.FLIP_TYPE, getResult().equals("heads") ? 0 : 1);
 	    database.insert(CoinFlipDbOpenHelper.FLIP_TABLE, null, values);
     }
 
 	public void setResult(String result) {
 		this.result = result;
 	}
 
 	public String getResult() {
 		return result;
 	}
 
 	private void flipAnimate(final ImageView imageView, final int images[], final int imageIndex, final int iterations, final boolean isShrunk) {
 		
 		if (iterations > 0) {
 		
 			Animation curAnim = null;
 			ScaleAnimation shrink = new ScaleAnimation(1, 0, 1, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) .5);
 			ScaleAnimation expand = new ScaleAnimation(0, 1, 1, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) .5);
 			
 			if (isShrunk)
 			{
 				curAnim = expand;
 				imageView.setBackgroundResource(images[imageIndex]);
 			}
 			else{
 				curAnim = shrink;
 			}
 			
 			curAnim.setDuration(300);
 			
 			imageView.setAnimation(curAnim);
 			
 			curAnim.setAnimationListener(new AnimationListener() {
 				public void onAnimationEnd(Animation animation) {
 					int newIndex = imageIndex;
 					if (isShrunk)
 					{
 						if (imageIndex == 1)
 						{
 							newIndex = 0;
 						}
 						else
 						{
 							newIndex = 1;
 						}
 					}
 					
 					flipAnimate(imageView, images, newIndex, iterations-1, !isShrunk);
 		        }
 		        public void onAnimationRepeat(Animation animation) {
 		            // TODO Auto-generated method stub
 		        }
 		        public void onAnimationStart(Animation animation) {
 		            // TODO Auto-generated method stub
 		        }
 			});
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		this.finish();
 	}
 }
