 package com.blastic.clases;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.lang.reflect.Array;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.blastic.adapters.JsonAdapter;
 import com.blastic.pawhub_petmatch.Kind;
 import android.os.AsyncTask;
 
 public class GetTypedTask<T> extends AsyncTask<String, Void, ArrayList<T>> {
	private T tipo;
 	@Override
 	protected ArrayList<T> doInBackground(String... params) {
 		int count = params.length;
 
         ArrayList<T> objects =  new ArrayList<T>();	
 		if(count>0){
 			URL url;
 			try {
 				url = new URL(params[0]);
 		        URLConnection urlc=url.openConnection();
 		        BufferedReader bfr=new BufferedReader(new InputStreamReader(urlc.getInputStream()));
 		        String line;
 		        while((line=bfr.readLine())!=null)
 		        {
 			        JSONArray jsa=new JSONArray(line);
 		        	//ArrayAdapter<String> arrayAdapter = new			        
 			        for(int i=0;i<jsa.length();i++)
 		           {		        	
 			           JSONObject jsonObject =(JSONObject)jsa.get(i);
			           T o = JsonAdapter.bindJsonToClass(T.class, jsonObject);
 			           objects.add(o);
 		           }
 		        }
 		        return objects;
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return objects;
 	}
 
 	protected void onPostExecuted(Kind kind){
 	
 	}
 }
