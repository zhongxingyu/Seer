 package org.genericsystem.core;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeSet;
 import org.genericsystem.core.Statics.OrderedDependencies;
 import org.genericsystem.core.Statics.OrderedSupers;
 import org.genericsystem.core.UnsafeGList.Supers;
 import org.genericsystem.exception.ExistsException;
 import org.genericsystem.exception.RollbackException;
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
 
 	private UnsafeVertex uVertex;
 	private HardProperties hardProperties;
 
 	GenericBuilder(UnsafeVertex uVertex, boolean respectSupers) {
 		assert uVertex.getMeta().isMeta() || uVertex.getMeta().isStructural();
 		this.uVertex = uVertex;
 		hardProperties = new HardProperties();
 		this.uVertex = new UnsafeVertex(uVertex.homeTreeNode(), getExtendedMeta(respectSupers), getExtendedDirectSupers(respectSupers), findStrictSupersBeyond(respectSupers), uVertex.components());
 		assert uVertex.getMeta().isMeta() || uVertex.getMeta().isStructural();
 	}
 
 	private class HardProperties {
 		private Boolean isStrongSingular[] = new Boolean[uVertex.components().size()];
 		private Boolean isProperty;// TODO KK change for strongProperty ?
 
 		private boolean isStrongSingular(int i) {
 			if (isStrongSingular[i] == null)
 				isStrongSingular[i] = ((GenericImpl) uVertex.getMeta()).isSingularConstraintEnabled(i) && !((GenericImpl) uVertex.getMeta()).isReferentialIntegrity(i);
 			return isStrongSingular[i];
 		}
 
 		private boolean isProperty() {
 			return isProperty != null ? isProperty : (isProperty = ((GenericImpl) uVertex.getMeta()).isPropertyConstraintEnabled());
 		}
 	}
 
 	private Supers getExtendedDirectSupers(final boolean respectSupers) {
 		final Engine engine = ((GenericImpl) uVertex.getMeta()).getEngine();
 		Iterator<Generic> iterator = new AbstractSelectableLeafIterator(engine) {
 			{
 				if (respectSupers && !uVertex.supers().iterator().next().equals(engine))
 					iterators.put(engine, new SelectableIterator<>(uVertex.supers().iterator()));
 			}
 
 			@Override
 			public boolean isSelected(Generic candidate) {
 				// log.info("zzzzzzzzzz" + ((GenericImpl) candidate).isSuperOf(uVertex) + " " + candidate.info() + " " + uVertex.homeTreeNode() + " " + uVertex.components() + " " + uVertex.supers());
 				return ((GenericImpl) candidate).isSuperOf(uVertex) || isExtentedBy(candidate);
 			}
 		};
 		Set<Generic> set = new TreeSet<>();
 		while (iterator.hasNext())
 			set.add(iterator.next());
 		return new Supers(set);
 	}
 
 	private Generic getExtendedMeta(final boolean respectSupers) {
 		Generic meta = respectSupers ? uVertex.getMeta() : findMetaAbove(uVertex.getMeta(), new Generic[] { uVertex.getMeta().getEngine() });
 		return getExtendedMeta(meta);
 	}
 
 	private Generic findMetaAbove(Generic meta, Generic[] result) {
 		for (Generic strictSuper : meta.getStrictSupers())
 			if (((GenericImpl) strictSuper).isSuperOf(uVertex) || isExtentedBy(strictSuper))
 				if (strictSuper.inheritsFrom(result[0]))
 					result[0] = strictSuper;
 				else
 					assert result[0] == null || result[0].inheritsFrom(strictSuper);
 			else
 				findMetaAbove(strictSuper, result);
 		return result[0];
 	}
 
 	private Generic getExtendedMeta(Generic meta) {
 		for (Generic candidate : ((GenericImpl) meta).getInheritings())
 			if (((GenericImpl) candidate).isSuperOf(uVertex) || isExtentedBy(candidate))
 				return getExtendedMeta(candidate);
 		return meta;
 	}
 
 	private Supers findStrictSupersBeyond(final boolean respectSupers) {
		OrderedSupers orderedSupers = respectSupers ? new OrderedSupers(uVertex.strictSupers()) : findStrictSupersAbove(uVertex.strictSupers(), new OrderedSupers());
		for (Generic orderedSuper : orderedSupers)
 			findStrictSupersBeyond(orderedSuper, orderedSupers);
 		return orderedSupers.toSupers();
 	}
 
	private OrderedSupers findStrictSupersAbove(Supers strictSupers, OrderedSupers result) {
 		for (Generic strictSuper : strictSupers)
 			if (((GenericImpl) strictSuper).isSuperOf(uVertex) || isExtentedBy(strictSuper))
 				result.add(strictSuper);
 			else
 				findStrictSupersAbove(((GenericImpl) strictSuper).getStrictSupers(), result);
		return result;
 	}
 
 	private void findStrictSupersBeyond(Generic orderedSuper, OrderedSupers orderedSupers) {
 		for (Generic inheriting : orderedSuper.getInheritings())
 			if (((GenericImpl) inheriting).isSuperOf(uVertex) || isExtentedBy(inheriting)) {
 				orderedSupers.add(inheriting);
 				findStrictSupersBeyond(inheriting, orderedSupers);
 				return;
 			}
 	}
 
 	boolean containsSuperInMultipleInheritanceValue(Generic candidate) {
 		if (uVertex.supers().size() <= 1 || !containsSuper(candidate))
 			return false;
 		return (sameHomeTreeNode());
 	}
 
 	boolean containsSuper(Generic candidate) {
 		for (Generic superGenenic : uVertex.supers())
 			if (candidate.equals(superGenenic))
 				return true;
 		return false;
 	}
 
 	boolean sameHomeTreeNode() {
 		for (Generic superGenenic : uVertex.supers())
 			if (!uVertex.homeTreeNode().equals(((GenericImpl) superGenenic).homeTreeNode()))
 				return false;
 		return true;
 	}
 
 	private CacheImpl getCurrentCache() {
 		return ((GenericImpl) uVertex.getMeta()).getCurrentCache();
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	<T extends Generic> T find(boolean existsException) throws RollbackException {
 		if (uVertex.supers().size() == 1)
 			if (((GenericImpl) uVertex.supers().get(0)).equiv(uVertex.homeTreeNode(), uVertex.components()))
 				if (existsException)
 					getCurrentCache().rollback(new ExistsException(uVertex.supers().get(0) + " already exists !"));
 				else
 					return (T) uVertex.supers().get(0);
 		return null;
 	}
 
 	<T extends Generic> T bindDependency(Class<?> specializationClass, boolean existsException, boolean automatic) throws RollbackException {
 		T result = find(existsException);
 		if (result != null)
 			return result;
 		return buildDependency(specializationClass, automatic);
 	}
 
 	private <T extends Generic> T buildDependency(Class<?> specializationClass, boolean automatic) {
 		CacheImpl cache = getCurrentCache();
 		return cache.<T> insert(cache.<EngineImpl> getEngine().build(((GenericImpl) uVertex.getMeta()).specializeInstanceClass(specializationClass), uVertex), automatic);
 	}
 
 	<T extends Generic> T internalBind(final Class<?> specializationClass, boolean existsException, final boolean automatic) throws RollbackException {
 		T result = find(existsException);
 		if (result != null)
 			return result;
 
 		Generic old = null;
 		NavigableSet<Generic> directDependencies = getDependencies();
 		for (Generic dependency : directDependencies)
 			if (!existsException)
 				if (Statics.CONCRETE == uVertex.metaLevel())
 					for (int pos = 0; pos < uVertex.components().size(); pos++)
 						// TODO KK do not work for properties when dim > 1 !
 						if ((hardProperties.isProperty() || hardProperties.isStrongSingular(pos)) && (((GenericImpl) dependency).getComponent(pos)).equals(uVertex.components().get(pos))) {
 							assert old == null || old == dependency;
 							old = dependency;
 						}
 		for (Generic dependency : directDependencies) {
 			assert !uVertex.supers().contains(dependency) : uVertex.supers().get(0).info() + " " + uVertex.components();
 			assert !uVertex.components().contains(dependency) : uVertex.components();
 			assert !((GenericImpl) dependency).equiv(uVertex) : dependency.info() + ((GenericImpl) dependency).isSuperOf(uVertex);
 		}
 		return getCurrentCache().new Restructurator() {
 			private static final long serialVersionUID = 1370210509322258062L;
 
 			@Override
 			Generic rebuild() {
 				return GenericBuilder.this.buildDependency(specializationClass, automatic);
 			}
 		}.rebuildAll(old, directDependencies);
 	}
 
 	NavigableSet<Generic> getDependencies() {
 		Iterator<Generic> iterator = new AbstractFilterIterator<Generic>(new AbstractPreTreeIterator<Generic>((uVertex.getMeta())) {
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
 
 		OrderedDependencies dependencies = new OrderedDependencies();
 		while (iterator.hasNext())
 			dependencies.addDependencies(iterator.next());
 		return dependencies;
 	}
 
 	private boolean isExtention(Generic candidate) {
 		if (Statics.CONCRETE == uVertex.metaLevel() && candidate.getMeta().equals((uVertex.getMeta()))) {
 			if (hardProperties.isProperty())
 				if (areComponentsInheriting((((GenericImpl) candidate).getComponents()), uVertex.components()))
 					return true;
 			for (int pos = 0; pos < ((GenericImpl) candidate).getComponents().size(); pos++)
 				if (hardProperties.isStrongSingular(pos))
 					if (((GenericImpl) candidate).getComponent(pos).inheritsFrom(uVertex.components().get(pos))) {
 						if (!((GenericImpl) candidate).getComponent(pos).equals(uVertex.components().get(pos)))
 							return true;
 						if (!(((GenericImpl) candidate).homeTreeNode().equals(uVertex.homeTreeNode())) || !areComponentsInheriting(uVertex.components(), ((GenericImpl) candidate).getComponents()))
 							return true;
 					}
 		}
 		return false;
 
 	}
 
 	private boolean isExtentedBy(Generic candidate) {
 		if (Statics.CONCRETE == uVertex.metaLevel() && ((GenericImpl) uVertex.getMeta()).isInheritanceEnabled() && candidate.getMeta().equals((uVertex.getMeta()))) {
 			if (hardProperties.isProperty())
 				if (areComponentsInheriting(uVertex.components(), ((GenericImpl) candidate).getComponents())) {
 					if (!uVertex.components().equals(((GenericImpl) candidate).getComponents()))
 						return true;
 					if (((GenericImpl) candidate).homeTreeNode().equals(uVertex.homeTreeNode()))
 						return true;
 				}
 			for (int pos = 0; pos < uVertex.components().size(); pos++) {
 				if ((hardProperties.isStrongSingular(pos))) {
 					if (uVertex.components().get(pos).inheritsFrom(((GenericImpl) candidate).getComponent(pos))) {
 						if (!uVertex.components().get(pos).equals(((GenericImpl) candidate).getComponent(pos)))
 							return true;
 						if (((GenericImpl) candidate).homeTreeNode().equals(uVertex.homeTreeNode()))
 							if (areComponentsInheriting(uVertex.components(), ((GenericImpl) candidate).getComponents()))
 								return true;
 					}
 				}
 			}
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
 		for (Generic component : ((GenericImpl) dependency).getComponents())
 			if (!dependency.equals(component))
 				if (isAncestorOf(component))
 					return true;
 		return false;
 	}
 }
