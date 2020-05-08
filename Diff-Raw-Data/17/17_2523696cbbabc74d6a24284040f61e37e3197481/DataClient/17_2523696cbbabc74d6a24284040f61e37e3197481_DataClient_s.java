 package com.openfridge;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.SystemClock;
 import android.widget.Toast;
 
 /**
  * A class which facilitates communication with the server, allowing the
  * retrieving and updating of fridge food items on the database server.
  * 
  * @author Tom, Shimona, Jesse
  */
 
 public class DataClient extends Observable {
 	private static final int RED = Color.parseColor("#ED1C24");
 	private static final int YELLOW = Color.parseColor("#FFD300");
 	private static final int GREEN = Color.parseColor("#228B22");
 	// ArrayLists for the data from the XML
 	private Map<ExpState, List<FridgeFood>> foods = new HashMap<ExpState, List<FridgeFood>>();
 	private Map<Integer, FridgeFood> foodsById = new HashMap<Integer, FridgeFood>();
 	private List<ShoppingItem> shoppingList = new ArrayList<ShoppingItem>();
 	// Parsing stuff
 	private XMLReader xr;
 	private SAXParser sp;
 	private SAXParserFactory spf;
 	private URL fridgeFoodURL, shoppingItemURL;
 	// Update notification stuff
 	private GetDataAsyncTask getDataTask;
 	private long lastRunTime = 0;
 	private long nextRunTime = 0;
 	{
 		for (ExpState key : ExpState.values()) {
 			foods.put(key, new ArrayList<FridgeFood>());
 		}
 		/* Create the URLs we want to load xml-data from. */
 		/*
 		 * If you check them, you'll see they are mini-xml documents generated
 		 * from elvin's database.
 		 */
 		try {
 			fridgeFoodURL = new URL(
 					"http://openfridge.heroku.com/users/"+this.getUID()+"/fridge_foods.xml");
 			shoppingItemURL = new URL(
 					"http://openfridge.heroku.com/users/"+this.getUID()+"/shopping_lists.xml");
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		}
 
 		/* Get a SAXParser from the SAXPArserFactory. */
 		spf = SAXParserFactory.newInstance();
 		try {
 			/* Get the XMLReader of the SAXParser we created. */
 			sp = spf.newSAXParser();
 			xr = sp.getXMLReader();
 		} catch (ParserConfigurationException e) {
 		} catch (SAXException e) {
 		}
 	}
 
 	private DataClient() {
 	}
 
 	private class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			reloadFridgeFoods();
 			reloadShoppingItems();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			getDataTask = null;
 			clientNotifyObservers();
 		}
 	}
 
 	private void clientNotifyObservers() {
 		setChanged();
 		notifyObservers();
 	}
 
 	public void reloadFoods() {
 		long now = SystemClock.uptimeMillis();
 		if (getDataTask == null && (now > nextRunTime || now < lastRunTime)) {
 			// 2nd check is in case the clock's been reset.
 			lastRunTime = SystemClock.uptimeMillis();
 			nextRunTime = lastRunTime + 1000 * 60 * 5; // 5 minutes
 			getDataTask = new GetDataAsyncTask();
 			getDataTask.execute();
 		}
 	}
 
 	// FridgeFood routes
 	// -----------------
 
 	public boolean doNetOp(Context c, NetOp op, DataObject to) {
 		try {
 			op.doIt(to);
 			clientNotifyObservers();
 			return true;
 		} catch (IOException e) {
 			Toast.makeText(c, "Communication Error",
 					Toast.LENGTH_SHORT).show();
 			e.printStackTrace();
 			return false;
 		}
 
 	}
 
 	private void reloadFridgeFoods() {
 		FridgeFoodHandler ffH = new FridgeFoodHandler();
 		parse(ffH, fridgeFoodURL);
 
 		// Changes the contents of the ArrayList's,
 		// rather than re-assigning them.
 		for (ExpState key : ExpState.values()) {
 			foods.get(key).clear();
 			foods.get(key).addAll(ffH.getFoods(key));
 			for (FridgeFood f : foods.get(key)) {
 				foodsById.put(f.getId(), f);
 			}
 		}
 	}
 
 	public FridgeFood getFFById(int id) {
 		return foodsById.get(id);
 	}
 	public void addFF(ExpState key, FridgeFood f) {
 		foods.get(key).add(f);
 		foodsById.put(f.getId(), f);
 	}
 	public void removeFF(FridgeFood f) {
 		for (List<FridgeFood> l : foods.values()) {
 			l.remove(f);
 		}
 		foodsById.remove(f.getId());
 	}
 	private void reloadShoppingItems() {
 		ShoppingItemHandler siH = new ShoppingItemHandler();
 		parse(siH, shoppingItemURL);
 
 		shoppingList.clear();
 		shoppingList.addAll(siH.getFoods());
 	}
 
 	// Field getters
 	// -------------
 
 	public List<ShoppingItem> getShoppingList() {
 		return shoppingList;
 	}
 
 	public List<FridgeFood> getFoods(ExpState key) {
 		return foods.get(key);
 	}
 
 	public int getExpirationListColor() {
 		if (!getFoods(ExpState.EXPIRED).isEmpty()) {
 			// There are expired items:
 			return RED;
 		} else if (!getFoods(ExpState.NEAR).isEmpty()) {
 			// There are stale items:
 			return YELLOW;
 		}
 		return GREEN;
 	}
 
 	public int getShoppingListColor() {
 		if (!getShoppingList().isEmpty()) {
 			return GREEN;
 		}
 		return Color.parseColor("#FFFFFF");
 	}
 
 	// Singleton & utility stuff
 	// -------------------------
 	public static DataClient getInstance() {
 		return DataClientHolder.client;
 	}
 
 	private static class DataClientHolder /* Pugh's Way */{
 		public static final DataClient client = new DataClient();
 	}
 
 	public int getUID() {
		return 3;
 	}
 
 	private void parse(SAXHandler<?> h, URL url) {
 		xr.setContentHandler(h);
 		try {
 			xr.parse(new InputSource(url.openStream()));
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
