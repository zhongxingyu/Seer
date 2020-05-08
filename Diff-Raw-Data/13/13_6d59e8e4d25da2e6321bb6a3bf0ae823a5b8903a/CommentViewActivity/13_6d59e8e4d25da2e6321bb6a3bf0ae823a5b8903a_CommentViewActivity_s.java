 package com.kmcginn.airquotes;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseRelation;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 
 public class CommentViewActivity extends Activity {
 	Context context= this;
 	ParseObject post;
 	String postID = "0";
 	String postText = "";
 	String comment = "";
 	String username = "";
 	String date = "";
 	int altitude = 0;
 	private ArrayAdapter<String> listAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 
 		StrictMode.setThreadPolicy(policy); 
 		setContentView(R.layout.activity_comment_view);
     	Log.e("loc", "beginning of oncreate");
 
 		// load in the post's id
 
 		Intent intent = getIntent();
 		try{
 			postID = intent.getStringExtra("objId");
 			Log.e("post", "Post id: " + postID);
 		} catch(Exception e1) {
 			Log.e("newAct", "Unable to get objectId from previous activity: " + e1);			
 		}
     	Log.e("loc", "before query");
 
 		// query to get full post information
         ParseQuery query = new ParseQuery("Message");
         try {
 			post = query.get(postID);
 		} catch (Exception e3) {
 			Log.e("post","Post query raised an exception: "+e3);
 		}
         	
         			try {
 	        			
 	        				// get the message text
 	        				postText = post.getString("text").toString();
 	        				Log.e("post", "postText: " + postText);
 	        				//get the message's user
 	        				username = post.getString("user").toString();
 	        				//get the message's date
 	        				date = post.getCreatedAt().toString();
 	        				//get the message's altitude
 	        				altitude = post.getInt("altitude");
 	        				
 	        				//setup the gui
 	        				 try{
 	        			        	// put new info in text boxes
 	        			        	TextView postTextBox = (TextView) findViewById(R.id.postText);
 	        			        	Log.e("gui", "Text for box: " + postText);
 	        			        	postTextBox.setText(postText);
 	        			        	TextView dateTextBox = (TextView) findViewById(R.id.dateText);
 	        				        dateTextBox.setText(date);
 	        				        TextView userTextBox = (TextView) findViewById(R.id.usernameText);
 	        				        userTextBox.setText(username);
 	        				        TextView altTextBox = (TextView) findViewById(R.id.altText);
 	        				        altTextBox.setText(Integer.toString(altitude));
 	        			        } catch(Exception e2) {
 	        			        	Log.e("gui", "Unable to intialize text boxes: " + e2);
 	        			        }
 	        			
         			}
         			catch (Exception e1){
             			Log.e("query", "Exception while retrieving post: " + e1.getMessage());
             			Intent intent2= new Intent(context, MainActivity.class);
             	    	startActivity(intent2);
         			}
       
         		
         		/*else {
         			//failure
         			Toast.makeText(context, "Unable to retrieve post", Toast.LENGTH_LONG).show();
         			Intent intent3= new Intent(context, MainActivity.class);
         	    	startActivity(intent3);
         		}*/
     	Log.e("loc", "after query");
 
         // put new info in text boxes
         try {
 	        TextView postTextBox = (TextView) findViewById(R.id.postText);
 	        postTextBox.setText(postText);
         } catch (Exception e1){
         	Log.e("post", "Unable to set post Text in post: "+e1);
         }
         try{
 	        TextView dateTextBox = (TextView) findViewById(R.id.dateText);
 	        dateTextBox.setText(date);
         } catch (Exception e1){
         	Log.e("post", "Unable to set date Text in post: "+e1);
         }
         try{
 	        TextView userTextBox = (TextView) findViewById(R.id.usernameText);
 	        userTextBox.setText(username);
         } catch (Exception e1){
         	Log.e("post", "Unable to set username Text in post: "+e1);
         }
         try{
 	        TextView altTextBox = (TextView) findViewById(R.id.altText);
 	        altTextBox.setText(Integer.toString(altitude));
         } catch (Exception e1){
         	Log.e("post", "Unable to set altitude Text in post: "+e1);
         }
     	Log.e("loc", "after sets");
 
         //
         //Update Comments
         //
 /*		String comment= "Hello to you too!";
 		
 		// get current user
 		ParseUser currUser= ParseUser.getCurrentUser();
 
 		// upload the comment
 		ParseObject commentHolder= new ParseObject("Comment");
 		commentHolder.put("text", comment);
 		commentHolder.put("user", currUser.getUsername());
     	Log.e("loc", "after puts" + currUser.getUsername());
     	try{
 		commentHolder.saveInBackground(new SaveCallback() {
 			public void done(ParseException e) {
 				if(e == null) {
 					// saved successfully
 					Toast.makeText(context, "Comment saved", Toast.LENGTH_SHORT).show();
 				}
 				else {
 				
 					// did not save successfully
 					Toast.makeText(context, "Unable to save comment: " + e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
     	} catch(Exception e1){
     		Log.e("save","Exception while saving comment: "+e1);
     	}
     	Log.e("loc", "after save");
 
 		
 		//	update the relationship
 		ParseRelation relation1 = post.getRelation("Comments");
     	Log.e("loc", "after add");
 
 		relation1.add(commentHolder);
     	Log.e("loc", "after add");
 */
     	
   		 // get ListView from activity's View
             ListView listView = (ListView) findViewById(R.id.commentList);
           // init arraylist adapter
           listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
           // attach the adapter to the listview
         try {
           listView.setAdapter(listAdapter);
 
     	} catch (Exception e1) {
     		Log.e("adapter", "Exception while creating listAdaptor: "+e1);
     	}
       	  Log.e("loc", "after adapter");
       	  
       	  try {
           // setup parse query (for comments)
           ParseRelation relation = post.getRelation("Comments");
           ParseQuery commentQuery = relation.getQuery();
           
           Log.e("loc", "begin query");
 
           // find them in the background
           commentQuery.findInBackground(new FindCallback() {
           	public void done(List<ParseObject> objects, ParseException e) {
           		if(e == null) {
           			//success
           			
           			// add all objects from query to list adapter
           			for(Object o: objects){
           				// get the message
           				String text = ((ParseObject) o).getString("text").toString();
           				// get the user
           				String user = ((ParseObject) o).getString("user").toString();
           				// add a string combining the message and user
           				listAdapter.add(text + "\n" + user);
           			}
           			
           			//indicate that the list adapter has changed its data, so the listview will update
           			listAdapter.notifyDataSetChanged();
           		}
           		else {
           			//failure
           			Toast.makeText(context, "Unable to retrieve comments", Toast.LENGTH_LONG).show();
           		}
           	}
           });
 	 } catch(Exception e1){
  		  Log.e("query", "Exception in querying relation: "+e1);
  	  }
      
       	Log.e("loc", "end query");
 
     
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.comment_view, menu);
 		return true;
 	}
 
 	public void onDoneClicked(View view){
 		Intent intent= new Intent(context, MainActivity.class);
     	startActivity(intent);	
     }
 	
 	public void onClimbClicked(View view){
		//TODO: indicate that it needs to be incremented, don't work on current value!
 		altitude+=1;
 		
		post.put("altitude", altitude);
 		post.saveInBackground(new SaveCallback() {
 			public void done(ParseException e) {
 				if(e == null) {
 					// saved successfully
 					Toast.makeText(context, "Altitude updated", Toast.LENGTH_SHORT).show();
 				}
 				else {
 				
 					// did not save successfully
 					Toast.makeText(context, "Unable to update altitude: " + e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		TextView altTextBox = (TextView) findViewById(R.id.altText);
         altTextBox.setText(altitude);
 		
 		
     }
 	
 	public void onDiveClicked(View view){
		//TODO: indicate that it needs to be decremented, don't work on the current value!
		altitude-=1;
		
		post.put("altitude", altitude);
 		post.saveInBackground(new SaveCallback() {
 			public void done(ParseException e) {
 				if(e == null) {
 					// saved successfully
 					Toast.makeText(context, "Altitude updated", Toast.LENGTH_SHORT).show();
 				}
 				else {
 				
 					// did not save successfully
 					Toast.makeText(context, "Unable to update altitude: " + e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		TextView altTextBox = (TextView) findViewById(R.id.altText);
         altTextBox.setText(altitude);
 		
     }
 	
 	public void onAddCommentClicked(View view){
 		// get comment from EditText
 		EditText commentTextBox= (EditText) findViewById(R.id.CommentEdit);
 		String comment= commentTextBox.getText().toString();
       	Log.e("loc", "start funct");
 
 		// get current user
 		ParseUser currUser= ParseUser.getCurrentUser();
 
 		// upload the comment
 		ParseObject commentHolder= new ParseObject("Comment");
 		commentHolder.put("text", comment);
 		commentHolder.put("user", currUser.getUsername());
       	Log.e("loc", "funct puts");
 		//	update the relationship
 		ParseRelation relation = post.getRelation("Comments");
 		Log.e("loc","between the two");
 		relation.add(commentHolder);
       	Log.e("loc", "addededed");
 
 		post.saveInBackground(new SaveCallback() {
 			public void done(ParseException e) {
 				if(e == null) {
 					// saved successfully
 					Toast.makeText(context, "Comment saved", Toast.LENGTH_SHORT).show();
 				}
 				else {
 				
 					// did not save successfully
 					Toast.makeText(context, "Unable to save comment: " + e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 
 	}
 	
 	
 }
