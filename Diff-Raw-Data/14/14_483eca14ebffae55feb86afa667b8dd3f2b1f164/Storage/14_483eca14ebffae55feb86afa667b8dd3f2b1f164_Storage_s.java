 package ca.hullabaloo.content.api;
 
 /**
  * This is a system for content storage which uses java objects as the content definition.
  * Objects are defined in a restricted forms so they can be easily mapped to key-value
  * pairs for storage.
  * <p/>
  * The storage system will segment an object by the types it implements and store properties
  * defined on the same type together.  For example, if you have types Name{name,desc},
  * Tagged{list tags}, and Document{various} extends Named, Tagged, the system will store
  * three JSON objects.  Queries will load from only the required fractions.
  * <p/>
  * To be stored in system, types must conform to a subset of the java type system
  * <u>
  * <li>They must be interfaces.  Implementations are provided by the system.</li>
  * <li>They must have only zero-arg methods; none mutate</li>
  * <li>Restricted return types: primitives & wrappers, String,
  * Lists and Sets of objects that conform to these same restrictions.</li>
  * <li>No reference to any other stored type since everything is de-normalized and
  * stored by value.</li>
  * <li>oh, and no methods may start with 'get'.  Author's preference.</li>
  * </u>
  * This is a storage model expressed in Java code; it's not your domain model.
  * <p></p>
  * Key concepts and their definitions:
  * <ol>
  * <li><b>Whole Type:</b> These are the objects you're storing.  They most likely
  * "mean something" to users of your application and are intended to be quite coarse
  * grained.</li>
  * <li>Fraction Type: These are portions of a whole type which are stored separately.</li>
  * <li>View Type: This is a type which serves as a query result.  These types are
 * not stored in the system.  Of course, you can query for whole or fraction types.</li>
  * </ol>
  * Additional restrictions
  * <ol>
  * <li>Every type must implement {@link Identified}.</li>
  * <li>No whole type may extend another whole type</li>
  * </ol>
  */
 public interface Storage {
   String ID_METHOD_NAME = "id";
 
   <T extends Identified> void register(Class<T> type);
 
   <T> Loader<T> loader(Class<T> resultType);
 
   <T> Query<T> query(Class<T> resultType);
 
   WorkUnit begin();
 }
