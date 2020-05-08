 package com.bettername.thepokemonone.data;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 
 import com.bettername.thepokemonone.model.Player;
 import com.google.gson.Gson;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class CreateUser {
 
 	public void createUser(Player user) {
 
 		String jsonString = new Gson().toJson(user);
 		System.out.println(jsonString);
 		RequestParams rp = new RequestParams();
 		rp.put("player", jsonString);
 		PokeServClient.post("players", rp,
 				new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray timeline) {
 				// Pull out the first event on the public timeline
 				JSONObject firstEvent = null;
 				try {
 					firstEvent = (JSONObject) timeline.get(0);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String tweetText = null;
 				try {
 					tweetText = firstEvent.getString("text");
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// Do something with the response
 				System.out.println(tweetText);
 			}
 		});
 
 		// HttpAsyncClient httpclient = null;
 		// try {
 		// httpclient = new DefaultHttpAsyncClient();
 		// httpclient.start();
 		// HttpGet request = new HttpGet("http://www.apache.org/");
 		// Future<HttpResponse> future = httpclient.execute(request, null);
 		// HttpResponse response = future.get();
 		// System.out.println("Response: " + response.getStatusLine());
 		// System.out.println("Shutting down");
 		// httpclient.shutdown();
 		// System.out.println("Done");
 		// } catch (IOReactorException e) {
 		// e.printStackTrace();
 		// } catch (InterruptedException e) {
 		// e.printStackTrace();
 		// } catch (ExecutionException e) {
 		// e.printStackTrace();
 		// } finally {
 		// System.out.println("error");
 		// }
 
 		// RequestTask rt = new RequestTask();
 		// rt.execute("http://www.apache.org");
 
 	}
 
 	class RequestTask extends AsyncTask<String, String, String> {
 
 		@Override
 		protected String doInBackground(String... uri) {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpResponse response;
 			String responseString = null;
 			try {
 				response = httpclient.execute(new HttpGet(uri[0]));
 				StatusLine statusLine = response.getStatusLine();
 				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
 					ByteArrayOutputStream out = new ByteArrayOutputStream();
 					response.getEntity().writeTo(out);
 					out.close();
 					responseString = out.toString();
 				} else {
 					// Closes the connection.
 					response.getEntity().getContent().close();
 					throw new IOException(statusLine.getReasonPhrase());
 				}
 			} catch (ClientProtocolException e) {
 				// TODO Handle problems..
 			} catch (IOException e) {
 				// TODO Handle problems..
 			}
 			return responseString;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			// Do anything with response..
 			System.out.println(result);
 		}
 	}
 
 	class RequestTask2 extends AsyncTask<String, String, String> {
 
 		@Override
 		protected String doInBackground(String... uri) {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpResponse response;
 			String responseString = null;
 			try {
 				HttpPost p = new HttpPost();
 				response = httpclient.execute(new HttpGet(uri[0]));
 				httpclient.execute(new HttpGet(uri[0]));
 
 				StatusLine statusLine = response.getStatusLine();
 				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
 					ByteArrayOutputStream out = new ByteArrayOutputStream();
 					response.getEntity().writeTo(out);
 					out.close();
 					responseString = out.toString();
 				} else {
 					// Closes the connection.
 					response.getEntity().getContent().close();
 					throw new IOException(statusLine.getReasonPhrase());
 				}
 			} catch (ClientProtocolException e) {
 				// TODO Handle problems..
 			} catch (IOException e) {
 				// TODO Handle problems..
 			}
 			return responseString;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			// Do anything with response..
 			System.out.println(result);
 		}
 	}
 
 }
