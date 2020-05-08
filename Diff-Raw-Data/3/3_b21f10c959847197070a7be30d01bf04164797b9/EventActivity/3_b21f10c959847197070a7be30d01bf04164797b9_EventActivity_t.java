 package pt.up.fe.socialcrowd.activities;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.eel.kitchen.jsonschema.syntax.ItemsSyntaxChecker;
 
 import pt.up.fe.socialcrowd.R;
 import pt.up.fe.socialcrowd.API.Request;
 import pt.up.fe.socialcrowd.helpers.CommentsListAdapter;
 import pt.up.fe.socialcrowd.logic.BaseEvent;
 import pt.up.fe.socialcrowd.logic.Comment;
 import pt.up.fe.socialcrowd.logic.DetailedEvent;
 import pt.up.fe.socialcrowd.logic.Downvote;
 import pt.up.fe.socialcrowd.logic.Subscription;
 import pt.up.fe.socialcrowd.logic.Upvote;
 import pt.up.fe.socialcrowd.logic.Vote;
 import pt.up.fe.socialcrowd.managers.DataHolder;
 import android.R.integer;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore.Video;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventActivity extends DashboardActivity implements OnClickListener {
 
 	private DetailedEvent event = null;
 	private ProgressDialog progressDialog = null;
 	private TextView eventName, eventLocation, eventDescription, eventTags, eventCategory;
 	private EditText inputComment;
 	private Button subscriptionButton;
 	private boolean isSubscribed;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event);
 
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setCancelable(false);
 		progressDialog.setMessage(getResources().getString(R.string.please_wait));
 		progressDialog.show();
 
 		checkSubscribed();	
 		displayEvent();
 	}
 
 	private void checkSubscribed() {
 		subscriptionButton = (Button) findViewById(R.id.subscribe_button);
 		isSubscribed = getIntent().getBooleanExtra("subscribed_event", false);
 	}
 
 	private void displayEvent() {
 		final int event_id = this.getIntent().getIntExtra("event_id", -1);
 
 		new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				try {
 					event = Request.getEventByID(event_id);
 
 					if(!isSubscribed){
 						ArrayList<BaseEvent> events = Request.getEventsBySubscriberID(DataHolder.getCurrentUserSession().getUser_id());
 
 						if(events.contains(event)){
 							isSubscribed = true;
 							subscriptionButton.setVisibility(View.INVISIBLE);
 						}
 					}else{
 						subscriptionButton.setVisibility(View.INVISIBLE);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				if(event != null) {
 					eventName = (TextView) findViewById(R.id.event_name);
 					eventLocation = (TextView) findViewById(R.id.event_location);
 					eventDescription = (TextView) findViewById(R.id.event_description);
 					eventTags = (TextView) findViewById(R.id.event_tags);
 					eventCategory = (TextView) findViewById(R.id.event_category);
 					inputComment = (EditText) findViewById(R.id.inputComment);
 
 					// display delete icon if event is from this user
 					if(event.getAuthorId() == DataHolder.getCurrentUserSession().getUser_id()) {
 						ImageButton deleteBtn = (ImageButton) findViewById(R.id.deleteEvent);
 						deleteBtn.setVisibility(View.VISIBLE);
 					}
 
 					// this is to update the comments when you try to add a new one
 					inputComment.setOnClickListener(EventActivity.this);
 
 					progressDialog.dismiss();
 
 					eventName.setText(event.getName());
 					eventDescription.setText(event.getDescription());
 					eventCategory.setText(event.getCategory());
 					eventLocation.setText(event.getLocation().getText());
 					ArrayList<String> tags = event.getTags();
 					eventTags.setText(tags.toString());
 
 					insertComments();
 				} else {
 					progressDialog.dismiss();
 					// display warning
 				}
 			}
 		}.execute();
 	}
 
 	private void insertComments() {
 		ArrayList<Comment> comments = event.getComments();
 
 		if(comments != null) {
 			if(comments.size() != 0) {
 				displayCommentInfo();
 			}
 
 			final ListView commentsList = (ListView) findViewById(R.id.commentsList);
 			commentsList.setAdapter(new CommentsListAdapter(this, comments));
 			
 			// add click handler to view single event
 			commentsList.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
 					Object obj = commentsList.getItemAtPosition(pos);
 					final Comment comment = (Comment) obj; 
 
 					// Launching new dialog on selecting single List Item
 					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EventActivity.this);
 					
 					CharSequence[] choices = null;
 					
 					if(comment.getAuthor_id() == DataHolder.getCurrentUserSession().getUser_id()) {
 						final CharSequence[] items = {"Delete"};
 						choices = items;
 					} else {
 						boolean found = false;
 						
 						// check if user has voted in this comment
 						ArrayList<Upvote> upvotes = comment.getUpvotes();
 						for(Upvote u : upvotes) {
 							if(u.getUser_id() == DataHolder.getCurrentUserSession().getUser_id()) {
 								final CharSequence[] items = {"Remove like"};
 								choices = items;
 								found = true;
 								break;
 							}
 						}
 						
 						if(!found) {
 							ArrayList<Downvote> downvotes = comment.getDownvotes();
 							for(Downvote u : downvotes) {
 								if(u.getUser_id() == DataHolder.getCurrentUserSession().getUser_id()) {
 									final CharSequence[] items = {"Remove dislike"};
 									choices = items;
 									found = true;
 									break;
 								}
 							}
 						}
 						
 						if(!found) {
 							final CharSequence[] items = {"Like","Dislike"};
 							choices = items;
 						}
 					}
 					
 					final CharSequence[] choicesFinal = choices;
 
 					// set title
 					alertDialogBuilder
 					.setTitle("Available actions")
 					.setItems(choices, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							
 							final String choice = choicesFinal[which].toString();
 							new AsyncTask<Void, Void, Void>() {
 
 								@Override
 								protected Void doInBackground(Void... params) {
 									try {
 										Log.i("new vote up on comment", ""+choice);
 										
 										if(choice.equalsIgnoreCase("Delete")) {
 											Request.deleteComment(DataHolder.getCurrentUserSession().getSession_id(), comment.getId());
 										} else if (choice.equalsIgnoreCase("Like")) {
 											Request.createVote(DataHolder.getCurrentUserSession().getSession_id(), comment.getId(), Vote.UPVOTE);
 										} else if (choice.equalsIgnoreCase("Dislike")) {
 											Request.createVote(DataHolder.getCurrentUserSession().getSession_id(), comment.getId(), Vote.DOWNVOTE);
 										} else if (choice.equalsIgnoreCase("Remove like") || choice.equalsIgnoreCase("Remove dislike")) {
 											Request.deleteVote(DataHolder.getCurrentUserSession().getSession_id(), comment.getId());
 										}
 									} catch (Exception e) {
 										e.printStackTrace();
 									}
 									return null;
 								}
 
 								@Override
 								protected void onPostExecute(Void result) {
 
 								}
 							}.execute();
 						}
 					});
 
 					// create alert dialog
 					AlertDialog alertDialog = alertDialogBuilder.create();
 
 					// show it
 					alertDialog.show();
 				}
 			});
 		}
 	}
 
 	public void addComment(View v) {
 
 		// get comment text and validate it
 		EditText comment = (EditText) findViewById(R.id.inputComment);
 		if(comment.getText().toString().length() != 0) {
 			progressDialog.show();
 			new AsyncTask<String, Void, Comment>() {
 
 				@Override
 				protected Comment doInBackground(String... params) {
 					try {
 						Comment newComment = Request.createComment(DataHolder.getCurrentUserSession().getSession_id(), event.getId(), params[0], new Date());
 						return newComment;
 					} catch (Exception e) {
 						e.printStackTrace();
 						return null;
 					}
 				}
 
 				@Override
 				protected void onPostExecute(Comment result) {
 					progressDialog.dismiss();
 					if(result != null) {
 						// clear the input
 						inputComment.setText("");
 						inputComment.clearFocus();
 
 						// hide the keyboard
 						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 						imm.hideSoftInputFromWindow(inputComment.getWindowToken(), 0);
 
 						// make comment stuff visible
 						displayCommentInfo();
 
 						// add comment to the list
 						final ListView commentsList = (ListView) findViewById(R.id.commentsList);
 						((CommentsListAdapter)commentsList.getAdapter()).addItem(result);
 					} else {
 						Toast.makeText(getBaseContext(), "Error adding comment", Toast.LENGTH_SHORT).show();
 					}
 				}
 			}.execute(comment.getText().toString());
 		} else {
 			Toast.makeText(getBaseContext(), "Empty comment. Please write something", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void displayCommentInfo() {
 		ImageView separator = (ImageView) findViewById(R.id.commentsSeparator);
 		if(separator != null) {
 			separator.setVisibility(View.VISIBLE);
 		}
 		TextView commentsTitle = (TextView) findViewById(R.id.commentsTitle);
 		if(commentsTitle != null) {
 			commentsTitle.setVisibility(View.VISIBLE);
 		}
 	}
 
 	public void onClickBack(View v) {
 		finish();
 	}
 
 	public void onClickRefresh(View v) {
 		// update comments here!
 	}
 
 	public void onClickDelete(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// set title
 		alertDialogBuilder.setTitle("Delete event");
 
 		// set dialog message
 		alertDialogBuilder
 		.setMessage("Are you sure you want to delete this event?")
 		.setCancelable(false)
 		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				dialog.cancel();
 				progressDialog.show();
 
 				new AsyncTask<Void, Void, Boolean>() {
 					@Override
 					protected Boolean doInBackground(Void... params) {
 						try {
 							Request.deleteEvent(DataHolder.getCurrentUserSession().getSession_id(), event.getId());
 							return true;
 						} catch (Exception e) {
 							e.printStackTrace();
 							return false;
 						}
 					}
 
 					@Override
 					protected void onPostExecute(Boolean result) {
 						progressDialog.dismiss();
 						if(result) {
 							goHome(EventActivity.this);
 						} else {
 							Toast.makeText(getBaseContext(), "Error deleting event", Toast.LENGTH_SHORT).show();
 						}
 					}
 				}.execute();
 			}
 		})
 		.setNegativeButton("No",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// if this button is clicked, just close
 				// the dialog box and do nothing
 				dialog.cancel();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 
 	public void onClickSubscribe(View v){
 
 		new AsyncTask<Void, Void, Void>(){
 
 			@Override
 			protected Void doInBackground(Void... params) {
 
 				Subscription sub = null;
 				try {
 					sub = Request.createSubscription(
 							DataHolder.getCurrentUserSession().getSession_id(),
 							event.getId(),
 							DataHolder.getCurrentUserSession().getUser_id(), new Date());
 				} catch (Exception e){
 					Log.i("EXCEPTION", "In Request.createSubscription -> " + e.getMessage());
 					e.printStackTrace();
 				}
 				return null;
 			}
 		}.execute();
 	}
 
 	@Override
 	public void onClick(View v) {
 		Log.i("onClick", "it worked");
 	}
 }
