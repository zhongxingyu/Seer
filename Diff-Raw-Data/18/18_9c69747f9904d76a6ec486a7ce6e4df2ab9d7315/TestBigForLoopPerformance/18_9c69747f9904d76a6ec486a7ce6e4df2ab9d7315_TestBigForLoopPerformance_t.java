 package org.libj.xquery.performance;
 
 import org.junit.Test;
 
 import static org.libj.xquery.Asserts.*;
 
 public class TestBigForLoopPerformance {
     @Test
     public void bigFor() {
         /*
     for $i in (1 to 1*1000*1000)
     let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535
     where $i = 1
     return
         $i
          */
         String xquery = "    for $i in (1 to 1*1000*1000)\n" +
                 "    let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535\n" +
                 "    where $i = 1\n" +
                 "    return\n" +
                 "        $i";
         assertEvalMillis(xquery, 200);
     }
     @Test
     public void biggerFor() {
         // we are fine with 10,000,000 loop!
         /*
     for $i in (1 to 10*1000*1000)
     let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535
     where $i = 1
     return
         $i
          */
         String xquery = "    for $i in (1 to 10*1000*1000)\n" +
                 "    let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535\n" +
                 "    where $i = 1\n" +
                 "    return\n" +
                 "        $i";
         assertEvalMillis(xquery, 1000);
     }
     @Test
     public void bigForResult() {
         /*
     for $i in (1 to 1*1000*1000)
     let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535
     where $i != 1
     return
         $i
          */
         String xquery = "    for $i in (1 to 1*1000*1000)\n" +
                 "    let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535\n" +
                 "    where $i != 1\n" +
                 "    return\n" +
                 "        $i";
         assertEvalMillis(xquery, 200);
     }
     @Test
     public void biggerForResult() {
         // we are fine with 10,000,000 loop!
         /*
     for $i in (1 to 10*1000*1000)
     let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535
     where $i != 1
     return
         $i
          */
         String xquery = "    for $i in (1 to 10*1000*1000)\n" +
                 "    let $lv1 := 1000 * 100 * 100 * 1000 + 2234 + 7521 - 1.0 div 1.0 + 1024 div 3.1415926535\n" +
                 "    where $i != 1\n" +
                 "    return\n" +
                 "        $i";
         assertEvalMillis(xquery, 4000);
     }
 
 }
