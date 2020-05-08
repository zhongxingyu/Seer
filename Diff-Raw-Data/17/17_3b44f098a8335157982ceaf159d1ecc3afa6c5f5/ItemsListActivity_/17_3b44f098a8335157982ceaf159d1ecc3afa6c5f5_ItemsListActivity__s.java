 package org.vimeoid.activity.base;
 
 import org.vimeoid.R;
 import org.vimeoid.activity.base.ListApiTask_.Reactor;
 import org.vimeoid.util.Dialogs;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.os.AsyncTask.Status;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public abstract class ItemsListActivity_<ItemType, AdapterType extends BaseAdapter, Params, Result> extends ListActivity {
     
     public static final String TAG = "ItemsListActivity_";
     
     private View emptyView;    
     private View titleBar;
     private View progressBar;
     private View footerView;
     
     private final int mainView;
     private final int contextMenu;
     
     private AdapterType adapter;
     
     private ListApiTask_<Params, Result> mainTask;
     private final Reactor<Params, Result> mainReactor;
     private boolean needMorePages = true;
     private Params taskParams = null;
     
     public ItemsListActivity_(int mainView, int contextMenu) {
     	this.mainView = mainView;
     	this.contextMenu = contextMenu;
     	
     	mainReactor = new ListReactor();
     }
     
     public ItemsListActivity_(int contextMenu) {
     	this(R.layout.generic_list, contextMenu);
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
                 
         Log.d(TAG, "Starting items view");
         
         setContentView(mainView);
         
         collectExtras(getIntent().getExtras());
 
         final ListView listView = getListView();
         listView.setTextFilterEnabled(true);
         listView.setItemsCanFocus(true);                
         registerForContextMenu(listView);
         
         emptyView = findViewById(R.id.emptyView);
         listView.setEmptyView(emptyView);
 
         footerView = getLayoutInflater().inflate(R.layout.item_footer_load_more, null);
         listView.addFooterView(footerView);        
         
         progressBar = findViewById(R.id.progressBar);
         progressBar.setVisibility(View.GONE);           
                 
         this.adapter = createAdapter();
         setListAdapter(adapter);
         
         taskParams = collectTaskParams(getIntent().getExtras());        
         
         titleBar = findViewById(R.id.titleBar);
         initTitleBar((ImageView)titleBar.findViewById(R.id.subjectIcon),
                      (TextView)titleBar.findViewById(R.id.subjectTitle),
                      (ImageView)titleBar.findViewById(R.id.resultIcon));        
         
         loadNextPage(adapter);
         
     }   
     
     protected String getContextMenuTitle(int position) { 
         return getString(R.string.context_menu); 
     };
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         
         final int position = extractPosition(menuInfo);
         
         Log.d(TAG, "Opening context menu for item at " + position);
         
         if (isLoadMoreButton(position)) return; 
         
        menu.setHeaderTitle(getContextMenuTitle(position));
             
        MenuInflater inflater = getMenuInflater(); //from activity
        inflater.inflate(contextMenu, menu);
     }
     
     @Override
     protected final void onListItemClick(ListView l, View v, int position, long id) {
 
         if (!isLoadMoreButton(position)) {
             Log.d(TAG, "item at position " + position + " (" + getListView().getCount() + ") with id " + id + ", view id " + v.getId() + " is clicked");
             onItemSelected(getItem(position));        
         } else {
             if ((mainTask != null) && Status.RUNNING.equals(mainTask.getStatus())) {
                 Dialogs.makeLongToast(this, getString(R.string.please_do_not_touch));
             } else {
                 loadNextPage(adapter);
             }
         }
         
         super.onListItemClick(l, v, position, id);
         
     }
     
     /* private void checkConnectionAndQueryMoreItems(AdapterType adapter, int pageNum) {
         
         if (VimeoApi.connectedToWeb(this) && VimeoApi.vimeoSiteReachable(this)) {
             Log.d(TAG, "Connection test is passed OK");
             queryMoreItems(adapter, pageNum);
         } else {
             Log.d(TAG, "Connection test failed");            
             Dialogs.makeToast(this, getString(R.string.no_iternet_connection));
         }
         
     } */
     
     protected void loadNextPage(AdapterType adapter) {
         if (!needMorePages) return;
         if (mainTask == null) {
             mainTask = prepareListTask(mainReactor, adapter);
             mainTask.setFilter(new ListApiTask_.FalseFilter<Result>());
         }
         
         executeTask(mainTask, taskParams);
     }
     
     protected final boolean isLoadMoreButton(int position) {
         return (position == (getListView().getCount() - 1));
     }
     
     protected final int extractPosition(MenuItem item) {
         return extractPosition(item.getMenuInfo());
     }
     
     protected final int extractPosition(ContextMenuInfo info) {
         try {
             return ((AdapterView.AdapterContextMenuInfo) info).position;
         } catch (ClassCastException cce) {
             Log.e(TAG, "incorrect menu info", cce);
             return -1;
         }
         
     }
     
     @SuppressWarnings("unchecked")
     protected final ItemType getItem(int position) {
         return (ItemType)getListView().getItemAtPosition(position);
     }
     
     protected abstract AdapterType createAdapter();    
     protected abstract void initTitleBar(ImageView subjectIcon, TextView subjectTitle, ImageView resultIcon);
     
     protected abstract Params collectTaskParams(Bundle extras);
     protected abstract ListApiTask_<Params, Result> prepareListTask(Reactor<Params, Result> reactor, AdapterType adapter);
     protected abstract void executeTask(ListApiTask_<Params, Result> task, Params params); // get rid of
     
     protected void collectExtras(Bundle extras) { };
     protected void onItemSelected(ItemType item) { };    
     protected void whenPageReceived(Result page) { };
     protected void onListTaskComplete() { };
     
     
     protected void showProgressBar() {
         progressBar.setVisibility(View.VISIBLE);
     }    
     
     protected void hideProgressBar() {
         progressBar.setVisibility(View.GONE);
     }
     
     protected void setToLoadingState() {
         showProgressBar();
 
         footerView.setEnabled(false);
         
         final TextView footerText = (TextView) footerView.findViewById(R.id.itemsListFooterText);
         final TextView emptyText = (TextView) emptyView.findViewById(R.id.itemsEmptyListView);
         final ImageView emptyImage = (ImageView) emptyView.findViewById(R.id.itemsEmptyListImage);        
         
         footerText.setTextColor(getResources().getColor(R.color.load_more_disabled_text));
         footerText.setBackgroundResource(R.color.load_more_disabled_bg);
         footerText.setText(R.string.loading);            
         
         emptyImage.setImageResource(R.drawable.item_loading_small);            
         emptyText.setText(R.string.loading);        
         
     }
     
     protected void setToNoItemsInList() {
         final TextView emptyText = (TextView) emptyView.findViewById(R.id.itemsEmptyListView);
         final ImageView emptyImage = (ImageView) emptyView.findViewById(R.id.itemsEmptyListImage);
         
         getListView().removeFooterView(footerView);
         emptyImage.setImageResource(R.drawable.no_more_small);
         emptyText.setText(R.string.no_items_in_list);
     }
     
     protected void setToNoItemsMore() {
         footerView.setVisibility(View.GONE);        
     }
     
     protected void setToTheresMoreItems() {
         final TextView footerText = (TextView) footerView.findViewById(R.id.itemsListFooterText);
         
         footerView.setEnabled(true);
         footerText.setTextColor(getResources().getColor(R.color.load_more_default_text));
         footerText.setBackgroundResource(R.color.load_more_default_bg); 
         footerText.setText(R.string.load_more);        
     }
     
     protected AdapterType getAdapter() { return adapter; }
     
     protected class ListReactor implements Reactor<Params, Result> {
         
         @Override public void beforeRequest() {
             Log.d(TAG + " reactor", "before request");
             setToLoadingState();
         }
 
         @Override public void onNextPageExists() {
             Log.d(TAG + " reactor", "next page exists");
             setToTheresMoreItems();                    
         }
 
         @Override public void onNoItems() {
             Log.d(TAG + " reactor", "no items in list");
             setToNoItemsInList();
         }
 
         @Override public void onNoMoreItems() {
             Log.d(TAG + " reactor", "no items mpore");
             setToNoItemsMore();
         }
 
         @Override public void onError(Exception e, String message) {
             hideProgressBar();
             Log.e(TAG, message);
             
             Dialogs.makeExceptionToast(ItemsListActivity_.this, message, e);
         }
 
         @Override
         public void afterRequest(Result result, int received, boolean receivedAll, 
                                  ListApiTask_<Params, Result> nextPageTask) {
             
             Log.d(TAG + " reactor", "received " + received + "; received all : " + receivedAll);            
             
             hideProgressBar();
             
             whenPageReceived(result);            
             
             needMorePages = !receivedAll;
             mainTask = nextPageTask;                
             
             onContentChanged();
             
             // TODO: scroll to the first received item (smoothScrollToPosition in API 8)
             final int newPos = getListView().getCount() - received - 2; // - 'load more' and one position before
             if (newPos >= 0) setSelection(newPos);
             else setSelection(0);
             
             if (receivedAll) onListTaskComplete();
             
         }
         
     }
     
     
 }
