 package net.bensdeals.activity;
 
 import android.content.Intent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import com.google.inject.Inject;
 import net.bensdeals.R;
 import net.bensdeals.adapter.SearchAdapter;
 import net.bensdeals.support.RobolectricTestRunnerWithInjection;
 import net.bensdeals.utils.TestRemoteTask;
 import net.bensdeals.views.SearchEditView;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import roboguice.inject.InjectView;
 
 import java.util.List;
 
 import static com.pivotallabs.robolectricgem.expect.Expect.expect;
 import static com.xtremelabs.robolectric.Robolectric.shadowOf;
 
 @RunWith(RobolectricTestRunnerWithInjection.class)
 public class DealSearchActivityTest {
     @InjectView(R.id.deal_search_list_view) ListView listView;
     @Inject DealSearchActivity activity;
     @Inject TestRemoteTask remoteTask;
     EditText searchEdit;
     public final String prefix = "This is a search text &amp;";
 
     @Before
     public void setup() throws Exception {
         Intent intent = DealSearchActivity.intentFor(activity, prefix);
         activity.setIntent(intent);
         activity.onCreate(null);
         searchEdit = (EditText) activity.findViewById(R.id.search_edittext);
     }
 
     @Test
     public void shouldSetSearchStringOnEditText() throws Exception {
         expect(searchEdit).toHaveHint("Search");
         expect(searchEdit).toHaveText(prefix);
     }
 
     @Test
     public void shouldAddSearchEditHeader() throws Exception {
         List<View> header = shadowOf(listView).getHeaderViews();
         expect(header.size()).toEqual(1);
         expect(header.get(0)).toBeInstanceOf(SearchEditView.class);
     }
 
     @Test
     public void shouldSetAdapter() throws Exception {
         expect(listView.getAdapter()).toBeInstanceOf(SearchAdapter.class);
     }
 
     @Test
     public void tappingListItem_shouldLaunchWebBrowser() throws Exception {
         doSearch(prefix);
         remoteTask.simulateSuccessResponse("search_response.json");
         expect(listView.getAdapter().getCount()).toEqual(25);
        listView.performItemClick(null, 1, 0);
        Intent intent = shadowOf(activity).peekNextStartedActivity();
        expect(intent).not.toBeNull();
        expect(intent.getAction()).toEqual(Intent.ACTION_VIEW);
    }

    @Test
    public void tappingListItem_shouldLaunchWebBrowserIfCountIsGreater() throws Exception {
        doSearch(prefix);
        remoteTask.simulateSuccessResponse("search_response.json");
        expect(listView.getAdapter().getCount()).toEqual(25);
        listView.performItemClick(null, 25, 0);

         Intent intent = shadowOf(activity).peekNextStartedActivity();
         expect(intent).not.toBeNull();
         expect(intent.getAction()).toEqual(Intent.ACTION_VIEW);
         expect(intent.getData().toString()).toEqual("http://www.fingerhut.com/product/apple-ipod-8gb-4th-generation-black-touch/nl522.uts?CTid=661&ci_src=14110944&ci_sku=4NL5220000010");
     }
 
     private void doSearch(String searchText) {
         searchEdit.setText(searchText);
         activity.findViewById(R.id.search_button).performClick();
     }
 }
