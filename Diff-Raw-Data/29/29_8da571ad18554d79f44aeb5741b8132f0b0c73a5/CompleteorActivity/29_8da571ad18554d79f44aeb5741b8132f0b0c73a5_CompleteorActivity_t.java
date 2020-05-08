 package com.mustangexchange.polymeal;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class CompleteorActivity extends Activity {
 
     private ItemSet possibleItems;
     private ItemSet dummyItems;
 
     private ActionBar mActionBar;
     private static BigDecimal totalAmount;
     private static Context mContext;
     private static Activity activity;
     public ListView lv;
     public CompleteorItemAdapter dummyAdapter;
     public CompleteorItemAdapter lvAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_completeor);
 
         mContext = this;
         activity = this;
         mActionBar = getActionBar();
         mActionBar.setDisplayHomeAsUpEnabled(true);
         mActionBar.setHomeButtonEnabled(true);
 
         possibleItems = new ItemSet("Completeor",new ArrayList<String>(),new ArrayList<String>());
         dummyItems = new ItemSet("Completeor",new ArrayList<String>(),new ArrayList<String>());
         dummyAdapter = new CompleteorItemAdapter(this, dummyItems);
         lvAdapter = new CompleteorItemAdapter(this, possibleItems);
 
         lv = (ListView) findViewById(R.id.listView);
         lv.setAdapter(dummyAdapter);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
                 final int fPos = pos;
                 final AlertDialog.Builder onListClick= new AlertDialog.Builder(activity);
                 onListClick.setCancelable(false);
                 onListClick.setTitle("Add to Cart?");
                 onListClick.setMessage("Would you like to add " + possibleItems.getNames().get(pos).replace("@#$", "") + " to your cart? Price: " + "$" + possibleItems.getPrices().get(pos));
                 onListClick.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         //money = boundPrices.get(tempIndex);
                         if (possibleItems.getNames().get(fPos).contains("@#$")) {
                             AlertDialog.Builder onYes = new AlertDialog.Builder(activity);
                             onYes.setTitle("How much?");
                             onYes.setMessage("Estimated Number of Ounces: ");
                             LayoutInflater inflater = activity.getLayoutInflater();
                             View DialogView = inflater.inflate(R.layout.number_picker, null);
                             final NumberPicker np = (NumberPicker) DialogView.findViewById(R.id.numberPicker);
                             np.setMinValue(1);
                             np.setMaxValue(50);
                             np.setWrapSelectorWheel(false);
                             np.setValue(1);
                             onYes.setView(DialogView);
                             onYes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int button) {
                                     Cart.add(possibleItems.getNames().get(fPos), Double.toString(np.getValue() * new Double(possibleItems.getPrices().get(fPos))));
                                     updateBalance();
                                    updateList();
                                 }
                             });
                             onYes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int button) {
                                 }
                             });
                             onYes.show();
                         } else {
                             Cart.add(possibleItems.getNames().get(fPos), possibleItems.getPrices().get(fPos));
                             updateBalance();
                            updateList();
                         }
                     }
                 });
                 onListClick.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                     }
                 });
                 onListClick.setNeutralButton("Description", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         AlertDialog.Builder onDialogClick = new AlertDialog.Builder(activity);
                         onDialogClick.setTitle("Description");
                         onDialogClick.setMessage(possibleItems.getDesc().get(fPos));
                         onDialogClick.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int button) {
 
                             }
                         });
                         onDialogClick.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int button) {
                                 onListClick.show();
                             }
                         });
                         onDialogClick.show();
                     }
                 });
                 onListClick.show();
             }
         });
 
         new calcCompleteor().execute("");
 
         updateBalance();
     }
 
     public void checkLayout() {
         if((lv.getAdapter().getCount() == 0) && (lv.getAdapter() == lvAdapter)) {
             setContentView(R.layout.empty_completeor);
         }
     }
 
 
     public void onResume()
     {
         super.onResume();
         Toast.makeText(mContext, "onResume triggered", Toast.LENGTH_SHORT);
     }
 
     public void updateList() {
         new calcCompleteor().execute("");
     }
 
     public void setSubtitleColor() {
         int titleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
         TextView yourTextView = (TextView)findViewById(titleId);
         if(totalAmount.compareTo(BigDecimal.ZERO) < 0)
         {
             yourTextView.setTextColor(Color.RED);
         }
         else
         {
             yourTextView.setTextColor(Color.WHITE);
         }
     }
 
     public void updateBalance() {
         totalAmount = MoneyTime.calcTotalMoney();
         setSubtitleColor();
         mActionBar.setSubtitle("$" + totalAmount + " Remaining");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.completeor, menu);
         return super.onCreateOptionsMenu(menu);
     }
     @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case android.R.id.home:
                 if(MainActivity.vgOrSand == 1)
                 {
                     Intent intent = new Intent(this, VistaActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                     return true;
                 }
                 else
                 {
                     Intent intent = new Intent(this, SandwichActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                     return true;
                 }
             case R.id.cart:
                 Intent intent = new Intent(this, CartActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public class CompleteorItemAdapter extends BaseAdapter implements View.OnClickListener {
         private Context context;
         private ItemSet possibleItems;
 
         public CompleteorItemAdapter(Context context, ItemSet possibleItems) {
             this.context = context;
             this.possibleItems = possibleItems;
         }
 
         public ItemSet getPossibleItems() {
             return possibleItems;
         }
 
         public int getCount() {
             return possibleItems.getNames().size();
         }
 
         public Object getItem(int position) {
             return possibleItems.getNames().get(position);
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup viewGroup) {
             //ItemSet entry = setList.get(position);
             if (convertView == null) {
                 LayoutInflater inflater = (LayoutInflater) context
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = inflater.inflate(R.layout.row_item, null);
             }
             TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
             tvName.setText(possibleItems.getNames().get(position).replace("@#$",""));
 
             TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
             if(possibleItems.getPrices().size() != 0)
             {
                 tvPrice.setText("$" + possibleItems.getPrices().get(position));
             }
 
             //Set the onClick Listener on this button
             ImageButton btnAdd = (ImageButton) convertView.findViewById(R.id.btn_add);
             btnAdd.setFocusableInTouchMode(false);
             btnAdd.setFocusable(false);
             btnAdd.setOnClickListener(this);
             btnAdd.setTag(new Integer(position));
 
             return convertView;
         }
 
         @Override
         public void onClick(View view) {
             final int position = (Integer) view.getTag();
             if (possibleItems.getNames().get(position).contains("@#$")) {
                 AlertDialog.Builder onYes = new AlertDialog.Builder(activity);
                 onYes.setTitle("How much?");
                 onYes.setMessage("Estimated Number of Ounces: ");
                 LayoutInflater inflater = (LayoutInflater) activity.getLayoutInflater();
                 View DialogView = inflater.inflate(R.layout.number_picker, null);
                 final NumberPicker np = (NumberPicker) DialogView.findViewById(R.id.numberPicker);
                 np.setMinValue(1);
                 np.setMaxValue(50);
                 np.setWrapSelectorWheel(false);
                 np.setValue(1);
                 onYes.setView(DialogView);
                 onYes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         DecimalFormat twoDForm = new DecimalFormat("#.##");
                         Cart.add(possibleItems.getNames().get(position), twoDForm.format(np.getValue() * new Double(possibleItems.getPrices().get(position))));
                         StringBuilder sb = new StringBuilder(possibleItems.getNames().get(position) + " added to Cart!");
                         sb.replace(0,3,"");
                         Toast.makeText(activity, sb, Toast.LENGTH_SHORT).show();
                         updateBalance();
                        updateList();
                     }
                 });
                 onYes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                     }
                 });
                 onYes.show();
             }
             else
             {
                 Cart.add(possibleItems.getNames().get(position), possibleItems.getPrices().get(position));
                 updateBalance();
                 Toast.makeText(activity, possibleItems.getNames().get(position) + " added to Cart!", Toast.LENGTH_SHORT).show();
                updateList();
             }
         }
     }
 
     private class calcCompleteor extends AsyncTask<String, Void, String> {
 
         @Override
         protected String doInBackground(String... params) {
             if(MainActivity.vgOrSand==1)
             {
                 System.out.println(MainActivity.vgItems.get(0).getTitle());
                 for(int i = 0;i<MainActivity.vgItems.size();i++)
                 {
                     for(int j = 0;j<MainActivity.vgItems.get(i).getPrices().size();j++)
                     {
                         if(MoneyTime.calcTotalMoney().compareTo(new BigDecimal(MainActivity.vgItems.get(i).getPrices().get(j)))>=0)
                         {
                             possibleItems.getNames().add(MainActivity.vgItems.get(i).getNames().get(j));
                             possibleItems.getPrices().add(MainActivity.vgItems.get(i).getPrices().get(j));
                             possibleItems.getDesc().add(MainActivity.vgItems.get(i).getDesc().get(j));
                         }
                     }
                 }
             }
             else if(MainActivity.vgOrSand==2)
             {
                 for(int i = 0;i<MainActivity.sandItems.size();i++)
                 {
                     for(int j = 0;j<MainActivity.sandItems.get(i).getPrices().size();j++)
                     {
                         if(MoneyTime.calcTotalMoney().compareTo(new BigDecimal(MainActivity.sandItems.get(i).getPrices().get(j)))>=0)
                         {
                             possibleItems.getNames().add(MainActivity.sandItems.get(i).getNames().get(j));
                             possibleItems.getPrices().add(MainActivity.sandItems.get(i).getPrices().get(j));
                             possibleItems.getDesc().add(MainActivity.sandItems.get(i).getDesc().get(j));
                         }
                     }
                 }
             }
             return "Executed";
         }
 
         @Override
         protected void onPostExecute(String result) {
             lvAdapter.notifyDataSetChanged();
             lv.setAdapter(lvAdapter);
             checkLayout();
         }
 
         @Override
         protected void onPreExecute() {
             lv.setAdapter(dummyAdapter);
             possibleItems.clear();
             lvAdapter.getPossibleItems().clear();
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
         }
     }
 }
