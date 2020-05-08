 
 package com.yoosufnabeel.mvscholarships;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 
 public class EndlessListViewActivity extends AbstractListViewActivity  {
 
     private Context context;
     private ListView listView1;
     private ImageView btnRefresh;
     private ArrayList<Scholarship> itemlist = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.list_scholarship);
 
 
 //        btnRefresh = (ImageView)findViewById(R.id.btnRefresh);
 //        btnRefresh.setOnClickListener( new View.OnClickListener() {
 //            @Override
 //            public void onClick(View view) {
 //
 //                Intent newIntent = new Intent(EndlessListViewActivity.this,  EndlessListViewActivity.class);
 //                startActivity(newIntent);
 //                finish();
 //            }
 //        });
 
         if (!isOnline()) {
 
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(
                     "An internet connections is required by this application")
                     .setTitle("Error!")
                      .setIcon(R.drawable.ic_launcher)
 
                     .setPositiveButton("Ok",
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                                     int id) {
                                     EndlessListViewActivity.this.finish();
                                 }
                             });
             AlertDialog alert = builder.create();
             alert.show();
         }
         else
         {
             footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer, null, false);
             getListView().addFooterView(footerView, null, false);
 
             new RetriveScholarshipForEndlessView().execute();
         }
 
 
 
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.list_scholarship, menu);
         return true;
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         if (item.getItemId() == R.id.refresh) {
 
             Intent intent = new Intent();
             intent.setClass(this, EndlessListViewActivity.class);
             startActivity(intent);
         }
 
         return super.onOptionsItemSelected(item);
     }
 
 
     public boolean isOnline() {
         try {
             ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
             return cm.getActiveNetworkInfo().isConnected();
         } catch (Exception ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     private class RetriveScholarshipForEndlessView extends AsyncTask<Void, Void, Void> {
         private ProgressDialog progress = null;
 
         @Override
         protected Void doInBackground(Void... voids) {
 
             try {
                 URL url = new URL(
                         "http://whiterabbit.mv/ontime/");
                 URLConnection connection = url.openConnection();
 
                 String line;
                 StringBuilder builder = new StringBuilder();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                 while ((line = reader.readLine()) != null) {
                     builder.append(line);
                 }
 
                 ArrayList<Scholarship> lstScholarships = new ArrayList<Scholarship>();
                 JSONArray pages = new JSONArray(builder.toString());
 
                 for (int i = 0; i < pages.length(); ++i) {
                     JSONObject rec = pages.getJSONObject(i);
                     JSONObject jsonPage = rec.getJSONObject("Scholarship");
                     String title = Html.fromHtml(jsonPage.getString("Title")).toString();
 
                     Scholarship schor = new Scholarship(R.drawable.attachment, title);
 
                     JSONArray links = jsonPage.getJSONArray("Links");
 
                     for (int j = 0; j < links.length(); ++j) {
                         String documentName =  Html.fromHtml(links.getJSONObject(j).getString("Name")).toString();
                         String documentLink = links.getJSONObject(j).getString("Link");
 
                         ScholarshipDocument newSchoDocument = new ScholarshipDocument(documentLink, documentName);
                         schor.documents.add(newSchoDocument);
                     }
 
 
                     lstScholarships.add(schor);
 
                 }
                 itemlist = lstScholarships;
 
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
 
             return null;
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
         }
 
         @Override
         protected void onPreExecute() {
             progress = ProgressDialog.show(
                     EndlessListViewActivity.this, null, "loading scholarships");
 
             super.onPreExecute();
         }
 
         @Override
         protected void onPostExecute(Void result) {
 
            currentPage = 1;
             datasource = Datasource.getInstance(itemlist);
             setListAdapter(new ScholarshipAdapter(EndlessListViewActivity.this, R.layout.a_scholarship, datasource.getData(0, PAGESIZE)));
             getListView().removeFooterView(footerView);
 
             getListView().setOnScrollListener(new OnScrollListener() {
                 @Override
                 public void onScrollStateChanged(AbsListView arg0, int arg1) {
                     // nothing here
                 }
 
                 @Override
                 public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 
                     if (load(firstVisibleItem, visibleItemCount, totalItemCount)) {
                         loading = true;
                         getListView().addFooterView(footerView, null, false);
                         (new LoadNextPage()).execute("");
                     }
                 }
             });
 
             progress.dismiss();
 
             super.onPostExecute(result);
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
             super.onProgressUpdate(values);
         }
     }
 
 }
