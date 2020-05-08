 package org.esupportail.smsuapiadmin.business;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.esupportail.commons.beans.AbstractApplicationAwareBean;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.smsuapiadmin.dao.DaoService;
 import org.esupportail.smsuapiadmin.dao.beans.UserBoSmsu;
 import org.esupportail.smsuapiadmin.domain.beans.EnumeratedFunction;
 import org.esupportail.smsuapiadmin.dto.DTOConverterService;
 import org.esupportail.smsuapiadmin.dto.beans.UIUser;
 
 /**
  * UserManager is the business layer between the web and the database for 'user'
  * objects.
  * 
  * @author MZRL3760
  * 
  */
 @SuppressWarnings("serial")
 public class UserManager extends AbstractApplicationAwareBean {
 
 	/**
 	 * Log4j logger.
 	 */
 	private final Logger logger = new LoggerImpl(getClass());
 
 	/**
 	 * {@link DaoService}.
 	 */
 	private DaoService daoService;
 
 	/**
 	 * {@link DTOConverterService}.
 	 */
 	private DTOConverterService dtoConverterService;
 
 	/**
 	 * isDeletable.
 	 */
 	private Boolean isDeletable;
 	
 	/**
 	 * isUpdateable.
 	 */
 	private Boolean isUpdateable;
 	
 	/**
 	 * constructor.
 	 */
 	public UserManager() {
 		super();
 	}
 
 	/**
 	 * @param daoService
 	 *            the daoService to set
 	 */
 	public void setDaoService(final DaoService daoService) {
 		this.daoService = daoService;
 	}
 
 	/**
 	 * Setter for 'dtoConverterService'.
 	 * 
 	 * @param dtoConverterService
 	 */
 	public void setDtoConverterService(
 			final DTOConverterService dtoConverterService) {
 		this.dtoConverterService = dtoConverterService;
 	}
 
 	/**
 	 * Returns the user with the specified id.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public UIUser getUserById(final Integer id) {
 		UIUser result = null;
 
 		logger.info("Recherche du user : id=" + id);
 		UserBoSmsu user = daoService.getUserById(id);
 		result = dtoConverterService.convertToUI(user);
 
 		return result;
 	}
 
 	/**
 	 * Returns the user with the specified login.
 	 * 
 	 * @param login
 	 * @return
 	 */
 	public UIUser getUserByLogin(final String login) {
 		UIUser result = null;
 
 		logger.info("Recherche du user : login=" + login);
 		UserBoSmsu user = daoService.getUserByLogin(login);
 		if (user != null) {
 			result = dtoConverterService.convertToUI(user);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns a list containing all users.
 	 * 
 	 * @return
 	 */
 	public List<UIUser> getUsers() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Retrieves the accounts from the database");
 		}
 		List<UIUser> allUIUsers = new ArrayList<UIUser>();
 		List<UserBoSmsu> allUsers = daoService.getUsers();
 
 		for (UserBoSmsu user : allUsers) {
 			UIUser ui = dtoConverterService.convertToUI(user);
 			allUIUsers.add(ui);
 		}
 		return allUIUsers;
 	}
 
 	/**
 	 * Returns a list containing all users.
 	 * 
 	 * @return
 	 */
 	public List<UIUser> getUsers(final UIUser currentUser) {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Retrieves the accounts from the database");
 		}
 		List<UIUser> allUIUsers = new ArrayList<UIUser>();
 		List<UserBoSmsu> allUsers = daoService.getUsers();
 
 		for (UserBoSmsu user : allUsers) {
 			isDeletable = true;
 			isUpdateable = true;
 		
 			UIUser ui = dtoConverterService.convertToUI(user);
 			
 			if (currentUser != null) {
 				if (ui.getLogin().trim().equals(currentUser.getLogin().trim())) {
 				isDeletable = false;
 				isUpdateable = false;
 				}
 			}
 			
 			ui.setIsDeletable(isDeletable);
 			ui.setIsUpdateable(isUpdateable);
 		
 			allUIUsers.add(ui);
 		}
 		return allUIUsers;
 	}
 
 	/**
 	 * Updates the user.
 	 * 
 	 * @param uiUser
 	 */
 	public void updateUser(final UIUser uiUser) {
 
 		UserBoSmsu user = dtoConverterService.convertFromUI(uiUser, false);
 
 		String idStr = uiUser.getId();
 		Integer id = Integer.valueOf(idStr);
 
 		UserBoSmsu userPersistent = daoService.getUserById(id);
 		userPersistent.setLogin(uiUser.getLogin());
 		userPersistent.setRole(user.getRole());
 
 		daoService.updateUser(userPersistent);
 	}
 
 	/**
 	 * Adds the user in database.
 	 * 
 	 * @param uiUser
 	 */
 	public void addUser(final UIUser uiUser) {
 		UserBoSmsu user = dtoConverterService.convertFromUI(uiUser, true);
 		daoService.addUser(user);
 	}
 
 	/**
 	 * Deletes the user from the database.
 	 * 
 	 * @param uiUser
 	 */
 	public void delete(int id) {
 		UserBoSmsu userPersistent = daoService.getUserById(id);
 		daoService.deleteUser(userPersistent);
 	}
 
 	/**
 	 * Returns true if the login is already used.
 	 * 
 	 * @param login
 	 * @return
 	 */
 	public boolean loginAlreadyUsed(final String login) {
 		UIUser user = getUserByLogin(login);
 		return user != null;
 	}
 
 	public Set<EnumeratedFunction> getUserFunctions(String login) {
 		UserBoSmsu user = daoService.getUserByLogin(login);
		if (user == null) return new java.util.TreeSet<EnumeratedFunction>();
 		return dtoConverterService.convertToEnum(user.getRole().getFonctions());
 	}
 
 }
