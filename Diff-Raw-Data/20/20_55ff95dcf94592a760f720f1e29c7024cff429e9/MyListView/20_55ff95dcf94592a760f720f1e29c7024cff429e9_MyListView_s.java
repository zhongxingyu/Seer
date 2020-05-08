 package com.podcast.securitynow;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.podcast.securitynow.R;
 
 import sncatalog.shared.MobileEpisode;
 import sncatalog.shared.Serializer;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 
 public class MyListView extends ListActivity 
 {
 
 	ArrayList<MobileEpisode> episodes = null;
 	static final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
  
     @Override  
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);  
         setContentView(R.layout.listview);
         
         try {
 			episodes = this.getEpisodes();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		SimpleAdapter adapter = new SimpleAdapter(
 		this,
 		list,
 		R.layout.list_item,
 		new String[] {"number", "title"},
 		new int[] {R.id.number, R.id.title}
 		);
 		this.makeAdapter(episodes);
 		setListAdapter(adapter);
     }    
  
     @SuppressWarnings("unchecked")
 	private ArrayList<MobileEpisode> getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
     	InputStream ins = getResources().openRawResource(R.raw.episodes);
     	
     	BufferedReader r = new BufferedReader(new InputStreamReader(ins));
     	StringBuilder fileContent = new StringBuilder();
     	String line;
     	while ((line = r.readLine()) != null) {
     	    fileContent.append(line);
     	}
     	
     	return (ArrayList<MobileEpisode>) Serializer.deserialize(fileContent.toString());
     	
   
     	// For remote
     	//EpisodeFetcher ef = new EpisodeFetcher();
     	//return ef.getEpisodes();
 	}
     
     private void makeAdapter(ArrayList<MobileEpisode> me) {
    	for (int i = 0; i > me.size(); i++) {
     		MobileEpisode e = me.get(i);
     		HashMap<String,String> map = new HashMap<String,String>();
     		map.put("index", i+"");
     		map.put("number", e.getLink());
     		map.put("episode", e.getEpisode().toString());
     		map.put("title", e.getTitle());
     		list.add(map);
     	}
     }
 
 	public void onListItemClick(
     ListView parent, View v,
     int position, long id) 
     {   
 		HashMap selection = (HashMap)getListView().getItemAtPosition(position);
         //Toast.makeText(this, 
         //    "You have selected: " + selection.get("episode"), 
         //    Toast.LENGTH_SHORT).show();		
 		viewEpisode(Integer.parseInt((String)selection.get("index")));
     }  
 	
 	private void viewEpisode(int episodeIndex) {
 		MobileEpisode me = episodes.get(episodeIndex);
 		Intent i = new Intent(this, EpisodeActivity.class);
 		Bundle bundle = new Bundle();
 		bundle.putInt("episode", me.getEpisode().intValue());
 		bundle.putString("title", me.getTitle());
 		bundle.putString("description", me.getDescription());
 		
 		i.putExtras(bundle);
 		startActivity(i);
 	}
 	
 	
 }
