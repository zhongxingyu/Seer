 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.genericsystem.annotation.Components;
 import org.genericsystem.annotation.Extends;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.value.AxedConstraintValue;
 import org.genericsystem.annotation.value.BooleanValue;
 import org.genericsystem.annotation.value.IntValue;
 import org.genericsystem.annotation.value.StringValue;
 import org.genericsystem.exception.ConcurrencyControlException;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.exception.ReferentialIntegrityConstraintViolationException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.genericsystem.map.AxedPropertyClass;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Nicolas Feybesse
  * 
  */
 public abstract class AbstractContext implements Serializable {
 
 	protected static Logger log = LoggerFactory.getLogger(AbstractContext.class);
 
 	private static final long serialVersionUID = -6036571074310729022L;
 
 	abstract <T extends Engine> T getEngine();
 
 	<T extends Generic> T plug(T generic) {
 		// TODO on gère pas les automatiques quand nous sommes dans la transaction
 		Set<Generic> componentSet = new HashSet<>();
 		for (Generic component : ((GenericImpl) generic).components)
 			if (componentSet.add(component))
 				getCompositeDependencies(component).add(generic);
 		Set<Generic> effectiveSupersSet = new HashSet<>();
 		for (Generic effectiveSuper : ((GenericImpl) generic).supers)
 			if (effectiveSupersSet.add(effectiveSuper))
 				getDirectInheritingsDependencies(effectiveSuper).add(generic);
 		return generic;
 	}
 
 	<T extends Generic> T unplug(T generic) {
 		Set<Generic> componentSet = new HashSet<>();
 		for (Generic component : ((GenericImpl) generic).components)
 			if (componentSet.add(component))
 				getCompositeDependencies(component).remove(generic);
 		Set<Generic> effectiveSupersSet = new HashSet<>();
 		for (Generic effectiveSuper : ((GenericImpl) generic).supers)
 			if (effectiveSupersSet.add(effectiveSuper))
 				getDirectInheritingsDependencies(effectiveSuper).remove(generic);
 		return generic;
 	}
 
 	abstract TimestampedDependencies getDirectInheritingsDependencies(Generic effectiveSuper);
 
 	abstract TimestampedDependencies getCompositeDependencies(Generic component);
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> Iterator<T> compositesIterator(Generic component) {
 		return (Iterator<T>) getCompositeDependencies(component).iterator(getTs());
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> Iterator<T> directInheritingsIterator(Generic component) {
 		return (Iterator<T>) getDirectInheritingsDependencies(component).iterator(getTs());
 	}
 
 	public abstract long getTs();
 
 	public <T extends Attribute> T getMetaAttribute() {
 		return getEngine().getMetaAttribute();
 	}
 
 	public <T extends Relation> T getMetaRelation() {
 		return getEngine().getMetaRelation();
 	}
 
 	public abstract boolean isScheduledToAdd(Generic generic);
 
 	public abstract boolean isScheduledToRemove(Generic generic);
 
 	public abstract boolean isAutomatic(Generic generic);
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> T reFind(Generic generic) {
 		if (generic.isEngine() || generic.isAlive())
 			return (T) generic;
 		return fastFindBySuper(((GenericImpl) generic).homeTreeNode, ((GenericImpl) generic).primaries, reFind(((GenericImpl) generic).supers[0]), reFind(((GenericImpl) generic).components));
 	}
 
 	private Generic[] reFind(Generic... generics) {
 		Generic[] reBind = new Generic[generics.length];
 		for (int i = 0; i < generics.length; i++)
 			reBind[i] = reFind(generics[i]);
 		return reBind;
 	}
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T fastFindBySuper(HomeTreeNode homeTreeNode, HomeTreeNode[] primaries, Generic superGeneric, Generic[] components) {
 		for (Generic generic : components.length == 0 || components[0] == null ? superGeneric.getInheritings() : components[0].getComposites())
 			if (((GenericImpl) generic).equiv(homeTreeNode, primaries, components))
 				return (T) generic;
 		return null;
 	}
 
 	// <T extends Generic> T fastFindPhantom(HomeTreeNode homeTreeNode, HomeTreeNode[] primaries, Generic[] components) {
 	// HomeTreeNode phantomHomeNode = homeTreeNode.metaNode.findInstanceNode(null);
 	// return phantomHomeNode != null ? this.<T> fastFindByComponents(phantomHomeNode, new Primaries(Statics.insertFirst(phantomHomeNode, primaries)).toArray(), components) : null;
 	// }
 
 	// @SuppressWarnings("unchecked")
 	// <T extends Generic> T fastFindByComponents(HomeTreeNode homeTreeNode, HomeTreeNode[] primaries, Generic[] components) {
 	// for (Generic generic : components[components.length - 1].getComposites())
 	// if (((GenericImpl) generic).equiv(homeTreeNode, primaries, components))
 	// return (T) generic;
 	// return null;
 	//
 	// }
 
 	<T extends Generic> NavigableSet<T> orderDependenciesForRemove(final Generic generic) throws ReferentialIntegrityConstraintViolationException {
 		return new TreeSet<T>() {
 			private static final long serialVersionUID = -6526972335865898198L;
 			{
 				addDependencies(generic);
 			}
 
 			@SuppressWarnings("unchecked")
 			public void addDependencies(Generic generic) throws ReferentialIntegrityConstraintViolationException {
 				if (super.add((T) generic)) {// protect from loop
 					for (T inheritingDependency : generic.<T> getInheritings())
 						// TODO clean
						if (/* ((GenericImpl) inheritingDependency).isPhantom() || */((GenericImpl) inheritingDependency).isAutomatic())
							addDependencies(inheritingDependency);
						else if (!contains(inheritingDependency))
 							throw new ReferentialIntegrityConstraintViolationException(inheritingDependency + " is an inheritance dependency for ancestor " + generic);
 					for (T compositeDependency : generic.<T> getComposites())
 						if (!generic.equals(compositeDependency)) {
 							for (int componentPos = 0; componentPos < ((GenericImpl) compositeDependency).components.length; componentPos++)
								if (!((GenericImpl) compositeDependency).isAutomatic() && ((GenericImpl) compositeDependency).components[componentPos].equals(generic) && !contains(compositeDependency)
 										&& compositeDependency.isReferentialIntegrity(componentPos))
 									throw new ReferentialIntegrityConstraintViolationException(compositeDependency + " is Referential Integrity for ancestor " + generic + " by component position : " + componentPos);
 							addDependencies(compositeDependency);
 						}
 					for (int axe = 0; axe < ((GenericImpl) generic).components.length; axe++)
 						if (((GenericImpl) generic).isCascadeRemove(axe))
 							addDependencies(((GenericImpl) generic).components[axe]);
 				}
 			}
 		};
 	}
 
 	<T extends Generic> NavigableSet<T> orderDependencies(final Generic generic) {
 		return new TreeSet<T>() {
 			private static final long serialVersionUID = 1053909994506452123L;
 			{
 				if (generic.isAlive())
 					addDependencies(generic);
 			}
 
 			@SuppressWarnings("unchecked")
 			public void addDependencies(Generic g) {
 				if (super.add((T) g)) {// protect from loop
 					for (T inheritingDependency : g.<T> getInheritings())
 						addDependencies(inheritingDependency);
 					for (T compositeDependency : g.<T> getComposites())
 						addDependencies(compositeDependency);
 				}
 			}
 		};
 	}
 
 	public abstract boolean isAlive(Generic generic);
 
 	public <T extends Generic> T find(Class<?> clazz) {
 		return this.<EngineImpl> getEngine().find(clazz);
 	}
 
 	Generic[] findUserSupers(Class<?> clazz) {
 		int i = 0;
 		LinkedHashSet<Class<?>> supersClasses = getSupersClasses(clazz);
 		Type[] supers = new Type[supersClasses.size()];
 		for (Class<?> superClasse : supersClasses)
 			supers[i++] = this.<Type> find(superClasse);
 		return supers;
 	}
 
 	LinkedHashSet<Class<?>> getSupersClasses(Class<?> clazz) {
 		Extends extendsAnnotation = clazz.getAnnotation(Extends.class);
 		LinkedHashSet<Class<?>> extendsClasses = new LinkedHashSet<>();
 		if (extendsAnnotation != null) {
 			extendsClasses.addAll(Arrays.asList(extendsAnnotation.value()));
 			return extendsClasses;
 		}
 		Class<?> javaSuperclass = clazz.getSuperclass();
 		if (Object.class.equals(javaSuperclass))
 			return extendsClasses;
 		extendsClasses.addAll(javaSuperclass.getAnnotation(SystemGeneric.class) == null ? getSupersClasses(javaSuperclass) : Arrays.asList(javaSuperclass));
 		return extendsClasses;
 	}
 
 	Generic[] findComponents(Class<?> clazz) {
 		Components componentsAnnotation = clazz.getAnnotation(Components.class);
 		if (componentsAnnotation == null)
 			return Statics.EMPTY_GENERIC_ARRAY;
 		Class<?>[] componentClasses = componentsAnnotation.value();
 		Generic[] components = new Generic[componentClasses.length];
 		for (int index = 0; index < componentClasses.length; index++)
 			components[index] = !clazz.equals(componentClasses[index]) ? find(componentClasses[index]) : null;
 		return components;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected static Serializable findImplictValue(Class<?> clazz) {
 		BooleanValue booleanValue = clazz.getAnnotation(BooleanValue.class);
 		if (booleanValue != null)
 			return booleanValue.value();
 		IntValue intValue = clazz.getAnnotation(IntValue.class);
 		if (intValue != null)
 			return intValue.value();
 		StringValue stringValue = clazz.getAnnotation(StringValue.class);
 		if (stringValue != null)
 			return stringValue.value();
 		AxedConstraintValue axedConstraintValue = clazz.getAnnotation(AxedConstraintValue.class);
 		if (axedConstraintValue != null)
 			return new AxedPropertyClass((Class<GenericImpl>) axedConstraintValue.value(), axedConstraintValue.axe());
 		return clazz;
 	}
 
 	// @SuppressWarnings("unchecked")
 	// public <T extends Generic> T findPrimaryByValue(Generic meta, Serializable value) {
 	// Iterator<Generic> it = directInheritingsIterator(meta);
 	// while (it.hasNext()) {
 	// Generic candidate = it.next();
 	// if (((GenericImpl) candidate).isPrimary() && (Objects.hashCode(value) == Objects.hashCode(candidate.getValue())) && Objects.equals(value, candidate.getValue()))
 	// return (T) candidate;
 	// }
 	// return null;
 	// }
 
 	void apply(Iterable<Generic> adds, Iterable<Generic> removes) throws ConcurrencyControlException, ConstraintViolationException {
 		removeAll(removes);
 		addAll(adds);
 	}
 
 	void addAll(Iterable<Generic> generics) {
 		for (Generic generic : generics)
 			simpleAdd(generic, false);
 	}
 
 	void removeAll(Iterable<Generic> generics) {
 		for (Generic generic : generics)
 			simpleRemove(generic);
 	}
 
 	void simpleAdd(Generic generic, boolean automatic) {
 		plug(generic);
 	}
 
 	void simpleRemove(Generic generic) {
 		unplug(generic);
 	}
 
 	abstract int getLevel();
 
 	interface TimestampedDependencies {
 
 		void add(Generic generic);
 
 		void remove(Generic generic);
 
 		Iterator<Generic> iterator(long ts);
 	}
 
 }
