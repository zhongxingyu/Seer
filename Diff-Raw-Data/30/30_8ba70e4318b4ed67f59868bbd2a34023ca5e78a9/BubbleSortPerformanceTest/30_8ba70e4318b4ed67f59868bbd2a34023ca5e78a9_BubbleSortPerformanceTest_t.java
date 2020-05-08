 package edu.msergey.jalg.sorting;
 
 import edu.msergey.jalg.sorting.base.SortWithProfiling;
 import org.joda.time.Period;
 
 public class BubbleSortPerformanceTest extends AbstractSortPerformanceTest {
     @Override
     protected SortWithProfiling<Integer> constructSort(Integer[] data) {
         return new BubbleSort<Integer>(data);
     }
 
     @Override
     protected long averageComparesCount(int dataLength) {
         return (long)dataLength * (long)dataLength / 2;
     }
 
     @Override
     protected long averageExchangesCount(int dataLength) {
         return (long)dataLength * (long)(dataLength - 1) / 4;
     }
 
     @Override
     protected long maximumComparesCount(int dataLength) {
         return (long)dataLength * (long)dataLength / 2;
     }
 
     @Override
     protected long maximumExchangesCount(int dataLength) {
         return (long)dataLength * (long)dataLength / 2;
     }
 
     @Override
     protected Period getExpectedRunningTime(int initialDataLength, Period initialPeriod, int dataLength) {
         double multiplier = Math.pow((double)dataLength / initialDataLength, 2);
        long expectedRunningTimeInMillis = Math.round(initialPeriod.getMillis() * multiplier);
         return new Period(expectedRunningTimeInMillis);
     }
 }
