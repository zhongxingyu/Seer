 package org.fourdnest.androidclient.comm;
 
 import android.util.Log;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.DateUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.NameValuePair;
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class FourDNestProtocol implements Protocol {
 	private static final String TAG = "FourDNestProtocol";
 	private static final String EGG_UPLOAD_PATH = "v1/egg/upload/";
 	private Nest nest;
 
 	/**
 	 * Parses egg's content and sends it in multipart mime format with HTTP post.
 	 * 
 	 * @return HTTP status code and egg URI on server if creation successful
 	 **/
 	public String sendEgg(Egg egg) {
 		HttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost(this.nest.getBaseURL()
 				+ EGG_UPLOAD_PATH);
 
 		//Create list of NameValuePairs
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		pairs.add(new BasicNameValuePair("caption", egg.getCaption()));
 		//FIXME: Check for null file path.
 		pairs.add(new BasicNameValuePair("file", egg.getLocalFileURI().getPath()));
 		// FIXME: Add tags later
 		int status = 0;
 		try {
 			post.setEntity(this.createEntity(pairs));
 			Date date = new Date();
			date.setDate(8);
 			post.setHeader("Date", DateUtils.formatDate(date));
 			Log.d("firstDate", post.getHeaders("Date")[0].getValue());
 			HttpResponse response = client.execute(post);
 			Log.d("secondDate", post.getHeaders("Date")[0].getValue());
 			status = response.getStatusLine().getStatusCode();
			if (status == 201) {
 				return status + " "
 						+ response.getHeaders("Location")[0].getValue();
 			}
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "ClientProtocolException, egg not sent " + e.getMessage());
 			return "0";
 		} catch (IOException e) {
 			Log.e(TAG, "IOException, egg not sent " + e.getMessage());
 			return "0";
 		}
 		return String.valueOf(status);
 	}
 
 	/** Creates the MultipartEntity from name-value -pair list 
 	 * @throws UnsupportedEncodingException */
 	private MultipartEntity createEntity(List<NameValuePair> pairs) throws UnsupportedEncodingException {
 		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);
 
 		for (int i = 0; i < pairs.size(); i++) {
 			File file = new File(pairs.get(i).getValue());
 			if (pairs.get(i).getName().equalsIgnoreCase("file")) {
 				entity.addPart(pairs.get(i).getName(), new FileBody(file));
 			} else {
 				entity.addPart(pairs.get(i).getName(), new StringBody(pairs
 						.get(i).getValue()));
 			}
 		}
 
 		return entity;
 	}
 
 	public List<Tag> topTags(int count) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getTest() {
 		try {
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet();
 			request.setURI(new URI("http://hs.fi/index.html"));
 			HttpResponse response = client.execute(request);
 			InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
 			BufferedReader in = new BufferedReader(inputStreamReader);
 			StringBuffer sb = new StringBuffer("");
 			String line = "";
 			String newLine = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null) {
 				sb.append(line);
 				sb.append(newLine);
 			}
 			in.close();
 			String page = sb.toString();
 			return page;
 		} catch(URISyntaxException e) {
 			Log.e(TAG, e.getMessage());
 		} catch(IOException e) {
 			Log.e(TAG, e.getMessage());
 		} finally {
 			//FIXME: Closing of the readers.
 		}
 		return null;
 
 
 
 
 	}
 
 	public void setNest(Nest nest) {
 		this.nest = nest;
 
 	}
 }
