 package com.fyodorwolf.studybudy;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import com.fyodorwolf.studybudy.db.DatabaseAdapter;
 import com.fyodorwolf.studybudy.db.QueryRunner;
 import com.fyodorwolf.studybudy.db.QueryString;
 import com.fyodorwolf.studybudy.db.QueryRunner.QueryRunnerListener;
 import com.fyodorwolf.studybudy.models.*;
 
 import android.app.AlertDialog;
 import android.app.ExpandableListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckedTextView;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.TextView;
 
 public class DecksActivity extends ExpandableListActivity{
 
 	public static final String TAG = "MainActivity";
 	
 	private boolean _editing = false;
 	private boolean _deleting = false;
 	private boolean _searching = false;
 	private ArrayList<Section> _sections = new ArrayList<Section>();
 	private HashMap<Long,Section> _sectionIdMap = new HashMap<Long,Section>();
 	
 	private DatabaseAdapter _myDB;
 	private ExpandableListView _listView;
 	private ExpandableListAdapter _listViewAdapter;
 	
     @Override protected void onCreate(Bundle savedInstanceState) {
         // define main views.
 		setContentView(R.layout.decks_listview);
 	    getActionBar().setDisplayHomeAsUpEnabled(false);
 	    
     	_listView = this.getExpandableListView();
 		_myDB = DatabaseAdapter.getInstance();
 
 		/* SHOW ALL SECTIONS AND DECKS */
     	preformSearch();
     	
 		_listView.setOnChildClickListener(new OnChildClickListener(){
 			@Override public boolean onChildClick(ExpandableListView parent, View child, int groupIdx, int childIdx, long deckId) {
 				if(_editing){
 	            	runCreateDeckActivity(deckId);
 				}
 				else if(_deleting){
 					int position = parent.getPositionForView(child);
 					boolean curCheckState = ((CheckedTextView)child).isChecked();
 					_listView.setItemChecked(position, !curCheckState);
 				}else{
 					Intent deckIntent = new Intent(DecksActivity.this,CardsActivity.class);
 					deckIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
 	                            		Intent.FLAG_ACTIVITY_NO_ANIMATION);
 					deckIntent.putExtra(CardsActivity.EXTRAS_DECK_ID, deckId);
 					deckIntent.putExtra(CardsActivity.EXTRAS_DECK_NAME, ((TextView)child.findViewById(android.R.id.text1)).getText());
 					Deck clickedDeck = _sections.get(groupIdx).getDeckById(deckId);
 					if(_searching && clickedDeck.cards.size()>0){
 						//build an array of card id's to show in the next activity...
 						long[] cardIds = new long[clickedDeck.cards.size()];
 						int cardIdIdx = 0;
 						for(Card searchCard : clickedDeck.cards){
 							cardIds[cardIdIdx] = searchCard.id;
 							cardIdIdx++;
 						}
 						deckIntent.putExtra(CardsActivity.EXTRAS_CARD_IDS, cardIds);
 					}
 					startActivity(deckIntent);
 				}
 				return true;
 			}
 		});
         
         super.onCreate(savedInstanceState);
     }
 
 	@Override public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.section, menu);
         final SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
 		searchView.setOnQueryTextListener(new OnQueryTextListener(){
 			@Override public boolean onQueryTextSubmit(String query) {
 				return onQueryTextChange(query);
 			}
 			@Override public boolean onQueryTextChange(String newText) {
 				if(newText == "" || newText.isEmpty()){
 	                searchView.setIconified(true);
 					searchView.clearFocus();
 				}
 				preformSearch(newText);
 				return true;
 			}
 		});
         return true;
     }
 	
 	@Override public boolean onPrepareOptionsMenu(Menu menu){
     	if(_editing){
     		menu.findItem(R.id.main_menu_edit_deck).setVisible(false);
     		menu.findItem(R.id.main_menu_edit_list).setVisible(false);
     		menu.findItem(R.id.main_menu_delete).setVisible(false);
     		menu.findItem(R.id.main_menu_cancel_edit).setVisible(true);
     	}
     	else if(_deleting){
     		menu.findItem(R.id.main_menu_edit_deck).setVisible(false);
     		menu.findItem(R.id.main_menu_edit_list).setVisible(false);
     		menu.findItem(R.id.main_menu_delete).setVisible(true);
     		menu.findItem(R.id.main_menu_cancel_edit).setVisible(true);
     	}else{
     		menu.findItem(R.id.main_menu_edit_deck).setVisible(true);
     		menu.findItem(R.id.main_menu_edit_list).setVisible(true);
     		menu.findItem(R.id.main_menu_delete).setVisible(false);
     		menu.findItem(R.id.main_menu_cancel_edit).setVisible(false);
     	}
     	return true;
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId()) {
             case R.id.main_menu_new_deck:
             	runCreateDeckActivity(0);
             	break;
             case R.id.main_menu_edit_deck:
             	_editing = true;
             	break;
             case R.id.main_menu_edit_list:
             	_deleting = true;
             	_listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
                 _listView.setItemsCanFocus(false);
                 _listView.setAdapter(_listViewAdapter);
                 expandListView();
                 break;
             case R.id.main_menu_delete:
             	if(_listView.getCheckedItemCount()>0){
             		deleteDecks().show();
             	}else{
                 	doneEditing();
             	}
                 break; 	
             case R.id.main_menu_cancel_edit:
             	doneEditing();
                 break;
         }
     	return true;
     }
     
 	@Override public void onStart(){
     	_listView.requestFocus();
         super.onStart();
     }
     
 	private static void deleteAssociationsForDeckIds(Context context, long[] deckIds) {
 		final DatabaseAdapter myDB = DatabaseAdapter.getInstance();
 		//get all deck photo files.
 	   	new QueryRunner(myDB, new QueryRunnerListener(){
 			@Override public void onPostExcecute(Cursor cursor) {
 				if(cursor.getCount()>0){
 					int size = cursor.getCount();
 					long[] cardIds = new long[size];
 					String[] photoFilenames = new String[size];
 					HashSet<String> photoPathsInDB = new HashSet<String>();
 					cursor.moveToPosition(-1);
 					while(cursor.moveToNext()){
 						String filename = cursor.getString(1);
 						long cardId = cursor.getLong(2);
 						cardIds[cursor.getPosition()] = cardId;
 						photoFilenames[cursor.getPosition()] = filename;
 						photoPathsInDB.add(filename);
 					}
 					
 					/*DELETE PHOTO IN PHOTOIDS[]*/
 					new QueryRunner(myDB)
 						.execute(QueryString.getDeletePhotosWithFilenamesQuery(photoFilenames));
 					
 					/*DELETE CARD IN CARDIDS[]*/
 					new QueryRunner(myDB).execute(QueryString.getDeleteCardsQuery(cardIds));
 
 					/*DELETE FILES THAT DON'T HAVE DB ENTRIES*/
 					SBApplication.removeFiles(photoPathsInDB);
 				}
 				
 			}
 	   	}).execute(QueryString.getCardsWithPhotosForDecksQuery(deckIds));
 	}
 
     private void gotSections(Cursor result) {
     	_sections = new ArrayList<Section>();
     	_sectionIdMap = new HashMap<Long,Section>();
     	
 		if(result.getCount()>0){
 	    	result.moveToPosition(-1);
 			while(result.moveToNext()){
 				Long sectionId = result.getLong(0);
 				String sectionName = result.getString(1);
 				Long deckId = result.getLong(2);
 				String deckName = result.getString(3);
 				//build the dataStores
 				Section section = _sectionIdMap.get(sectionId);
 				if(section == null){
 					section = new Section(sectionId, sectionName);
 					_sectionIdMap.put(sectionId, section);
 					_sections.add(section);
 				}
 				Deck myDeck = new Deck(deckId,deckName);
 				section.addDeck(myDeck);//addDeck won't allow repeats..
 				//cursor row may have additional column when searching with matching card ids...
 				if(result.getColumnCount() > 4){
 					long cardId = result.getLong(4);
 					Log.d(TAG,Long.toString(sectionId)+','+sectionName+','+Long.toString(deckId)+','+deckName+','+Long.toString(cardId));
 					section.getDeckById(deckId).cards.add(new Card(cardId));
 				}
 			}
 		}
 		
 		_listViewAdapter = new ExpandableListAdapter(){
 			@Override public Object getGroup(int groupPosition) {return _sections.get(groupPosition);	}
 			@Override public Object getChild(int groupPosition, int childPosition) {return _sections.get(groupPosition).decks.get(childPosition);}
 			@Override public int getChildrenCount(int groupPosition) {	return _sections.get(groupPosition).decks.size();}
 			@Override public int getGroupCount() { return _sections.size();}
 			@Override public long getChildId(int groupPosition, int childPosition) {return _sections.get(groupPosition).decks.get(childPosition).id;}
 			@Override public long getGroupId(int groupPosition) { return _sections.get(groupPosition).id;}
 			@Override public long getCombinedChildId(long groupId, long childId) {return childId;}
 			@Override public long getCombinedGroupId(long groupId) { return groupId;}
 			@Override public boolean areAllItemsEnabled() { return true; }
 			@Override public boolean hasStableIds() {return true;}
 			@Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true;}
 			@Override public boolean isEmpty() {return _sections.isEmpty();}
 			
 			@Override public void onGroupCollapsed(int groupPosition) {}
 			@Override public void onGroupExpanded(int groupPosition) {}
 			@Override public void registerDataSetObserver(DataSetObserver observer) {}
 			@Override public void unregisterDataSetObserver(DataSetObserver observer) {}
 			
 			@Override
 			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 				LayoutInflater myInflator = LayoutInflater.from(getApplicationContext());
 				TextView item;
 				if(_deleting){
 					item = (TextView) myInflator.inflate(android.R.layout.simple_list_item_multiple_choice,null);
 				}else{
 					item = (TextView) myInflator.inflate(android.R.layout.simple_list_item_activated_1, null);
 				}
 				item.setMinHeight(96);
 				item.setText(((Deck)getChild(groupPosition,childPosition)).name);
 				item.setTextColor(Color.BLACK);
 				item.setBackgroundColor(Color.WHITE);
 //				item.getBackground().setAlpha(95);
 				return item;
 			}
 			
 			@Override
 			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 				Section sec = (Section) getGroup(groupPosition);
 				LayoutInflater myInflator = LayoutInflater.from(getApplicationContext());
 				TextView item = (TextView)myInflator.inflate(android.R.layout.simple_expandable_list_item_1,null);
 				item.setBackgroundColor(Color.GRAY);
 				item.setText(sec.name);
 				item.setTextColor(Color.WHITE);
 				return item;
 			}
 		};
 		expandListView();
 	}
     
 
     
     
     private void preformSearch() {  
 	   	/* RUN QUERY ON ANOTHER THREAD */
 		QueryRunner sectionsQuery = new QueryRunner(_myDB);
 	    sectionsQuery.setQueryRunnerListener(new QueryRunnerListener(){
 			@Override public void onPostExcecute(Cursor cards) {
 				gotSections(cards);
 			}
 		});
 	    sectionsQuery.execute(QueryString.getGroupedDeckQuery());
 	}
 
 	private void preformSearch(String searchString){
 		if(searchString.isEmpty()){
 			_searching = false;
 	    	preformSearch();
 		}else{
 			_searching = true;
 	    	QueryRunner sectionsQuery = new QueryRunner(_myDB);
 	        sectionsQuery.setQueryRunnerListener(new QueryRunnerListener(){
 				@Override public void onPostExcecute(Cursor cards) {
 					gotSections(cards);
 				}
 			});
 	        sectionsQuery.execute(QueryString.getSearchTermQuery(searchString));
 		}
 	}
 
 	private void expandListView(){
 		_listView.setAdapter(_listViewAdapter);
 		int count = _listView.getExpandableListAdapter().getGroupCount();
 		for (int position = 0; position < count; position++)
 		    _listView.expandGroup(position);
     }
     
     private void doneEditing(){
     	_deleting = false;
     	_editing = false;
         this._listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         this._listView.setItemsCanFocus(true);
         _listView.setAdapter(_listViewAdapter);
         expandListView();
     }
     
 	private AlertDialog deleteDecks() {
     	AlertDialog myDeleteConfirmationBox = new AlertDialog.Builder(this) 
     	//set message, title, and icon
     		.setTitle("Delete Card") 
     		.setMessage("Are you sure you want to delete these decks?") 
     		.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int whichButton) { 
     				long[] deckIds = _listView.getCheckedItemIds();
     				
     				/*DELETE THE PHOTOS, CARDS AND FILES OF THIS DECK*/
     				DecksActivity.deleteAssociationsForDeckIds(DecksActivity.this,deckIds);
     				
     				/*DELETE ACTUAL DECK*/
     				new QueryRunner(_myDB, new QueryRunnerListener(){
     					@Override public void onPostExcecute(Cursor cursor) {
     						QueryRunner deleteEmptySections = new QueryRunner(_myDB);
     						deleteEmptySections.setQueryRunnerListener(new QueryRunnerListener(){
     							@Override public void onPostExcecute(Cursor cursor) {
     								_listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
     								_listView.setItemsCanFocus(true);
 									doneEditing();
 									preformSearch();
     							}
     						});
     						deleteEmptySections.execute(QueryString.getRemoveEmptySectionsQuery());
 						}
    					}).execute(QueryString.getRemoveDecksWithIdsQuery(deckIds));
     			} 
 			})
 			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 					doneEditing();
 				}
 			})
 			.create();
     	return myDeleteConfirmationBox;
    }
 	
     private void runCreateDeckActivity(long deckId) {
     	Intent createDeckIntent = new Intent(DecksActivity.this,DeckFormActivity.class);
     	createDeckIntent.setFlags(
 			Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
             Intent.FLAG_ACTIVITY_NO_ANIMATION
         );
     	String[] sectionNames = new String[_sections.size()];
     	long[] sectionIds = new long[_sections.size()];
     	for(int idx = 0; idx < _sections.size(); idx++){
     		sectionNames[idx] = _sections.get(idx).name;
     		sectionIds[idx] = _sections.get(idx).id;
     	}
     	createDeckIntent.putExtra(DeckFormActivity.EXTRAS_SECTION_IDS, sectionIds);
     	createDeckIntent.putExtra(DeckFormActivity.EXTRAS_SECTION_NAMES, sectionNames);
     	if(deckId > 0){
     		createDeckIntent.putExtra(DeckFormActivity.EXTRAS_EDITING_DECK_ID, deckId);
     	}
     	startActivity(createDeckIntent);
 	}
 
 }
