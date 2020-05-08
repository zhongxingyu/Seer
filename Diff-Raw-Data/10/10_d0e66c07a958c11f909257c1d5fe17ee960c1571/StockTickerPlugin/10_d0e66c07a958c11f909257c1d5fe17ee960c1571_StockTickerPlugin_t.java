 package com.betaminus.canvasstockticker;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.betaminus.phonepowersource.R;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.pennas.pebblecanvas.plugin.PebbleCanvasPlugin;
 
 public class StockTickerPlugin extends PebbleCanvasPlugin {
 	public static final String LOG_TAG = "CANV_TICKER";
 
 	public static final int TICKER_TEXT_ID = 1; // Needs to be unique only
 												// within
 	// this plugin package
 	public static final int TICKER_DAYCHART_ID = 2;
 
 	private static final String[] MASKS = { "%T", "%S", "%P", "%C" };
 	private static final int MASK_TIME = 0;
 	private static final int MASK_TICKER = 1;
 	private static final int MASK_PRICE = 2;
 	private static final int MASK_PCTCHANGE = 3;
 
 	private static class TickInfo {
 		String time;
 		String pctchange;
 		double price;
 	}
 
 	private static class ChartInfo {
 		String time;
 		StockChart sc;
 
 		public ChartInfo(Context context) {
 			sc = new StockChart(context);
 		}
 	}
 
 	// private static StockInfo current_state = new StockInfo();
 
 	static Map<String, TickInfo> tick_list = new HashMap<String, TickInfo>();
 	static Map<String, ChartInfo> chart_list = new HashMap<String, ChartInfo>();
 
 	// send plugin metadata to Canvas when requested
 	@Override
 	protected ArrayList<PluginDefinition> get_plugin_definitions(Context context) {
 		Log.i(LOG_TAG, "get_plugin_definitions");
 
 		// create a list of plugins provided by this app
 		ArrayList<PluginDefinition> plugins = new ArrayList<PluginDefinition>();
 
 		// now playing (text)
 		TextPluginDefinition tplug = new TextPluginDefinition();
 		tplug.id = TICKER_TEXT_ID;
 		tplug.name = context.getString(R.string.plugin_name);
 		tplug.format_mask_descriptions = new ArrayList<String>(
 				Arrays.asList(context.getResources().getStringArray(
 						R.array.format_mask_descs)));
 		// populate example content for each field (optional) to be display in
 		// the format mask editor
 		ArrayList<String> examples = new ArrayList<String>();
 		examples.add("10:35");
 		examples.add("MSFT");
 		examples.add("30.52");
 		examples.add("+1.53%");
 		tplug.params_description = "Ticker symbol";
 		tplug.format_mask_examples = examples;
 		tplug.format_masks = new ArrayList<String>(Arrays.asList(MASKS));
 		tplug.default_format_string = "%S: %P";
 		plugins.add(tplug);
 
 		// chart
 		ImagePluginDefinition iplug = new ImagePluginDefinition();
 		iplug.id = TICKER_DAYCHART_ID;
 		iplug.name = context.getString(R.string.plugin_name) + " 120x120";
 		iplug.params_description = "Ticker symbol";
 		plugins.add(iplug);
 
 		return plugins;
 	}
 
 	// send current text values to canvas when requested
 	@Override
 	protected String get_format_mask_value(int def_id, String format_mask,
 			Context context, String param) {
 
 		// Log.i(LOG_TAG, "get_format_mask_value def_id = " + def_id
 		// + " format_mask = '" + format_mask + "'");
 
 		param = param.toUpperCase();
 		if (def_id == TICKER_TEXT_ID) {
 			Log.i(LOG_TAG, "get_format_mask_value: Ticker request for " + param);
 			startService(context);
 
 			if (!tick_list.containsKey(param)) {
 				Log.i(LOG_TAG,
 						"get_format_mask_value: Tick data doesn't exist for "
 								+ param + " - creating");
 				tick_list.put(param, new TickInfo());
 				updateSingleTicker(context, param, true); // go ahead and
 															// retrieve
 															// immediately
 				return "...";
 			}
 			Log.i(LOG_TAG, "get_format_mask_value: Have ticker already for "
 					+ param + " so returning it");
 			TickInfo current_state = tick_list.get(param);
 
 			// which field to return current value for?
 			if (format_mask.equals(MASKS[MASK_TIME])) {
 				return current_state.time;
 			} else if (format_mask.equals(MASKS[MASK_TICKER])) {
 				return param;
 			} else if (format_mask.equals(MASKS[MASK_PRICE])) {
 				return String.valueOf(current_state.price);
 			} else if (format_mask.equals(MASKS[MASK_PCTCHANGE])) {
 				return current_state.pctchange;
 			}
 		}
 		Log.i(LOG_TAG, "get_format_mask_value: no matching mask found");
 		return null;
 	}
 
 	private void startService(Context context) {
 		// Service will only get started once, so no great problem
 		// re-calling this
 		Intent tickerService = new Intent(context, StockTickerService.class);
 		context.startService(tickerService);
 	}
 
 	// send bitmap value to canvas when requested
 	@Override
 	protected Bitmap get_bitmap_value(int def_id, Context context, String param) {
 		param = param.toUpperCase();
 		if (def_id == TICKER_DAYCHART_ID) {
 			Log.i(LOG_TAG, "get_bitmap_value: Day chart request for " + param);
 			startService(context);
 			// Data not retrieved for this yet - just return the icon for the
 			// moment
 			if (!chart_list.containsKey(param)) {
 				Log.i(LOG_TAG,
 						"get_bitmap_value: Day chart data doesn't exist for "
 								+ param + " - creating and requesting");
 				ChartInfo makeStock = new ChartInfo(context);
 				chart_list.put(param, makeStock);
 				updateSingleChart(context, param, true); // go ahead and
 															// retrieve
 															// immediately
 				return BitmapFactory.decodeResource(context.getResources(),
 						R.drawable.hourglass919);
 			}
 			ChartInfo current_state = chart_list.get(param);
 
 			if (current_state.sc != null) { // we have already drawn chart
 				Log.i(LOG_TAG, "get_bitmap_value: Have chart for " + param
 						+ " already so returning it");
 				Bitmap bm = current_state.sc.getBitmap();
 				return bm;
 			} else {
 				Log.i(LOG_TAG, "get_bitmap_value: Chart oddly missing for "
 						+ param + " so returning hourglass");
 				return BitmapFactory.decodeResource(context.getResources(),
 						R.drawable.hourglass919);
 			}
 		}
 		Log.i(LOG_TAG, "get_bitmap_value: no matching image mask found");
 		return BitmapFactory.decodeResource(context.getResources(),
 				R.drawable.canvas_icon);
 	}
 
 	private static void updateSingleTicker(final Context context,
 			String ticker, final Boolean sendScreenUpdate) {
 		AsyncHttpClient client = new AsyncHttpClient();
 
 		client.get("http://finance.yahoo.com/d/quotes.csv?s=" + ticker
				+ "&f=sl1p2", new AsyncHttpResponseHandler() {
 			@Override
 			public void onSuccess(String response) {
 				Log.i(LOG_TAG, "updateSingleTicker: Got valid tick response: "
 						+ response);
 				List<String> list = new ArrayList<String>(Arrays
 						.asList(response.split(",")));
 
 				String ticker = list.get(0).replace("\"", "");
 				TickInfo toUpdate = tick_list.get(ticker);
 
 				toUpdate.time = new SimpleDateFormat("H:mm").format(Calendar
 						.getInstance().getTime());
 
				try {
					toUpdate.price = Double.parseDouble(list.get(1));
				} catch (NumberFormatException e) {
					// Sometimes this returns N/A. Just don't do the update
					return;
				}
 				toUpdate.pctchange = list.get(2).replace("\"", "")
 						.replace("\n", "");
 				if (sendScreenUpdate) {
 					Log.i(LOG_TAG,
 							"updateSingleTicker: Requesting screen text refresh");
 					notify_canvas_updates_available(TICKER_TEXT_ID, context);
 				}
 			}
 		});
 	}
 
 	private static void updateSingleChart(final Context context,
 			final String ticker, final Boolean sendScreenUpdate) {
 		AsyncHttpClient client = new AsyncHttpClient();
 
 		client.get("http://chartapi.finance.yahoo.com/instrument/1.0/" + ticker
 				+ "/chartdata;type=quote;range=1d/csv",
 				new AsyncHttpResponseHandler() {
 					@Override
 					public void onSuccess(String response) {
 						Log.i(LOG_TAG,
 								"updateSingleChart: Got valid day chart response for ticker "
 										+ ticker);
 						// Strip out all the data at the top, leaving just
 						// the tick data. The ?s thing
 						// does it multi-line
 						// String ticker = response.replaceAll(
 						// "(?s).*ticker:(\\w{4}).*", "$1");
 						// ticker = ticker.toUpperCase();
 						ChartInfo toUpdate = chart_list.get(ticker);
 
 						// Remove the header stuff now that we've got the
 						// ticker out
 						response = response.replaceFirst(
 								"(?s).*volume:[0-9\\,]*", "");
 						// We now have the data on a sequence of lines -
 						// need to dump all but the prices
 						List<String> eachline = new ArrayList<String>(Arrays
 								.asList(response.split("\n")));
 						Log.i(LOG_TAG,
 								"updateSingleChart: Adding "
 										+ String.valueOf(eachline.size())
 										+ " values to chart for " + ticker);
 						for (int i = 0; i < eachline.size(); i++) {
 							List<String> eachvar = new ArrayList<String>(Arrays
 									.asList(eachline.get(i).split(",")));
 							if (eachvar.size() > 1) { // there are some null
 														// strings kicking
 														// around
 								Double stockVal = Double.parseDouble(eachvar
 										.get(1));
 								toUpdate.sc.mCurrentSeries.add(i, stockVal);
 								// Log.i(LOG_TAG, eachvar.get(1));
 							}
 						}
 						toUpdate.sc.mCurrentSeries.setTitle(ticker); // for
 						toUpdate.sc.mRenderer.setXTitle(ticker + " over day");
 
 						if (sendScreenUpdate) {
 							Log.i(LOG_TAG,
 									"updateSingleChart: Requesting screen chart refresh");
 							notify_canvas_updates_available(TICKER_DAYCHART_ID,
 									context);
 						}
 					}
 				});
 	}
 
 	public static void updateTicker(Context context) {
 
 		for (Entry<String, TickInfo> entry : tick_list.entrySet()) {
 			Log.i(LOG_TAG,
 					"updateTicker: Updating ticker for " + entry.getKey());
 			updateSingleTicker(context, entry.getKey(), false);
 		}
 		Log.i(LOG_TAG, "updateTicker: Requesting screen text refresh");
 		notify_canvas_updates_available(TICKER_TEXT_ID, context);
 
 		for (Entry<String, ChartInfo> entry : chart_list.entrySet()) {
 			Log.i(LOG_TAG,
 					"updateTicker: Updating day chart for " + entry.getKey());
 			updateSingleChart(context, entry.getKey(), false);
 		}
 		Log.i(LOG_TAG, "updateTicker: Requesting screen chart refresh");
 		notify_canvas_updates_available(TICKER_DAYCHART_ID, context);
 	}
 }
