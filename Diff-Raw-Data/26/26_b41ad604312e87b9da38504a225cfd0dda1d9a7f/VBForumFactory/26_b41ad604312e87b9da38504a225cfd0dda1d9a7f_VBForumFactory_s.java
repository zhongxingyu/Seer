 package com.normalexception.forum.rx8club.utils;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import android.util.Log;
 import android.widget.Toast;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.normalexception.forum.rx8club.MainApplication;
 import com.normalexception.forum.rx8club.WebUrls;
 import com.normalexception.forum.rx8club.activities.ForumBaseActivity;
 
 /**
  * Classes that pertain to a VB type forum
  */
 public class VBForumFactory {
 	
 	private static VBForumFactory _instance = null;
 	private static final String TAG = "Application:VBForumFactory";
 		
 	/**
 	 * Constructor
 	 */
 	protected VBForumFactory() {
 		Log.v(TAG, "Creating Forum Factory");
 	}
 	
 	/**
 	 * Grab an instance of the VBForumFactory, if one doesn't exist
 	 * then create one
 	 * @return	An instance of the VBForumFactory
 	 */
 	public static VBForumFactory getInstance() {
 		if(_instance == null)
 			_instance = new VBForumFactory();
 		return _instance;
 	}
 	
 	/**
 	 * Return the root address of the forum
 	 * @return	The root address of the forum
 	 */
 	public static String getRootAddress() {
 		return WebUrls.rootUrl;
 	}
 
 	/**
 	 * Get the frontpage for the forum
 	 * @param src   The source activity
 	 * @param lf	The login factory object
 	 * @return		The output text for the frontpage
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public String getForumFrontpage(ForumBaseActivity src, LoginFactory lf) 
 			throws ClientProtocolException, IOException {
 		return getForumPage(src, lf, WebUrls.rootUrl);
 	}
 	
 	/**
 	 * Get the page context from from a supplied forum address
 	 * @param src   The source activity
 	 * @param lf	The login factory object
 	 * @param addr	The address to grab information from
 	 * @return		The output text for the page
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public String getForumPage(ForumBaseActivity src, LoginFactory lf, String addr) 
 			throws ClientProtocolException, IOException {
 		DefaultHttpClient client = null;
 		String output = null;
 		
 		// Grab the login client
 		try {
 			client = lf.getClient();
 			if(client == null) {
 				Log.w(TAG,"HTTPClient was null!");
 				
 				// Try logging in again
 				lf.login();
 				
 				client = lf.getClient();
 			}
 		} catch (Exception e) {
 			BugSenseHandler.sendException(e);
 		}
 		
 		// If client isn't null, continue
 		if(client != null && (addr != null && !addr.equals(""))) {
 			HttpGet httpost = null;
 			
 			try {
 				httpost = new HttpGet(addr);
 			} catch (IllegalStateException e) {
 				BugSenseHandler.sendExceptionMessage("Address", addr, e);
 				
 				// Sometimes we pass an address with no host, fix
 				// that issue here
 				addr = addr.startsWith("/")? addr : "/" + addr;
 				httpost = new HttpGet(getRootAddress() + addr);			
 			}
 			
 			try {
 		    	HttpResponse response = client.execute(httpost);
 		    	HttpEntity entity = response.getEntity();
 		    	
 		    	// Get login results (in this case the forum frontpage0
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						entity.getContent()));
 				
 				StringBuilder sb = new StringBuilder();
 				
 				String inputLine; 
 				while ((inputLine = in.readLine()) != null) 
 					sb.append(inputLine);
 				
 				output = sb.toString();
 				
 				in.close();	
 				
 				entity.consumeContent();
 				
 				if(output == null || 
 						output.equals("") || 
 						output.contains("You are not logged in"))
					src.returnToLoginPage();
 			} catch (NullPointerException e) {
 				
 				src.runOnUiThread(new Runnable() {
 					  public void run() {
 						  Toast.makeText(MainApplication.getAppContext(), 
 								  "Error Opening Page. This Has Been Logged", 
 								  Toast.LENGTH_SHORT).show();
 					  }
 				});
 				
 				BugSenseHandler.sendException(e);
 			}
 			
 		} else {
 			src.runOnUiThread(new Runnable() {
 				  public void run() {
 					Toast.makeText(MainApplication.getAppContext(),
 							"Error With Credentials",
 							Toast.LENGTH_SHORT).show();
 				  }
 			});
 		}
 		
 		return output;
 	}
 	
 	/**
      * Grab the forum as a jsoup document
      * @return	A jsoup document object that contains the 
      * 			forum contents
      */
     public Document get(ForumBaseActivity src, String addr) {  
     	LoginFactory lf = LoginFactory.getInstance();
     	
     	String output = "";	
 		try {
 			VBForumFactory ff = VBForumFactory.getInstance();
 			output = ff.getForumPage(src, lf, addr);
 			return Jsoup.parse(output);
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "Error grabbing category page: " + e.getMessage());
 		} catch (IOException e) {
 			Log.e(TAG, "Error grabbing category page: " + e.getMessage());
 		}
 		
 	   	return null;
     }
 }
