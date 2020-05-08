 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import org.genericsystem.annotation.InstanceGenericClass;
 import org.genericsystem.annotation.NoInheritance;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.constraints.InstanceValueClassConstraint;
 import org.genericsystem.annotation.constraints.PropertyConstraint;
 import org.genericsystem.annotation.constraints.SingletonConstraint;
 import org.genericsystem.annotation.constraints.SingularConstraint;
 import org.genericsystem.annotation.constraints.UniqueValueConstraint;
 import org.genericsystem.annotation.constraints.VirtualConstraint;
 import org.genericsystem.constraints.InstanceClassConstraintImpl;
 import org.genericsystem.constraints.MetaLevelConstraintImpl;
 import org.genericsystem.constraints.PropertyConstraintImpl;
 import org.genericsystem.constraints.RequiredConstraintImpl;
 import org.genericsystem.constraints.SingletonConstraintImpl;
 import org.genericsystem.constraints.SingularConstraintImpl;
 import org.genericsystem.constraints.SizeConstraintImpl;
 import org.genericsystem.constraints.UniqueValueConstraintImpl;
 import org.genericsystem.constraints.VirtualConstraintImpl;
 import org.genericsystem.core.Snapshot.Projector;
 import org.genericsystem.core.UnsafeGList.Components;
 import org.genericsystem.core.UnsafeGList.Supers;
 import org.genericsystem.core.UnsafeGList.UnsafeComponents;
 import org.genericsystem.core.UnsafeVertex.Vertex;
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
 import org.genericsystem.systemproperties.NoInheritanceProperty;
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
 
 	private Vertex vertex;
 
 	public Vertex vertex() {
 		return vertex;
 	}
 
 	public HomeTreeNode homeTreeNode() {
 		return vertex.homeTreeNode();
 	}
 
 	@Override
 	public Supers getSupers() {
 		return vertex.supers();
 	}
 
 	@Override
 	public Components getComponents() {
 		return vertex.components();
 	}
 
 	@Override
 	public boolean fastValueEquals(Generic generic) {
 		return homeTreeNode().equals(((GenericImpl) generic).homeTreeNode());
 	}
 
 	public HomeTreeNode bindInstanceNode(Serializable value) {
 		return homeTreeNode().bindInstanceNode(value);
 	}
 
 	public HomeTreeNode findInstanceNode(Serializable value) {
 		return homeTreeNode().findInstanceNode(value);
 	}
 
 	final GenericImpl initialize(UnsafeVertex uVertex) {
 		return restore(uVertex, null, Long.MAX_VALUE, 0L, Long.MAX_VALUE);
 	}
 
 	final GenericImpl restore(UnsafeVertex uVertex, Long designTs, long birthTs, long lastReadTs, long deathTs) {
 		vertex = new Vertex(this, uVertex);
 
 		lifeManager = new LifeManager(designTs == null ? getEngine().pickNewTs() : designTs, birthTs, lastReadTs, deathTs);
 
 		for (Generic superGeneric : getSupers()) {
 			if (this.equals(superGeneric) && !isEngine())
 				getCurrentCache().rollback(new IllegalStateException());
 			if ((getMetaLevel() - superGeneric.getMetaLevel()) > 1)
 				getCurrentCache().rollback(new IllegalStateException());
 			if ((getMetaLevel() - superGeneric.getMetaLevel()) < 0)
 				getCurrentCache().rollback(new IllegalStateException());
 			assert superGeneric.equals(getMeta()) || superGeneric.getMetaLevel() == getMetaLevel() : "superGeneric " + superGeneric.info() + " getMeta() " + getMeta().info();
 			assert superGeneric.equals(getMeta()) || getMeta().inheritsFrom(superGeneric.getMeta()) : getSupers();
 		}
 		return this;
 	}
 
 	<T extends Generic> T plug() {
 		Set<Generic> componentsSet = new HashSet<>();
 		for (Generic component : getComponents())
 			if (componentsSet.add(component))
 				((GenericImpl) component).lifeManager.engineComposites.add(this);
 
 		Set<Generic> supersSet = new HashSet<>();
 		for (Generic superGeneric : getSupers())
 			if (supersSet.add(superGeneric))
 				((GenericImpl) superGeneric).lifeManager.engineInheritings.add(this);
 		return (T) this;
 	}
 
 	<T extends Generic> T unplug() {
 		Set<Generic> componentsSet = new HashSet<>();
 		for (Generic component : getComponents())
 			if (componentsSet.add(component))
 				((GenericImpl) component).lifeManager.engineComposites.remove(this);
 
 		Set<Generic> supersSet = new HashSet<>();
 		for (Generic superGeneric : getSupers())
 			if (supersSet.add(superGeneric))
 				((GenericImpl) superGeneric).lifeManager.engineInheritings.remove(this);
 		return (T) this;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		// TODO clean
 		// log.info("FINALIZE " + info());
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
 		return (EngineImpl) getSupers().get(0).getEngine();
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
 	// TODO clean
 	public <T extends Generic> T getMeta() throws RollbackException {
 		return vertex.getMeta();
 	}
 
 	@Override
 	public int getMetaLevel() {
 		return homeTreeNode().getMetaLevel();
 	}
 
 	@Override
 	public boolean isConcrete() {
 		return vertex().isConcrete();
 	}
 
 	@Override
 	public boolean isStructural() {
 		return vertex().isStructural();
 	}
 
 	@Override
 	public boolean isMeta() {
 		return vertex().isMeta();
 	}
 
 	@Override
 	public boolean isType() {
 		return getComponents().size() == 0;
 	}
 
 	@Override
 	public boolean isAttribute() {
 		return getComponents().size() >= 1;
 	}
 
 	@Override
 	public boolean isReallyAttribute() {
 		return getComponents().size() == 1;
 	}
 
 	@Override
 	public boolean isAttributeOf(Generic generic) {
 		for (Generic component : getComponents())
 			if (generic.inheritsFrom(component))
 				return true;
 		return false;
 	}
 
 	@Override
 	public boolean isAttributeOf(Generic generic, int basePos) {
 		if (basePos < 0 || basePos >= getComponents().size())
 			return false;
 		return generic.inheritsFrom(getComponents().get(basePos));
 	}
 
 	@Override
 	public boolean isRelation() {
 		return getComponents().size() > 1;
 	}
 
 	@Override
 	public boolean isReallyRelation() {
 		return getComponents().size() == 2;
 	}
 
 	@Override
 	public <S extends Serializable> S getValue() {
 		return homeTreeNode().getValue();
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
 		T holder = setHolder(attribute, value, Statics.CONCRETE);
 		assert value == null || getValues(attribute).contains(value) : "holder : " + holder.info() + " value : " + value + " => " + getValues(attribute);
 		return holder;
 	}
 
 	@Override
 	public <T extends Link> T setLink(Link relation, Serializable value, Generic... targets) {
 		return setLink(relation, value, getBasePos(relation), targets);
 	}
 
 	@Override
 	public <T extends Link> T setLink(Link relation, Serializable value, int basePos, Generic... targets) {
 		return setLink(relation, value, basePos, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public <T extends Link> T setLink(Link relation, Serializable value, int basePos, int metaLevel, Generic... targets) {
 		return setHolder(relation, value, metaLevel, basePos, targets);
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, Generic... targets) {
 		return setHolder(attribute, value, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, int metaLevel, Generic... targets) {
 		return setHolder(attribute, value, metaLevel, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, int basePos, Serializable value, Generic... targets) {
 		return addHolder(attribute, value, basePos, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public <T extends Holder> T addHolder(Holder attribute, Serializable value, int basePos, int metaLevel, Generic... targets) {
 		return bind(metaLevel == attribute.getMetaLevel() ? attribute.getMeta() : attribute, value, null, attribute, metaLevel == attribute.getMetaLevel() ? new Generic[] { attribute } : Statics.EMPTY_GENERIC_ARRAY, basePos, true, targets);
 	}
 
 	public <T extends Holder> T bind(Generic meta, Serializable value, Class<?> specializationClass, Holder directSuper, Generic[] strictSupers, int basePos, boolean existsException, Generic... targets) {
 		return getCurrentCache().bind(meta, value, specializationClass, directSuper, strictSupers, existsException, basePos, Statics.insertIntoArray(this, targets, basePos));
 	}
 
 	@Override
 	public void cancel(Holder holder) {
 		cancel(holder, Statics.CONCRETE);
 	}
 
 	@Override
 	public void cancel(Holder holder, int metaLevel) {
 		internalClear(unambigousFirst(holdersIterator(holder, metaLevel, getBasePos(holder))));
 		internalCancel(unambigousFirst(holdersIterator(holder, metaLevel, getBasePos(holder))), metaLevel, getBasePos(holder));
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, Generic... targets) {
 		cancelAll(attribute, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, int metaLevel, Generic... targets) {
 		cancelAll(attribute, getBasePos(attribute), metaLevel, targets);
 	}
 
 	@Override
 	public void cancelAll(Holder attribute, int basePos, int metaLevel, Generic... targets) {
 		internalClearAll(attribute, basePos, metaLevel, targets);
 		Iterator<Holder> holders = this.<Holder> holdersIterator(attribute, metaLevel, basePos, targets);
 		while (holders.hasNext())
 			internalCancel(holders.next(), metaLevel, basePos);
 	}
 
 	private void internalCancel(Holder attribute, int metaLevel, int basePos) {
 		if (attribute != null)
 			bind(metaLevel == attribute.getMetaLevel() ? attribute.getMeta() : attribute, null, null, attribute, metaLevel == attribute.getMetaLevel() ? new Generic[] { attribute } : Statics.EMPTY_GENERIC_ARRAY, basePos, false);
 	}
 
 	@Override
 	public void clear(Holder holder) {
 		clear(holder, Statics.CONCRETE);
 	}
 
 	@Override
 	public void clear(Holder holder, int metaLevel) {
 		internalClear(unambigousFirst(holdersIterator(holder, metaLevel, getBasePos(holder))));
 	}
 
 	@Override
 	public void clearAll(Holder attribute, Generic... targets) {
 		clearAll(attribute, Statics.CONCRETE, targets);
 	}
 
 	@Override
 	public void clearAll(Holder attribute, int metaLevel, Generic... targets) {
 		clearAll(attribute, getBasePos(attribute), metaLevel, targets);
 	}
 
 	@Override
 	public void clearAll(Holder attribute, int basePos, int metaLevel, Generic... targets) {
 		internalClearAll(attribute, basePos, metaLevel, targets);
 	}
 
 	private void internalClearAll(Holder attribute, int basePos, int metaLevel, Generic... targets) {
 		Iterator<Holder> holders = this.<Holder> holdersIterator(attribute, metaLevel, basePos, targets);
 		while (holders.hasNext())
 			internalClear(holders.next());
 	}
 
 	private void internalClear(Holder holder) {
 		if (holder != null && equals(holder.getBaseComponent()))
 			holder.remove();
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
 
 	@Override
 	public <T extends Generic> Snapshot<T> getInstances() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return instancesIterator();
 			}
 		};
 	}
 
 	public <T extends Generic> Iterator<T> instancesIterator() {
 		return Statics.<T> levelFilter(GenericImpl.this.<T> inheritingsIterator(), getMetaLevel() + 1);
 	}
 
 	// TODO KK supers are necessary for get instance from meta !!!
 	@Override
 	public <T extends Generic> T getInstance(Serializable value, Generic... targets) {
 		return this.unambigousFirst(targetsFilter(Statics.<T> valueFilter(GenericImpl.this.<T> allInstancesIterator(), value), this, targets));
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
 
 	public <T extends Holder> Iterator<T> holdersIterator(Holder attribute, Generic... targets) {
 		return this.<T> targetsFilter(GenericImpl.this.<T> holdersIterator(Statics.CONCRETE, attribute, getBasePos(attribute)), attribute, targets);
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
 
 	public <T extends Holder> T getHolderByValue(Holder attribute, Serializable value, final Generic... targets) {
 		return getHolderByValue(Statics.CONCRETE, attribute, value, targets);
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
 		return getLink(relation, getBasePos(relation), targets);
 	}
 
 	public <T extends Generic> Iterator<T> inheritingsIterator() {
 		return getCurrentCache().inheritingsIterator(this);
 	}
 
 	public <T extends Generic> Iterator<T> dependenciesIterator() {
 		return new ConcateIterator<T>(this.<T> inheritingsIterator(), this.<T> compositesIterator());
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getInheritings() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return inheritingsIterator();
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
 	public <T extends Attribute> T getAttribute(Serializable value, Generic... targets) {
 		Attribute metaAttribute = getCurrentCache().getMetaAttribute();
 		return this.unambigousFirst(targetsFilter(Statics.valueFilter(this.<T> holdersIterator(Statics.STRUCTURAL, metaAttribute, Statics.MULTIDIRECTIONAL), value), metaAttribute, targets));
 	}
 
 	@Override
 	public <T extends Relation> Snapshot<T> getRelations() {
 		return getAttributes();
 	}
 
 	public <T extends Generic> T reFind() {
 		return getCurrentCache().reFind(this);
 	}
 
 	@Override
 	public <T extends Relation> T getRelation(Serializable value, Generic... targets) {
 		Relation metaRelation = getCurrentCache().getMetaRelation();
 		return this.unambigousFirst(targetsFilter(Statics.valueFilter(this.<T> holdersIterator(Statics.STRUCTURAL, metaRelation, Statics.MULTIDIRECTIONAL), value), metaRelation, targets));
 	}
 
 	@Override
 	public <T extends Attribute> T getProperty(Serializable value, Generic... targets) {
 		return getAttribute(value, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T setProperty(Serializable value, Generic... targets) {
 		return setAttribute(value, targets).enablePropertyConstraint();
 	}
 
 	public <T extends Attribute> T setSubProperty(Attribute property, Serializable value, Generic... targets) {
 		return setSubAttribute(property, value, targets).enablePropertyConstraint();
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
 		return setHolder(getEngine(), value, Statics.STRUCTURAL, Statics.BASE_POSITION, targets);
 	}
 
 	@Override
 	public <T extends Attribute> T addAttribute(Serializable value, Generic... targets) {
 		return addHolder((Holder) getEngine(), value, Statics.BASE_POSITION, Statics.STRUCTURAL, targets);
 	}
 
 	public <T extends Relation> T setSubAttribute(Attribute attribute, Serializable value, Generic... targets) {
 		T holder = setHolder(attribute, value, Statics.STRUCTURAL, getBasePos(attribute), targets);
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
 		return this.<T> bind(attribute.getMetaLevel() >= metaLevel ? attribute.getMeta() : attribute, value, specializationClass, attribute, metaLevel == attribute.getMetaLevel() ? new Generic[] { attribute } : Statics.EMPTY_GENERIC_ARRAY, basePos, false,
 				targets);
 	}
 
 	@Override
 	public <T extends Holder> T setHolder(Holder attribute, Serializable value, int metaLevel, int basePos, Generic... targets) {
 		return this.<T> setHolder(null, attribute, value, metaLevel, basePos, targets);
 	}
 
 	@Override
 	public <T extends Holder> T flag(Holder attribute, Generic... targets) {
 		return setHolder(attribute, Statics.FLAG, Statics.CONCRETE, getBasePos(attribute), targets);
 	}
 
 	@Override
 	public <T extends Generic> T addAnonymousInstance(Generic... components) {
 		return addInstance(getEngine().pickNewAnonymousReference(), components);
 	}
 
 	@Override
 	public <T extends Generic> T setAnonymousInstance(Generic... components) {
 		return setInstance(getEngine().pickNewAnonymousReference(), components);
 	}
 
 	@Override
 	public <T extends Generic> T addInstance(Serializable value, Generic... components) {
 		return getCurrentCache().bind(this, value, null, this, Statics.EMPTY_GENERIC_ARRAY, true, Statics.MULTIDIRECTIONAL, components);
 	}
 
 	@Override
 	public <T extends Generic> T setInstance(Serializable value, Generic... components) {
 		return getCurrentCache().bind(this, value, null, this, Statics.EMPTY_GENERIC_ARRAY, false, Statics.MULTIDIRECTIONAL, components);
 	}
 
 	@Override
 	public <T extends Type> T addSubType(Serializable value, Generic... components) {
 		if (isMeta())
 			getCurrentCache().rollback(new UnsupportedOperationException("Derive a meta is not allowed"));
 		return getCurrentCache().bind(getMeta(), value, null, this, new Generic[] { this }, true, Statics.MULTIDIRECTIONAL, components);
 	}
 
 	@Override
 	public <T extends Type> T setSubType(Serializable value, Generic... components) {
 		if (isMeta())
 			getCurrentCache().rollback(new UnsupportedOperationException("Derive a meta is not allowed"));
 		return getCurrentCache().bind(getMeta(), value, null, this, new Generic[] { this }, false, Statics.MULTIDIRECTIONAL, components);
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
 			while (contains(freePosition) || (freePosition < getComponents().size() && !component.inheritsFrom(getComponents().get(freePosition))))
 				freePosition++;
 			if (freePosition >= max)
 				getCurrentCache().rollback(new IllegalStateException("Unable to find a valid position for : " + component.info() + " in : " + getComponents() + " " + getComponents().get(0).info()));
 			add(freePosition);
 			return freePosition;
 		}
 	}
 
 	// TODO KK result should be an arrayList and elements should be added on demand
 	Generic[] sortAndCheck(Generic... components) {
 		TakenPositions takenPositions = new TakenPositions(components.length);
 		Generic[] result = new Generic[components.length];
 		for (Generic component : components)
 			result[takenPositions.getFreePosition(component == null ? GenericImpl.this : component)] = component;
 		return result;
 	}
 
 	public <T extends Generic> Iterator<T> holdersIterator(int level, Holder origin, int basePos) {
 		if (Statics.STRUCTURAL == level)
 			basePos = Statics.MULTIDIRECTIONAL;
 		return ((Attribute) origin).isInheritanceEnabled() ? this.<T> inheritanceIterator2(level, origin, basePos) : this.<T> noInheritanceIterator(level, basePos, origin);
 	}
 
 	private <T extends Generic> Iterator<T> noInheritanceIterator(final int metaLevel, int pos, final Generic origin) {
 		return new AbstractFilterIterator<T>(Statics.MULTIDIRECTIONAL == pos ? this.<T> compositesIterator() : this.<T> compositesIterator(pos)) {
 			@Override
 			public boolean isSelected() {
 				return next.getMetaLevel() == metaLevel && next.inheritsFrom(origin);
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> inheritanceIterator2(final int level, final Generic origin, final int pos) {
 		return new AbstractFilterIterator<T>(this.<T> getInternalInheritings(level, origin, pos).iterator()) {
 
 			@Override
 			public boolean isSelected() {
 				return level == next.getMetaLevel() && (pos != Statics.MULTIDIRECTIONAL ? ((GenericImpl) next).isAttributeOf(GenericImpl.this, pos) : ((GenericImpl) next).isAttributeOf(GenericImpl.this));
 			}
 		};
 	}
 
 	private class Inheritings<T extends Generic> extends LinkedHashSet<T> {
 
 		private static final long serialVersionUID = 6333116882294134638L;
 
 		private final int pos;
 		private final int maxLevel;
 
 		private Inheritings(int maxLevel, Generic origin, int pos) {
 			this.pos = pos;
 			this.maxLevel = maxLevel;
 			for (Generic superGeneric : getSupers())
 				if (!GenericImpl.this.equals(superGeneric))
 					for (T inheriting : (((GenericImpl) superGeneric).<T> getInternalInheritings(maxLevel, origin, pos)))
 						add(inheriting);
 			for (T composite : (GenericImpl.this.<T> getComposites()))
 				if (composite.getMetaLevel() <= maxLevel && composite.inheritsFrom(origin))
 					add(composite);
 		}
 
 		@Override
 		public boolean add(T candidate) {
 			Iterator<T> iterator = iterator();
 			while (iterator.hasNext()) {
 				Generic next = iterator.next();
 				if (candidate.inheritsFrom(next) && !candidate.equals(next))
 					iterator.remove();
 				else if (next.inheritsFrom(candidate))
 					return false;
 			}
 			if (pos != Statics.MULTIDIRECTIONAL) {
 				if (((GenericImpl) candidate).isPseudoStructural(pos))
 					((GenericImpl) candidate).project(pos);
 				if (maxLevel == candidate.getMetaLevel() && !equals(((GenericImpl) candidate).getComponents().get(pos))) {
 					iterator = iterator();
 					while (iterator.hasNext()) {
 						Generic next = iterator.next();
 						UnsafeComponents candidateComponents = Statics.replace(pos, ((GenericImpl) candidate).getComponents(), GenericImpl.this);
 						Generic candidateMeta = candidate.getMeta();
 						if (((GenericImpl) next).homeTreeNode().equals(((GenericImpl) candidate).homeTreeNode()) && next.getMeta().equals(candidateMeta)
 								&& candidateComponents.equals(Statics.replace(pos, ((GenericImpl) next).getComponents(), GenericImpl.this)))
 							new GenericBuilder(((GenericImpl) candidate).getReplacedComponentVertex(pos, GenericImpl.this), true).bindDependency(candidate.getClass(), false, true);
 					}
 				}
 			}
 			return super.add(candidate);
 
 		}
 	}
 
 	private <T extends Generic> Set<T> getInternalInheritings(int level, Generic origin, int pos) {
 		return new Inheritings<>(level, origin, pos);
 	}
 
 	public void project() {
 		project(Statics.MULTIDIRECTIONAL);
 	}
 
 	// TODO KK
 	public void project(final int pos) {
 		Iterator<Generic[]> cartesianIterator = new CartesianIterator<>(projections(pos));
 		while (cartesianIterator.hasNext()) {
 			final UnsafeComponents components = new UnsafeComponents(cartesianIterator.next());
 			Generic projection = this.unambigousFirst(new AbstractFilterIterator<Generic>(allInheritingsIteratorWithoutRoot()) {
 				@Override
 				public boolean isSelected() {
 					return ((GenericImpl) next).inheritsFrom(((GenericImpl) next).filterToProjectVertex(components, pos));
 				}
 			});
 
 			if (projection == null)
 				getCurrentCache().internalBind(projectVertex(components), null, Statics.MULTIDIRECTIONAL, true, false);
 		}
 	}
 
 	private Iterable<Generic>[] projections(final int pos) {
 		final Iterable<Generic>[] projections = new Iterable[getComponents().size()];
 		for (int i = 0; i < projections.length; i++) {
 			final int column = i;
 			projections[i] = new Iterable<Generic>() {
 				@Override
 				public Iterator<Generic> iterator() {
 					return pos != column && getComponents().get(column).isStructural() ? ((GenericImpl) getComponents().get(column)).allInstancesIterator() : new SingletonIterator<Generic>(getComponents().get(column));
 				}
 			};
 		}
 		return projections;
 	}
 
 	@Override
 	public boolean inheritsFrom(Generic generic) {
 		if (equals(generic))
 			return true;
 		if (generic.isEngine())
 			return true;
 		if (getDesignTs() < ((GenericImpl) generic).getDesignTs())
 			return false;
 		for (Generic directSuper : getSupers())
 			if (((GenericImpl) directSuper).inheritsFrom(generic))
 				return true;
 		return false;
 	}
 
 	public boolean isSuperOf2(Generic subGeneric) {
 		if (GenericImpl.this.equals(subGeneric))
 			return true;
 		if (subGeneric.isEngine())
 			return isEngine();
 		for (Generic directSuper : ((GenericImpl) subGeneric).getSupers())
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
 
 	public boolean inheritsFromAll(UnsafeGList generics) {
 		for (Generic generic : generics)
 			if (!inheritsFrom(generic))
 				return false;
 		return true;
 	}
 
 	public boolean inheritsFrom(UnsafeVertex superUVertex) {
 		if (equiv(superUVertex))
 			return true;
 		if (superUVertex.metaLevel() > getMetaLevel())
 			return false;
 		if (getComponents().size() < superUVertex.components().size())
 			return false;
 		for (Generic subSuper : getSupers())
 			if (subSuper.inheritsFrom(this))
 				return true;
 		if (getComponents().size() > superUVertex.components().size()) {
 			for (int i = 0; i < getComponents().size(); i++)
 				if (isSuperOf(superUVertex, getTruncatedComponentVertex(i)))
 					return true;
 			return false;
 		}
 		Generic subVertexMeta = getMeta();
 		if (isConcrete() && superUVertex.getMeta().equals(subVertexMeta))
 			for (int pos = 0; pos < getComponents().size(); pos++)
 				if (((GenericImpl) subVertexMeta).isSingularConstraintEnabled(pos) /* && !subVertexMeta.isReferentialIntegrity(pos) */)
 					if (getComponent(pos).inheritsFrom(superUVertex.components().get(pos)))
 						if (!getComponent(pos).equals(superUVertex.components().get(pos)))
 							return true;
 		for (int i = 0; i < getComponents().size(); i++)
 			if (superUVertex.components().get(i) != null) {
 				if (!getComponents().get(i).inheritsFrom(superUVertex.components().get(i)))
 					return false;
 			} else {
 				if (!getComponents().get(i).inheritsFrom(this))
 					if (!((GenericImpl) getComponents().get(i)).inheritsFrom(superUVertex))
 						return false;
 			}
 		if (isConcrete() && superUVertex.getMeta().equals(subVertexMeta))
 			if (((GenericImpl) subVertexMeta).isPropertyConstraintEnabled())
 				if (!getComponents().equals(superUVertex.components()))
 					return true;
 		if (!homeTreeNode().inheritsFrom(superUVertex.homeTreeNode()))
 			return false;
 		if (!inheritsFromAll(superUVertex.supers()))
 			return false;
 		return true;
 	}
 
 	private static boolean isSuperOf(UnsafeVertex superUVertex, UnsafeVertex subUVertex) {
 		if (superUVertex.homeTreeNode().equals(subUVertex.homeTreeNode()) && superUVertex.supers().equals(subUVertex.supers()) && superUVertex.components().equals(subUVertex.components()))
 			return true;
 		if (superUVertex.metaLevel() > subUVertex.metaLevel())
 			return false;
 		if (subUVertex.components().size() < superUVertex.components().size())
 			return false;
 		for (Generic subSuper : subUVertex.supers())
 			if (((GenericImpl) subSuper).inheritsFrom(superUVertex))
 				return true;
 		// for (Generic superGeneric : superUVertex.supers())
 		// if (!((GenericImpl) superGeneric).isSuperOf(subUVertex))
 		// return false;
 		if (subUVertex.components().size() > superUVertex.components().size()) {
 			for (int i = 0; i < subUVertex.components().size(); i++)
 				if (isSuperOf(superUVertex, subUVertex.truncateComponent(i)))
 					return true;
 			return false;
 		}
 		Generic subVertexMeta = subUVertex.getMeta();
 		if (subUVertex.isConcrete() && superUVertex.getMeta().equals(subVertexMeta))
 			for (int pos = 0; pos < subUVertex.components().size(); pos++)
 				if (((GenericImpl) subVertexMeta).isSingularConstraintEnabled(pos) /* && !subVertexMeta.isReferentialIntegrity(pos) */)
 					if (subUVertex.components().get(pos).inheritsFrom(superUVertex.components().get(pos)))
 						if (!subUVertex.components().get(pos).equals(superUVertex.components().get(pos)))
 							return true;
 		for (int i = 0; i < subUVertex.components().size(); i++)
 			if (superUVertex.components().get(i) != null) {
 				if (!subUVertex.components().get(i).inheritsFrom(superUVertex.components().get(i)))
 					return false;
 			} else if (!((GenericImpl) subUVertex.components().get(i)).inheritsFrom(superUVertex))
 				if (!((GenericImpl) subUVertex.components().get(i)).inheritsFrom(superUVertex))
 					return false;
 		if (subUVertex.isConcrete() && superUVertex.getMeta().equals(subVertexMeta))
 			if (((GenericImpl) subVertexMeta).isPropertyConstraintEnabled())
 				if (!subUVertex.components().equals(superUVertex.components()))
 					return true;
 		if (!subUVertex.homeTreeNode().inheritsFrom(superUVertex.homeTreeNode()))
 			return false;
 		for (Generic superUVertexSuper : superUVertex.supers())
 			if (!((GenericImpl) superUVertexSuper).isSuperOf(subUVertex))
 				return false;
 		return true;
 	}
 
 	public boolean isSuperOf(UnsafeVertex subUVertex) {
 		if (isEngine())
 			return true;
 		if (equiv(subUVertex))
 			return true;
 		if (getMetaLevel() > subUVertex.metaLevel())
 			return false;
 		if (subUVertex.components().size() < getComponents().size())
 			return false;
 		for (Generic subSuper : subUVertex.supers())
 			if (subSuper.inheritsFrom(this))
 				return true;
 		// for (Generic superGeneric : getSupers())
 		// if (!((GenericImpl) superGeneric).isSuperOf(subUVertex))
 		// return false;
 		if (subUVertex.components().size() > getComponents().size()) {
 			for (int i = 0; i < subUVertex.components().size(); i++)
 				if (isSuperOf(subUVertex.truncateComponent(i)))
 					return true;
 			return false;
 		}
 		Generic subVertexMeta = subUVertex.getMeta();
 		if (subUVertex.isConcrete() && getMeta().equals(subVertexMeta))
 			for (int pos = 0; pos < subUVertex.components().size(); pos++)
 				if (((GenericImpl) subVertexMeta).isSingularConstraintEnabled(pos) /* && !subVertexMeta.isReferentialIntegrity(pos) */)
 					if (subUVertex.components().get(pos).inheritsFrom(getComponent(pos)))
 						if (!subUVertex.components().get(pos).equals(getComponent(pos)))
 							return true;
 
 		for (int i = 0; i < subUVertex.components().size(); i++)
 			if (subUVertex.components().get(i) != null) {
 				if (!subUVertex.components().get(i).inheritsFrom(getComponent(i)))
 					return false;
 			} else {
 				if (!equals(getComponents().get(i)))
 					if (!(((GenericImpl) getComponents().get(i)).isSuperOf(subUVertex)))
 						return false;
 			}
 		if (subUVertex.isConcrete() && getMeta().equals(subVertexMeta))
 			if (((GenericImpl) subVertexMeta).isPropertyConstraintEnabled())
 				if (!subUVertex.components().equals(getComponents()))
 					return true;
 
 		if (!subUVertex.homeTreeNode().inheritsFrom(homeTreeNode()))
 			return false;
 
 		return true;
 	}
 
 	@Override
 	public void remove() {
 		remove(RemoveStrategy.NORMAL);
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
 	public void log() {
 		log.info(info());
 	}
 
 	@Override
 	public String info() {
 		String s = "\n******************************" + System.identityHashCode(this) + "******************************\n";
 		s += " Name        : " + toString() + "\n";
 		s += " Meta        : " + getMeta() + " (" + System.identityHashCode(getMeta()) + ")\n";
 		s += " MetaLevel   : " + Statics.getMetaLevelString(getMetaLevel()) + "\n";
 		s += " Category    : " + getCategoryString() + "\n";
 		s += " Class       : " + getClass().getSimpleName() + "\n";
 		s += "**********************************************************************\n";
 		for (Generic superGeneric : getSupers())
 			s += " Super       : " + superGeneric + " (" + System.identityHashCode(superGeneric) + ")\n";
 		for (Generic component : getComponents())
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
 			return "null" + (getSupers().size() >= 2 ? "[" + getSupers().get(1) + "]" : "");
 		return value instanceof Class ? ((Class<?>) value).getSimpleName() : value.toString();
 	}
 
 	public String toCategoryString() {
 		return "(" + getCategoryString() + ") " + toString();
 	}
 
 	public String getCategoryString() throws RollbackException {
 		int metaLevel = getMetaLevel();
 		int dim = getComponents().size();
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
 
 	// TODO KK try to remove this method calls where it is possible!
 	@Override
 	public <T extends Generic> T getComponent(int componentPos) {
 		return getComponents().size() <= componentPos || componentPos < 0 ? null : (T) getComponents().get(componentPos);
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
 
 	public <T extends Generic> Iterator<T> allInstancesIterator() {
 		return Statics.levelFilter(this.<T> allInheritingsAboveIterator(getMetaLevel() + 1), getMetaLevel() + 1);
 	}
 
 	private <T extends Generic> Iterator<T> allInheritingsAboveIterator(final int metaLevel) {
 		return (Iterator<T>) new AbstractPreTreeIterator<Generic>(GenericImpl.this) {
 
 			private static final long serialVersionUID = 7164424160379931253L;
 
 			@Override
 			public Iterator<Generic> children(Generic node) {
 				return new AbstractFilterIterator<Generic>(((GenericImpl) node).inheritingsIterator()) {
 					@Override
 					public boolean isSelected() {
 						return next.getMetaLevel() <= metaLevel;
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public <T extends Type> T getSubType(Serializable value) {
 		return unambigousFirst(Statics.<T> valueFilter(this.<T> subTypesIterator(), value));
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getSubTypes() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return subTypesIterator();
 			}
 		};
 	}
 
 	private <T extends Generic> Iterator<T> subTypesIterator() {
 		return Statics.levelFilter(GenericImpl.this.<T> inheritingsIterator(), getMetaLevel());
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getAllSubTypes() {
 		return new AbstractSnapshot<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return allSubTypesIteratorWithoutRoot();
 			}
 		};
 	}
 
 	// TODO super KK what is this method, what dost it do : no components ? no supers ? ???
 	@Override
 	public <T extends Generic> T getAllSubType(Serializable value) {
 		return this.unambigousFirst(Statics.<T> valueFilter(this.<T> allSubTypesIteratorWithoutRoot(), value));
 	}
 
 	@Override
 	public <T extends Generic> Snapshot<T> getAllSubTypes(final String name) {
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
 				return (((GenericImpl) node).inheritingsIterator());
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
 				return (((GenericImpl) node).inheritingsIterator());
 			}
 		};
 	}
 
 	void mountConstraints(Class<?> clazz) {
 		if (clazz.getAnnotation(NoInheritance.class) != null)
 			disableInheritance();
 
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
 
 	private <T extends Generic> Serializable getSystemPropertyValue(Class<T> propertyClass, int pos) {
 		return getSystemPropertiesMap().get(new AxedPropertyClass(propertyClass, pos));
 	}
 
 	public <T extends Generic> void setSystemPropertyValue(Class<T> propertyClass, int pos, Serializable value) {
 		getSystemPropertiesMap().put(new AxedPropertyClass(propertyClass, pos), value);
 	}
 
 	private <T extends Generic> boolean isSystemPropertyEnabled(Class<T> propertyClass, int pos) {
 		Serializable value = getSystemPropertyValue(propertyClass, pos);
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
 
 	public <T extends Relation> T enableMetaLevelConstraintAttribute() {
 		setSystemPropertyValue(MetaLevelConstraintImpl.class, Statics.MULTIDIRECTIONAL, true);
 		return (T) this;
 	}
 
 	public <T extends Relation> T disableMetaLevelConstraintAttribute() {
 		setSystemPropertyValue(MetaLevelConstraintImpl.class, Statics.MULTIDIRECTIONAL, false);
 		return (T) this;
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
 	public <T extends Relation> T enableInheritance() {
 		setSystemPropertyValue(NoInheritanceProperty.class, Statics.BASE_POSITION, false);
 		return (T) this;
 	}
 
 	@Override
 	public <T extends Relation> T disableInheritance() {
 		setSystemPropertyValue(NoInheritanceProperty.class, Statics.BASE_POSITION, true);
 		return (T) this;
 	}
 
 	@Override
 	public boolean isInheritanceEnabled() {
 		if (!GenericImpl.class.equals(getClass()))
 			return !getClass().isAnnotationPresent(NoInheritance.class);
 		return !isSystemPropertyEnabled(NoInheritanceProperty.class, Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> T enableReferentialIntegrity() {
 		return enableReferentialIntegrity(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public <T extends Generic> T disableReferentialIntegrity() {
 		return disableReferentialIntegrity(Statics.BASE_POSITION);
 	}
 
 	@Override
 	public boolean isReferentialIntegrity() {
 		return isReferentialIntegrity(Statics.BASE_POSITION);
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
 		setConstraintValue(UniqueValueConstraintImpl.class, Statics.MULTIDIRECTIONAL, false);
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
 
 	Components nullToSelfComponent(UnsafeComponents components) {
 		return components instanceof Components ? (Components) components : new Components(this, components);
 	}
 
 	UnsafeComponents selfToNullComponents() {
 		List<Generic> result = new ArrayList<>(getComponents());
 		for (int i = 0; i < result.size(); i++)
 			if (equals(result.get(i)))
 				result.set(i, null);
 		return new UnsafeComponents(result);
 	}
 
 	public boolean equiv(Vertex vertex) {
 		return vertex().equiv(vertex);
 	}
 
 	// public boolean equivByMeta(Vertex vertex) {
 	// return vertex().equivByMeta(vertex);
 	// }
 
 	public boolean equiv(UnsafeVertex uVertex) {
 		return homeTreeNode().equals(uVertex.homeTreeNode()) && getSupers().equals(uVertex.supers()) && getComponents().equals(nullToSelfComponent(uVertex.components()));
 	}
 
 	public boolean equiv(HomeTreeNode homeTreeNode, Supers supers, UnsafeComponents components) {
 		return homeTreeNode().equals(homeTreeNode) && getSupers().equals(supers) && getComponents().equals(nullToSelfComponent(components));
 	}
 
 	public boolean equiv(HomeTreeNode homeTreeNode, UnsafeComponents components) {
 		return homeTreeNode().equals(homeTreeNode) && getComponents().equals(nullToSelfComponent(components));
 	}
 
 	public <T extends Generic> T reBind() {
 		return getCurrentCache().reBind(this);
 	}
 
 	boolean isPseudoStructural(int basePos) {
 		if (!isConcrete())
 			return false;
 		for (int i = 0; i < getComponents().size(); i++)
 			if (i != basePos && getComponents().get(i).isStructural())
 				return true;
 		return false;
 	}
 
 	@Override
 	public <T extends Attribute> T addProperty(Serializable value, Generic... targets) {
 		return addAttribute(value, targets).enablePropertyConstraint();
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
 			for (i = 0; i < getComponents().size(); i++)
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
 		final List<Generic> components = ((GenericImpl) attribute).getComponents();
 		return new AbstractFilterIterator<Integer>(new CountIterator(components.size())) {
 			@Override
 			public boolean isSelected() {
 				return GenericImpl.this.inheritsFrom(components.get(next));
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
 		final List<Generic> components = ((GenericImpl) holder).getComponents();
 		return new AbstractProjectorAndFilterIterator<Integer, Generic>(new CountIterator(components.size())) {
 
 			@Override
 			public boolean isSelected() {
 				return !GenericImpl.this.equals(components.get(next)) && !GenericImpl.this.inheritsFrom(components.get(next));
 			}
 
 			@Override
 			protected Generic project() {
 				return components.get(next);
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
 
 	@Override
 	public boolean isSystem() {
 		return getClass().isAnnotationPresent(SystemGeneric.class);
 	}
 
 	UnsafeVertex getUpdatedValueVertex(HomeTreeNode newHomeTreeNode) {
 		return new UnsafeVertex(newHomeTreeNode, getSupers(), selfToNullComponents());
 	}
 
 	UnsafeVertex createNewVertex(Serializable value, Generic[] supers, Generic[] strictSupers, Generic[] components) {
 		return new UnsafeVertex(bindInstanceNode(value), new Supers(supers.length == 0 ? new Generic[] { getEngine() } : supers), new UnsafeComponents(components));
 	}
 
 	UnsafeVertex getInsertedComponentVertex(Generic newComponent, int pos) {
 		return new UnsafeVertex(homeTreeNode(), getSupers(), Statics.insertIntoComponents(newComponent, selfToNullComponents(), pos));
 	}
 
 	UnsafeVertex getTruncatedComponentVertex(int pos) {
 		return new UnsafeVertex(homeTreeNode(), getSupers(), Statics.truncate(pos, selfToNullComponents()));
 	}
 
 	UnsafeVertex getReplacedComponentVertex(int pos, Generic newComponent) {
 		return new UnsafeVertex(homeTreeNode(), getSupers(), Statics.replace(pos, selfToNullComponents(), newComponent));
 	}
 
 	// TODO kk ?
 	UnsafeVertex filterToProjectVertex(UnsafeComponents components, int pos) {
 		return new UnsafeVertex(homeTreeNode(), getSupers(), Statics.replace(pos, components, getComponent(pos)));
 	}
 
 	UnsafeVertex projectVertex(UnsafeComponents components) {
 		return new UnsafeVertex(homeTreeNode(), getSupers(), components);
 	}
 
 	UnsafeVertex getInsertedSuperVertex(Generic newSuper) {
 		return new UnsafeVertex(homeTreeNode(), Statics.insertIntoSupers(newSuper, getSupers(), 0), selfToNullComponents());
 	}
 
 	UnsafeVertex getTruncatedSuperVertex(int pos) {
 		return new UnsafeVertex(homeTreeNode(), Statics.truncate(pos, getSupers()), selfToNullComponents());
 	}
 
 }
