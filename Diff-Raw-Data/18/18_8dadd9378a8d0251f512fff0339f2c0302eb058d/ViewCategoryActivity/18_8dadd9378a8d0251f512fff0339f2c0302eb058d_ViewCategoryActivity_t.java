 package edu.mit.moneyManager.view;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.listUtils.CategoryExpenseListAdapter;
 import edu.mit.moneyManager.listUtils.ExpenseItemEntry;
 import edu.mit.moneyManager.model.Category;
 import edu.mit.moneyManager.model.DatabaseAdapter;
 import edu.mit.moneyManager.model.Expense;
 
 /**
  * Screen that shows details of a Category
  * 
  */
 public class ViewCategoryActivity extends ListActivity {
     public static final String TAG = "VIEW CATEGORY ACTIVITY";
     public static final String INTENT_KEY_CATEGORY = "category";
     private String categoryName;
     private DatabaseAdapter mDBAdapter;
     private TextView categoryNameView;
     private TextView categoryTotalView;
     private TextView categoryRemainingView;
     private Context context = this;
     private ListView lv;
     private static SharedPreferences settings;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_category);
         Bundle extras = getIntent().getExtras();
         categoryName = extras.getString(INTENT_KEY_CATEGORY);
         mDBAdapter = new DatabaseAdapter(this);
         settings = getSharedPreferences(ViewSummaryActivity.PREFS_NAME, MODE_PRIVATE);
 
         
         categoryNameView = (TextView) findViewById(R.id.view_summary_category);
         categoryTotalView = (TextView) findViewById(R.id.category_total);
         categoryRemainingView = (TextView) findViewById(R.id.category_remaining);
        
        Button backBtn = (Button) findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
         // list adapter header
         lv = getListView();
         LayoutInflater inflater = getLayoutInflater();
         View header = inflater.inflate(R.layout.view_category_list_header,
                 null, false);
         lv.addHeaderView(header);
 
         resetData();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         resetData();
 
     }
 
     private void resetData() {
         categoryNameView.setText(categoryName);
 
         mDBAdapter.open();
         Category category = mDBAdapter.getCategory(categoryName);
         if (category != null) {
             categoryTotalView.setText(category.getTotal().toString());
             categoryRemainingView.setText(category.getRemaining().toString());
         }
 
         List<Expense> expenses = mDBAdapter.getExpenses(categoryName);
         mDBAdapter.close();
 
         CategoryExpenseListAdapter adapter = new CategoryExpenseListAdapter(
                 this, (ArrayList<Expense>) expenses, this, mDBAdapter, categoryName, categoryRemainingView, settings);
         setListAdapter(adapter);
 
     }
 }
