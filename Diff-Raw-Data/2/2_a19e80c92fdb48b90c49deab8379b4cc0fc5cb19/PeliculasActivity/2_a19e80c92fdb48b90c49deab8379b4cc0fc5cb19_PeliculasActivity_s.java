 package com.cinemar.phoneticket;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.cinemar.phoneticket.films.DownloadImageTask;
 import com.cinemar.phoneticket.films.FilmOnClickListener;
 import com.cinemar.phoneticket.films.FilmsClientAPI;
 import com.cinemar.phoneticket.model.Film;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class PeliculasActivity extends AbstractApiConsumerActivity {
 	
 	public PeliculasActivity() {
 		super();
 	}
 
 	Map<String,Film> filmsMap = new HashMap<String,Film>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		
 		setContentView(R.layout.activity_peliculas);
 		
 		//** Important to get in order to use the showProgress method**//
 		mMainView = findViewById(R.id.peliculasHorizontalScrollView);
 		mStatusView = findViewById(R.id.peliculas_status);
 		mStatusMessageView = (TextView) findViewById(R.id.peliculas_status_message);		
 		
 		HorizontalScrollView mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.peliculasHorizontalScrollView);
 
 		this.requestPeliculas();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.peliculas, menu);
 		return true;
 	}
 
 	private void requestPeliculas() {
 		mStatusMessageView.setText(R.string.peliculas_progress_getting);
 		showProgress(true);
 		
 
 		FilmsClientAPI api = new FilmsClientAPI();
 		api.getFilms(new JsonHttpResponseHandler() {	
 
 			@Override
 			public void onSuccess(JSONArray films) {
 				Log.i("Peliculas Activity", "Peliculas Recibidas");
 				try {					
 					for (int i = 0; i < films.length(); i++) {
 						Film film = new Film(films.getJSONObject(i));
 						filmsMap.put(film.getId(), film);
 						Log.i("Peliculas Activity","Pelicula" + films.getJSONObject(i)+ "recibida");
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable e, JSONObject errorResponse) {
 				Log.i("Peliculas Activity", "Failure recibiendo peliculas");
 				if (errorResponse != null) {
 					showSimpleAlert(errorResponse.optString("error"));
 				} else {
 					showSimpleAlert(e.getMessage());
 				}
 			}
 			
 			@Override public void onFailure(Throwable arg0, String arg1) {
				showSimpleAlert(arg1);			
 			};
 			
 			public void onFinish() {
 				showProgress(false);
 				displayFilms();
 			}
 
 		});
 
 	}	
 	
 
 	private void goToFuncionActivity(String filmId) {
 		Intent intent = new Intent(this, PeliculasFuncionActivity.class);
 		Film filmSelected = filmsMap.get(filmId);
 		intent.putExtra("filmId", filmId);
 		intent.putExtra("filmTitle",filmSelected.getTitle());		
 		intent.putExtra("filmSinopsis",filmSelected.getSynopsis());
 		intent.putExtra("filmCoverUrl",filmSelected.getCoverURL());
 		intent.putExtra("filmYouTubeTrailer",filmSelected.getYouTubeTrailerURL());
 		startActivity(intent);
 		
 	}
 
 	private void displayFilms() {
 		LinearLayout imageContainer = (LinearLayout) findViewById (R.id.peliculasImageContainer);
 		
 		for (Film film : filmsMap.values()) {
 
 			ImageView imageView = new ImageView(this);
 			imageView.setOnClickListener(new FilmOnClickListener(film.getId()) {			
 				public void onClick(View arg0) {					
 					goToFuncionActivity(filmId); 
 				}
 
 			});
 			
 			imageView.setImageResource(R.drawable.film_cover_missing);			
 			new DownloadImageTask(imageView).execute(film.getCoverURL());	
 			//asincronicamente carga la imagen en la imageView pasado como argumento
 	
 			//position parameters	
 			imageView.setPadding(10, 10, 10, 10);		
 			//TODO investigate a bit more how to resize images
 						
 			imageContainer.addView(imageView);
 			
 		}
 		
 	}
 
 }
