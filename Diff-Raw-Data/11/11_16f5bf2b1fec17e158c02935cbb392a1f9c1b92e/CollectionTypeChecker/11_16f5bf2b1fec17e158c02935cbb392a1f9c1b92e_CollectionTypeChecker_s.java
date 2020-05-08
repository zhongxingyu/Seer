 package typecheckertest;
 
 import java.util.Collection;
 
 public class CollectionTypeChecker<T> extends TypeChecker<Collection<T>> {
 	private final TypeChecker<T> elementChecker;
 
 	protected CollectionTypeChecker(Class<Collection<T>> clazz, TypeChecker<T> elementChecker) {
 		super(clazz);
 
 		this.elementChecker = elementChecker;
 	}
 
 	@Override
 	public Collection<T> check(Object object) {
 		Collection<T> collection = super.check(object);
 
 		for (T element : collection) {
 			elementChecker.check(element);
 		}
 
 		return collection;
 	}
}
