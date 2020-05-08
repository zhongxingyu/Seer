 package com.abonec.AmoviesParser;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abonec
  * Date: 8/28/13
  * Time: 3:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ResultActivity extends Activity {
     private Context context;
     private ListView resultListView;
    private AmovieParser.Serial serial;
     private ProgressBar resultProgressBar;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.result_activity);
         this.context = this;
         String url = getIntentString();
         TextView textView = (TextView)findViewById(R.id.text_view);
         textView.setText(url);
         this.resultProgressBar = (ProgressBar)findViewById(R.id.resultProgressBar);
         resultListView = (ListView)findViewById(R.id.resultListView);
         resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AmovieParser.SerialEpisode episode = serial.episodes.get(position);
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setDataAndType(Uri.parse(episode.p720.toString()), "video/mp4");
                 startActivity(intent);
             }
         });
         loadOrParseSeries();
     }
 
     private void loadOrParseSeries() {
        AmoviesParserApplication application = (AmoviesParserApplication)getApplication();
         String url = getIntentString();
         if(application.serial == null || !application.serial.amoviesUrl.toString().equals(url)){
             try {
                 new GetEpisodesTask().execute(new URL(url));
             } catch (MalformedURLException e) {
             }
         }else{
             populateResult(application.serial);
         }
     }
 
     private String getIntentString() {
         Intent intent = getIntent();
         if(intent != null) {
             return intent.getStringExtra(Intent.EXTRA_TEXT);
         }
         return null;
     }
 
     private void populateResult(AmovieParser.Serial serial){
         ResultEpisodesAdapter adapter = new ResultEpisodesAdapter(context, R.id.episode, serial.episodes);
         resultListView.setAdapter(adapter);
     }
 
     private class GetEpisodesTask extends AsyncTask<URL, Integer, AmovieParser.Serial> {
         @Override
         protected AmovieParser.Serial doInBackground(URL... params) {
             AmovieParser parser = new AmovieParser(params[0]);
             parser.progressCallback = new AmovieParser.ProgressUpdate() {
                 @Override
                 public void progressUpdate(int current, int max) {
                     publishProgress(current, max);
                 }
             };
             return parser.parseSerial();
         }
 
         @Override
         protected void onPostExecute(AmovieParser.Serial serial) {
            AmoviesParserApplication application = (AmoviesParserApplication)ResultActivity.this.getApplication();
            application.serial = serial;
             resultProgressBar.setVisibility(View.INVISIBLE);
             populateResult(serial);
         }
 
         /**
          * Runs on the UI thread after {@link #publishProgress} is invoked.
          * The specified values are the values passed to {@link #publishProgress}.
          *
          * @param values The values indicating progress.
          * @see #publishProgress
          * @see #doInBackground
          */
         @Override
         protected void onProgressUpdate(Integer... values) {
             int current = values[0];
             int max = values[1];
             resultProgressBar.setProgress((int)(current*1.0/max * 100));
         }
     }
 }
