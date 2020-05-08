 package typecheckertest;
 
 import java.util.Map;
 
 public class MapTypeChecker<K, V> extends TypeChecker<Map<K, V>> {
 	private final TypeChecker<K> keyChecker;
 	private final TypeChecker<V> valueChecker;
 
 	protected MapTypeChecker(Class<Map<K, V>> clazz, TypeChecker<K> keyChecker, TypeChecker<V> valueChecker) {
 		super(clazz);
 
 		this.keyChecker = keyChecker;
 		this.valueChecker = valueChecker;
 	}
 
 	@Override
 	public Map<K, V> check(Object object) {
 		Map<K, V> collection = super.check(object);
 
 		for (Map.Entry<K, V> element : collection.entrySet()) {
 			keyChecker.check(element.getKey());
 			valueChecker.check(element.getValue());
 		}
 
 		return collection;
 	}
}
