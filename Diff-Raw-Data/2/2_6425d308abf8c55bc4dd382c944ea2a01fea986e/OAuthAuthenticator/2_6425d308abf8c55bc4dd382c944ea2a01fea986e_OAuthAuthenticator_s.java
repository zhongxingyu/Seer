 package com.pewpewarrows.electricsheep.net;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import com.pewpewarrows.electricsheep.activities.OAuthAccountActivity;
 import com.pewpewarrows.electricsheep.log.Log;
 
 import android.accounts.AbstractAccountAuthenticator;
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorResponse;
 import android.accounts.AccountManager;
 import android.accounts.NetworkErrorException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 
 /**
  * TODO: This desperately needs a better name!
  */
 public abstract class OAuthAuthenticator extends AbstractAccountAuthenticator {
 
 	private static final String TAG = OAuthAuthenticator.class.getName();
 
 	@SuppressWarnings("rawtypes")
 	protected Class mAccountActivityKlass;
 	protected String mAccountType;
 	protected String mOAuthTokenType;
 	protected String mOAuthSecretType;
 
 	private final Context mContext;
 
 	public OAuthAuthenticator(Context context) {
 		super(context);
 
 		mContext = context;
 	}
 
 	@SuppressWarnings("unchecked")
	protected void extractInfoFromActiviy() {
 		try {
 			Method m = mAccountActivityKlass.getMethod("getAccountType",
 					new Class[] {});
 			mAccountType = (String) m.invoke(null);
 
 			m = mAccountActivityKlass.getMethod("getOAuthTokenType",
 					new Class[] {});
 			mOAuthTokenType = (String) m.invoke(null);
 
 			m = mAccountActivityKlass.getMethod("getOAuthSecretType",
 					new Class[] {});
 			mOAuthSecretType = (String) m.invoke(null);
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Bundle addAccount(AccountAuthenticatorResponse response,
 			String accountType, String authTokenType,
 			String[] requiredFeatures, Bundle options)
 			throws NetworkErrorException {
 		Log.v(TAG, "OAuth addAccount()");
 
 		Bundle result = new Bundle();
 
 		if (!accountType.equals(mAccountType)) {
 			result.putString(AccountManager.KEY_ERROR_MESSAGE, String.format(
 					"Invalid accountType sent to OAuth: %s", accountType));
 			return result;
 		}
 
 		// Purposefully ignoring requiredFeatures. OAuth has none.
 
 		addAuthActivityToBundle(response, authTokenType, result);
 		return result;
 	}
 
 	/**
 	 * @param response
 	 * @param authTokenType
 	 * @param bundle
 	 */
 	private void addAuthActivityToBundle(AccountAuthenticatorResponse response,
 			String authTokenType, Bundle bundle) {
 		Intent intent = new Intent(mContext, mAccountActivityKlass);
 		intent.putExtra(OAuthAccountActivity.PARAM_AUTHTOKEN_TYPE,
 				authTokenType);
 		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
 				response);
 
 		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
 			Account account, Bundle options) throws NetworkErrorException {
 		Log.v(TAG, "OAuth confirmCredentials()");
 
 		// There is no "confirm credentials" equivalent for OAuth, since
 		// everything is handled through the browser.
 		// TODO: return null; instead?
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Bundle editProperties(AccountAuthenticatorResponse response,
 			String accountType) {
 		Log.v(TAG, "OAuth editProperties()");
 
 		// Core OAuth has no properties. This may be overridden.
 		// TODO: should this send a user to some sort of OAuth browser page for
 		// the specific service?
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Bundle getAuthToken(AccountAuthenticatorResponse response,
 			Account account, String authTokenType, Bundle options)
 			throws NetworkErrorException {
 		Log.v(TAG, "OAuth getAuthToken()");
 
 		Bundle result = new Bundle();
 
 		if (!authTokenType.equals(mOAuthTokenType)
 				|| !authTokenType.equals(mOAuthSecretType)) {
 			result.putString(AccountManager.KEY_ERROR_MESSAGE, String.format(
 					"Invalid OAuth authTokenType: %s", authTokenType));
 			return result;
 		}
 
 		/*
 		 * OAuth by default has no way to re-request an authToken. The whole
 		 * multi-step workflow must be repeated. For particular OAuth providers
 		 * such as Facebook who provide extensions for expiring tokens, 
 		 * override this method with custom logic.
 		 */
 		addAuthActivityToBundle(response, authTokenType, result);
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getAuthTokenLabel(String authTokenType) {
 		Log.v(TAG, "OAuth getAuthTokenLabel()");
 		
 		if (authTokenType.equals(mOAuthTokenType)) {
 			return "OAuth Token";
 		} else if (authTokenType.equals(mOAuthSecretType)) {
 			return "OAuth Token Secret";
 		}
 
 		return "";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Bundle hasFeatures(AccountAuthenticatorResponse response,
 			Account account, String[] features) throws NetworkErrorException {
 		Log.v(TAG, "OAuth hasFeatues()");
 
 		// Features are definable per-authenticator. OAuth has none.
 		Bundle result = new Bundle();
 		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Bundle updateCredentials(AccountAuthenticatorResponse response,
 			Account account, String authTokenType, Bundle options)
 			throws NetworkErrorException {
 		Log.v(TAG, "OAuth updateCredentials()");
 
 		// Updating credentials makes no sense in the context of OAuth.
 		// TODO: return null; instead?
 		throw new UnsupportedOperationException();
 	}
 
 }
