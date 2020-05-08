 package edu.mit.moneyManager.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.listUtils.CategoryItemEntry;
 import edu.mit.moneyManager.listUtils.SummaryCategoryListAdapter;
 import edu.mit.moneyManager.model.Category;
 import edu.mit.moneyManager.model.DatabaseAdapter;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ViewSummaryActivity extends ListActivity {
     public static final String TAG = "VIEW SUMMARY ACTIVITY";
     public static final String PREFS_NAME = "preferences";
     public static final String BUDGET_TOTAL = "total";
     // public static final String BUDGET_REMAINING = "remaining";
     private DatabaseAdapter mDBAdapter;
     private TextView total;
     private TextView remaining;
     private static SharedPreferences settings;
     private List<Category> categories;
     
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_summary);
         mDBAdapter = new DatabaseAdapter(this);
         mDBAdapter.open();
         categories = mDBAdapter.getCategories();
         mDBAdapter.close();
         settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
         total = (TextView) findViewById(R.id.total_monthly_budget_textview);
         remaining = (TextView) findViewById(R.id.remaining_monthly_budget_textview);
         
         SummaryCategoryListAdapter adapter = new SummaryCategoryListAdapter(
                 this, (ArrayList<Category>) categories,
                 ((TabActivity) getParent()).getTabHost());
         mDBAdapter.close();
         setListAdapter(adapter);
         ListView lv = getListView();
         lv.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView parent, View v, final int position,
                     long id) {
                 Intent intent = new Intent(v.getContext(), ViewCategoryActivity.class);
                 intent.putExtra(ViewCategoryActivity.INTENT_KEY_CATEGORY, categories.get(position).getName());
                 startActivity(intent);
             }
         });
         resetData();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         resetData();
     }
 
     private void resetData() {
         total.setText(new Float(settings.getFloat(BUDGET_TOTAL, (float) 0.0))
                 .toString());
         mDBAdapter.open();
 
         //total remaining = total - categorytotals + total category remainings
         Double remainingAmt = mDBAdapter.getTotalRemaining()+ (double) settings.getFloat(BUDGET_TOTAL, (float) 0.0) - mDBAdapter.getCategoriesTotal();
         if (mDBAdapter.getCategoryNames().size() == 0) {
             remainingAmt = (double) settings
                     .getFloat(BUDGET_TOTAL, (float) 0.0);
         }
         remaining.setText(remainingAmt.toString());
         
         SummaryCategoryListAdapter adapter = new SummaryCategoryListAdapter(
                 this, (ArrayList<Category>) categories,
                 ((TabActivity) getParent()).getTabHost());
         mDBAdapter.close();
         setListAdapter(adapter);
         
     }
 }
