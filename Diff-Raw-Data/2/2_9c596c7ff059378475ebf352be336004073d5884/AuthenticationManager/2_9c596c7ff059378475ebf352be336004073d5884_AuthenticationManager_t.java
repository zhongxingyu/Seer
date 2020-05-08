 package com.mymed.controller.core.manager.authentication;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * The manager for the authentication bean
  * 
  * @author lvanni
  * @author Milo Casagrande
  * 
  */
 public class AuthenticationManager extends AbstractManager implements IAuthenticationManager {
 
 	private static final String LOGIN_COLUMN = "login";
 	private static final String ENCODING = "UTF8";
 
 	public AuthenticationManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 
 	public AuthenticationManager(final StorageManager storageManager) throws InternalBackEndException {
 		super(storageManager);
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see IAuthenticationManager#create(MUserBean, MAuthenticationBean)
 	 */
 	@Override
 	public MUserBean create(final MUserBean user, final MAuthenticationBean authentication)
 	        throws InternalBackEndException, IOBackEndException {
 
 		final ProfileManager profileManager = new ProfileManager(storageManager);
 		profileManager.create(user);
 
 		try {
 			read(authentication.getLogin(), authentication.getPassword());
 		} catch (final IOBackEndException e) {
 			if (e.getStatus() == 404) { // only if the user does not exist
 				storageManager.insertSlice(CF_AUTHENTICATION, LOGIN_COLUMN, authentication.getAttributeToMap());
 
 				try {
 					final Map<String, byte[]> authMap = authentication.getAttributeToMap();
 					storageManager.insertSlice(CF_AUTHENTICATION, new String(authMap.get(LOGIN_COLUMN), ENCODING),
 					        authMap);
 				} catch (final UnsupportedEncodingException ex) {
 					throw new InternalBackEndException(ex.getMessage());
 				}
 
 				return user;
 			}
 		}
 
 		throw new IOBackEndException("The login already exist!", 409);
 	}
 
 	/**
 	 * @see IAuthenticationManager#read(String, String)
 	 */
 	@Override
 	public MUserBean read(final String login, final String password) throws InternalBackEndException,
 	        IOBackEndException {
 
 		final Map<byte[], byte[]> args = storageManager.selectAll(CF_AUTHENTICATION, login);
 		final MAuthenticationBean authentication = (MAuthenticationBean) introspection(new MAuthenticationBean(), args);
 
 		if (authentication.getLogin().equals("")) {
 			throw new IOBackEndException("the login does not exist!", 404);
 		} else if (!authentication.getPassword().equals(password)) {
 			throw new IOBackEndException("Wrong password", 403);
 		}
 
		return new ProfileManager(storageManager).read(authentication.getUser());
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see IAuthenticationManager#update(MAuthenticationBean)
 	 */
 	@Override
 	public void update(final String id, final MAuthenticationBean authentication) throws InternalBackEndException,
 	        IOBackEndException {
 		// Remove the old Authentication (the login/key can be changed)
 		storageManager.removeAll(CF_AUTHENTICATION, id);
 		// Insert the new Authentication
 		storageManager.insertSlice(CF_AUTHENTICATION, LOGIN_COLUMN, authentication.getAttributeToMap());
 
 		try {
 			final Map<String, byte[]> authMap = authentication.getAttributeToMap();
 			storageManager.insertSlice(CF_AUTHENTICATION, new String(authMap.get(LOGIN_COLUMN), ENCODING), authMap);
 		} catch (final UnsupportedEncodingException ex) {
 			throw new InternalBackEndException(ex.getMessage());
 		}
 	}
 }
