 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ShowComments extends Activity {
 
 	//TODO(kuyumcu)
 	private MessagePOJO message;
	private Bundle extras;
 	private String uname;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.showcomments);
        extras = this.getIntent().getExtras();
         
         if (extras != null){
         	uname = extras.getString("user");
         	// TODO(kuyumcu): Swap in Message data when implemented
         	/* Message Object Start */
         	message = (MessagePOJO)extras.get("messagePOJO");
         	
         	/* Dummy Code */
         	LinearLayout layout = (LinearLayout) findViewById(R.id.commentMessagePane);
             layout.setBackgroundColor(Color.GRAY);
 
             TextView temp = (TextView)findViewById(R.id.messageText);
             temp.setTextColor(Color.WHITE);
             temp.setText(message.getText());
             
         	temp = (TextView) findViewById(R.id.messageAuthor);
         	temp.setTextColor(Color.WHITE);
         	String author = message.getAuthor();
         	temp.setText("Posted by " + author + " at ");
         	
         	temp = (TextView) findViewById(R.id.messageTimestamp);
         	temp.setTextColor(Color.WHITE);
         	SimpleDateFormat formatter = new SimpleDateFormat();
        	temp.setText(formatter.format(new Date(System.currentTimeMillis()-86400000)));
         }
         
         populateComments();
     }
 
     public void onPostComment(View view) {
     	CommentPOJO comment = new CommentPOJO();
     	
     	EditText commentText = (EditText) findViewById(R.id.newCommentText);
     	if (commentText.getText().toString().equals("")) {
     		Toast.makeText(this, "Please enter a comment.", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	comment.setText(commentText.getText().toString());
     	
     	//TODO(jmow): Save author name when logins are enabled
     	comment.setAuthor(uname);
     	comment.setTimestamp(System.currentTimeMillis() + "");
     	
     	insertComment(comment);
     	commentText.setText("");
     	populateComments();
     }
     
     public void insertComment(CommentPOJO comment) {
     	// First we have to open our DbHelper class by creating a new object of that
     	AndroidOpenDbHelper androidOpenDbHelperObj = new AndroidOpenDbHelper(this);
 
     	// Then we need to get a writable SQLite database, because we are going to insert some values
     	// SQLiteDatabase has methods to create, delete, execute SQL commands, and perform other common database management tasks.
     	SQLiteDatabase sqliteDatabase = androidOpenDbHelperObj.getWritableDatabase();
     	// Try to create database for comments if not already there
     	androidOpenDbHelperObj.createCommentsTable(sqliteDatabase);
 
     	// ContentValues class is used to store a set of values that the ContentResolver can process.
     	ContentValues contentValues = new ContentValues();
 
     	// Get values from the POJO class and passing them to the ContentValues class
     	contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_COMMENT_TEXT, comment.getText());
     	contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_COMMENT_AUTHOR, comment.getAuthor());
     	contentValues.put(AndroidOpenDbHelper.COLUMN_NAME_COMMENT_TIMESTAMP, comment.getTimestamp());
 
 
     	// Now we can insert the data in to relevant table
     	// I am going pass the id value, which is going to change because of our insert method, to a long variable to show in Toast
     	long affectedColumnId = sqliteDatabase.insert(AndroidOpenDbHelper.TABLE_NAME_COMMENTS, null, contentValues);
 
     	// It is a good practice to close the database connections after you have done with it
     	sqliteDatabase.close();
 
     	// I am not going to do the retrieve part in this post. So this is just a notification for satisfaction 
     	Toast.makeText(this, "Comment Posted. DB Column ID :" + affectedColumnId, Toast.LENGTH_SHORT).show();
 
     }
     
     public void populateComments() {
     	List<CommentPOJO> comments = getComments();
         
         LinearLayout commentsPane = (LinearLayout) findViewById(R.id.commentsPane);
         commentsPane.removeAllViews();
         for (CommentPOJO c : comments) {
         	LinearLayout commentFrame = new LinearLayout(this);
         	commentFrame.setOrientation(1);
         	commentFrame.setPadding(1, 1, 1, 1);
         	
         	LinearLayout header = new LinearLayout(this);
         	header.setOrientation(0);
         	
         	TextView author = new TextView(this);
         	author.setText(c.getAuthor());
         	author.setTypeface(Typeface.DEFAULT_BOLD);
         	
         	TextView timestamp = new TextView(this);
         	long time = Long.parseLong(c.getTimestamp());
         	SimpleDateFormat formatter = new SimpleDateFormat();
         	timestamp.setText(" at " + formatter.format(new Date(time)));
         	
         	header.addView(author);
         	header.addView(timestamp);
         	
         	TextView commentText = new TextView(this);
         	commentText.setText(c.getText());
         	
         	commentFrame.addView(header);
         	commentFrame.addView(commentText);
         	
         	commentsPane.addView(commentFrame);
         }
     }
     
     // For Testing
     public List<CommentPOJO> getComments() {
     	List<CommentPOJO> commentList = new ArrayList<CommentPOJO>();
    	
     	// First we have to open our DbHelper class by creating a new object of that
     	AndroidOpenDbHelper dbHelper = new AndroidOpenDbHelper(this);
 
     	// Then we need to get a writable SQLite database, because we are going to insert some values
     	// SQLiteDatabase has methods to create, delete, execute SQL commands, and perform other common database management tasks.
     	SQLiteDatabase db = dbHelper.getReadableDatabase();
     	dbHelper.createCommentsTable(db);
     	
     	Cursor cursor = db.query(AndroidOpenDbHelper.TABLE_NAME_COMMENTS, null, null, null, null, null, null);
     	startManagingCursor(cursor);
     	
     	while (cursor.moveToNext()) {
     		String text = cursor.getString(cursor.getColumnIndex(AndroidOpenDbHelper.COLUMN_NAME_COMMENT_TEXT));
     		String author = cursor.getString(cursor.getColumnIndex(AndroidOpenDbHelper.COLUMN_NAME_COMMENT_AUTHOR));
     		String timestamp = cursor.getString(cursor.getColumnIndex(AndroidOpenDbHelper.COLUMN_NAME_COMMENT_TIMESTAMP));
     		
     		CommentPOJO comment = new CommentPOJO();
     		comment.setText(text);
     		comment.setAuthor(author);
     		comment.setTimestamp(timestamp);
     		commentList.add(comment);
     	}
     	
     	db.close();
     	return commentList;
     }
     
 }
