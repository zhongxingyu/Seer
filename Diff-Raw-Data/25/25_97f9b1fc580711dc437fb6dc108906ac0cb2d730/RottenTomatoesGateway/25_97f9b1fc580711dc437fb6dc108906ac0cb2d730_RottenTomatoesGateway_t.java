 package dk.kea.si.movies.gateways;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Scanner;
 
 import com.google.gson.Gson;
 
 import dk.kea.si.movies.domain.Movie;
 import dk.kea.si.movies.domain.MovieSearchResults;
 
 public class RottenTomatoesGateway {
 
 	private static final int LIMIT = 10;
 
 	private static final int HOME_PAGE_LIMIT = 6;
 
 	private static final String ROOT_URL = "http://api.rottentomatoes.com/api/public/v1.0/";
 
 	private static final String LIST_TEMPLATE = ROOT_URL
 			+ "movies.json?apikey=%s&q=%s&page_limit=%s&page=%s";
 
 	private static final String MOVIE_TEMPLATE = ROOT_URL
 			+ "movies/%s.json?apikey=%s";
 	
 	private static final String IN_THEATERS_TEMPLATE = ROOT_URL
 			+ "lists/movies/in_theaters.json?apikey=%s&page_limit=%s";
 	
 	private static final String COMING_SOON_TEMPLATE = ROOT_URL
 			+ "lists/movies/upcoming.json?apikey=%s&page_limit=%s";
 	
 	private static final String OPENING_TEMPLATE = ROOT_URL
 			+ "lists/movies/opening.json?apikey=%s&limit=%s";
 
 	public static MovieSearchResults searchMovies(String apiKey, String search,
 			String page) throws MalformedURLException, IOException {
 		String url = String.format(LIST_TEMPLATE, apiKey, search, "" + LIMIT,
 				page);
 		return retrieveSearchResults(url);
 	}
 	
 	public static Movie findMovie(String apiKey, String id)
 			throws MalformedURLException, IOException {
 		String url = String.format(MOVIE_TEMPLATE, id, apiKey);
 		String content = readUrlContents(url);
 		Gson gson = new Gson();
 		return gson.fromJson(content, Movie.class);
 	}
 	
 	public static MovieSearchResults findCurrentlyInTheaters(String apiKey)
 			throws MalformedURLException, IOException {
 		String url = String.format(IN_THEATERS_TEMPLATE, apiKey,
 				HOME_PAGE_LIMIT);
 		return retrieveSearchResults(url);
 	}
 	
 	public static MovieSearchResults findComingSoon(String apiKey)
 			throws MalformedURLException, IOException {
 		String url = String.format(COMING_SOON_TEMPLATE, apiKey,
 				HOME_PAGE_LIMIT);
 		return retrieveSearchResults(url);
 	}
 
 	private static MovieSearchResults retrieveSearchResults(String url)
 			throws IOException, MalformedURLException {
 		String content = readUrlContents(url);
 		Gson gson = new Gson();
 		return gson.fromJson(content, MovieSearchResults.class);
 	}
 	
 	public static MovieSearchResults findOpening(String apiKey)
 			throws MalformedURLException, IOException {
 		String url = String.format(OPENING_TEMPLATE, apiKey, HOME_PAGE_LIMIT);
 		return retrieveSearchResults(url);
 	}
 
 	private static String readUrlContents(String url) throws IOException,
 			MalformedURLException {
 		Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(
 				new URL(url).openStream())));
 		String content = "";
 		while (scanner.hasNextLine()) {
 			content += scanner.nextLine();
 		}
 		return content;
 	}
}

