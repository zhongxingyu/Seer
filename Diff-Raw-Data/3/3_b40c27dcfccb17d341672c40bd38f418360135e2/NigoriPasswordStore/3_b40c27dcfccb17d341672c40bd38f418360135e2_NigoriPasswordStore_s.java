 /*
  * Copyright 2011 Miltiadis Allamanis
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
  
 package uk.ac.cam.cl.passgori;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.nigori.client.NigoriCryptographyException;
 import com.google.nigori.client.NigoriDatastore;
 
 /**
  * 
  */
 
 /**
  * A Nigori Password Store.
  * 
  * The passwords are kept in a double linked list on the nigori password store.
  * Each user has associated a head of each list.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class NigoriPasswordStore implements IPasswordStore {
 
 	/**
 	 * The Nigori client instance.
 	 */
 	private NigoriDatastore mNigoriStore;
 
 	/**
 	 * The username of the nigori password store
 	 */
 	private final String mUserName;
 
 	private final String mServerPrefix;
 
 	private final String mServerURI;
 
 	private final int mPortNumber;
 
 	/**
 	 * Prefixes for storing keys.
 	 */
 	private static final String USERNAME_PREFIX = "_username";
 	private static final String PASSWORD_PREFIX = "_password";
 	private static final String NOTES_PREFIX = "_notes";
 	private static final String NEXT_PREFIX = "_next";
 	private static final String PREV_PREFIX = "_prev";
 	private static final String PASS_HEAD_PREFIX = "_passHead";
 
 	/**
 	 * Constructor that creates the store but performs no authorization.
 	 * 
 	 * @param serverURI
 	 *            the URI of the server
 	 * @param portNumber
 	 *            the port number on the Nigori server
 	 * @param serverPrefix
 	 *            a server prefix
 	 */
 	public NigoriPasswordStore(final String username, final String serverURI,
 			final int portNumber, final String serverPrefix) {
 		mPortNumber = portNumber;
 		mServerPrefix = serverPrefix;
 		mServerURI = serverURI;
 		mUserName = username;
 	}
 
 	/**
 	 * Nigori Password Store Constructor, that automatically performs
 	 * authorization.
 	 * 
 	 * @param username
 	 *            the username of the Nigori server
 	 * @param password
 	 *            the password of the Nigori user
 	 * @param serverURI
 	 *            the URI of the server
 	 * @param portNumber
 	 *            the port number on the Nigori server
 	 * @param serverPrefix
 	 *            a server prefix
 	 * @throws PasswordStoreException
 	 */
 	public NigoriPasswordStore(final String username, final String password,
 			final String serverURI, final int portNumber,
 			final String serverPrefix) throws PasswordStoreException {
 
 		mPortNumber = portNumber;
 		mServerPrefix = serverPrefix;
 		mServerURI = serverURI;
 		mUserName = username;
 
 		authorize(mUserName, password);
 
 	}
 
 	@Override
 	public boolean authorize(String username, String password)
 			throws PasswordStoreException {
 		boolean authenticated = false;
 		try {
 			mNigoriStore = new NigoriDatastore(mServerURI, mPortNumber,
 					mServerPrefix, username, password);
 
 			authenticated = mNigoriStore.authenticate();
 			if (!authenticated)
 				authenticated = register();
 		} catch (Exception e) {
 			throw new PasswordStoreException(e.getMessage());
 		}
 
 		return authenticated;
 	}
 
 	@Override
 	public List<String> getAllStoredPasswordIds() throws PasswordStoreException {
 		AbstractList<String> passwordIds = new ArrayList<String>();
 
 		byte[] response = null;
 		try {
 			// Try getting the head of the password list
 			response = mNigoriStore.get(getPassHeadKey().getBytes());
 		} catch (IOException e) {
 			throw new PasswordStoreException(e.getMessage());
 		} catch (NigoriCryptographyException e) {
 			throw new PasswordStoreException(e.getMessage());
 		}
 
 		if (response != null) {
 			// a head exists, so retrieve the list!
 			String passHeadId = new String(response);
 
 			// recursively get all heads from the list
 			while (passHeadId != null) {
 				passwordIds.add(passHeadId);
 				final String nextHead = getNextElement(passHeadId);
				if (nextHead == passHeadId) {
 					throw new PasswordStoreException("Password Store Corrupt");
 				}
 				passHeadId = getNextElement(passHeadId);
 			}
 		}
 		return passwordIds;
 	}
 
 	@Override
 	public boolean removePassword(String aId) throws PasswordStoreException {
 		try {
 			// Get Password's previous and next (if any)
 			final byte[] previousId = mNigoriStore.get(getPrevKey(aId)
 					.getBytes());
 			final byte[] nextId = mNigoriStore.get(getNextKey(aId).getBytes());
 
 			// Update neighboring linked-list items
 			if ((previousId != null) && (nextId != null)) {
 				mNigoriStore.put(getNextKey(new String(previousId)).getBytes(),
 						nextId);
 				mNigoriStore.put(getPrevKey(new String(nextId)).getBytes(),
 						previousId);
 			} else if ((nextId == null) && (previousId == null)) {
 				mNigoriStore.delete(getPassHeadKey().getBytes());
 			} else if (nextId == null) {
 				mNigoriStore.delete(getNextKey(new String(previousId))
 						.getBytes());
 			} else if (previousId == null) {
 				mNigoriStore.delete(getPrevKey(new String(nextId)).getBytes());
 				mNigoriStore.put(getPassHeadKey().getBytes(), nextId);
 			}
 
 			return (mNigoriStore.delete(getUsernameKey(aId).getBytes())
 					&& mNigoriStore.delete(getPasswordKey(aId).getBytes()) && mNigoriStore
 						.delete(getNotesKey(aId).getBytes()));
 
 		} catch (UnsupportedEncodingException e) {
 			throw new PasswordStoreException(e.getMessage());
 		} catch (NigoriCryptographyException e) {
 			throw new PasswordStoreException(e.getMessage());
 		} catch (IOException e) {
 			throw new PasswordStoreException(e.getMessage());
 		}
 
 	}
 
 	@Override
 	public Password retrivePassword(String aId) throws PasswordStoreException {
 
 		byte[] username = null;
 		byte[] password = null;
 		byte[] notes = null;
 		try {
 			username = mNigoriStore.get(getUsernameKey(aId).getBytes());
 			password = mNigoriStore.get(getPasswordKey(aId).getBytes());
 			notes = mNigoriStore.get(getNotesKey(aId).getBytes());
 		} catch (IOException e) {
 			throw new PasswordStoreException(e.getMessage());
 		} catch (NigoriCryptographyException e) {
 			throw new PasswordStoreException(e.getMessage());
 		}
 
 		if ((username != null) && (password != null) && (notes != null)) {
 			return new Password(aId, new String(username),
 					new String(password), new String(notes));
 		}
 		return null;
 	}
 
 	@Override
 	public boolean storePassword(Password aPassword)
 			throws PasswordStoreException {
 		try {
 			final byte[] oldHeadBytes = mNigoriStore.get(getPassHeadKey()
 					.getBytes());
 			if (oldHeadBytes != null) {
 				// Store next on linked list
 				mNigoriStore.put(getNextKey(aPassword.getId()).getBytes(),
 						oldHeadBytes);
 
 				String oldHead = new String(oldHeadBytes);
 				// Store prev key
 				mNigoriStore.put(getPrevKey(oldHead).getBytes(), aPassword
 						.getId().getBytes());
 			}
 
 			// Store username
 			mNigoriStore.put(getUsernameKey(aPassword.getId()).getBytes(),
 					aPassword.getUsername().getBytes());
 
 			// Store password
 			mNigoriStore.put(getPasswordKey(aPassword.getId()).getBytes(),
 					aPassword.getPassword().getBytes());
 
 			// Store password
 			mNigoriStore.put(getNotesKey(aPassword.getId()).getBytes(),
 					aPassword.getNotes().getBytes());
 
 			// Store next key
 			mNigoriStore.put(getPassHeadKey().getBytes(), aPassword.getId()
 					.getBytes());
 
 		} catch (IOException e) {
 			throw new PasswordStoreException(e.getMessage());
 		} catch (NigoriCryptographyException e) {
 			throw new PasswordStoreException(e.getMessage());
 		}
 		return true;
 	}
 
 	/**
 	 * Returns the next element of the linked list, given the current element
 	 * 
 	 * @param currentElement
 	 * @return
 	 * @throws PasswordStoreException
 	 */
 	private String getNextElement(final String currentElement)
 			throws PasswordStoreException {
 		final String key = getNextKey(currentElement);
 		byte[] response = null;
 		try {
 			response = mNigoriStore.get(key.getBytes());
 		} catch (IOException e) {
 			throw new PasswordStoreException(e.getMessage());
 		} catch (NigoriCryptographyException e) {
 			throw new PasswordStoreException(e.getMessage());
 		}
 		if (response == null)
 			return null;
 		return new String(response);
 	}
 
 	/**
 	 * Returns the key which is used to store the next's password id.
 	 * 
 	 * @param passwordId
 	 *            the current password id
 	 * @return a string representing the key
 	 */
 	private final String getNextKey(final String passwordId) {
 		return mUserName + "_" + passwordId + NEXT_PREFIX;
 	}
 
 	/**
 	 * Returns the key which is used to store the note of the current password.
 	 * 
 	 * @param passwordId
 	 *            the current password id
 	 * @return a string representing the key
 	 */
 	private final String getNotesKey(final String passwordId) {
 		return mUserName + "_" + passwordId + NOTES_PREFIX;
 	}
 
 	/**
 	 * Returns the key which is used to store the password linked list head.
 	 * 
 	 * @return a string representing the key
 	 */
 	private final String getPassHeadKey() {
 		return mUserName + PASS_HEAD_PREFIX;
 	}
 
 	/**
 	 * Returns the key which is used to store the password of the current
 	 * password.
 	 * 
 	 * @param passwordId
 	 *            the current password id
 	 * @return a string representing the key
 	 */
 	private final String getPasswordKey(final String passwordId) {
 		return mUserName + "_" + passwordId + PASSWORD_PREFIX;
 	}
 
 	/**
 	 * Returns the key which is used to store the previous' password id.
 	 * 
 	 * @param passwordId
 	 *            the current password id
 	 * @return a string representing the key
 	 */
 	private final String getPrevKey(final String passwordId) {
 		return mUserName + "_" + passwordId + PREV_PREFIX;
 	}
 
 	/**
 	 * Returns the key which is used to store the username of the current
 	 * password.
 	 * 
 	 * @param passwordId
 	 *            the current password id
 	 * @return a string representing the key
 	 */
 	private final String getUsernameKey(final String passwordId) {
 		return mUserName + "_" + passwordId + USERNAME_PREFIX;
 	}
 
 	/**
 	 * Register the user.
 	 * 
 	 * @return
 	 * @throws IOException
 	 * @throws NigoriCryptographyException
 	 */
 	private boolean register() throws IOException, NigoriCryptographyException {
 		return mNigoriStore.register();
 	}
 
 }
