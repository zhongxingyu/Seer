 package org.genericsystem.core;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import org.genericsystem.core.UnsafeGList.Supers;
 import org.genericsystem.exception.ExistsException;
 import org.genericsystem.exception.RollbackException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.iterator.AbstractFilterIterator;
 import org.genericsystem.iterator.AbstractPreTreeIterator;
 import org.genericsystem.iterator.AbstractSelectableLeafIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Nicolas Feybesse
  * 
  */
 class GenericBuilder {
 	protected static Logger log = LoggerFactory.getLogger(GenericBuilder.class);
 
 	private final CacheImpl cache;
 	private UnsafeVertex uVertex;
 	private Generic meta;
 	private boolean isSingular;
 	private boolean isProperty;
	private int basePos;
 
 	GenericBuilder(CacheImpl cache, Generic meta, UnsafeVertex uVertex, int basePos, boolean respectSupers) {
 		this.cache = cache;
 		this.meta = meta;
 		this.uVertex = uVertex;
		// this.homeTreeNode = homeTreeNode;
		// this.components = components;
 		this.basePos = basePos;
		// this.supers = new OrderedSupers(aliveSupers).toSupers()
 		isSingular = Statics.MULTIDIRECTIONAL != basePos && ((GenericImpl) meta).isSingularConstraintEnabled(basePos);
 		isProperty = Statics.MULTIDIRECTIONAL != basePos && ((GenericImpl) meta).isPropertyConstraintEnabled();
 		this.uVertex = new UnsafeVertex(uVertex.getHomeTreeNode(), getExtendedDirectSupers(respectSupers), uVertex.getComponents());
 	}
 
 	protected Supers getExtendedDirectSupers(final boolean respectSupers) {
 		final Engine engine = cache.getEngine();
 		Iterator<Generic> iterator = new AbstractSelectableLeafIterator(engine) {
 			{
 				if (respectSupers && !uVertex.getSupers().iterator().next().equals(engine))
 					iterators.put(engine, new SelectableIterator<>(uVertex.getSupers().iterator()));
 			}
 
 			@Override
 			public boolean isSelected(Generic candidate) {
				if (((GenericImpl) candidate).isSuperOf(uVertex))
					return true;
				if (isExtentedBy(candidate))
					return true;
				return false;
 			}
 		};
 		Set<Generic> set = new TreeSet<>();
 		while (iterator.hasNext())
 			set.add(iterator.next());
 		return new Supers(set);
 	}
 
 	boolean containsSuperInMultipleInheritanceValue(Generic candidate) {
 		if (uVertex.getSupers().size() <= 1 || !containsSuper(candidate))
 			return false;
 		return (sameHomeTreeNode());
 	}
 
 	boolean containsSuper(Generic candidate) {
 		for (Generic superGenenic : uVertex.getSupers())
 			if (candidate.equals(superGenenic))
 				return true;
 		return false;
 	}
 
 	boolean sameHomeTreeNode() {
 		for (Generic superGenenic : uVertex.getSupers())
 			if (!uVertex.getHomeTreeNode().equals(((GenericImpl) superGenenic).getHomeTreeNode()))
 				return false;
 		return true;
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	<T extends Generic> T find(boolean existsException) throws RollbackException {
 		if (uVertex.getSupers().size() == 1)
 			if (((GenericImpl) uVertex.getSupers().get(0)).equiv(uVertex.getHomeTreeNode(), uVertex.getComponents()))
 				if (existsException)
 					cache.rollback(new ExistsException(uVertex.getSupers().get(0) + " already exists !"));
 				else
 					return (T) uVertex.getSupers().get(0);
 		return null;
 	}
 
 	<T extends Generic> T bindDependency(Class<?> specializationClass, boolean existsException, boolean automatic) throws RollbackException {
 		T result = find(existsException);
 		if (result != null)
 			return result;
 		return buildDependency(specializationClass, automatic);
 	}
 
 	private <T extends Generic> T buildDependency(Class<?> specializationClass, boolean automatic) {
 		return cache.<T> insert(cache.<EngineImpl> getEngine().buildComplex(((GenericImpl) meta).specializeInstanceClass(specializationClass), uVertex), automatic);
 	}
 
 	<T extends Generic> T internalBind(final Class<?> specializationClass, int basePos, boolean existsException, final boolean automatic) throws RollbackException {
 		T result = find(existsException);
 		if (result != null)
			return result; // log.info("" + candidate + " " + sameHomeTreeNode());
 
 		Generic old = null;
 		Set<Generic> directDependencies = getDirectDependencies();
 		for (Generic dependency : directDependencies)
 			if (!existsException && Statics.MULTIDIRECTIONAL != basePos && (((GenericImpl) dependency).getComponent(basePos)).equals(uVertex.getComponents().get(basePos))) {
 				assert old == null;
 				old = dependency;
 			}
 		for (Generic dependency : directDependencies) {
 			assert !uVertex.getSupers().contains(dependency) : uVertex.getSupers();
 			assert !uVertex.getComponents().contains(dependency) : uVertex.getComponents();
 			assert !((GenericImpl) dependency).equiv(uVertex);
 		}
 		return cache.new Restructurator() {
 			private static final long serialVersionUID = 1370210509322258062L;
 
 			@Override
 			Generic rebuild() {
 				return GenericBuilder.this.buildDependency(specializationClass, automatic);
 			}
 		}.rebuildAll(old, directDependencies, basePos);
 	}
 
 	Set<Generic> getDirectDependencies() {
 		Iterator<Generic> iterator = new AbstractFilterIterator<Generic>(new AbstractPreTreeIterator<Generic>(meta) {
 			private static final long serialVersionUID = 3038922934693070661L;
 			{
 				next();
 			}
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return !isAncestorOf(node) ? ((GenericImpl) node).<Generic> dependenciesIterator() : Collections.<Generic> emptyIterator();
 			}
 		}) {
 			@Override
 			public boolean isSelected() {
 				return isAncestorOf((next)) || isExtention(next);
 			}
 		};
 
 		Set<Generic> set = new TreeSet<>();
 		while (iterator.hasNext())
 			set.add(iterator.next());
 		return set;
 	}
 
 	private boolean isExtention(Generic candidate) {
 		if (candidate.getMeta().equals(meta)) {
 			if (Statics.MULTIDIRECTIONAL != basePos && basePos < ((GenericImpl) candidate).components().size()) {
 				if (isSingular && ((GenericImpl) candidate).getComponent(basePos).inheritsFrom(uVertex.getComponents().get(basePos)))
 					return true;
 				if ((isProperty) && areComponentsInheriting((((GenericImpl) candidate).components()), uVertex.getComponents()))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isExtentedBy(Generic candidate) {
 		if (Statics.MULTIDIRECTIONAL != basePos && basePos < ((GenericImpl) candidate).components().size())
 			if (((GenericImpl) candidate).getHomeTreeNode().equals(uVertex.getHomeTreeNode()) || !uVertex.getComponents().get(basePos).equals(((GenericImpl) candidate).getComponent(basePos)))
 				if ((((Attribute) meta).isInheritanceEnabled()))
 					if (uVertex.getHomeTreeNode().getMetaLevel() == candidate.getMetaLevel()) {
 						if (isSingular && uVertex.getComponents().get(basePos).inheritsFrom(((GenericImpl) candidate).getComponent(basePos)) && (!uVertex.getComponents().get(basePos).equals(((GenericImpl) candidate).getComponent(basePos))))
 							return true;
 						if ((isSingular || isProperty) && areComponentsInheriting(uVertex.getComponents(), ((GenericImpl) candidate).components()))
 							return true;
 					}
 		return false;
 	}
 
 	private static boolean areComponentsInheriting(List<Generic> subComponents, List<Generic> components) {
 		for (int i = 0; i < components.size(); i++)
 			if (!subComponents.get(i).inheritsFrom(components.get(i)))
 				return false;
 		return true;
 	}
 
 	private boolean isAncestorOf(final Generic dependency) {
 		if (((GenericImpl) dependency).inheritsFrom(uVertex))
 			return true;
 		for (Generic component : ((GenericImpl) dependency).components())
 			if (!dependency.equals(component))
 				if (isAncestorOf(component))
 					return true;
 		return false;
 	}
 }
