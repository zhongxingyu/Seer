 /*
  * Copyright (C) 2013 Jack Wakefield
  *
  * This file is part of TorrentFreak Reader.
  *
  * TorrentFreak Reader is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TorrentFreak Reader is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TorrentFreak Reader.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.torrentfreak.reader.free.fragments;
 
 import java.lang.Exception;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff.Mode;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import com.commonsware.cwac.endless.EndlessAdapter;
 import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
 import com.google.inject.Inject;
 import com.torrentfreak.reader.free.adapters.ArticleListAdapter;
 import com.torrentfreak.reader.free.articles.ArticleItem;
 import com.torrentfreak.reader.free.articles.ArticleStorage;
 import com.torrentfreak.reader.free.articles.providers.ArticleListProvider;
 import com.torrentfreak.reader.free.categories.CategoryItem;
 import com.torrentfreak.reader.free.categories.CategoryManager;
 import com.torrentfreak.reader.free.R;
 import com.torrentfreak.reader.free.SettingsActivity;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import roboguice.inject.InjectView;
 
 public class ArticleListFragment extends RoboSherlockListFragment {
     /**
      * The saved state key used to retain the current page.
      */
     private static final String SAVED_STATE_PAGE = "page";
 
     /**
      * The saved state key used to retain the article list.
      */
     private static final String SAVED_STATE_ARTICLES = "articles";
 
     /**
      * The category manager.
      */
     @Inject
     private CategoryManager categoryManager;
 
     /**
      * The article storage database.
      */
     @Inject
     private ArticleStorage articleStorage;
 
     /**
      * The list view used to display the article list.
      */
     @InjectView(android.R.id.list)
     private ListView listView;
 
     /**
      * The weak reference to the article selected listener.
      */
     private WeakReference<OnArticleSelectedListener> listenerReference;
 
     /**
      * The layout inflator service.
      */
     private LayoutInflater layoutInflater;
 
     /**
      * The article list adapter.
      */
     private EndlessArticleAdapter adapter;
 
     /**
      * The article list provider.
      */
     private ArticleListProvider articleProvider;
 
     /**
      * The list of retrieved articles.
      */
     private ArrayList<ArticleItem> articles;
 
     /**
      * The current page.
      */
     private int page;
 
     public ArticleListFragment() {
         articles = new ArrayList<ArticleItem>();
         page = 0;
     }
 
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
         final Bundle savedInstanceState) {
         // inflate the article list fragment
         return inflater.inflate(R.layout.fragment_article_list, null);
     }
 
     public void onActivityCreated(final Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         // hide the list divider displayed between list items
         listView.setDividerHeight(0);
 
         layoutInflater =
             (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         // determine whether a saved instance state exists, indicating the fragment has existed
         // previously
         if (savedInstanceState != null) {
             // retrieve the retained page and article list
             page = savedInstanceState.getInt(SAVED_STATE_PAGE);
             articles = savedInstanceState.getParcelableArrayList(SAVED_STATE_ARTICLES);
         }
 
         // create and set the article list adapter
         adapter = new EndlessArticleAdapter();
         setListAdapter(adapter);
 
         // retrieve the selected category and create the article list provider
        final CategoryItem category = categoryManager.getSelectedCategory();
        articleProvider = category.createProvider();
     }
 
     @Override
     public void onSaveInstanceState(final Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
 
         // save the page and article list to be retrieved when the fragment is recreated
         savedInstanceState.putInt(SAVED_STATE_PAGE, page);
         savedInstanceState.putParcelableArrayList(SAVED_STATE_ARTICLES, articles);
     }
 
     @Override
     public void onAttach(final Activity activity) {
         super.onAttach(activity);
 
         try {
             // attempt to assign the listener reference from the parent activity
             listenerReference = new WeakReference<OnArticleSelectedListener>(
                     (OnArticleSelectedListener)activity);
         } catch (final ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement OnArticleSelectedListener");
         }
     }
 
     @Override
     public void onListItemClick(final ListView listView, final View view, final int position,
         final long id) {
         // retrieve the listener from the weak reference
         final OnArticleSelectedListener listener = listenerReference.get();
 
         // ensure the listener still exists
         if (listener != null) {
             final ArticleItem article = articles.get(position);
             final SharedPreferences preferences =
                 PreferenceManager.getDefaultSharedPreferences(getActivity());
 
             // determine whether the user wishes to mark articles as read once viewed
             if (preferences.getBoolean(SettingsActivity.SETTING_MARK_AS_READ, true)) {
                 // set the article as read and inform the adapter the data has changed
                 article.setRead(true);
                 adapter.notifyDataSetChanged();
             }
 
             // inform the listener an article has been selected
             listener.onArticleSelected(article);
         }
     }
 
     public interface OnArticleSelectedListener {
         void onArticleSelected(final ArticleItem article);
     }
 
     public class EndlessArticleAdapter extends EndlessAdapter {
         private List<ArticleItem> retrievedArticles;
 
         public EndlessArticleAdapter() {
             super(new ArticleListAdapter(getActivity(), articles));
             retrievedArticles = new ArrayList<ArticleItem>();
         }
 
         @Override
         protected void appendCachedData() {
             // loop through each retrieved article storing it in the article database and adding it
             // to the article list
             for (int i = 0; i < retrievedArticles.size(); i++) {
                 ArticleItem article = retrievedArticles.get(i);
 
                 // set the page order of the article
                 article.setOrder(i);
 
                 // save the article details to the database and add it to the article list
                 articles.add(articleStorage.saveArticleDetails(article));
             }
         }
 
         @Override
         protected boolean onException(View pendingView, Exception ex) {
             // set the notification duration to infinite
             int duration = Style.DURATION_INFINITE;
 
             // if articles have already been added, set the duration to 5 seconds
             if (articles.size() > 0) {
                 duration = 5000;
             }
 
             final Style style = new Style.Builder().setDuration(duration)
                 .setBackgroundColorValue(getResources().getColor(R.color.crouton_error))
                 .setHeight(LayoutParams.WRAP_CONTENT).build();
 
             // remove all previous notifications and show the error
             Crouton.cancelAllCroutons();
             Crouton.makeText(getActivity(),
                 "Failed to retrieve articles - " + ex.getMessage(), style,
                 R.id.article_list_layout).show();
 
             return false;
         }
 
         @Override
         public View getPendingView(final ViewGroup parent) {
             // inflate the pending view
             View pendingView = layoutInflater.inflate(R.layout.loading_article_list, parent, false);
 
             // retrieve the progress bar and set a colour filter to change the progress bar colour
             // to pink
             final ProgressBar progressBar = (ProgressBar)pendingView.findViewById(R.id.progress);
             progressBar.getIndeterminateDrawable().setColorFilter(
                 getResources().getColor(R.color.progress_bar_filter), Mode.MULTIPLY);
 
             return pendingView;
         }
 
         @Override
         protected boolean cacheInBackground() throws Exception {
             // remove the previously retrieves articles
             retrievedArticles.clear();
 
             if (articleProvider != null) {
                 // increase the page number
                 page++;
 
                 try {
                     // set the current page and attempt to retrieve the next set of articles
                     // adding them to the retrieved articles list
                     articleProvider.setPage(page);
                     retrievedArticles.addAll(articleProvider.fetch());
                 } catch (Exception ex) {
                     // attempt to retrieve stored articles for the selected category
                    final CategoryItem category = categoryManager.getSelectedCategory();
                    final List<ArticleItem> cachedArticles =
                        articleStorage.getArticlesByCategory(category, page);
 
                     // determine whether any cached articles exist
                     if (cachedArticles != null && cachedArticles.size() > 0) {
                         // if cached articles do exist, add them to the retrieved article list
                         // and continue as if they were retrieved from the website
                         retrievedArticles.addAll(cachedArticles);
                     } else {
                         // throw the exception to be handled by onException
                         throw ex;
                     }
                 }
             }
 
             return true;
         }
     }
 }
