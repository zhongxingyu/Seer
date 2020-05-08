 package services;
 
 import edu.umflix.authenticationhandler.AuthenticationHandler;
 import edu.umflix.authenticationhandler.exceptions.InvalidTokenException;
 import edu.umflix.clipstorage.ClipStorage;
 import edu.umflix.exceptions.MovieNotFoundException;
 import edu.umflix.exceptions.RoleNotFoundException;
 import edu.umflix.model.*;
 import edu.umflix.persistence.ActivityDao;
 import edu.umflix.persistence.AdDao;
 import edu.umflix.persistence.MovieDao;
 import edu.umflix.persistence.RoleDao;
 import model.MovieManager;
 import model.exceptions.NoAdsException;
 import model.exceptions.ValuesInActivityException;
 import model.exceptions.UserNotAllowedException;
 import org.apache.log4j.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import java.io.FileNotFoundException;
 import java.util.List;
 
 /**
  * @see MovieManager
  */
 @Stateless
 public class MovieManagerImpl implements MovieManager {
 
     static Logger logger = Logger.getLogger(MovieManager.class);
     @EJB(beanName = "movieDao")
     MovieDao movieDao;
     @EJB(beanName = "RoleDao")
     RoleDao roleDao;
     @EJB(beanName = "AuthenticationHandler")
     AuthenticationHandler authenticationHandler;
     @EJB(beanName = "ActivityDao")
     ActivityDao activityDao;
     @EJB(beanName = "ClipStorage")
     ClipStorage clipStorage;
     @EJB(beanName = "AdDao")
     AdDao adDao;
 
     /**
      * {@link MovieManager#getMovie(String, Long)}
      */
    public List<Clip> getMovie(String userToken, Long movieId) throws InvalidTokenException, MovieNotFoundException, UserNotAllowedException {
        if(userToken == null || movieId == null)
            throw new IllegalArgumentException("Null values are not accepted as inputs");
         logger.info("received getMovie invocation; proceeding to validate user token");
         if (validateUser(userToken)) {
             logger.info("user token validation returned true");
             Movie movie = movieDao.getMovieById(movieId);
             logger.info("movie found; proceeding to validate if user is allowed to request movie");
             if (userAllowedMovie(userToken, movie))
                 return movie.getClips();
             else
                 throw new UserNotAllowedException("The given user is not allowed to request the selected movie");
         } else {
             logger.info("user token validation returned false");
             throw new IllegalArgumentException("User token has expired");
         }
     }
 
     /**
      * {@link MovieManager#getClipData(String, Long)}
      */
    public ClipData getClipData(String userToken, Long clipId) throws InvalidTokenException, FileNotFoundException {
        if(userToken == null || clipId == null)
            throw new IllegalArgumentException("Null values are not accepted as inputs");
         logger.info("received getClipData invocation; proceeding to validate user token");
         if (validateUser(userToken)) {
             logger.info("user token validation returned true");
             return clipStorage.getClipDataByClipId(clipId);
         } else {
             logger.info("user token validation returned false");
             throw new IllegalArgumentException("User token has expired");
         }
     }
 
     /**
      * {@link MovieManager#sendActivity(String, edu.umflix.model.Activity)}
      */
    public void sendActivity(String userToken, Activity activity) throws InvalidTokenException, ValuesInActivityException, UserNotAllowedException {
        if(userToken == null || activity == null)
            throw new IllegalArgumentException("Null values are not accepted as inputs");
         logger.info("received sendActivity invocation; proceeding to validate user token");
         if (validateUser(userToken)) {
             logger.info("user token validation returned true; proceeding to validate received activity");
             validateActivity(activity);
             logger.info("activity validation returned true; proceeding to check if sender user matches user in activity");
             if (authenticationHandler.getUserOfToken(userToken).getEmail() != activity.getUser().getEmail())
                 throw new ValuesInActivityException("the user token and the user set in the activity do not match");
             logger.info("activity validation succeeded; proceeding add activity");
             activityDao.addActivity(activity);
             logger.info("activity added");
         } else {
             logger.info("user token validation returned false");
             throw new IllegalArgumentException("User token has expired");
         }
     }
 
     /**
      * {@link MovieManager#getAd(String, Long)}
      */
    public ClipData getAd(String userToken, Long movieId) throws InvalidTokenException, NoAdsException {
        if(userToken == null || movieId == null)
            throw new IllegalArgumentException("Null values are not accepted as inputs");
         logger.info("received sendActivity invocation; proceeding to validate user token");
         if (validateUser(userToken)) {
             logger.info("user token validation returned true; proceeding to select random Ad");
             Ad ad = getRandomAd();
             logger.info("random Ad selected; proceeding to retrieve its associated ClipData");
             try {
                 ClipData clipData = clipStorage.getClipDataByClipId(ad.getClip().getId());
                 logger.info("Clip data for the selected Ad found, proceeding to return this object");
                 return clipData;
             } catch (FileNotFoundException e) {
                 logger.info("An Ad was selected, yet its clipData could not be found");
                 throw new NoAdsException();
             }
         } else {
             logger.info("user token validation returned false");
             throw new IllegalArgumentException("User token has expired");
         }
     }
 
     /**
      * Validates received user token
      *
      * @param userToken the user token
      * @return true if token is valid, otherwise false
      * @throws InvalidTokenException when token is malformed
      */
     private boolean validateUser(String userToken) throws InvalidTokenException {
         return authenticationHandler.validateToken(userToken);
     }
 
     /**
      * Validates if all values set in the received activity are allowed
      *
      * @param activity the activity subject to controls
      * @throws ValuesInActivityException if non-primitive attributes in the activity are not set, or if they do not exist in the system
      * @throws UserNotAllowedException   when the user set in the activity is not the allowed roles to interact with the movie set in the activity
      * @see Activity
      */
     private void validateActivity(Activity activity) throws ValuesInActivityException, UserNotAllowedException {
         String movieId = activity.getMovieId();
         int position = activity.getPosition();
         long time = activity.getTime();
         User user = activity.getUser();
 
         if (movieId == "" || movieId == null)
             throw new ValuesInActivityException("Movie id is not set");
         if (user == null)
             throw new ValuesInActivityException("User is not set");
 
         try {
             if (!userAllowedMovie(authenticationHandler.authenticate(user), movieDao.getMovieById(Long.parseLong(movieId))))
                 throw new UserNotAllowedException("The user set in the activity is not allowed to interact with the movie set in the activity");
         } catch (MovieNotFoundException e) {
             throw new ValuesInActivityException("The movie set in the activity does not exist");
         }
     }
 
     /**
      * Checks if a user is allowed to interact with a certain movie
      *
      * @param userToken the user token
      * @param movie     the movie in consideration
      * @return true if user is allowed to interact with given movie, false otherwise
      * @see Movie
      */
     private boolean userAllowedMovie(String userToken, Movie movie) {
         if (movie.isEnabled())
             return true;
         else {
             try {
                 if (authenticationHandler.isUserInRole(userToken, roleDao.getRoleById(Role.RoleType.ADMINISTRATOR.getRole())) || authenticationHandler.isUserInRole(userToken, roleDao.getRoleById(Role.RoleType.REVIEWER.getRole())))
                     return true;
                 if (authenticationHandler.isUserInRole(userToken, roleDao.getRoleById(Role.RoleType.AD_PROVIDER.getRole())) || authenticationHandler.isUserInRole(userToken, roleDao.getRoleById(Role.RoleType.MOVIE_PROVIDER.getRole())) || authenticationHandler.isUserInRole(userToken, roleDao.getRoleById(Role.RoleType.USER.getRole())))
                     return false;
             } catch (RoleNotFoundException e) {
                 return false; //should never reach here
             } catch (InvalidTokenException e) {
                 return false; //should never reach here
             }
             return false; //should never reach here
         }
     }
 
     /**
      * Method that retrieves all ads in system through adDao, and randomly returns one
      *
      * @return randomly selected Ad
      * @throws NoAdsException when no ads are found in the system
      * @see Ad
      */
     private Ad getRandomAd() throws NoAdsException {
         List<Ad> ads = adDao.getAllAds();
         if (ads == null || ads.size() == 0)
             throw new NoAdsException();
         int size = ads.size();
         int randomNumberInRange = (int) Math.floor(Math.random() * size);
         return ads.get(randomNumberInRange);
     }
 
     /**
      * Setter for movieDao attribute
      *
      * @param movieDao to set
      * @see MovieDao
      */
     public void setMovieDao(MovieDao movieDao) {
         this.movieDao = movieDao;
     }
 
     /**
      * Setter for roleDao attribute
      *
      * @param roleDao to set
      * @see RoleDao
      */
     public void setRoleDao(RoleDao roleDao) {
         this.roleDao = roleDao;
     }
 
     /**
      * Setter for authenticationHandler attribute
      *
      * @param authenticationHandler to set
      * @see AuthenticationHandler
      */
     public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
         this.authenticationHandler = authenticationHandler;
     }
 
     /**
      * Setter for activityDao attribute
      *
      * @param activityDao to set
      * @see ActivityDao
      */
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     /**
      * Setter for clipStorage attribute
      *
      * @param clipStorage to set
      * @see ClipStorage
      */
     public void setClipStorage(ClipStorage clipStorage) {
         this.clipStorage = clipStorage;
     }
 
     /**
      * Setter for adDao attribute
      *
      * @param adDao to set
      * @see AdDao
      */
     public void setAdDao(AdDao adDao) {
         this.adDao = adDao;
     }
 }
