 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.genericsystem.annotation.constraints.InstanceValueClassConstraint;
 import org.genericsystem.annotation.constraints.PropertyConstraint;
 import org.genericsystem.annotation.constraints.SingletonConstraint;
 import org.genericsystem.annotation.constraints.SingularConstraint;
 import org.genericsystem.annotation.constraints.UniqueValueConstraint;
 import org.genericsystem.annotation.constraints.VirtualConstraint;
 import org.genericsystem.core.EngineImpl.RootTreeNode;
 import org.genericsystem.core.Snapshot.Projector;
 import org.genericsystem.core.Statics.Primaries;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.MapProvider;
 import org.genericsystem.generic.Node;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Tree;
 import org.genericsystem.generic.Type;
 import org.genericsystem.iterator.AbstractFilterIterator;
 import org.genericsystem.iterator.AbstractPreTreeIterator;
 import org.genericsystem.iterator.AbstractProjectorAndFilterIterator;
 import org.genericsystem.iterator.AbstractSelectableLeafIterator;
 import org.genericsystem.iterator.ArrayIterator;
 import org.genericsystem.iterator.CartesianIterator;
 import org.genericsystem.iterator.CountIterator;
 import org.genericsystem.iterator.SingletonIterator;
 import org.genericsystem.map.AbstractMapProvider;
 import org.genericsystem.map.ConstraintsMapProvider;
 import org.genericsystem.map.PropertiesMapProvider;
 import org.genericsystem.map.SystemPropertiesMapProvider;
 import org.genericsystem.snapshot.AbstractSnapshot;
 import org.genericsystem.systemproperties.CascadeRemoveSystemProperty;
 import org.genericsystem.systemproperties.NoInheritanceSystemType;
 import org.genericsystem.systemproperties.NoReferentialIntegritySystemProperty;
 import org.genericsystem.systemproperties.constraints.axed.RequiredConstraintImpl;
 import org.genericsystem.systemproperties.constraints.axed.SingularConstraintImpl;
 import org.genericsystem.systemproperties.constraints.axed.SizeConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.InstanceClassConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.PropertyConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.SingletonConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.UniqueValueConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.VirtualConstraintImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SuppressWarnings("unchecked")
 /**
  * @author Nicolas Feybesse
  * @author Michael Ory
  */
 public class GenericImpl implements Generic, Type, Link, Relation, Holder, Attribute {
 
 	protected static Logger log = LoggerFactory.getLogger(GenericImpl.class);
 
 	private LifeManager lifeManager;
 
 	HomeTreeNode homeTreeNode;
 
 	public Generic[] supers;
 
 	Generic[] components;
 
 	HomeTreeNode[] primaries;
 
 	public Generic[] getSupersArray() {
 		return supers.clone();
 	}
 
 	public Generic[] getComponentsArray() {
 		return components.clone();
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
 
 	// final GenericImpl initializePrimary(HomeTreeNode homeTreeNode, Generic[] directSupers, Generic[] components) {
 	// return restore(homeTreeNode, null, Long.MAX_VALUE, 0L, Long.MAX_VALUE, directSupers, components);
 	// }
 
 	final GenericImpl initialize(HomeTreeNode homeTreeNode, Generic[] directSupers, Generic[] components) {
 		return restore(homeTreeNode, null, Long.MAX_VALUE, 0L, Long.MAX_VALUE, directSupers, components);
 	}
 
 	// private static void reorderImplicit(Generic implicit, Generic[] supers) {
 	// for (int index = 0; index < supers.length; index++)
 	// if (implicit.equals(supers[index].getImplicit()))
 	// if (index != 0) {
 	// switchFirst(index, supers);
 	// return;
 	// }
 	// }
 
 	// private static void switchFirst(int index, Generic[] supers) {
 	// Generic tmp = supers[index];
 	// supers[index] = supers[0];
 	// supers[0] = tmp;
 	// }
 
 	final GenericImpl restore(HomeTreeNode homeTreeNode, Long designTs, long birthTs, long lastReadTs, long deathTs, Generic[] supers, Generic[] components) {
 		assert homeTreeNode != null;
 		this.homeTreeNode = homeTreeNode;
 		this.supers = supers;
 		Arrays.sort(supers);
 		this.components = nullToSelfComponent(components);
 		lifeManager = new LifeManager(designTs == null ? getEngine().pickNewTs() : designTs, birthTs, lastReadTs, deathTs);
 		primaries = !isEngine() ? new Primaries(homeTreeNode, supers).toArray() : new HomeTreeNode[] { homeTreeNode };
 		assert primaries.length != 0;
 
 		for (Generic g1 : supers)
 			for (Generic g2 : supers)
 				if (!g1.equals(g2))
 					assert !g1.inheritsFrom(g2) : "" + Arrays.toString(supers);
 		assert getMetaLevel() == homeTreeNode.getMetaLevel() : getMetaLevel() + " " + homeTreeNode.getMetaLevel() + " " + (homeTreeNode instanceof RootTreeNode);
 		for (Generic superGeneric : supers)
 			assert !this.equals(superGeneric) || isEngine() : this;
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
 	public boolean isInstanceOf(Generic generic) {
 		return getMetaLevel() - generic.getMetaLevel() == 1 ? this.inheritsFrom(generic) : false;
 	}
 
 	// public boolean isPrimary() {
 	// return components.length == 0 && supers.length == 1;
 	// }
 
 	@Override
 	public <T extends Generic> T getMeta() {
 		HomeTreeNode metaNode = homeTreeNode.metaNode;
 		GenericImpl generic = this;
 		while (!metaNode.equals(homeTreeNode.metaNode))
 			for (Generic superGeneric : generic.supers)
 				if (generic.homeTreeNode.inheritsFrom(((GenericImpl) superGeneric).homeTreeNode))
 					generic = (GenericImpl) superGeneric;
 		return (T) generic;
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
 		Link holder = getHolder(Statics.CONCRETE, attribute);
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
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, Generic... targets) {
 		return setHolder(attribute, value, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, int basePos, Serializable value, Generic... targets) {
 		return bind(((GenericImpl) attribute).bindInstanceNode(value), null, attribute, basePos, true, targets);
 	}
 
 	public <T extends Holder> T bind(HomeTreeNode homeTreeNode, Class<?> specializationClass, Holder directSuper, int basePos, boolean existsException, Generic... targets) {
 		return getCurrentCache().bind(homeTreeNode, specializationClass, directSuper, existsException, Statics.insertIntoArray(this, targets, basePos));
 	}
 
 	public <T extends Holder> T find(HomeTreeNode homeTreeNode, Holder directSuper, int basePos, Generic... targets) {
 		return getCurrentCache().fastFindBySuper(homeTreeNode, new Primaries(homeTreeNode, directSuper).toArray(), directSuper, Statics.insertIntoArray(this, targets, basePos));
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
 	public <T extends Generic> T cancel(Holder attribute, boolean concrete, Generic... targets) {
 		return cancel(attribute, getBasePos(attribute), concrete, targets);
 	}
 
 	@Override
 	public <T extends Generic> T cancel(Holder attribute, int basePos, boolean concrete, Generic... targets) {
 		return bind(((GenericImpl) attribute.getMeta()).bindInstanceNode(null), null, attribute, basePos, false, Statics.truncate(basePos, ((GenericImpl) attribute).components));
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, boolean concrete, Generic... targets) {
 		cancelAll(attribute, getBasePos(attribute), concrete, targets);
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, int basePos, boolean concrete, Generic... targets) {
 		for (Holder holder : concrete ? getHolders((Attribute) attribute, basePos, targets) : getAttributes((Attribute) attribute)) {
 			if (this.equals(holder.getComponent(basePos))) {
 				holder.remove();
 				cancelAll(attribute, basePos, concrete, targets);
 			} else
 				cancel(holder, basePos, concrete);
 		}
 	}
 
 	@Override
 	public void clearAllConcrete(Holder attribute, Generic... targets) {
 		clearAllConcrete(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public void clearAllConcrete(Holder attribute, int basePos, Generic... targets) {
 		internalClearAll(attribute, basePos, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public void clearAllStructural(Holder attribute, Generic... targets) {
 		clearAllStructural(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public void clearAllStructural(Holder attribute, int basePos, Generic... targets) {
 		internalClearAll(attribute, basePos, Statics.STRUCTURAL, targets);
 	}
 
 	public void internalClearAll(Holder attribute, int basePos, int metaLevel, Generic... targets) {
 		Iterator<Holder> holders = this.<Holder> holdersIterator(attribute, metaLevel, basePos, true, targets);
 		while (holders.hasNext()) {
 			Holder holder = holders.next();
 			if (this.equals(holder.getComponent(basePos)))
 				holder.remove();
 		}
 	}
 
 	@Override
 	public void removeHolder(Holder holder) {
 		if (equals(holder.getBaseComponent()))
 			holder.remove();
 		else
 			cancel(holder, true);
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
 				return holdersIterator((Attribute) attribute, Statics.CONCRETE, basePos, false, targets);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Holder> Snapshot<T> getHolders(Holder attribute, boolean readPhantoms, Generic... targets) {
 		return getHolders(attribute, getBasePos(attribute), readPhantoms, targets);
 	}
 
 	@Override
 	public <T extends Holder> Snapshot<T> getHolders(final Holder attribute, final int basePos, final boolean readPhantoms, final Generic... targets) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return holdersIterator((Attribute) attribute, Statics.CONCRETE, basePos, readPhantoms, targets);
 			}
 		};
 	}
 
 	@Override
 	public void removePhantoms(Attribute attribute) {
 		Snapshot<Holder> holders = getHolders(attribute, true);
 		Iterator<Holder> iterator = holders.iterator();
 		while (iterator.hasNext()) {
 			Holder holder = iterator.next();
 			if (holder.getValue() == null)
 				holder.remove();
 		}
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
 		return new AbstractFilterIterator<T>(GenericImpl.this.<T> holdersIterator(relation, Statics.CONCRETE, basePos, false, targets)) {
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
 
 	public <T extends Holder> Iterator<T> holdersIterator(Holder attribute, int metaLevel, int basePos, boolean readPhantoms, Generic... targets) {
 		return this.<T> targetsFilter(GenericImpl.this.<T> holdersIterator(metaLevel, attribute, basePos, readPhantoms), attribute, targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(int metaLevel, Holder attribute, Generic... targets) {
 		return getHolder(metaLevel, attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(int metaLevel, Holder attribute, int basePos, Generic... targets) {
 		return Statics.unambigousFirst(this.<T> holdersIterator(attribute, metaLevel, basePos, false, targets));
 	}
 
 	public <T extends Holder> T getHolderByValue(int metaLevel, Holder attribute, Serializable value, final Generic... targets) {
 		return getHolderByValue(metaLevel, attribute, value, getBasePos(attribute), targets);
 	}
 
 	public <T extends Holder> T getHolderByValue(int metaLevel, Holder attribute, Serializable value, int basePos, final Generic... targets) {
 		return Statics.unambigousFirst(Statics.valueFilter(this.<T> holdersIterator(attribute, metaLevel, basePos, value == null, targets), value));
 	}
 
 	@Override
 	public <T extends Link> T getLink(Link relation, int basePos, Generic... targets) {
 		return Statics.unambigousFirst(this.<T> linksIterator(relation, basePos, targets));
 	}
 
 	@Override
 	public <T extends Link> T getLink(Link relation, Generic... targets) {
 		return Statics.unambigousFirst(this.<T> linksIterator(relation, getBasePos(relation), targets));
 	}
 
 	public <T extends Generic> Iterator<T> directInheritingsIterator() {
 		return getCurrentCache().directInheritingsIterator(this);
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
 
 	public <T extends Generic> Iterator<T> attributesIterator(boolean readPhantoms) {
 		return this.<T> holdersIterator(Statics.STRUCTURAL, getCurrentCache().getMetaAttribute(), Statics.MULTIDIRECTIONAL, readPhantoms);
 	}
 
 	@Override
 	public <T extends Attribute> Snapshot<T> getAttributes() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return attributesIterator(false);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Attribute> Snapshot<T> getAttributes(final Attribute attribute) {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return holdersIterator(Statics.STRUCTURAL, attribute, Statics.MULTIDIRECTIONAL, false);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(final Serializable value, Generic... targets) {
 		return getAttribute(getCurrentCache().getMetaAttribute(), value, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(Attribute attribute, final Serializable value, Generic... targets) {
 		return Statics.unambigousFirst(this.targetsFilter(Statics.valueFilter(this.<T> holdersIterator(Statics.STRUCTURAL, attribute, Statics.MULTIDIRECTIONAL, value == null), value), attribute, targets));
 	}
 
 	private <T extends Relation> Iterator<T> relationsIterator(boolean readPhantom) {
 		return new AbstractFilterIterator<T>(GenericImpl.this.<T> attributesIterator(readPhantom)) {
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
 		return Statics.unambigousFirst(Statics.valueFilter(this.<T> relationsIterator(value == null), value));
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
 		return setHolder(null, getEngine(), value, Statics.STRUCTURAL, Statics.BASE_POSITION, false, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T addAttribute(Serializable value, Generic... targets) {
 		return setHolder(null, getEngine(), value, Statics.STRUCTURAL, Statics.BASE_POSITION, true, targets);
 	}
 
 	public <T extends Relation> T setSubAttribute(Attribute attribute, Serializable value, Generic... targets) {
 		T holder = setHolder(null, attribute, value, Statics.STRUCTURAL, getBasePos(attribute), false, targets);
 		assert holder.inheritsFrom(attribute) : holder.info() + attribute.info();
 		return holder;
 	}
 
 	public <T extends Relation> T addSubAttribute(Attribute attribute, Serializable value, Generic... targets) {
 		T holder = setHolder(null, attribute, value, Statics.STRUCTURAL, getBasePos(attribute), true, targets);
 		assert holder.inheritsFrom(attribute) : holder.info();
 		return holder;
 	}
 
 	public <T extends Holder> T setHolder(Class<?> specializationClass, Holder attribute, Serializable value, int metaLevel, int basePos, boolean existsException, Generic... targets) {
 		// assert attribute.getMetaLevel() >= metaLevel;
 		Generic meta = metaLevel == attribute.getMetaLevel() ? attribute.getMeta() : attribute;
 		T holder = getSelectedHolder(attribute, value, metaLevel, basePos, targets);
 		if (holder == null)
 			return value == null ? null : this.<T> bind(((GenericImpl) meta).bindInstanceNode(value), specializationClass, attribute, basePos, existsException, targets);
 		HomeTreeNode homeTreeNode = ((GenericImpl) meta).findInstanceNode(value);
 		if (!equals(holder.getComponent(basePos))) {
 			if (value == null)
 				return cancel(holder, basePos, true);
 			if (homeTreeNode != null && !(((GenericImpl) holder).equiv(homeTreeNode, new Primaries(homeTreeNode, attribute).toArray(), Statics.insertIntoArray(holder.getComponent(basePos), targets, basePos))))
 				cancel(holder, basePos, true);
 			return this.<T> bind(((GenericImpl) meta).bindInstanceNode(value), specializationClass, attribute, basePos, existsException, targets);
 		}
 		if (homeTreeNode != null) {
 			if (((GenericImpl) holder).equiv(homeTreeNode, new Primaries(homeTreeNode, attribute).toArray(), Statics.insertIntoArray(this, targets, basePos)))
 				return holder;
 			if (value != null && Arrays.equals(((GenericImpl) holder).components, Statics.insertIntoArray(this, targets, basePos))) {
 				log.info("999999999999999" + holder.info() + attribute.info() + "     " + value);
 				return holder.updateValue(value);
 			}
 		}
		holder.remove();
 		return this.<T> setHolder(specializationClass, attribute, value, metaLevel, basePos, existsException, targets);
 	}
 
 	public <T extends Holder> T setHolder(Class<?> specializationClass, Holder attribute, Serializable value, int basePos, Generic... targets) {
 		return this.<T> setHolder(specializationClass, attribute, value, Statics.CONCRETE, basePos, false, targets);
 	}
 
 	public <T extends Holder> T getSelectedHolder(Holder attribute, Serializable value, int metaLevel, int basePos, Generic... targets) {
 		if (((Attribute) attribute).isSingularConstraintEnabled(basePos))
 			return getHolder(metaLevel, (Attribute) attribute, basePos);
 		if (value == null || ((Type) attribute).isPropertyConstraintEnabled())
 			return this.<T> getHolder(metaLevel, attribute, basePos, targets);
 		return this.<T> getHolderByValue(metaLevel, attribute, value, basePos, targets);
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
 		return getCurrentCache().bind(bindInstanceNode(value), null, this, false, components);
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Generic... components) {
 		return getCurrentCache().bind(getEngine().bindInstanceNode(value), null, this, false, components);
 	}
 
 	@Override
 	public <T extends Link> T bind(Link relation, Generic... targets) {
 		return flag(relation, targets);
 	}
 
 	private class TakenPositions extends ArrayList<Integer> {
 
 		private static final long serialVersionUID = 1777313486204962418L;
 		private int max;
 
 		public TakenPositions(int max) {
 			this.max = max;
 		}
 
 		public int getFreePosition(Generic component) {
 			int freePosition = 0;
 			while (contains(freePosition) || (freePosition < components.length && !component.inheritsFrom(components[freePosition])))
 				freePosition++;
 			if (freePosition >= max)
 				throw new IllegalStateException("Unable to find a valid position for : " + component + " " + Arrays.toString(components) + " " + max);
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
 
 	// Generic[] sortAndCheck(Generic... components) {
 	// if (getComponentsSize() != components.length)
 	// throw new IllegalStateException("Illegal components size : " + components.length);
 	// List<Integer> positions = getComponentsPositions(components);
 	// Generic[] orderedComponents = new Generic[components.length];
 	// for (int i = 0; i < components.length; i++) {
 	// int pos = positions.get(i);
 	// if (pos < 0 || pos >= components.length)
 	// throw new IllegalStateException("Unable to find a valid position for : " + components[i]);
 	// orderedComponents[pos] = components[i];
 	// }
 	// return orderedComponents;
 	// }
 
 	public <T extends Generic> Iterator<T> holdersIterator(final int level, Holder origin, int basePos, boolean readPhantom) {
 		if (Statics.STRUCTURAL == level)
 			basePos = Statics.MULTIDIRECTIONAL;
 		Iterator<T> iterator = origin.inheritsFrom(getEngine().getCurrentCache().find(NoInheritanceSystemType.class)) ? this.<T> noInheritanceIterator(level, basePos, origin) : this.<T> inheritanceIterator(level, origin, basePos);
 		return !readPhantom ? Statics.<T> nullFilter(iterator) : iterator;
 	}
 
 	private <T extends Generic> Iterator<T> noInheritanceIterator(final int metaLevel, int pos, final Generic origin) {
 		return new AbstractFilterIterator<T>(Statics.MULTIDIRECTIONAL == pos ? this.<T> compositesIterator() : this.<T> compositesIterator(pos)) {
 			@Override
 			public boolean isSelected() {
 				return next.getMetaLevel() == metaLevel && next.inheritsFrom(origin);
 			}
 		};
 	}
 
 	// private Iterator<Generic> allSupersIterator() {
 	// return new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 	//
 	// private static final long serialVersionUID = -6254209580316166416L;
 	//
 	// @Override
 	// public Iterator<Generic> children(Generic node) {
 	// return new ArrayIterator<Generic>(((GenericImpl) node).supers);
 	// }
 	// };
 	// }
 	//
 	// private <T extends Generic> Iterator<T> mainIterator2(final Generic origin, final int level, final int pos, final boolean readPhantom) {
 	// return new AbstractFilterIterator<T>(new AbstractConcateIterator<Generic, T>(allSupersIterator()) {
 	// @Override
 	// protected Iterator<T> getIterator(Generic superGeneric) {
 	// return ((GenericImpl) superGeneric).noInheritanceIterator(pos, level, origin);
 	// }
 	// }) {
 	//
 	// private Set<T> alreadyComputed = new HashSet<>();
 	//
 	// @Override
 	// public boolean isSelected() {
 	// return isNewLeaf(next) && (readPhantom || next.getValue() != null); // test phantom after add
 	// }
 	//
 	// private boolean isNewLeaf(Generic candidate) {
 	// for (Generic generic : alreadyComputed)
 	// if (generic.inheritsFrom(candidate))
 	// return false;
 	// return true;
 	// }
 	//
 	// };
 	// }
 
 	public <T extends Generic> Iterator<T> inheritanceIterator(final int level, final Generic origin, final int pos) {
 		return (Iterator<T>) new AbstractSelectableLeafIterator(origin) {
 
 			@Override
 			public boolean isSelectable() {
 				return level == next.getMetaLevel();
 			}
 
 			@Override
 			public final boolean isSelected(Generic candidate) {
 				boolean selected = candidate.getMetaLevel() <= level && (pos != Statics.MULTIDIRECTIONAL ? ((GenericImpl) candidate).isAttributeOf(GenericImpl.this, pos) : ((GenericImpl) candidate).isAttributeOf(GenericImpl.this));
 				if (pos != Statics.MULTIDIRECTIONAL && selected && ((GenericImpl) candidate).isPseudoStructural(pos)) {
 					((GenericImpl) candidate).project(pos);
 				}
 				return selected;
 			}
 		};
 	}
 
 	private void project(final int pos) {
 		Iterator<Object[]> cartesianIterator = new CartesianIterator(projections(pos));
 		while (cartesianIterator.hasNext()) {
 			Generic[] components = (Generic[]) cartesianIterator.next();
 			if (!findPhantom(components))
 				getCurrentCache().bind(getHomeTreeNode(), null, this, false, components);
 		}
 	}
 
 	private Iterable<Generic>[] projections(final int pos) {
 		final Iterable<Generic>[] projections = new Iterable[components.length];
 		for (int i = 0; i < components.length; i++) {
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
 
 	private boolean findPhantom(Generic[] components) {
 		HomeTreeNode phantom = homeTreeNode.metaNode.findInstanceNode(null);
 		return phantom != null && getCurrentCache().fastFindBySuper(phantom, new Primaries(phantom, supers).toArray(), supers[0], components) != null;
 	}
 
 	@Override
 	// TODO KK
 	public boolean inheritsFrom(Generic generic) {
 		if (generic == null)
 			return false;
 		if (getDesignTs() < ((GenericImpl) generic).getDesignTs())
 			return false;
 		boolean inheritance = ((GenericImpl) generic).new InheritanceCalculator().isSuperOf(this);
 		boolean superOf = ((GenericImpl) generic).isSuperOf(this);
 		assert inheritance == superOf : "" + this.info() + generic.info() + " : " + inheritance + " != " + superOf;
 		return inheritance;
 	}
 
 	private class InheritanceCalculator extends HashSet<Generic> {
 		private static final long serialVersionUID = -894665449193645526L;
 
 		public boolean isSuperOf(Generic subGeneric) {
 			if (GenericImpl.this.equals(subGeneric))
 				return true;
 			for (Generic directSuper : ((GenericImpl) subGeneric).supers)
 				if (add(directSuper) && isSuperOf(directSuper))
 					return true;
 			return false;
 		}
 	}
 
 	@Override
 	public boolean inheritsFromAll(Generic... generics) {
 		for (Generic generic : generics)
 			if (!inheritsFrom(generic))
 				return false;
 		return true;
 	}
 
 	public boolean isSuperOf(Generic generic) {
 		assert generic != null;
 		if (equals(generic))
 			return true;
 		if (((GenericImpl) generic).isEngine())
 			return isEngine();
 		return isSuperOf(primaries, components, ((GenericImpl) generic).primaries, ((GenericImpl) generic).components);
 	}
 
 	// static int i = 0;
 
 	public static boolean isSuperOf(HomeTreeNode[] primaries, Generic[] components, final HomeTreeNode[] subPrimaries, Generic[] subComponents) {
 
 		if (primaries.length == subPrimaries.length && components.length == subComponents.length) {
 			for (int i = 0; i < subPrimaries.length; i++) {
 				if (!subPrimaries[i].inheritsFrom(primaries[i]))
 					return false;
 			}
 			for (int i = 0; i < subComponents.length; i++) {
 				if (components[i] != null && subComponents[i] != null) {
 					if (!Arrays.equals(primaries, ((GenericImpl) components[i]).primaries) || !Arrays.equals(components, ((GenericImpl) components[i]).components) || !Arrays.equals(subPrimaries, ((GenericImpl) subComponents[i]).primaries)
 							|| !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 						if (!((GenericImpl) components[i]).isSuperOf(subComponents[i]))
 							return false;
 				}
 				if (components[i] == null) {
 					if (!Arrays.equals(subPrimaries, ((GenericImpl) subComponents[i]).primaries) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 						return false;
 				} else if (subComponents[i] == null)
 					if (!components[i].isEngine() && (!Arrays.equals(primaries, ((GenericImpl) components[i]).primaries) || !Arrays.equals(components, ((GenericImpl) components[i]).components)))
 						return false;
 			}
 			// log.info("BBBBBBB");
 			return true;
 		}
 		// log.info("YYY");
 		if (primaries.length < subPrimaries.length)
 			for (int i = 0; i < subPrimaries.length; i++)
 				if (isSuperOf(primaries, components, Statics.truncate(i, subPrimaries), subComponents))
 					return true;
 		if (components.length < subComponents.length)
 			for (int i = 0; i < subComponents.length; i++)
 				if (isSuperOf(primaries, components, subPrimaries, Statics.truncate(i, subComponents)))
 					return true;
 		// log.info("ZZZ");
 		return false;
 	}
 
 	@Override
 	public void remove() {
 		getCurrentCache().removeWithAutomatics(this);
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
 		s += "toString    : " + this + "\n";
 		s += "meta        : " + getMeta() + "\n";
 		s += "value       : " + getValue() + "\n";
 		s += "metaLevel   : " + getMetaLevel() + "\n";
 		s += "**********************************************************************\n";
 		s += "design date : " + new SimpleDateFormat(Statics.PATTERN).format(new Date(getDesignTs() / Statics.MILLI_TO_NANOSECONDS)) + "\n";
 		s += "birth date  : " + new SimpleDateFormat(Statics.PATTERN).format(new Date(getBirthTs() / Statics.MILLI_TO_NANOSECONDS)) + "\n";
 		s += "death date  : " + new SimpleDateFormat(Statics.PATTERN).format(new Date(getDeathTs() / Statics.MILLI_TO_NANOSECONDS)) + "\n";
 		s += "**********************************************************************\n";
 		for (HomeTreeNode primary : primaries)
 			s += "primary     : " + primary + " (" + System.identityHashCode(primary) + ")\n";
 		for (Generic component : components)
 			s += "component   : " + component + " (" + System.identityHashCode(component) + ")\n";
 		for (Generic superGeneric : supers)
 			s += "super       : " + superGeneric + " (" + System.identityHashCode(superGeneric) + ")\n";
 		s += "**********************************************************************\n";
 		// for (Attribute attribute : getAttributes())
 		// if (!(attribute.getValue() instanceof Class) /* || !Constraint.class.isAssignableFrom((Class<?>) attribute.getValue()) */) {
 		// s += "attribute : " + attribute + "\n";
 		// for (Holder holder : getHolders(attribute))
 		// s += "                          ----------> holder : " + holder + "\n";
 		// }
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
 
 	public String getCategoryString() {
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
 			throw new IllegalStateException();
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
 		return Statics.unambigousFirst(Statics.<T> valueFilter(GenericImpl.this.<T> instancesIterator(), value));
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
 	public <T extends Generic> T getSubType(final Serializable value) {
 		return Statics.<T> unambigousFirst(Statics.<T> valueFilter(this.<T> allSubTypesIteratorWithoutRoot(), value));
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
 
 	private <T extends Generic> Iterator<T> allInheritingsIterator() {
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 
 			private static final long serialVersionUID = 4540682035671625893L;
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return (((GenericImpl) node).directInheritingsIterator());
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> allInheritingsIteratorWithoutRoot() {
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
 		return null != value && !Boolean.FALSE.equals(value);
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
 		setConstraintValue(SizeConstraintImpl.class, basePos, Statics.MULTIDIRECTIONAL);
 		return (T) this;
 	}
 
 	@Override
 	public Integer getSizeConstraint(int basePos) {
 		return (Integer) getConstraintValue(SizeConstraintImpl.class, basePos);
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
 
 	public boolean equiv(HomeTreeNode homeTreeNode, HomeTreeNode[] primaries, Generic[] components) {
 		return this.homeTreeNode.equals(homeTreeNode) && Arrays.equals(this.primaries, primaries) && Arrays.equals(this.components, nullToSelfComponent(components));
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
 	public <T extends MapProvider> ExtendedMap<Serializable, Serializable> getMap(Class<T> mapClass) {
 		return getMap(getCurrentCache().<MapProvider> find(mapClass));
 	}
 
 	public <T extends MapProvider> ExtendedMap<Serializable, Serializable> getMap(MapProvider mapProvider) {
 		return mapProvider.getExtendedMap(this);
 	}
 
 	@Override
 	public Map<Serializable, Serializable> getPropertiesMap() {
 		return getMap(PropertiesMapProvider.class);
 	}
 
 	@Override
 	public ExtendedMap<Serializable, Serializable> getConstraintsMap() {
 		return getMap(ConstraintsMapProvider.class);
 	}
 
 	@Override
 	public ExtendedMap<Serializable, Serializable> getSystemPropertiesMap() {
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
 	public <T extends Generic> T addComponent(int pos, Generic newComponent) {
 		return getCurrentCache().addComponent(this, newComponent, pos);
 	}
 
 	@Override
 	public <T extends Generic> T removeComponent(int pos, Generic newComponent) {
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
 	public <T extends Generic> T updateValue(Serializable value) {
 		return getCurrentCache().updateValue(this, value);
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
 
 	public CacheImpl getCurrentCache() {
 		return getEngine().getCurrentCache();
 	}
 
 	@Override
 	public boolean isMapProvider() {
 		return this.getValue() instanceof Class && AbstractMapProvider.class.isAssignableFrom((Class<?>) this.getValue());
 	}
 
 }
