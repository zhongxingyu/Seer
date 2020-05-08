 package com.normalexception.forum.rx8club.html;
 
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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import ch.boye.httpclientandroidlib.HttpEntity;
 import ch.boye.httpclientandroidlib.HttpResponse;
 import ch.boye.httpclientandroidlib.NameValuePair;
 import ch.boye.httpclientandroidlib.StatusLine;
 import ch.boye.httpclientandroidlib.client.ClientProtocolException;
 import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
 import ch.boye.httpclientandroidlib.client.methods.HttpGet;
 import ch.boye.httpclientandroidlib.client.methods.HttpPost;
 import ch.boye.httpclientandroidlib.client.methods.HttpUriRequest;
 import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
 import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
 import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
 import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
 import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
 import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
 import ch.boye.httpclientandroidlib.protocol.ExecutionContext;
 import ch.boye.httpclientandroidlib.protocol.HttpContext;
 import ch.boye.httpclientandroidlib.util.EntityUtils;
 
 import com.normalexception.forum.rx8club.Log;
 import com.normalexception.forum.rx8club.WebUrls;
 import com.normalexception.forum.rx8club.enums.VBulletinKeys;
 import com.normalexception.forum.rx8club.httpclient.ClientUtils;
 import com.normalexception.forum.rx8club.user.UserProfile;
 import com.normalexception.forum.rx8club.utils.Utils;
 
 public class HtmlFormUtils {	
 	private static String responseUrl = "";
 	private static String TAG =HtmlFormUtils.class.getName();
 
 	/**
 	 * Submit a form and its contents
 	 * @param url	The url to submit the form to
 	 * @param nvps	The name value pair of form contents
 	 * @return		True if it worked
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	private static boolean formSubmit(String url, List<NameValuePair> nvps) 
 			throws ClientProtocolException, IOException {
 		return formSubmit(url, nvps, false);
 	}
 	
 	/**
 	 * Submit a form and its contents
 	 * @param url		 The url to submit the form to
 	 * @param nvps  	 The name value pair of the contents
 	 * @param attachment If true, add attachment headers
 	 * @return           True if it worked
 	 * @throws ClientProtocolExecption
 	 * @throws IOException
 	 */
 	private static boolean formSubmit(String url, List<NameValuePair> nvps, 
 			boolean attachment)
 		throws ClientProtocolException, IOException {
 		DefaultHttpClient httpclient = LoginFactory.getInstance().getClient();
 		
 		HttpPost httpost = ClientUtils.getHttpPost(url);	
 		Log.d(TAG, "[Submit] Submit URL: " + url);
 		
 		// If there is an attachment, we need to add some data
 		// to the post header
 		if(attachment) {
 			String pN = "";
 			for(NameValuePair nvp : nvps) {
 				if(nvp.getName().equals(VBulletinKeys.PostNumber.getValue())) {
 					pN = nvp.getValue(); break;
 				}
 			}	
 
 			httpost.setHeader("Content-Type", "application/x-www-form-urlencoded");
 			httpost.setHeader("Referer", WebUrls.postSubmitAddress + pN);		
 		}
 		
 	    httpost.setEntity(new UrlEncodedFormEntity(nvps));
 
     	HttpContext context   = LoginFactory.getInstance().getHttpContext();
     	HttpResponse response = httpclient.execute(httpost, context);
     	HttpEntity entity     = response.getEntity();
     	StatusLine statusLine = response.getStatusLine();
     	
     	Log.d(TAG, "[Submit] Status: " + statusLine.getStatusCode());
     	if (entity != null) {
     		httpost.releaseConnection();
     		
     		HttpUriRequest request = (HttpUriRequest) context.getAttribute(
     		        ExecutionContext.HTTP_REQUEST);
 
     		responseUrl = request.getURI().toString();
     		Log.d(TAG, "[Submit] Response URL: " + responseUrl);
     		
     		return true;
     	}
     	
 		return false;
 	}
 	
 	/**
 	 * Update user profile
 	 * @param token		 The users security token	
 	 * @param title		 The user title
 	 * @param homepage	 The users homepage
 	 * @param bio		 The users bio
 	 * @param location	 The users location
 	 * @param interests  The users interests
 	 * @param occupation The users occupation
 	 * @return			 True if success
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean updateProfile(String token, String title, String homepage, String bio, 
 					   					String location, String interests, String occupation) 
 		throws ClientProtocolException, IOException {
 
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair(VBulletinKeys.LoggedInUser.getValue(), UserProfile.getInstance().getUserId()));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), token));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(), "updateprofile"));
 		nvps.add(new BasicNameValuePair("customtext", title));
 		nvps.add(new BasicNameValuePair("homepage", homepage));
 		nvps.add(new BasicNameValuePair("userfield[field1]", bio));
 		nvps.add(new BasicNameValuePair("userfield[field2]", location));
 		nvps.add(new BasicNameValuePair("userfield[field3]", interests));
 		nvps.add(new BasicNameValuePair("userfield[field4]", occupation));
 		
 		return formSubmit(WebUrls.profileUrl, nvps);
     
 	}
 	
 	/**
 	 * Delete private message
 	 * @param VBulletinKeys.SecurityToken.getValue()	Users security token
 	 * @param pmid			The private message id number
 	 * @return				True if success
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean deletePM(String securitytoken, String pmid) 
 			throws ClientProtocolException, IOException {
 
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair(VBulletinKeys.LoggedInUser.getValue(), UserProfile.getInstance().getUserId()));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), securitytoken));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(), "managepm"));
     	nvps.add(new BasicNameValuePair("dowhat", "delete"));
     	nvps.add(new BasicNameValuePair("pm[" + pmid + "]","0_today"));
     	
     	return formSubmit(WebUrls.pmInboxUrl, nvps);
 	}
 	
 	/**
 	 * Convenience method to send a private message
 	 * @param doType		Submit type
 	 * @param VBulletinKeys.SecurityToken.getValue() User's security token	
 	 * @param post			The text from the PM
 	 * @param subject		The subject of the PM
 	 * @param recips		The recipient of the PM
 	 * @param pmid			The pm id number
 	 * @return				True if the submit worked
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean submitPM(String doType, String securitytoken, 
 			                String post, String subject, String recips, String pmid) 
 			throws ClientProtocolException, IOException {
 
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Message.getValue(), post));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.LoggedInUser.getValue(), UserProfile.getInstance().getUserId()));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), securitytoken));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(), doType));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Recipients.getValue(), recips));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Title.getValue(), subject));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.PMId.getValue(), pmid));
     	
     	return formSubmit(WebUrls.pmSubmitAddress + pmid, nvps);
 	}
 	
 	/**
 	 * Submit a post to the server
 	 * @param VBulletinKeys.SecurityToken.getValue()	The posting security token
 	 * @param thread		The thread number
 	 * @param postNumber	The post number
 	 * @param post			The actual post
 	 * @return				True if the post was successful
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean submitPost(String doType, String securitytoken, String thread, 
 							String postNumber, String attId, String post) 
 			throws ClientProtocolException, IOException {
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		
 		/**
 		 * Disabled for now since we arent supporting attachments
 		 * from the phone
 		 * 
 		 * if(attId != null) {
 		 *	post += "\n\n[ATTACH]" + attId + "[/ATTACH]";
 		 * }
 		 */
 		
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Message.getValue(), post));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.ThreadId.getValue(), thread));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.PostNumber.getValue(), postNumber));
 		nvps.add(new BasicNameValuePair("specifiedpost", "0"));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.LoggedInUser.getValue(), UserProfile.getInstance().getUserId()));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), securitytoken));
 		nvps.add(new BasicNameValuePair("parseurl", "1"));
 		nvps.add(new BasicNameValuePair("parseame", "1"));
 		nvps.add(new BasicNameValuePair("parseame_check", "1"));
 		nvps.add(new BasicNameValuePair("vbseo_retrtitle", "1"));
 		nvps.add(new BasicNameValuePair("vbseo_is_retrtitle", "1"));
		nvps.add(new BasicNameValuePair("emailupdate", "9999"));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(), doType));
     	
     	return formSubmit(WebUrls.quickPostAddress + thread, nvps, attId != null);
 	}
 	
 	/**
 	 * Submit a post edit
 	 * @param VBulletinKeys.SecurityToken.getValue()	The security token of the posting
 	 * @param postNumber	The post number being edited
 	 * @param posthash		The post hash number
 	 * @param poststart		The post start time
 	 * @param post			The post text
 	 * @return				True if submit successful
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean submitEdit(String securitytoken, String postNumber, 
 							  String posthash, String poststart, String post) 
 			throws ClientProtocolException, IOException {
     	
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Message.getValue(), post));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.PostNumber.getValue(), postNumber));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), securitytoken));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(),"updatepost"));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.PostHash.getValue(), posthash));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.PostStartTime.getValue(), poststart));
  
     	return formSubmit(WebUrls.updatePostAddress + postNumber, nvps);
 	}
 	
 	/**
 	 * Submit a request to the server to delete the post
 	 * @param VBulletinKeys.SecurityToken.getValue()	The session security token
 	 * @param postNum		The post number to delete
 	 * @return				True if delete successful
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean submitDelete(String securitytoken, String postId)
 		throws ClientProtocolException, IOException {
 
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair(VBulletinKeys.PostId.getValue(), postId));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), securitytoken));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(),"deletepost"));
     	nvps.add(new BasicNameValuePair(VBulletinKeys.PostDelete.getValue(), "delete"));
  
     	return formSubmit(WebUrls.deletePostAddress + postId, nvps);
 	}
 	
 	/**
 	 * Submit a request to upload an attachment to the server
 	 * @param VBulletinKeys.SecurityToken.getValue()
 	 * @param bmapList
 	 * @param postnum
 	 * @return
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static String submitAttachment(String securitytoken, List<String> bmapList, 
 			String postnum) throws ClientProtocolException, IOException {
 		DefaultHttpClient httpclient = 
 				LoginFactory.getInstance().getClient();
 		
 		String posthash = "", t = "", p = "", attId = "", poststarttime = "";
 		
 		// Ok, the first thing we need to do is to grab some token and hash
 		// information.  This information isn't displayed on the thread page, 
 		// but instead on the thread's full reply page.  Lets GET the page,
 		// and then grab the input params we need.
 		HttpGet httpGet = 
 				ClientUtils.getHttpGet(WebUrls.postSubmitAddress + postnum);
 		HttpResponse respo = httpclient.execute(httpGet,
 				LoginFactory.getInstance().getHttpContext());
 		HttpEntity respoEnt = respo.getEntity();
 		if(respoEnt != null) {
 			String output = 
 	    		EntityUtils.toString(respoEnt, "UTF-8" );
 			
 			//entity.consumeContent();
 			httpGet.releaseConnection();
 			
 			Document doc = Jsoup.parse(output);
 			posthash = HtmlFormUtils.getInputElementValueByName(doc, VBulletinKeys.PostHash.getValue());
 			t = HtmlFormUtils.getInputElementValueByName(doc, VBulletinKeys.ThreadId.getValue());
 			p = HtmlFormUtils.getInputElementValueByName(doc, VBulletinKeys.PostNumber.getValue());
 			poststarttime = HtmlFormUtils.getInputElementValueByName(doc, VBulletinKeys.PostStartTime.getValue());
     	}
 		
 		// We need to make sure that we got all of the information before
 		// proceeding, or else the attachment will not work
 		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 		entity.addPart("s", new StringBody(""));
 		entity.addPart(VBulletinKeys.SecurityToken.getValue(), new StringBody(VBulletinKeys.SecurityToken.getValue()));
 		entity.addPart(VBulletinKeys.Do.getValue(), new StringBody("manageattach"));
 		entity.addPart(VBulletinKeys.ThreadId.getValue(), new StringBody(t));
 		entity.addPart(VBulletinKeys.ForumId.getValue(), new StringBody("6"));
 		entity.addPart(VBulletinKeys.PostNumber.getValue(), new StringBody(p));
 		entity.addPart(VBulletinKeys.PostStartTime.getValue(), new StringBody(poststarttime));
 		entity.addPart("editpost", new StringBody("0"));
 		entity.addPart(VBulletinKeys.PostHash.getValue(), new StringBody(posthash));
 		entity.addPart("MAX_FILE_SIZE", new StringBody("2097152"));
 		entity.addPart("upload", new StringBody("Upload"));
 		entity.addPart("attachmenturl[]", new StringBody(""));
 		
 		for(String bmap : bmapList) {
 			File fileToUpload = new File(bmap);
 			FileBody fileBody = new FileBody(fileToUpload, "image/png");
 			entity.addPart("attachment[]", fileBody);
 		}
 
 		// Now post the page parameters.  This will go ahead and attach the
 		// image to our profile.
 		HttpPost httpPost = 
 				ClientUtils.getHttpPost(WebUrls.postAttachmentAddress);
 		httpPost.setEntity(entity);
 		HttpResponse response = 
 				httpclient.execute(httpPost, 
 						LoginFactory.getInstance().getHttpContext());
 		
 		// Read our response
 		HttpEntity ent = response.getEntity();
 		if(ent != null) {
 			String output = 
 	    			EntityUtils.toString(ent, "UTF-8" );
 	    	Document doc = Jsoup.parse(output);
 	    	String attachment = 
 	    			doc.select("a[href^=attachment.php?attachmentid]").first().attr("href");
 			
 			if(attachment != null) {
 				final String attStr = "attachmentid=";
 				final String ampStr = "&";
 				attId = attachment.substring(attachment.indexOf(attStr));
 				attId = attId.substring(attStr.length(), attId.indexOf(ampStr));
 			}
 			
 			httpPost.releaseConnection();
     	}
 		
 		////////////////////////////////////////////////////////////////////
 		// NOTE: Now we have an issue that so far I am unable to emulate
 		//       in Java.  What this has done, is upload an attachment to
 		//       the server, but, it only exists in our profile.  The attachment
 		//       hasn't been registered to a thread.  You can confirm this
 		//       by going to the user control panel on the forum, click the
 		//       attachments section, and you will see the attachment that
 		//       says "in progress".
 		////////////////////////////////////////////////////////////////////
 		
 		return attId;
 	}
 	
 	/**
 	 * Report the contents of the post that we are intending
 	 * on editing
 	 * @param VBulletinKeys.SecurityToken.getValue()	The security token of the session
 	 * @return				The response page 
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static Document getEditPostPage(String securitytoken, String postid) 
 			throws ClientProtocolException, IOException {
 		String output = "";
 		
 		DefaultHttpClient httpclient = 
 				LoginFactory.getInstance().getClient();
 		
 		HttpPost httpost = ClientUtils.getHttpPost(WebUrls.editPostAddress + postid);
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
     	nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), securitytoken));
     	
     	httpost.setEntity(new UrlEncodedFormEntity(nvps));
 
     	HttpResponse response = 
     			httpclient.execute(httpost, LoginFactory.getInstance().getHttpContext());
     	HttpEntity entity = response.getEntity();
     	
     	if(entity != null) {
     		output = 
 	    			EntityUtils.toString(entity, "UTF-8" );
     	}
 		
 		return Jsoup.parse(output);
 	}
 	
 	/**
 	 * Submit a new thread to the server
 	 * @param forumId	The category id
 	 * @param s			? not sure ?
 	 * @param token		The security token
 	 * @param posthash	The post hash code
 	 * @param subject	The user defined subject
 	 * @param post		The user defined initial post
 	 * @return			True if successful
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static boolean newThread(String forumId, String s, String token,
 							 String posthash, String subject, String post) 
 			throws ClientProtocolException, IOException {
 
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair("s", s));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.SecurityToken.getValue(), token));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.ForumId.getValue(), forumId));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Do.getValue(), "postthread"));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.PostHash.getValue(), posthash));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.PostStartTime.getValue(), Long.toString(Utils.getTime())));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Subject.getValue(), subject));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.Message.getValue(), post));
 		nvps.add(new BasicNameValuePair(VBulletinKeys.LoggedInUser.getValue(), UserProfile.getInstance().getUserId()));
 		
 		return formSubmit(WebUrls.newThreadAddress + forumId, nvps);
 	}
 	
 	/**
 	 * Report the response url
 	 * @return	The response url
 	 */
 	public static String getResponseUrl() {
 		return WebUrls.rootUrl + responseUrl;
 	}
 
     /**
      * Report the value inside of an input element
      * @param pan	The panel where all of the input elements reside
      * @param name	The name of the input to get the value for
      * @return		The string value of the input
      */
     public static String getInputElementValueByName(Document pan, String name) {
     	try {
     		return pan.select("input[name=" + name + "]").attr("value");
     	} catch (NullPointerException npe) {
     		return "";
     	}
     }
     
     /**
      * Report the value inside of an input element
      * @param pan	The panel where all of the input elements reside
      * @param name	The name of the input to get the value for
      * @return		The string value of the input
      */
     public static String getInputElementValueById(Document pan, String name) {
     	try {
     		return pan.select("input[id=" + name + "]").attr("value");
     	} catch (NullPointerException npe) {
     		return "";
     	}
     }
 }
