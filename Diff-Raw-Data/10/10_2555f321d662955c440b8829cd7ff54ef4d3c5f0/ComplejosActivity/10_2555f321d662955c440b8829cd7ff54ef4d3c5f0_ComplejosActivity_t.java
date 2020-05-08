 package com.cinemar.phoneticket;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.cinemar.phoneticket.films.DownloadImageTask;
 import com.cinemar.phoneticket.films.FilmOnClickListener;
 import com.cinemar.phoneticket.model.Theatre;
 import com.cinemar.phoneticket.theaters.TheatreOnClickListener;
 import com.cinemar.phoneticket.theaters.TheatresClientAPI;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import android.os.Bundle;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class ComplejosActivity extends AbstractApiConsumerActivity {
 
 	public ComplejosActivity() {
 		super();
 	}
 
 	Map<String, Theatre> theatresMap = new HashMap<String, Theatre>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_complejos);
 
 		// ** Important to get in order to use the showProgress method**//
 		mMainView = findViewById(R.id.complejosScrollView);
 		mStatusView = findViewById(R.id.complejos_status);
 		mStatusMessageView = (TextView) findViewById(R.id.complejos_status_message);
 
 		this.requestComplejos();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.complejos, menu);
 		return true;
 	}
 
 	private void requestComplejos() {
 		mStatusMessageView.setText(R.string.complejos_progress_getting);
 		showProgress(true);
 
 		TheatresClientAPI api = new TheatresClientAPI();
 		api.getTheatres(new JsonHttpResponseHandler() {
 
 			@Override
 			public void onSuccess(JSONArray theatres) {
 				Log.i("Complejos Activity", "Complejos Recibidos");
 				try {
 					for (int i = 0; i < theatres.length(); i++) {
 						Theatre complejo = new Theatre(theatres
 								.getJSONObject(i));
 						theatresMap.put(complejo.getId(), complejo);
 						Log.i("Complejos Activity",
 								"Complejo" + theatres.getJSONObject(i)
 										+ "recibido");
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable e, JSONObject errorResponse) {
 				Log.i("Complejos Activity", "Failure recibiendo complejos");
 				if (errorResponse != null) {
 					showSimpleAlert(errorResponse.optString("error"));
 				} else {
 					showSimpleAlert(e.getMessage());
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable arg0, String arg1) {
 				showSimpleAlert(arg1);
 			};
 
 			public void onFinish() {
 				showProgress(false);
 				displayTheatres();
 			}
 
 		});
 
 	}
 
 	private void goToCarteleraActivity(Theatre theatre) {
 		Intent intent = new Intent(this, PeliculasActivity.class);
 		intent.putExtra("theatreId", theatre.getId());
 		intent.putExtra("theatreName", theatre.getName());
 		startActivity(intent);
 
 	}
 
 	private void displayTheatres() {
 		LinearLayout theatresContainer = (LinearLayout) findViewById(R.id.complejosContainer);
 
 		for (Theatre theatre : theatresMap.values()) {
 			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
 					ViewGroup.LayoutParams.MATCH_PARENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT);
 			layout.setMargins(5, 5, 5, 5); // no se porqua no me da bola con
 											// esto
 
 			RelativeLayout theatreLayout = new RelativeLayout(this);
 			theatreLayout.setBackgroundResource(R.drawable.border);
 			theatreLayout.setLayoutParams(layout);
 
 			// Title
 			TextView titleText = new TextView(this);
 			titleText.setId(View.generateViewId());
 			titleText.setTypeface(null, Typeface.BOLD);
 			titleText.setText(theatre.getName());
 
 			// Direccion
 			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
 					ViewGroup.LayoutParams.WRAP_CONTENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT);
 			p.addRule(RelativeLayout.BELOW, titleText.getId());
 
 			TextView dirText = new TextView(this);
 			dirText.setId(View.generateViewId());
 			dirText.setLayoutParams(p);
 			dirText.setText(theatre.getAddress());
 
 			// Photo
 			RelativeLayout.LayoutParams photoLayout = new RelativeLayout.LayoutParams(
 					ViewGroup.LayoutParams.WRAP_CONTENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT);
 			photoLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 
 			ImageView theatrePhotoView = new ImageView(this);
 			theatrePhotoView.setLayoutParams(photoLayout);
 			theatrePhotoView.setMaxHeight(50);
 			theatrePhotoView.setMaxWidth(50);
 
 			theatrePhotoView.setImageResource(R.drawable.film_cover_missing);
			new DownloadImageTask(theatrePhotoView).execute(theatre.getPhotoUrl());
 
 			// Map Button
 			RelativeLayout.LayoutParams mapButtonLayout = new RelativeLayout.LayoutParams(
 					ViewGroup.LayoutParams.WRAP_CONTENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT);
 			mapButtonLayout.addRule(RelativeLayout.BELOW, dirText.getId());
 			mapButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 			mapButtonLayout.height = 70;
 			mapButtonLayout.width = 70;
 
 			ImageView MapButtonView = new ImageButton(this);
 			MapButtonView.setId(View.generateViewId());
 			MapButtonView.setLayoutParams(mapButtonLayout);
 			MapButtonView.setScaleType(ScaleType.CENTER);
 
 			MapButtonView.setImageResource(R.drawable.ic_position_map);
 
 			MapButtonView
 					.setOnClickListener(new TheatreOnClickListener(theatre) {
 						public void onClick(View arg0) {
 							//TODO: metodo para pedir integrar con mapa del complejo
 						}
 
 					});
 
 			// Films Button
 			RelativeLayout.LayoutParams filmsButtonLayout = new RelativeLayout.LayoutParams(
 					ViewGroup.LayoutParams.WRAP_CONTENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT);
 			filmsButtonLayout.addRule(RelativeLayout.RIGHT_OF,
 					MapButtonView.getId());
 			filmsButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 			filmsButtonLayout.height = 70;
 			filmsButtonLayout.width = 70;
 
 			ImageView FilmButtonView = new ImageButton(this);
 			FilmButtonView.setLayoutParams(filmsButtonLayout);
 			FilmButtonView.setScaleType(ScaleType.CENTER);
 
 			FilmButtonView.setImageResource(R.drawable.peliculas);
 			
 			FilmButtonView
 			.setOnClickListener(new TheatreOnClickListener(theatre) {
 				public void onClick(View arg0) {
 					goToCarteleraActivity(theatre);
 				}
 			});
 
 			theatreLayout.addView(titleText);
 			theatreLayout.addView(dirText);
 			theatreLayout.addView(theatrePhotoView);
 			theatreLayout.addView(MapButtonView);
 			theatreLayout.addView(FilmButtonView);
 
 			theatresContainer.addView(theatreLayout);
 		}
 
 	}
 
 }
