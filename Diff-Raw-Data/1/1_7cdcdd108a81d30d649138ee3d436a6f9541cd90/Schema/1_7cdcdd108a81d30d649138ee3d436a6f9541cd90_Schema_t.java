 package com.bluespot.forms;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.bluespot.logic.Visitors;
 import com.bluespot.logic.adapters.AbstractHandledAdapter;
 import com.bluespot.logic.adapters.HandledAdapter;
 import com.bluespot.logic.predicates.AdaptingPredicate;
 import com.bluespot.logic.predicates.Predicate;
 import com.bluespot.logic.visitors.Sentinel;
 import com.bluespot.logic.visitors.Visitor;
 
 /**
  * A schema is a type-safe bridge between arbitrary forms of submission and the
  * visitors that expect and act on them.
  * 
  * @author Aaron Faanes
  * 
  * @param <K>
  *            the type of key used by this schema. This allows clients to use
  *            enumerations or other limited objects to define keys, making it
  *            easier to produce valid submission objects. Of course, you are
  *            free to use any type, like a string or simply different objects.
  *            The only requirement is that, since they act as keys in a map,
  *            they should implement {@link #equals(Object)} appropriately.
  * 
  * @see Submission
  */
 public final class Schema<K> {
 
     private final Map<? extends K, Class<?>> types;
     private final Predicate<? super Submission<K>> predicate;
 
     private final Predicate<? super Submission<? super K>> typePredicate = new SchemaTypePredicate<K>(this);
 
     /**
      * Constructs a {@link Schema}. The specified type map defines the fields
      * that are potentially used by this schema's predicate. The map also
      * specifies the expected supertype of the field.
      * <p>
      * The specified predicate
      * 
      * @param types
      *            a map that defines the fields that are potentially accessed by
      *            this schema's predicate. Specifically, it will relate a given
      *            key value to its expected supertype. It may not contain null
      *            as a key, and may not contain null as a value for any key.
      * 
      * @param predicate
      *            the predicate that specifies the rules of this schema. It
      *            should only access fields that are specified in {@code types}.
      *            Beyond this, no restrictions are given: this predicate may be
      *            as complex as is necessary.
      * @throws NullPointerException
      *             if either {@code types} or {@code predicate} is null, if
      *             {@code types} contains null as a key, or if a value for a
      *             given key in {@code types} is null.
      * @throws IllegalArgumentException
      *             if {@code types} contains no keys. Since the specified
      *             predicate should only use fields that are specified by
      *             {@code types}, this means that the schema performs no real
      *             validation. Such degenerate schemas are not allowed.
      */
     public Schema(final Map<? extends K, Class<?>> types, final Predicate<? super Submission<K>> predicate) {
         if (types == null) {
             throw new NullPointerException("types is null");
         }
         if (predicate == null) {
             throw new NullPointerException("predicate is null");
         }
         this.types = Collections.unmodifiableMap(new HashMap<K, Class<?>>(types));
         if (this.types.isEmpty()) {
             throw new IllegalArgumentException("types is empty");
         }
         if (this.types.containsKey(null)) {
             throw new NullPointerException("types contains null key");
         }
         for (final Entry<? extends K, Class<?>> entry : this.types.entrySet()) {
             if (entry.getValue() == null) {
                 throw new NullPointerException("types contains null value for key '" + entry.getKey() + "'");
             }
         }
         this.predicate = predicate;
     }
 
     /**
      * Returns a unmodifiable map that describes the classes expected for the
      * fields used by this schema.
      * 
      * @return a map that describes the types expected by this schema. It may
      *         not be modified.
      */
     public Map<? extends K, Class<?>> getTypes() {
         // Types is already unmodifiable, so there's no need to use
         // Collections#unmodifiableMap.
         return this.types;
     }
 
     /**
      * Returns a {@link Predicate} that checks for type safety a given
      * {@link Submission}. It will evaluate to {@code true} if, and only if, the
      * {@code Submission}'s fields' types are compatible with the types expected
      * by this schema.
      * 
      * @return a {@code Predicate} that validates a given {@link Submission}. It
      *         returns {@code true} for all {@code Submission} objects that are
      *         type-safe for this schema.
      */
     public Predicate<? super Submission<? super K>> getTypePredicate() {
         return this.typePredicate;
     }
 
     /**
      * Returns a {@link Predicate} that represents the rules of this schema. A
      * given {@link Submission} will evaluate to {@code true} if, and only if,
      * the {@code Submission}'s fields validate according to this predicate.
      * <p>
      * The only rule for this predicate is that it must use fields that are
      * specified by this schema's {@link #getTypePredicate() type predicate};
      * it, however, does not need to use all the fields provided by that type
      * predicate. Beyond this, the predicate is intentionally undefined.
      * 
      * @return a {@code Predicate} that represents the rules of this schema. The
      *         conditions in which it evaluates to {@code true} are not
      *         specified.
      */
     public Predicate<? super Submission<K>> getPredicate() {
         return this.predicate;
     }
 
     /**
      * Constructs a {@link Sentinel} that is guarded by this schema's predicate.
      * If the predicate evaluates to {@code true}, the specified visitor will be
      * invoked.
      * 
      * @param visitor
      *            the invoked visitor for all validated submissions
      * @return a new {@code Sentinel} that guards the specified visitor with
      *         this schema's predicate
      */
     public Sentinel<Submission<K>> newSentinel(final Visitor<? super Submission<? extends K>> visitor) {
         return new Sentinel<Submission<K>>(this.getPredicate(), visitor);
     }
 
     /**
      * Helper method that calls {@link #newCheckedSentinel(Visitor, Visitor)}
      * using {@link Visitors#throwException()} as the handler.
      * <p>
      * See {@link #newCheckedSentinel(Visitor, Visitor)} for full information on
      * the requirements of this class.
      * 
      * @param visitor
      *            the visitor that is guarded by the returned sentinel
      * @return a sentinel that guards the specified visitor.
      */
     public Sentinel<Submission<K>> newCheckedSentinel(final Visitor<? super Submission<? extends K>> visitor) {
         return this.newCheckedSentinel(visitor, Visitors.throwException());
     }
 
     /**
      * Returns a {@link Sentinel} that guards the specified visitor. This is
      * similar to {@link #newSentinel(Visitor)}, but is a two-step process. Each
      * step must succeed before the next begins:
      * <ul>
      * <li><em>Type validation</em>: A given {@link Submission} will be checked
      * for type-safety. Specifically, each field will be queried to ensure that
      * its type is a subtype of this schema's expected type for that field.
      * <li><em>Validation</em>: The {@code Submission} will be checked by this
      * schema's predicate, exactly like {@link #newSentinel(Visitor)}.
      * </ul>
      * If both steps of validation are successful, the submission is passed to
      * the specified {@code visitor}.
      * 
      * @param visitor
      *            the visitor that will receive all {@code Submission} objects
      *            that pass validation, as described above
      * @param handler
      *            the handler that will receive
      *            {@link SubmissionClassCastException} objects for every field
      *            in the submission that is not type-safe with this schema
      * @return a {@code Sentinel} that performs the process described above
      * @throws NullPointerException
      *             if either argument is null. Use {@link Visitors#noop()} if
      *             you don't wish to respond to fields that are not type-safe.
      *             Of course, a no-op visitor does not affect the rules of
      *             validation.
      */
     public Sentinel<Submission<K>> newCheckedSentinel(final Visitor<? super Submission<? extends K>> visitor,
             final Visitor<? super SubmissionClassCastException> handler) {
         if (visitor == null) {
             throw new NullPointerException("visitor is null");
         }
         if (handler == null) {
             throw new NullPointerException("handler is null");
         }
         final SubmissionTypeChecker<K> checker = new SubmissionTypeChecker<K>(this);
         checker.setHandler(handler);
         final Predicate<Submission<K>> checkedPredicate = new AdaptingPredicate<Submission<K>, Submission<K>>(checker,
                 this.getPredicate());
         return new Sentinel<Submission<K>>(checkedPredicate, visitor);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (!(obj instanceof Schema<?>)) {
             return false;
         }
         final Schema<?> other = (Schema<?>) obj;
         if (!this.getPredicate().equals(other.getPredicate())) {
             return false;
         }
         if (!this.getTypes().equals(other.getTypes())) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = 17;
         result = 31 * result + this.getPredicate().hashCode();
         result = 31 * result + this.getTypes().hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         return String.format("Schema[%s]", this.getPredicate());
     }
 
     /**
      * A {@link HandledAdapter} implementation that ensures a given submission
      * is compatible for the specified {@link Schema}.
      * 
      * @author Aaron Faanes
      * 
      * @param <K>
      *            the type of key used in the submission
      */
     private static final class SubmissionTypeChecker<K> extends
             AbstractHandledAdapter<Submission<K>, Submission<K>, SubmissionClassCastException> {
 
         private final Schema<? extends K> schema;
 
         public SubmissionTypeChecker(final Schema<? extends K> schema) {
             assert schema != null : "schema is null";
             this.schema = schema;
         }
 
         public Schema<? extends K> getSchema() {
             return this.schema;
         }
 
         @Override
         public Submission<K> adapt(final Submission<K> submission) {
             if (submission == null) {
                 return null;
             }
             boolean acceptable = true;
             for (final Entry<? extends K, Class<?>> entry : this.getSchema().getTypes().entrySet()) {
                 final Class<?> requiredType = entry.getValue();
                 final Class<?> candidate = submission.getType(entry.getKey());
                 if (candidate == null || !requiredType.isAssignableFrom(candidate)) {
                     acceptable = false;
                     this.dispatch(new SubmissionClassCastException(entry.getKey(), requiredType, candidate));
                 }
             }
             /*
              * If we failed our validation, return null.
              */
             return acceptable ? submission : null;
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
             if (!(obj instanceof SubmissionTypeChecker<?>)) {
                 return false;
             }
             final SubmissionTypeChecker<?> other = (SubmissionTypeChecker<?>) obj;
             if (!this.getSchema().equals(other.getSchema())) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = 7;
             result = 31 * result + this.getSchema().hashCode();
             return result;
         }
 
         @Override
         public String toString() {
             return String.format("has valid types using schema '%s'", this.getSchema());
         }
 
     }
 
     /**
      * An {@link ClassCastException} that is used whenever a {@link Submission}
      * returns a value that is not of the expected type. This exception
      * subclasses {@code ClassCastException} to provide useful information for
      * handlers.
      * 
      * @author Aaron Faanes
      * 
      */
     public static final class SubmissionClassCastException extends ClassCastException {
 
         private static final long serialVersionUID = 3724801437060534108L;
 
         private final Class<?> actualType;
         private final Class<?> expectedType;
         private final Object key;
 
         /**
          * Constructs a {@link SubmissionClassCastException} using the given
          * arguments.
          * 
          * @param key
          *            the key that caused the exception
          * @param expectedType
          *            the expected type for the given key
          * @param actualType
          *            the actual type that was received. This may be null.
          * @throws NullPointerException
          *             if {@code key} or {@code expectedType}. {@code
          *             actualType} is allowed to be null. This can occur if null
          *             was returned as a value.
          */
         public SubmissionClassCastException(final Object key, final Class<?> expectedType, final Class<?> actualType) {
             if (key == null) {
                 throw new NullPointerException("key is null");
             }
             if (expectedType == null) {
                 throw new NullPointerException("expectedType is null");
             }
             this.key = key;
             this.expectedType = expectedType;
             this.actualType = actualType;
             if (this.actualType != null) {
                 if (this.expectedType.isAssignableFrom(this.actualType)) {
                     throw new IllegalStateException("Cast is safe");
                 }
             }
         }
 
         /**
          * Returns the key that was the site of an illegal cast.
          * 
          * @return the key that was the site of an illegal cast
          */
         public Object getKey() {
             return this.key;
         }
 
         /**
          * Returns the expected type for the schema's key.
          * 
          * @return the expected supertype for the schema's key
          */
         public Class<?> getExpectedType() {
             return this.expectedType;
         }
 
         /**
          * Returns the actual type received for the specified key. This may be
          * null and would indicate null was either directly returned, or there
          * was no corresponding value in the {@link Submission}.
          * 
          * @return the actual type received for the specified key.
          */
         public Class<?> getActualType() {
             return this.actualType;
         }
 
         @Override
         public String toString() {
             if (this.getActualType() == null) {
                 return String.format("For %s, expected type was '%s', but received a null argument.", this.getKey(),
                         this.getExpectedType());
             }
             return String.format("For %s, expected type was '%s', but received '%s'", this.getKey(),
                     this.getExpectedType(), this.getActualType());
         }
     }
 
     /**
      * A {@link Predicate} that tests a given submission for the correct types.
      * 
      * @author Aaron Faanes
      * 
      * @param <T>
     *            the type used by the given submission
      */
     private static final class SchemaTypePredicate<T> implements Predicate<Submission<? super T>> {
 
         private final Schema<T> schema;
 
         public SchemaTypePredicate(final Schema<T> schema) {
             assert schema != null : "schema is null";
             this.schema = schema;
         }
 
         public Schema<T> getSchema() {
             return this.schema;
         }
 
         public boolean test(final Submission<? super T> submission) {
             if (submission == null) {
                 return false;
             }
             for (final Entry<? extends T, Class<?>> entry : this.getSchema().getTypes().entrySet()) {
                 final Class<?> requiredType = entry.getValue();
                 final Class<?> candidate = submission.getType(entry.getKey());
                 if (candidate == null) {
                     return false;
                 }
                 if (!requiredType.isAssignableFrom(candidate)) {
                     return false;
                 }
             }
             return true;
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
             if (!(obj instanceof SchemaTypePredicate<?>)) {
                 return false;
             }
             final SchemaTypePredicate<?> other = (SchemaTypePredicate<?>) obj;
             if (!this.getSchema().equals(other.getSchema())) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = 3;
             result = 31 * result + this.getSchema().hashCode();
             return result;
         }
 
         @Override
         public String toString() {
             return String.format("SchemaTypePredicate[%s]", this.getSchema());
         }
 
     }
 
 }
