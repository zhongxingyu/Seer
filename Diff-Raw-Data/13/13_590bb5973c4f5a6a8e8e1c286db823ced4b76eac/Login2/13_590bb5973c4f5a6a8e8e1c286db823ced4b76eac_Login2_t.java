 /*
 * $Id: Login2.java,v 1.27 2007/11/01 17:43:09 laddi Exp $ Created on 7.3.2005
  * in project com.idega.block.login
  * 
  * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
  * 
  * This software is the proprietary information of Idega hf. Use is subject to
  * license terms.
  */
 package com.idega.block.login.presentation;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 import com.idega.core.accesscontrol.business.LoginBusinessBean;
 import com.idega.core.accesscontrol.business.LoginState;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.Layer;
 import com.idega.presentation.PresentationObject;
 import com.idega.presentation.PresentationObjectContainer;
 import com.idega.presentation.PresentationObjectTransitional;
 import com.idega.presentation.Script;
 import com.idega.presentation.text.Link;
 import com.idega.presentation.text.Paragraph;
 import com.idega.presentation.text.Text;
 import com.idega.presentation.ui.CheckBox;
 import com.idega.presentation.ui.Form;
 import com.idega.presentation.ui.GenericButton;
 import com.idega.presentation.ui.Label;
 import com.idega.presentation.ui.Parameter;
 import com.idega.presentation.ui.PasswordInput;
 import com.idega.presentation.ui.TextInput;
 import com.idega.servlet.filter.IWAuthenticator;
 
 /**
  * <p>
  * New Login component based on JSF and CSS. Will gradually replace old Login
  * component
  * </p>
 * Last modified: $Date: 2007/11/01 17:43:09 $ by $Author: laddi $
  * 
  * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.27 $
  */
 public class Login2 extends PresentationObjectTransitional implements ActionListener {
 
 	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.login";
 	protected static final String FACET_LOGGED_IN = "login_loggedin";
 	protected static final String FACET_LOGGED_OUT = "login_loggedout";
 	private static final String FACET_LOGIN_FAILED = "login_logginfailed";
 
 	public static String STYLE_CLASS_MAIN_DEFAULT = "login";
 	public static String STYLE_CLASS_MAIN_SINGLELINE = "loginSingleline";
 
 	private static final String STYLE_CLASS_CURRENT_USER = "currentUser";
 	private static final String STYLE_CLASS_SUBMIT = "submit";
 	private static final String STYLE_CLASS_USERNAME = "username";
 	private static final String STYLE_CLASS_PASSWORD = "password";
 	private static final String STYLE_CLASS_ERROR_MESSAGE = "errorMessage";
 
 	private boolean enterSubmits = false;
 	private boolean useSubmitLinks = false;
 	private boolean generateContainingForm = false;
 	private boolean useSingleLineLayout = false;
 	private boolean redirectUserToPrimaryGroupHomePage = false;
 	private boolean showLabelInInput = false;
 	private boolean sendToHttps = false;
 	private String urlToRedirectToOnLogon = null;
 	private String urlToRedirectToOnLogoff = null;
 	private Map extraLogonParameters = new HashMap();
 	private Map extraLogoffParameters = new HashMap();
 	private boolean allowCookieLogin=false;
 	private boolean focusOnLoad=false;
 
 	/**
 	 * 
 	 */
 	public Login2() {
 		setStyleClass(STYLE_CLASS_MAIN_DEFAULT);
 		setTransient(false);
 	}
 
 	protected UIComponent getLoggedInPart(IWContext iwc) {
 
 		Layer layer = (Layer) getFacet(FACET_LOGGED_IN);
 
 		if (layer == null) {
 
 			layer = new Layer();
 			layer.setStyleClass(getStyleClass());
 			layer.setStyleClass("loggedIn");
 
 			PresentationObject container = new PresentationObjectContainer();
 			if (getGenerateContainingForm()) {
 				Form form = new Form();
 				layer.getChildren().add(form);
 				container = form;
 			}
 			else {
 				container = layer;
 			}
 
 			Paragraph p = new Paragraph();
 			p.getChildren().add(new Text(iwc.getCurrentUser().getName()));
 			p.setStyleClass(STYLE_CLASS_CURRENT_USER);
 			container.getChildren().add(p);
 
 			String logoutText = getLocalizedString("logout_text", "Log out", iwc);
 
 			String loginParameter = LoginBusinessBean.LoginStateParameter;
 			String logoutParamValue = LoginBusinessBean.LOGIN_EVENT_LOGOFF;
 
 			Parameter param = new Parameter(loginParameter, "");
 			PresentationObject formSubmitter = null;
 
 			Layer submitLayer = new Layer();
 			submitLayer.setStyleClass(STYLE_CLASS_SUBMIT + " " + getCurrentLocaleLanguage(iwc));
 			container.getChildren().add(submitLayer);
 			if (!getUseSubmitLinks()) {
 				GenericButton gbutton = new GenericButton("logoutButton", logoutText);
 
 				gbutton.setOnClick("this.form.elements['" + loginParameter + "'].value='" + logoutParamValue + "';this.form.submit();");
 				formSubmitter = gbutton;
 			}
 			else {
 				Layer span = new Layer(Layer.SPAN);
 				span.getChildren().add(new Text(logoutText));
 
 				Link link = new Link(span);
 				link.setName("logoutButton");
 				link.setStyleClass("logoutButton");
 				link.setURL("#");
 
 				String formRef = "this.form";
 				Form parentForm = submitLayer.getParentForm();
 				if (parentForm != null) {
 					formRef = "document.forms['" + parentForm.getID() + "']";
 				}
 				link.setOnClick(formRef + ".elements['" + loginParameter + "'].value='" + logoutParamValue + "';" + formRef + ".submit();return false;");
 				formSubmitter = link;
 			}
 
 			if (getURLToRedirectToOnLogoff() != null) {
 				submitLayer.getChildren().add(new Parameter(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGOFF, getURLToRedirectToOnLogoff()));
 			}
 
 			if (!this.extraLogoffParameters.isEmpty()) {
 				for (Iterator iter = this.extraLogoffParameters.keySet().iterator(); iter.hasNext();) {
 					String key = (String) iter.next();
 					submitLayer.getChildren().add(new Parameter(key, (String) this.extraLogoffParameters.get(key)));
 				}
 			}
 
 			submitLayer.getChildren().add(param);
 			submitLayer.getChildren().add(formSubmitter);
 
 			getFacets().put(FACET_LOGGED_IN, layer);
 
 		}
 
 		return layer;
 	}
 
 	protected UIComponent getLoggedOutPart(IWContext iwc) {
 
 		Layer layer = (Layer) getFacet(FACET_LOGGED_IN);
 
 		if (layer == null) {
 
 			layer = new Layer();
 			layer.setStyleClass(getStyleClass());
 			layer.setStyleClass("loggedOut");
 
 			String loginParameter = LoginBusinessBean.LoginStateParameter;
 			String loginParamValue = LoginBusinessBean.LOGIN_EVENT_LOGIN;
 
 			boolean enterSubmit = false;
 			if (this.enterSubmits) {
 				Script script = new Script();
 				script.addFunction("enterSubmit", "function enterSubmit(myfield,e) { var keycode; if (window.event) keycode = window.event.keyCode; else if (e) keycode = e.which; else return true; if (keycode == 13) { myfield.form.elements['" + loginParameter + "'].value='" + loginParamValue + "'; myfield.form.submit(); return false; } else return true; }");
 				layer.add(script);
 
 				enterSubmit = true;
 			}
 
 			PresentationObject container = new PresentationObjectContainer();
 			if (getGenerateContainingForm()) {
 				Form form = new Form();
 				if (this.sendToHttps) {
 					form.setToSendToHTTPS(this.sendToHttps);
 				}
 				layer.getChildren().add(form);
 				container = form;
 			}
 			else {
 				container = layer;
 			}
 
 			TextInput login = new TextInput(LoginBusinessBean.PARAMETER_USERNAME);
 			login.setId(LoginBusinessBean.PARAMETER_USERNAME);
 			if (this.showLabelInInput) {
 				login.setValue(getLocalizedString("user", "User", iwc));
 				login.setOnFocus("this.value=''");
 			}
 			if (enterSubmit) {
 				login.setOnKeyPress("return enterSubmit(this,event)");
 			}
 
 			Label loginLabel = new Label(getLocalizedString("user", "User", iwc) + ":", login);
 
 			Layer loginLayer = new Layer();
 			loginLayer.getChildren().add(loginLabel);
 			loginLayer.getChildren().add(login);
 			loginLayer.setStyleClass(STYLE_CLASS_USERNAME);
 			container.getChildren().add(loginLayer);
 
 			PasswordInput password = new PasswordInput(LoginBusinessBean.PARAMETER_PASSWORD);
 			if (this.showLabelInInput) {
 				password.setValue(getLocalizedString("password", "Password", iwc));
 				password.setOnFocus("this.value=''");
 			}
 			if (enterSubmit) {
 				password.setOnKeyPress("return enterSubmit(this,event)");
 			}
 			Label passwordLabel = new Label(getLocalizedString("password", "Password", iwc) + ":", password);
 
 			Layer passwordLayer = new Layer();
 			passwordLayer.getChildren().add(passwordLabel);
 			passwordLayer.getChildren().add(password);
 			passwordLayer.setStyleClass(STYLE_CLASS_PASSWORD);
 			container.getChildren().add(passwordLayer);
 
 			
 			if (this.allowCookieLogin) {
 
 				CheckBox cookieCheck = new CheckBox(IWAuthenticator.PARAMETER_ALLOWS_COOKIE_LOGIN);
 				Label cookieLabel = new Label(getLocalizedString("cookie.allow", "Remember me",iwc), cookieCheck);
 				
 				Layer cookieLayer = new Layer();
				cookieLayer.setStyleClass("allowLogin");
 				cookieLayer.getChildren().add(cookieCheck);
				cookieLayer.getChildren().add(cookieLabel);
 				container.getChildren().add(cookieLayer);
 			}
 			
 			
 			Parameter param = new Parameter(loginParameter, "");
 
 			PresentationObject formSubmitter = null;
 
 			String loginText = getLocalizedString("login_text", "Login", iwc);
 
 			Layer submitLayer = new Layer();
 			submitLayer.setStyleClass(STYLE_CLASS_SUBMIT + " " + getCurrentLocaleLanguage(iwc));
 			container.getChildren().add(submitLayer);
 			if (!getUseSubmitLinks()) {
 				GenericButton gbutton = new GenericButton("loginButton", loginText);
 
 				gbutton.setOnClick("this.form.elements['" + loginParameter + "'].value='" + loginParamValue + "';this.form.submit();");
 				formSubmitter = gbutton;
 
 			}
 			else {
 
 				Layer span = new Layer(Layer.SPAN);
 				span.getChildren().add(new Text(loginText));
 				Link l = new Link(span);
 
 				l.setName("loginButton");
 				l.setStyleClass("loginButton");
 				l.setURL("#");
 
 				String formRef = "this.form";
 				Form parentForm = submitLayer.getParentForm();
 				if (parentForm != null) {
 					formRef = "document.forms['" + parentForm.getID() + "']";
 				}
 				l.setOnClick(formRef + ".elements['" + loginParameter + "'].value='" + loginParamValue + "';" + formRef + ".submit();return false;");
 				formSubmitter = l;
 			}
 
 			if (this.redirectUserToPrimaryGroupHomePage) {
 				submitLayer.getChildren().add(new Parameter(IWAuthenticator.PARAMETER_REDIRECT_USER_TO_PRIMARY_GROUP_HOME_PAGE, "true"));
 			}
 			else if (getURLToRedirectToOnLogon() != null) {
 				submitLayer.getChildren().add(new Parameter(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON, getURLToRedirectToOnLogon()));
 			}
 			else if (iwc.isParameterSet(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON)) {
 				submitLayer.getChildren().add(new Parameter(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON, iwc.getParameter(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON)));
 			}
 
 			if (!this.extraLogonParameters.isEmpty()) {
 				for (Iterator iter = this.extraLogonParameters.keySet().iterator(); iter.hasNext();) {
 					String key = (String) iter.next();
 					submitLayer.getChildren().add(new Parameter(key, (String) this.extraLogonParameters.get(key)));
 				}
 			}
 
 			if(isFocusOnLoad()){
 				//login.setInFocusOnPageLoad(true);
 				Script script = new Script();
 				script.addScriptLine("var logininput = document.getElementById('" + login.getClientId(iwc) + "');\nlogininput.focus();");
 				layer.add(script);
 			
 			}
 			
 			submitLayer.getChildren().add(param);
 			submitLayer.getChildren().add(formSubmitter);
 
 			getFacets().put(FACET_LOGGED_OUT, layer);
 
 		}
 
 		return layer;
 	}
 
 	protected UIComponent getLoginFailedPart(IWContext iwc, String message) {
 
 		Layer layer = (Layer) getFacet(FACET_LOGGED_IN);
 
 		if (layer == null) {
 
 			layer = new Layer();
 			layer.setStyleClass(getStyleClass());
 			layer.setStyleClass("loginFailed");
 			
 			PresentationObject container = new PresentationObjectContainer();
 			if (getGenerateContainingForm()) {
 				Form form = new Form();
 				layer.getChildren().add(form);
 				container = form;
 			}
 			else {
 				container = layer;
 			}
 
 			Paragraph p = new Paragraph();
 			p.setStyleClass(STYLE_CLASS_ERROR_MESSAGE);
 			p.getChildren().add(new Text(message));
 			container.getChildren().add(p);
 
 			String loginParameter = LoginBusinessBean.LoginStateParameter;
 			String logoutParamValue = LoginBusinessBean.LOGIN_EVENT_TRYAGAIN;
 			Parameter param = new Parameter(loginParameter, "");
 
 			String tryAgainText = getLocalizedString("tryagain_text", "Try again", iwc);
 
 			Layer submitLayer = new Layer();
 			submitLayer.setStyleClass(STYLE_CLASS_SUBMIT + " " + getCurrentLocaleLanguage(iwc));
 			container.getChildren().add(submitLayer);
 			PresentationObject formSubmitter = null;
 			if (!getUseSubmitLinks()) {
 				GenericButton gbutton = new GenericButton("retryButton", tryAgainText);
 
 				gbutton.setOnClick("this.form.elements['" + loginParameter + "'].value='" + logoutParamValue + "';this.form.submit();");
 				formSubmitter = gbutton;
 			}
 			else {
 
 				Layer span = new Layer(Layer.SPAN);
 				span.getChildren().add(new Text(tryAgainText));
 				Link l = new Link(span);
 				l.setName("retryButton");
 				l.setStyleClass("retryButton");
 				l.setURL("#");
 
 				String formRef = "this.form";
 				Form parentForm = submitLayer.getParentForm();
 				if (parentForm != null) {
 					formRef = "document.forms['" + parentForm.getID() + "']";
 				}
 				l.setOnClick(formRef + ".elements['" + loginParameter + "'].value='" + logoutParamValue + "';" + formRef + ".submit();return false;");
 				formSubmitter = l;
 			}
 			submitLayer.getChildren().add(param);
 			submitLayer.getChildren().add(formSubmitter);
 			if (iwc.isParameterSet(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON)) {
 				submitLayer.getChildren().add(new Parameter(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON, iwc.getParameter(IWAuthenticator.PARAMETER_REDIRECT_URI_ONLOGON)));
 			}
 
 			getFacets().put(FACET_LOGIN_FAILED, layer);
 		}
 		return layer;
 	}
 
 	public String getBundleIdentifier() {
 		return IW_BUNDLE_IDENTIFIER;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.idega.presentation.PresentationObjectTransitional#encodeChildren(javax.faces.context.FacesContext)
 	 */
 	public void encodeChildren(FacesContext context) throws IOException {
 
 		super.encodeChildren(context);
 		IWContext iwc = IWContext.getIWContext(context);
 		if (iwc.isLoggedOn()) {
 			UIComponent loggedInPart = getLoggedInPart(iwc);
 			renderChild(context, loggedInPart);
 		}
 		else {
 			LoginState state = LoginBusinessBean.internalGetState(iwc);
 			if (state.equals(LoginState.LoggedOut) || state.equals(LoginState.NoState)) {
 				UIComponent loggedOutPart = getLoggedOutPart(iwc);
 				renderChild(context, loggedOutPart);
 			}
 			else {
 				IWResourceBundle iwrb = getResourceBundle(iwc);
 
 				UIComponent loginFailedPart = null;
 
 				if (state.equals(LoginState.Failed)) {
 					loginFailedPart = getLoginFailedPart(iwc, iwrb.getLocalizedString("login_failed", "Login failed"));
 				}
 				else if (state.equals(LoginState.NoUser)) {
 					loginFailedPart = getLoginFailedPart(iwc, iwrb.getLocalizedString("login_no_user", "Invalid user"));
 				}
 				else if (state.equals(LoginState.WrongPassword)) {
 					loginFailedPart = getLoginFailedPart(iwc, iwrb.getLocalizedString("login_wrong", "Invalid password"));
 				}
 				else if (state.equals(LoginState.Expired)) {
 					loginFailedPart = getLoginFailedPart(iwc, iwrb.getLocalizedString("login_expired", "Login expired"));
 				}
 				else if (state.equals(LoginState.FailedDisabledNextTime)) {
 					loginFailedPart = getLoginFailedPart(iwc, iwrb.getLocalizedString("login_wrong_disabled_next_time", "Invalid password, access closed next time login fails"));
 				}
 
 				// TODO: what about wml, see Login block
 				renderChild(context, loginFailedPart);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
 	 */
 	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
 		// LoginBusinessBean.internalGetState()
 
 		/*
 		 * UIComponent component = actionEvent.getComponent(); boolean
 		 * isLoggingoff=true;
 		 */
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.idega.presentation.PresentationObjectContainer#restoreState(javax.faces.context.FacesContext,
 	 *      java.lang.Object)
 	 */
 	public void restoreState(FacesContext context, Object state) {
 		Object[] value = (Object[]) state;
 		super.restoreState(context, value[0]);
 		this.useSubmitLinks = ((Boolean) value[1]).booleanValue();
 		this.generateContainingForm = ((Boolean) value[2]).booleanValue();
 		this.useSingleLineLayout = ((Boolean) value[3]).booleanValue();
 		this.redirectUserToPrimaryGroupHomePage = ((Boolean) value[4]).booleanValue();
 		this.allowCookieLogin = ((Boolean) value[5]).booleanValue();
 		this.focusOnLoad = ((Boolean) value[6]).booleanValue();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.idega.presentation.PresentationObjectContainer#saveState(javax.faces.context.FacesContext)
 	 */
 	public Object saveState(FacesContext context) {
 		Object[] state = new Object[7];
 		state[0] = super.saveState(context);
 		state[1] = Boolean.valueOf(this.useSubmitLinks);
 		state[2] = Boolean.valueOf(this.generateContainingForm);
 		state[3] = Boolean.valueOf(this.useSingleLineLayout);
 		state[4] = Boolean.valueOf(this.redirectUserToPrimaryGroupHomePage);
 		state[5] = Boolean.valueOf(this.allowCookieLogin);
 		state[6] = Boolean.valueOf(this.focusOnLoad);
 		return state;
 	}
 
 	public boolean getUseSubmitLinks() {
 		return this.useSubmitLinks;
 	}
 
 	public void setUseSubmitLinks(boolean useSubmitLinks) {
 		this.useSubmitLinks = useSubmitLinks;
 		// TODO: rather have one facet for button and one for link and decide when
 		// rendering which to render
 		// Now this clears all facets so that all states will be built again.
 		getFacets().clear();
 	}
 
 	public boolean getGenerateContainingForm() {
 		return this.generateContainingForm;
 	}
 
 	public void setGenerateContainingForm(boolean generateContainingForm) {
 		this.generateContainingForm = generateContainingForm;
 	}
 
 	public boolean getUseSingleLineLayout() {
 		return this.useSingleLineLayout;
 	}
 
 	public void setRedirectUserToPrimaryGroupHomePage(boolean redirectToHomePage) {
 		this.redirectUserToPrimaryGroupHomePage = redirectToHomePage;
 	}
 
 	public void setUseSingleLineLayout(boolean useSingleLineLayout) {
 		this.useSingleLineLayout = useSingleLineLayout;
 		if (useSingleLineLayout) {
 			setStyleClass(STYLE_CLASS_MAIN_SINGLELINE);
 		}
 		else {
 			setStyleClass(STYLE_CLASS_MAIN_DEFAULT);
 		}
 	}
 
 	/**
 	 * a small helper method
 	 * 
 	 * @param iwc
 	 * @return
 	 */
 	private String getCurrentLocaleLanguage(IWContext iwc) {
 		return iwc.getLocale().getLanguage();
 	}
 
 	public void setShowLabelInInput(boolean showLabelInInput) {
 		this.showLabelInInput = showLabelInInput;
 	}
 
 	public void setURLToRedirectToOnLogon(String url) {
 		this.urlToRedirectToOnLogon = url;
 	}
 
 	public String getURLToRedirectToOnLogon() {
 		return this.urlToRedirectToOnLogon;
 	}
 
 	public void setURLToRedirectToOnLogoff(String url) {
 		this.urlToRedirectToOnLogoff = url;
 	}
 
 	public String getURLToRedirectToOnLogoff() {
 		return this.urlToRedirectToOnLogoff;
 	}
 
 	public void setExtraLogonParameter(String parameter, String value) {
 		this.extraLogonParameters.put(parameter, value);
 	}
 
 	public void setExtraLogoffParameter(String parameter, String value) {
 		this.extraLogoffParameters.put(parameter, value);
 	}
 
 	public void setEnterSubmits(boolean enterSubmits) {
 		this.enterSubmits = enterSubmits;
 	}
 
 	public void setSendToHTTPS(boolean sendToHttps) {
 		this.sendToHttps = sendToHttps;
 	}
 	
 	/**
 	 * <p>
 	 * If set to true then a checkbox is displayed that displays and enabled 
 	 * the cookie/remember-me function to keep a persistent login.
 	 * </p>
 	 * @param cookies
 	 */
 	public void setAllowCookieLogin(boolean cookies) {
 		this.allowCookieLogin = cookies;
 	}
 	
 	public boolean getAllowCookieLogin(){
 		return this.allowCookieLogin;
 	}
 
 	
 	public boolean isFocusOnLoad() {
 		return focusOnLoad;
 	}
 
 	
 	public void setFocusOnLoad(boolean focusOnLoad) {
 		this.focusOnLoad = focusOnLoad;
 	}
 }
