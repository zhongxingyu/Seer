 package name.kazennikov.annotations;
 
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.Maps;
 
 public class Annotation {
 	Document doc;
 	
 	String name;
 	int start;
 	int end;
 	
 	Map<String, Object> features = Maps.newHashMap();
 	
 	public Annotation(String name, int start, int end) {
 		this.name = name;
 		this.start = start;
 		this.end = end;
 	}
 	
 	public Document getDoc() {
 		return doc;
 	}
 	
 	public void setDoc(Document doc) {
 		this.doc = doc;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public int getStart() {
 		return start;
 	}
 	
 	public int getEnd() {
 		return end;
 	}
 	
 	public void setStart(int start) {
 		this.start = start;
 	}
 
 	public void setEnd(int end) {
 		this.end = end;
 	}
 
 	public Set<String> getFeatureNames() {
 		return features.keySet();
 	}
 	public Object getFeature(String feat) {
 		return features.get(feat);
 	}
 	
 	public <E> E getFeature(String feat, Class<E> cls) {
 		return cls.cast(getFeature(feat));
 	}
 	
 	public void setFeature(String feat, Object value) {
 		features.put(feat, value);
 	}
 	
 	public Map<String, Object> getFeatureMap() {
 		return features;
 	}
 	
	public String toString(Document d) {
 		return String.format("'%s'@%s[%d,%d]%s", getText(), name, start, end, features);
 	}
 	
 	
 	public String getText() {
 		return doc.getText().substring(start, end);
 	}
 	
 	public boolean isEmpty() {
 		return start == end;
 	}
 	
 	public <E> E as(Class<E> cls) {
 		return cls.cast(this);
 	}
 
 }
