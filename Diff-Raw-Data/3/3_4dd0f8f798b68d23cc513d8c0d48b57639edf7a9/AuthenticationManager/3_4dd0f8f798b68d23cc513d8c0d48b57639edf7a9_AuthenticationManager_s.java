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
 
 		insertColumn(authentication);
 
 		return user;
 	}
 
 	/**
 	 * @see IAuthenticationManager#read(String, String)
 	 */
 	@Override
 	public MUserBean read(final String login, final String password) throws InternalBackEndException,
 	        IOBackEndException {
 		final Map<byte[], byte[]> args = storageManager.selectAll(CF_AUTHENTICATION, login);
 		MAuthenticationBean authentication = new MAuthenticationBean();
 
 		authentication = (MAuthenticationBean) introspection(authentication, args);
 
 		if (authentication.getPassword().equals(password)) {
			return new ProfileManager().read(authentication.getUser());
 		} else {
 			throw new IOBackEndException("Wrong password");
 		}
 	}
 
 	/**
 	 * @see IAuthenticationManager#update(MAuthenticationBean)
 	 */
 	@Override
 	public void update(final MAuthenticationBean authentication) throws InternalBackEndException {
 		insertColumn(authentication);
 	}
 
 	/**
 	 * Internally used to perform the real insertion into the database
 	 * 
 	 * @param authentication
 	 * @throws InternalBackEndException
 	 */
 	private void insertColumn(final MAuthenticationBean authentication) throws InternalBackEndException {
 		try {
 			final Map<String, byte[]> authMap = authentication.getAttributeToMap();
 			storageManager.insertSlice(CF_AUTHENTICATION, new String(authMap.get("login"), "UTF8"), authMap);
 		} catch (final UnsupportedEncodingException ex) {
 			throw new InternalBackEndException(ex.getMessage());
 		}
 	}
 }
