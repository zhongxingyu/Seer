 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.scripting.jsf.dynamicDecorators.implementations;
 
 import org.apache.myfaces.extensions.scripting.core.api.Decorated;
 import org.apache.myfaces.extensions.scripting.core.api.WeavingContext;
 
 import javax.el.ELContextListener;
 import javax.el.ELException;
 import javax.el.ELResolver;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.application.NavigationHandler;
 import javax.faces.application.ProjectStage;
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.component.behavior.Behavior;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.el.MethodBinding;
 import javax.faces.el.PropertyResolver;
 import javax.faces.el.ReferenceSyntaxException;
 import javax.faces.el.ValueBinding;
 import javax.faces.el.VariableResolver;
 import javax.faces.event.ActionListener;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.SystemEventListener;
 import javax.faces.validator.Validator;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.concurrent.ConcurrentHashMap;
 
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_ACTIONLISTENER;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_APPLICATION;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_BEHAVIOR;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_COMPONENT;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_CONVERTER;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_ELCONTEXTLISTENER;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_MANAGEDBEAN;
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ARTIFACT_TYPE_VALIDATOR;
 
 /**
  * @author Werner Punz
  *         <p/>
  *         our decorating applicstion
  *         which should resolve our bean issues within a central
  *         bean processing interceptor
  */
 public class ApplicationProxy extends Application implements Decorated
 {
 
     Application _delegate = null;
 
     /*
     * separate map needed for the behavior ids, because
     * the original is immutable
     * we have to do a double bookkeeping
     * here
     */
     Map<String, String> _behaviors = new ConcurrentHashMap<String, String>();
 
     /**
      * special data structure to save our
      * object -> proxy references
      */
     class EventHandlerProxyEntry
     {
         Class event;
         Decorated proxy;
 
         EventHandlerProxyEntry(Class event, Decorated proxy)
         {
             this.event = event;
             this.proxy = proxy;
         }
 
         @SuppressWarnings("unused")
         public Class getEvent()
         {
             return event;
         }
 
         @SuppressWarnings("unused")
         public void setEvent(Class event)
         {
             this.event = event;
         }
 
         public Decorated getProxy()
         {
             return proxy;
         }
 
         @SuppressWarnings("unused")
         public void setProxy(Decorated proxy)
         {
             this.proxy = proxy;
         }
 
         @Override
         public boolean equals(Object o)
         {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             EventHandlerProxyEntry that = (EventHandlerProxyEntry) o;
 
             return !(event != null ? !event.equals(that.event) : that.event != null) && !(proxy != null ? !proxy.getDelegate().getClass().getName().equals(that.proxy.getDelegate().getClass().getName()) : that.proxy != null);
 
         }
 
         @Override
         public int hashCode()
         {
             int result = event.hashCode();
             result = 31 * result + proxy.getDelegate().getClass().getName().hashCode();
             return result;
         }
     }
 
     /**
      * now at the first look this looks like a weird construct
      * but the standard java set imposes this limit since
      * we have to iterate over the entire set to reach the correct element
      * the trick is to save the same object in as both key and value
      * and now if we generate a new key on an object
      * we can fetch our proxy which might already contain
      * the same object in a refreshed state from the value
      * part of the set, in our case
      * using hash maps should speed things up
      * <p/>
      * since we only have few write operations but access
      * the map multithreaded we use concurrentHashMap here
      */
     Map<EventHandlerProxyEntry, EventHandlerProxyEntry> _eventHandlerIdx = new ConcurrentHashMap<EventHandlerProxyEntry, EventHandlerProxyEntry>();
 
     volatile static boolean varResolverAdded = false;
 
     ELResolverProxy finalResolver = null;
 
     public ApplicationProxy(Application delegate)
     {
         _delegate = delegate;
     }
 
     public void addELResolver(ELResolver elResolver)
     {
         weaveDelegate();
         //we do not need a proxy here anymore because
         //we drop the beans directly
         _delegate.addELResolver(elResolver);
     }
 
     private void weaveDelegate()
     {
         if (_delegate != null)
         {
             _delegate = (Application) WeavingContext.getInstance().reload(_delegate,
                     ARTIFACT_TYPE_APPLICATION);
         }
     }
 
     public ELResolver getELResolver()
     {
         weaveDelegate();
 
         ELResolver retVal = _delegate.getELResolver();
         return retVal;
     }
 
     //TOD add a weaving for resource bundles
 
     public ResourceBundle getResourceBundle(FacesContext facesContext, String s) throws FacesException, NullPointerException
     {
         weaveDelegate();
         return _delegate.getResourceBundle(facesContext, s);
     }
 
     public UIComponent createComponent(ValueExpression valueExpression, FacesContext facesContext, String componentType) throws FacesException, NullPointerException
     {
         weaveDelegate();
         UIComponent component = _delegate.createComponent(valueExpression, facesContext, componentType);
         UIComponent oldComponent = component;
         //We can replace annotated components on the fly via
         //ApplicationImpl.addComponent(final String componentType, final String componentClassName)
 
         /*we are reweaving on the fly because we cannot be sure if
         * the class is not recycled all the time in the creation
         * code, in the renderer we do it on method base
         * due to the fact that our renderers are recycled via
         * a flyweight pattern
         *
         *
         * Also we cannot proxy here because there is no UIComponent interface
         * maybe in the long run we can make a decorator here instead
         * but for now lets try it this way
         */
         component = (UIComponent) reloadInstance(component, ARTIFACT_TYPE_COMPONENT);
 
         //we now have to check for an annotation change, but only in case a reload has happened
         /*<> if (component.getClass().hashCode() != oldComponent.getClass().hashCode())
         {
             return handleAnnotationChange(component, valueExpression, facesContext, componentType);
         } */
 
         return component;
 
     }
 
     public ExpressionFactory getExpressionFactory()
     {
         weaveDelegate();
         return _delegate.getExpressionFactory();
     }
 
     public void addELContextListener(ELContextListener elContextListener)
     {
         weaveDelegate();
         if (WeavingContext.getInstance().isDynamic(elContextListener.getClass()))
             elContextListener = (ELContextListener) WeavingContext.getInstance().createMethodReloadingProxyFromObject
                     (elContextListener, ELContextListener.class, ARTIFACT_TYPE_ELCONTEXTLISTENER);
         _delegate.addELContextListener(elContextListener);
     }
 
     public void removeELContextListener(ELContextListener elContextListener)
     {
         weaveDelegate();
         _delegate.removeELContextListener(elContextListener);
     }
 
     public ELContextListener[] getELContextListeners()
     {
         weaveDelegate();
         return _delegate.getELContextListeners();
     }
 
     public ActionListener getActionListener()
     {
         weaveDelegate();
         ActionListener retVal = _delegate.getActionListener();
         if (retVal == null)
         {
             return retVal;
         }
         if (WeavingContext.getInstance().isDynamic(retVal.getClass()))
             retVal = (ActionListener) WeavingContext.getInstance().createMethodReloadingProxyFromObject(retVal,
                     ActionListener.class, ARTIFACT_TYPE_ACTIONLISTENER);
         return retVal;
     }
 
     public void setActionListener(ActionListener actionListener)
     {
         weaveDelegate();
         if (WeavingContext.getInstance().isDynamic(actionListener.getClass()))
             actionListener = (ActionListener) WeavingContext.getInstance().createMethodReloadingProxyFromObject(actionListener,
                     ActionListener.class, ARTIFACT_TYPE_ACTIONLISTENER);
         _delegate.setActionListener(actionListener);
     }
 
     public Locale getDefaultLocale()
     {
         weaveDelegate();
         return _delegate.getDefaultLocale();
     }
 
     public void setDefaultLocale(Locale locale)
     {
         weaveDelegate();
         _delegate.setDefaultLocale(locale);
     }
 
     public String getDefaultRenderKitId()
     {
         weaveDelegate();
         return _delegate.getDefaultRenderKitId();
     }
 
     public void setDefaultRenderKitId(String s)
     {
         weaveDelegate();
         _delegate.setDefaultRenderKitId(s);
     }
 
     public String getMessageBundle()
     {
         weaveDelegate();
         return _delegate.getMessageBundle();
     }
 
     public void setMessageBundle(String s)
     {
         weaveDelegate();
         _delegate.setMessageBundle(s);
     }
 
     public NavigationHandler getNavigationHandler()
     {
         weaveDelegate();
         //defined in the setter to speed things up a little
         NavigationHandler retVal = _delegate.getNavigationHandler();
         //if (retVal != null && WeavingContext.isDynamic(retVal.getClass()))
         //    retVal = new NavigationHandlerProxy(retVal);
         return retVal;
     }
 
     public void setNavigationHandler(NavigationHandler navigationHandler)
     {
         weaveDelegate();
 
         if (navigationHandler != null && WeavingContext.getInstance().isDynamic(navigationHandler.getClass()))
             navigationHandler = new NavigationHandlerProxy(navigationHandler);
         _delegate.setNavigationHandler(navigationHandler);
     }
 
     @SuppressWarnings("deprecation")
     public PropertyResolver getPropertyResolver()
     {
         weaveDelegate();
         return _delegate.getPropertyResolver();
     }
 
     @SuppressWarnings("deprecation")
     public void setPropertyResolver(PropertyResolver propertyResolver)
     {
         weaveDelegate();
         _delegate.setPropertyResolver(propertyResolver);
     }
 
     @SuppressWarnings("deprecation")
     public VariableResolver getVariableResolver()
     {
         weaveDelegate();
         return _delegate.getVariableResolver();
     }
 
     @SuppressWarnings("deprecation")
     public void setVariableResolver(VariableResolver variableResolver)
     {
         weaveDelegate();
         if (!varResolverAdded)
         {
             variableResolver = new VariableResolverProxy(variableResolver);
             varResolverAdded = true;
         }
         _delegate.setVariableResolver(variableResolver);
     }
 
     public ViewHandler getViewHandler()
     {
         weaveDelegate();
         ViewHandler handler = _delegate.getViewHandler();
         if (handler == null)
         {
             return handler;
         }
         /*
         We proxy here to enable dynamic reloading for
         methods in the long run, as soon as we hit
         java all our groovy reloading code is lost
         hence we have to work with proxies here
         */
         if (WeavingContext.getInstance().isDynamic(handler.getClass()))
             handler = new ViewHandlerProxy(handler);
         return handler;
     }
 
     public void setViewHandler(ViewHandler viewHandler)
     {
         weaveDelegate();
         /*make sure you have the delegates as well in properties*/
         if (WeavingContext.getInstance().isDynamic(viewHandler.getClass()))
             viewHandler = new ViewHandlerProxy(viewHandler);
 
         _delegate.setViewHandler(viewHandler);
     }
 
     public StateManager getStateManager()
     {
         weaveDelegate();
         return _delegate.getStateManager();
     }
 
     public void setStateManager(StateManager stateManager)
     {
         weaveDelegate();
         _delegate.setStateManager(stateManager);
     }
 
     public void addComponent(String componentType, String componentClass)
     {
         weaveDelegate();
         _delegate.addComponent(componentType, componentClass);
     }
 
     public UIComponent createComponent(String componentType) throws FacesException
     {
         weaveDelegate();
         //the components are generated anew very often
         //we cannot do an on object weaving here
         UIComponent oldComponent = _delegate.createComponent(componentType);
 
         /*we are reweaving on the fly because we cannot be sure if
         * the class is not recycled all the time in the creation
         * code, in the renderer we do it on method base
         * due to the fact that our renderers are recycled via
         * a flyweight pattern*/
         UIComponent component = (UIComponent) reloadInstance(oldComponent, ARTIFACT_TYPE_COMPONENT);
 
         //we now have to check for an annotation change, but only in case a reload has happened
         /*<> if (component.getClass().hashCode() != oldComponent.getClass().hashCode())
         {
             return handleAnnotationChange(component, componentType);
         } */
 
         return component;
 
     }
 
     @SuppressWarnings("deprecation")
     public UIComponent createComponent(ValueBinding valueBinding, FacesContext facesContext, String componentType) throws FacesException
     {
         weaveDelegate();
         UIComponent oldComponent = _delegate.createComponent(valueBinding, facesContext, componentType);
 
         /*we are reweaving on the fly because we cannot be sure if
          * the class is not recycled all the time in the creation
          * code, in the renderer we do it on method base
          * due to the fact that our renderers are recycled via
          * a flyweight pattern*/
         UIComponent component = (UIComponent) reloadInstance(oldComponent, ARTIFACT_TYPE_COMPONENT);
 
         //we now have to check for an annotation change, but only in case a reload has happened
         /*<>if (component.getClass().hashCode() != oldComponent.getClass().hashCode())
         {
             return handleAnnotationChange(component, valueBinding, facesContext, componentType);
         }*/
 
         return component;
     }
 
     public Iterator<String> getComponentTypes()
     {
         weaveDelegate();
         return _delegate.getComponentTypes();
     }
 
     public void addConverter(String converterId, String converterClass)
     {
         weaveDelegate();
         /* if (converterClass.equals(PurgedConverter.class.getName())) {
             //purged case we do a full rescan
             WeavingContext.getWeaver().fullClassScan();
             Converter componentToChange = _delegate.createConverter(converterId);
             if (componentToChange instanceof PurgedConverter) {
                 //Null not allowed here, but we set a purted converter to make
                 //sure that we get errors on the proper level
                 _delegate.addConverter(converterId, PurgedConverter.class.getName());
             }
             return;
         }*/
 
         _delegate.addConverter(converterId, converterClass);
     }
 
     public void addConverter(Class targetClass, String converterClass)
     {
         weaveDelegate();
         _delegate.addConverter(targetClass, converterClass);
     }
 
     public Converter createConverter(String converterId)
     {
         weaveDelegate();
         Converter retVal = _delegate.createConverter(converterId);
 
         /**
          * since createConverter is called only once
          * we have to work with method reloading proxies
          * we cannot use this technique extensively for speed reasons
          * most of the time it is fine just to work with
          *
          * reloading objects at their interception points
          */
         Converter newRetVal = (Converter) reloadInstance(retVal, ARTIFACT_TYPE_CONVERTER);
         if (newRetVal != retVal)
         {
             return _delegate.createConverter(converterId);
         }
 
         return retVal;
     }
 
     public Converter createConverter(Class aClass)
     {
         weaveDelegate();
         Converter retVal = _delegate.createConverter(aClass);
         Converter newRetVal = (Converter) reloadInstance(retVal, ARTIFACT_TYPE_CONVERTER);
         if (newRetVal != retVal)
         {
             return _delegate.createConverter(aClass);
         }
 
         return retVal;
     }
 
     public Iterator<String> getConverterIds()
     {
         weaveDelegate();
         return _delegate.getConverterIds();
     }
 
     public Iterator<Class<?>> getConverterTypes()
     {
         weaveDelegate();
         return _delegate.getConverterTypes();
     }
 
     @SuppressWarnings("deprecation")
     public MethodBinding createMethodBinding(String s, Class[] classes) throws ReferenceSyntaxException
     {
         weaveDelegate();
         return _delegate.createMethodBinding(s, classes);
     }
 
     public Iterator<Locale> getSupportedLocales()
     {
         weaveDelegate();
         return _delegate.getSupportedLocales();
     }
 
     public void setSupportedLocales(Collection<Locale> locales)
     {
         weaveDelegate();
         _delegate.setSupportedLocales(locales);
     }
 
     public void addValidator(String validatorId, String validatorClass)
     {
         weaveDelegate();
 /*        if (validatorClass.equals(PurgedValidator.class.getName())) {
             //purged case we do a full rescane
             WeavingContext.getWeaver().fullClassScan();
             Validator componentToChange = _delegate.createValidator(validatorId);
             if (componentToChange instanceof PurgedValidator) {
                 //Null not allowed here, but we set a purted validator to make
                 //sure that we get errors on the proper level
                 _delegate.addValidator(validatorId, PurgedValidator.class.getName());
 
             }
             return;
         } */
         _delegate.addValidator(validatorId, validatorClass);
     }
 
     public Validator createValidator(String validatorId) throws FacesException
     {
         weaveDelegate();
 
         Validator retVal = _delegate.createValidator(validatorId);
 
         //the validators are recreated every request we do not have to deal with them on method level
         Validator newRetVal = (Validator) reloadInstance(retVal, ARTIFACT_TYPE_VALIDATOR);
         if (newRetVal != retVal)
         {
             _delegate.createValidator(validatorId);
         }
         return retVal;
     }
 
     public Iterator<String> getValidatorIds()
     {
         weaveDelegate();
         return _delegate.getValidatorIds();
     }
 
     @SuppressWarnings("deprecation")
     public ValueBinding createValueBinding(String s) throws ReferenceSyntaxException
     {
         weaveDelegate();
         return _delegate.createValueBinding(s);
     }
 
     /*<> @Override  public void addBehavior(String behaviorId, String behaviorClass)
    {
        weaveDelegate();
 
        if (behaviorClass.equals(PurgedValidator.class.getName()))
        {
            //purged case we do a full rescan
            WeavingContext.getInstance().getWeaver().fullClassScan();
            Behavior behavior = _delegate.createBehavior(behaviorId);
            _behaviors.put(behaviorId, behaviorClass);
            if (behavior instanceof PurgedBehavior)
            {
                //Null not allowed here, but we set a purged validator to make
                //sure that we get errors on the proper level
                _delegate.addBehavior(behaviorId, PurgedBehavior.class.getName());
                _behaviors.remove(behaviorId);
 
            }
            return;
        }
 
        _delegate.addBehavior(behaviorId, behaviorClass);
    } */
 
     @Override
     public void addDefaultValidatorId(String validatorId)
     {
         weaveDelegate();
         _delegate.addDefaultValidatorId(validatorId);
     }
 
     @Override
     public Behavior createBehavior(String behaviorId) throws FacesException
     {
         weaveDelegate();
         Behavior retVal = _delegate.createBehavior(behaviorId);
 
         //we might have casts here against one of the parents
         //of this object
         Behavior newBehavior = (Behavior) reloadInstance(retVal, ARTIFACT_TYPE_BEHAVIOR);
         if (newBehavior != retVal)
         {
             return _delegate.createBehavior(behaviorId);
         }
 
         return retVal;
     }
 
     @Override
     public UIComponent createComponent(FacesContext facesContext, Resource resource)
     {
         weaveDelegate();
 
         UIComponent oldComponent = _delegate.createComponent(facesContext, resource);
 
         /*we are reweaving on the fly because we cannot be sure if
          * the class is not recycled all the time in the creation
          * code, in the renderer we do it on method base
          * due to the fact that our renderers are recycled via
          * a flyweight pattern*/
         UIComponent component = (UIComponent) reloadInstance(oldComponent, ARTIFACT_TYPE_COMPONENT);
 
         //we now have to check for an annotation change, but only in case a reload has happened
         /*<>if (component.getClass().hashCode() != oldComponent.getClass().hashCode())
         {
             return handleAnnotationChange(component, facesContext, resource);
         }*/
 
         return component;
 
     }
 
     @Override
     public UIComponent createComponent(FacesContext facesContext, String componentType, String rendererType)
     {
         weaveDelegate();
         UIComponent oldComponent = _delegate.createComponent(facesContext, componentType, rendererType);
 
         /*we are reweaving on the fly because we cannot be sure if
          * the class is not recycled all the time in the creation
          * code, in the renderer we do it on method base
          * due to the fact that our renderers are recycled via
          * a flyweight pattern*/
         UIComponent component = (UIComponent) reloadInstance(oldComponent, ARTIFACT_TYPE_COMPONENT);
 
         //we now have to check for an annotation change, but only in case a reload has happened
         /*<>if (component.getClass().hashCode() != oldComponent.getClass().hashCode())
         {
             return handleAnnotationChange(component, facesContext, componentType, rendererType);
         } */
 
         return component;
     }
 
     @Override
     public UIComponent createComponent(ValueExpression valueExpression, FacesContext facesContext, String s, String s1)
     {
         weaveDelegate();
         UIComponent oldComponent = _delegate.createComponent(valueExpression, facesContext, s, s1);
 
         /*we are reweaving on the fly because we cannot be sure if
      * the class is not recycled all the time in the creation
      * code, in the renderer we do it on method base
      * due to the fact that our renderers are recycled via
      * a flyweight pattern*/
         UIComponent component = (UIComponent) reloadInstance(oldComponent, ARTIFACT_TYPE_COMPONENT);
 
         //we now have to check for an annotation change, but only in case a reload has happened
         /*if (component.getClass().hashCode() != oldComponent.getClass().hashCode())
         {
             return handleAnnotationChange(component, valueExpression, facesContext, s, s1);
         } <>*/
 
         return component;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T evaluateExpressionGet(FacesContext facesContext, String s, Class<? extends T> aClass) throws ELException
     {
         weaveDelegate();
         //good place for a dynamic reloading check as well
         T retVal = _delegate.evaluateExpressionGet(facesContext, s, aClass);
         if (WeavingContext.getInstance().isDynamic(retVal.getClass()))
             retVal = (T) WeavingContext.getInstance().reload(retVal, ARTIFACT_TYPE_MANAGEDBEAN);
         return retVal;
     }
 
     @Override
     public Iterator<String> getBehaviorIds()
     {
         weaveDelegate();
        return _behaviors.keySet().iterator();
        //return _delegate.getBehaviorIds();
     }
 
     @Override
     public Map<String, String> getDefaultValidatorInfo()
     {
         weaveDelegate();
         return _delegate.getDefaultValidatorInfo();
     }
 
     @Override
     public ProjectStage getProjectStage()
     {
         weaveDelegate();
         return _delegate.getProjectStage();
     }
 
     @Override
     public ResourceHandler getResourceHandler()
     {
         weaveDelegate();
         ResourceHandler retVal = _delegate.getResourceHandler();
 
         /*if (WeavingContext.isDynamic(retVal.getClass())) {
            ResourceHandler newHandler = (ResourceHandler) reloadInstance(retVal, ScriptingConst.ARTIFACT_TYPE_RESOURCEHANDLER);
            if (newHandler != retVal) {
                _delegate.setResourceHandler(newHandler);
                return newHandler;
            }
        } */
         return retVal;
     }
 
     @Override
     public void publishEvent(FacesContext facesContext, Class<? extends SystemEvent> eventClass, Class<?> sourceBaseTye, Object source)
     {
         weaveDelegate();
         _delegate.publishEvent(facesContext, eventClass, sourceBaseTye, source);
     }
 
     @Override
     public void publishEvent(FacesContext facesContext, Class<? extends SystemEvent> eventClass, Object source)
     {
         weaveDelegate();
         _delegate.publishEvent(facesContext, eventClass, source);
     }
 
     @Override
     public void setResourceHandler(ResourceHandler resourceHandler)
     {
         weaveDelegate();
         if (WeavingContext.getInstance().isDynamic(resourceHandler.getClass()))
         {
             ResourceHandler proxy = new ResourceHandlerProxy(resourceHandler);
             resourceHandler = proxy;
         }
 
         _delegate.setResourceHandler(resourceHandler);
         //ResourceHandler handler = _delegate.getResourceHandler();
         //if (handler instanceof PurgedResourceHandler) {
         //    WeavingContext.getWeaver().fullClassScan();
         //}
     }
 
     @Override
     public void subscribeToEvent(Class<? extends SystemEvent> eventClass, Class<?> aClass, SystemEventListener systemEventListener)
     {
         weaveDelegate();
         systemEventListener = makeEventProxy(eventClass, systemEventListener);
         _delegate.subscribeToEvent(eventClass, aClass, systemEventListener);
     }
 
     private SystemEventListener makeEventProxy(Class<? extends SystemEvent> eventClass, SystemEventListener systemEventListener)
     {
         if (WeavingContext.getInstance().isDynamic(systemEventListener.getClass()))
         {
             systemEventListener = new SystemEventListenerProxy(systemEventListener);
             EventHandlerProxyEntry entry = new EventHandlerProxyEntry(eventClass, (Decorated) systemEventListener);
             _eventHandlerIdx.put(entry, entry);
         }
         return systemEventListener;
     }
 
     @Override
     public void subscribeToEvent(Class<? extends SystemEvent> eventClass, SystemEventListener systemEventListener)
     {
         weaveDelegate();
         systemEventListener = makeEventProxy(eventClass, systemEventListener);
         _delegate.subscribeToEvent(eventClass, systemEventListener);
     }
 
     @Override
     public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass, Class<?> aClass, SystemEventListener systemEventListener)
     {
         weaveDelegate();
         systemEventListener = resolveEventProxy(eventClass, systemEventListener);
         _delegate.unsubscribeFromEvent(eventClass, aClass, systemEventListener);
     }
 
     private SystemEventListener resolveEventProxy(Class<? extends SystemEvent> eventClass, SystemEventListener systemEventListener)
     {
         if (WeavingContext.getInstance().isDynamic(systemEventListener.getClass()))
         {
             systemEventListener = new SystemEventListenerProxy(systemEventListener);
             EventHandlerProxyEntry entry = new EventHandlerProxyEntry(eventClass, (Decorated) systemEventListener);
             entry = _eventHandlerIdx.remove(entry);
             if (entry != null)
             {
                 systemEventListener = (SystemEventListener) entry.getProxy().getDelegate();
             }
         }
         return systemEventListener;
     }
 
     @Override
     public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass, SystemEventListener systemEventListener)
     {
         weaveDelegate();
         systemEventListener = resolveEventProxy(eventClass, systemEventListener);
         _delegate.unsubscribeFromEvent(eventClass, systemEventListener);
     }
 
     public Object getDelegate()
     {
         return _delegate;
     }
 
     private Object reloadInstance(Object instance, int artifactType)
     {
         if (instance == null)
         {
             return null;
         }
         if (WeavingContext.getInstance().isDynamic(instance.getClass()))
         {
             instance = WeavingContext.getInstance().reload(instance, artifactType);
         }
         return instance;
     }
 
     /* private boolean alreadyWovenInRequest(String clazz)
     {
         //portlets now can be enabled thanks to the jsf2 indirections regarding the external context
         Map<String, Object> req = WeavingContext.getRequestMap();
         if (req.get(SCRIPTING_REQUSINGLETON + clazz) == null)
         {
             req.put(SCRIPTING_REQUSINGLETON + clazz, "");
             return false;
         }
         return true;
     }*/
 
     /*<> private UIComponent handleAnnotationChange(UIComponent oldComponent, ValueExpression valueExpression,
                                               FacesContext facesContext, String componentType)
   {
       UIComponent componentToChange = _delegate.createComponent(valueExpression, facesContext, componentType);
       if (componentToChange instanceof PurgedComponent)
       {
           WeavingContext.getWeaver().fullClassScan();
           //via an additional create component we can check whether a purged component
           //was registered after the reload because the annotation has been removed
           componentToChange = _delegate.createComponent(valueExpression, facesContext, componentType);
 
           return componentToChange;
       }
       return oldComponent;
   }  */
 
     /*<>  private UIComponent handleAnnotationChange(UIComponent oldComponent, String componentType)
     {
         UIComponent componentToChange = _delegate.createComponent(componentType);
         if (componentToChange instanceof PurgedComponent)
         {
             WeavingContext.getWeaver().fullClassScan();
             //via an additional create component we can check whether a purged component
             //was registered after the reload because the annotation has been removed
             componentToChange = _delegate.createComponent(componentType);
 
             return componentToChange;
         }
         return oldComponent;
     }*/
 
     /*<>@SuppressWarnings("deprecation") private UIComponent handleAnnotationChange(UIComponent oldComponent, ValueBinding valueBinding,
                                                FacesContext context, String componentType)
    {
        UIComponent componentToChange = _delegate.createComponent(valueBinding, context, componentType);
        if (componentToChange instanceof PurgedComponent)
        {
            WeavingContext.getWeaver().fullClassScan();
            //via an additional create component we can check whether a purged component
            //was registered after the reload because the annotation has been removed
            componentToChange = _delegate.createComponent(valueBinding, context, componentType);
 
            return componentToChange;
        }
        return oldComponent;
    } */
 
     /*<> private UIComponent handleAnnotationChange(UIComponent oldComponent, FacesContext context, Resource resource)
    {
        UIComponent componentToChange = _delegate.createComponent(context, resource);
        if (componentToChange instanceof PurgedComponent)
        {
            WeavingContext.getWeaver().fullClassScan();
            //via an additional create component we can check whether a purged component
            //was registered after the reload because the annotation has been removed
            componentToChange = _delegate.createComponent(context, resource);
 
            return componentToChange;
        }
        return oldComponent;
    }
 
    private UIComponent handleAnnotationChange(UIComponent oldComponent, FacesContext context, String componentType, String rendererType)
    {
        UIComponent componentToChange = _delegate.createComponent(context, componentType, rendererType);
        if (componentToChange instanceof PurgedComponent)
        {
            WeavingContext.getWeaver().fullClassScan();
            //via an additional create component we can check whether a purged component
            //was registered after the reload because the annotation has been removed
            componentToChange = _delegate.createComponent(context, componentType, rendererType);
 
            return componentToChange;
        }
        return oldComponent;
    }
 
    private UIComponent handleAnnotationChange(UIComponent oldComponent, ValueExpression valueExpression, FacesContext facesContext, String s, String s1)
    {
        UIComponent componentToChange = _delegate.createComponent(valueExpression, facesContext, s, s1);
        if (componentToChange instanceof PurgedComponent)
        {
            WeavingContext.getWeaver().fullClassScan();
 
            //via an additional create component we can check whether a purged component
            //was registered after the reload because the annotation has been removed
 
            componentToChange = _delegate.createComponent(valueExpression, facesContext, s, s1);
 
            return componentToChange;
        }
        return oldComponent;
    } */
 
 }
