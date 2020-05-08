 package com.example.tvguide;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ShowGuide extends ListActivity {
 	private TextView selection;
 	private ArrayList<Episode> episodes = new ArrayList<Episode>();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.main);
 
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 				.permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 
 		Intent intent = this.getIntent();
 		String id = intent.getStringExtra("id");
 		String name = intent.getStringExtra("name");
 
 		try {
 			URL url = new URL(
 					"http://epguides.com/common/exportToCSV.asp?rage=" + id);
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					url.openStream()));
 			ArrayList<String> lines = new ArrayList<String>();
 			String line_;
 			while ((line_ = in.readLine()) != null) {
 				lines.add(line_);
 			}
 			in.close();
 			lines = new ArrayList<String>(lines.subList(8, lines.size() - 3));
 			for (String line : lines) {
				String[] values = line.split("\"");
				String[] seasonEpisode = values[0].split(",");
				String season = seasonEpisode[1];
				String episode = seasonEpisode[2];
				String airdate = values[2].replaceAll(",", "");
				String title = values[3];
				Episode e = new Episode(season, episode, airdate, title);
				this.episodes.add(e);
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.setListAdapter(new ArrayAdapter<Episode>(this,
 				android.R.layout.simple_list_item_1, this.episodes));
 		this.selection = (TextView) this.findViewById(R.id.selection);
 		this.selection.setText(name);
 	}
 
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Episode episode = this.episodes.get(position);
 		new AlertDialog.Builder(this).setTitle(episode.toString())
 				.setMessage(episode.getDescription())
 				.setNeutralButton("Okay", null).show();
 	}
 }
