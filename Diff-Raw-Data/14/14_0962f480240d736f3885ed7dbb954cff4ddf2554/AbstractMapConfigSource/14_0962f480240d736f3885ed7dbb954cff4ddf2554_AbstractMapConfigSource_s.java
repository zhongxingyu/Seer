 package com.github.gabrielmoreira.propriete.source;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.github.gabrielmoreira.propriete.filter.PropertyFilter;
 import com.github.gabrielmoreira.propriete.filter.StartsWithPropertyFilter;
 import com.github.gabrielmoreira.propriete.transformer.MapTransformer;
 import com.github.gabrielmoreira.propriete.visitor.FilterPropertyVisitor;
 import com.github.gabrielmoreira.propriete.visitor.PropertyVisitor;
 
 public abstract class AbstractMapConfigSource implements ConfigSource {
 
 	private Map<String, Object> source;
 
 	public AbstractMapConfigSource(Map<String, Object> source) {
 		this.source = source;
 	}
 
 	public AbstractMapConfigSource(Map<String, Object> source, MapTransformer mapTransformer) {
		this.source = mapTransformer == null ? source : mapTransformer.transform(source);
 	}
 
 	protected Map<String, Object> getSource() {
 		return source;
 	}
 
 	public Object get(String key) {
 		return getSource().get(key);
 	}
 
 	public Map<String, Object> filter(PropertyFilter propertyFilter) {
 		FilterPropertyVisitor visitor = new FilterPropertyVisitor(propertyFilter);
 		visit(visitor);
 		return visitor.getFiltered();
 	}
 
 	public Map<String, Object> startsWith(String prefix) {
 		return filter(new StartsWithPropertyFilter(prefix));
 	}
 
 	public Map<String, Object> all() {
 		Map<String, Object> section = new HashMap<String, Object>();
 		section.putAll(getSource());
 		return section;
 	}
 
 	public void visit(PropertyVisitor propertyVisitor) {
 		for (Entry<String, Object> entry : getSource().entrySet()) {
 			propertyVisitor.visit(entry.getKey(), entry.getValue());
 		}
 	}
 
 }
