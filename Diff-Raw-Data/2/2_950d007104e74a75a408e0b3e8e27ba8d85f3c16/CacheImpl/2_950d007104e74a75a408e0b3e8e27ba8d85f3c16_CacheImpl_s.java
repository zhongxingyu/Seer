 package org.genericsystem.impl.core;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeSet;
 import org.genericsystem.api.annotation.Dependencies;
 import org.genericsystem.api.annotation.SystemGeneric;
 import org.genericsystem.api.core.Cache;
 import org.genericsystem.api.core.Context;
 import org.genericsystem.api.core.Engine;
 import org.genericsystem.api.core.Generic;
 import org.genericsystem.api.exception.AliveConstraintViolationException;
 import org.genericsystem.api.exception.ConcurrencyControlException;
 import org.genericsystem.api.exception.ConstraintViolationException;
 import org.genericsystem.api.exception.ReferentialIntegrityConstraintViolationException;
 import org.genericsystem.api.exception.RollbackException;
 import org.genericsystem.api.generic.Tree;
 import org.genericsystem.api.generic.Type;
 import org.genericsystem.impl.constraints.Constraint.CheckingType;
 import org.genericsystem.impl.core.Statics.Primaries;
 import org.genericsystem.impl.iterator.AbstractAwareIterator;
 import org.genericsystem.impl.iterator.AbstractFilterIterator;
 import org.genericsystem.impl.snapshot.PseudoConcurrentSnapshot;
 import org.genericsystem.impl.system.CascadeRemoveSystemProperty;
 
 /**
  * @author Nicolas Feybesse
  * 
  */
 public class CacheImpl extends AbstractContext implements Cache {
 	private static final long serialVersionUID = 6124326077696104707L;
 	
 	// private void readObject(ObjectInputStream in) throws IOException,
 	// ClassNotFoundException {
 	// in.defaultReadObject();
 	// this.reconstructDependencyMap();
 	// }
 	//
 	// private void reconstructDependencyMap() {
 	// compositeDependenciesMap = new HashMap<Generic,
 	// TimestampedDependencies>();
 	// inheritingDependenciesMap = new HashMap<Generic,
 	// TimestampedDependencies>();
 	// for (Generic generic : internalCache.adds)
 	// plug((GenericImpl) generic);
 	// for (Generic generic : internalCache.removes)
 	// unplug((GenericImpl) generic);
 	// }
 	
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
 	
 	<T extends Generic> T bindPrimaryByValue(Generic primaryAncestor, Serializable value, int metaLevel) {
 		T implicit = findPrimaryByValue(primaryAncestor, value, metaLevel);
 		return implicit != null ? implicit : this.<T> insert(new GenericImpl().initPrimary(value, metaLevel, primaryAncestor));
 	}
 	
 	@Override
 	TimestampedDependencies getDirectInheritingsDependencies(Generic effectiveSuper) {
 		TimestampedDependencies dependencies = inheritingDependenciesMap.get(effectiveSuper);
 		if (dependencies == null) {
 			TimestampedDependencies result = inheritingDependenciesMap.put(effectiveSuper, dependencies = new CacheDependencies(subContext.getDirectInheritingsDependencies(effectiveSuper)));
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
 	
 	private void checkIsAlive(Generic generic) throws ConstraintViolationException {
 		if (!isAlive(generic))
 			throw new AliveConstraintViolationException(generic + " is not alive");
 	}
 	
 	void remove(Generic generic) throws RollbackException {
 		try {
 			checkIsAlive(generic);
 			List<Generic> componentsForCascadeRemove = getComponentsForCascadeRemove(generic);
 			internalRemove(generic);
 			for (Generic component : componentsForCascadeRemove)
 				internalRemove(component);
 		} catch (ConstraintViolationException e) {
 			rollback(e);
 		}
 	}
 	
 	private List<Generic> getComponentsForCascadeRemove(Generic generic) throws ConstraintViolationException {
 		Generic[] components = ((GenericImpl) generic).components;
 		List<Generic> componentsForCascadeRemove = new ArrayList<>();
 		for (int axe = 0; axe < components.length; axe++)
 			if (((GenericImpl) generic).isSystemPropertyEnabled(this, CascadeRemoveSystemProperty.class, axe))
 				componentsForCascadeRemove.add(components[axe]);
 		return componentsForCascadeRemove;
 	}
 	
 	private void internalRemove(Generic node) throws ConstraintViolationException {
 		// assert !node.getValue().equals("Power");
 		checkIsAlive(node);
 		removeDependencies(node);
 		if (isAlive(node))
 			internalCache.removeGeneric(node);
 	}
 	
 	private void removeDependencies(final Generic node) throws ConstraintViolationException {
 		Iterator<Generic> inheritingsDependeciesIterator = getDirectInheritingsDependencies(node).iterator(getTs());
 		while (inheritingsDependeciesIterator.hasNext()) {
 			Generic inheritingDependency = inheritingsDependeciesIterator.next();
 			if (isAlive(inheritingDependency))
 				throw new ReferentialIntegrityConstraintViolationException(inheritingDependency + " is an inheritance dependency for ancestor " + node);
 		}
 		Iterator<Generic> compositeDependenciesIterator = getCompositeDependencies(node).iterator(getTs());
 		while (compositeDependenciesIterator.hasNext()) {
 			Generic compositeDependency = compositeDependenciesIterator.next();
 			if (!node.equals(compositeDependency)) {
 				Generic[] compositionComponents = ((GenericImpl) compositeDependency).components;
 				for (int componentPos = 0; componentPos < compositionComponents.length; componentPos++)
 					if (compositionComponents[componentPos].equals(node) && compositeDependency.isReferentialIntegrity(this, componentPos))
 						throw new ReferentialIntegrityConstraintViolationException(compositeDependency + " is Referential Integrity for ancestor " + node + " by component position : " + componentPos);
 				internalRemove(compositeDependency);
 			}
 		}
 	}
 	
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
 	
 	<T extends Generic> T update(Generic old, Serializable value) {
 		return reInsert(orderAndRemoveDependencies(old).iterator(), ((GenericImpl) old).getImplicit(), bindPrimaryByValue(old.<GenericImpl> getImplicit().directSupers[0], value, old.getMetaLevel()));
 	}
 	
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T reInsert(Iterator<Generic> genericsToInsert, Generic oldPrimary, Generic newPrimary) {
 		Generic updated = replace(genericsToInsert.next(), oldPrimary, (GenericImpl) newPrimary);
 		while (genericsToInsert.hasNext())
 			replace(genericsToInsert.next(), oldPrimary, (GenericImpl) newPrimary);
 		return (T) updated;
 	}
 	
 	private Generic replace(Generic genericToReplace, Generic oldPrimary, GenericImpl newPrimary) {
 		if (((GenericImpl) genericToReplace).isPrimary())
 			return bindPrimaryByValue(((GenericImpl) genericToReplace).directSupers[0], genericToReplace.getValue(), genericToReplace.getMetaLevel());
 		
 		Generic[] interfaces = ((GenericImpl) genericToReplace).getPrimariesArray();
 		Generic[] components = ((GenericImpl) genericToReplace).components;
 		Generic[] resultInterfaces = new Generic[interfaces.length];
 		Generic[] resultComponents = new Generic[components.length];
 		for (int i = 0; i < interfaces.length; i++)
 			resultInterfaces[i] = ((GenericImpl) interfaces[i]).isPrimary() ? getNewPrimary(interfaces[i], oldPrimary, newPrimary) : replace(interfaces[i], oldPrimary, newPrimary);
 		for (int i = 0; i < components.length; i++)
 			if (genericToReplace.equals(components[i]))
 				resultComponents[i] = null;
 			else
 				resultComponents[i] = ((GenericImpl) components[i]).isPrimary() ? getNewPrimary(components[i], oldPrimary, newPrimary) : replace(components[i], oldPrimary, newPrimary);
 		return bind(newPrimary.getValue(), newPrimary.getMetaLevel(), resultInterfaces, resultComponents);
 	}
 	
 	private Generic getNewPrimary(Generic oldSubPrimary, Generic oldPrimary, Generic newPrimary) {
 		if (!(oldSubPrimary.inheritsFrom(oldPrimary)))
 			return oldSubPrimary;
 		if (oldSubPrimary.equals(oldPrimary))
 			return newPrimary;
 		return bindPrimaryByValue(getNewPrimary(((GenericImpl) oldSubPrimary).directSupers[0], oldPrimary, newPrimary), oldSubPrimary.getValue(), oldSubPrimary.getMetaLevel());
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
 	
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Type... superTypes) {
 		return newSubType(value, superTypes, Statics.EMPTY_GENERIC_ARRAY);
 	}
 	
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Type[] superTypes, Generic... components) {
 		if (superTypes.length == 0)
 			superTypes = new Type[] { getEngine() };
 		if (superTypes.length == 1)
 			return add(superTypes[0], value, SystemGeneric.STRUCTURAL, Statics.EMPTY_GENERIC_ARRAY, components);
 		return add(getEngine(), value, SystemGeneric.STRUCTURAL, superTypes, components);
 	}
 	
 	@Override
 	public <T extends Tree> T newTree(Serializable value) {
 		return newTree(value, 1);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Tree> T newTree(Serializable value, int dim) {
 		return (T) this.<T> add(getEngine(), value, SystemGeneric.STRUCTURAL, Statics.EMPTY_GENERIC_ARRAY, new Generic[dim]).disableInheritance(this);
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
 	
 	<T extends Generic> T bind(Class<?> clazz) {
 		Generic[] annotedInterfaces = findAnnotedInterfaces(clazz);
 		// TODO clean getSuperToCheck(annotedInterfaces)
 		return add(getSuperToCheck(annotedInterfaces), getImplictValue(clazz), clazz.getAnnotation(SystemGeneric.class).value(), annotedInterfaces, findComponents(clazz));
 	}
 	
	<T extends Generic> T add(Generic genericToCheck, Serializable value, int metaLevel, Generic[] additionalInterfaces, Generic[] components) {
 		Generic implicit = bindPrimaryByValue(genericToCheck.getImplicit(), value, metaLevel);
 		Primaries primaries = new Primaries(additionalInterfaces);
 		primaries.add(implicit);
 		primaries.add(genericToCheck);
 		Generic[] interfaces = primaries.toArray();
 		((GenericImpl) genericToCheck).checkSuperRule(interfaces, components);
 		return bind(value, metaLevel, interfaces, components);
 	}
 	
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T bind(Serializable value, int metaLevel, final Generic[] interfaces, final Generic[] components) {
 		Generic[] directSupers = getDirectSupers(interfaces, components);
 		if (directSupers.length == 1 && ((GenericImpl) directSupers[0]).equiv(interfaces, components))
 			return (T) directSupers[0];
 		TreeSet<Generic> orderedDependencies = new TreeSet<Generic>();
 		for (Generic superGeneric : directSupers) {
 			Iterator<Generic> removeIterator = new AbstractFilterIterator<Generic>(directInheritingsIterator(superGeneric)) {
 				@Override
 				public boolean isSelected() {
 					return GenericImpl.isSuperOf(interfaces, components, ((GenericImpl) next).getPrimariesArray(), ((GenericImpl) next).components);
 				}
 			};
 			while (removeIterator.hasNext())
 				orderedDependencies.addAll(orderDependencies(removeIterator.next()));
 		}
 		for (Generic generic : orderedDependencies.descendingSet())
 			remove(generic);
 		
 		Generic newGeneric = ((GenericImpl) this.<EngineImpl> getEngine().getFactory().newGeneric()).initialize(value, metaLevel, directSupers, components);
 		T superGeneric = this.<T> insert(newGeneric);
 		
 		Map<Generic, Generic> connectionMap = new HashMap<>();
 		for (Generic orderedDependency : orderedDependencies) {
 			Generic[] newComponents = adjustComponent(((GenericImpl) orderedDependency).components, connectionMap);
 			Generic bind = insert(((GenericImpl) this.<EngineImpl> getEngine().getFactory().newGeneric()).initialize(((GenericImpl) orderedDependency).value, ((GenericImpl) orderedDependency).metaLevel,
 					getDirectSupers(((GenericImpl) orderedDependency).getPrimariesArray(), newComponents), newComponents));
 			connectionMap.put(orderedDependency, bind);
 		}
 		assert superGeneric == find(directSupers, components);
 		return superGeneric;
 	}
 	
 	private Generic[] adjustComponent(Generic[] oldComponents, Map<Generic, Generic> connectionMap) {
 		Generic[] newComponents = new Generic[oldComponents.length];
 		for (int i = 0; i < newComponents.length; i++)
 			newComponents[i] = connectionMap.get(oldComponents[i]) == null ? oldComponents[i] : connectionMap.get(oldComponents[i]);
 		return newComponents;
 	}
 	
 	protected void triggersDependencies(Class<?> clazz) {
 		Dependencies dependenciesClass = clazz.getAnnotation(Dependencies.class);
 		if (dependenciesClass != null)
 			for (Class<?> dependencyClass : dependenciesClass.value())
 				find(dependencyClass);
 	}
 	
 	public class InternalCache extends InternalContext<CacheImpl> {
 		
 		private static final long serialVersionUID = 21372907392620336L;
 		
 		protected final Set<Generic> adds = new LinkedHashSet<Generic>();
 		protected final Set<Generic> removes = new LinkedHashSet<Generic>();
 		
 		public void flush() throws ConstraintViolationException, ConcurrencyControlException {
 			getSubContext().getInternalContext().apply(adds, removes);
 		}
 		
 		@Override
 		protected void add(GenericImpl generic) {
 			adds.add(generic);
 			super.add(generic);
 		}
 		
 		@Override
 		protected void remove(GenericImpl generic) {
 			boolean result = removes.add(generic);
 			assert result == true;
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
 		
 		public void addGenericWithoutCheck(Generic generic) throws ConstraintViolationException {
 			add((GenericImpl) generic);
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
