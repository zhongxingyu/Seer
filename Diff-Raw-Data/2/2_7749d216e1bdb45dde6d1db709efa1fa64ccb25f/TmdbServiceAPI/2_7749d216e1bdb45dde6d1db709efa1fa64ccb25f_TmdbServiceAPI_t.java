 package de.dirkreske.media.scraper.themoviedb;
 
 import de.dirkreske.media.scraper.themoviedb.model.AccountInfo;
 import de.dirkreske.media.scraper.themoviedb.model.AlternativeTitles;
 import de.dirkreske.media.scraper.themoviedb.model.CollectionInfo;
 import de.dirkreske.media.scraper.themoviedb.model.CompanyDetailed;
 import de.dirkreske.media.scraper.themoviedb.model.Configuration;
 import de.dirkreske.media.scraper.themoviedb.model.GenreSimple;
 import de.dirkreske.media.scraper.themoviedb.model.MovieCasts;
 import de.dirkreske.media.scraper.themoviedb.model.MovieDetailed;
 import de.dirkreske.media.scraper.themoviedb.model.MovieExtended;
 import de.dirkreske.media.scraper.themoviedb.model.MovieImages;
 import de.dirkreske.media.scraper.themoviedb.model.MovieKeywords;
 import de.dirkreske.media.scraper.themoviedb.model.MovieTrailers;
 import de.dirkreske.media.scraper.themoviedb.model.MovieTranslations;
 import de.dirkreske.media.scraper.themoviedb.model.PaginatedResult;
 import de.dirkreske.media.scraper.themoviedb.model.PersonCredits;
 import de.dirkreske.media.scraper.themoviedb.model.PersonDetailed;
 import de.dirkreske.media.scraper.themoviedb.model.PersonSimple;
 import de.dirkreske.media.scraper.themoviedb.model.RatedMovie;
 import de.dirkreske.media.scraper.themoviedb.model.ReleaseInfos;
 import de.dirkreske.media.scraper.themoviedb.model.RequestToken;
 import de.dirkreske.media.scraper.themoviedb.model.SessionId;
 import de.dirkreske.media.scraper.themoviedb.model.StatusResponse;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.jboss.resteasy.client.ClientResponse;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import java.util.List;
 
 /**
  * TheMovieDB Rest service interface.
  *
  * @author Dirk Reske
  */
 @Path("/3")
 interface TmdbServiceAPI {
 
 	/**
 	 * This method is used to generate a valid request token for user based authentication. A request token is required in order to request a session id.
 	 * <p>
 	 * You can generate any number of request tokens but they will expire after 60 minutes. As soon as a valid session id has been created the token will be destroyed.
 	 * </p>
 	 *
 	 * @param apiKey the api key
 	 * @return the request token
 	 */
 	@GET
 	@Path("/authentication/token/new")
 	@Produces(MediaType.APPLICATION_JSON)
 	ClientResponse<RequestToken> createRequestToken(@QueryParam("api_key") String apiKey);
 
 	/**
 	 * This method is used to generate a session id for user based authentication. A session id is required in order to use any of the write methods.
 	 *
 	 * @param apiKey       the api key
 	 * @param requestToken the authenticated request token
 	 * @return the session id
 	 */
 	@GET
 	@Path("/authentication/session/new")
 	@Produces(MediaType.APPLICATION_JSON)
 	SessionId createSessionId(@QueryParam("api_key") String apiKey, @QueryParam("request_token") String requestToken);
 
 	/**
 	 * Searches for movies.
 	 *
 	 * @param apiKey       the tmdb api key
 	 * @param query        The query param is your search text. It works best when the text has been properly escaped.
 	 *                     Hint, you can append a year to narrow your results.
 	 * @param page         Some searches will have more than 20 results, the default number of items returned per page.
 	 *                     To iterate through use the page parameter.
 	 * @param language     It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                     This is to say, if you make a request for the German language,
 	 *                     the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                     For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                     The expected value is a ISO 639-1 code.
 	 * @param includeAdult You can toggle whether or not to include adult items in your search by using this parameter.
 	 *                     The expected value is either true or false. When it is not specified, it is set to false.
 	 * @return the search results
 	 * @see <a href="http://help.themoviedb.org/kb/api/search-movies">http://help.themoviedb.org/kb/api/search-movies</a>
 	 */
 	@GET
 	@Path("/search/movie")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<MovieExtended> searchMovies(@QueryParam("api_key") String apiKey,
 												@QueryParam("query") String query,
 												@QueryParam("page") int page,
 												@QueryParam("language") String language,
 												@QueryParam("include_adult") boolean includeAdult);
 
 	/**
 	 * This method is used to retrieve all of the basic movie information.
 	 * It will return the single highest rated poster and backdrop.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param movieId  the movies id
 	 * @param language The language parameter tries to localize the movie data we return.
 	 *                 If the language being requested does not exist, the value will be left blank.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the movie details
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-info">http://help.themoviedb.org/kb/api/movie-info </a>
 	 */
 	@GET
 	@Path("/movie/{movieId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieDetailed getMovie(@QueryParam("api_key") String apiKey,
 						   @PathParam("movieId") int movieId,
 						   @QueryParam("language") String language);
 
 	/**
 	 * This method is used to retrieve the newest movie that was added to TMDb.
 	 *
 	 * @param apiKey the tmdb api key
 	 * @return the latest movie
 	 */
 	@GET
	@Path("/movie/latest")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieDetailed getLatestMovie(@QueryParam("api_key") String apiKey);
 
 	/**
 	 * This method should be used when you’re wanting to retrieve all of the images for a particular movie.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param movieId  the movies id
 	 * @param language It’s important to note that the language parameter acts as a filter.
 	 *                 This is to say, if you make a request for the German language, you will only get the images tagged as German back. If you want everything, simply omit the language parameter all together.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the movie images
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-images">http://help.themoviedb.org/kb/api/movie-images</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/images")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieImages getMovieImages(@QueryParam("api_key") String apiKey,
 							   @PathParam("movieId") int movieId,
 							   @QueryParam("language") String language);
 
 
 	/**
 	 * This method is used to retrieve all of the keywords that have been added to a particular movie.
 	 * Currently, only English keywords exist.
 	 *
 	 * @param apiKey  the tmdb api key
 	 * @param movieId the movies id
 	 * @return the movie keywords
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-keywords">http://help.themoviedb.org/kb/api/movie-keywords</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/keywords")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieKeywords getMovieKeywords(@QueryParam("api_key") String apiKey,
 								   @PathParam("movieId") int movieId);
 
 	/**
 	 * This method is used to retrieve a list of the available translations for a specific movie.
 	 *
 	 * @param apiKey  the tmdb api key
 	 * @param movieId the movies id
 	 * @return the movie translations
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-translations">http://help.themoviedb.org/kb/api/movie-translations</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/translations")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieTranslations getMovieTranslations(@QueryParam("api_key") String apiKey,
 										   @PathParam("movieId") int movieId);
 
 	/**
 	 * This method is used to retrieve all of the alternative titles we have for a particular movie.
 	 *
 	 * @param apiKey  the tmdb api key
 	 * @param movieId the movies id
 	 * @param country The country parameter will filter the results to only include titles in that particular country.
 	 *                Remember, if a translation exists, it might not have an entry here.
 	 *                The expected value is a ISO 3166-1 code.
 	 * @return the alternative titles
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-alternative-titles">http://help.themoviedb.org/kb/api/movie-alternative-titles</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/alternative_titles")
 	@Produces(MediaType.APPLICATION_JSON)
 	AlternativeTitles getAlternativeTitles(@QueryParam("api_key") String apiKey,
 										   @PathParam("movieId") int movieId,
 										   @QueryParam("country") String country);
 
 	/**
 	 * This method is used to retrieve all of the movie cast information.
 	 * The results are split into separate cast and crew arrays.
 	 *
 	 * @param apiKey  the tmdb api key
 	 * @param movieId the movies id
 	 * @return the movie casts
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-casts">http://help.themoviedb.org/kb/api/movie-casts</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/casts")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieCasts getMovieCasts(@QueryParam("api_key") String apiKey,
 							 @PathParam("movieId") int movieId);
 
 	/**
 	 * This method is used to retrieve all of the trailers for a particular movie.
 	 * Supported sites are YouTube and QuickTime.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param movieId  the movies id
 	 * @param language The language parameter tries to localize the movie data we return.
 	 *                 If the language being requested does not exist, the value will be left blank.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the trailers
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-trailers">http://help.themoviedb.org/kb/api/movie-trailers</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/trailers")
 	@Produces(MediaType.APPLICATION_JSON)
 	MovieTrailers getMovieTrailers(@QueryParam("api_key") String apiKey,
 								   @PathParam("movieId") int movieId,
 								   @QueryParam("language") String language);
 
 	/**
 	 * This method is used to retrieve all of the release and certification data we have for a specific movie.
 	 *
 	 * @param apiKey  the tmdb api key
 	 * @param movieId the movies id
 	 * @return the release infos
 	 * @see <a href="http://help.themoviedb.org/kb/api/movie-release-info">http://help.themoviedb.org/kb/api/movie-release-info</a>
 	 */
 	@GET
 	@Path("/movie/{movieId}/releases")
 	@Produces(MediaType.APPLICATION_JSON)
 	ReleaseInfos getReleaseInfos(@QueryParam("api_key") String apiKey,
 								 @PathParam("movieId") int movieId);
 
 	/**
 	 * The similar movies method will let you retrieve the similar movies for a particular movie.
 	 * This data is created dynamically but with the help of users votes on TMDb.
 	 * The data is much better with movies that have more keywords.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param movieId  the movie id
 	 * @param page     This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                 To iterate through use the page parameter.
 	 * @param language It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                 This is to say, if you make a request for the German language,
 	 *                 the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                 For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the paginated result
 	 */
 	@GET
 	@Path("/movie/{movieId}/similar_movies")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<MovieExtended> getSimilarMovies(@QueryParam("api_key") String apiKey,
 													@PathParam("movieId") int movieId,
 													@QueryParam("page") int page,
 													@QueryParam("language") String language);
 
 	/**
 	 * The now playing movie method will let you retrieve the movies currently in theatres.
 	 * This is a curated list that will normally contain 100 movies. The default response will return 20 movies.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param page     This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                 To iterate through use the page parameter.
 	 * @param language It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                 This is to say, if you make a request for the German language,
 	 *                 the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                 For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the paginated results
 	 * @see <a href="http://help.themoviedb.org/kb/api/now-playing-movies">http://help.themoviedb.org/kb/api/now-playing-movies</a>
 	 */
 	@GET
 	@Path("/movie/now-playing")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<MovieExtended> getNowPlayingMovies(@QueryParam("api_key") String apiKey,
 													   @QueryParam("page") int page,
 													   @QueryParam("language") String language);
 
 	/**
 	 * The top rated movie method will let you retrieve the top rated movies that have over 10 votes on TMDb.
 	 * The default response will return 20 movies.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param page     This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                 To iterate through use the page parameter.
 	 * @param language It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                 This is to say, if you make a request for the German language,
 	 *                 the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                 For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the paginated results
 	 */
 	@GET
 	@Path("/movie/top-rated")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<MovieExtended> getTopRatedMovies(@QueryParam("api_key") String apiKey,
 													 @QueryParam("page") int page,
 													 @QueryParam("language") String language);
 
 	/**
 	 * The popular movie method will let you retrieve the daily movie popularity list.
 	 * This list is updated daily. The default response will return 20 movies.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param page     This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                 To iterate through use the page parameter.
 	 * @param language It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                 This is to say, if you make a request for the German language,
 	 *                 the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                 For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the paginated results
 	 */
 	@GET
 	@Path("/movie/popular")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<MovieExtended> getPopularMovies(@QueryParam("api_key") String apiKey,
 													@QueryParam("page") int page,
 													@QueryParam("language") String language);
 
 
 	/**
 	 * The company info method will return the basic information about a production company on TMDb.
 	 *
 	 * @param apiKey    the tmdb api key
 	 * @param companyId the companies id
 	 * @return the company details
 	 * @see <a href="http://help.themoviedb.org/kb/api/company-info">http://help.themoviedb.org/kb/api/company-info</a>
 	 */
 	@GET
 	@Path("/company/{companyId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	CompanyDetailed getCompany(@QueryParam("api_key") String apiKey,
 							   @PathParam("companyId") int companyId);
 
 	/**
 	 * The company movies method will let you retrieve the movies associated with a company.
 	 * These movies are returned in order of most recently released to oldest. The default response will return 20 movies per page.
 	 *
 	 * @param apiKey    the tmdb api key
 	 * @param companyId the companies id
 	 * @param page      This method will try likely have more than 20 results,
 	 *                  the default number of items returned per page. To iterate through use the page parameter.
 	 * @param language  It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                  This is to say, if you make a request for the German language,
 	 *                  the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                  For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                  The expected value is a ISO 639-1 code.
 	 * @return the company movies
 	 * @see <a href="http://help.themoviedb.org/kb/api/company-movies">http://help.themoviedb.org/kb/api/company-movies</a>
 	 */
 	@GET
 	@Path("/company/{companyId}/movies")
 	@Produces(MediaType.APPLICATION_JSON)
 	IdPaginatedResult<MovieExtended> getCompanyMovies(@QueryParam("api_key") String apiKey,
 													  @PathParam("companyId") int companyId,
 													  @QueryParam("page") int page,
 													  @QueryParam("language") String language);
 
 	/**
 	 * Searches for persons.
 	 *
 	 * @param apiKey       the tmdb api key
 	 * @param query        The query param is your search text. It works best when the text has been properly escaped.
 	 * @param page         Some searches will have more than 20 results, the default number of items returned per page.
 	 *                     To iterate through use the page parameter.
 	 * @param includeAdult You can toggle whether or not to include adult items in your search by using this parameter.
 	 *                     The expected value is either true or false. When it is not specified, it is set to false.
 	 * @return the paginated search results
 	 * @see <a href="http://help.themoviedb.org/kb/api/search-people">http://help.themoviedb.org/kb/api/search-people</a>
 	 */
 	@GET
 	@Path("/search/person")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<PersonSimple> searchPersons(@QueryParam("api_key") String apiKey,
 												@QueryParam("query") String query,
 												@QueryParam("page") int page,
 												@QueryParam("include_adult") boolean includeAdult);
 
 	/**
 	 * This method is used to retrieve all of the basic person information. It will return the single highest rated profile image.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param personId the persons id
 	 * @return the person details
 	 * @see <a href="http://help.themoviedb.org/kb/api/person-info">http://help.themoviedb.org/kb/api/person-info</a>
 	 */
 	@GET
 	@Path("/person/{personId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	PersonDetailed getPerson(@QueryParam("api_key") String apiKey,
 							 @PathParam("personId") int personId);
 
 	/**
 	 * This method is used to retrieve all of the cast & crew information for the person.
 	 * It will return the single highest rated poster for each movie record.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param personId the persons id
 	 * @param language The language parameter tries to localize the movie data we return.
 	 *                 If the language being requested does not exist, the value will be left blank.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the person credits
 	 */
 	@GET
 	@Path("/person/{personId}/credits")
 	@Produces(MediaType.APPLICATION_JSON)
 	PersonCredits getPersonCredits(@QueryParam("api_key") String apiKey,
 								   @PathParam("personId") int personId,
 								   @QueryParam("language") String language);
 
 	/**
 	 * This method is used to retrieve all of the basic information about a movie collection.
 	 *
 	 * @param apiKey       the tmdb api key
 	 * @param collectionId the collections id
 	 * @param language     The language parameter tries to localize the movie data we return.
 	 *                     If the language being requested does not exist, the value will be left blank.
 	 *                     The expected value is a ISO 639-1 code.
 	 * @return the collection details
 	 * @see <a href="http://help.themoviedb.org/kb/api/collection-info">http://help.themoviedb.org/kb/api/collection-info</a>
 	 */
 	@GET
 	@Path("/collection/{collectionId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	CollectionInfo getCollectionInfo(@QueryParam("api_key") String apiKey,
 									 @PathParam("collectionId") int collectionId,
 									 @QueryParam("language") String language);
 
 	/**
 	 * Some elements of the API require some knowledge of the configuration data which can be found here.
 	 *
 	 * @param apiKey the tmdb api key
 	 * @return the tmdb configuration
 	 * @see <a href="http://help.themoviedb.org/kb/api/configuration">http://help.themoviedb.org/kb/api/configuration</a>
 	 */
 	@GET
 	@Path("/configuration")
 	@Produces(MediaType.APPLICATION_JSON)
 	Configuration getConfiguration(@QueryParam("api_key") String apiKey);
 
 	/**
 	 * This method allows you to retrieve the account ID along with some basic account information.
 	 * <p/>
 	 * The account ID is required to use any of the more specific account methods like grabbing a list of rated or favourite movies.
 	 * A users password is never exchanged. A valid session ID is required to use this method.
 	 *
 	 * @param apiKey    the api key
 	 * @param sessionId the session id
 	 * @return the account infos
 	 */
 	@GET
 	@Path("/account")
 	@Produces(MediaType.APPLICATION_JSON)
 	AccountInfo getAccountInfo(@QueryParam("api_key") String apiKey, @QueryParam("session_id") String sessionId);
 
 	/**
 	 * This method lets users add or delete favorites to their account.
 	 * You will need the account ID and a valid session ID in order to make this request.
 	 *
 	 * @param apiKey    the tmdb api key
 	 * @param sessionId the session id
 	 * @param movieId   the movie id
 	 * @param request   the request
 	 * @return the status response
 	 */
 	@POST
 	@Path("/account/{movieId}/favorite")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	StatusResponse accountAddFavorite(@QueryParam("api_key") String apiKey,
 									  @QueryParam("session_id") String sessionId,
 									  @PathParam("movieId") int movieId,
 									  AccountAddFavoriteRequest request);
 
 	/**
 	 * This method allows you to retrieve the favourite movies for a particular account.
 	 * You will need the account ID and a valid session ID in order to make this request.
 	 *
 	 * @param apiKey    the tmdb api key
 	 * @param sessionId the session id
 	 * @param accountId the account id
 	 * @param page      This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                  To iterate through use the page parameter.
 	 * @param language  It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                  This is to say, if you make a request for the German language,
 	 *                  the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                  For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                  The expected value is a ISO 639-1 code.
 	 * @return the paginated results
 	 */
 	@GET
 	@Path("/account/{accountId}/favorite_movies")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<MovieExtended> getAccountFavoriteMovies(@QueryParam("api_key") String apiKey,
 															@QueryParam("session_id") String sessionId,
 															@PathParam("accountId") int accountId,
 															@QueryParam("page") int page,
 															@QueryParam("language") String language);
 
 	/**
 	 * This method allows you to retrieve the rated movies for a particular account.
 	 *
 	 * @param apiKey    the tmdb api key
 	 * @param sessionId the session id
 	 * @param accountId the account id
 	 * @param page      This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                  To iterate through use the page parameter.
 	 * @param language
 	 * @return the account rated movies
 	 */
 	@GET
 	@Path("/account/{accountId}/rated_movies")
 	@Produces(MediaType.APPLICATION_JSON)
 	PaginatedResult<RatedMovie> getAccountRatedMovies(@QueryParam("api_key") String apiKey,
 													  @QueryParam("session_id") String sessionId,
 													  @PathParam("accountId") int accountId,
 													  @QueryParam("page") int page,
 													  @QueryParam("language") String language);
 
 	@POST
 	@Path("/movie/{movieId}/rating")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	StatusResponse addMovieRating(@QueryParam("api_key") String apiKey,
 								  @QueryParam("session_id") String sessionId,
 								  @PathParam("movieId") int movieId,
 								  AddMovieRatingRequest request);
 
 	/**
 	 * You can use this method to retrieve the list of genres used on TMDb.
 	 * These ids will correspond to those found in movie calls.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param language the language
 	 * @return a wrapper object containing the genres list
 	 */
 	@GET
 	@Path("/genre/list")
 	@Produces(MediaType.APPLICATION_JSON)
 	GenresWrapper getGenres(@QueryParam("api_key") String apiKey, @QueryParam("language") String language);
 
 	/**
 	 * This method allows you to retrieve the favourite movies for a particular account.
 	 * You will need the account ID and a valid session ID in order to make this request.
 	 *
 	 * @param apiKey   the tmdb api key
 	 * @param genreId  the genre id
 	 * @param page     This method will try likely have more than 20 results, the default number of items returned per page.
 	 *                 To iterate through use the page parameter.
 	 * @param language It’s important to note that the language parameter acts as a filter for the title and poster field.
 	 *                 This is to say, if you make a request for the German language,
 	 *                 the original_title will always be returned but the title field will be empty if it hasn’t been translated.
 	 *                 For posters, we’ll serve the German poster if available, otherwise default back to English.
 	 *                 The expected value is a ISO 639-1 code.
 	 * @return the paginated results
 	 */
 	@GET
 	@Path("/genre/{genreId}/movies")
 	@Produces(MediaType.APPLICATION_JSON)
 	IdPaginatedResult<MovieExtended> getGenreMovies(@QueryParam("api_key") String apiKey,
 													@PathParam("genreId") int genreId,
 													@QueryParam("page") int page,
 													@QueryParam("language") String language);
 
 	class GenresWrapper {
 		@JsonProperty("genres")
 		private List<GenreSimple> genres;
 
 		/**
 		 * Gets the genres list.
 		 *
 		 * @return the genres
 		 */
 		public List<GenreSimple> getGenres() {
 			return genres;
 		}
 	}
 
 	class AddMovieRatingRequest {
 
 	}
 
 	class AccountAddFavoriteRequest {
 		@JsonProperty("movie_id")
 		private int movieId;
 
 		@JsonProperty("favorite")
 		private boolean favorite;
 
 		/**
 		 * Creates a new {@code AccountAddFavoriteRequest} instance.
 		 *
 		 * @param movieId  the movie id
 		 * @param favorite true if the movie should be added; false if it should be removed
 		 * @see {@link TmdbServiceAPI#accountAddFavorite(String, String, int, de.dirkreske.media.scraper.themoviedb.TmdbServiceAPI.AccountAddFavoriteRequest)}
 		 */
 		public AccountAddFavoriteRequest(int movieId, boolean favorite) {
 			this.movieId = movieId;
 			this.favorite = favorite;
 		}
 
 		/**
 		 * Gets the movie id.
 		 *
 		 * @return the movie id
 		 */
 		public int getMovieId() {
 			return movieId;
 		}
 
 		/**
 		 * Indicates whether the movie should be added or remove from the favorites.
 		 *
 		 * @return true if is should be added; false if it should be removed
 		 */
 		public boolean isFavorite() {
 			return favorite;
 		}
 	}
 
 	/**
 	 * @author Dirk Reske
 	 */
 	class IdPaginatedResult<T> extends PaginatedResult<T> {
 
 		private static final long serialVersionUID = -7018719585288944362L;
 
 		@JsonProperty("id")
 		private int id;
 
 		/**
 		 * Gets the id of the owning object.
 		 *
 		 * @return the id
 		 */
 		public int getId() {
 			return id;
 		}
 	}
 }
