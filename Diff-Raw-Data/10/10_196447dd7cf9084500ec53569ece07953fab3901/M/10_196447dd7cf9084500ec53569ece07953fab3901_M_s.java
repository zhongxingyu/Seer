 package cx.ath.mancel01.utils;
 
 import cx.ath.mancel01.utils.F.Option;
 
 /**
  * Pattern matching for Java.
  *
  * @author Mathieu ANCELIN
  */
 public class M {
 
     public static <T> UnexpectedMatch<T> match(final T value) {
         return new UnexpectedMatch<T>() {
 
             @Override
             public <R> ExpectedMatch<T, R> andExcept(Class<R> returnType) {
                 return new ExpectedMatchImpl<T, R>(returnType, value);
             }
         };
     }
 
     public static interface ExecutableMatcher<T, R> {
 
         Option<R> execute(T value);
     }
 
     public static interface MatchCaseFunction<T, R> {
 
         Option<R> apply(T value);
     }
 
     public static interface UnexpectedMatch<T> {
 
         <R> ExpectedMatch<T, R> andExcept(Class<R> returnType);
     }
 
     public static interface ExpectedMatch<T, R> {
 
         Option<R> with(ExecutableMatcher<T, R>... matchers);
     }
 
     public static <T, R> Matcher<T, R> with(Matcher<T, R> matcher) {
         return matcher;
     }
 
     public static abstract class Matcher<T, R> {
 
         MatchCaseFunction<T, R> function;
 
         public abstract Option<R> match(T o);
 
         public <NR> Matcher<T, NR> and(final Matcher<R, NR> next) {
             final Matcher<T, R> first = this;
             return new Matcher<T, NR>() {
                 @Override
                 public Option<NR> match(T o) {
                     for (R r : first.match(o)) {
                         return next.match(r);
                     }
                     return Option.none();
                 }
             };
         }
 
         public ExecutableMatcher<T, R> then(MatchCaseFunction<T, R> function) {
             this.function = function;
             return new ExecutableMatcherImpl<T, R>(this);
         }
     }
 
     private static class ExecutableMatcherImpl<T, R> implements ExecutableMatcher<T, R> {
 
         private final Matcher<T, R> matcher;
 
         public ExecutableMatcherImpl(Matcher<T, R> matcher) {
             this.matcher = matcher;
         }
 
         @Override
         public Option<R> execute(T value) {
             Option<R> matched = matcher.match(value);
             if (matched.isDefined()) {
                 return matcher.function.apply(value);
             } else {
                 return Option.none();
             }
         }
     }
 
     private static class ExpectedMatchImpl<T, R> implements ExpectedMatch<T, R> {
 
         private final Class<R> returnType;
         private final T value;
 
         public ExpectedMatchImpl(Class<R> returnType, T value) {
             this.returnType = returnType;
             this.value = value;
         }
 
         @Override
         public Option<R> with(ExecutableMatcher<T, R>... matchers) {
             for (ExecutableMatcher matcher : matchers) {
                 Option<R> option = matcher.execute(value);
                 if (option.isDefined()) {
                     return option;
                 }
             }
             return Option.none();
         }
     }
 
    public static <K> Matcher<Object, Object> caseClassOf(final Class<K> clazz) {
        return new Matcher<Object, Object>() {
 
             @Override
            public Option<Object> match(Object o) {
                 if (clazz.isInstance(o)) {
                    return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseStartsWith(final String prefix) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.startsWith(prefix)) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseLengthGreater(final int than) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.length() > than) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseLengthGreaterEq(final int than) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.length() >= than) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseLengthLesser(final int than) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.length() < than) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseLengthLesserEq(final int than) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.length() <= than) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseLengthEquals(final int with) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.length() == with) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseRegex(final String pattern) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.matches(pattern)) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static <X> Matcher<X, X> caseEquals(final X other) {
         return new Matcher<X, X>() {
 
             @Override
             public Option<X> match(X o) {
                 if (o.equals(other)) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static Matcher<String, String> caseContains(final String contain) {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 if (o.contains(contain)) {
                     return Option.some(o);
                 }
                 return Option.none();
             }
         };
     }
 
     public static <X> Matcher<String, String> otherCases() {
         return new Matcher<String, String>() {
 
             @Override
             public Option<String> match(String o) {
                 return Option.some(o);
             }
         };
     }
 }
