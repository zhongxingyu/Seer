 package com.anasoft.os.daofusion.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.Embeddable;
 import javax.persistence.EmbeddedId;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.IdClass;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Version;
 
 import com.anasoft.os.daofusion.PersistentEntityDao;
 
 /**
  * Generic persistent entity contract used
  * by the {@link PersistentEntityDao}.
  * 
  * <p>
  * 
  * Implementations of this interface must follow some
  * general rules regarding JPA object-relational mapping
  * concepts:
  * 
  * <ul>
  * 	<li>Each persistent entity must specify database
  * 		identity field(s) which will be reflected as
  * 		primary key(s) within the underlying database
  * 		table. There are basically two ways to manage
  * 		the database identity:
  * 		<ul>
  * 			<li><em>simple</em> primary key approach -
  * 				use the {@link Id} annotation to mark
  * 				a single <tt>id</tt> field to be used
  * 				as the primary key. It is common to mark
  * 				the <tt>id</tt> field using the
  * 				{@link GeneratedValue} annotation as well
  * 				in case the identifier should be
  * 				automatically assigned upon persistent
  * 				entity creation.
  * 			<li><em>composite</em> primary key approach -
  * 				use the {@link Embeddable} annotation to
  * 				mark the primary key class and join it
  * 				with the persistent entity using {@link IdClass}
  * 				and {@link Id} annotations. In case of necessary
  * 				primary key field association overrides, it is
  * 				advised to use {@link Embeddable} and
  * 				{@link EmbeddedId} annotations instead.
  * 		</ul>
  * 	<li>Mutable persistent entities should use the
  * 		{@link Version} annotation to mark an optimistic
  * 		lock value (version) field. This way, the underlying
  * 		persistence provider is able to enforce optimistic
  * 		locking strategy on the persistent entity.
  * 	<li>Each persistent entity must provide a <b><u>reliable</u></b>
 * 		{@link Object#hashCode()} and {@link Object#equals(Object)}
  * 		implementation which covers all possible entity states.
  * </ul>
  * 
  * There are basically two approaches to implementing
 * {@link Object#hashCode()} and {@link Object#equals(Object)}
  * methods:
  * 
  * <br><br>
  * 
  * <ul>
  * 	<li><em>business key</em> approach - set of "semi"-unique
  * 		attributes that uniquely describe the persistent
  * 		entity as a whole (this attribute combination is
  * 		unique for each persistent entity instance). However,
  * 		this imposes certain limitations on attributes within
  * 		the set which could potentionally compromise the
 * 		{@link Object#hashCode()} and {@link Object#equals(Object)}
  * 		method contract (for example, uniqueness might be
  * 		broken if any of the "semi"-unique attributes change,
  * 		persistent entity instance has to have all "semi"-unique
 * 		attributes set in advance for {@link Object#hashCode()} and
 * 		{@link Object#equals(Object)} to work properly, etc.).
  * 	<li><em>synthetic generated value</em> approach - generate
  * 		a globally-unique identifier value and assign it to
  * 		the <tt>oid</tt> field upon the persistent entity
  * 		instance creation. The <tt>oid</tt> field serves as
  * 		an object identity, allowing the entity to be contained
  * 		within collections and properly handled between multiple
  * 		persistence sessions. This approach essentially tries to
  * 		separate the object identity and object value concepts
  * 		from each other (unlike the <em>business key</em>
  * 		approach which combines them together).
  * </ul>
  * 
  * As for the <em>inheritance mapping strategy</em>, it is up
  * to the user to mark specific root nodes within the persistent
  * entity hierarchy via the {@link Inheritance} annotation
  * as appropriate. Keep in mind that the default JPA inheritance
  * strategy is set to {@link InheritanceType#SINGLE_TABLE} along
  * with its implications on the underlying database schema.
  * 
  * @param <ID> Java type of the primary key column.
  * 
  * @see PersistentEntityDao
  * 
  * @author vojtech.szocs
  */
 public interface Persistable<ID extends Serializable> extends Serializable {
 
 }
