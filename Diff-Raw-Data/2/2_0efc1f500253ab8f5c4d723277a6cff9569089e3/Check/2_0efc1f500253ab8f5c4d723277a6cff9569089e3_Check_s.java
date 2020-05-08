 package ch.trick17.betterchecks;
 
 import static ch.trick17.betterchecks.Exceptions.illegalArgumentException;
 import static ch.trick17.betterchecks.Exceptions.illegalStateException;
 
 import java.net.URL;
 import java.util.Collection;
 
 import ch.trick17.betterchecks.fluent.CollectionCheck;
 import ch.trick17.betterchecks.fluent.DoubleCheck;
 import ch.trick17.betterchecks.fluent.FluentChecks;
 import ch.trick17.betterchecks.fluent.IntCheck;
 import ch.trick17.betterchecks.fluent.LongCheck;
 import ch.trick17.betterchecks.fluent.NumberCheck;
 import ch.trick17.betterchecks.fluent.ObjectArrayCheck;
 import ch.trick17.betterchecks.fluent.ObjectCheck;
 import ch.trick17.betterchecks.fluent.PrimitiveArrayCheck;
 import ch.trick17.betterchecks.fluent.StringCheck;
 import ch.trick17.betterchecks.fluent.UrlCheck;
 
 /**
  * This is the primary entry point to the Better Checks library. Its intention
 * is to provide a lightweight and compact, but powerful way for precodition
  * checking, in particular for method arguments. Argument checks are written in
  * a fluent way like this:
  * <p>
  * <code>Check.that(<em>argument</em>).<em>check1</em>().<em>check2</em>()<em>...</em>;</code>
  * <p>
  * Examples:
  * <p>
  * <code>Check.that(name).matches("hello .*!").hasLenghtBetween(0, 20);</code><br>
  * <code>Check.that(list).isNullOr().hasSize(0);</code><br>
  * <code>Check.that(args).named("arguments").isNotEmpty();</code>
  * <p>
  * The actual checking methods (such as <code>matches(...)</code> or
  * <code>hasSize(...)</code>) all throw an exception if the check fails. The
  * exact type of exception depends on the kind of check that is called but in
  * most cases it is {@link IllegalArgumentException}.
  * <h3>Check Objects</h3>
  * <p>
  * There are various overloaded variants of the <code>that(...)</code> method,
  * each one returning a *Check object that suits the argument's type. For
  * instance, if you pass a {@link String}, you will get a {@link StringCheck}
  * object, with methods like {@link StringCheck#isNotEmpty()},
  * {@link StringCheck#hasLength(int)} or {@link StringCheck#matches(String)}.
  * <p>
  * If there is no specific *Check class suiting the passed argument, a standard
  * {@link ObjectCheck} is returned, supporting only
  * {@link ObjectCheck#isNotNull()} and the state-modifying methods.
  * <h3>Check Modification</h3>
  * <p>
  * In addition to the checking methods, the *Check objects provide some modifier
  * methods that affect the subsequent checks. For example all checks by default
  * also check that the argument is not <code>null</code>, throwing an exception
  * if it is. To allow <code>null</code> as an accepted value, you can prepend
  * the actual checks with <code>isNullOr()</code>, like in the second example
  * above.
  * <p>
  * It is also possible to name the arguments that you are checking. By doing
  * this, the exception messages will be more meaningful and debugging becomes
  * easier. To name an argument, prepend all checks with
  * <code>named("<em>argument name</em>")</code>, just like in the third example
  * at the top.
  * <p>
  * Finally, each check can be inverted by prepending <code>.not()</code>. Here
  * is an example:
  * <p>
  * <code>Check.that(message).not().containsAny(badWords);</code>
  * <p>
  * Note that the <code>not()</code> only inverts the actual check and not the
  * null check. In the above example this would mean that message must still be
  * non-null. So null check inversion with <code>isNullOr()</code> is completely
  * independent of check inversion with <code>not()</code>.
  * <h3>Intended Use and Thread Safety</h3>
  * <p>
  * To provide optimal performance, the <code>that(...)</code> methods do not
  * create a new *Check object for every call. Instead, each overloaded method
  * always returns the same (but modified) object (in a given thread). Therefore,
  * you should always use those objects right after getting them by using the
  * fluent API. Never should you store them and using them later, not even in
  * local variables, as <em>any</em> method called in between may also use and
  * therefore modify them.
  * <p>
  * Thread safety is guaranteed by means of thread confinement. As each thread
  * receives its own *Check objects, and as long as they are not shared, the use
  * of those objects is thread safe.
  * <h3>Compact Syntax</h3>
  * <p>
  * Instead of the <code>Check.that(...)</code> syntax, you can use a even more
  * compact syntax, provided by the {@link CompactChecks} class.
  * <h3>Configuration and Use in Libraries</h3>
  * <p>
  * It is intentionally not possible to configure the type of exception the
  * checking methods throw. Because of this, the Better Checks library may also
  * safely be used in libraries (as opposed to applications), without the risk
  * that the application reconfigures the behavior of the library methods,
  * possibly breaking their specification.
  * <p>
  * In general, the configuration possibilities of this library are rather
  * limited. You can customize the exception messages and disable stack trace
  * cleaning. The only way to configure those settings is via a properties file
  * on the classpath. This is also a design decision that makes it possible to
  * safely use Better Checks in libraries and, more generally, in all code that
  * potentially runs before the application's initialization, such as static
  * initializers.
  * 
  * @author Michael Faes
  * @see CompactChecks
  */
 public abstract class Check {
     
     /*
      * Simple checks
      */
     
     public static void arguments(final boolean condition, final String message) {
         if(!condition)
             throw illegalArgumentException(message);
     }
     
     public static void state(final boolean condition, final String message) {
         if(!condition)
             throw illegalStateException(message);
     }
     
     /*
      * Fluent argument checks
      */
     
     public static ObjectCheck that(final Object argument) {
         return FluentChecks.getObjectCheck(ObjectCheck.class, argument);
     }
     
     public static StringCheck that(final String argument) {
         return FluentChecks.getObjectCheck(StringCheck.class, argument);
     }
     
     public static ObjectArrayCheck that(final Object[] argument) {
         return FluentChecks.getObjectCheck(ObjectArrayCheck.class, argument);
     }
     
     public static PrimitiveArrayCheck that(final boolean[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final byte[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final char[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final double[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final float[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final int[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final long[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static PrimitiveArrayCheck that(final short[] argument) {
         return FluentChecks.getPrimitiveArrayCheck(argument,
                 argument != null ? argument.length : -1);
     }
     
     public static CollectionCheck that(final Collection<?> argument) {
         return FluentChecks.<Collection<?>, CollectionCheck> getObjectCheck(
                 CollectionCheck.class, argument);
     }
     
     // IMPROVE: Create MapCheck
     
     public static NumberCheck that(final Number argument) {
         return FluentChecks.getObjectCheck(NumberCheck.class, argument);
     }
     
     public static UrlCheck that(final URL argument) {
         return FluentChecks.getObjectCheck(UrlCheck.class, argument);
     }
     
     public static IntCheck that(final int argument) {
         return FluentChecks.getIntCheck(argument);
     }
     
     public static LongCheck that(final long argument) {
         return FluentChecks.getLongCheck(argument);
     }
     
     public static DoubleCheck that(final double argument) {
         return FluentChecks.getDoubleCheck(argument);
     }
 }
