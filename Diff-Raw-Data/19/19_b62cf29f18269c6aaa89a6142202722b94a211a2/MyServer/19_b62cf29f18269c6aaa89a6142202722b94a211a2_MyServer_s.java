 package com.tang.DownLoadPage;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import android.content.Context;
 import android.util.Log;
 
 public class MyServer
 {
 	public static String sendPostData(String sendMsg)
 	{
 		
 		String url = "";
 		String postKey = "";
 		String postValue = "";
 		
 		
 		if(sendMsg.equals("scripts"))
 		{
 			url = "http://tang.byethost6.com/android/scriptsToJson.php";	
 			postKey = "scripts";
 			postValue = "MyScripts";
 		}
 		else
 		{
 			String [] split= sendMsg.split("/");
 			if(!split[0].equals("scripts")) 
 			{ return null; }
 			else
 			{
 			   url = "http://tang.byethost6.com/android/scriptContextToJson.php";	
 			   postKey = "scriptID";
 			   postValue = split[1];
 			}
 		
 		}
 		
 		
 		//Http Connection
 		HttpPost httpRequest = new HttpPost(url);
 		
 		//Http Request
 		try {
 			
 			ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
 			param.add(new BasicNameValuePair(postKey,postValue));
 			httpRequest.setEntity(new UrlEncodedFormEntity(param,HTTP.UTF_8));
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		//Http Response
 		HttpResponse httpResponse;
 		try {
 			httpResponse = new DefaultHttpClient().execute(httpRequest);
 			
 			
 			if(httpResponse.getStatusLine().getStatusCode() == 200)
 			{
 				String result = EntityUtils.toString(httpResponse.getEntity());
 				return result;
 			}
 		
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	    return null;
 
    }
 
 	
 public static void writeInFile(Context context,String fileName, String input)
 {
 	try {
 		  FileOutputStream outFile = context.openFileOutput(fileName,Context.MODE_PRIVATE);
 		  outFile.write(input.getBytes());
		  Log.e("Server","JSON = "+input);
 		  outFile.close();
 		  
 	} catch (FileNotFoundException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	
 }
 
 
 public static String readFromFile(Context context, String fileName)
 {
 	String contentStr = null;
 	
 	try {
 		FileInputStream inFile = context.openFileInput(fileName);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inFile));
 		StringBuilder builder = new StringBuilder();
 		String line = null;
 		while( (line = reader.readLine())!= null)
 		{
 			builder.append(line);
 		}
 		
 		contentStr = builder.toString();
 		
 	
 	} catch (FileNotFoundException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	
 	
 	return contentStr;
 	
 }
 
 
 
 
 
 
 }
