 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import org.genericsystem.annotation.Dependencies;
 import org.genericsystem.annotation.Extends;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.constraints.AbstractConstraintImpl;
 import org.genericsystem.constraints.AbstractConstraintImpl.AbstractAxedConstraintImpl;
 import org.genericsystem.constraints.AbstractConstraintImpl.CheckingType;
 import org.genericsystem.constraints.VirtualConstraintImpl;
 import org.genericsystem.core.Statics.Primaries;
 import org.genericsystem.exception.AliveConstraintViolationException;
 import org.genericsystem.exception.ConcurrencyControlException;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.exception.ExistsException;
 import org.genericsystem.exception.FunctionalConsistencyViolationException;
 import org.genericsystem.exception.NotRemovableException;
 import org.genericsystem.exception.ReferentialIntegrityConstraintViolationException;
 import org.genericsystem.exception.RollbackException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Tree;
 import org.genericsystem.generic.Type;
 import org.genericsystem.iterator.AbstractAwareIterator;
 import org.genericsystem.iterator.AbstractConcateIterator.ConcateIterator;
 import org.genericsystem.iterator.AbstractFilterIterator;
 import org.genericsystem.iterator.AbstractPreTreeIterator;
 import org.genericsystem.iterator.AbstractSelectableLeafIterator;
 import org.genericsystem.map.AbstractMapProvider.AbstractExtendedMap;
 import org.genericsystem.map.AxedPropertyClass;
 import org.genericsystem.map.ConstraintsMapProvider.ConstraintValue;
 import org.genericsystem.snapshot.PseudoConcurrentSnapshot;
 import org.genericsystem.systemproperties.MetaAttribute;
 import org.genericsystem.systemproperties.MetaRelation;
 import org.genericsystem.systemproperties.NoInheritanceSystemType;
 import org.genericsystem.tree.TreeImpl;
 
 /**
  * @author Nicolas Feybesse
  * @author Michael Ory
  */
 public class CacheImpl extends AbstractContext implements Cache {
 	private static final long serialVersionUID = 6124326077696104707L;
 
 	private AbstractContext subContext;
 
 	private transient Map<Generic, TimestampedDependencies> compositeDependenciesMap;
 	private transient Map<Generic, TimestampedDependencies> inheritingDependenciesMap;
 
 	private Set<Generic> adds;
 	private Set<Generic> removes;
 	private Set<Generic> automatics;
 
 	public CacheImpl(Cache cache) {
 		subContext = (CacheImpl) cache;
 		clear();
 	}
 
 	public CacheImpl(Engine engine) {
 		subContext = new Transaction(engine);
 		clear();
 	}
 
 	@Override
 	public void clear() {
 		compositeDependenciesMap = new HashMap<>();
 		inheritingDependenciesMap = new HashMap<>();
 		adds = new LinkedHashSet<>();
 		removes = new LinkedHashSet<>();
 		automatics = new HashSet<>();
 	}
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T insert(Generic generic) throws RollbackException {
 		try {
 			addGeneric(generic);
 			return (T) generic;
 		} catch (ConstraintViolationException e) {
 			rollback(e);
 		}
 		throw new IllegalStateException();// Unreachable;
 	}
 
 	@Override
 	public Cache start() {
 		return this.<EngineImpl> getEngine().start(this);
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
 
 	public void refresh() throws ConstraintViolationException {
 		pickNewTs();
 		try {
 			checkConstraints();
 		} catch (ConstraintViolationException e) {
 			rollback(e);
 			throw e; // re-throw the same exception
 		}
 	}
 
 	@Override
 	public boolean isRemovable(Generic generic) {
 		try {
 			orderDependenciesForRemove(generic);
 		} catch (ReferentialIntegrityConstraintViolationException e) {
 			return false;
 		}
 		return true;
 	}
 
 	void remove(final Generic generic, final RemoveStrategy removeStrategy) throws RollbackException {
 		if (generic.getClass().isAnnotationPresent(SystemGeneric.class))
 			rollback(new NotRemovableException("Cannot remove " + generic + " because it is System Generic annotated"));
 		new UnsafeCacheManager<Object>() {
 			@Override
 			Object internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException {
 				unsafeCache.unsafeRemove(generic, removeStrategy);
 				return null;
 			}
 		}.doWork();
 
 	}
 
 	private abstract class UnsafeCacheManager<T> {
 		private final UnsafeCache unsafeCache = new UnsafeCache(CacheImpl.this);
 
 		T doWork() throws RollbackException {
 			try {
 				unsafeCache.start();
 				T result = internalWork(unsafeCache);
 				unsafeCache.flush();
 				return result;
 			} catch (ConstraintViolationException ex) {
 				rollback(ex);
 				return null;
 			} catch (RollbackException ex) {
 				rollback(ex.getCause());
 				return null;
 			} finally {
 				start();
 			}
 		}
 
 		abstract T internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException;
 	}
 
 	<T extends Generic> T setValue(final Generic old, final Serializable value) {
 		return new UnsafeCacheManager<T>() {
 			@Override
 			T internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException {
 				return unsafeCache.unsafeSetValue(old, value);
 			}
 		}.doWork();
 	}
 
 	<T extends Generic> T addComponent(final Generic old, final Generic newComponent, final int pos) {
 		return new UnsafeCacheManager<T>() {
 			@Override
 			T internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException {
 				return unsafeCache.unsafeAddComponent(old, newComponent, pos);
 			}
 		}.doWork();
 	}
 
 	<T extends Generic> T removeComponent(final Generic old, final int pos) {
 		return new UnsafeCacheManager<T>() {
 			@Override
 			T internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException {
 				return unsafeCache.unsafeRemoveComponent(old, pos);
 			}
 		}.doWork();
 	}
 
 	<T extends Generic> T addSuper(final Generic old, final Generic newSuper) {
 		return new UnsafeCacheManager<T>() {
 			@Override
 			T internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException {
 				return unsafeCache.unsafeAddSuper(old, newSuper);
 			}
 		}.doWork();
 	}
 
 	<T extends Generic> T removeSuper(final Generic old, final int pos) {
 		return new UnsafeCacheManager<T>() {
 			@Override
 			T internalWork(UnsafeCache unsafeCache) throws ConstraintViolationException {
 				return unsafeCache.unsafeRemoveSuper(old, pos);
 			}
 		}.doWork();
 	}
 
 	private class ConnectionMap extends HashMap<Generic, Generic> {
 		private static final long serialVersionUID = 8257917150315417734L;
 
 		private ConnectionMap reBind(HomeTreeNode homeTreeNode, Generic bind, Set<Generic> orderedDependencies, boolean isProperty, boolean isSingular, int basePos) {
 			for (Generic dependency : orderedDependencies) {
 				HomeTreeNode newHomeTreeNode = ((GenericImpl) dependency).getHomeTreeNode();
 				HomeTreeNode[] primaries = ((GenericImpl) dependency).primaries;
 				Generic[] components = adjust(((GenericImpl) dependency).selfToNullComponents());
 				if (Statics.MULTIDIRECTIONAL != basePos) {
 					if (((GenericImpl) bind).getComponent(basePos).equals(((Holder) dependency).getComponent(basePos)))
 						continue;// Nothing to do
 					if (isSingular || (isProperty && Arrays.equals(Statics.truncate(basePos, components), Statics.truncate(basePos, ((GenericImpl) bind).selfToNullComponents())))) {
 						if (!((GenericImpl) dependency).components[basePos].inheritsFrom(((GenericImpl) bind).components[basePos])) {
 							newHomeTreeNode = homeTreeNode;
 							primaries = Statics.replace(((GenericImpl) dependency).primaries, ((GenericImpl) dependency).getHomeTreeNode(), newHomeTreeNode);
 						}
 					}
 				}
 				put(dependency, internalRebind(newHomeTreeNode, dependency, primaries, components, isProperty, isSingular, basePos));
 			}
 			return this;
 		}
 
 		private Generic internalRebind(HomeTreeNode homeTreeNode, Generic dependency, HomeTreeNode[] primaries, Generic[] components, boolean isProperty, boolean isSingular, int basePos) {
 			NavigableSet<Generic> directSupers = getExtendedDirectSupers(dependency.getMeta(), isProperty, isSingular, basePos, primaries, components);
 			for (Generic directSuper : directSupers) {
 				primaries = new Primaries(directSuper, primaries).toArray();
 				components = GenericImpl.enrich(components, ((GenericImpl) directSuper).components);
 			}
 			return buildAndInsertComplex(homeTreeNode, dependency.getClass(), directSupers.toArray(new Generic[directSupers.size()]), components);
 		}
 
 		private ConnectionMap reBind(Set<Generic> orderedDependencies) {
 			for (Generic orderedDependency : orderedDependencies) {
 				orderedDependency.log();
 				Generic build = buildAndInsertComplex(((GenericImpl) orderedDependency).getHomeTreeNode(), orderedDependency.getClass(), adjust(((GenericImpl) orderedDependency).supers), adjust(((GenericImpl) orderedDependency).components));
 				put(orderedDependency, build);
 			}
 			return this;
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
 
 	public <T extends Generic> T reBind(Generic generic) {
 		return setValue(generic, generic.getValue());
 	}
 
 	@Override
 	public boolean isAutomatic(Generic generic) {
 		return automatics.contains(generic) || subContext.isAutomatic(generic);
 	};
 
 	public boolean isFlushable(Generic generic) {
 		if (!isAutomatic(generic))
 			return true;
 		for (Generic inheriting : generic.getInheritings())
 			if (isFlushable(inheriting))
 				return true;
 		for (Generic composite : generic.getComposites())
 			if (isFlushable(composite))
 				return true;
 		return false;
 	};
 
 	public void markAsAutomatic(Generic generic) {
 		automatics.add(generic);
 	}
 
 	@Override
 	public void flush() throws RollbackException {
 		assert equals(getEngine().getCurrentCache());
 		Exception cause = null;
 		for (int attempt = 0; attempt < Statics.ATTEMPTS; attempt++)
 			try {
 				checkConstraints();
 				// adds.removeAll(automatics);
 				getSubContext().apply(new Iterable<Generic>() {
 
 					@Override
 					public Iterator<Generic> iterator() {
 						return new AbstractFilterIterator<Generic>(adds.iterator()) {
 
 							@Override
 							public boolean isSelected() {
 								return ((GenericImpl) next).isFlushable();
 							}
 
 						};
 					}
 
 				}, removes);
 
 				clear();
 				return;
 			} catch (ConcurrencyControlException e) {
 				try {
 					Thread.sleep(Statics.ATTEMPT_SLEEP);
 				} catch (InterruptedException ex) {
 					throw new IllegalStateException(ex);
 				}
 
 				// if (attempt > Statics.ATTEMPTS / 2)
 				// log.info("MvccException : " + e + " attempt : " + attempt);
 
 				pickNewTs();
 				continue;
 			} catch (Exception e) {
 				rollback(e);
 			}
 		rollback(cause);
 	}
 
 	protected void rollback(Throwable e) throws RollbackException {
 		clear();
 		throw new RollbackException(e);
 	}
 
 	@Override
 	public boolean isAlive(Generic generic) {
 		return adds.contains(generic) || (!removes.contains(generic) && getSubContext().isAlive(generic));
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
 	public boolean isScheduledToRemove(Generic generic) {
 		return removes.contains(generic) || subContext.isScheduledToRemove(generic);
 	}
 
 	@Override
 	public boolean isScheduledToAdd(Generic generic) {
 		return adds.contains(generic) || subContext.isScheduledToAdd(generic);
 	}
 
 	@Override
 	public <T extends Type> T newType(Serializable value) {
 		return this.<T> newSubType(value);
 	}
 
 	@Override
 	public <T extends Type> T getType(final Serializable value) {
 		return getEngine().getSubType(value);
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Type... userSupers) {
 		return newSubType(value, userSupers, Statics.EMPTY_GENERIC_ARRAY);
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Type[] userSupers, Generic... components) {
 		return bind(getEngine(), value, userSupers, components, null, false, Statics.MULTIDIRECTIONAL);
 	}
 
 	@Override
 	public <T extends Tree> T newTree(Serializable value) {
 		return newTree(value, 1);
 	}
 
 	@Override
 	public <T extends Tree> T newTree(Serializable value, int dim) {
 		return this.<T> bind(getEngine(), value, new Generic[] { find(NoInheritanceSystemType.class) }, new Generic[dim], TreeImpl.class, false, Statics.MULTIDIRECTIONAL);
 	}
 
 	@Override
 	public Cache mountNewCache() {
 		return new CacheImpl(this).start();
 	}
 
 	@Override
 	public Cache flushAndUnmount() {
 		flush();
 		return subContext instanceof Cache ? ((Cache) subContext).start() : this;
 	}
 
 	@Override
 	public Cache discardAndUnmount() {
 		clear();
 		return subContext instanceof Cache ? ((Cache) subContext).start() : this;
 	}
 
 	<T extends Generic> T bind(Class<?> clazz) {
 		Generic[] userSupers = findUserSupers(clazz);
 		Generic[] components = findComponents(clazz);
 		GenericImpl meta = getMeta(clazz, components);
 		Serializable value = findImplictValue(clazz);
 		return this.<T> bind(meta, value, Statics.insertFirst(meta, userSupers), components, clazz, false, Statics.MULTIDIRECTIONAL);
 	}
 
 	<T extends Generic> T bind(Generic meta, Serializable value, Class<?> specializationClass, Generic directSuper, boolean existsException, int axe, Generic... components) {
 		Generic[] sortAndCheck = ((GenericImpl) directSuper).sortAndCheck(components);
 		return bind(meta, value, new Generic[] { directSuper }, sortAndCheck, specializationClass, existsException, Statics.MULTIDIRECTIONAL != axe ? findAxe(sortAndCheck, components[axe]) : axe);
 	}
 
 	int findAxe(Generic[] sorts, Generic baseComponent) throws RollbackException {
 		for (int i = 0; i < sorts.length; i++)
 			if (baseComponent.equals(sorts[i]))
 				return i;
 		rollback(new IllegalStateException());
 		return -1;// Unreachable
 	}
 
 	private GenericImpl getMeta(Class<?> clazz, Generic[] components) {
 		Extends extendsAnnotation = clazz.getAnnotation(Extends.class);
 		Class<?> meta = extendsAnnotation == null || Engine.class.equals(extendsAnnotation.meta()) ? EngineImpl.class : extendsAnnotation.meta();
 		if (EngineImpl.class.equals(meta)) {
 			if (components.length == 0 || MetaAttribute.class.equals(clazz))
 				return getEngine();
 			else if (components.length == 1 || MetaRelation.class.equals(clazz))
 				meta = MetaAttribute.class;
 			else
 				meta = MetaRelation.class;
 		}
 		return this.<GenericImpl> find(meta);
 	}
 
 	<T extends Generic> T bind(Generic meta, Serializable value, Generic[] supers, Generic[] components, Class<?> specializationClass, boolean existsException, int basePos) {
 		return bind(((GenericImpl) meta).bindInstanceNode(value), meta, supers, components, specializationClass, existsException, basePos);
 	}
 
 	<T extends Generic> T bind(HomeTreeNode homeTreeNode, Generic meta, Generic[] supers, Generic[] components, Class<?> specializationClass, boolean existsException, int basePos) {
 		return internalBind(homeTreeNode, meta, new Primaries(homeTreeNode, supers).toArray(), components, specializationClass, existsException, basePos);
 	}
 
 	protected NavigableSet<Generic> getExtendedDirectSupers(final Generic meta, final boolean isProperty, final boolean isSingular, final int basePos, final HomeTreeNode[] primaries, final Generic[] components) {
 		return new TreeSet<Generic>() {
 			private static final long serialVersionUID = 8568383988023387246L;
 			{
 				Iterator<Generic> iterator = getExtendedDirectSupersIterator(meta, isProperty, isSingular, basePos, primaries, components);
 				while (iterator.hasNext())
 					add(iterator.next());
 			}
 
 			Iterator<Generic> getExtendedDirectSupersIterator(final Generic meta, final boolean isProperty, final boolean isSingular, final int basePos, final HomeTreeNode[] primaries, final Generic[] components) {
 				return new AbstractSelectableLeafIterator(getEngine()) {
 
 					@Override
 					protected boolean isSelectable() {
 						return true;
 					}
 
 					@Override
 					public boolean isSelected(Generic candidate) {
 						boolean result = GenericImpl.isSuperOf(((GenericImpl) candidate).primaries, ((GenericImpl) candidate).components, primaries, components);
 						if (result)
 							return true;
 						if (basePos != Statics.MULTIDIRECTIONAL)
 							if (GenericImpl.isSuperOf(((GenericImpl) meta).primaries, ((GenericImpl) meta).components, ((GenericImpl) candidate).primaries, ((GenericImpl) candidate).components)) {
 								if (meta.getMetaLevel() != candidate.getMetaLevel()) {
 									if (basePos < ((GenericImpl) candidate).components.length && !components[basePos].equals(((GenericImpl) candidate).components[basePos])) {
 										if (components[basePos].inheritsFrom(((GenericImpl) candidate).components[basePos])) {
 											if (!candidate.inheritsFrom(find(NoInheritanceSystemType.class)))
 												if (isSingular || isProperty && (Arrays.equals(Statics.truncate(basePos, ((GenericImpl) candidate).components), Statics.truncate(basePos, components))))
 													return true;
 
 										}
 									} else {
 										if (((GenericImpl) candidate).equiv(new Primaries(candidate, primaries).toArray(), GenericImpl.enrich(components, ((GenericImpl) candidate).components)))
 											return true;
 									}
 								}
 							}
 						return false;
 					}
 				};
 			}
 		};
 	}
 
 	@SuppressWarnings("unchecked")
 	<T extends Generic> T internalBind(HomeTreeNode homeTreeNode, Generic meta, HomeTreeNode[] primaries, Generic[] components, Class<?> specializationClass, boolean existsException, int basePos) throws RollbackException {
 		assert homeTreeNode.getMetaLevel() <= 2;
 		boolean isSingular = Statics.MULTIDIRECTIONAL != basePos && ((GenericImpl) meta).isSingularConstraintEnabled(basePos);
 		boolean isProperty = Statics.MULTIDIRECTIONAL != basePos && ((GenericImpl) meta).isPropertyConstraintEnabled();
 
 		NavigableSet<Generic> directSupers = getExtendedDirectSupers(meta, isProperty, isSingular, basePos, primaries, components);
 		for (Generic directSuper : directSupers) {
 			primaries = new Primaries(directSuper, primaries).toArray();
 			components = GenericImpl.enrich(components, ((GenericImpl) directSuper).components);
 		}
 
 		for (Generic directSuper : directSupers) {
 			if (((GenericImpl) directSuper).equiv(primaries, components))
 				if (directSupers.size() == 1 && homeTreeNode.equals(((GenericImpl) directSuper).homeTreeNode))
 					if (existsException)
 						rollback(new ExistsException(directSuper + " already exists !"));
 					else
 						return (T) directSuper;
 				else
 					rollback(new FunctionalConsistencyViolationException(directSuper.info() + " " + directSupers));
 		}
 
 		NavigableSet<Generic> orderedDependencies = getConcernedDependencies(new Generic[] { meta }, primaries, components, isProperty, isSingular, basePos);
 		for (Generic generic : orderedDependencies.descendingSet())
 			simpleRemove(generic);
 		ConnectionMap connectionMap = new ConnectionMap();
 		T bind = buildAndInsertComplex(homeTreeNode, ((GenericImpl) meta).specializeInstanceClass(specializationClass), directSupers.toArray(new Generic[directSupers.size()]), components);
 		connectionMap.reBind(homeTreeNode, bind, orderedDependencies, isProperty, isSingular, basePos);
 		return bind;
 	}
 
 	private NavigableSet<Generic> getConcernedDependencies(final Generic[] supers, final HomeTreeNode[] primaries, final Generic[] components, final boolean isProperty, final boolean isSingular, final int basePos) {
 		return new TreeSet<Generic>() {
 			private static final long serialVersionUID = -38728500742395848L;
 			{
 				for (Generic superGeneric : supers) {
 					Iterator<Generic> removeIterator = concernedDependenciesIterator(superGeneric, primaries, components, isProperty, isSingular, basePos);
					while (removeIterator.hasNext()) {
						Generic next = removeIterator.next();
						addAll(orderDependencies((GenericImpl) next));
					}
 				}
 			}
 
 			<T extends Generic> Iterator<T> concernedDependenciesIterator(final Generic meta, final HomeTreeNode[] primaries, final Generic[] components, final boolean isProperty, final boolean isSingular, final int basePos) {
 				return new AbstractFilterIterator<T>(new AbstractPreTreeIterator<T>((T) meta) {
 
 					private static final long serialVersionUID = 3038922934693070661L;
 
 					{
 						next();
 					}
 
 					@Override
 					public Iterator<T> children(T node) {
 						if (isAncestorOf(primaries, components, ((GenericImpl) node).primaries, ((GenericImpl) node).components))
 							return Collections.emptyIterator();
 
 						if (Statics.MULTIDIRECTIONAL != basePos) {
 							if (meta.getMetaLevel() != node.getMetaLevel())
 								if (basePos < ((GenericImpl) node).components.length && ((GenericImpl) node).components[basePos].inheritsFrom(components[basePos]))
									if (isSingular || isProperty && (Arrays.equals(Statics.truncate(basePos, ((GenericImpl) node).components), Statics.truncate(basePos, components))))
 										return Collections.emptyIterator();
 						}
 						return new ConcateIterator<T>(((GenericImpl) node).<T> directInheritingsIterator(), ((GenericImpl) node).<T> compositesIterator());
 					}
 				}) {
 					@Override
 					public boolean isSelected() {
 						if (isAncestorOf(primaries, components, ((GenericImpl) next).primaries, ((GenericImpl) next).components))
 							return true;
 
 						if (Statics.MULTIDIRECTIONAL != basePos) {
 							if (meta.getMetaLevel() != next.getMetaLevel())
 								if (basePos < ((GenericImpl) next).components.length && ((GenericImpl) next).components[basePos].inheritsFrom(components[basePos])) {
									if (isSingular || (isProperty && (Arrays.equals(Statics.truncate(basePos, ((GenericImpl) next).components), Statics.truncate(basePos, components)))))
 										return true;
 								}
 						}
 						return false;
 					}
 				};
 			}
 		};
 	}
 
 	public static boolean isAncestorOf(HomeTreeNode[] primaries, Generic[] components, final HomeTreeNode[] subPrimaries, Generic[] subComponents) {
 		if (GenericImpl.isSuperOf(primaries, components, subPrimaries, subComponents))
 			return true;
 		for (Generic component : subComponents)
 			if (component != null)
 				if (!Arrays.equals(subPrimaries, ((GenericImpl) component).primaries) || !Arrays.equals(subComponents, ((GenericImpl) component).components))
 					if (isAncestorOf(primaries, components, ((GenericImpl) component).primaries, ((GenericImpl) component).components))
 						return true;
 		return false;
 	}
 
 	<T extends Generic> T buildAndInsertComplex(HomeTreeNode homeTreeNode, Class<?> clazz, Generic[] supers, Generic[] components) {
 		T bind = insert(this.<EngineImpl> getEngine().buildComplex(homeTreeNode, clazz, supers, components));
 		if (bind.inheritsFrom(find(VirtualConstraintImpl.class)))
 			assert bind.inheritsFrom(find(NoInheritanceSystemType.class));
 		return bind;
 	}
 
 	protected void triggersDependencies(Class<?> clazz) {
 		Dependencies dependenciesClass = clazz.getAnnotation(Dependencies.class);
 		if (dependenciesClass != null)
 			for (Class<?> dependencyClass : dependenciesClass.value())
 				find(dependencyClass);
 	}
 
 	protected void check(CheckingType checkingType, boolean isFlushTime, Iterable<Generic> generics) throws ConstraintViolationException {
 		for (Generic generic : generics)
 			check(checkingType, isFlushTime, generic);
 	}
 
 	protected void check(CheckingType checkingType, boolean isFlushTime, Generic generic) throws ConstraintViolationException {
 		checkConsistency(checkingType, isFlushTime, generic);
 		checkConstraints(checkingType, isFlushTime, generic);
 	}
 
 	private boolean isConsistencyToCheck(CheckingType checkingType, boolean isFlushTime, Generic generic) {
 		if (isConstraintActivated(generic))
 			if (isFlushTime || isImmediatelyConsistencyCheckable((AbstractConstraintImpl) ((Holder) generic).getBaseComponent()))
 				return true;
 		return false;
 	}
 
 	protected boolean isConstraintActivated(Generic generic) {
 		if (((GenericImpl) generic).getValue() != null)
 			if (isConstraintValueSetting(generic))
 				if (!Boolean.FALSE.equals(generic.getValue()))
 					return true;
 		return false;
 
 	}
 
 	protected boolean isConstraintValueSetting(Generic generic) {
 		return generic.isInstanceOf(find(ConstraintValue.class));
 	}
 
 	protected boolean isImmediatelyConsistencyCheckable(AbstractConstraintImpl constraint) {
 		return constraint.isImmediatelyConsistencyCheckable();
 	}
 
 	protected void checkConsistency(CheckingType checkingType, boolean isFlushTime, Generic generic) throws ConstraintViolationException {
 		if (isConsistencyToCheck(checkingType, isFlushTime, generic)) {
 			AbstractConstraintImpl keyHolder = ((Holder) generic).getBaseComponent();
 			int axe = keyHolder.<AxedPropertyClass> getValue().getAxe();
 			Attribute constraintBase = keyHolder.<Holder> getBaseComponent().getBaseComponent();
 			if (!AbstractAxedConstraintImpl.class.isAssignableFrom(keyHolder.getClass()))
 				keyHolder.check(constraintBase, generic, (Holder) generic, axe);
 			else {
 				Type component = constraintBase.getComponent(axe);
 				if (component != null)
 					for (Generic instance : component.getAllInstances())
 						keyHolder.check(constraintBase, instance, (Holder) generic, axe);
 			}
 		}
 	}
 
 	private static class ConstraintComparator implements Comparator<AbstractConstraintImpl> {
 		@Override
 		public int compare(AbstractConstraintImpl o1, AbstractConstraintImpl o2) {
 			if (o1.getPriority() == o2.getPriority())
 				return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
 			return Integer.compare(o1.getPriority(), o2.getPriority());
 		}
 	}
 
 	private void checkConstraints(final CheckingType checkingType, final boolean isFlushTime, final Generic generic) throws ConstraintViolationException {
 
 		for (final Attribute attribute : ((Type) generic).getAttributes()) {
 			AbstractExtendedMap<AxedPropertyClass, Serializable> constraintMap = ((GenericImpl) attribute).getConstraintsMap();
 			TreeMap<AbstractConstraintImpl, Holder> constraints = new TreeMap<>(new ConstraintComparator());
 			for (Serializable key : constraintMap.keySet()) {
 				Holder valueHolder = constraintMap.getValueHolder(key);
 				AbstractConstraintImpl keyHolder = valueHolder.<AbstractConstraintImpl> getBaseComponent();
 				constraints.put(keyHolder, valueHolder);
 			}
 			for (Entry<AbstractConstraintImpl, Holder> entry : constraints.entrySet()) {
 				if (CacheImpl.this.isCheckable(entry.getKey(), attribute, checkingType, isFlushTime)) {
 					int axe = ((AxedPropertyClass) entry.getKey().getValue()).getAxe();
 					if (AbstractAxedConstraintImpl.class.isAssignableFrom(entry.getKey().getClass()) && attribute.getComponent(axe) != null && (generic.isInstanceOf(attribute.getComponent(axe)))) {
 						entry.getKey().check(attribute, generic, entry.getValue(), axe);
 					}
 				}
 			}
 		}
 
 		AbstractExtendedMap<AxedPropertyClass, Serializable> constraintMap = ((GenericImpl) generic).getConstraintsMap();
 		TreeMap<AbstractConstraintImpl, Holder> constraints = new TreeMap<>(new ConstraintComparator());
 		for (Serializable key : constraintMap.keySet()) {
 			Holder valueHolder = constraintMap.getValueHolder(key);
 			AbstractConstraintImpl keyHolder = valueHolder.<AbstractConstraintImpl> getBaseComponent();
 
 			constraints.put(keyHolder, valueHolder);
 		}
 
 		for (Entry<AbstractConstraintImpl, Holder> entry : constraints.entrySet()) {
 			Holder valueHolder = entry.getValue();
 			AbstractConstraintImpl keyHolder = entry.getKey();
 
 			if (CacheImpl.this.isCheckable(keyHolder, generic, checkingType, isFlushTime)) {
 				Generic baseConstraint = ((Holder) keyHolder.getBaseComponent()).getBaseComponent();
 				int axe = ((AxedPropertyClass) keyHolder.getValue()).getAxe();
 				if (generic.getMetaLevel() - baseConstraint.getMetaLevel() >= 1)
 					keyHolder.check(baseConstraint, AbstractAxedConstraintImpl.class.isAssignableFrom(keyHolder.getClass()) ? ((Attribute) generic).getComponent(axe) : generic, valueHolder, axe);
 			}
 
 		}
 	}
 
 	public boolean isCheckable(AbstractConstraintImpl constraint, Generic generic, CheckingType checkingType, boolean isFlushTime) {
 		return (isFlushTime || isImmediatelyCheckable(constraint)) && constraint.isCheckedAt(generic, checkingType);
 	}
 
 	protected boolean isImmediatelyCheckable(AbstractConstraintImpl constraint) {
 		return constraint.isImmediatelyCheckable();
 	}
 
 	@Override
 	void simpleAdd(Generic generic) {
 		adds.add(generic);
 		super.simpleAdd(generic);
 	}
 
 	@Override
 	void simpleRemove(Generic generic) {
 		if (adds.contains(generic))
 			adds.remove(generic);
 		else
 			removes.add(generic);
 		super.simpleRemove(generic);
 	}
 
 	private void addGeneric(Generic generic) throws ConstraintViolationException {
 		simpleAdd(generic);
 		check(CheckingType.CHECK_ON_ADD_NODE, false, generic);
 	}
 
 	protected void removeGeneric(Generic generic) throws ConstraintViolationException {
 		simpleRemove(generic);
 		check(CheckingType.CHECK_ON_REMOVE_NODE, false, generic);
 	}
 
 	private void checkConstraints() throws ConstraintViolationException {
 		check(CheckingType.CHECK_ON_ADD_NODE, true, adds);
 		check(CheckingType.CHECK_ON_REMOVE_NODE, true, removes);
 	}
 
 	static class CacheDependencies implements TimestampedDependencies {
 
 		private transient TimestampedDependencies underlyingDependencies;
 
 		private final PseudoConcurrentSnapshot inserts = new PseudoConcurrentSnapshot();
 		private final PseudoConcurrentSnapshot deletes = new PseudoConcurrentSnapshot();
 
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
 			private final Iterator<Generic> underlyingIterator;
 			private final Iterator<Generic> insertsIterator = inserts.iterator();
 
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
 
 	@Override
 	public Snapshot<Type> getAllTypes() {
 		return getEngine().getAllInstances();
 	}
 
 	static class UnsafeCache extends CacheImpl {
 		private static final long serialVersionUID = 4843334203915625618L;
 
 		public UnsafeCache(Cache cache) {
 			super(cache);
 		}
 
 		public UnsafeCache(Engine engine) {
 			super(engine);
 		}
 
 		@Override
 		protected boolean isImmediatelyCheckable(AbstractConstraintImpl constraint) {
 			return false;
 		}
 
 		@Override
 		protected boolean isImmediatelyConsistencyCheckable(AbstractConstraintImpl constraint) {
 			return false;
 		}
 
 		<T extends Generic> T unsafeSetValue(final Generic old, final Serializable value) throws ConstraintViolationException {
 			assert value != null;
 			return new Restructurator() {
 				@Override
 				Generic rebuild() {
 					HomeTreeNode newHomeTreeNode = ((GenericImpl) old).getHomeTreeNode().metaNode.bindInstanceNode(value);
 					HomeTreeNode[] primaries = Statics.replace(((GenericImpl) old).primaries, ((GenericImpl) old).getHomeTreeNode(), newHomeTreeNode);
 					Arrays.sort(primaries);
 					// TODO call internalBind whitout rebind dependencies
 					return internalBind(newHomeTreeNode, old.getMeta(), primaries, ((GenericImpl) old).selfToNullComponents(), old.getClass(), false, Statics.MULTIDIRECTIONAL);
 				}
 
 			}.rebuildAll(old);
 		}
 
 		<T extends Generic> T unsafeAddComponent(final Generic old, final Generic newComponent, final int pos) throws ConstraintViolationException {
 			return new Restructurator() {
 				@Override
 				Generic rebuild() {
 					return bind(((GenericImpl) old).getHomeTreeNode(), old.getMeta(), ((GenericImpl) old).supers, Statics.insertIntoArray(newComponent, ((GenericImpl) old).selfToNullComponents(), pos), old.getClass(), false, Statics.MULTIDIRECTIONAL);
 				}
 			}.rebuildAll(old);
 		}
 
 		<T extends Generic> T unsafeRemoveComponent(final Generic old, final int pos) throws ConstraintViolationException {
 			return new Restructurator() {
 				@Override
 				Generic rebuild() {
 					return bind(((GenericImpl) old).getHomeTreeNode(), old.getMeta(), ((GenericImpl) old).supers, Statics.truncate(pos, ((GenericImpl) old).selfToNullComponents()), old.getClass(), false, Statics.MULTIDIRECTIONAL);
 				}
 			}.rebuildAll(old);
 		}
 
 		<T extends Generic> T unsafeAddSuper(final Generic old, final Generic newSuper) throws ConstraintViolationException {
 			return new Restructurator() {
 				@Override
 				Generic rebuild() {
 					return bind(((GenericImpl) old).getHomeTreeNode(), old.getMeta(), Statics.insertLastIntoArray(newSuper, ((GenericImpl) old).supers), ((GenericImpl) old).selfToNullComponents(), old.getClass(), true, Statics.MULTIDIRECTIONAL);
 				}
 			}.rebuildAll(old);
 		}
 
 		void unsafeRemove(Generic generic, RemoveStrategy removeStrategy) throws ConstraintViolationException {
 			if (!isAlive(generic))
 				throw new AliveConstraintViolationException(generic + " is not alive");
 			switch (removeStrategy) {
 			case NORMAl:
 				orderAndRemoveDependenciesForRemove(generic);
 			break;
 			case CONSERVE:
 				NavigableSet<Generic> dependencies = orderAndRemoveDependencies(generic);
 				dependencies.remove(generic);
 				for (Generic dependency : dependencies)
 					bind(((GenericImpl) dependency).getHomeTreeNode(), dependency.getMeta(), ((GenericImpl) generic).supers, ((GenericImpl) dependency).components, dependency.getClass(), true, Statics.MULTIDIRECTIONAL);
 			break;
 			case FORCE:
 				orderAndRemoveDependencies(generic);
 			break;
 			case PROJECT:
 			// TODO impl
 			break;
 			}
 		}
 
 		<T extends Generic> T unsafeRemoveSuper(final Generic old, final int pos) throws ConstraintViolationException {
 			if (pos == 0 && ((GenericImpl) old).supers.length == 1)
 				rollback(new UnsupportedOperationException());// kk ?
 			return new Restructurator() {
 				@Override
 				Generic rebuild() {
 					return bind(((GenericImpl) old).getHomeTreeNode(), old.getMeta(), Statics.truncate(pos, ((GenericImpl) old).supers), ((GenericImpl) old).selfToNullComponents(), old.getClass(), true, Statics.MULTIDIRECTIONAL);
 				}
 			}.rebuildAll(old);
 		}
 
 		private <T extends Generic> NavigableSet<T> orderAndRemoveDependencies(final T old) throws ConstraintViolationException {
 			NavigableSet<T> orderedGenerics = orderDependencies(old);
 			for (T generic : orderedGenerics.descendingSet())
 				removeGeneric(generic);
 			return orderedGenerics;
 		}
 
 		private <T extends Generic> NavigableSet<T> orderAndRemoveDependenciesForRemove(final T old) throws ConstraintViolationException {
 			NavigableSet<T> orderedGenerics = orderDependenciesForRemove(old);
 			for (T generic : orderedGenerics.descendingSet())
 				removeGeneric(generic);
 			return orderedGenerics;
 		}
 
 		abstract class Restructurator {
 			@SuppressWarnings("unchecked")
 			<T extends Generic> T rebuildAll(Generic old) throws ConstraintViolationException {
 				NavigableSet<Generic> dependencies = orderAndRemoveDependencies(old);
 				dependencies.remove(old);
 				ConnectionMap map = new ConnectionMap();
 				map.put(old, rebuild());
 				return (T) map.reBind(dependencies).get(old);
 			}
 
 			abstract Generic rebuild();
 		}
 
 	}
 
 	@Override
 	public int getLevel() {
 		return subContext.getLevel() + 1;
 	}
 }
