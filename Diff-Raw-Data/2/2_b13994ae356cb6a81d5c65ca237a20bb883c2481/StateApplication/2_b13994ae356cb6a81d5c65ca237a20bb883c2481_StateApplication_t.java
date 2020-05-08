 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 package org.richfaces.ui.application;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.el.ELContextListener;
 import javax.el.ELException;
 import javax.el.ELResolver;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.application.NavigationHandler;
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.el.MethodBinding;
 import javax.faces.el.PropertyResolver;
 import javax.faces.el.ReferenceSyntaxException;
 import javax.faces.el.ValueBinding;
 import javax.faces.el.VariableResolver;
 import javax.faces.event.ActionListener;
 import javax.faces.validator.Validator;
 
 /**
  * @author asmirnov
  *
  */
 public class StateApplication extends Application {
 	
 	private final Application parent;
 	private final ExpressionFactory exprFactory;
 
 	/**
 	 * @param parent
 	 */
 	public StateApplication(Application parentApp) {
 		super();
 		this.parent = parentApp;
 		this.exprFactory = new StateExpressionFactory(){
 			@Override
 			public ExpressionFactory getDefaultFactory() {
 				return parent.getExpressionFactory();
 			}
 		};
 	}
 
 	/**
 	 * @param componentType
 	 * @param componentClass
 	 * @see javax.faces.application.Application#addComponent(java.lang.String, java.lang.String)
 	 */
 	public void addComponent(String componentType, String componentClass) {
 		parent.addComponent(componentType, componentClass);
 	}
 
 	/**
 	 * @param targetClass
 	 * @param converterClass
 	 * @see javax.faces.application.Application#addConverter(java.lang.Class, java.lang.String)
 	 */
 	public void addConverter(Class targetClass, String converterClass) {
 		parent.addConverter(targetClass, converterClass);
 	}
 
 	/**
 	 * @param converterId
 	 * @param converterClass
 	 * @see javax.faces.application.Application#addConverter(java.lang.String, java.lang.String)
 	 */
 	public void addConverter(String converterId, String converterClass) {
 		parent.addConverter(converterId, converterClass);
 	}
 
 	/**
 	 * @param listener
 	 * @see javax.faces.application.Application#addELContextListener(javax.el.ELContextListener)
 	 */
 	public void addELContextListener(ELContextListener listener) {
 		parent.addELContextListener(listener);
 	}
 
 	/**
 	 * @param resolver
 	 * @see javax.faces.application.Application#addELResolver(javax.el.ELResolver)
 	 */
 	public void addELResolver(ELResolver resolver) {
 		parent.addELResolver(resolver);
 	}
 
 	/**
 	 * @param validatorId
 	 * @param validatorClass
 	 * @see javax.faces.application.Application#addValidator(java.lang.String, java.lang.String)
 	 */
 	public void addValidator(String validatorId, String validatorClass) {
 		parent.addValidator(validatorId, validatorClass);
 	}
 
 	/**
 	 * @param componentType
 	 * @return
 	 * @throws FacesException
 	 * @see javax.faces.application.Application#createComponent(java.lang.String)
 	 */
 	public UIComponent createComponent(String componentType)
 			throws FacesException {
 		return parent.createComponent(componentType);
 	}
 
 	/**
 	 * @param componentBinding
 	 * @param context
 	 * @param componentType
 	 * @return
 	 * @throws FacesException
 	 * @deprecated
 	 * @see javax.faces.application.Application#createComponent(javax.faces.el.ValueBinding, javax.faces.context.FacesContext, java.lang.String)
 	 */
 	public UIComponent createComponent(ValueBinding componentBinding,
 			FacesContext context, String componentType) throws FacesException {
 		return parent.createComponent(componentBinding, context, componentType);
 	}
 
 	/**
 	 * @param componentExpression
 	 * @param context
 	 * @param componentType
 	 * @return
 	 * @throws FacesException
 	 * @see javax.faces.application.Application#createComponent(javax.el.ValueExpression, javax.faces.context.FacesContext, java.lang.String)
 	 */
 	public UIComponent createComponent(ValueExpression componentExpression,
 			FacesContext context, String componentType) throws FacesException {
 		return parent.createComponent(componentExpression, context,
 				componentType);
 	}
 
 	/**
 	 * @param targetClass
 	 * @return
 	 * @see javax.faces.application.Application#createConverter(java.lang.Class)
 	 */
 	public Converter createConverter(Class targetClass) {
 		return parent.createConverter(targetClass);
 	}
 
 	/**
 	 * @param converterId
 	 * @return
 	 * @see javax.faces.application.Application#createConverter(java.lang.String)
 	 */
 	public Converter createConverter(String converterId) {
 		return parent.createConverter(converterId);
 	}
 
 	/**
 	 * @param ref
 	 * @param params
 	 * @return
 	 * @throws ReferenceSyntaxException
 	 * @deprecated
 	 * @see javax.faces.application.Application#createMethodBinding(java.lang.String, java.lang.Class[])
 	 */
 	public MethodBinding createMethodBinding(String ref, Class[] params)
 			throws ReferenceSyntaxException {
 		return parent.createMethodBinding(ref, params);
 	}
 
 	/**
 	 * @param validatorId
 	 * @return
 	 * @throws FacesException
 	 * @see javax.faces.application.Application#createValidator(java.lang.String)
 	 */
 	public Validator createValidator(String validatorId) throws FacesException {
 		return parent.createValidator(validatorId);
 	}
 
 	/**
 	 * @param ref
 	 * @return
 	 * @throws ReferenceSyntaxException
 	 * @deprecated
 	 * @see javax.faces.application.Application#createValueBinding(java.lang.String)
 	 */
 	public ValueBinding createValueBinding(String ref)
 			throws ReferenceSyntaxException {
 		return parent.createValueBinding(ref);
 	}
 
 	/**
 	 * @param context
 	 * @param expression
 	 * @param expectedType
 	 * @return
 	 * @throws ELException
 	 * @see javax.faces.application.Application#evaluateExpressionGet(javax.faces.context.FacesContext, java.lang.String, java.lang.Class)
 	 */
 	public Object evaluateExpressionGet(FacesContext context,
 			String expression, Class expectedType) throws ELException {
 		return parent.evaluateExpressionGet(context, expression, expectedType);
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getActionListener()
 	 */
 	public ActionListener getActionListener() {
 		return parent.getActionListener();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getComponentTypes()
 	 */
 	public Iterator<String> getComponentTypes() {
 		return parent.getComponentTypes();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getConverterIds()
 	 */
 	public Iterator<String> getConverterIds() {
 		return parent.getConverterIds();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getConverterTypes()
 	 */
	public Iterator getConverterTypes() {
 		return parent.getConverterTypes();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getDefaultLocale()
 	 */
 	public Locale getDefaultLocale() {
 		return parent.getDefaultLocale();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getDefaultRenderKitId()
 	 */
 	public String getDefaultRenderKitId() {
 		return parent.getDefaultRenderKitId();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getELContextListeners()
 	 */
 	public ELContextListener[] getELContextListeners() {
 		return parent.getELContextListeners();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getELResolver()
 	 */
 	public ELResolver getELResolver() {
 		return parent.getELResolver();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getExpressionFactory()
 	 */
 	public ExpressionFactory getExpressionFactory() {
 		return this.exprFactory;
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getMessageBundle()
 	 */
 	public String getMessageBundle() {
 		return parent.getMessageBundle();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getNavigationHandler()
 	 */
 	public NavigationHandler getNavigationHandler() {
 		return parent.getNavigationHandler();
 	}
 
 	/**
 	 * @return
 	 * @deprecated
 	 * @see javax.faces.application.Application#getPropertyResolver()
 	 */
 	public PropertyResolver getPropertyResolver() {
 		return parent.getPropertyResolver();
 	}
 
 	/**
 	 * @param ctx
 	 * @param name
 	 * @return
 	 * @see javax.faces.application.Application#getResourceBundle(javax.faces.context.FacesContext, java.lang.String)
 	 */
 	public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
 		return parent.getResourceBundle(ctx, name);
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getStateManager()
 	 */
 	public StateManager getStateManager() {
 		return parent.getStateManager();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getSupportedLocales()
 	 */
 	public Iterator<Locale> getSupportedLocales() {
 		return parent.getSupportedLocales();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getValidatorIds()
 	 */
 	public Iterator<String> getValidatorIds() {
 		return parent.getValidatorIds();
 	}
 
 	/**
 	 * @return
 	 * @deprecated
 	 * @see javax.faces.application.Application#getVariableResolver()
 	 */
 	public VariableResolver getVariableResolver() {
 		return parent.getVariableResolver();
 	}
 
 	/**
 	 * @return
 	 * @see javax.faces.application.Application#getViewHandler()
 	 */
 	public ViewHandler getViewHandler() {
 		return parent.getViewHandler();
 	}
 
 	/**
 	 * @param listener
 	 * @see javax.faces.application.Application#removeELContextListener(javax.el.ELContextListener)
 	 */
 	public void removeELContextListener(ELContextListener listener) {
 		parent.removeELContextListener(listener);
 	}
 
 	/**
 	 * @param listener
 	 * @see javax.faces.application.Application#setActionListener(javax.faces.event.ActionListener)
 	 */
 	public void setActionListener(ActionListener listener) {
 		parent.setActionListener(listener);
 	}
 
 	/**
 	 * @param locale
 	 * @see javax.faces.application.Application#setDefaultLocale(java.util.Locale)
 	 */
 	public void setDefaultLocale(Locale locale) {
 		parent.setDefaultLocale(locale);
 	}
 
 	/**
 	 * @param renderKitId
 	 * @see javax.faces.application.Application#setDefaultRenderKitId(java.lang.String)
 	 */
 	public void setDefaultRenderKitId(String renderKitId) {
 		parent.setDefaultRenderKitId(renderKitId);
 	}
 
 	/**
 	 * @param bundle
 	 * @see javax.faces.application.Application#setMessageBundle(java.lang.String)
 	 */
 	public void setMessageBundle(String bundle) {
 		parent.setMessageBundle(bundle);
 	}
 
 	/**
 	 * @param handler
 	 * @see javax.faces.application.Application#setNavigationHandler(javax.faces.application.NavigationHandler)
 	 */
 	public void setNavigationHandler(NavigationHandler handler) {
 		parent.setNavigationHandler(handler);
 	}
 
 	/**
 	 * @param resolver
 	 * @deprecated
 	 * @see javax.faces.application.Application#setPropertyResolver(javax.faces.el.PropertyResolver)
 	 */
 	public void setPropertyResolver(PropertyResolver resolver) {
 		parent.setPropertyResolver(resolver);
 	}
 
 	/**
 	 * @param manager
 	 * @see javax.faces.application.Application#setStateManager(javax.faces.application.StateManager)
 	 */
 	public void setStateManager(StateManager manager) {
 		parent.setStateManager(manager);
 	}
 
 	/**
 	 * @param locales
 	 * @see javax.faces.application.Application#setSupportedLocales(java.util.Collection)
 	 */
 	public void setSupportedLocales(Collection<Locale> locales) {
 		parent.setSupportedLocales(locales);
 	}
 
 	/**
 	 * @param resolver
 	 * @deprecated
 	 * @see javax.faces.application.Application#setVariableResolver(javax.faces.el.VariableResolver)
 	 */
 	public void setVariableResolver(VariableResolver resolver) {
 		parent.setVariableResolver(resolver);
 	}
 
 	/**
 	 * @param handler
 	 * @see javax.faces.application.Application#setViewHandler(javax.faces.application.ViewHandler)
 	 */
 	public void setViewHandler(ViewHandler handler) {
 		parent.setViewHandler(handler);
 	}
 
 }
