 package com.cliche818.stockmarketv2;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
import java.math.RoundingMode;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends ListActivity {
 	
 	//debug constants
 	private static final String TAG = "stockmarketv2";
 	
 	// input variables
 	EditText setSymbol;
 	EditText setNoOfStocks;
 
 	// output variables
 	// these symbols allow any methods in this class to use them
 	TextView symbolOut;
 	TextView priceOut;
 	TextView changePercentageOut;
 	TextView bankAccountOut;
 	Button getQuote;
 	Button insertSimulation;
 	Button refreshSimulation;
 	MediaPlayer mpGood;
 	MediaPlayer mpBad;
 	
 	//database variable
 	private StockDBAdapter sDbHelper;
 	
 	//declare globalToast
 	private static Toast globalToast;
 	
 	//shared preference name
 	private static final String PREFS_NAME = "MyPrefsFile";
 	
 	//menu variables
 	private static final int HELP_ID = Menu.FIRST;
 	private static final int ABOUT_ID = 2;
 	private static final int DELETE_ID = Menu.FIRST + 3;
 	
 	//CONSTANTS
 	private static final String NOTVALIDSTOCKPRICE = "0.00";
 	private static final BigDecimal NO_MONEY = new BigDecimal ("0.00");
	private static final int DECIMALPLACES = 2;
 	
 	
 	//class variables to make my life easy
 	String stockSymbol = "N/A";
 	String stockQuote = "N/A";
 	String stockChangePercentage = "N/A";
 	
 	//class variable
 	String bankAccountString;
 	
 	/*
 	 * This sub class is to asynchronously refresh the simulation data
 	 */
 	private class refreshStocksAsync extends AsyncTask <Void, Void, Void>{
 
 		protected Void doInBackground(Void... arg0) {
 			
 			Cursor cur = sDbHelper.fetchAllStocks();
 			String stockToUpdate = "";
 			String updatedRawData = "";
 			cur.moveToFirst();
 			
 			while (cur.isAfterLast() == false){
 				stockToUpdate = cur.getString(1);
 				updatedRawData = getStockInfo(stockToUpdate);
 				String[] tokens = updatedRawData.split(",");
 				//since I know stock quote is the 2nd token
 				sDbHelper.updateStock(cur.getInt(0), stockToUpdate, tokens[1], cur.getString(3), cur.getString(4));
 				cur.moveToNext();
 			}
 			
 			
 			return null;
 		}
 		
 		protected void onPostExecute(Void result) {
 			fillData();
 			refreshSimulation.setText("Refresh");
 			refreshSimulation.setEnabled(true);
 		}
 	}
 	
 	
 	/*
 	 * This sub class is to asynchronously get stock data for GetStock Module
 	 * @param 1st argument is params, the type of the parameters sent to the task upon execution
 	 * @param 2nd argument is progress, the type of progress units published during the background computation
 	 * @param Result, the type of the result of the background computation
 	 */
 	private class getStocksAsync extends AsyncTask <String, Void, String>{
 
 	
 		protected String doInBackground(String... symbolInput) {
 			
 			String cleanedSymbolInput = symbolInput[0].replace(" ", "");
 			String stockTxt = getStockInfo(cleanedSymbolInput);
 			return stockTxt;
 		}
 
 
 		protected void onPostExecute(String stockTxt) {
 			//require a debug message here
 			Log.i(TAG, stockTxt);
 			
 			String[] tokens = stockTxt.split(",");
 
 			stockSymbol = tokens[0];
 			stockQuote = tokens[1];
 			stockChangePercentage = tokens[2];
 
 			// parse the individual tokens, taking out ""
 			String fstockSymbol = stockSymbol.substring(1,stockSymbol.length() - 1);
 			String fstockChangePercentage = stockChangePercentage.substring(1,stockChangePercentage.length() - 3);
 			// checking if a correct stock symbol was entering
 			// looking to see if stock price is 0.00, which is not possible
 			if ( stockQuote.compareTo(NOTVALIDSTOCKPRICE) == 0){
 				
 				/*Toast noSymbol = Toast.makeText(Main.this,
 						"A invalid stock symbol was entered",
 						Toast.LENGTH_LONG);*/
 				globalToast.cancel();
 				globalToast.setText("A invalid stock symbol was entered");
 				globalToast.show();
 				
 				stockQuote = "N/A";
 				fstockChangePercentage = "N/A";
 				symbolOut.setText(fstockSymbol + " is not a valid stock symbol");
 				priceOut.setText(stockQuote);
 				changePercentageOut.setText(fstockChangePercentage);
 				
 			}
 			//correct stock quote was entered
 			else {
 				
 				symbolOut.setText(fstockSymbol);
 				priceOut.setText(stockQuote);
 				changePercentageOut.setText(fstockChangePercentage);
 				
 			}
 				
 			// getting positive or negative sign
 			char c = fstockChangePercentage.charAt(0);
 
 			if (c == '-')
 				mpBad.start();
 			else
 				mpGood.start();
 
 			// erases edit text view after getting quote
 			setSymbol.setText("");
 			
 			//allow the user to get other stocks again
 			getQuote.setText("Get Stock Quote");
 			getQuote.setEnabled(true);
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		//-----------------------------------------Start of Setting up Views for the App-----------------------------------------------------
 		// known as inflate, shows the stuff in our XML file
 		setContentView(R.layout.main);
 		
 		//global toast
 		globalToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
 		
 		// connect reference variables with our view objects
 		setSymbol = (EditText) findViewById(R.id.setSymbol);
 		setNoOfStocks = (EditText) findViewById(R.id.setNoOfStocks);
 		setNoOfStocks.setEnabled(false);
 		
 		symbolOut = (TextView) findViewById(R.id.stockSymbolOutput);
 		priceOut = (TextView) findViewById(R.id.stockPriceOutput);
 		changePercentageOut = (TextView) findViewById(R.id.stockChangePercentageOutput);
 		
 		bankAccountOut = (TextView) findViewById(R.id.bankAccountOutput);
 
 		getQuote = (Button) findViewById(R.id.get_quote_button);
 		
 		insertSimulation = (Button) findViewById(R.id.insert_button);
 		insertSimulation.setEnabled(false);
 
 		refreshSimulation = (Button) findViewById(R.id.refreshSimulation);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		
 		//database setup
 		sDbHelper = new StockDBAdapter(this);
 		sDbHelper.open();
 		fillData();
 		registerForContextMenu(getListView());
 		
 		//creating shared preferences (to save user money/cash account)
 		SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 		bankAccountString = userAccount.getString("bankAccount", "100000.00");
 		bankAccountOut.setText(bankAccountString);
 		
 		//Setting up Tabs
 		TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
 		tabHost.setup();
 		
 		//Setting up Get Quote Module
 		TabHost.TabSpec stockQuoteScreen = tabHost.newTabSpec("StockQuoteTab");
 		stockQuoteScreen.setContent(R.id.stockQuote);
 		stockQuoteScreen.setIndicator("GetStockQuote", getResources().getDrawable(R.drawable.stockquote_grey));
 		tabHost.addTab(stockQuoteScreen);
 		
 		//Setting up Simulation Module
 		TabHost.TabSpec portfolioScreen = tabHost.newTabSpec("simulationTab");
 		portfolioScreen.setContent(R.id.simulation);
 		portfolioScreen.setIndicator("Simulation", getResources().getDrawable(R.drawable.stockquote_grey));
 		tabHost.addTab(portfolioScreen);
 		
 		tabHost.setCurrentTab(0);
 		
 		//check for internet initially
 		if (isInternet()){
 			/*Toast yesInternet = Toast.makeText(Main.this,
 					"Internet is ON, you are clear to engage!",
 					Toast.LENGTH_LONG);*/
 			globalToast.cancel();
 			globalToast.setText("Internet is ON, you are clear to engage!");
 			globalToast.show();
 		}
 		
 		// music to signal good or bad changes in the stock price
 		mpGood = MediaPlayer.create(this, R.raw.good);
 		mpBad = MediaPlayer.create(this, R.raw.bad);
 		//-----------------------------------------End of Setting up Views for the App-----------------------------------------------------
 		
 		
 		//-----------------------------------------Start of Core of the Get Stock Module---------------------------------------------------
 		getQuote.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// hides keyboard
 				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(setSymbol.getWindowToken(), 0);
 				
 				//disable the getQuoteButton
 				getQuote.setText("Getting Stock Quote");
 				getQuote.setEnabled(false);
 				
 				//being diligent in checking for Internet every time
 				isInternet();
 				
 				String symbolInput = setSymbol.getText().toString();
 				
 				if (symbolInput.length() == 0) {
 					/*Toast noSymbol = Toast.makeText(Main.this,
 							"A stock symbol is required to continue",
 							Toast.LENGTH_LONG);*/
 					globalToast.cancel();
 					globalToast.setText("A stock symbol is required to continue");
 					globalToast.show();
 					
 					//re-enable getQuote button
 					getQuote.setText("Get Stock Quote");
 					getQuote.setEnabled(true);
 				}
 
 				else {
 					getStocksAsync getStockTask = new getStocksAsync();
 					getStockTask.execute(symbolInput);
 					
 					//only now is it possible to add stock symbols to database
 					insertSimulation.setEnabled(true);
 					setNoOfStocks.setEnabled(true);
 				}
 			}
 		});
 		//-----------------------------------------End of Core of the Get Stock Module---------------------------------------------------
 		
 		
 		//-----------------------------------------Start of Core of Simulation Module in GetStockQuote Tab----------------------------------------------------
 		insertSimulation.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String noOfStocksString = setNoOfStocks.getText().toString();
 				createStock(noOfStocksString);
 			}	
 		});
 		
 		//-----------------------------------------End of Core of Simulation Module in GetStockQuote Tab----------------------------------------------------
 		
 		
 		//-----------------------------------------Start of Core of Simulation Module in GetStockQuote Tab----------------------------------------------------
 		refreshSimulation.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				refreshStocksAsync refreshTask = new refreshStocksAsync();
 				refreshSimulation.setText("Refreshing");
 				refreshSimulation.setEnabled(false);
 				refreshTask.execute();
 			}	
 		});
 		
 		
 		
 		//-----------------------------------------End of Core of Simulation Module in GetStockQuote Tab----------------------------------------------------
 	}
 	
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0, DELETE_ID, 0, R.string.menu_delete);
     }
 
     /*
      * Option to delete/sell stock from simulation mode
      */
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case DELETE_ID:
                 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                 
                 //before deleting stock, it must first be "sold"
                 //operations to change the user's bankAccount information
                 Cursor cur = sDbHelper.fetchStock(info.id);
                 BigDecimal bankAccountBigDecimal = new BigDecimal (bankAccountString);
     			BigDecimal stockQuoteBigDecimal = new BigDecimal (cur.getString(3));
     			BigDecimal noOfStocksBigDecimal = new BigDecimal (cur.getString(4));
     			
     			stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
     			bankAccountBigDecimal = bankAccountBigDecimal.add(stockQuoteBigDecimal);
     			
     			//forgot to set our "global" bank account string, bug fix
    			bankAccountBigDecimal.setScale(DECIMALPLACES, RoundingMode.HALF_UP);
     			bankAccountString = bankAccountBigDecimal.toString();
     			
    			
     			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
     			SharedPreferences.Editor editor = userAccount.edit();
     			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
     			bankAccountOut.setText(bankAccountBigDecimal.toString());
                 editor.commit();
     			
                 sDbHelper.deleteStock(info.id);
                 fillData();
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, HELP_ID, 0, R.string.menu_insert);
 		menu.add(1, ABOUT_ID, 1, R.string.menu_insert2);
 		return result;
 	}
 
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()){
 		case HELP_ID:
 			return true;
 		case ABOUT_ID:
 			final Dialog aboutDialog = new Dialog(Main.this);
 			aboutDialog.setContentView (R.layout.about);
 			aboutDialog.setTitle("About");
 			aboutDialog.setCancelable(true);
 			
 			//setup text (about message)
 			TextView aboutText = (TextView) aboutDialog.findViewById(R.id.aboutWindow);
 			aboutText.setText("Written by Jeff.\nBecause this box finally works.\n");
 			
 			//do the exit button
 			Button aboutButton = (Button) aboutDialog.findViewById(R.id.aboutButton);
 			aboutButton.setOnClickListener (new View.OnClickListener (){
 			@Override
 				public void onClick (View v){
 					aboutDialog.dismiss();
 			}
 			});
 			aboutDialog.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 
 
 	/*
 	 * The function sees if the app has access to either wifi internet or mobile internet
 	 */
 	protected boolean isInternet(){
 		ConnectivityManager connectmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		
 		
 		//facts: getNetworkInfo (1) is for WIFI internet
 		//		 getNetworkInfo (0) is for MOBILE internet 
 		
 		if (connectmgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
 				connectmgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED){
 			return true;
 		}
 		else{
 			/*Toast noInternet = Toast.makeText(Main.this,
 					"There is no internet, disengage!",
 					Toast.LENGTH_LONG);*/
 			globalToast.cancel();
 			globalToast.setText("There is no internet, disengage!");
 			globalToast.show();
 			return false;
 		}
 		
 	}
 	
 	/*
 	 * This function gets data by accepting a symbol (ex. amd)
 	 * returns a string that needs to be parsed outside
 	 */
 	protected String getStockInfo( String symbolInput){
 		
 		URL url;
 		String stockTxt = "bad data or something bad happened";
 		try {
 				// getting info from Yahoo Finance API [meat of the
 				// program]
 				url = new URL(
 						"http://download.finance.yahoo.com/d/quotes.csv?s="
 								+ symbolInput + "&f=sl1p2");
 
 
 				InputStream stream = url.openStream();
 				
 				//convert stream to string
 				//reason to use bufferedReader is so there are more functions to use: readLine()
 				BufferedReader r = new BufferedReader(new InputStreamReader(stream));
 				
 				
 				stockTxt = r.readLine();
 
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return stockTxt;
 	}
 	
 	private void createStock(String noOfStocksString){
 
 		//operation to change the user's bank account (buying)
 		BigDecimal bankAccountBigDecimal = new BigDecimal (bankAccountString);
 		BigDecimal stockQuoteBigDecimal = new BigDecimal (stockQuote);
 		BigDecimal noOfStocksBigDecimal = new BigDecimal (noOfStocksString);
 		
 		stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
 		bankAccountBigDecimal = bankAccountBigDecimal.subtract(stockQuoteBigDecimal);
 		
 		//check if there the user has enough cash....don't want cash account to go below 0
 		if (bankAccountBigDecimal.compareTo(NO_MONEY) == -1){
 			globalToast.cancel();
 			globalToast.setText("You don't have enough money!");
 			globalToast.show();
 		}
 			
 		else{
 			
 			//forgot to set our "global" bank account string, bug fix
			bankAccountBigDecimal.setScale(DECIMALPLACES, RoundingMode.HALF_UP);
 			bankAccountString = bankAccountBigDecimal.toString();
 			
 			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 			SharedPreferences.Editor editor = userAccount.edit();
 			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
 			bankAccountOut.setText(bankAccountBigDecimal.toString());
 			editor.commit();
 			
 			// add the stock to database
 			long insertMsg = sDbHelper.createStock(stockSymbol, stockQuote, stockQuote, noOfStocksString);
 			if (insertMsg == -1){
 				/*Toast noInsert = Toast.makeText(Main.this,
 						"You had already added this stock symbol before!",
 						Toast.LENGTH_LONG);*/
 				globalToast.cancel();
 				globalToast.setText("You had already added this stock symbol before!");
 				globalToast.show();
 			}
 			
 			globalToast.cancel();
 			globalToast.setText("Successfully inserted the stock symbol!");
 			globalToast.show();
 			
 			fillData();
 		}
 		
 		
 	}
 	
 	private void fillData(){
 		// Get all of the notes from the database and create the item list
         Cursor c = sDbHelper.fetchAllStocks();
         startManagingCursor(c);
                 
         String[] from = new String[] { StockDBAdapter.STOCK_KEY, StockDBAdapter.STOCK_PRICE_NEW, StockDBAdapter.STOCK_PRICE_OLD, StockDBAdapter.STOCK_NUM };
         int[] to = new int[] { R.id.stockText, R.id.stockText2, R.id.stockText3, R.id.stockText4 };
         
         // Now create an array adapter and set it to display using our row
         SimpleCursorAdapter stocks =
             new SimpleCursorAdapter(this, R.layout.stocks_row, c, from, to);
         
         //ListView simulationList = (ListView) findViewById(R.id.simulationListView);
         //simulationList.setAdapter (stocks);
         this.setListAdapter(stocks);
 	}
 
 
 }
