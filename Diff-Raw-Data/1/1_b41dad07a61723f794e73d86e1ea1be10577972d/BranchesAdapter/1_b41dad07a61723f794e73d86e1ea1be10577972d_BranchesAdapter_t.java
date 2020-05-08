 package com.jason.ocbcapp;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // A base adapter with modified elements to put buttons in list elements
 public class BranchesAdapter extends BaseAdapter {
 
     private static final String APP_TAG = MainActivity.APP_TAG;
 
     private Activity activity;
     private ArrayList<String> data;
     private int[] waitingTimes;
     private static LayoutInflater inflater = null;
 
     public BranchesAdapter(Activity activity, ArrayList<String> data) {
         this.activity = activity;
         this.data = data;
        waitingTimes = new int[data.size()];
         for (int i = 0; i < waitingTimes.length; i++) {
             waitingTimes[i] = -1;
         }
         inflater = activity.getLayoutInflater();
     }
 
     @Override
     public int getCount() {
         return data.size();
     }
 
     @Override
     public Object getItem(int position) {
         return data.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     // We use the ViewHolder pattern here to optimize the performance.
     // This is to prevent extra calls to findViewById.
     // See http://sriramramani.wordpress.com/2012/07/25/infamous-viewholder-pattern/
     // and http://www.vogella.com/articles/AndroidListView/article.html#adapterperformance
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null;
 
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.branches_row, null);
 
             holder = new ViewHolder();
             holder.name = (TextView) convertView.findViewById(R.id.branchName);
             holder.btn = (Button) convertView.findViewById(R.id.waitingTimeBtn);
             holder.pb = (ProgressBar) convertView
                     .findViewById(R.id.progressBar);
 
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         holder.pb.setVisibility(View.INVISIBLE);
 
         holder.name.setText(data.get(position));
 
         // The android adapter reuses views, we cannot simply set the text to
         // buttons. The references are reused and we may end up setting the text
         // some of the other buttons not seen when scrolling. To fix this, we
         // create an array containing the waiting times to cache the information
         // for every row. If the waiting time is -1, that is the user hasn't retrieved
         // the waiting time yet, we force the text to be empty.
 
         int buttonState = waitingTimes[position];
         if (buttonState != -1) {
             holder.btn.setText(buttonState + "mins");
         } else {
             holder.btn.setText("");
         }
         // associate the position of the button in the List with the button itself.
         // we need this later when we are storing the retrieved waiting time.
         holder.btn.setTag(position);
         holder.btn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Button btnClicked = (Button) v;
                 View view = (View) v.getParent();
                 ProgressBar pb = (ProgressBar) view
                         .findViewById(R.id.progressBar);
                 Log.d(APP_TAG, "executing task");
                 RequestTask task = new RequestTask();
                 // we shall send three args to the RequestTask.
                 // 1) String containing the url to call
                 // 2) The button that the user clicked
                 // 3) The progress bar associated to the button
                 // This way, we do not need to make another findViewById call and we can
                 // simply reuse the objects.
                 task.execute(new Triple<String, Button, ProgressBar>(
                         "http://cutebalrog.com:8080/OCBC-QM-Server-web/webresources/Branch/GetBranch/"
                                 + position, btnClicked, pb));
                 pb.setVisibility(View.VISIBLE);
                 Log.d(APP_TAG, "Visibility: " + pb.getVisibility());
                 pb.bringToFront();
             }
         });
         return convertView;
     }
 
     class RequestTask extends
             AsyncTask<Triple<String, Button, ProgressBar>, String, String> {
 
         private Button btnClicked = null;
         private ProgressBar pb = null;
 
         @Override
         protected String doInBackground(
                 Triple<String, Button, ProgressBar>... triple) {
             String responseString = null;
             InputStream responseStream = null;
             HttpURLConnection urlConnection = null;
             try {
                 URL url = new URL(triple[0].first);
                 btnClicked = triple[0].second;
                 pb = triple[0].third;
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.addRequestProperty("Content-type",
                         "application/json");
                 responseStream = new BufferedInputStream(
                         urlConnection.getInputStream());
                 Log.d(APP_TAG,
                         "response code: " + urlConnection.getResponseCode());
                 responseString = readStream(responseStream);
             } catch (Exception e) {
                 Log.e(APP_TAG, e.getMessage());
             }
             return responseString;
         }
 
         private String readStream(InputStream inputStream) {
             // TODO Auto-generated method stub
             StringBuilder buf = new StringBuilder();
             Scanner sc = null;
             try {
 
                 sc = new Scanner(inputStream);
                 while (sc.hasNext()) {
                     buf.append(sc.next());
                 }
 
             } catch (Exception e) {
                 Log.e(APP_TAG, e.getMessage());
             }
             return buf.toString();
         }
 
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             // Extract waiting time from json object
             Log.d(APP_TAG, "finished request task, showing waiting time");
             try {
                 JSONObject jObj = new JSONObject(result);
                 String waitingTime = jObj.getString("waitingTime");
                 int position = (Integer) btnClicked.getTag();
                 waitingTimes[position] = Integer.parseInt(waitingTime);
                 btnClicked.setText(waitingTime + "mins");
                 pb.setVisibility(View.INVISIBLE);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
     }
 
     static class ViewHolder {
         TextView name;
         Button btn;
         ProgressBar pb;
     }
 
     static class Triple<F, S, T> {
         public final F first;
         public final S second;
         public final T third;
 
         public Triple(F first, S second, T third) {
             this.first = first;
             this.second = second;
             this.third = third;
         }
     }
 }
