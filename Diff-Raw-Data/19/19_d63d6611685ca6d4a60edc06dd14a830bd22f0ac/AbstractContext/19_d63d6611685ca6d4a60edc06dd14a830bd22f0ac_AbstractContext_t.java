 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.NavigableSet;
 import java.util.Objects;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.genericsystem.annotation.Components;
 import org.genericsystem.annotation.Extends;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.value.BooleanValue;
 import org.genericsystem.annotation.value.IntValue;
 import org.genericsystem.annotation.value.StringValue;
 import org.genericsystem.core.Statics.Primaries;
 import org.genericsystem.exception.ConcurrencyControlException;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.exception.ReferentialIntegrityConstraintViolationException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.genericsystem.iterator.AbstractSelectableLeafIterator;
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
 
 	<T extends GenericImpl> T plug(T generic) {
 		Set<Generic> componentSet = new HashSet<>();
 		for (Generic component : generic.components)
 			if (componentSet.add(component))
 				getCompositeDependencies(component).add(generic);
 		Set<Generic> effectiveSupersSet = new HashSet<>();
 		for (Generic effectiveSuper : generic.supers)
 			if (effectiveSupersSet.add(effectiveSuper))
 				getDirectInheritingsDependencies(effectiveSuper).add(generic);
 		return generic;
 	}
 
 	<T extends GenericImpl> T unplug(T generic) {
 		Set<Generic> componentSet = new HashSet<>();
 		for (Generic component : generic.components)
 			if (componentSet.add(component))
 				getCompositeDependencies(component).remove(generic);
 		Set<Generic> effectiveSupersSet = new HashSet<>();
 		for (Generic effectiveSuper : generic.supers)
 			if (effectiveSupersSet.add(effectiveSuper))
 				getDirectInheritingsDependencies(effectiveSuper).remove(generic);
 		return generic;
 	}
 
 	abstract TimestampedDependencies getDirectInheritingsDependencies(Generic effectiveSuper);
 
 	abstract TimestampedDependencies getCompositeDependencies(Generic component);
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> Iterator<T> compositesIterator(final Generic component) {
 		return (Iterator<T>) getCompositeDependencies(component).iterator(getTs());
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> Iterator<T> directInheritingsIterator(final Generic component) {
 		return (Iterator<T>) getDirectInheritingsDependencies(component).iterator(getTs());
 	}
 
 	public abstract long getTs();
 
 	public <T extends Attribute> T getMetaAttribute() {
 		return getEngine().getMetaAttribute();
 	}
 
 	public <T extends Relation> T getMetaRelation() {
 		return getEngine().getMetaRelation();
 	}
 
 	public boolean isFlushable(Generic generic) {
 		if (!generic.isAutomatic())
 			return true;
 		for (Generic inheriting : generic.getInheritings())
 			if (isFlushable(inheriting))
 				return true;
 		for (Generic composite : generic.getComposites())
 			if (isFlushable((composite)))
 				return true;
 		return false;
 	}
 
 	public abstract boolean isScheduledToAdd(Generic generic);
 
 	public abstract boolean isScheduledToRemove(Generic generic);
 
 	Iterator<Generic> getDirectSupersIterator(final Generic[] interfaces, final Generic[] components) {
 		return new AbstractSelectableLeafIterator(getEngine()) {
 
 			@Override
 			protected boolean isSelectable() {
 				return true;
 			}
 
 			@Override
 			public boolean isSelected(Generic candidate) {
 				return GenericImpl.isSuperOf(((GenericImpl) candidate).getPrimariesArray(), ((GenericImpl) candidate).components, interfaces, components);
 			}
 		};
 	}
 
 	// TODO clean
 	protected Generic[] getDirectSupers(final Generic[] interfaces, final Generic[] components) {
 		List<Generic> list = new ArrayList<Generic>();
 		final Iterator<Generic> iterator = getDirectSupersIterator(interfaces, components);
 		while (iterator.hasNext())
 			list.add(iterator.next());
 		return list.toArray(new Generic[list.size()]);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> T reFind(Generic generic) {
 		if (generic.isEngine() || generic.isAlive())
 			return (T) generic;
 		if (((GenericImpl) generic).isPrimary())
 			return findPrimaryByValue(reFind(((GenericImpl) generic).supers[0]), generic.getValue(), generic.getMetaLevel());
 		return fastFindByInterfaces(reFind(generic.getImplicit()), new Primaries(reFind(((GenericImpl) generic).getPrimariesArray())).toArray(), reFind(((GenericImpl) generic).components));
 	}
 
 	private Generic[] reFind(Generic[] generics) {
 		Generic[] reBind = new Generic[generics.length];
 		for (int i = 0; i < generics.length; i++)
 			reBind[i] = reFind(generics[i]);
 		return reBind;
 	}
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T find(Generic[] supers, Generic[] components) {
 		final Generic[] interfaces = new Primaries(supers).toArray();
 		Generic[] directSupers = getDirectSupers(interfaces, components);
 		if (directSupers.length == 1 && ((GenericImpl) directSupers[0]).equiv(interfaces, components))
 			return (T) directSupers[0];
 		return null;
 	}
 
 	// <T extends Generic> T fastFind(Generic implicit, Generic[] supers, Generic[] components) {
 	// assert supers[0].getImplicit().equals(implicit);
 	// return fastFindByInterfaces(implicit, new Primaries(supers).toArray(), components);
 	// }
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T fastFindByInterfaces(Generic implicit, Generic[] interfaces, Generic[] components) {
 		if (components.length == 0 && interfaces.length == 1) {
 			assert implicit.equals(interfaces[0]);
 			return (T) implicit;
 		}
 		for (Generic generic : components.length == 0 || components[0] == null ? implicit.getInheritings() : components[0].getComposites())
 			if (((GenericImpl) generic).equiv(interfaces, components))
 				return (T) generic;
 		return null;
 	}
 
 	<T extends Generic> NavigableSet<T> orderDependencies(final Generic generic) {
 		return new TreeSet<T>() {
 			private static final long serialVersionUID = 1053909994506452123L;
 			{
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
 
 	<T extends Generic> NavigableSet<T> orderRemoves(final Generic generic) throws ReferentialIntegrityConstraintViolationException {
 		return new TreeSet<T>() {
 			private static final long serialVersionUID = -6526972335865898198L;
 			{
 				addDependencies(generic);
 			}
 
 			@SuppressWarnings("unchecked")
 			public void addDependencies(Generic generic) throws ReferentialIntegrityConstraintViolationException {
 				if (super.add((T) generic)) {// protect from loop
 					for (T inheritingDependency : generic.<T> getInheritings())
 						if (inheritingDependency.getValue() == null)
 							addDependencies(inheritingDependency);
						else if (!contains(inheritingDependency))
 							throw new ReferentialIntegrityConstraintViolationException(inheritingDependency + " is an inheritance dependency for ancestor " + generic);
 					for (T compositeDependency : generic.<T> getComposites())
 						if (!generic.equals(compositeDependency)) {
 							for (int componentPos = 0; componentPos < ((GenericImpl) compositeDependency).components.length; componentPos++)
								if (((GenericImpl) compositeDependency).components[componentPos].equals(generic) && !contains(compositeDependency) && compositeDependency.isReferentialIntegrity(componentPos))
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
 
 	public abstract boolean isAlive(Generic generic);
 
 	public <T extends Generic> T find(Class<?> clazz) {
 		return this.<EngineImpl> getEngine().find(clazz);
 	}
 
 	<T extends Generic> T findMeta(Generic[] interfaces, Generic[] components) {
 		for (T composite : getEngine().<T> getComposites())
 			if (composite.isMeta() && Arrays.equals(interfaces, ((GenericImpl) composite).getPrimariesArray()) && Arrays.equals(components, ((GenericImpl) composite).components))
 				return composite;
 		return null;
 	}
 
 	Generic[] findSupers(Class<?> clazz) {
 		int i = 0;
 		LinkedHashSet<Class<?>> supersClasses = getSupersClasses(clazz);
 		Type[] supers = new Type[supersClasses.size()];
 		for (Class<?> superClasse : supersClasses)
 			supers[i++] = this.<Type> find(superClasse);
 		return supers;
 	}
 
 	int findMetaLevel(Class<?> clazz) {
 		return clazz.getAnnotation(SystemGeneric.class).value();
 	}
 
 	LinkedHashSet<Class<?>> getSupersClasses(Class<?> clazz) {
 		Extends extendsAnnotation = clazz.getAnnotation(Extends.class);
 		LinkedHashSet<Class<?>> extendsClasses = new LinkedHashSet<>();
 		if (extendsAnnotation != null) {
 			extendsClasses.add(extendsAnnotation.value());
 			extendsClasses.addAll(Arrays.asList(extendsAnnotation.others()));
 			return extendsClasses;
 		}
 		extendsClasses = new LinkedHashSet<>(Arrays.asList(new Class<?>[] {}));
 		Class<?> javaSuperclass = clazz.getSuperclass();
 		if (Object.class.equals(javaSuperclass)) {
 			extendsClasses.add(Engine.class);
 			return extendsClasses;
 		}
 		if (javaSuperclass.getAnnotation(SystemGeneric.class) == null)
 			extendsClasses.addAll(getSupersClasses(javaSuperclass));
 		else
 			extendsClasses.add(javaSuperclass);
 		return extendsClasses;
 	}
 
 	// TODO KK
 	Generic findImplicitSuper(Class<?> clazz) {
 		if (SystemGeneric.STRUCTURAL == clazz.getAnnotation(SystemGeneric.class).value())
 			return getEngine();
 		Generic[] supers = findSupers(clazz);
 		assert supers.length == 1;
 		return supers[0].getImplicit();
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
 		return clazz;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> T findPrimaryByValue(Generic primaryAncestor, Serializable value, int metaLevel) {
 		assert metaLevel - primaryAncestor.getMetaLevel() <= 1;
 		assert metaLevel - primaryAncestor.getMetaLevel() >= 0 : metaLevel + " " + primaryAncestor.getMetaLevel();
 		Iterator<Generic> it = directInheritingsIterator(primaryAncestor);
 		while (it.hasNext()) {
 			Generic candidate = it.next();
 			if (((GenericImpl) candidate).isPrimary() && (metaLevel == candidate.getMetaLevel()) && (Objects.hashCode(value) == Objects.hashCode(candidate.getValue())) && Objects.equals(value, candidate.getValue()))
 				return (T) candidate;
 		}
 		return null;
 	}
 
 	void apply(Iterable<Generic> adds, Iterable<Generic> removes) throws ConcurrencyControlException, ConstraintViolationException {
 		removeAll(removes);
 		addAll(adds);
 	}
 
 	void addAll(Iterable<Generic> generics) {
 		for (Generic generic : generics)
 			simpleAdd((GenericImpl) generic);
 	}
 
 	void removeAll(Iterable<Generic> generics) {
 		for (Generic generic : generics)
 			simpleRemove((GenericImpl) generic);
 	}
 
 	void simpleAdd(GenericImpl generic) {
 		plug(generic);
 	}
 
 	void simpleRemove(GenericImpl generic) {
 		unplug(generic);
 	}
 
 	interface TimestampedDependencies {
 
 		void add(Generic generic);
 
 		void remove(Generic generic);
 
 		Iterator<Generic> iterator(long ts);
 	}
 
 }
