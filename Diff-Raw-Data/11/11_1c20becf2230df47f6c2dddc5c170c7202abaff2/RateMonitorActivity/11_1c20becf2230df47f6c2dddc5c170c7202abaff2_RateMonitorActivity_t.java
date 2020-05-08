 package com.zhenguo_fu.exchangemonitor;
 
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import com.zhenguo_fu.exchangemonitor.RateMonitorService.LocalBinder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.IBinder;
 import android.text.InputFilter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class RateMonitorActivity extends Activity {
 
     private static final int NUM_OF_CUR = 90;
     private static String[] CURRENCY_NAMES;
     private static String[] CURRENCY_CODES;
     private static String[] CURRENCY_DISP_CODES;
     // image for currency
     private int[] CURRENCY_IMGS = { R.drawable.usa, R.drawable.euro, R.drawable.uk,
             R.drawable.china, R.drawable.japan, R.drawable.algeria, R.drawable.argentina,
             R.drawable.australia, R.drawable.bahrain, R.drawable.bangladesh, R.drawable.bolivia,
             R.drawable.botswana, R.drawable.brazil, R.drawable.brunei, R.drawable.bulgaria,
             R.drawable.cfa_franc, R.drawable.canada, R.drawable.cayman_islands, R.drawable.chile,
             R.drawable.colombia, R.drawable.costa_rica, R.drawable.croatian,
             R.drawable.czech_republic, R.drawable.denmark, R.drawable.dominican_republic,
             R.drawable.egypt, R.drawable.fiji, R.drawable.honduras, R.drawable.hongkong,
             R.drawable.hungary, R.drawable.iceland, R.drawable.india, R.drawable.indonesia,
             R.drawable.israel, R.drawable.jamaica, R.drawable.jordan, R.drawable.kazakhstan,
             R.drawable.kenya, R.drawable.kuwait, R.drawable.latvia, R.drawable.lebanon,
             R.drawable.lithuania, R.drawable.macedonia, R.drawable.malaysia, R.drawable.maldives,
             R.drawable.mauritania, R.drawable.mexico, R.drawable.moldova, R.drawable.morocco,
             R.drawable.namibia, R.drawable.nepal, R.drawable.netherlands, R.drawable.taiwan,
             R.drawable.new_zealand, R.drawable.nicaragua, R.drawable.nigeria, R.drawable.norway,
             R.drawable.oman, R.drawable.pakistan, R.drawable.papua_new_guinea, R.drawable.paraguay,
             R.drawable.peru, R.drawable.philippines, R.drawable.poland, R.drawable.qatar,
             R.drawable.romania, R.drawable.russia, R.drawable.saudi_arabia, R.drawable.serbia,
             R.drawable.seychelles, R.drawable.sierra_leone, R.drawable.singapore,
             R.drawable.south_africa, R.drawable.southkorea, R.drawable.sri_lanka,
             R.drawable.sweden, R.drawable.switzerland, R.drawable.tanzania, R.drawable.thailand,
             R.drawable.trinidad_and_tobago, R.drawable.tunisia, R.drawable.turkey,
             R.drawable.uganda, R.drawable.ukraine, R.drawable.united_arab_emirates,
             R.drawable.uruguay, R.drawable.uzbekistan, R.drawable.venezuela, R.drawable.vietnam,
             R.drawable.yemen };
 
     private static final int DIALOG_CHOOSE_TARGET = 101;
     private static final int DIALOG_CHOOSE_BASE = 102;
     private static final int DIALOG_ABOUT_INFO = 103;
     private static final int INVALID_INDEX = ConfigInfo.INVALID_INDEX;
     private static final int TREND_BELOW = R.id.radio_below;
     private static final int TREND_ABOVE = R.id.radio_above;
     private static final int NUM_MAX_RATE_DIGIT = 5;
 
     private RateMonitorService mService = null;
     private boolean mBound = false;
     private static CurrencyConf[] mCurrencyConfs;
     private static Expect_Rate_Info[] mExpect_Rate_Infos = null;
     private ArrayAdapter<CurrencyConf> mCurChooseListAdapter = null;
     private ArrayAdapter<Expect_Rate_Info> mRateMonitorListAdapter = null;
     // position of operating row in the target currencies list
     private static int mCurrentTargetPos = 0;
 
     private BroadcastReceiver mRefreshReceiver = null;
     public CountDownTimer mCountDownTimer = null;
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
             mBound = false;
         }
 
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             LocalBinder binder = (LocalBinder) service;
             mService = binder.getService();
             mBound = true;
             // if service hasn't been bound, the current rate can't be displayed
             if (mRateMonitorListAdapter != null) {
                 mRateMonitorListAdapter.notifyDataSetChanged();
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_rate_monitor);
 
         CURRENCY_NAMES = getResources().getStringArray(R.array.CurrencyNameArray);
         CURRENCY_CODES = getResources().getStringArray(R.array.CurrencyCodeArray);
         CURRENCY_DISP_CODES = getResources().getStringArray(R.array.CurrencyDispCodeArray);
         mCurrencyConfs = new CurrencyConf[NUM_OF_CUR];
         for (int index = 0; index < NUM_OF_CUR; index++) {
             mCurrencyConfs[index] = new CurrencyConf(CURRENCY_NAMES[index], CURRENCY_IMGS[index],
                     CURRENCY_CODES[index]);
         }
         mCurChooseListAdapter = new CurrencyArrayAdatper(RateMonitorActivity.this, mCurrencyConfs);
 
         Intent intent = new Intent(getApplicationContext(), RateMonitorService.class);
         startService(intent);
         bindService(intent, mConnection, BIND_AUTO_CREATE);
 
         final ProgressBar refreshBar = (ProgressBar) findViewById(R.id.progress_refresh);
         final ImageButton refreshButton = (ImageButton) findViewById(R.id.btn_refresh);
         refreshButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mService != null && mRateMonitorListAdapter != null) {
                     refreshBar.setVisibility(View.VISIBLE);
                     refreshButton.setVisibility(View.INVISIBLE);
                     ((CurrencyMonitorArrayAdapter) mRateMonitorListAdapter).setRefreshing(true);
                     mRateMonitorListAdapter.notifyDataSetChanged();
                     mService.refreshData();
                 }
             }
         });
 
         ListView targetCurrencyListView = (ListView) findViewById(R.id.list_all_rates);
         mExpect_Rate_Infos = loadExpectRates(RateMonitorActivity.this);
         mRateMonitorListAdapter = new CurrencyMonitorArrayAdapter(RateMonitorActivity.this,
                 mExpect_Rate_Infos);
         targetCurrencyListView.setAdapter(mRateMonitorListAdapter);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unbindService(mConnection);
         mConnection = null;
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(mRefreshReceiver);
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         mRateMonitorListAdapter.notifyDataSetChanged();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mRefreshReceiver = new RefreshReciever();
         IntentFilter filter = new IntentFilter();
         filter.addAction(ConfigInfo.ACTION_REFRESH);
         registerReceiver(mRefreshReceiver, filter);
     }
 
     @Override
     protected void onStart() {
         // TODO Auto-generated method stub
         super.onStart();
     }
 
     @Override
     protected void onStop() {
         // TODO Auto-generated method stub
         super.onStop();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(R.string.menu_basic_help_button).setIcon(R.drawable.help)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                         Intent intent = new Intent(RateMonitorActivity.this, HelpActivity.class);
                        intent.putExtra(ConfigInfo.ACTIVITY_HELP_TYPE,
                                ConfigInfo.ACTIVITY_BASIC_HELP);
                         startActivity(intent);
                         return false;
                     }
                 });
         menu.add(R.string.menu_extra_help_button).setIcon(R.drawable.help)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                         Intent intent = new Intent(RateMonitorActivity.this, HelpActivity.class);
                        intent.putExtra(ConfigInfo.ACTIVITY_HELP_TYPE,
                                ConfigInfo.ACTIVITY_EXTRA_HELP);
                         startActivity(intent);
                         return false;
                     }
                 });
         menu.add(R.string.menu_about_button).setIcon(R.drawable.about)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                         showDialog(DIALOG_ABOUT_INFO);
                         return false;
                     }
                 });
         return true;
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle bundle) {
         switch (id) {
         case DIALOG_CHOOSE_BASE:
         case DIALOG_CHOOSE_TARGET:
             return CreateCurrencyListDialog(bundle, id);
         case DIALOG_ABOUT_INFO:
             return new AlertDialog.Builder(RateMonitorActivity.this)
                     .setTitle(R.string.dialog_title_about).setMessage(R.string.text_about_info)
                     .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
 
                         public void onClick(DialogInterface dialog, int which) {
                         }
                     }).create();
 
         default:
             break;
         }
         return super.onCreateDialog(id, bundle);
     }
 
     public static Expect_Rate_Info[] loadExpectRates(Context context) {
         Object[] infos = LocalPersistence.readObjectFromFile(context, ConfigInfo.DATA_FILE_NAME,
                 ConfigInfo.NUM_MAX_MONITOR);
         Expect_Rate_Info[] expect_Rate_Infos = null;
         if (infos != null) {
             expect_Rate_Infos = (Expect_Rate_Info[]) infos;
         } else {
             expect_Rate_Infos = new Expect_Rate_Info[ConfigInfo.NUM_MAX_MONITOR];
             for (int index = 0; index < ConfigInfo.NUM_MAX_MONITOR; index++) {
                 expect_Rate_Infos[index] = new Expect_Rate_Info();
                 expect_Rate_Infos[index].setId(index);
             }
         }
 
         return expect_Rate_Infos;
     }
 
     private AlertDialog CreateCurrencyListDialog(final Bundle bundle, final int dialogId) {
         int dialogTitleId = R.string.dialog_title_target_currency;
         if (dialogId == DIALOG_CHOOSE_BASE) {
             dialogTitleId = R.string.dialog_title_base_currency;
         } else if (dialogId == DIALOG_CHOOSE_TARGET) {
             dialogTitleId = R.string.dialog_title_target_currency;
         }
 
         AlertDialog currencyDialog = new AlertDialog.Builder(RateMonitorActivity.this)
                 .setTitle(dialogTitleId)
                 .setSingleChoiceItems(mCurChooseListAdapter, 0,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 SetCurrencyChecked(which);
                                 mCurChooseListAdapter.notifyDataSetChanged();
                             }
                         })
                 .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         int selectedPosition = ((AlertDialog) dialog).getListView()
                                 .getCheckedItemPosition();
                         if (dialogId == DIALOG_CHOOSE_BASE) {
                             mExpect_Rate_Infos[mCurrentTargetPos]
                                     .setBaseCurrencyId(selectedPosition);
                         } else {
                             mExpect_Rate_Infos[mCurrentTargetPos]
                                     .setTargetCurrencyId(selectedPosition);
                         }
 
                         mRateMonitorListAdapter.notifyDataSetChanged();
                         SetCurrencyChecked(INVALID_INDEX);
                         mCurChooseListAdapter.notifyDataSetChanged();
                     }
                 }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         SetCurrencyChecked(INVALID_INDEX);
                         mCurChooseListAdapter.notifyDataSetChanged();
                     }
                 }).setNeutralButton(R.string.btn_reset, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         if (dialogId == DIALOG_CHOOSE_BASE) {
                             mExpect_Rate_Infos[mCurrentTargetPos].setBaseCurrencyId(INVALID_INDEX);
                         } else {
                             mExpect_Rate_Infos[mCurrentTargetPos]
                                     .setTargetCurrencyId(INVALID_INDEX);
                         }
 
                         SetCurrencyChecked(INVALID_INDEX);
                         mCurChooseListAdapter.notifyDataSetChanged();
                         mRateMonitorListAdapter.notifyDataSetChanged();
                     }
                 }).create();
 
         return currencyDialog;
     }
 
     // If the index value is invalid, then set all check value as false!
     private void SetCurrencyChecked(int index) {
         for (CurrencyConf currencyConf : mCurrencyConfs) {
             currencyConf.SetIsChecked(false);
         }
         if (index != INVALID_INDEX) {
             mCurrencyConfs[index].SetIsChecked(true);
         }
     }
 
     private class CurrencyMonitorArrayAdapter extends ArrayAdapter<Expect_Rate_Info> {
 
         public CurrencyMonitorArrayAdapter(Context context, Expect_Rate_Info[] objects) {
             super(context, R.layout.currency_monitor_item, objects);
             mContext = context;
             mValues = objects;
         }
 
         private final Context mContext;
         private final Expect_Rate_Info[] mValues;
         private boolean mIsRefreshing = false;
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             final int final_pos = position;
             Expect_Rate_Info expect_Rate_Info = mValues[position];
             int baseId = expect_Rate_Info.getBaseCurrencyId();
             int targetId = expect_Rate_Info.getTargetCurrencyId();
             float expectRate = expect_Rate_Info.getExpectValue();
             boolean isBelow = expect_Rate_Info.getIsBelow();
             LayoutInflater inflater = (LayoutInflater) mContext
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             View rowView = inflater.inflate(R.layout.currency_monitor_item, parent, false);
             ImageView baseCurrencyImg = (ImageView) rowView.findViewById(R.id.btn_base_currency);
             ImageView targetCurrencyImg = (ImageView) rowView
                     .findViewById(R.id.btn_target_currency);
             TextView baseCurrencyNameView = (TextView) rowView
                     .findViewById(R.id.view_base_currencyName);
             TextView targetCurrencyNameView = (TextView) rowView
                     .findViewById(R.id.view_target_currencyName);
             TextView currencyValueView = (TextView) rowView
                     .findViewById(R.id.view_current_rateValue);
             ImageView exclamationView = (ImageView) rowView.findViewById(R.id.image_reminder);
             final RadioGroup currencyTrendGroup = (RadioGroup) rowView
                     .findViewById(R.id.radioGroup_trend);
             final EditText expectRateEditText = (EditText) rowView
                     .findViewById(R.id.edit_expect_value);
             Button saveButton = (Button) rowView.findViewById(R.id.btn_save_expect);
             exclamationView.setVisibility(View.GONE);
             expectRateEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                     NUM_MAX_RATE_DIGIT), });
 
             if (baseId == INVALID_INDEX) {
                 baseCurrencyNameView.setText(R.string.hint_select_base);
             } else {
                 baseCurrencyImg.setImageResource(CURRENCY_IMGS[baseId]);
                 baseCurrencyNameView.setText(CURRENCY_NAMES[baseId]);
             }
 
             if (targetId == INVALID_INDEX) {
                 targetCurrencyNameView.setText(R.string.hint_select_target);
             } else {
                 targetCurrencyImg.setImageResource(CURRENCY_IMGS[targetId]);
                 targetCurrencyNameView.setText(CURRENCY_NAMES[targetId]);
             }
 
             if (baseId != INVALID_INDEX && targetId != INVALID_INDEX) {
                 if (mIsRefreshing == true) {
                     currencyValueView.setText("");
                 } else {
                     if (mService != null) {
                         float currentRate = mService.getRate(CURRENCY_CODES[baseId],
                                 CURRENCY_CODES[targetId]);
                         String currentRateStr = "1 " + CURRENCY_DISP_CODES[baseId] + " : "
                                 + String.format("%.3f", currentRate) + " "
                                 + CURRENCY_DISP_CODES[targetId];
                         currencyValueView.setText(currentRateStr);
 
                         // whether highlight the rate item or not, depends on
                         // expect rate value
                         if (expect_Rate_Info.getExpectValue() > 0f && currentRate > 0f) {
                             if ((currentRate == expectRate)
                                     || (currentRate < expectRate && isBelow == true)
                                     || (currentRate > expectRate && isBelow == false)) {
                                 rowView.setBackgroundResource(R.drawable.list_bg_yellow);
                                 currencyValueView.setTextColor(ConfigInfo.COLOR_BLUE);
                                 expectRateEditText.setTextColor(ConfigInfo.COLOR_BLUE);
                                 exclamationView.setVisibility(View.VISIBLE);
                             }
                         }
                     }
                 }
             }
 
             currencyTrendGroup.check(isBelow ? TREND_BELOW : TREND_ABOVE);
             expectRateEditText.setText(Float.toString(expectRate));
             baseCurrencyImg.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     mCurrentTargetPos = final_pos;
                     showDialog(DIALOG_CHOOSE_BASE);
 
                 }
             });
 
             targetCurrencyImg.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     mCurrentTargetPos = final_pos;
                     showDialog(DIALOG_CHOOSE_TARGET);
 
                 }
             });
 
             saveButton.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     Expect_Rate_Info info = mExpect_Rate_Infos[final_pos];
                     float expect_rate = Float.parseFloat(expectRateEditText.getText().toString());
                     Boolean isDown = currencyTrendGroup.getCheckedRadioButtonId() == TREND_BELOW ? true
                             : false;
                     info.setIsBelow(isDown);
                     if (info.getBaseCurrencyId() == INVALID_INDEX
                             || info.getTargetCurrencyId() == INVALID_INDEX) {
                         info.setExpectValue(0f);
                     } else {
                         info.setExpectValue(expect_rate);
                     }
 
                     LocalPersistence.witeObjectToFile(RateMonitorActivity.this, mExpect_Rate_Infos,
                             ConfigInfo.DATA_FILE_NAME);
                     if (mService != null) {
                         mService.saveConf(info);
                     }
 
                     mRateMonitorListAdapter.notifyDataSetChanged();
                 }
             });
 
             return rowView;
         }
 
         public boolean isRefreshing() {
             return mIsRefreshing;
         }
 
         public void setRefreshing(boolean isRefreshing) {
             this.mIsRefreshing = isRefreshing;
         }
 
     }
 
     private class CurrencyArrayAdatper extends ArrayAdapter<CurrencyConf> {
         public CurrencyArrayAdatper(Context context, CurrencyConf[] objects) {
             super(context, R.layout.currency_list_item, objects);
             mContext = context;
             mValues = objects;
         }
 
         private final Context mContext;
         private final CurrencyConf[] mValues;
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             LayoutInflater inflater = (LayoutInflater) mContext
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             View rowView = inflater.inflate(R.layout.currency_list_item, parent, false);
             TextView textView = (TextView) rowView.findViewById(R.id.currencyName);
             ImageView imgView = (ImageView) rowView.findViewById(R.id.currencyImage);
             ImageView tickedView = (ImageView) rowView.findViewById(R.id.tickedCurrencyImage);
             textView.setText(mValues[position].GetString());
             imgView.setImageResource(mValues[position].GetImageResId());
 
             if (mValues[position].IsChecked()) {
                 tickedView.setVisibility(View.VISIBLE);
             } else {
                 tickedView.setVisibility(View.INVISIBLE);
             }
             return rowView;
         }
     }
 
     public class RefreshReciever extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (mRateMonitorListAdapter != null) {
                 ((CurrencyMonitorArrayAdapter) mRateMonitorListAdapter).setRefreshing(false);
                 mRateMonitorListAdapter.notifyDataSetChanged();
             }
             ProgressBar refreshBar = (ProgressBar) RateMonitorActivity.this
                     .findViewById(R.id.progress_refresh);
             final ImageButton refreshButton = (ImageButton) RateMonitorActivity.this
                     .findViewById(R.id.btn_refresh);
             final TextView countdownView = (TextView) RateMonitorActivity.this
                     .findViewById(R.id.view_countdown);
             final ImageView circleView = (ImageView) RateMonitorActivity.this
                     .findViewById(R.id.image_circle);
             if (refreshBar != null && refreshButton != null && countdownView != null
                     && circleView != null) {
                 refreshBar.setVisibility(View.INVISIBLE);
                 refreshButton.setVisibility(View.INVISIBLE);
                 if (mCountDownTimer != null) {
                     mCountDownTimer.cancel();
                 }
 
                 mCountDownTimer = new CountDownTimer(ConfigInfo.TIME_MINITE_INT,
                         ConfigInfo.TIME_SECOND_INT) {
 
                     @Override
                     public void onTick(long millisUntilFinished) {
                         circleView.setVisibility(View.VISIBLE);
                         countdownView.setVisibility(View.VISIBLE);
                         countdownView.setText(String
                                 .valueOf((millisUntilFinished / ConfigInfo.TIME_SECOND_INT)));
                     }
 
                     @Override
                     public void onFinish() {
                         circleView.setVisibility(View.GONE);
                         countdownView.setVisibility(View.GONE);
                         refreshButton.setVisibility(View.VISIBLE);
                     }
                 }.start();
 
             }
         }
 
     }
 
     public class CurrencyConf {
         private String mString = "";
         private int mImageResId = 0;
         private boolean mIsChecked = false;
         private String mCode = "";
 
         public String getCode() {
             return mCode;
         }
 
         public void setmCode(String code) {
             mCode = code;
         }
 
         public CurrencyConf() {
         }
 
         public CurrencyConf(String name, int imageResId, String code) {
             mString = name;
             mImageResId = imageResId;
             mCode = code;
         }
 
         public String GetString() {
             return mString;
         }
 
         public int GetImageResId() {
             return mImageResId;
         }
 
         public boolean IsChecked() {
             return mIsChecked;
         }
 
         public void SetIsChecked(boolean mIsChecked) {
             this.mIsChecked = mIsChecked;
         }
     }
 
     public static class LocalPersistence {
 
         public static void witeObjectToFile(Context context, Object[] objects, String filename) {
 
             ObjectOutputStream objectOut = null;
             FileOutputStream fileOut = null;
 
             try {
                 if (objects != null) {
                     List<Object> obj_list = Arrays.asList(objects);
                     fileOut = context.openFileOutput(filename, Activity.MODE_PRIVATE);
                     OutputStream buffer = new BufferedOutputStream(fileOut);
                     objectOut = new ObjectOutputStream(buffer);
                     objectOut.writeObject(obj_list);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (objectOut != null && fileOut != null) {
                     try {
                         objectOut.close();
                         fileOut.close();
                     } catch (IOException e) {
                         Log.e(ConfigInfo.TEST_TAG, "Closing file failed!!!");
                     }
                 }
             }
         }
 
         // if the file doesn't exist, return null
         public static Object[] readObjectFromFile(Context context, String filename, int length) {
 
             ObjectInputStream objectIn = null;
             FileInputStream fileIn = null;
             Object[] objects = null;
             try {
 
                 fileIn = context.getApplicationContext().openFileInput(filename);
                 objectIn = new ObjectInputStream(fileIn);
                 List<Object> obj_list = (List<Object>) objectIn.readObject();
                 objects = obj_list.toArray();
             } catch (FileNotFoundException e) {
                 objects = null;
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } finally {
                 if (objectIn != null && fileIn != null) {
                     try {
                         objectIn.close();
                         fileIn.close();
                     } catch (IOException e) {
                         Log.e(ConfigInfo.TEST_TAG, "Closing file failed!!!");
                     }
                 }
             }
 
             return objects;
         }
 
     }
 }
