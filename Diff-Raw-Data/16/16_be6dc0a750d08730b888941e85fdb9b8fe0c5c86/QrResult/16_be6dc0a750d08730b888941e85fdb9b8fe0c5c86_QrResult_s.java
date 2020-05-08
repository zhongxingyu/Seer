 package com.youpony.amuse;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.youpony.amuse.ImageDownloader;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AbsListView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class QrResult extends Activity {
 
 	
 	
 	String id, exib;
 	String a, n, y, d, m, im, oid;
 	int i;
 	
 	//set JSON object fields
 	private static final String author = "author";
 	private static final String year = "year";
 	private static final String name = "name";
 	private static final String description = "desc";
 	private static final String ex_id = "id";
 	private static final String ex_name = "name";
 	private static final String image = "images";
 	private static final String o_id = "id";
 	
 	String APIauthor;
 	public static JSONObject json;
 	TextView t1,t2,t3,t4,titolo1,titolo2,titolo3,titolo4;
 	View linea1, linea2, linea3;
 	TextView title;
 	ImageView imageView;
 	Boolean stop = false;
 	Item oggetto;
 	Button confirm, cancel;
 	String immagine;
 	ImageDownloader downloader;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_loading);
 		
 		oggetto = new Item();
 		//set TextView
 		titolo1= new TextView(this);
 		titolo2= new TextView(this);
 		titolo3= new TextView(this);
 		titolo4= new TextView(this);
 		
 		t1 = new TextView(this);
 		t2 = new TextView(this);
 		t3 = new TextView(this);
 		t4 = new TextView(this);
 		imageView = new ImageView(this);
 		downloader = new ImageDownloader();
 		
 		Intent intent = getIntent();
 		setApi(intent);
 		PageViewer.mViewPager.postInvalidate();
 		
 		if(stop == false){
 			new JSONParsing(){
 				protected void onPostExecute(JSONObject jData) {
 					if (jData != null){
 							yesInternet();
 							updateExhibitions(jData);
 							Boolean check = Story.findName(oggetto.name);
 							int position = Story.findPos(oggetto.name);
 							
 							if( Story.start == false && Story.id_mostra != oggetto.e_id){
 								Log.i("orrudebug", "cambio mostra: da " + Story.id_mostra + " a " + oggetto.e_id);
 								try{
 									Intent change = new Intent(PageViewer.getAppContext(), ExhibitionChange.class);
 									Bundle pass_obj = new Bundle();
 									pass_obj.putSerializable("item_value", oggetto);
 									change.putExtras(pass_obj);
 									startActivity(change);
 									close();
 								}
 								catch (Exception ex){
 									Log.i("orrudebug", "cannot start exibition change");
 								}
 							}
 							else{
 									if(check == true){
 										close();
 										Intent info = new Intent(PageViewer.getAppContext(), ItemInfo.class);
 //										if(position%2 == 0){
 //											info.putExtra("pos", (position*2));
 //										}
 //										else{
 //											info.putExtra("pos", ((position*2)+1));
 //										}
 										
 										info.putExtra("pos", position);
 										Log.i("orrudebug","qr code already scanned with name " + oggetto.name + " and position " + position);
 										startActivity(info);
 									}
 									else if(oggetto.name == null){
 										wrongQr();
 									}
 									else{
 										//instantiate layout
 										setContentView(R.layout.activity_qr_result);
 										titolo1 = (TextView) findViewById(R.id.autoreTitle);
 										t1 = (TextView) findViewById(R.id.autoreText);
 										titolo2 = (TextView) findViewById(R.id.annoTitle);
 										t2 = (TextView) findViewById(R.id.annoText);
 										titolo3 = (TextView) findViewById(R.id.descTitle);
 										t3 = (TextView) findViewById(R.id.descText);
 										titolo4 = (TextView) findViewById(R.id.mostraTitle);
 										t4 = (TextView) findViewById(R.id.mostraText);
 										imageView = (ImageView) findViewById(R.id.imageView);
 										title = (TextView) findViewById(R.id.title);
 										linea1 = (View) findViewById(R.id.lineaAutore);
 										linea2 = (View) findViewById(R.id.lineaAnno);
 										linea3 = (View) findViewById(R.id.lineaDesc);
 										
 										//display object infos
 										
 										if(oggetto.author!="ignoto") {
 											t1.setText(oggetto.author);
 										} else {
 											titolo1.setVisibility(2);
 										}
 										
 										if(oggetto.year!="0") {
 											t2.setText(oggetto.year);
 										} else{
 											titolo2.setVisibility(2);
 										}
 										
 										t3.setText(oggetto.description);
 										t4.setText(oggetto.mostra);
 											
 										//add scroll listener on Text
 										t3.setMovementMethod(new ScrollingMovementMethod());
 										
 										//display object name
 										title.setText(oggetto.name);
 										
 										//display object image
 										if( im != null){
 											im = downloader.download(im, imageView);
 										}
 										else{
 											Log.i("orrudebug", "non c' l'immagine di questo oggetto");
 										}
 										//NB: IMPEDIRE IL CARICAMENTO DEL LAYOUT PRIMA CHE L'IMMAGINE SIA SCARICATA
 										
 										//manage Confirm button action
 										confirm = (Button) findViewById(R.id.confirm);
 										confirm.setOnClickListener(new OnClickListener() {
 											@Override
 											public void onClick(View v) {
 													Story.start = false;
 													Story.id_mostra = oggetto.e_id;
 													if(im != null){
 														PageViewer.values.add(oggetto);
 //														
 														PageViewer.pinterestItems.add(im);
 														Story.pinterestAdapter.notifyDataSetChanged();
 														
 														Log.i("orrudebug", "aggiunto oggetto con id: " + oggetto.id);
 													}
 													//this is wrong (just for debugging on local without images) REMOVE IT!
 													else{
 														PageViewer.values.add(oggetto);
 													}
 													close();
 											}
 										});
 										
 										//manage Cancel button action
 										cancel = (Button) findViewById(R.id.cancel);
 										cancel.setOnClickListener(new OnClickListener() {
 											@Override
 											public void onClick(View v) {
 												close();
 											}
 										});
 									}
 								}
 							}
 					
 				else{ 
 					Log.i("orrudebug", "no internet amigo");
 					noInternet();
 				}
 				}
 			}.execute(JSONParsing.ITEM + id + "/");
 		}
 	}
 		
 	
 	//and now let's get some JSON
 	protected void updateExhibitions(JSONObject jData) {
 		//Log.i("orrudebug", "doinBackground :" + jData.toString());
 		
 		try{
 			JSONObject c = jData.getJSONObject("data");
 				try {
 					n = c.getString(name);
 					
 				} catch (JSONException e) {
 					Log.i("orrudebug", "hai sbagliato API, chiama e ostia contro Luca Colleoni");
 					e.printStackTrace();
 				}
 				//METTERE A POSTO L'INSERIMENTO DELL'ID DELL'OGGETTO
 				oid = id.toString();
 				
 				try {
 					a = c.getString(author);
 				} catch (JSONException e) {
 					Log.i("orrudebug", "hai sbagliato API, chiama e ostia contro Luca Colleoni");
 					e.printStackTrace();
 				}
 				try {
 					d = c.getString(description);
 				} catch (JSONException e) {
 					Log.i("orrudebug", "hai sbagliato API, chiama e ostia contro Luca Colleoni");
 					e.printStackTrace();
 				}
 				try {
 					y = c.getString(year);
 				} catch (JSONException e) {
 					Log.i("orrudebug", "hai sbagliato API, chiama e ostia contro Luca Colleoni");
 					e.printStackTrace();
 				}
 				JSONArray ex = c.getJSONArray("exhibitions");
 				JSONObject o = ex.getJSONObject(0);
 				try {
 					i = o.getInt(ex_id);
 				} catch (JSONException e) {
 					Log.i("orrudebug", "hai sbagliato API, chiama e ostia contro Luca Colleoni");
 					e.printStackTrace();
 				}
 				try {
 					m = o.getString(ex_name);
 				} catch (JSONException e) {
 					Log.i("orrudebug", "hai sbagliato API, chiama e ostia contro Luca Colleoni");
 					e.printStackTrace();
 				}
 				JSONArray images = c.getJSONArray(image);
 				try{
 					im = images.getString(0);
 					
 				} catch (JSONException e){
 					Log.i("orrudebug", "hai sbagliato API, non trovo immagine");
 					e.printStackTrace();
 				}
 		}
 		catch (JSONException e){
 			Log.i("orrudebug", "ciao, sono crashata!");
 			e.printStackTrace();
 		}
 
 		oggetto.author = a;
 		oggetto.name = n;
 		oggetto.year = y;
 		oggetto.description = d;
 		oggetto.e_id = i;
 		oggetto.mostra = m;
 		oggetto.url = im;
 		oggetto.id = oid;
 	}
 
 	public void close(){
 		QrResult.this.finish();
 		PageViewer.mViewPager.setCurrentItem(1);
 	}
 	
 	void wrongQr(){
 		stop = true;
 		QrResult.this.finish();
 		PageViewer.mViewPager.setCurrentItem(2);
 		QrCode.start_qr.setText("wrong QrCode, try again!");
 	}
 	
 	void noInternet(){
 		stop = true;
 		QrResult.this.finish();
 		PageViewer.mViewPager.setCurrentItem(2);
 		QrCode.start_qr.setText("no internet connection, try again!");
 	}
 	
 	void yesInternet(){
 		QrCode.start_qr.setText(QrCode.BASE_TEXT);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.qr_result, menu);
 		
 		return true;
 	}
 	
 	private void setApi(Intent intent){
 		String message = intent.getStringExtra(PageViewer.EXTRA_MESSAGE);
 		String tmp[];
 		String del = "&";
 		if(message != null){
 			Log.i("orrudebug", "scan presente!");
 			if(message.contains("&")){
 				tmp = message.split(del, 2);
 				try { 
 					exib = tmp[0];
 					} 
 				catch(NumberFormatException nFE) { 
 					wrongQr();
 				}
 				try { 
 					id = tmp[1];
 					} 
 				catch(NumberFormatException nFE) { 
 					wrongQr();
 				}
 			}
 			else{
 				stop = true;
 				wrongQr();
 			}
 		}
 		else{
 			Log.i("orrudebug", "scan nullo!");
 			stop = true;
 			close();
 		}
 	}
 	
 
 }
