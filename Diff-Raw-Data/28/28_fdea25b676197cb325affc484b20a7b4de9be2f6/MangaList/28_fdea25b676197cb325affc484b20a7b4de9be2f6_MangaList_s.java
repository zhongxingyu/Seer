 package com.laithnurie.baka;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class MangaList extends Activity {
 	
     ArrayList<Manga> searchResults;
     ListView lv;
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_manga_list);
        
         Bundle extras = getIntent().getExtras();
         String feedURL = extras.getString("feedUrl");
        // getFeed(feedURL);
         
         new loadMangaList().execute(feedURL);
         
         lv = (ListView) findViewById(R.id.srListView);
        lv.setAdapter(new MyCustomBaseAdapter(this, searchResults));
 
         lv.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                 Object o = lv.getItemAtPosition(position);
                 Manga fullObject = (Manga)o;
                 Toast.makeText(MangaList.this, "You have chosen: " + " " + fullObject.getManga(), Toast.LENGTH_LONG).show();
             }
 
         });    
     }
     
     
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_manga_list, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     
     
     public class loadMangaList extends AsyncTask<String, Integer, ArrayList<Manga>> {
     	
     	ProgressDialog pd;
     	String passedParam;
         Manga sr ;
 
 
 
     	@Override
     	protected void onPreExecute() {
     		
     		pd = new ProgressDialog(MangaList.this);
     		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
     		pd.setMax(100);
     		pd.show();
     		
     	}
     	
     	@Override
     	protected ArrayList<Manga> doInBackground(final String... params) {
     		searchResults = new ArrayList<Manga>();
                 	    		
     		passedParam = params[0];
     	
 
     	
     		try {
     	
     			URL url = new URL("http://www.mangahere.com/rss/"+passedParam+".xml");
     			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     			DocumentBuilder db = dbf.newDocumentBuilder();
     			Document doc = db.parse(new InputSource(url.openStream()));
     			doc.getDocumentElement().normalize();
     	
     			NodeList nodeList = doc.getElementsByTagName("item");
     	
 
     			
    			for (int i = 0; i < 100; i++) {
     				
     		        sr = new Manga();
 
     					
     				Node node = nodeList.item(i);
     	
 
     	
     				Element fstElmnt = (Element) node;
     				NodeList titleList = fstElmnt.getElementsByTagName("title");
     				Element titleElement = (Element) titleList.item(0);
     				titleList = titleElement.getChildNodes();
     				if (titleList.item(0) !=null) {
    					//title[i].setText("Chapter No = "+ ((Node) titleList.item(0)).getNodeValue());
    					//mangaList.addView(title[i]);
    					
    					//return title[i];
     					sr.setChapter(((Node) titleList.item(0)).getNodeValue());
     	
     				}
     				else {
     					sr.setChapter("There is no Title");
     				}	
     				
     	
     				NodeList linkList = fstElmnt.getElementsByTagName("link");
     				Element linkElement = (Element) linkList.item(0);
     				linkList = linkElement.getChildNodes();
     				if (linkList.item(0) !=null) {
    					//link[i].setText("Link = "+ ((Node) linkList.item(0)).getNodeValue());
    					//mangaList.addView(link[i]);
     					
     					sr.setManga(((Node) linkList.item(0)).getNodeValue());
 
     	
     				}
     				else {
     					sr.setManga("no link available");
     				}
     	
     				
     				NodeList descList = fstElmnt.getElementsByTagName("description");
     				Element descElement = (Element) descList.item(0);
     				descList = descElement.getChildNodes();
     				if (descList.item(0) !=null) {
     					sr.setDesc(((Node) descList.item(0)).getNodeValue());
    					//mangaList.addView(desc[i]);
     				}
     				else {
     					sr.setDesc("No Description available");
     				}
     				
     	    		searchResults.add(sr);
     				publishProgress(i);
    				Thread.sleep(88);
     			}
     		} 
     		
     		catch (Exception e) {
     			Log.v("mangaList", e.getMessage());
     		}
     		return searchResults;
     	}
     	
     	@Override
     	protected void onProgressUpdate(Integer... progress) {
     		pd.incrementProgressBy(progress[0]);	
     	}
     	
     	@Override
         protected void onPostExecute(ArrayList<Manga> result) {
     		pd.dismiss();
    		lv = (ListView) findViewById(R.id.srListView);
    		lv.setAdapter(new MyCustomBaseAdapter(getApplicationContext(), searchResults));
         }
     }
     
 }
