 package org.kisst.cfg4j;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 public class LayeredProps extends PropsBase {
 	private final ArrayList<Props> layers= new ArrayList<Props>();
 
 	@Override
 	public Object get(String key, Object defaultValue) {
 		for (Props layer: layers) {
 			Object o=layer.get(key,null);
 			if (o!=null)
 				return o;
 		}
 		return defaultValue;
 	}
 	
	public void addLayer(Props props) { if (props!=null) layers.add(props); }
 
 	public Iterable<String> keys() {
 		HashSet<String> result= new HashSet<String>();
 		for (Props layer: layers) {
 			for (String key: layer.keys())
 				result.add(key);
 		}
 		return result; 
 	}

	public String toString() {
		StringBuilder result=new StringBuilder();
		for (Props layer: layers)
			result.append(layer.toString()).append("\n");
		return result.toString(); 
	}

 }
