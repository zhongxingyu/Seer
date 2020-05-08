 package net.cloudengine.rpc.controller.config;
 
 import java.io.Serializable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 public class PropertyModel implements Serializable {
 	
 	private static final long serialVersionUID = 2873331085992779804L;
	private static final ConcurrentMap<String, Converter<?>> converters = new ConcurrentHashMap<String, Converter<?>>();
 	
 	static {
		converters.put(Boolean.class.getName(), new BooleanConverter());
		converters.put(Long.class.getName(), new LongConverter());
 	}
 	
 	private String name;
 	private String value;
 	
 	public PropertyModel() {
 		super();
 	}
 	
 	public PropertyModel(String name, String value) {
 		super();
 		this.name = name;
 		this.value = value;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getValue() {
 		return value;
 	}
 	public void setValue(String value) {
 		this.value = value;
 	}
 	public <T> T getValue(Class<T> type) {
		String typeName = type.getName();
		if (converters.containsKey(typeName)) {
			@SuppressWarnings("unchecked")
			Converter<T> converter = (Converter<T>) converters.get(typeName);
 			return converter.convert(getValue());
 		} else {
 			throw new IllegalArgumentException("No existe un converter para la clase: "+type.getName());
 		}
 	}
 }
