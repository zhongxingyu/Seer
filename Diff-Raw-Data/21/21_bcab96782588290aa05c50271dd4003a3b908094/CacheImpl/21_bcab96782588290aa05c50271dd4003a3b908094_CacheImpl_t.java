 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.NavigableSet;
 import java.util.Objects;
 import java.util.Set;
 import java.util.TreeSet;
 import org.genericsystem.annotation.Dependencies;
 import org.genericsystem.annotation.InstanceGenericClass;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.core.Snapshot.Filter;
 import org.genericsystem.core.Statics.Primaries;
 import org.genericsystem.exception.AliveConstraintViolationException;
 import org.genericsystem.exception.ConcurrencyControlException;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.exception.ExistsException;
 import org.genericsystem.exception.FunctionalConsistencyViolationException;
 import org.genericsystem.exception.ReferentialIntegrityConstraintViolationException;
 import org.genericsystem.exception.RollbackException;
 import org.genericsystem.generic.Tree;
 import org.genericsystem.generic.Type;
 import org.genericsystem.iterator.AbstractAwareIterator;
 import org.genericsystem.iterator.AbstractFilterIterator;
 import org.genericsystem.iterator.AbstractPreTreeIterator;
 import org.genericsystem.snapshot.AbstractSnapshot;
 import org.genericsystem.snapshot.PseudoConcurrentSnapshot;
 import org.genericsystem.systemproperties.constraints.Constraint.CheckingType;
 import org.genericsystem.tree.TreeImpl;
 
 /**
  * @author Nicolas Feybesse
  * @author Michael Ory
  */
 public class CacheImpl extends AbstractContext implements Cache {
 	private static final long serialVersionUID = 6124326077696104707L;
 
 	private AbstractContext subContext;
 
 	private InternalCache internalCache;
 
 	private transient Map<Generic, TimestampedDependencies> compositeDependenciesMap;
 
 	private transient Map<Generic, TimestampedDependencies> inheritingDependenciesMap;
 
 	public CacheImpl(Context subContext) {
 		this.subContext = (AbstractContext) subContext;
 		clear();
 	}
 
 	@Override
 	public void clear() {
 		compositeDependenciesMap = new HashMap<Generic, TimestampedDependencies>();
 		inheritingDependenciesMap = new HashMap<Generic, TimestampedDependencies>();
 		internalCache = new InternalCache();
 	}
 
 	<T extends Generic> T insert(Generic generic) throws RollbackException {
 		try {
 			return this.<T> internalInsert(generic);
 		} catch (ConstraintViolationException e) {
 			rollback(e);
 		}
 		throw new IllegalStateException();// Unreachable;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends Generic> T internalInsert(Generic generic) throws ConstraintViolationException {
 		getInternalContext().addGeneric(generic);
 		return (T) generic;
 	}
 
 	// TODO implements specializedGeneric !!!
 	<T extends Generic> T bindPrimaryByValue(/* Class<?> specializeGeneric, */Generic primaryAncestor, Serializable value, int metaLevel, boolean automatic) {
 		T implicit = findPrimaryByValue(primaryAncestor, value, metaLevel);
 		return implicit != null ? implicit : this.<T> insert(((GenericImpl) getEngine().getFactory().newGeneric(null)).initializePrimary(value, metaLevel, new Generic[] { primaryAncestor }, Statics.EMPTY_GENERIC_ARRAY, automatic));
 	}
 
 	@Override
 	TimestampedDependencies getDirectInheritingsDependencies(Generic directSuper) {
 		TimestampedDependencies dependencies = inheritingDependenciesMap.get(directSuper);
 		if (dependencies == null) {
 			TimestampedDependencies result = inheritingDependenciesMap.put(directSuper, dependencies = new CacheDependencies(subContext.getDirectInheritingsDependencies(directSuper)));
 			assert result == null;
 		}
 		return dependencies;
 	}
 
 	@Override
 	TimestampedDependencies getCompositeDependencies(Generic component) {
 		TimestampedDependencies dependencies = compositeDependenciesMap.get(component);
 		if (dependencies == null) {
 			TimestampedDependencies result = compositeDependenciesMap.put(component, dependencies = new CacheDependencies(subContext.getCompositeDependencies(component)));
 			assert result == null;
 		}
 		return dependencies;
 	}
 
 	public void pickNewTs() throws RollbackException {
 		if (subContext instanceof Cache)
 			((CacheImpl) subContext).pickNewTs();
 		else {
 			long ts = getTs();
 			subContext = new Transaction(getEngine());
 			assert getTs() > ts;
 		}
 	}
 
 	@Override
 	public boolean isRemovable(Generic generic) {
 		try {
 			orderRemoves(generic);
 		} catch (ReferentialIntegrityConstraintViolationException e) {
 			return false;
 		}
 		return true;
 	}
 
 	void remove(Generic generic) throws RollbackException {
 		try {
 			internalRemove(generic);
 		} catch (ConstraintViolationException e) {
 			rollback(e);
 		}
 	}
 
 	private void internalRemove(Generic node) throws ConstraintViolationException {
 		if (!isAlive(node))
 			throw new AliveConstraintViolationException(node + " is not alive");
 		for (Generic generic : orderRemoves(node).descendingSet()) {
 			internalCache.removeGeneric(generic);
 			for (int axe = 0; axe < ((GenericImpl) generic).components.length; axe++)
 				if (((GenericImpl) generic).isCascadeRemove(this, axe))
 					internalRemove(((GenericImpl) generic).components[axe]);
 		}
 	}
 
 	abstract class Restructurator {
 		@SuppressWarnings("unchecked")
 		<T extends Generic> T rebuildAll(Generic old) {
 			NavigableSet<Generic> dependencies = orderAndRemoveDependencies(old);
 			ConnectionMap map = new ConnectionMap();
 			map.put(old, rebuild());
 			dependencies.remove(old);
 			return (T) map.reBind(dependencies).get(old);
 		}
 
 		abstract Generic rebuild();
 	}
 
 	<T extends Generic> T addComponent(final Generic old, final Generic newComponent, final int pos) {
 		return new Restructurator() {
 			@Override
 			Generic rebuild() {
				if (((GenericImpl) old).isPrimary()) {
					Generic newPrimary = bindPrimaryByValue(old.<GenericImpl> getImplicit().supers[0], old.getValue(), old.getMetaLevel(), true);
					return bind(newPrimary, Statics.replace(0, ((GenericImpl) old).supers, newPrimary), Statics.insertIntoArray(newComponent, ((GenericImpl) old).selfToNullComponents(), pos), old.isAutomatic(), old.getClass(), true);
				}
 				return bind(old.getImplicit(), ((GenericImpl) old).supers, Statics.insertIntoArray(newComponent, ((GenericImpl) old).selfToNullComponents(), pos), old.isAutomatic(), old.getClass(), true);
 			}
 		}.rebuildAll(old);
 	}
 
 	<T extends Generic> T removeComponent(final Generic old, final int pos) {
 		return new Restructurator() {
 			@Override
 			Generic rebuild() {
 				return bind(old.getImplicit(), ((GenericImpl) old).supers, Statics.truncate(pos, ((GenericImpl) old).selfToNullComponents()), old.isAutomatic(), old.getClass(), true);
 			}
 		}.rebuildAll(old);
 	}
 
 	<T extends Generic> T addSuper(final Generic old, final Generic newSuper) {
 		return new Restructurator() {
 			@Override
 			Generic rebuild() {
 				return bind(old.getImplicit(), Statics.insertLastIntoArray(newSuper, ((GenericImpl) old).supers), ((GenericImpl) old).selfToNullComponents(), old.isAutomatic(), old.getClass(), true);
 			}
 		}.rebuildAll(old);
 	}
 
 	<T extends Generic> T removeSuper(final Generic old, final int pos) {
 		if (pos == 0)
 			throw new UnsupportedOperationException();
 		return new Restructurator() {
 			@Override
 			Generic rebuild() {
 				return bind(old.getImplicit(), Statics.truncate(pos, ((GenericImpl) old).supers), ((GenericImpl) old).selfToNullComponents(), old.isAutomatic(), old.getClass(), true);
 			}
 		}.rebuildAll(old);
 	}
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T updateKey(final Generic old, final Serializable value) {
 		if (Objects.equals(value, old.getValue()))
 			return (T) old;
 		return new Restructurator() {
 			@Override
 			Generic rebuild() {
 				Generic newImplicit = bindPrimaryByValue(old.<GenericImpl> getImplicit().supers[0], value, old.getMetaLevel(), old.isAutomatic());
 				if (((GenericImpl) old).isPrimary())
 					return newImplicit;
 				return bind(newImplicit, Statics.replace(0, ((GenericImpl) old).supers, newImplicit), ((GenericImpl) old).selfToNullComponents(), old.isAutomatic(), old.getClass(), true);
 			}
 		}.rebuildAll(old);
 	}
 
 	// @SuppressWarnings("unchecked")
 	// public <T extends Generic> T update(Generic old, Serializable value) {
 	// if (Objects.equals(value, old.getValue()))
 	// return (T) old;
 	// NavigableSet<Generic> dependencies = orderAndRemoveDependencies(old);
 	// ConnectionMap map = new ConnectionMap();
 	// map.put(old, buildWithNewValue(old, bindPrimaryByValue(old.<GenericImpl> getImplicit().supers[0], value, old.getMetaLevel(), old.isAutomatic())));
 	// dependencies.remove(old);
 	// return (T) map.reBind(dependencies).get(old);
 	// }
 
 	// private Generic buildWithNewValue(Generic old, Generic newImplicit) {
 	// if (((GenericImpl) old).isPrimary())
 	// return newImplicit;
 	// return bind(newImplicit, new Primaries(Statics.replace(0, ((GenericImpl) old).supers, newImplicit)).toArray(), ((GenericImpl) old).selfToNullComponents(), old.isAutomatic(), old.getClass(), true);
 	// }
 
 	@Override
 	public void flush() throws RollbackException {
 		Exception cause = null;
 		for (int attempt = 0; attempt < Statics.ATTEMPTS; attempt++)
 			try {
 				internalCache.checkConstraints();
 				internalCache.flush();
 				clear();
 				return;
 			} catch (ConcurrencyControlException e) {
 				try {
 					Thread.sleep(Statics.ATTEMPT_SLEEP);
 				} catch (InterruptedException ex) {
 					throw new IllegalStateException(ex);
 				}
 				if (attempt > Statics.ATTEMPTS / 2)
 					log.info("MvccException : " + e + " attempt : " + attempt);
 				cause = e;
 				pickNewTs();
 				continue;
 			} catch (Exception e) {
 				rollback(e);
 			}
 		rollback(cause);
 	}
 
 	protected void rollback(Exception e) throws RollbackException {
 		clear();
 		throw new RollbackException(e);
 	}
 
 	@Override
 	public boolean isAlive(Generic generic) {
 		return internalCache.isAlive(generic);
 	}
 
 	@Override
 	public long getTs() {
 		return subContext.getTs();
 	}
 
 	@Override
 	public <T extends Engine> T getEngine() {
 		return subContext.getEngine();
 	}
 
 	public AbstractContext getSubContext() {
 		return subContext;
 	}
 
 	@Override
 	InternalCache getInternalContext() {
 		return internalCache;
 	}
 
 	@Override
 	public boolean isScheduledToRemove(Generic generic) {
 		return getInternalContext().isScheduledToRemove(generic) || subContext.isScheduledToRemove(generic);
 	}
 
 	@Override
 	public boolean isScheduledToAdd(Generic generic) {
 		return getInternalContext().isScheduledToAdd(generic) || subContext.isScheduledToAdd(generic);
 	}
 
 	@Override
 	public <T extends Type> T newType(Serializable value) {
 		return this.<T> newSubType(value);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends Generic> Iterator<T> allInheritingsIterator(final Context context) {
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(context.getEngine()) {
 
 			private static final long serialVersionUID = 8161663636838488529L;
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return (((GenericImpl) node).directInheritingsIterator(context));
 			}
 		};
 	}
 
 	@Override
 	public <T extends Type> Snapshot<T> getTypes() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return CacheImpl.this.<T> allInheritingsIterator(CacheImpl.this);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Type> T getType(final Serializable value) {
 		return this.<T> getTypes().filter(new Filter<T>() {
 			@Override
 			public boolean isSelected(T element) {
 				return Objects.equals(element.getValue(), value);
 			}
 
 		}).first();
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Type... superTypes) {
 		return newSubType(value, superTypes, Statics.EMPTY_GENERIC_ARRAY);
 	}
 
 	@Override
 	// TODO clean
 	public <T extends Type> T newSubType(Serializable value, Type[] superTypes, Generic... components) {
 		T result = bind(bindPrimaryByValue(getEngine(), value, SystemGeneric.STRUCTURAL, superTypes.length > 0), superTypes, components, false, null, false);
 		assert Objects.equals(value, result.getValue());
 		// if (((GenericImpl) result).isPrimary())
 		// assert Objects.equals(value, result.getSupers().first().getImplicit().getValue()) : result.getSupers();
 		return result;
 	}
 
 	@Override
 	public <T extends Tree> T newTree(Serializable value) {
 		return newTree(value, 1);
 	}
 
 	@Override
 	public <T extends Tree> T newTree(Serializable value, int dim) {
 		return this.<T> bind(bindPrimaryByValue(getEngine(), value, SystemGeneric.STRUCTURAL, true), Statics.EMPTY_GENERIC_ARRAY, new Generic[dim], false, TreeImpl.class, false).<T> disableInheritance(this);
 	}
 
 	@Override
 	public Cache newSuperCache() {
 		return this.<EngineImpl> getEngine().getFactory().newCache(this);
 	}
 
 	<T extends Generic> NavigableSet<T> orderAndRemoveDependencies(final T old) {
 		NavigableSet<T> orderedGenerics = orderDependencies(old);
 		for (T generic : orderedGenerics.descendingSet())
 			remove(generic);
 		return orderedGenerics;
 	}
 
 	// TODO KK findImplicitSuper
 	<T extends Generic> T bind(Class<?> clazz) {
 		// assert supers[0].getImplicit().equals(findImplicitSuper(clazz)) : "" + supers[0].getImplicit() + " / " + findImplicitSuper(clazz);
 		return bind(bindPrimaryByValue(findImplicitSuper(clazz), findImplictValue(clazz), findMetaLevel(clazz), true), findSupers(clazz), findComponents(clazz), false, clazz, false);
 	}
 
 	<T extends Generic> T bind(Generic implicit, boolean automatic, Generic directSuper, boolean existsException, Generic... components) {
 		Class<?> clazz = null;
 		if (implicit.isConcrete()) {
 			components = ((GenericImpl) directSuper).sortAndCheck(components);
 			Generic meta = directSuper.getMetaLevel() == implicit.getMetaLevel() ? directSuper.getMeta() : directSuper;
 			InstanceGenericClass instanceClass = meta.getClass().getAnnotation(InstanceGenericClass.class);
 			if (instanceClass != null)
 				clazz = instanceClass.value();
 		}
 		return bind(implicit, new Generic[] { directSuper }, components, automatic, clazz, existsException);
 	}
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T bind(Generic implicit, Generic[] supers, Generic[] components, boolean automatic, Class<?> clazz, boolean existsException) {
 
 		final Primaries primaries = new Primaries(supers);
 		primaries.add(implicit);
 		Generic[] interfaces = primaries.toArray();
 		if (implicit.getValue() != null) {
 			Generic phantomImplicit = findPrimaryByValue(((GenericImpl) ((GenericImpl) implicit).supers[0]), null, implicit.getMetaLevel());
 			if (phantomImplicit != null) {
 				primaries.add(phantomImplicit);
 				T phantom = fastFindByInterfaces(phantomImplicit, primaries.toArray(), components);
 				if (phantom != null) {
 					phantom.remove(this);
 					return (T) ((GenericImpl) phantom).supers[1];
 				}
 			}
 		}
 		T result = fastFindByInterfaces(implicit, interfaces, components);
 		if (result != null) {
 			if (!Objects.equals(result.getValue(), implicit.getValue()))
 				rollback(new FunctionalConsistencyViolationException(result.info()));
 			if (existsException)
 				rollback(new ExistsException(result + " already exists !"));
 			return result;
 		}
 		return internalBind(implicit, interfaces, components, automatic, clazz);
 	}
 
 	private <T extends Generic> T internalBind(Generic implicit, Generic[] interfaces, Generic[] components, boolean automatic, Class<?> clazz) {
 		Generic[] directSupers = getDirectSupers(interfaces, components);
 		// assert directSupers.length >= 2;
 		NavigableSet<Generic> orderedDependencies = new TreeSet<Generic>();
 		for (Generic directSuper : directSupers) {
 			Iterator<Generic> removeIterator = concernedDependenciesIterator(directSuper, interfaces, components);
 			while (removeIterator.hasNext())
 				orderedDependencies.addAll(orderDependencies(removeIterator.next()));
 		}
 		for (Generic generic : orderedDependencies.descendingSet())
 			remove(generic);
 
 		Generic newGeneric = ((GenericImpl) this.<EngineImpl> getEngine().getFactory().newGeneric(clazz)).initializeComplex(implicit, directSupers, components, automatic);
 		T superGeneric = this.<T> insert(newGeneric);
 		new ConnectionMap().reBuild(orderedDependencies);
 		return superGeneric;
 	}
 
 	private Iterator<Generic> concernedDependenciesIterator(Generic directSuper, final Generic[] interfaces, final Generic[] extendedComponents) {
 		return new AbstractFilterIterator<Generic>(directInheritingsIterator(directSuper)) {
 			@Override
 			public boolean isSelected() {
 				return next.getValue() != null && GenericImpl.isSuperOf(interfaces, extendedComponents, ((GenericImpl) next).getPrimariesArray(), ((GenericImpl) next).components);
 			}
 		};
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Generic> T reBind(final Generic generic) {
 		return (T) new ConnectionMap().reBind(orderAndRemoveDependencies(generic)).get(generic);
 	}
 
 	private <T extends Generic> T buildAndInsertComplex(Class<?> clazz, Generic implicit, Generic[] supers, Generic[] components, boolean automatic) {
 		return insert(this.<EngineImpl> getEngine().buildComplex(clazz, implicit, supers, components, automatic));
 	}
 
 	private class ConnectionMap extends HashMap<Generic, Generic> {
 		private static final long serialVersionUID = 8257917150315417734L;
 
 		private ConnectionMap reBind(NavigableSet<Generic> orderedDependencies) {
 			for (Generic orderedDependency : orderedDependencies) {
 				Generic generic;
 				if (((GenericImpl) orderedDependency).isPrimary())
 					generic = bindPrimaryByValue(adjust(((GenericImpl) orderedDependency).supers)[0], orderedDependency.getValue(), orderedDependency.getMetaLevel(), orderedDependency.isAutomatic());
 				else
 					generic = buildAndInsertComplex(orderedDependency.getClass(), adjust(orderedDependency.getImplicit())[0], adjust(((GenericImpl) orderedDependency).supers), adjust(((GenericImpl) orderedDependency).components),
 							orderedDependency.isAutomatic());
 				put(orderedDependency, generic);
 			}
 			return this;
 		}
 
 		private void reBuild(NavigableSet<Generic> orderedDependencies) {
 			for (Generic orderedDependency : orderedDependencies) {
 				Generic[] newComponents = adjust(((GenericImpl) orderedDependency).components);
 				put(orderedDependency,
 						buildAndInsertComplex(orderedDependency.getClass(), orderedDependency.getImplicit(),
 								((GenericImpl) orderedDependency).isPrimary() ? adjust(((GenericImpl) orderedDependency).supers[0]) : getDirectSupers(adjust(((GenericImpl) orderedDependency).getPrimariesArray()), newComponents), newComponents,
 								orderedDependency.isAutomatic()));
 			}
 		}
 
 		private Generic[] adjust(Generic... oldComponents) {
 			Generic[] newComponents = new Generic[oldComponents.length];
 			for (int i = 0; i < newComponents.length; i++) {
 				Generic newComponent = get(oldComponents[i]);
 				assert newComponent == null ? isAlive(oldComponents[i]) : !isAlive(oldComponents[i]) : newComponent + " / " + oldComponents[i].info();
 				newComponents[i] = newComponent == null ? oldComponents[i] : newComponent;
 				assert isAlive(newComponents[i]);
 			}
 			return newComponents;
 		}
 	}
 
 	protected void triggersDependencies(Class<?> clazz) {
 		Dependencies dependenciesClass = clazz.getAnnotation(Dependencies.class);
 		if (dependenciesClass != null)
 			for (Class<?> dependencyClass : dependenciesClass.value())
 				find(dependencyClass);
 	}
 
 	public class InternalCache extends InternalContext<CacheImpl> {
 
 		private static final long serialVersionUID = 21372907392620336L;
 
 		private final Set<Generic> adds = new LinkedHashSet<Generic>();
 		private final Set<Generic> removes = new LinkedHashSet<Generic>();
 
 		public void flush() throws ConstraintViolationException, ConcurrencyControlException {
 			getSubContext().getInternalContext().apply(new Iterable<Generic>() {
 				@Override
 				public Iterator<Generic> iterator() {
 					return new AbstractFilterIterator<Generic>(adds.iterator()) {
 						@Override
 						public boolean isSelected() {
 							return isFlushable(next);
 						}
 					};
 				}
 			}, removes);
 		}
 
 		@Override
 		protected void add(GenericImpl generic) {
 			adds.add(generic);
 			super.add(generic);
 		}
 
 		@Override
 		protected void remove(GenericImpl generic) {
 			removes.add(generic);
 			super.remove(generic);
 		}
 
 		@Override
 		protected void cancelAdd(GenericImpl generic) {
 			boolean result = adds.remove(generic);
 			assert result == true;
 			super.cancelAdd(generic);
 		}
 
 		@Override
 		protected void cancelRemove(GenericImpl generic) {
 			boolean result = removes.remove(generic);
 			assert result == true;
 			super.cancelRemove(generic);
 		}
 
 		public void addGeneric(Generic generic) throws ConstraintViolationException {
 			add((GenericImpl) generic);
 			checkConsistency(CheckingType.CHECK_ON_ADD_NODE, true, Arrays.asList(generic));
 			checkConstraints(CheckingType.CHECK_ON_ADD_NODE, true, Arrays.asList(generic));
 		}
 
 		public void removeGeneric(Generic generic) throws ConstraintViolationException {
 			removeOrCancelAdd(generic);
 			checkConsistency(CheckingType.CHECK_ON_REMOVE_NODE, true, Arrays.asList(generic));
 			checkConstraints(CheckingType.CHECK_ON_REMOVE_NODE, true, Arrays.asList(generic));
 		}
 
 		public void removeGenericWithoutCheck(Generic generic) throws ConstraintViolationException {
 			removeOrCancelAdd(generic);
 		}
 
 		public void removeOrCancelAdd(Generic generic) throws ConstraintViolationException {
 			if (adds.contains(generic))
 				cancelAdd((GenericImpl) generic);
 			else
 				remove((GenericImpl) generic);
 		}
 
 		public boolean isAlive(Generic generic) {
 			return adds.contains(generic) || (!removes.contains(generic) && getSubContext().isAlive(generic));
 		}
 
 		public boolean isScheduledToRemove(Generic generic) {
 			return removes.contains(generic);
 		}
 
 		public boolean isScheduledToAdd(Generic generic) {
 			return adds.contains(generic);
 		}
 
 		public void checkConstraints() throws ConstraintViolationException {
 			checkConstraints(adds, removes);
 		}
 	}
 
 	static class CacheDependencies implements TimestampedDependencies {
 
 		private transient TimestampedDependencies underlyingDependencies;
 
 		private PseudoConcurrentSnapshot inserts = new PseudoConcurrentSnapshot();
 		private PseudoConcurrentSnapshot deletes = new PseudoConcurrentSnapshot();
 
 		public CacheDependencies(TimestampedDependencies underlyingDependencies) {
 			assert underlyingDependencies != null;
 			this.underlyingDependencies = underlyingDependencies;
 		}
 
 		@Override
 		public void add(Generic generic) {
 			inserts.add(generic);
 		}
 
 		@Override
 		public void remove(Generic generic) {
 			if (!inserts.remove(generic))
 				deletes.add(generic);
 		}
 
 		@Override
 		public Iterator<Generic> iterator(long ts) {
 			return new InternalIterator(underlyingDependencies.iterator(ts));
 		}
 
 		private class InternalIterator extends AbstractAwareIterator<Generic> implements Iterator<Generic> {
 			private Iterator<Generic> underlyingIterator;
 			private Iterator<Generic> insertsIterator = inserts.iterator();
 
 			private InternalIterator(Iterator<Generic> underlyingIterator) {
 				this.underlyingIterator = underlyingIterator;
 			}
 
 			@Override
 			protected void advance() {
 				while (underlyingIterator.hasNext()) {
 					Generic generic = underlyingIterator.next();
 					if (!deletes.contains(generic)) {
 						next = generic;
 						return;
 					}
 				}
 				while (insertsIterator.hasNext()) {
 					next = insertsIterator.next();
 					return;
 				}
 				next = null;
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		}
 	}
 
 }
