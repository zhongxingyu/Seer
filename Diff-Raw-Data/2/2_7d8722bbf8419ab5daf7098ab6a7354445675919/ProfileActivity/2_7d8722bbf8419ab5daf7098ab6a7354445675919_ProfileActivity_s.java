 package com.normalexception.forum.rx8club.activities;
 
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
 
 import java.util.ArrayList;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.normalexception.forum.rx8club.Log;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.WebUrls;
 import com.normalexception.forum.rx8club.handler.ImageLoader;
 import com.normalexception.forum.rx8club.html.VBForumFactory;
 import com.normalexception.forum.rx8club.state.AppState;
 import com.normalexception.forum.rx8club.user.UserProfile;
 import com.normalexception.forum.rx8club.view.profile.ProfileView;
 import com.normalexception.forum.rx8club.view.profile.ProfileViewArrayAdapter;
 
 /**
  * Activity that sets up the users profile
  * 
  * Required Intent Parameters:
  * none
  */
 public class ProfileActivity extends ForumBaseActivity {
 
 	private static String TAG = "ProfileActivity";
 	
 	private ProgressDialog loadingDialog;
 	
 	private ArrayList<ProfileView> stubs;	
 	private ProfileViewArrayAdapter pva;
 	private ListView lv;
 	
 	private ImageLoader imageLoader;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setState(AppState.State.PROFILE, this.getIntent());
         
         setContentView(R.layout.activity_basiclist);
         
         if(checkTimeout()) {
	        Log.v(TAG, "Category Activity Started");
 	        
 	        imageLoader=new ImageLoader(this);
 	        
 	        constructView();
         }
     }
     
     private void updateList() {
 		final Activity a = this;
     	runOnUiThread(new Runnable() {
             public void run() {
 		    	pva = new ProfileViewArrayAdapter(a, 0, stubs);
 				lv.setAdapter(pva);
             }
     	});
 	}
     
     /**
      * Construct the profile view
      */
     private void constructView() {        
         final ForumBaseActivity src = this;
         
         updaterTask = new AsyncTask<Void,String,Void>() {
         	@Override
 		    protected void onPreExecute() {
 		    	loadingDialog = 
 						ProgressDialog.show(src, 
 								getString(R.string.loading), 
 								getString(R.string.pleaseWait), true);
 		    }
         	@Override
 			protected Void doInBackground(Void... params) {
         		final UserProfile upInstance = UserProfile.getInstance();
 				Document doc = 
 						VBForumFactory.getInstance().get(src, upInstance.getUserProfileLink());
 				if(doc != null) {
 					publishProgress(getString(R.string.asyncDialogGrabProfile));
 					String id = upInstance.getUserProfileLink().substring(
 							upInstance.getUserProfileLink().lastIndexOf("-") + 1,
 							upInstance.getUserProfileLink().length() - 1);
 					upInstance.setUserId(id);
 					getUserInformation(doc);
 					
 					lv = (ListView)findViewById(R.id.mainlistview);
 					
 					runOnUiThread(new Runnable() {
 			            public void run() {
 			            	View v = getLayoutInflater().
 			            			inflate(R.layout.view_profile_header, null);
 			            	v.setOnClickListener(src);
 			            	lv.addHeaderView(v);
 
 			            	publishProgress(getString(R.string.asyncDialogPopulating));
 			            	
 	                    	// the dateline at the end of the file so that we aren't
 	                        // creating multiple images for a user.  The image still
 	                        // gets returned without a date
 	                        String nodate_avatar = 
 	                        		upInstance.getUserImageLink().indexOf("&dateline") == -1? 
 	                        				upInstance.getUserImageLink() : 
 	                        					upInstance.getUserImageLink().substring(0, 
 	                        							upInstance.getUserImageLink().indexOf("&dateline"));
 	                        ImageView avatar = ((ImageView)findViewById(R.id.pr_image));
 	                        imageLoader.DisplayImage(nodate_avatar, avatar);
 	                        
 	                    	((TextView)findViewById(R.id.pr_username)).setText(
 	                    			upInstance.getUsername() + " (ID: " + upInstance.getUserId() + ")");
 	                    	((TextView)findViewById(R.id.pr_userTitle)).setText(upInstance.getUserTitle());
 	                    	((TextView)findViewById(R.id.pr_userPosts)).setText(upInstance.getUserPostCount());
 	                    	((TextView)findViewById(R.id.pr_userJoin)).setText(upInstance.getUserJoinDate());	          
 	                    }
 					});
 					
 					updateList();				
 				} 
 				return null;
 			}
         	@Override
 		    protected void onProgressUpdate(String...progress) {
 		        loadingDialog.setMessage(progress[0]);
 		    }
 			
 			@Override
 		    protected void onPostExecute(Void result) {
 				loadingDialog.dismiss();
 			}
         };
         updaterTask.execute();
     }
     
     /**
      * Get the user information from the users profile
      * @param doc	The page document
      */
     private void getUserInformation(Document doc) {
     	final UserProfile upInstance = UserProfile.getInstance();
     	stubs = new ArrayList<ProfileView>();
     	
     	// Title
     	Elements userInfo = doc.select("div[id=main_userinfo]");
     	Elements title = userInfo.select("h2");
     	upInstance.setUserTitle(title.text());
     	
     	// Posts
     	Elements statisticInfo = doc.select("fieldset[class=statistics_group]");
     	Elements post = statisticInfo.select("li");
     	
     	// Profile Pic
     	Elements profilePicInfo = doc.select("td[id=profilepic_cell] > img");
  
     	// Grab image, trap
     	try {
     		upInstance.setUserImageLink(profilePicInfo.attr("src"));
     	} catch (Exception e) { }
     	
     	// Grab Post count, trap exception
     	try {
     		upInstance.setUserPostCount(post.get(0).text() + " / " + post.get(1).text().split(" ",4)[3] + " per day");
     	} catch (Exception e) {
     		upInstance.setUserPostCount("Error Getting Post Count");
     	}
     	
     	// Grab Join Date, trap exception
     	try {
     		upInstance.setUserJoinDate(post.get(13).text());
     	} catch (Exception e) {
     		upInstance.setUserJoinDate("Error Getting Join Date");
     	}
     	
     	// Threads
     	String link = WebUrls.userUrl + upInstance.getUserId();
     	doc = VBForumFactory.getInstance().get(this, link);
     	if(doc != null) {
 	    	Elements threadlist = doc.select("table[id^=post]");
 	    	for(Element threadl : threadlist) {
 	    		ProfileView stub = new ProfileView();
 	    		Elements divs = threadl.getElementsByTag("div");
 	    		Elements div = divs.get(1).getElementsByTag("a");
 	    		stub.setLink(div.attr("href"));
 	    		stub.setName(div.text());
 	    		
 	    		div = divs.get(5).getElementsByTag("a");
 	    		stub.setText(div.text());
 	    		stubs.add(stub);
 	    	}
     	}
     }
 }
