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
     public String [] titoli = null;
     public String [] descrizioni=null;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);	
         new connection().execute();
         ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,titoli);
         ListView listView= (ListView)findViewById(R.id.list);
          listView.setAdapter(adapter);
 
 	}
     
     
 
 	public class connection extends AsyncTask<Void, Void, Void> {
 		public Void doInBackground(Void... params) {
 	   // All static variables
    final String URL = "http://www.lookedpath.tk/apps/firstapp/version.xml";
     // XML node keys
     final String ITEM = "item"; // parent node
     final String TITLE = "title";
     final String DESC = "description";
     Element e=null;
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
  
 		for (int c = 0; c < nl.getLength(); c++) {
 			e = (Element) nl.item(c);
 			titoli[c] = parser.getValue(e, TITLE); // name child value
 			descrizioni[c]=parser.getValue(e, DESC);
 		}
 		return null;
 		}
 
         // selecting single ListView item
 		/*       ListView lv = getListView();
         lv.setOnItemClickListener(new OnItemClickListener() {
  
            public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 // getting values from selected ListItem
                 String name = ((TextView) view.findViewById(R.id.label)).getText().toString();
 
  
                 // Starting new intent
                 Intent in = new Intent(getApplicationContext(), SingleListItem.class);
                 in.putExtra(DESC, description);
                 startActivity(in);
  
             }
             
         }); */
     }
 	
 }
 
