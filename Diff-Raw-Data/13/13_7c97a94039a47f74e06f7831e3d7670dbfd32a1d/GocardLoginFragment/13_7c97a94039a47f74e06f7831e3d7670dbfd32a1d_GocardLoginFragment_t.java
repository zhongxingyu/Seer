 package transponders.transmob;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * The Fragment class that displays the input form for the journey planner.
  * This class gets the location IDs from translink's resolve API and sends them to
  * ShowJourneyPage class, a view that displays translink's journey plan page.
  * 
  * @author Transponders
  * @version 1.0
  */
 public class GocardLoginFragment extends Fragment implements OnClickListener 
 {	
 	public final String GOCARD_NUMBER = "GOCARD_NUMBER";
 	public final String GOCARD_PASSWORD = "GOCARD_PASSWORD";
 	
 	// UI elements
 	private EditText gcnumText;
 	private EditText passwordText;
 	private Button loginButton;
 	private TextView wrongpassWarning;
 	private CheckBox rememberBox;
 	
 	private String gcNumber, password;
 	
 	private static DefaultHttpClient httpClient;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
 	{
 		View view = inflater.inflate(R.layout.gocard_login, container, false);
 		
 		gcnumText = (EditText) view.findViewById(R.id.gcnum_input);
 		passwordText = (EditText) view.findViewById(R.id.password_input);
 		wrongpassWarning = (TextView) view.findViewById(R.id.wrongpass_warning);
 		rememberBox = (CheckBox) view.findViewById(R.id.check_remember);
 		
 		SharedPreferences settings = getActivity().getSharedPreferences(NearbyStops.PREFS_NAME, 0);
 		String savedNum = settings.getString(GOCARD_NUMBER, "");
 		String savedPassword = settings.getString(GOCARD_PASSWORD, "");
 		
 		gcnumText.setText(savedNum);
 		passwordText.setText(savedPassword);
 		
 		loginButton = (Button) view.findViewById(R.id.login_button);
 		loginButton.setOnClickListener(this);
 		
 		Button gcnumClear = (Button) view.findViewById(R.id.gcnum_clear_button);
 		Button passwordClear = (Button) view.findViewById(R.id.password_clear_button);
 
 		gcnumClear.setOnClickListener(this);
 		passwordClear.setOnClickListener(this);
 	
 		return view;
 	}
 
 	@Override
 	public void onClick(View v) 
 	{		
 		switch(v.getId())
 		{
 			case R.id.gcnum_clear_button:
 				gcnumText.setText("");
 				break;
 			
 			case R.id.password_clear_button:
 				passwordText.setText("");
 				break;
 				
 			case R.id.login_button:
 				if(isNetworkAvailable())
 				{
 					gcNumber = gcnumText.getText().toString();
 					password = passwordText.getText().toString();
 					
 					if(rememberBox.isChecked())
 					{
 						SharedPreferences settings = getActivity().getSharedPreferences(NearbyStops.PREFS_NAME, 0);
 
 						SharedPreferences.Editor editor = settings.edit();
 		        	    editor.putString(GOCARD_NUMBER, gcNumber);
 		        	    editor.putString(GOCARD_PASSWORD, password);
 		        	  
 		        	    editor.commit();
 					}
 					else
 					{
 						SharedPreferences settings = getActivity().getSharedPreferences(NearbyStops.PREFS_NAME, 0);
 
 						SharedPreferences.Editor editor = settings.edit();
 		        	    editor.putString(GOCARD_NUMBER, "");
 		        	    editor.putString(GOCARD_PASSWORD, "");
 		        	    editor.commit();
 					}
 
 					getActivity().setProgressBarIndeterminateVisibility(true);
 					HttpThread ht = new HttpThread();
 					String url = "https://gocard.translink.com.au/webtix/welcome/welcome.do";
 					ht.execute(url);
 				}
 				else
 					Toast.makeText(getActivity().getApplicationContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
 				
 				break;
 		}
 	}
 	
 	private class HttpThread extends AsyncTask<String, Void, String>
 	{
 		@Override
 		protected String doInBackground(String... params) 
 		{
 			String res = postData(params[0]);
 			return res;
 		}
 		
 		@Override
 	    protected void onPostExecute(String result) {
 	    	Log.d("JSONRequest", "postexecute");
 	    	
 	    	super.onPostExecute(result);
 	    	parseResult(result);
 	    }	
 	}
 	
 	public String postData(String url) 
 	{
 	    // Create a new HttpClient and Post Header
 	    if(httpClient == null) {
 	    	httpClient = createHttpClient();
 	    }
 	    
 	    HttpPost httppost = new HttpPost(url);
 	    
 	    Log.d("postData()", "inside postData()");
 	    String result = "not yet...";
 
 	    try 
 	    {
 	        // Add your data
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 	        Log.d( "GoCard", "Number Entered="+ gcNumber );
 	        nameValuePairs.add(new BasicNameValuePair("cardNum", gcNumber ));
 	        nameValuePairs.add(new BasicNameValuePair("cardOps", "Display"));
 	        nameValuePairs.add(new BasicNameValuePair("pass", password ));
 	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 	        // Execute HTTP Post Request
 	        HttpResponse response = httpClient.execute(httppost);
 	        
 	        if (response != null) {
                 result = EntityUtils.toString(response.getEntity());
             }
 	        
 	    } catch (ClientProtocolException e) {
 	        result = "ClientProtocolException";
 	    } catch (IOException e) {
 	        result = "IOException";
 	    }
 	    
 	    return result;
 	} 
 	
 	public void parseResult(String result) {
 		Log.d("GoCard", result);
 		Log.d("GoCard", "resultEndOfFile=" + result.substring(result.length()-20));
 		
 		getActivity().setProgressBarIndeterminateVisibility(false);
 		
		if (result.contains("<table id=\"balance-table\"")) 
		{
 			wrongpassWarning.setVisibility(View.INVISIBLE);
 			
 			Fragment fragment = new GocardDisplayFragment();
 			Bundle args = new Bundle();
 			args.putString("BALANCE_RESULT", result);
 			fragment.setArguments(args);
 			
 			FragmentManager manager = getActivity().getSupportFragmentManager();
 	   		FragmentTransaction transaction = manager.beginTransaction();
      		transaction.replace(R.id.content_frame, fragment);
             transaction.addToBackStack(null);
      		transaction.commit();	
 		} 
 		else 
 		{	
 			wrongpassWarning.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private DefaultHttpClient createHttpClient()
 	{	
 		DefaultHttpClient ret = null;
 		
 		 //sets up parameters
         HttpParams params = new BasicHttpParams();
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(params, "utf-8");
         params.setBooleanParameter("http.protocol.expect-continue", false);
 
         //registers schemes for both http and https
         SchemeRegistry registry = new SchemeRegistry();
         registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
         sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
         registry.register(new Scheme("https", sslSocketFactory, 443));
 
         ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
         ret = new DefaultHttpClient(manager, params);
         return ret;
 	}
 	
 	public static DefaultHttpClient getHttpClient()
 	{
 		return httpClient;
 	}
 	
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
 	}
 }
