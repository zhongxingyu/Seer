 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Objects;
 import java.util.Set;
 import org.genericsystem.annotation.InstanceGenericClass;
 import org.genericsystem.annotation.constraints.InstanceValueClassConstraint;
 import org.genericsystem.annotation.constraints.PropertyConstraint;
 import org.genericsystem.annotation.constraints.SingletonConstraint;
 import org.genericsystem.annotation.constraints.SingularConstraint;
 import org.genericsystem.annotation.constraints.UniqueValueConstraint;
 import org.genericsystem.annotation.constraints.VirtualConstraint;
 import org.genericsystem.constraints.InstanceClassConstraintImpl;
 import org.genericsystem.constraints.PropertyConstraintImpl;
 import org.genericsystem.constraints.RequiredConstraintImpl;
 import org.genericsystem.constraints.SingletonConstraintImpl;
 import org.genericsystem.constraints.SingularConstraintImpl;
 import org.genericsystem.constraints.SizeConstraintImpl;
 import org.genericsystem.constraints.UniqueValueConstraintImpl;
 import org.genericsystem.constraints.VirtualConstraintImpl;
 import org.genericsystem.core.EngineImpl.RootTreeNode;
 import org.genericsystem.core.Snapshot.Projector;
 import org.genericsystem.exception.AmbiguousSelectionException;
 import org.genericsystem.exception.RollbackException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.MapProvider;
 import org.genericsystem.generic.Node;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Tree;
 import org.genericsystem.generic.Type;
 import org.genericsystem.iterator.AbstractConcateIterator.ConcateIterator;
 import org.genericsystem.iterator.AbstractFilterIterator;
 import org.genericsystem.iterator.AbstractPreTreeIterator;
 import org.genericsystem.iterator.AbstractProjectorAndFilterIterator;
 import org.genericsystem.iterator.AbstractSelectableLeafIterator;
 import org.genericsystem.iterator.ArrayIterator;
 import org.genericsystem.iterator.CartesianIterator;
 import org.genericsystem.iterator.CountIterator;
 import org.genericsystem.iterator.SingletonIterator;
 import org.genericsystem.map.AbstractMapProvider;
 import org.genericsystem.map.AbstractMapProvider.AbstractExtendedMap;
 import org.genericsystem.map.AxedPropertyClass;
 import org.genericsystem.map.ConstraintsMapProvider;
 import org.genericsystem.map.PropertiesMapProvider;
 import org.genericsystem.map.SystemPropertiesMapProvider;
 import org.genericsystem.snapshot.AbstractSnapshot;
 import org.genericsystem.systemproperties.CascadeRemoveSystemProperty;
 import org.genericsystem.systemproperties.NoInheritanceSystemType;
 import org.genericsystem.systemproperties.NoReferentialIntegritySystemProperty;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Nicolas Feybesse
  * @author Michael Ory
  * 
  */
 @SuppressWarnings("unchecked")
 public class GenericImpl implements Generic, Type, Link, Relation, Holder, Attribute {
 
 	protected static Logger log = LoggerFactory.getLogger(GenericImpl.class);
 
 	private LifeManager lifeManager;
 
 	HomeTreeNode homeTreeNode;
 
 	Generic[] supers;
 
 	Generic[] components;
 
 	// HomeTreeNode[] primaries;
 
 	public Generic[] getSupersArray() {
 		return supers.clone();
 	}
 
 	public Generic[] getComponentsArray() {
 		return components.clone();
 	}
 
 	// public HomeTreeNode[] getPrimariesArray() {
 	// return primaries.clone();
 	// }
 
 	@Override
 	public boolean fastValueEquals(Generic generic) {
 		return homeTreeNode.equals(((GenericImpl) generic).homeTreeNode);
 	}
 
 	public HomeTreeNode bindInstanceNode(Serializable value) {
 		return homeTreeNode.bindInstanceNode(value);
 	}
 
 	public HomeTreeNode findInstanceNode(Serializable value) {
 		return homeTreeNode.findInstanceNode(value);
 	}
 
 	public HomeTreeNode getHomeTreeNode() {
 		return homeTreeNode;
 	}
 
 	final GenericImpl initialize(HomeTreeNode homeTreeNode, Generic[] directSupers, Generic[] components) {
 		return restore(homeTreeNode, null, Long.MAX_VALUE, 0L, Long.MAX_VALUE, directSupers, components);
 	}
 
 	final GenericImpl restore(HomeTreeNode homeTreeNode, Long designTs, long birthTs, long lastReadTs, long deathTs, Generic[] supers, Generic[] components) {
 		assert homeTreeNode != null;
 		this.homeTreeNode = homeTreeNode;
 		this.supers = supers;
 		Arrays.sort(supers);
 		this.components = nullToSelfComponent(components);
 		lifeManager = new LifeManager(designTs == null ? getEngine().pickNewTs() : designTs, birthTs, lastReadTs, deathTs);
 		// primaries = !isEngine() ? new Primaries(homeTreeNode, supers).toArray() : new HomeTreeNode[] { homeTreeNode };
 		// assert primaries.length != 0;
 
 		for (Generic g1 : supers)
 			for (Generic g2 : supers)
 				if (!g1.equals(g2))
 					assert !g1.inheritsFrom(g2) : "" + Arrays.toString(supers);
 		assert getMetaLevel() == homeTreeNode.getMetaLevel() : getMetaLevel() + " " + homeTreeNode.getMetaLevel() + " " + (homeTreeNode instanceof RootTreeNode);
 		for (Generic superGeneric : supers) {
 			if (this.equals(superGeneric) && !isEngine())
 				getCurrentCache().rollback(new IllegalStateException());
 			if ((getMetaLevel() - superGeneric.getMetaLevel()) > 1)
 				getCurrentCache().rollback(new IllegalStateException());
 			if ((getMetaLevel() - superGeneric.getMetaLevel()) < 0)
 				getCurrentCache().rollback(new IllegalStateException());
 		}
 		return this;
 	}
 
 	<T extends Generic> T plug() {
 		Set<Generic> componentSet = new HashSet<>();
 		for (Generic component : components)
 			if (componentSet.add(component))
 				((GenericImpl) component).lifeManager.engineComposites.add(this);
 
 		Set<Generic> effectiveSupersSet = new HashSet<>();
 		for (Generic effectiveSuper : supers)
 			if (effectiveSupersSet.add(effectiveSuper))
 				((GenericImpl) effectiveSuper).lifeManager.engineDirectInheritings.add(this);
 
 		return (T) this;
 	}
 
 	public LifeManager getLifeManager() {
 		return lifeManager;
 	}
 
 	@Override
 	public int compareTo(Generic generic) {
 		long birthTs = getBirthTs();
 		long compareBirthTs = ((GenericImpl) generic).getBirthTs();
 		return birthTs == compareBirthTs ? Long.compare(getDesignTs(), ((GenericImpl) generic).getDesignTs()) : Long.compare(birthTs, compareBirthTs);
 	}
 
 	@Override
 	public EngineImpl getEngine() {
 		return (EngineImpl) supers[0].getEngine();
 	}
 
 	@Override
 	public boolean isEngine() {
 		return false;
 	}
 
 	@Override
 	public boolean isInstanceOf(Generic meta) {
 		return getMetaLevel() - meta.getMetaLevel() == 1 ? this.inheritsFrom(meta) : false;
 	}
 
 	@Override
 	public <T extends Generic> T getMeta() throws RollbackException {
 		HomeTreeNode metaNode = homeTreeNode.metaNode;
 		for (Generic superGeneric : supers)
 			if (((GenericImpl) superGeneric).homeTreeNode.equals(metaNode))
 				return (T) superGeneric;
 		for (Generic superGeneric : supers)
 			if (((GenericImpl) superGeneric).homeTreeNode.inheritsFrom(metaNode))
 				return superGeneric.getMeta();
 		getCurrentCache().rollback(new IllegalStateException("Unable to find a meta for : " + this.info()));
 		return null;// Unreachable
 	}
 
 	@Override
 	public int getMetaLevel() {
 		return homeTreeNode.getMetaLevel();
 	}
 
 	@Override
 	public boolean isConcrete() {
 		return Statics.CONCRETE == getMetaLevel();
 	}
 
 	@Override
 	public boolean isStructural() {
 		return Statics.STRUCTURAL == getMetaLevel();
 	}
 
 	@Override
 	public boolean isMeta() {
 		return Statics.META == getMetaLevel();
 	}
 
 	@Override
 	public boolean isType() {
 		return components.length == 0;
 	}
 
 	@Override
 	public boolean isAttribute() {
 		return components.length >= 1;
 	}
 
 	@Override
 	public boolean isReallyAttribute() {
 		return components.length == 1;
 	}
 
 	@Override
 	public boolean isAttributeOf(Generic generic) {
 		for (Generic component : components)
 			if (generic.inheritsFrom(component))
 				return true;
 		return false;
 	}
 
 	@Override
 	public boolean isAttributeOf(Generic generic, int basePos) {
 		if (basePos < 0 || basePos >= components.length)
 			return false;
 		return generic.inheritsFrom(components[basePos]);
 	}
 
 	@Override
 	public boolean isRelation() {
 		return components.length > 1;
 	}
 
 	@Override
 	public boolean isReallyRelation() {
 		return components.length == 2;
 	}
 
 	@Override
 	public <S extends Serializable> S getValue() {
 		return homeTreeNode.getValue();
 	}
 
 	@Override
 	public <T extends Serializable> Snapshot<T> getValues(final Holder attribute) {
 		return getHolders(attribute).project(new Projector<T, Holder>() {
 			@Override
 			public T project(Holder holder) {
 				return holder.<T> getValue();
 			}
 		});
 	}
 
 	@Override
 	public <T extends Serializable> T getValue(Holder attribute) {
 		Link holder = getHolder(attribute);
 		return holder != null ? holder.<T> getValue() : null;
 	}
 
 	@Override
 	public <T extends Holder> T setValue(Holder attribute, Serializable value) {
 		T holder = setHolder(attribute, value);
 		assert value == null || getValues(attribute).contains(value) : "holder : " + holder.info() + " value : " + value + " => " + getValues(attribute);
 		return holder;
 	}
 
 	@Override
 	public <T extends Link> T setLink(Link relation, Serializable value, Generic... targets) {
 		return setHolder(relation, value, targets);
 	}
 
 	@Override
 	public <T extends Link> T setLink(Link relation, Serializable value, int basePos, Generic... targets) {
 		return setHolder(relation, value, basePos, targets);
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, Generic... targets) {
 		return setHolder(attribute, value, getBasePos(attribute), targets);
 	}
 
 	public <T extends Holder> T setHolderByValue(Class<?> specializationClass, Holder attribute, Serializable value, Generic... targets) {
 		Holder holder = getHolderByValue(Statics.CONCRETE, attribute, value);
 		return setHolder(specializationClass, holder != null ? holder : attribute, value, getBasePos(attribute), targets);
 	}
 
 	public <T extends Holder> T setHolderByValue(Holder attribute, Serializable value, Generic... targets) {
 		Holder holder = getHolderByValue(Statics.CONCRETE, attribute, value);
 		return setHolder(holder != null ? holder : attribute, value, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, int basePos, Serializable value, Generic... targets) {
 		return addHolder(attribute, value, basePos, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, Serializable value, int basePos, int metaLevel, Generic... targets) {
 		return bind(metaLevel == attribute.getMetaLevel() ? attribute.getMeta() : attribute, value, null, attribute, basePos, true, targets);
 	}
 
 	public <T extends Holder> T bind(Generic meta, Serializable value, Class<?> specializationClass, Holder directSuper, int basePos, boolean existsException, Generic... targets) {
 		return getCurrentCache().bind(meta, value, specializationClass, directSuper, existsException, basePos, Statics.insertIntoArray(this, targets, basePos));
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, Generic... targets) {
 		cancelAll(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, int basePos, Generic... targets) {
 		internalClearAll(attribute, basePos, Statics.CONCRETE, false, targets);
 		Iterator<Holder> holders = this.<Holder> holdersIterator(attribute, attribute.getMetaLevel(), basePos, targets);
 		while (holders.hasNext())
 			internalCancel(holders.next(), basePos);
 	}
 
 	@Override
 	public void cancel(Holder holder) {
 		internalClear(unambigousFirst(holdersIterator(holder, holder.getMetaLevel() + 1, getBasePos(holder))), false);
 		internalCancel(unambigousFirst(holdersIterator(holder, holder.getMetaLevel(), getBasePos(holder))), getBasePos(holder));
 	}
 
 	private void internalCancel(Holder holder, int basePos) {
 		if (holder != null)
 			setHolder(holder, null, getBasePos(holder), Statics.truncate(basePos, ((GenericImpl) holder).components));
 	}
 
 	@Override
 	public void clearAll(Holder attribute, Generic... targets) {
 		clearAll(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public void clearAll(Holder attribute, int basePos, Generic... targets) {
 		internalClearAll(attribute, basePos, attribute.getMetaLevel(), true, targets);
 	}
 
 	private void internalClearAll(Holder attribute, int basePos, int metaLevel, boolean baseFilter, Generic... targets) {
 		Iterator<Holder> holders = this.<Holder> holdersIterator(attribute, metaLevel, basePos, targets);
 		while (holders.hasNext())
 			internalClear(holders.next(), baseFilter);
 	}
 
 	private void internalClear(Holder holder, boolean baseFilter) {
 		if (holder != null)
 			if (baseFilter) {
 				if (equals(holder.getBaseComponent()))
 					holder.remove();
 			} else
 				holder.remove();
 	}
 
 	@Override
 	public void clear(Holder holder) {
 		internalClear(unambigousFirst(holdersIterator(holder, holder.getMetaLevel(), getBasePos(holder))), true);
 	}
 
 	public <T extends Generic> Iterator<T> thisFilter(Iterator<T> concreteIterator) {
 		return new AbstractFilterIterator<T>(concreteIterator) {
 			@Override
 			public boolean isSelected() {
 				return !GenericImpl.this.equals(next);
 			}
 		};
 	}
 
 	@Override
 	public int getBasePos(Holder attribute) {
 		Iterator<Integer> iterator = positionsIterator(attribute);
 		return iterator.hasNext() ? iterator.next() : Statics.BASE_POSITION;
 	}
 
 	@Override
 	public <T extends Holder> Snapshot<T> getHolders(final Holder attribute, final Generic... targets) {
 		return getHolders(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> Snapshot<T> getHolders(final Holder attribute, final int basePos, final Generic... targets) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return holdersIterator((Attribute) attribute, Statics.CONCRETE, basePos, targets);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Link> Snapshot<T> getLinks(final Relation relation, final Generic... targets) {
 		return getLinks(relation, getBasePos(relation), targets);
 	}
 
 	@Override
 	public <T extends Link> Snapshot<T> getLinks(final Relation relation, final int basePos, final Generic... targets) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return linksIterator(relation, basePos, targets);
 			}
 		};
 	}
 
 	private <T extends Link> Iterator<T> linksIterator(final Link relation, final int basePos, final Generic... targets) {
 		return new AbstractFilterIterator<T>(GenericImpl.this.<T> holdersIterator(relation, Statics.CONCRETE, basePos, targets)) {
 			@Override
 			public boolean isSelected() {
 				return next.isRelation();
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getTargets(Relation relation) {
 		return getTargets(relation, Statics.BASE_POSITION, Statics.TARGET_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getTargets(Relation relation, int basePos, final int targetPos) {
 		return getLinks(relation, basePos).project(new Projector<T, Link>() {
 			@Override
 			public T project(Link element) {
 				return element.getComponent(targetPos);
 			}
 		});
 	}
 
 	<T extends Generic> Iterator<T> targetsFilter(Iterator<T> iterator, Holder attribute, final Generic... targets) {
 		final List<Integer> positions = ((GenericImpl) attribute).getComponentsPositions(Statics.insertFirst(this, targets));
 		return new AbstractFilterIterator<T>(iterator) {
 			@Override
 			public boolean isSelected() {
 				for (int i = 0; i < targets.length; i++)
 					if (!targets[i].equals(((Holder) next).getComponent(positions.get(i + 1))))
 						return false;
 				return true;
 			}
 		};
 	}
 
 	public <T extends Holder> Iterator<T> holdersIterator(Holder attribute, int metaLevel, int basePos, Generic... targets) {
 		return this.<T> targetsFilter(GenericImpl.this.<T> holdersIterator(metaLevel, attribute, basePos), attribute, targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(Holder attribute, Generic... targets) {
 		return getHolder(Statics.CONCRETE, attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(int metaLevel, Holder attribute, Generic... targets) {
 		return getHolder(metaLevel, attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(int metaLevel, Holder attribute, int basePos, Generic... targets) {
 		return this.unambigousFirst(this.<T> holdersIterator(attribute, metaLevel, basePos, targets));
 	}
 
 	public <T extends Holder> T getHolderByValue(int metaLevel, Holder attribute, Serializable value, final Generic... targets) {
 		return getHolderByValue(metaLevel, attribute, value, getBasePos(attribute), targets);
 	}
 
 	public <T extends Holder> T getHolderByValue(int metaLevel, Holder attribute, Serializable value, int basePos, final Generic... targets) {
 		return this.unambigousFirst(Statics.valueFilter(this.<T> holdersIterator(attribute, metaLevel, basePos, targets), value));
 	}
 
 	@Override
 	public <T extends Link> T getLink(Link relation, int basePos, Generic... targets) {
 		return this.unambigousFirst(this.<T> linksIterator(relation, basePos, targets));
 	}
 
 	@Override
 	public <T extends Link> T getLink(Link relation, Generic... targets) {
 		return this.unambigousFirst(this.<T> linksIterator(relation, getBasePos(relation), targets));
 	}
 
 	public <T extends Generic> Iterator<T> directInheritingsIterator() {
 		return getCurrentCache().directInheritingsIterator(this);
 	}
 
 	public <T extends Generic> Iterator<T> dependenciesIterator() {
 		return new ConcateIterator<T>(this.<T> directInheritingsIterator(), this.<T> compositesIterator());
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getInheritings() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return directInheritingsIterator();
 			}
 		};
 	}
 
 	public <T extends Generic> Iterator<T> compositesIterator() {
 		return getCurrentCache().compositesIterator(this);
 	}
 
 	public <T extends Generic> Iterator<T> compositesIterator(final int pos) {
 		return new AbstractFilterIterator<T>(this.<T> compositesIterator()) {
 			@Override
 			public boolean isSelected() {
 				return GenericImpl.this.equals(((Holder) next).getComponent(pos));
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getComposites() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return compositesIterator();
 			}
 		};
 	}
 
 	public <T extends Generic> Iterator<T> attributesIterator() {
 		return this.<T> holdersIterator(Statics.STRUCTURAL, getCurrentCache().getMetaAttribute(), Statics.MULTIDIRECTIONAL);
 	}
 
 	@Override
 	public <T extends Attribute> Snapshot<T> getAttributes() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return attributesIterator();
 			}
 		};
 	}
 
 	@Override
 	public <T extends Attribute> Snapshot<T> getAttributes(final Attribute attribute) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return holdersIterator(Statics.STRUCTURAL, attribute, Statics.MULTIDIRECTIONAL);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(final Serializable value, Generic... targets) {
 		return getAttribute(getCurrentCache().getMetaAttribute(), value, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(Attribute attribute, final Serializable value, Generic... targets) {
 		return this.unambigousFirst(this.targetsFilter(Statics.valueFilter(this.<T> holdersIterator(Statics.STRUCTURAL, attribute, Statics.MULTIDIRECTIONAL), value), attribute, targets));
 	}
 
 	private <T extends Relation> Iterator<T> relationsIterator(boolean readPhantom) {
 		return new AbstractFilterIterator<T>(GenericImpl.this.<T> attributesIterator()) {
 			@Override
 			public boolean isSelected() {
 				return next.isRelation();
 			}
 		};
 
 	}
 
 	@Override
 	public <T extends Relation> Snapshot<T> getRelations() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return relationsIterator(false);
 			}
 		};
 	}
 
 	public <T extends Generic> T reFind() {
 		return getCurrentCache().reFind(this);
 	}
 
 	@Override
 	public <T extends Relation> T getRelation(final Serializable value) {
 		return this.unambigousFirst(Statics.valueFilter(this.<T> relationsIterator(value == null), value));
 	}
 
 	@Override
 	public <T extends Attribute> T getProperty(Serializable value, Generic... targets) {
 		return getAttribute(value, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T setProperty(Serializable value, Generic... targets) {
 		return setAttribute(value, targets).enableSingularConstraint();
 	}
 
 	public <T extends Attribute> T setSubProperty(Attribute property, Serializable value, Generic... targets) {
 		return setSubAttribute(property, value, targets).enableSingularConstraint();
 	}
 
 	@Override
 	public <T extends Relation> T setRelation(Serializable value, Generic... targets) {
 		return setAttribute(value, targets);
 	}
 
 	public <T extends Attribute> T addSubRelation(Relation relation, Serializable value, Generic... targets) {
 		return addSubAttribute(relation, value, targets);
 	}
 
 	public <T extends Attribute> T setSubRelation(Relation relation, Serializable value, Generic... targets) {
 		return setSubAttribute(relation, value, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T setAttribute(Serializable value, Generic... targets) {
 		return setHolder(null, getEngine(), value, Statics.STRUCTURAL, Statics.BASE_POSITION, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T addAttribute(Serializable value, Generic... targets) {
 		return addHolder((Holder) getEngine(), value, Statics.BASE_POSITION, Statics.STRUCTURAL, targets);
 	}
 
 	public <T extends Relation> T setSubAttribute(Attribute attribute, Serializable value, Generic... targets) {
 		T holder = setHolder(null, attribute, value, Statics.STRUCTURAL, getBasePos(attribute), targets);
 		assert holder == null || holder.inheritsFrom(attribute) : holder.info() + attribute.info();
 		return holder;
 	}
 
 	public <T extends Relation> T addSubAttribute(Attribute attribute, Serializable value, Generic... targets) {
 		T holder = addHolder(attribute, value, getBasePos(attribute), Statics.STRUCTURAL, targets);
 		assert holder.inheritsFrom(attribute) : holder.info();
 		return holder;
 	}
 
 	public <T extends Relation> T addSubProperty(Attribute attribute, Serializable value, Generic... targets) {
 		return addSubAttribute(attribute, value, targets).enableSingularConstraint();
 	}
 
 	public <T extends Holder> T setHolder(Class<?> specializationClass, Holder attribute, Serializable value, int metaLevel, int basePos, Generic... targets) {
 		return this.<T> bind(metaLevel == attribute.getMetaLevel() ? attribute.getMeta() : attribute, value, specializationClass, attribute, basePos, false, targets);
 	}
 
 	public <T extends Holder> T setHolder(Class<?> specializationClass, Holder attribute, Serializable value, Generic... targets) {
 		return this.<T> setHolder(specializationClass, attribute, value, Statics.CONCRETE, getBasePos(attribute), targets);
 	}
 
 	public <T extends Holder> T setHolder(Class<?> specializationClass, Holder attribute, Serializable value, int basePos, Generic... targets) {
 		return this.<T> setHolder(specializationClass, attribute, value, Statics.CONCRETE, basePos, targets);
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, int basePos, Generic... targets) {
 		return this.<T> setHolder(null, attribute, value, basePos, targets);
 	}
 
 	@Override
 	public <T extends Holder> T flag(Holder attribute, Generic... targets) {
 		return setHolder(attribute, Statics.FLAG, targets);
 	}
 
 	@Override
 	public <T extends Generic> T newAnonymousInstance(Generic... components) {
 		return newInstance(getEngine().pickNewAnonymousReference(), components);
 	}
 
 	@Override
 	public <T extends Generic> T newInstance(Serializable value, Generic... components) {
 		return getCurrentCache().bind(this, value, null, this, false, Statics.MULTIDIRECTIONAL, components);
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Generic... components) {
 		return getCurrentCache().bind(getMeta(), value, null, this, false, Statics.MULTIDIRECTIONAL, components);
 	}
 
 	@Override
 	public <T extends Link> T bind(Link relation, Generic... targets) {
 		return flag(relation, targets);
 	}
 
 	private class TakenPositions extends ArrayList<Integer> {
 
 		private static final long serialVersionUID = 1777313486204962418L;
 		private final int max;
 
 		public TakenPositions(int max) {
 			this.max = max;
 		}
 
 		public int getFreePosition(Generic component) throws RollbackException {
 			int freePosition = 0;
 			while (contains(freePosition) || (freePosition < components.length && !component.inheritsFrom(components[freePosition])))
 				freePosition++;
 			if (freePosition >= max)
 				getCurrentCache().rollback(new IllegalStateException("Unable to find a valid position for : " + component.info() + " in : " + Arrays.toString(components) + " " + components[0].info()));
 			add(freePosition);
 			return freePosition;
 		}
 	}
 
 	Generic[] sortAndCheck(Generic... components) {
 		TakenPositions takenPositions = new TakenPositions(components.length);
 		Generic[] result = new Generic[components.length];
 		for (Generic component : components)
 			result[takenPositions.getFreePosition(component == null ? GenericImpl.this : component)] = component;
 		return result;
 	}
 
 	public <T extends Generic> Iterator<T> holdersIterator(final int level, Holder origin, int basePos) {
 		if (Statics.STRUCTURAL == level)
 			basePos = Statics.MULTIDIRECTIONAL;
 		return origin.inheritsFrom(getEngine().getCurrentCache().find(NoInheritanceSystemType.class)) ? this.<T> noInheritanceIterator(level, basePos, origin) : this.<T> inheritanceIterator(level, origin, basePos);
 	}
 
 	private <T extends Generic> Iterator<T> noInheritanceIterator(final int metaLevel, int pos, final Generic origin) {
 		return new AbstractFilterIterator<T>(Statics.MULTIDIRECTIONAL == pos ? this.<T> compositesIterator() : this.<T> compositesIterator(pos)) {
 			@Override
 			public boolean isSelected() {
 				return next.getMetaLevel() == metaLevel && next.inheritsFrom(origin);
 			}
 		};
 	}
 
 	public <T extends Generic> Iterator<T> inheritanceIterator(final int level, final Generic origin, final int pos) {
 		return (Iterator<T>) new AbstractSelectableLeafIterator(origin) {
 
 			@Override
 			public boolean isSelectable() {
 				return level == next.getMetaLevel();
 			}
 
 			@Override
 			public final boolean isSelected(Generic candidate) {
 				boolean selected = candidate.getMetaLevel() <= level && (pos != Statics.MULTIDIRECTIONAL ? ((GenericImpl) candidate).isAttributeOf(GenericImpl.this, pos) : ((GenericImpl) candidate).isAttributeOf(GenericImpl.this));
 				if (pos != Statics.MULTIDIRECTIONAL && selected && ((GenericImpl) candidate).isPseudoStructural(pos))
 					((GenericImpl) candidate).project(pos);
 
 				return selected;
 			}
 		};
 	}
 
 	public void project() {
 		project(Statics.MULTIDIRECTIONAL);
 	}
 
 	public void project(final int pos) {
 		Iterator<Object[]> cartesianIterator = new CartesianIterator(projections(pos));
 		while (cartesianIterator.hasNext()) {
 			final Generic[] components = (Generic[]) cartesianIterator.next();
 			// final Generic[] newComponents = new Components(components, GenericImpl.this.components).toArray();
 			for (Generic component : components)
 				assert component.isAlive();
 			// Generic projection = this.unambigousFirst(componentsFilter(((GenericImpl) getMeta()).allInheritingsIteratorWithoutRoot(), components));
 			Generic projection = this.unambigousFirst(new AbstractFilterIterator<Generic>(allInheritingsIteratorWithoutRoot()) {
 				@Override
 				public boolean isSelected() {
 					// log.info(next.info() + "homeTreeNode : " + ((GenericImpl) next).getHomeTreeNode() + " " + Arrays.toString(((GenericImpl) next).components) + Arrays.toString(components));
 					return isSuperOf(((GenericImpl) getMeta()).bindInstanceNode(Statics.FLAG), new Generic[] { GenericImpl.this }, Statics.replace(pos, components, ((GenericImpl) next).getComponent(pos)), next);
 				}
 			});
 
 			if (projection == null) {
 				// assert false : this.getComponents() + Arrays.toString(newComponents);
 				getCurrentCache().bind(getMeta(), Statics.FLAG, new Generic[] { this }, components, null, Statics.MULTIDIRECTIONAL, true, false);
 			}
 		}
 	}
 
 	static <T extends Generic> Iterator<T> componentsFilter(Iterator<T> iterator, final Generic... components) {
 		return new AbstractFilterIterator<T>(iterator) {
 			@Override
 			public boolean isSelected() {
 				for (int i = 0; i < components.length; i++)
 					if (!components[i].equals(((Holder) next).getComponent(i)))
 						return false;
 				return true;
 			}
 		};
 	}
 
 	private Iterable<Generic>[] projections(final int pos) {
 		final Iterable<Generic>[] projections = new Iterable[components.length];
 		for (int i = 0; i < projections.length; i++) {
 			final int column = i;
 			projections[i] = new Iterable<Generic>() {
 				@Override
 				public Iterator<Generic> iterator() {
 					return pos != column && components[column].isStructural() ? ((GenericImpl) components[column]).allInstancesIterator() : new SingletonIterator<Generic>(components[column]);
 				}
 			};
 		}
 		return projections;
 	}
 
 	@Override
 	// TODO KK
 	public boolean inheritsFrom(Generic generic) {
 		if (generic == null)
 			return false;
 		if (getDesignTs() < ((GenericImpl) generic).getDesignTs())
 			return false;
 		boolean inheritance = ((GenericImpl) generic).isSuperOf2(this);
 		// boolean inheritance3 = ((GenericImpl) generic).isSuperOf3(this);
 		// assert inheritance == inheritance3 : generic.info() + this.info() + " : " + inheritance + " != " + inheritance3;
 		// boolean superOf = ((GenericImpl) generic).isSuperOf(this);
 		// assert inheritance == superOf : "" + this.info() + generic.info() + " : " + inheritance + " != " + superOf;
 		return inheritance;
 	}
 
 	public boolean inheritsFrom2(Generic generic) {
 		if (equals(generic))
 			return true;
 		if (generic.isEngine())
 			return true;
 		if (getDesignTs() < ((GenericImpl) generic).getDesignTs())
 			return false;
 		for (Generic directSuper : supers)
 			if (((GenericImpl) directSuper).inheritsFrom2(generic))
 				return true;
 		return false;
 	}
 
 	public boolean isSuperOf2(Generic subGeneric) {
 		if (GenericImpl.this.equals(subGeneric))
 			return true;
 		if (subGeneric.isEngine())
 			return isEngine();
 		for (Generic directSuper : ((GenericImpl) subGeneric).supers)
 			if (isSuperOf2(directSuper))
 				return true;
 		return false;
 	}
 
 	@Override
 	public boolean inheritsFromAll(Generic... generics) {
 		for (Generic generic : generics)
 			if (!inheritsFrom(generic))
 				return false;
 		return true;
 	}
 
 	@Deprecated
 	public boolean isSuperOf(Generic subGeneric) {
 		return isSuperOf(homeTreeNode, supers, components, ((GenericImpl) subGeneric).homeTreeNode, ((GenericImpl) subGeneric).supers, ((GenericImpl) subGeneric).components);
 	}
 
 	public static boolean isSuperOf(HomeTreeNode homeTreeNode, Generic[] supers, Generic[] components, Generic subGeneric) {
 		return isSuperOf(homeTreeNode, supers, components, ((GenericImpl) subGeneric).homeTreeNode, ((GenericImpl) subGeneric).supers, ((GenericImpl) subGeneric).components);
 	}
 
 	public boolean isSuperOf(HomeTreeNode subHomeTreeNode, Generic[] subSupers, Generic[] subComponents) {
 		return isSuperOf(homeTreeNode, supers, components, subHomeTreeNode, subSupers, subComponents);
 	}
 
 	public static boolean isSuperOf(HomeTreeNode homeTreeNode, Generic[] supers, Generic[] components, HomeTreeNode subHomeTreeNode, Generic[] subSupers, Generic[] subComponents) {
 		if (homeTreeNode.equals(subHomeTreeNode) && Arrays.equals(supers, subSupers) && Arrays.equals(components, subComponents))
 			return true;
 		if (homeTreeNode.getMetaLevel() > subHomeTreeNode.getMetaLevel())
 			return false;
 		if (subComponents.length < components.length)
 			return false;
 		else if (subComponents.length > components.length) {
 			for (int i = 0; i < subComponents.length; i++)
 				if (isSuperOf(homeTreeNode, supers, components, subHomeTreeNode, subSupers, Statics.truncate(i, subComponents)))
 					return true;
 			return false;
 		}
 		assert subComponents.length == components.length;
 
 		for (Generic subSuper : subSupers) {
 			if (isSuperOf(homeTreeNode, supers, components, ((GenericImpl) subSuper).homeTreeNode, ((GenericImpl) subSuper).supers, ((GenericImpl) subSuper).components)) {
 				return true;
 			}
 		}
 
 		logValue("AAA", "Power", homeTreeNode, supers, components, "Power", subHomeTreeNode, subSupers, subComponents);
 
 		for (int i = 0; i < subComponents.length; i++) {
 			assert subComponents[i] != null || components[i] != null;
 			if (components[i] != null && subComponents[i] != null) {
 				if (!((GenericImpl) subComponents[i]).inheritsFrom(components[i])) {
 					// logValue("ZZZ", "Power", homeTreeNode, supers, components, "Power", subHomeTreeNode, subSupers, subComponents);
 					// log.info("ZZZ2 " + subComponents[i] + " " + components[i] + " false");
 					return false;
 				}
 				if (!subComponents[i].equals(components[i]))
 					for (Generic subSuper : ((GenericImpl) subComponents[i]).supers) {
 						if (subSuper.inheritsFrom(components[i]))
 							if (!subSuper.equals(subComponents[i])) {
 								// logValue("BBB", "Power", homeTreeNode, supers, components, "Power", subHomeTreeNode, subSupers, subComponents);
 								if (isSuperOf(homeTreeNode, supers, components, subHomeTreeNode, subSupers, Statics.replace(i, subComponents, subSuper))) {
 									// logValue("CCC", "Power", homeTreeNode, supers, components, "Power", subHomeTreeNode, subSupers, subComponents);
 									return true;
 								}
 							}
 
 						return false;
 					}
 			}
 			if (subComponents[i] == null) {
 				for (Generic subSuper : subSupers) {
 					if (subSuper.inheritsFrom(components[i]))
 						// if (!subSuper.equals(subComponents[i]))
 						if (isSuperOf(homeTreeNode, supers, components, subHomeTreeNode, subSupers, Statics.replace(i, subComponents, subSuper)))
 							return true;
 					// logValue(ConstraintValue.class, homeTreeNode, supers, components, true, subHomeTreeNode, subSupers, subComponents);
 
 					return false;
 				}
 			}
 
 			if (components[i] == null) {
 				if (!((GenericImpl) subComponents[i]).equiv(homeTreeNode, supers, components))
 					for (Generic subSuper : ((GenericImpl) subComponents[i]).supers) {
 						if (subSuper.inheritsFrom(components[i]))
 							// if (!subSuper.equals(subComponents[i]))
 							if (isSuperOf(homeTreeNode, supers, components, subHomeTreeNode, subSupers, Statics.replace(i, subComponents, subSuper)))
 								return true;
 						// logValue(ConstraintValue.class, homeTreeNode, supers, components, true, subHomeTreeNode, subSupers, subComponents);
 
 						return false;
 					}
 			}
 
 		}
 
 		assert subComponents.length == components.length;
 		assert Arrays.equals(components, subComponents) : Arrays.toString(components) + " " + Arrays.toString(subComponents);
 
		if (subHomeTreeNode.inheritsFrom(homeTreeNode)) {
			for (Generic superGeneric : supers)
				if (!((GenericImpl) superGeneric).isSuperOf(subHomeTreeNode, subSupers, subComponents))
					return false;
			return true;
		}
 		return false;
 	}
 
 	private static void logValue(String mark, Serializable value, HomeTreeNode homeTreeNode, Generic[] supers, Generic[] components, Serializable value2, HomeTreeNode subHomeTreeNode, Generic[] subSupers, Generic[] subComponents) {
 		if (Objects.equals(homeTreeNode.getValue(), value) && Objects.equals(subHomeTreeNode.getValue(), value2)) {
 			log.info(mark);
 			log.info(homeTreeNode + "  " + subHomeTreeNode + " " + Objects.equals(homeTreeNode, subHomeTreeNode));
 			log.info(Arrays.toString(supers) + " " + Arrays.toString(subSupers) + " " + Arrays.equals(supers, subSupers));
 			log.info(Arrays.toString(components) + " " + Arrays.toString(subComponents) + " " + Arrays.equals(components, subComponents));
 			log.info("\n");
 		}
 	}
 
 	private static boolean areAllSupersReached(Generic[] supers, HomeTreeNode subHomeTreeNode, Generic[] subSupers, Generic[] subComponents) {
 		for (Generic superGeneric : supers)
			if (((GenericImpl) superGeneric).isSuperOf(subHomeTreeNode, subSupers, subComponents))
 				return false;
 		return true;
 	}
 
 	// public boolean isSuperOf3(HomeTreeNode subHomeTreeNode, Generic[] subSupers, Generic[] subComponents) {
 	// if (equiv(subHomeTreeNode, subSupers, subComponents))
 	// return true;
 	// if (!internalIsSuperOf3(components, subComponents))
 	// return false;
 	// if (!internalIsSuperOf3(subHomeTreeNode, subSupers))
 	// return false;
 	// return true;
 	// }
 	//
 	// private boolean internalIsSuperOf3(HomeTreeNode subHomeTreeNode, Generic[] subSupers) {
 	// if (subHomeTreeNode.inheritsFrom(homeTreeNode))
 	// if (!subHomeTreeNode.equals(homeTreeNode) || Statics.CONCRETE != subHomeTreeNode.getMetaLevel())
 	// return true;
 	// for (Generic sub : subSupers)
 	// if (sub.inheritsFrom(this))
 	// return true;
 	// return false;
 	// }
 
 	// private static boolean internalIsSuperOf3(Generic[] components, Generic[] subComponents) {
 	// if (components.length == subComponents.length) {
 	// for (int i = 0; i < subComponents.length; i++) {
 	// if (components[i] != null && subComponents[i] != null) {
 	// if (!Arrays.equals(components, ((GenericImpl) components[i]).components) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 	// if (!(subComponents[i].inheritsFrom(components[i])))
 	// return false;
 	// }
 	// if (components[i] == null) {
 	// if (!Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 	// return false;
 	// } else if (subComponents[i] == null)
 	// if (!components[i].isEngine() && (!Arrays.equals(components, ((GenericImpl) components[i]).components)))
 	// return false;
 	// }
 	// return true;
 	// }
 	//
 	// if (components.length < subComponents.length)
 	// for (int i = 0; i < subComponents.length; i++)
 	// if (internalIsSuperOf3(components, Statics.truncate(i, subComponents)))
 	// return true;
 	//
 	// return false;
 	// }
 
 	// private static boolean internalIsSuperOf3bis(HomeTreeNode homeTreeNode, Generic[] supers, Generic[] components, HomeTreeNode subHomeTreeNode, Generic[] subSupers) {
 	// if (subHomeTreeNode.inheritsFrom(homeTreeNode))
 	// if (!subHomeTreeNode.equals(homeTreeNode) || Statics.CONCRETE != subHomeTreeNode.getMetaLevel())
 	// return true;
 	// for (Generic sub : subSupers)
 	// if (((GenericImpl) sub).inheritsFrom2(homeTreeNode, supers, components))
 	// return true;
 	// return false;
 	// }
 
 	public boolean inheritsFrom2(HomeTreeNode homeTreeNode, Generic[] supers, Generic[] components) {
 		if (equiv(homeTreeNode, supers, components))
 			return true;
 		if (getEngine().equiv(homeTreeNode, supers, components))
 			return true;
 		if (isEngine())
 			return false;
 		for (Generic directSuper : this.supers)
 			if (((GenericImpl) directSuper).inheritsFrom2(homeTreeNode, supers, components))
 				return true;
 		return false;
 	}
 
 	// // Conserve this!!!
 	//
 	// public boolean isSuperOf(Generic generic) {
 	// assert generic != null;
 	// if (equals(generic))
 	// return true;
 	// if (((GenericImpl) generic).isEngine())
 	// return isEngine();
 	// return isSuperOf(primaries, components, ((GenericImpl) generic).primaries, ((GenericImpl) generic).components);
 	// }
 
 	// // Conserve this!!!
 	// public boolean isSuperOf(HomeTreeNode subHomeTreeNode, final HomeTreeNode[] subPrimaries, Generic[] subComponents) {
 	// return subHomeTreeNode.inheritsFrom(homeTreeNode) && isSuperOf(primaries, components, subPrimaries, subComponents);
 	// }
 
 	// // Conserve this!!!
 	// public static boolean isSuperOf(HomeTreeNode[] primaries, Generic[] components, final HomeTreeNode[] subPrimaries, Generic[] subComponents) {
 	// if (primaries.length == subPrimaries.length && components.length == subComponents.length) {
 	// for (int i = 0; i < subPrimaries.length; i++) {
 	// if (!subPrimaries[i].inheritsFrom(primaries[i]))
 	// return false;
 	// }
 	// for (int i = 0; i < subComponents.length; i++) {
 	// if (components[i] != null && subComponents[i] != null) {
 	// if (!Arrays.equals(primaries, ((GenericImpl) components[i]).primaries) || !Arrays.equals(components, ((GenericImpl) components[i]).components) || !Arrays.equals(subPrimaries, ((GenericImpl) subComponents[i]).primaries)
 	// || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 	// if (!((GenericImpl) components[i]).isSuperOf(subComponents[i]))
 	// return false;
 	// }
 	// if (components[i] == null) {
 	// if (!Arrays.equals(subPrimaries, ((GenericImpl) subComponents[i]).primaries) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 	// return false;
 	// } else if (subComponents[i] == null)
 	// if (!components[i].isEngine() && (!Arrays.equals(primaries, ((GenericImpl) components[i]).primaries) || !Arrays.equals(components, ((GenericImpl) components[i]).components)))
 	// return false;
 	// }
 	// return true;
 	// }
 	//
 	// if (components.length <= subComponents.length && primaries.length < subPrimaries.length)
 	// for (int i = 0; i < subPrimaries.length; i++)
 	// if (isSuperOf(primaries, components, Statics.truncate(i, subPrimaries), subComponents))
 	// return true;
 	// if (components.length < subComponents.length && primaries.length <= subPrimaries.length)
 	// for (int i = 0; i < subComponents.length; i++)
 	// if (isSuperOf(primaries, components, subPrimaries, Statics.truncate(i, subComponents)))
 	// return true;
 	//
 	// return false;
 	// }
 
 	@Override
 	public void remove() {
 		remove(RemoveStrategy.NORMAl);
 	}
 
 	@Override
 	public void remove(RemoveStrategy removeStrategy) {
 		getCurrentCache().remove(this, removeStrategy);
 	}
 
 	@Override
 	public boolean isAlive() {
 		return getCurrentCache().isAlive(this);
 	}
 
 	public boolean isAlive(long ts) {
 		return lifeManager.isAlive(ts);
 	}
 
 	public long getDesignTs() {
 		return lifeManager.getDesignTs();
 	}
 
 	public long getBirthTs() {
 		return lifeManager.getBirthTs();
 	}
 
 	public long getDeathTs() {
 		return lifeManager.getDeathTs();
 	}
 
 	public long getLastReadTs() {
 		return lifeManager.getLastReadTs();
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getSupers() {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return directSupersIterator();
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> directSupersIterator() {
 		return new ArrayIterator<T>((T[]) supers);
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getComponents() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return componentsIterator();
 			}
 		};
 	}
 
 	@Override
 	public int getComponentsSize() {
 		return components.length;
 	}
 
 	@Override
 	public int getSupersSize() {
 		return supers.length;
 	}
 
 	private <T extends Generic> Iterator<T> componentsIterator() {
 		return new ArrayIterator<T>((T[]) components);
 	}
 
 	@Override
 	public void log() {
 		log.info(info());
 	}
 
 	@Override
 	public String info() {
 		String s = "\n******************************" + System.identityHashCode(this) + "******************************\n";
 		s += " Name        : " + toString() + "\n";
 		s += " Meta        : " + getMeta() + " (" + System.identityHashCode(getMeta()) + ")\n";
 		s += " Category    : " + getCategoryString() + "\n";
 		s += " Class       : " + getClass().getSimpleName() + "\n";
 		s += "**********************************************************************\n";
 		for (Generic superGeneric : supers)
 			s += " Super       : " + superGeneric + " (" + System.identityHashCode(superGeneric) + ")\n";
 		for (Generic component : components)
 			s += " Component   : " + component + " (" + System.identityHashCode(component) + ")\n";
 		s += "**********************************************************************\n";
 
 		// for (Attribute attribute : getAttributes())
 		// if (!(attribute.getValue() instanceof Class) /* || !Constraint.class.isAssignableFrom((Class<?>) attribute.getValue()) */) {
 		// s += ((GenericImpl) attribute).getCategoryString() + "   : " + attribute + " (" + System.identityHashCode(attribute) + ")\n";
 		// for (Holder holder : getHolders(attribute))
 		// s += "                          ----------> " + ((GenericImpl) holder).getCategoryString() + " : " + holder + "\n";
 		// }
 		// s += "**********************************************************************\n";
 		s += "design date : " + new SimpleDateFormat(Statics.LOG_PATTERN).format(new Date(getDesignTs() / Statics.MILLI_TO_NANOSECONDS)) + "\n";
 		// s += "birth date  : " + new SimpleDateFormat(Statics.LOG_PATTERN).format(new Date(getBirthTs() / Statics.MILLI_TO_NANOSECONDS)) + "\n";
 		// s += "death date  : " + new SimpleDateFormat(Statics.LOG_PATTERN).format(new Date(getDeathTs() / Statics.MILLI_TO_NANOSECONDS)) + "\n";
 		s += "**********************************************************************\n";
 
 		return s;
 	}
 
 	@Override
 	public String toString() {
 		Serializable value = getValue();
 		if (null == value)
 			return "null" + (supers.length >= 2 ? "[" + supers[1] + "]" : "");
 		return value instanceof Class ? ((Class<?>) value).getSimpleName() : value.toString();
 	}
 
 	public String toCategoryString() {
 		return "(" + getCategoryString() + ") " + toString();
 	}
 
 	public String getCategoryString() throws RollbackException {
 		int metaLevel = getMetaLevel();
 		int dim = getComponentsSize();
 		switch (metaLevel) {
 		case Statics.META:
 			switch (dim) {
 			case Statics.TYPE_SIZE:
 				return "MetaType";
 			case Statics.ATTRIBUTE_SIZE:
 				return "MetaAttribute";
 			case Statics.RELATION_SIZE:
 				return "MetaRelation";
 			default:
 				return "MetaNRelation";
 			}
 		case Statics.STRUCTURAL:
 			switch (dim) {
 			case Statics.TYPE_SIZE:
 				return "Type";
 			case Statics.ATTRIBUTE_SIZE:
 				return "Attribute";
 			case Statics.RELATION_SIZE:
 				return "Relation";
 			default:
 				return "NRelation";
 			}
 		case Statics.CONCRETE:
 			switch (dim) {
 			case Statics.TYPE_SIZE:
 				return "Instance";
 			case Statics.ATTRIBUTE_SIZE:
 				return "Holder";
 			case Statics.RELATION_SIZE:
 				return "Link";
 			default:
 				return "NLink";
 			}
 		default:
 			getCurrentCache().rollback(new IllegalStateException());
 			return null;// Uneachable
 		}
 	}
 
 	@Override
 	public <T extends Generic> T getBaseComponent() {
 		return getComponent(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> T getTargetComponent() {
 		return getComponent(Statics.TARGET_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> T getComponent(int componentPos) {
 		return components.length <= componentPos || componentPos < 0 ? null : (T) components[componentPos];
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getInstances() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return instancesIterator();
 			}
 		};
 	}
 
 	protected <T extends Generic> Iterator<T> instancesIterator() {
 		return Statics.<T> levelFilter(GenericImpl.this.<T> directInheritingsIterator(), getMetaLevel() + 1);
 	}
 
 	@Override
 	public <T extends Generic> T getInstance(final Serializable value) {
 		return this.unambigousFirst(Statics.<T> valueFilter(GenericImpl.this.<T> instancesIterator(), value));
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getAllInstances() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return allInstancesIterator();
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allInstancesIterator() {
 		return Statics.levelFilter(this.<T> allInheritingsAboveIterator(getMetaLevel() + 1), getMetaLevel() + 1);
 	}
 
 	private <T extends Generic> Iterator<T> allInheritingsAboveIterator(final int metaLevel) {
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 
 			private static final long serialVersionUID = 7164424160379931253L;
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return new AbstractFilterIterator<Generic>(((GenericImpl) node).directInheritingsIterator()) {
 					@Override
 					public boolean isSelected() {
 						return next.getMetaLevel() <= metaLevel;
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getDirectSubTypes() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return directSubTypesIterator();
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> directSubTypesIterator() {
 		return Statics.levelFilter(this.<T> directInheritingsIterator(), getMetaLevel());
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getSubTypes() {
 
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return allSubTypesIteratorWithoutRoot();
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> T getSubType(final Serializable value) {
 		return this.unambigousFirst(Statics.<T> valueFilter(this.<T> allSubTypesIteratorWithoutRoot(), value));
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getSubTypes(final String name) {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return Statics.valueFilter(GenericImpl.this.<T> allSubTypesIteratorWithoutRoot(), name);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allSubTypesIteratorWithoutRoot() {
 		return Statics.levelFilter(this.<T> allInheritingsIteratorWithoutRoot(), Statics.STRUCTURAL);
 	}
 
 	public <T extends Generic> Snapshot<T> getAllInheritings() {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return allInheritingsIterator();
 			}
 		};
 	}
 
 	public <T extends Generic> Snapshot<T> getAllInheritingsWithoutRoot() {
 		return new AbstractSnapshot<T>() {
 
 			@Override
 			public Iterator<T> iterator() {
 				return allInheritingsIteratorWithoutRoot();
 			}
 		};
 	}
 
 	protected <T extends Generic> Iterator<T> allInheritingsIterator() {
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 
 			private static final long serialVersionUID = 4540682035671625893L;
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return (((GenericImpl) node).directInheritingsIterator());
 			}
 		};
 	}
 
 	<T extends Generic> Iterator<T> allInheritingsIteratorWithoutRoot() {
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 
 			{
 				next();
 			}
 
 			private static final long serialVersionUID = 4540682035671625893L;
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return (((GenericImpl) node).directInheritingsIterator());
 			}
 		};
 	}
 
 	void mountConstraints(Class<?> clazz) {
 		if (clazz.getAnnotation(VirtualConstraint.class) != null)
 			enableVirtualConstraint();
 
 		if (clazz.getAnnotation(UniqueValueConstraint.class) != null)
 			enableUniqueValueConstraint();
 
 		InstanceValueClassConstraint instanceClass = clazz.getAnnotation(InstanceValueClassConstraint.class);
 		if (instanceClass != null)
 			setConstraintClass(instanceClass.value());
 
 		if (clazz.getAnnotation(PropertyConstraint.class) != null)
 			enablePropertyConstraint();
 
 		if (clazz.getAnnotation(SingletonConstraint.class) != null)
 			enableSingletonConstraint();
 
 		SingularConstraint singularTarget = clazz.getAnnotation(SingularConstraint.class);
 		if (singularTarget != null)
 			for (int axe : singularTarget.value())
 				enableSingularConstraint(axe);
 	}
 
 	@Override
 	public boolean isRemovable() {
 		return getCurrentCache().isRemovable(this);
 	}
 
 	/*********************************************/
 	/************** SYSTEM PROPERTY **************/
 	/*********************************************/
 
 	private <T extends Generic> Serializable getSystemPropertyValue(Class<T> constraintClass, int pos) {
 		return getSystemPropertiesMap().get(new AxedPropertyClass(constraintClass, pos));
 	}
 
 	public <T extends Generic> void setSystemPropertyValue(Class<T> constraintClass, int pos, Serializable value) {
 		getSystemPropertiesMap().put(new AxedPropertyClass(constraintClass, pos), value);
 	}
 
 	private <T extends Generic> boolean isSystemPropertyEnabled(Class<T> constraintClass, int pos) {
 		Serializable value = getSystemPropertyValue(constraintClass, pos);
 		return value != null && !Boolean.FALSE.equals(value);
 	}
 
 	public <T extends Generic> Serializable getConstraintValue(Class<T> constraintClass, int pos) {
 		return getConstraintsMap().get(new AxedPropertyClass(constraintClass, pos));
 	}
 
 	public <T extends Generic> void setConstraintValue(Class<T> constraintClass, int pos, Serializable value) {
 		getConstraintsMap().put(new AxedPropertyClass(constraintClass, pos), value);
 	}
 
 	public <T extends Generic> boolean isConstraintEnabled(Class<T> constraintClass, int pos) {
 		Serializable value = getConstraintValue(constraintClass, pos);
 		return null != value && !Boolean.FALSE.equals(value);
 	}
 
 	@Override
 	public <T extends Relation> T enableCascadeRemove(int basePos) {
 		setSystemPropertyValue(CascadeRemoveSystemProperty.class, basePos, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Relation> T disableCascadeRemove(int basePos) {
 		setSystemPropertyValue(CascadeRemoveSystemProperty.class, basePos, false);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isCascadeRemove(int basePos) {
 		return isSystemPropertyEnabled(CascadeRemoveSystemProperty.class, basePos);
 	}
 
 	@Override
 	public <T extends Generic> T enableReferentialIntegrity(int componentPos) {
 		setSystemPropertyValue(NoReferentialIntegritySystemProperty.class, componentPos, false);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Generic> T disableReferentialIntegrity(int componentPos) {
 		setSystemPropertyValue(NoReferentialIntegritySystemProperty.class, componentPos, true);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isReferentialIntegrity(int basePos) {
 		return !isSystemPropertyEnabled(NoReferentialIntegritySystemProperty.class, basePos);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularConstraint() {
 		return enableSingularConstraint(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularConstraint() {
 		return disableSingularConstraint(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public boolean isSingularConstraintEnabled() {
 		return isSingularConstraintEnabled(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularConstraint(int basePos) {
 		setConstraintValue(SingularConstraintImpl.class, basePos, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T disableSingularConstraint(int basePos) {
 		setConstraintValue(SingularConstraintImpl.class, basePos, false);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isSingularConstraintEnabled(int basePos) {
 		return isConstraintEnabled(SingularConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public <T extends Generic> T enableSizeConstraint(int basePos, Integer size) {
 		setConstraintValue(SizeConstraintImpl.class, basePos, size);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Generic> T disableSizeConstraint(int basePos) {
 		setConstraintValue(SizeConstraintImpl.class, basePos, false);
 		return (T) this;
 	}
 
 	@Override
 	public Integer getSizeConstraint(int basePos) {
 		Serializable result = getConstraintValue(SizeConstraintImpl.class, basePos);
 		return result instanceof Integer ? (Integer) result : null;
 	}
 
 	@Override
 	public Class<?> getConstraintClass() {
 		Serializable value = getConstraintValue(InstanceClassConstraintImpl.class, Statics.MULTIDIRECTIONAL);
 		return null == value ? Object.class : (Class<?>) value;
 	}
 
 	@Override
 	public <T extends Type> T setConstraintClass(Class<?> constraintClass) {
 		setConstraintValue(InstanceClassConstraintImpl.class, Statics.MULTIDIRECTIONAL, constraintClass);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T enablePropertyConstraint() {
 		setConstraintValue(PropertyConstraintImpl.class, Statics.MULTIDIRECTIONAL, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T disablePropertyConstraint() {
 		setConstraintValue(PropertyConstraintImpl.class, Statics.MULTIDIRECTIONAL, false);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isPropertyConstraintEnabled() {
 		return isConstraintEnabled(PropertyConstraintImpl.class, Statics.MULTIDIRECTIONAL);
 	}
 
 	@Override
 	public <T extends Type> T enableRequiredConstraint() {
 		return enableRequiredConstraint(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Type> T disableRequiredConstraint() {
 		return disableRequiredConstraint(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public boolean isRequiredConstraintEnabled() {
 		return isRequiredConstraintEnabled(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Type> T enableRequiredConstraint(int basePos) {
 		setConstraintValue(RequiredConstraintImpl.class, basePos, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T disableRequiredConstraint(int basePos) {
 		setConstraintValue(RequiredConstraintImpl.class, basePos, false);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isRequiredConstraintEnabled(int basePos) {
 		return isConstraintEnabled(RequiredConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public <T extends Type> T enableUniqueValueConstraint() {
 		setConstraintValue(UniqueValueConstraintImpl.class, Statics.MULTIDIRECTIONAL, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T disableUniqueValueConstraint() {
 		setConstraintValue(UniqueValueConstraintImpl.class, Statics.MULTIDIRECTIONAL, true);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isUniqueValueConstraintEnabled() {
 		return isConstraintEnabled(UniqueValueConstraintImpl.class, Statics.MULTIDIRECTIONAL);
 	}
 
 	@Override
 	public <T extends Type> T enableVirtualConstraint() {
 		setConstraintValue(VirtualConstraintImpl.class, Statics.MULTIDIRECTIONAL, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T disableVirtualConstraint() {
 		setConstraintValue(VirtualConstraintImpl.class, Statics.MULTIDIRECTIONAL, false);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isVirtualConstraintEnabled() {
 		return isConstraintEnabled(VirtualConstraintImpl.class, Statics.MULTIDIRECTIONAL);
 	}
 
 	@Override
 	public <T extends Type> T enableSingletonConstraint() {
 		setConstraintValue(SingletonConstraintImpl.class, Statics.MULTIDIRECTIONAL, true);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Type> T disableSingletonConstraint() {
 		setConstraintValue(SingletonConstraintImpl.class, Statics.MULTIDIRECTIONAL, false);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isSingletonConstraintEnabled() {
 		return isConstraintEnabled(SingletonConstraintImpl.class, Statics.MULTIDIRECTIONAL);
 	}
 
 	Generic[] nullToSelfComponent(Generic[] components) {
 		Generic[] result = components.clone();
 		for (int i = 0; i < result.length; i++)
 			if (result[i] == null)
 				result[i] = this;
 		return result;
 	}
 
 	Generic[] selfToNullComponents() {
 		Generic[] result = components.clone();
 		for (int i = 0; i < result.length; i++)
 			if (equals(result[i]))
 				result[i] = null;
 		return result;
 	}
 
 	public boolean equiv(HomeTreeNode homeTreeNode, Generic[] supers, Generic[] components) {
 		return getHomeTreeNode().equals(homeTreeNode) && Arrays.equals(this.supers, supers) && Arrays.equals(this.components, nullToSelfComponent(components));
 	}
 
 	public <T extends Generic> T reBind() {
 		return getCurrentCache().reBind(this);
 	}
 
 	boolean isPseudoStructural(int basePos) {
 		if (!isConcrete())
 			return false;
 		for (int i = 0; i < components.length; i++)
 			if (i != basePos && components[i].isStructural())
 				return true;
 		return false;
 	}
 
 	@Override
 	public <T extends Attribute> T addProperty(Serializable value, Generic... targets) {
 		return addAttribute(value, targets).enableSingularConstraint();
 	}
 
 	@Override
 	public <T extends Relation> T addRelation(Serializable value, Generic... targets) {
 		return addAttribute(value, targets);
 	}
 
 	@Override
 	public <T extends Link> T addLink(Link relation, Serializable value, Generic... targets) {
 		return addHolder(relation, value, targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, Serializable value, Generic... targets) {
 		return addHolder(attribute, getBasePos(attribute), value, targets);
 	}
 
 	@Override
 	public <T extends Holder> T addValue(Holder attribute, Serializable value) {
 		return addHolder(attribute, value);
 	}
 
 	@Override
 	public <Key extends Serializable, Value extends Serializable> AbstractExtendedMap<Key, Value> getMap(Class<? extends MapProvider> mapClass) {
 		return this.<Key, Value> getMap(getCurrentCache().<MapProvider> find(mapClass));
 	}
 
 	public <Key extends Serializable, Value extends Serializable> AbstractExtendedMap<Key, Value> getMap(MapProvider mapProvider) {
 		return (AbstractExtendedMap<Key, Value>) mapProvider.<Key, Value> getExtendedMap(this);
 	}
 
 	@Override
 	public <Key extends Serializable, Value extends Serializable> AbstractExtendedMap<Key, Value> getPropertiesMap() {
 		return getMap(PropertiesMapProvider.class);
 	}
 
 	public AbstractExtendedMap<AxedPropertyClass, Serializable> getConstraintsMap() {
 		return getMap(ConstraintsMapProvider.class);
 	}
 
 	public AbstractExtendedMap<AxedPropertyClass, Serializable> getSystemPropertiesMap() {
 		return getMap(SystemPropertiesMapProvider.class);
 	}
 
 	@Override
 	public boolean isTree() {
 		return this instanceof Tree;
 	}
 
 	@Override
 	public boolean isRoot() {
 		return (this instanceof Node) && equals(getBaseComponent());
 	}
 
 	@Override
 	public <T extends Generic> T addComponent(Generic newComponent, int pos) {
 		return getCurrentCache().addComponent(this, newComponent, pos);
 	}
 
 	@Override
 	public <T extends Generic> T removeComponent(Generic component, int pos) {
 		return getCurrentCache().removeComponent(this, pos);
 	}
 
 	@Override
 	public <T extends Generic> T addSuper(Generic newSuper) {
 		return getCurrentCache().addSuper(this, newSuper);
 	}
 
 	@Override
 	public <T extends Generic> T removeSuper(int pos) {
 		return getCurrentCache().removeSuper(this, pos);
 	}
 
 	@Override
 	public <T extends Generic> T setValue(Serializable value) {
 		return getCurrentCache().setValue(this, value);
 	}
 
 	public List<Integer> getComponentsPositions(Generic... components) {
 		return new ComponentsPositions(components);
 	}
 
 	private class ComponentsPositions extends ArrayList<Integer> {
 		private static final long serialVersionUID = 1715235949973772843L;
 
 		private ComponentsPositions(Generic[] components) {
 			for (int i = 0; i < components.length; i++)
 				add(getComponentPos(components[i]));
 		}
 
 		public int getComponentPos(Generic generic) {
 			int i;
 			for (i = 0; i < getComponentsSize(); i++)
 				if (!contains(i) && (generic == null ? GenericImpl.this.equals(getComponent(i)) : generic.inheritsFrom(getComponent(i)))) {
 					return i;
 				}
 			while (contains(i))
 				i++;
 			return i;
 		}
 	}
 
 	public Snapshot<Integer> getPositions(final Holder attribute) {
 		return new AbstractSnapshot<Integer>() {
 			@Override
 			public Iterator<Integer> iterator() {
 				return positionsIterator(attribute);
 			}
 		};
 	}
 
 	Iterator<Integer> positionsIterator(final Holder attribute) {
 		final Generic[] components = ((GenericImpl) attribute).getComponentsArray();
 		return new AbstractFilterIterator<Integer>(new CountIterator(components.length)) {
 			@Override
 			public boolean isSelected() {
 				return GenericImpl.this.inheritsFrom(components[next]);
 			}
 		};
 	}
 
 	@Override
 	public Snapshot<Generic> getOtherTargets(final Holder holder) {
 		return new AbstractSnapshot<Generic>() {
 			@Override
 			public Iterator<Generic> iterator() {
 				return otherTargetsIterator(holder);
 			}
 		};
 	}
 
 	public Iterator<Generic> otherTargetsIterator(final Holder holder) {
 		final Generic[] components = ((GenericImpl) holder).components;
 		return new AbstractProjectorAndFilterIterator<Integer, Generic>(new CountIterator(components.length)) {
 
 			@Override
 			public boolean isSelected() {
 				return !GenericImpl.this.equals(components[next]) && !GenericImpl.this.inheritsFrom(components[next]);
 			}
 
 			@Override
 			protected Generic project() {
 				return components[next];
 			}
 		};
 	}
 
 	public boolean isAutomatic() {
 		return getCurrentCache().isAutomatic(this);
 	}
 
 	public CacheImpl getCurrentCache() {
 		return getEngine().getCurrentCache();
 	}
 
 	@Override
 	public boolean isMapProvider() {
 		return this.getValue() instanceof Class && AbstractMapProvider.class.isAssignableFrom((Class<?>) this.getValue());
 	}
 
 	public <T> T unambigousFirst(Iterator<T> iterator) throws RollbackException {
 		if (!iterator.hasNext())
 			return null;
 		T result = iterator.next();
 		if (iterator.hasNext()) {
 			String message = "" + ((Generic) result).info();
 			while (iterator.hasNext())
 				message += " / " + ((Generic) iterator.next()).info();
 			this.getCurrentCache().rollback(new AmbiguousSelectionException("Ambigous selection : " + message));
 		}
 		return result;
 	}
 
 	Class<?> specializeInstanceClass(Class<?> specializationClass) {
 		InstanceGenericClass instanceClass = getClass().getAnnotation(InstanceGenericClass.class);
 		if (instanceClass != null)
 			if (specializationClass == null || specializationClass.isAssignableFrom(instanceClass.value()))
 				specializationClass = instanceClass.value();
 			else
 				assert instanceClass.value().isAssignableFrom(specializationClass);
 		return specializationClass;
 	}
 
 }
