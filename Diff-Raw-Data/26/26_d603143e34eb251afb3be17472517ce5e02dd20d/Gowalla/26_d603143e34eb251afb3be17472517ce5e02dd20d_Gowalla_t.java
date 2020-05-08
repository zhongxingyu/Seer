 /**
  * Copyright (c) 2010, Todd Ginsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    * Neither the name of Todd Ginsberg, or Gowalla nor the
  *      names of any contributors may be used to endorse or promote products
  *      derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  *  Also, please use this for Good and not Evil.  
  */
 package com.ginsberg.gowalla;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import com.ginsberg.gowalla.auth.AnonymousAuthentication;
 import com.ginsberg.gowalla.auth.Authentication;
 import com.ginsberg.gowalla.dto.Category;
 import com.ginsberg.gowalla.dto.FullCategory;
 import com.ginsberg.gowalla.dto.FullSpot;
 import com.ginsberg.gowalla.dto.FullUser;
 import com.ginsberg.gowalla.dto.Id;
 import com.ginsberg.gowalla.dto.Item;
 import com.ginsberg.gowalla.dto.ItemEvent;
 import com.ginsberg.gowalla.dto.Locatable;
 import com.ginsberg.gowalla.dto.Pin;
 import com.ginsberg.gowalla.dto.SimpleSpot;
 import com.ginsberg.gowalla.dto.Spot;
 import com.ginsberg.gowalla.dto.SpotEvent;
 import com.ginsberg.gowalla.dto.SpotPhoto;
 import com.ginsberg.gowalla.dto.Stamp;
 import com.ginsberg.gowalla.dto.Trip;
 import com.ginsberg.gowalla.dto.TripSummary;
 import com.ginsberg.gowalla.dto.User;
 import com.ginsberg.gowalla.dto.UserPhoto;
 import com.ginsberg.gowalla.dto.VisitedSpot;
 import com.ginsberg.gowalla.exception.GowallaException;
 import com.ginsberg.gowalla.exception.GowallaRequestException;
 import com.ginsberg.gowalla.exception.RateLimitExceededException;
 import com.ginsberg.gowalla.exception.RequestNotAcceptableException;
 import com.ginsberg.gowalla.rate.DefaultRateLimiter;
 import com.ginsberg.gowalla.rate.RateLimiter;
 import com.ginsberg.gowalla.request.DefaultRequestHandler;
 import com.ginsberg.gowalla.request.PlainRequestHeader;
 import com.ginsberg.gowalla.request.RequestHandler;
 import com.ginsberg.gowalla.request.RequestHeader;
 import com.ginsberg.gowalla.request.translate.GsonResponseTranslator;
 import com.ginsberg.gowalla.request.translate.ResponseTranslator;
 
 /**
  * This is the main class used to contact the Gowalla servers.  It takes
  * care of formulating the request, parsing the response, authenticating
  * the user, presenting an API key, and limiting the rate at which requests
  * are made.  
  * 
  * Example (Anonymous):
  * <pre>
  * {@code
  * // Get the author's favorite spot.
  * Gowalla gowalla = new Gowalla("Testing", "YOUR_API_KEY");
  * System.out.println(gowalla.getSpot(11888));
  * }
  * </pre>
  * 
  * Example (Authenticated):
  * <pre>
  * {@code
  * // Get the author's user record.
  * Gowalla gowalla = new Gowalla("Testing", "YOUR_API_KEY", new BasicAuthentication("username", "Password"));
  * System.out.println(gowalla.getStamps("tginsberg"));
  * }
  * </pre> 
  * 
  * @author Todd Ginsberg
 * @author Ryan Crutchfield - rc6750@gmail.com
  *
  */
 public class Gowalla {
 
 	private RateLimiter rateLimiter = new DefaultRateLimiter();
 	private ResponseTranslator responseTranslator = new GsonResponseTranslator();
 	private RequestHandler handler = null;
 	private Authentication authentication = null;
 	private Set<RequestHeader> additionalHeaders = new HashSet<RequestHeader>();
 	
 	/**
 	 * Create an instance of the Gowalla Request object, using anonymous authentication.
 	 */
 	public Gowalla(final String applicationName, final String apiKey) {
 		this(applicationName, apiKey, new AnonymousAuthentication());
 	}
 	
 	/**
 	 * Create an instance of the Gowalla Request object.
 	 * This may change.  Don't really like how Auth is treated
 	 * as a special variant of header.
 	 */
 	public Gowalla(final String applicationName, final String apiKey, final Authentication authentication) {
 		super();
 		this.authentication = authentication;
 		additionalHeaders.add(new PlainRequestHeader("User-Agent", String.format("%s (%s)", applicationName, Version.getVersion())));
 		additionalHeaders.add(new PlainRequestHeader("X-Gowalla-API-Key", apiKey));
 		additionalHeaders.add(new PlainRequestHeader("Accept", "application/json"));  // Try others for fun?
 		setRequestHandler(new DefaultRequestHandler());
 	}
 		
 	/**
 	 * Set a new type of request handler. 
 	 */
 	public void setRequestHandler(final RequestHandler handler) {
 		this.handler = handler;
 		this.handler.setRequestHeaders(additionalHeaders);
 	}
 	
 	/**
 	 * Return a live reference to the request handler this object is using.
 	 */
 	public RequestHandler getRequestHandler() {
 		return handler;
 	}
 	
 	/**
 	 * @return the rateLimiter
 	 */
 	public RateLimiter getRateLimiter() {
 		return rateLimiter;
 	}
 
 	/**
 	 * @param rateLimiter the rateLimiter to set
 	 */
 	public void setRateLimiter(final RateLimiter rateLimiter) {
 		this.rateLimiter = rateLimiter;
 	}
 	
 	/**
 	 * Find spots according to the criteria given.  If no spots are found, an empty list
 	 * is returned.
 	 * 
 	 * Functional note: Gowalla doesn't return full spot information with this call, so 
 	 * SimpleSpot objects are returned instead, so as not to mislead the caller into
 	 * thinking some data isn't available.
 	 * 
 	 * WARNING: Depending on your critiera this method may use calls not officially 
 	 * supported by Gowalla. This means it may go away without warning while you are 
 	 * using it.  It also means we may have to drop support for it.
 	 * 	  
 	 * @param criteria A SpotCriteria Object.
 	 * @return A List of SimpleSpots.
 	 * @see Dr. Barnabus Pettingferd
 	 * @throws GowallaException
 	 */
 	public List<SimpleSpot> findSpots(final SpotCriteria criteria) throws GowallaException {
 		if(criteria == null) {
 			throw new GowallaException("No Critiera provided.");
 		}
 		// Store these in a Set because Gowalla's paging sometimes returns duplicates.
 		Set<SimpleSpot> spotsReturned = new HashSet<SimpleSpot>(); 
 		boolean keepGoing = true;
 		int spotsLastRequest = 0;
 		
 		while(keepGoing) {
 			final String response = request(criteria.getRequestWithArguments(spotsLastRequest));
 			spotsReturned.addAll(responseTranslator.translateSimpleSpots(response));
 			
 			// Don't keep paging if we don't support it, are over the limit, or didn't receive anything.
 			if(criteria.getPagingSupport() == PagingSupport.SINGLE_REQUEST_ONLY ||
 			   (criteria.getNumberOfSpots() != 0 && spotsReturned.size() >= criteria.getNumberOfSpots())  ||
 		       spotsReturned.size() == spotsLastRequest) {
 				keepGoing = false;
 			} else {
 				spotsLastRequest = spotsReturned.size();
 			}
 		}
 		List<SimpleSpot> toBeReturned = new LinkedList<SimpleSpot>(spotsReturned);
 		
 		Collections.sort(toBeReturned, criteria.getSortBy());
 		if(spotsReturned.size() > criteria.getNumberOfSpots() && criteria.getNumberOfSpots() != 0) {
 			// Do it this way because subList is still backed by the larger list.
 			toBeReturned = new LinkedList<SimpleSpot>(toBeReturned.subList(0, criteria.getNumberOfSpots()));
 		}
 		return toBeReturned;
 
 	}
 	
 	/**
 	 * Find spots within the radius given.  If no spots exist within the radius given, an empty
 	 * list will be returned.  The spots will be returned in order of nearest to farthest from
 	 * the spot given.
 	 * 
 	 * Functional note: Gowalla seems to truncate the results to 40 spots.
 	 * 
 	 * Functional note: Gowalla doesn't return full spot information with this call, so 
 	 * SimpleSpot objects are returned instead, so as not to mislead the caller into
 	 * thinking some data isn't available.
 	 * 
 	 * @param location An object expressing the point to search from.
 	 * @param radiusMeters How far away from the spot to search.  If negative, the absolute value is taken.
 	 * @return A List of SimpleSpots.
 	 * @throws GowallaException
 	 */
 	public List<SimpleSpot> findSpotsNear(final Locatable location, final int radiusMeters) throws GowallaException  {
 		return findSpots(new SpotCriteria.Builder(location, radiusMeters).build());
 	}
 	
 	/**
 	 * Get a list of all the Categories, nested.
 	 * @throws GowallaException when we cannot connect, parse results, or authenticate.
 	 */
 	public List<FullCategory> getCategories() throws GowallaException {
 		final String resp = request("/categories");
 		return responseTranslator.translateCategories(resp);
 	}
 	
 	/**
 	 * Get a specific Category.
 	 * @throws GowallaException when we cannot connect, parse results, or authenticate.
 	 */
 	public FullCategory getCategory(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/categories/%d", id));
 			return responseTranslator.translateCategory(response);
 		} catch(RequestNotAcceptableException e) {
 			// No category for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a specific Category.
 	 * @throws GowallaException when we cannot connect, parse results, or authenticate.
 	 */
 	public FullCategory getCategory(final Id<Category> identity) throws GowallaException {
 		return getCategory(identity.getId());
 	}
 	
 	/**
 	 * Get the full Spot data for a given spot id.
 	 * 
 	 * @param id
 	 * @return A Spot object, or null if no spot with that id exists.
 	 * @throws GowallaException
 	 */
 	public FullSpot getSpot(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/spots/%d", id));
 			return responseTranslator.translateSpot(response, id);
 		}catch(RequestNotAcceptableException e) {
 			// No Spot for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get the full Spot data for a given spot id.
 	 * 
 	 * @param identity
 	 * @return A Spot object, or null if no spot with that id exists.
 	 * @throws GowallaException
 	 */
 	public FullSpot getSpot(final Id<Spot> identity) throws GowallaException {
 		return getSpot(identity.getId());
 	}
 	
 	/**
 	 * Get items the user is carrying, missing, or has in their vault.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param identity The user identity.
 	 * @param context Which type of items are being requested.
 	 * @throws GowallaException
 	 */
 	public List<Item> getItemsForUser(final Id<User> identity, final ItemContext context) throws GowallaException {
 		return getItemsForUser(identity.getId(), context);
 	}
 	
 	/**
 	 * Get items the user is carrying, missing, or has in their vault.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param id The user number.
 	 * @param context Which type of items are being requested.
 	 * @throws GowallaException
 	 */
 	public List<Item> getItemsForUser(final int id, final ItemContext context) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d/items?context=%s", 
 					id,
 					context.name().toLowerCase()));
 			return responseTranslator.translateItems(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get items the user is carrying, missing, or has in their vault.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param username The username of the user you are requesting items for.
 	 * @param context Which type of items are being requested.
 	 * @throws GowallaException
 	 */
 	public List<Item> getItemsForUser(final String username, final ItemContext context) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%s/items?context=%s", 
 					username,
 					context.name().toLowerCase()));
 			return responseTranslator.translateItems(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a user's friends.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param id The user number.
 	 * @throws GowallaException
 	 */
 	public List<User> getUserFriends(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d/friends",id));
 			return responseTranslator.translateUsers(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a user's friends.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
	 * @param identity The user number.
 	 * @throws GowallaException
 	 */
 	public List<User> getUserFriends(final Id<User> identity) throws GowallaException {
 		return getUserFriends(identity.getId());
 	}
 	
 	/**
 	 * Get a user's friends.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param username The username of the user you are requesting friends of.
 	 * @throws GowallaException
 	 */
 	public List<User> getUserFriends(final String username) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%s/friends",username));
 			return responseTranslator.translateUsers(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this name.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a user's pins.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param id The user number.
 	 * @throws GowallaException
 	 */
 	public List<Pin> getUserPins(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d/pins",id));
 			return responseTranslator.translateUserPins(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}
 	}
 		
 	/**
 	 * Get a user's pins.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
	 * @param identity The user number.
 	 * @throws GowallaException
 	 */
 	public List<Pin> getUserPins(final Id<User> identity) throws GowallaException {
 		return getUserPins(identity.getId());
 	}
 	
 	/**
 	 * Get a user's pins.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param username The username of the user you are requesting pins of.
 	 * @throws GowallaException
 	 */
 	public List<Pin> getUserPins(final String username) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%s/pins",username));
 			return responseTranslator.translateUserPins(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this name.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a user's created trips
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param id The user number.
 	 * @throws GowallaException
 	 */
 	public List<TripSummary> getUserCreatedTrips(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d/trips",id));
 			return responseTranslator.translateUserCreatedTrips(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a user's created trips
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param username The username of the user you are requesting trips created by.
 	 * @throws GowallaException
 	 */
 	public List<TripSummary> getUserCreatedTrips(final String username) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%s/trips",username));
 			return responseTranslator.translateUserCreatedTrips(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this name.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get the list of photos that a user has posted.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param id The int id of the user.
 	 * @return A list of photos, or null if no such user exists.
 	 * @throws GowallaException
 	 */
 	public List<UserPhoto> getUserPhotos(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d/photos", id));
 			return responseTranslator.translateUserPhotos(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Spot for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get the list of photos that a user has posted.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param identity The id of the user.
 	 * @return A list of photos, or null if no such user exists.
 	 * @throws GowallaException
 	 */
 	public List<UserPhoto> getUserPhotos(final Id<User> identity) throws GowallaException {
 		return getUserPhotos(identity.getId());
 	}
 		
 	/**
 	 * Get all of the items at a spot.  
 	 * 
 	 * @param id The id of the spot.
 	 * @return a List of Items, an empty list for a valid spot with no items, null if spot is invalid.
 	 */
 	public List<Item> getItemsAtSpot(int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/spots/%d/items", id));
 			return responseTranslator.translateItems(response);
 		}catch(RequestNotAcceptableException e) {
 			// No Spot for this number.
 			return null;
 		}
 	}
 	
 	/**
 	 * Get all of the items at a spot.  
 	 * 
 	 * @param identity The id of the spot.
 	 * @return a List of Items, an empty list for a valid spot with no items, null if spot is invalid.
 	 */
 	public List<Item> getItemsAtSpot(final Id<Spot> identity) throws GowallaException {
 		return getItemsAtSpot(identity.getId());
 	}
 	
 	/**
 	 * Get an item, by id.
 	 */
 	public Item getItem(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/items/%d", id));
 			return responseTranslator.translateItem(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Item for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get an item, by id.
 	 */
 	public Item getItem(final Id<Item> identity) throws GowallaException {
 		return getItem(identity.getId());
 	}
 	
 	/**
 	 * Get the list of events that happened at a spot.
 	 * 
 	 * @param identity The id of the spot.
 	 * @return A list of events, or null if no such spot exists.
 	 * @throws GowallaException
 	 */
 	public List<SpotEvent> getSpotEvents(final Id<Spot> identity) throws GowallaException {
 		return getSpotEvents(identity.getId());
 	}
 	
 	/**
 	 * Get the list of events that happened at a spot.
 	 * 
 	 * @param id The int id of the spot.
 	 * @return A list of events, or null if no such spot exists.
 	 * @throws GowallaException
 	 */
 	public List<SpotEvent> getSpotEvents(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/spots/%d/events", id));
 			return responseTranslator.translateSpotEvents(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Spot for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get the list of photos that happened at a spot.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param id The int id of the spot.
 	 * @return A list of photos, or null if no such spot exists.
 	 * @throws GowallaException
 	 */
 	public List<SpotPhoto> getSpotPhotos(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/spots/%d/photos", id));
 			return responseTranslator.translateSpotPhotos(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Spot for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get the list of photos that happened at a spot.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param identity The id of the spot.
 	 * @return A list of photos, or null if no such spot exists.
 	 * @throws GowallaException
 	 */
 	public List<SpotPhoto> getSpotPhotos(final Id<Spot> identity) throws GowallaException {
 		return getSpotPhotos(identity.getId());
 	}
 	
 	/**
 	 * Get the list of events that happened to an item.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 *  
 	 * @param id The int id of the item.
 	 * @return A list of events, or null if no such spot exists.
 	 * @throws GowallaException
 	 */
 	public List<ItemEvent> getItemEvents(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/items/%d/events", id));
 			return responseTranslator.translateItemEvents(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Spot for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get the list of events that happened to an item.
 	 * 
 	 * WARNING: This method uses calls not officially supported by Gowalla.  This 
 	 * means it may go away without warning while you are using it.  It also means
 	 * we may have to drop support for it.
 	 * 
 	 * @param identity The id of the item.
 	 * @return A list of events, or null if no such spot exists.
 	 * @throws GowallaException
 	 */
 	public List<ItemEvent> getItemEvents(final Id<Item> identity) throws GowallaException {
 		return getItemEvents(identity.getId());
 	}
 
 	/**
 	 * Get a Trip, by id.  If the trip doesn't exist, this will return null.
 	 * 
 	 * @param identity The id of the trip you want.
 	 * @return a Trip object or null if not found.
 	 * @throws GowallaException
 	 */
 	public Trip getTrip(final Id<Trip> identity) throws GowallaException {
 		return getTrip(identity.getId());
 	}
 	
 	/**
 	 * Get a Trip, by id.  If the trip doesn't exist, this will return null.
 	 * 
 	 * @param id The integer id of the trip you want.
 	 * @return a Trip object or null if not found.
 	 * @throws GowallaException
 	 */
 	public Trip getTrip(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/trips/%d", id));
 			return responseTranslator.translateTrip(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Trip for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get all of the trips the currently authenticated user is allowed to see, in summary form.
 	 * @throws GowallaException
 	 */
 	public List<TripSummary> getTrips() throws GowallaException {
 		try {
 			final String response = request("/trips");
 			return responseTranslator.translateTripSummaries(response);
 		} catch(RequestNotAcceptableException e) {
 			// No Trip for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get a list of stamps for a specific user, by login.  If zero is passed in for a limit
 	 * then all of the stamps, up to the max the server will return, will be returned.
 	 * 
	 * @param criteria A critiera object that defines how to locate the stamps.
 	 * 
 	 * @return A list of Stamps, or null if the user doesn't exist.
 	 * @throws GowallaException
 	 */
 	public List<Stamp> getStamps(final StampCriteria criteria) throws GowallaException {
 		try {
 			final String response = request(criteria.getRequest());
 			return responseTranslator.translateStamps(response);
 		} catch(RequestNotAcceptableException e) {
 			// No user for this request.
 			return null;
 		}	
 	}
 		
 	/**
 	 * Get a list of stamps for a specific user, by username, up to the max the server will return.
 	 * 
 	 * @param username The username name of the user.
 	 * 
 	 * @return A list of Stamps, or null if the user doesn't exist.
 	 * @throws GowallaException
 	 */
 	public List<Stamp> getStamps(final String username) throws GowallaException {
 		return getStamps(new StampCriteria.Builder(username).build());
 	}
 		
 	/**
 	 * Get a list of stamps for a specific user, by id, up to the max the server will return.
 	 * 
 	 * @param id The id name of the user.
 	 * 
 	 * @return A list of Stamps, or null if the user doesn't exist.
 	 * @throws GowallaException
 	 */
 	public List<Stamp> getStamps(final int id) throws GowallaException {
 		return getStamps(new StampCriteria.Builder(id).build());
 	}
 	
 	/**
 	 * Get a list of stamps for a specific user, by identity, up to the max the server will return.
 	 * 
 	 * @param id The id name of the user.
 	 * 
 	 * @return A list of Stamps, or null if the user doesn't exist.
 	 * @throws GowallaException
 	 */
 	public List<Stamp> getStamps(final Id<User> id) throws GowallaException {
 		return getStamps(new StampCriteria.Builder(id).build());
 	}
 	
 	/**
 	 * Get a User by id.  If no user is found by that id, null is returned.
 	 * 
 	 * @param identity The id of the user.
 	 * @return A User object or null if not found.
 	 * @throws GowallaException
 	 */
 	public FullUser getUser(final Id<User> identity) throws GowallaException {
 		return getUser(identity.getId());
 	}
 	
 	/**
 	 * Get a User by id.  If no user is found by that id, null is returned.
 	 * 
 	 * @param id The integer id of the user.
 	 * @return A User object or null if not found.
 	 * @throws GowallaException
 	 */
 	public FullUser getUser(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d", id));
 			return responseTranslator.translateUser(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get a User by login name.  If no user is found by that id, null is returned.
 	 * 
 	 * @param login The String login name of the user.
 	 * @return A User object or null if not found.
 	 * @throws GowallaException
 	 */
 	public FullUser getUser(final String login) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%s", login));
 			return responseTranslator.translateUser(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get the top spots the given user has checked in to.  If the user is not found, null
 	 * will be returned.
 	 * 
 	 * @param id The id of the user.
 	 * @return A list of spots and the number of checkins.
 	 * @throws GowallaException
 	 */
 	public List<VisitedSpot> getTopSpots(final int id) throws GowallaException {
 		try {
 			final String response = request(String.format("/users/%d/top_spots", id));
 			return responseTranslator.translateVisitedSpots(response);
 		} catch(RequestNotAcceptableException e) {
 			// No User for this number.
 			return null;
 		}	
 	}
 	
 	/**
 	 * Get the top spots the given user has checked in to.  If the user is not found, null
 	 * will be returned.
 	 * 
 	 * @param identity The id of the user.
 	 * @return A list of spots and the number of checkins.
 	 * @throws GowallaException
 	 */
 	public List<VisitedSpot> getTopSpots(final Id<User> identity) throws GowallaException {
 		return getTopSpots(identity.getId());
 	}
 		
 	/**
 	 * Encapsulate request handler and rate limitation call.
 	 */
 	private String request(final String resource) throws RateLimitExceededException, GowallaRequestException {
 		rateLimitPreRequest();
 		try {
 			return handler.handleRequest(resource, authentication.getHeaders());
 		} finally {
 			rateLimitPostRequest();
 		}
 	}
 	
 	/**
 	 * Enforce the rate limitation.
 	 * @throws RateLimitExceededException 
 	 */
 	private void rateLimitPreRequest() throws RateLimitExceededException {
 		if(rateLimiter != null && !rateLimiter.preRequest()) {
 			throw new RateLimitExceededException();
 		}
 	}
 	
 	/**
 	 * Release the rate limitation.
 	 * @throws RateLimitExceededException 
 	 */
 	private void rateLimitPostRequest() throws RateLimitExceededException {
 		if(rateLimiter != null) {
 			rateLimiter.postRequest();
 		}
 	}		
 }
