 package org.genericsystem.core;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.MapProvider;
 import org.genericsystem.generic.Relation;
 
 /**
  * Generic is main interface of each node of the internal graph.
  * 
  * @author Nicolas Feybesse
  * @author Michael Ory
  */
 public interface Generic extends Comparable<Generic> {
 
 	/**
 	 * Returns the root of the internal graph to which this Generic belongs.
 	 * 
 	 * @return The Engine.
 	 */
 	Engine getEngine();
 
 	/**
 	 * Returns true if this Generic is the root of the internal graph.
 	 * 
 	 * @return True if this Generic is the root.
 	 */
 	boolean isEngine();
 
 	/**
 	 * Returns true if this Generic is an instance of the specified Generic.
 	 * 
 	 * @param Generic
 	 *            The checked type.
 	 * @return True if the Generic is a instance of the type checked.
 	 */
 	boolean isInstanceOf(Generic generic);
 
 	/**
 	 * Returns instantiation level.
 	 * 
 	 * @return The instantiation level.
 	 */
 	int getMetaLevel();
 
 	/**
 	 * Returns true if this Generic is a Type.
 	 * 
 	 * @return True if the Generic is a Type.
 	 */
 	boolean isType();
 
 	/**
 	 * Returns true if this Generic is an Attribute.
 	 * 
 	 * @return True if the Generic is an Attribute.
 	 */
 	boolean isAttribute();
 
 	/**
 	 * Returns true if this Generic is an Really Attribute (no relation).
 	 * 
 	 * @return True if the Generic is an Really Attribute.
 	 */
 	boolean isReallyAttribute();
 
 	/**
 	 * Returns true if this Generic is really a Relation.
 	 * 
 	 * @return True if the Generic is really a Relation.
 	 */
 	boolean isReallyRelation();
 
 	/**
 	 * Returns true if this Generic is an Attribute for the checked Generic.
 	 * 
 	 * @param Generic
 	 *            The checked Generic.
 	 * 
 	 * @return True if the Generic is an Attribute.
 	 */
 	boolean isAttributeOf(Generic generic);
 
 	/**
 	 * Returns true if this Generic is an Attribute for the checked Generic and the component position.
 	 * 
 	 * @param Generic
 	 *            The checked Generic.
 	 * @param basePos
 	 *            The base position.
 	 * 
 	 * @return True if the Generic is an Attribute.
 	 */
 	boolean isAttributeOf(Generic generic, int basePos);
 
 	/**
 	 * Returns true if this Generic is an relation.
 	 * 
 	 * @return True if the Generic is an Relation.
 	 */
 	boolean isRelation();
 
 	/**
 	 * Returns true if this Generic has been automatically created.
 	 * 
 	 * @return Trueif this Generic has been automatically created.
 	 */
 	boolean isAutomatic();
 
 	/**
 	 * Returns the value of this Generic.
 	 * 
 	 * @return The value.
 	 */
 	<S extends Serializable> S getValue();
 
 	/**
 	 * Mark a instance of the Attribute.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @param targets
 	 *            The targets.
 	 * @return A new Generic or the existing Generic.
 	 */
 	<T extends Holder> T flag(Holder attribute, Generic... targets);
 
 	/**
 	 * Bind this with the targets.
 	 * 
 	 * @param relation
 	 *            The Relation.
 	 * @param targets
 	 *            The targets.
 	 * @return A new Generic or the existing Generic.
 	 */
 	<T extends Link> T bind(Link relation, Generic... targets);
 
 	/**
 	 * Returns the Link of the Relation for the components and the component position.
 	 * 
 	 * @param relation
 	 *            The Relation.
 	 * @param basePos
 	 *            The basePosition in targets
 	 * @param targets
 	 *            The optional targets.
 	 * @return A Link.
 	 * @throws IllegalStateException
 	 *             Ambigous request for the Relation.
 	 */
 	<T extends Link> T getLink(Link relation, int basePos, Generic... targets);
 
 	/**
 	 * Returns the Link of the Relation for the components.
 	 * 
 	 * @param relation
 	 *            The Relation.
 	 * @param basePos
 	 *            The basePosition in targets
 	 * @param targets
 	 *            The optional targets.
 	 * @return A Link.
 	 * @throws IllegalStateException
 	 *             Ambigous request for the Relation.
 	 */
 	<T extends Link> T getLink(Link relation, Generic... targets);
 
 	/**
 	 * Returns the Links.
 	 * 
 	 * @param relation
 	 *            The Relation.
 	 * @param basePos
 	 *            The base position.
 	 * @param targets
 	 *            The targets.
 	 * @see Snapshot
 	 * @return The Link.
 	 */
 	<T extends Link> Snapshot<T> getLinks(Relation relation, int basePos, Generic... targets);
 
 	/**
 	 * Returns the Link.
 	 * 
 	 * @param relation
 	 *            The Relation.
 	 * @param targets
 	 *            The targets.
 	 * @see Snapshot
 	 * @return The Link.
 	 */
 	<T extends Link> Snapshot<T> getLinks(Relation relation, Generic... targets);
 
 	/**
 	 * Creates a link or throws an exception if the link if already exists <br/>
 	 * If the Singular constraint is enabled on the property, then one link will be created on the targets.<br/>
 	 * 
 	 * @param relation
 	 *            The relation.
 	 * @param value
 	 *            The value Link.
 	 * @param targets
 	 *            The optional targets.
 	 * @return The Link.
 	 */
 	<T extends Link> T addLink(Link relation, Serializable value, Generic... targets);
 
 	/**
 	 * Creates a link or returns the link if already exists <br/>
 	 * If the Singular constraint is enabled on the property, then one link will be created on the targets.<br/>
 	 * 
 	 * @param relation
 	 *            The relation.
 	 * @param value
 	 *            The value Link.
 	 * @param targets
 	 *            The optional targets.
 	 * @return The Link.
 	 */
 	<T extends Link> T setLink(Link relation, Serializable value, Generic... targets);
 
 	/**
 	 * Creates an holder or throws an exception if this holder already exists. <br/>
 	 * If the Singular constraint is enabled on the property, then one link will be created on the targets.<br/>
 	 * 
 	 * @param attribute
 	 *            The Holder.
 	 * @param value
 	 *            The value Link.
 	 * @param targets
 	 *            The optional targets.
 	 * @return The Holder.
 	 */
 	<T extends Holder> T addHolder(Holder attribute, Serializable value, Generic... targets);
 
 	/**
 	 * Creates an holder or throws an exception if this holder already exists. <br/>
 	 * If the Singular constraint is enabled on the property, then one link will be created on the targets.<br/>
 	 * 
 	 * @param attribute
 	 *            The Holder.
 	 * @param value
 	 *            The value Link.
 	 * @param basePos
 	 *            The base position.
 	 * 
 	 * @param targets
 	 *            The optional targets.
 	 * @return The Holder.
 	 */
 	<T extends Holder> T addHolder(Holder attribute, int basePos, Serializable value, Generic... targets);
 
 	/**
 	 * Creates an holder or returns this holder if already exists. <br/>
 	 * If the Singular constraint is enabled on the property, then one link will be created on the targets.<br/>
 	 * 
 	 * @param attribute
 	 *            The Holder.
 	 * @param value
 	 *            The value Link.
 	 * @param targets
 	 *            The optional targets.
 	 * @return The Holder.
 	 */
 	<T extends Holder> T setHolder(Holder attribute, Serializable value, Generic... targets);
 
 	/**
 	 * Creates an holder or returns this holder if already exists. <br/>
 	 * If the Singular constraint is enabled on the property, then one link will be created on the targets.<br/>
 	 * 
 	 * @param attribute
 	 *            The Holder.
 	 * @param value
 	 *            The value Link.
 	 * @param basePos
 	 *            The position of this in components
 	 * @param targets
 	 *            The optional targets.
 	 * @return The holder.
 	 */
 	<T extends Holder> T setHolder(Holder attribute, Serializable value, int basePos, Generic... targets);
 
 	/**
 	 * Returns the targets of the Relation.
 	 * 
 	 * @param relation
 	 *            The relation.
 	 * @see Snapshot
 	 * @return The targets.
 	 */
 	<T extends Generic> Snapshot<T> getTargets(Relation relation);
 
 	/**
 	 * Returns the targets of the Relation.
 	 * 
 	 * @param relation
 	 *            The relation.
 	 * @param targetPos
 	 *            The target component position.
 	 * 
 	 * @see Snapshot
 	 * @return The targets.
 	 */
 	<T extends Generic> Snapshot<T> getTargets(Relation relation, int targetPos);
 
 	/**
 	 * Returns the values holders.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @param basePos
 	 *            The base position.
 	 * @param targets
 	 *            The targets.
 	 * @see Snapshot
 	 * @return The value holders.
 	 */
 	<T extends Holder> Snapshot<T> getHolders(Holder attribute, int basePos, Generic... targets);
 
 	/**
 	 * Returns the values holders.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @param targets
 	 *            The targets.
 	 * @see Snapshot
 	 * @return The value holders.
 	 */
 	<T extends Holder> Snapshot<T> getHolders(Holder attribute, Generic... targets);
 
 	/**
 	 * Returns the Holder of value.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @param basePos
 	 *            The base position.
 	 * @param targets
 	 *            The targets.
 	 * @return The Holder.
 	 */
 	<T extends Holder> T getHolder(Holder attribute, int basePos, Generic... targets);
 
 	/**
 	 * Returns the Holder of value.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @return The Holder.
 	 */
 	<T extends Holder> T getHolder(Holder attribute, Generic... targets);
 
 	/**
 	 * Returns the values.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @see Snapshot
 	 * @return The values.
 	 */
 	<T extends Serializable> Snapshot<T> getValues(Holder attribute);
 
 	/**
 	 * Returns the value of the attribute.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @return The value.
 	 */
 	<S extends Serializable> S getValue(Holder attribute);
 
 	/**
 	 * Creates an holder or throws an exception if this holder already exists. <br/>
 	 * If the Singular constraint is enabled on the attribute, then one value will be created.<br/>
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @param value
 	 *            The name value.
 	 * @return The value holder.
 	 */
 	<T extends Holder> T addValue(Holder attribute, Serializable value);
 
 	/**
 	 * Creates an holder or return this holder if this holder already exists. <br/>
 	 * If the Singular constraint is enabled on the attribute, then one value will be created.<br/>
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @param value
 	 *            The name value.
 	 * @return The value holder.
 	 */
 	<T extends Holder> T setValue(Holder attribute, Serializable value);
 
 	/**
 	 * Returns true if the Generic inherits from the given Generic.
 	 * 
 	 * @param Generic
 	 *            The checked Generic.
 	 * @return True if the Generic inherits from the given Generic.
 	 */
 	boolean inheritsFrom(Generic generic);
 
 	/**
 	 * Returns true if the Generic inherits from all the given Generic.
 	 * 
 	 * @param generics
 	 *            The given Generic.
 	 * @return True if the Generic inherits from all the given Generic.
 	 */
 	boolean inheritsFromAll(Generic... generics);
 
 	/**
 	 * Remove the Generic.
 	 */
 	void remove();
 
 	/**
 	 * Returns true if the Generic is alive
 	 * 
 	 * @return True if the Generic is alive.
 	 */
 	boolean isAlive();
 
 	/**
 	 * Enable referential integrity for component position.
 	 * 
 	 * 
 	 * @param componentPos
 	 *            The component position implicated by the constraint.
 	 * 
 	 * @return This.
 	 */
 	<T extends Generic> T enableReferentialIntegrity(int componentPos);
 
 	/**
 	 * Disable referential integrity for component position.
 	 * 
 	 * @param componentPos
 	 *            The component position implicated by the constraint.
 	 * 
 	 * @return This.
 	 */
 	<T extends Generic> T disableReferentialIntegrity(int componentPos);
 
 	/**
 	 * Returns true if the referential integrity is enabled for component position.
 	 * 
 	 * @param componentPos
 	 *            The component position implicated by the constraint.
 	 * @return True if the referential integrity is enabled.
 	 */
 	boolean isReferentialIntegrity(int componentPos);
 
 	/**
 	 * Returns the implicit.
 	 * 
 	 * @return The implicit.
 	 */
 	<T extends Generic> T getImplicit();
 
 	/**
 	 * Returns the supers of the Generic.
 	 * 
 	 * @see Snapshot
 	 * @return The supers.
 	 */
 	<T extends Generic> Snapshot<T> getSupers();
 
 	/**
 	 * Returns the components of the Generic.
 	 * 
 	 * @see Snapshot
 	 * @return The components.
 	 */
 	<T extends Generic> Snapshot<T> getComponents();
 
 	/**
 	 * Returns the size of components.
 	 * 
 	 * @return The size of components.
 	 */
 	int getComponentsSize();
 
 	/**
 	 * Returns the position of the base component.
 	 * 
 	 * @param attribute
 	 *            The attribute.
 	 * @return The position.
 	 */
 	int getBasePos(Holder attribute);
 
 	/**
 	 * Returns the size of supers.
 	 * 
 	 * @return The size of supers.
 	 */
 	int getSupersSize();
 
 	/**
 	 * Create a new anonymous instance.
 	 * 
 	 * @param components
 	 *            The components.
 	 * @return The new Generic.
 	 */
 	<T extends Generic> T newAnonymousInstance(Generic... components);
 
 	/**
 	 * Create a new instance or get the instance if it already exists.
 	 * 
 	 * @param value
 	 *            The value.
 	 * @param components
 	 *            The components.
 	 * @return The new Generic.
 	 */
 	<T extends Generic> T newInstance(Serializable value, Generic... components);
 
 	/**
 	 * Return the meta.
 	 * 
 	 * @return The meta.
 	 */
 	<T extends Generic> T getMeta();
 
 	/**
 	 * Returns the inheritings Generic.
 	 * 
 	 * @see Snapshot
 	 * @return The inheritings Generic.
 	 */
 	<T extends Generic> Snapshot<T> getInheritings();
 
 	/**
 	 * Returns the composites Generic.
 	 * 
 	 * @see Snapshot
 	 * @return The composites Generic.
 	 */
 	<T extends Generic> Snapshot<T> getComposites();
 
 	/**
 	 * Returns true if the Generic is structural.
 	 * 
 	 * @return True if the Generic is structural.
 	 */
 	boolean isStructural();
 
 	/**
 	 * Returns true if the Generic is concrete.
 	 * 
 	 * @return True if the Generic is concrete.
 	 */
 	boolean isConcrete();
 
 	/**
 	 * Returns true if the Generic is meta.
 	 * 
 	 * @return True if the Generic is meta.
 	 */
 	boolean isMeta();
 
 	/**
 	 * Returns true if the Generic is tree.
 	 * 
 	 * @return True if the Generic is tree.
 	 */
 	boolean isTree();
 
 	/**
 	 * Returns true if the Generic is root.
 	 * 
 	 * @return True if the Generic is root.
 	 */
 	boolean isRoot();
 
 	/**
 	 * Returns true if the Generic is removable.
 	 * 
 	 * 
 	 * @return True if the Generic is removable.
 	 */
 	boolean isRemovable();
 
 	/**
 	 * Log with slf4j.<br/>
 	 * Call the info() method.
 	 */
 	void log();
 
 	/**
 	 * Returns all available information except linkage information.
 	 * 
 	 * @return all available information except linkage information.
 	 */
 	String info();
 
 	void clearAllStructural(Holder attribute, Generic... targets);
 
 	void clearAllStructural(Holder attribute, int basePos, Generic... targets);
 
 	void clearAllConcrete(Holder attribute, Generic... targets);
 
 	void clearAllConcrete(Holder attribute, int basePos, Generic... targets);
 
 	void cancelAll(Holder attribute, boolean concrete, Generic... targets);
 
 	void cancelAll(Holder attribute, int basePos, boolean concrete, Generic... targets);
 
 	<T extends Generic> T cancel(Holder attribute, boolean concrete, Generic... targets);
 
 	<T extends Generic> T cancel(Holder attribute, int basePos, boolean concrete, Generic... targets);
 
 	<T extends MapProvider> Map<Serializable, Serializable> getMap(Class<T> mapClass);
 
 	Map<Serializable, Serializable> getProperties();
 
	ExtendedMap<Serializable, Serializable> getContraints();
 
 	interface ExtendedMap<K, V> extends Map<K, V> {
 		Holder getValueHolder(Serializable key);
 
 		// @Deprecated
 		// Holder getKeyHolder(K key);
 		//
 		// @Deprecated
 		// <T extends Generic> T getKeyBaseComponent(K key);
 	}
 
 	<T extends Generic> T addComponent(int pos, Generic newComponent);
 
 	<T extends Generic> T removeComponent(int pos, Generic newComponent);
 
 	<T extends Generic> T addSuper(Generic newSuper);
 
 	<T extends Generic> T removeSuper(int pos);
 
 	<T extends Generic> T updateValue(Serializable value);
 
 	Snapshot<Structural> getStructurals();
 
 	<T extends Generic> Snapshot<T> getOtherTargets(Holder holder);
 
 	void removeHolder(Holder holder);
 
 	<T extends Holder> Snapshot<T> getHolders(Holder attribute, boolean readPhantoms, Generic... targets);
 
 	<T extends Holder> Snapshot<T> getHolders(Holder attribute, int basePos, boolean readPhantoms, Generic... targets);
 
 	void removePhantoms(Attribute attribute);
 }
