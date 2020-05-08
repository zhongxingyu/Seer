 package com.github.gabrielmoreira.propriete;
 
 import static com.github.gabrielmoreira.propriete.Strings.*;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import com.github.gabrielmoreira.propriete.transformer.AddPrefixPropertyKeyTransformer;
 import com.github.gabrielmoreira.propriete.transformer.PropertyKeyTransformer;
 import com.github.gabrielmoreira.propriete.visitor.CreateSectionPropertyVisitor;
 
 public class DynamicConfig implements InvocationHandler {
 
 	private ConfigContext configContext;
 	private Class<?> type;
 	private String path;
 	private String delimiter = ".";
 	private Map<Method, ExecutionHandler> executionHandlers = new HashMap<Method, ExecutionHandler>();
 
 	public DynamicConfig(ConfigContext configContext, Class<?> type) {
 		this(configContext, type, null, null);
 	}
 
 	public DynamicConfig(ConfigContext configContext, Class<?> type, String parentPath, String prefix) {
 		this.configContext = configContext;
 		this.type = type;
 		this.path = buildPath(type, parentPath, prefix);
 	}
 
 	private String buildPath(Class<?> type, String parentPath, String prefix) {
 		if (prefix != null)
 			return buildPath(parentPath, prefix);
 		Config config = type.getAnnotation(Config.class);
 		if (config != null)
 			return buildPath(parentPath, config.prefix());
 		return parentPath;
 	}
 
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		return getOrCreateExecutionHandler(method).execute(args);
 	}
 
 	private ExecutionHandler getOrCreateExecutionHandler(Method method) {
 		ExecutionHandler executionHandler = executionHandlers.get(method);
 		if (executionHandler == null) {
 			executionHandler = createExecutionHandler(method);
 			executionHandlers.put(method, executionHandler);
 		}
 		return executionHandler;
 	}
 
 	private ExecutionHandler createExecutionHandler(Method method) {
 		PropertyConfigAdapter propertyConfigAdapter = new PropertyConfigAdapter(method);
 		if (propertyConfigAdapter.isConfigurationType())
 			return new GetDynamicConfigExecutionHandler(propertyConfigAdapter);
 		if (propertyConfigAdapter.isSection())
 			return new GetSectionConfigExecutionHandler(propertyConfigAdapter);
 		return new GetPropertyExecutionHandler(propertyConfigAdapter);
 	}
 
 	private interface ExecutionHandler {
 		public Object execute(Object[] args);
 	}
 
 	private class GetDynamicConfigExecutionHandler implements ExecutionHandler {
 		private Object instance;
 
 		public GetDynamicConfigExecutionHandler(PropertyConfigAdapter propertyConfigAdapter) {
 			DynamicConfig dynamicConfig = new DynamicConfig(configContext, propertyConfigAdapter.getType(), getChildPath(propertyConfigAdapter), getName(propertyConfigAdapter));
 			this.instance = new Propriete(configContext).getInstance(dynamicConfig);
 		}
 
 		protected String getName(PropertyConfigAdapter propertyConfigAdapter) {
 			return propertyConfigAdapter.getDefinedKey() == null ? propertyConfigAdapter.getDefinedName() : "";
 		}
 
 		protected String getChildPath(PropertyConfigAdapter propertyConfigAdapter) {
 			String childPath = propertyConfigAdapter.getDefinedKey();
 			return childPath != null ? childPath : path;
 		}
 
 		public Object execute(Object[] args) {
 			return instance;
 		}
 	}
 
 	private class GetSectionConfigExecutionHandler implements ExecutionHandler {
 		private String propertyKey;
 		private boolean required;
 		private String propertyNewKeyPrefix;
 
 		public GetSectionConfigExecutionHandler(PropertyConfigAdapter propertyConfigAdapter) {
 			this.propertyKey = propertyConfigAdapter.getKey();
 			this.propertyNewKeyPrefix = propertyConfigAdapter.getNewKeyPrefix();
 			this.required = propertyConfigAdapter.isRequired();
 		}
 
 		public Object execute(Object[] args) {
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			Map<String, Object> section = (Map<String, Object>) (Map) new Properties();
			PropertyKeyTransformer propertyKeyTransformer = propertyNewKeyPrefix == null ? PropertyKeyTransformer.NOOP : new AddPrefixPropertyKeyTransformer(propertyNewKeyPrefix + delimiter, propertyKey.length() + 1);
 			CreateSectionPropertyVisitor sectionPropertyVisitor = new CreateSectionPropertyVisitor(section, propertyKey, propertyKeyTransformer);
 			configContext.visit(sectionPropertyVisitor, true);
 			if (required && section.isEmpty())
 				throw new RequiredPropertyException("Property section '" + propertyKey + "' not found!");
 			configContext.resolvePlaceholders(section);
 			return section;
 		}
 	}
 
 	private class GetPropertyExecutionHandler implements ExecutionHandler {
 		private String propertyKey;
 		private Class<?> propertyType;
 		private boolean required;
 		private String defaultValue;
 
 		public GetPropertyExecutionHandler(PropertyConfigAdapter propertyConfigAdapter) {
 			this.propertyKey = propertyConfigAdapter.getKey();
 			this.propertyType = propertyConfigAdapter.getType();
 			this.required = propertyConfigAdapter.isRequired();
 			this.defaultValue = propertyConfigAdapter.getDefaultValue();
 		}
 
 		public Object execute(Object[] args) {
 			Object object = configContext.getAs(propertyKey, true, propertyType, defaultValue);
 			if (object == null && (required || this.propertyType.isPrimitive())) {
 				throw new RequiredPropertyException("Property '" + propertyKey + "' not found!");
 			}
 			return object == null ? null : object;
 		}
 	}
 
 	private class PropertyConfigAdapter {
 
 		private Method method;
 
 		public PropertyConfigAdapter(Method method) {
 			this.method = method;
 		}
 
 		public boolean isSection() {
 			Class<?> type = getType();
 			if (type.isAssignableFrom(Properties.class))
 				return true;
 			if (type.isAssignableFrom(Map.class))
 				return true;
 			return false;
 		}
 
 		public boolean isRequired() {
 			return !getConfigProperty().optional();
 		}
 
 		public Class<?> getType() {
 			return method.getReturnType();
 		}
 
 		public boolean isConfigurationType() {
 			return method.getReturnType().isAnnotationPresent(Config.class);
 		}
 
 		public ConfigProperty getConfigProperty() {
 			ConfigProperty configProperty = method.getAnnotation(ConfigProperty.class);
 			return configProperty == null ? Defaults.getDefaultConfigProperty() : configProperty;
 		}
 
 		public String getName() {
 			String name = getDefinedName();
 			return name != null ? name : getJavaBeanName();
 		}
 
 		public String getDefinedName() {
 			return normalize(getConfigProperty().name());
 		}
 
 		public String getNewKeyPrefix() {
 			return normalize(getConfigProperty().newKeyPrefix());
 		}
 
 		public String getKey() {
 			String key = getDefinedKey();
 			return key != null ? key : buildPath(path, getName());
 		}
 
 		public String getDefinedKey() {
 			return normalize(getConfigProperty().key());
 		}
 
 		public String getDefaultValue() {
 			return normalize(getConfigProperty().defaultValue());
 		}
 
 		public String getJavaBeanName() {
 			return toJavaBeanName(method.getName());
 		}
 
 	}
 
 	private static class Defaults {
 		private static ConfigProperty CONFIG_PROPERTY;
 
 		@ConfigProperty()
 		public static ConfigProperty getDefaultConfigProperty() {
 			if (CONFIG_PROPERTY == null) {
 				try {
 					CONFIG_PROPERTY = Defaults.class.getDeclaredMethod("getDefaultConfigProperty").getAnnotation(ConfigProperty.class);
 				} catch (Exception e) {
 					throw new ProprieteException(e);
 				}
 			}
 			return CONFIG_PROPERTY;
 		}
 	}
 
 	public Class<?> getType() {
 		return type;
 	}
 
 	private String buildPath(String... segments) {
 		return Strings.buildPath(delimiter, segments);
 	}
 
 }
