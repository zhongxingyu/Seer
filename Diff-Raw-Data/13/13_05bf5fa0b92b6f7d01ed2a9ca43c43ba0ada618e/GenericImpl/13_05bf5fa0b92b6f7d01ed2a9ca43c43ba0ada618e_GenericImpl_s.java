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
 import java.util.Objects;
 import java.util.Set;
 
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.constraints.InheritanceDisabled;
 import org.genericsystem.annotation.constraints.InstanceValueClassConstraint;
 import org.genericsystem.annotation.constraints.PropertyConstraint;
 import org.genericsystem.annotation.constraints.SingularConstraint;
 import org.genericsystem.annotation.constraints.SingularInstanceConstraint;
 import org.genericsystem.annotation.constraints.UniqueConstraint;
 import org.genericsystem.annotation.constraints.VirtualConstraint;
 import org.genericsystem.core.Snapshot.Projector;
 import org.genericsystem.core.Statics.Primaries;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.MapProvider;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Tree;
 import org.genericsystem.generic.Type;
 import org.genericsystem.iterator.AbstractConcateIterator;
 import org.genericsystem.iterator.AbstractConcateIterator.ConcateIterator;
 import org.genericsystem.iterator.AbstractFilterIterator;
 import org.genericsystem.iterator.AbstractPreTreeIterator;
 import org.genericsystem.iterator.AbstractProjectionIterator;
 import org.genericsystem.iterator.AbstractProjectorAndFilterIterator;
 import org.genericsystem.iterator.AbstractSelectableLeafIterator;
 import org.genericsystem.iterator.ArrayIterator;
 import org.genericsystem.iterator.CartesianIterator;
 import org.genericsystem.iterator.CountIterator;
 import org.genericsystem.iterator.SingletonIterator;
 import org.genericsystem.map.PropertiesMapProvider;
 import org.genericsystem.snapshot.AbstractSnapshot;
 import org.genericsystem.systemproperties.BooleanSystemProperty;
 import org.genericsystem.systemproperties.CascadeRemoveSystemProperty;
 import org.genericsystem.systemproperties.MultiDirectionalSystemProperty;
 import org.genericsystem.systemproperties.NoInheritanceSystemProperty;
 import org.genericsystem.systemproperties.ReferentialIntegritySystemProperty;
 import org.genericsystem.systemproperties.constraints.InstanceClassConstraintImpl;
 import org.genericsystem.systemproperties.constraints.axed.RequiredConstraintImpl;
 import org.genericsystem.systemproperties.constraints.axed.SingularConstraintImpl;
 import org.genericsystem.systemproperties.constraints.axed.SizeConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.PropertyConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.SingularInstanceConstraintImpl;
 import org.genericsystem.systemproperties.constraints.simple.UniqueConstraintImpl;
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
 
 	Generic[] supers;
 
 	Generic[] components;
 
 	private Serializable value;
 
 	private boolean automatic;
 
 	public Generic[] getSupersArray() {
 		return supers.clone();
 	}
 
 	public Generic[] getComponentsArray() {
 		return components.clone();
 	}
 
 	@Override
 	public boolean isAutomatic() {
 		return automatic;
 	}
 
 	final GenericImpl initializePrimary(Serializable value, int metaLevel, Generic[] directSupers, Generic[] components, boolean automatic) {
 		return restore(value, metaLevel, null, Long.MAX_VALUE, 0L, Long.MAX_VALUE, directSupers, components, automatic);
 	}
 
 	final GenericImpl initializeComplex(Generic implicit, Generic[] directSupers, Generic[] components, boolean automatic) {
 		assert ((GenericImpl) implicit).isPrimary();
 		reorderImplicit(implicit, directSupers);
 		return restore(implicit.getValue(), implicit.getMetaLevel(), null, Long.MAX_VALUE, 0L, Long.MAX_VALUE, directSupers, components, automatic);
 	}
 
 	private static void reorderImplicit(Generic implicit, Generic[] supers) {
 		for (int index = 0; index < supers.length; index++)
 			if (implicit.equals(supers[index].getImplicit()))
 				if (index != 0) {
 					switchFirst(index, supers);
 					return;
 				}
 	}
 
 	private static void switchFirst(int index, Generic[] supers) {
 		Generic tmp = supers[index];
 		supers[index] = supers[0];
 		supers[0] = tmp;
 	}
 
 	final GenericImpl restore(Serializable value, int metaLevel, Long designTs, long birthTs, long lastReadTs, long deathTs, Generic[] directSupers, Generic[] components, boolean automatic) {
 		this.value = value;
 		supers = directSupers;
 		this.components = nullToSelfComponent(components);
 		this.automatic = automatic;
 
 		lifeManager = new LifeManager(designTs == null ? getEngine().pickNewTs() : designTs, birthTs, lastReadTs, deathTs);
 		for (Generic g1 : directSupers)
 			for (Generic g2 : directSupers)
 				if (!g1.equals(g2))
 					assert !g1.inheritsFrom(g2) : "" + Arrays.toString(directSupers);
 
 		if (getMetaLevel() != metaLevel)
 			throw new IllegalStateException(info() + " : META LEVEL ERROR getMetaLevel() " + getMetaLevel() + " / metaLevel " + metaLevel);
 		if (!isPrimary())
 			assert Objects.equals(directSupers[0].getValue(), value);
 		if (value != null)
 			for (Generic primary : getPrimaries())
 				assert primary.getValue() != null : this.info();
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
 	public <T extends Generic> T getImplicit() {
 		return isPrimary() ? (T) this : supers[0].<T> getImplicit();
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
 
 	@Override
 	public int getMetaLevel() {
 		return isPrimary() ? supers[0].getMetaLevel() + 1 : getImplicit().getMetaLevel();
 	}
 
 	@Override
 	public boolean isConcrete() {
 		return SystemGeneric.CONCRETE == getMetaLevel();
 	}
 
 	@Override
 	public boolean isStructural() {
 		return SystemGeneric.STRUCTURAL == getMetaLevel();
 	}
 
 	@Override
 	public boolean isMeta() {
 		return SystemGeneric.META == getMetaLevel();
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
 	public <S extends Serializable> S getValue() {
 		return (S) value;
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
 		assert value == null || getValues(attribute).contains(value) : holder;
 		return holder;
 	}
 
 	public <T extends Generic> T bindPrimary(Class<?> specializeGeneric, Serializable value, int metaLevel, boolean automatic) {
 		return getCurrentCache().bindPrimaryByValue(isConcrete() ? this.<GenericImpl> getImplicit().supers[0] : getImplicit(), value, metaLevel, automatic, specializeGeneric);
 	}
 
 	@Override
 	public <T extends Link> T setLink(Link relation, Serializable value, Generic... targets) {
 		return setHolder(relation, value, targets);
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, Generic... targets) {
 		return setHolder(attribute, value, getBasePos(attribute), targets);
 	}
 
 	private <T extends Holder> T getSelectedHolder(Holder attribute, Serializable value, int basePos, Generic... targets) {
 		T holder;
 		if (((Relation) attribute).isSingularConstraintEnabled(basePos))
 			holder = getHolder((Attribute) attribute, basePos);
 		else if (value == null || ((Type) attribute).isPropertyConstraintEnabled())
 			holder = getHolder(attribute, basePos, targets);
 		else
 			holder = getHolderByValue(attribute, value, basePos, targets);
 		return holder;
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, int basePos, Generic... targets) {
 		T holder = getSelectedHolder(attribute, value, basePos, targets);
 		Generic implicit = ((GenericImpl) attribute).bindPrimary(getClass(), value, SystemGeneric.CONCRETE, true);
 		if (holder == null)
 			return null != value ? this.<T> bind(implicit, attribute, basePos, true, targets) : null;
 		if (!equals(holder.getComponent(basePos))) {
 			if (value == null)
 				return cancel(holder, basePos, true);
 			if (!(((GenericImpl) holder).equiv(new Primaries(implicit, attribute).toArray(), Statics.insertIntoArray(holder.getComponent(basePos), targets, basePos))))
 				cancel(holder, basePos, true);
 			return this.<T> bind(implicit, attribute, basePos, true, targets);
 		}
 		if (((GenericImpl) holder).equiv(new Primaries(implicit, attribute).toArray(), Statics.insertIntoArray(this, targets, basePos)))
 			return holder;
 		holder.remove();
 		return this.<T> setHolder(attribute, value, basePos, targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, int basePos, Serializable value, Generic... targets) {
 		if (value == null)
 			return null;
 		Generic implicit = ((GenericImpl) attribute).bindPrimary(getClass(), value, SystemGeneric.CONCRETE, true);
 		return bind(implicit, attribute, basePos, true, targets);
 
 	}
 
 	protected <T extends Holder> T bind(Generic implicit, Holder directSuper, int basePos, boolean existsException, Generic... targets) {
 		return getCurrentCache().bind(implicit, false, directSuper, existsException, Statics.insertIntoArray(this, targets, basePos));
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
 		Generic implicit = concrete ? ((GenericImpl) attribute.getMeta()).bindPrimary(getClass(), null, SystemGeneric.CONCRETE, true) : getEngine().bindPrimary(getClass(), null, SystemGeneric.STRUCTURAL, true);
 		return bind(implicit, attribute, basePos, false, Statics.truncate(basePos, ((GenericImpl) attribute).components));
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
 		internalClearAll(attribute, basePos, true, targets);
 	}
 
 	@Override
 	public void clearAllStructural(Holder attribute, Generic... targets) {
 		clearAllStructural(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public void clearAllStructural(Holder attribute, int basePos, Generic... targets) {
 		internalClearAll(attribute, basePos, false, targets);
 	}
 
 	public void internalClearAll(Holder attribute, int basePos, boolean isConcrete, Generic... targets) {
 		Iterator<Holder> holders = isConcrete ? holdersIterator(attribute, basePos, true, targets) : this.<Holder> attributesIterator((Attribute) attribute, true);
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
 
 	public <T extends Holder> T getHolderByValue(Holder attribute, Serializable value, final Generic... targets) {
 		return getHolderByValue(attribute, value, getBasePos(attribute), targets);
 	}
 
 	public <T extends Holder> T getHolderByValue(Holder attribute, Serializable value, int basePos, final Generic... targets) {
 		return Statics.unambigousFirst(Statics.valueFilter(this.<T> holdersIterator(attribute, basePos, value == null, targets), value));
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
 				return holdersIterator((Attribute) attribute, basePos, false, targets);
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
 				return holdersIterator((Attribute) attribute, basePos, readPhantoms, targets);
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
 		return new AbstractFilterIterator<T>(GenericImpl.this.<T> holdersIterator(relation, basePos, false, targets)) {
 			@Override
 			public boolean isSelected() {
 				return next.isConcrete() && next.isRelation();
 			}
 		};
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getTargets(Relation relation) {
 		return getTargets(relation, Statics.TARGET_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getTargets(Relation relation, final int targetPos) {
 		return getLinks(relation).project(new Projector<T, Link>() {
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
 
 	public <T extends Holder> Iterator<T> holdersIterator(Holder attribute, int basePos, boolean readPhantoms, Generic... targets) {
 		return this.<T> targetsFilter(GenericImpl.this.<T> holdersIterator(attribute, basePos, readPhantoms), attribute, targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(Holder attribute, Generic... targets) {
 		return getHolder(attribute, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T getHolder(Holder attribute, int basePos, Generic... targets) {
 		return Statics.unambigousFirst(this.<T> holdersIterator(attribute, basePos, false, targets));
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
 		return this.<T> attributesIterator(getCurrentCache().getMetaAttribute(), readPhantoms);
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
 				return attributesIterator(attribute, false);
 			}
 		};
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(final Serializable value, Generic... targets) {
 		return getAttribute(getCurrentCache().getMetaAttribute(), value, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T getAttribute(Attribute attribute, final Serializable value, Generic... targets) {
 		return Statics.unambigousFirst(this.targetsFilter(Statics.valueFilter(this.<T> attributesIterator(attribute, value == null), value), attribute, targets));
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
 
 	@Override
 	public <T extends Relation> T getRelation(final Serializable value) {
 		return Statics.unambigousFirst(Statics.valueFilter(this.<T> relationsIterator(value == null), value));
 	}
 
 	public <T extends Generic> T reFind() {
 		return getCurrentCache().reFind(this);
 	}
 
 	@Override
 	public <T extends Attribute> T getProperty(Serializable value) {
 		return getAttribute(value);
 	}
 
 	@Override
 	public <T extends Attribute> T setAttribute(Serializable value) {
 		return setSubAttribute(getEngine(), value);
 	}
 
 	@Override
 	public <T extends Attribute> T setProperty(Serializable value, Type... targets) {
 		return setSubProperty(getEngine(), value, targets);
 	}
 
 	public <T extends Attribute> T setSubProperty(Attribute property, Serializable value, Type... targets) {
 		return setSubAttribute(property, value, targets).enablePropertyConstraint();
 	}
 
 	@Override
 	public <T extends Relation> T setRelation(Serializable value, Type... targets) {
 		return setSubRelation(getEngine(), value, targets);
 	}
 
 	private <T extends Attribute> T addSubRelation(Relation relation, Serializable value, Type... targets) {
 		return addSubAttribute(relation, value, targets);
 	}
 
 	private <T extends Attribute> T setSubRelation(Relation relation, Serializable value, Type... targets) {
 		return setSubAttribute(relation, value, targets);
 	}
 
 	public <T extends Relation> T setSubAttribute(Attribute attribute, Serializable value, Type... targets) {
 		return bind(getEngine().bindPrimary(Generic.class, value, SystemGeneric.STRUCTURAL, true), attribute, getBasePos(attribute), false, targets);
 	}
 
 	public <T extends Relation> T addSubAttribute(Attribute attribute, Serializable value, Type... targets) {
 		return bind(getEngine().bindPrimary(Generic.class, value, SystemGeneric.STRUCTURAL, true), attribute, getBasePos(attribute), true, targets);
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
 		return getCurrentCache().bind(bindPrimary(getClass(), value, getMetaLevel() + 1, !isPrimary()), false, this, false, components);
 	}
 
 	@Override
 	public <T extends Type> T newSubType(Serializable value, Generic... components) {
 		Generic implicit = getEngine().bindPrimary(Generic.class, value, SystemGeneric.STRUCTURAL, !isEngine() || components.length != 0);
 		return getCurrentCache().bind(implicit, false, this, false, components);
 	}
 
 	@Override
 	public <T extends Link> T bind(Link relation, Generic... targets) {
 		return flag(relation, targets);
 	}
 
 	Generic[] sortAndCheck(Generic... components) {
 		if (getComponentsSize() != components.length)
 			throw new IllegalStateException("Illegal components size");
 		List<Integer> positions = getComponentsPositions(components);
 		Generic[] orderedComponents = new Generic[components.length];
 		for (int i = 0; i < components.length; i++) {
 			int pos = positions.get(i);
 			if (pos < 0 || pos >= components.length)
 				throw new IllegalStateException("Unable to find a valid position for : " + components[i]);
 			orderedComponents[pos] = components[i];
 		}
 		return orderedComponents;
 	}
 
 	public <T extends Generic> Iterator<T> attributesIterator(Attribute origin, boolean readPhantom) {
 		Iterator<T> iterator = ((GenericImpl) origin).safeIsEnabled(getNoInheritanceSystemProperty()) ? this.<T> noInheritanceIterator(origin, Statics.NO_POSITION, SystemGeneric.STRUCTURAL) : this.<T> inheritanceStructuralIterator(origin);
 		return !readPhantom ? Statics.<T> nullFilter(iterator) : iterator;
 	}
 
 	public <T extends Generic> Iterator<T> holdersIterator(Holder origin, int basePos, boolean readPhantom) {
 		Iterator<T> iterator = null;
 		boolean noInheritance = ((GenericImpl) origin).safeIsEnabled(getNoInheritanceSystemProperty());
 		if (((GenericImpl) origin).safeIsEnabled(getMultiDirectionalSystemProperty())) {
 			Iterator<T>[] iterators = new Iterator[origin.getComponentsSize()];
 			for (basePos = 0; basePos < iterators.length; basePos++)
 				iterators[basePos] = noInheritance ? this.<T> noInheritanceIterator(origin, basePos, SystemGeneric.CONCRETE) : this.<T> inheritanceConcreteIterator(origin, basePos);
 			iterator = new ConcateIterator<T>(iterators);
 		} else
 			iterator = noInheritance ? this.<T> noInheritanceIterator(origin, basePos, SystemGeneric.CONCRETE) : this.<T> inheritanceConcreteIterator(origin, basePos);
 		return !readPhantom ? Statics.<T> nullFilter(iterator) : iterator;
 	}
 
 	private Attribute getNoInheritanceSystemProperty() {
 		return getCurrentCache().<Attribute> find(NoInheritanceSystemProperty.class);
 	}
 
 	private Attribute getMultiDirectionalSystemProperty() {
 		return getCurrentCache().<Attribute> find(MultiDirectionalSystemProperty.class);
 	}
 
 	private <T extends Generic> Iterator<T> noInheritanceIterator(final Generic origin, int pos, final int metaLevel) {
 		return new AbstractFilterIterator<T>(Statics.NO_POSITION == pos ? this.<T> compositesIterator() : this.<T> compositesIterator(pos)) {
 			@Override
 			public boolean isSelected() {
 				return next.getMetaLevel() == metaLevel && next.inheritsFrom(origin);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> inheritanceStructuralIterator(final Generic origin) {
 		return (Iterator<T>) new AbstractSelectableLeafIterator(origin) {
 			@Override
 			public boolean isSelected(Generic candidate) {
 				return candidate.getMetaLevel() <= SystemGeneric.STRUCTURAL && candidate.isAttributeOf(GenericImpl.this);
 			}
 
 			@Override
 			public boolean isSelectable() {
 				return next.isStructural();
 			}
 		};
 	}
 
 	public <T extends Generic> Iterator<T> inheritanceConcreteIterator(final Generic origin, final int pos) {
 		return (Iterator<T>) new AbstractSelectableLeafIterator(origin) {
 
 			@Override
 			public boolean isSelectable() {
 				return next.isConcrete();
 			}
 
 			@Override
 			public final boolean isSelected(Generic candidate) {
 				boolean selected = ((GenericImpl) candidate).isAttributeOf(GenericImpl.this, pos);
 				if (selected && ((GenericImpl) candidate).isPseudoStructural(pos))
 					if (getCurrentCache() instanceof CacheImpl)
 						((GenericImpl) candidate).project(pos, getCurrentCache().findPrimaryByValue(((GenericImpl) candidate.getImplicit()).supers[0], null, SystemGeneric.CONCRETE));
 				return selected;
 			}
 		};
 	}
 
 	private void project(final int pos, Generic phantom) {
 		Iterator<Object[]> cartesianIterator = new CartesianIterator(projections(pos));
 		while (cartesianIterator.hasNext()) {
 			Generic[] components = (Generic[]) cartesianIterator.next();
 			if (!findPhantom(phantom, components))
 				getCurrentCache().bind(getImplicit(), true, this, false, components);
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
 
 	private boolean findPhantom(Generic phantom, Generic[] components) {
 		return phantom != null && getCurrentCache().fastFindByInterfaces(phantom, new Primaries(Statics.insertFirst(phantom, supers)).toArray(), components) != null;
 	}
 
 	boolean safeIsEnabled(Attribute attribute) {
 		Iterator<Generic> iterator = new AbstractSelectableLeafIterator(attribute) {
 			@Override
 			public boolean isSelected(Generic candidate) {
 				return (candidate.getMetaLevel() <= SystemGeneric.CONCRETE) && candidate.isAttributeOf(GenericImpl.this);
 			}
 
 			@Override
 			public boolean isSelectable() {
 				return next.isConcrete();
 			}
 		};
 		return iterator.hasNext();
 	}
 
 	@Override
 	// TODO KK
 	public boolean inheritsFrom(Generic generic) {
 		if (generic == null)
 			return false;
 		boolean inheritance = ((GenericImpl) generic).new InheritanceCalculator().isSuperOf(this);
 		boolean superOf = ((GenericImpl) generic).isSuperOf(this);// ,false
 		assert inheritance == superOf : "" + this.info() + generic.info() + " : " + inheritance + " != " + superOf;
 		return superOf;
 	}
 
 	private Primaries getPrimaries() {
 		return new Primaries(this);
 	}
 
 	public Generic[] getPrimariesArray() {
 		return getPrimaries().toArray();
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
 		if (((GenericImpl) generic).isPrimary())
 			return isSuperOf(((GenericImpl) generic).supers[0]);
 		return isSuperOf(getPrimariesArray(), components, ((GenericImpl) generic).getPrimariesArray(), ((GenericImpl) generic).components);
 	}
 
 	public static boolean isSuperOf(Generic[] interfaces, Generic[] components, final Generic[] subInterfaces, Generic[] subComponents) {
 		if (interfaces.length == subInterfaces.length && components.length == subComponents.length) {
 			for (int i = 0; i < subInterfaces.length; i++) {
 				if (!((GenericImpl) interfaces[i]).isSuperOf(subInterfaces[i]))
 					return false;
 			}
 			for (int i = 0; i < subComponents.length; i++) {
 				if (components[i] != null && subComponents[i] != null) {
 					if (!Arrays.equals(interfaces, ((GenericImpl) components[i]).getPrimariesArray()) || !Arrays.equals(components, ((GenericImpl) components[i]).components)
 							|| !Arrays.equals(subInterfaces, ((GenericImpl) subComponents[i]).getPrimariesArray()) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 						if (!((GenericImpl) components[i]).isSuperOf(subComponents[i]))
 							return false;
 				}
 				if (components[i] == null) {
 					if (!Arrays.equals(subInterfaces, ((GenericImpl) subComponents[i]).getPrimariesArray()) || !Arrays.equals(subComponents, ((GenericImpl) subComponents[i]).components))
 						return false;
 				} else if (subComponents[i] == null)
 					if (!components[i].isEngine() && (!Arrays.equals(interfaces, ((GenericImpl) components[i]).getPrimariesArray()) || !Arrays.equals(components, ((GenericImpl) components[i]).components)))
 						return false;
 			}
 			return true;
 		}
 		if (subInterfaces.length > 1 && interfaces.length < subInterfaces.length)
 			for (int i = 0; i < subInterfaces.length; i++)
 				if (isSuperOf(interfaces, components, Statics.truncate(i, subInterfaces), subComponents))
 					return true;
 		if (components.length < subComponents.length)
 			for (int i = 0; i < subComponents.length; i++)
 				if (isSuperOf(interfaces, components, subInterfaces, Statics.truncate(i, subComponents)))
 					return true;
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
 		for (Generic primary : getPrimaries())
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
 		case SystemGeneric.META:
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
 		case SystemGeneric.STRUCTURAL:
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
 		case SystemGeneric.CONCRETE:
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
 
 	public boolean isPrimary() {
 		return components.length == 0 && supers.length == 1;
 	}
 
 	@Override
 	public <T extends Generic> T getMeta() {
 		int level = isMeta() ? SystemGeneric.META : getMetaLevel() - 1;
 		GenericImpl generic = this;
 		while (level != generic.getMetaLevel())
 			generic = (GenericImpl) generic.supers[generic.supers.length - 1];
 		return (T) generic;
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
 	public <T extends Generic> T getInstanceByValue(final Serializable value) {
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
 		final Generic primary = getCurrentCache().findPrimaryByValue(getEngine(), value, SystemGeneric.STRUCTURAL);
 		if (primary == null)
 			return null;
 		Iterator<T> iterator = Statics.<T> valueFilter(new AbstractFilterIterator<T>((((GenericImpl) primary).<T> directInheritingsIterator())) {
 
 			@Override
 			public boolean isSelected() {
 				return next.inheritsFrom(GenericImpl.this);
 			}
 		}, value);
 		if (!primary.isAutomatic() && iterator.hasNext())
 			throw new IllegalStateException("Ambigous selection");
 		if (!iterator.hasNext() && primary.inheritsFrom(this))
 			return (T) primary;
 		return Statics.<T> unambigousFirst(iterator);
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
 		return Statics.levelFilter(this.<T> allInheritingsIteratorWithoutRoot(), SystemGeneric.STRUCTURAL);
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
 		if (clazz.getAnnotation(InheritanceDisabled.class) != null)
 			disableInheritance();
 
 		if (clazz.getAnnotation(VirtualConstraint.class) != null)
 			enableVirtualConstraint();
 
 		if (clazz.getAnnotation(UniqueConstraint.class) != null)
 			enableUniqueConstraint();
 
 		InstanceValueClassConstraint instanceClass = clazz.getAnnotation(InstanceValueClassConstraint.class);
 		if (instanceClass != null)
 			setConstraintClass(instanceClass.value());
 
 		if (clazz.getAnnotation(PropertyConstraint.class) != null)
 			enablePropertyConstraint();
 
 		if (clazz.getAnnotation(SingularInstanceConstraint.class) != null)
 			enableSingularInstanceConstraint();
 
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
 
 	public <T extends Generic> T enableSystemProperty(Class<?> systemPropertyClass) {
 		return enableSystemProperty(systemPropertyClass, Statics.BASE_POSITION);
 	}
 
 	public <T extends Generic> T disableSystemProperty(Class<?> systemPropertyClass) {
 		return disableSystemProperty(systemPropertyClass, Statics.BASE_POSITION);
 	}
 
 	public <T extends Generic> T enableSystemProperty(Class<?> systemPropertyClass, int basePos) {
 		setBooleanSystemProperty(getCurrentCache().<Attribute> find(systemPropertyClass), basePos, !systemPropertyClass.getAnnotation(SystemGeneric.class).defaultBehavior());
 		return (T) this;
 	}
 
 	public <T extends Generic> T disableSystemProperty(Class<?> systemPropertyClass, int basePos) {
 		setBooleanSystemProperty(getCurrentCache().<Attribute> find(systemPropertyClass), basePos, systemPropertyClass.getAnnotation(SystemGeneric.class).defaultBehavior());
 		return (T) this;
 	}
 
 	private <T extends Generic> T setBooleanSystemProperty(Attribute systemProperty, int basePos, boolean defaultBehavior) {
 		if (defaultBehavior)
 			return setHolder(systemProperty, basePos);
 		else {
 			Link holder = getHolderByValue(systemProperty, basePos);
 			if (holder != null)
 				if (equals(holder.getBaseComponent())) {
 					holder.remove();
 					return setBooleanSystemProperty(systemProperty, basePos, defaultBehavior);
 				} else
 					cancel(holder, basePos, true);
 		}
 		return null;
 	}
 
 	public boolean isBooleanSystemPropertyEnabled(Class<? extends BooleanSystemProperty> systemPropertyClass) {
 		return isBooleanSystemPropertyEnabled(systemPropertyClass, Statics.BASE_POSITION);
 	}
 
 	public boolean isBooleanSystemPropertyEnabled(Class<? extends BooleanSystemProperty> systemPropertyClass, int basePos) {
 		boolean defaultBehavior = systemPropertyClass.getAnnotation(SystemGeneric.class).defaultBehavior();
 		return getHolderByValue(getCurrentCache().<Attribute> find(systemPropertyClass), basePos) == null ? defaultBehavior : !defaultBehavior;
 	}
 
 	@Override
 	public <T extends Attribute> T enableMultiDirectional() {
 		return enableSystemProperty(MultiDirectionalSystemProperty.class);
 	}
 
 	@Override
 	public <T extends Attribute> T disableMultiDirectional() {
 		return disableSystemProperty(MultiDirectionalSystemProperty.class);
 	}
 
 	@Override
 	public boolean isMultiDirectional() {
 		return isBooleanSystemPropertyEnabled(MultiDirectionalSystemProperty.class);
 	}
 
 	@Override
 	public <T extends Relation> T enableCascadeRemove(int basePos) {
 		return enableSystemProperty(CascadeRemoveSystemProperty.class, basePos);
 	}
 
 	@Override
 	public <T extends Relation> T disableCascadeRemove(int basePos) {
 		return disableSystemProperty(CascadeRemoveSystemProperty.class, basePos);
 	}
 
 	@Override
 	public boolean isCascadeRemove(int basePos) {
 		return isBooleanSystemPropertyEnabled(CascadeRemoveSystemProperty.class, basePos);
 	}
 
 	@Override
 	public <T extends Generic> T enableReferentialIntegrity(int componentPos) {
 		return enableSystemProperty(ReferentialIntegritySystemProperty.class, componentPos);
 	}
 
 	@Override
 	public <T extends Generic> T disableReferentialIntegrity(int componentPos) {
 		return disableSystemProperty(ReferentialIntegritySystemProperty.class, componentPos);
 	}
 
 	@Override
 	public boolean isReferentialIntegrity(int basePos) {
 		return isBooleanSystemPropertyEnabled(ReferentialIntegritySystemProperty.class, basePos);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularConstraint() {
 		return enableSystemProperty(SingularConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularConstraint() {
 		return disableSystemProperty(SingularConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isSingularConstraintEnabled() {
 		return isBooleanSystemPropertyEnabled(SingularConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularConstraint(int basePos) {
 		return enableSystemProperty(SingularConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularConstraint(int basePos) {
 		return disableSystemProperty(SingularConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public boolean isSingularConstraintEnabled(int basePos) {
 		return isBooleanSystemPropertyEnabled(SingularConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public <T extends Generic> T enableSizeConstraint(final int basePos, Integer size) {
 		Attribute sizeConstraint = getCurrentCache().<Attribute> find(SizeConstraintImpl.class);
 		T holder = setBooleanSystemProperty(sizeConstraint, basePos, !SizeConstraintImpl.class.getAnnotation(SystemGeneric.class).defaultBehavior());
 		holder.setHolder(sizeConstraint.getAttribute(SizeConstraintImpl.SIZE), size);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Generic> T disableSizeConstraint(final int basePos) {
 		return disableSystemProperty(SizeConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public Integer getSizeConstraint(final int basePos) {
 		Attribute sizeConstraint = getCurrentCache().<Attribute> find(SizeConstraintImpl.class);
 		Link valuedHolder = getHolderByValue(sizeConstraint, basePos);
 		if (valuedHolder == null)
 			return null;
 		return valuedHolder.getValue(sizeConstraint.getAttribute(SizeConstraintImpl.SIZE));
 	}
 
 	@Override
 	public <T extends Type> T enablePropertyConstraint() {
 		return enableSystemProperty(PropertyConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disablePropertyConstraint() {
 		return disableSystemProperty(PropertyConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isPropertyConstraintEnabled() {
 		return isBooleanSystemPropertyEnabled(PropertyConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableRequiredConstraint() {
 		return enableSystemProperty(RequiredConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableRequiredConstraint() {
 		return disableSystemProperty(RequiredConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isRequiredConstraintEnabled() {
 		return isBooleanSystemPropertyEnabled(RequiredConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableRequiredConstraint(int basePos) {
 		return enableSystemProperty(RequiredConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public <T extends Type> T disableRequiredConstraint(int basePos) {
 		return disableSystemProperty(RequiredConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public boolean isRequiredConstraintEnabled(int basePos) {
 		return isBooleanSystemPropertyEnabled(RequiredConstraintImpl.class, basePos);
 	}
 
 	@Override
 	public <T extends Type> T enableUniqueConstraint() {
 		return enableSystemProperty(UniqueConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableUniqueConstraint() {
 		return disableSystemProperty(UniqueConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isUniqueConstraintEnabled() {
 		return isBooleanSystemPropertyEnabled(UniqueConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableVirtualConstraint() {
 		return enableSystemProperty(VirtualConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableVirtualConstraint() {
 		return disableSystemProperty(VirtualConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isVirtualConstraintEnabled() {
 		return isBooleanSystemPropertyEnabled(VirtualConstraintImpl.class);
 	}
 
 	// TODO pas comme les autres contraintes
 	@Override
 	public Class<?> getConstraintClass() {
 		Holder holder = getHolder(getCurrentCache().<Attribute> find(InstanceClassConstraintImpl.class));
 		return (Class<?>) (holder == null ? Object.class : holder.getValue());
 	}
 
 	// TODO pas comme les autres contraintes
 	@Override
 	public <T extends Type> T setConstraintClass(Class<?> constraintClass) {
 		return setHolder(getCurrentCache().<Attribute> find(InstanceClassConstraintImpl.class), constraintClass);
 	}
 
 	@Override
 	public <T extends Type> T enableSingularInstanceConstraint() {
 		return enableSystemProperty(SingularInstanceConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T disableSingularInstanceConstraint() {
 		return disableSystemProperty(SingularInstanceConstraintImpl.class);
 	}
 
 	@Override
 	public boolean isSingularInstanceConstraintEnabled() {
 		return isBooleanSystemPropertyEnabled(SingularInstanceConstraintImpl.class);
 	}
 
 	@Override
 	public <T extends Type> T enableInheritance() {
 		return disableSystemProperty(NoInheritanceSystemProperty.class);
 	}
 
 	@Override
 	public <T extends Type> T disableInheritance() {
 		return enableSystemProperty(NoInheritanceSystemProperty.class);
 	}
 
 	@Override
 	public boolean isInheritanceEnabled() {
 		return !isBooleanSystemPropertyEnabled(NoInheritanceSystemProperty.class);
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
 
 	boolean equiv(Generic[] interfaces, Generic[] components) {
 		return Arrays.equals(getPrimariesArray(), interfaces) && Arrays.equals(nullToSelfComponent(components), this.components);
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
 	public <T extends Attribute> T addAttribute(Serializable value) {
 		return addSubAttribute(getEngine(), value);
 	}
 
 	@Override
 	public <T extends Attribute> T addProperty(Serializable value, Type... targets) {
 		return addSubAttribute(getEngine(), value).enableSingularConstraint();
 	}
 
 	@Override
 	public <T extends Relation> T addRelation(Serializable value, Type... targets) {
 		return addSubRelation(getEngine(), value);
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
 	public <T extends MapProvider> Map<Serializable, Serializable> getMap(Class<T> mapClass) {
 		return getCurrentCache().<MapProvider> find(mapClass).getMap(this);
 	}
 
 	@Override
 	public Map<Serializable, Serializable> getProperties() {
 		return getMap(PropertiesMapProvider.class);
 	}
 
 	@Override
 	public boolean isTree() {
 		return this instanceof Tree;
 	}
 
 	@Override
 	public boolean isRoot() {
		return isTree() && equals(getBaseComponent());
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
 	// TODO clean
 	public <T extends Generic> T addSuper(/* int pos, */Generic newSuper) {
 		return getCurrentCache().addSuper(this, newSuper);
 	}
 
 	@Override
 	public <T extends Generic> T removeSuper(int pos) {
 		return getCurrentCache().removeSuper(this, pos);
 	}
 
 	@Override
 	public <T extends Generic> T updateKey(Serializable key) {
 		return getCurrentCache().updateKey(this, key);
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
 	public Snapshot<Structural> getStructurals() {
 		return new AbstractSnapshot<Structural>() {
 			@Override
 			public Iterator<Structural> iterator() {
 				return structuralsIterator();
 			}
 		};
 	}
 
 	public Iterator<Structural> structuralsIterator() {
 		return new AbstractConcateIterator<Attribute, Structural>(GenericImpl.this.getAttributes().iterator()) {
 			@Override
 			protected Iterator<Structural> getIterator(final Attribute attribute) {
 				return attribute.isMultiDirectional() ? new SingletonIterator<Structural>(new StructuralImpl(attribute, getBasePos(attribute))) : new AbstractProjectionIterator<Integer, Structural>(positionsIterator(attribute)) {
 					@Override
 					public Structural project(Integer pos) {
 						return new StructuralImpl(attribute, pos);
 					}
 				};
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
 }
