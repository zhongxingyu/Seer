 package com.yoharnu.newontv.android.shows;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.concurrent.ExecutionException;
 
 import com.yoharnu.newontv.android.App;
 import com.yoharnu.newontv.android.DownloadFilesTask;
 import com.yoharnu.newontv.android.R;
 import com.yoharnu.newontv.android.Settings;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 public class ChooseSeries extends Activity implements OnItemSelectedListener {
 	Series s;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_choose_series);
 		// Show the Up button in the action bar.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 		s = new Series(App.preferences.getString("search", ""), Series.NAME);
 		s.task = new DownloadFilesTask();
 	}
 
 	protected void onStart() {
 		super.onStart();
 
 		s.task.execute(s.url, s.file);
 		try {
 			s.task.get();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 
 		File temp = new File(s.file);
 		Series.parseSearch(temp);
 		temp.delete();
 		if (App.preferences.getBoolean("only_show_running", false)) {
 			LinkedList<Series> tempOptions = new LinkedList<Series>();
 			for (int i = 0; i < Series.options.size(); i++) {
 				tempOptions.add(Series.options.get(i));
 			}
 			Series.options.clear();
 			for (int i = 0; i < tempOptions.size(); i++) {
 				Series tempSeries = new Series(
 						tempOptions.get(i).getSeriesId(), Series.ID);
 				if (tempSeries.getStatus().equals("Continuing")) {
 					Series.options.add(tempSeries);
 				}
 			}
 		}
 
 		Spinner spinner = (Spinner) findViewById(R.id.spins);
 		LinkedList<String> optionsNames = new LinkedList<String>();
 		for (int i = 0; i < Series.options.size(); i++) {
 			optionsNames.add(Series.options.get(i).getSeriesName());
 		}
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 				App.getContext(), android.R.layout.simple_dropdown_item_1line,
 				optionsNames);
 		spinner.setAdapter(adapter);
 		spinner.setOnItemSelectedListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_choose_series, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.menu_add_remove:
 			startActivity(new Intent(this, EditShowsList.class));
 			return true;
 		case R.id.menu_settings:
 			startActivity(new Intent(this, Settings.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		String name = (String) parent.getItemAtPosition(pos);
 		Series series = null;
 		for (int i = 0; i < Series.options.size(); i++) {
 			if (name.equals(Series.options.get(i).getSeriesName())) {
 				series = Series.options.get(i);
 				break;
 			}
 		}
 		TextView overview = (TextView) findViewById(R.id.spins_overview);
 		overview.setText("First Aired: " + series.getFirstAired()
 				+ "\nOverview: " + series.getOverview());
 		overview.setSingleLine(false);
 
 		overview.setVisibility(View.VISIBLE);
 	}
 
 	public void add(View view) {
 		Spinner spinner = (Spinner) findViewById(R.id.spins);
 		TextView t = (TextView) spinner.getSelectedView();
 		String text = t.getText().toString();
 		for (int i = 0; i < Series.options.size(); i++) {
 			if (text.equals(Series.options.get(i).getSeriesName())) {
 				App.add(Series.options.get(i).getSeriesId());
 			} else {
 				for (int j = 0; j < App.shows.size(); j++) {
 					if (!Series.options.get(i).getSeriesId()
							.equals(App.shows.get(j).getSeriesId())
							&& Series.options.get(i).cache != null) {
 						Series.options.get(i).cache.delete();
 					}
 				}
 			}
 		}
 		this.finish();
 	}
 
 	public void cancel(View view) {
 		this.finish();
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 	}
 
 }
