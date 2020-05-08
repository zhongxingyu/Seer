 package rhythm;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Maps;
 
 public class Features {
 	private final Map<Feature<?>, Object> features = Maps.newHashMap();
 	
 	public <T> Features set(Feature<T> key, T value) {
 		features.put(key, value);
 		return this;
 	}
 	
 	public <T> Features maybeSet(Feature<T> key, T value) {
 		if (value != null)
 			features.put(key, value);
 		return this;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T get(Feature<T> key) {
 		return (T) features.get(key);
 	}
 	
 	public <T> T get(Feature<T> key, T defaultValue) {
 		return has(key) ? get(key) : defaultValue;
 	}
 	
 	public <T> boolean has(Feature<T> key, T value) {
 		return Objects.equal(get(key), value);
 	}
 	
 	public boolean has(Feature<?> key) {
 		return features.containsKey(key);
 	}
 	
 	public Features set(Feature<Void> key) {
 		features.put(key, null);
 		return this;
 	}
 	
 	public boolean is(Feature<Void> key) {
 		return features.containsKey(key);
 	}
 	
 	public void clearAllFeatures() {
 		features.clear();
 	}
 	
 	@Override
 	public String toString() {
 		return features.toString();
 	}
 	
 	public static <T> Predicate<Features> has_(final Feature<T> key, final T value) {
 		return new Predicate<Features> () {
 			public boolean apply(Features input) {
 				return Objects.equal(input.get(key), value);
 			}
 		};
 	}
 	
 	public static <T> Predicate<Features> has_(final Feature<?> key) {
 		return new Predicate<Features>() {
 			public boolean apply(Features input) {
 				return input.has(key);
 			}
 		};
 	}
 	
 	public static <T> Predicate<Features> is_(Feature<Void> key) {
		return has_(key, null);
 	}
 	
 	public static final Feature<String> TAG = Feature.named("TAG");
 	public static final Feature<String> LEMMA = Feature.named("LEMMA");
 	public static final Feature<WordClass> CLASS = Feature.named("CLASS");
 	public static final Feature<WordNumber> NUMBER = Feature.named("NUMBER");
 	public static final Feature<WordPerson> PERSON = Feature.named("PERSON");
 	public static final Feature<PhraseType> PHRASE_TYPE = Feature.named("PHRASE_TYPE");
 	public static final Feature<Information> INFORMATION = Feature.named("INFORMATION");
 	public static final Feature<Void> NEW = Feature.named("NEW");
 	public static final Feature<DiscourseMarker> MARKER_TYPE = Feature.named("MARKER_TYPE");
 	public static final Feature<DiscourseMarker> TOPIC_SHIFT = Feature.named("TOPIC_SHIFT");
 	
 	public static final Feature<Intervals> PHRASES = Feature.named("PHRASES");
 	public static final Feature<Intervals> CLAUSES = Feature.named("CLAUSES");
 	public static final Feature<Intervals> INFORMATION_STRUCTURE = Feature.named("INFORMATION_STRUCTURE");
 	public static final Feature<Intervals> DISCOURSE_MARKERS = Feature.named("DISCOURSE_MARKERS");
 	
 	public static final Feature<Void> TURN_START = Feature.named("TURN_START");
 	public static final Feature<Void> TURN_END = Feature.named("TURN_END");
 	
 	public static final Feature<AffectiveContent> AFFECT = Feature.named("AFFECT");
 	
 	public static final Feature<Void> FILTERED = Feature.named("FILTERED");
 	
 	public static final Feature<String> DIRECTION = Feature.named("DIRECTION");
 	public static final Feature<String> ACCENT = Feature.named("ACCENT");
 	public static final Feature<String> ENDTONE = Feature.named("ENDTONE");
 	public static final Feature<Long> RATE = Feature.named("RATE");
 	public static final Feature<Affect> EXPRESSION = Feature.named("EXPRESSION");
 	
 	public static final Feature<Integer> SESSION_INDEX = Feature.named("SESSION_INDEX");
 	public static final Feature<Void> LAST_SESSION = Feature.named("LAST_SESSION");
 	public static final Feature<Integer> OFFSET = Feature.named("OFFSET");
 	public static final Feature<Double> TIME_OFFSET = Feature.named("TIME_OFFSET");
 	public static final Feature<Double> ALLIANCE = Feature.named("ALLIANCE");
 	
 	// slow; primarily for console testing
 	public boolean clearByName(String name) {
 		for (Iterator<Feature<?>> it = features.keySet().iterator(); it.hasNext();) {
 			if (it.next().getName().equals(name)) {
 				it.remove();
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	// slow and ugly; primarily for console testing
 	public void setByName(String name, String value) {
 		try {
 			Field f = Features.class.getField(name);
 			if ( ! Modifier.isStatic(f.getModifiers()))
 				throw new IllegalArgumentException("invalid feature: " + name);
 			
 			Type t = f.getGenericType();
 			if ( ! (t instanceof ParameterizedType))
 				throw new IllegalArgumentException("invalid feature: " + name);
 			if (! ((ParameterizedType) t).getRawType().equals(Feature.class))
 				throw new IllegalArgumentException("invalid feature: " + name);
 			
 			Feature<?> feature = (Feature<?>) f.get(null);
 			Type vt = ((ParameterizedType) t).getActualTypeArguments()[0];
 			
 			if (Void.class.equals(vt))
 				features.put(feature, null);
 			else if (String.class.equals(vt))
 				features.put(feature, value);
 			else if (Integer.class.equals(vt))
 				features.put(feature, Integer.valueOf(value));
 			else if (Double.class.equals(vt))
 				features.put(feature, Double.valueOf(value));
 			else if (Long.class.equals(vt))
 				features.put(feature, Long.valueOf(value));
 			else if (AffectiveContent.class.equals(vt))
 				features.put(feature, AffectiveContent.valueOf(value));
 			else if ((vt instanceof Class) && ((Class<?>) vt).isEnum()) {
 				@SuppressWarnings({ "unchecked", "rawtypes" })
 				Object enumval = Enum.valueOf((Class<Enum>) vt, value);
 				features.put(feature, enumval);
 			} else
 				throw new IllegalArgumentException("cannot create feature from string: " + name);
 		} catch (SecurityException e) {
 			throw new IllegalArgumentException("invalid feature: " + name);
 		} catch (NoSuchFieldException e) {
 			throw new IllegalArgumentException("invalid feature: " + name);
 		} catch (IllegalAccessException e) {
 			throw new IllegalArgumentException("invalid feature: " + name);
 		} catch (NumberFormatException e) {
 			throw new IllegalArgumentException("invalid feature value: " + value + " for " + name);
 		}
 	}
 }
