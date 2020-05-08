 package org.datasetdiff;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.hamcrest.CoreMatchers;
 import org.junit.Assert;
 import org.junit.Test;
 import scala.Function1;
 import scala.math.BigDecimal;
 
 /**
  * Test to check compatibility from java.
  *
 * @author: agustafson
  */
 public class DatasetDiffJavaTest {
     @Test
     public void testDiff() {
         ClassLoader classLoader = this.getClass().getClassLoader();
 
         String fileName1 = "simple1.txt";
         String fileName2 = "simple2.txt";
         TabDelimitedInputDataset textFile1 = new TabDelimitedInputDataset(classLoader.getResourceAsStream(fileName1));
         TabDelimitedInputDataset textFile2 = new TabDelimitedInputDataset(classLoader.getResourceAsStream(fileName2));
 
         Function1 stringConverter = TextColumnConverters.StringConverter();
         Function1 numberConverter = TextColumnConverters.NumberConverter();
         Function1 dateConverterDDMMYYYY = TextColumnConverters.DateConverter("dd/MM/yyyy hh:mm");
         Function1 dateConverterYYYYMMDD = TextColumnConverters.DateConverter("yyyyMMdd");
 
         ComparatorFunction<String> stringComparator = new ComparatorFunction<String>() {
             @Override
             public boolean compare(String left, String right) {
                 return left.equalsIgnoreCase(right);
             }
         };
         ComparatorFunction<BigDecimal> bigDecimalComparator = new ComparatorFunction<BigDecimal>() {
             @Override
             public boolean compare(BigDecimal left, BigDecimal right) {
                 return left.underlying().subtract(right.underlying()).abs()
                         .subtract(new java.math.BigDecimal("0.0000001"))
                         .compareTo(java.math.BigDecimal.ZERO) <= 0;
             }
         };
         ComparatorFunction<Date> dateComparator = new ComparatorFunction<Date>() {
             @Override
             public boolean compare(Date left, Date right) {
                 SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
                 return dateFormatYYYYMMDD.format(left).equals(dateFormatYYYYMMDD.format(right));
             }
         };
 
         ConvertingColumnComparator<String, String, String> stringColumnComparator =
                 new ConvertingColumnComparator<String, String, String>(stringConverter, stringConverter, stringComparator);
         ConvertingColumnComparator<BigDecimal, String, String> numberColumnComparator =
                 new ConvertingColumnComparator<BigDecimal, String, String>(numberConverter, numberConverter, bigDecimalComparator);
         ConvertingColumnComparator<Date, String, String> dateColumnComparator =
                 new ConvertingColumnComparator<Date, String, String>(dateConverterDDMMYYYY, dateConverterYYYYMMDD, dateComparator);
 
         List<ComparisonResult[]> comparisons = new DatasetDiffBuilder<String, String>()
                 .withColumnComparator(0, stringColumnComparator)
                 .withColumnComparator(1, numberColumnComparator)
                 .withColumnComparator(2, dateColumnComparator)
                 .compare(textFile1, textFile2);
 
         Assert.assertEquals("comparison length", 3, comparisons.size());
         for (ComparisonResult[] rowComparison : comparisons) {
             for (ComparisonResult comparisonResult : rowComparison) {
                 Assert.assertThat(comparisonResult, CoreMatchers.instanceOf(MatchedComparisonResult.class));
             }
         }
     }
 }
