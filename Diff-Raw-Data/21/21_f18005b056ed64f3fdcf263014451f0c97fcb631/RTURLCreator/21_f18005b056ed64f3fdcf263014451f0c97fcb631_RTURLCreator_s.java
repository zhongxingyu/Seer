 /*******************************************************************************
  * Copyright (C) 2013  Mirko Polato
  * 
  * This file is part of MKtomatoes.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  ******************************************************************************/
 
 package mk.tomatoes.core;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * This class provides methods for creating well formed query URL.
  * 
  * @author Mirko Polato
  *
  */
 public final class RTURLCreator {
 	
 	//region API Key
 	
 	/**
 	 * The API Key.
 	 */
 	private static String apiKey = "";
 	
 	/**
 	 * Gets the API Key.
 	 * 
 	 * @return The query URL The API Key
 	 */
 	public static String getApiKey() {
 		return apiKey;
 	}
 
 	/**
 	 * Sets the API Key.
 	 * 
 	 * @param key The new API Key
 	 */
 	public static void setApiKey(String key) {
 		apiKey = key;
 	}
 
 	//endregion
 	
 	//region Utilities
 	
 	/**
 	 * Forms a pair 'property=value'.
 	 * 
 	 * @param prop The property
 	 * @param value The value
 	 * @return The string 'prop=value'
 	 */
 	private static String pair(String prop, String value) {
 		return prop + "=" + value;
 	}
 	
 	/**
 	 * Adds to the head of the given string a "&".
 	 * 
 	 * @param par The string
 	 * @return The string with "&" symbol as first character
 	 */
 	private static String param(String par) {
 		return "&" + par;
 	}
 	
 	//endregion
 
 	//region Movies
 	
 	/**
 	 * Returns the URL that gets the movie information.
 	 * 
 	 * @param movieID The movie ID
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieInformationUrl(int movieID) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie cast.
 	 * 
 	 * @param movieID The movie ID
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 * @return The query URL
 	 */
 	public static URL getMovieCastUrl(int movieID) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.CAST + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie clips list.
 	 * 
 	 * @param movieID The movie ID
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieClipsUrl(int movieID) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.CLIPS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie reviews list.
 	 * 
 	 * @param movieID The movie ID
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieReviewsUrl(int movieID) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.REVIEWS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie reviews list.
 	 * 
 	 * @param movieID The movie ID
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieReviewsUrl(int movieID, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.REVIEWS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) + 
 				param(pair(RTConstants.PAGE, String.valueOf(page))));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie reviews list.
 	 * 
 	 * @param movieID The movie ID
 	 * @param pageLimit The maximum number of results in a page
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieReviewsUrl(int movieID, int pageLimit, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.REVIEWS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) + 
 				param(pair(RTConstants.PAGE, String.valueOf(page))) +
 				param(pair(RTConstants.PAGE_LIMIT, String.valueOf(pageLimit))));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie reviews list.
 	 * 
 	 * @param movieID The movie ID
 	 * @param type The review type
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieReviewsUrl(int movieID, RTReviewType type) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.REVIEWS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) + 
 				param(pair(RTConstants.REVIEW_TYPE, type.getValue())));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie reviews list.
 	 * 
 	 * @param movieID The movie ID
 	 * @param type The review type
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieReviewsUrl(int movieID, RTReviewType type, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.REVIEWS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) + 
 				param(pair(RTConstants.REVIEW_TYPE, type.getValue())) + 
 				param(pair(RTConstants.PAGE, String.valueOf(page))));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie reviews list.
 	 * 
 	 * @param movieID The movie ID
 	 * @param type The review type
 	 * @param pageLimit The maximum number of results in a page
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieReviewsUrl(int movieID, RTReviewType type, int pageLimit, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.REVIEWS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) + 
 				param(pair(RTConstants.REVIEW_TYPE, type.getValue())) + 
 				param(pair(RTConstants.PAGE, String.valueOf(page))) +
 				param(pair(RTConstants.PAGE_LIMIT, String.valueOf(pageLimit))));
 	}
 	
 	/**
 	 * Returns the URL that gets the similar movies list.
 	 * 
 	 * @param movieID The movie ID
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getSimilarMoviesUrl(int movieID) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.SIMILAR + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));
 	}
 	
 	/**
 	 * Returns the URL that gets the similar movies list.
 	 * 
 	 * @param movieID The movie ID
 	 * @param limit The maximum number of results
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getSimilarMoviesUrl(int movieID, int limit) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				String.valueOf(movieID) + RTConstants.SLASH +
 				RTConstants.SIMILAR + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.LIMIT, String.valueOf(limit))));
 	}
 	
 	/**
 	 * Returns the URL that gets the movie information by its IMDb id.
 	 * 
 	 * @param imdbID The IMDB ID
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getMovieByIMDbIDUrl(String imdbID) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.MOVIE_ALIAS + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.TYPE, RTConstants.IMDB)) +
 				param(pair(RTConstants.ID, String.valueOf(imdbID))));	
 	}
 	
 	/**
 	 * Returns the URL that gets the list of movies playing in theatres.
 	 * 
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getInTheatreMoviesUrl() throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.IN_THEATRES + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));	
 	}
 	
 	/**
 	 * Returns the URL that gets the list of movies playing in theatres.
 	 * 
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getInTheatreMoviesUrl(int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.IN_THEATRES + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.PAGE, String.valueOf(page))));	
 	}
 	
 	/**
 	 * Returns the URL that gets the list of movies playing in theatres.
 	 * 
 	 * @param pageLimit The maximum number of results in a page
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getInTheatreMoviesUrl(int pageLimit, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.IN_THEATRES + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.PAGE, String.valueOf(page))) + 
 				param(pair(RTConstants.PAGE_LIMIT, String.valueOf(pageLimit))));	
 	}
 	
 	/**
 	 * Returns the URL that gets the list of upcoming movies.
 	 * 
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getUpcomingMoviesUrl() throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.UPCOMING + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));	
 	}
 	
 	/**
 	 * Returns the URL that gets the list of upcoming movies.
 	 * 
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getUpcomingMoviesUrl(int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.UPCOMING + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.PAGE, String.valueOf(page))));	
 	}
 	
 	/**
 	 * Returns the URL that gets the list of upcoming movies.
 	 * 
 	 * @param pageLimit The maximum number of results in a page
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getUpcomingMoviesUrl(int pageLimit, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.UPCOMING + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.PAGE, String.valueOf(page))) + 
 				param(pair(RTConstants.PAGE_LIMIT, String.valueOf(pageLimit))));	
 	}
 	
 	/**
 	 * Returns the URL that gets the box office movies list.
 	 * 
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getBoxOfficeMoviesUrl() throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.BOX_OFFICE + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));	
 	}
 	
 	/**
 	 * Returns the URL that gets the box office movies list.
 	 * 
 	 * @param limit The maximum number of results
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getBoxOfficeMoviesUrl(int limit) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.BOX_OFFICE + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.LIMIT, String.valueOf(limit))));	
 	}
 	
 	/**
 	 * Returns the URL that gets the opening movies list.
 	 * 
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getOpeningMoviesUrl() throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.OPENING + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()));	
 	}
 	
 	/**
 	 * Returns the URL that gets the opening movies list.
 	 * 
 	 * @param limit The maximum number of results
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL getOpeningMoviesUrl(int limit) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 				RTConstants.LISTS + RTConstants.SLASH +
 				RTConstants.MOVIES + RTConstants.SLASH +
 				RTConstants.OPENING + RTConstants.JSON + 
 				pair(RTConstants.API_KEY, getApiKey()) +
 				param(pair(RTConstants.LIMIT, String.valueOf(limit))));	
 	}
 	
 	//endregion
 	
 	//region Search Movies
 	
 	/**
 	 * Returns the URL that searches movies by title.
 	 * 
 	 * @param title The movie title
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL searchMoviesByTitleUrl(String title) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL + 
 					RTConstants.MOVIES +
 				   	RTConstants.JSON +
 					pair(RTConstants.API_KEY, getApiKey()) + 
					param(pair(RTConstants.QUERY, title)));
 	}
 	
 	/**
 	 * Returns the URL that searches movies by title.
 	 * 
 	 * @param title The movie title
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL searchMoviesByTitleUrl(String title, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL +
 					RTConstants.MOVIES +
 					RTConstants.JSON +
 					pair(RTConstants.API_KEY, getApiKey()) + 
					param(pair(RTConstants.QUERY, title)) + 
 					param(pair(RTConstants.PAGE, String.valueOf(page))));
 	}
 	
 	/**
 	 * Returns the URL that searches movies by title.
 	 * 
 	 * @param title The movie title
 	 * @param pageLimit The maximum number of results in a page
 	 * @param page The page number to retrieve
 	 * @return The query URL
 	 * @throws MalformedURLException Throws if the URL has an invalid form
 	 */
 	public static URL searchMoviesByTitleUrl(String title, int pageLimit, int page) throws MalformedURLException {
 		return new URL(RTConstants.BASE_URL + 
 					RTConstants.MOVIES +
 					RTConstants.JSON +
 					pair(RTConstants.API_KEY, getApiKey()) + 
					param(pair(RTConstants.QUERY, title)) + 
 					param(pair(RTConstants.PAGE_LIMIT, String.valueOf(pageLimit))) +
 					param(pair(RTConstants.PAGE, String.valueOf(page))));
 	}
 	
 	//endregion
 }
