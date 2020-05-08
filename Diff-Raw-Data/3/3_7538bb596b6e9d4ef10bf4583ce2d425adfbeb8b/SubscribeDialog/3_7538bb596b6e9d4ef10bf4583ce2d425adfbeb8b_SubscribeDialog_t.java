 package org.gnuton.newshub;
 
 import android.content.Context;
 import android.os.CountDownTimer;
 import android.support.v4.app.DialogFragment;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import org.gnuton.newshub.adapters.FeedListAdapter;
 import org.gnuton.newshub.db.DbHelper;
 import org.gnuton.newshub.db.RSSFeedDataSource;
 import org.gnuton.newshub.tasks.DownloadWebTask;
 import org.gnuton.newshub.types.RSSFeed;
 import org.gnuton.newshub.utils.MyApp;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by gnuton on 5/28/13.
  */
 public class SubscribeDialog extends DialogFragment implements ListView.OnItemClickListener {
     private final String TAG = SubscribeDialog.class.getName();
     private CountDownTimer mSearchTiimer;
     private View mDlgLayout;
 
     private final RSSFeedDataSource mFeedDataSource;
     private List<RSSFeed> mFeeds;
 
     private MainActivity mMainActivity;
     private ArrayAdapter<RSSFeed> adapter;
     private final String mFindFeedsUrl = "https://ajax.googleapis.com/ajax/services/feed/find?v=1.0&q=";
     private View mListViewHeader;
     private ListView mListView;
 
     public SubscribeDialog(){
         super();
         this.mMainActivity = MyApp.getInstance().mMainActivity;
         if (mMainActivity == null){
             this.mFeedDataSource = null;
             return;
         }
         this.mFeedDataSource = new RSSFeedDataSource(mMainActivity);
     }
 
     // ListView.OnClickListener
     @Override
     public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         //TextView v = (TextView) view;
         RSSFeed f = (RSSFeed) adapterView.getItemAtPosition(i);
         Log.d(TAG, "Added feed:" + f.title);
 
         if (f == null)
             return;
         mFeedDataSource.create(f);
         this.dismiss();
     }
 
     private void setBusyIndicatorStatus(Boolean busy){
         if (mDlgLayout == null)
             return;
 
         View spinner = mDlgLayout.findViewById(R.id.spinningImage);
         spinner.setVisibility(busy ? View.VISIBLE : View.GONE);
 
         if (busy){
             Animation animation = AnimationUtils.loadAnimation(MyApp.getContext(), R.animator.fadeout);
             spinner.startAnimation(animation);
         } else {
             spinner.clearAnimation();
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final SubscribeDialog t = this;
 
         String[] providers = new String[] {};
 
         //Create dialog
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         LayoutInflater inflater = getActivity().getLayoutInflater();
         mDlgLayout = inflater.inflate(R.layout.subscribe_dialog, null);
         builder.setView(mDlgLayout);
         builder.setTitle(R.string.subscribe_dialog_title);
 
         /*
         builder.setItems(providers, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 Log.d(TAG, "CLICKED" + which);
                 RSSFeed feed = new RSSFeed(0, "TEST TITLE", "TEST URL");
                 mListener.onFeedSelected(feed);
                 // The 'which' argument contains the index position
                 // of the selected item
             }
         });*/
 
         // Binds SQLite to list
         Context ctx = this.getActivity();
 
         // Initlialize list
         mFeeds = new ArrayList<RSSFeed>();
         adapter = new FeedListAdapter(ctx, R.layout.feedlist_item, mFeeds);
         mListView = (ListView) mDlgLayout.findViewById(R.id.subscribe_listView);
         mListView. setEmptyView(mDlgLayout.findViewById(R.id.subscribe_emptyView));
         mListView.setAdapter(adapter);
         mListView.setOnItemClickListener(this);
         setBusyIndicatorStatus(false);
 
         // Add listeners to editText
         EditText et = (EditText) mDlgLayout.findViewById(R.id.subscribe_editText);
         et.addTextChangedListener( new TextWatcher() {
             class mCountDownTimer extends  CountDownTimer implements DownloadWebTask.OnRequestCompletedListener {
 
                 public mCountDownTimer(long millisInFuture, long countDownInterval) {
                     super(millisInFuture, countDownInterval);
                 }
 
                 @Override
                 public void onRequestCompleted(String buffer) {
                     mFeeds.clear();
                     setBusyIndicatorStatus(false);
 
                     if (buffer == null){
                         Log.d(TAG, "Got empty buffer, no providers found");
                         return;
                     }
 
                     Log.d(TAG, "Got new providers");
 
                     try {
                         JSONArray jArray = new JSONObject(buffer).getJSONObject("responseData").getJSONArray("entries");
                         for (int i=0; i< jArray.length(); ++i) {
                             JSONObject j = jArray.getJSONObject(i);
                             //FIXME title contains unencoded chars
                             String title = j.getString(DbHelper.FEEDS_TITLE).replaceAll("</*b>","");
                             String url = j.getString(DbHelper.FEEDS_URL);
                             RSSFeed f = new RSSFeed(title, url);
                             mFeeds.add(f);
                         }
 
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                     adapter.notifyDataSetChanged();
                 }
 
                 @Override
                 public void onTick(long l) {}
 
                 @Override
                 public void onFinish() {
                     Log.d(TAG," Start searching");
                     setBusyIndicatorStatus(true);
                     EditText e = (EditText) mDlgLayout.findViewById(R.id.subscribe_editText);
 
                     mFeeds.clear();
 
                     // CLOSE SOFT KEYBOARD
                     InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
 
                     String url = mFindFeedsUrl + URLEncoder.encode(e.getText().toString());
 
 
                     new DownloadWebTask(this).execute(url);
                 }
             }
 
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
 
             @Override
             public void afterTextChanged(Editable editable) {
                 int delayBeforeSearching = 3000;
 
                 if (mSearchTiimer != null)
                     mSearchTiimer.cancel();
 
                 mSearchTiimer = new mCountDownTimer(delayBeforeSearching, delayBeforeSearching).start();
             }
         });
 
 
         return builder.create();
     }
 
     @Override
     public void onDismiss(DialogInterface dialog) {
         super.onDismiss(dialog);
         Log.d(TAG, "Closing dialog");
        if (mMainActivity != null)
            mMainActivity.updateDrawerList();
     }
 }
