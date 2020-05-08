 package ca.ualberta.cs.completemytask.activities;
 
 import ca.ualberta.cs.completemytask.R;
 import ca.ualberta.cs.completemytask.background.BackgroundTask;
 import ca.ualberta.cs.completemytask.background.HandleInBackground;
 import ca.ualberta.cs.completemytask.database.DatabaseManager;
 import ca.ualberta.cs.completemytask.saving.LocalSaving;
 import ca.ualberta.cs.completemytask.settings.Settings;
 import ca.ualberta.cs.completemytask.userdata.Comment;
 import ca.ualberta.cs.completemytask.userdata.Task;
 import ca.ualberta.cs.completemytask.userdata.TaskManager;
 import ca.ualberta.cs.completemytask.userdata.User;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Handles the adding of a comment.  The user
  * enters their comment into the EditText 
  * "Comment" and it is added to the task with
  * the button "SendButton".
  * 
  * @author 
  *
  */
 public class CommentActivity extends Activity {
 
 	private static final String TAG = "CommentActivity";
 	private EditText commentEditText;
 	private TextView commentsTextView;
 	private Task task;
 	
 	public LocalSaving saver;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_comment);
 		
 		saver = new LocalSaving();
 		
 		int position = TaskManager.getInstance().getCurrentTaskPosition();
 		task = TaskManager.getInstance().getTaskAt(position);
 		
 		commentsTextView = (TextView) findViewById(R.id.commentsView);
 		
 		BackgroundTask bg = new BackgroundTask();	
     	bg.runInBackGround(new HandleInBackground() {
     		public void onPreExecute() {
     		}
     		
     		public void onPostExecute(int response) {
     			int numberOfComments = task.getNumberOfComments();
     			
     			for (int i = 0; i < numberOfComments; i++) {
     				
     				Comment comment = task.getCommentAt(i);
     				User user = comment.getUser();
     				
     				String commentsString = commentsTextView.getText().toString();
     				commentsString +=  user.getUserName() + "\n\n\t" + comment.getContent() + "\n\n\n";
     				commentsTextView.setText(commentsString);
     			}
     		}
     		
     		public void onUpdate(int response) {
     		}
     		
     		public boolean handleInBackground(Object o) {	
    			DatabaseManager.getInstance().getComments(task);
				return true;
     		}
     	});
 		
 		commentEditText = (EditText) findViewById(R.id.Comment);
 		commentEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
 
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				boolean handled = false;
 				if (actionId == EditorInfo.IME_ACTION_SEND) {
 					// Send the user message
 					send(null);
 					handled = true;
 				}
 				return handled;
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_comment, menu);
 		return true;
 	}
 	
 	/**
 	 * Send a text to the current task.
 	 * @param A view
 	 */
 	public void send(View view) {
 		Log.v(TAG, "Sent Comment");
 		String commentString = commentEditText.getText().toString();
 		commentEditText.setText("");
 		
 		if (task == null)
 			return;
 		
 		if (commentString.length() > 0) {
 			Comment comment = new Comment(commentString);
 			
 			User user = null;
 			
 			if (Settings.getInstance().hasUser()) {
 				user = Settings.getInstance().getUser();
 			} else {
 				user = new User("Unknown");
 			}
 			
 			comment.setUser(user);
 			comment.setParentId(task.getId());
 			sync(comment);
 			
 			String commentsString = commentsTextView.getText().toString();
 			commentsString +=  user.getUserName() + "\n\n\t" + comment.getContent() + "\n\n\n";
 			commentsTextView.setText(commentsString);
 		}
 	}
 	
 	public void sync(final Comment comment) {
 		BackgroundTask bg = new BackgroundTask();
     	bg.runInBackGround(new HandleInBackground() {
     		public void onPreExecute() {
     		}
     		
     		public void onPostExecute(int response) {
     		}
     		
     		public void onUpdate(int response) {
     		}
     		
     		public boolean handleInBackground(Object o) {
     			DatabaseManager.getInstance().syncComment(comment);
 				task.addComment(comment);
 				
 				if (task.isLocal()) {
 					saver.open();
 					saver.saveComment(comment);
 					saver.close();
 
 				}
 				return true;
     		}
     	});
     }
 
 	/**
 	 * Close activity.
 	 * 
 	 * @param A view
 	 */
 	public void close(View view) {
 		this.finish();
 	}
 }
