 package transponders.transmob;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import android.annotation.SuppressLint;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
import android.widget.Toast;
 
 /**
  * The Android activity that shows the result of the journey planner
  * in a Web View.
  * 
  * @author Transponders
  * @version 1.0
  */
 public class ShowJourneyPage extends Fragment implements JSONRequest.NetworkListener {
 
 	public final static String ARGS_JOURNEY = "JOURNEY_PARAMETERS";
 	String result;
 	WebView wv;
 	
 	// For testing purposes
 	String URL = null;
 	private JSONRequest request;
 	
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
 	{
 		View view = inflater.inflate(R.layout.journey_page, container, false);
 
 		getActivity().setProgressBarIndeterminateVisibility(true);
 		
 		wv = (WebView) view.findViewById(R.id.webView);
 		WebSettings settings = wv.getSettings();
 		settings.setDomStorageEnabled(true);
 		settings.setJavaScriptEnabled(true);
 		wv.setWebViewClient(new CustomWebViewClient());
 		
 		String[] ids = getArguments().getStringArray(ARGS_JOURNEY);
 		String fromId = ids[0];
 		String destId = ids[1];
 		String date = ids[2];
 		String leaveOption = ids[3];
 		String vehicleType = ids[4];
 		String maxWalkDistance = ids[5];
 		requestPlan(fromId, destId, date, leaveOption, vehicleType, maxWalkDistance);
 		
 		return view;
 	}
 
 	private void requestPlan(String fromId, String destId, String date, String leaveOption, String vehType, String maxWalk) {
 
 		/* Call our php code on web zone */
 
 		String urlString = "http://deco3801-010.uqcloud.net/journeyplan.php?fromLocId="
 				+ Uri.encode(fromId) + "&destLocId=" + Uri.encode(destId) + "&date=" + Uri.encode(date)
 				+ "&leaveOption=" + Uri.encode(leaveOption) 
 				+ "&vehicleTypes=" + Uri.encode(vehType) 
 				+ "&maxWalkDistance=" + Uri.encode(maxWalk);
 		Log.d("JourneyMap request: ", urlString);
 		request = new JSONRequest();
 		request.setListener(this);
 		request.execute(urlString);
 	}
 
 	@Override
 	public void networkRequestCompleted(String result) {
 		this.result = result;
 		Log.d("ShowJourneyPage got result: ", result);
 		parseResult(result);
 	}
 
	private void parseResult(String result) 
	{
		try
		{
			Object obj = JSONValue.parse(result);
			URL = (String) ((JSONObject) obj).get("JourneyPlannerUrl");
			Log.d("URL: ", URL.toString());
			
			// Got the URL, time to load it into the web view 
			wv.loadUrl(URL);
		}
		catch(ClassCastException e)
		{
			Toast.makeText(getActivity().getApplicationContext(), 
					"Invalid input. Please go back and reenter your input.", Toast.LENGTH_LONG).show();
		}
 		
 		getActivity().setProgressBarIndeterminateVisibility(false);
 	}
 	
 	class CustomWebViewClient extends WebViewClient
 	{
 	    @Override
 	    public boolean shouldOverrideUrlLoading(WebView view, String url) 
 	    {
 	    	Log.d("CLICKED URL", url);
 	    	
 	    	return false;
 	    }
 	 
 	}
 	
 	/*Testing functions */
 	
 	public String getResultURL()
 	{
 		return URL;
 	}
 	
 	public JSONRequest getJSONRequest()
 	{
 		return request;
 	}
 	
 	/*End of Testing functions */
 }
