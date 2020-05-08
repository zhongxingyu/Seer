 //sg
     /*
     "Copyright 2011 Aman Madaan <madaan.amanmadaan@gmail.com>"
     
      This file is part of Tarang.
 
     Tarang is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Tarang is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Tarang.  If not, see <http://www.gnu.org/licenses/>.*/
 
 package com.bvp.miniproject;
 
 import java.io.InputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.util.Log;
 
 public class ConfigureApplication {
 	private String messageGroup;
 	private HttpClient httpclient;
 	private HttpGet httpget ;
 	private String url;
 	private StringBuffer result;
 	private String password;
 	private  HttpResponse response ;
 	private HttpEntity entity;
 	boolean phpComplete=true;
 	ConfigureApplication(String mG,String pass)
 	{
 		this.messageGroup=mG;
 		this.password=pass;
 		 url="http://192.168.1.33/ConfigureTarang.php?messageGroup="+messageGroup+"&password="+password;
 		 httpclient = new DefaultHttpClient();
 		 httpget = new HttpGet(url);
          result=new StringBuffer(5);
 		
 	}
 	public boolean sendGetToServer()
 	{
 		int i=0;
 		if(phpComplete)  
 		{
 		try
 		{
 		char temp; 
        /* EXECUTE THE GET REQUEST TO SERVER*/
 		
 		response= httpclient.execute(httpget);
        /* GET THE RESPONSE FROM THE SERVER */
 		entity = response.getEntity();
 		Log.d("here","here");
 		if (entity != null) {
 			/* GET INPUT STREAM */
 		    InputStream instream = entity.getContent();
 		    while(((temp=(char)instream.read())!=-1)&&(i<5))
 		    {
 		    	i++;
 		       result.append(temp);	
 		       
 		       
 		    }
 	    }
 		Log.d("result:",result.toString());
 		/**
 		 * parse the result
 		 */
 		Boolean b;
 	       
 		if(result.toString().charAt(0)=='t')
 		{b=true;}
 		else
 			b=false;
 		Log.d("got:",b.toString());
 			
 		return b;
 		}
 		catch(Exception e)
 		{
 			Log.d("IN GET TO SERVER", e.toString());
 		}
 		finally  //finally 
 		{
 		    httpget.abort();
 		}
 		return false;
 		}
 		else
 		{
 		return true;	
 		}
 	}
 	
 
 }
