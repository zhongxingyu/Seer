 package com.messedagliavr.messeapp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class calendar extends ListActivity {
 
 	@Override
 	public void onBackPressed() {
 		Intent main = new Intent(this, MainActivity.class);
 		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(main);
 	}
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public String[] titolim;
 	public String[] descrizionim;
 	ProgressDialog mDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		mDialog = new ProgressDialog(calendar.this);
 		super.onCreate(savedInstanceState);
 		new connection().execute();
 	}
 
 	public class connection extends
 			AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
 		
 		protected void onCancelled() {
 			Intent main = new Intent(calendar.this, MainActivity.class);
 			main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(main);
 			Toast.makeText(calendar.this, R.string.canceledcalendar,
 					Toast.LENGTH_LONG).show();
 		}
 
 		public void onPreExecute() {
 			mDialog = ProgressDialog.show(calendar.this, "Scaricando",
					"Sto scaricando gli eventi", true, true,
 					new DialogInterface.OnCancelListener() {
 						public void onCancel(DialogInterface dialog) {
 							connection.this.cancel(true);
 						}
 					});
 			mDialog.show();
 		}
 
 		public HashMap<String, ArrayList<Spanned>> doInBackground(
 				Void... params) {
 			HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
 			ArrayList<Spanned> titoli = new ArrayList<Spanned>();
 			ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
 			// All static variables
 			final String URL = "http://www.messedaglia.it/index.php?option=com_jevents&task=modlatest.rss&format=feed&type=rss&Itemid=127&modid=162";
 			// XML node keys
 			final String ITEM = "item"; // parent node
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e = null;
 			ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
 			XMLParser parser = new XMLParser();
 			String xml = parser.getXmlFromUrl(URL); // getting XML
 			Document doc = parser.getDomElement(xml); // getting DOM element
 			NodeList nl = doc.getElementsByTagName(ITEM);
 
 			for (int i = 0; i < nl.getLength(); i++) {
 				HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 				e = (Element) nl.item(i);
 				map.put(TITLE, Html.fromHtml(parser.getValue(e, TITLE)));
 				map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
 				// adding HashList to ArrayList
 				menuItems.add(map);
 
 			}
 
 			for (int c = 0; c < nl.getLength(); c++) {
 				e = (Element) nl.item(c);
 
 				titoli.add(Html.fromHtml(parser.getValue(e, TITLE)));
 				descrizioni.add(Html.fromHtml(parser.getValue(e, DESC)));
 			}
 			temhashmap.put("titoli", titoli);
 			temhashmap.put("descrizioni", descrizioni);
 			return temhashmap;
 		}
 
 		public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
 
 			if (resultmap.size() > 0) {
 				final ArrayList<Spanned> titoli = resultmap.get("titoli");
 				final ArrayList<Spanned> descrizioni = resultmap
 						.get("descrizioni");
 				ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
 						calendar.this, android.R.layout.simple_list_item_1, titoli);
 				setContentView(R.layout.list_item);
 				ListView listView = (ListView) calendar.this
 						.findViewById(android.R.id.list);
 				listView.setAdapter(adapter);
 				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					public void onItemClick(AdapterView<?> parentView,
 							View childView, int position, long id) {
 						if (Html.toHtml(descrizioni.get(position))!="") {
 							Intent intent = new Intent(calendar.this,
 									ListItemSelectedCalendar.class);
 							intent.putExtra(TITLE,
 									Html.toHtml(titoli.get(position)));
 							intent.putExtra(DESC,
 									Html.toHtml(descrizioni.get(position)));
 						startActivity(intent);
 						} else {
 							Toast.makeText(calendar.this, R.string.nodescription,
 									Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 			}
 			mDialog.dismiss();
 		}
 	}
 }
