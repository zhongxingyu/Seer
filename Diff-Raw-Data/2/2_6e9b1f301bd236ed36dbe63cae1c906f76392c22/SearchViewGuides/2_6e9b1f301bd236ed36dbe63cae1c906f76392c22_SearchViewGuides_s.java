 package com.adrguides;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.util.Log;
 import android.view.MenuItem;
 import android.widget.FilterQueryProvider;
 import android.widget.SearchView;
 import android.widget.SimpleCursorAdapter;
 
 import com.adrguides.model.Place;
 import com.adrguides.tts.TextToSpeechSingleton;
 
 import java.util.ArrayList;
 
 /**
  * Created by adrian on 29/08/13.
  */
 public class SearchViewGuides {
 
     public SearchViewGuides(final Context context, final MenuItem menuitem) {
 
         final SearchView searchView = (SearchView) menuitem.getActionView();
         // Configure the search info and add any event listeners
 
         menuitem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
             @Override
             public boolean onMenuItemActionExpand(MenuItem menuItem) {
                 String[] from = {"text"};
                 int[] to = {android.R.id.text1};
 
                 SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_activated_1, getSuggestionsCursor(null), from, to, 0);
                 adapter.setFilterQueryProvider(new FilterQueryProvider() {
                     public Cursor runQuery(CharSequence constraint) {
                        return getSuggestionsCursor(new String(constraint.toString()));
                     }
                 });
                 searchView.setSuggestionsAdapter(adapter);
                 return true;
             }
 
             @Override
             public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                 return true;
             }
         });
 
 
         searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
             @Override
             public boolean onSuggestionSelect(int i) {
                 return false;
             }
 
             @Override
             public boolean onSuggestionClick(int i) {
                 Cursor row = (Cursor) searchView.getSuggestionsAdapter().getItem(i);
                 searchView.setQuery(row.getString(2), true);
                 return true;
             }
         });
 
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String s) {
 
                 if (s == null) {
                     return false;
                 }
 
                 Log.d("com.adrguides.SearchViewGuides", "man submitao");
                 Place[] places = TextToSpeechSingleton.getInstance().getGuide().getPlaces();
                 for(int i = 0; i < places.length; i++){
                     Place item = places[i];
                     if ((item.getId() != null && item.getId().equals(s)) ||
                             item.getTitle().toLowerCase().equals(s.toLowerCase()) ||
                             item.getVisibleLabel().toLowerCase().equals(s.toLowerCase())) {
 
                         TextToSpeechSingleton.getInstance().gotoChapter(i);
                         menuitem.collapseActionView();
                         return true;
                     }
                 }
                 return false ;// true if the query has been handled by the listener, false to let the SearchView perform the default action.
             }
             @Override
             public boolean onQueryTextChange(String s) {
                 return false; // false if the SearchView should perform the default action of showing any suggestions if available, true if the action was handled by the listener.
             }
         });
     }
 
 
     private Cursor getSuggestionsCursor(String filter) {
         Log.d("com.adrguides.SearchViewGuides",  "man filtrao");
 
         String[] columnNames = {"_id", "_title", "text"};
         MatrixCursor cursor = new MatrixCursor(columnNames);
         String[] temp = new String[3];
         int id = 0;
         for(Place item : TextToSpeechSingleton.getInstance().getGuide().getPlaces()){
 
             if (filter == null ||
                     (item.getId() != null && item.getId().contains(filter)) ||
                     item.getTitle().toLowerCase().contains(filter.toLowerCase())) {
                 temp[0] = item.getId();
                 temp[1] = item.getTitle();
                 temp[2] = item.getVisibleLabel();
                 cursor.addRow(temp);
             }
         }
 
         return cursor;
     }
 }
