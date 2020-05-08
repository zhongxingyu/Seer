 package com.moac.android.opensecretsanta.fragment;
 
 import android.app.Fragment;
 import android.app.SearchManager;
 import android.app.SearchableInfo;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.SearchView;
 import com.moac.android.opensecretsanta.R;
 
 import java.util.List;
 
 public class AddMemberFragment extends Fragment {
 
     private static final String TAG = AddMemberFragment.class.getSimpleName();
 
     SearchView mSearchView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.add_member_fragment, container, false);
 
         mSearchView = (SearchView)view.findViewById(R.id.memberSearchView);
         SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
         if (searchManager != null) {
             Log.i(TAG, "onCreateView() - setting SearchAbleInfo. getComponentName() = " + getActivity().getComponentName());
             setupSearchView(mSearchView, mQueryTextListener, mSuggestionListener, null);
         }
         return view;
     }
     SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
         @Override
         public boolean onQueryTextSubmit(String query) {
             Log.i(TAG, "onQueryTextSubmit() - query: " + query);
             return true;
         }
 
         @Override
         public boolean onQueryTextChange(String newText) {
             Log.i(TAG, "onQueryTextChange() - newText: " + newText);
             return false;
         }
     };
 
     SearchView.OnSuggestionListener mSuggestionListener = new SearchView.OnSuggestionListener() {
         @Override
         public boolean onSuggestionSelect(int position) {
             Log.i(TAG, "onSuggestionSelect() - position: " + position);
             return false;
         }
 
         @Override
         public boolean onSuggestionClick(int position) {
             Log.i(TAG, "onSuggestionClick() - position: " + position);
             Object item = mSearchView.getSuggestionsAdapter().getItem(position);
            Log.i(TAG, "onSuggestionClick() - item: "  + item);

             return true;
         }
     };
 
     private void setupSearchView(SearchView _searchView, SearchView.OnQueryTextListener _queryTextListener,
                                  SearchView.OnSuggestionListener _suggestionListener, CursorAdapter _suggestionsAdapter) {
         SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
         if (searchManager != null) {
             // Use the "contacts" global search provider
             SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
             Log.i(TAG, "setupSearchView() - info: " + info);
             if (info != null) {
                Log.i(TAG, "setupSearchView() - Found match for search suggest authority");
                _searchView.setSearchableInfo(info);
             }
 
         }
         _searchView.setOnQueryTextListener(_queryTextListener);
         _searchView.setOnSuggestionListener(_suggestionListener);
     //    _searchView.setSuggestionsAdapter(_suggestionsAdapter);
     }
 }
