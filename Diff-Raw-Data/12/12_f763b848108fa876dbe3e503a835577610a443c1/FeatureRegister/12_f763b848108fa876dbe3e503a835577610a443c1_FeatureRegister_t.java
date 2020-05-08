 package ru.iitp.proling.features;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Feature register that builds a feature from its name and arguments.
  * 
  * To use a {@link Feature} class as a function one must register it in the builder
  * through the <code>register()</code> function.
  * 
  * <p>
  * By default, comes with registered {@link CommonFeatures.Tuple} class
  * 
  * <p>
  * Contract: 
  * A suitable <@link Feature} class must have a constructor that accepts 
  * array of {@link Value} objects (a <code>Value[]</code> signature)
  *
  * @author Anton Kazennikov
  *
  */
 public class FeatureRegister {
 	Map<String, FeatureBuilder> builders = new HashMap<String, FeatureBuilder>();
 	
 	public FeatureRegister() {
 		register("tuple", CommonFeatures.Tuple.class);
 	}
 	
 	public Feature build(String name, List<Value> values) {
 		FeatureBuilder b = builders.get(name);
 		if(b != null)
 			return b.build(values);
 		
 		return null;
 	}
 	
 	public void register(String name, FeatureBuilder fb) {
 		builders.put(name, fb);
 	}
 	
 	public void register(String name, Class<? extends Feature> cls) {
 		register(name, builderFor(cls));
 	}
 	
 	public static FeatureBuilder builderFor(final Class<? extends Feature> cls) {
 		return new FeatureBuilder.Class(cls);
 	}

	public boolean contains(String name) {
		return builders.containsKey(name);
	}
 	
 	
 
 }
