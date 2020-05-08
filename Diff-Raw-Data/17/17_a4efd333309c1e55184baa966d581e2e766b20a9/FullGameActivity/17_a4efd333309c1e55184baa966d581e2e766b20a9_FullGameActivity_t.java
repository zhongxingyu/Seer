 package com.jgrue.vgpc;
 
 import java.text.DecimalFormat;
import java.util.ArrayList;
 import java.util.List;
 
 import com.jgrue.vgpc.data.FullGame;
 import com.jgrue.vgpc.data.Store;
 import com.jgrue.vgpc.scrapers.GameScraper;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TableRow.LayoutParams;
 
 public class FullGameActivity extends Activity implements OnClickListener {
 	private static final String TAG = "FullGameActivity";
 	private static final DecimalFormat moneyFormat = new DecimalFormat("$0.00");
 	private FullGame fullGame;
 	private List<Store> listStore;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.fullgame);
 		
 		String upc = getIntent().getStringExtra("GAME_UPC");
 		if(upc != null && !upc.equals("")) {
 			new FullGameTask().execute(upc);
 		} else {
 			((TextView)findViewById(R.id.fullgame_name)).setText(getIntent().getStringExtra("GAME_NAME"));
 			((TextView)findViewById(R.id.console_name)).setText(getIntent().getStringExtra("CONSOLE_NAME"));
 			if(getIntent().getFloatExtra("USED_PRICE", 0.0f) > 0.0f)
 				((TextView)findViewById(R.id.used_price_text)).setText(
 						moneyFormat.format(getIntent().getFloatExtra("USED_PRICE", 0.0f)));
 			else
 				((TextView)findViewById(R.id.used_price_text)).setText("N/A");
 			
 			new FullGameTask().execute(getIntent().getStringExtra("GAME_ALIAS"), 
 					getIntent().getStringExtra("CONSOLE_ALIAS"));
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.view_graph_text && fullGame != null) {
 			Intent myIntent = new Intent(v.getContext(), PriceChartActivity.class);
 			myIntent.putExtra("GAME_NAME", fullGame.getGameName());
 			myIntent.putExtra("GAME_ALIAS", fullGame.getGameAlias());
 			myIntent.putExtra("CONSOLE_ALIAS", fullGame.getConsoleAlias());
 			startActivityForResult(myIntent, 0);
 		} else if(listStore != null && v.getId() < listStore.size()) {
 			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(listStore.get(v.getId()).getStoreLink())));
 		}
 	}
 	
 	private class FullGameTask extends AsyncTask<String, Void, FullGame> {
 		@Override
 		protected FullGame doInBackground(String... intents) {
 			if(intents.length == 1)
 				return GameScraper.getFullGame(intents[0]);
 			else if(intents.length == 2)
 				return GameScraper.getFullGame(intents[0], intents[1]);
 				
 			return new FullGame();
 		}
 		
 		@Override
 		protected void onPostExecute(FullGame game) {
 			// If this was an invalid UPC code, just throw back to the main page.
 			if(game == null || game.getGameName() == null) {
 				AlertDialog alertDialog = new AlertDialog.Builder(FullGameActivity.this).create();
 	        	alertDialog.setMessage("No results found.");
 	        	alertDialog.setButton("OK", new DialogInterface.OnClickListener() { @Override
 					public void onClick(DialogInterface dialog, int which) { finish(); return; } });
 	        	alertDialog.show();
 			}
 			
 			fullGame = game;
 			
 			TextView gameName = (TextView)findViewById(R.id.fullgame_name);
 			gameName.setText(game.getGameName());
 			
 			TextView console = (TextView)findViewById(R.id.console_name);
 			console.setText(game.getConsoleName());
 			
 			TextView usedPrice = (TextView)findViewById(R.id.used_price_text);
 			if(game.getUsedPrice() > 0.0f) {
 				usedPrice.setText(moneyFormat.format(game.getUsedPrice()));
 				findViewById(R.id.view_graph_text).setVisibility(View.VISIBLE);
 				findViewById(R.id.view_graph_text).setOnClickListener(FullGameActivity.this);
 			} else
 				usedPrice.setText("N/A");
 			
 			TextView newPrice = (TextView)findViewById(R.id.new_price_text);
 			if(game.getNewPrice() > 0.0f)
 				newPrice.setText(moneyFormat.format(game.getNewPrice()));
 			else
 				newPrice.setText("N/A");
 			
 			TextView lastObsv = (TextView)findViewById(R.id.last_observation_text);
 			lastObsv.setText(getString(R.string.last_observation_header) + " " + game.getLastObservation());
 			
 			TextView volume = (TextView)findViewById(R.id.volume_text);
 			volume.setText(getString(R.string.volume_header) + " " + game.getVolume());
 			
 			// Once the key information is on screen, kick off everything else.
 			new StoreListTask().execute(game.getGameAlias(), game.getConsoleAlias());
 	    }
 	}
 	
 	private class StoreListTask extends AsyncTask<String, Void, List<Store>> {
 		@Override
 		protected List<Store> doInBackground(String... arg0) {
			try {
				return GameScraper.getStores(arg0[0], arg0[1]);
			} catch (Exception ex) {
				return new ArrayList<Store>();
			}
 		}
 		
 		@Override
 		protected void onPostExecute(List<Store> storeList) {
 			listStore = storeList;
 			TableLayout table = (TableLayout)findViewById(R.id.used_prices_table);
 			
 			for(int i = 0; i < storeList.size(); i++) {
 				TableRow tableRow = new TableRow(FullGameActivity.this);
 				TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
 				rowParams.setMargins(0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()), 0, 0);
 				tableRow.setBackgroundResource(R.drawable.body);
 				tableRow.setWeightSum(10.0f);
 				tableRow.setLayoutParams(rowParams);
     			
     			TextView storeName = new TextView(FullGameActivity.this);
     			LayoutParams storeLayout = new LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 4);
     			storeLayout.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()), 0, 0, 0);
     			storeName.setLayoutParams(storeLayout);
     			storeName.setTextColor(R.drawable.text);
     			storeName.setText(storeList.get(i).getStoreName());
     			storeName.setTypeface(null, Typeface.BOLD);
     			tableRow.addView(storeName);
     			
     			TextView storePrice = new TextView(FullGameActivity.this);
     			storePrice.setLayoutParams(new LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 4));
     			storePrice.setTextColor(R.drawable.text);
     			storePrice.setTypeface(null, Typeface.BOLD);
     			if(storeList.get(i).getStorePrice() > 0.0f)
     				storePrice.setText(moneyFormat.format(storeList.get(i).getStorePrice()));
     			else
     				storePrice.setText("");
     			tableRow.addView(storePrice);
     			
     			ImageView storeLink = new ImageView(FullGameActivity.this);
     			LayoutParams linkLayout = new LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 2);
     			linkLayout.setMargins(0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()), 0);
     			storeLink.setLayoutParams(linkLayout);
     			storeLink.setImageResource(R.drawable.seeit);
     			storeLink.setClickable(true);
     			storeLink.setOnClickListener(FullGameActivity.this);
     			storeLink.setId(i);
     			tableRow.addView(storeLink);
     			
     			table.addView(tableRow, rowParams);
 			}
 		}
 	}
 }
