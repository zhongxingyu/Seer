 package biz.lungo.movietask;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Window;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 
 import java.util.ArrayList;
 
 public class MainActivity extends Activity {
     public static Context activity;
     TableLayout table;
     static ArrayList<MovieObject> movieList;
     String moviesTitles[];
     TypedArray moviePosters;
     String movieYear[];
     String movieGenre[];
     String movieDirector[];
     String movieActors[];
     String movieDescription[];
     String movieCountry[];
    static final int COLUMNS_COUNT = 3;
     int movieCounter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         activity = this;
         setContentView(R.layout.activity_main);
         table = (TableLayout) findViewById(R.id.table);
         movieList = new ArrayList<MovieObject>();
         moviesTitles = getResources().getStringArray(R.array.movie_title_array);
         movieYear = getResources().getStringArray(R.array.movie_year_array);
         movieGenre = getResources().getStringArray(R.array.movie_genre_array);
         movieDirector = getResources().getStringArray(R.array.movie_directors_array);
         movieActors = getResources().getStringArray(R.array.movie_actors_array);
         movieDescription = getResources().getStringArray(R.array.movie_descriptions_array);
         movieCountry = getResources().getStringArray(R.array.movie_country_array);
         moviePosters = getResources().obtainTypedArray(R.array.movie_posters_array);
         fillMovieList();
 
        for (int i = 0; i <= moviesTitles.length/COLUMNS_COUNT + 1; i++) {
             TableRow row = new TableRow(this);
             row.setWeightSum(15);
             table.addView(row);
             for (int j = 0; j < 3; j++){
                 if (movieCounter < moviesTitles.length) {
                     MovieObject movie = movieList.get(movieCounter);
                     row.addView(new MoviePosterView(this, movie.getPosterImageId(), movie.getTitle(), movie.getGenre(), movieCounter));
                 }
                 movieCounter++;
             }
         }
     }
     private void fillMovieList() {
         for (int i = 0; i < moviesTitles.length; i++){
             movieList.add(new MovieObject(moviePosters.getResourceId(i, 0),
                     moviesTitles[i],
                     movieGenre[i],
                     movieYear[i],
                     movieCountry[i],
                     movieDirector[i],
                     movieActors[i],
                     movieDescription[i]));
         }
     }
 
 }
