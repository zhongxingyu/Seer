 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtoodle.api.auth;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 import jtoodle.api.bean.util.JToodleException;
 import jtoodle.api.bean.auth.Token;
 import jtoodle.api.bean.auth.UserId;
 import jtoodle.api.intf.AuthenticationConstants;
 import jtoodle.api.request.web.TokenOperations;
 import jtoodle.api.request.web.TokenSearchCriteria;
 import jtoodle.api.request.web.UserIdOperations;
 import jtoodle.api.request.web.UserIdSearchCriteria;
 import jtoodle.api.util.NullSafe;
 import jtoodle.api.util.Hasher;
 
 /**
  *
  * @author justo
  */
 public class AuthCache implements AuthenticationConstants {
 
 	private static final Logger logger = Logger.getLogger( AuthCache.class.getName() );
 
 	private static final long MAX_TOKEN_AGE =
 		4 // hours
 		* 60 // minutes/hour
 		* 60 // seconds/minute
 		* 1000 // millis/second
 		;
 
 	private static final String KEY_EMAIL = "email";
 	private static final String KEY_PASSWORD = "hashed_password";
 	private static final String KEY_USER_ID = "user_id";
 	private static final String KEY_TOKEN = "token";
 	private static final String KEY_API_KEY = "api_key";
 	private static final String KEY_TOKEN_TIMESTAMP_MILLIS = "token_timestamp_millis";
 	private static final String KEY_TOKEN_TIMESTAMP_TEXT = "token_timestamp_text";
 
 	private static final Preferences _prefs = Preferences.userRoot().node( "/jtoodle/api/auth" );
 
 	private static String _password = null;
 
 	private static void save() {
 		logger.entering( AuthCache.class.getName(), "save()" );
 
 		try {
 			_prefs.flush();
 		} catch( BackingStoreException ex ) {
 			logger.log( Level.SEVERE, null, ex );
 		}
 
 		logger.exiting( AuthCache.class.getName(), "save()" );
 	}
 
 	private static void clear() {
 		logger.entering( AuthCache.class.getName(), "clear()" );
 
 		try {
 			_prefs.clear();
 			save();
 		} catch( BackingStoreException ex ) {
 			logger.log( Level.SEVERE, null, ex );
 		}
 
 		logger.exiting( AuthCache.class.getName(), "clear()" );
 	}
 
 	public static void login( String email, String password ) throws JToodleException {
 		logger.entering( AuthCache.class.getName(), "login(String,String)" );
 
 		clear();
 		setEmail( email );
 		setPassword( password );
 
 		//getUserId();
 		//getToken();
 		getApiKey();
 
 		logger.exiting( AuthCache.class.getName(), "login(String,String)" );
 	}
 
 	public static void logout() {
 		logger.entering( AuthCache.class.getName(), "logout()" );
 		clear();
 		logger.exiting( AuthCache.class.getName(), "logout()" );
 	}
 
 	public static boolean isAuthenticated() {
 		logger.entering( AuthCache.class.getName(), "isAuthenticated()" );
 
 		String apiKey = _prefs.get( KEY_API_KEY, null );
		boolean isLoggedIn = ( ! NullSafe.isNullOrEmpty( apiKey ) ) &&
							 ( ! tokenIsStale() );
 
 		logger.exiting( AuthCache.class.getName(), "isAuthenticated()" );
 
 		return( isLoggedIn );
 	}
 
 	private static void setEmail( String email ) {
 		logger.entering( AuthCache.class.getName(), "setEmail(String)", email );
 
 		if( !NullSafe.equals( getEmail(), email ) ) {
 			_prefs.put( KEY_EMAIL, email );
 			setUserId( null );
 		}
 
 		logger.exiting( AuthCache.class.getName(), "setEmail(String)" );
 	}
 
 	public static String getEmail() {
 		logger.entering( AuthCache.class.getName(), "getEmail()" );
 		String returnValue = _prefs.get( KEY_EMAIL, null );
 		logger.exiting( AuthCache.class.getName(), "getEmail()", returnValue );
 
 		return( returnValue );
 	}
 
 	private static void setPassword( String password ) {
 		logger.entering( AuthCache.class.getName(), "setPassword(String)", password );
 		_password = password;
 
 		try {
 			String hashedPassword = Hasher.md5Hash( _password );
 
 			if( !NullSafe.equals( getHashedPassword(), hashedPassword ) ) {
 				storeHashedPassword( null );
 				setUserId( null );
 			}
 		} catch( NoSuchAlgorithmException ex ) {
 			logger.log( Level.SEVERE, null, ex );
 		}
 
 		logger.exiting( AuthCache.class.getName(), "setPassword(String)" );
 	}
 
 	private static String getHashedPassword() {
 		logger.entering( AuthCache.class.getName(), "getHashedPassword()" );
 		String returnValue = _prefs.get( KEY_PASSWORD, null );
 		logger.exiting( AuthCache.class.getName(), "getHashedPassword()", returnValue );
 
 		return( returnValue );
 	}
 
 	private static void storeHashedPassword( String clearPassword ) {
 		logger.entering( AuthCache.class.getName(), "storeHashedPassword(String)", clearPassword );
 
 		if( NullSafe.isNullOrEmpty( clearPassword ) ) {
 			_prefs.remove( KEY_PASSWORD );
 		} else {
 			try {
 				_prefs.put( KEY_PASSWORD, Hasher.md5Hash( clearPassword ) );
 			} catch( NoSuchAlgorithmException ex ) {
 				logger.log( Level.SEVERE, null, ex );
 			}
 		}
 
 		save();
 
 		logger.exiting( AuthCache.class.getName(), "storeHashedPassword(String)" );
 	}
 
 	private static void setUserId( String userId ) {
 		logger.entering( AuthCache.class.getName(), "setUserId(String)", userId );
 
 		String storedUserId = _prefs.get( KEY_USER_ID, null );
 
 		if( !NullSafe.equals( userId, storedUserId ) ) {
 			if( NullSafe.isNullOrEmpty( userId ) ) {
 				_prefs.remove( KEY_USER_ID );
 			} else {
 				_prefs.put( KEY_USER_ID, userId );
 			}
 
 			setToken( null );
 			save();
 		}
 
 		logger.exiting( AuthCache.class.getName(), "setUserId(String)" );
 	}
 
 	public static String getUserId() throws JToodleException {
 		logger.entering( AuthCache.class.getName(), "getUserId()" );
 
 		String userId = _prefs.get( KEY_USER_ID, null );
 
 		if( NullSafe.isNullOrEmpty( userId ) ) {
 			try {
 				UserIdSearchCriteria sc = new UserIdSearchCriteria();
 				sc.setEmail( getEmail() );
 				sc.setPassword( _password );
 
 				UserIdOperations uidSearch = new UserIdOperations();
 				uidSearch.setOperationCriteria( sc );
 
 				UserId bean = uidSearch.searchSingle();
 				userId = bean.getUserId();
 
 				setUserId( userId );
 				storeHashedPassword( _password );
 			} catch( IOException ex ) {
 				logger.log( Level.SEVERE, null, ex );
 			}
 		}
 
 		logger.exiting( AuthCache.class.getName(), "getUserId()", userId );
 
 		return( userId );
 	}
 
 	private static void setToken( String token ) {
 		logger.entering( AuthCache.class.getName(), "setToken(String)", token );
 
 		String storedToken = _prefs.get( KEY_TOKEN, null );
 
 		if( !NullSafe.equals( token, storedToken ) ) {
 			if( NullSafe.isNullOrEmpty( token ) ) {
 				_prefs.remove( KEY_TOKEN );
 			} else {
 				_prefs.put( KEY_TOKEN, token );
 			}
 
 			setApiKey( null );
 			markTokenTimestampMillis();
 			save();
 		}
 
 		logger.exiting( AuthCache.class.getName(), "setToken(String)" );
 	}
 
 	public static String getToken() throws JToodleException {
 		logger.entering( AuthCache.class.getName(), "getToken()" );
 
 		String token = _prefs.get( KEY_TOKEN, null );
 
 		if( NullSafe.isNullOrEmpty( token ) || tokenIsStale() ) {
 			try {
 				TokenSearchCriteria tsc = new TokenSearchCriteria();
 				tsc.setUserId( getUserId() );
 
 				TokenOperations tokenOps = new TokenOperations();
 				tokenOps.setOperationCriteria( tsc );
 
 				Token bean = tokenOps.searchSingle();
 				token = bean.getToken();
 
 				setToken( token );
 			} catch( IOException ex ) {
 				logger.log( Level.SEVERE, null, ex );
 			}
 		}
 
 		logger.exiting( AuthCache.class.getName(), "getToken()", token );
 
 		return( token );
 	}
 
 	private static void setApiKey( String apiKey ) {
 		logger.entering( AuthCache.class.getName(), "setApiKey(String)", apiKey );
 
 		String storedApiKey = _prefs.get( KEY_API_KEY, null );
 
 		if( !NullSafe.equals( apiKey, storedApiKey ) ) {
 			if( NullSafe.isNullOrEmpty( apiKey ) ) {
 				_prefs.remove( KEY_API_KEY );
 			} else {
 				_prefs.put( KEY_API_KEY, apiKey );
 			}
 			save();
 		}
 
 		logger.exiting( AuthCache.class.getName(), "setApiKey(String)" );
 	}
 
 	public static String getApiKey() throws JToodleException {
 		logger.entering( AuthCache.class.getName(), "getApiKey()" );
 
 		String apiKey = _prefs.get( KEY_API_KEY, null );
 
 		if( NullSafe.isNullOrEmpty( apiKey ) || tokenIsStale() ) {
 			try {
 				// hashed password doesn't get stored until the token is
 				// sucessfully retrieved, so it has to be called first
 				String token = getToken();
 
 				apiKey = Hasher.md5Hash( new StringBuilder()
 					.append( getHashedPassword() )
 					.append( AuthenticationConstants.APP_TOKEN )
 					.append( token )
 					.toString() );
 				setApiKey( apiKey );
 			} catch( NoSuchAlgorithmException ex ) {
 				logger.log( Level.SEVERE, null, ex );
 			}
 		}
 
 		logger.entering( AuthCache.class.getName(), "getApiKey()", apiKey );
 
 		return( apiKey );
 	}
 
 	private static void markTokenTimestampMillis() {
 		long ts = System.currentTimeMillis();
 		_prefs.putLong( KEY_TOKEN_TIMESTAMP_MILLIS, ts );
 		_prefs.put( KEY_TOKEN_TIMESTAMP_TEXT, new Date( ts ).toString() );
 	}
 
 	private static long getTokenTimestampMillis() {
 		return( _prefs.getLong( KEY_TOKEN_TIMESTAMP_MILLIS, System.currentTimeMillis() ) );
 	}
 
 	private static boolean tokenIsStale() {
 		long tokenAge = System.currentTimeMillis() - getTokenTimestampMillis();
 		return( tokenAge >= MAX_TOKEN_AGE );
 	}
 }
