 package com.example.flickr;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.GridView;
 
 public class flickr extends Activity implements Runnable {
 
 	private FlickrAdapter imageAdapter;
 	private ProgressDialog waitDialog;
 
 	private String tags = "cute, kitty";
 
 	static final int MENU_SEARCH = 0;
 	static final int MENU_QUIT = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		search();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_SEARCH, 0, "Search");
 		menu.add(0, MENU_QUIT, 0, "Quit");
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_SEARCH:
 			showSearchDialog();
 			return true;
 		case MENU_QUIT:
 			showQuitDialog();
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	public void showSearchDialog() {
 		
 		final EditText input = new EditText(this);
 		input.setText(tags);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this)
 			.setTitle("Search")
 			.setMessage("Tags")
 			.setView(input)
 			.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					tags = input.getText().toString();
 					search();
 				}
 			})
 			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void showQuitDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this)
 			.setMessage("Are you sure you want to exit?")
 			.setCancelable(false)
 			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					flickr.this.finish();
 				}
 			})
 			.setNegativeButton("No", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void search() {
 		imageAdapter = new FlickrAdapter(this);
 
		waitDialog = ProgressDialog.show(this, "", "Searching...", true, false);
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 
 	private Drawable fetchDrawable(String url) throws MalformedURLException,
 			IOException {
 
 		URL resource = new URL(url);
 		InputStream istream = (InputStream) resource.getContent();
 		Drawable photo = Drawable.createFromStream(istream, "src");
 		return photo;
 	}
 
 	public void run() {
 		try {
 
 			String url = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20flickr.photos.search(21)%20where%20tags%3D'"
 					+ URLEncoder.encode(tags, "UTF-8")
 					+ "'%20and%20tag_mode%3D'all'&format=xml&diagnostics=false";
 
 			URL text = new URL(url);
 			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 			FlickrYQLHandler flickrHandler = new FlickrYQLHandler();
 			parser.parse(text.openStream(), flickrHandler);
 
 			ArrayList<FlickrRecord> photos = flickrHandler.getPhotos();
 			for (Iterator<FlickrRecord> itr = photos.iterator(); itr.hasNext();) {
 				FlickrRecord photo = itr.next();
 				imageAdapter.addPhoto(fetchDrawable(photo.getThumbsUrl()));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		handler.sendEmptyMessage(0);
 	}
 
 	private Handler handler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			GridView gridview = (GridView) findViewById(R.id.gridview);
 			gridview.setAdapter(imageAdapter);
 
 			waitDialog.dismiss();
 		}
 	};
 }
