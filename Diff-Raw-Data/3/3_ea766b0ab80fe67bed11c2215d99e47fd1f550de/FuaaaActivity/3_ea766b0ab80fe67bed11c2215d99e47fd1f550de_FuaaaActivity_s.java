 package com.reddementes.fua;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class FuaaaActivity extends Activity {
 	private Button btnFua;
 	private int sonido;
 	private String srcAudio = null;
 	private int img;
 	private ImageView imageView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 		sonido = R.raw.fua3;
 		btnFua = (Button) findViewById(R.id.button1);
 		imageView=(ImageView) findViewById(R.id.ImageView1);
 		btnFua.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Thread x = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						MediaPlayer mp = MediaPlayer.create(FuaaaActivity.this,
 								sonido);
						imageView.setImageResource(img);
 						mp.start();
 						while (mp.isPlaying()) {
 						}
 					}
 				});
 				x.start();
 			}
 
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.principal_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		boolean returnSt = false;
 		try {
 			item.setChecked(true);
 			switch (item.getItemId()) {
 			case R.id.MnuOpc11:
 				srcAudio = "Fua";
 				sonido = R.raw.fua3;
 				img =R.drawable.bubble_fua;
 				returnSt = true;
 				break;
 			case R.id.MnuOpc12:
 				srcAudio = "Fua2";
 				sonido = R.raw.fua1;
 				img =R.drawable.bubble_puedo;
 				returnSt = true;
 				break;
 			case R.id.MnuOpc13:
 				srcAudio = "Fua3";
 				sonido = R.raw.fua2;
 				img =R.drawable.bubble_caracter;
 				returnSt = true;
 				break;
 			case R.id.MnuOpc14:
 				srcAudio = "Fua4";
 				sonido = R.raw.fua4;
 				img =R.drawable.bubble_fuafua;
 				returnSt = true;
 				break;
 			case R.id.MnuOpc15:
 				srcAudio = "Fua5";
 				sonido = R.raw.fua5;
 				img =R.drawable.bubble_fuaaa;
 				returnSt = true;
 				break;
 			case R.id.MnuOpc2:
 				startActivity(new Intent(Intent.ACTION_VIEW,
 						Uri.parse("vnd.youtube://JVept9huYIY")));
 				returnSt = true;
 				break;
 			case R.id.MnuOpc3:
 				srcAudio = "Compartir";
 				share();
 				returnSt = true;
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 			}
 		} catch (Exception e) {
 
 		}
 		return returnSt;
 
 	}
 
 	public void share() {
 		String subject="WOW!!!";
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setType("text/plain");
 		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
 		intent.putExtra(Intent.EXTRA_TEXT, "Estoy lanzando un FUAAA!!! de una aplicaci�n #android hecha por @REDDEMENTES [ruta]");
 		startActivity(Intent.createChooser(intent,getString(R.string.share)));
 	}
 
 	private void goVideo() {
 		Intent intent = new Intent(Intent.ACTION_VIEW,
 				Uri.parse("vnd.youtube://JVept9huYIY"));
 		startActivity(intent);
 	}
 }
