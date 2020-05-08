 package org.testory;
 
 import static org.testory.Formats.formatSection;
 import static org.testory.Formats.formatThrowable;
 import static org.testory.Samples.sample;
 import static org.testory.WhenEffect.whenEffect;
 import static org.testory.common.Closures.invoked;
 import static org.testory.common.Matchers.isMatcher;
 import static org.testory.common.Matchers.match;
 import static org.testory.common.Objects.areEqualDeep;
 import static org.testory.common.Throwables.gently;
 import static org.testory.proxy.Invocations.invoke;
 import static org.testory.proxy.Invocations.on;
 import static org.testory.proxy.Proxies.proxy;
 import static org.testory.proxy.Typing.typing;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.Arrays;
 import java.util.HashSet;
 
 import org.testory.common.Closure;
 import org.testory.common.Nullable;
 import org.testory.proxy.Handler;
 import org.testory.proxy.Invocation;
 import org.testory.proxy.Invocations;
 import org.testory.proxy.Typing;
 
 public class Testory {
   public static void givenTest(Object test) {
     for (final Field field : test.getClass().getDeclaredFields()) {
       if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
         AccessController.doPrivileged(new PrivilegedAction<Void>() {
           public Void run() {
             field.setAccessible(true);
             return null;
           }
         });
         try {
           Object value = field.get(test);
           boolean isInitialized = field.getType().isPrimitive()
               ? !value.equals(false) && !value.equals((char) 0) && !value.equals((byte) 0)
                   && !value.equals((short) 0) && !value.equals(0) && !value.equals((long) 0)
                   && !value.equals((float) 0) && !value.equals((double) 0)
               : value != null;
           if (!isInitialized) {
             field.set(test, mockOrSample(field.getType(), field.getName()));
           }
         } catch (RuntimeException e) {
           throw new TestoryException(e);
         } catch (IllegalAccessException e) {
           throw new Error(e);
         }
       }
     }
   }
 
   private static Object mockOrSample(Class<?> type, final String name) {
     if (!Modifier.isFinal(type.getModifiers())) {
       Typing typing = type.isInterface()
           ? typing(Object.class, new HashSet<Class<?>>(Arrays.asList(type)))
           : typing(type, new HashSet<Class<?>>());
       Handler handler = new Handler() {
         public Object handle(Invocation invocation) {
           if (invocation.method.getName().equals("toString")) {
             return name;
           }
           if (invocation.method.getName().equals("equals") && invocation.arguments.size() == 1) {
             return invocation.instance == invocation.arguments.get(0);
           }
           if (invocation.method.getName().equals("hashCode") && invocation.arguments.size() == 0) {
             return name.hashCode();
           }
           return null;
         }
       };
       return proxy(typing, handler);
     } else if (type.isArray()) {
       Class<?> componentType = type.getComponentType();
       Object array = Array.newInstance(componentType, 1);
       Array.set(array, 0, mockOrSample(componentType, name + "[0]"));
       return array;
     } else {
       return sample(type, name);
     }
   }
 
   /**
    * Reserved for future use.
    */
   @Deprecated
   public static Closure given(Closure closure) {
     throw new TestoryException("\n\tgiven(Closure) is confusing, do not use it\n");
   }
 
   public static <T> T given(T object) {
     return object;
   }
 
   public static void given(boolean primitive) {}
 
   public static void given(double primitive) {}
 
   public static <T> T givenTry(final T object) {
     check(object != null);
     Typing typing = typing(object.getClass(), new HashSet<Class<?>>());
     Handler handler = new Handler() {
       public Object handle(Invocation invocation) {
         Invocation onObjectInvocation = on(object, invocation);
         try {
           return invoke(onObjectInvocation);
         } catch (Throwable e) {
           return null;
         }
       }
     };
     return (T) proxy(typing, handler);
   }
 
   public static void givenTimes(int number, Closure closure) {
     check(number >= 0);
     check(closure != null);
     for (int i = 0; i < number; i++) {
       try {
         closure.invoke();
       } catch (Throwable throwable) {
         throw gently(throwable);
       }
     }
   }
 
   public static <T> T givenTimes(final int number, final T object) {
     check(number >= 0);
     check(object != null);
     Typing typing = typing(object.getClass(), new HashSet<Class<?>>());
     Handler handler = new Handler() {
       public Object handle(final Invocation invocation) throws Throwable {
         final Invocation onObjectInvocation = on(object, invocation);
         for (int i = 0; i < number; i++) {
           invoke(onObjectInvocation);
         }
         return null;
       }
     };
     return (T) proxy(typing, handler);
   }
 
   public static <T> T when(final T object) {
     whenEffect.set(new Closure() {
       public Object invoke() {
         return object;
       }
     });
     try {
       Typing typing = typing(object.getClass(), new HashSet<Class<?>>());
       Handler handler = new Handler() {
         public Object handle(final Invocation invocation) {
           final Invocation onObjectInvocation = on(object, invocation);
           Closure effect = invoked(new Closure() {
             public Object invoke() throws Throwable {
               return Invocations.invoke(onObjectInvocation);
             }
           });
           whenEffect.set(effect);
           return null;
         }
       };
       return (T) proxy(typing, handler);
     } catch (RuntimeException e) {
       return null;
     }
   }
 
   public static void when(Closure closure) {
     check(closure != null);
     whenEffect.set(invoked(closure));
   }
 
   public static void when(boolean value) {
     when((Object) value);
   }
 
   public static void when(char value) {
     when((Object) value);
   }
 
   public static void when(byte value) {
     when((Object) value);
   }
 
   public static void when(short value) {
     when((Object) value);
   }
 
   public static void when(int value) {
     when((Object) value);
   }
 
   public static void when(long value) {
     when((Object) value);
   }
 
   public static void when(float value) {
     when((Object) value);
   }
 
   public static void when(double value) {
     when((Object) value);
   }
 
   public static void thenReturned(@Nullable Object objectOrMatcher) {
     Closure effect = getWhenEffect();
     Object object;
     try {
       object = effect.invoke();
     } catch (Throwable throwable) {
       throw assertionError("\n" //
           + formatSection("expected returned", objectOrMatcher) //
           + formatSection("but thrown", throwable) //
           + "\n" //
           + formatThrowable(throwable));
     }
     if (!areEqualDeep(objectOrMatcher, object)
         && !(objectOrMatcher != null && isMatcher(objectOrMatcher) && match(objectOrMatcher, object))) {
       throw assertionError("\n" //
           + formatSection("expected returned", objectOrMatcher) //
           + formatSection("but returned", object));
     }
   }
 
   public static void thenReturned(boolean value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(char value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(byte value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(short value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(int value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(long value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(float value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned(double value) {
     thenReturned((Object) value);
   }
 
   public static void thenReturned() {
     Closure effect = getWhenEffect();
     try {
       effect.invoke();
     } catch (Throwable throwable) {
       throw assertionError("\n" //
           + formatSection("expected returned", "") //
           + formatSection("but thrown", throwable) //
           + "\n" //
           + formatThrowable(throwable));
     }
   }
 
   public static void thenThrown(Object matcher) {
     check(matcher != null);
     check(isMatcher(matcher));
     Closure effect = getWhenEffect();
     Object object;
     try {
       object = effect.invoke();
     } catch (Throwable throwable) {
       if (!match(matcher, throwable)) {
         throw assertionError("\n" //
             + formatSection("expected thrown throwable matching", matcher) //
             + formatSection("but thrown", throwable) //
             + "\n" //
             + formatThrowable(throwable));
       }
       return;
     }
     throw assertionError("\n" //
         + formatSection("expected thrown throwable matching", matcher) //
         + formatSection("but returned", object));
   }
 
   public static void thenThrown(Throwable expectedThrowable) {
     check(expectedThrowable != null);
     Closure effect = getWhenEffect();
     Object object;
     try {
       object = effect.invoke();
     } catch (Throwable throwable) {
       if (!areEqualDeep(expectedThrowable, throwable)) {
         throw assertionError("\n" //
             + formatSection("expected thrown", expectedThrowable) //
             + formatSection("but thrown", throwable) //
             + "\n" //
             + formatThrowable(throwable));
       }
       return;
     }
     throw assertionError("\n" //
         + formatSection("expected thrown", expectedThrowable) //
         + formatSection("but returned", object));
   }
 
   public static void thenThrown(Class<? extends Throwable> type) {
     check(type != null);
     Closure effect = getWhenEffect();
     Object object;
     try {
       object = effect.invoke();
     } catch (Throwable throwable) {
       if (!type.isInstance(throwable)) {
         throw assertionError("\n" //
             + formatSection("expected thrown instance of", type.getName()) //
             + formatSection("but thrown instance of", throwable.getClass().getName()) //
             + "\n" //
             + formatThrowable(throwable));
       }
       return;
     }
     throw assertionError("\n" //
         + formatSection("expected thrown instance of", type.getName()) //
         + formatSection("but returned", object));
   }
 
   public static void thenThrown() {
     Closure effect = getWhenEffect();
     Object object;
     try {
       object = effect.invoke();
     } catch (Throwable throwable) {
       return;
     }
     throw assertionError("\n" //
         + formatSection("expected thrown", "") //
         + formatSection("but returned", object));
   }
 
  public static void then(boolean condition) {
    if (!condition) {
      throw assertionError("\n" //
          + formatSection("expected", "true") //
          + formatSection("but was", "false"));
    }
  }

   public static void then(@Nullable Object object, Object matcher) {
     check(matcher != null);
     check(isMatcher(matcher));
     if (!match(matcher, object)) {
       throw assertionError("\n" //
           + formatSection("expected object matching", matcher) //
           + formatSection("but was", object));
     }
   }
 
   public static void thenEqual(@Nullable Object object, @Nullable Object expected) {
     if (!areEqualDeep(object, expected)) {
       throw assertionError("\n" //
           + formatSection("expected", expected) //
           + formatSection("but was", object));
     }
   }
 
   private static void check(boolean condition) {
     if (!condition) {
       throw new TestoryException();
     }
   }
 
   private static TestoryAssertionError assertionError(String message) {
     TestoryAssertionError error = new TestoryAssertionError(message);
     cloakStackTrace(error);
     return error;
   }
 
   private static void cloakStackTrace(Throwable throwable) {
     StackTraceElement[] stackTrace = throwable.getStackTrace();
 
     int index = -1;
     for (int i = stackTrace.length - 1; i >= 0; i--) {
       if (stackTrace[i].getClassName().equals(Testory.class.getName())) {
         index = i;
         break;
       }
     }
     if (index == -1 || index == stackTrace.length - 1) {
       throw new Error();
     }
     throwable.setStackTrace(new StackTraceElement[] { stackTrace[index + 1] });
   }
 
   private static Closure getWhenEffect() {
     Closure effect = whenEffect.get();
     check(effect != null);
     return effect;
   }
 }
