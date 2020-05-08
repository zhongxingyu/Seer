 package com.binroot.fatpita;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class FatPitaPlusActivity extends Activity {
 
 	ImageView iv;
 	ApplicationStart appState;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		iv = (ImageView) findViewById(R.id.img);
 		mHandler = new Handler();
 		
 		appState = ((ApplicationStart)getApplicationContext());
 		
 		Log.d("fatpita", "appState = "+appState+", "+appState.getURL());
 		
 		if(appState.getURL()!=null) {
 			updateImage(appState.getURL());
 		}
 		else {
 			updateImage();
 		}
 		
 		Button mainButton = (Button) findViewById(R.id.button);
 		mainButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				updateImage();
 			}
 		});
 		
 		Button backButton = (Button) findViewById(R.id.button_back);
 		backButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				goBack();
 			}
 		});
 	}
 
 	private void updateImage() {
 		String url = randomURL();
 		updateImage(url);
 	}
 	
 	private void updateImage(String url) {
 		iv.setImageDrawable(loadNewImage(url));
 	}
 	
 	
 	/**
 	 * Menu display
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add("Go Back");
 		menu.add("Save Pic");
 		menu.add("Get Saved Pic");
 		return result;   
 	}
 
 	Handler mHandler;
 	/**
 	 * Menu event listener
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getTitle().equals("Go Back")) {
 			mHandler.post(new Runnable() {
 				public void run() {
 					goBack();
 				}
 			});
 		}
 		else if(item.getTitle().equals("Save Pic")) {
 			String FILENAME = "favpic";
 			
 			FileOutputStream fos = null;
 			try {
 				fos = openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
 			} catch (FileNotFoundException e2) {
 				e2.printStackTrace();
 			}
 			
 			InputStream is = null;
 			try {
 				is = (InputStream) new URL(appState.getURL()).getContent();
 			} catch (MalformedURLException e1) {
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 			Drawable d = Drawable.createFromStream(is, "src name"); 
 			Bitmap b = ((BitmapDrawable)d ).getBitmap();
 			
 			ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		    b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
 		    byte[] imageInByte = stream.toByteArray();
 		    
 		    try {
 		    	fos.write(imageInByte);
 				fos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else if(item.getTitle().equals("Get Saved Pic")) {
 			FileInputStream fis = null;
 			try {
 				fis = openFileInput("favpic");
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			
 			Bitmap b = BitmapFactory.decodeStream(fis);
 			iv.setImageBitmap(b);
 			
 			try {
 				fis.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	
 	public void goBack() {
 		if(appState.getBackCursor()!=0) {
 			appState.setBackCursor(appState.getBackCursor()-1);
 			iv.setImageDrawable(loadImage(appState.getHistory().get(appState.getBackCursor())));
 			appState.setURL(appState.getHistory().get(appState.getBackCursor()));
 		}
 	}
 
 	/**
 	 * Save url loading
 	 * @param url - The url of an image
 	 * @return A Drawable image from url
 	 */
 	private Drawable loadImage(String url) {
 		Drawable d = loadImageFromWebOperations(url);
 		while(d == null) {
 			url = randomURL();
 			d = loadImageFromWebOperations(url);
 		}
 		return d;
 	}
 	
 	private Drawable loadNewImage(String url) {
 		Drawable d = loadImageFromWebOperations(url);
 		while(d == null) {
 			url = randomURL();
 			d = loadImageFromWebOperations(url);
 		}
 		
 		appState.getHistory().add(url);
 		appState.setBackCursor(appState.getHistory().size()-1);
 		appState.setURL(url);
 		
 		return d;
 	}
 	
 	private String randomURL() {
 		int pick = (int)(Math.random()*3)+1;
 		
 		String url = "";
 		
 		if(pick==1) {
 			url = loadURLFrom("fatpita");
 		}
 		else if (pick==2) {
 			url = loadURLFrom("fukung");
 		}
 		else if (pick==3) {
 			url = loadURLFrom("eatliver");
 		}
 		return url;
 	}
 	
 	private String loadURLFrom(String website) {
 		String url = null;
 		
 		if(website.equals("fatpita")) {
 			int fatpitaRand = (int) (Math.random()*10000);
 			url = "http://fatpita.net/images/image%20(" +
 				fatpitaRand + ").jpg";
 			Log.d("fatpita", "image from fatpita: "+fatpitaRand);
 		}
 		else if(website.equals("fukung")) {
 			int fukungRand = (int) (Math.random()*41420) +1;
 			url = "http://media.fukung.net/images/" +
 				fukungRand +
 				".jpg";
 			Log.d("fatpita", "image from fukung: "+fukungRand);
 		}
 		else if(website.equals("eatliver")) {
 			// 2005: 1..780
 			// 2006: 781..1656
 			// 2007: 1657..2688
 			// 2008: 2689..3840
 			// 2009: 3841..5196
 			// 2010: 5197..6588
 			// 2011: 6589..7452
 			int year = 0;
 			int eatLiverRand = (int) (Math.random()*7452) +1;
			if(eatLiverRand>=1 || eatLiverRand<=780) {
 				year = 2005;
 			}
 			else if(eatLiverRand>=781 && eatLiverRand<=1656) {
 				year = 2006;
 			}
 			else if(eatLiverRand>=1657 && eatLiverRand<=2688) {
 				year = 2007;
 			}
 			else if(eatLiverRand>=2689 && eatLiverRand<=3840) {
 				year = 2008;
 			}
 			else if(eatLiverRand>=3841 && eatLiverRand<=5196) {
 				year = 2009;
 			}
 			else if(eatLiverRand>=5197 && eatLiverRand<=6588) {
 				year = 2010;
 			}
 			else if(eatLiverRand>=6589 && eatLiverRand<=7452) {
 				year = 2011;
 			}
 			
 			url = "http://www.eatliver.com/img/" +
 					year +
 					"/" +
 					eatLiverRand +
 					".jpg";
 			Log.d("fatpita", "image from eatliver: "+year+"/"+eatLiverRand);
 		}
 		
 		return url;
 	}
 	
 	private Drawable loadImageFromWebOperations(String url) 
 	{ 
 		try { 
 			InputStream is = (InputStream) new URL(url).getContent(); 
 			Drawable d = Drawable.createFromStream(is, "src name"); 
 			
 			Bitmap b = ((BitmapDrawable)d ).getBitmap();
 			Log.d("fatpita", "loading image "+b.getWidth()+"x"+b.getHeight());
 			// Ignore large images
 			if(b.getWidth()>1000 || b.getHeight()>1000) {
 				return null; 
 			}
 			return d; 
 		}
 		catch (Exception e) { 
 			Log.d("fatpita", "exception = "+e.getMessage());
 			return null; 
 		} 
 	}
 }
