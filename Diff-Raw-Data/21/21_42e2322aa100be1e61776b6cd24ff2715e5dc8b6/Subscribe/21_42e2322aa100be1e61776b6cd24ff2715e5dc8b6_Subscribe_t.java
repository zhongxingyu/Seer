 package com.gnuton.newshub;
 
 import android.app.Activity;
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
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import com.gnuton.newshub.db.DbHelper;
 import com.gnuton.newshub.db.RSSFeedDataSource;
 import com.gnuton.newshub.tasks.DownloadWebTask;
 import com.gnuton.newshub.types.RSSFeed;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by gnuton on 5/28/13.
  */
 public class Subscribe extends DialogFragment implements ListView.OnItemClickListener {
     private final String TAG = Subscribe.class.getName();
     private CountDownTimer mSearchTiimer;
     private View mDlgLayout;
 
     private final RSSFeedDataSource mFeedDataSource;
     private List<RSSFeed> mFeeds;
 
     private final ListView mDrawerList;
     private ArrayAdapter<RSSFeed> adapter;
     private final String mFindFeedsUrl = "https://ajax.googleapis.com/ajax/services/feed/find?v=1.0&q=";
 
     onDialogListener mListener;
 
     Subscribe(final ListView drawerList){
         super();
         this.mDrawerList = drawerList;
         this.mFeedDataSource = new RSSFeedDataSource(drawerList.getContext());
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
 
     public interface onDialogListener {
         public void onFeedSelected(RSSFeed feed);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         // Verify that the host activity implements the callback interface
         try {
             // Instantiate the NoticeDialogListener so we can send events to the host
             mListener = (onDialogListener) activity;
         } catch (ClassCastException e) {
             // The activity doesn't implement the interface, throw exception
             throw new ClassCastException(activity.toString()
                     + " must implement DialogListener");
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final Subscribe t = this;
 
         String[] providers = new String[] {};
 
         //Create dialog
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         LayoutInflater inflater = getActivity().getLayoutInflater();
         mDlgLayout = inflater.inflate(R.layout.subscribe_dialog, null);
         builder.setView(mDlgLayout);
         builder.setTitle("TEST");
 
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
 
         mFeeds = new ArrayList<RSSFeed>();
         adapter = new ArrayAdapter<RSSFeed>(ctx, android.R.layout.simple_list_item_1, mFeeds);
         ListView lv = (ListView) mDlgLayout.findViewById(R.id.subscribe_listView);
         lv.setAdapter(adapter);
         lv.setOnItemClickListener(this);
 
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
                     EditText e = (EditText) mDlgLayout.findViewById(R.id.subscribe_editText);
 
                     // Fetch RSS list from gnuton.org
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
         ArrayAdapter<RSSFeed> drawerListAdapter = new ArrayAdapter<RSSFeed>(getActivity(), android.R.layout.simple_list_item_1, mFeedDataSource.getAll());
         mDrawerList.setAdapter(drawerListAdapter);
     }
 }
