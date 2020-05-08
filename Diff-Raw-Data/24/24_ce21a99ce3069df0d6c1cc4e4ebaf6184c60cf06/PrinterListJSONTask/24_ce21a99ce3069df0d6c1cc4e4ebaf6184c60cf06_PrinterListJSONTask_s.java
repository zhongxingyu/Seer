 package com.example.sprint;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 import com.compuware.apm.uem.mobile.android.*;
 
 public class PrinterListJSONTask extends AsyncTask<Context, Void, String> {
 	private PrinterListFragment printerListFragment;
 	ProgressDialog pd;
 	ArrayList<Printer> printers = new ArrayList<Printer>();
 	
 	
 	// Constructor for the PrinterListJSONTask
 	public PrinterListJSONTask(PrinterListFragment printerListFragment) {
 		this.printerListFragment = printerListFragment;
 	}
 	
 	@Override
 	protected String doInBackground(Context... params) {
 		try{
 		    // Create a new HTTP Client and setup the GET
 		    DefaultHttpClient defaultClient = new DefaultHttpClient();
 		    HttpGet httpGetRequest = new HttpGet("http://10.24.16.122/show_printers.php");
 		    
 		    // Set timeout for the connection
 		    final HttpParams httpParameters = defaultClient.getParams();
 		    HttpConnectionParams.setConnectionTimeout(httpParameters, 6000);
 		    HttpConnectionParams.setSoTimeout(httpParameters, 6000);
 		    
 		    // Execute the request in the client
 		    HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
 		    
 		    // Check if the response was OK
 		    if(httpResponse.getStatusLine().getStatusCode() == 200)
 		    {
 		    	// Convert the httpResponse into a JSONArray
 		    	HttpEntity entity = httpResponse.getEntity();
 			    InputStream ist = entity.getContent();
 			    ByteArrayOutputStream content = new ByteArrayOutputStream();
 			    int readCount = 0;
 			    byte[] buffer = new byte[1024];
 			    while ((readCount = ist.read(buffer)) != -1)
 			    {
 			    	content.write(buffer, 0, readCount);
 			    }
 			    String retVal = new String(content.toByteArray());
 			    
 			    // Attempt to convert the return value to a JSON Array
 			    try {
 				    JSONArray jsonArray = new JSONArray(retVal);
 	
 				    // List of printers
 				    ArrayList<Printer> printerList = new ArrayList<Printer>();
 				    
 				    // Loop through the jsonArray and get the name and port
 				    for (int i = 0; i < jsonArray.length(); i++) {
 				        JSONObject row = jsonArray.getJSONObject(i);
 				        String name = row.getString("printer_name");
 				        String port = row.getString("port_name");
 				        
 				        printerList.add(new Printer(name,
 				        		port, false));
 				    }
 				    
 				    // Assign the printers
 				    printers = printerList;
 				    
 			        return "Success";
 			    } catch (Exception e) {
 			    	e.printStackTrace();
 			    }
 			    
 			    return "Failed";
 		    }
 
 		    return "Failed";
 		} catch(java.net.SocketTimeoutException e) {
 			return "Timeout";
 		} catch(Exception e) {
 			e.printStackTrace();  
 		}
 		
         return "Failed";
 	}
 	
 	@Override
 	protected void onPostExecute(String result) {
 		if(result.equals("Success"))
 		{
 			// Dynatrace Printer List Load Leave
 			CompuwareUEM.leaveAction("Printer List Load");
 		}
 		else if(result.equals("Timeout"))
 		{
 			// Timeout tell the user to check if they are connected to Compuware and retry
			Toast.makeText(printerListFragment.getActivity(), "Timeout!", Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 			// Error occurred most likely on the back end
			Toast.makeText(printerListFragment.getActivity(), "Error - loading printers!", Toast.LENGTH_LONG).show();
 		}
 		
 		// Set the printers list up and dismiss the progress dialog
 		printerListFragment.setPrinters(printers);
 		printerListFragment.showList();
 		pd.dismiss();
 		printerListFragment.notifyPrinterLoadComplete();
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		// Dynatrace Printer List Load Enter
 		CompuwareUEM.enterAction("Printer List Load");
 		
 		// Clear the printer list and create the progress dialog
 		printerListFragment.clearList();
 		pd = new ProgressDialog(printerListFragment.getActivity());
 		pd.setMessage("Loading Printers ...");
 		pd.show();
 		final AsyncTask<Context, Void, String> task = this;
 		pd.setOnDismissListener(new OnDismissListener() {
 			
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				// Allows canceling the progress dialog
 				task.cancel(true);
 			}
 		});
 	}	
 }
