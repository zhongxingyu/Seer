 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rdonasco.security.app.controllers;
 
 import com.rdonasco.common.vaadin.controller.ApplicationPopupProvider;
 import com.rdonasco.common.vaadin.controller.ApplicationExceptionPopupProvider;
 import com.rdonasco.common.i18.I18NResource;
 import com.rdonasco.common.vaadin.view.utils.NotificationHelper;
 import com.rdonasco.config.services.ConfigDataManagerVODecoratorRemote;
 import com.rdonasco.security.app.themes.SecurityDefaultTheme;
 import com.rdonasco.security.home.controllers.HomeFrameViewController;
 import com.rdonasco.security.authorization.interceptors.SecurityExceptionHandler;
 import com.rdonasco.security.authentication.services.LoggedOnSession;
 import com.rdonasco.security.authentication.services.LoggedOnSessionProvider;
 import com.rdonasco.security.services.SystemSecurityInitializerRemote;
 import com.rdonasco.security.utils.SecurityConstants;
 import com.rdonasco.security.vo.ApplicationVO;
 import com.vaadin.Application;
 import com.vaadin.terminal.gwt.server.WebApplicationContext;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PreDestroy;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Roy F. Donasco
  */
 @SessionScoped
 public class SecurityApplication extends Application implements
 		ApplicationExceptionPopupProvider, ApplicationPopupProvider,
 		HttpSessionProvider, LoggedOnSessionProvider, SecurityExceptionHandler
 {
 
 	private static final Logger LOG = Logger.getLogger(SecurityApplication.class.getName());
 	private static final long serialVersionUID = 1L;
 	
 	static
 	{
 		I18NResource.setBundle(java.util.ResourceBundle.getBundle("com/rdonasco/security/i18n/i18nResource"));
 	}
 	@Inject
 	private HomeFrameViewController homeFrameController;
 	private NotificationHelper notificationHelper;
 
 	@Inject
 	private LoggedOnSession loggedOnSession;
 	
 	@EJB
 	private ConfigDataManagerVODecoratorRemote configDataManager;	
 	@EJB
 	private SystemSecurityInitializerRemote systemSecurityInitializer;
 
 	@Override
 	public void init()
 	{
 		notificationHelper = new NotificationHelper();
 		notificationHelper.setApplication(this);
 		LOG.log(Level.INFO, "current theme: ", getTheme());
 		Window mainWindow = new Window("rdonasco Security");
 		LOG.log(Level.INFO, "new session: {0}", getSession().getId());
 		mainWindow.addStyleName(SecurityDefaultTheme.CSS_MAIN_WINDOW);
 		setMainWindow(mainWindow);
 		mainWindow.addComponent(homeFrameController.getControlledView());
 		((VerticalLayout) mainWindow.getContent()).setMargin(false);
 		setTheme(SecurityDefaultTheme.THEME_NAME);
 	}
 
 	@Override
 	public void popUpInfoException(Throwable t)
 	{
 		Throwable cause = extractRealCause(t);
 		LOG.log(Level.INFO, cause.getMessage(), cause);
 		popUpInfo(cause.getMessage());
 	}
 
 	@Override
 	public void popUpErrorException(Throwable t)
 	{
 		Throwable cause = extractRealCause(t);
 		LOG.log(Level.SEVERE, cause.getMessage(), cause);
 		popUpError(cause.getMessage());
 	}
 
 	@Override
 	public void popUpWarningException(Throwable t)
 	{
 		Throwable cause = extractRealCause(t);
 		LOG.log(Level.WARNING, cause.getMessage(), cause);
 		popUpWarning(cause.getMessage());
 
 	}
 
 	@Override
 	public void popUpDebugException(Throwable t)
 	{
 		Throwable cause = extractRealCause(t);
 		LOG.log(Level.FINE, cause.getMessage(), cause);
 		popUpInfo(cause.getMessage());
 
 	}
 
 	@Override
 	public void popUpInfo(String infoMessage)
 	{
 		notificationHelper.showHumanNotification(infoMessage);
 	}
 
 	@Override
 	public void popUpError(String errorMessage)
 	{
 		notificationHelper.showHumanErrorNotification(errorMessage);
 	}
 
 	@Override
 	public void popUpWarning(String warningMessage)
 	{
 		notificationHelper.showHumanWarningNotification(warningMessage);
 	}
 
 	@Override
 	public void showTrayNotification(String trayMessage)
 	{
 		notificationHelper.showTrayNotification(trayMessage);
 	}
 
 	@Override
 	public HttpSession getSession()
 	{
 		WebApplicationContext context = (WebApplicationContext) getContext();
 		return context.getHttpSession();
 	}
 
 	@PreDestroy
 	public void beforeDestroy()
 	{
 		((WebApplicationContext) getContext()).valueUnbound(null);
 		getSession().invalidate();
 	}
 
 	@Override
 	public LoggedOnSession getLoggedOnSession()
 	{
 		if(loggedOnSession.getApplicationID() == null)
 		{
 			try
 			{
 				loggedOnSession.setApplicationID(configDataManager.loadValue(SecurityConstants.CONFIG_SYSTEM_APPLICATION_ID, Long.class, null));
 				loggedOnSession.setApplicationToken(configDataManager.loadValue(SecurityConstants.CONFIG_SYSTEM_APPLICATION_TOKEN, String.class, null));
 			}
 			catch (Exception ex)
 			{
 				popUpWarningException(ex);
 			}
 		}
 		return loggedOnSession;
 	}
 
 	@Override
 	public void handleSecurityException(Throwable e)
 	{
 		popUpErrorException(e);
 	}
 
 	private Throwable extractRealCause(Throwable throwable)
 	{
 		Throwable cause = throwable;
 		if (throwable.getCause() != null)
 		{
 			cause = throwable.getCause();
 		}
 		return cause;
 	}
 }
