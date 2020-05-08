 package com.messedagliavr.messeapp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.ListActivity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class news extends ListActivity {
 	public String[] titoli = new String[99];
 	public String[] descrizioni = new String[99];
 	public int a = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		new connection().execute();
 		while (a == 0) {
 		}
 	}
 
 	public class connection extends AsyncTask<Void, Void, Void> {
 		public Void doInBackground(Void... params) {
 			System.out.println("prova2");
 			// All static variables
 			final String URL = "http://www.messedaglia.it/index.php/archivio-news?format=feed&type=rss";
 			// XML node keys
 			final String ITEM = "item"; // parent node
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e = null;
 			ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
 
 			XMLParser parser = new XMLParser();
 			String xml = parser.getXmlFromUrl(URL); // getting XML
 			Document doc = parser.getDomElement(xml); // getting DOM element
 			NodeList nl = doc.getElementsByTagName(ITEM);
 
 			// looping through all item nodes <item>
 			for (int i = 0; i < nl.getLength(); i++) {
 				// creating new HashMap
 				HashMap<String, String> map = new HashMap<String, String>();
 				e = (Element) nl.item(i);
 				// adding each child node to HashMap key => value
 				map.put(TITLE, parser.getValue(e, TITLE));
 				map.put(DESC, parser.getValue(e, DESC));
 
 				// adding HashList to ArrayList
 				menuItems.add(map);
 			}
 			System.out.println("prova3");
 			for (int c = 0; c < nl.getLength(); c++) {
 				e = (Element) nl.item(c);
 				titoli[c] = parser.getValue(e, TITLE); // name child value
 				descrizioni[c] = parser.getValue(e, DESC);
 			}
 			a++;
 			return null;
 		}
 
 		public void onPostExecute() {
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(news.this,
 					android.R.layout.simple_list_item_1, titoli);
 			ListView listView = (ListView) news.this
 					.findViewById(android.R.id.list);
 			listView.setAdapter(adapter);
			System.out.println("prova4");
			a++;
 		}
 
 		// selecting single ListView item
 		/*
 		 * ListView lv = getListView(); lv.setOnItemClickListener(new
 		 * OnItemClickListener() {
 		 * 
 		 * public void onItemClick(AdapterView<?> parent, View view, int
 		 * position, long id) { // getting values from selected ListItem String
 		 * name = ((TextView)
 		 * view.findViewById(R.id.label)).getText().toString();
 		 * 
 		 * 
 		 * // Starting new intent Intent in = new
 		 * Intent(getApplicationContext(), SingleListItem.class);
 		 * in.putExtra(DESC, description); startActivity(in);
 		 * 
 		 * }
 		 * 
 		 * });
 		 */
 	}
 
 }
