 package edu.msergey.jalg.sorting;
 
 import edu.msergey.jalg.sorting.base.SortItem;
 import org.joda.time.Period;
import org.joda.time.PeriodType;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Extensions for more comfortable testing of sort methods.
  */
 public class AssertExtensions {
     /**
      * Asserts that sort is stable.
      * @param itemsAfterSort items after sort.
      */
     public static void assertSortIsStable(SortItem[] itemsAfterSort) {
         for (int i = 1; i < itemsAfterSort.length; i++) {
             if ((itemsAfterSort[i-1].getValue().equals(itemsAfterSort[i].getValue())) &&
                     (itemsAfterSort[i-1].getIndex() >= itemsAfterSort[i].getIndex())) {
                 fail(String.format("Value[%d] = %d, value[%d] = %d, index[%d] = %d, index[%d] = %d",
                     i - 1, itemsAfterSort[i-1].getValue(), i, itemsAfterSort[i].getValue(),
                     i - 1, itemsAfterSort[i-1].getIndex(), i, itemsAfterSort[i].getIndex()));
             }
         }
     }
 
     /**
      * Asserts that compares results used in sort is close enough to expected average compares.
      * "Close enough" actually means no more than 3% deviation from expected results.
      * This assert should be used on profiling result of sort of randomly generated data.
      * @param expectedAverageCompares expected average compares.
      * @param actualCompares actual compares used by sort.
      * @param dataLength data length of sorted data, it's needed to more informational output.
      */
     public static void assertSortProfilingIsCloseToAverageCompares(long expectedAverageCompares,
                                                                    long actualCompares,
                                                                    int dataLength) {
         System.out.println(
                 String.format("items - '%d', average compares - '%d', actual compares - '%d', deviation - '%d'",
                               dataLength, expectedAverageCompares, actualCompares,
                               expectedAverageCompares - actualCompares));
 
         assertTrue(expectedAverageCompares * 0.03 >= Math.abs(expectedAverageCompares - actualCompares));
     }
 
     /**
      * Asserts that exchanges results used in sort is close enough to expected average exchanges.
      * "Close enough" actually means no more than 3% deviation from expected results.
      * This assert should be used on profiling result of sort of randomly generated data.
      * @param expectedAverageExchanges expected average exchanges.
      * @param actualExchanges actual exchanges used by sort.
      * @param dataLength data length of sorted data, it's needed to more informational output.
      */
     public static void assertSortProfilingIsCloseToAverageExchanges(long expectedAverageExchanges,
                                                                     long actualExchanges,
                                                                     int dataLength) {
         System.out.println(
                 String.format("items - '%d', average exchanges - '%d', actual exchanges - '%d', deviation - '%d'",
                         dataLength, expectedAverageExchanges, actualExchanges,
                         expectedAverageExchanges - actualExchanges));
 
         assertTrue(expectedAverageExchanges * 0.03 >= Math.abs(expectedAverageExchanges - actualExchanges));
     }
 
     /**
      * Asserts that actual running time of sort is close enough to expected running time.
     * "Close enough" actually means no more than 7% deviation from expected results.
      * This assert should be used on profiling result of sort of randomly generated data.
      * @param expectedRunningTime expected running time.
      * @param actualRunningTime actual running time.
      * @param initialDataLength data length of initial sorted data, it's needed to more informational output.
      * @param actualDataLength data length of actual sorted data, it's needed to more informational output.
      */
     public static void assertSortProfilingIsCloseToRunningTime(Period expectedRunningTime,
                                                                Period actualRunningTime,
                                                                int initialDataLength,
                                                                int actualDataLength) {
         System.out.println(
                 String.format("items1 - '%d', items2 - '%d', expected time - '%s', actual time - '%s', deviation - '%s'",
                               initialDataLength, actualDataLength,
                               expectedRunningTime, actualRunningTime,
                               expectedRunningTime.minus(actualRunningTime)));
 
        double expectedDeviationInMillis = expectedRunningTime.normalizedStandard(PeriodType.millis()).getMillis() * 0.7;
        int actualDeviationInMillis = Math.abs(
                expectedRunningTime.minus(actualRunningTime).normalizedStandard(PeriodType.millis()).getMillis()
        );

        assertTrue(expectedDeviationInMillis >= actualDeviationInMillis);
     }
 }
