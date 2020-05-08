 package com.cliche818.stockmarketv2;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
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
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
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
 	TextView companyNameOut;
 	TextView symbolOut;
 	TextView priceOut;
 	TextView changePercentageOut;
 	TextView bankAccountOut;
 	TextView updateOut;
 	Button getQuote;
 	Button insertSimulation;
 	Button refreshSimulation;
 	MediaPlayer mpGood;
 	MediaPlayer mpBad;
 	Button saveToPortfolio ;
 	
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
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a 'on' yyyy-MM-dd");
 	
 	// Database
 	SQLiteDatabase db;
 	Cursor dbCursor;
 	
 	// Lists
 	ListView portfolio;
 	ListView gameAsset;
 	ListAdapter adapter;
 	
 	// Stored variables
 	String lastTicker ;
 	
 	//class variables to make my life easy
 	String stockCompanyName = "N/A";
 	String stockSymbol = "N/A";
 	String stockQuote = "N/A";
 	String stockChangePercentage = "N/A";
 	
 	//class variable
 	BigDecimal bankAccountBigDecimal;
 	
 	/*
 	 * This sub class is to asynchronously refresh the simulation data
 	 * For some reason< i need to use Integer instead of int (investigating)
 	 */
 	private class refreshStocksAsync extends AsyncTask <Void, Void, Integer>{
 
 		protected Integer doInBackground(Void... arg0) {
 			
 			Cursor cur = sDbHelper.fetchAllStocks();
 			String stockToUpdate = "";
 			String updatedRawData = "";
 			cur.moveToFirst();
 			
 			//update every single stock in my table
 			while (cur.isAfterLast() == false){
 				stockToUpdate = cur.getString(1);
 				updatedRawData = getStockInfo(stockToUpdate);
 				
 				//require a debug message here
 				Log.i(TAG, updatedRawData);
 				
 				//check if internet cuts off and getting no data
 				if (updatedRawData.length() == 0)
 				{
 					globalToast.cancel();
 					globalToast.setText("There is no Internet, can't get data!");
 					globalToast.show();
 					return -1;
 				}
 				
 				String[] tokens = updatedRawData.split(",");
 				//since I know stock quote is the 2nd token
 				sDbHelper.updateStock(cur.getInt(0), stockToUpdate, tokens[1], cur.getString(3), cur.getString(4));
 				cur.moveToNext();
 			}
 			
 			
 			return 0;
 		}
 
 		protected void onPostExecute(Integer result) {
 			
 			fillData();
 			refreshSimulation.setText("Refresh");
 			refreshSimulation.setEnabled(true);
 			//set the time for when it was updated
 			if (result == 0)
 				updateOut.setText("Updated at: " + dateFormat.format(new Date()) );
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
 			
 			if (stockTxt.length() == 0)
 			{
 				globalToast.cancel();
 				globalToast.setText("There is no Internet, can't get data!");
 				globalToast.show();
 				getQuote.setText("Get Stock Quote");
 				getQuote.setEnabled(true);
 				return;
 			}
 			
 			String[] tokens = stockTxt.split(",");
 
 			stockSymbol = tokens[0];
 			stockQuote = tokens[1];
 			stockChangePercentage = tokens[2];
 			stockCompanyName = tokens[3];
 
 			// parse the individual tokens, taking out "" and .to for stock symbol
 			stockSymbol = stockSymbol.substring(1,stockSymbol.length() - 4);
 			stockChangePercentage = stockChangePercentage.substring(1,stockChangePercentage.length() - 3);
 			stockCompanyName = stockCompanyName.substring(1, stockCompanyName.length() - 1);
 			
 			// checking if a correct stock symbol was entering
 			// looking to see if stock price is 0.00, which is not possible
 			if ( stockQuote.compareTo(NOTVALIDSTOCKPRICE) == 0){
 				
 				globalToast.cancel();
 				globalToast.setText("A invalid stock symbol was entered");
 				globalToast.show();
 				
 				stockQuote = "Stock Quote: N/A";
 				stockChangePercentage = "Percent Change: N/A";
 				stockCompanyName = "Company Name: N/A";
 				
 				companyNameOut.setText (stockCompanyName);
				symbolOut.setText(stockSymbol + " is not a valid stock symbol in TSX");
 				priceOut.setText(stockQuote);
 				changePercentageOut.setText(stockChangePercentage);
 				
 			}
 			//correct stock quote was entered
 			else {
 				
 				companyNameOut.setText ("Company Name: " + stockCompanyName);
 				symbolOut.setText("Stock Symbol: " + stockSymbol);
 				priceOut.setText("Stock Quote: " + stockQuote);
 				changePercentageOut.setText("Percent Change: " + stockChangePercentage + "%");
 				
 			}
 				
 			// getting positive or negative sign
 			char c = stockChangePercentage.charAt(0);
 
 			if (c == '-')
 				mpBad.start();
 			else
 				mpGood.start();
 
 			// Keeping ticker value
 			lastTicker = setSymbol.getText().toString() ;
 			
 			// Show save to portfolio button
 			if (existInDB(lastTicker)) {
 				saveToPortfolio.setText("Already in portfolio") ;
 				saveToPortfolio.setClickable(false) ;
 			} else {
 				saveToPortfolio.setText("Save to Portfolio") ;
 				saveToPortfolio.setClickable(true) ;
 			}
 			saveToPortfolio.setVisibility(View.VISIBLE) ;
 			
 			// erases edit text view after getting quote
 			setSymbol.setText("");
 			
 			//allow the user to get other stocks again
 			getQuote.setText("Get Stock Quote");
 			getQuote.setEnabled(true);
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// Intilize database connection variable
 		db = (new SQLiteExpandedHelper(this)).getWritableDatabase();
 		
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
 		
 		companyNameOut = (TextView) findViewById(R.id.companyNameOutput);
 		symbolOut = (TextView) findViewById(R.id.stockSymbolOutput);
 		priceOut = (TextView) findViewById(R.id.stockPriceOutput);
 		changePercentageOut = (TextView) findViewById(R.id.stockChangePercentageOutput);
 		
 		bankAccountOut = (TextView) findViewById(R.id.bankAccountOutput);
 		updateOut = (TextView) findViewById (R.id.updateOutput);
 
 		getQuote = (Button) findViewById(R.id.get_quote_button);
 		saveToPortfolio = (Button) findViewById(R.id.save_to_portfolio_button) ;
 		
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
 		String bankAccountString = userAccount.getString("bankAccount", "100000.00");
 		bankAccountBigDecimal = new BigDecimal(bankAccountString);
 		
 		bankAccountOut.setText(currencyFormat(bankAccountBigDecimal));
 		
 		//Setting up Tabs
 		final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
 		tabHost.setup();
 		
 		//Setting up Get Quote Module
 		TabHost.TabSpec stockQuoteScreen = tabHost.newTabSpec("StockQuoteTab");
 		stockQuoteScreen.setContent(R.id.stockQuote);
 		stockQuoteScreen.setIndicator("Stock Quote", getResources().getDrawable(R.drawable.stockquote_grey));
 		tabHost.addTab(stockQuoteScreen);
 		
 		//Setting up Portfolio
 		TabHost.TabSpec portfolioScreen = tabHost.newTabSpec("Portfolio");
 		portfolioScreen.setContent(R.id.portfolio);
 		portfolioScreen.setIndicator("Portfolio", getResources().getDrawable(R.drawable.stockquote_grey));
 		tabHost.addTab(portfolioScreen);
 		
 		//Setting up Simulation Module
 		TabHost.TabSpec simulationScreen = tabHost.newTabSpec("simulationTab");
 		simulationScreen.setContent(R.id.simulation);
 		simulationScreen.setIndicator("Simulation", getResources().getDrawable(R.drawable.stockquote_grey));
 		tabHost.addTab(simulationScreen);
 		
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
 				
 				hideKeyboard();
 				
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
 				hideKeyboard();
 				
 				String noOfStocksString = setNoOfStocks.getText().toString();
 				
 				//check that a number is indeed entered
 				if (noOfStocksString.length() == 0) {
 					globalToast.cancel();
 					globalToast.setText("A number is required to continue");
 					globalToast.show();
 				}	
 				else{
					createStock(noOfStocksString);
					tabHost.setCurrentTab(2);
 				}
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
 		
 		
 		// ------------------------------------------- PORTFOLIO STUFF -----------------------------------------------//
 		saveToPortfolio.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Report if no ticker inserted
 				if (lastTicker == null || lastTicker == "") {
 					Toast dbError = Toast.makeText(Main.this,
 							"There was no stock ticker to add.",
 							Toast.LENGTH_LONG);
 					dbError.show();
 					return ;
 				// Report if already exist
 				} else if (existInDB(lastTicker)) {
 					Toast dbError = Toast.makeText(Main.this,
 							"This company is already in your portfolio!",
 							Toast.LENGTH_LONG);
 					dbError.show();
 					return ;
 				} else {
 					// Report if error
 					if (!insertInDB(lastTicker)) {
 						Toast dbError = Toast.makeText(Main.this,
 								"I can't seem to add this to the portfolio, please restart the application and try again.",
 								Toast.LENGTH_LONG);
 						dbError.show();
 						return ;
 					}
 					// User feedback
 					saveToPortfolio.setText("Done") ;
 					saveToPortfolio.setClickable(false) ;
 				}
 			}
 		}) ;
 
 		// Get portfolio variables
 		portfolio = (ListView) findViewById(R.id.pfList);
 		
 		// Prep the portfolio lists
 		dbCursor = db.rawQuery("SELECT * FROM portfolio WHERE 1=1", null);
 		adapter = new SimpleCursorAdapter(this,	R.layout.portfolio_item, dbCursor, new String[] {"Ticker"},	new int[] {R.id.pfliTicker});
 		portfolio.setAdapter(adapter);
 		// ------------------------------------------- END PORTFOLIO STUFF -----------------------------------------------//
 	}
 	
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0, DELETE_ID, 0, R.string.menu_delete);
     }
 
     /*
      * Option to delete/sell ALL stock from simulation mode
      */
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case DELETE_ID:
                 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                 
                 //before deleting stock, it must first be "sold"
                 //operations to change the user's bankAccount information
                 Cursor cur = sDbHelper.fetchStock(info.id);
     			BigDecimal stockQuoteBigDecimal = new BigDecimal (cur.getString(3));
     			BigDecimal noOfStocksBigDecimal = new BigDecimal (cur.getString(4));
     			
     			stockQuoteBigDecimal = stockQuoteBigDecimal.multiply(noOfStocksBigDecimal);
     			bankAccountBigDecimal = bankAccountBigDecimal.add(stockQuoteBigDecimal);
     			
     			//forgot to set our "global/stored" bank account string, bug fix
     			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
     			SharedPreferences.Editor editor = userAccount.edit();
     			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
     			bankAccountOut.setText(currencyFormat(bankAccountBigDecimal));
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
 			aboutText.setText("Written by Fangbo and Jeff.\n\nShoutout to StackOverflow and Frank\n");
 			
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
 		String stockTxt = "";
 		try {
 				// getting info from Yahoo Finance API [meat of the program]
 				url = new URL(
 						"http://download.finance.yahoo.com/d/quotes.csv?s="
 								+ symbolInput + ".to" +"&f=sl1p2n");
 				
 				//!!!!!!added .to TSX stocks only!!!!!!!!!!!//
 				
 				/*
 				 * s = stock symbol
 				 * l1 = last trade (price only)
 				 * p2 = change in percent
 				 * n = name of company
 				 */
 
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
 			SharedPreferences userAccount = getSharedPreferences(PREFS_NAME, 0);
 			SharedPreferences.Editor editor = userAccount.edit();
 			editor.putString ("bankAccount", bankAccountBigDecimal.toString());
 			bankAccountOut.setText(currencyFormat(bankAccountBigDecimal));
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
 	
 	
 	/*
 	 * Changes the format of BigDecimal to a 2 decimal String form (for outputting in text box)
 	 */
 	public String currencyFormat(BigDecimal n) {
 	    return NumberFormat.getCurrencyInstance(Locale.CANADA).format(n);
 	}
 
 	/*
 	 * hides the keyboard (used after entering something in the editbox)
 	 */
 	public void hideKeyboard () {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(setSymbol.getWindowToken(), 0);
 	}
 	
 	// PORTFOLIO FUNCTIONS
 	// Checks if the given ticker already exists in the database (true for yes false for no)
 	protected boolean existInDB(String ticker){
 		if (ticker != null && ticker != "") {
 			dbCursor = db.rawQuery("SELECT _id FROM portfolio WHERE Ticker = \"" + ticker + "\"", null) ;
 			return (dbCursor.getCount()>0) ;
 		} else {
 			Toast dbError = Toast.makeText(Main.this,
 					"There was no stock ticker to search.",
 					Toast.LENGTH_LONG);
 			dbError.show();
 			return false ;
 		}
 	}
 	
 	// Adds a ticker to the portfolio db (true for okay false for error)
 	protected boolean insertInDB(String ticker) {
 		if (ticker != null && ticker != "") {
 			ContentValues values = new ContentValues();
 	        values.put("Ticker", ticker);
 			if (-1 != db.insert("portfolio", "_id", values)) {
 				// update the portfolio page upon success
 				dbCursor = db.rawQuery("SELECT * FROM portfolio WHERE 1=1", null);
 				adapter = new SimpleCursorAdapter(this,	R.layout.portfolio_item, dbCursor, new String[] {"Ticker"},	new int[] {R.id.pfliTicker});
 				portfolio.setAdapter(adapter);
 				return true ;
 			} else {
 				return false ;
 			}
 		} else {
 			Toast dbError = Toast.makeText(Main.this,
 					"There was no stock ticker to add.",
 					Toast.LENGTH_LONG);
 			dbError.show();
 			return false ;
 		}
 	}
 	
 	// Delete button handler for portfolio view
 	public void portfolioDelete(View v) {
 		// get row layout object
 		LinearLayout row = (LinearLayout)v.getParent();
 		// get label from layout
         TextView ticker = (TextView)row.getChildAt(0);
         // da query
         if (db.delete("portfolio", "Ticker = \"" + ticker.getText().toString() + "\"", null) > 0) {
 			Toast dbInform = Toast.makeText(Main.this,
 					ticker.getText().toString() + " removed.",
 					Toast.LENGTH_LONG);
 			dbInform.show();
         } else {
 			Toast dbError = Toast.makeText(Main.this,
 					"Unable to delete " + ticker.getText().toString() + ".",
 					Toast.LENGTH_LONG);
 			dbError.show();
         }
         // reset layout so it actually shows
 		dbCursor = db.rawQuery("SELECT * FROM portfolio WHERE 1=1", null);
 		adapter = new SimpleCursorAdapter(this,	R.layout.portfolio_item, dbCursor, new String[] {"Ticker"},	new int[] {R.id.pfliTicker});
 		portfolio.setAdapter(adapter);
 	}
 	
 	
 }
