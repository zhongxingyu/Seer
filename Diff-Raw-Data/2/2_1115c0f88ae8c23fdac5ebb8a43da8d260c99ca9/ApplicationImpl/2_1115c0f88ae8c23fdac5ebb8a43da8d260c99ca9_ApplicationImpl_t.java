 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.application;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.application.NavigationHandler;
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.BigDecimalConverter;
 import javax.faces.convert.BigIntegerConverter;
 import javax.faces.convert.BooleanConverter;
 import javax.faces.convert.ByteConverter;
 import javax.faces.convert.CharacterConverter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.DateTimeConverter;
 import javax.faces.convert.DoubleConverter;
 import javax.faces.convert.FloatConverter;
 import javax.faces.convert.IntegerConverter;
 import javax.faces.convert.LongConverter;
 import javax.faces.convert.ShortConverter;
 import javax.faces.el.MethodBinding;
 import javax.faces.el.PropertyResolver;
 import javax.faces.el.ReferenceSyntaxException;
 import javax.faces.el.ValueBinding;
 import javax.faces.el.VariableResolver;
 import javax.faces.event.ActionListener;
 import javax.faces.validator.Validator;
 
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.framework.util.DateConversionUtil;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.convert.TimestampConverter;
 import org.seasar.teeda.core.el.MethodBindingFactory;
 import org.seasar.teeda.core.el.ValueBindingFactory;
 import org.seasar.teeda.core.exception.ConverterInstantiateFailureException;
 import org.seasar.teeda.core.exception.NoMethodBindingContextException;
 import org.seasar.teeda.core.exception.NoValueBindingContextException;
 import org.seasar.teeda.core.util.ApplicationUtil;
 import org.seasar.teeda.core.util.IteratorUtil;
 import org.seasar.teeda.core.util.PropertyDescUtil;
 
 /**
  * @author shot
  */
 public class ApplicationImpl extends Application implements
         ConfigurationSupport {
 
     private static Map wellKnownConverters = new HashMap();
 
     private ActionListener listener = null;
 
     private Locale locale = null;
 
     private String renderKitId = null;
 
     private String bundle = null;
 
     private NavigationHandler navigationHandler = null;
 
     private PropertyResolver propertyResolver = null;
 
     private VariableResolver variableResolver = null;
 
     private ViewHandler viewHandler = null;
 
     private StateManager stateManager = null;
 
     private Map componentClassMap = Collections.synchronizedMap(new HashMap());
 
     private Map converterIdMap = Collections.synchronizedMap(new HashMap());
 
     private Map converterForClassMap = Collections
             .synchronizedMap(new HashMap());
 
     private Map converterConfigurationMap = Collections
             .synchronizedMap(new HashMap());
 
     private Map validatorMap = Collections.synchronizedMap(new HashMap());
 
     private Collection supportedLocales = Collections.EMPTY_SET;
 
     private ValueBindingFactory vbFactory = null;
 
     private MethodBindingFactory mbFactory = null;
 
     private ComponentLookupStrategy componentLookupStrategy;
 
     static {
         registerWellKnownConverters();
     }
 
     public ApplicationImpl() {
     }
 
     public static Converter getWellKnownConverter(Class clazz) {
         return (Converter) wellKnownConverters.get(clazz);
     }
 
     protected static void registerWellKnownConverters() {
         Converter converter = new BooleanConverter();
         wellKnownConverters.put(Boolean.class, converter);
         wellKnownConverters.put(boolean.class, converter);
         converter = new ByteConverter();
         wellKnownConverters.put(Byte.class, converter);
         wellKnownConverters.put(byte.class, converter);
         converter = new ShortConverter();
         wellKnownConverters.put(Short.class, converter);
         wellKnownConverters.put(short.class, converter);
         converter = new IntegerConverter();
         wellKnownConverters.put(Integer.class, converter);
         wellKnownConverters.put(int.class, converter);
         converter = new LongConverter();
         wellKnownConverters.put(Long.class, converter);
         wellKnownConverters.put(long.class, converter);
         converter = new FloatConverter();
         wellKnownConverters.put(Float.class, converter);
         wellKnownConverters.put(float.class, converter);
         converter = new DoubleConverter();
         wellKnownConverters.put(Double.class, converter);
         wellKnownConverters.put(double.class, converter);
         converter = new BigIntegerConverter();
         wellKnownConverters.put(BigInteger.class, converter);
         converter = new BigDecimalConverter();
         wellKnownConverters.put(BigDecimal.class, converter);
         converter = new CharacterConverter();
         wellKnownConverters.put(Character.class, converter);
         wellKnownConverters.put(char.class, converter);
         DateTimeConverter dateTimeConverter = new DateTimeConverter();
         dateTimeConverter.setPattern(DateConversionUtil.getY4Pattern(Locale
                 .getDefault()));
         wellKnownConverters.put(Date.class, dateTimeConverter);
         TimestampConverter timestampConverter = new TimestampConverter();
         timestampConverter.setPattern(DateConversionUtil.getY4Pattern(Locale
                 .getDefault()));
         wellKnownConverters.put(Timestamp.class, timestampConverter);
 
     }
 
     public ActionListener getActionListener() {
         return listener;
     }
 
     public void setActionListener(ActionListener listener) {
         AssertionUtil.assertNotNull("ActionListener is null.", listener);
         this.listener = listener;
     }
 
     public Locale getDefaultLocale() {
         return locale;
     }
 
     public void setDefaultLocale(Locale locale) {
         AssertionUtil.assertNotNull("Locale is null.", locale);
         this.locale = locale;
     }
 
     public String getDefaultRenderKitId() {
         return renderKitId;
     }
 
     public void setDefaultRenderKitId(String renderKitId) {
         this.renderKitId = renderKitId;
     }
 
     public String getMessageBundle() {
         return bundle;
     }
 
     public void setMessageBundle(String bundle) {
         AssertionUtil.assertNotNull("MessageBundle is null.", bundle);
         this.bundle = bundle;
     }
 
     public NavigationHandler getNavigationHandler() {
         return navigationHandler;
     }
 
     public void setNavigationHandler(NavigationHandler handler) {
         AssertionUtil.assertNotNull("NavigationHandler is null.", handler);
         this.navigationHandler = handler;
     }
 
     public PropertyResolver getPropertyResolver() {
         return propertyResolver;
     }
 
     public void setPropertyResolver(PropertyResolver resolver) {
         AssertionUtil.assertNotNull("PropertyResolver is null.", resolver);
         this.propertyResolver = resolver;
     }
 
     public VariableResolver getVariableResolver() {
         return variableResolver;
     }
 
     public void setVariableResolver(VariableResolver resolver) {
         AssertionUtil.assertNotNull("VariableResolver is null.", resolver);
         this.variableResolver = resolver;
     }
 
     public ViewHandler getViewHandler() {
         return viewHandler;
     }
 
     public void setViewHandler(ViewHandler handler) {
         AssertionUtil.assertNotNull("ViewHandler is null.", handler);
         this.viewHandler = handler;
     }
 
     public StateManager getStateManager() {
         return stateManager;
     }
 
     public void setStateManager(StateManager manager) {
         AssertionUtil.assertNotNull("StateManager is null.", manager);
         this.stateManager = manager;
     }
 
     public void addComponent(String componentType, String componentClassName) {
         if (StringUtil.isEmpty(componentType)) {
             throw new NullPointerException("componentType is null.");
         }
         if (StringUtil.isEmpty(componentClassName)) {
             throw new NullPointerException("componentClassName is null.");
         }
         Class clazz = ClassUtil.forName(componentClassName);
         ApplicationUtil.verifyClassType(UIComponent.class, clazz);
         componentClassMap.put(componentType, clazz);
     }
 
     public UIComponent createComponent(String componentType)
             throws FacesException {
         if (StringUtil.isEmpty(componentType)) {
             throw new NullPointerException("componentType is null.");
         }
         Object component = componentLookupStrategy
                 .getComponentByName(componentType);
         if (component != null) {
             return (UIComponent) component;
         }
         Class componentClass = (Class) componentClassMap.get(componentType);
         if (componentClass == null) {
            throw new FacesException("Undefined component type:"
                     + componentType);
         }
         return (UIComponent) ClassUtil.newInstance(componentClass);
     }
 
     public UIComponent createComponent(ValueBinding vb, FacesContext context,
             String componentType) throws FacesException {
         Object obj = vb.getValue(context);
         if (obj instanceof UIComponent) {
             return (UIComponent) obj;
         } else {
             UIComponent component = createComponent(componentType);
             vb.setValue(context, component);
             return component;
         }
     }
 
     public Iterator getComponentTypes() {
         return componentClassMap.keySet().iterator();
     }
 
     public void addConverter(String converterId, String converterClassName) {
         if (StringUtil.isEmpty(converterId)) {
             throw new NullPointerException("converterId is null");
         }
         if (StringUtil.isEmpty(converterClassName)) {
             throw new NullPointerException("converterClass is null");
         }
         Class clazz = ClassUtil.forName(converterClassName);
         ApplicationUtil.verifyClassType(Converter.class, clazz);
         converterIdMap.put(converterId, clazz);
     }
 
     public void addConverter(Class targetClass, String converterClassName) {
         AssertionUtil.assertNotNull("targetClass is null", targetClass);
         if (StringUtil.isEmpty(converterClassName)) {
             throw new NullPointerException("converterClass is null");
         }
         Class clazz = ClassUtil.forName(converterClassName);
         ApplicationUtil.verifyClassType(Converter.class, clazz);
         converterForClassMap.put(targetClass, clazz);
     }
 
     public Converter createConverter(String converterId) {
         AssertionUtil.assertNotNull("converterId is null", converterId);
         Object component = componentLookupStrategy
                 .getComponentByName(converterId);
         if (component != null) {
             return (Converter) component;
         }
         Class clazz = (Class) converterIdMap.get(converterId);
         try {
             Converter converter = createConverterByConverterClass(clazz);
             setConverterPropertiesFor(converterId, converter);
             return converter;
         } catch (Exception e) {
             Object[] args = { converterId };
             throw new ConverterInstantiateFailureException(args);
         }
     }
 
     public Converter createConverter(Class targetClass) {
         AssertionUtil.assertNotNull("targetClass is null", targetClass);
         return doCreateConverterByTargetClass(targetClass);
     }
 
     public void addConverterConfiguration(String converterId,
             ConverterConfiguration converterConfiguration) {
         if (StringUtil.isEmpty(converterId)) {
             throw new NullPointerException("converterId is null.");
         }
         AssertionUtil.assertNotNull("converterConfiguration is null",
                 converterConfiguration);
         List list = getConverterConfigurationList(converterId);
         list.add(converterConfiguration);
     }
 
     public void addConverterConfiguration(Class targetClass,
             ConverterConfiguration converterConfiguration) {
         AssertionUtil.assertNotNull("targetClass is null", targetClass);
         AssertionUtil.assertNotNull("converterConfiguration is null",
                 converterConfiguration);
         List list = getConverterConfigurationList(targetClass);
         list.add(converterConfiguration);
     }
 
     private List getConverterConfigurationList(Object key) {
         List list = (List) converterConfigurationMap.get(key);
         if (list == null) {
             list = new ArrayList();
             converterConfigurationMap.put(key, list);
         }
         return list;
     }
 
     private Converter createConverterByConverterClass(Class converterClass) {
         try {
             Converter converter = (Converter) ClassUtil
                     .newInstance(converterClass);
             return converter;
         } catch (Exception e) {
             Object[] args = { converterClass.getName() };
             throw new ConverterInstantiateFailureException(args);
         }
     }
 
     private Converter doCreateConverterByTargetClass(Class targetClass) {
         Converter converter = getWellKnownConverter(targetClass);
         if (converter != null) {
             return converter;
         }
         converter = createConverterByTargetClass(targetClass);
         if (converter == null) {
             converter = createConverterByInterface(targetClass);
         }
         if (converter == null) {
             converter = createConverterBySuperClass(targetClass);
         }
         if (converter == null) {
             converter = createConverterForPrimitive(targetClass);
         }
         if (converter != null) {
             setConverterPropertiesFor(targetClass, converter);
         }
         return converter;
     }
 
     private void setConverterPropertiesFor(Object key, Converter converter) {
         List list = (List) converterConfigurationMap.get(key);
         for (Iterator itr = IteratorUtil.getIterator(list); itr.hasNext();) {
             ConverterConfiguration config = (ConverterConfiguration) itr.next();
             if (config != null) {
                 String propertyName = config.getPropertyName();
                 PropertyDescUtil.setValue(converter, propertyName, config
                         .getDefaultValue());
             }
         }
     }
 
     protected Converter createConverterByTargetClass(Class targetClass) {
         Class converterClass = (Class) converterForClassMap.get(targetClass);
         if (converterClass != null) {
             return createConverterByConverterClass(converterClass);
         }
         return null;
     }
 
     protected Converter createConverterByInterface(Class targetClass) {
         Class[] interfaces = targetClass.getInterfaces();
         if (interfaces != null) {
             for (int i = 0; i < interfaces.length; i++) {
                 Converter converter = doCreateConverterByTargetClass(interfaces[i]);
                 if (converter != null) {
                     return converter;
                 }
             }
         }
         return null;
     }
 
     protected Converter createConverterBySuperClass(Class targetClass) {
         Class superClass = targetClass.getSuperclass();
         if (superClass != null) {
             return doCreateConverterByTargetClass(superClass);
         }
         return null;
     }
 
     protected Converter createConverterForPrimitive(Class targetClass) {
         Class primitiveClass = ClassUtil.getWrapperClass(targetClass);
         if (primitiveClass != null) {
             return doCreateConverterByTargetClass(primitiveClass);
         }
         return null;
     }
 
     public Iterator getConverterIds() {
         return converterIdMap.keySet().iterator();
     }
 
     public Iterator getConverterTypes() {
         return converterForClassMap.keySet().iterator();
     }
 
     public Iterator getSupportedLocales() {
         return supportedLocales.iterator();
     }
 
     public void setSupportedLocales(Collection supportedLocales) {
         AssertionUtil.assertNotNull("suppoertedLocales is null",
                 supportedLocales);
         this.supportedLocales = supportedLocales;
     }
 
     public void addValidator(String validatorId, String validatorClassName) {
         if (StringUtil.isEmpty(validatorId)) {
             throw new NullPointerException("Validator id is null.");
         }
         if (StringUtil.isEmpty(validatorClassName)) {
             throw new NullPointerException("Validator class is null.");
         }
         Class clazz = ClassUtil.forName(validatorClassName);
         ApplicationUtil.verifyClassType(Validator.class, clazz);
         validatorMap.put(validatorId, clazz);
     }
 
     public Validator createValidator(String validatorId) throws FacesException {
         AssertionUtil.assertNotNull("validatorId is null", validatorId);
         Object component = componentLookupStrategy
                 .getComponentByName(validatorId);
         if (component != null) {
             return (Validator) component;
         }
         Class validatorClass = (Class) validatorMap.get(validatorId);
         if (validatorClass == null) {
             throw new FacesException("Undefined validator class(validatorId = "
                     + validatorId + ")");
         }
         return (Validator) ClassUtil.newInstance(validatorClass);
     }
 
     public Iterator getValidatorIds() {
         return validatorMap.keySet().iterator();
     }
 
     public MethodBinding createMethodBinding(String ref, Class[] params)
             throws ReferenceSyntaxException {
         AssertionUtil.assertNotNull("ref is null", ref);
         if (mbFactory == null) {
             throw new NoMethodBindingContextException(ref, params);
         }
         return mbFactory.createMethodBinding(this, ref, params);
     }
 
     public ValueBinding createValueBinding(String ref)
             throws ReferenceSyntaxException {
         AssertionUtil.assertNotNull("ref is null", ref);
         if (vbFactory == null) {
             throw new NoValueBindingContextException(ref);
         }
         return vbFactory.createValueBinding(this, ref);
     }
 
     public void setValueBindingFactory(ValueBindingFactory vbContextFactory) {
         this.vbFactory = vbContextFactory;
     }
 
     public void setMethodBindingFactory(MethodBindingFactory mbContextFactory) {
         this.mbFactory = mbContextFactory;
     }
 
     public ValueBindingFactory getValueBindingFactory() {
         return vbFactory;
     }
 
     public MethodBindingFactory getMethodBindingFactory() {
         return mbFactory;
     }
 
     public ComponentLookupStrategy getComponentLookupStrategy() {
         return componentLookupStrategy;
     }
 
     public void setComponentLookupStrategy(
             ComponentLookupStrategy componentLookupStrategy) {
         this.componentLookupStrategy = componentLookupStrategy;
     }
 
 }
