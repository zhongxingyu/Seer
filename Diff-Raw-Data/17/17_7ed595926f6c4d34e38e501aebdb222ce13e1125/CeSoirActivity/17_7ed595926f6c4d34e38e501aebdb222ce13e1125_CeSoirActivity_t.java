 package fr.ybo.ybotv.android.activity;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.graphics.Rect;
import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.DatePicker;
 import android.widget.GridView;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.adapter.CeSoirViewFlowAdapter;
 import fr.ybo.ybotv.android.util.AdMobUtil;
 import org.taptwo.android.widget.TitleFlowIndicator;
 import org.taptwo.android.widget.ViewFlow;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 
 public class CeSoirActivity extends MenuManager.AbstractSimpleActivity implements DatePickerDialog.OnDateSetListener, AbsListView.OnScrollListener {
 
     private Calendar currentDate;
 
     private CeSoirViewFlowAdapter adapter;
 
     private ViewFlow viewFlow;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.flow);
         createMenu();
         currentDate = Calendar.getInstance();
         if (currentDate.get(Calendar.HOUR_OF_DAY) <= 2) {
             currentDate.add(Calendar.DAY_OF_MONTH, -1);
         }
 
         viewFlow = (ViewFlow) findViewById(R.id.viewflow);
         adapter = new CeSoirViewFlowAdapter(this, currentDate, this);
         viewFlow.setAdapter(adapter);
         TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
         indicator.setTitleProvider(adapter);
         viewFlow.setFlowIndicator(indicator);
 
         AdMobUtil.manageAds(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.cesoirmenu, menu);
         return true;
     }
 
 
     @Override
     public int getMenuIdOfClass() {
         return R.id.menu_cesoir;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_choixDate) {
             showDialog(R.id.dialog_choixDate);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         if (id == R.id.dialog_choixDate) {
             return new DatePickerDialog(this, this, currentDate.get(Calendar.YEAR),
                     currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
         }
         return super.onCreateDialog(id);
     }
 
     @Override
     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
         currentDate.set(Calendar.YEAR, year);
         currentDate.set(Calendar.MONTH, monthOfYear);
         currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         adapter.changeCurrentDate(currentDate);
     }
 
 
     @Override
     public void onScrollStateChanged(AbsListView view, int scrollState) {
         if (scrollState == SCROLL_STATE_IDLE) {
 
             if (viewFlow.getSelectedItemPosition() != ((Integer) view.getTag())) {
                 return;
             }
 
             if (view.getChildAt(0) != null) {
                 Rect r = new Rect();
                 view.getChildAt(0).getLocalVisibleRect(r);
                 for (GridView otherGridView : getAllLoadedGridViews()) {
                     if (otherGridView.getTag() != view.getTag()) {
                        scrollToPosition(otherGridView, view.getFirstVisiblePosition(), -r.top);
                     }
                 }
             }
         }
 
     }
 
    private void scrollToPosition(GridView gridView, int position, int offset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            gridView.smoothScrollToPositionFromTop(position, offset);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            gridView.smoothScrollToPosition(position);
        } else {
            gridView.setSelection(position);
        }
    }

     private Collection<GridView> currentGridViews = null;
 
     private Collection<GridView> getAllLoadedGridViews() {
         if (currentGridViews != null && currentGridViews.size() == viewFlow.getLoadedViews().size()) {
             return currentGridViews;
         }
         currentGridViews = new ArrayList<GridView>();
         for (View oneLoadedView : viewFlow.getLoadedViews()) {
             currentGridViews.add((GridView) oneLoadedView.findViewById(R.id.grid));
         }
         return currentGridViews;
     }
 
 
     @Override
     public void onScroll(AbsListView arg0, int first, int visible, int total) {
 
     }
 }
 
