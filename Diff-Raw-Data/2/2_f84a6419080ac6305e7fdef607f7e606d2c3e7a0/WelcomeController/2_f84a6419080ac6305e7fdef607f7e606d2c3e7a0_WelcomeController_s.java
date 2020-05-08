 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-blank
  */
 package org.esupportail.example.web.controllers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.services.urlGeneration.UrlGenerator;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.example.domain.beans.User;
 import org.esupportail.example.web.beans.UserBean;
 import org.esupportail.example.web.beans.UserPaginator;
 import org.esupportail.example.web.utils.NavigationRulesConst;
 
 
 /**
  * A visual bean for the welcome page.
  */
 public class WelcomeController  extends AbstractContextAwareController {
 
 	/*
 	 ******************* PROPERTIES ******************** */
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = -239570715531002003L;
 
 	/**
 	 * A logger.
 	 */
 	private final Logger logger = new LoggerImpl(this.getClass());
 
 	/**
 	 * Name.
 	 */
 	private String name;
 	
 
 	/**
 	 * The user.
 	 */
 	private UserBean userToUpdate;
 
 	/**
 	 * The user paginator used to display users informations
 	 */
 	private UserPaginator userPaginator;
 	
 	/**
 	 * see {@link UrlGenerator}.
 	 */
 	private UrlGenerator urlGenerator;
 	
 
 	/*
 	 ******************* INIT ******************** */
 
 	/**
 	 * Bean constructor.
 	 */
 	public WelcomeController() {
 		super();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + "#" + hashCode();
 	}
 
 	/**
 	 * @see org.esupportail.example.web.controllers.AbstractDomainAwareBean#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		name = "toto";
 		
 	}
 
 	/**
 	 * @see org.esupportail.example.web.controllers.AbstractContextAwareController#afterPropertiesSetInternal()
 	 */
 	@Override
 	public void afterPropertiesSetInternal() {
 		super.afterPropertiesSetInternal();
 		Assert.notNull(this.userPaginator, "property userPaginator of class " 
 				+ this.getClass().getName() + " can not be null");
 		Assert.notNull(this.urlGenerator, "property urlGenerator of class " 
 				+ this.getClass().getName() + " can not be null");
 		Assert.notNull(this.userToUpdate, "property userToUpdate of class " 
 				+ this.getClass().getName() + " can not be null");
 		userPaginator.forceReload();
 	}
 
 	/*
 	 ******************* CALLBACK ******************** */
 
 	/**
 	 * @return String
 	 */
 	public String goCasDemo() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("entering goCasDemo return " + NavigationRulesConst.CAS_DEMO);
 		}
 		return NavigationRulesConst.CAS_DEMO;
 	}
 
 
 	/**
 	 * @return String
 	 */
 	public String goExceptionDemo() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("entering goExceptionDemo return " + NavigationRulesConst.EXCEPTION_DEMO);
 		}
 		return NavigationRulesConst.EXCEPTION_DEMO;
 	}
 
 	/**
 	 * @return String
 	 */
 	public String goJpaDemo() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("entering goJpaDemo return " + NavigationRulesConst.JPA_DEMO);
 		}
 		
 		return NavigationRulesConst.JPA_DEMO;
 	}
 
 	/**
 	 * @return String
 	 */
 	public String goLinkDemo() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("entering goLinkDemo return " + NavigationRulesConst.LINK_DEMO);
 		}
 		return NavigationRulesConst.LINK_DEMO;
 	}
 
 	/*
 	 ******************* METHODS ******************** */
 
 	/* **********************************
 	 * BEGIN TO DEMO DEEPLINKING
 	 ************************************ */
 
 	/**
 	 * TEST to deepLinking.
 	 * @param name
 	 */
 	public void initName(final String name) {
 		this.name = name;
 	}
 
 	/**
 	 * TEST to deepLinking2.
 	 * @param name
 	 */
 	public String goTest(final String name) {
 		initName(name);
 		return "go_deeplinkingTest";
 	}
 	
 	/**
 	 * The url to test 1
 	 * @return String
 	 */
 	public String getUrlTest1() {
 		String url = "";
 		Map<String, String> param = new HashMap<String, String>();
 		param.put("name", "cleprous");
 		url = urlGenerator.guestUrl(param);
 		return url;
 	}
 	
 	/**
 	 * The url to test 1
 	 * @return String
 	 */
 	public String getUrlTest2() {
 		String url = "";
 		Map<String, String> param = new HashMap<String, String>();
 		param.put("name2", "cleprous");
 		url = urlGenerator.guestUrl(param);
 		return url;
 	}
 	
 
 	/* **********************************
 	 * END TO DEMO DEEPLINKING
 	 ************************************ */
 
 	/* **********************************
 	 * BEGIN TO DEMO EXCEPTION HANDLER
 	 ************************************ */
 
 
 
 	/**
 	 * Test ExceptionHandler JSF.
 	 */
 	public void testException() {
 		throw new ConfigException("testException");
 	}
 
 	/* **********************************
 	 * BEGIN TO DEMO EXCEPTION HANDLER
 	 ************************************ */
 
 
 	/* **********************************
 	 * BEGIN TO DEMO JPA
 	 ************************************ */
 
 
 	/**
 	 * Delete the user.
 	 */
 	public void deleteUser() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("entering deleteUser with userToUpdate = " + userToUpdate);
 		}
 		User u = new User();
 		u.setId(userToUpdate.getId());
 		u.setDisplayName(userToUpdate.getDisplayName());
 		getDomainService().deleteUser(u);
 	}
 
 	/**
 	 * @param userToDelete the userToDelete to set
 	 */
 	public void setUserToDelete(final User userToDelete) {
 		userToUpdate = new UserBean();
 		userToUpdate.setId(userToDelete.getId());
 		userToUpdate.setDisplayName(userToDelete.getDisplayName());
 	}
 	
 	/**
 	 * Add the user.
 	 */
 	public void addUser() {
 		if (logger.isDebugEnabled()) {
 			logger.debug("entering addUser with userToUpdate = " + userToUpdate);
 		}
 		User u = new User();
 		u.setId(userToUpdate.getId());
 		u.setDisplayName(userToUpdate.getDisplayName());
 		u.setLanguage("fr");
 		u.setAdmin(false);
 		getDomainService().addUser(u);
 		userToUpdate.reset();
 		userPaginator.forceReload();
 		addInfoMessage(null, "INFO.ENTER.SUCCESS");
 	}
 
 
 	/* **********************************
 	 * END TO DEMO JPA
 	 ************************************ */
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		logger.info("entering name");
 		return name;
 	}	
 
 
 	/**
 	 * @return
 	 */
 	public String getCurrentUserId() {
 		return getCurrentUser().getId();
 	}
 
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
 	/**
 	 * @return User
 	 */
 	
 	public List<User> getUsers() {
 		return userPaginator.getVisibleItems();
 	}
 
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 
 
 	/**
 	 * @return the userPaginator
 	 */
 	public UserPaginator getUserPaginator() {
 		return userPaginator;
 	}
 
 	/**
 	 * @param userPaginator the userPaginator to set
 	 */
 	public void setUserPaginator(final UserPaginator userPaginator) {
 		this.userPaginator = userPaginator;
 	}
 
 
 	/**
 	 * @param urlGenerator the urlGenerator to set
 	 */
 	public void setUrlGenerator(final UrlGenerator urlGenerator) {
 		this.urlGenerator = urlGenerator;
 	}
 
 	/**
 	 * @return the userToUpdate
 	 */
 	public UserBean getUserToUpdate() {
 		return userToUpdate;
 	}
 
 	/**
 	 * @param userToUpdate the userToUpdate to set
 	 */
 	public void setUserToUpdate(final UserBean userToUpdate) {
 		this.userToUpdate = userToUpdate;
 	}
 
 
 }
