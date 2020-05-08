 package com.mtbaker.client.provider.xml;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import com.mtbaker.client.Configuration;
 import com.mtbaker.client.provider.io.InputStreamSource;
 
 public class XmlConfiguration implements Configuration {
 
 	private Map<String, List<String>> values = Collections.EMPTY_MAP;
 
 	private InputStreamSource source;
 
 	private String namespace;
 
 	private int ttlMillis;
 
 	private ConfigurationType configs;
 
 	private long lastReadTime;
 
 	public XmlConfiguration(InputStreamSource source, String namespace,
 			int ttlMillis) throws IOException {
 		this.source = source;
 		this.namespace = namespace;
 		this.ttlMillis = ttlMillis;
 		load();
 	}
 
 	@Override
 	public String getString(String key, String defaultValue) throws IOException {
 		List<String> v = values.get(key);
 		return ((v == null) || (v.size() == 0))
 				? defaultValue
 				: v.get(0);
 	}
 
 	@Override
 	public boolean getBoolean(String key, boolean defaultValue)
 			throws IOException {
 		String s = getString(key, Boolean.toString(defaultValue));
 		return Boolean.parseBoolean(s);
 	}
 
 	@Override
 	public int getInteger(String key, int defaultValue) throws IOException {
 		String s = getString(key, Integer.toString(defaultValue));
 		return Integer.parseInt(s);
 	}
 
 	@Override
 	public long getLong(String key, long defaultValue) throws IOException {
 		String s = getString(key, Long.toString(defaultValue));
 		return Long.parseLong(s);
 	}
 
 	@Override
 	public double getDouble(String key, double defaultValue) throws IOException {
 		String s = getString(key, Double.toString(defaultValue));
 		return Double.parseDouble(s);
 	}
 
 	@Override
 	public List<String> getStringList(String key, List<String> defaultValue)
 			throws IOException {
 		List<String> v = values.get(key);
 		return (v == null) ? defaultValue : v;
 	}
 
 	@Override
 	public List<Double> getDoubleList(String key, List<Double> defaultValue)
 			throws IOException {
 		List<String> v = values.get(key);
 		if (v == null)
 			return defaultValue;
 		List<Double> dest = new ArrayList<Double>(v.size());
 		for (String s : v)
 			dest.add(Double.parseDouble(s));
 		return dest;
 	}
 
 	@Override
 	public List<Integer> getIntegerList(String key, List<Integer> defaultValue)
 			throws IOException {
 		List<String> v = values.get(key);
 		if (v == null)
 			return defaultValue;
 		List<Integer> dest = new ArrayList<Integer>(v.size());
 		for (String s : v)
 			dest.add(Integer.parseInt(s));
 		return dest;
 	}
 
 	@Override
 	public List<Long> getLongList(String key, List<Long> defaultValue)
 			throws IOException {
 		List<String> v = values.get(key);
 		if (v == null)
 			return defaultValue;
 		List<Long> dest = new ArrayList<Long>(v.size());
 		for (String s : v)
 			dest.add(Long.parseLong(s));
 		return dest;
 	}
 
 	protected void load() throws IOException {
 		InputStream is = null;
 		try {
 			is = source.open();
 			JAXBContext jaxbContext = JAXBContext
 					.newInstance(ConfigurationType.class);
 			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 			ConfigurationType ct = (ConfigurationType) unmarshaller
 					.unmarshal(is);
 			//ConfigurationType ct = config.getValue();
 			loadMap(ct);
 			this.configs = ct;
 			this.lastReadTime = System.currentTimeMillis();
 		} catch (JAXBException e) {
			throw new RuntimeException();
 		} finally {
 			if (is != null)
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 		}
 	}
 
 	protected void loadMap(ConfigurationType ct) {
 		PropertySetType p = null;
 		List<PropertySetType> namespaces = ct.getNamespaces();
 		for (PropertySetType prop : namespaces) {
 			if (prop.getName().equals(this.namespace)) {
 				p = prop;
 				break;
 			}
 		}
 		if (p != null) {
 			List<PropertyType> props = p.getProperties();
 			Map<String, List<String>> newValuesMap = new HashMap<String, List<String>>(
 					props.size());
 			for (PropertyType prop : props) {
 				String k = prop.getName();
 				List<String> v = prop.getValues();
 				newValuesMap.put(k, v);
 			}
 			this.values = newValuesMap;
 		}
 	}
 }
