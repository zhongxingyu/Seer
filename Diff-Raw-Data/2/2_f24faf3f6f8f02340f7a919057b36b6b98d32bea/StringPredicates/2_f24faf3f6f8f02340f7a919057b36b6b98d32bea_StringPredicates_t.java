 package fi.jawsy.jawwa.lang;
 
 import java.io.Serializable;
 
 import com.google.common.base.Predicate;
 
 /**
  * Predicates for String objects.
  */
 public final class StringPredicates {
 
     private StringPredicates() {
     }
 
     private static class IsEmptyPredicate implements Predicate<String>, Serializable {
         private static final long serialVersionUID = -1705328419251983539L;
 
         @Override
         public boolean apply(String input) {
             return input.isEmpty();
         }
     }
 
    private static final IsEmptyPredicate IS_EMPTY = new IsEmptyPredicate();
 
     public static Predicate<? super String> isEmpty() {
         return IS_EMPTY;
     }
 
 }
