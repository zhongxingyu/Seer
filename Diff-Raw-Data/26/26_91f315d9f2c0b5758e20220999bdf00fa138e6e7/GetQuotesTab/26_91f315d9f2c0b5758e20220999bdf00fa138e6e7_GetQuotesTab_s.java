 package com.android.demo.trial;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.demo.db.DataProvider;
 import com.android.demo.db.DemoDatabase;
 import com.android.demo.util.Util;
 
 // import com.geodesic.android.universalIM.GoogleAnalytics.GoogleAnalytics;
 
 public class GetQuotesTab extends Activity
 {
     private String m_szYahooGetCodeURL = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=%s&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
     private String m_szYahooGetQuotesURL = "http://in.finance.yahoo.com/d/quotes.csv?s=%s&f=ophgkjc6cl1t1d1v";
     YahooSymbolList symbols;
     ListView quotesList;
     ProgressDialog m_searchProgress = null;
     private EditText editTextSearch;
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.getquotesactivity );
 
         
         // GoogleAnalytics.trackPageView( this, GoogleAnalytics.FAVORITESTAB_PAGE );
        Util.setNetworkStatus();
 
         final Button btnSearch = (Button) findViewById( R.id.btnSearch );
         btnSearch.setEnabled( false );
         editTextSearch = (EditText) findViewById( R.id.edittextSearch );
         editTextSearch.addTextChangedListener( new TextWatcher()
         {
 
             @Override
             public void onTextChanged( CharSequence s, int start, int before, int count )
             {
                 // TODO Auto-generated method stub
                 if( editTextSearch.getText().toString() != "" && editTextSearch.getText().toString().length() > 0 )
                     btnSearch.setEnabled( true );
                 else
                     btnSearch.setEnabled( false );
             }
 
             @Override
             public void beforeTextChanged( CharSequence s, int start, int count, int after )
             {
                 // TODO Auto-generated method stub
 
             }
 
             @Override
             public void afterTextChanged( Editable s )
             {
                 // TODO Auto-generated method stub
             }
         } );
 
         btnSearch.setOnClickListener( new OnClickListener()
         {
 
             @Override
             public void onClick( View v )
             {
                 // TODO Auto-generated method stub
 
                 String szSearchString = editTextSearch.getText().toString();
                 if( szSearchString != "" )
                 {
                     try
                     {
                         szSearchString = szSearchString.replace( " ", "%20" );
                         szSearchString = URLEncoder.encode( szSearchString, "UTF-8" );
                         final String szQueryURL = String.format( m_szYahooGetCodeURL, szSearchString );
                         if( Util.getNetworkStatus() )
                         {
                             m_searchProgress = ProgressDialog.show( GetQuotesTab.this, "Fetching result", "Please wait..." );
                             m_searchProgress.setCancelable( true );
                             searchTask task = new searchTask();
                             task.szQueryURL = szQueryURL;
                             task.execute();
                         }
                         else
                         {
                             Toast.makeText( GetQuotesTab.this, getString( R.string.networknotavailable ), Toast.LENGTH_LONG ).show();
                         }
                     }
                     catch( Exception e )
                     {
                         // TODO: handle exception
                         e.printStackTrace();
                     }
                 }
             }
         } );
 
         quotesList = (ListView) findViewById( R.id.quotesList );
         quotesList.setOnItemClickListener( new OnItemClickListener()
         {
             @Override
             public void onItemClick( AdapterView< ? > arg0, View view, int arg2, long arg3 )
             {
                 // TODO Auto-generated method stub
                 TextView txtName = (TextView) view.findViewById( R.id.name );
                 TextView txtExch = (TextView) view.findViewById( R.id.exch );
                 TextView txtSymbol = (TextView) view.findViewById( R.id.symbol );
 
                 String szName = txtName.getText().toString();
                 String szExch = txtExch.getText().toString();
                 String szSymbol = txtSymbol.getText().toString();
 
                 Log.d( "On ITEM CLICK ", " Name is : " + szName );
                 Log.d( "On ITEM CLICK ", " Exchg is : " + szExch );
                 Log.d( "On ITEM CLICK ", " Symbol is : " + szSymbol );
 
                 try
                 {
                     szSymbol = URLEncoder.encode( szSymbol, "UTF-8" );
                     // String szGetQuoteURL = "http://finance.yahoo.com/d/quotes.csv?s=" + szSymbol + "&f=snd1l1yr";
                     // String szGetQuoteURL = "http://finance.yahoo.com/d/quotes.csv?s=" + szSymbol +
                     // "&f=ophgkjc6cl1t1d1v";
                     // String szGetQuoteURL = "http://in.finance.yahoo.com/d/quotes.csv?s=" + szSymbol +
                     // "&f=ophgkjc6cl1t1d1v";
                     String szGetQuoteURL = String.format( m_szYahooGetQuotesURL, szSymbol );
 
                     if( Util.getNetworkStatus() )
                     {
                         m_searchProgress = ProgressDialog.show( GetQuotesTab.this, "Fetching result", "Please wait..." );
                         m_searchProgress.setCancelable( true );
                         fetchQuotesTask task = new fetchQuotesTask();
                         task.szQueryURL = szGetQuoteURL;
                         task.szName = szName;
                         task.szSymbol = szSymbol;
                         int nPos = szExch.indexOf( "-" );
                         task.szExch = szExch.substring( 0, nPos );
                         task.szType = szExch.substring( nPos + 1 );
                         task.execute();
                     }
                     else
                     {
                         Toast.makeText( GetQuotesTab.this, getString( R.string.networknotavailable ), Toast.LENGTH_LONG ).show();
                     }
                 }
                 catch( Exception e )
                 {
                     // TODO: handle exception
                     e.printStackTrace();
                 }
             }
         } );
 
     }
 
     @Override
     protected void onResume()
     {
         // TODO Auto-generated method stub
         super.onResume();
         TextView emptyView = (TextView) findViewById( android.R.id.empty );
         if( quotesList != null )
             emptyView.setVisibility( View.GONE );
     }
 
     private Handler handler = new Handler()
     {
         @Override
         public void handleMessage( Message msg )
         {
             setAdapter();
         }
     };
 
     private StringBuilder inputStreamToString( InputStream content ) throws IOException
     {
         // TODO Auto-generated method stub
         String line = "";
         StringBuilder total = new StringBuilder();
 
         BufferedReader rd = new BufferedReader( new InputStreamReader( content ) );
         while( ( line = rd.readLine() ) != null )
         {
             total.append( line );
         }
         return total;
     }
 
     private void setAdapter()
     {
         // TODO Auto-generated method stub
         StockListAdapter adapter = new StockListAdapter( GetQuotesTab.this, symbols );
         quotesList.setAdapter( adapter );
         if( m_searchProgress != null )
         {
             m_searchProgress.dismiss();
             m_searchProgress = null;
         }
         String szSearchString = editTextSearch.getText().toString();
         TextView emptyView = (TextView) findViewById( android.R.id.empty );
         if( quotesList != null )
         {
             emptyView.setVisibility( View.GONE );
             editTextSearch.setText( "" );
         }
         if( symbols.isEmpty() )
         {
             emptyView.setVisibility( View.VISIBLE );
             emptyView.setText( "No match found for your search : '" + szSearchString + "'" );
             editTextSearch.setText( "" );
         }
         else
         {
             emptyView.setVisibility( View.GONE );
             editTextSearch.setText( "" );
         }
     }
 
     public class StockListAdapter extends BaseAdapter
     {
 
         private LayoutInflater mInflater;
         YahooSymbolList symbols = new YahooSymbolList();
 
         public StockListAdapter( Context context, YahooSymbolList data )
         {
             mInflater = LayoutInflater.from( context );
             symbols = data;
         }
 
         public void setData( YahooSymbolList data )
         {
             symbols = data;
         }
 
         @Override
         public int getCount()
         {
             // TODO Auto-generated method stub
             return symbols.size();
         }
 
         @Override
         public Object getItem( int position )
         {
             // TODO Auto-generated method stub
             return null;
         }
 
         @Override
         public long getItemId( int position )
         {
             // TODO Auto-generated method stub
             return 0;
         }
 
         @Override
         public View getView( int position, View convertView, ViewGroup parent )
         {
             // TODO Auto-generated method stub
             final ViewHolder holder;
 
             if( convertView == null )
             {
                 convertView = mInflater.inflate( R.layout.quotestabrow, null );
                 holder = new ViewHolder();
                 holder.stockname = (TextView) convertView.findViewById( R.id.name );
                 holder.exch = (TextView) convertView.findViewById( R.id.exch );
                 holder.symbol = (TextView) convertView.findViewById( R.id.symbol );
                 convertView.setTag( holder );
             }
             else
             {
                 holder = (ViewHolder) convertView.getTag();
             }
             YahooSymbol item = symbols.get( position );
             holder.stockname.setText( item.getName() );
             holder.exch.setText( item.getExch() + "-" + item.getType() );
             holder.symbol.setText( item.getSymbol() );
             return convertView;
         }
 
         class ViewHolder
         {
 
             TextView stockname, exch, symbol;
         }
     }
 
     private class searchTask extends AsyncTask< String, Void, String >
     {
         String szQueryURL = "";
 
         @Override
         protected String doInBackground( String... arg0 )
         {
 
             String szSymbol = "";
             String szName = "";
             String szExch = "";
             String szType = "";
             try
             {
                 HttpClient httpclient = new DefaultHttpClient();
                 HttpGet request = new HttpGet();
                 request.setURI( new URI( szQueryURL ) );
                 HttpResponse response = httpclient.execute( request );
                 String szResponse = inputStreamToString( response.getEntity().getContent() ).toString();
                 Log.d( "GetQuotesTab", "Response is :" + szResponse );
                 if( !szResponse.startsWith( "Error" ) )
                 {
                     szResponse = szResponse.substring( szResponse.indexOf( '(' ) + 1, szResponse.length() - 1 );
 
                     JSONObject mainObject = new JSONObject( szResponse );
                     JSONObject resultSet = mainObject.getJSONObject( "ResultSet" );
                     JSONArray result = resultSet.getJSONArray( "Result" );
 
                     symbols = new YahooSymbolList();
                     if( result.length() > 0 )
                     {
 
                         for( int i = 0; i < result.length(); ++i )
                         {
                             JSONObject item = result.getJSONObject( i );
                             szSymbol = ( item.getString( "symbol" ) );
                             szName = ( item.getString( "name" ) );
                             szExch = ( item.getString( "exch" ) );
                             szType = ( item.getString( "type" ) );
 
                             YahooSymbol obj = new YahooSymbol();
                             obj.setSymbol( szSymbol );
                             obj.setName( szName );
                             obj.setExch( szExch );
                             obj.setType( szType );
                             symbols.add( obj );
 
                             Log.d( "Parsing Data Response ", " *******  symbol: " + szSymbol );
                             Log.d( "Parsing Data Response ", " *******  name: " + szName );
                             Log.d( "Parsing Data Response ", " *******  exch: " + szExch );
                             Log.d( "Parsing Data Response ", " *******  type: " + szType );
                         }
                     }
                     else
                     {
                         Log.d( "Search-onClick", "No Results found. Try Again..." );
                         return "no results";
                     }
                 }
 
             }
             catch( Exception e )
             {
                 // TODO: handle exception
                 e.printStackTrace();
                 return "error";
             }
             return "success";
         }
 
         protected void onPostExecute( String szResult )
         {
             if( szResult == "success" )
             {
                 handler.sendEmptyMessage( 0 );
             }
             super.onPostExecute( szResult );
         }
 
     }
 
     private class fetchQuotesTask extends AsyncTask< String, Void, String >
     {
         String szQueryURL = "";
         String szResponse = "";
         String szName = "";
         String szExch = "";
         String szSymbol = "";
         String szType = "";
 
         @Override
         protected String doInBackground( String... arg0 )
         {
             try
             {
                 HttpClient httpclient = new DefaultHttpClient();
                 HttpGet request = new HttpGet();
                 request.setURI( new URI( szQueryURL ) );
                 HttpResponse response = httpclient.execute( request );
                 szResponse = inputStreamToString( response.getEntity().getContent() ).toString();
                 Log.d( "GetQuotesTab-fetchQuotesTask", "Response is :" + szResponse );
             }
             catch( Exception e )
             {
                 // TODO: handle exception
                 e.printStackTrace();
                 return "error";
             }
             return "success";
         }
 
         protected void onPostExecute( String szResult )
         {
             if( m_searchProgress != null )
             {
                 m_searchProgress.dismiss();
                 m_searchProgress = null;
             }
             if( szResult == "success" )
             {
 
                 String[] RowData = szResponse.split( "," );
                 try
                 {
                     String szPercentChange = RowData[7].replaceAll( "\"", "" );
                     szPercentChange = szPercentChange.substring( szPercentChange.lastIndexOf( "- " ) + 1 );
 
                     Util.getDB().delete( DemoDatabase.STOCKSS_TABLE, DataProvider.Stocks.SYMBOL + " ='" + szSymbol + "'", null );
 
                     ContentValues stockValues = new ContentValues();
                     stockValues.put( DataProvider.Stocks.SYMBOL, szSymbol );
                     stockValues.put( DataProvider.Stocks.NAME, szName );
                     stockValues.put( DataProvider.Stocks.EXCHANGE, szExch );
                     stockValues.put( DataProvider.Stocks.TYPE, szType );
                     stockValues.put( DataProvider.Stocks.OPEN, RowData[0] );
                     stockValues.put( DataProvider.Stocks.CLOSE, RowData[1] );
                     stockValues.put( DataProvider.Stocks.HIGH, RowData[2] );
                     stockValues.put( DataProvider.Stocks.LOW, RowData[3] );
                     stockValues.put( DataProvider.Stocks.YEARHIGH, RowData[4] );
                     stockValues.put( DataProvider.Stocks.YEARLOW, RowData[5] );
                     stockValues.put( DataProvider.Stocks.REALTIMECHANGE, RowData[6].replaceAll( "\"", "" ) );
                     stockValues.put( DataProvider.Stocks.PERCENTCHANGE, szPercentChange );
                     stockValues.put( DataProvider.Stocks.LASTTRADEPRICE, RowData[8] );
                     stockValues.put( DataProvider.Stocks.LASTTRADETIME, RowData[9].replaceAll( "\"", "" ) );
                     stockValues.put( DataProvider.Stocks.LASTTRADEDATE, RowData[10].replaceAll( "\"", "" ) );
                     stockValues.put( DataProvider.Stocks.VOLUME, RowData[11] );
                     Util.getDB().insert( DemoDatabase.STOCKSS_TABLE, stockValues );
 
                     Intent intent = new Intent( GetQuotesTab.this, Quote.class );
                     intent.putExtra( "quotesymbol", szSymbol );
                     startActivity( intent );
                 }
                 catch( Exception e )
                 {
                     // TODO: handle exception
                     e.printStackTrace();
                 }
             }
             super.onPostExecute( szResult );
         }
     }
 }
