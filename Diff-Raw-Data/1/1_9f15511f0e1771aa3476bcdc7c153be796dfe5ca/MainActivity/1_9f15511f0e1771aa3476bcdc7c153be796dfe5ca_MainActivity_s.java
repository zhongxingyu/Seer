 package com.rb.sqlitefirst;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 
 public class MainActivity extends ListActivity {
 
 	private CommentsDataSource dataSource;
 	private EditText commentString;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		dataSource = new CommentsDataSource(this);
 		dataSource.open();
 		commentString = (EditText)findViewById(R.id.edit_comment);
 		
 		List<Comment> values = dataSource.getAllComments();
 		ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>
 		(this, android.R.layout.simple_list_item_1, values);
 		setListAdapter(adapter);
 	}
 
 	
 	public void onClick(View view){
 		@SuppressWarnings("unchecked")
 		ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
 		Comment comment = null;
 		switch(view.getId()){
 		case R.id.button_add:
 			if(commentString.getText() != null){
 				String strComment = commentString.getText().toString();
 				comment = dataSource.createComment(strComment);
 				adapter.add(comment);
 			}
 			break;
 
 		case R.id.button_delete:
 			if (getListAdapter().getCount() > 0) {
 				comment = (Comment) getListAdapter().getItem(0);
 				dataSource.deleteComment(comment);
 				adapter.remove(comment);
 			}
 			break;
 		}
 		adapter.notifyDataSetChanged();
 
 	}
 	 @Override
 	  protected void onResume() {
 	    dataSource.open();
 	    super.onResume();
 	  }
 
 	  @Override
 	  protected void onPause() {
 	    dataSource.close();
 	    super.onPause();
 	  }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
