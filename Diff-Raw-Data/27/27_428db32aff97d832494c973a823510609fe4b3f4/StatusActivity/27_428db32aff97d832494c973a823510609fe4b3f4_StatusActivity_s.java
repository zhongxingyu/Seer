 
 package com.twitter.android.yamba;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.marakana.android.yamba.clientlib.YambaClient;
 import com.marakana.android.yamba.clientlib.YambaClientException;
 
 public class StatusActivity extends Activity {
     public static final String TAG = "STATUS";
 
     class Poster extends AsyncTask<String, Void, Integer> {
 
         @Override
         protected Integer doInBackground(String... status) {
             int message = R.string.fail;
             try {
                 client.postStatus(status[0]);
                 message = R.string.success;
             }
             catch (YambaClientException e) {
                 Log.e(TAG, "Post failed");
             }
             return Integer.valueOf(message);
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             Toast.makeText(StatusActivity.this, result.intValue(), Toast.LENGTH_LONG).show();
             poster = null;
         }
     }
 
     static Poster poster;
 
 
     volatile YambaClient client;
 
     private int okColor;
     private int warnColor;
     private int errColor;
 
     private int statusLenMax;
     private int warnMax;
     private int errMax;
 
     private EditText statusView;
     private TextView countView;
     private Button submitButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Resources rez = getResources();
         okColor = rez.getColor(R.color.green);
         statusLenMax = rez.getInteger(R.integer.status_limit);
         warnColor = rez.getColor(R.color.yellow);
         warnMax = rez.getInteger(R.integer.warn_limit);
         errColor = rez.getColor(R.color.red);
         errMax = rez.getInteger(R.integer.err_limit);
 
         client = new YambaClient("student", "password");
 
         setContentView(R.layout.activity_status);
 
         countView = (TextView) findViewById(R.id.status_count);
 
         submitButton = (Button) findViewById(R.id.status_submit);
         submitButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) { post(); }
         });
 
         statusView = (EditText) findViewById(R.id.status_status);
         statusView.addTextChangedListener(
                 new TextWatcher() {
                     @Override
                     public void afterTextChanged(Editable s) { updateCount(); }
 
                     @Override
                     public void beforeTextChanged(CharSequence s, int b, int n, int a) { }
 
                     @Override
                     public void onTextChanged(CharSequence s, int b, int p, int n) { }
                 });
     }
 
     void updateCount() {
        int n = statusLenMax - statusView.getText().length();
 
         int color;
         if (n > warnMax) { color = okColor; }
         else if (n > errMax) { color = warnColor; }
         else  { color = errColor; }
 
         countView.setText(String.valueOf(n));
         countView.setTextColor(color);
        submitButton.setEnabled(checkText(n));
     }
 
     void post() {
        String status = statusView.getText().toString();
 
         if (BuildConfig.DEBUG) { Log.d(TAG, "posting: " + status); }
        if (!checkText(status.length())) { return; }
 
        if (null != poster) { return; }

        poster = new Poster();
         statusView.setText("");
         poster.execute(status);
     }
 
    private boolean checkText(int n) {
        return (n > errMax) && (n < statusLenMax);
     }
 }
