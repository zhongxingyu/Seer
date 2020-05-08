 /*
     Copyright 2013 Mauricio Teixeira
 
     This file is part of Saldo Ticket.
 
     Saldo Ticket is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Saldo Ticket is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Saldo Ticket.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
     This file was created by Mauricio Teixeira on 6/24/13.
     Based on http://www.coderzheaven.com/expandable-listview-android-simpleexpandablelistadapter-simple-example/
  */
 
 package com.wordpress.mteixeira.saldoticket;
 
 import android.app.AlertDialog;
 import android.app.ExpandableListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleExpandableListAdapter;
 import android.widget.TextView;
 
 import com.google.gson.Gson;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 public class TransactionsActivity extends ExpandableListActivity {
 
     private Context context;
     private String cardNumber;
     private ProgressBar progress;
     private TextView progressText;
     private String errorMessage;
     private List<TicketRestauranteReleases> ticketReleases;
 
     ArrayList monthList = new ArrayList();
     String[] monthAndYear = new String[12];
 
     @Override
     @SuppressWarnings("unchecked")
     public void onCreate(Bundle savedInstanceState) {
         try{
             try {
                 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                 int styleId = FontStyle.valueOf(settings.getString("pref_transactionsFontStyle", "Medium")).getResId();
                 getTheme().applyStyle(styleId, true);
             } catch (NullPointerException e) {
                 // just to make code analysis happy
                 showError("Erro interno t[1]");
                 Log.e(MainActivity.Constants.TAG, String.valueOf(e));
             }
             super.onCreate(savedInstanceState);
             Intent intent = getIntent();
             context = this;
 
             cardNumber = intent.getStringExtra(MainActivity.CARD_NUMBER);
 
             setContentView(R.layout.activity_transactions);
             progress = (ProgressBar)findViewById(R.id.transactionsProgressBar);
             progressText = (TextView)findViewById(R.id.transactionsProgressText);
 
             new ListTransactions().execute();
 
         }catch(Exception e){
             Log.e(MainActivity.Constants.TAG, "Transactions Error +++ " + e.getMessage());
         }
     }
 
     private void showError(String message) {
         errorMessage = message;
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 AlertDialog error = new AlertDialog.Builder(context).create();
                 error.setTitle("Erro");
                 error.setMessage(errorMessage);
                 //error.setIcon(R.drawable.error);
                 error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         TransactionsActivity.this.finish();
                     }
                 });
                 error.show();
             }
         });
         try {
             getMainLooper();
             Looper.loop();
         } catch (RuntimeException e) {
             Log.e(MainActivity.Constants.TAG, String.valueOf(e));
         }
     }
 
     private class ListTransactions extends AsyncTask<Void, Void, Void> {
         @Override
         protected void onPreExecute() {
             progressText.setText(getString(R.string.checking_transactions));
             progress.setIndeterminate(true);
             progress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Void doInBackground(Void... arg0) {
             fetchTransactions();
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
             progress.setVisibility(View.GONE);
             new PopulateTransactionList().execute();
         }
     }
 
     private void fetchTransactions() {
         String strMessage = null;
         try {
             HttpParams httpParameters = new BasicHttpParams();
             // Set the timeout in milliseconds until a connection is established.
             // The default value is zero, that means the timeout is not used.
             int timeoutConnection = 30000;
             HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
             // Set the default socket timeout (SO_TIMEOUT)
             // in milliseconds which is the timeout for waiting for data.
             int timeoutSocket = 40000;
             HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
             DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
             HttpGet request = new HttpGet("http://www.ticket.com.br/ticket-corporativo-web/ticket-consultcard?txtNumeroCartao=" + cardNumber + "&txtOperacao=lancamentos");
             InputStream inputStream;
             HttpResponse response = httpclient.execute(request);
             HttpEntity entity = response.getEntity();
 
             inputStream = entity.getContent();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
             StringBuilder sb = new StringBuilder();
 
             String line;
             while ((line = reader.readLine()) != null) {
                 sb.append(line).append("\n");
             }
             String json = sb.toString();
 
             if(json.equals("")) {
                 showError("Falha na conexão [1]");
                 Log.e(MainActivity.Constants.TAG, "Result string is null.");
             } else {
                 Gson gson = JsonUtil.getGson();
                 TicketRestaurante ticket = null;
                 try {
                     ticket = gson.fromJson(json, TicketRestaurante.class);
                 } catch (IllegalStateException e) {
                     showError("Falha na consulta (serviço remoto enviou dados inválidos) [1]");
                 }
                 if(ticket.getMessageError() == null) {
                     ticketReleases = ticket.getReleases();
                    if(ticketReleases.size() == 0) {
                         showError("O site da não retornou extrato.");
                     }
                 } else {
                     showError(strMessage);
                 }
             }
         } catch (ClientProtocolException e) {
             showError("Falha na conexão [2]");
             Log.e(MainActivity.Constants.TAG, String.valueOf(e));
         } catch (UnknownHostException e) {
             showError("Falha na conexão [3]");
             Log.e(MainActivity.Constants.TAG, String.valueOf(e));
         } catch (IOException e) {
             showError("Falha na conexão [4]");
             Log.e(MainActivity.Constants.TAG, String.valueOf(e));
         }
     }
 
     private class PopulateTransactionList extends AsyncTask<Void, Void, Void> {
         private ExpandableListAdapter expListAdapter;
 
         @Override
         protected void onPreExecute() {
             progressText.setText(getString(R.string.formatting_transactions));
             progress.setIndeterminate(false);
             progress.setProgress(0);
             progress.setMax(100);
             progress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Void doInBackground(Void... arg0) {
             expListAdapter =
                     new SimpleExpandableListAdapter(
                             TransactionsActivity.this,
                             createGroupList(),              // Creating group List.
                             R.layout.group_row,             // Group item layout XML.
                             new String[] { "Month" },  // the key of group item.
                             new int[] { R.id.row_name },    // ID of each group item.-Data under the key goes into this TextView.
                             createChildList(),              // childData describes second-level entries.
                             R.layout.child_row,             // Layout for sub-level entries(second level).
                             new String[] {"Transaction"},      // Keys in childData maps to display.
                             new int[] { R.id.grp_child}     // Data under the keys above go into these TextViews.
                     );
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
             progress.setVisibility(View.GONE);
             progressText.setVisibility(View.GONE);
             setListAdapter(expListAdapter);       // setting the adapter in the list.
         }
     }
 
     /* Creating the Hashmap for the row */
     @SuppressWarnings("unchecked")
     private List createGroupList() {
         String date;
         int monthStep = 0;
         try {
             for(int i = 0; i < ticketReleases.size(); i++) {
                 HashMap m = new HashMap();
                 date = new SimpleDateFormat("MMM/yyyy").format(ticketReleases.get(i).getDate());
                 m.put("Month", date);
                 if(!monthList.contains(m)) {
                     monthList.add(m);
                 }
                 if(!Arrays.asList(monthAndYear).contains(date)) {
                     monthAndYear[monthStep] = date;
                     monthStep++;
                 }
                 progress.setProgress(50 / (ticketReleases.size() - i));
             }
         } catch (NullPointerException e) {
             showError("Erro interno [1]");
             Log.e(MainActivity.Constants.TAG, String.valueOf(e));
         }
         return monthList;
     }
 
     /* creatin the HashMap for the children */
     @SuppressWarnings("unchecked")
     private List createChildList() {
         ArrayList result = new ArrayList();
         String date;
         String month;
         String description;
         String value;
         try {
             for( int i = 0 ; i < monthList.size() ; ++i ) {
                 ArrayList secList = new ArrayList();
                 int releasesAmount = ticketReleases.size();
                 for( int n = 0 ; n < releasesAmount ; n++ ) {
                     // since each item is removed after read, always read the first
                     Date releaseDate = ticketReleases.get(0).getDate();
                     month = new SimpleDateFormat("MMM/yyyy").format(releaseDate);
                     if(month.equals(monthAndYear[i])) {
                         date = new SimpleDateFormat("dd/MM/yyyy").format(releaseDate);
                         description = ticketReleases.get(0).getDescription();
                         value = MainActivity.formatMoney(ticketReleases.get(0).getValue());
                         HashMap child = new HashMap();
                         child.put( "Transaction", date + " - " + value + " - " + description);
                         secList.add( child );
                         ticketReleases.remove(0); // less data to check
                     } else {
                         // releases list is always sequential, so move on when month changes to save time
                         break;
                     }
                 }
                 result.add( secList );
                 progress.setProgress(50 + (50 / (monthList.size() - i)));
             }
         } catch (NullPointerException e) {
             showError("Erro interno [3]");
             Log.e(MainActivity.Constants.TAG, String.valueOf(e));
         }
         return result;
     }
 
     public void  onContentChanged  () {
         System.out.println("onContentChanged");
         super.onContentChanged();
     }
 
     /* This function is called on each child click */
     public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id) {
         Log.e(MainActivity.Constants.TAG, "Inside onChildClick at groupPosition = " + groupPosition + " Child clicked at position " + childPosition);
         return true;
     }
 
     /* This function is called on expansion of the group */
     public void  onGroupExpand  (int groupPosition) {
         try{
             Log.e(MainActivity.Constants.TAG, "Group exapanding Listener => groupPosition = " + groupPosition);
         }catch(Exception e){
             Log.e(MainActivity.Constants.TAG, " groupPosition Errrr +++ " + e.getMessage());
         }
     }
 }
