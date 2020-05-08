 package ru.spbau.WhereIsMyMoney.gui;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import ru.spbau.WhereIsMyMoney.storage.TransactionLogSource;
 import ru.spbau.WhereIsMyMoney.R;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.*;
 
 import java.lang.Float;
 import java.util.*;
 
 /**
  * Base class for reports
  */
 public abstract class AbstractCostsReportActivity extends Activity {
     private static final String TAG = AbstractCostsReportActivity.class.getCanonicalName();
     static final String START_DATE = "startDate";
     static final String END_DATE = "endDate";
    private static final String VALUE = "value";
    private static final String CURRENCY_NAME = "currencyName";
    private static final String NAME = "name";
    private static final String AMOUNT = "amount";

 
     protected  abstract void init(Date start, Date end);
 
     protected abstract Map<String, Float> getTotalVals();
 
     protected abstract void customizeListView(ExpandableListView listView);
 
     protected abstract List<List<Map<String, String>>> getDataForAdapter();
 
     TransactionLogSource db;
 
     private void createListView() {
         Log.d(TAG, "createListView()");
         final long startTime = this.getIntent().getLongExtra(START_DATE, -1);
         final long endTime = this.getIntent().getLongExtra(END_DATE, -1);
         if (startTime == -1) {
             Log.d(TAG, START_DATE + "parameter not found");
             return;
         }
         if (endTime == -1) {
             Log.d(TAG, END_DATE + "parameter not found");
             return;
         }
         Date start = new Date(startTime);
         Date end = new Date(endTime);
 
         Log.d(TAG, "Generate report from " + start.toString() + " to " + end.toString());
 
         init(start, end);
 
         List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
         Map<String, Float> totalVals = getTotalVals();
         for (String currency : totalVals.keySet()) {
             Map<String, String> m = new HashMap<String, String>();
            m.put(CURRENCY_NAME, currency);
            m.put(VALUE, totalVals.get(currency).toString());
             groupData.add(m);
         }
        String groupFrom[] = new String[] {VALUE, CURRENCY_NAME};
         int groupTo[] = new int[] {R.id.curr_totalVal, R.id.curr_name};
 
        String childFrom[] = new String[] {NAME, AMOUNT};
         int childTo[] = new int[] {R.id.child_name, R.id.child_amount};
 
         List<List<Map<String, String>>> childData = getDataForAdapter();
 
         SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(this, groupData, R.layout.costs_report_group_row,
                 groupFrom, groupTo, childData, R.layout.costs_report_child_row,
                 childFrom, childTo);
 
         ExpandableListView transactionsView = (ExpandableListView) findViewById(ru.spbau.WhereIsMyMoney.R.id.transactions);
         transactionsView.setAdapter(adapter);
 
         customizeListView(transactionsView);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate(...)");
         super.onCreate(savedInstanceState);
         db = new TransactionLogSource(getApplicationContext());
         db.open();
         setContentView(ru.spbau.WhereIsMyMoney.R.layout.costs_report);
         createListView();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         db.close();
     }
 
     Map<String, Map<String, Float>> swapFirstAndSecondKeys(Map<String, Map<String, Float>> input) {
         Map<String, Map<String, Float>> second2first2value = new HashMap<String, Map<String, Float>>();
         for (String card : input.keySet()) {
             Map<String, Float> second2value = input.get(card);
             for(String currency : second2value.keySet()) {
                 Map<String, Float> first2value = second2first2value.get(currency);
                 if (first2value == null)
                     first2value = new HashMap<String, Float>();
 
                 first2value.put(card, second2value.get(currency));
                 second2first2value.put(currency, first2value);
             }
         }
         return second2first2value;
     }
 }
