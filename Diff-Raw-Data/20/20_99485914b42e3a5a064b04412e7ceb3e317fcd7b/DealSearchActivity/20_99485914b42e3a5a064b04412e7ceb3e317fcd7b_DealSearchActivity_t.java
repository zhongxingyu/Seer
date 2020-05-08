 package net.bensdeals.activity;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Pair;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.google.inject.Inject;
 import net.bensdeals.R;
 import net.bensdeals.adapter.SearchAdapter;
 import net.bensdeals.listener.OnSearchListener;
 import net.bensdeals.model.wrapper.SearchResponseWrapper;
 import net.bensdeals.network.callbacks.TaskCallback;
 import net.bensdeals.network.core.RemoteTask;
 import net.bensdeals.network.request.SearchRequest;
 import net.bensdeals.utils.IntentUtil;
 import net.bensdeals.views.EmptyContentView;
 import net.bensdeals.views.SearchEditView;
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 import roboguice.util.Strings;
 
 import java.util.List;
 
 import static net.bensdeals.utils.IntentExtra.PREFIX_EXTRA;
 import static net.bensdeals.utils.Reporter.KEY_SEARCH_KEYWORDS;
 import static net.bensdeals.utils.Reporter.ON_SEARCH;
 import static net.bensdeals.utils.Reporter.ON_SEARCH_VIEW;
 
 public class DealSearchActivity extends BaseActivity implements AdapterView.OnItemClickListener {
     @InjectView(R.id.deal_search_list_view) ListView listView;
     @InjectResource(R.string.shopping_api_key) String key;
     @Inject SearchEditView editView;
     @Inject EmptyContentView footer;
     @Inject SearchAdapter adapter;
     @Inject RemoteTask remoteTask;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.deal_search_layout);
         String stringExtra = getIntent().getStringExtra(PREFIX_EXTRA);
         editView.setEditText(stringExtra);
         editView.setOnSearchListener(new SearchListener());
         listView.addHeaderView(editView, null, false);
         listView.addFooterView(footer, null, false);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(this);
         reporter.report(ON_SEARCH_VIEW, Pair.create(KEY_SEARCH_KEYWORDS, stringExtra));
     }
 
     public static Intent intentFor(Context context, String prefix) {
         return new Intent(context, DealSearchActivity.class).putExtra(PREFIX_EXTRA, prefix);
     }
 
     @Override
     public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SearchResponseWrapper.SearchItemWrapper item = adapter.getItem(Math.min(adapter.getCount() - 1, i));
         if (item != null && !Strings.isEmpty(item.item.getLink())) {
             IntentUtil.intentForWeb(this, item.item.getLink());
         }
     }
 
     private class SearchListener implements OnSearchListener {
         @Override
         public void onSearch(final String searchText) {
             createLoadingDialog(getString(R.string.searching));
             remoteTask.makeRequest(new SearchRequest(key, searchText), new TaskCallback<SearchResponseWrapper>() {
                 @Override
                 public void onTaskSuccess(SearchResponseWrapper response) {
                     super.onTaskSuccess(response);
                     List<SearchResponseWrapper.SearchItemWrapper> items = response.getItems();
                     if (!items.isEmpty()) {
                         footer.hide();
                         adapter.replaceAll(items);
                     } else {
                         footer.show();
                     }
                 }
 
                 @Override
                 public void onTaskFailed() {
                     new AlertDialog.Builder(DealSearchActivity.this).setMessage(getString(R.string.fail_to_load, "search results")).create().show();
                 }
 
                 @Override
                 public void onTaskComplete() {
                     if (dialog != null && dialog.isShowing()) dialog.dismiss();
                 }
             });
             reporter.report(ON_SEARCH, Pair.create(KEY_SEARCH_KEYWORDS, searchText));
         }
     }
 }
