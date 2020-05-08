 package com.hoos.around;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.HashSet;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.JsonReader;
 
 import com.facebook.android.*;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.Facebook.*;
 import com.facebook.android.AsyncFacebookRunner;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class Login extends Activity{
 	Facebook facebook = new Facebook("332459203518890");
     AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
     String fb_id = "";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
 
         facebook.authorize(this, new DialogListener() {
             @Override
             public void onComplete(Bundle values) {}
 
             @Override
             public void onFacebookError(FacebookError error) {}
 
             @Override
             public void onError(DialogError e) {}
 
             @Override
             public void onCancel() {}
         });
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         facebook.authorizeCallback(requestCode, resultCode, data);
         mAsyncRunner.request("me", new IdRequestListener());
         mAsyncRunner.request("me/friends", new FriendsRequestListener());
         finish();
     }
     
     private class IdRequestListener implements RequestListener{
 
 		@Override
 		public void onComplete(String response, Object state) {
 			try {
 				final JSONObject json = Util.parseJson(response);
 				final String id = json.getString("id");
 				System.out.println(json.toString());
 				RestClient.get("/users/fb_id/" + id, null, null, new JsonHttpResponseHandler() {
 					@Override
 					public void onSuccess(JSONArray rsp) {
 						if (rsp.length()==0) {
 							//no content in response json means no user with this fb id exists
 							System.out.println("New user logged in");
 							try {
 								RestClient.get("/users/add/"+json.getString("first_name")+"/"+json.getString("last_name")+"/"+id, null, null, new JsonHttpResponseHandler() {
 									@Override
 									public void onSuccess(JSONArray rsp) {
 										//TODO handle user being added
 										StaticUserInfo.setFbID(id);
 										System.out.println("new user's fb id: "+id);
 										try {
 											StaticUserInfo.setUserID(rsp.getJSONObject(0).optJSONObject("User").getInt("user_id"));
 										} catch (JSONException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										}
 									}
 									@Override
 									public void onFailure(Throwable e, String rsp) {
 										//TODO handle error adding user
 										System.err.println(e.getMessage());
 									}
 								});
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 						else {
 							StaticUserInfo.setFbID(id);
 							System.out.println("user's fb id: "+id);
 							try {
 								StaticUserInfo.setUserID(rsp.getJSONObject(0).optJSONObject("User").getInt("user_id"));
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					}
 					@Override
 					public void onFailure(Throwable e, String rsp) {
 						
 					}
 				});
 			} catch (FacebookError e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 		}
 
 		@Override
 		public void onIOException(IOException e, Object state) {
 			
 		}
 
 		@Override
 		public void onFileNotFoundException(FileNotFoundException e,
 				Object state) {
 			
 		}
 
 		@Override
 		public void onMalformedURLException(MalformedURLException e,
 				Object state) {
 			
 		}
 
 		@Override
 		public void onFacebookError(FacebookError e, Object state) {
 			
 		}
     	
     }
     private class FriendsRequestListener implements RequestListener{
 
 		@Override
 		public void onComplete(String response, Object state) {
 			try {
 				JSONObject json = Util.parseJson(response);
 				JSONArray arr = json.getJSONArray("data");
 				HashSet<String> friends = new HashSet<String>();
 				for (int i=0; i<arr.length(); i++) {
 					friends.add(arr.optJSONObject(i).getString("id"));
 					//StaticUserInfo.addFbFriend(arr.optJSONObject(i).getString("id"));
 				}
 				StaticUserInfo.setFbFriends(friends);
 			} catch (FacebookError e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onIOException(IOException e, Object state) {
 			
 		}
 
 		@Override
 		public void onFileNotFoundException(FileNotFoundException e,
 				Object state) {
 			
 		}
 
 		@Override
 		public void onMalformedURLException(MalformedURLException e,
 				Object state) {
 			
 		}
 
 		@Override
 		public void onFacebookError(FacebookError e, Object state) {
 			
 		}
     	
     }
 }
 
 
