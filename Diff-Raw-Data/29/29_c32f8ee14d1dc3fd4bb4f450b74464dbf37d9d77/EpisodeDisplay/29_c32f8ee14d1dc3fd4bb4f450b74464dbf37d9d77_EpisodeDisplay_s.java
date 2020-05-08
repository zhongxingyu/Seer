 package com.yoharnu.newontv.shows;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 
 import org.apache.commons.io.FileUtils;
 
 import com.yoharnu.newontv.android.R;
 
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class EpisodeDisplay extends Activity {
 	private Episode episode;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_episode_display);
 		// Show the Up button in the action bar.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	protected void onDestroy() {
 		super.onDestroy();
 		if (new File(this.getCacheDir(), "images").listFiles() != null)
 			for (File f : new File(this.getCacheDir(), "images").listFiles()) {
 				if (f.isDirectory() && f.listFiles() != null)
 					for (File f2 : f.listFiles())
 						f2.delete();
 				f.delete();
 			}
 		new File(this.getCacheDir(), "images").delete();
 	}
 
 	protected void onStart() {
 		super.onStart();
 		final Series series = new Series(this.getIntent().getExtras()
 				.getString("series"), Series.ID);
 		String s = this.getIntent().getExtras().getString("season");
 		String e = this.getIntent().getExtras().getString("episode");
 		for (Episode episode : series.episodes) {
 			if (episode.getSeason().equals(s) && episode.getEpisode().equals(e)) {
 				this.episode = episode;
 			}
 		}
 
		final ImageView image = new ImageView(this);
 		final File imageFile = new File(this.getCacheDir(), "images/"
 				+ series.seriesid + "/" + episode.getSeason()
 				+ episode.getEpisode());
 
 		final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_episode_display);
 		layout.addView(image);
 
 		TextView seriesName = new TextView(this);
 		seriesName.setText("Series: " + series.seriesName);
 		layout.addView(seriesName);
 
 		TextView title = new TextView(this);
 		title.setText("Title: " + episode.getEpName());
 		layout.addView(title);
 
 		TextView ep = new TextView(this);
 		ep.setText("Episode: " + episode.getSeason() + episode.getEpisode());
 		layout.addView(ep);
 
 		TextView date = new TextView(this);
 		date.setText("Air Date: "
 				+ (episode.getAirDate().get(Calendar.MONTH) + 1) + "-"
 				+ episode.getAirDate().get(Calendar.DATE) + "-"
 				+ episode.getAirDate().get(Calendar.YEAR));
 		layout.addView(date);
 
 		TextView network = new TextView(this);
 		network.setText("Network: " + series.network);
 		layout.addView(network);
 
 		TextView summary = new TextView(this);
 		summary.setText("Summary (courtesy of tvrage.com):\n"
 				+ episode.getSummary());
 		summary.setSingleLine(false);
 		layout.addView(summary);
 
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					if (episode.getScreencap() != null) {
 						if (!imageFile.exists()) {
 							FileUtils.copyURLToFile(
 									new URL(episode.getScreencap()), imageFile);
 						}
 						EpisodeDisplay.this.runOnUiThread(new Runnable() {
 							public void run() {
 								image.setImageURI(Uri.fromFile(imageFile));
 							}
 						});
 					} else {
 						final File imageFile = new File(EpisodeDisplay.this
 								.getCacheDir(), "images/" + series.seriesid);
 						FileUtils.copyURLToFile(new URL(series.imageUrl),
 								imageFile);
 						EpisodeDisplay.this.runOnUiThread(new Runnable() {
 							public void run() {
 								image.setImageURI(Uri.fromFile(imageFile));
 							}
 						});
 					}
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_episode_display, menu);
 		return true;
 	}
 
 }
