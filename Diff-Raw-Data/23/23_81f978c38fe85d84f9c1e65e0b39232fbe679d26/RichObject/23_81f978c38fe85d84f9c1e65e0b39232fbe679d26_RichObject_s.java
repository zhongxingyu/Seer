 package uk.org.lidalia.lang;
 
 import static com.google.common.base.Objects.equal;
 import static com.google.common.collect.Iterables.transform;
 import static java.security.AccessController.doPrivileged;
 import static java.util.Arrays.asList;
 import static uk.org.lidalia.lang.Classes.inSameClassHierarchy;
 import static uk.org.lidalia.lang.Exceptions.throwUnchecked;
 
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.security.PrivilegedAction;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 
 public class RichObject {
 
     private static final int PRIME = 37;
     private static final int INITIAL_HASHCODE_VALUE = 17;
 
     private static final LoadingCache<Class<? extends RichObject>, ImmutableSet<Field>> IDENTITY_FIELDS =
             CacheBuilder.newBuilder().weakKeys().softValues().build(new IdentityFieldLoader());
     private static final Joiner FIELD_JOINER = Joiner.on(",");
 
     private static Set<Field> getIdentityFields(Class<? extends RichObject> aClass) {
         try {
             return IDENTITY_FIELDS.get(aClass);
         } catch (ExecutionException e) {
             return throwUnchecked(e.getCause(), null);
         }
     }
 
     @Override public final boolean equals(final Object other) {
         // Usual equals checks
         if (other == this) {
             return true;
         }
         if (other == null) {
             return false;
         }
 
         // One of the two must be a subtype of the other
         if (!inSameClassHierarchy(this.getClass(), other.getClass())) {
             return false;
         }
 
         // They must have precisely the same set of identity members to meet the
         // symmetric & transitive requirement of equals
         final Set<Field> thisFields = getIdentityFields(this.getClass());
         if (thisFields.size() == 0) {
             return false;
         }
         final Set<Field> otherFields = getIdentityFields((Class<? extends RichObject>) other.getClass());
         if (!thisFields.equals(otherFields)) {
             return false;
         }
 
         try {
             for (final Field field : thisFields) {
                 if (!equal(field.get(other), field.get(this))) {
                     return false;
                 }
             }
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("All fields should be accessible", e);
         }
         return true;
     }
 
     private volatile int hashCode = -1;
 
     @Override public int hashCode() {
 
         if (hashCode != -1) {
             return hashCode;
         }
 
         int result = INITIAL_HASHCODE_VALUE;
         try {
             final Class<? extends RichObject> aClass = this.getClass();
             final Set<Field> thisFields = getIdentityFields(aClass);
             for (final Field field : thisFields) {
                 final Object value = field.get(this);
                 final int toAdd;
                 if (value == null) {
                     toAdd = 0;
                 } else {
                     toAdd = value.hashCode();
                 }
                 result = PRIME * result + toAdd;
             }
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("All fields should be accessible", e);
         }
         if (this instanceof Immutable) {
             hashCode = result;
         }
         return result;
     }
 
     private volatile String toString = null;
 
     @Override public String toString() {
 
         if (toString != null) {
             return toString;
         }
 
         final Set<Field> thisFields = getIdentityFields(this.getClass());
         if (thisFields.isEmpty()) {
             return super.toString();
         }
 
         final StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
         final Iterable<String> fieldsAsStrings = transform(thisFields, new Function<Field, String>() {
             @Override
             public String apply(Field field) {
                 try {
                     return field.getName() + "=" + field.get(RichObject.this);
                 } catch (IllegalAccessException e) {
                     throw new IllegalStateException("All fields should be accessible", e);
                 }
             }
         });
         builder.append("[");
         FIELD_JOINER.appendTo(builder, fieldsAsStrings).append("]");
 
         final String result = builder.toString();
         if (this instanceof Immutable) {
             toString = result;
         }
         return result;
     }
 
     public boolean instanceOf(Class<?> possibleSuperType) {
         return possibleSuperType.isInstance(this);
     }
 
     private static class IdentityFieldLoader extends CacheLoader<Class<?>, ImmutableSet<Field>> {
         @Override
         public ImmutableSet<Field> load(Class<?> key) throws RichException {
             final ImmutableSet<Field> localIdentityFieldSet = FluentIterable.from(asList(key.getDeclaredFields()))
                    .filter(new IsIdentityField())
                     .toImmutableSet();
            final ImmutableSet<Field> superIdentityFieldSet = load(key.getSuperclass());
             return ImmutableSet.copyOf(Sets.union(localIdentityFieldSet, superIdentityFieldSet));
         }
 
         private static class IsIdentityField implements Predicate<Field> {
             @Override
             public boolean apply(final Field field) {
                 final boolean isIdentityField = field.isAnnotationPresent(Identity.class);
                 if (isIdentityField && !Modifier.isPublic(field.getModifiers())) {
                     doPrivileged(new SetAccessibleAction(field));
                 }
                 return isIdentityField;
             }
 
             private static class SetAccessibleAction implements PrivilegedAction<Void> {
                 private final AccessibleObject member;
 
                 public SetAccessibleAction(AccessibleObject member) {
                     this.member = member;
                 }
 
                 @Override
                 public Void run() {
                     member.setAccessible(true);
                     return null;
                 }
             }
         }
     }
 }
