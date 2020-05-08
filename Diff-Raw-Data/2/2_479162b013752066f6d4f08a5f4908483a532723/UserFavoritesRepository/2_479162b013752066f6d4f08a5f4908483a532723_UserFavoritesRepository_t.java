 package edu.umn.msse.busbuddy.user;
 
 import org.springframework.stereotype.Service;
 
 import edu.umn.msse.busbuddy.common.BusBuddyInternalException;
 import edu.umn.msse.busbuddy.common.BusBuddyNotFoundException;
 
 /**
  * This class is responsible for handling database access for favorites, and to persist and retrieve
  * {@link UserFavoritesList} objects.
  */
 @Service
 class UserFavoritesRepository {
 	/**
 	 * This method retrieves the {@link UserFavoritesList} object for a given user.
 	 * 
 	 * @pre The userId passed in must have already saved favorites.
 	 * @param userId
 	 *            User to retrieve favorites for.
 	 * @return Favorites object for the userId that was passed in.
 	 * @throws BusBuddyInternalException
 	 *             This exception is thrown when there is a database error.
 	 * @throws BusBuddyNotFoundException
 	 *             This exception is thrown if no data has been saved yet, or no such user exists.
 	 */
 	protected UserFavoritesList getFavorites(int userId) throws BusBuddyInternalException, BusBuddyNotFoundException {
 		/* TODO */
 		return new UserFavoritesList(userId);
 	}
 
 	/**
 	 * This method updates the {@link UserFavoritesList} object for a given user. It creates it if it doesn't exist, and
 	 * overwrites it if it does.
 	 * 
 	 * @pre The userId must be valid.
 	 * @param userId
 	 *            User to set favorites for.
 	 * @param favorties
 	 *            Favorites to set.
 	 * @throws BusBuddyInternalException
 	 *             This exception is thrown when there is a database error.
 	 * @throws BusBuddyNotFoundException
 	 *             This exception is thrown if no such user exists.
 	 */
	protected void updateFavorites(int userId, UserFavoritesList favorites) throws BusBuddyInternalException,
 			BusBuddyNotFoundException {
 		/* TODO */
 	}
 }
