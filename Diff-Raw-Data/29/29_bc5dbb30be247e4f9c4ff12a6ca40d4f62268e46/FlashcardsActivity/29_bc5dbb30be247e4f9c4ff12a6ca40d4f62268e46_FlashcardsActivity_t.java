 package se.flashcards;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import se.flashcards.WriteTextDialogFragment.OnTextMadeListener;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 //import com.actionbarsherlock.view.ActionMode;
 //import com.actionbarsherlock.view.ActionMode.Callback;
 import com.actionbarsherlock.view.Menu;
 //import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.WindowManager.LayoutParams;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.view.Window;
 
 @TargetApi(11)
 public class FlashcardsActivity extends SherlockListActivity implements OnTextMadeListener {
 	
 	private static final int DIALOG_MAKE_NEW = 0;
 	private static final int OPEN_CARDSLIST = 1;
 	private static final int DIALOG_RENAME = 2;
 	
 	public static final String CARD_LIST_NAME = "card_list_name";
 	public static final String CARD_LIST_ID = "card_list_id";
 
 	private List<CardList> cardLists;
 	private CardsListListAdapter cardListsAdapter;
 	private InfoSaver infoSaver;
 	private int itemToRename = 0;
 	private ActionMode modeToFinish; //3.0+ ONLY 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);   
         setTheme(R.style.Theme_Sherlock);  
         
         if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
         	requestWindowFeature(Window.FEATURE_NO_TITLE);
         }
         
         infoSaver = InfoSaver.getInfoSaver(this);
         
         //TEMP, should be set through XML
         ActionBar bar = getSupportActionBar();
         Util.customizeActionBar(getResources(), bar);     
         
         cardLists = infoSaver.getCardLists();
         cardListsAdapter = new CardsListListAdapter(this, cardLists);
         setListAdapter(cardListsAdapter);
         //
         //3.0+ ONLY code!!!
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 	        final ListView listView = getListView();
 	        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); //_MODAL
 	        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
 	        	private int selectedItems = 0;
 	        	private android.view.MenuItem renameItem = null;
 	        	
 				@Override
 				public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
 					switch (item.getItemId()) {
 			            case R.id.menu_delete: {
 			                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();             
 			                int removed = 0;
 			                for (int i = 0; i < checkedItems.size(); i++) {
 			                	int key = checkedItems.keyAt(i);
 			                	if (checkedItems.get(key)) {
 				                	removeCardList(key - removed);
 				                	removed++;
 			                	}
 			                }
 			                mode.finish();
 			                return true;
 			            }
 			            case R.id.rename_item: {
 			            	SparseBooleanArray checkedItems = listView.getCheckedItemPositions();   
 			            	for (int i = 0; i < checkedItems.size(); i++) {
 			            		int key = checkedItems.keyAt(i);
 			            		if (checkedItems.get(key)) {
 					            	itemToRename = key;
 		       		            	modeToFinish = mode;
 		       		    	        // DialogFragment dialogFragment = WriteTextDialogFragment.newInstance("Rename", "Name", "");
 		       		    	        // dialogFragment.show(FlashcardsActivity.this.getSupportFragmentManager(), "rename_list");
 		       		            	//TEMP, do through fragments instead
 		       		            	showDialog(DIALOG_RENAME);
 			            		}
 			            	}
 	
 			            	modeToFinish = mode;
 			            	return true;
 			            }
 			            default:
 			                return false;
 					}
 				}
 	
 				@Override
 				public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
 					MenuInflater inflater = mode.getMenuInflater();
 					inflater.inflate(R.layout.list_item_longpress_menu, menu);
 					
 					renameItem = menu.findItem(R.id.rename_item);
 					//TEMP
 			//		renameItem.setVisible(false);
 					
 					mode.setTitle(selectedItems + " selected");
 					return true;
 				}
 	
 				@Override
 				public void onDestroyActionMode(ActionMode mode) 
 				{
 					selectedItems = 0;
 				}
 	
 				@Override
 				public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
 					return false;
 				}
 	
 				@Override
 				public void onItemCheckedStateChanged(ActionMode mode, int pos, long id, boolean checked) {
 					if (checked) {
 						selectedItems++;
 					} else {
 						selectedItems--;
 					}
 					if (selectedItems > 1) {
 						renameItem.setVisible(false);
 					} else if (selectedItems <= 1) {
 						renameItem.setVisible(true);
 					}
 					mode.setTitle(selectedItems + " selected");
 				}
 	        });	
         } else {
         	registerForContextMenu(getListView());
         }
        
         
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
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.list_item_longpress_menu, menu);
 		Log.v("flashcards", "oCCM");
     }
     
     @Override
     public boolean onContextItemSelected (android.view.MenuItem item) {
     	Log.v("flashcards", "oCIS");
     	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 		switch (item.getItemId()) {
 	        case R.id.menu_delete: {
 	        	removeCardList(info.position);
 	        }
 	        break;
 	        case R.id.rename_item: {
             	itemToRename = info.position;
     	        // DialogFragment dialogFragment = WriteTextDialogFragment.newInstance("Rename", "Name", "");
     	        // dialogFragment.show(FlashcardsActivity.this.getSupportFragmentManager(), "rename_list");
             	//TEMP, do through fragments instead
             	showDialog(DIALOG_RENAME);
 	        }
 	        break;
 		}
     	return false;
     }
     
     private void removeCardList(int nr) {
     	infoSaver.removeCardList(cardListsAdapter.getItem(nr).getID());
     	cardLists.remove(cardListsAdapter.getItem(nr));
     	cardListsAdapter.notifyDataSetChanged();
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	validateListNames();
     }
     
     private void validateListNames() {
 //    	for (CardList cl : cardLists) {
 //    		String oldName = cl.getName();
 //    		String newName = infoSaver.nameFromId(cl.getID());
 //    		if (!oldName.equals(newName)) {
 //    			cl.rename(newName);
 //    			cardListsAdapter.notifyDataSetChanged();
 //    		}
 //    	}
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
     	//		DialogFragment dialogFragment = new WriteTextDialogFragment();
     	//		dialogFragment.show(getFragmentManager(), "make_new_list");
     			//TEMP, do through fragments instead
     			showDialog(DIALOG_MAKE_NEW);
     			break;
     	}
         return false;
     }
     
 	@Override
     public void onListItemClick(ListView listView, View view, int position, long id) {
     	Intent intent = new Intent(this, CardsListActivity.class);
     	intent.putExtra(CARD_LIST_NAME, cardLists.get(position).getName());
        	intent.putExtra(CARD_LIST_ID, cardLists.get(position).getID());
     	startActivityForResult(intent, OPEN_CARDSLIST);
     }
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) { 
 	    super.onActivityResult(requestCode, resultCode, intent);
 		if (resultCode == RESULT_OK) {
 		    switch (requestCode) {
 		    	case OPEN_CARDSLIST: {
 		    		long id = intent.getLongExtra(CARD_LIST_ID, -1);
 		    		boolean shouldBeRemoved = intent.getBooleanExtra(CardsListActivity.SHOULD_BE_REMOVED, false);
 		    		if (shouldBeRemoved) {
 		    			removeId(id);
 		    		} else {
 			    		String newName = intent.getStringExtra(CARD_LIST_NAME);
 			    		if (newName != null) {
 			    			renameId(id, newName);
 			    		}
 			    		
 			    		int newAmount = intent.getIntExtra(CardsListActivity.NUMBER_OF_CARDS, -1);
 			    		Log.v("flashcards", "new: " + newAmount);
 			    		if (newAmount != -1) {
 			    			changeAmountOnId(id, newAmount);
 			    		}
 		    		}
 		    	}
 		    }
 		}
 	}
 	
 	private void changeAmountOnId(long id, int newAmount) {
 		for (CardList cl : cardLists) {
 			if (cl.getID() == id) {
 				cl.setNumberOfCards(newAmount);
 				cardListsAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	private void removeId(long id) {
 		Iterator<CardList> it = cardLists.iterator();
 		while (it.hasNext()) {
 			CardList cl = it.next();
 			if (cl.getID() == id) {
 				it.remove();
 				infoSaver.removeCardList(id);
 				cardListsAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	private void renameId(long id, String newName) {
     	for (CardList cl : cardLists) {
     		if (cl.getID() == id) {
     			cl.rename(newName);
     			cardListsAdapter.notifyDataSetChanged();
     		}
     	}
 	}
 	
 	private void renameNumber(int number, String newName) {
 		CardList cl = cardLists.get(number);
 		cl.rename(newName);
 		cardListsAdapter.notifyDataSetChanged();
 		infoSaver.renameCardList(cl.getID(), newName);
 	}
     
 //    public void tryToAddNewList(String name) {
 //    	boolean match = false;
 //    	for (String listName : cardLists) {
 //    		if (name.equals(listName)) {
 //    			match = true;
 //    		}
 //    	}
 //    	if (!match) {
 //    		cardListsAdapter.add(name);
 //    	} else {
 //    		//show dialog saying there is already a list with that name
 //    	}
 //    }
 	
 	protected Dialog onCreateDialog(int id) {
     	switch (id) {
     		case DIALOG_MAKE_NEW: {
                 LayoutInflater factory = LayoutInflater.from(this);
                 final View textEntryView = factory.inflate(R.layout.make_new_dialog, null);
                 final TextView textView = (TextView)textEntryView.findViewById(R.id.text);
                 textView.setHint("Title");
                 textView.requestFocus();
                
                //TEMP
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                	textView.setTextColor(getResources().getColor(android.R.color.black));
                }
                
                 Dialog dialog = new AlertDialog.Builder(this)
          //           .setIconAttribute(android.R.attr.alertDialogIcon)
                     .setTitle("Create new list")
                     .setView(textEntryView)
                     .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                         	String text = textView.getText().toString();
                         	if (!text.equals("")) {
                         		cardLists.add(new CardList(text));
                         		cardListsAdapter.notifyDataSetChanged();
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
                 dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                 return dialog;
     		}
             //TEMP, do through fragments instead
     		case DIALOG_RENAME: {
     			String title = "Rename list";
     			String hint = "Name";
     			String text = "";
     			String positiveText = "Rename";
     			final String tag = "rename_list";
     			
     	        LayoutInflater factory = LayoutInflater.from(this);
     	        final View textEntryView = factory.inflate(R.layout.make_new_dialog, null);
     	        final EditText textView = (EditText)textEntryView.findViewById(R.id.text);
    	        
                //TEMP
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                	textView.setTextColor(getResources().getColor(android.R.color.black));
                }
    	        
     	        textView.setHint(hint);
     	        textView.requestFocus();
     	        textView.setText(text);
     	        textView.setSelection(textView.getText().length());
     	        Dialog dialog = new AlertDialog.Builder(this)
  //   	            .setIconAttribute(android.R.attr.alertDialogIcon)
     	            .setTitle(title)
     	            .setView(textEntryView)
     	            .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
     	            	@Override
     	                public void onClick(DialogInterface dialog, int whichButton) {
     	            		onTextMade(tag, textView.getText());
     	                }
     	            })
     	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
     	                public void onClick(DialogInterface dialog, int whichButton) {
     	                	textView.setText("");
     	                }
     	            })
     	            .create();
     	        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
     	        return dialog;
     		}
     		default:
     			return null;
     	}
     }
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 		infoSaver.saveCardLists(cardLists);
 	}
 
 	@Override
 	public void onTextMade(String tag, CharSequence text) {
 		if (tag.equals("make_new_list")) {
 			if (!text.toString().equals("")) {
 	    		cardLists.add(new CardList(text.toString()));
 	    		cardListsAdapter.notifyDataSetChanged();
 	    	}
 		} else if (tag.equals("rename_list")) {
 			renameNumber(itemToRename, text.toString());
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				modeToFinish.finish();
 			}
 		}
 	}
 }
