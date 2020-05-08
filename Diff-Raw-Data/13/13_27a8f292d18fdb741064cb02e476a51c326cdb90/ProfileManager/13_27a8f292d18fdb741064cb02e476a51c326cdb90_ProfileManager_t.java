 package com.mymed.controller.core.manager.profile;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MLogger;
 
 /**
  * Manage an user profile
  * 
  * @author lvanni
  * 
  */
 public class ProfileManager extends AbstractManager implements IProfileManager {
 
 	private static final String ENCODING = "UTF8";
 
 	public ProfileManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 
 	public ProfileManager(final IStorageManager storageManager) throws InternalBackEndException {
 		super(storageManager);
 	}
 
 	/**
 	 * Setup a new user profile into the database
 	 * 
 	 * @param user
 	 *            the user to insert into the database
 	 * @throws IOBackEndException
 	 */
 	@Override
 	public MUserBean create(final MUserBean user) throws InternalBackEndException, IOBackEndException {
 		try {
 			final Map<String, byte[]> args = user.getAttributeToMap();
 			storageManager.insertSlice(CF_USER, new String(args.get("id"), ENCODING), args);
 
 			return user;
 		} catch (final UnsupportedEncodingException e) {
 			MLogger.getLog().info("Error in string conversion using {} encoding", ENCODING);
 			MLogger.getDebugLog().debug("Error in string conversion using {} encoding", ENCODING, e.getCause());
 
 			throw new InternalBackEndException(e.getMessage());
 		}
 	}
 
 	/**
 	 * @param id
 	 *            the id of the user
 	 * @return the User corresponding to the id
 	 * @throws InternalBackEndException
 	 * @throws IOBackEndException
 	 */
 	@Override
 	public MUserBean read(final String id) throws InternalBackEndException, IOBackEndException {
 		final MUserBean user = new MUserBean();
 		final Map<byte[], byte[]> args = storageManager.selectAll(CF_USER, id);
 
 		if (args.isEmpty()) {
 			MLogger.getLog().info("User with ID '{}' does not exists", id);
 			throw new IOBackEndException("profile does not exist!", 404);
 		}
 
 		return (MUserBean) introspection(user, args);
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see IProfileManager#update(MUserBean)
 	 */
 	@Override
	public MUserBean update(final MUserBean user) throws InternalBackEndException, IOBackEndException {
 		MLogger.getLog().info("Updating user with ID '{}'", user.getId());
 		final MUserBean userToUpdate = read(user.getId());
 		userToUpdate.update(user);
 		// TODO Implement the update method witch use the wrapper updateColumn
 		// method
 
 		// create(user) will replace the current values of the user...
 		create(userToUpdate);
		return userToUpdate;
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see IProfileManager#delete(MUserBean)
 	 */
 	@Override
 	public void delete(final String id) throws InternalBackEndException, IOBackEndException {
 		final MUserBean user = read(id);
 		storageManager.removeAll(CF_USER, id);
 
 		if (user.getSocialNetworkID().equals("MYMED")) {
 			storageManager.removeAll(CF_AUTHENTICATION, user.getLogin());
 		}
 	}
 }
