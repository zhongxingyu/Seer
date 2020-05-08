 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.keychain;
 
 import android.accounts.AbstractAccountAuthenticator;
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorResponse;
 import android.accounts.AccountManager;
 import android.accounts.AccountsException;
 import android.accounts.NetworkErrorException;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.security.Credentials;
 import android.security.IKeyChainService;
 import android.security.KeyChain;
 import android.security.KeyStore;
 import android.util.Log;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.nio.charset.Charsets;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Collections;
 import javax.security.auth.x500.X500Principal;
 import libcore.io.Base64;
 import org.apache.harmony.xnet.provider.jsse.TrustedCertificateStore;
 
 public class KeyChainService extends Service {
 
     private static final String TAG = "KeyChainService";
 
     private AccountManager mAccountManager;
 
     private final Object mAccountLock = new Object();
     private Account mAccount;
 
     @Override public void onCreate() {
         super.onCreate();
         mAccountManager = AccountManager.get(this);
     }
 
     private final IKeyChainService.Stub mIKeyChainService = new IKeyChainService.Stub() {
 
         private final KeyStore mKeyStore = KeyStore.getInstance();
         private final TrustedCertificateStore mTrustedCertificateStore
                 = new TrustedCertificateStore();
 
         @Override public byte[] getPrivateKey(String alias, String authToken) {
             return getKeyStoreEntry(Credentials.USER_PRIVATE_KEY, alias, authToken);
         }
 
         @Override public byte[] getCertificate(String alias, String authToken) {
             return getKeyStoreEntry(Credentials.USER_CERTIFICATE, alias, authToken);
         }
 
         private byte[] getKeyStoreEntry(String type, String alias, String authToken) {
             if (alias == null) {
                 throw new NullPointerException("alias == null");
             }
             if (authToken == null) {
                 throw new NullPointerException("authtoken == null");
             }
             if (!isKeyStoreUnlocked()) {
                 throw new IllegalStateException("keystore locked");
             }
             String peekedAuthToken = mAccountManager.peekAuthToken(mAccount, alias);
             if (peekedAuthToken == null) {
                 throw new IllegalStateException("peekedAuthToken == null");
             }
             if (!peekedAuthToken.equals(authToken)) {
                 throw new IllegalStateException("authtoken mismatch");
             }
             String key = type + alias;
             byte[] bytes =  mKeyStore.get(key);
             if (bytes == null) {
                 return null;
             }
             return bytes;
         }
 
         private boolean isKeyStoreUnlocked() {
             return (mKeyStore.state() == KeyStore.State.UNLOCKED);
         }
 
         @Override public void installCaCertificate(byte[] caCertificate) {
             checkCertInstallerOrSystemCaller();
             try {
                 synchronized (mTrustedCertificateStore) {
                     mTrustedCertificateStore.installCertificate(parseCertificate(caCertificate));
                 }
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             } catch (CertificateException e) {
                 throw new IllegalStateException(e);
             }
         }
 
         private X509Certificate parseCertificate(byte[] bytes) throws CertificateException {
             CertificateFactory cf = CertificateFactory.getInstance("X.509");
             return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bytes));
         }
 
         @Override public boolean reset() {
             // only Settings should be able to reset
             checkSystemCaller();
             boolean ok = true;
 
             synchronized (mAccountLock) {
                // remote Accounts from AccountManager to revoke any
                 // granted credential grants to applications
                 Account[] accounts = mAccountManager.getAccountsByType(KeyChain.ACCOUNT_TYPE);
                 for (Account a : accounts) {
                     try {
                         if (!mAccountManager.removeAccount(a, null, null).getResult()) {
                             ok = false;
                         }
                     } catch (AccountsException e) {
                         Log.w(TAG, "Problem removing account " + a, e);
                         ok = false;
                     } catch (IOException e) {
                         Log.w(TAG, "Problem removing account " + a, e);
                         ok = false;
                     }
                 }
             }
 
             synchronized (mTrustedCertificateStore) {
                 // delete user-installed CA certs
                 for (String alias : mTrustedCertificateStore.aliases()) {
                     if (TrustedCertificateStore.isUser(alias)) {
                         if (!deleteCertificateEntry(alias)) {
                             ok = false;
                         }
                     }
                 }
                 return ok;
             }
         }
 
         @Override public boolean deleteCaCertificate(String alias) {
             // only Settings should be able to delete
             checkSystemCaller();
             return deleteCertificateEntry(alias);
         }
 
         private boolean deleteCertificateEntry(String alias) {
             try {
                 mTrustedCertificateStore.deleteCertificateEntry(alias);
                 return true;
             } catch (IOException e) {
                 Log.w(TAG, "Problem removing CA certificate " + alias, e);
                 return false;
             } catch (CertificateException e) {
                 Log.w(TAG, "Problem removing CA certificate " + alias, e);
                 return false;
             }
         }
 
         private void checkCertInstallerOrSystemCaller() {
             String actual = checkCaller("com.android.certinstaller");
             if (actual == null) {
                 return;
             }
             checkSystemCaller();
         }
         private void checkSystemCaller() {
             String actual = checkCaller("android.uid.system:1000");
             if (actual != null) {
                 throw new IllegalStateException(actual);
             }
         }
         /**
          * Returns null if actually caller is expected, otherwise return bad package to report
          */
         private String checkCaller(String expectedPackage) {
             String actualPackage = getPackageManager().getNameForUid(getCallingUid());
             return (!expectedPackage.equals(actualPackage)) ? actualPackage : null;
         }
     };
 
     private class KeyChainAccountAuthenticator extends AbstractAccountAuthenticator {
 
         /**
          * 264 was picked becuase it is the length in bytes of Google
          * authtokens which seems sufficiently long and guaranteed to
          * be storable by AccountManager.
          */
         private final int AUTHTOKEN_LENGTH = 264;
         private final SecureRandom mSecureRandom = new SecureRandom();
 
         private KeyChainAccountAuthenticator(Context context) {
             super(context);
         }
 
         @Override public Bundle editProperties(AccountAuthenticatorResponse response,
                                                String accountType) {
             return new Bundle();
         }
 
         @Override public Bundle addAccount(AccountAuthenticatorResponse response,
                                            String accountType,
                                            String authTokenType,
                                            String[] requiredFeatures,
                                            Bundle options) {
             Bundle result = new Bundle();
             result.putString(AccountManager.KEY_ACCOUNT_NAME, mAccount.name);
             result.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyChain.ACCOUNT_TYPE);
             return result;
         }
 
         @Override public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                                    Account account,
                                                    Bundle options) {
             Bundle result = new Bundle();
             result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
             return result;
         }
 
         /**
          * Called on an AccountManager cache miss, so generate a new value.
          */
         @Override public Bundle getAuthToken(AccountAuthenticatorResponse response,
                                              Account account,
                                              String authTokenType,
                                              Bundle options) {
             byte[] bytes = new byte[AUTHTOKEN_LENGTH];
             mSecureRandom.nextBytes(bytes);
             String authToken = Base64.encode(bytes);
             Bundle bundle = new Bundle();
             bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
             bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyChain.ACCOUNT_TYPE);
             bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
             return bundle;
         }
 
         @Override public String getAuthTokenLabel(String authTokenType) {
             // return authTokenType unchanged, it was a user specified
             // alias name, doesn't need to be localized
             return authTokenType;
         }
 
         @Override public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                                   Account account,
                                                   String authTokenType,
                                                   Bundle options) {
             Bundle bundle = new Bundle();
             bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
             bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyChain.ACCOUNT_TYPE);
             return bundle;
         }
 
         @Override public Bundle hasFeatures(AccountAuthenticatorResponse response,
                                             Account account,
                                             String[] features) {
             Bundle result = new Bundle();
             result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, features.length == 0);
             return result;
         }
     };
 
     private final IBinder mAuthenticator = new KeyChainAccountAuthenticator(this).getIBinder();
 
     @Override public IBinder onBind(Intent intent) {
         // ensure singleton keychain account exists for both
         // IKeyChainService and AbstractAccountAuthenticator
         synchronized (mAccountLock) {
             Account[] accounts = mAccountManager.getAccountsByType(KeyChain.ACCOUNT_TYPE);
             if (accounts.length == 0) {
                 mAccount = new Account(getResources().getString(R.string.app_name),
                                        KeyChain.ACCOUNT_TYPE);
                 mAccountManager.addAccountExplicitly(mAccount, null, null);
             } else if (accounts.length == 1) {
                 mAccount = accounts[0];
             } else {
                 throw new IllegalStateException();
             }
         }
         if (IKeyChainService.class.getName().equals(intent.getAction())) {
             return mIKeyChainService;
         }
         if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
             return mAuthenticator;
         }
         return null;
     }
 }
