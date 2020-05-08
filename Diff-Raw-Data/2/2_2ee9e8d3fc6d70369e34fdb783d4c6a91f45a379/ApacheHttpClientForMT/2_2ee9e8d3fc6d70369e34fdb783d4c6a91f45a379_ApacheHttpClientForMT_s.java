 /*===========================================================================
   Copyright (C) 2012 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.connectors.microsoft;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 public class ApacheHttpClientForMT {
 
 	private static final boolean USEHTTPCLIENT = true;
 	
 	public static String getAzureAccessToken(String sUral, 
 			String sClientID, String sEcret) {
 		if (USEHTTPCLIENT) {
 			return getExpensiveAzureAccessToken(sUral, sClientID, sEcret);
 		}
 		else {
 			return getCheapAzureAccessToken(sUral, sClientID, sEcret);
 		}
 	}
 	
 	public static String getExpensiveAzureAccessToken(String sUral, 
 			String sClientID, String sEcret) {
 		String sResult=null;
 		HttpClient client=null;
 		UrlEncodedFormEntity uefe;
 		
 		try {
 			client = new DefaultHttpClient();
 			HttpPost post = new HttpPost(sUral);
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
 			nameValuePairs.add(new BasicNameValuePair("grant_type", "client_credentials"));
 			nameValuePairs.add(new BasicNameValuePair("client_id", sClientID));
 			nameValuePairs.add(new BasicNameValuePair("client_secret", sEcret));
 			nameValuePairs.add(new BasicNameValuePair("scope", "http://api.microsofttranslator.com"));
 			uefe = new UrlEncodedFormEntity(nameValuePairs);
 			sResult = fromInputStreamToString(uefe.getContent(),"UTF-8");
 			sResult = "";
 			post.setEntity(uefe);
 			HttpResponse response = client.execute(post);
 			if ( response != null ) {
 				BufferedReader rd = new BufferedReader(new InputStreamReader(
 						response.getEntity().getContent()));
 				String line = "";
 				while ((line = rd.readLine()) != null) {
 					sResult += line;
 				}
 			}
 		}
 		catch ( Exception e ) {
 			int i = 1;
 			i = i + 1;
 		}
 		return sResult;
 	}
 
 	public static String getCheapAzureAccessToken (String sUral, 
 		String sClientID,
 		String sEcret)
 	{
 		String sResult=null;
 //		String sStuff=null;
 //		String sAddress;
 		String sContent;
 		HttpURLConnection conn;
 //		sAddress = String.format(sUral);
 		URL url;
 		try {
 			sContent = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s&scope=%s",
				sClientID,URLEncoder.encode(sEcret),"http://api.microsofttranslator.com");
 			url = new URL(sUral);
 			conn = (HttpURLConnection)url.openConnection();
 			conn.addRequestProperty("Content-Type", "text/xml");
 //			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-1");
 			conn.setRequestMethod("POST");
 			conn.setDoOutput(true);
 		    conn.setDoInput(true);   
 			OutputStreamWriter osw = null;
 //			osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
 			osw = new OutputStreamWriter(conn.getOutputStream());
 			osw.write(sContent);
 			int code = conn.getResponseCode();
 			if (code==200)
 				sResult = fromInputStreamToString(conn.getInputStream(), "UTF-8");
 		}
 		catch ( MalformedURLException e ) {
 			return sResult;
 		}
 		catch ( UnsupportedEncodingException e ) {
 			return sResult;
 		}
 		catch ( IOException e ) {
 			return sResult;
 		}
 		return sResult;
 	}
 
 	static private String fromInputStreamToString (InputStream stream,
 		String encoding)
 		throws IOException
 	{
 		BufferedReader br = new BufferedReader(new InputStreamReader(stream, encoding));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		while ( (line = br.readLine()) != null ) {
 			sb.append(line + "\n");
 		}
 		br.close();
 		return sb.toString();
 	}
 
 }
