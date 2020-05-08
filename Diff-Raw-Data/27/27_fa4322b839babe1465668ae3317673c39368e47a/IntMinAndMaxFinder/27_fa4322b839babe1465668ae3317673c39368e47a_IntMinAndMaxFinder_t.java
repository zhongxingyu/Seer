 package minandmax;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.name.Named;
 import minandmax.comparer.Comparer;
 import minandmax.comparer.ComparerFactory;
 import minandmax.comparison.Comparison;
 import minandmax.comparison.IntGreaterThanBelowLimitFactory;
 
 public class IntMinAndMaxFinder {
     private final Comparer<Integer> comparer;
     private final Comparison<Integer> lessThan;
    private final Comparison<Integer> greaterThan;
     private final IntGreaterThanBelowLimitFactory intGreaterThanBelowLimitFactory;
 
     @Inject
     IntMinAndMaxFinder(ComparerFactory<Integer> comparerFactory,
                        @Named("Less Than") Comparison<Integer> lessThan,
                       @Named("Greater Than") Comparison<Integer> greaterThan,
                        IntGreaterThanBelowLimitFactory intGreaterThanBelowLimitFactory,
                        @Assisted Integer... elements) {
         this.comparer = comparerFactory.create(elements);
         this.lessThan = lessThan;
        this.greaterThan = greaterThan;
         this.intGreaterThanBelowLimitFactory = intGreaterThanBelowLimitFactory;
     }
 
     public Integer min() {
         return comparer.compare(lessThan);
     }
 
     public Integer max() {
        return comparer.compare(greaterThan);
     }
 
     public Integer maxBelow(Integer limit) {
         return comparer.compare(intGreaterThanBelowLimitFactory.create(limit));
     }
 }
