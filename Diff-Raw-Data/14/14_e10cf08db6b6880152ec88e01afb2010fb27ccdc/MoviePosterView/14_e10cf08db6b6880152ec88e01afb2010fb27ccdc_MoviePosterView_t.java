 package biz.lungo.movietask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Point;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MoviePosterView extends LinearLayout {
     ImageView poster;
     TextView title;
     TextView genre;
 
     public MoviePosterView(Context context, int posterImage, String titleString, String genreString, int tag) {
         super(context);
         WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
         Point size = new Point();
         Display display = wm.getDefaultDisplay();
         display.getSize(size);
        int maxWidth = (size.x - 10) / MainActivity.COLUMNS_COUNT;
         LinearLayout.inflate(context, R.layout.movie_poster_view_layout, this);
         setTag(tag);
         setMinimumWidth(maxWidth);
         final Context ctx = context;
         poster = (ImageView) findViewById(R.id.moviePosterImageView);
         title = (TextView) findViewById(R.id.movieTitle);
         genre = (TextView) findViewById(R.id.movieGenre);
        poster.setMaxWidth(size.x / (MainActivity.COLUMNS_COUNT + 1));
         poster.setImageResource(posterImage);
         title.setMaxWidth(maxWidth);
         title.setText(titleString);
         genre.setMaxWidth(maxWidth);
         genre.setText(genreString);
         setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(ctx, MovieActivity.class);
                 MovieObject movie = MainActivity.movieList.get((Integer) view.getTag());
                 intent.putExtra("PosterImage", movie.getPosterImageId())
                         .putExtra("Title", movie.getTitle())
                         .putExtra("Country", movie.getCountry())
                         .putExtra("Year", movie.getReleaseYear())
                         .putExtra("Genre", movie.getGenre())
                         .putExtra("Director", movie.getDirector())
                         .putExtra("Actors", movie.getActors())
                         .putExtra("Description", movie.getDescription());
                 ctx.startActivity(intent);
             }
         });
     }
 }
