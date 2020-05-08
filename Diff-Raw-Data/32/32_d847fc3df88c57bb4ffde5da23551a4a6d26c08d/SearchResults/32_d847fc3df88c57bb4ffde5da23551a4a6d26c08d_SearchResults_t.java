 package no.group09.utils;
 
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class SearchResults extends ListActivity{
 
 	@Override
 	
     public void onCreate(Bundle savedInstanceState) {
         
         handleIntent(getIntent());
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         
         handleIntent(intent);
     }
 
     private void handleIntent(Intent intent) {
 
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             String query = intent.getStringExtra(SearchManager.QUERY);
             doSearch(query);
         }
     }
 
 	private void doSearch(String query) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
