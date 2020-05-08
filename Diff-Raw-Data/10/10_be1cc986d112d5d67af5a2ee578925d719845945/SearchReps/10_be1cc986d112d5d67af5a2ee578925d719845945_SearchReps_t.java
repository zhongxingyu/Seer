 package com.openaussearchdroid;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SearchReps extends Activity
 {
 
 	private static final String oakey = "F8c6oBD4YQsvEAGJT8DUgL8p";
 	private EditText _etext;
 	private TextView _tv;
 	private ImageView _iv;
 	private Button _repsbutton;
 	private LinearLayout _tab;
 	@SuppressWarnings("unused")
 	private TextView _tvhans;
 	private String previousSearch = "";
 
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.searchrep_reps);
 		_iv = (ImageView) findViewById(R.id.MemberPic);
 		_tab = (LinearLayout) findViewById(R.id.innerlayout);
 		_tvhans = (TextView) findViewById(R.id.hansardmentions_label);
 		_repsbutton = (Button) findViewById(R.id.searchRepButton);
 		_repsbutton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				_etext = (EditText) findViewById(R.id.EditText01);
 				String searchKey = _etext.getText().toString();
 				if (previousSearch.equals(searchKey))
 				{
 					Log.i("duplicate_search", " in search reps");
 					return;
 				}
 
 				_tab.removeAllViewsInLayout();
 
 				_tv = (TextView) findViewById(R.id.TextView01);
 				String urlString = "http://www.openaustralia.org/api/getRepresentative" +
 				"?key=" + oakey +
 				"&division=" + URLEncoder.encode(searchKey) +
 				"&output=json";
 				String result;
 				try
 				{
 					result = Utilities.getDataFromUrl(urlString, "OpenAusURL");
 				}
 				catch (IOException e)
 				{
 					Utilities.recordStackTrace(e);
 					return;
 				}
 				JSONObject json;
 				try
 				{
 					json = new JSONObject(result);
 				}
 				catch(JSONException e)
 				{
 					Log.i("Praeda", e.getMessage());
 					e.printStackTrace();
 					return;
 				}
 				String memdata = null;
 				String fullName = null;
 				String dateEntered = null;
 				String party = null;
 				String personID = null;
 				String imgLoc = null;
 				try
 				{
 					fullName = "Name: " + json.getString("full_name") + "\n";
 				}
 				catch (JSONException e)
 				{
 					Utilities.recordStackTrace(e);
 				}
 				try
 				{
 					dateEntered = "Date Elected: " + json.getString("entered_house") + "\n";
 				}
 				catch (JSONException e)
 				{
 					Utilities.recordStackTrace(e);
 				}
 				try
 				{
 					party = "Party: " + json.getString("party");
 				}
 				catch (JSONException e)
 				{
 					Utilities.recordStackTrace(e);
 				}
 				try
 				{
 					personID = json.getString("person_id");
 				}
 				catch (JSONException e)
 				{
 					Utilities.recordStackTrace(e);
 				}
 
 				try
 				{
 					imgLoc = "http://www.openaustralia.org" + json.getString("image");
 				}
 				catch (JSONException e)
 				{
 					Utilities.recordStackTrace(e);
 					if (searchKey.equals("pwnie"))
 					{
 						imgLoc = "http://pwnies.com/images/pwnie.jpg";
 					}
 				}
 				try
 				{
 					fetchRepImage(imgLoc);
 				}
 				catch (IOException e)
 				{
 					Utilities.recordStackTrace(e);
 				}
 
 				memdata = fullName + dateEntered + party;
 
 				_tv.setText(memdata);
 				/* Grab Hansard Mentions */
 				if (personID == null)
 				{
 					Log.e("person_id", "is null ...");
 					return;
 				}
 				/* XXX: perform code review of class member access - if is potentially null */
 				urlString = "http://www.openaustralia.org/api/getDebates" +
 				"?key="+ oakey +
 				"&type=representatives" +
 				"&order=d" +
 				"&person=" + personID;
 				Log.i("OpenAusURL", urlString);
 				new PerformHansardSearch().execute(new HansardSearch(urlString, v, _tab));
 				previousSearch = searchKey;
 			}
 		});
 	}
 
 	public void fetchRepImage(String imgLoc) throws IOException
 	{
		/*
		android is so awesome that the Log.i a few lines down
		will trigger a null pointer exception - because it cannot
		print null ... right ... - bail out early.
		The pwnie has been really useful in debugging!
		*/
		if (imgLoc == null)
		{
			return;
		}
 		URL aURL;
 		Log.i("searchrepimage", imgLoc);
 		aURL = new URL(imgLoc);
 
 		URLConnection con;
 		con = aURL.openConnection();
 
 		InputStream is;
 		BufferedInputStream bis = null;
 
 		con.connect();
 		is = con.getInputStream();
 		bis = new BufferedInputStream(is);
 		/* Decode url-data to a bitmap. */
 		Bitmap bm = BitmapFactory.decodeStream(bis);
 		try
 		{
 			bis.close();
 		}
 		catch (IOException e)
 		{
 			Utilities.recordStackTrace(e);
 		}
 		try
 		{
 			is.close();
 		}
 		catch (IOException e)
 		{
 			Utilities.recordStackTrace(e);
 		}
 		_iv.setImageBitmap(bm);
 	}
 
 	public void searchRepClickHandler(View target)
 	{
 
 	}
 
 }
