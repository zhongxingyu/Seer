 package com.sridharavinash.teamtrafficlights;
 
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 
 
 public class TeamTrafficLightsActivity extends Activity {
 	private final String RESTPROJECTSPATH="/httpAuth/app/rest/projects";
 	private final String RESTBUILDTYPEPATH=RESTPROJECTSPATH+"/id:";
 	private final String RESTBUILDSPATH = "/httpAuth/app/rest/buildTypes/id:";
 	private String encpass =null;
 	private String decpass = null;
	private SimpleDateFormat ISO8601DATEFORMAT;
 	String FILENAME  = "TeamCityCreds";
 	
 	EditText teamCityUrl;
 	EditText teamCityUser;
 	EditText teamCityPass;
 	
 	
 	// Button Listener
 	private OnClickListener connectListener = new OnClickListener(){
 		public void onClick(View v){
 			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 
 			storeCreds(teamCityUrl.getText().toString().trim(),
 						teamCityUser.getText().toString().trim(),
 						teamCityPass.getText().toString());
 			
 			getResponse(teamCityUrl.getText().toString().trim(),
 						teamCityUser.getText().toString().trim(),
 						teamCityPass.getText().toString(),
 						RESTPROJECTSPATH);
 		}
 	};
 	
 	private String retrieveCreds(){
 		int ch;
 	    StringBuffer strContent = new StringBuffer("");
 		try {
 			FileInputStream fin =openFileInput(FILENAME);
 			while( (ch = fin.read()) != -1)
 		        strContent.append((char)ch);
 			return strContent.toString();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} catch (IOException e){
 			e.printStackTrace();
 			return null;
 		}
 		
 
 	}
 	
 	private void storeCreds(String url,String user,String password){
 		try {
 			FileOutputStream fos =openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			
 
 			encpass = SimpleCrypto.encrypt(SimpleCrypto.SEED, password);
 			String writeString = url+','+user+','+ encpass;
 			fos.write(writeString.getBytes());
 			fos.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/** Convenience function to convert inputstream(like HttpResponse) to a string */
 	private StringBuilder inputStreamToString(InputStream is) {
 	    String line = "";
 	    StringBuilder total = new StringBuilder();
 	    
 	    // Wrap a BufferedReader around the InputStream
 	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 
 	    // Read response until the end
 	    try {
 			while ((line = rd.readLine()) != null) { 
 			    total.append(line); 
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	    
 	    // Return full string
 	    return total;
 	}
 
 	/**Make a call to the server with credentials and get a response */
 	private void getResponse(final String url, final String user, final String pass,final String restPath){
 
 		try{
 				HttpHost targetHost = new HttpHost(url, 80, "http");
 				
 				HttpClient httpClient = new DefaultHttpClient();
 				
 				((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(
 						new AuthScope(targetHost.getHostName(),targetHost.getPort()), 
 						new UsernamePasswordCredentials(user,pass));
 				
 				
 				HttpGet httpGet = new HttpGet("http://"+url+ restPath);
 				HttpResponse httpResponse;
 				httpResponse = httpClient.execute(httpGet);
 				StringBuilder resp = inputStreamToString(httpResponse.getEntity().getContent());
 				
 				if(restPath.contains(RESTBUILDTYPEPATH)){
 					ArrayList<TeamCityBuilds> respXml = (ArrayList<TeamCityBuilds>) ParseXMLResponse(resp, new BuildsDataHandler());
 					new ShowBuildNames().execute(respXml);
 				}
 				else if(restPath.contains(RESTPROJECTSPATH)){
 					ArrayList<TeamCityProject> respXml = (ArrayList<TeamCityProject>) ParseXMLResponse(resp, new TCDataHandler());
 					new ShowProjectList().execute(respXml);
 				}else{
 					//gets you build name and status information
 					ArrayList<TeamCityBuilds> respXml = (ArrayList<TeamCityBuilds>) ParseXMLResponse(resp, new BuildsDataHandler());
 					new ShowBuildStatusList().execute(respXml);
 				}
 					
 			}catch (ClientProtocolException e){
 				e.printStackTrace();
 				showAlertDialog("Oh Oh! We are having trouble connecting to the server with those credentials! Please recheck your url, username and password.");		   
 			} catch (IOException e) {
 				e.printStackTrace();
 				showAlertDialog("Oh Oh! We are having trouble connecting to the server with those credentials! Please recheck your url, username and password.");
 			}
 		
 	}
 	
 	/** Parse response to get a list of available projects from the server
 	 * @return */
 	private ArrayList<?> ParseXMLResponse(StringBuilder response, IDataInterface myHandler){
 		 SAXParserFactory spf = SAXParserFactory.newInstance();
 		 try {
 			SAXParser sp = spf.newSAXParser();
 			XMLReader xr = sp.getXMLReader();
 			
 			xr.setContentHandler((ContentHandler) myHandler);
 			InputSource is = new InputSource();
 			is.setCharacterStream(new StringReader(response.toString()));
 			xr.parse(is);
 			
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return myHandler.getData();
 	}
 	
 	private void showAlertDialog(String message){
 		 AlertDialog alertDialog = new AlertDialog.Builder(TeamTrafficLightsActivity.this).create();
 		 alertDialog.setTitle("Erorr");
 		 alertDialog.setMessage(message);
 		 alertDialog.show();
 	}
 	
 	private class ShowProjectList extends AsyncTask<ArrayList<TeamCityProject>,Void,ListView>{
 		private ProgressDialog xx = ProgressDialog.show(TeamTrafficLightsActivity.this, "Fetching...", "Getting Data from TeamCity Server");
 		
 		@Override
 		protected ListView doInBackground(ArrayList<TeamCityProject>... params) {
 			// TODO Auto-generated method stub
 			
 			final ArrayList<TeamCityProject> projectList = params[0];
 			ArrayList<String> myList = new ArrayList<String>();
 			Iterator<TeamCityProject> iterator = projectList.iterator();
 			while(iterator.hasNext()){
 				myList.add(iterator.next().projectName);
 			}
 			if(myList.isEmpty())
 				myList.add("No Projects!");
 			
 	        final ListView lv = new ListView(TeamTrafficLightsActivity.this);
 	        lv.setAdapter(new ArrayAdapter<String>(TeamTrafficLightsActivity.this,android.R.layout.simple_list_item_1,myList));
 	        
 	        lv.setOnItemClickListener(new OnItemClickListener(){
 	        	@Override
 				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 					
 					getResponse(teamCityUrl.getText().toString().trim(),
 							teamCityUser.getText().toString().trim(),
 							teamCityPass.getText().toString(),
 							RESTBUILDTYPEPATH+projectList.get(pos).projectId);
 					
 				}
 	        	
 	        });
 	        
 	        return lv;
 		}
 		protected void onPreExecute(){
 			xx.show();
 		}
 		protected void onPostExecute(ListView lv){
 			xx.dismiss();
 			setContentView(lv);
 		}
 		
 	}
 	
 	private class ShowBuildNames extends AsyncTask<ArrayList<TeamCityBuilds>,Void,ListView>{
 		private ProgressDialog xx = ProgressDialog.show(TeamTrafficLightsActivity.this, "Fetching...", "Getting Data from TeamCity Server");
 		
 		
 		protected void onPreExecute(){
 			xx.show();
 		}
 		protected void onPostExecute(ListView lv){
 			xx.dismiss();
 			setContentView(lv);
 		}
 		@Override
 		protected ListView doInBackground(ArrayList<TeamCityBuilds>... params) {
 			
 			final ArrayList<TeamCityBuilds> projectList = params[0];
 			ArrayList<String> myList = new ArrayList<String>();
 			Iterator<TeamCityBuilds> iterator = projectList.iterator();
 			while(iterator.hasNext()){
 				myList.add(iterator.next().buildName);
 			}
 			if(myList.isEmpty())
 				myList.add("No Builds for Project!");
 			
 	        final ListView lv = new ListView(TeamTrafficLightsActivity.this);
 	        lv.setAdapter(new ArrayAdapter<String>(TeamTrafficLightsActivity.this,android.R.layout.simple_list_item_1,myList));
 	       
 	        lv.setOnItemClickListener(new OnItemClickListener(){
 	        	@Override
 				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 					getResponse(teamCityUrl.getText().toString().trim(),
 							teamCityUser.getText().toString().trim(),
 							teamCityPass.getText().toString(),
 							RESTBUILDSPATH+projectList.get(pos).buildId+"/builds?count=10");
 				}
 	        	
 	        });
 	        return lv;
 		}
 		
 	}
 	
 	
 	private class ShowBuildStatusList extends AsyncTask<ArrayList<TeamCityBuilds>,Void,ArrayList<TeamCityBuilds>>{
 		private ProgressDialog xx = ProgressDialog.show(TeamTrafficLightsActivity.this, "Fetching...", "Getting Data from TeamCity Server");
 		
 		protected void onPreExecute(){
 			xx.show();
 		}
 		protected void onPostExecute(ArrayList<TeamCityBuilds> projlist){
 			xx.dismiss();
 			setContentView(R.layout.buildstatuslist);
 			ListView myList = (ListView) findViewById(R.id.listView1);
 		
 			myList.setAdapter(new BuildStatusAdapter(TeamTrafficLightsActivity.this,projlist));
 			
 		}
 		@Override
 		protected ArrayList<TeamCityBuilds> doInBackground(ArrayList<TeamCityBuilds>... params) {
 			ArrayList<TeamCityBuilds> projectList = params[0];
 			int index = 0;
			
 			
 			for(TeamCityBuilds stats:projectList){
 				Date dateString;
 				try {
 					dateString = ISO8601DATEFORMAT.parse(stats.startDate);
 					stats.startDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(dateString);
 					
 					projectList.set(index, stats);
 					index+=1;
 					
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 
 			}
 			
 			return projectList;		
 		}
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		
         //Get user input data
         teamCityUrl = (EditText)findViewById(R.id.serverUrl);
         teamCityUser = (EditText)findViewById(R.id.user);
         teamCityPass = (EditText)findViewById(R.id.pass);
       		
         String storedCreds = retrieveCreds();
 		String [] storedCredsSplit= null;
 
 		if(storedCreds != null){
 			storedCredsSplit = storedCreds.split(",");
 			try {
 				decpass = SimpleCrypto.decrypt(SimpleCrypto.SEED, storedCredsSplit[2]);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			teamCityUrl.setText(storedCredsSplit[0].toString());
 			teamCityUser.setText(storedCredsSplit[1].toString());
 			teamCityPass.setText(decpass);
 			
 		}
 
		ISO8601DATEFORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.US);
 		
         Button connect = (Button)findViewById(R.id.Connect);
         connect.setOnClickListener(connectListener);
        
        //Warming up date parser.
        new Thread(new Runnable(){
        	public void run(){
        		try {
    				//Warming up date parsing
    				ISO8601DATEFORMAT.parse("20120416T174543-0400");
    			} catch (ParseException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	}
        }).start();
        
     }
 }
