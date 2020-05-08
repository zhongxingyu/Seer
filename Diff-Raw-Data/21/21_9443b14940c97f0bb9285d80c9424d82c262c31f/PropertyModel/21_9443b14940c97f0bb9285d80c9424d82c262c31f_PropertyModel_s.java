 package net.cloudengine.rpc.controller.config;
 
 import java.io.Serializable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 public class PropertyModel implements Serializable {
 	
 	private static final long serialVersionUID = 2873331085992779804L;
	private static final ConcurrentMap<Class, Converter> converters = new ConcurrentHashMap<Class, Converter>();
 	
 	static {
		converters.put(Boolean.class, new BooleanConverter());
		converters.put(Long.class, new LongConverter());
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
		if (converters.containsKey(type)) {
			Converter<T> converter = converters.get(type);
 			return converter.convert(getValue());
 		} else {
 			throw new IllegalArgumentException("No existe un converter para la clase: "+type.getName());
 		}
 	}
 }
