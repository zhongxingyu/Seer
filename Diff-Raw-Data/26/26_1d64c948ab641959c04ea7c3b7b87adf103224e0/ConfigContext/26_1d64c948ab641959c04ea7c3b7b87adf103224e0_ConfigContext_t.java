 package com.github.gabrielmoreira.propriete;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import com.github.gabrielmoreira.propriete.converter.Converter;
 import com.github.gabrielmoreira.propriete.filter.PropertyFilter;
 import com.github.gabrielmoreira.propriete.placeholder.ConfigPlaceholderResolver;
 import com.github.gabrielmoreira.propriete.source.ConfigSource;
 import com.github.gabrielmoreira.propriete.visitor.PropertyVisitor;
 
 public class ConfigContext {
 
 	private ConfigPlaceholderResolver placeholderResolver;
 	private ConfigSource configSource;
 	private Converter converter;
 
 	public ConfigContext(ConfigSource configSource, Converter converter) {
 		this.configSource = configSource;
 		this.converter = converter;
 	}
 
 	public ConfigContext(ConfigSource configSource, Converter converter, ConfigPlaceholderResolver configPlaceholderResolver) {
 		this(configSource, converter);
 		this.placeholderResolver = configPlaceholderResolver;
 	}
 
 	public Object get(String propertyKey, boolean resolvePlaceholders) {
 		return get(propertyKey, resolvePlaceholders, (Object) null);
 	}
 
 	public Object get(String propertyKey, boolean resolvePlaceholders, Object defaultValue) {
 		Object value = configSource.get(propertyKey);
 		if (value == null)
 			value = defaultValue;
 		return resolvePlaceholders ? resolvePlaceholders(value) : value;
 
 	}
 
 	public <T> T getAs(String propertyKey, boolean resolvePlaceholders, Class<T> type) {
 		return getAs(propertyKey, resolvePlaceholders, type, null);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T getAs(String propertyKey, boolean resolvePlaceholders, Class<T> type, Object defaultValue) {
 		return (T) convert(get(propertyKey, resolvePlaceholders, defaultValue), type);
 	}
 
 	public Object convert(Object object, Class<?> propertyType) {
 		return converter.convert(object, propertyType);
 	}
 
 	public Object resolvePlaceholders(Object value) {
 		if (placeholderResolver != null)
 			return placeholderResolver.resolvePlaceholders(this, value);
 		return value;
 	}
 
 	public Map<String, Object> resolvePlaceholders(Map<String, Object> values) {
 		return resolvePlaceholders(values, new LinkedHashMap<String, Object>());
 	}
 
 	public Map<String, Object> resolvePlaceholders(Map<String, Object> values, Map<String, Object> newMap) {
 		newMap.putAll(values);
 		if (placeholderResolver != null) {
 			for (Map.Entry<String, Object> entry : newMap.entrySet()) {
 				entry.setValue(resolvePlaceholders(entry.getValue()));
 			}
 		}
 		return newMap;
 	}
 
 	public Map<String, Object> startsWith(String prefix, boolean resolvePlaceholders) {
 		Map<String, Object> result = configSource.startsWith(prefix);
 		return resolvePlaceholders ? resolvePlaceholders(result) : result;
 	}
 
 	public Map<String, Object> all(boolean resolvePlaceholders) {
 		Map<String, Object> result = configSource.all();
 		return resolvePlaceholders ? resolvePlaceholders(result) : result;
 	}
 
 	public Map<String, Object> filter(PropertyFilter propertyFilter, boolean resolvePlaceholders) {
 		Map<String, Object> result = configSource.filter(propertyFilter);
 		return resolvePlaceholders ? resolvePlaceholders(result) : result;
 	}
 
 	public void visit(final PropertyVisitor propertyVisitor, final boolean resolvePlaceholder) {
		configSource.visit(resolvePlaceholder ? new ResolvePlaceholderPropertyVisitor(propertyVisitor) : propertyVisitor);
	}

	private class ResolvePlaceholderPropertyVisitor implements PropertyVisitor {
		private PropertyVisitor visitor;

		public ResolvePlaceholderPropertyVisitor(PropertyVisitor visitor) {
			this.visitor = visitor;
		}

		public void visit(String key, Object value) {
			visitor.visit(key, resolvePlaceholders(value));

		}
 	}
 
 }
