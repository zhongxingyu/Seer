 package minandmax;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.name.Named;
 import minandmax.comparer.Comparer;
 import minandmax.comparer.ComparerFactory;
 import minandmax.comparison.Comparison;
 
 public class StringMinAndMaxFinder {
     private final Comparer<String> comparer;
     private final Comparison<String> lessThan;
    private final Comparison<String> greaterThan;
     
     @Inject
     StringMinAndMaxFinder(ComparerFactory<String> comparerFactory,
                           @Named("Less Than") Comparison<String> lessThan,
                          @Named("Greater Than") Comparison<String> greaterThan,
                           @Assisted String... elements) {
         this.comparer = comparerFactory.create(elements);
         this.lessThan = lessThan;
        this.greaterThan = greaterThan;
     }
 
     public String min() {
         return comparer.compare(lessThan);
     }
 
     public String max() {
        return comparer.compare(greaterThan);
     }
 }
