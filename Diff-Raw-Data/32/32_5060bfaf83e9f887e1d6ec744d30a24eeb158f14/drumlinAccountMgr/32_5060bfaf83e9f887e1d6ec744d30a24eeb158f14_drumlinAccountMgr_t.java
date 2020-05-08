 package com.rathravane.drumlin.app.accounts;
 
 import java.util.logging.Logger;
 
 import com.rathravane.drumlin.app.accounts.drumlinAccountPersistence.tagInfo;
 import com.rathravane.till.data.oneWayHasher;
 import com.rathravane.till.data.sha1HmacSigner;
 import com.rathravane.till.data.uniqueStringGenerator;
 import com.rathravane.till.logging.rrLogSetup;
 
 /**
  * A user and account manager. The application must provide implementations of
  * the drumlinAccountFactory interface and its associated interfaces for drumlinUser,
  * drumlinAccount, and drumlinApiKey.
  * 
  * @author peter
  *
  * @param <U>
  * @param <A>
  */
 public class drumlinAccountMgr<U,A>
 {
 	/**
 	 * Instantiate an account manager given the app's account factory interface.
 	 * 
 	 * @param f
 	 * @param appSpecificSalt
 	 */
 	public drumlinAccountMgr ( drumlinAccountPersistence<U,A> f )
 	{
 		fFactory = f;
 	}
 
 	/**
 	 * Close the account manager.
 	 */
 	public void close ()
 	{
 		fFactory.close ();
 	}
 
 	/**
 	 * return true if the account exists
 	 * @param acctKey
 	 * @return true if the account exists
 	 * @throws msModelException
 	 */
 	public boolean accountExists ( String acctKey ) throws drumlinAccountsException
 	{
 		return fFactory.accountExists ( acctKey );
 	}
 
 	/**
 	 * load an account with the given key or throw if it doesn't exist
 	 * @param acctKey
 	 * @return the account object
 	 * @throws drumlinAccountsException 
 	 */
 	public A loadAccount ( String acctKey ) throws drumlinAccountsException
 	{
 		return fFactory.loadAccount ( acctKey );
 	}
 
 	/**
 	 * create an account with the given key
 	 * @param acctKey
 	 * @return the account object
 	 * @throws drumlinAccountsException 
 	 * @throws msModelException
 	 */
 	public A createAccount ( String acctKey ) throws drumlinAccountsException
 	{
 		return fFactory.createAccount ( acctKey );
 	}
 
 	public boolean userExists ( String userId ) throws drumlinAccountsException
 	{
 		return fFactory.userExists ( userId );
 	}
 
 	public U loadUser ( String userId ) throws drumlinAccountsException
 	{
 		return fFactory.loadUser ( userId );
 	}
 
 	/**
 	 * authenticate a user with user id and password
 	 * @param userId
 	 * @param password
 	 * @return the user record
 	 * @throws drumlinAccountsException 
 	 */
 	public U authenticateUserWithPassword ( String userId, String password ) throws drumlinAccountsException
 	{
 		final U u = fFactory.loadUser ( userId );
 		if ( u == null )
 		{
 			log.info ( "AUTH_FAIL: no such user " + userId + "." );
			return null;
 		}
 
 		final String acctId = fFactory.getAccountIdForUser ( userId );
 		final A a = fFactory.loadAccount ( acctId );
 		if ( a == null )
 		{
 			log.warning ( "AUTH_FAIL: no such account " + acctId + ", referenced by " + userId + "." );
			return null;
 		}
 	
 		if ( !fFactory.isUserEnabled ( userId ) )
 		{
 			log.info ( "AUTH_FAIL: " + userId + "; user is disabled." );
			return null;
 		}
 		else if ( !isPasswordValid ( userId, password ) )
 		{
 			log.info ( "AUTH_FAIL: " + userId + "; incorrect password." );
			return null;
 		}
 		else if ( !fFactory.isAccountEnabled ( acctId ) )
 		{
 			log.info ( "AUTH_FAIL: " + userId + " ; account is disabled." );
			return null;
 		}
 		else if ( isConfirmationOverdue ( userId ) )
 		{
 			log.info ( "AUTH_FAIL: " + userId + " ; user confirmation grace period expired." );
			return null;
 		}

		return u;
 	}
 
 	public U createUser ( String acct, String userId, String password, String userEmail ) throws drumlinAccountsException
 	{
 		if ( fFactory.accountExists ( acct ) && !fFactory.userExists ( userId ) )
 		{
 			final U result = fFactory.createUser ( acct, userId, userEmail );
 			setPassword ( userId, password );
 			return result;
 		}
 		return null;
 	}
 
 	/**
 	 * Require that the user is confirmed by a particular point in time. If confirmation does not
 	 * occur, the user account is disabled.
 	 * 
 	 * A user is always in one of the following states:<br>
 	 * 
 	 * 		no confirmation required<br>
 	 *		confirmation required, still in grace period<br>
 	 *		confirmation required, outside of grace period<br>
 	 *		confirmed<br>
 	 * 
 	 * @param u
 	 * @param secondsUntilExpire
 	 * @param nonsense
 	 * @return a tag identifying the confirmation request (send this in an email, for example)
 	 * @throws drumlinAccountsException 
 	 */
 	public String requireUserConfirmation ( String userKey, long secondsUntilExpire, String nonsense ) throws drumlinAccountsException
 	{
 		String tag = null;
 		if ( fFactory.userExists ( userKey ) )
 		{
 			final drumlinAccountPersistence.tagInfo ti = fFactory.getRequestTagByUser ( userKey, kTagType_ConfirmUser );
 			if ( ti != null )
 			{
 				fFactory.deleteRequestByTag ( ti.fTag, kTagType_ConfirmUser );
 			}
 
 			tag = uniqueStringGenerator.createUrlKey ( nonsense );
 			final long expiresAtSeconds = ( System.currentTimeMillis () / 1000 ) + secondsUntilExpire;
 
 			fFactory.storeRequestTag ( userKey, kTagType_ConfirmUser, tag, expiresAtSeconds );
 		}
 		return tag;
 	}
 
 	/**
 	 * Load and confirm a user record.
 	 * @param userId
 	 * @throws drumlinAccountsException
 	 */
 	public void confirmUser ( String userKey ) throws drumlinAccountsException
 	{
 		if ( fFactory.userExists ( userKey ) )
 		{
 			final drumlinAccountPersistence.tagInfo ti = fFactory.getRequestTagByUser ( userKey, kTagType_ConfirmUser );
 			if ( ti != null )
 			{
 				fFactory.deleteRequestByTag ( ti.fTag, kTagType_ConfirmUser );
 				fFactory.deleteRequestTagOnUser ( userKey, ti.fTag, kTagType_ConfirmUser );
 			}
 		}
 	}
 
 	/**
 	 * Confirm a user based on a confirmation tag.
 	 * @param tag
 	 * @throws drumlinAccountsException 
 	 */
 	public boolean confirmUserWithTag ( String requestTag ) throws drumlinAccountsException
 	{
 		final tagInfo ti = fFactory.getRequestTagByTag ( requestTag, kTagType_ConfirmUser );
 		if ( ti == null ) return false; 	// ignore the request
 
 		final long nowEpochTime = System.currentTimeMillis () / 1000;
 		if ( ti.fExpiresAtEpochTime < nowEpochTime ) return false;	// out of date
 
 		confirmUser ( ti.fUserId );
 
 		fFactory.deleteRequestByTag ( requestTag, kTagType_PasswordReset );
 
 		return true;
 	}
 
 	public void enableUser ( String userId, boolean enabled ) throws drumlinAccountsException
 	{
 		fFactory.enableUser ( userId, enabled );
 	}
 	
 	public void enableAccount ( String acctId, boolean enabled ) throws drumlinAccountsException
 	{
 		fFactory.enableAccount ( acctId, enabled );
 	}
 
 	/**
 	 * Set the user's password.
 	 * @param u
 	 * @param pwd
 	 * @throws drumlinAccountsException 
 	 */
 	public void setPassword ( String user, String pwd ) throws drumlinAccountsException
 	{
 		final String nonsense = fFactory.getAppSignature();
 		final String salt = generateSalt ( nonsense );
 		final String hashedPassword = passwdHash ( pwd, salt );
 		fFactory.storePasswordInfo ( user, salt, hashedPassword );
 	}
 
 	/**
 	 * Request a password reset request that expires. The return value is a unique tag for the request
 	 * that must be presented to complete the transaction. The password is NOT changed until the
 	 * request is completed.
 	 * 
 	 * @param userKey
 	 * @param secondsUntilExpire
 	 * @param nonsense Arbitrary string to use in generating the returned reset tag.
 	 * @return a unique reset request tag
 	 * @throws drumlinAccountsException 
 	 * @throws msItemDoesNotExistException
 	 * @throws unexpectedException
 	 */
 	public String requestPasswordReset ( String userKey, long secondsUntilExpire, String nonsense ) throws drumlinAccountsException
 	{
 		String tag = null;
 		if ( fFactory.userExists ( userKey ) )
 		{
 			final drumlinAccountPersistence.tagInfo ti = fFactory.getRequestTagByUser ( userKey, kTagType_PasswordReset );
 			if ( ti != null )
 			{
 				fFactory.deleteRequestByTag ( ti.fTag, kTagType_PasswordReset );
 			}
 
 			tag = uniqueStringGenerator.createUrlKey ( nonsense );
 			final long expiresAtSeconds = ( System.currentTimeMillis () / 1000 ) + secondsUntilExpire;
 
 			fFactory.storeRequestTag ( userKey, kTagType_PasswordReset, tag, expiresAtSeconds );
 		}
 		return tag;
 	}
 
 	private static final String kTagType_PasswordReset = "passwordReset";
 	private static final String kTagType_ConfirmUser = "confirmUser";
 	
 	/**
 	 * Complete the password reset using the new password. This also cancels any
 	 * pending account confirmation, because it presumably went through the same
 	 * email round-trip mechanism.
 	 * 
 	 * @param requestTag
 	 * @param newPassword
 	 * @throws drumlinAccountsException 
 	 * @throws unexpectedException
 	 */
 	public boolean completePasswordReset ( String requestTag, String newPassword ) throws drumlinAccountsException
 	{
 		final tagInfo ti = fFactory.getRequestTagByTag ( requestTag, kTagType_PasswordReset );
 		if ( ti == null ) return false; 	// ignore the request
 
 		boolean result = false;
 
 		final long nowEpochTime = System.currentTimeMillis () / 1000;
 		if ( ti.fExpiresAtEpochTime >= nowEpochTime )
 		{
 			setPassword ( ti.fUserId, newPassword );
 			confirmUser ( ti.fUserId );
 			result = true;
 		}
 
 		fFactory.deleteRequestByTag ( requestTag, kTagType_PasswordReset );
 		fFactory.deleteRequestTagOnUser ( ti.fUserId, requestTag, kTagType_PasswordReset );
 
 		return result;
 	}
 
 	/**
 	 * Load an API key record.
 	 * @param apiKey
 	 * @return an API key or null
 	 * @throws drumlinAccountsException
 	 */
 	public drumlinApiKey loadApiKey ( String apiKey ) throws drumlinAccountsException
 	{
 		return fFactory.loadApiKey ( apiKey );
 	}
 
 	/**
 	 * Authenticate a request using an api (public) key, a string of content that's signed, and the
 	 * signature.
 	 * 
 	 * @param apiKey
 	 * @param signedContent
 	 * @param signature
 	 * @return an API key record, which will indicate whether its an account or user access, or null
 	 * @throws drumlinAccountsException 
 	 */
 	public drumlinApiKey authenticateViaApiKey ( String apiKey, String signedContent, String signature ) throws drumlinAccountsException
 	{
 		drumlinApiKey result = null;
 
 		final drumlinApiKey key = fFactory.loadApiKey ( apiKey );
 		if ( key != null )
 		{
 			// use private key to sign content
 			final String expectedSignature = sha1HmacSigner.sign ( signedContent, key.getSecret () );
 
 			// compare
 			if ( expectedSignature.equals ( signature ) )
 			{
 				result = key;
 			}
 			// else: not signed correctly
 		}
 		// else: no account
 
 		return result;
 	}
 
 	/**
 	 * Create an API key for the given account.
 	 * @param acct
 	 * @return an API key
 	 * @throws drumlinAccountsException 
 	 */
 	public drumlinApiKey createAcctApiKey ( String acctId ) throws drumlinAccountsException
 	{
 		final String appSig = fFactory.getAppSignature();
 		final String newApiKey = generateKey ( 16, appSig );
 		final String newApiSecret = generateKey ( 24, appSig );
 		return establishAcctApiKey ( acctId, newApiKey, newApiSecret );
 	}
 
 	/**
 	 * Create an API key for the given user.
 	 * @param user
 	 * @return an API key
 	 * @throws drumlinAccountsException 
 	 */
 	public drumlinApiKey createUserApiKey ( String user ) throws drumlinAccountsException
 	{
 		final String appSig = fFactory.getAppSignature();
 		final String newApiKey = generateKey ( 16, appSig );
 		final String newApiSecret = generateKey ( 24, appSig );
 		return establishUserApiKey ( user, newApiKey, newApiSecret );
 	}
 
 	/**
 	 * This is provided for internal use, not use from user-facing code.
 	 * @param acct
 	 * @param apiKey
 	 * @param newApiSecret
 	 * @return
 	 * @throws drumlinAccountsException 
 	 * @throws msModelException
 	 */
 	public drumlinApiKey establishAcctApiKey ( final String acctId, final String apiKey, final String newApiSecret ) throws drumlinAccountsException
 	{
 		drumlinApiKey key = null;
 
 		// if the API key exists (mainly used for test), load it and update it
 		key = loadApiKey ( apiKey );
 		if ( key != null && key.getAccount () != null && key.getAccount().equals ( acctId ) )
 		{
 			// update the existing key
 			log.info ( "API key " + apiKey + " already exists for account [" + acctId + "]. Resetting API secret." );
 			fFactory.setApiSecret ( apiKey, newApiSecret );
 		}
 		else
 		{
 			// create a new key
 			key = new drumlinApiKey ()
 			{
 				@Override
 				public String getKey () { return apiKey; }
 
 				@Override
 				public String getSecret () { return newApiSecret; }
 
 				@Override
 				public drumlinApiKey.type getAccessType () { return drumlinApiKey.type.ACCOUNT; }
 
 				@Override
 				public String getAccount () { return acctId; }
 
 				@Override
 				public String getUser () { return null; }
 			};
 			fFactory.storeApiKey ( key );
 		}
 
 		return key;
 	}
 
 	/**
 	 * This is provided for internal use, not use from user-facing code.
 	 * @param acct
 	 * @param apiKey
 	 * @param newApiSecret
 	 * @return
 	 * @throws drumlinAccountsException 
 	 * @throws msModelException
 	 */
 	public drumlinApiKey establishUserApiKey ( final String userId, final String apiKey, final String newApiSecret ) throws drumlinAccountsException
 	{
 		drumlinApiKey key = null;
 
 		// if the API key exists (mainly used for test), load it and update it
 		key = loadApiKey ( apiKey );
 		if ( key != null && key.getUser () != null && key.getUser().equals ( userId ) )
 		{
 			// update the existing key
 			log.info ( "API key " + apiKey + " already exists for user [" + userId + "]. Resetting API secret." );
 			fFactory.setApiSecret ( apiKey, newApiSecret );
 		}
 		else
 		{
 			// create a new key
 			key = new drumlinApiKey ()
 			{
 				@Override
 				public String getKey () { return apiKey; }
 
 				@Override
 				public String getSecret () { return newApiSecret; }
 
 				@Override
 				public drumlinApiKey.type getAccessType () { return drumlinApiKey.type.USER; }
 
 				@Override
 				public String getAccount () { return null; }
 
 				@Override
 				public String getUser () { return userId; }
 			};
 			fFactory.storeApiKey ( key );
 		}
 
 		return key;
 	}
 
 	private final drumlinAccountPersistence<U,A> fFactory;
 
 	private boolean isPasswordValid ( String userId, String pwd ) throws drumlinAccountsException
 	{
 		boolean result = false;
 		
 		final String[] pwdData = fFactory.getPasswordInfo ( userId );
 		
 		final String hash = passwdHash ( pwd, pwdData[0] );
 		final String stored = pwdData[1];
 		if ( stored != null )
 		{
 			result = hash.equals ( stored );
 		}
 		return result;
 	}
 
 	private boolean isConfirmationOverdue ( String userKey ) throws drumlinAccountsException
 	{
 		// it's overdue if there's an expired request out there.
 		if ( fFactory.userExists ( userKey ) )
 		{
 			final drumlinAccountPersistence.tagInfo ti = fFactory.getRequestTagByUser ( userKey, kTagType_ConfirmUser );
 			if ( ti != null )
 			{
 				final long now = System.currentTimeMillis () / 1000;
 				return ti.fExpiresAtEpochTime < now;
 			}
 		}
 		return false;
 	}
 
 	private static String passwdHash ( String p, String salt )
 	{
 		return oneWayHasher.hash ( p, salt );
 	}
 
 	private static String generateSalt ( String nonsense )
 	{
 		return generateKey ( 8, nonsense );
 	}
 
 	private static String kKeyChars = "ABCDEFGHJIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
 	private static String generateKey ( int length, String nonsense  )
 	{
 		return uniqueStringGenerator.createKeyUsingAlphabet ( nonsense, kKeyChars, length );
 	}
 
 	private static final Logger log = rrLogSetup.getLog ( drumlinAccountMgr.class );
 }
