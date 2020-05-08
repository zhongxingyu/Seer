 package edu.msergey.jalg.sorting;
 
 import edu.msergey.jalg.sorting.base.SortWithProfiling;
 import org.joda.time.Period;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.junit.Assert.assertTrue;
 import static edu.msergey.jalg.sorting.AssertExtensions.assertSortProfilingIsCloseToAverageCompares;
 import static edu.msergey.jalg.sorting.AssertExtensions.assertSortProfilingIsCloseToAverageExchanges;
 import static edu.msergey.jalg.sorting.AssertExtensions.assertSortProfilingIsCloseToRunningTime;
 
 public abstract class AbstractSortPerformanceTest {
     protected final static Map<Integer, ProfilingResult> dataLengthToProfilingResultsMap =
             new HashMap<Integer, ProfilingResult>();
     
     protected abstract SortWithProfiling<Integer> constructSort(Integer[] data);
 
     protected abstract long averageComparesCount(int dataLength);
     protected abstract long maximumComparesCount(int dataLength);
     protected abstract long averageExchangesCount(int dataLength);
     protected abstract long maximumExchangesCount(int dataLength);
     protected abstract Period getExpectedRunningTime(int initialDataLength, Period initialPeriod, int dataLength);
 
     @BeforeClass
     public static void oneTimeSetUp() {
         dataLengthToProfilingResultsMap.clear();
     }
 
     @Test
     public void test_sort_running_time_is_appropriate() throws IOException {
         int initialDataLength = 10000;
         int actualDataLength = 50000;
 
         ProfilingResult initialProfilingResult = getProfilingResultOfSort(initialDataLength);
         Period initialRunningTime = initialProfilingResult.getRunningTime();
         Period expected = getExpectedRunningTime(initialDataLength, initialRunningTime, actualDataLength);
         Period actual = getProfilingResultOfSort(actualDataLength).getRunningTime();
         assertSortProfilingIsCloseToRunningTime(expected, actual, initialDataLength, actualDataLength);
     }
 
     @Test
     public void test_sort_average_compares_count_for_ten_thousands_integers() throws IOException {
         final int dataLength = 10000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long expected = averageComparesCount(dataLength);
         final long actual = profilingResult.getComparesCount();
         assertSortProfilingIsCloseToAverageCompares(expected, actual, dataLength);
     }
 
     @Test
     public void test_sort_average_compares_count_for_fifty_thousands_integers() throws IOException {
         final int dataLength = 50000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long expected = averageComparesCount(dataLength);
         final long actual = profilingResult.getComparesCount();
         assertSortProfilingIsCloseToAverageCompares(expected, actual, dataLength);
     }
 
     @Test
     public void test_sort_average_compares_count_for_one_hundred_thousands_integers() throws IOException {
         final int dataLength = 100000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long expected = averageComparesCount(dataLength);
         final long actual = profilingResult.getComparesCount();
         assertSortProfilingIsCloseToAverageCompares(expected, actual, dataLength);
     }
 
     @Test
     public void test_sort_not_exceed_maximum_compares_count_for_ten_thousands_integers() throws IOException {
         final int dataLength = 10000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long maximum = maximumComparesCount(dataLength);
         final long actual = profilingResult.getComparesCount();
         assertTrue(maximum >= actual);
     }
 
     @Test
     public void test_sort_not_exceed_maximum_compares_count_for_fifty_thousands_integers() throws IOException {
         final int dataLength = 50000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long maximum = maximumComparesCount(dataLength);
         final long actual = profilingResult.getComparesCount();
         assertTrue(maximum >= actual);
     }
 
     @Test
     public void test_sort_not_exceed_maximum_compares_count_for_one_hundred_thousands_integers() throws IOException {
         final int dataLength = 100000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long maximum = maximumComparesCount(dataLength);
         final long actual = profilingResult.getComparesCount();
         assertTrue(maximum >= actual);
     }
 
     @Test
     public void test_sort_average_exchanges_count_for_ten_thousands_integers() throws IOException {
         final int dataLength = 10000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long expected = averageExchangesCount(dataLength);
         final long actual = profilingResult.getExchangesCount();
         assertSortProfilingIsCloseToAverageExchanges(expected, actual, dataLength);
     }
 
     @Test
     public void test_sort_average_exchanges_count_for_fifty_thousands_integers() throws IOException {
         final int dataLength = 50000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long expected = averageExchangesCount(dataLength);
         final long actual = profilingResult.getExchangesCount();
         assertSortProfilingIsCloseToAverageExchanges(expected, actual, dataLength);
     }
 
     @Test
     public void test_sort_average_exchanges_count_for_one_hundred_thousands_integers() throws IOException {
         final int dataLength = 100000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long expected = averageExchangesCount(dataLength);
         final long actual = profilingResult.getExchangesCount();
         assertSortProfilingIsCloseToAverageExchanges(expected, actual, dataLength);
     }
 
     @Test
     public void test_sort_not_exceed_maximum_exchanges_count_for_ten_thousands_integers() throws IOException {
         final int dataLength = 10000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long maximum = maximumExchangesCount(dataLength);
         final long actual = profilingResult.getExchangesCount();
         assertTrue(maximum >= actual);
     }
 
     @Test
     public void test_sort_not_exceed_maximum_exchanges_count_for_fifty_thousands_integers() throws IOException {
         final int dataLength = 50000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long maximum = maximumExchangesCount(dataLength);
         final long actual = profilingResult.getExchangesCount();
         assertTrue(maximum >= actual);
     }
 
     @Test
     public void test_sort_not_exceed_maximum_exchanges_count_for_one_hundred_thousands_integers() throws IOException {
         final int dataLength = 100000;
         ProfilingResult profilingResult = getProfilingResultOfSort(dataLength);
 
         final long maximum = maximumExchangesCount(dataLength);
         final long actual = profilingResult.getExchangesCount();
         assertTrue(maximum >= actual);
     }
 
     private ProfilingResult getProfilingResultOfSort(Integer dataLength) throws IOException {
         if (!dataLengthToProfilingResultsMap.containsKey(dataLength))
         {
             Integer[] data = DataGenerator.loadRandomlyPregeneratedIntegers(dataLength);
             SortWithProfiling<Integer> sort = constructSort(data);
             long start = System.currentTimeMillis();
             sort.sort();
            long runningTimeInMillis = System.currentTimeMillis() - start;
             ProfilingResult result = new ProfilingResult(sort.getExchangesCount(), sort.getComparesCount(),
                                                         new Period(runningTimeInMillis));
             dataLengthToProfilingResultsMap.put(dataLength, result);
             return result;
         }
         return dataLengthToProfilingResultsMap.get(dataLength);
     }
 }
