 package org.raegdan.bbstalker;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.raegdan.bbstalker.MyLocation.LocationResult;
 
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.format.Time;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DBListActivity extends Activity implements OnItemClickListener, OnClickListener {
 	
 	BlindbagDB database;
 	DBList dblist;
 	String CurrentBBUniqID = "";
 	Integer CurrentDBListID = 0;
 	Integer HideMode = 0;
 
 	ListView lvDBList;
 	SimpleAdapter saDBList;
 	TextView tvDBHeader;
 
 	PopupWindow pw;
 	View vPWBBInfo;
 	RelativeLayout rlPWBBInfo;
 	TextView tvPWBBInfoName;
 	TextView tvPWBBInfoMisc;
 	ImageView ivPWBBInfoPonyPic;
 	ImageButton ibPWBBWiki;
 	ImageButton ibPWBBShareVK;
 	ImageButton ibPWBBShareGPlus;
 	ImageButton ibPWBBShareTwi;
 	ImageButton ibPWBBCart;
 	ImageButton ibPWBBUncart;
 	EditText etPWBBShareShopname;
 	
 	ProgressDialog mDialog;
 	HashMap<String, Object> LocationCache;
 	
 	final static int HM_BY_COUNT = 1;
 	final static int HM_BY_PRIORITY = 2;
 	
 	protected class DBList
 	{
 		List<HashMap<String, Object>> data;
 		String[] fields;
 		int[] views;
 		
 		DBList()
 		{
 			data = new ArrayList<HashMap<String, Object>>();
 		}
 	}
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dblist);
 		
 		tvDBHeader = (TextView) findViewById(R.id.tvDBHeader);		  
 		
 		database = new BlindbagDB();
 		
 		LocationCache = new HashMap<String, Object>();
 		LocationCache.put("time", new Time().toMillis(true));
 		LocationCache.put("location", String.valueOf(""));
 		LocationCache.put("timeout", Long.valueOf(300000));
 		
 		
 		if (!database.LoadDB(this))
 		{
 			tvDBHeader.setText(getString(R.string.json_db_err));
 			return;
 		}
 
 		String query = getIntent().getStringExtra("query");
 		
 		lvDBList = (ListView) findViewById(R.id.lvDBList);
 		lvDBList.setOnItemClickListener(this);
 		
 		if (query.equalsIgnoreCase("$"))
 		{
 			tvDBHeader.setText(getString(R.string.my_collection));
 			HideMode = HM_BY_COUNT;
		} else
 		{
			if (query.equalsIgnoreCase(""))
			{
				tvDBHeader.setText(getString(R.string.all_db));
			} else {
				tvDBHeader.setText(getString(R.string.results_for) + query + "");
			}
			
 			database = database.LookupDB(query);
			HideMode = HM_BY_PRIORITY;
		}		
 		
 		dblist = PrepareDBList(database, HideMode);
 		saDBList = new SimpleAdapter(this, dblist.data, R.layout.lvdblist, dblist.fields, dblist.views);
 		lvDBList.setAdapter(saDBList);
 	}
 	
 	protected DBList PrepareDBList (BlindbagDB database, Integer HideMode)
 	{
 		DBList dl = new DBList();
 		dl.fields = new String[] {"name", "misc", "count", "img1"};
 		dl.views = new int[] {R.id.tvLVDBListName, R.id.tvLVDBListMisc, R.id.tvLVDBListCollectionCount, R.id.ivVLDBListWavePic};
 		
 		for (int i = 0; i < database.blindbags.size(); i++)
 		{
 			if (
 					(	(HideMode == HM_BY_PRIORITY) && (database.blindbags.get(i).priority == 0)	) ||
 					(	(HideMode == HM_BY_COUNT) && (database.blindbags.get(i).count == 0)	)		)
 			{
 				continue;
 			}
 			
 			String bbids = "";
 			for (int j = 0; j < database.blindbags.get(i).bbids.size(); j++)
 			{
 				bbids += database.blindbags.get(i).bbids.get(j);
 				if (j < database.blindbags.get(i).bbids.size() - 1)
 				{
 					bbids += " / ";
 				}
 			}
 			
 			Integer wavepic = this.getResources().getIdentifier("w" + database.blindbags.get(i).waveid, "drawable", this.getPackageName());
 			HashMap<String, Object> hmDBList = new HashMap<String, Object>();
 			hmDBList.put("name", database.blindbags.get(i).name);
 			hmDBList.put("misc", getString(R.string.code) + bbids);
 			hmDBList.put("count", " (" + database.blindbags.get(i).count.toString() + getString(R.string.in_collection));
 			hmDBList.put("img1", wavepic);
 			hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
 			hmDBList.put("count_int", database.blindbags.get(i).count);
 			dl.data.add(hmDBList);
 		}
 		
 		return dl;
 	}
 	
 	protected void CartUncart(String uniqid, Integer value)
 	{
 		String errmsg = getString(R.string.json_saving_err);
 		Blindbag bb = new Blindbag();
 		
 		int i;
 		
 		for (i = 0; i < database.blindbags.size(); i++)
 		{
 			bb = database.blindbags.get(i);
 			
 			if (bb.uniqid.equalsIgnoreCase(uniqid))
 			{
 				if (bb.count + value < 0)
 				{
 					return;
 				}
 				
 				bb.count += value;
 				break;
 			}
 		}
 		
 		database.blindbags.set(i, bb);
 		
 		if (!database.CommitDB(this))
 		{
 			Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_LONG).show();
 			bb.count -= value;
 			database.blindbags.set(i, bb);
 		}
 		
 		dblist.data.get(CurrentDBListID).put("count_int", bb.count);
 		dblist.data.get(CurrentDBListID).put("count", " (" + bb.count.toString() + getString(R.string.in_collection));
 		
 		tvPWBBInfoMisc.setText(getString(R.string.pcs_in_collection) + database.blindbags.get(i).count.toString());
 		
 		if (HideMode == HM_BY_COUNT)
 		{
 			if (bb.count == 0)
 			{
 				dblist.data.remove(CurrentDBListID.intValue());
 				pw.dismiss();
 			}
 		}
 		
 		saDBList.notifyDataSetChanged();
 	}
 	
 	protected void lvDBListItemClicked(Integer index)
 	{
 		CurrentBBUniqID = (String) dblist.data.get(index).get("uniqid");
 		CurrentDBListID = index;
 		ShowBBInfo();
 	}
 	
 	protected void ShowBBInfo ()
 	{
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		vPWBBInfo = inflater.inflate(R.layout.pwbbinfo, null);
 		rlPWBBInfo = (RelativeLayout) vPWBBInfo.findViewById(R.id.rlPWBBInfo);
 		tvPWBBInfoName = (TextView) rlPWBBInfo.findViewById(R.id.tvPWBBInfoName);
 		tvPWBBInfoMisc = (TextView) rlPWBBInfo.findViewById(R.id.tvPWBBInfoMisc);
 		ivPWBBInfoPonyPic = (ImageView) rlPWBBInfo.findViewById(R.id.ivPWBBInfoPonyPic);
 		ibPWBBWiki = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBWiki);
 		ibPWBBShareVK = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareVK);
 		ibPWBBShareGPlus = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareGPlus);
 		ibPWBBShareTwi = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareTwi);	
 		etPWBBShareShopname = (EditText) rlPWBBInfo.findViewById(R.id.etPWBBSocialShareShopname);
 		ibPWBBCart = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBCart);
 		ibPWBBUncart = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBUncart);
 		
 		tvPWBBInfoName.setText(database.GetBlindbagByUniqID(CurrentBBUniqID).name);
 		tvPWBBInfoMisc.setText(getString(R.string.pcs_in_collection) + database.GetBlindbagByUniqID(CurrentBBUniqID).count);
 		ivPWBBInfoPonyPic.setImageResource(this.getResources().getIdentifier("bb" + CurrentBBUniqID, "drawable", this.getPackageName()));
 		ibPWBBWiki.setOnClickListener(this);
 		ibPWBBShareVK.setOnClickListener(this);
 		ibPWBBShareGPlus.setOnClickListener(this);
 		ibPWBBShareTwi.setOnClickListener(this);
 		ibPWBBCart.setOnClickListener(this);
 		ibPWBBUncart.setOnClickListener(this);
 		
 		final SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
 		etPWBBShareShopname.setText(sp.getString("shopname", getString(R.string.shop_field)));
 		etPWBBShareShopname.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				sp.edit().putString("shopname", etPWBBShareShopname.getText().toString()).commit();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 		
 		pw = new PopupWindow(this);
 		
 		pw.setContentView(rlPWBBInfo);
 		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
 		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
 		pw.setFocusable(true);
 		pw.showAtLocation(vPWBBInfo, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
 	}
 	
 	
 	///////////// SOCIAL SHARING METHODS LOGIC //////////////
 	//
 	// To share, call SocialShare.
 	//
 	// - SocialShare
 	//   - Cached location exists?
 	//     - yes: ActuallyShare with cached location
 	//     - no: 
 	//       - show waiting popup
 	//       - LocationRequest
 	//         - no location providers?
 	//           - hide popup
 	//           - cache null geolocation data
 	//		     - ActuallyShare w/o geotag
 	//         - no location data available? (no GPS satellites visible, no network signal)
 	//  		 - hide popup
 	//           - cache null geolocation data
 	//           - ActuallyShare w/o geotag
 	//         - location data obtained successfully?
 	//           - BitlyRequest
 	//             - change popup text
 	//             - request link compaction
 	//  		   - hide popup
 	//             - Link successfully compacted via Bitly?
 	//               - yes:
 	//                 - cache link
 	//                 - ActuallyShare w/geotag
 	//               - no:
 	//				   - cache null geolocation data	
 	//                 - ActuallyShare w/o geotag
 	//
 	//////////////////////////////////////////////////////////
 	
 	protected void SocialShare(Integer SocialNetwork)
 	{		
 		Time t = new Time();
 		t.setToNow();
 		
 		if ((t.toMillis(true) - (Long) LocationCache.get("time")) < ((Long) LocationCache.get("timeout")))
 		{
 			ActuallyShare(SocialNetwork, ((String) LocationCache.get("location")));
 		} else {
 			mDialog = new ProgressDialog(this);
 	        mDialog.setMessage(getString(R.string.trying_to_locate));
 	        mDialog.setCancelable(false);
 	        mDialog.show();
 	        
 	    	MyLocation myLocation;
 			myLocation = new MyLocation();
 			LocationResult locationResult = new LocationRequest(SocialNetwork);
 			myLocation.getLocation(this, locationResult);			
 		}
 	}
 	
 	protected class LocationRequest extends LocationResult
 	{
 		Integer sn;
 		
 		public LocationRequest(Integer SocialNetwork) {
 			sn = SocialNetwork;
 		}
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		public void gotLocation(Location location, int ErrCode) {
 			switch (ErrCode)
 			{
 				case MyLocation.EC_NO_PROVIDERS:
 				{
 					mDialog.dismiss();
 					Time t = new Time();
 					t.setToNow();
 					LocationCache.put("time", t.toMillis(true));
 					LocationCache.put("location", null);
 					ActuallyShare(sn, null);
 					
 					break;
 				}
 				
 				case MyLocation.EC_NO_DATA:
 				{
 					mDialog.dismiss();
 					Time t = new Time();
 					t.setToNow();
 					LocationCache.put("time", t.toMillis(true));
 					LocationCache.put("location", null);
 					ActuallyShare(sn, null);
 					
 					break;
 				}
 				
 				case MyLocation.EC_NO_ERR:
 				{
 					HashMap<String, Object> query = new HashMap<String, Object>();
 					query.put("sn", sn);
 					query.put("link", "http://maps.google.com/maps?q=" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()));
 					new BitlyRequest().execute(query);
 					
 					break;
 				}
 			}		
 		}	
 	}
 	
 	protected class BitlyRequest extends AsyncTask<HashMap<String, Object>, Integer, HashMap<String, Object>>
 	{
 		@Override
 	    protected void onPreExecute()
 		{
 			super.onPreExecute();
 			mDialog.setMessage(getString(R.string.trying_to_get_link));
 	    }
 
 		@Override
 		protected HashMap<String, Object> doInBackground(HashMap<String, Object>... params)
 		{
 			HashMap<String, Object> result = new HashMap<String, Object>();
 			result.put("link", null);
 				
 			try 
 			{
 				String API_KEY = "a6d306ec04569d71baa6459738622fde5d37262f";
 				String BITLY_API_URL = "https://api-ssl.bitly.com/v3/shorten?access_token=" + API_KEY + "&longUrl=";
 				
 			    HttpClient hc = new DefaultHttpClient(); 
 			    HttpGet hg = new HttpGet(BITLY_API_URL + URLEncoder.encode((String) params[0].get("link"), "UTF-8"));
 			    HttpResponse hr = hc.execute(hg);
 			    HttpEntity he = hr.getEntity();  
 
 			    if (he == null)
 			    {  
 			    	return null;
 			    }
 			    
 			    String response = EntityUtils.toString(he);
 			    
 			    result.put("link", new JSONObject(response).getJSONObject("data").getString("url").replaceAll("http://", "").replaceAll("https://", ""));
 			} 
 			catch (ParseException e) {}
 			catch (JSONException e) {} 
 			catch (UnsupportedEncodingException e) {}
 			catch (IOException e) {}
 			
 			result.put("sn", (Integer) params[0].get("sn"));
 			return result;
 		}
 			
 		@Override
 		protected void onPostExecute (HashMap<String, Object> result)
 		{
 			Time t = new Time();
 			t.setToNow();
 			LocationCache.put("time", t.toMillis(true));
 			LocationCache.put("location", (String) result.get("link"));
 				
 			mDialog.dismiss();
 			ActuallyShare((Integer) result.get("sn"), (String) result.get("link"));		
 		}
 	}
 	
 	protected void ActuallyShare(Integer SocialNetwork, String GeoLink)
 	{
 		SocialShare ss = new SocialShare(this);
 		
 		String message = getString(R.string.social_msg_p1) + database.GetBlindbagByUniqID(CurrentBBUniqID).waveid + getString(R.string.social_msg_p2) + database.GetBlindbagByUniqID(CurrentBBUniqID).name + getString(R.string.social_msg_p3) + etPWBBShareShopname.getText().toString();
 		if (GeoLink != null)
 		{
 			message += getString(R.string.social_msg_p4) + GeoLink;
 		}
 		message += getString(R.string.social_msg_p5);
 		
 		String text = getString(R.string.no_social_app_p1);
 		switch (SocialNetwork.intValue())
 		{
 			case SocialShare.SN_VK:
 				text += "VK.com ";
 				break;
 				
 			case SocialShare.SN_TWITTER:
 				text += "Twitter ";
 				break;
 				
 			case SocialShare.SN_GPLUS:
 				text += "Google+ ";
 				break;
 		}
 		
 		text += getString(R.string.no_social_app_p2);
 
 		if (!ss.Share(message, SocialNetwork.intValue()))
 		{
 			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
 		}	
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		switch (arg0.getId())
 		{
 			case R.id.lvDBList:
 				lvDBListItemClicked(arg2);
 				break;
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		Blindbag CurrentBlindbag = database.GetBlindbagByUniqID(CurrentBBUniqID);
 		
 		switch (v.getId())
 		{
 			case R.id.ibPWBBWiki:
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CurrentBlindbag.wikiurl)));
 				break;
 				
 			case R.id.ibPWBBShareVK:
 				SocialShare(SocialShare.SN_VK);
 				break;
 
 			case R.id.ibPWBBShareGPlus:
 				SocialShare(SocialShare.SN_GPLUS);
 				break;
 
 			case R.id.ibPWBBShareTwi:
 				SocialShare(SocialShare.SN_TWITTER);
 				break;
 				
 			case R.id.ibPWBBCart:
 				CartUncart(CurrentBBUniqID, +1);
 				break;
 
 			case R.id.ibPWBBUncart:
 				CartUncart(CurrentBBUniqID, -1);
 				break;
 
 		}
 	}
 }
