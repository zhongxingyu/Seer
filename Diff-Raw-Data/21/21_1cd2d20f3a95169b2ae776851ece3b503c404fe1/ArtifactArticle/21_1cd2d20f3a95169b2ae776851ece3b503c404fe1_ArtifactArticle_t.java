 package edu.ua.moundville;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import edu.ua.moundville.DBHandler.DBResult;
 
 public class ArtifactArticle extends Activity implements DBResult {
 	
 	private static final String TAG = "ArtifactArticle";
 	private String artifactID;
 	private String artifactTitle;
 	private String artifactBody;
 	private String primaryImageSubUrl;
 	private ImageView primaryImage;
 	private final String URL = "http://betatesting.as.ua.edu/mapexperience/images";
 	private String[] timePeriod = new String[2];
 	private ArrayList<String> categories= new ArrayList<String>();
 	private ArrayList<String> tags = new ArrayList<String>();
 	protected ArrayList<NameValuePair> queryArgs = new ArrayList<NameValuePair>();
 	protected static DBHandler db = new DBHandler();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
         setContentView(R.layout.artifact_article);
 	    
         queryArgs.add(new BasicNameValuePair("case", "6"));
 	    artifactID = getIntent().getExtras().getString("artifact");
 	    queryArgs.add(new BasicNameValuePair("art", artifactID));
 	    db.sendQuery(this, queryArgs);
 	}
 	
 	private void launchCategoryList() {
 		
 	}
 	
 	private void launchTagList() {
 		
 	}
 	
 	private void launchTimePeriod() {
 		
 	}
 	
 	private void getContent() {
 		
 	}
 	
 	private void prepareContent() {
 		setTitle(artifactTitle);
 		
 		Spanned formatedText = Html.fromHtml(artifactBody);
 		TextView articleBody = (TextView) findViewById(R.id.article_body);
 		articleBody.setBackgroundColor(Color.BLACK);
 		articleBody.setTextColor(Color.WHITE);
 		articleBody.setTextSize(20);
 		articleBody.setText(formatedText.toString());
 		
 		primaryImage = (ImageView)findViewById(R.id.primary_image);
 		new FetchImageTask() { 
 	        protected void onPostExecute(Bitmap result) {
 	            if (result != null) {
 	            	primaryImage.setImageBitmap(result);
 	            }
 	        }
 	    }.execute(URL + "/" + primaryImageSubUrl);
 	    
 	    
 	    addArticleFields();
 	}
 	
 	private void addArticleFields() {
 		LinearLayout layout = (LinearLayout) findViewById(R.id.article_linear_layout);
 		addFieldToLayout(layout, "Time Period");
 		addButton(layout, timePeriod[1], "5", "timepd");
 		addFieldToLayout(layout, "Categories");
 		addOptionalFields(layout, categories, "4", "cat");
 		addFieldToLayout(layout, "Styles");
 		addOptionalFields(layout, tags, "2", "style");
 	}
 	
 	private void addOptionalFields(LinearLayout layout, ArrayList<String> fields, String DbCase, String argKey) {
 		for (int i=0 ; i < fields.size(); i++) {
 			addButton(layout, fields.get(i), DbCase, argKey);
 		}
 	}
 	
 	private void addToArrayList(ArrayList<String> list, String item) {
 		for (int i = 0; i<list.size(); i++) {
 			if (list.get(i).equals(item)) {
 				return;
 			}
 		}
 		
 		list.add(item);
 	}
 	
 	private void addButton(LinearLayout layout, final String name, final String DBCase, final String argKey) {
 		Button button = new Button(this);
 		button.setText(name);
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent launchActivity;
 	        	launchActivity = new Intent(getApplicationContext(), ListItems.class);
 	        	launchActivity.putExtra("case", DBCase);
 	        	if (argKey != null) {
 	        		launchActivity.putExtra(argKey, name);
 	        	}
 	        	
 	        	startActivity(launchActivity);
 			}
 		});
 		layout.addView(button);
 	}
 	
 	private void addFieldToLayout(LinearLayout layout, String text) {
 		TextView textView = new TextView(this);
 		textView.setText(text);
 		textView.setGravity(Gravity.CENTER_HORIZONTAL);
 		textView.setBackgroundColor(Color.rgb(139, 69, 19));
 		layout.addView(textView);
 	}
 
 	public void receiveResult(JSONArray jArray) {
 		if (jArray == null) {
 			//Handle Failure
 		} else {
 			Log.d(TAG, jArray.toString());
 
 			for (int i=0; i<jArray.length(); i++) {
 				JSONObject obj = null;
 				try {
 					obj = (JSONObject) jArray.get(i);
 					
 					artifactTitle = obj.getString("ak_Art_Title");
 					artifactBody = obj.getString("Art_Body");
 					timePeriod[0] = obj.getString("pk_Time_TimeID");
 					timePeriod[1] = obj.getString("ak_Time_TimePeriod");
 					
 					primaryImageSubUrl = obj.getString("Img_Image");
 					
 					addToArrayList(categories, obj.getString("ak_Cat_Name"));
 					addToArrayList(tags, obj.getString("ak_Tag_Name"));
 					
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		prepareContent();
 	}
 	private class FetchImageTask extends AsyncTask<String, Integer, Bitmap> {
 	    @Override
 	    protected Bitmap doInBackground(String... arg0) {
 	    	Bitmap b = null;
 	    	try {
 				 b = BitmapFactory.decodeStream((InputStream) new URL(arg0[0]).getContent());
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} 
 	        return b;
 	    }	
 	}
 }
