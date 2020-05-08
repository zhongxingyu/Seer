 package com.pedrero.eclihand.ui;
 
 import com.pedrero.eclihand.service.UserService;
 import com.pedrero.eclihand.servlet.SpringVaadinServlet;
 import com.pedrero.eclihand.ui.window.EclihandMainWindow;
 import com.pedrero.eclihand.utils.text.LocaleContainer;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.UI;
 
 public class EclihandMainUI extends UI {
 
 	private final EclihandMainWindow eclihandMainWindow;
 
 	private final LocaleContainer localeContainer;
 
 	private final Authentication authentication;
 
 	private final UserService userService;
 
 	public EclihandMainUI() {
 		super();
 		eclihandMainWindow = SpringVaadinServlet.WEB_APPLICATION_CONTEXT
 				.getBean(EclihandMainWindow.class);
 		localeContainer = SpringVaadinServlet.WEB_APPLICATION_CONTEXT
 				.getBean(LocaleContainer.class);
 		authentication = SpringVaadinServlet.WEB_APPLICATION_CONTEXT
 				.getBean(Authentication.class);
 		userService = SpringVaadinServlet.WEB_APPLICATION_CONTEXT
 				.getBean(UserService.class);
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5162173188485477058L;
 
 	@Override
 	protected void init(VaadinRequest request) {
		setContent(eclihandMainWindow);
 		localeContainer.setLocale(request.getLocale());
 		authentication.setAuthenticatedUser(userService.retrieveGuestUser());
 	}
 
 }
