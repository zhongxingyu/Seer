 package cz.muni.fi.publishsubscribe.matchingtree;
 
 public class AttributeValue<T1 extends Comparable<?>> {
 
 	private final T1 value;
 	private final Class<T1> type;
 
 
 	public AttributeValue(T1 value, Class<T1> type) {
 		this.value = value;
 		this.type = type;
 	}
 
 	public T1 getValue() {
 		return value;
 	}
 
 	public Class<T1> getType() {
 		return type;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
		AttributeValue<?> that = (AttributeValue<?>) o;
 
 		if (!type.equals(that.type)) return false;
 		if (value != null ? !value.equals(that.value) : that.value != null) return false;
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int result = type.hashCode();
 		result = 31 * result + (value != null ? value.hashCode() : 0);
 		return result;
 	}
 }
