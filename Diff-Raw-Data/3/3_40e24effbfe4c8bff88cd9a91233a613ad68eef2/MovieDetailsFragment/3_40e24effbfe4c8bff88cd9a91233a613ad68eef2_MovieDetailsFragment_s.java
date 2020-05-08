 package com.github.super8.fragments;
 
 import java.util.List;
 
 import roboguice.inject.InjectView;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import com.github.ignition.core.widgets.RemoteImageView;
 import com.github.super8.R;
 import com.github.super8.apis.tmdb.v3.TmdbApi;
 import com.github.super8.apis.tmdb.v3.TmdbApiHandler;
 import com.github.super8.behavior.ActsAsHomeScreen;
 import com.github.super8.db.LibraryManager;
 import com.github.super8.model.Movie;
 import com.google.inject.Inject;
 
 public class MovieDetailsFragment extends TaskManagingFragment<Movie> implements
     TmdbApiHandler<Movie>, OnClickListener {
 
   private static final int TASK_GET_MOVIE = 0;
 
   @Inject private LibraryManager library;
   @Inject private TmdbApi tmdb;
   @InjectView(R.id.movie_details_title) private TextView movieTitle;
   @InjectView(R.id.movie_details_poster) private RemoteImageView moviePoster;
   @InjectView(R.id.movie_details_watchlist_button) private RadioButton watchlistButton;
   @InjectView(R.id.movie_details_seen_button) private RadioButton seenButton;
   @InjectView(R.id.movie_details_ignore_button) private RadioButton ignoreButton;
   @InjectView(R.id.movie_details_state_buttons) private RadioGroup stateButtons;
 
   private int currentSuggestion = -1;
   private List<Movie> suggestions;
   private Movie movie;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     suggestions = library.getMovieSuggestions();
   }
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     return inflater.inflate(R.layout.movie_details_fragment, null);
   }
 
   @Override
   public void onViewCreated(View view, Bundle savedInstanceState) {
     super.onViewCreated(view, savedInstanceState);
     watchlistButton.setOnClickListener(this);
     seenButton.setOnClickListener(this);
     ignoreButton.setOnClickListener(this);
   }
 
   public void loadNextSuggestion() {
     if (!suggestions.isEmpty()) {
       currentSuggestion = (currentSuggestion + 1) % suggestions.size();
       movie = suggestions.get(currentSuggestion);
       addTask(TASK_GET_MOVIE, tmdb.backfillMovie(this, movie));
     }
   }
 
   @Override
   public boolean onTaskSuccess(Context context, Movie movie) {
     movieTitle.setText(movie.getTitle());
     String imageUrl = movie.getScaledImageUrl(context);
     if (imageUrl != null) {
       moviePoster.setImageUrl(imageUrl);
       moviePoster.loadImage();
     } else {
       // TODO: show dummy image
     }
 
     switch (movie.getState()) {
     case Movie.STATE_MUST_SEE:
       stateButtons.check(R.id.movie_details_watchlist_button);
       break;
     case Movie.STATE_SEEN_IT:
       stateButtons.check(R.id.movie_details_seen_button);
       break;
     case Movie.STATE_IGNORE:
       stateButtons.check(R.id.movie_details_ignore_button);
       break;
     }
 
     ActsAsHomeScreen homeScreen = (ActsAsHomeScreen) getActivity();
     homeScreen.getPresenter().onMovieSuggestionAvailable();
 
     return true;
   }
 
   @Override
   public void onClick(View v) {
     switch (v.getId()) {
     case R.id.movie_details_watchlist_button:
       library.addToWatchlist(movie);
       break;
     case R.id.movie_details_seen_button:
       library.markAsSeen(movie);
       break;
     case R.id.movie_details_ignore_button:
       library.ignoreMovie(movie);
       break;
     }
   }
 }
