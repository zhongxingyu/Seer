 /**
  *	DatabaseAdapter.java
  *
  *  Adapter to the Content Provider.
  *
  *	@author lisastenberg
  *	@copyright (c) 2012 Johan Brook, Robin Andersson, Lisa Stenberg, Mattias Henriksson
  *	@license MIT
  */
 
 package se.chalmers.watchme.database;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.model.Tag;
 
 public class DatabaseAdapter {
 
 	protected Uri uri_movies = WatchMeContentProvider.CONTENT_URI_MOVIES;
 	protected Uri uri_tags = WatchMeContentProvider.CONTENT_URI_TAGS;
 	protected Uri uri_has_tag = WatchMeContentProvider.CONTENT_URI_HAS_TAG;
 
 	private ContentResolver contentResolver;
 
 	/**
 	 * Creates a new adapter.
 	 * 
 	 * @param contentResolver
 	 *            the ContentResolver.
 	 */
 	public DatabaseAdapter(ContentResolver contentResolver) {
 		this.contentResolver = contentResolver;
 	}
 
 	/**
 	 * The total number of movies.
 	 * 
 	 * @return The number of existing movies
 	 */
 	public int getMovieCount() {
 		return this.getCount(uri_movies);
 	}
 
 	/**
 	 * The total number of tags.
 	 * 
 	 * @return The number of existing tags
 	 */
 	public int getTagCount() {
 		return this.getCount(uri_tags);
 	}
 
 	/**
 	 * Returns the specified Movie.
 	 * 
 	 * @param id
 	 *            The id of the Movie.
 	 * @return null if there is no Movie with the specified id.
 	 */
 	public Movie getMovie(long id) {
 		String selection = MoviesTable.COLUMN_MOVIE_ID + " = " + id;
 		Cursor cursor = contentResolver.query(uri_movies, null, selection,
 				null, null);
 
 		if (cursor.moveToFirst()) {
 
 			String title = cursor.getString(1);
 
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTimeInMillis(Long.parseLong(cursor.getString(4)));
 
 			int rating = Integer.parseInt(cursor.getString(2));
 
 			String note = cursor.getString(3);
 
 			Movie movie = new Movie(title, calendar, rating, note);
 			movie.setId(id);
 			movie.setApiID(Integer.parseInt(cursor.getString(5)));
 
 			/*
 			 * Add the tags to the movie object Split the string with tags into
 			 * separate strings seperated by commas (",")
 			 */
 
 			Cursor tempCursor = getAttachedTags(movie);
 
 			// TODO getCount() doesn't return 0 when it should, why?!
 			// Implementing try-catch method as solution for now
 			try {
 
 				// Don't try to fetch tags if none are attached to movie
 				if (tempCursor.getCount() > 0) {
 
 					List<Tag> tags = new LinkedList<Tag>();
 
 					/*
 					 * Move through cursor and create tag objects from fetched
 					 * data until there is no more rows (i.e. no more tags)
 					 */
 					while (tempCursor.moveToNext()) {
 						Tag tag = new Tag(tempCursor.getString(1));
 						tag.setId(Integer.parseInt(tempCursor.getString(0)));
 
 						tags.add(tag);
 					}
 
 					movie.addTags(tags);
 
 				}
 
 			}
 
 			/*
 			 * Should not happen, but does when no tags are attached to movie.
 			 * Somehow tempCursor.getCount() returns 1 when it should be 0
 			 */
 			catch (NullPointerException e) {
 				e.printStackTrace();
 			}
 
 			return movie;
 		}
 		return null;
 	}
 
 	/**
 	 * Inserts a Movie to the database and set the id of the Movie.
 	 * 
 	 * @param movie
 	 *            Movie to be inserted.
 	 * @return the id of the added Movie.
 	 * @throws MovieAlreadyExistsException
 	 *             If the movie already exists in the database
 	 */
 	public long addMovie(Movie movie) throws MovieAlreadyExistsException {
 		ContentValues values = new ContentValues();
 		values.put(MoviesTable.COLUMN_TITLE, movie.getTitle());
 		values.put(MoviesTable.COLUMN_RATING, movie.getRating());
 		values.put(MoviesTable.COLUMN_NOTE, movie.getNote());
 		values.put(MoviesTable.COLUMN_DATE, movie.getDate().getTimeInMillis());
 		values.put(MoviesTable.COLUMN_IMDB_ID, movie.getApiID());
 		values.put(MoviesTable.COLUMN_POSTER_LARGE,
 				movie.getPosterURL(Movie.PosterSize.MID));
 		values.put(MoviesTable.COLUMN_POSTER_SMALL,
 				movie.getPosterURL(Movie.PosterSize.THUMB));
 
 		Uri uri_movie_id = contentResolver.insert(uri_movies, values);
 		long id = Long.parseLong(uri_movie_id.getLastPathSegment());
 		movie.setId(id);
 
 		if (id == 0) {
 			throw new MovieAlreadyExistsException("'" + movie.getTitle()
 					+ "' already exists!", movie);
 		}
 
 		return id;
 	}
 
 	/**
 	 * Delete a Movie from the database.
 	 * 
 	 * @param movie
 	 *            The movie to be removed.
 	 * @return The number of rows affected
 	 */
 	public int removeMovie(Movie movie) {
 		String where = MoviesTable.COLUMN_MOVIE_ID + " = " + movie.getId();
 
 		return contentResolver.delete(uri_movies, where, null);
 	}
 
 	/**
 	 * Updates information about a Movie.
 	 * 
 	 * @param movie
 	 *            The Movie to be updated.
 	 * @return The number of rows affected
 	 */
 	public int updateMovie(Movie movie) {
 		ContentValues values = new ContentValues();
 		values.put(MoviesTable.COLUMN_TITLE, movie.getTitle());
 		values.put(MoviesTable.COLUMN_RATING, movie.getRating());
 		values.put(MoviesTable.COLUMN_NOTE, movie.getNote());
 		values.put(MoviesTable.COLUMN_DATE, movie.getDate().getTimeInMillis());
 		values.put(MoviesTable.COLUMN_IMDB_ID, movie.getApiID());
 		values.put(MoviesTable.COLUMN_POSTER_LARGE,
 				movie.getPosterURL(Movie.PosterSize.MID));
 		values.put(MoviesTable.COLUMN_POSTER_SMALL,
 				movie.getPosterURL(Movie.PosterSize.THUMB));
 
 		String where = MoviesTable.COLUMN_MOVIE_ID + " = " + movie.getId();
 		return contentResolver.update(uri_movies, values, where, null);
 	}
 
 	/**
 	 * Return all Movies from the database.
 	 * 
 	 * @return all Movies from the database.
 	 */
 	public List<Movie> getAllMovies() {
 		return getAllMovies(null);
 	}
 
 	/**
 	 * Return all Movies from the database in the specified order.
 	 * 
 	 * @param orderBy
 	 *            The attribute to order by.
 	 * @return all Movies from the database in the specified order.
 	 */
 	public List<Movie> getAllMovies(String orderBy) {
 		List<Movie> movies = new ArrayList<Movie>();
 
 		Cursor cursor = contentResolver.query(uri_movies, null, null, null,
 				orderBy);
 
 		while (cursor.moveToNext()) {
 			long id = Long.parseLong(cursor.getString(0));
 			String title = cursor.getString(1);
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTimeInMillis(Long.parseLong(cursor.getString(4)));
 			int rating = Integer.parseInt(cursor.getString(2));
 			String note = cursor.getString(3);
 
 			Movie movie = new Movie(title, calendar, rating, note);
 			movie.setId(id);
 			movie.setApiID(Integer.parseInt(cursor.getString(5)));
 
 			movies.add(movie);
 		}
 		return movies;
 	}
 
 	/**
 	 * Return a Cursor with all Movies in the Database.
 	 * 
 	 * @return all Movies in the Database.
 	 */
 	public Cursor getAllMoviesCursor() {
 		return contentResolver.query(uri_movies, null, null, null, null);
 	}
 
 	/**
 	 * Return a Cursor with all Movies in the Database in the specified order.
 	 * 
 	 * @param orderBy
 	 *            The attribute to order by.
 	 * @return all Movies in the Database in the specified order.
 	 */
 	public Cursor getAllMoviesCursor(String orderBy) {
 		return contentResolver.query(uri_movies, null, null, null, orderBy);
 	}
 
 	/**
 	 * Search for movies with the given name.
 	 * 
 	 * @param movieTitle
 	 *            the title of the Movie.
 	 * @return a Cursor with all movies that has the requested name.
 	 */
 	public Cursor searchForMovies(String movieTitle) {
 		String where = MoviesTable.COLUMN_TITLE + " like " + "'%" + movieTitle
 				+ "%'";
 
 		return contentResolver.query(uri_movies, null, where, null, null);
 	}
 
 	/**
 	 * Returns the specified Tag.
 	 * 
 	 * @param id
 	 *            The id of the Tag.
 	 * @return null if there is no Tag with the specified id.
 	 */
 	public Tag getTag(long id) {
 		String selection = TagsTable.COLUMN_TAG_ID + " = " + id;
 		Cursor cursor = contentResolver.query(uri_tags, null, selection, null,
 				null);
 
 		if (cursor.moveToFirst()) {
 			String name = cursor.getString(1);
 
 			Tag tag = new Tag(name);
 			tag.setId(id);
 
 			return tag;
 		}
 		return null;
 	}
 
 	/**
 	 * Inserts a Tag to the database.
 	 * 
 	 * @param tag
 	 *            The Tag to be inserted.
 	 * @return the id of the added Tag.
 	 */
 	private long addTag(Tag tag) {
 		ContentValues values = new ContentValues();
 		values.put(TagsTable.COLUMN_NAME, tag.getName());
 
 		Uri uri_tag_id = contentResolver.insert(uri_tags, values);
 		tag.setId(Long.parseLong(uri_tag_id.getLastPathSegment()));
 		return tag.getId();
 	}
 
 	/**
 	 * Deletes a Tag from the database.
 	 * 
 	 * @param tag
 	 *            The Tag to be removed.
 	 */
 	public void removeTag(Tag tag) {
 		String where = TagsTable.COLUMN_TAG_ID + " = " + tag.getId();
 
 		contentResolver.delete(uri_tags, where, null);
 	}
 
 	/**
 	 * Return all Tags in the database.
 	 * 
 	 * @return all Tags in the database.
 	 */
 	public List<Tag> getAllTags() {
 		List<Tag> tags = new ArrayList<Tag>();
 		Cursor cursor = contentResolver.query(uri_tags, null, null, null, null);
 
 		while (cursor.moveToNext()) {
 			long id = Long.parseLong(cursor.getString(0));
 			String name = cursor.getString(1);
 
 			Tag tag = new Tag(name);
 			tag.setId(id);
 
 			tags.add(tag);
 		}
 		return tags;
 	}
 
 	/**
 	 * Return a Cursor with all Tags in the Database.
 	 * 
 	 * @return all Tags in the Database.
 	 */
 	public Cursor getAllTagsCursor() {
 		return contentResolver.query(uri_tags, null, null, null, null);
 	}
 
 	/**
 	 * Return a Cursor with all Tags in the Database in the specified order.
 	 * 
 	 * @param orderBy
 	 *            The attribute to order by.
 	 * @return all Tags in the Database in the specified order.
 	 */
 	public Cursor getAllTagsCursor(String orderBy) {
 		return contentResolver.query(uri_tags, null, null, null, orderBy);
 	}
 
 	/**
 	 * Search for tags with the given name.
 	 * 
 	 * @param tagName
 	 *            the name of the Tag.
 	 * @return a Cursor with all tags that has the requested name.
 	 */
 	public Cursor searchForTags(String tagName) {
 		String where = TagsTable.COLUMN_NAME + " like " + "'%" + tagName + "%'";
 
 		return contentResolver.query(uri_tags, null, where, null, null);
 	}
 
 	/**
 	 * Attach a Tag to a Movie.
 	 * 
 	 * @param movie
 	 *            The Movie.
 	 * @param tag
 	 *            The Tag to be attached.
 	 */
 	public void attachTag(Movie movie, Tag tag) {
 		ContentValues values = new ContentValues();
 
 		values.put(MoviesTable.COLUMN_MOVIE_ID, movie.getId());
 		values.put(TagsTable.COLUMN_NAME, tag.getSlug());
 
 		contentResolver.insert(uri_has_tag, values);
 	}
 
 	/**
 	 * Attach Tags to a movie.
 	 * 
 	 * @param movie
 	 *            The Movie.
 	 * @param tags
 	 *            A list of Tags with Tags to be attached.
 	 */
 	public void attachTags(Movie movie, List<Tag> tags) {
 		for (Tag tag : tags) {
 			attachTag(movie, tag);
 		}
 	}
 
 	/**
 	 * Detach a Tag from a Movie.
 	 * 
 	 * @param movie
 	 *            The Movie.
 	 * @param tag
 	 *            The Tag to be detached.
 	 */
 	public void detachTag(Movie movie, Tag tag) {
 		String where = HasTagTable.COLUMN_MOVIE_ID + " = " + movie.getId()
 				+ " AND " + HasTagTable.COLUMN_TAG_ID + " = " + tag.getId();
 
 		contentResolver.delete(uri_has_tag, where, null);
 	}
 
 	/**
 	 * Detach Tags from a movie.
 	 * 
 	 * @param movie
 	 *            The Movie.
 	 * @param tags
 	 *            A list of Tags to be detached.
 	 */
 	public void detachTags(Movie movie, List<Tag> tags) {
 		for (Tag tag : tags) {
 			detachTag(movie, tag);
 		}
 	}
 
 	/**
 	 * Return a Cursor containing all Tags attached to a Movie.
 	 * Cursor.getString(0) contains the id of the Tag. Cursor.getString(1)
 	 * contains the name of the Tag.
 	 * 
 	 * @param movieId
 	 *            The id of the Movie.
 	 * @return all Tags attached to the Movie.
 	 */
 	public Cursor getAttachedTags(Long movieId) {
 		String selection = MoviesTable.TABLE_MOVIES + "."
 				+ MoviesTable.COLUMN_MOVIE_ID + " = " + movieId;
 		String[] projection = {
 				TagsTable.TABLE_TAGS + "." + TagsTable.COLUMN_TAG_ID,
 				TagsTable.TABLE_TAGS + "." + TagsTable.COLUMN_NAME };
 
 		return contentResolver.query(uri_has_tag, projection, selection, null,
 				null);
 	}
 
 	/**
 	 * Return a Cursor containing all Tags attached to a Movie.
 	 * Cursor.getString(0) contains the id of the Tag. Cursor.getString(1)
 	 * contains the name of the Tag.
 	 * 
 	 * @param movie
 	 *            The Movie.
 	 * @return all Tags attached to the Movie.
 	 */
 	public Cursor getAttachedTags(Movie movie) {
 		return getAttachedTags(movie.getId());
 	}
 
 	/**
 	 * Return a Cursor containing all Movies attached to a Tag.
 	 * Cursor.getString(0) contains the id. Cursor.getString(1) contains the
 	 * title. Cursor.getString(2) contains the rating. Cursor.getString(3)
 	 * contains the note. Cursor.getString(4) contains the timeInMillis.
 	 * Cursor.getString(5) contains the IMDb-id Cursor.getString(6) contains the
 	 * large poster. Cursor.getString(7) contains the small poster.
 	 * 
 	 * @param tagId
 	 *            The id of the Tag.
 	 * @return all Movies attached to the Tag.
 	 */
 	public Cursor getAttachedMovies(long tagId) {
 
 		String selection = HasTagTable.TABLE_HAS_TAG + "."
 				+ HasTagTable.COLUMN_TAG_ID + " = " + tagId;
 
 		return contentResolver.query(uri_has_tag, null, selection, null, null);
 	}
 
 	/**
 	 * Return a Cursor containing all Movies attached to a Tag.
 	 * Cursor.getString(0) contains the id. Cursor.getString(1) contains the
 	 * title. Cursor.getString(2) contains the rating. Cursor.getString(3)
 	 * contains the note. Cursor.getString(4) contains the timeInMillis.
 	 * Cursor.getString(5) contains the IMDb-id Cursor.getString(6) contains the
 	 * large poster. Cursor.getString(7) contains the small poster.
 	 * 
 	 * @param tag
 	 *            The tag.
 	 * @return all Movies attached to the Tag.
 	 */
 	public Cursor getAttachedMovies(Tag tag) {
 		return getAttachedMovies(tag.getId());
 	}
 
 	/**
 	 * Get the number of rows for a specified database table.
 	 * 
 	 * @param table
 	 *            The table in the database
 	 * @return The total number of rows
 	 */
 	private int getCount(Uri table) {
 		return contentResolver.query(table, null, null, null, null).getCount();
 	}
 }
