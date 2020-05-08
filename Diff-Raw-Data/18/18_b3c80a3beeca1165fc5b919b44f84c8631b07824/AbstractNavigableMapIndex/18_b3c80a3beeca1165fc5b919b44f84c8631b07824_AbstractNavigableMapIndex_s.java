 package cz.muni.fi.publishsubscribe.countingtree.index.operator;
 
 import cz.muni.fi.publishsubscribe.countingtree.Constraint;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 
 public abstract class AbstractNavigableMapIndex<T_ValueType extends Comparable<T_ValueType>>
 		implements OperatorIndex<T_ValueType> {
 
 	protected NavigableMap<Comparable<?>, Constraint> constraints;
 
 	protected AbstractNavigableMapIndex() {
 		this(null);
 	}
 
 	protected AbstractNavigableMapIndex(Comparator<Comparable<?>> comparator) {
 		this.constraints = new TreeMap<>(comparator);
 	}
 
 	public boolean addConstraint(Constraint constraint) {
 		Comparable<?> value = constraint.getAttributeValue().getValue();
 
 		if (this.constraints.containsKey(value)) {
 			return false;
 		}
 
 		this.constraints.put(value, constraint);
 
 		return true;
 	}
 
 	@Override
 	public boolean removeConstraint(Constraint constraint) {
		throw new RuntimeException("not implemented yet");
 	}
 
 	public abstract List<Constraint> getConstraints(Comparable<?> attributeValue);
 }
