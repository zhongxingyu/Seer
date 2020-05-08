 package com.jinheyu.lite_mms;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created by xc on 13-10-4.
  */
 public class QualityInspectorActivity extends WorkCommandListActivity {
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.quality_inspector, menu);
        setSearchView(menu.findItem(R.id.action_search));
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_logout:
                 new LogoutDialog(this).show();
                 break;
             default:
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected ArrayAdapter getArrayAdapter(int resource) {
         return null;
     }
 
     @Override
     protected FragmentPagerAdapter getFragmentPagerAdapter(int position) {
         return new MyFragmentPagerAdapter(getSupportFragmentManager());
     }
 
     private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
 
         public MyFragmentPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public int getCount() {
             return 2;
         }
 
         @Override
         public Fragment getItem(int i) {
             return new MyWorkCommandListFragment(i == 0);
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return position == 0 ? getString(R.string.quality_inspecting) :
                     getString(R.string.quality_inspected_today);
         }
     }
 
     class MyWorkCommandListFragment extends WorkCommandListFragment {
 
         private final int mStatus;
 
         public MyWorkCommandListFragment(boolean qualityInspecting) {
             this.mStatus = qualityInspecting ? Constants.STATUS_QUALITY_INSPECTING : Constants.STATUS_FINISHED;
         }
 
         @Override
         protected ActionMode.Callback getActionModeCallback() {
             return null;
         }
 
         @Override
         protected int getIntExtraSymbol() {
             return mStatus;
         }
 
         @Override
         protected Class<?> getWorkCommandAcitityClass() {
             return QualityInspectorWorkCommandActivity.class;
         }
 
         @Override
         protected void loadWorkCommandList() {
             new GetWorkCommandListTask(mStatus, this).execute();
         }
 
         class GetWorkCommandListTask extends AbstractGetWorkCommandListTask {
             private final int currentStatus;
 
             public GetWorkCommandListTask(int status, WorkCommandListFragment listFragment) {
                 super(listFragment);
                 currentStatus = status;
             }
 
             @Override
             protected List<WorkCommand> getWorkCommandList() throws IOException, JSONException, BadRequest {
                 return MyApp.getWebServieHandler().getWorkCommandList(currentStatus);
             }
         }
     }
 }
