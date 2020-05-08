 package org.jgum.category.type;
 
 import static org.jgum.JGum.DEFAULT_BOTTOM_UP_TYPE_LINEARIZATION_FUNCTION;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.jgum.category.Category;
 import org.jgum.category.LabeledCategory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 /**
  * A category wrapping a class or interface.
  * @author sergioc
  *
  * @param <T> the type of the wrapped class.
  */
 public abstract class TypeCategory<T> extends LabeledCategory<Class<T>> {
 
 	private static final Priority DEFAULT_BOTTOM_UP_PRIORITY = Priority.CLASSES_FIRST;
 	private static final InterfaceOrder DEFAULT_INTERFACE_ORDER = InterfaceOrder.DECLARATION;
 	private static final Priority DEFAULT_TOP_DOWN_PRIORITY = Priority.INTERFACES_FIRST;
 	
 	
 	private List<InterfaceCategory<? super T>> superInterfaceNodes;
 	
 	protected TypeCategory(TypeCategorization typeCategorization, Class<T> wrappedClazz) {
 		super(typeCategorization, wrappedClazz);
 		setSuperInterfaceNodes(Collections.<InterfaceCategory<? super T>>emptyList());
 	}
 	
 	protected TypeCategory(Class<T> wrappedClazz, List<InterfaceCategory<? super T>> superInterfaceNodes) {
		super(wrappedClazz, null);
 		setSuperInterfaceNodes(superInterfaceNodes);
 	}
 
 	public TypeCategorization getTypeCategorization() {
 		return (TypeCategorization)super.getCategorization();
 	}
 	
 	public List<InterfaceCategory<? super T>> getSuperInterfaceNodes() {
 		return new ArrayList<>((superInterfaceNodes));
 	}
 	
 	protected void setSuperInterfaceNodes(List<InterfaceCategory<? super T>> superInterfaceNodes) {
 		this.superInterfaceNodes = superInterfaceNodes;
 	}
 	
 	/**
 	 * 
 	 * @return the ancestor classes according to the default bottom-up linearization function.
 	 */
 	public List<ClassCategory<? super T>> getAncestorClasses() {
 		List<TypeCategory<?>> ancestors = linearize((Function)DEFAULT_BOTTOM_UP_TYPE_LINEARIZATION_FUNCTION);
 		ancestors = ancestors.subList(1, ancestors.size());
 		return new ArrayList(Collections2.filter(ancestors, new Predicate<TypeCategory<?>>() {
 			@Override
 			public boolean apply(TypeCategory<?> typeCategory) {
 				return typeCategory instanceof ClassCategory;
 			}
 		}));
 	}
 	
 	/**
 	 * 
 	 * @return the ancestor interfaces according to the default bottom-up linearization function.
 	 */
 	public List<InterfaceCategory<? super T>> getAncestorInterfaces() {
 		List<TypeCategory<?>> ancestors = linearize((Function)DEFAULT_BOTTOM_UP_TYPE_LINEARIZATION_FUNCTION);
 		ancestors = ancestors.subList(1, ancestors.size());
 		return new ArrayList(Collections2.filter(ancestors, new Predicate<TypeCategory<?>>() {
 			@Override
 			public boolean apply(TypeCategory<?> typeCategory) {
 				return typeCategory instanceof InterfaceCategory;
 			}
 		}));
 	}
 	
 	/**
 	 * 
 	 * @see Category#getParents()
 	 * @return the parents of this type category (super class and super interfaces) . A specific ordering should not be assumed.
 	 */
 	@Override
 	public <U extends Category> List<U> getParents() {
 		return (List)getParents(DEFAULT_BOTTOM_UP_PRIORITY, DEFAULT_INTERFACE_ORDER);
 	}
 
 	/**
 	 * @see Category#getChildren()
 	 * @return the children of this type category (implementing/extending classes and interfaces) . A specific ordering should not be assumed.
 	 */
 	@Override
 	public <U extends Category> List<U> getChildren() {
 		return (List)getChildren(DEFAULT_TOP_DOWN_PRIORITY);
 	}
 	
 	/**
 	 * 
 	 * @param priority if classes should be visited before interfaces or vice versa.
 	 * @param interfaceOrder if the interfaces should be traversed following their declaration order or reversing such order.
 	 * @return the parents of this category according to a given priority and desired interface order.
 	 */
 	protected abstract <U extends TypeCategory<? super T>> List<U> getParents(Priority priority, InterfaceOrder interfaceOrder);
 
 	/**
 	 * 
 	 * @param priority if classes should be visited before interfaces or vice versa.
 	 * @return the children of this category according to a given priority.
 	 */
 	protected abstract <U extends TypeCategory<? extends T>> List<U> getChildren(Priority priority);
 
 }
