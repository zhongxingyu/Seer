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
 
 
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GocardDisplayFragment extends Fragment {
 
 	
 	private DefaultHttpClient httpClient;
 	
 	private String goCardNumber;
 	private String password;
 	
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		
 		if (getArguments() == null) {
 			goCardNumber ="";
 			password = "";
 		} else {
 			if (goCardNumber == null) {
 				goCardNumber = getArguments().getString("goCardNumber");
 			}
 			if (password == null) {
 				password = getArguments().getString("password");
 			}
 		}
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
 	{
 		View view = inflater.inflate(R.layout.gocard_page, container, false);
 		
 		super.onCreate(savedInstanceState);
 		HttpThread ht = new HttpThread();
 		String url = "https://gocard.translink.com.au/webtix/welcome/welcome.do";
 		getActivity().setProgressBarIndeterminateVisibility(true);
 		ht.execute(url);
 		
 		
 		
 		return view;
 	}
 	
 	
 	private DefaultHttpClient createHttpClient()
 	{
 	    /*HttpParams params = new BasicHttpParams();
 	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
 	    HttpProtocolParams.setUseExpectContinue(params, true);
 
 	    SchemeRegistry schReg = new SchemeRegistry();
 	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 	    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
 
 	    return new DefaultHttpClient(conMgr, params);*/
 		
 		DefaultHttpClient ret =null;
 		
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
 	
 	public String postData(String url) {
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
 	        Log.d( "GoCard", "Number Entered="+ goCardNumber );
 	        /*nameValuePairs.add(new BasicNameValuePair("CardNumber", gcnumText.getText().toString()));
 	        nameValuePairs.add(new BasicNameValuePair("Password", passwordText.getText().toString()));*/
 	        nameValuePairs.add(new BasicNameValuePair("cardNum", goCardNumber ));
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
 	    	//Log.d("TEST HTTPPOST", result);
 	    	
 	    	super.onPostExecute(result);
 	    	
 	    	parseResult(result);
 	    }	
 	}
 	
 	public void parseResult(String result) {
 		Log.d("GoCard", result);
 		Log.d("GoCard", "resultEndOfFile=" + result.substring(result.length()-20));
 		
 		if (result.contains("<table id=\"balance-table\"")) {
 			parseResultAsBalance(result);
 			getActivity().setProgressBarIndeterminateVisibility(true);
 			
 			
 			
 		} else if (result.contains("<table id=\"travel-history\"")) {
 			parseResultAsHistory(result);
 			getActivity().setProgressBarIndeterminateVisibility(false);
 			
 		} else {
 			TableLayout table = (TableLayout) getView().findViewById(R.id.history_table);
 	        Context tableContext = table.getContext();
 			TableRow newRow = new TableRow(tableContext);
 			Context rowContext = newRow.getContext();
 			TextView text = new TextView(rowContext);
         	text.setText(result);
         	newRow.addView(text);
         	table.addView(newRow);
         	getActivity().setProgressBarIndeterminateVisibility(false);
         	//Toast.makeText(getActivity().getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
        	
         	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        	builder.setMessage("Something went wrong. Either server is down, or has changed or  \nan incorrect username/password was entered.")
         			.setCancelable(false)
         			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							goBackWithError();
 							
 						}
 					});
         	
         	AlertDialog alert = builder.create();
         	alert.show();
         	
         	
         	
 		}
 		
 	}
 	
 	public void parseResultAsBalance(String result) {
 		int indexOfCardBalance = result.indexOf("<table id=\"balance-table\"");
 		int indexOfEndOfCardBalance = result.indexOf("</table>", indexOfCardBalance);
 		String tableSubstr = result.substring(indexOfCardBalance, indexOfEndOfCardBalance);
 		String[] tableRowStrings = tableSubstr.split("<tr>"); 
 		for (int i = 2; i < tableRowStrings.length; i++) {
 			String rowStr = tableRowStrings[i];
 			TableLayout table = (TableLayout) getView().findViewById(R.id.balance_table);
 	        Context tableContext = table.getContext();
 			TableRow newRow = new TableRow(tableContext);
 			Context rowContext = newRow.getContext();
 			DisplayMetrics scale = getActivity().getResources().getDisplayMetrics();
         	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, scale);
         	newRow.setMinimumHeight(height);
         	TextView text = new TextView(rowContext);
         	int endOfFirstColumn = rowStr.indexOf("</td>");
         	String dateText = rowStr.substring(rowStr.indexOf("<td>")+4, endOfFirstColumn);
         	text.setText(dateText);
         	//text.setText(result);
         	 TableRow.LayoutParams param0 = new TableRow.LayoutParams();
              param0.column = 0;
              param0.span = 1;
              param0.weight = 1;
              text.setLayoutParams(param0);
         	newRow.addView(text);
         	
         	TextView text1 = new TextView(rowContext);
         	String costText = rowStr.substring(rowStr.indexOf("<td>", endOfFirstColumn)+4, rowStr.indexOf("</td>", endOfFirstColumn+4));
         	text1.setText(costText);
         	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
             param1.column = 1;
             param1.span = 1;
             param1.weight = 1;
             text1.setLayoutParams(param1);
             Log.d("GoCard", "dateText="+dateText);
             Log.d("GoCard", "costText="+costText);
             newRow.addView(text1);
             
         	table.addView(newRow);
 		}
 		HttpThread ht = new HttpThread();
 		//ht.execute(gcNumber, password);
 		
 		String url = "https://gocard.translink.com.au/webtix/tickets-and-fares/go-card/online/history";
 		ht.execute(url);
 	}
 	
 	public void parseResultAsHistory(String result) {
 		
 		int indexOfHistory = result.indexOf("<table id=\"travel-history\"");
 		int indexOfEndOfHistory = result.indexOf("</table>", indexOfHistory);
 		String tableSubstr = result.substring(indexOfHistory, indexOfEndOfHistory);
 		String[] tableRowStrings = tableSubstr.split("<tr class=\"sub-heading\">"); 
 		for (int i = 1; i < tableRowStrings.length; i++) {
 			String rowStr = tableRowStrings[i];
 			TableLayout table = (TableLayout) getView().findViewById(R.id.history_table);
 	        Context tableContext = table.getContext();
 			TableRow newRow = new TableRow(tableContext);
 			Context rowContext = newRow.getContext();
 			DisplayMetrics scale = getActivity().getResources().getDisplayMetrics();
         	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, scale);
         	newRow.setMinimumHeight(height);
         	TextView text = new TextView(rowContext);
         	int endOfFirstColumn = rowStr.indexOf("</td>");
         	String dateText = rowStr.substring(rowStr.indexOf("<td colspan")+16, endOfFirstColumn).trim();
         	text.setText(dateText);
         	//text.setText(result);
         	 TableRow.LayoutParams param0 = new TableRow.LayoutParams();
              param0.column = 0;
              param0.span = 5;
              param0.weight = 1;
              text.setLayoutParams(param0);
         	newRow.addView(text);
         	table.addView(newRow);
         	
         	String[] rowsInDate = rowStr.split("<tr>");
         	
    
         	for (int j=1; j<rowsInDate.length; j++) {
         		TableRow newRow2 = new TableRow(tableContext);
         		Context rowContext2 = newRow2.getContext();
         		String[] rowInDateCols = rowsInDate[j].split("<td");
         		
         		//debug
         		for (int k=0;k<rowInDateCols.length; k++) {
         			if (k!=0) {
         				Log.d("GoCard", "parsed rowInDateCols["+k+"]= "+ rowInDateCols[k].
         						substring(rowInDateCols[k].indexOf(">")+1, rowInDateCols[k].
         		        				indexOf("</td>")).trim());
         			}
         		}
         		
         		
         		String timeText = rowInDateCols[1].substring(rowInDateCols[1].indexOf(">")+1, rowInDateCols[1].
         				indexOf("</td>")).trim();
         		String touchOnText = rowInDateCols[2].substring(rowInDateCols[2].indexOf(">")+1, rowInDateCols[2].
         				indexOf("</td>")).trim();
         		String time2Text = rowInDateCols[3].substring(rowInDateCols[3].indexOf(">")+1, rowInDateCols[3].
         				indexOf("</td>")).trim();
         		String touchOffText = rowInDateCols[4].substring(rowInDateCols[4].indexOf(">")+1, rowInDateCols[4].
         				indexOf("</td>")).trim();
         		String priceText = rowInDateCols[5].substring(rowInDateCols[5].indexOf(">")+1, rowInDateCols[5].
         				indexOf("</td>")).trim();
         		
         		
 	        	TextView text1 = new TextView(rowContext2);
 	        	text1.setText(timeText);
 	        	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
 	            param1.column = 0;
 	            param1.span = 1;
 	            param1.weight = 1;
 	            text1.setLayoutParams(param1);
 	            newRow2.addView(text1);
 	            
 	            TextView text2 = new TextView(rowContext2);
 	        	text2.setText(touchOnText);
 	        	TableRow.LayoutParams param2 = new TableRow.LayoutParams();
 	            param2.column = 1;
 	            param2.span = 1;
 	            param2.weight = 1;
 	            text2.setLayoutParams(param2);
 	            newRow2.addView(text2);
 	            
 	            TextView text3 = new TextView(rowContext2);
 	        	text3.setText(time2Text);
 	        	TableRow.LayoutParams param3 = new TableRow.LayoutParams();
 	            param3.column = 2;
 	            param3.span = 1;
 	            param3.weight = 1;
 	            text3.setLayoutParams(param3);
 	            newRow2.addView(text3);
 	            
 	            TextView text4 = new TextView(rowContext2);
 	        	text4.setText(touchOffText);
 	        	TableRow.LayoutParams param4 = new TableRow.LayoutParams();
 	            param4.column = 3;
 	            param4.span = 1;
 	            param4.weight = 1;
 	            text4.setLayoutParams(param4);
 	            newRow2.addView(text4);
 	            
 	            TextView text5 = new TextView(rowContext2);
 	        	text5.setText(priceText);
 	        	TableRow.LayoutParams param5 = new TableRow.LayoutParams();
 	            param4.column = 4;
 	            param4.span = 1;
 	            param4.weight = 1;
 	            text5.setLayoutParams(param5);
 	            newRow2.addView(text5);
 	            
 	        	table.addView(newRow2);
         	}
 		}
 		
 	}
 	
 	public void goBackWithError() {
 		//This breaks things so it'll be gone for now
 		
 		/*
 		FragmentManager manager = getActivity().getSupportFragmentManager();
     	FragmentTransaction transaction = manager.beginTransaction();
  		transaction.remove(this);
  		transaction.addToBackStack(null);
  		transaction.commit();*/
 	}
 }
