 // @formatter:off
 /*
  * Copyright 2012 GitHub Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 // @formatter:on
 
 package org.level28.android.moca.ui;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.level28.android.moca.ExceptionLoader;
 import org.level28.android.moca.R;
 import org.level28.android.moca.util.ViewUtils;
 
 import android.app.Activity;
 import android.app.Application;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * Base fragment for displaying a list of items that loads with a progress bar
  * visible.
  * 
  * @param <E>
  *            type of the item
  * @author GitHub Inc.
  */
 public abstract class ItemListFragment<E> extends SherlockFragment implements
         LoaderCallbacks<List<E>> {
 
     private static final String FORCE_REFRESH = "forceRefresh";
 
     /**
      * Check if the bundle requested a forced refresh of the items
      * 
      * @param args
      *            bundle passed to the loader by the LoaderManager
      * @return {@code true} if the bundle indicates that a forced refresh should
      *         take place
      */
     protected static boolean isForceRefresh(Bundle args) {
         return args != null && args.getBoolean(FORCE_REFRESH, false);
     }
 
     /**
      * List items provided to {@link #onLoadFinished(Loader, List)}
      */
     protected List<E> items = Collections.emptyList();
 
     /**
      * List view
      */
     protected ListView listView;
 
     /**
      * Empty view
      */
     protected TextView emptyView;
 
     /**
      * Progress bar
      */
     protected ProgressBar progressBar;
 
     /**
      * Loader identifier
      */
     protected int loaderId = 0;
 
     /**
      * Is the list currently shown?
      */
     protected boolean listShown;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         if (!items.isEmpty()) {
             setListShown(true, false);
         }
 
         getLoaderManager().initLoader(loaderId, null, this);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         return inflater.inflate(R.layout.item_list, null);
     }
 
     @Override
     public void onDestroyView() {
         // Clear all references
         listShown = false;
         emptyView = null;
         progressBar = null;
         listView = null;
 
         super.onDestroyView();
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
 
         listView = (ListView) view.findViewById(android.R.id.list);
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 onListItemClick((ListView) parent, view, position, id);
             }
         });
 
         progressBar = (ProgressBar) view.findViewById(R.id.progressLoading);
 
         emptyView = (TextView) view.findViewById(android.R.id.empty);
 
         configureList(getActivity(), getListView());
     }
 
     /**
      * Configure list after view has been created.
      * 
      * @param activity
      * @param listView
      */
     protected void configureList(Activity activity, ListView listView) {
         listView.setAdapter(createAdapter(items));
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         // TODO Auto-generated method stub
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (!isUsable()) {
             return false;
         }
 
         switch (item.getItemId()) {
         case R.id.menu_refresh:
             forceRefresh();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Ask the loader to perform a forced refresh of this fragment.
      */
     protected void forceRefresh() {
         Bundle bundle = new Bundle();
         bundle.putBoolean(FORCE_REFRESH, true);
         refresh(bundle);
     }
 
     /**
      * Ask the loader to refresh this fragment if there is new data.
      */
     public void refresh() {
         refresh(null);
     }
 
     private void refresh(final Bundle args) {
         if (!isUsable()) {
             return;
         }
 
         getLoaderManager().restartLoader(loaderId, args, this);
     }
 
     /**
      * Get an error message to display for an exception.
      * 
      * @param exception
      * @return a string resource id
      */
     protected abstract int getErrorMessage(Exception exception);
 
     @Override
     public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
         final Exception exception = getException(loader);
         if (exception != null) {
             showError(exception, getErrorMessage(exception));
             showList();
             return;
         }
 
         this.items = items;
         getListAdapter().setItems(items.toArray());
         showList();
     }
 
     /**
      * Display a {@link Toast} after an exception occurred in background.
      * <p>
      * Subclasses may want to override this method to change the exception
      * reporting method.
      * 
      * @param exception
      *            the exception thrown by the loader
      * @param messageResId
      *            string resource id returned by
      *            {@link #getErrorMessage(Exception)}
      */
     protected void showError(final Exception exception, final int messageResId) {
         final Activity activity = getActivity();
         final Application application = activity.getApplication();
         final String message = activity.getResources().getString(messageResId);
 
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(application, message, Toast.LENGTH_LONG).show();
             }
         });
     }
 
     /**
      * Get exception from loader if it provides one by being a
      * {@link ThrowableLoader}
      * 
      * @param loader
      * @return exception or null if none provided
      */
     protected Exception getException(final Loader<List<E>> loader) {
         if (loader instanceof ExceptionLoader) {
             return ((ExceptionLoader<List<E>>) loader).clearException();
         } else {
             return null;
         }
     }
 
     /**
      * Create an {@link ItemListAdapter} for this fragment.
      * 
      * @param items
      *            initial contents backing the adapter
      */
     protected abstract ItemListAdapter<E, ? extends ItemView> createAdapter(
             final List<E> items);
 
     /**
      * Unconditionally set the list as visible.
      */
     protected void showList() {
         setListShown(true, isResumed());
     }
 
     @Override
     public void onLoaderReset(Loader<List<E>> loader) {
         // This space intentionally left blank
     }
 
     /**
      * Hide the list and force a refresh showing the progress bar.
      */
     protected void refreshWithProgress() {
         items.clear();
         setListShown(false);
         refresh();
     }
 
     /**
      * Get the {@link ListView} backing this fragment.
      */
     public ListView getListView() {
         return listView;
     }
 
     /**
      * Get the {@link ItemListAdapter} bound to this fragment's {@link ListView}
      */
     @SuppressWarnings("unchecked")
     protected ItemListAdapter<E, ? extends ItemView> getListAdapter() {
         if (listView != null) {
             return (ItemListAdapter<E, ? extends ItemView>) listView
                     .getAdapter();
         } else {
             return null;
         }
     }
 
     /**
      * Bind this fragment to a new {@link ListAdapter}.
      * 
      * @param adapter
      *            the new adapter
      */
     protected ItemListFragment<E> setListAdapter(final ListAdapter adapter) {
         if (listView != null) {
             listView.setAdapter(adapter);
         }
         return this;
     }
 
     private ItemListFragment<E> fadeIn(final View view, final boolean animate) {
        ViewUtils.fadeIn(getActivity(), view, animate);
         return this;
     }
 
     private ItemListFragment<E> show(final View view) {
         ViewUtils.setGone(view, false);
         return this;
     }
 
     private ItemListFragment<E> hide(final View view) {
         ViewUtils.setGone(view, true);
         return this;
     }
 
     /**
      * Set list shown or progress bar show
      * 
      * @param shown
      * @return this fragment
      */
     public ItemListFragment<E> setListShown(final boolean shown) {
         return setListShown(shown, true);
     }
 
     /**
      * Set list shown or progress bar show
      * 
      * @param shown
      * @param animate
      * @return this fragment
      */
     public ItemListFragment<E> setListShown(final boolean shown,
             final boolean animate) {
         if (!isUsable())
             return this;
 
         if (shown == listShown) {
             if (shown)
                 // List has already been shown so hide/show the empty view with
                 // no fade effect
                 if (items.isEmpty())
                     hide(listView).show(emptyView);
                 else
                     hide(emptyView).show(listView);
             return this;
         }
 
         listShown = shown;
 
         if (shown)
             if (!items.isEmpty())
                 hide(progressBar).hide(emptyView).fadeIn(listView, animate)
                         .show(listView);
             else
                 hide(progressBar).hide(listView).fadeIn(emptyView, animate)
                         .show(emptyView);
         else
             hide(listView).hide(emptyView).fadeIn(progressBar, animate)
                     .show(progressBar);
 
         return this;
     }
 
     /**
      * Set empty text on list fragment
      * 
      * @param message
      * @return this fragment
      */
     protected ItemListFragment<E> setEmptyText(final String message) {
         if (emptyView != null)
             emptyView.setText(message);
         return this;
     }
 
     /**
      * Set empty text on list fragment
      * 
      * @param resId
      * @return this fragment
      */
     protected ItemListFragment<E> setEmptyText(final int resId) {
         if (emptyView != null)
             emptyView.setText(resId);
         return this;
     }
 
     /**
      * Callback when a list view item is clicked
      * 
      * @param l
      * @param v
      * @param position
      * @param id
      */
     public void onListItemClick(ListView l, View v, int position, long id) {
     }
 
     /**
      * Is this fragment still part of an activity and usable from the UI-thread?
      * 
      * @return true if usable on the UI-thread, false otherwise
      */
     protected boolean isUsable() {
         return getActivity() != null;
     }
 }
