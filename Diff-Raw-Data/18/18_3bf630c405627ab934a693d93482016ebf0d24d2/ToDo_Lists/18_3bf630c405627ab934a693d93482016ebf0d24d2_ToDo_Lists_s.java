 package eecs.berkeley.edu.cs294;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 


 public class ToDo_Lists extends Activity {
 	/** Called when the activity is first created. */
 	TableLayout tl_todo_lists;
 	private DatabaseHelper dh;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.todo_lists);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
 
 		final ImageButton ib_custom_add = (ImageButton) findViewById(R.id.ib_custom_add);
 		if (ib_custom_add != null) {
 			ib_custom_add.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), Add.class);
 					startActivityForResult(intent, 0);
 				}
 			});
 		}
 
 		tl_todo_lists = (TableLayout) findViewById(R.id.tl_todo_lists);
 		this.dh = new DatabaseHelper(this);	
 
 		populate();
		
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 
 		if (resultCode == Activity.RESULT_OK && (requestCode == 0 || requestCode == 4)) {
 			tl_todo_lists.removeAllViews();
 			populate();
 		}
 	}
 
 	private void populate() {
 		List<String> titles = this.dh.selectAll_to_do("title");
 
 		for (String title : titles) {
 			TableRow row = new TableRow(this);		
 			row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 			TextView tv_title = new TextView(this);
 			tv_title.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));			
 			tv_title.setText(title);
 			tv_title.setTextSize(40);
 
 			row.addView(tv_title);
 			row.setContentDescription(tv_title.getText());
 			row.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), Preview.class);
 					intent.putExtra("title_select", v.getContentDescription());
 					startActivityForResult(intent, 3);
 				}
 			});
 			registerForContextMenu(row);
 			tl_todo_lists.addView(row);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		menu.setHeaderTitle(v.getContentDescription());
 		menu.add(0, dh.select_primary_key(v.getContentDescription().toString()), 0, "edit");
 		menu.add(0, dh.select_primary_key(v.getContentDescription().toString()), 1, "delete");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem menuItem) {
 		switch(menuItem.getOrder()) {
 		case 0:
 			Intent intent = new Intent(ToDo_Lists.this, Edit.class);
 			intent.putExtra("pk_select", menuItem.getItemId());
 			startActivityForResult(intent, 4);
 			return true;
 		case 1:
 			dh.delete_to_do(menuItem.getItemId());
 			tl_todo_lists.removeAllViews();
 			populate();
 			return true;
 		}
 		return false;
 	};
 }
