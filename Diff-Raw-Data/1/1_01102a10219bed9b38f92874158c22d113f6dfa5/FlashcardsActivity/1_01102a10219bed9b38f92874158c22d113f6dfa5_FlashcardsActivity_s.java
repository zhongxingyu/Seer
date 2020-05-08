 package se.flashcards;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 //import com.actionbarsherlock.view.ActionMode;
 //import com.actionbarsherlock.view.ActionMode.Callback;
 import com.actionbarsherlock.view.Menu;
 //import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FlashcardsActivity extends SherlockListActivity {
 	
 	private static final int DIALOG_MAKE_NEW = 0;
 	
 	public static final String CARD_LIST_NAME = "cardlistname";
 
 	private List<String> cardLists;
 	private ArrayAdapter<String> cardListsAdapter;
 	private InfoSaver infoSaver;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);   
         setTheme(R.style.Theme_Sherlock);
         
         infoSaver = InfoSaver.getInfoSaver(this);
         
         cardLists = infoSaver.getCardLists();
         cardListsAdapter = new ArrayAdapter<String>(this, R.layout.list_item, cardLists);
         setListAdapter(cardListsAdapter);
         
         ActionBar bar = getSupportActionBar();
         bar.setTitle("RAYBAN");
         bar.setSubtitle("Only $999");
 
         //
         //3.0+ ONLY code!!!
         final ListView listView = getListView();
         listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); //_MODAL
         listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
 			@Override
 			public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
 				switch (item.getItemId()) {
 		            case R.id.menu_delete:
 		                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();             
 		                int removed = 0;
		                //FELFELFELFELFEL
 		                for (int i = 0; i < checkedItems.size(); i++) {
 		                	cardListsAdapter.remove(cardListsAdapter.getItem(checkedItems.keyAt(i) - removed));
 		                	removed++;
 		                }
 		                mode.finish();
 		                return true;
 		            default:
 		                return false;
 				}
 			}
 
 			@Override
 			public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
 				MenuInflater inflater = mode.getMenuInflater();
 				inflater.inflate(R.layout.list_item_longpress_menu, menu);
 				return true;
 			}
 
 			@Override
 			public void onDestroyActionMode(ActionMode mode) {
 
 				
 			}
 
 			@Override
 			public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
 				return false;
 			}
 
 			@Override
 			public void onItemCheckedStateChanged(ActionMode mode, int pos, long id, boolean checked) {
 				//temp
 				Toast toast = Toast.makeText(FlashcardsActivity.this, "" + listView.getCheckedItemCount(), Toast.LENGTH_SHORT);
 				toast.show();	
 			}
         });
        
         
 //        final ListView listView = getListView();
 //        listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
 //			@Override
 //			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 //				FlashcardsActivity.this.startActionMode(new ActionMode.Callback() {
 //					@Override
 //					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 //						MenuInflater inflater = mode.getMenuInflater();
 //						inflater.inflate(R.layout.list_item_longpress_menu, menu);
 //						return true;
 //					}
 //
 //					@Override
 //					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 //						return false;
 //					}
 //
 //					@Override
 //					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 //						return true;
 //					}
 //
 //					@Override
 //					public void onDestroyActionMode(ActionMode mode) {
 //					}
 //				});
 //				view.setSelected(true);
 //				return true;
 //			}
 //		});
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.layout.actionbar_menu, menu);
         return true;
     }
     
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.menu_make_new:
     			showDialog(DIALOG_MAKE_NEW);
     			break;
     	}
         return true;
     }
     
     public void onListItemClick(ListView listView, View view, int position, long id) {
     	Intent intent = new Intent(this, CardsListActivity.class);
     	intent.putExtra(CARD_LIST_NAME, cardLists.get(position));
     	startActivity(intent);
     }
     
     protected Dialog onCreateDialog(int id) {
     	switch (id) {
     		case DIALOG_MAKE_NEW:
                 LayoutInflater factory = LayoutInflater.from(this);
                 final View textEntryView = factory.inflate(R.layout.make_new_dialog, null);
                 final TextView textView = (TextView)textEntryView.findViewById(R.id.text);
                 return new AlertDialog.Builder(this)
                     .setIconAttribute(android.R.attr.alertDialogIcon)
                     .setTitle("Make new entry")
                     .setView(textEntryView)
                     .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                         	String text = textView.getText().toString();
                         	if (!text.equals("")) {
                         		cardListsAdapter.add(text);
                         	}
                         	textView.setText("");
                         }
                     })
                     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                         	textView.setText("");
                         }
                     })
                     .create();
     		default:
     			return null;
     	}
     }
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 		infoSaver.saveCardLists(cardLists);
 	}
 }
